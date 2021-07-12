package com.wolfking.jeesite.modules.mq.conf;

import com.kkl.kklplus.entity.sys.mq.MQConstant;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SysUserSubConfig {

    @Bean
    public Queue sysUserSubQueue() {
        return new Queue(MQConstant.MS_MQ_SYS_USER_SUB, true);
    }

    @Bean
    public DirectExchange sysUserSubDirectExchange() {
        return (DirectExchange) ExchangeBuilder.directExchange(MQConstant.MS_MQ_SYS_USER_SUB).delayed().withArgument("x-delayed-type", "direct").build();
    }

    @Bean
    public Binding sysUserSubBinding(Queue sysUserSubQueue, DirectExchange sysUserSubDirectExchange) {
        return BindingBuilder.bind(sysUserSubQueue).to(sysUserSubDirectExchange).with(MQConstant.MS_MQ_SYS_USER_SUB);
    }

}
