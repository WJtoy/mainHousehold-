package com.wolfking.jeesite.ms.providermd.service;

import com.google.common.collect.Lists;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.md.MDAreaTimeLiness;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.modules.md.entity.AreaTimeLiness;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.ms.providermd.feign.MSAreaTimeLinessFeign;
import com.wolfking.jeesite.ms.providermd.utils.MDUtils;
import ma.glasnost.orika.MapperFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Optional;

@Service
public class MSAreaTimeLinessService {
    @Autowired
    private MSAreaTimeLinessFeign msAreaTimeLinessFeign;

    @Autowired
    private MapperFacade mapper;

    /**
     * 通过多个areaId获取区域时效列表-->基础资料
     *
     * @param areaIdList
     */
    public List<AreaTimeLiness> findListByAreaIdsForMD(List<Long> areaIdList) {
        List<AreaTimeLiness> areaTimeLinessList = Lists.newArrayList();
        if (!ObjectUtils.isEmpty(areaIdList)) {
            Lists.partition(areaIdList, 100).forEach(partAreaIdList->{
                List<AreaTimeLiness> partAreaTimeLinessList = MDUtils.findListNecessaryConvertType(AreaTimeLiness.class, ()->msAreaTimeLinessFeign.findListByAreaIdsForMD(partAreaIdList));
                if (!ObjectUtils.isEmpty(partAreaTimeLinessList)) {
                    areaTimeLinessList.addAll(partAreaTimeLinessList);
                }
            });
        }
        return areaTimeLinessList;
    }

    /**
     * 通过多个areaId分页获取有效品类的时效-->基础资料
     *
     * @param areaIdList
     */
    public List<AreaTimeLiness>  findListByAreaIdsAndProductCategoryForMD(List<Long> areaIdList) {
        int pageNo = 1;
        int pageSize = 500;
        List<AreaTimeLiness> areaTimeLinessList = Lists.newArrayList();
        MSResponse<MSPage<MDAreaTimeLiness>> msResponse = msAreaTimeLinessFeign.findListByAreaIdsAndProductCategoryForMD(areaIdList, pageNo, pageSize);

        if (MSResponse.isSuccess(msResponse)) {
            MSPage<MDAreaTimeLiness> msPage = msResponse.getData();
            List<AreaTimeLiness> tmpAreaTimeLinessList = mapper.mapAsList(msPage.getList(), AreaTimeLiness.class);
            if (tmpAreaTimeLinessList != null && !tmpAreaTimeLinessList.isEmpty()) {
                areaTimeLinessList.addAll(tmpAreaTimeLinessList);
            }

            while (pageNo < msPage.getPageCount()) {
                pageNo++;
                MSResponse<MSPage<MDAreaTimeLiness>> returnResponse = msAreaTimeLinessFeign.findListByAreaIdsAndProductCategoryForMD(areaIdList, pageNo, pageSize);
                if (MSResponse.isSuccess(returnResponse)) {
                    MSPage<MDAreaTimeLiness> returnPage = returnResponse.getData();
                    tmpAreaTimeLinessList = mapper.mapAsList(returnPage.getList(), AreaTimeLiness.class);
                    if (tmpAreaTimeLinessList != null && !tmpAreaTimeLinessList.isEmpty()) {
                        areaTimeLinessList.addAll(tmpAreaTimeLinessList);
                    }
                }
            }
        }
        return areaTimeLinessList;
    }

    /**
     * 批量操作-->基础资料
     *
     * @param areaTimeLinessList
     * @return
    */
    public void batchSaveForMD(List<AreaTimeLiness> areaTimeLinessList) {
        MSErrorCode msErrorCode = MDUtils.genericBatchSave(areaTimeLinessList, MDAreaTimeLiness.class, msAreaTimeLinessFeign::batchSaveForMD);
        if (msErrorCode.getCode() != 0) {
            throw new RuntimeException("批量保存数据失败.");
        }
    }

    /**
     * 根据区域id获取isOpen标识-->工单
     *
     * @param areaId
    */
    public Integer getIsOpenByAreaIdFromCacheForSD(Long areaId) {
        return MDUtils.getObjUnnecessaryConvertType(()->msAreaTimeLinessFeign.getIsOpenByAreaIdFromCacheForSD(areaId));
    }

    public AreaTimeLiness getFromCacheForSD(Long areaId) {
        Integer isOpen = getIsOpenByAreaIdFromCacheForSD(areaId);
        AreaTimeLiness areaTimeLiness = new AreaTimeLiness();
        areaTimeLiness.setArea(new Area(areaId));
        areaTimeLiness.setIsOpen(Optional.ofNullable(isOpen).orElse(0));
        return areaTimeLiness;
    }

    /**
     * 根据区域id获取isOpen标识-->工单
     *
     * @param areaId
     */
    public Integer getIsOpenByAreaIdAndProductCategoryIdFromCacheForSD(Long areaId, Long productCategoryId) {
        return MDUtils.getObjUnnecessaryConvertType(()->msAreaTimeLinessFeign.getIsOpenByAreaIdAndCategoryFromCacheForSD(areaId, productCategoryId));
    }

    /**
     *
     * @param areaId
     * @param productCategoryId
     * @return
     */
    public AreaTimeLiness getFromCacheForSD(Long areaId, Long productCategoryId) {
        Integer isOpen = getIsOpenByAreaIdAndProductCategoryIdFromCacheForSD(areaId, productCategoryId);
        AreaTimeLiness areaTimeLiness = new AreaTimeLiness();
        areaTimeLiness.setArea(new Area(areaId));
        areaTimeLiness.setProductCategoryId(productCategoryId);
        areaTimeLiness.setIsOpen(Optional.ofNullable(isOpen).orElse(0));
        return areaTimeLiness;
    }
}
