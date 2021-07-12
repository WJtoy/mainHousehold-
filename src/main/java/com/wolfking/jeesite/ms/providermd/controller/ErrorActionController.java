package com.wolfking.jeesite.ms.providermd.controller;

import com.google.common.collect.Lists;
import com.kkl.kklplus.entity.md.MDActionCode;
import com.kkl.kklplus.entity.md.MDErrorAction;
import com.kkl.kklplus.entity.md.MDErrorCode;
import com.kkl.kklplus.entity.md.MDErrorType;
import com.kkl.kklplus.entity.md.dto.MDActionCodeDto;
import com.kkl.kklplus.entity.md.dto.MDErrorActionDto;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.Product;
import com.wolfking.jeesite.modules.md.entity.ServiceType;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.SeqUtils;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providermd.service.*;
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
@RequestMapping(value = "${adminPath}/provider/md/errorAction")
public class ErrorActionController extends BaseController {
    @Autowired
    private MSErrorActionService msErrorActionService;
    @Autowired
    private MSServiceTypeService msServiceTypeService;
    @Autowired
    private MSErrorTypeService msErrorTypeService;
    @Autowired
    private MSErrorCodeService msErrorCodeService;
    @Autowired
    private MSProductService msProductService;
    @Autowired
    private MSActionCodeService msActionCodeService;
	
    @RequestMapping(value={"findList"})
    public String findList(MDErrorActionDto mdErrorActionDto, HttpServletRequest request, HttpServletResponse response, Model model ) {
        Page<MDErrorActionDto> page;
        List<MDErrorType> mdErrorTypeList = Lists.newArrayList();
        /*if (mdErrorActionDto.getProductId() == null || mdErrorActionDto.getProductId().intValue() ==0) {
            page = new Page<>(request, response);
        } else {*/
            mdErrorActionDto.setCustomerId(0L);
            page = msErrorActionService.findPage(new Page<>(request, response), mdErrorActionDto);
            mdErrorTypeList = msErrorTypeService.findErrorTypesByProductId(mdErrorActionDto.getProductId());
        List<Product> productList = msProductService.findSingleListByProductCategoryId(mdErrorActionDto.getProductCategoryId());
        //}
        List<ServiceType> serviceTypeList = msServiceTypeService.findListByMaintenance();
        model.addAttribute("serviceTypeList", serviceTypeList);
        model.addAttribute("errorActionDto", mdErrorActionDto);
        model.addAttribute("errorTypeList", mdErrorTypeList);
        model.addAttribute("productList", productList);
        model.addAttribute("page", page);
        return "modules/providermd/errorActionList";
    }

    @RequestMapping(value={"findListForActionCode"})
    public String findActionList(MDErrorActionDto mdErrorActionDto, HttpServletRequest request, HttpServletResponse response, Model model ) {
        Page<MDErrorActionDto> page;
        List<MDErrorType> mdErrorTypeList = Lists.newArrayList();
        List<MDErrorCode> mdErrorCodeList = Lists.newArrayList();
        if (mdErrorActionDto.getProductId() == null || mdErrorActionDto.getProductId().intValue() ==0) {
            page = new Page<>(request, response);
        } else {
            mdErrorActionDto.setCustomerId(0L);
            page = msErrorActionService.findPageForActionCode(new Page<>(request, response),mdErrorActionDto);
            mdErrorTypeList = msErrorTypeService.findErrorTypesByProductId(mdErrorActionDto.getProductId());
            if (mdErrorActionDto.getErrorCodeDto() != null && mdErrorActionDto.getErrorCodeDto().getErrorTypeId() != null) {
                mdErrorCodeList = msErrorCodeService.findListByProductAndErrorType(mdErrorActionDto.getErrorCodeDto().getErrorTypeId(), mdErrorActionDto.getProductId());
            }
        }
        List<ServiceType> serviceTypeList = msServiceTypeService.findListByMaintenance();

        model.addAttribute("errorActionDto", mdErrorActionDto);
        model.addAttribute("serviceTypeList", serviceTypeList);
        model.addAttribute("errorTypeList", mdErrorTypeList);
        model.addAttribute("errorCodeList", mdErrorCodeList);
        model.addAttribute("page", page);
        return "modules/providermd/actionCodeList";
    }

    @RequestMapping(value={"form"})
    public String form(String id, HttpServletRequest request, HttpServletResponse response, Model model ) {
        List<ServiceType> serviceTypeList = msServiceTypeService.findListByMaintenance();
        try {
            Long errorActionId = StringUtils.toLong(id);
            MDErrorActionDto errorActionDto = msErrorActionService.getAssociatedDataById(errorActionId);
            model.addAttribute("errorActionDto", errorActionDto);
        } catch (Exception ex) {
        }
        model.addAttribute("serviceTypeList", serviceTypeList);

        return "modules/providermd/errorActionForm";
    }


