package com.wolfking.jeesite.modules.servicepoint.sd.web;


import com.google.common.collect.Lists;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.entity.md.MDErrorCode;
import com.kkl.kklplus.entity.md.MDErrorType;
import com.kkl.kklplus.entity.md.dto.MDActionCodeDto;
import com.kkl.kklplus.entity.praise.PraiseStatusEnum;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.exception.OrderException;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.IntegerRange;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.*;
import com.wolfking.jeesite.modules.md.service.ProductCategoryService;
import com.wolfking.jeesite.modules.md.service.ServicePointService;
import com.wolfking.jeesite.modules.md.service.ServiceTypeService;
import com.wolfking.jeesite.modules.md.service.UrgentLevelService;
import com.wolfking.jeesite.modules.sd.entity.*;
import com.wolfking.jeesite.modules.sd.entity.viewModel.HistoryPlanOrderModel;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderSearchModel;
import com.wolfking.jeesite.modules.sd.entity.viewModel.RegionSearchModel;
import com.wolfking.jeesite.modules.sd.service.*;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.servicepoint.sd.service.ServicePointProcessOrderListService;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.KefuTypeEnum;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.SystemService;
import com.wolfking.jeesite.modules.sys.utils.AreaUtils;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providermd.service.*;
import com.wolfking.jeesite.ms.tmall.md.entity.B2bCustomerMap;
import com.wolfking.jeesite.ms.tmall.md.service.B2bCustomerMapService;
import com.wolfking.jeesite.ms.tmall.sd.entity.TmallAnomalyRecourse;
import com.wolfking.jeesite.ms.tmall.sd.entity.TmallServiceMonitor;
import com.wolfking.jeesite.ms.tmall.sd.entity.ViewModel.TmallAnomalyRecourseSearchVM;
import com.wolfking.jeesite.ms.tmall.sd.entity.ViewModel.TmallServiceMonitorSearchVM;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * 网点订单列表
 * 复制客服订单列表
 *
 * @author Ryan
 * @date 2021/01/05
 *
 */
@Controller
@RequestMapping(value = "${adminPath}/servicePoint/sd/processOrderList/")
@Slf4j
public class ServicePointProcessOrderListController extends BaseController {

    private static final int DEFAULT_PAGE_SIZE = 12;

    @Autowired
    private ServicePointProcessOrderListService processOrderListService;

    @Autowired
    private UrgentLevelService urgentLevelService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ServicePointService servicePointService;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private OrderVoiceTaskService orderVoiceTaskService;

    @Autowired
    private OrderAuxiliaryMaterialService orderAuxiliaryMaterialService;

	@Autowired
    private MSCustomerErrorTypeService msCustomerErrorTypeService;
    @Autowired
    private MSErrorCodeService msErrorCodeService;
    @Autowired
    private MSActionCodeService msActionCodeService;
    @Autowired
    private ServiceTypeService serviceTypeService;
    @Autowired
    private B2bCustomerMapService b2bCustomerMapService;
    @Autowired
    private OrderStatusFlagService orderStatusFlagService;
    @Autowired
    private MSCustomerProductService msCustomerProductService;
    @Autowired
    private MSRegionPermissionService msRegionPermissionService;
    @Autowired
    private SystemService systemService;
    @Autowired
    private ProductCategoryService productCategoryService;


    private static final String MODEL_ATTR_PAGE = "page";
    private static final String MODEL_ATTR_ORDER = "order";

    private static final String VIEW_NAME_PLANNING_LIST = "modules/servicePoint/sd/processOrderList/planingList";
    private static final String VIEW_NAME_NOAPPOINTMENT_LIST = "modules/servicePoint/sd/processOrderList/noAppointmentList";
    private static final String VIEW_NAME_ARRIVEAPPOINTMENT_LIST = "modules/servicePoint/sd/processOrderList/arriveAppointmentList";
    private static final String VIEW_NAME_PASSAPPOINTMENT_LIST = "modules/servicePoint/sd/processOrderList/passAppointmentList";
    private static final String VIEW_NAME_PENDING_LIST = "modules/servicePoint/sd/processOrderList/pendingList";
    private static final String VIEW_NAME_SERVICED_LIST = "modules/servicePoint/sd/processOrderList/servicedList";
    private static final String VIEW_NAME_FOLLOWUP_FAIL_LIST = "modules/servicePoint/sd/processOrderList/followUpFailList";
    private static final String VIEW_NAME_UMCOMPLETED_LIST = "modules/servicePoint/sd/processOrderList/uncompletedList";
    private static final String VIEW_NAME_ALL_LIST = "modules/servicePoint/sd/processOrderList/allList";
    private static final String VIEW_NAME_COMPLETED_LIST = "modules/servicePoint/sd/processOrderList/completedList";
    private static final String VIEW_NAME_REMINDER_LIST = "modules/servicePoint/sd/processOrderList/reminderList";

    private static final String VIEW_NAME_TMALLANOMALY_LIST = "modules/servicePoint/sd/processOrderList/tmallAnomalyList";
    private static final String VIEW_NAME_TMALLSERVICEMONITOR_LIST = "modules/servicePoint/sd/processOrderList/tmallServiceMonitorList";
    private static final String VIEW_NAME_RUSHING_LIST = "modules/servicePoint/sd/processOrderList/rushingList";
    private static final String VIEW_NAME_COMPLAIN_LIST = "modules/servicePoint/sd/processOrderList/complainList";

    private static final String VIEW_NAME_BROKE_APPOINTMENT_ORDER_LIST = "modules/servicePoint/sd/processOrderList/reservationList";

    //region 辅助方法

    /**
     * 设置必须的查询条件
     * @param user  当前帐号
     * @param searchModel   查询条件
     * @param initMonths    初始最小查询时间段(月)
     * @param searchByOrderDateRange by下单日期查询开关
     * @param maxOrderDays   下单最大查询范围(天)
     * @param searchByCompleteDateRange by完成日期查询开关
     * @param maxCompleteDays 完成最大查询范围(天)
     */
    private OrderSearchModel setSearchModel(User user,OrderSearchModel searchModel,Model model ,
                                            int initMonths,boolean searchByOrderDateRange ,int maxOrderDays,
                                            boolean searchByCompleteDateRange,int maxCompleteDays) {
        if (searchModel == null) {
            searchModel = new OrderSearchModel();
        }
        /* 当前网点 暂不使用
        ServicePoint point = getCurrentServicePoint();
        searchModel.setServicePoint(point);
        */
        Area area = searchModel.getArea();
        if(area == null){
            area = new Area(0L);
            searchModel.setArea(area);
        }
        if(area.getParent()==null || area.getParent().getId() == null){
            area.setParent(new Area(0L));
        }
        String checkRegion = processOrderListService.loadAndCheckUserRegions(searchModel,user);
        if(StringUtils.isNotEmpty(checkRegion)){
            addMessage(model, checkRegion);
            searchModel.setValid(false);
            return searchModel;
        }
        Date now = new Date();
        //下单日期
        if(searchByOrderDateRange) {
            if (searchModel.getBeginDate() == null) {
                searchModel.setEndDate(DateUtils.getDateEnd(new Date()));
                searchModel.setBeginDate(DateUtils.getStartDayOfMonth(DateUtils.addMonth(new Date(), 0 - initMonths)));
            } else {
                searchModel.setEndDate(DateUtils.getDateEnd(searchModel.getEndDate()));
            }
            //检查最大时间范围
            if(maxOrderDays > 0) {
                Date maxDate = DateUtils.addDays(searchModel.getEndDate(), 0 - maxOrderDays);
                maxDate = DateUtils.getDateStart(maxDate);
                if (DateUtils.pastDays(searchModel.getBeginDate(), maxDate) > 0) {
                    searchModel.setBeginDate(maxDate);
                }
            }
        }
        //完成日期
        if(searchByCompleteDateRange){
            if (searchModel.getCompleteEnd() != null) {
                searchModel.setCompleteEnd(DateUtils.getDateEnd(searchModel.getCompleteEnd()));
            }
            //检查最大时间范围
            if(maxCompleteDays > 0 && searchModel.getCompleteBegin() != null){
                Date maxDate = DateUtils.addDays(searchModel.getCompleteBegin(),maxCompleteDays-1);
                maxDate = DateUtils.getDateEnd(maxDate);
                if(searchModel.getCompleteEnd() == null){
                    searchModel.setCompleteEnd(DateUtils.getDateEnd(now));
                }
                if(DateUtils.pastDays(maxDate,searchModel.getCompleteEnd())>0){
                    searchModel.setCompleteEnd(maxDate);
                }
            }
        }
        //账号品类
        Boolean loadResult = loadUserCategories(model,searchModel,user.getId());
        if(!loadResult){
            searchModel.setValid(false);
            return searchModel;
        }
        //按订单号或电话号码查询
        Page<Order> page = new Page<>();
        Boolean isValide = checkOrderNoAndPhone(searchModel,model,page);
        if(!isValide){
            searchModel.setValid(false);
            return searchModel;
        }
        loadB2BShops(searchModel);
        return searchModel;
    }

    /**
     * 检查订单号，手机号输入
     * @param searchModel
     * @param model
     * @return
     */
    private Boolean checkOrderNoAndPhone(OrderSearchModel searchModel,Model model,Page<Order> page){
        if(searchModel == null){
            return true;
        }
        //检查电话
        int orderSerchType = searchModel.getOrderNoSearchType();
        if (StringUtils.isNotBlank(searchModel.getOrderNo())){
            if(orderSerchType != 1) {
                addMessage(model, "错误：请输入正确的订单号码");
                model.addAttribute(MODEL_ATTR_PAGE, page);
                model.addAttribute(MODEL_ATTR_ORDER, searchModel);
                return false;
            }else{
                //检查分片
                try {
                    Date goLiveDate = OrderUtils.getGoLiveDate();
                    String[] quarters = DateUtils.getQuarterRange(goLiveDate, new Date());
                    if(quarters.length == 2) {
                        int start = StringUtils.toInteger(quarters[0]);
                        int end = StringUtils.toInteger(quarters[1]);
                        if(start>0 && end > 0){
                            int quarter = StringUtils.toInteger(searchModel.getQuarter());
                            if(quarter < start || quarter > end){
                                addMessage(model, "错误：请输入正确的订单号码");
                                model.addAttribute(MODEL_ATTR_PAGE, page);
                                model.addAttribute(MODEL_ATTR_ORDER, searchModel);
                                return false;
                            }
                        }
                    }
                }catch (Exception e){
                    log.error("检查分片错误,orderNo:{}",searchModel.getOrderNo(),e);
                }
            }
        }
        if (StringUtils.isNotBlank(searchModel.getPhone1())){
            if(searchModel.getIsPhone() != 1){
                addMessage(model, "错误：请输入正确的用户电话");
                model.addAttribute(MODEL_ATTR_PAGE, page);
                model.addAttribute(MODEL_ATTR_ORDER, searchModel);
                return false;
            }
        }
        return true;
    }

    /**
     * 按客户+数据源装载店铺列表
     */
    private void loadB2BShops(OrderSearchModel searchModel){
        //b2b
        if (searchModel.getCustomer() != null && searchModel.getCustomer().getId() != null && searchModel.getCustomer().getId() > 0
                && searchModel.getDataSource() >0 && B2BDataSourceEnum.isDataSource(searchModel.getDataSource())) {
            List<B2bCustomerMap> shopList = b2bCustomerMapService.getShopListByCustomerNew(searchModel.getDataSource(), searchModel.getCustomer().getId());
            searchModel.setShopList(shopList);
        }
    }

    /**
     * 装载加急等级列表
     */
    private void loadUrgentLevels(OrderSearchModel searchModel){
        List<UrgentLevel> urgentLevels = urgentLevelService.findAllList();
        searchModel.setUrgentLevels(urgentLevels);
    }

    private Boolean loadUserCategories(Model model, RegionSearchModel searchModel, Long userId) {
        List<ProductCategory> categories = productCategoryService.findAllList();
        if (categories == null) {
            categories = Lists.newArrayList();
            searchModel.setQueryAllCategory(0);
        } else {
            if(categories.size()>0) {
                categories = categories.stream().filter(t -> t.getDelFlag().equals(0)).collect(Collectors.toList());
            }
        }
        List<Long> categoryList = systemService.getAuthorizedProductCategoryIds(userId);
        if(ObjectUtils.isEmpty(categoryList)){
            addMessage(model, "您未开通产品类目权限，请联系管理员");
            return false;
        }
        //只能看到自己的品类
        if(!ObjectUtils.isEmpty(categoryList)){
            searchModel.setUserCategoryList(categoryList);
            searchModel.setQueryAllCategory(0);
            if(!ObjectUtils.isEmpty(categories)){
                categories = categories.stream().filter(t-> categoryList.contains(t.getId())).collect(Collectors.toList());
            }
        }
        model.addAttribute("categories", categories);
        return true;
    }

    private ServicePoint getCurrentServicePoint() {
        ServicePoint servicePoint = null;
        User currentUser = UserUtils.getUser();
        if (currentUser.isEngineer() && currentUser.getCompanyId() > 0) {
            servicePoint = servicePointService.getFromCache(currentUser.getCompanyId());
        }
        return servicePoint;
    }

    //endregion 辅助方法

    //region 普通列表

