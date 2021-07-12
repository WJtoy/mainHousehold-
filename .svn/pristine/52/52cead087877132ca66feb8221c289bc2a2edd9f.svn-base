package com.wolfking.jeesite.modules.servicepoint.sd.web;


import com.google.common.collect.Lists;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.exception.OrderException;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.api.util.ErrorCode;
import com.wolfking.jeesite.modules.md.entity.Engineer;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.sd.entity.*;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.servicepoint.ms.md.SpServicePointService;
import com.wolfking.jeesite.modules.servicepoint.ms.sd.SpOrderCacheReadService;
import com.wolfking.jeesite.modules.servicepoint.ms.sys.SpDictService;
import com.wolfking.jeesite.modules.servicepoint.ms.utils.SpRedisUtils;
import com.wolfking.jeesite.modules.servicepoint.sd.service.ServicePointOrderOperationService;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;


/**
 * 网点工单操作
 */
@Controller
@RequestMapping(value = "${adminPath}/servicePoint/sd/orderOperation/")
@Slf4j
public class ServicePointOrderOperationController extends BaseController {

    @Autowired
    private SpRedisUtils redisUtils;

    @Autowired
    private SpDictService dictService;

    @Autowired
    private SpServicePointService servicePointService;

    @Autowired
    private SpOrderCacheReadService orderCacheReadService;

    @Autowired
    private ServicePointOrderOperationService orderOperationService;


    private static final String VIEW_NAME_SERVICEPOINT_PLAN_FORM = "modules/servicePoint/sd/orderOperation/orderServicePointPlanForm";
    private static final String VIEW_NAME_SERVICEPOINT_PENDING_FORM = "modules/servicePoint/sd/orderOperation/orderServicePointPendingForm";
    private static final String VIEW_NAME_SERVICEPOINT_COMPLETE_FORM = "modules/servicePoint/sd/orderOperation/orderServicePointCompleteForm";
    private static final String VIEW_NAME_SERVICEPOINT_TRACKING_FORM = "modules/servicePoint/sd/orderOperation/orderServicePointTrackingForm";

    private static final String MODEL_ATTR_ORDER = "order";

    /**
     * 派单 form (网点)
     */
    @RequiresPermissions(value = {"sd:order:engineeraccept"}, logical = Logical.OR)
    @RequestMapping(value = "servicePointPlan", method = RequestMethod.GET)
    public String servicePointPlan(String orderId, String quarter, HttpServletRequest request, Model model) {
        Order order = new Order();
        Long lorderId = Long.valueOf(orderId);
        if (lorderId == null || lorderId <= 0) {
            addMessage(model, "派单时发生错误：订单号丢失");
            model.addAttribute("canSave", false);
            model.addAttribute("order", order);
            return VIEW_NAME_SERVICEPOINT_PLAN_FORM;
        }

        order = orderCacheReadService.getOrderById(lorderId, quarter, OrderUtils.OrderDataLevel.FEE, true);
        if (order == null || order.getOrderCondition() == null) {
            addMessage(model, "错误：系统繁忙，读取订单失败，请重试。");
            model.addAttribute("canSave", false);
            model.addAttribute("order", new Order());
            return VIEW_NAME_SERVICEPOINT_PLAN_FORM;
        }
        // 检查是否可以取消
        if (!order.canPlanOrder()) {
            addMessage(model, String.format("操作失败：订单：%s 无法派单，当前订单状态:", order.getOrderNo(), order.getOrderCondition().getStatus().getLabel()));
            model.addAttribute("canSave", false);
            model.addAttribute("order", order);
            return VIEW_NAME_SERVICEPOINT_PLAN_FORM;
        }

        order.setRemarks("");
        model.addAttribute("canSave", true);
        model.addAttribute("order", order);
        return VIEW_NAME_SERVICEPOINT_PLAN_FORM;
    }

