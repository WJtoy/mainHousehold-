package com.wolfking.jeesite.modules.fi.task;

import com.google.common.collect.Lists;
import com.kkl.kklplus.entity.fi.servicepoint.ServicePointBalanceMonthlyDetail;
import com.kkl.kklplus.entity.fi.servicepoint.ServicePointInvoiceMonthlyDetail;
import com.kkl.kklplus.entity.fi.servicepoint.ServicePointPaidMonthlyDetail;
import com.kkl.kklplus.utils.NumberUtils;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.fi.dao.*;
import com.wolfking.jeesite.modules.fi.entity.EngineerChargeCondition;
import com.wolfking.jeesite.modules.fi.entity.EngineerCurrency;
import com.wolfking.jeesite.modules.fi.entity.ServicePointPayableMonthlyDetail;
import com.wolfking.jeesite.modules.fi.entity.ServicePointWithdraw;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.rpt.dao.ServicePointBalanceMonthlyDetailDao;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

//@Component
//@Lazy(value = false)/*need*/
@Slf4j
public class FISchedules {
    @Resource
    private ServicePointPayableMonthlyDao servicePointPayableMonthlyDao;
    @Resource
    private EngineerCurrencyDao engineerCurrencyDao;
    @Resource
    private EngineerChargeConditionDao engineerChargeConditionDao;
    @Resource
    private ServicePointPayableMonthlyDetailDao payableMonthlyDetailDao;
    @Resource
    private ServicePointBalanceMonthlyDetailDao balanceMonthlyDetailDao;
    @Resource
    private ServicePointWithdrawDao servicePointWithdrawDao;
    @Resource
    private ServicePointPaidMonthlyDetailDao paidMonthlyDetailDao;
    @Resource
    private ServicePointInvoiceMonthlyDetailDao invoiceMonthlyDetailDao;

    public void updatePayableDetailMonthly(int year, int month, String beginDate, String endDate) {
        try {
            Long startTime = System.currentTimeMillis();
            int pageNo = 1;
            Page<Long> payableServicePointIdPage = new Page<>(pageNo, 500);
            payableServicePointIdPage.setList(servicePointPayableMonthlyDao.getPayableMonthByYearMonth(payableServicePointIdPage, year, month));
            if (payableServicePointIdPage != null && payableServicePointIdPage.getList() != null && payableServicePointIdPage.getList().size() > 0) {
                for (Long servicePointId : payableServicePointIdPage.getList()) {
                    updatePayableDetailMonthlyByServicePointId(servicePointId, year, month, beginDate, endDate);
                }
            }
            while (pageNo < payableServicePointIdPage.getLast()) {
                pageNo++;
                payableServicePointIdPage = new Page<>(pageNo, 500);
                payableServicePointIdPage.setList(servicePointPayableMonthlyDao.getPayableMonthByYearMonth(payableServicePointIdPage, year, month));
                if (payableServicePointIdPage != null && payableServicePointIdPage.getList() != null && payableServicePointIdPage.getList().size() > 0) {
                    for (Long servicePointId : payableServicePointIdPage.getList()) {
                        updatePayableDetailMonthlyByServicePointId(servicePointId, year, month, beginDate, endDate);
                    }
                }
            }

            double spendSeconds = (System.currentTimeMillis() - startTime) / 1000d;
            StringBuilder message = new StringBuilder();
            message.append("运行时间:")
                    .append(DateUtils.formatDateTime(DateUtils.longToDate(startTime)))
                    .append(",用时:")
                    .append(NumberUtils.formatNum(spendSeconds))
                    .append("秒");
            LogUtils.saveLog("定时任务", "updatePayableDetailMonthly", message.toString(), null, null);
        } catch (Exception e) {
            LogUtils.saveLog("定时任务", "updatePayableDetailMonthly", DateUtils.getDateTime(), e, null);
        }
    }

