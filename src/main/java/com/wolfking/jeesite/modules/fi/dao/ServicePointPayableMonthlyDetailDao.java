package com.wolfking.jeesite.modules.fi.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.fi.entity.ServicePointPayableMonthlyDetail;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author: jeff.zhao
 * @date: 2019/10/23 17:36
 * @Description:
 */
@Mapper
public interface ServicePointPayableMonthlyDetailDao extends LongIDCrudDao<ServicePointPayableMonthlyDetail> {
    /**
     * 按月，支付方式, 品类累计已付网点款 For 对帐
     * @param servicePointPayableMonthlyDetail
     */
    void incrAmountForCharge(ServicePointPayableMonthlyDetail servicePointPayableMonthlyDetail);

    List<com.kkl.kklplus.entity.fi.servicepoint.ServicePointPayableMonthlyDetail> getNeedPayListForUpdateBalanceDetail10Pay(com.kkl.kklplus.entity.fi.servicepoint.ServicePointPayableMonthlyDetail servicePointPayableMonthlyDetail);

    List<com.kkl.kklplus.entity.fi.servicepoint.ServicePointPayableMonthlyDetail> getNeedPayListForUpdateBalanceDetail20Pay(com.kkl.kklplus.entity.fi.servicepoint.ServicePointPayableMonthlyDetail servicePointPayableMonthlyDetail);
}
