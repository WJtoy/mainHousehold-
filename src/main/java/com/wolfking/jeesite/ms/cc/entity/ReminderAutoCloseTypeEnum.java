package com.wolfking.jeesite.ms.cc.entity;

/**
 * @author Ryan Lu
 * @version 1.0.0
 * 催单自动关闭类型
 * @date 2019-08-15 14:32
 */
public enum ReminderAutoCloseTypeEnum {

    All(0,"所有"),
    OrderCancel(1,"订单已取消"),
    OrderReturn(2,"订单退单并通过审核"),
    OrderComplete(3,"订单完成");

    public int code;
    public String msg;

    private ReminderAutoCloseTypeEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public static ReminderAutoCloseTypeEnum fromCode(int code) {
        for (ReminderAutoCloseTypeEnum type : ReminderAutoCloseTypeEnum.values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }
}
