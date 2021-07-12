package com.wolfking.jeesite.test.common;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.wolfking.jeesite.common.config.redis.GsonRedisSerializer;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.config.redis.RedisTuple;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.wolfking.jeesite.common.config.redis.RedisConstant.MD_CUSTOMER_ALL;

//import com.wolfking.jeesite.common.utils.JedisTemplate;

/**
 * Created by yanshenglu on 2017/4/5.
 */
@RunWith(SpringJUnit4ClassRunner.class)
//@WebAppConfiguration
//@Transactional
//@SpringApplicationConfiguration(WolfkingJeesiteDriver.class)
//@RunWith(SpringRunner.class)
@SpringBootTest
public class RedisTest {


//    @Autowired
//    private JedisTemplate jedisTemplate;

//    @Autowired
//    private JedisPool jedisPool;

    @Autowired
    private JedisConnectionFactory jedisConnectionFactory;

    @Autowired
    private RedisUtils redisUtils;

    @Resource(name = "gsonRedisSerializer")
    public GsonRedisSerializer gsonRedisSerializer;

    @Test
    public void testSelectDb(){

        redisUtils.set(RedisConstant.RedisDBType.REDIS_DB14,"db:index",14,100l);
        Long dbIndex = (Long)redisUtils.get("db:index",long.class);
        System.out.println("db:index=" + dbIndex);
        //Class.forName("java.lang.Long")
    }

    /**
     * 测试String类型
     */
    @Test
    public void testString(){
        /* ok
        String key = "name";
        Integer dbIndex = 15;
        redisUtils.set(dbIndex,key,"我",-1l);
        Object value = redisUtils.get(dbIndex,key,String.class);
//        Assert.assertEquals(value,null);
        System.out.println(value.toString());
        Assert.assertTrue(value.equals("我"));


        Dict dict = new Dict();
        dict.setType("theme");
        dict.setValue("blue");
        dict.setLabel("蓝色主题");
        dict.setId(1l);
        key = "dict";
        //List存储时,转成gson格式
        redisUtils.set(dbIndex,key,dict,-1l);
        Dict d = (Dict)redisUtils.get(dbIndex,key,Dict.class);
        System.out.println(d.getLabel());
        Assert.assertTrue(d.getId().longValue() == 1l);
        //Assert.assertEquals(dict.getValue(),dictr.getValue());
        */

        /* ok */
        List<Dict> list = Lists.newArrayList();
        Dict dict1 = new Dict();
        dict1.setType("theme");
        dict1.setValue("blue");
        dict1.setLabel("蓝色主题");
        dict1.setId(1l);
        list.add(dict1);
        Dict dict2 = new Dict();
        dict2.setType("theme");
        dict2.setValue("red");
        dict2.setLabel("红色主题");
        dict2.setId(2l);
        list.add(dict2);
        redisUtils.set("dicts",list,0l);
        long time = System.currentTimeMillis();
        List<Dict> rlist = redisUtils.getList("dicts",Dict[].class);
        System.out.println("耗时"+(System.currentTimeMillis()-time)+" 毫秒");
        for (Dict d:rlist){
            System.out.println("dict:" + d.getValue());
        }


        /** getset ok
        String key = "name";
        Integer dbIndex = 15;
        redisUtils.set(key,"ryan",-1l);

        String old = (String)redisUtils.getSet(key,"lu");
        System.out.println("old:" + old);

        redisUtils.setEX(key,"ryan lu",0l);

        Assert.assertTrue(redisUtils.setNX(key,"ryan",0l));
        */


    }

