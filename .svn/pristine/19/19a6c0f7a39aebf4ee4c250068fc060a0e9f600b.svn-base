package com.wolfking.jeesite.modules.mq.conf;

import com.wolfking.jeesite.modules.mq.receiver.ShortMessageReceiver;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ShortMessageConfig extends CommonConfig {

    //短信发送队列
    public static final String MQ_SHORTMESSAGE_EXCHANGE = "MQ:MESSAGE:SHORTMESSAGE:EXCHANGE";
    public static final String MQ_SHORTMESSAGE_ROUTING = "MQ:MESSAGE:SHORTMESSAGE:ROUTING";
    public static final String MQ_SHORTMESSAGE_QUEUE = "MQ:MESSAGE:SHORTMESSAGE:QUEUE";
    public static final String MQ_SHORTMESSAGE_COUNTER  = "SHORTMESSAGE";

    @Bean
    public Queue shortMessageQueue() {
        return new Queue(MQ_SHORTMESSAGE_QUEUE, true);
    }

    @Bean
    DirectExchange shortMessageExchange() {
        return new DirectExchange(MQ_SHORTMESSAGE_EXCHANGE);
    }

    @Bean
    Binding shortMessageBindingExchangeMessage() {
        return BindingBuilder.bind(shortMessageQueue()).to(shortMessageExchange()).with(MQ_SHORTMESSAGE_ROUTING);
    }

    @Bean
    SimpleMessageListenerContainer shortMessageContainer(ConnectionFactory manualConnectionFactory, MessageListenerAdapter shortMessageListenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(manualConnectionFactory);
        container.setQueues(shortMessageQueue());
        container.setAcknowledgeMode(AcknowledgeMode.valueOf(acknowledgeMode.toUpperCase()));
        container.setConcurrentConsumers(concurrency);
        container.setMaxConcurrentConsumers(maxConcurrency);
        container.setPrefetchCount(prefetch);
        container.setMessageListener(shortMessageListenerAdapter);
        return container;
    }

    @Bean
    MessageListenerAdapter shortMessageListenerAdapter(ShortMessageReceiver receiver) {
        return new MessageListenerAdapter(receiver);
    }

}
