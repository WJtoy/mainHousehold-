package com.wolfking.jeesite.modules.mq.receiver;

import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.entity.b2b.mq.B2BMQConstant;
import com.kkl.kklplus.entity.push.AppMessageType;
import com.kkl.kklplus.entity.sys.SysSMSTypeEnum;
import com.kkl.kklplus.utils.StringUtils;
import com.rabbitmq.client.Channel;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.md.service.ServicePointService;
import com.wolfking.jeesite.modules.mq.dto.MQCreateOrderPushMessage;
import com.wolfking.jeesite.modules.mq.entity.OrderCreateBody;
import com.wolfking.jeesite.modules.mq.sender.CreateOrderPushMessageSender;
import com.wolfking.jeesite.modules.mq.sender.sms.SmsMQSender;
import com.wolfking.jeesite.modules.mq.service.OrderCreateMessageService;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.entity.OrderFee;
import com.wolfking.jeesite.modules.sd.entity.OrderProcessLog;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.td.entity.Message2;
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
 * 下单短信及消息重试消费者
 * Created by Ryan on 2017/7/27
 */
@Component
@Slf4j
public class CreateOrderPushMessageRetryReceiver implements ChannelAwareMessageListener {

    @Autowired
    private RabbitProperties rabbitProperties;

    @Autowired
    private CreateOrderPushMessageSender createOrderPushMessageSender;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ServicePointService servicePointService;

    @Autowired
    private OrderCreateMessageService orderCreateMessageService;

    @Autowired
    private APPMessagePushService appMessagePushService;

    @Autowired
    private SmsMQSender smsMQSender;

