package com.wolfking.jeesite.ms.recharge.mq.config;

import com.kkl.kklplus.entity.fi.mq.MQConstant;
import com.wolfking.jeesite.modules.mq.conf.CommonConfig;
import com.wolfking.jeesite.ms.recharge.mq.receiver.RechargeOrderRetryMQReceiver;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RechargeOrderRetryMQConfig extends CommonConfig{

    @Bean
    public Queue rechargeOrderRetryQueue() {
        return new Queue(MQConstant.MS_MQ_RECHARGE_NOTIFY_RETRY, true);
    }

    @Bean
    DirectExchange rechargeOrderRetryExchange() {
        return (DirectExchange) ExchangeBuilder.directExchange(MQConstant.MS_MQ_RECHARGE_NOTIFY_RETRY).
                delayed().withArgument("x-delayed-type", "direct").build();
    }

    @Bean
    Binding rechargeOrderExchangeMessage(Queue rechargeOrderRetryQueue, DirectExchange rechargeOrderRetryExchange) {
        return BindingBuilder.bind(rechargeOrderRetryQueue)
                .to(rechargeOrderRetryExchange)
                .with(MQConstant.MS_MQ_RECHARGE_NOTIFY_RETRY);
    }

    @Bean
    MessageListenerAdapter rechargeOrderRetryListenerAdapter(RechargeOrderRetryMQReceiver rechargeOrderRetryMQReceiver) {
        return new MessageListenerAdapter(rechargeOrderRetryMQReceiver);
    }

    @Bean
    SimpleMessageListenerContainer rechargeOrderRetryContainer(MessageListenerAdapter rechargeOrderRetryListenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(manualConnectionFactory());
        container.setQueues(rechargeOrderRetryQueue());
        container.setAcknowledgeMode(AcknowledgeMode.valueOf(acknowledgeMode.toUpperCase()));
        container.setConcurrentConsumers(concurrency);
        container.setMaxConcurrentConsumers(maxConcurrency);
        container.setPrefetchCount(prefetch);
        container.setMessageListener(rechargeOrderRetryListenerAdapter);
        return container;
    }

}
