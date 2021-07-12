package com.wolfking.jeesite.modules.fi.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.fi.entity.ServicePointInvoiceMonthly;
import org.apache.ibatis.annotations.Mapper;

/**
 * Created by Jeff on 2017/6/14.
 */
@Mapper
public interface ServicePointInvoiceMonthlyDao extends LongIDCrudDao<ServicePointInvoiceMonthly> {

    /**
     * 获取ID,用于判断记录是否存在
     * @param servicePointInvoiceMonthly
     * @return
     */
    Long getOneId(ServicePointInvoiceMonthly servicePointInvoiceMonthly);

    /**
     * 按月，支付方式累计出帐网点款
     * @param servicePointInvoiceMonthly
     */
    void incrAmount(ServicePointInvoiceMonthly servicePointInvoiceMonthly);
}
