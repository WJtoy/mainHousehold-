package com.wolfking.jeesite.modules.customer.md.web;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.netflix.discovery.converters.Auto;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.customer.md.service.CtCustomerMaterialService;
import com.wolfking.jeesite.modules.customer.md.service.CtMaterialService;
import com.wolfking.jeesite.modules.md.entity.*;
import com.wolfking.jeesite.modules.md.service.ProductService;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providermd.service.*;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@Controller
@Slf4j
@RequestMapping(value = "${adminPath}/customer/md/customerMaterial")
public class CtCustomerMaterialController extends BaseController {

    @Autowired
    private MSCustomerMaterialService msCustomerMaterialService;

    @Autowired
    private MSMaterialService msMaterialService;

    @Autowired
    private MSCustomerService msCustomerService;

    @Autowired
    private MSProductService msProductService;

    @Autowired
    private CtCustomerMaterialService ctCustomerMaterialService;

    @Autowired
    private CtMaterialService ctMaterialService;

    @Autowired
    private MSProductCategoryNewService msProductCategoryNewService;

    @Autowired
    private MSCustomerProductService msCustomerProductService;

    @Autowired
    private ProductService productService;



    /**
     * 分页查询
     *
     * @param customerMaterial
     * @return
     */
    @RequiresPermissions("customer:md:customermaterial:view")
    @RequestMapping(value = { "list", "" })
    public String list(CustomerMaterial customerMaterial, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<CustomerMaterial> page = new Page<>();
        User user = UserUtils.getUser();
        Boolean errorFlag = false;
        List<ProductCategory> productCategoryList = msProductCategoryNewService.findAllListForMDWithEntity();
        List<Product> productList = msProductService.findAllList();
        List<Material> materialList = msMaterialService.findAllList();

        if (user.isCustomer()) {
            if (user.getCustomerAccountProfile() != null && user.getCustomerAccountProfile().getCustomer() != null) {
                //登录用户的客户，防篡改
                customerMaterial.setCustomer(user.getCustomerAccountProfile().getCustomer());
            } else {
                addMessage(model, "错误：登录超时，请退出后重新登录。");
                errorFlag = true;
            }
        }
        if (errorFlag) {
            if (errorFlag) {
                model.addAttribute("page", page);
                model.addAttribute("customerMaterial", customerMaterial);
                model.addAttribute("productCategoryList", productCategoryList);
                return "modules/customer/md/ctCustomerMaterialList";
            }
        }
        if (customerMaterial.getCustomer() == null || customerMaterial.getCustomer().getId() == null ||  customerMaterial.getCustomer().getId()<= 0) {
            addMessage(model, "请选择客户");
            return "modules/customer/md/ctCustomerMaterialList";
        }
        if (customerMaterial == null || customerMaterial.getCustomer() == null || customerMaterial.getCustomer().getId() == null || customerMaterial.getCustomer().getId() == 0) {
            model.addAttribute("page", page);
            model.addAttribute("customerMaterial", customerMaterial);
            model.addAttribute("productList", productList);
            model.addAttribute("materialList", materialList);
            model.addAttribute("productCategoryList", productCategoryList);
            return "modules/customer/md/ctCustomerMaterialList";
        }
        productList = msCustomerProductService.findProductByCustomerIdFromCache(customerMaterial.getCustomer().getId());
        if (customerMaterial.getProductCategoryId() != null) {
            productList = msProductService.findListByCustomerIdAndCategoryId(customerMaterial.getCustomer().getId(), customerMaterial.getProductCategoryId());
        }

        page = msCustomerMaterialService.findList(new Page<>(request, response), customerMaterial);
        productCategoryList = msProductCategoryNewService.findIdAndNameListByCustomerIdWithEntity(customerMaterial.getCustomer().getId());

        Set<Long> productIds = page.getList().stream().map(r -> r.getProduct().getId()).collect(Collectors.toSet());
        Map<Long, Product> productMap = productService.getProductMap(Lists.newArrayList(productIds));  //TODO: 用MSProductService替换
        if (page.getList() != null && page.getList().size() > 0) {
            Map<Long, Material> materialMap = materialList.stream().collect(Collectors.toMap(Material::getId, material -> material));
            Product product;
            for (CustomerMaterial item : page.getList()) {
                product = productMap.get(item.getProduct().getId());
                if (product != null) {
                    item.getProduct().setName(product.getName());
                }
                Material material = materialMap.get(item.getMaterial().getId());
                if (material != null) {
                    item.getMaterial().setName(material.getName());
                }

            }
        }
        model.addAttribute("materialList", materialList);
        model.addAttribute("page", page);
        model.addAttribute("customerMaterial", customerMaterial);
        model.addAttribute("productList", productList);
        model.addAttribute("productCategoryList", productCategoryList);
        return "modules/customer/md/ctCustomerMaterialList";
    }

