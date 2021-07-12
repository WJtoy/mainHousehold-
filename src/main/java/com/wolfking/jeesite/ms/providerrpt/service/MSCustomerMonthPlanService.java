package com.wolfking.jeesite.ms.providerrpt.service;


import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RptCustomerMonthOrderEntity;
import com.kkl.kklplus.entity.rpt.common.RPTReportEnum;
import com.kkl.kklplus.entity.rpt.common.RPTReportTypeEnum;
import com.kkl.kklplus.entity.rpt.search.RPTCustomerOrderPlanDailySearch;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.common.config.MicroServicesProperties;
import com.wolfking.jeesite.ms.providerrpt.controller.MSCustomerMonthPlanRptController;
import com.wolfking.jeesite.ms.providerrpt.feign.MSCustomerMonthDailyRptFeign;
import com.wolfking.jeesite.ms.providerrpt.utils.RedisGsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Service
public class MSCustomerMonthPlanService {

    @Autowired
    MSCustomerMonthPlanRptController msCustomerMonthPlanRptController;

    @Autowired
    private MicroServicesProperties microServicesProperties;

    @Autowired
    private MSCustomerMonthDailyRptFeign msCustomerMonthDailyRptFeign;

    @Autowired
    private ReportExportTaskService reportExportTaskService;


    public List<RptCustomerMonthOrderEntity> getCustomerMonthPlanDailyList(Integer selectedYear, Long salesId, Long customerId, List<Long> productCategoryIds,Integer subFlag) {
        List<RptCustomerMonthOrderEntity> list = new ArrayList<>();
        if (selectedYear != null && selectedYear > 0) {
            RPTCustomerOrderPlanDailySearch search = new RPTCustomerOrderPlanDailySearch();
            Date queryDate = DateUtils.getDate(selectedYear, 1, 1);
            Date startDate = DateUtils.getStartDayOfMonth(queryDate);
            search.setStartDate(startDate.getTime());
            if(salesId !=null && salesId !=0){
                search.setSalesId(salesId);
            }
            if (customerId != null && customerId != 0) {
                search.setCustomerId(customerId);
            }
            search.setSubFlag(subFlag);
            search.setProductCategoryIds(productCategoryIds);
            if (microServicesProperties.getReport().getEnabled()) {
                MSResponse<List<RptCustomerMonthOrderEntity>> msResponse = msCustomerMonthDailyRptFeign.getCustomerMonthPlanDailyList(search);
                if (MSResponse.isSuccess(msResponse)) {
                    list = msResponse.getData();
                }
            }
        }
        return list;
    }

    /**
     * 从rpt微服务中获取客户每月催单图表数据
     */
    public Map<String, Object> getCustomerOrderPlanDailyChartList(Integer selectedYear, Long salesId, Long customerId, List<Long> productCategoryIds,Integer subFlag) {
        Map<String, Object> map = new HashMap<>();
        if (selectedYear != null && selectedYear > 0 ) {
            RPTCustomerOrderPlanDailySearch search = new RPTCustomerOrderPlanDailySearch();
            Date queryDate = DateUtils.getDate(selectedYear, 1, 1);
            Date startDate = DateUtils.getStartDayOfMonth(queryDate);
            search.setStartDate(startDate.getTime());
            if(salesId !=null && salesId !=0){
                search.setSalesId(salesId);
            }
            if (customerId != null && customerId != 0) {
                search.setCustomerId(customerId);
            }
            search.setSubFlag(subFlag);
            search.setProductCategoryIds(productCategoryIds);
            if (microServicesProperties.getReport().getEnabled()) {
                MSResponse<Map<String, Object>> msResponse = msCustomerMonthDailyRptFeign.getCustomerMonthPlanChartList(search);
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
    public void checkRptExportTask(Integer selectedYear, Long salesId, Long customerId, List<Long> productCategoryIds, User user,Integer subFlag) {

        RPTCustomerOrderPlanDailySearch searchCondition = setSearchCondition(selectedYear , salesId, customerId, productCategoryIds,subFlag);
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        reportExportTaskService.checkRptExportTask(RPTReportEnum.CUSTOMER_MONTH_ORDER_RPT, user, searchConditionJson);
    }

    /**
     * 创建报表导出任务
     */
    public void createRptExportTask(Integer selectedYear, Long salesId, Long customerId, List<Long> productCategoryIds, User user,Integer subFlag) {

        RPTCustomerOrderPlanDailySearch searchCondition = setSearchCondition(selectedYear, salesId, customerId, productCategoryIds,subFlag);
        String reportTitle =  "客户每月下单报表" + selectedYear + "年";
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        reportExportTaskService.createRptExportTask(RPTReportEnum.CUSTOMER_MONTH_ORDER_RPT, RPTReportTypeEnum.ORDER_REPORT, user, reportTitle, searchConditionJson);
    }


    /**
     * 设置筛选项的值
     */
    public RPTCustomerOrderPlanDailySearch setSearchCondition(Integer selectedYear , Long salesId, Long customerId, List<Long> productCategoryIds,Integer subFlag) {

        RPTCustomerOrderPlanDailySearch searchCondition = new RPTCustomerOrderPlanDailySearch();

        Date queryDate = DateUtils.getDate(selectedYear, 1, 1);
        Date startDate = DateUtils.getStartDayOfMonth(queryDate);
        searchCondition.setStartDate(startDate.getTime());
        if(salesId !=null && salesId !=0){
            searchCondition.setSalesId(salesId);
        }
        if (customerId != null && customerId != 0) {
            searchCondition.setCustomerId(customerId);
        }
        searchCondition.setSubFlag(subFlag);
        searchCondition.setProductCategoryIds(productCategoryIds);
        return searchCondition;
    }
}
