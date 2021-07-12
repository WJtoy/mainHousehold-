package com.wolfking.jeesite.modules.customer.fi.web;

import com.kkl.kklplus.entity.fi.recharge.CustomerOfflineRechargeSearch;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.config.Global;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.customer.fi.service.CtCustomerCurrencyService;
import com.wolfking.jeesite.modules.fi.entity.CustomerCurrency;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providermd.service.MSCustomerService;
import com.wolfking.jeesite.ms.recharge.entity.CustomerOfflineRechargeModel;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Controller
@RequestMapping(value="${adminPath}/customer/fi/customerCurrency")
public class CtCustomerCurrencyController extends BaseController {

    private static final int DEFAULT_PAGE_SIZE = 12;

    @Autowired
    private CtCustomerCurrencyService ctCustomerCurrencyService;

    @Autowired
    private MSCustomerService msCustomerService;

    @Value("${site.code}")
    private String siteCode;

    @ModelAttribute
    public CustomerCurrency get(@RequestParam(required = false) Long id)
    {
        if (id != null){
            return ctCustomerCurrencyService.get(id);
        } else{
            return new CustomerCurrency();
        }
    }

    @RequiresPermissions(value = {"customer:fi:customercurrency:view"})
    @RequestMapping(value ="list")
    public String list(@RequestParam Map<String, Object> paramMap, CustomerCurrency customerCurrency, HttpServletRequest request, HttpServletResponse response, Model model)
    {
        if (customerCurrency.getCreateDate() == null) {
            customerCurrency.setCreateDate(DateUtils.setDays(new Date(), 1));
        }

        if (customerCurrency.getUpdateDate() == null) {
            customerCurrency.setUpdateDate(DateUtils.addDays(DateUtils.addMonths(customerCurrency.getCreateDate(), 1), -1));
        }

        if (customerCurrency.getFirstSearch() == 1) {
            customerCurrency.setFirstSearch(0);
            return "modules/customer/fi/ctCustomerCurrencyNewList";
        }

        Page<CustomerCurrency> page;
        User user = UserUtils.getUser();
        if(user.isCustomer()){
            customerCurrency.setCustomer(UserUtils.getUser()
                    .getCustomerAccountProfile().getCustomer());
        }else if(user.isSaleman()){
            user.setSalesId(user.getId());
            Customer customer;
            if(paramMap.get("customer.id")!=null&&paramMap.get("customer.id").toString()!="") {
                // customer = customerService.get(Long.valueOf(paramMap.get("customer.id").toString()));  // mark on 2019-6-27
                customer = new Customer(Long.valueOf(paramMap.get("customer.id").toString()));   // add on 2019-6-27
            }
            else {
                customer = new Customer();
            }
            customer.setSales(user);
            customerCurrency.setCustomer(customer);
        }
        else{
            if(paramMap.get("customer.id")!=null&&paramMap.get("customer.id").toString()!="") {
                //customerCurrency.setCustomer(customerService.get(Long.valueOf(paramMap.get("customer.id").toString())));  // mark on 2019-6-27
                customerCurrency.setCustomer(new Customer(Long.valueOf(paramMap.get("customer.id").toString()))); // add on 2019-6-27
            }
        }
        /*
        String customerId = UserUtils.getUser().getCustomerAccountProfile()!= null ? UserUtils.getUser().getCustomerAccountProfile().getCustomer().getId().toString():"0";

        // 如果是客户身份登录
        if (!customerId.equals("0"))
        {
            customerCurrency.setCustomer(UserUtils.getUser()
                    .getCustomerAccountProfile().getCustomer());
        }else {
            if(paramMap.get("customer.id")!=null&&paramMap.get("customer.id").toString()!="") {
                customerCurrency.setCustomer(customerService.get(Long.valueOf(paramMap.get("customer.id").toString())));
            }
        }
        */

//        if (request.getMethod().equalsIgnoreCase("post"))
//        {
        List<String> quarters = QuarterUtils.getQuarters(customerCurrency.getCreateDate(), customerCurrency.getUpdateDate());
        customerCurrency.setQuarters(quarters);
        page =ctCustomerCurrencyService.find(new Page<>(request, response, DEFAULT_PAGE_SIZE), customerCurrency);
//        }
        //切换为微服务
        if (page.getCount() > 0){
            Map<String, Dict> paymentTypeMap = MSDictUtils.getDictMap("PaymentType");
            Map<String,Dict> chargeTypeMap = MSDictUtils.getDictMap("ChargeType");
            if (chargeTypeMap.size() > 0) {
                for (CustomerCurrency currency : page.getList()) {
                    //currency.getCustomerFinance().setPaymentType(paymentTypeMap.get(currency.getCustomerFinance().getPaymentType().getValue())); // mark on 2019-6-29
                    // add on 2019-6-29 begin
                    if (currency.getCustomerFinance() != null) {
                        currency.getCustomerFinance().setPaymentType(paymentTypeMap.get(currency.getCustomerFinance().getPaymentType().getValue()));
                    }
                    // add on 2019-6-29 end
                    if (currency.getPaymentType() != null && currency.getPaymentType() > 0) {
                        currency.setPaymentTypeName(chargeTypeMap.get(currency.getPaymentType().toString()).getLabel());
                    }
                }
            }
        }
        model.addAttribute("page", page);
        model.addAllAttributes(paramMap);
        return "modules/customer/fi/ctCustomerCurrencyNewList";
    }

