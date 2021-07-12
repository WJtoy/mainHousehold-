package com.wolfking.jeesite.modules.mq.conf;

import com.wolfking.jeesite.modules.mq.receiver.OrderGradeMessageReceiver;
import com.wolfking.jeesite.modules.mq.receiver.OrderGradeRetryMessageReceiver;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 订单客评重试消息队列
 * 1.保存客评信息(sd_order_grade)
 * 2.更新网点及安维评分
 */
@Configuration
public class OrderGradeRetryMessageConfig extends CommonConfig {

    public static final String MQ_ORDER_GRADE_RETRY = "MQ:ORDER:GRADE:RETRY";

    /**
     * 重试的延迟时间
     */
    public static final int DELAY_MILLISECOND = 15 * 1000;
    /**
     * 重试次数
     */
    public static final int RETRY_TIMES = 3;

    @Bean
    public Queue orderGradeMessageRetryQueue() {
        return new Queue(MQ_ORDER_GRADE_RETRY, true);
    }

    @Bean
    DirectExchange orderGradeMessageRetryExchange() {
        return (DirectExchange) ExchangeBuilder.directExchange(MQ_ORDER_GRADE_RETRY).
                delayed().withArgument("x-delayed-type", "direct").build();
    }

    @Bean
    Binding bindingOrderGradeMessageRetryExchangeMessage(Queue orderGradeMessageRetryQueue, DirectExchange orderGradeMessageRetryExchange) {
        return BindingBuilder.bind(orderGradeMessageRetryQueue)
                .to(orderGradeMessageRetryExchange)
                .with(MQ_ORDER_GRADE_RETRY);
    }

    @Bean
    MessageListenerAdapter orderGradeMessageRetryListenerAdapter(OrderGradeRetryMessageReceiver orderGradeRetryMessageMQReceiver) {
        return new MessageListenerAdapter(orderGradeRetryMessageMQReceiver);
    }

    @Bean
    SimpleMessageListenerContainer orderGradeMessageRetryContainer(MessageListenerAdapter orderGradeMessageRetryListenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(manualConnectionFactory());
        container.setQueues(orderGradeMessageRetryQueue());
        container.setAcknowledgeMode(AcknowledgeMode.valueOf(acknowledgeMode.toUpperCase()));
        container.setConcurrentConsumers(concurrency);
        container.setMaxConcurrentConsumers(maxConcurrency);
        container.setPrefetchCount(prefetch);
        container.setMessageListener(orderGradeMessageRetryListenerAdapter);
        return container;
    }

}
