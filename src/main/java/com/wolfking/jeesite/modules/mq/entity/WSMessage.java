package com.wolfking.jeesite.modules.mq.entity;

import com.wolfking.jeesite.common.persistence.LongIDDataEntity;
import com.wolfking.jeesite.modules.sys.entity.Notice;
import com.wolfking.jeesite.modules.sys.entity.User;

import java.util.Date;

/**
 * WebSocket消息消息
 * Created by Ryan on 2017/6/24.
 */
public class WSMessage extends LongIDDataEntity<WSMessage> {

    private String quarter;         //季度,分片根据,如20171
    private Long customerId = 0l;      //客户
    private Long areaId = 0l;     //区域
    private Long orderId =0l;     //订单id
    private String orderNo ="";     //订单号
    private User triggerBy = new User(0l,"");  //发送者
    private Date triggerDate;     //触发时间
    private Long receiver = 0l;   //接收者,不为空代表点对点

    private Integer messageType = 0;
    private String title;         //主题
    private String context;       //消息内容
    private Long kefuId;  //客服
    private Long salesId;   //业务员
    private Long createId;  //订单创建者，客户或客服，业务

    private String distination;//目的地址

    private Integer retryTimes = 0;     //重试次数，失败后再重试3次，超过3次视为失败
    private Integer status = 0;     // 狀態，10：待处理，20：失败重试中，30：处理成功，40：处理失败

    public WSMessage(){}

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getAreaId() {
        return areaId;
    }

    public void setAreaId(Long areaId) {
        this.areaId = areaId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getReceiver() {
        return receiver;
    }

    public void setReceiver(Long receiver) {
        this.receiver = receiver;
    }

    public Integer getMessageType() {
        return messageType;
    }

    public void setMessageType(Integer messageType) {
        this.messageType = messageType;
    }

    public Integer getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(Integer retryTimes) {
        this.retryTimes = retryTimes;
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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getDistination() {
        return distination;
    }

    public void setDistination(String distination) {
        this.distination = distination;
    }

    //根据消息类型获得目的地址（客户端订阅的地址）
    public static String getDistination(Integer messageType){
        String distination = new String("/topic/notice");
        switch (messageType){
            case Notice.NOTICE_TYPE_FEEDBACK://点对点
                distination = "/queue/notifications";
                break;
            case Notice.NOTICE_TYPE_APPABNORMALY://点对点
                distination = "/queue/notifications";
                break;
            case Notice.NOTICE_TYPE_PARTS://点对点
                distination = "/queue/notifications";
                break;
            case Notice.NOTICE_TYPE_RETURN_PARTS://点对点
                distination = "/queue/notifications";
                break;
            case Notice.NOTICE_TYPE_NOTICE://all
                distination = "/topic/notice";
                break;
            case Notice.NOTICE_TYPE_CUSTOMER://厂商
                distination =  "/topic/customer";
                break;
            default:
                break;
        }
        return distination;
    }


    public static Boolean isSendToUser(Integer messageType){
        Boolean flag = false;
        switch (messageType){
            case Notice.NOTICE_TYPE_FEEDBACK://点对点
                flag = true;
                break;
            case Notice.NOTICE_TYPE_APPABNORMALY://点对点
                flag = true;
                break;
            case Notice.NOTICE_TYPE_PARTS://点对点
                flag = true;
                break;
            case Notice.NOTICE_TYPE_RETURN_PARTS://点对点
                flag = true;
                break;
            case Notice.NOTICE_TYPE_NOTICE://all
                flag = false;
                break;
            case Notice.NOTICE_TYPE_CUSTOMER://厂商
                flag = false;
                break;
            default:
                break;
        }
        return flag;
    }

    public User getTriggerBy() {
        return triggerBy;
    }

    public void setTriggerBy(User triggerBy) {
        this.triggerBy = triggerBy;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public Long getKefuId() {
        return kefuId;
    }

    public void setKefuId(Long kefuId) {
        this.kefuId = kefuId;
    }

    public Long getSalesId() {
        return salesId;
    }

    public void setSalesId(Long salesId) {
        this.salesId = salesId;
    }

    public Long getCreateId() {
        return createId;
    }

    public void setCreateId(Long createId) {
        this.createId = createId;
    }
}
