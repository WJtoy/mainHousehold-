package com.wolfking.jeesite.modules.sd.entity.mapper;

import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.md.entity.Engineer;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.mq.dto.MQCommon;
import com.wolfking.jeesite.modules.mq.dto.MQOrderGradeMessage;
import com.wolfking.jeesite.modules.mq.dto.MQOrderProcessLog;
import com.wolfking.jeesite.modules.sd.entity.OrderGrade;
import com.wolfking.jeesite.modules.sd.entity.OrderProcessLog;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderGradeModel;
import com.wolfking.jeesite.modules.sys.entity.User;
import org.mapstruct.*;

/**
 * 客评消息队列与客评模型之间相互转换
 */
@Mapper(collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class OrderProcessLogMessageMapper {

    @Mappings({
            @Mapping(target = "createBy",ignore = true),
            @Mapping(target = "createDate",ignore = true),
            @Mapping(target = "currentUser",ignore = true),
            @Mapping(target = "page",ignore = true),
            @Mapping(target = "sqlMap",ignore = true),
            @Mapping(target = "isNewRecord",ignore = true)
    })
    public abstract OrderProcessLog messageToModel(MQOrderProcessLog.OrderProcessLog message);


    @AfterMapping
    protected void after(@MappingTarget final OrderProcessLog model,MQOrderProcessLog.OrderProcessLog message) {

        //createBy
        if(message.getCreateBy() != null) {
            model.setCreateBy(new User(message.getCreateBy().getId(), message.getCreateBy().getName(),""));
        }

        //createDate
        if(message.getCreateDate()>0) {
            model.setCreateDate(DateUtils.longToDate(message.getCreateDate()));
        }
    }

    public  MQOrderProcessLog.OrderProcessLog modelToMessage(OrderProcessLog model){
        if(model == null){
            return null;
        }
        MQOrderProcessLog.OrderProcessLog.Builder builder = MQOrderProcessLog.OrderProcessLog.newBuilder()
                .setId(model.getId()==null?null:model.getId())
                .setQuarter(model.getQuarter())
                .setOrderId(model.getOrderId())
                .setAction(model.getAction())
                .setActionComment(model.getActionComment())
                .setStatusFlag(model.getStatusFlag())
                .setStatus(model.getStatus())
                .setStatusValue(model.getStatusValue())
                .setCloseFlag(model.getCloseFlag())
                .setRemarks(model.getRemarks())
                .setCreateDate(model.getCreateDate().getTime())
                .setVisibilityFlag(model.getVisibilityFlag())
                .setCreateBy(
                        MQCommon.User.newBuilder()
                                .setId(model.getCreateBy().getId())
                                .setName(model.getCreateBy().getName())
                                .build()
                );

        return builder.build();
    }


}
