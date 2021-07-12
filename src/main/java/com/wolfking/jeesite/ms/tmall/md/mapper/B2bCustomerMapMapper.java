package com.wolfking.jeesite.ms.tmall.md.mapper;

import com.wolfking.jeesite.ms.tmall.md.entity.B2bCustomerMap;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface B2bCustomerMapMapper {

    B2bCustomerMapMapper INSTANCE = Mappers.getMapper(B2bCustomerMapMapper.class);

    /**
     * 查询数据模型
     * @param entity
     * @return
     */
    @Mappings({
            @Mapping(source = "shopId", target = "value"),
            @Mapping(source = "shopName", target = "name")
    })
    NameValuePair toPair(B2bCustomerMap entity);


    List<NameValuePair> toPairs(List<B2bCustomerMap> entity);
}
