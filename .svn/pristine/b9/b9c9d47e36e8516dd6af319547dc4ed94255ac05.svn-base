/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.wolfking.jeesite.modules.md.web;

import com.google.common.collect.Lists;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.zTreeEntity;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.viewModel.ServiceAreaModel;
import com.wolfking.jeesite.modules.md.service.EngineerService;
import com.wolfking.jeesite.modules.md.service.ServicePointService;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providermd.service.MSEngineerAreaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@Controller
@RequestMapping(value = "${adminPath}/md/engineerServiceArea")
public class EngineerServiceAreaController extends BaseController {


    @Autowired
    private MSEngineerAreaService msEngineerAreaService;

    @Autowired
    private ServicePointService servicePointService;

    @Autowired
    private EngineerService engineerService;


    @RequestMapping(value = "serviceArea")
    public String serviceArea(Long servicePointId, Long engineerId, Model model) {
        List<Area> serviceAreas = servicePointService.getAreas(servicePointId);
        if (serviceAreas == null || serviceAreas.size() == 0) {
            addMessage(model, "网点未授权负责的区域。");
            return "modules/md/engineerServiceArea";
        }
        serviceAreas = serviceAreas.stream()
                .filter(t -> t.getId() > 1)
                .sorted(Comparator.comparing(Area::getType)
                        .thenComparing(Area::getParentId)
                        .thenComparing(Area::getSort))
                .collect(Collectors.toList());
        List<zTreeEntity> treeList = serviceAreas.stream().map(t -> new zTreeEntity(t.getId(), t.getParentId(), t.getName(), t.getType())).collect(Collectors.toList());
        List<Long> provinceIds = treeList.stream().filter(t -> t.getType() == 2).map(zTreeEntity::getId).collect(Collectors.toList());
        Map<Long, List<zTreeEntity>> collect = treeList.stream().collect(Collectors.groupingBy(zTreeEntity::getpId));
        Map<Long, List<zTreeEntity>> idMap = treeList.stream().collect(Collectors.groupingBy(zTreeEntity::getId));
        List<ServiceAreaModel> list = Lists.newArrayList();
        ServiceAreaModel province;
        ServiceAreaModel city;
        ServiceAreaModel area;

        for (Long provinceId : provinceIds) {
            for (zTreeEntity entity : idMap.get(provinceId)) {
                province = new ServiceAreaModel();
                province.setEntity(entity);
                for (zTreeEntity cityEn : collect.get(provinceId)) {
                    city = new ServiceAreaModel();
                    city.setEntity(cityEn);
                    for (zTreeEntity areaEn : collect.get(cityEn.getId())) {
                        area = new ServiceAreaModel();
                        area.setEntity(areaEn);
                        city.getModels().add(area);
                    }
                    province.getModels().add(city);
                }
                list.add(province);
            }
        }
        model.addAttribute("servicePointId", servicePointId);
        model.addAttribute("engineerId", engineerId);
        model.addAttribute("list", list);
        return "modules/md/engineerServiceArea";
    }

    /**
     * 编辑或新增安维时，装载区域
     * 1.网点的区域
     * 2.安维已分配的区域
     *
     * @param sid 网点id
     * @param eid 安维人员id
     */
    @ResponseBody
    @RequestMapping(value = "loadEngineerAreas", method = RequestMethod.GET)
    public AjaxJsonEntity loadEngineerAreas(Long sid, Long eid, HttpServletResponse response) {
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        User user = UserUtils.getUser();
        if (user.getId() == null) {
            result.setSuccess(false);
            result.setMessage("登录超时，请重新登录。");
            return result;
        }
        try {
            List<Long> ids = Lists.newArrayList();
            if (eid != null && eid > 0) {
                List<Area> areas = servicePointService.getEngineerAreaList(eid);
                if (areas != null && areas.size() > 0) {
                    ids = areas.stream().map(Area::getId).collect(Collectors.toList());
                }
            }
            result.setData(ids);
        } catch (Exception ex) {
            log.error("[EngineerController.loadEngineerAreas]", ex);
            //return "false";
            result.setSuccess(false);
            result.setMessage("读取网点及安维区域错误");
        }
        return result;
    }

    @RequestMapping(value = "saveEngineerServiceArea")
    @ResponseBody
    public AjaxJsonEntity saveEngineerServiceArea(String areas, Long engineerId) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        List<Long> areaIds = Arrays.stream(areas.split(",")).map(Long::valueOf).collect(Collectors.toList());
        try {

            if (engineerId != null) {
                msEngineerAreaService.removeEnigineerAreas(engineerId);
                msEngineerAreaService.assignEngineerAreas(areaIds, engineerId);
                ajaxJsonEntity.setMessage("保存成功");
            }
            List<String> areaNames = engineerService.getServiceAreaNames(areaIds);
            ajaxJsonEntity.setData(areaNames);
            return ajaxJsonEntity;
        } catch (Exception ex) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage(ex.getMessage());
            return ajaxJsonEntity;
        }

    }

}
