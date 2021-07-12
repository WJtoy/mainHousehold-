package com.wolfking.jeesite.modules.md.service;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.service.LongIDCrudService;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.wolfking.jeesite.modules.md.dao.PlanRadiusDao;
import com.wolfking.jeesite.modules.md.entity.PlanRadius;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.ms.providermd.service.MSPlanRadiusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/*
 * 区域半径设定
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class PlanRadiusService extends LongIDCrudService<PlanRadiusDao,PlanRadius> {

    //自动派单区域半径
    public static final int MOD_DIVISOR = 50; //取模除数

    @Autowired
    private AreaService areaService;

    /*@Autowired
    private RedisUtils redisUtils;*/

    @Autowired
    private MSPlanRadiusService msPlanRadiusService;

    public PlanRadius get(Long id) {
        //return dao.get(id); mark on 2019-10-18
        //调用微服务 start 2019-10-18
        return msPlanRadiusService.getById(id);
        // end
    }

    public PlanRadius getByAreaId(long areaId) {
        if(areaId<=0){
            return null;
        }
        return Optional.ofNullable(getAreaIdFromCache(areaId)).orElse(null);
    }

    public List<PlanRadius> findList(PlanRadius planRadius) {
        return dao.findList(planRadius);
    }

    @Override
    public Page<PlanRadius> findPage(Page<PlanRadius> page, PlanRadius planRadius){
        if(planRadius.getArea()!=null && planRadius.getArea().getId()>0){
            Integer areaType = planRadius.getArea().getType();
            if(areaType.equals(Area.TYPE_VALUE_PROVINCE) || areaType.equals(Area.TYPE_VALUE_CITY)){
                String strParentIds= planRadius.getArea().getParentIds()+planRadius.getArea().getId()+",%";
                Area area = new Area();
                area.setParentIds(strParentIds);
                area.setType(Area.TYPE_VALUE_COUNTY);
                List<Long> areaIdList = areaService.findIdByParentIdsAndType(area);
               if(areaIdList!=null && areaIdList.size()>0){
                   planRadius.setAreaIdList(areaIdList);
               }
            }
        }
        return msPlanRadiusService.findList(page,planRadius);
    }


    public void save(PlanRadius planRadius) {
        boolean isNew = planRadius.getIsNewRecord();
        //super.save(planRadius);   //mark on 2020-1-9
        //调用微服务 start 2019-10-8
        MSErrorCode errorCode = msPlanRadiusService.save(planRadius,isNew);
        if(errorCode.getCode()>0){
            throw new RuntimeException("保存派单区域半径失败.失败原因:" + errorCode.getMsg());
        }
        //end
        //updateFromCache(planRadius);  //mark on 2020-1-9
    }

    public void delete(PlanRadius planRadius) {
        planRadius.setDelFlag(PlanRadius.DEL_FLAG_DELETE);
        planRadius.preUpdate();
        //dao.delete(planRadius);  //mark on 2020-1-9
        //调用微服务 add on 2019-10-21
        MSErrorCode errorCode = msPlanRadiusService.enableOrDisable(planRadius);
        if(errorCode.getCode()>0){
            throw new RuntimeException("停用派单区域半径失败.失败原因:" + errorCode.getMsg());
        }
        // end
        /*
        //mark on 2020-1-9
        Long areaId =  Optional.ofNullable(planRadius).map(PlanRadius::getArea).map(Area::getId).orElse(null);
        deleteAreaIdFromCache(areaId);
        */
    }

    public void enable(PlanRadius planRadius) {
        planRadius.setDelFlag(PlanRadius.DEL_FLAG_NORMAL);
        planRadius.preUpdate();
        //dao.delete(planRadius);  //mark on 2020-1-9
        //调用微服务 add on 2019-10-21
        MSErrorCode errorCode = msPlanRadiusService.enableOrDisable(planRadius);
        if(errorCode.getCode()>0){
            throw new RuntimeException("启动派单区域半径失败.失败原因:" + errorCode.getMsg());
        }
        // end
        //updateFromCache(planRadius);  //mark on 2020-1-9
    }

    public List<Area>  findAreaList(Area area) {
        String parentIds = area.getParentIds()+area.getId();

        List<Area> subAreaList = areaService.findListByType(Area.TYPE_VALUE_COUNTY);
        final String finalParentIds = parentIds;

        List<Area> countyAreaList =subAreaList.stream().filter(r->r.getParentIds().startsWith(finalParentIds)).collect(Collectors.toList());
        return countyAreaList;
    }

    //region cache

    /**
     * 读取缓存中配置
     * 对传入区域id进行模运算，将区域分组
     * @param areaId 区域id
     * @return
     */
    public PlanRadius getAreaIdFromCache(Long areaId) {
        if(areaId==null || areaId<=0){
            return new PlanRadius();
        }
        //调用微服务 add on 2019-10-21
        PlanRadius  planRadius = msPlanRadiusService.getByAreaId(areaId);
        if(planRadius!=null){
            Area area = areaService.getFromCache(areaId);
            planRadius.setArea(area);
            return planRadius;
        }else{
            planRadius = new PlanRadius();
            Area area = areaService.getFromCache(areaId);
            planRadius.setArea(area);
            return planRadius;
        }
        // end
        //mark on 2019-10-23
/*      String key = MessageFormat.format(RedisConstant.MD_AREA_AUTO_PLAN_RADIUS,areaId % MOD_DIVISOR);
        planRadius = redisUtils.hGet(RedisConstant.RedisDBType.REDIS_SYS_AREA, key, areaId.toString(), PlanRadius.class);
        if(planRadius == null) {
            planRadius = dao.getByAreaId(areaId);
//            if (planRadius == null) {
//                planRadius = new PlanRadius(areaId);
//            }
            //set cache
            if (!org.springframework.util.ObjectUtils.isEmpty(planRadius)) {
                redisUtils.hmSet(RedisConstant.RedisDBType.REDIS_SYS_AREA, key, areaId.toString(), planRadius, 0);
            } else {
                planRadius = new PlanRadius();
                Area area = areaService.getFromCache(areaId);
                planRadius.setArea(area);
            }
        }*/
        //return planRadius;
    }

    /**
     * 读取缓存中区域自动派单检索半径
     * @param areaId 区县id
     * @return PlanRadius 如缓存中没有返回null
     */
    /*
    // mark on 2020-1-9 begin
    public PlanRadius getByAreaIdFromCache(Long areaId) {
        if(areaId==null || areaId<=0){
            return null;
        }
        String key = MessageFormat.format(RedisConstant.MD_AREA_AUTO_PLAN_RADIUS,areaId % MOD_DIVISOR);
        return redisUtils.hGet(RedisConstant.RedisDBType.REDIS_SYS_AREA, key, areaId.toString(), PlanRadius.class);
    }

    public void updateFromCache(PlanRadius planRadius) {
        Long areaId =  Optional.ofNullable(planRadius).map(PlanRadius::getArea).map(Area::getId).orElse(null);
        if(areaId==null || areaId<=0){
           return ;
        }
        String key = MessageFormat.format(RedisConstant.MD_AREA_AUTO_PLAN_RADIUS,areaId % MOD_DIVISOR);
        //set cache
        redisUtils.hmSet(RedisConstant.RedisDBType.REDIS_SYS_AREA, key, areaId.toString(), planRadius, 0);
    }

    public void deleteAreaIdFromCache(Long areaId) {
        if (areaId == null) {
            return;
        }
        String key = MessageFormat.format(RedisConstant.MD_AREA_AUTO_PLAN_RADIUS,areaId % MOD_DIVISOR);
        redisUtils.hdel(RedisConstant.RedisDBType.REDIS_SYS_AREA, key, areaId.toString());
    }
    // mark on 2020-1-9 end
    */
}
