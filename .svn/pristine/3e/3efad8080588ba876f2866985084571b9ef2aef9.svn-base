package com.wolfking.jeesite.test.mq;

import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.entity.sys.mq.MQSysShortMessage;
import com.kkl.kklplus.entity.voiceservice.mq.MQSmsCallbackMessage;
import com.wolfking.jeesite.modules.mq.dto.MQShortMessage;
import com.wolfking.jeesite.modules.mq.sender.ShortMessageSender;
import com.wolfking.jeesite.modules.mq.sender.sms.SmsMQSender;
import com.wolfking.jeesite.modules.td.service.MessageService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

/**
 * Created by Ryan on 2017/08/01.
 */
//@RunWith(SpringRunner.class)
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@ActiveProfiles("dev")
public class MQShortMessageTest {

    @Autowired
    SmsMQSender smsMQSender;

    @Autowired
    MessageService messageService;

    @Test
    public void sendMessage(){
        MQSysShortMessage.SysShortMessage message = MQSysShortMessage.SysShortMessage.newBuilder()
                .setMobile("13760468206")
                .setSendTime(new Date().getTime())
                //.setContent("石师傅,现有: 赵辉13760468206 河东区 大桥道萦东花园8号楼4门602九阳燃气灶1台 需要安装，请2小时内联系用户确认安维环境并预约上门时间，务必48小时内上门，严禁对产品作任何评价，带齐相应的工具和配件，现场有问题请联系客服林小姐0757-26169180/4006663653")
                .setContent("净师傅,现有: 赵辉13760468206 河东区 大桥道萦东花园8号楼4门602九阳燃气灶1台 需要安装")
                .setId(0l)
                .setExtNo("")
                .setTriggerBy(1l)
                .setTriggerDate(new Date().getTime())
                .setType("pt")
                .build();
        smsMQSender.send(message);
    }

    @Test
    public void sendTemplateMessage(){
        MQSysShortMessage.SysShortMessage message = MQSysShortMessage.SysShortMessage.newBuilder()
                .setMobile("13760468206")
                .setSendTime(new Date().getTime())
                .setTemplateCode("55622")
                .setParams("夏先生")
                .setId(0l)
                .setExtNo("")
                .setTriggerBy(1l)
                .setTriggerDate(new Date().getTime())
                .setType("pt")
                .build();
        smsMQSender.send(message);
    }

    @Test
    public void parseMessage(){
        //原始内容
        String msgString = "EgsxNTgyMDQ3MjgyMBpF5ZGo5biI5YKF77yM5pyJ5paw5Y2V5rS+57uZ5oKo77yM6K+35Y+K5pe25omT5byAQVBQ6L+b6KGM5p+l55yL5aSE55CGIgJwdDj+" +
                "uKb6nC5YAWDtoJj6nC5wFA==";
        //base64解密
        byte[] baseBytes = Base64.getDecoder().decode(msgString);
        //装载到流
        InputStream is = new ByteArrayInputStream(baseBytes);
        try {
            //转换
            MQShortMessage.ShortMessage callbackEntity = MQShortMessage.ShortMessage.parseFrom(is);
            if(callbackEntity == null){
                System.out.println("convert message error ,is null");
            }
            String json = new JsonFormat().printToString(callbackEntity);
            System.out.println(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
