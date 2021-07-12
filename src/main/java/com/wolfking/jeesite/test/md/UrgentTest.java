package com.wolfking.jeesite.test.md;

import com.wolfking.jeesite.modules.md.entity.Material;
import com.wolfking.jeesite.modules.md.entity.Product;
import com.wolfking.jeesite.modules.md.entity.ProductCategory;
import com.wolfking.jeesite.modules.md.entity.UrgentLevel;
import com.wolfking.jeesite.modules.md.entity.viewModel.AreaUrgentModel;
import com.wolfking.jeesite.modules.md.service.ProductService;
import com.wolfking.jeesite.modules.md.service.UrgentCustomerService;
import com.wolfking.jeesite.modules.md.service.UrgentLevelService;
import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.Logger;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * 加急配置测试
 * Created by Ryan on 2018/6/24.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@Slf4j
public class UrgentTest {

    @Autowired
    private UrgentCustomerService urgentCustomerService;

    @Autowired
    private UrgentLevelService urgentLevelService;

    @Test
    public void testFindAll(){
        List<UrgentLevel> list = urgentLevelService.findAllList();
        Assert.assertTrue("not retrun any",list != null && list.size()>0);
        list.forEach(t->{
            log.info("id:{} ,label:{} ,chargeIn:{} ,chargeOut:{}",t.getId(),t.getLabel(),t.getChargeIn(),t.getChargeOut());
        });
    }

    @Test
    public void testGetCustomerUrgentConfig(){
        List<AreaUrgentModel> items = urgentCustomerService.findListByCustomerId(618l);
        Assert.assertTrue("not return any value",items != null && items.size()>0);
        items.forEach(t->{
            log.info("area id:{},",t.getArea().getId());
            t.getList().forEach(p->{
                log.info("level id:{} ,label:{} ,chargeIn:{} chargeOut:{}",p.getUrgentLevel().getId(),p.getUrgentLevel().getLabel(),p.getChargeIn(),p.getChargeOut());
            });
        });
    }
}
