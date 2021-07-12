package com.wolfking.jeesite.ms.providerrpt.service;

import com.google.common.collect.Maps;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTCrushAreaEntity;
import com.kkl.kklplus.entity.rpt.RPTSpecialChargeAreaEntity;
import com.kkl.kklplus.entity.rpt.common.RPTReportEnum;
import com.kkl.kklplus.entity.rpt.common.RPTReportTypeEnum;
import com.kkl.kklplus.entity.rpt.search.RPTSpecialChargeSearchCondition;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.AreaUtils;
import com.wolfking.jeesite.ms.common.config.MicroServicesProperties;
import com.wolfking.jeesite.ms.providerrpt.feign.MSCrushAreaRptFeign;
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
public class MSCrushAreaRptService {

    @Autowired
    private MSCrushAreaRptFeign msCrushAreaRptFeign;


    @Autowired
    private MicroServicesProperties microServicesProperties;

    @Autowired
    private ReportExportTaskService reportExportTaskService;

    /**
     *获取数据
     */
    public List<RPTCrushAreaEntity> getCrushAreaList(Integer year, Integer month,List<Long> productCategoryIds) {
        Date queryDate = DateUtils.getDate(year, month, 1);
        String yearMonth = DateUtils.getYearMonth(queryDate);
        String quarter = QuarterUtils.getSeasonQuarter(queryDate);
        RPTSpecialChargeSearchCondition searchCondition = new RPTSpecialChargeSearchCondition();
        searchCondition.setYearmonth(Integer.valueOf(yearMonth));
        searchCondition.setProductCategoryIds(productCategoryIds);
        searchCondition.setQuarter(quarter);
        MSResponse<List<RPTCrushAreaEntity>> specialChargeList = msCrushAreaRptFeign.getCrushList(searchCondition);
        List<RPTCrushAreaEntity> list = Lists.newArrayList();
        if (microServicesProperties.getReport().getEnabled()) {
            if (MSResponse.isSuccess(specialChargeList)) {
                list = specialChargeList.getData();
            }
        }
        return list;
    }




    public void checkRptExportTask(Integer yearMonth,List<Long> productCategoryIds,User user) {
        RPTSpecialChargeSearchCondition searchCondition = new RPTSpecialChargeSearchCondition();
        searchCondition.setYearmonth(yearMonth);
        searchCondition.setProductCategoryIds(productCategoryIds);

        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        reportExportTaskService.checkRptExportTask(RPTReportEnum.RPT_AREA_CRUSH_QTY, user, searchConditionJson);
    }


    public void createRptExportTask(Integer yearMonth,List<Long> productCategoryIds,User user) throws ParseException {
        RPTSpecialChargeSearchCondition searchCondition = new RPTSpecialChargeSearchCondition();
        searchCondition.setYearmonth(yearMonth);
        searchCondition.setProductCategoryIds(productCategoryIds);
        String dateString = DateUtils.formatDate(DateUtils.parse(String.valueOf(yearMonth), "yyyyMM"), "yyyy年MM月");

        String reportTitle =   dateString + "突击单量区域报表";
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);

        reportExportTaskService.createRptExportTask(RPTReportEnum.RPT_AREA_CRUSH_QTY, RPTReportTypeEnum.ORDER_REPORT, user, reportTitle, searchConditionJson);
    }

}
