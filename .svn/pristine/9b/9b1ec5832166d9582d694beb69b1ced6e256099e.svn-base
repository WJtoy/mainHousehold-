package com.wolfking.jeesite.test.async;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.utils.CustomerUtils;
import com.wolfking.jeesite.modules.sd.service.OrderTaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.math.LongRange;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * CompletableFuture异步测试
 *
 * @autor Ryan Lu
 * @date 2018/10/22 1:13 PM
 */
@SpringBootTest
@Slf4j
//@RunWith(SpringJUnit4ClassRunner.class)
public class CompletableFutureTest {

    @Autowired
    private OrderTaskService orderTaskService;
    //https://www.jianshu.com/p/4897ccdcb278

    // 正常情况
    @Test
    public void test1() throws Exception {
        CompletableFuture<String> completableFuture = new CompletableFuture();
        new Thread(new Runnable() {
            @Override
            public void run() {
                //模拟执行耗时任务
                System.out.println("task doing...");
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //告诉completableFuture任务已经完成
                completableFuture.complete("result");
            }
        }).start();
        //获取任务结果，如果没有完成会一直阻塞等待
        String result = completableFuture.get();
        System.out.println("计算结果:" + result);
    }

    //异常
    @Test
    public void test2() throws Exception {
        CompletableFuture<String> completableFuture = new CompletableFuture();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //模拟执行耗时任务
                    System.out.println("task doing...");
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    throw new RuntimeException("抛异常了");
                } catch (Exception e) {
                    //告诉completableFuture任务发生异常了
                    completableFuture.completeExceptionally(e);
                }
            }
        }).start();
        //获取任务结果，如果没有完成会一直阻塞等待
        String result = completableFuture.get();
        System.out.println("计算结果:" + result);
    }

    //region 工厂方法

    @Test
    public void test3() throws Exception {
        //supplyAsync内部使用ForkJoinPool线程池执行任务
        CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(() -> {
            //模拟执行耗时任务
            System.out.println("task doing...");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //throw new RuntimeException("异常了");
            //返回结果
            return "result";
        });
        System.out.println("计算结果:" + completableFuture.get());
    }

    @Test
    public void test4() throws Exception {
        CompletableFuture<String> completableFuture1 = CompletableFuture.supplyAsync(() -> {
            //模拟执行耗时任务
            System.out.println("task1 doing...");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //返回结果
            return "result1";
        });

        CompletableFuture<String> completableFuture2 = CompletableFuture.supplyAsync(() -> {
            //模拟执行耗时任务
            System.out.println("task2 doing...");
            try {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                double val = 1 / 0;
                return "result2:ok";
            } catch (RuntimeException e) {
                e.printStackTrace();
                return "result2:fail";
            }
            //返回结果

        });

        /*
        CompletableFuture<Object> anyResult=CompletableFuture.anyOf(completableFuture1,completableFuture2);

        System.out.println("第一个完成的任务结果:"+anyResult.get());

        CompletableFuture<Void> allResult=CompletableFuture.allOf(completableFuture1,completableFuture2);
        //阻塞等待所有任务执行完成
        allResult.join();
        System.out.println("所有任务执行完成");
        */
        CompletableFuture<String> allResult = CompletableFuture.allOf(completableFuture1, completableFuture2)
                .thenApply((Void) -> {
                    try {
                        return completableFuture1.get() + "," + completableFuture2.get();
                    } catch (Exception e) {
                        return "error";
                    }
                }).exceptionally(e -> {
                    e.printStackTrace();
                    return "error2";
                });
        System.out.println(allResult.get());
    }

    //endregion

    @Test
    public void test5() {
        /*
        List<Customer> customers = CustomerUtils.getCustomerList();
        long minId = customers.stream().map(t->t.getId()).reduce(Long::min).get();
        long maxId = customers.stream().map(t->t.getId()).reduce(Long::max).get();
        long l = maxId/3;
        Map<Integer, LongRange> customerIdMap = Maps.newHashMap();
        customerIdMap.put(0, new LongRange(minId,l));
        customerIdMap.put(1, new LongRange(l+1,l*2));
        customerIdMap.put(2, new LongRange(l*2+1,maxId));
        log.warn("index:{}, min: {} ,max: {}",0,minId,l);
        log.warn("index:{}, min: {} ,max: {}",1,l+1,l*2);
        log.warn("index:{}, min: {} ,max: {}",2,l*2+1,maxId);
        */
        Date endDate = DateUtils.getEndOfDay(new Date());
        Date beginDate1 = DateUtils.addDays(endDate,-30);
        Date beginDate = DateUtils.getDateStart(beginDate1);
        orderTaskService.reloadCheckRepeatOrderCache(beginDate,endDate);
    }

    //region https://www.imooc.com/article/21655

    @Test
    public void testThenApply() {
        //thenApply的功能相当于将CompletableFuture<T>转换成CompletableFuture<U>

        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> "Hello")
                .thenApply(s -> s + " World").thenApply(String::toUpperCase);

        try {
            System.out.println(future.get());// HELLO WORLD
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        CompletableFuture<Double> future1 = CompletableFuture.supplyAsync(() -> "10")
                .thenApply(Integer::parseInt)//转换为整数
                .thenApply(i -> i * 10.0);//乘以10

        try {
            System.out.println(future1.get());// 100.0
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testThenCompose() {
        //thenCompose:组合多个CompletableFuture，将前一个结果作为下一个计算的参数，它们之间存在着先后顺序

        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> "Hello")
                .thenCompose(s -> CompletableFuture.supplyAsync(() -> s + " World"));

        try {
            System.out.println(future.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testThenCombine() {
        //现在有CompletableFuture<T>、CompletableFuture<U>和一个函数(T,U)->V，
        // thenCompose就是将CompletableFuture<T>和CompletableFuture<U>变为CompletableFuture<V>

        //使用thenCombine()之后future1、future2之间是并行执行的，最后再将结果汇总

        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> "100");
        CompletableFuture<Integer> future2 = CompletableFuture.supplyAsync(() -> 100);

        CompletableFuture<Double> future = future1.thenCombine(future2, (s, i) -> Double.parseDouble(s + i));

        try {
            System.out.println(future.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testThenAcceptBoth() {
        //thenAcceptBoth跟thenCombine类似，但是返回CompletableFuture<Void>类型。

        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> "100");
        CompletableFuture<Integer> future2 = CompletableFuture.supplyAsync(() -> {
            return 100;
            //throw new RuntimeException("future2 error");
        });

        CompletableFuture<Void> future = future1.thenAcceptBoth(future2, (s, i) -> System.out.println(Double.parseDouble(s + i)));

        try {
            future.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testWhenComplete() {
        //当CompletableFuture完成计算结果后，对结果进行一些处理
        //不返回值，相当于void()

        CompletableFuture.supplyAsync(() -> "Hello")
                .thenApply(s -> s + " World")
                .thenApply(s -> s + "\nThis is CompletableFuture demo")
                .thenApply(String::toLowerCase)
                .whenComplete((result, throwable) -> System.out.println(result));

    }


    @Test
    public void testHandel() {
        //与CompletableFuture类似
        //但返回值
        CompletableFuture<Double> future = CompletableFuture.supplyAsync(() -> "100")
                .thenApply(s -> s + "100")
                .handle((s, t) -> s != null ? Double.parseDouble(s) : 0);

        try {
            System.out.println(future.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testException() {
        CompletableFuture.supplyAsync(() -> "hello world")
                .thenApply(s -> {
                    s = null;
                    int length = s.length();
                    return length;
                }).thenAccept(i -> System.out.println(i))
                .exceptionally(t -> {
                    System.out.println("Unexpected error:" + t);
                    return null;
                });
    }
    //endregion

    //region https://www.jianshu.com/p/e776e17a544b

    @Test
    public void testNoExceptionally() {
        CompletableFuture<String> future = new CompletableFuture();
        // task1
        future.completeExceptionally(new Exception("测试抛异常"));
        future.whenComplete((s, t) -> {
            // 1
            log.info("1:{}", s);
            if (t != null)
                log.error(t.getMessage());
        })
                .whenComplete((s, t) -> {
                    // 2
                    log.info("2:{}", s);
                    if (t != null)
                        log.error(t.getMessage());
                })
                .thenApply(s -> {  // task2
                    // 3
                    log.info("3:{}", s);
                    return s;
                })
                .exceptionally(e -> {
                    // 4       异常中断了task2，在这里被消化
                    log.error("4:{}", e.getMessage());
                    return e.getMessage();
                })
                .join();
    }

    @Test
    public void testPipeline() {
        CompletableFuture<String> future = new CompletableFuture();
        // task1
        future.completeExceptionally(new Exception("测试抛异常"));
        future.exceptionally(e -> {
            // 1
            log.error("1:{}", e.getMessage());
            return e.getMessage();
        }).whenComplete((s, t) -> {
            // 2
            log.info("2:{}", s);
            if (t != null)   // 注意，t如果为null，会导致抛异常
                log.error(t.getMessage());
        }).exceptionally(e -> {
            // 3    1将异常消化了，所以不会触发3
            log.error("3:{}", e.getMessage());
            return e.getMessage();
        }).thenAccept(s -> {  // task2
            // 4
            log.info("4:{}", s + " world");
        })
        .join();     //main线程等任务执行完再结束
    }


    @Test
    public void testCmopletableFuture() throws Exception {

        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(this::getThreadName);
        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(this::getThreadName);

        sleep(3000);
        CompletableFuture.allOf(future1, future2)  //  执行到allof，future1和 future2已经结束
                .thenApply(t ->{
                    log.info(Thread.currentThread().getName());//此处为main线程
                    return getThreadName(future1, future2);}
                )
                .exceptionally(e->{
                    log.info(Thread.currentThread().getName());
                    return Lists.newArrayList();})
                .join();
    }

    private String getThreadName(){
        sleep(1000);
        String res = Thread.currentThread().getName();
        return res;
    }

    private List<String> getThreadName(CompletableFuture<String> future1, CompletableFuture<String> future2){
        List<String> res = Lists.newLinkedList();
        try{
            res.add(future1.get());
            res.add(future2.get());
        }catch(Exception e){

        }
        log.info("{}", res);
        return res;
    }

    private void sleep(long millis){
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    //endregion

}
