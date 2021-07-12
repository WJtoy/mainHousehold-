package com.wolfking.jeesite.modules.sd.entity.mapper;

import com.wolfking.jeesite.modules.mq.dto.MQOrderProcessLog.OrderProcessLog;
import javax.annotation.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2021-06-28T15:55:09+0800",
    comments = "version: 1.2.0.Final, compiler: javac, environment: Java 1.8.0_181 (Oracle Corporation)"
)
public class OrderProcessLogMessageMapperImpl extends OrderProcessLogMessageMapper {

    @Override
    public com.wolfking.jeesite.modules.sd.entity.OrderProcessLog messageToModel(OrderProcessLog message) {
        if ( message == null ) {
            return null;
        }

        com.wolfking.jeesite.modules.sd.entity.OrderProcessLog orderProcessLog = new com.wolfking.jeesite.modules.sd.entity.OrderProcessLog();

        orderProcessLog.setId( message.getId() );
        if ( message.getRemarks() != null ) {
            orderProcessLog.setRemarks( message.getRemarks() );
        }
        if ( message.getAction() != null ) {
            orderProcessLog.setAction( message.getAction() );
        }
        if ( message.getActionComment() != null ) {
            orderProcessLog.setActionComment( message.getActionComment() );
        }
        orderProcessLog.setStatusFlag( message.getStatusFlag() );
        if ( message.getStatus() != null ) {
            orderProcessLog.setStatus( message.getStatus() );
        }
        orderProcessLog.setCloseFlag( message.getCloseFlag() );
        orderProcessLog.setVisibilityFlag( message.getVisibilityFlag() );
        orderProcessLog.setOrderId( message.getOrderId() );
        orderProcessLog.setStatusValue( message.getStatusValue() );
        if ( message.getQuarter() != null ) {
            orderProcessLog.setQuarter( message.getQuarter() );
        }

        after( orderProcessLog, message );

        return orderProcessLog;
    }
}
