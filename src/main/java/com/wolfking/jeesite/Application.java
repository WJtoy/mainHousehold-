package com.wolfking.jeesite;

import com.wolfking.jeesite.common.config.WebProperties;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.wolfking.jeesite.modules.api.config.RestAuthorizeFilter;
import com.wolfking.jeesite.modules.sys.service.SystemService;
import com.wolfking.jeesite.ms.common.config.MicroServicesProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jeff on 2017/4/17.
 */

@EnableAsync
@EnableCaching
@SpringBootApplication
@EnableScheduling //启用定时任务
@ServletComponentScan("com.wolfking.jeesite")
@ComponentScan(value = "com.wolfking.jeesite",lazyInit = true)
@EnableEurekaClient
@EnableCircuitBreaker
@EnableFeignClients
@EnableConfigurationProperties({MicroServicesProperties.class, WebProperties.class})
public class Application extends SpringBootServletInitializer {
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(Application.class);
    }

    @Autowired
    RedisUtils redisUtils;

//    @Autowired
//    ServicePointService servicePointService;

    @Bean
    public FilterRegistrationBean restAuthorizeFilter() {
        final FilterRegistrationBean registrationBean = new FilterRegistrationBean();
//        registrationBean.setFilter(new RestAuthorizeFilter(redisUtils,servicePointService));
        registrationBean.setFilter(new RestAuthorizeFilter(redisUtils));
        Map<String,String> m = new HashMap<String,String>();
        m.put("targetBeanName","restAuthorizeFilter");
        m.put("targetFilterLifecycle","true");
        registrationBean.setInitParameters(m);
        registrationBean.addUrlPatterns("/api/*");

        return registrationBean;
    }


    /**
     * 定时任务线程池
     * 根据任务多少修改
     */
    @Bean(name = "TaskPool")
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(5);
        return taskScheduler;
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
        SystemService.printKeyLoadMessage();
//        DictUtils.CacheAllDict();
    }
}
