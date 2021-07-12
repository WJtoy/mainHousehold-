package com.wolfking.jeesite.test.sys;

import com.alipay.config.AlipayConfig;
import com.kkl.kklplus.entity.sys.SysLog;
import com.kkl.kklplus.entity.sys.mq.MQSysLogMessage;
import com.wolfking.jeesite.common.config.Global;
import com.wolfking.jeesite.modules.mq.sender.LogSender;
import com.wolfking.jeesite.modules.sys.service.LogService;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.ms.b2bcenter.sd.service.B2BCenterOrderService;
import com.wolfking.jeesite.ms.jd.sd.service.JdOrderService;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * Created by ryan
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class LogTest {

    @Autowired
    private LogSender logSender;

    /*
    // mark on 2020-7-11
    @Autowired
    private LogService logService;
    */


    @Test
    public void testAsycLog() {
        System.out.println("start");

        System.out.println("end");
    }

    @Test
    public void testGetConfig() {

        String PartnerIP = AlipayConfig.AlipayHTTP;
        String notify_url = PartnerIP + Global.getAdminPath() + "/fi/customercurrency/async";
        System.out.println("notify_url:" + notify_url);
        // 需http://格式的完整路径，不能加?id=123这类自定义参数
        // 页面跳转同步通知页面路径
        // String return_url =
        // "http://117.136.40.242:8080/jeesite/modules/sd/return_url";
        String return_url = PartnerIP + Global.getAdminPath() + "/fi/customercurrency/return_url";
        System.out.println("return_url:" + return_url);
    }

    @Test
    public void testLogSender() {
        MQSysLogMessage.SysLogMessage logMessage = MQSysLogMessage.SysLogMessage.newBuilder()
                .setType(SysLog.TYPE_EXCEPTION)
                .setTitle("Web测试日志")
                .setRemoteAddr("localhost")
                .setRequestUri("LogTest.testLogSender")
                .setMethod("GET")
                .setParams("null")
                .setUserAgent("")
                .setException("null")
                .setCreateBy(11L)
                .setCreateDate(System.currentTimeMillis())
                .setQuarter("20183").build();
        logSender.send(logMessage);
    }

    @Test
    public void testLogUtils() {
        //LogUtils.saveLog("Web测试日志","LogTest.testLogSender","123",null,null,SysLog.TYPE_EXCEPTION);
        //LogUtils.saveLog("Web测试日志","LogTest.testLogSender","123",null,new User(1l),SysLog.TYPE_EXCEPTION);
        LogUtils.saveLog("自动派单", "OrderAutoPlan", "orderNo", null, null, SysLog.TYPE_EXCEPTION);
    }

    @Autowired
    private JdOrderService jdOrderService;

    @Test
    public void retryCancelJdOrder() {
        //jdOrderService.manualCancel();  //mark on 2020-7-11 log2微服务化
    }

    @Autowired
    public B2BCenterOrderService b2BCenterOrderService;

    @Test
    public void retryCompleted() {
        List<Long> ids = Lists.newArrayList();

        ids.add(1213407232579211264L);
        ids.add(1215261943833825280L);
        ids.add(1216371758358466560L);
        ids.add(1216890224383234049L);
        ids.add(1217076399853277184L);
        ids.add(1217604886255243265L);
        ids.add(1217778532361834497L);
        ids.add(1210067720276676608L);
        ids.add(1223876205527568385L);
        ids.add(1225451595983228929L);
        ids.add(1225668012720394240L);
        ids.add(1226032916468076545L);


        b2BCenterOrderService.retryCompletedOrder(ids);
    }

    @Test
    public void retryReturnOrder() {
        List<Long> ids = Lists.newArrayList();
        ids.add(1167673384109621248L);


        b2BCenterOrderService.retryReturnOrder(ids);
    }

    @Test
    public void retryCancelOrder() {
        List<Long> ids = Lists.newArrayList();

        b2BCenterOrderService.retryCancelOrder(ids);
    }
}
