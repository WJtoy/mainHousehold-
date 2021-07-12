package com.wolfking.jeesite.modules.md.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.md.entity.TimeLinessPrice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 产品分类时效奖励设定DAO
 *
 */
@Mapper
public interface TimeLinessPriceDao extends LongIDCrudDao<TimeLinessPrice> {

    /**
     * 按产品类别id删除
     * @param categoryId    产品类别id
     */
    void deleteByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * 按产品类别id获得时效奖励费用
     * @param categoryId    产品类别id
     * @return
     */
    List<TimeLinessPrice> getPrices(@Param("categoryId") Long categoryId);
}
