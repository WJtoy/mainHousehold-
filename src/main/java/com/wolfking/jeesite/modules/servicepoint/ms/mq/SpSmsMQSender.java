package com.wolfking.jeesite.modules.servicepoint.ms.mq;

import com.kkl.kklplus.entity.sys.SysSMSTypeEnum;
import com.wolfking.jeesite.modules.mq.sender.sms.SmsMQSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class SpSmsMQSender {


    @Autowired
    private SmsMQSender smsMQSender;


    /**
     * 普通发送，增加短信类型字段
     */
    public void sendNew(String mobile, String content, String extNo, long sender, Long sendedAt, SysSMSTypeEnum type) {
        smsMQSender.sendNew(mobile, content, extNo, sender, sendedAt, type);
    }
}
