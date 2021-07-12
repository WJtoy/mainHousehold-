package com.wolfking.jeesite.ms.b2bcenter.mq.config;

import com.kkl.kklplus.entity.b2b.mq.B2BMQConstant;
import com.wolfking.jeesite.modules.mq.conf.CommonConfig;
import com.wolfking.jeesite.ms.b2bcenter.mq.receiver.B2BOrderStatusUpdateMQReceiver;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class B2BOrderStatusUpdateMQConfig extends CommonConfig {

    @Bean
    public Queue b2bOrderStatusUpdateQueue() {
        return new Queue(B2BMQConstant.MQ_B2BCENTER_B2BORDER_STATUS_UPDATE, true);
    }

    @Bean
    DirectExchange b2bOrderStatusUpdateExchange() {
        return new DirectExchange(B2BMQConstant.MQ_B2BCENTER_B2BORDER_STATUS_UPDATE);
    }

    @Bean
    Binding bindingB2BOrderStatusUpdateExchangeMessage(Queue b2bOrderStatusUpdateQueue, DirectExchange b2bOrderStatusUpdateExchange) {
        return BindingBuilder.bind(b2bOrderStatusUpdateQueue)
                .to(b2bOrderStatusUpdateExchange)
                .with(B2BMQConstant.MQ_B2BCENTER_B2BORDER_STATUS_UPDATE);
    }

    @Bean
    MessageListenerAdapter b2bOrderStatusUpdateListenerAdapter(B2BOrderStatusUpdateMQReceiver b2BOrderStatusUpdateMQReceiver) {
        return new MessageListenerAdapter(b2BOrderStatusUpdateMQReceiver);
    }

    @Bean
    SimpleMessageListenerContainer b2bOrderStatusUpdateContainer(MessageListenerAdapter b2bOrderStatusUpdateListenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(manualConnectionFactory());
        container.setQueues(b2bOrderStatusUpdateQueue());
        container.setAcknowledgeMode(AcknowledgeMode.valueOf(acknowledgeMode.toUpperCase()));
        container.setConcurrentConsumers(concurrency);
        container.setMaxConcurrentConsumers(maxConcurrency);
        container.setPrefetchCount(prefetch);
        container.setMessageListener(b2bOrderStatusUpdateListenerAdapter);
        return container;
    }

}
