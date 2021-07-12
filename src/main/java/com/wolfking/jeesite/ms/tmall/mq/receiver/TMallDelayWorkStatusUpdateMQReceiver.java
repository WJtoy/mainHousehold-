package com.wolfking.jeesite.ms.tmall.mq.receiver;

import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.entity.b2b.common.B2BProcessFlag;
import com.kkl.kklplus.entity.b2b.mq.B2BMQConstant;
import com.kkl.kklplus.entity.b2b.mq.B2BMQQueueType;
import com.kkl.kklplus.entity.b2b.pb.MQTmallPushWorkcardStatusUpdateMessage;
import com.rabbitmq.client.Channel;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.ms.tmall.mq.sender.TMallWorkStatusUpdateMQSender;
import com.wolfking.jeesite.ms.tmall.mq.service.MqB2bTmallLogService;
import com.wolfking.jeesite.ms.utils.B2BFailureLogUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.stereotype.Component;

/**
 * 天猫推送订单状态变更消息延迟消费端
 * 此变更为物流段的通知，tmall会两次次推送订单消息
 * 1.发货，status:-1
 * 2.签收，status:1
 * 拒收：不再推送
 * 退货会以此消息通知
 * @author  Ryan
 * @date    2018/05/29
 */
@Slf4j
@Component
public class TMallDelayWorkStatusUpdateMQReceiver implements ChannelAwareMessageListener {

    @Autowired
    private OrderService orderService;

    @Autowired
    private TMallWorkStatusUpdateMQSender tMallWorkStatusUpdateMQSender;

    @Autowired
    private RabbitProperties rabbitProperties;

    @Autowired
    private MqB2bTmallLogService mqB2bTmallLogService;

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        try {
            MQTmallPushWorkcardStatusUpdateMessage.TmallPushWorkcardStatusUpdateMessage workcardStatusUpdateMessage = MQTmallPushWorkcardStatusUpdateMessage.TmallPushWorkcardStatusUpdateMessage.parseFrom(message.getBody());
            if (workcardStatusUpdateMessage != null) {
                try {
                    orderService.tmallWorkcardStatusUpdate(workcardStatusUpdateMessage);
                }catch (Exception e){
                    int times = StringUtils.toInteger(message.getMessageProperties().getHeaders().get(B2BMQConstant.MESSAGE_PROPERTIES_HEADER_KEY_TIMES));
                    if (times < getMaxAttempts()) {
                        times++;
                        tMallWorkStatusUpdateMQSender.sendDelay(workcardStatusUpdateMessage, getDelaySeconds(times), times);
                    } else {
                        String msgJson = new JsonFormat().printToString(workcardStatusUpdateMessage);
                        B2BFailureLogUtils.saveFailureLog(B2BMQQueueType.DELAY_WORKCARD_STATUS_UPDATE, msgJson,
                                3L,
                                B2BProcessFlag.PROCESS_FLAG_FAILURE, times + 1,
                                e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.error("TMallDelayWorkStatusUpdateMQReceiver", e);
        }
    }

    private int getMaxAttempts() {
        return rabbitProperties.getTemplate().getRetry().getMaxAttempts();
    }

    private int getDelaySeconds(int times) {
        return (int) (rabbitProperties.getTemplate().getRetry().getInitialInterval() * rabbitProperties.getTemplate().getRetry().getMultiplier() * times);
    }

}
