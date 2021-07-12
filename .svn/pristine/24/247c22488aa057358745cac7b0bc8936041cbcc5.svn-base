/**
 * Copyright &copy; 2014-2014 All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.wolfking.jeesite.modules.rpt.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.fi.entity.ServicePointPayableMonthly;
import com.wolfking.jeesite.modules.rpt.entity.ServicePointBalanceMonthly;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 计算财务报表中网点余额的DAO接口
 * @author ThinkGem
 * @version 2013-8-23
 */
@Mapper
public interface ServicePointBalanceMonthlyDao extends LongIDCrudDao<ServicePointBalanceMonthly> {

    /**
     * 获取网点当月余额
     * @param servicePointId
     * @param paymentType
     * @return
     */
    Double getServicePointCurrentMonthBalance(@Param("servicePointId") Long servicePointId,
                                              @Param("paymentType") Integer paymentType);

    /**
     * 更新网点指定月份余额
     * @param year
     * @param monthField
     * @param balance
     * @param servicePointId
     * @param paymentType
     */
    void updateMonthBalance(@Param("year") Integer year,
                            @Param("monthField") String monthField,
                            @Param("balance") Double balance,
                            @Param("servicePointId") Long servicePointId,
                            @Param("paymentType") Integer paymentType);


    /**
     * 对帐操作时同步增加余额
     * @param servicePointPayableMonthly
     */
    void incrBalance(ServicePointPayableMonthly servicePointPayableMonthly);

}
