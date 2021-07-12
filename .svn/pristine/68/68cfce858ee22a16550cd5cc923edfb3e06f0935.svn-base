package com.wolfking.jeesite.ms.praise.controller;

import com.google.common.collect.Lists;
import com.kkl.kklplus.entity.common.NameValuePair;
import com.kkl.kklplus.entity.md.MDCustomerPraiseFee;
import com.kkl.kklplus.entity.md.MDCustomerPraiseFeePraiseStandardItem;
import com.kkl.kklplus.entity.praise.Praise;
import com.kkl.kklplus.entity.praise.PraiseCreatorTypeEnum;
import com.kkl.kklplus.entity.praise.PraisePageSearchModel;
import com.kkl.kklplus.entity.praise.PraiseStatusEnum;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.config.Global;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.entity.viewModel.RegionSearchModel;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.KefuTypeEnum;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.SystemService;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.modules.utils.PraiseUtils;
import com.wolfking.jeesite.ms.praise.entity.PraiseLogModel;
import com.wolfking.jeesite.ms.praise.entity.ViewPraiseModel;
import com.wolfking.jeesite.ms.praise.service.OrderPraiseService;
import com.wolfking.jeesite.ms.providermd.service.MSCustomerPraiseFeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "${adminPath}/praise/orderPraise")
@Slf4j
public class OrderPraiseController extends BaseController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderPraiseService orderPraiseService;

    @Autowired
    private MSCustomerPraiseFeeService customerPraiseFeeService;

    @Autowired
    private SystemService systemService;

    /**
     * 好评费申请页面
     */
    @RequestMapping("praiseForm")
    public String praiseForm(Long orderId, Long customerId, String quarter, Model model) {
        model.addAttribute("canAction", true);
        model.addAttribute("canSave", false);
        Order order = orderService.getOrderById(orderId, quarter, OrderUtils.OrderDataLevel.DETAIL, true);
        if (order == null || order.getOrderCondition() == null) {
            Praise praise = new Praise();
            praise.setOrderId(orderId);
            praise.setQuarter(quarter);
            addMessage(model, "错误:读取订单失败,请重试！");
            model.addAttribute("canAction", false);
            model.addAttribute("praise", praise);
            return "modules/sd/praise/applyPraiseForm";
        }
        MDCustomerPraiseFee customerPraiseFee = customerPraiseFeeService.getByCustomerIdFromCacheNewForCP(customerId);
        List<MDCustomerPraiseFeePraiseStandardItem> praiseList = Lists.newArrayList();
        if (customerPraiseFee == null) {
            Praise praise = new Praise();
            praise.setOrderId(orderId);
            praise.setQuarter(quarter);
            addMessage(model, "错误:获取客户好评配置错误！");
            model.addAttribute("canAction", false);
            model.addAttribute("praise", praise);
            return "modules/sd/praise/applyPraiseForm";
        }else{
            praiseList = customerPraiseFee.getPraiseStandardItems();
        }
        customerPraiseFee.setPraiseRequirement
                (org.apache.commons.lang3.StringUtils.replace(customerPraiseFee.getPraiseRequirement(),"\n","<br>"));
        double servicePointFee = 0.0;
        if(customerPraiseFee.getPraiseFeeFlag()!=null && customerPraiseFee.getPraiseFeeFlag()==1){
            NameValuePair<Double,Double> nameValuePair = PraiseUtils.calculatePraiseCost(customerPraiseFee.getPraiseFee(),customerPraiseFee.getMaxPraiseFee(),customerPraiseFee.getDiscount(),null);
            servicePointFee = nameValuePair.getValue();
        }
        double servicePointMinFee = servicePointFee; //网点底价
        ServicePoint servicePoint = order.getOrderCondition().getServicePoint();
        Praise praise = orderPraiseService.getByOrderId(quarter, orderId, servicePoint.getId());
        if (praise != null) { //修改
            if (praise.getStatus() == PraiseStatusEnum.REJECT.code || praise.getStatus() == PraiseStatusEnum.NEW.code) {
                model.addAttribute("canSave", true);
            }
        } else {
            //添加
            praise = new Praise();
            praise.setOrderId(orderId);
            praise.setQuarter(quarter);
            model.addAttribute("canSave",true);
            String strProductName="";
            List<String> productNames = order.getItems().stream().map(t->t.getProduct().getName()).collect(Collectors.toList());
            strProductName = StringUtils.join(productNames,",");
            praise.setApplyServicepointPraiseFee(servicePointFee);
            praise.setApplyCustomerPraiseFee(customerPraiseFee.getPraiseFee());
            praise.setProductNames(strProductName);
            praise.setOrderNo(order.getOrderNo());
        }
        praise.setServicepointId(servicePoint.getId());
        String servicePointName = servicePoint.getName();
        if (order.getOrderCondition().getStatusValue() >= Order.ORDER_STATUS_CHARGED) {
            addMessage(model, "工单已完成");
            model.addAttribute("canSave", false);
        }
        model.addAttribute("praise", praise);
        model.addAttribute("customerPraiseFee", customerPraiseFee);
        model.addAttribute("servicePointName", servicePointName);
        model.addAttribute("praiseList",praiseList);
        model.addAttribute("servicePointMinFee",servicePointMinFee);
        return "modules/sd/praise/applyPraiseForm";
    }


    /**
     * 申请好评费
     */
    @RequestMapping("applyPraise")
    @ResponseBody
    public AjaxJsonEntity applyPraise(Praise praise, HttpServletRequest request) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        Order order = orderService.getOrderById(praise.getOrderId(), praise.getQuarter(), OrderUtils.OrderDataLevel.DETAIL, true);
        if (order == null || order.getOrderCondition() == null) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("读取订单失败,请重试！");
            return ajaxJsonEntity;
        }
        User user = UserUtils.getUser();
        if (user == null || user.getId() == null || user.getId() <= 0) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("读取当前用户失败,请重新登录");
            return ajaxJsonEntity;
        }
        try {
            MDCustomerPraiseFee customerPraiseFee = customerPraiseFeeService.getByCustomerIdFromCacheNewForCP(order.getOrderCondition().getCustomerId());
            if(customerPraiseFee==null){
                ajaxJsonEntity.setSuccess(false);
                ajaxJsonEntity.setMessage("读取好评配置信息失败,请重试");
                return ajaxJsonEntity;
            }
            if(customerPraiseFee.getPraiseFeeFlag()==0){ //如果客户好评配置停用,则好无好评费用
                praise.setApplyCustomerPraiseFee(0.0);
                praise.setApplyServicepointPraiseFee(0.0);
            }else {
                NameValuePair<Double,Double> praiseFee = OrderPraiseService.getPraiseFee(praise,customerPraiseFee);
                if(!praiseFee.getName().equals(praise.getApplyCustomerPraiseFee())){
                    ajaxJsonEntity.setSuccess(false);
                    ajaxJsonEntity.setMessage("申请厂商好评费不准确!请确认");
                    return ajaxJsonEntity;
                }
                if(!praiseFee.getValue().equals(praise.getApplyServicepointPraiseFee())){
                    ajaxJsonEntity.setSuccess(false);
                    ajaxJsonEntity.setMessage("申请网点好评费不准确!请确认");
                    return ajaxJsonEntity;
                }
            }
            if (praise.getId() != null && praise.getId() > 0) {
                orderPraiseService.updatePraise(praise, user, PraiseCreatorTypeEnum.KEFU.code);
            } else {
                orderPraiseService.saveApplyPraise(praise, order, user,PraiseCreatorTypeEnum.KEFU.code);
            }
        } catch (Exception e) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage(e.getMessage());
        }
        return ajaxJsonEntity;
    }


    /**
     * 客服待审核好评单列表
     */
    @RequiresPermissions("sd:praise:kefu:review")
    @RequestMapping(value = "pendingReviewList")
    public String pendingReviewList(PraisePageSearchModel praisePageSearchModel, HttpServletRequest request, HttpServletResponse response, Model model) {
        String viewForm = "modules/sd/praise/kefu/pendingReviewList";
        Page<ViewPraiseModel> page = new Page(request, response);
        User user = UserUtils.getUser();
        if (user == null || user.getId() == null) {
            addMessage(model, "错误：登录超时，请退出后重新登录。");
            model.addAttribute("page", page);
            model.addAttribute("praisePageSearchModel", praisePageSearchModel);
            return viewForm;
        }
        if (!user.isKefu() && !user.isAdmin()) {
            addMessage(model, "错误：无权限，此功能只开放给客服人员使用。");
            model.addAttribute("page", page);
            model.addAttribute("praisePageSearchModel", praisePageSearchModel);
            return viewForm;
        }
        if (user.isKefu()) {
            RegionSearchModel regionSearchModel = new RegionSearchModel();
            regionSearchModel.setArea(null);
            String checkRegion = orderPraiseService.loadAndCheckUserRegions(regionSearchModel, user);
            if (StringUtils.isNotBlank(checkRegion)) {
                addMessage(model, checkRegion);
                model.addAttribute("page", page);
                model.addAttribute("praisePageSearchModel", praisePageSearchModel);
                return viewForm;
            } else {
                praisePageSearchModel.setProvinceList(regionSearchModel.getProvinceList());
                praisePageSearchModel.setCityList(regionSearchModel.getCityList());
                praisePageSearchModel.setAreaList(regionSearchModel.getAreaList());
                int cnt = 0;
                if (!ObjectUtils.isEmpty(praisePageSearchModel.getProvinceList())) {
                    cnt++;
                }
                if (!ObjectUtils.isEmpty(praisePageSearchModel.getCityList())) {
                    cnt++;
                }
                if (!ObjectUtils.isEmpty(praisePageSearchModel.getAreaList())) {
                    cnt++;
                }
                praisePageSearchModel.setRegionFilterCount(cnt);
            }
            if (user.isKefu() && user.getSubFlag() == KefuTypeEnum.VIPKefu.getCode()) {
                //vip客服
                //praisePageSearchModel.setCustomerType(1);
                List<Long> customers = systemService.findVipCustomerIdListByKefu(user.getId());
                praisePageSearchModel.setCustomerIdList(customers);
            } else if (user.isKefu() && user.getSubFlag() == KefuTypeEnum.Kefu.getCode()) {
                //普通客服，不能查询vip客户订单
                praisePageSearchModel.setCustomerType(0);
            } else {
                //查所有客户
                praisePageSearchModel.setCustomerType(null);
            }
            List<Long> productCategoryIds = systemService.getAuthorizedProductCategoryIds(user.getId());
            praisePageSearchModel.setProductCategoryIds(productCategoryIds);
        }
        Date date;
        if (StringUtils.isBlank(praisePageSearchModel.getBeginDate())) {
            date = DateUtils.getDateEnd(new Date());
            praisePageSearchModel.setEndDate(DateUtils.formatDate(date, "yyyy-MM-dd"));
            praisePageSearchModel.setEndDt(date.getTime());
            date = DateUtils.getStartDayOfMonth(DateUtils.addMonth(new Date(), -1));
            praisePageSearchModel.setBeginDate(DateUtils.formatDate(date, "yyyy-MM-dd"));
            praisePageSearchModel.setBeginDt(date.getTime());
        } else {
            date = DateUtils.parseDate(praisePageSearchModel.getBeginDate());
            praisePageSearchModel.setBeginDt(date.getTime());
            date = DateUtils.parseDate(praisePageSearchModel.getEndDate());
            date = DateUtils.getDateEnd(date);
            praisePageSearchModel.setEndDt(date.getTime());
        }

        page = orderPraiseService.pendingReviewList(page, praisePageSearchModel);
        model.addAttribute("page", page);
        model.addAttribute("praisePageSearchModel", praisePageSearchModel);
        return viewForm;
    }


    /**
     * 好评单审核
     *
     * @param id
     * @param quarter
     * @param model
     */
    @RequiresPermissions("sd:praise:kefu:review")
    @RequestMapping(value = "praiseReview")
    public String praiseReview(Long id, String quarter, Model model) {
        String viewForm = "modules/sd/praise/kefu/praiseReviewForm";
        model.addAttribute("canAction", true);
        model.addAttribute("canSave", false);
        ViewPraiseModel praiseModel = orderPraiseService.getPraiseInfoForReview(id, quarter);
        if (praiseModel == null) {
            addMessage(model, "读取好评费失败,请重新尝试!");
            model.addAttribute("canAction", false);
            return viewForm;
        }
        if (praiseModel.getStatus() != PraiseStatusEnum.PENDING_REVIEW.code) {
            addMessage(model, "好评单已审核,请确认!");
            model.addAttribute("canAction", false);
            return viewForm;
        }

        MDCustomerPraiseFee customerPraiseFee = customerPraiseFeeService.getByCustomerIdFromCacheNewForCP(praiseModel.getCustomerId());
        if(customerPraiseFee==null){
            addMessage(model,"错误,读取客户好评配置失败,请重新尝试!");
            model.addAttribute("canAction",false);
            return "modules/sd/praise/salesPraiseList/praiseInfoForSalesForm";
        }
        customerPraiseFee.setPraiseRequirement
                (org.apache.commons.lang3.StringUtils.replace(customerPraiseFee.getPraiseRequirement(),"\n","<br>"));
        model.addAttribute("canSave", true);
        List<PraiseLogModel> praiseLogModelList = orderPraiseService.finPraiseLogList(quarter, id);
        if (praiseLogModelList == null) {
            praiseLogModelList = Lists.newArrayList();
        }
        model.addAttribute("praise", praiseModel);
        model.addAttribute("customerPraiseFee", customerPraiseFee);
        model.addAttribute("praiseLogModelList", praiseLogModelList);
        return viewForm;
    }

    /**
     * 通过好评单审核
     *
     * @param praise
     */
    @RequiresPermissions("sd:praise:kefu:review")
    @RequestMapping("approve")
    @ResponseBody
    public AjaxJsonEntity approve(Praise praise, HttpServletRequest request) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        User user = UserUtils.getUser();
        if (user == null) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("您的帐号登录已超时！请重新登录！");
            return ajaxJsonEntity;
        }
        try {
            praise.setServicepointPraiseFee(0.00);
            praise.setCustomerPraiseFee(0.00);
            orderPraiseService.approve(praise, user);
        } catch (Exception e) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage(ExceptionUtils.getRootCauseMessage(e));
        }
        return ajaxJsonEntity;
    }


    /**
     * 驳回好评单审核
     *
     * @param praise
     */
    @RequiresPermissions("sd:praise:kefu:review")
    @RequestMapping("reject")
    @ResponseBody
    public AjaxJsonEntity reject(Praise praise) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        Order order = orderService.getOrderById(praise.getOrderId(), praise.getQuarter(), OrderUtils.OrderDataLevel.CONDITION, true);
        if (order == null) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("读取工单信息失败,请重试");
            return ajaxJsonEntity;
        }
        User user = UserUtils.getUser();
        if (user == null) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("您的帐号登录已超时！请重新登录！");
            return ajaxJsonEntity;
        }
        try {
            orderPraiseService.reject(praise, order, user);
        } catch (Exception e) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage(ExceptionUtils.getRootCauseMessage(e));
        }
        return ajaxJsonEntity;
    }


    /**
     * 取消好评单审核
     *
     * @param praise
     */
    @RequiresPermissions("sd:praise:kefu:review")
    @RequestMapping("cancelled")
    @ResponseBody
    public AjaxJsonEntity cancelled(Praise praise) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        User user = UserUtils.getUser();
        if (user == null) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("您的帐号登录已超时！请重新登录！");
            return ajaxJsonEntity;
        }
        try {
            orderPraiseService.cancelled(praise, user);
        } catch (Exception e) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage(ExceptionUtils.getRootCauseMessage(e));
        }
        return ajaxJsonEntity;
    }

    /**
     * 客服查询无费用已通过好评单列表
     * @param praisePageSearchModel
     * @param request
     */
    @RequiresPermissions("sd:praise:kefu:review")
    @RequestMapping(value = "noFeesApprovedList")
    public String noFeesApprovedList(PraisePageSearchModel praisePageSearchModel, HttpServletRequest request, HttpServletResponse response, Model model) {
        String viewForm = "modules/sd/praise/kefu/noFeeApprovedList";
        Page<ViewPraiseModel> page = new Page(request, response);
        User user = UserUtils.getUser();
        if (user == null || user.getId() == null) {
            addMessage(model, "错误：登录超时，请退出后重新登录。");
            model.addAttribute("page", page);
            model.addAttribute("praisePageSearchModel", praisePageSearchModel);
            return viewForm;
        }
        if (!user.isKefu() && !user.isAdmin()) {
            addMessage(model, "错误：无权限，此功能只开放给客服人员使用。");
            model.addAttribute("page", page);
            model.addAttribute("praisePageSearchModel", praisePageSearchModel);
            return viewForm;
        }
        praisePageSearchModel = setKefuSearchModel(model,user,praisePageSearchModel,1,265);
        if(!praisePageSearchModel.getIsValid()){
            model.addAttribute("page", page);
            model.addAttribute("praisePageSearchModel", praisePageSearchModel);
            return viewForm;
        }
        page = orderPraiseService.noFeesApprovedKefuList(page,praisePageSearchModel);
        model.addAttribute("page", page);
        model.addAttribute("praisePageSearchModel", praisePageSearchModel);
        return viewForm;
    }

    /**
     * 客服查询无效好评单列表
     * @param praisePageSearchModel
     * @param request
     */
    @RequiresPermissions("sd:praise:kefu:review")
    @RequestMapping(value = "invalidationList")
    public String invalidationList(PraisePageSearchModel praisePageSearchModel, HttpServletRequest request, HttpServletResponse response, Model model) {
        String viewForm = "modules/sd/praise/kefu/invalidationList";
        Page<ViewPraiseModel> page = new Page(request, response);
        User user = UserUtils.getUser();
        if (user == null || user.getId() == null) {
            addMessage(model, "错误：登录超时，请退出后重新登录。");
            model.addAttribute("page", page);
            model.addAttribute("praisePageSearchModel", praisePageSearchModel);
            return viewForm;
        }
        if (!user.isKefu() && !user.isAdmin()) {
            addMessage(model, "错误：无权限，此功能只开放给客服人员使用。");
            model.addAttribute("page", page);
            model.addAttribute("praisePageSearchModel", praisePageSearchModel);
            return viewForm;
        }
        praisePageSearchModel = setKefuSearchModel(model,user,praisePageSearchModel,1,265);
        if(!praisePageSearchModel.getIsValid()){
            model.addAttribute("page", page);
            model.addAttribute("praisePageSearchModel", praisePageSearchModel);
            return viewForm;
        }
        page = orderPraiseService.invalidationKefuList(page,praisePageSearchModel);
        model.addAttribute("page", page);
        model.addAttribute("praisePageSearchModel", praisePageSearchModel);
        return viewForm;
    }


    /**
     * 客服查看好评单详情
     * @param id
     * @param quarter
     * @return
     */
    @RequiresPermissions("sd:praise:kefu:review")
    @RequestMapping(value = "invalidPraiseKefuForm")
    public String getPraiseInfoForKefu(Long id,String quarter,Model model){
        model.addAttribute("canAction",true);
        ViewPraiseModel praiseModel = orderPraiseService.getPraiseInfoForReview(id,quarter);
        if(praiseModel==null){
            addMessage(model,"读取好评费失败,请重新尝试!");
            model.addAttribute("canAction",false);
            return "modules/sd/praise/kefu/invalidPraiseKefuForm";
        }
        if(praiseModel.getStatus()!=PraiseStatusEnum.APPROVE.code && praiseModel.getStatus()!=PraiseStatusEnum.INVALIDATION.code){
            addMessage(model,"错误:好评单状态不正确!请检查");
            model.addAttribute("canAction",false);
            return "modules/sd/praise/kefu/invalidPraiseKefuForm";
        }
   /*     double timeDiffer = DateUtils.differTime(praiseModel.getUpdateDt(),System.currentTimeMillis());
        if(timeDiffer>OrderPraiseService.TIME_HOUR){ //大于24小时
            praiseModel.setOvertimeFlag(ViewPraiseModel.HAS_OVERTIME_FLAG);
        }*/
        List<PraiseLogModel> praiseLogModelList = orderPraiseService.finPraiseLogList(quarter,praiseModel.getId());
        if(praiseLogModelList==null){
            praiseLogModelList = Lists.newArrayList();
        }
        model.addAttribute("praise",praiseModel);
        model.addAttribute("praiseLogModelList",praiseLogModelList);
        return "modules/sd/praise/kefu/invalidPraiseKefuForm";
    }

    /**
     * 客服设置无费用好评无效
     * @param praise
     * @return
     */
    @RequiresPermissions("sd:praise:kefu:review")
    @ResponseBody
    @RequestMapping(value = "invalidPraise")
    public AjaxJsonEntity invalidPraise(Praise praise){
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        User user = UserUtils.getUser();
        if(user==null){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("当前用户不存在,请重新登陆");
            return ajaxJsonEntity;
        }
        try {
            orderPraiseService.invalidPraise(praise,user);
        }catch (Exception e){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage(e.getMessage());
        }
        return ajaxJsonEntity;
    }

    /**
     * 设置必须的查询条件
     * @param user  当前帐号
     * @param searchModel   查询条件
     * @param initMonths    初始最小查询时间段(月)
     * @param maxDays   最大查询范围(天)
     *
     */
    private PraisePageSearchModel setKefuSearchModel(Model model, User user, PraisePageSearchModel searchModel, int initMonths, int maxDays) {
        if (searchModel == null) {
            searchModel = new PraisePageSearchModel();
        }
        if(!checkOrderNoAndPhone(searchModel,model)){
            searchModel.setIsValid(false);
            return searchModel;
        }
        if (user.isKefu()) {
            RegionSearchModel regionSearchModel = new RegionSearchModel();
            regionSearchModel.setArea(null);
            String checkRegion = orderPraiseService.loadAndCheckUserRegions(regionSearchModel, user);
            if (StringUtils.isNotBlank(checkRegion)) {
                addMessage(model, checkRegion);
                searchModel.setIsValid(false);
                return searchModel;
            } else {
                searchModel.setProvinceList(regionSearchModel.getProvinceList());
                searchModel.setCityList(regionSearchModel.getCityList());
                searchModel.setAreaList(regionSearchModel.getAreaList());
                int cnt = 0;
                if (!ObjectUtils.isEmpty(searchModel.getProvinceList())) {
                    cnt++;
                }
                if (!ObjectUtils.isEmpty(searchModel.getCityList())) {
                    cnt++;
                }
                if (!ObjectUtils.isEmpty(searchModel.getAreaList())) {
                    cnt++;
                }
                searchModel.setRegionFilterCount(cnt);
            }
            //1.by 客户，前端客户已按客服筛选了
            if(user.isKefu()){
                if(user.getSubFlag() == KefuTypeEnum.VIPKefu.getCode()){
                    //vip客服
                    List<Long> customers = systemService.findVipCustomerIdListByKefu(user.getId());
                    searchModel.setCustomerIdList(customers);
                    searchModel.setKefuType(KefuTypeEnum.VIPKefu.getKefuType());//忽略突击区域
                } else if(user.getSubFlag() == KefuTypeEnum.Kefu.getCode()){
                    //普通客服
                    searchModel.setCustomerType(0);//指派客户，关联sys_user_customer
                    searchModel.setKefuType(KefuTypeEnum.Kefu.getKefuType());
                }else if (user.getSubFlag() == KefuTypeEnum.Rush.getCode()){
                    //突击客服，只看自己负责的单
                    searchModel.setCustomerType(null);//不能查询vip客户订单
                    searchModel.setKefuType(KefuTypeEnum.Rush.getKefuType());
                } else if(user.getSubFlag() == KefuTypeEnum.AutomaticKefu.getCode()){
                    searchModel.setCustomerType(null);//指派客户，不关联sys_user_customer
                    searchModel.setKefuType(KefuTypeEnum.AutomaticKefu.getKefuType());
                }else if(user.getSubFlag() == KefuTypeEnum.COMMON_KEFU.getCode()){
                    searchModel.setCustomerType(null);//指派客户，不关联sys_user_customer
                    searchModel.setKefuType(KefuTypeEnum.COMMON_KEFU.getKefuType());
                }else {//超级客服，查询所有客户订单
                    //超级客服，查询所有客户订单，包含Vip客户
                    searchModel.setCustomerType(null); //可查看Vip客户订单
                    searchModel.setKefuType(null);//可查看突击区域订单
                }
            } else {
                //其他类型帐号，不限制客户及突击区域订单
                searchModel.setCustomerType(null);
                searchModel.setKefuType(null);//可查看突击区域订单
            }
            boolean isServiceSupervisor = user.getRoleEnNames().contains("Customer service supervisor");//客服主管
            if (isServiceSupervisor) {
                searchModel.setCreateBy(user);//*
            } else if (user.isKefu()) {
                searchModel.setCreateBy(user);//*,只有客服才按帐号筛选
            } else if (user.isInnerAccount()) { //内部帐号
                searchModel.setCreateBy(user);//*
            }
            List<Long> productCategoryIds = systemService.getAuthorizedProductCategoryIds(user.getId());
            searchModel.setProductCategoryIds(productCategoryIds);
        }
        Date date;
        Date now = new Date();
        if (StringUtils.isBlank(searchModel.getBeginDate())) {
            date = DateUtils.getDateEnd(now);
            searchModel.setEndDate(DateUtils.formatDate(date, "yyyy-MM-dd"));
            searchModel.setEndDt(date.getTime());
            date = DateUtils.getStartDayOfMonth(DateUtils.addMonth(now, 0 - initMonths));
            searchModel.setBeginDate(DateUtils.formatDate(date, "yyyy-MM-dd"));
            searchModel.setBeginDt(date.getTime());
        } else {
            date = DateUtils.parseDate(searchModel.getBeginDate());
            searchModel.setBeginDt(date.getTime());
            date = DateUtils.parseDate(searchModel.getEndDate());
            date = DateUtils.getDateEnd(date);
            searchModel.setEndDt(date.getTime());
        }

        //检查最大时间范围
        if(maxDays > 0){
            date = DateUtils.parseDate(searchModel.getBeginDate());
            Date maxDate = DateUtils.addDays(date,maxDays-1);
            maxDate = DateUtils.getDateEnd(maxDate);
            date = DateUtils.parseDate(searchModel.getEndDate());
            if(DateUtils.pastDays(maxDate,date)>0){
                searchModel.setEndDate(DateUtils.formatDate(maxDate,"yyyy-MM-dd"));
                searchModel.setEndDt(maxDate.getTime());
            }
        }

        return searchModel;
    }

    /**
     * 检查订单号，手机号输入
     * @param searchModel
     * @param model
     * @return
     */
    private Boolean checkOrderNoAndPhone(PraisePageSearchModel searchModel, Model model){
        if(searchModel == null){
            return true;
        }
        int orderNoSearchType = 0;
        searchModel.setOrderNo(StringUtils.trimToEmpty(searchModel.getOrderNo()));
        if (StringUtils.isNotBlank(searchModel.getOrderNo())){
            String orderNoPrefix = Global.getConfig("OrderPrefix");
            if(!searchModel.getOrderNo().startsWith(orderNoPrefix)) {
                orderNoSearchType = 3;//前缀错误
            }else if (searchModel.getOrderNo().length() == 14) {//长度
                orderNoSearchType = 1;
                String quarter = QuarterUtils.getOrderQuarterFromNo(searchModel.getOrderNo());
                if (StringUtils.isNotBlank(quarter)) {
                    searchModel.setQuarter(quarter);
                }
            }else{
                orderNoSearchType = 2;//前缀正常
            }
        }
        searchModel.setOrderNoSearchType(orderNoSearchType);
        if (orderNoSearchType > 2){
            addMessage(model, "错误：请输入正确的工单单号");
            return false;
        }
        //检查订单号
        if(orderNoSearchType == 1) {
            //检查分片
            try {
                Date goLiveDate = OrderUtils.getGoLiveDate();
                String[] quarters = DateUtils.getQuarterRange(goLiveDate, new Date());
                if (quarters.length == 2) {
                    int start = StringUtils.toInteger(quarters[0]);
                    int end = StringUtils.toInteger(quarters[1]);
                    if (start > 0 && end > 0) {
                        int quarter = StringUtils.toInteger(searchModel.getQuarter());
                        if (quarter < start || quarter > end) {
                            addMessage(model, "错误：请输入正确的工单单号,日期超出范围");
                            return false;
                        }
                    }
                }
            } catch (Exception e) {
                log.error("检查分片错误,orderNo:{}", searchModel.getOrderNo(), e);
            }
        }
        if (StringUtils.isNotBlank(searchModel.getUserPhone())){
            if(!"".equalsIgnoreCase(StringUtils.isPhoneWithRelaxed(searchModel.getUserPhone()))){
                addMessage(model, "错误：请输入正确的电话");
                return false;
            }
        }
        return true;
    }


}
