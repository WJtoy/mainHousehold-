/**
 * Copyright &copy; 2012-2013 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.wolfking.jeesite.modules.sd.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.config.Global;
import com.wolfking.jeesite.common.utils.*;
import com.wolfking.jeesite.modules.md.entity.*;
import com.wolfking.jeesite.modules.md.service.CustomerProductCompletePicService;
import com.wolfking.jeesite.modules.md.service.ProductCompletePicService;
import com.wolfking.jeesite.modules.md.service.ServiceTypeService;
import com.wolfking.jeesite.modules.md.utils.ProductCompletedPicItemAdapter;
import com.wolfking.jeesite.modules.sd.dao.OrderDao;
import com.wolfking.jeesite.modules.sd.entity.*;
import com.wolfking.jeesite.modules.sd.entity.viewModel.CreateOrderModel;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderItemModel;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.VisibilityFlagEnum;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 订单工具类
 *
 * @author Ryan Lu
 * @version 2013-5-29
 */
public class OrderUtils {

    private static OrderDao orderDao = SpringContextHolder.getBean(OrderDao.class);

    private static ServiceTypeService serviceTypeService = SpringContextHolder.getBean(ServiceTypeService.class);

    //private static OrderService orderService = SpringContextHolder.getBean(OrderService.class);

    private static ProductCompletePicService productCompletePicService = SpringContextHolder.getBean(ProductCompletePicService.class);
    private static CustomerProductCompletePicService customerProductCompletePicService = SpringContextHolder.getBean(CustomerProductCompletePicService.class);

    public static final String CACHE_SERVICETYPE = "customer";
    public static final String ORDER_CREATE_COOKIE_KEY = "tmp_order_cookie";
    public static final String ORDER_CREATE_V2_COOKIE_KEY = "tmp_order_v2_cookie";
    public static final String ORDER_EDIT_COOKIE_KEY = "edit_order_cookie";
    public static final String ORDER_EDIT_V2_COOKIE_KEY = "edit_order_v2_cookie";
    public static final Long TMP_ORDER_EXPIRED = 30 * 60l;//临时订单缓存有效期，半个小时
    public static final Long ORDER_EXPIRED = 93600L;//2*24*60*60l;//未完成订单缓存2天
    public static final Long ORDER_LOCK_EXPIRED = 60l;//锁定时间
    public static final long REDIS_CREATE_LOG_EXPIRED = 7 * 24 * 60 * 60;//下单log，保存在redis中，便于核对

    //public static final String ORDERDETAIL_MATERIALAPPLYKEY = "orderDetail_materialApply";
    //public static final String ORDERDETAIL_MATERIALRETURNKEY = "orderDetail_materialReturn";
    // 业务取消订单时，如已派单，需发送app通知安维
    // 样例: 张三 （K2019120315685）用户的工单商家已经取消，如有疑问请联系客服0757-2923XXXX
    public static final String SALE_CANCEL_APP_NOTICE_TEMPLATE = "%s (%s) 用户的工单商家已经取消，如有疑问请联系客服%s";
    // 网点时效费用计算规则
    public static final String TIMELINE_FEE_LOGIC = "timeline_charge_logic";
    public static final String SYNC_CUSTOMER_CHARGE_DICT="customer_auto_remotecharge_category";
    public static final String LIMIT_REMOTECHARGE_CATEGORY_DICT = "limit_remotecharge_category";

    //读取订单内容分级
    public enum OrderDataLevel {
        //订单单头
        HEAD,
        //查询条件
        CONDITION,
        //财务
        FEE,
        //状态
        STATUS,
        //实际服务
        DETAIL,
        //日志
        LOG
    }

    /**
     * 自动同步加的应收费用
     */
    public enum SyncCustomerCharge {
        //远程费
        TRAVEL,
        //其他费用
        OTHER
    }

    /**
     * 完工项目类型
     */
    public enum OrderTypeEnum {
        TEST(0,"检测"),
        INSTALL(1, "安装"),
        REPAIRE(2, "维修"),
        BACK(3,"退货"),
        EXCHANGE(4,"换货");

        private int id;
        private String name;
        private Class<?> clazz;

