package com.wolfking.jeesite.ms.b2bcenter.mq.receiver;

import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2b.common.B2BProcessFlag;
import com.kkl.kklplus.entity.b2b.mq.B2BMQConstant;
import com.kkl.kklplus.entity.b2b.mq.B2BMQQueueType;
import com.kkl.kklplus.entity.b2bcenter.pb.MQB2BCenterOrderExpressArrivalMessage;
import com.rabbitmq.client.Channel;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.ms.b2bcenter.mq.config.B2BCenterOrderExpressArrivalRetryMQConfig;
import com.wolfking.jeesite.ms.b2bcenter.mq.sender.B2BCenterOrderExpressArrivalMQSender;
import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderVModel;
import com.wolfking.jeesite.ms.b2bcenter.sd.service.B2BCenterOrderExpressArrivalService;
import com.wolfking.jeesite.ms.tmall.mq.service.MqB2bTmallLogService;
import com.wolfking.jeesite.ms.utils.B2BFailureLogUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class B2BCenterOrderExpressArrivalRetryMQReceiver implements ChannelAwareMessageListener {

    @Autowired
    private B2BCenterOrderExpressArrivalService b2BCenterOrderExpressArrivalService;

    @Autowired
    private B2BCenterOrderExpressArrivalMQSender b2BCenterOrderExpressArrivalMQSender;
    @Autowired
    private MqB2bTmallLogService mqB2bTmallLogService;

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        User user = B2BOrderVModel.b2bUser;
        MQB2BCenterOrderExpressArrivalMessage.B2BCenterOrderExpressArrivalMessage msgObj = null;
        try {
            msgObj = MQB2BCenterOrderExpressArrivalMessage.B2BCenterOrderExpressArrivalMessage.parseFrom(message.getBody());
            if (msgObj != null) {
                MSResponse response = b2BCenterOrderExpressArrivalService.processOrderExpressArrivalMessage(msgObj, user);
                if (!MSResponse.isSuccessCode(response)) {
                    int times = StringUtils.toInteger(message.getMessageProperties().getHeaders().get(B2BMQConstant.MESSAGE_PROPERTIES_HEADER_KEY_TIMES));
                    if (times < B2BCenterOrderExpressArrivalRetryMQConfig.RETRY_TIMES) {
                        times++;
                        b2BCenterOrderExpressArrivalMQSender.sendRetry(msgObj, times);
                    } else {
                        String msgJson = new JsonFormat().printToString(msgObj);
                        B2BFailureLogUtils.saveFailureLog(B2BMQQueueType.B2BCENTER_ORDER_EXPRESS_ARRIVAL_RETRY, msgJson, 0L,
                                B2BProcessFlag.PROCESS_FLAG_FAILURE, times + 1,
                                response != null ? response.getMsg() : "B2BCenterOrderExpressArrivalRetryMQReceiver");
                        LogUtils.saveLog("重试3次失败", "B2BCenterOrderExpressArrivalRetryMQReceiver.onMessage", msgJson, null, user);
                    }
                }
            }
        } catch (Exception e) {
            if (msgObj != null) {
                String msgJson = new JsonFormat().printToString(msgObj);
                log.error("重试消息失败, json:{}, errorMsg: {}", msgJson, e);
                LogUtils.saveLog("重试消息失败", "B2BCenterOrderExpressArrivalMQReceiver.onMessage", msgJson, e, user);
            } else {
                log.error("重试消息失败, errorMsg: {}", e);
                LogUtils.saveLog("重试消息失败", "B2BCenterOrderExpressArrivalMQReceiver.onMessage", "", e, user);
            }
        } finally {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }
    }
}

