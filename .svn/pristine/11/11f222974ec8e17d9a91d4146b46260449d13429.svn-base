package com.wolfking.jeesite.ms.providermd.service;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.wolfking.jeesite.ms.providermd.feign.MSProductCategoryServicePointFeign;
import com.wolfking.jeesite.ms.providermd.utils.MDUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class MSProductCategoryServicePointService {
    @Autowired
    private MSProductCategoryServicePointFeign productCategoryServicePointFeign;

    /**
     * 修改网点产品类目映射
     *
     * @param servicePointId     网点id
     * @param productCategoryIds 产品类目id列表
     * @return
    */
    public void update(Long servicePointId, List<Long> productCategoryIds) {
        MSErrorCode msErrorCode = MDUtils.customSave(()->productCategoryServicePointFeign.update(servicePointId, productCategoryIds));
        if (msErrorCode.getCode() >0) {
            throw new RuntimeException("调用微服务更新网点-产品类型映射失败.失败原因:"+msErrorCode.getMsg());
        }
    }

    /**
     * 根据网点id查询网点品类
     * @param servicePointId
     * @return
     */
    public List<Long> findListByServicePiontIdFromCacheForSD(Long servicePointId) {
        return MDUtils.findListUnnecessaryConvertType(()->productCategoryServicePointFeign.findListByServicePointIdFromCacheForSD(servicePointId));
    }

    /**
     * 根据网点id查询网点品类
     * @param servicePointId
     * @return
     */
    public List<Long> findListByServicePointIdForMD(Long servicePointId) {
        return MDUtils.findListUnnecessaryConvertType(()->productCategoryServicePointFeign.findListByServicePointIdForMD(servicePointId));
    }

    /**
     * 根据网点Id和品类id从缓存中判断网点品类是否存在
     * @param servicePointId
     * @param productCategoryId
     * @return
     */
    public boolean existByPointIdAndCategoryIdFromCacheForSD(Long servicePointId, Long productCategoryId) {
        Boolean exists =  MDUtils.getObjUnnecessaryConvertType(()->productCategoryServicePointFeign.existByPointIdAndCategoryIdFromCacheForSD(servicePointId, productCategoryId));
        if (exists == null) {
            return false;
        }
        return exists;
    }

    /**
     *
     * @param servicePointIds
     * @param productCategoryId
     * @return
     */
    public List<Long> findListByProductCategoryIdAndServicePointIds(List<Long> servicePointIds, Long productCategoryId) {
        if(CollectionUtils.isEmpty(servicePointIds) || productCategoryId == null || productCategoryId.longValue() <= 0){
            return null;
        }
       return MDUtils.findListUnnecessaryConvertType(()->productCategoryServicePointFeign.findListByProductCategoryIdAndServicePointIds(servicePointIds, productCategoryId));
    }
}
