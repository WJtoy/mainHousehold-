package com.wolfking.jeesite.modules.fi.entity;

import com.wolfking.jeesite.common.persistence.LongIDDataEntity;
import com.wolfking.jeesite.modules.md.entity.Engineer;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.sys.entity.User;

import java.util.Date;

/**
 * Created by Jeff on 2017/6/14.
 */
public class ServicePointWithdraw extends LongIDDataEntity<ServicePointWithdraw> {

    //状态 10:新申请，20：提现失败，30：提现成功
    public static final Integer SPW_STATUS_NEW = 10;
    public static final Integer SPW_STATUS_PROCESS = 20;
    public static final Integer SPW_STATUS_FAIL = 30;
    public static final Integer SPW_STATUS_SUCCESS = 40;
    //提现类型：10：网点申请提现，20：财务主动打款,30:客服申请提现,40:网点后台申请提现,50:APP申请提现
    public static final Integer SPW_WITHDRAW_TYPE_WITHDRAW = 10;
    public static final Integer SPW_WITHDRAW_TYPE_PAY = 20;
    public static final Integer SPW_WITHDRAW_TYPE_KF = 30;
    public static final Integer SPW_WITHDRAW_TYPE_SP = 40;
    public static final Integer SPW_WITHDRAW_TYPE_APP = 50;

    private String          withdrawNo;             //提现付款编号，单据编号中管理，ServicePoinWithdrawNo,T,年月日,5位流水码
    private ServicePoint    servicePoint;           //网点
    private Engineer        engineer;               //安维
    private Integer         paymentType;            //付款類型(日结月结)
    private String          paymentTypeName;        //付款类型名称
    private Integer         status;                 //狀態,10:新申请，20：提现失败，30：提现成功
    private String          statusName;             //状态名称
    private Integer         withdrawType;           //提现类型：10：网点申请提现，20：财务主动打款,30:客服申请提现,40:网点后台申请提现,50:APP申请提现
    private Integer         bank;                   //银行ID,数据字典维护
    private String          bankName;               //银行名称,数据字典维护
    private String          branch;                 //支行
    private String          bankNo;                 //银行帐号
    private String          bankOwner;              //开户人
    private String          bankOwnerIdNo;          //开户人身份证号码
    private String          bankOwnerPhone;         //开户人银行预留手机号码
    private Double          beforeBalance;          //申请提现前帐户余额
    private Double          beforeDebts;            //申请提现前欠款余额
    private Double          applyAmount;            //申请提现金额
    private Double          payAmount;              //实际打款金额
    private Double          debtsDeduction;         //欠款抵扣
    private Double          deductionAmount;        //待扣款抵扣
    private Double          platformFee;            //平台服务费
    private User            payBy;                  //财务打款人
    private Date            payDate;                //财务打款时间
    private Integer         payForYear;             //所付款对应的年份
    private Integer         payForMonth;            //所付款对应的月份
    private String          quarter;                //季度,分片依据

    private Date            createBeginDate;        //查询创建开始时间
    private Date            createEndDate;          //查询创建结束时间
    private Date            payBeginDate;       //查询付款开始时间
    private Date            payEndDate;         //查询付款结束时间

    public String getWithdrawNo() {
        return withdrawNo;
    }

    public void setWithdrawNo(String withdrawNo) {
        this.withdrawNo = withdrawNo;
    }

    public ServicePoint getServicePoint() {
        return servicePoint;
    }

    public void setServicePoint(ServicePoint servicePoint) {
        this.servicePoint = servicePoint;
    }

    public Engineer getEngineer() {
        return engineer;
    }

    public void setEngineer(Engineer engineer) {
        this.engineer = engineer;
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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    public Integer getWithdrawType() {
        return withdrawType;
    }

    public void setWithdrawType(Integer withdrawType) {
        this.withdrawType = withdrawType;
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

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getBankNo() {
        return bankNo;
    }

    public void setBankNo(String bankNo) {
        this.bankNo = bankNo;
    }

    public String getBankOwner() {
        return bankOwner;
    }

    public void setBankOwner(String bankOwner) {
        this.bankOwner = bankOwner;
    }

    public String getBankOwnerIdNo() {
        return bankOwnerIdNo;
    }

    public void setBankOwnerIdNo(String bankOwnerIdNo) {
        this.bankOwnerIdNo = bankOwnerIdNo;
    }

    public String getBankOwnerPhone() {
        return bankOwnerPhone;
    }

    public void setBankOwnerPhone(String bankOwnerPhone) {
        this.bankOwnerPhone = bankOwnerPhone;
    }

    public Double getBeforeBalance() {
        return beforeBalance;
    }

    public void setBeforeBalance(Double beforeBalance) {
        this.beforeBalance = beforeBalance;
    }

    public Double getBeforeDebts() {
        return beforeDebts;
    }

    public void setBeforeDebts(Double beforeDebts) {
        this.beforeDebts = beforeDebts;
    }

    public Double getApplyAmount() {
        return applyAmount;
    }

    public void setApplyAmount(Double applyAmount) {
        this.applyAmount = applyAmount;
    }

    public Double getPayAmount() {
        return payAmount;
    }

    public void setPayAmount(Double payAmount) {
        this.payAmount = payAmount;
    }

    public Double getDebtsDeduction() {
        return debtsDeduction;
    }

    public void setDebtsDeduction(Double debtsDeduction) {
        this.debtsDeduction = debtsDeduction;
    }

    public Double getDeductionAmount() { return deductionAmount; }

    public void setSetDeductionAmount(Double deductionAmount) {
        this.deductionAmount = deductionAmount;
    }

    public Double getPlatformFee() {
        return platformFee;
    }

    public void setPlatformFee(Double platformFee) {
        this.platformFee = platformFee;
    }

    public User getPayBy() {
        return payBy;
    }

    public void setPayBy(User payBy) {
        this.payBy = payBy;
    }

    public Date getPayDate() {
        return payDate;
    }

    public void setPayDate(Date payDate) {
        this.payDate = payDate;
    }

    public Integer getPayForYear() {
        return payForYear;
    }

    public void setPayForYear(Integer payForYear) {
        this.payForYear = payForYear;
    }

    public Integer getPayForMonth() {
        return payForMonth;
    }

    public void setPayForMonth(Integer payForMonth) {
        this.payForMonth = payForMonth;
    }

    public String getQuarter() {
        return quarter;
    }

    public void setQuarter(String quarter) {
        this.quarter = quarter;
    }

    public Date getCreateBeginDate() {
        return createBeginDate;
    }

    public void setCreateBeginDate(Date createBeginDate) {
        this.createBeginDate = createBeginDate;
    }

    public Date getCreateEndDate() {
        return createEndDate;
    }

    public void setCreateEndDate(Date createEndDate) {
        this.createEndDate = createEndDate;
    }

    public Date getPayBeginDate() {
        return payBeginDate;
    }

    public void setPayBeginDate(Date payBeginDate) {
        this.payBeginDate = payBeginDate;
    }

    public Date getPayEndDate() {
        return payEndDate;
    }

    public void setPayEndDate(Date payEndDate) {
        this.payEndDate = payEndDate;
    }
}
