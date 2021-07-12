package com.wolfking.jeesite.modules.finance.rpt.service;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTServicePointInvoiceEntity;
import com.kkl.kklplus.entity.rpt.common.RPTReportEnum;
import com.kkl.kklplus.entity.rpt.common.RPTReportTypeEnum;
import com.kkl.kklplus.entity.rpt.search.RPTServicePointInvoiceSearch;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.common.config.MicroServicesProperties;
import com.wolfking.jeesite.ms.providerrpt.feign.MSServicePointInvoiceSummaryRptFeign;
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
public class FiServicePointInvoiceSummaryRptService {

    @Autowired
    private MicroServicesProperties microServicesProperties;

    @Autowired
    private FiReportExportTaskService fiReportExportTaskService;

    @Autowired
    private MSServicePointInvoiceSummaryRptFeign msServicePointInvoiceSummaryRptFeign;

    public List<RPTServicePointInvoiceEntity> getServicePointPaymentSummary( Integer paymentType, Integer bank,Integer selectedYear,Integer selectedMonth) {
        List<RPTServicePointInvoiceEntity> list = new ArrayList<>();
        RPTServicePointInvoiceSearch search = new RPTServicePointInvoiceSearch();
        Date queryDate = DateUtils.getDate(selectedYear, selectedMonth, 1);
        String quarter = QuarterUtils.getSeasonQuarter(queryDate);
        Date beginDate = DateUtils.getStartDayOfMonth(queryDate);
        Date endDate = DateUtils.getLastDayOfMonth(queryDate);
        search.setPaymentType(paymentType);
        search.setBank(bank);
        search.setBeginDate(beginDate.getTime());
        search.setEndDate(endDate.getTime());
        search.setQuarter(quarter);
        if (microServicesProperties.getReport().getEnabled()) {
            MSResponse<List<RPTServicePointInvoiceEntity>> msResponse = msServicePointInvoiceSummaryRptFeign.getServicePointPaymentSummary(search);
            if (MSResponse.isSuccess(msResponse)) {
                list = msResponse.getData();
            }
        }

        return  list;

    }


    /**
     * 检查报表导出
     */
    public void checkRptExportTask( Integer paymentType, Integer bank,Integer selectedYear,Integer selectedMonth, User user) {

        RPTServicePointInvoiceSearch searchCondition = setSearch(paymentType,bank,selectedYear,selectedMonth);
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        fiReportExportTaskService.checkRptExportTask(RPTReportEnum.FI_SERVICEPOINT_PAYMENT_SUMMARY_RPT, user, searchConditionJson);
    }

    /**
     * 创建报表导出任务
     */
    public void createRptExportTask( Integer paymentType, Integer bank,Integer selectedYear,Integer selectedMonth, User user) {

        RPTServicePointInvoiceSearch searchCondition = setSearch(paymentType,bank,selectedYear,selectedMonth);
        String reportTitle =  "网点付款汇总报表" + selectedYear + "年" + selectedMonth + "月";
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        fiReportExportTaskService.createRptExportTask(RPTReportEnum.FI_SERVICEPOINT_PAYMENT_SUMMARY_RPT, RPTReportTypeEnum.ORDER_REPORT, user, reportTitle, searchConditionJson);
    }

    public RPTServicePointInvoiceSearch setSearch( Integer paymentType, Integer bank,Integer selectedYear,Integer selectedMonth){
        RPTServicePointInvoiceSearch search = new RPTServicePointInvoiceSearch();
        Date queryDate = DateUtils.getDate(selectedYear, selectedMonth, 1);
        String quarter = QuarterUtils.getSeasonQuarter(queryDate);
        Date beginDate = DateUtils.getStartDayOfMonth(queryDate);
        Date endDate = DateUtils.getLastDayOfMonth(queryDate);
        search.setPaymentType(paymentType);
        search.setBank(bank);
        search.setBeginDate(beginDate.getTime());
        search.setEndDate(endDate.getTime());
        search.setQuarter(quarter);

        return search;


    }

}
