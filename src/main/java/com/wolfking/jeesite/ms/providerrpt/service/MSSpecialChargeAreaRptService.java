package com.wolfking.jeesite.ms.providerrpt.service;

import com.google.common.collect.Maps;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTSpecialChargeAreaEntity;
import com.kkl.kklplus.entity.rpt.common.RPTReportEnum;
import com.kkl.kklplus.entity.rpt.common.RPTReportTypeEnum;
import com.kkl.kklplus.entity.rpt.search.RPTSpecialChargeSearchCondition;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.AreaUtils;
import com.wolfking.jeesite.ms.providerrpt.feign.MSSpecialChargeAreaRptFeign;
import com.wolfking.jeesite.ms.providerrpt.utils.RedisGsonUtils;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Service
public class MSSpecialChargeAreaRptService {
    @Autowired
    private MSSpecialChargeAreaRptFeign msSpecialChargeAreaRptFeign;

    @Autowired
    private ReportExportTaskService reportExportTaskService;

    /**
     *获取数据
     */
    public List<RPTSpecialChargeAreaEntity> getSpecialChargeAreaList(Integer year,Integer month,Long areaId,List<Long> productCategoryIds,Integer flag) {
        Date queryDate = DateUtils.getDate(year, month, 1);
        String yearMonth = DateUtils.getYearMonth(queryDate);
        RPTSpecialChargeSearchCondition searchCondition = new RPTSpecialChargeSearchCondition();
        searchCondition.setYearmonth(Integer.valueOf(yearMonth));
        searchCondition.setProductCategoryIds(productCategoryIds);
        searchCondition.setAreaId(areaId);
        MSResponse<List<RPTSpecialChargeAreaEntity>> specialChargeList = msSpecialChargeAreaRptFeign.getSpecialChargeList(searchCondition);
        List<RPTSpecialChargeAreaEntity> list = Lists.newArrayList();
        if (MSResponse.isSuccess(specialChargeList)){
            list = specialChargeList.getData();
        }
        if (list.size()>0){
            //区县
            Map<Long, List<RPTSpecialChargeAreaEntity>> countyMap = list.stream().collect(Collectors.groupingBy(RPTSpecialChargeAreaEntity::getCountyId));
            List<RPTSpecialChargeAreaEntity> countyList = Lists.newArrayList();
            for (List<RPTSpecialChargeAreaEntity> listItem : countyMap.values()) {
                RPTSpecialChargeAreaEntity countyItem = new RPTSpecialChargeAreaEntity();
                countyItem.setProvinceId(listItem.get(0).getProvinceId());
                countyItem.setProvinceName(listItem.get(0).getProvinceName());
                countyItem.setCityId(listItem.get(0).getCityId());
                countyItem.setCityName(listItem.get(0).getCityName());
                countyItem.setCountyId(listItem.get(0).getCountyId());
                countyItem.setCountyName(listItem.get(0).getCountyName());
                computeSumAndPerForCount(listItem, "O", countyItem);
                computeSumAndPerForCount(listItem, "T", countyItem);
                countyList.add(countyItem);
            }

            //地市
            Map<Long, List<RPTSpecialChargeAreaEntity>> cityMap = Maps.newHashMap();
            for (RPTSpecialChargeAreaEntity item : countyList) {
                List<RPTSpecialChargeAreaEntity> tempCity;
                if (cityMap.containsKey(item.getCityId())) {
                    tempCity = cityMap.get(item.getCityId());
                } else {
                    tempCity = Lists.newArrayList();
                    cityMap.put(item.getCityId(), tempCity);
                }
                tempCity.add(item);
            }
            List<RPTSpecialChargeAreaEntity> cityList = Lists.newArrayList();
            for (List<RPTSpecialChargeAreaEntity> listItem : cityMap.values()) {
                RPTSpecialChargeAreaEntity cityItem = new RPTSpecialChargeAreaEntity();
                cityItem.setProvinceId(listItem.get(0).getProvinceId());
                cityItem.setProvinceName(listItem.get(0).getProvinceName());
                cityItem.setCityId(listItem.get(0).getCityId());
                cityItem.setCityName(listItem.get(0).getCityName());
                cityItem.setCountyId(listItem.get(0).getCountyId());
                cityItem.setCountyName(listItem.get(0).getCountyName());
                cityItem.setItemList(listItem);
                computeSumAndPerForCount(listItem, "O", cityItem);
                computeSumAndPerForCount(listItem, "T", cityItem);
                cityList.add(cityItem);
            }
//            if (flag==2){
//                RPTSpecialChargeAreaEntity sumSCAD = new RPTSpecialChargeAreaEntity();
//                computeSumAndPerForCount(cityList, "O", sumSCAD);
//                computeSumAndPerForCount(cityList, "T", sumSCAD);
//                cityList.add(sumSCAD);
//                return cityList;
//            }
            //省份
            Map<Long, List<RPTSpecialChargeAreaEntity>> provinceMap = Maps.newHashMap();
            for (RPTSpecialChargeAreaEntity item : cityList) {
                List<RPTSpecialChargeAreaEntity> tempProvince;
                if (provinceMap.containsKey(item.getProvinceId())) {
                    tempProvince = provinceMap.get(item.getProvinceId());
                } else {
                    tempProvince = Lists.newArrayList();
                    provinceMap.put(item.getProvinceId(), tempProvince);
                }
                tempProvince.add(item);
            }

            List<RPTSpecialChargeAreaEntity> provinceList = Lists.newArrayList();
            for (List<RPTSpecialChargeAreaEntity> listItem : provinceMap.values()) {
                RPTSpecialChargeAreaEntity provinceItem = new RPTSpecialChargeAreaEntity();

                provinceItem.setProvinceId(listItem.get(0).getProvinceId());
                provinceItem.setProvinceName(listItem.get(0).getProvinceName());
                provinceItem.setCityId(listItem.get(0).getCityId());
                provinceItem.setCityName(listItem.get(0).getCityName());
                provinceItem.setCountyId(listItem.get(0).getCountyId());
                provinceItem.setCountyName(listItem.get(0).getCountyName());

                provinceItem.setItemList(listItem);
                computeSumAndPerForCount(listItem, "O", provinceItem);
                computeSumAndPerForCount(listItem, "T", provinceItem);
                provinceList.add(provinceItem);
            }

            RPTSpecialChargeAreaEntity sumSCAD = new RPTSpecialChargeAreaEntity();
            computeSumAndPerForCount(provinceList, "O", sumSCAD);
            computeSumAndPerForCount(provinceList, "T", sumSCAD);
            provinceList.add(sumSCAD);
            return provinceList;
        }
        return Lists.newArrayList();

    }

