package com.wolfking.jeesite.ms.providermd.service;

import com.google.common.collect.Lists;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.entity.md.MDUrgentLevel;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.modules.md.entity.UrgentLevel;
import com.wolfking.jeesite.ms.providermd.feign.MSUrgentLevelFeign;
import com.wolfking.jeesite.ms.providermd.utils.MDUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MSUrgentLevelService {

    @Autowired
    private MSUrgentLevelFeign msUrgentLevelFeign;

    /**
     * 根据id获取加急等级
     * @param id
     * @return
     */
    public UrgentLevel getById(Long id) {
        return MDUtils.getById(id, UrgentLevel.class, msUrgentLevelFeign::getById);
    }

    /**
     * 根据id从缓存获取加急等级
     * @param id
     * @return
     */
    public UrgentLevel getFromCache(Long id){
        return MDUtils.getById(id, UrgentLevel.class, msUrgentLevelFeign::getFromCache);
    }


    /**
     * 缓存获取所有数据
     * @return
     */
    public List<UrgentLevel> findAllList() {
         List<UrgentLevel> list = MDUtils.findAllList(UrgentLevel.class, msUrgentLevelFeign::findAllList);
         if(list !=null && list.size()>0){
              return list.stream().sorted(Comparator.comparing(UrgentLevel::getId)).collect(Collectors.toList());
         }else{
             return Lists.newArrayList();
         }
    }

    /**
     * 获取分页数据
     * @param urgentLevelPage
     * @param urgentLevel
     * @return
     */
    public Page<UrgentLevel> findList(Page<UrgentLevel> urgentLevelPage, UrgentLevel urgentLevel) {
        return MDUtils.findListForPage(urgentLevelPage, urgentLevel, UrgentLevel.class, MDUrgentLevel.class, msUrgentLevelFeign::findList);
    }


    /**
     * 添加/更新
     * @param urgentLevel
     * @param isNew
     * @return
     */
    public MSErrorCode save(UrgentLevel urgentLevel, boolean isNew) {
        return MDUtils.genericSave(urgentLevel, MDUrgentLevel.class, isNew, isNew?msUrgentLevelFeign::insert:msUrgentLevelFeign::update);
    }

    /**
     * 删除
     * @param urgentLevel
     * @return
     */
    public MSErrorCode delete(UrgentLevel urgentLevel) {
        return MDUtils.genericSave(urgentLevel, MDUrgentLevel.class, false, msUrgentLevelFeign::delete);
    }
}
