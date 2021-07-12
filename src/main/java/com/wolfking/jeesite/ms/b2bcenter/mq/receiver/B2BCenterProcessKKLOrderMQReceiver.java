package com.wolfking.jeesite.ms.b2bcenter.mq.receiver;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.pb.MQB2BOrderProcessMessage;
import com.rabbitmq.client.Channel;
import com.wolfking.jeesite.ms.b2bcenter.mq.sender.B2BCenterProcessKKLOrderMQSender;
import com.wolfking.jeesite.ms.b2bcenter.sd.service.B2BCenterOrderProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class B2BCenterProcessKKLOrderMQReceiver implements ChannelAwareMessageListener {

    @Autowired
    private B2BCenterOrderProcessService b2BCenterOrderProcessService;
    @Autowired
    private B2BCenterProcessKKLOrderMQSender b2BCenterProcessKKLOrderMQSender;

    @Override
    public void onMessage(Message message, Channel channel) throws IOException {
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        try {
            MQB2BOrderProcessMessage.B2BOrderProcessMessage processMessage = MQB2BOrderProcessMessage.B2BOrderProcessMessage.parseFrom(message.getBody());
            if (processMessage != null) {
                MSResponse response = b2BCenterOrderProcessService.processKKLOrderProcessMessage(processMessage);
                if (!MSResponse.isSuccessCode(response)) {
                    b2BCenterProcessKKLOrderMQSender.sendRetry(processMessage, 1);
                }
            }
        } catch (Exception e) {
            log.error("B2BCenterProcessKKLOrderMQReceiver - {}", e);
        }
    }
}
