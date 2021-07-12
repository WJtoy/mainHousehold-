package com.wolfking.jeesite.modules.fi.dao;

import com.kkl.kklplus.entity.fi.servicepoint.ServicePointPayableMonthlyDetail;
import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.modules.fi.entity.ServicePointPayableMonthly;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by Jeff on 2017/6/14.
 */
@Mapper
public interface ServicePointPayableMonthlyDao extends LongIDCrudDao<ServicePointPayableMonthly> {

    /**
     * 获取需要重新计算当月余额的分组网点列表
     * @return
     */
    List<ServicePointPayableMonthly> getGroupedListForUpdateRptBalance(@Param("page") Page<ServicePointPayableMonthly> page);

    /**
     * 获取一个id,用于判断记录是否存在
     * @param servicePointId
     * @param year
     * @param paymentType
     * @return
     */
    Long getOneId(@Param("servicePointId") Long servicePointId,
                  @Param("year") Integer year,
                  @Param("paymentType") Integer paymentType);

    /**
     * 初始化
     * @param servicePointPayableMonthly
     */
    void insertDefaults(ServicePointPayableMonthly servicePointPayableMonthly);
    /**
     * 按月，支付方式累计已付网点款
     * @param servicePointPayableMonthly
     */
//    void incrAmount(ServicePointPayableMonthly servicePointPayableMonthly);

    /**
     * 按月，支付方式累计已付网点款 For 对帐
     * @param servicePointPayableMonthly
     */
    void incrAmountForCharge(ServicePointPayableMonthly servicePointPayableMonthly);

    /**
     * 补10月网点按品类应付数据
     * @return
     */
    List<Long> getPayableMonthByYearMonth(@Param("page") Page<Long> page, @Param("year") int year, @Param("month") int month);
}
