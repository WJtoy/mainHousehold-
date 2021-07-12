package com.wolfking.jeesite.ms.b2bcenter.mq.config;

import com.kkl.kklplus.entity.b2b.mq.B2BMQConstant;
import com.wolfking.jeesite.modules.mq.conf.CommonConfig;
import com.wolfking.jeesite.ms.b2bcenter.mq.receiver.B2BCenterModifyB2BOrderRetryMQReceiver;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class B2BCenterModifyB2BOrderRetryMQConfig extends CommonConfig {

    /**
     * 重试的延迟时间
     */
    public static final int DELAY_MILLISECOND = 15 * 1000;
    /**
     * 重试次数
     */
    public static final int RETRY_TIMES = 3;

    @Bean
    public Queue b2bCenterModifyB2BOrderRetryQueue() {
        return new Queue(B2BMQConstant.MQ_B2BCENTER_MODIFY_B2B_ORDER_RETRY, true);
    }

    @Bean
    DirectExchange b2bCenterModifyB2BOrderRetryExchange() {
        return (DirectExchange) ExchangeBuilder.directExchange(B2BMQConstant.MQ_B2BCENTER_MODIFY_B2B_ORDER_RETRY).
                delayed().withArgument("x-delayed-type", "direct").build();
    }

    @Bean
    Binding bindingB2BCenterModifyB2BOrderRetryExchangeMessage(Queue b2bCenterModifyB2BOrderRetryQueue, DirectExchange b2bCenterModifyB2BOrderRetryExchange) {
        return BindingBuilder.bind(b2bCenterModifyB2BOrderRetryQueue).
                to(b2bCenterModifyB2BOrderRetryExchange).
                with(B2BMQConstant.MQ_B2BCENTER_MODIFY_B2B_ORDER_RETRY);
    }

    @Bean
    MessageListenerAdapter b2bCenterModifyB2BOrderRetryListenerAdapter(B2BCenterModifyB2BOrderRetryMQReceiver b2BCenterModifyB2BOrderRetryMQReceiver) {
        return new MessageListenerAdapter(b2BCenterModifyB2BOrderRetryMQReceiver);
    }

    @Bean
    SimpleMessageListenerContainer b2bCenterModifyB2BOrderRetryContainer(MessageListenerAdapter b2bCenterModifyB2BOrderRetryListenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(manualConnectionFactory());
        container.setQueues(b2bCenterModifyB2BOrderRetryQueue());
        container.setAcknowledgeMode(AcknowledgeMode.valueOf(acknowledgeMode.toUpperCase()));
        container.setConcurrentConsumers(concurrency);
        container.setMaxConcurrentConsumers(maxConcurrency);
        container.setPrefetchCount(prefetch);
        container.setMessageListener(b2bCenterModifyB2BOrderRetryListenerAdapter);
        return container;
    }

}
