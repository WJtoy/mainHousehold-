package com.wolfking.jeesite.ms.material.mq.config;

import com.kkl.kklplus.entity.b2b.mq.B2BMQConstant;
import com.wolfking.jeesite.modules.mq.conf.CommonConfig;
import com.wolfking.jeesite.ms.material.mq.receiver.B2BMaterialRetryReceiver;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * B2B物流消息队列
 * 包含：审核/驳回，发货通知
 */
@Configuration
public class B2BMaterialRetryMQConfig extends CommonConfig{


    @Bean
    public Queue b2BMaterialRetryQueue() {
        return new Queue(B2BMQConstant.MQ_B2B_MATERIAL_STATUS_NOTIFY_RETRY, true);
    }

    @Bean
    DirectExchange b2BMaterialRetryExchange() {
        return (DirectExchange) ExchangeBuilder.directExchange(B2BMQConstant.MQ_B2B_MATERIAL_STATUS_NOTIFY_RETRY).
                delayed().withArgument("x-delayed-type", "direct").build();
    }

    @Bean
    Binding b2BMaterialExchangeMessage(Queue b2BMaterialRetryQueue, DirectExchange b2BMaterialRetryExchange) {
        return BindingBuilder.bind(b2BMaterialRetryQueue)
                .to(b2BMaterialRetryExchange)
                .with(B2BMQConstant.MQ_B2B_MATERIAL_STATUS_NOTIFY_RETRY);
    }

    @Bean
    MessageListenerAdapter b2BMaterialRetryListenerAdapter(B2BMaterialRetryReceiver b2BMaterialRetryReceiver) {
        return new MessageListenerAdapter(b2BMaterialRetryReceiver);
    }

    @Bean
    SimpleMessageListenerContainer b2BMaterialRetryContainer(MessageListenerAdapter b2BMaterialRetryListenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(manualConnectionFactory());
        container.setQueues(b2BMaterialRetryQueue());
        container.setAcknowledgeMode(AcknowledgeMode.valueOf(acknowledgeMode.toUpperCase()));
        container.setConcurrentConsumers(concurrency);
        container.setMaxConcurrentConsumers(maxConcurrency);
        container.setPrefetchCount(prefetch);
        container.setMessageListener(b2BMaterialRetryListenerAdapter);
        return container;
    }
}
