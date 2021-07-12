package com.wolfking.jeesite.modules.sd.entity.viewModel;

import com.google.common.collect.Lists;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.sd.entity.OrderFee;
import com.wolfking.jeesite.modules.sd.entity.OrderItem;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 订单视图数据模型
 */

public class OrderViewModel implements Serializable {

    private Long id;

    //用户
    private Area area;//用户区域
    private String userName;//用户名
    private String phone1;
    private String phone2;
    private String phone3;
    private String servicePhone;//用户实际联络电话
    private String email;
    private String address;//用户地址
    private String serviceAddress;//实际上门地址

    //fee
    private OrderFee fee;//费用

    //客户
    private Dict orderPaymentType;//客户结算方式(维护自客户)
    private Customer customer;

    //安维
    private ServicePoint servicePoint; //安维网点
    private User engineer;// 安维主账号派单给安维子账号
    private Dict engineerPaymentType;//安维结算方式

    //订单
    private String orderNo = "";// 订单号
    private Dict orderType;
    private Integer totalQty = 0;// 产品数量
    private Integer confirmDoor;// 确认上门标记

    private String description;// 服务描述
    private Integer trackingFlag=0;// 进度跟踪标记
    private Integer appAnomalyFlag=0;// 1:app异常标记
    private User kefu;// 客服名

    //配件
    //private Integer partsFlag = 0;//配件标记
    //private Integer returnPartsFlag=0;//反件标记
    //反馈
    //private Integer replyFlagKefu;// 1:客服回复新的标记订单为异常 ，0:厂家处理之后需要手动改为正常
    //private Integer replyFlagCustomer;// 1:厂家回复新的标记订单为异常 ，0:客服处理之后需要手动改为正常



    //订单开单审批,由客户主账号审批子账号订单
    private Integer customerApproveFlag = 0;// 客户开单审批标示
    private User customerApproveBy;// 审批人
    private Date customerApproveDate;// 审批日期 --> Order也定义了

    //接单(客服)
    private Integer acceptFlag = 0;// 接单标示
    private User acceptBy;// 接单人 ?
    private Date acceptDate;// 接单日期

    //派单(客服)
//    private Integer planFlag = 0;// 派单标示 1:已派单
    private User planBy;// 派单人
    private Date planDate;// 派单日期 --> Order也定义了
    private String planComment;// 派单备注

    private Date firstContactDate;// 首次联系用户时间

    //上门服务
    private Integer serviceFlag = 0;// 上门服务标示
    private Date serviceDate;// 上门服务日期
    private String serviceComment;// 上门服务备注

    private Integer serviceTimes = 0;// 累计上门次数,order表也有

    //关闭,客服关闭或者用户回复自动关闭
    private Integer closeFlag = 0;// 关闭状态
    private User closeBy;// 关闭人
    private Date closeDate;// 关闭日期

    //取消订单
    private String cancelResponsible;// 退单责任方（调整为记录退单的类型，在退单明细报表加过滤区分)
    private User cancelApplyBy;// 退单申请人
    private Date cancelApplyDate;// 退单申请日期
    private String cancelApplyComment;// 退单申请原因

    private Integer cancelApproveFlag = 0;// 退单审核标示 0:没有任何操作，1：通过审核 2：驳回
    private User cancelApproveBy;// 退单审核人
    private Date cancelApproveDate;// 退单审核日期

    //Items
    List<OrderItem> items = Lists.newArrayList();

    //details ?

    public OrderViewModel(){

    }

