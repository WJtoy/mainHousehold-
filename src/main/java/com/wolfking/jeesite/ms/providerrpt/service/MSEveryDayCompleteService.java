package com.wolfking.jeesite.ms.providerrpt.service;

import com.google.common.collect.Maps;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTAreaOrderPlanDailyEntity;
import com.kkl.kklplus.entity.rpt.RPTEveryDayCompleteEntity;
import com.kkl.kklplus.entity.rpt.RPTEveryDayCompleteSearch;
import com.kkl.kklplus.entity.rpt.common.RPTReportEnum;
import com.kkl.kklplus.entity.rpt.common.RPTReportTypeEnum;
import com.kkl.kklplus.entity.rpt.search.RPTAreaOrderPlanDailySearch;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.utils.CustomerUtils;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.common.config.MicroServicesProperties;

import com.wolfking.jeesite.ms.providerrpt.feign.MSEveryDayCompleteRPTFeign;
import com.wolfking.jeesite.ms.providerrpt.utils.RedisGsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Auther wj
 * @Date 2021/5/27 9:59
 */
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Service
public class MSEveryDayCompleteService {

    @Autowired
    private MicroServicesProperties microServicesProperties;

    @Autowired
    private MSEveryDayCompleteRPTFeign msEveryDayCompleteRPTFeign;

    @Autowired
    private ReportExportTaskService reportExportTaskService;


    public Map<String, List<RPTEveryDayCompleteEntity>> getAreaOrderRateDailyList(Date queryDate, Integer areaType, Long areaId, Long customerId) {
        Map<String, List<RPTEveryDayCompleteEntity >> map = Maps.newHashMap();

            RPTEveryDayCompleteSearch search = new RPTEveryDayCompleteSearch();
            Date startDate = DateUtils.getStartOfDay(queryDate);
            Date endDate = DateUtils.getEndOfDay(queryDate);
            String quarter = QuarterUtils.getSeasonQuarter(queryDate);
            search.setStartDate(startDate.getTime());
            search.setEndDate(endDate.getTime());
            search.setQuarter(quarter);
            if(areaId != null && areaId != 0){
                search.setAreaId(areaId);
            }
            if(customerId != null && customerId !=0){
                search.setCustomerId(customerId);
            }
            search.setAreaType(areaType);
            if (microServicesProperties.getReport().getEnabled()) {
                MSResponse<Map<String,List<RPTEveryDayCompleteEntity>>> msResponse = msEveryDayCompleteRPTFeign.getAreaOrderCompleteRateList(search);
                if (MSResponse.isSuccess(msResponse)) {
                    map = msResponse.getData();
                }
            }

        return map;
    }


    /**
     * 检查报表导出
     */
    public void checkRptExportTask(Date queryDate, Integer areaType, Long areaId, Long customerId,User user) {

        RPTEveryDayCompleteSearch searchCondition = setSearchCondition(queryDate,areaType,areaId,customerId);
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        reportExportTaskService.checkRptExportTask(RPTReportEnum.CUSTOMER_EVERY_DAY_COMPLETE_RPT, user, searchConditionJson);
    }

    /**
     *创建报表导出任务
     */
    public void createRptExportTask(Date queryDate, Integer areaType, Long areaId, Long customerId, User user) {

        RPTEveryDayCompleteSearch  searchCondition = setSearchCondition(queryDate,areaType,areaId,customerId);
        Customer customer = CustomerUtils.getCustomer(customerId);
        String customerName = customer == null ? "" : customer.getName();

        String reportTitle = customerName + "每日完工时效报表" +DateUtils.formatDate(queryDate) ;
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        reportExportTaskService.createRptExportTask(RPTReportEnum.CUSTOMER_EVERY_DAY_COMPLETE_RPT, RPTReportTypeEnum.ORDER_REPORT, user, reportTitle, searchConditionJson);
    }



    /**
     *设置筛选项的值
     */
    public RPTEveryDayCompleteSearch  setSearchCondition(Date queryDate, Integer areaType, Long areaId, Long customerId){

        RPTEveryDayCompleteSearch searchCondition = new RPTEveryDayCompleteSearch();
        Date startDate = DateUtils.getStartOfDay(queryDate);
        Date endDate = DateUtils.getEndOfDay(queryDate);
        String quarter = QuarterUtils.getSeasonQuarter(queryDate);
        searchCondition.setStartDate(startDate.getTime());
        searchCondition.setEndDate(endDate.getTime());
        searchCondition.setQuarter(quarter);

        if(areaId != null && areaId != 0){
            searchCondition.setAreaId(areaId);
        }
        if(customerId != null && customerId !=0){
            searchCondition.setCustomerId(customerId);
        }
        searchCondition.setAreaType(areaType);

        return searchCondition;
    }

}
