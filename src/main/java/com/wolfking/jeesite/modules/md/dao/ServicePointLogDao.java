package com.wolfking.jeesite.modules.md.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.md.entity.ServicePointLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 */
@Mapper
public interface ServicePointLogDao extends LongIDCrudDao<ServicePointLog> {

    /**
     * 查询网点的派单备注历史
     */
    List<ServicePointLog> getHisPlanRemarks(@Param("servicePointId") Long servicePointId);

    /**
     * 查询网点的备注历史
     */
    List<ServicePointLog> getHisRemarks(@Param("servicePointId") Long servicePointId);

}
