package com.wolfking.jeesite.ms.b2bcenter.mq.config;

import com.kkl.kklplus.entity.b2b.mq.B2BMQConstant;
import com.wolfking.jeesite.modules.mq.conf.CommonConfig;
import com.wolfking.jeesite.ms.b2bcenter.mq.receiver.B2BCenterModifyB2BOrderMQReceiver;
import com.wolfking.jeesite.ms.b2bcenter.mq.receiver.B2BCenterNewB2BOrderReminderMQReceiver;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class B2BCenterNewB2BOrderReminderMQConfig extends CommonConfig {

    @Bean
    public Queue b2bCenterNewB2BOrderReminderQueue() {
        return new Queue(B2BMQConstant.MQ_B2BCENTER_NEW_B2BORDER_REMINDER, true);
    }

    @Bean
    DirectExchange b2bCenterNewB2BOrderReminderExchange() {
        return new DirectExchange(B2BMQConstant.MQ_B2BCENTER_NEW_B2BORDER_REMINDER);
    }

    @Bean
    Binding bindingB2BCenterNewB2BOrderReminderExchangeMessage(Queue b2bCenterNewB2BOrderReminderQueue, DirectExchange b2bCenterNewB2BOrderReminderExchange) {
        return BindingBuilder.bind(b2bCenterNewB2BOrderReminderQueue)
                .to(b2bCenterNewB2BOrderReminderExchange)
                .with(B2BMQConstant.MQ_B2BCENTER_NEW_B2BORDER_REMINDER);
    }

    @Bean
    MessageListenerAdapter b2bCenterNewB2BOrderReminderListenerAdapter(B2BCenterNewB2BOrderReminderMQReceiver b2BCenterNewB2BOrderReminderMQReceiver) {
        return new MessageListenerAdapter(b2BCenterNewB2BOrderReminderMQReceiver);
    }

    @Bean
    SimpleMessageListenerContainer b2bCenterNewB2BOrderReminderContainer(MessageListenerAdapter b2bCenterNewB2BOrderReminderListenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(manualConnectionFactory());
        container.setQueues(b2bCenterNewB2BOrderReminderQueue());
        container.setAcknowledgeMode(AcknowledgeMode.valueOf(acknowledgeMode.toUpperCase()));
        container.setConcurrentConsumers(concurrency);
        container.setMaxConcurrentConsumers(maxConcurrency);
        container.setPrefetchCount(prefetch);
        container.setMessageListener(b2bCenterNewB2BOrderReminderListenerAdapter);
        return container;
    }

}
