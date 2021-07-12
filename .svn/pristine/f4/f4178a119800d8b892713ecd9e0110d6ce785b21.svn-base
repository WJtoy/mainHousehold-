package com.wolfking.jeesite.ms.providerrpt.service;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTComplainStatisticsDailyEntity;
import com.kkl.kklplus.entity.rpt.common.RPTReportEnum;
import com.kkl.kklplus.entity.rpt.common.RPTReportTypeEnum;
import com.kkl.kklplus.entity.rpt.search.RPTComplainStatisticsDailySearch;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.common.config.MicroServicesProperties;
import com.wolfking.jeesite.ms.providerrpt.feign.MSComplainStatisticsRptFeign;
import com.wolfking.jeesite.ms.providerrpt.utils.RedisGsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Service
public class MSComplainStatisticsDailyRptService {

    @Autowired
    private MSComplainStatisticsRptFeign msComplainStatisticsRptFeign;

    @Autowired
    private MicroServicesProperties microServicesProperties;

    @Autowired
    private ReportExportTaskService reportExportTaskService;

    public List<RPTComplainStatisticsDailyEntity> getComplainStatisticsDaily(Date endDate, Integer areaType, Long areaId, Long customerId, Long salesId, Long keFuId, Long servicePointId, List<Long> productCategoryIds,Integer subFlag) {
        List<RPTComplainStatisticsDailyEntity> list = new ArrayList<>();
        RPTComplainStatisticsDailySearch search = setComplainStaticsSearchCondition(endDate,areaType,areaId,customerId,salesId,keFuId,servicePointId,productCategoryIds,subFlag);
        if (microServicesProperties.getReport().getEnabled()) {
            MSResponse<List<RPTComplainStatisticsDailyEntity>> msResponse = msComplainStatisticsRptFeign.getComplainStatisticsDailyList(search);
            if (MSResponse.isSuccess(msResponse)) {
                list = msResponse.getData();
            }
        }
         return list;
    }

    public Map<String, Object> turnToChartInformationNew(Date endDate, Integer areaType, Long areaId, Long customerId, Long salesId, Long keFuId, Long servicePointId, List<Long> productCategoryIds,Integer subFlag){
        Map<String, Object> map = new HashMap<>();
        RPTComplainStatisticsDailySearch search = setComplainStaticsSearchCondition(endDate,areaType,areaId,customerId,salesId,keFuId,servicePointId,productCategoryIds,subFlag);
        if (microServicesProperties.getReport().getEnabled()) {
            MSResponse<Map<String, Object>> msResponse = msComplainStatisticsRptFeign.getComplainStatisticsDailyChart(search);
            if (MSResponse.isSuccess(msResponse)) {
                map = msResponse.getData();
            }
        }

        return  map;
    }

    /**
     * 检查报表导出
     */
    public void checkRptExportTask(Date endDate, Integer areaType, Long areaId, Long customerId, Long salesId, Long keFuId, Long servicePointId, List<Long> productCategoryIds,User user,Integer subFlag) {

        RPTComplainStatisticsDailySearch searchCondition = setComplainStaticsSearchCondition(endDate,areaType,areaId,customerId,salesId,keFuId,servicePointId,productCategoryIds,subFlag);
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        reportExportTaskService.checkRptExportTask(RPTReportEnum.COMPLAIN_STATISTICS_DAILY_RPT, user, searchConditionJson);
    }

    /**
     * 创建报表导出任务
     */
    public void createRptExportTask(Date endDate, Integer areaType, Long areaId, Long customerId, Long salesId, Long keFuId, Long servicePointId, List<Long> productCategoryIds,User user,Integer subFlag) {
        endDate = DateUtils.getEndOfDay(endDate);
        Date startDate = DateUtils.addMonth(endDate, -1);
        String strEndDate = DateUtils.formatDate(endDate, "yyyy年MM月dd日");
        String strStartDate = DateUtils.formatDate(startDate, "yyyy年MM月dd日");
        RPTComplainStatisticsDailySearch searchCondition = setComplainStaticsSearchCondition(endDate,areaType,areaId,customerId,salesId,keFuId,servicePointId,productCategoryIds,subFlag);
        String reportTitle = strStartDate + "~" + strEndDate + "每日投诉统计";
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        reportExportTaskService.createRptExportTask(RPTReportEnum.COMPLAIN_STATISTICS_DAILY_RPT, RPTReportTypeEnum.ORDER_REPORT, user, reportTitle, searchConditionJson);
    }



    public RPTComplainStatisticsDailySearch setComplainStaticsSearchCondition(Date endDate, Integer areaType, Long areaId, Long customerId, Long salesId, Long keFuId, Long servicePointId, List<Long> productCategoryIds,Integer subFlag){
        RPTComplainStatisticsDailySearch search = new RPTComplainStatisticsDailySearch();
        endDate = DateUtils.getEndOfDay(endDate);
        Date startDate = DateUtils.addDays(endDate, -31);
        String quarter = QuarterUtils.getSeasonQuarter(startDate);
        String endQuarter = QuarterUtils.getSeasonQuarter(endDate);
        if (!quarter.equals(endQuarter)) {
            quarter = null;
        }
        search.setStartDate(startDate.getTime());
        search.setEndDate(endDate.getTime());
        search.setAreaId(areaId);
        search.setAreaType(areaType);
        search.setCustomerId(customerId);
        search.setKeFuId(keFuId);
        search.setSalesId(salesId);
        search.setSubFlag(subFlag);
        search.setServicePointId(servicePointId);
        search.setProductCategoryIds(productCategoryIds);
        search.setQuarter(quarter);
        return search;
    }

}
