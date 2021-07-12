package com.wolfking.jeesite.modules.servicepoint.ms.mq;

import com.wolfking.jeesite.modules.mq.sender.sms.SmsCallbackTaskMQSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class SpSmsCallbackTaskMQSender {


    @Autowired
    private SmsCallbackTaskMQSender smsCallbackTaskMQSender;


    /**
     * 普通发送
     */
    public void send(Long orderId, String quarter, String mobile, String content, String templateCode, String params, String extNo, long sender, Long sendedAt) {
        smsCallbackTaskMQSender.send(orderId, quarter, mobile, content, templateCode, params, extNo, sender, sendedAt);
    }
}
