package com.wolfking.jeesite.ms.providerrpt.service;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTGradeQtyDailyEntity;
import com.kkl.kklplus.entity.rpt.common.RPTReportEnum;
import com.kkl.kklplus.entity.rpt.common.RPTReportTypeEnum;
import com.kkl.kklplus.entity.rpt.search.RPTCustomerOrderPlanDailySearch;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.common.config.MicroServicesProperties;
import com.wolfking.jeesite.ms.providerrpt.feign.MSGradeQtyDailyRptFeign;
import com.wolfking.jeesite.ms.providerrpt.utils.RedisGsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Service
@Slf4j
public class MSGradeQtyDailyRptService {

    @Autowired
    private MicroServicesProperties microServicesProperties;

    @Autowired
    private MSGradeQtyDailyRptFeign msGradeQtyDailyRptFeign;

    @Autowired
    private ReportExportTaskService reportExportTaskService;


    public List<RPTGradeQtyDailyEntity> getGradeQtyRpt(Integer selectedYear, Integer selectedMonth, List<Long> productCategoryIds) {
        List<RPTGradeQtyDailyEntity> list = new ArrayList<>();
        RPTCustomerOrderPlanDailySearch search = setSearchCondition(selectedYear,selectedMonth,productCategoryIds);
        if (microServicesProperties.getReport().getEnabled()) {
            MSResponse<List<RPTGradeQtyDailyEntity>> msResponse = msGradeQtyDailyRptFeign.getGradeQtyDailyList(search);
            if (MSResponse.isSuccess(msResponse)) {
                list = msResponse.getData();
            }
        }
          return list;
    }


    /**
     * 检查报表导出
     */
    public void checkRptExportTask(Integer selectedYear, Integer selectedMonth, List<Long> productCategoryIds,User user) {

        RPTCustomerOrderPlanDailySearch searchCondition = setSearchCondition(selectedYear , selectedMonth, productCategoryIds);
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        reportExportTaskService.checkRptExportTask(RPTReportEnum.GRADE_QTY_DAILY_RPT, user, searchConditionJson);
    }

    /**
     * 创建报表导出任务
     */
    public void createRptExportTask(Integer selectedYear, Integer selectedMonth, List<Long> productCategoryIds, User user) {

        RPTCustomerOrderPlanDailySearch searchCondition = setSearchCondition(selectedYear, selectedMonth, productCategoryIds);
        String reportTitle =  "客评数量统计" + selectedYear + "年" + selectedMonth + "月";
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        reportExportTaskService.createRptExportTask(RPTReportEnum.GRADE_QTY_DAILY_RPT, RPTReportTypeEnum.ORDER_REPORT, user, reportTitle, searchConditionJson);
    }

    /**
     * 设置筛选项的值
     */
    public RPTCustomerOrderPlanDailySearch setSearchCondition(Integer selectedYear , Integer selectedMonth, List<Long> productCategoryIds) {
        RPTCustomerOrderPlanDailySearch search = new RPTCustomerOrderPlanDailySearch();
        Date queryDate = DateUtils.getDate(selectedYear, selectedMonth, 1);
        Date startDate = DateUtils.getStartDayOfMonth(queryDate);
        Date endDate = DateUtils.getLastDayOfMonth(queryDate);
        search.setStartDate(startDate.getTime());
        search.setEndDate(endDate.getTime());
        search.setProductCategoryIds(productCategoryIds);

        return  search;
    }

}
