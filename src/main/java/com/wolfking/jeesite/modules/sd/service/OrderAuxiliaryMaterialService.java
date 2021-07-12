package com.wolfking.jeesite.modules.sd.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kkl.kklplus.entity.md.MDAuxiliaryMaterialCategory;
import com.kkl.kklplus.entity.md.MDAuxiliaryMaterialItem;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.exception.OrderException;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.md.entity.Product;
import com.wolfking.jeesite.modules.md.service.ProductService;
import com.wolfking.jeesite.modules.sd.dao.AuxiliaryMaterialDao;
import com.wolfking.jeesite.modules.sd.dao.AuxiliaryMaterialMasterDao;
import com.wolfking.jeesite.modules.sd.entity.AuxiliaryMaterial;
import com.wolfking.jeesite.modules.sd.entity.AuxiliaryMaterialMaster;
import com.wolfking.jeesite.ms.providermd.utils.AuxiliaryMaterialUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Service
@Slf4j
public class OrderAuxiliaryMaterialService {

    private AuxiliaryMaterialDao auxiliaryMaterialDao;
    private AuxiliaryMaterialMasterDao auxiliaryMaterialMasterDao;
    private ProductService productService;
    private RedisUtils redisUtils;

    //region 依赖注入

    @Resource
    public void setAuxiliaryMaterialDao(AuxiliaryMaterialDao auxiliaryMaterialDao) {
        this.auxiliaryMaterialDao = auxiliaryMaterialDao;
    }

    @Resource
    public void setAuxiliaryMaterialMasterDao(AuxiliaryMaterialMasterDao auxiliaryMaterialMasterDao) {
        this.auxiliaryMaterialMasterDao = auxiliaryMaterialMasterDao;
    }

    @Autowired
    public void setProductService(ProductService productService) {
        this.productService = productService;
    }

    @Autowired
    public void setRedisUtils(RedisUtils redisUtils) {
        this.redisUtils = redisUtils;
    }

    //endregion 依赖注入

    //region 私有方法

    //endregion 私有方法


    public AuxiliaryMaterial getAuxiliaryMaterial(Long id) {
        AuxiliaryMaterial entity = null;
        if (id != null) {
            entity = auxiliaryMaterialDao.get(id);
        }
        return entity;
    }

    public AuxiliaryMaterialMaster getAuxiliaryMaterialMaster(Long orderId, String quarter) {
        AuxiliaryMaterialMaster entity = null;
        if (orderId != null) {
            entity = auxiliaryMaterialMasterDao.getAuxiliaryMaterialMasterByOrderId(orderId, quarter);
        }
        return entity;
    }

    /**
     * 是否有附加费用(辅材)
     */
    public boolean hasAuxiliaryMaterials(Long orderId, String quarter) {
        boolean result = false;
        if (orderId != null && orderId > 0 && StringUtils.isNotBlank(quarter)) {
            AuxiliaryMaterialMaster master = getAuxiliaryMaterialMaster(orderId, quarter);
//            if (StringUtils.isBlank(master.getRemarks()) && master.getTotal() == 0 && master.getActualTotalCharge() == 0) {
            if (master != null) {
                result = true;
            }
        }
        return result;
    }


    /**
     * 获取产品的辅材与服务项目
     */
//    public Map<Product, List<B2BServiceFeeItem>> getProductAuxiliaryMaterials(Integer dataSourceId, List<Long> productIds) {
//        Map<Product, List<B2BServiceFeeItem>> map = Maps.newHashMap();
//        if (productIds != null && !productIds.isEmpty()) {
//            Map<Long, Product> productMap = productService.getProductMap(productIds);
//            List<Long> splitProductIds = Lists.newArrayList();
//            Product product;
//            Long productId;
//            for (Long id : productIds) {
//                product = productMap.get(id);
//                if (product != null) {
//                    if (product.getSetFlag() == 1) {
//                        final String[] setIds = product.getProductIds().split(",");
//                        for (String innerId : setIds) {
//                            productId = StringUtils.toLong(innerId);
//                            if (productId > 0) {
//                                splitProductIds.add(productId);
//                            }
//                        }
//                    } else {
//                        splitProductIds.add(id);
//                    }
//                }
//            }
//            Map<Long, List<B2BServiceFeeItem>> serviceFeeItemMap = B2BMDUtils.getB2BServiceItemAndCategoryMap(dataSourceId, splitProductIds);
//            if (!serviceFeeItemMap.isEmpty()) {
//                List<Long> returnProductIds = Lists.newArrayList(serviceFeeItemMap.keySet());
//                productMap = productService.getProductMap(returnProductIds);
//                List<B2BServiceFeeItem> items;
//                for (Long id : returnProductIds) {
//                    product = productMap.get(id);
//                    items = serviceFeeItemMap.get(id);
//                    if (product != null && items != null && !items.isEmpty()) {
//                        map.put(product, items);
//                    }
//                }
//            }
//        }
//        return map;
//    }
//
//    public Map<Product, List<B2BServiceFeeItem>> getProductAuxiliaryMaterialsNew(Integer dataSourceId, List<Long> productIds) {
//        Map<Product, List<B2BServiceFeeItem>> map = Maps.newHashMap();
//        if (productIds != null && !productIds.isEmpty()) {
//            Map<Long, List<B2BServiceFeeItem>> serviceFeeItemMap = B2BMDUtils.getB2BServiceItemAndCategoryMap(dataSourceId, productIds);
//            if (!serviceFeeItemMap.isEmpty()) {
//                List<Long> returnProductIds = Lists.newArrayList(serviceFeeItemMap.keySet());
//                Map<Long, Product> productMap = productService.getProductMap(returnProductIds);
//                List<B2BServiceFeeItem> items;
//                Product product;
//                for (Long id : returnProductIds) {
//                    product = productMap.get(id);
//                    items = serviceFeeItemMap.get(id);
//                    if (product != null && items != null && !items.isEmpty()) {
//                        map.put(product, items);
//                    }
//                }
//            }
//        }
//        return map;
//    }

