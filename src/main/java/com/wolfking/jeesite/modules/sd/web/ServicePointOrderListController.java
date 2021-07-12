package com.wolfking.jeesite.modules.sd.web;


import com.google.common.base.Splitter;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.exception.OrderException;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.GsonUtils;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.api.controller.sd.RestOrderController;
import com.wolfking.jeesite.modules.api.entity.md.RestLoginUserInfo;
import com.wolfking.jeesite.modules.api.entity.sd.request.RestCloseOrderRequest;
import com.wolfking.jeesite.modules.api.entity.sd.request.RestOrderBaseRequest;
import com.wolfking.jeesite.modules.api.entity.sd.request.RestSetAppointmentDateRequest;
import com.wolfking.jeesite.modules.api.service.sd.RestOrderService;
import com.wolfking.jeesite.modules.api.util.ErrorCode;
import com.wolfking.jeesite.modules.api.util.RestResult;
import com.wolfking.jeesite.modules.api.util.RestResultGenerator;
import com.wolfking.jeesite.modules.api.util.RestSessionUtils;
import com.wolfking.jeesite.modules.md.entity.Engineer;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.md.service.ServicePointService;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.entity.OrderCondition;
import com.wolfking.jeesite.modules.sd.entity.OrderDetail;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderSearchModel;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderServicePointSearchModel;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.modules.sd.service.ServicePointOrderListService;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.cc.entity.OrderReminderVM;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


/**
 * 网点工单列表Controller
 */
@Controller
@RequestMapping(value = "${adminPath}/sd/order/servicePointOrderList/")
@Slf4j
public class ServicePointOrderListController extends BaseController {

    @Autowired
    private ServicePointOrderListService servicePointOrderListService;

    @Autowired
    private ServicePointService servicePointService;

    @Autowired
    private RestOrderService restOrderService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private RedisUtils redisUtils;

    private static final String HTTP_METHOD_POST = "post";

    private static final String MODEL_ATTR_PAGE = "page";
    private static final String MODEL_ATTR_ORDER = "order";

    private static final String VIEW_NAME_NOAPPOINTMENT_LIST = "modules/sd/servicePointOrderList/noAppointmentList";
    private static final String VIEW_NAME_ARRIVEAPPOINTMENT_LIST = "modules/sd/servicePointOrderList/arriveAppointmentList";
    private static final String VIEW_NAME_PASSAPPOINTMENT_LIST = "modules/sd/servicePointOrderList/passAppointmentList";
    private static final String VIEW_NAME_PENDING_LIST = "modules/sd/servicePointOrderList/pendingList";
    private static final String VIEW_NAME_SERVICED_LIST = "modules/sd/servicePointOrderList/servicedList";
    private static final String VIEW_NAME_APP_COMPLETED_LIST = "modules/sd/servicePointOrderList/appCompletedList";
    private static final String VIEW_NAME_UNCOMPLETED_LIST = "modules/sd/servicePointOrderList/uncompletedList";
    private static final String VIEW_NAME_ALL_LIST = "modules/sd/servicePointOrderList/allList";
    private static final String VIEW_NAME_COMPLETED_LIST = "modules/sd/servicePointOrderList/completedList";
    private static final String VIEW_NAME_WAITINGACCESORY_LIST = "modules/sd/servicePointOrderList/waitingAccesoryList";
    private static final String VIEW_NAME_RETURN_LIST = "modules/sd/servicePointOrderList/returnList";
    private static final String VIEW_NAME_REMINDER_LIST = "modules/sd/servicePointOrderList/reminderList";

    //region 客服操作
    private static final String VIEW_NAME_ORDER_PENDINGTYPE_FORM = "modules/sd/servicePointOrderList/orderPendingTypeForm";
    private static final String VIEW_NAME_ORDER_COMPLETE_FORM = "modules/sd/servicePointOrderList/orderAppCompleteForm";

    //region 网点工单列表

