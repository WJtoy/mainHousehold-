package com.wolfking.jeesite.modules.fi.entity.viewModel;

import com.wolfking.jeesite.modules.sys.entity.Dict;

import java.util.Date;

/**
 * Created by Ryan on 2018/1/18.
 */
public class CustomerCurrencyModel {

    private String currencyNo;
    private double amount;
    private Dict actionType;
    private String quarter;
    private Date createDate;

    public CustomerCurrencyModel() {
    }

    public String getCurrencyNo() {
        return currencyNo;
    }

    public void setCurrencyNo(String currencyNo) {
        this.currencyNo = currencyNo;
    }



    public Dict getActionType() {
        return actionType;
    }

    public void setActionType(Dict actionType) {
        this.actionType = actionType;
    }

    public String getQuarter() {
        return quarter;
    }

    public void setQuarter(String quarter) {
        this.quarter = quarter;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }
}
