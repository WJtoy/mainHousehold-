package com.wolfking.jeesite.modules.sd.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.entity.OrderDetail;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderPendingSearchModel;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderSearchModel;
import com.wolfking.jeesite.modules.sd.entity.viewModel.RepeateOrderVM;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单job
 *
 * @author Ryan
 * @date 2019-09-14
 */
@Mapper
public interface OrderTaskDao extends LongIDCrudDao<Order> {

    /**
     * 定时处理将未即时自动对账的订单转为手动对账
     * @param beginDate 对账开始日期
     * @param endDate   对账结束日期
     */
    public void updateToManualCharge(@Param("quarter") String quarter,@Param("beginDate") Date beginDate ,@Param("endDate") Date endDate);

    public List<RepeateOrderVM> getOrderCreateInfo(@Param("quarter") String quarter, @Param("quarters") List<String> quarters,
                                                   @Param("beginDate") Date beginDate, @Param("endDate") Date endDate,
                                                   @Param("minCustomerId") Long minCustomerId, @Param("maxCustomerId") Long maxCustomerId
                                                        );
}
