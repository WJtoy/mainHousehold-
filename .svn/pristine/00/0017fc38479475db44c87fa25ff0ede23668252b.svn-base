package com.wolfking.jeesite.ms.tmall.mq.receiver;


import com.kkl.kklplus.entity.b2b.mq.B2BMQConstant;
import com.kkl.kklplus.entity.b2b.pb.MQTmallPushWorkcardUpdateMessage;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 天猫推送订单变更消息消费端
 * @author  Ryan
 * @date    2018/05/29
 */
@Slf4j
@Component
public class TMallWorkUpdateMQReceiver {

//    @RabbitListener(queues = B2BMQConstant.MQ_TMALLPUSH_WORKCARDUPDATE_QUEUE)
    public void onMessage(Message message, Channel channel) throws Exception {
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        /*
        try {
            MQTmallPushWorkcardUpdateMessage.TmallPushWorkcardUpdateMessage workcardUpdateMessage =
                    MQTmallPushWorkcardUpdateMessage.TmallPushWorkcardUpdateMessage.parseFrom(message.getBody());
            log.info("{}", workcardUpdateMessage.toString());
        }
        catch (Exception e) {
            e.printStackTrace();
        }*/
    }

}
