/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.wolfking.jeesite.modules.sys.service;

import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.kkl.kklplus.entity.sys.SyncTypeEnum;
import com.kkl.kklplus.entity.sys.mq.MQSysUserRegionMessage;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.service.LongIDCrudService;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.wolfking.jeesite.modules.mq.sender.RPTSysUserRegionSender;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderSearchModel;
import com.wolfking.jeesite.modules.sd.entity.viewModel.RegionSearchModel;
import com.wolfking.jeesite.modules.sys.dao.UserRegionDao;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.UserRegion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 用户区域权限Service
 *
 * @author Ryan
 * @version 2019-10-12
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class UserRegionService extends LongIDCrudService<UserRegionDao, UserRegion> {

    @Autowired
    private RedisUtils redisUtils;
    @SuppressWarnings("rawtypes")
    @Autowired
    public RedisTemplate redisTemplate;

    @Autowired
    private RPTSysUserRegionSender sysUserRegionSender;

    /**
     * 获得用户区域列表
     */
    public List<UserRegion> getUserRegions(Long userId){
        if (userId == null || userId <= 0){
            return Lists.newArrayListWithCapacity(0);
        }
        //from cache
        if(redisUtils.hexist(RedisConstant.RedisDBType.REDIS_SYS_DB,RedisConstant.SYS_USER_REGION,userId.toString())){
            return redisUtils.hGetList(RedisConstant.RedisDBType.REDIS_SYS_DB, RedisConstant.SYS_USER_REGION, userId.toString(), UserRegion[].class);
        }
        List<UserRegion> regions = dao.getUserRegions(userId);
        if(org.springframework.util.ObjectUtils.isEmpty(regions)){
            return Lists.newArrayListWithCapacity(0);
        }
        //write cache
        try {
            redisUtils.hmSet(RedisConstant.RedisDBType.REDIS_SYS_DB, RedisConstant.SYS_USER_REGION, userId.toString(), regions, -1);
        }catch (Exception e){
            log.error("redis hmSet user region error:{}",userId,e);
        }
        return regions;
    }

    public List<UserRegion> getUserRegionsFromDB(Long userId){
        // add on 2019-12-11
        if (userId == null || userId <= 0){
            return Lists.newArrayListWithCapacity(0);
        }
        List<UserRegion> regions = dao.getUserRegions(userId);
        if(org.springframework.util.ObjectUtils.isEmpty(regions)){
            return Lists.newArrayListWithCapacity(0);
        }
        return regions;
    }


    /**
     * 保存用户区域
     * @param newRegions 新增的区域列表
     * @param removeRegions 要删除的区域列表
     */
    @Transactional
    public void saveUserRegions(Long userId,Set<UserRegion> newRegions,Set<UserRegion> removeRegions){
        if(userId == null || userId<=0){
            throw new RuntimeException("参数：用户id 未传值");
        }
        if(!ObjectUtils.isEmpty(removeRegions)){
            for(UserRegion region:removeRegions){
                dao.delete(region);

                //  发送用户区域删除消息给报表 begin  2020-9-8
                MQSysUserRegionMessage.SysUserRegionMessage.Builder builder = MQSysUserRegionMessage.SysUserRegionMessage.newBuilder();
                builder.setUserId(region.getUserId());
                builder.setProvinceId(region.getProvinceId());
                builder.setCityId(region.getCityId());
                builder.setAreaId(region.getAreaId());
                builder.setSyncType(SyncTypeEnum.DELETE.getValue());
                sysUserRegionSender.send(builder.build());
                // 发送用户区域删除消息给报表 end
            }
        }
        if(!ObjectUtils.isEmpty(newRegions)){
            for(UserRegion region:newRegions){
                if(region.getUserId() <=0){
                    region.setUserId(userId);
                }
                dao.insert(region);

                // 发送用户区域添加消息给报表 begin 2020-9-8
                MQSysUserRegionMessage.SysUserRegionMessage.Builder builder = MQSysUserRegionMessage.SysUserRegionMessage.newBuilder();
                builder.setSyncType(SyncTypeEnum.ADD.getValue())
                        .setUserId(region.getUserId())
                        .setAreaType(region.getAreaType())
                        .setProvinceId(region.getProvinceId())
                        .setCityId(region.getCityId())
                        .setAreaId(region.getAreaId());

                sysUserRegionSender.send(builder.build());
                // 发送用户区域添加消息给报表 end
            }
        }
        //clear redis
        removeUserRegionCash(userId);
    }

    public void removeUserRegionCash(Long userId){
        if(userId == null || userId <=0){
            return;
        }
        //clear redis
        try {
            redisUtils.hdel(RedisConstant.RedisDBType.REDIS_SYS_DB, RedisConstant.SYS_USER_REGION, userId.toString());
        }catch (Exception e){
            log.error("hdel user regions error:{}",userId,e);
        }
    }

    public void writeUserRegionCache(Long userId, List<UserRegion> userRegionList) {
        //write cache
        try {
            redisUtils.hmSet(RedisConstant.RedisDBType.REDIS_SYS_DB, RedisConstant.SYS_USER_REGION, userId.toString(), userRegionList, -1);
        }catch (Exception e){
            log.error("redis hmSet user region error:{}",userId,e);
        }
    }

    public void deleteByUserId(Long userId) {
        // add on 2019-12-11
        dao.deleteByUserId(userId);

        //  发送用户区域删除消息给报表 begin  2020-9-8
        UserRegion userRegion = new UserRegion();
        userRegion.setUserId(userId);
        MQSysUserRegionMessage.SysUserRegionMessage.Builder builder = MQSysUserRegionMessage.SysUserRegionMessage.newBuilder();
        builder.setUserId(userId);
        builder.setSyncType(SyncTypeEnum.DELETE.getValue());
        sysUserRegionSender.send(builder.build());
        // 发送用户区域删除消息给报表 end
    }



    /**
     * 检查用户是否有目标区域的权限
     * 1.先检查是否有全国的权限
     * 2.再逐一检查是否有省市区/县的权限
     * @return boolean
     */
    public boolean checkUserHasRegionPermission(RegionSearchModel searchModel, List<UserRegion> regions, Area area, Integer areaLevel){
        if(searchModel != null) {
            searchModel.setProvinceList(null);
            searchModel.setCityList(null);
            searchModel.setAreaList(null);
        }
        if(ObjectUtils.isEmpty(regions)){
            return false;
        }
        if(area == null || area.getId() == null || area.getId()<=0){
            return true;
        }
        Supplier<Stream<UserRegion>> streamSupplier = () -> regions.stream();
        UserRegion userRegion = streamSupplier.get()
                .filter(t->t.getAreaType() == Area.TYPE_VALUE_COUNTRY)
                .findFirst()
                .orElse(null);
        //ALL(国家)
        if(userRegion != null){
            return true;
        }

        // 以下按选择等级判断
        long areaId = area.getId().longValue();
        boolean checkResult = true;
        //省
        switch (areaLevel){
            case 0://省
                userRegion = streamSupplier.get()
                        .filter(t->t.getAreaType() == Area.TYPE_VALUE_PROVINCE && t.getProvinceId() == areaId)
                        .findFirst()
                        .orElse(null);
                if(userRegion != null){
                    checkResult = true;
                    break;
                }
                // 1.上级
                // 国家，上面代码已判断
                // 2.下级
                // 2.1.是否有下属的市
                List<Long> cities = streamSupplier.get()
                        .filter(t->t.getAreaType() == Area.TYPE_VALUE_CITY && t.getProvinceId() == areaId)
                        .map(t->t.getCityId())
                        .distinct()
                        .collect(Collectors.toList());
                if(!ObjectUtils.isEmpty(cities)){
                    if(searchModel != null){
                        searchModel.setCityList(cities);
                    }
                }
                //2.2.是否有下属的区县
                List<Long> areas = streamSupplier.get()
                        .filter(t -> t.getAreaType() == Area.TYPE_VALUE_COUNTY && t.getProvinceId() == areaId)
                        .map(t -> t.getAreaId())
                        .distinct()
                        .collect(Collectors.toList());
                if (!ObjectUtils.isEmpty(areas)) {
                    if (searchModel != null) {
                        searchModel.setAreaList(areas);
                    }
                }

                checkResult = (searchModel.getCityList() != null || searchModel.getAreaList() != null);
                break;
            case 1://市
                userRegion = streamSupplier.get()
                        .filter(t->t.getAreaType() == Area.TYPE_VALUE_CITY && t.getCityId() == areaId)
                        .findFirst()
                        .orElse(null);
                if(userRegion != null){
                    checkResult = true;
                    break;
                }
                // 1.上级
                // 1.1.省
                Area province = area.getParent();
                if(province == null || province.getId() == null || province.getId() <= 0){
                    checkResult = false;
                    break;
                }
                userRegion = streamSupplier.get()
                        .filter(t->t.getAreaType() == Area.TYPE_VALUE_PROVINCE && t.getProvinceId() == province.getId().longValue())
                        .findFirst()
                        .orElse(null);
                if(userRegion != null){
                    checkResult = true;
                    break;
                }

                // 2.下级
                // 2.1.区县
                List<Long> areas1 = streamSupplier.get()
                        .filter(t->t.getAreaType() == Area.TYPE_VALUE_COUNTY && t.getCityId() == areaId)
                        .map(t->t.getAreaId())
                        .distinct()
                        .collect(Collectors.toList());
                if(ObjectUtils.isEmpty(areas1)){
                    checkResult = false;
                    break;
                }
                if(searchModel != null){
                    searchModel.setAreaList(areas1);
                }
                checkResult = true;
                break;
            case 2://区
                checkResult = true;
                break;
                /*
                userRegion = streamSupplier.get().filter(t->t.getAreaType() == 4 && t.getAreaId() == areaId).findFirst().orElse(null);
                if(userRegion != null){
                    checkResult = true;
                    break;
                }
                // 1.上级
                // 1.1.市
                Area city3 = area.getParent();
                if(city3 == null || city3.getId() == null || city3.getId() <=0){
                    checkResult = false;
                    break;
                }
                userRegion = streamSupplier.get().filter(t->t.getAreaType() == 3 && t.getCityId() == city3.getId().longValue()).findFirst().orElse(null);
                if(userRegion != null){
                    checkResult = true;
                    break;
                }
                //省
                Area province3 = city3.getParent();
                if(province3 == null || province3.getId() == null || province3.getId() <=0){
                    checkResult = false;
                    break;
                }
                userRegion = streamSupplier.get().filter(t->t.getAreaType() == 2 && t.getProvinceId() == province3.getId().longValue()).findFirst().orElse(null);
                checkResult = userRegion != null;
                //2.下级
                // 无下级
                break;
                */
            case 3://街道
                checkResult =  true;
                break;
            default:
                checkResult =  false;
                break;
        }
        return checkResult;
    }

    /**
     * 所有数据列表
     *
     * @return
     */
    @Override
    public List<UserRegion> findAllList() {
        List<UserRegion> userRegionList = Lists.newArrayList();
        int pageNo = 1;
        Page<UserRegion> userRegionPage = new Page<UserRegion>();
        userRegionPage.setPageNo(pageNo);
        userRegionPage.setPageSize(500);
        userRegionList.addAll(dao.findAllList(userRegionPage));
        while(pageNo < userRegionPage.getPageCount()) {
           pageNo++;
           userRegionPage.setPageNo(pageNo);
           userRegionList.addAll(dao.findAllList(userRegionPage));
        }
       return userRegionList;
    }
}
