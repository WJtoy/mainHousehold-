package com.wolfking.jeesite.ms.providerrpt.service;


import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTCustomerRechargeSummaryEntity;
import com.kkl.kklplus.entity.rpt.RPTRechargeRecordEntity;
import com.kkl.kklplus.entity.rpt.common.RPTReportEnum;
import com.kkl.kklplus.entity.rpt.common.RPTReportTypeEnum;
import com.kkl.kklplus.entity.rpt.search.RPTCustomerOrderPlanDailySearch;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.common.config.MicroServicesProperties;
import com.wolfking.jeesite.ms.providerrpt.feign.MSCustomerRechargeSummaryRptFeign;
import com.wolfking.jeesite.ms.providerrpt.feign.MSDepositRechargeRptFeign;
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
public class MSDepositRechargeSummaryRptService {


    @Autowired
    private MicroServicesProperties microServicesProperties;

    @Autowired
    private ReportExportTaskService reportExportTaskService;

    @Autowired
    private MSDepositRechargeRptFeign msDepositRechargeRptFeign;


    public List<RPTCustomerRechargeSummaryEntity>  getDepositRechargeSummary (Long customerId, Integer selectedYear, Integer selectedMonth){
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
            MSResponse<List<RPTCustomerRechargeSummaryEntity>> msResponse = msDepositRechargeRptFeign.getDepositRechargeSummary(search);
            if (MSResponse.isSuccess(msResponse)) {
                list = msResponse.getData();
            }
        }

        return list;


    }


    public Page<RPTRechargeRecordEntity> getDepositRechargeDetails(Page<RPTRechargeRecordEntity> page, Integer actionType, Date beginDate, Date endDate, Long customerId) {
        Page<RPTRechargeRecordEntity> returnPage = new Page<>(page.getPageNo(), page.getPageSize());
        RPTCustomerOrderPlanDailySearch search = new RPTCustomerOrderPlanDailySearch();
        search.setPageNo(page.getPageNo());
        search.setPageSize(page.getPageSize());
        search.setCustomerId(customerId);
        search.setPaymentType(actionType);
        Date[] dates = OrderUtils.getQuarterDates(beginDate,endDate, 0, 0);
        List<String> quarters = QuarterUtils.getQuarters(dates[0], dates[1]);
        if (quarters != null && quarters.size() > 0) {
            search.setQuarters(quarters);
        }
        search.setStartDate(beginDate.getTime());
        search.setEndDate(endDate.getTime());
        if (microServicesProperties.getReport().getEnabled()) {
            MSResponse<MSPage<RPTRechargeRecordEntity>> msResponse = msDepositRechargeRptFeign.getDepositRechargeDetails(search);
            if (MSResponse.isSuccess(msResponse)) {
                MSPage<RPTRechargeRecordEntity> data = msResponse.getData();
                returnPage.setCount(data.getRowCount());
                returnPage.setList(data.getList());
            }
        }

        return  returnPage;

    }

    /**
     * 检查报表导出
     */
    public void checkRptExportTask( Long customerId, Integer selectedYear, Integer selectedMonth, User user) {

        RPTCustomerOrderPlanDailySearch searchCondition = setSearch(customerId,selectedYear,selectedMonth);
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        reportExportTaskService.checkRptExportTask(RPTReportEnum.DEPOSIT_CHARGE_SUMMARY_RPT, user, searchConditionJson);
    }

    /**
     * 创建报表导出任务
     */
    public void createRptExportTask(Long customerId, Integer selectedYear, Integer selectedMonth, User user) {

        RPTCustomerOrderPlanDailySearch searchCondition = setSearch(customerId,selectedYear,selectedMonth);
        String reportTitle =  "质保金汇总" + selectedYear + "年" + selectedMonth + "月";
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        reportExportTaskService.createRptExportTask(RPTReportEnum.DEPOSIT_CHARGE_SUMMARY_RPT, RPTReportTypeEnum.ORDER_REPORT, user, reportTitle, searchConditionJson);
    }


    /**
     * 检查报表导出
     */
    public void checkExportTask(Integer actionType, Date beginDate,Date endDate,Long customerId, User user) {

        RPTCustomerOrderPlanDailySearch searchCondition = setDepositSearch(customerId,beginDate,endDate,actionType);
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        reportExportTaskService.checkRptExportTask(RPTReportEnum.DEPOSIT_CHARGE_DETAILS_RPT, user, searchConditionJson);
    }

    /**
     * 创建报表导出任务
     */
    public void createExportTask(Integer actionType, Date beginDate,Date endDate,Long customerId,User user) {

        RPTCustomerOrderPlanDailySearch searchCondition = setDepositSearch(customerId,beginDate,endDate,actionType);
        String reportTitle = "质保金明细（" + DateUtils.formatDate(beginDate, "yyyy年MM月dd日") +
                "~" + DateUtils.formatDate(endDate, "yyyy年MM月dd日") + "）";
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        reportExportTaskService.createRptExportTask(RPTReportEnum.DEPOSIT_CHARGE_DETAILS_RPT, RPTReportTypeEnum.ORDER_REPORT, user, reportTitle, searchConditionJson);
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

    public RPTCustomerOrderPlanDailySearch setDepositSearch(Long customerId, Date beginDate, Date endDate,Integer actionType){

        RPTCustomerOrderPlanDailySearch search = new RPTCustomerOrderPlanDailySearch();
        Date[] dates = OrderUtils.getQuarterDates(beginDate,endDate, 0, 0);
        List<String> quarters = QuarterUtils.getQuarters(dates[0], dates[1]);
        if (quarters != null && quarters.size() > 0) {
            search.setQuarters(quarters);
        }
        search.setPaymentType(actionType);
        search.setCustomerId(customerId);
        search.setStartDate(beginDate.getTime());
        search.setEndDate(endDate.getTime());
        return  search;

    }


}
