package com.wolfking.jeesite.modules.md.entity.viewModel;

import com.google.common.collect.Lists;
import com.wolfking.jeesite.common.persistence.IntegerRange;
import com.wolfking.jeesite.modules.md.entity.Product;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.md.entity.ServicePrice;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.User;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by yanshenglu on 2017/5/3.
 */
public class ServicePointModel extends ServicePoint {

    public ServicePointModel(){
        super.setAutoPlanFlag(-1);
        super.setInsuranceFlag(-1);
        super.setTimeLinessFlag(-1);
        super.setUseDefaultPrice(-1);
    }

    private Integer searchType = 0;//0-按网点名称查询,1-按网点编号查询
    private String layerIndex; //layer弹窗索引，刷新时top.layer.index会变
    private IntegerRange levelRange = null;

    private Area city;
    //区域类型
    private Integer areaLevel;//0 省 1市 2区

    private String parentLayerIndex;

    private Long canRush = 0L; //可突击标识

    public Integer getSearchType() {
        return searchType;
    }

    public void setSearchType(Integer searchType) {
        this.searchType = searchType;
    }

    public String getLayerIndex() {
        return layerIndex;
    }

    public void setLayerIndex(String layerIndex) {
        this.layerIndex = layerIndex;
    }

    public IntegerRange getLevelRange() {
        return levelRange;
    }

    public void setLevelRange(IntegerRange levelRange) {
        this.levelRange = levelRange;
    }

    public Area getCity() {
        return city;
    }

    public void setCity(Area city) {
        this.city = city;
    }

    public Integer getAreaLevel() {
        return areaLevel;
    }

    public void setAreaLevel(Integer areaLevel) {
        this.areaLevel = areaLevel;
    }

    public String getParentLayerIndex() {
        return parentLayerIndex;
    }

    public void setParentLayerIndex(String parentLayerIndex) {
        this.parentLayerIndex = parentLayerIndex;
    }

    public Long getCanRush() {
        return canRush;
    }

    public void setCanRush(Long canRush) {
        this.canRush = canRush;
    }
}
