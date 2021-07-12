package com.wolfking.jeesite.modules.sd.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.entity.OrderDetail;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderSearchModel;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderServicePointSearchModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 *
 */
@Mapper
public interface ServicePointOrderListDao extends LongIDCrudDao<Order> {


    /**
     * 催单
     */
    List<Order> findReminderOrderList(OrderServicePointSearchModel searchModel);

     /**
     * 获取网点的未预约工单列表
     */
    List<Order> findServicePointNoAppointmentOrderList(OrderServicePointSearchModel searchModel);

    /**
     * 获取网点的预约到期工单列表
     */
    List<Order> findServicePointArriveAppointmentOrderList(OrderServicePointSearchModel searchModel);

    /**
     * 获取网点的预约超期工单列表
     */
    List<Order> findServicePointPassAppointmentOrderList(OrderServicePointSearchModel searchModel);

    /**
     * 获取网点的停滞工单列表
     */
    List<Order> findServicePointPendingOrderList(OrderServicePointSearchModel searchModel);

    /**
     * 获取网点的待完成工单列表
     */
    List<Order> findServicePointServicedOrderList(OrderServicePointSearchModel searchModel);

    /**
     * 获取网点的待回访工单列表
     */
    List<Order> findServicePointAppCompletedOrderList(OrderServicePointSearchModel searchModel);

    /**
     * 获取网点的未完成工单列表
     */
    List<Order> findServicePointUncompletedOrderList(OrderServicePointSearchModel searchModel);

    /**
     * 获取网点的所有工单
     */
    List<Order> findServicePointAllOrderList(OrderServicePointSearchModel searchModel);

    /**
     * 获取网点的完成工单列表
     */
    List<Order> findServicePointCompletedOrderList(OrderServicePointSearchModel searchModel);

    /**
     * 获取工单的实际服务明细（只返回指定网点或师傅的实际服务明细）
     * 使用时间：工单数量应该控制在20以内，以免影响效率
     */
    List<OrderDetail> getOrderDetailListByOrderIds(@Param("orderIds") List<Long> orderIds,
                                                   @Param("servicePointId") Long servicePointId,
                                                   @Param("engineerId") Long engineerId);

    /**
     * 获取网点等配件工单列表
     */
    List<Order> findServicePointWaitingAccessoryList(OrderServicePointSearchModel searchModel);

    /**
     * 获取网点退单工单列表
     */
    List<Order> findServicePointReturnedList(OrderServicePointSearchModel searchModel);

}
