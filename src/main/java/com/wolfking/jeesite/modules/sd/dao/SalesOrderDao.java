package com.wolfking.jeesite.modules.sd.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.sd.entity.LongTwoTuple;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderSearchModel;
import com.wolfking.jeesite.modules.sd.entity.viewModel.SalesOrderSearchResultVM;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
public interface SalesOrderDao extends LongIDCrudDao<Order> {

    /**
     * 业务待发配件的工单列表
     */
    List<SalesOrderSearchResultVM> getWaitingPartsOrderIdList(OrderSearchModel searchModel);

    List<Order> getWaitingPartsOrderList(@Param("quarter") String quarter,@Param("orderIds") List<Long> orderIds);

    /**
     * 业务处理中工单列表
     */
    List<Order> getProcessingOrderList(OrderSearchModel searchModel);

    /**
     * 业务已完成工单列表
     */
    List<Order> getCompletedOrderList(OrderSearchModel searchModel);

    /**
     * 业务已完成工单列表
     */
    List<Order> getCanceledOrderList(OrderSearchModel searchModel);

    /**
     * 业务已完成工单列表
     */
    List<Order> getReturnedOrderList(OrderSearchModel searchModel);

    /**
     * 业务已完成工单列表
     */
    List<Order> getAllOrderList(OrderSearchModel searchModel);

    /**
     * 业务投诉工单列表
     */
    List<Order> getComplainedOrderList(OrderSearchModel searchModel);

    /**
     * 业务催单工单列表
     */
    List<Order> getReminderOrderLit(OrderSearchModel searchModel);

    /**
     * 获取工单的投诉状态
     */
    List<LongTwoTuple> getComplainStatusByOrderIds(@Param("quarter") String quarter,
                                                   @Param("orderIds") List<Long> orderIds);


}
