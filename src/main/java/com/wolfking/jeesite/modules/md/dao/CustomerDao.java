package com.wolfking.jeesite.modules.md.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.md.entity.Customer;
import org.apache.ibatis.annotations.Mapper;

/**
 * 客户数据访问接口
 * Created on 2017-04-12.
 */
@Mapper
public interface CustomerDao extends LongIDCrudDao<Customer> {

    /**
     * 通过用户id或客户id查找客户列表
     * @param paramMap
     * @return
     */
    //java.util.List<Customer> findListByUserIdOrCustomerId(java.util.Map<String,Object> paramMap);  //mark on 2020-2-10

    //List<Customer> findSpecList(Customer customer);  //mark on 2020-2-10
    //List<Customer> findApproveList(Customer customer);//mark on 2020-2-10
    //Long existsCustomerByCode(String name); //mark on 2020-2-10
    //void deleteById(long id);  //mark on 2020-2-11
    //Customer getApprove(long id); //mark on 2020-2-11
    //List<Customer> rptFindCustomerList(Customer customer);  //mark on 2020-2-11

    /**
     * 读取已分配客服的客户id
     * @return
     */
    //List<Long> getCustomerIdsAssignedService();  //mark on 2020-2-10

    /// 以下是价格管理

    /**
     * 清除网点下所有的价格
     * @param id 网点
     * @return
     */
//    int deletePrices(Long id);

    /**
     * 修改价格
     */
    //void updatePrice(CustomerPrice price);  //mark on 2020-2-11

    /**
     * 修改价格
     */
    //void updatePriceByMap(HashMap<String,Object> maps);  //mark on 2020-2-11

    /**
     * 获得客户id清单
     * @param customerPrice 查询条件
     * @return
     */
    //List<Customer> findCustomerIdList(CustomerPrice customerPrice);  //mark on 2020-2-11

    /**
     * 获得某业务负责的客户
     * @param id  业务员帐号id
     */
    //List<Customer> getCustomerListOfSales(Long id);  //mark on 2020-2-20

    /**
     * 按id获得具体的价格
     * @param id
     * @return
     */
    //CustomerPrice getPrice(@Param("id") Long id,@Param("delFlag") Integer delFlag);  //mark on 2020-2-11

    /**
     * 获得某客户的所有价格清单
     * @param id 客户id
     * @param delFlag 0:启用的价格 1:停用的价格 2:待审核的价格 null:所有
     * @return
     */
    //List<CustomerPrice> getPrices(@Param("id") Long id,@Param("delFlag") Integer delFlag); //mark on 2020-2-11

    /**
     * 获得某客户的生效价格清单用于缓存
     * @param id 客户id
     * @return
     */
    //List<CustomerPrice> getPricesForCache(@Param("id") Long id);   //mark on 2020-2-11

    /**
     * 获得客户id清单
     * @param customerPrice 查询条件
     * @return
     */
    //List<CustomerPrice> findist(CustomerPrice customerPrice);  //mark on 2020-2-11

    /**
     * 按多个id获得客户下价格
     * @param ids 客户id列表
     * @param productId 产品id列表
     * @return
     */
    //List<CustomerPrice> getPricesByCustomers(@Param("ids") List<Long> ids, @Param("productId") Long productId, @Param("serviceTypeId") Long serviceTypeId); //mark on 2020-2-11

    //List<CustomerPrice> getPricesByPriceIds(@Param("ids") List<Long> ids);  //mark on 2020-2-11

    /**
     * 添加客户服务价格
     */
    //void insertPrices(CustomerPrices customerPrices);  //mark on 2020-2-11

    /**
     * 添加一笔价格
     * @param customerPrice
     */
    //void insertPrice(CustomerPrice customerPrice); //mark on 2020-2-11

    /**
     * 获得待审核价格清单
     * @param customerPrice 查询条件
     * @return
     */
    //List<CustomerPrice> findApprovePriceList(CustomerPrice customerPrice);  //mark on 2020-2-11

    /**
     * 审核
     * @param ids
     */
    //void approvePrices(@Param("ids") List<Long> ids,@Param("updateBy") Long updateBy); //mark on 2020-2-11

    /**
     * 停用
     * @param ids
     */
    //void stopPrices(@Param("ids") List<Long> ids,@Param("updateBy") Long updateBy);//mark on 2020-2-11

    /**
     * 删除客户产品价格
     * @param customerId
     * @param products
     */
    //void deletePricesByCustomerAndProducts(@Param("customerId") Long customerId, @Param("products") List<Long> products); //mark on 2020-2-11

    /**
     * 查询客户的基础知识，供“客户信息”报表使用
     * @param customerId
     * @param paymentType
     * @return
     */
    // mark on 2020-2-11 web端去customer
//    List<CustomerBaseInfoRptEntity> getCustomerBaseInfoList(@Param("customerId") Long customerId,
//                                                            @Param("paymentType") Integer paymentType);

    /**
     * 查询客户的产品列表，供“客户信息”报表使用
     * 结果集中的Product对象使用name属性存储客户的产品列表
     * @param customerId
     * @param paymentType
     * @return
     */
    // mark on 2020-2-11 web端去customer
//    List<Product> getCustomerProducts(@Param("customerId") Long customerId,
//                                      @Param("paymentType") Integer paymentType);


    /**
     * 查询客户的产品列表，供“客户信息”报表使用
     * 结果集中的Product对象使用name属性存储客户的产品列表
     * @param customerId
     * @param paymentType
     * @return
     */
    //mark on 2020-2-11
//    List<Product> getCustomerProductsWithOutCustomer(@Param("customerId") Long customerId,
//                                                     @Param("paymentType") Integer paymentType);


}
