package com.wolfking.jeesite.ms.providermd.controller;

import com.google.common.collect.Lists;
import com.kkl.kklplus.entity.md.MDActionCode;
import com.kkl.kklplus.entity.md.MDErrorAction;
import com.kkl.kklplus.entity.md.MDErrorCode;
import com.kkl.kklplus.entity.md.MDErrorType;
import com.kkl.kklplus.entity.md.dto.MDActionCodeDto;
import com.kkl.kklplus.entity.md.dto.MDErrorActionDto;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.Product;
import com.wolfking.jeesite.modules.md.entity.ServiceType;
import com.wolfking.jeesite.modules.md.service.CustomerService;
import com.wolfking.jeesite.modules.md.service.ProductService;
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
@RequestMapping(value = "${adminPath}/provider/md/customerErrorAction")
public class CustomerErrorActionController extends BaseController {
    @Autowired
    private MSCustomerErrorActionService customerErrorActionService;
    @Autowired
    private MSServiceTypeService msServiceTypeService;
    @Autowired
    private MSErrorTypeService msErrorTypeService;
    @Autowired
    private MSErrorCodeService msErrorCodeService;

    @Autowired
    private MSErrorActionService msErrorActionService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ProductService productService;

    @Autowired
    private MSCustomerErrorTypeService customerErrorTypeService;

    @Autowired
    private MSCustomerErrorCodeService customerErrorCodeService;

    @RequiresPermissions("md:customererroraction:view")
    @RequestMapping(value={"findList"})
    public String findList(MDErrorActionDto mdErrorActionDto, HttpServletRequest request, HttpServletResponse response, Model model ) {
        Page<MDErrorActionDto> page;
        List<MDErrorType> mdErrorTypeList = Lists.newArrayList();
        List<Product> productList = Lists.newArrayList();
        if (mdErrorActionDto.getCustomerId() == null || mdErrorActionDto.getCustomerId()<=0) {
            page = new Page<>(request, response);
        } else {
            page = customerErrorActionService.findPage(new Page<>(request, response), mdErrorActionDto);
            if(mdErrorActionDto.getProductId()!=null && mdErrorActionDto.getProductId()>0){
                mdErrorTypeList = customerErrorTypeService.findErrorTypesByProductId(mdErrorActionDto.getCustomerId(),mdErrorActionDto.getProductId());
            }
            productList = productService.getCustomerProductList(mdErrorActionDto.getCustomerId());
        }
        List<ServiceType> serviceTypeList = msServiceTypeService.findListByMaintenance();
        model.addAttribute("serviceTypeList", serviceTypeList);
        model.addAttribute("errorActionDto", mdErrorActionDto);
        model.addAttribute("errorTypeList", mdErrorTypeList);
        model.addAttribute("productList", productList);
        model.addAttribute("page", page);
        return "modules/providermd/customerErrorActionList";
    }

    @RequiresPermissions("md:customererroraction:view")
    @RequestMapping(value={"findListForActionCode"})
    public String findActionList(MDErrorActionDto mdErrorActionDto, HttpServletRequest request, HttpServletResponse response, Model model ) {
        Page<MDErrorActionDto> page;
        List<MDErrorType> mdErrorTypeList = Lists.newArrayList();
        List<MDErrorCode> mdErrorCodeList = Lists.newArrayList();
        List<Product> productList = Lists.newArrayList();
        if (mdErrorActionDto.getCustomerId()==null || mdErrorActionDto.getCustomerId()<=0 || mdErrorActionDto.getProductId() == null || mdErrorActionDto.getProductId().intValue() ==0) {
            page = new Page<>(request, response);
        } else {
            page = customerErrorActionService.findPageForActionCode(new Page<>(request, response),mdErrorActionDto);
            if(mdErrorActionDto.getCustomerId()!=null && mdErrorActionDto.getCustomerId()>0){
                productList = productService.getCustomerProductList(mdErrorActionDto.getCustomerId());
                mdErrorTypeList = customerErrorTypeService.findErrorTypesByProductId(mdErrorActionDto.getCustomerId(),mdErrorActionDto.getProductId());
                if (mdErrorActionDto.getErrorCodeDto() != null && mdErrorActionDto.getErrorCodeDto().getErrorTypeId() != null) {
                    mdErrorCodeList = customerErrorCodeService.findListByProductAndErrorType(mdErrorActionDto.getErrorCodeDto().getErrorTypeId(), mdErrorActionDto.getProductId(),mdErrorActionDto.getCustomerId());
                }
            }
        }
        List<ServiceType> serviceTypeList = msServiceTypeService.findListByMaintenance();
        model.addAttribute("errorActionDto", mdErrorActionDto);
        model.addAttribute("serviceTypeList", serviceTypeList);
        model.addAttribute("errorTypeList", mdErrorTypeList);
        model.addAttribute("errorCodeList", mdErrorCodeList);
        model.addAttribute("productList",productList);
        model.addAttribute("page", page);
        return "modules/providermd/customerErrorActionCodeList";
    }

    @RequestMapping(value={"form"})
    public String form(String id, HttpServletRequest request, HttpServletResponse response, Model model ) {
        List<ServiceType> serviceTypeList = msServiceTypeService.findListByMaintenance();
        Long errorActionId = StringUtils.toLong(id);
        MDErrorActionDto errorActionDto = msErrorActionService.getAssociatedDataById(errorActionId);
        if(errorActionDto!=null && errorActionDto.getCustomerId()!=null){
            Customer customer = customerService.get(errorActionDto.getCustomerId());
            if(customer!=null){
                errorActionDto.setCustomerName(customer.getName());
            }
        }
        model.addAttribute("errorActionDto", errorActionDto);
        model.addAttribute("serviceTypeList", serviceTypeList);

        return "modules/providermd/customerErrorActionForm";
    }


    @RequiresPermissions("md:customererroraction:edit")
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
            customerErrorActionService.save(mdErrorActionDto);
        } else {
            addMessage(redirectAttributes, "当前用户不存在");
        }
        return "redirect:" + adminPath + "/provider/md/customerErrorAction/findListForActionCode?productId=" + mdErrorActionDto.getProductId() + "&customerId="+mdErrorActionDto.getCustomerId();
    }

    @RequiresPermissions("md:customererroraction:edit")
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
        return "redirect:" + adminPath + "/provider/md/customerErrorAction/findList?productId="+mdErrorAction.getProductId() + "&customerId="+mdErrorAction.getCustomerId();
    }


    /**
     * 更新故障处理中故障分析
     * @param
     */
    @RequiresPermissions("md:customererroraction:edit")
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
            mdActionCode.setUpdateById(user.getId());
            mdActionCode.setUpdateDate(new Date());
            msErrorActionService.updateActionCodeNameAndAnalysis(mdActionCode);
            jsonEntity.setSuccess(true);
        }catch (Exception e){
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage(e.getMessage());
        }
        return jsonEntity;
    }

    @RequiresPermissions("md:customererroraction:edit")
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
        return "redirect:" + adminPath + "/provider/md/customerErrorAction/findListForActionCode?productId=" + mdErrorAction.getProductId() +"&customerId="+mdErrorAction.getCustomerId();
    }

}
