package com.wolfking.jeesite.test.common;

import com.wolfking.jeesite.common.utils.DateUtils;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;

/**
 * 测试synchronized
 * @autor Ryan Lu
 * @date 2018/11/24 11:28 PM
 */
@SpringBootTest
public class TestSynchronized {

    private static final int THREAD_COUNT = 5;

    @Test
    public void testStringThread() {
        Thread[] threads = new Thread[THREAD_COUNT];
        for (int i = 0; i < THREAD_COUNT; i++) {
            if(i==2) {
                threads[i] = new Thread(new StringThread("192.168.1.2"));
            }else{
                threads[i] = new Thread(new StringThread("192.168.1.1"));
            }
        }

        for (int i = 0; i < THREAD_COUNT; i++) {
            threads[i].start();
        }

        for (;;);
    }

    public class StringThread implements Runnable {

        private static final String LOCK_PREFIX = "XXX---";

        private String ip;

        public StringThread(String ip) {
            this.ip = ip;
        }

        @Override
        public void run() {
            String lock = buildLock();
            synchronized (lock) {//根据不同的key锁
                String threadName = Thread.currentThread().getName();
                System.out.println(MessageFormat.format("{0} {1} start", DateUtils.getDate("yyyy-MM-dd HH:mm:ss.SSS"),threadName));
                // 休眠5秒模拟脚本调用
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(MessageFormat.format("{0} {1} end", DateUtils.getDate("yyyy-MM-dd HH:mm:ss.SSS"),threadName));
            }
        }

        private String buildLock() {
            StringBuilder sb = new StringBuilder();
            sb.append(LOCK_PREFIX);
            sb.append(ip);

            String lock = sb.toString().intern();
            //System.out.println("[" + Thread.currentThread().getName() + "]构建了锁[" + lock + "]");

            return lock;
        }

    }
}
