package com.wolfking.jeesite.modules.ws.entity;

/**
 * 问题反馈统计
 */
public class WSFeedbackStats {

    private Long customerId;//客户id
    private Long userId;//用户id
    private Integer userType;//用户类型
    private Integer Qty;//数量

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getUserType() {
        return userType;
    }

    public void setUserType(Integer userType) {
        this.userType = userType;
    }

    public Integer getQty() {
        return Qty;
    }

    public void setQty(Integer qty) {
        Qty = qty;
    }
}