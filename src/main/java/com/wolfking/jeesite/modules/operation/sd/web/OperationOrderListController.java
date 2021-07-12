package com.wolfking.jeesite.modules.operation.sd.web;


import com.google.common.collect.Lists;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.persistence.IntegerRange;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.UrgentLevel;
import com.wolfking.jeesite.modules.md.service.UrgentLevelService;
import com.wolfking.jeesite.modules.operation.sd.service.OperationOrderListService;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.entity.viewModel.HistoryPlanOrderModel;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderSearchModel;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.b2bcenter.md.utils.B2BMDUtils;
import com.wolfking.jeesite.ms.tmall.md.entity.B2bCustomerMap;
import com.wolfking.jeesite.ms.tmall.md.service.B2bCustomerMapService;
import com.wolfking.jeesite.ms.tmall.sd.entity.TmallAnomalyRecourse;
import com.wolfking.jeesite.ms.tmall.sd.entity.TmallServiceMonitor;
import com.wolfking.jeesite.ms.tmall.sd.entity.ViewModel.TmallAnomalyRecourseSearchVM;
import com.wolfking.jeesite.ms.tmall.sd.entity.ViewModel.TmallServiceMonitorSearchVM;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;


/**
 * 运营部订单列表
 * 复制客服订单列表
 *
 * @author Ryan
 * @date 2021/02/22
 *
 */
@Controller
@RequestMapping(value = "${adminPath}/operation/sd/orderList/")
@Slf4j
public class OperationOrderListController extends BaseController {

    private static final int DEFAULT_PAGE_SIZE = 12;

    @Autowired
    private UrgentLevelService urgentLevelService;

    @Autowired
    private B2bCustomerMapService b2bCustomerMapService;

    @Autowired
    private OperationOrderListService operationOrderListService;

    private static final String MODEL_ATTR_PAGE = "page";
    private static final String MODEL_ATTR_ORDER = "order";

    private static final String VIEW_NAME_PLANNING_LIST = "modules/operation/sd/orderList/planingList";
    private static final String VIEW_NAME_NOAPPOINTMENT_LIST = "modules/operation/sd/orderList/noAppointmentList";
    private static final String VIEW_NAME_ARRIVEAPPOINTMENT_LIST = "modules/operation/sd/orderList/arriveAppointmentList";
    private static final String VIEW_NAME_PASSAPPOINTMENT_LIST = "modules/operation/sd/orderList/passAppointmentList";
    private static final String VIEW_NAME_PENDING_LIST = "modules/operation/sd/orderList/pendingList";
    private static final String VIEW_NAME_SERVICED_LIST = "modules/operation/sd/orderList/servicedList";
    private static final String VIEW_NAME_FOLLOWUP_FAIL_LIST = "modules/operation/sd/orderList/followUpFailList";
    private static final String VIEW_NAME_UMCOMPLETED_LIST = "modules/operation/sd/orderList/uncompletedList";
    private static final String VIEW_NAME_ALL_LIST = "modules/operation/sd/orderList/allList";
    private static final String VIEW_NAME_COMPLETED_LIST = "modules/operation/sd/orderList/completedList";
    private static final String VIEW_NAME_REMINDER_LIST = "modules/operation/sd/orderList/reminderList";

    private static final String VIEW_NAME_TMALLANOMALY_LIST = "modules/operation/sd/orderList/tmallAnomalyList";
    private static final String VIEW_NAME_TMALLSERVICEMONITOR_LIST = "modules/operation/sd/orderList/tmallServiceMonitorList";
    private static final String VIEW_NAME_RUSHING_LIST = "modules/operation/sd/orderList/rushingList";
    private static final String VIEW_NAME_COMPLAIN_LIST = "modules/operation/sd/orderList/complainList";


