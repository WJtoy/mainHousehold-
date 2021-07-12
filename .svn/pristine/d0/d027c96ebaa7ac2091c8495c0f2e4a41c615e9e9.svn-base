package com.wolfking.jeesite.modules.finance.md.web;

import com.google.common.collect.Lists;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.finance.md.service.FiCustomerPriceService;
import com.wolfking.jeesite.modules.md.entity.*;
import com.wolfking.jeesite.modules.md.entity.viewModel.CustomerPrices;
import com.wolfking.jeesite.modules.md.service.CustomerService;
import com.wolfking.jeesite.modules.md.service.ProductService;
import com.wolfking.jeesite.modules.md.service.ServiceTypeService;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providermd.entity.CustomerPriceModel;
import com.wolfking.jeesite.ms.providermd.service.MSCustomerPriceService;
import com.wolfking.jeesite.ms.providermd.service.MSProductPriceService;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 客户价格
 */
@Controller
@Slf4j
@RequestMapping(value = "${adminPath}/fi/md/customerPrice")
public class FiCustomerPriceController extends BaseController {

    @Autowired
    private ServiceTypeService serviceTypeService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private FiCustomerPriceService fiCustomerPriceService;

    @Autowired
    private ProductService productService;

    @Autowired
    private MSProductPriceService msProductPriceService;

    @Autowired
    private MSCustomerPriceService msCustomerPriceService;

    /**
     * 分页
     * @param customerPrice
     * @param request
     * @param response
     * @param model
     * @return
     */
    @RequiresPermissions("fi:md:customerprice:view")
    @RequestMapping(value = {"list", "price"})
    public String priceList(CustomerPrice customerPrice, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<CustomerPrice> page = new Page<>(request, response);
        if (customerPrice == null) {
            customerPrice = new CustomerPrice();
        }
        if (customerPrice.getProduct() == null) {
            customerPrice.setProduct(new Product());
        }
        if (customerPrice.getCustomer() == null || customerPrice.getCustomer().getId() <= 0) {
            customerPrice.setCustomer(new Customer());
            List<ServiceType> serviceTypes = serviceTypeService.findAllListIdsAndNames();
            //end
            model.addAttribute("page", page);
            model.addAttribute("serviceTypes", ServiceType.ServcieTypeOrdering.sortedCopy(serviceTypes));
            model.addAttribute("show", false);
            addMessage(model, "请选择客户");
            return "modules/finance/md/fiCustomerPriceList";
        } else {
            Customer customer = customerService.getCustomerByIdFromCache(customerPrice.getCustomer().getId());
            if (customer != null) {
                customerPrice.setCustomer(customer);
                model.addAttribute("customer", customer);
            }
        }
        User user = UserUtils.getUser();
        boolean errorFlag = false;
        if (user.isCustomer()) {
            if (user.getCustomerAccountProfile() != null && user.getCustomerAccountProfile().getCustomer() != null) {
                //登录用户的客户，防篡改
                customerPrice.setCustomer(user.getCustomerAccountProfile().getCustomer());
            } else {
                addMessage(model, "错误：登录超时，请退出后重新登录。");
                errorFlag = true;
            }
        }
        if (user.isSalesPerson()) {   //业务员
            customerPrice.getCustomer().setSales(user);
        } else if (user.isMerchandiser()) { //跟单员
            customerPrice.getCustomer().setMerchandiser(user);
        }
        if (errorFlag) {
            model.addAttribute("page", page);
            model.addAttribute("customerPrice", customerPrice);
            model.addAttribute("show", false);
            return "modules/finance/md/fiCustomerPriceList";
        }
        boolean notUsePrice = Objects.equals(String.valueOf(customerPrice.getCustomer().getUseDefaultPrice()), "0");
        if (notUsePrice) {
            model.addAttribute("notUsePrice", true);
        }
        // add on 2019-7-26 end
        page = fiCustomerPriceService.findPage(page, customerPrice);
        model.addAttribute("page", page);
        //调用微服务 只返回id 和 名称 start 2019-10-11
        List<ServiceType> serviceTypes = serviceTypeService.findAllListIdsAndNames();
        //end
        model.addAttribute("serviceTypes", ServiceType.ServcieTypeOrdering.sortedCopy(serviceTypes));
        model.addAttribute("customerPrice", customerPrice);
        model.addAttribute("show", true);
        return "modules/finance/md/fiCustomerPriceList";
    }

