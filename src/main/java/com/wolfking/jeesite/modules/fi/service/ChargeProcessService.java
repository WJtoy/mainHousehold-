package com.wolfking.jeesite.modules.fi.service;

import com.wolfking.jeesite.modules.fi.dao.ServicePointPayableMonthlyDao;
import com.wolfking.jeesite.modules.fi.dao.ServicePointPayableMonthlyDetailDao;
import com.wolfking.jeesite.modules.fi.entity.ServicePointPayableMonthly;
import com.wolfking.jeesite.modules.fi.entity.ServicePointPayableMonthlyDetail;
import com.wolfking.jeesite.modules.md.dao.CustomerFinanceDao;
import com.wolfking.jeesite.modules.md.dao.ServicePointDao;
import com.wolfking.jeesite.modules.md.entity.CustomerFinance;
import com.wolfking.jeesite.modules.md.entity.ServicePointFinance;
import com.wolfking.jeesite.modules.rpt.dao.ServicePointBalanceMonthlyDao;
import com.wolfking.jeesite.modules.rpt.dao.ServicePointBalanceMonthlyDetailDao;
import com.wolfking.jeesite.modules.sd.dao.OrderDao;
import com.wolfking.jeesite.modules.sd.entity.OrderCondition;
import com.wolfking.jeesite.modules.sd.entity.OrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class ChargeProcessService {
    @Resource
    private OrderDao orderDao;
    @Resource
    private CustomerFinanceDao customerFinanceDao;
    @Resource
    private ServicePointDao servicePointDao;
    @Resource
    private ServicePointPayableMonthlyDao servicePointPayableMonthlyDao;
    @Resource
    private ServicePointPayableMonthlyDetailDao servicePointPayableMonthlyDetailDao;
    @Resource
    private ServicePointBalanceMonthlyDao servicePointBalanceMonthlyDao;
    @Resource
    private ServicePointBalanceMonthlyDetailDao servicePointBalanceMonthlyDetailDao;

    @Transactional
    public void saveCreateCharge(HashMap<String, Object> map) throws RuntimeException{
        //修改订单对帐标记
        OrderCondition orderCondition = (OrderCondition) map.get("orderCondition");
        orderDao.updateChargeFlag(orderCondition);
        //修改订单状态
        OrderStatus orderStatus = (OrderStatus) map.get("orderStatus");
        orderDao.updateStatusFlagsFromCharge(orderStatus);
        //更新客户余额
        CustomerFinance customerFinance = (CustomerFinance) map.get("customerFinance");
        customerFinanceDao.updateBalanceFromInvoice(customerFinance);
        customerFinanceDao.updateAmountFromInvoice(customerFinance);

        List<ServicePointFinance> servicePointFinanceList = (List<ServicePointFinance>) map.get("servicePointFinanceList");
        List<ServicePointPayableMonthly> payableMonthlyList = (List<ServicePointPayableMonthly>) map.get("payableMonthlyList");
        List<ServicePointPayableMonthlyDetail> payableMonthlyDetailList = (List<ServicePointPayableMonthlyDetail>) map.get("payableMonthlyDetailList");
        for (int index = 0; index < servicePointFinanceList.size(); index++) {
            //更新网点余额
            ServicePointFinance servicePointFinance = servicePointFinanceList.get(index);
            servicePointDao.updateBalance(servicePointFinance);
            //更新应付款
            ServicePointPayableMonthly servicePointPayableMonthly = payableMonthlyList.get(index);
            servicePointPayableMonthlyDao.incrAmountForCharge(servicePointPayableMonthly);
            //更新应付款按品类
            ServicePointPayableMonthlyDetail payableDetail = payableMonthlyDetailList.get(index);
            servicePointPayableMonthlyDetailDao.incrAmountForCharge(payableDetail);
            //增加报表余额
            servicePointBalanceMonthlyDao.incrBalance(servicePointPayableMonthly);
            //增加报表余额按品类
            servicePointBalanceMonthlyDetailDao.incrBalance(payableDetail);
        }
    }
}
