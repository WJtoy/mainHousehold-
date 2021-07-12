package com.wolfking.jeesite.modules.md.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.md.entity.InsurancePrice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 保险价格DAO
 *
 */
@Mapper
public interface InsurancePriceDao extends LongIDCrudDao<InsurancePrice> {

    InsurancePrice getByCategory(@Param("categoryId") Long categoryId);
}
