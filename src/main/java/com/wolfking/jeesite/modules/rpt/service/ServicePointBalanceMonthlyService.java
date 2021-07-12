/**
 * Copyright &copy; 2012-2013 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.wolfking.jeesite.modules.rpt.service;

import com.wolfking.jeesite.common.service.BaseService;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.rpt.dao.ServicePointBalanceMonthlyDao;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author
 */
@Service
@Component
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class ServicePointBalanceMonthlyService extends BaseService {

    @Resource
    private ServicePointBalanceMonthlyDao servicePointBalanceMonthlyDao;

    /**
     * 重新计算当月余额
     *
     * @param servicePointId
     * @param paymentType
     */
    @Transactional()
    public void calculateAndUpdateServicePointCurrentMonthBalance(Long servicePointId, Integer paymentType) {
        Date now = new Date();
        int year = DateUtils.getYear(now);
        int month = DateUtils.getMonth(now);
        Double currentMonthBalance = servicePointBalanceMonthlyDao.getServicePointCurrentMonthBalance(servicePointId, paymentType);
//		if (currentMonthBalance != null && !currentMonthBalance.equals(0d)) {
        if (currentMonthBalance != null) {
            servicePointBalanceMonthlyDao.updateMonthBalance(year, "m".concat(String.valueOf(month)), currentMonthBalance, servicePointId, paymentType);
        }
    }

}
