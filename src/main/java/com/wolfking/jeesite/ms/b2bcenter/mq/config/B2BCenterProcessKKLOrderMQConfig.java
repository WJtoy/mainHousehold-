package com.wolfking.jeesite.ms.b2bcenter.mq.config;

import com.kkl.kklplus.entity.b2b.mq.B2BMQConstant;
import com.wolfking.jeesite.modules.mq.conf.CommonConfig;
import com.wolfking.jeesite.ms.b2bcenter.mq.receiver.B2BCenterProcessKKLOrderMQReceiver;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class B2BCenterProcessKKLOrderMQConfig extends CommonConfig {

    @Bean
    public Queue b2bCenterProcessKKLOrderQueue() {
        return new Queue(B2BMQConstant.MQ_B2BCENTER_PROCESS_KKL_ORDER, true);
    }

    @Bean
    DirectExchange b2bCenterProcessKKLOrderExchange() {
        return new DirectExchange(B2BMQConstant.MQ_B2BCENTER_PROCESS_KKL_ORDER);
    }

    @Bean
    Binding bindingB2BCenterProcessKKLOrderExchangeMessage(Queue b2bCenterProcessKKLOrderQueue, DirectExchange b2bCenterProcessKKLOrderExchange) {
        return BindingBuilder.bind(b2bCenterProcessKKLOrderQueue)
                .to(b2bCenterProcessKKLOrderExchange)
                .with(B2BMQConstant.MQ_B2BCENTER_PROCESS_KKL_ORDER);
    }

    @Bean
    MessageListenerAdapter b2bCenterProcessKKLOrderListenerAdapter(B2BCenterProcessKKLOrderMQReceiver b2BCenterProcessKKLOrderMQReceiver) {
        return new MessageListenerAdapter(b2BCenterProcessKKLOrderMQReceiver);
    }

    @Bean
    SimpleMessageListenerContainer b2bCenterProcessKKLOrderContainer(MessageListenerAdapter b2bCenterProcessKKLOrderListenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(manualConnectionFactory());
        container.setQueues(b2bCenterProcessKKLOrderQueue());
        container.setAcknowledgeMode(AcknowledgeMode.valueOf(acknowledgeMode.toUpperCase()));
        container.setConcurrentConsumers(concurrency);
        container.setMaxConcurrentConsumers(maxConcurrency);
        container.setPrefetchCount(prefetch);
        container.setMessageListener(b2bCenterProcessKKLOrderListenerAdapter);
        return container;
    }

}
