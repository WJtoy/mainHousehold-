package com.wolfking.jeesite.modules.finance.rpt.service;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.ServicePointChargeRptEntity;
import com.kkl.kklplus.entity.rpt.common.RPTReportEnum;
import com.kkl.kklplus.entity.rpt.common.RPTReportTypeEnum;
import com.kkl.kklplus.entity.rpt.search.RPTServicePointWriteOffSearch;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.common.config.MicroServicesProperties;
import com.wolfking.jeesite.ms.providerrpt.feign.MSPointWriteRptFeign;
import com.wolfking.jeesite.ms.providerrpt.service.ReportExportTaskService;
import com.wolfking.jeesite.ms.providerrpt.utils.RedisGsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;


@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Service
public class FiServciePointWritePptService {

    @Autowired
    private MicroServicesProperties microServicesProperties;

    @Autowired
    private MSPointWriteRptFeign msPointWriteRptFeign;

    @Autowired
    private FiReportExportTaskService fiReportExportTaskService;


    /**
     * 从rpt微服务中获取网点对账信息
     *
     * @param servicePointId
     * @param selectedYear
     * @param selectedMonth
     */
    public Page<ServicePointChargeRptEntity> getNrPointWriteOff(Page<ServicePointChargeRptEntity> page, List<Long> productCategoryIds, Long servicePointId, Integer selectedYear, Integer selectedMonth) {
        Page<ServicePointChargeRptEntity> returnPage = new Page<>(page.getPageNo(), page.getPageSize());
        if (servicePointId != null && servicePointId > 0 && selectedYear != null && selectedYear > 0 && selectedMonth != null && selectedMonth > 0) {
            RPTServicePointWriteOffSearch search = new RPTServicePointWriteOffSearch();
            Date queryDate = DateUtils.getDate(selectedYear, selectedMonth, 1);
            String quarter = QuarterUtils.getSeasonQuarter(queryDate);
            Date beginDate = DateUtils.getStartOfDay(queryDate);
            Date endDate = DateUtils.addMonth(beginDate, 1);
            search.setBeginWriteOffCreateDate(beginDate.getTime());
            search.setEndWriteOffCreateDate(endDate.getTime());
            search.setServicePointId(servicePointId);
            search.setQuarter(quarter);
            search.setPageNo(page.getPageNo());
            search.setPageSize(page.getPageSize());
            search.setProductCategoryIds(productCategoryIds);
            if (microServicesProperties.getReport().getEnabled()) {
                MSResponse<MSPage<ServicePointChargeRptEntity>> msResponse = msPointWriteRptFeign.getNrPointWriteOff(search);
                if (MSResponse.isSuccess(msResponse)) {
                    MSPage<ServicePointChargeRptEntity> data = msResponse.getData();
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
    public void checkRptExportTask(Long servicePointId, List<Long> productCategoryIds, Integer selectedYear, Integer selectedMonth, User user) {

        RPTServicePointWriteOffSearch searchCondition = setSearchCondition(servicePointId,productCategoryIds,selectedYear,selectedMonth);
        searchCondition.setPageNo(1);
        searchCondition.setPageSize(200000);
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);

        fiReportExportTaskService.checkRptExportTask(RPTReportEnum.FI_SERVICEPOINT_CHARGE_RPT, user, searchConditionJson);
    }

    /**
     *创建报表导出任务
     */
    public void createRptExportTask(String ServicePointName, Long servicePointId,List<Long> productCategoryIds,Integer selectedYear, Integer selectedMonth,User user) {

        RPTServicePointWriteOffSearch searchCondition = setSearchCondition(servicePointId,productCategoryIds,selectedYear,selectedMonth);
        searchCondition.setPageNo(1);
        searchCondition.setPageSize(200000);
        String reportTitle = selectedYear + "年" + selectedMonth + "月" + ServicePointName + "网点明细对账报表";
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        fiReportExportTaskService.createRptExportTask(RPTReportEnum.FI_SERVICEPOINT_CHARGE_RPT, RPTReportTypeEnum.FINANCE_REPORT, user, reportTitle, searchConditionJson);
    }


    /**
     *设置筛选项的值
     */
    public RPTServicePointWriteOffSearch setSearchCondition(Long servicePointId,List<Long> productCategoryIds,Integer selectedYear, Integer selectedMonth){

        RPTServicePointWriteOffSearch searchCondition = new RPTServicePointWriteOffSearch();
        Date queryDate = DateUtils.getDate(selectedYear, selectedMonth, 1);
        Date beginDate = DateUtils.getStartOfDay(queryDate);
        Date endDate = DateUtils.addMonth(beginDate, 1);
        searchCondition.setServicePointId(servicePointId);
        searchCondition.setBeginWriteOffCreateDate(beginDate.getTime());
        searchCondition.setEndWriteOffCreateDate(endDate.getTime());
        searchCondition.setProductCategoryIds(productCategoryIds);

        return searchCondition;
    }

}


