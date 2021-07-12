package com.wolfking.jeesite.ms.b2bcenter.mq.sender;

import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.entity.b2b.common.B2BProcessFlag;
import com.kkl.kklplus.entity.b2b.mq.B2BMQConstant;
import com.kkl.kklplus.entity.b2b.mq.B2BMQQueueType;
import com.kkl.kklplus.entity.b2bcenter.pb.MQB2BOrderDismountReturnMessage;
import com.wolfking.jeesite.common.utils.Exceptions;
import com.wolfking.jeesite.ms.utils.B2BFailureLogUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

/**
 * B2B-退换货流程消息生产者
 * @author Ryan
 * @date 2020-10-28
 */
@Slf4j
@Component
public class B2BCenterOrderDismountReturnMQSender implements RabbitTemplate.ConfirmCallback {

    
    private RabbitTemplate rabbitTemplate;
    private RetryTemplate retryTemplate;

    @Autowired
    public B2BCenterOrderDismountReturnMQSender(RabbitTemplate manualRabbitTemplate, RetryTemplate kklRabbitRetryTemplate) {
        this.rabbitTemplate = manualRabbitTemplate;
        this.rabbitTemplate.setConfirmCallback(this);
        this.retryTemplate = kklRabbitRetryTemplate;
    }
    

    /**
     * 延迟发送消息
     *
     * @param message 消息体
     * @param delay   延迟时间
     * @param times   第几次发送
     */
    public void sendDelay(MQB2BOrderDismountReturnMessage.B2BOrderDismountReturnMessage message, int delay, int times) {
        try {
            retryTemplate.execute((RetryCallback<Object, Exception>) context -> {
                context.setAttribute(B2BMQConstant.RETRY_CONTEXT_ATTRIBUTE_KEY_MESSAGE, message);
                rabbitTemplate.convertAndSend(
                        B2BMQConstant.MQ_B2BCENTER_ORDER_RETURN_DELAY,
                        B2BMQConstant.MQ_B2BCENTER_ORDER_RETURN_DELAY,
                        message.toByteArray(), msg -> {
                            msg.getMessageProperties().setDelay(delay * 1000);
                            msg.getMessageProperties().setHeader(B2BMQConstant.MESSAGE_PROPERTIES_HEADER_KEY_TIMES, times);
                            msg.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                            return msg;
                        }, new CorrelationData());
                return null;
            }, context -> {
                Object msgObj = context.getAttribute(B2BMQConstant.RETRY_CONTEXT_ATTRIBUTE_KEY_MESSAGE);
                MQB2BOrderDismountReturnMessage.B2BOrderDismountReturnMessage msg = MQB2BOrderDismountReturnMessage.B2BOrderDismountReturnMessage.parseFrom((byte[]) msgObj);
                Throwable throwable = context.getLastThrowable();
                String msgJson = new JsonFormat().printToString(msg);
                B2BFailureLogUtils.saveFailureLog(B2BMQQueueType.B2BCENTER_ORDER_DISMOUNT_AND_RETURN_RETRY, msgJson, 3L,
                        B2BProcessFlag.PROCESS_FLAG_FAILURE, 0,
                        "delay send error -- " + throwable.getLocalizedMessage());
                log.error("delay send error {}, {}", Exceptions.getStackTraceAsString(throwable), msg);
                return null;
            });
        } catch (Exception e) {
            StringBuilder json = new StringBuilder(1000);
            if(message != null){
                json.append(new JsonFormat().printToString(message));
            }
            log.error("B2BOrderDismountReturnMQSender data:{}", json.toString(),e);
        }
    }

    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {

    }
}
