package com.wolfking.jeesite.modules.md.web;

import com.google.common.collect.Lists;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.*;
import com.wolfking.jeesite.modules.md.service.*;
import com.wolfking.jeesite.modules.md.utils.CustomerUtils;
import com.wolfking.jeesite.modules.md.utils.ProductCompletePicItemMapper;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 客户产品完成图片管理
 */
@Slf4j
@Controller
@RequestMapping(value = "${adminPath}/md/customer/pic")
public class CustomerProductCompletePicController extends BaseController {
    @Autowired
    private CustomerProductCompletePicService customerPicService;

    @Autowired
    private CustomerProductCompletePicNewService customerProductCompletePicNewService;

    @Autowired
    private ProductService productService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ProductCategoryService productCategoryService;
    /**
     * 列表显示
     *
     * @param entity
     * @param request
     * @param response
     * @param model
     * @returno
     */
    @RequiresPermissions("md:customerpic:view")
    @RequestMapping(value = {"list", ""})
    public String list(ProductCompletePic entity, HttpServletRequest request, HttpServletResponse response, Model model) {

        if (entity == null) {
            entity = new ProductCompletePic();
        }
        List<ProductCategory> productCategoryList = productCategoryService.findAllList();
        List<Product> productList = productService.findAllList();
        if(entity.getCustomer() == null || entity.getCustomer().getId() == null){
            model.addAttribute("entity", entity);
            model.addAttribute("productCategoryList", productCategoryList);
            model.addAttribute("productList", productList);
            return "modules/md/customerProductPicNewList";
        }
        User user = UserUtils.getUser();
        boolean errorFlag = false;
        if (entity.getCustomer() == null) {
            entity.setCustomer(new Customer());
        }
        if (user.isCustomer()) {
            if (user.getCustomerAccountProfile() != null && user.getCustomerAccountProfile().getCustomer() != null) {
                entity.getCustomer().setId(user.getCustomerAccountProfile().getCustomer().getId());
                entity.getCustomer().setName(user.getCustomerAccountProfile().getCustomer().getName());
            } else {
                addMessage(model, "错误：登录超时，请退出后重新登录。");
                errorFlag = true;
            }
        } else if (user.isSaleman()) {
            entity.getCustomer().getSales().setId(user.getId());
        }

        if (errorFlag) {
            Page<ProductCompletePic> page = new Page<>();
            model.addAttribute("page", page);
            model.addAttribute("entity", entity);
            model.addAttribute("productCategoryList", productCategoryList);
            model.addAttribute("productList", productList);
            return "modules/md/customerProductPicNewList";
        }
        productList = productService.getCustomerProductList(entity.getCustomer().getId());
        if (entity.getProductCategoryId() != null) {
            productList = productService.ProductByCategoryIdFindAllList(entity.getCustomer().getId(), entity.getProductCategoryId());
        }
        Page<ProductCompletePic> page = customerProductCompletePicNewService.findPage(new Page<>(request, response), entity);
        productCategoryList = productCategoryService.findIdAndNameListByCustomerId(entity.getCustomer().getId());
        model.addAttribute("page", page);
        model.addAttribute("entity", entity);
        model.addAttribute("productCategoryList", productCategoryList);
        model.addAttribute("productList", productList);
        return "modules/md/customerProductPicNewList";
    }

