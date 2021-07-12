package com.wolfking.jeesite.ms.providerrpt.service;


import com.google.common.collect.Lists;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.*;
import com.kkl.kklplus.entity.rpt.common.RPTReportEnum;
import com.kkl.kklplus.entity.rpt.common.RPTReportTypeEnum;
import com.kkl.kklplus.entity.rpt.search.RPTGradedOrderSearch;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.md.utils.CustomerUtils;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.AreaUtils;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.common.config.MicroServicesProperties;
import com.wolfking.jeesite.ms.providermd.service.MSCustomerService;
import com.wolfking.jeesite.ms.providermd.service.MSServicePointService;
import com.wolfking.jeesite.ms.providerrpt.feign.MSGradedOrderRptFeign;
import com.wolfking.jeesite.ms.providerrpt.utils.RedisGsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;


@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Service
@Slf4j
public class MSGradedOrderRptService {

    @Autowired
    private MSGradedOrderRptFeign msGradedOrderRptFeign;

    @Autowired
    private ReportExportTaskService reportExportTaskService;

    @Autowired
    private MSCustomerService msCustomerService;

    @Autowired
    private MSServicePointService msServicePointService;

    @Autowired
    private MicroServicesProperties microServicesProperties;
    /**
     * 获取客服每日完工数据
     * @param selectYear
     * @param selectMonth
     * @param kefuId
     * @return
     */
    public List<RPTKefuCompletedDailyEntity> getKefuCompletedDailyRptData(Integer subFlag ,Integer selectYear, Integer selectMonth,List<Long> productCategoryIds, Long kefuId){
        RPTGradedOrderSearch rptGradedOrderSearchCondition = setKefuCompletedSearchCondition(subFlag,selectYear, selectMonth,productCategoryIds, kefuId);
        MSResponse<List<RPTKefuCompletedDailyEntity>> kefuCompletedOrderDailyRpt = msGradedOrderRptFeign.getKefuCompletedOrderDailyRpt(rptGradedOrderSearchCondition);
        List<RPTKefuCompletedDailyEntity> list = Lists.newArrayList();
        if (microServicesProperties.getReport().getEnabled()) {
            if (MSResponse.isSuccess(kefuCompletedOrderDailyRpt)) {
                list =  kefuCompletedOrderDailyRpt.getData();
            }
        }
        return list;
    }

    /**
     *获取省完工单数据
     */
    public List<RPTAreaCompletedDailyEntity> getProvinceCompletedRptData(Integer areaType, Long areaId, Integer selectYear, Integer selectMonth, Long customerId, List<Long> productCategoryIds){
        RPTGradedOrderSearch rptGradedOrderSearchCondition = setSearchCondition(areaType, areaId, selectYear, selectMonth, customerId, productCategoryIds);
        MSResponse<List<RPTAreaCompletedDailyEntity>> provinceCompletedOrderRpt = msGradedOrderRptFeign.getProvinceCompletedOrderRpt(rptGradedOrderSearchCondition);
        List<RPTAreaCompletedDailyEntity> list = Lists.newArrayList();
        if (microServicesProperties.getReport().getEnabled()) {
            if (MSResponse.isSuccess(provinceCompletedOrderRpt)) {
                list =  provinceCompletedOrderRpt.getData();
            }
        }
        return list;
    }
   //设置查询条件
    public RPTGradedOrderSearch setSearchCondition(Integer areaType, Long areaId, Integer selectYear, Integer selectMonth, Long customerId, List<Long> productCategoryIds){
        RPTGradedOrderSearch rptGradedOrderSearchCondition = new RPTGradedOrderSearch();
        Date queryDate = DateUtils.getDate(selectYear, selectMonth, 1);
        String quarter = DateUtils.getQuarter(queryDate);
        Date beginDate = DateUtils.getStartDayOfMonth(queryDate);
        Date endDate = DateUtils.getLastDayOfMonth(queryDate);
        rptGradedOrderSearchCondition.setCustomerId(customerId);
        rptGradedOrderSearchCondition.setAreaType(areaType);
        rptGradedOrderSearchCondition.setAreaId(areaId);
        rptGradedOrderSearchCondition.setBeginDate(beginDate.getTime());
        rptGradedOrderSearchCondition.setEndDate(endDate.getTime());
        rptGradedOrderSearchCondition.setQuarter(quarter);
        rptGradedOrderSearchCondition.setProductCategoryIds(productCategoryIds);
        return rptGradedOrderSearchCondition;
    }
    public RPTGradedOrderSearch setKefuCompletedSearchCondition(Integer subFlag,Integer selectYear, Integer selectMonth,List<Long> productCategoryIds, Long kefuId){
        RPTGradedOrderSearch rptGradedOrderSearchCondition = new RPTGradedOrderSearch();
        Date queryDate = DateUtils.getDate(selectYear, selectMonth, 1);
        Date beginDate = DateUtils.getStartDayOfMonth(queryDate);
        Date endDate = DateUtils.getLastDayOfMonth(queryDate);
        String quarter = DateUtils.getQuarter(queryDate);
        rptGradedOrderSearchCondition.setBeginDate(beginDate.getTime());
        rptGradedOrderSearchCondition.setEndDate(endDate.getTime());
        rptGradedOrderSearchCondition.setQuarter(quarter);
        rptGradedOrderSearchCondition.setProductCategoryIds(productCategoryIds);
        rptGradedOrderSearchCondition.setKefuId(kefuId);
        rptGradedOrderSearchCondition.setSubFlag(subFlag);

        return rptGradedOrderSearchCondition;
    }

