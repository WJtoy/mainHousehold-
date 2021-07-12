package com.wolfking.jeesite.modules.servicepoint.ms.receipt;

import com.wolfking.jeesite.modules.sd.entity.OrderOpitionTrace;
import com.wolfking.jeesite.modules.sd.service.OrderOpitionTraceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


/**
 * 网点催单
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class SpOrderOpitionTraceService {


    @Autowired
    private OrderOpitionTraceService opitionTraceService;


    /**
     * 新增App反馈
     */
    @Transactional(readOnly = false)
    public void insert(OrderOpitionTrace feedback) throws Exception {
        opitionTraceService.insert(feedback);
    }
}