    /**
     * 保存工单的辅材与服务项目
     * 注意：更新会覆盖原来的辅材与服务项目设定（数据库中已存但更新参数中没有的项目会被删除）
     */
    @Transactional()
    public void updateOrderAuxiliaryMaterials(AuxiliaryMaterialMaster param) {
        if (param.getOrderId() != null && param.getOrderId() > 0 && StringUtils.isNotBlank(param.getQuarter()) && param.getItems() != null) {
            long orderId = param.getOrderId();
            String quarter = param.getQuarter();
            List<AuxiliaryMaterial> materials = param.getItems();
            String lockKey = String.format(RedisConstant.SD_AUXILIARY_MATERIALS, orderId);
            Boolean locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockKey, 1, 30);
            if (!locked) {
                throw new OrderException("该单正在被其他人处理中，请稍候重试，或刷新页面。");
            }
            try {
                AuxiliaryMaterialMaster materialMaster = getAuxiliaryMaterialMaster(orderId, quarter);
                if (materialMaster != null) {
                    materialMaster.setQty(param.getQty());
                    materialMaster.setTotal(param.getTotal());
                    materialMaster.setActualTotalCharge(param.getActualTotalCharge());
                    materialMaster.setRemarks(param.getRemarks());
                    auxiliaryMaterialMasterDao.update(materialMaster);
                } else {
                    auxiliaryMaterialMasterDao.insert(param);
                }

                String key;
                Map<String, AuxiliaryMaterial> materialMap = Maps.newHashMap();
                if (!materials.isEmpty()) {
                    for (AuxiliaryMaterial item : materials) {
                        key = String.format("%d:%d", item.getProduct().getId(), item.getMaterial().getId());
                        materialMap.put(key, item);
                    }
                }
                List<AuxiliaryMaterial> materialsOfExisted = auxiliaryMaterialDao.getAuxiliaryMaterialsByOrderId(orderId, quarter, null);
                Map<String, AuxiliaryMaterial> materialMapForUpdate = Maps.newHashMap();
                List<AuxiliaryMaterial> materialsForDelete = Lists.newArrayList();
                if (materialsOfExisted != null && !materialsOfExisted.isEmpty()) {
                    for (AuxiliaryMaterial item : materialsOfExisted) {
                        key = String.format("%d:%d", item.getProduct().getId(), item.getMaterial().getId());
                        if (materialMap.containsKey(key)) {
                            materialMapForUpdate.put(key, item);
                        } else {
                            if (item.getDelFlag().intValue() == AuxiliaryMaterial.DEL_FLAG_NORMAL) {
                                materialsForDelete.add(item);
                            }
                        }
                    }
                }
                AuxiliaryMaterial material;
                for (AuxiliaryMaterial item : materials) {
                    key = String.format("%d:%d", item.getProduct().getId(), item.getMaterial().getId());
                    material = materialMapForUpdate.get(key);
                    if (material != null) {
                        material.setMaterial(item.getMaterial());
                        material.setQty(item.getQty());
                        material.setSubtotal(item.getSubtotal());
                        material.setUnit(item.getUnit());
                        material.setUpdateBy(item.getUpdateBy());
                        material.setUpdateDate(item.getUpdateDate());
                        auxiliaryMaterialDao.update(material);
                    } else {
                        material = item;
                        auxiliaryMaterialDao.insert(material);
                    }
                }
                for (AuxiliaryMaterial item : materialsForDelete) {
                    auxiliaryMaterialDao.delete(item);
                }

            } finally {
                if (lockKey != null) {
                    redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockKey);
                }
            }
        }
    }

