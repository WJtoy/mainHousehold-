package com.wolfking.jeesite.modules.sd.web;


import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.entity.OrderCondition;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderSearchModel;
import com.wolfking.jeesite.modules.sd.service.KefuOrderListService;
import com.wolfking.jeesite.modules.sd.service.ServiceLeaderOrderService;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.KefuTypeEnum;
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

/**
 * 客服订单Controller
 *
 * @author Ryan
 */
@Controller
@RequestMapping(value = "${adminPath}/sd/order/serviceLeaderNew/")
@Slf4j
public class ServiceLeaderOrderController extends BaseController {

    @Autowired
    private ServiceLeaderOrderService serviceLeaderOrderService;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private KefuOrderListService kefuOrderListService;

    private static final String MODEL_ATTR_PAGE = "page";
    private static final String MODEL_ATTR_ORDER = "order";

    private static final int DEFAULT_PAGE_SIZE = 10;

    private static final String VIEW_NAME_DELAY_ORDER_LIST = "modules/sd/serviceleaderNew/delayList";
    private static final String VIEW_NAME_BROKE_APPOINTMENT_ORDER_LIST = "modules/sd/serviceleaderNew/reservationList";
    private static final String VIEW_NAME_COMPLAINED_ORDER_LIST = "modules/sd/serviceleaderNew/complainList";
    private static final String VIEW_NAME_TRAVEL_ORDER_LIST = "modules/sd/serviceleaderNew/travelList";

    //region 订单列表

    private Boolean checkOrderNoAndPhone(OrderSearchModel searchModel,Model model,Page<Order> page){
        if(searchModel == null){
            return true;
        }
        //检查电话
        int orderSerchType = searchModel.getOrderNoSearchType();
        if (orderSerchType != 1 && StringUtils.isNotBlank(searchModel.getOrderNo())){
            addMessage(model, "错误：请输入正确的订单号码");
            model.addAttribute(MODEL_ATTR_PAGE, page);
            model.addAttribute(MODEL_ATTR_ORDER, searchModel);
            return false;
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
        Area area = searchModel.getArea();
        if(area == null){
            area = new Area(0L);
            searchModel.setArea(area);
        }
        if(area.getParent()==null || area.getParent().getId() == null){
            area.setParent(new Area(0L));
        }
        String checkRegion = kefuOrderListService.loadAndCheckUserRegions(searchModel,user);
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
        //int subQueryUserArea = 1;
        //vip客服查询自己负责的单，by客户+区域+品类
        //1.by 客户，前端客户已按客服筛选了
     /*   if(user.isKefu()) {
            if (user.getSubFlag() == KefuTypeEnum.VIPKefu.getCode()) {
                //vip客服查询自己负责的单，by客户+区域+品类
                searchModel.setCustomerType(1);//指派客户，关联sys_user_customer
                searchModel.setRushType(null);//忽略突击区域
                searchModel.setKefuType(OrderCondition.COMMON_KEFU_TYPE);
            } else if (user.getSubFlag() == KefuTypeEnum.Kefu.getCode()) {
                //普通客服
                searchModel.setCustomerType(0);//不能查询vip客户订单
                searchModel.setRushType(0);//不能查看突击区域订单
                searchModel.setKefuType(OrderCondition.COMMON_KEFU_TYPE);//大客服
            } else if (user.getSubFlag() == KefuTypeEnum.Rush.getCode()){
                //突击客服，只看自己负责的单
                searchModel.setCustomerType(null);//查询vip客户订单
                searchModel.setRushType(1);//查看突击区域订单
                searchModel.setKefuType(OrderCondition.RUSH_KEFU_TYPE);
            }else if(user.getSubFlag() == KefuTypeEnum.AutomaticKefu.getCode()){
                searchModel.setCustomerType(null);//查询所有客户
                searchModel.setRushType(0);//不能查看突击区域订单
                searchModel.setKefuType(OrderCondition.AUTOMATIC_KEFU_TYPE);//自动客服
            }else if(user.getSubFlag() == KefuTypeEnum.COMMON_KEFU.getCode()){
                searchModel.setCustomerType(null);//查询所有客户
                searchModel.setRushType(0);//不能查看突击区域订单
                searchModel.setKefuType(OrderCondition.COMMON_KEFU_TYPE);
            }else{
                //超级客服，查询所有客户订单，包含Vip客户
                searchModel.setCustomerType(null); //可查看Vip客户订单
                searchModel.setRushType(null);//可查看突击区域订单
                searchModel.setKefuType(null);
            }
        }else{
            //其他类型帐号，不限制客户及突击区域订单
            searchModel.setCustomerType(null);
            searchModel.setRushType(null);
            searchModel.setKefuType(null);
        }*/
        //vip客服查询自己负责的单，by客户+区域+品类
        //1.by 客户，前端客户已按客服筛选了
        if(user.isKefu()) {
            KefuTypeEnum kefuTypeEnum = KefuTypeEnum.fromCode(user.getSubFlag());
            if(kefuTypeEnum!=null){
                searchModel.setCustomerType(kefuTypeEnum.getCustomerType());
                searchModel.setKefuType(kefuTypeEnum.getKefuType());
            }else{
                addMessage(model, "错误:读取客服类型错误");
                searchModel.setValid(false);
                return searchModel;
            }
        }else{
            //其他类型帐号，不限制客户及突击区域订单
            searchModel.setCustomerType(null);
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
     * 异常订单-爽约列表
     * 预约时间两次及以上的工单
     */
    @RequiresPermissions(value = "sd:order:serviceleader")
    @RequestMapping(value = "reservationlist")
    public String reservationList(OrderSearchModel order, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<Order> page = new Page<>();
        User user = UserUtils.getUser();
        order.setOrderDataLevel(OrderUtils.OrderDataLevel.CONDITION);//从数据库/redis中读取具体的数据内容
        order = setSearchModel(user,order,model,3,true,365,false,0);
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
            page = serviceLeaderOrderService.getBrokeAppointmentOrderlist(new Page<>(request, response), order);
        }catch (Exception e){
            addMessage(model, "错误：" + e.getMessage());
        }
        model.addAttribute(MODEL_ATTR_PAGE, page);
        model.addAttribute(MODEL_ATTR_ORDER, order);
        return VIEW_NAME_BROKE_APPOINTMENT_ORDER_LIST;
    }

    //endregion 订单列表
}

