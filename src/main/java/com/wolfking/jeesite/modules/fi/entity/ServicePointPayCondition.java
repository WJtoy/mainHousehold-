package com.wolfking.jeesite.modules.fi.entity;

public class ServicePointPayCondition {

    private Long servicePointId;
    private Integer paymentType;
    private Integer bank;
    private String bankName;

    public Long getServicePointId() {
        return servicePointId;
    }

    public void setServicePointId(Long servicePointId) {
        this.servicePointId = servicePointId;
    }

    public Integer getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(Integer paymentType) {
        this.paymentType = paymentType;
    }

    public Integer getBank() {
        return bank;
    }

    public void setBank(Integer bank) {
        this.bank = bank;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("servicePointId:").
                append(servicePointId).
                append(",paymentType:").
                append(paymentType).
                append(",bank:").
                append(bank).
                append(",bankName:").
                append(bankName);
        return stringBuilder.toString();
    }
}
