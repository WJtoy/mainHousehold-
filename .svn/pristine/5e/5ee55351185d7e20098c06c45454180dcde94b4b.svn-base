package com.wolfking.jeesite.modules.mq.sender;

import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.wolfking.jeesite.modules.mq.conf.ShortMessageConfig;
import com.wolfking.jeesite.modules.mq.dto.MQShortMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static com.wolfking.jeesite.common.config.redis.RedisConstant.*;

/**
 * 短信发送
 */
@Component
public class ShortMessageSender implements RabbitTemplate.ConfirmCallback {

    private RabbitTemplate shortMessageRabbitTemplate;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    public ShortMessageSender(RabbitTemplate manualRabbitTemplate){
        this.shortMessageRabbitTemplate = manualRabbitTemplate;
        this.shortMessageRabbitTemplate.setConfirmCallback(this);
    }
    /*
    public void send(MQShortMessage.ShortMessage message) {
        this.shortMessageRabbitTemplate.convertAndSend
                (
                        ShortMessageConfig.MQ_SHORTMESSAGE_EXCHANGE,
                        ShortMessageConfig.MQ_SHORTMESSAGE_ROUTING,
                        message.toByteArray(),
                        new CorrelationData(UUID.randomUUID().toString())
                );

        redisUtils.incr(RedisConstant.RedisDBType.REDIS_MQ_DB, String.format(MQ_SS, ShortMessageConfig.MQ_SHORTMESSAGE_COUNTER));
    }
    */

    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        if (ack) {
            redisUtils.incr(RedisConstant.RedisDBType.REDIS_MQ_DB, String.format(MQ_RS, ShortMessageConfig.MQ_SHORTMESSAGE_COUNTER));
        } else {
            redisUtils.incr(RedisConstant.RedisDBType.REDIS_MQ_DB, String.format(MQ_RE, ShortMessageConfig.MQ_SHORTMESSAGE_COUNTER));
        }
    }
}
