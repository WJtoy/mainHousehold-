package com.wolfking.jeesite.modules.finance.sd.web;


import com.google.common.collect.Lists;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.entity.cc.Reminder;
import com.kkl.kklplus.entity.cc.vm.BulkRereminderCheckModel;
import com.kkl.kklplus.entity.cc.vm.ReminderOrderModel;
import com.kkl.kklplus.entity.praise.PraisePageSearchModel;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.finance.sd.service.FiCustomerOrderService;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.CustomerFinance;
import com.wolfking.jeesite.modules.md.entity.Product;
import com.wolfking.jeesite.modules.md.entity.UrgentLevel;
import com.wolfking.jeesite.modules.md.service.CustomerService;
import com.wolfking.jeesite.modules.md.service.ProductService;
import com.wolfking.jeesite.modules.md.service.UrgentLevelService;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderSearchModel;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.cc.service.ReminderService;
import com.wolfking.jeesite.ms.praise.entity.ViewPraiseModel;
import com.wolfking.jeesite.ms.praise.service.CustomerPraiseService;
import com.wolfking.jeesite.ms.providermd.service.MSCustomerService;
import com.wolfking.jeesite.ms.tmall.md.entity.B2bCustomerMap;
import com.wolfking.jeesite.ms.tmall.md.service.B2bCustomerMapService;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 财务部门客户工单Controller
 *
 * @author Ryan
 */
@Controller
@RequestMapping(value = "${adminPath}/sd/finance/customerOrder")
@Slf4j
public class FiCustomerOrderController extends BaseController {

    @Autowired
    public FiCustomerOrderController(ProductService productService,
                                     CustomerService customerService,
                                     UrgentLevelService urgentLevelService,
                                     B2bCustomerMapService b2bCustomerMapService,
                                     FiCustomerOrderService fiCustomerOrderService,
                                     ReminderService reminderService,
                                     MSCustomerService msCustomerService) {
        super();
        this.productService = productService;
        this.customerService = customerService;
        this.urgentLevelService = urgentLevelService;
        this.b2bCustomerMapService = b2bCustomerMapService;
        this.fiCustomerOrderService = fiCustomerOrderService;
        this.reminderService = reminderService;
        this.msCustomerService = msCustomerService;
    }


    @Autowired
    private CustomerPraiseService customerPraiseService;

    private final ProductService productService;
    private final CustomerService customerService;
    private final UrgentLevelService urgentLevelService;
    private final B2bCustomerMapService b2bCustomerMapService;
    private final FiCustomerOrderService fiCustomerOrderService;
    private final ReminderService reminderService;
    private final MSCustomerService msCustomerService;

    private static final String MODEL_ATTR_PAGE = "page";
    private static final String MODEL_ATTR_ORDER = "order";
    private static final String MODEL_ATTR_PRODUCT_LIST = "productList";
    private static final String MODEL_ATTR_CAN_SEARCH = "canSearch";
    private static final String MODEL_REMINDER_FLAG = "reminderFlag";

    private static final int DEFAULT_PAGE_SIZE = 10;

    private static final String VIEW_NAME_PROCESSING_ORDER_LIST = "modules/finance/sd/customerOrder/list";
    private static final String VIEW_NAME_COMPLETED_ORDER_LIST = "modules/finance/sd/customerOrder/finishList";
    private static final String VIEW_NAME_CANCELED_ORDER_LIST = "modules/finance/sd/customerOrder/cancelList";
    private static final String VIEW_NAME_RETURNED_ORDER_LIST = "modules/finance/sd/customerOrder/returnList";
    private static final String VIEW_NAME_ALL_ORDER_LIST = "modules/finance/sd/customerOrder/allList";
    private static final String VIEW_NAME_COMPLAINED_ORDER_LIST = "modules/finance/sd/customerOrder/complainList";
    private static final String VIEW_NAME_WAITING_PARTS_ORDER_LIST = "modules/finance/sd/customerOrder/orderListMaterial";
    private static final String VIEW_NAME_REMINDER_ORDER_LIST = "modules/finance/sd/customerOrder/reminderList";

