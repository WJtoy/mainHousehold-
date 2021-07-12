package com.wolfking.jeesite.modules.servicepoint.sd.dao;

import com.wolfking.jeesite.modules.sd.entity.OrderInsurance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ServicePointOrderOperationDao {

    /**
     * 获得互助基金
     */
    List<OrderInsurance> getInsurancesByServicePointId(@Param("quarter") String quarter, @Param("orderId") Long orderId, @Param("servicePointId") Long servicePointId);
}
