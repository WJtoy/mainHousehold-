package com.wolfking.jeesite.modules.customer.md.service;

import com.google.common.collect.Lists;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.CustomerTimeliness;
import com.wolfking.jeesite.modules.md.entity.TimelinessLevel;
import com.wolfking.jeesite.modules.md.entity.viewModel.AreaTimelinessModel;
import com.wolfking.jeesite.modules.md.entity.viewModel.TimelinessChargeModel;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.ms.providermd.service.MSCustomerTimelinessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CtCustomerTimelinessService {
    @Autowired
    private AreaService areaService;

    @Autowired
    private MSCustomerTimelinessService msCustomerTimelinessService;

    /**
     * 通过客户id获得区域 时效等级列表
     * @param customerId
     * @return
     */
    public List<AreaTimelinessModel> findListByCustomerId(Long customerId){
        List<Area> areaList = areaService.findListByType(Area.TYPE_VALUE_PROVINCE);
        List<AreaTimelinessModel> list = Lists.newArrayList();

        // add on 2019-7-31 begin
        CustomerTimeliness customerTimeliness = new CustomerTimeliness();
        customerTimeliness.setCustomer(new Customer(customerId));
        List<CustomerTimeliness> customerTimelinessList = msCustomerTimelinessService.findListByCustomerId(customerTimeliness);

        Map<Long,List<CustomerTimeliness>> customerTimelinesMap = customerTimelinessList != null && !customerTimelinessList.isEmpty()?customerTimelinessList
                .stream().collect(Collectors.groupingBy(r->r.getArea().getId(), Collectors.mapping(r->r,Collectors.toList()))):null;
        // add on 2019-7-31 end

        for (Area area:areaList) {
            //查找客户下的区域
            // List<TimelinessChargeModel> customerTimelinesses= dao.findListByCustomerId(customerId,area.getId()); // mark on 2019-7-31
            List<CustomerTimeliness> subCustomerTimeslinessList = null;
            if (customerTimelinesMap != null && customerTimelinesMap.containsKey(area.getId())) {
                subCustomerTimeslinessList = customerTimelinesMap.get(area.getId());
            }
            List<TimelinessChargeModel> customerTimelinesses = findListByCustomerIdAndAreaId(subCustomerTimeslinessList);  // add on 2019-8-5

            //如果该客户 该区域下有数据再缓存
            if(customerTimelinesses != null && customerTimelinesses.size() > 0){
                AreaTimelinessModel areaTimelinessModel=new AreaTimelinessModel();
                areaTimelinessModel.setArea(area);
                areaTimelinessModel.setList(customerTimelinesses);
                list.add(areaTimelinessModel);
            }
        }

        return list;
    }


    /**
     * 根据区域id从客户时效等级查询时效费用数据
     * @param customerTimelinessList
     * @return
     */
    public  List<TimelinessChargeModel>  findListByCustomerIdAndAreaId(List<CustomerTimeliness> customerTimelinessList) {
        List<TimelinessChargeModel> timelinessChargeModelList = Lists.newArrayList();

        if (customerTimelinessList != null && !customerTimelinessList.isEmpty()) {
            List<CustomerTimeliness> subCustomerTimesLinessList = customerTimelinessList.stream().sorted(Comparator.comparing(r->r.getTimelinessLevel().getId())).collect(Collectors.toList());
            if (subCustomerTimesLinessList != null && !subCustomerTimesLinessList.isEmpty()) {
                subCustomerTimesLinessList.stream().forEach(customerTimeliness -> {
                    TimelinessChargeModel timelinessChargeModel = new TimelinessChargeModel();
                    timelinessChargeModel.setTimelinessLevel(new TimelinessLevel(customerTimeliness.getTimelinessLevel().getId()));
                    timelinessChargeModel.setChargeIn(customerTimeliness.getChargeIn());
                    timelinessChargeModel.setChargeOut(customerTimeliness.getChargeOut());
                    timelinessChargeModelList.add(timelinessChargeModel);
                });
            }
        }
        return timelinessChargeModelList;
    }
}
