package com.wolfking.jeesite.ms.b2bcenter.mq.receiver;

import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2b.mq.B2BMQConstant;
import com.kkl.kklplus.entity.b2bcenter.pb.MQB2BCenterPushOrderInfoToMsMessage;
import com.kkl.kklplus.utils.StringUtils;
import com.rabbitmq.client.Channel;
import com.wolfking.jeesite.ms.b2bcenter.mq.config.B2BCenterPushOrderInfoToMsRetryMQConfig;
import com.wolfking.jeesite.ms.b2bcenter.mq.sender.B2BCenterPushOrderInfoToMsMQSender;
import com.wolfking.jeesite.ms.b2bcenter.sd.service.B2BCenterPushOrderInfoToMsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class B2BCenterPushOrderInfoToMsRetryMQReceiver implements ChannelAwareMessageListener {

    @Autowired
    private B2BCenterPushOrderInfoToMsService pushOrderInfoToMsService;
    @Autowired
    private B2BCenterPushOrderInfoToMsMQSender pushOrderInfoToMsMQSender;

    @Override
    public void onMessage(Message message, Channel channel) throws IOException {
        MQB2BCenterPushOrderInfoToMsMessage.B2BCenterPushOrderInfoToMsMessage msgObj = null;
        try {
            msgObj = MQB2BCenterPushOrderInfoToMsMessage.B2BCenterPushOrderInfoToMsMessage.parseFrom(message.getBody());
            if (msgObj != null) {
                MSResponse response = pushOrderInfoToMsService.processMQMessage(msgObj);
                if (!MSResponse.isSuccessCode(response)) {
                    int times = StringUtils.toInteger(message.getMessageProperties().getHeaders().get(B2BMQConstant.MESSAGE_PROPERTIES_HEADER_KEY_TIMES));
                    if (times < B2BCenterPushOrderInfoToMsRetryMQConfig.RETRY_TIMES) {
                        times++;
                        pushOrderInfoToMsMQSender.sendRetry(msgObj, times);
                    } else {
                        B2BCenterPushOrderInfoToMsService.saveFailureLog("B2BCenterPushOrderInfoToMsRetryMQReceiver.onMessage(" + "重试" + times + "次失败)", new JsonFormat().printToString(msgObj), null);
                    }
                }
            }
        } catch (Exception e) {
            if (msgObj != null) {
                B2BCenterPushOrderInfoToMsService.saveFailureLog("B2BCenterPushOrderInfoToMsRetryMQReceiver.onMessage", new JsonFormat().printToString(msgObj), e);
            } else {
                B2BCenterPushOrderInfoToMsService.saveFailureLog("B2BCenterPushOrderInfoToMsRetryMQReceiver.onMessage", "msgObj == null", e);
            }
        } finally {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }
    }
}
