package com.wolfking.jeesite.ms.providermd.controller;

import com.kkl.kklplus.entity.md.MDErrorType;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.SeqUtils;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providermd.service.MSCustomerErrorTypeService;
import com.wolfking.jeesite.ms.providermd.service.MSErrorCodeService;
import com.wolfking.jeesite.ms.providermd.service.MSErrorTypeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping(value = "${adminPath}/provider/md/errorType")
@Slf4j
public class ErrorTypeController extends BaseController {
    @Autowired
    private MSErrorTypeService msErrorTypeService;

    @Autowired
    private MSCustomerErrorTypeService msCustomerErrorTypeService;

    @RequestMapping(value={"findList"})
    public String findList(MDErrorType mdErrorType, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<MDErrorType> page;
        if (mdErrorType.getProductId() == null || mdErrorType.getProductId().intValue() ==0) {
            page = new Page<>(request, response);
            page.setPageSize(5);
        } else {
            Page<MDErrorType> initPage = new Page<>(request, response);
            initPage.setPageSize(5);
            mdErrorType.setCustomerId(0L);
            page = msErrorTypeService.findListForPage(initPage, mdErrorType);
        }

        model.addAttribute("errorType", mdErrorType);
        model.addAttribute("page", page);

        return "modules/providermd/errorTypeList";
    }

    @RequiresPermissions("md:errortype:edit")
    @RequestMapping(value={"save"})
    public String save(MDErrorType mdErrorType, HttpServletRequest request, HttpServletResponse response, Model model, RedirectAttributes redirectAttributes) {
        User user = UserUtils.getUser();
        if (user.getId() != null) {
            mdErrorType.setCreateById(user.getId());
            mdErrorType.setUpdateById(user.getId());
            mdErrorType.setCreateDate(new Date());
            mdErrorType.setUpdateDate(new Date());
            mdErrorType.setCode(SeqUtils.NextSequenceNo("ErrorType"));
            msErrorTypeService.save(mdErrorType);
        } else {
            addMessage(redirectAttributes, "当前用户不存在");
        }
        return "redirect:" + adminPath + "/provider/md/errorType/findList?productId="+mdErrorType.getProductId();
    }

    @RequiresPermissions("md:errortype:edit")
    @RequestMapping(value={"delete"})
    public String delete(MDErrorType mdErrorType, HttpServletRequest request, HttpServletResponse response, Model model, RedirectAttributes redirectAttributes) {
        User user = UserUtils.getUser();
        if (user.getId() != null) {
            mdErrorType.setUpdateById(user.getId());
            mdErrorType.setUpdateDate(new Date());
            mdErrorType.setDelFlag(MDErrorType.DEL_FALG_DELETE);
            msErrorTypeService.delete(mdErrorType);
        } else {
            addMessage(redirectAttributes, "当前用户不存在");
        }
        return "redirect:" + adminPath + "/provider/md/errorType/findList?productId="+mdErrorType.getProductId();
    }

    @ResponseBody
    @RequestMapping(value={"checkName"})
    public String checkName(Long productId, String name) {
        User user = UserUtils.getUser();
        if (user == null || user.getId() == null) {
            return "false";
        }
        if (productId == null || productId.intValue()==0) {
            return "false";
        }
        if (StringUtils.isBlank(name)) {
            return "true";
        }
        try {
            String result = msErrorTypeService.checkName(productId, name,0L);
            return result.equalsIgnoreCase("true") ? result : "故障分类已存在.";
        } catch (Exception ex) {
            log.error("error,", ex);
            return "false";
        }
    }

    /**
     * 上门服务，根据客户+产品读取故障分类
     * @param productId
     * @param response
     * @return
     */
    @ResponseBody
    @RequestMapping(value={"ajax/findListByProductId"})
    public AjaxJsonEntity findListByProductId(Long productId,Long customerId, HttpServletResponse response) {
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity jsonEntity=new AjaxJsonEntity();
        jsonEntity.setSuccess(true);
        try{
            List<MDErrorType> mdErrorCodeList = msCustomerErrorTypeService.findListByProductIdAndCustomerIdFromCache(productId,customerId);
            jsonEntity.setSuccess(true);
            jsonEntity.setData(mdErrorCodeList);
        }catch (Exception e){
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage(e.getMessage());
        }
        return jsonEntity;
    }

}
