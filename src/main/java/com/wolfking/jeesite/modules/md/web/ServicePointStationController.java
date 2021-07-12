package com.wolfking.jeesite.modules.md.web;


import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.entity.es.mq.MQSyncType;
import com.wolfking.jeesite.common.config.redis.GsonIgnoreStrategy;
import com.wolfking.jeesite.common.config.redis.GsonRedisSerializer;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.PlanRadius;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.md.entity.ServicePointStation;
import com.wolfking.jeesite.modules.md.entity.ServicePointStatus;
import com.wolfking.jeesite.modules.md.service.PlanRadiusService;
import com.wolfking.jeesite.modules.md.service.ServicePointService;
import com.wolfking.jeesite.modules.md.service.ServicePointStationService;
import com.wolfking.jeesite.modules.sd.entity.TwoTuple;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.entity.viewModel.AreaModel;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.utils.AreaUtils;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providermd.service.MSServicePointService;
import com.wolfking.jeesite.ms.providermd.service.MSServicePointStationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping(value = "${adminPath}/md/servicepointstation")
public class ServicePointStationController extends BaseController {

    @Autowired
    private ServicePointStationService servicePointStationService;

    @Autowired
    private ServicePointService servicePointService;

    @Autowired
    private GsonRedisSerializer gsonRedisSerializer;

    @Autowired
    private AreaService areaService;

    @Autowired
    private PlanRadiusService planRadiusService;

    @Autowired
    private MSServicePointService msServicePointService;

    @Autowired
    private MSServicePointStationService msServicePointStationService;

    /*
    // mark on 2019-10-3
    @ModelAttribute
    public ServicePointStation get(@RequestParam(required=false) Long id,@RequestParam(required=false) Long servicePointId) {
        return Optional.ofNullable(id).map(r->{
            return servicePointStationService.get(servicePointId,id);
        }).orElse(ServicePointStation.builder().servicePoint(servicePointId==null?null:new ServicePoint(servicePointId)).build());
    }
    */


    @RequiresPermissions(value = {"md:servicepointstation:view"})
    @RequestMapping(value = "areaStationList")
    public String areaStationList(ServicePoint servicePoint,HttpServletRequest request, HttpServletResponse response,Model model) {
        //Long areaId = 1609L;
        //Area area = areaService.getFromCache(areaId);
        if (ObjectUtils.isEmpty(servicePoint.getArea())) {
            //servicePoint.setArea(area);
            return "modules/md/servicePointStationAreaList";
        } else {
            Area cachedArea = areaService.getFromCache(servicePoint.getArea().getId());
            servicePoint.setArea(cachedArea);
        }

        // 区域服务点列表
        Long parentId = servicePoint.getArea().getId(); //获得区县级id
        if (parentId == null) {
            return "modules/md/servicePointStationAreaList";
        }

        List<ServicePointStation> servicePointStationlList = servicePointStationService.findServicePointStationAreaList(parentId);

        model.addAttribute("servicePoint",servicePoint);
        model.addAttribute("list",servicePointStationlList);

        return "modules/md/servicePointStationAreaList";
    }



    @RequiresPermissions(value = {"md:servicepointstation:view"})
    @RequestMapping(value = "amap")
    public String toAmap(ServicePoint servicePoint, HttpServletRequest request, HttpServletResponse response,Model model) {
        String strAreaFullName = "广东省佛山市顺德区";
        Long areaId = 1609L;  // 顺德区

        if (!ObjectUtils.isEmpty(servicePoint.getArea()) && servicePoint.getArea().getId() != null) {
            areaId = servicePoint.getArea().getId();
        }

        Area area = areaService.getFromCache(areaId);
        if (!ObjectUtils.isEmpty(area)) {
            strAreaFullName = area.getFullName();
            servicePoint.setArea(area);
        } else {
            servicePoint.setArea(new Area(areaId));
        }

        String[] areaArray = AreaUtils.getLocation(strAreaFullName);
        Double centerLongtitude =0D,
                centerLatitude = 0D;

        if (!ObjectUtils.isEmpty(areaArray) && areaArray.length == 2) {
            centerLongtitude = Double.valueOf(areaArray[0]);
            centerLatitude = Double.valueOf(areaArray[1]);
        }

        // 获取网点下的所有网点
        servicePoint.setOrderBy("s.auto_plan_flag desc");
        servicePoint.setServicePointNo(servicePoint.getServicePointNo().trim());//去除空格
        servicePoint.setName(servicePoint.getName().trim());
        if (servicePoint.getFirstSearch() == 1) {
            servicePoint.setAutoPlanFlag(1);  //是否自动派单,为-1时查询条件被忽略
            servicePoint.setFirstSearch(0);
        }
        if (!ObjectUtils.isEmpty(servicePoint.getFinance())) {
            servicePoint.getFinance().setInvoiceFlag(-1);
            servicePoint.getFinance().setDiscountFlag(-1);  // 是否折扣,为-1时查询条件被忽略
        }
        servicePoint.setInsuranceFlag(-1);   //购买保险
        servicePoint.setTimeLinessFlag(-1);  //快可立补贴
        servicePoint.setUseDefaultPrice(-1); //结算标准

        servicePoint.setStatus(new Dict(ServicePointStatus.NORMAL.getValue()+""));
        Page<ServicePoint> page = servicePointService.findServicePointListForStation(new Page<>(request, response), servicePoint);

        // 获取网点下的所有有效服务点
        ServicePointStation servicePointStationEntity = ServicePointStation.builder().build();
        servicePointStationEntity.setArea(area);
        ServicePoint servicePointEntity =new ServicePoint();
        servicePointEntity.setAutoPlanFlag(1);
        servicePointEntity.setLevel(servicePoint.getLevel());
        servicePointEntity.setServicePointNo(servicePoint.getServicePointNo());
        servicePointStationEntity.setServicePoint(servicePointEntity);
        //List<ServicePointStation> servicePointStationList = servicePointStationService.findAutoPlanList(servicePointStationEntity); //mark on 2019-10-4
        List<ServicePointStation> servicePointStationList = null;  // add on 2019-10-4

        Gson gson = new GsonBuilder()
                .addSerializationExclusionStrategy(new GsonIgnoreStrategy())
                .setPrettyPrinting()
                .create();

        String strJson = "{}";
        if (!org.springframework.util.ObjectUtils.isEmpty(servicePointStationList)) {
            strJson = gson.toJson(servicePointStationList);
            //System.out.println("servicePointStationList gson:"+ gson.toJson(servicePointStationList));
        }

        model.addAttribute("centerLng",centerLongtitude);
        model.addAttribute("centerLat",centerLatitude);
        model.addAttribute("servicePointStationList",strJson);

        model.addAttribute("page", page);
        model.addAttribute("servicePoint", servicePoint);
        return "modules/md/servicePointStationGaoDe";
    }

