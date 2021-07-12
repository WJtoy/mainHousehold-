package com.wolfking.jeesite.modules.sd.entity.mapper;

import com.wolfking.jeesite.modules.mq.dto.MQOrderGradeMessage;
import com.wolfking.jeesite.modules.sd.entity.OrderGrade;
import org.mapstruct.*;

import java.util.List;

/**
 * 客评消息队列与客评模型之间相互转换
 */
@Mapper(collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class OrderGradeItemMessageMapper {

    @Mappings({
            @Mapping(target = "gradeId",source = "gradeId"),
            @Mapping(target = "gradeName",source = "gradeName"),
            @Mapping(target = "gradeItemId",source = "gradeItemId"),
            @Mapping(target = "gradeItemName",source = "gradeItemName"),
            @Mapping(target = "sort",source = "sort"),
            @Mapping(target = "point",source = "point"),
    })
    public abstract OrderGrade mqToGradeModelItem(MQOrderGradeMessage.GradeItemMessage message);

    public abstract List<OrderGrade> mqToGradeModelItems(List<MQOrderGradeMessage.GradeItemMessage> items);

}
