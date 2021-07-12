package com.wolfking.jeesite.modules.customer.fi.dao;

import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.modules.fi.entity.CustomerCurrency;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface CtCustomerCurrencyDao {

    CustomerCurrency get(Long id);

    List<CustomerCurrency> findCurrencyList(CustomerCurrency entity);


    /**
     * 获取客户的冻结流水信息
     * @param customerId
     * @param salesId
     * @param beginDate
     * @param endDate
     * @param currencyType
     * @param currencyNo
     * @param page
     * @return
     */
    List<CustomerCurrency> getCustomerBlockAmountList(@Param("customerId") Long customerId,
                                                      @Param("salesId") Long salesId,
                                                      @Param("beginDate") Date beginDate,
                                                      @Param("endDate") Date endDate,
                                                      @Param("currencyType") Integer currencyType,
                                                      @Param("currencyNo") String currencyNo,
                                                      @Param("page") Page<CustomerCurrency> page);

    /**
     * 插入数据
     * @param entity
     * @return
     */
    int insert(CustomerCurrency entity);
}
