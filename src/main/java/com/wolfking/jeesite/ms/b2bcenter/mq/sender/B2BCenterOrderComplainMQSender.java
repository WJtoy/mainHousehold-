package com.wolfking.jeesite.ms.b2bcenter.mq.sender;

import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.entity.b2b.common.B2BProcessFlag;
import com.kkl.kklplus.entity.b2b.mq.B2BMQConstant;
import com.kkl.kklplus.entity.b2b.mq.B2BMQQueueType;
import com.kkl.kklplus.entity.b2bcenter.pb.MQB2BCenterOrderExpressArrivalMessage;
import com.kkl.kklplus.entity.b2bcenter.pb.MQB2BOrderComplainMessage;
import com.wolfking.jeesite.common.utils.Exceptions;
import com.wolfking.jeesite.ms.b2bcenter.mq.config.B2BCenterOrderComplainConfig;
import com.wolfking.jeesite.ms.b2bcenter.mq.config.B2BCenterOrderExpressArrivalRetryMQConfig;
import com.wolfking.jeesite.ms.tmall.mq.service.MqB2bTmallLogService;
import com.wolfking.jeesite.ms.utils.B2BFailureLogUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class B2BCenterOrderComplainMQSender implements RabbitTemplate.ConfirmCallback {

    private RabbitTemplate rabbitTemplate;
    private RetryTemplate retryTemplate;

    @Autowired
    public B2BCenterOrderComplainMQSender(RabbitTemplate manualRabbitTemplate, RetryTemplate kklRabbitRetryTemplate) {
        this.rabbitTemplate = manualRabbitTemplate;
        this.rabbitTemplate.setConfirmCallback(this);
        this.retryTemplate = kklRabbitRetryTemplate;
    }

    /**
     * 延迟发送消息
     *
     * @param message 消息体
     * @param times   第几次发送
     */
    public void sendRetry(MQB2BOrderComplainMessage.B2BOrderComplainMessage message, int times) {
        try {
            retryTemplate.execute((RetryCallback<Object, Exception>) context -> {
                context.setAttribute(B2BMQConstant.RETRY_CONTEXT_ATTRIBUTE_KEY_MESSAGE, message);
                rabbitTemplate.convertAndSend(
                        B2BMQConstant.MQ_B2BCENTER_COMPLAIN_DELAY,
                        B2BMQConstant.MQ_B2BCENTER_COMPLAIN_DELAY,
                        message.toByteArray(), msg -> {
                            msg.getMessageProperties().setDelay(B2BCenterOrderComplainConfig.DELAY_MILLISECOND);
                            msg.getMessageProperties().setHeader(B2BMQConstant.MESSAGE_PROPERTIES_HEADER_KEY_TIMES, times);
                            msg.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                            return msg;
                        }, new CorrelationData());
                return null;
            }, context -> {
                Object msgObj = context.getAttribute(B2BMQConstant.RETRY_CONTEXT_ATTRIBUTE_KEY_MESSAGE);
                MQB2BOrderComplainMessage.B2BOrderComplainMessage msg = MQB2BOrderComplainMessage.B2BOrderComplainMessage.parseFrom((byte[]) msgObj);
                String msgJson = new JsonFormat().printToString(msg);
                String errorMsg = Exceptions.getStackTraceAsString(context.getLastThrowable());
                log.error("【B2BCenterOrderComplainMQSender.sendDelay_1】msgJson: {}, errorMsg: {}", msgJson, errorMsg);
                B2BFailureLogUtils.saveFailureLog(B2BMQQueueType.B2BCENTER_ORDER_COMPLAIN_RETRY, msgJson, 3L,
                        B2BProcessFlag.PROCESS_FLAG_FAILURE, 0, errorMsg);
                return null;
            });
        } catch (Exception e) {
            String msgJson = new JsonFormat().printToString(message);
            String errorMsg = Exceptions.getStackTraceAsString(e);
            log.error("【B2BCenterOrderComplainMQSender.sendDelay_2】msgJson: {}, errorMsg: {}", msgJson, errorMsg);
            B2BFailureLogUtils.saveFailureLog(B2BMQQueueType.B2BCENTER_ORDER_COMPLAIN_RETRY, msgJson, 3L,
                    B2BProcessFlag.PROCESS_FLAG_FAILURE, 0, "【B2BCenterOrderComplainMQSender.sendDelay_2】" + errorMsg);
        }

    }

    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
    }
}
