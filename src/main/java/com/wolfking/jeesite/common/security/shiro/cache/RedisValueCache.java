package com.wolfking.jeesite.common.security.shiro.cache;

import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.UnavailableSecurityManagerException;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.session.InvalidSessionException;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 *
 * 项目名称：
 *
 * 描述：
 *
 * 创建人：Ryan Lu
 *
 * 创建时间：2017年4月5日 下午2:56:55
 *
 * Copyright
 *
 */
@Slf4j
public class RedisValueCache<K, V> implements Cache<K, V>, Serializable {
    /**
     *
     */
    private static final long serialVersionUID = -327493024746553059L;

    @Resource
    private RedisTemplate<K, V> redisTemplate;

    private final String REDIS_SHIRO_CACHE = "shiro-cache:";
    private String cacheKey;
    public static long globExpire = 240;// 4*60分钟

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public RedisValueCache(String name, RedisTemplate redisTemplate) {
        this.cacheKey = this.REDIS_SHIRO_CACHE + name + ":";
        this.redisTemplate = redisTemplate;
    }

    public Session getSession(){
        Session session = null;
        try{
            Subject subject = SecurityUtils.getSubject();
            session = subject.getSession(false);
            if (session == null){
                session = subject.getSession();
            }
        }catch (InvalidSessionException e){
            log.error("Invalid session error", e);
        }catch (UnavailableSecurityManagerException e2){
            log.error("Unavailable SecurityManager error", e2);
        }
        return session;
    }

    @Override
    public V get(K key) throws CacheException {
        redisTemplate.boundValueOps(getCacheKey(key)).expire(globExpire, TimeUnit.MINUTES);
        return redisTemplate.boundValueOps(getCacheKey(key)).get();
    }

    @Override
    public V put(K key, V value) throws CacheException {
//        System.out.println("put key:"+key.toString());
        V old = get(key);
        redisTemplate.boundValueOps(getCacheKey(key)).set(value,globExpire, TimeUnit.MINUTES);
        return old;
    }

    @Override
    public V remove(K key) throws CacheException {
        V old = get(key);
        redisTemplate.delete(getCacheKey(key));
        return old;
    }

    @Override
    public void clear() throws CacheException {
        redisTemplate.delete(keys());
    }

    @Override
    public int size() {
        return keys().size();
    }

    @SuppressWarnings("unchecked")
    private K getCacheKey(Object k) {
        return (K) (this.cacheKey + k);
    }

    @Override
    public Set<K> keys() {
        return redisTemplate.keys(getCacheKey("*"));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Collection<V> values() {
        Set<K> set = keys();
        List list = new LinkedList();
        for (K s : set) {
            list.add(get(s));
        }
        return list;
    }

}
