package com.wolfking.jeesite.ms.providermd.controller;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.md.CustomerBrand;
import com.kkl.kklplus.entity.md.CustomerBrandCategory;
import com.kkl.kklplus.entity.md.CustomerProductModel;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.Brand;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.Product;
import com.wolfking.jeesite.modules.md.service.*;
import com.wolfking.jeesite.modules.md.utils.CustomerUtils;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providermd.service.CustomerBrandCategoryService;
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
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "${adminPath}/provider/md/customerBrandCategory")
public class CustomerBrandCategoryController extends BaseController {

    @Autowired
    private CustomerBrandCategoryService customerBrandCategoryService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ProductService productService;

    @Autowired
    private BrandCategoryService brandCategoryService;

    @Autowired
    private ProductModelService productModelService;

    @Autowired
    private CustomerBrandService customerBrandService;


    /**
     * 分页查询
     *
     * @param customerBrandCategory
     * @return
     */
    /*@RequiresPermissions("md:customerbrandcategory:view")
    @RequestMapping(value = {"getList", ""})
    public String getList(CustomerBrandCategory customerBrandCategory, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<CustomerBrandCategory> page = new Page<>(request, response);
        User user = UserUtils.getUser();
        Boolean erroFlag = false;
        CustomerBrand searchCustomerBrand = new CustomerBrand();
        if(user.isCustomer()){
            if (user.getCustomerAccountProfile() != null && user.getCustomerAccountProfile().getCustomer() != null) {
                //登录用户的客户，防篡改
                customerBrandCategory.setCustomerId(user.getCustomerAccountProfile().getCustomer().getId());
                searchCustomerBrand.setCustomerId(user.getCustomerAccountProfile().getCustomer().getId());
            } else {
                addMessage(model, "错误：登录超时，请退出后重新登录。");
                erroFlag = true;
            }
        }else if(user.isSaleman() && (customerBrandCategory.getCustomerId()==null || customerBrandCategory.getCustomerId()<=0)){
            List<Customer> customers = CustomerUtils.getMyCustomerList();
            if(customers !=null && customers.size()>0){
                customerBrandCategory.setCustomerId(customers.get(0).getId());
            }else{
                customerBrandCategory.setCustomerId(0L);
            }
        }
        if(erroFlag){
            if(erroFlag){
                model.addAttribute("page", page);
                model.addAttribute("customerBrandCategory", customerBrandCategory);
                return "modules/providermd/customerBrandCategoryList";
            }
        }
        page = customerBrandCategoryService.getList(new Page<CustomerBrandCategory>(request, response), customerBrandCategory);
        List<CustomerBrandCategory> list = page.getList();
        for(CustomerBrandCategory entity:list){
            entity.setCustomerName(customerService.getFromCache(entity.getCustomerId()).getName());
        }
        for(CustomerBrandCategory entity:list){
            entity.setProductName(productService.getProductByIdFromCache(entity.getProductId()).getName());
        }
        for(CustomerBrandCategory entity:list){
            CustomerBrand customerBrand = customerBrandService.getById(entity.getBrandId());
            if(customerBrand!=null){
                entity.setBrandName(customerBrand.getBrandName());
            }
        }
        List<CustomerBrand> customerBrandList =customerBrandService.findAllList(searchCustomerBrand);
        model.addAttribute("customerBrandList",customerBrandList);
        model.addAttribute("page", page);
        model.addAttribute("customerBrandCategory", customerBrandCategory);
        return "modules/providermd/customerBrandCategoryList";
    }*/