    public static void computeSumAndPerForCount(List baseDailyReports,String str,
                                                RPTSpecialChargeAreaEntity sumDailyReport) {

        //计算每日的总单数
        if (sumDailyReport != null) {
            Class sumDailyReportClass = sumDailyReport.getClass();
            for (Object object : baseDailyReports) {
                RPTSpecialChargeAreaEntity item = (RPTSpecialChargeAreaEntity) object;
                Class itemClass = item.getClass();
                for (int i = 1; i < 32; i++) {
                    String strGetMethodName = "get"+str + i;
                    String strSetMethodName = "set"+str + i;
                    try {
                        Method itemGetMethod = itemClass.getMethod(strGetMethodName);
                        Object itemGetD = itemGetMethod.invoke(item);

                        Method sumDailyReportClassGetMethod = sumDailyReportClass.getMethod(strGetMethodName);
                        Object sumDailyReportClassGetD = sumDailyReportClassGetMethod.invoke(sumDailyReport);

                        Double dSum = StringUtils.toDouble(sumDailyReportClassGetD) + StringUtils.toDouble(itemGetD);

                        Method sumDailyReportSetMethod = sumDailyReportClass.getMethod(strSetMethodName, Double.class);
                        sumDailyReportSetMethod.invoke(sumDailyReport, dSum);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

            }
        }
    }


    public void checkRptExportTask(Long areaId, Integer yearMonth,List<Long> productCategoryIds,User user) {
        RPTSpecialChargeSearchCondition searchCondition = new RPTSpecialChargeSearchCondition();
        searchCondition.setAreaId(areaId);
        searchCondition.setYearmonth(yearMonth);
        searchCondition.setProductCategoryIds(productCategoryIds);

        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        reportExportTaskService.checkRptExportTask(RPTReportEnum.SPECIAL_CHARGE_AREA_RPT_1, user, searchConditionJson);
    }


    public void createRptExportTask(Long areaId, Integer yearMonth,List<Long> productCategoryIds,User user,int flag) throws ParseException {
        RPTSpecialChargeSearchCondition searchCondition = new RPTSpecialChargeSearchCondition();
        searchCondition.setAreaId(areaId);
        searchCondition.setYearmonth(yearMonth);
        searchCondition.setProductCategoryIds(productCategoryIds);
        String dateString = DateUtils.formatDate(DateUtils.parse(String.valueOf(yearMonth), "yyyyMM"), "yyyy年MM月");
        Map<Long, Area> areaMap = AreaUtils.getAreaMap(4);
        Map<Long, Area> cityMap = AreaUtils.getAreaMap(3);
        Map<Long, Area> provinceMap = AreaUtils.getAreaMap(2);

        String areaName = "";
        if (areaId==null){
            if (flag==1){
                areaName = "省市";
            }else {
                areaName = "区县";
            }
        }else {
            Area area = areaMap.get(areaId);
            if (area!=null){
                areaName = area.getFullName();
            }
            Area city = cityMap.get(areaId);
            if (city!=null){
                areaName = city.getFullName();
            }
            Area province = provinceMap.get(areaId);
            if (province!=null){
                areaName = province.getFullName();
            }
        }
        String reportTitle =   dateString +areaName+ "特殊费用分布";
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);

        reportExportTaskService.createRptExportTask(flag==1?RPTReportEnum.SPECIAL_CHARGE_AREA_RPT_1:RPTReportEnum.SPECIAL_CHARGE_AREA_RPT_2, RPTReportTypeEnum.ORDER_REPORT, user, reportTitle, searchConditionJson);
    }

}
