package com.wolfking.jeesite.ms.b2bcenter.mq.config;

import com.kkl.kklplus.entity.b2b.mq.B2BMQConstant;
import com.wolfking.jeesite.modules.mq.conf.CommonConfig;
import com.wolfking.jeesite.ms.b2bcenter.mq.receiver.B2BCenterModifyKKLOrderMQReceiver;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class B2BCenterModifyKKLOrderMQConfig extends CommonConfig {

    @Bean
    public Queue b2bCenterModifyKKLOrderQueue() {
        return new Queue(B2BMQConstant.MQ_B2BCENTER_MODIFY_KKL_ORDER, true);
    }

    @Bean
    DirectExchange b2bCenterModifyKKLOrderExchange() {
        return new DirectExchange(B2BMQConstant.MQ_B2BCENTER_MODIFY_KKL_ORDER);
    }

    @Bean
    Binding bindingB2BCenterModifyKKLOrderExchangeMessage(Queue b2bCenterModifyKKLOrderQueue, DirectExchange b2bCenterModifyKKLOrderExchange) {
        return BindingBuilder.bind(b2bCenterModifyKKLOrderQueue)
                .to(b2bCenterModifyKKLOrderExchange)
                .with(B2BMQConstant.MQ_B2BCENTER_MODIFY_KKL_ORDER);
    }

    @Bean
    MessageListenerAdapter b2bCenterModifyKKLOrderListenerAdapter(B2BCenterModifyKKLOrderMQReceiver b2BCenterModifyKKLOrderMQReceiver) {
        return new MessageListenerAdapter(b2BCenterModifyKKLOrderMQReceiver);
    }

    @Bean
    SimpleMessageListenerContainer b2bCenterModifyKKLOrderContainer(MessageListenerAdapter b2bCenterModifyKKLOrderListenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(manualConnectionFactory());
        container.setQueues(b2bCenterModifyKKLOrderQueue());
        container.setAcknowledgeMode(AcknowledgeMode.valueOf(acknowledgeMode.toUpperCase()));
        container.setConcurrentConsumers(concurrency);
        container.setMaxConcurrentConsumers(maxConcurrency);
        container.setPrefetchCount(prefetch);
        container.setMessageListener(b2bCenterModifyKKLOrderListenerAdapter);
        return container;
    }

}
