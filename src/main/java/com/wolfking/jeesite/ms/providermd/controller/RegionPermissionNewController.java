package com.wolfking.jeesite.ms.providermd.controller;

import com.google.common.collect.Lists;
import com.kkl.kklplus.entity.md.MDCustomerVipLevel;
import com.kkl.kklplus.entity.md.MDRegionPermission;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providermd.entity.AreaProductcategoryModel;
import com.wolfking.jeesite.ms.providermd.entity.AreaRemoteFeeModel;
import com.wolfking.jeesite.ms.providermd.entity.RegionPermissionModel;
import com.wolfking.jeesite.ms.providermd.service.MSRegionPermissionNewService;
import com.wolfking.jeesite.ms.providermd.service.MSRegionPermissionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "${adminPath}/provider/md/regionPermissionNew")
@Slf4j
public class RegionPermissionNewController extends BaseController {

    @Autowired
    private MSRegionPermissionNewService regionPermissionNewService;

    @Autowired
    private AreaService areaService;

    /**
     * 根据城市获取客服街道
     */
    @RequiresPermissions(value = {"md:regionPermission:view"})
    @RequestMapping("/findRegionPermissionList")
    public String findRemoteFeeList(MDRegionPermission regionPermission, Model model) {
        String view = "modules/providermd/regionPermissionVerSecondList";
        regionPermission.setGroupType(1);
        if (regionPermission.getCityId() == null) {
            model.addAttribute("regionPermission", regionPermission);
            model.addAttribute("show", false);
            return view;
        }
        try {
            Map<String, Object> map = regionPermissionNewService.findRegionPermissionListNew(regionPermission);
            if (map.get("empty") != null) {
                model.addAttribute("regionPermission", regionPermission);
                model.addAttribute("show", false);
                addMessage(model, map.get("empty").toString());
                return view;
            }
            model.addAttribute("regionPermission", regionPermission);
            model.addAttribute("datas", map);
            model.addAttribute("show", true);
            return view;
        } catch (Exception e) {
            model.addAttribute("regionPermission", regionPermission);
            model.addAttribute("datas", new HashMap<>());
            model.addAttribute("show", false);
            addMessage(model, e.getMessage());
            return view;
        }
    }
    /**
     * 根据城市获取远程设置
     * */
    @RequiresPermissions(value = {"md:regionPermission:view"})
    @RequestMapping("/regionPermissionRemoteList")
    public String regionPermissionRemoteList(MDRegionPermission regionPermission,Model model){
        String view = "modules/providermd/regionPermissionRemoteList";
        regionPermission.setType(2);
        regionPermission.setGroupType(2);

        if(regionPermission.getCityId()==null){
            model.addAttribute("regionPermission",regionPermission);
            model.addAttribute("show", false);
            return view;
        }
        try {
            Map<String, Object> map = regionPermissionNewService.findRegionPermissionListNew(regionPermission);
            if (map.get("empty") != null) {
                model.addAttribute("regionPermission", regionPermission);
                model.addAttribute("show", false);
                addMessage(model, map.get("empty").toString());
                return view;
            }
            model.addAttribute("regionPermission", regionPermission);
            model.addAttribute("datas", map);
            model.addAttribute("show", true);
            return view;
        }catch (Exception e){
            model.addAttribute("regionPermission", regionPermission);
            model.addAttribute("datas", new HashMap<>());
            model.addAttribute("show", false);
            addMessage(model,e.getMessage());
            return view;
        }
    }

    /**
     * 根据城市获取远程设置
     * */
    @RequiresPermissions(value = {"md:regionPermissionArea:view"})
    @RequestMapping("/regionPermissionRemoteNewList")
    public String regionPermissionRemoteNewList(MDRegionPermission regionPermission,Model model){
        String view = "modules/providermd/regionPermissionRemoteNewList";
        regionPermission.setType(1);
        regionPermission.setGroupType(3);

        if(regionPermission.getCityId()==null){
            model.addAttribute("regionPermission",regionPermission);
            model.addAttribute("show", false);
            return view;
        }
        try {
            Map<String, Object> map = regionPermissionNewService.findRegionPermissionAreaListNew(regionPermission);
            if (map.get("empty") != null) {
                model.addAttribute("regionPermission", regionPermission);
                model.addAttribute("show", false);
                addMessage(model, map.get("empty").toString());
                return view;
            }
            model.addAttribute("regionPermission", regionPermission);
            model.addAttribute("datas", map);
            model.addAttribute("show", true);
            return view;
        }catch (Exception e){
            model.addAttribute("regionPermission", regionPermission);
            model.addAttribute("datas", new HashMap<>());
            model.addAttribute("show", false);
            addMessage(model,e.getMessage());
            return view;
        }
    }

