package com.wolfking.jeesite.modules.md.web;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kkl.kklplus.entity.md.MDCustomerAction;
import com.kkl.kklplus.entity.md.MDCustomerProductType;
import com.kkl.kklplus.entity.md.MDCustomerProductTypeMapping;
import com.kkl.kklplus.entity.md.MDErrorType;
import com.kkl.kklplus.entity.md.dto.MDCustomerActionDto;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.Product;
import com.wolfking.jeesite.modules.md.entity.ProductCategory;
import com.wolfking.jeesite.modules.md.service.CustomerProductTypeService;
import com.wolfking.jeesite.modules.md.service.ProductCategoryService;
import com.wolfking.jeesite.modules.md.service.ProductService;
import com.wolfking.jeesite.modules.md.utils.CustomerUtils;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Controller
@RequestMapping(value = "${adminPath}/md/customerProductType")
public class CustomerProductTypeController extends BaseController {

    @Autowired
    private CustomerProductTypeService customerProductTypeService;

    @Autowired
    private ProductCategoryService productCategoryService;

    @Autowired
    private ProductService productService;


    @RequiresPermissions("md:customeraction:view")
    @RequestMapping("customerActionList")
    public String customerActionList(MDCustomerActionDto mdCustomerActionDto, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<MDCustomerActionDto> entityPage = new Page<>(request,response);
        List<Product> list = Lists.newArrayList();
        List<String> errorTypeNameList = Lists.newArrayList();
        if (mdCustomerActionDto.getCustomerId() != null) {
            Page<Object> page = new Page<>(request, response);
            entityPage = customerProductTypeService.findCustomerActionDtoList(page, mdCustomerActionDto);
            list = productService.getCustomerProductList(mdCustomerActionDto.getCustomerId());
            if (mdCustomerActionDto.getProductId() != null) {
                errorTypeNameList = customerProductTypeService.findErrorTypeNameList(mdCustomerActionDto.getCustomerId(), mdCustomerActionDto.getProductId());
            }
        }
        model.addAttribute("entityPage", entityPage);
        model.addAttribute("productList", list);
        model.addAttribute("errorTypeNameList", errorTypeNameList);
        model.addAttribute("mdCustomerActionDto", mdCustomerActionDto);
        return "modules/md/customerActionList";
    }

    @RequiresPermissions("md:customeraction:edit")
    @RequestMapping("customerActionForm")
    public String customerActionForm(MDCustomerActionDto mdCustomerActionDto, Model model) {
        if (mdCustomerActionDto.getCustomerId() != null) {
            String customerName = CustomerUtils.getCustomer(mdCustomerActionDto.getCustomerId()).getName();
            mdCustomerActionDto.setCustomerName(customerName);
        }
        model.addAttribute("mdCustomerActionDto", mdCustomerActionDto);
        return "modules/md/customerActionForm";
    }


    @RequiresPermissions("md:customeraction:view")
    @RequestMapping("customerProductTypeList")
    public String customerProductTypeList(MDCustomerProductType mdCustomerProductType, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<MDCustomerProductType> entityPage = new Page<>();
        if (mdCustomerProductType.getCustomerId() != null) {
            Page<Object> page = new Page<>(request, response);
            entityPage = customerProductTypeService.customerProductTypeFindList(mdCustomerProductType.getCustomerId(), page.getPageNo(), page.getPageSize());
        }
        model.addAttribute("page", entityPage);
        model.addAttribute("mdCustomerProductType", mdCustomerProductType);
        return "modules/md/customerProductTypeList";
    }

    @RequiresPermissions("md:customeraction:edit")
    @RequestMapping("customerProductTypeForm")
    public String customerProductTypeForm(MDCustomerProductType mdCustomerProductType, Model model) {
        MDCustomerProductType entity = new MDCustomerProductType();
        if (mdCustomerProductType.getId() != null) {
            entity = customerProductTypeService.customerProductTypeGetById(mdCustomerProductType.getId());
        }
        if (mdCustomerProductType.getCustomerId() != null) {
            entity.setCustomerId(mdCustomerProductType.getCustomerId());
            String customerName = CustomerUtils.getCustomer(mdCustomerProductType.getCustomerId()).getName();
            entity.setCustomerName(customerName);
        }
        model.addAttribute("entity", entity);
        return "modules/md/customerProductTypeForm";
    }

