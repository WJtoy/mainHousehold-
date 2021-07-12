package com.wolfking.jeesite.modules.md.entity.mapper;

import com.wolfking.jeesite.modules.md.entity.*;
import com.wolfking.jeesite.modules.md.entity.viewModel.MDServicePointPriceVM;
import org.mapstruct.*;

import java.util.List;

/**
 * md缓存网点价格转换为：ServicePointPrice
 */
@Mapper(collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class MDServicePointPriceVMMapper {

    @Mappings({
            @Mapping(target = "id",source = "id"),
            @Mapping(target = "delFlag",source = "delFlag"),
            @Mapping(target = "price",source = "price"),
            @Mapping(target = "discountPrice",source = "discountPrice"),
            @Mapping(target = "product",ignore = true),
            @Mapping(target = "servicePoint",ignore = true),
            @Mapping(target = "createBy",ignore = true),
            @Mapping(target = "createDate",ignore = true),
            @Mapping(target = "updateBy",ignore = true),
            @Mapping(target = "updateDate",ignore = true),
            @Mapping(target = "remarks",ignore = true),
            @Mapping(target = "currentUser",ignore = true),
            @Mapping(target = "page",ignore = true),
            @Mapping(target = "isNewRecord",ignore = true),
            @Mapping(target = "sqlMap",ignore = true),
            @Mapping(target = "priceType",ignore = true),
            @Mapping(target = "referPrice",ignore = true),
            @Mapping(target = "referDiscountPrice",ignore = true),
            @Mapping(target = "flag",ignore = true),
            @Mapping(target = "productCategory",ignore = true),
            @Mapping(target = "productIds",ignore = true),
            @Mapping(target = "customizeFlag",ignore = true),
            @Mapping(target = "unit",ignore = true),
    })
    public abstract ServicePrice toServicePointPrice(MDServicePointPriceVM model);

    @AfterMapping
    protected void after(@MappingTarget final ServicePrice model, MDServicePointPriceVM message) {
        model.setProduct(new Product(message.getProductId()));
        model.setServiceType(new ServiceType(message.getServiceTypeId()));
    }

    public abstract List<ServicePrice> toServicePointPrices(List<MDServicePointPriceVM> prices);
}
