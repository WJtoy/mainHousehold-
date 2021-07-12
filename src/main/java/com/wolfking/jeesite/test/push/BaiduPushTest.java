package com.wolfking.jeesite.test.push;

import com.wolfking.jeesite.modules.mq.sender.PushMessageSender;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BaiduPushTest {
    @Autowired
    PushMessageSender pushMessageSender;

    @Test
    public void pushMessageTest(){
        pushMessage();
    }

    private void pushMessage(){
        for (int i = 10; i< 21; i ++) {
//            MQPushMessage.PushMessage.Builder pushMessage = MQPushMessage.PushMessage.newBuilder();
//            pushMessage.setUserId(50760);
//            pushMessage.setPushMessageType(MQPushMessage.PushMessageType.Notification);
//            pushMessage.setMessageType(1);
//            pushMessage.setSubject("test subject");
//            pushMessage.setDescription("test description");
//            pushMessage.setContent("test content");
//            pushMessage.setTimestamp(System.currentTimeMillis());
//            pushMessageSender.send(pushMessage.build());
        }
    }
}
