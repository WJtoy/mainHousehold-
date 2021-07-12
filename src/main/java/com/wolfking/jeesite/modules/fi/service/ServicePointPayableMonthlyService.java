package com.wolfking.jeesite.modules.fi.service;

import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.service.LongIDCrudService;
import com.wolfking.jeesite.modules.fi.dao.ServicePointPayableMonthlyDao;
import com.wolfking.jeesite.modules.fi.entity.ServicePointPayableMonthly;
import com.wolfking.jeesite.modules.rpt.service.ServicePointBalanceMonthlyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class ServicePointPayableMonthlyService extends LongIDCrudService<ServicePointPayableMonthlyDao, ServicePointPayableMonthly> {

    //region 网点余额功能上线数据调整方法

    @Resource
    ServicePointPayableMonthlyDao servicePointPayableMonthlyDao;

    @Resource

    @Autowired
    ServicePointBalanceMonthlyService servicePointBalanceMonthlyService;

    /**
     * 分页获取需要重新计算当月余额的分组网点列表
     * @param page
     * @return
     */
    public Page<ServicePointPayableMonthly> getServicePointCurrencyListForTask(Page<ServicePointPayableMonthly> page) {
        List<ServicePointPayableMonthly> list =  servicePointPayableMonthlyDao.getGroupedListForUpdateRptBalance(page);
        page.setList(list);
        return page;
    }

    @Transactional()
    public void calcBalance(){
        List<ServicePointPayableMonthly> servicePointPayableMonthlyList = servicePointPayableMonthlyDao.findAllList();
        for (ServicePointPayableMonthly servicePointPayableMonthly : servicePointPayableMonthlyList){
            //重新计算报表余额
            servicePointBalanceMonthlyService.calculateAndUpdateServicePointCurrentMonthBalance(servicePointPayableMonthly.getServicePoint().getId(), servicePointPayableMonthly.getPaymentType());
        }

    }

    //endregion 网点余额功能上线数据调整方法

}
