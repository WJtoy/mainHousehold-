package com.wolfking.jeesite.modules.sd.entity.viewModel;

import com.google.common.collect.Lists;
import com.google.gson.annotations.JsonAdapter;
import com.wolfking.jeesite.common.config.Global;
import com.wolfking.jeesite.common.config.redis.GsonIgnore;
import com.wolfking.jeesite.common.mapper.adapters.DateTimeAdapter;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.md.utils.AreaSimpleAdapter;
import com.wolfking.jeesite.modules.md.utils.CustomerSimpleAdapter;
import com.wolfking.jeesite.modules.sd.entity.OrderCrush;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.entity.adapter.UserSimpleAdapter;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**
 * 突击单查询模型
 * @author Ryan
 * @date 2019-09-10
 */
@Data
@NoArgsConstructor
public class OrderCrushSearchVM extends RegionSearchModel<OrderCrushSearchVM>
{
	//检查是否合法
	private Boolean isValid = true;

	private long orderId;
	@GsonIgnore
	private String orderNo = "";//工单号
	private String quarter = "";//数据库分片，与工单相同

	private Long productCategoryId = 0L;
	private Integer orderServiceType = 0;//订单类型

	private String crushNo;//突击单号

	private Integer status;//状态

	@GsonIgnore
	private Date beginDate;
	@GsonIgnore
	private Date endDate;

	private String closeBy;//完成人

	private List<String> quarters;//分片列表

	private int orderNoSearchType = 0;//工单单号搜索类型
	private int crushNoSearchType = 0;//突击单单号搜索类型

	//1: 关联 sys_user_customer
	private Integer subQueryUserCustomer = 0;
	private Integer subQueryUserArea = 0;
	//客户类型
	private Integer customerType;
	private Integer rushType;

	//用户产品类别列表
	private List<Long> userProductCategoryList;

	public int getOrderNoSearchType(){
		if (this.orderNo != null){
			String orderNoPrefix = Global.getConfig("OrderPrefix");
			if (orderNo.length() == 14){
				orderNoSearchType = 1;
				String quarter = QuarterUtils.getOrderQuarterFromNo(orderNo);
				if(StringUtils.isNotBlank(quarter)){
					this.quarter = quarter;
				}
			}else if (orderNo.startsWith(orderNoPrefix)){
				orderNoSearchType = 2;
			}
		}
		return this.orderNoSearchType;
	}

	public int getCrushNoSearchType(){
		if (this.crushNo != null){
			this.crushNo = this.crushNo.toUpperCase().trim();
			if (this.crushNo.length() == 14){
				crushNoSearchType = 1;
			}else {
				crushNoSearchType = 2;
			}
		}
		return this.crushNoSearchType;
	}

}