package com.wolfking.jeesite.modules.md.service;

import com.google.common.collect.Lists;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.entity.md.GlobalMappingSyncTypeEnum;
import com.kkl.kklplus.entity.md.MDServicePointPriceSyncTypeEnum;
import com.kkl.kklplus.entity.md.MDServicePointProduct;
import com.kkl.kklplus.entity.md.mq.MQServicePointPriceMessage;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.md.entity.Product;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.md.entity.ServicePointLog;
import com.wolfking.jeesite.modules.mq.sender.ServicePointPriceSender;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.globalmapping.service.ProductCategoryServicePointMappingService;
import com.wolfking.jeesite.ms.providermd.service.MSServicePointProductService;
import com.wolfking.jeesite.ms.providermd.service.MSServicePointService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class ServicePointProductService {

    @Autowired
    private ServicePointService servicePointService;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private ProductService productService;

    @Autowired
    private MSServicePointService msServicePointService;

    //@Autowired
    //private ProductCategoryServicePointMappingService productCategoryServicePointMappingService;  //mark on 2020-6-12

    @Autowired
    private MSServicePointProductService msServicePointProductService;

    @Autowired
    private ServicePointLogService servicePointLogService;

    @Autowired
    private ServicePointPriceSender servicePointPriceSender;

    private static final String MD_SERVICEPOINT_LOCK = "LOCK:SERVICEPOINT:%s";

    @Transactional(readOnly = false)
    public void save(ServicePoint servicePoint) {
        log.warn("保存网点的产品信息,传入的servicePoint:{},products:{}", servicePoint.getId(),servicePoint.getProductIds());

        if(servicePoint != null){
            ServicePoint cachedServicePoint = servicePointService.getFromCache(servicePoint.getId());
            servicePoint.setName(cachedServicePoint.getName());
            servicePoint.setUseDefaultPrice(cachedServicePoint.getUseDefaultPrice());
        }
        // 获得锁
        String lockKey = String.format(MD_SERVICEPOINT_LOCK, servicePoint.getId());
        Boolean lock = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockKey, 1, 60);   //1分钟
        if (!lock) {
            throw new RuntimeException("网点正在修改中，请稍候重试。");
        }

        User actionUser = UserUtils.getUser();  //mark on 2020-3-5
        try {
            // 修改前的产品id列表
            List<Long> productIdList = getProductIdsById(servicePoint.getId());
            // newProductIds
            List<Long> products = Lists.newArrayList();
            if (StringUtils.isNoneBlank(servicePoint.getProductIds())) {
                products = Arrays.stream(servicePoint.getProductIds().split(","))
                        .map(t -> Long.valueOf(t))
                        .collect(Collectors.toList());

                if (!products.isEmpty()) {
                    // newProductMap
                    Map<Long, Product> productMap = productService.getProductMap(products);
                    List<Long> productCategoryIds = productMap.values().stream()
                            .filter(i -> i.getCategory() != null && i.getCategory().getId() != null)
                            .map(i->i.getCategory().getId())
                            .distinct()
                            .collect(Collectors.toList());
                    MSErrorCode msErrorCode = msServicePointService.updateProductCategoryServicePointMapping(servicePoint.getId(), productCategoryIds);
                    if (msErrorCode.getCode() >0) {
                        throw new RuntimeException("调用微服务更新网点-产品类型映射失败。失败原因:"+msErrorCode.getCode());
                    }
                    // mark on 2020-6-12 begin
//                    productCategoryServicePointMappingService
//                            .saveProductCategoryServicePointMapping(GlobalMappingSyncTypeEnum.UPDATE,
//                                    servicePoint.getId(), productCategoryIds);
                    // mark on 2020-6-12 end
                }
            }

            boolean resetPermit = false;//无重置权限
            if (SecurityUtils.getSubject().isPermitted("md:servicepoint:defaultpriceedit")) {
                resetPermit = true;
            }

            // 原产品列表
            Set<Long> productIdSet = productIdList.stream().collect(Collectors.toSet());
            // 移除网点下的产品
            msServicePointProductService.removeProducts(servicePoint.getId());
            if (!products.isEmpty()) {
                List<List<Long>> productParts = Lists.partition(products, 500);
                // 网点分配产品
                productParts.stream().forEach(list -> {
                    msServicePointProductService.assignProducts(list, servicePoint.getId());
                });
            }
            // 写网点操作日志
            servicePointLogService.saveServicePointLog(servicePoint.getId(),
                    ServicePointLog.ServicePointLogType.EDIT_SERVICEPOINT, "编辑网点产品",
                    "修改网点产品信息.", actionUser);

            // 无重置权限，或选择不重置价格,只添加新增的产品的价格
            if (!resetPermit || servicePoint.getResetPrice() == 0) {
                Set<Long> productListIdSet = products.stream().collect(Collectors.toSet());
                // 找到新增的产品
                productListIdSet.removeAll(productIdSet);
                if (productListIdSet.size() > 0) {
                    // 发送生成网点价格消息
                    MQServicePointPriceMessage.ServicePointPriceMessage.Builder  servicePointPriceMessage =  MQServicePointPriceMessage.ServicePointPriceMessage.newBuilder();
                    servicePointPriceMessage.setServicePointId(servicePoint.getId());
                    servicePointPriceMessage.addAllProductId(Lists.newArrayList(productListIdSet));
                    servicePointPriceMessage.setSyncType(MDServicePointPriceSyncTypeEnum.PARTADD.getValue());
                    servicePointPriceMessage.setUserId(UserUtils.getUser() != null && UserUtils.getUser().getId()!= null? UserUtils.getUser().getId(): 0L);
                    servicePointPriceSender.send(servicePointPriceMessage.build());
                }
            }
        } catch (Exception e) {
            LogUtils.saveLog("修改网点产品", "ServicePointProductService.save", "servicePointId:"+servicePoint.getId(), e, actionUser);
            throw new RuntimeException(e);
        } finally {
            if (lock && lockKey != null) {
                redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockKey);
            }
        }
    }

    public List<Long> getProductIdsById(Long servicePointId) {
        MDServicePointProduct mdServicePointProduct = new MDServicePointProduct();

        mdServicePointProduct.setServicePointId(servicePointId);
        List<Long> productIdListFromMS = msServicePointProductService.findProductIds(mdServicePointProduct);
        if (!org.springframework.util.ObjectUtils.isEmpty(productIdListFromMS)) {
            productIdListFromMS = productIdListFromMS.stream().sorted().collect(Collectors.toList());
        }
        return productIdListFromMS;
    }
}