    public RPTGradedOrderSearch setOrderServicePointFeeSearchCondition(Date beginDate, Date endDate, Long servicePointId, List<Long> productCategoryIds){
        RPTGradedOrderSearch rptGradedOrderSearchCondition = new RPTGradedOrderSearch();
        List<String> quarters = QuarterUtils.getQuarters(beginDate, endDate);
        Date dateStart = DateUtils.getDateStart(beginDate);
        Date dateEnd = DateUtils.getDateEnd(endDate);
        rptGradedOrderSearchCondition.setBeginDate(dateStart.getTime());
        rptGradedOrderSearchCondition.setEndDate(dateEnd.getTime());
        rptGradedOrderSearchCondition.setServicePointId(servicePointId);
        rptGradedOrderSearchCondition.setQuarters(quarters);
        rptGradedOrderSearchCondition.setQuarters(Lists.newArrayList(quarters));
        rptGradedOrderSearchCondition.setProductCategoryIds(productCategoryIds);
        return rptGradedOrderSearchCondition;
    }
    /**
     *获取市完工单数据
     */
    public List<RPTAreaCompletedDailyEntity> getCityCompletedRptData(Integer areaType, Long areaId, Integer selectYear, Integer selectMonth, Long customerId, List<Long> productCategoryIds){
        RPTGradedOrderSearch rptGradedOrderSearchCondition = setSearchCondition(areaType, areaId, selectYear, selectMonth, customerId, productCategoryIds);
        MSResponse<List<RPTAreaCompletedDailyEntity>> cityCompletedOrderRpt = msGradedOrderRptFeign.getCityCompletedOrderRpt(rptGradedOrderSearchCondition);
        List<RPTAreaCompletedDailyEntity> list = Lists.newArrayList();
        if (microServicesProperties.getReport().getEnabled()) {
            if (MSResponse.isSuccess(cityCompletedOrderRpt)) {
                list =  cityCompletedOrderRpt.getData();
            }
        }
        return list;
    }

    /**
     *获取区域完工单数据
     */
    public List<RPTAreaCompletedDailyEntity> getAreaCompletedRptData(Integer areaType, Long areaId, Integer selectYear, Integer selectMonth, Long customerId, List<Long> productCategoryIds){
        RPTGradedOrderSearch rptGradedOrderSearchCondition = setSearchCondition(areaType, areaId, selectYear, selectMonth, customerId, productCategoryIds);
        MSResponse<List<RPTAreaCompletedDailyEntity>> areaCompletedOrderRpt = msGradedOrderRptFeign.getAreaCompletedOrderRpt(rptGradedOrderSearchCondition);
        List<RPTAreaCompletedDailyEntity> list = Lists.newArrayList();
        if (microServicesProperties.getReport().getEnabled()) {
            if (MSResponse.isSuccess(areaCompletedOrderRpt)) {
                list =  areaCompletedOrderRpt.getData();
            }
        }
        return list;
    }

