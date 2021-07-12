package com.wolfking.jeesite.modules.mq.conf;

import com.wolfking.jeesite.modules.mq.receiver.WSMessageReceiver;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.context.annotation.Bean;

//@Configuration
public class WSMessageConfig extends CommonConfig {

    //WebSocket发送队列
    public static final String MQ_WEBSOCKET_EXCHANGE = "MQ:MESSAGE:WEBSOCKET:EXCHANGE";
    public static final String MQ_WEBSOCKET_ROUTING = "MQ:MESSAGE:WEBSOCKET:ROUTING";
    public static final String MQ_WEBSOCKET_QUEUE = "MQ:MESSAGE:WEBSOCKET:QUEUE";
    public static final String MQ_WEBSOCKET_COUNTER  = "WEBSOCKET";

    @Bean
    public Queue wsMessageQueue() {
        return new Queue(MQ_WEBSOCKET_QUEUE, true);
    }

    @Bean
    DirectExchange wsMessageExchange() {
        return new DirectExchange(MQ_WEBSOCKET_EXCHANGE);
    }

    @Bean
    Binding wsMessageBindingExchangeMessage() {
        return BindingBuilder.bind(wsMessageQueue()).to(wsMessageExchange()).with(MQ_WEBSOCKET_ROUTING);
    }

    @Bean
    SimpleMessageListenerContainer wsMessageContainer(ConnectionFactory manualConnectionFactory, MessageListenerAdapter wsMessageListenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(manualConnectionFactory);
        container.setQueues(wsMessageQueue());
        container.setAcknowledgeMode(AcknowledgeMode.valueOf(acknowledgeMode.toUpperCase()));
        container.setConcurrentConsumers(concurrency);
        container.setMaxConcurrentConsumers(maxConcurrency);
        container.setPrefetchCount(prefetch);
        container.setMessageListener(wsMessageListenerAdapter);
        return container;
    }

    @Bean
    MessageListenerAdapter wsMessageListenerAdapter(WSMessageReceiver receiver) {
        return new MessageListenerAdapter(receiver);
    }

}
