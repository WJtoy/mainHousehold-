package com.wolfking.jeesite.modules.md.web;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.md.CustomerProductModel;
import com.kkl.kklplus.entity.md.MDCustomerMaterial;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.*;
import com.wolfking.jeesite.modules.md.service.*;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providermd.service.MSMaterialService;
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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "${adminPath}/md/customerMaterialNew")
public class CustomerMaterialNewController extends BaseController {

    @Autowired
    private CustomerMaterialService customerMaterialService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ProductService productService;

    @Autowired
    private MaterialService materialService;

    @Autowired
    private MSMaterialService msMaterialService;

    @Autowired
    private ProductCategoryService productCategoryService;

    @Autowired
    private ProductModelService productModelService;

    @Autowired
    private MaterialCategoryService materialCategoryService;

    /**
     * 分页查询
     *
     * @param customerMaterial
     * @return
     */
    @RequiresPermissions("md:customermaterial:view")
    @RequestMapping(value = {"list", ""})
    public String list(CustomerMaterial customerMaterial, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<CustomerMaterial> page = new Page<>();
        User user = UserUtils.getUser();
        Boolean errorFlag = false;
        List<ProductCategory> productCategoryList = productCategoryService.findAllList();
        List<Product> productList = productService.findAllList();
        List<Material> materialList = materialService.findAllList();
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
                return "modules/md/customerMaterialNewList";
            }
        }
        if (customerMaterial == null || customerMaterial.getCustomer() == null || customerMaterial.getCustomer().getId() == null || customerMaterial.getCustomer().getId() == 0) {
            model.addAttribute("page", page);
            model.addAttribute("customerMaterial", customerMaterial);
            model.addAttribute("productList", productList);
            model.addAttribute("materialList", materialList);
            model.addAttribute("productCategoryList", productCategoryList);
            return "modules/md/customerMaterialNewList";
        }
        productList = productService.getCustomerProductList(customerMaterial.getCustomer().getId());
        if (customerMaterial.getProductCategoryId() != null) {
            productList = productService.ProductByCategoryIdFindAllList(customerMaterial.getCustomer().getId(), customerMaterial.getProductCategoryId());
        }

        page = customerMaterialService.findAllPage(new Page<>(request, response), customerMaterial);
        productCategoryList = productCategoryService.findIdAndNameListByCustomerId(customerMaterial.getCustomer().getId());

        Set<Long> productIds = page.getList().stream().map(r -> r.getProduct().getId()).collect(Collectors.toSet());
        Map<Long, Product> productMap = productService.getProductMap(Lists.newArrayList(productIds));
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
        return "modules/md/customerMaterialNewList";
    }

    @RequiresPermissions("md:customermaterial:view")
    @RequestMapping("form")
    public String form(CustomerMaterial customerMaterial, Model model) {
        User user = UserUtils.getUser();
        List<ProductCategory> productCategoryList = productCategoryService.findAllList();
        List<Product> productList = Lists.newArrayList();
        Long productCategoryId = customerMaterial.getProductCategoryId();
        if (user.isCustomer()) {
            if (user.getCustomerAccountProfile() != null && user.getCustomerAccountProfile().getCustomer() != null) {
                //登录用户的客户，防篡改
                customerMaterial.setCustomer(user.getCustomerAccountProfile().getCustomer());
                productCategoryList = productCategoryService.findIdAndNameListByCustomerId(customerMaterial.getCustomer().getId());
            } else {
                addMessage(model, "错误：登录超时，请退出后重新登录。");
                model.addAttribute("customerMaterial", customerMaterial);
                return "modules/md/customerMaterialNewForm";
            }
        }
        if (customerMaterial.getId() != null && customerMaterial.getId() > 0) {
            customerMaterial = customerMaterialService.get(customerMaterial.getId());
            if (customerMaterial != null) {
                Customer customer = customerService.getFromCache(customerMaterial.getCustomer().getId());
                if (customer != null) {
                    customerMaterial.getCustomer().setName(customer.getName());
                }
                Product product = productService.getProductByIdFromCache(customerMaterial.getProduct().getId());
                if (product != null) {
                    customerMaterial.getProduct().setName(product.getName());
                }
                Material material = materialService.getFromCache(customerMaterial.getMaterial().getId());
                if (material != null) {
                    customerMaterial.getMaterial().setName(material.getName());
                }
                productCategoryList = productCategoryService.findIdAndNameListByCustomerId(customerMaterial.getCustomer().getId());
                productList = productService.getCustomerProductList(customerMaterial.getCustomer().getId());
                if (productCategoryId != null) {
                    productList = productService.ProductByCategoryIdFindAllList(customerMaterial.getCustomer().getId(), productCategoryId);
                }
            }
        }
        model.addAttribute("customerMaterial", customerMaterial);
        model.addAttribute("productCategoryList", productCategoryList);
        model.addAttribute("productList", productList);
        return "modules/md/customerMaterialNewForm";
    }


    /**
     * 添加或者修改
     *
     * @param customerMaterial
     * @return
     */
    @RequiresPermissions("md:customermaterial:edit")
    @RequestMapping("save")
    @ResponseBody
    public AjaxJsonEntity save(CustomerMaterial customerMaterial) {
        AjaxJsonEntity jsonEntity = new AjaxJsonEntity(true);
        try {
            customerMaterialService.save(customerMaterial);
            jsonEntity.setMessage("配件信息已保存");
            jsonEntity.setSuccess(true);
        } catch (Exception e) {
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
    @RequiresPermissions("md:customermaterial:edit")
    @RequestMapping(value = "delete")
    public String delete(CustomerMaterial customerMaterial, RedirectAttributes redirectAttributes) {
        customerMaterialService.delete(customerMaterial);
        addMessage(redirectAttributes, "删除配件成功");
        return "redirect:" + adminPath + "/md/customerMaterialNew/listNew?repage&customer.id=" + customerMaterial.getCustomer().getId();
    }


    @RequiresPermissions("md:customermaterial:edit")
    @RequestMapping("ajax/delete")
    @ResponseBody
    public AjaxJsonEntity delete(CustomerMaterial customerMaterial, HttpServletResponse response) {
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity jsonEntity = new AjaxJsonEntity(true);
        try {
            customerMaterialService.delete(customerMaterial);
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
    public AjaxJsonEntity getMaterialListByProductId(Long productId, Long customerId, HttpServletResponse response) {
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity jsonEntity = new AjaxJsonEntity(true);
        try {
            Map<String, Object> map = Maps.newHashMap();
            List<Material> list = materialService.getMaterialListByProductId(productId);
            if (list != null && list.size() > 0) {
                jsonEntity.setSuccess(true);
            } else {
                list = Lists.newArrayList();
            }
            map.put("materialList", list);
            List<CustomerMaterial> customerMaterialList = customerMaterialService.getByCustomerAndProduct(customerId, productId);
            map.put("customerMaterialList", customerMaterialList);
            jsonEntity.setData(map);
        } catch (Exception e) {
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
    public AjaxJsonEntity getMaterial(Long materialId, HttpServletResponse response) {
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity jsonEntity = new AjaxJsonEntity(true);
        try {
            Material material = materialService.getFromCache(materialId);
            if (material == null) {
                material = new Material();
            }
            jsonEntity.setSuccess(true);
            jsonEntity.setData(material);
        } catch (Exception e) {
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
            List<Product> list = productService.getCustomerProductList(customerId);
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
            List<ProductCategory> list = productCategoryService.findIdAndNameListByCustomerId(customerId);
            if (list != null && list.size() > 0) {
                jsonEntity.setSuccess(true);
                jsonEntity.setData(list);
                return jsonEntity;
            } else {
                return jsonEntity;
            }
        } else {
            List<ProductCategory> list = productCategoryService.findAllList();
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
                List<Product> list = productService.ProductByCategoryIdFindAllList(customerId, productCategoryId);
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


//	/**
//	 *获取MS产品配件列表
//	 * @return
//	 */
//	@ResponseBody
//	@RequestMapping(value = "ajax/getMsCustomerProductMaterial")
//	public AjaxJsonEntity getMsCustomerProductMaterial(Long customerId,Long productId){
//		AjaxJsonEntity jsonEntity=new AjaxJsonEntity();
//		jsonEntity.setSuccess(false);
//		if(customerId != null && customerId > 0){
//			if (productId != null && productId > 0){
//				List<String> list = b2BProductMappingService.getB2BProduct69Code(customerId,productId);
//				if(list == null || list.size() > 1) {
//					jsonEntity.setSuccess(false);
//					jsonEntity.setMessage("产品code获取失败,请核对产品信息");
//				}else {
//					if(list.get(0) != null){
//						List<ProductParts> productParts = msMaterialService.getProductParts(list.get(0));
//						jsonEntity.setSuccess(true);
//						jsonEntity.setData(productParts);
//					}
//				}
//			}
//		}
//		return jsonEntity;
//	}
    //==================================================================================================================

    /**
     * 分页查询
     *
     * @param customerMaterial
     * @return
     */
    @RequiresPermissions("md:customermaterial:view")
    @RequestMapping(value = {"listNew"})
    public String listNew(CustomerMaterial customerMaterial, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<CustomerMaterial> page = new Page<>(request, response);
        User user = UserUtils.getUser();
        Boolean errorFlag = false;
        List<ProductCategory> productCategoryList = productCategoryService.findAllList();
        List<Product> productList = productService.findAllList();
        List<Material> materialList = materialService.findAllList();
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
                return "modules/md/customerMaterialListVerSecond";
            }
        }
        if (customerMaterial == null || customerMaterial.getCustomer() == null || customerMaterial.getCustomer().getId() == null || customerMaterial.getCustomer().getId() == 0) {
            model.addAttribute("page", page);
            model.addAttribute("customerMaterial", customerMaterial);
            model.addAttribute("productList", productList);
            model.addAttribute("materialList", materialList);
            model.addAttribute("productCategoryList", productCategoryList);
            return "modules/md/customerMaterialListVerSecond";
        }
        productList = productService.getCustomerProductList(customerMaterial.getCustomer().getId());
        if (customerMaterial.getProductCategoryId() != null) {
            productList = productService.ProductByCategoryIdFindAllList(customerMaterial.getCustomer().getId(), customerMaterial.getProductCategoryId());
        }

        page = customerMaterialService.findAllPage(new Page<>(request, response), customerMaterial);
        productCategoryList = productCategoryService.findIdAndNameListByCustomerId(customerMaterial.getCustomer().getId());

        Set<Long> productIds = page.getList().stream().map(r -> r.getProduct().getId()).collect(Collectors.toSet());
        Map<Long, Product> productMap = productService.getProductMap(Lists.newArrayList(productIds));
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
        if(customerMaterial.getProduct() != null && customerMaterial.getProduct().getId() != null){
            List<CustomerProductModel> customerProductModelList = productModelService.findListByCustomerAndProduct(customerMaterial.getCustomer().getId(), customerMaterial.getProduct().getId());
            model.addAttribute("customerProductModelList", customerProductModelList);
        }

        model.addAttribute("materialList", materialList);
        model.addAttribute("page", page);
        model.addAttribute("customerMaterial", customerMaterial);
        model.addAttribute("productList", productList);
        model.addAttribute("productCategoryList", productCategoryList);
        return "modules/md/customerMaterialListVerSecond";
    }

    @RequiresPermissions("md:customermaterial:edit")
    @RequestMapping("updateMaterialList")
    public String updateCustomerMaterialList(CustomerMaterial customerMaterial, HttpServletRequest request, HttpServletResponse response, Model model) {
        //
        Page<CustomerMaterial> page = new Page<>();
        User user = UserUtils.getUser();
        Boolean errorFlag = false;
        List<ProductCategory> productCategoryList = productCategoryService.findAllList();
        List<Product> productList = productService.findAllList();
        List<Material> materialList = materialService.findAllList();
        if (user.isCustomer()) {
            if (user.getCustomerAccountProfile() != null && user.getCustomerAccountProfile().getCustomer() != null) {
                //登录用户的客户，防篡改
                customerMaterial.setCustomer(user.getCustomerAccountProfile().getCustomer());
            } else {
                addMessage(model, "错误：登录超时，请退出后重新登录。");
                errorFlag = true;
            }
        }

        Long customerId = customerMaterial.getCustomer().getId();
        Long productId = customerMaterial.getProduct().getId();
        Long customerProductModelId = customerMaterial.getCustomerProductModelId();
        List<CustomerProductModel> customerProductModelList = productModelService.findListByCustomerAndProduct(customerId, productId);
        MSResponse<CustomerProductModel> msResponse = productModelService.getById(customerProductModelId);
        CustomerProductModel customerProductModel = msResponse.getData();
        if (StringUtils.isEmpty(customerProductModel.getCustomerModelId())) {
            addMessage(model, "客户的产品Id(69码)不存在.");
            errorFlag = true;
        }

        if (errorFlag) {
            if (errorFlag) {
                model.addAttribute("page", page);
                model.addAttribute("customerMaterial", customerMaterial);
                model.addAttribute("productCategoryList", productCategoryList);
                model.addAttribute("customerProductModelList", customerProductModelList);
                return "modules/md/customerMaterialListVerSecond";
            }
        }
        if (customerMaterial == null || customerMaterial.getCustomer() == null || customerMaterial.getCustomer().getId() == null || customerMaterial.getCustomer().getId() == 0) {
            model.addAttribute("page", page);
            model.addAttribute("customerMaterial", customerMaterial);
            model.addAttribute("productList", productList);
            model.addAttribute("materialList", materialList);
            model.addAttribute("productCategoryList", productCategoryList);
            model.addAttribute("customerProductModelList", customerProductModelList);
            return "modules/md/customerMaterialListVerSecond";
        }
        productList = productService.getCustomerProductList(customerMaterial.getCustomer().getId());
        if (customerMaterial.getProductCategoryId() != null) {
            productList = productService.ProductByCategoryIdFindAllList(customerMaterial.getCustomer().getId(), customerMaterial.getProductCategoryId());
        }

        String strReturnMsg = customerMaterialService.updateMaterialList(customerId, productId, customerProductModelId, customerProductModel.getCustomerModelId());

        page = customerMaterialService.findAllPage(new Page<>(request, response), customerMaterial);
        productCategoryList = productCategoryService.findIdAndNameListByCustomerId(customerMaterial.getCustomer().getId());

        Set<Long> productIds = page.getList().stream().map(r -> r.getProduct().getId()).collect(Collectors.toSet());
        Map<Long, Product> productMap = productService.getProductMap(Lists.newArrayList(productIds));
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
        model.addAttribute("customerProductModelList", customerProductModelList);
        addMessage(model, strReturnMsg);

        return "modules/md/customerMaterialListVerSecond";
    }

    @RequiresPermissions("md:customermaterial:view")
    @RequestMapping("formNew")
    public String formNew(CustomerMaterial customerMaterial, Model model) {
        User user = UserUtils.getUser();
        List<ProductCategory> productCategoryList = productCategoryService.findAllList();
        List<Product> productList = Lists.newArrayList();
        Long productCategoryId = customerMaterial.getProductCategoryId();
        if (user.isCustomer()) {
            if (user.getCustomerAccountProfile() != null && user.getCustomerAccountProfile().getCustomer() != null) {
                //登录用户的客户，防篡改
                customerMaterial.setCustomer(user.getCustomerAccountProfile().getCustomer());
                productCategoryList = productCategoryService.findIdAndNameListByCustomerId(customerMaterial.getCustomer().getId());
            } else {
                addMessage(model, "错误：登录超时，请退出后重新登录。");
                model.addAttribute("customerMaterial", customerMaterial);
                return "modules/md/customerMaterialFormVerSecond";
            }
        }
        if (customerMaterial.getId() != null && customerMaterial.getId() > 0) {
            customerMaterial = customerMaterialService.get(customerMaterial.getId());
            if (customerMaterial != null) {
                Customer customer = customerService.getFromCache(customerMaterial.getCustomer().getId());
                if (customer != null) {
                    customerMaterial.getCustomer().setName(customer.getName());
                }
                Product product = productService.getProductByIdFromCache(customerMaterial.getProduct().getId());
                if (product != null) {
                    customerMaterial.getProduct().setName(product.getName());
                }
                if(customerMaterial.getMaterial().getId() != null){
                    Material material = materialService.get(customerMaterial.getMaterial().getId());
                    if (material != null) {
                        customerMaterial.getMaterial().setName(material.getName());
                    }
                }

                productCategoryList = productCategoryService.findIdAndNameListByCustomerId(customerMaterial.getCustomer().getId());
                productList = productService.getCustomerProductList(customerMaterial.getCustomer().getId());
                if (productCategoryId != null) {
                    productList = productService.ProductByCategoryIdFindAllList(customerMaterial.getCustomer().getId(), productCategoryId);
                }
            }
        }
        model.addAttribute("customerMaterial", customerMaterial);
        model.addAttribute("productCategoryList", productCategoryList);
        model.addAttribute("productList", productList);
        return "modules/md/customerMaterialFormVerSecond";
    }

    /**
     * 添加或者修改
     *
     * @param mdCustomerMaterial
     * @return
     */
    @RequiresPermissions("md:customermaterial:edit")
    @RequestMapping("saveNew")
    @ResponseBody
    public AjaxJsonEntity save(MDCustomerMaterial mdCustomerMaterial ,HttpServletResponse response) {
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity jsonEntity = new AjaxJsonEntity(true);
        try {
            if(mdCustomerMaterial.getRecycleFlag() == 0){
                mdCustomerMaterial.setRecyclePrice(0.0D);
            }
            if(mdCustomerMaterial.getId() == null){
                customerMaterialService.insertCustomerMaterial(mdCustomerMaterial);
            }else {
                customerMaterialService.updateCustomerMaterial(mdCustomerMaterial);
            }
            jsonEntity.setMessage("配件信息已保存");
            jsonEntity.setSuccess(true);
        } catch (Exception e) {
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage(e.getMessage());
        }
        return jsonEntity;
    }
    @RequiresPermissions("md:customermaterial:edit")
    @RequestMapping("showProductMaterial")
    public String showProductMaterial(CustomerMaterial customerMaterial,Integer mark,Model model){
        List<MaterialCategory> materialCategoryList = materialCategoryService.findAllList();
        List<CustomerMaterial> customerMaterialList = customerMaterialService.getByCustomerAndProduct(customerMaterial.getCustomer().getId(), customerMaterial.getProduct().getId());
        model.addAttribute("materialCategoryList",materialCategoryList);
        model.addAttribute("customerMaterial",customerMaterial);
        model.addAttribute("customerMaterialList",customerMaterialList);
        model.addAttribute("mark",mark);
        return "modules/md/customerMaterialRelate";
    }

    /**
     * 根据配件类别ID获取配件列表
     * @param materialCategoryId
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "ajax/getMaterialCategoryFromMaterial")
    public AjaxJsonEntity getMaterialList(Long materialCategoryId){
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(false);
        if (materialCategoryId != null && materialCategoryId > 0) {
            List<Material> list = msMaterialService.findIdAndNameByCategoryId(materialCategoryId);
            ajaxJsonEntity.setData(list);
            ajaxJsonEntity.setSuccess(true);
            return ajaxJsonEntity;
        }
        return ajaxJsonEntity;
    }

    @ResponseBody
    @RequestMapping(value = "ajax/ajaxSaveMaterial")
    public AjaxJsonEntity saveMaterial(CustomerMaterial customerMaterial){
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        try {
            Material material = customerMaterial.getMaterial();
            if(material.getId() == null){
                material.preInsert();
                Long materialId = msMaterialService.saveMaterial(material);
                customerMaterial.getMaterial().setId(materialId);
                ajaxJsonEntity.setData(materialId);
            }else {
                ajaxJsonEntity.setData(material.getId());
            }
            if(customerMaterial.getId() != null){
                customerMaterial.preUpdate();
                MDCustomerMaterial mdCustomerMaterial = new MDCustomerMaterial();
                mdCustomerMaterial.setId(customerMaterial.getId());
                mdCustomerMaterial.setMaterialId(customerMaterial.getMaterialId());
                mdCustomerMaterial.setCustomerId(customerMaterial.getCustomer().getId());
                mdCustomerMaterial.setProductId(customerMaterial.getProduct().getId());
                mdCustomerMaterial.setUpdateDate(customerMaterial.getUpdateDate());
                mdCustomerMaterial.setUpdateById(customerMaterial.getUpdateBy().getId());
                customerMaterialService.updateCustomerMaterialId(mdCustomerMaterial);
                ajaxJsonEntity.setData(customerMaterial.getMaterialId());
            }
        } catch (Exception e) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage(e.getMessage());
        }
        return ajaxJsonEntity;
    }
    /**
     * 检查该配件是否存在
     * @param name
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "ajax/checkMaterialName")
    public AjaxJsonEntity checkMaterialName(String name){
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(false);
        if (name != null && name.length() > 0) {
            Material material = new Material();
            material.setName(name);
            Long id = msMaterialService.getIdByName(material);
            if (id == null || id <= 0) {
                ajaxJsonEntity.setSuccess(true);
                return ajaxJsonEntity;
            }
        }
        return ajaxJsonEntity;
    }

    /**
     * 检查该客户配件是否存在
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "ajax/checkCustomerMaterial")
    public AjaxJsonEntity checkCustomerMaterial(Long id,Long customerId,Long productId,Long materialId){
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(false);
        if (customerId != null && productId != null && materialId != null) {
            Long aLong = customerMaterialService.getIdByCustomerAndProductAndMaterial(customerId, productId, materialId);
            if (aLong == null || aLong <= 0) {
                ajaxJsonEntity.setSuccess(true);
                return ajaxJsonEntity;
            }else if(aLong.equals(id)){
                ajaxJsonEntity.setSuccess(true);
                return ajaxJsonEntity;
            }
        }
        return ajaxJsonEntity;
    }
}
