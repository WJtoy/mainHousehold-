package com.wolfking.jeesite.ms.b2bcenter.mq.receiver;

import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2b.common.B2BProcessFlag;
import com.kkl.kklplus.entity.b2b.mq.B2BMQConstant;
import com.kkl.kklplus.entity.b2b.mq.B2BMQQueueType;
import com.kkl.kklplus.entity.b2bcenter.pb.MQB2BOrderDismountReturnMessage;
import com.kkl.kklplus.utils.StringUtils;
import com.rabbitmq.client.Channel;
import com.wolfking.jeesite.ms.b2bcenter.mq.config.B2BCenterOrderDismountReturnConfig;
import com.wolfking.jeesite.ms.b2bcenter.mq.sender.B2BCenterOrderDismountReturnMQSender;
import com.wolfking.jeesite.ms.b2bcenter.sd.service.B2BCenterOrderService;
import com.wolfking.jeesite.ms.utils.B2BFailureLogUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 退换货流程消费者
 */
@Slf4j
@Component
public class B2BCenterOrderDismountReturnMQReceiver implements ChannelAwareMessageListener {

    @Autowired
    private B2BCenterOrderService centerOrderService;

    @Autowired
    private B2BCenterOrderDismountReturnMQSender dismountReturnMQSender;

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        try {
            MQB2BOrderDismountReturnMessage.B2BOrderDismountReturnMessage dismountReturnMessag = MQB2BOrderDismountReturnMessage.B2BOrderDismountReturnMessage.parseFrom(message.getBody());
            if (dismountReturnMessag != null) {
                MSResponse response = centerOrderService.processDismountAndReturnMessage(dismountReturnMessag);
                if (!MSResponse.isSuccessCode(response)) {
                    int times = StringUtils.toInteger(message.getMessageProperties().getHeaders().get(B2BMQConstant.MESSAGE_PROPERTIES_HEADER_KEY_TIMES));
                    if (times < B2BCenterOrderDismountReturnConfig.RETRY_TIMES) {
                        times++;
                        dismountReturnMQSender.sendDelay(dismountReturnMessag,B2BCenterOrderDismountReturnConfig.DELAY_SECOND,times);
                    } else {
                        String msgJson = new JsonFormat().printToString(dismountReturnMessag);
                        B2BFailureLogUtils.saveFailureLog(B2BMQQueueType.B2BCENTER_ORDER_DISMOUNT_AND_RETURN_RETRY, msgJson,
                                dismountReturnMessag.getCreateById(),
                                B2BProcessFlag.PROCESS_FLAG_FAILURE, times + 1,
                                response != null ? response.getMsg() : "调用B2B退换货接口失败");
                    }
                }
            }
        } catch (Exception e) {
            log.error("退换货消息消费失败", e);
        }
    }
}
