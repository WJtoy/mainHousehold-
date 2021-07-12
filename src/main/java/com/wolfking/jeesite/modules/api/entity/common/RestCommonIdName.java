package com.wolfking.jeesite.modules.api.entity.common;

/**
 * 公共请求实体,请求为id,name时可使用
 */
public class RestCommonIdName {
    private String id;
    private String name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
