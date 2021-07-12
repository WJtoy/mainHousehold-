package com.wolfking.jeesite.ms.tmall.mq.receiver;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2b.pb.MQWorkcardStatusUpdateMessage;
import com.rabbitmq.client.Channel;
import com.wolfking.jeesite.ms.tmall.mq.sender.WorkcardStatusUpdateMQSender;
import com.wolfking.jeesite.ms.tmall.sd.service.TmallOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class WorkcardStatusUpdateMQReceiver implements ChannelAwareMessageListener {

    @Autowired
    private TmallOrderService b2BOrderService;

    @Autowired
    private WorkcardStatusUpdateMQSender workcardStatusUpdateMQSender;

    @Autowired
    private RabbitProperties rabbitProperties;


    @Override
    public void onMessage(Message message, Channel channel) throws IOException {
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
//        try {
//            MQWorkcardStatusUpdateMessage.WorkcardStatusUpdateMessage workcardStatusUpdateMsg = MQWorkcardStatusUpdateMessage.WorkcardStatusUpdateMessage.parseFrom(message.getBody());
//            if (workcardStatusUpdateMsg != null) {
//                MSResponse<String> response = b2BOrderService.processB2BOrderStatusUpdateMessage(workcardStatusUpdateMsg);
//                if (!MSResponse.isSuccess(response)) {
//                    workcardStatusUpdateMQSender.sendDelay(workcardStatusUpdateMsg, getDelaySeconds(), 1);
//                }
//            }
//        } catch (Exception e) {
//            log.error("WorkcardStatusUpdateMQReceiver", e);
//        }
    }

//    private int getDelaySeconds() {
//        return (int) (rabbitProperties.getTemplate().getRetry().getInitialInterval() * rabbitProperties.getTemplate().getRetry().getMultiplier());
//    }

}
