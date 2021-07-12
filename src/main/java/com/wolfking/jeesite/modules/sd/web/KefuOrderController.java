package com.wolfking.jeesite.modules.sd.web;


import com.google.common.collect.Lists;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.config.redis.GsonRedisSerializer;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.Product;
import com.wolfking.jeesite.modules.md.service.ProductService;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.entity.OrderCondition;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderPendingSearchModel;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderSearchModel;
import com.wolfking.jeesite.modules.sd.service.KefuOrderService;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.KefuTypeEnum;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.b2bcenter.md.utils.B2BMDUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

//import com.wolfking.jeesite.common.web.FormToken;

/**
 *
 * 不再使用，最新代码在KefuOrderController里
 * @version 1.0
 * 客服订单Controller
 * @author Ryan
 *
 * @version 1.1
 * @date 2018/09/18
 * @author Ryan
 * 陈明彬确定，客服总监也按区域筛选订单，不做特殊判断和处理
 */
@Controller
@RequestMapping(value = "${adminPath}/sd/order/kefu/")
@Slf4j
public class KefuOrderController extends BaseController {

    private static final String MODEL_ATTR_PAGE = "page";
    private static final String MODEL_ATTR_ORDER = "order";

    @Autowired
    private KefuOrderService kefuOrderService;

    @Resource(name = "gsonRedisSerializer")
    public GsonRedisSerializer gsonRedisSerializer;

    @Autowired
    private ProductService productService;

    //region 订单列表

