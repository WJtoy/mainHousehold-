package com.wolfking.jeesite.modules.sys.entity;

public enum  UserAttributesEnum {
    CUSTOMERSHOP(1, "客户店铺");


    private int value;
    private String label;

    UserAttributesEnum(int value, String label) {
        this.value = value;
        this.label = label;
    }

    public int getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }
}