    public OrderViewModel(Long id){
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPhone1() {
        return phone1;
    }

    public void setPhone1(String phone1) {
        this.phone1 = phone1;
    }

    public String getPhone2() {
        return phone2;
    }

    public void setPhone2(String phone2) {
        this.phone2 = phone2;
    }

    public String getPhone3() {
        return phone3;
    }

    public void setPhone3(String phone3) {
        this.phone3 = phone3;
    }

    public String getServicePhone() {
        return servicePhone;
    }

    public void setServicePhone(String servicePhone) {
        this.servicePhone = servicePhone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getServiceAddress() {
        return serviceAddress;
    }

    public void setServiceAddress(String serviceAddress) {
        this.serviceAddress = serviceAddress;
    }

    public OrderFee getFee() {
        return fee;
    }

    public void setFee(OrderFee fee) {
        this.fee = fee;
    }

    public Dict getOrderPaymentType() {
        return orderPaymentType;
    }

    public void setOrderPaymentType(Dict orderPaymentType) {
        this.orderPaymentType = orderPaymentType;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public ServicePoint getServicePoint() {
        return servicePoint;
    }

    public void setServicePoint(ServicePoint servicePoint) {
        this.servicePoint = servicePoint;
    }

    public User getEngineer() {
        return engineer;
    }

    public void setEngineer(User engineer) {
        this.engineer = engineer;
    }

    public Dict getEngineerPaymentType() {
        return engineerPaymentType;
    }

    public void setEngineerPaymentType(Dict engineerPaymentType) {
        this.engineerPaymentType = engineerPaymentType;
    }

    public Dict getOrderType() {
        return orderType;
    }

    public void setOrderType(Dict orderType) {
        this.orderType = orderType;
    }

    public Integer getTotalQty() {
        return totalQty;
    }

    public void setTotalQty(Integer totalQty) {
        this.totalQty = totalQty;
    }

    public Integer getConfirmDoor() {
        return confirmDoor;
    }

    public void setConfirmDoor(Integer confirmDoor) {
        this.confirmDoor = confirmDoor;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getTrackingFlag() {
        return trackingFlag;
    }

    public void setTrackingFlag(Integer trackingFlag) {
        this.trackingFlag = trackingFlag;
    }

    public Integer getAppAnomalyFlag() {
        return appAnomalyFlag;
    }

    public void setAppAnomalyFlag(Integer appAnomalyFlag) {
        this.appAnomalyFlag = appAnomalyFlag;
    }

    public User getKefu() {
        return kefu;
    }

    public void setKefu(User kefu) {
        this.kefu = kefu;
    }

    public Integer getCustomerApproveFlag() {
        return customerApproveFlag;
    }

    public void setCustomerApproveFlag(Integer customerApproveFlag) {
        this.customerApproveFlag = customerApproveFlag;
    }

    public User getCustomerApproveBy() {
        return customerApproveBy;
    }

    public void setCustomerApproveBy(User customerApproveBy) {
        this.customerApproveBy = customerApproveBy;
    }

    public Date getCustomerApproveDate() {
        return customerApproveDate;
    }

    public void setCustomerApproveDate(Date customerApproveDate) {
        this.customerApproveDate = customerApproveDate;
    }

    public Integer getAcceptFlag() {
        return acceptFlag;
    }

    public void setAcceptFlag(Integer acceptFlag) {
        this.acceptFlag = acceptFlag;
    }

    public User getAcceptBy() {
        return acceptBy;
    }

    public void setAcceptBy(User acceptBy) {
        this.acceptBy = acceptBy;
    }

    public Date getAcceptDate() {
        return acceptDate;
    }

    public void setAcceptDate(Date acceptDate) {
        this.acceptDate = acceptDate;
    }

//    public Integer getPlanFlag() {
//        return planFlag;
//    }
//
//    public void setPlanFlag(Integer planFlag) {
//        this.planFlag = planFlag;
//    }

    public User getPlanBy() {
        return planBy;
    }

    public void setPlanBy(User planBy) {
        this.planBy = planBy;
    }

    public Date getPlanDate() {
        return planDate;
    }

    public void setPlanDate(Date planDate) {
        this.planDate = planDate;
    }

    public String getPlanComment() {
        return planComment;
    }

    public void setPlanComment(String planComment) {
        this.planComment = planComment;
    }

    public Date getFirstContactDate() {
        return firstContactDate;
    }

    public void setFirstContactDate(Date firstContactDate) {
        this.firstContactDate = firstContactDate;
    }

    public Integer getServiceFlag() {
        return serviceFlag;
    }

    public void setServiceFlag(Integer serviceFlag) {
        this.serviceFlag = serviceFlag;
    }

    public Date getServiceDate() {
        return serviceDate;
    }

    public void setServiceDate(Date serviceDate) {
        this.serviceDate = serviceDate;
    }

    public String getServiceComment() {
        return serviceComment;
    }

    public void setServiceComment(String serviceComment) {
        this.serviceComment = serviceComment;
    }

    public Integer getServiceTimes() {
        return serviceTimes;
    }

    public void setServiceTimes(Integer serviceTimes) {
        this.serviceTimes = serviceTimes;
    }

    public Integer getCloseFlag() {
        return closeFlag;
    }

    public void setCloseFlag(Integer closeFlag) {
        this.closeFlag = closeFlag;
    }

    public User getCloseBy() {
        return closeBy;
    }

    public void setCloseBy(User closeBy) {
        this.closeBy = closeBy;
    }

    public Date getCloseDate() {
        return closeDate;
    }

    public void setCloseDate(Date closeDate) {
        this.closeDate = closeDate;
    }

    public String getCancelResponsible() {
        return cancelResponsible;
    }

    public void setCancelResponsible(String cancelResponsible) {
        this.cancelResponsible = cancelResponsible;
    }

    public User getCancelApplyBy() {
        return cancelApplyBy;
    }

    public void setCancelApplyBy(User cancelApplyBy) {
        this.cancelApplyBy = cancelApplyBy;
    }

    public Date getCancelApplyDate() {
        return cancelApplyDate;
    }

    public void setCancelApplyDate(Date cancelApplyDate) {
        this.cancelApplyDate = cancelApplyDate;
    }

    public String getCancelApplyComment() {
        return cancelApplyComment;
    }

    public void setCancelApplyComment(String cancelApplyComment) {
        this.cancelApplyComment = cancelApplyComment;
    }

    public Integer getCancelApproveFlag() {
        return cancelApproveFlag;
    }

    public void setCancelApproveFlag(Integer cancelApproveFlag) {
        this.cancelApproveFlag = cancelApproveFlag;
    }

    public User getCancelApproveBy() {
        return cancelApproveBy;
    }

    public void setCancelApproveBy(User cancelApproveBy) {
        this.cancelApproveBy = cancelApproveBy;
    }

    public Date getCancelApproveDate() {
        return cancelApproveDate;
    }

    public void setCancelApproveDate(Date cancelApproveDate) {
        this.cancelApproveDate = cancelApproveDate;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

}

