package com.wolfking.jeesite.ms.cc.entity.mapper;

import com.wolfking.jeesite.modules.sd.entity.*;
import com.wolfking.jeesite.ms.cc.entity.OrderReminderVM;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * 订单催单视图模型复制
 */
@Mapper(collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface OrderReminderVMMapper {

    OrderReminderVMMapper INSTANCE = Mappers.getMapper(OrderReminderVMMapper.class);

    /**
     * 复制配件单
     * 不复制明细及图片
     */
    @Mappings({
            @Mapping(target = "reminderDate",ignore = true),
            @Mapping(target = "cutOffTimeLiness",ignore = true),
            @Mapping(target = "latestProcessTimeLiness",ignore = true)
    })
    OrderReminderVM toReminderModel(Order model);

}