    @Override
    public void onMessage(org.springframework.amqp.core.Message message, Channel channel) throws Exception {
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        MQCreateOrderPushMessage.CreateOrderPushMessage orderMessage = null;
        int times = StringUtils.toInteger(message.getMessageProperties().getHeaders().get(B2BMQConstant.MESSAGE_PROPERTIES_HEADER_KEY_TIMES));
        Long oid = null;
        Date date = null;
        User user = null;
        MQCreateOrderPushMessage.OrderFee mqFee = null;
        //执行进度
        int step = 0;

        try {

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
                //log.setId(SeqUtils.NextID());
                orderLog.setId(oid);//与订单id相同
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
            } catch (Exception e) {
                if (step == 0 && times < getMaxAttempts()) {
                    times++;
                    createOrderPushMessageSender.sendDelay(orderMessage, getDelaySeconds(times), times);
                } else {
                    log.error("[CreateOrderPushMessageReceiver] order id:{}", orderMessage.getOrderId(), e);
                }
                return;
            }
            //endregion 订单信息

            //region 重单检查缓存更新
            step = 2;
            String userPhone = orderMessage.getUserPhone();
            try {
                //因系统更新可能造成旧队列中无用户手机号，
                //增加如下处理
                if (StringUtils.isBlank(userPhone)) {
                    try {
                        Order order = orderService.getOrderById(orderMessage.getOrderId(), orderMessage.getQuarter(), OrderUtils.OrderDataLevel.CONDITION, true);
                        userPhone = order.getOrderCondition().getServicePhone().trim();
                    } catch (Exception e) {
                    }
                }
                if (StringUtils.isNotBlank(userPhone)) {
                    orderService.setNewRepeateOrderNo(orderMessage.getCustomer().getId(), userPhone, orderMessage.getOrderNo());
                }
            }catch (Exception e){
                log.error("更新重单检查缓存错误",e);
            }

            //endregion
//
//            MQOrderReport.OrderReport orderReportMessage = null;
//            MQOrderReport.OrderReport.Builder builder;

            //region 客户每日下单报表统计数据
//            step = 3;
//            try {
//                builder = MQOrderReport.OrderReport.newBuilder()
//                        .setOrderId(oid)
//                        .setQuarter(orderMessage.getQuarter())
//                        .setOrderType(Order.ORDER_STATUS_APPROVED)
//                        .setAmount(mqFee.getExpectCharge())
//                        .setQty(1)
//                        .setKefu(MQOrderReport.Kefu.newBuilder()
//                                .setId(orderMessage.getKefu().getId())
//                                .setName(orderMessage.getKefu().getName())
//                                .build()
//                        )
//                        .setTriggerDate(date.getTime())
//                        .setTriggerBy(user.getId());
//                //完整区域
//                List<Area> areas = areaService.getSelfAndParents(orderMessage.getAreaId());
//                if (areas == null || areas.size() < 3) {
//                    log.error("[CreateOrderPushMessageReceiver]检查区域错误,orderId:{},areaId:{}", oid, orderMessage.getAreaId());
//                    throw new RuntimeException("检查区域错误");
//                }
//
//                for (Area area : areas) {
//                    if (area.getType() == 2) {
//                        builder.setProvinceId(area.getId());
//                        builder.setProvinceName(area.getName());
//                    } else if (area.getType() == 3) {
//                        builder.setCityId(area.getId());
//                        builder.setCityName(area.getName());
//                    } else if (area.getType() == 4) {
//                        builder.setAreaId(area.getId());
//                        builder.setAreaName(area.getName());
//                    }
//                }
//                builder.setCustomer(orderMessage.getCustomer());
//                orderReportMessage = builder.build();
//                orderReportSender.send(orderReportMessage);
//            } catch (Exception e) {
//                OrderReport report = new OrderReport();
//                report.setOrderId(oid);
//                report.setDataQuarter(orderMessage.getQuarter());
//                report.setRetryTimes(0);
//                report.setStatus(40);
//                report.setQty(1);
//                report.setAmount(mqFee.getExpectCharge());
//                report.setOrderType(Order.ORDER_STATUS_APPROVED);
//                report.setTriggerBy(user.getId());
//                report.setTriggerDate(date);
//                try {
//                    mqOrderReportService.insert(report);
//                } catch (Exception e1) {
//                    if (orderReportMessage != null) {
//                        log.error("发送客户每日下单报表消息失败,json:{}", new JsonFormat().printToString(orderReportMessage), e1);
//                    } else {
//                        log.error("发送客户每日下单报表消息失败,orderId:{}", oid, e1);
//                    }
//                }
//            }

            //endregion

            //region 客服每日接单报表统计数据
//            step = 4;
//            try {
//                builder = MQOrderReport.OrderReport.newBuilder()
//                        .setOrderId(oid)
//                        .setOrderType(Order.ORDER_STATUS_ACCEPTED)
//                        .setQty(1)
//                        .setAmount(mqFee.getExpectCharge())
//                        .setTriggerDate(new Date().getTime())
//                        .setTriggerBy(user.getId());
//                MQOrderReport.Kefu mqkefu = MQOrderReport.Kefu.newBuilder()
//                        .setId(orderMessage.getKefu().getId())
//                        .setName(orderMessage.getKefu().getName()).build();
//                builder.setKefu(mqkefu);
//                orderReportMessage = builder.build();
//                orderReportSender.send(orderReportMessage);
//            } catch (Exception e) {
//                OrderReport report = new OrderReport();
//                report.setOrderId(oid);
//                report.setDataQuarter(orderMessage.getQuarter());
//                report.setRetryTimes(0);
//                report.setStatus(40);
//                report.setQty(1);
//                report.setAmount(mqFee.getExpectCharge());
//                report.setOrderType(Order.ORDER_STATUS_ACCEPTED);
//                report.setTriggerBy(orderMessage.getKefu().getId());
//                report.setTriggerDate(date);
//                try {
//                    mqOrderReportService.insert(report);
//                } catch (Exception e1) {
//                    if (orderReportMessage != null) {
//                        log.error("发送客服每日接单报表消息失败,json:{}", new JsonFormat().printToString(orderReportMessage), e1);
//                    } else {
//                        log.error("发送客服每日接单报表消息失败,orderId:{}", oid, e1);
//                    }
//                }
//            }
            //endregion

            //region 发送短信及APP推送消息
            step = 5;
            // 内容示例如下：
            // 张三师傅，在您附近有一张上门安装百得油烟机的工单，请尽快登陆APP接单~

            /* todo 提交或发布前取消注释 */
            if (orderMessage.getOrderApproveFlag() == 1 && StringUtils.isNotBlank(orderMessage.getMsgContent())) {//已审核
                try {
                    List<User> engineers = servicePointService.getEngineerAccountsListByAreaAndProductCategory(orderMessage.getAreaId(),orderMessage.getCategoryId());
                    if (engineers != null && engineers.size() > 0) {
                        Message2 shortMessage = new Message2();

                        for (User engineer : engineers) {
                            //手机接单权限
                            if (engineer.getAppFlag() == 0) {
                                continue;
                            }
                            // 短信
                            if (engineer.getShortMessageFlag() == 1) {
                                //shortMessage.setMobile(engineer.getMobile());
                                //shortMessage.setContent(engineer.getName().substring(0, 1).concat(orderMessage.getMsgContent()));
                                //String result = SendMessageUtils.SendMessage(shortMessage);
                                // 改为消息队列发送短信 2019/03/02
//                                smsMQSender.send(
//                                        engineer.getMobile(),
//                                        engineer.getName().substring(0, 1).concat(orderMessage.getMsgContent()),
//                                        "",
//                                        user.getId(),
//                                        System.currentTimeMillis()
//                                );
                                //TODO: 短信类型
                                smsMQSender.sendNew(
                                        engineer.getMobile(),
                                        engineer.getName().substring(0, 1).concat(orderMessage.getMsgContent()),
                                        "",
                                        user.getId(),
                                        System.currentTimeMillis(),
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
                            pushMessage.setDescription(engineer.getName().substring(0, 1).concat(orderMessage.getMsgContent()));
                            appMessagePushService.sendMessage(pushMessage);
                        }
                    }
                } catch (Exception e) {
                    log.error("发送下单短信及APP推送错误,orderId:{}",oid,e);
                }
            }

            //endregion
        }catch (Exception te){
            log.error("下单消息队列处理失败,step:{},json:{}",step,orderMessage!=null?new JsonFormat().printToString(orderMessage):"",te);
        }
    }

    private int getMaxAttempts() {
        return rabbitProperties.getTemplate().getRetry().getMaxAttempts();
    }

    private int getDelaySeconds(int times) {
        return (int) (rabbitProperties.getTemplate().getRetry().getInitialInterval() * rabbitProperties.getTemplate().getRetry().getMultiplier() * times);
    }
}
