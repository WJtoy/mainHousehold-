/**
 * Copyright &copy; 2014-2014 All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.wolfking.jeesite.modules.rpt.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.fi.entity.ServicePointPayableMonthlyDetail;
import com.wolfking.jeesite.modules.rpt.entity.ServicePointBalanceMonthlyDetail;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author: jeff.zhao
 * @date: 2019/10/24 16:04
 * @Description: 计算财务报表中网点余额的DAO接口
 */
@Mapper
public interface ServicePointBalanceMonthlyDetailDao extends LongIDCrudDao<ServicePointBalanceMonthlyDetail> {

    /**
     * 对帐操作时同步增加余额按品类
     * @param servicePointPayableMonthlyDetail
     */
    void incrBalance(ServicePointPayableMonthlyDetail servicePointPayableMonthlyDetail);

    /**
     * 对帐操作时同步增加余额按品类
     * @param servicePointBalanceMonthlyDetail
     */
    int incrBalanceWithBalanceForUpdateBalanceDetail(com.kkl.kklplus.entity.fi.servicepoint.ServicePointBalanceMonthlyDetail servicePointBalanceMonthlyDetail);

}
