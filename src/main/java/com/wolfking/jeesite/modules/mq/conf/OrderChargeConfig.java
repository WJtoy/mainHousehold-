package com.wolfking.jeesite.modules.mq.conf;

import com.kkl.kklplus.entity.fi.mq.MQConstant;
import com.wolfking.jeesite.modules.mq.receiver.OrderChargeReceiver;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class OrderChargeConfig extends CommonConfig {

    //订单自动对账结账队列
    public static final String MQ_ORDER_CHARGE = "MQ:ORDER:CHARGE";
    public static final String MQ_ORDER_CHARGE_COUNTER  = "FI.OrderCharge";

    @Bean
    public Queue orderChargeQueue() {
        return new Queue(MQ_ORDER_CHARGE, true);
    }

    @Bean
    DirectExchange orderChargeExchange() {
        return (DirectExchange) ExchangeBuilder.directExchange(MQ_ORDER_CHARGE).delayed().withArgument("x-delayed-type", "direct").build();
    }

    @Bean
    Binding bindingOrderChargeExchangeMessage(Queue orderChargeQueue, DirectExchange orderChargeExchange) {
        return BindingBuilder.bind(orderChargeQueue).to(orderChargeExchange).with(MQ_ORDER_CHARGE);
    }

    @Bean
    SimpleMessageListenerContainer orderChargeContainer(ConnectionFactory manualConnectionFactory, MessageListenerAdapter orderChargeListenerAdapter) {
        SimpleMessageListenerContainer orderChargeContainer = new SimpleMessageListenerContainer();
        orderChargeContainer.setConnectionFactory(manualConnectionFactory);
        orderChargeContainer.setQueues(orderChargeQueue());
        orderChargeContainer.setAcknowledgeMode(AcknowledgeMode.valueOf(acknowledgeMode.toUpperCase()));
        orderChargeContainer.setConcurrentConsumers(concurrency);
        orderChargeContainer.setMaxConcurrentConsumers(maxConcurrency);
        orderChargeContainer.setPrefetchCount(prefetch);
        orderChargeContainer.setMessageListener(orderChargeListenerAdapter);
        return orderChargeContainer;
    }

    @Bean
    MessageListenerAdapter orderChargeListenerAdapter(OrderChargeReceiver receiver) {
        return new MessageListenerAdapter(receiver);
    }
}
