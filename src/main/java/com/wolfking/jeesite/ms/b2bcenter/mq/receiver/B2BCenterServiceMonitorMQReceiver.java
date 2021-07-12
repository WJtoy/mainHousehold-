package com.wolfking.jeesite.ms.b2bcenter.mq.receiver;

import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.entity.b2b.common.B2BProcessFlag;
import com.kkl.kklplus.entity.b2b.mq.B2BMQQueueType;
import com.kkl.kklplus.entity.b2b.pb.MQTmallServiceMonitorMessageMessage;
import com.rabbitmq.client.Channel;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.ms.b2bcenter.mq.sender.B2BCenterServiceMonitorMQSender;
import com.wolfking.jeesite.ms.tmall.mq.service.MqB2bTmallLogService;
import com.wolfking.jeesite.ms.tmall.sd.service.TmallServiceMonitorService;
import com.wolfking.jeesite.ms.utils.B2BFailureLogUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.stereotype.Component;
import org.springframework.transaction.UnexpectedRollbackException;

import java.io.IOException;

@Slf4j
@Component
public class B2BCenterServiceMonitorMQReceiver implements ChannelAwareMessageListener {

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
                    B2BFailureLogUtils.saveFailureLog(B2BMQQueueType.TMALLPUSH_SERVICEMONITOR, msgJson, 3l,
                            B2BProcessFlag.PROCESS_FLAG_REJECT, 0,jsonEntity.getMessage());
                    log.error("[B2BCenterServiceMonitorMQReceiver]保存天猫预警消息失败:" + jsonEntity.getMessage());
                    LogUtils.saveLog("转换天猫预警消息失败", "B2BCenterServiceMonitorMQ", String.valueOf(serviceMonitorMsg.getMonitorId()), new RuntimeException(jsonEntity.getMessage()), null);
                }
            }else{
                log.error("[B2BCenterServiceMonitorMQReceiver]天猫预警消息接受失败:消息体解析错误");
            }
        }
        catch (UnexpectedRollbackException ur){
            //事务处理失败，delay & retry
            if(serviceMonitorMsg != null){
                b2BCenterServiceMonitorMQSender.sendRetry(serviceMonitorMsg, getDelaySeconds(),1);
            }
        }
        catch (Exception e){
            if(serviceMonitorMsg != null){
                b2BCenterServiceMonitorMQSender.sendRetry(serviceMonitorMsg, getDelaySeconds(),1);
            }else{
                LogUtils.saveLog("转换天猫预警消息失败", "B2BCenterServiceMonitorMQ", "", e, null);
                log.error("转换天猫预警消息失败", e);
            }

        }
    }

    private int getDelaySeconds() {
        return (int) (rabbitProperties.getTemplate().getRetry().getInitialInterval() * rabbitProperties.getTemplate().getRetry().getMultiplier());
    }

}
