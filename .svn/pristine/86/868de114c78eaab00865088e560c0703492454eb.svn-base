package com.wolfking.jeesite.ms.providermd.service;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.md.MDPlanRadius;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.modules.md.entity.PlanRadius;
import com.wolfking.jeesite.ms.providermd.feign.MSPlanRadiusFeign;
import com.wolfking.jeesite.ms.providermd.utils.MDUtils;
import ma.glasnost.orika.MapperFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MSPlanRadiusService {

    @Autowired
    private MSPlanRadiusFeign msPlanRadiusFeign;

    @Autowired
    private MapperFacade mapper;

    /**
     * 根据id获取派单区域半径
     * @param id
     * @return
     */
    public PlanRadius getById(Long id) {
        return MDUtils.getById(id, PlanRadius.class, msPlanRadiusFeign::getById);
    }



    /**
     * 获取所有数据
     * @return
     */
    public List<PlanRadius> findAllList() {
        return MDUtils.findAllList(PlanRadius.class, msPlanRadiusFeign::findAllList);
    }

    /**
     * 获取分页数据
     * @param planRadiusPage
     * @param planRadius
     * @return
     */
    public Page<PlanRadius> findList(Page<PlanRadius> planRadiusPage, PlanRadius planRadius) {
        return MDUtils.findListForPage(planRadiusPage, planRadius, PlanRadius.class, MDPlanRadius.class, msPlanRadiusFeign::findList);
    }

    /**
     * 根据区域id获取数据
     * @param areaId
     * @return
     */
    public PlanRadius getByAreaId(Long areaId){
        MSResponse<MDPlanRadius> msResponse = msPlanRadiusFeign.getByAreaId(areaId);
        if(MSResponse.isSuccess(msResponse)){
            PlanRadius planRadius = mapper.map(msResponse.getData(),PlanRadius.class);
            return planRadius;
        }else{
            return null;
        }
    }


    /**
     * 添加/更新
     * @param planRadius
     * @param isNew
     * @return
     */
    public MSErrorCode save(PlanRadius planRadius, boolean isNew) {
        return MDUtils.genericSave(planRadius, MDPlanRadius.class, isNew, isNew?msPlanRadiusFeign::insert:msPlanRadiusFeign::update);
    }

    /**
     * 停用或者启用
     * @param planRadius
     * @return
     */
    public MSErrorCode enableOrDisable(PlanRadius planRadius) {
        return MDUtils.genericSave(planRadius, MDPlanRadius.class, false, msPlanRadiusFeign::enableOrDisable);
    }
}
