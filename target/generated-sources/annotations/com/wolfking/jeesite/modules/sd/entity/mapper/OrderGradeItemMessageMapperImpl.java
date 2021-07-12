package com.wolfking.jeesite.modules.sd.entity.mapper;

import com.wolfking.jeesite.modules.mq.dto.MQOrderGradeMessage.GradeItemMessage;
import com.wolfking.jeesite.modules.sd.entity.OrderGrade;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2021-05-18T17:10:41+0800",
    comments = "version: 1.2.0.Final, compiler: javac, environment: Java 1.8.0_181 (Oracle Corporation)"
)
public class OrderGradeItemMessageMapperImpl extends OrderGradeItemMessageMapper {

    @Override
    public OrderGrade mqToGradeModelItem(GradeItemMessage message) {
        if ( message == null ) {
            return null;
        }

        OrderGrade orderGrade = new OrderGrade();

        orderGrade.setGradeItemId( message.getGradeItemId() );
        if ( message.getGradeName() != null ) {
            orderGrade.setGradeName( message.getGradeName() );
        }
        orderGrade.setGradeId( message.getGradeId() );
        if ( message.getGradeItemName() != null ) {
            orderGrade.setGradeItemName( message.getGradeItemName() );
        }
        orderGrade.setSort( message.getSort() );
        orderGrade.setPoint( message.getPoint() );

        return orderGrade;
    }

    @Override
    public List<OrderGrade> mqToGradeModelItems(List<GradeItemMessage> items) {
        if ( items == null ) {
            return null;
        }

        List<OrderGrade> list = new ArrayList<OrderGrade>( items.size() );
        for ( GradeItemMessage gradeItemMessage : items ) {
            list.add( mqToGradeModelItem( gradeItemMessage ) );
        }

        return list;
    }
}
