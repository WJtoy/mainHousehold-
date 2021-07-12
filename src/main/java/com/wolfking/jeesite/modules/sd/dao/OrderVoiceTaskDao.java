package com.wolfking.jeesite.modules.sd.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.entity.OrderVoiceTask;
import com.wolfking.jeesite.modules.sd.entity.viewModel.RepeateOrderVM;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 订单智能回访记录
 *
 * @author Ryan
 * @date 2019-01-15
 */
@Mapper
public interface OrderVoiceTaskDao extends LongIDCrudDao<OrderVoiceTask> {

    public OrderVoiceTask getByOrderId(@Param("quarter") String quarter,@Param("orderId") Long orderId);

    public OrderVoiceTask getBaseInfoByOrderId(@Param("quarter") String quarter,@Param("orderId") Long orderId);

    public Integer getVoiceTaskResult(@Param("quarter") String quarter,@Param("orderId") Long orderId);

    public Integer cancel(@Param("quarter") String quarter,@Param("orderId") Long orderId,@Param("updateDate") Long updateDate);

}
