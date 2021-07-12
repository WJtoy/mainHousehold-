package com.wolfking.jeesite.ms.recharge.controller;

import com.google.common.collect.Maps;
import com.kkl.kklplus.entity.fi.common.PaymentType;
import com.kkl.kklplus.entity.fi.common.RechargeType;
import com.kkl.kklplus.utils.SimpleSignUtil;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.service.RechargeSequenceIdService;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.kkl.kklplus.utils.SequenceIdUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.fi.entity.CustomerCurrency;
import com.wolfking.jeesite.modules.md.dao.CustomerFinanceDao;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.CustomerAccountProfile;
import com.wolfking.jeesite.modules.md.entity.CustomerFinance;
import com.wolfking.jeesite.modules.md.service.CustomerAccountProfileService;
import com.wolfking.jeesite.modules.md.service.CustomerService;
import com.wolfking.jeesite.modules.mq.dto.MQCustomer;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.modules.sys.utils.SeqUtils;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("${adminPath}/fi/recharge/customer")
public class CustomerRechargeController extends BaseController {
    /*
    @Value("${sequence.workerid}")
    private int workerid;

    @Value("${sequence.datacenterid}")
    private int datacenterid;

    //id generator
    private static SequenceIdUtils sequenceIdUtils;

    @PostConstruct
    public void init() {
        CustomerRechargeController.sequenceIdUtils = new SequenceIdUtils(workerid,datacenterid);
    }
     */

    @Autowired
    private RechargeSequenceIdService idService;

    //private static SequenceIdUtils sequenceIdUtils = new SequenceIdUtils(31,0);

    //充值视图
    private final static String RECHARGE_FORM = "modules/fi/recharge/customerRechargeForm";

    //是否启用充值微服务
    @Value("${ms.rechargeService.enabled}")
    private boolean rechargeServiceEnabled;
    @Value("${ms.rechargeService.accessKey}")
    private String rechargeAccessKey;
    @Value("${ms.rechargeService.submitUrl}")
    private String rechargeSubmitUrl;

    @Value("${site.code}")
    private String siteCode;

    @Autowired
    private CustomerAccountProfileService customerAccountProfileService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private RedisUtils redisUtils;


    /**
     * 充值窗口
     */
    @RequiresPermissions("fi:customercurrency:chargeonline")
    @RequestMapping(value ={ "rechargeForm" }, method = RequestMethod.GET)
    public String rechargeForm(CustomerCurrency customerCurrency,
                               HttpServletRequest request, HttpServletResponse response,
                               Model model)
    {
        if(customerCurrency == null){
            customerCurrency = new CustomerCurrency();
            customerCurrency.setCustomer(new Customer(0L));
        }
        if(!rechargeServiceEnabled){
            addMessage(model,"错误：充值微服务未启用，无法充值！");
            model.addAttribute("canRecharge",false);
            model.addAttribute("customerCurrency", customerCurrency);
            return RECHARGE_FORM;
        }
        Long customerId = getCustomerId(null);
        Double balanceAmount=0.0;
        String mobile = "";
        // 如果是客户身份登录
        if (customerId != 0) {
            Customer customer = customerService.get(customerId);
            if(customer == null){
                addMessage(model,"错误：确认充值账号信息失败，请重试！");
                model.addAttribute("canRecharge",false);
                model.addAttribute("customerCurrency", customerCurrency);
                return RECHARGE_FORM;
            }
            customerCurrency.setCustomer(customer);
            balanceAmount = customerService.getBalanceAmount(customerId);
            User user = UserUtils.getUser();
            if(user!=null){
                mobile = user.getMobile();
            }
        }
        if(customerCurrency.getId() == null || customerCurrency.getId() <= 0){
            customerCurrency.setId(idService.nextId());//id
        }
        model.addAttribute("canRecharge",true);
        model.addAttribute("customerCurrency", customerCurrency);
        model.addAttribute("balanceAmount",balanceAmount);
        model.addAttribute("mobile",mobile);
        return RECHARGE_FORM;
    }

