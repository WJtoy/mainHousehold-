package com.wolfking.jeesite.modules.md.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.md.entity.CustomerMaterial;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 客户配件
 * Created on 2019-06-01.
 */
@Mapper
public interface CustomerMaterialDao extends LongIDCrudDao<CustomerMaterial> {

    /**
     * 检测数据是否已经存在
     * @param  customerMaterial
     * @return Long
     */
    Long checkIsExist(CustomerMaterial customerMaterial);

    /**
     * 根据客户,产品,配件获取
     * @param  customerMaterial
     * @return customerMaterial
     */
    CustomerMaterial getByCustomerAndProductAndMaterial(CustomerMaterial customerMaterial);

    /**
     * 根据客户,产品删除数据
     * @param  customerId,productId
     * @return customerMaterial
     */
    CustomerMaterial getByCustomerAndProductAndMaterial(@Param("customerId")long customerId,@Param("productId") long productId,@Param("materialId") long materialId);

 /**
     * 根据客户,产品删除数据
     * @param  customerId,productId
     * @return customerMaterial
     */
    Long deleteByCustomerAndProduct(@Param("customerId") Long customerId, @Param("productId")Long productId);

    /**
     * 根据客户+产品 读取配件列表
     * @param   customerId
     * @param   productId
     */
    List<CustomerMaterial> getListByCustomerAndProduct(@Param("customerId")long customerId, @Param("productId") long productId);

}
