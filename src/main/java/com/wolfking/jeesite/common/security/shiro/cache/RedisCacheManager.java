package com.wolfking.jeesite.common.security.shiro.cache;


import javax.annotation.Resource;

import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisCacheManager implements CacheManager{

    @Resource(name="redisTemplate")
    private RedisTemplate<String,Object> redisTemplate;
//    private RedisTemplate<byte[],Object> redisTemplate;

    @Override
    public <K, V> Cache<K, V> getCache(String name) throws CacheException {
        return new RedisValueCache<>(name, redisTemplate);
    }

}
