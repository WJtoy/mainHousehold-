package com.wolfking.jeesite.modules.customer.rpt.service;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTUncompletedOrderEntity;
import com.kkl.kklplus.entity.rpt.common.RPTReportEnum;
import com.kkl.kklplus.entity.rpt.common.RPTReportTypeEnum;
import com.kkl.kklplus.entity.rpt.search.RPTUncompletedOrderSearch;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.utils.CustomerUtils;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.common.config.MicroServicesProperties;
import com.wolfking.jeesite.ms.providerrpt.customer.feign.CtUncompletedOrderRptFeign;
import com.wolfking.jeesite.ms.providerrpt.feign.MSUncompletedOrderRptFeign;
import com.wolfking.jeesite.ms.providerrpt.service.ReportExportTaskService;
import com.wolfking.jeesite.ms.providerrpt.utils.RedisGsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Service
public class CtUncompletedOrderRptService {
    @Autowired
    private CtUncompletedOrderRptFeign ctUncompletedOrderRptFeign;

    @Autowired
    private MicroServicesProperties microServicesProperties;

    @Autowired
    private CtReportExportTaskService ctReportExportTaskService;

    /**
     * 从rpt微服务中获取未完工单明细数据
     */
    public Page<RPTUncompletedOrderEntity> getUnCompletedOrderList(Page<RPTUncompletedOrderEntity> page, Long customerId, Date endDate) {
        Page<RPTUncompletedOrderEntity> returnPage = new Page<>(page.getPageNo(), page.getPageSize());
        if (customerId != null && customerId > 0 && endDate != null) {
            RPTUncompletedOrderSearch search = new RPTUncompletedOrderSearch();
            search.setPageNo(page.getPageNo());
            search.setPageSize(page.getPageSize());
            search.setCustomerId(customerId);
            endDate = DateUtils.getEndOfDay(endDate);
            search.setEndDate(endDate);
            search.setEndDt(endDate.getTime());
            if (microServicesProperties.getReport().getEnabled()) {
                MSResponse<MSPage<RPTUncompletedOrderEntity>> msResponse = ctUncompletedOrderRptFeign.getUnCompletedOrderList(search);
                if (MSResponse.isSuccess(msResponse)) {
                    MSPage<RPTUncompletedOrderEntity> data = msResponse.getData();
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
    public void checkRptExportTask(Long customerId,Date endDate,User user) {

        RPTUncompletedOrderSearch searchCondition = setSearchCondition(customerId, endDate);
        searchCondition.setPageNo(1);
        searchCondition.setPageSize(200000);
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);

        ctReportExportTaskService.checkRptExportTask(RPTReportEnum.CT_UNCOMPLETED_ORDER_RPT, user, searchConditionJson);
    }

    /**
     *创建报表导出任务
     */
    public void createRptExportTask(Long customerId,Date endDate,User user) {

        RPTUncompletedOrderSearch searchCondition = setSearchCondition(customerId, endDate);
        Customer customer = CustomerUtils.getCustomer(customerId);
        String customerName = customer.getName();
        searchCondition.setPageNo(1);
        searchCondition.setPageSize(200000);
        String reportTitle = customerName + "未完工明细表（截止到" + DateUtils.formatDate(endDate, "yyyy年MM月dd日") + "）";
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        ctReportExportTaskService.createRptExportTask(RPTReportEnum.CT_UNCOMPLETED_ORDER_RPT, RPTReportTypeEnum.ORDER_REPORT, user, reportTitle, searchConditionJson);
    }


    /**
     *设置筛选项的值
     */
    public RPTUncompletedOrderSearch setSearchCondition(Long customerId,Date endDate){

        RPTUncompletedOrderSearch searchCondition = new RPTUncompletedOrderSearch();
        searchCondition.setCustomerId(customerId);
        endDate = DateUtils.getEndOfDay(endDate);
        searchCondition.setEndDate(endDate);
        searchCondition.setEndDt(endDate.getTime());

        return searchCondition;
    }
}
