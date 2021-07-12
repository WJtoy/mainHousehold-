package com.wolfking.jeesite.modules.sd.entity.viewModel;

import com.google.gson.annotations.JsonAdapter;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.config.redis.GsonIgnore;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.entity.adapter.UserSimpleAdapter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.util.ObjectUtils;

import java.io.Serializable;
import java.util.List;

/**
 * @author Ryan Lu
 * @version 1.0.0
 * @{NAME} 〈一句话功能简述〉<br>
 * @date 2020/4/21 10:48 下午
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegionSearchModel<T> implements Serializable {

    private static final long serialVersionUID = 1587481011773L;

    /**
     * 当前实体分页对象
     */
    @GsonIgnore
    protected Page<T> page;

    @JsonAdapter(UserSimpleAdapter.class)
    private User createBy;	// 创建者

    private String remarks = StringUtils.EMPTY;

    private Area area;//2018-05-25 lzx
    private Integer areaLevel;//0 省 1市 2区
    /**
     * 账号负责的品类
     */
    private List<Long> userCategoryList;
    /**
     * 查询全品类标识
     */
    private Integer queryAllCategory = 0;

    //以下为账号本身负责的区域
    private List<Long> provinceList; //省
    private List<Long> cityList; //市
    private List<Long> areaList; //区
    private String regionFilterType; //区域筛选类型，用于xml中动态设置
    private int regionFilterCount;//区域筛选分类数，如按省+市=2，按省市区=3，按市区=2

    public int getRegionFilterCount(){
        int cnt = 0;
        if(!ObjectUtils.isEmpty(provinceList)){
            cnt++;
        }
        if(!ObjectUtils.isEmpty(cityList)){
            cnt++;
        }
        if(!ObjectUtils.isEmpty(areaList)){
            cnt++;
        }
        this.regionFilterCount = cnt;
        return this.regionFilterCount;
    }
}
