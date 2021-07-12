package com.wolfking.jeesite.test.mq;

import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.mq.dto.MQCustomer;
import com.wolfking.jeesite.modules.mq.dto.MQOrderReport;
import com.wolfking.jeesite.modules.mq.sender.OrderReportSender;
import com.wolfking.jeesite.modules.sd.entity.Order;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Created by Ryan on 2017/08/01.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class OrderReportTest {

    @Autowired
    OrderReportSender orderReportSender;

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


}