    @RequiresPermissions("md:erroraction:edit")
    @RequestMapping(value={"save"})
    public String save(MDErrorActionDto mdErrorActionDto, RedirectAttributes redirectAttributes) {
        User user = UserUtils.getUser();
        if (user.getId() != null) {
            mdErrorActionDto.getActionCodeDto().setCreateById(user.getId());
            mdErrorActionDto.getActionCodeDto().setUpdateById(user.getId());
            mdErrorActionDto.getActionCodeDto().setCreateDate(new Date());
            mdErrorActionDto.getActionCodeDto().setUpdateDate(new Date());
            mdErrorActionDto.getActionCodeDto().setCode(SeqUtils.NextSequenceNo("ErrorAction"));
            mdErrorActionDto.getActionCodeDto().setProductId(mdErrorActionDto.getProductId());
            msErrorActionService.save(mdErrorActionDto);
        } else {
            addMessage(redirectAttributes, "当前用户不存在");
        }
        return "redirect:" + adminPath + "/provider/md/errorAction/findListForActionCode?productId=" + mdErrorActionDto.getProductId();
    }

    @RequiresPermissions("md:erroraction:edit")
    @RequestMapping(value={"delete"})
    public String delete(MDErrorAction mdErrorAction, RedirectAttributes redirectAttributes) {
        User user = UserUtils.getUser();
        if (user.getId() != null) {
            mdErrorAction.setUpdateById(user.getId());
            mdErrorAction.setUpdateDate(new Date());
            mdErrorAction.setDelFlag(MDErrorType.DEL_FALG_DELETE);
            msErrorActionService.delete(mdErrorAction);
        } else {
            addMessage(redirectAttributes, "当前用户不存在");
        }
        return "redirect:" + adminPath + "/provider/md/errorAction/findList?productId="+mdErrorAction.getProductId();
    }

    @RequiresPermissions("md:erroraction:edit")
    @RequestMapping(value={"deleteForActionCode"})
    public String deleteForActionCode(MDErrorAction mdErrorAction, RedirectAttributes redirectAttributes) {
        User user = UserUtils.getUser();
        if (user.getId() != null) {
            mdErrorAction.setUpdateById(user.getId());
            mdErrorAction.setUpdateDate(new Date());
            mdErrorAction.setDelFlag(MDErrorType.DEL_FALG_DELETE);
            msErrorActionService.delete(mdErrorAction);
        } else {
            addMessage(redirectAttributes, "当前用户不存在");
        }
        return "redirect:" + adminPath + "/provider/md/errorAction/findListForActionCode?productId=" + mdErrorAction.getProductId();
    }

    @ResponseBody
    @RequestMapping(value={"ajax/getIdByProductAndErrorCode"})
    public AjaxJsonEntity getIdByProductAndErrorCode(Long productId, Long errorCodeId, HttpServletResponse response) {
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity jsonEntity=new AjaxJsonEntity();
        jsonEntity.setSuccess(true);
        try{
            Long id = msErrorActionService.getIdByProductAndErrorCode(errorCodeId,productId);
            jsonEntity.setSuccess(true);
            jsonEntity.setData(id==null?0:id);
        }catch (Exception e){
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage(e.getMessage());
        }
        return jsonEntity;
    }

    /**
     * 按产品id + 故障代码id读取故障处理
     * @param productId 产品id
     * @param errorCodeId   故障代码
     */
    @ResponseBody
    @RequestMapping(value={"ajax/findListByProductAndEC"})
    public AjaxJsonEntity findListByProductAndErrorCode(Long productId, Long errorCodeId, HttpServletResponse response) {
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity jsonEntity=new AjaxJsonEntity();
        jsonEntity.setSuccess(true);
        try{
            List<MDActionCodeDto> mdErrorCodeList = msActionCodeService.findListByProductAndErrorCode(errorCodeId,productId);
            jsonEntity.setSuccess(true);
            jsonEntity.setData(mdErrorCodeList);
        }catch (Exception e){
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage(e.getMessage());
        }
        return jsonEntity;
    }

    /**
     * 更新故障处理中故障分析
     * @param
     */
    @ResponseBody
    @RequestMapping(value={"ajax/updateActionCodeNameAndAnalysis"})
    public AjaxJsonEntity updateActionCodeNameAndAnalysis(Long id,String analysis, String name, Long serviceTypeId, HttpServletResponse response) {
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity jsonEntity = new AjaxJsonEntity();
        jsonEntity.setSuccess(true);
        User user = UserUtils.getUser();
        if(user==null || user.getId()<=0){
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage("用户不存在,请重新登录");
            return jsonEntity;
        }
        try{
            MDActionCode mdActionCode = new MDActionCode();
            mdActionCode.setId(id);
            mdActionCode.setAnalysis(analysis);
            mdActionCode.setName(name);
            mdActionCode.setServiceTypeId(serviceTypeId);
            mdActionCode.setUpdateDate(new Date());
            mdActionCode.setUpdateById(user.getId());
            msErrorActionService.updateActionCodeNameAndAnalysis(mdActionCode);
            jsonEntity.setSuccess(true);
        }catch (Exception e){
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage(e.getMessage());
        }
        return jsonEntity;
    }

}
