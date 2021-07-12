package com.wolfking.jeesite.modules.md.service;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.config.redis.RedisTuple;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.service.LongIDCrudService;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.wolfking.jeesite.modules.md.dao.TimelinessLevelDao;
import com.wolfking.jeesite.modules.md.entity.TimelinessLevel;
import com.wolfking.jeesite.ms.providermd.service.MSTimelinessLevelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class TimelinessLevelService extends LongIDCrudService<TimelinessLevelDao, TimelinessLevel> {

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private MSTimelinessLevelService msTimelinessLevelService;


    /**
     * 根据id获取数据
     * */
    public TimelinessLevel get(Long id){
        return msTimelinessLevelService.getById(id);
    }

    public Page<TimelinessLevel> findPage(Page<TimelinessLevel> timelinessLevelPage,TimelinessLevel TimelinessLevel){
          return msTimelinessLevelService.findList(timelinessLevelPage,TimelinessLevel);
    }

    @Override
    @Transactional()
    public void save(TimelinessLevel entity){
        boolean isNew = entity.getIsNewRecord();
        //super.save(entity); //mark on 2020-1-4
        //调用微服务 add on 2019-12-28
        MSErrorCode msErrorCode = msTimelinessLevelService.save(entity,isNew);
        if(msErrorCode.getCode()>0){
            throw new RuntimeException("调用微服务保存时效等级失败.失败原因：" + msErrorCode.getMsg());
        }
        /*
        // mark on 2020-1-4
        if(redisUtils.exists(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_TIMELINESS_ALL)) {
            redisUtils.zSetEX(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_TIMELINESS_ALL, entity, entity.getId(), 0);
        }
        */
    }

    @Override
    @Transactional
    public void delete(TimelinessLevel entity){
        //super.delete(entity);  //mark on 2020-1-4
        //调用未付add on 2019-12-25
        MSErrorCode msErrorCode = msTimelinessLevelService.delete(entity);
        if(msErrorCode.getCode()>0){
            throw new RuntimeException("调用微服务删除时效等级失败.失败原因：" + msErrorCode.getMsg());
        }

        /*
        // mark on 2020-1-4
        if (redisUtils.zCount(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_TIMELINESS_ALL, entity.getId(), entity.getId()) > 0) {
            redisUtils.zRemRangeByScore(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_TIMELINESS_ALL, entity.getId(), entity.getId());
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
    private List<TimelinessLevel> loadDataFromDB2Cache(){
        List<TimelinessLevel> list = super.findAllList();
        //List<TimelinessLevel> list = msTimelinessLevelService.findAllList();
        if(list != null && list.size()>0) {
            Set<RedisZSetCommands.Tuple> sets = list.stream()
                    .map(t -> new RedisTuple(redisUtils.gsonRedisSerializer.serialize(t), t.getId().doubleValue()))
                    .collect(Collectors.toSet());
            redisUtils.zAdd(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_TIMELINESS_ALL, sets, -1);
            //for (TimelinessLevel entity : list) {
            //    redisUtils.zAdd(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_TIMELINESS_ALL, entity, entity.getId(), 0);
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
    public List<TimelinessLevel> findAllList(){
        List<TimelinessLevel> listTimelinessLevel = msTimelinessLevelService.findAllList();
        return listTimelinessLevel;
        /*
        // mark on 2020-1-4
        if(listTimelinessLevel !=null && listTimelinessLevel.size()>0){
            return listTimelinessLevel;
        }
        boolean isExistsCache = redisUtils.exists(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_TIMELINESS_ALL);
        if (!isExistsCache){
            return loadDataFromDB2Cache();
        }
        List<TimelinessLevel> List = redisUtils.zRange(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_TIMELINESS_ALL,0,-1, TimelinessLevel.class);
        return List;
        */
    }

    /**
     * 获得加急等级信息(没地方调用)
     * @param id
     * @return
     */
    /*
    //mark on 2020-1-4 //没有地方调用，注释以下所有代码
    public TimelinessLevel getFromCache(long id) {
        boolean isExistsCache = redisUtils.exists(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_TIMELINESS_ALL);
        if (!isExistsCache){
            loadDataFromDB2Cache();
        }
        try {
            return (TimelinessLevel) redisUtils.zRangeOneByScore(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_TIMELINESS_ALL, id, id, TimelinessLevel.class);
        }catch (Exception e){
            return null;
        }
    }
     */
}
