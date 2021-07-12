package com.wolfking.jeesite.test.mq;

import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.entity.b2bcenter.pb.MQB2BOrderComplainMessage;
import com.kkl.kklplus.entity.b2bcenter.pb.MQB2BOrderStatusUpdateMessage;
import com.kkl.kklplus.entity.fi.mq.MQRechargeOrderMessage;
import com.kkl.kklplus.entity.lm.mq.MQLMExpress;
import com.kkl.kklplus.entity.praise.dto.MQPraiseMessage;
import com.kkl.kklplus.entity.sys.mq.MQSysLogMessage;
import com.kkl.kklplus.entity.voiceservice.OperateType;
import com.kkl.kklplus.entity.voiceservice.mq.MQSmsCallbackMessage;
import com.kkl.kklplus.entity.voiceservice.mq.MQVoiceSeviceMessage;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.mq.dto.MQCreateOrderPushMessage;
import com.wolfking.jeesite.modules.mq.dto.MQCustomer;
import com.wolfking.jeesite.modules.mq.dto.MQOrderAutoComplete;
import com.wolfking.jeesite.modules.mq.dto.MQOrderReport;
import com.wolfking.jeesite.modules.mq.sender.CreateOrderPushMessageSender;
import com.wolfking.jeesite.modules.mq.sender.OrderAutoCompleteDelaySender;
import com.wolfking.jeesite.modules.mq.sender.OrderAutoCompleteSender;
import com.wolfking.jeesite.modules.mq.sender.OrderReportSender;
import com.wolfking.jeesite.modules.mq.sender.voice.NewTaskMQSender;
import com.wolfking.jeesite.modules.mq.sender.voice.OperateTaskMQSender;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.ms.b2bcenter.mq.sender.B2BCenterOrderComplainMQSender;
import com.wolfking.jeesite.ms.praise.mq.sender.PraiseFormMQRetrySender;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Date;

/**
 * Created by Ryan on 2017/08/01.
 */
@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@ActiveProfiles("dev")
public class MQTest {

    @Autowired
    OrderReportSender orderReportSender;

    @Autowired
    OrderAutoCompleteDelaySender orderAutoCompleteDelaySender;

    @Autowired
    private NewTaskMQSender newTaskMQSender;

    @Autowired
    private OperateTaskMQSender operateTaskMQSender;

    @Autowired
    private CreateOrderPushMessageSender createOrderPushMessageSender;

    @Autowired
    private PraiseFormMQRetrySender praiseFormMQRetrySender;

    @Autowired
    private B2BCenterOrderComplainMQSender complainMQSender;

    //region 订单报表消息测试
    /**
     * 创建
     */
    @Test
    public void sendCreateOrderMessage(){
        //下单
        MQOrderReport.OrderReport message = MQOrderReport.OrderReport.newBuilder()
                .setOrderId(891639549665611776l)
                .setQuarter("20173")
                .setOrderType(Order.ORDER_STATUS_APPROVED)
                .setAmount(110.0)
                .setTriggerDate(DateUtils.parseDate("2017-07-30 20:40:09").getTime())
                .setTriggerBy(1)
                .setKefu(MQOrderReport.Kefu.newBuilder()
                        .setId(15)
                        .setName("客服15")
                        .build()
                )
                .setCustomer(MQCustomer.Customer.newBuilder()
                        .setId(1058)
                        .setName("002优盟电器2")
                        .setSalesName("宋娟1")
                        .setPaymentType(0)
                        .setPaymentTypeName("无")
                        .build()
                )
                .setProvinceId(3)
                .setProvinceName("天津市")
                .setCityId(35)
                .setCityName("天津市")
                .setAreaId(577)
                .setAreaName("北辰区")
                .setQty(1)
                .build();
        orderReportSender.send(message);
    }

