package com.wolfking.jeesite.modules.md.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.service.BaseService;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.wolfking.jeesite.modules.md.dao.ServicePointDao;
import com.wolfking.jeesite.modules.md.entity.ServicePointFinance;
import com.wolfking.jeesite.modules.md.utils.ServicePointAdapter;
import com.wolfking.jeesite.modules.md.utils.ServicePointFinanceAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ServicePointFinanceService extends BaseService {
    @Resource
    private ServicePointDao servicePointDao;

    @Autowired
    private RedisUtils redisUtils;

    private static final int SHARDING_COUNT = 40;  //分片数量

    /**
     *  获取所有的网点财务数据  2020-5-1
     * @return
     */
    public List<ServicePointFinance> findAllFinanceList() {
        List<ServicePointFinance> servicePointFinanceList = Lists.newArrayList();
        int pageNo = 1;
        Page<ServicePointFinance> page = new Page<>();
        page.setPageNo(pageNo);
        page.setPageSize(1000);
        servicePointFinanceList.addAll(servicePointDao.findAllFinanceList(page));
        while(pageNo < page.getPageCount()) {
            pageNo++;
            page.setPageNo(pageNo);
            servicePointFinanceList.addAll(servicePointDao.findAllFinanceList(page));
        }
        return servicePointFinanceList;
    }

    public Integer reloadAllToCache() {
        //
        //  重载缓存
        //
        int[] arrayRowCount = new int[1];
        arrayRowCount[0] = 0;
        try {
            List<ServicePointFinance> servicePointFinanceList = findAllFinanceList();
            if (ObjectUtils.isEmpty(servicePointFinanceList)) {
                return arrayRowCount[0];
            }
            Map<Long, List<ServicePointFinance>> servicePointFinanceMaps = servicePointFinanceList.stream().collect(Collectors.groupingBy(r -> r.getId() % SHARDING_COUNT));
            StringBuffer key = new StringBuffer();
            List<String> fields = Lists.newArrayList();

            servicePointFinanceMaps.forEach((k, v) -> {
                key.setLength(0);
                key.append(String.format(RedisConstant.MD_SERVICEPOINT_FINANCE, k.intValue()));

                Map<String, Object> objectMap = Maps.newHashMap();
                v.forEach(servicePointFinance -> {
                    objectMap.put(servicePointFinance.getId().toString(), servicePointFinance);
                    fields.add(servicePointFinance.getId().toString());
                    arrayRowCount[0]++;
                });
                String[] arrayFields = new String[fields.size()];

                redisUtils.hdel(RedisConstant.RedisDBType.REDIS_MD_DB, key.toString(), fields.toArray(arrayFields));  //del
                redisUtils.hmSetAll(RedisConstant.RedisDBType.REDIS_MD_DB, key.toString(), objectMap, -1);
            });
        } catch (Exception ex) {
            log.error("重载网点财务缓存出错.出错原因:{}", ex.getStackTrace());
        }
        return arrayRowCount[0];
    }

    public ServicePointFinance getFromCache(Long servicePointId) {
        if (servicePointId == null || servicePointId <= 0) {
            throw new RuntimeException("输入的网点Id错误!");
        }
        long modKey = servicePointId % SHARDING_COUNT;
        String key = String.format(RedisConstant.MD_SERVICEPOINT_FINANCE, modKey);
        ServicePointFinance servicePointFinance = redisUtils.hGet(RedisConstant.RedisDBType.REDIS_MD_DB, key, servicePointId.toString(), ServicePointFinance.class);
        if (servicePointFinance == null) {
            // 缓存中获取到数据为空，从DB中获取
            servicePointFinance = servicePointDao.getFinanceNew(servicePointId);
        }

        return servicePointFinance;
    }

    public void updateCache(ServicePointFinance servicePointFinance) {
        if (servicePointFinance == null) {
            throw new RuntimeException("网点财务对象为空!");
        }
        if (servicePointFinance.getId() == null || servicePointFinance.getId() <= 0) {
            throw new RuntimeException("输入的网点Id错误!");
        }
        Long servicePointId = servicePointFinance.getId();
        long modKey = servicePointId % SHARDING_COUNT;
        String key = String.format(RedisConstant.MD_SERVICEPOINT_FINANCE, modKey);
        String lockkey = String.format("lock:servicepointfinance:%s", servicePointId);
        //获得锁
        Boolean locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey, 1, 60);//1分钟
        if (!locked) {
            throw new RuntimeException("网点财务正在修改中，请稍候重试。");
        }

        try {
            redisUtils.hmSet(RedisConstant.RedisDBType.REDIS_MD_DB, key, servicePointId.toString(), servicePointFinance, -1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (locked && lockkey != null) {
                redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey);
            }
        }
    }





