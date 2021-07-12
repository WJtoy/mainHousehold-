package com.wolfking.jeesite.modules.fi.entity;

import com.wolfking.jeesite.common.persistence.LongIDDataEntity;

import java.util.Date;

/**
 * Created by Jeff on 2017/4/19.
 */
public class CustomerChargeCondition extends LongIDDataEntity<CustomerChargeCondition> {
    private Long orderId;
    private String orderNo;
    private Long customerId;
    private Long productCategoryId;
    private String productIds;
    private Integer serviceTimes;
    private Integer paymentType;
    private Integer status;
    private Integer chargeOrderType;
    private Integer totalQty;
    private Date orderCreateDate;
    private Date orderCloseDate;
    private String serviceTypes;
    private Double timeLiness = 0.0;
    private String quarter;
    private Date createBeginDate;
    private Date createEndDate;
    private Date orderCreateBeginDate;
    private Date orderCreateEndDate;
    private Date orderCloseBeginDate;
    private Date orderCloseEndDate;

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

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getProductCategoryId() { return productCategoryId; }

    public void setProductCategoryId(Long productCategoryId) { this.productCategoryId = productCategoryId; }

    public String getProductIds() {
        return productIds;
    }

    public void setProductIds(String productIds) {
        this.productIds = productIds;
    }

    public Integer getServiceTimes() {
        return serviceTimes;
    }

    public void setServiceTimes(Integer serviceTimes) {
        this.serviceTimes = serviceTimes;
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

    public Integer getTotalQty() {
        return totalQty;
    }

    public void setTotalQty(Integer totalQty) {
        this.totalQty = totalQty;
    }

    public Date getOrderCreateDate() {
        return orderCreateDate;
    }

    public void setOrderCreateDate(Date orderCreateDate) {
        this.orderCreateDate = orderCreateDate;
    }

    public Date getOrderCloseDate() {
        return orderCloseDate;
    }

    public void setOrderCloseDate(Date orderCloseDate) {
        this.orderCloseDate = orderCloseDate;
    }

    public String getServiceTypes() {
        return serviceTypes;
    }

    public void setServiceTypes(String serviceTypes) {
        this.serviceTypes = serviceTypes;
    }

    public Double getTimeLiness() {
        return timeLiness;
    }

    public void setTimeLiness(Double timeLiness) {
        this.timeLiness = timeLiness;
    }

    public String getQuarter() {
        return quarter;
    }

    public void setQuarter(String quarter) {
        this.quarter = quarter;
    }
    public Date getCreateBeginDate() {
        return createBeginDate;
    }

    public void setCreateBeginDate(Date createBeginDate) {
        this.createBeginDate = createBeginDate;
    }

    public Date getCreateEndDate() {
        return createEndDate;
    }

    public void setCreateEndDate(Date createEndDate) {
        this.createEndDate = createEndDate;
    }

    public Date getOrderCreateBeginDate() {
        return orderCreateBeginDate;
    }

    public void setOrderCreateBeginDate(Date orderCreateBeginDate) {
        this.orderCreateBeginDate = orderCreateBeginDate;
    }

    public Date getOrderCreateEndDate() {
        return orderCreateEndDate;
    }

    public void setOrderCreateEndDate(Date orderCreateEndDate) {
        this.orderCreateEndDate = orderCreateEndDate;
    }

    public Date getOrderCloseBeginDate() {
        return orderCloseBeginDate;
    }

    public void setOrderCloseBeginDate(Date orderCloseBeginDate) {
        this.orderCloseBeginDate = orderCloseBeginDate;
    }

    public Date getOrderCloseEndDate() {
        return orderCloseEndDate;
    }

    public void setOrderCloseEndDate(Date orderCloseEndDate) {
        this.orderCloseEndDate = orderCloseEndDate;
    }
}
