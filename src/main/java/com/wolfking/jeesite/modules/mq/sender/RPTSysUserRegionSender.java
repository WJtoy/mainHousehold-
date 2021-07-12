package com.wolfking.jeesite.modules.mq.sender;

import com.kkl.kklplus.entity.sys.mq.MQConstant;
import com.kkl.kklplus.entity.sys.mq.MQSysUserRegionMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

/**
 * 报表用户区域消息发送
 */
@Component
@Slf4j
public class RPTSysUserRegionSender implements RabbitTemplate.ConfirmCallback {
    private RabbitTemplate rabbitTemplate;
    private RetryTemplate retryTemplate;

    @Autowired
    public RPTSysUserRegionSender(RabbitTemplate manualRabbitTemplate, RetryTemplate kklRabbitRetryTemplate) {
        this.rabbitTemplate = manualRabbitTemplate;
        this.rabbitTemplate.setConfirmCallback(this);
        this.retryTemplate = kklRabbitRetryTemplate;
    }

    public void send(MQSysUserRegionMessage.SysUserRegionMessage message) {
        try {
            retryTemplate.execute((RetryCallback<Object,Exception>)context->{
               context.setAttribute("message", message);
               rabbitTemplate.convertAndSend(MQConstant.MS_MQ_SYS_USER_REGION, MQConstant.MS_MQ_SYS_USER_REGION, message.toByteArray(), msg->{
                   msg.getMessageProperties().setDelay(1000);
                   msg.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                   return msg;
               }, new CorrelationData());
               return null;
            }, context->{
                MQSysUserRegionMessage.SysUserRegionMessage sysUserRegionMessage = MQSysUserRegionMessage.SysUserRegionMessage.parseFrom((byte[])context.getAttribute("message"));
                Throwable throwable = context.getLastThrowable();
                log.error("RPTSysUserRegionSender send error{}, {}", throwable.getLocalizedMessage(), sysUserRegionMessage);
                return null;
            });
        } catch (Exception ex) {
            log.error("RPTSysUserRegionSender.send：{}", ex.getLocalizedMessage());
        }
    }




    @Override
    public void confirm(CorrelationData correlationData, boolean b, String s) {

    }
}
