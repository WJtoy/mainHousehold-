package com.wolfking.jeesite.ms.tmall.mq.receiver;

import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.entity.b2b.common.B2BProcessFlag;
import com.kkl.kklplus.entity.b2b.mq.B2BMQConstant;
import com.kkl.kklplus.entity.b2b.mq.B2BMQQueueType;
import com.kkl.kklplus.entity.b2b.pb.MQTmallPushWorkcardStatusUpdateMessage;
import com.kkl.kklplus.entity.b2b.pb.MQTmallPushWorkcardUpdateMessage;
import com.rabbitmq.client.Channel;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.ms.tmall.mq.sender.TMallWorkStatusUpdateMQSender;
import com.wolfking.jeesite.ms.tmall.mq.sender.TMallWorkUpdateMQSender;
import com.wolfking.jeesite.ms.tmall.mq.service.MqB2bTmallLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.stereotype.Component;

/**
 * 天猫推送订单变更消息延迟消费端
 * @author  Ryan
 * @date    2018/05/29
 */
@Slf4j
@Component
public class TMallDelayWorkUpdateMQReceiver implements ChannelAwareMessageListener {

    @Autowired
    private OrderService orderService;

    @Autowired
    private TMallWorkUpdateMQSender tMallWorkUpdateMQSender;

    @Autowired
    private RabbitProperties rabbitProperties;

    @Autowired
    private MqB2bTmallLogService mqB2bTmallLogService;

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        /*
        try {
            MQTmallPushWorkcardUpdateMessage.TmallPushWorkcardUpdateMessage workcardUpdateMessage = MQTmallPushWorkcardUpdateMessage.TmallPushWorkcardUpdateMessage.parseFrom(message.getBody());
            if (workcardUpdateMessage != null) {
                try {
                    orderService.tmallWorkcardUpdate(workcardUpdateMessage);
                }catch (Exception e){
                    int times = StringUtils.toInteger(message.getMessageProperties().getHeaders().get(B2BMQConstant.MESSAGE_PROPERTIES_HEADER_KEY_TIMES));
                    if (times < getMaxAttempts()) {
                        times++;
                        tMallWorkUpdateMQSender.sendDelay(workcardUpdateMessage, getDelaySeconds(times), times);
                    } else {
                        String msgJson = new JsonFormat().printToString(workcardUpdateMessage);
                        mqB2bTmallLogService.insertMqB2bTmallLog(B2BMQQueueType.DELAY_WORKCARD_STATUS_UPDATE, msgJson,
                                3L,
                                B2BProcessFlag.PROCESS_FLAG_FAILURE, times + 1,
                                e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("{}", e.getMessage());
        }
        */
    }

    private int getMaxAttempts() {
        return rabbitProperties.getTemplate().getRetry().getMaxAttempts();
    }

    private int getDelaySeconds(int times) {
        return (int) (rabbitProperties.getTemplate().getRetry().getInitialInterval() * rabbitProperties.getTemplate().getRetry().getMultiplier() * times);
    }

}