    @RequiresPermissions("md:customerpic:view")
    @RequestMapping(value = "form")
    public String form(ProductCompletePic entity, Model model, HttpServletRequest request) {
        boolean canAction = true;
        String id = request.getParameter("id");
        String productId = request.getParameter("productId");
        String customerId = request.getParameter("customerId");
        String barcodeMustFlag = request.getParameter("barcodeMustFlag");

        List<Customer> customerList = CustomerUtils.getMyCustomerList();

        if (customerList == null || customerList.size() <= 0) {
            customerList = new ArrayList<>();
        }
        if (StringUtils.isNotBlank(id) && StringUtils.isNumeric(id) && StringUtils.isNotBlank(productId)
                && StringUtils.isNumeric(productId) && StringUtils.isNotBlank(customerId) && StringUtils.isNumeric(customerId)) {
            //entity = customerPicService.getFromCache(Long.valueOf(productId),Long.valueOf(customerId));
            entity = customerPicService.get(Long.parseLong(id));
            if (entity == null) {
                entity = new ProductCompletePic();
                model.addAttribute("entity", entity);
                addMessage(model, "实体对象不存在");
                canAction = false;
                model.addAttribute("canAction", canAction);
                return "modules/md/customerProductPicNewForm";
            }
            entity.setBarcodeMustFlag(Integer.valueOf(barcodeMustFlag));
            entity.setProduct(productService.getProductByIdFromCache(Long.valueOf(productId)));
            entity.setCustomer(customerService.getFromCache(Long.parseLong(customerId)));
        } else {
            entity = new ProductCompletePic();
            entity.setItems(Lists.newArrayList());
            entity.setBarcodeMustFlag(0);
            // add on 2020-5-12 begin
            if (StringUtils.isNotBlank(customerId) && StringUtils.isNumeric(customerId)) {
                entity.setCustomer(customerService.getFromCache(Long.parseLong(customerId)));
            }
            if (StringUtils.isNotBlank(productId) && StringUtils.isNumeric(productId)) {
                entity.setProduct(productService.getProductByIdFromCache(Long.valueOf(productId)));
            }
            // add on 2020-5-12 end
        }
        List<Dict> picTypes = MSDictUtils.getDictList(ProductCompletePic.DICTTYPE);
        if (picTypes == null || picTypes.isEmpty()) {
            addMessage(model, "未设定产品完成图片类型");
            canAction = false;
        } else {
            entity.parseItemsFromJson();
            customerPicService.syncItem(entity);
            List<ProductCompletePicItem> items = entity.getItems();
            if(customerId != null && productId != null) {
                items = customerPicService.getCompletePicItem(entity.getCustomer().getId(), entity.getProduct().getId()).getItems();
            }else {
                items = customerPicService.mergeAllItems(items, picTypes);
            }
            entity.setItems(items);
        }
        model.addAttribute("canAction", canAction);
        model.addAttribute("entity", entity);
        model.addAttribute("customerList", customerList);
        return "modules/md/customerProductPicNewForm";
    }

    /**
     * 保存修改数据
     *
     * @param entity
     * @param response
     */
    @ResponseBody
    @RequiresPermissions("md:customerpic:edit")
    @RequestMapping(value = "save")
    public AjaxJsonEntity save(ProductCompletePic entity, HttpServletResponse response) {
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        User user = UserUtils.getUser();
        if (user == null || user.getId() == null) {
            result.setSuccess(false);
            result.setMessage("登录超时，请重新登录。");
            return result;
        }

        if (entity == null || entity.getProduct() == null || entity.getProduct().getId() == null || entity.getProduct().getId().longValue() == 0l
                || entity.getItems() == null || entity.getItems().isEmpty() || entity.getCustomer() == null || entity.getCustomer().getId() == null
                || entity.getCustomer().getId().longValue() == 0l) {
            result.setSuccess(false);
            result.setMessage("传入内容有误，请检查。");
            return result;
        }
        try {
            entity.toJsonInfo();
            customerPicService.save(entity);
        } catch (Exception e) {
            if (StringUtils.contains(e.getMessage(), "Duplicate")) {
                result.setSuccess(false);
                result.setMessage("数据库中数据重复定义，请确认");
            } else {
                result.setSuccess(false);
                result.setMessage(e.getMessage());
            }
        }
        return result;
    }

    /**
     * 删除
     *
     * @param id
     * @param customerId
     * @param customerId
     * @param redirectAttributes
     */
    @RequiresPermissions("md:customerpic:edit")
    @RequestMapping(value = "delete")
    public String delete(Long id, Long productId, Long customerId, RedirectAttributes redirectAttributes) {
        try {
            if (id == null || id.longValue() == 0l || productId == null || productId.longValue() == 0l
                    || customerId == null || customerId.longValue() == 0l) {
                addMessage(redirectAttributes, "删除错误:参数不合法");
            } else {
                customerPicService.deleteById(new ProductCompletePic(id, productId, customerId));
                addMessage(redirectAttributes, "删除成功");
            }
        } catch (Exception e) {
            log.error("[ProductCompletePicController.delete]", e);
            addMessage(redirectAttributes, "删除错误:" + ExceptionUtils.getMessage(e));
        } finally {
            return "redirect:" + adminPath + "/md/customer/pic/list?repage";
        }
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
            return jsonEntity;
        }

    }

    /**
     * 获取已配置产品图片规格
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "ajax/getCompletePicItem")
    public AjaxJsonEntity getCompletePicItem(Long customerId,Long productId) {
        AjaxJsonEntity jsonEntity = new AjaxJsonEntity(true);
        ProductCompletePic entity = customerPicService.getCompletePicItem(customerId,productId);
        if (entity == null) {
            jsonEntity.setSuccess(false);
            return jsonEntity;
        }
        jsonEntity.setData(entity);
        return jsonEntity;
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
}
