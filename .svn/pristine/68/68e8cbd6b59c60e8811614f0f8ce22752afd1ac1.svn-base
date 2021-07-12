package com.wolfking.jeesite.ms.tmall.md.service;

import com.google.common.collect.Lists;
import com.kkl.kklplus.entity.b2bcenter.md.B2BCustomerMapping;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.service.LongIDBaseService;
import com.wolfking.jeesite.ms.b2bcenter.md.service.B2BCustomerMappingService;
import com.wolfking.jeesite.ms.tmall.md.entity.B2bCustomerMap;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 对接系统店铺与厂商关联
 *
 * @author Ryan
 * @date 2018/05/04
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Slf4j
public class B2bCustomerMapService extends LongIDBaseService {

//    @Autowired
//    private RedisUtils redisUtils;
//
//    @Autowired
//    private B2bProductMapService b2bProductMapService;

    @Autowired
    private B2BCustomerMappingService customerMappingService;

    @Autowired
    private MapperFacade mapperFacade;


//    @Override
//    public void save(B2bCustomerMap entity) {
//        if (entity.getId() != null && entity.getId() > 0) {
//            //判断是否修改了店铺ID，如果修改了店铺的ID，则需要更新绑定产品的信息已经缓存
//
//            List<B2bCustomerMap> list = getAllShopList(entity.getDataSource());
//            B2bCustomerMap b2bCustomerMap = list.stream().filter(t -> t.getId() == entity.getId()).findFirst().orElse(null);
//
//            if (b2bCustomerMap != null && !b2bCustomerMap.getShopId().equalsIgnoreCase(entity.getShopId())) {
//                //如果是修改了店铺ID 更新店铺绑定产品的信息
//                //删除原来店铺->客户的缓存
//                String key = String.format(RedisConstant.B2B_SHOPID_TO_CUSTOMER_MAP, entity.getDataSource());
//                redisUtils.hdel(RedisConstant.RedisDBType.REDIS_B2B_DB, key, b2bCustomerMap.getShopId());
//
//                b2bProductMapService.updateShopId(entity.getDataSource(), b2bCustomerMap.getShopId(), entity.getShopId());
//
//            }
//        }
//        super.save(entity);
//
//        //更新客户->店铺的缓存
//        String key = String.format(RedisConstant.B2B_CUSTOMER_TO_SHOPID_LIST, entity.getDataSource(), entity.getCustomerId());
//        redisUtils.zSetEX(RedisConstant.RedisDBType.REDIS_B2B_DB, key, entity, entity.getId(), -1);
//
//        //更新店铺->客户的缓存
//        String key1 = String.format(RedisConstant.B2B_SHOPID_TO_CUSTOMER_MAP, entity.getDataSource());
//        redisUtils.hmSet(RedisConstant.RedisDBType.REDIS_B2B_DB, key1, entity.getShopId(), entity.getCustomerId(), 0L);
//
//        //更新所有店铺
//        String key2 = String.format(RedisConstant.B2B_SHOP_ALL_DATASOURCR, entity.getDataSource());
//        if (redisUtils.exists(RedisConstant.RedisDBType.REDIS_B2B_DB, key2)) {
//            redisUtils.zSetEX(RedisConstant.RedisDBType.REDIS_B2B_DB, key2, entity, entity.getId(), -1);
//        }
//    }

//    @Override
//    public void delete(B2bCustomerMap entity) {
//        super.delete(entity);
//        //删除客户->店铺的缓存
//        String key = String.format(RedisConstant.B2B_CUSTOMER_TO_SHOPID_LIST, entity.getDataSource(), entity.getCustomerId());
//        redisUtils.zRemRangeByScore(RedisConstant.RedisDBType.REDIS_B2B_DB, key, entity.getId(), entity.getId());
//
//        //删除店铺->客户的缓存
//        String key1 = String.format(RedisConstant.B2B_SHOPID_TO_CUSTOMER_MAP, entity.getDataSource());
//        redisUtils.hdel(RedisConstant.RedisDBType.REDIS_B2B_DB, key1, entity.getShopId());
//
//        //删除所有店铺
//        String key2 = String.format(RedisConstant.B2B_SHOP_ALL_DATASOURCR, entity.getDataSource());
//        if (redisUtils.zCount(RedisConstant.RedisDBType.REDIS_B2B_DB, key2, entity.getId(), entity.getId()) > 0) {
//            redisUtils.zRemRangeByScore(RedisConstant.RedisDBType.REDIS_B2B_DB, key2, entity.getId(), entity.getId());
//        }
//        //删除该店铺绑定的产品
//        b2bProductMapService.deleteByShop(entity.getDataSource(), entity.getShopId());
//
//    }

