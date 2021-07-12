package com.wolfking.jeesite.modules.sd.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.sd.entity.LongTwoTuple;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderSearchModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 客服主管订单数据访问接口
 *
 * @author Ryan
 * @date 2018-06-21
 */
@Mapper
public interface ServiceLeaderOrderDao extends LongIDCrudDao<Order> {

    /**
     * 延时工单列表
     */
    //List<Order> getDelayedOrderlist(OrderSearchModel searchModel);
    //List<Order> getDelayedOrderlistNew(OrderSearchModel searchModel);

    /**
     * 爽约工单列表
     */
    List<Order> getBrokeAppointmentOrderlist(OrderSearchModel searchModel);
    List<Order> getBrokeAppointmentOrderlistNew(OrderSearchModel searchModel);

    /**
     * 被投诉工单列表
     */
    //List<Order> getComplainedOrderlist(OrderSearchModel searchModel);
    //List<Order> getComplainedOrderlistNew(OrderSearchModel searchModel);

    /**
     * 远程工单列表
     */
    //List<Order> getTravelOrderlist(OrderSearchModel searchModel);
    //List<Order> getTravelOrderlistNew(OrderSearchModel searchModel);

    /**
     * 获取工单的投诉状态
     */
    List<LongTwoTuple> getComplainStatusByOrderIds(@Param("quarter") String quarter,
                                                   @Param("orderIds") List<Long> orderIds);


}
