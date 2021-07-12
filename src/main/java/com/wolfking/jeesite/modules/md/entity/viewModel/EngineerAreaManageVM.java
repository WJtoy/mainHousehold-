package com.wolfking.jeesite.modules.md.entity.viewModel;

import com.google.common.collect.Lists;
import com.wolfking.jeesite.common.persistence.zTreeEntity;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.sys.entity.Area;

import java.io.Serializable;
import java.util.List;

/**
 * 维护安维区域
 * Created by yanshenglu on 2017/5/3.
 */
public class EngineerAreaManageVM implements Serializable {

    public EngineerAreaManageVM(){}

    private List<zTreeEntity> serviceAreas = Lists.newArrayList();
    private List<Long> areaIds = Lists.newArrayList();


    public List<zTreeEntity> getServiceAreas() {
        return serviceAreas;
    }

    public void setServiceAreas(List<zTreeEntity> serviceAreas) {
        this.serviceAreas = serviceAreas;
    }

    public List<Long> getAreaIds() {
        return areaIds;
    }

    public void setAreaIds(List<Long> areaIds) {
        this.areaIds = areaIds;
    }
}
