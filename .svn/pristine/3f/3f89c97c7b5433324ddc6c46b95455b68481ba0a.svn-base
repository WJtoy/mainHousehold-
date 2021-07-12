package com.wolfking.jeesite.test.ms.tmall.sd;

import com.google.common.collect.Lists;
import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.entity.b2b.pb.MQTmallAnomalyRecourseMessage;
import com.kkl.kklplus.entity.b2b.pb.MQTmallServiceMonitorMessageMessage;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.entity.b2bcenter.pb.MQB2BOrderMessage;
import com.kkl.kklplus.entity.voiceservice.mq.MQSmsCallbackMessage;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.ms.b2bcenter.mq.sender.B2BCenterAnomalyRecourseMQSender;
import com.wolfking.jeesite.ms.b2bcenter.mq.sender.B2BCenterServiceMonitorMQSender;
import com.wolfking.jeesite.ms.b2bcenter.mq.sender.B2BOrderMQSender;
import com.wolfking.jeesite.ms.tmall.mq.service.MqB2bTmallLogService;
import lombok.extern.slf4j.Slf4j;
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
import java.util.ArrayList;
import java.util.Base64;

/**
 * Created by Ryan on 2018/08/07
 * B2B订单消息测试类
 */
//@RunWith(SpringRunner.class)
@RunWith(SpringJUnit4ClassRunner.class)
@Slf4j
@SpringBootTest
@ActiveProfiles("dev")
public class MQB2BOrderTest {

    @Autowired
    private B2BOrderMQSender b2BOrderMQSender;

    @Autowired
    private B2BCenterAnomalyRecourseMQSender anomalyRecourseMQSender;

    @Autowired
    private B2BCenterServiceMonitorMQSender serviceMonitorMQSender;

    @Autowired
    private MqB2bTmallLogService mqB2bTmallLogService;


    @Test
    public void testRetrySendB2BOrderStatusUpdateMessage() {
//        mqB2bTmallLogService.retrySendB2BOrderStatusUpdateMessage();
    }



    /**
     * 测试发送B2B自动转单订单
     */
    @Test
    public void testSendCanBoOrderMessage(){
        /* 1.天猫 */
        MQB2BOrderMessage.B2BOrderMessage message = MQB2BOrderMessage.B2BOrderMessage.newBuilder()
                .setDataSource(B2BDataSourceEnum.TMALL.id)
                .setOrderNo("20200810003")
                .setParentBizOrderId("202008100020003")
                .setShopId("500295137")
                .setUserName("周生生")
                .setIssueBy("")
                .setUserMobile("13800000087")
                .setUserPhone("")
                .setUserAddress("广东省 深圳市 龙华区 龙华街道 龙观东路")
                .setServiceType("sendAndInstall")
                .setWarrantyType("保内")
                .setStatus(1)
                .setBrand("Setir/森太")
                .setProcessFlag(0)
                .setProcessTime(0)
                .setProcessComment("")
                .setQuarter("20203")
                .setDescription("天猫自动转单测试")
                .addB2BOrderItem(MQB2BOrderMessage.B2BOrderItem.newBuilder()
                        .setProductCode("350511") //category
                        .setProductName("Setir/森太 CXW-368-B530QV油烟机侧吸式双电机吸油烟机正品特价")
                        .setProductSpec("CXW-368-B530QV")
                        .setWarrantyType("保内")
                        .setServiceType("sendAndInstall")
                        .setQty(1)
                        .addPics("https://static.chiphell.com/forum/202007/01/173119mefxerbvfz9v99ff.jpg")
                        .build())
                .build();
        b2BOrderMQSender.send(message);

        /* 2.康宝
        MQB2BOrderMessage.B2BOrderMessage message = MQB2BOrderMessage.B2BOrderMessage.newBuilder()
                .setDataSource(B2BDataSourceEnum.CANBO.id)
                .setOrderNo("MD2018102700003")
                .setShopId("CANBO")
                .setUserName("柳生")
                .setIssueBy("负责人")
                .setUserMobile("14512345671")
                .setUserPhone("14512345671")
                .setUserAddress("羊流镇羊流镇苏庄24队1组10号")
                .setServiceType("安装")
                .setWarrantyType("保内")
                .setStatus(1)
                .setBrand("康宝")
                .setProcessFlag(1)
                .setProcessTime(1)
                .setProcessComment("此单为人工测试发送消息")
                .setQuarter("20184")
                .setDescription("康宝自动转单测试")
                //.addB2BOrderItem(MQB2BOrderMessage.B2BOrderItem.newBuilder().setProductCode("A100")
                //        .setProductName("消毒柜")
                //        .setQty(2)
                //        .build())
                .addB2BOrderItem(
                    MQB2BOrderMessage.B2BOrderItem
                            .newBuilder()
                            .setProductCode("B187")
                            .setProductName("油烟机")
                            .setProductSpec("型号120W")
                            .setQty(1)
                            .build())
                .build();
        b2BOrderMQSender.send(message);
        */
    }

