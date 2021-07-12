package com.wolfking.jeesite.ms.providerrpt.service;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTKeFuOrderPlanDailyEntity;
import com.kkl.kklplus.entity.rpt.common.RPTReportEnum;
import com.kkl.kklplus.entity.rpt.common.RPTReportTypeEnum;
import com.kkl.kklplus.entity.rpt.search.RPTKeFuOrderPlanDailySearch;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.common.config.MicroServicesProperties;
import com.wolfking.jeesite.ms.providerrpt.feign.MSKeFuOrderPlanDailyRptFeign;
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
public class MSKeFuOrderPlanDailyRptService {
    @Autowired
    private MSKeFuOrderPlanDailyRptFeign msKeFuOrderPlanDailyRptFeign;
    
    @Autowired
    private MicroServicesProperties microServicesProperties;

    @Autowired
    private ReportExportTaskService reportExportTaskService;

    /**
     * 从rpt微服务中获取客服每日接单明细数据
     */
    public List<RPTKeFuOrderPlanDailyEntity> getKeFuOrderPlanDailyList(Integer subType, Integer selectedYear, Integer selectedMonth, Long keFuId, List<Long> productCategoryIds) {
         List<RPTKeFuOrderPlanDailyEntity> list = new ArrayList<>();
        if ( selectedYear != null && selectedYear > 0 && selectedMonth != null && selectedMonth > 0) {
            RPTKeFuOrderPlanDailySearch search = new RPTKeFuOrderPlanDailySearch();
            Date queryDate = DateUtils.getDate(selectedYear, selectedMonth, 1);
            Date startDate = DateUtils.getStartDayOfMonth(queryDate);
            Date endDate = DateUtils.getLastDayOfMonth(queryDate);
            String quarter = QuarterUtils.getSeasonQuarter(queryDate);
            search.setStartDate(startDate.getTime());
            search.setEndDate(endDate.getTime());
            search.setSubFlag(subType);
            search.setQuarter(quarter);
            if(keFuId != null && keFuId !=0){
                search.setKeFuId(keFuId);
            }
            search.setProductCategoryIds(productCategoryIds);
            if (microServicesProperties.getReport().getEnabled()) {
                MSResponse<List<RPTKeFuOrderPlanDailyEntity>> msResponse = msKeFuOrderPlanDailyRptFeign.getKeFuOrderPlanDailyList(search);
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
    public void checkRptExportTask(Integer subType,Integer selectedYear, Integer selectedMonth, Long keFuId, List<Long> productCategoryIds,User user) {

        RPTKeFuOrderPlanDailySearch searchCondition = setSearchCondition(subType,selectedYear,selectedMonth,keFuId,productCategoryIds);
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        reportExportTaskService.checkRptExportTask(RPTReportEnum.KEFU_PLAN_ORDER_RPT, user, searchConditionJson);
    }

    /**
     *创建报表导出任务
     */
    public void createRptExportTask(Integer subType,Integer selectedYear, Integer selectedMonth,  Long keFuId, List<Long> productCategoryIds,User user) {

        RPTKeFuOrderPlanDailySearch searchCondition = setSearchCondition(subType,selectedYear,selectedMonth,keFuId,productCategoryIds);

        String reportTitle =  "客服每日接单报表" + selectedYear + "年" + selectedMonth + "月";
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        reportExportTaskService.createRptExportTask(RPTReportEnum.KEFU_PLAN_ORDER_RPT, RPTReportTypeEnum.ORDER_REPORT, user, reportTitle, searchConditionJson);
    }


    /**
     *设置筛选项的值
     */
    public RPTKeFuOrderPlanDailySearch setSearchCondition(Integer subType,Integer selectedYear, Integer selectedMonth,  Long keFuId, List<Long> productCategoryIds){

        RPTKeFuOrderPlanDailySearch searchCondition = new RPTKeFuOrderPlanDailySearch();

        Date queryDate = DateUtils.getDate(selectedYear, selectedMonth, 1);
        Date startDate = DateUtils.getStartDayOfMonth(queryDate);
        Date endDate = DateUtils.getLastDayOfMonth(queryDate);
        String quarter = QuarterUtils.getSeasonQuarter(queryDate);
        searchCondition.setStartDate(startDate.getTime());
        searchCondition.setEndDate(endDate.getTime());
        searchCondition.setQuarter(quarter);
        searchCondition.setSubFlag(subType);
        if(keFuId != null && keFuId !=0){
            searchCondition.setKeFuId(keFuId);
        }

        searchCondition.setProductCategoryIds(productCategoryIds);
        return searchCondition;
    }
}
