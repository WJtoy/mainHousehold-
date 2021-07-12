package com.wolfking.jeesite.ms.b2bcenter.mq.config;

import com.kkl.kklplus.entity.b2b.mq.B2BMQConstant;
import com.wolfking.jeesite.modules.mq.conf.CommonConfig;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class B2BCenterProcessKKLOrderRetryMQConfig extends CommonConfig {

    /**
     * 重试的延迟时间
     */
    public static final int DELAY_MILLISECOND = 15 * 1000;
    /**
     * 重试次数
     */
    public static final int RETRY_TIMES = 3;

    @Bean
    public Queue b2bCenterProcessKKLOrderRetryQueue() {
        return new Queue(B2BMQConstant.MQ_B2BCENTER_MODIFY_KKL_ORDER_RETRY, true);
    }

    @Bean
    DirectExchange b2bCenterProcessKKLOrderRetryExchange() {
        return (DirectExchange) ExchangeBuilder.directExchange(B2BMQConstant.MQ_B2BCENTER_MODIFY_KKL_ORDER_RETRY).
                delayed().withArgument("x-delayed-type", "direct").build();
    }

    @Bean
    Binding bindingB2BCenterProcessKKLOrderRetryExchangeMessage(Queue b2bCenterProcessKKLOrderRetryQueue, DirectExchange b2bCenterProcessKKLOrderRetryExchange) {
        return BindingBuilder.bind(b2bCenterProcessKKLOrderRetryQueue).
                to(b2bCenterProcessKKLOrderRetryExchange).
                with(B2BMQConstant.MQ_B2BCENTER_MODIFY_KKL_ORDER_RETRY);
    }

    @Bean
    MessageListenerAdapter b2bCenterProcessKKLOrderRetryListenerAdapter(B2BCenterProcessKKLOrderRetryMQConfig b2bCenterProcessKKLOrderRetryMQReceiver) {
        return new MessageListenerAdapter(b2bCenterProcessKKLOrderRetryMQReceiver);
    }

    @Bean
    SimpleMessageListenerContainer b2bCenterProcessKKLOrderRetryContainer(MessageListenerAdapter b2bCenterProcessKKLOrderListenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(manualConnectionFactory());
        container.setQueues(b2bCenterProcessKKLOrderRetryQueue());
        container.setAcknowledgeMode(AcknowledgeMode.valueOf(acknowledgeMode.toUpperCase()));
        container.setConcurrentConsumers(concurrency);
        container.setMaxConcurrentConsumers(maxConcurrency);
        container.setPrefetchCount(prefetch);
        container.setMessageListener(b2bCenterProcessKKLOrderListenerAdapter);
        return container;
    }

}
