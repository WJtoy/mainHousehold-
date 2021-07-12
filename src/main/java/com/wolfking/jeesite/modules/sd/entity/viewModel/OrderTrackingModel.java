package com.wolfking.jeesite.modules.sd.entity.viewModel;

import com.google.common.collect.Lists;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.sd.entity.OrderFee;
import com.wolfking.jeesite.modules.sd.entity.OrderItem;
import com.wolfking.jeesite.modules.sd.entity.OrderProcessLog;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 进度跟踪 of 订单详情
 */

public class OrderTrackingModel implements Serializable {

    private Long orderId;

    private String remarks = "";

    private Date trackingDate; //跟踪日期
    /**
     * 添加订单进度跟踪时，是否对客户可见
     * 默认是0 对客户不可见
     */
    private Integer isCustomerSame = 0;
    private String ctx = "";

    /**
     * 判断是否有下一页
     * */
    private boolean nextPageFlag;

    /**
     * 分页查询的偏离量
     * */
    private Integer nextPageNo = 0;

    private String quarter="";


    List<Dict> tracks = Lists.newArrayList();
    //logs
    List<OrderProcessLog> logs = Lists.newArrayList();

    private Integer pageCount = 0;

    public OrderTrackingModel(){

    }

    public OrderTrackingModel(Long orderId){
        this.orderId = orderId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Date getTrackingDate() {
        return trackingDate;
    }

    public void setTrackingDate(Date trackingDate) {
        this.trackingDate = trackingDate;
    }

    public Integer getIsCustomerSame() {
        return isCustomerSame;
    }

    public void setIsCustomerSame(Integer isCustomerSame) {
        this.isCustomerSame = isCustomerSame;
    }

    public String getCtx() {
        return ctx;
    }

    public void setCtx(String ctx) {
        this.ctx = ctx;
    }

    public List<Dict> getTracks() {
        return tracks;
    }

    public void setTracks(List<Dict> tracks) {
        this.tracks = tracks;
    }

    public List<OrderProcessLog> getLogs() {
        return logs;
    }

    public void setLogs(List<OrderProcessLog> logs) {
        this.logs = logs;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Integer getNextPageNo() {
        return nextPageNo;
    }

    public void setNextPageNo(Integer nextPageNo) {
        this.nextPageNo = nextPageNo;
    }

    public boolean isNextPageFlag() {
        return nextPageFlag;
    }

    public void setNextPageFlag(boolean nextPageFlag) {
        this.nextPageFlag = nextPageFlag;
    }

    public String getQuarter() {
        return quarter;
    }

    public void setQuarter(String quarter) {
        this.quarter = quarter;
    }

    public Integer getPageCount() {
        return pageCount;
    }

    public void setPageCount(Integer pageCount) {
        this.pageCount = pageCount;
    }
}

