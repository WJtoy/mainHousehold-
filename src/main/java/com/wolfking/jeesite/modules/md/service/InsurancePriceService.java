package com.wolfking.jeesite.modules.md.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.config.redis.RedisTuple;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.service.LongIDCrudService;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.wolfking.jeesite.modules.md.dao.InsurancePriceDao;
import com.wolfking.jeesite.modules.md.entity.InsurancePrice;
import com.wolfking.jeesite.modules.md.entity.ProductCategory;
import com.wolfking.jeesite.ms.providermd.service.MSProductCategoryNewService;
import com.wolfking.jeesite.ms.providermd.service.MSProductCategoryService;
import com.wolfking.jeesite.ms.providermd.service.MSProductInsuranceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Jeff on 2017/4/24.
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class InsurancePriceService extends LongIDCrudService<InsurancePriceDao, InsurancePrice> {
    /*@Autowired
    private RedisUtils redisUtils;*/

    @SuppressWarnings("rawtypes")
    @Autowired
    public RedisTemplate redisTemplate;

//    @Autowired
//    private MSProductCategoryService msProductCategoryService;

    @Autowired
    private MSProductCategoryNewService msProductCategoryNewService;


    @Autowired
    private MSProductInsuranceService msProductInsuranceService;

    @Override
    public InsurancePrice get(long id) {
        //InsurancePrice insurancePrice = super.get(id);  // mark on 2019-8-29
        InsurancePrice insurancePrice = msProductInsuranceService.getById(id); // add on 2019-8-29 //ProductInsurance微服务
        if (insurancePrice != null) {
            // add on 2019-8-14 begin  //mark on 2020-3-16 begin
            // 调用ProductCategory微服务获取
//            ProductCategory productCategory = msProductCategoryService.getById(insurancePrice.getCategory().getId());
//            if (productCategory != null) {
//                insurancePrice.getCategory().setName(productCategory.getName());
//            }
            // add on 2019-8-14 end  //mark on 2020-3-16 end
            // add on 2020-3-16 begin
            //String strName = msProductCategoryService.getNameById(insurancePrice.getCategory().getId());  //mark on 2020-4-1
            String strName = msProductCategoryNewService.getFromCacheForMD(insurancePrice.getCategory().getId()); //add on 2020-4-1
            insurancePrice.getCategory().setName(strName);
            // add on 2020-3-16 end
        }
        return insurancePrice;
    }

    @Override
    public Page<InsurancePrice> findPage(Page<InsurancePrice> page, InsurancePrice entity) {
        // add on 2019-8-14
        entity.setPage(page);
//        List<InsurancePrice> insurancePriceList = dao.findList(entity);  //mark on 2019-8-29
        List<InsurancePrice> insurancePriceList = Lists.newArrayList();
        // add on 2019-8-29  begin //productInsurance微服务
        Page<InsurancePrice> insurancePricePage = msProductInsuranceService.findList(page, entity);
        if (insurancePricePage != null && insurancePricePage.getList() != null && !insurancePricePage.getList().isEmpty()) {
            insurancePriceList = insurancePricePage.getList();
        }
        // add on 2019-8-29  end

        if (insurancePriceList != null && !insurancePriceList.isEmpty()) {
            //List<ProductCategory> productCategoryList = msProductCategoryService.findAllList();  //mark on 2020-4-1
            List<ProductCategory> productCategoryList = msProductCategoryNewService.findAllListForMDWithEntity();  //add on 2020-4-1
            Map<Long,ProductCategory> productCategoryMap = Maps.newHashMap();
            if (productCategoryList != null && !productCategoryList.isEmpty()) {
                productCategoryMap = productCategoryList.stream().collect(Collectors.toMap(ProductCategory::getId, r->r));
            }
            Map<Long,ProductCategory> finalProductCategoryMap = productCategoryMap;
            insurancePriceList.stream().forEach(insurancePrice -> {
                ProductCategory productCategory = finalProductCategoryMap.get(insurancePrice.getCategory().getId());
                insurancePrice.getCategory().setName(productCategory==null?"":productCategory.getName());
            });
        }
//        page.setList(insurancePriceList);  // mark on 2019-8-29
//        return page;                       // mark on 2019-8-29
        insurancePricePage.setList(insurancePriceList);  // add on 2019-8-29
        return insurancePricePage;                       // add on 2019-8-29
    }

    @Override
    @Transactional()
    public void save(InsurancePrice insurancePrice){
        boolean isNew = insurancePrice.getIsNewRecord();  // add on 2019-8-29
        //super.save(insurancePrice); //mark on 2020-1-9
        // add on 2019-8-29 begin //productInsurance微服务
        MSErrorCode msErrorCode = msProductInsuranceService.save(insurancePrice, isNew);
        if (msErrorCode.getCode() >0) {
            throw new RuntimeException("调用保险价格微服务失败.失败原因:" + msErrorCode.getMsg());
        }
        // add on 2019-8-29 end
        //清除缓存
        //clearCache();
        //重新读取 如果没有缓存就 插入所有再加入缓存
        //findAllList();  //mark on 2020-1-9
    }

    @Override
    @Transactional()
    public void delete(InsurancePrice insurancePrice){
        //super.delete(insurancePrice);  //mark on
        // add on 2019-8-29 begin //productInsurance微服务
        MSErrorCode msErrorCode = msProductInsuranceService.delete(insurancePrice);
        if (msErrorCode.getCode() >0) {
            throw new RuntimeException("调用保险价格微服务失败.失败原因:" + msErrorCode.getMsg());
        }
        // add on 2019-8-29 end
        //清除缓存
        //remCach(insurancePrice.getCategory().getId()); //mark on 2020-1-9
    }

    @Override
    public List<InsurancePrice> findAllList() {
        /*
        //mark on 2020-1-9 begin
        List<InsurancePrice> list = redisUtils.zRange(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_PRODUCT_CATEGORY_INSURANCE,0,-1,InsurancePrice.class);
        if(list != null && list.size()>0){
            return list;
        }

//        list = super.findAllList();  // mark on 2019-8-29
        list = msProductInsuranceService.findAllList();  // add on 2019-8-29 //productInsurance微服务
        if(list != null && list.size()>0){
            // add on 2019-8-14 begin
            // 调用ProductCategory获取数据
            List<ProductCategory> productCategoryList = msProductCategoryService.findAllList();
            Map<Long,ProductCategory> productCategoryMap = Maps.newHashMap();
            if (productCategoryList != null && !productCategoryList.isEmpty()) {
                productCategoryMap = productCategoryList.stream().collect(Collectors.toMap(ProductCategory::getId, r->r));
            }
            Map<Long,ProductCategory> finalProductCategoryMap = productCategoryMap;
            list.stream().forEach(insurancePrice -> {
                ProductCategory productCategory = finalProductCategoryMap.get(insurancePrice.getCategory().getId());
                insurancePrice.getCategory().setName(productCategory==null?"":productCategory.getName());
            });
            // add on 2019-8-14 end
            List<InsurancePrice> finalList = list;
            Set<RedisZSetCommands.Tuple> sets = Sets.newHashSet();
            list.forEach(item ->{
                sets.add(new RedisTuple(redisUtils.gsonRedisSerializer.serialize(item),Double.valueOf(item.getCategory().getId().toString())));
            });
            redisUtils.zAdd(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_PRODUCT_CATEGORY_INSURANCE, sets,0l);

//            redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
//                connection.select(RedisConstant.RedisDBType.REDIS_MD_DB.ordinal());
//                for(int index = 0; index < finalList.size(); index++){
//                    InsurancePrice insurancePrice = finalList.get(index);
//                    connection.zAdd(RedisConstant.MD_PRODUCT_CATEGORY_INSURANCE.getBytes(StandardCharsets.UTF_8), insurancePrice.getId(), redisUtils.gsonRedisSerializer.serialize(insurancePrice));
//                }
//                return null;
//            });
            finalList.clear();
        }

        return list;
        //mark on 2020-1-9 end
        */

        List<InsurancePrice> list = msProductInsuranceService.findAllList();
        if(list != null && list.size()>0){
            // 调用ProductCategory获取数据
            //List<ProductCategory> productCategoryList = msProductCategoryService.findAllList();  //mark on 2020-4-1
            List<ProductCategory> productCategoryList = msProductCategoryNewService.findAllListForMDWithEntity();  //add on 2020-4-1
            Map<Long,ProductCategory> productCategoryMap = Maps.newHashMap();
            if (productCategoryList != null && !productCategoryList.isEmpty()) {
                productCategoryMap = productCategoryList.stream().collect(Collectors.toMap(ProductCategory::getId, r->r));
            }
            Map<Long,ProductCategory> finalProductCategoryMap = productCategoryMap;
            list.stream().forEach(insurancePrice -> {
                ProductCategory productCategory = finalProductCategoryMap.get(insurancePrice.getCategory().getId());
                insurancePrice.getCategory().setName(productCategory==null?"":productCategory.getName());
            });
        }

        return list;

    }

    /*
    // mark on 2020-1-9 begin
    //删除产品目录缓存
    public void clearCache(){
        redisUtils.remove(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_PRODUCT_CATEGORY_INSURANCE);
    }

    //删除单个zSet项
    public void remCach(Long categoryId){
        redisUtils.zRemRangeByScore(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_PRODUCT_CATEGORY_INSURANCE,categoryId,categoryId);
    }
    // mark on 2020-1-9 end
    */

    /**
     * 按产品类别id获得保险费
     * @return
     */
    /*
    // mark on 2020-1-9 begin
    public InsurancePrice getByCategory(Long categoryId) {
        InsurancePrice insurancePrice = null;
        if(redisUtils.exists(RedisConstant.RedisDBType.REDIS_MD_DB,RedisConstant.MD_PRODUCT_CATEGORY_INSURANCE)){
            insurancePrice = (InsurancePrice)redisUtils.zRangeOneByScore(RedisConstant.RedisDBType.REDIS_MD_DB,RedisConstant.MD_PRODUCT_CATEGORY_INSURANCE,categoryId,categoryId,InsurancePrice.class);
        }
        if(insurancePrice == null){
            insurancePrice = dao.getByCategory(categoryId);
            if(insurancePrice != null){
                // add on 2019-8-14 begin
                // 调用ProductCategory微服务获取
                ProductCategory productCategory = msProductCategoryService.getById(insurancePrice.getCategory().getId());
                if (productCategory != null) {
                    insurancePrice.getCategory().setName(productCategory.getName());
                }
                // ad on 2019-8-14 end
                redisUtils.zAdd(RedisConstant.RedisDBType.REDIS_MD_DB,RedisConstant.MD_PRODUCT_CATEGORY_INSURANCE,insurancePrice,categoryId,0l);
            }
        }
        return insurancePrice;
    }
    // mark on 2020-1-9 end
    */
    public Long getIdByCategoryId(Long productCategoryId){
        MSResponse<Long> msResponse = msProductInsuranceService.getIdByCategoryId(productCategoryId);
        if(MSResponse.isSuccess(msResponse)){
            return msResponse.getData();
        }else {
            return null;
        }
    }
}
