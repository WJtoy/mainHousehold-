package com.wolfking.jeesite.ms.b2bcenter.mq.config;

import com.kkl.kklplus.entity.b2b.mq.B2BMQConstant;
import com.wolfking.jeesite.modules.mq.conf.CommonConfig;
import com.wolfking.jeesite.ms.b2bcenter.mq.receiver.B2BCenterOrderExpressArrivalMQReceiver;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class B2BCenterOrderExpressArrivalMQConfig extends CommonConfig {

    @Bean
    public Queue b2bCenterOrderExpressArrivalQueue() {
        return new Queue(B2BMQConstant.MQ_B2BCENTER_ORDER_EXPRESS_ARRIVAL, true);
    }

    @Bean
    DirectExchange b2bCenterOrderExpressArrivalExchange() {
        return new DirectExchange(B2BMQConstant.MQ_B2BCENTER_ORDER_EXPRESS_ARRIVAL);
    }

    @Bean
    Binding bindingB2BCenterOrderExpressArrivalExchangeMessage(Queue b2bCenterOrderExpressArrivalQueue, DirectExchange b2bCenterOrderExpressArrivalExchange) {
        return BindingBuilder.bind(b2bCenterOrderExpressArrivalQueue)
                .to(b2bCenterOrderExpressArrivalExchange)
                .with(B2BMQConstant.MQ_B2BCENTER_ORDER_EXPRESS_ARRIVAL);
    }

    @Bean
    MessageListenerAdapter b2bCenterOrderExpressArrivalListenerAdapter(B2BCenterOrderExpressArrivalMQReceiver b2BCenterOrderExpressArrivalMQReceiver) {
        return new MessageListenerAdapter(b2BCenterOrderExpressArrivalMQReceiver);
    }

    @Bean
    SimpleMessageListenerContainer b2bCenterOrderExpressArrivalContainer(MessageListenerAdapter b2bCenterOrderExpressArrivalListenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(manualConnectionFactory());
        container.setQueues(b2bCenterOrderExpressArrivalQueue());
        container.setAcknowledgeMode(AcknowledgeMode.valueOf(acknowledgeMode.toUpperCase()));
        container.setConcurrentConsumers(concurrency);
        container.setMaxConcurrentConsumers(maxConcurrency);
        container.setPrefetchCount(prefetch);
        container.setMessageListener(b2bCenterOrderExpressArrivalListenerAdapter);
        return container;
    }

}
