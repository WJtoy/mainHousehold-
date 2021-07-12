package com.wolfking.jeesite.modules.finance.rpt.service;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTAbnormalFinancialAuditEntity;
import com.kkl.kklplus.entity.rpt.common.RPTReportEnum;
import com.kkl.kklplus.entity.rpt.common.RPTReportTypeEnum;
import com.kkl.kklplus.entity.rpt.search.RPTCustomerOrderPlanDailySearch;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.common.config.MicroServicesProperties;
import com.wolfking.jeesite.ms.providerrpt.feign.MSAbnormalFinancialReviewRptFeign;
import com.wolfking.jeesite.ms.providerrpt.service.ReportExportTaskService;
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
public class FiAbnormalFinancialReviewRptService {

    @Autowired
    private MicroServicesProperties microServicesProperties;

    @Autowired
    private FiReportExportTaskService fiReportExportTaskService;

    @Autowired
    private MSAbnormalFinancialReviewRptFeign msAbnormalFinancialReviewRptFeign;

    public List<RPTAbnormalFinancialAuditEntity> getAbnormalPlanDailyList(Integer selectedYear, Integer selectedMonth , Long checkerId) {
            List<RPTAbnormalFinancialAuditEntity> list = new ArrayList<>();
            RPTCustomerOrderPlanDailySearch search = new RPTCustomerOrderPlanDailySearch();
            Date queryDate = DateUtils.getDate(selectedYear, selectedMonth, 1);
            Date startDate = DateUtils.getStartDayOfMonth(queryDate);
            Date endDate = DateUtils.getLastDayOfMonth(queryDate);
            search.setEndDate(endDate.getTime());
            search.setStartDate(startDate.getTime());
            search.setId(checkerId);
            if (microServicesProperties.getReport().getEnabled()) {
                MSResponse<List<RPTAbnormalFinancialAuditEntity>> msResponse = msAbnormalFinancialReviewRptFeign.getAbnormalFinancialList(search);
                if (MSResponse.isSuccess(msResponse)) {
                    list = msResponse.getData();
                }
            }
        return list;
    }


    /**
     * 检查报表导出
     */
    public void checkRptExportTask(Integer selectedYear, Integer selectedMonth , Long checkerId,User user) {

        RPTCustomerOrderPlanDailySearch searchCondition = setSearchCondition(selectedYear,selectedMonth,checkerId);
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        fiReportExportTaskService.checkRptExportTask(RPTReportEnum.FI_RPT_ORDER_AUDIT_ABNORMAL, user, searchConditionJson);
    }

    /**
     *创建报表导出任务
     */
    public void createRptExportTask(Integer selectedYear, Integer selectedMonth , Long checkerId,User user) {

        RPTCustomerOrderPlanDailySearch searchCondition = setSearchCondition(selectedYear,selectedMonth,checkerId);
        String reportTitle = "每日审单数量单"+selectedYear+"年"+selectedMonth+"月";
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        fiReportExportTaskService.createRptExportTask(RPTReportEnum.FI_RPT_ORDER_AUDIT_ABNORMAL, RPTReportTypeEnum.ORDER_REPORT, user, reportTitle, searchConditionJson);
    }


    /**
     *设置筛选项的值
     */
    public RPTCustomerOrderPlanDailySearch setSearchCondition(Integer selectedYear, Integer selectedMonth , Long checkerId){

        RPTCustomerOrderPlanDailySearch searchCondition = new RPTCustomerOrderPlanDailySearch();
        Date queryDate = DateUtils.getDate(selectedYear, selectedMonth, 1);
        Date startDate = DateUtils.getStartDayOfMonth(queryDate);
        Date endDate = DateUtils.getLastDayOfMonth(queryDate);
        searchCondition.setEndDate(endDate.getTime());
        searchCondition.setStartDate(startDate.getTime());
        searchCondition.setId(checkerId);

        return searchCondition;
    }
}
