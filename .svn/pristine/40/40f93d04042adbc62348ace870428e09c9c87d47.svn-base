package com.wolfking.jeesite.ms.b2bcenter.mq.config;

import com.kkl.kklplus.entity.b2b.mq.B2BMQConstant;
import com.wolfking.jeesite.modules.mq.conf.CommonConfig;
import com.wolfking.jeesite.ms.b2bcenter.mq.receiver.B2BCenterOrderMQRetryReceiver;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConditionalOnProperty(name = "ms.b2bcenter.mq.order.retryConsumer.enabled", matchIfMissing = false)
public class B2BCenterOrderRetryMQConfig extends CommonConfig {


    @Bean
    public Queue b2bCenterOrderRetryQueue() {
        return new Queue(B2BMQConstant.MQ_B2BCENTER_RECEIVE_NEW_B2BORDER_RETRY, true);
    }

    @Bean
    DirectExchange b2bCenterOrderRetryExchange() {
        return (DirectExchange) ExchangeBuilder.directExchange(B2BMQConstant.MQ_B2BCENTER_RECEIVE_NEW_B2BORDER_RETRY).
                delayed().withArgument("x-delayed-type", "direct").build();
    }

    @Bean
    Binding bindingB2BCenterOrderRetryExchangeMessage(Queue b2bCenterOrderRetryQueue, DirectExchange b2bCenterOrderRetryExchange) {
        return BindingBuilder.bind(b2bCenterOrderRetryQueue).
                to(b2bCenterOrderRetryExchange).
                with(B2BMQConstant.MQ_B2BCENTER_RECEIVE_NEW_B2BORDER_RETRY);
    }

    @Bean
    MessageListenerAdapter b2bCenterOrderRetryListenerAdapter(B2BCenterOrderMQRetryReceiver b2BCenterOrderMQRetryReceiver) {
        return new MessageListenerAdapter(b2BCenterOrderMQRetryReceiver);
    }

    @Bean
    SimpleMessageListenerContainer b2bCenterOrderRetryContainer(MessageListenerAdapter b2bCenterOrderRetryListenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(manualConnectionFactory());
        container.setQueues(b2bCenterOrderRetryQueue());
        container.setAcknowledgeMode(AcknowledgeMode.valueOf(acknowledgeMode.toUpperCase()));
        container.setConcurrentConsumers(concurrency);
        container.setMaxConcurrentConsumers(maxConcurrency);
        container.setPrefetchCount(prefetch);
        container.setMessageListener(b2bCenterOrderRetryListenerAdapter);
        return container;
    }

}
