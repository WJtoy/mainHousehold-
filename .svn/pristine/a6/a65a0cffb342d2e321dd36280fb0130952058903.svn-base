package com.wolfking.jeesite.modules.mq.receiver;

import com.googlecode.protobuf.format.JsonFormat;
import com.rabbitmq.client.Channel;
import com.wolfking.jeesite.modules.mq.dto.MQOrderCharge;
import com.wolfking.jeesite.modules.sd.service.OrderFeeService;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 对账后更新财务扣费同步到订单
 * @author Ryan
 * @date 2020-04-03
 */
@Slf4j
@Component
public class OderFeeUpdateAfterChargeReceiver implements ChannelAwareMessageListener {

    @Autowired
    private OrderFeeService orderFeeService;

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {

        MQOrderCharge.OrderFeeUpdateAfterCharge updateAfterCharge = MQOrderCharge.OrderFeeUpdateAfterCharge.parseFrom(message.getBody());

        if(updateAfterCharge == null){
            //消息内容为空,丢弃
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }

        //成功
        try{
            double taxFee = 0.00;
            double infoFee = 0.00;
            double deposit = 0.00;//质保金 2021-02-22
            for(MQOrderCharge.FeeUpdateItem item:updateAfterCharge.getItemsList()){
                taxFee = taxFee + item.getTaxFee();
                infoFee = infoFee + item.getInfoFee();
                deposit = deposit + item.getDeposit();
            }
            orderFeeService.updateFeeAfterCharge(updateAfterCharge.getOrderId(),updateAfterCharge.getQuarter(),updateAfterCharge.getTriggerBy(),updateAfterCharge.getTriggerDate(),taxFee,infoFee,deposit,updateAfterCharge.getItemsList());
        }catch (Exception e){
            StringBuffer json = new StringBuffer(new JsonFormat().printToString(updateAfterCharge));
            log.error("对账后更新费用错误,data:{}",json.toString(),e);
            try {
                LogUtils.saveLog("对账后更新费用", "OderFeeUpdateAfterChargeReceiver",json.toString(),e,null,2);
            }catch (Exception ex){
            }finally {
                json.setLength(0);
                json = null;
            }
        }finally {
            //成功
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }
        //失败,重新放回队列
        //channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
    }
}