    /**
     * ajax提交网点派单信息
     */
    @RequiresPermissions("sd:order:engineeraccept")
    @ResponseBody
    @RequestMapping(value = "servicePointPlan", method = RequestMethod.POST)
    public AjaxJsonEntity servicePointPlan(Order order, HttpServletRequest request, HttpServletResponse response) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        if (order == null || order.getId() == null) {
            result.setSuccess(false);
            result.setMessage("派单时发生错误：订单号丢失");
            return result;
        }
        OrderCondition condition = order.getOrderCondition();
        if (condition == null || condition.getEngineer() == null || condition.getEngineer().getId() == null) {
            result.setSuccess(false);
            result.setMessage("未指派安维人员");
            return result;
        }

        try {
            User user = UserUtils.getUser();
            order.setCreateBy(user);
            orderOperationService.servicePointPlanOrder(order);
            result.setSuccess(true);
            result.setMessage("派单成功");
        } catch (OrderException oe) {
            result.setSuccess(false);
            result.setMessage(oe.getMessage());
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage(e.getMessage());
            log.error("[OrderController.servicePointPlan] orderId:{}", order.getId(), e);
        }
        return result;
    }

    /**
     * 设定订单停滞原因 form
     */
    @RequiresPermissions(value = "sd:order:engineeraccept")
    @RequestMapping(value = "servicePointPending", method = RequestMethod.GET)
    public String servicePointPending(String orderId, String quarter, HttpServletRequest request, Model model) {
        Order order = new Order();
        OrderCondition condition = new OrderCondition();
        Long orderIdLong = StringUtils.toLong(orderId);
        if (orderIdLong <= 0) {
            addMessage(model, "错误：订单参数无效");
            model.addAttribute("canSave", false);
            model.addAttribute(MODEL_ATTR_ORDER, condition);
            return VIEW_NAME_SERVICEPOINT_PENDING_FORM;
        } else {
            order = orderCacheReadService.getOrderById(orderIdLong, quarter, OrderUtils.OrderDataLevel.CONDITION, true);
            if (order == null || order.getOrderCondition() == null) {
                addMessage(model, "错误：不允许改约（预约时间17点前（含），需要在当天23点前改约；预约时间17点以后的，需要在第二天23点前完成改约）。");
                model.addAttribute("canSave", false);
                model.addAttribute(MODEL_ATTR_ORDER, condition);
                return VIEW_NAME_SERVICEPOINT_PENDING_FORM;
            }
            if (order.isAllowedForSetAppointment() == 0) {
                addMessage(model, "错误：系统繁忙，读取订单失败，请重试。");
                model.addAttribute("canSave", false);
                model.addAttribute(MODEL_ATTR_ORDER, condition);
                return VIEW_NAME_SERVICEPOINT_PENDING_FORM;
            }
            if (!order.canPendingType()) {
                addMessage(model, String.format("错误：此订单不允许设置停滞原因，当前订单状态:%s", order.getOrderCondition().getStatus().getLabel()));
                model.addAttribute("canSave", false);
                model.addAttribute(MODEL_ATTR_ORDER, condition);
                return VIEW_NAME_SERVICEPOINT_PENDING_FORM;
            }
        }
        String lockkey = String.format(RedisConstant.SD_ORDER_LOCK, orderId);
        if (redisUtils.exists(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey)) {
            addMessage(model, "错误：此订单正在处理中，请稍候重试，或刷新页面。");
            model.addAttribute("canSave", false);
            model.addAttribute(MODEL_ATTR_ORDER, condition);
            return VIEW_NAME_SERVICEPOINT_PENDING_FORM;
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
        return VIEW_NAME_SERVICEPOINT_PENDING_FORM;
    }

    /**
     * ajax提交停滞原因
     */
    @RequiresPermissions(value = "sd:order:engineeraccept")
    @ResponseBody
    @RequestMapping(value = "servicePointPending", method = RequestMethod.POST)
    public AjaxJsonEntity servicePointPending(OrderCondition order, HttpServletRequest request, HttpServletResponse response) {
        try {
            User user = UserUtils.getUser();
            if (user == null) {
                return AjaxJsonEntity.fail("登录超时，请刷新页面并重新登录！", null);
            }
            if (order.getOrderId() == null || order.getOrderId() == 0) {
                return AjaxJsonEntity.fail("错误：订单参数无效", null);
            }
            if (order.getAppointmentDate() == null) {
                return AjaxJsonEntity.fail("错误：预约日期未设定", null);
            }
            int pendingTypeValue = order.getPendingType() == null ? 0 : StringUtils.toInteger(order.getPendingType().getValue());
            if (pendingTypeValue == 0) {
                return AjaxJsonEntity.fail("错误：请选择停滞原因", null);
            }
            Dict pendingType = MSDictUtils.getDictByValue(Integer.toString(pendingTypeValue), Dict.DICT_TYPE_PENDING_TYPE);
            if (pendingType == null || pendingType.getIntValue() == null) {
                return AjaxJsonEntity.fail(ErrorCode.NOT_FOUND_PENDINGTYPE.message, null);
            }
            orderOperationService.servicePointPendingOrder(order.getOrderId(), order.getQuarter(), pendingType, order.getAppointmentDate(), StringUtils.toString(order.getRemarks()), user);
            return AjaxJsonEntity.success("设置成功", null);
        } catch (Exception e) {
            if (order != null && order.getOrderId() != null) {
                log.error("[OrderController.pending] orderId:{}", order.getOrderId(), e);
            } else {
                log.error("[OrderController.pending]", e);
            }
            return AjaxJsonEntity.fail(e.getMessage(), null);
        }
    }


    /**
     * 确认上门(ajax)
     */
    @ResponseBody
    @RequestMapping(value = "servicePointConfirmDoorAuto", method = RequestMethod.POST)
    public AjaxJsonEntity servicePointConfirmDoorAuto(String orderId, String quarter, HttpServletResponse response) {
        User user = UserUtils.getUser();
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        if (user == null) {
            return AjaxJsonEntity.fail("登录超时，请刷新页面并重新登录！", null);
        }
        Long lorderId = StringUtils.toLong(orderId);
        if (lorderId == 0) {
            return AjaxJsonEntity.fail("错误：订单参数无效", null);
        }
        try {
            orderOperationService.confirmDoorAuto(lorderId, quarter, user);
        } catch (OrderException oe) {
            result.setSuccess(false);
            result.setMessage(oe.getMessage());
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage(ExceptionUtils.getRootCauseMessage(e));
            result.setData(ExceptionUtils.getRootCauseStackTrace(e));
            log.error("[OrderController.confirmDoorAuto] orderId:{}", orderId, e);
        }
        return result;
    }

    /**
     * 完成订单form
     */
    @RequiresPermissions(value = "sd:order:engineeraccept")
    @RequestMapping(value = "servicePointComplete", method = RequestMethod.GET)
    public String servicePointComplete(String orderId, String quarter, HttpServletRequest request, Model model) {
        Order order;
        OrderCondition condition = new OrderCondition();
        Long orderIdLong = StringUtils.toLong(orderId);
        if (orderIdLong <= 0) {
            addMessage(model, "错误：订单参数无效");
            model.addAttribute("canSave", false);
            model.addAttribute(MODEL_ATTR_ORDER, condition);
            return VIEW_NAME_SERVICEPOINT_COMPLETE_FORM;
        } else {
            order = orderCacheReadService.getOrderById(orderIdLong, quarter, OrderUtils.OrderDataLevel.DETAIL, true);
            if (order == null || order.getOrderCondition() == null) {
                addMessage(model, "错误：读取订单失败，请重试。");
                model.addAttribute("canSave", false);
                model.addAttribute(MODEL_ATTR_ORDER, condition);
                return VIEW_NAME_SERVICEPOINT_COMPLETE_FORM;
            }
            OrderCondition orderCondition = order.getOrderCondition();
            if (orderCondition.getServicePoint() == null || orderCondition.getEngineer() == null) {
                addMessage(model, "错误：读取网点或师傅失败，请重试。");
                model.addAttribute("canSave", false);
                model.addAttribute(MODEL_ATTR_ORDER, condition);
                return VIEW_NAME_SERVICEPOINT_COMPLETE_FORM;
            }
            Engineer engineer = servicePointService.getEngineerFromCache(orderCondition.getServicePoint().getId(), orderCondition.getEngineer().getId());
            if (null == engineer) {
                addMessage(model, "错误：读取师傅失败，请重试。");
                model.addAttribute("canSave", false);
                model.addAttribute(MODEL_ATTR_ORDER, condition);
                return VIEW_NAME_SERVICEPOINT_COMPLETE_FORM;
            }
            final Long engineerId = engineer.getId();
            List<OrderDetail> details = order.getDetailList();
            if (details == null || details.size() == 0) {
                addMessage(model, "错误：订单无上门服务项目");
                model.addAttribute("canSave", false);
                model.addAttribute(MODEL_ATTR_ORDER, condition);
                return VIEW_NAME_SERVICEPOINT_COMPLETE_FORM;
            }
            OrderDetail detail = details.stream().filter(t -> t.getEngineer().getId().equals(engineerId)).findFirst().orElse(null);
            if (detail == null) {
                addMessage(model, "错误：订单无上门服务项目");
                model.addAttribute("canSave", false);
                model.addAttribute(MODEL_ATTR_ORDER, condition);
                return VIEW_NAME_SERVICEPOINT_COMPLETE_FORM;
            }
        }

        String lockkey = String.format(RedisConstant.SD_ORDER_LOCK, orderId);
        if (redisUtils.exists(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey)) {
            addMessage(model, "错误：此订单正在处理中，请稍候重试，或刷新页面。");
            model.addAttribute("canSave", false);
            model.addAttribute(MODEL_ATTR_ORDER, condition);
            return VIEW_NAME_SERVICEPOINT_COMPLETE_FORM;
        }

        condition = order.getOrderCondition();
        condition.setOrderId(order.getId());
        condition.setRemarks("");
        model.addAttribute("canSave", true);
        model.addAttribute(MODEL_ATTR_ORDER, condition);
        return VIEW_NAME_SERVICEPOINT_COMPLETE_FORM;
    }


    /**
     * ajax提交完成订单
     */
    @RequiresPermissions(value = "sd:order:engineeraccept")
    @ResponseBody
    @RequestMapping(value = "servicePointComplete", method = RequestMethod.POST)
    public AjaxJsonEntity servicePointComplete(OrderCondition order, HttpServletRequest request, HttpServletResponse response) {
        try {
            User user = UserUtils.getUser();
            if (user == null) {
                return AjaxJsonEntity.fail("登录超时，请刷新页面并重新登录！", null);
            }
            if (order.getOrderId() == null || order.getOrderId() == 0) {
                return AjaxJsonEntity.fail("错误：订单参数无效", null);
            }
            String servicePointCompleteType = (order.getPendingType() == null ? "" : order.getPendingType().getValue());
            if (StringUtils.isBlank(servicePointCompleteType)) {
                return AjaxJsonEntity.fail("错误：无效的完成类型", null);
            }
            if (StringUtils.isNoneBlank(order.getRemarks()) && order.getRemarks().trim().length() > 200) {
                return AjaxJsonEntity.fail("错误：备注长度过长，不能超过200字", null);
            }
            Dict completeType = MSDictUtils.getDictByValue(servicePointCompleteType.trim(), "completed_type");
            if (completeType == null) {
                return AjaxJsonEntity.fail("错误：系统中未定义此完成类型，请确认", null);
            }
            orderOperationService.servicePointCompleteOrder(order, user, completeType, order.getRemarks());
            return AjaxJsonEntity.success("订单已标记完成", null);
        } catch (Exception e) {
            if (order != null && order.getOrderId() != null) {
                log.error("[complete] orderId:{}", order.getOrderId(), e);
            } else {
                log.error("complete]", e);
            }
            return AjaxJsonEntity.fail(e.getMessage(), null);
        }
    }

    /**
     * 跟踪订单进度 form 安维人员添加的跟踪进度
     */
    @RequestMapping(value = "servicePointTracking", method = RequestMethod.GET)
    public String servicePointTracking(String orderId, String quarter, HttpServletRequest request, Model model) {
        Long lorderId = StringUtils.toLong(orderId);
        if (lorderId <= 0) {
            model.addAttribute("canAction", false);
            return VIEW_NAME_SERVICEPOINT_TRACKING_FORM;
        }

        Order order = orderCacheReadService.getOrderById(lorderId, quarter, OrderUtils.OrderDataLevel.DETAIL, true);
        if (order == null || order.getOrderCondition() == null) {
            addMessage(model, "错误：系统繁忙，读取订单失败，请重试。");
            model.addAttribute("canAction", false);
            return VIEW_NAME_SERVICEPOINT_TRACKING_FORM;
        }
        // 检查是否可以追踪
        if (!order.canTracking() && order.getOrderCondition().getPendingFlag() != 2) {
            addMessage(model, String.format("订单：%s 不能进度跟踪，当前订单状态:%s", order.getOrderNo(), order.getOrderCondition().getStatus().getLabel()));
            model.addAttribute("canAction", false);
            return VIEW_NAME_SERVICEPOINT_TRACKING_FORM;
        }

        order.setTrackingDate(new Date());
        order.setRemarks("");
        User user = UserUtils.getUser();
        //读取跟踪进度 statusFlag:4 进度跟踪 closeFlag=2:安维提交
        List<OrderProcessLog> list = orderOperationService.getAppOrderLogs(lorderId, order.getQuarter().trim());
        if (user.isEngineer()) {
            Engineer e = servicePointService.getEngineer(user.getEngineerId());
            //派单记录
            List<OrderPlan> plans = orderOperationService.getOrderPlanList(lorderId, order.getQuarter(), null);
            list = OrderUtils.filterServicePointOrderProcessLog(list, plans, e.getServicePoint().getId());
        }
        order.setLogList(list);
        model.addAttribute("order", order);
        //切换为微服务
        List<Dict> types = dictService.findListByType("TrackingType");
        model.addAttribute("tracks", types);
        model.addAttribute("canAction", true);
        //质保金
        model.addAttribute("showInsurance",false);
        List<OrderInsurance> orderInsurances = Lists.newArrayList();
        Dict status = order.getOrderCondition().getStatus();
        if(status!=null && status.getIntValue()>=50 && status.getIntValue()<=100){
            User currentUser = UserUtils.getUser();
            if(currentUser.isEngineer() && currentUser.getCompanyId() > 0){
                orderInsurances =orderOperationService.getInsuranceByServicePoint(lorderId,quarter,currentUser.getCompanyId());
                if(orderInsurances!=null && orderInsurances.size()>0){
                    model.addAttribute("showInsurance",true);
                    model.addAttribute("orderInsurances",orderInsurances);
                }
            }
        }
        return VIEW_NAME_SERVICEPOINT_TRACKING_FORM;
    }


    /**
     * 安维提交跟踪进度(ajax)
     */
    @ResponseBody
    @RequestMapping(value = "saveServicePointTracking", method = RequestMethod.POST)
    public AjaxJsonEntity saveServicePointTracking(Order order, HttpServletResponse response) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try {
            User user = UserUtils.getUser();
            if (user == null) {
                return AjaxJsonEntity.fail("登录超时，请刷新页面并重新登录！", null);
            }
            order.setCreateBy(user);
            order.setCreateDate(new Date());
            OrderProcessLog log = orderOperationService.saveTracking(order);
            result.setData(log);
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("进度跟踪时发生异常:" + e.getMessage());
        }
        return result;
    }

}

