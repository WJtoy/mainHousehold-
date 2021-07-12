package com.wolfking.jeesite.ms.providermd.controller;

import com.google.common.collect.Lists;
import com.kkl.kklplus.entity.md.MDErrorCode;
import com.kkl.kklplus.entity.md.MDErrorType;
import com.kkl.kklplus.entity.md.dto.MDErrorCodeDto;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.Product;
import com.wolfking.jeesite.modules.md.service.ProductService;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.SeqUtils;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providermd.service.MSCustomerErrorCodeService;
import com.wolfking.jeesite.ms.providermd.service.MSCustomerErrorTypeService;
import com.wolfking.jeesite.ms.providermd.service.MSErrorCodeService;
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
@RequestMapping(value = "${adminPath}/provider/md/customerErrorCode")
@Slf4j
public class CustomerErrorCodeController extends BaseController {

    @Autowired
    private MSCustomerErrorCodeService customerErrorCodeService;

    @Autowired
    private ProductService productService;

    @Autowired
    private MSErrorCodeService msErrorCodeService;

    @Autowired
    private MSCustomerErrorTypeService customerErrorTypeService;

    @RequiresPermissions("md:customererroraction:view")
    @RequestMapping(value= "findList")
    public String  findList(MDErrorCode mdErrorCode, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<MDErrorCodeDto> page;
        List<MDErrorType> mdErrorTypeList = Lists.newArrayList();
        List<Product> productList = Lists.newArrayList();
        if (mdErrorCode.getCustomerId()==null || mdErrorCode.getCustomerId()<=0 || mdErrorCode.getProductId() == null || mdErrorCode.getProductId().intValue() ==0) {
            page = new Page<>(request, response);
        } else {
            page = customerErrorCodeService.findListForPage(new Page<>(request, response), mdErrorCode);
            if(mdErrorCode.getCustomerId() !=null && mdErrorCode.getCustomerId()>0){
                mdErrorTypeList = customerErrorTypeService.findErrorTypesByProductId(mdErrorCode.getCustomerId(),mdErrorCode.getProductId());
                productList = productService.getCustomerProductList(mdErrorCode.getCustomerId());
            }
        }
        model.addAttribute("errorTypeList", mdErrorTypeList);
        model.addAttribute("errorCode", mdErrorCode);
        model.addAttribute("productList",productList);
        model.addAttribute("page", page);

        return "modules/providermd/customerErrorCodeList";
    }

    @RequiresPermissions("md:customererroraction:edit")
    @RequestMapping(value="save")
    public String save(MDErrorCode mdErrorCode, HttpServletRequest request, HttpServletResponse response, Model model, RedirectAttributes redirectAttributes) {
        User user = UserUtils.getUser();
        if (user.getId() != null) {
            mdErrorCode.setCreateById(user.getId());
            mdErrorCode.setUpdateById(user.getId());
            mdErrorCode.setCreateDate(new Date());
            mdErrorCode.setUpdateDate(new Date());
            mdErrorCode.setCode(SeqUtils.NextSequenceNo("ErrorCode"));
            customerErrorCodeService.save(mdErrorCode);
        } else {
            addMessage(redirectAttributes, "当前用户不存在");
        }
        return "redirect:" + adminPath + "/provider/md/customerErrorCode/findList?productId="+mdErrorCode.getProductId() + "&customerId=" + mdErrorCode.getCustomerId()+"&errorTypeId="+mdErrorCode.getErrorTypeId();
    }

    @RequiresPermissions("md:customererroraction:edit")
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
        return "redirect:" + adminPath + "/provider/md/customerErrorCode/findList?productId="+mdErrorCode.getProductId() +"&customerId=" +mdErrorCode.getCustomerId();
    }

    @ResponseBody
    @RequestMapping(value={"checkName"})
    public String checkName(Long customerId,Long productId, Long errorTypeId, String name) {
        User user = UserUtils.getUser();
        if (user == null || user.getId() == null) {
            return "false";
        }
        if(customerId==null || customerId<=0){
            return "请选中客户";
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
            mdErrorCode.setCustomerId(customerId);
            String result = msErrorCodeService.checkName(mdErrorCode);
            return result.equalsIgnoreCase("true") ? result : "该客户故障分类已存在.";
        } catch (Exception ex) {
            log.error("error,", ex);
            return "false";
        }
    }

    /**
     * 根据客户id+产品id+故障类型id读取故障现象
     * */
    @ResponseBody
    @RequestMapping(value={"ajax/findListByProductIdAndErrorTypeId"})
    public AjaxJsonEntity findListByProductIdAndErrorTypeId(Long customerId,Long productId, Long errorTypeId, HttpServletResponse response) {
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity jsonEntity=new AjaxJsonEntity();
        jsonEntity.setSuccess(true);
        try{
            List<MDErrorCode> mdErrorCodeList = customerErrorCodeService.findListByProductAndErrorType(errorTypeId,productId,customerId);
            jsonEntity.setSuccess(true);
            jsonEntity.setData(mdErrorCodeList);
        }catch (Exception e){
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage(e.getMessage());
        }
        return jsonEntity;
    }

}
