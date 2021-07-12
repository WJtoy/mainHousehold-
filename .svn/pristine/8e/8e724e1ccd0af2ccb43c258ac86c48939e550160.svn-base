package com.wolfking.jeesite.ms.providerrpt.service;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTServicePointPaySummaryEntity;
import com.kkl.kklplus.entity.rpt.common.RPTReportEnum;
import com.kkl.kklplus.entity.rpt.common.RPTReportTypeEnum;
import com.kkl.kklplus.entity.rpt.search.RPTServicePointPaySummarySearch;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.md.service.ServicePointService;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.common.config.MicroServicesProperties;
import com.wolfking.jeesite.ms.providerrpt.feign.MSServicePointChargeRptFeign;
import com.wolfking.jeesite.ms.providerrpt.utils.RedisGsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Service
public class MSServicePointChargeRptService {
    @Autowired
    private MSServicePointChargeRptFeign msServicePointChargeRptFeign;

    @Autowired
    private MicroServicesProperties microServicesProperties;

    @Autowired
    private ServicePointService servicePointService;

    @Autowired
    private ReportExportTaskService reportExportTaskService;

    /**
     * 从rpt微服务中获取网点应付汇总数据
     */
    public Page<RPTServicePointPaySummaryEntity> getServicePointPaySummaryRptList(Page<RPTServicePointPaySummaryEntity> page, Integer selectedYear, Integer selectedMonth, Integer paymentType, Long servicePointId, List<Long> productCategoryIds) {
        Page<RPTServicePointPaySummaryEntity> returnPage = new Page<>(page.getPageNo(), page.getPageSize());

        RPTServicePointPaySummarySearch search = new RPTServicePointPaySummarySearch();
        search.setPageNo(page.getPageNo());
        search.setPageSize(page.getPageSize());
        search.setServicePointId(servicePointId);
        search.setSelectedYear(selectedYear);
        search.setSelectedMonth(selectedMonth);
        search.setPaymentType(paymentType);
        search.setProductCategoryIds(productCategoryIds);
        if (microServicesProperties.getReport().getEnabled()) {
            MSResponse<MSPage<RPTServicePointPaySummaryEntity>> msResponse = msServicePointChargeRptFeign.getServicePointPaySummaryRptList(search);
            if (MSResponse.isSuccess(msResponse)) {
                MSPage<RPTServicePointPaySummaryEntity> data = msResponse.getData();
                returnPage.setCount(data.getRowCount());
                returnPage.setList(data.getList());
            }
        }

        return returnPage;
    }

    /**
     * 从rpt微服务中获取网点成本排名数据
     */
    public Page<RPTServicePointPaySummaryEntity> getServicePointCostPerRptList(Page<RPTServicePointPaySummaryEntity> page, Integer selectedYear, Integer selectedMonth, Integer areaType, Long areaId, Integer paymentType, Long servicePointId, Integer appFlag, Integer completeQty, List<Long> productCategoryIds) {
        Page<RPTServicePointPaySummaryEntity> returnPage = new Page<>(page.getPageNo(), page.getPageSize());

        RPTServicePointPaySummarySearch search = new RPTServicePointPaySummarySearch();
        search.setPageNo(page.getPageNo());
        search.setPageSize(page.getPageSize());
        search.setServicePointId(servicePointId);
        search.setSelectedYear(selectedYear);
        search.setSelectedMonth(selectedMonth);
        search.setPaymentType(paymentType);
        search.setAppFlag(appFlag);
        search.setCompleteQty(completeQty);
        search.setAreaType(areaType);
        search.setAreaId(areaId);
        search.setProductCategoryIds(productCategoryIds);
        if (microServicesProperties.getReport().getEnabled()) {
            MSResponse<MSPage<RPTServicePointPaySummaryEntity>> msResponse = msServicePointChargeRptFeign.getServicePointCostPerRptList(search);
            if (MSResponse.isSuccess(msResponse)) {
                MSPage<RPTServicePointPaySummaryEntity> data = msResponse.getData();
                returnPage.setCount(data.getRowCount());
                returnPage.setList(data.getList());
            }
        }

        return returnPage;
    }

    /**
     * 检查报表导出
     */
    public void checkPaySummaryRptExportTask(Integer selectedYear, Integer selectedMonth, Integer paymentType, Long servicePointId, List<Long> productCategoryIds, User user) {

        RPTServicePointPaySummarySearch searchCondition = setPaySummarySearchCondition(selectedYear, selectedMonth, paymentType, servicePointId, productCategoryIds);
        searchCondition.setPageNo(1);
        searchCondition.setPageSize(200000);
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);

