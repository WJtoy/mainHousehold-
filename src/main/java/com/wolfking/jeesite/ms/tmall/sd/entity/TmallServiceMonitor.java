package com.wolfking.jeesite.ms.tmall.sd.entity;

import com.wolfking.jeesite.common.persistence.LongIDDataEntity;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;



/**
 * 天猫预警记录表
 * @date 2018-09-13
 */
@Slf4j
@Data
public class TmallServiceMonitor extends LongIDDataEntity<TmallServiceMonitor>{

	/**
	 * 订单id
	 */
	private Long orderId;

	/**
	 * 天猫预警id
	 */
	private Long monitorId;

	//区域id(区县级)
	private Long areaId;

	//区域名称(包含省市区)
	private String areaName;

	/**
	 * 等级 1-预警 2-警告 3-严重
	 */
	private Integer level;

	/**
	 * 预警时间
	 */
	private Date gmtDate;

	/**
	 * 天猫预警标记 1-预警，未反馈 2-预警，已反馈
	 */
	private Integer status = 1;

	/**
	 * 预警内容
	 */
	private String content;

	/**
	 *反馈人id
	 */
	private Long replierId = 0l;

	/**
	 *反馈人姓名
	 */
	private String replierName ="";

	/**
	 *反馈时间
	 */
	private Date replyDate;

	/**
	 *反馈内容
	 */
	private String replyContent;

	/**
	 *预警规则id
	 */
	private String ruleId;

	/**
	 *服务类型
	 */
	private String serviceCode;

	/**
	 *订单号
	 */
	private String orderNo;

	/**
	 *分片
	 */
	private String quarter;

	private Long customerId = 0L;

	private Long provinceId = 0L;

	private Long cityId = 0L;

	private Integer canRush = 0;

	private  Long productCategoryId = 0L;

	/**
	 * 客服类型
	 * */
	private Integer kefuType = 0;

}