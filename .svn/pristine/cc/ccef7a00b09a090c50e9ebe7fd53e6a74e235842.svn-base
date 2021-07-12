/**
 * Copyright &copy; 2014-2014 All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.wolfking.jeesite.modules.sd.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.sd.entity.Feedback;
import com.wolfking.jeesite.modules.sd.entity.FeedbackItem;
import com.wolfking.jeesite.modules.sd.entity.viewModel.NoticeMessageItemVM;
import com.wolfking.jeesite.modules.ws.entity.WSFeedbackStats;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 问题反馈接口
 * @author ThinkGem
 * @version 2013-8-23
 */
@Mapper
public interface FeedbackDao extends LongIDCrudDao<Feedback> {

	/**
	 * 新增问题反馈
	 * @param feedback
	 */
	public void insertFeedback(Feedback feedback);

	/**
	 * 更新反馈主表
	 * @param map
	 */
	public void updateFeedback(HashMap<String,Object> map);

	/**
	 * 递增已上传图片数
	 * @param id
	 */
	public void incrAttachmentCount(@Param("id") Long id,@Param("quarter") String quarter);

	/**
	 * 按问题反馈id获得问题反馈单头
	 * @param id
	 * @return
	 */
	public Feedback getById(@Param("id") Long id,@Param("quarter") String quarter);

	/**
	 * 按订单id获得问题反馈单头
	 * @param orderId
	 * @return
	 */
	public Feedback getByOrderId(@Param("orderId") Long orderId,@Param("quarter") String quarter);

	/**
	 * 递增下一反馈明细楼层数
	 * @param id 问题反馈id
	 */
	public void incrNextFloor(@Param("id") Long id,@Param("quarter") String quarter);

	/**
	 * 获得下一楼层数
	 * @param id
	 * @return
	 */
	public Integer getNextFloor(@Param("id") Long id,@Param("quarter") String quarter);

	public void insertFeedbackItem(FeedbackItem item);

	/**
	 * 按问题反馈id获得问题反馈单头及详细的内容
	 * @param id
	 * @return
	 */
	public Feedback getWithItemsById(@Param("id") Long id,@Param("quarter") String quarter);

	/**
	 * 按订单id获得问题反馈单头及详细的内容
	 * @param orderId
	 * @return
	 */
	public Feedback getWithItemsByOrderId(@Param("orderId") Long orderId,@Param("quarter") String quarter);

	/**
	 * 读取反馈上传的图片列表
	 * @param feedbackId
	 * @return
	 */
	public List<FeedbackItem> getImageItems(@Param("feedbackId") Long feedbackId,@Param("quarter") String quarter);

	/**
	 * 读取问题反馈文本内容
	 * @param feedbackId
	 * @return
	 */
	public List<FeedbackItem> getFeedbackItems(@Param("feedbackId") Long feedbackId,@Param("quarter") String quarter);

	//region 消息

	/**
	 * 统计已分配客户的客服的数据(by customer)
	 */
	public List<Map<String, Object>> groupByKefuOfCustomer(@Param("quarters") List<String> quarters);

	/**
	 * 统计已分配客户的客服的数据(by customer)  //add on 2020-9-14
	*/
	List<Map<String, Object>> groupByKefuOfCustomerWithoutUserCustomer(@Param("customerIds") List<Long> customerIds,@Param("quarters") List<String> quarters);

	/**
	 * 统计未分配客户的客服的数据(by area)
	 */
	public List<Map<String, Object>> groupByKefuOfArea(@Param("quarters") List<String> quarters);

	/**
	 * 统计未分配客户的客服的数据(by area)  // add on 2020-9-14
	 */
	public List<Map<String, Object>> groupByKefuOfAreaWithoutUserCustomer(@Param("customerIds") List<Long> customerIds,@Param("quarters") List<String> quarters);


	/**
	 * 统计客户的数据
	 */
	public List<Map<String, Object>> groupByCustomer(@Param("quarters") List<String> quarters);

	void updateReadedNew(HashMap<String, Object> map);

	//endregion 问题反馈


}
