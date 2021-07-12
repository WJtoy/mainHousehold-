package com.wolfking.jeesite.test.fi;

import com.wolfking.jeesite.modules.md.dao.ServicePointDao;
import com.wolfking.jeesite.modules.md.entity.ServicePointFinance;
import com.wolfking.jeesite.modules.sd.entity.Order;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * Created by Jeff on 2017/4/21.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class ChargeTest {
    
    @Autowired
    private ServicePointDao servicePointDao;

    @Test
    public void chargeCreate(){
        Order order = new Order();
        order.getOrderStatus().setChargeDate(new Date());
    }

    @Test
    @Transactional()
    public void updateSelectTest(){
        ServicePointFinance servicePointFinance = servicePointDao.getAmounts(1l);
        log.info("\n\n==================== balance  " + servicePointFinance.getBalance() + "\n\n");
        servicePointFinance.setBalance(100);
        servicePointDao.updateBalance(servicePointFinance);
        servicePointFinance = servicePointDao.getAmounts(1l);
        log.info("\n\n==================== balance  " + servicePointFinance.getBalance() + "\n\n");
        throw new RuntimeException("test");
    }
}
