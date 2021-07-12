package com.wolfking.jeesite.ms.tmall.mq.config;

import com.kkl.kklplus.entity.b2b.mq.B2BMQConstant;
import com.wolfking.jeesite.modules.mq.conf.CommonConfig;
import com.wolfking.jeesite.ms.tmall.mq.receiver.WorkcardStatusUpdateMQReceiver;
import com.wolfking.jeesite.ms.tmall.mq.receiver.DelayWorkcardStatusUpdateMQReceiver;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


//@Configuration
public class WorkcardStatusUpdateMQConfig extends CommonConfig {
/*
    @Bean
    public Queue workcardStatusUpdateQueue() {
        return new Queue(B2BMQConstant.MQ_WORKCARDSTATUSUPDATE_QUEUE, true);
    }

    @Bean
    DirectExchange workcardStatusUpdateExchange() {
        return new DirectExchange(B2BMQConstant.MQ_WORKCARDSTATUSUPDATE_EXCHANGE);
    }

    @Bean
    Binding bindingWorkcardStatusUpdateExchangeMessage(Queue workcardStatusUpdateQueue, DirectExchange workcardStatusUpdateExchange) {
        return BindingBuilder.bind(workcardStatusUpdateQueue).
                to(workcardStatusUpdateExchange).
                with(B2BMQConstant.MQ_WORKCARDSTATUSUPDATE_ROUTING);
    }

    @Bean
    MessageListenerAdapter workcardStatusUpdateListenerAdapter(WorkcardStatusUpdateMQReceiver workcardStatusUpdateMQReceiver) {
        return new MessageListenerAdapter(workcardStatusUpdateMQReceiver);
    }

    @Bean
    SimpleMessageListenerContainer workcardStatusUpdateContainer(MessageListenerAdapter workcardStatusUpdateListenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(manualConnectionFactory());
        container.setQueues(workcardStatusUpdateQueue());
        container.setAcknowledgeMode(AcknowledgeMode.valueOf(acknowledgeMode.toUpperCase()));
        container.setConcurrentConsumers(concurrency);
        container.setMaxConcurrentConsumers(maxConcurrency);
        container.setPrefetchCount(prefetch);
        container.setMessageListener(workcardStatusUpdateListenerAdapter);
        return container;
    }


    @Bean
    public Queue delayWorkcardStatusUpdateQueue() {
        return new Queue(B2BMQConstant.MQ_DELAY_WORKCARDSTATUSUPDATE_QUEUE, true);
    }

    @Bean
    DirectExchange delayWorkcardStatusUpdateExchange() {
        return (DirectExchange) ExchangeBuilder.directExchange(B2BMQConstant.MQ_DELAY_WORKCARDSTATUSUPDATE_EXCHANGE).
                delayed().withArgument("x-delayed-type", "direct").build();
    }

    @Bean
    Binding bindingDelayWorkcardStatusUpdateExchangeMessage(Queue delayWorkcardStatusUpdateQueue, DirectExchange delayWorkcardStatusUpdateExchange) {
        return BindingBuilder.bind(delayWorkcardStatusUpdateQueue).
                to(delayWorkcardStatusUpdateExchange).
                with(B2BMQConstant.MQ_DELAY_WORKCARDSTATUSUPDATE_ROUTING);
    }

    @Bean
    MessageListenerAdapter delayWorkcardStatusUpdateListenerAdapter(DelayWorkcardStatusUpdateMQReceiver delayWorkcardStatusUpdateMQReceiver) {
        return new MessageListenerAdapter(delayWorkcardStatusUpdateMQReceiver);
    }

    @Bean
    SimpleMessageListenerContainer delayWorkcardStatusUpdateContainer(MessageListenerAdapter delayWorkcardStatusUpdateListenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(manualConnectionFactory());
        container.setQueues(delayWorkcardStatusUpdateQueue());
        container.setAcknowledgeMode(AcknowledgeMode.valueOf(acknowledgeMode.toUpperCase()));
        container.setConcurrentConsumers(concurrency);
        container.setMaxConcurrentConsumers(maxConcurrency);
        container.setPrefetchCount(prefetch);
        container.setMessageListener(delayWorkcardStatusUpdateListenerAdapter);
        return container;
    }
*/
}
