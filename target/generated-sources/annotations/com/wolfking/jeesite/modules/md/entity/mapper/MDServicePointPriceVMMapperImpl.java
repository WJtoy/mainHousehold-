package com.wolfking.jeesite.modules.md.entity.mapper;

import com.wolfking.jeesite.modules.md.entity.ServicePrice;
import com.wolfking.jeesite.modules.md.entity.viewModel.MDServicePointPriceVM;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2021-05-18T17:10:41+0800",
    comments = "version: 1.2.0.Final, compiler: javac, environment: Java 1.8.0_181 (Oracle Corporation)"
)
public class MDServicePointPriceVMMapperImpl extends MDServicePointPriceVMMapper {

    @Override
    public ServicePrice toServicePointPrice(MDServicePointPriceVM model) {
        if ( model == null ) {
            return null;
        }

        ServicePrice servicePrice = new ServicePrice();

        servicePrice.setDiscountPrice( model.getDiscountPrice() );
        servicePrice.setDelFlag( model.getDelFlag() );
        servicePrice.setPrice( model.getPrice() );
        servicePrice.setId( model.getId() );

        after( servicePrice, model );

        return servicePrice;
    }

    @Override
    public List<ServicePrice> toServicePointPrices(List<MDServicePointPriceVM> prices) {
        if ( prices == null ) {
            return null;
        }

        List<ServicePrice> list = new ArrayList<ServicePrice>( prices.size() );
        for ( MDServicePointPriceVM mDServicePointPriceVM : prices ) {
            list.add( toServicePointPrice( mDServicePointPriceVM ) );
        }

        return list;
    }
}
