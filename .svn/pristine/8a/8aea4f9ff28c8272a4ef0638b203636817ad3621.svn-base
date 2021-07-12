package com.wolfking.jeesite.modules.sd.entity.viewModel;

import com.google.common.collect.Lists;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.config.Global;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.ProductCategory;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * 异常处理查询模型
 * 
 * @author Ryan
 */
@Data
@NoArgsConstructor
public class OrderPendingSearchModel extends RegionSearchModel<OrderPendingSearchModel> {

	private static final long serialVersionUID = 1L;

	private Boolean valid = true;
	private int dataSource = 0; //数据来源，如天猫，快可立下单
	private String quarter; // 分片
	private String orderNo = "";// 订单号
	private Customer customer;//上游客户(订单来源)

	// 用户
	private String userName = "";// 用户名
	private String servicePhone = "";// 用户实际联络电话
	private int isPhone = 0; //是否是合法的手机号码
	private Integer productQty = 0;//数量
	private String productName = "";//产品名称
	private List<String> productIds = Lists.newArrayList(); //
	private ProductCategory category = new ProductCategory(); //产品类别

	private Date completeBegin;//完成开始日期
	private Date completeEnd;//完成结束日期

	//查询时是否按客户负责的客户做子查询，如果sys_user_customer有记录，且customer=null，则加子查询
	private Integer subQueryUserCustomer = 0;
	private Integer subQueryUserArea = 0;

	private int orderNoSearchType = 0;//工单单号搜索类型
	//客户类型 2019/12/11
	private Integer customerType;
	private Integer rushType;//可突击订单

	private Integer kefuType; //客服类型

	public int getOrderNoSearchType(){
		if (this.orderNo != null){
			this.orderNo = this.orderNo.trim().toUpperCase();
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

	public int getIsPhone(){
		if (StringUtils.isNotBlank(this.servicePhone)){
			if("".equalsIgnoreCase(StringUtils.isPhoneWithRelaxed(this.servicePhone))){
				this.isPhone = 1;
			}
		}
		return this.isPhone;
	}
}