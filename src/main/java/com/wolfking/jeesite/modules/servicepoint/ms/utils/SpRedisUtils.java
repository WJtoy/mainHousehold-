package com.wolfking.jeesite.modules.servicepoint.ms.utils;

import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class SpRedisUtils {


    @Autowired
    private RedisUtils redisUtils;


    /**
     * 判断缓存中是否有对应的key
     */
    public Boolean exists(final RedisConstant.RedisDBType dbType, final String key) {
        return redisUtils.exists(dbType, key);
    }

    /**
     * 写入缓存,是『SET if Not eXists』(如果不存在，则 SET)的简写。
     * key不存在，写入，返回true
     * key存在，不覆盖，返回false
     *
     * @param expireSeconds 过期时间（单位:秒）
     */
    public Boolean setNX(final RedisConstant.RedisDBType dbType, final String key, Object value, long expireSeconds) {
        return redisUtils.setNX(dbType, key, value, expireSeconds);
    }

    /**
     * 删除对应的value
     */
    public Boolean remove(final RedisConstant.RedisDBType dbType, final String key) {
        return redisUtils.remove(dbType, key);
    }

}
