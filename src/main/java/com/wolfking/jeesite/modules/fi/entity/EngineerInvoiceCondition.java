package com.wolfking.jeesite.modules.fi.entity;

import com.wolfking.jeesite.common.persistence.LongIDDataEntity;

import java.util.Date;

/**
 * Created by Jeff on 2017/4/19.
 */
public class EngineerInvoiceCondition extends LongIDDataEntity<EngineerInvoiceCondition> {

    private Long orderId;
    private String orderNo;
    private Long servicePointId;
    private Long engineerId;
    private Long productCategoryId;
    private Long productId;
    private Long serviceTypeId;
    private Integer paymentType;
    private Integer status;
    private Integer chargeOrderType;
    private Integer autoChargeFlag;
    private Date chargeDate;
    private Long chargeBy;
    private Date orderCloseDate;
    private String insuranceNo = "";
    private String quarter;

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

    public Long getServicePointId() {
        return servicePointId;
    }

    public void setServicePointId(Long servicePointId) {
        this.servicePointId = servicePointId;
    }

    public Long getEngineerId() {
        return engineerId;
    }

    public void setEngineerId(Long engineerId) {
        this.engineerId = engineerId;
    }

    public Long getProductCategoryId() {
        return productCategoryId;
    }

    public void setProductCategoryId(Long productCategoryId) {
        this.productCategoryId = productCategoryId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getServiceTypeId() {
        return serviceTypeId;
    }

    public void setServiceTypeId(Long serviceTypeId) {
        this.serviceTypeId = serviceTypeId;
    }

    public Integer getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(Integer paymentType) {
        this.paymentType = paymentType;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getChargeOrderType() {
        return chargeOrderType;
    }

    public void setChargeOrderType(Integer chargeOrderType) {
        this.chargeOrderType = chargeOrderType;
    }

    public Integer getAutoChargeFlag() {
        return autoChargeFlag;
    }

    public void setAutoChargeFlag(Integer autoChargeFlag) {
        this.autoChargeFlag = autoChargeFlag;
    }

    public Date getChargeDate() {
        return chargeDate;
    }

    public void setChargeDate(Date chargeDate) {
        this.chargeDate = chargeDate;
    }

    public Long getChargeBy() {
        return chargeBy;
    }

    public void setChargeBy(Long chargeBy) {
        this.chargeBy = chargeBy;
    }

    public Date getOrderCloseDate() {
        return orderCloseDate;
    }

    public void setOrderCloseDate(Date orderCloseDate) {
        this.orderCloseDate = orderCloseDate;
    }

    public String getInsuranceNo() {
        return insuranceNo;
    }

    public void setInsuranceNo(String insuranceNo) {
        this.insuranceNo = insuranceNo;
    }

    public String getQuarter() {
        return quarter;
    }

    public void setQuarter(String quarter) {
        this.quarter = quarter;
    }
}
