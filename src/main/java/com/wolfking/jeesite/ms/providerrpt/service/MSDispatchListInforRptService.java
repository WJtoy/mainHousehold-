package com.wolfking.jeesite.ms.providerrpt.service;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTDispatchOrderEntity;
import com.kkl.kklplus.entity.rpt.common.RPTReportEnum;
import com.kkl.kklplus.entity.rpt.common.RPTReportTypeEnum;
import com.kkl.kklplus.entity.rpt.search.RPTCompletedOrderDetailsSearch;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.common.config.MicroServicesProperties;
import com.wolfking.jeesite.ms.providerrpt.feign.MSDispatchListRptFeign;
import com.wolfking.jeesite.ms.providerrpt.utils.RedisGsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Service
public class MSDispatchListInforRptService {

    @Autowired
    private MicroServicesProperties microServicesProperties;

    @Autowired
    private MSDispatchListRptFeign msDispatchListRptFeign;

    @Autowired
    private ReportExportTaskService reportExportTaskService;

    public List<RPTDispatchOrderEntity> getPlanInformation(Integer selectedYear, Integer selectedMonth, List<Long> productCategoryIds){
        RPTCompletedOrderDetailsSearch search = new RPTCompletedOrderDetailsSearch();
        List<RPTDispatchOrderEntity> returnList = new ArrayList<>();
        if(selectedYear != null && selectedYear > 0 && selectedMonth != null && selectedMonth > 0){
            Date queryDate = DateUtils.getDate(selectedYear, selectedMonth, 1);
            Date startDate = DateUtils.getStartDayOfMonth(queryDate);
            Date endDate = DateUtils.getLastDayOfMonth(queryDate);
            String quarter = QuarterUtils.getSeasonQuarter(queryDate);
            search.setBeginDate(startDate.getTime());
            search.setEndDate(endDate.getTime());
            search.setQuarter(quarter);
            search.setProductCategoryIds(productCategoryIds);

            if (microServicesProperties.getReport().getEnabled()) {
                MSResponse<List<RPTDispatchOrderEntity>> msResponse = msDispatchListRptFeign.getDispatchListInformation(search);
                if (MSResponse.isSuccess(msResponse)) {
                    returnList = msResponse.getData();
                }
            }
        }
        return returnList;
    }

    /**
     * 获取接派单图表
     */
    public Map<String, Object> turnToChartInformation(Integer selectedYear, Integer selectedMonth,List<Long> productCategoryIds) {
        RPTCompletedOrderDetailsSearch search = new RPTCompletedOrderDetailsSearch();
        Map<String, Object> map = new HashMap<>();
        if(selectedYear != null && selectedYear > 0 && selectedMonth != null && selectedMonth > 0){
            Date queryDate = DateUtils.getDate(selectedYear, selectedMonth, 1);
            Date startDate = DateUtils.getStartDayOfMonth(queryDate);
            Date endDate = DateUtils.getLastDayOfMonth(queryDate);
            String quarter = QuarterUtils.getSeasonQuarter(queryDate);
            search.setBeginDate(startDate.getTime());
            search.setEndDate(endDate.getTime());
            search.setQuarter(quarter);
            search.setProductCategoryIds(productCategoryIds);
            if (microServicesProperties.getReport().getEnabled()) {
                MSResponse<Map<String, Object>> msResponse = msDispatchListRptFeign.getDispatchListInforChart(search);
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
    public void checkRptExportTask(Integer selectedYear,Integer selectedMonth , List<Long> productCategoryIds, User user) {

        RPTCompletedOrderDetailsSearch searchCondition = setSearchCondition(selectedYear , selectedMonth, productCategoryIds);
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        reportExportTaskService.checkRptExportTask(RPTReportEnum.DISPATCH_ORDER_RPT, user, searchConditionJson);
    }

    /**
     * 创建报表导出任务
     */
    public void createRptExportTask(Integer selectedYear,Integer selectedMonth, List<Long> productCategoryIds, User user) {

        RPTCompletedOrderDetailsSearch searchCondition = setSearchCondition(selectedYear, selectedMonth, productCategoryIds);
        String reportTitle = selectedYear + "年" + selectedMonth + "月" + "每日下单统计";
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        reportExportTaskService.createRptExportTask(RPTReportEnum.DISPATCH_ORDER_RPT, RPTReportTypeEnum.ORDER_REPORT, user, reportTitle, searchConditionJson);
    }


    /**
     * 设置筛选项的值
     */
    public RPTCompletedOrderDetailsSearch setSearchCondition(Integer selectedYear ,Integer selectedMonth, List<Long> productCategoryIds) {

        RPTCompletedOrderDetailsSearch searchCondition = new RPTCompletedOrderDetailsSearch();
        if(selectedYear != null && selectedYear > 0 && selectedMonth != null && selectedMonth > 0) {
            Date queryDate = DateUtils.getDate(selectedYear, selectedMonth, 1);
            Date startDate = DateUtils.getStartDayOfMonth(queryDate);
            Date endDate = DateUtils.getLastDayOfMonth(queryDate);
            String quarter = QuarterUtils.getSeasonQuarter(queryDate);
            searchCondition.setBeginDate(startDate.getTime());
            searchCondition.setEndDate(endDate.getTime());
            searchCondition.setQuarter(quarter);
            searchCondition.setProductCategoryIds(productCategoryIds);
        }
        return searchCondition;
    }

}
