package com.wolfking.jeesite.ms.providerrpt.service;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTSMSQtyStatisticsEntity;
import com.kkl.kklplus.entity.rpt.common.RPTReportEnum;
import com.kkl.kklplus.entity.rpt.common.RPTReportTypeEnum;
import com.kkl.kklplus.entity.rpt.search.RPTSMSQtyStatisticsSearch;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.common.config.MicroServicesProperties;
import com.wolfking.jeesite.ms.providerrpt.feign.MSSMSQtyStatisticsRptFeign;
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
public class MSSMSQtyStatisticsRptService {

    @Autowired
    private MSSMSQtyStatisticsRptFeign mssmsQtyStatisticsRptFeign;

    @Autowired
    private MicroServicesProperties microServicesProperties;

    @Autowired
    private ReportExportTaskService reportExportTaskService;

    /**
     * 从rpt微服务中获取数据
     */
    public List<RPTSMSQtyStatisticsEntity> getSMSQtyStatisticsList(Integer selectedYear, Integer selectedMonth) {

        List<RPTSMSQtyStatisticsEntity> list = new ArrayList<>();
        if (selectedYear != null && selectedMonth != null) {
            RPTSMSQtyStatisticsSearch search = new RPTSMSQtyStatisticsSearch();
            search.setSelectedYear(selectedYear);
            search.setSelectedMonth(selectedMonth);
            if (microServicesProperties.getReport().getEnabled()) {
                MSResponse<List<RPTSMSQtyStatisticsEntity>> msResponse = mssmsQtyStatisticsRptFeign.getSMSQtyStatisticsRptList(search);
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
    public Map<String, Object> getSMSQtyStatisticsChartList(Integer selectedYear, Integer selectedMonth) {
        Map<String, Object> map = new HashMap<>();
        if (selectedYear != null && selectedMonth != null) {
            RPTSMSQtyStatisticsSearch search = new RPTSMSQtyStatisticsSearch();
            search.setSelectedYear(selectedYear);
            search.setSelectedMonth(selectedMonth);
            if (microServicesProperties.getReport().getEnabled()) {
                MSResponse<Map<String, Object>> msResponse = mssmsQtyStatisticsRptFeign.getSMSQtyStatisticsChartList(search);
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
    public void checkRptExportTask(Integer selectedYear, Integer selectedMonth, User user) {

        RPTSMSQtyStatisticsSearch searchCondition = setSearchCondition(selectedYear,selectedMonth);
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        reportExportTaskService.checkRptExportTask(RPTReportEnum.SMS_QTY_STATISTICS_RPT, user, searchConditionJson);
    }

    /**
     * 创建报表导出任务
     */
    public void createRptExportTask(Integer selectedYear, Integer selectedMonth, User user) {

        RPTSMSQtyStatisticsSearch searchCondition = setSearchCondition(selectedYear,selectedMonth);
        String reportTitle = selectedYear + "年" + selectedMonth+ "月" + "短信数量统计";
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        reportExportTaskService.createRptExportTask(RPTReportEnum.SMS_QTY_STATISTICS_RPT, RPTReportTypeEnum.OTHER_REPORT, user, reportTitle, searchConditionJson);
    }


    /**
     * 设置筛选项的值
     */
    public RPTSMSQtyStatisticsSearch setSearchCondition(Integer selectedYear, Integer selectedMonth) {

        RPTSMSQtyStatisticsSearch searchCondition = new RPTSMSQtyStatisticsSearch();
        searchCondition.setSelectedYear(selectedYear);
        searchCondition.setSelectedMonth(selectedMonth);
        return searchCondition;
    }
}
