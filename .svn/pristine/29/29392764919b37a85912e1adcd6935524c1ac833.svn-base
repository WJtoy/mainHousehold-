package com.wolfking.jeesite.modules.mq.conf.sms;

import com.kkl.kklplus.entity.voiceservice.mq.VoiceServiceMQConstant;
import com.wolfking.jeesite.modules.mq.conf.CommonConfig;
import com.wolfking.jeesite.modules.mq.receiver.sms.SmsCallbackNoGradeReceiver;
import com.wolfking.jeesite.modules.mq.receiver.sms.SmsCallbackReceiver;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 非客评短信回复消息队列
 */
@Configuration
public class SmsCallbackNoGradeMQConfig extends CommonConfig {

    @Value("${site.code}")
    private String siteCode;

    @Bean
    public Queue smsCallbackNoGradeQueue() {
        return new Queue(VoiceServiceMQConstant.MQ_SMS_RECEIVE_CALLBACK_NOGRADE, true);
    }

    @Bean
    DirectExchange smsCallbackNoGradeExchange() {
        return new DirectExchange(VoiceServiceMQConstant.MQ_SMS_RECEIVE_CALLBACK_NOGRADE);
    }

    @Bean
    Binding bindingSmsCallbackExchangeMessage(Queue smsCallbackNoGradeQueue, DirectExchange smsCallbackNoGradeExchange) {
        return BindingBuilder.bind(smsCallbackNoGradeQueue)
                .to(smsCallbackNoGradeExchange)
                .with(VoiceServiceMQConstant.MQ_SMS_RECEIVE_CALLBACK_NOGRADE);
    }

    @Bean
    SimpleMessageListenerContainer smsCallbackNoGradeContainer(ConnectionFactory manualConnectionFactory, MessageListenerAdapter smsCallbackNoGradeListenerAdapter) {
        SimpleMessageListenerContainer callbackContainer = new SimpleMessageListenerContainer();
        callbackContainer.setConnectionFactory(manualConnectionFactory);
        callbackContainer.setQueues(smsCallbackNoGradeQueue());
        callbackContainer.setAcknowledgeMode(AcknowledgeMode.valueOf(acknowledgeMode.toUpperCase()));
        callbackContainer.setConcurrentConsumers(concurrency);
        callbackContainer.setMaxConcurrentConsumers(maxConcurrency);
        callbackContainer.setPrefetchCount(prefetch);
        callbackContainer.setMessageListener(smsCallbackNoGradeListenerAdapter);
        return callbackContainer;
    }

    @Bean
    MessageListenerAdapter smsCallbackNoGradeListenerAdapter(SmsCallbackNoGradeReceiver receiver) {
        return new MessageListenerAdapter(receiver);
    }
}
