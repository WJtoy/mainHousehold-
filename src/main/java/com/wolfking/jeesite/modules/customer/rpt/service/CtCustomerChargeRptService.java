package com.wolfking.jeesite.modules.customer.rpt.service;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTCancelledOrderEntity;
import com.kkl.kklplus.entity.rpt.RPTCompletedOrderEntity;
import com.kkl.kklplus.entity.rpt.RPTCustomerChargeSummaryMonthlyEntity;
import com.kkl.kklplus.entity.rpt.RPTCustomerWriteOffEntity;
import com.kkl.kklplus.entity.rpt.common.RPTReportEnum;
import com.kkl.kklplus.entity.rpt.common.RPTReportTypeEnum;
import com.kkl.kklplus.entity.rpt.search.RPTCancelledOrderSearch;
import com.kkl.kklplus.entity.rpt.search.RPTCompletedOrderSearch;
import com.kkl.kklplus.entity.rpt.search.RPTCustomerChargeSearch;
import com.kkl.kklplus.entity.rpt.search.RPTCustomerWriteOffSearch;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.utils.CustomerUtils;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.common.config.MicroServicesProperties;
import com.wolfking.jeesite.ms.providerrpt.customer.feign.CtCustomerChargeRptFeign;
import com.wolfking.jeesite.ms.providerrpt.feign.MSCustomerChargeRptFeign;
import com.wolfking.jeesite.ms.providerrpt.service.ReportExportTaskService;
import com.wolfking.jeesite.ms.providerrpt.utils.RedisGsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Service
public class CtCustomerChargeRptService {


    @Autowired
    private CtCustomerChargeRptFeign ctCustomerChargeRptFeign;

    @Autowired
    private CtReportExportTaskService ctReportExportTaskService;

    @Autowired
    private MicroServicesProperties microServicesProperties;

    /**
     *从rpt微服务中获取客户的工单数量与消费金额信息
     * @param customerId
     * @param selectedYear
     * @param selectedMonth
     * @return
     */
    public RPTCustomerChargeSummaryMonthlyEntity getCustomerChargeSummary(Long customerId, Integer selectedYear, Integer selectedMonth) {
        RPTCustomerChargeSummaryMonthlyEntity result = null;
        if (customerId != null && customerId > 0 && selectedYear != null && selectedYear > 0 && selectedMonth != null && selectedMonth > 0) {
            RPTCustomerChargeSearch search = new RPTCustomerChargeSearch();
            search.setCustomerId(customerId);
            search.setSelectedYear(selectedYear);
            search.setSelectedMonth(selectedMonth);
            if (microServicesProperties.getReport().getEnabled()) {
                MSResponse<RPTCustomerChargeSummaryMonthlyEntity> msResponse = ctCustomerChargeRptFeign.getCustomerChargeSummaryMonthly(search);
                if (MSResponse.isSuccess(msResponse)) {
                    result = msResponse.getData();
                }
            }
        }
        return result;
    }

    /**
     * 从rpt微服务中获取完工单明细数据
     */
    public Page<RPTCompletedOrderEntity> getCompletedOrderList(Page<RPTCompletedOrderEntity> page, Long customerId, Integer selectedYear, Integer selectedMonth) {
        Page<RPTCompletedOrderEntity> returnPage = new Page<>(page.getPageNo(), page.getPageSize());
        if (customerId != null && customerId > 0 && selectedYear != null && selectedYear > 0 && selectedMonth != null && selectedMonth > 0) {
            Date queryDate = DateUtils.getDate(selectedYear, selectedMonth, 1);
            String quarter = QuarterUtils.getSeasonQuarter(queryDate);
            Date beginDate = DateUtils.getStartOfDay(queryDate);
            Date endDate = DateUtils.addMonth(beginDate, 1);
            RPTCompletedOrderSearch search = new RPTCompletedOrderSearch();
            search.setPageNo(page.getPageNo());
            search.setPageSize(page.getPageSize());
            search.setQuarter(quarter);
            search.setBeginChargeDate(beginDate.getTime());
            search.setEndChargeDate(endDate.getTime() - 1);
            search.setCustomerId(customerId);
            if (microServicesProperties.getReport().getEnabled()) {
                MSResponse<MSPage<RPTCompletedOrderEntity>> msResponse = ctCustomerChargeRptFeign.getCompletedOrderList(search);
                if (MSResponse.isSuccess(msResponse)) {
                    MSPage<RPTCompletedOrderEntity> data = msResponse.getData();
                    returnPage.setCount(data.getRowCount());
                    returnPage.setList(data.getList());

                }
            }
        }
        return returnPage;
    }
    /**
     * 从rpt微服务中获取退单或取消单明细数据
     */
    public Page<RPTCancelledOrderEntity> getCancelledOrReturnedOrderList(Page<RPTCancelledOrderEntity> page, Long customerId, Integer selectedYear, Integer selectedMonth) {
        Page<RPTCancelledOrderEntity> returnPage = new Page<>(page.getPageNo(), page.getPageSize());
        if (customerId != null && customerId > 0 && selectedYear != null && selectedYear > 0 && selectedMonth != null && selectedMonth > 0) {
            Date queryDate = DateUtils.getDate(selectedYear, selectedMonth, 1);
            String quarter = QuarterUtils.getSeasonQuarter(queryDate);
            Date beginDate = DateUtils.getStartOfDay(queryDate);
            Date endDate = DateUtils.addMonth(beginDate, 1);
            RPTCancelledOrderSearch search = new RPTCancelledOrderSearch();
            search.setPageNo(page.getPageNo());
            search.setPageSize(page.getPageSize());
            search.setQuarter(quarter);
            search.setBeginCloseDate(beginDate.getTime());
            search.setEndCloseDate(endDate.getTime() - 1);
            search.setCustomerId(customerId);
            if (microServicesProperties.getReport().getEnabled()) {
                MSResponse<MSPage<RPTCancelledOrderEntity>> msResponse = ctCustomerChargeRptFeign.getCancelledOrderList(search);
                if (MSResponse.isSuccess(msResponse)) {
                    MSPage<RPTCancelledOrderEntity> data = msResponse.getData();
                    returnPage.setCount(data.getRowCount());
                    returnPage.setList(data.getList());

                }
            }
        }
        return returnPage;
    }

