package com.wolfking.jeesite.modules.rpt.dao;

import com.wolfking.jeesite.modules.rpt.entity.SpecialFeesDetailsOfRptEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface CustomerSpecialFeesDetailsOfRptDao {


    List<SpecialFeesDetailsOfRptEntity> getCustomerRemoteOfOrderChargeDate(@Param("startDate") Date startDate,
                                                                           @Param("endDate") Date endDate,
                                                                           @Param("parentId") Long parentId,
                                                                           @Param("productCategoryIds") List<Long> productCategoryIds,
                                                                           @Param("customerId") Long customerId);

    List<SpecialFeesDetailsOfRptEntity> getCustomerOtherCostsOfTheOrderChargeDate(@Param("startDate") Date startDate,
                                                                                  @Param("endDate") Date endDate,
                                                                                  @Param("parentId") Long parentId,
                                                                                  @Param("productCategoryIds") List<Long> productCategoryIds,
                                                                                  @Param("customerId") Long customerId);
}
