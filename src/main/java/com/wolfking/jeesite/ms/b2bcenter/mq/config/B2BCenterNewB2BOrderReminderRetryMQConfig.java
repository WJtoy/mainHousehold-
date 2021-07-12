package com.wolfking.jeesite.ms.b2bcenter.mq.config;

import com.kkl.kklplus.entity.b2b.mq.B2BMQConstant;
import com.wolfking.jeesite.modules.mq.conf.CommonConfig;
import com.wolfking.jeesite.ms.b2bcenter.mq.receiver.B2BCenterModifyB2BOrderRetryMQReceiver;
import com.wolfking.jeesite.ms.b2bcenter.mq.receiver.B2BCenterNewB2BOrderReminderRetryMQReceiver;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class B2BCenterNewB2BOrderReminderRetryMQConfig extends CommonConfig {

    /**
     * 重试的延迟时间
     */
    public static final int DELAY_MILLISECOND = 15 * 1000;
    /**
     * 重试次数
     */
    public static final int RETRY_TIMES = 3;

    @Bean
    public Queue b2bCenterNewB2BOrderReminderRetryQueue() {
        return new Queue(B2BMQConstant.MQ_B2BCENTER_NEW_B2BORDER_REMINDER_RETRY, true);
    }

    @Bean
    DirectExchange b2bCenterNewB2BOrderReminderRetryExchange() {
        return (DirectExchange) ExchangeBuilder.directExchange(B2BMQConstant.MQ_B2BCENTER_NEW_B2BORDER_REMINDER_RETRY).
                delayed().withArgument("x-delayed-type", "direct").build();
    }

    @Bean
    Binding bindingB2BCenterNewB2BOrderReminderRetryExchangeMessage(Queue b2bCenterNewB2BOrderReminderRetryQueue, DirectExchange b2bCenterNewB2BOrderReminderRetryExchange) {
        return BindingBuilder.bind(b2bCenterNewB2BOrderReminderRetryQueue).
                to(b2bCenterNewB2BOrderReminderRetryExchange).
                with(B2BMQConstant.MQ_B2BCENTER_NEW_B2BORDER_REMINDER_RETRY);
    }

    @Bean
    MessageListenerAdapter b2bCenterNewB2BOrderReminderRetryListenerAdapter(B2BCenterNewB2BOrderReminderRetryMQReceiver b2BCenterNewB2BOrderReminderRetryMQReceiver) {
        return new MessageListenerAdapter(b2BCenterNewB2BOrderReminderRetryMQReceiver);
    }

    @Bean
    SimpleMessageListenerContainer b2bCenterNewB2BOrderReminderRetryContainer(MessageListenerAdapter b2bCenterNewB2BOrderReminderRetryListenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(manualConnectionFactory());
        container.setQueues(b2bCenterNewB2BOrderReminderRetryQueue());
        container.setAcknowledgeMode(AcknowledgeMode.valueOf(acknowledgeMode.toUpperCase()));
        container.setConcurrentConsumers(concurrency);
        container.setMaxConcurrentConsumers(maxConcurrency);
        container.setPrefetchCount(prefetch);
        container.setMessageListener(b2bCenterNewB2BOrderReminderRetryListenerAdapter);
        return container;
    }

}
