package com.wolfking.jeesite.ms.providerrpt.service;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTTravelChargeRankEntity;
import com.kkl.kklplus.entity.rpt.common.RPTReportEnum;
import com.kkl.kklplus.entity.rpt.common.RPTReportTypeEnum;
import com.kkl.kklplus.entity.rpt.search.RPTTravelChargeRankSearchCondition;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.sys.entity.User;

import com.wolfking.jeesite.ms.providerrpt.feign.MSTravelChargeRankRptFeign;
import com.wolfking.jeesite.ms.providerrpt.utils.RedisGsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Service
public class MSTravelChargeRankRptService {
    @Resource
    private MSTravelChargeRankRptFeign customerNewOrderDailyRptFeign;

    @Autowired
    private ReportExportTaskService reportExportTaskService;



    /**
     * 从rpt微服务中远程费用排名信息
     */
    public Page<RPTTravelChargeRankEntity> getTravelChargeRankRptList(Page<RPTTravelChargeRankEntity> page, int selectedYear, int selectedMonth,
                                                                           Integer paymentType, Long areaId,
                                                                           String servicePointNo, String servicePointName, String contactInfo,
                                                                           Integer appFlag, Integer finishQty,
                                                                           List<Long> productCategoryIds) {
        Date queryDate = DateUtils.getDate(selectedYear, selectedMonth, 1);
        Integer yearMonth = Integer.parseInt(DateUtils.getYearMonth(queryDate));
        RPTTravelChargeRankSearchCondition searchCondition = setSearchCondition(yearMonth, paymentType, areaId, servicePointNo, servicePointName, contactInfo, appFlag, finishQty, productCategoryIds);
        searchCondition.setPageNo(page.getPageNo());
        searchCondition.setPageSize(page.getPageSize());
        MSResponse<MSPage<RPTTravelChargeRankEntity>> travelChargeRankList = customerNewOrderDailyRptFeign.getTravelChargeRankList(searchCondition);
        Page<RPTTravelChargeRankEntity> list = new Page<>();
        if (MSResponse.isSuccess(travelChargeRankList)){
            MSPage<RPTTravelChargeRankEntity> data = travelChargeRankList.getData();
            list.setPageNo(data.getPageNo());
            list.setPageSize(data.getPageSize());
            list.setCount(data.getRowCount());
            list.setList(data.getList());
        }
        return list;
    }

    /**
     * 检查报表导出
     */
    public void checkRptExportTask(int selectedYear, int selectedMonth,
                                   Integer paymentType, Long areaId,
                                   String servicePointNo, String servicePointName, String contactInfo,
                                   Integer appFlag, Integer finishQty,
                                   List<Long> productCategoryIds, User user) {
        Date queryDate = DateUtils.getDate(selectedYear, selectedMonth, 1);
        Integer yearMonth = Integer.parseInt(DateUtils.getYearMonth(queryDate));
        RPTTravelChargeRankSearchCondition searchCondition = setSearchCondition(yearMonth, paymentType, areaId, servicePointNo, servicePointName, contactInfo, appFlag, finishQty, productCategoryIds);
        searchCondition.setPageNo(1);
        searchCondition.setPageSize(200000);
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);

        reportExportTaskService.checkRptExportTask(RPTReportEnum.TRAVEL_CHARGE_RANK_RPT, user, searchConditionJson);
    }

    /**
     *创建报表导出任务
     */
    public void createRptExportTask(int selectedYear, int selectedMonth,
                                    Integer paymentType, Long areaId,
                                    String servicePointNo, String servicePointName, String contactInfo,
                                    Integer appFlag, Integer finishQty,
                                    List<Long> productCategoryIds, User user) {

        Date queryDate = DateUtils.getDate(selectedYear, selectedMonth, 1);
        Integer yearMonth = Integer.parseInt(DateUtils.getYearMonth(queryDate));
        RPTTravelChargeRankSearchCondition searchCondition = setSearchCondition(yearMonth, paymentType, areaId, servicePointNo, servicePointName, contactInfo, appFlag, finishQty, productCategoryIds);
        searchCondition.setPageNo(1);
        searchCondition.setPageSize(200000);

        String reportTitle = "远程费用排名报表" + selectedYear + "年" + selectedMonth + "月";
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        reportExportTaskService.createRptExportTask(RPTReportEnum.TRAVEL_CHARGE_RANK_RPT, RPTReportTypeEnum.ORDER_REPORT, user, reportTitle, searchConditionJson);
    }

    /**
     *设置筛选项的值
     */
    public RPTTravelChargeRankSearchCondition setSearchCondition(Integer yearMonth,Integer paymentType, Long areaId,
                                                                 String servicePointNo, String servicePointName, String contactInfo,
                                                                 Integer appFlag, Integer finishQty,
                                                                 List<Long> productCategoryIds){

        RPTTravelChargeRankSearchCondition searchCondition = new RPTTravelChargeRankSearchCondition();
        searchCondition.setPaymentType(paymentType);
        searchCondition.setProductCategoryIds(productCategoryIds);
        searchCondition.setServicePointName(servicePointName);
        searchCondition.setServicePointNo(servicePointNo);
        searchCondition.setAppFlag(appFlag);
        searchCondition.setAreaId(areaId);
        searchCondition.setCompleteQty(finishQty);
        searchCondition.setContactInfo(contactInfo);
        searchCondition.setYearMonth(yearMonth);
        return searchCondition;
    }


}