    /**
     * 接单
     */
    @Test
    public void sendAcceptOrderMessage(){
        //接单
        MQOrderReport.OrderReport message = MQOrderReport.OrderReport.newBuilder()
                .setOrderId(891639549665611776l)
                .setQuarter("20173")
                .setOrderType(Order.ORDER_STATUS_ACCEPTED)
                .setAmount(110.0)
                .setTriggerDate(DateUtils.parseDate("2017-07-30 20:40:29").getTime())
                .setTriggerBy(1)
                .setKefu(MQOrderReport.Kefu.newBuilder()
                        .setId(15)
                        .setName("客服15")
                        .build()
                )
                .setQty(1)
                .build();
        orderReportSender.send(message);
    }

    /**
     * 退单
     */
    @Test
    public void sendReturnOrderMessage(){
        //退单
        MQOrderReport.OrderReport message = MQOrderReport.OrderReport.newBuilder()
                .setOrderId(891639549665611776l)
                .setQuarter("20173")
                .setOrderType(Order.ORDER_STATUS_RETURNED)
                .setAmount(110.0)
                .setTriggerDate(DateUtils.parseDate("2017-07-30 20:45:29").getTime())
                .setTriggerBy(1)
                .setQty(1)
                .setAmount(110.00)
                .setCustomer(MQCustomer.Customer.newBuilder()
                        .setId(1058)
                        .setName("002优盟电器2")
                        .setSalesName("宋娟1")
                        .setPaymentType(0)
                        .setPaymentTypeName("无")
                        .build()
                )
                .setKefu(MQOrderReport.Kefu.newBuilder()
                        .setId(15)
                        .setName("客服15")
                        .build()
                )
                .build();
        orderReportSender.send(message);
    }

    /**
     * 取消
     */
    @Test
    public void sendCancelOrderMessage(){
        //客户取消订单
        MQOrderReport.OrderReport message = MQOrderReport.OrderReport.newBuilder()
                .setOrderId(891639549665611776l)
                .setQuarter("20173")
                .setOrderType(Order.ORDER_STATUS_CANCELED)
                .setAmount(110.0)
                .setTriggerDate(DateUtils.parseDate("2017-07-30 20:45:29").getTime())
                .setTriggerBy(1)
                .setQty(1)
                .setAmount(110.00)
                .setCustomer(MQCustomer.Customer.newBuilder()
                        .setId(1058)
                        .setName("002优盟电器2")
                        .setSalesName("宋娟1")
                        .setPaymentType(0)
                        .setPaymentTypeName("无")
                        .build()
                )
                .setKefu(MQOrderReport.Kefu.newBuilder()
                        .setId(15)
                        .setName("客服15")
                        .build()
                )
                .build();
        orderReportSender.send(message);
    }

    /**
     * 完成
     */
    @Test
    public void sendFinishGradeMessage(){
        //客评，更新客服报表数据
        MQOrderReport.OrderReport message = MQOrderReport.OrderReport.newBuilder()
                .setOrderId(891639549665611776l)
                .setOrderType(Order.ORDER_STATUS_COMPLETED)
                .setAmount(0.0)
                .setTriggerDate(DateUtils.parseDate("2017-07-30 20:45:29").getTime())
                .setTriggerBy(1)
                .setKefu(MQOrderReport.Kefu.newBuilder()
                        .setId(1)
                        .setName("客服15")
                        .build()
                )
                .setQty(1)
                .build();
        orderReportSender.send(message);
    }

    /**
     * 对账
     */
    @Test
    public void sendMessageAfterCharge(){
        //客评，更新客服报表数据
        MQOrderReport.OrderReport message = MQOrderReport.OrderReport.newBuilder()
                .setOrderId(891639549665611776l)
                .setOrderType(200)
                .setAmount(0.0)
                .setTriggerDate(DateUtils.parseDate("2017-07-30 20:45:29").getTime())
                .setTriggerBy(1)
                .setQty(1)
                .build();
        orderReportSender.send(message);
    }


    //endregion 订单报表消息测试

    //region 自动完工

    @Test
    public void sendOrderAutoCompleteMessge(){
        MQOrderAutoComplete.OrderAutoComplete message = MQOrderAutoComplete.OrderAutoComplete.newBuilder()
                .setOrderId(1386522478322520065l)
                .setQuarter("20212")
                .setTriggerBy(1)
                .setTriggerDate(System.currentTimeMillis())
                .build();
        orderAutoCompleteDelaySender.send(message);
    }

