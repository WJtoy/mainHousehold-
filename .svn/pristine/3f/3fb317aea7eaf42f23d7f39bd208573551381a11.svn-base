package com.wolfking.jeesite.ms.recharge.service;

import com.wolfking.jeesite.modules.fi.entity.CustomerCurrency;
import com.wolfking.jeesite.modules.fi.service.CustomerCurrencyService;
import com.wolfking.jeesite.modules.md.dao.CustomerFinanceDao;
import com.wolfking.jeesite.modules.md.entity.CustomerFinance;
import com.wolfking.jeesite.modules.md.service.CustomerService;
import com.wolfking.jeesite.modules.mq.service.MQRechargeService;
import com.wolfking.jeesite.modules.sys.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;

@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class RechargeOrderService {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerCurrencyService customerCurrencyService;

    @Autowired
    private MQRechargeService mqRechargeService;

    /**
     * 根据Id获取充值单

    public RechargeOrder getById(Long id){
        MSResponse<RechargeOrder> msResponse = rechargeOrderFeign.get(id);
        if(MSResponse.isSuccess(msResponse)){
            return msResponse.getData();
        }else{
            return null;
        }
    }
     */

    /**
     * 充值处理
     * 1.新增流水记录
     * 2.更新客户账户余额
     */
    @Transactional()
    public void rechargeProcess(CustomerCurrency currency,int isUpdateTask){
        if(currency == null || currency.getId() == null || currency.getId()<=0
            || currency.getCustomer() == null || currency.getCustomer().getId() == null || currency.getCustomer().getId() <= 0){
            return;
        }
        User user = currency.getUpdateBy();
        Date date = currency.getUpdateDate();
        customerCurrencyService.insert(currency);

        //更新余额
        CustomerFinance finance = new CustomerFinance();
        finance.setId(currency.getCustomer().getId());
        finance.setTransactionAmount(currency.getAmount());//变更金额
        //finance.setUpdateBy(user);
        //finance.setUpdateDate(date);
        customerService.increaseFinanceAmount(finance);
        if(isUpdateTask == 1){
            mqRechargeService.mqConsumeSuccess(currency.getCustomer().getId(),currency.getId());
        }
    }

}