    /**
     * 测试发送天猫一键求助消息
     */
    @Test
    public void testSendTmallAnomalyRecourseMessage(){
        MQTmallAnomalyRecourseMessage.TmallAnomalyRecourseMessage.Builder builder =  MQTmallAnomalyRecourseMessage.TmallAnomalyRecourseMessage.newBuilder();
        builder.setAnomalyRecourseId(2019031532419l)
                .setOrderId(1106469039854718977l)
                .setQuarter("20191")
                .setQuestionType("001")
                .setStatus(0)
                .setServiceCode("安装")
                .setSubmitTime(DateUtils.getTimestamp())
                //.addRecourseMessageList(MQTmallAnomalyRecourseMessage.RecourseMessage.newBuilder()
                //        .setRecourseText("未安装1")
                //        .setSubmitTime(DateUtils.getTimestamp())
                //        .addAllImageUrls(Lists.newArrayList("https://static.chiphell.com/portal/201808/18/094141cyig8d4izdzin459.jpg","https://f1cdn.wstx.com/uploadfile/2018/0917/20180917063715131.jpg"))
                //        .build())
                .addRecourseMessageList(MQTmallAnomalyRecourseMessage.RecourseMessage.newBuilder()
                        .setRecourseText("ShortCut S 特质键盘")
                        .setSubmitTime(DateUtils.getTimestamp())
                        .addAllImageUrls(Lists.newArrayList("https://making-photos.b0.upaiyun.com/photos/69091ef7fe513f2c8914442b09e097d0.jpeg!middle","https://making-photos.b0.upaiyun.com/photos/1416929b3f1f72fbbf10e64e3c62007b.jpg!middle","https://making-photos.b0.upaiyun.com/photos/7c2ae9dfcbbc689a41ebdd40dd996920.jpg!middle"))
                        .build());
        MQTmallAnomalyRecourseMessage.TmallAnomalyRecourseMessage  message = builder.build();
        anomalyRecourseMQSender.send(message);
    }

    /**
     * 测试发送天猫预警消息
     */
    @Test
    public void testSendTmallServiceMonitorMessage(){
        MQTmallServiceMonitorMessageMessage.TmallServiceMonitorMessageMessage.Builder builder =  MQTmallServiceMonitorMessageMessage.TmallServiceMonitorMessageMessage.newBuilder();
        builder.setMonitorId(2019031532419l)
                .setOrderId(1106469039854718977l)
                .setQuarter("20191")
                .setRuleId("003")
                .setServiceCode("安装")
                .setLevel(1)
                .setGmtCreate("2019-03-15 16:10:00")
                .setContent("请及时处理即将超出6小时时未回传工人信息的服务工单，详情如下：父订单编号：XXX，服务子订单：XXX，服务工单号：XXX。")
                .setBizId(1l)
                .setBizType(1);
        MQTmallServiceMonitorMessageMessage.TmallServiceMonitorMessageMessage  message = builder.build();
        serviceMonitorMQSender.send(message);
    }

    /**
     * 生成天猫预警消息体，使用RabitMq management发送  失败
     */
    @Test
    public void testTmallServiceMonitorMessage(){
        MQTmallServiceMonitorMessageMessage.TmallServiceMonitorMessageMessage.Builder builder =  MQTmallServiceMonitorMessageMessage.TmallServiceMonitorMessageMessage.newBuilder();
        builder.setMonitorId(2019031532419l)
                .setOrderId(1106469039854718977l)
                .setQuarter("20191")
                .setRuleId("003")
                .setServiceCode("安装")
                .setLevel(1)
                .setGmtCreate("2019-03-15 16:10:00")
                .setContent("请及时处理即将超出6小时时未回传工人信息的服务工单，详情如下：父订单编号：XXX，服务子订单：XXX，服务工单号：XXX。")
                .setBizId(1l)
                .setBizType(1);
        MQTmallServiceMonitorMessageMessage.TmallServiceMonitorMessageMessage  message = builder.build();
        byte[] bytes = message.toByteArray();
        String msg = Base64.getEncoder().encodeToString(bytes);
        System.out.println(msg);
        /*
        String msgString = "CIGgiMzuo7OdDxIFMjAxOTEaATEgASoq55So5oi35Zue5aSNOiAx77yM5a+55biI5YKF55qE5pyN5Yqh5ruh5oSPOImbtZaULQ==";
        byte[] baseBytes = Base64.getDecoder().decode(msgString);
        InputStream is = new ByteArrayInputStream(baseBytes);
        try {
            MQSmsCallbackMessage.SmsCallbackEntity callbackEntity = MQSmsCallbackMessage.SmsCallbackEntity
                    .parseFrom(is);
            if(callbackEntity == null){
                System.out.println("convert message error ,is null");
            }
            String json = new JsonFormat().printToString(callbackEntity);
            System.out.println(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
        */
    }


}
