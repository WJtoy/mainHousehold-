package com.wolfking.jeesite.modules.sd.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.entity.OrderDetail;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderPendingSearchModel;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderSearchModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 客服订单数据访问接口
 *
 * @author Ryan
 * @date 2018-06-21
 */
@Mapper
public interface KefuOrderDao extends LongIDCrudDao<Order> {

    //待派单订单列表
    //List<Order> findKefuPlaningOrderList(OrderSearchModel searchModel);


    /**
     * 退单待审核列表
     */
    List<Order> getOrderReturnApproveList(OrderSearchModel searchModel);

    /**
     * 新迎燕退单待审核列表
    List<Order> getXYYOrderReturnApproveList(OrderSearchModel searchModel);
     */

    /**
     * 异常处理列表
     */
    List<Order> getExceptionHandlingList(OrderPendingSearchModel searchModel);

    /**
     * 获取工单的上门服务项
     */
    List<OrderDetail> getOrderDetailByOrderIds(@Param("quarter") String quarter,
                                               @Param("orderIds") List<Long> orderIds);

}
