package com.wolfking.jeesite.modules.sd.entity.viewModel;

import com.wolfking.jeesite.common.persistence.IntegerRange;
import com.wolfking.jeesite.common.persistence.LongIDDataEntity;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;

import java.util.Date;

/**
 * 订单审核模型
 * 
 * @author Ryan
 */

public class OrderApproveModel {

	private static final long serialVersionUID = 1L;

	private String quarter = "";//数据库分片
	private String orderId = "";
	private String version = "";
	private String orderNo = "";
	private String remarks = "";


	// Constructors
	public OrderApproveModel() {
		super();
	}

	public String getQuarter() {
		return quarter;
	}

	public void setQuarter(String quarter) {
		this.quarter = quarter;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
}