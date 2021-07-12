package com.wolfking.jeesite.modules.sys.entity;

/**
 * @author Ryan Lu
 * @version 1.0.0
 * 客服类型
 * @date 2019/12/11 6:02 下午
 */
public enum KefuTypeEnum {

    SuperKefu(0,"超级客服",null,null),
    VIPKefu(1,"VIP客服",1,0),
    Kefu(2,"普通客服",0,0),
    Rush(3,"突击客服",null,2),
    AutomaticKefu(4,"自动客服",null,1),
    COMMON_KEFU(5,"大客服(含KA)",null,0);

    public Integer code;
    public String msg;
    public Integer customerType;//控制客服查看工单列表是否可查看vip客户工单 0:查看非vip工单,1:查看vip客户工单,null:查看所有客户
    public Integer kefuType;//查看工单客服类型0:大客服,1:自动客服,2:突击客服

    private KefuTypeEnum(Integer code, String msg,Integer customerType,Integer kefuType) {
        this.code = code;
        this.msg = msg;
        this.customerType = customerType;
        this.kefuType =kefuType;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Integer getCustomerType() {
        return customerType;
    }

    public void setCustomerType(Integer customerType) {
        this.customerType = customerType;
    }

    public Integer getKefuType() {
        return kefuType;
    }

    public void setKefuType(Integer kefuType) {
        this.kefuType = kefuType;
    }

    public static KefuTypeEnum fromCode(int code) {
        for (KefuTypeEnum type : KefuTypeEnum.values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }
}
