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
import ma.glasnost.orika.MapperFacade;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import javax.print.attribute.standard.PDLOverrideSupported;
import javax.swing.plaf.BorderUIResource;

/**
 * 客评消息队列与客评模型之间相互转换
 */
@Mapper(uses={OrderGradeItemMessageMapper.class,OrderProcessLogMessageMapper.class},
        collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class OrderGradeMessageMapper {

    @Mappings({
            @Mapping(target = "servicePoint",ignore = true),
            @Mapping(target = "engineer",ignore = true),
            @Mapping(target = "ids",ignore = true),
            @Mapping(target = "order",ignore = true),
            @Mapping(target = "checkOrderFee",ignore = true),
            //@Mapping(target = "checkCanAutoCharge",ignore = true),
            //@Mapping(target = "canAutoCharge",ignore = true),
            @Mapping(target = "timeLinessCharge",ignore = true),
            @Mapping(target = "createBy",ignore = true),
            @Mapping(target = "createDate",ignore = true),
            @Mapping(target = "content",ignore = true),
            @Mapping(target = "processLog",source = "processLog"),
            @Mapping(target = "gradeList",source = "itemsList")
    })
    public abstract OrderGradeModel mqToModel(MQOrderGradeMessage.OrderGradeMessage message);


    @AfterMapping
    protected void after(@MappingTarget final OrderGradeModel model,MQOrderGradeMessage.OrderGradeMessage message) {
        //servicePoint
        if(message.getServicePoint() != null) {
            ServicePoint servicePoint = new ServicePoint(message.getServicePoint().getId());
            servicePoint.setName(message.getServicePoint().getName());
            model.setServicePoint(servicePoint);
        }
        //engineer
        if(message.getEngineer() != null) {
            Engineer engineer = new Engineer(message.getEngineer().getId());
            engineer.setName(message.getEngineer().getName());
            model.setEngineer(engineer);
        }
        //createBy
        if(message.getCreateBy() != null) {
            model.setCreateBy(new User(message.getCreateBy().getId(), message.getCreateBy().getName(),""));
        }

        //createDate
        if(message.getCreateDate()>0) {
            model.setCreateDate(DateUtils.longToDate(message.getCreateDate()));
        }
        for(MQOrderProcessLog.OrderProcessLog item:message.getLogsList()){
            OrderProcessLog orderProcessLog =  Mappers.getMapper( OrderProcessLogMessageMapper.class ).messageToModel(item);
            model.getFeeProcessLogs().add(orderProcessLog);
        }
    }

    public  MQOrderGradeMessage.OrderGradeMessage modelToMq(OrderGradeModel model){
        if(model == null){
            return null;
        }
        MQOrderGradeMessage.OrderGradeMessage.Builder builder = MQOrderGradeMessage.OrderGradeMessage.newBuilder()
                .setId(model.getId()==null?model.getOrderId():model.getId())
                .setQuarter(model.getQuarter())
                .setOrderId(model.getOrderId())
                .setOrderNo(model.getOrderNo())
                .setPoint(model.getPoint())
                .setTimeLiness(model.getTimeLiness())
                .setRushCloseFlag(model.getRushCloseFlag())
                .setCreateDate(model.getCreateDate().getTime())
                .setCreateBy(
                        MQCommon.User.newBuilder()
                                .setId(model.getCreateBy().getId())
                                .setName(model.getCreateBy().getName())
                                .build()
                )
                .setServicePoint(
                        MQCommon.User.newBuilder()
                                .setId(model.getServicePoint().getId())
                                .setName(model.getServicePoint().getName())
                                .build()
                )
                .setEngineer(
                        MQCommon.User.newBuilder()
                                .setId(model.getEngineer().getId())
                                .setName(model.getEngineer().getName())
                                .build()
                );
       MQOrderProcessLog.OrderProcessLog processLog = Mappers.getMapper(OrderProcessLogMessageMapper.class).modelToMessage(model.getProcessLog());
        builder.setProcessLog(processLog);
    /*    if(model.getKefuProcessLog()!=null){
            MQOrderProcessLog.OrderProcessLog kefuProcessLog = Mappers.getMapper(OrderProcessLogMessageMapper.class).modelToMessage(model.getKefuProcessLog());
            if(kefuProcessLog!=null){
                builder.addLogs(kefuProcessLog);
            }
        }
        if(model.getCustomerProcesslLog()!=null){
            MQOrderProcessLog.OrderProcessLog customerProcessLog = Mappers.getMapper(OrderProcessLogMessageMapper.class).modelToMessage(model.getCustomerProcesslLog());
            if(customerProcessLog!=null){
                builder.addLogs(customerProcessLog);
            }
        }*/
        for(OrderProcessLog item:model.getFeeProcessLogs()){
            MQOrderProcessLog.OrderProcessLog feeProcessLog = Mappers.getMapper(OrderProcessLogMessageMapper.class).modelToMessage(item);
            if(feeProcessLog!=null){
                builder.addLogs(feeProcessLog);
            }
        }
        for(OrderGrade item:model.getGradeList()){
            builder.addItems(
                    MQOrderGradeMessage.GradeItemMessage.newBuilder()
                            .setGradeId(item.getGradeId())
                            .setGradeName(item.getGradeName())
                            .setGradeItemId(item.getGradeItemId())
                            .setGradeItemName(item.getGradeItemName())
                            .setSort(item.getSort())
                            .setPoint(item.getPoint())
                            .build()
            );
        }
        return builder.build();
    }


}
