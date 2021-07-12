package com.wolfking.jeesite.modules.mq.task;

import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.entity.fi.mq.MQRechargeOrderMessage;
import com.kkl.kklplus.utils.NumberUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.config.Global;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.fi.entity.CustomerCurrency;
import com.wolfking.jeesite.modules.fi.service.ChargeServiceNew;
import com.wolfking.jeesite.modules.fi.service.CustomerCurrencyService;
import com.wolfking.jeesite.modules.mq.entity.MQRecharge;
import com.wolfking.jeesite.modules.mq.entity.OrderCharge;
import com.wolfking.jeesite.modules.mq.service.MQRechargeService;
import com.wolfking.jeesite.modules.mq.service.OrderChargeService;
import com.wolfking.jeesite.modules.sys.entity.Log2;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.ms.recharge.mq.sender.RechargeOrderMQSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * 消息任务
 * Ryan 2017/5/19.
 */
@Slf4j
@Component
@Lazy(value = false)
public class MQTasks {

    @Autowired
    private OrderChargeService orderChargeService;

    @Autowired
    private ChargeServiceNew chargeServiceNew;

    @Autowired
    private MQRechargeService mqRechargeService;

    @Autowired
    private CustomerCurrencyService customerCurrencyService;

    @Autowired
    private RechargeOrderMQSender rechargeOrderMQSender;

    //region 支付微服务

    /**
     * 重新消费充值失败的消息
     */
    //每5分钟一次
    @Scheduled(cron = "0 0/5 * * * ?")
    public void retryMQRechargeMessage(){
        boolean scheduleEnabled = Boolean.valueOf(Global.getConfig("scheduleEnabled"));
        if (!scheduleEnabled) {
            return;
        }
        Date date = new Date();
        date = DateUtils.addDays(date,-1);
        List<MQRecharge> list = mqRechargeService.getRetryList(date.getTime(),System.currentTimeMillis(),null);
        MQRecharge mqRecharge;
        MQRechargeOrderMessage.RechargeOrderMessage message;
        for(int i=0,size=list.size();i<size;i++){
            mqRecharge = list.get(i);
            if(StringUtils.isBlank(mqRecharge.getMessageContent())){
                try {
                    mqRechargeService.mqConsumeFail(mqRecharge.getCustomerId(), mqRecharge.getId(), "数据库中messageContent无内容");
                }catch (Exception e){
                    log.error("更新重试结果错误",e);
                    LogUtils.saveLog("MQTask:retryMQRechargeMessage","mqConsumeFail","更新重试结果错误" ,e,null, Log2.TYPE_EXCEPTION);
                }
                continue;
            }
            message = fromJson(mqRecharge.getMessageContent());
            if(message != null){
                CustomerCurrency customerCurrency = customerCurrencyService.get(message.getId());
                //已充值
                if(customerCurrency != null){
                    mqRechargeService.mqConsumeSuccess(message.getReferId(),message.getId());
                    continue;
                }
                rechargeOrderMQSender.sendDelay(message,0,3);//3:mq只重试1次
            }else{
                try {
                    mqRechargeService.mqConsumeFail(mqRecharge.getCustomerId(), mqRecharge.getId(), "fromJson错误，请检查日志文件");
                }catch (Exception e){
                    log.error("更新重试结果错误",e);
                    LogUtils.saveLog("MQTask:retryMQRechargeMessage","mqConsumeFail","更新重试结果错误" ,e,null, Log2.TYPE_EXCEPTION);
                }
            }
        }
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
            LogUtils.saveLog("MQTask:retryMQRechargeMessage","fromJson","json转消息体错误" ,e,null, Log2.TYPE_EXCEPTION);
            log.error("json转消息体错误",e);
        }
        return null;
    }

    //endregion

    /**
     * 自动对账异常处理
     * orderCondition.autoChargeFlag=3
     * 每天凌晨0,3点10分执行
     */
    @Scheduled(cron = "0 10 0,3 ? * *")
    public void AutoCharge() {
        boolean scheduleEnabled = Boolean.valueOf(Global.getConfig("scheduleEnabled"));
        if (!scheduleEnabled) {
            return;
        }
        Long start = System.currentTimeMillis();
        Date startDate = null;
        Date endDate = DateUtils.addDays(new Date(),-1);
        try {
            endDate = DateUtils.getDateEnd(endDate);
            startDate = DateUtils.getDateStart(DateUtils.addDays(endDate,-5));//5天内
        } catch (Exception e) {
            LogUtils.saveLog("定时任务","AutoCharge","错误，运行时间:" + DateUtils.formatDateTime(DateUtils.longToDate(start)),null,null, Log2.TYPE_EXCEPTION);
            return;
        }

        List<OrderCharge> list;
        try {
            list = orderChargeService.selectRetryList(startDate, endDate, 1000);
            if (list != null && list.size() > 0) {
                for (OrderCharge order : list) {
                    order.setRetryTimes(1);
                    order.setRemarks("");
                    try {
                        chargeServiceNew.createCharge(order.getOrderId(), order.getTriggerBy());
                        order.setStatus(30);
                        orderChargeService.save(order);
                    } catch (Exception e) {
                        if(StringUtils.contains(e.getMessage(),"已经生成对帐单")){
                            order.setStatus(30);
                            order.setRemarks("已经生成对帐单");
                            orderChargeService.save(order);
                        }else {
                            log.error("MQTasks.AutoCharge", e);
                            order.setStatus(40);
                            order.setRemarks(StringUtils.left(e.getMessage(), 250));
                            orderChargeService.save(order);
                        }
                    }
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                    }
                }
            }
            Long end = System.currentTimeMillis();
            StringBuilder message = new StringBuilder();
            double time = 1.0d * (end - start) / 1000d;
            message.append("运行时间:")
                    .append(DateUtils.formatDateTime(DateUtils.longToDate(start)))
                    .append(",用时:")
                    .append(NumberUtils.formatNum(time))
                    .append("秒");
            LogUtils.saveLog("定时任务", "AutoCharge", message.toString(), null, null, Log2.TYPE_ACCESS);
        } catch (Exception e) {
            LogUtils.saveLog("定时任务", "AutoCharge", "错误,运行时间:" + DateUtils.formatDateTime(DateUtils.longToDate(start)), e, null);
        }
    }

}
