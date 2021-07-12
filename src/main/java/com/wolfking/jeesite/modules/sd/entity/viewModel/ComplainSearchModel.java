package com.wolfking.jeesite.modules.sd.entity.viewModel;

import com.wolfking.jeesite.common.config.Global;
import com.wolfking.jeesite.common.persistence.LongIDDataEntity;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.ObjectUtils;

import java.util.Date;
import java.util.List;

/**
 * 头查询模型
 * 
 * @author Ryan
 */
@Data
@NoArgsConstructor
public class ComplainSearchModel extends RegionSearchModel<ComplainSearchModel> {
	//检查是否合法
	private Boolean isValid = true;
	private String quarter = "";//数据库分片
	private Long orderId;
	private String orderNo = "";// 订单号
	private String complainNo = "";//投诉单号
	private Customer customer;//客户，厂商
	private ServicePoint servicePoint; //安维网点

	// 用户
	private String userName = "";// 用户名
	private String userPhone = "";// 用户电话
	private int isPhone = 0; //是否是合法的手机号码
	private String complainBy = "";// 投诉人
	private Dict complainType;//投诉类型
	private Dict status;//状态
	private User kefu;//客服
	//投诉对象
	private Dict complainObject;
	private Double complainObjectValue;//用来接收页面的字典值进行运算后传参数给XML
	//投诉项目
	private Dict complainItem;
	private Double complainItemValue;//用来接收页面的字典值进行运算后传参数给XML

	private String searchType;//查询类型
	private Date beginDate;//投诉开始日期
	private Date endDate;//投诉结束日期

	//责任对象
	private Dict judgeObject;
	private Double judgeObjectValue;
	//责任项目
	private Dict judgeItem;
	private Double judgeItemValue;

	private User judgeBy;//判定人
	private Date judgeDate;//判定日期

	private Date completeBeginDate;//结案日期
	private Date completeEndDate;//结束日期

	private int orderNoSearchType = 0;//工单单号搜索类型
	private int complainNoSearchType = 0;//投诉单号搜索类型

	private Date currentTime; //当前时间
	//产品类别
	private Long productCategoryId = 0L;
	//用户产品类别列表
	private List<Long> userProductCategoryList;

	//1: 关联 sys_user_customer
	private Integer subQueryUserCustomer = 0;
	private Integer subQueryUserArea = 0;
	//客户类型
	private Integer customerType;
	//可突击订单 0:非突击区域订单 1:突击区域订单
	private Integer rushType;

	//区域属性
	private Integer kefuType;

	private Long salesId; //业务员
	private Integer subUserType = 0;//子账号类型
	//线下单客户列表，跟单及业务查询使用
	private List<Long> offlineCustomerList;

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

	public int getComplainNoSearchType(){
		if (this.complainNo != null){
		    this.complainNo = this.complainNo.toUpperCase().trim();
		    /*
			String complainNoPrefix = Global.getConfig("ComplainNoPrefix");
			if (complainNo.length() == 11){
				complainNoSearchType = 1;
			}else if (complainNo.startsWith(complainNoPrefix)){
				complainNoSearchType = 2;
			}*/
            if (this.complainNo.length() == 11){
                complainNoSearchType = 1;
            }else {
                complainNoSearchType = 2;
            }
		}
		return this.complainNoSearchType;
	}

	public int getIsPhone(){
		if (StringUtils.isNotBlank(this.userPhone)){
			if("".equalsIgnoreCase(StringUtils.isPhoneWithRelaxed(this.userPhone))){
				this.isPhone = 1;
			}
		}
		return this.isPhone;
	}



}