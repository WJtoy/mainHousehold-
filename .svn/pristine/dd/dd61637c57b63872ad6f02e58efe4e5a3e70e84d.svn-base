/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.wolfking.jeesite.modules.sd.service;

import com.google.common.collect.Maps;
import com.wolfking.jeesite.common.config.Global;
import com.wolfking.jeesite.common.config.redis.GsonRedisSerializer;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.service.LongIDBaseService;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.service.CustomerService;
import com.wolfking.jeesite.modules.md.utils.CustomerUtils;
import com.wolfking.jeesite.modules.sd.dao.OrderTaskDao;
import com.wolfking.jeesite.modules.sd.entity.viewModel.RepeateOrderVM;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.math.LongRange;
import org.apache.jasper.tagplugins.jstl.core.ForEach;
import org.assertj.core.util.Lists;
import org.joda.time.DateTime;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 订单Job Service
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Slf4j
public class OrderTaskService extends LongIDBaseService {

    public final static String CHECK_REPEATE_ORDER_KEY = "repeate:order:check:{0}";

    /**
     * 持久层对象
     */
    @Resource
    protected OrderTaskDao dao;

    @Autowired
    protected CustomerService customerService;

    @Autowired
    private RedisUtils redisUtils;

    /**
     * 定时处理将未即时自动对账的订单转为手动对账
     */
    //@Transactional(readOnly = false)
    public void updateToManualCharge() {

       DateTime endDate = new DateTime();
       endDate.plusMinutes(-15);
       DateTime beginDate = endDate.minusDays(30);
       DateTime quarterDate = endDate.minusMonths(18);//18个月，最多7个分片
        String goLiveDateStr = Global.getConfig("GoLiveDate");
        DateTime goLiveDate;
        goLiveDate = new DateTime(goLiveDateStr);
        if(goLiveDate.isAfter(quarterDate)){
            quarterDate = goLiveDate;
        }
        List<String> quarters = QuarterUtils.getQuarters(quarterDate.toDate(), endDate.toDate());
        try {
            beginDate = beginDate.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0);
            for(String quarter:quarters){
                //log.warn("\ndao.updateToManualCharge('{}','{}','{}')",quarter,beginDate.toString("yyyy-MM-dd HH:mm:ss"),endDate.toString("yyyy-MM-dd HH:mm:ss"));
                dao.updateToManualCharge(quarter,beginDate.toDate(),endDate.toDate());
                TimeUnit.SECONDS.sleep(1);
            }
        } catch (Exception e) {
            log.error("[OrderTaskService.updateToManualCharge] 自动对账状态转手工对账错误", e);
            throw new RuntimeException("自动对账状态转手工对账错误");
        }
    }

    /**
     * 将30天内下单信息同步到缓存
     * type: hash
     * key: repeate:order:check:[customerId]
     * field: 手机号
     * value: 订单号
     */
    public void reloadCheckRepeatOrderCache(Date beginDate,Date endDate){
        //String quarter =
        List<String> quarters = QuarterUtils.getQuarters(beginDate, endDate);
        //all customers
        //按客户分多次取数据

        long maxId = 0l;
        Customer customer = CustomerUtils.getMaxCustomer();
        if(customer == null){
            return;
        }

        maxId = customer.getId();
        if(maxId<=0){
            return;
        }
        long minId = 1;
        long perOfGroup = 1000;//600读取一次
        Double d = Math.ceil(1.0*maxId/perOfGroup);
        int grpNum = d.intValue();
        List<LongRange> customerIdGroups = com.google.common.collect.Lists.newArrayList();
        for(int i=0;i<grpNum;i++){
            if(i == grpNum-1){
                customerIdGroups.add(new LongRange(i*perOfGroup+1,maxId));
            }else{
                customerIdGroups.add(new LongRange(i*perOfGroup+1,perOfGroup*(i+1)));
            }
        }
        Map<Long,List<RepeateOrderVM>> maps = Maps.newHashMap();
        CompletableFuture[] cfs = customerIdGroups.stream()
                .map(range -> CompletableFuture.supplyAsync(() -> {
                            List<RepeateOrderVM> list = dao.getOrderCreateInfo(null,quarters,
                                    beginDate,endDate,
                                    range.getMinimumLong(),range.getMaximumLong()
                                );
                            return list;
                            }).whenComplete((s, t) -> {
                                if (t != null) {
                                    System.out.println(t.getMessage());
                                }else {
                                    maps.put(range.getMinimumLong(), s);
                                }
                            })
                ).toArray(CompletableFuture[]::new);
        //等待所有任务完成
        CompletableFuture.allOf(cfs).join();

        //redis
        long start2 = System.currentTimeMillis();
        StringBuffer sbKey = new StringBuffer(100);
        List<RepeateOrderVM> list;
        //按客户+电话分组后的结果
        GsonRedisSerializer gsonRedisSerializer = redisUtils.gsonRedisSerializer;
        RedisTemplate redisTemplate = redisUtils.redisTemplate;
        final int dbIndex = RedisConstant.RedisDBType.REDIS_TEMP_DB.ordinal();
        //clear
        List<Long> clearIds = Lists.newArrayList();
        for(long i=1;i<=maxId;i++){
            clearIds.add(i);
        }
        List<List<Long>> clearGrps = com.google.common.collect.Lists.partition(clearIds, 20);
        clearGrps.stream().forEach(t -> {
            redisTemplate.execute(new RedisCallback<Object>() {
                @Override
                public Boolean doInRedis(RedisConnection connection)
                        throws DataAccessException {
                    connection.select(dbIndex);
                    try {
                        for(Long cid:t){
                            sbKey.setLength(0);
                            sbKey.append(MessageFormat.format(CHECK_REPEATE_ORDER_KEY,cid.toString()));
                            final byte[] bkey = sbKey.toString().getBytes(StandardCharsets.UTF_8);
                            connection.del(bkey);
                        }
                        return true;
                    } catch (Exception e) {
                        log.error("[RedisUtils.set]", e);
                        return false;
                    }
                }
            });
        });
        //add
        for(Map.Entry<Long,List<RepeateOrderVM>> entry :maps.entrySet()){
            list = entry.getValue();
            if(list != null && !list.isEmpty()) {
                System.out.println("key: " + entry.getKey() + " ,size:" + list.size());
                //cache
                //按客户+电话分组后的结果
                Map<Long,Map<String,Optional<RepeateOrderVM>>> cacheMap = list.stream()
                        .collect(
                            Collectors.groupingBy(
                                    RepeateOrderVM::getCustomerId,
                                    Collectors.groupingBy(RepeateOrderVM::getPhone,Collectors.maxBy(Comparator.comparing(RepeateOrderVM::getOrderId)))
                                    //Collectors.toMap(RepeateOrderVM::getPhone, item -> item.getOrderNo())
                            )
                        );
                redisTemplate.execute(new RedisCallback<Object>() {
                    @Override
                    public Boolean doInRedis(RedisConnection connection)
                            throws DataAccessException {
                        try {
                            connection.select(dbIndex);
                            for(Map.Entry<Long,Map<String,Optional<RepeateOrderVM>>> subEntry:cacheMap.entrySet()) {
                                sbKey.setLength(0);
                                sbKey.append(MessageFormat.format(CHECK_REPEATE_ORDER_KEY,subEntry.getKey().toString()));
                                //sbKey.append("repeate:order:check:").append(subEntry.getKey().toString());
                                final byte[] bkey = sbKey.toString().getBytes(StandardCharsets.UTF_8);
                                final Map<byte[], byte[]> bmap = new HashMap<>();
                                subEntry.getValue().forEach(
                                        (k, v) -> {
                                            if(v.isPresent()) {
                                                byte[] field = k.getBytes(StandardCharsets.UTF_8);
                                                byte[] value = v.get().getOrderNo().getBytes(StandardCharsets.UTF_8);
                                                //byte[] value = gsonRedisSerializer.serialize(v);
                                                bmap.put(field, value);
                                            }
                                        }
                                );

                                connection.del(bkey);
                                connection.hMSet(bkey, bmap);
                            }
                                return true;
                            } catch (Exception e) {
                                log.error("[reloadRepeatOrderCache]", e);
                                return false;
                            }
                        }
                    });


            }else{
                System.out.println("key: " + entry.getKey() + " ,size: 0");
            }
        }

        long end = System.currentTimeMillis();
        System.out.println("finsih write cache ,use time:" + (end-start2)/1000.0);
    }

}
