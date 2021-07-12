package com.wolfking.jeesite.ms.providermd.controller;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.md.CustomerBrand;
import com.kkl.kklplus.entity.md.CustomerProductModel;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.Product;
import com.wolfking.jeesite.modules.md.service.CustomerService;
import com.wolfking.jeesite.modules.md.service.ProductService;
import com.wolfking.jeesite.modules.md.utils.CustomerUtils;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providermd.service.CustomerBrandService;
import com.wolfking.jeesite.ms.providermd.service.ProductModelService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 客户产品型号服务
 */
@Controller
@RequestMapping(value = "${adminPath}/provider/md/customerProductModel")
public class ProductModelController extends BaseController {

    @Autowired
    private ProductModelService productModelService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ProductService productService;

    @Autowired
    private CustomerBrandService customerBrandService;

    /**
     * 分页查询
     *
     * @param customerProductModel
     * @return
     */
    @RequiresPermissions("md:customerproductmodel:view")
    @RequestMapping(value = {"getList", ""})
    public String getList(CustomerProductModel customerProductModel, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<CustomerProductModel> page = new Page<>(request, response);
        User user = UserUtils.getUser();
        Boolean erroFlag = false;
        CustomerBrand searchCustomerBrand = new CustomerBrand();
        if(user.isCustomer()){
            if (user.getCustomerAccountProfile() != null && user.getCustomerAccountProfile().getCustomer() != null) {
                //登录用户的客户，防篡改
                customerProductModel.setCustomerId(user.getCustomerAccountProfile().getCustomer().getId());
                searchCustomerBrand.setCustomerId(user.getCustomerAccountProfile().getCustomer().getId());
                customerProductModel.setCustomerId(user.getCustomerAccountProfile().getCustomer().getId());
            } else {
                addMessage(model, "错误：登录超时，请退出后重新登录。");
                erroFlag = true;
            }
        }else if(user.isSaleman() && (customerProductModel.getCustomerId()==null || customerProductModel.getCustomerId()<=0)){
            List<Customer> customers = CustomerUtils.getMyCustomerList();
            if(customers !=null && customers.size()>0){
                customerProductModel.setCustomerId(customers.get(0).getId());
            }else{
                customerProductModel.setCustomerId(0L);
            }
        }else {
            if (customerProductModel.getCustomerId() != null && customerProductModel.getCustomerId() > 0) {
                searchCustomerBrand.setCustomerId(customerProductModel.getCustomerId());
            }
        }
        if (customerProductModel.getCustomerId() == null || customerProductModel.getCustomerId() <= 0) {
            addMessage(model, "请选择客户");
            return "modules/providermd/customerProductModelListNew";
        }
        if(erroFlag){
            model.addAttribute("page", page);
            model.addAttribute("customerProductModel", customerProductModel);
//            return "modules/providermd/customerProductModelList";
            return "modules/providermd/customerProductModelListNew";
        }
        page = productModelService.getList(new Page<CustomerProductModel>(request, response), customerProductModel);
        List<CustomerProductModel> list = page.getList();
        for(CustomerProductModel entity:list){
            entity.setCustomerName(customerService.getFromCache(entity.getCustomerId()).getName());
        }
        for(CustomerProductModel entity:list){
            Product product = productService.getProductByIdFromCache(entity.getProductId());
            if(product!=null){
                entity.setProductName(product.getName());
            }

        }
        for(CustomerProductModel entity:list){
            CustomerBrand customerBrand = customerBrandService.getById(entity.getBrandId());
            if(customerBrand!=null){
                entity.setBrandName(customerBrand.getBrandName());
            }
        }
        List<CustomerBrand> customerBrandList =customerBrandService.findAllList(searchCustomerBrand);
        model.addAttribute("customerBrandList",customerBrandList);
        model.addAttribute("page", page);
        model.addAttribute("customerProductModel", customerProductModel);
//        return "modules/providermd/customerProductModelList";
        return "modules/providermd/customerProductModelListNew";
    }