    private void updatePayableDetailMonthlyByServicePointId(Long servicePointId, int year, int month, String beginDate, String endDate) {
        int pageNo = 1;
        Page<EngineerCurrency> currencyPage = new Page<>(pageNo, 500);
        currencyPage.setList(engineerCurrencyDao.getCurrencyByDateRangeForUpdatePayable(currencyPage, servicePointId, beginDate, endDate));
        if (currencyPage != null && currencyPage.getList() != null && currencyPage.getList().size() > 0) {
            for (EngineerCurrency currency : currencyPage.getList()) {
                EngineerChargeCondition chargeCondition = engineerChargeConditionDao.getOneByOrderNoAndServicePointIdForUpdatePayable(currency.getCurrencyNo(), servicePointId);

                ServicePointPayableMonthlyDetail payableDetail = new ServicePointPayableMonthlyDetail();
                payableDetail.setTotalId((servicePointId * 10000 + year) * 100 + chargeCondition.getPaymentType());
                payableDetail.setServicePoint(new ServicePoint(servicePointId));
                payableDetail.setPaymentType(chargeCondition.getPaymentType());
                payableDetail.setYear(year);
                payableDetail.setMonth(month);
                payableDetail.setProductCategoryId(chargeCondition.getProductCategoryId());
                payableDetail.setId((servicePointId * 10000 + year) * 100 + chargeCondition.getPaymentType() + chargeCondition.getProductCategoryId());
                payableDetail.setAmount(currency.getAmount());

                payableMonthlyDetailDao.incrAmountForCharge(payableDetail);
                balanceMonthlyDetailDao.incrBalance(payableDetail);
            }
        }
        while (pageNo < currencyPage.getLast()) {
            pageNo++;
            currencyPage = new Page<>(pageNo, 500);
            currencyPage.setList(engineerCurrencyDao.getCurrencyByDateRangeForUpdatePayable(currencyPage, servicePointId, beginDate, endDate));
            if (currencyPage != null && currencyPage.getList() != null && currencyPage.getList().size() > 0) {
                for (EngineerCurrency currency : currencyPage.getList()) {
                    EngineerChargeCondition chargeCondition = engineerChargeConditionDao.getOneByOrderNoAndServicePointIdForUpdatePayable(currency.getCurrencyNo(), servicePointId);

                    ServicePointPayableMonthlyDetail payableDetail = new ServicePointPayableMonthlyDetail();
                    payableDetail.setTotalId((servicePointId * 10000 + year) * 100 + chargeCondition.getPaymentType());
                    payableDetail.setServicePoint(new ServicePoint(servicePointId));
                    payableDetail.setPaymentType(chargeCondition.getPaymentType());
                    payableDetail.setYear(year);
                    payableDetail.setMonth(month);
                    payableDetail.setProductCategoryId(chargeCondition.getProductCategoryId());
                    payableDetail.setId((servicePointId * 10000 + year) * 100 + chargeCondition.getPaymentType() + chargeCondition.getProductCategoryId());
                    payableDetail.setAmount(currency.getAmount());

                    payableMonthlyDetailDao.incrAmountForCharge(payableDetail);
                    balanceMonthlyDetailDao.incrBalance(payableDetail);
                }
            }
        }
    }

