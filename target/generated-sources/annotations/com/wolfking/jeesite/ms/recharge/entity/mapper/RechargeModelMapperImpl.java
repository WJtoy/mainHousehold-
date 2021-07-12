package com.wolfking.jeesite.ms.recharge.entity.mapper;

import com.kkl.kklplus.entity.fi.mq.MQRechargeOrderMessage.RechargeOrderMessage;
import com.wolfking.jeesite.modules.fi.entity.CustomerCurrency;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.sys.entity.User;
import javax.annotation.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2021-05-18T17:10:41+0800",
    comments = "version: 1.2.0.Final, compiler: javac, environment: Java 1.8.0_181 (Oracle Corporation)"
)
public class RechargeModelMapperImpl extends RechargeModelMapper {

    @Override
    public CustomerCurrency mqToCustomerCurrency(RechargeOrderMessage message) {
        if ( message == null ) {
            return null;
        }

        CustomerCurrency customerCurrency = new CustomerCurrency();

        customerCurrency.setCreateBy( rechargeOrderMessageToUser( message ) );
        customerCurrency.setCustomer( rechargeOrderMessageToCustomer( message ) );
        if ( message.getTradeNo() != null ) {
            customerCurrency.setCurrencyNo( message.getTradeNo() );
        }
        customerCurrency.setAmount( message.getAmount() );
        customerCurrency.setId( message.getId() );
        if ( message.getRemarks() != null ) {
            customerCurrency.setRemarks( message.getRemarks() );
        }
        customerCurrency.setUpdateBy( rechargeOrderMessageToUser1( message ) );

        customerCurrency.setCurrencyType( Integer.parseInt( "10" ) );
        customerCurrency.setUpdateDate( new java.util.Date() );
        customerCurrency.setPaymentType( Integer.parseInt( "20" ) );
        customerCurrency.setActionType( Integer.parseInt( "10" ) );
        customerCurrency.setQuarter( com.wolfking.jeesite.common.utils.QuarterUtils.getSeasonQuarter(message.getCreateAt()) );
        customerCurrency.setCreateDate( com.wolfking.jeesite.common.utils.DateUtils.longToDate(message.getCreateAt()) );

        return customerCurrency;
    }

    protected User rechargeOrderMessageToUser(RechargeOrderMessage rechargeOrderMessage) {
        if ( rechargeOrderMessage == null ) {
            return null;
        }

        User user = new User();

        user.setId( rechargeOrderMessage.getCreateBy() );

        return user;
    }

    protected Customer rechargeOrderMessageToCustomer(RechargeOrderMessage rechargeOrderMessage) {
        if ( rechargeOrderMessage == null ) {
            return null;
        }

        Customer customer = new Customer();

        customer.setId( rechargeOrderMessage.getReferId() );

        return customer;
    }

    protected User rechargeOrderMessageToUser1(RechargeOrderMessage rechargeOrderMessage) {
        if ( rechargeOrderMessage == null ) {
            return null;
        }

        User user = new User();

        user.setId( Long.parseLong( "0" ) );

        return user;
    }
}
