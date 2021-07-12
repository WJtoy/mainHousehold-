package com.wolfking.jeesite.test.common;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.kkl.kklplus.utils.SequenceIdUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.utils.BitUtils;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.wolfking.jeesite.common.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.LongToDoubleFunction;

/**
 * 测试随机ID产生
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@Slf4j
public class SequenceIdTest {

    @Autowired
    private RedisUtils redisUtils;

    @Test
    public void createSequenceId(){
        SequenceIdUtils sequence = new SequenceIdUtils(0,0);
        Long id;
        Map<Long, Long> ids = new HashMap<Long, Long>();
        for(int i=0;i<10000;i++){
            id = sequence.nextId();
            if(ids.containsKey(id)){
                System.out.println(">>> id:" + id.toString() + " exists");
            }else{
                ids.put(id,id);
                System.out.println("id:" + id.toString());
            }
        }
    }

    /**
     * 重复性测试
     */
    @Test
    public void testRepeated() {
        Set<Long> set = Sets.newHashSet();
        int maxTimes = 10000;
        //SequenceIdUtils sequence = new SequenceIdUtils(0,0);
        //SequenceIdUtils sequence = new SequenceIdUtils();
        for(int j=0;j<10;j++) {
            int workerId = ThreadLocalRandom.current().nextInt(32);
            int datacenterId = ThreadLocalRandom.current().nextInt(32);
            SequenceIdUtils sequence = new SequenceIdUtils(workerId,datacenterId);
            for (int i = 0; i < maxTimes; i++) {
                set.add(sequence.nextId());
            }
            try {
                Thread.sleep(5000l);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("size:" + set.size());
        Assert.assertEquals(maxTimes*10, set.size());
        /*
        SequenceIdUtils sequence1 = new SequenceIdUtils(1,0);
        for (int i = 0; i < maxTimes; i++) {
            set.add(sequence1.nextId());
        }
        System.out.println("size:" + set.size());
        Assert.assertEquals(maxTimes*2, set.size());
        */
    }

    /**
     * 测试多线程产生id重复测试
     * 使用redis set保存，按实际加到redis的数量和成功的数量比较，不同有重复
     */
    @Test
    public void testMultRepeated() {
        //运行开始时间
        long startTime = System.currentTimeMillis();

        int threadCount = 5;
        int times = 10000;
        final CyclicBarrier barrier = new CyclicBarrier(threadCount);
        //不限制大小的线程池
        ExecutorService pool = Executors.newCachedThreadPool();
        SequenceIdUtils sequenceIdUtils = new SequenceIdUtils(0,0);
        for (int index = 0; index <threadCount; index++) {
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    int count = 0;
                    try {
                        barrier.await();
                        long id;
                        for(int i=0;i<times;i++) {
                            try {
                                id = sequenceIdUtils.nextId();
                                redisUtils.sAdd("ID", id, 0l);
                                count++;
                            }catch (Exception e){

                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        System.out.println("count:" + count);
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
     * 测试ThreadLocalRandom随机数
     */
    @Test
    public void testThreadLocalRandom(){
        Long id = 1011992847130301600l;
        double score = BigDecimal.valueOf(id).doubleValue();
        log.info("id:{} ,score:{}",id,String.valueOf(score));
        /*
        int workerId;
        for(int i=0;i<100;i++){
            workerId = ThreadLocalRandom.current().nextInt(32);
            log.info("{}",workerId);
        }
        */
    }

    /**
     * 测试随机5位数字，取数字后5位，放入set中以去重
     */
    @Test
    public void testRandomNumber(){
        Set<String> set = new HashSet<String>();
        int numLen = 5;
        int maxTimes = 10000 * 2;
        SequenceIdUtils sequence = new SequenceIdUtils(0,0);
        long id;
        for (int i = 0; i < maxTimes; i++) {
            id = sequence.nextId();
            set.add(StringUtils.right(String.valueOf(id),5));
        }
        System.out.println("size:" + set.size());
        set.stream().forEach(t->System.out.println(t.toString()));
        Assert.assertEquals(maxTimes, set.size());
    }

    @Test
    public void testQuarter(){
        /*
        Date date = DateUtils.getDate(2018,12,1);
        int month = date.getMonth();
        log.info("month:{}",month);
        int startQuarter,endQuarter;
        // 0 ~ 2
        int startMonth = 1;
        int endMonth = 2;
        startQuarter = startMonth /3 + 1;
        endQuarter = endMonth / 3 + 1;
        log.info("start:{} end:{}",startQuarter,endQuarter);
        Assert.assertTrue("fail",startQuarter == endQuarter);
        // 3~5
        startMonth = 4;
        endMonth = 5;
        startQuarter = startMonth /3 + 1;
        endQuarter = endMonth / 3 + 1;
        log.info("start:{} end:{}",startQuarter,endQuarter);
        Assert.assertTrue("fail",startQuarter == endQuarter);
        // 6~8
        startMonth = 7;
        endMonth = 8;
        startQuarter = startMonth /3 + 1;
        endQuarter = endMonth / 3 + 1;
        log.info("start:{} end:{}",startQuarter,endQuarter);
        Assert.assertTrue("fail",startQuarter == endQuarter);
        //9~11
        startMonth = 10;
        endMonth = 11;
        startQuarter = startMonth /3 + 1;
        endQuarter = endMonth / 3 + 1;
        log.info("start:{} end:{}",startQuarter,endQuarter);
        Assert.assertTrue("fail",startQuarter == endQuarter);
        */


        Date startDate = DateUtils.parseDate("2017-12-13 10:10:10");
        Date endDate = null;
        List<String> quarters = QuarterUtils.getQuarters(startDate,endDate);
        for(int i=0,size=quarters.size();i<size;i++){
            log.info(quarters.get(i));
        }

    }

    public static void main(String[] args) {

        List<String> positions = BitUtils.getPositions(67584, String.class);
        String result = Joiner.on(",").skipNulls().join(positions);
        System.out.println(result);

        positions = BitUtils.getPositions(327680, String.class);
        result = Joiner.on(",").skipNulls().join(positions);
        System.out.println(result);
        /*
        List<Integer> positions = Lists.newArrayList(1,2,2,3);
        int i = BitUtils.markedAndToTags(positions);
        System.out.println("toal:" + i);// 18
        */
    }
}
