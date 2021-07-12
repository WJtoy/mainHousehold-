package com.wolfking.jeesite.test.mq;

import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.utils.SequenceIdUtils;
import com.wolfking.jeesite.modules.mq.dto.MQOrderAutoPlanMessage;
import com.wolfking.jeesite.modules.mq.sender.OrderAutoPlanMessageSender;
import com.wolfking.jeesite.modules.mq.sender.ServicePointStationSender;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.ThreadLocalRandom;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@Slf4j
public class ServicePointStationSenderTest {

    @Autowired
    private ServicePointStationSender sevicePointStationSender;

    @Autowired
    private OrderAutoPlanMessageSender orderAutoPlanMessageSender;

    //@Test
//    public void addServicePointStation() throws Exception {
//        MQSyncServicePointStationMessage.SyncServicePointStationMessage syncServicePointStationMessage = MQSyncServicePointStationMessage.SyncServicePointStationMessage.newBuilder()
//                .setMessageId(generateMessageId())
//                .setSyncType(MQSyncType.SyncType.ADD)
//                .setStationId(100)
//                .setAreaId(1609)
//                .setStationName("测试服务点")
//                .setStationAddress("环城路228号")
//                .setLongitude(116.182558)
//                .setLatitude(39.751174)
//                .setServicePointId(13979)
//                .setServicePointNo("粤E0048（QD）")
//                .setName("袁海洋")
//                .setContactInfo1("13118811789")
//                .setPaymentType(20)
//                .setLevel(1)
//                .setAutoPlanFlag(1)
//                .build();
//
//        String json = new JsonFormat().printToString(syncServicePointStationMessage);
//        System.out.println(json);
//
//        MQSyncServicePointStationMessage.SyncServicePointStationMessage.Builder builder = MQSyncServicePointStationMessage.SyncServicePointStationMessage.newBuilder();
//        new JsonFormat().merge(new ByteArrayInputStream(json.getBytes()), builder);
//
//        MQSyncServicePointStationMessage.SyncServicePointStationMessage servicePointStationMessage = builder.build();
//        System.out.println("messageId:"+ servicePointStationMessage.getMessageId());
//        System.out.println("syncType:"+ servicePointStationMessage.getSyncType());
//        System.out.println("stationId:"+ servicePointStationMessage.getStationId());
//        System.out.println("stationName:"+ servicePointStationMessage.getStationName());
//        System.out.println("stationAddress:"+ servicePointStationMessage.getStationAddress());
//        System.out.println("longitude:"+ servicePointStationMessage.getLongitude());
//        System.out.println("latitude:"+ servicePointStationMessage.getLatitude());
//        System.out.println("servicePointId:" + servicePointStationMessage.getServicePointId());
//        System.out.println("servicePointNo:" + servicePointStationMessage.getServicePointNo());
//        System.out.println("name:" + servicePointStationMessage.getName());
//        System.out.println("contactInfo1:" + servicePointStationMessage.getContactInfo1());
//        System.out.println("paymentType: " + servicePointStationMessage.getPaymentType());
//        System.out.println("level: " + servicePointStationMessage.getLevel());
//        System.out.println("autoPlanFlag: " + servicePointStationMessage.getAutoPlanFlag());
//
//        sevicePointStationSender.sendRetry(syncServicePointStationMessage);
//    }
//
//    @Test
//    public void updateServicePointStation() throws Exception {
//        MQSyncServicePointStationMessage.SyncServicePointStationMessage syncServicePointStationMessage = MQSyncServicePointStationMessage.SyncServicePointStationMessage.newBuilder()
//                .setMessageId(generateMessageId())
//                .setSyncType(MQSyncType.SyncType.UPDATE)
//                .setStationId(100)
//                .setAreaId(1609)
//                .setStationName("来客服务点")
//                .setStationAddress("环城路500号")
//                .setLongitude(116.182558)
//                .setLatitude(39.751174)
//                .setServicePointId(13979)
//                .setServicePointNo("粤E0048（QD）")
//                .setName("袁海洋")
//                .setContactInfo1("13118811789")
//                .setPaymentType(10)
//                .setLevel(1)
//                .setAutoPlanFlag(0)
//                .build();
//
//        String json = new JsonFormat().printToString(syncServicePointStationMessage);
//        System.out.println(json);
//
//        MQSyncServicePointStationMessage.SyncServicePointStationMessage.Builder builder = MQSyncServicePointStationMessage.SyncServicePointStationMessage.newBuilder();
//        new JsonFormat().merge(new ByteArrayInputStream(json.getBytes()), builder);
//
//        MQSyncServicePointStationMessage.SyncServicePointStationMessage servicePointStationMessage = builder.build();
//        System.out.println("messageId:"+ servicePointStationMessage.getMessageId());
//        System.out.println("syncType:"+ servicePointStationMessage.getSyncType());
//        System.out.println("stationId:"+ servicePointStationMessage.getStationId());
//        System.out.println("stationName:"+ servicePointStationMessage.getStationName());
//        System.out.println("stationAddress:"+ servicePointStationMessage.getStationAddress());
//        System.out.println("longitude:"+ servicePointStationMessage.getLongitude());
//        System.out.println("latitude:"+ servicePointStationMessage.getLatitude());
//        System.out.println("servicePointId:" + servicePointStationMessage.getServicePointId());
//        System.out.println("servicePointNo:" + servicePointStationMessage.getServicePointNo());
//        System.out.println("name:" + servicePointStationMessage.getName());
//        System.out.println("contactInfo1:" + servicePointStationMessage.getContactInfo1());
//        System.out.println("paymentType: " + servicePointStationMessage.getPaymentType());
//        System.out.println("level: " + servicePointStationMessage.getLevel());
//        System.out.println("autoPlanFlag: " + servicePointStationMessage.getAutoPlanFlag());
//
//        sevicePointStationSender.sendRetry(syncServicePointStationMessage);
//    }
//
//    //@Test
//    public void deleteServicePointStation() throws Exception {
//        MQSyncServicePointStationMessage.SyncServicePointStationMessage syncServicePointStationMessage = MQSyncServicePointStationMessage.SyncServicePointStationMessage.newBuilder()
//                .setMessageId(generateMessageId())
//                .setSyncType(MQSyncType.SyncType.DELETE)
//                .setStationId(100)
////                .setAreaId(13456)
////                .setStationName("测试服务点")
////                .setStationAddress("")
////                .setLongitude(12.333)
////                .setLatitude(2222)
////                .setServicePointId(13979)
////                .setServicePointNo("粤E0048（QD）")
////                .setName("袁海洋")
////                .setContactInfo1("13118811789")
////                .setPaymentType(10)
////                .setLevel(1)
////                .setAutoPlanFlag(1)
//                .build();
//
//        String json = new JsonFormat().printToString(syncServicePointStationMessage);
//        System.out.println(json);
//
//        MQSyncServicePointStationMessage.SyncServicePointStationMessage.Builder builder = MQSyncServicePointStationMessage.SyncServicePointStationMessage.newBuilder();
//        new JsonFormat().merge(new ByteArrayInputStream(json.getBytes()), builder);
//
//        MQSyncServicePointStationMessage.SyncServicePointStationMessage servicePointStationMessage = builder.build();
//        System.out.println("messageId:"+ servicePointStationMessage.getMessageId());
//        System.out.println("syncType:"+ servicePointStationMessage.getSyncType());
//        System.out.println("stationId:"+ servicePointStationMessage.getStationId());
////        System.out.println("stationName:"+ servicePointStationMessage.getStationName());
////        System.out.println("stationAddress:"+ servicePointStationMessage.getStationAddress());
////        System.out.println("longitude:"+ servicePointStationMessage.getLongitude());
////        System.out.println("latitude:"+ servicePointStationMessage.getLatitude());
////        System.out.println("servicePointId:" + servicePointStationMessage.getServicePointId());
////        System.out.println("servicePointNo:" + servicePointStationMessage.getServicePointNo());
////        System.out.println("name:" + servicePointStationMessage.getName());
////        System.out.println("contactInfo1:" + servicePointStationMessage.getContactInfo1());
////        System.out.println("paymentType: " + servicePointStationMessage.getPaymentType());
////        System.out.println("level: " + servicePointStationMessage.getLevel());
////        System.out.println("autoPlanFlag: " + servicePointStationMessage.getAutoPlanFlag());
//
//        sevicePointStationSender.send(syncServicePointStationMessage);
//    }

