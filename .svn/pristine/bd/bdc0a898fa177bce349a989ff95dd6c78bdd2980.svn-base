package com.wolfking.jeesite.modules.mq.service;

import com.wolfking.jeesite.common.service.LongIDCrudService;
import com.wolfking.jeesite.modules.mq.dao.OrderChargeDao;
import com.wolfking.jeesite.modules.mq.entity.OrderCharge;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * Created by Jeff on 2017/6/16.
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class OrderChargeService extends LongIDCrudService<OrderChargeDao, OrderCharge> {

    public List<OrderCharge> selectRetryList(Date startDate, Date endDate, Integer count){
        return dao.selectRetryList(startDate,endDate,count);
    }
}
