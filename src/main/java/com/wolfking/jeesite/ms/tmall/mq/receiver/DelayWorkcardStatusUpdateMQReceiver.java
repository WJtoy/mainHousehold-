package com.wolfking.jeesite.ms.tmall.mq.receiver;

import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2b.common.B2BProcessFlag;
import com.kkl.kklplus.entity.b2b.mq.B2BMQConstant;
import com.kkl.kklplus.entity.b2b.mq.B2BMQQueueType;
import com.kkl.kklplus.entity.b2b.pb.MQWorkcardStatusUpdateMessage;
import com.rabbitmq.client.Channel;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.ms.tmall.mq.sender.WorkcardStatusUpdateMQSender;
import com.wolfking.jeesite.ms.tmall.mq.service.MqB2bTmallLogService;
import com.wolfking.jeesite.ms.tmall.sd.service.TmallOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DelayWorkcardStatusUpdateMQReceiver implements ChannelAwareMessageListener {

    @Autowired
    private WorkcardStatusUpdateMQSender workcardStatusUpdateMQSender;

    @Autowired
    private RabbitProperties rabbitProperties;

    @Autowired
    private MqB2bTmallLogService mqB2bTmallLogService;

    @Autowired
    private TmallOrderService b2BOrderService;

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
//        try {
//            MQWorkcardStatusUpdateMessage.WorkcardStatusUpdateMessage workcardStatusUpdateMsg = MQWorkcardStatusUpdateMessage.WorkcardStatusUpdateMessage.parseFrom(message.getBody());
//            if (workcardStatusUpdateMsg != null) {
////                MSResponse<String> response = b2BOrderService.processB2BOrderStatusUpdateMessage(workcardStatusUpdateMsg);
//                if (!MSResponse.isSuccess(response)) {
//                    int times = StringUtils.toInteger(message.getMessageProperties().getHeaders().get(B2BMQConstant.MESSAGE_PROPERTIES_HEADER_KEY_TIMES));
//                    if (times < getMaxAttempts()) {
//                        times++;
//                        workcardStatusUpdateMQSender.sendDelay(workcardStatusUpdateMsg, getDelaySeconds(times), times);
//                    } else {
//                        String msgJson = new JsonFormat().printToString(workcardStatusUpdateMsg);
//                        mqB2bTmallLogService.insertMqB2bTmallLog(B2BMQQueueType.DELAY_WORKCARD_STATUS_UPDATE, msgJson,
//                                workcardStatusUpdateMsg.getUpdateId(),
//                                B2BProcessFlag.PROCESS_FLAG_FAILURE, times + 1,
//                                response != null ? response.getMsg() : "调用天猫接口失败");
//                    }
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            log.error("{}", e.getLocalizedMessage());
//        }
    }

//    private int getMaxAttempts() {
//        return rabbitProperties.getTemplate().getRetry().getMaxAttempts();
//    }
//
//    private int getDelaySeconds(int times) {
//        return (int) (rabbitProperties.getTemplate().getRetry().getInitialInterval() * rabbitProperties.getTemplate().getRetry().getMultiplier() * times);
//    }

}
