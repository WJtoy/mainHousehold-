package com.wolfking.jeesite.ms.b2bcenter.mq.receiver;

import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2b.common.B2BProcessFlag;
import com.kkl.kklplus.entity.b2b.mq.B2BMQConstant;
import com.kkl.kklplus.entity.b2b.mq.B2BMQQueueType;
import com.kkl.kklplus.entity.b2bcenter.pb.MQB2BOrderProcessLogMessage;
import com.rabbitmq.client.Channel;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.ms.b2bcenter.mq.config.B2BPushOrderProcessLogToMSRetryMQConfig;
import com.wolfking.jeesite.ms.b2bcenter.mq.sender.B2BPushOrderProcessLogToMSMQSender;
import com.wolfking.jeesite.ms.b2bcenter.sd.service.B2BCenterOrderProcessLogService;
import com.wolfking.jeesite.ms.tmall.mq.service.MqB2bTmallLogService;
import com.wolfking.jeesite.ms.utils.B2BFailureLogUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class B2BPushOrderProcessLogToMSRetryMQReceiver implements ChannelAwareMessageListener {

    @Autowired
    private B2BCenterOrderProcessLogService orderProcessLogService;

    @Autowired
    private B2BPushOrderProcessLogToMSMQSender orderProcessLogToMSMQSender;

    @Autowired
    private MqB2bTmallLogService mqB2bTmallLogService;

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        try {
            MQB2BOrderProcessLogMessage.B2BOrderProcessLogMessage orderProcessLogMsg = MQB2BOrderProcessLogMessage.B2BOrderProcessLogMessage.parseFrom(message.getBody());
            if (orderProcessLogMsg != null) {
                MSResponse response = orderProcessLogService.processOrderProcessLogMessage(orderProcessLogMsg);
                if (!MSResponse.isSuccessCode(response)) {
                    int times = StringUtils.toInteger(message.getMessageProperties().getHeaders().get(B2BMQConstant.MESSAGE_PROPERTIES_HEADER_KEY_TIMES));
                    if (times < B2BPushOrderProcessLogToMSRetryMQConfig.RETRY_TIMES) {
                        times++;
                        orderProcessLogToMSMQSender.sendRetry(orderProcessLogMsg, times);
                    } else {
                        String msgJson = new JsonFormat().printToString(orderProcessLogMsg);
                        B2BFailureLogUtils.saveFailureLog(B2BMQQueueType.B2BCENTER_PUSH_ORDERPROCESSLOG_TO_MS_RETRY, msgJson,
                                orderProcessLogMsg.getCreateById(),
                                B2BProcessFlag.PROCESS_FLAG_FAILURE, times + 1,
                                response != null ? response.getMsg() : "调用推送工单日志接口失败");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("{}", e.getLocalizedMessage());
        }
    }
}
