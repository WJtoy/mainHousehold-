package com.wolfking.jeesite.modules.mq.receiver;

import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.entity.b2b.mq.B2BMQConstant;
import com.rabbitmq.client.Channel;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.mq.dto.MQOrderGradeMessage;
import com.wolfking.jeesite.modules.mq.entity.OrderCreateBody;
import com.wolfking.jeesite.modules.mq.sender.OrderGradeMessageSender;
import com.wolfking.jeesite.modules.mq.service.OrderCreateMessageService;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.entity.OrderStatus;
import com.wolfking.jeesite.modules.sd.entity.mapper.OrderGradeMessageMapper;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderGradeModel;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.b2bcenter.mq.config.B2BCenterServiceMonitorRetryMQConfig;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;

/**
 * 订单客评重试消息消费者
 */
@Slf4j
@Component
public class OrderGradeRetryMessageReceiver implements ChannelAwareMessageListener {

    @Autowired
    private RabbitProperties rabbitProperties;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderGradeMessageSender orderGradeMessageSender;

    @Autowired
    private OrderCreateMessageService orderCreateMessageService;

    @Override
    public void onMessage(Message message, Channel channel) throws IOException {
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        MQOrderGradeMessage.OrderGradeMessage msg = null;
        try {
            msg = MQOrderGradeMessage.OrderGradeMessage.parseFrom(message.getBody());
            if (msg != null) {
                OrderGradeModel gradeModel = Mappers.getMapper(OrderGradeMessageMapper.class).mqToModel(msg);
                //读取orderStatus,用到reminderStaus，来决定是否调用微服务关闭催单 2019/08/15
                OrderStatus orderStatus = orderService.getOrderStatusById(gradeModel.getOrderId(),gradeModel.getQuarter(),false);
                Order order = new Order(gradeModel.getOrderId());
                order.setQuarter(gradeModel.getQuarter());
                order.setOrderStatus(orderStatus);
                gradeModel.setOrder(order);
                //end 2019/08/15
                orderService.saveGradeRecordAndServicePoint(gradeModel);
            }else{
                log.error("[OrderGradeMessageReceiver]订单客评消息接受失败:消息体解析错误");
            }
        }
        catch (Exception e){
            if(msg != null){
                int times = StringUtils.toInteger(message.getMessageProperties().getHeaders().get(B2BMQConstant.MESSAGE_PROPERTIES_HEADER_KEY_TIMES));
                if (times < B2BCenterServiceMonitorRetryMQConfig.RETRY_TIMES) {
                    times++;
                    orderGradeMessageSender.sendRetry(msg,getDelaySeconds(times), times);
                } else {
                    log.error("订单客评消息处理失败- times:{},orderId:{}",times,msg.getOrderId(),e);
                    try {
                        String msgJson = new JsonFormat().printToString(msg);
                        OrderCreateBody body = new OrderCreateBody();
                        //body.setId(orderMessage.getId());
                        body.setQuarter(msg.getQuarter());
                        body.setOrderId(msg.getOrderId());
                        body.setRemarks("订单客评消息处理失败,请查看日志文件");
                        body.setStatus(40);//fail
                        body.setType(2);
                        User createBy = new User(msg.getCreateBy().getId());
                        createBy.setName(msg.getCreateBy().getName());
                        body.setTriggerBy(createBy);
                        body.setTriggerDate(DateUtils.longToDate(msg.getCreateDate()));
                        body.setCreateBy(createBy);
                        body.setCreateDate(body.getTriggerDate());
                        body.setUpdateDate(new Date());
                        body.setJson(msgJson);

                        orderCreateMessageService.insert(body);
                    }catch (Exception e1){
                        log.error("订单客评重试消息消费失败时，保存失败记录错误:{}",msg.getOrderNo(),e1);
                    }
                    //LogUtils.saveLog("订单客评消息消费失败", "OrderGradeRetryMessageReceiver", msg.getOrderNo(), e, null);
                }
            }else{
                log.error("订单客评消息消费失败,msg为null", e);
            }
        }
    }

    private int getDelaySeconds(int times) {
        return (int) (rabbitProperties.getTemplate().getRetry().getInitialInterval() * rabbitProperties.getTemplate().getRetry().getMultiplier() * times);
    }


}