    /**
     * 根据品类和市id去查
     *
     * @param regionPermission
     * @param model
     * @return
     */
    @RequiresPermissions(value = {"md:regionPermission:view"})
    @RequestMapping(value = "/findRegionPermission", method = RequestMethod.POST)
    @ResponseBody
    public AjaxJsonEntity findRegionPermission(MDRegionPermission regionPermission, Model model) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        List<MDRegionPermission> data = Lists.newArrayList();
        try {
            data = regionPermissionNewService.findListByCategoryAndCityId(regionPermission);
            ajaxJsonEntity.setData(data);
        } catch (Exception e) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage(e.getMessage());
        }
        model.addAttribute("subAreas", data);
        return ajaxJsonEntity;
    }

    /**
     * 加载省市区
     *
     * @param id
     * @param model
     * @return
     */
    @RequiresPermissions(value = {"md:regionPermission:view"})
    @RequestMapping("/selectStreet")
    public String selectStreet(@RequestParam(required = false) Long id, Long productCategoryId, Integer groupType,Model model) {
        String view = "modules/providermd/regionPermissionVerSecondForm";
        if(groupType == 2){
            view = "modules/providermd/regionPermissionRemoteForm";
        }
        AreaRemoteFeeModel areaRemoteFeeModel = new AreaRemoteFeeModel();
        if (id <= 0) {
            model.addAttribute("areaRemoteFeeModel", areaRemoteFeeModel);
            return view;
        }
        try {
            Map<String, Object> map = regionPermissionNewService.selectStreet(groupType,id);
            model.addAttribute("area", map.get("area"));
            model.addAttribute("areaModelList", map.get("areaModelList"));
            MDRegionPermission regionPermission = new MDRegionPermission();
            regionPermission.setCityId(id);
            regionPermission.setProductCategoryId(productCategoryId);
            regionPermission.setGroupType(groupType);
            model.addAttribute("regionPermission", regionPermission);
            return view;
        } catch (Exception e) {
            addMessage(model, e.getMessage());
            return view;
        }
    }

    /**
     * 根据品类和市id去查
     *
     * @param regionPermission
     * @param model
     * @return
     */
    @RequiresPermissions(value = {"md:regionPermissionArea:view"})
    @RequestMapping(value = "/findRegionPermissionNew", method = RequestMethod.POST)
    @ResponseBody
    public AjaxJsonEntity findRegionPermissionNew(MDRegionPermission regionPermission, Model model) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        List<MDRegionPermission> data = Lists.newArrayList();
        try {
            data = regionPermissionNewService.findListByCategoryAndCityId(regionPermission);
            ajaxJsonEntity.setData(data);
        } catch (Exception e) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage(e.getMessage());
        }
        model.addAttribute("subAreas", data);
        return ajaxJsonEntity;
    }

    /**
     * 加载省市区
     *
     * @param id
     * @param model
     * @return
     */
    @RequiresPermissions(value = {"md:regionPermissionArea:view"})
    @RequestMapping("/selectStreetNew")
    public String selectStreetNew(@RequestParam(required = false) Long id, Long productCategoryId, Integer groupType,Model model) {
        String view = "modules/providermd/regionPermissionVerSecondForm";
        if(groupType == 3){
            view = "modules/providermd/regionPermissionRemoteNewForm";
        }
        AreaRemoteFeeModel areaRemoteFeeModel = new AreaRemoteFeeModel();
        if (id <= 0) {
            model.addAttribute("areaRemoteFeeModel", areaRemoteFeeModel);
            return view;
        }
        try {
            Map<String, Object> map = regionPermissionNewService.selectStreetNew(groupType,id);
            model.addAttribute("area", map.get("area"));
            model.addAttribute("areaModelList", map.get("areaModelList"));
            MDRegionPermission regionPermission = new MDRegionPermission();
            regionPermission.setCityId(id);
            regionPermission.setProductCategoryId(productCategoryId);
            regionPermission.setGroupType(groupType);
            model.addAttribute("regionPermission", regionPermission);
            return view;
        } catch (Exception e) {
            addMessage(model, e.getMessage());
            return view;
        }
    }

    @RequiresPermissions(value = {"md:regionPermissionArea:edit"})
    @RequestMapping(value = "saveNew")
    @ResponseBody
    public AjaxJsonEntity saveNew(RegionPermissionModel regionPermissionModel) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        User user = UserUtils.getUser();
        try {
            regionPermissionNewService.saveNew(regionPermissionModel.getRegionPermissions(), user);
        } catch (Exception e) {
            ajaxJsonEntity.setMessage(e.getMessage());
            ajaxJsonEntity.setSuccess(false);
        }
        return ajaxJsonEntity;
    }


    @RequiresPermissions(value = {"md:regionPermission:edit"})
    @RequestMapping(value = "save")
    @ResponseBody
    public AjaxJsonEntity save(RegionPermissionModel regionPermissionModel) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        User user = UserUtils.getUser();
        try {
            regionPermissionNewService.save(regionPermissionModel.getRegionPermissions(), user);
        } catch (Exception e) {
            ajaxJsonEntity.setMessage(e.getMessage());
            ajaxJsonEntity.setSuccess(false);
        }
        return ajaxJsonEntity;
    }

    @RequiresPermissions(value = {"md:regionPermission:edit"})
    @RequestMapping(value = "batch")
    public String batch(Long cityId, Long countyId, Long productCategoryId, Model model) {
        String cityName = areaService.get(cityId).getName();
        String countyName = "";
        if (countyId != null) {
            countyName = areaService.get(countyId).getName();
        }
        MDRegionPermission regionPermission = new MDRegionPermission();
        regionPermission.setCityId(cityId);
        regionPermission.setCityName(cityName);
        regionPermission.setAreaId(countyId);
        regionPermission.setProductCategoryId(productCategoryId);
        model.addAttribute("countyName", countyName);
        model.addAttribute("regionPermission", regionPermission);
        return "modules/providermd/regionPermissionVerSecondBatchSet";
    }

    @RequiresPermissions(value = {"md:regionPermission:edit"})
    @RequestMapping(value = "batchSave")
    @ResponseBody
    public AjaxJsonEntity batchSave(Long cityId, Long areaId, Integer type, Long productCategoryId) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        User user = UserUtils.getUser();
        int groupType = 1; //客服街道
        try {
            regionPermissionNewService.batchSave(cityId, areaId, productCategoryId, groupType, type, user);
        } catch (Exception e) {
            ajaxJsonEntity.setMessage(e.getMessage());
            ajaxJsonEntity.setSuccess(false);
        }
        return ajaxJsonEntity;
    }

    @RequiresPermissions(value = {"md:regionPermission:view"})
    @RequestMapping("/findAreaList")
    public String findAreaList(Model model) {
        List<AreaProductcategoryModel> list = regionPermissionNewService.findAreaList();
        AreaProductcategoryModel areaModel = regionPermissionNewService.findCountyList();
        MDCustomerVipLevel mdCustomerVipLevels = regionPermissionNewService.findCustomerLevel();
        model.addAttribute("list", list);
        model.addAttribute("areaModel", areaModel);
        model.addAttribute("mdCustomerVipLevels", mdCustomerVipLevels);
      return "modules/providermd/regionList";
    }



    @RequestMapping(value = "/form")
    public String form(Model model) {
        MDCustomerVipLevel mdCustomerVipLevels = regionPermissionNewService.findCustomerLevel();
        List<MDCustomerVipLevel> list = regionPermissionNewService.findCustomerLevelList();

        model.addAttribute("list", list);
        model.addAttribute("mdCustomerVipLevels", mdCustomerVipLevels);
        return "modules/providermd/regionListForm";
    }


    @RequiresPermissions(value = {"md:regionPermission:edit"})
    @RequestMapping(value = "saveVipLevels")
    @ResponseBody
    public AjaxJsonEntity saveVipLevels(Long id) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        User user = UserUtils.getUser();
            try {
            regionPermissionNewService.saveCustomerVip(id, user);
            ajaxJsonEntity.setMessage("保存成功");
        } catch (Exception e) {
            ajaxJsonEntity.setMessage(e.getMessage());
            ajaxJsonEntity.setSuccess(false);
        }

        return ajaxJsonEntity;
    }
}
