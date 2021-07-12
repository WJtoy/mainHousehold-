package com.wolfking.jeesite.modules.mq.receiver;

import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.entity.push.AppMessageType;
import com.kkl.kklplus.entity.sys.SysSMSTypeEnum;
import com.kkl.kklplus.utils.StringUtils;
import com.rabbitmq.client.Channel;
import com.wolfking.jeesite.common.service.SequenceIdService;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.md.service.ServicePointService;
import com.wolfking.jeesite.modules.mq.dto.MQCreateOrderPushMessage;
import com.wolfking.jeesite.modules.mq.entity.OrderCreateBody;
import com.wolfking.jeesite.modules.mq.sender.CreateOrderPushMessageSender;
import com.wolfking.jeesite.modules.mq.sender.OrderReportSender;
import com.wolfking.jeesite.modules.mq.sender.sms.SmsMQSender;
import com.wolfking.jeesite.modules.mq.service.OrderCreateMessageService;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.entity.OrderFee;
import com.wolfking.jeesite.modules.sd.entity.OrderProcessLog;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.ms.entity.AppPushMessage;
import com.wolfking.jeesite.ms.service.push.APPMessagePushService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * 下单短信及消息消费者
 * Created by Ryan on 2017/7/27
 *
 * @date 2020/12/31
 * @author ryan
 * 1.京东优易+的单不发抢单短信及App消息
 *
 */
@Component
@Slf4j
public class CreateOrderPushMessageReceiver implements ChannelAwareMessageListener {

    @Autowired
    private SequenceIdService sequenceIdService;

    @Autowired
    private RabbitProperties rabbitProperties;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ServicePointService servicePointService;

    @Autowired
    private OrderCreateMessageService orderCreateMessageService;

    @Autowired
    private CreateOrderPushMessageSender createOrderPushMessageSender;

    @Autowired
    private SmsMQSender smsMQSender;

    @Autowired
    private APPMessagePushService appMessagePushService;

    @Override
    public void onMessage(org.springframework.amqp.core.Message message, Channel channel) throws Exception {
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        MQCreateOrderPushMessage.CreateOrderPushMessage orderMessage = null;
        Long oid = null;
        Date date = null;
        User user = null;
        MQCreateOrderPushMessage.OrderFee mqFee = null;
        //执行进度
        int step = 0;
        //region 订单信息
        try {
            orderMessage = MQCreateOrderPushMessage.CreateOrderPushMessage.parseFrom(message.getBody());
            if (orderMessage == null) {
                log.error("消息体信息错误:解析消息错误");
                return;
            }
            if (orderMessage.getOrderId() <= 0) {
                log.error("消息体信息错误,message:{}", new JsonFormat().printToString(orderMessage));
                return;
            }

            user = new User(orderMessage.getTriggerBy().getId(), orderMessage.getTriggerBy().getName(), "");
            date = DateUtils.longToDate(orderMessage.getTriggerDate());
            oid = orderMessage.getOrderId();
            if(oid == null || oid <= 0){
                log.error("[CreateOrderPushMessageReceiver.onMessage]消息体信息错误:订单id null");
                return;
            }

            //order fee
            OrderFee fee = new OrderFee();
            mqFee = orderMessage.getOrderFee();
            fee.setOrderId(orderMessage.getOrderId());
            fee.setQuarter(orderMessage.getQuarter());
            fee.setExpectCharge(mqFee.getExpectCharge());
            fee.setBlockedCharge(mqFee.getBlockedCharge());
            fee.setCustomerUrgentCharge(mqFee.getCustomerUrgentCharge());
            fee.setEngineerUrgentCharge(mqFee.getEngineerUrgentCharge());
            fee.setOrderPaymentType(new Dict(mqFee.getOrderPaymentType(), ""));

            //log
            OrderProcessLog orderLog = new OrderProcessLog();
            //orderLog.setId(oid);//与订单id相同,生产中出现过id与订单id相同的情况
            orderLog.setId(sequenceIdService.nextId());
            orderLog.setQuarter(orderMessage.getQuarter());
            orderLog.setOrderId(oid);
            orderLog.setStatus("下单");
            orderLog.setStatusValue(Order.ORDER_STATUS_NEW);
            orderLog.setStatusFlag(OrderProcessLog.OPL_SF_CHANGED_STATUS);
            orderLog.setCloseFlag(0);
            orderLog.setCreateBy(user);
            orderLog.setCreateDate(date);
            //是否自动审核 1-是
            if (orderMessage.getOrderApproveFlag() == 1) {
                orderLog.setAction("下单");
                orderLog.setActionComment(String.format("客户下单并审核:%s,下单人:%s", orderMessage.getOrderNo(), user.getName()));

            } else {
                orderLog.setAction("下单");
                orderLog.setActionComment(String.format("客户下单:%s,下单人:%s", orderMessage.getOrderNo(), user.getName()));
            }

            //save to db
            orderService.insertCreateLogAndFee(fee, orderLog);
            //补偿机制重发的队列
            step = 1;
            if (orderMessage.getId() > 0) {
                OrderCreateBody body = new OrderCreateBody();
                body.setId(orderMessage.getId());
                body.setRemarks("ok");
                body.setStatus(30);//ok
                body.setUpdateDate(new Date());
                orderCreateMessageService.update(body);
            }

            //region 制造异常
            step = 2;
            //该异常不会重试，因订单的事务已成功执行
            //int i = 10 / 0;
            //System.out.println(i);
            //endregion

        } catch (Exception e) {
            if (step == 0) {
                //订单事务失败，重试
                createOrderPushMessageSender.sendDelay(orderMessage, getDelaySeconds(), 1);
                //System.out.println("send Delay");
            } else {
                log.error("[CreateOrderPushMessageReceiver] order id:{}", orderMessage.getOrderId(), e);
            }
            //return;
        }
        //endregion 订单信息

        // 重单检查缓存更新
        syncOrderRepeatCheckCache(orderMessage.getCustomer().getId(), orderMessage.getOrderId(), orderMessage.getOrderNo(), orderMessage.getQuarter(), orderMessage.getUserPhone());

        // 发送短信及APP推送消息
        sendSmsAndPushMessage(orderMessage.getDataSource(),oid, orderMessage.getCategoryId(), orderMessage.getOrderApproveFlag(), orderMessage.getMsgContent(), orderMessage.getAreaId(), user.getId(), date.getTime());
    }

