package com.wolfking.jeesite.modules.rpt.entity;

import com.google.common.collect.Lists;
import com.wolfking.jeesite.common.config.Global;
import com.wolfking.jeesite.common.config.redis.GsonIgnore;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.kkl.kklplus.utils.StringUtils;

import java.util.Date;
import java.util.List;

/**
 * 报表的查询条件
 * Created on 2017-07-29.
 */
public class RptSearchCondition {

    private static final long serialVersionUID = 1L;
    private static final int ORDER_LENGTH = 14;

    public static final int IS_SEARCHING_NO     = 0;
    public static final int IS_SEARCHING_YES    = 1;

    private String kefuName;  //客服名称
    private Long kefuId; //客服Id
    private Integer selectedYear;   //选中的年
    private Integer selectedMonth;  //选中的月

    private Date beginDate; //开始日期
    private Date endDate;  //结束日期

    private String dateString;

    private String paymentType; //结算方式
    private Long salesId = 0L;
    private String salesName; //业务员名称
    private Long customerId; //客户id
    private String customerName; //客户名称
    private Long reviewerId;//审单员id

    private Long invoiceId;//结账单号
    private String withdrawNo;//取现单号

    private Long areaId; //区域id
    private String areaName; //区域名称
    private Integer areaLevel;//区域类型 0:省 1：市 2：区
    private String levelValue;

    private String cancelResponsible; //退单责任方（调整为记录退单的类型，在退单明细报表加过滤区分)

    private Date beginPlanDate; //开始下单日期
    private Date endPlanDate;  //结束下单日期

    private Date beginCancelApplyDate; //开始退单日期
    private Date endCancelApplyDate;  //结束退单日期

    private Date beginInvoiceDate; //开始结账日期
    private Date endInvoiceDate;  //最后的结账日期

    private Long servicePointId; //网点id
    private String servicePointNo;//网点编号
    private String servicePointName; //网点名称
    private String contactInfo; //联系方式
    private Integer appFlag;//是否手机接单
    private Integer finishQty;//完成单数量
    private String province;//省份
    private String city;//市
    private Integer bank;//开户行
    private Integer orderServiceType; //订单类型
    private String remarks;//描述
    private Integer actionType;
    private Integer rptPageSize;
    private String orderNo;//订单号
    private String reminderNo;//催单编号
    private Integer reminderTimes;//催单次数
    private Integer status;//状态
    private Integer subFlag;//客服类型
    private Integer dataSource;//工单来源
    private String mallName;//店铺名称
    private String mobile;//联系方式
    private Long mallId;//店铺ID

    private Integer warrantyStatus;//质保类型

    private int orderNoSearchType = 0;//工单单号搜索类型
    private int isPhone = 0; //是否是合法的手机号码

    @GsonIgnore
    private Integer productCategory = 0;//服务品类
    @GsonIgnore
    private Integer reportId = 0;
    @GsonIgnore
    private Integer reportType = 0;
    @GsonIgnore
    private Integer middleTableId = 0;
    @GsonIgnore
    private Integer middleTableType = 0;
    @GsonIgnore
    private Integer rebuildOperationType = 0;

    private int days; //天数
    private Integer isSearching; //是否从数据库中搜索数据
    private String quarter = "";//数据库分片
    private List<?> list = Lists.newArrayList();//要显示的报表数据

    public String getDateString() {
        return dateString;
    }

    public void setDateString(String dateString) {
        this.dateString = dateString;
    }

    public Long getMallId() {
        return mallId;
    }

    public void setMallId(Long mallId) {
        this.mallId = mallId;
    }

    public String getMallName() {
        return mallName;
    }

