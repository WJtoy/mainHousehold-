package com.wolfking.jeesite.modules.mq.receiver;

import com.rabbitmq.client.Channel;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.fi.service.ChargeServiceNew;
import com.wolfking.jeesite.modules.mq.dto.MQOrderCharge;
import com.wolfking.jeesite.modules.mq.entity.OrderCharge;
import com.wolfking.jeesite.modules.mq.service.OrderChargeService;
import com.wolfking.jeesite.modules.sys.utils.SeqUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Created by Jeff on 2017/7/27.
 */
@Slf4j
@Component
public class OrderChargeReceiver implements ChannelAwareMessageListener {

    @Autowired
    private ChargeServiceNew chargeServiceNew;

    @Autowired
    private OrderChargeService orderChargeService;

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {

        MQOrderCharge.OrderCharge orderCharge = MQOrderCharge.OrderCharge.parseFrom(message.getBody());

        if(orderCharge == null){
            //消息内容为空,丢弃
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }

        OrderCharge orderChargeModel = new OrderCharge();
        orderChargeModel.setId(SeqUtils.NextIDValue(SeqUtils.TableName.MqOrderCharge));
        orderChargeModel.setQuarter(DateUtils.getYear()+DateUtils.getSeason());
        orderChargeModel.setOrderId(orderCharge.getOrderId());
        orderChargeModel.setRetryTimes(0);
        orderChargeModel.setTriggerBy(orderCharge.getTriggerBy());
        orderChargeModel.setTriggerDate(new Date(orderCharge.getTriggerDate()));
        orderChargeModel.setCreateDate(new Date());
        //成功
        try{
            chargeServiceNew.createCharge(orderCharge.getOrderId(), orderCharge.getTriggerBy());
        }catch (Exception e){
            log.error(e.getLocalizedMessage());
            try {
                orderChargeModel.setStatus(40);
                if (e != null && e.getLocalizedMessage() != null){
                    orderChargeModel.setDescription(e.getLocalizedMessage().length()>255?e.getLocalizedMessage().substring(0,254):e.getLocalizedMessage());
                }
                orderChargeService.insert(orderChargeModel);
            }catch (Exception ex){
                log.error("MQ:保存自动对帐错误记录失败", ex);
            }
        }

        //失败
//        channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        //成功
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}
