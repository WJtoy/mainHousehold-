package com.wolfking.jeesite.modules.fi.web;

import com.alipay.config.AlipayConfig;
import com.alipay.util.AlipayNotify;
import com.alipay.util.AlipaySubmit;
import com.kkl.kklplus.entity.fi.recharge.CustomerOfflineRechargeSearch;
import com.netflix.discovery.converters.Auto;
import com.wolfking.jeesite.common.config.Global;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.fi.entity.CustomerCurrency;
import com.wolfking.jeesite.modules.fi.service.CustomerCurrencyService;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.CustomerAccountProfile;
import com.wolfking.jeesite.modules.md.entity.CustomerFinance;
import com.wolfking.jeesite.modules.md.service.CustomerAccountProfileService;
import com.wolfking.jeesite.modules.md.service.CustomerService;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.modules.sys.utils.SeqUtils;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providermd.service.MSCustomerService;
import com.wolfking.jeesite.ms.recharge.entity.CustomerOfflineRechargeModel;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;

/**
 * 客户账号流水Controller
 * @author
 * @version
 */
@Controller
@RequestMapping(value = "${adminPath}/fi/customercurrency")
public class CustomerCurrencyController extends BaseController
{
    private static final int DEFAULT_PAGE_SIZE = 12;

    @Autowired
    private CustomerCurrencyService customerCurrencyService;
    @Autowired
    private CustomerService customerService;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private CustomerAccountProfileService customerAccountProfileService;

    @Autowired
    private MSCustomerService msCustomerService;  //customer微服务service

    //充值微服务启动标记
    @Value("${ms.rechargeService.enabled}")
    private boolean msRechargeEnabled;

    @Value("${site.code}")
    private String siteCode;

    @ModelAttribute
    public CustomerCurrency get(@RequestParam(required = false) Long id)
    {
        if (id != null){
            return customerCurrencyService.get(id);
        } else{
            return new CustomerCurrency();
        }
    }