//    /**
//     * 读取工单用到的辅材与服务项目
//     */
//    public AuxiliaryMaterialMaster getOrderAuxiliaryMaterials(Integer dataSourceId, Long orderId, String quarter) {
//        AuxiliaryMaterialMaster materialMaster = getAuxiliaryMaterialMaster(orderId, quarter);
//        if (materialMaster != null) {
//            List<AuxiliaryMaterial> materials = auxiliaryMaterialDao.getAuxiliaryMaterialsByOrderId(orderId, quarter, AuxiliaryMaterial.DEL_FLAG_NORMAL);
//            if (materials != null && !materials.isEmpty()) {
//                List<Long> productIds = materials.stream().map(i -> i.getProduct().getId()).distinct().collect(Collectors.toList());
//                Map<Long, B2BServiceFeeCategory> serviceFeeCategoryMap = B2BMDUtils.getB2BServiceFeeCategoryMap(dataSourceId);
//                Map<Long, B2BServiceFeeItem> serviceFeeItemMap = B2BMDUtils.getB2BServiceFeeItemMap(dataSourceId, productIds);
//                Map<Long, Product> productMap = productService.getProductMap(productIds);
//                for (AuxiliaryMaterial item : materials) {
//                    B2BServiceFeeItem feeItem = serviceFeeItemMap.get(item.getMaterial().getId());
//                    if (feeItem != null) {
////                        if (feeItem.getCustomPriceFlag() == B2BServiceFeeItem.CUSTOM_PRICE_FLAG_USER_DEFINED) {
////                            feeItem = B2BServiceFeeItem.copy(feeItem);
////                            feeItem.setCharge(item.getMaterial().getCharge());
////                        }
//                        item.setMaterial(feeItem);
//                    }
//                    B2BServiceFeeCategory feeCategory = serviceFeeCategoryMap.get(item.getCategory().getId());
//                    if (feeCategory != null) {
//                        item.setCategory(feeCategory);
//                    }
//                    Product product = productMap.get(item.getProduct().getId());
//                    if (product != null) {
//                        item.setProduct(product);
//                    }
//                }
//            }
//            materialMaster.setItems(materials);
//        }
//        return materialMaster;
//    }


    //region 辅材v2.0


    /**
     * 读取工单用到的辅材与服务项目
     */
    public AuxiliaryMaterialMaster getOrderAuxiliaryMaterialsV2(Long orderId, String quarter) {
        AuxiliaryMaterialMaster materialMaster = getAuxiliaryMaterialMaster(orderId, quarter);
        if (materialMaster != null && materialMaster.getFormType()==AuxiliaryMaterialMaster.FormTypeEnum.HAS_MATERIAL_ITEM.getValue()) {
            List<AuxiliaryMaterial> materials = auxiliaryMaterialDao.getAuxiliaryMaterialsByOrderId(orderId, quarter, AuxiliaryMaterial.DEL_FLAG_NORMAL);
            if (materials != null && !materials.isEmpty()) {
                List<Long> productIds = materials.stream().map(i -> i.getProduct().getId()).distinct().collect(Collectors.toList());
                Map<Long, MDAuxiliaryMaterialCategory> categoryMap = AuxiliaryMaterialUtils.getAuxiliaryMaterialCategoryMap();
                Map<Long, MDAuxiliaryMaterialItem> materialItemMap = AuxiliaryMaterialUtils.getAuxiliaryMaterialItemMap(productIds);
                Map<Long, Product> productMap = productService.getProductMap(productIds);
                for (AuxiliaryMaterial item : materials) {
                    MDAuxiliaryMaterialItem feeItem = materialItemMap.get(item.getMaterial().getId());
                    if (feeItem != null) {
                        item.setMaterial(feeItem);
                    }
                    MDAuxiliaryMaterialCategory feeCategory = categoryMap.get(item.getCategory().getId());
                    if (feeCategory != null) {
                        item.setCategory(feeCategory);
                    }
                    Product product = productMap.get(item.getProduct().getId());
                    if (product != null) {
                        item.setProduct(product);
                    }
                }
            }
            materialMaster.setItems(materials);
        }
        return materialMaster;
    }

    public List<AuxiliaryMaterial> getOrderAuxiliaryMaterialList(Long orderId, String quarter) {
        List<AuxiliaryMaterial> result = Lists.newArrayList();
        if (orderId != null && orderId > 0 && StringUtils.isNotBlank(quarter)) {
            result = auxiliaryMaterialDao.getAuxiliaryMaterialsByOrderId(orderId, quarter, AuxiliaryMaterial.DEL_FLAG_NORMAL);
        }
        return result;
    }

    //endregion 辅材v2.0

}


