package com.wolfking.jeesite.test.ms.fi;

import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.entity.cc.Reminder;
import com.kkl.kklplus.entity.cc.ReminderStatus;
import com.kkl.kklplus.entity.cc.ReminderType;
import com.kkl.kklplus.entity.fi.common.RechargeType;
import com.kkl.kklplus.entity.fi.mq.MQRechargeOrderMessage;
import com.wolfking.jeesite.common.utils.GsonUtils;
import com.wolfking.jeesite.ms.cc.entity.ReminderModel;
import com.wolfking.jeesite.ms.cc.entity.mapper.ReminderModelMapper;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

/**
 * 测试充值消息
 */
//@RunWith(SpringJUnit4ClassRunner.class)
@Slf4j
@SpringBootTest
public class RechargeTest {

    /**
     * 测试转换
     */
    @Test
    public void testMapper() {
        MQRechargeOrderMessage.RechargeOrderMessage message = MQRechargeOrderMessage.RechargeOrderMessage.newBuilder()
                .setId(1166196883081793536L)
                .setTradeNo("R2019082700042")
                .setReferId(3122L)//customer id
                .setAmount(500.00)
                .setCreateAt(1566877983117L)
                .setCreateBy(78633L)
                .setRechargeType(RechargeType.Customer.getCode())
                .setStatus(2)
                .setRemarks("B950珠海城派电子商务有限公司在线充值")
                .build();
        System.out.println("json:" + new JsonFormat().printToString(message));

    }

}
