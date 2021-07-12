package com.wolfking.jeesite.ms.tmall.md.mapper;

import com.wolfking.jeesite.ms.tmall.md.entity.B2bCustomerMap;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import org.apache.commons.httpclient.NameValuePair;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2021-05-18T17:10:40+0800",
    comments = "version: 1.2.0.Final, compiler: javac, environment: Java 1.8.0_181 (Oracle Corporation)"
)
public class B2bCustomerMapMapperImpl implements B2bCustomerMapMapper {

    @Override
    public NameValuePair toPair(B2bCustomerMap entity) {
        if ( entity == null ) {
            return null;
        }

        NameValuePair nameValuePair = new NameValuePair();

        nameValuePair.setName( entity.getShopName() );
        nameValuePair.setValue( entity.getShopId() );

        return nameValuePair;
    }

    @Override
    public List<NameValuePair> toPairs(List<B2bCustomerMap> entity) {
        if ( entity == null ) {
            return null;
        }

        List<NameValuePair> list = new ArrayList<NameValuePair>( entity.size() );
        for ( B2bCustomerMap b2bCustomerMap : entity ) {
            list.add( toPair( b2bCustomerMap ) );
        }

        return list;
    }
}
