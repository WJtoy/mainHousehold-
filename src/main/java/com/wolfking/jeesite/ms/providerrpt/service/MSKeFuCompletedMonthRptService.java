package com.wolfking.jeesite.ms.providerrpt.service;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTKeFuCompletedMonthEntity;
import com.kkl.kklplus.entity.rpt.common.RPTReportEnum;
import com.kkl.kklplus.entity.rpt.common.RPTReportTypeEnum;
import com.kkl.kklplus.entity.rpt.search.RPTCustomerOrderPlanDailySearch;
import com.kkl.kklplus.entity.rpt.search.RPTGradedOrderSearch;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.common.config.MicroServicesProperties;
import com.wolfking.jeesite.ms.providerrpt.feign.MSKeFuCompletedMonthRptFeign;
import com.wolfking.jeesite.ms.providerrpt.utils.RedisGsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Service
public class MSKeFuCompletedMonthRptService {

    @Autowired
    private MicroServicesProperties microServicesProperties;

    @Autowired
    private MSKeFuCompletedMonthRptFeign msKeFuCompletedMonthRptFeign;

    @Autowired
    private ReportExportTaskService reportExportTaskService;

    public List<RPTKeFuCompletedMonthEntity> getKeFuCompletedMonthList(Integer subFlag,Integer selectedYear, Long kefuId, List<Long> productCategoryIds) {
        List<RPTKeFuCompletedMonthEntity> list = new ArrayList<>();
        if (selectedYear != null && selectedYear > 0) {
            RPTGradedOrderSearch search = setKefuCompletedSearchCondition(subFlag,selectedYear, kefuId,productCategoryIds);
            if (microServicesProperties.getReport().getEnabled()) {
                MSResponse<List<RPTKeFuCompletedMonthEntity>> msResponse = msKeFuCompletedMonthRptFeign.getKeFuCompletedMonthInfo(search);
                if (MSResponse.isSuccess(msResponse)) {
                    list = msResponse.getData();
                }
            }
        }
        return list;
    }


    public RPTGradedOrderSearch setKefuCompletedSearchCondition(Integer subFlag,Integer selectYear, Long kefuId,List<Long> productCategoryIds){
        RPTGradedOrderSearch rptGradedOrderSearchCondition = new RPTGradedOrderSearch();
        Date queryDate = DateUtils.getDate(selectYear, 1, 1);
        Date beginDate = DateUtils.getStartDayOfMonth(queryDate);
        rptGradedOrderSearchCondition.setBeginDate(beginDate.getTime());
        rptGradedOrderSearchCondition.setKefuId(kefuId);
        rptGradedOrderSearchCondition.setProductCategoryIds(productCategoryIds);
        rptGradedOrderSearchCondition.setSubFlag(subFlag);
        return rptGradedOrderSearchCondition;
    }

    public Map<String, Object> getKeFuCompletedMonthChart(Integer subFlag,Integer selectedYear,Long kefuId,List<Long> productCategoryIds) {
        Map<String, Object> map = new HashMap<>();
        if (selectedYear != null && selectedYear > 0 ) {
            RPTGradedOrderSearch search = setKefuCompletedSearchCondition(subFlag,selectedYear, kefuId,productCategoryIds);
            if (microServicesProperties.getReport().getEnabled()) {
                MSResponse<Map<String, Object>> msResponse = msKeFuCompletedMonthRptFeign.getKeFuCompletedMonthChartList(search);
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
    public void checkRptExportTask(Integer subFlag,Integer selectedYear, Long kefuId , List<Long> productCategoryIds, User user) {

        RPTGradedOrderSearch searchCondition = setKefuCompletedSearchCondition(subFlag,selectedYear , kefuId, productCategoryIds);
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        reportExportTaskService.checkRptExportTask(RPTReportEnum.KEFU_COMPLETED_MONTH_ORDER_RPT, user, searchConditionJson);
    }

    /**
     * 创建报表导出任务
     */
    public void createRptExportTask(Integer subFlag,Integer selectedYear, Long kefuId, List<Long> productCategoryIds, User user) {

        RPTGradedOrderSearch searchCondition = setKefuCompletedSearchCondition(subFlag,selectedYear, kefuId, productCategoryIds);
        String reportTitle =  "客服每月完工单报表" + selectedYear + "年";
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        reportExportTaskService.createRptExportTask(RPTReportEnum.KEFU_COMPLETED_MONTH_ORDER_RPT, RPTReportTypeEnum.ORDER_REPORT, user, reportTitle, searchConditionJson);
    }


}
