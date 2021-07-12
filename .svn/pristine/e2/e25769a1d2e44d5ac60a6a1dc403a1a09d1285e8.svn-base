package com.wolfking.jeesite.modules.customer.fi.dao;

import com.wolfking.jeesite.modules.md.entity.CustomerFinance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CtCustomerFinanceDao {

    /**
     * 获取单条数据
     * @param id
     * @return
     */
    public CustomerFinance get(long id);

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
     * 更新balance(在线充值)
     * @param entity
     */
    void updateBalance(CustomerFinance entity);
}
