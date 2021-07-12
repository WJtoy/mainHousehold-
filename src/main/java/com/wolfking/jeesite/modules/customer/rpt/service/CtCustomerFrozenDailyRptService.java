package com.wolfking.jeesite.modules.customer.rpt.service;

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
import com.wolfking.jeesite.ms.providerrpt.customer.feign.CtCustomerFrozenDailyRptFeign;
import com.wolfking.jeesite.ms.providerrpt.feign.MSCustomerFrozenDailyRptFeign;
import com.wolfking.jeesite.ms.providerrpt.service.ReportExportTaskService;
import com.wolfking.jeesite.ms.providerrpt.utils.RedisGsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Service
public class CtCustomerFrozenDailyRptService {


    @Autowired
    private MicroServicesProperties microServicesProperties;

    @Autowired
    private CtCustomerFrozenDailyRptFeign ctCustomerFrozenDailyRptFeign;

    @Autowired
    private CtReportExportTaskService ctReportExportTaskService;

    @Autowired
    private MSCustomerService msCustomerService;

    /**
     * 从rpt微服务中获取客户每日下单数据
     */
    public Page<RPTCustomerFrozenDailyEntity> getCustomerFrozenDailyRptList(Page<RPTCustomerFrozenDailyEntity> page, Long customerId, Long saleId, Long queryDate) {

        RPTSearchCondtion rptSearchCondtion = new RPTSearchCondtion();
        rptSearchCondtion.setBeginDate(queryDate);
        rptSearchCondtion.setCustomerId(customerId);
        rptSearchCondtion.setSalesId(saleId);
        rptSearchCondtion.setPageNo(page.getPageNo());
        rptSearchCondtion.setPageSize(page.getPageSize());
        Page<RPTCustomerFrozenDailyEntity> customerNewOrderDailyRptEntityPage = new Page<>();
        customerNewOrderDailyRptEntityPage.setPageSize(page.getPageSize());
        customerNewOrderDailyRptEntityPage.setPageNo(page.getPageNo());
        if (microServicesProperties.getReport().getEnabled()) {
            MSResponse<MSPage<RPTCustomerFrozenDailyEntity>> customerNewOrderDailyRptList = ctCustomerFrozenDailyRptFeign.getCustomerFrozenDailyRptList(rptSearchCondtion);
            if (MSResponse.isSuccess(customerNewOrderDailyRptList)) {
                MSPage<RPTCustomerFrozenDailyEntity> data = customerNewOrderDailyRptList.getData();
                customerNewOrderDailyRptEntityPage.setCount(data.getRowCount());
                customerNewOrderDailyRptEntityPage.setList(data.getList());

            }
        }
        return customerNewOrderDailyRptEntityPage;
    }

    public void checkRptExportTask(Long customerId, Long saleId, Date orderCreateDate, User user) {
        RPTSearchCondtion searchCondition = new RPTSearchCondtion();
        searchCondition.setBeginDate(orderCreateDate.getTime());
        searchCondition.setCustomerId(customerId);
        searchCondition.setSalesId(saleId);
        searchCondition.setPageNo(1);
        searchCondition.setPageSize(200000);
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        ctReportExportTaskService.checkRptExportTask(RPTReportEnum.CT_CUSTOMER_FROZEN_DAILY_RPT, user, searchConditionJson);
    }

    public void createRptExportTask(Long customerId, Long saleId, Date orderCreateDate, User user) {
        RPTSearchCondtion searchCondition = new RPTSearchCondtion();
        searchCondition.setBeginDate(orderCreateDate.getTime());
        searchCondition.setCustomerId(customerId);
        searchCondition.setSalesId(saleId);
        Customer customer = msCustomerService.getByIdToCustomer(customerId);
        String customerName = "客户";
        if (customer != null && StringUtils.isNotBlank(customer.getName())) {
            customerName = customer.getName();
        }
        String reportTitle = customerName + DateUtils.formatDate(orderCreateDate, "yyyy年MM月dd日") + "冻结明细";
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        ctReportExportTaskService.createRptExportTask(RPTReportEnum.CT_CUSTOMER_FROZEN_DAILY_RPT, RPTReportTypeEnum.ORDER_REPORT, user, reportTitle, searchConditionJson);
    }
}
