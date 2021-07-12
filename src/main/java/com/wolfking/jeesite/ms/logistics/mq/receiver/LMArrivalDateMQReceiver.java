package com.wolfking.jeesite.ms.logistics.mq.receiver;

import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.entity.lm.mq.MQLMExpress;
import com.rabbitmq.client.Channel;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.ms.logistics.service.LogisticsBusinessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 物流接口更新到货日期消费者
 *
 * @author Ryan Lu
 * @date 2019/5/27 9:58 AM
 * @since 1.0.0
 */
@Slf4j
@Component
public class LMArrivalDateMQReceiver implements ChannelAwareMessageListener {

    @Autowired
    private LogisticsBusinessService businessService;

    @Override
    public void onMessage(Message message, Channel channel) throws IOException {
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        MQLMExpress.ArrivalDateMessage arrivalDateMessage = null;
        try {
            arrivalDateMessage = MQLMExpress.ArrivalDateMessage.parseFrom(message.getBody());
            String checkResult = checkMessage(arrivalDateMessage);
            if (StringUtils.isBlank(checkResult)) {
                switch (arrivalDateMessage.getGoodsType()){
                    case Goods:
                        businessService.updateArrivalDate(arrivalDateMessage);
                        break;
                    case Parts:
                        businessService.updatePartsArrivalDate(arrivalDateMessage);
                        break;
                    default:
                        break;
                }
            } else {
                LogUtils.saveLog("同步物流到货日期失败:消息体错误- " + checkResult, "LMArriveDateMQReceiver.onMessage", "", null, null);
            }
        } catch (Exception e) {
            if (arrivalDateMessage != null) {
                String msgJson = new JsonFormat().printToString(arrivalDateMessage);
                LogUtils.saveLog("同步物流到货日期失败", "LMArriveDateMQReceiver.onMessage", msgJson, e, null);
            } else {
                LogUtils.saveLog("同步物流到货日期失败", "LMArriveDateMQReceiver.onMessage", "", e, null);
            }
        }
    }

    private String checkMessage(MQLMExpress.ArrivalDateMessage message){
        StringBuilder sb = new StringBuilder(100);
        if(message == null){
            sb.append("消息体null");
        }
        else if(StringUtils.isBlank(message.getQuarter())){
            sb.append("订单分片无内容");
        }else if(message.getOrderId()<=0){
            sb.append("订单id错误");
        }
        return sb.toString();
    }

}

