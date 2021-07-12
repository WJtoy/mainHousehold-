package com.wolfking.jeesite.modules.servicepoint.ms.receipt;

import com.kkl.kklplus.entity.cc.AbnormalForm;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.cc.service.AbnormalFormService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


/**
 * 网点催单
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class SpAbnormalFormService {


    @Autowired
    private AbnormalFormService abnormalFormService;


    /**
     * 组装异常单信息
     *
     * @param reason   异常原因
     * @param channel  来源
     * @param formType 异常类型
     * @param subType  子类型
     * @param remarks  描述
     */
    public AbnormalForm handleAbnormalForm(Order order, String reason, User user, Integer channel, Integer formType, Integer subType, String remarks) {
        return abnormalFormService.handleAbnormalForm(order, reason, user, channel, formType, subType, remarks);
    }

    /**
     * 添加异常单
     */
    public void save(AbnormalForm abnormalForm) {
        abnormalFormService.save(abnormalForm);
    }
}