    /**
     * 分页查询
     *
     * @param customerBrandCategory
     * @return
     */
    @RequiresPermissions("md:customerbrandcategory:view")
    @RequestMapping(value = {"getList", ""})
    public String getList(CustomerBrandCategory customerBrandCategory, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<CustomerBrandCategory> page = new Page<>(request, response);
        User user = UserUtils.getUser();
        Boolean erroFlag = false;
        CustomerBrand searchCustomerBrand = new CustomerBrand();

        if(user.isCustomer()){
            if (user.getCustomerAccountProfile() != null && user.getCustomerAccountProfile().getCustomer() != null) {
                //登录用户的客户，防篡改
                customerBrandCategory.setCustomerId(user.getCustomerAccountProfile().getCustomer().getId());
                searchCustomerBrand.setCustomerId(user.getCustomerAccountProfile().getCustomer().getId());
            } else {
                addMessage(model, "错误：登录超时，请退出后重新登录。");
                erroFlag = true;
            }
        }else if(user.isSaleman() && (customerBrandCategory.getCustomerId()==null || customerBrandCategory.getCustomerId()<=0)){
            List<Customer> customers = CustomerUtils.getMyCustomerList();
            if(customers !=null && customers.size()>0){
                customerBrandCategory.setCustomerId(customers.get(0).getId());
            }else{
                customerBrandCategory.setCustomerId(0L);
            }
        }else {
            if (customerBrandCategory.getCustomerId() != null && customerBrandCategory.getCustomerId() > 0) {
                searchCustomerBrand.setCustomerId(customerBrandCategory.getCustomerId());
            }
        }
        if (customerBrandCategory.getCustomerId() == null || customerBrandCategory.getCustomerId() <= 0) {
            addMessage(model, "请选择客户");
            return "modules/providermd/customerBrandCategoryListNew";
        }
        if(erroFlag){
            if(erroFlag){
                model.addAttribute("page", page);
                model.addAttribute("customerBrandCategory", customerBrandCategory);
                return "modules/providermd/customerBrandCategoryListNew";
            }
        }
        page = customerBrandCategoryService.getList(new Page<CustomerBrandCategory>(request, response), customerBrandCategory);
        List<CustomerBrandCategory> list = page.getList();
        for(CustomerBrandCategory entity:list){
            entity.setCustomerName(customerService.getFromCache(entity.getCustomerId()).getName());
        }
        Product product;
        for(CustomerBrandCategory entity:list){
            product = productService.getProductByIdFromCache(entity.getProductId());
            if(product != null){
                entity.setProductName(product.getName());
            }
        }
        for(CustomerBrandCategory entity:list){
            CustomerBrand customerBrand = customerBrandService.getById(entity.getBrandId());
            if(customerBrand!=null){
                entity.setBrandName(customerBrand.getBrandName());
            }
        }
        List<CustomerBrand> customerBrandList =customerBrandService.findAllList(searchCustomerBrand);
        model.addAttribute("customerBrandList",customerBrandList);
        model.addAttribute("page", page);
        model.addAttribute("customerBrandCategory", customerBrandCategory);
        return "modules/providermd/customerBrandCategoryListNew";
    }

    /*@RequiresPermissions("md:customerbrandcategory:edit")
    @RequestMapping(value = "form")
    public String form(CustomerBrandCategory customerBrandCategory, Model model) {
        String viewModel = "modules/providermd/customerBrandCategoryForm";

        User user = UserUtils.getUser();
        if(user.isCustomer()){
            if (user.getCustomerAccountProfile() != null && user.getCustomerAccountProfile().getCustomer() != null) {
                //登录用户的客户，防篡改
                customerBrandCategory.setCustomerId(user.getCustomerAccountProfile().getCustomer().getId());
            } else {
                addMessage(model, "错误：登录超时，请退出后重新登录。");
                model.addAttribute("canAction", false);
                model.addAttribute("customerBrandCategory", customerBrandCategory);
                return viewModel;
            }
        }
       if (customerBrandCategory.getCustomerId() != null &&customerBrandCategory.getCustomerId() > 0){
           List<Product> products =productService.getCustomerProductList(customerBrandCategory.getCustomerId());
           List<CustomerBrand> customerBrands =customerBrandService.getListByCustomer(customerBrandCategory.getCustomerId());
           if(customerBrandCategory.getBrandId() !=null && customerBrandCategory.getBrandId()>0){
               List<CustomerBrandCategory> list = customerBrandCategoryService.findListByBrand(customerBrandCategory.getCustomerId(),customerBrandCategory.getBrandId());
               String productIds=String.join(",",list.stream().map(t->t.getProductId().toString()).collect(Collectors.toList()));
               customerBrandCategory.setBrandIds(productIds);
           }
           model.addAttribute("products", products);
           model.addAttribute("customerBrands", customerBrands);
           model.addAttribute("customerBrandCategory",customerBrandCategory);
       }
        model.addAttribute("canAction", true);
        return viewModel;
    }*/

