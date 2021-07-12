package com.wolfking.jeesite.ms.providerrpt.service;


import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTCustomerRechargeSummaryEntity;
import com.kkl.kklplus.entity.rpt.RptCustomerMonthOrderEntity;
import com.kkl.kklplus.entity.rpt.common.RPTReportEnum;
import com.kkl.kklplus.entity.rpt.common.RPTReportTypeEnum;
import com.kkl.kklplus.entity.rpt.search.RPTCustomerOrderPlanDailySearch;
import com.kkl.kklplus.entity.rpt.search.RPTServicePointInvoiceSearch;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.common.config.MicroServicesProperties;
import com.wolfking.jeesite.ms.providerrpt.feign.MSCustomerRechargeSummaryRptFeign;
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
public class MSCustomerRechargeSummaryRptService {


    @Autowired
    private MicroServicesProperties microServicesProperties;

    @Autowired
    private ReportExportTaskService reportExportTaskService;

    @Autowired
    private MSCustomerRechargeSummaryRptFeign msCustomerRechargeSummaryRptFeign;


    public List<RPTCustomerRechargeSummaryEntity>  getCustomerRechargeSummary (Long customerId, Integer selectedYear, Integer selectedMonth){
        List<RPTCustomerRechargeSummaryEntity> list = new ArrayList<>();
        RPTCustomerOrderPlanDailySearch search = new RPTCustomerOrderPlanDailySearch();
        Date queryDate = DateUtils.getDate(selectedYear, selectedMonth, 1);
        Date startDate = DateUtils.getStartDayOfMonth(queryDate);
        Date endDate = DateUtils.getLastDayOfMonth(queryDate);
        String quarter =  QuarterUtils.getSeasonQuarter(queryDate);
        search.setStartDate(startDate.getTime());
        search.setEndDate(endDate.getTime());
        search.setQuarter(quarter);
        search.setCustomerId(customerId);
        if (microServicesProperties.getReport().getEnabled()) {
            MSResponse<List<RPTCustomerRechargeSummaryEntity>> msResponse = msCustomerRechargeSummaryRptFeign.getCustomerRechargeSummarys(search);
            if (MSResponse.isSuccess(msResponse)) {
                list = msResponse.getData();
            }
        }

        return list;


    }


    /**
     * 检查报表导出
     */
    public void checkRptExportTask( Long customerId, Integer selectedYear, Integer selectedMonth, User user) {

        RPTCustomerOrderPlanDailySearch searchCondition = setSearch(customerId,selectedYear,selectedMonth);
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        reportExportTaskService.checkRptExportTask(RPTReportEnum.CUSTOMER_RECHARGE_SUMMARY_RPT, user, searchConditionJson);
    }

    /**
     * 创建报表导出任务
     */
    public void createRptExportTask(Long customerId, Integer selectedYear, Integer selectedMonth, User user) {

        RPTCustomerOrderPlanDailySearch searchCondition = setSearch(customerId,selectedYear,selectedMonth);
        String reportTitle =  "客户充值汇总" + selectedYear + "年" + selectedMonth + "月";
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        reportExportTaskService.createRptExportTask(RPTReportEnum.CUSTOMER_RECHARGE_SUMMARY_RPT, RPTReportTypeEnum.ORDER_REPORT, user, reportTitle, searchConditionJson);
    }


    public RPTCustomerOrderPlanDailySearch setSearch(Long customerId, Integer selectedYear, Integer selectedMonth) {
        RPTCustomerOrderPlanDailySearch search = new RPTCustomerOrderPlanDailySearch();
        Date queryDate = DateUtils.getDate(selectedYear, selectedMonth, 1);
        Date startDate = DateUtils.getStartDayOfMonth(queryDate);
        Date endDate = DateUtils.getLastDayOfMonth(queryDate);
        String quarter = QuarterUtils.getSeasonQuarter(queryDate);
        search.setStartDate(startDate.getTime());
        search.setEndDate(endDate.getTime());
        search.setQuarter(quarter);
        search.setCustomerId(customerId);
        return search;


    }


}