    public Long generateMessageId() {
        //随机，防止同用户产生重复id
        int workerId = ThreadLocalRandom.current().nextInt(32);
        int datacenterId = ThreadLocalRandom.current().nextInt(32);
        SequenceIdUtils sequence = new SequenceIdUtils(workerId,datacenterId);
        return sequence.nextId();
    }

    @Test
    public void sendAutoPlanMessage(){
        MQOrderAutoPlanMessage.OrderAutoPlan message = MQOrderAutoPlanMessage.OrderAutoPlan.newBuilder()
                .setOrderId(1229699345268477952L)
                .setQuarter("20201")
                .setOrderNo("K2020021857822")
                .setCreateBy(1L)
                .setCreator("管理员")
                .setCreateAt(1582018145000L)
                .setAreaId(3403)
                .setAreaName("广东省 深圳市 龙华区 观湖街道")
                .setServiceAddress("广东省 深圳市 龙华区 观湖街道 测试地址")
                .setAreaRadius(MQOrderAutoPlanMessage.AreaRadius.newBuilder()
                        .setRadius1(0)
                        .setRadius2(0)
                        .setRadius3(0)
                        .build())
                .setLongitude(0.00)
                .setLatitude(0.00)
                .build();
        //if(canAutoPlan(message)) {
        try {
            orderAutoPlanMessageSender.sendRetry(message, 5000, 0);
        }catch (Exception e){
            log.error("自动派单入队错误，body:{}",new JsonFormat().printToString(message),e);
        }
    }
}
