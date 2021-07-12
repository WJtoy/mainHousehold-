package com.wolfking.jeesite.modules.sd.entity.viewModel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wolfking.jeesite.common.config.redis.GsonIgnore;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.entity.OrderReturnComplete;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 退换货完工提交数据类
 * 
 * @author Ryan
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReturnCompleteModel implements Serializable{

	private Long orderId;		// id
	private String quarter = "";//数据库分片
	private Integer dataSource; // 数据源
	private Long productId;
	private String productName;
	private Long b2bOrderId;	//b2b子系统的主键
	private String b2bOrderNo;	//第三方订单号
	private Integer orderServiceType; //订单类型

	private String remarks = "";//备注

	@GsonIgnore
	@JsonIgnore
	private User user;

	private Date operateDate;

	//项目
	List<OrderReturnComplete> items;
	// 完工类型
	private String completeType = "";
}