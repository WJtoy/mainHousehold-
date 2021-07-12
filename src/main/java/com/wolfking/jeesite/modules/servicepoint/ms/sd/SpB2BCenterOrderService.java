package com.wolfking.jeesite.modules.servicepoint.ms.sd;

import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderStatusEnum;
import com.wolfking.jeesite.modules.md.entity.Engineer;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.b2bcenter.sd.service.B2BCenterOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class SpB2BCenterOrderService {


    @Autowired
    private B2BCenterOrderService b2BCenterOrderService;


    /**
     * B2B派单（APP接单、客户派单、网点派单）
     */
    public void planOrder(Order order, Engineer engineer, User updater, Date updateDate) {
        b2BCenterOrderService.planOrder(order, engineer, updater, updateDate);
    }

    /**
     * 网点派单（APP转派、网点Web派单）
     */
    public void servicePointPlanOrder(Order order, Engineer engineer, User updater, Date updateDate) {
        b2BCenterOrderService.servicePointPlanOrder(order, engineer, updater, updateDate);
    }

    /**
     * B2B上门服务
     */
    public void serviceOrder(Order order, Long servicePointId, Long engineerId, User updater, Date updateDate) {
        b2BCenterOrderService.serviceOrder(order, servicePointId, engineerId, updater, updateDate);
    }

    /**
     * B2B预约
     */
    public void pendingOrder(Order order, Long servicePointId, Long engineerId, Integer pendingType, Date appointmentDate, User updater, Date updateDate, String remarks) {
        b2BCenterOrderService.pendingOrder(order, servicePointId, engineerId, pendingType, appointmentDate, updater, updateDate, remarks);
    }
}
