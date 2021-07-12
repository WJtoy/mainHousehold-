package com.wolfking.jeesite.ms.b2bcenter.mq.config;

import com.kkl.kklplus.entity.b2b.mq.B2BMQConstant;
import com.wolfking.jeesite.modules.mq.conf.CommonConfig;
import com.wolfking.jeesite.ms.b2bcenter.mq.receiver.B2BCenterServiceMonitorRetryMQReceiver;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 天猫预警重试队列
 */
@Configuration
@ConditionalOnProperty(name = "ms.b2bcenter.mq.order.consumer.enabled", matchIfMissing = false)
public class B2BCenterServiceMonitorRetryMQConfig extends CommonConfig {

    /**
     * 重试的延迟时间
     */
    public static final int DELAY_MILLISECOND = 15 * 1000;
    /**
     * 重试次数
     */
    public static final int RETRY_TIMES = 3;

    @Bean
    public Queue b2bCenterServiceMonitorRetryQueue() {
        return new Queue(B2BMQConstant.MQ_B2BCENTER_SERVICEMONITOR_RETRY, true);
    }

    @Bean
    DirectExchange b2bCenterServiceMonitorRetryExchange() {
        return (DirectExchange) ExchangeBuilder.directExchange(B2BMQConstant.MQ_B2BCENTER_SERVICEMONITOR_RETRY).
                delayed().withArgument("x-delayed-type", "direct").build();
    }

    @Bean
    Binding bindingServiceMonitorRetryExchangeMessage(Queue b2bCenterServiceMonitorRetryQueue, DirectExchange b2bCenterServiceMonitorRetryExchange) {
        return BindingBuilder.bind(b2bCenterServiceMonitorRetryQueue)
                .to(b2bCenterServiceMonitorRetryExchange)
                .with(B2BMQConstant.MQ_B2BCENTER_SERVICEMONITOR_RETRY);
    }

    @Bean
    MessageListenerAdapter b2bCenterServiceMonitorRetryListenerAdapter(B2BCenterServiceMonitorRetryMQReceiver b2BCenterServiceMonitorRetryMQReceiver) {
        return new MessageListenerAdapter(b2BCenterServiceMonitorRetryMQReceiver);
    }

    @Bean
    SimpleMessageListenerContainer b2bCenterServiceMonitorRetryContainer(MessageListenerAdapter b2bCenterServiceMonitorRetryListenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(manualConnectionFactory());
        container.setQueues(b2bCenterServiceMonitorRetryQueue());
        container.setAcknowledgeMode(AcknowledgeMode.valueOf(acknowledgeMode.toUpperCase()));
        container.setConcurrentConsumers(concurrency);
        container.setMaxConcurrentConsumers(maxConcurrency);
        container.setPrefetchCount(prefetch);
        container.setMessageListener(b2bCenterServiceMonitorRetryListenerAdapter);
        return container;
    }

}
