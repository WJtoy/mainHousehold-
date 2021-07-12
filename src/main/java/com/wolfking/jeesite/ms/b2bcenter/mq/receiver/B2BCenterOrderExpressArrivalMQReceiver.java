package com.wolfking.jeesite.ms.b2bcenter.mq.receiver;

import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.entity.b2bcenter.pb.MQB2BCenterOrderExpressArrivalMessage;
import com.rabbitmq.client.Channel;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.ms.b2bcenter.mq.sender.B2BCenterOrderExpressArrivalMQSender;
import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderVModel;
import com.wolfking.jeesite.ms.b2bcenter.sd.service.B2BCenterOrderExpressArrivalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class B2BCenterOrderExpressArrivalMQReceiver implements ChannelAwareMessageListener {

    @Autowired
    private B2BCenterOrderExpressArrivalService b2BCenterOrderExpressArrivalService;
    @Autowired
    private B2BCenterOrderExpressArrivalMQSender b2BCenterOrderExpressArrivalMQSender;

    @Override
    public void onMessage(Message message, Channel channel) throws IOException {
        User user = B2BOrderVModel.b2bUser;
        MQB2BCenterOrderExpressArrivalMessage.B2BCenterOrderExpressArrivalMessage msgObj = null;
        try {
            msgObj = MQB2BCenterOrderExpressArrivalMessage.B2BCenterOrderExpressArrivalMessage.parseFrom(message.getBody());
            if (msgObj != null && msgObj.getKklOrderId() > 0 && B2BDataSourceEnum.isB2BDataSource(msgObj.getDataSource())) {
                MSResponse response = b2BCenterOrderExpressArrivalService.processOrderExpressArrivalMessage(msgObj, user);
                if (!MSResponse.isSuccessCode(response)) {
                    b2BCenterOrderExpressArrivalMQSender.sendRetry(msgObj, 1);
                }
            } else {
                LogUtils.saveLog("消息体格式错误", "B2BCenterOrderExpressArrivalMQReceiver.onMessage", "", null, user);
            }
        } catch (Exception e) {
            if (msgObj != null) {
                String msgJson = new JsonFormat().printToString(msgObj);
                log.error("处理消息失败, json:{}, errorMsg: {}", msgJson, e);
                LogUtils.saveLog("处理消息失败", "B2BCenterOrderExpressArrivalMQReceiver.onMessage", msgJson, e, user);
            } else {
                log.error("处理消息失败, errorMsg: {}", e);
                LogUtils.saveLog("处理消息失败", "B2BCenterOrderExpressArrivalMQReceiver.onMessage", "", e, user);
            }
        } finally {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }
    }
}
