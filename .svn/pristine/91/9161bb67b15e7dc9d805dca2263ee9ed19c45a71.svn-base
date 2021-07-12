package com.wolfking.jeesite.modules.md.utils;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.wolfking.jeesite.modules.md.entity.CustomerShop;

import java.io.IOException;

public class CustomerShopAdapter extends TypeAdapter<CustomerShop> {
    @Override
    public void write(final JsonWriter out, final CustomerShop customerShop) throws IOException {
        out.beginObject();
        out.name("id").value(customerShop.getId());
        out.name("name").value(customerShop.getName());
        out.name("dataSource").value(customerShop.getDataSource());
        out.endObject();
    }

    @Override
    public CustomerShop read(JsonReader in) throws IOException {
        final CustomerShop customerShop = new CustomerShop();
        in.beginObject();
        while (in.hasNext()) {
            switch (in.nextName()) {
                case "id":
                    customerShop.setId(in.nextString());
                    break;
                case "name":
                    customerShop.setName(in.nextString());
                    break;
                case "dataSource":
                    customerShop.setDataSource(in.nextInt());
                    break;
                default:
                    break;
            }
        }

        in.endObject();
        return customerShop;
    }
}
