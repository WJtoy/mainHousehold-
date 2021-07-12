package com.wolfking.jeesite.modules.mq.receiver;

import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.entity.b2b.mq.B2BMQConstant;
import com.rabbitmq.client.Channel;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.mq.dto.MQOrderServicePointMessage;
import com.wolfking.jeesite.modules.mq.sender.OrderServicePointMessageSender;
import com.wolfking.jeesite.modules.mq.service.ServicePointOrderBusinessService;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.ms.b2bcenter.mq.config.B2BCenterServiceMonitorRetryMQConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 订单网点消息消费者(延迟队列)
 */
@Slf4j
@Component
public class OrderServicePointMessageReceiver implements ChannelAwareMessageListener {

    @Autowired
    private OrderServicePointMessageSender sender;

    @Autowired
    private RabbitProperties rabbitProperties;

    @Autowired
    private ServicePointOrderBusinessService businessService;

    @Override
    public void onMessage(Message message, Channel channel) throws IOException {
        MQOrderServicePointMessage.ServicePointMessage msg = null;
        try {
            msg = MQOrderServicePointMessage.ServicePointMessage.parseFrom(message.getBody());
            if (msg == null) {
                log.error("[OrderServicePointMessageReceiver]消息接受失败:消息体解析错误");
                // 确认消息已经消费成功
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                return;
            }
            //处理逻辑
            businessService.processMessage(msg);
            // 确认消息已经消费成功
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (org.springframework.dao.DuplicateKeyException e){
            // 确认消息已经消费成功
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            log.error("消息重复处理，数据重复:orderId:{} spId:{}",msg.getOrderId(),msg.getServicePointInfo().getServicePointId());
        } catch (Exception e){
            if(msg != null){
                // 确认消息已经消费成功,发到重试队列
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                log.error("消息处理失败,body:{}", new JsonFormat().printToString(msg),e);
                int times = StringUtils.toInteger(message.getMessageProperties().getHeaders().get(B2BMQConstant.MESSAGE_PROPERTIES_HEADER_KEY_TIMES));
                if (times < B2BCenterServiceMonitorRetryMQConfig.RETRY_TIMES) {
                    times++;
                    sender.sendRetry(msg,getDelaySeconds(times), times);
                } else {
                    log.error("达到重试上限 - times:{},orderId:{}",times,msg.getOrderId(),e);
                    String msgJson = new JsonFormat().printToString(msg);
                    try {
                        LogUtils.saveLog("MQOrderServicePointMessage","onMessage",msgJson,e,null);
                    }catch (Exception e1){
                        log.error("保存日志错误:orderId:{},body:{}",msg.getOrderId(),msgJson,e);
                    }
                }
            }else{
                //  确认消息已经消费消费失败，将消息发给下一个消费者
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);
                log.error("消息消费失败,msg为null", e);
            }
        }
    }

    private int getDelaySeconds(int times) {
        return (int) (rabbitProperties.getTemplate().getRetry().getInitialInterval() * rabbitProperties.getTemplate().getRetry().getMultiplier() * times);
    }

}
