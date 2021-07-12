package com.wolfking.jeesite.test.common;

import com.wolfking.jeesite.common.config.Global;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;


/**
 * Created by yanshenglu
 * 测试logback日志
 */
//@RunWith(SpringJUnit4ClassRunner.class)
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class LogbackTest {

    @Test
    public void testWriteLogs(){
        log.trace("trace:{}",System.currentTimeMillis());
        log.info("info:{}",System.currentTimeMillis());
        log.debug("debug:{}",System.currentTimeMillis());
        log.warn("warn:{}",System.currentTimeMillis());
        log.error("error:{}",System.currentTimeMillis());
    }


}
