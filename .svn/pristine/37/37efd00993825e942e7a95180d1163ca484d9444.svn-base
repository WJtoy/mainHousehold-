package com.wolfking.jeesite.modules.mq.sender;

import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.wolfking.jeesite.modules.mq.conf.OrderAutoCompleteConfig;
import com.wolfking.jeesite.modules.mq.dto.MQOrderAutoComplete;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 下单时发送短信及APP推送
 */
@Component
public class OrderAutoCompleteSender implements RabbitTemplate.ConfirmCallback{

    private RabbitTemplate orderAutoCompleteRabbitTemplate;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    public OrderAutoCompleteSender(RabbitTemplate manualRabbitTemplate){
        this.orderAutoCompleteRabbitTemplate = manualRabbitTemplate;
        this.orderAutoCompleteRabbitTemplate.setConfirmCallback(this);
    }

    /* comment at 2019/08/12 Ryan 此方法只有测试类调用
    public void send(MQOrderAutoComplete.OrderAutoComplete message) {
        this.orderAutoCompleteRabbitTemplate.convertAndSend
                (
                        OrderAutoCompleteConfig.MQ_ORDER_AUTOCOMPLETE_EXCHANGE,
                        OrderAutoCompleteConfig.MQ_ORDER_AUTOCOMPLETE_ROUTING,
                        message.toByteArray(),
                        new CorrelationData(UUID.randomUUID().toString())
                );

        redisUtils.incr(RedisConstant.RedisDBType.REDIS_MQ_DB, String.format(RedisConstant.MQ_SS, OrderAutoCompleteConfig.MQ_ORDER_AUTOCOMPLETE_COUNTER));
    }
    */

    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        if (ack) {
            redisUtils.incr(RedisConstant.RedisDBType.REDIS_MQ_DB, String.format(RedisConstant.MQ_RS, OrderAutoCompleteConfig.MQ_ORDER_AUTOCOMPLETE_COUNTER));
        } else {
            redisUtils.incr(RedisConstant.RedisDBType.REDIS_MQ_DB, String.format(RedisConstant.MQ_RE, OrderAutoCompleteConfig.MQ_ORDER_AUTOCOMPLETE_COUNTER));
        }
    }
}
