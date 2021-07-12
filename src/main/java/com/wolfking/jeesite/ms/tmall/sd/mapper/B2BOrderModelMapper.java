package com.wolfking.jeesite.ms.tmall.sd.mapper;

import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderVModel;
import com.wolfking.jeesite.ms.tmall.sd.entity.WorkcardInfoModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

@Mapper
public abstract class B2BOrderModelMapper {

    /**
     * B2B共用模型转化为天猫的数据模型
     */
    @Mappings({
            @Mapping(target = "customer",source = "customer"),
            @Mapping(target = "workcardId",source = "orderNo" ),
            @Mapping(target = "sellerShopId", source = "shopId"),
            @Mapping(target = "buyerName", source = "userName"),
            @Mapping(target = "buyerMobile", source = "userMobile"),
            @Mapping(target = "buyerPhone", source = "userPhone"),
            @Mapping(target = "buyerAddress", source = "userAddress"),
            @Mapping(target = "brand", source = "brand"),
            @Mapping(target = "serviceCode", source = "serviceType"),
            @Mapping(target = "taskMemo", source = "description"),
            @Mapping(target = "taskStatus", source = "status"),
            @Mapping(target = "processFlag", source = "processFlag"),
            @Mapping(target = "processTime", source = "processTime"),
            @Mapping(target = "processComment", source = "processComment"),
            @Mapping(target = "quarter", source = "quarter"),
            @Mapping(target = "categoryId", expression = "java(entity.getItems() != null && !entity.getItems().isEmpty() ? Long.valueOf(entity.getItems().get(0).getProductCode()) : 0)"),
            @Mapping(target = "serviceCount", expression = "java(entity.getItems() != null && !entity.getItems().isEmpty() ? entity.getItems().get(0).getQty() : 0)"),
            @Mapping(target = "auctionName", expression = "java(entity.getItems() != null && !entity.getItems().isEmpty() ? entity.getItems().get(0).getProductName() : \"\")"),
            @Mapping(target = "modelNumber", expression = "java(entity.getItems() != null && !entity.getItems().isEmpty() ? entity.getItems().get(0).getProductSpec() : \"\")"),
            @Mapping(target = "serviceLabel",ignore = true),
            @Mapping(target = "processLogLabel",ignore = true),
            @Mapping(target = "taskStatusLabel",ignore = true),
            @Mapping(target = "dataSource",ignore = true),
    })
    public abstract WorkcardInfoModel toTMallModel(B2BOrderVModel entity);

    public abstract List<WorkcardInfoModel> listToTMallModel(List<B2BOrderVModel> order);

}
