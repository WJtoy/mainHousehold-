package com.wolfking.jeesite.modules.mq.receiver;

import com.rabbitmq.client.Channel;
import com.wolfking.jeesite.common.utils.DateUtils;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 延迟消息消费者
 * Created by Ryan on 2017/7/27.
 */
@Component
public class DelayMessageReceiver implements ChannelAwareMessageListener {

    @Override
    public void onMessage(org.springframework.amqp.core.Message message, Channel channel) throws Exception {
        System.out.println("receive at:" + DateUtils.formatDate(new Date(),"yyyy-MM-dd HH:mm:ss.SSS"));
        //成功
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }


}
