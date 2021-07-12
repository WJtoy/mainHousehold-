/**
 * Copyright &copy; 2014-2014 All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.wolfking.jeesite.modules.sd.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.sd.entity.Feedback;
import com.wolfking.jeesite.modules.sd.entity.FeedbackItem;
import com.wolfking.jeesite.modules.sd.entity.OrderAttachment;
import com.wolfking.jeesite.modules.sys.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * 附件dao接口
 */
@Mapper
public interface OrderAttachmentDao extends LongIDCrudDao<OrderAttachment> {

	/**
	 * 按id返回附件
	 * @param id
	 * @return
	 */
	public OrderAttachment getById(@Param("id")  Long id,@Param("quarter") String quarter);

	/**
	 * 按订单id返回订单下所有附件
	 * @param orderId
	 * @return
	 */
	public List<OrderAttachment> getByOrderId(@Param("orderId") Long orderId,@Param("quarter") String quarter);

	/**
	 * 按订单id清除所有完成照片
	 * @param orderId
	 * @param quarter
	 */
	public void deleteByOrderId(@Param("orderId") Long orderId,@Param("quarter") String quarter);

}
