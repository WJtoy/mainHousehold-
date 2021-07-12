package com.wolfking.jeesite.test.ms.material;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.wolfking.jeesite.ms.material.service.B2BMaterialExecutor;
import com.wolfking.jeesite.ms.material.service.B2BMaterialExecutorFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/**
 * B2B配件微服务测试类
 */
@ActiveProfiles("dev")
@RunWith(SpringJUnit4ClassRunner.class)
//@RunWith(SpringRunner.class)
@Slf4j
@SpringBootTest
public class B2BMaterialTest {

    @Autowired
    private B2BMaterialExecutorFactory b2BMaterialExecutorFactory;

    /**
     * 测试工厂模型
     */
    @Test
    public void testB2BMateiralFactory() {
        B2BMaterialExecutor executor = b2BMaterialExecutorFactory.getExecutor(B2BDataSourceEnum.JOYOUNG);
        MSResponse msResponse = executor.newMaterialForm(null);
        System.out.println(msResponse.getMsg());
    }

}
