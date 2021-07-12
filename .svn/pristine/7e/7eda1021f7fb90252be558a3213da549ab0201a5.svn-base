package com.wolfking.jeesite.modules.fi.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.fi.entity.ServicePointPaidMonthly;
import org.apache.ibatis.annotations.Mapper;

/**
 * Created by Jeff on 2017/6/14.
 */
@Mapper
public interface ServicePointPaidMonthlyDao extends LongIDCrudDao<ServicePointPaidMonthly> {

    /**
     * 初始化
     * @param servicePointPaidMonthly
     */
    void insertDefaults(ServicePointPaidMonthly servicePointPaidMonthly);
    /**
     * 按月，支付方式累计已付网点款
     * @param servicePointPaidMonthly
     */
    void incrAmount(ServicePointPaidMonthly servicePointPaidMonthly);
}