    /**
     * 按厂商获得所有关联店铺(可能1:n)
     */
//    public List<B2bCustomerMap> getShopListByCustomer(int dataSource, Long customerId) {
//        String key = String.format(RedisConstant.B2B_CUSTOMER_TO_SHOPID_LIST, dataSource, customerId);
//        List<B2bCustomerMap> list = redisUtils.zRange(RedisConstant.RedisDBType.REDIS_B2B_DB, key, 0, -1, B2bCustomerMap.class);
//        if (list == null || list.size() == 0) {
//            list = dao.getShopListByCustomer(dataSource, customerId);
//            for (B2bCustomerMap b2bCustomerMap : list) {
//                redisUtils.zAdd(RedisConstant.RedisDBType.REDIS_B2B_DB, key, b2bCustomerMap, b2bCustomerMap.getId(), -1);
//            }
//            return list;
//        }
//        return list;
//
//    }
    public List<B2bCustomerMap> getShopListByCustomer(int dataSource, Long customerId) {
        List<B2bCustomerMap> result = Lists.newArrayList();
        if (customerId != null) {
            List<B2BCustomerMapping> list = customerMappingService.getListByDataSource(B2BDataSourceEnum.valueOf(dataSource));
            if (list != null && list.size() > 0) {
                B2bCustomerMap customerMap = null;
                for (B2BCustomerMapping item : list) {
                    if (item.getCustomerId().equals(customerId)) {
                        customerMap = mapperFacade.map(item, B2bCustomerMap.class);
                        result.add(customerMap);
                    }
                }
            }
        }
        return result;

    }

    public List<B2bCustomerMap> getShopListByCustomerNew(int dataSource, Long customerId) {
        List<B2bCustomerMap> result = Lists.newArrayList();
        if (customerId != null) {
            B2BDataSourceEnum dataSourceEnum = dataSource == 1 ? B2BDataSourceEnum.KKL : B2BDataSourceEnum.valueOf(dataSource);
            List<B2BCustomerMapping> list = customerMappingService.getListByDataSource(dataSourceEnum);
            if (list != null && list.size() > 0) {
                B2bCustomerMap customerMap = null;
                for (B2BCustomerMapping item : list) {
                    if (item.getCustomerId().equals(customerId)) {
                        customerMap = mapperFacade.map(item, B2bCustomerMap.class);
                        result.add(customerMap);
                    }
                }
            }
        }
        return result;

    }

    /**
     * 因为缓存都是缓存每一个客户的店铺信息
     * 所以取所有客户的店铺时直接是从数据库读取
     * 获得所有店铺列表
     *
     * @param dataSource
     * @return
     */
//    public List<B2bCustomerMap> getAllShopList(int dataSource) {
//        String key2 = String.format(RedisConstant.B2B_SHOP_ALL_DATASOURCR, dataSource);
//        List<B2bCustomerMap> list = redisUtils.zRange(RedisConstant.RedisDBType.REDIS_B2B_DB, key2, 0, -1, B2bCustomerMap.class);
//        if (list == null || list.size() == 0) {
//            list = dao.getShopListByCustomer(dataSource, null);
//            if (list == null || list.size() == 0) {
//                return null;
//            }
//            Set<RedisCommands.Tuple> set = list.stream().map(t -> new RedisTuple(redisUtils.gsonRedisSerializer.serialize(t), t.getId().doubleValue())).collect(Collectors.toSet());
//            redisUtils.zAdd(RedisConstant.RedisDBType.REDIS_B2B_DB, key2, set, -1);
//        }
//        return list;
//    }
    public List<B2bCustomerMap> getAllShopList(int dataSource) {
        List<B2bCustomerMap> result = Lists.newArrayList();
        List<B2BCustomerMapping> list = customerMappingService.getListByDataSource(B2BDataSourceEnum.valueOf(dataSource));
        if (list != null && list.size() > 0) {
            B2bCustomerMap customerMap = null;
            for (B2BCustomerMapping item : list) {
                customerMap = mapperFacade.map(item, B2bCustomerMap.class);
                result.add(customerMap);
            }
        }
        return result;
    }

