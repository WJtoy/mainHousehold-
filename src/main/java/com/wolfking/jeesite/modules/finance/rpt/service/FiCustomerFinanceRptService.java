package com.wolfking.jeesite.modules.finance.rpt.service;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTCustomerFinanceEntity;
import com.kkl.kklplus.entity.rpt.common.RPTReportEnum;
import com.kkl.kklplus.entity.rpt.common.RPTReportTypeEnum;
import com.kkl.kklplus.entity.rpt.search.RPTCustomerFinanceSearch;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.utils.CustomerUtils;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.common.config.MicroServicesProperties;
import com.wolfking.jeesite.ms.providerrpt.feign.MSCustomerFinanceRptFeign;
import com.wolfking.jeesite.ms.providerrpt.service.ReportExportTaskService;
import com.wolfking.jeesite.ms.providerrpt.utils.RedisGsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Service
public class FiCustomerFinanceRptService {
    @Autowired
    private MSCustomerFinanceRptFeign msCustomerFinanceRptFeign;

    @Autowired
    private MicroServicesProperties microServicesProperties;

    @Autowired
    private FiReportExportTaskService fiReportExportTaskService;

    /**
     * 从rpt微服务中获取数据
     */
    public Page<RPTCustomerFinanceEntity> getCompletedOrderList(Page<RPTCustomerFinanceEntity> page, Long customerId,Long salesId ,Long merchandiserId,Integer paymentType, String code) {
        Page<RPTCustomerFinanceEntity> returnPage = new Page<>(page.getPageNo(), page.getPageSize());
        RPTCustomerFinanceSearch search = new RPTCustomerFinanceSearch();
        search.setPageNo(page.getPageNo());
        search.setPageSize(page.getPageSize());
        search.setCustomerId(customerId);
        search.setCode(code);
        search.setPaymentType(paymentType);
        search.setSalesId(salesId);
        search.setMerchandiserId(merchandiserId);
        if (microServicesProperties.getReport().getEnabled()) {
            MSResponse<MSPage<RPTCustomerFinanceEntity>> msResponse = msCustomerFinanceRptFeign.getCustomerFinanceRptList(search);
            if (MSResponse.isSuccess(msResponse)) {
                MSPage<RPTCustomerFinanceEntity> data = msResponse.getData();
                returnPage.setCount(data.getRowCount());
                returnPage.setList(data.getList());
            }
        }
        return returnPage;
    }


    /**
     * 检查报表导出
     */
    public void checkRptExportTask(Long customerId,Long salesId, Long merchandiserId,Integer paymentType ,String code, User user) {

        RPTCustomerFinanceSearch searchCondition = setSearchCondition(customerId,salesId,merchandiserId,paymentType,code);
        searchCondition.setPageNo(1);
        searchCondition.setPageSize(200000);

        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);

        fiReportExportTaskService.checkRptExportTask(RPTReportEnum.FI_CUSTOMER_FINANCE_RPT, user, searchConditionJson);
    }

    /**
     *创建报表导出任务
     */
    public void createRptExportTask(Long customerId,Long salesId, Long merchandiserId,Integer paymentType ,String code,User user) {

        RPTCustomerFinanceSearch searchCondition = setSearchCondition(customerId,salesId,merchandiserId,paymentType,code);
        searchCondition.setPageNo(1);
        searchCondition.setPageSize(200000);
        Customer customer = CustomerUtils.getCustomer(customerId);
        String customerName = customer == null ? "" : customer.getName();
        String reportTitle = customerName + "客户账户余额";
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        fiReportExportTaskService.createRptExportTask(RPTReportEnum.FI_CUSTOMER_FINANCE_RPT, RPTReportTypeEnum.FINANCE_REPORT, user, reportTitle, searchConditionJson);
    }


    /**
     *设置筛选项的值
     */
    public RPTCustomerFinanceSearch setSearchCondition(Long customerId,Long salesId,Long merchandiserId ,Integer paymentType ,String code){

        RPTCustomerFinanceSearch searchCondition = new RPTCustomerFinanceSearch();
        searchCondition.setCustomerId(customerId);
        searchCondition.setSalesId(salesId);
        searchCondition.setPaymentType(paymentType);
        searchCondition.setCode(code);
        searchCondition.setMerchandiserId(merchandiserId);
        return searchCondition;
    }
}
