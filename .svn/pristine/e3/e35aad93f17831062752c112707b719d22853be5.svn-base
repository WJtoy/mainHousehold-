package com.wolfking.jeesite.ms.providermd.controller;

import com.google.common.collect.Lists;
import com.kkl.kklplus.entity.md.MDErrorType;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.Product;
import com.wolfking.jeesite.modules.md.service.ProductService;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.SeqUtils;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providermd.service.MSCustomerErrorTypeService;
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
@RequestMapping(value = "${adminPath}/provider/md/customerErrorType")
@Slf4j
public class CustomerErrorTypeController extends BaseController {
    @Autowired
    private MSCustomerErrorTypeService customerErrorTypeService;

    @Autowired
    private ProductService productService;

    @Autowired
    private MSErrorTypeService msErrorTypeService;

    @RequiresPermissions("md:customererroraction:view")
    @RequestMapping(value={"findList"})
    public String findList(MDErrorType mdErrorType, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<MDErrorType> page;
        if (mdErrorType.getCustomerId()==null || mdErrorType.getCustomerId()<=0 || mdErrorType.getProductId() == null || mdErrorType.getProductId().intValue() ==0) {
            page = new Page<>(request, response);
            page.setPageSize(5);
        } else {
            Page<MDErrorType> initPage = new Page<>(request, response);
            initPage.setPageSize(5);
            page = customerErrorTypeService.findListForPage(initPage, mdErrorType);
        }
        List<Product> productList = Lists.newArrayList();
        if(mdErrorType.getCustomerId()!=null && mdErrorType.getCustomerId()>0){
            productList = productService.getCustomerProductList(mdErrorType.getCustomerId());
        }
        model.addAttribute("errorType", mdErrorType);
        model.addAttribute("productList",productList);
        model.addAttribute("page", page);

        return "modules/providermd/customerErrorTypeList";
    }

    @RequiresPermissions("md:customererroraction:edit")
    @RequestMapping(value={"save"})
    public String save(MDErrorType mdErrorType, HttpServletRequest request, HttpServletResponse response, Model model, RedirectAttributes redirectAttributes) {
        User user = UserUtils.getUser();
        if (user.getId() != null) {
            mdErrorType.setCreateById(user.getId());
            mdErrorType.setUpdateById(user.getId());
            mdErrorType.setCreateDate(new Date());
            mdErrorType.setUpdateDate(new Date());
            mdErrorType.setCode(SeqUtils.NextSequenceNo("ErrorType"));
            customerErrorTypeService.save(mdErrorType);
        } else {
            addMessage(redirectAttributes, "当前用户不存在");
        }
        return "redirect:" + adminPath + "/provider/md/customerErrorType/findList?productId="+mdErrorType.getProductId()+"&customerId="+mdErrorType.getCustomerId();
    }

    @RequiresPermissions("md:customererroraction:edit")
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
        return "redirect:" + adminPath + "/provider/md/customerErrorType/findList?productId="+mdErrorType.getProductId()+"&customerId="+mdErrorType.getCustomerId();
    }

    @ResponseBody
    @RequestMapping(value={"checkName"})
    public String checkName(Long customerId,Long productId, String name) {
        User user = UserUtils.getUser();
        if (user == null || user.getId() == null) {
            return "false";
        }
        if(customerId==null || customerId<=0){
            return "false";
        }
        if (productId == null || productId.intValue()==0) {
            return "false";
        }
        if (StringUtils.isBlank(name)) {
            return "true";
        }
        try {
            String result = msErrorTypeService.checkName(productId, name,customerId);
            return result.equalsIgnoreCase("true") ? result : "该客户故障分类已存在.";
        } catch (Exception ex) {
            log.error("error,", ex);
            return "false";
        }
    }

    @ResponseBody
    @RequestMapping(value={"ajax/findListByCustomerAndProduct"})
    public AjaxJsonEntity findListByProductId(Long customerId,Long productId, HttpServletResponse response) {
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity jsonEntity=new AjaxJsonEntity();
        jsonEntity.setSuccess(true);
        try{
            List<MDErrorType> mdErrorCodeList = customerErrorTypeService.findErrorTypesByProductId(customerId,productId);
            jsonEntity.setSuccess(true);
            jsonEntity.setData(mdErrorCodeList);
        }catch (Exception e){
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage(e.getMessage());
        }
        return jsonEntity;
    }

}
