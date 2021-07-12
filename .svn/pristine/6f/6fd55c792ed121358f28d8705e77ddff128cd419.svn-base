package com.wolfking.jeesite.modules.finance.md.dao;

import com.wolfking.jeesite.modules.md.entity.CustomerFinance;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FiCustomerFinanceDao {

    void insert(CustomerFinance finance);

    void update(CustomerFinance finance);

    void deleteById(long id);

    CustomerFinance get(long id);

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
}
