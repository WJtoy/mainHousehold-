package com.wolfking.jeesite.ms.tmall.mq.sender;

import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.entity.b2b.common.B2BProcessFlag;
import com.kkl.kklplus.entity.b2b.mq.B2BMQConstant;
import com.kkl.kklplus.entity.b2b.mq.B2BMQQueueType;
import com.kkl.kklplus.entity.b2b.pb.MQWorkcardStatusUpdateMessage;
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
public class WorkcardStatusUpdateMQSender implements RabbitTemplate.ConfirmCallback {

    @Autowired
    private MqB2bTmallLogService mqB2bTmallLogService;

    private RabbitTemplate rabbitTemplate;
    private RetryTemplate retryTemplate;

    @Autowired
    public WorkcardStatusUpdateMQSender(RabbitTemplate manualRabbitTemplate, RetryTemplate kklRabbitRetryTemplate) {
        this.rabbitTemplate = manualRabbitTemplate;
        this.rabbitTemplate.setConfirmCallback(this);
        this.retryTemplate = kklRabbitRetryTemplate;
    }

    /**
     * 正常发送消息
     *
     * @param message 消息体
     */
    public void send(MQWorkcardStatusUpdateMessage.WorkcardStatusUpdateMessage message) {
        try {
            retryTemplate.execute((RetryCallback<Object, Exception>) context -> {
                context.setAttribute(B2BMQConstant.RETRY_CONTEXT_ATTRIBUTE_KEY_MESSAGE, message);
                rabbitTemplate.convertAndSend(
                        B2BMQConstant.MQ_WORKCARDSTATUSUPDATE_EXCHANGE,
                        B2BMQConstant.MQ_WORKCARDSTATUSUPDATE_ROUTING,
                        message.toByteArray(),
                        new CorrelationData());
                return null;
            }, context -> {
                Object msgObj = context.getAttribute(B2BMQConstant.RETRY_CONTEXT_ATTRIBUTE_KEY_MESSAGE);
                MQWorkcardStatusUpdateMessage.WorkcardStatusUpdateMessage msg = MQWorkcardStatusUpdateMessage.WorkcardStatusUpdateMessage.parseFrom((byte[]) msgObj);
                Throwable throwable = context.getLastThrowable();
                log.error("normal send error {}, {}", throwable.getLocalizedMessage(), msg);
                String msgJson = new JsonFormat().printToString(msg);
                B2BFailureLogUtils.saveFailureLog(B2BMQQueueType.WORKCARD_STATUS_UPDATE, msgJson, msg.getUpdateId(),
                        B2BProcessFlag.PROCESS_FLAG_FAILURE, 0,
                        "normal send error -- " + throwable.getLocalizedMessage());
                return null;
            });
        } catch (Exception e) {
            log.error("WorkcardStatusUpdateMQSender.send", e);
        }
    }

    /**
     * 延迟发送消息
     *
     * @param message 消息体
     * @param delay   延迟时间
     * @param times   第几次发送
     */
    public void sendDelay(MQWorkcardStatusUpdateMessage.WorkcardStatusUpdateMessage message, int delay, int times) {
        try {
            retryTemplate.execute((RetryCallback<Object, Exception>) context -> {
                context.setAttribute(B2BMQConstant.RETRY_CONTEXT_ATTRIBUTE_KEY_MESSAGE, message);
                rabbitTemplate.convertAndSend(
                        B2BMQConstant.MQ_DELAY_WORKCARDSTATUSUPDATE_EXCHANGE,
                        B2BMQConstant.MQ_DELAY_WORKCARDSTATUSUPDATE_ROUTING,
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
                B2BFailureLogUtils.saveFailureLog(B2BMQQueueType.DELAY_WORKCARD_STATUS_UPDATE, msgJson, msg.getUpdateId(),
                        B2BProcessFlag.PROCESS_FLAG_FAILURE, 0,
                        "delay send error -- " + throwable.getLocalizedMessage());
                log.error("delay send error {}, {}", throwable.getLocalizedMessage(), msg);
                return null;
            });
        } catch (Exception e) {
            log.error("WorkcardStatusUpdateMQSender.sendDelay", e);
        }

    }

    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {

    }
}
