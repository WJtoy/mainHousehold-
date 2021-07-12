package com.wolfking.jeesite.modules.md.web;

import com.google.common.collect.Lists;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.md.MDCustomer;
import com.kkl.kklplus.entity.md.MDCustomerAddress;
import com.kkl.kklplus.entity.md.MDCustomerEnum;
import com.kkl.kklplus.entity.md.MDCustomerVipLevel;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.config.Global;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.CustomerFinance;
import com.wolfking.jeesite.modules.md.entity.CustomerProduct;
import com.wolfking.jeesite.modules.md.service.CustomerNewService;
import com.wolfking.jeesite.modules.md.service.CustomerService;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.service.SystemService;
import com.wolfking.jeesite.modules.sys.utils.AreaUtils;
import com.wolfking.jeesite.modules.sys.utils.SeqUtils;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providermd.service.MSCustomerNewService;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * 客戶Controller
 *
 * @author ThinkGem
 * @version 2013-3-23
 */
@Slf4j
@Controller
@RequestMapping(value = "${adminPath}/md/customerNew")
public class CustomerNewController extends BaseController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private MSCustomerNewService msCustomerNewService;

    @Autowired
    private CustomerNewService customerNewService;

    @Autowired
    private SystemService systemService;

    @Autowired
    private AreaService areaService;

    //客户管理 （新）  开始
    @RequestMapping(value = {"list"})
    public String list(MDCustomer mdCustomer, HttpServletRequest request, HttpServletResponse response, Model model) {

        if (!SecurityUtils.getSubject().isPermitted("md:customer:view")) {
            addMessage(model, "未开通浏览权限");
            model.addAttribute("page", new Page<MDCustomer>());
            model.addAttribute("mdCustomer", new MDCustomer());
            return "modules/md/customerListMSNew";
        }
        //  微服务调用获取客户信息
        if (mdCustomer == null) {
            mdCustomer = new MDCustomer();
        }
        User user = UserUtils.getUser();

        if (user.isSalesPerson()) {
            mdCustomer.setSalesId(user.getId());
        } else {
            if (user.isMerchandiser()) {
                mdCustomer.setMerchandiserId(user.getId());
            }
            if (mdCustomer.getId() == null && StringUtils.isBlank(mdCustomer.getCode()) && StringUtils.isBlank(mdCustomer.getPhone())) {
                addMessage(model, "请选择客户或输入负责人电话");
                model.addAttribute("page", new Page<MDCustomer>());
                model.addAttribute("mdCustomer", mdCustomer);
                return "modules/md/customerListMSNew";
            }
        }

        Page<MDCustomer> page = customerNewService.findMDCustomerNewList(new Page<MDCustomer>(request, response), mdCustomer);

        model.addAttribute("page", page);
        model.addAttribute("mdCustomer", mdCustomer);
        return "modules/md/customerListMSNew";
    }

    @RequiresPermissions("md:customer:edit")
    @RequestMapping(value = "form")
    public String form(Customer customer, Model model) {
        User user = UserUtils.getUser();
        List<Dict> customerSignClassify = MSDictUtils.getDictList("customer_sign_classify");
        if (customer.getId() == null) {

            if (user.isSalesPerson()) {
                customer.setSales(user);//自己
            }

            CustomerFinance finance = new CustomerFinance();
            Dict paymentType = new Dict();
            //切换为微服务
            paymentType.setId(MSDictUtils.getDictByValue(String.valueOf(CustomerFinance.YF), "PaymentType").getId());
            paymentType.setValue(String.valueOf(CustomerFinance.YF));
            paymentType.setLabel("预付");
            paymentType.setType("PaymentType");
            finance.setPaymentType(paymentType);//预付
            customer.setFinance(finance);
            List<MDCustomerAddress> customerAddressList = new ArrayList<>();
            customerAddressList.add(new MDCustomerAddress(MDCustomerEnum.CustomerAddressType.CUSTOMERADDR.getValue(), MDCustomerEnum.CustomerAddressType.CUSTOMERADDR.getLabel()));
            customerAddressList.add(new MDCustomerAddress(MDCustomerEnum.CustomerAddressType.SHIPADDR.getValue(), MDCustomerEnum.CustomerAddressType.SHIPADDR.getLabel()));
            customerAddressList.add(new MDCustomerAddress(MDCustomerEnum.CustomerAddressType.RETURNADDR.getValue(), MDCustomerEnum.CustomerAddressType.RETURNADDR.getLabel()));
            customer.setCustomerAddresses(customerAddressList);
            model.addAttribute("productIds", "");
            customer.setAutoCompleteOrder(1); //自动客评默认为开


        } else {
            customer = customerNewService.get(customer.getId());
            if (customer != null) {
                List<String> nameIdList = Lists.newArrayList();
                List<CustomerProduct> productcustomerList = customerService.getListByCustomer(customer.getId());
                for (CustomerProduct p : productcustomerList) {
                    nameIdList.add(Long.toString(p.getProduct().getId()));
                }

                model.addAttribute("productIds", StringUtils.join(nameIdList, ","));
            }

        }
        List<MDCustomerVipLevel> customerVipLevelList = customerNewService.customerVipLevelList();
        MDCustomerVipLevel customerLevel = customerNewService.findCustomerLevel();
        customerVipLevelList.sort(Comparator.comparing(MDCustomerVipLevel::getValue));
        model.addAttribute("customer", customer);
        model.addAttribute("customerSignClassify", customerSignClassify);
        model.addAttribute("customerVipLevelList", customerVipLevelList);
        model.addAttribute("customerLevel", customerLevel);
        return "modules/md/customerFormNew";
    }

    @RequestMapping(value = "customerAddressForms")
    public String customerAddressForms(MDCustomerAddress customerAddress,Integer editType, Model model) {
        MDCustomerAddress address = new MDCustomerAddress();

        if (customerAddress.getCustomerId() != null) {
            address = msCustomerNewService.getCustomerAddress(customerAddress.getId());
        }
        if(customerAddress.getId() != null){
            editType = 20;
        }
        if (address != null && address.getId() != null) {
            if (AreaUtils.getCountyFullName(address.getAreaId()) != null) {
                address.setAreaName(AreaUtils.getCountyFullName(address.getAreaId()));
            }
            if (address.getAddressType() == null) {
                address.setAddressType(customerAddress.getAddressType());
            }
            address.setAddressTypeName(MDCustomerEnum.CustomerAddressType.fromValue(address.getAddressType()).getLabel());
            if (address.getCustomerId() == null) {
                address.setCustomerId(customerAddress.getCustomerId());
            }
        } else {
            address = new MDCustomerAddress();
            address.setAddressType(customerAddress.getAddressType());
            address.setAddressTypeName(MDCustomerEnum.CustomerAddressType.fromValue(customerAddress.getAddressType()).getLabel());
            if(customerAddress.getAreaId() != null){
                address.setAreaId(customerAddress.getAreaId());
            }
            if(customerAddress.getUserName() != null){
                address.setUserName(customerAddress.getUserName());
            }
            if(customerAddress.getContactInfo() != null){
                address.setContactInfo(customerAddress.getContactInfo());
            }
            if(customerAddress.getAreaName() != null){
                address.setAreaName(customerAddress.getAreaName());
            }
            if(customerAddress.getAddress() != null){
                address.setAddress(customerAddress.getAddress());
            }
            if (customerAddress.getCustomerId() != null) {
                address.setCustomerId(customerAddress.getCustomerId());
            }
            if (customerAddress.getId() != null) {
                address.setId(customerAddress.getId());
            }
            if (customerAddress.getIsDefault() != null) {
                address.setIsDefault(customerAddress.getIsDefault());
            }
        }

        model.addAttribute("customerAddress", address);
        model.addAttribute("editType", editType );

        return "modules/md/customerAddressForm";
    }

    @RequestMapping(value = "saveCustomerAddress")
    @ResponseBody
    public AjaxJsonEntity saveCustomerAddress(MDCustomerAddress customerAddress, Model model) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        if (!beanValidator(model, customerAddress)) {
            ajaxJsonEntity.setSuccess(false);
            return ajaxJsonEntity;
        }
        try {
            if (customerAddress.getCustomerId() != null) {
                User user = UserUtils.getUser();
                MSResponse<Long> response = new MSResponse<>();
                if (customerAddress.getAreaId() != null) {
                    Area city = areaService.getFromCache(customerAddress.getAreaId());
                    if (city != null) {
                        customerAddress.setCityId(city.getParentId());
                        Area province = areaService.getFromCache(city.getParentId());
                        if (province != null) {
                            customerAddress.setProvinceId(province.getParentId());
                        }
                    }
                }
                if(customerAddress.getAddress() != null){
                    customerAddress.setAddress(customerAddress.getAddress().replace("&nbsp;"," "));
                }
                if (customerAddress.getId() != null) {
                    customerAddress.setUpdateById(user.getId());
                    customerAddress.setUpdateDate(new Date());
                    MSResponse<Integer> updateMSResponse = msCustomerNewService.updateCustomerAddress(customerAddress);
                    if(updateMSResponse.getCode() > MSErrorCode.SUCCESS.getCode()){
                        ajaxJsonEntity.setSuccess(false);
                        ajaxJsonEntity.setMessage(response.getMsg());
                    }else {
                        ajaxJsonEntity.setMessage("修改地址成功");
                        ajaxJsonEntity.setData(response.getData());
                    }
                } else {
                    customerAddress.setCreateById(user.getId());
                    customerAddress.setCreateDate(new Date());
                    response = msCustomerNewService.insertCustomerAddress(customerAddress);
                    if (response.getCode() > MSErrorCode.SUCCESS.getCode()) {
                        ajaxJsonEntity.setSuccess(false);
                        ajaxJsonEntity.setMessage(response.getMsg());
                    } else {
                        ajaxJsonEntity.setMessage("保存地址成功");
                        ajaxJsonEntity.setData(response.getData());
                    }
                }


            }

            return ajaxJsonEntity;

        } catch (Exception ex) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage(ex.getMessage());
            return ajaxJsonEntity;
        }

    }
    @RequestMapping(value = "deleteCustomerAddress")
    @ResponseBody
    public AjaxJsonEntity deleteCustomerAddress(Long addressId, Model model) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        try {
            Integer integer = msCustomerNewService.deleteCustomerAddress(addressId);
            if (integer > 0){
                ajaxJsonEntity.setMessage("删除成功");
            }else {
                ajaxJsonEntity.setSuccess(false);
                ajaxJsonEntity.setMessage("删除失败");
            }
        }catch (Exception e){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("删除失败");
        }
        return ajaxJsonEntity;
    }
    @RequestMapping(value = "customerFinanceForms")
    public String customerFinanceForms(Long customerId, String financeType, Model model) {
        CustomerFinance customerFinance = new CustomerFinance();
        if (customerId != null) {
            customerFinance = customerService.getFinance(customerId);
            if (customerFinance == null) {
                customerFinance = new CustomerFinance();
                customerFinance.setId(customerId);
            }
        }
        model.addAttribute("customerFinance", customerFinance);
        model.addAttribute("financeType", financeType);


        return "modules/md/customerFinanceForm";
    }

    @RequestMapping(value = "saveCustomerFinance")
    @ResponseBody
    public AjaxJsonEntity saveCustomerFinance(CustomerFinance customerFinance, Integer financeType, Model model) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        if (!beanValidator(model, customerFinance)) {
            ajaxJsonEntity.setSuccess(false);
            return ajaxJsonEntity;
        }
        try {
            if (customerFinance.getId() != null) {
                customerNewService.saveCustomerFinance(customerFinance, financeType);
            }
            ajaxJsonEntity.setMessage("保存成功");
            return ajaxJsonEntity;

        } catch (Exception ex) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage(ex.getMessage());
            return ajaxJsonEntity;
        }

    }

    @RequiresPermissions("md:customer:edit")
    @ResponseBody
    @RequestMapping(value = "save")
    public AjaxJsonEntity save(Customer customer, String oldCode, Double oldCredit, Model model, RedirectAttributes redirectAttributes) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        if (!StringUtils.isNotBlank(customer.getCode())) {
            customer.setCode(SeqUtils.NextSequenceNo("Customer"));//新的方法
        }

        if (customer.getId() != null && customer.getId() > 0) {
            if (!"true".equals(checkCustomerCode(oldCode, customer.getCode()))) {
                addMessage(model, "客户'" + customer.getCode() + "'已存在");
            }
        }
        try {
            customerNewService.save(customer, oldCredit);

            addMessage(redirectAttributes, "保存客戶'" + customer.getName() + "'成功");
        } catch (Exception e) {
            log.error("保存客戶{}失败", e);
            ajaxJsonEntity.setSuccess(false);
//            addMessage(redirectAttributes, "保存客戶'" + customer.getName()
//                    + "'失败:" + e.getMessage());
            String strMsg = "保存客戶'" + customer.getName() + "'失败,原因:" + e.getMessage();
            ajaxJsonEntity.setMessage(strMsg);
        }
        return ajaxJsonEntity;
    }

    @RequestMapping(value = "salesChangeForm")
    public String salesUpdateForm(Customer customer, Model model) {

        if (customer.getId() != null) {
            customer = customerService.get(customer.getId());
        }
        model.addAttribute("customer", customer);

        return "modules/md/customerSalesFormNew";
    }

    @RequestMapping(value = "merchandiserChangeForm")
    public String merchandiserUpdateForm(Customer customer, Model model) {
        if (customer.getId() != null) {
            customer = customerService.get(customer.getId());
        }
        model.addAttribute("customer", customer);

        return "modules/md/customerMerchandiserFormNew";
    }

    @RequiresPermissions("md:customer:edit")
    @RequestMapping(value = "delete")
    public String delete(String id, RedirectAttributes redirectAttributes) {

        if (Global.isDemoMode()) {
            addMessage(redirectAttributes, "演示模式，不允许操作！");
            return "redirect:" + Global.getAdminPath() + "/md/customerNew/list?repage";
        }

        customerService.deleteById(Long.parseLong(id));

        addMessage(redirectAttributes, "删除客戶成功");

        return "redirect:" + Global.getAdminPath() + "/md/customerNew/list?repage";

    }
    @RequiresPermissions("md:customer:edit")
    @RequestMapping(value = "ajax/delete")
    @ResponseBody
    public AjaxJsonEntity deleteCustomer(Long id) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        if (Global.isDemoMode()) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("演示模式，不允许操作！");
        }

        customerService.deleteById(id);

        ajaxJsonEntity.setMessage("删除客戶成功");

        return ajaxJsonEntity;
    }

    @RequiresPermissions("md:customer:edit")
    @RequestMapping(value = "selectCustomerOrder")
    @ResponseBody
    public AjaxJsonEntity selectCustomerOrder(Long id) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        if (Global.isDemoMode()) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("演示模式，不允许操作！");
        }
        Boolean aBoolean = customerNewService.isCustomerOrOrder(id);
        if(aBoolean){
            ajaxJsonEntity.setSuccess(true);
        }else {
            ajaxJsonEntity.setSuccess(false);
        }
        return ajaxJsonEntity;
    }
    /**
     * 检查客户帐号手机号是否注册
     *
     * @param id 帐号id
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "checkPhone")
    public AjaxJsonEntity checkPhone(Long id, String phone) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        if (StringUtils.isBlank(phone)) {
            return result;
        }
        Long userId = systemService.checkLoginName(id, phone);
        if (id == null) {
            if (userId > 0) {
                result.setMessage("手机号已注册");
                result.setSuccess(false);
            }
        } else {
            if (userId != 0 && !userId.equals(id)) {
                result.setMessage("手机号已注册");
                result.setSuccess(false);
            }
        }

        return result;
    }

    /**
     * 检查客户编号是否注册
     *
     * @param oldLoginName
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "checkLoginName")
    public String checkCustomerCode(String oldLoginName, String code) {
        if (code != null && code.equals(oldLoginName)) {
            return "true";
        } else if (code != null && !customerService.existsCustomerByCode(code)) {
            return "true";
        }
        return "false";
    }

    /**
     * 检查客户名称是否注册
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "checkCustomerName")
    public String checkCustomerName(Long customerId, HttpServletRequest request) {
        String name = request.getParameter("name");
        return customerNewService.existsCustomerByName(customerId, name);
    }


    //客户管理 （新）  结束

    @ResponseBody
    @RequestMapping(value = "getCustomerByIdFromCache/{id}")
    public Customer getCustomerByIdFromCache(@PathVariable Long id) {
        if(id != null){
            return msCustomerNewService.getCustomerByIdFromCache(id);
        }
        return null;
    }

    @ResponseBody
    @RequestMapping(value = "reloadCustomerCacheById/{id}")
    public MSResponse reloadCustomerCacheById(@PathVariable Long id) {
        if(id != null){
            return msCustomerNewService.reloadCustomerCacheById(id);
        }
        return null;
    }
}
