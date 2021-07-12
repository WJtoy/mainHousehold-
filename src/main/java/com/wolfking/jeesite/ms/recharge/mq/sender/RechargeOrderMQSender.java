package com.wolfking.jeesite.ms.recharge.mq.sender;

import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.entity.b2b.mq.B2BMQConstant;
import com.kkl.kklplus.entity.fi.mq.MQConstant;
import com.kkl.kklplus.entity.fi.mq.MQRechargeOrderMessage;
import com.wolfking.jeesite.common.utils.Exceptions;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RechargeOrderMQSender implements RabbitTemplate.ConfirmCallback{


    private RabbitTemplate rabbitTemplate;
    private RetryTemplate retryTemplate;

    @Autowired
    public RechargeOrderMQSender(RabbitTemplate manualRabbitTemplate, RetryTemplate kklRabbitRetryTemplate) {
        this.rabbitTemplate = manualRabbitTemplate;
        this.rabbitTemplate.setConfirmCallback(this);
        this.retryTemplate = kklRabbitRetryTemplate;
    }

    /**
     * 延迟发送消息
     *
     * @param message 消息体
     * @param delay   延迟时间(毫秒)
     * @param times   第几次发送
     */
    public void sendDelay(MQRechargeOrderMessage.RechargeOrderMessage message, int delay, int times) {
        try {
            retryTemplate.execute((RetryCallback<Object, Exception>) context -> {
                context.setAttribute(B2BMQConstant.RETRY_CONTEXT_ATTRIBUTE_KEY_MESSAGE, message);
                rabbitTemplate.convertAndSend(
                        MQConstant.MS_MQ_RECHARGE_NOTIFY_RETRY,
                        MQConstant.MS_MQ_RECHARGE_NOTIFY_RETRY,
                        message.toByteArray(), msg -> {
                            msg.getMessageProperties().setDelay(delay);
                            msg.getMessageProperties().setHeader(B2BMQConstant.MESSAGE_PROPERTIES_HEADER_KEY_TIMES, times);
                            msg.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                            return msg;
                        }, new CorrelationData());
                return null;
            }, context -> {
                Object msgObj = context.getAttribute(B2BMQConstant.RETRY_CONTEXT_ATTRIBUTE_KEY_MESSAGE);
                MQRechargeOrderMessage.RechargeOrderMessage msg = MQRechargeOrderMessage.RechargeOrderMessage.parseFrom((byte[]) msgObj);
                Throwable throwable = context.getLastThrowable();
                String msgJson = new JsonFormat().printToString(msg);
                LogUtils.saveLog("充值异步通知重试发送消息队列失败","RechargeOrderMQSender.sendDelay",msgJson,null,null);
                log.error("delay send error {}, {}", Exceptions.getStackTraceAsString(throwable), msg);
                return null;
            });
        } catch (Exception e) {
            LogUtils.saveLog("充值异步通知重试发送消息队列失败","RechargeOrderMQSender.sendDelay","",e,null);
            log.error("RechargeOrderMQSender.sendDelay:{}", Exceptions.getStackTraceAsString(e));
        }

    }

    @Override
    public void confirm(CorrelationData correlationData, boolean b, String s) {

    }
}