    /*
    //mark  on 2019-7-27
    @RequiresPermissions(value = {"md:servicepointstation:view","md:servicepointstation:edit"},logical = Logical.OR)
    @RequestMapping(value = "form")
    public String form(ServicePointStation servicePointStation,HttpServletRequest request, Model model) {
        User user = UserUtils.getUser();
        String strVia = request.getParameter("via");  // 获取调用方

        Optional.ofNullable(servicePointStation)
                .map(ServicePointStation::getServicePoint)
                .map(ServicePoint::getId)
                .ifPresent(c->{
                    ServicePoint point = servicePointService.get(c.longValue());
                    servicePointStation.setServicePoint(point);
                });

        model.addAttribute("servicePointStation", servicePointStation);
        model.addAttribute("via",strVia);

        return "modules/md/servicePointStationForm";
    }
    */

    /*
    // mark on 2019-7-27
    @RequiresPermissions("md:servicepointstation:edit")
    @RequestMapping(value = "save")
    public String save(ServicePointStation servicePointStation, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        if (!beanValidator(model, servicePointStation)){
            return form(servicePointStation, request,model);
        }
        servicePointStation.setDelFlag(ServicePoint.DEL_FLAG_NORMAL);

        String strVia = request.getParameter("via");  // 获取调用方


//        if (!ObjectUtils.isEmpty(servicePointStation.getRadius())) {  //将从前端获得的半径从千米换算成米
//            servicePointStation.setRadius(servicePointStation.getRadius()*1000);
//        }

        //servicepoint
        ServicePoint servicePoint = servicePointService.getFromCache(servicePointStation.getServicePoint().getId());
        if(!ObjectUtils.isEmpty(servicePoint)) {
            servicePointStation.setServicePoint(servicePoint);
        }
        //区域列表
        StringBuilder json = new StringBuilder(servicePointStation.getAreas());
        List<Area> areaList = Lists.newArrayList();
        if(json.length()>0){
            json = new StringBuilder(json.toString().replace("&quot;","\""));
            areaList = Arrays.asList((Area[])gsonRedisSerializer.fromJson(json.toString(),Area[].class));
        }

        if (!ObjectUtils.isEmpty(areaList)) {
            Long areaId = areaList.stream().findFirst().get().getId();
            Area area = areaService.getFromCache(areaId);
            servicePointStation.setArea(area);

            if (StringUtils.isEmpty(servicePointStation.getAddress())) {
                servicePointStation.setAddress("");
            }

            String strFullAddressName = "";
            if (area.getType().equals(Area.TYPE_VALUE_TOWN)) {
                strFullAddressName = area.getFullName();   // new code
            } else {
                strFullAddressName = area.getFullName().concat(servicePointStation.getAddress());
            }
            String[] areaArray = AreaUtils.getLocation(strFullAddressName);

            Double dblLongtitude = 0D;
            Double dblLatitude = 0D;
            if (!org.springframework.util.ObjectUtils.isEmpty(areaArray)) {
                if (areaArray.length == 2) {
                    dblLongtitude = Double.valueOf(areaArray[0]);
                    dblLatitude = Double.valueOf(areaArray[1]);

                    System.out.println(String.format("网点地址转换成坐标为{longtitude:%s,latitude:%s}", dblLongtitude, dblLatitude));
                    servicePointStation.setLongtitude(dblLongtitude);
                    servicePointStation.setLatitude(dblLatitude);
                }
            } else {
                addMessage(model, "你输入的地址不正确.");
                return form(servicePointStation,request,model);
            }
        }
        try {
            if (servicePointStation.getRadius() == null) {
                servicePointStation.setRadius(0);
            }
            if (StringUtils.isEmpty(servicePointStation.getAddress())) {
                servicePointStation.setAddress("");
            }
            servicePointStationService.save(servicePointStation);
            addMessage(redirectAttributes, "保存服务点'" + servicePointStation.getName() + "'成功");
            if (strVia.toLowerCase().equals("arealist")) {
                return String.format("redirect:%s/md/servicepointstation/areaStationList?repage&area.id=%s", adminPath,servicePointStation.getArea().getParentId());
            } else {
                return String.format("redirect:%s/md/servicepoint/selectForStation?repage", adminPath);
            }
        }catch (Exception ex){
            //ex.printStackTrace();
            return form(servicePointStation,request,model);
        }
    }
    */

