package com.wolfking.jeesite.test.fi;

import com.kkl.kklplus.entity.fi.mq.MQCreateCustomerCurrencyMessage;
import com.wolfking.jeesite.modules.mq.sender.CreateCustomerCurrencySender;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * 客户财务测试类
 * @author: Jeff.Zhao
 * @date: 2018/9/3 15:22
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class FICustomerTest {
    @Autowired
    private CreateCustomerCurrencySender createCustomerCurrencySender;

    @Test
    public void CreateCustomerCurrencyTest(){
        MQCreateCustomerCurrencyMessage.CreateCustomerCurrencyMessage createCustomerCurrencyMessage = MQCreateCustomerCurrencyMessage.CreateCustomerCurrencyMessage.newBuilder()
                .setMessageId(123)
                .setCustomerId(1)
                .setCurrencyType(20)
                .setCurrencyNo("Test123")
                .setBeforeBalance(100)
                .setBalance(38)
                .setAmount(62)
                .setPaymentType(10)
                .setActionType(20)
                .setQuarter("20183")
                .setCreateById(22)
                .setCreateById(System.currentTimeMillis())
                .build();
        try {
            createCustomerCurrencySender.send(createCustomerCurrencyMessage);
        } catch (Exception e) {
            log.error("生成客户流水数据失败，{}", e);
        }
    }
}
