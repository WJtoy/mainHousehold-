package com.wolfking.jeesite.modules.sd.web;


import com.google.common.collect.Lists;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.entity.cc.Reminder;
import com.kkl.kklplus.entity.cc.vm.BulkRereminderCheckModel;
import com.kkl.kklplus.entity.cc.vm.ReminderOrderModel;
import com.wolfking.jeesite.common.persistence.IntegerRange;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.CustomerFinance;
import com.wolfking.jeesite.modules.md.entity.Product;
import com.wolfking.jeesite.modules.md.entity.UrgentLevel;
import com.wolfking.jeesite.modules.md.service.CustomerService;
import com.wolfking.jeesite.modules.md.service.ProductService;
import com.wolfking.jeesite.modules.md.service.UrgentLevelService;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderSearchModel;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.modules.sd.service.SalesOrderService;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.cc.service.ReminderService;
import com.wolfking.jeesite.ms.tmall.md.entity.B2bCustomerMap;
import com.wolfking.jeesite.ms.tmall.md.service.B2bCustomerMapService;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 业务订单Controller
 *
 * @author Ryan
 */
@Controller
@RequestMapping(value = "${adminPath}/sd/order/salesNew")
@Slf4j
public class SalesOrderController extends BaseController {

    @Autowired
    public SalesOrderController(ProductService productService,
                                CustomerService customerService,
                                UrgentLevelService urgentLevelService,
                                B2bCustomerMapService b2bCustomerMapService,
                                SalesOrderService salesOrderService,
                                ReminderService reminderService,
                                OrderService orderService) {
        super();
        this.productService = productService;
        this.customerService = customerService;
        this.urgentLevelService = urgentLevelService;
        this.b2bCustomerMapService = b2bCustomerMapService;
        this.salesOrderService = salesOrderService;
        this.reminderService = reminderService;
        this.orderService = orderService;
    }

    private final ProductService productService;
    private final CustomerService customerService;
    private final UrgentLevelService urgentLevelService;
    private final B2bCustomerMapService b2bCustomerMapService;
    private final SalesOrderService salesOrderService;
    private final ReminderService reminderService;
    private final OrderService orderService;

    private static final String MODEL_ATTR_PAGE = "page";
    private static final String MODEL_ATTR_ORDER = "order";
    private static final String MODEL_ATTR_PRODUCT_LIST = "productList";
    private static final String MODEL_ATTR_CAN_SEARCH = "canSearch";

    private static final int DEFAULT_PAGE_SIZE = 10;

    private static final String VIEW_NAME_PROCESSING_ORDER_LIST = "modules/sd/salesNew/list";
    private static final String VIEW_NAME_COMPLETED_ORDER_LIST = "modules/sd/salesNew/finishList";
    private static final String VIEW_NAME_CANCELED_ORDER_LIST = "modules/sd/salesNew/cancelList";
    private static final String VIEW_NAME_RETURNED_ORDER_LIST = "modules/sd/salesNew/returnList";
    private static final String VIEW_NAME_ALL_ORDER_LIST = "modules/sd/salesNew/allList";
    private static final String VIEW_NAME_COMPLAINED_ORDER_LIST = "modules/sd/salesNew/complainList";
    private static final String VIEW_NAME_REMINDER_ORDER_LIST = "modules/sd/salesNew/reminderList";
    private static final String VIEW_NAME_WAITING_PARTS_ORDER_LIST = "modules/sd/salesNew/orderListMaterial";

