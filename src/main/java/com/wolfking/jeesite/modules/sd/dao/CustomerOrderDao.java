package com.wolfking.jeesite.modules.sd.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.sd.entity.LongTwoTuple;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.entity.OrderCondition;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderSearchModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


@Mapper
public interface CustomerOrderDao extends LongIDCrudDao<Order> {

    /**
     * 客户待发配件的工单列表
     */
    List<Order> getWaitingPartsOrderList(OrderSearchModel searchModel);

    /**
     * 客户处理中工单列表
     */
    List<Order> getProcessingOrderList(OrderSearchModel searchModel);

    /**
     * 客户已完成工单列表
     */
    List<Order> getCompletedOrderList(OrderSearchModel searchModel);

    /**
     * 客户已完成工单列表
     */
    List<Order> getCanceledOrderList(OrderSearchModel searchModel);

    /**
     * 客户已完成工单列表
     */
    List<Order> getReturnedOrderList(OrderSearchModel searchModel);

    /**
     * 客户已完成工单列表
     */
    List<Order> getAllOrderList(OrderSearchModel searchModel);

    /**
     * 客户已完成工单列表
     * 关联sd_order_head,取代sd_order
     */
    List<Order> getNewAllOrderList(OrderSearchModel searchModel);

    /**
     * 客户已完成工单列表

    List<Order> getOrderListByIds(@Param("quarters") Map<String,List<Long>> quarters);
     */
    /**
     * 客户已完成工单列表
     */
    List<Order> getComplainedOrderList(OrderSearchModel searchModel);

    /**
     * 获取工单的投诉状态
     */
    List<LongTwoTuple> getComplainStatusByOrderIds(@Param("quarter") String quarter,
                                                   @Param("orderIds") List<Long> orderIds);

    /**
     * 客户催单工单列表
     */
    List<Order> getReminderOrderLit(OrderSearchModel searchModel);

    /**
     * 查询待审核工单列表
     */
    List<OrderCondition> getWaitingApproveOrderList(OrderSearchModel searchModel);


}
