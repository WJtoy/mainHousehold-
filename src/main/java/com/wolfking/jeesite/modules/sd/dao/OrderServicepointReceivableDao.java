/**
 * Copyright &copy; 2014-2014 All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.wolfking.jeesite.modules.sd.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.sd.entity.OrderServicepointReceivable;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 网点应收应付表
 */
@Mapper
public interface OrderServicepointReceivableDao extends LongIDCrudDao<OrderServicepointReceivable> {

	/**
	 * 按订单id读取
	 * @param orderId
	 * @param quarter 分片
	 * @param servicePointId 网点id ,有值按网点筛选
	 * @return
	 */
	public List<OrderServicepointReceivable> getByOrderId(@Param("orderId") long orderId, @Param("quarter") String quarter,@Param("itemNo") Integer itemNo,@Param("servicePointId") Long servicePointId);

	/**
	 * 按订单id+项目编号读取
	 * @param orderId
	 * @param quarter 分片
	 * @param itemNo 项目
	 * @return
	 */
	public List<OrderServicepointReceivable> getByItemNo(@Param("orderId") long orderId, @Param("quarter") String quarter,@Param("itemNo") int itemNo);

	/**
	 * 按订单+网点+应收应付项目更新
	 */
	public int updateByItemAndServicePoint(@Param("orderId") long orderId, @Param("quarter") String quarter, @Param("servicePointId") long servicePointId, @Param("itemNo") int itemNo,
										   @Param("amount") double amount, @Param("formNo") String formNo, @Param("remark") String remark,
										   @Param("updateBy") long updateBy,@Param("updateAt") long updateAt);

	/**
	 * 按id切换有效状态
	 */
	public int switchEnabled(@Param("orderId") long orderId, @Param("quarter") String quarter, @Param("servicePointId") long servicePointId, @Param("itemNo") int itemNo,
							 @Param("delFlag") int delFlag, @Param("updateBy") long updateBy,@Param("updateAt") long updateAt);

}
