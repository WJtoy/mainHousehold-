package com.wolfking.jeesite.modules.fi.entity;

import com.google.common.collect.Lists;
import com.wolfking.jeesite.common.config.redis.GsonIgnore;
import com.wolfking.jeesite.common.persistence.LongIDDataEntity;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.CustomerFinance;

import java.util.List;

/**
 * Created by Jeff on 2017/4/14.
 */
public class CustomerCurrency extends LongIDDataEntity<CustomerCurrency> {

    public static final int CURRENCY_RMB  = 10;  //RMB-人民币

    public static final int PAYMENT_TYPE_CASH = 10;  //现金
    public static final int PAYMENT_TYPE_TRANSFER_ACCOUNT =20; //转账

    public static final int CURRENCY_TYPE_IN    = 10;           //收入
    public static final int CURRENCY_TYPE_OUT   = 20;           //支出
    public static final int CURRENCY_TYPE_NONE  = 0;            //无

    /**actionType长度为10**/
    /*public static final int ACTION_TYPE_CHARGE      = 10;//结算
    public static final int ACTION_TYPE_RECHARGE    = 20;//充值
    public static final int ACTION_TYPE_CREDIT      = 30;//信用额度更改
    public static final int ACTION_TYPE_ORDER       = 40;//下单w
    public static final int ACTION_TYPE_REBATE      = 50;//客户返点类型的操作
    public static final int ACTION_TYPE_TEMPRECHARGE    = 60;//在线充值临时类型
    public static final int ACTION_TYPE_CHARGEONLINE    = 70;//现在充值类型*/
    public static final int ACTION_TYPE_NONE            = 0;//无
    public static final int ACTION_TYPE_CHARGEONLINE    = 10;//充值
    public static final int ACTION_TYPE_RECHARGE        = 20;//财务充值
    public static final int ACTION_TYPE_REFUND          = 30;//退款
    public static final int ACTION_TYPE_REPLENISH       = 40;//补款
    public static final int ACTION_TYPE_ORDERCHARGE     = 50;//订单扣款
    public static final int ACTION_TYPE_TEMPRECHARGE    = 60;//充值临时记录
    public static final int ACTION_TYPE_TEMPRECHARGE2   = 62;//充值临时记录已更新（充值完成会重新记录一笔流水）
    public static final int ACTION_TYPE_BLOCK           = 70;//冻结
    public static final int ACTION_TYPE_CREDIT          = 80;//信用额度
    public static final int ACTION_TYPE_CHARGEOFFLINE   = 90;//线下充值

    public static final int ACTION_TYPE_CHARGEONLINE_RECHARGE   = 1020;//充值
    public static final int ACTION_TYPE_REFUND_REPLENISH        = 3040;//退补

    //private Long customerId;
    private Customer customer;
    private CustomerFinance customerFinance;
    private Integer currencyType;
    private String currencyTypeName = ""; //切换为微服务有用到
    private String CurrencyNo;
    private Double beforeBalance;
    private Double balance;
    private Double amount;
    private Integer paymentType;
    private String paymentTypeName; //切换为微服务有用到
    private Integer actionType;
    private String actionTypeName = "";//切换为微服务有用到
    private String quarter;
    //是否是第一次查询
    private Integer firstSearch = 1;
    /**
     * 查询客户流水时，需要指定分片
     */
    @GsonIgnore
    private List<String> quarters = Lists.newArrayList();

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Integer getCurrencyType() {
        return currencyType;
    }

    public void setCurrencyType(Integer currencyType) {
        this.currencyType = currencyType;
    }

    public String getCurrencyTypeName() {
        return currencyTypeName;
    }

    public void setCurrencyTypeName(String currencyTypeName) {
        this.currencyTypeName = currencyTypeName;
    }

    public String getCurrencyNo() {
        return CurrencyNo;
    }

    public void setCurrencyNo(String currencyNo) {
        CurrencyNo = currencyNo;
    }

    public Double getBeforeBalance() {
        return beforeBalance;
    }

    public void setBeforeBalance(Double beforeBalance) {
        this.beforeBalance = beforeBalance;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
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

    public Integer getActionType() {
        return actionType;
    }

    public void setActionType(Integer actionType) {
        this.actionType = actionType;
    }

    public String getActionTypeName() {
        return actionTypeName;
    }

    public void setActionTypeName(String actionTypeName) {
        this.actionTypeName = actionTypeName;
    }

    public String getQuarter() {
        return quarter;
    }

    public void setQuarter(String quarter) {
        this.quarter = quarter;
    }

    public CustomerFinance getCustomerFinance() {
        return customerFinance;
    }

    public void setCustomerFinance(CustomerFinance customerFinance) {
        this.customerFinance = customerFinance;
    }

    public Integer getFirstSearch() {
        return firstSearch;
    }

    public void setFirstSearch(Integer firstSearch) {
        this.firstSearch = firstSearch;
    }

    public List<String> getQuarters() {
        return quarters;
    }

    public void setQuarters(List<String> quarters) {
        this.quarters = quarters;
    }
}