    @RequiresPermissions(value = {"fi:customercurrency:view","fi:customercurrency:fimanager:view"}, logical = Logical.OR)
    @RequestMapping(value ={ "list", "" })
    public String list(@RequestParam Map<String, Object> paramMap,CustomerCurrency customerCurrency, HttpServletRequest request, HttpServletResponse response,Model model)
    {
        if (customerCurrency.getCreateDate() == null) {
            customerCurrency.setCreateDate(DateUtils.setDays(new Date(), 1));
        }

        if (customerCurrency.getUpdateDate() == null) {
            customerCurrency.setUpdateDate(DateUtils.addDays(DateUtils.addMonths(customerCurrency.getCreateDate(), 1), -1));
        }

        String strDirectPage = request.getParameter("directPage");
        Function<String, String> directPageFun = (directPage) ->{
            if (StringUtils.isNotBlank(directPage) && directPage.equalsIgnoreCase("fiManager")) {
                return "modules/fi/customerCurrencyNewListForFI";
            } else {
                return "modules/fi/customerCurrencyNewList";
            }
        };

        if (customerCurrency.getFirstSearch() == 1) {
            customerCurrency.setFirstSearch(0);
            //return "modules/fi/customerCurrencyList";
            return directPageFun.apply(strDirectPage);
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
            page =customerCurrencyService.find(new Page<>(request, response, DEFAULT_PAGE_SIZE), customerCurrency);
//        }
        //切换为微服务
        if (page.getCount() > 0){
            Map<String,Dict> paymentTypeMap = MSDictUtils.getDictMap("PaymentType");
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
        // return "modules/fi/customerCurrencyList";
        return directPageFun.apply(strDirectPage);
    }

    @RequiresPermissions(value={"fi:blockamountlist:view","fi:blockamountlist:fimanager:view"}, logical = Logical.OR)
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

        String strDirectPage = request.getParameter("directPage");

        Function<String, String> directPageFun = (directPage) ->{
            if (StringUtils.isNotBlank(directPage) && directPage.equalsIgnoreCase("fiManager")) {
                return "modules/fi/customerBlockAmountNewListForFI";
            } else {
                return "modules/fi/customerBlockAmountNewList";
            }
        };

        // add on 2019-6-29 begin
        if (searchEntity.getFirstSearch() == 1) {
            searchEntity.setFirstSearch(0);
            /*if (StringUtils.isNotBlank(strJspFlag) && strJspFlag.equalsIgnoreCase("fiManager")) {
                return "modules/fi/customerBlockAmountListForFI";
            } else {
                return "modules/fi/customerBlockAmountList";
            }*/
            return directPageFun.apply(strDirectPage);
        }
        // add on 2019-6-29 end
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
        page = customerCurrencyService.getCustomerBlockAmountList(customerId, salesId, searchEntity.getCurrencyType(), searchEntity.getCurrencyNo(),
                searchEntity.getCreateDate(), searchEntity.getUpdateDate(), page);

        model.addAttribute("page", page);
        model.addAttribute("beginDate", beginDate);
        model.addAttribute("endDate", endDate);
        //return "modules/fi/customerBlockAmountList";
        return directPageFun.apply(strDirectPage);
    }

    @RequiresPermissions(value={"fi:customeroffline:view","fi:customeroffline:fimanager:view"}, logical = Logical.OR)
    @RequestMapping(value ={"customerofflinelist"})
    public String customerOfflineList(CustomerOfflineRechargeSearch search,Integer firstSearch, HttpServletRequest request, HttpServletResponse response, Model model) {
        Date now = new Date();
        if (search.getCreateDate() == null) {
            search.setCreateDate(DateUtils.getStartDayOfMonth(now));
        }
        if (search.getUpdateDate() == null) {
            search.setUpdateDate(DateUtils.getLastDayOfMonth(now));
        }

        search.setBeginDt(DateUtils.getStartOfDay(search.getCreateDate()).getTime());
        search.setEndDt(DateUtils.getEndOfDay(search.getUpdateDate()).getTime());

        String strDirectPage = request.getParameter("directPage");

        Function<String, String> directPageFun = (directPage) ->{
            if (StringUtils.isNotBlank(directPage) && directPage.equalsIgnoreCase("fiManager")) {
                return "modules/fi/customerOfflineRechargeListForFI";
            } else {
                return "modules/fi/customerOfflineRechargeList";
            }
        };

        // add on 2019-6-29 begin
        if (firstSearch == null) {
            firstSearch = 1;
            model.addAttribute("search", search);
            return directPageFun.apply(strDirectPage);
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
        Page<CustomerOfflineRechargeModel> page = customerCurrencyService.getCustomerOfflineRecharge(offlineRechargeModelPage, search);
        model.addAttribute("page", page);
        model.addAttribute("firstSearch", firstSearch);
        model.addAttribute("search", search);

        return directPageFun.apply(strDirectPage);
    }

    @RequiresPermissions(value={"fi:customercurrency:view","fi:customercurrency:fimanager:view",},logical = Logical.OR)
    @RequestMapping(value = "form")
    public String form(CustomerCurrency customerCurrency, Model model, HttpServletRequest request)
    {
        if (customerCurrency.getCustomer() != null && (customerCurrency.getCustomer().getId() != null)) {
            // customerCurrency.setCustomer(customerService.get(customerCurrency.getCustomer().getId()));  // mark on 2019-7-22
            Customer customer = msCustomerService.getByIdToCustomer(customerCurrency.getCustomer().getId());
            if (customer != null) {
                customerCurrency.setCustomer(customer); // add on 2019-7-22
            }
        }
        model.addAttribute("customerCurrency", customerCurrency);

        String strDirectPage = request.getParameter("directPage");

        Function<String, String> directPageFun = (directPage) ->{
            if (StringUtils.isNotBlank(directPage) && directPage.equalsIgnoreCase("fiManager")) {
                return "modules/fi/customerCurrencyFormForFI";
            } else {
                return "modules/fi/customerCurrencyForm";
            }
        };
        return directPageFun.apply(strDirectPage);
        //return "modules/fi/customerCurrencyForm";
    }

    @RequiresPermissions(value={"fi:customercurrency:charge","fi:customercurrency:fimanager:charge"}, logical = Logical.OR)
    @RequestMapping(value = "save")
    public String save(CustomerCurrency customerCurrency, String oldCode,
                       HttpServletRequest request, Model model,
                       RedirectAttributes redirectAttributes){
        customerCurrency.setCurrencyType(CustomerCurrency.CURRENCY_TYPE_IN);   //10-入账,20-结账
        if (!beanValidator(model, customerCurrency)) {
            return form(customerCurrency, model, request);
        }

        try {
            customerCurrencyService.onSave(customerCurrency);
            addMessage(redirectAttributes, "充值成功");
            String strDirectPage = request.getParameter("directPage");
            if (StringUtils.isNotBlank(strDirectPage) && strDirectPage.equalsIgnoreCase("fiManager")) {
                return "redirect:" + Global.getAdminPath() + "/fi/customercurrency/list?repage&directPage=fiManager";
            } else {
                return "redirect:" + Global.getAdminPath() + "/fi/customercurrency/list?repage";
            }
        }catch (Exception ex){
            addMessage(model, "充值失败:" + ex.getLocalizedMessage());
            return form(customerCurrency, model, request);
        }
    }

    //region 在线充值

    //form
    @RequestMapping(value ={ "chargeonline" }, method = RequestMethod.GET)
    public String chargeonlineGet(CustomerCurrency customerCurrency,
                                  HttpServletRequest request, HttpServletResponse response,
                                  Model model)
    {
        //充值微服务
        if(msRechargeEnabled){
            String msRechargeUtl = "redirect:%s/fi/recharge/customer/rechargeForm";
            return String.format(msRechargeUtl, Global.getAdminPath());
        }
        LogUtils.saveLog(request,null,null,"在线充值表单",UserUtils.getUser());
        Long customerId = getCustomerId();
        // 如果是客户身份登录
        if ( customerId != 0) {
            // Customer customer = customerService.get(customerId); // mark on 2019-7-22
            Customer customer = msCustomerService.getByIdToCustomer(customerId);  // add on 2019-7-22
            if (customer != null) {
                customerCurrency.setCustomer(customer);
                model.addAttribute("canSave",true);
            }else{
                model.addAttribute("canSave",false);
                addMessage(model,"读取账号所属厂商信息失败");
            }
        }else{
            model.addAttribute("canSave",false);
            addMessage(model,"您的账号无厂商信息");
        }
        model.addAttribute("customerCurrency", customerCurrency);
        return "modules/fi/customerChargeOnlineForm";
    }

    //submit
    @RequestMapping(value ={ "chargeonline" }, method = RequestMethod.POST)
    public String chargeonlinePost(CustomerCurrency customerCurrency,
                                   HttpServletRequest request, HttpServletResponse response,
                                   Model model) throws Exception
    {
        Long customerId = getCustomerId();
        customerCurrency.setRemarks(customerCurrency.getRemarks() + customerCurrency.getCustomer().getName()+"在线充值");
        User user = UserUtils.getUser();
        if(user == null || user.getId() == null || user.getId()<0){
            model.addAttribute("customerCurrency", customerCurrency);
            addMessage(model, "登录已超时，请刷新页面重新登录！");
            return "modules/fi/customerChargeOnlineForm";
        }
        Boolean locked = false;
        String lockKey = null;
        String out_trade_no = "";
        try {
            if (customerId != 0) {
                //锁
                lockKey = String.format(RedisConstant.LOCK_CUSTOMER_ONLINECHAGE, customerId, user.getId());
                if (redisUtils.exists(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockKey)) {
                    addMessage(model, "您的帐号正在充值中...，请稍后重试");
                    model.addAttribute("customerCurrency", customerCurrency);
                    return "modules/fi/customerChargeOnlineForm";
                }

                locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockKey, 1, 60);//1分钟
                if (!locked) {
                    addMessage(model, "您的帐号正在充值中...，请稍后重试");
                    model.addAttribute("customerCurrency", customerCurrency);
                    return "modules/fi/customerChargeOnlineForm";
                }

                // 支付类型
                String payment_type = "1";
                // 必填，不能修改
                // 服务器异步通知页面路径
                String PartnerIP = AlipayConfig.AlipayHTTP;
                String notify_url = PartnerIP + Global.getAdminPath() + "/fi/customercurrency/async";
                // 需http://格式的完整路径，不能加?id=123这类自定义参数
                // 页面跳转同步通知页面路径
                // String return_url =
                // "http://117.136.40.242:8080/jeesite/modules/sd/return_url";
                String return_url = PartnerIP + Global.getAdminPath() + "/fi/customercurrency/return_url";

                // 需http://格式的完整路径，不能加?id=123这类自定义参数，不能写成http://localhost/
                // 商户订单号
                //String out_trade_no = IdGen.uuid();// new
                out_trade_no = SeqUtils.NextSequenceNo("CustomerCurrencyNo");//

                // String(request.getParameter("WIDout_trade_no").getBytes("ISO-8859-1"),"UTF-8");
                // 商户网站订单系统中唯一订单号，必填
                // 订单名称
                String subject = "快可立账户充值";
                // 必填
                // 付款金额
                String total_fee = customerCurrency.getAmount().toString();
                // 必填
                // 订单描述
                String body = customerCurrency.getRemarks();
                // 商品展示地址
                String show_url = PartnerIP + "/static/images/wx_qrcode.jpg";
                // 需以http://开头的完整路径，例如：http://www.商户网址.com/myorder.html
                // 防钓鱼时间戳
                String anti_phishing_key = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCgNWlqYAnxMXq229vmbhfAPfqGXgHn+Zege0WFNBwUsgmqWL/Xo/J7d5gAptUXd3JhbEGZvXUWXS+bAGAaNKEgGzxnhIhepb0gd0t+gwj8/38D5dYSJ2xND3ON+/qB0Jh1hV5SaZNOeb7yuY2UdPDL/RNSpPPEyTZrumRL2WYt3QIDAQAB";// AlipaySubmit.query_timestamp();//
                // 若要使用请调用类文件submit中的query_timestamp函数

                // 客户端的IP地址
                String exter_invoke_ip = AlipayConfig.Exter_Invoke_IP;
                // 非局域网的外网IP地址，如：221.0.0.1

                // 零时保存充值信息
                // CustomerCurrency customerCurrency=new CustomerCurrency();
                customerCurrency.setId(SeqUtils.NextIDValue(SeqUtils.TableName.CustomerCurrency));
                Customer customer = new Customer();
                customer.setId(customerId);
                customerCurrency.setCustomer(customer);
                customerCurrency.setCurrencyType(CustomerCurrency.CURRENCY_TYPE_IN);
                customerCurrency.setCurrencyNo(out_trade_no);
                customerCurrency.setActionType(CustomerCurrency.ACTION_TYPE_TEMPRECHARGE);//临时订单

                customerCurrencyService.saveTempCurrency(customerCurrency);
                // ////////////////////////////////////////////////////////////////////////////////

                // 网银支付begin
                // 获得页面支付的方式
                String paymethod = "bankPay";
                String defaultbank = request.getParameter("BC");

                // 网银支付end
                // 把请求参数打包成数组
                Map<String, String> sParaTemp = new HashMap<String, String>();
                sParaTemp.put("service", "create_direct_pay_by_user");
                sParaTemp.put("partner", AlipayConfig.partner);
                sParaTemp.put("seller_email", AlipayConfig.seller_email);
                sParaTemp.put("_input_charset", AlipayConfig.input_charset);
                sParaTemp.put("payment_type", payment_type);
                sParaTemp.put("notify_url", notify_url);
                sParaTemp.put("return_url", return_url);
                sParaTemp.put("out_trade_no", out_trade_no);
                sParaTemp.put("subject", subject);
                sParaTemp.put("total_fee", total_fee);
                sParaTemp.put("body", body);

                // 不是默认支付宝的话 就提交网银的支付请求
                if (!defaultbank.equalsIgnoreCase("ZFB")) {
                    sParaTemp.put("paymethod", paymethod);
                    sParaTemp.put("defaultbank", defaultbank);
                }

                sParaTemp.put("show_url", show_url);
                sParaTemp.put("anti_phishing_key", anti_phishing_key);
                sParaTemp.put("exter_invoke_ip", exter_invoke_ip);

                //log
                LogUtils.saveLog(request, null, null, out_trade_no + ":在线充值提交" , UserUtils.getUser());

                // 建立请求
                String sHtmlText = AlipaySubmit.buildRequest(sParaTemp, "get", "确认");

                response.setHeader("Pragma", "No-cache");
                response.setHeader("Cache-Control", "no-cache");
                response.setContentType("text/html; charset=utf-8");
                response.getWriter().write(sHtmlText);
                response.getWriter().close();

                return null;
            } else {
                model.addAttribute("customerCurrency", customerCurrency);
                addMessage(model, "在线充值登录帐号必须为客户帐号！");
                return "modules/fi/customerChargeOnlineForm";
            }
        }catch (Exception e){
            model.addAttribute("customerCurrency", customerCurrency);
            addMessage(model, e.getMessage());
            //log
            LogUtils.saveLog(request, null, e, out_trade_no + ":在线充值提交失败" , UserUtils.getUser());
            return "modules/fi/customerChargeOnlineForm";
        }finally {
            if(StringUtils.isNotBlank(lockKey) && locked){
                redisUtils.expire(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockKey,5);//5秒后过期
            }
        }
    }

    /**
     * 同步通知的页面的Controller
     *
     * @param request
     * @param response
     * @return
     * @author
     */
    @RequestMapping(value = "return_url")
    public String Return_url(HttpServletRequest request,
                             HttpServletResponse response, Model model) {
        String successViewForm = "modules/fi/customerChargeSuccessReturnURLForm";
        String failViewForm = "modules/fi/customerChargeFailedReturnURLForm";
        User user = UserUtils.getUser();
        String tradeNo = request.getParameter("out_trade_no");
        String tradeStatus = request.getParameter("trade_status");

        StringBuffer sbMsg = new StringBuffer(300);
        //log
        LogUtils.saveLog(request,null,null,tradeNo + ":支付宝同步页面 BEGIN",user);

        Map<String, String> params = new HashMap<String, String>();
        Map requestParams = request.getParameterMap();
        for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext(); ) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
        }
        //验证
        if (!AlipayNotify.verify(params)) {// 验证失败
            LogUtils.saveLog(tradeNo+":支付宝同步页面-充值失败:验证失败", "return_url", "", null, null, 2);
            addMessage(model,"验证失败");
            return failViewForm;
        }

