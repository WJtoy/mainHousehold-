package com.wolfking.jeesite.modules.finance.rpt.service;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTCustomerRevenueEntity;
import com.kkl.kklplus.entity.rpt.common.RPTReportEnum;
import com.kkl.kklplus.entity.rpt.common.RPTReportTypeEnum;
import com.kkl.kklplus.entity.rpt.search.RPTCustomerRevenueSearch;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.common.config.MicroServicesProperties;
import com.wolfking.jeesite.ms.providerrpt.feign.MSCustomerRevenueRptFeign;
import com.wolfking.jeesite.ms.providerrpt.service.ReportExportTaskService;
import com.wolfking.jeesite.ms.providerrpt.utils.RedisGsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Service
public class FiCustomerRevenueRptService {
    @Autowired
    private MSCustomerRevenueRptFeign msCustomerRevenueRptFeign;

    @Autowired
    private MicroServicesProperties microServicesProperties;

    @Autowired
    private FiReportExportTaskService fiReportExportTaskService;

    /**
     * 从rpt微服务中获取数据
     */
    public List<RPTCustomerRevenueEntity> getCustomerRevenueRptList(Long customerId,Integer selectedYear, Integer selectedMonth,List<Long> productCategoryIds) {

        List<RPTCustomerRevenueEntity> list = new ArrayList<>();
        if (selectedYear != null && selectedMonth != null) {
            RPTCustomerRevenueSearch search = new RPTCustomerRevenueSearch();
            if(customerId != null){
                search.setCustomerId(customerId);
            }
            search.setSelectedYear(selectedYear);
            search.setSelectedMonth(selectedMonth);
            search.setProductCategoryIds(productCategoryIds);
            if (microServicesProperties.getReport().getEnabled()) {
                MSResponse<List<RPTCustomerRevenueEntity>> msResponse = msCustomerRevenueRptFeign.getCustomerRevenueRptList(search);
                if (MSResponse.isSuccess(msResponse)) {
                    list = msResponse.getData();
                }
            }
        }
        return list;
    }
    /**
     * 从rpt微服务中获取图表数据
     */
    public Map<String, Object> getSMSQtyStatisticsChartList(Long customerId,Integer selectedYear, Integer selectedMonth,List<Long> productCategoryIds) {
        Map<String, Object> map = new HashMap<>();
        if (selectedYear != null && selectedMonth != null) {
            RPTCustomerRevenueSearch search = new RPTCustomerRevenueSearch();
            if(customerId != null){
                search.setCustomerId(customerId);
            }
            search.setSelectedYear(selectedYear);
            search.setSelectedMonth(selectedMonth);
            search.setProductCategoryIds(productCategoryIds);
            if (microServicesProperties.getReport().getEnabled()) {
                MSResponse<Map<String, Object>> msResponse = msCustomerRevenueRptFeign.getCustomerRevenueChartList(search);
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
    public void checkRptExportTask(Long customerId,Integer selectedYear, Integer selectedMonth,List<Long> productCategoryIds, User user) {

        RPTCustomerRevenueSearch searchCondition = setSearchCondition(customerId,selectedYear,selectedMonth,productCategoryIds);
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        fiReportExportTaskService.checkRptExportTask(RPTReportEnum.FI_CUSTOMER_REVENUE_RPT, user, searchConditionJson);
    }

    /**
     * 创建报表导出任务
     */
    public void createRptExportTask(Long customerId,Integer selectedYear, Integer selectedMonth,List<Long> productCategoryIds, User user) {

        RPTCustomerRevenueSearch searchCondition = setSearchCondition(customerId,selectedYear,selectedMonth,productCategoryIds);
        String reportTitle = selectedYear + "年" + selectedMonth+ "月" + "客户营收明细";
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        fiReportExportTaskService.createRptExportTask(RPTReportEnum.FI_CUSTOMER_REVENUE_RPT, RPTReportTypeEnum.FINANCE_REPORT, user, reportTitle, searchConditionJson);
    }


    /**
     * 设置筛选项的值
     */
    public RPTCustomerRevenueSearch setSearchCondition(Long customerId,Integer selectedYear, Integer selectedMonth,List<Long> productCategoryIds) {

        RPTCustomerRevenueSearch searchCondition = new RPTCustomerRevenueSearch();
        searchCondition.setCustomerId(customerId);
        searchCondition.setSelectedYear(selectedYear);
        searchCondition.setSelectedMonth(selectedMonth);
        searchCondition.setProductCategoryIds(productCategoryIds);
        return searchCondition;
    }
}
