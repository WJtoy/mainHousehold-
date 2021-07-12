package com.wolfking.jeesite.ms.providermd.controller;

import com.kkl.kklplus.entity.md.MDDepositLevel;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.rpt.entity.RptSearchCondition;
import com.wolfking.jeesite.ms.providermd.service.DepositLevelService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping(value = "${adminPath}/provider/md/depositLevel")
public class DepositLevelController extends BaseController {

    @Autowired
    private DepositLevelService depositLevelService;

    @RequiresPermissions("md:depositLevel:view")
    @RequestMapping(value = {"list"})
    public String list(HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<MDDepositLevel> page = depositLevelService.findList(new Page<>(request, response));
        if(page.getList() != null && !page.getList().isEmpty()){
            List<MDDepositLevel> mdDepositLevels;
            mdDepositLevels = page.getList().stream().sorted(Comparator.comparing(MDDepositLevel::getSort)).collect(Collectors.toList());
            page.setList(mdDepositLevels);
        }
        model.addAttribute("page", page);
        return "modules/providermd/depositLevelList";
    }

    @RequiresPermissions("md:depositLevel:edit")
    @RequestMapping(value = "form")
    public String form(Long id, Model model) {
        MDDepositLevel mdDepositLevel = new MDDepositLevel();
        if (id != null && id > 0) {
            mdDepositLevel = depositLevelService.getById(id);
            if (mdDepositLevel == null) {
                mdDepositLevel = new MDDepositLevel();
                mdDepositLevel.setMaxAmount(null);
                mdDepositLevel.setMinAmount(null);
                mdDepositLevel.setDeductPerOrder(null);
            }else {
                if(mdDepositLevel.getMaxAmount() == 0){
                    mdDepositLevel.setMaxAmount(null);
                }
                if(mdDepositLevel.getMinAmount() == 0){
                    mdDepositLevel.setMinAmount(null);
                }

                if(mdDepositLevel.getDeductPerOrder() == 0){
                    mdDepositLevel.setDeductPerOrder(null);
                }
            }
        }else {
            mdDepositLevel.setMaxAmount(null);
            mdDepositLevel.setMinAmount(null);
            mdDepositLevel.setDeductPerOrder(null);
        }
        model.addAttribute("mdDepositLevel", mdDepositLevel);
        return "modules/providermd/depositLevelFrom";
    }


    /**
     * 保存数据(添加或修改)
     *
     * @param mdDepositLevel
     * @return
     */
    @RequiresPermissions("md:depositLevel:edit")
    @RequestMapping("save")
    @ResponseBody
    public AjaxJsonEntity save(MDDepositLevel mdDepositLevel) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity();
        try {
            depositLevelService.save(mdDepositLevel);
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
    @RequiresPermissions("md:depositLevel:edit")
    @RequestMapping("delete")
    @ResponseBody
    public AjaxJsonEntity delete(Long id) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity();
        ajaxJsonEntity.setSuccess(true);
        ajaxJsonEntity.setMessage("删除成功");
        try {
            depositLevelService.delete(id);
        } catch (Exception e) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage(e.getMessage());
        }
        return ajaxJsonEntity;
    }

    @ResponseBody
    @RequiresPermissions("md:depositLevel:edit")
    @RequestMapping(value = "check")
    public AjaxJsonEntity checkExportTask(Long id) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        try {

            Integer sum = depositLevelService.isDepositLevel(id);
            if(sum == 0){
                ajaxJsonEntity.setSuccess(false);
                ajaxJsonEntity.setMessage("质保等级使用中,不能删除");
            }

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
        Long id = depositLevelService.getByName(name);
        if (id == null) {
            return "true";
        } else if (loginId != null && loginId.equals(id)) {
            return "true";
        }
        return "false";
    }

    /**
     * 验证Code是否有效
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "checkCode")
    public String checkLoginValue(Long loginId, String code) {
        Long id = depositLevelService.getByCode(code);
        if (id == null) {
            return "true";
        } else if (loginId != null && loginId.equals(id)) {
            return "true";
        }
        return "false";
    }

}
