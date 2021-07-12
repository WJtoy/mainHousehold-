package com.wolfking.jeesite.ms.providerrpt.service;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTOrderDailyWorkEntity;
import com.kkl.kklplus.entity.rpt.common.RPTReportEnum;
import com.kkl.kklplus.entity.rpt.common.RPTReportTypeEnum;
import com.kkl.kklplus.entity.rpt.search.RPTCompletedOrderDetailsSearch;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.common.config.MicroServicesProperties;
import com.wolfking.jeesite.ms.providerrpt.feign.MSCreatedOrderRptFeign;
import com.wolfking.jeesite.ms.providerrpt.utils.RedisGsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.text.NumberFormat;
import java.util.*;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Service
public class MSCreatedOrderRptService {

    @Autowired
    private MicroServicesProperties microServicesProperties;

    @Autowired
    private MSCreatedOrderRptFeign createdOrderRptFeign;

    @Autowired
    private ReportExportTaskService reportExportTaskService;

    /**
     * 从rpt微服务中获取工单来源统计
     *
     */
    public List<RPTOrderDailyWorkEntity> getCreatedOrderSource(Integer selectedYear, Integer selectedMonth, Long customerId, List<Long> productCategoryIds) {
        List<RPTOrderDailyWorkEntity> returnList = new ArrayList<>();
        if (selectedYear != null && selectedYear > 0 && selectedMonth != null && selectedMonth > 0) {
            Date queryDate = DateUtils.getDate(selectedYear, selectedMonth, 1);
            Date startDate = DateUtils.getStartDayOfMonth(queryDate);
            Date endDate = DateUtils.getLastDayOfMonth(queryDate);
            RPTCompletedOrderDetailsSearch search = new RPTCompletedOrderDetailsSearch();
            search.setBeginDate(startDate.getTime());
            search.setEndDate(endDate.getTime());
            search.setCustomerId(customerId);
            search.setProductCategoryIds(productCategoryIds);
            if (microServicesProperties.getReport().getEnabled()) {
                MSResponse<List<RPTOrderDailyWorkEntity>> msResponse = createdOrderRptFeign.getCreatedOrderList(search);
                if (MSResponse.isSuccess(msResponse)) {
                    returnList = msResponse.getData();
                }
            }
        }
        return returnList;
    }

    /**
     * 检查报表导出
     */
    public void checkRptExportTask(Long servicePointId, List<Long> productCategoryIds, Integer selectedYear, Integer selectedMonth, User user) {

        RPTCompletedOrderDetailsSearch searchCondition = setSearchCondition(servicePointId, productCategoryIds, selectedYear, selectedMonth);
        searchCondition.setPageNo(1);
        searchCondition.setPageSize(200000);
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);

