package com.wolfking.jeesite.modules.mq.conf;

import com.kkl.kklplus.entity.applet.mq.MQConstant;
import com.wolfking.jeesite.modules.mq.receiver.AppletUserGradeOrderMQReceiver;
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
public class AppletUserGradeOrderMQConfig extends CommonConfig {

    @Bean
    public Queue appletUserGradeOrderQueue() {
        return new Queue(MQConstant.APPLET_USER_GRADE_ORDER, true);
    }

    @Bean
    DirectExchange appletUserGradeOrderExchange() {
        return (DirectExchange) ExchangeBuilder.directExchange(MQConstant.APPLET_USER_GRADE_ORDER).delayed().withArgument("x-delayed-type", "direct").build();
    }

    @Bean
    Binding bindingCreateCustomerBlockCurrencyExchangeMessage() {
        return BindingBuilder.bind(appletUserGradeOrderQueue()).to(appletUserGradeOrderExchange()).with(MQConstant.APPLET_USER_GRADE_ORDER);
    }

    @Bean
    SimpleMessageListenerContainer appletUserGradeOrderContainer(ConnectionFactory manualConnectionFactory, MessageListenerAdapter appletUserGradeOrderListenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(manualConnectionFactory);
        container.setQueues(appletUserGradeOrderQueue());
        container.setAcknowledgeMode(AcknowledgeMode.valueOf(acknowledgeMode.toUpperCase()));
        container.setConcurrentConsumers(concurrency);
        container.setMaxConcurrentConsumers(maxConcurrency);
        container.setPrefetchCount(prefetch);
        container.setMessageListener(appletUserGradeOrderListenerAdapter);
        return container;
    }

    @Bean
    MessageListenerAdapter appletUserGradeOrderListenerAdapter(AppletUserGradeOrderMQReceiver receiver) {
        return new MessageListenerAdapter(receiver);
    }
    

}
