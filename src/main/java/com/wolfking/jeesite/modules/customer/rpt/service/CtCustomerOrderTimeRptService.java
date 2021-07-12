package com.wolfking.jeesite.modules.customer.rpt.service;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTCustomerOrderTimeEntity;
import com.kkl.kklplus.entity.rpt.common.RPTReportEnum;
import com.kkl.kklplus.entity.rpt.common.RPTReportTypeEnum;
import com.kkl.kklplus.entity.rpt.search.RPTCustomerOrderTimeSearch;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.utils.CustomerUtils;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.common.config.MicroServicesProperties;
import com.wolfking.jeesite.ms.providerrpt.customer.feign.CtCustomerOrderTimeRptFeign;
import com.wolfking.jeesite.ms.providerrpt.feign.MSCustomerOrderTimeRptFeign;
import com.wolfking.jeesite.ms.providerrpt.service.ReportExportTaskService;
import com.wolfking.jeesite.ms.providerrpt.utils.RedisGsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Service
public class CtCustomerOrderTimeRptService {
    @Autowired
    private CtCustomerOrderTimeRptFeign ctCustomerOrderTimeRptFeign;

    @Autowired
    private MicroServicesProperties microServicesProperties;

    @Autowired
    private CtReportExportTaskService ctReportExportTaskService;

    /**
     * 从rpt微服务中获取客服完工时效明细数据
     */
    public List<RPTCustomerOrderTimeEntity> getCustomerOrderTimeList(Date endDate, Integer areaType, Long areaId, Long salesId, Long customerId, Long keFuId, Long servicePointId, Integer orderServiceType, List<Long> productCategoryIds) {

        List<RPTCustomerOrderTimeEntity> list = new ArrayList<>();
        if (endDate != null) {
            RPTCustomerOrderTimeSearch search = new RPTCustomerOrderTimeSearch();
            search.setEndDate(endDate.getTime());
            search.setSalesId(salesId);
            search.setCustomerId(customerId);
            search.setAreaType(areaType);
            search.setAreaId(areaId);
            search.setKeFuId(keFuId);
            search.setOrderServiceType(orderServiceType);
            search.setServicePointId(servicePointId);
            search.setProductCategoryIds(productCategoryIds);
            if (microServicesProperties.getReport().getEnabled()) {
                MSResponse<List<RPTCustomerOrderTimeEntity>> msResponse = ctCustomerOrderTimeRptFeign.getCustomerOrderTimeRptList(search);
                if (MSResponse.isSuccess(msResponse)) {
                    list = msResponse.getData();
                }
            }
        }
        return list;
    }

    /**
     * 从rpt微服务中获取客服完工时效图表数据
     */
    public Map<String, Object> getCustomerOrderTimeChartList(Date endDate, Integer areaType, Long areaId, Long salesId, Long customerId, Long keFuId, Long servicePointId, Integer orderServiceType, List<Long> productCategoryIds) {
        Map<String, Object> map = new HashMap<>();
        if (endDate != null) {
            RPTCustomerOrderTimeSearch search = new RPTCustomerOrderTimeSearch();
            search.setEndDate(endDate.getTime());
            search.setSalesId(salesId);
            search.setCustomerId(customerId);
            search.setAreaType(areaType);
            search.setAreaId(areaId);
            search.setKeFuId(keFuId);
            search.setOrderServiceType(orderServiceType);
            search.setServicePointId(servicePointId);
            search.setProductCategoryIds(productCategoryIds);
            if (microServicesProperties.getReport().getEnabled()) {
                MSResponse<Map<String, Object>> msResponse = ctCustomerOrderTimeRptFeign.getCustomerOrderTimeChartList(search);
                if (MSResponse.isSuccess(msResponse)) {
                    map = msResponse.getData();
                }
            }
        }
        return map;
    }

    /**
     * 检查报表导出
     */
    public void checkRptExportTask(Date endDate, Integer areaType, Long areaId, Long salesId, Long customerId, Long keFuId, Long servicePointId,
                                   Integer orderServiceType, List<Long> productCategoryIds, User user) {

        RPTCustomerOrderTimeSearch searchCondition = setSearchCondition(endDate, areaType, areaId,salesId, customerId, keFuId,servicePointId,orderServiceType,productCategoryIds);
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        ctReportExportTaskService.checkRptExportTask(RPTReportEnum.CT_CUSOTMER_ORDER_TIME_RPT, user, searchConditionJson);
    }

    /**
     * 创建报表导出任务
     */
    public void createRptExportTask(Date endDate, Integer areaType, Long areaId, Long salesId, Long customerId, Long keFuId, Long servicePointId,
                                    Integer orderServiceType, List<Long> productCategoryIds, User user) {

        RPTCustomerOrderTimeSearch searchCondition = setSearchCondition(endDate, areaType, areaId,salesId, customerId, keFuId,servicePointId,orderServiceType,productCategoryIds);

        Date startDate = DateUtils.addDays(endDate, -30);
        Customer customer = CustomerUtils.getCustomer(customerId);
        String customerName = customer == null ? "" : customer.getName();
        String beginDateStr = DateUtils.formatDate(startDate, "yyyy年MM月dd日");
        String endDateStr = DateUtils.formatDate(endDate, "yyyy年MM月dd日");
        String reportTitle = customerName + beginDateStr+ "~" + endDateStr +"客户工单时效报表";
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        ctReportExportTaskService.createRptExportTask(RPTReportEnum.CT_CUSOTMER_ORDER_TIME_RPT, RPTReportTypeEnum.ORDER_REPORT, user, reportTitle, searchConditionJson);
    }


    /**
     * 设置筛选项的值
     */
    public RPTCustomerOrderTimeSearch setSearchCondition(Date endDate, Integer areaType, Long areaId, Long salesId, Long customerId, Long keFuId,
                                                        Long servicePointId, Integer orderServiceType, List<Long> productCategoryIds) {

        RPTCustomerOrderTimeSearch searchCondition = new RPTCustomerOrderTimeSearch();
        searchCondition.setEndDate(endDate.getTime());
        searchCondition.setSalesId(salesId);
        searchCondition.setCustomerId(customerId);
        searchCondition.setAreaType(areaType);
        searchCondition.setAreaId(areaId);
        searchCondition.setKeFuId(keFuId);
        searchCondition.setOrderServiceType(orderServiceType);
        searchCondition.setServicePointId(servicePointId);
        searchCondition.setProductCategoryIds(productCategoryIds);
        return searchCondition;
    }
}