    /*
    // mark on 2019-7-27
    @RequiresPermissions(value = {"md:servicepointstation:view","md:servicepointstation:edit"},logical = Logical.OR)
    @RequestMapping(value = "areaForm")
    public String areaForm(ServicePointStation servicePointStation,HttpServletRequest request, HttpServletResponse response, Model model) {
        User user = UserUtils.getUser();

        // 当前获取到的是乡/镇/街道的area_id
        Long areaId = Optional.ofNullable(servicePointStation)
                .map(ServicePointStation::getArea)
                .map(Area::getId)
                .orElse(-1L);

        Area area = areaService.getFromCache(areaId);
        servicePointStation.setArea(area);

        // 获取乡/镇/街道的上级区/县的area_id
        Area parentArea = null;
        if (!ObjectUtils.isEmpty(area)) {
            parentArea = areaService.getFromCache(area.getParentId());
        }

        ServicePoint servicePoint = new ServicePoint();
        servicePoint.setArea(parentArea);
        servicePoint.setInvoiceFlag(-1);
        if (!ObjectUtils.isEmpty(servicePoint.getFinance())) {
            servicePoint.getFinance().setDiscountFlag(-1);  // 是否折扣,为-1时查询条件被忽略
        }
        servicePoint.setStatus(new Dict(String.valueOf(ServicePointStatus.NORMAL.getValue())));
        servicePoint.setAutoPlanFlag(ServicePoint.AUTO_PLAN_FLAG_ENABLED);
        Page<ServicePoint> page = new Page<>(request, response);
        page.setPageSize(100000);
        servicePoint.setOrderBy("s.order_count desc,s.servicepoint_no");//sort
        List<ServicePoint> servicePointList = servicePointStationService.findNoConfigServicePointStationList(page,servicePoint,area);

        servicePointStation.setRadius(5000);
        model.addAttribute("list",servicePointList);
        model.addAttribute("servicePointStation", servicePointStation);

        return "modules/md/servicePointStationAreaForm";
    }
    */

    /*
    // mark on 2019-7-27
    @RequiresPermissions("md:servicepointstation:edit")
    @RequestMapping(value = "areaSave")
    public String areaSave(ServicePointStation servicePointStation, String servicePointIds, HttpServletRequest request, HttpServletResponse response, Model model, RedirectAttributes redirectAttributes) {
        if (!beanValidator(model, servicePointStation)){
            return areaForm(servicePointStation,request,response,model);
        }
        servicePointStation.setDelFlag(ServicePoint.DEL_FLAG_NORMAL);

        Long areaId = servicePointStation.getArea().getId();
        Area area = areaService.getFromCache(areaId);
        servicePointStation.setArea(area);
        servicePointStation.setName(area.getName());  //默认地名为区域简称
        servicePointStation.setAddress("");   //服务区域默认为空

        String strFullAddressName = area.getFullName();
        //strFullAddressName = "广东省佛山市南海区盐步镇";  //only for test
        String[] areaArray = AreaUtils.getLocation(strFullAddressName);
        Double dblLongtitude = 0D;
        Double dblLatitude = 0D;
        if (!org.springframework.util.ObjectUtils.isEmpty(areaArray)) {
            if (areaArray.length == 2) {
                dblLongtitude = Double.valueOf(areaArray[0]);
                dblLatitude = Double.valueOf(areaArray[1]);

                System.out.println(String.format("网点地址:[%s] 转换成坐标为{longtitude:%s,latitude:%s}", strFullAddressName,dblLongtitude, dblLatitude));
                servicePointStation.setLongtitude(dblLongtitude);
                servicePointStation.setLatitude(dblLatitude);
            }
        } else {
            addMessage(model, "你输入的地址不正确.");
            return areaForm(servicePointStation,request,response,model);
        }

        if (servicePointStation.getRadius() == null) {
            servicePointStation.setRadius(0);
        }

        if (servicePointIds == null || servicePointIds.equals("")) {
            addMessage(model, "请你至少选择一个网点.");
            return areaForm(servicePointStation,request,response,model);
        }

        String[] idsArr = servicePointIds.split(",");
        for(int i=0;i < idsArr.length;i++) {
            Long servicePointId = Long.valueOf(idsArr[i]);
            ServicePoint servicePoint = servicePointService.getFromCache(servicePointId);
            ServicePointStation servicePointStationEntity = ServicePointStation.builder().build();
            BeanUtils.copyProperties(servicePointStation,servicePointStationEntity);

            servicePointStationEntity.setServicePoint(servicePoint);
            //System.out.println(servicePointStationEntity);
            try {
                //servicePointService.updateAutoPlanFlag(servicePoint.getId(),ServicePoint.AUTO_PLAN_FLAG_ENABLED);
                servicePointStationService.save(servicePointStationEntity);
            }catch (Exception ex){
                ex.printStackTrace();
                return areaForm(servicePointStation,null,null,model);
            }
        }
        addMessage(redirectAttributes, "保存服务点'" + servicePointStation.getName() + "'成功");
        return String.format("redirect:%s/md/servicepointstation/areaStationList?repage&area.id=%s",adminPath,area.getParentId());
    }
    */

