package com.wolfking.jeesite.modules.mq.receiver;

import com.rabbitmq.client.Channel;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.wolfking.jeesite.modules.mq.dto.MQOrderAutoComplete;
import com.wolfking.jeesite.modules.mq.entity.OrderAutoComplete;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * APP确认完成，自动客评及对账队列
 * Created by Ryan on 2017/12/05.
 */
@Component
@Slf4j
public class OrderAutoCompleteReceiver implements ChannelAwareMessageListener {

    @Autowired
    private OrderService orderService;

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        long index = 0;
        //long index = redisUtils.incr("appClose");
        //log.warn("[[onMessage]]=={}== {}",index,new Date());
        MQOrderAutoComplete.OrderAutoComplete body = MQOrderAutoComplete.OrderAutoComplete.parseFrom(message.getBody());

        if(body == null){
            //消息内容为空,丢弃
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }
        OrderAutoComplete autoMesasge = new OrderAutoComplete();
        autoMesasge.setId(null);
        autoMesasge.setOrderId(body.getOrderId());
        autoMesasge.setQuarter(body.getQuarter());
        autoMesasge.setTriggerBy(body.getTriggerBy());
        autoMesasge.setTriggerDate(new Date(body.getTriggerDate()));
        try {
            orderService.autoComplete(autoMesasge);
        }catch (Exception e){
            log.error("[[onMessage]]=={}== 订单自动完工错误",index,e);
            LogUtils.saveLog("订单自动完工错误", "OrderAutoCompleteReceiver.onMessage", String.valueOf(body.getOrderId()), e, null);
        }finally {
            //成功
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }
        //失败
//        channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);

    }
}
