package com.wolfking.jeesite.ms.b2bcenter.mq.receiver;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.pb.MQB2BOrderReminderMessage;
import com.rabbitmq.client.Channel;
import com.wolfking.jeesite.common.utils.Exceptions;
import com.wolfking.jeesite.ms.b2bcenter.mq.sender.B2BCenterNewB2BOrderReminderMQSender;
import com.wolfking.jeesite.ms.b2bcenter.sd.service.B2BOrderReminderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class B2BCenterNewB2BOrderReminderMQReceiver implements ChannelAwareMessageListener {

    @Autowired
    private B2BOrderReminderService b2BOrderReminderService;
    @Autowired
    private B2BCenterNewB2BOrderReminderMQSender b2BCenterNewB2BOrderReminderMQSender;

    @Override
    public void onMessage(Message message, Channel channel) throws IOException {
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        try {
            MQB2BOrderReminderMessage.B2BOrderReminderMessage msgObj = MQB2BOrderReminderMessage.B2BOrderReminderMessage.parseFrom(message.getBody());
            if (msgObj != null) {
                MSResponse response = b2BOrderReminderService.processReminderB2BOrderMessage(msgObj);
                if (!MSResponse.isSuccessCode(response)) {
                    b2BCenterNewB2BOrderReminderMQSender.sendRetry(msgObj, 1);
                }
            }
        } catch (Exception e) {
            log.error("B2BCenterNewB2BOrderReminderMQReceiver - {}", Exceptions.getStackTraceAsString(e));
        }
    }
}