    /**
     * 获得店铺名
     *
     * @param dataSource 数据来源
     * @param shopId     店铺id
     */
    public B2bCustomerMap getShopInfo(int dataSource, String shopId) {
        List<B2bCustomerMap> list = getAllShopList(dataSource);
        if (list != null && list.size() > 0) {
            return list.stream().filter(t -> t.getShopId().equalsIgnoreCase(shopId)).findFirst().orElse(null);
        }
        return null;
    }

    /**
     * 获得店铺名
     *
     * @param dataSource 数据来源
     * @param customerId 客户id
     * @param shopId     店铺id
     */
    public String getShopName(int dataSource, Long customerId, String shopId) {
        List<B2bCustomerMap> list = getShopListByCustomer(dataSource, customerId);
        if (list != null && list.size() > 0) {
            B2bCustomerMap b2bCustomerMap = list.stream().filter(t -> t.getShopId().equalsIgnoreCase(shopId)).findFirst().orElse(null);
            if (b2bCustomerMap == null) {
                return "";
            } else {
                return b2bCustomerMap.getShopName();
            }
        }
        return "";
    }


    /**
     * 按商铺id获得厂商id(1:1)
     */
//    public Long getCustomerIdByShopId(int dataSource, String shopId) {
//        Long customerId = null;
//        String key = String.format(RedisConstant.B2B_SHOPID_TO_CUSTOMER_MAP, dataSource);
//        if (redisUtils.exists(RedisConstant.RedisDBType.REDIS_B2B_DB, key)) {
//            customerId = redisUtils.hGet(RedisConstant.RedisDBType.REDIS_B2B_DB, key, shopId, Long.class);
//            if (customerId != null && customerId > 0) {
//                return customerId;
//            }
//        }
//        //from db
//        customerId = dao.getCustomerIdByShopId(dataSource, shopId);
//        try {
//            if (customerId != null) {
//                redisUtils.hmSet(RedisConstant.RedisDBType.REDIS_B2B_DB, key, shopId, customerId, 0L);
//            }
//        } catch (Exception e) {
//            LogUtils.saveLog("按商铺读取厂商id", "B2bCustomerMapService.getCustomerIdByShopId", String.format("datasource:%s,shopId:%s", dataSource, shopId), e, null);
//        }
//        return customerId;
//    }
    public Long getCustomerIdByShopId(int dataSource, String shopId) {
        Long customerId = null;
        B2BCustomerMapping customerMapping = null;
        if (StringUtils.isNotBlank(shopId)) {
            List<B2BCustomerMapping> list = customerMappingService.getListByDataSource(B2BDataSourceEnum.valueOf(dataSource));
            if (list != null && list.size() > 0) {
                for (B2BCustomerMapping item : list) {
                    if (item.getShopId().equals(shopId.trim())) {
                        customerMapping = item;
                        break;
                    }
                }
            }
        }
        customerId = customerMapping != null ? customerMapping.getCustomerId() : null;
        return customerId;
    }

//    public boolean checkShopId(Long id, Integer dataSoruce, String shopId) {
//        return dao.getByShopId(id, dataSoruce, shopId) != null;
//    }
}