    /**
     * 待接单/待派单
     */
    @RequiresPermissions(value =
            {"sd:order:accept", "sd:order:plan", "sd:order:complete",
                    "sd:order:return", "sd:order:grade", "sd:order:feedback"}, logical = Logical.OR)
    @RequestMapping(value = "planinglist")
    public String planingList(OrderSearchModel order, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<Order> page = new Page<>();
        User user = UserUtils.getUser();
        order = setSearchModel(user,order,model,3,true,365,false,0);
        if(!order.getValid()){
            model.addAttribute(MODEL_ATTR_PAGE, page);
            model.addAttribute(MODEL_ATTR_ORDER, order);
            return VIEW_NAME_PLANNING_LIST;
        }
        try {
            order.setPage(new Page<>(request, response, DEFAULT_PAGE_SIZE));
            order.setNow(new Date());
            if (order.getBeginDate() != null) {
                Date[] dates = OrderUtils.getQuarterDates(order.getBeginDate(), order.getEndDate(), 0, 0);
                List<String> quarters = QuarterUtils.getQuarters(dates[0], dates[1]);
                if (quarters != null && quarters.size() > 0) {
                    order.setQuarters(quarters);
                }
            }
            page = processOrderListService.findKefuPlaningOrderList(order, true);
        } catch (Exception e) {
            addMessage(model, "错误：" + e.getMessage());
        }
        model.addAttribute(MODEL_ATTR_PAGE, page);
        model.addAttribute(MODEL_ATTR_ORDER, order);
        return VIEW_NAME_PLANNING_LIST;
    }

    /**
     * 未预约
     */
    @RequiresPermissions(value =
            {"sd:order:accept", "sd:order:plan", "sd:order:complete",
                    "sd:order:return", "sd:order:grade", "sd:order:feedback"}, logical = Logical.OR)
    @RequestMapping(value = "noAppointmentList")
    public String noAppointmentList(OrderSearchModel order, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<Order> page = new Page<>();
        User user = UserUtils.getUser();
        order = setSearchModel(user,order,model,3,true,365,false,0);
        if(!order.getValid()){
            model.addAttribute(MODEL_ATTR_PAGE, page);
            model.addAttribute(MODEL_ATTR_ORDER, order);
            return VIEW_NAME_PLANNING_LIST;
        }
        try {
            //加急
            loadUrgentLevels(order);
            Date now = new Date();
            Page<OrderSearchModel> parPage = new Page<>(request, response, DEFAULT_PAGE_SIZE);
            order.setPage(parPage);
            order.setNow(now);
            order.setPlanDateBegin(DateUtils.addHour(now, -2));
            if (order.getBeginDate() != null) {
                Date[] dates = OrderUtils.getQuarterDates(order.getBeginDate(), order.getEndDate(), 0, 0);
                List<String> quarters = QuarterUtils.getQuarters(dates[0], dates[1]);
                if (quarters != null && quarters.size() > 0) {
                    order.setQuarters(quarters);
                }
            }
            page = processOrderListService.findKefuNoAppointmentOrderList(order, true);
        } catch (Exception e) {
            addMessage(model, "错误：" + e.getMessage());
        }
        model.addAttribute(MODEL_ATTR_PAGE, page);
        model.addAttribute(MODEL_ATTR_ORDER, order);

        //记录反馈，反馈内容条件搜索频率情况
        if(order.getReplyFlagCustomer()==2){
            LogUtils.saveLog(request,null,null,"网点工单,未预约列表,未回复反馈搜索查询","kefuNoAppointmentList",user);
        }
        if(StringUtils.isNotBlank(order.getRemarks())){
            LogUtils.saveLog(request,null,null,"网点工单,未预约列表,反馈内容搜索查询","kefuNoAppointmentList",user);
        }

        return VIEW_NAME_NOAPPOINTMENT_LIST;

    }

    /**
     * 预约到期
     */
    @RequiresPermissions(value =
            {"sd:order:accept", "sd:order:plan", "sd:order:complete",
                    "sd:order:return", "sd:order:grade", "sd:order:feedback"}, logical = Logical.OR)
    @RequestMapping(value = "arriveAppointmentList")
    public String arriveAppointmentList(OrderSearchModel order, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<Order> page = new Page<>();
        User user = UserUtils.getUser();
        order = setSearchModel(user,order,model,3,true,365,false,0);
        if(!order.getValid()){
            model.addAttribute(MODEL_ATTR_PAGE, page);
            model.addAttribute(MODEL_ATTR_ORDER, order);
            return VIEW_NAME_PLANNING_LIST;
        }
        try {
            //加急
            loadUrgentLevels(order);
            Page<OrderSearchModel> parPage = new Page<>(request, response, DEFAULT_PAGE_SIZE);
            order.setPage(parPage);
            Date now = new Date();
            Date beginAppointmentDate = null;
            //允许改约规则： 预约下午17点前（含） ， 当天23点前改约； 预约下午17点以后的， 第二天23点前完成改约。
            if (now.getTime() < DateUtils.getDate(now, 23, 0, 0).getTime()) {
                beginAppointmentDate = DateUtils.getDate(DateUtils.addDays(now, -1), 17, 0, 0);
            } else {
                beginAppointmentDate = DateUtils.getDate(now, 17, 0, 0);
            }
            order.setStartOfToday(beginAppointmentDate);
            order.setNow(now);
            if (order.getBeginDate() != null) {
                Date[] dates = OrderUtils.getQuarterDates(order.getBeginDate(), order.getEndDate(), 0, 0);
                List<String> quarters = QuarterUtils.getQuarters(dates[0], dates[1]);
                if (quarters != null && quarters.size() > 0) {
                    order.setQuarters(quarters);
                }
            }
            page = processOrderListService.findKefuArriveAppointmentOrderList(order, true);
        } catch (Exception e) {
            addMessage(model, "错误：" + e.getMessage());
        }
        model.addAttribute(MODEL_ATTR_PAGE, page);
        model.addAttribute(MODEL_ATTR_ORDER, order);

        //记录反馈，反馈内容条件搜索频率情况
        if(order.getReplyFlagCustomer()==2){
            LogUtils.saveLog(request,null,null,"网点工单,预约到期列表,未回复反馈搜索查询","kefuArriveAppointmentList",user);
        }
        if(StringUtils.isNotBlank(order.getRemarks())){
            LogUtils.saveLog(request,null,null,"网点工单,预约到期列表,反馈内容搜索查询","kefuArriveAppointmentList",user);
        }

        return VIEW_NAME_ARRIVEAPPOINTMENT_LIST;

    }

    /**
     * 预约超期
     */
    @RequiresPermissions(value =
            {"sd:order:accept", "sd:order:plan", "sd:order:complete",
                    "sd:order:return", "sd:order:grade", "sd:order:feedback"}, logical = Logical.OR)
    @RequestMapping(value = "passAppointmentList")
    public String passAppointmentList(OrderSearchModel order, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<Order> page = new Page<>();
        User user = UserUtils.getUser();
        order = setSearchModel(user,order,model,3,true,365,false,0);
        if(!order.getValid()){
            model.addAttribute(MODEL_ATTR_PAGE, page);
            model.addAttribute(MODEL_ATTR_ORDER, order);
            return VIEW_NAME_PLANNING_LIST;
        }
        try {
            //加急
            loadUrgentLevels(order);
            Page<OrderSearchModel> parPage = new Page<>(request, response, DEFAULT_PAGE_SIZE);
            order.setPage(parPage);
            Date now = new Date();
            Date endAppointmentDate = null;
            //允许改约规则： 预约下午17点前（含） ， 当天23点前改约； 预约下午17点以后的， 第二天23点前完成改约。
            if (now.getTime() < DateUtils.getDate(now, 23, 0, 0).getTime()) {
                endAppointmentDate = DateUtils.getDate(DateUtils.addDays(now, -1), 17, 0, 0);
            } else {
                endAppointmentDate = DateUtils.getDate(now, 17, 0, 0);
            }
            order.setStartOfToday(endAppointmentDate);
            if (order.getBeginDate() != null) {
                Date[] dates = OrderUtils.getQuarterDates(order.getBeginDate(), order.getEndDate(), 0, 0);
                List<String> quarters = QuarterUtils.getQuarters(dates[0], dates[1]);
                if (quarters != null && quarters.size() > 0) {
                    order.setQuarters(quarters);
                }
            }
            page = processOrderListService.findKefuPassAppointmentOrderList( order, true);
        } catch (Exception e) {
            addMessage(model, "错误：" + e.getMessage());
        }
        model.addAttribute(MODEL_ATTR_PAGE, page);
        model.addAttribute(MODEL_ATTR_ORDER, order);

        //记录反馈，反馈内容条件搜索频率情况
        if(order.getReplyFlagCustomer()==2){
            LogUtils.saveLog(request,null,null,"网点工单,预约超期列表,未回复反馈搜索查询","kefuPassAppointmentList",user);
        }
        if(StringUtils.isNotBlank(order.getRemarks())){
            LogUtils.saveLog(request,null,null,"网点工单,预约超期列表,反馈内容搜索查询","kefuPassAppointmentList",user);
        }

        return VIEW_NAME_PASSAPPOINTMENT_LIST;

    }

    /**
     * 停滞工单
     */
    @RequiresPermissions(value =
            {"sd:order:accept", "sd:order:plan", "sd:order:service",
                    "sd:order:complete", "sd:order:return", "sd:order:grade",
                    "sd:order:feedback"}, logical = Logical.OR)
    @RequestMapping(value = "pendinglist")
    public String pendinglist(OrderSearchModel order, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<Order> page = new Page<>();
        User user = UserUtils.getUser();
        order = setSearchModel(user,order,model,3,true,365,true,365);
        if(!order.getValid()){
            model.addAttribute(MODEL_ATTR_PAGE, page);
            model.addAttribute(MODEL_ATTR_ORDER, order);
            return VIEW_NAME_PLANNING_LIST;
        }
        try {
            loadUrgentLevels(order);
            Page<OrderSearchModel> parPage = new Page<>(request, response, DEFAULT_PAGE_SIZE);
            Date now = new Date();
            order.setPage(parPage);
            order.setNow(now);
            order.setStartOfToday(DateUtils.getStartOfDay(now));
            if (order.getBeginDate() != null) {
                Date[] dates = OrderUtils.getQuarterDates(order.getBeginDate(), order.getEndDate(), 0, 0);
                List<String> quarters = QuarterUtils.getQuarters(dates[0], dates[1]);
                if (quarters != null && quarters.size() > 0) {
                    order.setQuarters(quarters);
                }
            }
                    page = processOrderListService.findKefuPendingOrderList(order, true);
        } catch (Exception e) {
            addMessage(model, "错误：" + e.getMessage());
        }
        model.addAttribute(MODEL_ATTR_PAGE, page);
        model.addAttribute(MODEL_ATTR_ORDER, order);

        //记录反馈，反馈内容条件搜索频率情况
        if(order.getReplyFlagCustomer()==2){
            LogUtils.saveLog(request,null,null,"网点工单,停滞列表,未回复反馈搜索查询","kefuPendinglist",user);
        }
        if(StringUtils.isNotBlank(order.getRemarks())){
            LogUtils.saveLog(request,null,null,"网点工单,停滞列表,反馈内容搜索查询","kefuPendinglist",user);
        }

        return VIEW_NAME_PENDING_LIST;
    }

    /**
     * 待回访
     */
    @RequiresPermissions(value =
            {"sd:order:accept", "sd:order:plan", "sd:order:complete",
                    "sd:order:return", "sd:order:grade", "sd:order:feedback"}, logical = Logical.OR)
    @RequestMapping(value = "servicedList")
    public String servicedList(OrderSearchModel order, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<Order> page = new Page<>();
        User user = UserUtils.getUser();
        order = setSearchModel(user,order,model,3,true,365,false,0);
        if(!order.getValid()){
            model.addAttribute(MODEL_ATTR_PAGE, page);
            model.addAttribute(MODEL_ATTR_ORDER, order);
            return VIEW_NAME_PLANNING_LIST;
        }
        try {
            //加急
            loadUrgentLevels(order);
            Page<OrderSearchModel> parPage = new Page<>(request, response, DEFAULT_PAGE_SIZE);
            Date now = new Date();
            order.setPage(parPage);
            order.setNow(now);
            order.setStartOfToday(DateUtils.getStartOfDay(now));
            if (order.getBeginDate() != null) {
                Date[] dates = OrderUtils.getQuarterDates(order.getBeginDate(), order.getEndDate(), 0, 0);
                List<String> quarters = QuarterUtils.getQuarters(dates[0], dates[1]);
                if (quarters != null && quarters.size() > 0) {
                    order.setQuarters(quarters);
                }
            }
            page = processOrderListService.findKefuServicedOrderList(order, true);
        } catch (Exception e) {
            addMessage(model, "错误：" + e.getMessage());
        }
        model.addAttribute(MODEL_ATTR_PAGE, page);
        model.addAttribute(MODEL_ATTR_ORDER, order);

        //记录反馈，反馈内容条件搜索频率情况
        if(order.getReplyFlagCustomer()==2){
            LogUtils.saveLog(request,null,null,"网点工单,待回访列表,未回复反馈搜索查询","kefuServicedList",user);
        }
        if(StringUtils.isNotBlank(order.getRemarks())){
            LogUtils.saveLog(request,null,null,"网点工单,待回访列表,反馈内容搜索查询","kefuServicedList",user);
        }

        return VIEW_NAME_SERVICED_LIST;

    }

