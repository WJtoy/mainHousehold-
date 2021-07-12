package com.wolfking.jeesite.modules.fi.service;

import com.kkl.kklplus.entity.fi.servicepoint.ServicePointDeducted;
import com.wolfking.jeesite.modules.fi.dao.ServicePointDeductedDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by Jeff on 2017/6/16.
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class ServicePointDeductedService {
    @Resource
    private ServicePointDeductedDao servicePointDeductedDao;

    /**
     * 获取抵扣款汇总列表
     * @param deductionYearMonth
     * @return
     */
    public List<ServicePointDeducted> getDeductedAmountList(int deductionYearMonth, Long servicePointId){
        return servicePointDeductedDao.getDeductedAmountList(deductionYearMonth, servicePointId);
    }
}
