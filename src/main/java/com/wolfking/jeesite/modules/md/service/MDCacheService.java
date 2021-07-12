package com.wolfking.jeesite.modules.md.service;

import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.persistence.LongIDBaseEntity;
import com.wolfking.jeesite.common.service.BaseService;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.md.entity.CacheDataTypeEnum;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.md.utils.ServicePointAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * 基础资料缓存操作
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class MDCacheService extends BaseService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ServicePointAdapter servicePointAdapter;

    //------------------------------------------------------------------------------------------------------ServicePoint

    /**
     * 新增/更新单个网点信息的缓存
     */
    public boolean updateServicePoint(ServicePoint servicePoint) {
        boolean result = false;
        if (servicePoint != null && servicePoint.getId() != null && servicePoint.getId() > 0) {
            String servicePointJson = toObjJson(servicePoint, CacheDataTypeEnum.SERVICEPOINT);
            result = zAddOrUpdateByScore(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_SERVICEPOINT_ALL, servicePoint.getId(), servicePointJson, 0L);
            if (!result) {
                deleteServicePoint(servicePoint.getId());
            }
        }
        return result;
    }

    /**
     * 新增/更新一组网点信息的缓存（少量的数据更新）
     */
    public boolean updateServicePoints(List<ServicePoint> servicePoints) {
        boolean result = false;
        if (servicePoints != null && !servicePoints.isEmpty()) {
            servicePoints = servicePoints.stream().filter(i -> i.getId() != null && i.getId() > 0).collect(Collectors.toList());
            if (!servicePoints.isEmpty()) {
                List<Long> scores = servicePoints.stream().map(LongIDBaseEntity::getId).collect(Collectors.toList());
                List<String> servicePointJsons = servicePoints.stream()
                        .map(i -> toObjJson(i, CacheDataTypeEnum.SERVICEPOINT))
                        .filter(StringUtils::isNotBlank).collect(Collectors.toList());
                result = zAddOrUpdateByScores(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_SERVICEPOINT_ALL, scores, servicePointJsons, 0L);
                if (!result) {
                    deleteServicePoints(scores);
                }
            }

        }
        return result;
    }

    /**
     * 新增/更新一组网点信息的缓存(大批量的更新数据)
     */
    public boolean updateServicePointsPipelined(List<ServicePoint> servicePoints) {
        boolean result = false;
        if (servicePoints != null && !servicePoints.isEmpty()) {
            servicePoints = servicePoints.stream().filter(i -> i.getId() != null && i.getId() > 0).collect(Collectors.toList());
            if (!servicePoints.isEmpty()) {
                List<Long> scores = servicePoints.stream().map(LongIDBaseEntity::getId).collect(Collectors.toList());
                List<String> servicePointJsons = servicePoints.stream()
                        .map(i -> toObjJson(i, CacheDataTypeEnum.SERVICEPOINT))
                        .filter(StringUtils::isNotBlank).collect(Collectors.toList());
                result = zAddOrUpdateByScoresPipelined(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_SERVICEPOINT_ALL, scores, servicePointJsons, 0L);
                if (!result) {
                    deleteServicePoints(scores);
                }
            }

        }
        return result;
    }

    /**
     * 删除单个网点信息的缓存
     */
    public boolean deleteServicePoint(Long servicePointId) {
        boolean result = false;
        if (servicePointId != null && servicePointId > 0) {
            result = zRemRangeByScore(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_SERVICEPOINT_ALL, servicePointId);
        }
        return result;
    }

    /**
     * 删除一组网点信息的缓存
     */
    public boolean deleteServicePoints(List<Long> servicePointIds) {
        boolean result = false;
        if (servicePointIds != null && !servicePointIds.isEmpty()) {
            List<Long> scores = servicePointIds.stream().filter(Objects::nonNull).collect(Collectors.toList());
            if (!scores.isEmpty()) {
                result = zRemRangeByScores(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_SERVICEPOINT_ALL, scores);
            }
        }
        return result;
    }

    //---------------------------------------------------------------------------------------------------Redis Operation

    /**
     * 将对象转成JSon字符串
     */
    private String toObjJson(Object obj, CacheDataTypeEnum objType) {
        String valueJson = null;
        if (obj != null && objType != null) {
            try {
                switch (objType) {
                    case SERVICEPOINT:
                        valueJson = servicePointAdapter.toJson((ServicePoint) obj);
                }
            } catch (Exception e) {
                log.error("[MDCacheService.toObjJson]", e);
            }
        }
        return valueJson;
    }


    /**
     * 向有序列表中添加或更新元素
     */
    private boolean zAddOrUpdateByScore(final RedisConstant.RedisDBType dbType, final String key, final long score, final String objJson, final long expireSeconds) {
        if (dbType == null || StringUtils.isBlank(key) || StringUtils.isBlank(objJson)) {
            return false;
        }
        final byte[] bKey = key.getBytes(StandardCharsets.UTF_8);
        final byte[] bValue = objJson.getBytes(StandardCharsets.UTF_8);
        Boolean result;
        try {
            result = (Boolean) redisTemplate.execute((RedisCallback<Boolean>) connection -> {
                connection.select(dbType.ordinal());
                connection.multi();
                connection.zRemRangeByScore(bKey, score, score);
                connection.zAdd(bKey, score, bValue);
                if (expireSeconds > 0) {
                    connection.expire(bKey, expireSeconds);
                }
                connection.exec();
                return true;
            });
        } catch (Exception e) {
            result = false;
            log.error("[MDCacheService.zAddOrUpdateByScore]", e);
        }
        return result == null ? false : result;
    }

    /**
     * 向有序列表中添加或更新一组元素
     */
    private boolean zAddOrUpdateByScores(final RedisConstant.RedisDBType dbType, final String key, final List<Long> scores, final List<String> objJsons, final long expireSeconds) {
        if (dbType == null || StringUtils.isBlank(key) || scores == null || scores.isEmpty() || objJsons == null || objJsons.isEmpty() || scores.size() != objJsons.size()) {
            return false;
        }
        final byte[] bKey = key.getBytes(StandardCharsets.UTF_8);
        final byte[][] bValues = new byte[objJsons.size()][];
        for (int i = 0; i < objJsons.size(); i++) {
            bValues[i] = objJsons.get(i).getBytes(StandardCharsets.UTF_8);
        }
        Boolean result;
        try {
            result = (Boolean) redisTemplate.execute((RedisCallback<Boolean>) connection -> {
                connection.select(dbType.ordinal());
                connection.multi();
                double score;
                for (int i = 0; i < objJsons.size(); i++) {
                    score = scores.get(i);
                    connection.zRemRangeByScore(bKey, score, score);
                    connection.zAdd(bKey, score, bValues[i]);
                }
                if (expireSeconds > 0) {
                    connection.expire(bKey, expireSeconds);
                }
                connection.exec();
                return true;
            });
        } catch (Exception e) {
            result = false;
            log.error("[MDCacheService.zAddOrUpdateByScores]", e);
        }
        return result == null ? false : result;
    }

    /**
     * 向有序列表中添加或更新一组元素
     * <p>
     * 数据量过大时，需要使用管道，并且不能使用事务（使用事务会出现连接超时）
     */
    private boolean zAddOrUpdateByScoresPipelined(final RedisConstant.RedisDBType dbType, final String key, final List<Long> scores, final List<String> objJsons, final long expireSeconds) {
        if (dbType == null || StringUtils.isBlank(key) || scores == null || scores.isEmpty() || objJsons == null || objJsons.isEmpty() || scores.size() != objJsons.size()) {
            return false;
        }
        final byte[] bKey = key.getBytes(StandardCharsets.UTF_8);
        final byte[][] bValues = new byte[objJsons.size()][];
        for (int i = 0; i < objJsons.size(); i++) {
            bValues[i] = objJsons.get(i).getBytes(StandardCharsets.UTF_8);
        }
        boolean result;
        try {
            redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                connection.select(dbType.ordinal());
                double score;
                for (int i = 0; i < objJsons.size(); i++) {
                    score = scores.get(i);
                    connection.zRemRangeByScore(bKey, score, score);
                    connection.zAdd(bKey, score, bValues[i]);
                }
                if (expireSeconds > 0) {
                    connection.expire(bKey, expireSeconds);
                }
                return null;
            });
            result = true;
        } catch (Exception e) {
            result = false;
            log.error("[MDCacheService.zAddOrUpdateByScoresPipelined]", e);
        }
        return result;
    }

    /**
     * 删除集合中指定分值对应的元素
     */
    private boolean zRemRangeByScore(final RedisConstant.RedisDBType dbType, final String key, final long score) {
        if (dbType == null || StringUtils.isBlank(key)) {
            return false;
        }
        final byte[] bKey = key.getBytes(StandardCharsets.UTF_8);
        Boolean result;
        try {
            result = (Boolean) redisTemplate.execute((RedisCallback<Boolean>) connection -> {
                connection.select(dbType.ordinal());
                connection.zRemRangeByScore(bKey, score, score);
                return true;
            });
        } catch (Exception e) {
            log.error("[MDCacheService.zRemRangeByScore]", e);
            result = false;
        }
        return result;
    }

    /**
     * 删除集合中指定分值对应的元素
     */
    private boolean zRemRangeByScores(final RedisConstant.RedisDBType dbType, final String key, final List<Long> scores) {
        if (dbType == null || StringUtils.isBlank(key) || scores == null || scores.isEmpty()) {
            return false;
        }
        final byte[] bKey = key.getBytes(StandardCharsets.UTF_8);
        Boolean result;
        try {
            result = (Boolean) redisTemplate.execute((RedisCallback<Boolean>) connection -> {
                connection.select(dbType.ordinal());
                connection.multi();
                for (Long score : scores) {
                    connection.zRemRangeByScore(bKey, score, score);
                }
                connection.exec();
                return true;
            });
        } catch (Exception e) {
            log.error("[MDCacheService.zRemRangeByScores]", e);
            result = false;
        }
        return result;
    }

}