    @RequiresPermissions("customer:md:customermaterial:view")
    @RequestMapping("form")
    public String form(CustomerMaterial customerMaterial,Model model) {
        User user = UserUtils.getUser();
        List<ProductCategory> productCategoryList = msProductCategoryNewService.findAllListForMDWithEntity();
        List<Product> productList = Lists.newArrayList();
        Long productCategoryId = customerMaterial.getProductCategoryId();
        if (user.isCustomer()) {
            if (user.getCustomerAccountProfile() != null && user.getCustomerAccountProfile().getCustomer() != null) {
                //登录用户的客户，防篡改
                customerMaterial.setCustomer(user.getCustomerAccountProfile().getCustomer());
                productCategoryList = msProductCategoryNewService.findIdAndNameListByCustomerIdWithEntity(customerMaterial.getCustomer().getId());
            } else {
                addMessage(model, "错误：登录超时，请退出后重新登录。");
                model.addAttribute("customerMaterial", customerMaterial);
                return "modules/customer/md/customerMaterialForm";
            }
        }
        if (customerMaterial.getId() != null && customerMaterial.getId() > 0) {
            customerMaterial = msCustomerMaterialService.getById(customerMaterial.getId());
            if (customerMaterial != null) {
                Customer customer = msCustomerService.getFromCache(customerMaterial.getCustomer().getId());
                if (customer != null) {
                    customerMaterial.getCustomer().setName(customer.getName());
                }
                Product product = productService.getProductByIdFromCache(customerMaterial.getProduct().getId());
                if (product != null) {
                    customerMaterial.getProduct().setName(product.getName());
                }
                Material material = msMaterialService.getById(customerMaterial.getMaterial().getId());
                if (material != null) {
                    customerMaterial.getMaterial().setName(material.getName());
                }
                productCategoryList = msProductCategoryNewService.findIdAndNameListByCustomerIdWithEntity(customerMaterial.getCustomer().getId());
                productList = msCustomerProductService.findProductByCustomerIdFromCache(customerMaterial.getCustomer().getId());
                if (productCategoryId != null) {
                    productList = msProductService.findListByCustomerIdAndCategoryId(customerMaterial.getCustomer().getId(), productCategoryId);
                }
            }
        }

        model.addAttribute("customerMaterial", customerMaterial);
        model.addAttribute("productCategoryList", productCategoryList);
        model.addAttribute("productList", productList);
        return "modules/customer/md/ctCustomerMaterialForm";
    }


    /**
     * 添加或者修改
     *
     * @param customerMaterial
     * @return
     */
    @RequiresPermissions("customer:md:customermaterial:edit")
    @RequestMapping("save")
    @ResponseBody
    public AjaxJsonEntity save(CustomerMaterial customerMaterial, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        AjaxJsonEntity jsonEntity = new AjaxJsonEntity(true);
        try {
            ctCustomerMaterialService.save(customerMaterial);
            jsonEntity.setSuccess(true);
        }catch (Exception e){
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage(e.getMessage());
        }
        return jsonEntity;
    }

    /**
     * 删除
     *
     * @param customerMaterial
     * @return
     */
    @RequiresPermissions("customer:md:customermaterial:edit")
    @RequestMapping(value = "delete")
    public String delete(CustomerMaterial customerMaterial, RedirectAttributes redirectAttributes) {
        ctCustomerMaterialService.delete(customerMaterial);
        addMessage(redirectAttributes, "删除配件成功");
        return "redirect:" + adminPath + "/customer/md/customerMaterial/list?repage";
    }

    @RequiresPermissions("customer:md:customermaterial:edit")
    @RequestMapping("ajax/delete")
    @ResponseBody
    public AjaxJsonEntity delete(CustomerMaterial customerMaterial, HttpServletResponse response) {
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity jsonEntity = new AjaxJsonEntity(true);
        try {
            ctCustomerMaterialService.delete(customerMaterial);
            jsonEntity.setMessage("删除成功");
        } catch (Exception e) {
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage(e.getMessage());
        }
        return jsonEntity;
    }

