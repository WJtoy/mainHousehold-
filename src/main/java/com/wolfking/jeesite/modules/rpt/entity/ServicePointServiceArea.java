package com.wolfking.jeesite.modules.rpt.entity;


public class ServicePointServiceArea extends BaseRptEntity {

    private Long servicePointId;

    private Long areaId;
    private String areaName;

    public Long getServicePointId() {
        return servicePointId;
    }

    public void setServicePointId(Long servicePointId) {
        this.servicePointId = servicePointId;
    }

    public Long getAreaId() {
        return areaId;
    }

    public void setAreaId(Long areaId) {
        this.areaId = areaId;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }


}