    /**
     * 设置必须的查询条件
     */
    private OrderServicePointSearchModel setSearchModel(OrderServicePointSearchModel searchModel) {
        if (searchModel == null) {
            searchModel = new OrderServicePointSearchModel();
        }
        User currentUser = UserUtils.getUser();
        if (currentUser.isEngineer()) {
            Engineer engineer = null;
            if (currentUser.getCompanyId() > 0) {
                engineer = servicePointService.getEngineerFromCache(currentUser.getCompanyId(), currentUser.getEngineerId());
            }
            if (engineer == null) {
                engineer = servicePointService.getEngineer(currentUser.getEngineerId());
            }
            Long servicePointId = (engineer.getServicePoint() == null ? null : engineer.getServicePoint().getId());
            searchModel.setServicePointId(servicePointId);
            if (engineer.getMasterFlag() == 1) {
                searchModel.setEngineerId(null);
                if(servicePointId != null) {
                    List<Engineer> engineers = servicePointService.getEngineerListOfServicePoint(servicePointId);
                    searchModel.setEngineerList(engineers);
                }
            } else {
                searchModel.setEngineerId(currentUser.getEngineerId());
            }
        }

        Date now = new Date();
        if(StringUtils.isBlank(searchModel.getAcceptDateRange())){
            searchModel.setBeginAcceptDate(DateUtils.addMonth(now, -1));
            searchModel.setEndAcceptDate(now);
        }else{
            List<String> dates = Splitter.onPattern("~") //[~|-]
                    .omitEmptyStrings()
                    .trimResults()
                    .splitToList(searchModel.getAcceptDateRange());
            if(dates.isEmpty()){
                searchModel.setBeginAcceptDate(DateUtils.addMonth(now, -1));
                searchModel.setEndAcceptDate(now);
            }else{
                searchModel.setBeginAcceptDate(DateUtils.parseDate(dates.get(0)));
                if(dates.size()>1){
                    searchModel.setEndAcceptDate(DateUtils.parseDate(dates.get(1)));
                }else{
                    searchModel.setEndAcceptDate(now);
                }
            }
        }
        searchModel.setBeginAcceptDate(DateUtils.getStartOfDay(searchModel.getBeginAcceptDate()));
        searchModel.setEndAcceptDate(DateUtils.getEndOfDay(searchModel.getEndAcceptDate()));
        /*
        if (searchModel.getBeginAcceptDate() == null) {
            searchModel.setBeginAcceptDate(DateUtils.addMonth(now, -1));
        }

        */
        return searchModel;
    }

