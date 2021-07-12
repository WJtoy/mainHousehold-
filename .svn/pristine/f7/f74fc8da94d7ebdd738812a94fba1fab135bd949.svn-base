package com.wolfking.jeesite.modules.fi.service;

import com.wolfking.jeesite.common.service.LongIDCrudService;
import com.wolfking.jeesite.modules.fi.dao.EngineerChargeMasterDao;
import com.wolfking.jeesite.modules.fi.entity.EngineerChargeMaster;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 网点对帐总记录操作-按工单，网点
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class EngineerChargeMasterService extends LongIDCrudService<EngineerChargeMasterDao, EngineerChargeMaster> {
    /**
     * 根据ID查询工单级别费用
     * @param orderId
     * @param servicePointId
     * @return
     */
    public EngineerChargeMaster findOrderLevelFee(Long orderId, Long servicePointId) {
        return dao.findOrderLevelFee(orderId, servicePointId);
    }
}
