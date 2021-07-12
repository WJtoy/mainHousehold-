/**
 * Copyright &copy; 2014-2014 All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.wolfking.jeesite.modules.sd.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.sd.entity.OrderCrush;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderCrushSearchVM;
import com.wolfking.jeesite.modules.sys.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * 突击单
 */
@Mapper
public interface OrderCrushDao extends LongIDCrudDao<OrderCrush> {


	/**
	 * 新建突击单
	 */
	void insertOrderCrush(OrderCrush entity);

	/**
	 * 分页查询突击订单列表
	 */
	List<OrderCrush> findPageList(OrderCrushSearchVM searchEntity);

	/**
	 * 查找突击订单列表 (旧方法)

	List<OrderCrush> findOrderCrushList(OrderCrushSearchVM entity);
	 */

	/**
	 * 查找订单下的突击订单列表
	 */
	List<OrderCrush> findCrushListOfOrder(OrderCrush entity);

	/**  2020-05-24
	 * 根据突击单区县或街道ID读取同区域以往派单记录

	List<HistoryPlanOrderModel> findOrderListOfCrush(OrderCrushSearchVM searchModel);

	List<HistoryPlanOrderModel> getOrderServiceItemList(@Param("quarter") String quarter, @Param("orderIds") List<Long> orderIds);
	 */

	/**
	 * 完成突击单
	 */
	int closeOrderCursh(OrderCrush entity);

	/**
	 * 按订单关闭突击单突击单
	 */
	int closeOrderCurshByOrderId(
			@Param("orderId") long orderId,
			@Param("quarter") String quarter,
			@Param("status") int status,
			@Param("closeRemark") String closeRemark,
			@Param("closeBy") User closeBy,
			@Param("closeDate") Date closeDate,
			@Param("timeLiness") double timeLiness
	);

	/**
	 * 更新突击单
	 */
	int updateOrderCrush(OrderCrush entity);

	int updateOrderCrushLastFlagToZero(@Param("quarter") String quarter,
										@Param("orderId") Long orderId,
										@Param("exceptId") Long exceptId);

	/**
	 * 获取突击单(不包含网点信息)
	 */
	OrderCrush getOrderCrushNoDetailById(@Param("id") Long id, @Param("quarter") String quarter);

	/**
	 * 获取完整突击单
	 */
	OrderCrush getOrderCrushById(@Param("id") Long id, @Param("quarter") String quarter);

	/**
	 * 读取最大序号
	*/
	Integer getMaxTimes(@Param("orderId") Long orderId, @Param("quarter") String quarter);

	/**
	 * 查询未完成的突击单ID
	 */
	Long getOpenOrderCrushId(@Param("orderId") Long orderId, @Param("quarter") String quarter);

	/**
	 * 活动订单当前暂存的突击单
	 */
	OrderCrush getTempSaveOrderCrush(@Param("orderId") Long orderId, @Param("quarter") String quarter);


	/**
	 * 修改街道,上门地址
	 */
	void updateAddress(HashMap<String, Object> condition);

	/**
	 * 根据工单号获取未关闭突击单
	 */
	OrderCrush getOrderCrushByOrderId(@Param("orderId") Long orderId, @Param("quarter") String quarter);



}
