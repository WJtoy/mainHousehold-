package com.wolfking.jeesite.modules.md.web;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kkl.kklplus.entity.md.MDServicePointAutoPlan;
import com.kkl.kklplus.entity.md.dto.MDServicePointAutoPlanDto;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.utils.GsonUtils;
import com.wolfking.jeesite.modules.md.entity.ProductCategory;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.md.entity.ServicePointStation;
import com.wolfking.jeesite.modules.md.service.ServicePointService;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.entity.viewModel.AreaModel;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providermd.service.MSProductCategoryNewService;
import com.wolfking.jeesite.ms.providermd.service.MSServicePointAutoPlanService;
import com.wolfking.jeesite.ms.providermd.service.MSServicePointService;
import com.wolfking.jeesite.ms.providermd.service.MSServicePointStationService;
import com.wolfking.jeesite.ms.providersys.service.MSSysAreaService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("${adminPath}/md/servicePointAutoPlan")
@Slf4j
public class ServicePointAutoPlanController {

    @Autowired
    private MSServicePointService msServicePointService;

    @Autowired
    private ServicePointService servicePointService;

    @Autowired
    private AreaService areaService;

    @Autowired
    private MSServicePointStationService msServicePointStationService;

    @Autowired
    private MSProductCategoryNewService msProductCategoryNewService;

    @Autowired
    private MSServicePointAutoPlanService msServicePointAutoPlanService;

    @Autowired
    private MSSysAreaService msSysAreaService;