    //endregion 自动完工

    //region 下单消息队列

    @Test
    public void testSendCreateOrderPushMessage(){
        MQCreateOrderPushMessage.CreateOrderPushMessage orderMessage;
        orderMessage = MQCreateOrderPushMessage.CreateOrderPushMessage.newBuilder()
                .setOrderId(1037582832924495872l)
                .setQuarter("20183")
                .setOrderNo("K2018090661780")
                .setCategoryId(1) // 2019-09-26
                .setTriggerBy(MQCreateOrderPushMessage.TriggerBy.newBuilder()
                        .setId(1)
                        .setName("系统管理员")
                        .build())
                .setTriggerDate(1536214009000l)
                .setOrderApproveFlag(1)
                //order fee
                .setOrderFee(MQCreateOrderPushMessage.OrderFee.newBuilder()
                        .setExpectCharge(120.0)
                        .setBlockedCharge(0)
                        .setCustomerUrgentCharge(0)
                        .setEngineerUrgentCharge(0)
                        .setOrderPaymentType(30)
                        .setOrderPaymentTypeName("预付")
                        .build())
                //kefu
                .setKefu(MQCreateOrderPushMessage.TriggerBy.newBuilder()
                        .setId(49542)
                        .setName("张嗣蓝")
                        .build())
                .setMsgContent("测试下单短信内容")
                .setCustomer(MQCustomer.Customer.newBuilder()
                        .setId(1402)
                        .setCode("C10212")
                        .setName("0100姚昆")
                        .setContractDate(1395849600000l)
                        .setSalesId(49250)
                        .setSalesName("程怀明")
                        .setPaymentType(30)
                        .setPaymentTypeName("预付")
                        .build())
                //area
                .setAreaId(1590)
                .build();

        createOrderPushMessageSender.send(orderMessage);
    }
    //endregion

    //region 语音回访

    @Test
    public void testNewVoiceTask(){
        try {
            MQVoiceSeviceMessage.Task message = MQVoiceSeviceMessage.Task.newBuilder()
                    .setVoiceType(1)
                    .setSite("CW")
                    .setOrderId(1085539196451557376l)
                    .setQuarter("20191")
                    .setUserName("卢生")
                    .setPhone("13760468206")
                    .setProducts("油烟机")
                    .setOpeningSpeech("yanji1") //开场白（产品+服务） 1：安装 2：其他
                    .setCaption("K2019011620389自动回访")
                    .setCreateBy("快可立")
                    .setCreateDate(1547647862218l)
                    .build();
            newTaskMQSender.send(message);
        }catch (Exception e){
            e.printStackTrace();
        }
        /*
        {"site": "CW", "phone": "13760468206", "caption": "K2019011620389自动回访", "orderId": 1085539196451557376, "quarter": "20191", "createBy": "快可立", "products": "油烟机", "userName": "卢生", "voiceType": 1, "createDate": 1547647862218, "openingSpeech": "yanji1"}
         */
    }

    @Test
    public void testCancelVoiceTask(){
        MQVoiceSeviceMessage.OperateCommand operateCommand = MQVoiceSeviceMessage.OperateCommand.newBuilder()
                .setSite("CW")
                .setOrderId(1094548191493361665l)
                .setCommand(OperateType.STOP.code)
                .setCreateBy("管理员")
                .setCreateDate(System.currentTimeMillis())
                .build();
        operateTaskMQSender.send(operateCommand);
    }

    //endregion


    @Test
    public void testParseMessage(){
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
    }

