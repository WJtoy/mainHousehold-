package com.wolfking.jeesite.modules.md.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.md.entity.CustomerPrice;
import com.wolfking.jeesite.modules.md.entity.CustomerProduct;
import com.wolfking.jeesite.modules.md.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 客户数据访问接口
 * Created on 2017-04-12.
 */
@Mapper
public interface CustomerProductDao extends LongIDCrudDao<CustomerProduct> {

    /**
     * 通过用户id或客户id查找客户列表
     * @param paramMap
     * @return
     */

    /*long save(CustomerProduct customerProduct);
    List<CustomerProduct> getByCustomer(CustomerProduct customerProduct);

    void deleteByCustomer(@Param("customerId") Long customerId);

    List<CustomerProduct> getCustomerProductsByIds(CustomerPrice customerPrice);

    // 用来product微服务后这个方法可以删除
    List<CustomerProduct> getCustomerProductsByIdsWithOutCustomer(CustomerPrice customerPrice);

    List<CustomerProduct> getCustomerProductsByIdsWithoutCustomerAndProduct(CustomerPrice customerPrice);

    *//**
     * 读取网点负责的产品ID列表
     * @param id 网点id
     * @return
     *//*
    public List<Product> getProductIdsById(Long id);

    *//**
     * 添加客户负责的产品
     * @param id    网点id
     * @param products 产品id列表
     *//*
    void assignProducts(@Param("id") Long id,@Param("products") List<Long> products);*/
}
