package com.wolfking.jeesite.ms.enums;

import com.google.common.collect.Lists;
import com.wolfking.jeesite.modules.sys.entity.Dict;

import java.util.List;

public enum OrderStatusType {

    NEW(10, "下单"),
    APPROVED(20, "待接单"),
    ACCEPTED(30, "已接单"),
    PLANNED(40, "已派单"),
    SERVICED(50, "已上门"),
    APP_COMPLETED(55, "待回访"),
    RETURNING(60, "退单申请"),
    CANCELING(70, "取消中"),
    COMPLETED(80, "完成"),
    CHARGED(85, "已入账"),
    RETURNED(90, "已退单"),
    CANCELED(100, "已取消");

    public int value;
    public String label;

    OrderStatusType(int value, String label) {
        this.value = value;
        this.label = label;
    }

    public static List<Dict> getAllOrderStatusTypes() {
        List<Dict> list = Lists.newArrayList();
        list.add(new Dict(NEW.value, NEW.label));
        list.add(new Dict(APPROVED.value, APPROVED.label));
        list.add(new Dict(ACCEPTED.value, ACCEPTED.label));
        list.add(new Dict(PLANNED.value, PLANNED.label));
        list.add(new Dict(APP_COMPLETED.value, APP_COMPLETED.label));
        list.add(new Dict(SERVICED.value, SERVICED.label));
        list.add(new Dict(RETURNING.value, RETURNING.label));
        list.add(new Dict(CANCELING.value, CANCELING.label));
        list.add(new Dict(COMPLETED.value, COMPLETED.label));
        list.add(new Dict(CHARGED.value, CHARGED.label));
        list.add(new Dict(RETURNED.value, RETURNED.label));
        list.add(new Dict(CANCELED.value, CANCELED.label));
        return list;
    }


    public static Dict toDict(OrderStatusType statusType) {
        if (statusType == null) {
            return null;
        }
        return new Dict(statusType.value, statusType.label);
    }
}
