package com.wolfking.jeesite.ms.providerrpt.service;

import com.google.common.collect.Lists;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTAreaCompletedDailyEntity;
import com.kkl.kklplus.entity.rpt.RPTKefuCompletedDailyEntity;
import com.kkl.kklplus.entity.rpt.common.RPTReportEnum;
import com.kkl.kklplus.entity.rpt.common.RPTReportTypeEnum;
import com.kkl.kklplus.entity.rpt.search.RPTGradedOrderSearch;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.utils.CustomerUtils;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.AreaUtils;
import com.wolfking.jeesite.ms.common.config.MicroServicesProperties;
import com.wolfking.jeesite.ms.providermd.service.MSCustomerService;
import com.wolfking.jeesite.ms.providerrpt.feign.MSComplainRatioDailyRptFeign;
import com.wolfking.jeesite.ms.providerrpt.utils.RedisGsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Service
public class MSComplainRatioDailyRptService {

    @Autowired
    private MicroServicesProperties microServicesProperties;

    @Autowired
    private MSComplainRatioDailyRptFeign msComplainRatioDailyRptFeign;

    @Autowired
    private ReportExportTaskService reportExportTaskService;

    @Autowired
    private MSCustomerService msCustomerService;

    /**
     *获取省完工单和投诉数据
     */
    public List<RPTAreaCompletedDailyEntity> getProvinceComplainCompletedRptData(Integer areaType, Long areaId, Integer selectYear, Integer selectMonth, Long customerId, List<Long> productCategoryIds){
        RPTGradedOrderSearch rptGradedOrderSearchCondition = setSearchCondition(areaType, areaId, selectYear, selectMonth, customerId, productCategoryIds);
        MSResponse<List<RPTAreaCompletedDailyEntity>> provinceCompletedOrderRpt = msComplainRatioDailyRptFeign.getProvinceComplainCompletedOrderRpt(rptGradedOrderSearchCondition);
        List<RPTAreaCompletedDailyEntity> list = Lists.newArrayList();
        if (microServicesProperties.getReport().getEnabled()) {
            if (MSResponse.isSuccess(provinceCompletedOrderRpt)) {
                list =  provinceCompletedOrderRpt.getData();
            }
        }
        return list;
    }


    /**
     *获取市完工单和投诉数据
     */
    public List<RPTAreaCompletedDailyEntity> getCityComplainCompletedRptData(Integer areaType, Long areaId, Integer selectYear, Integer selectMonth, Long customerId, List<Long> productCategoryIds){
        RPTGradedOrderSearch rptGradedOrderSearchCondition = setSearchCondition(areaType, areaId, selectYear, selectMonth, customerId, productCategoryIds);
        MSResponse<List<RPTAreaCompletedDailyEntity>> cityCompletedOrderRpt = msComplainRatioDailyRptFeign.getCityComplainCompletedOrderRpt(rptGradedOrderSearchCondition);
        List<RPTAreaCompletedDailyEntity> list = Lists.newArrayList();
        if (microServicesProperties.getReport().getEnabled()) {
            if (MSResponse.isSuccess(cityCompletedOrderRpt)) {
                list =  cityCompletedOrderRpt.getData();
            }
        }
        return list;
    }


    /**
     *获取区域完工单和投诉数据
     */
    public List<RPTAreaCompletedDailyEntity> getAreaComplainCompletedRptData(Integer areaType, Long areaId, Integer selectYear, Integer selectMonth, Long customerId, List<Long> productCategoryIds){
        RPTGradedOrderSearch rptGradedOrderSearchCondition = setSearchCondition(areaType, areaId, selectYear, selectMonth, customerId, productCategoryIds);
        MSResponse<List<RPTAreaCompletedDailyEntity>> areaCompletedOrderRpt = msComplainRatioDailyRptFeign.getAreaComplainCompletedOrderRpt(rptGradedOrderSearchCondition);
        List<RPTAreaCompletedDailyEntity> list = Lists.newArrayList();
        if (microServicesProperties.getReport().getEnabled()) {
            if (MSResponse.isSuccess(areaCompletedOrderRpt)) {
                list =  areaCompletedOrderRpt.getData();
            }
        }
        return list;
    }


