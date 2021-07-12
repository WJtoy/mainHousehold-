package com.wolfking.jeesite.ms.providerrpt.service;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTCustomerReceivableSummaryEntity;
import com.kkl.kklplus.entity.rpt.RPTServicePointInvoiceEntity;
import com.kkl.kklplus.entity.rpt.common.RPTReportEnum;
import com.kkl.kklplus.entity.rpt.common.RPTReportTypeEnum;
import com.kkl.kklplus.entity.rpt.search.RPTCustomerOrderPlanDailySearch;
import com.kkl.kklplus.entity.rpt.search.RPTServicePointInvoiceSearch;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.common.config.MicroServicesProperties;
import com.wolfking.jeesite.ms.providerrpt.feign.MSCustomerReceivableSummaryRptFeign;
import com.wolfking.jeesite.ms.providerrpt.feign.MSServicePointInvoiceRptFeign;
import com.wolfking.jeesite.ms.providerrpt.utils.RedisGsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Service
public class MSCustomerReceivableSummaryRptService {

    @Autowired
    private MicroServicesProperties microServicesProperties;

    @Autowired
    private ReportExportTaskService reportExportTaskService;

    @Autowired
    private MSCustomerReceivableSummaryRptFeign msCustomerReceivableSummaryRptFeign;

    public Page<RPTCustomerReceivableSummaryEntity> getCustomerReceivableSummaryRpt(Page<RPTCustomerReceivableSummaryEntity> page, Long customerId, Integer selectedYear,
                                                                              Integer selectedMonth, Integer paymentType, List<Long> productCategoryIds,Long salesId,Integer subFlag) {
        Page<RPTCustomerReceivableSummaryEntity> returnPage = new Page<>(page.getPageNo(), page.getPageSize());
        RPTCustomerOrderPlanDailySearch search = setSearchCondition(customerId,selectedYear,selectedMonth,paymentType,productCategoryIds,salesId,subFlag);
        search.setPageNo(page.getPageNo());
        search.setPageSize(page.getPageSize());
        if (microServicesProperties.getReport().getEnabled()) {
            MSResponse<MSPage<RPTCustomerReceivableSummaryEntity>> msResponse = msCustomerReceivableSummaryRptFeign.getCustomerReceivableSummaryByPage(search);
            if (MSResponse.isSuccess(msResponse)) {
                MSPage<RPTCustomerReceivableSummaryEntity> data = msResponse.getData();
                returnPage.setCount(data.getRowCount());
                returnPage.setList(data.getList());
            }
        }

        return  returnPage;

    }

    public RPTCustomerOrderPlanDailySearch setSearchCondition( Long customerId,Integer selectedYear, Integer selectedMonth,
                                                               Integer paymentType,List<Long> productCategoryIds,Long salesId,Integer subFlag){

        RPTCustomerOrderPlanDailySearch rptSearchCondtion = new RPTCustomerOrderPlanDailySearch();
        Date queryDate = DateUtils.getDate(selectedYear, selectedMonth, 1);
        Date beginDate = DateUtils.getStartOfDay(queryDate);
        rptSearchCondtion.setStartDate(beginDate.getTime());
        rptSearchCondtion.setCustomerId(customerId);
        rptSearchCondtion.setPaymentType(paymentType);
        rptSearchCondtion.setProductCategoryIds(productCategoryIds);
        rptSearchCondtion.setSalesId(salesId);
        rptSearchCondtion.setSubFlag(subFlag);
        return rptSearchCondtion;
    }

    /**
     * 检查报表导出
     */
    public void checkRptExportTask(Long customerId, Integer selectedYear,
                                   Integer selectedMonth, Integer paymentType, List<Long> productCategoryIds, User user,Long salesId,Integer subFlag) {

        RPTCustomerOrderPlanDailySearch searchCondition = setSearchCondition(customerId,selectedYear,selectedMonth,paymentType,productCategoryIds,salesId,subFlag);
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        reportExportTaskService.checkRptExportTask(RPTReportEnum.CUSTOMER_RECEIVABLE_SUMMARY_RPT, user, searchConditionJson);
    }

    /**
     * 创建报表导出任务
     */
    public void createRptExportTask(Long customerId, Integer selectedYear,
                                    Integer selectedMonth, Integer paymentType, List<Long> productCategoryIds,  User user,Long salesId,Integer subFlag) {

        RPTCustomerOrderPlanDailySearch searchCondition = setSearchCondition(customerId,selectedYear,selectedMonth,paymentType,productCategoryIds,salesId,subFlag);
        String reportTitle = "客户订单消费汇总" + selectedYear + "年" + selectedMonth + "月";
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        reportExportTaskService.createRptExportTask(RPTReportEnum.CUSTOMER_RECEIVABLE_SUMMARY_RPT, RPTReportTypeEnum.ORDER_REPORT, user, reportTitle, searchConditionJson);
    }

}
