package com.wolfking.jeesite.ms.tmall.mq.sender;

import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.entity.b2b.common.B2BProcessFlag;
import com.kkl.kklplus.entity.b2b.mq.B2BMQConstant;
import com.kkl.kklplus.entity.b2b.mq.B2BMQQueueType;
import com.kkl.kklplus.entity.b2b.pb.MQTmallPushWorkcardMessage;
import com.kkl.kklplus.entity.b2b.pb.MQWorkcardStatusUpdateMessage;
import com.wolfking.jeesite.common.utils.Exceptions;
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

/**
 * 天猫推送订单消息重送
 */
@Slf4j
@Component
public class TMallWorkcardMQSender implements RabbitTemplate.ConfirmCallback {

    @Autowired
    private MqB2bTmallLogService mqB2bTmallLogService;

    private RabbitTemplate rabbitTemplate;
    private RetryTemplate retryTemplate;

    @Autowired
    public TMallWorkcardMQSender(RabbitTemplate manualRabbitTemplate, RetryTemplate kklRabbitRetryTemplate) {
        this.rabbitTemplate = manualRabbitTemplate;
        this.rabbitTemplate.setConfirmCallback(this);
        this.retryTemplate = kklRabbitRetryTemplate;
    }

    /**
     * 正常发送消息
     *
     * @param message 消息体
     */
    public void send(MQTmallPushWorkcardMessage.TmallPushWorkcardMessage message) {
        try {
            retryTemplate.execute((RetryCallback<Object, Exception>) context -> {
                context.setAttribute(B2BMQConstant.RETRY_CONTEXT_ATTRIBUTE_KEY_MESSAGE, message);
                rabbitTemplate.convertAndSend(
                        B2BMQConstant.MQ_TMALLPUSH_WORKCARD_EXCHANGE,
                        B2BMQConstant.MQ_TMALLPUSH_WORKCARD_ROUTING,
                        message.toByteArray(),
                        new CorrelationData());
                return null;
            }, context -> {
                Object msgObj = context.getAttribute(B2BMQConstant.RETRY_CONTEXT_ATTRIBUTE_KEY_MESSAGE);
                MQTmallPushWorkcardMessage.TmallPushWorkcardMessage msg = MQTmallPushWorkcardMessage.TmallPushWorkcardMessage.parseFrom((byte[]) msgObj);
                Throwable throwable = context.getLastThrowable();
                log.error("normal send error {}, {}", throwable.getLocalizedMessage(), msg);
                String msgJson = new JsonFormat().printToString(msg);
                B2BFailureLogUtils.saveFailureLog(B2BMQQueueType.TMALLPUSH_WORKCARD, msgJson, 3l,
                        B2BProcessFlag.PROCESS_FLAG_FAILURE, 0,
                        "normal send error -- " + throwable.getLocalizedMessage());
                return null;
            });
        } catch (Exception e) {
            log.error("TMallWorkcardMQSender", e);
        }
    }


    /**
     * 延迟发送消息
     *
     * @param message 消息体
     * @param delay   延迟时间
     * @param times   第几次发送
     */
    public void sendDelay(MQTmallPushWorkcardMessage.TmallPushWorkcardMessage message, int delay, int times) {
        try {
            retryTemplate.execute((RetryCallback<Object, Exception>) context -> {
                context.setAttribute(B2BMQConstant.RETRY_CONTEXT_ATTRIBUTE_KEY_MESSAGE, message);
                rabbitTemplate.convertAndSend(
                        B2BMQConstant.MQ_DELAY_TMALLPUSH_WORKCARD_EXCHANGE,
                        B2BMQConstant.MQ_DELAY_TMALLPUSH_WORKCARD_ROUTING,
                        message.toByteArray(), msg -> {
                            msg.getMessageProperties().setDelay(delay * 1000);
                            msg.getMessageProperties().setHeader(B2BMQConstant.MESSAGE_PROPERTIES_HEADER_KEY_TIMES, times);
                            msg.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                            return msg;
                        }, new CorrelationData());
                return null;
            }, context -> {
                Object msgObj = context.getAttribute(B2BMQConstant.RETRY_CONTEXT_ATTRIBUTE_KEY_MESSAGE);
                MQWorkcardStatusUpdateMessage.WorkcardStatusUpdateMessage msg = MQWorkcardStatusUpdateMessage.WorkcardStatusUpdateMessage.parseFrom((byte[]) msgObj);
                Throwable throwable = context.getLastThrowable();
                String msgJson = new JsonFormat().printToString(msg);
                B2BFailureLogUtils.saveFailureLog(B2BMQQueueType.TMALLPUSH_WORKCARD, msgJson, msg.getUpdateId(),
                        B2BProcessFlag.PROCESS_FLAG_FAILURE, 0,
                        "delay send error -- " + throwable.getLocalizedMessage());
                log.error("delay send error {}, {}", Exceptions.getStackTraceAsString(throwable), msg);
                return null;
            });
        } catch (Exception e) {
            log.error("TMallWorkcardMQSender.sendDelay:{}", Exceptions.getStackTraceAsString(e));
        }

    }

    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {

    }
}
