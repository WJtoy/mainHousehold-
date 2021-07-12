package com.wolfking.jeesite.modules.md.task;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.wolfking.jeesite.common.config.Global;
import com.wolfking.jeesite.common.config.redis.GsonRedisSerializer;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.Engineer;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.md.entity.ServicePointFinance;
import com.wolfking.jeesite.modules.md.service.*;
import com.wolfking.jeesite.modules.md.utils.CustomerAdapter;
import com.wolfking.jeesite.modules.md.utils.ServicePointAdapter;
import com.wolfking.jeesite.modules.md.utils.ServicePointFinanceAdapter;
import com.wolfking.jeesite.modules.sd.entity.OrderItem;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.dao.DataAccessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 模组基本资料保存至redis
 * Ryan 2017/5/19.
 */
@Slf4j
@Component
@Order(value=2)
public class MDRedisTasks implements CommandLineRunner {

    @Autowired
    ServicePointService servicePointService;

    @Autowired
    ServicePointFinanceService servicePointFinaService;


//    @Autowired
//    CustomerService customerService; //mark on 2020-2-11

    @Autowired
    ProductCategoryService productCategoryService;

    @Autowired
    ProductService productService;

    @Autowired
    ServiceTypeService serviceTypeService;

    @Autowired
    MaterialService materialService;

    /*
    // mark on 2020-1-11
    @Autowired
    CustomerProductCompletePicService CustomerCompletePicService;
    */

    @SuppressWarnings("rawtypes")
    @Autowired
    public RedisTemplate redisTemplate;

    @Resource(name = "gsonRedisSerializer")
    public GsonRedisSerializer gsonRedisSerializer;

    @Resource(name="customerAdapter")
    private CustomerAdapter customerAdapter;

    @Resource(name="servicePointAdapter")
    private ServicePointAdapter servicePointAdapter;

    @Resource(name="servicePointFinanceAdapter")
    private ServicePointFinanceAdapter servicePointFinanceAdapter;

    @Override
    public void run(String... args) throws Exception {
        boolean loadInRedis = Boolean.valueOf(Global.getConfig("loadInRedis"));
        if(!loadInRedis){
            return;
        }
        log.info("服务启动任务：装载MD模组redis数据：");

        log.info("1.客户");
        //zset key:MD:CUSTOMER:ALL score:id
        /*
        // mark on 2020-2-11 begin
        List<Customer> customers = customerService.findAll(); // customerService.findAllList(new Customer());

        // enable Redis Pipeline
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            connection.select(RedisConstant.RedisDBType.REDIS_MD_DB.ordinal());
            connection.del(RedisConstant.MD_CUSTOMER_ALL.getBytes(StandardCharsets.UTF_8));
            customers.forEach(customer -> {
                connection.zAdd(RedisConstant.MD_CUSTOMER_ALL.getBytes(StandardCharsets.UTF_8),customer.getId(),customerAdapter.toJson(customer).getBytes());
            });
            return null;
        });
        customers.clear();
        // mark on 2020-2-11 end
        */

        log.info("2.网点");
        //string key:MD:SERVICEPOINT:ALL

        // mark on 2020-1-15  begin
        /*
        List<ServicePoint> points = servicePointService.findAllList();
        // enable Redis Pipeline
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            connection.select(RedisConstant.RedisDBType.REDIS_MD_DB.ordinal());
            connection.del(RedisConstant.MD_SERVICEPOINT_ALL.getBytes(StandardCharsets.UTF_8));
            for (int i = 0,length = points.size(); i < length; i++){
                ServicePoint point = points.get(i);
                connection.zAdd(RedisConstant.MD_SERVICEPOINT_ALL.getBytes(StandardCharsets.UTF_8),point.getId(),servicePointAdapter.toJson(point).getBytes());
            }
            return null;
        });
        points.clear();
        */
        // mark on 2020-1-15  end

        log.info("3.网点-安维人员");
        //zset key:MD:SERVICEPOINT:ENGINEER:#id score:id
        /*
        // mark on 2019-11-12 //改从Engineer微服务读取缓存
        List<Engineer> engineers = pointService.findAllEngineers();
        //list -> map<pointid,List<Engineer>>
        Map<ServicePoint, List<Engineer>> engineerMaps = engineers.stream().collect(Collectors.groupingBy(Engineer::getServicePoint));
        final StringBuffer key = new StringBuffer();
        // enable Redis Pipeline
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            connection.select(RedisConstant.RedisDBType.REDIS_MD_DB.ordinal());
            engineerMaps.forEach((k,v)->{
                key.setLength(0);
                key.append(String.format(RedisConstant.MD_SERVICEPOINT_ENGINEER,k.getId()));
                connection.del(key.toString().getBytes(StandardCharsets.UTF_8));
                v.forEach(item -> {
                            connection.zAdd(key.toString().getBytes(StandardCharsets.UTF_8),item.getId(),gsonRedisSerializer.serialize(item));
                        });

            });
            return null;
        });
        engineers.clear();
        */

        log.info("4.产品类别");
        //zset key:MD:PRODUCT:CATEGORY:LIST score:id
        /*
        // mark on 2020-1-6
        productCategoryService.delProductCategoryCache();
        productCategoryService.findAllList();
         */

        log.info("5.产品");
        //zset key:MD:PRODUCT:ALL score:id
        //productService.delProductCache();
        //productService.findAllList();

        log.info("6.服务类型");
        //zset key:MD:PRODUCT:ALL score:id
        //serviceTypeService.findAllList();

        log.info("7.网点-价格");
        //zsetMD:SERVICEPOINT:PRICE:#id
        // TODO

        log.info("8.客户-价格");
        //zset MD:CUSTOMER:PRICE:#id
        // TODO 要不要缓存?

        log.info("9.配件");
        //配件信息 MD:MATERIAL:INFO
        //所有配件列表 MD:MATERIAL:ALL
        materialService.findAllList();

        log.info("10.客户产品图片配置");
        //CustomerCompletePicService.loadDataFromDB2Cache();

        log.info("11.加载网点财务数据");
        List<ServicePointFinance> servicePointFinanceList = servicePointFinaService.findAllFinanceList();
        Map<Long,List<ServicePointFinance>> servicePointFinanceMaps = servicePointFinanceList.stream().collect(Collectors.groupingBy(r->r.getId() % 40));
        redisTemplate.executePipelined(new RedisCallback<Object>() {    // enable Redis Pipeline
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                connection.select(RedisConstant.RedisDBType.REDIS_MD_DB.ordinal());
                StringBuffer key = new StringBuffer();
                servicePointFinanceMaps.forEach((k,v)->{
                    key.setLength(0);
                    key.append(String.format(RedisConstant.MD_SERVICEPOINT_FINANCE, k.intValue()));
                    connection.del(key.toString().getBytes(StandardCharsets.UTF_8));  //del
                    Map<byte[], byte[]> objectMap = Maps.newHashMap();
                    v.forEach(servicePointFinance->{
                        objectMap.put(servicePointFinance.getId().toString().getBytes(StandardCharsets.UTF_8), servicePointFinanceAdapter.toJson(servicePointFinance).getBytes(StandardCharsets.UTF_8));
                    });
                    connection.hMSet(key.toString().getBytes(StandardCharsets.UTF_8), objectMap);
                });
                return null;
            }
        });

        log.info("数据装载完成");
    }
}