    /**
     * 充值提交接口
     * 1.检查充值微服务
     * 2.检查提交内容
     * 3.生成签名，提交到微服务
     */
    @RequiresPermissions("fi:customercurrency:chargeonline")
    @RequestMapping(value ={ "submitForm" }, method = RequestMethod.POST)
    public String submitForm(CustomerCurrency customerCurrency,
                             HttpServletRequest request, HttpServletResponse response,
                             Model model) throws Exception
    {
        Map<String,Object> attributesMap = Maps.newHashMapWithExpectedSize(5);
        attributesMap.put("canRecharge",false);
        attributesMap.put("customerCurrency",customerCurrency);
        if(!rechargeServiceEnabled){
            addMessage(model,"错误：充值微服务未启用，无法充值！");
            model.addAllAttributes(attributesMap);
            return RECHARGE_FORM;
        }
        if(customerCurrency.getId() == null || customerCurrency.getId()<=0){
            addMessage(model,"错误：非法请求，无唯一编号！");
            model.addAllAttributes(attributesMap);
            return RECHARGE_FORM;
        }
        User user = UserUtils.getUser();
        if(user == null || user.getId() == null || user.getId()<0){
            model.addAllAttributes(attributesMap);
            addMessage(model, "登录已超时，请刷新页面重新登录！");
            return RECHARGE_FORM;
        }
        Long customerId = getCustomerId(user);
        if (customerId == 0) {
            return submitFailResponse(customerCurrency, model, attributesMap, "在线充值登录帐号必须为客户帐号！");
        }
        if(!customerCurrency.getCustomer().getId().equals(customerId)){
            return submitFailResponse(customerCurrency, model, attributesMap, "充值账号与您所属账号不一致，请确认后再充值");
        }
        if(customerCurrency.getAmount()<=0){
            return submitFailResponse(customerCurrency, model, attributesMap, "充值金额应大于0");
        }

        customerCurrency.setRemarks(customerCurrency.getRemarks() + customerCurrency.getCustomer().getName()+"在线充值");
        Boolean locked = false;
        String lockKey = StringUtils.EMPTY;
        String out_trade_no = StringUtils.EMPTY;
        try {
            //锁
            lockKey = String.format(RedisConstant.LOCK_CUSTOMER_ONLINECHAGE, customerId, user.getId());
            locked = redisUtils.getLock(lockKey, customerId.toString(), 60);
            if (!locked) {
                return submitFailResponse(customerCurrency, model, attributesMap, "您的帐号正在充值中...，请稍后重试");
            }
            // 商户订单号
            out_trade_no = SeqUtils.NextSequenceNo("CustomerCurrencyNo",0,3);
            if(StringUtils.isBlank(out_trade_no)){
                return submitFailResponse(customerCurrency, model, attributesMap, "创建充值单据号失败，请稍后重试");
            }
            Date date = new Date();
            Map<String, String> params = new HashMap<String, String>();
            params.put("BC",request.getParameter("BC"));
            params.put("id",customerCurrency.getId().toString());
            params.put("site",siteCode);
            params.put("referId",customerId.toString());
            params.put("referName",customerCurrency.getCustomer().getName());
            params.put("tradeNo",out_trade_no);
            params.put("rechargeType", String.valueOf(RechargeType.Customer.code));
            params.put("payType",String.valueOf(PaymentType.Alipay.code));
            params.put("amount",customerCurrency.getAmount().toString());
            params.put("createAt",String.valueOf(new Date().getTime()));
            params.put("createById",user.getId().toString());
            params.put("createBy",user.getName());
            //时间戳,2分钟内有效，使用包含时间戳的参数产生的签名也防止伪造
            params.put("timestamp", String.valueOf(DateUtils.addMinutes(date,2).getTime()));
            // 建立请求
            String sHtmlText = SimpleSignUtil.buildRequest(params,rechargeAccessKey,"post","确认",rechargeSubmitUrl);
            response.setHeader("Pragma", "No-cache");
            response.setHeader("Cache-Control", "no-cache");
            response.setHeader("timestamp",params.get("timestamp"));
            response.setContentType("text/html; charset=utf-8");
            response.getWriter().write(sHtmlText);
            response.getWriter().close();
            return null;//一定要有return
        }catch (Exception e){
            LogUtils.saveLog(request, null, e, out_trade_no + ":在线充值-提交微服务失败" , UserUtils.getUser());
            return submitFailResponse(customerCurrency, model, attributesMap, ExceptionUtils.getMessage(e));
        }finally {
            //释放锁
            if(StringUtils.isNotBlank(lockKey) && locked){
                redisUtils.releaseLock(lockKey,customerId.toString());
            }
        }
    }

    /**
     * 处理失败
     * @param msg 错误提示内容
     */
    private String submitFailResponse(CustomerCurrency customerCurrency, Model model, Map<String, Object> attributesMap, String msg) {
        attributesMap.put("customerCurrency", customerCurrency);
        model.addAllAttributes(attributesMap);
        addMessage(model, msg);
        return RECHARGE_FORM;
    }


    /**
     * 获取当前登录客户的客户id
     * @return
     */
    private Long getCustomerId(User user)
    {
        Long customerId =0l;
        if(user == null) {
            user = UserUtils.getUser();
        }
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
}
