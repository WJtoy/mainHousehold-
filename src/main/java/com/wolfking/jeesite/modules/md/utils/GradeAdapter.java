package com.wolfking.jeesite.modules.md.utils;

import com.google.common.collect.Lists;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.md.entity.Grade;
import com.wolfking.jeesite.modules.md.entity.GradeItem;
import com.wolfking.jeesite.modules.sys.entity.Area;

import java.io.IOException;
import java.util.List;

/**
 * 客评自定义Gson序列化/序列化
 */
public class GradeAdapter extends TypeAdapter<Grade> {

    @Override
    public Grade read(final JsonReader in) throws IOException {
        final Grade model = new Grade();
        in.beginObject();
        while (in.hasNext()) {
            switch (in.nextName()) {
                case "id":
                    model.setId(in.nextLong());
                    break;
                case "name":
                    model.setName(in.nextString());
                    break;
                case "port":
                    model.setPoint(in.nextInt());
                    break;
                case "dictType":
                    if(in.peek() == JsonToken.NULL){
                        in.nextNull();
                        model.setDictType("");
                    }else{
                        model.setDictType(in.nextString());
                    }
                    break;
                case "sort":
                    model.setSort(in.nextInt());
                    break;
                case "remarks":
                    model.setRemarks(in.nextString());
                    break;
                case "itemList":
                    in.beginArray();
                    final List items = Lists.newArrayList();
                    while (in.hasNext()) {
                        items.add(GradeItemAdapter.getInstance().read(in));
                    }
                    model.setItemList(items);
                    in.endArray();
                    break;
            }
        }
        in.endObject();
        return model;
    }

    @Override
    public void write(final JsonWriter out, final Grade model) throws IOException {
        out.beginObject();
        out.name("id").value(model.getId())
            .name("name").value(model.getName())
            .name("port").value(model.getPoint())
            .name("dictType").value(StringUtils.isBlank(model.getDictType())?"":model.getDictType())
            .name("sort").value(model.getSort())
            .name("remarks").value(model.getRemarks());

        out.name("itemList").beginArray();
        for(GradeItem item:model.getItemList()){
            GradeItemAdapter.getInstance().write(out,item);
        }
        out.endArray();

        out.endObject();
    }

    private static GradeAdapter adapter;

    public GradeAdapter() {}

    public static GradeAdapter getInstance() {
        if (adapter == null){
            adapter = new GradeAdapter();
        }
        return adapter;
    }
}