    //设置查询条件
    public RPTGradedOrderSearch setSearchCondition(Integer areaType, Long areaId, Integer selectYear, Integer selectMonth, Long customerId, List<Long> productCategoryIds){
        RPTGradedOrderSearch rptGradedOrderSearchCondition = new RPTGradedOrderSearch();
        Date queryDate = DateUtils.getDate(selectYear, selectMonth, 1);
        String quarter = DateUtils.getQuarter(queryDate);
        Date beginDate = DateUtils.getStartDayOfMonth(queryDate);
        Date endDate = DateUtils.getLastDayOfMonth(queryDate);
        rptGradedOrderSearchCondition.setCustomerId(customerId);
        rptGradedOrderSearchCondition.setAreaType(areaType);
        rptGradedOrderSearchCondition.setAreaId(areaId);
        rptGradedOrderSearchCondition.setBeginDate(beginDate.getTime());
        rptGradedOrderSearchCondition.setEndDate(endDate.getTime());
        rptGradedOrderSearchCondition.setQuarter(quarter);
        rptGradedOrderSearchCondition.setProductCategoryIds(productCategoryIds);
        return rptGradedOrderSearchCondition;
    }

    public void checkAreaCompletedRptExportTask(Long areaId, Integer areaType, Integer selectYear, Integer selectMonth, Long customerId, List<Long> productCategoryIds, User user) {
        RPTGradedOrderSearch rptGradedOrderSearchCondition = setSearchCondition(areaType, areaId, selectYear, selectMonth, customerId, productCategoryIds);
        String searchConditionJson = RedisGsonUtils.toJson(rptGradedOrderSearchCondition);
        reportExportTaskService.checkRptExportTask(RPTReportEnum.PROVINCE_ORDER_COMPLAIN_RPT, user, searchConditionJson);
    }

    public void createAreaCompletedRptExportTask(Long areaId,Integer areaType , Integer selectYear, Integer selectMonth ,Long customerId,List<Long> productCategoryIds,Integer flag,User user)  {
        RPTGradedOrderSearch rptGradedOrderSearchCondition = setSearchCondition(areaType, areaId, selectYear, selectMonth, customerId, productCategoryIds);
        Map<Long, Area> areaMap = AreaUtils.getAreaMap(Area.TYPE_VALUE_COUNTY);
        Map<Long, Area> cityMap = AreaUtils.getAreaMap(Area.TYPE_VALUE_CITY);
        Map<Long, Area> provinceMap = AreaUtils.getAreaMap(Area.TYPE_VALUE_PROVINCE);
        CustomerUtils.getAllCustomerMap();
        String areaName = "";
        if (flag==1){
            areaName = "省";
            Area province = provinceMap.get(areaId);
            if (province!=null){
                areaName = province.getFullName();
            }
        }else if (flag==2){
            areaName = "市";
            Area city = cityMap.get(areaId);
            if (city!=null){
                areaName = city.getFullName();
            }
        }else if (flag==3){
            areaName = "区";
            Area area = areaMap.get(areaId);
            if (area!=null){
                areaName = area.getFullName();
            }
        }
        Customer customer = msCustomerService.getByIdToCustomer(customerId);
        String customerName = "";
        if (customer != null) {
            customerName = customer.getName();
        }
        String reportTitle =   selectYear +"年"+selectMonth +"月" +areaName + customerName + "投诉率报表";
        String searchConditionJson = RedisGsonUtils.toJson(rptGradedOrderSearchCondition);
        reportExportTaskService.createRptExportTask(flag==1?RPTReportEnum.PROVINCE_ORDER_COMPLAIN_RPT:flag==2?RPTReportEnum.CITY_ORDER_COMPLAIN_RPT:RPTReportEnum.COUNTY_ORDER_COMPLAIN_RPT, RPTReportTypeEnum.ORDER_REPORT, user, reportTitle, searchConditionJson);
    }
}
