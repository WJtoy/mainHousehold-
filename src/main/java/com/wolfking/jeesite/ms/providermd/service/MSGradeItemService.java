package com.wolfking.jeesite.ms.providermd.service;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.md.MDGradeItem;
import com.wolfking.jeesite.modules.md.entity.GradeItem;
import com.wolfking.jeesite.ms.providermd.feign.MSGradeItemFeign;
import com.wolfking.jeesite.ms.providermd.utils.MDUtils;
import ma.glasnost.orika.MapperFacade;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class MSGradeItemService {

    @Autowired
    private MSGradeItemFeign msGradeItemFeign;

    @Autowired
    private MapperFacade mapper;

    /**
     * 根据id获取客评信息
     * @param id
     * @return
     */
    public GradeItem getById(Long id) {
        return MDUtils.getById(id, GradeItem.class, msGradeItemFeign::getById);
    }


    /**
     * 添加/更新
     * @param gradeItem
     * @param isNew
     * @return
     */
    public MSErrorCode save(GradeItem gradeItem, boolean isNew) {
        return MDUtils.genericSave(gradeItem, MDGradeItem.class, isNew, isNew?msGradeItemFeign::insert:msGradeItemFeign::update);
    }

    /**
     * 删除
     * @param gradeItem
     * @return
     */
    public MSErrorCode delete(GradeItem gradeItem) {
        return MDUtils.genericSave(gradeItem, MDGradeItem.class, false, msGradeItemFeign::delete);
    }

    /**
     * 根据客评id获取客评标准
     * @param gradeId
     * @return
     */
    public List<GradeItem> findListByGradeId(Long gradeId){
        MSResponse<List<MDGradeItem>> msResponse = msGradeItemFeign.findListByGradeId(gradeId);
        if(MSResponse.isSuccess(msResponse)){
            List<GradeItem> list = mapper.mapAsList(msResponse.getData(),GradeItem.class);
            if(list !=null && list.size()>0){
                return list;
            }else{
                return Lists.newArrayList();
            }
        }else{
            return Lists.newArrayList();
        }
    }
}
