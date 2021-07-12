package com.wolfking.jeesite.ms.providerrpt.service;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTCustomerFrozenDailyEntity;
import com.kkl.kklplus.entity.rpt.RPTSearchCondtion;
import com.kkl.kklplus.entity.rpt.common.RPTReportEnum;
import com.kkl.kklplus.entity.rpt.common.RPTReportTypeEnum;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.common.config.MicroServicesProperties;
import com.wolfking.jeesite.ms.providermd.service.MSCustomerService;
import com.wolfking.jeesite.ms.providerrpt.feign.MSCustomerFrozenDailyRptFeign;
import com.wolfking.jeesite.ms.providerrpt.feign.MSCustomerFrozenMonthRptFeign;
import com.wolfking.jeesite.ms.providerrpt.utils.RedisGsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Service
public class MSCustomerFrozenMonthRptService {


    @Autowired
    private MicroServicesProperties microServicesProperties;

    @Autowired
    private MSCustomerFrozenMonthRptFeign msCustomerFrozenMonthRptFeign;

    @Autowired
    private ReportExportTaskService reportExportTaskService;

    @Autowired
    private MSCustomerService msCustomerService;

    /**
     * 从rpt微服务中获取客户每日下单数据
     */
    public Page<RPTCustomerFrozenDailyEntity> getCustomerFrozenMonthRptList(Page<RPTCustomerFrozenDailyEntity> page, Long customerId, Long saleId,Integer subFlag, Integer selectedYear,Integer selectedMonth) {

        RPTSearchCondtion rptSearchCondtion = new RPTSearchCondtion();
        Date queryDate = DateUtils.getDate(selectedYear, selectedMonth, 1);
        rptSearchCondtion.setBeginDate(queryDate.getTime());
        rptSearchCondtion.setCustomerId(customerId);
        rptSearchCondtion.setSalesId(saleId);
        rptSearchCondtion.setSubFlag(subFlag);
        rptSearchCondtion.setPageNo(page.getPageNo());
        rptSearchCondtion.setPageSize(page.getPageSize());
        Page<RPTCustomerFrozenDailyEntity> customerNewOrderDailyRptEntityPage = new Page<>();
        customerNewOrderDailyRptEntityPage.setPageSize(page.getPageSize());
        customerNewOrderDailyRptEntityPage.setPageNo(page.getPageNo());
        if (microServicesProperties.getReport().getEnabled()) {
            MSResponse<MSPage<RPTCustomerFrozenDailyEntity>> customerNewOrderDailyRptList = msCustomerFrozenMonthRptFeign.getCustomerFrozenMonthRptList(rptSearchCondtion);
            if (MSResponse.isSuccess(customerNewOrderDailyRptList)) {
                MSPage<RPTCustomerFrozenDailyEntity> data = customerNewOrderDailyRptList.getData();
                customerNewOrderDailyRptEntityPage.setCount(data.getRowCount());
                customerNewOrderDailyRptEntityPage.setList(data.getList());

            }
        }
        return customerNewOrderDailyRptEntityPage;
    }

    public void checkRptExportTask(Long customerId, Long saleId,Integer selectedYear,Integer selectedMonth,User user,Integer subFlag) {
        RPTSearchCondtion searchCondition = new RPTSearchCondtion();
        Date queryDate = DateUtils.getDate(selectedYear, selectedMonth, 1);
        searchCondition.setBeginDate(queryDate.getTime());
        searchCondition.setCustomerId(customerId);
        searchCondition.setSalesId(saleId);
        searchCondition.setSubFlag(subFlag);
        searchCondition.setPageNo(1);
        searchCondition.setPageSize(200000);
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        reportExportTaskService.checkRptExportTask(RPTReportEnum.CUSTOMER_FROZEN_MONTH_RPT, user, searchConditionJson);
    }

    public void createRptExportTask(Long customerId, Long saleId, Integer selectedYear,Integer selectedMonth, User user,Integer subFlag) {
        RPTSearchCondtion searchCondition = new RPTSearchCondtion();
        Date queryDate = DateUtils.getDate(selectedYear, selectedMonth, 1);
        searchCondition.setBeginDate(queryDate.getTime());
        searchCondition.setCustomerId(customerId);
        searchCondition.setSalesId(saleId);
        searchCondition.setSubFlag(subFlag);
        Customer customer = msCustomerService.getByIdToCustomer(customerId);
        String customerName = "客户";
        if (customer != null && StringUtils.isNotBlank(customer.getName())) {
            customerName = customer.getName();
        }
        String reportTitle = customerName +selectedYear +"年"+ selectedMonth + "月冻结明细";
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        reportExportTaskService.createRptExportTask(RPTReportEnum.CUSTOMER_FROZEN_MONTH_RPT, RPTReportTypeEnum.ORDER_REPORT, user, reportTitle, searchConditionJson);
    }
}