    /**
     * 从rpt微服务中获取退补单明细数据
     */
    public Page<RPTCustomerWriteOffEntity> getCustomerWriteOffList(Page<RPTCustomerWriteOffEntity> page, Long customerId, Integer selectedYear, Integer selectedMonth) {
        Page<RPTCustomerWriteOffEntity> returnPage = new Page<>(page.getPageNo(), page.getPageSize());
        if (customerId != null && customerId > 0 && selectedYear != null && selectedYear > 0 && selectedMonth != null && selectedMonth > 0) {
            Date queryDate = DateUtils.getDate(selectedYear, selectedMonth, 1);
            String quarter = QuarterUtils.getSeasonQuarter(queryDate);
            Date beginDate = DateUtils.getStartOfDay(queryDate);
            Date endDate = DateUtils.addMonth(beginDate, 1);
            RPTCustomerWriteOffSearch search = new RPTCustomerWriteOffSearch();
            search.setPageNo(page.getPageNo());
            search.setPageSize(page.getPageSize());
            search.setQuarter(quarter);
            search.setBeginWriteOffCreateDate(beginDate.getTime());
            search.setEndWriteOffCreateDate(endDate.getTime() - 1);
            search.setCustomerId(customerId);
            if (microServicesProperties.getReport().getEnabled()) {
                MSResponse<MSPage<RPTCustomerWriteOffEntity>> msResponse = ctCustomerChargeRptFeign.getCustomerWriteOffList(search);
                if (MSResponse.isSuccess(msResponse)) {
                    MSPage<RPTCustomerWriteOffEntity> data = msResponse.getData();
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
    public void checkRptExportTask(Long customerId,int selectedYear,int selectedMonth,User user) {

        RPTCustomerChargeSearch searchCondition = setSearchCondition(customerId, selectedYear, selectedMonth);
        searchCondition.setPageNo(1);
        searchCondition.setPageSize(200000);
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);

        ctReportExportTaskService.checkRptExportTask(RPTReportEnum.CT_CUSTOMER_CHARGE_RPT, user, searchConditionJson);
    }

    /**
     *创建报表导出任务
     */
    public void createRptExportTask(Long customerId,int selectedYear,int selectedMonth,User user) {


        RPTCustomerChargeSearch searchCondition = setSearchCondition(customerId, selectedYear, selectedMonth);
        Customer customer = CustomerUtils.getCustomer(customerId);
        String customerName = customer.getName();
        searchCondition.setPageNo(1);
        searchCondition.setPageSize(200000);
        String reportTitle = customerName + selectedYear + "年" + selectedMonth + "月" + "对账单";
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        ctReportExportTaskService.createRptExportTask(RPTReportEnum.CT_CUSTOMER_CHARGE_RPT, RPTReportTypeEnum.FINANCE_REPORT, user, reportTitle, searchConditionJson);
    }


    /**
     *设置筛选项的值
     */
    public RPTCustomerChargeSearch setSearchCondition(Long customerId,int selectedYear,int selectedMonth){

        RPTCustomerChargeSearch searchCondition = new RPTCustomerChargeSearch();
        searchCondition.setCustomerId(customerId);
        searchCondition.setSelectedYear(selectedYear);
        searchCondition.setSelectedMonth(selectedMonth);

        return searchCondition;
    }
}
