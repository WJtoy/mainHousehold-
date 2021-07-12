package com.wolfking.jeesite.ms.b2bcenter.config;

import com.wolfking.jeesite.ms.b2bcenter.handler.CreateOrderNoSecurityHandler;
import com.wolfking.jeesite.ms.common.config.MicroServicesProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Component
public class CreateOrderNoConfigurerAdapter extends WebMvcConfigurerAdapter {

    @Autowired
    private MicroServicesProperties properties;

    @Autowired
    private CreateOrderNoSecurityHandler handler;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (properties.getB2bCreateOrderNo().getMethods().size() > 0) {
            String[] methods = new String[properties.getB2bCreateOrderNo().getMethods().size()];
            properties.getB2bCreateOrderNo().getMethods().toArray(methods);
            registry.addInterceptor(handler).addPathPatterns(methods);
        }
        super.addInterceptors(registry);
    }

}
