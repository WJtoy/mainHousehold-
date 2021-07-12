package com.wolfking.jeesite.ms.b2bcenter.mq.config;

import com.kkl.kklplus.entity.b2b.mq.B2BMQConstant;
import com.wolfking.jeesite.modules.mq.conf.CommonConfig;
import com.wolfking.jeesite.ms.b2bcenter.mq.receiver.B2BCenterPushOrderInfoToMsRetryMQReceiver;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class B2BCenterPushOrderInfoToMsRetryMQConfig extends CommonConfig {

    /**
     * 重试的延迟时间
     */
    public static final int DELAY_MILLISECOND = 15 * 1000;
    /**
     * 重试次数
     */
    public static final int RETRY_TIMES = 3;

    @Bean
    public Queue b2bCenterPushOrderInfoToMsRetryQueue() {
        return new Queue(B2BMQConstant.MQ_B2B_CENTER_PUSH_ORDER_INFO_TO_MS_RETRY, true);
    }

    @Bean
    DirectExchange b2bCenterPushOrderInfoToMsRetryExchange() {
        return (DirectExchange) ExchangeBuilder.directExchange(B2BMQConstant.MQ_B2B_CENTER_PUSH_ORDER_INFO_TO_MS_RETRY).
                delayed().withArgument("x-delayed-type", "direct").build();
    }

    @Bean
    Binding bindingB2BCenterPushOrderInfoToMsRetryExchangeMessage(Queue b2bCenterPushOrderInfoToMsRetryQueue, DirectExchange b2bCenterPushOrderInfoToMsRetryExchange) {
        return BindingBuilder.bind(b2bCenterPushOrderInfoToMsRetryQueue).
                to(b2bCenterPushOrderInfoToMsRetryExchange).
                with(B2BMQConstant.MQ_B2B_CENTER_PUSH_ORDER_INFO_TO_MS_RETRY);
    }

    @Bean
    MessageListenerAdapter b2bCenterPushOrderInfoToMsRetryListenerAdapter(B2BCenterPushOrderInfoToMsRetryMQReceiver b2BCenterPushOrderInfoToMsRetryMQReceiver) {
        return new MessageListenerAdapter(b2BCenterPushOrderInfoToMsRetryMQReceiver);
    }

    @Bean
    SimpleMessageListenerContainer b2bCenterPushOrderInfoToMsRetryContainer(MessageListenerAdapter b2bCenterPushOrderInfoToMsRetryListenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(manualConnectionFactory());
        container.setQueues(b2bCenterPushOrderInfoToMsRetryQueue());
        container.setAcknowledgeMode(AcknowledgeMode.valueOf(acknowledgeMode.toUpperCase()));
        container.setConcurrentConsumers(concurrency);
        container.setMaxConcurrentConsumers(maxConcurrency);
        container.setPrefetchCount(prefetch);
        container.setMessageListener(b2bCenterPushOrderInfoToMsRetryListenerAdapter);
        return container;
    }

}