    /**
     * 编辑客户某产品下各服务价格
     * @param customerPrices
     */
    @RequiresPermissions("fi:md:customerprice:edit")
    @RequestMapping(value = "productForm")
    public String productForm(CustomerPrices customerPrices, String qCustomerId, String qCustomerName,
                              String qProductCategoryId, String qProductCategoryName, String qProductId,
                              String qProductName, String qFirstSearch, Model model) {
        model.addAttribute("customerId", qCustomerId);
        model.addAttribute("customerName", qCustomerName);
        model.addAttribute("productCategoryId", qProductCategoryId);
        model.addAttribute("productCategoryName", qProductCategoryName);
        model.addAttribute("productId", qProductId);
        model.addAttribute("productName", qProductName);
        model.addAttribute("qFirstSearch", qFirstSearch);

        if (customerPrices.getCustomer() == null || customerPrices.getCustomer().getId() == null ||
                customerPrices.getProduct() == null || customerPrices.getProduct().getId() == null) {
            addMessage(model, "参数：客户或产品错误.");
            model.addAttribute("canAction", false);
            return "modules/finance/md/fiCustomerPriceProductForm";
        }

        Long cid = customerPrices.getCustomer().getId();
        Customer customer = customerService.getCustomerByIdFromCache(cid);
        if (customer == null) {
            addMessage(model, "读取客户信息失败，请返回并重新打开。");
            model.addAttribute("canAction", false);
            return "modules/finance/md/fiCustomerPriceProductForm";
        }
        model.addAttribute("useDefaultPrice", customer.getUseDefaultPrice());
        customerPrices.setCustomer(customer);
        final String useDefaultPrice = String.valueOf(customer.getUseDefaultPrice());

        Long pid = customerPrices.getProduct().getId();// 获取客户价格下的产品
        Product product = productService.getProductByIdFromCache(pid);
        if (product == null) {
            addMessage(model, "读取产品信息失败，请返回并重新打开。");
            model.addAttribute("canAction", false);
            return "modules/finance/md/fiCustomerPriceProductForm";
        }
        customerPrices.setProduct(product);
        //调用微服务获取服务类型,返回id,名称,warrantyStatus,code start 2019-10-12
        List<ServiceType> serviceTypes = serviceTypeService.findAllListIdsAndNamesAndCodes();
        //end
        if (serviceTypes == null || serviceTypes.size() == 0) {
            addMessage(model, "读取服务类型信息失败，请返回并重新打开。");
            model.addAttribute("canAction", false);
            return "modules/finance/md/fiCustomerPriceProductForm";
        }
        //默认价格 标准价
        List<ProductPrice> allPrices = msProductPriceService.findGroupList(null, Lists.newArrayList(pid), null, null, customer.getId());
        // 获取此客户下的服务项目ids
        List<Long> allServiceTypeIdList = (allPrices == null || allPrices.isEmpty())?null:allPrices.stream().map(t -> t.getServiceType().getId()).distinct().collect(Collectors.toList());
        if (allPrices == null || allPrices.size() == 0) {
            addMessage(model, "产品参考价格为空，请先维护产品参考价格。");
            model.addAttribute("canAction", false);
            return "modules/finance/md/fiCustomerPriceProductForm";
        }
        // 所有价格
        List<CustomerPrice> prices = msCustomerPriceService.findPricesNew(cid, null);// 包含待审核价格,停用
        // 获取包含当前产品的价格
        final List<CustomerPrice> hasprices = prices != null && !prices.isEmpty() ? prices.stream().filter(t -> Objects.equals(t.getProduct().getId(), pid))
                .collect(Collectors.toList()) : Lists.newArrayList();
        final String priceType = hasprices.stream().map(p -> p.getPriceType().getValue()).findFirst().orElse(null);
        List<CustomerPrice> list = Lists.newArrayList();
        // 遍历服务项目
        allServiceTypeIdList.forEach(t -> {
            // 筛选出匹配服务项目的参考价格
            List<ProductPrice> pp = allPrices.stream().filter(p -> Objects.equals(p.getServiceType().getId(), t)).collect(Collectors.toList());

            // 筛选第一个匹配的服务项目
            ServiceType st = serviceTypes.stream().filter(m -> Objects.equals(m.getId(), t)).findFirst().orElse(null);
            if (st != null) {
                // 筛选第一个匹配的客户价格
                CustomerPrice price = hasprices != null && !hasprices.isEmpty() ? hasprices.stream()
                        .filter(s -> Objects.equals(s.getServiceType().getId(), t))
                        .findFirst().orElse(null) : null;
                // 匹配到了服务项目下的客户价格不为空
                if (price == null) {
                    price = new CustomerPrice();
                }
                price.setServiceType(st);

                // 客户有使用价格
                ProductPrice entity = allPrices.stream().filter(p -> Objects.equals(p.getServiceType().getId(), t)
                        && Objects.equals(p.getPriceType().getValue(), useDefaultPrice))
                        .findFirst().orElse(null);
                if (entity != null) {
                    price.setReferPrice(entity.getCustomerStandardPrice());
                    price.setReferDiscountPrice(entity.getCustomerDiscountPrice());
                }

                List<HashMap<String, Object>> productPriceList = Lists.newArrayList();
                // 切换为微服务
                Map<String, Dict> priceTypeMap = MSDictUtils.getDictMap("PriceType");
                for (ProductPrice productPrice : pp) {
                    HashMap<String, Object> productPriceMap = new HashMap<>();
                    if (useDefaultPrice.equals(productPrice.getPriceType().getValue())) {
                        productPriceMap.put("priceType", productPrice.getPriceType().getValue());
                        productPriceMap.put("priceTypeName", priceTypeMap.get(productPrice.getPriceType().getValue()).getLabel());
                        productPriceMap.put("standPrice", productPrice.getCustomerStandardPrice());
                        productPriceMap.put("discountPrice", productPrice.getCustomerDiscountPrice());
                        productPriceList.add(productPriceMap);
                    }
                }
                price.setProductPriceList(productPriceList);

                if (price.getUpdateBy() != null && price.getUpdateBy().getId() != null) {
                    User user = UserUtils.get(price.getUpdateBy().getId());
                    if (user != null) {
                        price.getUpdateBy().setName(user.getName());
                    }
                }
                list.add(price);
            }
        });

        customerPrices.setPrices(list);

        model.addAttribute("canAction", true);
        model.addAttribute("customerPrices", customerPrices);
        boolean isNewPrice = customerPrices.getPrices().stream().filter(i -> i.getId() == null).findAny().isPresent();// 判断是否含有新增价格

        model.addAttribute("isPresent", isNewPrice);
        return "modules/finance/md/fiCustomerPriceProductForm";
    }

