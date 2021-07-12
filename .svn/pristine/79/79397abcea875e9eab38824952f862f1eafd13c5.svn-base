package com.wolfking.jeesite.ms.providerrpt.service;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTCustomerReminderEntity;
import com.kkl.kklplus.entity.rpt.common.RPTReportEnum;
import com.kkl.kklplus.entity.rpt.common.RPTReportTypeEnum;
import com.kkl.kklplus.entity.rpt.search.RPTCustomerReminderSearch;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.utils.CustomerUtils;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.common.config.MicroServicesProperties;
import com.wolfking.jeesite.ms.providerrpt.feign.MSCustomerReminderRptFeign;
import com.wolfking.jeesite.ms.providerrpt.utils.RedisGsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Service
public class MSCustomerReminderRptService {
    @Autowired
    private MSCustomerReminderRptFeign msCustomerReminderRptFeign;

    @Autowired
    private MicroServicesProperties microServicesProperties;

    @Autowired
    private ReportExportTaskService reportExportTaskService;

    /**
     * 从rpt微服务中获取客户每日催单明细数据
     */
    public List<RPTCustomerReminderEntity> getCustomerReminderList(Long customerId, Integer selectedYear, Integer selectedMonth,List<Long> productCategoryIds) {
        List<RPTCustomerReminderEntity> returnList = new ArrayList<>();
        if (customerId != null && customerId > 0 && selectedYear != null && selectedYear > 0 && selectedMonth != null && selectedMonth > 0) {
            RPTCustomerReminderSearch search = new RPTCustomerReminderSearch();
            search.setCustomerId(customerId);
            search.setSelectedYear(selectedYear);
            search.setSelectedMonth(selectedMonth);
            search.setProductCategoryIds(productCategoryIds);
            if (microServicesProperties.getReport().getEnabled()) {
                MSResponse<List<RPTCustomerReminderEntity>> msResponse = msCustomerReminderRptFeign.getCustomerReminderList(search);
                if (MSResponse.isSuccess(msResponse)) {
                    returnList = msResponse.getData();
                }
            }
        }
        return returnList;
    }


    /**
     * 检查报表导出
     */
    public void checkRptExportTask(Long customerId, Integer selectedYear, Integer selectedMonth,List<Long> productCategoryIds,User user) {

        RPTCustomerReminderSearch searchCondition = setSearchCondition(customerId, selectedYear,selectedMonth,productCategoryIds);
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        reportExportTaskService.checkRptExportTask(RPTReportEnum.CUSTOMER_REMINDER_RPT, user, searchConditionJson);
    }

    /**
     *创建报表导出任务
     */
    public void createRptExportTask(Long customerId, Integer selectedYear, Integer selectedMonth,List<Long> productCategoryIds,User user) {

        RPTCustomerReminderSearch searchCondition = setSearchCondition(customerId, selectedYear,selectedMonth,productCategoryIds);
        Customer customer = CustomerUtils.getCustomer(customerId);
        String customerName = customer.getName();
        String reportTitle = customerName + selectedYear + "年" + selectedMonth + "月" + "催单明细";
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        reportExportTaskService.createRptExportTask(RPTReportEnum.CUSTOMER_REMINDER_RPT, RPTReportTypeEnum.ORDER_REPORT, user, reportTitle, searchConditionJson);

    }


    /**
     *设置筛选项的值
     */
    public RPTCustomerReminderSearch setSearchCondition(Long customerId, Integer selectedYear, Integer selectedMonth,List<Long> productCategoryIds){

        RPTCustomerReminderSearch searchCondition = new RPTCustomerReminderSearch();
        searchCondition.setCustomerId(customerId);
        searchCondition.setSelectedYear(selectedYear);
        searchCondition.setSelectedMonth(selectedMonth);
        searchCondition.setProductCategoryIds(productCategoryIds);
        return searchCondition;
    }
}