    /**
     * 检查订单号，手机号输入
     * @param searchModel
     * @param model
     * @return
     */
    private Boolean checkOrderNoAndPhone(OrderServicePointSearchModel searchModel,Model model,Page<Order> page){
        if(searchModel == null){
            return true;
        }
        //检查电话
        if (StringUtils.isNotBlank(searchModel.getOrderNo())){
            int orderSerchType = searchModel.getOrderNoSearchType();
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
                                addMessage(model, "错误：请输入正确的订单号码,日期超出范围");
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
        if (StringUtils.isNotBlank(searchModel.getUserPhone())){
            if(searchModel.getIsPhone() != 1){
                addMessage(model, "错误：请输入正确的电话");
                model.addAttribute(MODEL_ATTR_PAGE, page);
                model.addAttribute(MODEL_ATTR_ORDER, searchModel);
                return false;
            }
        }
        return true;
    }

    /**
     * 催单订单列表
     */
    @RequiresPermissions("sd:order:engineeraccept")
    @RequestMapping(value = "reminderList")
    public String reminderList(OrderServicePointSearchModel order, Model model,
                                    HttpServletRequest request, HttpServletResponse response) {
        Page page = new Page<>(request, response);
        order = setSearchModel(order);
        Boolean isValide = checkOrderNoAndPhone(order,model,page);
        if(!isValide){
            return VIEW_NAME_REMINDER_LIST;
        }
        try {
            page = servicePointOrderListService.findReminderOrderList(new Page<>(request, response), order);
        } catch (Exception e) {
            addMessage(model, "错误：" + e.getMessage());
        }
        model.addAttribute(MODEL_ATTR_PAGE, page);
        model.addAttribute(MODEL_ATTR_ORDER, order);
        return VIEW_NAME_REMINDER_LIST;
    }

    /**
     * 网点的等待预约的订单列表
     */
    @RequiresPermissions("sd:order:engineeraccept")
    @RequestMapping(value = "noAppointmentList")
    public String noAppointmentList(OrderServicePointSearchModel order, Model model,
                                    HttpServletRequest request, HttpServletResponse response) {
        Page<Order> page = new Page<>(request, response);
        order = setSearchModel(order);
        Boolean isValide = checkOrderNoAndPhone(order,model,page);
        if(!isValide){
            return VIEW_NAME_NOAPPOINTMENT_LIST;
        }
        try {
            page = servicePointOrderListService.findServicePointNoAppointmentOrderList(new Page<>(request, response), order);
        } catch (Exception e) {
            addMessage(model, "错误：" + e.getMessage());
        }
        model.addAttribute(MODEL_ATTR_PAGE, page);
        model.addAttribute(MODEL_ATTR_ORDER, order);
        return VIEW_NAME_NOAPPOINTMENT_LIST;
    }


    /**
     * 网点的预约到期订单列表
     */
    @RequiresPermissions("sd:order:engineeraccept")
    @RequestMapping(value = "arriveAppointmentList")
    public String arriveAppointmentList(OrderServicePointSearchModel order, Model model,
                                        HttpServletRequest request, HttpServletResponse response) {
        Page<Order> page = new Page<>(request, response);
        order = setSearchModel(order);
        Boolean isValide = checkOrderNoAndPhone(order,model,page);
        if(!isValide){
            return VIEW_NAME_ARRIVEAPPOINTMENT_LIST;
        }
        try {
            page = servicePointOrderListService.findServicePointArriveAppointmentOrderList(new Page<>(request, response), order);
        } catch (Exception e) {
            addMessage(model, "错误：" + e.getMessage());
        }
        model.addAttribute(MODEL_ATTR_PAGE, page);
        model.addAttribute(MODEL_ATTR_ORDER, order);
        return VIEW_NAME_ARRIVEAPPOINTMENT_LIST;
    }

    /**
     * 网点的预约超期订单列表
     */
    @RequiresPermissions("sd:order:engineeraccept")
    @RequestMapping(value = "passAppointmentList")
    public String passAppointmentList(OrderServicePointSearchModel order, Model model,
                                      HttpServletRequest request, HttpServletResponse response) {
        Page<Order> page = new Page<>(request, response);
        order = setSearchModel(order);
        Boolean isValide = checkOrderNoAndPhone(order,model,page);
        if(!isValide){
            return VIEW_NAME_PASSAPPOINTMENT_LIST;
        }
        try {
            page = servicePointOrderListService.findServicePointPassAppointmentOrderList(new Page<>(request, response), order);
        } catch (Exception e) {
            addMessage(model, "错误：" + e.getMessage());
        }
        model.addAttribute(MODEL_ATTR_PAGE, page);
        model.addAttribute(MODEL_ATTR_ORDER, order);

        return VIEW_NAME_PASSAPPOINTMENT_LIST;
    }

    /**
     * 网点的停滞订单列表
     */
    @RequiresPermissions("sd:order:engineeraccept")
    @RequestMapping(value = "pendingList")
    public String pendingList(OrderServicePointSearchModel order, Model model,
                              HttpServletRequest request, HttpServletResponse response) {
        Page<Order> page = new Page<>(request, response);
        order = setSearchModel(order);
        Boolean isValide = checkOrderNoAndPhone(order,model,page);
        if(!isValide){
            return VIEW_NAME_PENDING_LIST;
        }
        try {
            page = servicePointOrderListService.findServicePointPendingOrderList(new Page<>(request, response), order);
        } catch (Exception e) {
            addMessage(model, "错误：" + e.getMessage());
        }
        model.addAttribute(MODEL_ATTR_PAGE, page);
        model.addAttribute(MODEL_ATTR_ORDER, order);

        return VIEW_NAME_PENDING_LIST;
    }


    /**
     * 网点的待完成订单列表
     */
    @RequiresPermissions("sd:order:engineeraccept")
    @RequestMapping(value = "servicedList")
    public String servicedList(OrderServicePointSearchModel order, Model model,
                               HttpServletRequest request, HttpServletResponse response) {
        Page<Order> page = new Page<>(request, response);
        order = setSearchModel(order);
        Boolean isValide = checkOrderNoAndPhone(order,model,page);
        if(!isValide){
            return VIEW_NAME_SERVICED_LIST;
        }
        try {
            page = servicePointOrderListService.findServicePointServicedOrderList(new Page<>(request, response), order);
        } catch (Exception e) {
            addMessage(model, "错误：" + e.getMessage());
        }
        model.addAttribute(MODEL_ATTR_PAGE, page);
        model.addAttribute(MODEL_ATTR_ORDER, order);
        return VIEW_NAME_SERVICED_LIST;
    }

    /**
     * 网点的待回访订单列表
     */
    @RequiresPermissions("sd:order:engineeraccept")
    @RequestMapping(value = "appCompletedList")
    public String appCompletedList(OrderServicePointSearchModel order, Model model,
                               HttpServletRequest request, HttpServletResponse response) {
        Page<Order> page = new Page<>(request, response);
        order = setSearchModel(order);
        Boolean isValide = checkOrderNoAndPhone(order,model,page);
        if(!isValide){
            return VIEW_NAME_APP_COMPLETED_LIST;
        }
        try {
            page = servicePointOrderListService.findServicePointAppCompletedOrderList(new Page<>(request, response), order);
        } catch (Exception e) {
            addMessage(model, "错误：" + e.getMessage());
        }
        model.addAttribute(MODEL_ATTR_PAGE, page);
        model.addAttribute(MODEL_ATTR_ORDER, order);
        return VIEW_NAME_APP_COMPLETED_LIST;
    }

    /**
     * 网点的未完成订单列表
     */
    @RequiresPermissions("sd:order:engineeraccept")
    @RequestMapping(value = "uncompletedList")
    public String uncompletedList(OrderServicePointSearchModel order, Model model,
                                  HttpServletRequest request, HttpServletResponse response) {
        Page<Order> page = new Page<>(request, response);
        order = setSearchModel(order);
        Boolean isValide = checkOrderNoAndPhone(order,model,page);
        if(!isValide){
            return VIEW_NAME_UNCOMPLETED_LIST;
        }
        try {
            page = servicePointOrderListService.findServicePointUncompletedOrderList(new Page<>(request, response), order);
        } catch (Exception e) {
            addMessage(model, "错误：" + e.getMessage());
        }
        model.addAttribute(MODEL_ATTR_PAGE, page);
        model.addAttribute(MODEL_ATTR_ORDER, order);
        return VIEW_NAME_UNCOMPLETED_LIST;
    }

    /**
     * 网点所有订单列表
     */
    @RequiresPermissions("sd:order:engineeraccept")
    @RequestMapping(value = "allList")
    public String allList(OrderServicePointSearchModel order, Model model,
                          HttpServletRequest request, HttpServletResponse response) {
        Page<Order> page = new Page<>(request, response);
        Date now = new Date();
        if (request.getMethod().equalsIgnoreCase(HTTP_METHOD_POST)) {
            order = setSearchModel(order);
            Boolean isValide = checkOrderNoAndPhone(order,model,page);
            if(!isValide){
                return VIEW_NAME_ALL_LIST;
            }
            try {
                page = servicePointOrderListService.findServicePointAllOrderList(new Page<>(request, response), order);
            } catch (Exception e) {
                addMessage(model, "错误：" + e.getMessage());
            }
        } else {
            order = setSearchModel(order);
            //order.setEndAcceptDate(now);
            //order.setBeginAcceptDate(DateUtils.addMonth(now, -1));
        }
        model.addAttribute(MODEL_ATTR_PAGE, page);
        model.addAttribute(MODEL_ATTR_ORDER, order);
        return VIEW_NAME_ALL_LIST;
    }

    /**
     * 我的订单之完成列表
     */
    @RequiresPermissions("sd:order:engineeraccept")
    @RequestMapping(value = "completedList")
    public String completedList(OrderServicePointSearchModel order,
                                HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<Order> page = new Page<>();
        Date now = new Date();
        if (request.getMethod().equalsIgnoreCase(HTTP_METHOD_POST)) {
            order = setSearchModel(order);
            Boolean isValide = checkOrderNoAndPhone(order,model,page);
            if(!isValide){
                return VIEW_NAME_COMPLETED_LIST;
            }
            //完成日期
            if(!StringUtils.isBlank(order.getCompleteDateRange())){
                List<String> dates = Splitter.onPattern("~") //[~|-]
                        .omitEmptyStrings()
                        .trimResults()
                        .splitToList(order.getCompleteDateRange());
                if(!dates.isEmpty()){
                    if(StringUtils.isNotBlank(dates.get(0))) {
                        order.setBeginCompleteDate(DateUtils.parseDate(dates.get(0)));
                    }
                    if(dates.size()>1){
                        if(StringUtils.isNotBlank(dates.get(1))) {
                            order.setEndCompleteDate(DateUtils.getEndOfDay(DateUtils.parseDate(dates.get(1))));
                        }
                    }
                }
            }
            //if (order.getEndCompleteDate() != null) {
            //    order.setEndCompleteDate(DateUtils.getDateEnd(order.getEndCompleteDate()));
            //}
            try {
                page = servicePointOrderListService.findServicePointCompletedOrderList(new Page<>(request, response), order);
            } catch (Exception e) {
                addMessage(model, "错误：" + e.getMessage());
            }
        } else {
            order = setSearchModel(order);
            //order.setEndAcceptDate(new Date());
            //order.setBeginAcceptDate(DateUtils.addMonth(now, -1));
        }
        model.addAttribute("page", page);
        model.addAttribute("order", order);
        return VIEW_NAME_COMPLETED_LIST;
    }

    /**
     * 网点的等配件列表
     *
     * @param order
     * @param request
     * @param response
     * @param model
     * @return
     */
    @RequiresPermissions("sd:order:engineeraccept")
    @RequestMapping(value = {"waitingAccessoryList"})
    public String waitingAccessoryOrderList(OrderServicePointSearchModel order, Model model,
                                                        HttpServletRequest request, HttpServletResponse response) {

        Page<Order> page = new Page<>(request, response);
        order = setSearchModel(order);
        Boolean isValide = checkOrderNoAndPhone(order,model,page);
        if(!isValide){
            return VIEW_NAME_WAITINGACCESORY_LIST;
        }
        Date appointDate = DateUtils.getEndOfDay(new Date());
        order.setAppointmentDate(appointDate);
        try {
            page = servicePointOrderListService.findServicePointWaitingAccessoryList(new Page<>(request, response), order);
        } catch (Exception e) {
            addMessage(model, "错误：" + e.getMessage());
        }
        model.addAttribute(MODEL_ATTR_PAGE, page);
        model.addAttribute(MODEL_ATTR_ORDER, order);
        return VIEW_NAME_WAITINGACCESORY_LIST;
    }

    /**
     * 网点退单列表
     */
    @RequiresPermissions("sd:order:engineeraccept")
    @RequestMapping(value = "returnlist")
    public String returnList(OrderServicePointSearchModel order, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<Order> page = new Page<>(request, response);
        order = setSearchModel(order);
        Boolean isValide = checkOrderNoAndPhone(order,model,page);
        if(!isValide){
            return VIEW_NAME_RETURN_LIST;
        }
        if(request.getMethod().equalsIgnoreCase("post")) {
            //退单日期
            if(!StringUtils.isBlank(order.getCompleteDateRange())){
                List<String> dates = Splitter.onPattern("~") //[~|-]
                        .omitEmptyStrings()
                        .trimResults()
                        .splitToList(order.getCompleteDateRange());
                if(!dates.isEmpty()){
                    if(StringUtils.isNotBlank(dates.get(0))) {
                        order.setBeginCompleteDate(DateUtils.parseDate(dates.get(0)));
                    }
                    if(dates.size()>1){
                        if(StringUtils.isNotBlank(dates.get(1))) {
                            order.setEndCompleteDate(DateUtils.getEndOfDay(DateUtils.parseDate(dates.get(1))));
                        }
                    }
                }
            }
            try {
                page = servicePointOrderListService.findServicePointReturnedList(new Page<>(request, response), order);
            } catch (Exception e) {
                addMessage(model, "错误：" + e.getMessage());
            }
        }
        model.addAttribute(MODEL_ATTR_PAGE, page);
        model.addAttribute(MODEL_ATTR_ORDER, order);
        return VIEW_NAME_RETURN_LIST;
    }



    //endregion 网点工单列表


    //region 操作

    /**
     * 设定订单停滞原因 form
     */
    @RequiresPermissions(value = "sd:order:engineeraccept")
    @RequestMapping(value = "service/pending", method = RequestMethod.GET)
    public String pending(String orderId, String quarter, HttpServletRequest request, Model model) {
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

        // 时间取整点时间
        Date date = DateUtils.addDays(new Date(), 1);
        String time = DateUtils.formatDate(date, "yyyy-MM-dd 12:00:00");
        Date appointmentDate = null;
        try {
            appointmentDate = DateUtils.parse(time, "yyyy-MM-dd HH:00:00");
        } catch (java.text.ParseException e) {
            log.error("[OrderController.pending] invalid datetime:{}", time, e);
        }
        // 时间取整点时间
        order.getOrderCondition().setAppointmentDate(appointmentDate);
        model.addAttribute("canSave", true);
        model.addAttribute(MODEL_ATTR_ORDER, condition);
        return VIEW_NAME_ORDER_PENDINGTYPE_FORM;
    }

    /**
     * ajax提交停滞原因
     */
    @RequiresPermissions(value = "sd:order:engineeraccept")
    @ResponseBody
    @RequestMapping(value = "service/pending", method = RequestMethod.POST)
    public AjaxJsonEntity pending(OrderCondition order,HttpServletRequest request, HttpServletResponse response)
    {
        try {
            User user = UserUtils.getUser();
            if(user == null){
                return AjaxJsonEntity.fail("登录超时，请刷新页面并重新登录！",null);
            }
            RestSetAppointmentDateRequest orderRequest = new RestSetAppointmentDateRequest();
            orderRequest.setOrderId(order.getOrderId().toString());
            orderRequest.setQuarter(order.getQuarter());
            orderRequest.setOrderNo(order.getOrderNo());
            orderRequest.setAppointmentDate(order.getAppointmentDate() == null ? null : order.getAppointmentDate().getTime());
            orderRequest.setPendingType(order.getPendingType() == null ? null : order.getPendingType().getIntValue());
            orderRequest.setRemarks(order.getRemarks());
            RestResult<Object> result = restOrderService.saveAppAppointmentDate(request, user, orderRequest);
            if(result.getCode() == ErrorCode.NO_ERROR.code) {
                return AjaxJsonEntity.success("设置成功", null);
            }else{
                return AjaxJsonEntity.fail(result.getMsg(),null);
            }
        } catch (Exception e){
            if(order != null && order.getOrderId() != null) {
                log.error("[OrderController.pending] orderId:{}",order.getOrderId(), e);
            }else{
                log.error("[OrderController.pending]", e);
            }
            return AjaxJsonEntity.fail(e.getMessage(),null);
        }
    }


    /**
     * 完成订单form
     */
    @RequiresPermissions(value = "sd:order:engineeraccept")
    @RequestMapping(value = "service/complete", method = RequestMethod.GET)
    public String complete(String orderId,String quarter, HttpServletRequest request, Model model) {
        Order order = new Order();
        OrderCondition condition = new OrderCondition();
        Long orderIdLong = StringUtils.toLong(orderId);
        if (orderIdLong <= 0) {
            addMessage(model, "错误：订单参数无效");
            model.addAttribute("canSave", false);
            model.addAttribute(MODEL_ATTR_ORDER, condition);
            return VIEW_NAME_ORDER_COMPLETE_FORM;
        } else {
            order = orderService.getOrderById(orderIdLong, quarter, OrderUtils.OrderDataLevel.DETAIL, true);
            if (order == null || order.getOrderCondition() == null) {
                addMessage(model, "错误：读取订单失败，请重试。");
                model.addAttribute("canSave", false);
                model.addAttribute(MODEL_ATTR_ORDER, condition);
                return VIEW_NAME_ORDER_COMPLETE_FORM;
            }
            OrderCondition orderCondition = order.getOrderCondition();
            if(orderCondition.getServicePoint() == null || orderCondition.getEngineer() == null){
                addMessage(model, "错误：读取网点或师傅失败，请重试。");
                model.addAttribute("canSave", false);
                model.addAttribute(MODEL_ATTR_ORDER, condition);
                return VIEW_NAME_ORDER_COMPLETE_FORM;
            }
            Engineer engineer = servicePointService.getEngineerFromCache(orderCondition.getServicePoint().getId(), orderCondition.getEngineer().getId());
            if (null == engineer) {
                addMessage(model, "错误：读取师傅失败，请重试。");
                model.addAttribute("canSave", false);
                model.addAttribute(MODEL_ATTR_ORDER, condition);
                return VIEW_NAME_ORDER_COMPLETE_FORM;
            }
            final Long engineerId = engineer.getId();
            List<OrderDetail> details = order.getDetailList();
            if (details == null || details.size() == 0) {
                addMessage(model, "错误：订单无上门服务项目");
                model.addAttribute("canSave", false);
                model.addAttribute(MODEL_ATTR_ORDER, condition);
                return VIEW_NAME_ORDER_COMPLETE_FORM;
            }
            if (details != null && details.size() > 0) {
                OrderDetail detail = details.stream().filter(t -> t.getEngineer().getId().equals(engineerId))
                        .findFirst()
                        .orElse(null);
                if (detail == null) {
                    addMessage(model, "错误：订单无上门服务项目");
                    model.addAttribute("canSave", false);
                    model.addAttribute(MODEL_ATTR_ORDER, condition);
                    return VIEW_NAME_ORDER_COMPLETE_FORM;
                }
            }
        }

        String lockkey = String.format(RedisConstant.SD_ORDER_LOCK, orderId);
        if (redisUtils.exists(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey)) {
            addMessage(model, "错误：此订单正在处理中，请稍候重试，或刷新页面。");
            model.addAttribute("canSave", false);
            model.addAttribute(MODEL_ATTR_ORDER, condition);
            return VIEW_NAME_ORDER_COMPLETE_FORM;
        }

        condition = order.getOrderCondition();
        condition.setOrderId(order.getId());
        condition.setRemarks("");
        //List<Dict> completeTypes = MSDictUtils.getDictList("completed_type");
        model.addAttribute("canSave", true);
        model.addAttribute(MODEL_ATTR_ORDER, condition);
        return VIEW_NAME_ORDER_COMPLETE_FORM;
    }


    /**
     * ajax提交完成订单
     */
    @RequiresPermissions(value = "sd:order:engineeraccept")
    @ResponseBody
    @RequestMapping(value = "service/complete", method = RequestMethod.POST)
    public AjaxJsonEntity complete(OrderCondition order,HttpServletRequest request, HttpServletResponse response)
    {
        try {
            User user = UserUtils.getUser();
            if(user == null){
                return AjaxJsonEntity.fail("登录超时，请刷新页面并重新登录！",null);
            }
            RestCloseOrderRequest orderRequest = new RestCloseOrderRequest();
            orderRequest.setOrderId(order.getOrderId().toString());
            orderRequest.setQuarter(order.getQuarter());
            orderRequest.setOrderNo(order.getOrderNo());
            orderRequest.setCompleteType(order.getPendingType() == null ? "" : order.getPendingType().getValue());
            orderRequest.setRemarks(order.getRemarks());
            RestResult<Object> result = restOrderService.saveAppComplete(request, user, orderRequest);
            if(result.getCode() == ErrorCode.NO_ERROR.code) {
                return AjaxJsonEntity.success("订单已标记完成", null);
            }else{
                return AjaxJsonEntity.fail(result.getMsg(),null);
            }
        } catch (Exception e){
            if(order != null && order.getOrderId() != null) {
                log.error("[complete] orderId:{}",order.getOrderId(), e);
            }else{
                log.error("complete]", e);
            }
            return AjaxJsonEntity.fail(e.getMessage(),null);
        }
    }


    //endregion 操作


}