    @RequiresPermissions(value = {"md:servicepointstation:view","md:servicepointstation:edit"},logical = Logical.OR)
    @RequestMapping(value = "batchForm")
    public String batchForm(ServicePoint servicePoint,HttpServletRequest request, HttpServletResponse response, Model model) {
        //
        // 为网点批量添加服务区域
        //
        User user = UserUtils.getUser();
        servicePoint = servicePointService.getFromCache(servicePoint.getId());

        // 查询该网点下所要服务的区域列表(区域到街道/乡镇)
        List<AreaModel> areaModels = Lists.newArrayList();
        List<Area> serviceAreas = servicePointService.getAreas(servicePoint.getId());
        serviceAreas = serviceAreas.stream().filter(r->r.getType().equals(Area.TYPE_VALUE_COUNTY)).sorted(Comparator.comparing(r->r.getParent().getId())).collect(Collectors.toList());
        if (!ObjectUtils.isEmpty(serviceAreas)) {
            serviceAreas.stream().forEach(r->{
                AreaModel areaModel = new AreaModel();
                BeanUtils.copyProperties(r,areaModel);
                Area parentArea = areaService.getFromCache(r.getParent().getId());
                areaModel.setParent(parentArea);
                List<Area> subAreaList = areaService.findListByParent(Area.TYPE_VALUE_TOWN,r.getId());
                areaModel.setSubAreas(subAreaList);
                areaModels.add(areaModel);
            });
        }

        // 查询该网点下已经建好的服务街道
        ServicePointStation servicePointStation = ServicePointStation.builder()
                .servicePoint(servicePoint)
                .build();
        List<ServicePointStation> servicePointStationList = servicePointStationService.findList(servicePointStation);
        List<Long> areaIds = Lists.newArrayList();
        if (!ObjectUtils.isEmpty(servicePointStationList)) {
            areaIds = servicePointStationList.stream().filter(r -> r.getDelFlag().equals(ServicePointStation.DEL_FLAG_NORMAL))
                    .map(ServicePointStation::getArea)
                    .map(Area::getId)
                    .collect(Collectors.toList());

            servicePointStationList = servicePointStationList.stream().filter(r -> r.getDelFlag().equals(ServicePointStation.DEL_FLAG_NORMAL))
                    .collect(Collectors.toList());
        }

        model.addAttribute("list",areaModels);
        model.addAttribute("servicePoint", servicePoint);
        model.addAttribute("areaIdList",areaIds);
        model.addAttribute("servicePointStationList",gsonRedisSerializer.toJson(servicePointStationList));

        return "modules/md/servicePointStationBatchForm";
    }

