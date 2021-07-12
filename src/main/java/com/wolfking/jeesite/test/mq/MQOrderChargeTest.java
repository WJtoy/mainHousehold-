package com.wolfking.jeesite.test.mq;

import com.wolfking.jeesite.modules.mq.dto.MQOrderCharge;
import com.wolfking.jeesite.modules.mq.sender.OrderChargeSender;
import com.wolfking.jeesite.modules.mq.sender.OrderFeeUpdateAfterChargeSender;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by Jeff on 2017/6/23.
 */
//@RunWith(SpringRunner.class)
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@ActiveProfiles("dev")
public class MQOrderChargeTest {

    @Autowired
    OrderChargeSender orderChargeSender;

    @Autowired
    private OrderFeeUpdateAfterChargeSender orderFeeUpdateAfterChargeSender;

    @Test
    public void orderChargeTester(){
        Assert.assertTrue(orderChargeSender());
//        assert saveMQOrderCharge();
    }

    public boolean orderChargeSender(){
        for (int i=0; i<100000; i++){
//        while (true){
            MQOrderCharge.OrderCharge.Builder orderChargeBuilder = MQOrderCharge.OrderCharge.newBuilder();
            orderChargeBuilder.setOrderId(i);
            orderChargeBuilder.setTriggerBy(12);
            orderChargeBuilder.setTriggerDate(System.currentTimeMillis());
//            orderChargeSender.send(orderChargeBuilder.build());
            System.out.println(" ======= S ======" + String.valueOf(i));
        }
//        MQOrderCharge.OrderCharge.Builder orderChargeBuilder = MQOrderCharge.OrderCharge.newBuilder();
//        orderChargeBuilder.setOrderId(11);
//        orderChargeBuilder.setTriggerBy(12);
//        orderChargeBuilder.setTriggerDate(System.currentTimeMillis());
//        orderChargeSender.send(orderChargeBuilder.build());
        return true;
    }


//    public boolean saveMQOrderCharge(){
//        try {
//            OrderCharge orderCharge = new OrderCharge();
//            orderCharge.setId(SeqUtils.NextIDValue(SeqUtils.TableName.MqOrderCharge));
//            orderCharge.setQuarter(DateUtils.getYear()+DateUtils.getSeason());
//            orderCharge.setOrderId(1l);
//            orderCharge.setRetryTimes(3);
//            orderCharge.setStatus(40);
//            orderCharge.setDescription("2017-06-24 11:39:48.004  INFO 20972 --- [pool-5-thread-4] c.w.j.modules.mq.core.MQAccessBuilder    : process message failed: null");
//            orderCharge.setTriggerBy(1L);
//            orderCharge.setTriggerDate(new Date(System.currentTimeMillis()));
//            orderCharge.setCreateDate(new Date());
//            orderChargeService.insert(orderCharge);
//            return true;
//        }catch (Exception e){
//            logger.error(e.getLocalizedMessage());
//            return false;
//        }
//    }

    @Test
    public void sendOrderFeeUpdateAfterChargeSender(){
        MQOrderCharge.OrderFeeUpdateAfterCharge charge = MQOrderCharge.OrderFeeUpdateAfterCharge
                .newBuilder()
                .setOrderId(1247069373135458305l)
                .setQuarter("20202")
                .setTriggerBy(1l)
                .setTriggerDate(1586162799000l)
                .addItems(
                        MQOrderCharge.FeeUpdateItem.newBuilder()
                                .setServicePointId(4991l)
                                .setTaxFee(-1.80)
                                .setInfoFee(-1.15)
                                .build()
                )
                .addItems(
                        MQOrderCharge.FeeUpdateItem.newBuilder()
                                .setServicePointId(4990l)
                                .setTaxFee(-1.0)
                                .setInfoFee(-0.8)
                                .build()
                )
                .build();
        try {
            orderFeeUpdateAfterChargeSender.sendDelay(charge,5000,1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
