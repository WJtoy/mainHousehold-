package com.wolfking.jeesite.ms.tmall.mq.receiver;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2b.mq.B2BMQConstant;
import com.kkl.kklplus.entity.b2b.pb.MQTmallPushWorkcardStatusUpdateMessage;
import com.rabbitmq.client.Channel;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.ms.tmall.mq.sender.TMallWorkStatusUpdateMQSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.stereotype.Component;

/**
 * 天猫推送订单状态变更消息消费端
 * 不需要，此变更为物流段的通知，tmall会两次次推送订单消息
 * 1.发货，status:-1
 * 2.签收，status:1
 * 拒收：不再推送
 * 退货会以此消息通知
 * @author  Ryan
 * @date    2018/05/29
 */
@Slf4j
@Component
public class TMallWorkStatusUpdateMQReceiver {

    @Autowired
    private RabbitProperties rabbitProperties;

    @Autowired
    private OrderService orderService;

    @Autowired
    private TMallWorkStatusUpdateMQSender tMallWorkStatusUpdateMQSender;

//    @RabbitListener(queues = B2BMQConstant.MQ_WORKCARDSTATUSUPDATE_QUEUE)
    public void onMessage(Message message, Channel channel) throws Exception {
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        /*
        try {
            MQTmallPushWorkcardStatusUpdateMessage.TmallPushWorkcardStatusUpdateMessage workcardStatusUpdateMessage =
                    MQTmallPushWorkcardStatusUpdateMessage.TmallPushWorkcardStatusUpdateMessage.parseFrom(message.getBody());
            if(workcardStatusUpdateMessage != null) {
                //log.error("{}", workcardStatusUpdateMessage.toString());
                try {
                    orderService.tmallWorkcardStatusUpdate(workcardStatusUpdateMessage);
                }catch (Exception e){
                    tMallWorkStatusUpdateMQSender.sendDelay(workcardStatusUpdateMessage, getDelaySeconds(), 1);
                    log.error("tmall workcard status update error:{}",e.getMessage());
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            log.error("tmall workcard status update error:{}", e.getMessage());
        }*/
    }

    private int getDelaySeconds() {
        return (int) (rabbitProperties.getTemplate().getRetry().getInitialInterval() * rabbitProperties.getTemplate().getRetry().getMultiplier());
    }
}