    @RequiresPermissions("md:servicepointstation:edit")
    @RequestMapping(value = "batchSave")
    public String batchSave(ServicePoint servicePoint, String areaIds,String autoPlanIds, HttpServletRequest request, HttpServletResponse response, Model model, RedirectAttributes redirectAttributes) {
        servicePoint = servicePointService.getFromCache(servicePoint.getId());
        ServicePointStation servicePointStation = ServicePointStation.builder().build();
        servicePointStation.setDelFlag(ServicePoint.DEL_FLAG_NORMAL);
        servicePointStation.setServicePoint(servicePoint);

        if (servicePointStation.getRadius() == null) {
            servicePointStation.setRadius(0);
        }

        if (areaIds == null || areaIds.equals("")) {
            addMessage(model, "请你至少选择一个街道.");
            return batchForm(servicePoint,request,response,model);
        }

        // 处理从前端返回有效区域id列表
        String[] idsArr = areaIds.split(",");
        List<Long> inputIds = Arrays.asList(idsArr).stream().map(r->Long.valueOf(r)).collect(Collectors.toList());

        // 处理从前端返回的需要自动派单的区域id列表
        String[] autoPlanIdsArr = StringUtils.isEmpty(autoPlanIds)? new String[]{}:autoPlanIds.split(",");
        List<Long> inputAutoPlanIds = autoPlanIdsArr.length > 0 ? Arrays.asList(autoPlanIdsArr).stream().map(r->Long.valueOf(r)).collect(Collectors.toList()) : Lists.newArrayList();

        // 从db中获取当前网点下所有的有效的服务点
        ServicePointStation servicePointStationtemp = ServicePointStation.builder()
                .servicePoint(servicePoint)
                .build();
        List<ServicePointStation> servicePointStationList = servicePointStationService.findList(servicePointStationtemp);
        List<Long> areaIdArray = Lists.newArrayList();
        if (!ObjectUtils.isEmpty(servicePointStationList)) {
            areaIdArray = servicePointStationList.stream().filter(r -> r.getDelFlag().equals(ServicePointStation.DEL_FLAG_NORMAL))
                    .map(ServicePointStation::getArea)
                    .map(Area::getId)
                    .collect(Collectors.toList());
        }

        //比较集合
        //输入的ids有,数据库areaIdArray没有,对数据进行插入操作
        List<Long> retainIdsCopy = Lists.newArrayList();    // 用来保存取交集和取差集的结果
        List<Long> inputIdsCopy = Lists.newArrayList();
        inputIdsCopy.addAll(inputIds);
        inputIdsCopy.retainAll(areaIdArray); // 两个集合中相同的结果(交集)
        retainIdsCopy.addAll(inputIdsCopy); //保存取交集的结果

        inputIdsCopy.clear();
        inputIdsCopy.addAll(inputIds);
        inputIdsCopy.removeAll(areaIdArray);  // 取差集
        retainIdsCopy.addAll(inputIdsCopy);   //保存取差集的结果

        List<TwoTuple<MQSyncType.SyncType,ServicePointStation>> twoTupleList = Lists.newArrayList();
        if (!ObjectUtils.isEmpty(retainIdsCopy)) {
            List<ServicePointStation> addServicePointStationList = Lists.newArrayList(); // add on 2019-11-27
            for (Long val : retainIdsCopy) {
                //ServicePointStation servicePointStationEntity = servicePointStationService.generateServicePointStation(val,servicePointStation,inputAutoPlanIds);   // mark on 2019-12-26
                ServicePointStation servicePointStationEntity = servicePointStationService.generateServicePointStationNew(val, servicePointStation, inputAutoPlanIds, servicePointStationList);// add on 2019-12-26
                log.warn("生成的服务点对象:{}",servicePointStationEntity);
                try {
                    // mark on 2020-2-2 begin
                    //TwoTuple<MQSyncType.SyncType,ServicePointStation> twoTuple = servicePointStationService.simpleSave(servicePointStationEntity);
                    //twoTupleList.add(twoTuple);
                    // mark on 2020-2-2 end
                    addServicePointStationList.add(servicePointStationEntity); //add on 2019-11-27
                    if (servicePointStationEntity.getAutoPlanFlag().equals(1)) {  //1-自动派单,0-手工派单
                        Area districtArea = servicePointStationEntity.getArea().getParent();
                        if (!ObjectUtils.isEmpty(districtArea) && !ObjectUtils.isEmpty(districtArea.getId())) {
                            PlanRadius planRadius = planRadiusService.getByAreaId(districtArea.getId());
                            if (planRadius.getAutoPlanFlag() == null) {
                                planRadius.setAutoPlanFlag(0);
                            }
                            if (planRadius.getAutoPlanFlag().equals(0)) {
                                planRadius.setAutoPlanFlag(1);   //1-自动派单,0-手工派单
                                log.warn("服务点有自动派单更新PlanRadius中自动派单标志.服务点区域名称:{}", districtArea.getName());
                                planRadiusService.save(planRadius);
                            }
                            // 如果planRadius中的自动派单标志已经为1了，就不再操作数据库了
                        }
                    }
                } catch (Exception ex) {
                    addMessage(model, ex.getMessage());
                    return batchForm(servicePoint, request,response, model);
                }
            }
            // 调用我服务 add on 2019-11-27
            /*
            // mark on 2020-2-2 begin
            MSErrorCode msErrorCode = msServicePointStationService.batchSave(addServicePointStationList);
            if(msErrorCode.getCode()>0){
                throw new RuntimeException(msErrorCode.getMsg());
            }
            // mark on 2020-2-2 end
            */
            // add on 2020-2-2 begin
            List<ServicePointStation> servicePointStations = msServicePointStationService.batchSave(addServicePointStationList);
            for(ServicePointStation servicePointStationEntity:servicePointStations) {
                TwoTuple<MQSyncType.SyncType, ServicePointStation> twoTuple = servicePointStationService.simpleSave(servicePointStationEntity);
                twoTupleList.add(twoTuple);
            }
            // add on 2020-2-2 end
            // end
        }

        // 删除
        List<Long> dbIdsCopy = Lists.newArrayList();
        dbIdsCopy.addAll(areaIdArray);
        dbIdsCopy.removeAll(inputIds);

        if (!ObjectUtils.isEmpty(dbIdsCopy)) {
            List<ServicePointStation> deleteServicePointStationList = Lists.newArrayList(); //add on 2019-11-27
            for(Long val : dbIdsCopy) {
                Area area = areaService.getFromCache(val);
                ServicePointStation entity = ServicePointStation.builder()
                        .servicePoint(servicePoint)
                        .area(area)
                        .build();

                //ServicePointStation retServicePointStation = servicePointStationService.getByServicePointIdAndAreaId(entity);  //mark on 2019-12-26
                // add on 2019-12-26 begin
                ServicePointStation retServicePointStation = ObjectUtils.isEmpty(servicePointStationList)?
                        null: servicePointStationList.stream().filter(x->x.getServicePoint().getId().equals(entity.getServicePoint().getId()) && x.getArea().getId().equals(entity.getArea().getId())).findFirst().orElse(null);
                // add on 2019-12-26 end
                if (!ObjectUtils.isEmpty(retServicePointStation)) {
                    servicePointStationService.deleteForWeb(retServicePointStation);
                    deleteServicePointStationList.add(retServicePointStation); ////add on 2019-11-27
                }
            }
            // 调用微服务 add on 2017-11-27
            if (!ObjectUtils.isEmpty(deleteServicePointStationList)) {
                MSErrorCode msErrorCode = msServicePointStationService.batchDelete(deleteServicePointStationList);
                if (msErrorCode.getCode() > 0) {
                    throw new RuntimeException("删除网点服务区域失败.失败原因:" + msErrorCode.getCode());
                }
            }
            // end
        }

        // mark on 2019-10-4 begin
//        List<ServicePointStation> existsSpsList = servicePointStationService.findList(servicePointStation);
//        if (!ObjectUtils.isEmpty(existsSpsList))
//        {
        //Long iCount = existsSpsList.stream().filter(r->r.getAutoPlanFlag().equals(1)).count();
        // mark on 2019-10-4 end
        Integer iCount = autoPlanIdsArr.length;
        //log.warn("当前网点:{} 自动派单的服务点数据量是:{},AutoPlanFlag:{}",servicePoint.getName(),iCount,servicePoint.getAutoPlanFlag());
        int iAutoPlanFlag = 0;
        if (iCount != null && iCount.intValue() >0) {
            iAutoPlanFlag = ServicePoint.AUTO_PLAN_FLAG_ENABLED;
        } else if (iCount != null && iCount.intValue() == 0) {
            iAutoPlanFlag = ServicePoint.AUTO_PLAN_FLAG_DISABLED;
        }
        servicePointService.updateAutoPlanFlag(servicePoint.getId(),iAutoPlanFlag);
        log.warn("将当前网点:{} 自动派单标志置为{}.",servicePoint.getName(),iAutoPlanFlag);
        //add on 2019-10-4 begin
        //ServicePoint微服务
        ServicePoint servicePointFromMS = null;
        if (servicePointStation.getServicePoint() != null && servicePointStation.getServicePoint().getId() != null) {
            servicePointFromMS = msServicePointService.getById(servicePointStation.getServicePoint().getId());
        }
        //add on 2019-10-4 end

        if (twoTupleList != null && !twoTupleList.isEmpty()) {
            int lambdaAutoPlanFlag = iAutoPlanFlag;
            final ServicePoint finalServicePoint = servicePointFromMS;
            twoTupleList.stream().forEach(r->{
                // add on 2019-10-4 begin
                //ServicePoint微服务
                if (r.getBElement().getServicePoint() ==null) {
                    r.getBElement().setServicePoint(finalServicePoint);
                }
                // add on 2019-10-4 end
                r.getBElement().getServicePoint().setAutoPlanFlag(lambdaAutoPlanFlag);
            });
            servicePointStationService.handlerMessageAndCache(twoTupleList);
        }
//        }  //add on 2019-10-4

        addMessage(redirectAttributes, "保存服务区域成功");
        return String.format("redirect:%s/md/servicepoint/selectForStation?repage",adminPath);
    }