    private static final String VIEW_NAME_NEW_ALL_ORDER_LIST = "modules/finance/sd/customerOrder/newallList";

    private static final String VIEW_NAME_NEW_PRAISE_List = "modules/finance/sd/customerOrder/praiseList";

    //region 列表

    /**
     * 设置必须的查询条件
     * @param request
     * @param autoSearch 是否进入页面按初始条件自动查询，false:则判断请求是否是post请求
     * @param model
     * @param user  当前帐号
     * @param searchModel   查询条件
     * @param initMonths    初始最小查询时间段(月)
     * @param searchByOrderDateRange by下单日期查询开关
     * @param maxOrderDays   下单最大查询范围(天)
     * @param searchByCompleteDateRange by完成日期查询开关
     * @param maxCompleteDays 完成最大查询范围(天)
     * @param initCompleteMonths 初始最小查询时间段(月)
     */
    private Boolean setSearchModel(HttpServletRequest request,boolean autoSearch,Model model,Page<Order> page,User user,OrderSearchModel searchModel,int initMonths,boolean searchByOrderDateRange ,int maxOrderDays,boolean searchByCompleteDateRange,int maxCompleteDays,int initCompleteMonths) {
        //Map<String,Object> resultMap = Maps.newHashMapWithExpectedSize(1);
        if (searchModel == null) {
            searchModel = new OrderSearchModel();
        }
        //第一次进入页面时，检查全局加急标记
        if (request.getMethod().equalsIgnoreCase("get")) {
            String urgentFlag = MSDictUtils.getDictSingleValue("OrderUrgentFlag", "0");
            searchModel.setUrgentFlag(Integer.valueOf(urgentFlag));
        }

        Boolean canSearch = true;
        Boolean errorFlag = false;
        Integer reminderFlag = 1; //客户是否可以催单
        //检查客户帐号信息
        /*if (user.isCustomer()) {
            if (user.getCustomerAccountProfile() != null && user.getCustomerAccountProfile().getCustomer() != null) {
                //登录用户的客户，防篡改
                searchModel.setCustomer(user.getCustomerAccountProfile().getCustomer());
                if (searchModel.getCustomer() != null && searchModel.getCustomer().getId() != null && searchModel.getCustomer().getId() > 0) {
                    Customer customerFromMS = msCustomerService.get(searchModel.getCustomer().getId());
                    if (customerFromMS != null) {
                        reminderFlag = customerFromMS.getReminderFlag();
                    }
                }
            } else {
                errorFlag = true;
                canSearch = false;
            }
        }*/
        Date now = new Date();
        if(autoSearch || (!autoSearch && request.getMethod().equalsIgnoreCase("post"))) {
           /* if (user.isSaleman()) {
                searchModel.setSalesId(user.getId());
            }*/
            if (errorFlag) {
                model.addAttribute(MODEL_ATTR_PAGE, page);
                model.addAttribute(MODEL_ATTR_ORDER, searchModel);
                //resultMap.put("erroFlag", erroFlag);
                //return resultMap;
                return errorFlag;
            }
            //下单日期
            if (searchByOrderDateRange) {
                if (searchModel.getBeginDate() == null) {
                    searchModel.setEndDate(DateUtils.getDateEnd(now));
                    searchModel.setBeginDate(DateUtils.getDateStart(DateUtils.addMonth(new Date(), 0 - initMonths)));
                    //searchModel.setBeginDate(DateUtils.getStartDayOfMonth(DateUtils.addMonth(now, 0 - initMonths)));
                } else {
                    searchModel.setEndDate(DateUtils.getDateEnd(searchModel.getEndDate()));
                }
                //检查最大时间范围
                if (maxOrderDays > 0) {
                    /*
                    Date maxDate = DateUtils.addDays(searchModel.getBeginDate(), maxOrderDays - 1);
                    maxDate = DateUtils.getDateEnd(maxDate);
                    if (DateUtils.pastDays(maxDate, searchModel.getEndDate()) > 0) {
                        searchModel.setEndDate(maxDate);
                    }*/
                    Date maxDate = DateUtils.addDays(searchModel.getEndDate(), 0 - maxOrderDays);
                    maxDate = DateUtils.getDateStart(maxDate);
                    if(DateUtils.pastDays(searchModel.getBeginDate(),maxDate)>0) {
                        searchModel.setBeginDate(maxDate);
                    }
                }
            }
            //完成日期
            if (searchByCompleteDateRange) {
                boolean isInit = false;
                if (initCompleteMonths>0 && searchModel.getCompleteBegin() == null) {
                    searchModel.setCompleteEnd(DateUtils.getDateEnd(now));
                    searchModel.setCompleteBegin(DateUtils.getDateStart(DateUtils.addMonth(now, 0 - initCompleteMonths)));
                    isInit = true;
                }
                if (!isInit && searchModel.getCompleteEnd() != null) {
                    searchModel.setCompleteEnd(DateUtils.getDateEnd(searchModel.getCompleteEnd()));
                }
                //检查最大时间范围
                if (maxCompleteDays > 0 && searchModel.getCompleteBegin() != null) {
                    Date maxDate = DateUtils.addDays(searchModel.getCompleteBegin(), maxCompleteDays - 1);
                    maxDate = DateUtils.getDateEnd(maxDate);
                    if (searchModel.getCompleteEnd() == null) {
                        searchModel.setCompleteEnd(DateUtils.getDateEnd(now));
                    }
                    if (DateUtils.pastDays(maxDate, searchModel.getCompleteEnd()) > 0) {
                        searchModel.setCompleteEnd(maxDate);
                    }
                }
            }
        }else{
            //下单日期
            if (searchByOrderDateRange) {
                searchModel.setEndDate(new Date());
                searchModel.setBeginDate(DateUtils.getDateStart(DateUtils.addMonth(now, 0 - initMonths)));
            }
        }
        //读取客户账户余额
        getCustomerFinanceBalance(searchModel);
        searchModel.setOrderDataLevel(OrderUtils.OrderDataLevel.FEE);
        //子帐号，只查询自己的单
       /* if (user.getUserType().equals(User.USER_TYPE_SUBCUSTOMER)) {
            searchModel.setCreateBy(user);
        }*/
        searchModel.setOrderNo(searchModel.getOrderNo().trim());
        searchModel.setUserName(searchModel.getUserName().trim());
        searchModel.setPhone1(searchModel.getPhone1().trim());
        searchModel.setPhone2(searchModel.getPhone2().trim());
        searchModel.setAddress(searchModel.getAddress().trim());
        searchModel.setCreator(searchModel.getCreator().trim());
        searchModel.setRemarks(searchModel.getRemarks().trim());

        //加急
        List<UrgentLevel> urgentLevels = urgentLevelService.findAllList();
        searchModel.setUrgentLevels(urgentLevels);
        //b2b
        if (searchModel.getCustomer() != null && searchModel.getCustomer().getId() > 0 && B2BDataSourceEnum.isDataSource(searchModel.getDataSource())) {
            List<B2bCustomerMap> shopList = b2bCustomerMapService.getShopListByCustomerNew(searchModel.getDataSource(), searchModel.getCustomer().getId());
            searchModel.setShopList(shopList);
        }
        model.addAttribute(MODEL_ATTR_PAGE, page);
        model.addAttribute(MODEL_ATTR_ORDER, searchModel);
        model.addAttribute(MODEL_ATTR_CAN_SEARCH, canSearch);
        model.addAttribute(MODEL_REMINDER_FLAG, reminderFlag);
        //resultMap.put("erroFlag",erroFlag);
        //return resultMap;
        return errorFlag;
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
     * 检查订单号，手机号输入
     * @param searchModel
     * @param model
     * @return
     */
    private Boolean checkOrderNoAndPhone(OrderSearchModel searchModel,Model model,Page<Order> page){
        if(searchModel == null){
            return true;
        }
        //检查
        if(StringUtils.isNotBlank(searchModel.getOrderNo())) {
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
        //if (orderSerchType != 1 && StringUtils.isNotBlank(searchModel.getOrderNo())){
        //    addMessage(model, "错误：请输入正确的工单号");
        //    model.addAttribute(MODEL_ATTR_PAGE, page);
        //    model.addAttribute(MODEL_ATTR_ORDER, searchModel);
        //    return false;
        //}
        //检查上门电话
        if (StringUtils.isNotBlank(searchModel.getPhone1())){
            if(searchModel.getIsPhone() != 1){
                addMessage(model, "错误：请输入正确的上门电话");
                model.addAttribute(MODEL_ATTR_PAGE, page);
                model.addAttribute(MODEL_ATTR_ORDER, searchModel);
                return false;
            }
        }
        //检查下单电话
        if (StringUtils.isNotBlank(searchModel.getPhone2())){
            if(searchModel.getIsPhone2() != 1){
                addMessage(model, "错误：请输入正确的下单电话");
                model.addAttribute(MODEL_ATTR_PAGE, page);
                model.addAttribute(MODEL_ATTR_ORDER, searchModel);
                return false;
            }
        }
        return true;
    }

    /**
     * 我的订单之处理中(客户)
     */
    @RequiresPermissions(value = {"sd:order:edit", "sd:order:approvenew","sd:order:view"},logical = Logical.OR )
    @RequestMapping(value = "list")
    public String processingOrderLit(OrderSearchModel order, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<Order> page = new Page<>();
        page.setPageSize(DEFAULT_PAGE_SIZE);
        User user = UserUtils.getUser();
        Boolean erroFlag = setSearchModel(request,true,model,page,user,order,3,true,365,false,0,0);
        if (erroFlag) {
            return VIEW_NAME_PROCESSING_ORDER_LIST;
        }
        Boolean isValide = checkOrderNoAndPhone(order,model,page);
        if(!isValide){
            return VIEW_NAME_PROCESSING_ORDER_LIST;
        }
        try {
            page = fiCustomerOrderService.getProcessingOrderLit(new Page<>(request, response, DEFAULT_PAGE_SIZE), order);
            //判断权限
            Subject subject = SecurityUtils.getSubject();
            if(subject.isPermitted("sd:reminder:create")) {
                List<Order> orders = page.getList();
                orderCanReminder(orders);
            }
            model.addAttribute(MODEL_ATTR_PAGE, page);
        } catch (Exception e) {
            addMessage(model, "错误：" + e.getMessage());
        }
        List<Product> products;
        if (user.isCustomer()) {
            products = productService.getCustomerProductList(user.getCustomerAccountProfile().getCustomer().getId());
        } else {
            products = productService.findAllList();
        }
        model.addAttribute(MODEL_ATTR_PRODUCT_LIST, products);
        return VIEW_NAME_PROCESSING_ORDER_LIST;
    }

    /**
     * 我的订单之完成列表(客户)
     * 订单状态(=80)：Completed
     */
    @RequiresPermissions("sd:order:view")
    @RequestMapping(value = "finishlist")
    public String completedOrderLit(OrderSearchModel order, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<Order> page = new Page<>();
        page.setPageSize(DEFAULT_PAGE_SIZE);
        User user = UserUtils.getUser();
        Boolean erroFlag = setSearchModel(request,true,model,page,user,order,3,true,365,true,365,0);
        if (erroFlag) {
            return VIEW_NAME_COMPLETED_ORDER_LIST;
        }
        Boolean isValide = checkOrderNoAndPhone(order,model,page);
        if(!isValide){
            return VIEW_NAME_COMPLETED_ORDER_LIST;
        }
        try {
            page = fiCustomerOrderService.getCompletedOrderLit(new Page<>(request, response, DEFAULT_PAGE_SIZE), order);
            model.addAttribute(MODEL_ATTR_PAGE, page);
        } catch (Exception e) {
            addMessage(model, "错误：" + e.getMessage());
        }
        List<Product> products;
        /*if (user.isCustomer()) {
            products = productService.getCustomerProductList(user.getCustomerAccountProfile().getCustomer().getId());
        } else {
            products = productService.findAllList();
        }*/
        products = productService.findAllList();
        model.addAttribute(MODEL_ATTR_PRODUCT_LIST, products);
        return VIEW_NAME_COMPLETED_ORDER_LIST;
    }

    /**
     * 我的订单之取消列表(客户)
     * 订单状态(=100)
     */
    @RequiresPermissions("sd:order:view")
    @RequestMapping(value = "cancellist")
    public String canceledOrderLit(OrderSearchModel order, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<Order> page = new Page<>();
        page.setPageSize(DEFAULT_PAGE_SIZE);
        User user = UserUtils.getUser();
        Boolean erroFlag = setSearchModel(request,false,model,page,user,order,1,true,365,true,365,0);
        if (erroFlag) {
            return VIEW_NAME_CANCELED_ORDER_LIST;
        }
        Boolean isValide = checkOrderNoAndPhone(order,model,page);
        if(!isValide){
            return VIEW_NAME_CANCELED_ORDER_LIST;
        }
        if (request.getMethod().equalsIgnoreCase("post")) {
            try {
                page = fiCustomerOrderService.getCanceledOrderLit(new Page<>(request, response, DEFAULT_PAGE_SIZE), order);
                model.addAttribute(MODEL_ATTR_PAGE, page);
            } catch (Exception e) {
                addMessage(model, "错误：" + e.getMessage());
            }
        }
        List<Product> products;
        /*if (user.isCustomer()) {
            products = productService.getCustomerProductList(user.getCustomerAccountProfile().getCustomer().getId());
        } else {
            products = productService.findAllList();
        }*/
        products = productService.findAllList();
        model.addAttribute(MODEL_ATTR_PRODUCT_LIST, products);
        return VIEW_NAME_CANCELED_ORDER_LIST;
    }

    /**
     * 我的订单之退单列表(客户)
     * 订单状态(=90)
     */
    @RequiresPermissions("sd:order:view")
    @RequestMapping(value = "returnlist")
    public String returnedOrderLit(OrderSearchModel order, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<Order> page = new Page<>();
        page.setPageSize(DEFAULT_PAGE_SIZE);
        User user = UserUtils.getUser();
        Boolean erroFlag = setSearchModel(request,true,model,page,user,order,3,true,365,true,365,0);
        if (erroFlag) {
            return VIEW_NAME_RETURNED_ORDER_LIST;
        }
        Boolean isValide = checkOrderNoAndPhone(order,model,page);
        if(!isValide){
            return VIEW_NAME_RETURNED_ORDER_LIST;
        }
        try {
            page = fiCustomerOrderService.getReturnedOrderLit(new Page<>(request, response, DEFAULT_PAGE_SIZE), order);
            //判断权限
            Subject subject = SecurityUtils.getSubject();
            if(subject.isPermitted("sd:reminder:create")) {
                List<Order> orders = page.getList();
                orderCanReminder(orders);
            }
            model.addAttribute(MODEL_ATTR_PAGE, page);
        } catch (Exception e) {
            addMessage(model, "错误：" + ExceptionUtils.getMessage(e));
        }

        List<Product> products;
        /*if (user.isCustomer()) {
            products = productService.getCustomerProductList(user.getCustomerAccountProfile().getCustomer().getId());
        } else {
            //所有产品
            products = productService.findAllList();
        }*/
        products = productService.findAllList();
        model.addAttribute(MODEL_ATTR_PRODUCT_LIST, products);
        return VIEW_NAME_RETURNED_ORDER_LIST;
    }


    /**
     * 我的订单之所有列表(客户)
     */
    @RequiresPermissions("sd:order:view")
    @RequestMapping(value = "alllist")
    public String allOrderList(OrderSearchModel order, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<Order> page = new Page<>();
        page.setPageSize(DEFAULT_PAGE_SIZE);
        User user = UserUtils.getUser();
        Boolean erroFlag = setSearchModel(request,true,model,page,user,order,3,true,365,false,0,0);
        if (erroFlag) {
            return VIEW_NAME_ALL_ORDER_LIST;
        }
        Boolean isValide = checkOrderNoAndPhone(order,model,page);
        if(!isValide){
            return VIEW_NAME_ALL_ORDER_LIST;
        }
        String messageType = request.getParameter("messageType");
        if(request.getMethod().equalsIgnoreCase("post") ||
                (request.getMethod().equalsIgnoreCase("get")  && StringUtils.isNotBlank(messageType))) {
            try {
                page = fiCustomerOrderService.getAllOrderLit(new Page<>(request, response, DEFAULT_PAGE_SIZE), order);
                //判断权限
                Subject subject = SecurityUtils.getSubject();
                if (subject.isPermitted("sd:reminder:create")) {
                    List<Order> orders = page.getList();
                    orderCanReminder(orders);
                }
                model.addAttribute(MODEL_ATTR_PAGE, page);
            } catch (Exception e) {
                addMessage(model, "错误：" + e.getMessage());
            }
        }
        //客户的产品
        List<Product> products;
        /*if (user.isCustomer()) {
            products = productService.getCustomerProductList(user.getCustomerAccountProfile().getCustomer().getId());
        } else {
            products = productService.findAllList();
        }*/
        products = productService.findAllList();
        model.addAttribute(MODEL_ATTR_PRODUCT_LIST, products);
        return VIEW_NAME_ALL_ORDER_LIST;
    }


    /**
     * 我的订单之投诉列表(客户)
     */
    @RequiresPermissions("sd:order:view")
    @RequestMapping(value = "complainlist")
    public String complainedOrderLit(OrderSearchModel order, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<Order> page = new Page<>();
        page.setPageSize(DEFAULT_PAGE_SIZE);
        User user = UserUtils.getUser();
        Boolean erroFlag = setSearchModel(request,true,model,page,user,order,3,true,0,false,0,0);
        if (erroFlag) {
            return VIEW_NAME_COMPLAINED_ORDER_LIST;
        }
        Boolean isValide = checkOrderNoAndPhone(order,model,page);
        if(!isValide){
            return VIEW_NAME_COMPLAINED_ORDER_LIST;
        }
        try {
            page = fiCustomerOrderService.getComplainedOrderLit(new Page<>(request, response, DEFAULT_PAGE_SIZE), order);
            //判断权限
            Subject subject = SecurityUtils.getSubject();
            if(subject.isPermitted("sd:reminder:create")) {
                List<Order> orders = page.getList();
                orderCanReminder(orders);
            }
            model.addAttribute(MODEL_ATTR_PAGE, page);
        } catch (Exception e) {
            addMessage(model, "错误：" + e.getMessage());
        }
        //客户的产品
        List<Product> products;
        /*if (user.isCustomer()) {
            products = productService.getCustomerProductList(user.getCustomerAccountProfile().getCustomer().getId());
        } else {
            //所有产品
            products = productService.findAllList();
        }*/
        products = productService.findAllList();
        model.addAttribute(MODEL_ATTR_PRODUCT_LIST, products);
        return VIEW_NAME_COMPLAINED_ORDER_LIST;
    }

    /**
     * 我的订单之配件单（待发件）
     */
    @RequiresPermissions("sd:order:view")
    @RequestMapping(value = "materialList")
    public String waitingPartsOrderList(OrderSearchModel order, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<Order> page = new Page<>();
        page.setPageSize(DEFAULT_PAGE_SIZE);
        User user = UserUtils.getUser();
        Boolean erroFlag = setSearchModel(request,true,model,page,user,order,3,true,365,false,0,0);
        if (erroFlag) {
            return VIEW_NAME_WAITING_PARTS_ORDER_LIST;
        }
        Boolean isValide = checkOrderNoAndPhone(order,model,page);
        if(!isValide){
            return VIEW_NAME_WAITING_PARTS_ORDER_LIST;
        }
        try {
            page = fiCustomerOrderService.getWaitingPartsOrderList(new Page<>(request, response, DEFAULT_PAGE_SIZE), order);
            //判断权限
            Subject subject = SecurityUtils.getSubject();
            if(subject.isPermitted("sd:reminder:create")) {
                List<Order> orders = page.getList();
                orderCanReminder(orders);
            }
            model.addAttribute(MODEL_ATTR_PAGE, page);
        } catch (Exception e) {
            addMessage(model, "错误：" + e.getMessage());
        }
        List<Product> products;
        /*if (user.isCustomer()) {
            products = productService.getCustomerProductList(user.getCustomerAccountProfile().getCustomer().getId());
        } else {
            //所有产品
            products = productService.findAllList();
        }*/
        products = productService.findAllList();
        model.addAttribute(MODEL_ATTR_PRODUCT_LIST, products);
        return VIEW_NAME_WAITING_PARTS_ORDER_LIST;
    }


    /**
     * 我的订单之催单
     */
    @RequiresPermissions("sd:order:view")
    @RequestMapping(value = "reminderList")
    public String customerReminderList(OrderSearchModel order, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<Order> page = new Page<>();
        page.setPageSize(DEFAULT_PAGE_SIZE);
        User user = UserUtils.getUser();
        Boolean erroFlag = setSearchModel(request,true,model,page,user,order,3,true,365,true,31,1);
        if (erroFlag) {
            return VIEW_NAME_REMINDER_ORDER_LIST;
        }
        Boolean isValide = checkOrderNoAndPhone(order,model,page);
        if(!isValide){
            return VIEW_NAME_REMINDER_ORDER_LIST;
        }
        try {
            page = fiCustomerOrderService.getReminderOrderLit(new Page<>(request, response, DEFAULT_PAGE_SIZE), order);
            model.addAttribute(MODEL_ATTR_PAGE, page);
        } catch (Exception e) {
            addMessage(model, "错误：" + e.getMessage());
        }
        List<Product> products;
        /*if (user.isCustomer()) {
            products = productService.getCustomerProductList(user.getCustomerAccountProfile().getCustomer().getId());
        } else {
            //所有产品
            products = productService.findAllList();
        }*/
        products = productService.findAllList();
        model.addAttribute(MODEL_ATTR_PRODUCT_LIST, products);
        return VIEW_NAME_REMINDER_ORDER_LIST;
    }

    //endregion 列表


    //region new All List

    /**
     * 我的订单之所有列表(客户)
     */
    @RequiresPermissions("sd:order:view")
    @RequestMapping(value = "newalllist")
    public String newAllOrderList(OrderSearchModel order, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<Order> page = new Page<>();
        page.setPageSize(DEFAULT_PAGE_SIZE);
        User user = UserUtils.getUser();
        Boolean erroFlag = setSearchModel(request,true,model,page,user,order,3,true,365,false,0,0);
        if (erroFlag) {
            return VIEW_NAME_NEW_ALL_ORDER_LIST;
        }
        Boolean isValide = checkOrderNoAndPhone(order,model,page);
        if(!isValide){
            return VIEW_NAME_NEW_ALL_ORDER_LIST;
        }
        try {
            page = fiCustomerOrderService.getNewAllOrderLit(new Page<>(request, response, DEFAULT_PAGE_SIZE), order);
            //判断权限
            Subject subject = SecurityUtils.getSubject();
            if(subject.isPermitted("sd:reminder:create")) {
                List<Order> orders = page.getList();
                orderCanReminder(orders);
            }
            model.addAttribute(MODEL_ATTR_PAGE, page);
        } catch (Exception e) {
            addMessage(model, "错误：" + e.getMessage());
        }
        //客户的产品
        List<Product> products;
        /*if (user.isCustomer()) {
            products = productService.getCustomerProductList(user.getCustomerAccountProfile().getCustomer().getId());
        } else {
            products = productService.findAllList();
        }*/
        products = productService.findAllList();
        model.addAttribute(MODEL_ATTR_PRODUCT_LIST, products);
        return VIEW_NAME_NEW_ALL_ORDER_LIST;
    }


    private PraisePageSearchModel setPraiseSerachModel(PraisePageSearchModel praisePageSearchModel,User user,Model model){
        //检查客户帐号信息
        Integer reminderFlag = 1; //客户是否可以催单
        Date date;
        if (StringUtils.isBlank(praisePageSearchModel.getBeginDate())) {
            date = DateUtils.getDateEnd(new Date());
            praisePageSearchModel.setEndDate(DateUtils.formatDate(date,"yyyy-MM-dd"));
            praisePageSearchModel.setEndDt(date.getTime());
            date = DateUtils.getStartDayOfMonth(DateUtils.addMonth(new Date(), -1));
            praisePageSearchModel.setBeginDate(DateUtils.formatDate(date,"yyyy-MM-dd"));
            praisePageSearchModel.setBeginDt(date.getTime());
        } else {
            date = DateUtils.parseDate(praisePageSearchModel.getBeginDate());
            praisePageSearchModel.setBeginDt(date.getTime());
            date = DateUtils.parseDate(praisePageSearchModel.getEndDate());
            date = DateUtils.getDateEnd(date);
            praisePageSearchModel.setEndDt(date.getTime());
        }
        model.addAttribute("reminderFlag",reminderFlag);
        return praisePageSearchModel;
    }

    /**
     * 客服查询待处理好评信息列表
     * @param praisePageSearchModel
     * @param request
     */
    @RequiresPermissions("sd:order:view")
    @RequestMapping(value = "praiseList")
    public String praiseList(PraisePageSearchModel praisePageSearchModel, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<ViewPraiseModel> page = new Page(request, response);
        User user = UserUtils.getUser();
        if (user == null || user.getId() == null) {
            addMessage(model, "错误：登录超时，请退出后重新登录。");
            model.addAttribute("page", page);
            model.addAttribute("praisePageSearchModel", praisePageSearchModel);
            return VIEW_NAME_NEW_PRAISE_List;
        }
        praisePageSearchModel = setPraiseSerachModel(praisePageSearchModel,user,model);
        page = customerPraiseService.waitProcessList(page,praisePageSearchModel);
        model.addAttribute("page", page);
        model.addAttribute("praisePageSearchModel", praisePageSearchModel);
        return VIEW_NAME_NEW_PRAISE_List;
    }


    //endregion new All List

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
        int size = orders.size();
        for (int i = 0; i < size; i++) {
            o = orders.get(i);
            //未完成
            //TODO: APP完工[55]
//            if(o.getOrderCondition().getStatusValue() <= Order.ORDER_STATUS_SERVICED) {
            if(o.getOrderCondition().getStatusValue() <= Order.ORDER_STATUS_APP_COMPLETED) {
                orderModel = ReminderOrderModel.builder()
                        .orderId(o.getId())
                        .quarter(o.getQuarter())
                        .reminderFlag(o.getOrderStatus().getReminderStatus())
                        .createAt(o.getOrderCondition().getCreateDate().getTime())
                        .build();
                orderModelList.add(orderModel);
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

}

