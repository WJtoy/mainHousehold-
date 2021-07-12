package com.wolfking.jeesite.ms.b2bcenter.mq.receiver;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.pb.MQB2BOrderStatusUpdateMessage;
import com.rabbitmq.client.Channel;
import com.wolfking.jeesite.ms.b2bcenter.mq.config.B2BOrderStatusUpdateRetryMQConfig;
import com.wolfking.jeesite.ms.b2bcenter.mq.sender.B2BOrderStatusUpdateMQSender;
import com.wolfking.jeesite.ms.b2bcenter.sd.service.B2BCenterOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class B2BOrderStatusUpdateMQReceiver implements ChannelAwareMessageListener {

    @Autowired
    private B2BCenterOrderService centerOrderService;

    @Autowired
    private B2BOrderStatusUpdateMQSender orderStatusUpdateMQSender;

    @Override
    public void onMessage(Message message, Channel channel) throws IOException {
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        try {
            MQB2BOrderStatusUpdateMessage.B2BOrderStatusUpdateMessage orderStatusUpdateMsg = MQB2BOrderStatusUpdateMessage.B2BOrderStatusUpdateMessage.parseFrom(message.getBody());
            if (orderStatusUpdateMsg != null) {
                MSResponse response = centerOrderService.processB2BOrderStatusUpdateMessage(orderStatusUpdateMsg);
                if (!MSResponse.isSuccessCode(response)) {
                    orderStatusUpdateMQSender.sendRetry(orderStatusUpdateMsg, 1);
                }
            }
        } catch (Exception e) {
            log.error("B2BOrderStatusUpdateMQReceiver", e);
        }
    }
}
