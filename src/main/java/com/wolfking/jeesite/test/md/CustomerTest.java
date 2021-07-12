package com.wolfking.jeesite.test.md;

import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.CustomerAccountProfile;
import com.wolfking.jeesite.modules.md.entity.CustomerPrice;
import com.wolfking.jeesite.modules.md.service.CustomerService;
import com.wolfking.jeesite.modules.md.utils.CustomerAdapter;
import com.wolfking.jeesite.modules.md.utils.CustomerUtils;
import com.wolfking.jeesite.modules.sys.service.SystemService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by ryan
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@Slf4j
public class CustomerTest {

    @Autowired
    private CustomerService customerService;
    @Autowired
    private SystemService systemService;

    @Autowired
    private RedisUtils redisUtils;

    @Test
    public void getFromRedis(){
        Long cid = 1103L;
        Customer customer = customerService.getFromCache(cid);
        log.info(CustomerAdapter.getInstance().toJson(customer));
        if(customer == null){
            log.error("customer is null");
        }else{
            log.info("id:{} name:{} balance:{} contract date:{} timeLinessFlag:{} urgentFlag:{} invoiceFlag:{}",customer.getId(),customer.getName(),customer.getFinance().getBalance(),customer.getContractDate(),customer.getTimeLinessFlag(),customer.getUrgentFlag(),customer.getFinance().getInvoiceFlag());
        }
        Assert.assertTrue("timeLiness flag",customer.getTimeLinessFlag() == 0);
        customer = customerService.get(cid);
        String json = CustomerAdapter.getInstance().toJson(customer);
        log.info("json:{}",json);

        /*
        Customer customer = (Customer) redisUtils.zRangeOneByScore(RedisConstant.RedisDBType.REDIS_MD_DB,RedisConstant.MD_CUSTOMER_ALL,1,1,Customer.class);
        if(customer == null){
            System.out.println("null");
        }else{
            System.out.println(String.format("id:%s name:%s balance:%s",customer.getId(),customer.getName(),customer.getFinance().getBalance()));
        }*/
    }

    @Test
    public void getServicePrice(){
        List<CustomerPrice> prices =customerService.getPricesFromCache(1282l);
        if(prices == null || prices.size()==0){
            System.out.println("no data");
//            Assert.error("无数据返回");
        }
        System.out.println("size:" + prices.size());
    }

    @Test
    public void resetServicePrice(){
        //Boolean result = customerService.resetCustomerPricesCache(6l);
        //Asserts.check(result,"OK");
    }

    @Test
    public void getAllCustomers(){
        List<Customer> customers = CustomerUtils.getCustomerList();
        Customer c;
        for(int i=0;i<110;i++){
            c = customers.get(i);
            System.out.println(String.format("Id:%s Name:%s",c.getId(),c.getName()));
        }
    }

