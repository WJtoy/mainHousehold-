package com.wolfking.jeesite.ms.praise.mq.receiver;

import cn.hutool.core.util.StrUtil;
import com.google.protobuf.InvalidProtocolBufferException;
import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.entity.praise.PraiseStatusEnum;
import com.kkl.kklplus.entity.praise.dto.MQPraiseMessage;
import com.rabbitmq.client.Channel;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.entity.OrderCondition;
import com.wolfking.jeesite.modules.sd.entity.OrderStatusFlag;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.modules.sd.service.OrderStatusFlagService;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.ms.praise.service.PraiseFormService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * 好评单审核结果处理消息消费者
 *
 * @author Ryan Lu
 * @date 2020/03/31 10:59 AM
 * @since 1.0.0
 */
@Slf4j
@Component
public class PraiseFormMQReceiver implements ChannelAwareMessageListener {

    private static final Long CACHE_TIME = 86500L;

    @Autowired
    private PraiseFormService praiseFormService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderStatusFlagService orderStatusFlagService;

    @Autowired
    private RedisUtils redisUtils;

    @Override
    public void onMessage(Message message, Channel channel) throws IOException {
        MQPraiseMessage.PraiseActionMessage praiseActionMessage = null;
        boolean consumed = true;
        try {
            praiseActionMessage = MQPraiseMessage.PraiseActionMessage.parseFrom(message.getBody());
            if(praiseActionMessage == null){
                log.error("消息体null");
                return;
            }
            String checkResult = checkMessage(praiseActionMessage);
            if (StringUtils.isNotBlank(checkResult)) {
                LogUtils.saveLog("好评单消息处理失败:消息体错误- " + checkResult, "PraiseFormMQReceiver", new JsonFormat().printToString(praiseActionMessage), null, null);
                return;
            }
            //状态筛选
            int status = praiseActionMessage.getStatus();
            if (status != PraiseStatusEnum.APPROVE.code && status != PraiseStatusEnum.CANCELED.code && status != PraiseStatusEnum.PENDING_REVIEW.code
                    && status != PraiseStatusEnum.REJECT.code ) {
                log.error("好评结果消息处理失败：状态错误.json:{}", new JsonFormat().printToString(praiseActionMessage));
                return;
            }
            Map<String, Object> conMap = orderService.getOrderConditionSpecialFromMasterById(praiseActionMessage.getOrderId(),praiseActionMessage.getQuarter());
            if(conMap == null || !conMap.containsKey("status_value")){
                log.error("好评单消息处理失败:读取订单失败,orderId:{}",praiseActionMessage.getOrderId());
                return;
            }
            Integer orderStatus = (Integer) conMap.get("status_value");
            conMap.clear();
            //增加审核的好评单状态检查，防止消息重复处理
            if(status == PraiseStatusEnum.APPROVE.code) {
                String msgJson = new JsonFormat().printToString(praiseActionMessage);
                //2021-01-11 保存到redis,用于检查是否重复并记录日志
                String key = StrUtil.format("PRAISE:AUDIT:{}",praiseActionMessage.getOrderId());
                if(redisUtils.exists(RedisConstant.RedisDBType.REDIS_DB16,key)) {
                    log.error("[好评单审核消息重复] orderId:{} json: {}", praiseActionMessage.getOrderId(), msgJson);
                }
                redisUtils.hmSet(RedisConstant.RedisDBType.REDIS_DB16,key,String.valueOf(System.currentTimeMillis()),msgJson,CACHE_TIME);
                //end
                OrderStatusFlag orderStatusFlag = orderStatusFlagService.getByOrderId(praiseActionMessage.getOrderId(), praiseActionMessage.getQuarter(), true);
                if (orderStatusFlag == null) {
                    //String msgJson = new JsonFormat().printToString(praiseActionMessage);
                    LogUtils.saveLog("好评单消息处理失败:无订单好评状态记录", "PraiseFormMQReceiver", msgJson, null, null);
                    consumed = true;
                    return;
                }
                if (orderStatusFlag.getPraiseStatus() == status) {
                    //String msgJson = new JsonFormat().printToString(praiseActionMessage);
                    LogUtils.saveLog("好评单消息处理失败:当前好评状态与消息体相同", "PraiseFormMQReceiver", msgJson, null, null);
                    consumed = true;
                    return;
                }
            }
            praiseFormService.reviewResultHandle(praiseActionMessage,orderStatus);
        } catch (InvalidProtocolBufferException ie){
            log.error("消息体错误");
        } catch (Exception e) {
            consumed = false;
            String msgJson = new JsonFormat().printToString(praiseActionMessage);
            LogUtils.saveLog("好评单消息处理失败", "PraiseFormMQReceiver", msgJson, e, null);
        } finally {
            if(consumed) {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            }else{
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);
            }
        }
    }

    /**
     * 检查消息体
     * @param message
     * @return
     */
    private String checkMessage(MQPraiseMessage.PraiseActionMessage message){
        StringBuilder sb = new StringBuilder(30);
        if(message.getOrderId() <= 0){
            sb.append("订单id错误");
        }else if(StringUtils.isBlank(message.getQuarter())){
            sb.append("无分片数据");
        }else if(message.getStatus() <= 0){
            sb.append("好评单状态无效");
        }else if(message.getTrigger().getId() <= 0 || StringUtils.isBlank(message.getTrigger().getName())){
            sb.append("无操作人信息");
        }else if(message.getTriggerAt() <= 0){
            sb.append("无操作日期");
        }else if(message.getStatus() != PraiseStatusEnum.CANCELED.code) {
            //取消特殊处理，有可能取消所有好评单
            if (StringUtils.isBlank(message.getFormNo())) {
                sb.append("无好评单单号");
            } else if (message.getServicePointId() <= 0) {
                sb.append("无网点信息");
            }
        }
        return sb.toString();
    }

}

