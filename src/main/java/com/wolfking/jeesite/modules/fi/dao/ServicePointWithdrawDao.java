package com.wolfking.jeesite.modules.fi.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.modules.fi.entity.ServicePointPayCondition;
import com.wolfking.jeesite.modules.fi.entity.ServicePointWithdraw;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * Created by Jeff on 2017/6/14.
 */
@Mapper
public interface ServicePointWithdrawDao extends LongIDCrudDao<ServicePointWithdraw> {
    /**
     * 更新付款年月
     * @param servicePointWithdraw
     */
    void updatePayForYearMonth(ServicePointWithdraw servicePointWithdraw);

    /**
     * 付款确认获取
     * @param id
     * @return
     */
    ServicePointWithdraw getForConfirm(@Param(value = "id")long id);

    /**
     * 获取处理中的银行列表
     * @param paymentType
     * @return
     */
    List<Integer> getProcessBankList(@Param(value = "paymentType")Integer paymentType);

    /**
     * 获取一条记录ID，用于判断记录是否存在
     * @param servicePointWithdraw
     * @return
     */
    Long getOneId(ServicePointWithdraw servicePointWithdraw);

    /**
     * 获取正在处理中的网点提现记录ID，用于判断是否重复提现申请
     * @param ServicePointId
     * @return
     */
    Long getProcessOneId(@Param(value = "id") Long ServicePointId);

    /**
     * 获取付款确认列表
     * @param servicePointWithdraw
     * @return
     */
    List<ServicePointPayCondition> getInvoiceConfirmList(ServicePointWithdraw servicePointWithdraw);

    /**
     *
     * @param servicePointWithdraw
     * @return
     */
    //List<ServicePointWithdraw> getInvoiceConfirmDetailList(ServicePointWithdraw servicePointWithdraw);  //mark on 2020-2-16

    /**
     *
     * @param servicePointWithdraw
     * @return
     */
    List<ServicePointWithdraw> getInvoiceConfirmDetailListWithoutServicePoint(ServicePointWithdraw servicePointWithdraw);

    List<ServicePointWithdraw> getWithdrawByDateRangeForUpdateBalanceDetail(@Param("page") Page<ServicePointWithdraw> page, @Param("beginDate") String beginDate, @Param("endDate") String endDate);
}
