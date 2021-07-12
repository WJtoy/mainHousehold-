package com.wolfking.jeesite.modules.mq.receiver;

import com.rabbitmq.client.Channel;
import com.wolfking.jeesite.modules.mq.dto.MQWebSocketMessage;
import com.wolfking.jeesite.modules.mq.entity.WSMessage;
import com.wolfking.jeesite.modules.sys.entity.Office;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.ws.service.WebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * WebSocket消息消费者
 * Created by Ryan on 2017/7/27.
 */
@Component
@Slf4j
public class WSMessageReceiver implements ChannelAwareMessageListener {

    @Autowired
    private WebSocketService messageService;

    /*
    @Override 
    public void onMessage(org.springframework.amqp.core.Message message, Channel channel) throws Exception {

        MQWebSocketMessage.WebSocketMessage mqMessage = MQWebSocketMessage.WebSocketMessage.parseFrom(message.getBody());
        if(mqMessage == null){
            //消息内容为空,丢弃
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }
        WSMessage wsMessage = new WSMessage();
        wsMessage.setId(mqMessage.getId());
        wsMessage.setCreateId(mqMessage.getCreateId());
        wsMessage.setQuarter(mqMessage.getQuarter());
        wsMessage.setCustomerId(mqMessage.getCustomerId());
        wsMessage.setOrderId(mqMessage.getOrderId());
        wsMessage.setOrderNo(mqMessage.getOrderNo());
        wsMessage.setKefuId(mqMessage.getKefuId());
        wsMessage.setSalesId(mqMessage.getSalesId());
        wsMessage.setAreaId(mqMessage.getAreaId());
        User user = new User(mqMessage.getTriggerBy().getId(),mqMessage.getTriggerBy().getName());
        user.setCompany(new Office(mqMessage.getTriggerBy().getCompanyId()));
        user.setOffice(new Office(mqMessage.getTriggerBy().getOfficeId()));
        user.setUserType(mqMessage.getTriggerBy().getUserType());
        wsMessage.setTriggerBy(user);
        wsMessage.setTriggerDate(new Date(mqMessage.getTriggerDate()));
        wsMessage.setReceiver(mqMessage.getReceiver());
        wsMessage.setMessageType(mqMessage.getNoticeType());
        wsMessage.setTitle(mqMessage.getTitle());
        wsMessage.setContext(mqMessage.getContext());

        boolean isNew = (wsMessage.getId()==null || wsMessage.getId()<=0);
        //成功
        try{
            wsMessage.setDistination(WSMessage.getDistination(mqMessage.getNoticeType()));
            messageService.sendMessage(wsMessage);
        }catch (Exception e){
            log.error(e.getLocalizedMessage());
        }

//        //失败
//        channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        //成功
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
    */

    @Override
    public void onMessage(org.springframework.amqp.core.Message message, Channel channel) throws Exception {

        MQWebSocketMessage.WebSocketMessage mqMessage = MQWebSocketMessage.WebSocketMessage.parseFrom(message.getBody());
        if(mqMessage == null){
            //消息内容为空,丢弃
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }


        WSMessage wsMessage = new WSMessage();
        wsMessage.setId(mqMessage.getId());
        wsMessage.setCreateId(mqMessage.getCreateId());
        wsMessage.setQuarter(mqMessage.getQuarter());
        wsMessage.setCustomerId(mqMessage.getCustomerId());
        wsMessage.setOrderId(mqMessage.getOrderId());
        wsMessage.setOrderNo(mqMessage.getOrderNo());
        wsMessage.setKefuId(mqMessage.getKefuId());
        wsMessage.setSalesId(mqMessage.getSalesId());
        wsMessage.setAreaId(mqMessage.getAreaId());
        User user = new User(mqMessage.getTriggerBy().getId(),mqMessage.getTriggerBy().getName());
        user.setCompany(new Office(mqMessage.getTriggerBy().getCompanyId()));
        user.setOffice(new Office(mqMessage.getTriggerBy().getOfficeId()));
        user.setUserType(mqMessage.getTriggerBy().getUserType());
        wsMessage.setTriggerBy(user);
        wsMessage.setTriggerDate(new Date(mqMessage.getTriggerDate()));
        wsMessage.setReceiver(mqMessage.getReceiver());
        wsMessage.setMessageType(mqMessage.getNoticeType());
        wsMessage.setTitle(mqMessage.getTitle());
        wsMessage.setContext(mqMessage.getContext());

        boolean isNew = (wsMessage.getId()==null || wsMessage.getId()<=0);
        //成功
        try{
            wsMessage.setDistination(WSMessage.getDistination(mqMessage.getNoticeType()));
            messageService.sendMessage(wsMessage);
            /*
            if(result.equalsIgnoreCase("OK")){
                wsMessage.setStatus(30);
                wsMessage.setCreateDate(new Date());
                if(wsMessage.getId()==null || wsMessage.getId()==0) {
                    wsMessage.setId(SeqUtils.NextID());
                    messageService.insert(wsMessage);
                }else{
                    messageService.update(wsMessage);
                    //失败
                    //channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
                }
            }else{
                wsMessage.setStatus(40);
                wsMessage.setRetryTimes(1);
                wsMessage.setCreateDate(new Date());
                if(wsMessage.getId()==null || wsMessage.getId()==0) {
                    wsMessage.setId(SeqUtils.NextID());
                    messageService.insert(wsMessage);
                }else{
                    messageService.update(wsMessage);
                }
                //失败
                //channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
            }*/
        }catch (Exception e){
            log.error(e.getLocalizedMessage());
            /*
            try {
                if (e != null && e.getLocalizedMessage() != null){
                    wsMessage.setRemarks(e.getLocalizedMessage().length()>255?e.getLocalizedMessage().substring(0,254):e.getLocalizedMessage());
                }
                wsMessage.setRetryTimes(1);
                wsMessage.setStatus(40);
                wsMessage.setCreateDate(new Date());
                if(isNew==true) {
                    messageService.insert(wsMessage);
                }else{
                    messageService.update(wsMessage);
                }
            }catch (Exception ex){
                log.error("MQ:保存短信发送错误记录失败," + ex.getLocalizedMessage());
            }*/
        }

//        //失败
//        channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        //成功
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }


}
