package com.wolfking.jeesite.modules.fi.entity.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.fi.entity.viewModel.CustomerCurrencyModel;
import com.wolfking.jeesite.modules.sys.entity.adapter.DictSimpleAdapter;

import java.io.IOException;

/**
 * 订单自定义Gson序列化/序列化
 */
public class CustomerCurrencyModelAdapter extends TypeAdapter<CustomerCurrencyModel> {

    @Override
    public CustomerCurrencyModel read(final JsonReader in) throws IOException {
        return null;
    }

    @Override
    public void write(final JsonWriter out, final CustomerCurrencyModel customerCurrency) throws IOException {
        out.beginObject();
        out.name("currencyNo").value(customerCurrency.getCurrencyNo());
        out.name("quarter").value(customerCurrency.getQuarter());
        out.name("actionType");
        DictSimpleAdapter.getInstance().write(out,customerCurrency.getActionType());
        out.name("amount").value(customerCurrency.getAmount());
        out.name("createDate").value(DateUtils.formatDate(customerCurrency.getCreateDate(),"yyyy-MM-dd HH:mm:ss"));

        out.endObject();
    }

}