    /**
     * 编辑或新增网点服务点时，装载区域
     * 1.网点的区域
     * 2.网点服务点的区域
     * @param sid 		网点id
     * @param eid 		网点服务点id
     */
    /*
    // mark on 2019-7-27
    @ResponseBody
    @RequestMapping(value = "loadStationAreas", method = RequestMethod.GET )
    public AjaxJsonEntity loadStationAreas(Long sid, Long eid, HttpServletResponse response) {
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity result = new AjaxJsonEntity(true);

        User user = UserUtils.getUser();
        if(user==null || user.getId()==null){
            result.setSuccess(false);
            result.setMessage("登录超时，请重新登录。");
            return result;
        }
        if(sid==null){
            result.setMessage("未设置网点。");
            return result;
        }

        try{
            List<Area> serviceAreas = servicePointService.getAreas(sid);
            if(serviceAreas == null || serviceAreas.size()==0){
                result.setSuccess(false);
                result.setMessage("网点未授权负责的区域。");
                return result;
            }
            serviceAreas = serviceAreas.stream()
                    .filter(t->t.getId() >1)
                    .sorted(Comparator.comparing(Area::getType)
                            .thenComparing(Area::getSort))
                    .collect(Collectors.toList());

            if(serviceAreas.stream().filter(t->t.getType()==1).count()==0){
                serviceAreas.add(0,new Area(1L,"区域列表",1));
            }
            EngineerAreaManageVM data = new EngineerAreaManageVM();
            List<zTreeEntity> treeList = Lists.newArrayList();
            //treeList = serviceAreas.stream().map(t-> new zTreeEntity(t.getId(),t.getParentId(),t.getName())).collect(Collectors.toList());
            treeList = serviceAreas.stream().map(t-> new zTreeEntity(t.getId(),t.getParentId(),t.getName(),t.getType())).collect(Collectors.toList());

            data.setServiceAreas(treeList);

            if(eid !=null && eid>0) {
                List<Area> areas = servicePointStationService.getStationAreaList(eid);
                if (areas != null && areas.size() > 0) {
                    List<Long> ids = areas.stream()
                            .map(Area::getId)
                            .collect(Collectors.toList());
                    data.setAreaIds(ids);
                }
            }
            result.setData(data);
        } catch (Exception ex){
            log.error("[ServicePointStationController.loadStationAreas]",ex);
            result.setSuccess(false);
            result.setMessage("读取网点及网点服务点区域错误");
        }
        return result;
    }
    */

    /**
     * 停用
     * 逻辑删除网点服务点
     */
    @RequiresPermissions("md:servicepointstation:stop")
    @RequestMapping(value = "delete")
    public String delete(ServicePointStation servicePointStation, HttpServletRequest request, RedirectAttributes redirectAttributes)
    {
        if(ObjectUtils.isEmpty(servicePointStation)){
            addMessage(redirectAttributes, "该服务点不存在");
            //return "redirect:" + Global.getAdminPath() + "/md/servicepointstation/list?repage";
            return String.format("redirect:%s/md/servicepoint/selectForStation?repage",adminPath);
        }else if(servicePointStation.getDelFlag() == ServicePointStation.DEL_FLAG_DELETE){
            addMessage(redirectAttributes, "该服务点已停用");
            //return "redirect:" + Global.getAdminPath() + "/md/servicepointstation/list?repage";
            return String.format("redirect:%s/md/servicepoint/selectForStation?repage",adminPath);
        }
        servicePointStationService.delete(servicePointStation);
        addMessage(redirectAttributes, "停用服务点成功");

        Area area = servicePointStation.getArea();
        if (!ObjectUtils.isEmpty(area)) {
            area = areaService.getFromCache(area.getId());
        }
        return String.format("redirect:%s/md/servicepointstation/areaStationList?repage&area.id=%s",adminPath,area.getParentId());
    }

