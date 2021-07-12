package com.wolfking.jeesite.modules.mq.receiver;

import com.rabbitmq.client.Channel;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.mq.dto.MQShortMessage;
import com.wolfking.jeesite.modules.mq.sender.sms.SmsMQSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 短信消息消费者
 * Created by Ryan on 2017/7/27.
 */
@Component
@Slf4j
//@RabbitListener(queues = "MQ:MESSAGE:SHORTMESSAGE:QUEUE")
public class ShortMessageReceiver implements ChannelAwareMessageListener {

    //@Autowired
    //private MessageService messageService;

    @Autowired
    private SmsMQSender smsMQSender;

    //短信内容最大长度
    //public static int CONTENT_MAX_LENGTH = 500;
    
    @Override 
    public void onMessage(org.springframework.amqp.core.Message message, Channel channel) throws Exception {
        MQShortMessage.ShortMessage myMessage = MQShortMessage.ShortMessage.parseFrom(message.getBody());
        if(myMessage == null){
            //消息内容为空,丢弃
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }
        // 改为消息队列发送短信 2019/03/02
        if(StringUtils.isNotBlank(myMessage.getMobile()) && StringUtils.isNotBlank(myMessage.getContent())){

            smsMQSender.send(
                    myMessage.getMobile(),
                    myMessage.getContent(),
                    "",
                    myMessage.getTriggerBy(),
                    myMessage.getTriggerDate()
            );
            //TODO: 短信类型
        }
        //成功
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

        /* 改为消息队列发送短信 2019/03/02
        Message2 shortMessage = new Message2();
        shortMessage.setId(myMessage.getId());
        shortMessage.setMobile(myMessage.getMobile());
        shortMessage.setContent(myMessage.getContent());
        shortMessage.setExtno(myMessage.getExtNo());
        shortMessage.setType(myMessage.getType());
        shortMessage.setCreateDate(new Date());
        shortMessage.setSendTime(new Date(myMessage.getSendTime()));
        shortMessage.setTriggerBy(myMessage.getTriggerBy());
        shortMessage.setTriggerDate(new Date(myMessage.getTriggerDate()));
        shortMessage.setQuarter(QuarterUtils.getSeasonQuarter(shortMessage.getCreateDate()));
        boolean isNew = (shortMessage.getId()==null || shortMessage.getId()<=0);
        try{
            //检查手机号
            if(StringUtils.isBlank(myMessage.getMobile())){
                shortMessage.setStatus(40);
                shortMessage.setRetryTimes(1);
                shortMessage.setCreateDate(new Date());
                shortMessage.setRemarks("手机号为空");
            } else if(myMessage.getContent().length() > CONTENT_MAX_LENGTH){
                log.error("短信内容超过最大长度：mobile:{},content:{}",myMessage.getMobile(),myMessage.getContent());
                shortMessage.setStatus(40);
                shortMessage.setRetryTimes(1);
                shortMessage.setCreateDate(new Date());
                //content
                shortMessage.setContent(StringUtils.left(myMessage.getContent(),CONTENT_MAX_LENGTH));
                StringBuffer remarks = new StringBuffer(250);
                remarks.append(StringUtils.substring(myMessage.getContent(),CONTENT_MAX_LENGTH));
                if(remarks.length()<250){
                    remarks.append("-内容过长");
                }
                remarks.setLength(255);//只保留255个字符
                shortMessage.setRemarks(remarks.toString().replaceAll("[\u0000]",""));//\u0000 空格
            } else {
                //发送
                String result = SendMessageUtils.SendMessage(shortMessage);
                if (result.equalsIgnoreCase("failed")) {
                    //fail
                    shortMessage.setStatus(40);
                    shortMessage.setRetryTimes(1);
                    shortMessage.setCreateDate(new Date());
                } else {
                    shortMessage.setStatus(30);
                    shortMessage.setCreateDate(new Date());
                }
            }
            if (shortMessage.getId() == null || shortMessage.getId() == 0) {
                shortMessage.setId(SeqUtils.NextID());
                messageService.insert(shortMessage);
            } else {
                messageService.update(shortMessage);
            }
        }catch (Exception e){
            try {
                if (e != null && e.getMessage() != null){
                    shortMessage.setRemarks(StringUtils.left(e.getMessage(),250));
                }else{
                    shortMessage.setRemarks("发送错误");
                }
                shortMessage.setRetryTimes(1);
                shortMessage.setStatus(40);
                shortMessage.setCreateDate(new Date());
                if(isNew==true) {
                    messageService.insert(shortMessage);
                }else{
                    messageService.update(shortMessage);
                }
            } catch (Exception ex){
                log.error("MQ:保存短信发送错误记录失败,mobile:{}",myMessage.getMobile(),ex);
            }
        }finally {
            //成功
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }
        */
//        //失败
//        channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);


    }
}
