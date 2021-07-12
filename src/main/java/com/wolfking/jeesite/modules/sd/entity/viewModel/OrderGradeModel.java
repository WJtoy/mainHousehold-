package com.wolfking.jeesite.modules.sd.entity.viewModel;


import com.google.common.collect.Lists;
import com.wolfking.jeesite.common.config.redis.GsonIgnore;
import com.wolfking.jeesite.modules.md.entity.Engineer;
import com.wolfking.jeesite.modules.md.entity.Grade;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.entity.OrderGrade;
import com.wolfking.jeesite.modules.sd.entity.OrderProcessLog;
import com.wolfking.jeesite.modules.sys.entity.User;

import java.io.Serializable;
import java.util.Date;
import java.util.List;


/**
 * 订单评分视图数据模型
 * @author Ryan Lu
 */

public class
OrderGradeModel implements Serializable{

	private static final long serialVersionUID = 1L;
	private String quarter = "";//分片
	private Long id;
	private Long orderId;
	private String orderNo="";//订单号
	private ServicePoint servicePoint;//网点
	private Engineer engineer;//安维
	private Integer autoGradeFlag = 0; //自动客评标记 1:自动客评
	//post values
	private String ids = "";
	private Order order;
	private Boolean checkOrderFee = true;//是否检查金额
	//private Boolean checkCanAutoCharge = true;//是否检查能否自动生成对账单
	//private Boolean canAutoCharge = false;//是否要生成对账单
	
	private List<OrderGrade> gradeList = Lists.newArrayList(); // 评价明细

	private Integer point = 0; //总评分

	private Double timeLiness = 0.0; //时效(小时)
	private Double timeLinessCharge = 0.0; //时效金额

	//2018/10/09 begin
	private User createBy;
	private Date createDate;
	private String content;//json内容
	private OrderProcessLog processLog;
	private int rushCloseFlag = 0;//突击单关闭标志，1:关闭
	//客户或者客户费用的跟踪进度信息
	private List<OrderProcessLog> feeProcessLogs = Lists.newArrayList();

	//2018/10/09 end

	public OrderGradeModel(){}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}
	
	public Engineer getEngineer() {
		return engineer;
	}

	public void setEngineer(Engineer engineer) {
		this.engineer = engineer;
	}

	
	public List<OrderGrade> getGradeList() {
		return gradeList;
	}

	public void setGradeList(List<OrderGrade> gradeList) {
		this.gradeList = gradeList;
	}

	/**
	 * @return the ids
	 */
	public String getIds() {
		return ids;
	}

	/**
	 * @param ids the ids to set
	 */
	public void setIds(String ids) {
		this.ids = ids;
	}

	public Long getOrderId() {
		return orderId;
	}

	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}

	public ServicePoint getServicePoint() {
		return servicePoint;
	}

	public void setServicePoint(ServicePoint servicePoint) {
		this.servicePoint = servicePoint;
	}

	public Integer getAutoGradeFlag() {
		return autoGradeFlag;
	}

	public void setAutoGradeFlag(Integer autoGradeFlag) {
		this.autoGradeFlag = autoGradeFlag;
	}

	public String getQuarter() {
		return quarter;
	}

	public void setQuarter(String quarter) {
		this.quarter = quarter;
	}

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	public Boolean getCheckOrderFee() {
		return checkOrderFee;
	}

	public void setCheckOrderFee(Boolean checkOrderFee) {
		this.checkOrderFee = checkOrderFee;
	}

	public Double getTimeLiness() {
		return timeLiness;
	}

	public void setTimeLiness(Double timeLiness) {
		this.timeLiness = timeLiness;
	}

	public Double getTimeLinessCharge() {
		return timeLinessCharge;
	}

	public void setTimeLinessCharge(Double timeLinessCharge) {
		this.timeLinessCharge = timeLinessCharge;
	}

	public Integer getPoint() {
		return point;
	}

	public void setPoint(Integer point) {
		this.point = point;
	}

	public User getCreateBy() {
		return createBy;
	}

	public void setCreateBy(User createBy) {
		this.createBy = createBy;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public OrderProcessLog getProcessLog() {
		return processLog;
	}

	public void setProcessLog(OrderProcessLog processLog) {
		this.processLog = processLog;
	}

	public int getRushCloseFlag() {
		return rushCloseFlag;
	}

	public void setRushCloseFlag(int rushCloseFlag) {
		this.rushCloseFlag = rushCloseFlag;
	}

	public List<OrderProcessLog> getFeeProcessLogs() {
		return feeProcessLogs;
	}

	public void setFeeProcessLogs(List<OrderProcessLog> feeProcessLogs) {
		this.feeProcessLogs = feeProcessLogs;
	}
}