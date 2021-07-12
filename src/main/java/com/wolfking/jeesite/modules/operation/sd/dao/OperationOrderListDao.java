package com.wolfking.jeesite.modules.operation.sd.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.md.entity.ServicePointFinance;
import com.wolfking.jeesite.modules.sd.entity.LongTwoTuple;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.entity.viewModel.HistoryPlanOrderModel;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderSearchModel;
import com.wolfking.jeesite.ms.tmall.sd.entity.TmallAnomalyRecourse;
import com.wolfking.jeesite.ms.tmall.sd.entity.TmallServiceMonitor;
import com.wolfking.jeesite.ms.tmall.sd.entity.ViewModel.TmallAnomalyRecourseSearchVM;
import com.wolfking.jeesite.ms.tmall.sd.entity.ViewModel.TmallServiceMonitorSearchVM;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 运营部门订单数据访问接口
 *
 * @author wangshoujiang
 * @date 2021-2-22
 */
@Mapper
public interface OperationOrderListDao extends LongIDCrudDao<Order> {

    //region 普通列表

    /**
     * 待接/待派工单列表
     */
    List<Order> findKefuPlaningOrderList(OrderSearchModel searchModel);

    /**
     * 没有预约的工单列表
     */
    List<Order> findKefuNoAppointmentOrderList(OrderSearchModel searchModel);

    /**
     * 预约到期的工单列表
     */
    List<Order> findKefuArriveAppointmentOrderList(OrderSearchModel searchModel);

    /**
     * 预约超期的工单列表
     */
    List<Order> findKefuPassAppointmentOrderList(OrderSearchModel searchModel);

    /**
     * 停滞订单列表(预约、停滞、待跟进未到期的工单)
     */
    List<Order> findKefuPendingOrderList(OrderSearchModel searchModel);

    /**
     * 待回访的工单列表（已上门并且没有设置待跟进的工单)
     */
    List<Order> findKefuServicedOrderList(OrderSearchModel searchModel);

    /**
     * 回访失败的工单列表（已上门并且没有设置待跟进的工单 & subStatus = 75)
     */
    List<Order> findKefuFollowUpFailOrderList(OrderSearchModel searchModel);

    /**
     * 未完成的工单列表
     */
    List<Order> findKefuUncompletedOrderList(OrderSearchModel searchModel);

    /**
     * 所有的工单列表
     */
    List<Order> findKefuAllOrderList(OrderSearchModel searchModel);

    /**
     * 完成订单列表
     */
    List<Order> findKefuCompletedOrderList(OrderSearchModel searchModel);

    //endregion 普通列表

    //region 特殊列表

    /**
     * 天猫求助单
     */
    List<TmallAnomalyRecourse> findKefuTmallAnomalyList(TmallAnomalyRecourseSearchVM searchModel);

    /**
     * 天猫预警单
     */
    List<TmallServiceMonitor> findKefuTmallServiceMonitorList(TmallServiceMonitorSearchVM searchModel);

    /**
     * 突击订单列表
     */
    List<Order> findKefuRushingOrderList(OrderSearchModel searchModel);

    /**
     * 投诉单列表
     */
    List<Order> findKefuComplainOrderList(OrderSearchModel searchModel);

    /**
     * 催单列表
     */
    List<Order> findReminderOrderLit(OrderSearchModel searchModel);

    /**
     * 根据区县或街道分页查询历史派单列表
     */
    List<HistoryPlanOrderModel> findHistoryPlaningOrderList(OrderSearchModel searchModel);

    //endregion 特殊列表

    /**
     * 获取网点的余额信息
     */
    List<ServicePointFinance> getBalanceInfoByServicePointIds(@Param("servicePointIds") List<Long> servicePointIds);

    /**
     * 获取工单的投诉状态
     */
    List<LongTwoTuple> getComplainStatusByOrderIds(@Param("quarter") String quarter,
                                                   @Param("orderIds") List<Long> orderIds);

    /**
     * 退单待审核列表
     */
    List<Order> getOrderReturnApproveList(OrderSearchModel searchModel);


}
