package com.wolfking.jeesite.ms.providermd.controller;

import com.google.common.collect.Lists;
import com.kkl.kklplus.entity.md.MDErrorCode;
import com.kkl.kklplus.entity.md.MDErrorType;
import com.kkl.kklplus.entity.md.dto.MDErrorCodeDto;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.SeqUtils;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providermd.service.MSCustomerErrorCodeService;
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
@RequestMapping(value = "${adminPath}/provider/md/errorCode")
@Slf4j
public class ErrorCodeController extends BaseController {

    @Autowired
    private MSErrorCodeService msErrorCodeService;

    @Autowired
    private MSCustomerErrorCodeService msCustomerErrorCodeService;

    @Autowired
    private MSErrorTypeService msErrorTypeService;

    @Autowired
    private MSCustomerErrorTypeService msCustomerErrorTypeService;

    @RequestMapping(value= "findList")
    public String  findList(MDErrorCode mdErrorCode, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<MDErrorCodeDto> page;
        List<MDErrorType> mdErrorTypeList = Lists.newArrayList();
        if (mdErrorCode.getProductId() == null || mdErrorCode.getProductId().intValue() ==0) {
            page = new Page<>(request, response);
        } else {
            mdErrorCode.setCustomerId(0L);
            page = msErrorCodeService.findListForPage(new Page<>(request, response), mdErrorCode);
            mdErrorTypeList = msErrorTypeService.findErrorTypesByProductId(mdErrorCode.getProductId());
        }

        model.addAttribute("errorTypeList", mdErrorTypeList);
        model.addAttribute("errorCode", mdErrorCode);
        model.addAttribute("page", page);

        return "modules/providermd/errorCodeList";
    }

    @RequiresPermissions("md:errorcode:edit")
    @RequestMapping(value="save")
    public String save(MDErrorCode mdErrorCode, HttpServletRequest request, HttpServletResponse response, Model model, RedirectAttributes redirectAttributes) {
        User user = UserUtils.getUser();
        if (user.getId() != null) {
            mdErrorCode.setCreateById(user.getId());
            mdErrorCode.setUpdateById(user.getId());
            mdErrorCode.setCreateDate(new Date());
            mdErrorCode.setUpdateDate(new Date());
            mdErrorCode.setCode(SeqUtils.NextSequenceNo("ErrorCode"));
            msErrorCodeService.save(mdErrorCode);
        } else {
            addMessage(redirectAttributes, "当前用户不存在");
        }
        return "redirect:" + adminPath + "/provider/md/errorCode/findList?productId="+mdErrorCode.getProductId();
        //return "redirect:" + adminPath + "/provider/md/errorCode/findList?productId="+mdErrorCode.getProductId()+"&errorTypeId="+mdErrorCode.getErrorTypeId();
    }

    @RequiresPermissions("md:errorcode:edit")
    @RequestMapping(value="delete")
    public String delete(MDErrorCode mdErrorCode, HttpServletRequest request, HttpServletResponse response, Model model, RedirectAttributes redirectAttributes) {
        User user = UserUtils.getUser();
        if (user.getId() != null) {
            mdErrorCode.setUpdateById(user.getId());
            mdErrorCode.setUpdateDate(new Date());
            mdErrorCode.setDelFlag(MDErrorCode.DEL_FALG_DELETE);
            msErrorCodeService.delete(mdErrorCode);
        } else {
            addMessage(redirectAttributes, "当前用户不存在");
        }
        return "redirect:" + adminPath + "/provider/md/errorCode/findList?productId="+mdErrorCode.getProductId();
    }

    @ResponseBody
    @RequestMapping(value={"checkName"})
    public String checkName(Long productId, Long errorTypeId, String name) {
        User user = UserUtils.getUser();
        if (user == null || user.getId() == null) {
            return "false";
        }
        if (productId == null || productId.intValue()==0) {
            return "请选择产品";
        }
        if (errorTypeId == null || errorTypeId.intValue()==0) {
            return "请选择故障分类";
        }
        if (StringUtils.isBlank(name)) {
            return "true";
        }
        try {
            MDErrorCode mdErrorCode = new MDErrorCode();
            mdErrorCode.setProductId(productId);
            mdErrorCode.setErrorTypeId(errorTypeId);
            mdErrorCode.setName(name);
            String result = msErrorCodeService.checkName(mdErrorCode);
            return result.equalsIgnoreCase("true") ? result : "故障分类已存在.";
        } catch (Exception ex) {
            log.error("error,", ex);
            return "false";
        }
    }

    /**
     * 上门服务，
     * @param productId
     * @param errorTypeId
     * @param response
     * @return
     */
    @ResponseBody
    @RequestMapping(value={"ajax/findListByProductIdAndErrorTypeId"})
    public AjaxJsonEntity findListByProductIdAndErrorTypeId(Long productId,Long customerId, Long errorTypeId, HttpServletResponse response) {
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity jsonEntity=new AjaxJsonEntity();
        jsonEntity.setSuccess(true);
        try{
            List<MDErrorCode> mdErrorCodeList = msCustomerErrorCodeService.findListByProductAndErrorType(errorTypeId,productId,customerId);
            jsonEntity.setSuccess(true);
            jsonEntity.setData(mdErrorCodeList);
        }catch (Exception e){
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage(e.getMessage());
        }
        return jsonEntity;
    }

    @ResponseBody
    @RequestMapping(value={"ajax/getIdByProductAndErrorType"})
    public AjaxJsonEntity getIdByProductAndErrorType(Long productId, Long errorTypeId, HttpServletResponse response) {
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity jsonEntity=new AjaxJsonEntity();
        jsonEntity.setSuccess(true);
        try{
            Long id = msErrorCodeService.getIdByProductAndErrorType(errorTypeId,productId);
            jsonEntity.setSuccess(true);
            jsonEntity.setData(id==null?0:id);
        }catch (Exception e){
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage(e.getMessage());
        }
        return jsonEntity;
    }
}
