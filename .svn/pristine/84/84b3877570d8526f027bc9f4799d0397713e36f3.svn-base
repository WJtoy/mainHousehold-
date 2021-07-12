package com.wolfking.jeesite.ms.providermd.service;

import com.google.common.collect.Lists;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.entity.md.MDGrade;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.modules.md.entity.Grade;
import com.wolfking.jeesite.modules.md.entity.GradeItem;
import com.wolfking.jeesite.modules.sd.entity.OrderGrade;
import com.wolfking.jeesite.ms.providermd.feign.MSGradeFeign;
import com.wolfking.jeesite.ms.providermd.utils.MDUtils;
import ma.glasnost.orika.MapperFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MSGradeService {

    @Autowired
    private MSGradeFeign msGradeFeign;

    @Autowired
    private MapperFacade mapper;

    /**
     * 根据id获取客评信息
     * @param id
     * @return
     */
    public Grade getById(Long id) {
        return MDUtils.getById(id, Grade.class, msGradeFeign::getById);
    }


    /**
     * 获取所有数据
     * @return
     */
    public List<Grade> findAllList() {
        return MDUtils.findAllList(Grade.class, msGradeFeign::findAllList);
    }


    /**
     * 获取分页数据
     * @param gradePage
     * @param grade
     * @return
     */
    public Page<Grade> findList(Page<Grade> gradePage, Grade grade) {
        return MDUtils.findListForPage(gradePage, grade, Grade.class, MDGrade.class, msGradeFeign::findList);
    }


    /**
     * 添加/更新
     * @param grade
     * @param isNew
     * @return
     */
    public MSErrorCode save(Grade grade, boolean isNew) {
        return MDUtils.genericSave(grade, MDGrade.class, isNew, isNew?msGradeFeign::insert:msGradeFeign::update);
    }

    /**
     * 删除
     * @param grade
     * @return
     */
    public MSErrorCode delete(Grade grade) {
        return MDUtils.genericSave(grade, MDGrade.class, false, msGradeFeign::delete);
    }

    /**
     * 获取所有客评项目
     * @return
     */
    public List<Grade> findAllEnabledGradeAndItems(){
        return MDUtils.findAllList(Grade.class, msGradeFeign::findAllEnabledGradeAndItems);
    }

    /**
     * 读取客评项目 (for 订单)
     *
     * @return
     */
    public List<OrderGrade> getToOrderGrade(){
        List<Grade> list = findAllEnabledGradeAndItems();
        List<OrderGrade> orderGradeList = mapper.mapAsList(list,OrderGrade.class);
        if(orderGradeList!=null && orderGradeList.size()>0){
            return orderGradeList;
        }else{
            return Lists.newArrayList();
        }
    }


    /**
     * 从数据库中获取所有客评项目以及客评标准,用于web端的rides缓存
     * @return
     */
    public List<Grade> findAllGradeListToLoadCache(){
        List<Grade> list = findAllEnabledGradeAndItems();
        return synGrade(list);

    }

    /**
     * 从微服务缓存中获取所有客评项目以及客评标准
     * @return
     */
    public List<Grade> findAllGradeListFromCache(){
        List<Grade> list =  findAllList();
        return synGrade(list);
    }

    /**
     * 给gradeItem的grade属性赋值
     * @return
     */
    public  List<Grade> synGrade(List<Grade> list){
        if(list!=null && list.size()>0){
            for(Grade item:list){
                if(item.getItemList()!=null && item.getItemList().size()>0){
                    Grade grade;
                    for(GradeItem gradeItem:item.getItemList()){
                        grade = new Grade();
                        grade.setId(item.getId());
                        grade.setName(item.getName());
                        gradeItem.setGrade(grade);
                    }
                }
            }
            return list;
        }else{
            return Lists.newArrayList();
        }
    }

}
