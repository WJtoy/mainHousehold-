package com.wolfking.jeesite.modules.md.entity.viewModel;

import com.google.common.collect.Lists;
import com.wolfking.jeesite.common.persistence.zTreeEntity;

import java.io.Serializable;
import java.util.List;

public class ServiceAreaModel implements Serializable {

    private zTreeEntity entity = new zTreeEntity();
    private List<ServiceAreaModel> models = Lists.newArrayList();

    public zTreeEntity getEntity() {
        return entity;
    }

    public void setEntity(zTreeEntity entity) {
        this.entity = entity;
    }

    public List<ServiceAreaModel> getModels() {
        return models;
    }

    public void setModels(List<ServiceAreaModel> models) {
        this.models = models;
    }
}
