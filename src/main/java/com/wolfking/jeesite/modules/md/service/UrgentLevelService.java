package com.wolfking.jeesite.modules.md.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.config.redis.RedisTuple;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.service.LongIDCrudService;
import com.wolfking.jeesite.common.utils.GsonUtils;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.wolfking.jeesite.modules.md.dao.UrgentLevelDao;
import com.wolfking.jeesite.modules.md.entity.UrgentLevel;
import com.wolfking.jeesite.ms.providermd.service.MSUrgentLevelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 */
@Service
@Slf4j
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class UrgentLevelService extends LongIDCrudService<UrgentLevelDao, UrgentLevel> {

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private MSUrgentLevelService msUrgentLevelService;


    /**
     *根据id获取
     **/
    public UrgentLevel get(Long id){
        return msUrgentLevelService.getById(id);
    }

    /**
     *分页查询
     **/
    public Page<UrgentLevel> findPage(Page<UrgentLevel> urgentLevelPage,UrgentLevel urgentLevel){
        return msUrgentLevelService.findList(urgentLevelPage,urgentLevel);
    }

    @Override
    @Transactional()
    public void save(UrgentLevel entity){
        boolean isNew = entity.getIsNewRecord();
        //super.save(entity);   //mark on 2020-1-4
        //调用微服务 add on 2019-12-30
        MSErrorCode msErrorCode = msUrgentLevelService.save(entity,isNew);
        if(msErrorCode.getCode()>0){
            throw new RuntimeException("调用微服务保存加急等级失败.失败原因:" + msErrorCode.getMsg());
        }
        /*
        // mark on 2020-1-4
        if(redisUtils.exists(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_URGENTLEVEL_ALL)) {
            redisUtils.zSetEX(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_URGENTLEVEL_ALL, entity, entity.getId(), 0);
        }
         */
    }

    @Override
    @Transactional
    public void delete(UrgentLevel entity){
        //super.delete(entity);  //mark on 2020-1-4
        //调用微服务 add on 2019-12-30
        MSErrorCode msErrorCode = msUrgentLevelService.delete(entity);
        if(msErrorCode.getCode()>0){
            throw new RuntimeException("调用微服务删除加急等级失败.失败原因:" + msErrorCode.getMsg());
        }
        /*
        // mark on 2020-1-4
        if (redisUtils.zCount(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_URGENTLEVEL_ALL, entity.getId(), entity.getId()) > 0) {
            redisUtils.zRemRangeByScore(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_URGENTLEVEL_ALL, entity.getId(), entity.getId());
        }
        */
    }

    /****************************************************************************
     * redis操作
     ****************************************************************************/

    /**从数据库读取信息至缓存
     * @return
     */
    /*
    // mark on 2020-1-4
    private List<UrgentLevel> loadDataFromDB2Cache(){
        List<UrgentLevel> list = super.findAllList();
        if(list != null && list.size()>0) {
            Set<RedisZSetCommands.Tuple> sets = list.stream()
                    .map(t -> new RedisTuple(redisUtils.gsonRedisSerializer.serialize(t), t.getId().doubleValue()))
                    .collect(Collectors.toSet());
            redisUtils.zAdd(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_URGENTLEVEL_ALL, sets, -1);
            //for (UrgentLevel entity : list) {
            //    redisUtils.zAdd(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_URGENTLEVEL_ALL, entity, entity.getId(), 0);
            //}
        }
        return list;
    }
     */


    /**
     * 加载所有加急等级，当缓存未命中则从数据库装载至缓存
     * @return
     */
    @Override
    public List<UrgentLevel> findAllList(){
        //调用微服务
        List<UrgentLevel> urgentLevels = msUrgentLevelService.findAllList();
        return urgentLevels;
        /*
        // mark on 2020-1-4
        List<Long> urgentLevelIdFromMS = Lists.newArrayList();
        List<Long> urgentLevelIdFromWeb = Lists.newArrayList();
        if(urgentLevels !=null && urgentLevels.size()>0){
            urgentLevelIdFromMS = urgentLevels.stream().map(UrgentLevel::getId).collect(Collectors.toList());
        }
        boolean isExistsCache = redisUtils.exists(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_URGENTLEVEL_ALL);
        if (!isExistsCache){
            return loadDataFromDB2Cache();
        }
        List<UrgentLevel> list = redisUtils.zRange(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_URGENTLEVEL_ALL,0,-1, UrgentLevel.class);
        urgentLevelIdFromWeb = list.stream().map(UrgentLevel::getId).collect(Collectors.toList());
        if(urgentLevelIdFromMS.size()!= urgentLevelIdFromWeb.size()){
            try {
                log.error("web服务于微服务获取的加急列表数据不一致,web端:" + GsonUtils.toGsonString(list) + ",微服务端:" + GsonUtils.toGsonString(urgentLevels));
            }catch (Exception e){}
        }
        return list;
       */
    }

    /**
     * 加载所有加急等级，当缓存未命中则从数据库装载至缓存
     * @return
     */
    public Map<Long,UrgentLevel> findAllMap(){
        List<UrgentLevel> list = findAllList();
        if(list == null || list.size()== 0){
            return Maps.newHashMap();
        }
        return list.stream().collect(Collectors.toMap(
                e->e.getId(),
                e->e
        ));
    }

    /**
     * 获得加急等级信息
     * @param id
     * @return
     */
    public UrgentLevel getFromCache(long id) {
        UrgentLevel urgentLevel = msUrgentLevelService.getFromCache(id);
        return urgentLevel;

        /*
        // mark on 2020-1-4
        boolean isExistsCache = redisUtils.exists(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_URGENTLEVEL_ALL);
        if (!isExistsCache){
            loadDataFromDB2Cache();
        }
        try {
            return (UrgentLevel) redisUtils.zRangeOneByScore(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_URGENTLEVEL_ALL, id, id, UrgentLevel.class);
        }catch (Exception e){
            return null;
        }
         */
    }
}
