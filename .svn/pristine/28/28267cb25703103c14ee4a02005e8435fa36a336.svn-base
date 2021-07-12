package com.wolfking.jeesite.modules.fi.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.fi.entity.EngineerCharge;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by Jeff on 2017/4/19.
 */
@Mapper
public interface EngineerChargeDao extends LongIDCrudDao<EngineerCharge> {

    /**
     * 关闭对帐单
     * @param id
     */
    void closeSingle(@Param("id") Long id);
    /**
     * 关闭对帐单，结账时关闭
     * @param chargeIds
     * @param updateById
     */
    void close(@Param("chargeIds") List<Long> chargeIds, @Param("updateById") Long updateById);

    List<EngineerCharge> getNotExistCurrencyList();
}
