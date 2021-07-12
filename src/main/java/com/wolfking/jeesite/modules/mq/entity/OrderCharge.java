package com.wolfking.jeesite.modules.mq.entity;

import com.wolfking.jeesite.common.persistence.LongIDDataEntity;

import java.util.Date;

/**
 * Created by Jeff on 2017/6/24.
 */
public class OrderCharge extends LongIDDataEntity<OrderCharge> {
    private Long orderId;           //订单id
    private Integer retryTimes;     //重试次数，失败后再重试3次，超过3次视为失败
    private Integer status;         //狀態，10：待处理，20：失败重试中，30：处理成功，40：处理失败
    private String description;            //备注
    private Long triggerBy;         //触发者
    private Date triggerDate;       //触发时间
    private String quarter;         //季度,分片根据,如20171
    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Integer getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(Integer retryTimes) {
        this.retryTimes = retryTimes;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getTriggerBy() {
        return triggerBy;
    }

    public void setTriggerBy(Long triggerBy) {
        this.triggerBy = triggerBy;
    }

    public Date getTriggerDate() {
        return triggerDate;
    }

    public void setTriggerDate(Date triggerDate) {
        this.triggerDate = triggerDate;
    }

    public String getQuarter() {
        return quarter;
    }

    public void setQuarter(String quarter) {
        this.quarter = quarter;
    }
}
