package com.wolfking.jeesite.ms.b2bcenter.mq.config;

import com.kkl.kklplus.entity.b2b.mq.B2BMQConstant;
import com.wolfking.jeesite.modules.mq.conf.CommonConfig;
import com.wolfking.jeesite.ms.b2bcenter.mq.receiver.B2BCenterOrderExpressArrivalRetryMQReceiver;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class B2BCenterOrderExpressArrivalRetryMQConfig extends CommonConfig {

    /**
     * 重试的延迟时间
     */
    public static final int DELAY_MILLISECOND = 15 * 1000;
    /**
     * 重试次数
     */
    public static final int RETRY_TIMES = 3;


    @Bean
    public Queue b2bCenterOrderExpressArrivalRetryQueue() {
        return new Queue(B2BMQConstant.MQ_B2BCENTER_ORDER_EXPRESS_ARRIVAL_RETRY, true);
    }

    @Bean
    DirectExchange b2bCenterOrderExpressArrivalRetryExchange() {
        return (DirectExchange) ExchangeBuilder.directExchange(B2BMQConstant.MQ_B2BCENTER_ORDER_EXPRESS_ARRIVAL_RETRY).
                delayed().withArgument("x-delayed-type", "direct").build();
    }

    @Bean
    Binding bindingB2BCenterOrderExpressArrivalRetryExchangeMessage(Queue b2bCenterOrderExpressArrivalRetryQueue, DirectExchange b2bCenterOrderExpressArrivalRetryExchange) {
        return BindingBuilder.bind(b2bCenterOrderExpressArrivalRetryQueue).
                to(b2bCenterOrderExpressArrivalRetryExchange).
                with(B2BMQConstant.MQ_B2BCENTER_ORDER_EXPRESS_ARRIVAL_RETRY);
    }

    @Bean
    MessageListenerAdapter b2bCenterOrderExpressArrivalRetryListenerAdapter(B2BCenterOrderExpressArrivalRetryMQReceiver b2BCenterOrderExpressArrivalRetryMQReceiver) {
        return new MessageListenerAdapter(b2BCenterOrderExpressArrivalRetryMQReceiver);
    }

    @Bean
    SimpleMessageListenerContainer b2bCenterOrderExpressArrivalRetryContainer(MessageListenerAdapter b2bCenterOrderExpressArrivalRetryListenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(manualConnectionFactory());
        container.setQueues(b2bCenterOrderExpressArrivalRetryQueue());
        container.setAcknowledgeMode(AcknowledgeMode.valueOf(acknowledgeMode.toUpperCase()));
        container.setConcurrentConsumers(concurrency);
        container.setMaxConcurrentConsumers(maxConcurrency);
        container.setPrefetchCount(prefetch);
        container.setMessageListener(b2bCenterOrderExpressArrivalRetryListenerAdapter);
        return container;
    }

}
