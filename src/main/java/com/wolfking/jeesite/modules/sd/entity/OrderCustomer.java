package com.wolfking.jeesite.modules.sd.entity;

import java.io.Serializable;

/**
 * 工单客户信息实体
 * @author: Jeff.Zhao
 * @date: 2019/6/28 14:26
 */
public class OrderCustomer implements Serializable {
    private Long orderId;
    private Long salesId;
    private String quarter;

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getSalesId() {
        return salesId;
    }

    public void setSalesId(Long salesId) {
        this.salesId = salesId;
    }

    public String getQuarter() {
        return quarter;
    }

    public void setQuarter(String quarter) {
        this.quarter = quarter;
    }
}