        String lockKey = String.format(RedisConstant.LOCK_ALIPAY_SYNC,tradeNo);
        Boolean locked = false;
        //是否有其他进程在处理
        Boolean hasLock = false;
        //锁
        if(redisUtils.exists(RedisConstant.RedisDBType.REDIS_LOCK_DB,lockKey)){
            LogUtils.saveLog(tradeNo+":支付宝同步页面-充值失败:已有其他进程在处理", "return_url", "", null, null, 2);
            addMessage(model,"已有其他进程在处理！");
            return failViewForm;
        }

        locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockKey, 1, 4 * 60);//4分钟
        if (!locked) {
            LogUtils.saveLog(tradeNo + ":支付宝同步页面-充值失败:加锁失败或已有其他进程在处理", "return_url", null, null, null, 2);
            addMessage(model,"已有其他进程在处理!");
            return failViewForm;
        }

        try {
            CustomerCurrency customerCurrency = customerCurrencyService.getByCurrencyNo(tradeNo, CustomerCurrency.ACTION_TYPE_TEMPRECHARGE);
            if (customerCurrency == null) {
                //已经更新成功，不能重复更新余额
                customerCurrency = customerCurrencyService.getByCurrencyNo(tradeNo, CustomerCurrency.ACTION_TYPE_TEMPRECHARGE2);
                model.addAttribute("customerCurrency", customerCurrency);
            } else if (tradeStatus.equals("TRADE_FINISHED") || tradeStatus.equals("TRADE_SUCCESS")) {
                // 要写的逻辑。自己按自己的要求写
                if (customerCurrency.getActionType().intValue() == CustomerCurrency.ACTION_TYPE_TEMPRECHARGE) {
                    // 无锁，才处理
                    if(!hasLock && locked) {
                        // 1.更新原记录状态
                        // 2.新增真正的充值记录
                        // 3.更新客户账户余额
                        customerCurrencyService.updateEntity(customerCurrency);
                    }
                }
                model.addAttribute("customerCurrency", customerCurrency);
                LogUtils.saveLog(request, null, null, tradeNo + ":支付宝同步页面 END", user);
                //return viewForm;
                //return "modules/fi/customerChargeSuccessReturnURLForm";
            }
            try {
                CustomerFinance finance = customerService.getFinanceForAddOrder(customerCurrency.getCustomer().getId());
                customerCurrency.setBalance(finance.getBalance() + (finance.getCreditFlag()==1?finance.getCredit():0d) - finance.getBlockAmount());
            }catch (Exception e){
                LogUtils.saveLog("充值同步返回：读取客户余额错误","CustomerCurrencyController.Return_url",tradeNo,e,user);
            }
            return successViewForm;
        }catch (Exception e){
            addMessage(model, ExceptionUtils.getMessage(e));
            LogUtils.saveLog(request, null, null, tradeNo + ":支付宝同步页面失败", user);
        }
        return failViewForm;
    }


    /**
     * 异步通知付款状态的Controller
     *
     * @param request
     * @param response
     * @return
     * @author 宗潇帅
     */
    @SuppressWarnings("rawtypes")
    @RequestMapping(value = "async") // ,method = RequestMethod.POST
    @ResponseBody
    public void async(HttpServletRequest request,
                        HttpServletResponse response, Model model) throws Exception
    {
        String tradeNo = new String("");
        tradeNo = request.getParameter("out_trade_no");
        User user = new User(0l);
        try {
            LogUtils.saveLog(request, null, null, tradeNo + ":支付宝异步页面 BEGIN", user);//log
        }catch (Exception e){}
        BufferedOutputStream out = null;
        String lockKey = new String("");
        Boolean locked = false;
        try {
            response.reset(); // 必要地清除response中的缓存信息
            response.setContentType("text/html; charset=UTF-8");
            lockKey = String.format(RedisConstant.LOCK_ALIPAY_SYNC,tradeNo);
            out = new BufferedOutputStream(response.getOutputStream());
            Map<String, String> params = new HashMap<String, String>();
            Map requestParams = request.getParameterMap();
            for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext(); ) {
                String name = (String) iter.next();
                String[] values = (String[]) requestParams.get(name);
                String valueStr = "";
                for (int i = 0; i < values.length; i++) {
                    valueStr = (i == values.length - 1) ? valueStr + values[i]
                            : valueStr + values[i] + ",";
                }
                params.put(name, valueStr);
            }

            String tradeStatus = request.getParameter("trade_status");
            String logparams = String.format("%s,%s",tradeNo,tradeStatus);
            if(StringUtils.isBlank(tradeNo)){
                out.write("fail".getBytes(StandardCharsets.UTF_8));
                LogUtils.saveLog(request, null, null, "支付宝异步页面:trade no无值", user);//log
                return;
            }
            /*
            if(redisUtils.exists(RedisConstant.RedisDBType.REDIS_LOCK_DB,lockKey)){
                out.write("fail".getBytes(StandardCharsets.UTF_8));
                //LogUtils.saveLog(tradeNo+":支付宝异步页面-充值失败:已有其他进程在处理", "async", logparams, null, null, 2);
                LogUtils.saveLog(request, null, null, tradeNo + ":支付宝异步页面-充值失败:已有其他进程在处理", user);//log
                return;
            }*/
            //锁
            //locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockKey, 1, 4*60);//4分钟
            locked = redisUtils.getLock(RedisConstant.RedisDBType.REDIS_LOCK_DB,lockKey,tradeNo,60);
            if (!locked) {
                out.write("fail".getBytes(StandardCharsets.UTF_8));
                LogUtils.saveLog(request, null, null, tradeNo + ":支付宝异步页面-充值失败:已有其他进程在处理", user);//log
                return;
            }
            if (AlipayNotify.verify(params)) {   // 验证成功
                if (tradeStatus.equals("TRADE_FINISHED") || tradeStatus.equals("TRADE_SUCCESS")) {
                    // 要写的逻辑
                    CustomerCurrency customerCurrency = customerCurrencyService.getByCurrencyNo(tradeNo, CustomerCurrency.ACTION_TYPE_TEMPRECHARGE);
                    if (customerCurrency == null) {//流水单不存在
                        LogUtils.saveLog(request,tradeNo+":支付宝异步页面-充值失败，读取单据错误" );
                        out.write("fail".getBytes(StandardCharsets.UTF_8));
                    } else if (customerCurrency.getActionType().intValue() == CustomerCurrency.ACTION_TYPE_CHARGEONLINE
                            || customerCurrency.getActionType().intValue() == CustomerCurrency.ACTION_TYPE_TEMPRECHARGE2) {
                        //已处理
                        out.write("success".getBytes(StandardCharsets.UTF_8));
                        LogUtils.saveLog(request, null, null, tradeNo+":支付宝异步页面-已成功，此次是重复通知", user);
                    } else if (customerCurrency.getActionType().intValue() == CustomerCurrency.ACTION_TYPE_TEMPRECHARGE) {
                        // 1.更新原记录状态
                        // 2.新增真正的充值记录
                        // 3.更新客户账户余额
                        customerCurrencyService.updateEntity(customerCurrency);
                        out.write("success".getBytes(StandardCharsets.UTF_8));
                        LogUtils.saveLog(request, null, null, tradeNo+":支付宝异步页面-充值成功", user);
                    } else {
                        out.write("fail".getBytes(StandardCharsets.UTF_8));
                        LogUtils.saveLog(request, null, null, tradeNo + ":支付宝异步页面-充值失败", user);//log
                    }
                }else{
                    out.write("fail".getBytes(StandardCharsets.UTF_8));
                    LogUtils.saveLog(request, null, null, tradeNo + ":支付宝异步页面-充值失败,通知状态错误", user);//log
                }
            } else {// 验证失败
                //log
                LogUtils.saveLog(request,null,null,tradeNo +":支付宝异步页面-验证失败" ,user);
                out.write("fail".getBytes(StandardCharsets.UTF_8));
            }
        }catch (Exception e){
            out.write("fail".getBytes(StandardCharsets.UTF_8));
            //log
            LogUtils.saveLog(request,null,e,tradeNo+":支付宝异步页面-异常" ,user);
        }finally {
            if(out != null) {
                out.flush();
                out.close();
            }
            /*锁延时释放
            if(StringUtils.isNotBlank(lockKey) && locked){
                redisUtils.expire(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockKey,5);//5秒后过期
            }*/
            try {
                LogUtils.saveLog(request, null, null, tradeNo+":支付宝异步页面 END", user);//log
            }catch (Exception e){}
        }
    }

    /**
     * 异步通知付款状态的Controller (旧版 2017-11-27)
     *
     * @param request
     * @param response
     * @return
     * @author 宗潇帅

    @SuppressWarnings("rawtypes")
    @RequestMapping(value = "async") // ,method = RequestMethod.POST
    @ResponseBody
    public String async(HttpServletRequest request,
                        HttpServletResponse response, Model model)
    {
        //log
        LogUtils.saveLog(request,null,null,"支付宝异步页面" ,new User(0l));
        try {
            Map<String, String> params = new HashMap<String, String>();
            Map requestParams = request.getParameterMap();
            for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext(); ) {
                String name = (String) iter.next();
                String[] values = (String[]) requestParams.get(name);
                String valueStr = "";
                for (int i = 0; i < values.length; i++) {
                    valueStr = (i == values.length - 1) ? valueStr + values[i]
                            : valueStr + values[i] + ",";
                }
                params.put(name, valueStr);
            }
            String tradeNo = request.getParameter("out_trade_no");
            String tradeStatus = request.getParameter("trade_status");
            // String notifyId = request.getParameter("notify_id");
            PrintWriter out = null;
            if (AlipayNotify.verify(params)) {   // 验证成功
                if (tradeStatus.equals("TRADE_FINISHED") || tradeStatus.equals("TRADE_SUCCESS")) {
                    CustomerCurrency customerCurrency = customerCurrencyService.getByCurrencyNo(tradeNo, CustomerCurrency.ACTION_TYPE_TEMPRECHARGE);
                    // 要写的逻辑。自己按自己的要求写
                    if (customerCurrency != null && customerCurrency.getActionType().intValue() == CustomerCurrency.ACTION_TYPE_TEMPRECHARGE) {
                        // 1.更新原记录状态
                        // 2.新增真正的充值记录
                        // 3.更新客户账户余额
                        customerCurrencyService.updateEntity(customerCurrency);
                        //System.out.println("ALiPay:ChangeCustomerBalance");
                    }
                }
                return "success";
            } else {// 验证失败
                //log
                LogUtils.saveLog(request,null,null,"支付宝异步页面:验证失败" ,new User(0l));
                return "fail";
            }
        }catch (Exception e){
            //log
            LogUtils.saveLog(request,null,e,"支付宝异步页面:验证失败" ,new User(0l));
            return "fail";
        }
    }*/

    //endregion 在线充值


    /**
     * 获取当前登录客户的客户id
     * @return
     */
    private Long getCustomerId()
    {
        Long customerId =0l;
        User user = UserUtils.getUser();
        if (user.isCustomer() && user.getCustomerAccountProfile() != null && user.getCustomerAccountProfile().getId() !=null) {
            if (user.getCustomerAccountProfile().getCustomer() == null) {
                CustomerAccountProfile entity = customerAccountProfileService.get(user.getCustomerAccountProfile().getId());
                customerId = entity.getCustomer().getId();
            } else {
                customerId = user.getCustomerAccountProfile().getCustomer().getId();
            }
        }
        return customerId;
    }



    /**
     * 异步通知付款状态的Controller
     *
     * @param request
     * @param response
     * @return
     * @author 宗潇帅
     */
    @SuppressWarnings("rawtypes")
    @RequestMapping(value = "test12356") // ,method = RequestMethod.POST
    @ResponseBody
    public void test12356(HttpServletRequest request,
                        HttpServletResponse response, Model model) throws Exception
    {

        response.reset(); // 必要地清除response中的缓存信息
        response.setContentType("text/html; charset=UTF-8");
        BufferedOutputStream out = null;
        //response.setContentType("application/octet-stream; charset=utf-8");
        try {
            out = new BufferedOutputStream(response.getOutputStream());
            out.write("success".getBytes(StandardCharsets.UTF_8));
        }catch (Exception e){
            out.write("fail".getBytes(StandardCharsets.UTF_8));
        }finally {
            if(out != null){
                out.flush();
                out.close();
            }
        }
    }
}
