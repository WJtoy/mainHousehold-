package com.wolfking.jeesite.modules.fi.dao;

import com.kkl.kklplus.entity.fi.servicepoint.ServicePointPaidMonthlyDetail;
import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.fi.entity.ServicePointPaidMonthly;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author: jeff.zhao
 * @date: 2019/11/1 2:20
 * @Description:
 */
@Mapper
public interface ServicePointPaidMonthlyDetailDao extends LongIDCrudDao<ServicePointPaidMonthlyDetail> {
    /**
     * 计帐金额异动
     * @param paidMonthlyDetail
     * @return
     */
    int incrAmountForUpdateBalanceDetail(ServicePointPaidMonthlyDetail paidMonthlyDetail);
}
