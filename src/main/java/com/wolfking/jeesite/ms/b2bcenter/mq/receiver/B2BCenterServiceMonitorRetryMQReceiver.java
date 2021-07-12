package com.wolfking.jeesite.ms.b2bcenter.mq.receiver;

import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.entity.b2b.common.B2BProcessFlag;
import com.kkl.kklplus.entity.b2b.mq.B2BMQConstant;
import com.kkl.kklplus.entity.b2b.mq.B2BMQQueueType;
import com.kkl.kklplus.entity.b2b.pb.MQTmallServiceMonitorMessageMessage;
import com.rabbitmq.client.Channel;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.ms.b2bcenter.mq.config.B2BCenterServiceMonitorRetryMQConfig;
import com.wolfking.jeesite.ms.b2bcenter.mq.sender.B2BCenterServiceMonitorMQSender;
import com.wolfking.jeesite.ms.tmall.mq.service.MqB2bTmallLogService;
import com.wolfking.jeesite.ms.tmall.sd.service.TmallServiceMonitorService;
import com.wolfking.jeesite.ms.utils.B2BFailureLogUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@ConditionalOnProperty(name = "ms.b2bcenter.mq.order.consumer.enabled", matchIfMissing = false)
public class B2BCenterServiceMonitorRetryMQReceiver implements ChannelAwareMessageListener {

    @Autowired
    private RabbitProperties rabbitProperties;

    @Autowired
    private TmallServiceMonitorService monitorService;

    @Autowired
    private B2BCenterServiceMonitorMQSender b2BCenterServiceMonitorMQSender;

    @Autowired
    private MqB2bTmallLogService mqB2bTmallLogService;

    @Override
    public void onMessage(Message message, Channel channel) throws IOException {
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        MQTmallServiceMonitorMessageMessage.TmallServiceMonitorMessageMessage serviceMonitorMsg = null;
        try {
            serviceMonitorMsg = MQTmallServiceMonitorMessageMessage.TmallServiceMonitorMessageMessage.parseFrom(message.getBody());
            if (serviceMonitorMsg != null) {
                AjaxJsonEntity jsonEntity =  monitorService.saveMonitor(serviceMonitorMsg);
                if(!jsonEntity.getSuccess()){
                    String msgJson = new JsonFormat().printToString(serviceMonitorMsg);
                    B2BFailureLogUtils.saveFailureLog(B2BMQQueueType.TMALLPUSH_DELAY_SERVICEMONITOR, msgJson, 3l,
                            B2BProcessFlag.PROCESS_FLAG_REJECT, 1,jsonEntity.getMessage());
                    log.error("[B2BCenterServiceMonitorMQReceiver]保存天猫预警消息失败:" + jsonEntity.getMessage());
                    LogUtils.saveLog("转换天猫预警消息失败", "B2BCenterServiceMonitorMQ", String.valueOf(serviceMonitorMsg.getMonitorId()), new RuntimeException(jsonEntity.getMessage()), null);
                }
            }else{
                log.error("天猫预警消息接受失败:消息体解析错误");
            }
        }
        catch (Exception e){
            if(serviceMonitorMsg != null){
                int times = StringUtils.toInteger(message.getMessageProperties().getHeaders().get(B2BMQConstant.MESSAGE_PROPERTIES_HEADER_KEY_TIMES));
                if (times < B2BCenterServiceMonitorRetryMQConfig.RETRY_TIMES) {
                    times++;
                    b2BCenterServiceMonitorMQSender.sendRetry(serviceMonitorMsg,getDelaySeconds(times), times);
                } else {
                    LogUtils.saveLog("转换天猫预警消息失败", "B2BCenterServiceMonitorRetryMQ", String.valueOf(serviceMonitorMsg.getMonitorId()), e, null);
                }
            }else{
                LogUtils.saveLog("转换天猫预警消息失败", "B2BCenterServiceMonitorRetryMQ", "", e, null);
                log.error("转换天猫预警消息失败", e);
            }
        }
    }

    private int getDelaySeconds(int times) {
        return (int) (rabbitProperties.getTemplate().getRetry().getInitialInterval() * rabbitProperties.getTemplate().getRetry().getMultiplier() * times);
    }


}
