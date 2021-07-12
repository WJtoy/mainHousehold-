package com.wolfking.jeesite.modules.rpt.service;

import com.google.common.collect.Maps;
import com.kkl.kklplus.entity.rpt.RPTKeFuCompleteTimeEntity;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.rpt.dao.CustomerRevenueFeesRptDao;
import com.wolfking.jeesite.modules.rpt.dao.CustomerSpecialFeesDetailsOfRptDao;
import com.wolfking.jeesite.modules.rpt.entity.CustomerRevenueFeesRptEntity;
import com.wolfking.jeesite.modules.rpt.entity.SpecialFeesDetailsOfRptEntity;
import com.wolfking.jeesite.ms.providermd.utils.MDUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CustomerRevenueFeesRptService {

    @Resource
    CustomerRevenueFeesRptDao customerRevenueFeesRptDao;


    public CustomerRevenueFeesRptEntity getCustomerRevenueFeeOfChargeDate(Integer selectedYear, Integer selectedMonth, List<Long> productCategoryIds, Long customerId) {
        Date queryDate = DateUtils.getDate(selectedYear, selectedMonth, 1);
        Date startDate = DateUtils.getStartOfDay(queryDate);
        int yearMonth = generateYearMonth(queryDate);
        int currentYearMonth = generateYearMonth(new Date());
        String quarter = QuarterUtils.getSeasonQuarter(queryDate);
        Date endDate;
        if (yearMonth == currentYearMonth) {
            endDate = DateUtils.getStartOfDay(new Date());
        } else {
            endDate = DateUtils.addMonth(startDate, 1);
        }
        CustomerRevenueFeesRptEntity rptEntity = new CustomerRevenueFeesRptEntity();
        CustomerRevenueFeesRptEntity receivableCharge;
        CustomerRevenueFeesRptEntity payableChargeAList;
        CustomerRevenueFeesRptEntity payableChargeBList;

          //应收合计
            receivableCharge = customerRevenueFeesRptDao.getReceivableCharge(startDate, endDate, productCategoryIds, customerId, quarter);
            CustomerRevenueFeesRptEntity customerWriteOffCharge = customerRevenueFeesRptDao.getWriteOffChargeList(startDate, endDate, productCategoryIds, customerId, quarter);
            if(receivableCharge != null ){
                rptEntity.setCustomerServiceCharge(receivableCharge.getCustomerServiceCharge());
                rptEntity.setCustomerExpressCharge(receivableCharge.getCustomerExpressCharge());
                rptEntity.setCustomerTravelCharge(receivableCharge.getCustomerTravelCharge());
                rptEntity.setCustomerMaterialCharge(receivableCharge.getCustomerMaterialCharge());
                rptEntity.setCustomerTimelinessCharge(receivableCharge.getCustomerTimelinessCharge());
                rptEntity.setCustomerUrgentCharge(receivableCharge.getCustomerUrgentCharge());
                rptEntity.setCustomerOtherCharge(receivableCharge.getCustomerOtherCharge());
                rptEntity.setCustomerPraiseFee(receivableCharge.getCustomerPraiseFee());
            }


            if(customerWriteOffCharge != null){
                rptEntity.setCustomerWriteOffCharge(customerWriteOffCharge.getCustomerWriteOffCharge());
                rptEntity.setCustomerPraiseFee(rptEntity.getCustomerPraiseFee()+customerWriteOffCharge.getCustomerPraiseFee());

            }

        //应付合计
            payableChargeAList = customerRevenueFeesRptDao.getPayableChargeA(startDate, endDate, productCategoryIds, customerId, quarter);
            payableChargeBList = customerRevenueFeesRptDao.getPayableChargeB(startDate, endDate, productCategoryIds, customerId, quarter);
            CustomerRevenueFeesRptEntity pointWriteOffCharge = customerRevenueFeesRptDao.getDiffCharge(startDate, endDate, productCategoryIds, customerId, quarter);

            if(payableChargeAList!=null) {
                rptEntity.setEngineerServiceCharge(payableChargeAList.getEngineerServiceCharge());
                rptEntity.setEngineerExpressCharge(payableChargeAList.getEngineerExpressCharge());
                rptEntity.setEngineerTravelCharge(payableChargeAList.getEngineerTravelCharge());
                rptEntity.setEngineerMaterialCharge(payableChargeAList.getEngineerMaterialCharge());
                rptEntity.setEngineerOtherCharge(payableChargeAList.getEngineerOtherCharge());
            }

            if(payableChargeBList!=null) {
                rptEntity.setEngineerCustomerTimelinessCharge(payableChargeBList.getEngineerCustomerTimelinessCharge());
                rptEntity.setEngineerInsuranceCharge(payableChargeBList.getEngineerInsuranceCharge());
                rptEntity.setEngineerTimelinessCharge(payableChargeBList.getEngineerTimelinessCharge());
                rptEntity.setEngineerUrgentCharge(payableChargeBList.getEngineerUrgentCharge());
                rptEntity.setEngineerPraiseFee(payableChargeBList.getEngineerPraiseFee());
                rptEntity.setTaxFee(payableChargeBList.getTaxFee());
                rptEntity.setInfoFee(payableChargeBList.getInfoFee());
                rptEntity.setEngineerDeposit(payableChargeBList.getEngineerDeposit());
            }

            if(pointWriteOffCharge !=null){
                rptEntity.setEngineerWriteOffCharge(pointWriteOffCharge.getEngineerWriteOffCharge());
            }


        return rptEntity;
    }

    private int generateYearMonth(Date date) {
        int selectedYear = DateUtils.getYear(date);
        int selectedMonth = DateUtils.getMonth(date);
        return generateYearMonth(selectedYear, selectedMonth);
    }


    private int generateYearMonth(int selectedYear, int selectedMonth) {
        return StringUtils.toInteger(String.format("%04d%02d", selectedYear, selectedMonth));
    }
}
