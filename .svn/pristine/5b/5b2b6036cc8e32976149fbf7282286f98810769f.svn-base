package com.wolfking.jeesite.modules.fi.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.modules.fi.entity.CustomerCurrency;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * Created by Jeff on 2017/4/19.
 */
@Mapper
public interface CustomerCurrencyDao extends LongIDCrudDao<CustomerCurrency> {

    public Long checkRepeate(@Param("currencyNo") String currencyNo,@Param("actionTypes") Integer[] actionTypes);
    /**
     *
     * @param currencyNo
     * @return
     */
    CustomerCurrency getByCurrencyNo(@Param("currencyNo") String currencyNo,@Param("actionType") Integer actionType);

    /**
     * 按订单单号和类型返回流水单据
     * @param orderNo 订单号
     * @param actionTypes 流水类型
     * @return
     */
    public List<CustomerCurrency> getByOrderNoAndActionTypes(@Param("orderNo") String orderNo,@Param("actionTypes") Integer[] actionTypes);

    /**
     * 更新处理类型
     * @param entity
     */
    void updateActionType(CustomerCurrency entity);

    /**
     * 获得指定日期的客户余额
     * @param customerId
     * @param currentDate 截止日期
     * @return
     */
    CustomerCurrency getLastBalance(@Param("customerId") long customerId,@Param("currentDate") Date currentDate);
    List<CustomerCurrency> findCurrencyList(CustomerCurrency entity);

    /**
     * 获取客户的冻结流水信息
     * @param customerId
     * @param salesId
     * @param beginDate
     * @param endDate
     * @param currencyType
     * @param currencyNo
     * @param page
     * @return
     */
    List<CustomerCurrency> getCustomerBlockAmountList(@Param("customerId") Long customerId,
                                                      @Param("salesId") Long salesId,
                                                      @Param("beginDate") Date beginDate,
                                                      @Param("endDate") Date endDate,
                                                      @Param("currencyType") Integer currencyType,
                                                      @Param("currencyNo") String currencyNo,
                                                      @Param("page") Page<CustomerCurrency> page);
}
