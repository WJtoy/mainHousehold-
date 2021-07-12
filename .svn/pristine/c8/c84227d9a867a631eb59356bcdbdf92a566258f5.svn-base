package com.wolfking.jeesite.ms.recharge.entity.mapper;


import com.kkl.kklplus.entity.fi.mq.MQRechargeOrderMessage;
import com.wolfking.jeesite.modules.fi.entity.CustomerCurrency;
import com.wolfking.jeesite.modules.mq.entity.MQRecharge;
import org.mapstruct.*;

/**
 * 客户充值类转换方法
 */
@Mapper(collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class RechargeModelMapper {

    /**
     * 充值成功消息队列转客户流水
     */
    @Mappings({
            @Mapping(target = "id",source = "id"),
            @Mapping(target = "customer.id",source = "referId"),
            @Mapping(target = "currencyNo",source = "tradeNo"),
            @Mapping(target = "quarter",expression = "java(com.wolfking.jeesite.common.utils.QuarterUtils.getSeasonQuarter(message.getCreateAt()))"),
            @Mapping(target = "amount",source = "amount"),
            @Mapping(target = "paymentType",constant = "20"),
            @Mapping(target = "actionType",constant = "10"),
            @Mapping(target = "currencyType",constant = "10"),
            @Mapping(target = "remarks",source = "remarks"),
            @Mapping(target = "createBy.id",source = "createBy"),
            @Mapping(target = "createDate",expression = "java(com.wolfking.jeesite.common.utils.DateUtils.longToDate(message.getCreateAt()))"),
            @Mapping(target = "updateDate",expression = "java(new java.util.Date())"),
            @Mapping(target = "updateBy.id",constant = "0"),
            @Mapping(target = "delFlag",ignore = true),
    })
    public abstract CustomerCurrency mqToCustomerCurrency(MQRechargeOrderMessage.RechargeOrderMessage message);

}
