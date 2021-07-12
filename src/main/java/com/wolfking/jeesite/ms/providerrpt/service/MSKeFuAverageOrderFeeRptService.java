package com.wolfking.jeesite.ms.providerrpt.service;

import com.google.common.collect.Lists;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTComplainStatisticsDailyEntity;
import com.kkl.kklplus.entity.rpt.RPTCompletedOrderDetailsEntity;
import com.kkl.kklplus.entity.rpt.RPTKeFuAverageOrderFeeEntity;
import com.kkl.kklplus.entity.rpt.common.RPTReportEnum;
import com.kkl.kklplus.entity.rpt.common.RPTReportTypeEnum;
import com.kkl.kklplus.entity.rpt.search.RPTComplainStatisticsDailySearch;
import com.kkl.kklplus.entity.rpt.search.RPTCompletedOrderDetailsSearch;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.ProductCategory;
import com.wolfking.jeesite.modules.md.service.ProductCategoryService;
import com.wolfking.jeesite.modules.md.utils.CustomerUtils;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.common.config.MicroServicesProperties;
import com.wolfking.jeesite.ms.providerrpt.feign.MSCompletedOrderNewRptFeign;
import com.wolfking.jeesite.ms.providerrpt.feign.MSKeFuAverageOrderFeeRptFeign;
import com.wolfking.jeesite.ms.providerrpt.utils.RedisGsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Service
public class MSKeFuAverageOrderFeeRptService {

    @Autowired
    private MSKeFuAverageOrderFeeRptFeign msKeFuAverageOrderFeeRptFeign;

    @Autowired
    private MicroServicesProperties microServicesProperties;

    @Autowired
    private ReportExportTaskService reportExportTaskService;


    /**
     * 从rpt微服务中获取非KA客服特殊费用
     */
    public List<RPTKeFuAverageOrderFeeEntity> getKeFuAverageOrderFeeList(List<Long> productCategoryIds,Long kefuId,Long customerId,Integer areaType, Long areaId,Integer year, Integer month) {
        List<RPTKeFuAverageOrderFeeEntity> list  = Lists.newArrayList();
        RPTComplainStatisticsDailySearch search = new RPTComplainStatisticsDailySearch();
        Date queryDate = DateUtils.getDate(year, month, 1);
        Date startDate = DateUtils.getStartDayOfMonth(queryDate);
        Date endDate = DateUtils.getLastDayOfMonth(queryDate);
        String quarter = QuarterUtils.getSeasonQuarter(queryDate);
        search.setProductCategoryIds(productCategoryIds);
        search.setQuarter(quarter);
        search.setStartDate(startDate.getTime());
        search.setEndDate(endDate.getTime());
        search.setKeFuId(kefuId);
        search.setCustomerId(customerId);
        search.setAreaType(areaType);
        search.setAreaId(areaId);
        if (microServicesProperties.getReport().getEnabled()) {
            MSResponse<List<RPTKeFuAverageOrderFeeEntity>> msResponse = msKeFuAverageOrderFeeRptFeign.getKeFuAverageOrderFeeList(search);
            if (MSResponse.isSuccess(msResponse)) {
                list = msResponse.getData();
            }
        }

        return list;
    }


    /**
     * 从rpt微服务中获取KA客服特殊费用
     */
    public List<RPTKeFuAverageOrderFeeEntity> getVipKeFuAverageOrderFeeList(List<Long> productCategoryIds,Long kefuId,Long customerId,Integer areaType, Long areaId,Integer year, Integer month) {
        List<RPTKeFuAverageOrderFeeEntity> list  = Lists.newArrayList();
        RPTComplainStatisticsDailySearch search = new RPTComplainStatisticsDailySearch();
        Date queryDate = DateUtils.getDate(year, month, 1);
        Date startDate = DateUtils.getStartDayOfMonth(queryDate);
        Date endDate = DateUtils.getLastDayOfMonth(queryDate);
        String quarter = QuarterUtils.getSeasonQuarter(queryDate);
        search.setProductCategoryIds(productCategoryIds);
        search.setQuarter(quarter);
        search.setStartDate(startDate.getTime());
        search.setEndDate(endDate.getTime());
        search.setKeFuId(kefuId);
        search.setCustomerId(customerId);
        search.setAreaType(areaType);
        search.setAreaId(areaId);
        if (microServicesProperties.getReport().getEnabled()) {
            MSResponse<List<RPTKeFuAverageOrderFeeEntity>> msResponse = msKeFuAverageOrderFeeRptFeign.getVipKeFuAverageOrderFeeList(search);
            if (MSResponse.isSuccess(msResponse)) {
                list = msResponse.getData();
            }
        }

        return list;
    }