    @RequiresPermissions("md:customerbrandcategory:edit")
    @RequestMapping(value = "form")
    public String form(CustomerBrandCategory customerBrandCategory, Model model) {
        String viewModel = "modules/providermd/customerBrandCategoryFormTwo";

        User user = UserUtils.getUser();
        if(user.isCustomer()){
            if (user.getCustomerAccountProfile() != null && user.getCustomerAccountProfile().getCustomer() != null) {
                //登录用户的客户，防篡改
                customerBrandCategory.setCustomerId(user.getCustomerAccountProfile().getCustomer().getId());
            } else {
                addMessage(model, "错误：登录超时，请退出后重新登录。");
                model.addAttribute("canAction", false);
                model.addAttribute("customerBrandCategory", customerBrandCategory);
                return viewModel;
            }
        }
        if (customerBrandCategory.getCustomerId() != null &&customerBrandCategory.getCustomerId() > 0){
            List<Product> products =productService.getCustomerProductList(customerBrandCategory.getCustomerId());
            List<CustomerBrand> customerBrands =customerBrandService.getListByCustomer(customerBrandCategory.getCustomerId());
            if(customerBrandCategory.getBrandId() !=null && customerBrandCategory.getBrandId()>0){
                List<CustomerBrandCategory> list = customerBrandCategoryService.findListByBrand(customerBrandCategory.getCustomerId(),customerBrandCategory.getBrandId());
                String productIds=String.join(",",list.stream().map(t->t.getProductId().toString()).collect(Collectors.toList()));
                customerBrandCategory.setBrandIds(productIds);
            }
            model.addAttribute("products", products);
            model.addAttribute("customerBrands", customerBrands);
            model.addAttribute("customerBrandCategory",customerBrandCategory);
        }
        model.addAttribute("canAction", true);
        return viewModel;
    }


    /**
     * 保存数据(添加或修改)
     *
     * @param customerBrandCategory
     * @return
     */
    /*@RequiresPermissions("md:customerbrandcategory:edit")
    @RequestMapping("save")
    public String save(CustomerBrandCategory customerBrandCategory, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        if (!beanValidator(model, customerBrandCategory)) {
            return form(customerBrandCategory, model);
        }
        User user = UserUtils.getUser();
        if (user.getId() != null) {
            customerBrandCategory.setCreateById(user.getId());
            customerBrandCategory.setUpdateById(user.getId());
            MSErrorCode mSResponse = customerBrandCategoryService.save(customerBrandCategory);
            if (mSResponse.getCode() == 0) {
                addMessage(redirectAttributes, "保存成功");
            } else {
                addMessage(redirectAttributes, mSResponse.getMsg());
            }
        } else {
            addMessage(redirectAttributes, "当前用户不存在");
        }
        if(user.isSaleman()){
            return "redirect:" + adminPath + "/provider/md/customerBrandCategory/getList?repage&customerId=" + customerBrandCategory.getCustomerId();
        }
        return "redirect:" + adminPath + "/provider/md/customerBrandCategory/getList?repage";
    }*/
    @RequiresPermissions("md:customerbrandcategory:edit")
    @RequestMapping("save")
    @ResponseBody
    public AjaxJsonEntity save(CustomerBrandCategory customerBrandCategory, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        User user = UserUtils.getUser();
        if (user.getId() != null) {
            customerBrandCategory.setCreateById(user.getId());
            customerBrandCategory.setUpdateById(user.getId());
            MSErrorCode mSResponse = customerBrandCategoryService.save(customerBrandCategory);
            if (mSResponse.getCode() == 0) {
                ajaxJsonEntity.setMessage("保存成功");
//                addMessage(redirectAttributes, "保存成功");
            } else {
                ajaxJsonEntity.setSuccess(false);
                ajaxJsonEntity.setMessage("保存失败，原因：" + mSResponse.getMsg());
//                addMessage(redirectAttributes, mSResponse.getMsg());
            }
        } else {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("当前用户不存在");
//            addMessage(redirectAttributes, "当前用户不存在");
        }
        return ajaxJsonEntity;
    }

    /**
     * 删除
     * @param entity
     * @return
     */
    /*@RequiresPermissions("md:customerbrandcategory:edit")
    @RequestMapping(value = "delete")
    public String delete(CustomerBrandCategory entity, RedirectAttributes redirectAttributes) {
        List<CustomerProductModel> list = productModelService.getListFromCacheByField(entity.getCustomerId(),entity.getProductId(),entity.getBrandId());
        if(list!=null && list.size()>0){
            addMessage(redirectAttributes, "该品牌已经关联产品型号,不允许删除");
            return "redirect:" + adminPath + "/provider/md/customerBrandCategory/getList?repage";
        }
        MSResponse<Integer> msResponse = customerBrandCategoryService.delete(entity);
        if (msResponse.getCode() == 0) {
            addMessage(redirectAttributes, "删除成功");
        } else {
            addMessage(redirectAttributes, msResponse.getMsg());
        }
        return "redirect:" + adminPath + "/provider/md/customerBrandCategory/getList?repage";
    }*/

