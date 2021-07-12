package com.wolfking.jeesite.ms.recharge.controller;


import com.kkl.kklplus.entity.fi.recharge.CustomerOfflineRecharge;
import com.kkl.kklplus.entity.fi.recharge.CustomerOfflineRechargeSearch;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.GsonUtils;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.CustomerAccountProfile;
import com.wolfking.jeesite.modules.md.service.CustomerAccountProfileService;
import com.wolfking.jeesite.modules.md.service.CustomerService;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.recharge.entity.CustomerOfflineRechargeModel;
import com.wolfking.jeesite.ms.recharge.service.CustomerOfflineRechargeService;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import lombok.extern.slf4j.Slf4j;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Controller
@RequestMapping("${adminPath}/fi/customer/offline/recharge")
public class CustomerOfflineRechargeController extends BaseController {

    @Value("${site.code}")
    private String siteCode;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerOfflineRechargeService customerOfflineRechargeService;

    @Autowired
    private CustomerAccountProfileService customerAccountProfileService;

    @Autowired
    private RedisUtils redisUtils;

    /**
     * 保存线下充值
     * */
    @RequestMapping(value = {"save"},method = RequestMethod.POST)
    @ResponseBody
    public AjaxJsonEntity save(CustomerOfflineRecharge customerOfflineRecharge, HttpServletRequest request){
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        if(customerOfflineRecharge.getId() == null || customerOfflineRecharge.getId()<=0){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("错误：非法请求，无唯一编号！");
            return ajaxJsonEntity;
        }
        User user = UserUtils.getUser();
        if(user == null || user.getId() == null || user.getId()<0){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("登录已超时，请刷新页面重新登录！");
            return ajaxJsonEntity;
        }
        Long customerId = getCustomerId(user);
        if (customerId == 0) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("在线充值登录帐号必须为客户帐号！");
            return ajaxJsonEntity;
        }
        if(!customerOfflineRecharge.getCustomerId().equals(customerId)){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("充值账号与您所属账号不一致，请确认后再充值");
            return ajaxJsonEntity;
        }
        if(customerOfflineRecharge.getPendingAmount()<=0){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("充值金额应大于0");
            return ajaxJsonEntity;
        }
        if(customerOfflineRecharge.getPayType()==null || customerOfflineRecharge.getPayType()<=0){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("请选择支付类型");
            return ajaxJsonEntity;
        }
        try {
            customerOfflineRecharge.setCreateById(user.getId());
            customerOfflineRecharge.setCreateAt(new Date().getTime());
            customerOfflineRecharge.setSite(siteCode);
            customerOfflineRecharge.setStatus(CustomerOfflineRecharge.PENDING_STATUS);
            customerOfflineRechargeService.save(customerOfflineRecharge);
            ajaxJsonEntity.setData(customerOfflineRecharge);
        }catch (Exception e){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage(e.getMessage());
        }
        return ajaxJsonEntity;
    }


    /**
     * 返回充值成功页面
     * @return
     */
    @RequestMapping("successPage")
    public String successPage(Long customerId,double amount,Model model){
        String customerName = "";
        Customer customer = customerService.getFromCache(customerId);
        if(customer!=null){
            customerName = customer.getName();
        }
        model.addAttribute("customerName",customerName);
        model.addAttribute("amount",amount);
        return "modules/fi/recharge/customerOfflineRechargeSuccess";
    }

    /**
     * 分页查询
     * @param offlineRechargeSearch
     * @return
     */
    @RequiresPermissions("fi:offlinerecharge:view")
    @RequestMapping(value = {"findPendingList", ""})
    public String findPendingList(CustomerOfflineRechargeSearch offlineRechargeSearch, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<CustomerOfflineRechargeModel> offlineRechargePage = new Page<>(request, response);
        if(StringUtils.isNotBlank(offlineRechargeSearch.getBeginDate())){
            Date date = DateUtils.parseDate(offlineRechargeSearch.getBeginDate());
            offlineRechargeSearch.setBeginDt(date.getTime());
        }
        if(StringUtils.isNotBlank(offlineRechargeSearch.getEndDate())){
            Date date = DateUtils.parseDate(offlineRechargeSearch.getEndDate());
            date = DateUtils.getDateEnd(date);
            offlineRechargeSearch.setEndDt(date.getTime());
        }
        offlineRechargeSearch.setSite(siteCode);
        Page<CustomerOfflineRechargeModel> page= customerOfflineRechargeService.findPendingList(offlineRechargePage,offlineRechargeSearch);
        model.addAttribute("offlineRechargeSearch",offlineRechargeSearch);
        model.addAttribute("page", page);
        return "modules/fi/recharge/list/pendingReviewList";
    }

    /**
     * 分页查询已审核线下充值审单列表
     * @param offlineRechargeSearch
     * @return
     */
    @RequiresPermissions("fi:offlinerecharge:view")
    @RequestMapping("findHasReviewList")
    public String findHasReviewList(CustomerOfflineRechargeSearch offlineRechargeSearch, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<CustomerOfflineRechargeModel> offlineRechargePage = new Page<>(request, response);
        Date date;
        if (StringUtils.isBlank(offlineRechargeSearch.getBeginDate())) {
            date = DateUtils.getDateEnd(new Date());
            offlineRechargeSearch.setEndDate(DateUtils.formatDate(date,"yyyy-MM-dd"));
            offlineRechargeSearch.setEndDt(date.getTime());
            date = DateUtils.getStartDayOfMonth(DateUtils.addMonth(new Date(), -1));
            offlineRechargeSearch.setBeginDate(DateUtils.formatDate(date,"yyyy-MM-dd"));
            offlineRechargeSearch.setBeginDt(date.getTime());
        } else {
            date = DateUtils.parseDate(offlineRechargeSearch.getBeginDate());
            offlineRechargeSearch.setBeginDt(date.getTime());
            date = DateUtils.parseDate(offlineRechargeSearch.getEndDate());
            date = DateUtils.getDateEnd(date);
            offlineRechargeSearch.setEndDt(date.getTime());
        }
        offlineRechargeSearch.setSite(siteCode);
        Page<CustomerOfflineRechargeModel> page= customerOfflineRechargeService.findHasReviewList(offlineRechargePage,offlineRechargeSearch);
        model.addAttribute("offlineRechargeSearch",offlineRechargeSearch);
        model.addAttribute("page", page);
        return "modules/fi/recharge/list/hasReviewList";
    }

    /**
     * 审核通过
     * @param customerOfflineRecharge
     * @return
     */
    @RequiresPermissions("fi:offlinerecharge:edit")
    @RequestMapping(value = {"approved"},method = RequestMethod.POST)
    @ResponseBody
    public AjaxJsonEntity approved(CustomerOfflineRecharge customerOfflineRecharge,HttpServletResponse response){
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        if(customerOfflineRecharge.getId()==null || customerOfflineRecharge.getId()<=0){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("充值单id丢失,请重试!");
            return ajaxJsonEntity;
        }
        User user = UserUtils.getUser();
        if(user == null || user.getId() == null || user.getId()<0){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("登录已超时，请刷新页面重新登录！");
            return ajaxJsonEntity;
        }
        if(customerOfflineRecharge.getActualAmount()<=0){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("实际到账金额不能小于0！");
            return ajaxJsonEntity;
        }
        if(StringUtils.isBlank(customerOfflineRecharge.getTransferNo())){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("交易单号不存在请检查！");
            return ajaxJsonEntity;
        }
        Boolean locked = false;
        String lockKey = StringUtils.EMPTY;
        String requestId = StringUtils.uuid();
        try {
            lockKey = String.format(RedisConstant.OFFLINE_RECHARGE,customerOfflineRecharge.getId());
            locked = redisUtils.getLock(lockKey, requestId, 60);
            if (!locked) {
                ajaxJsonEntity.setSuccess(false);
                ajaxJsonEntity.setMessage("充值单正在审核！请稍后重试");
                return ajaxJsonEntity;
            }
            customerOfflineRecharge.setStatus(CustomerOfflineRecharge.PASSED_STATUS);
            customerOfflineRecharge.setUpdateById(user.getId());
            customerOfflineRechargeService.approved(customerOfflineRecharge,user);
        }catch (Exception e){
            log.error("CustomerOfflineRechargeController.approved线下充值审核通过失败:{}", GsonUtils.toGsonString(customerOfflineRecharge),e);
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage(e.getMessage());
        }finally {
            //释放锁
            if(StringUtils.isNotBlank(lockKey) && locked){
                redisUtils.releaseLock(lockKey,requestId);
            }
        }
        return ajaxJsonEntity;
    }


    /**
     * 审核无效页面
     * @param customerOfflineRecharge
     * @return
     */
    @RequiresPermissions("fi:offlinerecharge:edit")
    @RequestMapping("invalidForm")
    public String invalidForm(CustomerOfflineRecharge customerOfflineRecharge,Model model){
        model.addAttribute("customerOfflineRecharge",customerOfflineRecharge);
        return "modules/fi/recharge/list/invalidForm";
    }

    /**
     * 审核无效页面
     * @param customerOfflineRecharge
     * @return
     */
    @RequiresPermissions("fi:offlinerecharge:edit")
    @RequestMapping("invalid")
    @ResponseBody
    public AjaxJsonEntity invalid(CustomerOfflineRecharge customerOfflineRecharge){
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        if(customerOfflineRecharge.getId()==null || customerOfflineRecharge.getId()<=0){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("充值单id丢失,请重试!");
            return ajaxJsonEntity;
        }
        User user = UserUtils.getUser();
        if(user == null || user.getId() == null || user.getId()<0){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("登录已超时，请刷新页面重新登录！");
            return ajaxJsonEntity;
        }
        try {
            customerOfflineRecharge.setStatus(CustomerOfflineRecharge.INVALID_STATUS);
            customerOfflineRecharge.setUpdateAt(new Date().getTime());
            customerOfflineRecharge.setUpdateById(user.getId());
            customerOfflineRechargeService.invalid(customerOfflineRecharge,user);
        }catch (Exception e){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage(e.getMessage());
        }
        return ajaxJsonEntity;
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

    /**
     * 获取当前登录客户的客户id
     * @return
     */
     @RequestMapping("getMoneyBack")
     @ResponseBody
     public AjaxJsonEntity getMoneyBack(double amount){
         AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
         double money = 0.0;
         try {
             List<Dict> dictList = MSDictUtils.getDictList("offline_recharge_back_ratio");
             money = customerOfflineRechargeService.getMoneyBack(amount,dictList);
             ajaxJsonEntity.setData(money);
         }catch (Exception e){
             ajaxJsonEntity.setSuccess(false);
             ajaxJsonEntity.setMessage("获取返现金额失败,"+ e.getMessage());
         }
         return ajaxJsonEntity;
     }



}
