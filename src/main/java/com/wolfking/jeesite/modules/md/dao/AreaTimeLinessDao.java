package com.wolfking.jeesite.modules.md.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.md.entity.AreaTimeLiness;
import com.wolfking.jeesite.modules.md.entity.Brand;
import org.apache.ibatis.annotations.Mapper;

/**
 * 区域产品时效奖励开关表
 */
@Mapper
public interface AreaTimeLinessDao extends LongIDCrudDao<AreaTimeLiness> {

}
