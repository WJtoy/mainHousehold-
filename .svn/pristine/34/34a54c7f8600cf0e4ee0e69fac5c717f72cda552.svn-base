package com.wolfking.jeesite.ms.b2bcenter.mq.receiver;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.pb.MQB2BOrderProcessLogMessage;
import com.rabbitmq.client.Channel;
import com.wolfking.jeesite.ms.b2bcenter.mq.sender.B2BPushOrderProcessLogToMSMQSender;
import com.wolfking.jeesite.ms.b2bcenter.sd.service.B2BCenterOrderProcessLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class B2BPushOrderProcessLogToMSMQReceiver implements ChannelAwareMessageListener {

    @Autowired
    private B2BCenterOrderProcessLogService orderProcessLogService;

    @Autowired
    private B2BPushOrderProcessLogToMSMQSender orderProcessLogToMSMQSender;

    @Override
    public void onMessage(Message message, Channel channel) throws IOException {
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        try {
            MQB2BOrderProcessLogMessage.B2BOrderProcessLogMessage orderProcessLogMsg = MQB2BOrderProcessLogMessage.B2BOrderProcessLogMessage.parseFrom(message.getBody());
            if (orderProcessLogMsg != null) {
                MSResponse response = orderProcessLogService.processOrderProcessLogMessage(orderProcessLogMsg);
                if (!MSResponse.isSuccessCode(response)) {
                    orderProcessLogToMSMQSender.sendRetry(orderProcessLogMsg, 1);
                }
            }
        } catch (Exception e) {
            log.error("B2BCenterPushOrderProcessLogToMSMQReceiver", e);
        }
    }
}
