package com.wolfking.jeesite.modules.md.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.md.entity.UrgentCustomer;
import com.wolfking.jeesite.modules.md.entity.viewModel.UrgentChargeModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 客户加急等级费用DAO
 *
 */
@Mapper
public interface UrgentCustomerlDao extends LongIDCrudDao<UrgentCustomer> {

    void deleteByCustomerId(@Param("customerId") Long customerId);

    /*List<UrgentCustomer> findListByCustomerId(@Param("customerId") Long customerId);*/

    List<UrgentChargeModel> findListByCustomerId(@Param("customerId") Long customerId, @Param("areaId") Long areaId);
}
