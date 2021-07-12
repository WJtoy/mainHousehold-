package com.wolfking.jeesite.ms.providerrpt.service;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTKeFuCompleteTimeEntity;
import com.kkl.kklplus.entity.rpt.common.RPTReportEnum;
import com.kkl.kklplus.entity.rpt.common.RPTReportTypeEnum;
import com.kkl.kklplus.entity.rpt.search.RPTKeFuCompleteTimeSearch;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.utils.CustomerUtils;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.common.config.MicroServicesProperties;
import com.wolfking.jeesite.ms.providerrpt.feign.MSKeFuCompleteTimeNewRptFeign;
import com.wolfking.jeesite.ms.providerrpt.feign.MSKeFuCompleteTimeRptFeign;
import com.wolfking.jeesite.ms.providerrpt.utils.RedisGsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Service
public class MSKeFuCompleteTimeNewRptService {

    @Autowired
    private MSKeFuCompleteTimeNewRptFeign msKeFuCompleteTimeNewRptFeign;

    @Autowired
    private MicroServicesProperties microServicesProperties;

    @Autowired
    private ReportExportTaskService reportExportTaskService;

    /**
     * 从rpt微服务中获取客服完工时效明细数据
     */
    public List<RPTKeFuCompleteTimeEntity> getKeFuCompleteTimeList(Integer subFlag,Date endDate, Integer areaType, Long areaId, Long salesId, Long customerId, Long keFuId, Long servicePointId, Integer orderServiceType, List<Long> productCategoryIds) {

        List<RPTKeFuCompleteTimeEntity> list = new ArrayList<>();
        if (endDate != null) {
            RPTKeFuCompleteTimeSearch search = new RPTKeFuCompleteTimeSearch();
            search.setEndDate(endDate.getTime());
            search.setSalesId(salesId);
            search.setCustomerId(customerId);
            search.setAreaType(areaType);
            search.setAreaId(areaId);
            search.setKeFuId(keFuId);
            search.setOrderServiceType(orderServiceType);
            search.setServicePointId(servicePointId);
            search.setProductCategoryIds(productCategoryIds);
            search.setSubFlag(subFlag);
            if (microServicesProperties.getReport().getEnabled()) {
                MSResponse<List<RPTKeFuCompleteTimeEntity>> msResponse = msKeFuCompleteTimeNewRptFeign.getKeFuCompleteTimeRptList(search);
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
    public Map<String, Object> getKeFuCompleteTimeChartList(Integer subFlag,Date endDate, Integer areaType, Long areaId, Long salesId, Long customerId, Long keFuId, Long servicePointId, Integer orderServiceType, List<Long> productCategoryIds) {
        Map<String, Object> map = new HashMap<>();
        if (endDate != null) {
            RPTKeFuCompleteTimeSearch search = new RPTKeFuCompleteTimeSearch();
            search.setEndDate(endDate.getTime());
            search.setSalesId(salesId);
            search.setCustomerId(customerId);
            search.setAreaType(areaType);
            search.setAreaId(areaId);
            search.setKeFuId(keFuId);
            search.setOrderServiceType(orderServiceType);
            search.setServicePointId(servicePointId);
            search.setProductCategoryIds(productCategoryIds);
            search.setSubFlag(subFlag);
            if (microServicesProperties.getReport().getEnabled()) {
                MSResponse<Map<String, Object>> msResponse = msKeFuCompleteTimeNewRptFeign.getKeFuCompleteTimeChartList(search);
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
    public void checkRptExportTask(Integer subFlag,Date endDate, Integer areaType, Long areaId, Long salesId, Long customerId, Long keFuId, Long servicePointId,
                                   Integer orderServiceType, List<Long> productCategoryIds, User user) {

        RPTKeFuCompleteTimeSearch searchCondition = setSearchCondition(subFlag,endDate, areaType, areaId,salesId, customerId, keFuId,servicePointId,orderServiceType,productCategoryIds);
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        reportExportTaskService.checkRptExportTask(RPTReportEnum.KEFU_COMPLETE_TIME_NEW_RPT, user, searchConditionJson);
    }

    /**
     * 创建报表导出任务
     */
    public void createRptExportTask(Integer subFlag,Date endDate, Integer areaType, Long areaId, Long salesId, Long customerId, Long keFuId, Long servicePointId,
                                    Integer orderServiceType, List<Long> productCategoryIds, User user) {

        RPTKeFuCompleteTimeSearch searchCondition = setSearchCondition(subFlag,endDate, areaType, areaId,salesId, customerId, keFuId,servicePointId,orderServiceType,productCategoryIds);

        Date startDate = DateUtils.addDays(endDate, -30);
        Customer customer = CustomerUtils.getCustomer(customerId);
        String customerName = customer == null ? "" : customer.getName();
        String beginDateStr = DateUtils.formatDate(startDate, "yyyy年MM月dd日");
        String endDateStr = DateUtils.formatDate(endDate, "yyyy年MM月dd日");
        String reportTitle = customerName + beginDateStr+ "~" + endDateStr +"客服完工时效报表";
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        reportExportTaskService.createRptExportTask(RPTReportEnum.KEFU_COMPLETE_TIME_NEW_RPT, RPTReportTypeEnum.ORDER_REPORT, user, reportTitle, searchConditionJson);
    }


    /**
     * 设置筛选项的值
     */
    public RPTKeFuCompleteTimeSearch setSearchCondition(Integer subFlag,Date endDate, Integer areaType, Long areaId, Long salesId, Long customerId, Long keFuId,
                                                        Long servicePointId, Integer orderServiceType, List<Long> productCategoryIds) {

        RPTKeFuCompleteTimeSearch searchCondition = new RPTKeFuCompleteTimeSearch();
        searchCondition.setEndDate(endDate.getTime());
        searchCondition.setSalesId(salesId);
        searchCondition.setCustomerId(customerId);
        searchCondition.setAreaType(areaType);
        searchCondition.setAreaId(areaId);
        searchCondition.setKeFuId(keFuId);
        searchCondition.setOrderServiceType(orderServiceType);
        searchCondition.setServicePointId(servicePointId);
        searchCondition.setProductCategoryIds(productCategoryIds);
        searchCondition.setSubFlag(subFlag);
        return searchCondition;
    }
}
