package com.wolfking.jeesite.modules.mq.conf;

import com.wolfking.jeesite.modules.mq.receiver.OrderReportReceiver;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 订单统计数据队列
 */
@Configuration
public class OrderReportConfig extends CommonConfig{

    public static final String MQ_ORDER_REPORT_EXCHANGE = "MQ:ORDER:REPORT:EXCHANGE";
    public static final String MQ_ORDER_REPORT_ROUTING = "MQ:ORDER:REPORT:ROUTING";
    public static final String MQ_ORDER_REPORT_QUEUE = "MQ:ORDER:REPORT:QUEUE";
    public static final String MQ_ORDER_REPORT_COUNTER  = "ORDERREPORT";

    @Bean
    public Queue orderReportQueue() {
        return new Queue(MQ_ORDER_REPORT_QUEUE, true);
    }

    @Bean
    DirectExchange orderReportExchange() {
        return new DirectExchange(MQ_ORDER_REPORT_EXCHANGE);
    }

    @Bean
    Binding bindingOrderReportExchangeMessage() {
        return BindingBuilder.bind(orderReportQueue()).to(orderReportExchange()).with(MQ_ORDER_REPORT_ROUTING);
    }

    @Bean
    SimpleMessageListenerContainer reportContainer(ConnectionFactory manualConnectionFactory, MessageListenerAdapter reportListenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(manualConnectionFactory);
        container.setQueues(orderReportQueue());
        container.setAcknowledgeMode(AcknowledgeMode.valueOf(acknowledgeMode.toUpperCase()));
        container.setConcurrentConsumers(concurrency);
        container.setMaxConcurrentConsumers(maxConcurrency);
        container.setPrefetchCount(prefetch);
        container.setMessageListener(reportListenerAdapter);
        return container;
    }

    @Bean
    MessageListenerAdapter reportListenerAdapter(OrderReportReceiver receiver) {
        return new MessageListenerAdapter(receiver);
    }
    

}
