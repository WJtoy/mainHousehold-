package com.wolfking.jeesite.modules.mq.conf;

import com.wolfking.jeesite.modules.mq.receiver.NoticeMessageReceiver;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NoticeMessageConfig extends CommonConfig {

    //提醒消息
    public static final String MQ_NOTICE_EXCHANGE   = "MQ:MESSAGE:NOTICE:EXCHANGE";
    public static final String MQ_NOTICE_ROUTING    = "MQ:MESSAGE:NOTICE:ROUTING";
    public static final String MQ_NOTICE_QUEUE      = "MQ:MESSAGE:NOTICE:QUEUE";
    public static final String MQ_NOTICE_COUNTER    = "WEBNOTICE";

    public static final int NOTICE_TYPE_FEEDBACK = 1; // 未读问题反馈(未读消息)
    public static final int NOTICE_TYPE_FEEDBACK_PENDING = 2; // 待处理问题反馈（反馈未处理）
    public static final int NOTICE_TYPE_APPABNORMALY = 3; // app异常(异常反馈)

    @Bean
    public Queue noticeMessageQueue() {
        return new Queue(MQ_NOTICE_QUEUE, true);
    }

    @Bean
    DirectExchange noticeMessageExchange() {
        return new DirectExchange(MQ_NOTICE_EXCHANGE);
    }

    @Bean
    Binding wsMessageBindingExchangeMessage() {
        return BindingBuilder.bind(noticeMessageQueue()).to(noticeMessageExchange()).with(MQ_NOTICE_ROUTING);
    }

    @Bean
    SimpleMessageListenerContainer noticeMessageContainer(ConnectionFactory manualConnectionFactory, MessageListenerAdapter noticeMessageListenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(manualConnectionFactory);
        container.setQueues(noticeMessageQueue());
        container.setAcknowledgeMode(AcknowledgeMode.valueOf(acknowledgeMode.toUpperCase()));
        container.setConcurrentConsumers(concurrency);
        container.setMaxConcurrentConsumers(maxConcurrency);
        container.setPrefetchCount(prefetch);
        container.setMessageListener(noticeMessageListenerAdapter);
        return container;
    }

    @Bean
    MessageListenerAdapter noticeMessageListenerAdapter(NoticeMessageReceiver receiver) {
        return new MessageListenerAdapter(receiver);
    }

}
