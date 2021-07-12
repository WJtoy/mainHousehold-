package com.wolfking.jeesite.modules.sd.task;

import com.kkl.kklplus.utils.NumberUtils;
import com.wolfking.jeesite.common.config.Global;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.ParseException;

/**
 * 订单定时任务
 * 1.每周一凌晨2:00清除登录信息(redis db0)
 *
 * Created on 2017-11-20
 */
@Component
@Lazy(value = false)/*need*/
@Slf4j
public class OrderSchedules
{

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    protected OrderService orderService;


    /**
     * 定时清除登录信息
     */
    //@Scheduled(fixedRate = 5000) 每5秒执行一次
    //@Scheduled(initialDelay = 1000, fixedDelay = 3000) 第一次延迟1秒执行，当执行完后3秒再执行
    //@Scheduled(cron = "50 27 23 * * ?") 每天23点27分50秒时执行
    //@Scheduled(cron = "00 00 02 * * ?") 每天23点27分50秒时执行
    //@Scheduled(cron = "0 00 02 ? * *")
    //@Scheduled(cron = "0 0 3 ? * 2,4,6") //每周1，3，5凌晨3点运行
    @Scheduled(cron = "00 00 3 * * ?") //每天凌晨3点运行
    public void ClearWebLoginInfo() throws ParseException {
        boolean scheduleEnabled = Boolean.valueOf(Global.getConfig("scheduleEnabled"));
        if (!scheduleEnabled) {
            return;
        }
        try {
            redisUtils.redisTemplate.execute(new RedisCallback() {
                public String doInRedis(RedisConnection connection) throws DataAccessException {
                    connection.select(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB.ordinal());
                    connection.flushDb();
                    return "ok";
                }
            });
            LogUtils.saveLog("定时任务", "清除登录信息", DateUtils.getDateTime(), null, null);
        } catch (Exception e) {
            LogUtils.saveLog("定时任务", "清除登录信息", DateUtils.getDateTime(), e, null);
        }
    }


   /* @Scheduled(cron = "${updateSubAreaIdCron}")
    public void UpdateSubAreaId() {
        long startTime = System.currentTimeMillis();
        boolean needUpdateSubAreaId = Boolean.parseBoolean(Global.getConfig("updateSubAreaId.enabled"));
        if (!needUpdateSubAreaId) {
            return;
        }
        String quarter = Global.getConfig("updateSubAreaId.quarter");
        String status = Global.getConfig("updateSubAreaId.status");
        orderService.updateSubAreaId(quarter, status);
        long endTime = System.currentTimeMillis();
        double spendSeconds = (endTime - startTime) / 1000d;
        try {
            StringBuilder message = new StringBuilder();
            message.append("开始时间:")
                    .append(DateUtils.formatDateTime(DateUtils.longToDate(startTime)))
                    .append(",结束时间:")
                    .append(DateUtils.formatDateTime(DateUtils.longToDate(endTime)))
                    .append(",用时:")
                    .append(NumberUtils.formatNum(spendSeconds))
                    .append("秒");
            LogUtils.saveLog("定时任务", "updateSubAreaId", message.toString(), null, null);
        } catch (Exception e) {
            LogUtils.saveLog("定时任务", "updateSubAreaId", "", e, null);
        }
    }*/
    /**
     * 转历史订单
     * 每天凌晨1,4,6点
    //@Scheduled(cron = "0 50 10 ? * *") //run at 10:10
    @Scheduled(cron = "0 0 1,4,6 ? * *")
    public void OrderToHistory() {
        boolean scheduleEnabled = Boolean.valueOf(Global.getConfig("scheduleEnabled"));
        if (!scheduleEnabled) {
            return;
        }
        Long start = System.currentTimeMillis();
        try {
            Date endDate = DateUtils.addDays(new Date(),-1);
            endDate = DateUtils.getDateEnd(endDate);
            Date startDate = DateUtils.getDateStart(DateUtils.addDays(endDate,-30));//一个月
            Integer total=0;
            int success = 0;
            List<Map<String, Object>> list= orderService.findTransToHistoryList(startDate,endDate,6000);
            if(list != null && list.size()>0){
                total = list.size();
                Long orderId;
                Integer status;
                String quarter = new String("");
                Date closeDate;
                int orderType;
                Map<String,Object> map;
                for(int i=0;i<total;i++) {
                    orderId = null;
                    try {
                        map = list.get(i);
                        status = (Integer)map.get("status");
                        switch (status){
                            case 80:
                                orderType=10;
                                break;
                            case 90:
                                orderType = 40;//return
                                break;
                            case 100:
                                orderType = 30;//cancel
                                break;
                            default:
                                orderType = 0;
                                break;
                        }
                        if(orderType==0){
                            continue;
                        }
                        orderId = (Long)map.get("order_id");
                        quarter = (String) map.get("quarter");
                        closeDate = (Date) map.get("close_date");
                        try {
                            orderDetailReportService.SaveHisOrderDetailById(orderId,quarter, orderType, closeDate);
                            success++;
                            try{
                                Thread.sleep(200l);
                            }catch (Exception e){}
                            //System.out.println(">>>PASS :" + orderId.toString());
                        } catch (Exception ex) {
                            //System.out.println(">>>FAIL :" + orderId.toString());
                            LogUtils.saveLog("历史订单记录失败","OrderToHistory",orderId.toString(),ex,null);
                        }
                    } catch (Exception e) {
                        LogUtils.saveLog("历史订单记录失败","OrderToHistory",orderId==null?"":orderId.toString(),e,null);
                    }
                }
            }
            Long end = System.currentTimeMillis();
            StringBuilder message = new StringBuilder();
            double time = 1.0d*(end-start)/1000d;
            message.append("运行时间:")
                    .append(DateUtils.formatDateTime(DateUtils.longToDate(start)))
                    .append(",总数:").append(total.toString())
                    .append(",成功数:").append(String.valueOf(success))
                    .append(",用时:")
                    .append(StringUtils.formatNum(time))
                    .append("秒");
            LogUtils.saveLog("定时任务", "OrderToHistory", message.toString(), null, null);
        } catch (Exception e) {
            LogUtils.saveLog("定时任务","OrderToHistory","错误,运行时间:" + DateUtils.formatDateTime(DateUtils.longToDate(start)),e,null);
        }
    }
     */

}
