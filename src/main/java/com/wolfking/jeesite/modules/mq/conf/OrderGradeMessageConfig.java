package com.wolfking.jeesite.modules.mq.conf;

import com.wolfking.jeesite.modules.mq.receiver.OrderGradeMessageReceiver;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 订单客评消息队列
 * 1.保存客评信息(sd_order_grade)
 * 2.更新网点及安维评分
 */
@Configuration
public class OrderGradeMessageConfig extends CommonConfig {

    public static final String MQ_ORDER_GRADE = "MQ:ORDER:GRADE";
    
    @Bean
    public Queue orderGradeMessageQueue() {
        return new Queue(MQ_ORDER_GRADE, true);
    }

    @Bean
    DirectExchange orderGradeMessageExchange() {
        return new DirectExchange(MQ_ORDER_GRADE);
    }

    @Bean
    Binding bindingOrderGradeMessageExchange(Queue orderGradeMessageQueue, DirectExchange orderGradeMessageExchange) {
        return BindingBuilder.bind(orderGradeMessageQueue)
                .to(orderGradeMessageExchange)
                .with(MQ_ORDER_GRADE);
    }

    @Bean
    MessageListenerAdapter orderGradeMessageListenerAdapter(OrderGradeMessageReceiver orderGradeMessageMQReceiver) {
        return new MessageListenerAdapter(orderGradeMessageMQReceiver);
    }

    @Bean
    SimpleMessageListenerContainer orderGradeMessageContainer(MessageListenerAdapter orderGradeMessageListenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(manualConnectionFactory());
        container.setQueues(orderGradeMessageQueue());
        container.setAcknowledgeMode(AcknowledgeMode.valueOf(acknowledgeMode.toUpperCase()));
        container.setConcurrentConsumers(concurrency);
        container.setMaxConcurrentConsumers(maxConcurrency);
        container.setPrefetchCount(prefetch);
        container.setMessageListener(orderGradeMessageListenerAdapter);
        return container;
    }

}
