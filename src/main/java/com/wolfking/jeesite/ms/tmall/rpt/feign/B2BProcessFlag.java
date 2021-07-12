package com.wolfking.jeesite.ms.tmall.rpt.feign;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public enum B2BProcessFlag {
    PROCESS_FLAG_ACCEPT(0, "受理", "接受成功，但是还未处理"),
    PROCESS_FLAG_PROCESSING(1, "执行", "接收成功，正在执行业务数据处理"),
    PROCESS_FLAG_REJECT(2, "拒绝", "接收成功，但是执行处理生成业务数据失败，业务数据不满足要求"),
    PROCESS_FLAG_FAILURE(3, "失败", "接收成功，但是执行处理生成业务数据报错"),
    PROCESS_FLAG_SUCESS(4, "成功", "接收成功，并且执行处理生成业务数据成功");

    public int value;
    public String label;
    public String description;
    private static final Map<Integer, B2BProcessFlag> MAP = new HashMap();

    private B2BProcessFlag(int value, String label, String description) {
        this.value = value;
        this.label = label;
        this.description = description;
    }

    public int getValue() {
        return value;
    }



    public String getLabel() {
        return label;
    }



    public String getDescription() {
        return description;
    }



    public static B2BProcessFlag get(int value) {
        return MAP.get(value);
    }

    public static List<B2BProcessFlag> getAllProcessFlags() {
        return (List)MAP.entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList());
    }
    public static B2BProcessFlag getByValue(int value) {
        for(B2BProcessFlag typeEnum : B2BProcessFlag.values()) {
            if(typeEnum.value == value) {
                return typeEnum;
            }
        }
        throw new IllegalArgumentException("No element matches " + value);
    }
    static {
        MAP.put(PROCESS_FLAG_ACCEPT.value, PROCESS_FLAG_ACCEPT);
        MAP.put(PROCESS_FLAG_PROCESSING.value, PROCESS_FLAG_PROCESSING);
        MAP.put(PROCESS_FLAG_REJECT.value, PROCESS_FLAG_REJECT);
        MAP.put(PROCESS_FLAG_FAILURE.value, PROCESS_FLAG_FAILURE);
        MAP.put(PROCESS_FLAG_SUCESS.value, PROCESS_FLAG_SUCESS);
    }
}

