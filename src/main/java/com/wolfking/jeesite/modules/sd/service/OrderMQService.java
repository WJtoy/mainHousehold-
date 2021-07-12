/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.wolfking.jeesite.modules.sd.service;

import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.entity.fi.mq.MQCreateCustomerCurrencyMessage;
import com.kkl.kklplus.entity.lm.mq.MQLMExpress;
import com.kkl.kklplus.entity.push.AppMessageType;
import com.kkl.kklplus.entity.rpt.common.RPTOrderProcessTypeEnum;
import com.kkl.kklplus.entity.sys.SysSMSTypeEnum;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.service.SequenceIdService;
import com.wolfking.jeesite.modules.fi.entity.CustomerCurrency;
import com.wolfking.jeesite.modules.fi.service.CustomerBlockCurrencyService;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.CustomerFinance;
import com.wolfking.jeesite.modules.md.entity.PlanRadius;
import com.wolfking.jeesite.modules.md.service.PlanRadiusService;
import com.wolfking.jeesite.modules.mq.dto.MQCreateOrderPushMessage;
import com.wolfking.jeesite.modules.mq.dto.MQCustomer;
import com.wolfking.jeesite.modules.mq.dto.MQOrderAutoPlanMessage;
import com.wolfking.jeesite.modules.mq.entity.OrderCreateBody;
import com.wolfking.jeesite.modules.mq.entity.RPTOrderProcessModel;
import com.wolfking.jeesite.modules.mq.sender.CreateCustomerCurrencySender;
import com.wolfking.jeesite.modules.mq.sender.CreateOrderPushMessageSender;
import com.wolfking.jeesite.modules.mq.sender.OrderAutoPlanMessageSender;
import com.wolfking.jeesite.modules.mq.sender.sms.SmsMQSender;
import com.wolfking.jeesite.modules.mq.service.OrderCreateMessageService;
import com.wolfking.jeesite.modules.mq.service.RPTOrderProcessService;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.entity.OrderFee;
import com.wolfking.jeesite.modules.sd.entity.OrderItem;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.modules.sys.utils.SeqUtils;
import com.wolfking.jeesite.ms.b2bcenter.sd.service.B2BCenterPushOrderInfoToMsService;
import com.wolfking.jeesite.ms.entity.AppPushMessage;
import com.wolfking.jeesite.ms.logistics.service.LogisticsBusinessService;
import com.wolfking.jeesite.ms.service.push.APPMessagePushService;
import com.wolfking.jeesite.ms.um.sd.service.UMOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * 工单消息队列生产者
 */
