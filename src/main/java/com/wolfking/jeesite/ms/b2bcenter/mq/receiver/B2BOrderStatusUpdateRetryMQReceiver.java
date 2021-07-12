package com.wolfking.jeesite.ms.b2bcenter.mq.receiver;

import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2b.common.B2BProcessFlag;
import com.kkl.kklplus.entity.b2b.mq.B2BMQConstant;
import com.kkl.kklplus.entity.b2b.mq.B2BMQQueueType;
import com.kkl.kklplus.entity.b2bcenter.pb.MQB2BOrderStatusUpdateMessage;
import com.rabbitmq.client.Channel;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.ms.b2bcenter.mq.config.B2BOrderStatusUpdateRetryMQConfig;
import com.wolfking.jeesite.ms.b2bcenter.mq.sender.B2BOrderStatusUpdateMQSender;
import com.wolfking.jeesite.ms.b2bcenter.sd.service.B2BCenterOrderService;
import com.wolfking.jeesite.ms.tmall.mq.service.MqB2bTmallLogService;
import com.wolfking.jeesite.ms.utils.B2BFailureLogUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class B2BOrderStatusUpdateRetryMQReceiver implements ChannelAwareMessageListener {

    @Autowired
    private B2BCenterOrderService centerOrderService;

    @Autowired
    private B2BOrderStatusUpdateMQSender orderStatusUpdateMQSender;

    @Autowired
    private MqB2bTmallLogService mqB2bTmallLogService;

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        try {
            MQB2BOrderStatusUpdateMessage.B2BOrderStatusUpdateMessage orderStatusUpdateMsg = MQB2BOrderStatusUpdateMessage.B2BOrderStatusUpdateMessage.parseFrom(message.getBody());
            if (orderStatusUpdateMsg != null) {
                MSResponse response = centerOrderService.processB2BOrderStatusUpdateMessage(orderStatusUpdateMsg);
                if (!MSResponse.isSuccessCode(response)) {
                    int times = StringUtils.toInteger(message.getMessageProperties().getHeaders().get(B2BMQConstant.MESSAGE_PROPERTIES_HEADER_KEY_TIMES));
                    if (times < B2BOrderStatusUpdateRetryMQConfig.RETRY_TIMES) {
                        times++;
                        orderStatusUpdateMQSender.sendRetry(orderStatusUpdateMsg, times);
                    } else {
                        String msgJson = new JsonFormat().printToString(orderStatusUpdateMsg);
                        B2BFailureLogUtils.saveFailureLog(B2BMQQueueType.DELAY_WORKCARD_STATUS_UPDATE, msgJson,
                                orderStatusUpdateMsg.getUpdaterId(),
                                B2BProcessFlag.PROCESS_FLAG_FAILURE, times + 1,
                                response != null ? response.getMsg() : "调用B2B工单状态变更接口失败");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("{}", e.getLocalizedMessage());
        }
    }
}
