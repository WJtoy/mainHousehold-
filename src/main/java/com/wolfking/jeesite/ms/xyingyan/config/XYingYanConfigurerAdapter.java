package com.wolfking.jeesite.ms.xyingyan.config;

import com.wolfking.jeesite.ms.common.config.MicroServicesProperties;
import com.wolfking.jeesite.ms.xyingyan.handler.XYingYanSecurityHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Component
public class XYingYanConfigurerAdapter extends WebMvcConfigurerAdapter {

    @Autowired
    private MicroServicesProperties properties;

    @Autowired
    private XYingYanSecurityHandler handler;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        String[] methods = new String[properties.getXYingYan().getMethods().size()];
        properties.getXYingYan().getMethods().toArray(methods);
        registry.addInterceptor(handler).addPathPatterns(methods);
        super.addInterceptors(registry);
    }

}
