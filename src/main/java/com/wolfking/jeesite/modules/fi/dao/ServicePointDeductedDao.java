package com.wolfking.jeesite.modules.fi.dao;

import com.kkl.kklplus.entity.fi.servicepoint.ServicePointDeducted;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by Jeff on 2017/6/14.
 */
@Mapper
public interface ServicePointDeductedDao{
    /**
     * 获取抵扣款汇总列表
     * @param deductionYearMonth
     * @return
     */
    List<ServicePointDeducted> getDeductedAmountList(@Param("deductionYearMonth") int deductionYearMonth, @Param("servicePointId") Long servicePointId);
}
