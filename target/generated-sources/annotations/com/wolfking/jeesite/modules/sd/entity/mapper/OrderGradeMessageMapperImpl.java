package com.wolfking.jeesite.modules.sd.entity.mapper;

import com.wolfking.jeesite.modules.mq.dto.MQOrderGradeMessage.OrderGradeMessage;
import com.wolfking.jeesite.modules.sd.entity.OrderGrade;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderGradeModel;
import java.util.List;
import javax.annotation.Generated;
import org.mapstruct.factory.Mappers;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2021-06-28T15:55:09+0800",
    comments = "version: 1.2.0.Final, compiler: javac, environment: Java 1.8.0_181 (Oracle Corporation)"
)
public class OrderGradeMessageMapperImpl extends OrderGradeMessageMapper {

    private final OrderGradeItemMessageMapper orderGradeItemMessageMapper = Mappers.getMapper( OrderGradeItemMessageMapper.class );
    private final OrderProcessLogMessageMapper orderProcessLogMessageMapper = Mappers.getMapper( OrderProcessLogMessageMapper.class );

    @Override
    public OrderGradeModel mqToModel(OrderGradeMessage message) {
        if ( message == null ) {
            return null;
        }

        OrderGradeModel orderGradeModel = new OrderGradeModel();

        List<OrderGrade> list = orderGradeItemMessageMapper.mqToGradeModelItems( message.getItemsList() );
        if ( list != null ) {
            orderGradeModel.setGradeList( list );
        }
        else {
            orderGradeModel.setGradeList( null );
        }
        if ( message.hasProcessLog() ) {
            orderGradeModel.setProcessLog( orderProcessLogMessageMapper.messageToModel( message.getProcessLog() ) );
        }
        orderGradeModel.setId( message.getId() );
        if ( message.getOrderNo() != null ) {
            orderGradeModel.setOrderNo( message.getOrderNo() );
        }
        orderGradeModel.setOrderId( message.getOrderId() );
        orderGradeModel.setAutoGradeFlag( message.getAutoGradeFlag() );
        if ( message.getQuarter() != null ) {
            orderGradeModel.setQuarter( message.getQuarter() );
        }
        orderGradeModel.setTimeLiness( message.getTimeLiness() );
        orderGradeModel.setPoint( message.getPoint() );
        orderGradeModel.setRushCloseFlag( message.getRushCloseFlag() );

        after( orderGradeModel, message );

        return orderGradeModel;
    }
}
