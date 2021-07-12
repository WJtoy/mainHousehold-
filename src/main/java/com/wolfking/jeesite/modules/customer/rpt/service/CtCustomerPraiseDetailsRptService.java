package com.wolfking.jeesite.modules.customer.rpt.service;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTKeFuPraiseDetailsEntity;
import com.kkl.kklplus.entity.rpt.common.RPTReportEnum;
import com.kkl.kklplus.entity.rpt.common.RPTReportTypeEnum;
import com.kkl.kklplus.entity.rpt.search.RPTKeFuCompleteTimeSearch;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.utils.CustomerUtils;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.common.config.MicroServicesProperties;
import com.wolfking.jeesite.ms.providerrpt.customer.feign.CtCustomerPraiseDetailsRptFeign;
import com.wolfking.jeesite.ms.providerrpt.feign.MSCustomerPraiseDetailsRptFeign;
import com.wolfking.jeesite.ms.providerrpt.service.ReportExportTaskService;
import com.wolfking.jeesite.ms.providerrpt.utils.RedisGsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Service
public class CtCustomerPraiseDetailsRptService {

    @Autowired
    private MicroServicesProperties microServicesProperties;

    @Autowired
    private CtReportExportTaskService ctReportExportTaskService;

    @Autowired
    private CtCustomerPraiseDetailsRptFeign ctCustomerPraiseDetailsRptFeign;


    public Page<RPTKeFuPraiseDetailsEntity> getPraiseOrderList(Page<RPTKeFuPraiseDetailsEntity> page,Integer status,Long customerId, Long saleId , Date beginDate, Date endDate) {
        Page<RPTKeFuPraiseDetailsEntity> returnPage = new Page<>(page.getPageNo(), page.getPageSize());
        if ( beginDate != null && endDate != null) {
            RPTKeFuCompleteTimeSearch search = new RPTKeFuCompleteTimeSearch();
            search.setPageNo(page.getPageNo());
            search.setPageSize(page.getPageSize());
            search.setCustomerId(customerId);
            search.setBeginDate(beginDate.getTime());
            search.setEndDate(endDate.getTime());
            search.setSalesId(saleId);
            search.setStatus(status);
            if (microServicesProperties.getReport().getEnabled()) {
                MSResponse<MSPage<RPTKeFuPraiseDetailsEntity>> msResponse = ctCustomerPraiseDetailsRptFeign.getCustomerPraiseDetailsList(search);
                if (MSResponse.isSuccess(msResponse)) {
                    MSPage<RPTKeFuPraiseDetailsEntity> data = msResponse.getData();
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
    public void checkRptExportTask(Integer status,Long customerId, Long saleId,Date beginDate, Date endDate, User user) {

        RPTKeFuCompleteTimeSearch searchCondition = setSearchCondition(status,customerId,saleId,beginDate,endDate);
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        ctReportExportTaskService.checkRptExportTask(RPTReportEnum.CT_CUSTOMER_PRAISE_DETAILS_RPT, user, searchConditionJson);
    }

    /**
     *创建报表导出任务
     */
    public void createRptExportTask(Integer status,Long customerId, Long saleId, Date beginDate, Date endDate,User user) {

        RPTKeFuCompleteTimeSearch searchCondition = setSearchCondition(status,customerId,saleId,beginDate,endDate);
        Customer customer = CustomerUtils.getCustomer(customerId);
        String customerName = customer == null ? "" : customer.getName();
        String reportTitle = customerName + DateUtils.formatDate(beginDate, "yyyy年MM月dd日") + "~" + DateUtils.formatDate(endDate, "yyyy年MM月dd日")+ "客户好评明细表";
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        ctReportExportTaskService.createRptExportTask(RPTReportEnum.CT_CUSTOMER_PRAISE_DETAILS_RPT, RPTReportTypeEnum.ORDER_REPORT, user, reportTitle, searchConditionJson);
    }


    /**
     *设置筛选项的值
     */
    public RPTKeFuCompleteTimeSearch setSearchCondition(Integer status,Long customerId, Long saleId,Date beginDate, Date endDate){

        RPTKeFuCompleteTimeSearch search = new RPTKeFuCompleteTimeSearch();
        search.setBeginDate(beginDate.getTime());
        search.setEndDate(endDate.getTime());
        search.setCustomerId(customerId);
        search.setSalesId(saleId);
        search.setStatus(status);
        return search;
    }

}
