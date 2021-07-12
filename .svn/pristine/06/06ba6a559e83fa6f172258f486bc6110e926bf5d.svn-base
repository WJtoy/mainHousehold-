package com.wolfking.jeesite.ms.providerrpt.service;


import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTChargeDailyEntity;
import com.kkl.kklplus.entity.rpt.common.RPTReportEnum;
import com.kkl.kklplus.entity.rpt.common.RPTReportTypeEnum;
import com.kkl.kklplus.entity.rpt.search.RPTServicePointWriteOffSearch;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.common.config.MicroServicesProperties;
import com.wolfking.jeesite.ms.providerrpt.feign.MSChargeDailyRptFeign;
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
public class MSChargeDailyRptService {

    @Autowired
    private MicroServicesProperties microServicesProperties;

    @Autowired
    private MSChargeDailyRptFeign msChargeDailyRptFeign;

    @Autowired
    private ReportExportTaskService reportExportTaskService;


    public List<RPTChargeDailyEntity> getManualAutoDayRptData(int selectedYear, int selectedMonth) {
        List<RPTChargeDailyEntity> list = new ArrayList<>();
        RPTServicePointWriteOffSearch searchCondition = new RPTServicePointWriteOffSearch();
        Date queryDate = DateUtils.getDate(selectedYear, selectedMonth, 1);
        Date startDate = DateUtils.getStartDayOfMonth(queryDate);
        Date endDate = DateUtils.getLastDayOfMonth(queryDate);
        searchCondition.setBeginWriteOffCreateDate(startDate.getTime());
        searchCondition.setEndWriteOffCreateDate(endDate.getTime());
        if (microServicesProperties.getReport().getEnabled()) {
            MSResponse<List<RPTChargeDailyEntity>> msResponse = msChargeDailyRptFeign.getChargeDailyList(searchCondition);
            if (MSResponse.isSuccess(msResponse)) {
                list = msResponse.getData();
            }
        }

        return list;

    }

    /**
     * 检查报表导出
     */
    public void checkRptExportTask(Integer selectedYear,Integer selectedMonth, User user) {

        RPTServicePointWriteOffSearch searchCondition = setSearchCondition(selectedYear,selectedMonth);
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        reportExportTaskService.checkRptExportTask(RPTReportEnum.CHARGE_DAILY_RPT, user, searchConditionJson);
    }

    /**
     * 创建报表导出任务
     */
    public void createRptExportTask(Integer selectedYear,Integer selectedMonth, User user) {

        RPTServicePointWriteOffSearch searchCondition = setSearchCondition(selectedYear,selectedMonth);
        String reportTitle =  "每日对账数量单" + selectedYear + "年" + selectedMonth + "月";
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        reportExportTaskService.createRptExportTask(RPTReportEnum.CHARGE_DAILY_RPT, RPTReportTypeEnum.ORDER_REPORT, user, reportTitle, searchConditionJson);
    }

    public RPTServicePointWriteOffSearch setSearchCondition(Integer selectedYear,Integer selectedMonth ){
        RPTServicePointWriteOffSearch searchCondition = new RPTServicePointWriteOffSearch();
        Date queryDate = DateUtils.getDate(selectedYear, selectedMonth, 1);
        Date startDate = DateUtils.getStartDayOfMonth(queryDate);
        Date endDate = DateUtils.getLastDayOfMonth(queryDate);
        searchCondition.setBeginWriteOffCreateDate(startDate.getTime());
        searchCondition.setEndWriteOffCreateDate(endDate.getTime());
        return searchCondition;
    }

}
