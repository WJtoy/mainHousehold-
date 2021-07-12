package com.wolfking.jeesite.ms.tmall.sd.entity;

import com.google.gson.annotations.JsonAdapter;
import com.wolfking.jeesite.common.persistence.LongIDDataEntity;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.Product;
import com.wolfking.jeesite.modules.md.utils.CustomerSimpleAdapter;
import lombok.Data;

import java.util.Date;

/**
 * 天猫B2B工单查询视图模型
 * @author Ryan Lu
 */
@Data
public class TMallWorkcardSearchVM extends LongIDDataEntity<TMallWorkcardSearchVM> {

	private static final long serialVersionUID = 1L;

	//数据源
	private int dataSource = 0;

	//查询类型
	private String searchType = "";

	//工单处理标识
	private Integer processFlag;

	//厂商
	//private Customer customer;

	//店铺id
	private String shopId = "0";

	//用户名
	private String buyerName = "";

	//手机号
	private String buyerPhone = "";

	//用户地址
	private String buyerAddress = "";

	//产品
	private Product product;

	//品牌
	private String brand = "";

	//型号/规格
	private String modelNumber = "";

	//工单创建日期
	private Date gmtCreateStart = null;
	private Date gmtCreateEnd = null;

	//工单收货日期
	private Date receiveTimeStart = null;
	private Date receiveTimeEnd = null;

}