    /**
     * 测试列表
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testList(){
        String key = "test:list:theme";
//        Integer dbIndex = RedisConstant.RedisDBType.REDIS_CONSTANTS_DB;
        List<Dict> list = Lists.newArrayList();
        Dict d1 = new Dict();
        d1.setId(1L);
        d1.setType("theme");
        d1.setLabel("红色");
        d1.setValue("red");
        list.add(d1);
        //redisUtils.lPush(dbIndex,key,d1);

        Dict d2 = new Dict();
        d2.setId(2L);
        d2.setType("theme");
        d2.setLabel("蓝色");
        d2.setValue("blue");
        list.add(d2);
        Dict d3 = new Dict();
        d3.setId(3L);
        d3.setType("theme");
        d3.setLabel("绿色");
        d3.setValue("green");
        list.add(d3);
        //redisUtils.lPush(dbIndex,key,d2);
        //添加多个
        redisUtils.lPushAll(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB,key,list);
        /*get
        long time = System.currentTimeMillis();
        List<Dict> values = redisUtils.lRange(dbIndex,key,0,-1,Dict.class);
        System.out.println("耗时"+(System.currentTimeMillis()-time)+" 毫秒");
        for (Dict dict:values) {
            System.out.println("dict:" + dict.getLabel());
        }*/
        //redisUtils.remove("test:list:theme");
        /*取出
        Dict d = (Dict)redisUtils.lPop(dbIndex,key,Dict.class);
        System.out.println("theme name:" + d.getLabel());
        */
        redisUtils.lTrim(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB,key,1,2);
        long time = System.currentTimeMillis();
        List<Dict> values = redisUtils.lRange(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB,key,0,-1,Dict.class);
        System.out.println("耗时"+(System.currentTimeMillis()-time)+" 毫秒");
        for (Dict dict:values) {
            System.out.println("dict:" + dict.getLabel());
        }
    }

    @Test
    public void testHash(){
//        int dbIndex = RedisConstant.RedisDBType.REDIS_CONSTANTS_DB;
        /*
        //set
        redisUtils.hmSet(1,"order:1","no","12345555",-1l);
        redisUtils.hmSet(1,"order:1","customer","客户",-1l);
        //get
        System.out.println("order:1:no=" + redisUtils.hmGet(1,"order:1","no").toString());
        System.out.println("order:1:customer=" + redisUtils.hmGet(1,"order:1","customer").toString());
        //redisUtils.remove("test:list:theme");
        String[] keys = {"no","customer"};
        Map<String,Object> orderHash = redisUtils.hGetAll(1,"order:1");
        if(orderHash != null){
            orderHash.forEach((k,v) -> System.out.println("key:"+k + " value:"+ v.toString()));
        }
        String name = (String)redisUtils.get("name",String.class);
        System.out.println("name:" + name);
        */

        Map<String,Object> values = new HashMap<>();
        values.put("no","12345");
        User user = new User();
        user.setId(1l);
        user.setName("管理员");
        values.put("user",user);
        redisUtils.hmSetAll(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB,"order:1",values,5l*60);
        String[] keys = {"no","user"};
        List<byte[]> hvalues = redisUtils.hGet(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB,"order:1",keys);
        System.out.println("size:" + hvalues.size());
        Assert.assertTrue(hvalues != null && hvalues.size() == 2);
//        Gson gson = new Gson();
        User u  = (User)redisUtils.gsonRedisSerializer.deserialize(hvalues.get(1),User.class);
        System.out.println(u.getName());
        User u2 = redisUtils.hGet(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB,"order:1","user",User.class);
        Assert.assertTrue(u2.getClass().getSimpleName().equals("User"));
        Assert.assertTrue(u2.equals(u));


    }

    @Test
    public void testHashBatch(){
        //set
        Map<String,Object> maps = Maps.newHashMap();
        maps.put("no","123456");
        maps.put("customer","customer");
        redisUtils.hmSetAll(RedisConstant.RedisDBType.REDIS_TEST_DB,"order:1",maps,-1l);

        maps.clear();
        //maps.put("no","123456");
        maps.put("customer","customer A");
        maps.put("engineer","engineer");
        redisUtils.hmSetAll(RedisConstant.RedisDBType.REDIS_TEST_DB,"order:1",maps,-1l);
        //get
        Assert.assertEquals("123456",redisUtils.hGet(RedisConstant.RedisDBType.REDIS_TEST_DB,"order:1","no",String.class));
        System.out.println("order:1:customer=" + redisUtils.hGet(RedisConstant.RedisDBType.REDIS_TEST_DB,"order:1","customer A",String.class));
        //redisUtils.remove("test:list:theme");

    }

    @Test
    public void testSet(){
        User user = new User();
        user.setId(1l);
        user.setName("管理员");
        Long cnt = redisUtils.sAdd(RedisConstant.RedisDBType.REDIS_TEST_DB,"users",user,69*10);
        System.out.println("user 1 cnt:" + cnt.toString());

        User user2 = new User();
        user2.setId(2l);
        user2.setName("测试帐号");
        cnt = redisUtils.sAdd(RedisConstant.RedisDBType.REDIS_TEST_DB,"users",user2,69*10);
        System.out.println("user 2 cnt:" + cnt.toString());

        Set<User> sets = redisUtils.sMembers(RedisConstant.RedisDBType.REDIS_TEST_DB,"users",User.class);
        for (User u: sets) {
            System.out.println("set value:" + u.getId() + u.getName());
        }

    }

    @Test
    public void testSetBatch(){
        //set
//        List<String> orders = Lists.newArrayList();
//        orders.add("order1");
//        orders.add("order2");
//        redisUtils.add("customer:1",orders);
//        //get
//        Set sets = redisUtils.setMembers("customer:1");
//        for(Object o:sets){
//            System.out.println(o.toString());
//        }

    }

    @Test
    public void testIncr(){
//        long id = redisUtils.incr(1,"orderid");
//        System.out.println("id:"+id);
    }

    @Test
    public void testLoadDict(){
        MSDictUtils.CacheAllDict();
    }

    @Test
    public void testCommand(){
        //remove removePattern

        redisUtils.removePattern("area:*");

        /* keys
        Set<byte[]> keys = redisUtils.keys(15,"name:*");
        Assert.assertTrue(keys != null);
        System.out.println("keys cnt:" + keys.size());
//        keys.stream().forEach(t->System.out.println("key:" + StringUtils.toString(t)));
        keys.stream().forEach(t->System.out.println("key:" + new String(t)));
        */
    }

    @Test
    public void testAreas(){
        /*
        UserUtils.loadAreas();
         */
        System.out.println("start:");
        long time = System.currentTimeMillis();
        List<Area> areas = UserUtils.getAreaList();
        System.out.println("耗时"+(System.currentTimeMillis()-time));
        Assert.assertTrue(areas != null && areas.size()>0);

    }

    @Test
    public void testZSet(){
        /*
        Set<String> sets = redisUtils.zRange("zset1",0,-1,String.class);
        if(sets != null){
            for (String s:sets) {
                System.out.println("set value:" + s);
            }
        }*/
        /*
        String key = "zuser";
        User user = new User();
        user.setId(1l);
        user.setName("管理员");
        redisUtils.zAdd(key,user,1,0);

        User user3 = new User();
        user3.setId(3l);
        user3.setName("测试帐号3");
        redisUtils.zAdd(key,user3,3,0);

        User user2 = new User();
        user2.setId(2l);
        user2.setName("测试帐号2");
        redisUtils.zAdd(key,user2,2,0);

//        Set<User> sets = redisUtils.zRange(key,0,-1,User.class);
//        for (User u: sets) {
//            System.out.println("set value:" + u.getId() +" - " + u.getName());
//        }

        Set<User> sets1 = redisUtils.zRevRange(key,0,-1,User.class);
        for (User u: sets1) {
            System.out.println("set value:" + u.getId() +" - " + u.getName());
        }*/


        String key = "zuser";
        redisUtils.remove(key);
        redisUtils.zAdd(key,"user1",1,0);
        redisUtils.zAdd(key,"user3",3,0);
        redisUtils.zAdd(key,"user2",2,0);

//        Set<User> sets = redisUtils.zRange(key,0,-1,User.class);
//        for (User u: sets) {
//            System.out.println("set value:" + u.getId() +" - " + u.getName());
//        }
        /*小->大 ok
        System.out.println("小->大" );
        Set<String> sets = redisUtils.zRange(key,0,-1,String.class);
        for (String s: sets) {
            System.out.println("set value:" + s);
        }*/

        /*->大->小 ng
        System.out.println("大->小" );
        Set<String> sets1 = redisUtils.zRevRange(key,0,-1,String.class);
        for (String s: sets1) {
            System.out.println("set value:" + s);
        }*/

        System.out.println("大->小" );
        List<String> sets1 = redisUtils.zRevRange(key,0,-1,String.class);
        for (String s: sets1) {
            System.out.println("set value:" + s);
        }

        /* ok
        Set<RedisZSetCommands.Tuple> sets1 = redisUtils.zRevRangeWithScore(0,key,0,-1,String.class);

        for (RedisZSetCommands.Tuple s: sets1) {
            System.out.println("set value:" + redisUtils.gsonRedisSerializer.deserialize(s.getValue(),String.class) +" scor:"+s.getScore());
        }*/

    }

    @Test
    public void testZSetBatchAdd(){
        String key = "zset";
        Set<RedisZSetCommands.Tuple> sets = Sets.newHashSet();
        User user = new User();
        user.setId(1l);
        user.setName("管理员");
        RedisTuple tuple = new RedisTuple(gsonRedisSerializer.serialize(user),1d);
        sets.add(tuple);

        user = new User();
        user.setId(2l);
        user.setName("Ryan");
        tuple = new RedisTuple(gsonRedisSerializer.serialize(user),2d);
        sets.add(tuple);
        //set
        redisUtils.zAdd(RedisConstant.RedisDBType.REDIS_SD_DB,key,sets,0l);

        //read
        Set<RedisZSetCommands.Tuple> rtnSets = redisUtils.zRangeWithScore(RedisConstant.RedisDBType.REDIS_SD_DB,key,0,-1);
        for (RedisZSetCommands.Tuple tuple1 : rtnSets){
            User u = (User)gsonRedisSerializer.deserialize(tuple1.getValue(),User.class);
            System.out.println(String.format("score:%s name:%s",tuple1.getScore(),u!=null?u.getName():""));
        }
    }
    @Test
    /**
     * 测试zSetEX
     */
    public void testzSetEX(){
        String key =new String("service:engineer:1");
        redisUtils.zSetEX(key,"1",1,0l);
        redisUtils.zSetEX(key,"2",2,0l);
        redisUtils.zSetEX(key,"3",1,0l);
    }

    @Test
    /**
     * 测试按key及score返回第一个值
     */
    public void testZRangeOneByScore(){
        Customer customer = (Customer) redisUtils.zRangeOneByScore(RedisConstant.RedisDBType.REDIS_MD_DB, MD_CUSTOMER_ALL,100,100, Customer.class);
        Assert.assertNotNull(customer);
        Assert.assertTrue(Objects.equals(customer.getId(),100l));
        System.out.println(customer.toString());
    }

    @Test
    public void redisLock(){
        String lockKey = "test";
        if(redisUtils.exists(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockKey)){
            System.out.println("redisUtils.exists test");
        }

        //加锁
        try {
            Boolean accepted = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockKey, 1, 300);//30秒
            if (!accepted) {
                System.out.println("redisUtils.setNX failed");
            }
            else {
                System.out.println("============ success ===============");
            }
            //throw new RuntimeException("exception");//测试抛出异常
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("redisUtils.setNX catched failed");
        }finally {
            redisUtils.expire(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockKey, 20);
        }
    }

    @Test
    public void testRedisLock(){
        int threadCount = 3;
        final CyclicBarrier barrier = new CyclicBarrier(threadCount);
        //定长线程池，可控制线程最大并发数，超出的线程会在队列中等待
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        final String lockKey = "test:lock";
        //运行开始时间
        long startTime = System.currentTimeMillis();
        for (int i = 0; i <9; i++) {
            final int index = i;
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        barrier.await(); /**等待所有线程到位*/
//                        System.out.println("index:" + index);
                        Boolean accepted = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockKey, 1, 100);//100秒
                        if (!accepted) {
                            System.out.println("thread-" + index + ":未获得锁");
                        }
                        else {
                            System.out.println("thread-" + index + ":获得锁");
                            redisUtils.expire(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockKey,1);//1秒后过期
                            Thread.sleep(1000);
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

    @Test
    public void testRedisConnection(){
        Long start = System.currentTimeMillis();
        for(int i=0;i<100;i++){
//            jedisPool.getResource().get("name"); fail
//            jedisConnectionFactory.getConnection().get("name".getBytes());
            redisUtils.get(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB,"name",String.class);
        }
        System.out.println("用时:".concat(String.format("%s",System.currentTimeMillis()-start)).concat(" 毫秒"));
    }

    @Test
    public void testBatchLock(){
        List<String> keys = Lists.newArrayList();
        for(int i=0;i<10;i++){
            keys.add("1" + StringUtils.leftPad(String.valueOf(i),9,"0"));
        }
        Boolean locked = redisUtils.batchSetNX(keys,1,10*60);
        if(locked){
            System.out.println("locked");
        }else{
            System.out.println("lock fail");
        }
    }

    @Test
    public void moveDB(){
        StringBuilder key = new StringBuilder(100);
        for(int i=1;i<3000;i++){
            key.setLength(0);
            key.append("repeate:order:check:").append(i);
            redisUtils.moveToDb(RedisConstant.RedisDBType.REDIS_APP_DB,key.toString(),RedisConstant.RedisDBType.REDIS_TEMP_DB);
        }
    }

    @Test
    public void testScan(){
       List<String> keys = Lists.newArrayList();
       keys = redisUtils.scanList(RedisConstant.RedisDBType.REDIS_SYS_AREA,"5:*:七星镇",10000);
       String value = StringUtils.EMPTY;
       for(String key:keys){
           value =(String)redisUtils.getString(RedisConstant.RedisDBType.REDIS_SYS_AREA,key,String.class);
           System.out.printf("key:%s value:%s\n",key,value);
       }
    }
}
