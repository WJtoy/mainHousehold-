package com.wolfking.jeesite.test.fi;

import com.google.common.collect.Lists;
import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.entity.es.mq.MQSyncServicePointMessage;
import com.kkl.kklplus.entity.fi.mq.MQCreateEngineerCurrencyMessage;
import com.wolfking.jeesite.modules.fi.dao.ServicePointInvoiceMonthlyDao;
import com.wolfking.jeesite.modules.fi.dao.ServicePointPaidMonthlyDao;
import com.wolfking.jeesite.modules.fi.dao.ServicePointPayableMonthlyDao;
import com.wolfking.jeesite.modules.fi.entity.*;
import com.wolfking.jeesite.modules.fi.service.ServicePointPayableMonthlyService;
import com.wolfking.jeesite.modules.fi.task.FISchedules;
import com.wolfking.jeesite.modules.md.dao.ServicePointDao;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.mq.sender.CreateEngineerCurrencySender;
import com.wolfking.jeesite.modules.rpt.dao.ServicePointBalanceMonthlyDao;
import com.wolfking.jeesite.modules.sys.dao.Log2Dao;
import com.wolfking.jeesite.modules.sys.utils.SeqUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class FIServicePointTest {

    @Autowired
    private ServicePointPayableMonthlyDao servicePointPayableMonthlyDao;

    @Autowired
    private ServicePointPaidMonthlyDao servicePointPaidMonthlyDao;

    @Autowired
    private ServicePointInvoiceMonthlyDao servicePointInvoiceMonthlyDao;

    @Autowired
    private ServicePointBalanceMonthlyDao servicePointBalanceMonthlyDao;

    @Autowired
    private ServicePointPayableMonthlyService servicePointPayableMonthlyService;

    @Autowired
    private CreateEngineerCurrencySender createEngineerCurrencySender;

    @Autowired
    private ServicePointDao servicePointDao;

    @Autowired
    private FISchedules fiSchedules;

    /*
    // mark on 2020-7-11
    @Resource
    private Log2Dao log2Dao;
    */

    @Test
    public void updateSelectTest() {
        ServicePoint servicePoint = new ServicePoint();
        servicePoint.setId(1621989L);

        Long servicePointPayableId = SeqUtils.NextIDValue(SeqUtils.TableName.ServicePointPayableMonthly);
        ServicePointPayableMonthly servicePointPayableMonthly = new ServicePointPayableMonthly();
        servicePointPayableMonthly.setId(servicePointPayableId);
        servicePointPayableMonthly.setServicePoint(servicePoint);
        servicePointPayableMonthly.setPaymentType(10);
        servicePointPayableMonthly.setYear(2016);
        servicePointPayableMonthly.setMonth(12);
        servicePointPayableMonthly.setAmount(80.0);

//        servicePointPayableMonthlyDao.incrAmount(servicePointPayableMonthly);

        ServicePointPaidMonthly servicePointPaidMonthly = new ServicePointPaidMonthly();
        servicePointPaidMonthly.setServicePoint(servicePoint);
        servicePointPaidMonthly.setPaymentType(20);
        servicePointPaidMonthly.setYear(2016);
        servicePointPaidMonthly.setMonth(3);
        servicePointPaidMonthly.setId((servicePointPaidMonthly.getServicePoint().getId() * 10000 + servicePointPaidMonthly.getYear()) * 100 + servicePointPaidMonthly.getPaymentType());
        servicePointPaidMonthly.setAmount(50.8);

        servicePointPaidMonthlyDao.incrAmount(servicePointPaidMonthly);
    }

    @Test
    public void calcBalance() {
        servicePointPayableMonthlyService.calcBalance();
    }

    @Test
    public void increaseById() {
        ServicePointPayableMonthly payableMonthly = new ServicePointPayableMonthly();
        payableMonthly.setServicePoint(new ServicePoint(1L));
        payableMonthly.setPaymentType(10);
        payableMonthly.setYear(2018);
        payableMonthly.setMonth(12);
        payableMonthly.setId((payableMonthly.getServicePoint().getId() * 10000 + payableMonthly.getYear()) * 100 + payableMonthly.getPaymentType());
        payableMonthly.setAmount(-100d);
        servicePointPayableMonthlyDao.incrAmountForCharge(payableMonthly);
        servicePointBalanceMonthlyDao.incrBalance(payableMonthly);

        ServicePointInvoiceMonthly invoiceMonthly = new ServicePointInvoiceMonthly();
        invoiceMonthly.setServicePoint(new ServicePoint(1L));
        invoiceMonthly.setPaymentType(10);
        invoiceMonthly.setYear(2018);
        invoiceMonthly.setMonth(12);
        invoiceMonthly.setId((invoiceMonthly.getServicePoint().getId() * 10000 + invoiceMonthly.getYear()) * 100 + invoiceMonthly.getPaymentType());
        invoiceMonthly.setAmount(-100d);
        servicePointInvoiceMonthlyDao.incrAmount(invoiceMonthly);

        ServicePointPaidMonthly paidMonthly = new ServicePointPaidMonthly();
        paidMonthly.setServicePoint(new ServicePoint(1L));
        paidMonthly.setPaymentType(10);
        paidMonthly.setYear(2018);
        paidMonthly.setMonth(12);
        paidMonthly.setId((paidMonthly.getServicePoint().getId() * 10000 + paidMonthly.getYear()) * 100 + paidMonthly.getPaymentType());
        paidMonthly.setAmount(-100d);
        servicePointPaidMonthlyDao.incrAmount(paidMonthly);
    }

    @Test
    public void getMinusList() {
        List<ServicePointPayableMonthlyDetail> details = Lists.newArrayListWithCapacity(3);
        ServicePointPayableMonthlyDetail detail = new ServicePointPayableMonthlyDetail();
        detail.setTotalId(1627630201910l);
        detail.setMonth(10);
        details.add(detail);
        detail = new ServicePointPayableMonthlyDetail();
        detail.setTotalId(1627630201910l);
        detail.setMonth(9);
        details.add(detail);
        List<ServicePointPayableMonthlyDetail> payableMinusMonthlyDetailList = servicePointDao.getPayableMinusMonthlyDetailList(details);
        String s = "";
    }

    @Test
    public void sendEngineerCurrencyMessage() {
        MQCreateEngineerCurrencyMessage.CreateEngineerCurrencyMessage.Builder createEngineerCurrencyMessageBuilder = MQCreateEngineerCurrencyMessage.CreateEngineerCurrencyMessage.newBuilder()
                .setMessageId(100005l)
                .setServicePointId(1627630l)
                .setCurrencyType(EngineerCurrency.CURRENCY_TYPE_OUT)
                .setCurrencyNo("withdrawNo001")
                .setBeforeBalance(162.0)
                .setBalance(0)
                .setAmount(162.0)
                .setPaymentType(EngineerCurrency.PAYMENT_TYPE_TRANSFER_ACCOUNT)
                .setActionType(EngineerCurrency.ACTION_TYPE_PAY)
                .setCreateById(1l)
                .setCreateDate(System.currentTimeMillis())
                .setRemarks(String.format("本次付款%.2f元(扣除平台服务费%.2f元,实际付款%.2f元), %s", 162.0, -8.1, 153.9, "remarks"))
                .setQuarter("20194");
        //生成按品类金额异动消息
        MQCreateEngineerCurrencyMessage.IncreaseAmountByProductCategory increaseAmountByProductCategory = MQCreateEngineerCurrencyMessage.IncreaseAmountByProductCategory.newBuilder()
                .setPaymentType(10)
                .setPayForYear(2019)
                .setPayForMonth(10)
                .setInvoiceYear(2019)
                .setInvoiceMonth(10)
                .setAmount(277.0)
                .build();
        createEngineerCurrencyMessageBuilder.setIncreaseAmountByProductCategory(increaseAmountByProductCategory);
        //生成抵扣消息
        List<MQCreateEngineerCurrencyMessage.DeductionByProductCategory> deductionByProductCategories = Lists.newArrayListWithCapacity(3);
        MQCreateEngineerCurrencyMessage.DeductionByProductCategory deductionByProductCategory = MQCreateEngineerCurrencyMessage.DeductionByProductCategory.newBuilder()
                .setProductCategoryId(1)
                .setAmount(-10)
                .build();
        deductionByProductCategories.add(deductionByProductCategory);

        deductionByProductCategory = MQCreateEngineerCurrencyMessage.DeductionByProductCategory.newBuilder()
                .setProductCategoryId(2)
                .setAmount(-25)
                .build();
        deductionByProductCategories.add(deductionByProductCategory);

        deductionByProductCategory = MQCreateEngineerCurrencyMessage.DeductionByProductCategory.newBuilder()
                .setProductCategoryId(4)
                .setAmount(13)
                .build();
        deductionByProductCategories.add(deductionByProductCategory);

        createEngineerCurrencyMessageBuilder.addAllDeductionByProductCategories(deductionByProductCategories);
        //发送生成网点流水消息
        MQCreateEngineerCurrencyMessage.CreateEngineerCurrencyMessage createEngineerCurrencyMessage = createEngineerCurrencyMessageBuilder.build();
        try {
            createEngineerCurrencySender.send(createEngineerCurrencyMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    @Test
//    public void updatePayableDetailMonthly() {
//        fiSchedules.updatePayableDetailMonthly(2019, 11, "2019/11/1 0:00:00", "2019/11/4 20:23:00");
//    }
//
    @Test
    public void updateBalanceDetailMonthly() {
        fiSchedules.updateBalanceDetailMonthly(2019, 11, "2019/11/1 0:00:00", "2019/11/4 16:50:00");
    }


    @Test
    public void reSendEngineerCurrencyMessage() throws Exception {
        List<String> messageStrings = Lists.newArrayList();  //log2Dao.getParamsForInsertEngineerCurrencyMessage();  //mark on 2020-7-11 sys_log2微服务化
        for (String messageString : messageStrings) {
            MQCreateEngineerCurrencyMessage.CreateEngineerCurrencyMessage.Builder createEngineerCurrencyMessageBuilder =
                    MQCreateEngineerCurrencyMessage.CreateEngineerCurrencyMessage.newBuilder();
            new JsonFormat().merge(new ByteArrayInputStream(messageString.getBytes("utf-8")), createEngineerCurrencyMessageBuilder);
            MQCreateEngineerCurrencyMessage.CreateEngineerCurrencyMessage createEngineerCurrencyMessage =
                    createEngineerCurrencyMessageBuilder.build();
            createEngineerCurrencySender.send(createEngineerCurrencyMessage);
        }
    }
}
