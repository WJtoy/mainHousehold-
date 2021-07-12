package com.wolfking.jeesite.ms.providermd.controller;

import com.kkl.kklplus.entity.md.AppFeedbackEnum;
import com.kkl.kklplus.entity.md.MDAppFeedbackType;
import com.kkl.kklplus.entity.md.dto.MDAppFeedbackTypeDto;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.mapper.md.MDAppFeedbackTypeMapper;
import com.wolfking.jeesite.ms.providermd.entity.AppFeedbackTypeVModel;
import com.wolfking.jeesite.ms.providermd.service.MSAppFeedbackTypeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;


/**
 * 客户产品型号服务
 */
@Controller
@Slf4j
@RequestMapping(value = "${adminPath}/provider/md/appFeedbackType")
public class AppFeedbackTypeController extends BaseController {


    @Autowired
    private MSAppFeedbackTypeService appFeedbackTypeService;

    /**
     * 分页查询
     * @param appFeedbackType
     * @return
     */
    @RequiresPermissions("md:appfeedbacktype:view")
    @RequestMapping(value = {"findList", ""})
    public String findList(MDAppFeedbackType appFeedbackType, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<MDAppFeedbackTypeDto> appFeedbackTypePage = new Page<>(request, response);
        Page<AppFeedbackTypeVModel> page= appFeedbackTypeService.findListForPage(appFeedbackTypePage,appFeedbackType);
        model.addAttribute("appFeedbackType",appFeedbackType);
        model.addAttribute("page", page);
        return "modules/providermd/appFeedbackTypeList";
    }

    @RequiresPermissions("md:appfeedbacktype:edit")
    @RequestMapping(value = "form")
    public String form(MDAppFeedbackType appFeedbackType,Model model) {
        if(appFeedbackType.getId()!=null && appFeedbackType.getId()>0){
            appFeedbackType = appFeedbackTypeService.getById(appFeedbackType.getId());
            if(appFeedbackType == null){
                appFeedbackType = new MDAppFeedbackType();
            }
        }else{
            int sortBt = appFeedbackTypeService.getMaxSortBy();
            appFeedbackType.setSortBy(sortBt + 10);
            appFeedbackType.setParentId(0L);
            appFeedbackType.setHasChildren(0);
            appFeedbackType.setIsEffect(0);
            appFeedbackType.setIsAbnormaly(0);
            appFeedbackType.setAbnormalyOverTimes(0);
            appFeedbackType.setActionType(0);
            appFeedbackType.setName("");
            appFeedbackType.setUserType(0);
            appFeedbackType.setSumType(1);

        }
        model.addAttribute("feedbackTypeEnumList",appFeedbackTypeService.findFeedbackType());
        model.addAttribute("appFeedbackType", appFeedbackType);
        return "modules/providermd/appFeedbackTypeForm";
    }

    @RequiresPermissions("md:appfeedbacktype:edit")
    @RequestMapping(value = "feedbackReasonFrom")
    public String feedbackReasonFrom(MDAppFeedbackType appFeedbackType,Model model){
        if(appFeedbackType.getId()!=null && appFeedbackType.getId()>0){
            appFeedbackType = appFeedbackTypeService.getById(appFeedbackType.getId());
            MDAppFeedbackType parentFeedbackType = appFeedbackTypeService.getById(appFeedbackType.getParentId());
            if(parentFeedbackType!=null){
                appFeedbackType.setParentName(parentFeedbackType.getLabel());
            }
            if(appFeedbackType == null){
                appFeedbackType = new MDAppFeedbackType();
            }
        }else if(appFeedbackType.getParentId()!=null && appFeedbackType.getParentId()>0){
            MDAppFeedbackType parentFeedbackType = appFeedbackTypeService.getById(appFeedbackType.getParentId());
            if(parentFeedbackType !=null){
                appFeedbackType.setParentName(parentFeedbackType.getLabel());
                appFeedbackType.setFeedbackType(parentFeedbackType.getFeedbackType());
            }
            int sortBt = appFeedbackTypeService.getMaxSortBy();
            appFeedbackType.setSortBy(sortBt + 10);
        }
        model.addAttribute("feedbackTypeEnumList",appFeedbackTypeService.findFeedbackType());
        model.addAttribute("actionTypeEnumList",appFeedbackTypeService.findActionTypeList());
        model.addAttribute("userTypeList",appFeedbackTypeService.findUserTypeList());
        model.addAttribute("sumTypeList",appFeedbackTypeService.findSumTypeList());
        model.addAttribute("appFeedbackType", appFeedbackType);
        return "modules/providermd/appFeedbackReasonForm";
    }


    /**
     * 保存数据(添加或修改)
     * @param appFeedbackType
     * @return
     */
    @RequiresPermissions("md:appfeedbacktype:edit")
    @RequestMapping("save")
    @ResponseBody
    public AjaxJsonEntity save(MDAppFeedbackType appFeedbackType, HttpServletRequest request) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity();
        User user = UserUtils.getUser();
        if (user.getId() != null) {
            appFeedbackType.setCreateById(user.getId());
            appFeedbackType.setUpdateById(user.getId());
            appFeedbackType.setCreateAt(new Date().getTime());
            appFeedbackType.setUpdateAt(new Date().getTime());
            try {
                appFeedbackTypeService.save(appFeedbackType);
                ajaxJsonEntity.setSuccess(true);
                ajaxJsonEntity.setMessage("保存成功");
            }catch (Exception e){
                ajaxJsonEntity.setSuccess(false);
                ajaxJsonEntity.setMessage(e.getMessage());
            }
        } else {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("当前用户不存在");
        }
        return ajaxJsonEntity;
    }

    /**
     * 启用或者停用
     * @param appFeedbackType
     * @return
     */
    @RequestMapping(value = "disableOrEnable")
    @ResponseBody
    public AjaxJsonEntity disableOrEnable(MDAppFeedbackType appFeedbackType) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity();
        User user = UserUtils.getUser();
        if (user.getId() != null) {
            appFeedbackType.setCreateById(user.getId());
            appFeedbackType.setUpdateById(user.getId());
            appFeedbackType.setCreateAt(new Date().getTime());
            appFeedbackType.setUpdateAt(new Date().getTime());
            try {
                appFeedbackTypeService.disableOrEnable(appFeedbackType);
                ajaxJsonEntity.setSuccess(true);
                if(appFeedbackType.getIsEffect() == 0){
                    ajaxJsonEntity.setMessage("停用成功");
                }else{
                    ajaxJsonEntity.setMessage("启用成功");
                }
            }catch (Exception e){
                ajaxJsonEntity.setSuccess(false);
                ajaxJsonEntity.setMessage(e.getMessage());
            }
        } else {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("当前用户不存在");
        }
        return ajaxJsonEntity;
    }

}
