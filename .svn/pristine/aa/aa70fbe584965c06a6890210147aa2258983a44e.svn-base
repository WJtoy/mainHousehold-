package com.wolfking.jeesite.modules.md.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.md.entity.CustomerTimeliness;
import com.wolfking.jeesite.modules.md.entity.viewModel.TimelinessChargeModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 客户时效等级关联DAO
 *
 */
@Mapper
public interface CustomerTimelinessDao extends LongIDCrudDao<CustomerTimeliness> {

    void deleteByCustomerId(@Param("customerId") Long customerId);

    /**
     * 查找客户某个省级区域下的时效等级value list
     *
     * @param customerId
     * @param areaId
     * @return
     */
    List<TimelinessChargeModel> findListByCustomerId(@Param("customerId") Long customerId,@Param("areaId") Long areaId);

}
