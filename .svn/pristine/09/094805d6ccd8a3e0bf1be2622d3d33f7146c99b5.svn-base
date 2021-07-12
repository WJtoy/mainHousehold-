package com.wolfking.jeesite.modules.fi.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.fi.entity.CustomerCharge;
import com.wolfking.jeesite.modules.fi.entity.EngineerCharge;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Created by Jeff on 2017/4/19.
 */
@Mapper
public interface CustomerChargeDao extends LongIDCrudDao<CustomerCharge> {
    /**
     * 读取订单退补单信息
     * 只读取最后一条，createDate:对账日期，updateDate结算日期
     */
    public CustomerCharge getCustomerRetrieveCharge(@Param("orderId") Long orderId,@Param("quarter") String quarter);
}
