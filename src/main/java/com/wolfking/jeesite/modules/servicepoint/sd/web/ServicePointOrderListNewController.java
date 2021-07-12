package com.wolfking.jeesite.modules.servicepoint.sd.web;


import com.google.common.base.Splitter;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.Engineer;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderServicePointSearchModel;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.servicepoint.ms.md.SpServicePointService;
import com.wolfking.jeesite.modules.servicepoint.sd.service.ServicePointOrderListNewService;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;


/**
 * 网点工单列表
 */
@Slf4j
@Controller
@RequestMapping(value = "${adminPath}/servicePoint/sd/orderList/")
public class ServicePointOrderListNewController extends BaseController {

    @Autowired
    private SpServicePointService servicePointService;

    @Autowired
    private ServicePointOrderListNewService orderListNewService;

    private static final String HTTP_METHOD_POST = "post";
    private static final String MODEL_ATTR_PAGE = "page";
    private static final String MODEL_ATTR_ORDER = "order";

    private static final String VIEW_NAME_NOAPPOINTMENT_LIST = "modules/servicePoint/sd/orderList/noAppointmentList";
    private static final String VIEW_NAME_ARRIVEAPPOINTMENT_LIST = "modules/servicePoint/sd/orderList/arriveAppointmentList";
    private static final String VIEW_NAME_PASSAPPOINTMENT_LIST = "modules/servicePoint/sd/orderList/passAppointmentList";
    private static final String VIEW_NAME_PENDING_LIST = "modules/servicePoint/sd/orderList/pendingList";
    private static final String VIEW_NAME_SERVICED_LIST = "modules/servicePoint/sd/orderList/servicedList";
    private static final String VIEW_NAME_APP_COMPLETED_LIST = "modules/servicePoint/sd/orderList/appCompletedList";
    private static final String VIEW_NAME_UNCOMPLETED_LIST = "modules/servicePoint/sd/orderList/uncompletedList";
    private static final String VIEW_NAME_ALL_LIST = "modules/servicePoint/sd/orderList/allList";
    private static final String VIEW_NAME_COMPLETED_LIST = "modules/servicePoint/sd/orderList/completedList";
    private static final String VIEW_NAME_WAITINGACCESORY_LIST = "modules/servicePoint/sd/orderList/waitingAccesoryList";
    private static final String VIEW_NAME_RETURN_LIST = "modules/servicePoint/sd/orderList/returnList";
    private static final String VIEW_NAME_REMINDER_LIST = "modules/servicePoint/sd/orderList/reminderList";


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
                if (servicePointId != null) {
                    List<Engineer> engineers = servicePointService.getEngineerListOfServicePoint(servicePointId);
                    searchModel.setEngineerList(engineers);
                }
            } else {
                searchModel.setEngineerId(currentUser.getEngineerId());
            }
        }

        Date now = new Date();
        if (StringUtils.isBlank(searchModel.getAcceptDateRange())) {
            searchModel.setBeginAcceptDate(DateUtils.addMonth(now, -1));
            searchModel.setEndAcceptDate(now);
        } else {
            List<String> dates = Splitter.onPattern("~") //[~|-]
                    .omitEmptyStrings()
                    .trimResults()
                    .splitToList(searchModel.getAcceptDateRange());
            if (dates.isEmpty()) {
                searchModel.setBeginAcceptDate(DateUtils.addMonth(now, -1));
                searchModel.setEndAcceptDate(now);
            } else {
                searchModel.setBeginAcceptDate(DateUtils.parseDate(dates.get(0)));
                if (dates.size() > 1) {
                    searchModel.setEndAcceptDate(DateUtils.parseDate(dates.get(1)));
                } else {
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
     *
     * @param searchModel
     * @param model
     * @return
     */
    private Boolean checkOrderNoAndPhone(OrderServicePointSearchModel searchModel, Model model, Page<Order> page) {
        if (searchModel == null) {
            return true;
        }
        //检查电话
        if (StringUtils.isNotBlank(searchModel.getOrderNo())) {
            int orderSerchType = searchModel.getOrderNoSearchType();
            if (orderSerchType != 1) {
                addMessage(model, "错误：请输入正确的订单号码");
                model.addAttribute(MODEL_ATTR_PAGE, page);
                model.addAttribute(MODEL_ATTR_ORDER, searchModel);
                return false;
            } else {
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
                                addMessage(model, "错误：请输入正确的订单号码,日期超出范围");
                                model.addAttribute(MODEL_ATTR_PAGE, page);
                                model.addAttribute(MODEL_ATTR_ORDER, searchModel);
                                return false;
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("检查分片错误,orderNo:{}", searchModel.getOrderNo(), e);
                }
            }
        }
        if (StringUtils.isNotBlank(searchModel.getUserPhone())) {
            if (searchModel.getIsPhone() != 1) {
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
        Boolean isValide = checkOrderNoAndPhone(order, model, page);
        if (!isValide) {
            return VIEW_NAME_REMINDER_LIST;
        }
        try {
            page = orderListNewService.findReminderOrderList(new Page<>(request, response), order);
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
        Boolean isValide = checkOrderNoAndPhone(order, model, page);
        if (!isValide) {
            return VIEW_NAME_NOAPPOINTMENT_LIST;
        }
        try {
            page = orderListNewService.findServicePointNoAppointmentOrderList(new Page<>(request, response), order);
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
        Boolean isValide = checkOrderNoAndPhone(order, model, page);
        if (!isValide) {
            return VIEW_NAME_ARRIVEAPPOINTMENT_LIST;
        }
        try {
            page = orderListNewService.findServicePointArriveAppointmentOrderList(new Page<>(request, response), order);
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
        Boolean isValide = checkOrderNoAndPhone(order, model, page);
        if (!isValide) {
            return VIEW_NAME_PASSAPPOINTMENT_LIST;
        }
        try {
            page = orderListNewService.findServicePointPassAppointmentOrderList(new Page<>(request, response), order);
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
        Boolean isValide = checkOrderNoAndPhone(order, model, page);
        if (!isValide) {
            return VIEW_NAME_PENDING_LIST;
        }
        try {
            page = orderListNewService.findServicePointPendingOrderList(new Page<>(request, response), order);
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
        Boolean isValide = checkOrderNoAndPhone(order, model, page);
        if (!isValide) {
            return VIEW_NAME_SERVICED_LIST;
        }
        try {
            page = orderListNewService.findServicePointServicedOrderList(new Page<>(request, response), order);
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
        Boolean isValide = checkOrderNoAndPhone(order, model, page);
        if (!isValide) {
            return VIEW_NAME_APP_COMPLETED_LIST;
        }
        try {
            page = orderListNewService.findServicePointAppCompletedOrderList(new Page<>(request, response), order);
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
        Boolean isValide = checkOrderNoAndPhone(order, model, page);
        if (!isValide) {
            return VIEW_NAME_UNCOMPLETED_LIST;
        }
        try {
            page = orderListNewService.findServicePointUncompletedOrderList(new Page<>(request, response), order);
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
            Boolean isValide = checkOrderNoAndPhone(order, model, page);
            if (!isValide) {
                return VIEW_NAME_ALL_LIST;
            }
            try {
                page = orderListNewService.findServicePointAllOrderList(new Page<>(request, response), order);
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
            Boolean isValide = checkOrderNoAndPhone(order, model, page);
            if (!isValide) {
                return VIEW_NAME_COMPLETED_LIST;
            }
            //完成日期
            if (!StringUtils.isBlank(order.getCompleteDateRange())) {
                List<String> dates = Splitter.onPattern("~") //[~|-]
                        .omitEmptyStrings()
                        .trimResults()
                        .splitToList(order.getCompleteDateRange());
                if (!dates.isEmpty()) {
                    if (StringUtils.isNotBlank(dates.get(0))) {
                        order.setBeginCompleteDate(DateUtils.parseDate(dates.get(0)));
                    }
                    if (dates.size() > 1) {
                        if (StringUtils.isNotBlank(dates.get(1))) {
                            order.setEndCompleteDate(DateUtils.getEndOfDay(DateUtils.parseDate(dates.get(1))));
                        }
                    }
                }
            }
            //if (order.getEndCompleteDate() != null) {
            //    order.setEndCompleteDate(DateUtils.getDateEnd(order.getEndCompleteDate()));
            //}
            try {
                page = orderListNewService.findServicePointCompletedOrderList(new Page<>(request, response), order);
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
     */
    @RequiresPermissions("sd:order:engineeraccept")
    @RequestMapping(value = {"waitingAccessoryList"})
    public String waitingAccessoryOrderList(OrderServicePointSearchModel order, Model model,
                                            HttpServletRequest request, HttpServletResponse response) {

        Page<Order> page = new Page<>(request, response);
        order = setSearchModel(order);
        Boolean isValide = checkOrderNoAndPhone(order, model, page);
        if (!isValide) {
            return VIEW_NAME_WAITINGACCESORY_LIST;
        }
        Date appointDate = DateUtils.getEndOfDay(new Date());
        order.setAppointmentDate(appointDate);
        try {
            page = orderListNewService.findServicePointWaitingAccessoryList(new Page<>(request, response), order);
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
        Boolean isValide = checkOrderNoAndPhone(order, model, page);
        if (!isValide) {
            return VIEW_NAME_RETURN_LIST;
        }
        if (request.getMethod().equalsIgnoreCase("post")) {
            //退单日期
            if (!StringUtils.isBlank(order.getCompleteDateRange())) {
                List<String> dates = Splitter.onPattern("~") //[~|-]
                        .omitEmptyStrings()
                        .trimResults()
                        .splitToList(order.getCompleteDateRange());
                if (!dates.isEmpty()) {
                    if (StringUtils.isNotBlank(dates.get(0))) {
                        order.setBeginCompleteDate(DateUtils.parseDate(dates.get(0)));
                    }
                    if (dates.size() > 1) {
                        if (StringUtils.isNotBlank(dates.get(1))) {
                            order.setEndCompleteDate(DateUtils.getEndOfDay(DateUtils.parseDate(dates.get(1))));
                        }
                    }
                }
            }
            try {
                page = orderListNewService.findServicePointReturnedList(new Page<>(request, response), order);
            } catch (Exception e) {
                addMessage(model, "错误：" + e.getMessage());
            }
        }
        model.addAttribute(MODEL_ATTR_PAGE, page);
        model.addAttribute(MODEL_ATTR_ORDER, order);
        return VIEW_NAME_RETURN_LIST;
    }


    //endregion 网点工单列表

}

