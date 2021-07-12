package com.wolfking.jeesite.modules.md.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.md.entity.CustomerFinance;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 客户数据访问接口
 * Created on 2017-04-12.
 */
@Mapper
public interface CustomerFinanceDao extends LongIDCrudDao<CustomerFinance> {
    /**
     * 读取客户财务信息For下单
     * @param id
     * @return
     */
    CustomerFinance getForAddOrder(long id);

    void deleteById(long id);

    /**
     * 更新balance(在线充值)
     * @param entity
     */
    void updateBalance(CustomerFinance entity);

    /**
     * 增减订单 待付+冻结 金额
     * 都更新到block_amount字段
     * @param id 客户id
     * @param blockAmount 增减冻结金额,正数:增加 负数:扣减
     * @param orderAmount 订单金额
     * @param updateBy 操作人
     */
    void incBlockAmount(@Param("id") Long id,@Param("blockAmount") double blockAmount,@Param("orderAmount") double orderAmount,@Param("updateBy") long updateBy,@Param("updateDate") Date updateDate);

    Double getBalanceAmount(@Param("id") Long id);

    /**
     * 根据ID获取客户所有金额
     * @param id
     * @return
     */
    CustomerFinance getAmounts(@Param("id") Long id);

    /**
     * 充值时更新余额
     */
    void updateBalanceForRecharge(CustomerFinance customerFinance);

    /**
     * 结帐时更新余额
     * @param customerFinance
     */
    void updateBalanceFromInvoice(CustomerFinance customerFinance);

    /**
     * 结帐时更新Amount
     * @param customerFinance
     */
    void updateAmountFromInvoice(CustomerFinance customerFinance);

    /**
     * 根据用ID列表获取客户冻结金额
     * @param ids
     * @return
     */
    List<CustomerFinance> getBlockAmountByIds(@Param("ids")List<Long> ids);

    /**
     * 根据用ID列表获取客户余额
     * @param ids
     * @return
     */
    List<CustomerFinance> getBalanceByIds(@Param("ids")List<Long> ids);

    /**
     * 更新对私银行信息
     * @param customerFinance
     */
    void updatePrivate(CustomerFinance customerFinance);

    /**
     * 更新对公银行信息
     * @param customerFinance
     */
    void updatePublic(CustomerFinance customerFinance);
    /**
     * 根据用ID列表获取客户信用额度和押金
     * @param customerName  客户名称
     * @param paymentType 结算方式
     * @return
     */

//    List<CustomerFinance> getCreditAndDeposits(@Param("customerName") String customerName,
//                                               @Param("paymentType") Integer paymentType);  //mark on 2020-2-11

//    /**
//     * 获取客户信用额度和押金
//     * @param customerId 客户ID
//     * @return
//     */
//    @MapKey("id")
//    Map<Long, CustomerFinance> getCreditAndDepositsWithOutCustomer(@Param("customerId") Long customerId);
}
