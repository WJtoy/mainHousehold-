package com.wolfking.jeesite.modules.fi.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.fi.entity.CustomerChargeCondition;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Created by Jeff on 2017/4/19.
 */
@Mapper
public interface CustomerChargeConditionDao extends LongIDCrudDao<CustomerChargeCondition> {
    /**
     * 查询满足条件的ID
     * @param customerChargeCondition
     * @return
     */
    List<CustomerChargeCondition> selectConditionInfo(CustomerChargeCondition customerChargeCondition);
}
