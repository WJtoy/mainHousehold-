package com.wolfking.jeesite.ms.providermd.controller;

import com.kkl.kklplus.entity.md.MDCustomerVipLevel;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.ms.providermd.service.MSCustomerVipLevelService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Controller
@RequestMapping(value = "${adminPath}/provider/md/customerVip")
public class CustomerVipLevelController extends BaseController {

    @Autowired
    private MSCustomerVipLevelService msCustomerVipLevelService;

    @RequiresPermissions("md:customervip:view")
    @RequestMapping(value = {"list"})
    public String list(MDCustomerVipLevel mdCustomerVipLevel, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<MDCustomerVipLevel> page = msCustomerVipLevelService.findList(new Page<>(request, response), mdCustomerVipLevel);
        model.addAttribute("mdCustomerVipLevel", mdCustomerVipLevel);
        model.addAttribute("page", page);
        return "modules/providermd/customerVipLevelList";
    }

    @RequiresPermissions("md:customervip:edit")
    @RequestMapping(value = "form")
    public String form(Long id, Model model) {
        MDCustomerVipLevel mdCustomerVipLevel = new MDCustomerVipLevel();
        if (id != null && id > 0) {
            mdCustomerVipLevel = msCustomerVipLevelService.getById(id);
            if (mdCustomerVipLevel == null) {
                mdCustomerVipLevel = new MDCustomerVipLevel();
            }
        }
        model.addAttribute("mdCustomerVipLevel", mdCustomerVipLevel);
        return "modules/providermd/customerVipLevelFrom";
    }


    /**
     * 保存数据(添加或修改)
     *
     * @param mdCustomerVipLevel
     * @return
     */
    @RequiresPermissions("md:customerproduct:edit")
    @RequestMapping("save")
    @ResponseBody
    public AjaxJsonEntity save(MDCustomerVipLevel mdCustomerVipLevel) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity();
        try {
            msCustomerVipLevelService.save(mdCustomerVipLevel);
            ajaxJsonEntity.setSuccess(true);
            ajaxJsonEntity.setMessage("保存成功");
        } catch (Exception e) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage(e.getMessage());
        }
        return ajaxJsonEntity;
    }

    /**
     * 删除
     *
     * @param id
     * @return
     */
    @RequiresPermissions("md:customerproduct:edit")
    @RequestMapping("delete")
    @ResponseBody
    public AjaxJsonEntity delete(Long id) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity();
        try {
            msCustomerVipLevelService.delete(id);
            ajaxJsonEntity.setSuccess(true);
            ajaxJsonEntity.setMessage("删除成功");
        } catch (Exception e) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage(e.getMessage());
        }
        return ajaxJsonEntity;
    }
    /**
     * 验证名称是否有效
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "checkName")
    public String checkName(Long loginId, String name) {
        Long id = msCustomerVipLevelService.getByName(name);
        if (id == null) {
            return "true";
        } else if (loginId != null && loginId.equals(id)) {
            return "true";
        }
        return "false";
    }

    /**
     * 验证值是否有效
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "checkValue")
    public String checkLoginValue(Long loginId, Integer value) {
        Long id = msCustomerVipLevelService.getByValue(value);
        if (id == null) {
            return "true";
        } else if (loginId != null && loginId.equals(id)) {
            return "true";
        }
        return "false";
    }

}
