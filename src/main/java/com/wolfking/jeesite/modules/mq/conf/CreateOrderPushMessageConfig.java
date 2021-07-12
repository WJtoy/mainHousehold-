package com.wolfking.jeesite.modules.mq.conf;

import com.wolfking.jeesite.modules.mq.receiver.CreateOrderPushMessageReceiver;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 下单消息队列
 */
@Configuration
public class CreateOrderPushMessageConfig extends CommonConfig {

    //APP消息推送队列
    public static final String MQ_CREATEORDER_PUSH_MESSAGE_EXCHANGE = "MQ:CREATEORDER:MESSAGE:EXCHANGE";
    public static final String MQ_CREATEORDER_PUSH_MESSAGE_ROUTING = "MQ:CREATEORDER:MESSAGE:ROUTING";
    public static final String MQ_CREATEORDER_PUSH_MESSAGE_QUEUE = "MQ:CREATEORDER:MESSAGE:QUEUE";
    public static final String MQ_CREATEORDER_PUSH_MESSAGE_COUNTER  = "CREATEORDERPUSHMESSAGE";
    public static final String MQ_PUSH_MESSAGE_ANDROID_COUNTER = "ANDROID";
    public static final String MQ_PUSH_MESSAGE_IOS_COUNTER = "IOS";

    @Bean
    public Queue createOrderPushMessageQueue() {
        return new Queue(MQ_CREATEORDER_PUSH_MESSAGE_QUEUE, true);
    }

    @Bean
    DirectExchange createOrderPushMessageExchange() {
        return new DirectExchange(MQ_CREATEORDER_PUSH_MESSAGE_EXCHANGE);
    }

    @Bean
    Binding bindingCreateOrderPushMessageExchangeMessage() {
        return BindingBuilder.bind(createOrderPushMessageQueue()).to(createOrderPushMessageExchange()).with(MQ_CREATEORDER_PUSH_MESSAGE_ROUTING);
    }

    @Bean
    SimpleMessageListenerContainer createOrderPushMessageContainer(ConnectionFactory manualConnectionFactory, MessageListenerAdapter createOrderPushMessageListenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(manualConnectionFactory);
        container.setQueues(createOrderPushMessageQueue());
        container.setAcknowledgeMode(AcknowledgeMode.valueOf(acknowledgeMode.toUpperCase()));
        container.setConcurrentConsumers(concurrency);
        container.setMaxConcurrentConsumers(maxConcurrency);
        container.setPrefetchCount(prefetch);
        container.setMessageListener(createOrderPushMessageListenerAdapter);//TODO 上线或提交时取注释
        return container;
    }

    @Bean
    MessageListenerAdapter createOrderPushMessageListenerAdapter(CreateOrderPushMessageReceiver receiver) {
        return new MessageListenerAdapter(receiver);
    }

}
