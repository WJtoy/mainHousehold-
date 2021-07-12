package com.wolfking.jeesite.modules.mq.sender;

import com.kkl.kklplus.entity.sys.mq.MQConstant;
import com.kkl.kklplus.entity.sys.mq.MQSysUserCustomerMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
@Slf4j
public class SysUserCustomerSender implements RabbitTemplate.ConfirmCallback {
    private RabbitTemplate rabbitTemplate;
    private RetryTemplate retryTemplate;

    @Autowired
    public SysUserCustomerSender(RabbitTemplate manualRabbitTemplate, RetryTemplate kklRabbitRetryTemplate) {
        this.rabbitTemplate = manualRabbitTemplate;
        this.rabbitTemplate.setConfirmCallback(this);
        this.retryTemplate = kklRabbitRetryTemplate;
    }

    /**
     * 正常发送消息
     *
     * @param message 消息体
     */
    public boolean send(MQSysUserCustomerMessage.SysUserCustomerMessage message) {
        AtomicBoolean result = new AtomicBoolean(false);
        try {
            retryTemplate.execute((RetryCallback<Object, Exception>) context -> {
                context.setAttribute(MQConstant.MS_MQ_SYS_USER_CUSTOMER, message);
                rabbitTemplate.convertAndSend(
                        MQConstant.MS_MQ_SYS_USER_CUSTOMER,
                        MQConstant.MS_MQ_SYS_USER_CUSTOMER,
                        message.toByteArray(),
                        new CorrelationData());
                result.set(true);
                return null;
            }, context -> {
                Object msgObj = context.getAttribute(MQConstant.RETRY_CONTEXT_ATTRIBUTE_KEY_MESSAGE);
                MQSysUserCustomerMessage.SysUserCustomerMessage msg = MQSysUserCustomerMessage.SysUserCustomerMessage.parseFrom((byte[]) msgObj);
                Throwable throwable = context.getLastThrowable();
                log.error("normal send error {}, {}", throwable.getLocalizedMessage(), msg);
                return null;
            });
        } catch (Exception e) {
            log.error("SysUserCustomerSender.send：{}", e.getLocalizedMessage());
        }
        return result.get();
    }

    @Override
    public void confirm(CorrelationData correlationData, boolean b, String s) {

    }
}
