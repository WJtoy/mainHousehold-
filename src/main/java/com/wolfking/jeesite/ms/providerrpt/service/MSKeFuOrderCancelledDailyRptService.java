package com.wolfking.jeesite.ms.providerrpt.service;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTKeFuOrderCancelledDailyEntity;
import com.kkl.kklplus.entity.rpt.common.RPTReportEnum;
import com.kkl.kklplus.entity.rpt.common.RPTReportTypeEnum;
import com.kkl.kklplus.entity.rpt.search.RPTKeFuOrderCancelledDailySearch;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.common.config.MicroServicesProperties;
import com.wolfking.jeesite.ms.providerrpt.feign.MSKeFuOrderCancelledDailyRptFeign;
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
public class MSKeFuOrderCancelledDailyRptService {

    @Autowired
    private MSKeFuOrderCancelledDailyRptFeign keFuOrderCancelledDailyRptFeign;

    @Autowired
    private MicroServicesProperties microServicesProperties;

    @Autowired
    private ReportExportTaskService reportExportTaskService;

    /**
     * 从rpt微服务中获取客服每日退单明细数据
     */
    public List<RPTKeFuOrderCancelledDailyEntity> getKeFuOrderCancelledDailyList(Integer subFlag,Integer selectedYear, Integer selectedMonth, Long keFuId, List<Long> productCategoryIds) {
        List<RPTKeFuOrderCancelledDailyEntity> list = new ArrayList<>();
        if (selectedYear != null && selectedYear > 0 && selectedMonth != null && selectedMonth > 0) {
            RPTKeFuOrderCancelledDailySearch search = new RPTKeFuOrderCancelledDailySearch();
            Date queryDate = DateUtils.getDate(selectedYear, selectedMonth, 1);
            Date startDate = DateUtils.getStartDayOfMonth(queryDate);
            Date endDate = DateUtils.getLastDayOfMonth(queryDate);
            String quarter = QuarterUtils.getSeasonQuarter(queryDate);
            search.setStartDate(startDate.getTime());
            search.setEndDate(endDate.getTime());
            search.setSubFlag(subFlag);
            search.setQuarter(quarter);
            if(keFuId != null && keFuId !=0){
                search.setKeFuId(keFuId);
            }
            search.setProductCategoryIds(productCategoryIds);
            if (microServicesProperties.getReport().getEnabled()) {
                MSResponse<List<RPTKeFuOrderCancelledDailyEntity>> msResponse = keFuOrderCancelledDailyRptFeign.getKeFuOrderCancelledDailyList(search);
                if (MSResponse.isSuccess(msResponse)) {
                    list = msResponse.getData();
                }
            }
        }
        return list;
    }

    /**
     * 检查报表导出
     */
    public void checkRptExportTask(Integer subFlag,Integer selectedYear, Integer selectedMonth, Long keFuId, List<Long> productCategoryIds,User user) {

        RPTKeFuOrderCancelledDailySearch searchCondition = setSearchCondition(subFlag,selectedYear,selectedMonth,keFuId,productCategoryIds);
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        reportExportTaskService.checkRptExportTask(RPTReportEnum.KEFU_CANCELLED_ORDER_RPT, user, searchConditionJson);
    }

    /**
     *创建报表导出任务
     */
    public void createRptExportTask(Integer subFlag,Integer selectedYear, Integer selectedMonth,  Long keFuId, List<Long> productCategoryIds,User user) {

        RPTKeFuOrderCancelledDailySearch searchCondition = setSearchCondition(subFlag,selectedYear,selectedMonth,keFuId,productCategoryIds);

        String reportTitle =  "客服每日退单报表" + selectedYear + "年" + selectedMonth + "月";
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        reportExportTaskService.createRptExportTask(RPTReportEnum.KEFU_CANCELLED_ORDER_RPT, RPTReportTypeEnum.ORDER_REPORT, user, reportTitle, searchConditionJson);
    }


    /**
     *设置筛选项的值
     */
    public RPTKeFuOrderCancelledDailySearch setSearchCondition(Integer subFlag,Integer selectedYear, Integer selectedMonth,  Long keFuId, List<Long> productCategoryIds){

        RPTKeFuOrderCancelledDailySearch searchCondition = new RPTKeFuOrderCancelledDailySearch();

        Date queryDate = DateUtils.getDate(selectedYear, selectedMonth, 1);
        Date startDate = DateUtils.getStartDayOfMonth(queryDate);
        Date endDate = DateUtils.getLastDayOfMonth(queryDate);
        String quarter = QuarterUtils.getSeasonQuarter(queryDate);
        searchCondition.setStartDate(startDate.getTime());
        searchCondition.setEndDate(endDate.getTime());
        searchCondition.setQuarter(quarter);
        searchCondition.setSubFlag(subFlag);
        if(keFuId != null && keFuId !=0){
            searchCondition.setKeFuId(keFuId);
        }

        searchCondition.setProductCategoryIds(productCategoryIds);
        return searchCondition;
    }
}
