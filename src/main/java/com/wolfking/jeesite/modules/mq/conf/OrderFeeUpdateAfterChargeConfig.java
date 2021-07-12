package com.wolfking.jeesite.modules.mq.conf;

import com.wolfking.jeesite.modules.mq.dto.MQOrderCharge;
import com.wolfking.jeesite.modules.mq.receiver.OderFeeUpdateAfterChargeReceiver;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Base64;

/**
 * 对账后更新财务扣费同步到订单
 * @author Ryan
 * @date 2020-04-03
 */
@Configuration
public class OrderFeeUpdateAfterChargeConfig extends CommonConfig {

    public static final String MQ_ORDER_FEE_UPDATE_AFTER_CHARGE = "MQ:ORDER:FEE:UPDATE:AFTER:CHARGE";
    public static final String MQ_ORDER_FEE_AFTER_CHARGE_COUNTER  = "FI.OrderFeeUpdateAfterCharge";

    @Bean
    public Queue orderFeeUpdateAfterChargeQueue() {
        return new Queue(MQ_ORDER_FEE_UPDATE_AFTER_CHARGE, true);
    }

    @Bean
    DirectExchange orderFeeUpdateAfterChargeExchange() {
        return (DirectExchange) ExchangeBuilder.directExchange(MQ_ORDER_FEE_UPDATE_AFTER_CHARGE).delayed().withArgument("x-delayed-type", "direct").build();
    }

    @Bean
    Binding bindingOrderFeeUpdateAfterChargeExchangeMessage(Queue orderFeeUpdateAfterChargeQueue, DirectExchange orderFeeUpdateAfterChargeExchange) {
        return BindingBuilder.bind(orderFeeUpdateAfterChargeQueue).to(orderFeeUpdateAfterChargeExchange).with(MQ_ORDER_FEE_UPDATE_AFTER_CHARGE);
    }

    @Bean
    SimpleMessageListenerContainer orderFeeUpdateAfterChargeContainer(ConnectionFactory manualConnectionFactory, MessageListenerAdapter orderFeeUpdateAfterChargeListenerAdapter) {
        SimpleMessageListenerContainer orderChargeContainer = new SimpleMessageListenerContainer();
        orderChargeContainer.setConnectionFactory(manualConnectionFactory);
        orderChargeContainer.setQueues(orderFeeUpdateAfterChargeQueue());
        orderChargeContainer.setAcknowledgeMode(AcknowledgeMode.valueOf(acknowledgeMode.toUpperCase()));
        orderChargeContainer.setConcurrentConsumers(concurrency);
        orderChargeContainer.setMaxConcurrentConsumers(maxConcurrency);
        orderChargeContainer.setPrefetchCount(prefetch);
        orderChargeContainer.setMessageListener(orderFeeUpdateAfterChargeListenerAdapter);
        return orderChargeContainer;
    }

    @Bean
    MessageListenerAdapter orderFeeUpdateAfterChargeListenerAdapter(OderFeeUpdateAfterChargeReceiver receiver) {
        return new MessageListenerAdapter(receiver);
    }

}
