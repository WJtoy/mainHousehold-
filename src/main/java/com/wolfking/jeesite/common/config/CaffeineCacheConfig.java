package com.wolfking.jeesite.common.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * 本地缓存配置
 * @author: Ryan
 * @date: 2021/4/7 下午2:10
 * @Description:
 */
@Configuration
public class CaffeineCacheConfig {
    @Bean
    public Cache<String, Object> caffeineCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .initialCapacity(100)
                .weakKeys()
                .weakValues()
                .maximumSize(1000).build();
    }
}
