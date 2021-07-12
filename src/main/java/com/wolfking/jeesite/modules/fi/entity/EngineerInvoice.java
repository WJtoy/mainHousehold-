package com.wolfking.jeesite.modules.fi.entity;

import com.wolfking.jeesite.common.persistence.LongIDDataEntity;
import com.wolfking.jeesite.modules.md.entity.Engineer;
import com.wolfking.jeesite.modules.md.entity.Product;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.md.entity.ServiceType;

import java.util.Date;

/**
 * Created by Jeff on 2017/4/14.
 */
public class EngineerInvoice extends LongIDDataEntity<EngineerInvoice> {
    private Long orderId;
    private String orderNo;
    private ServicePoint servicePoint;
    private Engineer engineer;
    private Product product;
    private ServiceType serviceType;
    private Double serviceCharge;
    private Double expressCharge;
    private Double travelCharge;
    private Double materialCharge;
    private Double insuranceCharge = 0.0;
    private Double otherCharge;
    private String insuranceNo = "";
    private Integer serviceTimes;
    private Integer paymentType;
    private Integer status;
    private Date invoiceDate;
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

    public ServicePoint getServicePoint() {
        return servicePoint;
    }

    public void setServicePoint(ServicePoint servicePoint) {
        this.servicePoint = servicePoint;
    }

    public Engineer getEngineer() {
        return engineer;
    }

    public void setEngineer(Engineer engineer) {
        this.engineer = engineer;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
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

    public Double getInsuranceCharge() {
        return insuranceCharge;
    }

    public void setInsuranceCharge(Double insuranceCharge) {
        this.insuranceCharge = insuranceCharge;
    }

    public Double getOtherCharge() {
        return otherCharge;
    }

    public void setOtherCharge(Double otherCharge) {
        this.otherCharge = otherCharge;
    }

    public String getInsuranceNo() {
        return insuranceNo;
    }

    public void setInsuranceNo(String insuranceNo) {
        this.insuranceNo = insuranceNo;
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

    public Date getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(Date invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public String getQuarter() {
        return quarter;
    }

    public void setQuarter(String quarter) {
        this.quarter = quarter;
    }
}
