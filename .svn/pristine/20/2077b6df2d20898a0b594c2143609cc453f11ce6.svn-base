package com.wolfking.jeesite.ms.material.mq.service;

import com.wolfking.jeesite.modules.fi.entity.CustomerCurrency;
import com.wolfking.jeesite.modules.fi.service.CustomerCurrencyService;
import com.wolfking.jeesite.modules.md.entity.CustomerFinance;
import com.wolfking.jeesite.modules.md.service.CustomerService;
import com.wolfking.jeesite.modules.mq.service.MQRechargeService;
import com.wolfking.jeesite.modules.sys.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * 九阳配件服务
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class JoyoungMaterialMQService {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerCurrencyService customerCurrencyService;

    @Autowired
    private MQRechargeService mqRechargeService;



    /**
     * 审核处理
     * 1.更新配件单状态
     * 2.记录日志
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