    @RequiresPermissions("md:customeraction:view")
    @RequestMapping("customerRelatedProductsList")
    public String customerProductTypeList(MDCustomerAction mdCustomerAction, Model model) {
        List<MDCustomerProductTypeMapping> entityList = Lists.newArrayList();
        if (mdCustomerAction.getCustomerId() != null) {

            entityList = customerProductTypeService.findProductTypeMappingByCustomerId(mdCustomerAction.getCustomerId());
        }
        model.addAttribute("entityList", entityList);
        model.addAttribute("mdCustomerAction", mdCustomerAction);
        return "modules/md/customerRelatedProductsList";
    }

    @RequiresPermissions("md:customeraction:edit")
    @RequestMapping("updateCustomerRelatedProductsList")
    public String updateCustomerProductList(MDCustomerAction mdCustomerAction, Model model) {
        Integer integer = 0;
        if (mdCustomerAction.getCustomerId() == 5266) {
            integer = customerProductTypeService.updateProductTypeList(mdCustomerAction.getCustomerId());
        }
        List<MDCustomerProductTypeMapping> entityList = customerProductTypeService.findProductTypeMappingByCustomerId(mdCustomerAction.getCustomerId());

        model.addAttribute("entityList", entityList);
        model.addAttribute("mdCustomerAction", mdCustomerAction);
        addMessage(model, "更新完成，更新客户产品分类" + integer + "个，已配置0个，失效0个");
        return "modules/md/customerRelatedProductsList";
    }


    @RequiresPermissions("md:customeraction:edit")
    @RequestMapping("customerRelatedProductsForm")
    public String customerRelatedProductsForm(Long customerId, Long customerProductTypeId, String customerProductTypeName, Model model) {

        List<ProductCategory> productCategoryList = productCategoryService.findIdAndNameListByCustomerId(customerId);
        int idx = customerProductTypeName.indexOf("（");
        customerProductTypeName = customerProductTypeName.substring(0, idx);
        model.addAttribute("productCategoryList", productCategoryList);
        model.addAttribute("customerProductTypeId", customerProductTypeId);
        model.addAttribute("customerProductTypeName", customerProductTypeName);
        model.addAttribute("customerId", customerId);
        return "modules/md/customerRelatedProductsForm";
    }

    @RequiresPermissions("md:customeraction:view")
    @RequestMapping("customerAction")
    public String customerAction(Long customerId, Long customerProductTypeId, String customerProductTypeName, Model model) {

        List<MDCustomerAction> mdCustomerActionList = customerProductTypeService.findIdAndNameByCustomerId(customerId, customerProductTypeId);
        int idx = customerProductTypeName.indexOf("（");
        customerProductTypeName = customerProductTypeName.substring(0, idx);
        model.addAttribute("mdCustomerActionList", mdCustomerActionList);
        model.addAttribute("customerProductTypeId", customerProductTypeId);
        model.addAttribute("customerProductTypeName", customerProductTypeName);
        return "modules/md/customerProductTypeAction";
    }