        reportExportTaskService.checkRptExportTask(RPTReportEnum.SERVICEPOINT_PAY_SUMMARY_RPT, user, searchConditionJson);
    }

    /**
     * 创建报表导出任务
     */
    public void createPaySummaryRptExportTask(Integer selectedYear, Integer selectedMonth, Integer paymentType, Long servicePointId, List<Long> productCategoryIds, User user) {

        RPTServicePointPaySummarySearch searchCondition = setPaySummarySearchCondition(selectedYear, selectedMonth, paymentType, servicePointId, productCategoryIds);
        ServicePoint servicePoint = servicePointService.getFromCache(servicePointId);
        String servicePointName = servicePoint == null ? "" : servicePoint.getName();
        searchCondition.setPageNo(1);
        searchCondition.setPageSize(200000);
        String reportTitle = servicePointName + selectedYear + "年" + selectedMonth + "月" + "网点应付款汇总报表";
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        reportExportTaskService.createRptExportTask(RPTReportEnum.SERVICEPOINT_PAY_SUMMARY_RPT, RPTReportTypeEnum.FINANCE_REPORT, user, reportTitle, searchConditionJson);
    }


    /**
     * 设置筛选项的值
     */
    public RPTServicePointPaySummarySearch setPaySummarySearchCondition(Integer selectedYear, Integer selectedMonth, Integer paymentType, Long servicePointId, List<Long> productCategoryIds) {
        RPTServicePointPaySummarySearch search = new RPTServicePointPaySummarySearch();
        search.setServicePointId(servicePointId);
        search.setSelectedYear(selectedYear);
        search.setSelectedMonth(selectedMonth);
        search.setPaymentType(paymentType);
        search.setProductCategoryIds(productCategoryIds);
        return search;
    }


    /**
     * 检查报表导出
     */
    public void checkCostPerRptExportTask(Integer selectedYear, Integer selectedMonth, Integer areaType, Long areaId, Integer paymentType, Long servicePointId, Integer appFlag, Integer completeQty, List<Long> productCategoryIds, User user) {

        RPTServicePointPaySummarySearch searchCondition = setCostPerSearchCondition(selectedYear, selectedMonth, areaType, areaId, paymentType, servicePointId, appFlag, completeQty, productCategoryIds);
        searchCondition.setPageNo(1);
        searchCondition.setPageSize(200000);
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);

        reportExportTaskService.checkRptExportTask(RPTReportEnum.SERVICEPOINT_COST_PER_RPT, user, searchConditionJson);
    }

    /**
     * 创建报表导出任务
     */
    public void createCostPerRptExportTask(Integer selectedYear, Integer selectedMonth, Integer areaType, Long areaId, Integer paymentType, Long servicePointId, Integer appFlag, Integer completeQty, List<Long> productCategoryIds, User user) {

        RPTServicePointPaySummarySearch searchCondition = setCostPerSearchCondition(selectedYear, selectedMonth, areaType, areaId, paymentType, servicePointId, appFlag, completeQty, productCategoryIds);
        ServicePoint servicePoint = servicePointService.getFromCache(servicePointId);
        String servicePointName = servicePoint == null ? "" : servicePoint.getName();
        String reportTitle = servicePointName + selectedYear + "年" + selectedMonth + "月" + "网点成本排名报表";
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        reportExportTaskService.createRptExportTask(RPTReportEnum.SERVICEPOINT_COST_PER_RPT, RPTReportTypeEnum.FINANCE_REPORT, user, reportTitle, searchConditionJson);
    }


    /**
     * 设置筛选项的值
     */
    public RPTServicePointPaySummarySearch setCostPerSearchCondition(Integer selectedYear, Integer selectedMonth, Integer areaType, Long areaId, Integer paymentType, Long servicePointId, Integer appFlag, Integer completeQty, List<Long> productCategoryIds) {
        RPTServicePointPaySummarySearch search = new RPTServicePointPaySummarySearch();
        search.setServicePointId(servicePointId);
        search.setSelectedYear(selectedYear);
        search.setSelectedMonth(selectedMonth);
        search.setAreaType(areaType);
        search.setAreaId(areaId);
        search.setPaymentType(paymentType);
        search.setAppFlag(appFlag);
        search.setCompleteQty(completeQty);
        search.setProductCategoryIds(productCategoryIds);
        return search;
    }
}
