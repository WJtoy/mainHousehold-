package com.wolfking.jeesite.ms.b2bcenter.mq.config;

import com.kkl.kklplus.entity.b2b.mq.B2BMQConstant;
import com.wolfking.jeesite.modules.mq.conf.CommonConfig;
import com.wolfking.jeesite.ms.b2bcenter.mq.receiver.B2BCenterOrderComplainMQReceiver;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * B2B-创建投诉消息
 * @author Ryan
 * @date 2020-10-13
 */
@Configuration
public class B2BCenterOrderComplainConfig extends CommonConfig {
    /**
     * 延时秒数
     */
    public static final int DELAY_MILLISECOND = 10 * 1000;
    /**
     * 重试次数
     */
    public static final int RETRY_TIMES = 3;

    /**
     * 默认用户Id
     */
    public static final Long B2B_UID = 3L;

    @Bean
    public Queue b2bCenterOrderComplainQueue() {
        return new Queue(B2BMQConstant.MQ_B2BCENTER_COMPLAIN_DELAY, true);
    }

    @Bean
    DirectExchange b2bCenterOrderComplainExchange() {
        return (DirectExchange) ExchangeBuilder.directExchange(B2BMQConstant.MQ_B2BCENTER_COMPLAIN_DELAY).
                delayed().withArgument("x-delayed-type", "direct").build();
    }

    @Bean
    Binding bindingB2BCenterOrderComplainExchangeMessage(Queue b2bCenterOrderComplainQueue, DirectExchange b2bCenterOrderComplainExchange) {
        return BindingBuilder.bind(b2bCenterOrderComplainQueue).
                to(b2bCenterOrderComplainExchange).
                with(B2BMQConstant.MQ_B2BCENTER_COMPLAIN_DELAY);
    }

    @Bean
    MessageListenerAdapter b2bCenterOrderComplainListenerAdapter(B2BCenterOrderComplainMQReceiver b2BCenterOrderComplainMQReceiver) {
        return new MessageListenerAdapter(b2BCenterOrderComplainMQReceiver);
    }

    @Bean
    SimpleMessageListenerContainer b2bCenterOrderComplainRetryContainer(MessageListenerAdapter b2bCenterOrderComplainListenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(manualConnectionFactory());
        container.setQueues(b2bCenterOrderComplainQueue());
        container.setAcknowledgeMode(AcknowledgeMode.valueOf(acknowledgeMode.toUpperCase()));
        container.setConcurrentConsumers(concurrency);
        container.setMaxConcurrentConsumers(maxConcurrency);
        container.setPrefetchCount(prefetch);
        container.setMessageListener(b2bCenterOrderComplainListenerAdapter);
        return container;
    }

}
