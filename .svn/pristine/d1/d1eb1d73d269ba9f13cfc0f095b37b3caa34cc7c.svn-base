package com.wolfking.jeesite.modules.fi.dao;

import com.kkl.kklplus.entity.fi.servicepoint.ServicePointInvoiceMonthlyDetail;
import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.fi.entity.ServicePointInvoiceMonthly;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author: jeff.zhao
 * @date: 2019/11/1 2:20
 * @Description:
 */
@Mapper
public interface ServicePointInvoiceMonthlyDetailDao extends LongIDCrudDao<ServicePointInvoiceMonthlyDetail> {
    /**
     * 出帐金额异动
     * @param invoiceMonthlyDetail
     * @return
     */
    int incrAmountForUpdateBalanceDetail(ServicePointInvoiceMonthlyDetail invoiceMonthlyDetail);
}
