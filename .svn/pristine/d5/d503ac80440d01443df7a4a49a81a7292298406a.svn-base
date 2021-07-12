package com.wolfking.jeesite.modules.finance.rpt.service;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTServicePointBalanceEntity;
import com.kkl.kklplus.entity.rpt.common.RPTReportEnum;
import com.kkl.kklplus.entity.rpt.common.RPTReportTypeEnum;
import com.kkl.kklplus.entity.rpt.search.RPTServicePointWriteOffSearch;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.common.config.MicroServicesProperties;
import com.wolfking.jeesite.ms.providerrpt.feign.MSServicePointBalanceRptFeign;
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
public class FiServicePointBalanceRptService {

    @Autowired
    private MicroServicesProperties microServicesProperties;

    @Autowired
    private MSServicePointBalanceRptFeign msServicePointBalanceRptFeign;

    @Autowired
    private FiReportExportTaskService fiReportExportTaskService;

    public Page<RPTServicePointBalanceEntity> getServicePointBalanceRpt(Page<RPTServicePointBalanceEntity> page, List<Long> productCategoryIds, Long servicePointId, Integer paymentType, Integer selectedYear, Integer selectedMonth) {
        Page<RPTServicePointBalanceEntity> returnPage = new Page<>(page.getPageNo(), page.getPageSize());
        if (selectedYear != null && selectedYear > 0 && selectedMonth != null && selectedMonth > 0) {
            RPTServicePointWriteOffSearch search = setSearchCondition(servicePointId, paymentType,productCategoryIds,selectedYear,selectedMonth);
            search.setPageNo(page.getPageNo());
            search.setPageSize(page.getPageSize());
            if (microServicesProperties.getReport().getEnabled()) {
                MSResponse<MSPage<RPTServicePointBalanceEntity>> msResponse = msServicePointBalanceRptFeign.getServicePointBalanceByPage(search);
                if (MSResponse.isSuccess(msResponse)) {
                    MSPage<RPTServicePointBalanceEntity> data = msResponse.getData();
                    returnPage.setCount(data.getRowCount());
                    returnPage.setList(data.getList());
                }
            }
        }

        return returnPage;
    }


        /**
         *设置筛选项的值
         */
        public RPTServicePointWriteOffSearch setSearchCondition(Long servicePointId, Integer paymentType,List<Long> productCategoryIds,Integer selectedYear, Integer selectedMonth){

            RPTServicePointWriteOffSearch searchCondition = new RPTServicePointWriteOffSearch();
            Date queryDate = DateUtils.getDate(selectedYear, selectedMonth, 1);
            String quarter = QuarterUtils.getSeasonQuarter(queryDate);
            Date beginDate = DateUtils.getStartOfDay(queryDate);
            Date endDate = DateUtils.addMonth(beginDate, 1);
            searchCondition.setQuarter(quarter);
            if(servicePointId != null){
                searchCondition.setServicePointId(servicePointId);
            }
            searchCondition.setBeginWriteOffCreateDate(beginDate.getTime());
            searchCondition.setEndWriteOffCreateDate(endDate.getTime());
            searchCondition.setProductCategoryIds(productCategoryIds);
            searchCondition.setPaymentType(paymentType);

            return searchCondition;
        }
    /**
     * 检查报表导出
     */
    public void checkRptExportTask(Long servicePointId,Integer paymentType,List<Long> productCategoryIds, Integer selectedYear, Integer selectedMonth, User user) {

        RPTServicePointWriteOffSearch searchCondition = setSearchCondition(servicePointId, paymentType,productCategoryIds,selectedYear, selectedMonth);
        searchCondition.setPageNo(1);
        searchCondition.setPageSize(200000);
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);

        fiReportExportTaskService.checkRptExportTask(RPTReportEnum.FI_SERVICEPOINT_ACCOUNT_BALANCE_RPT, user, searchConditionJson);
    }

    /**
     *创建报表导出任务
     */
    public void createRptExportTask(Long servicePointId, Integer paymentType,List<Long> productCategoryIds,Integer selectedYear, Integer selectedMonth,User user) {

        RPTServicePointWriteOffSearch searchCondition = setSearchCondition(servicePointId, paymentType,productCategoryIds,selectedYear, selectedMonth);
        searchCondition.setPageNo(1);
        searchCondition.setPageSize(200000);
        String reportTitle = "网点余额（" + selectedYear + "年" + selectedMonth + "月）";
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        fiReportExportTaskService.createRptExportTask(RPTReportEnum.FI_SERVICEPOINT_ACCOUNT_BALANCE_RPT, RPTReportTypeEnum.FINANCE_REPORT, user, reportTitle, searchConditionJson);
    }

}
