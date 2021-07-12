package com.wolfking.jeesite.modules.servicepoint.ms.sys;

import com.wolfking.jeesite.ms.entity.AppPushMessage;
import com.wolfking.jeesite.ms.service.push.APPMessagePushService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class SpAPPMessagePushService {


    @Autowired
    private APPMessagePushService pushService;


    public void sendMessage(AppPushMessage message) {
        pushService.sendMessage(message);
    }
}
