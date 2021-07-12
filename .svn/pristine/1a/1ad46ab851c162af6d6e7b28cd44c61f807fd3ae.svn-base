package com.wolfking.jeesite.modules.customer.md.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.UrgentCustomer;
import com.wolfking.jeesite.modules.md.entity.UrgentLevel;
import com.wolfking.jeesite.modules.md.entity.viewModel.AreaUrgentModel;
import com.wolfking.jeesite.modules.md.entity.viewModel.UrgentChargeModel;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.ms.providermd.service.MSCustomerUrgentService;
import com.wolfking.jeesite.ms.providermd.service.MSUrgentLevelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CtUrgentCustomerService {

    @Autowired
    private AreaService areaService;

    @Autowired
    private MSCustomerUrgentService msCustomerUrgentService;

    @Autowired
    private MSUrgentLevelService msUrgentLevelService;


    /**
     * 客户id获得列表
     * @param customerId
     * @return
     */
    public List<AreaUrgentModel> findListByCustomerId(Long customerId){
        Map<Long, Area> areaMaps = areaService.findMapByType(Area.TYPE_VALUE_PROVINCE);
        List<AreaUrgentModel> list = Lists.newArrayList();

        // begin
        List<UrgentLevel> urgentLevelList = msUrgentLevelService.findAllList();
        Map<Long, UrgentLevel> urgentLevels;
        if(urgentLevelList == null || urgentLevelList.size()== 0){
            urgentLevels = Maps.newHashMap();
        } else {
            urgentLevels = urgentLevelList.stream().collect(Collectors.toMap(
                    e -> e.getId(),
                    e -> e
            ));
        }
        // end

        //查找客户下的区域
        List<UrgentChargeModel> urgentChargeModels = findListByCustomerIdFromMS(customerId);     // add on 2019-8-1 调用微服务获取数据

        if (urgentChargeModels != null && urgentChargeModels.size() > 0) {
            Map<Long, List<UrgentChargeModel>> groups = urgentChargeModels.stream()
                    .collect(Collectors.groupingBy(
                            p -> p.getAreaId(),
                            Collectors.mapping(p -> p,
                                    Collectors.toList()
                            )
                    ));
            Set<Map.Entry<Long, List<UrgentChargeModel>>> entrySet = groups.entrySet();
            Iterator<Map.Entry<Long, List<UrgentChargeModel>>> iter = entrySet.iterator();
            Map.Entry<Long, List<UrgentChargeModel>> entry;
            AreaUrgentModel areaUrgentModel;
            List<UrgentChargeModel> items;
            while (iter.hasNext()) {
                entry = iter.next();
                if (areaMaps.containsKey(entry.getKey())) {
                    areaUrgentModel = new AreaUrgentModel();
                    areaUrgentModel.setArea(areaMaps.get(entry.getKey()));
                    items = entry.getValue();
                    items.stream().forEach(t -> {
                        t.setUrgentLevel(urgentLevels.get(t.getUrgentLevel().getId()));
                    });
                    areaUrgentModel.setList(items);
                    list.add(areaUrgentModel);
                }
            }
        }

        return list;
    }

    /**
     * 根据customerId从微服务中获取加急费用等级列表  // add on 2019-8-1
     * @param customerId
     * @return
     */
    private List<UrgentChargeModel>  findListByCustomerIdFromMS(Long customerId) {
        List<UrgentChargeModel> urgentChargeModelList = Lists.newArrayList();

        UrgentCustomer urgentCustomer = new UrgentCustomer();
        urgentCustomer.setCustomer(new Customer(customerId));
        List<UrgentCustomer> urgentCustomerList = msCustomerUrgentService.findListByCustomerId(urgentCustomer);
        if (urgentCustomerList != null && !urgentCustomerList.isEmpty()) {
            urgentCustomerList.stream().sorted(Comparator.comparing(urgentCustomerEntity -> urgentCustomerEntity.getUrgentLevel().getId())).forEach(urgentCustomerEntity -> {
                UrgentChargeModel urgentChargeModelEntity = new UrgentChargeModel();
                urgentChargeModelEntity.setUrgentLevel(urgentCustomerEntity.getUrgentLevel());
                urgentChargeModelEntity.setAreaId(urgentCustomerEntity.getArea().getId());
                urgentChargeModelEntity.setChargeIn(urgentCustomerEntity.getChargeIn());
                urgentChargeModelEntity.setChargeOut(urgentCustomerEntity.getChargeOut());

                urgentChargeModelList.add(urgentChargeModelEntity);
            });
        }
        return urgentChargeModelList;
    }
}
