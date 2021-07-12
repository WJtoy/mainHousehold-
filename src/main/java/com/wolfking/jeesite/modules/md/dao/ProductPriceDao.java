package com.wolfking.jeesite.modules.md.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.md.entity.ProductPrice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 客户数据访问接口
 * Created on 2017-04-12.
 */
@Mapper
public interface ProductPriceDao extends LongIDCrudDao<ProductPrice> {
    /**
     * 获取某产品某项服务项目的默认价格
     * @param productId
     * @param serviceTypeId
     * @return
     */
//    ProductPrice getByProductIDAndServiceTypeId(@Param("productId") Long productId, @Param("serviceTypeId") Long serviceTypeId);

    /**
     * 获取某产品某项服务项目的默认价格Id
     * @param productId
     * @param serviceTypeId
     * @return
     */
//    Long getIdByProductIDAndServiceTypeId(@Param("productId") Long productId, @Param("serviceTypeId") Long serviceTypeId);

    /**
     * 获取某产品某项服务项目的默认价格
     * @param productId
     * @param serviceTypeId
     * @param priceType
     * @return
     */
//    ProductPrice getByProductIDAndServiceTypeIdAndPriceType(@Param("productId") Long productId, @Param("serviceTypeId") Long serviceTypeId, @Param("priceType") Integer priceType);

    /**
     * 获取某产品某项服务项目的默认价格Id
     * @param productId
     * @param serviceTypeId
     * @param priceType
     * @return
     */
//    Long getIdByProductIDAndServiceTypeIdAndPriceType(@Param("productId") Long productId, @Param("serviceTypeId") Long serviceTypeId, @Param("priceType") Integer priceType);

    /**
     * 获取分组可用参考价格列表
     * @param productIds
     * @return
     */
//    List<ProductPrice> findGroupList(@Param("productIds") List<Long> productIds, @Param("serviceTypeIds") List<Long> serviceTypeIds, @Param("priceType") Integer priceType, @Param("servicePointId") Long servicePointId, @Param("customerId") Long customerId);

    /**
     * 获取分组所有参考价格列表
     * @param productIds
     * @return
     */
//    List<ProductPrice> findAllGroupList(@Param("productIds") List<Long> productIds, @Param("serviceTypeIds") List<Long> serviceTypeIds, @Param("priceType") Integer priceType, @Param("servicePointId") Long servicePointId, @Param("customerId") Long customerId);
}
