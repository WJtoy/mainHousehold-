package com.wolfking.jeesite.ms.b2bcenter.sd.entity;

import com.kkl.kklplus.entity.b2bcenter.sd.*;
import com.kkl.kklplus.entity.canbo.sd.CanboOrderCompleted;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class B2BOrderStatusUpdateReqEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 数据源id
     */
    private Integer dataSourceId;

    /**
     * 快可立工单id
     */
    private Long orderId;

    /**
     * 快可立工单号
     */
    private String kklOrderNo;

    /**
     * B2B工单ID
     */
    private Long b2bOrderId;

    /**
     * B2B单号
     */
    private String b2bOrderNo;

    /**
     * B2B工单状态
     */
    private B2BOrderStatusEnum status;

    /**
     * 操作类型
     */
    private B2BOrderActionEnum actionType;

    /**
     * 操作人ID
     */
    private Long updaterId;

    /**
     * 操作人名称
     */
    private String updaterName;

    /**
     * 操作人电话
     */
    private String updaterMobile;

    /**
     * 操作时间
     */
    private Date updateDate;

    /**
     * 操作生效时间
     */
    private Date effectiveDate;

    /**
     * 安维师傅的唯一标识
     */
    private String engineerId;

    /**
     * 安维师傅
     */
    private String engineerName;

    /**
     * 安维电话
     */
    private String engineerMobile;

    /**
     * 描述
     */
    private String remarks;

    /**
     * 完工项目
     */
    private List<CanboOrderCompleted.CompletedItem> completedItems;
    private List<B2BOrderCompletedItem> orderCompletedItems;
    private B2BOrderPraiseItem orderPraiseItem;
    private B2BOrderValidateItem orderValidateItem;

    /**
     * 预约状态
     */
    private Integer appointmentStatus;

    /**
     * 安装状态
     */
    private Integer installStaus;

    /**
     * 网点ID
     */
    private Long servicePointId;

    private Double orderCharge;

    /**
     * 停滞类型
     */
    private String pendingType;
    /**
     * 实际的辅材费用总金额
     */
    private Double actualTotalSurcharge;

    /**
     * 实际上门服务项
     */
    private List<B2BOrderServiceItem> serviceItems;
    /**
     * 客户应收金额
     */
    private Double customerTotalCharge;
    /**
     * 对账时间
     */
    private Long chargeAt;
    /**
     * APP完成时间
     */
    private Long appCompleteDt;
    /**
     * 原因
     */
    private String b2bReason;
    /**
     * 经度
     */
    private Double longitude;
    /**
     * 纬度
     */
    private Double latitude;
    /**
     * 网点名称
     */
    private String servicePointName;
    /**
     * 验证码
     */
    private String verifyCode;

    private String extraField1;

    private B2BOrderStatusUpdateReqEntity(Builder builder) {
        this.dataSourceId = builder.dataSourceId;
        this.orderId = builder.orderId;
        this.kklOrderNo = builder.kklOrderNo;
        this.b2bOrderId = builder.b2bOrderId;
        this.b2bOrderNo = builder.b2bOrderNo;
        this.status = builder.status;
        this.actionType = builder.actionType;
        this.updaterId = builder.updaterId;
        this.updaterName = builder.updaterName;
        this.updaterMobile = builder.updaterMobile;
        this.updateDate = builder.updateDate;
        this.effectiveDate = builder.effectiveDate;
        this.engineerId = builder.engineerId;
        this.engineerName = builder.engineerName;
        this.engineerMobile = builder.engineerMobile;
        this.remarks = builder.remarks;
        this.completedItems = builder.completedItems;
        this.orderCompletedItems = builder.orderCompletedItems;
        this.orderPraiseItem = builder.orderPraiseItem;
        this.orderValidateItem = builder.orderValidateItem;
        this.appointmentStatus = builder.appointmentStatus;
        this.installStaus = builder.installStaus;
        this.servicePointId = builder.servicePointId;
        this.orderCharge = builder.orderCharge;
        this.pendingType = builder.pendingType;
        this.actualTotalSurcharge = builder.actualTotalSurcharge;
        this.serviceItems = builder.serviceItems;
        this.customerTotalCharge = builder.customerTotalCharge;
        this.chargeAt = builder.chargeAt;
        this.appCompleteDt = builder.appCompleteDt;
        this.b2bReason = builder.b2bReason;
        this.longitude = builder.longitude;
        this.latitude = builder.latitude;
        this.servicePointName = builder.servicePointName;
        this.verifyCode = builder.verifyCode;
        this.extraField1 = builder.extraField1;
    }

    /**
     * Builder实体构造工具类
     */
    public static class Builder {

        private Integer dataSourceId = 0;
        private String kklOrderNo = "";
        private Long orderId = 0L;
        private Long b2bOrderId = 0L;
        private String b2bOrderNo = "";
        private B2BOrderStatusEnum status;
        private B2BOrderActionEnum actionType = B2BOrderActionEnum.NONE;
        private Long updaterId = 0L;
        private String updaterName = "";
        private String updaterMobile = "";
        private Date updateDate = new Date();
        private Date effectiveDate;
        private String engineerId = "";
        private String engineerName = "";
        private String engineerMobile = "";
        private String remarks = "";
        private List<CanboOrderCompleted.CompletedItem> completedItems;
        private List<B2BOrderCompletedItem> orderCompletedItems;
        private B2BOrderPraiseItem orderPraiseItem;
        private B2BOrderValidateItem orderValidateItem;
        private Integer appointmentStatus = 0;
        private Integer installStaus = 0;
        private Long servicePointId = 0L;
        private Double orderCharge = 0.0;
        private String pendingType = "";
        private Double actualTotalSurcharge = 0.0;
        private List<B2BOrderServiceItem> serviceItems;
        private Double customerTotalCharge = 0.0;
        private Long chargeAt = 0L;
        private Long appCompleteDt = 0L;
        private String b2bReason = "";
        private Double longitude = 0.0;
        private Double latitude = 0.0;
        private String servicePointName = "";
        private String verifyCode = "";
        private String extraField1 = "";

        public Builder setDataSourceId(Integer dataSourceId) {
            this.dataSourceId = dataSourceId;
            return this;
        }

        public Builder setKklOrderNo(String kklOrderNo) {
            this.kklOrderNo = kklOrderNo;
            return this;
        }

        public Builder setOrderId(Long orderId) {
            this.orderId = orderId;
            return this;
        }

        public Builder setB2bOrderId(Long b2bOrderId) {
            this.b2bOrderId = b2bOrderId;
            return this;
        }

        public Builder setB2bOrderNo(String b2bOrderNo) {
            this.b2bOrderNo = b2bOrderNo;
            return this;
        }

        public Builder setStatus(B2BOrderStatusEnum status) {
            this.status = status;
            return this;
        }

        public Builder setActionType(B2BOrderActionEnum actionType) {
            this.actionType = actionType;
            return this;
        }

        public Builder setUpdaterId(Long updaterId) {
            this.updaterId = updaterId;
            return this;
        }

        public Builder setUpdaterName(String updaterName) {
            this.updaterName = updaterName;
            return this;
        }

        public Builder setUpdaterMobile(String updaterMobile) {
            this.updaterMobile = updaterMobile;
            return this;
        }

        public Builder setUpdateDate(Date updateDate) {
            this.updateDate = updateDate;
            return this;
        }

        public Builder setEffectiveDate(Date effectiveDate) {
            this.effectiveDate = effectiveDate;
            return this;
        }

        public Builder setEngineerId(String engineerId) {
            this.engineerId = engineerId;
            return this;
        }

        public Builder setEngineerName(String engineerName) {
            this.engineerName = engineerName;
            return this;
        }

        public Builder setEngineerMobile(String engineerMobile) {
            this.engineerMobile = engineerMobile;
            return this;
        }

        public Builder setRemarks(String remarks) {
            this.remarks = remarks;
            return this;
        }

        public Builder setCompletedItems(List<CanboOrderCompleted.CompletedItem> completedItems) {
            this.completedItems = completedItems;
            return this;
        }

        public Builder setOrderCompletedItems(List<B2BOrderCompletedItem> orderCompletedItems) {
            this.orderCompletedItems = orderCompletedItems;
            return this;
        }

        public Builder setOrderPraiseItem(B2BOrderPraiseItem orderPraiseItem) {
            this.orderPraiseItem = orderPraiseItem;
            return this;
        }

        public Builder setOrderValidateItem(B2BOrderValidateItem orderValidateItem) {
            this.orderValidateItem = orderValidateItem;
            return this;
        }

        public Builder setAppointmentStatus(Integer appointmentStatus) {
            this.appointmentStatus = appointmentStatus;
            return this;
        }

        public Builder setInstallStaus(Integer installStaus) {
            this.installStaus = installStaus;
            return this;
        }

        public Builder setServicePointId(Long servicePointId) {
            this.servicePointId = servicePointId;
            return this;
        }

        public Builder setOrderCharge(Double orderCharge) {
            this.orderCharge = orderCharge;
            return this;
        }

        public Builder setPendingType(String pendingType) {
            this.pendingType = pendingType;
            return this;
        }

        public Builder setActualTotalSurcharge(Double actualTotalSurcharge) {
            this.actualTotalSurcharge = actualTotalSurcharge;
            return this;
        }

        public Builder setServiceItems(List<B2BOrderServiceItem> serviceItems) {
            this.serviceItems = serviceItems;
            return this;
        }

        public Builder setCustomerTotalCharge(Double customerTotalCharge) {
            this.customerTotalCharge = customerTotalCharge;
            return this;
        }

        public Builder setChargeAt(Long chargeAt) {
            this.chargeAt = chargeAt;
            return this;
        }

        public Builder setAppCompleteDt(Long appCompleteDt) {
            this.appCompleteDt = appCompleteDt;
            return this;
        }

        public Builder setB2bReason(String b2bReason) {
            this.b2bReason = b2bReason;
            return this;
        }

        public Builder setLongitude(Double longitude) {
            this.longitude = longitude;
            return this;
        }

        public Builder setLatitude(Double latitude) {
            this.latitude = latitude;
            return this;
        }

        public Builder setServicePointName(String servicePointName) {
            this.servicePointName = servicePointName;
            return this;
        }

        public Builder setVerifyCode(String verifyCode) {
            this.verifyCode = verifyCode;
            return this;
        }

        public Builder setExtraField1(String extraField1) {
            this.extraField1 = extraField1;
            return this;
        }

        public B2BOrderStatusUpdateReqEntity build() {
            return new B2BOrderStatusUpdateReqEntity(this);
        }
    }

}
