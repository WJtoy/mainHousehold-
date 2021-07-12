package com.wolfking.jeesite.ms.tmall.mq.sender;


import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.entity.b2b.common.B2BProcessFlag;
import com.kkl.kklplus.entity.b2b.mq.B2BMQConstant;
import com.kkl.kklplus.entity.b2b.mq.B2BMQQueueType;
import com.kkl.kklplus.entity.b2b.pb.MQTmallPushWorkcardUpdateMessage;
import com.wolfking.jeesite.ms.tmall.mq.service.MqB2bTmallLogService;
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
public class TMallWorkUpdateMQSender implements RabbitTemplate.ConfirmCallback{

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RetryTemplate retryTemplate;

    @Autowired
    private MqB2bTmallLogService mqB2bTmallLogService;

    /**
     * 延迟发送消息
     *
     * @param message 消息体
     * @param delay   延迟时间
     * @param times   第几次发送
     */
    public void sendDelay(MQTmallPushWorkcardUpdateMessage.TmallPushWorkcardUpdateMessage message, int delay, int times) {
        /*
        try {
            retryTemplate.execute((RetryCallback<Object, Exception>) context -> {
                context.setAttribute(B2BMQConstant.RETRY_CONTEXT_ATTRIBUTE_KEY_MESSAGE, message);
                rabbitTemplate.convertAndSend(
                        B2BMQConstant.MQ_DELAY_TMALLPUSH_WORKCARDUPDATE_EXCHANGE,
                        B2BMQConstant.MQ_DELAY_TMALLPUSH_WORKCARDUPDATE_ROUTING,
                        message.toByteArray(), msg -> {
                            msg.getMessageProperties().setDelay(delay * 1000);
                            msg.getMessageProperties().setHeader(B2BMQConstant.MESSAGE_PROPERTIES_HEADER_KEY_TIMES, times);
                            msg.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                            return msg;
                        }, new CorrelationData());
                return null;
            }, context -> {
                Object msgObj = context.getAttribute(B2BMQConstant.RETRY_CONTEXT_ATTRIBUTE_KEY_MESSAGE);
                MQTmallPushWorkcardUpdateMessage.TmallPushWorkcardUpdateMessage msg = MQTmallPushWorkcardUpdateMessage.TmallPushWorkcardUpdateMessage.parseFrom((byte[]) msgObj);
                Throwable throwable = context.getLastThrowable();
                String msgJson = new JsonFormat().printToString(msg);
                mqB2bTmallLogService.insertMqB2bTmallLog(B2BMQQueueType.TMALLPUSH_DELAY_WORKCARD_UPDATE, msgJson, 3L,
                        B2BProcessFlag.PROCESS_FLAG_FAILURE, 0,
                        "delay send error -- " + throwable.getLocalizedMessage());
                log.error("delay send error {}, {}", throwable.getLocalizedMessage(), msg);
                return null;
            });
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getLocalizedMessage());
        }
        */
    }

    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {

    }
}