    @RequiresPermissions(value = "md:servicepointautoplan:edit")
    @RequestMapping(value = "batchForm")
    public String batchForm(ServicePoint servicePoint, HttpServletRequest request, HttpServletResponse response, Model model) {
        //
        // 为网点批量添加服务区域
        //
        servicePoint = msServicePointService.getSimpleById(servicePoint.getId());
        if (servicePoint == null) {
            return "modules/md/servicePointAutoPlanBatchForm";
        }

        // 查询该网点下所要服务的区域列表(区域到街道/乡镇)
        List<AreaModel> areaModels = Lists.newArrayList();
        List<Area> serviceAreas = servicePointService.getAreas(servicePoint.getId());
        if (!ObjectUtils.isEmpty(serviceAreas)) {
            // 获取区/县区域列表
            serviceAreas = serviceAreas.stream().filter(r->r.getType().equals(Area.TYPE_VALUE_COUNTY)).sorted(Comparator.comparing(r->r.getParent().getId())).collect(Collectors.toList());

            // 获取市级区域列表
            List<Long> parentAreaIds = serviceAreas.stream().map(r->r.getParentId()).distinct().collect(Collectors.toList());
            List<Area> parentAreas = Lists.newArrayList();
            if (!ObjectUtils.isEmpty(parentAreaIds)) {
                for (List<Long> longList : Lists.partition(parentAreaIds, 100)) {
                   List<Area> partAreaList = msSysAreaService.findListByAreaIdList(longList);
                   if (!ObjectUtils.isEmpty(partAreaList)) {
                       parentAreas.addAll(partAreaList);
                   }
                }
            }
            // 将获取的市级区域列表转换为Map对象
            Map<Long, Area> parentAreaMap = !ObjectUtils.isEmpty(parentAreas)?parentAreas.stream().collect(Collectors.toMap(r->r.getId(),r->r)): Maps.newHashMap();

            // 获取区/县区域id列表
            List<Long> districtAreaIds = !ObjectUtils.isEmpty(serviceAreas)? serviceAreas.stream().map(r->r.getId()).distinct().collect(Collectors.toList()) : Lists.newArrayList();
            // 根据区域ids获取当前网点下所有的街道/乡/镇区域id
            List<Area> totalSubAreaList = Lists.newArrayList();
            Map<Long, List<Area>> subAreaMap = Maps.newHashMap();
            if (!ObjectUtils.isEmpty(districtAreaIds)) {
                for (List<Long> longList : Lists.partition(districtAreaIds, 10)) {
                    List<Area> partSubAreaList = msSysAreaService.findListByTypeAndParentIds(Area.TYPE_VALUE_TOWN, longList);
                    if (!ObjectUtils.isEmpty(partSubAreaList)) {
                        totalSubAreaList.addAll(partSubAreaList);
                    }
                }
                subAreaMap = totalSubAreaList.stream().collect(Collectors.groupingBy(r->r.getParentId()));
            }

            Map<Long, List<Area>> finalSubAreaMap = subAreaMap;
            serviceAreas.stream().forEach(r->{
                AreaModel areaModel = new AreaModel();
                BeanUtils.copyProperties(r,areaModel);
                Area parentArea = parentAreaMap.get(r.getParentId());
                if (parentArea != null) {
                    areaModel.setParent(parentArea);
                }
                List<Area> subAreaList = finalSubAreaMap.get(r.getId());
                if (!ObjectUtils.isEmpty(subAreaList)) {
                    subAreaList = subAreaList.stream().sorted(Comparator.comparing(area->area.getSort())).collect(Collectors.toList());
                    areaModel.setSubAreas(subAreaList);
                }
                areaModels.add(areaModel);
            });
        }

        // 查询该网点下已经建好的服务街道
        ServicePointStation servicePointStation = ServicePointStation.builder()
                .servicePoint(servicePoint)
                .build();
        List<ServicePointStation> servicePointStationList = msServicePointStationService.findSpecList(servicePointStation);
        List<Long> areaIds = Lists.newArrayList();
        if (!ObjectUtils.isEmpty(servicePointStationList)) {
            areaIds = servicePointStationList.stream()
                    .map(ServicePointStation::getArea)
                    .map(Area::getId)
                    .collect(Collectors.toList());
        }

        List<ProductCategory> productCategoryList = msProductCategoryNewService.findAllListForMDWithEntity();
//        if (!ObjectUtils.isEmpty(productCategoryList)) {
//            productCategoryList = productCategoryList.stream().sorted(Comparator.comparing(r->r.getId())).collect(Collectors.toList());
//        }

        // 查询自动派单记录
        MDServicePointAutoPlan mdServicePointAutoPlan = new MDServicePointAutoPlan();
        mdServicePointAutoPlan.setServicePointId(servicePoint.getId());
        List<MDServicePointAutoPlan> mdServicePointAutoPlanList = msServicePointAutoPlanService.findList(mdServicePointAutoPlan);

        model.addAttribute("list",areaModels);
        model.addAttribute("servicePoint", servicePoint);
        model.addAttribute("areaIdList", areaIds);
        model.addAttribute("productCategoryList", productCategoryList);
        model.addAttribute("autoPlanList", GsonUtils.toGsonString(mdServicePointAutoPlanList));

        return "modules/md/servicePointAutoPlanBatchForm";
    }

    @ResponseBody
    @RequestMapping("/batchSave")
    public AjaxJsonEntity batchSave(@RequestBody MDServicePointAutoPlanDto servicePointAutoPlanDto) {
        AjaxJsonEntity jsonEntity = new AjaxJsonEntity();
        try {
            // log.warn("{}", servicePointAutoPlanDto);
            msServicePointAutoPlanService.batchSave(servicePointAutoPlanDto);
            jsonEntity.setSuccess(true);
        } catch (Exception ex) {
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage("保存数据失败.失败原因:"+ex.getMessage());
        }

        return jsonEntity;
    }

    @ResponseBody
    @RequestMapping("/syncAutoPlan")
    public AjaxJsonEntity syncAutoPlan() {
        AjaxJsonEntity jsonEntity = new AjaxJsonEntity();
        try {
            msServicePointAutoPlanService.pushAllServicePointStationMessageToES();
            jsonEntity.setSuccess(true);
        } catch (Exception ex) {
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage("同步数据失败.失败原因:"+ex.getMessage());
        }
        return jsonEntity;
    }
}