    /**
     * 获取客户产品
     *
     * @param customerProductTypeId
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "ajax/getCustomerProduct")
    public AjaxJsonEntity getCustomerProduct(Long customerId, Long productCategoryId, Long customerProductTypeId) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(false);
        try {
            Map<String, Object> map = Maps.newHashMap();
            List<Product> productList = productService.ProductByCategoryIdFindAllList(customerId, productCategoryId);
            List<Long> customerProducts = customerProductTypeService.findProductIds(customerId, customerProductTypeId);
            List<MDCustomerProductTypeMapping> customerProductOutList = customerProductTypeService.findListByCustomerId(customerId, customerProductTypeId);
            map.put("productList", productList);
            map.put("customerProducts", customerProducts);
            map.put("customerProductOutList", customerProductOutList);
            ajaxJsonEntity.setData(map);
            ajaxJsonEntity.setSuccess(true);
        } catch (Exception e) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage(e.getMessage());
        }

        return ajaxJsonEntity;
    }

    /**
     * 更新客户产品分类关联的产品
     *
     * @param customerProductTypeId
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "ajax/updateCustomerProductMapping")
    public AjaxJsonEntity updateCustomerProductMapping(Long customerProductTypeId, String productIds) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(false);
        try {
            List<Long> ids = Lists.newArrayList();
            if (!productIds.equals("")) {
                ids = Arrays.stream(productIds.split(",")).map(s -> Long.parseLong(s.trim())).collect(Collectors.toList());

            }
            customerProductTypeService.batchInsert(customerProductTypeId, ids);
            ajaxJsonEntity.setSuccess(true);
        } catch (Exception e) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage(e.getMessage());
        }

        return ajaxJsonEntity;
    }

    /**
     * 保存客户产品分类
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "ajax/saveCustomerProductType")
    public AjaxJsonEntity saveCustomerProductType(MDCustomerProductType mdCustomerProductType) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(false);
        try {
            User user = UserUtils.getUser();
            if (mdCustomerProductType.getId() != null) {
                mdCustomerProductType.setUpdateById(user.getId());
                mdCustomerProductType.setUpdateDate(new Date());
                customerProductTypeService.customerProductTypeUpdate(mdCustomerProductType);
            } else {
                mdCustomerProductType.setCreateById(user.getId());
                mdCustomerProductType.setCreateDate(new Date());
                customerProductTypeService.customerProductTypeInsert(mdCustomerProductType);
            }
            ajaxJsonEntity.setSuccess(true);
        } catch (Exception e) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage(e.getMessage());
        }

        return ajaxJsonEntity;
    }

    @RequiresPermissions("md:customeraction:edit")
    @ResponseBody
    @RequestMapping(value = {"delete"})
    public AjaxJsonEntity delete(MDCustomerActionDto mdCustomerActionDto) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(false);
        User user = UserUtils.getUser();
        if (user.getId() != null) {
            mdCustomerActionDto.setUpdateById(user.getId());
            mdCustomerActionDto.setUpdateDate(new Date());
            mdCustomerActionDto.setDelFlag(MDErrorType.DEL_FALG_DELETE);
            customerProductTypeService.deleteCustomerAction(mdCustomerActionDto);
            ajaxJsonEntity.setSuccess(true);
            ajaxJsonEntity.setMessage("删除故障列表成功");
        } else {
            ajaxJsonEntity.setMessage("当前用户不存在");
        }

        return ajaxJsonEntity;
    }

    /**
     * 根据客户获取客户产品分类列表
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "ajax/getCustomerProductTypeList")
    public AjaxJsonEntity getCustomerProductTypeList(Long customerId) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(false);
        try {
            List<MDCustomerProductType> customerProductTypeList = customerProductTypeService.getCustomerProductTypeList(customerId);
            ajaxJsonEntity.setData(customerProductTypeList);
            ajaxJsonEntity.setSuccess(true);
        } catch (Exception e) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage(e.getMessage());
        }

        return ajaxJsonEntity;
    }

    /**
     * 根据客户,产品获取故障分类列表
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "ajax/findErrorTypeNameList")
    public AjaxJsonEntity findErrorTypeNameList(Long customerId, Long productId) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(false);
        try {
            List<String> errorTypeNameList = customerProductTypeService.findErrorTypeNameList(customerId, productId);
            ajaxJsonEntity.setData(errorTypeNameList);
            ajaxJsonEntity.setSuccess(true);
        } catch (Exception e) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage(e.getMessage());
        }

        return ajaxJsonEntity;
    }

    /**
     * 根据客户Id,客户产品分类ID,一级,二级获取故障详
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "ajax/getCustomerActionList")
    public AjaxJsonEntity getCustomerActionList(Long customerId, Long customerProductTypeId, String errorTypeName, String errorAppearanceName) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(false);
        try {
            int verdict = 1;
            if (customerId == null) {
                verdict = 0;
            } else if (customerProductTypeId == null) {
                verdict = 0;
            } else if (errorTypeName.equals("")) {
                verdict = 0;
            } else if (errorAppearanceName.equals("")) {
                verdict = 0;
            }
            if (verdict == 1) {
                MDCustomerActionDto mdCustomerActionDto = new MDCustomerActionDto();
                mdCustomerActionDto.setCustomerId(customerId);
                mdCustomerActionDto.setCustomerProductTypeId(customerProductTypeId);
                mdCustomerActionDto.setErrorTypeName(errorTypeName);
                mdCustomerActionDto.setErrorAppearanceName(errorAppearanceName);
                MDCustomerActionDto customerAction = customerProductTypeService.getCustomerActionDto(mdCustomerActionDto);
                if (customerAction != null) {
                    if (customerAction.getCustomerActionDtoList() == null) {
                        customerAction.setCustomerActionDtoList(Lists.newArrayList());
                    }
                    ajaxJsonEntity.setData(customerAction);
                    ajaxJsonEntity.setSuccess(true);
                }
            }
        } catch (Exception e) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage(e.getMessage());
        }

        return ajaxJsonEntity;
    }


    /**
     * 删除客户获取客户产品分类列表
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "ajax/delete")
    public AjaxJsonEntity delete(Long id) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(false);
        try {
            User user = UserUtils.getUser();
            MDCustomerActionDto mdCustomerActionDto = new MDCustomerActionDto();
            mdCustomerActionDto.setId(id);
            mdCustomerActionDto.setUpdateById(user.getId());
            mdCustomerActionDto.setUpdateDate(new Date());
            customerProductTypeService.delete(mdCustomerActionDto);
            ajaxJsonEntity.setSuccess(true);
        } catch (Exception e) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage(e.getMessage());
        }
        return ajaxJsonEntity;
    }

    /**
     * 根据客户获取客户产品分类列表
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "ajax/save")
    public AjaxJsonEntity save(MDCustomerActionDto mdCustomerActionDto) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(false);
        try {
            User user = UserUtils.getUser();
            mdCustomerActionDto.setUpdateById(user.getId());
            mdCustomerActionDto.setUpdateDate(new Date());
            mdCustomerActionDto.setCreateById(user.getId());
            mdCustomerActionDto.setUpdateDate(new Date());
            if (mdCustomerActionDto.getCustomerActionDtoList() != null && mdCustomerActionDto.getCustomerActionDtoList().size() > 0) {
                List<MDCustomerActionDto> list = mdCustomerActionDto.getCustomerActionDtoList().stream().filter(t -> StringUtils.isNotBlank(t.getErrorAnalysisName()) || StringUtils.isNotBlank(t.getErrorProcess())).collect(Collectors.toList());
                if (list.size() > 0) {
                    mdCustomerActionDto.setNewFlag(false);
                }
                mdCustomerActionDto.setCustomerActionDtoList(list);
            }

            Integer integer = customerProductTypeService.saveCustomerActionDto(mdCustomerActionDto);
            if (integer > 0) {
                ajaxJsonEntity.setSuccess(true);
                ajaxJsonEntity.setMessage("保存成功");
            } else if (integer == 0) {
                ajaxJsonEntity.setSuccess(true);
                ajaxJsonEntity.setMessage("故障分类和故障现象已存在");
            } else {
                ajaxJsonEntity.setSuccess(false);
                ajaxJsonEntity.setMessage("请稍后重试！");
            }
        } catch (Exception e) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage(e.getMessage());
        }
        return ajaxJsonEntity;
    }


    /**
     * 检查客户分类名称是否存在
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "checkCustomerProductTypeName")
    public AjaxJsonEntity checkCustomerProductTypeName(Long id, Long customerId, String name) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        if (customerId != null) {
            MDCustomerProductType mdCustomerProductType = customerProductTypeService.getByCustomerIdAndName(customerId, name);
            if (mdCustomerProductType.getId() != null) {
                ajaxJsonEntity.setSuccess(false);
                if (mdCustomerProductType.getId().equals(id)) {
                    ajaxJsonEntity.setSuccess(true);
                }
            } else {
                ajaxJsonEntity.setSuccess(true);
            }
        }

        return ajaxJsonEntity;
    }


    /**
     * 删除客户产品分类
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "ajax/customerProductTypeDelete")
    public AjaxJsonEntity customerProductTypeDelete(Long customerId, Long id) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(false);
        try {
            User user = UserUtils.getUser();
            MDCustomerProductType mdCustomerProductType = new MDCustomerProductType();
            mdCustomerProductType.setId(id);
            mdCustomerProductType.setCustomerId(customerId);
            mdCustomerProductType.setUpdateById(user.getId());
            mdCustomerProductType.setUpdateDate(new Date());
            Integer integer = customerProductTypeService.customerProductTypeDelete(mdCustomerProductType);
            if (integer > 0) {
                ajaxJsonEntity.setSuccess(true);
                ajaxJsonEntity.setMessage("删除成功");
            } else {
                ajaxJsonEntity.setSuccess(false);
                ajaxJsonEntity.setMessage("该客户产品分类有关联产品 不能删除");
            }
        } catch (Exception e) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage(e.getMessage());
        }
        return ajaxJsonEntity;
    }
}
