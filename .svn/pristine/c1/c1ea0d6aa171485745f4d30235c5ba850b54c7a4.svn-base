package com.wolfking.jeesite.modules.mq.conf;

import com.kkl.kklplus.entity.b2b.mq.B2BMQConstant;
import com.wolfking.jeesite.modules.mq.receiver.CreateOrderPushMessageRetryReceiver;
import com.wolfking.jeesite.ms.b2bcenter.mq.receiver.B2BCenterAnomalyRecourseRetryMQReceiver;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 下单消息重试队列
 */
@Configuration
public class CreateOrderPushMessageRetryConfig extends CommonConfig {

    public static final String MQ_CREATEORDER_PUSH_MESSAGE_RETRY = "MQ:CREATEORDER:MESSAGE:RETRY";

    @Bean
    public Queue createOrderPushMessageRetryQueue() {
        return new Queue(MQ_CREATEORDER_PUSH_MESSAGE_RETRY, true);
    }

    @Bean
    DirectExchange createOrderPushMessageRetryExchange() {
        return (DirectExchange) ExchangeBuilder.directExchange(MQ_CREATEORDER_PUSH_MESSAGE_RETRY).
                delayed().withArgument("x-delayed-type", "direct").build();
    }

    @Bean
    Binding bindingCreateOrderRetryExchangeMessage(Queue createOrderPushMessageRetryQueue, DirectExchange createOrderPushMessageRetryExchange) {
        return BindingBuilder.bind(createOrderPushMessageRetryQueue)
                .to(createOrderPushMessageRetryExchange)
                .with(MQ_CREATEORDER_PUSH_MESSAGE_RETRY);
    }

    @Bean
    MessageListenerAdapter createOrderPushMessageRetryListenerAdapter(CreateOrderPushMessageRetryReceiver createOrderPushMessageRetryReceiver) {
        return new MessageListenerAdapter(createOrderPushMessageRetryReceiver);
    }

    @Bean
    SimpleMessageListenerContainer createOrderPushMessageRetryContainer(MessageListenerAdapter createOrderPushMessageRetryListenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(manualConnectionFactory());
        container.setQueues(createOrderPushMessageRetryQueue());
        container.setAcknowledgeMode(AcknowledgeMode.valueOf(acknowledgeMode.toUpperCase()));
        container.setConcurrentConsumers(concurrency);
        container.setMaxConcurrentConsumers(maxConcurrency);
        container.setPrefetchCount(prefetch);
        container.setMessageListener(createOrderPushMessageRetryListenerAdapter);
        return container;
    }

}