    @Test
    public void getPricesFromCache(){
        long startTime = System.currentTimeMillis();
        //并发线程数
        int threadNum = 10;
        // 模拟客户端数量
        int clientNum = 10;
        ExecutorService pool = Executors.newCachedThreadPool();
        // 可以同时访问的线程
        final Semaphore semp = new Semaphore(threadNum);
        for (int index = 0; index < clientNum; index++) {
            final int NO = index;
            Runnable run = new Runnable() {
                public void run() {
                    try {
                        // 获取许可
                        semp.acquire();
                        List<CustomerPrice> prices = customerService.getPricesFromCache(1104l);
                        if(prices == null || prices.isEmpty()){
                            System.out.println("no data");
                        }else {
                            System.out.println("size:" + prices.size());
                        }
                        // Thread.sleep((long) (Math.random()) * 1000);
                        // 释放
                        semp.release();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            };
            pool.execute(run);
        }
        // 退出线程池
        pool.shutdown();
        while (!pool.isTerminated()) {
        }
        System.out.println("运行时间为：【"+ TimeUnit.MILLISECONDS.toSeconds((System.currentTimeMillis() - startTime))+"】秒");
        /*
        List<CustomerPrice> prices = customerService.getPricesFromCache(1104l);
        CustomerPrice c;
        Product p;
        for(int i=0,len=prices.size();i<len;i++){
            c = prices.get(i);
            p = c.getProduct();
            System.out.println(String.format("procut id:%s Name:%s sort:%s",p.getId(),p.getName(),p.getSort()));
        }
        */
    }

    @Test
    public void getCustomerUsers(){
        long customerId = 1058;
        List<String> ids = customerService.getAccountList(customerId);
        for (String id: ids) {
            System.out.println(id);
        }
    }

//    @Test
//    public void getAllCustomerUsers(){
//        List<User> users = customerService.getAllAccountList();
//        Map<Long,List<User>> cmaps = users.stream().collect(Collectors.groupingBy(User::getCompanyId));
//        cmaps.forEach((k,v)->{
//            System.out.println("customer id:" + k.toString());
//            v.forEach(item->{
//                System.out.println("    user id:" + item.getId().toString());
//            });
//        });
//    }

    @Test
    public void testZaddCustomer(){
        Customer customer = customerService.get(1381);
        redisUtils.zAdd(RedisConstant.RedisDBType.REDIS_MD_DB,RedisConstant.MD_CUSTOMER_ALL, customer, customer.getId(), 0);
    }

    @Test
    public void testCustomerProfile(){
        CustomerAccountProfile m = customerService.getCustomerAccountProfile(2722l);
        if(m != null) {
            System.out.println(redisUtils.gsonRedisSerializer.toJson(m));
        }

        CustomerAccountProfile m1 = customerService.getCustomerAccountProfileByUserId(50897l);
        if(m1 != null){
            System.out.println(redisUtils.gsonRedisSerializer.toJson(m1));
        }
    }

    @Test
    public void testUpdateCustomerCache(){
        Customer customer = customerService.getFromCache(1l);
        customer.setCode("01702");//C01702

        //customerService.updateCustomerCache(customer);
    }

    // region gson序列化

    @Test
    public void testMultiThreadGetCustomerFromRedis(){
        final int threadNum = 50;
        CyclicBarrier cb = new CyclicBarrier(threadNum);

        int idx;
        Long cid;
        ExecutorService es = Executors.newFixedThreadPool(threadNum);
        for (int i = 0; i < threadNum; i++) {
            cid = java.util.concurrent.ThreadLocalRandom.current().nextLong(1700,1870);
            es.execute(new GetCustomerFromRedisThread(cb,customerService,cid));
        }
        es.shutdown();
        try {
            Thread.sleep(2000);
        }catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("ok");
    }

    //读取上门服务
    static class GetCustomerFromRedisThread implements Runnable {
        private CyclicBarrier cb;
        private CustomerService customerService;
        private Long cid;

        public GetCustomerFromRedisThread(CyclicBarrier cb,CustomerService customerService,Long cid) {
            this.cb = cb;
            this.customerService = customerService;
            this.cid = cid;
        }

        @Override
        public void run() {
            // TODO Auto-generated method stub
            try {
                // 等待所有任务准备就绪
                cb.await();
                // 定义每个线程负责的业务逻辑实现
                customerService.getFromCache(cid);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //endregion

    @Test
    public void testRedis() {
        //mark on 2020-2-11
//        Map<Long, Customer> customerMap = customerService.getCustomerMap(Lists.newArrayList(10L, 20L, 100L));
//        Map map = customerMap;
    }

    /**
     * 测试缓存穿透
     * 即缓存失效时，同时有多个请求，都读取数据库，然后同步缓存情况
     */
    @Test
    public void testGetCustomerList(){
        /* 多线程，并发测试*/
        //运行开始时间
        //并发线程数
        int threadCount = 10;
        long startTime = System.currentTimeMillis();
        final CyclicBarrier barrier = new CyclicBarrier(threadCount);
        //不限制大小的线程池
        ExecutorService pool = Executors.newCachedThreadPool();
        for (int index = 0; index <threadCount; index++) {
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        barrier.await();
                        List<Customer> customers = CustomerUtils.getCustomerList();
                        if(customers == null || customers.isEmpty()){
                            System.out.println("no data");
                        }else {
                            //System.out.println("size:" + customers.size());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        pool.shutdown();
        while (!pool.isTerminated()) {
        }
        System.out.println("运行时间为：【"+ TimeUnit.MILLISECONDS.toSeconds((System.currentTimeMillis() - startTime))+"】秒");

    }

    /**
     * 测试缓存穿透
     * 即缓存失效时，同时有多个请求，都读取数据库，然后同步缓存情况
     */
    @Test
    public void testGetCustomerList2(){
        long startTime = System.currentTimeMillis();
        //并发线程数
        int threadNum = 10;
        // 模拟客户端数量
        int clientNum = 10;
        ExecutorService pool = Executors.newCachedThreadPool();
        // 可以同时访问的线程
        final Semaphore semp = new Semaphore(threadNum);
        for (int index = 0; index < clientNum; index++) {
            final int NO = index;
            Runnable run = new Runnable() {
                public void run() {
                    try {
                        // 获取许可
                        semp.acquire();
                        List<Customer> customers = CustomerUtils.getCustomerList();
                        if(customers == null || customers.isEmpty()){
                            System.out.println("no data");
                        }else {
                            //System.out.println("size:" + customers.size());
                        }
                        // Thread.sleep((long) (Math.random()) * 1000);
                        // 释放
                        semp.release();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            };
            pool.execute(run);
        }
        // 退出线程池
        pool.shutdown();
        while (!pool.isTerminated()) {
        }
        System.out.println("运行时间为：【"+ TimeUnit.MILLISECONDS.toSeconds((System.currentTimeMillis() - startTime))+"】秒");
    }

    @Test
    public void testGetCustomerMap(){
        Map<Long,Customer> maps = CustomerUtils.getAllCustomerMap();
        if(maps == null || maps.isEmpty()){
            System.out.println("no data");
        }
        for(Map.Entry<Long,Customer> entry:maps.entrySet()){
            System.out.println(String.format("%s:%s",entry.getKey(),entry.getValue().getName()));
        }
    }
}