    /**
     * 获取工单费用报表数据
     * @param beginDate
     * @param endDate
     * @param servicePointId
     * @param productCategoryIds
     * @return
     */
    public Page<RPTGradedOrderEntity> getOrderServicePointFeeRptData(Page<RPTGradedOrderEntity> page, Date beginDate, Date endDate, Long servicePointId, List<Long> productCategoryIds){
        RPTGradedOrderSearch rptGradedOrderSearchCondition = setOrderServicePointFeeSearchCondition(beginDate, endDate, servicePointId, productCategoryIds);
        rptGradedOrderSearchCondition.setPageSize(page.getPageSize());
        rptGradedOrderSearchCondition.setPageNo(page.getPageNo());
        MSResponse<MSPage<RPTGradedOrderEntity>> orderServicePointFeeRpt = msGradedOrderRptFeign.getOrderServicePointFeeRpt(rptGradedOrderSearchCondition);
        if (microServicesProperties.getReport().getEnabled()) {
            if (MSResponse.isSuccess(orderServicePointFeeRpt)) {
                MSPage<RPTGradedOrderEntity> data = orderServicePointFeeRpt.getData();
                page.setList(data.getList());
                page.setPageNo(data.getPageNo());
                page.setPageSize(data.getPageSize());
                page.setCount(data.getRowCount());
            }
        }
        return page;
    }

    /**
     *
     */
    public List<RPTDevelopAverageOrderFeeEntity> getDevelopAverageFee(Date beginDate,Date endDate, List<Long> productCategoryIds){
        RPTGradedOrderSearch search = new RPTGradedOrderSearch();
        search.setBeginDate(beginDate.getTime());
        search.setEndDate(endDate.getTime());
        search.setQuarter(DateUtils.getQuarter(endDate));
        search.setProductCategoryIds(productCategoryIds);
        MSResponse<List<RPTDevelopAverageOrderFeeEntity>> developAverageFeeRpt = msGradedOrderRptFeign.getDevelopAverageFeeRpt(search);
        List<RPTDevelopAverageOrderFeeEntity> list = Lists.newArrayList();
        if (microServicesProperties.getReport().getEnabled()) {
            if (MSResponse.isSuccess(developAverageFeeRpt)) {
                list =  developAverageFeeRpt.getData();
            }
        }
        return list;
    }

    public Map<String, Object> turnToKeFuOrderCompleteDayChartInformation(Integer subFlag,int selectedYear, int selectedMonth, List<Long> productCategoryIds,Long kefuId) {
        Date queryDate = DateUtils.getDate(selectedYear, selectedMonth, 1);
        int days = DateUtils.getDaysOfMonth(queryDate);
        List<Integer> daySums = new ArrayList<>();

        List<String> createDates = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();

        List<RPTKefuCompletedDailyEntity> copList = getKefuCompletedDailyRptData(subFlag,selectedYear, selectedMonth,productCategoryIds, kefuId);
        if (copList.size()>3) {
            RPTBaseDailyEntity entity = copList.get(copList.size() - 3);
            copList.remove(copList.size() - 1);
            copList.remove(copList.size() - 1);
            copList.remove(copList.size() - 1);

            copList = copList.stream().sorted(Comparator.comparing(RPTKefuCompletedDailyEntity::getTotal).reversed()).collect(Collectors.toList());
            int size = copList.size();
            copList = copList.subList(0, size > 10 ? 10 : size);
            NumberFormat numberFormat = NumberFormat.getInstance();
            numberFormat.setMaximumFractionDigits(2);
            if (entity != null) {
                Class itemClass = entity.getClass();
                for (int i = 0; i < days; i++) {
                    String Date = DateUtils.formatDate(DateUtils.addDays(queryDate, i), "dd");
                    createDates.add(Date);
                    String strGetMethodName = "getD" + (i + 1);
                    try {
                        Method itemGetMethod = itemClass.getMethod(strGetMethodName);
                        Object itemGetD = itemGetMethod.invoke(entity);
                        Double dSum = StringUtils.toDouble(itemGetD);
                        int daySum = (int) Math.floor(dSum);

                        daySums.add(daySum);

                    } catch (Exception ex) {
                        log.error("OrderReportService.turnToKeFuOrderCompleteDayChartInformation", ex);
                    }
                }
            }
        }
        map.put("list",copList);
        map.put("createDates", createDates);
        map.put("daySums", daySums);
        return map;
    }

