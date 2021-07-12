package com.wolfking.jeesite.modules.mq.receiver;

import com.rabbitmq.client.Channel;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.wolfking.jeesite.modules.mq.dto.MQNoticeMessage;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * WebSocket消息消费者
 * Created by Ryan on 2017/7/27.
 */
@Component
public class NoticeMessageReceiver implements ChannelAwareMessageListener {

    @Autowired
    private RedisUtils redisUtils;

    @Override 
    public void onMessage(org.springframework.amqp.core.Message message, Channel channel) throws Exception {

        MQNoticeMessage.NoticeMessage mqMessage = MQNoticeMessage.NoticeMessage.parseFrom(message.getBody());
        if(mqMessage == null){
            //消息内容为空,丢弃
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }

        try{
            incrAppAbnormaly(mqMessage);
        }catch (Exception e){
            LogUtils.saveLog("App异常消息处理失败","NoticeMessageReceiver.onMessage",String.valueOf(mqMessage.getOrderId()),null,null);
        }finally {
            //成功
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }

//        //失败
//        channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);

    }

    /**
     * App异常计数
     * @param message
     */
    private void incrAppAbnormaly(MQNoticeMessage.NoticeMessage message){
        Long kefuId = message.getKefuId();
        Long cid = message.getCustomerId();
        Long areaId = message.getAreaId();
        if(kefuId == null || cid == null || areaId == null){
            return;
        }

        User user = UserUtils.get(kefuId,null,true);
        if(user == null){
            LogUtils.saveLog("读取客服错误","OrderService.incrAppAbnormaly",String.valueOf(message.getOrderId()),null,null);
            return;
        }
        if(!user.isKefu()){
            return;
        }

        if(user.getCustomerIds()!=null && user.getCustomerIds().contains(cid)){
            redisUtils.hIncrBy(RedisConstant.RedisDBType.REDIS_MS_DB,RedisConstant.MS_APP_ABNORMALY_KEFUBYCUSTOMER,cid.toString(),message.getDelta());
            return;
        }

        redisUtils.hIncrBy(RedisConstant.RedisDBType.REDIS_MS_DB,RedisConstant.MS_APP_ABNORMALY_KEFUBYAREA,areaId.toString(),message.getDelta());
    }

    /**
     * App异常计数
     * @param condition
    public void incrAppAbnormaly(OrderCondition condition, long delta){
        User kefu = condition.getKefu();
        if(kefu == null){
            return;
        }
        User user = UserUtils.get(kefu.getId(),null,true);
        if(user == null){
            LogUtils.saveLog("读取客服错误","OrderService.incrAppAbnormaly",condition.getOrderId().toString(),null,null);
            return;
        }
        if(!user.isKefu()){
            return;
        }
        Long cid = condition.getCustomer().getId();
        if(user.getCustomerIds()!=null && user.getCustomerIds().contains(cid)){
            redisUtils.hIncrBy(RedisConstant.RedisDBType.REDIS_MS_DB,RedisConstant.MS_APP_ABNORMALY_KEFUBYCUSTOMER,cid.toString(),delta);
            return;
        }
        Long areaId = condition.getArea().getId();
        redisUtils.hIncrBy(RedisConstant.RedisDBType.REDIS_MS_DB,RedisConstant.MS_APP_ABNORMALY_KEFUBYAREA,areaId.toString(),delta);
    }*/

}
