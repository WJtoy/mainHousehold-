/**
 * Copyright &copy; 2014-2014 All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.wolfking.jeesite.modules.sd.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.sd.entity.LongTwoTuple;
import com.wolfking.jeesite.modules.sd.entity.OrderComplain;
import com.wolfking.jeesite.modules.sd.entity.OrderComplainAttachment;
import com.wolfking.jeesite.modules.sd.entity.OrderComplainLog;
import com.wolfking.jeesite.modules.sd.entity.viewModel.ComplainSearchModel;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 投诉单数据操作类
 */
@Mapper
public interface OrderComplainDao extends LongIDCrudDao<OrderComplain> {

	/**
	 * 按id返回附件
	 * @param id
	 * @return
	 */
	public OrderComplain getById(@Param("id") Long id, @Param("quarter") String quarter);

	/**
	 * 按订单号返回订单下所有投诉单
	 * @param orderId
	 * @return
	 */
	public List<OrderComplain> getByOrderNo(@Param("orderId") Long orderId,@Param("orderNo") String orderNo, @Param("quarter") String quarter);

	/**
	 * 按订单读取所有投诉单Id及状态  For B2B投诉单状态变更使用
	 * B2B投诉可以同时发起多个投诉单，订单投诉单状态需考虑多个投诉单情况
	 */
	public List<LongTwoTuple> getStatusByOrder(@Param("orderId") Long orderId, @Param("quarter") String quarter);

	/**
	 * 分页查询
	 * 2020-04-21 改用新语句
	 * @param model
	 * @return
	 */
	public List<OrderComplain> findComplainListNew(ComplainSearchModel model);

	/**
	 * 判断网点订单是否有投诉
	 * @param servicePointId	网点ID
	 * @param orderIds 			订单ID列表
	 * @return
	 */
	public List<Long> getOrderIdList(@Param("servicePointId") Long servicePointId,@Param("orderIds") List<Long> orderIds);

	/**
	 * 按订单id列表查询投诉单状态
	 * @param quarter
	 * @param orderIds
	 * @return
	 */
	public List<Map<String,Object>> getOrderComplainStatusByOrderIds(@Param("quarter") String quarter,@Param("orderIds") List<Long> orderIds);
	/**
	 * 更改投诉单
	 */
	public void UpdateOrderComplain(HashMap<String,Object> maps);

	/**
	 * 新增附件
	 * @param model
	 */
	public long insertAttachement(OrderComplainAttachment model);

    /**
     * 增加或减少判责附件数
     * @param id
     * @param quarter
     * @param judgeAttachmentQty
     * @return
     */
	public int incOrDescJudgeAttachQty(@Param("id") Long id,@Param("quarter") String quarter,@Param("judgeAttachmentQty") int judgeAttachmentQty);

	/**
	 * 获得附件id列表
	 * @param complainId
	 * @param quarter
	 * @param attachmentType
	 * @return
	 */
	public List<Long> getAttachmentIds(@Param("complainId") Long complainId, @Param("quarter") String quarter, @Param("attachmentType") Integer attachmentType);

	/**
	 * 获得附件列表
	 * @param complainId
	 * @param quarter
	 * @param attachmentType
	 * @return
	 */
	public List<OrderComplainAttachment> getAttachments(@Param("complainId") Long complainId, @Param("quarter") String quarter, @Param("attachmentType") Integer attachmentType);

	/**
	 * 获得单个附件
	 */
	public OrderComplainAttachment getAttachment(@Param("id") Long id, @Param("quarter") String quarter);


	/**
	 * 按订单id清除所有完成照片
	 * @param id
	 * @param quarter
	 */
	public int delAttachment(@Param("id") Long id, @Param("quarter") String quarter, @Param("updateBy") User updateBy, @Param("updateDate") Date updateDate);

	/**
	 * 根据投诉单ID撤销投诉单
	 * @param complainId
	 * @param quarter
	 */
	public void cancleComplain(@Param("complainId") Long complainId,@Param("quarter") String quarter);


	/**
	 * 根据投诉单Id 更新申诉
	 * @param complainId
	 * @param quarter
	 */
	public void updateComplainAppeal(@Param("complainId") Long complainId, @Param("quarter") String quarter, @Param("status") Dict status, @Param("appealRemar") String appealRemar,@Param("appealBy") User appealBy, @Param("appealDate") Date appealDate);

	//region log

	/**
	 * 新增日志
	 * @param log
	 */
	public void insertLog(OrderComplainLog log);

	public List<OrderComplainLog> findListByComplainId(@Param("complainId") Long complainId, @Param("quarter") String quarter);

	/**
	 * 根据id读取投诉单信息
	 * @param id
	 */
	OrderComplain getOrderComplainById(@Param("id") Long id);


	//endregion log

}
