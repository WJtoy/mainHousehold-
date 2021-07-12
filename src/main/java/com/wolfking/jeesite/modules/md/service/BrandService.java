package com.wolfking.jeesite.modules.md.service;

import cn.hutool.core.lang.func.Func;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.config.redis.RedisTuple;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.service.LongIDCrudService;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.wolfking.jeesite.modules.md.dao.BrandDao;
import com.wolfking.jeesite.modules.md.entity.Brand;
import com.wolfking.jeesite.ms.providermd.service.MSBrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisCommands;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 品牌Service
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class BrandService extends LongIDCrudService<BrandDao, Brand> {

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private MSBrandService msBrandService;

    @Override
    public Brand get(long id) {
        // add on 2019-9-4
        return msBrandService.getById(id);
    }

    @Override
    public Page<Brand> findPage(Page<Brand> page, Brand entity) {
        // add on 2019-9-4
        return msBrandService.findList(page, entity);
    }

    @Override
    @Transactional()
    public void save(Brand brand){
        boolean isNew = brand.getIsNewRecord();  // add on 2019-9-4
        //super.save(brand);   //mark on 2020-1-4 保存到MD微服务
        // add on 2019-9-4 begin
        // brand微服务
        MSErrorCode msErrorCode = msBrandService.save(brand, isNew);
        if (msErrorCode.getCode() >0) {
            throw new RuntimeException("保存品牌失败.失败原因:"+msErrorCode.getMsg());
        }
        // add on 2019-9-4 end
        /*
        // mark on 2020-1-4 //MD微服务
        if (redisUtils.exists(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_BRAND_ALL)){
            redisUtils.zSetEX(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_BRAND_ALL, brand, brand.getId(), 0);
        }
         */
    }

    @Override
    @Transactional
    public void delete(Brand brand){
        // super.delete(brand);  // mark on 2020-1-4
        // add on 2019-9-4 begin
        // brand微服务
        MSErrorCode msErrorCode = msBrandService.delete(brand);
        if (msErrorCode.getCode() >0) {
            throw new RuntimeException("删除品牌失败.失败原因:"+msErrorCode.getMsg());
        }
        // add on 2019-9-4 end
        /*
        // mark on 2020-1-4
        if (redisUtils.zCount(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_BRAND_ALL, brand.getId(), brand.getId()) > 0) {
            redisUtils.zRemRangeByScore(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_BRAND_ALL, brand.getId(), brand.getId());
        }
        */
    }

    /****************************************************************************
     * redis操作
     ****************************************************************************/

    /**从数据库读取品牌信息至缓存
     * @return
     */
    /*
    // 已没有地方调用，方法废弃 //mark on 2020-1-4
    private List<Brand> loadDataFromDB2Cache(){
//        List<Brand> list = super.findAllList(); //mark on 2019-9-4
        List<Brand> list = msBrandService.findAllList(); //add on 2019-9-4
        if(list != null && list.size()>0) {
            Set<RedisCommands.Tuple> set=list.stream().map(t->new RedisTuple(redisUtils.gsonRedisSerializer.serialize(t),t.getId().doubleValue())).collect(Collectors.toSet());
            redisUtils.zAdd(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_BRAND_ALL,set,-1);
        }
        return list;
    }
     */


    /**
     * 加载所有品牌，当缓存未命中则从数据库装载至缓存
     * @return
     */
    @Override
    public List<Brand> findAllList(){
        /*
        // mark on 2020-1-4
        boolean isExistsCache = redisUtils.exists(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_BRAND_ALL);
        if (!isExistsCache){
            return loadDataFromDB2Cache();
        }
        List<Brand> brandList = redisUtils.zRange(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_BRAND_ALL,0,-1, Brand.class);
        return brandList;
        */
        return msBrandService.findAllList();
    }

    /**
     * 获得 品牌
     * @param id
     * @return
     */
    /*
    // mark on 2020-1-4 //只有com.wolfking.jeesite.test.md.BrandTest.getBrandFromCahe 这里调用，注释该方法代码
    public Brand getFromCache(long id) {
        boolean isExistsCache = redisUtils.exists(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_BRAND_ALL);
        if (!isExistsCache){
            loadDataFromDB2Cache();
        }
        try {
            return (Brand) redisUtils.zRangeOneByScore(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_BRAND_ALL, id, id, Brand.class);
        }catch (Exception e){
            return null;
        }
    }
     */

    /**
     * 根据编码获取品牌ID，最多一条，用于判断编码是否存在于数据库中
     * @param brand
     * @return
     */
    public Boolean isExistBrandCode(Brand brand){
        //return dao.getIdByCode(brand) != null; //mark on 2019-9-4
        return isExistsBrandProperites(brand, msBrandService::getIdByCode); //add on 2019-9-4
    }

    /**
     * 根据名称获取品牌ID，最多一条，用于判断名称是否存在于数据库中
     * @param brand
     * @return
     */
    public Boolean isExistBrandName(Brand brand){
//        return dao.getIdByName(brand) != null;  //mark on 2019-9-4
        return isExistsBrandProperites(brand, msBrandService::getIdByName); //add on 2019-9-4
    }

    private Boolean isExistsBrandProperites(Brand brand, Function<Brand, Long> fun) {
        //add on 2019-9-4
        Long id = fun.apply(brand);
        if (brand.getId()==null) {
            if (id == null) {
                return false;
            } else {
                return true;
            }
        } else {
            if (id == null) {
                return false;
            } else if (id.equals(brand.getId())){
                return false;
            } else {
                return true;
            }
        }
    }
}
