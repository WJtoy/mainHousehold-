package com.wolfking.jeesite.modules.mq.sender;

import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.wolfking.jeesite.modules.mq.conf.WSMessageConfig;
import com.wolfking.jeesite.modules.mq.dto.MQWebSocketMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static com.wolfking.jeesite.common.config.redis.RedisConstant.*;

/**
 * WebSocket发送
 */
@Component
public class WSMessageSender implements RabbitTemplate.ConfirmCallback {

    private RabbitTemplate wsMessageRabbitTemplate;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    public WSMessageSender(RabbitTemplate manualRabbitTemplate){
        this.wsMessageRabbitTemplate = manualRabbitTemplate;
        this.wsMessageRabbitTemplate.setConfirmCallback(this);
    }

    public void send(MQWebSocketMessage.WebSocketMessage message) {
        /*
        this.wsMessageRabbitTemplate.convertAndSend
                (
                        WSMessageConfig.MQ_WEBSOCKET_EXCHANGE,
                        WSMessageConfig.MQ_WEBSOCKET_ROUTING,
                        message.toByteArray(),
                        new CorrelationData(UUID.randomUUID().toString())
                );

        redisUtils.incr(RedisConstant.RedisDBType.REDIS_MQ_DB, String.format(MQ_SS, WSMessageConfig.MQ_WEBSOCKET_COUNTER));
        */
    }

    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        if (ack) {
            redisUtils.incr(RedisConstant.RedisDBType.REDIS_MQ_DB, String.format(MQ_RS, WSMessageConfig.MQ_WEBSOCKET_COUNTER));
        } else {
            redisUtils.incr(RedisConstant.RedisDBType.REDIS_MQ_DB, String.format(MQ_RE, WSMessageConfig.MQ_WEBSOCKET_COUNTER));
        }
    }
}