    public void checkAreaCompletedRptExportTask(Long areaId, Integer areaType, Integer selectYear, Integer selectMonth, Long customerId, List<Long> productCategoryIds, User user) {
        RPTGradedOrderSearch rptGradedOrderSearchCondition = setSearchCondition(areaType, areaId, selectYear, selectMonth, customerId, productCategoryIds);
        String searchConditionJson = RedisGsonUtils.toJson(rptGradedOrderSearchCondition);
        reportExportTaskService.checkRptExportTask(RPTReportEnum.PROVINCE_COMPLETED_ORDER_RPT, user, searchConditionJson);
    }

    public void checkKefuCompletedRptExportTask(Integer subFlag,Integer selectYear, Integer selectMonth, Long kefuId,List<Long> productCategoryIds, User user) {
        RPTGradedOrderSearch rptGradedOrderSearchCondition = setKefuCompletedSearchCondition(subFlag,selectYear, selectMonth,productCategoryIds, kefuId);
        String searchConditionJson = RedisGsonUtils.toJson(rptGradedOrderSearchCondition);
        reportExportTaskService.checkRptExportTask(RPTReportEnum.KEFU_COMPLETED_DAILY_RPT, user, searchConditionJson);
    }

    public void checkOrderServicePointFeeRptExportTask(Date beginDate, Date endDate, Long servicePointId, List<Long> productCategoryIds, User user) {
        RPTGradedOrderSearch rptGradedOrderSearchCondition = setOrderServicePointFeeSearchCondition(beginDate, endDate, servicePointId, productCategoryIds);
        String searchConditionJson = RedisGsonUtils.toJson(rptGradedOrderSearchCondition);
        reportExportTaskService.checkRptExportTask(RPTReportEnum.ORDER_SERVICE_POINT_FEE_RPT, user, searchConditionJson);
    }
    public void checkDevelopAverageRptExportTask(Date beginDate, Date endDate,  List<Long> productCategoryIds, User user) {
        RPTGradedOrderSearch search = new RPTGradedOrderSearch();
        search.setBeginDate(beginDate.getTime());
        search.setEndDate(endDate.getTime());
        search.setQuarter(DateUtils.getQuarter(endDate));
        search.setProductCategoryIds(productCategoryIds);
        String searchConditionJson = RedisGsonUtils.toJson(search);
        reportExportTaskService.checkRptExportTask(RPTReportEnum.DEVELOP_AVERAGE_ORDER_FEE, user, searchConditionJson);
    }
    public void createKefuCompletedRptExportTask(Integer subFlag,Long kefuId, Integer selectYear,Integer selectMonth ,List<Long> productCategoryIds,User user)  {
        RPTGradedOrderSearch rptGradedOrderSearchCondition = setKefuCompletedSearchCondition(subFlag,selectYear, selectMonth, productCategoryIds,kefuId);
        String kefuName = "";
        if (kefuId!=null && kefuId!=0) {
            List<User> kefuList = UserUtils.getKefuList();
            Map<Long, String> kefuMap = kefuList.stream().collect(Collectors.toMap(User::getId, User::getName));
            if (kefuMap!=null && kefuMap.get(kefuId)!= null) {
                kefuName = kefuMap.get(kefuId);
            }
        }
        String reportTitle =   selectYear+"年"+selectMonth + "月" +kefuName+ "客服每日完工";
        String searchConditionJson = RedisGsonUtils.toJson(rptGradedOrderSearchCondition);

        reportExportTaskService.createRptExportTask(RPTReportEnum.KEFU_COMPLETED_DAILY_RPT, RPTReportTypeEnum.ORDER_REPORT, user, reportTitle, searchConditionJson);
    }

