package com.wolfking.jeesite;

import com.google.gson.GsonBuilder;
import com.wolfking.jeesite.common.config.redis.GsonIgnoreStrategy;
import com.wolfking.jeesite.modules.md.utils.CustomerUtils;
import com.wolfking.jeesite.modules.sys.service.SystemService;
import com.wolfking.jeesite.modules.sys.utils.DictUtils;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;

/**
 * wolfking-jeesite
 * springboot的启动类
 * Created by wolfking(赵伟伟)
 * Created on 2017/1/8 16:20
 * Mail zww199009@163.com
 */
/*
@EnableCaching
//@SpringBootApplication
//@EnableScheduling 启用定时任务
@ServletComponentScan("com.wolfking.jeesite")
@ComponentScan(value = "com.wolfking.jeesite",lazyInit = true)
public class WolfkingJeesiteDriver extends WebMvcConfigurerAdapter {
    public static void main(String[] args) {
        new SpringApplicationBuilder(WolfkingJeesiteDriver.class).web(true).run(args);
        SystemService.printKeyLoadMessage();
    }
}
*/
