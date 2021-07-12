package com.wolfking.jeesite.modules.finance.rpt.service;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTCustomerOrderPlanDailyEntity;
import com.kkl.kklplus.entity.rpt.common.RPTReportEnum;
import com.kkl.kklplus.entity.rpt.common.RPTReportTypeEnum;
import com.kkl.kklplus.entity.rpt.search.RPTCustomerOrderPlanDailySearch;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.utils.CustomerUtils;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.common.config.MicroServicesProperties;
import com.wolfking.jeesite.ms.providerrpt.feign.MSCustomerOrderPlanDailyRptFeign;
import com.wolfking.jeesite.ms.providerrpt.service.ReportExportTaskService;
import com.wolfking.jeesite.ms.providerrpt.utils.RedisGsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Service
public class FiCustomerOrderPlanDailyRptService {

    @Autowired
    private MSCustomerOrderPlanDailyRptFeign msCustomerOrderPlanDailyRptFeign;

    @Autowired
    private MicroServicesProperties microServicesProperties;

    @Autowired
    private FiReportExportTaskService fiReportExportTaskService;

    /**
     * 从rpt微服务中获取客户每日下单单明细数据
     */
    public List<RPTCustomerOrderPlanDailyEntity> getCustomerOrderPlanDailyList(Integer selectedYear, Integer selectedMonth, Long salesId, Long customerId, List<Long> productCategoryIds) {
        List<RPTCustomerOrderPlanDailyEntity> list = new ArrayList<>();
        if (selectedYear != null && selectedYear > 0 && selectedMonth != null && selectedMonth > 0) {
            RPTCustomerOrderPlanDailySearch search = new RPTCustomerOrderPlanDailySearch();
            Date queryDate = DateUtils.getDate(selectedYear, selectedMonth, 1);
            Date startDate = DateUtils.getStartDayOfMonth(queryDate);
            Date endDate = DateUtils.getLastDayOfMonth(queryDate);
            String quarter = QuarterUtils.getSeasonQuarter(queryDate);
            search.setStartDate(startDate.getTime());
            search.setEndDate(endDate.getTime());
            search.setQuarter(quarter);
            search.setSalesId(salesId);
            if (customerId != null && customerId != 0) {
                search.setCustomerId(customerId);
            }
            search.setProductCategoryIds(productCategoryIds);
            if (microServicesProperties.getReport().getEnabled()) {
                MSResponse<List<RPTCustomerOrderPlanDailyEntity>> msResponse = msCustomerOrderPlanDailyRptFeign.getCustomerOrderPlanDailyList(search);
                if (MSResponse.isSuccess(msResponse)) {
                    list = msResponse.getData();
                }
            }
        }
        return list;
    }

    /**
     * 从rpt微服务中获取客户每日催单图表数据
     */
    public Map<String, Object> getCustomerOrderPlanDailyChartList(Integer selectedYear, Integer selectedMonth, Long salesId, Long customerId, List<Long> productCategoryIds) {
        Map<String, Object> map = new HashMap<>();
        if (selectedYear != null && selectedYear > 0 && selectedMonth != null && selectedMonth > 0) {
            RPTCustomerOrderPlanDailySearch search = new RPTCustomerOrderPlanDailySearch();
            Date queryDate = DateUtils.getDate(selectedYear, selectedMonth, 1);
            Date startDate = DateUtils.getStartDayOfMonth(queryDate);
            Date endDate = DateUtils.getLastDayOfMonth(queryDate);
            String quarter = QuarterUtils.getSeasonQuarter(queryDate);
            search.setStartDate(startDate.getTime());
            search.setEndDate(endDate.getTime());
            search.setQuarter(quarter);
            search.setSalesId(salesId);
            if (customerId != null && customerId != 0) {
                search.setCustomerId(customerId);
            }
            search.setProductCategoryIds(productCategoryIds);
            if (microServicesProperties.getReport().getEnabled()) {
                MSResponse<Map<String, Object>> msResponse = msCustomerOrderPlanDailyRptFeign.getCustomerOrderPlanChartList(search);
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
    public void checkRptExportTask(Integer selectedYear, Integer selectedMonth, Long salesId, Long customerId, List<Long> productCategoryIds, User user) {

        RPTCustomerOrderPlanDailySearch searchCondition = setSearchCondition(selectedYear, selectedMonth, salesId, customerId, productCategoryIds);
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        fiReportExportTaskService.checkRptExportTask(RPTReportEnum.FI_CUSTOMER_PLAN_ORDER_RPT, user, searchConditionJson);
    }

    /**
     * 创建报表导出任务
     */
    public void createRptExportTask(Integer selectedYear, Integer selectedMonth, Long salesId, Long customerId, List<Long> productCategoryIds, User user) {

        RPTCustomerOrderPlanDailySearch searchCondition = setSearchCondition(selectedYear, selectedMonth, salesId, customerId, productCategoryIds);
        Customer customer = CustomerUtils.getCustomer(customerId);
        String customerName = customer == null ? "" : customer.getName();
        String reportTitle = customerName + "客户每日下单报表" + selectedYear + "年" + selectedMonth + "月";
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        fiReportExportTaskService.createRptExportTask(RPTReportEnum.FI_CUSTOMER_PLAN_ORDER_RPT, RPTReportTypeEnum.ORDER_REPORT, user, reportTitle, searchConditionJson);
    }


    /**
     * 设置筛选项的值
     */
    public RPTCustomerOrderPlanDailySearch setSearchCondition(Integer selectedYear, Integer selectedMonth, Long salesId, Long customerId, List<Long> productCategoryIds) {

        RPTCustomerOrderPlanDailySearch searchCondition = new RPTCustomerOrderPlanDailySearch();

        Date queryDate = DateUtils.getDate(selectedYear, selectedMonth, 1);
        Date startDate = DateUtils.getStartDayOfMonth(queryDate);
        Date endDate = DateUtils.getLastDayOfMonth(queryDate);
        String quarter = QuarterUtils.getSeasonQuarter(queryDate);
        searchCondition.setStartDate(startDate.getTime());
        searchCondition.setEndDate(endDate.getTime());
        searchCondition.setQuarter(quarter);
        searchCondition.setSalesId(salesId);
        if (customerId != null && customerId != 0) {
            searchCondition.setCustomerId(customerId);
        }
        searchCondition.setProductCategoryIds(productCategoryIds);
        return searchCondition;
    }

}
