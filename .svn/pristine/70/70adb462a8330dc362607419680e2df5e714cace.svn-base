package com.wolfking.jeesite.modules.rpt.dao;

import com.wolfking.jeesite.modules.rpt.entity.SpecialFeesDetailsOfRptEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface SpecialFeesDetailsOfRptDao {

    List<SpecialFeesDetailsOfRptEntity> getRemoteCostOfOrder(@Param("startDate") Date startDate,
                                                             @Param("endDate") Date endDate,
                                                             @Param("parentId") Long parentId,
                                                             @Param("productCategoryIds")List<Long> productCategoryIds);

    List<SpecialFeesDetailsOfRptEntity> getOtherCostsOfTheOrder(@Param("startDate") Date startDate,
                                                                @Param("endDate") Date endDate,
                                                                @Param("parentId") Long parentId,
                                                                @Param("productCategoryIds")List<Long> productCategoryIds);

    List<SpecialFeesDetailsOfRptEntity> getRemoteCostOfOrderChargeDate(@Param("startDate") Date startDate,
                                                             @Param("endDate") Date endDate,
                                                             @Param("parentId") Long parentId,
                                                             @Param("productCategoryIds") List<Long> productCategoryIds);

    List<SpecialFeesDetailsOfRptEntity> getOtherCostsOfTheOrderChargeDate(@Param("startDate") Date startDate,
                                                                @Param("endDate") Date endDate,
                                                                @Param("parentId") Long parentId,
                                                                @Param("productCategoryIds") List<Long> productCategoryIds);
}