    /**
     * 回访失败列表
     */
    @RequiresPermissions(value =
            {"sd:order:accept", "sd:order:plan", "sd:order:complete",
                    "sd:order:return", "sd:order:grade", "sd:order:feedback"}, logical = Logical.OR)
    @RequestMapping(value = "followUpFailList")
    public String followUpFailList(OrderSearchModel order, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<Order> page = new Page<>();
        User user = UserUtils.getUser();
        order = setSearchModel(user,order,model,3,true,365,false,0);
        if(!order.getValid()){
            model.addAttribute(MODEL_ATTR_PAGE, page);
            model.addAttribute(MODEL_ATTR_ORDER, order);
            return VIEW_NAME_PLANNING_LIST;
        }
        try {
            //加急
            loadUrgentLevels(order);
            Page<OrderSearchModel> parPage = new Page<>(request, response, DEFAULT_PAGE_SIZE);
            Date now = new Date();
            order.setPage(parPage);
            order.setNow(now);
            order.setStartOfToday(DateUtils.getStartOfDay(now));
            if (order.getBeginDate() != null) {
                Date[] dates = OrderUtils.getQuarterDates(order.getBeginDate(), order.getEndDate(), 0, 0);
                List<String> quarters = QuarterUtils.getQuarters(dates[0], dates[1]);
                if (quarters != null && quarters.size() > 0) {
                    order.setQuarters(quarters);
                }
            }
            page = processOrderListService.findKefuFollowUpFailOrderList(order, true);
        } catch (Exception e) {
            addMessage(model, "错误：" + e.getMessage());
        }
        model.addAttribute(MODEL_ATTR_PAGE, page);
        model.addAttribute(MODEL_ATTR_ORDER, order);

        //记录反馈，反馈内容条件搜索频率情况
        if(order.getReplyFlagCustomer()==2){
            LogUtils.saveLog(request,null,null,"网点工单,回访失败列表,未回复反馈搜索查询","kefuFollowUpFailList",user);
        }
        if(StringUtils.isNotBlank(order.getRemarks())){
            LogUtils.saveLog(request,null,null,"网点工单,回访失败列表,反馈内容搜索查询","kefuFollowUpFailList",user);
        }

        return VIEW_NAME_FOLLOWUP_FAIL_LIST;

    }

    /**
     * 未完成
     */
    @RequiresPermissions(value =
            {"sd:order:accept", "sd:order:plan", "sd:order:complete",
                    "sd:order:return", "sd:order:grade", "sd:order:feedback"}, logical = Logical.OR)
    @RequestMapping(value = "uncompletedList")
    public String uncompletedList(OrderSearchModel order, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<Order> page = new Page<>();
        User user = UserUtils.getUser();
        order = setSearchModel(user,order,model,3,true,365,false,0);
        if(!order.getValid()){
            model.addAttribute(MODEL_ATTR_PAGE, page);
            model.addAttribute(MODEL_ATTR_ORDER, order);
            return VIEW_NAME_PLANNING_LIST;
        }
        try {
            //加急
            loadUrgentLevels(order);
            Page<OrderSearchModel> parPage = new Page<>(request, response, DEFAULT_PAGE_SIZE);
            Date now = new Date();
            order.setPage(parPage);
            order.setStartOfToday(DateUtils.getStartOfDay(now));
            if (order.getBeginDate() != null) {
                Date[] dates = OrderUtils.getQuarterDates(order.getBeginDate(), order.getEndDate(), 0, 0);
                List<String> quarters = QuarterUtils.getQuarters(dates[0], dates[1]);
                if (quarters != null && quarters.size() > 0) {
                    order.setQuarters(quarters);
                }
            }
            page = processOrderListService.findKefuUncompletedOrderList(order, true);
        } catch (Exception e) {
            addMessage(model, "错误：" + e.getMessage());
        }
        model.addAttribute(MODEL_ATTR_PAGE, page);
        model.addAttribute(MODEL_ATTR_ORDER, order);

        //记录反馈，反馈内容条件搜索频率情况
        if(order.getReplyFlagCustomer()==2){
            LogUtils.saveLog(request,null,null,"网点工单,未完成列表,未回复反馈搜索查询","kefuUncompletedList",user);
        }
        if(StringUtils.isNotBlank(order.getRemarks())){
            LogUtils.saveLog(request,null,null,"网点工单,未完成列表,反馈内容搜索查询","kefuUncompletedList",user);
        }

        return VIEW_NAME_UMCOMPLETED_LIST;
    }


    /**
     * 所有
     */
    @RequiresPermissions(value =
            {"sd:order:accept", "sd:order:plan", "sd:order:service",
                    "sd:order:complete", "sd:order:return", "sd:order:grade",
                    "sd:order:feedback"}, logical = Logical.OR)
    @RequestMapping(value = "alllist")
    public String allList(OrderSearchModel order, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<Order> page = new Page<>();
        User user = UserUtils.getUser();
        String viewForm = VIEW_NAME_ALL_LIST;
        if (user == null || user.getId() == null) {
            addMessage(model, "错误：登录超时，请退出后重新登录。");
            model.addAttribute(MODEL_ATTR_PAGE, page);
            model.addAttribute(MODEL_ATTR_ORDER, order);
            return viewForm;
        }
        String messageType = request.getParameter("messageType");
        String appAbnormalyFlag = request.getParameter("appAbnormalyFlag");
        Boolean findNoticeMessage = false;
        if (StringUtils.isNoneBlank(messageType) && StringUtils.isNumeric(messageType)) {
            findNoticeMessage = true;
            order.setMessageType(Integer.valueOf(messageType));
        } else {
            order.setMessageType(null);
        }
        if (StringUtils.isNoneBlank(appAbnormalyFlag) && StringUtils.isNumeric(appAbnormalyFlag)) {
            findNoticeMessage = true;
            order.setAppAbnormalyFlag(Integer.valueOf(appAbnormalyFlag));
        } else {
            order.setAppAbnormalyFlag(null);
        }
        //因界面需要，对area.parent初始化
        if(order.getArea() == null || order.getArea().getId() == null){
            order.setArea(new Area(0L));
            order.setAreaLevel(null);
        }
        //提交查询
        if (request.getMethod().equalsIgnoreCase("post") || findNoticeMessage) {
            order = setSearchModel(user,order,model,3,true,365,true,365);
            if(!order.getValid()){
                model.addAttribute(MODEL_ATTR_PAGE, page);
                model.addAttribute(MODEL_ATTR_ORDER, order);
                return VIEW_NAME_PLANNING_LIST;
            }
            try {
                Page<OrderSearchModel> parPage = new Page<>(request, response, DEFAULT_PAGE_SIZE);
                order.setPage(parPage);
                Date now = new Date();
                order.setNow(now);
                if (order.getBeginDate() != null) {
                    Date[] dates = OrderUtils.getQuarterDates(order.getBeginDate(), order.getEndDate(), 0, 0);
                    List<String> quarters = QuarterUtils.getQuarters(dates[0], dates[1]);
                    if (quarters != null && quarters.size() > 0) {
                        order.setQuarters(quarters);
                    }
                }
                page = processOrderListService.findKefuAllOrderList(order, true);
            } catch (Exception e) {
                addMessage(model, "错误：" + e.getMessage());
            }
        } else {
            //访问页面，未查询
            order.setEndDate(new Date());
            order.setBeginDate(DateUtils.getStartDayOfMonth(DateUtils.addMonth(new Date(), -3)));
        }
        loadUrgentLevels(order);
        model.addAttribute(MODEL_ATTR_PAGE, page);
        model.addAttribute(MODEL_ATTR_ORDER, order);

        //记录反馈，反馈内容条件搜索频率情况
        if(order.getReplyFlagCustomer()==2){
            LogUtils.saveLog(request,null,null,"网点工单,所有列表,未回复反馈搜索查询","kefuAllList",user);
        }
        if(StringUtils.isNotBlank(order.getRemarks())){
            LogUtils.saveLog(request,null,null,"网点工单,所有列表,反馈内容搜索查询","kefuAllList",user);
        }


        return viewForm;
    }

    @RequiresPermissions(value =
            {"sd:order:accept", "sd:order:plan", "sd:order:service",
                    "sd:order:complete", "sd:order:return", "sd:order:grade",
                    "sd:order:feedback"}, logical = Logical.OR)
    @RequestMapping(value = "completedList")
    public String completedList(OrderSearchModel order, HttpServletRequest request, HttpServletResponse response, Model model) {
        String viewForm = VIEW_NAME_COMPLETED_LIST;
        Page<Order> page = new Page<>();
        User user = UserUtils.getUser();
        if (user == null || user.getId() == null) {
            addMessage(model, "错误：登录超时，请退出后重新登录。");
            model.addAttribute(MODEL_ATTR_PAGE, page);
            model.addAttribute(MODEL_ATTR_ORDER, order);
            return viewForm;
        }

        //提交查询
        if (request.getMethod().equalsIgnoreCase("post")) {
            order = setSearchModel(user,order,model,3,true,365,true,365);
            if(!order.getValid()){
                model.addAttribute(MODEL_ATTR_PAGE, page);
                model.addAttribute(MODEL_ATTR_ORDER, order);
                return VIEW_NAME_PLANNING_LIST;
            }
            Page<OrderSearchModel> parPage = new Page<>(request, response, DEFAULT_PAGE_SIZE);
            order.setPage(parPage);
            Date now = new Date();
            order.setNow(now);
            if (order.getBeginDate() != null) {
                Date[] dates = OrderUtils.getQuarterDates(order.getBeginDate(), order.getEndDate(), 0, 0);
                List<String> quarters = QuarterUtils.getQuarters(dates[0], dates[1]);
                if (quarters != null && quarters.size() > 0) {
                    order.setQuarters(quarters);
                }
            }
            try {
                page = processOrderListService.findKefuCompletedOrderList(order, true);
            } catch (Exception e) {
                addMessage(model, "错误：" + e.getMessage());
            }
        } else {
            //访问页面，未查询
            order.setEndDate(new Date());
            order.setBeginDate(DateUtils.getStartDayOfMonth(DateUtils.addMonth(new Date(), -3)));
        }
        //加急
        loadUrgentLevels(order);
        model.addAttribute(MODEL_ATTR_PAGE, page);
        model.addAttribute(MODEL_ATTR_ORDER, order);

        //记录反馈，反馈内容条件搜索频率情况
        if(order.getReplyFlagCustomer()==2){
            LogUtils.saveLog(request,null,null,"网点工单,已完成列表,未回复反馈搜索查询","kefuCompletedList",user);
        }
        if(StringUtils.isNotBlank(order.getRemarks())){
            LogUtils.saveLog(request,null,null,"网点工单,已完成列表,反馈内容搜索查询","kefuCompletedList",user);
        }

        return viewForm;
    }

    //endregion 普通列表

    //region 爽约

    /**
     * 设置爽约列表必须的查询条件
     * @param user  当前帐号
     * @param searchModel   查询条件
     * @param initMonths    初始最小查询时间段(月)
     * @param searchByOrderDateRange by下单日期查询开关
     * @param maxOrderDays   下单最大查询范围(天)
     * @param searchByCompleteDateRange by完成日期查询开关
     * @param maxCompleteDays 完成最大查询范围(天)
     */
    private OrderSearchModel setBrokenSearchModel(User user,OrderSearchModel searchModel,Model model ,
                                            int initMonths,boolean searchByOrderDateRange ,int maxOrderDays,
                                            boolean searchByCompleteDateRange,int maxCompleteDays) {
        if (searchModel == null) {
            searchModel = new OrderSearchModel();
        }
        //当前网点
        ServicePoint point = getCurrentServicePoint();
        searchModel.setServicePoint(point);
        Area area = searchModel.getArea();
        if(area == null){
            area = new Area(0L);
            searchModel.setArea(area);
        }
        if(area.getParent()==null || area.getParent().getId() == null){
            area.setParent(new Area(0L));
        }
        String checkRegion = processOrderListService.loadAndCheckUserRegions(searchModel,user);
        if(StringUtils.isNotEmpty(checkRegion)){
            addMessage(model, checkRegion);
            searchModel.setValid(false);
            return searchModel;
        }
        Date now = new Date();
        //下单日期
        if(searchByOrderDateRange) {
            if (searchModel.getBeginDate() == null) {
                searchModel.setEndDate(DateUtils.getDateEnd(new Date()));
                searchModel.setBeginDate(DateUtils.getStartDayOfMonth(DateUtils.addMonth(new Date(), 0 - initMonths)));
            } else {
                searchModel.setEndDate(DateUtils.getDateEnd(searchModel.getEndDate()));
            }
            //检查最大时间范围
            if(maxOrderDays > 0) {
                Date maxDate = DateUtils.addDays(searchModel.getEndDate(), 0 - maxOrderDays);
                maxDate = DateUtils.getDateStart(maxDate);
                if (DateUtils.pastDays(searchModel.getBeginDate(), maxDate) > 0) {
                    searchModel.setBeginDate(maxDate);
                }
            }
        }
        //完成日期
        if(searchByCompleteDateRange){
            if (searchModel.getCompleteEnd() != null) {
                searchModel.setCompleteEnd(DateUtils.getDateEnd(searchModel.getCompleteEnd()));
            }
            //检查最大时间范围
            if(maxCompleteDays > 0 && searchModel.getCompleteBegin() != null){
                Date maxDate = DateUtils.addDays(searchModel.getCompleteBegin(),maxCompleteDays-1);
                maxDate = DateUtils.getDateEnd(maxDate);
                if(searchModel.getCompleteEnd() == null){
                    searchModel.setCompleteEnd(DateUtils.getDateEnd(now));
                }
                if(DateUtils.pastDays(maxDate,searchModel.getCompleteEnd())>0){
                    searchModel.setCompleteEnd(maxDate);
                }
            }
        }
        //账号品类
        Boolean loadResult = loadUserCategories(model,searchModel,user.getId());
        if(!loadResult){
            searchModel.setValid(false);
            return searchModel;
        }
        return searchModel;
    }


