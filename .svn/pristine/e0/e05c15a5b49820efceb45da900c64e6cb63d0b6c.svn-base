package com.wolfking.jeesite.ms.tmall.rpt.feign;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public enum B2BActionType {
//    ACTION_TYPE_NONE(-1, ""),
    ACTION_TYPE_CREATE(0, "新增"),
    ACTION_TYPE_UPDATE(1, "更新"),
    ACTION_TYPE_DELETE(2, "删除");

    public int value;
    public String label;
    private static final Map<Integer, B2BActionType> MAP = new HashMap();

    private B2BActionType(int value, String label) {
        this.value = value;
        this.label = label;
    }

    public static B2BActionType get(int value) {
        return MAP.get(value);
    }

    public int getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    public static List<B2BActionType> getAllActionTypes() {
        return (List)MAP.entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList());
    }

    static {
//        MAP.put(ACTION_TYPE_NONE.value, ACTION_TYPE_NONE);
        MAP.put(ACTION_TYPE_CREATE.value, ACTION_TYPE_CREATE);
        MAP.put(ACTION_TYPE_UPDATE.value, ACTION_TYPE_UPDATE);
        MAP.put(ACTION_TYPE_DELETE.value, ACTION_TYPE_DELETE);
    }
}

