package com.wolfking.jeesite.modules.api.entity.sd;

import com.google.gson.annotations.JsonAdapter;
import com.wolfking.jeesite.modules.api.entity.sd.adapter.RestOrderAdapter;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.sd.entity.OrderSuspendFlagEnum;
import com.wolfking.jeesite.modules.sd.entity.OrderSuspendTypeEnum;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;

import java.util.Date;

/**
 * 订单基础类
 * 只包含app列表中显示的数据
 */
@JsonAdapter(RestOrderAdapter.class)
public class RestOrder {

    // Fields
    private int dataSource;//数据源
    private Long orderId;
    private String quarter;//数据库分片
    private String orderNo = "";// 订单号
    private String userName= "";//用户姓名
    private String servicePhone = "";//电话
    private String serviceAddress = "";//地址
    private Dict status;//状态
    private Date appointDate;//预约日期
    private Date acceptDate; //安维接单日期
    private Date closeDate;//完成日期(客评日期)
    private String remarks = "";//服务内容
    private ServicePoint servicePoint;//网点
    private User engineer; //安维
    private Integer orderServiceType = 1;//工单类型 1：安装单 2:维修单
    private String orderServiceTypeName = "";//服务类型
    private String areaId;//区域id
    private String areaName;
    private String subAreaId;//四级区域ID
    private String appCompleteType = "";//app完成类型
    private Integer isAppCompleted = 0;//是否app完成
    private Integer appAbnormalyFlag = 0; //异常标志
    private Integer pendingFlag = 0;//停滞标志
    private Integer pendingType = 0;//
    private int isComplained = 0;//投诉标识 18/01/24

    private Date createDate;//工单创建时间
    private Integer isNewOrder = 0; //是否是新单
    private Long urgentLevelId = 0L; //加急等级id
    private int reminderFlag = 0; //催单标识 0：无催单 1:待回复 2:已回复 3:已处理 4:完成 19/07/09
    //以下两个属性在reminderFalg = 1 时使用
    private int reminderItemNo = 0;//第几次催单 19/11/25
    private long reminderTimeoutAt =0 ;//催单超时时间(毫秒)
    private Integer suspendType = OrderSuspendTypeEnum.VALIDATE.getValue();
    private Integer suspendFlag = OrderSuspendFlagEnum.NORMAL.getValue();


    public RestOrder(){	}

    public RestOrder(Long orderId)
    {
        this.orderId = orderId;
    }

    public int getDataSource() {
        return dataSource;
    }

    public void setDataSource(int dataSource) {
        this.dataSource = dataSource;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getServicePhone() {
        return servicePhone;
    }

    public void setServicePhone(String servicePhone) {
        this.servicePhone = servicePhone;
    }

    public String getServiceAddress() {
        return serviceAddress;
    }

    public void setServiceAddress(String serviceAddress) {
        this.serviceAddress = serviceAddress;
    }

    public Dict getStatus() {
        return status;
    }

    public void setStatus(Dict status) {
        this.status = status;
    }

    public Date getAppointDate() {
        return appointDate;
    }

    public void setAppointDate(Date appointDate) {
        this.appointDate = appointDate;
    }

    public String getQuarter() {
        return quarter;
    }

    public void setQuarter(String quarter) {
        this.quarter = quarter;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Date getAcceptDate() {
        return acceptDate;
    }

    public void setAcceptDate(Date acceptDate) {
        this.acceptDate = acceptDate;
    }

    public User getEngineer() {
        return engineer;
    }

    public void setEngineer(User engineer) {
        this.engineer = engineer;
    }

    public Integer getOrderServiceType() {
        return orderServiceType;
    }

    public void setOrderServiceType(Integer orderServiceType) {
        this.orderServiceType = orderServiceType;
    }

    public String getOrderServiceTypeName() {
        return orderServiceTypeName;
    }

    public void setOrderServiceTypeName(String orderServiceTypeName) {
        this.orderServiceTypeName = orderServiceTypeName;
    }

    public String getAreaId() {
        return areaId;
    }

    public void setAreaId(String areaId) {
        this.areaId = areaId;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public String getSubAreaId() {
        return subAreaId;
    }

    public void setSubAreaId(String subAreaId) {
        this.subAreaId = subAreaId;
    }

    public ServicePoint getServicePoint() {
        return servicePoint;
    }

    public void setServicePoint(ServicePoint servicePoint) {
        this.servicePoint = servicePoint;
    }

    public String getAppCompleteType() {
        return appCompleteType;
    }

    public void setAppCompleteType(String appCompleteType) {
        this.appCompleteType = appCompleteType;
    }

    public Date getCloseDate() {
        return closeDate;
    }

    public void setCloseDate(Date closeDate) {
        this.closeDate = closeDate;
    }

    public Integer getIsAppCompleted() {
        return isAppCompleted;
    }

    public void setIsAppCompleted(Integer isAppCompleted) {
        this.isAppCompleted = isAppCompleted;
    }

    public Integer getAppAbnormalyFlag() {
        return appAbnormalyFlag;
    }

    public void setAppAbnormalyFlag(Integer appAbnormalyFlag) {
        this.appAbnormalyFlag = appAbnormalyFlag;
    }

    public Integer getPendingFlag() {
        return pendingFlag;
    }

    public void setPendingFlag(Integer pendingFlag) {
        this.pendingFlag = pendingFlag;
    }

    public Integer getPendingType() {
        return pendingType;
    }

    public void setPendingType(Integer pendingType) {
        this.pendingType = pendingType;
    }

    public int getIsComplained() {
        return isComplained;
    }

    public void setIsComplained(int isComplained) {
        this.isComplained = isComplained;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Integer getIsNewOrder() {
        return isNewOrder;
    }

    public void setIsNewOrder(Integer isNewOrder) {
        this.isNewOrder = isNewOrder;
    }

    public Long getUrgentLevelId() {
        return urgentLevelId;
    }

    public void setUrgentLevelId(Long urgentLevelId) {
        this.urgentLevelId = urgentLevelId;
    }

    public int getReminderFlag() {
        return reminderFlag;
    }

    public void setReminderFlag(int reminderFlag) {
        this.reminderFlag = reminderFlag;
    }

    public int getReminderItemNo() {
        return reminderItemNo;
    }

    public void setReminderItemNo(int reminderItemNo) {
        this.reminderItemNo = reminderItemNo;
    }

    public long getReminderTimeoutAt() {
        return reminderTimeoutAt;
    }

    public void setReminderTimeoutAt(long reminderTimeoutAt) {
        this.reminderTimeoutAt = reminderTimeoutAt;
    }

    public Integer getSuspendType() {
        return suspendType;
    }

    public void setSuspendType(Integer suspendType) {
        this.suspendType = suspendType;
    }

    public Integer getSuspendFlag() {
        return suspendFlag;
    }

    public void setSuspendFlag(Integer suspendFlag) {
        this.suspendFlag = suspendFlag;
    }
}