    /**
     * 异常订单-爽约列表
     * 预约时间两次及以上的工单
     */
    @RequiresPermissions(value = "sd:order:reservation")
    @RequestMapping(value = "reservationlist")
    public String reservationList(OrderSearchModel order, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<Order> page = new Page<>();
        User user = UserUtils.getUser();
        order.setOrderDataLevel(OrderUtils.OrderDataLevel.CONDITION);//从数据库/redis中读取具体的数据内容
        order = setBrokenSearchModel(user,order,model,3,true,365,false,0);
        if(!order.getValid()){
            model.addAttribute(MODEL_ATTR_PAGE, page);
            model.addAttribute(MODEL_ATTR_ORDER, order);
            return VIEW_NAME_BROKE_APPOINTMENT_ORDER_LIST;
        }

        Boolean isValid = checkOrderNoAndPhone(order,model,page);
        if(!isValid){
            return VIEW_NAME_BROKE_APPOINTMENT_ORDER_LIST;
        }
        try {
            page = processOrderListService.getBrokeAppointmentOrderlist(new Page<>(request, response), order);
        }catch (Exception e){
            addMessage(model, "错误：" + e.getMessage());
        }
        model.addAttribute(MODEL_ATTR_PAGE, page);
        model.addAttribute(MODEL_ATTR_ORDER, order);
        return VIEW_NAME_BROKE_APPOINTMENT_ORDER_LIST;
    }

    //endregion 爽约

    //region 特殊列表