    //region 列表
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
        if(StringUtils.isNotBlank(searchModel.getOrderNo())) {
            int orderSerchType = searchModel.getOrderNoSearchType();
            if (orderSerchType != 1) {
                addMessage(model, "错误：请输入正确的订单号码");
                model.addAttribute(MODEL_ATTR_PAGE, page);
                model.addAttribute(MODEL_ATTR_ORDER, searchModel);
                model.addAttribute(MODEL_ATTR_CAN_SEARCH, false);
                model.addAttribute(MODEL_ATTR_PRODUCT_LIST, Lists.newArrayList());
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
                                model.addAttribute(MODEL_ATTR_CAN_SEARCH, false);
                                model.addAttribute(MODEL_ATTR_PRODUCT_LIST, Lists.newArrayList());
                                return false;
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("检查分片错误,orderNo:{}", searchModel.getOrderNo(), e);
                }
            }
        }
        if (StringUtils.isNotBlank(searchModel.getPhone1())){
            if(searchModel.getIsPhone() != 1){
                addMessage(model, "错误：请输入正确的用户电话");
                model.addAttribute(MODEL_ATTR_PAGE, page);
                model.addAttribute(MODEL_ATTR_ORDER, searchModel);
                model.addAttribute(MODEL_ATTR_CAN_SEARCH, false);
                model.addAttribute(MODEL_ATTR_PRODUCT_LIST, Lists.newArrayList());
                return false;
            }
        }
        return true;
    }

    /**
     * 初始化查询区域
     */
    private void initSearchArea(OrderSearchModel searchModel){
        if(searchModel == null){
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
    }

    /**
     * 我的订单之处理中(业务)
     */
    @RequiresPermissions(value = {"sd:order:edit", "sd:order:approvenew"},logical = Logical.OR)
    @RequestMapping(value = "list")
    public String salesOrderList(OrderSearchModel order, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<Order> page = new Page<>();
        page.setPageSize(DEFAULT_PAGE_SIZE);
        User user = UserUtils.getUser();
        if (!user.isSystemUser()) {
            addMessage(model, "错误：无权限，此功能只开放给后台人员使用。");
            model.addAttribute(MODEL_ATTR_PRODUCT_LIST, Lists.newArrayList());
            model.addAttribute(MODEL_ATTR_PAGE, page);
            model.addAttribute(MODEL_ATTR_ORDER, order);
            model.addAttribute(MODEL_ATTR_CAN_SEARCH, false);
            return VIEW_NAME_PROCESSING_ORDER_LIST;
        }
        Boolean isValide = checkOrderNoAndPhone(order,model,page);
        if(!isValide){
            return VIEW_NAME_PROCESSING_ORDER_LIST;
        }
        Boolean canSearch = true;
        if (user.isSaleman()) {
            order.setSalesId(user.getId());//业务员
            order.setSubUserType(user.getSubFlag());//子账号类型
            List<Long> offlineCustomers = customerService.findIdListByOfflineOrderFlagFromCacheForSD();
            if(!CollectionUtils.isEmpty(offlineCustomers)){
                order.setOfflineCustomerList(offlineCustomers);
            }
        }
        //第一次进入页面时，检查全局加急标记
        if (request.getMethod().equalsIgnoreCase("get")) {
            String urgentFlag = MSDictUtils.getDictSingleValue("OrderUrgentFlag", "0");
            order.setUrgentFlag(Integer.valueOf(urgentFlag));
        }
        //date
        if (order.getBeginDate() == null) {
            order.setEndDate(DateUtils.getDateEnd(new Date()));
            order.setBeginDate(DateUtils.getDateStart(DateUtils.addMonth(new Date(), -1)));
        } else {
            order.setEndDate(DateUtils.getDateEnd(order.getEndDate()));
        }
        //读取客户账户余额
        getCustomerFinanceBalance(order);
        //查询区域
        initSearchArea(order);
        order.setOrderDataLevel(OrderUtils.OrderDataLevel.FEE);
        try {
            order.setOrderNo(order.getOrderNo().trim());
            order.setUserName(order.getUserName().trim());
            order.setPhone1(order.getPhone1().trim());
            order.setAddress(order.getAddress().trim());
            order.setCreator(order.getCreator().trim());
            order.setRemarks(order.getRemarks().trim());
            page = salesOrderService.getProcessingOrderLit(new Page<>(request, response, DEFAULT_PAGE_SIZE), order);
            //判断权限
            Subject subject = SecurityUtils.getSubject();
            if(subject.isPermitted("sd:reminder:create")) {
                List<Order> orders = page.getList();
                orderCanReminder(orders);
            }
            //加急
            List<UrgentLevel> urgentLevels = urgentLevelService.findAllList();
            order.setUrgentLevels(urgentLevels);
            //b2b
            if (order.getCustomer() != null && order.getCustomer().getId() > 0 && B2BDataSourceEnum.isDataSource(order.getDataSource())) {
                List<B2bCustomerMap> shopList = b2bCustomerMapService.getShopListByCustomerNew(order.getDataSource(), order.getCustomer().getId());
                order.setShopList(shopList);
            }
        } catch (Exception e) {
            addMessage(model, "错误：" + e.getMessage());
        }
        //所有产品
        List<Product> products = productService.findAllList();

        model.addAttribute(MODEL_ATTR_PRODUCT_LIST, products);
        model.addAttribute(MODEL_ATTR_PAGE, page);
        model.addAttribute(MODEL_ATTR_ORDER, order);
        model.addAttribute(MODEL_ATTR_CAN_SEARCH, canSearch);
        return VIEW_NAME_PROCESSING_ORDER_LIST;
    }

    /**
     * 读取客户账户余额
     * @param order
     */
    private void getCustomerFinanceBalance(OrderSearchModel order) {
        if (order.getCustomer() != null && order.getCustomer().getId() != null && order.getCustomer().getId() > 0) {
            //Double balance = customerService.getBalanceAmount(order.getCustomer().getId());
            //order.setBalance(balance);
            CustomerFinance fi = customerService.getFinanceForAddOrder(order.getCustomer().getId());
            double balance = fi.getBalance() - fi.getBlockAmount();
            BigDecimal amount = new BigDecimal(balance);
            order.setBalance(amount.doubleValue());
        }
    }

    /**
     * 判断订单是否可以催单
     */
    private void orderCanReminder(List<Order> orders) {
        if (ObjectUtils.isEmpty(orders)) {
            return;
        }
        ReminderOrderModel orderModel;
        Order o;
        Reminder reminder;
        List<ReminderOrderModel> orderModelList = Lists.newArrayList();
        int orderStatus;
        int size = orders.size();
        //将关闭的订单筛选掉
        for (int i = 0; i < size; i++) {
            o = orders.get(i);
            orderStatus = o.getOrderCondition().getStatusValue();
            //未完成
            //TODO: APP完工[55]
//            if(orderStatus <= Order.ORDER_STATUS_SERVICED) {
            if(orderStatus <= Order.ORDER_STATUS_APP_COMPLETED) {
                orderModel = ReminderOrderModel.builder()
                        .orderId(o.getId())
                        .quarter(o.getQuarter())
                        .reminderFlag(o.getOrderStatus().getReminderStatus())
                        .createAt(o.getOrderCondition().getCreateDate().getTime())
                        .build();
                orderModelList.add(orderModel);
            }else{
                //订单已关闭，包含完成，退单，取消，退单申请
                o.getOrderCondition().setReminderCheckResult(4);
                o.getOrderCondition().setReminderCheckTitle("订单已关闭，不能催单");
            }
        }
        BulkRereminderCheckModel bulkRereminderCheckModel = new BulkRereminderCheckModel(orderModelList);
        Map<Long, Reminder> maps = reminderService.bulkRereminderCheck(bulkRereminderCheckModel);
        if (!ObjectUtils.isEmpty(maps)) {
            for (int i = 0; i < size; i++) {
                o = orders.get(i);
                reminder = maps.get(o.getId());
                if (reminder != null) {
                    o.getOrderCondition().setReminderCheckResult(reminder.getDelFlag());
                    o.getOrderCondition().setReminderCheckTitle(StringUtils.isBlank(reminder.getRemarks()) ? "" : reminder.getRemarks());
                    o.getOrderCondition().setReminderTimes(reminder.getReminderTimes());//2020-06-12
                }
            }
        }
    }

    /**
     * 我的订单之完成列表(业务)
     * 订单状态(=80)：Completed
     *
     * @param order 查询条件
     */
    @RequiresPermissions("sd:order:view")
    @RequestMapping(value = "finishlist")
    public String salesFinishlist(OrderSearchModel order, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<Order> page = new Page<>();
        page.setPageSize(DEFAULT_PAGE_SIZE);
        User user = UserUtils.getUser();
        if (user == null || user.getId() == null) {
            addMessage(model, "错误：登录超时，请退出后重新登录。");
            model.addAttribute(MODEL_ATTR_PRODUCT_LIST, Lists.newArrayList());
            model.addAttribute(MODEL_ATTR_PAGE, page);
            model.addAttribute(MODEL_ATTR_ORDER, order);
            model.addAttribute(MODEL_ATTR_CAN_SEARCH, false);
            return VIEW_NAME_COMPLETED_ORDER_LIST;
        }
        if (!user.isSystemUser()) {
            addMessage(model, "错误：无权限，此功能只开放给后台人员使用。");
            model.addAttribute(MODEL_ATTR_PRODUCT_LIST, Lists.newArrayList());
            model.addAttribute(MODEL_ATTR_PAGE, page);
            model.addAttribute(MODEL_ATTR_ORDER, order);
            model.addAttribute(MODEL_ATTR_CAN_SEARCH, false);
            return VIEW_NAME_COMPLETED_ORDER_LIST;
        }
        Boolean isValide = checkOrderNoAndPhone(order,model,page);
        if(!isValide){
            return VIEW_NAME_COMPLETED_ORDER_LIST;
        }
        Boolean canSearch = true;
        //提交查询
        if (request.getMethod().equalsIgnoreCase("post")) {
            //查询区域
            initSearchArea(order);
            if (user.isSaleman()) {
                order.setSalesId(user.getId());//业务员
                order.setSubUserType(user.getSubFlag());//子账号类型
                List<Long> offlineCustomers = customerService.findIdListByOfflineOrderFlagFromCacheForSD();
                if(!CollectionUtils.isEmpty(offlineCustomers)){
                    order.setOfflineCustomerList(offlineCustomers);
                }
            }
            //读取客户账户余额
            getCustomerFinanceBalance(order);
            //date
            if (order.getBeginDate() == null) {
                order.setEndDate(DateUtils.getDateEnd(new Date()));
                order.setBeginDate(DateUtils.getDateStart(DateUtils.addMonth(new Date(), -1)));
            } else {
                order.setEndDate(DateUtils.getDateEnd(order.getEndDate()));
            }
            //完成日期
            if (order.getCompleteEnd() != null) {
                order.setCompleteEnd(DateUtils.getDateEnd(order.getCompleteEnd()));
            }
            try {
                order.setOrderNo(order.getOrderNo().trim());
                order.setUserName(order.getUserName().trim());
                order.setPhone1(order.getPhone1().trim());
                order.setAddress(order.getAddress().trim());
                order.setCreator(order.getCreator().trim());
                order.setRemarks(order.getRemarks().trim());
                page = salesOrderService.getCompletedOrderLit(new Page<>(request, response, DEFAULT_PAGE_SIZE), order);

                //b2b
                if (order.getCustomer() != null && order.getCustomer().getId() > 0 && B2BDataSourceEnum.isDataSource(order.getDataSource())) {
                    List<B2bCustomerMap> shopList = b2bCustomerMapService.getShopListByCustomerNew(order.getDataSource(), order.getCustomer().getId());
                    order.setShopList(shopList);
                }
            } catch (Exception e) {
                addMessage(model, "错误：" + e.getMessage());
            }
        } else {
            //访问页面，未查询
            order.setEndDate(new Date());
            order.setBeginDate(DateUtils.getDateStart(DateUtils.addMonth(new Date(), -1)));
        }
        //加急
        List<UrgentLevel> urgentLevels = urgentLevelService.findAllList();
        order.setUrgentLevels(urgentLevels);
        //所有产品
        List<Product> products = productService.findAllList();
        model.addAttribute(MODEL_ATTR_PRODUCT_LIST, products);
        model.addAttribute(MODEL_ATTR_PAGE, page);
        model.addAttribute(MODEL_ATTR_ORDER, order);
        model.addAttribute(MODEL_ATTR_CAN_SEARCH, canSearch);
        return VIEW_NAME_COMPLETED_ORDER_LIST;
    }

    /**
     * 我的订单之取消列表(业务)
     * 订单状态(=100)
     *
     * @param order 查询条件
     */
    @RequiresPermissions("sd:order:view")
    @RequestMapping(value = "cancellist")
    public String salesCancelList(OrderSearchModel order, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<Order> page = new Page<>();
        page.setPageSize(DEFAULT_PAGE_SIZE);
        User user = UserUtils.getUser();
        if (user == null || user.getId() == null) {
            addMessage(model, "错误：登录超时，请退出后重新登录。");
            model.addAttribute(MODEL_ATTR_PRODUCT_LIST, Lists.newArrayList());
            model.addAttribute(MODEL_ATTR_PAGE, page);
            model.addAttribute(MODEL_ATTR_ORDER, order);
            model.addAttribute(MODEL_ATTR_CAN_SEARCH, false);
            return VIEW_NAME_CANCELED_ORDER_LIST;
        }
        if (!user.isSystemUser()) {
            addMessage(model, "错误：无权限，此功能只开放给后台人员使用。");
            model.addAttribute(MODEL_ATTR_PRODUCT_LIST, Lists.newArrayList());
            model.addAttribute(MODEL_ATTR_PAGE, page);
            model.addAttribute(MODEL_ATTR_ORDER, order);
            model.addAttribute(MODEL_ATTR_CAN_SEARCH, false);
            return VIEW_NAME_CANCELED_ORDER_LIST;
        }
        Boolean isValide = checkOrderNoAndPhone(order,model,page);
        if(!isValide){
            return VIEW_NAME_CANCELED_ORDER_LIST;
        }
        Boolean canSearch = true;
        //提交查询
        if (request.getMethod().equalsIgnoreCase("post")) {
            //查询区域
            initSearchArea(order);
            if (user.isSaleman()) {
                order.setSalesId(user.getId());//业务员
                order.setSubUserType(user.getSubFlag());//子账号类型
                List<Long> offlineCustomers = customerService.findIdListByOfflineOrderFlagFromCacheForSD();
                if(!CollectionUtils.isEmpty(offlineCustomers)){
                    order.setOfflineCustomerList(offlineCustomers);
                }
            }
            //读取客户账户余额
            getCustomerFinanceBalance(order);
            //date
            if (order.getBeginDate() == null) {
                order.setEndDate(DateUtils.getDateEnd(new Date()));
                order.setBeginDate(DateUtils.getDateStart(DateUtils.addMonth(new Date(), -1)));
            } else {
                order.setEndDate(DateUtils.getDateEnd(order.getEndDate()));
            }
            //取消日期
            if (order.getCompleteEnd() != null) {
                order.setCompleteEnd(DateUtils.getDateEnd(order.getCompleteEnd()));
            }
            try {
                order.setOrderNo(order.getOrderNo().trim());
                order.setUserName(order.getUserName().trim());
                order.setPhone1(order.getPhone1().trim());
                order.setAddress(order.getAddress().trim());
                order.setCreator(order.getCreator().trim());
                order.setRemarks(order.getRemarks().trim());
                page = salesOrderService.getCanceledOrderLit(new Page<>(request, response, DEFAULT_PAGE_SIZE), order);
                //b2b
                if (order.getCustomer() != null && order.getCustomer().getId() > 0 && order.getDataSource() > 1) {
                    List<B2bCustomerMap> shopList = b2bCustomerMapService.getShopListByCustomer(order.getDataSource(), order.getCustomer().getId());
                    order.setShopList(shopList);
                }
            } catch (Exception e) {
                addMessage(model, "错误：" + e.getMessage());
            }
        } else {
            //访问页面，未查询
            order.setEndDate(new Date());
            order.setBeginDate(DateUtils.getDateStart(DateUtils.addMonth(new Date(), -1)));
        }
        //所有产品
        List<Product> products = productService.findAllList();

        model.addAttribute(MODEL_ATTR_PRODUCT_LIST, products);
        model.addAttribute(MODEL_ATTR_PAGE, page);
        model.addAttribute(MODEL_ATTR_ORDER, order);
        model.addAttribute(MODEL_ATTR_CAN_SEARCH, canSearch);
        return VIEW_NAME_CANCELED_ORDER_LIST;
    }

    /**
     * 我的订单之退单列表(业务)
     * 订单状态(=90)
     *
     * @param order 查询条件
     */
    @RequiresPermissions("sd:order:view")
    @RequestMapping(value = "returnlist")
    public String salesReturnList(OrderSearchModel order, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<Order> page = new Page<>();
        page.setPageSize(DEFAULT_PAGE_SIZE);
        User user = UserUtils.getUser();
        if (user == null || user.getId() == null) {
            addMessage(model, "错误：登录超时，请退出后重新登录。");
            model.addAttribute(MODEL_ATTR_PRODUCT_LIST, Lists.newArrayList());
            model.addAttribute(MODEL_ATTR_PAGE, page);
            model.addAttribute(MODEL_ATTR_ORDER, order);
            model.addAttribute(MODEL_ATTR_CAN_SEARCH, false);
            return VIEW_NAME_RETURNED_ORDER_LIST;
        }
        if (!user.isSystemUser()) {
            addMessage(model, "错误：无权限，此功能只开放给后台人员使用。");
            model.addAttribute(MODEL_ATTR_PRODUCT_LIST, Lists.newArrayList());
            model.addAttribute(MODEL_ATTR_PAGE, page);
            model.addAttribute(MODEL_ATTR_ORDER, order);
            model.addAttribute(MODEL_ATTR_CAN_SEARCH, false);
            return VIEW_NAME_RETURNED_ORDER_LIST;
        }
        Boolean isValide = checkOrderNoAndPhone(order,model,page);
        if(!isValide){
            return VIEW_NAME_RETURNED_ORDER_LIST;
        }
        Boolean canSearch = true;
        //提交查询
        if (request.getMethod().equalsIgnoreCase("post")) {
            //查询区域
            initSearchArea(order);
            if (user.isSaleman()) {
                order.setSalesId(user.getId());//业务员
                order.setSubUserType(user.getSubFlag());//子账号类型
                List<Long> offlineCustomers = customerService.findIdListByOfflineOrderFlagFromCacheForSD();
                if(!CollectionUtils.isEmpty(offlineCustomers)){
                    order.setOfflineCustomerList(offlineCustomers);
                }
            }
            //读取客户账户余额
            getCustomerFinanceBalance(order);
            //date
            if (order.getBeginDate() == null) {
                order.setEndDate(DateUtils.getDateEnd(new Date()));
                order.setBeginDate(DateUtils.getDateStart(DateUtils.addMonth(new Date(), -1)));
            } else {
                order.setEndDate(DateUtils.getDateEnd(order.getEndDate()));
            }
            //取消日期
            if (order.getCompleteEnd() != null) {
                order.setCompleteEnd(DateUtils.getDateEnd(order.getCompleteEnd()));
            }
            try {
                order.setOrderNo(order.getOrderNo().trim());
                order.setUserName(order.getUserName().trim());
                order.setPhone1(order.getPhone1().trim());
                order.setAddress(order.getAddress().trim());
                order.setCreator(order.getCreator().trim());
                order.setRemarks(order.getRemarks().trim());
                page = salesOrderService.getReturnedOrderLit(new Page<>(request, response, DEFAULT_PAGE_SIZE), order);
                //b2b
                if (order.getCustomer() != null && order.getCustomer().getId() > 0 && B2BDataSourceEnum.isDataSource(order.getDataSource())) {
                    List<B2bCustomerMap> shopList = b2bCustomerMapService.getShopListByCustomerNew(order.getDataSource(), order.getCustomer().getId());
                    order.setShopList(shopList);
                }
            } catch (Exception e) {
                addMessage(model, "错误：" + e.getMessage());
            }
        } else {
            //访问页面，未查询
            order.setEndDate(new Date());
            order.setBeginDate(DateUtils.getDateStart(DateUtils.addMonth(new Date(), -1)));
        }

        //所有产品
        List<Product> products = productService.findAllList();
        model.addAttribute(MODEL_ATTR_PRODUCT_LIST, products);
        model.addAttribute(MODEL_ATTR_PAGE, page);
        model.addAttribute(MODEL_ATTR_ORDER, order);
        model.addAttribute(MODEL_ATTR_CAN_SEARCH, canSearch);
        return VIEW_NAME_RETURNED_ORDER_LIST;
    }


    /**
     * 我的订单之完成列表(业务)
     *
     * @param order 查询条件
     */
    @RequiresPermissions("sd:order:view")
    @RequestMapping(value = "alllist")
    public String salesAllList(OrderSearchModel order, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<Order> page = new Page<>();
        page.setPageSize(DEFAULT_PAGE_SIZE);
        User user = UserUtils.getUser();
        if (user == null || user.getId() == null) {
            addMessage(model, "错误：登录超时，请退出后重新登录。");
            model.addAttribute(MODEL_ATTR_PRODUCT_LIST, Lists.newArrayList());
            model.addAttribute(MODEL_ATTR_PAGE, page);
            model.addAttribute(MODEL_ATTR_ORDER, order);
            model.addAttribute(MODEL_ATTR_CAN_SEARCH, false);
            return VIEW_NAME_ALL_ORDER_LIST;
        }
        if (!user.isSystemUser()) {
            addMessage(model, "错误：无权限，此功能只开放给后台人员使用。");
            model.addAttribute(MODEL_ATTR_PRODUCT_LIST, Lists.newArrayList());
            model.addAttribute(MODEL_ATTR_PAGE, page);
            model.addAttribute(MODEL_ATTR_ORDER, order);
            model.addAttribute(MODEL_ATTR_CAN_SEARCH, false);
            return VIEW_NAME_ALL_ORDER_LIST;
        }
        Boolean isValide = checkOrderNoAndPhone(order,model,page);
        if(!isValide){
            return VIEW_NAME_ALL_ORDER_LIST;
        }
        Boolean canSearch = true;
        //提交查询
        if (request.getMethod().equalsIgnoreCase("post")) {
            //查询区域
            initSearchArea(order);
            if (user.isSaleman()) {
                order.setSalesId(user.getId());//业务员
                order.setSubUserType(user.getSubFlag());//子账号类型
                List<Long> offlineCustomers = customerService.findIdListByOfflineOrderFlagFromCacheForSD();
                if(!CollectionUtils.isEmpty(offlineCustomers)){
                    order.setOfflineCustomerList(offlineCustomers);
                }
            }
            //读取客户账户余额
            getCustomerFinanceBalance(order);
            // date
            if (order.getBeginDate() == null) {
                order.setEndDate(DateUtils.getDateEnd(new Date()));
                order.setBeginDate(DateUtils.getDateStart(DateUtils.addMonth(new Date(), -1)));
            } else {
                order.setEndDate(DateUtils.getDateEnd(order.getEndDate()));
            }
            String messageType = request.getParameter("messageType");
            if (StringUtils.isNoneBlank(messageType) && StringUtils.isNumeric(messageType)) {
                order.setMessageType(Integer.valueOf(messageType));
            } else {
                order.setMessageType(null);
            }
            try {
                order.setOrderNo(order.getOrderNo().trim());
                order.setUserName(order.getUserName().trim());
                order.setPhone1(order.getPhone1().trim());
                //order.setAddress(order.getAddress().trim());
                order.setCreator(order.getCreator().trim());
                order.setRemarks(order.getRemarks().trim());
                page = salesOrderService.getAllOrderLit(new Page<>(request, response, DEFAULT_PAGE_SIZE), order);
                //判断权限
                Subject subject = SecurityUtils.getSubject();
                if(subject.isPermitted("sd:reminder:create")) {
                    List<Order> orders = page.getList();
                    orderCanReminder(orders);
                }
                //b2b
                if (order.getCustomer() != null && order.getCustomer().getId() > 0 && B2BDataSourceEnum.isDataSource(order.getDataSource())) {
                    List<B2bCustomerMap> shopList = b2bCustomerMapService.getShopListByCustomerNew(order.getDataSource(), order.getCustomer().getId());
                    order.setShopList(shopList);
                }
            } catch (Exception e) {
                addMessage(model, "错误：" + e.getMessage());
            }
        } else {
            //访问页面，未查询
            order.setEndDate(new Date());
            order.setBeginDate(DateUtils.getDateStart(DateUtils.addMonth(new Date(), -1)));
        }
        //加急
        List<UrgentLevel> urgentLevels = urgentLevelService.findAllList();
        order.setUrgentLevels(urgentLevels);
        //所有产品
        List<Product> products = productService.findAllList();
        model.addAttribute(MODEL_ATTR_PRODUCT_LIST, products);
        model.addAttribute(MODEL_ATTR_PAGE, page);
        model.addAttribute(MODEL_ATTR_ORDER, order);
        model.addAttribute(MODEL_ATTR_CAN_SEARCH, canSearch);
        return VIEW_NAME_ALL_ORDER_LIST;
    }


    /**
     * 我的订单之投诉列表(业务)
     *
     * @param order 查询条件
     */
    @RequiresPermissions("sd:order:view")
    @RequestMapping(value = "complainlist")
    public String salesComplainList(OrderSearchModel order, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<Order> page = new Page<>();
        page.setPageSize(DEFAULT_PAGE_SIZE);
        User user = UserUtils.getUser();
        if (!user.isSystemUser()) {
            addMessage(model, "错误：无权限，此功能只开放给后台人员使用。");
            model.addAttribute(MODEL_ATTR_PRODUCT_LIST, Lists.newArrayList());
            model.addAttribute(MODEL_ATTR_PAGE, page);
            model.addAttribute(MODEL_ATTR_ORDER, order);
            model.addAttribute(MODEL_ATTR_CAN_SEARCH, false);
            return VIEW_NAME_COMPLAINED_ORDER_LIST;
        }
        Boolean isValide = checkOrderNoAndPhone(order,model,page);
        if(!isValide){
            return VIEW_NAME_COMPLAINED_ORDER_LIST;
        }
        Boolean canSearch = true;
        if (user.isSaleman()) {
            order.setSalesId(user.getId());//业务员
            order.setSubUserType(user.getSubFlag());//子账号类型
            List<Long> offlineCustomers = customerService.findIdListByOfflineOrderFlagFromCacheForSD();
            if(!CollectionUtils.isEmpty(offlineCustomers)){
                order.setOfflineCustomerList(offlineCustomers);
            }
        }
        //第一次进入页面时，检查全局加急标记
        if (request.getMethod().equalsIgnoreCase("get")) {
            String urgentFlag = MSDictUtils.getDictSingleValue("OrderUrgentFlag", "0");
            order.setUrgentFlag(Integer.valueOf(urgentFlag));
        }
        //读取客户账户余额
        getCustomerFinanceBalance(order);
        order.setOrderDataLevel(OrderUtils.OrderDataLevel.DETAIL);//从数据库/redis中读取具体的数据内容
        int subQueryComplain = 1;//是否子查询投诉表
        //投诉日期
        if (order.getCompleteBegin() == null) {
            order.setCompleteEnd(DateUtils.getDateEnd(new Date()));
            order.setCompleteBegin(DateUtils.getDateStart(DateUtils.addMonth(new Date(), -3)));//3个月
        } else {
            order.setCompleteEnd(DateUtils.getDateEnd(order.getCompleteEnd()));
        }
        String messageType = request.getParameter("messageType");
        if (StringUtils.isNoneBlank(messageType) && StringUtils.isNumeric(messageType)) {
            order.setMessageType(Integer.valueOf(messageType));
        } else {
            order.setMessageType(null);
        }
        //查询区域
        initSearchArea(order);
        try {
            order.setOrderNo(order.getOrderNo().trim());
            order.setUserName(order.getUserName().trim());
            order.setPhone1(order.getPhone1().trim());
            order.setAddress(order.getAddress().trim());
            order.setCreator(order.getCreator().trim());
            order.setRemarks(order.getRemarks().trim());
            page = salesOrderService.getComplainedOrderLit(new Page<OrderSearchModel>(request, response, DEFAULT_PAGE_SIZE), order);
            //判断权限
            Subject subject = SecurityUtils.getSubject();
            if(subject.isPermitted("sd:reminder:create")) {
                List<Order> orders = page.getList();
                orderCanReminder(orders);
            }
            //加急
            List<UrgentLevel> urgentLevels = urgentLevelService.findAllList();
            order.setUrgentLevels(urgentLevels);
            //b2b
            if (order.getCustomer() != null && order.getCustomer().getId() > 0 && B2BDataSourceEnum.isDataSource(order.getDataSource())) {
                List<B2bCustomerMap> shopList = b2bCustomerMapService.getShopListByCustomerNew(order.getDataSource(), order.getCustomer().getId());
                order.setShopList(shopList);
            }
        } catch (Exception e) {
            addMessage(model, "错误：" + e.getMessage());
        }

        //所有产品
        List<Product> products = productService.findAllList();

        model.addAttribute(MODEL_ATTR_PRODUCT_LIST, products);
        model.addAttribute(MODEL_ATTR_PAGE, page);
        model.addAttribute(MODEL_ATTR_ORDER, order);
        model.addAttribute(MODEL_ATTR_CAN_SEARCH, canSearch);
        return VIEW_NAME_COMPLAINED_ORDER_LIST;
    }

    /**
     * 我的订单之配件单（待发件 for 业务）
     */
    @RequiresPermissions("sd:order:view")
    @RequestMapping(value = "materialList")
    public String salesMateriallist(OrderSearchModel order, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<Order> page = new Page<>();
        page.setPageSize(DEFAULT_PAGE_SIZE);
        User user = UserUtils.getUser();
        if (!user.isSystemUser()) {
            addMessage(model, "错误：无权限，此功能只开放给后台人员使用。");
            model.addAttribute(MODEL_ATTR_PRODUCT_LIST, Lists.newArrayList());
            model.addAttribute(MODEL_ATTR_PAGE, page);
            model.addAttribute(MODEL_ATTR_ORDER, order);
            model.addAttribute(MODEL_ATTR_CAN_SEARCH, false);
            return VIEW_NAME_WAITING_PARTS_ORDER_LIST;
        }
        Boolean isValide = checkOrderNoAndPhone(order,model,page);
        if(!isValide){
            return VIEW_NAME_WAITING_PARTS_ORDER_LIST;
        }
        Boolean canSearch = true;
        if (user.isSaleman()) {
            order.setSalesId(user.getId());//业务员
            order.setSubUserType(user.getSubFlag());//子账号类型
            List<Long> offlineCustomers = customerService.findIdListByOfflineOrderFlagFromCacheForSD();
            if(!CollectionUtils.isEmpty(offlineCustomers)){
                order.setOfflineCustomerList(offlineCustomers);
            }
        }
        //第一次进入页面时，检查全局加急标记
        if (request.getMethod().equalsIgnoreCase("get")) {
            String urgentFlag = MSDictUtils.getDictSingleValue("OrderUrgentFlag", "0");
            order.setUrgentFlag(Integer.valueOf(urgentFlag));
        }
        //读取客户账户余额
        getCustomerFinanceBalance(order);
        order.setOrderDataLevel(OrderUtils.OrderDataLevel.DETAIL);
        //查询区域
        initSearchArea(order);
        // date
        if (order.getBeginDate() == null) {
            order.setEndDate(DateUtils.getDateEnd(new Date()));
            order.setBeginDate(DateUtils.getDateStart(DateUtils.addMonth(new Date(), -1)));
        } else {
            order.setEndDate(DateUtils.getDateEnd(order.getEndDate()));
        }

        try {
            //状态：所有
            if (order.getStatus() == null || StringUtils.isBlank(order.getStatus().getValue())) {
                order.setStatus(null);
                order.setStatusRange(new IntegerRange(Order.ORDER_STATUS_NEW, Order.ORDER_STATUS_COMPLETED - 1));//处理中
            }
            order.setOrderNo(order.getOrderNo().trim());
            order.setUserName(order.getUserName().trim());
            order.setPhone1(order.getPhone1().trim());
            //order.setAddress(order.getAddress().trim());
            order.setCreator(order.getCreator().trim());
            order.setRemarks(order.getRemarks().trim());
            page = salesOrderService.getWaitingPartsOrderList(new Page<>(request, response, DEFAULT_PAGE_SIZE), order);
            //判断权限
            Subject subject = SecurityUtils.getSubject();
            if(subject.isPermitted("sd:reminder:create")) {
                List<Order> orders = page.getList();
                orderCanReminder(orders);
            }
            //加急
            List<UrgentLevel> urgentLevels = urgentLevelService.findAllList();
            order.setUrgentLevels(urgentLevels);
            //b2b
            if (order.getCustomer() != null && order.getCustomer().getId() > 0 && B2BDataSourceEnum.isDataSource(order.getDataSource())) {
                List<B2bCustomerMap> shopList = b2bCustomerMapService.getShopListByCustomerNew(order.getDataSource(), order.getCustomer().getId());
                order.setShopList(shopList);
            }
        } catch (Exception e) {
            addMessage(model, "错误：" + e.getMessage());
        }

        //所有产品
        List<Product> products = productService.findAllList();

        model.addAttribute(MODEL_ATTR_PRODUCT_LIST, products);
        model.addAttribute(MODEL_ATTR_PAGE, page);
        model.addAttribute(MODEL_ATTR_ORDER, order);
        model.addAttribute(MODEL_ATTR_CAN_SEARCH, canSearch);
        return VIEW_NAME_WAITING_PARTS_ORDER_LIST;
    }


    /**
     * 我的订单之催单列表(业务)
     * 催单未关闭
     * @param order 查询条件
     */
    @RequiresPermissions("sd:order:view")
    @RequestMapping(value = "reminderlist")
    public String salesReminderList(OrderSearchModel order, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<Order> page = new Page<>();
        page.setPageSize(DEFAULT_PAGE_SIZE);
        User user = UserUtils.getUser();
        if (!user.isSystemUser()) {
            addMessage(model, "错误：无权限，此功能只开放给后台人员使用。");
            model.addAttribute(MODEL_ATTR_PRODUCT_LIST, Lists.newArrayList());
            model.addAttribute(MODEL_ATTR_PAGE, page);
            model.addAttribute(MODEL_ATTR_ORDER, order);
            model.addAttribute(MODEL_ATTR_CAN_SEARCH, false);
            return VIEW_NAME_REMINDER_ORDER_LIST;
        }
        Boolean isValide = checkOrderNoAndPhone(order,model,page);
        if(!isValide){
            return VIEW_NAME_REMINDER_ORDER_LIST;
        }
        Boolean canSearch = true;
        if (user.isSaleman()) {
            order.setSalesId(user.getId());//业务员
            order.setSubUserType(user.getSubFlag());//子账号类型
            List<Long> offlineCustomers = customerService.findIdListByOfflineOrderFlagFromCacheForSD();
            if(!CollectionUtils.isEmpty(offlineCustomers)){
                order.setOfflineCustomerList(offlineCustomers);
            }
        }
        //读取客户账户余额
        getCustomerFinanceBalance(order);
        order.setOrderDataLevel(OrderUtils.OrderDataLevel.DETAIL);//从数据库/redis中读取具体的数据内容
        if(order.getBeginDate() == null){
            order.setEndDate(DateUtils.getDateEnd(new Date()));
            order.setBeginDate(DateUtils.getDateStart(DateUtils.addMonth(new Date(), -3)));//3个月
        }else{
            order.setEndDate(DateUtils.getDateEnd(order.getEndDate()));
        }
        //催单日期
        if (order.getCompleteBegin() == null) {
            order.setCompleteEnd(DateUtils.getDateEnd(new Date()));
            order.setCompleteBegin(DateUtils.getDateStart(DateUtils.addMonth(new Date(), -1)));//1个月
        } else {
            order.setCompleteEnd(DateUtils.getDateEnd(order.getCompleteEnd()));
        }
        //查询区域
        initSearchArea(order);
        try {
            order.setOrderNo(order.getOrderNo().trim());
            order.setUserName(order.getUserName().trim());
            order.setPhone1(order.getPhone1().trim());
            order.setAddress(order.getAddress().trim());
            order.setCreator(order.getCreator().trim());
            order.setRemarks(order.getRemarks().trim());
            page = salesOrderService.getReminderOrderLit(new Page<OrderSearchModel>(request, response, DEFAULT_PAGE_SIZE), order);
            //加急
            List<UrgentLevel> urgentLevels = urgentLevelService.findAllList();
            order.setUrgentLevels(urgentLevels);
            //b2b
            if (order.getCustomer() != null && order.getCustomer().getId() > 0 && B2BDataSourceEnum.isDataSource(order.getDataSource())) {
                List<B2bCustomerMap> shopList = b2bCustomerMapService.getShopListByCustomerNew(order.getDataSource(), order.getCustomer().getId());
                order.setShopList(shopList);
            }
        } catch (Exception e) {
            addMessage(model, "错误：" + e.getMessage());
        }

        //所有产品
        List<Product> products = productService.findAllList();
        model.addAttribute(MODEL_ATTR_PRODUCT_LIST, products);
        model.addAttribute(MODEL_ATTR_PAGE, page);
        model.addAttribute(MODEL_ATTR_ORDER, order);
        model.addAttribute(MODEL_ATTR_CAN_SEARCH, canSearch);
        return VIEW_NAME_REMINDER_ORDER_LIST;
    }

    //endregion 列表
}

