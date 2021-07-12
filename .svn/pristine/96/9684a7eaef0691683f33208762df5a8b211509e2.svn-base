package com.wolfking.jeesite.ms.providermd.service;

import com.google.common.collect.Lists;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.entity.md.MDTimelinessLevel;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.modules.md.entity.TimelinessLevel;
import com.wolfking.jeesite.ms.providermd.feign.MSTimelinessLevelFeign;
import com.wolfking.jeesite.ms.providermd.utils.MDUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MSTimelinessLevelService {

    @Autowired
    private MSTimelinessLevelFeign msTimelinessLevelFeign;

    /**
     * 根据id获取时效等级
     * @param id
     * @return
     */
    public TimelinessLevel getById(Long id) {
        return MDUtils.getById(id, TimelinessLevel.class, msTimelinessLevelFeign::getById);
    }


    /**
     * 缓存获取所有数据
     * @return
     */
    public List<TimelinessLevel> findAllList() {
         List<TimelinessLevel> list = MDUtils.findAllList(TimelinessLevel.class, msTimelinessLevelFeign::findAllList);
         if(list !=null && list.size()>0){
              return list.stream().sorted(Comparator.comparing(TimelinessLevel::getId)).collect(Collectors.toList());
         }else{
             return Lists.newArrayList();
         }
    }

    /**
     * 获取分页数据
     * @param timelinessLevelPage
     * @param timelinessLevel
     * @return
     */
    public Page<TimelinessLevel> findList(Page<TimelinessLevel> timelinessLevelPage, TimelinessLevel timelinessLevel) {
        return MDUtils.findListForPage(timelinessLevelPage, timelinessLevel, TimelinessLevel.class, MDTimelinessLevel.class, msTimelinessLevelFeign::findList);
    }


    /**
     * 添加/更新
     * @param timelinessLevel
     * @param isNew
     * @return
     */
    public MSErrorCode save(TimelinessLevel timelinessLevel, boolean isNew) {
        return MDUtils.genericSave(timelinessLevel, MDTimelinessLevel.class, isNew, isNew?msTimelinessLevelFeign::insert:msTimelinessLevelFeign::update);
    }

    /**
     * 删除
     * @param timelinessLevel
     * @return
     */
    public MSErrorCode delete(TimelinessLevel timelinessLevel) {
        return MDUtils.genericSave(timelinessLevel, MDTimelinessLevel.class, false, msTimelinessLevelFeign::delete);
    }
}
