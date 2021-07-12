/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.wolfking.jeesite.modules.md.utils;

import com.google.common.collect.Lists;
import com.wolfking.jeesite.common.config.WebProperties;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.wolfking.jeesite.common.utils.SpringContextHolder;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.List;

/**
 * 字典工具类
 *
 * @author ThinkGem
 * @version 2013-5-29
 */
@Slf4j
public class ServicePointUtils {

    private static WebProperties webProperties = SpringContextHolder.getBean(WebProperties.class);

    /**
     * 检查网点的保险启用开关
     */
    public static boolean servicePointInsuranceEnabled(ServicePoint servicePoint) {
        boolean flag = false;
//        if (webProperties.getServicePoint().getInsuranceEnabled()
//                && servicePoint != null && servicePoint.getInsuranceFlag() != null && servicePoint.getInsuranceFlag() == ServicePoint.INSURANCE_FLAG_ENABLED
//                && servicePoint.getAppInsuranceFlag() != null && servicePoint.getAppInsuranceFlag() == ServicePoint.APP_INSURANCE_FLAG_AGREE) {
//            flag = true;
//        }
        //TODO: 不再判断APP是否确认合作条款，2019-4-23
        if (webProperties.getServicePoint().getInsuranceEnabled()
                && servicePoint != null && servicePoint.getInsuranceFlag() != null && servicePoint.getInsuranceFlag() == ServicePoint.INSURANCE_FLAG_ENABLED) {
            flag = true;
        }
        return flag;
    }

    // region 数据分片
    /**
     * 获取分片名字列表
     * @return
     */
    public static List<String> getQuarters(int quarterCount) {
        if (quarterCount <=0) {
            return null;
        }
        Date startDate = OrderUtils.getGoLiveDate();
        int iYear = DateUtils.getYear(startDate);
        int iSeason = DateUtils.getSeason(startDate);
        Date dtSeasonStartDate = getSeasonStartDate(iYear,iSeason);
        Date endDate = DateUtils.addMonth(dtSeasonStartDate, 3*quarterCount);
        endDate = DateUtils.addDays(endDate,-1);
        List<String> quarterList = getQuarters(startDate, endDate);
        log.warn("开始日期:{},结束日期:{},分片:{}", DateUtils.formatDate(dtSeasonStartDate,"yyyy-MM-dd"), DateUtils.formatDate(endDate,"yyyy-MM-dd") ,quarterList);
        return quarterList;
    }

    public static List<String> getQuarters(Date startDate, Date endDate){
        List<String> quarters = Lists.newArrayList();
        if(startDate == null){
            return quarters;
        }

        startDate = DateUtils.parseDate(DateUtils.formatDate(startDate,"yyyy-MM-dd"));
        endDate = DateUtils.parseDate(DateUtils.formatDate(endDate,"yyyy-MM-dd"));
        int startMonth,endMonth;
        while(true){
            startMonth = startDate.getMonth();
            endMonth = endDate.getMonth();
            if(startDate.getTime()>endDate.getTime()
                    && startMonth != endMonth
                    && (startMonth/3 + 1) != (endMonth /3 + 1)
                    ){
                break;
            }
            quarters.add(QuarterUtils.getSeasonQuarter(startDate));
            startDate = DateUtils.addMonth(startDate,3);
        }
        return quarters;
    }

    private static Date getSeasonStartDate(int year, int season) {
        String combineDate = "";
        switch(season) {
            case 1:
                combineDate =  year + "-01-01";
                break;
            case 2:
                combineDate = year + "-04-01";
                break;
            case 3:
                combineDate = year + "-07-01";
                break;
            case 4:
                combineDate = year + "-10-01";
                break;
        }
        return DateUtils.parseDate(combineDate);
    }
    // endregion  数据分片
}
