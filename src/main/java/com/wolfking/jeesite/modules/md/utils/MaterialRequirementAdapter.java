package com.wolfking.jeesite.modules.md.utils;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.kkl.kklplus.entity.md.MDMaterialRequirement;
import com.wolfking.jeesite.modules.md.entity.MaterialCategory;

import java.io.IOException;

public class MaterialRequirementAdapter extends TypeAdapter<MDMaterialRequirement> {
    @Override
    public MDMaterialRequirement read(final JsonReader in) throws IOException {
        final MDMaterialRequirement model = new MDMaterialRequirement();
        in.beginObject();
        while (in.hasNext()) {
            switch (in.nextName()) {
                case "url":
                    model.setUrl(in.nextString());
                    break;
                case "visibleFlag":
                    model.setVisibleFlag(in.nextInt());
                    break;
                case "mustFlag":
                    model.setMustFlag(in.nextInt());
                    break;
            }
        }
        in.endObject();
        return model;
    }

    @Override
    public void write(final JsonWriter out, final MDMaterialRequirement model) throws IOException {
        out.beginObject();
        out.name("url").value(model.getUrl());
        out.name("visibleFlag").value(model.getVisibleFlag());
        out.name("mustFlag").value(model.getMustFlag());
        out.endObject();
    }

    private static MaterialRequirementAdapter adapter;

    public MaterialRequirementAdapter() {}

    public static MaterialRequirementAdapter getInstance() {
        if (adapter == null){
            adapter = new MaterialRequirementAdapter();
        }
        return adapter;
    }
}