    public void createAreaCompletedRptExportTask(Long areaId,Integer areaType , Integer selectYear, Integer selectMonth ,Long customerId,List<Long> productCategoryIds,Integer flag,User user)  {
        RPTGradedOrderSearch rptGradedOrderSearchCondition = setSearchCondition(areaType, areaId, selectYear, selectMonth, customerId, productCategoryIds);
        Map<Long, Area> areaMap = AreaUtils.getAreaMap(Area.TYPE_VALUE_COUNTY);
        Map<Long, Area> cityMap = AreaUtils.getAreaMap(Area.TYPE_VALUE_CITY);
        Map<Long, Area> provinceMap = AreaUtils.getAreaMap(Area.TYPE_VALUE_PROVINCE);
        CustomerUtils.getAllCustomerMap();
        String areaName = "";
            if (flag==1){
                areaName = "省";
                Area province = provinceMap.get(areaId);
                if (province!=null){
                    areaName = province.getFullName();
                }
            }else if (flag==2){
                areaName = "市";
                Area city = cityMap.get(areaId);
                if (city!=null){
                    areaName = city.getFullName();
                }
            }else if (flag==3){
                areaName = "区";
                Area area = areaMap.get(areaId);
                if (area!=null){
                    areaName = area.getFullName();
                }
            }
        Customer customer = msCustomerService.getByIdToCustomer(customerId);
        String customerName = "";
        if (customer != null) {
            customerName = customer.getName();
        }
        String reportTitle =   selectYear +"年"+selectMonth +"月" +areaName + customerName + "每日完成";
        String searchConditionJson = RedisGsonUtils.toJson(rptGradedOrderSearchCondition);
        reportExportTaskService.createRptExportTask(flag==1?RPTReportEnum.PROVINCE_COMPLETED_ORDER_RPT:flag==2?RPTReportEnum.CITY_COMPLETED_ORDER_RPT:RPTReportEnum.COUNTY_COMPLETED_ORDER_RPT, RPTReportTypeEnum.ORDER_REPORT, user, reportTitle, searchConditionJson);
    }

    public void createOrderServicePointFeeRptExportTask(Date beginDate, Date endDate, Long servicePointId, List<Long> productCategoryIds,User user)  {
        RPTGradedOrderSearch rptGradedOrderSearchCondition = setOrderServicePointFeeSearchCondition(beginDate, endDate, servicePointId, productCategoryIds);
        rptGradedOrderSearchCondition.setPageNo(1);
        rptGradedOrderSearchCondition.setPageSize(200000);
        String beginDateStr = DateUtils.formatDate(beginDate, "yyyy年MM月dd日");
        String endDateStr = DateUtils.formatDate(endDate, "yyyy年MM月dd日");
        String servicePointName = "";
        if (servicePointId!=null){
            ServicePoint servicePoint = msServicePointService.getById(servicePointId);
            if (servicePoint!=null){
                servicePointName = servicePoint.getName();
            }
        }

        String reportTitle =   beginDateStr +"-" +endDateStr + servicePointName + "工单费用报表";
        String searchConditionJson = RedisGsonUtils.toJson(rptGradedOrderSearchCondition);
        reportExportTaskService.createRptExportTask(RPTReportEnum.ORDER_SERVICE_POINT_FEE_RPT, RPTReportTypeEnum.FINANCE_REPORT, user, reportTitle, searchConditionJson);
    }

    public void createDevelopAverageRptExportTask(Date beginDate, Date endDate, List<Long> productCategoryIds,User user)  {
        RPTGradedOrderSearch search = new RPTGradedOrderSearch();
        search.setBeginDate(beginDate.getTime());
        search.setEndDate(endDate.getTime());
        search.setQuarter(DateUtils.getQuarter(endDate));
        search.setProductCategoryIds(productCategoryIds);
        String endDateStr = DateUtils.formatDate(endDate, "yyyy年MM月dd日");
        String reportTitle = endDateStr  + "开发均单费用报表";
        String searchConditionJson = RedisGsonUtils.toJson(search);
        reportExportTaskService.createRptExportTask(RPTReportEnum.DEVELOP_AVERAGE_ORDER_FEE, RPTReportTypeEnum.FINANCE_REPORT, user, reportTitle, searchConditionJson);
    }
}
