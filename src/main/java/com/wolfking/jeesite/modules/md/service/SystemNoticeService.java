package com.wolfking.jeesite.modules.md.service;

import com.wolfking.jeesite.common.service.LongIDCrudService;
import com.wolfking.jeesite.modules.md.dao.SystemNoticeDao;
import com.wolfking.jeesite.modules.md.entity.SystemNotice;
import com.wolfking.jeesite.ms.service.push.APPMessagePushService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;



/**
 * 网点通知Service
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class SystemNoticeService extends LongIDCrudService<SystemNoticeDao, SystemNotice> {


    @Autowired
    APPMessagePushService appMessagePushService;

    @Override
    @Transactional()
    public void save(SystemNotice servicePointNotice){
        super.save(servicePointNotice);
        appMessagePushService.broadcastToAllServicePoints(servicePointNotice.getTitle(), servicePointNotice.getContent(), servicePointNotice.getCreateDate());
    }

}