    private void syncOrderRepeatCheckCache(long customerId,long orderId,String orderNo,String quarter,String phone){
        int step = 2;
        String userPhone = phone;
        try {
            //因系统更新可能造成旧队列中无用户手机号，
            //增加如下处理
            if (StringUtils.isBlank(userPhone)) {
                try {
                    Order order = orderService.getOrderById(orderId, quarter, OrderUtils.OrderDataLevel.CONDITION, true);
                    userPhone = order.getOrderCondition().getServicePhone().trim();
                } catch (Exception e) {
                }
            }
            if (StringUtils.isNotBlank(userPhone)) {
                orderService.setNewRepeateOrderNo(customerId, userPhone, orderNo);
            }
        }catch (Exception e){
            log.error("更新重单检查缓存错误",e);
        }
    }


    /**
     * 发送短信及APP推送消息
     *
     * @date 2020-12-31
     * 京东优易+的单不发抢单短信及App消息
     */
    private void sendSmsAndPushMessage(int dataSource,long orderId,long categoryId,int orderApproveFlag,String msgContent,long areaId,long sendBy,long sendAt){
        int step = 5;
        //京东优易+的单不发抢单短信及App消息 2020-12-31
        if(dataSource == B2BDataSourceEnum.JDUEPLUS.getId()) {
            return;
        }
        // 内容示例如下：
        // 张三师傅，在您附近有一张上门安装百得油烟机的工单，请尽快登陆APP接单~
        if (orderApproveFlag == 1 && StringUtils.isNotBlank(msgContent)) {//已审核
            try {
                List<User> engineers = servicePointService.getEngineerAccountsListByAreaAndProductCategory(areaId,categoryId);
                if (engineers != null && engineers.size() > 0) {
                    for (User engineer : engineers) {
                        //手机接单权限
                        if (engineer.getAppFlag() == 0) {
                            continue;
                        }
                        // 短信
                        if (engineer.getShortMessageFlag() == 1) {
                            smsMQSender.sendNew(engineer.getMobile(),
                                    engineer.getName().substring(0, 1).concat(msgContent),
                                    "",
                                    sendBy,
                                    sendAt,
                                    SysSMSTypeEnum.ORDER_CREATED
                            );
                        }

                        // 发送APP消息
                        // 张三师傅，在您附近有一张上门安装百得油烟机的工单，请尽快登陆APP接单~
                        AppPushMessage pushMessage = new AppPushMessage();
                        pushMessage.setPassThroughType(AppPushMessage.PassThroughType.NOTIFICATION);
                        pushMessage.setMessageType(AppMessageType.ACCEPTORDER);
                        pushMessage.setSubject("");
                        pushMessage.setContent("");
                        pushMessage.setTimestamp(System.currentTimeMillis());
                        pushMessage.setUserId(engineer.getId());
                        pushMessage.setDescription(engineer.getName().substring(0, 1).concat(msgContent));
                        appMessagePushService.sendMessage(pushMessage);
                    }
                }
            } catch (Exception e) {
                log.error("发送下单短信及APP推送错误,orderId:{}",orderId,e);
            }
        }

    }

    private int getDelaySeconds() {
        return (int) (rabbitProperties.getTemplate().getRetry().getInitialInterval() * rabbitProperties.getTemplate().getRetry().getMultiplier());
    }

}