    /**
     * 检查报表导出
     */
    public void checkRptExportTask(List<Long> productCategoryIds,Long kefuId, Long customerId,Integer areaType, Long areaId,Integer year, Integer month,User user) {

        RPTComplainStatisticsDailySearch searchCondition = setSearchCondition(productCategoryIds,kefuId,customerId,areaType,areaId,year,month);
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        reportExportTaskService.checkRptExportTask(RPTReportEnum.RPT_KEFU_AVERAGE_ORDER_FEE, user, searchConditionJson);
    }

    /**
     *创建报表导出任务
     */
    public void createRptExportTask(List<Long> productCategoryIds,Long kefuId, Long customerId,Integer areaType, Long areaId,Integer year, Integer month,User user) {

        RPTComplainStatisticsDailySearch searchCondition = setSearchCondition(productCategoryIds,kefuId,customerId,areaType,areaId,year,month);
        String reportTitle = year+"年" +month +"月" + "客服均单费用表";
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        reportExportTaskService.createRptExportTask(RPTReportEnum.RPT_KEFU_AVERAGE_ORDER_FEE, RPTReportTypeEnum.ORDER_REPORT, user, reportTitle, searchConditionJson);
    }



    /**
     * 检查报表导出
     */
    public void vipCheckRptExportTask(List<Long> productCategoryIds,Long kefuId,Long customerId,Integer areaType, Long areaId, Integer year, Integer month,User user) {

        RPTComplainStatisticsDailySearch searchCondition = setSearchCondition(productCategoryIds,kefuId,customerId,areaType,areaId,year,month);
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        reportExportTaskService.checkRptExportTask(RPTReportEnum.RPT_KAKEFU_AVERAGE_ORDER_FEE, user, searchConditionJson);
    }

    /**
     *创建报表导出任务
     */
    public void vipCreateRptExportTask(List<Long> productCategoryIds,Long kefuId,Long customerId,Integer areaType, Long areaId, Integer year, Integer month,User user) {

        RPTComplainStatisticsDailySearch searchCondition = setSearchCondition(productCategoryIds,kefuId,customerId,areaType,areaId,year,month);
        String reportTitle = year+"年" +month +"月" + "KA均单费用表";
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        reportExportTaskService.createRptExportTask(RPTReportEnum.RPT_KAKEFU_AVERAGE_ORDER_FEE, RPTReportTypeEnum.ORDER_REPORT, user, reportTitle, searchConditionJson);
    }


    /**
     *设置筛选项的值
     */
    public RPTComplainStatisticsDailySearch setSearchCondition(List<Long> productCategoryIds,Long kefuId, Long customerId,Integer areaType, Long areaId,Integer year, Integer month){

        RPTComplainStatisticsDailySearch search = new RPTComplainStatisticsDailySearch();
        Date queryDate = DateUtils.getDate(year, month, 1);
        Date startDate = DateUtils.getStartDayOfMonth(queryDate);
        Date endDate = DateUtils.getLastDayOfMonth(queryDate);
        String quarter = QuarterUtils.getSeasonQuarter(queryDate);
        search.setProductCategoryIds(productCategoryIds);
        search.setQuarter(quarter);
        search.setStartDate(startDate.getTime());
        search.setEndDate(endDate.getTime());
        search.setKeFuId(kefuId);
        search.setCustomerId(customerId);
        search.setAreaType(areaType);
        search.setAreaId(areaId);
        return search;
    }

}
