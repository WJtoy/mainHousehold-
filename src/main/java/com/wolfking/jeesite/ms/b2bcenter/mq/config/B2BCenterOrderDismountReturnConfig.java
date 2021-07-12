package com.wolfking.jeesite.ms.b2bcenter.mq.config;

import com.kkl.kklplus.entity.b2b.mq.B2BMQConstant;
import com.wolfking.jeesite.modules.mq.conf.CommonConfig;
import com.wolfking.jeesite.ms.b2bcenter.mq.receiver.B2BCenterOrderDismountReturnMQReceiver;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * B2B-退换货流程消息处理
 * @author Ryan
 * @date 2020-10-28
 */
@Configuration
public class B2BCenterOrderDismountReturnConfig extends CommonConfig {
    /**
     * 延时秒数
     */
    public static final int DELAY_SECOND = 10;
    /**
     * 重试次数
     */
    public static final int RETRY_TIMES = 3;

    /**
     * 默认用户Id
     */
    public static final Long B2B_UID = 3L;

    @Bean
    public Queue b2bCenterOrderDismountReturnQueue() {
        return new Queue(B2BMQConstant.MQ_B2BCENTER_ORDER_RETURN_DELAY, true);
    }

    @Bean
    DirectExchange b2bCenterOrderDismountReturnExchange() {
        return (DirectExchange) ExchangeBuilder.directExchange(B2BMQConstant.MQ_B2BCENTER_ORDER_RETURN_DELAY).
                delayed().withArgument("x-delayed-type", "direct").build();
    }

    @Bean
    Binding bindingB2BCenterOrderDismountReturnExchangeMessage(Queue b2bCenterOrderDismountReturnQueue, DirectExchange b2bCenterOrderDismountReturnExchange) {
        return BindingBuilder.bind(b2bCenterOrderDismountReturnQueue).
                to(b2bCenterOrderDismountReturnExchange).
                with(B2BMQConstant.MQ_B2BCENTER_ORDER_RETURN_DELAY);
    }

    @Bean
    MessageListenerAdapter b2bCenterOrderDismountReturnListenerAdapter(B2BCenterOrderDismountReturnMQReceiver b2BCenterOrderDismountReturnMQReceiver) {
        return new MessageListenerAdapter(b2BCenterOrderDismountReturnMQReceiver);
    }

    @Bean
    SimpleMessageListenerContainer b2bCenterOrderDismountReturnRetryContainer(MessageListenerAdapter b2bCenterOrderDismountReturnListenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(manualConnectionFactory());
        container.setQueues(b2bCenterOrderDismountReturnQueue());
        container.setAcknowledgeMode(AcknowledgeMode.valueOf(acknowledgeMode.toUpperCase()));
        container.setConcurrentConsumers(concurrency);
        container.setMaxConcurrentConsumers(maxConcurrency);
        container.setPrefetchCount(prefetch);
        container.setMessageListener(b2bCenterOrderDismountReturnListenerAdapter);
        return container;
    }

}
