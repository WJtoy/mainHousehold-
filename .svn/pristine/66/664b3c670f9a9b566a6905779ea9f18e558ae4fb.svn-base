package com.wolfking.jeesite.ms.b2bcenter.mq.config;

import com.kkl.kklplus.entity.b2b.mq.B2BMQConstant;
import com.wolfking.jeesite.modules.mq.conf.CommonConfig;
import com.wolfking.jeesite.ms.b2bcenter.mq.receiver.B2BCenterOrderMQReceiver;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "ms.b2bcenter.mq.order.consumer.enabled", matchIfMissing = false)
public class B2BCenterOrderMQConfig extends CommonConfig {

    @Bean
    public Queue b2bCenterOrderQueue() {
        return new Queue(B2BMQConstant.MQ_B2BCENTER_RECEIVE_NEW_B2BORDER, true);
    }

    @Bean
    DirectExchange b2bCenterOrderExchange() {
        return new DirectExchange(B2BMQConstant.MQ_B2BCENTER_RECEIVE_NEW_B2BORDER);
    }

    @Bean
    Binding bindingB2BCenterOrderExchangeMessage(Queue b2bCenterOrderQueue, DirectExchange b2bCenterOrderExchange) {
        return BindingBuilder.bind(b2bCenterOrderQueue)
                .to(b2bCenterOrderExchange)
                .with(B2BMQConstant.MQ_B2BCENTER_RECEIVE_NEW_B2BORDER);
    }

    @Bean
    MessageListenerAdapter b2bCenterOrderListenerAdapter(B2BCenterOrderMQReceiver b2bCenterOrderMQReceiver) {
        return new MessageListenerAdapter(b2bCenterOrderMQReceiver);
    }

    @Bean
    SimpleMessageListenerContainer b2bCenterOrderContainer(MessageListenerAdapter b2bCenterOrderListenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(manualConnectionFactory());
        container.setQueues(b2bCenterOrderQueue());
        container.setAcknowledgeMode(AcknowledgeMode.valueOf(acknowledgeMode.toUpperCase()));
        container.setConcurrentConsumers(concurrency);
        container.setMaxConcurrentConsumers(maxConcurrency);
        container.setPrefetchCount(prefetch);
        container.setMessageListener(b2bCenterOrderListenerAdapter);
        return container;
    }

}
