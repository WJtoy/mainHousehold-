package com.wolfking.jeesite.modules.mq.entity.mapper;

import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.Product;
import com.wolfking.jeesite.modules.md.entity.ServiceType;
import com.wolfking.jeesite.modules.mq.dto.MQOrderImportMessage.OrderImportMessage;
import com.wolfking.jeesite.modules.sd.entity.TempOrder;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.tmall.md.entity.B2bCustomerMap;
import javax.annotation.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2021-06-28T15:55:09+0800",
    comments = "version: 1.2.0.Final, compiler: javac, environment: Java 1.8.0_181 (Oracle Corporation)"
)
public class OrderImportMessageMapperImpl extends OrderImportMessageMapper {

    @Override
    public TempOrder mqToModel(OrderImportMessage message) {
        if ( message == null ) {
            return null;
        }

        TempOrder tempOrder = new TempOrder();

        tempOrder.setB2bShop( orderImportMessageToB2bCustomerMap( message ) );
        tempOrder.setProduct( orderImportMessageToProduct( message ) );
        tempOrder.setCreateBy( orderImportMessageToUser( message ) );
        tempOrder.setExpressCompany( orderImportMessageToDict( message ) );
        tempOrder.setCustomer( orderImportMessageToCustomer( message ) );
        tempOrder.setServiceType( orderImportMessageToServiceType( message ) );
        if ( message.getWorkCardId() != null ) {
            tempOrder.setThdNo( message.getWorkCardId() );
        }
        if ( message.getExpressNo() != null ) {
            tempOrder.setExpressNo( message.getExpressNo() );
        }
        if ( message.getDescription() != null ) {
            tempOrder.setDescription( message.getDescription() );
        }
        if ( message.getTel() != null ) {
            tempOrder.setTel( message.getTel() );
        }
        tempOrder.setId( message.getId() );
        if ( message.getBrand() != null ) {
            tempOrder.setBrand( message.getBrand() );
        }
        if ( message.getProductSpec() != null ) {
            tempOrder.setProductSpec( message.getProductSpec() );
        }
        if ( message.getAddress() != null ) {
            tempOrder.setAddress( message.getAddress() );
        }
        if ( message.getUserName() != null ) {
            tempOrder.setUserName( message.getUserName() );
        }
        if ( message.getErrorMsg() != null ) {
            tempOrder.setErrorMsg( message.getErrorMsg() );
        }
        tempOrder.setRetryTimes( message.getRetryTimes() );
        if ( message.getPhone() != null ) {
            tempOrder.setPhone( message.getPhone() );
        }
        tempOrder.setQty( message.getQty() );
        if ( message.getRepeateOrderNo() != null ) {
            tempOrder.setRepeateOrderNo( message.getRepeateOrderNo() );
        }

        tempOrder.setCreateDate( com.wolfking.jeesite.common.utils.DateUtils.longToDate(message.getCreateDate()) );

        return tempOrder;
    }

    protected B2bCustomerMap orderImportMessageToB2bCustomerMap(OrderImportMessage orderImportMessage) {
        if ( orderImportMessage == null ) {
            return null;
        }

        B2bCustomerMap b2bCustomerMap = new B2bCustomerMap();

        if ( orderImportMessage.getShopId() != null ) {
            b2bCustomerMap.setShopId( orderImportMessage.getShopId() );
        }

        return b2bCustomerMap;
    }

    protected Product orderImportMessageToProduct(OrderImportMessage orderImportMessage) {
        if ( orderImportMessage == null ) {
            return null;
        }

        Product product = new Product();

        product.setId( orderImportMessage.getProductId() );
        if ( orderImportMessage.getProductName() != null ) {
            product.setName( orderImportMessage.getProductName() );
        }

        return product;
    }

    protected User orderImportMessageToUser(OrderImportMessage orderImportMessage) {
        if ( orderImportMessage == null ) {
            return null;
        }

        User user = new User();

        user.setId( orderImportMessage.getCreateById() );
        user.setUserType( orderImportMessage.getCreateByType() );
        if ( orderImportMessage.getCreateByName() != null ) {
            user.setName( orderImportMessage.getCreateByName() );
        }

        return user;
    }

    protected Dict orderImportMessageToDict(OrderImportMessage orderImportMessage) {
        if ( orderImportMessage == null ) {
            return null;
        }

        Dict dict = new Dict();

        if ( orderImportMessage.getExpressCompanyLabel() != null ) {
            dict.setLabel( orderImportMessage.getExpressCompanyLabel() );
        }
        if ( orderImportMessage.getExpressCompanyValue() != null ) {
            dict.setValue( orderImportMessage.getExpressCompanyValue() );
        }

        return dict;
    }

    protected Customer orderImportMessageToCustomer(OrderImportMessage orderImportMessage) {
        if ( orderImportMessage == null ) {
            return null;
        }

        Customer customer = new Customer();

        if ( orderImportMessage.getCustomerName() != null ) {
            customer.setName( orderImportMessage.getCustomerName() );
        }
        customer.setId( orderImportMessage.getCustomerId() );

        return customer;
    }

    protected ServiceType orderImportMessageToServiceType(OrderImportMessage orderImportMessage) {
        if ( orderImportMessage == null ) {
            return null;
        }

        ServiceType serviceType = new ServiceType();

        if ( orderImportMessage.getServiceTypeName() != null ) {
            serviceType.setName( orderImportMessage.getServiceTypeName() );
        }
        serviceType.setId( orderImportMessage.getServiceTypeId() );

        return serviceType;
    }
}
