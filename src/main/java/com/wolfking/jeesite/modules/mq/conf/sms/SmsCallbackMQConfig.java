package com.wolfking.jeesite.modules.mq.conf.sms;

import com.kkl.kklplus.entity.voiceservice.mq.VoiceServiceMQConstant;
import com.wolfking.jeesite.modules.mq.conf.CommonConfig;
import com.wolfking.jeesite.modules.mq.receiver.sms.SmsCallbackReceiver;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 短信回访回调消息队列
 */
@Configuration
public class SmsCallbackMQConfig extends CommonConfig {

    @Value("${site.code}")
    private String siteCode;

    @Bean
    public Queue smsCallbackQueue() {
        return new Queue(VoiceServiceMQConstant.MQ_SMS_RECEIVE_CALLBACK, true);
    }

    @Bean
    DirectExchange smsCallbackExchange() {
        return new DirectExchange(VoiceServiceMQConstant.MQ_SMS_RECEIVE_CALLBACK);
    }

    @Bean
    Binding bindingSmsCallbackExchangeMessage(Queue smsCallbackQueue, DirectExchange smsCallbackExchange) {
        return BindingBuilder.bind(smsCallbackQueue)
                .to(smsCallbackExchange)
                .with(VoiceServiceMQConstant.MQ_SMS_RECEIVE_CALLBACK);
    }

    @Bean
    SimpleMessageListenerContainer smsCallbackContainer(ConnectionFactory manualConnectionFactory, MessageListenerAdapter smsCallbackListenerAdapter) {
        SimpleMessageListenerContainer callbackContainer = new SimpleMessageListenerContainer();
        callbackContainer.setConnectionFactory(manualConnectionFactory);
        callbackContainer.setQueues(smsCallbackQueue());
        callbackContainer.setAcknowledgeMode(AcknowledgeMode.valueOf(acknowledgeMode.toUpperCase()));
        callbackContainer.setConcurrentConsumers(concurrency);
        callbackContainer.setMaxConcurrentConsumers(maxConcurrency);
        callbackContainer.setPrefetchCount(prefetch);
        callbackContainer.setMessageListener(smsCallbackListenerAdapter);
        return callbackContainer;
    }

    @Bean
    MessageListenerAdapter smsCallbackListenerAdapter(SmsCallbackReceiver receiver) {
        return new MessageListenerAdapter(receiver);
    }
}
