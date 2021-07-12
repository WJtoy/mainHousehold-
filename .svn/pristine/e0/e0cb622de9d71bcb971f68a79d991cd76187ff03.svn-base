package com.wolfking.jeesite.test.common;

import com.wolfking.jeesite.common.utils.RedisUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.modules.sys.utils.SeqUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

//import com.wolfking.jeesite.common.utils.JedisTemplate;

/**
 * Created by yanshenglu
 * 测试序列号
 */
//@RunWith(SpringRunner.class)
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class SeqNoTest {
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private OrderService orderService;

    @Test
    public void testFormat(){
        /*
        String seqNo = String.format("%s-%s-%s","O", MoreObjects.firstNonNull(null,""),"1991");
        System.out.println(seqNo);
        */
        String seqNo = SeqUtils.NextSequenceNo("OrderNO");
        System.out.println("1."+seqNo);
    }


    @Test
    public void reputSeqNo(){
        String code = "OrderNO";
        Date date = new Date();
        SeqUtils.reputSequenceNo(code, date, "O2017070700240");
        SeqUtils.reputSequenceNo(code, date, "O2017070700243");
        SeqUtils.reputSequenceNo(code, date, "O2017070700242");
        SeqUtils.reputSequenceNo(code, date, "O2017070700244");
    }

    @Test
    public void testCreteSeqNo(){
        //运行开始时间
        long startTime = System.currentTimeMillis();
        //这个类主要是，使多个线程同时进行工作,如果不了解建议网上搜索相关的文章进行学习
        int threadCount = 100;
        final CyclicBarrier barrier = new CyclicBarrier(threadCount);
        //不限制大小的线程池
        ExecutorService pool = Executors.newCachedThreadPool();
        final String code = "OrderNO";
        for (int index = 0; index <threadCount; index++) {
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        barrier.await();
                        String seqNo = SeqUtils.NextSequenceNo(code);
                        System.out.println(seqNo);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
//        pool.execute(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    barrier.await();
//                    String seqNo = SeqUtils.NextSequenceNo(code);
//                    System.out.println("2."+seqNo);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//        pool.execute(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    barrier.await();
//                    String seqNo = SeqUtils.NextSequenceNo(code);
//                    System.out.println("3."+seqNo);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });
        pool.shutdown();
        while (!pool.isTerminated()) {
        }
        System.out.println("运行时间为：【"+ TimeUnit.MILLISECONDS.toSeconds((System.currentTimeMillis() - startTime))+"】秒");
    }

    /**
     * 测试产生新的订单号
     */
    @Test
    @Retryable(value= {RuntimeException.class},maxAttempts = 3,backoff = @Backoff(delay = 5000l,multiplier = 1))
    public void testCreteOrderNo(){
        //运行开始时间
        long startTime = System.currentTimeMillis();

        int threadCount = 10;
        final CyclicBarrier barrier = new CyclicBarrier(threadCount);
        //不限制大小的线程池
        ExecutorService pool = Executors.newCachedThreadPool();
        for (int index = 0; index <threadCount; index++) {
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        barrier.await();

                        String seqNo = SeqUtils.NextOrderNo();
                        //String seqNo = orderService.getOrderNo();
                        System.out.println(seqNo);
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
     * 测试多线程同时取号
     */
    @Test
    public void testMultGetNo(){
        //运行开始时间
        long startTime = System.currentTimeMillis();
        //最大线程数
        int threadCount = 1000;
        Random random = new Random();
        Date date = new Date();
        final CyclicBarrier barrier = new CyclicBarrier(threadCount);
        //不限制大小的线程池
        ExecutorService pool = Executors.newCachedThreadPool();
        for (int index = 0; index <threadCount; index++) {
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        barrier.await();
                        String seqNo = orderService.getNewOrderNo();
                        System.out.println("pop: " + seqNo);
                        //int rnd = random.nextInt(10);
                        //if(rnd %6 == 0 &&StringUtils.isNotBlank(seqNo)){
                        //    System.out.println("push: " + seqNo);
                        //    SeqUtils.reputOrderNo(date,seqNo);
                        //}
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
    public void testRetry(){
        //运行开始时间
        long startTime = System.currentTimeMillis();

        int threadCount = 100;
        final CyclicBarrier barrier = new CyclicBarrier(threadCount);
        //不限制大小的线程池
        ExecutorService pool = Executors.newCachedThreadPool();
        for (int index = 0; index <threadCount; index++) {
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        barrier.await();

                        String seqNo = orderService.getNewOrderNo();
                        if(StringUtils.isBlank(seqNo)){
                            System.out.println("return null");
                        }else {
                            System.out.println(seqNo);
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
    public void testRetryNewMethod(){
       try {
           String seqNo = SeqUtils.NextSequenceNo("MaterialFormNo",0,3);
           System.out.println("1." + seqNo);
       }catch (Exception e){
           e.printStackTrace();
       }
    }
}
