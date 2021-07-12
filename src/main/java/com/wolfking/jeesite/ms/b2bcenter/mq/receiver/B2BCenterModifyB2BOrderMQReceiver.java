package com.wolfking.jeesite.ms.b2bcenter.mq.receiver;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.pb.MQB2BOrderModifyMessage;
import com.rabbitmq.client.Channel;
import com.wolfking.jeesite.ms.b2bcenter.mq.sender.B2BCenterModifyB2BOrderMQSender;
import com.wolfking.jeesite.ms.b2bcenter.sd.service.B2BCenterOrderModifyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class B2BCenterModifyB2BOrderMQReceiver implements ChannelAwareMessageListener {

    @Autowired
    private B2BCenterOrderModifyService b2BCenterOrderModifyService;
    @Autowired
    private B2BCenterModifyB2BOrderMQSender b2BCenterModifyB2BOrderMQSender;

    @Override
    public void onMessage(Message message, Channel channel) throws IOException {
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        try {
            MQB2BOrderModifyMessage.B2BOrderModifyMessage modifyMessage = MQB2BOrderModifyMessage.B2BOrderModifyMessage.parseFrom(message.getBody());
            if (modifyMessage != null) {
                MSResponse response = b2BCenterOrderModifyService.processModifyB2BOrdeMessage(modifyMessage);
                if (!MSResponse.isSuccessCode(response)) {
                    b2BCenterModifyB2BOrderMQSender.sendRetry(modifyMessage, 1);
                }
            }
        } catch (Exception e) {
            log.error("B2BCenterModifyB2BOrderMQReceiver - {}", e);
        }
    }
}
