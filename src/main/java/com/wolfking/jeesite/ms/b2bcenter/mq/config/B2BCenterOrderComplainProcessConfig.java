package com.wolfking.jeesite.ms.b2bcenter.mq.config;

import com.kkl.kklplus.entity.b2b.mq.B2BMQConstant;
import com.wolfking.jeesite.modules.mq.conf.CommonConfig;
import com.wolfking.jeesite.ms.b2bcenter.mq.receiver.B2BCenterOrderComplainMQReceiver;
import com.wolfking.jeesite.ms.b2bcenter.mq.receiver.B2BCenterOrderComplainProcessMQReceiver;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * B2B-投诉处理消息
 * @author Ryan
 * @date 2020-10-13
 */
@Configuration
public class B2BCenterOrderComplainProcessConfig extends CommonConfig {
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
    public Queue b2bCenterOrderComplainProcessQueue() {
        return new Queue(B2BMQConstant.MQ_B2BCENTER_COMPLAIN_PROCESS_DELAY, true);
    }

    @Bean
    DirectExchange b2bCenterOrderComplainProcessExchange() {
        return (DirectExchange) ExchangeBuilder.directExchange(B2BMQConstant.MQ_B2BCENTER_COMPLAIN_PROCESS_DELAY).
                delayed().withArgument("x-delayed-type", "direct").build();
    }

    @Bean
    Binding bindingB2BCenterOrderComplainExchangeMessage(Queue b2bCenterOrderComplainProcessQueue, DirectExchange b2bCenterOrderComplainProcessExchange) {
        return BindingBuilder.bind(b2bCenterOrderComplainProcessQueue).
                to(b2bCenterOrderComplainProcessExchange).
                with(B2BMQConstant.MQ_B2BCENTER_COMPLAIN_PROCESS_DELAY);
    }

    @Bean
    MessageListenerAdapter b2bCenterOrderComplainProcessListenerAdapter(B2BCenterOrderComplainProcessMQReceiver b2BCenterOrderComplainProcessMQReceiver) {
        return new MessageListenerAdapter(b2BCenterOrderComplainProcessMQReceiver);
    }

    @Bean
    SimpleMessageListenerContainer b2bCenterOrderComplainProcessRetryContainer(MessageListenerAdapter b2bCenterOrderComplainProcessListenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(manualConnectionFactory());
        container.setQueues(b2bCenterOrderComplainProcessQueue());
        container.setAcknowledgeMode(AcknowledgeMode.valueOf(acknowledgeMode.toUpperCase()));
        container.setConcurrentConsumers(concurrency);
        container.setMaxConcurrentConsumers(maxConcurrency);
        container.setPrefetchCount(prefetch);
        container.setMessageListener(b2bCenterOrderComplainProcessListenerAdapter);
        return container;
    }

}