    /**
     * 设置及初始化查询条件
     * @param user  当前帐号
     * @param searchModel   查询条件
     * @param initMonths    初始最小查询时间段(月)
     * @param searchByOrderDateRange by下单日期查询开关
     * @param maxOrderDays   下单最大查询范围(天)
     * @param searchByCompleteDateRange by完成日期查询开关
     * @param maxCompleteDays 完成最大查询范围(天)
     */
    private OrderSearchModel setSearchModel(User user,OrderSearchModel searchModel,Model model,int initMonths,boolean searchByOrderDateRange ,int maxOrderDays,boolean searchByCompleteDateRange,int maxCompleteDays) {
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
        String checkRegion = kefuOrderService.loadAndCheckUserRegions(searchModel,user);
        if(StringUtils.isNotEmpty(checkRegion)){
            addMessage(model, checkRegion);
            searchModel.setValid(false);
            return searchModel;
        }
        //客服主管
        boolean isServiceSupervisor = user.getRoleEnNames().contains("Customer service supervisor");
        if (searchModel.getStatus() == null || StringUtils.isBlank(searchModel.getStatus().getValue())) {
            searchModel.setStatus(null);
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
            if(maxOrderDays > 0){
                Date maxDate = DateUtils.addDays(searchModel.getBeginDate(),maxOrderDays-1);
                maxDate = DateUtils.getDateEnd(maxDate);
                if(DateUtils.pastDays(maxDate,searchModel.getEndDate())>0){
                    searchModel.setEndDate(maxDate);
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
        //int subQueryUserArea = 1;
        //vip客服查询自己负责的单，by客户+区域+品类
        //1.by 客户，前端客户已按客服筛选了
        if(user.isKefu()) {
            if (user.getSubFlag() == KefuTypeEnum.VIPKefu.getCode()) {
                //vip客服查询自己负责的单，by客户+区域+品类
                searchModel.setCustomerType(1);//指派客户，关联sys_user_customer
                searchModel.setRushType(null);//忽略突击区域
                searchModel.setKefuType(OrderCondition.COMMON_KEFU_TYPE);
            } else if (user.getSubFlag() == KefuTypeEnum.Kefu.getCode()) {
                //普通客服
                searchModel.setCustomerType(0);//不能查询vip客户订单
                searchModel.setRushType(0); //不能查看突击区域订单
                searchModel.setKefuType(OrderCondition.COMMON_KEFU_TYPE); ///大客服
            } else if(user.getSubFlag() == KefuTypeEnum.Rush.getCode()){
                //突击客服，只看自己负责的单
                searchModel.setCustomerType(null);//查询vip客户订单
                searchModel.setRushType(1);//查看突击区域订单
                searchModel.setKefuType(OrderCondition.RUSH_KEFU_TYPE);//突击客服
            }else if(user.getSubFlag() == KefuTypeEnum.AutomaticKefu.getCode()){
                searchModel.setCustomerType(null);//查询所有客户订单
                searchModel.setRushType(0); //不能查看突击区域订单
                searchModel.setKefuType(OrderCondition.AUTOMATIC_KEFU_TYPE);//自动客服
            }else if(user.getSubFlag() == KefuTypeEnum.COMMON_KEFU.getCode()){
                searchModel.setCustomerType(null);//不能查询vip客户订单
                searchModel.setRushType(0); //不能查看突击区域订单
                searchModel.setKefuType(OrderCondition.COMMON_KEFU_TYPE);
            }else{
                //超级客服
                //查询所有客户订单，包含Vip客户
                searchModel.setCustomerType(null); //也可查看Vip客户订单
                searchModel.setRushType(null);//可查看突击区域订单
                searchModel.setKefuType(null);
            }
        }else{
            //其他类型帐号，不限制客户及突击区域订单
            searchModel.setCustomerType(null);
            searchModel.setRushType(null);
            searchModel.setKefuType(null);
        }
        //2.by 区域
        if (isServiceSupervisor) {
            searchModel.setCreateBy(user);//*
        } else if (user.isKefu()) {
            searchModel.setCreateBy(user);//*,只有客服才按帐号筛选
        } else if (user.isInnerAccount()) { //内部帐号
            searchModel.setCreateBy(user);//*
        }
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
     * 待派单列表
     * 包含未接单及未派单订单

    @RequiresPermissions(value =
            {"sd:order:accept", "sd:order:plan", "sd:order:complete",
                    "sd:order:return", "sd:order:grade", "sd:order:feedback"}, logical = Logical.OR)
    @RequestMapping(value = "planinglist")
    public String planingList(OrderSearchModel order, HttpServletRequest request, HttpServletResponse response, Model model) {
        String viewForm = "modules/sd/kefu/planingList";
        Page<Order> page = new Page<Order>();
        User user = UserUtils.getUser();
        //客服主管
        boolean isServiceSupervisor = user.getRoleEnNames().contains("Customer service supervisor");
        order.setOrderDataLevel(OrderUtils.OrderDataLevel.DETAIL);//从数据库/redis中读取具体的数据内容
        //状态：所有
        if (order.getStatus() == null || StringUtils.isBlank(order.getStatus().getValue())) {
            order.setStatus(null);
            order.setStatusRange(new IntegerRange(Order.ORDER_STATUS_APPROVED, Order.ORDER_STATUS_ACCEPTED));//待接单，已接单
        }
        //date
        if (order.getBeginDate() == null) {
            order.setEndDate(DateUtils.getDateEnd(new Date()));
            order.setBeginDate(DateUtils.getStartDayOfMonth(DateUtils.addMonth(new Date(), -1)));
        } else {
            order.setEndDate(DateUtils.getDateEnd(order.getEndDate()));
        }
        //检查电话
        if(StringUtils.isNotBlank(order.getOrderNo())) {
            int orderSerchType = order.getOrderNoSearchType();
            if (orderSerchType != 1) {
                addMessage(model, "错误：请输入正确的订单号");
                model.addAttribute("page", page);
                model.addAttribute("order", order);
                return viewForm;
            } else {
                //检查分片
                try {
                    Date goLiveDate = OrderUtils.getGoLiveDate();
                    String[] quarters = DateUtils.getQuarterRange(goLiveDate, new Date());
                    if (quarters.length == 2) {
                        int start = StringUtils.toInteger(quarters[0]);
                        int end = StringUtils.toInteger(quarters[1]);
                        if (start > 0 && end > 0) {
                            int quarter = StringUtils.toInteger(order.getQuarter());
                            if (quarter < start || quarter > end) {
                                addMessage(model, "错误：请输入正确的订单号,日期超出范围");
                                model.addAttribute("page", page);
                                model.addAttribute("order", order);
                                return viewForm;
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("检查分片错误,orderNo:{}", order.getOrderNo(), e);
                }
            }
        }
        if (StringUtils.isNotBlank(order.getPhone1())){
            if(order.getIsPhone() != 1){
                addMessage(model, "错误：请输入正确的用户电话");
                model.addAttribute("page", page);
                model.addAttribute("order", order);
                return viewForm;
            }
        }
        try {
            int subQueryUserArea = 1;
            //安维帐号，只查询自己负责的单
            //1.by 客户，前端客户已按安维做筛选
            order.setSubQueryUserCustomer(0);//未指派客户，不关联sys_user_customer
            //2.by 区域
            //如果是客服，要按其负责的区域过滤，前端区域选择已经按安维做筛选
            //其余系统帐号，不限定区域
            //如选择的区域是 [区/县]级，则直接查询订单的area_id与传入值相等
            //否则，需要关联sys_area表，根据parent_ids like查询
            if (isServiceSupervisor) {
                //order.setSubQueryUserArea(0);//可查询任何区域
                order.setSubQueryUserArea(1);
                order.setCreateBy(user);//*
            } else if (user.isKefu()) {
                order.setSubQueryUserArea(subQueryUserArea);
                order.setCreateBy(user);//*,只有客服才按帐号筛选
            } else {
                order.setSubQueryUserArea(0);//非客服可查询任何区域
            }

            order.setSearchType("planing");//*
            //查询
            page = kefuOrderService.findKefuPlaningOrderList(new Page<OrderSearchModel>(request, response), order, true);
        } catch (Exception e) {
            addMessage(model, "错误：" + e.getMessage());
        }
        model.addAttribute("page", page);
        model.addAttribute("order", order);
        return viewForm;
    }
    */

    /**
     * 待审核退单列表 (for 客服主管)
     * 客服提出退单申请，由客服主管审核
     */
    @RequiresPermissions("sd:order:approvereturn")
    @RequestMapping(value = "orderReturnApproveList")
    public String orderReturnApproveList(OrderSearchModel order, HttpServletRequest request, HttpServletResponse response, Model model) {
        String viewForm = "modules/sd/kefu/orderReturnApproveList";
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
            page = kefuOrderService.getOrderReturnApproveList(new Page<>(request, response), order);
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
        String viewForm = "modules/sd/kefu/xyyOrderReturnApproveList";
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
            page = kefuOrderService.getOrderReturnApproveList(new Page<>(request, response), order);
        } catch (Exception e) {
            addMessage(model, "错误：" + e.getMessage());
        }
        model.addAttribute(MODEL_ATTR_PAGE, page);
        model.addAttribute(MODEL_ATTR_ORDER, order);
        model.addAttribute("canSearch", true);
        return viewForm;
    }

    //region 审单异常

    /**
     * 设置及初始化查询条件
     * @param user  当前帐号
     * @param searchModel   查询条件
     * @param initMonths    初始最小查询时间段(月)
     * @param maxCompleteDays 完成最大查询范围(天)
     */
    private OrderPendingSearchModel setPendingSearchModel(User user,OrderPendingSearchModel searchModel,Model model,int initMonths,int maxCompleteDays) {
        if (searchModel == null) {
            searchModel = new OrderPendingSearchModel();
        }
        Area area = searchModel.getArea();
        if(area == null){
            area = new Area(0L);
            searchModel.setArea(area);
        }
        if(area.getParent()==null || area.getParent().getId() == null){
            area.setParent(new Area(0L));
        }
        //客服主管
        boolean isServiceSupervisor = user.getRoleEnNames().contains("Customer service supervisor");
        Date now = new Date();
        //完成日期
        if (searchModel.getCompleteBegin() == null) {
            searchModel.setCompleteEnd(DateUtils.getDateEnd(new Date()));
            searchModel.setCompleteBegin(DateUtils.getStartDayOfMonth(DateUtils.addMonth(now, 0 - initMonths)));
        } else {
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

        String checkRegion = kefuOrderService.loadAndCheckUserRegions(searchModel,user);
        if(StringUtils.isNotEmpty(checkRegion)){
            addMessage(model, checkRegion);
            searchModel.setValid(false);
            return searchModel;
        }
        //vip客服查询自己负责的单，by客户+区域+品类
        //1.by 客户，前端客户已按客服筛选了
        if(user.isKefu()) {
            if (user.getSubFlag() == KefuTypeEnum.VIPKefu.getCode()) {
                //vip客服查询自己负责的单，by客户+区域+品类
                //1.by 客户，前端客户已按客服筛选了
                searchModel.setCustomerType(1);//指派客户，关联sys_user_customer
                searchModel.setRushType(null);//忽略突击区域
                searchModel.setKefuType(OrderCondition.COMMON_KEFU_TYPE);
            } else if (user.getSubFlag() == KefuTypeEnum.Kefu.getCode()) {
                //普通客服，不能查询vip客户订单
                searchModel.setCustomerType(0);//关联 sys_user_customer
                searchModel.setRushType(0);//排除突击区域订单
                searchModel.setKefuType(OrderCondition.COMMON_KEFU_TYPE);
            } else if(user.getSubFlag() == KefuTypeEnum.Rush.getCode()){
                //突击客服，只看自己负责的单
                //searchModel.setCustomerType(0);//不能查询vip客户订单
                searchModel.setCustomerType(null);//不能查询vip客户订单
                searchModel.setRushType(1);//查看突击区域订单
                searchModel.setKefuType(OrderCondition.RUSH_KEFU_TYPE);
            } else if(user.getSubFlag() == KefuTypeEnum.AutomaticKefu.getCode()){
                searchModel.setCustomerType(null);//关联 sys_user_customer
                searchModel.setRushType(0);//排除突击区域订单
                searchModel.setKefuType(OrderCondition.AUTOMATIC_KEFU_TYPE);
            }else if(user.getSubFlag() == KefuTypeEnum.COMMON_KEFU.getCode()){
                searchModel.setCustomerType(null);//关联 sys_user_customer
                searchModel.setRushType(0);//排除突击区域订单
                searchModel.setKefuType(OrderCondition.COMMON_KEFU_TYPE);
            }else {//超级客服，查询所有客户订单
                searchModel.setCustomerType(null); //忽略客户过滤条件
                searchModel.setRushType(null);//可查看突击区域订单
                searchModel.setKefuType(null);
            }
        }else{
            //其他类型帐号，不限制客户
            searchModel.setCustomerType(null); //忽略客户过滤条件
            searchModel.setRushType(null);//可查看突击区域订单
            searchModel.setKefuType(null);
        }
        //2.by 区域
        if (isServiceSupervisor) {
            searchModel.setCreateBy(user);//*
        } else if (user.isKefu()) {
            searchModel.setCreateBy(user);//*,只有客服才按帐号筛选
        } else if (user.isInnerAccount()) { //内部帐号
            searchModel.setCreateBy(user);//*
        }
        return searchModel;
    }

    /**
     * 检查订单号，手机号输入
     * @param searchModel
     * @param model
     * @return
     */
    private Boolean checkPendingOrderNoAndPhone(OrderPendingSearchModel searchModel,Model model,Page<Order> page){
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
        if (StringUtils.isNotBlank(searchModel.getServicePhone())){
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
     * 审单异常
     */
    @RequiresPermissions(value ="sd:pending:view")
    @RequestMapping(value = { "exceptionHandlingList"})
    public String exceptionHandlingList(OrderPendingSearchModel order, HttpServletRequest request, HttpServletResponse response, Model model) {
        String viewForm = "modules/sd/kefu/exceptionHandlingList";
        Page<Order> page = new Page<>();
        User user = UserUtils.getUser();
        try {
            order = setPendingSearchModel(user,order,model,1,364);
            if(!order.getValid()){
                model.addAttribute(MODEL_ATTR_PAGE, page);
                model.addAttribute(MODEL_ATTR_ORDER, order);
                model.addAttribute("canSearch", false);
                return viewForm;
            }
            Boolean isValide = checkPendingOrderNoAndPhone(order,model,page);
            if(!isValide){
                model.addAttribute("canSearch", false);
                return viewForm;
            }
            //根据产品名取得产品列表（like）
            List<String> productIds = Lists.newArrayList();
            if(StringUtils.isNoneBlank(order.getProductName())){
                List<Product> products;

                if(order.getCustomer() != null && order.getCustomer().getId() != null){
                    products = productService.getCustomerProductList(order.getCustomer().getId());
                }else{
                    //all products
                    products = productService.findAllList();
                }
                if (products !=null && products.size() > 0){
                    final String productName = order.getProductName();
                    productIds = products.stream().filter(t->t.getName().contains(productName))
                            .map(t->t.getId().toString())
                            .collect(Collectors.toList());
                }
                order.setProductIds(productIds);
            }
            //产品名不存在
            if(StringUtils.isNoneBlank(order.getProductName()) && productIds.size() == 0){
                page.setList(Lists.newArrayList());
                page.setCount(0);
                page.setPageNo(1);
                page.setPageSize(10);
            }else {
                //查询
                page = kefuOrderService.getExceptionHandlingList(new Page<>(request, response), order);
            }
        } catch (Exception e) {
            addMessage(model, "错误："+e.getMessage());
        }
        model.addAttribute("page", page);
        model.addAttribute("order",order);
        return viewForm;
    }

    //endregion 审单异常

    //endregion 订单列表
}

