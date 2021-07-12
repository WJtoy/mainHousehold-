package com.wolfking.jeesite.modules.mq.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Ryan on 2017/6/24.
 */
public class MQCustomerModel implements Serializable {

    private Long id;//product
    private String code="";            //编码
    private String name="";            //名称
    private Long salesId;        //业务员
    private String salesMan ="";        //业务员
    private Date contractDate;      //签约日期
    private Integer paymentType = 0;
    private String paymentTypeName = "";

    public MQCustomerModel(){}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSalesMan() {
        return salesMan;
    }

    public void setSalesMan(String salesMan) {
        this.salesMan = salesMan;
    }

    public Date getContractDate() {
        return contractDate;
    }

    public void setContractDate(Date contractDate) {
        this.contractDate = contractDate;
    }

    public Integer getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(Integer paymentType) {
        this.paymentType = paymentType;
    }

    public String getPaymentTypeName() {
        return paymentTypeName;
    }

    public void setPaymentTypeName(String paymentTypeName) {
        this.paymentTypeName = paymentTypeName;
    }

    public Long getSalesId() {
        return salesId;
    }

    public void setSalesId(Long salesId) {
        this.salesId = salesId;
    }
}