    /**
     * 天猫请求
     */
    @RequiresPermissions(value =
            {"sd:order:accept", "sd:order:plan", "sd:order:complete", "sd:order:anomaly",
                    "sd:order:return", "sd:order:grade", "sd:order:feedback"}, logical = Logical.OR)
    @RequestMapping(value = "tmallAnomalyList")
    public String tmallAnomalyList(TmallAnomalyRecourseSearchVM searchEntity, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<TmallAnomalyRecourse> page = new Page<>();
        User user = UserUtils.getUser();
        //检查订单号码
        if(StringUtils.isNotBlank(searchEntity.getOrderNo())) {
            int orderSerchType = searchEntity.getOrderNoSearchType();
            if (orderSerchType != 1) {
                addMessage(model, "错误：请输入正确的订单号码");
                model.addAttribute("page", page);
                model.addAttribute("searchEntity", searchEntity);
                return VIEW_NAME_TMALLANOMALY_LIST;
            } else {
                //检查分片
                try {
                    Date goLiveDate = OrderUtils.getGoLiveDate();
                    String[] quarters = DateUtils.getQuarterRange(goLiveDate, new Date());
                    if (quarters.length == 2) {
                        int start = StringUtils.toInteger(quarters[0]);
                        int end = StringUtils.toInteger(quarters[1]);
                        if (start > 0 && end > 0) {
                            int quarter = StringUtils.toInteger(searchEntity.getQuarter());
                            if (quarter < start || quarter > end) {
                                quarters = null;
                                goLiveDate = null;
                                addMessage(model, "错误：请输入正确的订单号码");
                                model.addAttribute("page", page);
                                model.addAttribute("searchEntity", searchEntity);
                                return VIEW_NAME_TMALLANOMALY_LIST;
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("检查分片错误,orderNo:{}", searchEntity.getOrderNo(), e);
                }
            }
        }
        //date
        Date now = new Date();
        if (searchEntity.getSubmitStartDate() == null) {
            searchEntity.setSubmitEndDate(DateUtils.getDateEnd(now));
            searchEntity.setSubmitStartDate(DateUtils.getStartDayOfMonth(DateUtils.addMonth(now, -3)));
        } else {
            searchEntity.setSubmitEndDate(DateUtils.getDateEnd(searchEntity.getSubmitEndDate()));
        }
        String checkRegion = processOrderListService.loadAndCheckUserRegions(searchEntity,user);
        if(StringUtils.isNotEmpty(checkRegion)){
            addMessage(model, checkRegion);
            model.addAttribute("page", page);
            model.addAttribute("searchEntity", searchEntity);
            return VIEW_NAME_TMALLANOMALY_LIST;
        }
        //账号品类
        Boolean loadResult = loadUserCategories(model,searchEntity,user.getId());
        if(!loadResult){
            model.addAttribute("page", page);
            model.addAttribute("searchEntity", searchEntity);
            return VIEW_NAME_TMALLANOMALY_LIST;
        }
        try {
            //查询
            page = processOrderListService.findKefuTmallAnomalyList(new Page<>(request, response, DEFAULT_PAGE_SIZE), searchEntity);
        } catch (Exception e) {
            log.error("[AnomalyRecourseController.list] ", e);
            addMessage(model, "错误：" + e.getMessage());
        }
        model.addAttribute("page", page);
        model.addAttribute("searchEntity", searchEntity);
        return VIEW_NAME_TMALLANOMALY_LIST;
    }

    /**
     * 天猫预警
     */
    @RequiresPermissions(value =
            {"sd:order:accept", "sd:order:plan", "sd:order:service",
                    "sd:order:complete", "sd:order:return", "sd:order:grade",
                    "sd:order:feedback"}, logical = Logical.OR)
    @RequestMapping(value = {"tmallServiceMonitorList"})
    public String tmallServiceMonitorList(TmallServiceMonitorSearchVM tmallServiceMonitor, HttpServletRequest request, HttpServletResponse response, Model model) {
        if (tmallServiceMonitor.getStatus() == null || tmallServiceMonitor.getStatus() < 0) {
            tmallServiceMonitor.setStatus(1);
        }
        Page<TmallServiceMonitor> page = new Page<>();
        User user = UserUtils.getUser();
        //检查订单号码
        if(StringUtils.isNotBlank(tmallServiceMonitor.getOrderNo())) {
            int orderSerchType = tmallServiceMonitor.getOrderNoSearchType();
            if (orderSerchType != 1) {
                addMessage(model, "错误：订单号码输入无效");
                model.addAttribute(MODEL_ATTR_PAGE, page);
                model.addAttribute("entity", tmallServiceMonitor);
                return VIEW_NAME_TMALLSERVICEMONITOR_LIST;
            } else {
                //检查分片
                try {
                    Date goLiveDate = OrderUtils.getGoLiveDate();
                    String[] quarters = DateUtils.getQuarterRange(goLiveDate, new Date());
                    if (quarters.length == 2) {
                        int start = StringUtils.toInteger(quarters[0]);
                        int end = StringUtils.toInteger(quarters[1]);
                        if (start > 0 && end > 0) {
                            int quarter = StringUtils.toInteger(tmallServiceMonitor.getQuarter());
                            if (quarter < start || quarter > end) {
                                quarters = null;
                                goLiveDate = null;
                                addMessage(model, "错误：订单号码输入无效");
                                model.addAttribute(MODEL_ATTR_PAGE, page);
                                model.addAttribute("entity", tmallServiceMonitor);
                                return VIEW_NAME_TMALLSERVICEMONITOR_LIST;
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("检查分片错误,orderNo:{}", tmallServiceMonitor.getOrderNo(), e);
                }
            }
        }
        String checkRegion = processOrderListService.loadAndCheckUserRegions(tmallServiceMonitor,user);
        if(StringUtils.isNotEmpty(checkRegion)){
            addMessage(model, checkRegion);
            model.addAttribute("page", page);
            model.addAttribute("entity", tmallServiceMonitor);
            return VIEW_NAME_TMALLSERVICEMONITOR_LIST;
        }
        //账号品类
        Boolean loadResult = loadUserCategories(model,tmallServiceMonitor,user.getId());
        if(!loadResult){
            model.addAttribute("page", page);
            model.addAttribute("entity", tmallServiceMonitor);
            return VIEW_NAME_TMALLSERVICEMONITOR_LIST;
        }
        //其他类型帐号，不限制客户及突击区域订单
        tmallServiceMonitor.setCustomerType(null);
        tmallServiceMonitor.setRushType(null);
        page = processOrderListService.findKefuTmallServiceMonitorList(new Page<>(request, response, DEFAULT_PAGE_SIZE), tmallServiceMonitor);
        model.addAttribute("entity", tmallServiceMonitor);
        model.addAttribute(MODEL_ATTR_PAGE, page);
        return VIEW_NAME_TMALLSERVICEMONITOR_LIST;
    }

    /**
     * 突击列表
     * 突击未完成或未提交
     */
    @RequiresPermissions(value =
            {"sd:order:accept", "sd:order:plan", "sd:order:complete",
                    "sd:order:return", "sd:order:grade", "sd:order:feedback"}, logical = Logical.OR)
    @RequestMapping(value = "rushinglist")
    public String rushingList(OrderSearchModel order, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<Order> page = new Page<>();
        User user = UserUtils.getUser();
        //状态：所有
        if (order.getStatus() == null || StringUtils.isBlank(order.getStatus().getValue())) {
            order.setStatus(null);
            order.setStatusRange(new IntegerRange(Order.ORDER_STATUS_APPROVED, Order.ORDER_STATUS_ACCEPTED));//待接单，已接单
        }
        order = setSearchModel(user,order,model,1,true,365,false,0);
        if(!order.getValid()){
            model.addAttribute(MODEL_ATTR_PAGE, page);
            model.addAttribute(MODEL_ATTR_ORDER, order);
            return VIEW_NAME_PLANNING_LIST;
        }
        Page<OrderSearchModel> parPage = new Page<>(request, response, DEFAULT_PAGE_SIZE);
        order.setPage(parPage);
        Date now = new Date();
        order.setNow(now);
        if (order.getBeginDate() != null) {
            Date[] dates = OrderUtils.getQuarterDates(order.getBeginDate(), order.getEndDate(), 0, 0);
            List<String> quarters = QuarterUtils.getQuarters(dates[0], dates[1]);
            if (quarters != null && quarters.size() > 0) {
                order.setQuarters(quarters);
            }
        }
        //客服主管
        order.setOrderDataLevel(OrderUtils.OrderDataLevel.DETAIL);//从数据库/redis中读取具体的数据内容
        try {
            //加急
            loadUrgentLevels(order);
            page = processOrderListService.findKefuRushingOrderList(order, true);
        } catch (Exception e) {
            addMessage(model, "错误：" + e.getMessage());
        }
        model.addAttribute(MODEL_ATTR_PAGE, page);
        model.addAttribute(MODEL_ATTR_ORDER, order);
        return VIEW_NAME_RUSHING_LIST;
    }

    /**
     * 投诉列表
     */
    @RequiresPermissions(value =
            {"sd:order:accept", "sd:order:plan", "sd:order:complete",
                    "sd:order:return", "sd:order:grade", "sd:order:feedback"}, logical = Logical.OR)
    @RequestMapping(value = "complainlist")
    public String complainList(OrderSearchModel order, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<Order> page = new Page<>();
        User user = UserUtils.getUser();
        //状态：所有
        if (order.getStatus() == null || StringUtils.isBlank(order.getStatus().getValue())) {
            order.setStatus(null);
            order.setStatusRange(new IntegerRange(Order.ORDER_STATUS_APPROVED, Order.ORDER_STATUS_ACCEPTED));//待接单，已接单
        }
        order = setSearchModel(user,order,model,1,true,365,false,0);
        if(!order.getValid()){
            model.addAttribute(MODEL_ATTR_PAGE, page);
            model.addAttribute(MODEL_ATTR_ORDER, order);
            return VIEW_NAME_PLANNING_LIST;
        }
        order.setOrderDataLevel(OrderUtils.OrderDataLevel.DETAIL);//从数据库/redis中读取具体的数据内容
        Page<OrderSearchModel> parPage = new Page<>(request, response, DEFAULT_PAGE_SIZE);
        order.setPage(parPage);
        Date now = new Date();
        order.setNow(now);
        if (order.getBeginDate() != null) {
            Date[] dates = OrderUtils.getQuarterDates(order.getBeginDate(), order.getEndDate(), 0, 0);
            List<String> quarters = QuarterUtils.getQuarters(dates[0], dates[1]);
            if (quarters != null && quarters.size() > 0) {
                order.setQuarters(quarters);
            }
        }
        try {
            //加急
            loadUrgentLevels(order);
            page = processOrderListService.findKefuComplainOrderList(order, true);
        } catch (Exception e) {
            addMessage(model, "错误：" + e.getMessage());
        }
        model.addAttribute(MODEL_ATTR_PAGE, page);
        model.addAttribute(MODEL_ATTR_ORDER, order);
        return VIEW_NAME_COMPLAIN_LIST;
    }

    /**
     * 催单列表
     */
    @RequiresPermissions(value =
            {"sd:order:accept", "sd:order:plan", "sd:order:complete",
                    "sd:order:return", "sd:order:grade", "sd:order:feedback"}, logical = Logical.OR)
    @RequestMapping(value = "reminderlist")
    public String reminderList(OrderSearchModel order, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page page = new Page<>();
        User user = UserUtils.getUser();
        order = setSearchModel(user,order,model,3,true,365,false,0);
        if(!order.getValid()){
            model.addAttribute(MODEL_ATTR_PAGE, page);
            model.addAttribute(MODEL_ATTR_ORDER, order);
            return VIEW_NAME_PLANNING_LIST;
        }
        Page<OrderSearchModel> parPage = new Page<>(request, response, DEFAULT_PAGE_SIZE);
        order.setPage(parPage);
        Date now = new Date();
        order.setNow(now);
        if (order.getBeginDate() != null) {
            Date[] dates = OrderUtils.getQuarterDates(order.getBeginDate(), order.getEndDate(), 0, 0);
            List<String> quarters = QuarterUtils.getQuarters(dates[0], dates[1]);
            if (quarters != null && quarters.size() > 0) {
                order.setQuarters(quarters);
            }
        }
        try {
            order.setOrderNo(order.getOrderNo().trim());
            order.setUserName(order.getUserName().trim());
            order.setPhone1(order.getPhone1().trim());
            order.setAddress(order.getAddress().trim());
            order.setCreator(order.getCreator().trim());
            order.setRemarks(order.getRemarks().trim());
            page = processOrderListService.findReminderOrderLit(order,true);
            //加急
            loadUrgentLevels(order);
        } catch (Exception e) {
            addMessage(model, "错误：" + e.getMessage());
        }

        model.addAttribute(MODEL_ATTR_PAGE, page);
        model.addAttribute(MODEL_ATTR_ORDER, order);
        return VIEW_NAME_REMINDER_LIST;
    }

    /**
     * 历史派单列表(for客服)
     */
    @RequiresUser
    @RequestMapping(value = "historyPlanList")
    public String historyPlanList(OrderSearchModel order, HttpServletRequest request, HttpServletResponse response, Model model) {
        String viewForm = "modules/servicePoint/sd/processOrderList/service/historyPlanOrderList";
        Page<HistoryPlanOrderModel> page = new Page<>(request, response);
        Area area = order.getArea();
        if(area == null || area.getId() == null || area.getId() <= 0){
            addMessage(model, "参数错误：无区域");
            model.addAttribute(MODEL_ATTR_PAGE, page);
            model.addAttribute(MODEL_ATTR_ORDER, order);
            return viewForm;
        }
        User user = UserUtils.getUser();
        page.setPageSize(20);
        String checkRegion = processOrderListService.loadAndCheckUserRegions(order,user);
        if(org.apache.commons.lang3.StringUtils.isNotEmpty(checkRegion)){
            addMessage(model, checkRegion);
            model.addAttribute(MODEL_ATTR_PAGE, page);
            model.addAttribute(MODEL_ATTR_ORDER, order);
            return viewForm;
        }

        //date
        if (order.getBeginDate() == null) {
            order.setBeginDate(DateUtils.getStartDayOfMonth(DateUtils.addMonth(new Date(), -3)));
        }
        //完成日期
        if (order.getEndDate() == null) {
            order.setEndDate(DateUtils.getDateEnd(new Date()));
        } else {
            order.setEndDate(DateUtils.getDateEnd(order.getEndDate()));
        }
        if (order.getBeginDate() != null) {
            Date[] dates = OrderUtils.getQuarterDates(order.getBeginDate(), order.getEndDate(), 0, 0);
            List<String> quarters = QuarterUtils.getQuarters(dates[0], dates[1]);
            if (quarters != null && quarters.size() > 0) {
                order.setQuarters(quarters);
            }
        }
        page = processOrderListService.getHistoryPlanListForKefu(page, order);
        model.addAttribute("page", page);
        model.addAttribute("order", order);
        return viewForm;
    }

    //endregion 特殊列表

    //region 客服操作

    private static final String VIEW_NAME_NEW_ORDERDETAIL_INFO_KEFU_FORM = "modules/servicePoint/sd/processOrderList/service/newOrderDefailInfoKefuForm";
    private static final String VIEW_NAME_ORDER_PENDINGTYPE_FORM = "modules/servicePoint/sd/service/orderPendingTypeForm";

    private static final String VIEW_NAME_ORDER_FOLLOWUP_DETAIL_FORM = "modules/servicePoint/sd/processOrderList/service/orderDefailInfoFollowUpForm";
    private static final String VIEW_NAME_ORDER_FOLLOWUP_FAIL_FORM = "modules/servicePoint/sd/processOrderList/service/followUpFailForm";
    private static final String VIEW_NAME_ORDER_FOLLOWUP_ADD_SERVICE_FORM = "modules/servicePoint/sd/processOrderList/service/addFollowUpServiceItemForm";
    private static final String VIEW_NAME_ORDER_FOLLOWUP_EDIT_SERVICE_FORM = "modules/servicePoint/sd/processOrderList/service/editFollowUpServiceItemForm";
    private static final String VIEW_NAME_ORDER_RETURN_DETAIL_FORM = "modules/sd/returnProcess/orderDetailInfoReturnForm";//退换货

    //region 订单详情页

    /**
     * 查看订单明细(客服)
     *
     * @param id 订单id
     * @return
     */
    @RequestMapping(value = {"service/orderDetailInfo"})
    public String kefuOrderDetailInfo(@RequestParam String id, @RequestParam String quarter, String refreshParent, Model model,
                                      HttpServletRequest request) {
        Boolean errorFlag = false;
        Order order = new Order();
        Long orderId = StringUtils.toLong(id);
        boolean hasAuxiliaryMaterils = false;
        if (orderId <= 0) {
            errorFlag = true;
            addMessage(model, "订单代码传递错误");
            model.addAttribute(MODEL_ATTR_ORDER, order);
            model.addAttribute("hasAuxiliaryMaterils",  0);
            model.addAttribute("errorFlag", errorFlag);
            model.addAttribute("fourServicePhone", "400-666-3653");
            model.addAttribute("changed","false");
            return VIEW_NAME_NEW_ORDERDETAIL_INFO_KEFU_FORM;
        } else {
            order = orderService.getOrderById(orderId, quarter, OrderUtils.OrderDataLevel.DETAIL, true,false,true,true);
            if (order == null || order.getOrderCondition() == null) {
                errorFlag = true;
                addMessage(model, "错误：系统繁忙，读取订单失败，请重试。");
                model.addAttribute(MODEL_ATTR_ORDER, order);
                model.addAttribute("hasAuxiliaryMaterils",  0);
                model.addAttribute("errorFlag", errorFlag);
                model.addAttribute("fourServicePhone", "400-666-3653");
                model.addAttribute("changed","false");
                return VIEW_NAME_NEW_ORDERDETAIL_INFO_KEFU_FORM;
            } else {
                if (order.getOrderCondition().getServicePoint() != null && order.getOrderCondition().getServicePoint().getId() != null
                        && order.getOrderCondition().getServicePoint().getId() > 0
                        && order.getOrderCondition().getEngineer() != null && order.getOrderCondition().getEngineer().getId() != null
                        && order.getOrderCondition().getEngineer().getId() > 0) {
                    ServicePoint servicePoint = servicePointService.getFromCache(order.getOrderCondition().getServicePoint().getId());
                    Engineer engineer = servicePointService.getEngineerFromCache(order.getOrderCondition().getServicePoint().getId(), order.getOrderCondition().getEngineer().getId());
                    if (engineer != null) {
                        User engineerUser = new User(engineer.getId());
                        engineerUser.setName(engineer.getName());
                        engineerUser.setMobile(engineer.getContactInfo());
                        engineerUser.setSubFlag(engineer.getMasterFlag() == 1 ? 0 : 1);
//                        engineerUser.setAppLoged(engineer.getAppLoged());
//                        int appLoged = servicePoint !=null && servicePoint.getPrimary()!=null && servicePoint.getPrimary().getAppLoged() != null? servicePoint.getPrimary().getAppLoged() : 0;
                        engineerUser.setAppLoged(engineer.getAppLoged());
                        engineerUser.setAppFlag(engineer.getAppFlag());
                        order.getOrderCondition().setEngineer(engineerUser);
                    }
                }
                hasAuxiliaryMaterils = orderAuxiliaryMaterialService.hasAuxiliaryMaterials(order.getId(), order.getQuarter());
            }
        }
        //2020-09-10 屏蔽客户(应收)自动同步加的远程费及其他费用
        User user = UserUtils.getUser();
        if(user.isKefu()) {
            OrderUtils.customerSyncChargeActionShield(order);
        }
        model.addAttribute(MODEL_ATTR_ORDER, order);
        model.addAttribute("hasAuxiliaryMaterils", hasAuxiliaryMaterils? 1 : 0);
        model.addAttribute("errorFlag", errorFlag);
        if (!errorFlag) {
            model.addAttribute("fourServicePhone", MSDictUtils.getDictSingleValue("400ServicePhone", "400-666-3653"));
        } else {
            model.addAttribute("fourServicePhone", "400-666-3653");
        }
        model.addAttribute("refreshParent", StringUtils.isBlank(refreshParent) ? "true" : refreshParent);//调用方法决定是否在关闭详情页后刷新iframe
        String changed = request.getParameter("changed");
        model.addAttribute("changed", StringUtils.isBlank(changed) ? "false" : changed);
        //语音回访 2019/01/16
        Integer voiceResult = null;
        int orderStatusValue = order.getOrderCondition().getStatusValue();
        if(StringUtils.isNotBlank(order.getOrderCondition().getAppCompleteType())
                && orderStatusValue >= Order.ORDER_STATUS_SERVICED
                && order.getOrderCondition().getGradeFlag() != OrderUtils.OrderGradeType.APP_GRADE.value){
            voiceResult = orderVoiceTaskService.getVoiceTaskResult(order.getQuarter(), order.getId());
            if(voiceResult != null) {
                model.addAttribute("voiceResult", voiceResult);
            }
        }
        int praiseFlag = 0;
        OrderStatusFlag orderStatusFlag = orderStatusFlagService.getByOrderId(orderId,quarter);
        if(orderStatusFlag!=null && orderStatusFlag.getPraiseStatus() == PraiseStatusEnum.APPROVE.code){
            praiseFlag = 1;
        }
        model.addAttribute("praiseFlag",praiseFlag);
        return VIEW_NAME_NEW_ORDERDETAIL_INFO_KEFU_FORM;
    }

    /**
     * 查看历史派单订单明细(客服)
     */
    @RequestMapping(value = {"service/historyOrderDetailInfo"})
    public String kefuHistoryOrderDetailInfo(@RequestParam String id, @RequestParam String quarter, Model model,
                                      HttpServletRequest request) {
        String viewForm = "modules/servicePoint/sd/processOrderList/service/historyOrderDetailInfo";
        Boolean errorFlag = false;
        Order order = new Order();
        Long orderId = StringUtils.toLong(id);
        if (orderId <= 0) {
            errorFlag = true;
            addMessage(model, "订单代码传递错误");
        } else {
            order = orderService.getOrderById(orderId, quarter, OrderUtils.OrderDataLevel.DETAIL, true);
            if (order == null || order.getOrderCondition() == null) {
                errorFlag = true;
                addMessage(model, "错误：系统繁忙，读取订单失败，请重试。");
            } else {
                if (order.getOrderCondition().getServicePoint() != null && order.getOrderCondition().getServicePoint().getId() != null
                        && order.getOrderCondition().getServicePoint().getId() > 0
                        && order.getOrderCondition().getEngineer() != null && order.getOrderCondition().getEngineer().getId() != null
                        && order.getOrderCondition().getEngineer().getId() > 0) {
                    ServicePoint servicePoint = servicePointService.getFromCache(order.getOrderCondition().getServicePoint().getId());
                    Engineer engineer = servicePointService.getEngineerFromCache(order.getOrderCondition().getServicePoint().getId(), order.getOrderCondition().getEngineer().getId());
                    if (engineer != null) {
                        User engineerUser = new User(engineer.getId());
                        engineerUser.setName(engineer.getName());
                        engineerUser.setMobile(engineer.getContactInfo());
                        engineerUser.setSubFlag(engineer.getMasterFlag() == 1 ? 0 : 1);
//                        int appLoged = servicePoint !=null && servicePoint.getPrimary()!=null && servicePoint.getPrimary().getAppLoged() != null? servicePoint.getPrimary().getAppLoged() : 0;
                        engineerUser.setAppLoged(engineer.getAppLoged());
                        engineerUser.setAppFlag(engineer.getAppFlag());
                        order.getOrderCondition().setEngineer(engineerUser);
                    }
                }
            }
        }
        model.addAttribute(MODEL_ATTR_ORDER, order);
        model.addAttribute("errorFlag", errorFlag);
        return viewForm;
    }

    /**
     * 查看订单明细(For负责派单的客服)
     */
    @RequestMapping(value = {"service/orderDetailInfoForPlan"})
    public String kefuOrderDetailInfoForPlan(@RequestParam String id, @RequestParam String quarter, String refreshParent,
                                             HttpServletRequest request, HttpServletResponse response, Model model) {
        Boolean errorFlag = false;
        Order order = new Order();
        Long orderId = StringUtils.toLong(id);
        if (orderId <= 0) {
            errorFlag = true;
            addMessage(model, "订单代码传递错误");
        } else {
            order = orderService.getOrderById(orderId, quarter, OrderUtils.OrderDataLevel.DETAIL, true,false,true,true);
            if (order == null || order.getOrderCondition() == null) {
                errorFlag = true;
                addMessage(model, "错误：系统繁忙，读取订单失败，请重试。");
            } else {
                ServicePoint servicePoint = order.getOrderCondition().getServicePoint();
                if (servicePoint != null && servicePoint.getId() != null && servicePoint.getId() > 0) {
                    Engineer engineer = servicePointService.getEngineerFromCache(servicePoint.getId(), order.getOrderCondition().getEngineer().getId());
                    if (engineer != null) {
                        User engineerUser = new User(engineer.getId());
                        engineerUser.setName(engineer.getName());
                        engineerUser.setMobile(engineer.getContactInfo());
                        engineerUser.setSubFlag(engineer.getMasterFlag() == 1 ? 0 : 1);
                        order.getOrderCondition().setEngineer(engineerUser);
                    }
                }
            }
        }
        //2020-09-10 屏蔽客户(应收)自动同步加的远程费及其他费用
        User user = UserUtils.getUser();
        if(user.isKefu()) {
            OrderUtils.customerSyncChargeActionShield(order);
        }
        model.addAttribute(MODEL_ATTR_ORDER, order);
        model.addAttribute("errorFlag", errorFlag);
        if (!errorFlag) {
            model.addAttribute("fourServicePhone", MSDictUtils.getDictSingleValue("400ServicePhone", "400-666-3653"));
        } else {
            model.addAttribute("fourServicePhone", "400-666-3653");
        }
        model.addAttribute("refreshParent", StringUtils.isBlank(refreshParent) ? "true" : refreshParent);
        String changed = request.getParameter("changed");
        model.addAttribute("changed", StringUtils.isBlank(changed) ? "false" : changed);
        return "modules/servicePoint/sd/processOrderList/service/newOrderDefailInfoPlanForm";
    }


    /**
     * 查看订单明细(For客服处理回访)
     *
     * @param id 订单id
     * @return
     */
    @RequestMapping(value = {"service/orderDetailInfoForFollowUp"})
    public String orderDetailInfoForFollowUp(@RequestParam String id, @RequestParam String quarter, String refreshParent, Model model,
                                      HttpServletRequest request) {
        Boolean errorFlag = false;
        Order order = new Order();
        Long orderId = StringUtils.toLong(id);
        boolean hasAuxiliaryMaterils = false;
        if (orderId <= 0) {
            errorFlag = true;
            addMessage(model, "订单代码传递错误");
        } else {
            order = orderService.getOrderById(orderId, quarter, OrderUtils.OrderDataLevel.DETAIL, true,false,true,true);
            if (order == null || order.getOrderCondition() == null) {
                errorFlag = true;
                addMessage(model, "错误：系统繁忙，读取订单失败，请重试。");
            } else {
                if (order.getOrderCondition().getServicePoint() != null && order.getOrderCondition().getServicePoint().getId() != null
                        && order.getOrderCondition().getServicePoint().getId() > 0
                        && order.getOrderCondition().getEngineer() != null && order.getOrderCondition().getEngineer().getId() != null
                        && order.getOrderCondition().getEngineer().getId() > 0) {
                    ServicePoint servicePoint = servicePointService.getFromCache(order.getOrderCondition().getServicePoint().getId());
                    Engineer engineer = servicePointService.getEngineerFromCache(order.getOrderCondition().getServicePoint().getId(), order.getOrderCondition().getEngineer().getId());
                    if (engineer != null) {
                        User engineerUser = new User(engineer.getId());
                        engineerUser.setName(engineer.getName());
                        engineerUser.setMobile(engineer.getContactInfo());
                        engineerUser.setSubFlag(engineer.getMasterFlag() == 1 ? 0 : 1);
//                        int appLoged = servicePoint !=null && servicePoint.getPrimary()!=null && servicePoint.getPrimary().getAppLoged() != null? servicePoint.getPrimary().getAppLoged() : 0;
                        engineerUser.setAppLoged(engineer.getAppLoged());
                        engineerUser.setAppFlag(engineer.getAppFlag());
                        order.getOrderCondition().setEngineer(engineerUser);
                    }
                }
                hasAuxiliaryMaterils = orderAuxiliaryMaterialService.hasAuxiliaryMaterials(order.getId(), order.getQuarter());
            }
        }
        //2020-09-10 屏蔽客户(应收)自动同步加的远程费及其他费用
        User user = UserUtils.getUser();
        if(user.isKefu()) {
            OrderUtils.customerSyncChargeActionShield(order);
        }
        model.addAttribute(MODEL_ATTR_ORDER, order);
        model.addAttribute("hasAuxiliaryMaterils", hasAuxiliaryMaterils? 1 : 0);
        model.addAttribute("errorFlag", errorFlag);
        if (!errorFlag) {
            model.addAttribute("fourServicePhone", MSDictUtils.getDictSingleValue("400ServicePhone", "400-666-3653"));
        } else {
            model.addAttribute("fourServicePhone", "400-666-3653");
        }
        model.addAttribute("refreshParent", StringUtils.isBlank(refreshParent) ? "true" : refreshParent);//调用方法决定是否在关闭详情页后刷新iframe
        String changed = request.getParameter("changed");
        model.addAttribute("changed", StringUtils.isBlank(changed) ? "false" : changed);
        //语音回访 2019/01/16
        Integer voiceResult = null;
        int orderStatusValue = order.getOrderCondition().getStatusValue();
        if(StringUtils.isNotBlank(order.getOrderCondition().getAppCompleteType())
                && orderStatusValue >= Order.ORDER_STATUS_SERVICED
                && order.getOrderCondition().getGradeFlag() != OrderUtils.OrderGradeType.APP_GRADE.value){
            voiceResult = orderVoiceTaskService.getVoiceTaskResult(order.getQuarter(), order.getId());
            if(voiceResult != null) {
                model.addAttribute("voiceResult", voiceResult);
            }
        }
        int praiseFlag= 0;
        OrderStatusFlag orderStatusFlag =  orderStatusFlagService.getByOrderId(orderId,quarter);
        if(orderStatusFlag!=null && orderStatusFlag.getPraiseStatus()== PraiseStatusEnum.APPROVE.code){
            praiseFlag = 1;
        }
        model.addAttribute("praiseFlag",praiseFlag);
        return VIEW_NAME_ORDER_FOLLOWUP_DETAIL_FORM;
    }

    /**
     * 查看退换货订单明细(For客服)
     */
    @RequestMapping(value = {"service/orderDetailInfoForReturn"})
    public String orderDetailInfoFoReturn(@RequestParam String id, @RequestParam String quarter, String refreshParent, Model model,
                                             HttpServletRequest request) {
        Boolean errorFlag = false;
        Order order = new Order();
        Long orderId = StringUtils.toLong(id);
        boolean hasAuxiliaryMaterils = false;
        if (orderId <= 0) {
            errorFlag = true;
            addMessage(model, "订单代码传递错误");
        } else {
            order = orderService.getOrderById(orderId, quarter, OrderUtils.OrderDataLevel.DETAIL, true,false,true,true);
            if (order == null || order.getOrderCondition() == null) {
                errorFlag = true;
                addMessage(model, "错误：系统繁忙，读取订单失败，请重试。");
            } else {
                if (order.getOrderCondition().getServicePoint() != null && order.getOrderCondition().getServicePoint().getId() != null
                        && order.getOrderCondition().getServicePoint().getId() > 0
                        && order.getOrderCondition().getEngineer() != null && order.getOrderCondition().getEngineer().getId() != null
                        && order.getOrderCondition().getEngineer().getId() > 0) {
                    ServicePoint servicePoint = servicePointService.getFromCache(order.getOrderCondition().getServicePoint().getId());
                    Engineer engineer = servicePointService.getEngineerFromCache(order.getOrderCondition().getServicePoint().getId(), order.getOrderCondition().getEngineer().getId());
                    if (engineer != null) {
                        User engineerUser = new User(engineer.getId());
                        engineerUser.setName(engineer.getName());
                        engineerUser.setMobile(engineer.getContactInfo());
                        engineerUser.setSubFlag(engineer.getMasterFlag() == 1 ? 0 : 1);
//                        int appLoged = servicePoint !=null && servicePoint.getPrimary()!=null && servicePoint.getPrimary().getAppLoged() != null? servicePoint.getPrimary().getAppLoged() : 0;
                        engineerUser.setAppLoged(engineer.getAppLoged());
                        engineerUser.setAppFlag(engineer.getAppFlag());
                        order.getOrderCondition().setEngineer(engineerUser);
                    }
                }
                hasAuxiliaryMaterils = orderAuxiliaryMaterialService.hasAuxiliaryMaterials(order.getId(), order.getQuarter());
            }
        }
        //2020-09-10 屏蔽客户(应收)自动同步加的远程费及其他费用
        User user = UserUtils.getUser();
        if(user.isKefu()) {
            OrderUtils.customerSyncChargeActionShield(order);
        }
        model.addAttribute(MODEL_ATTR_ORDER, order);
        model.addAttribute("hasAuxiliaryMaterils", hasAuxiliaryMaterils? 1 : 0);
        model.addAttribute("errorFlag", errorFlag);
        if (!errorFlag) {
            model.addAttribute("fourServicePhone", MSDictUtils.getDictSingleValue("400ServicePhone", "400-666-3653"));
        } else {
            model.addAttribute("fourServicePhone", "400-666-3653");
        }
        model.addAttribute("refreshParent", StringUtils.isBlank(refreshParent) ? "true" : refreshParent);//调用方法决定是否在关闭详情页后刷新iframe
        String changed = request.getParameter("changed");
        model.addAttribute("changed", StringUtils.isBlank(changed) ? "false" : changed);
        //语音回访 2019/01/16
        Integer voiceResult = null;
        int orderStatusValue = order.getOrderCondition().getStatusValue();
        if(StringUtils.isNotBlank(order.getOrderCondition().getAppCompleteType())
                && orderStatusValue >= Order.ORDER_STATUS_SERVICED
                && order.getOrderCondition().getGradeFlag() != OrderUtils.OrderGradeType.APP_GRADE.value){
            voiceResult = orderVoiceTaskService.getVoiceTaskResult(order.getQuarter(), order.getId());
            if(voiceResult != null) {
                model.addAttribute("voiceResult", voiceResult);
            }
        }
        int praiseFlag= 0;
        OrderStatusFlag orderStatusFlag =  orderStatusFlagService.getByOrderId(orderId,quarter);
        if(orderStatusFlag!=null && orderStatusFlag.getPraiseStatus()== PraiseStatusEnum.APPROVE.code){
            praiseFlag = 1;
        }
        model.addAttribute("praiseFlag",praiseFlag);
        return VIEW_NAME_ORDER_RETURN_DETAIL_FORM;
    }

    //endregion

    /**
     * 设定订单停滞原因 form
     *
     * @param orderId 订单id
     * @param from    调用方 0:订单列表 1:订单明细页
     */
    @RequiresPermissions(value = {"sd:order:service", "sd:order:engineeraccept"}, logical = Logical.OR)
    @RequestMapping(value = "service/pending", method = RequestMethod.GET)
    public String pending(String orderId, String quarter, Long from, HttpServletRequest request, Model model) {
        Order order = new Order();
        OrderCondition condition = new OrderCondition();
        Long orderIdLong = StringUtils.toLong(orderId);
        if (orderIdLong <= 0) {
            addMessage(model, "错误：订单参数无效");
            model.addAttribute("canSave", false);
            model.addAttribute(MODEL_ATTR_ORDER, condition);
            return VIEW_NAME_ORDER_PENDINGTYPE_FORM;
        } else {
            order = orderService.getOrderById(orderIdLong, quarter, OrderUtils.OrderDataLevel.CONDITION, true);
            if (order == null || order.getOrderCondition() == null) {
                addMessage(model, "错误：不允许改约（预约时间17点前（含），需要在当天23点前改约；预约时间17点以后的，需要在第二天23点前完成改约）。");
                model.addAttribute("canSave", false);
                model.addAttribute(MODEL_ATTR_ORDER, condition);
                return VIEW_NAME_ORDER_PENDINGTYPE_FORM;
            }
            if (order.isAllowedForSetAppointment() == 0) {
                addMessage(model, "错误：系统繁忙，读取订单失败，请重试。");
                model.addAttribute("canSave", false);
                model.addAttribute(MODEL_ATTR_ORDER, condition);
                return VIEW_NAME_ORDER_PENDINGTYPE_FORM;
            }
            if (!order.canPendingType()) {
                addMessage(model, String.format("错误：此订单不允许设置停滞原因，当前订单状态:%s", order.getOrderCondition().getStatus().getLabel()));
                model.addAttribute("canSave", false);
                model.addAttribute(MODEL_ATTR_ORDER, condition);
                return VIEW_NAME_ORDER_PENDINGTYPE_FORM;
            }
        }
        String lockkey = String.format(RedisConstant.SD_ORDER_LOCK, orderId);
        if (redisUtils.exists(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey)) {
            addMessage(model, "错误：此订单正在处理中，请稍候重试，或刷新页面。");
            model.addAttribute("canSave", false);
            model.addAttribute(MODEL_ATTR_ORDER, condition);
            return VIEW_NAME_ORDER_PENDINGTYPE_FORM;
        }

        condition = order.getOrderCondition();
        condition.setOrderId(order.getId());
        condition.setRemarks("");
        //按排序取第一个
        List<Dict> pendingTypes = MSDictUtils.getDictExceptList("PendingType", "3");
        if (pendingTypes != null && pendingTypes.size() > 0) {
            condition.setPendingType(pendingTypes.get(0));
        }
        condition.setFeedbackId(from);//调用方

        // 时间取整点时间
        Date date = DateUtils.addDays(new Date(), 1);
        String time = DateUtils.formatDate(date, "yyyy-MM-dd 08:00:00");
        Date appointmentDate = null;
        try {
            appointmentDate = DateUtils.parse(time, "yyyy-MM-dd HH:00:00");
        } catch (java.text.ParseException e) {
            log.error("[OrderController.pending] invalid datetime:{}", time, e);
        }
        // 时间取整点时间
        order.getOrderCondition().setAppointmentDate(appointmentDate);
        model.addAttribute(MODEL_ATTR_ORDER, condition);
        return VIEW_NAME_ORDER_PENDINGTYPE_FORM;
    }

    //region 回访失败

    /**
     * 回访失败(Form)
     */
    @RequiresPermissions(value = {"sd:order:service", "sd:order:engineeraccept"}, logical = Logical.OR)
    @RequestMapping(value = "service/followUpFail", method = RequestMethod.GET)
    public String followUpFail(String orderId, String quarter, Long from, HttpServletRequest request, Model model) {
        Order order;
        OrderCondition condition = new OrderCondition();
        Long orderIdLong = StringUtils.toLong(orderId);
        if (orderIdLong <= 0) {
            addMessage(model, "错误：订单参数无效");
            model.addAttribute("canSave", false);
            model.addAttribute(MODEL_ATTR_ORDER, condition);
            return VIEW_NAME_ORDER_FOLLOWUP_FAIL_FORM;
        } else {
            order = orderService.getOrderById(orderIdLong, quarter, OrderUtils.OrderDataLevel.CONDITION, true);
            if (order == null || order.getOrderCondition() == null) {
                addMessage(model, "错误：读取订单失败，请重试。");
                model.addAttribute("canSave", false);
                model.addAttribute(MODEL_ATTR_ORDER, condition);
                return VIEW_NAME_ORDER_FOLLOWUP_FAIL_FORM;
            }
            if(0 == OrderUtils.canFollowUp(order.getOrderCondition())){
                addMessage(model, "错误：订单已处理，请确认。");
                model.addAttribute("canSave", false);
                model.addAttribute(MODEL_ATTR_ORDER, condition);
                return VIEW_NAME_ORDER_FOLLOWUP_FAIL_FORM;
            }
        }
        String lockkey = String.format(RedisConstant.SD_ORDER_LOCK, orderId);
        if (redisUtils.exists(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey)) {
            addMessage(model, "错误：此订单正在处理中，请稍候重试，或刷新页面。");
            model.addAttribute("canSave", false);
            model.addAttribute(MODEL_ATTR_ORDER, condition);
            return VIEW_NAME_ORDER_FOLLOWUP_FAIL_FORM;
        }

        condition = order.getOrderCondition();
        condition.setRemarks("");
        model.addAttribute("canSave", true);
        model.addAttribute(MODEL_ATTR_ORDER, condition);
        return VIEW_NAME_ORDER_FOLLOWUP_FAIL_FORM;
    }

    /**
     * ajax提交回访失败
     *
     * @param order
     * @param response
     * @return
     */
    @RequiresPermissions("sd:order:return")
    @ResponseBody
    @RequestMapping(value = "service/followUpFail", method = RequestMethod.POST)
    public AjaxJsonEntity saveFollowUpFail(OrderCondition order, HttpServletResponse response)
    {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        if(order == null){
            result.setSuccess(false);
            result.setMessage("传入单据无数据.");
            return result;
        }
        try
        {
            Order o = orderService.getOrderById(order.getOrderId(),order.getQuarter(), OrderUtils.OrderDataLevel.CONDITION,true);
            if(0 == OrderUtils.canFollowUp(o.getOrderCondition())){
                result.setSuccess(false);
                result.setMessage("订单无法设定回访失败，请刷新列表后重试.");
                return result;
            }
            User user = UserUtils.getUser();
            orderService.saveFollowUpFail(order.getOrderId(),o.getOrderNo(),order.getQuarter(),order.getRemarks(),o.getOrderCondition().getStatus(),user,o.getOrderCondition().getServicePoint().getId());
        } catch (OrderException oe){
            result.setSuccess(false);
            result.setMessage(oe.getMessage());
        } catch (Exception e){
            result.setSuccess(false);
            result.setMessage(e.getMessage());
            if(order != null && order.getOrderId() != null) {
                log.error("[saveFollowUpFail] orderId:{}",order.getOrderId(),e);
            }else{
                log.error("[saveFollowUpFail]", e);
            }
        }
        return result;
    }

    //endregion

    //region 上门服务

    /**
     * 添加服务明细窗口(for待回访)
     * @param orderId 订单id
     */
    @RequiresPermissions(value = { "sd:order:service" })
    @RequestMapping(value = "service/addServiceForFollowUp")
    public String addServiceForFollowUp(@RequestParam String orderId,@RequestParam(required = false) Integer addType, Model model) {
        Long customerId = 0L;
        int dataSource = 0;
        model.addAttribute("customerId",customerId);
        model.addAttribute("dataSource",dataSource);
        Long lorderId = Long.valueOf(orderId);
        if (lorderId == null || lorderId <= 0) {
            addMessage(model, "参数错误");
            return VIEW_NAME_ORDER_FOLLOWUP_ADD_SERVICE_FORM;
        }
        String lockkey = String.format(RedisConstant.SD_ORDER_LOCK,orderId);
        if(redisUtils.exists(RedisConstant.RedisDBType.REDIS_LOCK_DB,lockkey)){
            addMessage(model, "错误：此订单正在处理中，请稍候重试，或刷新页面。");
            model.addAttribute("canService","false");
            return VIEW_NAME_ORDER_FOLLOWUP_ADD_SERVICE_FORM;
        }

        Order order = orderService.getOrderById(lorderId,"", OrderUtils.OrderDataLevel.DETAIL, true);
        if (order == null || order.getOrderCondition() == null) {
            addMessage(model, "错误：系统繁忙，读取订单失败，请重试");
            model.addAttribute("canService","false");
            return VIEW_NAME_ORDER_FOLLOWUP_ADD_SERVICE_FORM;
        }
        if (!order.canService()) {
            addMessage(model, "错误：此订单不能添加上门服务具体服务项目，请刷新订单列查看订单处理状态。");
            model.addAttribute("canService","false");
            return VIEW_NAME_ORDER_FOLLOWUP_ADD_SERVICE_FORM;
        }
        //2020-10-21 从主库读取派单时预设的费用和单号
        OrderFee feeMaster = orderService.getPresetFeeWhenPlanFromMasterDB(order.getId(),order.getQuarter());
        if(feeMaster != null){
            OrderFee orderFee = order.getOrderFee();
            if(orderFee != null){
                orderFee.setPlanTravelCharge(feeMaster.getPlanTravelCharge());
                orderFee.setPlanTravelNo(feeMaster.getPlanTravelNo());
                orderFee.setPlanDistance(feeMaster.getPlanDistance());
                orderFee.setCustomerPlanTravelCharge(feeMaster.getCustomerPlanTravelCharge());
                orderFee.setPlanOtherCharge(feeMaster.getPlanOtherCharge());
                orderFee.setCustomerPlanOtherCharge(feeMaster.getCustomerPlanOtherCharge());
            }
        }
        //厂商设置远程费标识
        OrderCondition orderCondition = order.getOrderCondition();
        customerId = Optional.ofNullable(orderCondition.getCustomer()).map(t->t.getId()).orElse(0L);
        dataSource = order.getDataSourceId();
        //2020-09-24 接入云米，增加经纬度检查
        AjaxJsonEntity locationCheckResult = orderService.checkAddressLocation(dataSource,orderCondition.getOrderId(),orderCondition.getQuarter());
        if(!locationCheckResult.getSuccess()){
            addMessage(model, "错误：因" + locationCheckResult.getMessage() + "，不能上门服务");
            model.addAttribute("canService","false");
            return VIEW_NAME_ORDER_FOLLOWUP_ADD_SERVICE_FORM;
        }
        // 检查是否为受控品类：自动同步应收远程费和其他费用
        Integer customerRemoteFee = 1;
        Dict dict = MSDictUtils.getDictByValue(orderCondition.getProductCategoryId().toString(), OrderUtils.SYNC_CUSTOMER_CHARGE_DICT);
        if (dict != null && dict.getValue().equals(orderCondition.getProductCategoryId().toString())) {
            customerRemoteFee = 0;
        } else if (customerId > 0) {
            customerRemoteFee = orderCondition.getCustomer().getRemoteFeeFlag();
        }

        //区域远程费
        Integer areaRemoteFee = msRegionPermissionService.getRemoteFeeStatusFromCacheForSD(orderCondition.getProductCategoryId(),orderCondition.getArea().getId(),orderCondition.getSubArea().getId());
        OrderDetail detail = new OrderDetail();
        detail.setQuarter(order.getQuarter());
        detail.setOrderId(lorderId);
        if (addType != null) {
            detail.setAddType(addType);
        }
        OrderCondition condition = order.getOrderCondition();
        OrderStatus orderStatus = order.getOrderStatus();
        detail.setServicePoint(condition.getServicePoint());
        Engineer engineer = new Engineer(condition.getEngineer().getId());
        engineer.setName(condition.getEngineer().getName());
        detail.setEngineer(engineer);

        Integer times = condition.getServiceTimes();
        times = times + 1;
        detail.setServiceTimes(times);
        detail.setOrderServiceTimes(times);
        detail.setRemarks(orderStatus.getPlanComment());// 2015-01-16
        // 显示派单时的备注
        OrderItem orderItem = order.getItems().get(0);
        detail.setProduct(orderItem.getProduct());
        detail.setBrand(orderItem.getBrand());
        detail.setProductSpec(orderItem.getProductSpec());
        detail.setQty(orderItem.getQty());
        detail.setServiceType(orderItem.getServiceType());
        detail.setTravelNo(order.getOrderFee().getPlanTravelNo());// 派单时预设的远程单号
        //订单类型 2019-12-02
        detail.setServiceCategory(new Dict(condition.getOrderServiceType(),""));
        //服务类型列表(来自数据字典)
        List<Dict> serviceCategories = MSDictUtils.getDictList(Dict.DICT_TYPE_ORDER_SERVICE_TYPE);
        if(CollectionUtils.isNotEmpty(serviceCategories)){
            serviceCategories = serviceCategories.stream().filter(t->t.getIntValue()>0).collect(Collectors.toList());
        }
        //2020-11-22 远程费+其他费用总费用受控品类
        //合计费用超过设定金额，不允许派单
        //费用不超过设定金额，应收为0
        Dict limitRemoteDict = MSDictUtils.getDictByValue(condition.getProductCategoryId().toString(), OrderUtils.LIMIT_REMOTECHARGE_CATEGORY_DICT);
        if(limitRemoteDict != null){
            double limitRemoteCharge = 0.0;
            if(limitRemoteDict.getSort()>0){
                limitRemoteCharge = Double.valueOf(limitRemoteDict.getSort());
            }
            model.addAttribute("limitRemoteCharge",limitRemoteCharge);
        }
        model.addAttribute("serviceCategories",serviceCategories);
        model.addAttribute("canService","true");
        model.addAttribute("item", detail);
        model.addAttribute("order", order);
        model.addAttribute("customerRemoteFee",customerRemoteFee);
        model.addAttribute("areaRemoteFee",areaRemoteFee);
        model.addAttribute("customerId",customerId);
        model.addAttribute("dataSource",dataSource);
        return VIEW_NAME_ORDER_FOLLOWUP_ADD_SERVICE_FORM;
    }

    /**
     * 修改服务明细窗口(for待回访)
     * @param orderId 订单id
     */
    @RequiresPermissions(value = { "sd:order:service" })
    @RequestMapping(value = "service/editServiceForFollowUp")
    public String editServiceForFollowUp(@RequestParam String orderId,@RequestParam String detailId,@RequestParam String quarter, Model model) {
        User user = UserUtils.getUser();
        Long customerId = 0L;
        int dataSource = 0;
        model.addAttribute("customerId",customerId);
        model.addAttribute("dataSource",dataSource);
        Long lorderId = Long.valueOf(orderId);
        if (lorderId == null || lorderId <= 0) {
            addMessage(model, "参数错误");
            return VIEW_NAME_ORDER_FOLLOWUP_EDIT_SERVICE_FORM;
        }

        Long ldetailId = Long.valueOf(detailId);
        if (ldetailId == null || ldetailId <= 0) {
            addMessage(model, "参数错误");
            return VIEW_NAME_ORDER_FOLLOWUP_EDIT_SERVICE_FORM;
        }

        Order order = orderService.getOrderById(lorderId,quarter, OrderUtils.OrderDataLevel.DETAIL, true);
        if (order == null || order.getOrderCondition() == null) {
            addMessage(model, "错误：系统繁忙，读取订单失败，请重试");
            model.addAttribute("canService","false");
            return VIEW_NAME_ORDER_FOLLOWUP_EDIT_SERVICE_FORM;
        }
        if (!order.canService()) {
            addMessage(model, "错误：此订单不能修改上门服务项目，请刷新订单列查看订单处理状态。");
            model.addAttribute("canService","false");
            return VIEW_NAME_ORDER_FOLLOWUP_EDIT_SERVICE_FORM;
        }
        List<OrderDetail> details = order.getDetailList();
        if(details== null || details.isEmpty()){
            addMessage(model, "错误：此订单无上门服务项目。");
            model.addAttribute("canService","false");
            return VIEW_NAME_ORDER_FOLLOWUP_EDIT_SERVICE_FORM;
        }
        OrderDetail detail = details.stream().filter(t->t.getId().equals(ldetailId)).findFirst().orElse(null);
        if(detail == null){
            addMessage(model, "错误：此订单无此上门服务项目，请确认。");
            model.addAttribute("canService","false");
            return VIEW_NAME_ORDER_FOLLOWUP_EDIT_SERVICE_FORM;
        }
        if(detail.getDelFlag() == 1){
            addMessage(model, "错误：此上门服务项目已删除。");
            model.addAttribute("canService","false");
            return VIEW_NAME_ORDER_FOLLOWUP_EDIT_SERVICE_FORM;
        }
        //2020-10-21 从主库读取派单时预设的费用和单号
        OrderFee feeMaster = orderService.getPresetFeeWhenPlanFromMasterDB(order.getId(),order.getQuarter());
        if(feeMaster != null){
            OrderFee orderFee = order.getOrderFee();
            if(orderFee != null){
                orderFee.setPlanTravelCharge(feeMaster.getPlanTravelCharge());
                orderFee.setPlanTravelNo(feeMaster.getPlanTravelNo());
                orderFee.setPlanDistance(feeMaster.getPlanDistance());
                orderFee.setCustomerPlanTravelCharge(feeMaster.getCustomerPlanTravelCharge());
                orderFee.setPlanOtherCharge(feeMaster.getPlanOtherCharge());
                orderFee.setCustomerPlanOtherCharge(feeMaster.getCustomerPlanOtherCharge());
            }
        }
        OrderCondition condition = order.getOrderCondition();
        //厂商设置远程费标识
        customerId = Optional.ofNullable(condition.getCustomer()).map(t->t.getId()).orElse(0L);
        dataSource = order.getDataSourceId();
        //2020-09-24 接入云米，增加经纬度检查
        AjaxJsonEntity locationCheckResult = orderService.checkAddressLocation(dataSource,condition.getOrderId(),condition.getQuarter());
        if(!locationCheckResult.getSuccess()){
            addMessage(model, "错误：因" + locationCheckResult.getMessage() + "，不能修改上门服务");
            model.addAttribute("canService","false");
            return VIEW_NAME_ORDER_FOLLOWUP_ADD_SERVICE_FORM;
        }
        // 检查是否为受控品类：自动同步应收远程费和其他费用
        Integer customerRemoteFee = 1;
        Dict dict = MSDictUtils.getDictByValue(condition.getProductCategoryId().toString(), OrderUtils.SYNC_CUSTOMER_CHARGE_DICT);
        if (dict != null && dict.getValue().equals(condition.getProductCategoryId().toString())) {
            customerRemoteFee = 0;
        } else if (customerId > 0) {
            customerRemoteFee = condition.getCustomer().getRemoteFeeFlag();
        }
        if(customerRemoteFee == 0){
            //应收的其他和远程费用与应付相同，这里清零，保存时再自动同步处理
            detail.setOtherCharge(0.00);
            detail.setTravelCharge(0.00);
        }
        //区域远程费
        Integer areaRemoteFee = msRegionPermissionService.getRemoteFeeStatusFromCacheForSD(condition.getProductCategoryId(),condition.getArea().getId(),condition.getSubArea().getId());
        //订单类型 2019-12-02
        //detail.setServiceCategory(new Dict(condition.getOrderServiceType(),""));
        //服务类型列表(来自数据字典)
        List<Dict> serviceCategories = MSDictUtils.getDictList(Dict.DICT_TYPE_ORDER_SERVICE_TYPE);
        if(CollectionUtils.isNotEmpty(serviceCategories)){
            serviceCategories = serviceCategories.stream().filter(t->t.getIntValue()>0).collect(Collectors.toList());
        }
        model.addAttribute("serviceCategories",serviceCategories);
        //2020-11-22 远程费+其他费用总费用受控品类
        //合计费用超过设定金额，不允许派单
        //费用不超过设定金额，应收为0
        Dict limitRemoteDict = MSDictUtils.getDictByValue(condition.getProductCategoryId().toString(), OrderUtils.LIMIT_REMOTECHARGE_CATEGORY_DICT);
        if(limitRemoteDict != null){
            model.addAttribute("limitRemoteCharge",limitRemoteDict.getSort());
            //应收的其他和远程费用清零
            detail.setOtherCharge(0.00);
            detail.setTravelCharge(0.00);
        }
        //编辑上门服务时使用
        List<MDErrorType> errorTypes = null;
        List<MDErrorCode> errorCodes = null;
        List<MDActionCodeDto> actionCodes = null;
        List<ServiceType> serviceTypes = null;
        //非安装
        long pid = detail.getProduct().getId();
        if(detail.getServiceCategory().getIntValue() > 1){
            errorTypes = msCustomerErrorTypeService.findListByProductIdAndCustomerIdFromCache(pid,customerId);
            if(detail.getErrorType().getId() > 0) {
                errorCodes = msErrorCodeService.findListByProductAndErrorType(detail.getErrorType().getId(), pid);
                if(detail.getErrorCode().getId() > 0) {
                    actionCodes = msActionCodeService.findListByProductAndErrorCode(detail.getErrorCode().getId(),pid);
                    /*
                    if(detail.getActionCode().getId() > 0){
                        //目前一个故障处理对应一个服务类型
                        serviceTypes = Lists.newArrayList(detail.getServiceType());
                    }else{
                        //按订单类型装载多个服务类型
                        serviceTypes = serviceTypeService.findListOfOrderType(detail.getServiceCategory().getIntValue());
                    }*/
                }
            }/*else{
                //按订单类型装载多个服务类型
                serviceTypes = serviceTypeService.findListOfOrderType(detail.getServiceCategory().getIntValue());
            }*/
            serviceTypes = serviceTypeService.findListOfOrderType(detail.getServiceCategory().getIntValue());
        }else{
            //安装
            serviceTypes = serviceTypeService.findListOfOrderType(detail.getServiceCategory().getIntValue());
        }
        detail.setHasErrorType(1);
        if(CollectionUtils.isEmpty(errorTypes)){
            detail.setHasErrorType(0);
        }
        model.addAttribute("errorTypes",errorTypes==null?Lists.newArrayList():errorTypes);
        model.addAttribute("errorCodes",errorCodes==null?Lists.newArrayList():errorCodes);
        model.addAttribute("actionCodes",actionCodes==null?Lists.newArrayList():actionCodes);
        model.addAttribute("serviceTypes",serviceTypes==null?Lists.newArrayList():serviceTypes);
        //service Types
        model.addAttribute("canService","true");
        model.addAttribute("item", detail);
        model.addAttribute("order", order);
        model.addAttribute("customerRemoteFee",customerRemoteFee);
        model.addAttribute("areaRemoteFee",areaRemoteFee);
        model.addAttribute("customerId",customerId);
        model.addAttribute("dataSource",dataSource);
        return VIEW_NAME_ORDER_FOLLOWUP_EDIT_SERVICE_FORM;
    }

    // ajax 修改服务明细(for待回访)
    @RequiresPermissions(value = "sd:order:service")
    @ResponseBody
    @RequestMapping(value = "service/saveServiceForFollowUp")
    public AjaxJsonEntity saveServiceForFollowUp(OrderDetail detail, Model model, HttpServletResponse response)
    {
        User user = UserUtils.getUser();

        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity jsonEntity = new AjaxJsonEntity();
        jsonEntity.setSuccess(true);

        if (!beanValidator(model, detail))
        {
            jsonEntity.setSuccess(false);
            if (model.containsAttribute("message"))
            {
                jsonEntity.setMessage((String) model.asMap().get("message"));
            } else
            {
                jsonEntity.setMessage("输入错误，请检查。");
            }
            return jsonEntity;
        }

        try
        {
            Date date = new Date();
            detail.setCreateBy(user);
            detail.setCreateDate(date);
            detail.setUpdateBy(user);
            detail.setUpdateDate(date);
            if(detail.getId() == null || detail.getId()==0){
                processOrderListService.addDetailForFollowUp(detail);
            }else {
                processOrderListService.editDetailForFollowUp(detail);
            }
        } catch (Exception e)
        {
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage(e.getMessage());
        }

        return jsonEntity;
    }

    /**
     * 删除订单实际服务项目
     *
     * @param id 服务项目ID
     */
    @RequiresPermissions("sd:order:service")
    @ResponseBody
    @RequestMapping(value = "service/ajaxDelServiceForFollowUp")
    public AjaxJsonEntity ajaxDelServiceForFollowUp(@RequestParam(required = false) String id, @RequestParam(required = false) String orderId, HttpServletResponse response)
    {
        User user = UserUtils.getUser();
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity jsonEntity = new AjaxJsonEntity();
        jsonEntity.setSuccess(true);
        Long lid = Long.valueOf(id);
        if (lid == null || lid <=0)
        {
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage("服务项目编号错误，无法删除");
            return jsonEntity;
        }

        try {
            Long lorderId = Long.valueOf(orderId);
            if(lorderId==null || lorderId<=0){
                jsonEntity.setSuccess(false);
                jsonEntity.setMessage("订单id错误，无法删除");
            }else {
                Date date = new Date();
                OrderDetail detail = new OrderDetail();
                detail.setOrderId(lorderId);
                detail.setId(lid);
                detail.setCreateBy(user);
                detail.setCreateDate(date);
                processOrderListService.deleteDetailForFollowUp(detail);
                jsonEntity.setMessage("删除服务项目成功");
            }
        }catch (Exception e)
        {
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage(e.getMessage());
        }

        return jsonEntity;
    }


    /**
     * 确认上门(ajax for待回访)
     *
     * @param orderId
     * @param confirmType 确认类型: 0-客服 1-网点
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "service/confirmDoorAutoForFollowUp", method = RequestMethod.POST)
    public AjaxJsonEntity confirmDoorAutoForFollowUp(String orderId,String quarter, Integer confirmType, HttpServletResponse response)
    {
        User user = UserUtils.getUser();
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try
        {
            if(confirmType == null){
                confirmType = 0;
            }
            Long lorderId = Long.valueOf(orderId);
            processOrderListService.confirmDoorAutoForFollowUp(lorderId,quarter,user,confirmType);
        } catch (OrderException oe){
            result.setSuccess(false);
            result.setMessage(oe.getMessage());
        } catch (Exception e){
            result.setSuccess(false);
            result.setMessage(e.getMessage());
            log.error("[OrderController.confirmDoorAuto] orderId:{}",orderId, e);
        }
        return result;
    }

    /**
     * 取消APP异常(ajax)
     *
     * @param orderId
     * @param response
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "orderDealAPPException", method = RequestMethod.POST)
    public AjaxJsonEntity orderDealAPPException(String orderId,String quarter, HttpServletResponse response)
    {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try
        {
            Long lorderId = Long.valueOf(orderId);
            orderService.dealAPPException(lorderId,quarter);
        } catch (OrderException oe){
            result.setSuccess(false);
            result.setMessage(oe.getMessage());
        } catch (Exception e){
            result.setSuccess(false);
            result.setMessage("订单异常处理发生异常:" + e.getMessage());
            log.error("[OrderController.orderDealAPPException] orderId:{}",orderId, e);
        }
        return result;
    }


    /**
     * 转到预约到期列表(ajax)
     *
     * @param orderId
     * @param response
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "service/toArriveAppointment", method = RequestMethod.POST)
    public AjaxJsonEntity toArriveAppointment(String orderId,String quarter, HttpServletResponse response)
    {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try
        {
            User user = UserUtils.getUser();
            Long lorderId = Long.valueOf(orderId);
            Order order = new Order(lorderId);
            order.setQuarter(quarter);
            order.setCreateBy(user);
            processOrderListService.toArriveAppointment(order);
        } catch (OrderException oe){
            result.setSuccess(false);
            result.setMessage(oe.getMessage());
        } catch (Exception e){
            result.setSuccess(false);
            result.setMessage("移转预约到期处理发生异常:" + e.getMessage());
            log.error("[toArriveAppointment] orderId:{}",orderId, e);
        }
        return result;
    }

    //endregion 上门服务

    //endregion 客服操作

    @RequestMapping(value="locateAddress")
    public String locateAddress(String address, Model model) {
        try {
            String addr =java.net.URLDecoder.decode(address,"UTF-8");  //进行解码，会抛异常，直接捕获即可。
            String[] areaArray = AreaUtils.getLocation(address);
            Double  centerLongtitude =0.0,
                    centerLatitude = 0.0;

            if (!ObjectUtils.isEmpty(areaArray) && areaArray.length == 2) {
                centerLongtitude = Double.valueOf(areaArray[0]);
                centerLatitude = Double.valueOf(areaArray[1]);
            }
            model.addAttribute("centerLng",centerLongtitude);
            model.addAttribute("centerLat",centerLatitude);
            model.addAttribute("address", addr);
        } catch (UnsupportedEncodingException e) {
            log.error("地址解析失败",e);
        }
        return "modules/servicePoint/sd/processOrderList/locateServiceAddress";
    }

    /**
     * 根据客户id和产品获取产品安装规范
     *
     * @param customerId
     * @param productId
     * @return
     */
    @RequestMapping("getProductFixSpec")
    public String getProductFixSpec(Long customerId,Long productId,Model model){
        model.addAttribute("canSave",true);
        CustomerProduct customerProduct = msCustomerProductService.getFixSpecFromCache(customerId,productId);
        if(customerProduct!=null){
            //html转义处理
            String unescape = StringEscapeUtils.unescapeHtml4(customerProduct.getFixSpec());
            //String text  = Jsoup.clean(unescape, Whitelist.simpleText());
            customerProduct.setFixSpec(unescape);
            model.addAttribute("customerProduct",customerProduct);
        }else{
            model.addAttribute("customerProduct",new CustomerProduct());
        }
        return "modules/servicePoint/sd/processOrderList/service/productFixSpecView";
    }

    /**
     * 网点中西客服工单详情页面查看互助金额
     * @param strOrderId
     * @param strOrderId
     * @return
     */
    @RequestMapping("getOrderInsurance")
    @ResponseBody
    public AjaxJsonEntity getOrderInsurance(String strOrderId,String quarter){
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        try {
            Long orderId = Long.valueOf(strOrderId);
            List<OrderInsurance> list = processOrderListService.getOrderInsurance(orderId,quarter);
            ajaxJsonEntity.setData(list);
        }catch (Exception e){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage(e.getMessage());
        }
        return ajaxJsonEntity;
    }
}

