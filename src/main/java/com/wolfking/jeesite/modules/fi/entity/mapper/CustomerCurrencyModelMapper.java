package com.wolfking.jeesite.modules.fi.entity.mapper;

import com.wolfking.jeesite.modules.fi.entity.CustomerCurrency;
import com.wolfking.jeesite.modules.fi.entity.viewModel.CustomerCurrencyModel;
import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MappingContext;
import org.springframework.stereotype.Component;

@Component
public class CustomerCurrencyModelMapper extends CustomMapper<CustomerCurrency, CustomerCurrencyModel> {

    @Override
    public void mapAtoB(CustomerCurrency a, CustomerCurrencyModel b, MappingContext context) {
        b.setQuarter(a.getQuarter());
        b.setAmount(a.getAmount());
        b.setCurrencyNo(a.getCurrencyNo());
        b.setCreateDate(a.getCreateDate());
    }

    @Override
    public void mapBtoA(CustomerCurrencyModel b, CustomerCurrency a, MappingContext context) {

    }
}
