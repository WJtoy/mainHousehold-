package com.wolfking.jeesite.ms.b2bcenter.mq.config;

import com.kkl.kklplus.entity.b2b.mq.B2BMQConstant;
import com.wolfking.jeesite.modules.mq.conf.CommonConfig;
import com.wolfking.jeesite.ms.b2bcenter.mq.receiver.B2BPushOrderProcessLogToMSMQReceiver;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class B2BPushOrderProcessLogToMSMQConfig extends CommonConfig {

    @Bean
    public Queue b2bPushOrderProcessLogToMSQueue() {
        return new Queue(B2BMQConstant.MQ_B2BCENTER_PUSH_ORDERPROCESSLOG_TO_MS, true);
    }

    @Bean
    DirectExchange b2bPushOrderProcessLogToMSExchange() {
        return new DirectExchange(B2BMQConstant.MQ_B2BCENTER_PUSH_ORDERPROCESSLOG_TO_MS);
    }

    @Bean
    Binding bindingB2BPushOrderProcessLogToMSExchangeMessage(Queue b2bPushOrderProcessLogToMSQueue, DirectExchange b2bPushOrderProcessLogToMSExchange) {
        return BindingBuilder.bind(b2bPushOrderProcessLogToMSQueue)
                .to(b2bPushOrderProcessLogToMSExchange)
                .with(B2BMQConstant.MQ_B2BCENTER_PUSH_ORDERPROCESSLOG_TO_MS);
    }

    @Bean
    MessageListenerAdapter b2bPushOrderProcessLogToMSListenerAdapter(B2BPushOrderProcessLogToMSMQReceiver b2bPushOrderProcessLogToMSMQReceiver) {
        return new MessageListenerAdapter(b2bPushOrderProcessLogToMSMQReceiver);
    }

    @Bean
    SimpleMessageListenerContainer b2bPushOrderProcessLogToMSContainer(MessageListenerAdapter b2bPushOrderProcessLogToMSListenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(manualConnectionFactory());
        container.setQueues(b2bPushOrderProcessLogToMSQueue());
        container.setAcknowledgeMode(AcknowledgeMode.valueOf(acknowledgeMode.toUpperCase()));
        container.setConcurrentConsumers(concurrency);
        container.setMaxConcurrentConsumers(maxConcurrency);
        container.setPrefetchCount(prefetch);
        container.setMessageListener(b2bPushOrderProcessLogToMSListenerAdapter);
        return container;
    }

}
