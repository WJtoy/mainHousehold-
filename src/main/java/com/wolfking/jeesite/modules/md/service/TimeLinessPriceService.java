package com.wolfking.jeesite.modules.md.service;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.config.redis.RedisTuple;
import com.wolfking.jeesite.common.service.LongIDCrudService;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.wolfking.jeesite.modules.md.dao.TimeLinessPriceDao;
import com.wolfking.jeesite.modules.md.entity.TimeLinessPrice;
import com.wolfking.jeesite.modules.md.entity.TimeLinessPrices;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.ms.providermd.service.MSProductTimeLinessService;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Jeff on 2017/4/24.
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class TimeLinessPriceService extends LongIDCrudService<TimeLinessPriceDao, TimeLinessPrice> {

    /*
    // mark on 2020-1-10
    @Autowired
    private RedisUtils redisUtils;

    @SuppressWarnings("rawtypes")
    @Autowired
    public RedisTemplate redisTemplate;
    */

    @Autowired
    private MSProductTimeLinessService msProductTimeLinessService;

    @Override
    public List<TimeLinessPrice> findAllList() {
        // add on 2019-9-2
        return msProductTimeLinessService.findAllList();
    }

    @Transactional(readOnly = false)
    public void save(TimeLinessPrices model){
        //deleteByCategoryId(model.getCategory().getId());
        //dao.deleteByCategoryId(model.getCategory().getId());   //mark on 2020-1-10
        // add on 2019-9-2 begin
        // ProductTimeLiness微服务
        MSErrorCode msErrorCode = msProductTimeLinessService.deleteByProductCategoryId(model.getCategory().getId());
        if (msErrorCode.getCode() >0) {
            throw new RuntimeException("根据产品类别删除时效补贴失败.失败原因:"+msErrorCode.getMsg());
        }
        // add on 2019-9-2 end

        List<TimeLinessPrice> list = model.getList();
        for (TimeLinessPrice entity:list) {
            entity.setCategory(model.getCategory());
            entity.preInsert();
            //super.save(entity);  //mark on 2020-1-10
        }
        // add on 2019-9-2 begin
        // ProductTimeLiness微服务
        MSErrorCode msErrorCodeForBatch = msProductTimeLinessService.batchInsert(list);
        if (msErrorCodeForBatch != null && msErrorCodeForBatch.getCode()>0) {
            throw new RuntimeException("批量保存时效补贴数据失败.失败原因:" + msErrorCodeForBatch.getMsg());
        }
        // add on 2019-9-2 end

        //缓存淘汰
        //remCache(model.getCategory().getId());   // mark on 2020-1-20
        //同步缓存
        //getTimeLinessPrices(model.getCategory().getId());
    }

    /*
    // mark on 2020-1-10
    @Override
    @Transactional(readOnly = false)
    public void delete(TimeLinessPrice entity){
        super.delete(entity);
        //清除缓存
        remCache(entity.getCategory().getId());
    }
    */

    /**
     * 按产品类别id获得列表
     * @param categoryId
     * @return
     */
    public List<TimeLinessPrice> getTimeLinessPrices(Long categoryId){
        /*
        // mark on 2020-1-10 begin
        String key = String.format(RedisConstant.MD_PRODUCT_CATEGORY_TIMELINESS,categoryId);
        List<TimeLinessPrice> prices = redisUtils.zRange(RedisConstant.RedisDBType.REDIS_MD_DB,key,0,-1,TimeLinessPrice.class);
        if(prices==null || prices.size() ==0){

            //prices = dao.getPrices(categoryId);  // mark on 2019-9-2
            prices = msProductTimeLinessService.getPrices(categoryId); // add on 2019-9-2  //ProductTimesLiness微服务

            //ProductCategory category = ProductUtils.getProductCategory(categoryId);
            if(prices !=null && prices.size()>0){
                Set<RedisZSetCommands.Tuple> sets = Sets.newHashSet();
                List<Dict> levels = MSDictUtils.getDictList(TimeLinessPrice.TIME_LINESS_LEVEL);
                if(levels != null && levels.size()>0){
                    TimeLinessPrice price;
                    for(int i=0,size=levels.size();i<size;i++){
                        final Dict level = levels.get(i);
                        price = prices.stream().filter(t->t.getTimeLinessLevel().getValue().equalsIgnoreCase(level.getValue())).findFirst().orElse(null);
                        if(price != null){
                            price.setTimeLinessLevel(level);
                            sets.add(new RedisTuple(redisUtils.gsonRedisSerializer.serialize(price), Double.valueOf(level.getValue())));
                        }
                    }
                }else {
                    prices.forEach(item -> {
                        sets.add(new RedisTuple(redisUtils.gsonRedisSerializer.serialize(item), Double.valueOf(item.getTimeLinessLevel().getValue())));
                    });
                }
                redisUtils.zAdd(RedisConstant.RedisDBType.REDIS_MD_DB, key, sets,0l);
            }
        }
        // mark on 2020-1-10 end
        */

        List<TimeLinessPrice> prices = msProductTimeLinessService.getPrices(categoryId);
        if(prices !=null && prices.size()>0){
            List<Dict> levels = MSDictUtils.getDictList(TimeLinessPrice.TIME_LINESS_LEVEL);
            if(levels != null && levels.size()>0){
                TimeLinessPrice price;
                for(int i=0,size=levels.size();i<size;i++){
                    final Dict level = levels.get(i);
                    price = prices.stream().filter(t->t.getTimeLinessLevel().getValue().equalsIgnoreCase(level.getValue())).findFirst().orElse(null);
                    if(price != null){
                        price.setTimeLinessLevel(level);
                    }
                }
            }
        }

        return prices;
    }

    /**
     * 按多个产品类别id获得列表
     * @param categoryIds 产品类别id列表
     * @return
     */
    public List<TimeLinessPrice> getTimeLinessPrices(String categoryIds){
        List<String> cids = Splitter.onPattern("[.|,]")
                .omitEmptyStrings()
                .splitToList(categoryIds);
        Set<Long> ids = cids.stream().map(t-> Long.valueOf(t)).distinct().filter(t->t>0).collect(Collectors.toSet());
        if(ids.size()>0){
            return getTimeLinessPrices(ids);
        }
        return null;
    }

    /**
     * 按多个产品类别id获得列表
     * @param categoryIds 产品类别id列表
     * @return
     */
    public List<TimeLinessPrice> getTimeLinessPrices(Set<Long> categoryIds){
        List<TimeLinessPrice> list = Lists.newArrayList();
        List<TimeLinessPrice> prices;
        Iterator<Long> iter = categoryIds.iterator();
        Long id;
        while (iter.hasNext())
        {
            id = iter.next();
            prices = getTimeLinessPrices(id);
            if(prices != null && prices.size() > 0){
                list.addAll(prices);
            }
        }
        return list;
    }

    //删除缓存
    /*
    // mark on 2020-1-10
    public void remCache(Long categoryId){
        redisUtils.remove(RedisConstant.RedisDBType.REDIS_MD_DB, String.format(RedisConstant.MD_PRODUCT_CATEGORY_TIMELINESS,categoryId));
    }
    */

    /**
     * 按产品分类删除
     * @param categoryId
     */
    public void deleteByCategoryId(Long categoryId){
        //dao.deleteByCategoryId(categoryId); //mark on 2020-1-10
        // add on 2019-9-2 begin
        // ProductTimeLiness微服务
        MSErrorCode msErrorCode = msProductTimeLinessService.deleteByProductCategoryId(categoryId);
        if (msErrorCode.getCode() >0) {
            throw new RuntimeException("根据产品类别删除时效补贴失败.失败原因:"+msErrorCode.getMsg());
        }
        // add on 2019-9-2 end
        //remCache(categoryId);  // mark on 2020-1-10
    }

}
