package com.wolfking.jeesite.ms.providermd.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.md.MDServicePointAutoPlan;
import com.kkl.kklplus.entity.md.MDServicePointStation;
import com.kkl.kklplus.entity.md.dto.MDServicePointAutoPlanDto;
import com.wolfking.jeesite.common.utils.GsonUtils;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providermd.feign.MSServicePointAutoPlanFeign;
import com.wolfking.jeesite.ms.providermd.utils.MDUtils;
import com.wolfking.jeesite.ms.providersys.service.MSSysAreaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MSServicePointAutoPlanService {
    @Autowired
    private MSServicePointAutoPlanFeign msServicePointAutoPlanFeign;

    @Autowired
    private MSSysAreaService msSysAreaService;

    /**
     * 根据网点id获取网点自动派单区域与品类
     * @param mdServicePointAutoPlan
     * @return
     */
    public List<MDServicePointAutoPlan> findList(MDServicePointAutoPlan mdServicePointAutoPlan) {
        List<MDServicePointAutoPlan> mdServicePointAutoPlanList = Lists.newArrayList();
        int pageNo = 1;
        int pageSize = 200;

        MSPage<MDServicePointAutoPlan> msPage = new MSPage<>();
        msPage.setPageNo(pageNo);
        msPage.setPageSize(pageSize);
        mdServicePointAutoPlan.setPage(msPage);

        MSResponse<MSPage<MDServicePointAutoPlan>> msPageMSResponse = msServicePointAutoPlanFeign.findList(mdServicePointAutoPlan);
        if (MSResponse.isSuccess(msPageMSResponse)) {
            MSPage<MDServicePointAutoPlan> servicePointAutoPlanMSPage = msPageMSResponse.getData();
            if (servicePointAutoPlanMSPage != null ) {
                List<MDServicePointAutoPlan> servicePointAutoPlanList = servicePointAutoPlanMSPage.getList();
                if (servicePointAutoPlanList != null && !servicePointAutoPlanList.isEmpty()) {
                    mdServicePointAutoPlanList.addAll(servicePointAutoPlanList);
                }

                while (pageNo < servicePointAutoPlanMSPage.getPageCount()) {
                    pageNo++;
                    msPage.setPageNo(pageNo);
                    MSResponse<MSPage<MDServicePointAutoPlan>> whileResponse = msServicePointAutoPlanFeign.findList(mdServicePointAutoPlan);

                    if (MSResponse.isSuccess(whileResponse)) {
                        MSPage<MDServicePointAutoPlan> whilePage = whileResponse.getData();
                        if (whilePage != null) {
                            List<MDServicePointAutoPlan> whileServicePointList = whilePage.getList();
                            if (whileServicePointList != null && !whileServicePointList.isEmpty()) {
                                mdServicePointAutoPlanList.addAll(whileServicePointList);
                            }
                        }
                    }
                }
            }
        }
        return mdServicePointAutoPlanList;
    }


    public void batchSave(MDServicePointAutoPlanDto mdServicePointAutoPlanDto) {
        User user = UserUtils.getUser();
        Date currentDate = new Date();
        List<Long> subAreaIds = Lists.newArrayList();
        List<Area> areaList = Lists.newArrayList();
        Map<Long, String> areaMap = Maps.newHashMap();

        List<MDServicePointStation> servicePointStationList = mdServicePointAutoPlanDto.getServicePointStationList();
        if (!ObjectUtils.isEmpty(servicePointStationList)) {
            subAreaIds = servicePointStationList.stream().map(r->r.getSubAreaId()).distinct().collect(Collectors.toList());
            areaList = msSysAreaService.findSpecListByIds(subAreaIds);
            areaMap = !ObjectUtils.isEmpty(areaList)?areaList.stream().collect(Collectors.toMap(r->r.getId(), r->r.getName())): Maps.newHashMap();

            Map<Long, String> finalAreaMap = areaMap;
            servicePointStationList.stream().forEach(r->{
                r.setRadius(0);
                r.setAutoPlanFlag(0);
                r.setName(finalAreaMap.get(r.getSubAreaId()));
                r.setCreateById(Optional.ofNullable(user.getId()).orElse(0L));
                r.setCreateDate(currentDate);
                r.setUpdateById(Optional.ofNullable(user.getId()).orElse(0L));
                r.setUpdateDate(currentDate);
            });
        }

        List<MDServicePointAutoPlan> servicePointAutoPlanList = mdServicePointAutoPlanDto.getServicePointAutoPlanList();
        if (!ObjectUtils.isEmpty(servicePointAutoPlanList)) {
            List<Long> areaIds = servicePointStationList.stream().map(r->r.getAreaId()).distinct().collect(Collectors.toList());
            List<Area> threeLevelAreaList = msSysAreaService.findThreeLevelAreaIdByIds(areaIds);
            Map<Long, Area> threeLevelAreaMap = !ObjectUtils.isEmpty(threeLevelAreaList)?threeLevelAreaList.stream().collect(Collectors.toMap(r->r.getId(), r->r)): Maps.newHashMap();

            Map<Long, String> finalAreaMap = areaMap;
            servicePointAutoPlanList.stream().forEach(r->{
                r.setName(finalAreaMap.get(r.getSubAreaId()));
                Area area = threeLevelAreaMap.get(r.getAreaId());
                if (area != null) {
                    r.setCityId(area.getParentId());
                    r.setProvinceId(area.getParent().getParentId());
                }
                r.setCreateById(Optional.ofNullable(user.getId()).orElse(0L));
                r.setCreateDate(currentDate);
                r.setUpdateById(Optional.ofNullable(user.getId()).orElse(0L));
                r.setUpdateDate(currentDate);
            });
        }
        //log.warn("{}", mdServicePointAutoPlanDto);
        MSResponse<Integer> msResponse = msServicePointAutoPlanFeign.saveServicePointAutoPlanDto(mdServicePointAutoPlanDto);
        if (!MSResponse.isSuccessCode(msResponse)){
            throw new RuntimeException("调用微服务保存自动派单区域失败,失败原因:"+msResponse.getMsg());
        }
        try {
            LogUtils.saveLog("基础资料-网点自动派单", "MSServicePointAutoPlanService.batchSave-servicePointId:"+mdServicePointAutoPlanDto.getServicePointStationList().get(0).getServicePointId(), GsonUtils.toGsonString(mdServicePointAutoPlanDto.getServicePointAutoPlanList()), null, UserUtils.getUser());
        } catch(Exception ex) {
        }
    }

    /**
     * 将所有自动派单服务区域的网点都同步到ES
     */
    public void pushAllServicePointStationMessageToES() {
        MSResponse<Integer> msResponse = msServicePointAutoPlanFeign.pushAllServicePointStationMessageToES();
        if (!MSResponse.isSuccess(msResponse)) {
            throw  new RuntimeException("调用微服务保存同步自动派单区域失败,失败原因:"+msResponse.getMsg());
        }
    }
}