    @Test
    public void testParseSysLogMessage(){
        String msgString = "CAIY0qWz360uIgkxMjcuMC4wLjEqA1dlYjIbT3JkZXJBdXRvUGxhbk1lc3NhZ2VTZXJ2aWNlOgNMb2dCDksyMDIwMDYyMjk0NTAySukgamF2YS5sYW5nLlJ1" +
                "bnRpbWVFeGNlcHRpb246IOaXoOWPr+iHquWKqOa0vuWNlee9keeCuSxvcmRlck5vOksyMDIwMDYyMjk0NTAyICxhcmVhSWQ6MjQsMTU2ICwgbG9uZ3RpdHVk" +
                "ZToxMDUuMDAzLCBsYXRpdHVkZToyOS43MTkKCWF0IGNvbS53b2xma2luZy5qZWVzaXRlLm1vZHVsZXMubXEuc2VydmljZS5PcmRlckF1dG9QbGFuTWVzc2Fn" +
                "ZVNlcnZpY2UucHJvY2Vzc01lc3NhZ2UoT3JkZXJBdXRvUGxhbk1lc3NhZ2VTZXJ2aWNlLmphdmE6Mjk2KQoJYXQgY29tLndvbGZraW5nLmplZXNpdGUubW9k" +
                "dWxlcy5tcS5zZXJ2aWNlLk9yZGVyQXV0b1BsYW5NZXNzYWdlU2VydmljZSQkRmFzdENsYXNzQnlTcHJpbmdDR0xJQiQkZjdkZGYzNGQuaW52b2tlKDxnZW5l" +
                "cmF0ZWQ+KQoJYXQgb3JnLnNwcmluZ2ZyYW1ld29yay5jZ2xpYi5wcm94eS5NZXRob2RQcm94eS5pbnZva2UoTWV0aG9kUHJveHkuamF2YToyMDQpCglhdCBv" +
                "cmcuc3ByaW5nZnJhbWV3b3JrLmFvcC5mcmFtZXdvcmsuQ2dsaWJBb3BQcm94eSRDZ2xpYk1ldGhvZEludm9jYXRpb24uaW52b2tlSm9pbnBvaW50KENnbGli" +
                "QW9wUHJveHkuamF2YTo3MzgpCglhdCBvcmcuc3ByaW5nZnJhbWV3b3JrLmFvcC5mcmFtZXdvcmsuUmVmbGVjdGl2ZU1ldGhvZEludm9jYXRpb24ucHJvY2Vl" +
                "ZChSZWZsZWN0aXZlTWV0aG9kSW52b2NhdGlvbi5qYXZhOjE1NykKCWF0IG9yZy5zcHJpbmdmcmFtZXdvcmsudHJhbnNhY3Rpb24uaW50ZXJjZXB0b3IuVHJh" +
                "bnNhY3Rpb25JbnRlcmNlcHRvciQxLnByb2NlZWRXaXRoSW52b2NhdGlvbihUcmFuc2FjdGlvbkludGVyY2VwdG9yLmphdmE6OTkpCglhdCBvcmcuc3ByaW5n" +
                "ZnJhbWV3b3JrLnRyYW5zYWN0aW9uLmludGVyY2VwdG9yLlRyYW5zYWN0aW9uQXNwZWN0U3VwcG9ydC5pbnZva2VXaXRoaW5UcmFuc2FjdGlvbihUcmFuc2Fj" +
                "dGlvbkFzcGVjdFN1cHBvcnQuamF2YToyODIpCglhdCBvcmcuc3ByaW5nZnJhbWV3b3JrLnRyYW5zYWN0aW9uLmludGVyY2VwdG9yLlRyYW5zYWN0aW9uSW50" +
                "ZXJjZXB0b3IuaW52b2tlKFRyYW5zYWN0aW9uSW50ZXJjZXB0b3IuamF2YTo5NikKCWF0IG9yZy5zcHJpbmdmcmFtZXdvcmsuYW9wLmZyYW1ld29yay5SZWZs" +
                "ZWN0aXZlTWV0aG9kSW52b2NhdGlvbi5wcm9jZWVkKFJlZmxlY3RpdmVNZXRob2RJbnZvY2F0aW9uLmphdmE6MTc5KQoJYXQgb3JnLnNwcmluZ2ZyYW1ld29y" +
                "ay5hb3AuZnJhbWV3b3JrLkNnbGliQW9wUHJveHkkRHluYW1pY0FkdmlzZWRJbnRlcmNlcHRvci5pbnRlcmNlcHQoQ2dsaWJBb3BQcm94eS5qYXZhOjY3MykK" +
                "CWF0IGNvbS53b2xma2luZy5qZWVzaXRlLm1vZHVsZXMubXEuc2VydmljZS5PcmRlckF1dG9QbGFuTWVzc2FnZVNlcnZpY2UkJEVuaGFuY2VyQnlTcHJpbmdD" +
                "R0xJQiQkNDlmMzVkZjgucHJvY2Vzc01lc3NhZ2UoPGdlbmVyYXRlZD4pCglhdCBjb20ud29sZmtpbmcuamVlc2l0ZS5tb2R1bGVzLm1xLnNlcnZpY2UuT3Jk" +
                "ZXJBdXRvUGxhbk1lc3NhZ2VTZXJ2aWNlJCRGYXN0Q2xhc3NCeVNwcmluZ0NHTElCJCRmN2RkZjM0ZC5pbnZva2UoPGdlbmVyYXRlZD4pCglhdCBvcmcuc3By" +
                "aW5nZnJhbWV3b3JrLmNnbGliLnByb3h5Lk1ldGhvZFByb3h5Lmludm9rZShNZXRob2RQcm94eS5qYXZhOjIwNCkKCWF0IG9yZy5zcHJpbmdmcmFtZXdvcmsu" +
                "YW9wLmZyYW1ld29yay5DZ2xpYkFvcFByb3h5JENnbGliTWV0aG9kSW52b2NhdGlvbi5pbnZva2VKb2lucG9pbnQoQ2dsaWJBb3BQcm94eS5qYXZhOjczOCkK" +
                "CWF0IG9yZy5zcHJpbmdmcmFtZXdvcmsuYW9wLmZyYW1ld29yay5SZWZsZWN0aXZlTWV0aG9kSW52b2NhdGlvbi5wcm9jZWVkKFJlZmxlY3RpdmVNZXRob2RJ" +
                "bnZvY2F0aW9uLmphdmE6MTU3KQoJYXQgb3JnLnNwcmluZ2ZyYW1ld29yay50cmFuc2FjdGlvbi5pbnRlcmNlcHRvci5UcmFuc2FjdGlvbkludGVyY2VwdG9y" +
                "JDEucHJvY2VlZFdpdGhJbnZvY2F0aW9uKFRyYW5zYWN0aW9uSW50ZXJjZXB0b3IuamF2YTo5OSkKCWF0IG9yZy5zcHJpbmdmcmFtZXdvcmsudHJhbnNhY3Rp" +
                "b24uaW50ZXJjZXB0b3IuVHJhbnNhY3Rpb25Bc3BlY3RTdXBwb3J0Lmludm9rZVdpdGhpblRyYW5zYWN0aW9uKFRyYW5zYWN0aW9uQXNwZWN0U3VwcG9ydC5q" +
                "YXZhOjI4MikKCWF0IG9yZy5zcHJpbmdmcmFtZXdvcmsudHJhbnNhY3Rpb24uaW50ZXJjZXB0b3IuVHJhbnNhY3Rpb25JbnRlcmNlcHRvci5pbnZva2UoVHJh" +
                "bnNhY3Rpb25JbnRlcmNlcHRvci5qYXZhOjk2KQoJYXQgb3JnLnNwcmluZ2ZyYW1ld29yay5hb3AuZnJhbWV3b3JrLlJlZmxlY3RpdmVNZXRob2RJbnZvY2F0" +
                "aW9uLnByb2NlZWQoUmVmbGVjdGl2ZU1ldGhvZEludm9jYXRpb24uamF2YToxNzkpCglhdCBvcmcuc3ByaW5nZnJhbWV3b3JrLmFvcC5mcmFtZXdvcmsuQ2ds" +
                "aWJBb3BQcm94eSREeW5hbWljQWR2aXNlZEludGVyY2VwdG9yLmludGVyY2VwdChDZ2xpYkFvcFByb3h5LmphdmE6NjczKQoJYXQgY29tLndvbGZraW5nLmpl" +
                "ZXNpdGUubW9kdWxlcy5tcS5zZXJ2aWNlLk9yZGVyQXV0b1BsYW5NZXNzYWdlU2VydmljZSQkRW5oYW5jZXJCeVNwcmluZ0NHTElCJCRjNmNiYWMxNC5wcm9j" +
                "ZXNzTWVzc2FnZSg8Z2VuZXJhdGVkPikKCWF0IGNvbS53b2xma2luZy5qZWVzaXRlLm1vZHVsZXMubXEucmVjZWl2ZXIuT3JkZXJBdXRvUGxhbk1lc3NhZ2VS" +
                "ZWNlaXZlci5vbk1lc3NhZ2UoT3JkZXJBdXRvUGxhbk1lc3NhZ2VSZWNlaXZlci5qYXZhOjYyKQoJYXQgb3JnLnNwcmluZ2ZyYW1ld29yay5hbXFwLnJhYmJp" +
                "dC5saXN0ZW5lci5hZGFwdGVyLk1lc3NhZ2VMaXN0ZW5lckFkYXB0ZXIub25NZXNzYWdlKE1lc3NhZ2VMaXN0ZW5lckFkYXB0ZXIuamF2YToyNzMpCglhdCBv" +
                "cmcuc3ByaW5nZnJhbWV3b3JrLmFtcXAucmFiYml0Lmxpc3RlbmVyLkFic3RyYWN0TWVzc2FnZUxpc3RlbmVyQ29udGFpbmVyLmRvSW52b2tlTGlzdGVuZXIo" +
                "QWJzdHJhY3RNZXNzYWdlTGlzdGVuZXJDb250YWluZXIuamF2YTo4NDgpCglhdCBvcmcuc3ByaW5nZnJhbWV3b3JrLmFtcXAucmFiYml0Lmxpc3RlbmVyLkFi" +
                "c3RyYWN0TWVzc2FnZUxpc3RlbmVyQ29udGFpbmVyLmludm9rZUxpc3RlbmVyKEFic3RyYWN0TWVzc2FnZUxpc3RlbmVyQ29udGFpbmVyLmphdmE6NzcxKQoJ" +
                "YXQgb3JnLnNwcmluZ2ZyYW1ld29yay5hbXFwLnJhYmJpdC5saXN0ZW5lci5TaW1wbGVNZXNzYWdlTGlzdGVuZXJDb250YWluZXIuYWNjZXNzJDAwMShTaW1w" +
                "bGVNZXNzYWdlTGlzdGVuZXJDb250YWluZXIuamF2YToxMDIpCglhdCBvcmcuc3ByaW5nZnJhbWV3b3JrLmFtcXAucmFiYml0Lmxpc3RlbmVyLlNpbXBsZU1l" +
                "c3NhZ2VMaXN0ZW5lckNvbnRhaW5lciQxLmludm9rZUxpc3RlbmVyKFNpbXBsZU1lc3NhZ2VMaXN0ZW5lckNvbnRhaW5lci5qYXZhOjE5OCkKCWF0IG9yZy5z" +
                "cHJpbmdmcmFtZXdvcmsuYW1xcC5yYWJiaXQubGlzdGVuZXIuU2ltcGxlTWVzc2FnZUxpc3RlbmVyQ29udGFpbmVyLmludm9rZUxpc3RlbmVyKFNpbXBsZU1l" +
                "c3NhZ2VMaXN0ZW5lckNvbnRhaW5lci5qYXZhOjEzMTEpCglhdCBvcmcuc3ByaW5nZnJhbWV3b3JrLmFtcXAucmFiYml0Lmxpc3RlbmVyLkFic3RyYWN0TWVz" +
                "c2FnZUxpc3RlbmVyQ29udGFpbmVyLmV4ZWN1dGVMaXN0ZW5lcihBYnN0cmFjdE1lc3NhZ2VMaXN0ZW5lckNvbnRhaW5lci5qYXZhOjc1MikKCWF0IG9yZy5z" +
                "cHJpbmdmcmFtZXdvcmsuYW1xcC5yYWJiaXQubGlzdGVuZXIuU2ltcGxlTWVzc2FnZUxpc3RlbmVyQ29udGFpbmVyLmRvUmVjZWl2ZUFuZEV4ZWN1dGUoU2lt" +
                "cGxlTWVzc2FnZUxpc3RlbmVyQ29udGFpbmVyLmphdmE6MTI1NCkKCWF0IG9yZy5zcHJpbmdmcmFtZXdvcmsuYW1xcC5yYWJiaXQubGlzdGVuZXIuU2ltcGxl" +
                "TWVzc2FnZUxpc3RlbmVyQ29udGFpbmVyLnJlY2VpdmVBbmRFeGVjdXRlKFNpbXBsZU1lc3NhZ2VMaXN0ZW5lckNvbnRhaW5lci5qYXZhOjEyMjQpCglhdCBv" +
                "cmcuc3ByaW5nZnJhbWV3b3JrLmFtcXAucmFiYml0Lmxpc3RlbmVyLlNpbXBsZU1lc3NhZ2VMaXN0ZW5lckNvbnRhaW5lci5hY2Nlc3MkMTYwMChTaW1wbGVN" +
                "ZXNzYWdlTGlzdGVuZXJDb250YWluZXIuamF2YToxMDIpCglhdCBvcmcuc3ByaW5nZnJhbWV3b3JrLmFtcXAucmFiYml0Lmxpc3RlbmVyLlNpbXBsZU1lc3Nh" +
                "Z2VMaXN0ZW5lckNvbnRhaW5lciRBc3luY01lc3NhZ2VQcm9jZXNzaW5nQ29uc3VtZXIucnVuKFNpbXBsZU1lc3NhZ2VMaXN0ZW5lckNvbnRhaW5lci5qYXZh" +
                "OjE0NzApCglhdCBqYXZhLmxhbmcuVGhyZWFkLnJ1bihUaHJlYWQuamF2YTo3NDgpClIS6Ieq5Yqo5rS+5Y2V5aSx6LSlWgUyMDIwMg==";
        byte[] baseBytes = Base64.getDecoder().decode(msgString);
        InputStream is = new ByteArrayInputStream(baseBytes);
        try {
            MQSysLogMessage.SysLogMessage log = MQSysLogMessage.SysLogMessage
                    .parseFrom(is);
            if(log == null){
                System.out.println("convert message error ,is null");
            }
            String json = new JsonFormat().printToString(log);
            System.out.println(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testParseLogisticMessage(){
        String msgString = "CgUyMDE5MxCBoIik4L3j9A8aDksyMDE5MDcwNDA1MjMyIAEose2T4LstMgsxNzMwMDAwMDAwMDpACIDAsrzLv/f0DxBLGgnmsrnng5/mnLoiCHNoZW50b25nKgznlLPpgJrlv6vpgJIyDzY2NjY2NjY2Njk5OTk5OQ==";
        byte[] baseBytes = Base64.getDecoder().decode(msgString);
        InputStream is = new ByteArrayInputStream(baseBytes);
        try {
            MQLMExpress.ExpressMessage message = MQLMExpress.ExpressMessage
                    .parseFrom(is);
            if(message == null){
                System.out.println("convert message error ,is null");
            }
            String json = new JsonFormat().printToString(message);
            System.out.println(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testB2BOrderUpdateMessage(){
        String msgString = "CAIQgaCQ6KuUjNYRGgkyNjU3MDgwNTcgKCj5uARI2p3/pbAukAGAoJiisZaD3hGYAQ+iAQ5LMjAyMDA2MTg2MjQ0OKgBgcCJ3NaSjNYR";
        byte[] baseBytes = Base64.getDecoder().decode(msgString);
        InputStream is = new ByteArrayInputStream(baseBytes);
        try {
            MQB2BOrderStatusUpdateMessage.B2BOrderStatusUpdateMessage message = MQB2BOrderStatusUpdateMessage.B2BOrderStatusUpdateMessage
                    .parseFrom(is);
            if(message == null){
                System.out.println("convert message error ,is null");
            }
            String json = new JsonFormat().printToString(message);
            System.out.println(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testMQRechargeMessage(){
        MQRechargeOrderMessage.RechargeOrderMessage message = MQRechargeOrderMessage.RechargeOrderMessage.newBuilder()
                .setId(1147393544978305025l)
                .setTradeNo("RC201907270001")
                .setReferId(1584)
                .setRechargeType(1)
                .setAmount(100.0)
                .setCreateAt(System.currentTimeMillis())
                .setCreateBy(1l)
                .setRemarks("备注")
                .setStatus(40)
                //.setIsTaskRetry(1)
                .build();
        String json = new JsonFormat().printToString(message);
        System.out.println(json);
        //MQRechargeOrderMessage.RechargeOrderMessage message2 = message.toBuilder().setIsTaskRetry(1).build();
        //System.out.println(new JsonFormat().printToString(message2));

        MQRechargeOrderMessage.RechargeOrderMessage message2 = fromJson(json);
        System.out.println(new JsonFormat().printToString(message2));
    }

    /**
     * 将json转消息体,并设置:isTaskRetry = 1
     */
    private MQRechargeOrderMessage.RechargeOrderMessage fromJson(String json){
        MQRechargeOrderMessage.RechargeOrderMessage.Builder builder = MQRechargeOrderMessage.RechargeOrderMessage.newBuilder();
        try {
            new JsonFormat().merge(new ByteArrayInputStream(json.getBytes("utf-8")), builder);
            builder.setIsTaskRetry(1);
            return builder.build();
        } catch (IOException e) {
            log.error("json转消息体错误",e);
        }
        return null;
    }

    /**
     * 测试好评审核结果消息队列
     */
    @Test
    public void testPraiseAutoReviewed(){
        try {
            MQPraiseMessage.PraiseActionMessage message = MQPraiseMessage.PraiseActionMessage.newBuilder()
                    .setOrderId(1247760948681379840l)
                    .setQuarter("20202")
                    .setTrigger(MQPraiseMessage.User.newBuilder()
                            .setId(1l)
                            .setName("管理员")
                            .setUserType(3)
                            .build())
                    .setTriggerAt(1586324654003l)
                    .setServicePointId(4991l)
                    .setFormNo("P202004080004")
                    .setStatus(50) //20-待审核 30-驳回 40-通过 50-取消
                    .setPayable(8.00)
                    .setReceivable(10.00)
                    .setRemark("备注内容")
                    .setCustomerId(1482l)
                    .setDataSourceId(1)
                    .build();
            praiseFormMQRetrySender.sendRetry(message, 5000, 1);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //region 投诉
    /**
     * 测试创建投诉
     */
    @Test
    public void sendCreateComplainFormMessage(){
        //1.有KKL投诉单号
        MQB2BOrderComplainMessage.B2BOrderComplainMessage message = MQB2BOrderComplainMessage.B2BOrderComplainMessage.newBuilder()
                .setDataSource(19)
                .setOrderId(1309802080294277121L)
                .setQuarter("20203")
                .setB2BComplainNo("YM202010150001")
                //.setComplainNo("C2010140003")
                .setContent("测试：自建投诉消息队列，无KKL投诉单号")
                .setCreateAt(System.currentTimeMillis())
                .build();
        complainMQSender.sendRetry(message,0);
        /*
        2.无KKL投诉单号(如：九阳)
        MQB2BOrderComplainMessage.B2BOrderComplainMessage message2 = MQB2BOrderComplainMessage.B2BOrderComplainMessage.newBuilder()
                .setDataSource(19)
                .setOrderId(1283324233711226881L)
                .setQuarter("20203")
                .setB2BComplainNo("YM202010140002")
                .setContent("测试：自建投诉消息队列，无KKL投诉单号")
                .setCreateAt(System.currentTimeMillis())
                .build();
        complainMQSender.sendRetry(message,0);
        */
    }

    //endregion

    public static void main(String[] args) {
        /*
        //原始内容
        String msgString = "EICgiMLy+qOnERoFMjAyMDIiFQgBEg/ns7vnu5/nrqHnkIblkZgYAyjqn7KTlS5gAw==";
        //base64解密
        byte[] baseBytes = Base64.getDecoder().decode(msgString);
        //装载到流
        InputStream is = new ByteArrayInputStream(baseBytes);
        try {
            //转换
            MQPraiseMessage.PraiseActionMessage message = MQPraiseMessage.PraiseActionMessage
                    .parseFrom(is);
            if (message == null) {
                System.out.println("convert message error ,is null");
            }
            String json = new JsonFormat().printToString(message);
            System.out.println(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
        */

    }

}