    /**
     * 设置必须的查询条件增加自动客服
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
        Area area = searchModel.getArea();
        if(area == null){
            area = new Area(0L);
            searchModel.setArea(area);
        }
        if(area.getParent()==null || area.getParent().getId() == null){
            area.setParent(new Area(0L));
        }
        String checkRegion = operationOrderListService.loadAndCheckUserRegions(searchModel,user);
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
        Boolean isValide = checkOrderNoAndPhone(order,model,page);
        if(!isValide){
            return VIEW_NAME_PLANNING_LIST;
        }
        try {
            page = operationOrderListService.findKefuPlaningOrderList(new Page<>(request, response, DEFAULT_PAGE_SIZE), order, true);
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
        Boolean isValide = checkOrderNoAndPhone(order,model,page);
        if(!isValide){
            return VIEW_NAME_NOAPPOINTMENT_LIST;
        }
        try {
            //加急
            loadUrgentLevels(order);
            page = operationOrderListService.findKefuNoAppointmentOrderList(new Page<>(request, response, DEFAULT_PAGE_SIZE), order, true);
        } catch (Exception e) {
            addMessage(model, "错误：" + e.getMessage());
        }
        model.addAttribute(MODEL_ATTR_PAGE, page);
        model.addAttribute(MODEL_ATTR_ORDER, order);

        //记录反馈，反馈内容条件搜索频率情况
        if(order.getReplyFlagCustomer()==2){
            LogUtils.saveLog(request,null,null,"客服工单,未预约列表,未回复反馈搜索查询","kefuNoAppointmentList",user);
        }
        if(StringUtils.isNotBlank(order.getRemarks())){
            LogUtils.saveLog(request,null,null,"客服工单,未预约列表,反馈内容搜索查询","kefuNoAppointmentList",user);
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
        Boolean isValide = checkOrderNoAndPhone(order,model,page);
        if(!isValide){
            return VIEW_NAME_ARRIVEAPPOINTMENT_LIST;
        }
        try {
            //加急
            loadUrgentLevels(order);
            page = operationOrderListService.findKefuArriveAppointmentOrderList(new Page<>(request, response, DEFAULT_PAGE_SIZE), order, true);
        } catch (Exception e) {
            addMessage(model, "错误：" + e.getMessage());
        }
        model.addAttribute(MODEL_ATTR_PAGE, page);
        model.addAttribute(MODEL_ATTR_ORDER, order);

        //记录反馈，反馈内容条件搜索频率情况
        if(order.getReplyFlagCustomer()==2){
            LogUtils.saveLog(request,null,null,"客服工单,预约到期列表,未回复反馈搜索查询","kefuArriveAppointmentList",user);
        }
        if(StringUtils.isNotBlank(order.getRemarks())){
            LogUtils.saveLog(request,null,null,"客服工单,预约到期列表,反馈内容搜索查询","kefuArriveAppointmentList",user);
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
        Boolean isValide = checkOrderNoAndPhone(order,model,page);
        if(!isValide){
            return VIEW_NAME_PASSAPPOINTMENT_LIST;
        }
        try {
            //加急
            loadUrgentLevels(order);
            page = operationOrderListService.findKefuPassAppointmentOrderList(new Page<>(request, response, DEFAULT_PAGE_SIZE), order, true);
        } catch (Exception e) {
            addMessage(model, "错误：" + e.getMessage());
        }
        model.addAttribute(MODEL_ATTR_PAGE, page);
        model.addAttribute(MODEL_ATTR_ORDER, order);

        //记录反馈，反馈内容条件搜索频率情况
        if(order.getReplyFlagCustomer()==2){
            LogUtils.saveLog(request,null,null,"客服工单,预约超期列表,未回复反馈搜索查询","kefuPassAppointmentList",user);
        }
        if(StringUtils.isNotBlank(order.getRemarks())){
            LogUtils.saveLog(request,null,null,"客服工单,预约超期列表,反馈内容搜索查询","kefuPassAppointmentList",user);
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
        Boolean isValide = checkOrderNoAndPhone(order,model,page);
        if(!isValide){
            return VIEW_NAME_PENDING_LIST;
        }
        try {
            loadUrgentLevels(order);
            page = operationOrderListService.findKefuPendingOrderList(new Page<>(request, response, DEFAULT_PAGE_SIZE), order, true);
        } catch (Exception e) {
            addMessage(model, "错误：" + e.getMessage());
        }
        model.addAttribute(MODEL_ATTR_PAGE, page);
        model.addAttribute(MODEL_ATTR_ORDER, order);

        //记录反馈，反馈内容条件搜索频率情况
        if(order.getReplyFlagCustomer()==2){
            LogUtils.saveLog(request,null,null,"客服工单,停滞列表,未回复反馈搜索查询","kefuPendinglist",user);
        }
        if(StringUtils.isNotBlank(order.getRemarks())){
            LogUtils.saveLog(request,null,null,"客服工单,停滞列表,反馈内容搜索查询","kefuPendinglist",user);
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
        Boolean isValide = checkOrderNoAndPhone(order,model,page);
        if(!isValide){
            return VIEW_NAME_SERVICED_LIST;
        }
        try {
            //加急
            loadUrgentLevels(order);
            page = operationOrderListService.findKefuServicedOrderList(new Page<>(request, response, DEFAULT_PAGE_SIZE), order, true);
        } catch (Exception e) {
            addMessage(model, "错误：" + e.getMessage());
        }
        model.addAttribute(MODEL_ATTR_PAGE, page);
        model.addAttribute(MODEL_ATTR_ORDER, order);

        //记录反馈，反馈内容条件搜索频率情况
        if(order.getReplyFlagCustomer()==2){
            LogUtils.saveLog(request,null,null,"客服工单,待回访列表,未回复反馈搜索查询","kefuServicedList",user);
        }
        if(StringUtils.isNotBlank(order.getRemarks())){
            LogUtils.saveLog(request,null,null,"客服工单,待回访列表,反馈内容搜索查询","kefuServicedList",user);
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
        Boolean isValide = checkOrderNoAndPhone(order,model,page);
        if(!isValide){
            return VIEW_NAME_FOLLOWUP_FAIL_LIST;
        }
        try {
            //加急
            loadUrgentLevels(order);
            page = operationOrderListService.findKefuFollowUpFailOrderList(new Page<>(request, response, DEFAULT_PAGE_SIZE), order, true);
        } catch (Exception e) {
            addMessage(model, "错误：" + e.getMessage());
        }
        model.addAttribute(MODEL_ATTR_PAGE, page);
        model.addAttribute(MODEL_ATTR_ORDER, order);

        //记录反馈，反馈内容条件搜索频率情况
        if(order.getReplyFlagCustomer()==2){
            LogUtils.saveLog(request,null,null,"客服工单,回访失败列表,未回复反馈搜索查询","kefuFollowUpFailList",user);
        }
        if(StringUtils.isNotBlank(order.getRemarks())){
            LogUtils.saveLog(request,null,null,"客服工单,回访失败列表,反馈内容搜索查询","kefuFollowUpFailList",user);
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
        Boolean isValide = checkOrderNoAndPhone(order,model,page);
        if(!isValide){
            return VIEW_NAME_UMCOMPLETED_LIST;
        }
        try {
            //加急
            loadUrgentLevels(order);
            page = operationOrderListService.findKefuUncompletedOrderList(new Page<>(request, response, DEFAULT_PAGE_SIZE), order, true);
        } catch (Exception e) {
            addMessage(model, "错误：" + e.getMessage());
        }
        model.addAttribute(MODEL_ATTR_PAGE, page);
        model.addAttribute(MODEL_ATTR_ORDER, order);

        //记录反馈，反馈内容条件搜索频率情况
        if(order.getReplyFlagCustomer()==2){
            LogUtils.saveLog(request,null,null,"客服工单,未完成列表,未回复反馈搜索查询","kefuUncompletedList",user);
        }
        if(StringUtils.isNotBlank(order.getRemarks())){
            LogUtils.saveLog(request,null,null,"客服工单,未完成列表,反馈内容搜索查询","kefuUncompletedList",user);
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
            Boolean isValide = checkOrderNoAndPhone(order,model,page);
            if(!isValide){
                return viewForm;
            }
            try {
                //page = kefuOrderListService.findKefuAllOrderList(new Page<>(request, response, DEFAULT_PAGE_SIZE), order, true);
                page = operationOrderListService.findKefuAllOrderList(new Page<>(request, response, DEFAULT_PAGE_SIZE), order, true);
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
            LogUtils.saveLog(request,null,null,"客服工单,所有列表,未回复反馈搜索查询","kefuAllList",user);
        }
        if(StringUtils.isNotBlank(order.getRemarks())){
            LogUtils.saveLog(request,null,null,"客服工单,所有列表,反馈内容搜索查询","kefuAllList",user);
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
            Boolean isValide = checkOrderNoAndPhone(order,model,page);
            if(!isValide){
                return viewForm;
            }
            try {
                page = operationOrderListService.findKefuCompletedOrderList(new Page<>(request, response, DEFAULT_PAGE_SIZE), order, true);
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
            LogUtils.saveLog(request,null,null,"客服工单,已完成列表,未回复反馈搜索查询","kefuCompletedList",user);
        }
        if(StringUtils.isNotBlank(order.getRemarks())){
            LogUtils.saveLog(request,null,null,"客服工单,已完成列表,反馈内容搜索查询","kefuCompletedList",user);
        }

        return viewForm;
    }

    //endregion 普通列表

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
        String checkRegion = operationOrderListService.loadAndCheckUserRegions(searchEntity,user);
        if(StringUtils.isNotEmpty(checkRegion)){
            addMessage(model, checkRegion);
            model.addAttribute("page", page);
            model.addAttribute("searchEntity", searchEntity);
            return VIEW_NAME_TMALLANOMALY_LIST;
        }
        try {
            //查询
            page = operationOrderListService.findKefuTmallAnomalyList(new Page<>(request, response, DEFAULT_PAGE_SIZE), searchEntity);
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
        String checkRegion = operationOrderListService.loadAndCheckUserRegions(tmallServiceMonitor,user);
        if(StringUtils.isNotEmpty(checkRegion)){
            addMessage(model, checkRegion);
            model.addAttribute("page", page);
            model.addAttribute("entity", tmallServiceMonitor);
            return VIEW_NAME_TMALLSERVICEMONITOR_LIST;
        }
        page = operationOrderListService.findKefuTmallServiceMonitorList(new Page<>(request, response, DEFAULT_PAGE_SIZE), tmallServiceMonitor);
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
        Boolean isValide = checkOrderNoAndPhone(order,model,page);
        if(!isValide){
            return VIEW_NAME_RUSHING_LIST;
        }
        //客服主管
        order.setOrderDataLevel(OrderUtils.OrderDataLevel.DETAIL);//从数据库/redis中读取具体的数据内容
        try {
            //加急
            loadUrgentLevels(order);
            page = operationOrderListService.findKefuRushingOrderList(new Page<>(request, response, DEFAULT_PAGE_SIZE), order, true);
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
        Boolean isValide = checkOrderNoAndPhone(order,model,page);
        if(!isValide){
            return VIEW_NAME_COMPLAIN_LIST;
        }
        order.setOrderDataLevel(OrderUtils.OrderDataLevel.DETAIL);//从数据库/redis中读取具体的数据内容
        try {
            //加急
            loadUrgentLevels(order);
            page = operationOrderListService.findKefuComplainOrderList(new Page<>(request, response, DEFAULT_PAGE_SIZE), order, true);
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
        Boolean isValide = checkOrderNoAndPhone(order,model,page);
        if(!isValide){
            return VIEW_NAME_REMINDER_LIST;
        }
        try {
            order.setOrderNo(order.getOrderNo().trim());
            order.setUserName(order.getUserName().trim());
            order.setPhone1(order.getPhone1().trim());
            order.setAddress(order.getAddress().trim());
            order.setCreator(order.getCreator().trim());
            order.setRemarks(order.getRemarks().trim());
            page = operationOrderListService.findReminderOrderLit(new Page<OrderSearchModel>(request, response, DEFAULT_PAGE_SIZE), order,true);
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
        String viewForm = "modules/sd/kefuOrderList/service/historyPlanOrderList";
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
        String checkRegion = operationOrderListService.loadAndCheckUserRegions(order,user);
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
        boolean isServiceSupervisor = user.getRoleEnNames().contains("Customer service supervisor");
        if (isServiceSupervisor) {
            order.setCreateBy(user);//*
        } else if (user.isKefu()) {
            order.setCreateBy(user);//*客服按帐号筛选
        } else if (user.isInnerAccount()) { //内部帐号
            order.setCreateBy(user);//*
        }
        page = operationOrderListService.getHistoryPlanListForKefu(page, order);
        model.addAttribute("page", page);
        model.addAttribute("order", order);
        return viewForm;
    }

    /**
     * 待审核退单列表 (for 客服主管)
     * 客服提出退单申请，由客服主管审核
     */
    @RequiresPermissions("sd:order:approvereturn")
    @RequestMapping(value = "orderReturnApproveList")
    public String orderReturnApproveList(OrderSearchModel order, HttpServletRequest request, HttpServletResponse response, Model model) {
        String viewForm = "modules/operation/sd/approveList/orderReturnApproveNewList";
        boolean isShowXYYOrderReturnApproveList = B2BMDUtils.isB2BMicroServiceEnabled(B2BDataSourceEnum.XYINGYAN.id)
                || B2BMDUtils.isB2BMicroServiceEnabled(B2BDataSourceEnum.INSE.id)
                || B2BMDUtils.isB2BMicroServiceEnabled(B2BDataSourceEnum.VIOMI.id)
                || B2BMDUtils.isB2BMicroServiceEnabled(B2BDataSourceEnum.SF.id);
        Page<Order> page = new Page<>();
        User user = UserUtils.getUser();
        order = setSearchModel(user,order,model,1,true,90,false,0);
        if(!order.getValid()){
            model.addAttribute(MODEL_ATTR_PAGE, page);
            model.addAttribute(MODEL_ATTR_ORDER, order);
            model.addAttribute("canSearch", false);
            model.addAttribute("isShowXYYOrderReturnApproveList", isShowXYYOrderReturnApproveList);
            return viewForm;
        }
        Boolean isValide = checkOrderNoAndPhone(order,model,page);
        if(!isValide){
            model.addAttribute("canSearch", false);
            model.addAttribute("isShowXYYOrderReturnApproveList", isShowXYYOrderReturnApproveList);
            return viewForm;
        }
        try {
            //quarters
            Date[] dates = OrderUtils.getQuarterDates(order.getBeginDate(), order.getEndDate(), 0, 0);
            List<String> quarters = QuarterUtils.getQuarters(dates[0], dates[1]);
            if (quarters != null && quarters.size() > 0) {
                order.setQuarters(quarters);
            }
            //排除新迎燕的单
            order.setExclueDataSources(Lists.newArrayList(8,9,19,21));
            page = operationOrderListService.getOrderReturnApproveList(new Page<>(request, response), order);
        } catch (Exception e) {
            addMessage(model, "错误：" + e.getMessage());
        }
        model.addAttribute(MODEL_ATTR_PAGE, page);
        model.addAttribute(MODEL_ATTR_ORDER, order);
        model.addAttribute("canSearch", true);
        model.addAttribute("isShowXYYOrderReturnApproveList", isShowXYYOrderReturnApproveList);
        return viewForm;
    }

