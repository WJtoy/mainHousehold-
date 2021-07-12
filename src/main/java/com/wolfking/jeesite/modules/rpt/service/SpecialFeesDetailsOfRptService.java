package com.wolfking.jeesite.modules.rpt.service;

import com.google.common.collect.Maps;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.md.service.ServicePointService;
import com.wolfking.jeesite.modules.rpt.dao.SpecialFeesDetailsOfRptDao;
import com.wolfking.jeesite.modules.rpt.entity.SpecialFeesDetailsOfRptEntity;
import com.wolfking.jeesite.ms.providermd.utils.MDUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SpecialFeesDetailsOfRptService {
    @Resource
    SpecialFeesDetailsOfRptDao specialFeesDetailsOfRptDao;

    @Autowired
    ServicePointService servicePointService;


    public List<SpecialFeesDetailsOfRptEntity> getSpecialOrderFee(Integer selectedYear, Integer selectedMonth, Integer selectedDay, Long provinceId, Integer chargeFlag, List<Long> productCategoryIds) {
        Date queryDate = DateUtils.getDate(selectedYear, selectedMonth, selectedDay);
        Date endDate = DateUtils.getEndOfDay(queryDate);
        Date startDate = DateUtils.getStartOfDay(queryDate);
        List<SpecialFeesDetailsOfRptEntity> list = new ArrayList<>();
        if (chargeFlag == 1) {
            list = specialFeesDetailsOfRptDao.getRemoteCostOfOrder(startDate, endDate, provinceId, productCategoryIds);
        } else if (chargeFlag == 2) {
            list = specialFeesDetailsOfRptDao.getOtherCostsOfTheOrder(startDate, endDate, provinceId, productCategoryIds);
        }
        // add on 2019-11-1 begin
        Map<Long, String> engineerMap = Maps.newHashMap();
        if (list != null && !list.isEmpty()) {
            List<Long> engineerIds = list.stream().map(SpecialFeesDetailsOfRptEntity::getEngineerId).distinct().collect(Collectors.toList());
            engineerMap = MDUtils.getEngineerNamesByIds(engineerIds);
        }
        // add on 2019-11-1 end
        for (SpecialFeesDetailsOfRptEntity entity : list) {
            ServicePoint servicePoint = servicePointService.getFromCache(entity.getServicepointId());
            //Engineer engineer = servicePointService.getEngineerFromCache(entity.getServicepointId(),entity.getEngineerId());  //mark on 2019-11-1
            if (servicePoint != null) {
                entity.setServicepointName(servicePoint.getName());
                entity.setServicePointNo(servicePoint.getServicePointNo());
                /*
                // mark on 2019-11-1
                if(engineer != null){
                    entity.setEngineerName(engineer.getName());
                }
                */
                entity.setEngineerName(engineerMap.get(entity.getEngineerId()));   //add on 2019-11-1
            }
        }
        return list;
    }

    public List<SpecialFeesDetailsOfRptEntity> getSpecialOrderFeeOfChargeDate(Integer selectedYear, Integer selectedMonth, Integer selectedDay,
                                                                              Long provinceId, Integer chargeFlag, List<Long> productCategoryIds) {
        Date queryDate = DateUtils.getDate(selectedYear, selectedMonth, selectedDay);
        Date endDate = DateUtils.getEndOfDay(queryDate);
        Date startDate = DateUtils.getStartOfDay(queryDate);
        List<SpecialFeesDetailsOfRptEntity> list = new ArrayList<>();
        if (chargeFlag == 1) {
            list = specialFeesDetailsOfRptDao.getRemoteCostOfOrderChargeDate(startDate, endDate, provinceId, productCategoryIds);
        } else if (chargeFlag == 2) {
            list = specialFeesDetailsOfRptDao.getOtherCostsOfTheOrderChargeDate(startDate, endDate, provinceId, productCategoryIds);
        }
        // add on 2019-11-1 begin
        Map<Long, String> engineerMap = Maps.newHashMap();
        if (list != null && !list.isEmpty()) {
            List<Long> engineerIds = list.stream().map(SpecialFeesDetailsOfRptEntity::getEngineerId).distinct().collect(Collectors.toList());
            engineerMap = MDUtils.getEngineerNamesByIds(engineerIds);
        }
        // add on 2019-11-1 end
        for (SpecialFeesDetailsOfRptEntity entity : list) {
            ServicePoint servicePoint = servicePointService.getFromCache(entity.getServicepointId());
            //Engineer engineer = servicePointService.getEngineerFromCache(entity.getServicepointId(),entity.getEngineerId());  //mark on 2019-11-1
            if (servicePoint != null) {
                entity.setServicepointName(servicePoint.getName());
                entity.setServicePointNo(servicePoint.getServicePointNo());
                /*
                // mark on 2019-11-1
                if(engineer != null){
                    entity.setEngineerName(engineer.getName());
                }
                */
                entity.setEngineerName(engineerMap.get(entity.getEngineerId()));   //add on 2019-11-1
            }
        }
        return list;
    }

}
