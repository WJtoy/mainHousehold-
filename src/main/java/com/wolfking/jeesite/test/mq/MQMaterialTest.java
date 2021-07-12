package com.wolfking.jeesite.test.mq;

import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.entity.common.mq.message.MQB2BMaterialFormMessage;
import com.wolfking.jeesite.ms.material.mq.sender.B2BMateiralMQSender;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * 配件B2B消息测试类
 * 用于模拟B2发送审核，发货消息
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class MQMaterialTest {

    @Autowired
    B2BMateiralMQSender b2BMateiralMQSender;

    /**
     * 模拟审核通过消息
     */
    @Test
    public void mockAudit(){
        MQB2BMaterialFormMessage.B2BMaterialFormMessage message = MQB2BMaterialFormMessage.B2BMaterialFormMessage.newBuilder()
                .setDataSource(B2BDataSourceEnum.JOYOUNG.id)
                .setId(1175965616264318977L)
                .setQuarter("20193")
                .setNotifyType(MQB2BMaterialFormMessage.NotifyType.Audit)
                .setKklMasterId(1175965565220958208L)
                .setAuditStatus(1)//通过
                .setRemark("mock audit")
                .setNotifyDate(System.currentTimeMillis())
                .build();
        b2BMateiralMQSender.sendDelay(message,0,0);
    }

    /**
     * 模拟审核驳回消息
     */
    @Test
    public void mockAuditReject(){
        MQB2BMaterialFormMessage.B2BMaterialFormMessage message = MQB2BMaterialFormMessage.B2BMaterialFormMessage.newBuilder()
                .setDataSource(B2BDataSourceEnum.JOYOUNG.id)
                .setId(1175059821792202752L)
                .setQuarter("20193")
                .setNotifyType(MQB2BMaterialFormMessage.NotifyType.Audit)
                .setKklMasterId(1175059785733218304L)
                .setAuditStatus(0)//reject
                .setRemark("mock rject")
                .setNotifyDate(System.currentTimeMillis())
                .build();
        b2BMateiralMQSender.sendDelay(message,0,0);
    }


    /**
     * 模拟发货消息
     */
    @Test
    public void mockDelivery(){
        MQB2BMaterialFormMessage.B2BMaterialFormMessage message = MQB2BMaterialFormMessage.B2BMaterialFormMessage.newBuilder()
                .setDataSource(B2BDataSourceEnum.JOYOUNG.id)
                .setId(1173490795139960890L)
                .setQuarter("20193")
                .setNotifyType(MQB2BMaterialFormMessage.NotifyType.Delivery)
                .setKklMasterId(1175965565220958208L)
                .setExpressCompany("申通")
                .setExpressNo("1238634532525")
                .setDeliveryDate(1569118993000L)
                .setRemark("mock delivery")
                .setNotifyDate(System.currentTimeMillis())
                .build();
        b2BMateiralMQSender.sendDelay(message,0,0);
    }

}