    /**
     * 根据产品Id查询配件列表
     *
     * @param productId
     * @return
     */
    @RequestMapping("ajax/getMaterialListByProductId")
    @ResponseBody
    public AjaxJsonEntity getMaterialListByProductId(Long productId,Long customerId,HttpServletResponse response){
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity jsonEntity  = new AjaxJsonEntity(true);
        try {
            Map<String,Object> map = Maps.newHashMap();
            List<Material> list = ctMaterialService.getMaterialListByProductId(productId);
            if(list !=null && list.size()>0){
                jsonEntity.setSuccess(true);
            }else{
                list = Lists.newArrayList();
            }
            map.put("materialList",list);
            //List<CustomerMaterial> customerMaterialList = customerMaterialService.getByCustomerAndProduct(customerId,productId);  //mark on 2020-6-9
            List<CustomerMaterial> customerMaterialList = msCustomerMaterialService.findListByCustomerAndProduct(customerId,productId);  //add on 2020-6-9
            if (customerMaterialList == null || customerMaterialList.isEmpty()) {
                customerMaterialList = Lists.newArrayList();
            }
            map.put("customerMaterialList",customerMaterialList);
            jsonEntity.setData(map);
        }catch (Exception e){
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage(e.getMessage());
        }
        return jsonEntity;
    }

    /**
     * 根据配件Id获取配件信息
     *
     * @param materialId
     * @return
     */
    @RequestMapping("ajax/getMaterial")
    @ResponseBody
    public AjaxJsonEntity getMaterial(Long materialId,HttpServletResponse response){
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity jsonEntity  = new AjaxJsonEntity(true);
        try {
            //Material material = materialService.getFromCache(materialId);
            Material material = msMaterialService.getById(materialId);
            if(material==null){
                material = new Material();
            }
            jsonEntity.setSuccess(true);
            jsonEntity.setData(material);
        }catch (Exception e){
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage(e.getMessage());
        }
        return jsonEntity;
    }

    /**
     * 根据客户获取产品列表
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "ajax/customerProductList")
    public AjaxJsonEntity getCustomerProductList(Long customerId) {
        AjaxJsonEntity jsonEntity = new AjaxJsonEntity();
        jsonEntity.setSuccess(false);
        if (customerId != null && customerId > 0) {
            List<Product> list = msCustomerProductService.findProductByCustomerIdFromCache(customerId);
            if (list != null && list.size() > 0) {
                jsonEntity.setSuccess(true);
                jsonEntity.setData(list);
                return jsonEntity;
            } else {
                return jsonEntity;
            }
        } else {
            List<Product> list = productService.findAllList();
            jsonEntity.setSuccess(true);
            jsonEntity.setData(list);
            return jsonEntity;
        }

    }

    /**
     * 根据客户获取品类列表
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "ajax/customerProductCategoryList")
    public AjaxJsonEntity getCustomerProductCategoryList(Long customerId) {
        AjaxJsonEntity jsonEntity = new AjaxJsonEntity();
        jsonEntity.setSuccess(false);
        if (customerId != null && customerId > 0) {
            List<ProductCategory> list = msProductCategoryNewService.findIdAndNameListByCustomerIdWithEntity(customerId);
            if (list != null && list.size() > 0) {
                jsonEntity.setSuccess(true);
                jsonEntity.setData(list);
                return jsonEntity;
            } else {
                return jsonEntity;
            }
        } else {
            List<ProductCategory> list = msProductCategoryNewService.findAllListForMDWithEntity();
            jsonEntity.setSuccess(true);
            jsonEntity.setData(list);
            return jsonEntity;
        }
    }

    /**
     * 根据品类获取产品列表
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "ajax/getProductCategoryProductList")
    public AjaxJsonEntity getProductCategoryProductList(Long customerId, Long productCategoryId) {
        AjaxJsonEntity jsonEntity = new AjaxJsonEntity();
        jsonEntity.setSuccess(false);
        if (customerId != null && customerId > 0) {
            if (productCategoryId != null && productCategoryId > 0) {
                List<Product> list = msProductService.findListByCustomerIdAndCategoryId(customerId, productCategoryId);
                if (list != null && list.size() > 0) {
                    jsonEntity.setSuccess(true);
                    jsonEntity.setData(list);
                    return jsonEntity;
                } else {
                    return jsonEntity;
                }
            } else {
                return jsonEntity;
            }
        } else {
            return jsonEntity;
        }
    }

}
