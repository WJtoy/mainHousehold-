package com.wolfking.jeesite.modules.md.service;

import com.wolfking.jeesite.common.config.redis.GsonRedisSerializer;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.service.LongIDCrudService;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.wolfking.jeesite.modules.md.dao.CustomerProductModelDao;
import com.wolfking.jeesite.modules.md.entity.CustomerProductModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;



/**
 * 客户产品型号Service
 */
@Service
@Slf4j
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class CustomerProductModelService extends LongIDCrudService<CustomerProductModelDao, CustomerProductModel> {

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private ProductService productService;

    @Autowired
    private CustomerProductModelDao customerProductModelDao;

    @Resource(name = "gsonRedisSerializer")
    public GsonRedisSerializer gsonRedisSerializer;

    @Override
    @Transactional()
    public void save(CustomerProductModel entity) {
        super.save(entity);
        List<CustomerProductModel> list = dao.getListByProductId(entity.getCustomer().getId(),entity.getProduct().getId());
        list.forEach(t->t.setProductId(t.getProduct().getId()));
        String key =String.format(RedisConstant.MD_CUSTOMER_CUSTOMERPRODUCTMODEL,entity.getCustomer().getId());
        //数据加载到缓存中
        redisUtils.hmSet(RedisConstant.RedisDBType.REDIS_MD_DB,key,entity.getProduct().getId().toString(),list,-1);
    }


    @Override
    @Transactional()
    public void delete(CustomerProductModel entity) {
        super.delete(entity);
        String key = String.format(RedisConstant.MD_CUSTOMER_CUSTOMERPRODUCTMODEL,entity.getCustomer().getId());
        List<CustomerProductModel> list = getListByProductId(entity.getCustomer().getId(),entity.getProduct().getId());
        redisUtils.hdel(RedisConstant.RedisDBType.REDIS_MD_DB,key,entity.getProduct().getId().toString());
        if (list != null && list.size()>0){
            list.forEach(t->t.setProductId(t.getProduct().getId()));
            CustomerProductModel p = list.stream().filter(t->t.getId().equals(entity.getId())).findFirst().orElse(null);
            if (p != null) {
                list.remove(p);
            }
            if(list != null && list.size()>0){
                redisUtils.hmSet(RedisConstant.RedisDBType.REDIS_MD_DB,key,entity.getProduct().getId().toString(),list,-1);
            }
        }
    }

    /**
     * 检测某个产品的型号是否存在
     * @param customerProductModel
     * @return
     */
    public Long checkIsExist(CustomerProductModel customerProductModel){
        return customerProductModelDao.checkIsExist(customerProductModel);
    }

    /**
     * 根据客户和产品获取数据
     * @param customerId,productId
     * @return
     */
    public List<CustomerProductModel> getListByProductId(Long customerId,Long productId){
        String key = String.format(RedisConstant.MD_CUSTOMER_CUSTOMERPRODUCTMODEL,customerId);
        List<CustomerProductModel> list=redisUtils.hGetList(RedisConstant.RedisDBType.REDIS_MD_DB,key,productId.toString(),CustomerProductModel[].class);
        if(list!=null && list.size()>0){
            list.forEach(t->t.setProduct(productService.getProductByIdFromCache(t.getProductId())));
            return list;
        }else{
            list = dao.getListByProductId(customerId,productId);
            if(list!=null && list.size()>0){
                list.forEach(t->t.setProduct(productService.getProductByIdFromCache(t.getProduct().getId())));
                list.forEach(t->t.setProductId(t.getProduct().getId()));
                redisUtils.hmSet(RedisConstant.RedisDBType.REDIS_MD_DB,key,productId.toString(),list,-1);
            }
            return list;
      }
    }

}
