package com.wolfking.jeesite.modules.sd.entity.viewModel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wolfking.jeesite.common.config.redis.GsonIgnore;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.entity.OrderCondition;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

/**
 * 客服完工视图模型
 * 
 * @author Ryan
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"order","user"})
public class KefuCompleteModel implements Serializable{

	private Long orderId;		// id
	private String orderNo = "";//订单号
	private String quarter = "";//数据库分片
	private String remarks = "";//备注
	private Integer dataSourceId; // 数据源

	@GsonIgnore
	@JsonIgnore
	private Order order;

	@GsonIgnore
	@JsonIgnore
	private User user;

	private Dict completeType;

	@GsonIgnore
	@JsonIgnore
	private Date operateDate;

	@GsonIgnore
	@JsonIgnore
	private Date buyDate;

}