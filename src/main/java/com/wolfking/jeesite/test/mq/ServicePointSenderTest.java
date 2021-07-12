package com.wolfking.jeesite.test.mq;


import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.entity.es.mq.MQSyncServicePointMessage;
import com.kkl.kklplus.entity.es.mq.MQSyncType;
import com.kkl.kklplus.utils.SequenceIdUtils;
import com.wolfking.jeesite.modules.mq.sender.ServicePointSender;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class ServicePointSenderTest {
    @Autowired
    private ServicePointSender servicePointSender;


//    public void addServicePoint() throws Exception {
//        com.kkl.kklplus.entity.es.mq.MQSyncServicePointMessage.SyncServicePointMessage servicePointSyncMessage =
//                com.kkl.kklplus.entity.es.mq.MQSyncServicePointMessage.SyncServicePointMessage.newBuilder()
//                        .setMessageId(1000L)
//                        .setSyncType(MQSyncType.SyncType.ADD)
//                        .setServicePointId(13979)
//                        .setServicePointNo("粤E0048（QD）")
//                        .setName("袁海洋")
//                        .setContactInfo1("13118811789")
//                        .setPaymentType(10)
//                        .setLevel(1)
//                        .setAutoPlanFlag(1)
//                        .build();
//
//        String json = new JsonFormat().printToString(servicePointSyncMessage);
//        System.out.println(json);
//
//        MQSyncServicePointMessage.SyncServicePointMessage.Builder builder = MQSyncServicePointMessage.SyncServicePointMessage.newBuilder();
//        new JsonFormat().merge(new ByteArrayInputStream(json.getBytes()), builder);
//
//        MQSyncServicePointMessage.SyncServicePointMessage syncServicePointMessage = builder.build();
//        System.out.println("messageId:"+ syncServicePointMessage.getMessageId());
//        System.out.println("syncType:"+ syncServicePointMessage.getSyncType());
//        System.out.println("servicePointId:" + syncServicePointMessage.getServicePointId());
//        System.out.println("servicePointNo:" + syncServicePointMessage.getServicePointNo());
//        System.out.println("name:" + syncServicePointMessage.getName());
//        System.out.println("contactInfo1:" + syncServicePointMessage.getContactInfo1());
//        System.out.println("paymentType: " + syncServicePointMessage.getPaymentType());
//        System.out.println("level: " + syncServicePointMessage.getLevel());
//        System.out.println("autoPlanFlag: " + syncServicePointMessage.getAutoPlanFlag());

        //servicePointSender.send(MQServicePointSyncMessage);

//    }

    @Test
    public void updateServicePoint() throws Exception {
        com.kkl.kklplus.entity.es.mq.MQSyncServicePointMessage.SyncServicePointMessage servicePointSyncMessage =
                com.kkl.kklplus.entity.es.mq.MQSyncServicePointMessage.SyncServicePointMessage.newBuilder()
                        .setMessageId(generateMessageId())
                        .setSyncType(MQSyncType.SyncType.UPDATE)
                        .setServicePointId(1622038)
                        .setPaymentType(20)
                        .setAutoPlanFlag(1)
                        .build();

        String json = new JsonFormat().printToString(servicePointSyncMessage);
        System.out.println(json);

        MQSyncServicePointMessage.SyncServicePointMessage.Builder builder = MQSyncServicePointMessage.SyncServicePointMessage.newBuilder();
        new JsonFormat().merge(new ByteArrayInputStream(json.getBytes()), builder);

        MQSyncServicePointMessage.SyncServicePointMessage syncServicePointMessage = builder.build();
        System.out.println("messageId:"+ syncServicePointMessage.getMessageId());
        System.out.println("syncType:"+ syncServicePointMessage.getSyncType());
        System.out.println("servicePointId:" + syncServicePointMessage.getServicePointId());
        System.out.println("servicePointNo:" + syncServicePointMessage.getServicePointNo());
        System.out.println("name:" + syncServicePointMessage.getName());
        System.out.println("contactInfo1:" + syncServicePointMessage.getContactInfo1());
        System.out.println("paymentType: " + syncServicePointMessage.getPaymentType());
        System.out.println("level: " + syncServicePointMessage.getLevel());
        System.out.println("autoPlanFlag: " + syncServicePointMessage.getAutoPlanFlag());

        servicePointSender.sendRetry(servicePointSyncMessage);

    }

    //@Test
    public void deleteServicePoint() throws Exception {
        com.kkl.kklplus.entity.es.mq.MQSyncServicePointMessage.SyncServicePointMessage servicePointSyncMessage =
                com.kkl.kklplus.entity.es.mq.MQSyncServicePointMessage.SyncServicePointMessage.newBuilder()
                        .setMessageId(generateMessageId())
                        .setSyncType(MQSyncType.SyncType.DELETE)
                        .setServicePointId(13979)
                        .build();

        String json = new JsonFormat().printToString(servicePointSyncMessage);
        System.out.println(json);

        MQSyncServicePointMessage.SyncServicePointMessage.Builder builder = MQSyncServicePointMessage.SyncServicePointMessage.newBuilder();
        new JsonFormat().merge(new ByteArrayInputStream(json.getBytes()), builder);

        MQSyncServicePointMessage.SyncServicePointMessage syncServicePointMessage = builder.build();
        System.out.println("messageId:"+ syncServicePointMessage.getMessageId());
        System.out.println("syncType:"+ syncServicePointMessage.getSyncType());
        System.out.println("servicePointId:" + syncServicePointMessage.getServicePointId());

        //servicePointSender.send(MQServicePointSyncMessage);
    }

    public Long generateMessageId() {
        //随机，防止同用户产生重复id
        int workerId = ThreadLocalRandom.current().nextInt(32);
        int datacenterId = ThreadLocalRandom.current().nextInt(32);
        SequenceIdUtils sequence = new SequenceIdUtils(workerId,datacenterId);
        return sequence.nextId();
    }


}