    public void updateBalanceDetailMonthly(int year, int month, String beginDate, String endDate) {
        try {
            double totalInvoiceAmount = 0;
            Long startTime = System.currentTimeMillis();
            int pageNo = 1;
            Page<ServicePointWithdraw> page = new Page<>(pageNo, 500);
            page.setList(servicePointWithdrawDao.getWithdrawByDateRangeForUpdateBalanceDetail(page, beginDate, endDate));
            if (page != null && page.getList() != null && page.getList().size() > 0) {
                for (ServicePointWithdraw withdraw : page.getList()) {
                    updateBalanceDetailMonthlyWithWithdraw(year, month, withdraw);
                    totalInvoiceAmount = totalInvoiceAmount + withdraw.getPayAmount() - withdraw.getPlatformFee();
                }
            }
            while (pageNo < page.getLast()) {
                pageNo++;
                page = new Page<>(pageNo, 500);
                page.setList(servicePointWithdrawDao.getWithdrawByDateRangeForUpdateBalanceDetail(page, beginDate, endDate));
                if (page != null && page.getList() != null && page.getList().size() > 0) {
                    for (ServicePointWithdraw withdraw : page.getList()) {
                        updateBalanceDetailMonthlyWithWithdraw(year, month, withdraw);
                        totalInvoiceAmount = totalInvoiceAmount + withdraw.getPayAmount() - withdraw.getPlatformFee();
                    }
                }
            }

            log.error("============= total invoice amount : {}", totalInvoiceAmount);

            double spendSeconds = (System.currentTimeMillis() - startTime) / 1000d;
            StringBuilder message = new StringBuilder();
            message.append("运行时间:")
                    .append(DateUtils.formatDateTime(DateUtils.longToDate(startTime)))
                    .append(",用时:")
                    .append(NumberUtils.formatNum(spendSeconds))
                    .append("秒, 总计:")
                    .append(String.format("%.2f", totalInvoiceAmount));
            LogUtils.saveLog("定时任务", "updateBalanceDetailMonthly", message.toString(), null, null);
        } catch (Exception e) {
            LogUtils.saveLog("定时任务", "updateBalanceDetailMonthly", DateUtils.getDateTime(), e, null);
        }
    }

