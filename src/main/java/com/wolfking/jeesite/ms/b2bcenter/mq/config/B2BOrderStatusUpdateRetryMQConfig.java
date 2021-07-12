package com.wolfking.jeesite.ms.b2bcenter.mq.config;

import com.kkl.kklplus.entity.b2b.mq.B2BMQConstant;
import com.wolfking.jeesite.modules.mq.conf.CommonConfig;
import com.wolfking.jeesite.ms.b2bcenter.mq.receiver.B2BOrderStatusUpdateRetryMQReceiver;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class B2BOrderStatusUpdateRetryMQConfig extends CommonConfig {

    /**
     * 重试的延迟时间
     */
    public static final int DELAY_MILLISECOND = 60 * 1000;
    /**
     * 重试次数
     */
    public static final int RETRY_TIMES = 3;

    @Bean
    public Queue b2bOrderStatusUpdateRetryQueue() {
        return new Queue(B2BMQConstant.MQ_B2BCENTER_B2BORDER_STATUS_UPDATE_RETRY, true);
    }

    @Bean
    DirectExchange b2bOrderStatusUpdateRetryExchange() {
        return (DirectExchange) ExchangeBuilder.directExchange(B2BMQConstant.MQ_B2BCENTER_B2BORDER_STATUS_UPDATE_RETRY).
                delayed().withArgument("x-delayed-type", "direct").build();
    }

    @Bean
    Binding bindingB2BOrderStatusUpdateRetryExchangeMessage(Queue b2bOrderStatusUpdateRetryQueue, DirectExchange b2bOrderStatusUpdateRetryExchange) {
        return BindingBuilder.bind(b2bOrderStatusUpdateRetryQueue).
                to(b2bOrderStatusUpdateRetryExchange).
                with(B2BMQConstant.MQ_B2BCENTER_B2BORDER_STATUS_UPDATE_RETRY);
    }

    @Bean
    MessageListenerAdapter b2bOrderStatusUpdateRetryListenerAdapter(B2BOrderStatusUpdateRetryMQReceiver b2BOrderStatusUpdateRetryMQReceiver) {
        return new MessageListenerAdapter(b2BOrderStatusUpdateRetryMQReceiver);
    }

    @Bean
    SimpleMessageListenerContainer b2bOrderStatusUpdateRetryContainer(MessageListenerAdapter b2bOrderStatusUpdateRetryListenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(manualConnectionFactory());
        container.setQueues(b2bOrderStatusUpdateRetryQueue());
        container.setAcknowledgeMode(AcknowledgeMode.valueOf(acknowledgeMode.toUpperCase()));
        container.setConcurrentConsumers(concurrency);
        container.setMaxConcurrentConsumers(maxConcurrency);
        container.setPrefetchCount(prefetch);
        container.setMessageListener(b2bOrderStatusUpdateRetryListenerAdapter);
        return container;
    }

}
