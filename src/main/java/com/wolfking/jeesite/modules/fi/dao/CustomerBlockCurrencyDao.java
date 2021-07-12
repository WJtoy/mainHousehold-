package com.wolfking.jeesite.modules.fi.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.modules.fi.entity.CustomerCurrency;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface CustomerBlockCurrencyDao extends LongIDCrudDao<CustomerCurrency> {

    /**
     * 获取客户的冻结流水信息
     */
    List<CustomerCurrency> getCustomerBlockCurrencyList(@Param("beginDate") Date beginDate,
                                                        @Param("endDate") Date endDate,
                                                        @Param("customerId") Long customerId,
                                                        @Param("currencyType") Integer currencyType,
                                                        @Param("currencyNo") String currencyNo,
                                                        @Param("page") Page<CustomerCurrency> page);
}