        OrderTypeEnum(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    private static Set<Integer> ORDER_PROCESS_LOG_STATUS_FLAG_SET_KEFU = Sets.newHashSet(
            OrderProcessLog.OPL_SF_NOT_CHANGE_STATUS,
            OrderProcessLog.OPL_SF_CHANGED_STATUS,
            OrderProcessLog.OPL_SF_PENDDING,
            OrderProcessLog.OPL_SF_TRACKING,
            OrderProcessLog.OPL_SF_PENDINGED);

    private static Set<Integer> ORDER_PROCESS_LOG_STATUS_FLAG_SET_CUSTOMER = Sets.newHashSet(
            OrderProcessLog.OPL_SF_CHANGED_STATUS,
            OrderProcessLog.OPL_SF_PENDDING,
            OrderProcessLog.OPL_SF_PENDINGED);

    private static Set<Integer> ORDER_PROCESS_LOG_CLOSE_FLAG_SET_SERVICE_POINT = Sets.newHashSet(0, 2);

    /**
     * 将订单开立视图模型转换层数据模型
     * 有部分属性未转换，使用时需注意，尤其是更新操作
     *
     * @param order
     * @return
     */
    public static Order toOrder(CreateOrderModel order) {
        Order model = new Order();
        if (order == null) {
            return model;
        }
        model.setQuarter(order.getQuarter());
        if (order.getId() != null) {
            model.setId(order.getId());
        }

        model.setCreateBy(order.getCreateBy());// 创建者
        model.setCreateDate(order.getCreateDate());// 创建日期
        model.setOrderNo(order.getOrderNo());
        model.setWorkCardId(StringUtils.toString(order.getB2bOrderNo()));//第三方单号 2018/12/19
        String parentBizOrderId = StringUtils.isNotBlank(order.getParentBizOrderId()) ? order.getParentBizOrderId() : StringUtils.toString(order.getB2bOrderNo());
        model.setParentBizOrderId(parentBizOrderId);//第三方单号 2018/12/19
        model.setDataSource(order.getDataSource());//数据源ID 2019/6/11
        model.setB2bShop(order.getB2bShop());//店铺
        if(order.getOrderChannel()<=0){
            order.setOrderChannel(1);
        }
        model.setOrderChannel(new Dict(order.getOrderChannel(),""));//销售渠道 2020-04-27
        model.setOrderType(MSDictUtils.getDictByValue(String.valueOf(Order.ORDER_ORDERTYPE_DSXD), "order_type"));//切换为微服务
        model.setTotalQty(order.getTotalQty());
        String description = StringEscapeUtils.unescapeHtml4(order.getDescription())
                .replace("\"", "")
                .replace(":", "|")
                .replace("http|", "http:")
                .replace("https|", "https:")
                .replace("\\\\", "")
                .replace("\\", "");
        model.setDescription(description);
        model.setRepeateNo(order.getRepeateNo().trim());

        //地理信息表 2019-04-15
        OrderLocation location = new OrderLocation(order.getId(),order.getQuarter());
        location.setArea(order.getArea());//区县
        location.setLongitude(order.getLongitude()); //经度
        location.setLatitude(order.getLatitude()); //维度
        model.setOrderLocation(location);

        //Status
        OrderStatus ostatus = new OrderStatus();
        ostatus.setQuarter(model.getQuarter());
        ostatus.setOrderId(order.getId());
        ostatus.setUrgentDate(order.getCreateDate());
        model.setOrderStatus(ostatus);

        //Condition
        OrderCondition condition = new OrderCondition();
        condition.setQuarter(model.getQuarter());
        condition.setOrderId(order.getId());
        condition.setQuarter(order.getQuarter());
        condition.setOrderNo(order.getOrderNo());

        condition.setUserName(order.getUserName());
        condition.setPhone1(order.getPhone1());
        condition.setServicePhone(StringUtils.isBlank(order.getPhone1()) ? order.getPhone2() : order.getPhone1());
        condition.setPhone2(order.getPhone2().trim());
        condition.setAddress(StringEscapeUtils.unescapeHtml4(order.getAddress().replace("null", "")).replace("\"", "").replace(":", "|"));//详细地址
        condition.setServiceAddress(StringEscapeUtils.unescapeHtml4(order.getAddress().replace("null", "")).replace("\"", "").replace(":", "|"));
        condition.setDelFlag(0);
        condition.setCreateDate(order.getCreateDate());
        condition.setCreateBy(order.getCreateBy());
        condition.setCustomerOwner(order.getCustomerOwner());//客户负责人 2018/08/03

        condition.setTotalQty(order.getTotalQty());
        condition.setCustomer(order.getCustomer());
        condition.setArea(order.getArea());
        condition.setSubArea(order.getSubArea());
        condition.setStatus(MSDictUtils.getDictByValue(String.valueOf(order.getStatus()), "order_status"));//切换为微服务
        UrgentLevel urgentLevel = order.getUrgentLevel() == null ? new UrgentLevel(0l, "不加急") : order.getUrgentLevel();
        condition.setUrgentLevel(urgentLevel);
        model.setOrderCondition(condition);

        //fee
        OrderFee fee = new OrderFee();
        fee.setOrderId(order.getId());
        fee.setCustomerUrgentCharge(order.getCustomerUrgentCharge());
        fee.setEngineerUrgentCharge(order.getEngineerUrgentCharge());
        //冻结费用=预计服务费(应收)+加急费(应收)
        fee.setExpectCharge(order.getExpectCharge() + order.getCustomerUrgentCharge());
        fee.setBlockedCharge(order.getBlockedCharge());
        fee.setOrderPaymentType(order.getOrderPaymentType());
        fee.setQuarter(model.getQuarter());

        // 安维
        fee.setEngineerPaymentType(new Dict("0", ""));

        model.setOrderFee(fee);
        //items
        for (OrderItemModel item : order.getItems()) {
            if (!item.getFlag().equalsIgnoreCase("del")) {
                OrderItem m = toOrderItem(item);
                m.setQuarter(model.getQuarter());
                m.setOrderId(order.getId());
                model.getItems().add(m);
            }
        }

        return model;
    }


    public static OrderItem toOrderItem(OrderItemModel item) {
        OrderItem model = new OrderItem();
        if (item == null) {
            return model;
        }
        model.setQuarter(item.getQuarter());
        model.setId(item.getId());
        model.setItemNo(item.getItemNo());
        model.setBrand(item.getBrand() == null ? "" : item.getBrand());// 品牌
        model.setProduct(item.getProduct());
        model.setProductSpec(item.getProductSpec());
        model.setServiceType(item.getServiceType());
        model.setStandPrice(item.getStandPrice());
        model.setDiscountPrice(item.getDiscountPrice());
        model.setQty(item.getQty());
        model.setCharge(item.getCharge());
        model.setBlockedCharge(item.getBlockedCharge());
        model.setServiceType(item.getServiceType());
        model.setExpressCompany(item.getExpressCompany());
        model.setExpressNo(item.getExpressNo());
        model.setRemarks(item.getRemarks() == null ? "" : item.getRemarks());// 备注
        model.setCreateBy(item.getCreateBy());// 创建者
        model.setCreateDate(item.getCreateDate());// 创建日期
        model.setUpdateBy(item.getUpdateBy());//  更新者
        model.setUpdateDate(item.getUpdateDate());// 更新日期
        model.setDelFlag(item.getDelFlag());// 删除标记（0：正常；1：删除；2：审核）
        model.setB2bProductCode(item.getB2bProductCode());/* orderItem增加B2B产品编码 */
        //灯饰下单
        model.setProductType(item.getProductType());
        model.setProductTypeItem(item.getProductTypeItem());
        model.setPics(item.getPics());

        return model;
    }

    /**
     * 将订单转为订单修改视图
     *
     * @param order
     * @return
     */
    public static CreateOrderModel toCreateOrderModel(Order order) {
        CreateOrderModel model = new CreateOrderModel();
        if (order == null) {
            return model;
        }
        model.setQuarter(order.getQuarter());//*
        model.setId(order.getId());
        model.setOrderNo(order.getOrderNo());
        model.setB2bOrderNo(order.getWorkCardId());//第三方单号 2018/12/19
        model.setParentBizOrderId(order.getParentBizOrderId());
        model.setDataSource(order.getDataSource());
        model.setB2bShop(order.getB2bShop());
        if(order.getOrderChannel() != null) {
            model.setOrderChannel(order.getOrderChannel().getIntValue());
        }
        model.setTotalQty(order.getTotalQty());
        model.setDescription(order.getDescription());

        //Condition
        OrderCondition condition = order.getOrderCondition();
        model.setVersion(condition.getVersion());
        model.setUserName(condition.getUserName());
        model.setPhone1(condition.getPhone1());
        model.setServicePhone(condition.getServicePhone());
        model.setPhone2(condition.getPhone2());
        model.setAddress(condition.getAddress());//详细地址
        model.setServiceAddress(condition.getAddress());
        model.setCustomer(condition.getCustomer());
        model.setArea(condition.getArea());
        model.setSubArea(condition.getSubArea()); //获取4级区域  // 2019-5-21
        model.setUrgentLevel(condition.getUrgentLevel());
        if (condition.getUrgentLevel() == null) {
            model.setUrgentLevel(new UrgentLevel(0l, "不加急"));
            model.setUrgentFlag(0);
        } else if (condition.getUrgentLevel().getId() == 0) {
            model.setUrgentFlag(0);
        } else {
            model.setUrgentFlag(1);
        }
        //品类
        model.setCategory(new ProductCategory(condition.getProductCategoryId()==null?0L:condition.getProductCategoryId()));
        //Fee
        OrderFee fee = order.getOrderFee();
        model.setOrderPaymentType(fee.getOrderPaymentType());
        model.setExpectCharge(fee.getExpectCharge() - fee.getCustomerUrgentCharge());//*
        model.setBlockedCharge(fee.getBlockedCharge());
        model.setTotalQty(order.getTotalQty());
        model.setCustomerUrgentCharge(fee.getCustomerUrgentCharge());
        model.setEngineerUrgentCharge(fee.getEngineerUrgentCharge());

        //items
        for (OrderItem item : order.getItems()) {
            OrderItemModel m = toOrderItemModel(item);
            model.getItems().add(m);
        }

        model.setCreateBy(order.getCreateBy());// 创建者
        model.setCreateDate(order.getCreateDate());// 创建日期
        model.setUpdateDate(condition.getUpdateDate());

        return model;
    }


    public static OrderItemModel toOrderItemModel(OrderItem item) {
        OrderItemModel model = new OrderItemModel();
        if (item == null) {
            return model;
        }
        model.setQuarter(item.getQuarter());
        model.setId(item.getId());
        model.setTmpId(String.valueOf(item.getId()));
        model.setItemNo(item.getItemNo());
        model.setBrand(item.getBrand() == null ? "" : item.getBrand());// 品牌
        model.setProduct(item.getProduct());
        model.setB2bProductCode(item.getB2bProductCode());/* orderItem增加B2B产品编码 */
        model.setProductSpec(item.getProductSpec());
        model.setServiceType(item.getServiceType());
        model.setStandPrice(item.getStandPrice());
        model.setDiscountPrice(item.getDiscountPrice());
        model.setQty(item.getQty());
        model.setCharge(item.getCharge());
        model.setBlockedCharge(item.getBlockedCharge());
        model.setServiceType(item.getServiceType());
        model.setExpressCompany(item.getExpressCompany());
        model.setExpressNo(item.getExpressNo());
        model.setRemarks(item.getRemarks() == null ? "" : item.getRemarks());// 备注
        model.setDelFlag(item.getDelFlag());// 删除标记（0：正常；1：删除；2：审核）
        //灯饰下单
        model.setPics(item.getPics());
        model.setProductType(item.getProductType());
        model.setProductTypeItem(item.getProductTypeItem());

        return model;
    }

    //region 订单判断

    /**
     * 客户是否可以取消 ,派单后就不能取消了
     */
    public static boolean canCanceled(Integer status) {
        return status.intValue() <= Order.ORDER_STATUS_ACCEPTED.intValue();
    }

    /**
     * 是否可以退单 上门服务后也可退单
     */
    public static boolean canReturn(Integer status) {
        //TODO: APP完工[55]
//        return status.intValue() <= Order.ORDER_STATUS_SERVICED.intValue();
        return status.intValue() <= Order.ORDER_STATUS_APP_COMPLETED.intValue();
    }

    /**
     * 是否可以设置停滞原因
     * 派单~完成前(旧)
     * 接单 ~ 完成(2018/03/23)
     2019/08/29 ryan
    public static boolean canPendingType(Integer status) {
        return status.intValue() >= Order.ORDER_STATUS_ACCEPTED.intValue()
                && status.intValue() < Order.ORDER_STATUS_RETURNING.intValue();
    }*/

    /**
     * 订单是否完成，包含完成、已退单、已取消
     2019/08/29 ryan
    public static boolean isClosed(Integer status) {
        return status.intValue() >= Order.ORDER_STATUS_COMPLETED.intValue();
    }*/

    /**
     * 是否客户可以修改订单
     * 客服派单前可修改
     */
    public static boolean canEdit(Integer status) {
        return status.intValue() < Order.ORDER_STATUS_PLANNED.intValue();
    }

    /**
     * 主账号是否可以审核订单
     2019/08/29 ryan
    public static boolean canApproved(Integer status) {
        return status.intValue() <= Order.ORDER_STATUS_NEW.intValue();
    }*/
    /**
     * 主账号是否可以审核退单
     2019/08/29 ryan
    public static boolean canApproveReturn(Integer status) {
        return status.intValue() == Order.ORDER_STATUS_RETURNING.intValue();
    }*/

    /**
     * 是否客户可以反馈问题
     2019/08/29 ryan
    public static boolean canFeedback() {
        return true;
    }*/
    /**
     * 是否可以接单
     * 订单状态为Accepted-等待接单 或 订单状态为New-下单，但离下单时间已超过设定的时间，视为自动审核 --cancel
     2019/08/29 ryan
    public static boolean canAccept(Integer status) {
        return status.intValue() == Order.ORDER_STATUS_APPROVED.intValue();
    }*/

    /**
     * 是否可以派单 已派单可重新派单，或已上门服务之后"二"次上门前可重新派单给新安维人员
     * 订单审核后,还未完成期间都可派单
     2019/08/29 ryan
    public static boolean canPlanOrder(Integer status) {
        return status.intValue() >= Order.ORDER_STATUS_APPROVED.intValue()
                && status.intValue() < Order.ORDER_STATUS_RETURNING.intValue();
    }*/


    /**
     * 是否可以上门服务
     2019/08/29 ryan
    public static boolean canService(Integer status, Integer pendingFlag) {
        return status.intValue() >= Order.ORDER_STATUS_PLANNED.intValue()
                && status.intValue() <= Order.ORDER_STATUS_SERVICED.intValue()
                && pendingFlag.intValue() == 1;
    }*/

    /**
     * 是否可以评价安维人员 已评价，不允许修改;有上门服务项目才能客评
     2019/08/29 ryan
    public static boolean canGrade(Integer status, Integer appAbnormalyFlag, Integer gradeFlag, Integer serviceTimes) {
        return status.intValue() == Order.ORDER_STATUS_SERVICED.intValue()
                && appAbnormalyFlag.intValue() != 1
                && gradeFlag.intValue() != 1
                && serviceTimes.intValue() > 0;
    }*/

    /**
     * 是否可以录入进度跟踪
     2019/08/29 ryan
    public static boolean canTracking(Integer status) {
        return status.intValue() >= Order.ORDER_STATUS_APPROVED.intValue();
    }
    */
    /**
     * 是否可以录入用户投诉
     2019/08/29 ryan
    public static boolean canComplain(Integer status) {
        return status.intValue() >= Order.ORDER_STATUS_APPROVED.intValue();
    }
    */
    //endregion

    /**
     * 添加订单项目计价方法(不计算产品冻结金额)
     * 循环计价，取最高价
     */
    public static void rechargeOrder(List<OrderItemModel> list) {
        if (list == null || list.size() == 0) {
            return;
        }
        //本次上门，删除标记不等于1
        List<OrderItemModel> items;
        OrderItemModel m;
        int size;
        //1.厂商,先去标准价最高，标准价相同，取折扣价最低的
        Double cprice = 0.0;
        items = list.stream()
                .filter(t -> t.getDelFlag().intValue() != 1)
                .sorted(Comparator.comparingDouble(OrderItemModel::getStandPrice).reversed()
                        .thenComparingDouble(OrderItemModel::getDiscountPrice))
                .collect(Collectors.toList());
        if (items.size() == 0) {
            return;
        }
        size = items.size();
        for (int i = 0; i < size; i++) {
            m = items.get(i);
            if (i == 0) {
                m.setCharge(m.getStandPrice() + m.getDiscountPrice() * (m.getQty() - 1));
            } else {
                m.setCharge(m.getDiscountPrice() * m.getQty());
            }
            cprice = cprice + m.getCharge();
        }

    }

    public static void rechargeOrder2(List<? extends OrderItem> list) {
        if (list == null || list.size() == 0) {
            return;
        }
        //本次上门，删除标记不等于1
        List<OrderItem> items;
        OrderItem m;
        int size;
        //1.厂商,先去标准价最高，标准价相同，取折扣价最低的
        Double cprice = 0.0;
        items = list.stream()
                .filter(t -> t.getDelFlag().intValue() != 1)
                .sorted(Comparator.comparingDouble(OrderItem::getStandPrice).reversed()
                        .thenComparingDouble(OrderItem::getDiscountPrice))
                .collect(Collectors.toList());
        if (items.size() == 0) {
            return;
        }
        size = items.size();
        for (int i = 0; i < size; i++) {
            m = items.get(i);
            if (i == 0) {
                m.setCharge(m.getStandPrice() + m.getDiscountPrice() * (m.getQty() - 1));
            } else {
                m.setCharge(m.getDiscountPrice() * m.getQty());
            }
            cprice = cprice + m.getCharge();
        }

    }

    /**
     * 读取有效服务类型列表
     *
     * @return
     */
    public static List<ServiceType> getServiceTypes() {
        //mark on 2019-10-12
        //return serviceTypeService.findAllList();
        //调用微服务 只返回 id 跟服务类型名称 start on 2019-10-12
        return serviceTypeService.findAllListIdsAndNames();
    }

    /**
     * 从缓存中读取订单分片
     *
     * @param orderId
     * @return
    2019/08/29 ryan
    public static String getOrderQuarterFromCache(Long orderId) {
        return orderService.getOrderQuarterFromCache(orderId);
    }*/
    /**
     * 获得系统上线日期
     *
     * @return
     */
    public static Date getGoLiveDate() {
        Date goLiveDate = null;
        //from config file
        String date = Global.getConfig("GoLiveDate");
        if (StringUtils.isNotBlank(date)) {
            goLiveDate = DateUtils.parseDate(date);
        }
        if (goLiveDate != null) {
            return goLiveDate;
        }
        //from micoService
        Dict dict = MSDictUtils.getDictByValue("GoLiveDate", "GoLiveDate");
        if (dict == null) {
            return DateUtils.getDate(2018, 1, 1);
        }
        goLiveDate = DateUtils.parseDate(dict.getLabel());
        if (goLiveDate != null) {
            return goLiveDate;
        }
        return DateUtils.getDate(2018, 1, 1);
    }

    /* 2019/08/29 ryan
    public static Date getNewPicModuleGoLiveDate() {
        Date goLiveDate = null;
        String date = Global.getConfig("NewPicModuleGoLiveDate");
        if (StringUtils.isNotBlank(date)) {
            goLiveDate = DateUtils.parseDate(date);
        }
        if (goLiveDate == null) {
            goLiveDate = DateUtils.getDate(2018, 10, 1);
        }
        return goLiveDate;
    }*/

    /**
     * 是否是新单
     *
     * @return 0 - 不是新单、1 - 是新单
     */
    public static int isNewOrder(String orderNo) {
        return 1;
//        if (StringUtils.isBlank(orderNo) && orderNo.length() != ORDER_NO_LENGTH) {
////            throw new IllegalArgumentException("无效的工单编码");
//            return 0;
//        }
//        int year = com.wolfking.jeesite.common.utils.StringUtils.toInteger(orderNo.substring(1, 5));
//        int month = com.wolfking.jeesite.common.utils.StringUtils.toInteger(orderNo.substring(5, 7));
//        int day = com.wolfking.jeesite.common.utils.StringUtils.toInteger(orderNo.substring(7, 9));
//        Date orderCreateDate = DateUtils.getDate(year, month, day);
//        return orderCreateDate.getTime() >= getNewPicModuleGoLiveDate().getTime() ? 1 : 0;
    }

    /**
     * 获得系统上线分片
     *
     * @return
    2019/08/29 ryan
    public static String getGoLiveQuarter() {
        Date goLiveDate = getGoLiveDate();
        if (goLiveDate == null) {
            return "20181";
        } else {
            return QuarterUtils.getSeasonQuarter(goLiveDate);
        }
    }*/

    /**
     * 根据传入日期获得分片的开始日期和结束日期
     *
     * @param startDate   开始日期
     * @param endDate     结束日期
     * @param aheadMonths 提前多少月(正数，为负代表向后延迟)
     * @param aheadDays   提前多少天(正数)
     * @return
     */
    public static Date[] getQuarterDates(Date startDate, Date endDate, Integer aheadMonths, Integer aheadDays) {
        Date quarterSDate = getGoLiveDate();
        if (startDate == null) {
            startDate = new Date();
        }
        if (aheadMonths != null && aheadMonths != 0) {
            startDate = DateUtils.addMonth(startDate, 0 - aheadMonths);
        }
        if (aheadMonths != null && aheadDays != 0) {
            startDate = DateUtils.addDays(startDate, 0 - aheadDays);
        }
        if (startDate != null && quarterSDate.getTime() < startDate.getTime()) {
            quarterSDate = startDate;
        }
        return new Date[]{quarterSDate, endDate == null ? new Date() : endDate};
    }

    /**
     * 筛选出派单给指定网点的时间区间内所产生的工单日志，另外还包括工单在完成状态(80)下所产生的日志
     */
    public static List<OrderProcessLog> filterServicePointOrderProcessLog(List<OrderProcessLog> logList, List<OrderPlan> orderPlanList, Long servicePointId) {
        List<OrderProcessLog> list = Lists.newArrayList();
        if (logList != null && logList.size() > 0
                && orderPlanList != null && orderPlanList.size() > 0
                && servicePointId != null && servicePointId > 0) {
            List<LongTwoTuple> timePointList = Lists.newArrayList();
            for (OrderPlan plan : orderPlanList) {
                if (plan.getCreateDate() != null) {
                    timePointList.add(new LongTwoTuple(plan.getServicePoint().getId(), plan.getCreateDate().getTime()));
                }
                if (plan.getUpdateDate() != null) {
                    timePointList.add(new LongTwoTuple(plan.getServicePoint().getId(), plan.getUpdateDate().getTime()));
                }
            }
            timePointList = timePointList.stream().sorted(Comparator.comparing(LongTwoTuple::getBElement)).collect(Collectors.toList());
            LongTwoTuple timePoint;
            LongThreeTuple timeRange;
            Long endTimestamp = DateUtils.addDays(new Date(), 1).getTime();
            Long cElement;
            List<LongThreeTuple> timeRangeList = Lists.newArrayList();
            if (timePointList.size() == 1) {
                timePoint = timePointList.get(0);
                timeRange = new LongThreeTuple();
                timeRange.setAElement(timePoint.getAElement());
                timeRange.setBElement(timePoint.getBElement());
                timeRange.setCElement(endTimestamp);
                timeRangeList.add(timeRange);
            } else {
                for (int i = 0; i < timePointList.size(); i++) {
                    timePoint = timePointList.get(i);
                    cElement = i + 1 < timePointList.size() ? timePointList.get(i + 1).getBElement() : endTimestamp;
                    timeRange = new LongThreeTuple();
                    timeRange.setAElement(timePoint.getAElement());
                    timeRange.setBElement(timePoint.getBElement());
                    timeRange.setCElement(cElement);
                    timeRangeList.add(timeRange);
                }
            }

            timeRangeList = timeRangeList.stream().filter(i -> i.getAElement().equals(servicePointId)).collect(Collectors.toList());
            List<OrderProcessLog> tempLogList;
            for (LongThreeTuple item : timeRangeList) {
                tempLogList = logList.stream()
                        .filter(i -> i.getCreateDate().getTime() >= item.getBElement()
                                && i.getCreateDate().getTime() < item.getCElement()
                                && !i.getStatusValue().equals(Order.ORDER_STATUS_COMPLETED))
                        .collect(Collectors.toList());
                list.addAll(tempLogList);
            }
            tempLogList = logList.stream().filter(i -> i.getStatusValue().equals(Order.ORDER_STATUS_COMPLETED)).collect(Collectors.toList());
            list.addAll(tempLogList);

            list = list.stream().sorted(Comparator.comparing(OrderProcessLog::getId).reversed()).collect(Collectors.toList());
        }

        return list;
    }


    /**
     * ProductCompletePicItem列表转成json字符串
     */
    public static String toProductCompletePicItemsJson(List<ProductCompletePicItem> picItems) {
        String json = null;
        if (picItems != null && picItems.size() > 0) {
            Gson gson = new GsonBuilder().registerTypeAdapter(ProductCompletePicItem.class, ProductCompletedPicItemAdapter.getInstance()).create();
            json = gson.toJson(picItems, new TypeToken<List<ProductCompletePicItem>>() {
            }.getType());
            /**
             * 因为myCat1.6不支持在json或text类型的字段中存储英文括号，故将所有的英文括号替换成中文括号.
             */
            json = json.replace("(", "（");
            json = json.replace(")", "）");
        }
        return json;
    }

    /**
     * json字符串转成ProductCompletePicItem列表
     */
    public static List<ProductCompletePicItem> fromProductCompletePicItemsJson(String json) {
        List<ProductCompletePicItem> picItems = null;
        if (StringUtils.isNotEmpty(json)) {
            picItems = GsonUtils.getInstance().getGson().fromJson(json, new TypeToken<List<ProductCompletePicItem>>() {
            }.getType());
        }
        return picItems != null ? picItems : Lists.newArrayList();
    }

    /**
     * 获取客户产品的完工图片规格
     */
    public static Map<Long, ProductCompletePic> getCustomerProductCompletePicMap(List<Long> productIds, Long customerId) {
        if (productIds.isEmpty()) {
            return Maps.newHashMap();
        }
        Map<Long, ProductCompletePic> map = customerProductCompletePicService.getProductCompletePicMap(productIds, customerId);
        if (map == null || map.isEmpty()) {
            map = productCompletePicService.getProductCompletePicMap(productIds);
        } else {
            List<Long> ids = Lists.newArrayList();
            for (Long id : productIds) {
                if (!map.containsKey(id)) {
                    ids.add(id);
                }
            }
            if (!ids.isEmpty()) {
                Map<Long, ProductCompletePic> tempMap = productCompletePicService.getProductCompletePicMap(ids);
                if (!tempMap.isEmpty()) {
                    map.putAll(tempMap);
                }
            }
        }
        return map;
    }

    /**
     * 获取客户产品的完工图片规格
     */
    public static ProductCompletePic getCustomerProductCompletePic(Long prouctId, Long customerId) {
        if (prouctId == null || customerId == null) {
            return null;
        }
        ProductCompletePic productCompletePic = customerProductCompletePicService.getFromCache(prouctId, customerId);
        if (productCompletePic == null) {
            productCompletePic = productCompletePicService.getFromCache(prouctId);
        }
        return productCompletePic;
    }

    /**
     * 返回值为-1时表示计算工单服务类型失败
     */
    public static int calcOrderServiceType(List<OrderDetail> orderDetails, Map<Long, ServiceType> serviceTypeMap) {
        int orderServiceType = -1;
        if (orderDetails != null && !orderDetails.isEmpty() && !serviceTypeMap.isEmpty()) {
            List<ServiceType> serviceTypes = Lists.newArrayList();
            for (OrderDetail item : orderDetails) {
                if (item.getServiceType() != null && item.getServiceType().getId() != null) {
                    ServiceType serviceType = serviceTypeMap.get(item.getServiceType().getId());
                    serviceTypes.add(serviceType);
                }
            }
            if (!serviceTypes.isEmpty()) {
                orderServiceType = serviceTypes.stream().filter(i -> i.getOrderServiceType() != null).mapToInt(ServiceType::getOrderServiceType).max().orElse(-1);
            }
        }
        return orderServiceType;
    }

    /**
     * 返回值为-1时表示计算工单服务类型失败
     */
    public static int calcOrderServiceType(List<OrderDetail> orderDetails) {
        if (orderDetails == null || orderDetails.isEmpty()) {
            return -1;
        }
        Map<Long, ServiceType> serviceTypeMap = serviceTypeService.getAllServiceTypeMap();
        return calcOrderServiceType(orderDetails, serviceTypeMap);
    }

    /**
     * 订单是否可以回退
     2019/08/29 ryan
    public static boolean canBackApprove(Integer status, Date appointmentDate, Date planDate) {

        if (status.intValue() == Order.ORDER_STATUS_PLANNED.intValue()
                && appointmentDate == null
                && planDate != null
                && DateUtils.addHour(planDate, 2).getTime() < new Date().getTime()
        ) {
            return true;
        } else {
            return false;
        }

    }*/

    /**
     * 是否待回访
     *
     * @return 1：待回访
     */
    public static int canFollowUp(OrderCondition order) {
        if (order == null) {
            return 0;
        }
        int status = order.getStatusValue();
        //TODO: APP完工[55]
//        if (status < Order.ORDER_STATUS_APPROVED || status > Order.ORDER_STATUS_SERVICED) {
        if (status < Order.ORDER_STATUS_APPROVED || status > Order.ORDER_STATUS_APP_COMPLETED) {
            return 0;
        }
        if (order.getSubStatus() < Order.ORDER_SUBSTATUS_SERVICED || order.getSubStatus() > Order.ORDER_SUBSTATUS_APPCOMPLETED) {
            return 0;
        }
        if (order.getPendingTypeDate() == null) {
            return 1;
        }
        if (order.getPendingTypeDate().getTime() <= new Date().getTime()) {
            return 1;
        }
        return 0;
    }

    /**
     * 是否待回访
     *
     * @return 1：待回访
     */
    public static int canFollowUpFns(String status, Integer subStatus, Date pendingTypeDate) {
        OrderCondition order = new OrderCondition();
        order.setStatus(new Dict(status));
        order.setSubStatus(subStatus);
        order.setPendingTypeDate(pendingTypeDate);
        return canFollowUp(order);
    }

    /**
     * 计算日志的可见性标志
     */
    public static int calcProcessLogVisibilityFlag(OrderProcessLog log) {
        Set<VisibilityFlagEnum> visibilityFlags = Sets.newHashSet();
        if (log != null) {
            Integer statusFlag = log.getStatusFlag();
            String remarks = log.getRemarks();
            Integer closeFlag = log.getCloseFlag();
            Integer statusValue = log.getStatusValue();
            if (statusFlag != null) {
                if (ORDER_PROCESS_LOG_STATUS_FLAG_SET_KEFU.contains(statusFlag)) {
                    visibilityFlags.add(VisibilityFlagEnum.KEFU);
                }
                if (ORDER_PROCESS_LOG_STATUS_FLAG_SET_CUSTOMER.contains(statusFlag)) {
                    visibilityFlags.add(VisibilityFlagEnum.CUSTOMER);
                } else if (statusFlag.intValue() == OrderProcessLog.OPL_SF_TRACKING && StringUtils.isNotBlank(remarks)) {
                    visibilityFlags.add(VisibilityFlagEnum.CUSTOMER);
                }
            }
            if (closeFlag != null && ORDER_PROCESS_LOG_CLOSE_FLAG_SET_SERVICE_POINT.contains(closeFlag)
                    && statusValue != null && statusValue >= Order.ORDER_STATUS_PLANNED && statusValue <= Order.ORDER_STATUS_CHARGED) {
                visibilityFlags.add(VisibilityFlagEnum.SERVICE_POINT);
            }
        }
        return VisibilityFlagEnum.or(visibilityFlags);
    }

    public enum OrderGradeType {
        NO_GRADE(0, "未客评"),
        MANUAL_GRADE(1, "人工客评"),
        MESSAGE_GRADE(2, "短信客评"),
        VOICE_GRADE(3, "语音回访客评"),
        APP_GRADE(4, "APP自动客评");

        public int value;
        public String name;

        private OrderGradeType(int value, String name) {
            this.value = value;
            this.name = name;
        }

        public int getValue() {
            return this.value;
        }

        public static OrderGradeType fromValue(int value) {
            OrderGradeType[] var1 = values();
            int var2 = var1.length;

            for (int var3 = 0; var3 < var2; ++var3) {
                OrderGradeType type = var1[var3];
                if (type.getValue() == value) {
                    return type;
                }
            }
            return null;
        }
    }

    /**
     * 获取临时订单的缓存key,使用cookie保存，不存在则产生新的，并返回
     * @return
     */
    public static String getUserTmpOrderCacheKey(HttpServletRequest request, HttpServletResponse response,String action)
    {
        //edit order
        if(action.equalsIgnoreCase("edit")) {
            String orderkey = CookieUtils.getCookie(request, OrderUtils.ORDER_EDIT_COOKIE_KEY);
            if (StringUtils.isNotEmpty(orderkey)) {
                return orderkey;
            }
            StringBuilder key = new StringBuilder(32);
            key.append("edit:order:").append(IdGen.randomBase62(10));
            CookieUtils.setCookie(response, OrderUtils.ORDER_EDIT_COOKIE_KEY,
                    key.toString(), 60 * 60 * 24 * 7);
            return key.toString();
        } else if(action.equalsIgnoreCase("editv2")) {
            String orderkey = CookieUtils.getCookie(request, OrderUtils.ORDER_EDIT_V2_COOKIE_KEY);
            if (StringUtils.isNotEmpty(orderkey)) {
                return orderkey;
            }
            StringBuilder key = new StringBuilder(32);
            key.append("edit:order:v2:").append(IdGen.randomBase62(10));
            CookieUtils.setCookie(response, OrderUtils.ORDER_EDIT_V2_COOKIE_KEY,
                    key.toString(), 60 * 60 * 24 * 7);
            return key.toString();
        } else if(action.equalsIgnoreCase("newv2")){
            //灯饰new order
            String orderkey = CookieUtils.getCookie(request, OrderUtils.ORDER_CREATE_V2_COOKIE_KEY);
            if (StringUtils.isNotEmpty(orderkey)) {
                return orderkey;
            }
            StringBuilder key = new StringBuilder(32);
            key.append("tmp:order:v2:").append(IdGen.randomBase62(10));
            CookieUtils.setCookie(response, OrderUtils.ORDER_CREATE_V2_COOKIE_KEY,
                    key.toString(), 60 * 60 * 24 * 7);
            return key.toString();
        }else {
            //new order
            String orderkey = CookieUtils.getCookie(request, OrderUtils.ORDER_CREATE_COOKIE_KEY);
            if (StringUtils.isBlank(orderkey)) {
                orderkey = "tmp:order:" + IdGen.randomBase62(10);
                CookieUtils.setCookie(response, OrderUtils.ORDER_CREATE_COOKIE_KEY,
                        orderkey, 60 * 60 * 24 * 7);
            }
            return orderkey;
        }
    }

    /**
     * 屏蔽客户(应收)自动同步加的远程费及其他费用
     * 受控品类，不允许客服查看客户（应收）远程费及其他费用
     * @param order
     */
    public static void customerSyncChargeActionShield(Order order){
        if(order == null || order.getOrderCondition() == null){
            return;
        }
        OrderCondition orderCondition = order.getOrderCondition();
        if(CollectionUtils.isEmpty(order.getDetailList())){
            return;
        }
        // 检查是否为受控品类：自动同步应收远程费和其他费用
        //Dict dict = MSDictUtils.getDictByValue(orderCondition.getProductCategoryId().toString(), OrderUtils.SYNC_CUSTOMER_CHARGE_DICT);
        //if (dict == null || !dict.getValue().equals(orderCondition.getProductCategoryId().toString())) {
        //    return;
        //}
        //受控品类
        List<OrderDetail> details = order.getDetailList().stream().filter(t->t.getDelFlag() == 0 && t.getSyncChargeTags() >0).collect(Collectors.toList());
        for(OrderDetail detail:details){
            if(BitUtils.hasTag(detail.getSyncChargeTags(),BitUtils.positionToTag(SyncCustomerCharge.TRAVEL.ordinal()))){
                detail.setTravelCharge(0.0);
            }
            if(BitUtils.hasTag(detail.getSyncChargeTags(),BitUtils.positionToTag(SyncCustomerCharge.OTHER.ordinal()))){
                detail.setOtherCharge(0.0);
            }
        }
    }

    /**
     * 检查工单的类型
     */
    public static boolean checkOrderServiceType(OrderCondition condition, OrderTypeEnum type) {
        boolean result = false;
        if (condition != null) {
            result = condition.getOrderServiceType() == type.getId();
        }
        return result;
    }

    /**
     * 检查工单的类型
     */
    public static boolean checkOrderServiceType(Order order, OrderTypeEnum type) {
        boolean result = false;
        if (order != null) {
            result = checkOrderServiceType(order.getOrderCondition(), type);
        }
        return result;
    }

}
