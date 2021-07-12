package com.wolfking.jeesite.modules.fi.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.fi.entity.EngineerChargeCondition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by Jeff on 2017/4/19.
 */
@Mapper
public interface EngineerChargeConditionDao extends LongIDCrudDao<EngineerChargeCondition> {
    /**
     * 查询满足条件的ID
     * @param engineerChargeCondition
     * @return
     */
    List<EngineerChargeCondition> selectConditionInfo(EngineerChargeCondition engineerChargeCondition);

    /**
     * 关闭对帐单查询数据，结账时关闭
     * @param chargeConditionIds
     */
    void close(@Param("chargeConditionIds") List<Long> chargeConditionIds);

    /**
     * 补10月网点按品类应付数据
     * @param orderNo
     * @param servicePointId
     * @return
     */
    EngineerChargeCondition getOneByOrderNoAndServicePointIdForUpdatePayable(@Param("orderNo") String orderNo, @Param("servicePointId") Long servicePointId);
}
