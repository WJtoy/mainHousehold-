package com.wolfking.jeesite.modules.fi.entity;

import com.wolfking.jeesite.common.persistence.LongIDDataEntity;
import com.wolfking.jeesite.modules.md.entity.Customer;

/**
 * Created by Jeff on 2017/4/14.
 */
public class CustomerCharge extends LongIDDataEntity<CustomerCharge> {
    //对帐单状态
    public static final Integer CC_STATUS_NEW = 10;//未结账
    public static final Integer CC_STATUS_CLOSED = 20;//已结帐
    //对帐单类型,0:原对帐单,1:退补单
    public static final Integer CC_TYPE_ORIGINAL = 0;
    public static final Integer CC_TYPE_WRITE_OFF = 1;

    private Long orderId;
    private String orderNo;
    private Customer customer;
    private Double serviceCharge;
    private Double expressCharge;
    private Double travelCharge;
    private Double materialCharge;
    private Double timeLinessCharge = 0.0;
    private Double urgentCharge = 0.0;
    private Double praiseFee = 0.0;
    private Double otherCharge;
    private Integer currency;
    private Integer serviceTimes;
    private Integer paymentType;
    private Integer status;
    private String statusName = ""; //为微服务创建
    private Integer chargeOrderType;
    private String quarter;
    private CustomerChargeCondition condition;

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

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Double getServiceCharge() {
        return serviceCharge;
    }

    public void setServiceCharge(Double serviceCharge) {
        this.serviceCharge = serviceCharge;
    }

    public Double getExpressCharge() {
        return expressCharge;
    }

    public void setExpressCharge(Double expressCharge) {
        this.expressCharge = expressCharge;
    }

    public Double getTravelCharge() {
        return travelCharge;
    }

    public void setTravelCharge(Double travelCharge) {
        this.travelCharge = travelCharge;
    }

    public Double getMaterialCharge() {
        return materialCharge;
    }

    public void setMaterialCharge(Double materialCharge) {
        this.materialCharge = materialCharge;
    }

    public Double getTimeLinessCharge() {
        return timeLinessCharge;
    }

    public void setTimeLinessCharge(Double timeLinessCharge) {
        this.timeLinessCharge = timeLinessCharge;
    }

    public Double getUrgentCharge() {
        return urgentCharge;
    }

    public void setUrgentCharge(Double urgentCharge) {
        this.urgentCharge = urgentCharge;
    }

    public Double getPraiseFee() { return praiseFee; }

    public void setPraiseFee(Double praiseFee) { this.praiseFee = praiseFee; }

    public Double getOtherCharge() {
        return otherCharge;
    }

    public void setOtherCharge(Double otherCharge) {
        this.otherCharge = otherCharge;
    }

    public Integer getCurrency() {
        return currency;
    }

    public void setCurrency(Integer currency) {
        this.currency = currency;
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

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    public Integer getChargeOrderType() {
        return chargeOrderType;
    }

    public void setChargeOrderType(Integer chargeOrderType) {
        this.chargeOrderType = chargeOrderType;
    }

    public String getQuarter() {
        return quarter;
    }

    public void setQuarter(String quarter) {
        this.quarter = quarter;
    }

    public CustomerChargeCondition getCondition() {
        return condition;
    }

    public void setCondition(CustomerChargeCondition condition) {
        this.condition = condition;
    }
}
