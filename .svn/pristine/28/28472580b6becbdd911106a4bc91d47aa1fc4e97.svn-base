package com.wolfking.jeesite.ms.b2bcenter.mq.config;

import com.kkl.kklplus.entity.b2b.mq.B2BMQConstant;
import com.wolfking.jeesite.modules.mq.conf.CommonConfig;
import com.wolfking.jeesite.ms.b2bcenter.mq.receiver.B2BCenterAnomalyRecourseMQReceiver;
import com.wolfking.jeesite.ms.b2bcenter.mq.receiver.B2BCenterAnomalyRecourseRetryMQReceiver;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 天猫一键求助重试队列
 */
@Configuration
@ConditionalOnProperty(name = "ms.b2bcenter.mq.order.retryConsumer.enabled", matchIfMissing = false)
public class B2BCenterAnomalyRecourseRetryMQConfig extends CommonConfig {

    @Bean
    public Queue b2bCenterAnomalyRecourseRetryQueue() {
        return new Queue(B2BMQConstant.MQ_B2BCENTER_ANOMALYRECOURSE_RETRY, true);
    }

    @Bean
    DirectExchange b2bCenterAnomalyRecourseRetryExchange() {
        return (DirectExchange) ExchangeBuilder.directExchange(B2BMQConstant.MQ_B2BCENTER_ANOMALYRECOURSE_RETRY).
                delayed().withArgument("x-delayed-type", "direct").build();
    }

    @Bean
    Binding bindingRetryExchangeMessage(Queue b2bCenterAnomalyRecourseRetryQueue, DirectExchange b2bCenterAnomalyRecourseRetryExchange) {
        return BindingBuilder.bind(b2bCenterAnomalyRecourseRetryQueue)
                .to(b2bCenterAnomalyRecourseRetryExchange)
                .with(B2BMQConstant.MQ_B2BCENTER_ANOMALYRECOURSE_RETRY);
    }

    @Bean
    MessageListenerAdapter anomalyRecourseRetryListenerAdapter(B2BCenterAnomalyRecourseRetryMQReceiver anomalyRecourseRetryMQReceiver) {
        return new MessageListenerAdapter(anomalyRecourseRetryMQReceiver);
    }

    @Bean
    SimpleMessageListenerContainer b2bAnomalyRecourseRetryContainer(MessageListenerAdapter anomalyRecourseRetryListenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(manualConnectionFactory());
        container.setQueues(b2bCenterAnomalyRecourseRetryQueue());
        container.setAcknowledgeMode(AcknowledgeMode.valueOf(acknowledgeMode.toUpperCase()));
        container.setConcurrentConsumers(concurrency);
        container.setMaxConcurrentConsumers(maxConcurrency);
        container.setPrefetchCount(prefetch);
        container.setMessageListener(anomalyRecourseRetryListenerAdapter);
        return container;
    }

}
