package com.wolfking.jeesite.ms.logistics.mq.config;

import com.kkl.kklplus.entity.lm.mq.MQConstant;
import com.wolfking.jeesite.modules.mq.conf.CommonConfig;
import com.wolfking.jeesite.ms.logistics.mq.receiver.LMArrivalDateMQReceiver;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 订阅快递单到货通知消息
 * 2019/08/29 更改启用配置：
 *  orderFlag(订单) 和materialFlag(配件，不包含返件) 开关，其中一个开关打开就启动消费端
 */
@Configuration
//@ConditionalOnProperty(name = "logistics.orderFlag", matchIfMissing = false)
@ConditionalOnExpression("${logistics.orderFlag:true} || ${logistics.materialFlag:true}")
public class LMArrivalDateMQConfig extends CommonConfig {

    @Bean
    public Queue lmArriveDateQueue() {
        return new Queue(MQConstant.MS_MQ_LM_ARRIVALDATE, true);
    }

    @Bean
    DirectExchange lmArriveDateExchange() {
        return new DirectExchange(MQConstant.MS_MQ_LM_ARRIVALDATE);
    }

    @Bean
    Binding bindingLMArriveDateExchangeMessage(Queue lmArriveDateQueue, DirectExchange lmArriveDateExchange) {
        return BindingBuilder.bind(lmArriveDateQueue)
                .to(lmArriveDateExchange)
                .with(MQConstant.MS_MQ_LM_ARRIVALDATE);
    }

    @Bean
    MessageListenerAdapter lmArriveDateListenerAdapter(LMArrivalDateMQReceiver lmArrivalDateMQReceiver) {
        return new MessageListenerAdapter(lmArrivalDateMQReceiver);
    }

    @Bean
    SimpleMessageListenerContainer lmArriveDateMQReceiverContainer(MessageListenerAdapter lmArriveDateListenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(manualConnectionFactory());
        container.setQueues(lmArriveDateQueue());
        container.setAcknowledgeMode(AcknowledgeMode.valueOf(acknowledgeMode.toUpperCase()));
        container.setConcurrentConsumers(concurrency);
        container.setMaxConcurrentConsumers(maxConcurrency);
        container.setPrefetchCount(prefetch);
        container.setMessageListener(lmArriveDateListenerAdapter);
        return container;
    }

}
