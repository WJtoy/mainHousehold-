package com.wolfking.jeesite.ms.providermd.controller;

import com.google.common.collect.Lists;
import com.kkl.kklplus.entity.md.MDRegionPermission;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providermd.entity.AreaRemoteFeeModel;
import com.wolfking.jeesite.ms.providermd.entity.RegionPermissionModel;
import com.wolfking.jeesite.ms.providermd.service.MSRegionPermissionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(value = "${adminPath}/provider/md/regionPermission")
@Slf4j
public class RegionPermissionController extends BaseController {

    @Autowired
    private MSRegionPermissionService regionPermissionService;

    /**
     * 根据城市获取突击/远程设置
     * */
    @RequiresPermissions(value = {"md:regionPermission:view"})
    @RequestMapping("/findRegionPermissionList")
    public String findRemoteFeeList(MDRegionPermission regionPermission,Model model,RedirectAttributes redirectAttributes){
//        String view = "modules/providermd/regionPermissionList";
        String view = "modules/providermd/regionPermissionListNew";
        if(regionPermission.getCityId()==null){
            regionPermission.setType(1);
            model.addAttribute("regionPermission",regionPermission);
            model.addAttribute("show", false);
            return view;
        }
        try {
            Map<String, Object> map = regionPermissionService.findRegionPermissionListNew(regionPermission);
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
     * @param regionPermission
     * @param model
     * @return
     */
    @RequiresPermissions(value = {"md:regionPermission:view"})
    @RequestMapping(value = "/findRegionPermission", method = RequestMethod.POST)
    @ResponseBody
    public AjaxJsonEntity findRegionPermission(MDRegionPermission regionPermission, Model model){
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        List<MDRegionPermission> data = Lists.newArrayList();
        try {
            data = regionPermissionService.findListByCategoryAndCityId(regionPermission);
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
     * @param id
     * @param model
     * @return
     */
    @RequiresPermissions(value = {"md:regionPermission:view"})
    @RequestMapping("/selectStreet")
    public String selectStreet(@RequestParam(required=false) Long id, Long productCategoryId, Integer type, Model model){
//        String view = "modules/providermd/regionPermissionForm";
        String view = "modules/providermd/regionPermissionFormNew";
        AreaRemoteFeeModel areaRemoteFeeModel = new AreaRemoteFeeModel();
        if(id <= 0){
            model.addAttribute("areaRemoteFeeModel",areaRemoteFeeModel);
            return view;
        }
        try {
            Map<String, Object> map = regionPermissionService.selectStreet(id);
            model.addAttribute("area", map.get("area"));
            model.addAttribute("areaModelList", map.get("areaModelList"));
//            model.addAttribute("regionPermission", new MDRegionPermission());
            // update on 2020-06-17
            MDRegionPermission regionPermission = new MDRegionPermission();
            regionPermission.setCityId(id);
            regionPermission.setProductCategoryId(productCategoryId);
            regionPermission.setType(type);
            model.addAttribute("regionPermission", regionPermission);
            return view;
        }catch (Exception e){
            addMessage(model,e.getMessage());
            return view;
        }
    }

    @RequiresPermissions(value = {"md:regionPermission:edit"})
    @RequestMapping(value = "save")
    @ResponseBody
    public AjaxJsonEntity save(RegionPermissionModel regionPermissionModel){
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        User user = UserUtils.getUser();
        if(user == null){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("超时,请重新登录");
        }
        // 区县分组
//        Map<Long, List<MDRegionPermission>> map = regionPermissionModel.getRegionPermissions().stream().collect(Collectors.groupingBy(MDRegionPermission::getAreaId));
        try {
            regionPermissionService.save(regionPermissionModel.getRegionPermissions(), user);
        } catch (Exception e) {
            ajaxJsonEntity.setMessage(e.getMessage());
            ajaxJsonEntity.setSuccess(false);
        }
        return ajaxJsonEntity;
    }
}