@Slf4j
@Configuration
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class OrderMQService {

    private static final int DELAY_TIME = 30000; //延迟30秒

    @Autowired
    private OrderCreateMessageService orderCreateMessageService;

    @Autowired
    private CreateOrderPushMessageSender createOrderMessageSender;

    @Autowired
    private CreateCustomerCurrencySender createCustomerCurrencySender;

    @Autowired
    private OrderAutoPlanMessageSender autoPlanMessageSender;

    @Autowired
    private UMOrderService umOrderService;
    @Autowired
    private B2BCenterPushOrderInfoToMsService b2BCenterPushOrderInfoToMsService;

    @Autowired
    private PlanRadiusService planRadiusService;

    @Autowired
    private LogisticsBusinessService logisticsBusinessService;

    @Autowired
    private AreaService areaService;

    @Autowired
    private APPMessagePushService appMessagePushService;

    @Autowired
    private SmsMQSender smsMQSender;

    @Autowired
    private RPTOrderProcessService rptOrderProcessService;

    @Autowired
    private SequenceIdService sequenceIdService;

    @Autowired
    private CustomerBlockCurrencyService customerBlockCurrencyService;

    //自动派单标志
    @Value("${autoPlanFlag}")
    private Boolean autoPlanFlag;

    @Value("${cancelOrder.noticeType}")
    private int cancelOrderNoticeType;

    //region 下单

    /**
     * @version 2.0
     * 下单/转单成功时，推送下单消息
     * 下单，导入订单转单，B2B订单人工转单，B2B订单自动转单
     * 1.发送财务流水消息
     * 2.发送下单消息：
     *  2.1.processlog
     *  2.2.orderFee
     *  2.3.报表
     *
     * @version 2.1
     * @date 2019-04-25
     * @author Ryan
     * 增加发送自动派单消息
     *
     * @version 2.2
     * @date 2019-05-27
     * @author Ryan
     * 增加发送与快递100接口：快递单订阅消息
     *
     * @version 2.3
     * @date 2019-06-17
     * @author Ryan
     * 修复订单未审核自动派单问题，发送app推送及优盟订单信息也增加同样的判断
     *
     * @date 2020-12-31
     * @author Ryan
     * 1.消息体增加数据源，用于判断订单来源做处理
     * 2.京东优易+订单，不自动派单处理
     *
     * @param order
     * @param methodName    调用方的方法
     */
    public void sendCreateOrderMessage(Order order,String methodName) {
        if(order == null){
            return;
        }
        String json = new String();
        User user = order.getCreateBy();
        Long currencyId = null;
        try {
            currencyId = SeqUtils.NextIDValue(SeqUtils.TableName.CustomerCurrency);
        }catch (Exception e){
            log.error("获得id错误，key:{}",SeqUtils.TableName.CustomerCurrency,e);
        }
        if(currencyId == null || currencyId==0){
            currencyId = sequenceIdService.nextId();
        }
        long orderId = order.getId();
        String quarter = order.getQuarter();

        Customer customer = order.getOrderCondition().getCustomer();
        CustomerFinance finance = customer.getFinance();
        //冻结金额流水
        String remarks;
        OrderFee fee = order.getOrderFee();
        //TODO: 切分冻结流水
        customerBlockCurrencyService.saveOrderCreatedBlockCurrency(customer.getId(), order.getOrderNo(),
                finance.getBlockAmount(), fee.getBlockedCharge(), fee.getExpectCharge(), fee.getCustomerUrgentCharge(), quarter, user, order.getCreateDate());

//        if (fee.getCustomerUrgentCharge() == 0) {
//            remarks=String.format("下单冻结 %.2f元 相关单号为 %s", (fee.getBlockedCharge() + fee.getExpectCharge()), order.getOrderNo());
//        } else {
//            remarks = String.format("下单冻结 %.2f元,其中加急费：%.2f. 相关单号为 %s", (fee.getBlockedCharge() + fee.getExpectCharge()), fee.getCustomerUrgentCharge(), order.getOrderNo());
//        }
//        MQCreateCustomerCurrencyMessage.CreateCustomerCurrencyMessage createCustomerCurrencyMessage = null;
//        try {
//            createCustomerCurrencyMessage = MQCreateCustomerCurrencyMessage.CreateCustomerCurrencyMessage.newBuilder()
//                    .setMessageId(currencyId)
//                    .setCustomerId(customer.getId())
//                    .setCurrencyType(CustomerCurrency.CURRENCY_TYPE_IN)
//                    .setCurrencyNo(order.getOrderNo())
//                    .setBeforeBalance(finance.getBlockAmount())
//                    .setBalance(finance.getBlockAmount() + fee.getBlockedCharge() + fee.getExpectCharge())
//                    .setAmount(fee.getBlockedCharge() + fee.getExpectCharge())
//                    .setPaymentType(CustomerCurrency.PAYMENT_TYPE_CASH)
//                    .setActionType(70)
//                    .setQuarter(quarter)
//                    .setCreateById(user.getId())
//                    .setCreateDate(order.getCreateDate().getTime())
//                    .setRemarks(remarks)
//                    .build();
//            createCustomerCurrencySender.send(createCustomerCurrencyMessage);
//        } catch (Exception e) {
//            if (createCustomerCurrencyMessage != null) {
//                LogUtils.saveLog("订单创建.生成客户冻结流水", "MS:MQ:FI:CREATE:CUSTOMER:CURRENCY", new JsonFormat().printToString(createCustomerCurrencyMessage), e, user);
//            }else{
//                StringBuilder sbLog = new StringBuilder(150);
//                sbLog.append("orderId:").append(order.getId())
//                        .append(String.format(",beforeBalance:%2d", finance.getBlockAmount())).append(" ")
//                        .append(String.format(",balance:%2d", finance.getBlockAmount() + fee.getBlockedCharge() + fee.getExpectCharge()))
//                        .append(String.format(",amount:%2d", fee.getBlockedCharge() + fee.getExpectCharge()))
//                        .append(",paymentType:").append(CustomerCurrency.PAYMENT_TYPE_CASH)
//                        .append(",actionType:70")
//                        .append("remarks:").append(remarks);
//                log.error("生成客户流水数据失败，info:{}", sbLog.toString(), e);
//                sbLog = null;
//            }
//        }

        //region 下单消息队列

        // 张三师傅，在您附近有一张上门安装百得油烟机的工单，请尽快登陆APP接单~
        User kefu = order.getOrderCondition().getKefu();
        MQCreateOrderPushMessage.CreateOrderPushMessage orderPushMessage = null;
        try {
            StringBuilder appMessage = new StringBuilder(200);
            if (StringUtils.isNotBlank(order.getAppMessage())) {
                appMessage.append(order.getAppMessage());
            } else {
                appMessage.append("师傅，在您附近有一张  ");
                for (OrderItem item : order.getItems()) {
                    appMessage.append(item.getServiceType().getName())
                            .append(item.getBrand())
                            .append(com.wolfking.jeesite.common.utils.StringUtils.getStandardProductName(item.getProduct().getName()));
                }
                appMessage.append("的工单，请尽快登陆APP接单~");
            }
            orderPushMessage = MQCreateOrderPushMessage.CreateOrderPushMessage.newBuilder()
                    .setOrderId(orderId)
                    .setQuarter(quarter)
                    .setOrderNo(order.getOrderNo())
                    .setDataSource(order.getDataSourceId()) //2020-12-31
                    .setCategoryId(order.getOrderCondition().getProductCategoryId()) // 2019-09-26
                    .setUserPhone(order.getOrderCondition().getServicePhone())//2018/10/24
                    .setTriggerBy(MQCreateOrderPushMessage.TriggerBy.newBuilder()
                            .setId(user.getId())
                            .setName(user.getName())
                            .build())
                    .setTriggerDate(order.getCreateDate().getTime())
                    .setOrderApproveFlag(order.getOrderCondition().getStatusValue() == Order.ORDER_STATUS_NEW ? 0 : 1)
                    //order fee
                    .setOrderFee(MQCreateOrderPushMessage.OrderFee.newBuilder()
                            .setExpectCharge(fee.getExpectCharge())
                            .setBlockedCharge(fee.getBlockedCharge())
                            .setCustomerUrgentCharge(fee.getCustomerUrgentCharge())
                            .setEngineerUrgentCharge(fee.getEngineerUrgentCharge())
                            .setOrderPaymentType(fee.getOrderPaymentType().getIntValue())
                            .setOrderPaymentTypeName(fee.getOrderPaymentType().getLabel())
                            .build())
                    //kefu
                    .setKefu(MQCreateOrderPushMessage.TriggerBy.newBuilder()
                            .setId(kefu.getId())
                            .setName(kefu.getName())
                            .build())
                    .setMsgContent(appMessage.toString())
                    .setCustomer(MQCustomer.Customer.newBuilder()
                            .setId(customer.getId())
                            .setCode(customer.getCode())
                            .setName(customer.getName())
                            .setContractDate(customer.getContractDate() == null ? 0 : customer.getContractDate().getTime())
                            .setSalesId((customer.getSales() == null || customer.getSales().getId() == null) ? 0l : customer.getSales().getId())
                            .setSalesName(customer.getSales() == null ? "" : customer.getSales().getName())
                            .setPaymentType(Integer.parseInt(fee.getOrderPaymentType().getValue()))
                            .setPaymentTypeName(fee.getOrderPaymentType().getLabel())
                            .build())
                    //area
                    .setAreaId(order.getOrderCondition().getArea().getId())
                    .build();
            createOrderMessageSender.send(orderPushMessage);
        } catch (Exception e) {
            OrderCreateBody body = new OrderCreateBody();
            body.setId(orderId);
            body.setOrderId(orderId);
            body.setQuarter(quarter);
            body.setStatus(40);
            body.setTriggerBy(user);
            body.setTriggerDate(order.getCreateDate());
            body.setType(1);
            StringBuffer err = new StringBuffer(255);
            err.append(e.getCause());
            //err.setLength(250);//截断，只保留前250个字符
            body.setRemarks(StringUtils.left(err.toString(),250));
            body.setCreateDate(new Date());
            if (orderPushMessage != null) {
                body.setJson(new JsonFormat().printToString(orderPushMessage));
            }
            try {
                orderCreateMessageService.insert(body);
            } catch (Exception e1) {
                if (StringUtils.isNotBlank(body.getJson())) {
                    log.error("保存下单消息队列错误,order:{}", body.getJson(), e1);
                } else {
                    log.error("保存下单消息队列错误,orderId:{}", orderId, e1);
                }
            }
        }

        //endregion

        // 审核通过一些才继续处理
        int statusValue = order.getOrderCondition().getStatusValue();
        if (statusValue == Order.ORDER_STATUS_APPROVED.intValue()) {
            //向优盟微服务抛工单原始数据
            umOrderService.sendOrderDataToMS(order);
            b2BCenterPushOrderInfoToMsService.createOrder(order);
            //自动派单 2019-04-15
            sendAutoPlanOrderMessage(order);
            //version:2.2 订阅物流推送
            logisticsBusinessService.subsLogisticsMessage(MQLMExpress.GoodsType.Goods, order.getId(), order.getOrderNo(), order.getQuarter(), order.getOrderCondition().getServicePhone(), order.getItems());
        }
        // 报表微服务消息队列 add on 2019-12-10
        try {
            RPTOrderProcessModel rptOrderProcessModel = new RPTOrderProcessModel();
            rptOrderProcessModel.setProcessType(RPTOrderProcessTypeEnum.CREATE.getValue());
            rptOrderProcessModel.setOrderId(orderId);
            rptOrderProcessModel.setProvinceId(order.getOrderCondition().getProvinceId());
            rptOrderProcessModel.setCityId(order.getOrderCondition().getCityId());
            rptOrderProcessModel.setCountId(order.getOrderCondition().getArea().getId());
            rptOrderProcessModel.setCustomerId(order.getOrderCondition().getCustomerId());
            rptOrderProcessModel.setKeFuId(order.getOrderCondition().getKefu().getId());
            rptOrderProcessModel.setProductCategoryId(order.getOrderCondition().getProductCategoryId());
            rptOrderProcessModel.setDataSource(order.getDataSourceId());
            rptOrderProcessModel.setOrderCreateDate(order.getCreateDate().getTime());
            rptOrderProcessModel.setOrderServiceType(order.getOrderCondition().getOrderServiceType());
            rptOrderProcessService.sendRPTOrderProcess(rptOrderProcessModel);
        }catch (Exception e){
            log.error("下单发送报表微服务消息对列错误:"+ "orderId = " + orderId +"错误原因:" + e.getMessage());
        }
    }

    /**
     * @Version 0.1
     * @date 2019-04-20
     * @author Ryan
     * 自动派单消息入队
     * 1.检查全局开关
     * 2.检查传入订单
     * 3.检查区域
     * 4.检查检索半径
     *
     * @date 2020-12-31
     * 1.京东优易+不自动派单
     *
     * @param order
     */
    public void sendAutoPlanOrderMessage(Order order) {
        //检查全局开关
        if (!autoPlanFlag) {
            return;
        }
        //京东优易+不自动派单 2020-12-31
        if(order.getDataSourceId() == B2BDataSourceEnum.JDUEPLUS.getId()) {
            return;
        }
        //检查基本信息
        if (order == null || order.getId() == null || order.getId() <= 0
                || order.getOrderCondition() == null || order.getOrderCondition().getArea() == null
                || order.getOrderCondition().getArea().getId() == null || order.getOrderCondition().getArea().getId() <= 0
                || order.getOrderCondition().getSubArea() == null || order.getOrderCondition().getSubArea().getId() == null
                || order.getOrderCondition().getSubArea().getId() <= 3 //subArea中id为1时为手工选区域;2时为高德无法识别到街道;3时高德能识别到街道,系统redis无法找到街道配置
                || StringUtils.isBlank(order.getQuarter()) || StringUtils.isBlank(order.getOrderNo())) {
            log.warn("订单:{}无法通过基本信息检查,无法自动派单.", order.getId());
            return;
        }
        User user = order.getCreateBy();
        if (user == null || user.getId() == null) {
            log.error("自动派单：用户为null");
            return;
        }
        Area area = order.getOrderCondition().getArea();
        if (area == null || area.getId() == null || area.getId() <= 0) {
            return;
        }

        Area subArea = order.getOrderCondition().getSubArea();
        if (subArea == null || subArea.getId() == null || subArea.getId() <= 3) {
            Long subAreaId = subArea == null ? -1L : (subArea.getId() == null ? -2L : subArea.getId());
            log.warn("订单:{}的4级区域:{}不是有效区域,无法自动派单.", order.getId(), subAreaId);
            return;
        }
        subArea = areaService.getFromCache(subArea.getId(), Area.TYPE_VALUE_TOWN);

        //check area
        //读取区域自动检索半径信息,未设定半径的不自动派单
        // add on 2020-9-9 begin
        PlanRadius planRadius = new PlanRadius();
        planRadius.setAutoPlanFlag(1);
        planRadius.setRadius1(0);
        planRadius.setRadius2(0);
        planRadius.setRadius3(0);
        // add on 2020-9-9 end

        if (order.getCreateDate() == null) {
            order.setCreateDate(new Date());
        }
        MQOrderAutoPlanMessage.OrderAutoPlan message = MQOrderAutoPlanMessage.OrderAutoPlan.newBuilder()
                .setOrderId(order.getId())
                .setQuarter(order.getQuarter())
                .setOrderNo(order.getOrderNo())
                .setCreateBy(user.getId())
                .setCreator(user.getName())
                .setCreateAt(order.getCreateDate().getTime())
                .setAreaId(subArea.getId())
                .setAreaName(subArea.getName())
                //.setAreaId(area.getId())
                //.setAreaName(area.getName())
                .setServiceAddress(String.format("%s %s", area.getName(), order.getOrderCondition().getServiceAddress()))
                .setAreaRadius(MQOrderAutoPlanMessage.AreaRadius.newBuilder()
                        .setRadius1(planRadius.getRadius1())
                        .setRadius2(planRadius.getRadius2())
                        .setRadius3(planRadius.getRadius3())
                        .build())
                .setLongitude(order.getOrderLocation() == null ? 0.00 : order.getOrderLocation().getLongitude())
                .setLatitude(order.getOrderLocation() == null ? 0.00 : order.getOrderLocation().getLatitude())
                .build();
        try {
            autoPlanMessageSender.sendRetry(message, DELAY_TIME, 0);
        } catch (Exception e) {
            log.error("自动派单入队错误，body:{}", new JsonFormat().printToString(message), e);
        }
    }

    //endregion 下单

    /**
     * 判断是否符合自动派单条件
     */
    public static boolean canAutoPlan(MQOrderAutoPlanMessage.OrderAutoPlan message){
        if(message == null){
            log.error("自动派单错误：消息体为空");
            return false;
        }else if(message.getOrderId()<=0 || StringUtils.isBlank(message.getOrderNo())
                || StringUtils.isBlank(message.getQuarter()) || message.getAreaId() <= 0
                || StringUtils.isBlank(message.getAreaName()) || StringUtils.isBlank(message.getServiceAddress())
                ){
            try {
                String json = new JsonFormat().printToString(message);
                log.error("自动派单错误：消息内容不符合,message:{}",json);
            }catch (Exception e){
                log.error("自动派单错误:消息转json错误",e);
            }
            return false;
        }

        return true;
    }

    /**
     * 取消订单，需通知安维
     * @param user 取消人
     * @param engineerName  安维姓名
     * @param orderNo   单号
     * @param kefuPhone 客服电话
     */
    public void noticeEngineerOrderCanceled(User user,Long engineerId,String engineerName,String engineerMobile,String orderNo,String kefuPhone){
        if(StringUtils.isBlank(engineerName) || StringUtils.isBlank(orderNo) || StringUtils.isBlank(kefuPhone)){
            log.error("取消订单发送通知给安维失败，传送参数无效,name:{} ,orderNo:{} ,kefu phone:{}",engineerName,orderNo,kefuPhone);
            return;
        }
        if(cancelOrderNoticeType == 0 && StringUtils.isBlank(engineerMobile)){
            log.error("取消订单发送短信通知给安维失败，无安维电话");
            return;
        }else if(cancelOrderNoticeType == 1 && (engineerId == null || engineerId <= 0 )){
            log.error("取消订单发送APP通知给安维失败，无安维id");
            return;
        }
        //app notice
        if(cancelOrderNoticeType == 1) {
            AppPushMessage pushMessage = new AppPushMessage();
            pushMessage.setPassThroughType(AppPushMessage.PassThroughType.NOTIFICATION);
            pushMessage.setMessageType(AppMessageType.SYSTEM);
            pushMessage.setSubject("");
            pushMessage.setContent("");
            pushMessage.setTimestamp(System.currentTimeMillis());
            pushMessage.setUserId(engineerId);
            pushMessage.setDescription(String.format(OrderUtils.SALE_CANCEL_APP_NOTICE_TEMPLATE, engineerName, orderNo, kefuPhone));
            appMessagePushService.sendMessage(pushMessage);
            return;
        }
        //sms
        if(cancelOrderNoticeType == 0){
            smsMQSender.sendNew(engineerMobile,
                    String.format(OrderUtils.SALE_CANCEL_APP_NOTICE_TEMPLATE, engineerName, orderNo, kefuPhone),
                    "",
                    user.getId(),
                    System.currentTimeMillis(),
                    SysSMSTypeEnum.ORDER_CANCELLED
            );
        }
    }

}