    /**
     * 新迎燕待审核退单列表 (for 客服主管)
     * 客服提出退单申请，由新迎燕系统来审核，客服主管仅能查看
     */
    @RequiresPermissions("sd:order:approvereturn")
    @RequestMapping(value = "xyyOrderReturnApproveList")
    public String xyyOrderReturnApproveList(OrderSearchModel order, HttpServletRequest request, HttpServletResponse response, Model model) {
        String viewForm = "modules/operation/sd/approveList/xyyOrderReturnApproveNewList";
        Page<Order> page = new Page<>();
        User user = UserUtils.getUser();
        order = setSearchModel(user,order,model,1,true,90,false,0);
        if(!order.getValid()){
            model.addAttribute(MODEL_ATTR_PAGE, page);
            model.addAttribute(MODEL_ATTR_ORDER, order);
            model.addAttribute("canSearch", false);
            return viewForm;
        }
        Boolean isValide = checkOrderNoAndPhone(order,model,page);
        if(!isValide){
            model.addAttribute("canSearch", false);
            return viewForm;
        }
        try {
            //新迎燕的单
            order.setSearchDataSources(Lists.newArrayList(8,9,19,21));
            page = operationOrderListService.getOrderReturnApproveList(new Page<>(request, response), order);
        } catch (Exception e) {
            addMessage(model, "错误：" + e.getMessage());
        }
        model.addAttribute(MODEL_ATTR_PAGE, page);
        model.addAttribute(MODEL_ATTR_ORDER, order);
        model.addAttribute("canSearch", true);
        return viewForm;
    }

    //endregion 特殊列表

}

