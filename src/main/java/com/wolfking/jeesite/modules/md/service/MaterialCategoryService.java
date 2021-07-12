package com.wolfking.jeesite.modules.md.service;

import com.google.common.collect.Lists;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.service.LongIDCrudService;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.wolfking.jeesite.modules.md.dao.MaterialCategoryDao;
import com.wolfking.jeesite.modules.md.entity.MaterialCategory;
import com.wolfking.jeesite.ms.providermd.service.MSMaterialCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Function;


@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class MaterialCategoryService extends LongIDCrudService<MaterialCategoryDao,MaterialCategory> {

    /*
    @Autowired
    private RedisUtils redisUtils;
    */

    @Autowired
    private MSMaterialCategoryService msMaterialCategoryService;

    @Override
    public MaterialCategory get(long id){
         return msMaterialCategoryService.getById(id);
    }


    @Override
    public Page<MaterialCategory> findPage(Page<MaterialCategory> page, MaterialCategory materialCategory){
        return msMaterialCategoryService.findList(page,materialCategory);
    }

    @Override
    @Transactional()
    public void save(MaterialCategory materialCategory){
        boolean isNew = materialCategory.getIsNewRecord();
        //super.save(materialCategory);  // mark on 2020-1-8
        //调用微服务 2019-9-9
        MSErrorCode msErrorCode = msMaterialCategoryService.save(materialCategory,isNew);
        if(msErrorCode.getCode()>0){
            throw new RuntimeException("保存配件类别失败.失败原因:" + msErrorCode.getMsg());
        }

        /*
        // mark on 2020-1-8
        if(redisUtils.exists(RedisConstant.RedisDBType.REDIS_MD_DB,RedisConstant.MD_MATERIAL_CATEGORY_ALL)){
            redisUtils.zSetEX(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_MATERIAL_CATEGORY_ALL, materialCategory, materialCategory.getId(), 0);
        }
        */
    }

    @Override
    @Transactional()
    public void delete(MaterialCategory materialCategory){
        //super.delete(materialCategory);  // mark on 2020-1-8
        //调用微服务 2019-9-9
        MSErrorCode msErrorCode = msMaterialCategoryService.delete(materialCategory);
        if(msErrorCode.getCode()>0){
            throw new RuntimeException("删除配件类别失败.失败原因:" + msErrorCode.getMsg());
        }

        /*
        // mark on 2020-1-8
        if(redisUtils.zCount(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_MATERIAL_CATEGORY_ALL, materialCategory.getId(), materialCategory.getId()) > 0){
            redisUtils.zRemRangeByScore(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_MATERIAL_CATEGORY_ALL, materialCategory.getId(), materialCategory.getId());
        }
        */
    }



    /**
     * 检测数据是否已经存在
     * @param  name
     * @return Long
     */
    /*
    // mark on 2020-1-8
    public Long checkIsExist(String name){
        return dao.checkIsExist(name);
    }
    */

    /****************************************************************************
     * redis操作
     ****************************************************************************/

    /**从数据库读取数据到缓存中
     * @return
     */
    /*public List<MaterialCategory> loadDataFromDB2Cache(){
        //List<MaterialCategory> list = super.findAllList();
        List<MaterialCategory> list = msMaterialCategoryService.findAllList();
        if(list !=null && list.size()>0){
            Set<RedisCommands.Tuple> set=list.stream().map(t->new RedisTuple(redisUtils.gsonRedisSerializer.serialize(t),t.getId().doubleValue())).collect(Collectors.toSet());
            redisUtils.zAdd(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_MATERIAL_CATEGORY_ALL,set,-1);
        }
        return list;
    }*/

    /**
     * 加载所有配类别，当缓存未命中则从数据库装载至缓存
     * @return
     */
    @Override
    public List<MaterialCategory> findAllList(){
        /*boolean isExistsCache = redisUtils.exists(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_MATERIAL_CATEGORY_ALL);
        if (!isExistsCache){
             return loadDataFromDB2Cache();
        }
        List<MaterialCategory> materialCategoryList = redisUtils.zRange(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_MATERIAL_CATEGORY_ALL,0,-1, MaterialCategory.class);
        return materialCategoryList;*/
        //直接调用微服务
        List<MaterialCategory> list = msMaterialCategoryService.findAllList();
        if(list!=null && list.size()>0){
            return list;
        }
        return Lists.newArrayList();
    }

    /**
     * 根据id获取配件类别
     * @return  id
     */
    /*public MaterialCategory getFromCache(long id) {
        boolean isExistsCache = redisUtils.exists(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_MATERIAL_CATEGORY_ALL);
        if (!isExistsCache){
            loadDataFromDB2Cache();
        }
        try {
            MaterialCategory materialCategory  = (MaterialCategory) redisUtils.zRangeOneByScore(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_MATERIAL_CATEGORY_ALL, id, id, MaterialCategory.class);
            if(materialCategory==null){
                materialCategory = super.get(id);
                if(materialCategory!=null){
                    redisUtils.zSetEX(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_MATERIAL_CATEGORY_ALL, materialCategory, materialCategory.getId(), 0);
                }
            }
            return materialCategory;
        }catch (Exception e){
            return null;
        }
    }*/

    /**
     * 根据配件类别名称获取配件类别id，最多一条，用于判断名称是否存在于数据库中
     * @param materialCategory
     * @return
     */
    public boolean isExistName(MaterialCategory materialCategory){
        return isExistsMaterialCategoryProperties(materialCategory,msMaterialCategoryService::getIdByName);
    }

    private Boolean isExistsMaterialCategoryProperties(MaterialCategory materialCategory,Function<String,Long> fun){
        Long id = fun.apply(materialCategory.getName());
        if (materialCategory.getId()==null) {
            if (id == null) {
                return false;
            } else {
                return true;
            }
        } else {
            if (id == null) {
                return false;
            } else if (id.equals(materialCategory.getId())){
                return false;
            } else {
                return true;
            }
        }
    }
}
