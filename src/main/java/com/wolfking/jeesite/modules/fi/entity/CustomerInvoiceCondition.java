package com.wolfking.jeesite.modules.fi.entity;

import com.wolfking.jeesite.common.persistence.LongIDDataEntity;

import java.util.Date;

/**
 * Created by Jeff on 2017/4/19.
 */
public class CustomerInvoiceCondition extends LongIDDataEntity<CustomerInvoiceCondition> {
    private String customerInvoiceNo;
    private Long customerId;
    private Integer paymentType;
    private Integer status;
    private Date invoiceDate;
    private String serviceTypes;
    private Integer rebateFlag;
    private String quarter;

    public String getCustomerInvoiceNo() {
        return customerInvoiceNo;
    }

    public void setCustomerInvoiceNo(String customerInvoiceNo) {
        this.customerInvoiceNo = customerInvoiceNo;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
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

    public String getServiceTypes() {
        return serviceTypes;
    }

    public void setServiceTypes(String serviceTypes) {
        this.serviceTypes = serviceTypes;
    }

    public Integer getRebateFlag() {
        return rebateFlag;
    }

    public void setRebateFlag(Integer rebateFlag) {
        this.rebateFlag = rebateFlag;
    }

    public String getQuarter() {
        return quarter;
    }

    public void setQuarter(String quarter) {
        this.quarter = quarter;
    }
}
