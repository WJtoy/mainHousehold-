package com.wolfking.jeesite.ms.providerrpt.service;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTCustomerReceivableSummaryEntity;
import com.kkl.kklplus.entity.rpt.RPTKeFuOrderPlanDailyEntity;
import com.kkl.kklplus.entity.rpt.RPTMasterApplyEntity;
import com.kkl.kklplus.entity.rpt.common.RPTReportEnum;
import com.kkl.kklplus.entity.rpt.common.RPTReportTypeEnum;
import com.kkl.kklplus.entity.rpt.search.RPTComplainStatisticsDailySearch;
import com.kkl.kklplus.entity.rpt.search.RPTKeFuOrderPlanDailySearch;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.utils.CustomerUtils;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.common.config.MicroServicesProperties;
import com.wolfking.jeesite.ms.providerrpt.feign.MSKeFuOrderPlanDailyRptFeign;
import com.wolfking.jeesite.ms.providerrpt.feign.MSMasterApplyRptFeign;
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
public class MSMasterApplyRptService {
    @Autowired
    private MSMasterApplyRptFeign msMasterApplyRptFeign;
    
    @Autowired
    private MicroServicesProperties microServicesProperties;

    @Autowired
    private ReportExportTaskService reportExportTaskService;

    /**
     * 从rpt微服务中获取客服每日接单明细数据
     */
    public Page<RPTMasterApplyEntity> getMasterApplyList(Page<RPTMasterApplyEntity> page, Long customerId, Integer selectedYear, Integer selectedMonth) {
        Page<RPTMasterApplyEntity> returnPage = new Page<>(page.getPageNo(), page.getPageSize());
        if ( selectedYear != null && selectedYear > 0 && selectedMonth != null && selectedMonth > 0) {
            RPTComplainStatisticsDailySearch search = new RPTComplainStatisticsDailySearch();
            Date queryDate = DateUtils.getDate(selectedYear, selectedMonth, 1);
            Date startDate = DateUtils.getStartDayOfMonth(queryDate);
            Date endDate = DateUtils.getLastDayOfMonth(queryDate);
            String quarter = QuarterUtils.getSeasonQuarter(queryDate);
            search.setPageNo(page.getPageNo());
            search.setPageSize(page.getPageSize());
            search.setStartDate(startDate.getTime());
            search.setEndDate(endDate.getTime());
            search.setCustomerId(customerId);
            search.setQuarter(quarter);
            if (microServicesProperties.getReport().getEnabled()) {
                MSResponse<MSPage<RPTMasterApplyEntity>> msResponse = msMasterApplyRptFeign.getMasterApplyList(search);
                if (MSResponse.isSuccess(msResponse)) {
                    MSPage<RPTMasterApplyEntity> data = msResponse.getData();
                    returnPage.setCount(data.getRowCount());
                    returnPage.setList(data.getList());
                }
            }
        }
        return returnPage;
    }

    /**
     * 检查报表导出
     */
    public void checkRptExportTask(Long customerId,Integer selectedYear, Integer selectedMonth,User user) {

        RPTComplainStatisticsDailySearch searchCondition = setSearchCondition(customerId,selectedYear,selectedMonth);
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        reportExportTaskService.checkRptExportTask(RPTReportEnum.MASTER_APPLY_RPT, user, searchConditionJson);
    }

    /**
     *创建报表导出任务
     */
    public void createRptExportTask(Long customerId,Integer selectedYear, Integer selectedMonth,User user) {

        RPTComplainStatisticsDailySearch searchCondition = setSearchCondition(customerId,selectedYear,selectedMonth);
        Customer customer = CustomerUtils.getCustomer(customerId);
        String customerName = customer == null ? "" : customer.getName();
        String reportTitle =  customerName +  selectedYear + "年" + selectedMonth + "月"+ "配件报表";
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        reportExportTaskService.createRptExportTask(RPTReportEnum.MASTER_APPLY_RPT, RPTReportTypeEnum.ORDER_REPORT, user, reportTitle, searchConditionJson);
    }


    /**
     *设置筛选项的值
     */
    public RPTComplainStatisticsDailySearch setSearchCondition(Long customerId,Integer selectedYear, Integer selectedMonth){

        RPTComplainStatisticsDailySearch searchCondition = new RPTComplainStatisticsDailySearch();

        Date queryDate = DateUtils.getDate(selectedYear, selectedMonth, 1);
        Date startDate = DateUtils.getStartDayOfMonth(queryDate);
        Date endDate = DateUtils.getLastDayOfMonth(queryDate);
        String quarter = QuarterUtils.getSeasonQuarter(queryDate);
        searchCondition.setStartDate(startDate.getTime());
        searchCondition.setEndDate(endDate.getTime());
        searchCondition.setQuarter(quarter);
        searchCondition.setCustomerId(customerId);
        return searchCondition;
    }
}
