package com.wolfking.jeesite.modules.mq.receiver;

import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.entity.b2b.mq.B2BMQConstant;
import com.rabbitmq.client.Channel;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.mq.dto.MQOrderAutoPlanMessage;
import com.wolfking.jeesite.modules.mq.sender.OrderAutoPlanMessageSender;
import com.wolfking.jeesite.modules.mq.service.OrderAutoPlanMessageService;
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
 * 订单自动派单消息消费者(延迟队列)
 */
@Slf4j
@Component
public class OrderAutoPlanMessageReceiver implements ChannelAwareMessageListener {

    @Autowired
    private OrderAutoPlanMessageSender sender;

    @Autowired
    private RabbitProperties rabbitProperties;

    @Autowired
    private OrderAutoPlanMessageService businessService;

    @Override
    public void onMessage(Message message, Channel channel) throws IOException {
        MQOrderAutoPlanMessage.OrderAutoPlan msg = null;
        try {
            msg = MQOrderAutoPlanMessage.OrderAutoPlan.parseFrom(message.getBody());
            if (msg == null) {
                log.error("[OrderAutoPlanMessageReceiver]消息接受失败:消息体解析错误");
                return;
            }
            if(msg.getAreaId()<=0){
                log.error("消息消费失败, 无区域id,msg:{}", new JsonFormat().printToString(msg));
                return;
            }
            /*
            //  mark on 2019-5-30
            //无经纬度
            if(msg.getLongitude() == 0 || msg.getLatitude() == 0 || msg.getAreaId()<=0){
                log.error("消息消费失败,经纬度无内容，或无区域id,msg:{}", new JsonFormat().printToString(msg));
                return;
            }else if(msg.getAreaRadius() == null || msg.getAreaRadius().getRadius1()<=0){
                log.error("消息消费失败:未设定搜索半径,msg:{}", new JsonFormat().printToString(msg));
                return;
            }
            */
            //todo 处理逻辑
            businessService.processMessage(msg);
        }
        catch (org.springframework.dao.DuplicateKeyException e){
            log.error("消息重复处理，数据重复",e);
        }
        catch (Exception e){
            if(msg != null){
                // 确认消息已经消费成功,发到重试队列
                log.error("消息处理失败,body:{}", new JsonFormat().printToString(msg),e);
                int times = StringUtils.toInteger(message.getMessageProperties().getHeaders().get(B2BMQConstant.MESSAGE_PROPERTIES_HEADER_KEY_TIMES));
                if (times < B2BCenterServiceMonitorRetryMQConfig.RETRY_TIMES) {
                    times++;
                    sender.sendRetry(msg,getDelaySeconds(times), times);
                } else {
                    log.error("达到重试上限 - times:{},orderId:{}",times,msg.getOrderId(),e);
                    String msgJson = new JsonFormat().printToString(msg);
                    try {
                        LogUtils.saveLog("OrderAutoPlanMessage","onMessage",msgJson,e,null);
                    }catch (Exception e1){
                        log.error("保存日志错误:orderId:{},body:{}",msg.getOrderId(),msgJson,e);
                    }
                }
            }else{
                log.error("消息消费失败,msg为null", e);
            }
        }finally {
            // 确认消息已经消费成功
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }
    }

    private int getDelaySeconds(int times) {
        return (int) (rabbitProperties.getTemplate().getRetry().getInitialInterval() * rabbitProperties.getTemplate().getRetry().getMultiplier() * times);
    }

}
