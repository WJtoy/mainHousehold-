package com.wolfking.jeesite.ms.b2bcenter.mq.receiver;

import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.entity.b2bcenter.pb.MQB2BAsyncTriggerB2BOperationMessage;
import com.rabbitmq.client.Channel;
import com.wolfking.jeesite.ms.b2bcenter.sd.service.B2BCenterAsyncTriggerB2BOperationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class B2BAsyncTriggerB2BOperationMQReceiver implements ChannelAwareMessageListener {

    @Autowired
    private B2BCenterAsyncTriggerB2BOperationService b2BCenterAsyncTriggerB2BOperationService;

    @Override
    public void onMessage(Message message, Channel channel) throws IOException {
        MQB2BAsyncTriggerB2BOperationMessage.B2BAsyncTriggerB2BOperationMessage msgObj = null;
        try {
            msgObj = MQB2BAsyncTriggerB2BOperationMessage.B2BAsyncTriggerB2BOperationMessage.parseFrom(message.getBody());
            if (msgObj != null) {
                b2BCenterAsyncTriggerB2BOperationService.processAsyncTriggerB2BOperationMessage(msgObj);
            }
        } catch (Exception e) {
            if (msgObj != null) {
                B2BCenterAsyncTriggerB2BOperationService.saveFailureLog("B2BAsyncTriggerB2BOperationMQReceiver.onMessage", new JsonFormat().printToString(msgObj), e);
            } else {
                B2BCenterAsyncTriggerB2BOperationService.saveFailureLog("B2BAsyncTriggerB2BOperationMQReceiver.onMessage", "msgObj == null", e);
            }
        } finally {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }
    }
}