    @RequiresPermissions("customer:fi:blockamountlist:view")
    @RequestMapping(value ={"blockamountlist"})
    public String customerBlockAmountList(@ModelAttribute("searchEntity") CustomerCurrency searchEntity,
                                          @RequestParam(name = "beginDate", required = false) Date beginDate,
                                          @RequestParam(name = "endDate", required = false) Date endDate,
                                          HttpServletRequest request, HttpServletResponse response, Model model) {
        Date now = new Date();
        if (searchEntity.getCreateDate() == null) {
            searchEntity.setCreateDate(DateUtils.getStartDayOfMonth(now));
        }
        if (searchEntity.getUpdateDate() == null) {
            searchEntity.setUpdateDate(DateUtils.getLastDayOfMonth(now));
        }
        searchEntity.setCreateDate(DateUtils.getStartOfDay(searchEntity.getCreateDate()));
        searchEntity.setUpdateDate(DateUtils.getEndOfDay(searchEntity.getUpdateDate()));

        if (searchEntity.getFirstSearch() == 1) {
            searchEntity.setFirstSearch(0);
            return "modules/customer/fi/ctCustomerBlockAmountNewList";
        }

        Long customerId = null;
        Long salesId = null;

        User user = UserUtils.getUser();
        if(user.isCustomer()){
            customerId = UserUtils.getUser().getCustomerAccountProfile().getCustomer().getId();
        }
        else {
            if(user.isSaleman()) {
                salesId = user.getId();
            }
            if (searchEntity.getCustomer() != null && searchEntity.getCustomer().getId() != null) {
                customerId = searchEntity.getCustomer().getId();
            }
        }

        Page<CustomerCurrency> page = new Page<>(request,response, DEFAULT_PAGE_SIZE);
        page = ctCustomerCurrencyService.getCustomerBlockAmountList(customerId, salesId, searchEntity.getCurrencyType(), searchEntity.getCurrencyNo(),
                searchEntity.getCreateDate(), searchEntity.getUpdateDate(), page);

        model.addAttribute("page", page);
        model.addAttribute("beginDate", beginDate);
        model.addAttribute("endDate", endDate);

        return "modules/customer/fi/ctCustomerBlockAmountNewList";
    }

    @RequiresPermissions("customer:fi:customeroffline:view")
    @RequestMapping(value ={"customerofflinelist"})
    public String customerOfflineList(CustomerOfflineRechargeSearch search, Integer firstSearch, HttpServletRequest request, HttpServletResponse response, Model model) {
        Date now = new Date();
        if (search.getCreateDate() == null) {
            search.setCreateDate(DateUtils.getStartDayOfMonth(now));
        }
        if (search.getUpdateDate() == null) {
            search.setUpdateDate(DateUtils.getLastDayOfMonth(now));
        }

        search.setBeginDt(DateUtils.getStartOfDay(search.getCreateDate()).getTime());
        search.setEndDt(DateUtils.getEndOfDay(search.getUpdateDate()).getTime());



        // add on 2019-6-29 begin
        if (firstSearch == null) {
            firstSearch = 1;
            model.addAttribute("search", search);

            return "modules/customer/fi/ctCustomerOfflineRechargeList";
        }
        // add on 2019-6-29 end
        Long customerId = null;


        User user = UserUtils.getUser();
        if(user.isCustomer()){
            customerId = UserUtils.getUser().getCustomerAccountProfile().getCustomer().getId();
            search.setCustomerId(customerId);
        }


        Page<CustomerOfflineRechargeModel> offlineRechargeModelPage = new Page<>(request,response);
        search.setSite(siteCode);
        Page<CustomerOfflineRechargeModel> page = ctCustomerCurrencyService.getCustomerOfflineRecharge(offlineRechargeModelPage, search);
        model.addAttribute("page", page);
        model.addAttribute("firstSearch", firstSearch);
        model.addAttribute("search", search);

        return "modules/customer/fi/ctCustomerOfflineRechargeList";
    }


    @RequiresPermissions(value={"customer:fi:customercurrency:view"})
    @RequestMapping(value = "form")
    public String form(CustomerCurrency customerCurrency, Model model, HttpServletRequest request)
    {
        if (customerCurrency.getCustomer() != null && (customerCurrency.getCustomer().getId() != null)) {

            Customer customer = msCustomerService.getByIdToCustomer(customerCurrency.getCustomer().getId());
            if (customer != null) {
                customerCurrency.setCustomer(customer); // add on 2019-7-22
            }
        }
        model.addAttribute("customerCurrency", customerCurrency);

        return "modules/customer/fi/ctCustomerCurrencyForm";
    }

    @RequiresPermissions(value="customer:fi:customercurrency:charge")
    @RequestMapping(value = "save")
    public String save(CustomerCurrency customerCurrency, String oldCode,
                       HttpServletRequest request, Model model,
                       RedirectAttributes redirectAttributes){
        customerCurrency.setCurrencyType(CustomerCurrency.CURRENCY_TYPE_IN);   //10-入账,20-结账
        if (!beanValidator(model, customerCurrency)) {
            return form(customerCurrency, model, request);
        }

        try {
            ctCustomerCurrencyService.onSave(customerCurrency);
            addMessage(redirectAttributes, "充值成功");
            String strDirectPage = request.getParameter("directPage");
            return "redirect:" + Global.getAdminPath() + "/customer/fi/customerCurrency/list?repage";
        }catch (Exception ex){
            addMessage(model, "充值失败:" + ex.getLocalizedMessage());
            return form(customerCurrency, model, request);
        }
    }
}