    @RequiresPermissions("md:customerbrandcategory:edit")
    @RequestMapping(value = "delete")
    @ResponseBody
    public AjaxJsonEntity delete(CustomerBrandCategory entity, RedirectAttributes redirectAttributes) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        List<CustomerProductModel> list = productModelService.getListFromCacheByField(entity.getCustomerId(),entity.getProductId(),entity.getBrandId());
        if(list!=null && list.size()>0){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("该品牌已经关联产品型号,不允许删除");
            return ajaxJsonEntity;
        }
        MSResponse<Integer> msResponse = customerBrandCategoryService.delete(entity);
        if (msResponse.getCode() == 0) {
            ajaxJsonEntity.setMessage("删除成功");
        } else {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("删除失败");
        }
        return ajaxJsonEntity;
    }

    /**
     * 通过产品Id获取品牌
     * @param customerBrandCategory
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "ajax/getBrandListByCategory")
    public AjaxJsonEntity getBrandListByCustomer(CustomerBrandCategory customerBrandCategory,HttpServletResponse response)
    {
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity jsonEntity=new AjaxJsonEntity();
        jsonEntity.setSuccess(true);
        try{
            List<Brand> brandList = new ArrayList<>();
            String brandIds="";
            Map<String,Object> map = new HashMap<>();
            Product product = productService.getProductByIdFromCache(customerBrandCategory.getProductId());
            if (product !=null && product.getCategory()!=null && product.getCategory().getId() != null && product.getCategory().getId() > 0){
                brandList=brandCategoryService.getBrandListByCategory(product.getCategory().getId());
                List<CustomerBrandCategory> brandSelectedList=customerBrandCategoryService.getFromCache(customerBrandCategory.getCustomerId(),customerBrandCategory.getProductId());
                brandIds=String.join(",",brandSelectedList.stream().map(t->t.getBrandId().toString()).collect(Collectors.toList()));
                map.put("brandList",brandList);
                map.put("brandIds",brandIds);
                jsonEntity.setSuccess(true);
                jsonEntity.setData(map);
            }else{
                map.put("brandList",brandList);
                map.put("brandIds",brandIds);
                jsonEntity.setSuccess(true);
                jsonEntity.setData(map);
            }
        }catch (Exception e){
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage(e.getMessage());
        }
        return jsonEntity;
    }


    /**
     * 通过客户ID 和产品ID获取品牌列表
     * @param customerId,productId
     * @return
     */
     @ResponseBody
     @RequestMapping(value = "ajax/getListByCustomerId")
    public AjaxJsonEntity getListByCustomerId(Long customerId,Long productId,HttpServletResponse response){
         response.setContentType("application/json; charset=UTF-8");
         AjaxJsonEntity jsonEntity=new AjaxJsonEntity();
         jsonEntity.setSuccess(true);
         try {
             List<CustomerBrandCategory> list = customerBrandCategoryService.getFromCache(customerId,productId);
             jsonEntity.setSuccess(true);
             jsonEntity.setData(list);
         }catch (Exception e){
             jsonEntity.setSuccess(false);
             jsonEntity.setMessage(e.getMessage());
         }
         return jsonEntity;
     }

    /**
     * 通过客户获取
     * @param customerId
     * @return
     */
    @ResponseBody
    @RequestMapping("ajax/getListByCustomer")
    public AjaxJsonEntity getListByCustomer(Long customerId,HttpServletResponse response){
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity jsonEntity=new AjaxJsonEntity();
        jsonEntity.setSuccess(true);
        try{
            Map<String,Object> map = Maps.newHashMap();
            List<CustomerBrand> list = customerBrandService.getListByCustomer(customerId);
            List<Product> products = productService.getCustomerProductList(customerId);
            if(list!=null && list.size()>0){
                map.put("customerBrands",list);
            }else{
                map.put("customerBrands",Lists.newArrayList());
            }
            if(products!=null && products.size()>0){
                map.put("products",products);
            }else{
                map.put("products",Lists.newArrayList());
            }
            jsonEntity.setSuccess(true);
            jsonEntity.setData(map);
        }catch (Exception e){
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage(e.getMessage());
        }
        return jsonEntity;
    }

    /**
     * 通过客户,客户品牌获取
     * @param customerId,brandId
     * @return
     */
    @ResponseBody
    @RequestMapping("ajax/findListByBrand")
    public AjaxJsonEntity findListByBrand(Long customerId,Long brandId,HttpServletResponse response){
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity jsonEntity=new AjaxJsonEntity();
        jsonEntity.setSuccess(true);
        try{
            List<CustomerBrandCategory> list = customerBrandCategoryService.findListByBrand(customerId,brandId);
            jsonEntity.setSuccess(true);
            jsonEntity.setData(list);
        }catch (Exception e){
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage(e.getMessage());
        }
        return jsonEntity;
    }

}
