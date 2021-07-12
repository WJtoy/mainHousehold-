package com.wolfking.jeesite.ms.tmall.md.service;

import com.google.common.collect.Lists;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.entity.b2bcenter.md.B2BProductMapping;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.service.LongIDBaseService;
import com.wolfking.jeesite.common.service.LongIDCrudService;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.md.entity.Product;
import com.wolfking.jeesite.modules.md.service.ProductService;
import com.wolfking.jeesite.ms.b2bcenter.md.service.B2BProductMappingService;
import com.wolfking.jeesite.ms.tmall.md.adapter.B2BProductModel;
import com.wolfking.jeesite.ms.tmall.md.dao.B2bProductMapDao;
import com.wolfking.jeesite.ms.tmall.md.entity.B2bProductMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 对接系统店铺与工单系统产品关联
 *
 * @author Ryan
 * @date 2018/05/04
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class B2bProductMapService extends LongIDBaseService {

//    @Autowired
//    private ProductService productService;
//
//    @Autowired
//    private RedisUtils redisUtils;

    @Autowired
    private B2BProductMappingService productMappingService;

//    @Override
//    public void save(B2bProductMap entity) {
//        List<B2BProductModel> list = entity.getList();
//        list = list.stream().filter(t -> StringUtils.isNotBlank(t.getCustomerCategoryId())).collect(Collectors.toList());
//
//        //先删除店铺的记录
//        deleteByShop(entity.getDataSource(), entity.getShopId());
//
//        for (B2BProductModel model : list) {
//            B2bProductMap b2bProductMap = new B2bProductMap();
//            b2bProductMap.setDataSource(entity.getDataSource());
//            b2bProductMap.setCustomerId(entity.getCustomerId());
//            b2bProductMap.setShopId(entity.getShopId());
//            b2bProductMap.setCustomerCategoryId(model.getCustomerCategoryId());
//            b2bProductMap.setProduct(new Product(model.getProductId()));
//            b2bProductMap.setRemarks(entity.getRemarks());
//            super.save(b2bProductMap);
//        }
//        String key = String.format(RedisConstant.B2B_CUSTOMER_CATEGORY_TO_PRODUCT_MAP, entity.getDataSource(), entity.getShopId());
//        redisUtils.setEX(RedisConstant.RedisDBType.REDIS_B2B_DB, key, list, 0L);
//    }

    /**
     * 按照店铺删除产品
     *
     * @param dataSource
     * @param shopId
     */
//    public void deleteByShop(int dataSource, String shopId) {
//        dao.deleteByShop(dataSource, shopId);
//        String key = String.format(RedisConstant.B2B_CUSTOMER_CATEGORY_TO_PRODUCT_MAP, dataSource, shopId);
//        redisUtils.remove(RedisConstant.RedisDBType.REDIS_B2B_DB, key);
//    }
//
//    @Override
//    public void delete(B2bProductMap entity) {
//        super.delete(entity);
//        String key = String.format(RedisConstant.B2B_CUSTOMER_CATEGORY_TO_PRODUCT_MAP, entity.getDataSource(), entity.getShopId());
//        redisUtils.remove(RedisConstant.RedisDBType.REDIS_B2B_DB, key);
//    }

    /**
     * 按店铺获得关联产品id
     *
     * @param dataSource         数据源
     * @param shopId             店铺id
     * @param customerCategoryId 产品类目(叶子)
     */
    public List<Long> getProductIdByShopId(int dataSource, String shopId, String customerCategoryId) {
        List<B2BProductModel> list = getProductModelListByShop(dataSource, shopId);
        if (list == null || list.size() == 0) {
            return Lists.newArrayList();
        }
        return list.stream().filter(t -> t.getCustomerCategoryId().equalsIgnoreCase(customerCategoryId)).map(t -> t.getProductId()).collect(Collectors.toList());
    }

//    private List<B2BProductModel> loadDBTORedis(int dataSource, String shopId) {
//        String key = String.format(RedisConstant.B2B_CUSTOMER_CATEGORY_TO_PRODUCT_MAP, dataSource, shopId);
//        List<B2BProductModel> list = dao.getProductModelListByShop(dataSource, shopId);
//        for (B2BProductModel model : list) {
//            Product product = productService.getProductByIdFromCache(model.getProductId());
//            model.setProductName(product.getName());
//        }
//        redisUtils.setEX(RedisConstant.RedisDBType.REDIS_B2B_DB, key, list, 0L);
//        return list;
//    }

    /**
     * 获取店铺下产品对应列表
     *
     * @param dataSource
     * @param shopId
     * @return
     */
//    public List<B2BProductModel> getProductModelListByShop(int dataSource, String shopId) {
//
//        String key = String.format(RedisConstant.B2B_CUSTOMER_CATEGORY_TO_PRODUCT_MAP, dataSource, shopId);
//
//        List<B2BProductModel> list = redisUtils.getList(RedisConstant.RedisDBType.REDIS_B2B_DB, key, B2BProductModel[].class);
//        if (list == null) {
//
//            return loadDBTORedis(dataSource, shopId);
//        }
//        return list;
//    }
    public List<B2BProductModel> getProductModelListByShop(int dataSource, String shopId) {
        List<B2BProductModel> result = Lists.newArrayList();
        List<B2BProductMapping> list = productMappingService.getListByDataSource(B2BDataSourceEnum.valueOf(dataSource));
        if (list != null && list.size() > 0) {
            B2BProductModel productModel = null;
            for (B2BProductMapping item : list) {
//                if (item.getShopId().equals(shopId)) {
                productModel = new B2BProductModel();
                productModel.setProductId(item.getProductId());
                productModel.setProductName(item.getProductName());
                productModel.setCustomerCategoryId(item.getCustomerCategoryId());
                result.add(productModel);
//                }
            }
        }
        return result;
    }


    /**
     * 更新店铺的ID
     *
     * @param dataSource
     * @param oldShopId
     * @param newShopId
     */
//    public void updateShopId(int dataSource, String oldShopId, String newShopId) {
//        dao.updateShopId(dataSource, oldShopId, newShopId);
//
//        String oldkey = String.format(RedisConstant.B2B_CUSTOMER_CATEGORY_TO_PRODUCT_MAP, dataSource, oldShopId);
//        String newkey = String.format(RedisConstant.B2B_CUSTOMER_CATEGORY_TO_PRODUCT_MAP, dataSource, newShopId);
//
//        redisUtils.renameNX(RedisConstant.RedisDBType.REDIS_B2B_DB, oldkey, newkey);
//
//    }

}
