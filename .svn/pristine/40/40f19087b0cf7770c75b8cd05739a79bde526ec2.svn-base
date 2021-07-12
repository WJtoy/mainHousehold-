package com.wolfking.jeesite.modules.rpt.dao;

import com.wolfking.jeesite.modules.rpt.entity.CustomerRevenueFeesRptEntity;
import com.wolfking.jeesite.modules.rpt.entity.SpecialFeesDetailsOfRptEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface CustomerRevenueFeesRptDao {

    CustomerRevenueFeesRptEntity getReceivableCharge(@Param("startDate") Date startDate,
                                                           @Param("endDate") Date endDate,
                                                           @Param("productCategoryIds") List<Long> productCategoryIds,
                                                           @Param("customerId") Long customerId,
                                                           @Param("quarter") String quarter);

    CustomerRevenueFeesRptEntity getWriteOffChargeList(@Param("startDate") Date startDate,
                                                             @Param("endDate") Date endDate,
                                                             @Param("productCategoryIds") List<Long> productCategoryIds,
                                                             @Param("customerId") Long customerId,
                                                             @Param("quarter") String quarter);


    CustomerRevenueFeesRptEntity getPayableChargeA(@Param("startDate") Date startDate,
                                                   @Param("endDate") Date endDate,
                                                   @Param("productCategoryIds") List<Long> productCategoryIds,
                                                   @Param("customerId") Long customerId,
                                                   @Param("quarter") String quarter);


    CustomerRevenueFeesRptEntity getPayableChargeB(@Param("startDate") Date startDate,
                                                   @Param("endDate") Date endDate,
                                                   @Param("productCategoryIds") List<Long> productCategoryIds,
                                                   @Param("customerId") Long customerId,
                                                   @Param("quarter") String quarter);

    CustomerRevenueFeesRptEntity getDiffCharge(@Param("startDate") Date startDate,
                                               @Param("endDate") Date endDate,
                                               @Param("productCategoryIds") List<Long> productCategoryIds,
                                               @Param("customerId") Long customerId,
                                               @Param("quarter") String quarter);
}
