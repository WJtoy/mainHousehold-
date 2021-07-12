package com.wolfking.jeesite.ms.providerrpt.service;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTCancelledOrderEntity;
import com.kkl.kklplus.entity.rpt.common.RPTReportEnum;
import com.kkl.kklplus.entity.rpt.common.RPTReportTypeEnum;
import com.kkl.kklplus.entity.rpt.search.RPTCancelledOrderSearch;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.utils.CustomerUtils;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.common.config.MicroServicesProperties;
import com.wolfking.jeesite.ms.providerrpt.feign.MSCancelledOrderRptFeign;
import com.wolfking.jeesite.ms.providerrpt.utils.RedisGsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.wolfking.jeesite.common.persistence.Page;

import java.util.Date;
import java.util.List;


@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Service
public class MSCancelledOrderRptService {

    @Autowired
    private MSCancelledOrderRptFeign msCancelledOrderRptFeign;

    @Autowired
    private MicroServicesProperties microServicesProperties;

    @Autowired
    private ReportExportTaskService reportExportTaskService;

    /**
     * 从rpt微服务中获取订单退单明细数据
     */
    public Page<RPTCancelledOrderEntity> getCancelledOrderList(Page<RPTCancelledOrderEntity> page, Date cancelBeginDate, Date cancelEndDate,
                                                                 Long customerId, Integer paymentType, Integer cancelResponsible, Date createBeginDate, Date createEndDate,List<Long> productCategoryIds) {
        Page<RPTCancelledOrderEntity> returnPage = new Page<>(page.getPageNo(), page.getPageSize());
        if (cancelBeginDate != null && cancelEndDate != null) {
            RPTCancelledOrderSearch search = new RPTCancelledOrderSearch();
            search.setPageNo(page.getPageNo());
            search.setPageSize(page.getPageSize());
            search.setCustomerId(customerId);
            search.setBeginCloseDate(cancelBeginDate.getTime());
            search.setEndCloseDate(cancelEndDate.getTime());
            if(createBeginDate != null){
                search.setBeginCreateDate(createBeginDate.getTime());
            }
            if(createEndDate != null){
                search.setEndCreateDate(createEndDate.getTime());
            }
            search.setPaymentType(paymentType);
            search.setCancelResponsible(cancelResponsible);
            search.setProductCategoryIds(productCategoryIds);
            if (microServicesProperties.getReport().getEnabled()) {
                MSResponse<MSPage<RPTCancelledOrderEntity>> msResponse = msCancelledOrderRptFeign.getCancelledOrderList(search);
                if (MSResponse.isSuccess(msResponse)) {
                    MSPage<RPTCancelledOrderEntity> data = msResponse.getData();
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
    public void checkRptExportTask(Date cancelBeginDate, Date cancelEndDate,
                                   Long customerId, Integer paymentType, Integer cancelResponsible, Date createBeginDate, Date createEndDate,List<Long> productCategoryIds,User user) {

        RPTCancelledOrderSearch searchCondition = setSearchCondition(cancelBeginDate,cancelEndDate,customerId,paymentType,cancelResponsible,createBeginDate,createEndDate,productCategoryIds);
        searchCondition.setPageNo(1);
        searchCondition.setPageSize(200000);
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);

        reportExportTaskService.checkRptExportTask(RPTReportEnum.CANCELLED_ORDER_RPT, user, searchConditionJson);
    }

    /**
     *创建报表导出任务
     */
    public void createRptExportTask(Date cancelBeginDate, Date cancelEndDate,
                                    Long customerId, Integer paymentType, Integer cancelResponsible, Date createBeginDate, Date createEndDate,List<Long> productCategoryIds,User user) {

        RPTCancelledOrderSearch searchCondition = setSearchCondition(cancelBeginDate,cancelEndDate,customerId,paymentType,cancelResponsible,createBeginDate,createEndDate,productCategoryIds);
        Customer customer = CustomerUtils.getCustomer(customerId);
        String customerName = customer == null ? "" : customer.getName();
        searchCondition.setPageNo(1);
        searchCondition.setPageSize(200000);
        String reportTitle = customerName + DateUtils.formatDate(cancelBeginDate, "yyyy年MM月dd日") + "~" + DateUtils.formatDate(cancelEndDate, "yyyy年MM月dd日")+ "订单退单明细表";
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        reportExportTaskService.createRptExportTask(RPTReportEnum.CANCELLED_ORDER_RPT, RPTReportTypeEnum.ORDER_REPORT, user, reportTitle, searchConditionJson);
    }


    /**
     *设置筛选项的值
     */
    public RPTCancelledOrderSearch setSearchCondition(Date cancelBeginDate, Date cancelEndDate,
                                                      Long customerId, Integer paymentType, Integer cancelResponsible, Date createBeginDate, Date createEndDate,List<Long> productCategoryIds){

        RPTCancelledOrderSearch searchCondition = new RPTCancelledOrderSearch();
        searchCondition.setCustomerId(customerId);
        searchCondition.setBeginCloseDate(cancelBeginDate.getTime());
        searchCondition.setEndCloseDate(cancelEndDate.getTime());
        if(createBeginDate != null){
            searchCondition.setBeginCreateDate(createBeginDate.getTime());
        }
        if(createEndDate != null){
            searchCondition.setEndCreateDate(createEndDate.getTime());
        }
        searchCondition.setPaymentType(paymentType);
        searchCondition.setCancelResponsible(cancelResponsible);
        searchCondition.setProductCategoryIds(productCategoryIds);
        return searchCondition;
    }
}
