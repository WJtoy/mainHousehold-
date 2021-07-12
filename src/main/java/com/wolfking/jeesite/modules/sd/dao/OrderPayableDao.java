/**
 * Copyright &copy; 2014-2014 All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.wolfking.jeesite.modules.sd.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.sd.entity.OrderPayable;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订单应付表
 */
@Mapper
public interface OrderPayableDao extends LongIDCrudDao<OrderPayable> {

	/**
	 * 按订单id读取
	 * @param orderId
	 * @return
	 */
	public List<OrderPayable> getByOrderId(@Param("orderId") long orderId, @Param("quarter") String quarter);

	/**
	 * 更新
	 */
	public int updateByItem(@Param("orderId") long orderId, @Param("quarter") String quarter, @Param("itemNo") int itemNo,
                            @Param("amount") double amount, @Param("formNo") String formNo, @Param("remark") String remark,
							@Param("updateBy") long updateBy,@Param("updateAt") long updateAt);

	/**
	 * 按应付项目切换有效状态
	 */
	public int switchEnabled(@Param("orderId") long orderId ,@Param("quarter") String quarter ,@Param("itemNo") String itemNo, @Param("delFlag") int delFlag,
							 @Param("updateBy") long updateBy,@Param("updateAt") long updateAt);

}