//    /**
//     * 更新网点财务缓存
//     * @param servicePointId  网点id
//     * @param balance  账户余额
//     */
//    public void updateCacheForBalance(Long servicePointId, double balance) {
//        if (servicePointId == null || servicePointId <= 0) {
//            throw new RuntimeException("输入的网点Id错误!");
//        }
//        String lockkey = String.format("lock:servicepointfinance:%s", servicePointId);
//        //获得锁
//        Boolean locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey, 1, 60);//1分钟
//        if (!locked) {
//            throw new RuntimeException("网点财务正在修改账户余额，请稍候重试。");
//        }
//
//        try {
//            long modKey = servicePointId % SHARDING_COUNT;
//            String key = String.format(RedisConstant.MD_SERVICEPOINT_FINANCE, modKey);
//            StringBuilder stringBuilder = new StringBuilder();
//            stringBuilder.append(servicePointId.toString()).append(":").append("balance");
//            redisUtils.hmSet(RedisConstant.RedisDBType.REDIS_MD_DB, key, stringBuilder.toString(), balance,-1);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        } finally {
//            if (locked && lockkey != null) {
//                redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey);
//            }
//        }
//    }

//    /**
//     * 更新网点财务缓存
//     * @param servicePointId 网点id
//     * @param balance   余额
//     * @param lastPayDate  最后付款日期
//     * @param lastPayAmount  最后付款金额
//     */
//    public void updateCacheAAA(Long servicePointId, double balance, Date lastPayDate, double lastPayAmount) {
//        if (servicePointId == null || servicePointId <= 0) {
//            throw new RuntimeException("输入的网点Id错误!");
//        }
//        String lockkey = String.format("lock:servicepointfinance:%s", servicePointId);
//        //获得锁
//        Boolean locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey, 1, 60);//1分钟
//        if (!locked) {
//            throw new RuntimeException("网点财务正在修改中，请稍候重试。");
//        }
//
//        Map<String, Object> map = Maps.newHashMap();
//        try {
//            long modKey = servicePointId % SHARDING_COUNT;
//            String key = String.format(RedisConstant.MD_SERVICEPOINT_FINANCE, modKey);
//            StringBuilder stringBuilder = new StringBuilder();
//            stringBuilder.setLength(0);
//            stringBuilder.append(servicePointId.toString()).append(":").append("balance");
//            map.put(stringBuilder.toString(), balance);
//
//            stringBuilder.setLength(0);
//            stringBuilder.append(servicePointId.toString()).append(":").append("LastPayDate");
//            map.put(stringBuilder.toString(), lastPayDate);
//
//            stringBuilder.setLength(0);
//            stringBuilder.append(servicePointId.toString()).append(":").append("lastPayAmount");
//            map.put(stringBuilder.toString(), lastPayAmount);
//            redisUtils.hmSetAll(RedisConstant.RedisDBType.REDIS_MD_DB, key, map, -1);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        } finally {
//            if (locked && lockkey != null) {
//                redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey);
//            }
//        }
//    }
}
