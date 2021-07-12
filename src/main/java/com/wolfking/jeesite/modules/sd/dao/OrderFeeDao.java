package com.wolfking.jeesite.modules.sd.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.entity.OrderFee;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * 订单费用表
 * @author Ryan
 * @date 2020-04-03
 */
@Mapper
public interface OrderFeeDao extends LongIDCrudDao<OrderFee> {

    /**
     * 对账后更新财务网点其他扣费
     * @param orderId
     * @param quarter
     * @param engineerTaxFee    扣点费
     * @param engineerInfoFee   平台费
     * @return
     */
    int updateFeeAfterCharge(@Param("orderId") long orderId,
                             @Param("quarter") String quarter,
                             @Param("engineerTaxFee") double engineerTaxFee,
                             @Param("engineerInfoFee") double engineerInfoFee,
                             @Param("engineerDeposit") double engineerDeposit);

    /**
     * 更新好评费
     */
    int updatePraiseFee(Map<String,Object> params);

}
