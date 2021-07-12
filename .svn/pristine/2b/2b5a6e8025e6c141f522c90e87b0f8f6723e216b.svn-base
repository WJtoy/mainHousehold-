package com.wolfking.jeesite.modules.mq.conf;

import com.kkl.kklplus.entity.sys.mq.MQConstant;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SysUserCustomerConfig {

    @Bean
    public Queue sysUserCustomerQueue() {
        return new Queue(MQConstant.MS_MQ_SYS_USER_CUSTOMER, true);
    }

    @Bean
    public DirectExchange sysUserCustomerDirectExchange() {
        return (DirectExchange) ExchangeBuilder.directExchange(MQConstant.MS_MQ_SYS_USER_CUSTOMER).delayed().withArgument("x-delayed-type", "direct").build();
    }

    @Bean
    public Binding sysUserCustomerBinding(Queue sysUserCustomerQueue, DirectExchange sysUserCustomerDirectExchange) {
        return BindingBuilder.bind(sysUserCustomerQueue).to(sysUserCustomerDirectExchange).with(MQConstant.MS_MQ_SYS_USER_CUSTOMER);
    }

}