        reportExportTaskService.checkRptExportTask(RPTReportEnum.ORDER_SOURCE_RPT, user, searchConditionJson);
    }

    /**
     * 创建报表导出任务
     */
    public void createRptExportTask(Long customerId, List<Long> productCategoryIds, Integer selectedYear, Integer selectedMonth, User user) {

        RPTCompletedOrderDetailsSearch searchCondition = setSearchCondition(customerId, productCategoryIds, selectedYear, selectedMonth);
        searchCondition.setPageNo(1);
        searchCondition.setPageSize(200000);
        String reportTitle = selectedYear + "年" + selectedMonth + "月" + "每日工单统计";
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        reportExportTaskService.createRptExportTask(RPTReportEnum.ORDER_SOURCE_RPT, RPTReportTypeEnum.FINANCE_REPORT, user, reportTitle, searchConditionJson);
    }


    /**
     * 设置筛选项的值
     */
    public RPTCompletedOrderDetailsSearch setSearchCondition(Long customerId, List<Long> productCategoryIds, Integer selectedYear, Integer selectedMonth) {

        RPTCompletedOrderDetailsSearch searchCondition = new RPTCompletedOrderDetailsSearch();
        Date queryDate = DateUtils.getDate(selectedYear, selectedMonth, 1);
        Date beginDate = DateUtils.getStartOfDay(queryDate);
        Date endDate = DateUtils.getLastDayOfMonth(beginDate);
        searchCondition.setCustomerId(customerId);
        searchCondition.setBeginDate(beginDate.getTime());
        searchCondition.setEndDate(endDate.getTime());
        searchCondition.setProductCategoryIds(productCategoryIds);

        return searchCondition;
    }

    public Map<String, Object> turnToChartInformation(Integer selectedYear, Integer selectedMonth, Long customerId, List<Long> productCategoryIds) {
        Map<String, Object> map = new HashMap<>();
        if (selectedYear != null && selectedYear > 0 && selectedMonth != null && selectedMonth > 0) {
            List<RPTOrderDailyWorkEntity> entityList = getCreatedOrderSource(selectedYear, selectedMonth, customerId, productCategoryIds);
            if (entityList == null || entityList.size() <= 0) {
                return map;
            }
            List<String> createDates = new ArrayList<>();
            List<String> orderCreateDates = new ArrayList<>();
            List<Integer> manualOrders = new ArrayList<>();
            List<Integer> tmOrders = new ArrayList<>();
            List<Integer> pddOrders = new ArrayList<>();
            List<Integer> jdOrders = new ArrayList<>();
            List<Integer> restOrders = new ArrayList<>();
            List<Integer> daySums = new ArrayList<>();
            List<String> manualOrderRates = new ArrayList<>();
            List<String> b2bOrderRates = new ArrayList<>();

            Date queryDate = DateUtils.getDate(selectedYear, selectedMonth, 1);
            int days = DateUtils.getDaysOfMonth(queryDate);
            int day = Integer.valueOf(DateUtils.getDay());
            NumberFormat numberFormat = NumberFormat.getInstance();
            numberFormat.setMaximumFractionDigits(2);
            int tmOrder;
            int jdOrder;
            int pddOrder;
            int restOrder;
            int b2bOrderSum;
            Integer total;
            for (RPTOrderDailyWorkEntity entity : entityList) {
                if (entity.getOrderCreateDate() != null) {
                    createDates.add(entity.getOrderCreateDate().substring(8));
                    orderCreateDates.add(entity.getOrderCreateDate().substring(8));
                    manualOrders.add(entity.getManualOrder());
                    tmOrders.add(entity.getTmOrder());
                    pddOrders.add(entity.getPddOrder());
                    jdOrders.add(entity.getJdOrder());
                    restOrders.add(entity.getRestOrder());
                    daySums.add(entity.getDaySum());
                    manualOrderRates.add(entity.getManualOrderRate());
                    total = entity.getDaySum();
                    if (total != null && total != 0) {
                        tmOrder = entity.getTmOrder();
                        jdOrder = entity.getJdOrder();
                        pddOrder = entity.getPddOrder();
                        restOrder = entity.getRestOrder();

                        b2bOrderSum = tmOrder + jdOrder + restOrder + pddOrder;
                        if (b2bOrderSum > 0) {
                            String b2bOrderRate = numberFormat.format((float) b2bOrderSum / total * 100);
                            b2bOrderRates.add(b2bOrderRate);
                        } else {
                            b2bOrderRates.add("0");
                        }
                    } else {
                        b2bOrderRates.add("0");
                    }
                }
            }
            int year = DateUtils.getYear(new Date());
            int month = DateUtils.getMonth(new Date());
            String orderCreateDate;
            if (selectedYear == year && selectedMonth == month) {
                for (int i = day ; i < days; i++) {
                    orderCreateDate = DateUtils.formatDate(DateUtils.addDays(queryDate, i), "dd");
                    orderCreateDates.add(orderCreateDate);
                }
            }

            map.put("createDates", createDates);
            map.put("daySums", daySums);
            map.put("orderCreateDates", orderCreateDates);
            map.put("manualOrders", manualOrders);
            map.put("manualOrderRates", manualOrderRates);
            map.put("tmOrders", tmOrders);
            map.put("pddOrders",pddOrders);
            map.put("jdOrders", jdOrders);
            map.put("restOrders", restOrders);
            map.put("b2bOrderRates", b2bOrderRates);
        }
        return map;
    }
}

