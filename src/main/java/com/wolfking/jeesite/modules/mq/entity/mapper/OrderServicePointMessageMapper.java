package com.wolfking.jeesite.modules.mq.entity.mapper;

import com.wolfking.jeesite.modules.mq.dto.MQOrderServicePointMessage;
import com.wolfking.jeesite.modules.sd.entity.OrderServicePoint;
import org.mapstruct.*;

/**
 * 网点订单消息与模型之间相互转换
 */
@Mapper(collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class OrderServicePointMessageMapper {
    @Mappings({
            @Mapping(target = "id",source = "id"),
            @Mapping(target = "orderId",source = "orderId"),
            @Mapping(target = "orderNo",source = "orderInfo.orderNo"),
            @Mapping(target = "quarter",source = "quarter"),
            @Mapping(target = "orderServiceType",source = "orderInfo.orderServiceType"),
            @Mapping(target = "userName",source = "userInfo.userName"),
            @Mapping(target = "servicePhone",source = "userInfo.phone"),
            @Mapping(target = "serviceAddress",source = "userInfo.address"),
            @Mapping(target = "area.id",source = "orderInfo.areaId"),
            @Mapping(target = "area.name",source = "orderInfo.areaName"),
            @Mapping(target = "status.value",source = "orderInfo.status"),
            @Mapping(target = "subStatus",source = "subStatus"),
            @Mapping(target = "servicePointId",source = "servicePointInfo.servicePointId"),
            @Mapping(target = "engineer.id",source = "servicePointInfo.engineerId"),
            @Mapping(target = "createBy.id",source = "operationBy"),
            @Mapping(target = "createDate",expression = "java(com.wolfking.jeesite.common.utils.DateUtils.longToDate(message.getOperationAt()))"),
            @Mapping(target = "updateBy.id",source = "operationBy"),
            @Mapping(target = "updateDate",expression = "java(com.wolfking.jeesite.common.utils.DateUtils.longToDate(message.getOperationAt()))"),
            @Mapping(target = "reservationDate",expression = "java(com.wolfking.jeesite.common.utils.DateUtils.longToDate(message.getReservationDate()))"),
            @Mapping(target = "reservationAt",source = "reservationDate"),
            @Mapping(target = "planDate",expression = "java(com.wolfking.jeesite.common.utils.DateUtils.longToDate(message.getPlanDate()))"),
            @Mapping(target = "planAt",source = "planDate"),
            @Mapping(target = "pendingType",source = "pendingType"),
            @Mapping(target = "appointmentAt",source = "appointmentDate"),
            @Mapping(target = "planOrder",source = "servicePointInfo.planOrder"),
            @Mapping(target = "planType",source = "servicePointInfo.planType"),
            @Mapping(target = "closeAt",source = "closeDate"),
            @Mapping(target = "chargeAt",source = "chargeDate"),
            @Mapping(target = "masterFlag",source = "masterFlag"),// 2020-02-14
            @Mapping(target = "reminderFlag",source = "reminderFlag"),// 2020-02-14
            @Mapping(target = "complainFlag",source = "complainFlag"),// 2020-02-14
            @Mapping(target = "abnormalyFlag",source = "abnormalyFlag"),// 2020-02-14
            @Mapping(target = "appCompleteType",source = "appCompleteType"),// 2020-02-14
            @Mapping(target = "urgentLevelId",source = "urgentLevelId"),// 2020-02-14
            @Mapping(target = "orderChannel",source = "orderChannel"),// 2020-02-14
            @Mapping(target = "dataSource",source = "dataSource"),// 2020-02-14

            @Mapping(target = "appointmentDate",ignore = true),
            @Mapping(target = "closeDate",ignore = true),
            @Mapping(target = "chargeDate",ignore = true),
            @Mapping(target = "delFlag",ignore = true),
            //@Mapping(target = "reservationDate",ignore = true),
            //@Mapping(target = "planDate",ignore = true),
    })
    public abstract OrderServicePoint mqToModel(MQOrderServicePointMessage.ServicePointMessage message);



}
