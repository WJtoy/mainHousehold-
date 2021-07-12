package com.wolfking.jeesite.ms.providerrpt.service;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTCustomerComplainEntity;
import com.kkl.kklplus.entity.rpt.common.RPTReportEnum;
import com.kkl.kklplus.entity.rpt.common.RPTReportTypeEnum;
import com.kkl.kklplus.entity.rpt.search.RPTCustomerComplainSearch;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.utils.CustomerUtils;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.common.config.MicroServicesProperties;
import com.wolfking.jeesite.ms.providerrpt.feign.MSCustomerComplainRptFeign;
import com.wolfking.jeesite.ms.providerrpt.utils.RedisGsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Service
public class MSCustomerComplainRptService {
    @Autowired
    private MSCustomerComplainRptFeign msCustomerComplainRptFeign;

    @Autowired
    private MicroServicesProperties microServicesProperties;

    @Autowired
    private ReportExportTaskService reportExportTaskService;

    /**
     * 从rpt微服务中获取开发明细报表数据
     */
    public Page<RPTCustomerComplainEntity> getCustomerComplainList(Page<RPTCustomerComplainEntity> page, Date startDate, Date endDate,Long customerId,Long areaId,Long keFuId,Long servicePointId,
                                                                   Integer complainStatus,List<Long> productCategoryIds,Long salesId, Integer subFlag) {
        Page<RPTCustomerComplainEntity> returnPage = new Page<>(page.getPageNo(), page.getPageSize());
        if (startDate != null && endDate != null) {
            RPTCustomerComplainSearch search = new RPTCustomerComplainSearch();
            search.setPageNo(page.getPageNo());
            search.setPageSize(page.getPageSize());
            search.setCustomerId(customerId);
            search.setStartDt(startDate.getTime());
            search.setEndDt(endDate.getTime());
            search.setAreaId(areaId);
            search.setKeFuId(keFuId);
            search.setServicePointId(servicePointId);
            search.setComplainStatus(complainStatus);
            search.setProductCategoryIds(productCategoryIds);
            search.setSalesId(salesId);
            search.setSubFlag(subFlag);

            if (microServicesProperties.getReport().getEnabled()) {
                MSResponse<MSPage<RPTCustomerComplainEntity>> msResponse = msCustomerComplainRptFeign.getCustomerComplainList(search);
                if (MSResponse.isSuccess(msResponse)) {
                    MSPage<RPTCustomerComplainEntity> data = msResponse.getData();
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
    public void checkRptExportTask(Date startDate, Date endDate, Long customerId, Long areaId, Long keFuId, Long servicePointId, Integer complainStatus, List<Long> productCategoryIds,User user) {

        RPTCustomerComplainSearch searchCondition = setSearchCondition(startDate,endDate,customerId,areaId,keFuId,servicePointId,complainStatus,productCategoryIds);
        searchCondition.setPageNo(1);
        searchCondition.setPageSize(200000);
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);

        reportExportTaskService.checkRptExportTask(RPTReportEnum.CUSTOMER_COMPLAIN_RPT, user, searchConditionJson);
    }

    /**
     *创建报表导出任务
     */
    public void createRptExportTask(Date startDate, Date endDate,Long customerId,Long areaId,Long keFuId,Long servicePointId,Integer complainStatus,List<Long> productCategoryIds,User user) {

        RPTCustomerComplainSearch searchCondition = setSearchCondition(startDate,endDate,customerId,areaId,keFuId,servicePointId,complainStatus,productCategoryIds);
        Customer customer = CustomerUtils.getCustomer(customerId);
        String customerName = customer == null ? "" : customer.getName();
        searchCondition.setPageNo(1);
        searchCondition.setPageSize(200000);
        String beginDate = DateUtils.formatDate(startDate, "yyyy年MM月dd日");
        String strEndDate = DateUtils.formatDate(endDate, "yyyy年MM月dd日");
        String reportTitle = customerName + beginDate  + "~" +  strEndDate + "客户投诉表";
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        reportExportTaskService.createRptExportTask(RPTReportEnum.CUSTOMER_COMPLAIN_RPT, RPTReportTypeEnum.OTHER_REPORT, user, reportTitle, searchConditionJson);
    }


    /**
     *设置筛选项的值
     */
    public RPTCustomerComplainSearch setSearchCondition(Date startDate, Date endDate,Long customerId,Long areaId,Long keFuId,Long servicePointId,Integer complainStatus,List<Long> productCategoryIds){
        RPTCustomerComplainSearch search = new RPTCustomerComplainSearch();
        search.setCustomerId(customerId);
        search.setStartDt(startDate.getTime());
        search.setEndDt(endDate.getTime());
        search.setAreaId(areaId);
        search.setKeFuId(keFuId);
        search.setServicePointId(servicePointId);
        search.setComplainStatus(complainStatus);
        search.setProductCategoryIds(productCategoryIds);
        return search;
    }
}