    /**
     * 启用网点服务点
     * @param request
     * @param redirectAttributes
     * @return
     */
    @RequiresPermissions("md:servicepointstation:stop")
    @RequestMapping(value = "enable")
    public String enable(ServicePointStation servicePointStation, HttpServletRequest request, RedirectAttributes redirectAttributes)
    {
        if(ObjectUtils.isEmpty(servicePointStation)){
            addMessage(redirectAttributes, "该服务点不存在");
            //return "redirect:" + Global.getAdminPath() + "/md/servicepointstation/list?repage";
            return String.format("redirect:%s/md/servicepoint/selectForStation?repage",adminPath);
        }else if(servicePointStation.getDelFlag() == ServicePointStation.DEL_FLAG_NORMAL){
            addMessage(redirectAttributes, "该服务点已启用");
            //return "redirect:" + Global.getAdminPath() + "/md/servicepointstation/list?repage";
            return String.format("redirect:%s/md/servicepoint/selectForStation?repage",adminPath);
        }
        servicePointStationService.enable(servicePointStation);
        addMessage(redirectAttributes, "启用服务点成功");
        //return "redirect:" + Global.getAdminPath() + "/md/servicepointstation/list?repage";

        Area area = servicePointStation.getArea();
        if (!ObjectUtils.isEmpty(area)) {
            area = areaService.getFromCache(area.getId());
        }

        return String.format("redirect:%s/md/servicepointstation/areaStationList?repage&area.id=%s",adminPath,area.getParentId());
    }

    @ResponseBody
    @RequestMapping(value = "modifyRadius", method = RequestMethod.POST )
    public AjaxJsonEntity modifyRadius(Long id, Integer radius, HttpServletResponse response) {
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        User user = UserUtils.getUser();
        if(user==null || user.getId()==null){
            result.setSuccess(false);
            result.setMessage("登录超时，请重新登录。");
            return result;
        }
        if(id==null){
            result.setSuccess(false);
            result.setMessage("未设置网点服务点。");
            return result;
        }

        if (radius == null) {
            result.setSuccess(false);
            return result;
        }

        try{
            ServicePointStation servicePointStation = servicePointStationService.get(id);
            servicePointStation.setRadius(radius);
            servicePointStationService.save(servicePointStation);
            result.setSuccess(true);
        } catch (Exception ex){
            log.error("[ServicePointStationController.modifyRadius]",ex);
            result.setSuccess(false);
            result.setMessage("读取网点及网点服务点区域错误");
        }
        return result;
    }

    @ResponseBody
    @RequestMapping(value = "modifyPosition", method = RequestMethod.POST )
    public AjaxJsonEntity modifyPosition(Long id, Double longitude,Double latitude, HttpServletResponse response) {
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        User user = UserUtils.getUser();
        if(user==null || user.getId()==null){
            result.setSuccess(false);
            result.setMessage("登录超时，请重新登录。");
            return result;
        }
        if(id==null){
            result.setMessage("未设置网点服务点。");
            return result;
        }

        try{
            ServicePointStation servicePointStation = servicePointStationService.get(id);
            servicePointStation.setLongtitude(longitude);
            servicePointStation.setLatitude(latitude);
            servicePointStationService.save(servicePointStation);
            result.setSuccess(true);
        } catch (Exception ex){
            log.error("[ServicePointStationController.modifyPosition]",ex);
            result.setSuccess(false);
            result.setMessage("读取网点及网点服务点区域错误");
        }
        return result;
    }

    @ResponseBody
    @RequestMapping(value = "arealistbyservicepointid")
    public AjaxJsonEntity arealistbyids(Integer servicePointId, HttpServletResponse response) {
        // 此函数主要用来获取同一父级id下的所有4级区域数据
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity jsonEntity = new AjaxJsonEntity();
        try{
            if (servicePointId == null) {
                throw new Exception("传入网点id为空.");
            }

            ServicePointStation servicePointStation = ServicePointStation.builder()
                    .servicePoint(new ServicePoint(Long.valueOf(servicePointId)))
                    .build();
            List<ServicePointStation> servicePointStationList = servicePointStationService.findList(servicePointStation);

            List<Area> areaList = Lists.newArrayList();
            if (!ObjectUtils.isEmpty(servicePointStationList)) {
                servicePointStationList.stream().forEach(r->{
                    Area area = areaService.getFromCache(r.getArea().getId());
                    areaList.add(area);
                });
            }

            jsonEntity.setSuccess(true);
            jsonEntity.setData(areaList);
        }
        catch (Exception ex) {
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage(ex.getMessage().toString());
        }
        return jsonEntity;
    }



