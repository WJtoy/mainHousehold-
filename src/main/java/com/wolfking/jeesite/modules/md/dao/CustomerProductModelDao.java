package com.wolfking.jeesite.modules.md.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.md.entity.CustomerProductModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 客户产品型号DAO
 *
 */
@Mapper
public interface CustomerProductModelDao extends LongIDCrudDao<CustomerProductModel> {
    /**
     * 删除客户产品分类
     * @param customerId
     */
    void deleteByCustomer(@Param("customerId") Long customerId);

    List<CustomerProductModel> getProductModelListByCustomer(@Param("customerId") Long customerId);

    Long checkIsExist(CustomerProductModel customerProductModel);

    List<CustomerProductModel> getListByProductId(@Param("customerId") Long customerId,@Param("productId") Long productId);

}
