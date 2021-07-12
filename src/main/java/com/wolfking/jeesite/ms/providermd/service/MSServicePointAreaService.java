package com.wolfking.jeesite.ms.providermd.service;

import com.google.common.collect.Lists;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.md.MDServicePointArea;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.ms.providermd.feign.MSServicePointAreaFeign;
import com.wolfking.jeesite.ms.providermd.utils.MDUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MSServicePointAreaService {
    @Autowired
    private MSServicePointAreaFeign msServicePointAreaFeign;

    /**
     * 查询网点负责的区域id清单
     *
     * @param servicePointId
     * @return
     */
    public List<Long> findAreaIds(Long servicePointId) {
        return MDUtils.findListUnnecessaryConvertType(()->msServicePointAreaFeign.findAreaIds(servicePointId));
    }

    /**
     *
     * @param servicePointIds
     * @return
     */
    public List<MDServicePointArea>findServicePointAreasByServicePointIds(List<Long> servicePointIds){
        //return MDUtils.findListByUserDefineCondition(()->msServicePointAreaFeign.findServicePointAreasByServicePointIds(servicePointIds));
        List<MDServicePointArea> mdServicePointAreaList = Lists.newArrayList();
        if (servicePointIds.size() >100) {
            List<MDServicePointArea> finalServicePointAreaList = Lists.newArrayList();
            Lists.partition(servicePointIds, 100).forEach(ids->{
                List<MDServicePointArea> partAreaList = MDUtils.findListUnnecessaryConvertType(()->msServicePointAreaFeign.findServicePointAreasByServicePointIds(servicePointIds));
                if (!org.springframework.util.ObjectUtils.isEmpty(partAreaList)) {
                    finalServicePointAreaList.addAll(partAreaList);
                }
            });
            if (!org.springframework.util.ObjectUtils.isEmpty(finalServicePointAreaList)) {
                mdServicePointAreaList.addAll(finalServicePointAreaList);
            }
        } else {
            mdServicePointAreaList = MDUtils.findListUnnecessaryConvertType(()->msServicePointAreaFeign.findServicePointAreasByServicePointIds(servicePointIds));
        }
        return mdServicePointAreaList;
    }

    /**
     * 移除网点下的所有区域
     *
     * @param servicePointId
     * @return
     */
    public void removeAreas(Long servicePointId) {
        MSErrorCode msErrorCode = MDUtils.customSave(()->msServicePointAreaFeign.removeAreas(servicePointId));
        if (msErrorCode.getCode() >0) {
            throw new RuntimeException("调用微服务移除网点下的所有区域出错.出错的原因:"+msErrorCode.getMsg());
        }
    }

    /**
     * 给网点分配区域
     *
     * @param servicePointId
     * @param areas
     * @return
     */
    public void assignAreas(Long servicePointId, List<Long> areas) {
        MSErrorCode msErrorCode = MDUtils.customSave(()->msServicePointAreaFeign.assignAreas(servicePointId, areas));
        if (msErrorCode.getCode() >0) {
            throw new RuntimeException("调用微服务给网点分配区域出错.出错的原因:"+msErrorCode.getMsg());
        }
    }

    /**
     * 获取所有网点的区域id
     * @return
     */
    public List<Long> findListWithAreaIds() {
        List<Long> areaIds = Lists.newArrayList();
        List<MDServicePointArea> mdServicePointAreaList = Lists.newArrayList();
        int pageNo = 1;
        Page<MDServicePointArea>  servicePointAreaPage = new Page<MDServicePointArea>();
        servicePointAreaPage.setPageNo(pageNo);
        servicePointAreaPage.setPageSize(500);
        Page<MDServicePointArea> returnPage = MDUtils.findMDEntityListForPage(servicePointAreaPage, new MDServicePointArea(), msServicePointAreaFeign::findListWithAreaIds);
        mdServicePointAreaList.addAll(returnPage.getList());
        while (pageNo < returnPage.getPageCount()) {
            pageNo++;
            servicePointAreaPage.setPageNo(pageNo);
            Page<MDServicePointArea> whileReturnPage = MDUtils.findMDEntityListForPage(servicePointAreaPage, new MDServicePointArea(), msServicePointAreaFeign::findListWithAreaIds);
            mdServicePointAreaList.addAll(whileReturnPage.getList());
        }
        if (!ObjectUtils.isEmpty(mdServicePointAreaList)) {
            areaIds = mdServicePointAreaList.stream().map(MDServicePointArea::getAreaId).collect(Collectors.toList());
        }
        return areaIds;
    }
}
