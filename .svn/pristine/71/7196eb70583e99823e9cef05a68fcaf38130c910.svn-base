package com.wolfking.jeesite.test.mq;

import com.wolfking.jeesite.modules.mq.dto.MQWebSocketMessage;
import com.wolfking.jeesite.modules.mq.sender.WSMessageSender;
import com.wolfking.jeesite.modules.sys.entity.Notice;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

/**
 * Created by Ryan on 2017/08/01.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class WSMessageTest {

    @Autowired
    WSMessageSender messageSender;


    @Test
    public void sendMessage(){
        MQWebSocketMessage.WebSocketMessage message = MQWebSocketMessage.WebSocketMessage.newBuilder()
                .setNoticeType(Notice.NOTICE_TYPE_FEEDBACK)
                .setTitle("test")
                .setContext("群发")
                .setTriggerBy(MQWebSocketMessage.User.newBuilder().setId(1).setName("admin").build())
                .setTriggerDate(new Date().getTime())
                .build();
        messageSender.send(message);
    }


}