    public void setMallName(String mallName) {
        this.mallName = mallName;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public Integer getActionType() {
        return actionType;
    }

    public void setActionType(Integer actionType) {
        this.actionType = actionType;
    }

    public Integer getOrderServiceType() {
        return orderServiceType;
    }

    public void setOrderServiceType(Integer orderServiceType) {
        this.orderServiceType = orderServiceType;
    }

    public String getKefuName() {
        return kefuName;
    }

    public void setKefuName(String kefuName) {
        this.kefuName = kefuName;
    }

    public Integer getSelectedYear() {
        return selectedYear;
    }

    public Integer getAreaLevel() {
        return areaLevel;
    }

    public void setAreaLevel(Integer areaLevel) {
        this.areaLevel = areaLevel;
    }

    public String getLevelValue() {
        return levelValue;
    }

    public void setLevelValue(String levelValue) {
        this.levelValue = levelValue;
    }

    public Long getKefuId() {
        return kefuId;
    }

    public void setKefuId(Long kefuId) {
        this.kefuId = kefuId;
    }

    public void setSelectedYear(Integer selectedYear) {
        this.selectedYear = selectedYear;
    }

    public Integer getSelectedMonth() {
        return selectedMonth;
    }

    public void setSelectedMonth(Integer selectedMonth) {
        this.selectedMonth = selectedMonth;
    }

    public Date getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(Date beginDate) {
        this.beginDate = beginDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public Long getSalesId() {
        return salesId;
    }

    public void setSalesId(Long salesId) {
        this.salesId = salesId;
    }

    public String getSalesName() {
        return salesName;
    }

    public void setSalesName(String salesName) {
        this.salesName = salesName;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(Long reviewerId) {
        this.reviewerId = reviewerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Long invoiceId) {
        this.invoiceId = invoiceId;
    }

    public String getWithdrawNo() {
        return withdrawNo;
    }

    public void setWithdrawNo(String withdrawNo) {
        this.withdrawNo = withdrawNo;
    }

    public Long getAreaId() {
        return areaId;
    }

    public void setAreaId(Long areaId) {
        this.areaId = areaId;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public String getCancelResponsible() {
        return cancelResponsible;
    }

    public void setCancelResponsible(String cancelResponsible) {
        this.cancelResponsible = cancelResponsible;
    }

    public Date getBeginPlanDate() {
        return beginPlanDate;
    }

    public void setBeginPlanDate(Date beginPlanDate) {
        this.beginPlanDate = beginPlanDate;
    }

    public Date getEndPlanDate() {
        return endPlanDate;
    }

    public void setEndPlanDate(Date endPlanDate) {
        this.endPlanDate = endPlanDate;
    }

    public Date getBeginCancelApplyDate() {
        return beginCancelApplyDate;
    }

    public void setBeginCancelApplyDate(Date beginCancelApplyDate) {
        this.beginCancelApplyDate = beginCancelApplyDate;
    }

    public Date getEndCancelApplyDate() {
        return endCancelApplyDate;
    }

    public void setEndCancelApplyDate(Date endCancelApplyDate) {
        this.endCancelApplyDate = endCancelApplyDate;
    }

    public Date getBeginInvoiceDate() {
        return beginInvoiceDate;
    }

    public void setBeginInvoiceDate(Date beginInvoiceDate) {
        this.beginInvoiceDate = beginInvoiceDate;
    }

    public Date getEndInvoiceDate() {
        return endInvoiceDate;
    }

    public void setEndInvoiceDate(Date endInvoiceDate) {
        this.endInvoiceDate = endInvoiceDate;
    }

    public Long getServicePointId() {
        return servicePointId;
    }

    public void setServicePointId(Long servicePointId) {
        this.servicePointId = servicePointId;
    }

    public String getServicePointNo() {
        return servicePointNo;
    }

    public void setServicePointNo(String servicePointNo) {
        this.servicePointNo = servicePointNo;
    }

    public String getServicePointName() {
        return servicePointName;
    }

    public void setServicePointName(String servicePointName) {
        this.servicePointName = servicePointName;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public Integer getAppFlag() {
        return appFlag;
    }

    public void setAppFlag(Integer appFlag) {
        this.appFlag = appFlag;
    }

    public Integer getDataSource() {
        return dataSource;
    }

    public void setDataSource(Integer dataSource) {
        this.dataSource = dataSource;
    }

    public Integer getFinishQty() {
        return finishQty;
    }

    public void setFinishQty(Integer finishQty) {
        this.finishQty = finishQty;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Integer getBank() {
        return bank;
    }

    public void setBank(Integer bank) {
        this.bank = bank;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public int getRowsCount() {
        return list.size();
    }

    public Integer getRptPageSize() {
        return rptPageSize;
    }

    public void setRptPageSize(Integer rptPageSize) {
        this.rptPageSize = rptPageSize;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public Integer getIsSearching() {
        return isSearching;
    }

    public void setIsSearching(Integer isSearching) {
        this.isSearching = isSearching;
    }

    public Integer getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(Integer productCategory) {
        this.productCategory = productCategory;
    }

    public boolean isSearching() {
        if (isSearching != null && isSearching == IS_SEARCHING_YES) {
            return true;
        }
        else {
            return false;
        }
    }

    public int getSumRowNumber() {
        return BaseRptEntity.RPT_ROW_NUMBER_SUMROW;
    }

    public int getPerRowNumber() {
        return BaseRptEntity.RPT_ROW_NUMBER_PERROW;
    }

    public int getIsSearchingYes() {
        return IS_SEARCHING_YES;
    }

    public List<?> getList() {
        return list;
    }

    public void setList(List<?> list) {
        this.list = list;
    }

    public Integer getReportId() {
        return reportId;
    }

    public void setReportId(Integer reportId) {
        this.reportId = reportId;
    }

    public Integer getReportType() {
        return reportType;
    }

    public void setReportType(Integer reportType) {
        this.reportType = reportType;
    }

    public Integer getMiddleTableId() {
        return middleTableId;
    }

    public void setMiddleTableId(Integer middleTableId) {
        this.middleTableId = middleTableId;
    }

    public Integer getMiddleTableType() {
        return middleTableType;
    }

    public void setMiddleTableType(Integer middleTableType) {
        this.middleTableType = middleTableType;
    }

    public Integer getRebuildOperationType() {
        return rebuildOperationType;
    }

    public void setRebuildOperationType(Integer rebuildOperationType) {
        this.rebuildOperationType = rebuildOperationType;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getReminderNo() {
        return reminderNo;
    }

    public void setReminderNo(String reminderNo) {
        this.reminderNo = reminderNo;
    }

    public Integer getReminderTimes() {
        return reminderTimes;
    }

    public void setReminderTimes(Integer reminderTimes) {
        this.reminderTimes = reminderTimes;
    }

    public Integer getSubFlag() {
        return subFlag;
    }

    public void setSubFlag(Integer subFlag) {
        this.subFlag = subFlag;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getQuarter() {
        return quarter;
    }

    public void setQuarter(String quarter) {
        this.quarter = quarter;
    }

    public Integer getWarrantyStatus() { return warrantyStatus;}

    public void setWarrantyStatus(Integer warrantyStatus) { this.warrantyStatus = warrantyStatus; }

    public int getOrderNoSearchType(){
        if (StringUtils.isNotBlank(this.orderNo)){
            this.orderNo = this.orderNo.trim().toUpperCase();
            String orderNoPrefix = Global.getConfig("OrderPrefix");
            if (orderNo.length() == ORDER_LENGTH && orderNo.startsWith(orderNoPrefix)){
                orderNoSearchType = 1;
                String quarter = QuarterUtils.getOrderQuarterFromNo(orderNo);
                if(StringUtils.isNotBlank(quarter)){
                    this.quarter = quarter;
                }
            }else if (orderNo.startsWith(orderNoPrefix)){
                orderNoSearchType = 2;
            }
        }
        return this.orderNoSearchType;
    }

    public int getIsPhone(){
        if (StringUtils.isNotBlank(this.contactInfo)){
            if("".equalsIgnoreCase(StringUtils.isPhoneWithRelaxed(this.contactInfo))){
                this.isPhone = 1;
            }
        }
        return this.isPhone;
    }
}
