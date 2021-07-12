package com.wolfking.jeesite.ms.b2bcenter.mq.config;

import com.kkl.kklplus.entity.b2b.mq.B2BMQConstant;
import com.wolfking.jeesite.modules.mq.conf.CommonConfig;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class B2BCenterModifyKKLOrderRetryMQConfig extends CommonConfig {

    /**
     * 重试的延迟时间
     */
    public static final int DELAY_MILLISECOND = 15 * 1000;
    /**
     * 重试次数
     */
    public static final int RETRY_TIMES = 3;

    @Bean
    public Queue b2bCenterModifyKKLOrderRetryQueue() {
        return new Queue(B2BMQConstant.MQ_B2BCENTER_MODIFY_KKL_ORDER_RETRY, true);
    }

    @Bean
    DirectExchange b2bCenterModifyKKLOrderRetryExchange() {
        return (DirectExchange) ExchangeBuilder.directExchange(B2BMQConstant.MQ_B2BCENTER_MODIFY_KKL_ORDER_RETRY).
                delayed().withArgument("x-delayed-type", "direct").build();
    }

    @Bean
    Binding bindingB2BCenterModifyKKLOrderRetryExchangeMessage(Queue b2bCenterModifyKKLOrderRetryQueue, DirectExchange b2bCenterModifyKKLOrderRetryExchange) {
        return BindingBuilder.bind(b2bCenterModifyKKLOrderRetryQueue).
                to(b2bCenterModifyKKLOrderRetryExchange).
                with(B2BMQConstant.MQ_B2BCENTER_MODIFY_KKL_ORDER_RETRY);
    }

    @Bean
    MessageListenerAdapter b2bCenterModifyKKLOrderRetryListenerAdapter(B2BCenterModifyKKLOrderRetryMQConfig b2bCenterModifyKKLOrderRetryMQReceiver) {
        return new MessageListenerAdapter(b2bCenterModifyKKLOrderRetryMQReceiver);
    }

    @Bean
    SimpleMessageListenerContainer b2bCenterModifyKKLOrderRetryContainer(MessageListenerAdapter b2bCenterModifyKKLOrderListenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(manualConnectionFactory());
        container.setQueues(b2bCenterModifyKKLOrderRetryQueue());
        container.setAcknowledgeMode(AcknowledgeMode.valueOf(acknowledgeMode.toUpperCase()));
        container.setConcurrentConsumers(concurrency);
        container.setMaxConcurrentConsumers(maxConcurrency);
        container.setPrefetchCount(prefetch);
        container.setMessageListener(b2bCenterModifyKKLOrderListenerAdapter);
        return container;
    }

}
