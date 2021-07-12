package com.wolfking.jeesite.modules.md.utils;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.md.entity.Grade;
import com.wolfking.jeesite.modules.md.entity.GradeItem;
import com.wolfking.jeesite.modules.sys.entity.Area;

import java.io.IOException;

/**
 * 客评Item自定义Gson序列化/序列化
 */
public class GradeItemAdapter extends TypeAdapter<GradeItem> {

    @Override
    public GradeItem read(final JsonReader in) throws IOException {
        final GradeItem model = new GradeItem();
        in.beginObject();
        while (in.hasNext()) {
            switch (in.nextName()) {
                case "id":
                    model.setId(in.nextLong());
                    break;
                case "remarks":
                    model.setRemarks(in.nextString());
                    break;
                case "point":
                    model.setPoint(in.nextInt());
                    break;
                case "grade":
                    Grade grade = new Grade();
                    in.beginObject();
                    while (in.hasNext()) {
                        switch (in.nextName()) {
                            case "id":
                                grade.setId(in.nextLong());
                                break;
                            case "name":
                                grade.setName(in.nextString());
                                break;
                        }
                    }
                    model.setGrade(grade);
                    in.endObject();
                    break;
                case "dictValue":
                    if(in.peek() == JsonToken.NULL){
                        in.nextNull();
                        model.setDictValue("");
                    }else{
                        model.setDictValue(in.nextString());
                    }
                    break;
            }
        }
        in.endObject();
        return model;
    }

    @Override
    public void write(final JsonWriter out, final GradeItem model) throws IOException {
        out.beginObject();
        out.name("id").value(model.getId());
        out.name("remarks").value(model.getRemarks());
        out.name("point").value(model.getPoint());
        out.name("grade").beginObject()
                .name("id").value(model.getGrade().getId())
                .name("name").value(model.getGrade().getName())
                .endObject();
        out.name("dictValue").value(StringUtils.isBlank(model.getDictValue())?"":model.getDictValue());
        out.endObject();
    }

    private static GradeItemAdapter adapter;

    public GradeItemAdapter() {}

    public static GradeItemAdapter getInstance() {
        if (adapter == null){
            adapter = new GradeItemAdapter();
        }
        return adapter;
    }
}
