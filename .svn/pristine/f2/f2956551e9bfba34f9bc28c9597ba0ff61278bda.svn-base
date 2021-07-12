package com.wolfking.jeesite.ms.b2bcenter.mq.receiver;

import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.entity.b2bcenter.pb.MQB2BOrderMessage;
import com.rabbitmq.client.Channel;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderVModel;
import com.wolfking.jeesite.ms.b2bcenter.sd.service.B2BCenterOrderTransferService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@ConditionalOnProperty(name = "ms.b2bcenter.mq.order.consumer.enabled", matchIfMissing = false)
public class B2BCenterOrderMQReceiver implements ChannelAwareMessageListener {

    @Autowired
    private B2BCenterOrderTransferService b2BCenterOrderTransferService;

    @Override
    public void onMessage(Message message, Channel channel) throws IOException {
        User user = B2BOrderVModel.b2bUser;
        MQB2BOrderMessage.B2BOrderMessage b2BOrderMsg = null;
        try {
            b2BOrderMsg = MQB2BOrderMessage.B2BOrderMessage.parseFrom(message.getBody());
            if (b2BOrderMsg != null && StringUtils.isNotBlank(b2BOrderMsg.getOrderNo()) && B2BDataSourceEnum.isB2BDataSource(b2BOrderMsg.getDataSource())) {
                b2BCenterOrderTransferService.processB2BOrderMessage(b2BOrderMsg, user);
            } else {
                LogUtils.saveLog("B2B自动转单失败:消息体错误", "B2BCenterOrderMQReceiver.onMessage", "", null, user);
            }
        } catch (Exception e) {
            if (b2BOrderMsg != null) {
                String msgJson = new JsonFormat().printToString(b2BOrderMsg);
                log.error("B2B自动转单失败,json:{}",msgJson,e);
                LogUtils.saveLog("B2B自动转单失败", "B2BCenterOrderMQReceiver.onMessage", msgJson, e, user);
            } else {
                log.error("B2B自动转单失败",e);
                LogUtils.saveLog("B2B自动转单失败", "B2BCenterOrderMQReceiver.onMessage", "", e, user);
            }
        } finally {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }
    }
}
