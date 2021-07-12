package com.wolfking.jeesite.test.api;

import com.google.common.collect.Lists;
import com.wolfking.jeesite.modules.api.service.sd.AppOrderPraiseService;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class AppOrderPraiseTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private AppOrderPraiseService appOrderPraiseService;

    @Test
    public void saveOrderPraiseInfo() {
        Order order = orderService.getOrderById(1212212196692594688L, "20201", OrderUtils.OrderDataLevel.CONDITION, true);
        User user = new User(99999L, "APP测试工单好评");
        List<String> picUrls = Lists.newArrayList("2020/03/31/aa985642-f47d-4a98-bb7d-46f5024c73e72.jpg",
                "2020/04/06/a518fed1-6b7a-4cde-805a-888bd85a9cef1.jpg");
        appOrderPraiseService.saveOrderPraiseInfo(order, order.getOrderCondition().getServicePoint().getId(), order.getOrderCondition().getEngineer().getId(), picUrls, user);
    }
}
