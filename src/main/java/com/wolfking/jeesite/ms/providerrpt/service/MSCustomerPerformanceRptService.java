package com.wolfking.jeesite.ms.providerrpt.service;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTSalesPerfomanceEntity;
import com.kkl.kklplus.entity.rpt.common.RPTReportEnum;
import com.kkl.kklplus.entity.rpt.common.RPTReportTypeEnum;
import com.kkl.kklplus.entity.rpt.search.RPTCustomerOrderPlanDailySearch;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.common.config.MicroServicesProperties;
import com.wolfking.jeesite.ms.providerrpt.feign.MSCustomerPerformanceRptFeign;
import com.wolfking.jeesite.ms.providerrpt.utils.RedisGsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Service
public class MSCustomerPerformanceRptService {

    @Autowired
    private MicroServicesProperties microServicesProperties;

    @Autowired
    private MSCustomerPerformanceRptFeign msCustomerPerformanceRptFeign;

    @Autowired
    private ReportExportTaskService reportExportTaskService;

    public List<RPTSalesPerfomanceEntity> getSalesPerformanceMonthPlanDailyList(Integer selectedYear, Integer selectedMonth, List<Long> productCategoryIds, Long salesId,Integer subFlag) {
        List<RPTSalesPerfomanceEntity> list = new ArrayList<>();
        RPTCustomerOrderPlanDailySearch search =   setSearchCondition(productCategoryIds,selectedYear,selectedMonth);
        search.setSalesId(salesId);
        search.setSubFlag(subFlag);
            if (microServicesProperties.getReport().getEnabled()) {
                MSResponse<List<RPTSalesPerfomanceEntity>> msResponse = msCustomerPerformanceRptFeign.getSalesPerformanceList(search);
                if (MSResponse.isSuccess(msResponse)) {
                    list = msResponse.getData();
                }
            }

        return list;
    }


    public List<RPTSalesPerfomanceEntity> getSalesManAchievementRptDataNew(Integer selectedYear, Integer selectedMonth, Long salesId, List<Long> productCategoryIds,Integer subFlag) {
        List<RPTSalesPerfomanceEntity> list = new ArrayList<>();
        RPTCustomerOrderPlanDailySearch search =   setSearchCondition(productCategoryIds,selectedYear,selectedMonth);
        search.setSalesId(salesId);
        search.setSubFlag(subFlag);
        if (microServicesProperties.getReport().getEnabled()) {
            MSResponse<List<RPTSalesPerfomanceEntity>> msResponse = msCustomerPerformanceRptFeign.getCustomerPerformanceList(search);
            if (MSResponse.isSuccess(msResponse)) {
                list = msResponse.getData();
            }
        }

        return list;
    }

    public RPTCustomerOrderPlanDailySearch setSearchCondition(List<Long> productCategoryIds, Integer selectedYear, Integer selectedMonth){

        RPTCustomerOrderPlanDailySearch rptSearchCondtion = new RPTCustomerOrderPlanDailySearch();
        Date queryDate = DateUtils.getDate(selectedYear, selectedMonth, 1);
        Date beginDate = DateUtils.getStartOfDay(queryDate);
        rptSearchCondtion.setStartDate(beginDate.getTime());
        rptSearchCondtion.setProductCategoryIds(productCategoryIds);
        return rptSearchCondtion;
    }

    /**
     * 检查报表导出
     */
    public void checkRptExportTask(Integer selectedYear,  Integer selectedMonth, List<Long> productCategoryIds, User user) {
        Long salesId ;
        RPTCustomerOrderPlanDailySearch searchCondition = setSearchCondition(productCategoryIds,selectedYear,selectedMonth);
        if (user.isSaleman()) {
            salesId = user.getId();
            searchCondition.setSalesId(salesId);

        }
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        reportExportTaskService.checkRptExportTask(RPTReportEnum.SALES_PERFORMANCE_RPT, user, searchConditionJson);
    }

    /**
     * 创建报表导出任务
     */
    public void createRptExportTask(Integer selectedYear,  Integer selectedMonth, List<Long> productCategoryIds, User user) {
        Long salesId ;
        RPTCustomerOrderPlanDailySearch searchCondition = setSearchCondition(productCategoryIds,selectedYear,selectedMonth);
        String reportTitle =  "业务业绩排名表（" + selectedYear + "年" + selectedMonth + "月）";
        if (user.isSaleman()) {
            salesId = user.getId();
            searchCondition.setSalesId(salesId);
        }
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        reportExportTaskService.createRptExportTask(RPTReportEnum.SALES_PERFORMANCE_RPT, RPTReportTypeEnum.ORDER_REPORT, user, reportTitle, searchConditionJson);
    }

    /**
     * 检查报表导出
     */
    public void customerCheckRptExportTask(Integer selectedYear,  Integer selectedMonth, Long salesId, List<Long> productCategoryIds, User user,Integer subFlag) {
        RPTCustomerOrderPlanDailySearch searchCondition = setSearchCondition(productCategoryIds,selectedYear,selectedMonth);
        searchCondition.setSalesId(salesId);
        searchCondition.setSubFlag(subFlag);
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        reportExportTaskService.checkRptExportTask(RPTReportEnum.SALES_CUSTOMER_PERFORMANCE_RPT, user, searchConditionJson);
    }

    /**
     * 创建报表导出任务
     */
    public void customerCreateRptExportTask(Integer selectedYear,  Integer selectedMonth, Long salesId, List<Long> productCategoryIds, User user,Integer subFlag) {
        RPTCustomerOrderPlanDailySearch searchCondition = setSearchCondition(productCategoryIds,selectedYear,selectedMonth);
        searchCondition.setSalesId(salesId);
        searchCondition.setSubFlag(subFlag);
        String reportTitle =  "业务员业绩明细表（" +selectedYear + "年" + selectedMonth + "月）";
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        reportExportTaskService.createRptExportTask(RPTReportEnum.SALES_CUSTOMER_PERFORMANCE_RPT, RPTReportTypeEnum.ORDER_REPORT, user, reportTitle, searchConditionJson);
    }



}
