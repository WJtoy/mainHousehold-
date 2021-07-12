package com.wolfking.jeesite.modules.fi.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.fi.entity.EngineerChargeMaster;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Created by Jeff on 2017/4/19.
 */
@Mapper
public interface EngineerChargeMasterDao extends LongIDCrudDao<EngineerChargeMaster> {
    /**
     * 根据ID查询工单级别费用
     * @param orderId
     * @return
     */
    EngineerChargeMaster findOrderLevelFee(@Param("orderId") Long orderId, @Param("servicePointId") Long servicePointId);
}
