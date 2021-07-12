package com.wolfking.jeesite.ms.providerrpt.service;

import com.google.common.collect.Maps;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTAreaOrderPlanDailyEntity;
import com.kkl.kklplus.entity.rpt.common.RPTReportEnum;
import com.kkl.kklplus.entity.rpt.common.RPTReportTypeEnum;
import com.kkl.kklplus.entity.rpt.search.RPTAreaOrderPlanDailySearch;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.utils.CustomerUtils;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.common.config.MicroServicesProperties;
import com.wolfking.jeesite.ms.providerrpt.feign.MSAreaOrderPlanDailyRptFeign;
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
public class MSAreaOrderPlanDailyRptService {
    @Autowired
    private MSAreaOrderPlanDailyRptFeign msAreaOrderPlanDailyRptFeign;

    @Autowired
    private MicroServicesProperties microServicesProperties;

    @Autowired
    private ReportExportTaskService reportExportTaskService;
    /**
     * 从rpt微服务中获取省市区每日下单明细数据
     */
    public Map<String,List<RPTAreaOrderPlanDailyEntity>> getAreaOrderPlanDailyList(Integer selectedYear, Integer selectedMonth, Integer areaType, Long areaId, Long customerId, List<Long> productCategoryIds,Integer dataSource) {
        Map<String, List<RPTAreaOrderPlanDailyEntity>> map = Maps.newHashMap();
        if ( selectedYear != null && selectedYear > 0 && selectedMonth != null && selectedMonth > 0) {
            RPTAreaOrderPlanDailySearch search = new RPTAreaOrderPlanDailySearch();
            Date queryDate = DateUtils.getDate(selectedYear, selectedMonth, 1);
            Date startDate = DateUtils.getStartDayOfMonth(queryDate);
            Date endDate = DateUtils.getLastDayOfMonth(queryDate);
            String quarter = QuarterUtils.getSeasonQuarter(queryDate);
            search.setStartDate(startDate.getTime());
            search.setEndDate(endDate.getTime());
            search.setQuarter(quarter);
            search.setDataSource(dataSource);
            if(areaId != null && areaId != 0){
                search.setAreaId(areaId);
            }
            if(customerId != null && customerId !=0){
                search.setCustomerId(customerId);
            }
            search.setAreaType(areaType);
            search.setProductCategoryIds(productCategoryIds);
            if (microServicesProperties.getReport().getEnabled()) {
                MSResponse<Map<String,List<RPTAreaOrderPlanDailyEntity>>> msResponse = msAreaOrderPlanDailyRptFeign.getAreaOrderPlanDailyList(search);
                if (MSResponse.isSuccess(msResponse)) {
                    map = msResponse.getData();
                }
            }
        }
        return map;
    }


    /**
     * 检查报表导出
     */
    public void checkRptExportTask(Integer selectedYear, Integer selectedMonth, Integer areaType, Long areaId, Long customerId, List<Long> productCategoryIds,Integer dataSource,User user) {

        RPTAreaOrderPlanDailySearch searchCondition = setSearchCondition(selectedYear,selectedMonth,areaType,areaId,customerId,productCategoryIds,dataSource);
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        reportExportTaskService.checkRptExportTask(RPTReportEnum.AREA_PLAN_ORDER_RPT, user, searchConditionJson);
    }

    /**
     *创建报表导出任务
     */
    public void createRptExportTask(Integer selectedYear, Integer selectedMonth, Integer areaType, Long areaId, Long customerId, List<Long> productCategoryIds,Integer dataSource,User user) {

        RPTAreaOrderPlanDailySearch searchCondition = setSearchCondition(selectedYear,selectedMonth,areaType,areaId,customerId,productCategoryIds,dataSource);
        Customer customer = CustomerUtils.getCustomer(customerId);
        String customerName = customer == null ? "" : customer.getName();
        String reportTitle = customerName + "省市区日下单报表" + selectedYear + "年" + selectedMonth + "月";
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        reportExportTaskService.createRptExportTask(RPTReportEnum.AREA_PLAN_ORDER_RPT, RPTReportTypeEnum.ORDER_REPORT, user, reportTitle, searchConditionJson);
    }


    /**
     *设置筛选项的值
     */
    public RPTAreaOrderPlanDailySearch setSearchCondition(Integer selectedYear, Integer selectedMonth, Integer areaType, Long areaId, Long customerId, List<Long> productCategoryIds,Integer dataSource){

        RPTAreaOrderPlanDailySearch searchCondition = new RPTAreaOrderPlanDailySearch();

        Date queryDate = DateUtils.getDate(selectedYear, selectedMonth, 1);
        Date startDate = DateUtils.getStartDayOfMonth(queryDate);
        Date endDate = DateUtils.getLastDayOfMonth(queryDate);
        String quarter = QuarterUtils.getSeasonQuarter(queryDate);
        searchCondition.setStartDate(startDate.getTime());
        searchCondition.setEndDate(endDate.getTime());
        searchCondition.setQuarter(quarter);
        searchCondition.setDataSource(dataSource);
        if(areaId != null && areaId != 0){
            searchCondition.setAreaId(areaId);
        }
        if(customerId != null && customerId !=0){
            searchCondition.setCustomerId(customerId);
        }
        searchCondition.setAreaType(areaType);
        searchCondition.setProductCategoryIds(productCategoryIds);
        return searchCondition;
    }
}