    @RequiresPermissions("md:customerproductmodel:edit")
    @RequestMapping(value = "form")
    public String form(CustomerProductModel customerProductModel, Model model) {
        CustomerProductModel entity = new CustomerProductModel();
        User user = UserUtils.getUser();
        if(user.isCustomer()){
            if (user.getCustomerAccountProfile() != null && user.getCustomerAccountProfile().getCustomer() != null) {
                //登录用户的客户，防篡改
                entity.setCustomerId(user.getCustomerAccountProfile().getCustomer().getId());
            } else {
                addMessage(model, "错误：登录超时，请退出后重新登录。");
                model.addAttribute("canAction", false);
                model.addAttribute("customerProductModel", entity);
//                return "modules/providermd/customerProductModelForm";
                return "modules/providermd/customerProductModelFormNew";
            }
        }
        if (customerProductModel.getId() != null && customerProductModel.getId() > 0) {
            MSResponse<CustomerProductModel> msResponse = productModelService.getById(customerProductModel.getId());
            entity = msResponse.getData();
            if(entity !=null){
                entity.setCustomerName(customerProductModel.getCustomerName());
                entity.setProductName(customerProductModel.getProductName());
                if(entity.getBrandId()!=null && entity.getBrandId()>0){
                    entity.setBrandName(customerBrandService.getById(entity.getBrandId()).getBrandName());
                }
            }
        }
        model.addAttribute("canAction", true);
        model.addAttribute("customerProductModel", entity);
//        return "modules/providermd/customerProductModelForm";
        return "modules/providermd/customerProductModelFormNew";
    }


    /**
     * 保存数据(添加或修改)
     *
     * @param customerProductModel
     * @return
     */
    /*@RequiresPermissions("md:customerproductmodel:edit")
    @RequestMapping("save")
    public String save(CustomerProductModel customerProductModel, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        if (!beanValidator(model, customerProductModel)) {
            return form(customerProductModel, model);
        }
        User user = UserUtils.getUser();
        if (user.getId() != null) {
            customerProductModel.setCreateById(user.getId());
            customerProductModel.setUpdateById(user.getId());
            MSErrorCode mSResponse = productModelService.save(customerProductModel);
            if (mSResponse.getCode() == 0) {
                addMessage(redirectAttributes, "保存成功");
            } else {
                addMessage(redirectAttributes, mSResponse.getMsg());
            }
        } else {
            addMessage(redirectAttributes, "当前用户不存在");
        }
        if(user.isSaleman()){
            return "redirect:" + adminPath + "/provider/md/customerProductModel/getList?repage&customerId=" + customerProductModel.getCustomerId();
        }
        return "redirect:" + adminPath + "/provider/md/customerProductModel/getList?repage";
    }*/
    @RequiresPermissions("md:customerproductmodel:edit")
    @RequestMapping("save")
    @ResponseBody
    public AjaxJsonEntity save(CustomerProductModel customerProductModel, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        User user = UserUtils.getUser();
        if (user.getId() != null) {
            customerProductModel.setCreateById(user.getId());
            customerProductModel.setUpdateById(user.getId());
            MSErrorCode mSResponse = productModelService.save(customerProductModel);
            if (mSResponse.getCode() == 0) {
                ajaxJsonEntity.setMessage("保存成功");
            } else {
                ajaxJsonEntity.setSuccess(false);
                ajaxJsonEntity.setMessage("保存失败，原因：" + mSResponse.getMsg());
            }
        } else {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("当前用户不存在");
            return ajaxJsonEntity;
        }
        return ajaxJsonEntity;
    }

    /**
     * 删除
     *
     * @param entity
     * @return
     */
    /*@RequiresPermissions("md:customerproductmodel:edit")
    @RequestMapping(value = "delete")
    public String delete(CustomerProductModel entity, RedirectAttributes redirectAttributes) {
        MSResponse<Integer> msResponse = productModelService.delete(entity);
        if (msResponse.getCode() == 0) {
            addMessage(redirectAttributes, "删除成功");
        } else {
            addMessage(redirectAttributes, msResponse.getMsg());
        }
        return "redirect:" + adminPath + "/provider/md/customerProductModel/getList?repage";
    }*/
    @RequiresPermissions("md:customerproductmodel:edit")
    @RequestMapping(value = "delete")
    @ResponseBody
    public AjaxJsonEntity delete(CustomerProductModel entity, RedirectAttributes redirectAttributes) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        MSResponse<Integer> msResponse = productModelService.delete(entity);
        if (msResponse.getCode() == 0) {
            ajaxJsonEntity.setMessage("删除成功");
        } else {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("删除失败,原因：" + msResponse.getMsg());
        }
        return ajaxJsonEntity;
    }


    /**
     *根据客户获取产品列表
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "ajax/customerProductList")
    public AjaxJsonEntity getCustomerProductList(Long customerId)
    {
        AjaxJsonEntity jsonEntity=new AjaxJsonEntity();
        jsonEntity.setSuccess(false);
        if (customerId != null && customerId > 0){
            List<Product> list=productService.getCustomerProductList(customerId);
            if(list!=null && list.size()>0){
                jsonEntity.setSuccess(true);
                jsonEntity.setData(list);
                return jsonEntity;
            }else{
                return jsonEntity;
            }
        }else{
            return jsonEntity;
        }

    }
}