    /**
     * 返回网点区域服务页面(客服在派单选择网点时操作)
     * @param servicePointId
     * @param areaId
     * @param model
     * @return
     */
    @RequestMapping("showServicePointStationByAreaId")
    public String showServicePointStationByAreaId(Long servicePointId,Long areaId,Model model){
        String view = "modules/md/servicePointStationFormForKefu";
        if(servicePointId==null || servicePointId<=0){
            addMessage(model, "错误：网点id参数不存在");
            model.addAttribute("canSave",false);
            return view;
        }
        if(areaId ==null || areaId<=0){
            addMessage(model, "错误：区域id参数不存在");
            model.addAttribute("canSave",false);
            return view;
        }

        Area area = areaService.getFromCache(areaId);
        if(area == null){
            addMessage(model, "错误：区域不存在");
            model.addAttribute("canSave",false);
            return view;
        }
        ServicePoint servicePoint = servicePointService.getFromCache(servicePointId);
        AreaModel areaModel = new AreaModel();
        BeanUtils.copyProperties(area,areaModel);
        Area parentArea = areaService.getFromCache(area.getParent().getId());
        areaModel.setParent(parentArea);
        List<Area> subAreaList = areaService.findListByParent(Area.TYPE_VALUE_TOWN,area.getId());
        areaModel.setSubAreas(subAreaList);
        // 查询该网点下已经建好的服务街道
        ServicePointStation servicePointStation = ServicePointStation.builder()
                .servicePoint(servicePoint)
                .build();
        List<ServicePointStation> servicePointStationList = servicePointStationService.findList(servicePointStation);
        // 根据区县过滤数据
        servicePointStationList = servicePointStationList.stream().filter(t->t.getArea().getParent().getId().equals(areaId)).collect(Collectors.toList());
        List<Long> areaIds = Lists.newArrayList();
        if(servicePointStationList!=null && servicePointStationList.size()>0){
            areaIds = servicePointStationList.stream().filter(r -> r.getDelFlag().equals(ServicePointStation.DEL_FLAG_NORMAL))
                    .map(ServicePointStation::getArea)
                    .map(Area::getId)
                    .collect(Collectors.toList());

            servicePointStationList = servicePointStationList.stream().filter(r -> r.getDelFlag().equals(ServicePointStation.DEL_FLAG_NORMAL))
                    .collect(Collectors.toList());
        }
        model.addAttribute("areaModel",areaModel);
        model.addAttribute("servicePoint", servicePoint);
        model.addAttribute("areaIdList",areaIds);
        model.addAttribute("servicePointStationList",gsonRedisSerializer.toJson(servicePointStationList));
        return view;
    }

    /**
     * 保存网点区域服务(客服在派单选择网点时操作)
     * @param servicePointId
     * @param areaIds
     * @param response
     * @return
     */
    @RequestMapping("saveServicePointStationByAreaId")
    @ResponseBody
    public AjaxJsonEntity saveServicePointStationByAreaId(Long servicePointId,String areaIds,HttpServletResponse response){
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        if(servicePointId ==null || servicePointId<=0){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("参数错误,网点不存在");
            return ajaxJsonEntity;
        }
        ServicePoint servicePoint = servicePointService.getFromCache(servicePointId);
        if(servicePoint == null){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("网点不存在");
            return ajaxJsonEntity;
        }
        if(areaIds == null || areaIds.equals("")){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("至少勾选一条街道");
            return ajaxJsonEntity;
        }
        try {
            servicePointStationService.saveServicePointStationByAreaId(servicePoint,areaIds);
        }catch (Exception e){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage(e.getMessage());
            return ajaxJsonEntity;
        }
        return ajaxJsonEntity;
    }


    /**
     * 突击客服派单网点停用类表查看网点街道
     * @param servicePointId
     * @param areaId
     * @param model
     * @return
     */
    @RequestMapping("showServicePointStationForRushPlan")
    public String showServicePointStationForRushPlan(Long servicePointId,Long areaId,Model model){
        String view = "modules/md/servicePointStationForRushPlan";
        if(servicePointId==null || servicePointId<=0){
            addMessage(model, "错误：网点id参数不存在");
            model.addAttribute("canSave",false);
            return view;
        }
        if(areaId ==null || areaId<=0){
            addMessage(model, "错误：区域id参数不存在");
            model.addAttribute("canSave",false);
            return view;
        }

        Area area = areaService.getFromCache(areaId);
        if(area == null){
            addMessage(model, "错误：区域不存在");
            model.addAttribute("canSave",false);
            return view;
        }
        ServicePoint servicePoint = servicePointService.getFromCache(servicePointId);
        AreaModel areaModel = new AreaModel();
        BeanUtils.copyProperties(area,areaModel);
        Area parentArea = areaService.getFromCache(area.getParent().getId());
        areaModel.setParent(parentArea);
      /*  List<Area> subAreaList = areaService.findListByParent(Area.TYPE_VALUE_TOWN,area.getId()).;
        areaModel.setSubAreas(subAreaList);*/
        List<Area> subAreaList = areaService.findListByParent(Area.TYPE_VALUE_TOWN,area.getId());
        Map<Long,Area> sunAreaMap = subAreaList.stream().collect(Collectors.toMap(Area::getId, Function.identity(), (key1, key2) -> key2));
        // 查询该网点下已经建好的服务街道
        ServicePointStation servicePointStation = ServicePointStation.builder()
                .servicePoint(servicePoint)
                .build();
        List<ServicePointStation> servicePointStationList = servicePointStationService.findList(servicePointStation);
        // 根据区县过滤数据
        servicePointStationList = servicePointStationList.stream().filter(t->t.getArea().getParent().getId().equals(areaId)).collect(Collectors.toList());
        List<Long> areaIds = Lists.newArrayList();
        if(servicePointStationList!=null && servicePointStationList.size()>0){
            areaIds = servicePointStationList.stream().filter(r -> r.getDelFlag().equals(ServicePointStation.DEL_FLAG_NORMAL))
                    .map(ServicePointStation::getArea)
                    .map(Area::getId)
                    .collect(Collectors.toList());

            servicePointStationList = servicePointStationList.stream().filter(r -> r.getDelFlag().equals(ServicePointStation.DEL_FLAG_NORMAL))
                    .collect(Collectors.toList());
        }
        List<Area> townList = Lists.newArrayList();
        Area townArea=null;
        for (ServicePointStation item:servicePointStationList){
            townArea = sunAreaMap.get(item.getArea().getId());
            if(townArea!=null){
                townList.add(townArea);
            }
        }
        areaModel.setSubAreas(townList);
        model.addAttribute("areaModel",areaModel);
        return view;
    }


}
