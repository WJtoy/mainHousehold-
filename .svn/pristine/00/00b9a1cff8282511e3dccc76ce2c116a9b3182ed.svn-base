package com.wolfking.jeesite.ms.providerrpt.service;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTRechargeRecordEntity;
import com.kkl.kklplus.entity.rpt.RPTServicePointInvoiceEntity;
import com.kkl.kklplus.entity.rpt.common.RPTReportEnum;
import com.kkl.kklplus.entity.rpt.common.RPTReportTypeEnum;
import com.kkl.kklplus.entity.rpt.search.RPTCustomerOrderPlanDailySearch;
import com.kkl.kklplus.entity.rpt.search.RPTServicePointInvoiceSearch;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.common.config.MicroServicesProperties;
import com.wolfking.jeesite.ms.providerrpt.feign.MSRechargeRecordRptFeign;
import com.wolfking.jeesite.ms.providerrpt.utils.RedisGsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Service
public class MSRechargeRecordRptService {

    @Autowired
    private MicroServicesProperties microServicesProperties;

    @Autowired
    private MSRechargeRecordRptFeign msRechargeRecordRptFeign;

    @Autowired
    private ReportExportTaskService reportExportTaskService;

    public Page<RPTRechargeRecordEntity> getRechargeRecordPage(Page<RPTRechargeRecordEntity> page,Integer actionType, Date beginDate,Date endDate,Long customerId) {
        Page<RPTRechargeRecordEntity> returnPage = new Page<>(page.getPageNo(), page.getPageSize());
        RPTCustomerOrderPlanDailySearch search = new RPTCustomerOrderPlanDailySearch();
        search.setPageNo(page.getPageNo());
        search.setPageSize(page.getPageSize());
        search.setCustomerId(customerId);
        search.setPaymentType(actionType);
        Date[] dates = OrderUtils.getQuarterDates(beginDate,endDate, 0, 0);
        List<String> quarters = QuarterUtils.getQuarters(dates[0], dates[1]);
        if (quarters != null && quarters.size() > 0) {
            search.setQuarters(quarters);
        }
        search.setStartDate(beginDate.getTime());
        search.setEndDate(endDate.getTime());
        if (microServicesProperties.getReport().getEnabled()) {
            MSResponse<MSPage<RPTRechargeRecordEntity>> msResponse = msRechargeRecordRptFeign.getRechargeRecordByPage(search);
            if (MSResponse.isSuccess(msResponse)) {
                MSPage<RPTRechargeRecordEntity> data = msResponse.getData();
                returnPage.setCount(data.getRowCount());
                returnPage.setList(data.getList());
            }
        }

        return  returnPage;

    }

    public RPTCustomerOrderPlanDailySearch setSearch(Long customerId, Date beginDate, Date endDate,Integer actionType){

        RPTCustomerOrderPlanDailySearch search = new RPTCustomerOrderPlanDailySearch();
        Date[] dates = OrderUtils.getQuarterDates(beginDate,endDate, 0, 0);
        List<String> quarters = QuarterUtils.getQuarters(dates[0], dates[1]);
        if (quarters != null && quarters.size() > 0) {
            search.setQuarters(quarters);
        }
        search.setPaymentType(actionType);
        search.setCustomerId(customerId);
        search.setStartDate(beginDate.getTime());
        search.setEndDate(endDate.getTime());
        return  search;

    }

    /**
     * 检查报表导出
     */
    public void checkRptExportTask(Integer actionType, Date beginDate,Date endDate,Long customerId, User user) {

        RPTCustomerOrderPlanDailySearch searchCondition = setSearch(customerId,beginDate,endDate,actionType);
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        reportExportTaskService.checkRptExportTask(RPTReportEnum.RECHARGE_RECORD_RPT, user, searchConditionJson);
    }

    /**
     * 创建报表导出任务
     */
    public void createRptExportTask(Integer actionType, Date beginDate,Date endDate,Long customerId,User user) {

        RPTCustomerOrderPlanDailySearch searchCondition = setSearch(customerId,beginDate,endDate,actionType);
        String reportTitle = "充值明细（" + DateUtils.formatDate(beginDate, "yyyy年MM月dd日") +
                "~" + DateUtils.formatDate(endDate, "yyyy年MM月dd日") + "）";
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        reportExportTaskService.createRptExportTask(RPTReportEnum.RECHARGE_RECORD_RPT, RPTReportTypeEnum.ORDER_REPORT, user, reportTitle, searchConditionJson);
    }

}
