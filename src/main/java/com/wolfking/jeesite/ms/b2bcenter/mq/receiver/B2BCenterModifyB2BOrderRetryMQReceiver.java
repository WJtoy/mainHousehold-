package com.wolfking.jeesite.ms.b2bcenter.mq.receiver;

import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2b.common.B2BProcessFlag;
import com.kkl.kklplus.entity.b2b.mq.B2BMQConstant;
import com.kkl.kklplus.entity.b2b.mq.B2BMQQueueType;
import com.kkl.kklplus.entity.b2bcenter.pb.MQB2BOrderModifyMessage;
import com.rabbitmq.client.Channel;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.ms.b2bcenter.mq.config.B2BPushOrderProcessLogToMSRetryMQConfig;
import com.wolfking.jeesite.ms.b2bcenter.mq.sender.B2BCenterModifyB2BOrderMQSender;
import com.wolfking.jeesite.ms.b2bcenter.sd.service.B2BCenterOrderModifyService;
import com.wolfking.jeesite.ms.tmall.mq.service.MqB2bTmallLogService;
import com.wolfking.jeesite.ms.utils.B2BFailureLogUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class B2BCenterModifyB2BOrderRetryMQReceiver implements ChannelAwareMessageListener {

    @Autowired
    private B2BCenterOrderModifyService b2BCenterOrderModifyService;
    @Autowired
    private B2BCenterModifyB2BOrderMQSender b2BCenterModifyB2BOrderMQSender;
    @Autowired
    private MqB2bTmallLogService mqB2bTmallLogService;


    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        try {
            MQB2BOrderModifyMessage.B2BOrderModifyMessage modifyMessage = MQB2BOrderModifyMessage.B2BOrderModifyMessage.parseFrom(message.getBody());
            if (modifyMessage != null) {
                MSResponse response = b2BCenterOrderModifyService.processModifyB2BOrdeMessage(modifyMessage);
                if (!MSResponse.isSuccessCode(response)) {
                    int times = StringUtils.toInteger(message.getMessageProperties().getHeaders().get(B2BMQConstant.MESSAGE_PROPERTIES_HEADER_KEY_TIMES));
                    if (times < B2BPushOrderProcessLogToMSRetryMQConfig.RETRY_TIMES) {
                        times++;
                        b2BCenterModifyB2BOrderMQSender.sendRetry(modifyMessage, times);
                    } else {
                        String msgJson = new JsonFormat().printToString(modifyMessage);
                        B2BFailureLogUtils.saveFailureLog(B2BMQQueueType.B2BCENTER_MODIFY_B2B_ORDER_RETRY, msgJson,
                                0L,
                                B2BProcessFlag.PROCESS_FLAG_FAILURE, times + 1,
                                response != null ? response.getMsg() : "B2BCenterModifyB2BOrderRetryMQReceiver");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("{}", e.getLocalizedMessage());
        }
    }
}
