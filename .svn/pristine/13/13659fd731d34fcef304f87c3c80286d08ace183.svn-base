package com.wolfking.jeesite.modules.mq.conf.voice;

import com.kkl.kklplus.entity.voiceservice.mq.VoiceServiceMQConstant;
import com.wolfking.jeesite.modules.mq.conf.CommonConfig;
import com.wolfking.jeesite.modules.mq.receiver.voice.CallbackReceiver;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.text.MessageFormat;

/**
 * api接口回调消息队列
 */
@Configuration
public class CallbackMQConfig extends CommonConfig {

    @Value("${site.code}")
    private String siteCode;

    //private final String QUEUE_NAME = MessageFormat.format("{0}:{1}", VoiceServiceMQConstant.MQ_VOICE_RECEIVE_CALLBACK,siteCode);

    @Bean
    public Queue callbackQueue() {
        return new Queue(MessageFormat.format("{0}:{1}", VoiceServiceMQConstant.MQ_VOICE_RECEIVE_CALLBACK,siteCode), true);
    }

    @Bean
    DirectExchange callbackExchange() {
        return new DirectExchange(MessageFormat.format("{0}:{1}", VoiceServiceMQConstant.MQ_VOICE_RECEIVE_CALLBACK,siteCode));
    }

    @Bean
    Binding bindingCallbackExchangeMessage(Queue callbackQueue, DirectExchange callbackExchange) {
        return BindingBuilder.bind(callbackQueue)
                .to(callbackExchange)
                .with(MessageFormat.format("{0}:{1}", VoiceServiceMQConstant.MQ_VOICE_RECEIVE_CALLBACK,siteCode));
    }

    @Bean
    SimpleMessageListenerContainer callbackContainer(ConnectionFactory manualConnectionFactory, MessageListenerAdapter callbackListenerAdapter) {
        SimpleMessageListenerContainer callbackContainer = new SimpleMessageListenerContainer();
        callbackContainer.setConnectionFactory(manualConnectionFactory);
        callbackContainer.setQueues(callbackQueue());
        callbackContainer.setAcknowledgeMode(AcknowledgeMode.valueOf(acknowledgeMode.toUpperCase()));
        callbackContainer.setConcurrentConsumers(concurrency);
        callbackContainer.setMaxConcurrentConsumers(maxConcurrency);
        callbackContainer.setPrefetchCount(prefetch);
        callbackContainer.setMessageListener(callbackListenerAdapter);
        return callbackContainer;
    }

    @Bean
    MessageListenerAdapter callbackListenerAdapter(CallbackReceiver receiver) {
        return new MessageListenerAdapter(receiver);
    }
}
