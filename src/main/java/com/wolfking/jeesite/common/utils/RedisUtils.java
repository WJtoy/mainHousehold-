package com.wolfking.jeesite.common.utils;

import cn.hutool.core.util.StrUtil;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.wolfking.jeesite.common.config.redis.GsonRedisSerializer;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;
import com.kkl.kklplus.utils.StringUtils;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.MultiKeyCommands;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 项目名称：RedisUtils
 * <p>
 * 描述：Redis工具类
 * <p>
 * 创建人：Ryan Lu
 * <p>
 * 创建时间：
 * <p>
 * Copyright @ 2017
 */
@Component
@Configurable
@Slf4j
public class RedisUtils {

    private static Gson gson = new Gson();
    @Value("${spring.redis.database}")
    private int database;

    @SuppressWarnings("rawtypes")
    @Autowired
    public RedisTemplate redisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Resource(name = "gsonRedisSerializer")
    public GsonRedisSerializer gsonRedisSerializer;


    // ================================
    // 以下对普通key-value操作
    // ================================

    /**
     * 写入缓存
     *
     * @param key
     * @param value
     * @return
     */
    public Boolean set(final String key, Object value, long expireSeconds) {
        return set(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key, value, expireSeconds);
    }

    /**
     * 写入缓存
     *
     * @param key
     * @param value
     * @param dbType
     * @param expireSeconds 过期时间（单位:秒）
     * @return
     */
    public Boolean set(final RedisConstant.RedisDBType dbType, final String key, Object value, long expireSeconds) {
        if (value == null) return true;
        final byte[] bkey = key.getBytes(StandardCharsets.UTF_8);
        final byte[] bvalue = gsonRedisSerializer.serialize(value);
        return redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Long doInRedis(RedisConnection connection)
                    throws DataAccessException {
                connection.select(dbType.ordinal());
                try {
                    connection.set(bkey, bvalue);
                    if (expireSeconds > 0) {
                        connection.expire(key.getBytes("utf-8"), expireSeconds);
                    }
                    return 1L;
                } catch (UnsupportedEncodingException e) {
                    log.error("[RedisUtils.set]", e);
                    return -1L;
                }
            }
        }).equals(1L);
    }

    /**
     * 写入缓存,并设置过期时间（原子性(atomic)操作）
     * 将值 value 关联到 key ，并将 key 的生存时间设为 seconds (以秒为单位)。
     * 如果 key 已经存在， SETEX 命令将覆写旧值。
     *
     * @param key
     * @param value
     * @return
     */
    public Boolean setEX(final String key, Object value, long expireSeconds) {
        return setEX(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key, value, expireSeconds);
    }

    /**
     * 写入缓存,并设置过期时间（原子性(atomic)操作）
     * 将值 value 关联到 key ，并将 key 的生存时间设为 seconds (以秒为单位)。
     * 如果 key 已经存在， SETEX 命令将覆写旧值。
     *
     * @param key
     * @param value
     * @param dbType
     * @param expireSeconds 过期时间（单位:秒）<=0,默认设置为30天
     * @return
     */
    public Boolean setEX(final RedisConstant.RedisDBType dbType, final String key, Object value, long expireSeconds) {
        if (StringUtils.isBlank(key) || value == null) return true;
        final byte[] bkey = key.getBytes(StandardCharsets.UTF_8);
        final byte[] bvalue = gsonRedisSerializer.serialize(value);
        Long result = (Long) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Long doInRedis(RedisConnection connection)
                    throws DataAccessException {
                connection.select(dbType.ordinal());
                try {
                    connection.setEx(bkey, expireSeconds <= 0 ? 30 * 24 * 3600 : expireSeconds, bvalue);
                    return 1L;
                } catch (Exception e) {
                    log.error("[RedisUtils.setEX]", e);
                    return -1L;
                }
            }
        });
        return result.equals(1L);
    }

    /**
     * 写入缓存,是『SET if Not eXists』(如果不存在，则 SET)的简写。
     * key不存在，写入，返回true
     * key存在，不覆盖，返回false
     *
     * @param key
     * @param value
     * @return
     */
    public Boolean setNX(final String key, Object value, long expireSeconds) {
        return setNX(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key, value, expireSeconds);
    }

    /**
     * 写入缓存,是『SET if Not eXists』(如果不存在，则 SET)的简写。
     * key不存在，写入，返回true
     * key存在，不覆盖，返回false
     *
     * @param key
     * @param value
     * @param dbType
     * @param expireSeconds 过期时间（单位:秒）
     * @return
     */
    public Boolean setNX(final RedisConstant.RedisDBType dbType, final String key, Object value, long expireSeconds) {
        if (StringUtils.isBlank(key) || value == null) return false;
        final byte[] bkey = key.getBytes(StandardCharsets.UTF_8);
        final byte[] bvalue = gsonRedisSerializer.serialize(value);
        return (Boolean) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection)
                    throws DataAccessException {
                connection.select(dbType.ordinal());
                Boolean lock = false;
                try {
                    lock = connection.setNX(bkey, bvalue);

                    if (lock && expireSeconds > 0) {
                        connection.expire(bkey, expireSeconds);
                    }
                    return lock;
                } catch (Exception e) {
                    log.error("[RedisUtils.setNX]", e);
                    //setnx成功，但设置过期时间异常
                    if (lock && bkey != null) {
                        connection.del(bkey);
                    }
                    return false;
                }
            }
        });
    }

    /**
     * 批量锁
     *
     * @param keys          key列表
     * @param value         锁值
     * @param expireSeconds 过期时间（单位:秒）
     * @return 布尔型
     */
    public Boolean batchSetNX(final List<String> keys, Object value, long expireSeconds) {
        return batchSetNX(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, keys, value, expireSeconds);
    }

    /**
     * 批量锁
     *
     * @param dbType        db索引
     * @param keys          key列表
     * @param value         锁值
     * @param expireSeconds 过期时间（单位:秒）
     * @return 布尔型
     */
    public Boolean batchSetNX(final RedisConstant.RedisDBType dbType, final List<String> keys, Object value, long expireSeconds) {
        if (keys == null || keys.size() == 0 || value == null) {
            return false;
        }

        final byte[] bvalue = gsonRedisSerializer.serialize(value);
        return (Boolean) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection)
                    throws DataAccessException {
                connection.select(dbType.ordinal());
                Boolean lock = false;
                List<byte[]> lockedKeys = Lists.newArrayList();
                for (int i = 0, size = keys.size(); i < size; i++) {
                    try {
                        final byte[] bkey = keys.get(i).getBytes(StandardCharsets.UTF_8);
                        lock = connection.setNX(bkey, bvalue);
                        if (lock && expireSeconds > 0) {
                            connection.expire(bkey, expireSeconds);
                        }
                        if (lock) {
                            lockedKeys.add(bkey);
                        }
                    } catch (Exception e) {
                        log.error("[RedisUtils.batchSetNX]", e);
                        //删除成功的key
                        for (int j = 0, jsize = lockedKeys.size(); j < jsize; j++) {
                            connection.del(lockedKeys.get(j));
                        }
                        return false;
                    }
                }
                return true;
            }
        });
    }

    /**
     * 读取缓存
     *
     * @param key
     * @return
     * @Sample: Dict dict = (Dict)get("key",Dict.class)
     */
    public Object get(final String key, Class clazz) {
        return get(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key, clazz);
    }

    /**
     * 读取缓存
     *
     * @param key
     * @return
     * @Sample: Dict dict = (Dict)get("key",Dict.class)
     */
    public Object get(final RedisConstant.RedisDBType dbType, final String key, Class clazz) {
        if (StringUtils.isBlank(key)) return null;
        final byte[] bkey = key.getBytes(StandardCharsets.UTF_8);
        byte[] bytes = (byte[]) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection)
                    throws DataAccessException {
                //System.out.println(connection.toString());
                try {
                    connection.select(dbType.ordinal());
                    return connection.get(bkey);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
                return new byte[0];
            }
        });
        if (bytes == null || bytes.length == 0) return null;
        return gson.fromJson(new String(bytes), clazz);
    }

    /**
     * 读取缓存
     *
     * @param key
     * @return
     * @Sample: Dict dict = (Dict)get("key",Dict.class)
     */
    public Object getString(final RedisConstant.RedisDBType dbType, final String key, Class clazz) {
        if (StringUtils.isBlank(key)) return null;
        final byte[] bkey = key.getBytes(StandardCharsets.UTF_8);
        byte[] bytes = (byte[]) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection)
                    throws DataAccessException {
                try {
                    connection.select(dbType.ordinal());
                    return connection.get(bkey);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
                return new byte[0];
            }
        });
        if (bytes == null || bytes.length == 0) return null;
        return new String(bytes);
    }

    /**
     * 在Redis键中设置指定的字符串值，并返回其旧值
     *
     * @param key
     * @return
     * @Sample: Long time = (Long)getSet("key",10000)
     */
    public Object getSet(final String key, Object newValue) {
        return getSet(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key, newValue);
    }

    /**
     * 在Redis键中设置指定的字符串值，并返回其旧值
     *
     * @param key
     * @return
     * @Sample:
     */
    public Object getSet(final RedisConstant.RedisDBType dbType, final String key, Object newValue) {
        if (StringUtils.isBlank(key)) return null;
        final byte[] bkey = key.getBytes(StandardCharsets.UTF_8);
        final byte[] bvalue = gsonRedisSerializer.serialize(newValue);
        byte[] bytes = (byte[]) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection)
                    throws DataAccessException {
                try {
                    connection.select(dbType.ordinal());
                    return connection.getSet(bkey, bvalue);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
                return new byte[0];
            }
        });
        if (bytes == null || bytes.length == 0) return null;
        if (newValue instanceof String) {
            return new String(bytes);
        }
        return gson.fromJson(new String(bytes), newValue.getClass());
    }

    /**
     * 读取缓存,value存储对象为List<T>的json格式（使用google.gson序列化）
     *
     * @param key
     * @param type 反序列化后的数据类型
     * @param <T>
     * @return
     * @Sample List<Dict> rlist = redisUtils.getList(1,"dicts",Dict[].class);
     */
    public <T> List<T> getList(final String key, Class<T[]> type) {
        return getList(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key, type);
    }

    /**
     * 读取缓存,value存储对象为List<T>的json格式（使用google.gson序列化）
     *
     * @param dbType
     * @param key
     * @param type   反序列化后的数据类型
     * @param <T>
     * @return
     * @Sample List<Dict> rlist = redisUtils.getList(1,"dicts",Dict[].class);
     */
    public <T> List<T> getList(final RedisConstant.RedisDBType dbType, final String key, Class<T[]> type) {
        if (StringUtils.isBlank(key)) return Lists.newArrayList();
        final byte[] bkey = key.getBytes(StandardCharsets.UTF_8);
        byte[] bytes = (byte[]) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection)
                    throws DataAccessException {
                try {
                    connection.select(dbType.ordinal());
                    return connection.get(bkey);
                } catch (Exception e) {
                    log.error("[RedisUtils.getList] key:{} ,type:", key, type.getName(), e);
                }
                return Lists.newArrayList();
            }
        });
        if (bytes == null || bytes.length == 0) return null;
        T[] arr = gson.fromJson(StringUtils.toString(bytes), type);
        return new ArrayList<>(Arrays.asList(arr));
    }


    /**
     * 删除对应的value
     *
     * @param key
     */
    public Boolean remove(final String key) {
        return remove(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key);
    }

    /**
     * 删除对应的value
     *
     * @param key
     */
    public Boolean remove(final RedisConstant.RedisDBType dbType, final String key) {
        if (StringUtils.isBlank(key)) return false;
        final byte[] bkey = key.getBytes(StandardCharsets.UTF_8);
        return (Boolean) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection)
                    throws DataAccessException {

                long cnt = 0;
                try {
                    connection.select(dbType.ordinal());
                    cnt = connection.del(bkey);
                } catch (Exception e) {
                    log.error("[RedisUtils.remove]", e);
                }
                return cnt > 0;
            }
        });
    }

    /**
     * 批量删除对应的value
     *
     * @param keys
     */
    public Boolean remove(final String... keys) {
        return remove(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, keys);
    }

    /**
     * 批量删除对应的value
     *
     * @param keys
     */
    public Boolean remove(final RedisConstant.RedisDBType dbType, final String... keys) {
        if (keys == null || keys.length == 0) return false;
        int vsize = keys.length;
        final byte[][] bkeys = new byte[vsize][];
        for (int i = 0; i < vsize; i++) {
            bkeys[i] = keys[i].getBytes(StandardCharsets.UTF_8);
        }
        return (Boolean) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection)
                    throws DataAccessException {
                long cnt = 0;
                try {
                    connection.select(dbType.ordinal());
                    cnt = connection.del(bkeys);
                } catch (Exception e) {
                    log.error("[RedisUtils.remove]", e);
                }
                return cnt > 0;
            }
        });
    }

    /**
     * 批量删除key
     *
     * @param pattern
     */
    public Boolean removePattern(final String pattern) {
        return removePattern(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, pattern);
    }

    /**
     * 批量删除key
     *
     * @param dbType
     * @param pattern
     */
    public Boolean removePattern(final RedisConstant.RedisDBType dbType, final String pattern) {
        if (StringUtils.isBlank(pattern)) return false;
        return (Boolean) redisTemplate.execute((RedisCallback<Object>) connection -> {
            connection.select(dbType.ordinal());
            long cnt = 0;
            try {
                Set<byte[]> keys = new HashSet<>();
                Cursor<byte[]> cursor = connection.scan(new ScanOptions.ScanOptionsBuilder().match(pattern).count(300).build());
                while (cursor.hasNext()) {
                    keys.add(cursor.next());
                }
                if (keys.size() == 0) {
                    return true;
                }
                byte[][] bkeys = keys.toArray(new byte[keys.size()][]);
                cnt = connection.del(bkeys);

            } catch (Exception e) {
                log.error("[RedisUtils.remove]", e);
            }
            return cnt > 0;
        });
    }

    /**
     * 将key移到另外一个db中
     * @param dbType
     * @param key
     * @param toDbType
     * @return
     */
    public Boolean moveToDb(final RedisConstant.RedisDBType dbType,final String key,final RedisConstant.RedisDBType toDbType){
        if (StringUtils.isBlank(key)) return false;
        final byte[] bkey = key.getBytes(StandardCharsets.UTF_8);
        return (Boolean) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection)
                    throws DataAccessException {
                connection.select(dbType.ordinal());
                long cnt = 0;
                try {
                    return connection.move(bkey,toDbType.ordinal());
                } catch (Exception e) {
                    log.error("[RedisUtils.moveToDb]", e);
                    return false;
                }
            }
        });
    }

    /**
     * 返回缓存所有符合条件的key集合
     * @param dbType
     * @param pattern
     * @return

    public Set<byte[]> scan(final RedisConstant.RedisDBType dbType, final String pattern){
        if (StrUtil.isBlank(pattern)) return Sets.newHashSet();
        return (Set<byte[]>) redisTemplate.execute((RedisCallback<Set<byte[]>>) connection -> {
            try {
                connection.select(dbType.ordinal());
                Set<byte[]> keys = new HashSet<>();
                Cursor<byte[]> cursor = connection.scan(new ScanOptions.ScanOptionsBuilder().match(pattern).count(300).build());
                while (cursor.hasNext()) {
                    keys.add(cursor.next());
                }
                return keys;
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return Sets.newHashSet();
            }
        });
    }*/

    /**
     * 返回缓存所有符合条件的key集合
     * @param dbType
     * @param pattern
     * @param count
     * @return
     */
    public Set<byte[]> scan(final RedisConstant.RedisDBType dbType, final String pattern,final long count){
        if (StrUtil.isBlank(pattern)) return Sets.newHashSet();
        return (Set<byte[]>) redisTemplate.execute((RedisCallback<Set<byte[]>>) connection -> {
            try {
                connection.select(dbType.ordinal());
                Set<byte[]> keys = new HashSet<>();
                Cursor<byte[]> cursor = connection.scan(new ScanOptions.ScanOptionsBuilder().match(pattern).count(count).build());
                while (cursor.hasNext()) {
                    keys.add(cursor.next());
                }
                return keys;
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return Sets.newHashSet();
            }
        });
    }

    /**
     * scan 实现
     * 分多次调用scan
     * @param dbType    db index
     * @param pattern   表达式
     * @param count     每次扫描数量
     * @param consumer  对迭代到的key进行操作
     */
    public void scan(final RedisConstant.RedisDBType dbType,final String pattern,final Integer count, Consumer<String> consumer) {
        this.stringRedisTemplate.execute((RedisCallback<Boolean>) connection -> {
            connection.select(dbType.ordinal());
            Set<String> keys = Sets.newHashSet();
            JedisCommands commands = (JedisCommands) connection.getNativeConnection();
            MultiKeyCommands multiKeyCommands = (MultiKeyCommands) commands;
            ScanParams scanParams = new ScanParams();
            scanParams.match(pattern);
            scanParams.count(count);
            ScanResult<String> scan = multiKeyCommands.scan("0", scanParams);
            while (null != scan.getStringCursor()) {
                keys.addAll(scan.getResult());
                if (!StringUtils.equals("0", scan.getStringCursor())) {
                    scan = multiKeyCommands.scan(scan.getStringCursor(), scanParams);
                    continue;
                } else {
                    break;
                }
            }
            if(keys.size()>0){
                keys.forEach(consumer);
            }
            return true;
        });
    }

    public List<String> scanList(final RedisConstant.RedisDBType dbType,final String pattern,final Integer count){
        List<String> keys = new ArrayList<>();
        this.scan(dbType,pattern,10000, item -> {
            //符合条件的key
            //String key = new String(item,StandardCharsets.UTF_8);
            keys.add(item);
        });
        return keys;
    }

    /**
     * 获取过期时间(秒)
     *
     * @param key
     * @return
     */
    public long ttl(final String key) {
        return ttl(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key);
    }

    /**
     * 获取过期时间(秒)
     *
     * @param key
     * @return
     */
    public long ttl(final RedisConstant.RedisDBType dbType, final String key) {
        if (StringUtils.isBlank(key)) return 0l;
        final byte[] bkey = key.getBytes(StandardCharsets.UTF_8);
        return (long) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection)
                    throws DataAccessException {
                connection.select(dbType.ordinal());
                return connection.ttl(bkey);
            }
        });
    }

    /**
     * 获取过期时间(毫秒)
     *
     * @param key
     * @return
     */
    public long pTtl(final String key) {
        return pTtl(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key);
    }

    /**
     * 获取过期时间(毫秒)
     *
     * @param key
     * @return
     */
    public long pTtl(final RedisConstant.RedisDBType dbType, final String key) {
        if (StringUtils.isBlank(key)) return 0l;
        final byte[] bkey = key.getBytes(StandardCharsets.UTF_8);
        return (long) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection)
                    throws DataAccessException {
                connection.select(dbType.ordinal());
                return connection.pTtl(bkey);
            }
        });
    }

    /**
     * 设置key过期时间
     *
     * @param key
     * @param seconds 单位:秒
     * @return
     */
    public Boolean expire(final String key, final long seconds) {
        return expire(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key, seconds);
    }

    /**
     * 设置key过期时间
     *
     * @param key
     * @param seconds 单位:秒
     * @return
     */
    public Boolean expire(final RedisConstant.RedisDBType dbType, final String key, final long seconds) {
        if (StringUtils.isBlank(key) || seconds <= 0) return false;
        final byte[] bkey = key.getBytes(StandardCharsets.UTF_8);
        return (Boolean) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection)
                    throws DataAccessException {
                connection.select(dbType.ordinal());
                return connection.expire(bkey, seconds);
            }
        });
    }

    /**
     * 设置key在某时间过期
     *
     * @param key
     * @param timeStamp 所在时间(TIME_IN_UNIX_TIMESTAMP)
     * @return
     */
    public Boolean expireAt(final String key, final long timeStamp) {
        return expireAt(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key, timeStamp);
    }

    /**
     * 设置key在某时间过期
     *
     * @param dbType    database
     * @param key
     * @param timeStamp 所在时间(TIME_IN_UNIX_TIMESTAMP)
     * @return
     */
    public Boolean expireAt(final RedisConstant.RedisDBType dbType, final String key, final long timeStamp) {
        if (StringUtils.isBlank(key) || timeStamp <= 0) return false;
        final byte[] bkey = key.getBytes(StandardCharsets.UTF_8);
        return (Boolean) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection)
                    throws DataAccessException {
                connection.select(dbType.ordinal());
                return connection.expireAt(bkey, timeStamp);
            }
        });
    }

    /**
     * 判断缓存中是否有对应的key
     *
     * @param key
     * @return
     */
    public Boolean exists(final String key) {
        return exists(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key);
    }

    /**
     * 判断缓存中是否有对应的key
     *
     * @param dbType
     * @param key
     * @return
     */
    public Boolean exists(final RedisConstant.RedisDBType dbType, final String key) {
        if (StringUtils.isBlank(key)) return false;
        final byte[] bkey = key.getBytes(StandardCharsets.UTF_8);
        return (Boolean) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection)
                    throws DataAccessException {
                connection.select(dbType.ordinal());
                return connection.exists(bkey);
            }
        });
    }

    /**
     * 插入时取得某张表的自增id值
     *
     * @param key 示例:db.{tableName}.id
     * @return 大于0时表示正常 -1 表示key必须填写
     */
    public long incr(final String key) {
        return incr(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key);
    }

    /**
     * 插入时取得某张表的自增id值
     *
     * @param dbType 数据库下标
     * @param key    示例:db.{tableName}.id
     * @return 大于0时表示正常 -1 表示key必须填写
     */
    @SuppressWarnings("unchecked")
    public long incr(final RedisConstant.RedisDBType dbType, final String key) {
        if (StringUtils.isBlank(key)) {
            return -1l;
        }
        final byte[] bkey = key.getBytes(StandardCharsets.UTF_8);
        return (Long) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Long doInRedis(RedisConnection connection)
                    throws DataAccessException {
                connection.select(dbType.ordinal());
                long id = 0;
                try {
                    id = connection.incr(bkey);
                } catch (Exception e) {
                    log.error("[RedisUtils.incr]", e);
                }
                return id;
            }
        });
    }


    // ---------- //
    //  哈希
    // ---------- //

    /**
     * 判断哈希中是否有指定的字段
     *
     * @param key
     * @param field
     * @return
     */
    @SuppressWarnings("unchecked")
    public Boolean hexist(final String key, final String field) {
        return hexist(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key, field);
    }

    /**
     * 判断哈希中是否有指定的字段
     *
     * @param key
     * @param field
     * @return
     */
    @SuppressWarnings("unchecked")
    public Boolean hexist(final RedisConstant.RedisDBType dbType, final String key, final String field) {
        if (StringUtils.isBlank(key)) return false;
        final byte[] bkey = key.getBytes(StandardCharsets.UTF_8);
        final byte[] bfield = field.getBytes(StandardCharsets.UTF_8);
        return (Boolean) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection)
                    throws DataAccessException {
                try {
                    connection.select(dbType.ordinal());
                    return connection.hExists(bkey, bfield);
                } catch (Exception e) {
                    log.error("[RedisUtils.hexist]", e);
                    return false;
                }
            }
        });
    }

    /**
     * 哈希 添加
     *
     * @param key:哈希主Key
     * @param field:属性key
     * @param value:属性值
     * @param expireSeconds
     */
    public Boolean hmSet(final String key, String field, Object value, long expireSeconds) {
        return hmSet(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key, field, value, expireSeconds);
    }

    /**
     * 哈希 添加
     *
     * @param dbType
     * @param key:哈希主Key
     * @param field:属性key
     * @param value:属性值
     * @param expireSeconds
     */
    public Boolean hmSet(final RedisConstant.RedisDBType dbType, final String key, String field, Object value, long expireSeconds) {
        if (StringUtils.isBlank(key) || StringUtils.isBlank(field) || value == null) return false;
        final byte[] bkey = key.getBytes(StandardCharsets.UTF_8);
        final byte[] bfield = field.getBytes(StandardCharsets.UTF_8);
        final byte[] bvalue = gsonRedisSerializer.serialize(value);
        return (Boolean) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Boolean doInRedis(RedisConnection connection)
                    throws DataAccessException {
                try {
                    connection.select(dbType.ordinal());
                    Boolean result = connection.hSet(bkey, bfield, bvalue);
                    if (expireSeconds > 0) {
                        connection.expire(bkey, expireSeconds);
                    }
                    return result;
                } catch (Exception e) {
                    log.error("[RedisUtils.hmSet]", e);
                }
                return false;
            }
        });
    }

    /**
     * 哈希 添加多个
     *
     * @param key
     * @param map           hashMap
     * @param expireSeconds
     */
    public Boolean hmSetAll(String key, Map<String, Object> map, long expireSeconds) {
        return hmSetAll(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key, map, expireSeconds);
    }

    /**
     * 哈希 添加多个
     *
     * @param key
     * @param map hashMap
     */
    public Boolean hmSetAll(final RedisConstant.RedisDBType dbType, String key, Map<String, Object> map, long expireSeconds) {
        if (StringUtils.isBlank(key) || map == null || map.size() == 0) return false;
        final byte[] bkey = key.getBytes(StandardCharsets.UTF_8);
        final Map<byte[], byte[]> bmap = new HashMap<>();
        map.forEach(
                (k, v) -> {
                    byte[] field = k.getBytes(StandardCharsets.UTF_8);
                    byte[] value = gsonRedisSerializer.serialize(v);
                    bmap.put(field, value);
                }
        );
        return (Boolean) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Boolean doInRedis(RedisConnection connection)
                    throws DataAccessException {
                try {
                    connection.select(dbType.ordinal());
                    connection.hMSet(bkey, bmap);
                    if (expireSeconds > 0) {
                        connection.expire(bkey, expireSeconds);
                    }
                    return true;
                } catch (Exception e) {
                    log.error("[RedisUtils.hmSetAll]", e);
                }
                return false;
            }
        });
    }

    /**
     * 哈希获取数据
     *
     * @param key
     * @param field
     * @return
     */
    public <T> T hGet(String key, String field, Class<T> clazz) {
        //return redisTemplate.opsForHash().get(key,hashKey);
        return hGet(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key, field, clazz);
    }

    /**
     * 获得哈希指定field的值
     *
     * @param key
     * @param field
     * @return
     */
    public <T> T hGet(RedisConstant.RedisDBType dbType, String key, String field, Class<T> clazz) {
        if (StringUtils.isBlank(key) || StringUtils.isBlank(field)) return null;
        final byte[] bkey = key.getBytes(StandardCharsets.UTF_8);
        final byte[] bfield = field.getBytes(StandardCharsets.UTF_8);

        Object result = redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection)
                    throws DataAccessException {
                try {
                    connection.select(dbType.ordinal());
                    return connection.hGet(bkey, bfield);
                } catch (Exception e) {
                    log.error("[RedisUtils.hGet]", e);
                }
                return null;
            }
        });
        if (result == null) return null;
        byte[] bytes = (byte[]) result;
        if (bytes.length == 0) return null;
        return (T) gsonRedisSerializer.deserialize(bytes, clazz);
    }

    /**
     * 获取hashset 的值为List<T>
     *
     * @param dbType
     * @param key
     * @param field
     * @param type
     * @param <T>
     * @return
     */
    public <T> List<T> hGetList(final RedisConstant.RedisDBType dbType, final String key, String field, Class<T[]> type) {
        if (StringUtils.isBlank(key)) return Lists.newArrayList();
        final byte[] bkey = key.getBytes(StandardCharsets.UTF_8);
        final byte[] bfield = field.getBytes(StandardCharsets.UTF_8);
        byte[] bytes = (byte[]) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection)
                    throws DataAccessException {
                try {
                    connection.select(dbType.ordinal());
                    return connection.hGet(bkey, bfield);
                } catch (Exception e) {
                    log.error("[RedisUtils.hGetList]", e);
                }
                return Lists.newArrayList();
            }
        });
        if (bytes == null || bytes.length == 0) return null;
        T[] arr = gson.fromJson(StringUtils.toString(bytes), type);
        return new ArrayList<>(Arrays.asList(arr));
    }

    /**
     * 获得哈希指定一些的所有值
     *
     * @param key
     * @param fields
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<byte[]> hGet(final String key, final String[] fields) {
        return hGet(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key, fields);
    }

    /**
     * 获得哈希指定一些的所有值
     *
     * @param key
     * @param fields
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<byte[]> hGet(final RedisConstant.RedisDBType dbType, final String key, final String[] fields) {
        if (StringUtils.isBlank(key) || fields == null || fields.length == 0) return Lists.newArrayList();
        final byte[] bkey = key.getBytes(StandardCharsets.UTF_8);
        int vsize = fields.length;
        final byte[][] bfields = new byte[vsize][];
        for (int i = 0; i < vsize; i++) {
            bfields[i] = fields[i].getBytes(StandardCharsets.UTF_8);
        }
        return (List<byte[]>) redisTemplate
                .execute(new RedisCallback<Object>() {
                    @Override
                    public Object doInRedis(RedisConnection connection)
                            throws DataAccessException {
                        try {
                            connection.select(dbType.ordinal());
                            return connection.hMGet(bkey, bfields);
                        } catch (Exception e) {
                            log.error("[RedisUtils.hGet]", e);
                        }
                        return Lists.newArrayList();
                    }
                });
    }

    public Map<String, byte[]> hGetAll(final String key) {
        return hGetAll(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key);
    }

    @SuppressWarnings("unchecked")
    public Map<String, byte[]> hGetAll(final RedisConstant.RedisDBType dbType, final String key) {
        if (StringUtils.isBlank(key)) return null;
        final byte[] bkey = key.getBytes(StandardCharsets.UTF_8);
        Map<byte[], byte[]> maps = (Map<byte[], byte[]>) redisTemplate
                .execute(new RedisCallback<Object>() {
                    @Override
                    public Map<byte[], byte[]> doInRedis(
                            RedisConnection connection)
                            throws DataAccessException {
                        try {
                            connection.select(dbType.ordinal());
                            return connection.hGetAll(bkey);
                        } catch (Exception e) {
                            log.error("[RedisUtils.hGetAll]", e);
                        }
                        return null;
                    }
                });
        if (maps == null || maps.size() == 0) return new HashMap<>();
        //将key转成String
        return maps.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> StringUtils.toString(e.getKey()),
                        e -> e.getValue()
                ));
    }

    @SuppressWarnings("unchecked")
    public Map<byte[], byte[]> hGetAllByte(final RedisConstant.RedisDBType dbType, final String key) {
        if (StringUtils.isBlank(key)) return null;
        final byte[] bkey = key.getBytes(StandardCharsets.UTF_8);
        Map<byte[], byte[]> maps = (Map<byte[], byte[]>) redisTemplate
                .execute(new RedisCallback<Object>() {
                    @Override
                    public Map<byte[], byte[]> doInRedis(
                            RedisConnection connection)
                            throws DataAccessException {
                        try {
                            connection.select(dbType.ordinal());
                            return connection.hGetAll(bkey);
                        } catch (Exception e) {
                            log.error("[RedisUtils.hGetAll]", e);
                        }
                        return null;
                    }
                });
        if (maps == null || maps.size() == 0) return new HashMap<>();
        return maps;
    }

    public <T> List<T> hGetAllObjList(final RedisConstant.RedisDBType dbType,String key, Class<T> clazz) {
        List<T> result = Lists.newArrayList();
        Map<String, T> map = this.hGetAllObj(dbType,key, clazz);
        if (!map.isEmpty()) {
            result = (List)map.entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList());
        }

        return (List)result;
    }

    public <T> Map<String, T> hGetAllObj(final RedisConstant.RedisDBType dbType,String key, Class<T> clazz) {
        Map<String, T> result = Maps.newHashMap();
        Map<byte[], byte[]> map = hGetAllByte(dbType,key);
        if (!map.isEmpty()) {
            result = (Map)map.entrySet().stream().collect(Collectors.toMap((i) -> {
                return StringUtils.toString((byte[])i.getKey());
            }, (i) -> {
                return gson.fromJson(StringUtils.toString(i.getValue()), clazz);
            }));
        }

        return (Map)result;
    }

    /**
     * 哈希 字段自增
     * 如field不存在，自动添加,且值为0+delta
     *
     * @param dbType db索引
     * @param key    键值
     * @param field  字段
     * @param delta  步长
     * @return
     */
    @SuppressWarnings("unchecked")
    public long hIncrBy(final RedisConstant.RedisDBType dbType, final String key, final String field, final long delta) {
        if (StringUtils.isBlank(key) || StringUtils.isBlank(field)) return 0;
        final byte[] bkey = key.getBytes(StandardCharsets.UTF_8);
        final byte[] bfield = field.getBytes(StandardCharsets.UTF_8);
        return (Long) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Long doInRedis(RedisConnection connection)
                    throws DataAccessException {
                long id = 0;
                try {
                    connection.select(dbType.ordinal());
                    id = connection.hIncrBy(bkey, bfield, delta);
                } catch (Exception e) {
                    log.error("[RedisUtils.hIncrBy]", e);
                }
                return id;
            }
        });
    }

    /**
     * 哈希 字段自增
     * 如field不存在，自动添加,且值为0+delta
     *
     * @param dbType db索引
     * @param key    键值
     * @param fields 字段
     * @param delta  步长
     * @return
     */
    @SuppressWarnings("unchecked")
    public long hIncrByFields(final RedisConstant.RedisDBType dbType, final String key, final String[] fields, final long delta) {
        if (StringUtils.isBlank(key) || fields == null || fields.length == 0) return 0;
        final byte[] bkey = key.getBytes(StandardCharsets.UTF_8);
        final int vsize = fields.length;
        final byte[][] bfields = new byte[vsize][];
        for (int i = 0; i < vsize; i++) {
            bfields[i] = fields[i].getBytes(StandardCharsets.UTF_8);
        }
        ;
        return (Long) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Long doInRedis(RedisConnection connection)
                    throws DataAccessException {
                long id = 0;
                try {
                    connection.select(dbType.ordinal());
                    for (int i = 0, size = bfields.length; i < size; i++) {
                        id = connection.hIncrBy(bkey, bfields[i], delta);
                    }
                } catch (Exception e) {
                    log.error("[RedisUtils.hIncrByFields]", e);
                }
                return id;
            }
        });
    }

    /**
     * 哈希 字段自增+1
     * 如field不存在，自动添加,且值为0+1
     *
     * @param key
     * @param field
     * @return
     */
    public long hIncr(RedisConstant.RedisDBType dbType, final String key, final String field) {
        return hIncrBy(dbType, key, field, 1l);
    }

    /**
     * 哈希删除某个field
     *
     * @param key
     * @param field
     */
    public Long hdel(final String key, final String field) {
        return hdel(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key, field);
    }

    /**
     * 哈希删除某个field
     *
     * @param key
     * @param field
     */
    public Long hdel(final RedisConstant.RedisDBType dbType, final String key, final String field) {
        if (StringUtils.isBlank(key) || StringUtils.isBlank(field)) return 0l;
        final byte[] bkey = key.getBytes(StandardCharsets.UTF_8);
        final byte[] bfield = field.getBytes(StandardCharsets.UTF_8);
        return (Long) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection)
                    throws DataAccessException {
                try {
                    connection.select(dbType.ordinal());
                    return connection.hDel(bkey, bfield);
                } catch (Exception e) {
                    log.error("[RedisUtils.hdel]", e);
                }
                return 0l;
            }
        });
    }

    /**
     * 哈希删除多个field
     *
     * @param key
     * @param fields
     */
    public Long hdel(final String key, final String[] fields) {
        return hdel(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key, fields);
    }

    /**
     * 哈希删除多个field
     *
     * @param key
     * @param fields
     */
    public Long hdel(final RedisConstant.RedisDBType dbType, final String key, final String[] fields) {
        if (StringUtils.isBlank(key) || fields == null || fields.length == 0) return 0l;
        final byte[] bkey = key.getBytes(StandardCharsets.UTF_8);
        int vsize = fields.length;
        final byte[][] bfields = new byte[vsize][];
        for (int i = 0; i < vsize; i++) {
            bfields[i] = fields[i].getBytes(StandardCharsets.UTF_8);
        }
        return (Long) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection)
                    throws DataAccessException {
                try {
                    connection.select(dbType.ordinal());
                    return connection.hDel(bkey, bfields);
                } catch (Exception e) {
                    log.error("[RedisUtils.hdel]", e);
                }
                return 0l;
            }
        });
    }

    // ---------- //
    //  列表
    // ---------- //

    /**
     * 将值value插入到列表key的表头
     *
     * @param key   键
     * @param value 值
     */
    public Long lPush(String key, Object value) {
        return lPush(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key, value);
    }

    /**
     * 将值value插入到列表key的表头
     * 返回值大于0成功
     *
     * @param key
     * @return
     */
    @SuppressWarnings("unchecked")
    public Long lPush(final RedisConstant.RedisDBType dbType, final String key, final Object value) {
        if (StringUtils.isBlank(key) || value == null) return 0l;
        final byte[] bkey = key.getBytes(StandardCharsets.UTF_8);
        final byte[] bvalue = gsonRedisSerializer.serialize(value);
        return (Long) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Long doInRedis(RedisConnection connection)
                    throws DataAccessException {
                long cnt = 0;
                try {
                    connection.select(dbType.ordinal());
                    return connection.lPush(bkey, bvalue);
                } catch (Exception e) {
                    log.error("[RedisUtils.lPush]", e);
                }
                return cnt;
            }
        });
    }


    /**
     * 列表添加多个对象
     *
     * @param key
     * @param values
     * @return 0:发生错误 >0:成功
     */
    public Long lPushAll(String key, List<?> values) {
        return lPushAll(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key, values);
    }

    /**
     * 列表添加多个对象
     *
     * @param dbType
     * @param key
     * @param values
     */
    public Long lPushAll(final RedisConstant.RedisDBType dbType, String key, List<?> values) {
        if (StringUtils.isBlank(key) || values == null || values.size() == 0) return 0l;
        final byte[] bkey = key.getBytes(StandardCharsets.UTF_8);
        final byte[][] bvalues = Lists.transform(values, serialObjectToByte).stream().toArray(size -> new byte[size][]);
        return (Long) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Long doInRedis(RedisConnection connection)
                    throws DataAccessException {
                try {
                    connection.select(dbType.ordinal());
                    return connection.lPush(bkey, bvalues);
                } catch (Exception e) {
                    log.error("[RedisUtils.lPushAll]", e);
                }
                return 0l;
            }
        });
    }

    /**
     * 将值value插入到列表key的表尾
     *
     * @param key   键
     * @param value 值
     */
    public Long rPush(String key, Object value) {
        return rPush(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key, value);
    }

    /**
     * 将值value插入到列表key的表尾
     * 返回值大于0成功
     *
     * @param key
     * @return
     */
    @SuppressWarnings("unchecked")
    public Long rPush(final RedisConstant.RedisDBType dbType, final String key, final Object value) {
        if (StringUtils.isBlank(key) || value == null) return 0l;
        final byte[] bkey = key.getBytes(StandardCharsets.UTF_8);
        final byte[] bvalue = gsonRedisSerializer.serialize(value);
        return (Long) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Long doInRedis(RedisConnection connection)
                    throws DataAccessException {
                long cnt = 0;
                try {
                    connection.select(dbType.ordinal());
                    return connection.rPush(bkey, bvalue);
                } catch (Exception e) {
                    log.error("[RedisUtils.rPush]", e);
                }
                return cnt;
            }
        });
    }


    /**
     * 列表添加多个对象
     *
     * @param key
     * @param values
     * @return 0:发生错误 >0:成功
     */
    public Long rPushAll(String key, List<?> values, long expireSeconds) {
        return rPushAll(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key, values, expireSeconds);
    }

    /**
     * 列表添加多个对象
     *
     * @param dbType
     * @param key
     * @param values
     */
    public Long rPushAll(final RedisConstant.RedisDBType dbType, String key, List<?> values, long expireSeconds) {
        if (StringUtils.isBlank(key) || values == null || values.size() == 0) return 0l;
        final byte[] bkey = key.getBytes(StandardCharsets.UTF_8);
        final byte[][] bvalues = Lists.transform(values, serialObjectToByte).stream().toArray(size -> new byte[size][]);
        return (Long) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Long doInRedis(RedisConnection connection)
                    throws DataAccessException {
                long count = 0;
                try {
                    connection.select(dbType.ordinal());
                    count = connection.rPush(bkey, bvalues);
                    if (expireSeconds > 0) {
                        connection.expire(bkey, expireSeconds);
                    }
                    return count;
                } catch (Exception e) {
                    log.error("[RedisUtils.rPushAll]", e);
                }
                return 0l;
            }
        });
    }

    /**
     * 列表获取(列表中存放的值类型要一致)
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    public <T> List<T> lRange(final String key, final int start, final int end, Class<T> type) {
        return lRange(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key, start, end, type);
    }

    /**
     * 向SET中添加一个成员，为一个Key添加一个值。如果这个值已经在这个Key中，则返回FALSE
     *
     * @param key
     * @param start
     * @param end
     * @param type  返回对象类
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> lRange(final RedisConstant.RedisDBType dbType, final String key, final int start, final int end, Class<T> type) {
        if (StringUtils.isBlank(key)) return Lists.newArrayList();
        final byte[] bkey = key.getBytes(StandardCharsets.UTF_8);
        List<byte[]> list = (List<byte[]>) redisTemplate
                .execute(new RedisCallback<Object>() {
                    @Override
                    public List<byte[]> doInRedis(RedisConnection connection)
                            throws DataAccessException {
                        try {
                            connection.select(dbType.ordinal());
                            return connection.lRange(bkey, start, end);
                        } catch (Exception e) {
                            log.error("[RedisUtils.lRange]", e);
                        }
                        return null;
                    }
                });
        if (list != null && list.size() > 0) {
            return Lists.transform(list, new Function<byte[], T>() {
                @Override
                public T apply(byte[] bytes) {
                    return (T) gsonRedisSerializer.deserialize(bytes, type);
                }
            });
        } else {
            return Lists.newArrayList();
        }
    }

    /**
     * 保留列表指定范围内的元素
     *
     * @param key
     * @param begin
     * @param end
     * @return
     */
    @SuppressWarnings("unchecked")
    public Boolean lTrim(final String key, final int begin, final int end) {
        return lTrim(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key, begin, end);
    }

    /**
     * 保留列表指定范围内的元素
     *
     * @param dbType
     * @param key
     * @param begin
     * @param end
     * @return
     */
    @SuppressWarnings("unchecked")
    public Boolean lTrim(final RedisConstant.RedisDBType dbType, final String key, final int begin, final int end) {
        if (StringUtils.isBlank(key)) return false;
        final byte[] bkey = key.getBytes(StandardCharsets.UTF_8);
        return (Boolean) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Boolean doInRedis(RedisConnection connection)
                    throws DataAccessException {
                try {
                    connection.select(dbType.ordinal());
                    connection.lTrim(bkey, begin, end);
                    return true;
                } catch (Exception e) {
                    log.error("[RedisUtils.lTrim]", e);
                }
                return false;
            }
        });
    }

    /**
     * 列表左侧删除一个元素
     *
     * @param key
     * @return
     */
    @SuppressWarnings("unchecked")
    public Object lPop(final String key, Class type) {
        return lPop(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key, type);
    }

    /**
     * 列表左侧删除一个元素
     *
     * @param dbType
     * @param key
     * @return Dict d = (Dict)redisUtils.lPop(15,"test:list:theme",Dict.class);
     */
    @SuppressWarnings("unchecked")
    public Object lPop(final RedisConstant.RedisDBType dbType, final String key, Class type) {
        if (StringUtils.isBlank(key)) return null;
        final byte[] bkey = key.getBytes(StandardCharsets.UTF_8);
        byte[] bytes = (byte[]) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection)
                    throws DataAccessException {
                try {
                    connection.select(dbType.ordinal());
                    return connection.lPop(bkey);
                } catch (Exception e) {
                    log.error("[RedisUtils.lPop]", e);
                }
                return null;
            }
        });
        if (bytes == null || bytes.length == 0) return null;
        try {
            return gsonRedisSerializer.deserialize(bytes, type);
        } catch (Exception e) {
            log.error("[RedisUtils.lPop]", e);
        }
        return null;
    }

    /**
     * 通过索引号获得list对应的值
     *
     * @param key
     * @param index
     * @return
     */
    @SuppressWarnings("unchecked")
    public Object lIndex(final String key, final long index, Class clazz) {
        return lIndex(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key, index, clazz);
    }

    /**
     * 通过索引号获得list对应的值
     *
     * @param key
     * @param index
     * @return
     */
    @SuppressWarnings("unchecked")
    public Object lIndex(final RedisConstant.RedisDBType dbType, final String key, final long index, Class clazz) {
        if (StringUtils.isBlank(key) || index < 0) return null;
        final byte[] bkey = key.getBytes(StandardCharsets.UTF_8);
        byte[] bytes = (byte[]) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection)
                    throws DataAccessException {
                byte[] result = null;
                try {
                    connection.select(dbType.ordinal());
                    return connection.lIndex(bkey, index);
                } catch (Exception e) {
                    log.error("[RedisUtils.lIndex]", e);
                }
                return null;
            }
        });
        if (bytes == null || bytes.length == 0) return null;
        try {
            return gsonRedisSerializer.deserialize(bytes, clazz);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 列表中值数量
     *
     * @param key
     * @return
     */
    public long lLen(final String key) {
        return lLen(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key);
    }

    /**
     * 列表中值数量
     *
     * @param key
     * @return
     */
    public long lLen(final RedisConstant.RedisDBType dbType, final String key) {
        if (StringUtils.isBlank(key)) return 0l;
        final byte[] bkey = key.getBytes(StandardCharsets.UTF_8);
        return (Long) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Long doInRedis(RedisConnection connection)
                    throws DataAccessException {
                try {
                    connection.select(dbType.ordinal());
                    return connection.lLen(bkey);
                } catch (Exception e) {
                    log.error("[RedisUtils.lLen]", e);
                }
                return 0l;
            }
        });
    }

    // ---------- //
    //  集合/Set
    // ---------- //

    /**
     * 向SET中添加一个成员，为一个Key添加一个值。如果这个值已经在这个Key中，则返回FALSE
     *
     * @param key
     * @param value
     */
    public long sAdd(String key, Object value, final long expireSeconds) {
        return sAdd(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key, value, expireSeconds);
    }

    /**
     * 向SET中添加一个成员，为一个Key添加一个值。如果这个值已经在这个Key中，则返回FALSE
     *
     * @param key
     * @param value
     */
    public long sAdd(final RedisConstant.RedisDBType dbType, String key, Object value, final long expireSeconds) {
        if (StringUtils.isBlank(key) || value == null) return 0l;
        final byte[] bkey = key.getBytes(StandardCharsets.UTF_8);
        final byte[] bvalue = gsonRedisSerializer.serialize(value);
        return (Long) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection)
                    throws DataAccessException {
                long cnt = 0;
                try {
                    connection.select(dbType.ordinal());
                    cnt = connection.sAdd(bkey, bvalue);
                    if (expireSeconds > 0) {
                        connection.expire(bkey, expireSeconds);
                    }
                } catch (Exception e) {
                    log.error("[RedisUtils.sAdd]", e);
                }
                return cnt;
            }
        });
    }

    /**
     * 集合添加多个元素
     *
     * @param key
     * @param values
     */
    public long sAdd(String key, List<Object> values, final long expireSeconds) {
        return sAdd(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key, values, expireSeconds);
    }

    /**
     * 集合添加多个元素
     *
     * @param key
     * @param values 传入参数必须为List<Object>,不能是List<String>,
     *               否则会当初Object，调用上面的sAdd方法
     */
    public long sAdd(final RedisConstant.RedisDBType dbType, String key, List<Object> values, final long expireSeconds) {
        if (StringUtils.isBlank(key) || values == null || values.size() == 0) return 0l;
        final byte[] bkey = key.getBytes(StandardCharsets.UTF_8);
        final byte[][] bvalues = Lists.transform(values, serialObjectToByte).stream().toArray(size -> new byte[size][]);
        return (Long) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection)
                    throws DataAccessException {
                long cnt = 0;
                try {
                    connection.select(dbType.ordinal());
                    cnt = connection.sAdd(bkey, bvalues);
                    if (expireSeconds > 0) {
                        connection.expire(bkey, expireSeconds);
                    }
                } catch (Exception e) {
                    log.error("[RedisUtils.sAdd]", e);
                }
                return cnt;
            }
        });
    }

    /**
     * 移除并返回集合中的一个随机元素,当集合不存在或是空集时，返回 nil
     *
     * @param key
     * @param clazz 返回成员类型
     */
    public Object sPop(String key, Class clazz) {
        return sPop(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key, clazz);
    }

    /**
     * 移除并返回集合中的一个随机元素,当集合不存在或是空集时，返回 nil
     *
     * @param key
     * @param clazz 返回成员类型
     */
    public Object sPop(final RedisConstant.RedisDBType dbType, String key, Class clazz) {
        final byte[] bkey = key.getBytes(StandardCharsets.UTF_8);
        final byte[] bvalue = key.getBytes(StandardCharsets.UTF_8);
        byte[] bytes = (byte[]) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection)
                    throws DataAccessException {
                long cnt = 0;
                try {
                    connection.select(dbType.ordinal());
                    return connection.sPop(bkey);
                } catch (Exception e) {
                    log.error("[RedisUtils.sPop]", e);
                }
                return null;
            }
        });
        if (bytes == null || bytes.length == 0) return null;
        return gson.fromJson(new String(bytes), clazz);
    }


    /**
     * 将成员从源集合移出放入目标集合
     * 如果源集合不存在或不包哈指定成员，不进行任何操作，返回0
     * 否则该成员从源集合上删除，并添加到目标集合，如果目标集合中成员已存在，则只在源集合进行删除
     *
     * @param srckey 源集合
     * @param dstkey 目标集合
     * @param member 源集合中的成员
     * @return 状态码，1成功，0失败
     */
    public Boolean sMove(final String srckey, final String dstkey, Object member) {
        return sMove(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, srckey, dstkey, member);
    }

    /**
     * 将成员从源集合移出放入目标集合
     * 如果源集合不存在或不包哈指定成员，不进行任何操作，返回0
     * 否则该成员从源集合上删除，并添加到目标集合，如果目标集合中成员已存在，则只在源集合进行删除
     *
     * @param srckey 源集合
     * @param dstkey 目标集合
     * @param member 源集合中的成员
     * @return 状态码，1成功，0失败
     */
    public Boolean sMove(final RedisConstant.RedisDBType dbType, final String srckey, final String dstkey, Object member) {
        if (StringUtils.isBlank(srckey) || StringUtils.isBlank(dstkey) || member == null) return false;
        final byte[] sbkey = srckey.getBytes(StandardCharsets.UTF_8);
        final byte[] dbkey = dstkey.getBytes(StandardCharsets.UTF_8);
        final byte[] bmember = gsonRedisSerializer.serialize(member);
        return (Boolean) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection)
                    throws DataAccessException {
                try {
                    connection.select(dbType.ordinal());
                    return connection.sMove(sbkey, dbkey, bmember);
                } catch (Exception e) {
                    log.error("[RedisUtils.sMove]", e);
                    return false;
                }
            }
        });
    }

    /**
     * 取得SET所有成员
     *
     * @param key
     * @return
     */
    @SuppressWarnings("unchecked")
    public Set<byte[]> sMembers(final String key) {
        return sMembers(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key);
    }

    /**
     * 取得SET所有成员
     *
     * @param key
     * @return
     */
    @SuppressWarnings("unchecked")
    public Set<byte[]> sMembers(final RedisConstant.RedisDBType dbType, final String key) {
        if (StringUtils.isBlank(key)) return Sets.newHashSet();
        final byte[] bkey = key.getBytes(StandardCharsets.UTF_8);
        return (Set<byte[]>) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection)
                    throws DataAccessException {
                try {
                    connection.select(dbType.ordinal());
                    return connection.sMembers(bkey);
                } catch (Exception e) {
                    log.error("[RedisUtils.sMembers]", e);
                }
                return Sets.newHashSet();
            }
        });
    }

    /**
     * 取得SET所有成员
     *
     * @param key
     * @return Set<User> sets = RedisUtils.sMembers("users",User.class);
     */
    @SuppressWarnings("unchecked")
    public Set sMembers(final String key, Class clazz) {
        return sMembers(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key, clazz);
    }

    /**
     * 取得SET所有成员
     *
     * @param key
     * @return
     */
    @SuppressWarnings("unchecked")
    public Set sMembers(final RedisConstant.RedisDBType dbType, final String key, Class clazz) {
        if (StringUtils.isBlank(key)) return Sets.newHashSet();
        final byte[] bkey = key.getBytes(StandardCharsets.UTF_8);
        Set<byte[]> sets = (Set<byte[]>) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection)
                    throws DataAccessException {
                try {
                    connection.select(dbType.ordinal());
                    return connection.sMembers(bkey);

                } catch (Exception e) {
                    log.error("[RedisUtils.sMembers]", e);
                }
                return Sets.newHashSet();
            }
        });
        if (sets == null || sets.size() == 0) {
            return Sets.newHashSet();
        }
        return sets.stream()
                .collect(
                        () -> new HashSet<Object>(),
                        (set, item) -> set.add(gsonRedisSerializer.deserialize(item, clazz)),
                        (set, subSet) -> set.addAll(subSet)
                );
    }

    /**
     * 交集，并返回结果
     *
     * @param keys
     * @return
     */
    public Set<byte[]> sInter(String... keys) {
        return sInter(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, keys);
    }

    /**
     * 交集，并返回结果
     *
     * @param dbType
     * @param keys
     * @return
     */
    public Set<byte[]> sInter(final RedisConstant.RedisDBType dbType, String... keys) {
        if (keys.length == 0) return Sets.newHashSet();
        int vsize = keys.length;
        final byte[][] bkeys = new byte[vsize][];
        for (int i = 0; i < vsize; i++) {
            bkeys[i] = keys[i].getBytes(StandardCharsets.UTF_8);
        }
        return (Set) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection)
                    throws DataAccessException {
                try {
                    connection.select(dbType.ordinal());
                    return connection.sInter(bkeys);
                } catch (Exception e) {
                    log.error("[RedisUtils.sInter]", e);
                }
                return Sets.newHashSet();
            }
        });
    }

    /**
     * 交集，产生的结果保存在新的key中（newkey）
     *
     * @param newkey
     * @param keys
     * @return
     */
    public Long sInterStore(String newkey, String... keys) {
        return sInterStore(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, newkey, keys);
    }

    /**
     * 交集，产生的结果保存在新的key中（newkey）
     *
     * @param dbType
     * @param newkey
     * @param keys
     * @return
     */
    public Long sInterStore(final RedisConstant.RedisDBType dbType, String newkey, String... keys) {
        if (StringUtils.isBlank(newkey)) return 0l;
        if (keys.length == 0) return 0l;
        final byte[] newbkey = newkey.getBytes(StandardCharsets.UTF_8);
        int vsize = keys.length;
        final byte[][] bkeys = new byte[vsize][];
        for (int i = 0; i < vsize; i++) {
            bkeys[i] = keys[i].getBytes(StandardCharsets.UTF_8);
        }

        return (Long) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection)
                    throws DataAccessException {
                try {
                    connection.select(dbType.ordinal());
                    return connection.sInterStore(newbkey, bkeys);
                } catch (Exception e) {
                    log.error("[RedisUtils.sInterStore]", e);
                }
                return 0l;
            }
        });
    }

    /**
     * 并集，产生的结果保存在新的key中（newkey）
     *
     * @param keys
     * @return
     */
    public Set<byte[]> sUnion(String... keys) {
        return sUnion(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, keys);
    }

    /**
     * 并集，产生的结果保存在新的key中（newkey）
     *
     * @param dbType
     * @param keys
     * @return
     */
    public Set<byte[]> sUnion(final RedisConstant.RedisDBType dbType, String... keys) {
        if (keys.length == 0) return Sets.newHashSet();
        int vsize = keys.length;
        final byte[][] bkeys = new byte[vsize][];
        for (int i = 0; i < vsize; i++) {
            bkeys[i] = keys[i].getBytes(StandardCharsets.UTF_8);
        }
        return (Set) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection)
                    throws DataAccessException {
                try {
                    connection.select(dbType.ordinal());
                    return connection.sUnion(bkeys);
                } catch (Exception e) {
                    log.error("[RedisUtils.sUnion]", e);
                }
                return Sets.newHashSet();
            }
        });
    }

    /**
     * 差集，返回从第一组和所有的给定集合之间的差异的成员
     *
     * @param keys
     * @return 差异的成员集合
     */
    public Set<byte[]> sDiff(String... keys) {
        return sDiff(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, keys);
    }

    /**
     * 差集，返回从第一组和所有的给定集合之间的差异的成员
     *
     * @param keys
     * @return 差异的成员集合
     */
    public Set<byte[]> sDiff(final RedisConstant.RedisDBType dbType, String... keys) {
        if (keys.length == 0) return Sets.newHashSet();
        int vsize = keys.length;
        final byte[][] bkeys = new byte[vsize][];
        for (int i = 0; i < vsize; i++) {
            bkeys[i] = keys[i].getBytes(StandardCharsets.UTF_8);
        }
        return (Set) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection)
                    throws DataAccessException {
                try {
                    connection.select(dbType.ordinal());
                    return connection.sDiff(bkeys);
                } catch (Exception e) {
                    log.error("[RedisUtils.sDiff]", e);
                }
                return Sets.newHashSet();
            }
        });
    }

    /**
     * 差集，将从第一组和所有的给定集合之间的差异的成员保存到新的Key中（newkey）
     *
     * @param keys
     * @return 差异的成员集合
     */
    public Long sDiffStore(String newkey, String... keys) {
        return sDiffStore(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, newkey, keys);
    }

    /**
     * 差集，将从第一组和所有的给定集合之间的差异的成员保存到新的Key中（newkey）
     *
     * @param keys
     * @return 差异的成员集合
     */
    public Long sDiffStore(final RedisConstant.RedisDBType dbType, String newkey, String... keys) {
        if (StringUtils.isBlank(newkey)) return 0l;
        if (keys.length == 0) return 0l;
        final byte[] newbkey = newkey.getBytes(StandardCharsets.UTF_8);
        int vsize = keys.length;
        final byte[][] bkeys = new byte[vsize][];
        for (int i = 0; i < vsize; i++) {
            bkeys[i] = keys[i].getBytes(StandardCharsets.UTF_8);
        }

        return (Long) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection)
                    throws DataAccessException {
                try {
                    connection.select(dbType.ordinal());
                    return connection.sDiffStore(newbkey, bkeys);
                } catch (Exception e) {
                    log.error("[RedisUtils.sDiffStore]", e);
                }
                return 0l;
            }
        });
    }

    /**
     * 并集，产生的结果保存在新的key中（newkey）
     *
     * @param newkey
     * @param keys
     * @return
     */
    public Long sUnionStore(String newkey, String... keys) {
        return sUnionStore(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, newkey, keys);
    }

    /**
     * 并集，产生的结果保存在新的key中（newkey）
     *
     * @param dbType
     * @param newkey
     * @param keys
     * @return
     */
    public Long sUnionStore(final RedisConstant.RedisDBType dbType, String newkey, String... keys) {
        if (StringUtils.isBlank(newkey)) return 0l;
        if (keys.length == 0) return 0l;
        final byte[] newbkey = newkey.getBytes(StandardCharsets.UTF_8);
        int vsize = keys.length;
        final byte[][] bkeys = new byte[vsize][];
        for (int i = 0; i < vsize; i++) {
            bkeys[i] = keys[i].getBytes(StandardCharsets.UTF_8);
        }

        return (Long) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection)
                    throws DataAccessException {
                try {
                    connection.select(dbType.ordinal());
                    return connection.sUnionStore(newbkey, bkeys);
                } catch (Exception e) {
                    log.error("[RedisUtils.sUnionStore]", e);
                }
                return 0l;
            }
        });
    }

    // ---------- //
    //  有序集合/zSet
    // ---------- //

    /**
     * 向有序集合中添加一个成员，为一个Key添加一个值。如果这个值已经在这个Key中，则返回FALSE
     *
     * @param key
     * @param value
     * @param expireSeconds 过期时间（单位：秒），大于0有效
     */
    public Boolean zAdd(String key, Object value, double scoure, final long expireSeconds) {
        return zAdd(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key, value, scoure, expireSeconds);
    }

    /**
     * 向有序集合中添加一个成员，为一个Key添加一个值。如果这个值已经在这个Key中，则返回FALSE
     *
     * @param key
     * @param value
     */
    public Boolean zAdd(final RedisConstant.RedisDBType dbType, String key, Object value, double scoure, final long expireSeconds) {
        if (StringUtils.isBlank(key) || value == null) return false;
        final byte[] bkey = key.getBytes(StandardCharsets.UTF_8);
        final byte[] bvalue = gsonRedisSerializer.serialize(value);
        return (Boolean) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection)
                    throws DataAccessException {
                Boolean result = true;
                try {
                    connection.select(dbType.ordinal());
                    result = connection.zAdd(bkey, scoure, bvalue);
                    if (expireSeconds > 0) {
                        connection.expire(bkey, expireSeconds);
                    }
                    return result;
                } catch (Exception e) {
                    log.error("[RedisUtils.zAdd]", e);
                }
                return false;
            }
        });
    }

    /**
     * 向有序集合中添加一个成员，以分值/scoure作为值的id
     * 如果scoure存在，就替换
     *
     * @param key
     * @param value
     * @param scoure        分值
     * @param expireSeconds 过期时间（单位：秒），大于0有效
     */
    public Boolean zSetEX(String key, Object value, double scoure, final long expireSeconds) {
        return zSetEX(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key, value, scoure, expireSeconds);
    }

    /**
     * 向有序集合中添加一个成员，为一个Key添加一个值。
     * 如果这个值已经在这个Key中，则返回FALSE,并替换
     *
     * @param key
     * @param value
     */
    public Boolean zSetEX(final RedisConstant.RedisDBType dbType, String key, Object value, double score, final long expireSeconds) {
        if (StringUtils.isBlank(key) || value == null) return false;
        final byte[] bkey = key.getBytes(StandardCharsets.UTF_8);
        final byte[] bvalue = gsonRedisSerializer.serialize(value);
        return (Boolean) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection)
                    throws DataAccessException {
                try {
                    connection.select(dbType.ordinal());
                    connection.zRemRangeByScore(bkey, score, score);
                    Boolean result = connection.zAdd(bkey, score, bvalue);
                    if (expireSeconds > 0) {
                        connection.expire(bkey, expireSeconds);
                    }
                    return result;
                } catch (Exception e) {
                    log.error("[RedisUtils.zSetEX]", e);
                }
                return false;
            }
        });
    }

    /**
     * 向有序集合中批量添加成员
     *
     * @param key
     * @param values
     * @param expireSeconds 过期时间（单位：秒），大于0有效
     */
    public Boolean zAdd(String key, Set<RedisZSetCommands.Tuple> values, final long expireSeconds) {
        return zAdd(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key, values, expireSeconds);
    }

    /**
     * 向有序集合中添加一个成员，为一个Key添加一个值。如果这个值已经在这个Key中，则返回FALSE
     */
    public Boolean zAdd(final RedisConstant.RedisDBType dbType, String key, Set<RedisZSetCommands.Tuple> values, final long expireSeconds) {
        if (StringUtils.isBlank(key) || values == null || values.size() == 0) return false;
        final byte[] bkey = key.getBytes(StandardCharsets.UTF_8);
        return (Boolean) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection)
                    throws DataAccessException {
                try {
                    connection.select(dbType.ordinal());
                    Long rst = connection.zAdd(bkey, values);
                    if (expireSeconds > 0) {
                        connection.expire(bkey, expireSeconds);
                    }
                    return rst > 0;
                } catch (Exception e) {
                    log.error("[RedisUtils.zAdd]", e);
                }
                return false;
            }
        });
    }

    /**
     * 删除旧缓存数据，并向有序集合中添加新值
     */
    public Boolean zReplace(final RedisConstant.RedisDBType dbType, String key, Set<RedisZSetCommands.Tuple> values, final long expireSeconds) {
        if (StringUtils.isBlank(key) || values == null || values.size() == 0) return false;
        final byte[] bkey = key.getBytes(StandardCharsets.UTF_8);
        return (Boolean) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection)
                    throws DataAccessException {
                try {
                    connection.select(dbType.ordinal());
                    connection.del(bkey);
                    Long rst = connection.zAdd(bkey, values);
                    if (expireSeconds > 0) {
                        connection.expire(bkey, expireSeconds);
                    }
                    return rst > 0;
                } catch (Exception e) {
                    log.error("[RedisUtils.zAdd]", e);
                }
                return false;
            }
        });
    }

    /**
     * 获取给定值在集合中的权重
     *
     * @param key
     * @param value
     * @return double 权重
     */
    public double zScore(String key, Object value) {
        return zScore(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key, value);
    }

    /**
     * 获取给定值在集合中的权重
     * 异常或给定值不存在，返回-1
     *
     * @param key
     * @param value
     * @return double 权重
     */
    public double zScore(final RedisConstant.RedisDBType dbType, String key, Object value) {
        if (StringUtils.isBlank(key) || value == null) return 0;
        final byte[] bkey = key.getBytes(StandardCharsets.UTF_8);
        final byte[] bvalue = gsonRedisSerializer.serialize(value);
        return (double) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Double doInRedis(RedisConnection connection)
                    throws DataAccessException {
                try {
                    connection.select(dbType.ordinal());
                    return connection.zScore(bkey, bvalue);
                } catch (Exception e) {
                    log.error("[RedisUtils.zScore]", e);
                    return -1d;
                }
            }
        });
    }

    /**
     * 从集合中删除成员
     *
     * @param key
     * @param value
     * @return 返回1成功
     */
    public Long zRem(String key, Object value) {
        return zRem(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key, value);
    }

    /**
     * 从集合中删除成员
     *
     * @param key
     * @param value
     * @return 0：失败
     */
    public Long zRem(final RedisConstant.RedisDBType dbType, String key, Object value) {
        if (StringUtils.isBlank(key) || value == null) return 0l;
        final byte[] bkey = key.getBytes(StandardCharsets.UTF_8);
        final byte[] bvalue = gsonRedisSerializer.serialize(value);
        return (long) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Long doInRedis(RedisConnection connection)
                    throws DataAccessException {
                try {
                    connection.select(dbType.ordinal());
                    return connection.zRem(bkey, bvalue);
                } catch (Exception e) {
                    log.error("[RedisUtils.zRem]", e);
                    return 0L;
                }
            }
        });
    }


    /**
     * 删除给定位置区间的元素
     *
     * @param key
     * @param start 开始区间，从0开始(包含)
     * @param end   结束区间,-1为最后一个元素(包含)
     * @return 删除的数量
     */
    public Long zRemRange(String key, long start, long end) {
        return zRemRange(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key, start, end);
    }

    /**
     * 删除给定位置区间的元素
     *
     * @param key
     * @param start 开始区间，从0开始(包含)
     * @param end   结束区间,-1为最后一个元素(包含)
     * @return 删除的数量
     */
    public Long zRemRange(final RedisConstant.RedisDBType dbType, String key, long start, long end) {
        if (StringUtils.isBlank(key)) return 0l;
        final byte[] bkey = key.getBytes(StandardCharsets.UTF_8);
        return (Long) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Long doInRedis(RedisConnection connection)
                    throws DataAccessException {
                try {
                    connection.select(dbType.ordinal());
                    return connection.zRemRange(bkey, start, end);
                } catch (Exception e) {
                    log.error("[RedisUtils.zRemRange]", e);
                    return 0L;
                }
            }
        });
    }

    /**
     * 删除给定权重区间的元素
     *
     * @param key
     * @param min 下限权重(包含)
     * @param max 上限权重(包含)
     * @return 删除的数量
     */
    public Long zRemRangeByScore(String key, double min, double max) {
        return zRemRangeByScore(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key, min, max);
    }

    /**
     * 删除给定权重区间的元素
     *
     * @param key
     * @param min 下限权重(包含)
     * @param max 上限权重(包含)
     * @return 删除的数量
     */
    public Long zRemRangeByScore(final RedisConstant.RedisDBType dbType, String key, double min, double max) {
        if (StringUtils.isBlank(key)) return 0l;
        final byte[] bkey = key.getBytes(StandardCharsets.UTF_8);
        return (Long) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Long doInRedis(RedisConnection connection)
                    throws DataAccessException {
                try {
                    connection.select(dbType.ordinal());
                    return connection.zRemRangeByScore(bkey, min, max);
                } catch (Exception e) {
                    log.error("[RedisUtils.zRemRangeByScore]", e);
                    return 0L;
                }
            }
        });
    }

    /**
     * 权重增加给定值，如果给定的member已存在
     *
     * @param key
     * @param scoure 要增的权重
     * @param value  要插入的值
     * @return 增后的权重
     */
    public double zIncrBy(String key, double scoure, Object value) {
        return zIncrBy(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key, scoure, value);
    }

    /**
     * 权重增加给定值，如果给定的member已存在
     *
     * @param key
     * @param scoure 要增的权重
     * @param value  要插入的值
     * @return 增后的权重
     */
    public double zIncrBy(final RedisConstant.RedisDBType dbType, String key, double scoure, Object value) {
        if (StringUtils.isBlank(key) || value == null) return 0d;
        final byte[] bkey = key.getBytes(StandardCharsets.UTF_8);
        final byte[] bvalue = gsonRedisSerializer.serialize(value);
        return (double) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Double doInRedis(RedisConnection connection)
                    throws DataAccessException {
                try {
                    connection.select(dbType.ordinal());
                    return connection.zIncrBy(bkey, scoure, bvalue);
                } catch (Exception e) {
                    log.error("[RedisUtils.zIncrBy]", e);
                    return 0d;
                }
            }
        });
    }

    /**
     * 获取集合中元素的数量
     *
     * @param key
     * @return 如果返回0则集合不存在
     */
    public Long zCard(String key) {
        return zCard(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key);
    }

    /**
     * 获取集合中元素的数量
     *
     * @param key
     * @return 如果返回0则集合不存在
     */
    public Long zCard(final RedisConstant.RedisDBType dbType, String key) {
        if (StringUtils.isBlank(key)) return 0l;
        final byte[] bkey = key.getBytes(StandardCharsets.UTF_8);
        return (long) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Long doInRedis(RedisConnection connection)
                    throws DataAccessException {
                try {
                    connection.select(dbType.ordinal());
                    return connection.zCard(bkey);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    return 0L;
                }
            }
        });
    }

    /**
     * 获取指定权重区间内集合的数量
     *
     * @param key
     * @param min 最小排序位置
     * @param max 最大排序位置
     */
    public Long zCount(String key, double min, double max) {
        return zCount(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key, min, max);
    }

    /**
     * 获取指定权重区间内集合的数量
     *
     * @param key
     * @param min 最小排序位置
     * @param max 最大排序位置
     */
    public Long zCount(final RedisConstant.RedisDBType dbType, String key, double min, double max) {
        if (StringUtils.isBlank(key)) return 0L;
        byte[] bkey = key.getBytes(StandardCharsets.UTF_8);
        return (Long) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Long doInRedis(RedisConnection connection)
                    throws DataAccessException {
                try {
                    connection.select(dbType.ordinal());
                    return connection.zCount(bkey, min, max);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    return 0L;
                }
            }
        });
    }

    /**
     * 获得set的长度
     *
     * @param key
     * @return
     */
    public Integer zLength(String key) {
        return zLength(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key);
    }

    /**
     * 获得set的长度
     *
     * @param key
     * @return
     */
    public Integer zLength(final RedisConstant.RedisDBType dbType, String key) {
        if (StringUtils.isBlank(key)) return 0;
        byte[] bkey = key.getBytes(StandardCharsets.UTF_8);
        return (Integer) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Integer doInRedis(RedisConnection connection)
                    throws DataAccessException {
                try {
                    connection.select(dbType.ordinal());
                    return connection.zRange(bkey, 0, -1).size();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    return 0;
                }
            }
        });
    }

    /**
     * 返回指定位置的集合元素,0为第一个元素，-1为最后一个元素
     *
     * @param key
     * @param start 开始位置(包含)
     * @param end   结束位置(包含)
     * @return Set<byte                                                                                                                                                                                                                                                               [                                                                                                                                                                                                                                                               ]>
     */
    public Set<byte[]> zRange(String key, long start, long end) {
        return zRange(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key, start, end);
    }

    /**
     * 返回指定位置的集合元素,0为第一个元素，-1为最后一个元素
     * (按score从大到小排序)
     *
     * @param key
     * @param start 开始位置(包含)
     * @param end   结束位置(包含)
     * @return Set<byte                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               [                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               ]>
     */
    public Set<byte[]> zRange(final RedisConstant.RedisDBType dbType, String key, long start, long end) {
        if (StringUtils.isBlank(key)) return Sets.newHashSet();
        byte[] bkey = key.getBytes(StandardCharsets.UTF_8);
        return (Set<byte[]>) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection)
                    throws DataAccessException {
                try {
                    connection.select(dbType.ordinal());
                    return connection.zRange(bkey, start, end);
                } catch (Exception e) {
                    log.error("[RedisUtils.zRange]", e);
                    return Sets.newHashSet();
                }
            }
        });
    }

    /**
     * 返回指定位置的集合元素,0为第一个元素，-1为最后一个元素
     * (按score从小到大排序)
     *
     * @param key
     * @param start 开始位置(包含)
     * @param end   结束位置(包含)
     * @return Set
     */
    public List zRange(String key, long start, long end, Class clazz) {
        return zRange(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key, start, end, clazz);
    }

    /**
     * 返回指定位置的集合元素,0为第一个元素，-1为最后一个元素
     * (按score从小到大排序)
     *
     * @param key
     * @param start 开始位置(包含)
     * @param end   结束位置(包含)
     * @return List
     * List<Order> list = zRange(0,"wip:orders",0,-1,Order.class);
     */
    public List zRange(final RedisConstant.RedisDBType dbType, String key, long start, long end, Class clazz) {
        if (StringUtils.isBlank(key)) return Lists.newArrayList();
        byte[] bkey = key.getBytes(StandardCharsets.UTF_8);
        Set<byte[]> sets = (Set) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection)
                    throws DataAccessException {
                try {
                    connection.select(dbType.ordinal());
                    return connection.zRange(bkey, start, end);
                } catch (Exception e) {
                    log.error("[RedisUtils.zRange]", e);
                    return Sets.newHashSet();
                }
            }
        });
        if (sets == null || sets.size() == 0) {
            return Lists.newArrayList();
        }

        return sets.stream().map(t -> gsonRedisSerializer.deserialize(t, clazz))
                .collect(Collectors.toList());
    }

    /**
     * 返回指定位置的集合元素,0为第一个元素，-1为最后一个元素
     * (按score从大到小排序)
     *
     * @param key
     * @param start 开始位置(包含)
     * @param end   结束位置(包含)
     * @return List
     */
    public List zRevRange(String key, long start, long end, Class clazz) {
        return zRevRange(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key, start, end, clazz);
    }

    /**
     * 返回指定位置的集合元素,0为第一个元素，-1为最后一个元素
     * (按score从大到小排序)
     *
     * @param key
     * @param start 开始位置(包含)
     * @param end   结束位置(包含)
     * @return List
     * Set<Order> set = zRevRange(0,"wip:orders",0,-1,Order.class);
     */
    public List zRevRange(final RedisConstant.RedisDBType dbType, String key, long start, long end, Class clazz) {
        if (StringUtils.isBlank(key)) return Lists.newArrayList();
        byte[] bkey = key.getBytes(StandardCharsets.UTF_8);
        Set<byte[]> sets = (Set) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection)
                    throws DataAccessException {
                try {
                    connection.select(dbType.ordinal());
                    return connection.zRevRange(bkey, start, end);
                } catch (Exception e) {
                    log.error("[RedisUtils.zRevRange]", e);
                    return Sets.newHashSet();
                }
            }
        });
        if (sets == null || sets.size() == 0) {
            return Lists.newArrayList();
        }
        return sets.stream().map(t -> gsonRedisSerializer.deserialize(t, clazz))
                .collect(Collectors.toList());
    }

    /**
     * 返回指定位置的集合元素及分数,0为第一个元素，-1为最后一个元素
     * (按score从大到小排序)
     *
     * @param key
     * @param start 开始位置(包含)
     * @param end   结束位置(包含)
     * @return List
     */
    public Set<RedisZSetCommands.Tuple> zRevRangeWithScore(String key, long start, long end) {
        return zRevRangeWithScore(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key, start, end);
    }

    public Set<RedisZSetCommands.Tuple> zRevRangeWithScore(RedisConstant.RedisDBType dbType, String key, long start, long end) {
        if (StringUtils.isBlank(key)) return Sets.newHashSet();
        byte[] bkey = key.getBytes(StandardCharsets.UTF_8);
        Set<RedisZSetCommands.Tuple> sets = (Set) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection)
                    throws DataAccessException {
                try {
                    connection.select(dbType.ordinal());
                    return connection.zRevRangeWithScores(bkey, start, end);
                } catch (Exception e) {
                    log.error("[RedisUtils.zRevRangeWithScore]", e);
                    return Sets.newHashSet();
                }
            }
        });
        if (sets == null) {
            return Sets.newHashSet();
        }
        return sets;
    }

    /**
     * 返回指定位置的集合元素及分数,0为第一个元素，-1为最后一个元素
     * (按score从小到大排序)
     *
     * @param key
     * @param start 开始位置(包含)
     * @param end   结束位置(包含)
     * @return List
     */
    public Set<RedisZSetCommands.Tuple> zRangeWithScore(String key, long start, long end) {
        return zRevRangeWithScore(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key, start, end);
    }

    /**
     * 返回指定位置的集合元素及分数,0为第一个元素，-1为最后一个元素
     * (按score从小到大排序)
     */
    public Set<RedisZSetCommands.Tuple> zRangeWithScore(RedisConstant.RedisDBType dbType, String key, long start, long end) {
        if (StringUtils.isBlank(key)) return Sets.newHashSet();
        byte[] bkey = key.getBytes(StandardCharsets.UTF_8);
        Set<RedisZSetCommands.Tuple> sets = (Set) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection)
                    throws DataAccessException {
                try {
                    connection.select(dbType.ordinal());
                    return connection.zRangeWithScores(bkey, start, end);
                } catch (Exception e) {
                    log.error("[RedisUtils.zRangeWithScore]", e);
                    return Sets.newHashSet();
                }
            }
        });
        if (sets == null) {
            return Sets.newHashSet();
        }
        return sets;
    }

    /**
     * 获取指定值在集合中的位置，集合排序从低到高
     *
     * @param key
     * @param value
     * @return long 位置
     * @see
     */
    public Long zRank(String key, Object value) {
        return zRank(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key, value);
    }

    /**
     * 获取指定值在集合中的位置，集合排序从低到高
     *
     * @param key
     * @param value
     * @return long 位置
     * @see
     */
    public Long zRank(RedisConstant.RedisDBType dbType, String key, Object value) {
        if (StringUtils.isBlank(key)) return -1l;
        final byte[] bkey = key.getBytes(StandardCharsets.UTF_8);
        final byte[] bvalue = gsonRedisSerializer.serialize(value);
        return (Long) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Long doInRedis(RedisConnection connection)
                    throws DataAccessException {
                try {
                    connection.select(dbType.ordinal());
                    return connection.zRank(bkey, bvalue);
                } catch (Exception e) {
                    log.error("[RedisUtils.zRank]", e);
                    return 0L;
                }
            }
        });
    }

    /**
     * 获取指定值在集合中的位置，集合排序从高到低
     *
     * @param key
     * @param value
     * @return long 位置
     * @see
     */
    public Long zRevRank(String key, Object value) {
        return zRevRank(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key, value);
    }

    /**
     * 获取指定值在集合中的位置，集合排序从高到低
     *
     * @param key
     * @param value
     * @return long 位置
     * @see
     */
    public Long zRevRank(final RedisConstant.RedisDBType dbType, String key, Object value) {
        if (StringUtils.isBlank(key)) return -1l;
        final byte[] bkey = key.getBytes(StandardCharsets.UTF_8);
        final byte[] bvalue = gsonRedisSerializer.serialize(value);
        return (Long) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Long doInRedis(RedisConnection connection)
                    throws DataAccessException {
                try {
                    connection.select(dbType.ordinal());
                    return connection.zRevRank(bkey, bvalue);
                } catch (Exception e) {
                    log.error("[RedisUtils.zRevRank]", e);
                    return 0L;
                }
            }
        });
    }

    /**
     * 返回指定权重区间的元素集合
     *
     * @param key
     * @param scoure
     * @param scoure1
     * @return
     */
    public Set<byte[]> zRangeByScore(String key, double scoure, double scoure1) {
        return zRangeByScore(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key, scoure, scoure1);
    }

    /**
     * 返回指定权重区间的元素集合
     *
     * @param key
     * @param scoure
     * @param scoure1
     * @return
     */
    public Set<byte[]> zRangeByScore(final RedisConstant.RedisDBType dbType, String key, double scoure, double scoure1) {
        if (StringUtils.isBlank(key)) return Sets.newHashSet();
        final byte[] bkey = key.getBytes(StandardCharsets.UTF_8);
        return (Set<byte[]>) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection)
                    throws DataAccessException {
                try {
                    connection.select(dbType.ordinal());
                    return connection.zRangeByScore(bkey, scoure, scoure1);
                } catch (Exception e) {
                    log.error("[RedisUtils.zRangeByScore]", e);
                    return Sets.newHashSet();
                }
            }
        });
    }

    /**
     * 返回指定权重区间的元素集合
     *
     * @param key
     * @param scoure
     * @param scoure1
     * @return List<Long> orderids  = redisUtils.rangeByScore("wip:orders",1,10,Long.class);
     */
    public List zRangeByScore(String key, double scoure, double scoure1, Class clazz) {
        return zRangeByScore(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key, scoure, scoure1, clazz);
    }

    /**
     * 返回指定权重区间的元素集合
     *
     * @param key
     * @param scoure
     * @param scoure1
     * @return List<Long> orderids  = redisUtils.rangeByScore(0,"wip:orders",1,10,Long.class);
     */
    public List zRangeByScore(final RedisConstant.RedisDBType dbType, String key, double scoure, double scoure1, Class clazz) {
        if (StringUtils.isBlank(key)) Lists.newArrayList();
        final byte[] bkey = key.getBytes(StandardCharsets.UTF_8);
        Set<byte[]> sets = (Set<byte[]>) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection)
                    throws DataAccessException {
                try {
                    connection.select(dbType.ordinal());
                    return connection.zRangeByScore(bkey, scoure, scoure1);
                } catch (Exception e) {
                    log.error("[RedisUtils.zRangeByScore]", e);
                    return Sets.newHashSet();
                }
            }
        });

        if (sets == null || sets.size() == 0) {
            return Lists.newArrayList();
        }
        return sets.stream().map(t -> gsonRedisSerializer.deserialize(t, clazz))
                .collect(Collectors.toList());
    }

    /**
     * 返回指定权重区间的元素集合中第一个
     *
     * @param key     键值
     * @param scoure
     * @param scoure1
     * @return User user  = redisUtils.rangeByScore("wip:orders",1,10,User.class);
     */
    public Object zRangeOneByScore(String key, double scoure, double scoure1, Class clazz) {
        return zRangeOneByScore(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key, scoure, scoure1, clazz);
    }

    /**
     * 返回指定权重区间的元素集合
     *
     * @param key
     * @param scoure
     * @param scoure1
     * @return User user  = redisUtils.rangeByScore("wip:orders",1,10,User.class);
     */
    public Object zRangeOneByScore(final RedisConstant.RedisDBType dbType, String key, double scoure, double scoure1, Class clazz) {
        if (StringUtils.isBlank(key)) Lists.newArrayList();
        final byte[] bkey = key.getBytes(StandardCharsets.UTF_8);
        Set<RedisZSetCommands.Tuple> sets = (Set<RedisZSetCommands.Tuple>) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection)
                    throws DataAccessException {
                try {
                    connection.select(dbType.ordinal());
                    return connection.zRangeByScoreWithScores(bkey, scoure, scoure1);
                } catch (Exception e) {
                    log.error("[RedisUtils.zRangeByScore]", e);
                    return Sets.newHashSet();
                }
            }
        });

        if (sets == null || sets.size() == 0) {
            return null;
        }
        return sets.stream().filter(t -> {
            return Objects.equals(t.getScore(), scoure);
        })
                .map(t -> gsonRedisSerializer.deserialize(t.getValue(), clazz))
                .findFirst()
                .orElse(null);
    }

    /**
     * 交集，产生的结果保存在新的key中（newkey,覆盖原集合）
     *
     * @param newkey
     * @param keys
     * @return
     */
    public Long zInterStore(String newkey, String... keys) {
        return zInterStore(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, newkey, keys);
    }

    /**
     * 交集，产生的结果保存在新的key中（newkey,覆盖原集合）
     *
     * @param dbType
     * @param newkey
     * @param keys
     * @return
     */
    public Long zInterStore(final RedisConstant.RedisDBType dbType, String newkey, String... keys) {
        if (StringUtils.isBlank(newkey)) return 0l;
        if (keys.length == 0) return 0l;
        final byte[] bnewkey = newkey.getBytes(StandardCharsets.UTF_8);
        int vsize = keys.length;
        final byte[][] bkeys = new byte[vsize][];
        for (int i = 0; i < vsize; i++) {
            bkeys[i] = keys[i].getBytes(StandardCharsets.UTF_8);
        }

        return (Long) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection)
                    throws DataAccessException {
                try {
                    connection.select(dbType.ordinal());
                    return connection.zInterStore(bnewkey, bkeys);
                } catch (Exception e) {
                    log.error("[RedisUtils.zInterStore]", e);
                    return 0L;
                }
            }
        });
    }

    /**
     * 并集，产生的结果保存在新的key中（newkey,覆盖原集合）
     *
     * @param newkey
     * @param keys
     * @return
     */
    public Long zUnionStore(String newkey, String... keys) {
        return zUnionStore(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, newkey, keys);
    }

    /**
     * 并集，产生的结果保存在新的key中（newkey,覆盖原集合）
     *
     * @param dbType
     * @param newkey
     * @param keys
     * @return
     */
    public Long zUnionStore(final RedisConstant.RedisDBType dbType, String newkey, String... keys) {
        if (StringUtils.isBlank(newkey)) return 0l;
        if (keys.length == 0) return 0l;
        final byte[] bnewkey = newkey.getBytes(StandardCharsets.UTF_8);
        int vsize = keys.length;
        final byte[][] bkeys = new byte[vsize][];
        for (int i = 0; i < vsize; i++) {
            bkeys[i] = keys[i].getBytes(StandardCharsets.UTF_8);
        }

        return (Long) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection)
                    throws DataAccessException {
                try {
                    connection.select(dbType.ordinal());
                    return connection.zUnionStore(bnewkey, bkeys);
                } catch (Exception e) {
                    log.error("[RedisUtils.zUnionStore]", e);
                    return 0L;
                }
            }
        });
    }

    /**
     * 并集，产生的结果保存在新的key中（newkey,覆盖原集合）,并返回集合
     *
     * @param newkey
     * @param keys
     * @return
     */
    public Set zUnionStore(String newkey, Class clazz, String... keys) {
        return zUnionStore(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, newkey, clazz, keys);
    }

    /**
     * 并集，产生的结果保存在新的key中（newkey,覆盖原集合）,并返回集合
     *
     * @param dbType
     * @param newkey
     * @param keys
     * @return
     */
    public Set zUnionStore(final RedisConstant.RedisDBType dbType, String newkey, Class clazz, String... keys) {
        if (StringUtils.isBlank(newkey) || keys.length == 0) return Sets.newHashSet();
        final byte[] bnewkey = newkey.getBytes(StandardCharsets.UTF_8);
        int vsize = keys.length;
        final byte[][] bkeys = new byte[vsize][];
        for (int i = 0; i < vsize; i++) {
            bkeys[i] = keys[i].getBytes(StandardCharsets.UTF_8);
        }
        Long cnt = (Long) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection)
                    throws DataAccessException {
                try {
                    connection.select(dbType.ordinal());
                    return connection.zUnionStore(bnewkey, bkeys);
                } catch (Exception e) {
                    log.error("[RedisUtils.zUnionStore]", e);
                    return 0L;
                }
            }
        });
        if (cnt.longValue() == 0) {
            return Sets.newHashSet();
        }
        Set<byte[]> sets = (Set<byte[]>) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Set doInRedis(RedisConnection connection)
                    throws DataAccessException {
                try {
                    connection.select(dbType.ordinal());
                    return connection.zRange(bnewkey, 0, -1);
                } catch (Exception e) {
                    log.error("[RedisUtils.zUnionStore]", e);
                    return Sets.newHashSet();
                }
            }
        });
        if (sets == null || sets.size() == 0) {
            return Sets.newHashSet();
        }

        return sets.stream()
                .collect(Collectors.collectingAndThen(Collectors.toSet(), new Function<Set<byte[]>, Set>() {
                    @Override
                    public Set apply(Set<byte[]> bytes) {
                        final Set result = Sets.newHashSet();
                        bytes.forEach(t -> result.add(gsonRedisSerializer.deserialize(t, clazz)));
                        return result;
                    }
                }));
    }

    /**
     * 将byte数组反序列化为对象
     */
    public Function<byte[], Object> byteDeserialObject = new Function<byte[], Object>() {
        public Object apply(byte[] input) {
            return (Object) gsonRedisSerializer.deserialize(input, Object.class);
        }
    };

    public Function<Object, byte[]> serialObjectToByte = new Function<Object, byte[]>() {
        public byte[] apply(Object input) {
            return gsonRedisSerializer.serialize(input);
        }
    };

    private Function<String, byte[]> StringToByte = new Function<String, byte[]>() {
        public byte[] apply(String input) {
            return StringUtils.getBytes(input);
        }
    };

    //region lua脚本调用

    /**
     * 执行Lua脚本
     *
     * @param fileClasspath
     * @param resultType
     * @param keys
     * @param values
     * @param <T>
     * @return
     */
    public <T> T runLuaScript(String fileClasspath, Class<T> resultType, List<String> keys, Object... values) {
        DefaultRedisScript<T> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource(fileClasspath)));
        redisScript.setResultType(resultType);
        return (T) stringRedisTemplate.execute(redisScript, keys, values);
    }

    /**
     * 执行Lua脚本
     *
     * @param scriptText
     * @param resultType
     * @param keys
     * @param values
     * @param <T>
     * @return
     */
    public <T> T runLuaScriptText(String scriptText, Class<T> resultType, List<String> keys, Object... values) {
        DefaultRedisScript<T> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(scriptText);
        redisScript.setResultType(resultType);
        return (T) stringRedisTemplate.execute(redisScript, keys, values);
    }

    //endregion

    //region 锁相关的方法

    /**
     * 获取锁
     *
     * @param lockKey
     * @param requestId
     * @param expireSeconds
     * @return
     */
    public Boolean getLock(final String lockKey, String requestId, long expireSeconds) {
        return getLock(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockKey, requestId, expireSeconds);
    }

    /**
     * 获取锁
     *
     * @param dbType
     * @param lockKey
     * @param requestId
     * @param expireSeconds
     * @return
     */
    public Boolean getLock(final RedisConstant.RedisDBType dbType, final String lockKey, String requestId, long expireSeconds) {
        if (StringUtils.isEmpty(lockKey) || StringUtils.isEmpty(requestId)) {
            return false;
        }
        String scriptText = "redis.call(\"select\", ARGV[1]); " +
                "if (redis.call(\"setnx\", KEYS[1], ARGV[2]) == 1) then " +
                "redis.call('expire', KEYS[1], ARGV[3]); " +
                "return 1; " +
                "else " +
                "return 0; " +
                "end ";
        Long result = runLuaScriptText(scriptText, Long.class, Lists.newArrayList(lockKey), dbType.ordinal() + "", requestId, expireSeconds + "");
        return result != null && result == 1;
    }

    /**
     * 释放锁
     *
     * @param lockKey
     * @param requestId
     * @return
     */
    public Boolean releaseLock(final String lockKey, final String requestId) {
        return releaseLock(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockKey, requestId);
    }

    /**
     * 释放锁
     *
     * @param dbType
     * @param lockKey
     * @param requestId
     * @return
     */
    public Boolean releaseLock(final RedisConstant.RedisDBType dbType, final String lockKey, final String requestId) {
        if (lockKey == null || lockKey.isEmpty() || requestId == null || requestId.isEmpty()) {
            return false;
        }

        String scriptText = "redis.call(\"select\", ARGV[1]); " +
                "if (redis.call(\"get\", KEYS[1]) == ARGV[2]) then " +
                "return redis.call(\"del\", KEYS[1]); " +
                "else " +
                "return 0 " +
                "end ";

        Long result = runLuaScriptText(scriptText, Long.class, Lists.newArrayList(lockKey), dbType.ordinal() + "", requestId);
        return result == 1;
    }

    /**
     * 释放锁 延时释放锁
     *
     * @param lockKey
     * @param requestId
     * @param delaySeconds
     * @return
     */
    public Boolean releaseLockDelay(final String lockKey, final String requestId, long delaySeconds) {
        return releaseLockDelay(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockKey, requestId, delaySeconds);
    }

    /**
     * 释放锁 延时释放锁
     *
     * @param dbType
     * @param lockKey
     * @param requestId
     * @param delaySeconds
     * @return
     */
    public Boolean releaseLockDelay(final RedisConstant.RedisDBType dbType, final String lockKey, final String requestId, long delaySeconds) {
        if (lockKey == null || lockKey.isEmpty() || requestId == null || requestId.isEmpty()) {
            return false;
        }

        String scriptText = "redis.call(\"select\", ARGV[1]); " +
                "if (redis.call(\"get\", KEYS[1]) == ARGV[2]) then " +
                "return redis.call('expire', KEYS[1], ARGV[3]); " +
                "else " +
                "return 0; " +
                "end ";
        Long result = runLuaScriptText(scriptText, Long.class, Lists.newArrayList(lockKey), dbType.ordinal() + "", requestId, delaySeconds + "");
        return result != null && result == 1;
    }


    public Boolean renameNX(final RedisConstant.RedisDBType dbType, final String oldkey, final String newkey) {
        if (StringUtils.isBlank(newkey) || StringUtils.isBlank(oldkey)) return false;

        if (exists(dbType, oldkey)) {
            final byte[] oldkeys = oldkey.getBytes(StandardCharsets.UTF_8);
            final byte[] newkeys = newkey.getBytes(StandardCharsets.UTF_8);
            return (Boolean) redisTemplate.execute(new RedisCallback() {
                @Override
                public Object doInRedis(RedisConnection connection) throws DataAccessException {
                    try {
                        connection.select(dbType.ordinal());
                        return connection.renameNX(oldkeys, newkeys);
                    } catch (Exception e) {
                        log.error("[RedisUtils.renameNX]", e);
                    }
                    return false;

                }
            });
        }
        return false;
    }

    //endregion

    /**
     * 从ZSet中取出以id为分值的对象
     */
    public <T> List<T> getObjFromZSetByIds(final RedisConstant.RedisDBType dbType, String zsetKey, List<Long> ids, Class<T> classOfT) {
        List<T> listOfT = Lists.newArrayList();
        if (dbType == null || StringUtils.isBlank(zsetKey) || ids == null || ids.isEmpty() || classOfT == null) {
            return listOfT;
        }
        List<Long> idList = ids.stream().distinct().collect(Collectors.toList());
        final byte[] bZSetKey = zsetKey.getBytes(StandardCharsets.UTF_8);
        List list;
        try {
            list = (List) redisTemplate.execute((RedisCallback<List<Object>>) connection -> {
                connection.multi();
                connection.select(dbType.ordinal());
                for (Long id : idList) {
                    connection.zRangeByScore(bZSetKey, id, id);
                }
                return connection.exec();
            });
            if (list != null && !list.isEmpty()) {
                T objOfT;
                Set setObj;
                Object[] arrObj;
                for (Object obj : list) {
                    if (obj instanceof Set) {
                        setObj = (Set) obj;
                        if (!setObj.isEmpty()) {
                            arrObj = setObj.toArray();
                            if (arrObj[0] instanceof byte[]) {
                                objOfT = gson.fromJson(StringUtils.toString((byte[]) arrObj[0]), classOfT);
                                if (objOfT != null) {
                                    listOfT.add(objOfT);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("[RedisUtils.getObjFromZSetByIds]", e);
            listOfT = Lists.newArrayList();
        }

        return listOfT;
    }

    /**
     * 从HASH中取出指定key对象的对象
     */
    public <T> List<T> getObjFromHashByKeys(final RedisConstant.RedisDBType dbType, String hashKey, List<String> keys, Class<T> classOfT) {
        List<T> listOfT = Lists.newArrayList();
        if (dbType == null || StringUtils.isBlank(hashKey) || keys == null || keys.isEmpty() || classOfT == null) {
            return listOfT;
        }
        final byte[] bHashKey = hashKey.getBytes(StandardCharsets.UTF_8);
        List<String> keyList = keys.stream().distinct().collect(Collectors.toList());
        List list;
        try {
            list = (List) redisTemplate.execute((RedisCallback<List<Object>>) connection -> {
                connection.multi();
                connection.select(dbType.ordinal());
                for (String key : keyList) {
                    connection.hGet(bHashKey, key.getBytes(StandardCharsets.UTF_8));
                }
                return connection.exec();
            });
            if (list != null && !list.isEmpty()) {
                T objOfT;
                for (Object obj : list) {
                    if (obj instanceof byte[]) {
                        objOfT = gson.fromJson(StringUtils.toString((byte[]) obj), classOfT);
                        if (objOfT != null) {
                            listOfT.add(objOfT);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("[RedisUtils.getObjFromHashByKeys:redisTemplate.execute]", e);
            listOfT = Lists.newArrayList();
        }
        return listOfT;
    }

}
