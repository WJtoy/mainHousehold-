package com.wolfking.jeesite.modules.mq.receiver.sms;

import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.entity.sys.SMSCallbackActionTypeEnum;
import com.kkl.kklplus.entity.sys.mq.MQSysShortMessage;
import com.kkl.kklplus.entity.voiceservice.CallbackType;
import com.kkl.kklplus.entity.voiceservice.mq.MQSmsCallbackMessage;
import com.rabbitmq.client.Channel;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.entity.OrderCondition;
import com.wolfking.jeesite.modules.sd.entity.OrderProcessLog;
import com.wolfking.jeesite.modules.sd.service.OrderGradeService;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.httpclient.NameValuePair;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * 非客评短信回复消息消费者
 * @author Ryan
 * @date 2019/03/06
 */
@Slf4j
@Configuration
@Component
public class SmsCallbackNoGradeReceiver implements ChannelAwareMessageListener {

    @Autowired
    private OrderService orderService;

    @Autowired
    private SmsBusinessService businessService;

    @Value("${site.code}")
    private String siteCode;

    private static final User user = new User(2l, "短信回复", "");

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        //先确认
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

        MQSysShortMessage.SysShortMessage callbackEntity = MQSysShortMessage.SysShortMessage.parseFrom(message.getBody());
        if(callbackEntity == null) {
            return;
        }
        // 消息处理
        onAction(callbackEntity);
    }

    /**
     * 处理接收的消息
     */
    private void onAction(MQSysShortMessage.SysShortMessage callbackEntity){

        String json = new JsonFormat().printToString(callbackEntity);
        Date date = DateUtils.longToDate(callbackEntity.getTriggerDate());
        //region 业务处理部分
        try {
            // 按电话号码查找订单(可能返回多个)
            List<OrderCondition>  orders = orderService.getToGradeOrdersByPhoneAndDate(callbackEntity.getMobile(),date);
            //没找到订单，忽略
            if (orders == null || orders.size() == 0) {
                log.error("回复短信，但未找到订单");
                return;
            }
            //按优先级找到需要的订单
            OrderCondition orderCondition = getToGradeOrder(orders);
            /*待回访
            if(orderCondition.getSubStatus() == Order.ORDER_SUBSTATUS_APPCOMPLETED){
                //客评
                MQSmsCallbackMessage.SmsCallbackEntity.Builder smsGradeBuilder = MQSmsCallbackMessage.SmsCallbackEntity.newBuilder()
                        .setOrderId(orderCondition.getOrderId())
                        .setQuarter(orderCondition.getQuarter())
                        .setContent(callbackEntity.getContent())
                        .setSendedAt(callbackEntity.getTriggerDate());
                int score = businessService.smsContentToScore(callbackEntity.getContent());
                String labelling = businessService.scoreToGradeContent(score,callbackEntity.getContent());
                smsGradeBuilder.setScore(score)
                                .setLabelling(org.apache.commons.lang3.StringUtils.left(labelling,150));

                businessService.autoGradeAction(smsGradeBuilder.build());
                return;
            }
            int score = businessService.smsContentToScore(callbackEntity.getContent());
            if (score == 3){
                try{
                    Order order = orderService.getOrderById(orderCondition.getOrderId(),orderCondition.getQuarter(), OrderUtils.OrderDataLevel.CONDITION, orderCondition.getStatusValue() < 80 ? true : false);
                    String labelling = businessService.scoreToGradeContent(score,callbackEntity.getContent());
                    businessService.smsToComplain(order.getOrderCondition(),labelling);
                }catch (Exception e){
                    log.error("短信回复[3]转投诉失败-读取订单错误:{}",orderCondition.getOrderId(),e);
                }
            }
            //其他，记录日志并标记异常
            businessService.smsSaveOrderLogAndAppAbnormal(
                    orderCondition.getCustomer().getId(),
                    orderCondition.getArea().getId(),
                    orderCondition.getKefu().getId(),
                    orderCondition.getOrderId(),
                    orderCondition.getQuarter(),
                    orderCondition.getStatusValue(),
                    callbackEntity.getContent(),
                    user
            );
             */
            //转投诉单
            if (callbackEntity.getCallbackActionType() == SMSCallbackActionTypeEnum.COMPLAIN.getValue()){
                try{
                    Order order = orderService.getOrderById(orderCondition.getOrderId(),orderCondition.getQuarter(), OrderUtils.OrderDataLevel.CONDITION, orderCondition.getStatusValue() < 80 ? true : false);
                    //记录日志
                    businessService.smsSaveOrderLog(
                            orderCondition.getCustomer().getId(),
                            orderCondition.getOrderId(),
                            orderCondition.getQuarter(),
                            orderCondition.getStatusValue(),
                            callbackEntity.getContent(),
                            user
                    );
                    businessService.smsToComplain(order.getOrderCondition(),String.format("用户回复: %s",callbackEntity.getContent()));
                }catch (Exception e){
                    log.error("短信回复[3]转投诉失败-读取订单错误:{}",orderCondition.getOrderId(),e);
                }
            } else if(callbackEntity.getCallbackActionType() == SMSCallbackActionTypeEnum.ABNORMAL.getValue()){
                //标记异常
                businessService.smsSaveOrderLogAndAppAbnormal(
                        orderCondition.getCustomer().getId(),
                        orderCondition.getArea().getId(),
                        orderCondition.getKefu().getId(),
                        orderCondition.getOrderId(),
                        orderCondition.getQuarter(),
                        orderCondition.getStatusValue(),
                        callbackEntity.getContent(),
                        user
                );
            } else {
                //记录日志
                businessService.smsSaveOrderLog(
                        orderCondition.getCustomer().getId(),
                        orderCondition.getOrderId(),
                        orderCondition.getQuarter(),
                        orderCondition.getStatusValue(),
                        callbackEntity.getContent(),
                        user
                );
            }
        } catch (Exception e){
            log.error("短信回复:处理失败,msg:{}",json,e);
            try {
                LogUtils.saveLog("短信回复:处理失败","SmsCallbackNoGradeReceiver.onAction",json,e,user);
            }catch (Exception ex){}
        }

        //endregion
    }

    /**
     * 取得订单
     * 1.先待客评(grade_flag=2) -> grade_flag =0 && sub_status = 70
     * 2.未客评且已上门(grade_flag=0)
     * 2.未客评(grade_flag=0)
     * 3.已客评
     * 按订单id倒序，优先最新的订单
     * @param orders
     * @return
     */
    private OrderCondition getToGradeOrder(List<OrderCondition> orders){
        if(orders==null || orders.size()==0){
            return null;
        }
        if(orders.size()==1){
            return orders.get(0);
        }
        //待回访,sub_status=70
        OrderCondition order = orders.stream().filter(t->t.getGradeFlag()==0 && t.getSubStatus() == Order.ORDER_SUBSTATUS_APPCOMPLETED)
                .sorted(Comparator.comparing(OrderCondition::getOrderId).reversed())
                .findFirst().orElse(null);
        if(order != null){
            return order;
        }
        //grade_flag = 0 且 上门服务
        //TODO: APP完工[55]
//        order = orders.stream().filter(t->t.getGradeFlag()==0 && t.getStatusValue() == Order.ORDER_STATUS_SERVICED)
//                .sorted(Comparator.comparing(OrderCondition::getOrderId).reversed())
//                .findFirst().orElse(null);
        order = orders.stream().filter(t->t.getGradeFlag()==0 && (t.getStatusValue() == Order.ORDER_STATUS_SERVICED || t.getStatusValue() == Order.ORDER_STATUS_APP_COMPLETED))
                .sorted(Comparator.comparing(OrderCondition::getOrderId).reversed())
                .findFirst().orElse(null);
        if(order != null){
            return order;
        }
        //grade_flag = 0
        order = orders.stream().filter(t->t.getGradeFlag()==0)
                .sorted(Comparator.comparing(OrderCondition::getOrderId).reversed())
                .findFirst().orElse(null);
        if(order != null){
            return order;
        }
        order = orders.stream().filter(t->t.getGradeFlag() > 0)
                .sorted(Comparator.comparing(OrderCondition::getOrderId).reversed())
                .findFirst().orElse(null);
        return order;
    }
}
