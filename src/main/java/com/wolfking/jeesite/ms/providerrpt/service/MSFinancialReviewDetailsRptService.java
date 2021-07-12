package com.wolfking.jeesite.ms.providerrpt.service;


import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTFinancialReviewDetailsEntity;
import com.kkl.kklplus.entity.rpt.RPTKeFuPraiseDetailsEntity;
import com.kkl.kklplus.entity.rpt.common.RPTReportEnum;
import com.kkl.kklplus.entity.rpt.common.RPTReportTypeEnum;
import com.kkl.kklplus.entity.rpt.search.RPTCustomerOrderPlanDailySearch;
import com.kkl.kklplus.entity.rpt.search.RPTKeFuCompleteTimeSearch;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.common.config.MicroServicesProperties;
import com.wolfking.jeesite.ms.providerrpt.feign.MSFinancialReviewDetailsRptFeign;
import com.wolfking.jeesite.ms.providerrpt.utils.RedisGsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Service
public class MSFinancialReviewDetailsRptService {

    @Autowired
    private MicroServicesProperties microServicesProperties;

    @Autowired
    private ReportExportTaskService reportExportTaskService;

    @Autowired
    private MSFinancialReviewDetailsRptFeign msFinancialReviewDetailsRptFeign;



    public Page<RPTFinancialReviewDetailsEntity> getFinancialReviewList(Page<RPTFinancialReviewDetailsEntity> page, Date beginDate, Date endDate) {
        Page<RPTFinancialReviewDetailsEntity> returnPage = new Page<>(page.getPageNo(), page.getPageSize());
        if ( beginDate != null && endDate != null) {
            RPTCustomerOrderPlanDailySearch search = new RPTCustomerOrderPlanDailySearch();
            search.setPageNo(page.getPageNo());
            search.setPageSize(page.getPageSize());
            search.setStartDate(beginDate.getTime());
            search.setEndDate(endDate.getTime());
            Date[] dates = OrderUtils.getQuarterDates(beginDate,endDate, 0, 0);
            List<String> quarters = QuarterUtils.getQuarters(dates[0], dates[1]);
            if (quarters != null && quarters.size() > 0) {
                search.setQuarters(quarters);
            }
            if (microServicesProperties.getReport().getEnabled()) {
                MSResponse<MSPage<RPTFinancialReviewDetailsEntity>> msResponse = msFinancialReviewDetailsRptFeign.getFinancialReviewDetailsList(search);
                if (MSResponse.isSuccess(msResponse)) {
                    MSPage<RPTFinancialReviewDetailsEntity> data = msResponse.getData();
                    returnPage.setCount(data.getRowCount());
                    returnPage.setList(data.getList());

                }
            }
        }
        return returnPage;
    }

    /**
     * 检查报表导出
     */
    public void checkRptExportTask(Date beginDate, Date endDate, User user) {

        RPTCustomerOrderPlanDailySearch search = new RPTCustomerOrderPlanDailySearch();
        search.setStartDate(beginDate.getTime());
        search.setEndDate(endDate.getTime());
        Date[] dates = OrderUtils.getQuarterDates(beginDate,endDate, 0, 0);
        List<String> quarters = QuarterUtils.getQuarters(dates[0], dates[1]);
        if (quarters != null && quarters.size() > 0) {
            search.setQuarters(quarters);
        }
        String searchConditionJson = RedisGsonUtils.toJson(search);
        reportExportTaskService.checkRptExportTask(RPTReportEnum.FINANCIAL_REVIEW_DETAILS_RPT, user, searchConditionJson);
    }

    /**
     *创建报表导出任务
     */
    public void createRptExportTask(Date beginDate, Date endDate,User user) {

        RPTCustomerOrderPlanDailySearch search = new RPTCustomerOrderPlanDailySearch();
        search.setStartDate(beginDate.getTime());
        search.setEndDate(endDate.getTime());
        Date[] dates = OrderUtils.getQuarterDates(beginDate,endDate, 0, 0);
        List<String> quarters = QuarterUtils.getQuarters(dates[0], dates[1]);
        if (quarters != null && quarters.size() > 0) {
            search.setQuarters(quarters);
        }
        String reportTitle = DateUtils.formatDate(beginDate, "yyyy年MM月dd日") + "~" + DateUtils.formatDate(endDate, "yyyy年MM月dd日")+ "财务审单明细表";;
        String searchConditionJson = RedisGsonUtils.toJson(search);
        reportExportTaskService.createRptExportTask(RPTReportEnum.FINANCIAL_REVIEW_DETAILS_RPT, RPTReportTypeEnum.ORDER_REPORT, user, reportTitle, searchConditionJson);
    }


}
