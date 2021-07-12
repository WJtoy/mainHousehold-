package com.wolfking.jeesite.ms.b2bcenter.mq.config;

import com.kkl.kklplus.entity.b2b.mq.B2BMQConstant;
import com.wolfking.jeesite.modules.mq.conf.CommonConfig;
import com.wolfking.jeesite.ms.b2bcenter.mq.receiver.B2BCenterOrderReminderProcessMQReceiver;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 用于接收第三方系统回调的投诉处理进度
 * @date 2020-10-13
 */
@Configuration
public class B2BCenterOrderReminderProcessConfig extends CommonConfig {
    /**
     * 延时秒数
     */
    public static final int DELAY_MILLISECOND = 10 * 1000;
    /**
     * 重试次数
     */
    public static final int RETRY_TIMES = 3;

    /**
     * 默认用户Id
     */
    public static final Long B2B_UID = 3L;

    @Bean
    public Queue b2bCenterOrderReminderProcessQueue() {
        return new Queue(B2BMQConstant.MQ_B2BCENTER_PROCESS_B2BORDER_REMINDER_RETRY, true);
    }

    @Bean
    DirectExchange b2bCenterOrderReminderProcessExchange() {
        return (DirectExchange) ExchangeBuilder.directExchange(B2BMQConstant.MQ_B2BCENTER_PROCESS_B2BORDER_REMINDER_RETRY).
                delayed().withArgument("x-delayed-type", "direct").build();
    }

    @Bean
    Binding bindingB2BCenterOrderReminderProcessExchangeMessage(Queue b2bCenterOrderReminderProcessQueue, DirectExchange b2bCenterOrderReminderProcessExchange) {
        return BindingBuilder.bind(b2bCenterOrderReminderProcessQueue).
                to(b2bCenterOrderReminderProcessExchange).
                with(B2BMQConstant.MQ_B2BCENTER_PROCESS_B2BORDER_REMINDER_RETRY);
    }

    @Bean
    MessageListenerAdapter b2bCenterOrderReminderProcessListenerAdapter(B2BCenterOrderReminderProcessMQReceiver b2BCenterOrderReminderProcessMQReceiver) {
        return new MessageListenerAdapter(b2BCenterOrderReminderProcessMQReceiver);
    }

    @Bean
    SimpleMessageListenerContainer b2bCenterOrderReminderProcessRetryContainer(MessageListenerAdapter b2bCenterOrderReminderProcessListenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(manualConnectionFactory());
        container.setQueues(b2bCenterOrderReminderProcessQueue());
        container.setAcknowledgeMode(AcknowledgeMode.valueOf(acknowledgeMode.toUpperCase()));
        container.setConcurrentConsumers(concurrency);
        container.setMaxConcurrentConsumers(maxConcurrency);
        container.setPrefetchCount(prefetch);
        container.setMessageListener(b2bCenterOrderReminderProcessListenerAdapter);
        return container;
    }

}
