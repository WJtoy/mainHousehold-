package com.wolfking.jeesite.modules.mq.receiver;

import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.entity.applet.mq.MQAppletUserGradeOrderMessage;
import com.rabbitmq.client.Channel;
import com.wolfking.jeesite.modules.mq.service.AppletUserGradeOrderService;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 订单报表统计消息消费者
 * Created by Ryan on 2017/7/27.
 */
@Component
@Slf4j
public class AppletUserGradeOrderMQReceiver implements ChannelAwareMessageListener {
    @Autowired
    private AppletUserGradeOrderService appletUserGradeOrderService;

    @Override
    public void onMessage(org.springframework.amqp.core.Message message, Channel channel) throws Exception {
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        MQAppletUserGradeOrderMessage.AppletUserGradeOrderMessage msgObj = null;
        try {
            msgObj = MQAppletUserGradeOrderMessage.AppletUserGradeOrderMessage.parseFrom(message.getBody());
            if (msgObj != null) {
                appletUserGradeOrderService.processMessage(msgObj);
            } else {
                LogUtils.saveLog("小程序用户客评工单", "AppletUserGradeOrderMQReceiver#onMessage", "msgObj == null", null, null);
            }
        } catch (Exception e) {
            LogUtils.saveLog("小程序用户客评工单", "AppletUserGradeOrderMQReceiver#onMessage", msgObj != null ? new JsonFormat().printToString(msgObj) : "", e, null);
        }

    }
}
