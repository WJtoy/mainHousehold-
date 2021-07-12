package com.wolfking.jeesite.ms.b2bcenter.mq.config;

import com.kkl.kklplus.entity.b2b.mq.B2BMQConstant;
import com.wolfking.jeesite.modules.mq.conf.CommonConfig;
import com.wolfking.jeesite.ms.b2bcenter.mq.receiver.B2BPushOrderProcessLogToMSRetryMQReceiver;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class B2BPushOrderProcessLogToMSRetryMQConfig extends CommonConfig {

    /**
     * 重试的延迟时间
     */
    public static final int DELAY_MILLISECOND = 15 * 1000;
    /**
     * 重试次数
     */
    public static final int RETRY_TIMES = 3;

    @Bean
    public Queue b2bPushOrderProcessLogToMSRetryQueue() {
        return new Queue(B2BMQConstant.MQ_B2BCENTER_PUSH_ORDERPROCESSLOG_TO_MS_RETRY, true);
    }

    @Bean
    DirectExchange b2bPushOrderProcessLogToMSRetryExchange() {
        return (DirectExchange) ExchangeBuilder.directExchange(B2BMQConstant.MQ_B2BCENTER_PUSH_ORDERPROCESSLOG_TO_MS_RETRY).
                delayed().withArgument("x-delayed-type", "direct").build();
    }

    @Bean
    Binding bindingB2BPushOrderProcessLogToMSRetryExchangeMessage(Queue b2bPushOrderProcessLogToMSRetryQueue, DirectExchange b2bPushOrderProcessLogToMSRetryExchange) {
        return BindingBuilder.bind(b2bPushOrderProcessLogToMSRetryQueue).
                to(b2bPushOrderProcessLogToMSRetryExchange).
                with(B2BMQConstant.MQ_B2BCENTER_PUSH_ORDERPROCESSLOG_TO_MS_RETRY);
    }

    @Bean
    MessageListenerAdapter b2bPushOrderProcessLogToMSRetryListenerAdapter(B2BPushOrderProcessLogToMSRetryMQReceiver b2bPushOrderProcessLogToMSRetryMQReceiver) {
        return new MessageListenerAdapter(b2bPushOrderProcessLogToMSRetryMQReceiver);
    }

    @Bean
    SimpleMessageListenerContainer b2bPushOrderProcessLogToMSRetryContainer(MessageListenerAdapter b2bPushOrderProcessLogToMSRetryListenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(manualConnectionFactory());
        container.setQueues(b2bPushOrderProcessLogToMSRetryQueue());
        container.setAcknowledgeMode(AcknowledgeMode.valueOf(acknowledgeMode.toUpperCase()));
        container.setConcurrentConsumers(concurrency);
        container.setMaxConcurrentConsumers(maxConcurrency);
        container.setPrefetchCount(prefetch);
        container.setMessageListener(b2bPushOrderProcessLogToMSRetryListenerAdapter);
        return container;
    }

}
