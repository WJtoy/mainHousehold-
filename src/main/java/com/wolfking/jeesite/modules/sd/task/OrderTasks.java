package com.wolfking.jeesite.modules.sd.task;

import com.wolfking.jeesite.common.config.Global;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.modules.sd.service.OrderTaskService;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.modules.sys.utils.SeqUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Date;

/**
 * 订单任务
 * Ryan 2017/5/19.
 */
@Component
/* @Lazy(value = false) scheduled need*/
@Order(value=3)
public class OrderTasks implements CommandLineRunner {

    @Autowired
    protected OrderService orderService;

    @Autowired
    private OrderTaskService orderTaskService;

    @SuppressWarnings("rawtypes")
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedisUtils redisUtils;

    /**
     * 定时处理将未即时自动对账的订单转为手动对账
     * autoChargeFlag = 3 -> 2
     *
     * 每天07:15,12:15,17:15,22:15 执行
     */
    @Scheduled(cron = "0 15 7,12,17,22 * * ?")
    public void transferToManualCharge() throws ParseException {
        boolean scheduleEnabled = Boolean.valueOf(Global.getConfig("scheduleEnabled"));
        if (!scheduleEnabled) {
            return;
        }
        try {
            orderTaskService.updateToManualCharge();
            LogUtils.saveLog("定时任务", "自动对账订单转为手动对账", DateUtils.getDateTime(), null, null);
        } catch (Exception e) {
            LogUtils.saveLog("定时任务", "自动对账订单转为手动对账", DateUtils.getDateTime(), e, null);
        }
    }

    /**
     * 产生订单号
     *
     * 每天00:01 执行
     */
    @Scheduled(cron = "1 0 0 * * ?")
    public void generateAndStoreDialyOrderNo() throws ParseException {
        boolean scheduleEnabled = Boolean.valueOf(Global.getConfig("scheduleEnabled"));
        if (!scheduleEnabled) {
            return;
        }
        try {
            String orderNo = SeqUtils.NextOrderNo();
            StringBuilder msg = new StringBuilder(100);
            msg.append("生成订单号码 ").append(DateUtils.getDateTime());
            LogUtils.saveLog("定时任务", msg.toString(), DateUtils.getDateTime(), null, null);
        } catch (Exception e) {
            LogUtils.saveLog("定时任务", "生成订单号码", DateUtils.getDateTime(), e, null);
        }
    }

    /**
     * 90天内下单信息，用于重单检查
     * 2021-02-27 由30天更改为3个月
     * 每天00:05 执行
     */
    @Scheduled(cron = "0 5 0 * * ?")
    public void reloadCheckRepeatOrderCache() throws ParseException {
        boolean scheduleEnabled = Boolean.valueOf(Global.getConfig("scheduleEnabled"));
        if (!scheduleEnabled) {
            return;
        }
        try {
            Date endDate = DateUtils.getEndOfDay(new Date());
            Date beginDate = DateUtils.addMonth(endDate,-3);
            beginDate = DateUtils.getDateStart(beginDate);
            orderTaskService.reloadCheckRepeatOrderCache(beginDate,endDate);
            StringBuilder msg = new StringBuilder(100);
            msg.append("更新30天内下单缓存 [").append(DateUtils.formatDate(beginDate,"yyyy-MM-dd HH:mm:ss"))
                    .append("~").append(DateUtils.formatDate(endDate,"yyyy-MM-dd HH:mm:ss"))
                    .append("]");
            LogUtils.saveLog("定时任务", msg.toString(), DateUtils.getDateTime(), null, null);
        } catch (Exception e) {
            LogUtils.saveLog("定时任务", "更新30天内下单缓存", DateUtils.getDateTime(), e, null);
        }
    }

    @Override
    public void run(String... args) throws Exception {
        boolean loadInRedis = Boolean.valueOf(Global.getConfig("loadInRedis"));
        if (loadInRedis) {
            orderService.reloadNoticeMessage();
        }
    }


}
