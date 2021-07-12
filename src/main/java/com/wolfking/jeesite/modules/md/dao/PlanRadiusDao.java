package com.wolfking.jeesite.modules.md.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.md.entity.PlanRadius;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PlanRadiusDao extends LongIDCrudDao<PlanRadius> {
    /**
     * 按区域id读取
     * @param areaId 区域id(区县级)
     */
    public PlanRadius getByAreaId(@Param("areaId") long areaId);
}