    private void updateBalanceDetailMonthlyWithWithdraw(int year, int month, ServicePointWithdraw servicePointWithdraw) {
        long defaultProductCategoryId = 1l;

        com.kkl.kklplus.entity.fi.servicepoint.ServicePointPayableMonthlyDetail searchPayableMonthlyDetail = new com.kkl.kklplus.entity.fi.servicepoint.ServicePointPayableMonthlyDetail();
        long totalId = (servicePointWithdraw.getServicePoint().getId() * 10000 + servicePointWithdraw.getPayForYear()) * 100 +
                servicePointWithdraw.getPaymentType();
        searchPayableMonthlyDetail.setTotalId(totalId);
        searchPayableMonthlyDetail.setMonth(servicePointWithdraw.getPayForMonth());

        double invoiceAmount = servicePointWithdraw.getPayAmount() - servicePointWithdraw.getPlatformFee();

        double updatedAmount;

        if (servicePointWithdraw.getPayForYear() == year && servicePointWithdraw.getPayForMonth() == 11) {
            //获取按品类区分应付金额
            List<com.kkl.kklplus.entity.fi.servicepoint.ServicePointPayableMonthlyDetail> needPayList = Lists.newArrayList();
            if (servicePointWithdraw.getPaymentType() == 20) {
                needPayList = payableMonthlyDetailDao.getNeedPayListForUpdateBalanceDetail10Pay(searchPayableMonthlyDetail);
            }
//            List<com.kkl.kklplus.entity.fi.servicepoint.ServicePointPayableMonthlyDetail> sortedNeedPayList = needPayList.stream().sorted(Comparator.comparing(com.kkl.kklplus.entity.fi.servicepoint.ServicePointPayableMonthlyDetail::getAmount)).collect(Collectors.toList());
            double remainAmount = servicePointWithdraw.getPayAmount() - servicePointWithdraw.getPlatformFee();
            double detailAmount;
            updatedAmount = 0;
            for (com.kkl.kklplus.entity.fi.servicepoint.ServicePointPayableMonthlyDetail detail : needPayList) {
                detailAmount = detail.getAmount();
                if (remainAmount <= 0) {
                    break;
                }
                double amount = detailAmount;
                if (detailAmount > remainAmount) {
                    amount = remainAmount;
                }
                //计帐金额异动
                ServicePointPaidMonthlyDetail paidMonthlyDetail = new ServicePointPaidMonthlyDetail();
                paidMonthlyDetail.setServicepointId(servicePointWithdraw.getServicePoint().getId());
                paidMonthlyDetail.setPaymentType(servicePointWithdraw.getPaymentType());
                paidMonthlyDetail.setYear(servicePointWithdraw.getPayForYear());
                paidMonthlyDetail.setMonth(servicePointWithdraw.getPayForMonth());
                paidMonthlyDetail.setProductCategoryId(detail.getProductCategoryId());
                paidMonthlyDetail.setAmount(amount);
                paidMonthlyDetail.setId((paidMonthlyDetail.getServicepointId() * 10000 + paidMonthlyDetail.getYear()) * 100 +
                        paidMonthlyDetail.getPaymentType() + paidMonthlyDetail.getProductCategoryId());
                paidMonthlyDetailDao.incrAmountForUpdateBalanceDetail(paidMonthlyDetail);
                //出帐金额异动
                ServicePointInvoiceMonthlyDetail invoiceMonthlyDetail = new ServicePointInvoiceMonthlyDetail();
                invoiceMonthlyDetail.setServicepointId(servicePointWithdraw.getServicePoint().getId());
                invoiceMonthlyDetail.setPaymentType(servicePointWithdraw.getPaymentType());
                invoiceMonthlyDetail.setYear(year);
                invoiceMonthlyDetail.setMonth(month);
                invoiceMonthlyDetail.setProductCategoryId(detail.getProductCategoryId());
                invoiceMonthlyDetail.setAmount(amount);
                invoiceMonthlyDetail.setId((invoiceMonthlyDetail.getServicepointId() * 10000 + invoiceMonthlyDetail.getYear()) * 100 +
                        invoiceMonthlyDetail.getPaymentType() + invoiceMonthlyDetail.getProductCategoryId());
                invoiceMonthlyDetailDao.incrAmountForUpdateBalanceDetail(invoiceMonthlyDetail);
                //余额异动
                ServicePointBalanceMonthlyDetail balanceMonthlyDetail = new ServicePointBalanceMonthlyDetail();
                balanceMonthlyDetail.setServicepointId(servicePointWithdraw.getServicePoint().getId());
                balanceMonthlyDetail.setPaymentType(servicePointWithdraw.getPaymentType());
                balanceMonthlyDetail.setYear(year);
                balanceMonthlyDetail.setMonth(month);
                balanceMonthlyDetail.setProductCategoryId(detail.getProductCategoryId());
                balanceMonthlyDetail.setAmount(0 - amount);
                balanceMonthlyDetail.setId((balanceMonthlyDetail.getServicepointId() * 10000 + balanceMonthlyDetail.getYear()) * 100 +
                        balanceMonthlyDetail.getPaymentType() + balanceMonthlyDetail.getProductCategoryId());
                balanceMonthlyDetailDao.incrBalanceWithBalanceForUpdateBalanceDetail(balanceMonthlyDetail);

                remainAmount -= detailAmount;
                updatedAmount += amount;
            }
            if (remainAmount > 0) {
                //计帐金额异动
                ServicePointPaidMonthlyDetail paidMonthlyDetail = new ServicePointPaidMonthlyDetail();
                paidMonthlyDetail.setServicepointId(servicePointWithdraw.getServicePoint().getId());
                paidMonthlyDetail.setPaymentType(servicePointWithdraw.getPaymentType());
                paidMonthlyDetail.setYear(year);
                paidMonthlyDetail.setMonth(month);
                paidMonthlyDetail.setProductCategoryId(defaultProductCategoryId);
                paidMonthlyDetail.setAmount(remainAmount);
                paidMonthlyDetail.setId((paidMonthlyDetail.getServicepointId() * 10000 + paidMonthlyDetail.getYear()) * 100 +
                        paidMonthlyDetail.getPaymentType() + paidMonthlyDetail.getProductCategoryId());
                paidMonthlyDetailDao.incrAmountForUpdateBalanceDetail(paidMonthlyDetail);
                //出帐金额异动
                ServicePointInvoiceMonthlyDetail invoiceMonthlyDetail = new ServicePointInvoiceMonthlyDetail();
                invoiceMonthlyDetail.setServicepointId(servicePointWithdraw.getServicePoint().getId());
                invoiceMonthlyDetail.setPaymentType(servicePointWithdraw.getPaymentType());
                invoiceMonthlyDetail.setYear(year);
                invoiceMonthlyDetail.setMonth(month);
                invoiceMonthlyDetail.setProductCategoryId(defaultProductCategoryId);
                invoiceMonthlyDetail.setAmount(remainAmount);
                invoiceMonthlyDetail.setId((invoiceMonthlyDetail.getServicepointId() * 10000 + invoiceMonthlyDetail.getYear()) * 100 +
                        invoiceMonthlyDetail.getPaymentType() + invoiceMonthlyDetail.getProductCategoryId());
                invoiceMonthlyDetailDao.incrAmountForUpdateBalanceDetail(invoiceMonthlyDetail);
                //余额异动
                ServicePointBalanceMonthlyDetail balanceMonthlyDetail = new ServicePointBalanceMonthlyDetail();
                balanceMonthlyDetail.setServicepointId(servicePointWithdraw.getServicePoint().getId());
                balanceMonthlyDetail.setPaymentType(servicePointWithdraw.getPaymentType());
                balanceMonthlyDetail.setYear(year);
                balanceMonthlyDetail.setMonth(month);
                balanceMonthlyDetail.setProductCategoryId(defaultProductCategoryId);
                balanceMonthlyDetail.setAmount(0 - remainAmount);
                balanceMonthlyDetail.setId((balanceMonthlyDetail.getServicepointId() * 10000 + balanceMonthlyDetail.getYear()) * 100 +
                        balanceMonthlyDetail.getPaymentType() + balanceMonthlyDetail.getProductCategoryId());
                balanceMonthlyDetailDao.incrBalanceWithBalanceForUpdateBalanceDetail(balanceMonthlyDetail);
                updatedAmount += remainAmount;
            }
        }
//        else {
//            updatedAmount = 0;
//            //出帐金额异动
//            ServicePointInvoiceMonthlyDetail invoiceMonthlyDetail = new ServicePointInvoiceMonthlyDetail();
//            invoiceMonthlyDetail.setServicepointId(servicePointWithdraw.getServicePoint().getId());
//            invoiceMonthlyDetail.setPaymentType(servicePointWithdraw.getPaymentType());
//            invoiceMonthlyDetail.setYear(year);
//            invoiceMonthlyDetail.setMonth(month);
//            invoiceMonthlyDetail.setProductCategoryId(defaultProductCategoryId);
//            invoiceMonthlyDetail.setAmount(servicePointWithdraw.getPayAmount() - servicePointWithdraw.getPlatformFee());
//            invoiceMonthlyDetail.setId((invoiceMonthlyDetail.getServicepointId() * 10000 + invoiceMonthlyDetail.getYear()) * 100 +
//                    invoiceMonthlyDetail.getPaymentType() + invoiceMonthlyDetail.getProductCategoryId());
//            invoiceMonthlyDetailDao.incrAmountForUpdateBalanceDetail(invoiceMonthlyDetail);
//            //余额异动
//            ServicePointBalanceMonthlyDetail balanceMonthlyDetail = new ServicePointBalanceMonthlyDetail();
//            balanceMonthlyDetail.setServicepointId(servicePointWithdraw.getServicePoint().getId());
//            balanceMonthlyDetail.setPaymentType(servicePointWithdraw.getPaymentType());
//            balanceMonthlyDetail.setYear(year);
//            balanceMonthlyDetail.setMonth(month);
//            balanceMonthlyDetail.setProductCategoryId(defaultProductCategoryId);
//            balanceMonthlyDetail.setAmount(0 - servicePointWithdraw.getPayAmount() + servicePointWithdraw.getPlatformFee());
//            balanceMonthlyDetail.setId((balanceMonthlyDetail.getServicepointId() * 10000 + balanceMonthlyDetail.getYear()) * 100 +
//                    balanceMonthlyDetail.getPaymentType() + balanceMonthlyDetail.getProductCategoryId());
//            balanceMonthlyDetailDao.incrBalanceWithBalanceForUpdateBalanceDetail(balanceMonthlyDetail);
//
//            updatedAmount = updatedAmount + servicePointWithdraw.getPayAmount() - servicePointWithdraw.getPlatformFee();
//        }
        String s = "";
    }
//    @Autowired
//    private ServicePointPayableMonthlyService servicePointPayableMonthlyService;
//    @Autowired
//    private ServicePointBalanceMonthlyService servicePointBalanceMonthlyService;
//
//    @Scheduled(cron = "0 45 3 1 * ?") //每月第一天凌晨3点过45分执行
//    public void calculateServicePointBalanceFirstDay(){
//        calculateServicePointBalance();
//    }
//
////    @Scheduled(cron = "0 15 4 L * ? ") //每月最后一天凌晨4点过15分执行
////    public void calculateServicePointBalanceLastDay(){
////        calculateServicePointBalance();
////    }
//
//    private void calculateServicePointBalance(){
//        boolean scheduleEnabled = Boolean.valueOf(Global.getConfig("scheduleEnabled"));
//        if (!scheduleEnabled) {
//            return;
//        }
//        int total = 0;
//        int success = 0;
//        int fail = 0;
//        try {
//            Long startTime = System.currentTimeMillis();
//            int pageNo = 1;
//            Page<ServicePointPayableMonthly> page = new Page<>(pageNo, 20);
//            page = servicePointPayableMonthlyService.getServicePointCurrencyListForTask(page);
//            if (page != null && page.getList() != null && page.getList().size() > 0) {
//                for (ServicePointPayableMonthly servicePointPayableMonthly : page.getList()) {
//                    total++;
//                    try {
//                        servicePointBalanceMonthlyService.calculateAndUpdateServicePointCurrentMonthBalance(servicePointPayableMonthly);
//                        success++;
//                    } catch (Exception e) {
//                        LogUtils.saveLog("重新计算余额失败", "FISchedules.calculateServicePointBalance", servicePointPayableMonthly.getServicePoint().getId().toString(), e, null);
//                        fail++;
//                    }
//                }
//            }
//            while (pageNo < page.getLast()) {
//                pageNo++;
//                page = new Page<>(pageNo, 20);
//                page = servicePointPayableMonthlyService.getServicePointCurrencyListForTask(page);
//                if (page != null && page.getList() != null && page.getList().size() > 0) {
//                    for (ServicePointPayableMonthly servicePointPayableMonthly : page.getList()) {
//                        total = total + 1;
//                        try {
//                            servicePointBalanceMonthlyService.calculateAndUpdateServicePointCurrentMonthBalance(servicePointPayableMonthly);
//                            success = success + 1;
//                        } catch (Exception e) {
//                            LogUtils.saveLog("重新计算余额失败", "FISchedules.calculateServicePointBalance", servicePointPayableMonthly.getServicePoint().getId().toString(), e, null);
//                            fail = fail + 1;
//                        }
//                    }
//                }
//            }
//            double spendSeconds = (System.currentTimeMillis() - startTime) / 1000d;
//            StringBuilder message = new StringBuilder();
//            message.append("运行时间:")
//                    .append(DateUtils.formatDateTime(DateUtils.longToDate(startTime)))
//                    .append(",总数:").append(String.valueOf(total))
//                    .append(",成功数：").append(String.valueOf(success))
//                    .append(",失败数：").append(String.valueOf(fail))
//                    .append(",用时:")
//                    .append(StringUtils.formatNum(spendSeconds))
//                    .append("秒");
//            LogUtils.saveLog("定时任务", "重新计算余额", message.toString(), null, null);
//        } catch (Exception e) {
//            LogUtils.saveLog("定时任务", "重新计算余额", DateUtils.getDateTime(), e, null);
//        }
//    }
}
