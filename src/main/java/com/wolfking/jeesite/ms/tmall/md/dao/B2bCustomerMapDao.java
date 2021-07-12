package com.wolfking.jeesite.ms.tmall.md.dao;

import com.wolfking.jeesite.common.persistence.BaseDao;
import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.ms.tmall.md.entity.B2bCustomerMap;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 *  对接系统店铺与厂商关联
 */
@Mapper
public interface B2bCustomerMapDao extends BaseDao {

//
//    B2bCustomerMap get(Long id);
//
//    /**
//     * 按厂商获得说有关联店铺id(可能1:n)
//     */
//    List<B2bCustomerMap> getShopListByCustomer(@Param("dataSource") int dataSource, @Param("customerId") Long customerId);
//
//    /**
//     * 按商铺id获得厂商id(1:1)
//     */
//    Long getCustomerIdByShopId(@Param("dataSource") int dataSource,@Param("shopId")String shopId);
//
//    Long getByShopId(@Param("id") Long id, @Param("dataSource") Integer dataSource, @Param("shopId") String shopId);
}
