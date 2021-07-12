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

import java.util.Date;


/**
 * Created by yanshenglu
 * 测试读取配置文件(application.yml)
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@Slf4j
public class ConfigTest {

    @Autowired
    private OrderService orderService;
    @Test
    public void testGetGoLiveDateFromConfig(){
        String date = Global.getConfig("GoLiveDate");
        log.info("GoLiveDate:{}",date);
    }

    @Test
    public void testGetGoLiveDate(){
        Date date = OrderUtils.getGoLiveDate();
        Assert.assertTrue("date is null",date != null);
        log.info("date:{}", DateUtils.formatDate(date,"yyyy-MM-dd"));
    }

}