    @RequiresPermissions("fi:md:customerprice:edit")
    @RequestMapping(value = "saveProductPrices")
    @ResponseBody
    public AjaxJsonEntity saveProductPrices(CustomerPrices entity, String qCustomerId, String qCustomerName,
                                               String qProductCategoryId, String qProductCategoryName, String qProductId,
                                               String qProductName, String qFirstSearch, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);

        List<CustomerPrice> prices = entity.getNewPrices().stream().filter(t -> t.getServiceType() != null).collect(Collectors.toList());
        if (prices == null || prices.size() == 0) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("错误：请设定服务价格");
            return ajaxJsonEntity;
        }

        final User user = UserUtils.getUser();
        entity.setCreateBy(user);
        entity.setCreateDate(new Date());
        try {
            fiCustomerPriceService.saveProductPrices(entity);
            ajaxJsonEntity.setMessage("保存客户：" + entity.getCustomer().getName() + " 价格成功");
        } catch (Exception e) {
            log.error("保存客户：" + entity.getCustomer().getName() + " 价格失败:" + e.getMessage(), e);
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("保存客户：" + entity.getCustomer().getName() + "' 价格失败:" + e.getMessage());
        }
        return ajaxJsonEntity;
    }

    /**
     * 启用价格
     * @param id
     * @return
     * @throws UnsupportedEncodingException
     */
    @RequiresPermissions("fi:md:customerprice:edit")
    @RequestMapping(value = "active")
    @ResponseBody
    public AjaxJsonEntity activePriceNew(Long id) throws UnsupportedEncodingException {
        User user = UserUtils.getUser();
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        CustomerPrice p;
        try {
            p = msCustomerPriceService.getPriceNew(id, 1);
            if (p == null) {
                ajaxJsonEntity.setSuccess(false);
                ajaxJsonEntity.setMessage("停用的价格记录不存在");
                return ajaxJsonEntity;
            }
            fiCustomerPriceService.startPrice(p, user);
            ajaxJsonEntity.setMessage("启用客户价格成功");
        } catch (Exception e) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage(e.getMessage());
        }
        return ajaxJsonEntity;
    }

    /**
     * 停用价格
     * @param id
     * @return
     * @throws UnsupportedEncodingException
     */
    @RequiresPermissions("fi:md:customerprice:edit")
    @RequestMapping(value = "delete")
    @ResponseBody
    public AjaxJsonEntity deletePriceNew(Long id) throws UnsupportedEncodingException {
        CustomerPrice customerPrice;
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        try {
            customerPrice = msCustomerPriceService.getPriceNew(id, null);
            if (customerPrice == null) {
                ajaxJsonEntity.setSuccess(false);
                ajaxJsonEntity.setMessage("读取价格失败");
                customerPrice = new CustomerPrice();
                customerPrice.setCustomer(new Customer(0l, ""));
                return ajaxJsonEntity;
            } else {
                fiCustomerPriceService.deletePrice(id);
                ajaxJsonEntity.setMessage("停用客户价格成功");
            }
        } catch (Exception e) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage(e.getMessage());
        }
        return ajaxJsonEntity;
    }

    /**
     * 使用标准价
     * @param customerPriceModel
     * @return
     */
    @RequiresPermissions("fi:md:customerprice:edit")
    @RequestMapping(value = "updateCustomizePriceFlag")
    @ResponseBody
    public AjaxJsonEntity updateCustomizePriceFlag(CustomerPriceModel customerPriceModel){
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        if (customerPriceModel.getCustomerId() == null || customerPriceModel.getCustomerId() <= 0) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("错误：读取客户失败");
            return ajaxJsonEntity;
        }
        if (customerPriceModel.getProductId() == null || customerPriceModel.getProductId() <= 0) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("错误：读取产品失败");
            return ajaxJsonEntity;
        }
        try {
            fiCustomerPriceService.updateCustomizePriceFlag(customerPriceModel);
            ajaxJsonEntity.setMessage("更新客户价格成功");
        } catch (Exception e) {
            log.error("更新客户价格为标准价失败:" + e.getMessage(), e);
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("更新客户价格为标准价失败,原因：" + e.getMessage());
        }
        return ajaxJsonEntity;
    }

    /**
     * 待审核价格列表
     * @param customerPrice
     * @param request
     * @param response
     * @param model
     * @return
     */
    @RequiresPermissions("fi:md:customerprice:view")
    @RequestMapping(value = "approvelist")
    public String priceApprovelist(CustomerPrice customerPrice, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<CustomerPrice> page = new Page<>(request, response);
        String repage = request.getParameter("repage");
        if (request.getMethod().equalsIgnoreCase("post") || (repage != null && repage.equalsIgnoreCase("true"))) {
            page = fiCustomerPriceService.findApprovePricePage(page, customerPrice);
        }
        model.addAttribute("page", page);
        model.addAttribute("customerPrice", customerPrice);
        //return "modules/md/customerPriceApproveList";
        return "modules/finance/md/fiCustomerPriceApproveList";

    }

    /**
     * 审核服务价格
     *
     * @param ids  价格id
     * @param response
     * @return
     */
    @RequiresPermissions("fi:md:customerprice:approve")
    @ResponseBody
    @RequestMapping(value = "approve")
    public AjaxJsonEntity approvePrice(@RequestParam String ids, HttpServletResponse response) {
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        User user = UserUtils.getUser();
        if (user == null || user.getId() == null) {
            result.setSuccess(false);
            result.setMessage("登录超时，请重新登录。");
            return result;
        }
        if (StringUtils.isBlank(ids)) {
            result.setSuccess(false);
            result.setMessage("审核服务价格失败:未传递参数，请选择要审核的选项。");
            return result;
        }
        List<String> lstids;
        List<Long> lids;
        try {
            //字符转字符List
            lstids = Arrays.asList(ids.split(","));
            //List<String> -> List<Long>
            lids = lstids.stream().map(t -> Long.valueOf(t)).collect(Collectors.toList());
            customerService.approvePrices(lids, user.getId());
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("审核服务价格时发生异常:" + e.getMessage());
        }

        return result;
    }

    /**
     * 编辑网点单个产品价格
     * @param customerPrice
     * @param model
     * @return
     */
    @RequiresPermissions("fi:md:customerprice:view")
    @RequestMapping(value = "form")
    public String priceForm(CustomerPrice customerPrice, String qCustomerId, String qCustomerName,
                            String qProductCategoryId, String qProductCategoryName, String qProductId,
                            String qProductName, String qFirstSearch, String examine, Model model) {
        if (customerPrice != null && customerPrice.getId() != null && customerPrice.getCustomer() == null) {
            customerPrice = customerService.getPriceNew(customerPrice.getId(), null);
            model.addAttribute("warrantyStatus", customerPrice.getServiceType().getWarrantyStatus().getValue());
        }
        if (customerPrice == null) {
            addMessage(model, "价格不存在");
            model.addAttribute("canAction", false);
        } else {
            //参考价格
            List<ProductPrice> allPrices = msProductPriceService.findGroupList(null, Lists.newArrayList(customerPrice.getProduct().getId()), Lists.newArrayList(customerPrice.getServiceType().getId()), null, customerPrice.getCustomer().getId());
            List<HashMap<String, Object>> productPriceList = Lists.newArrayList();
            //切换为微服务
            Map<String, Dict> priceTypeMap = MSDictUtils.getDictMap("PriceType");
            for (ProductPrice productPrice : allPrices) {
                HashMap<String, Object> productPriceMap = new HashMap<>();
                productPriceMap.put("priceType", productPrice.getPriceType().getValue());
                productPriceMap.put("priceTypeName", priceTypeMap.get(productPrice.getPriceType().getValue()).getLabel());
                productPriceMap.put("standPrice", productPrice.getCustomerStandardPrice());
                productPriceMap.put("discountPrice", productPrice.getCustomerDiscountPrice());
                productPriceList.add(productPriceMap);
            }
            model.addAttribute("productPriceList", productPriceList);
            model.addAttribute("canAction", true);
        }
        model.addAttribute("customerId", qCustomerId);
        model.addAttribute("customerName", qCustomerName);
        model.addAttribute("productCategoryId", qProductCategoryId);
        model.addAttribute("productCategoryName", qProductCategoryName);
        model.addAttribute("productId", qProductId);
        model.addAttribute("productName", qProductName);
        model.addAttribute("customerPrice", customerPrice);
        model.addAttribute("qFirstSearch", qFirstSearch);
        if (StringUtils.isNotBlank(examine)) {
            //return "modules/md/customerPriceForm";
            return "modules/finance/md/fiCustomerPriceForm";
        }
        //return "modules/md/customerPriceFormNew";
        return "modules/finance/md/fiCustomerPriceForm";
    }

    @RequiresPermissions("fi:md:customerprice:edit")
    @RequestMapping(value = "save")
    @ResponseBody
    public AjaxJsonEntity savePriceNew(CustomerPrice customerPrice, String qCustomerId, String qCustomerName,
                                       String qProductCategoryId, String qProductCategoryName, String qProductId,
                                       String qProductName, String qFirstSearch, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) throws UnsupportedEncodingException {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);

        ServiceType serviceType = serviceTypeService.get(customerPrice.getServiceType().getId());
        if (serviceType.getWarrantyStatus().getValue().equals(ServiceType.WARRANTY_STATUS_IW)) {
            if (customerPrice.getPrice() == 0 || customerPrice.getDiscountPrice() == 0) {
                ajaxJsonEntity.setSuccess(false);
                ajaxJsonEntity.setMessage("保存客户服务价格失败，" + customerPrice.getServiceType().getName() + "(保内)价格不能为0.");
                return ajaxJsonEntity;
            }
        }
        if (customerPrice.getDelFlag() == CustomerPrice.DEL_FLAG_NORMAL) {
            customerPrice.setDelFlag(CustomerPrice.DEL_FLAG_AUDIT);//待审核
        }
        boolean isNew;
        try {
            if (customerPrice.getId() == null) {
                customerPrice.preInsert();
                isNew = true;
            } else {
                customerPrice.preUpdate();
                isNew = false;
            }
            customerService.savePrice(customerPrice, isNew);
            ajaxJsonEntity.setMessage("保存客户服务价格'" + customerPrice.getCustomer().getName() + "'成功");
        } catch (Exception e) {
            log.error("保存客户服务价格:" + customerPrice.getCustomer().getName() + " 价格失败:" + e.getMessage(), e);
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("保存客户服务价格'" + customerPrice.getCustomer().getName() + " 价格失败");
        }
        return ajaxJsonEntity;
    }
}
