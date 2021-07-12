package com.wolfking.jeesite.modules.sd.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.sd.entity.OrderReturnComplete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;


/**
 * 退换货完工
 */
@Mapper
public interface OrderReturnCompleteDao extends LongIDCrudDao<OrderReturnComplete> {

    List<OrderReturnComplete> getByOrderId(@Param("orderId") Long orderId, @Param("quarter") String quarter);

    OrderReturnComplete getById(@Param("id") Long id, @Param("quarter") String quarter);

    void updateSN(OrderReturnComplete entity);

    void updateEntity(OrderReturnComplete entity);

    int uploadSuccess(@Param("id") Long id,@Param("quarter") String quarter,@Param("updateBy") String updateBy,@Param("updateDate") Date updateDate);

}
