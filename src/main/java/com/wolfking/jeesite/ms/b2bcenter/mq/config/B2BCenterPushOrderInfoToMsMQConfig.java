package com.wolfking.jeesite.ms.b2bcenter.mq.config;

import com.kkl.kklplus.entity.b2b.mq.B2BMQConstant;
import com.wolfking.jeesite.modules.mq.conf.CommonConfig;
import com.wolfking.jeesite.ms.b2bcenter.mq.receiver.B2BCenterPushOrderInfoToMsMQReceiver;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class B2BCenterPushOrderInfoToMsMQConfig extends CommonConfig {

    @Bean
    public Queue b2bCenterPushOrderInfoToMsQueue() {
        return new Queue(B2BMQConstant.MQ_B2B_CENTER_PUSH_ORDER_INFO_TO_MS, true);
    }

    @Bean
    DirectExchange b2bCenterPushOrderInfoToMsExchange() {
        return new DirectExchange(B2BMQConstant.MQ_B2B_CENTER_PUSH_ORDER_INFO_TO_MS);
    }

    @Bean
    Binding bindingB2BCenterPushOrderInfoToMsExchangeMessage(Queue b2bCenterPushOrderInfoToMsQueue, DirectExchange b2bCenterPushOrderInfoToMsExchange) {
        return BindingBuilder.bind(b2bCenterPushOrderInfoToMsQueue)
                .to(b2bCenterPushOrderInfoToMsExchange)
                .with(B2BMQConstant.MQ_B2B_CENTER_PUSH_ORDER_INFO_TO_MS);
    }

    @Bean
    MessageListenerAdapter b2bCenterPushOrderInfoToMsListenerAdapter(B2BCenterPushOrderInfoToMsMQReceiver b2BCenterPushOrderInfoToMsMQReceiver) {
        return new MessageListenerAdapter(b2BCenterPushOrderInfoToMsMQReceiver);
    }

    @Bean
    SimpleMessageListenerContainer b2bCenterPushOrderInfoToMsContainer(MessageListenerAdapter b2bCenterPushOrderInfoToMsListenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(manualConnectionFactory());
        container.setQueues(b2bCenterPushOrderInfoToMsQueue());
        container.setAcknowledgeMode(AcknowledgeMode.valueOf(acknowledgeMode.toUpperCase()));
        container.setConcurrentConsumers(concurrency);
        container.setMaxConcurrentConsumers(maxConcurrency);
        container.setPrefetchCount(prefetch);
        container.setMessageListener(b2bCenterPushOrderInfoToMsListenerAdapter);
        return container;
    }

}
