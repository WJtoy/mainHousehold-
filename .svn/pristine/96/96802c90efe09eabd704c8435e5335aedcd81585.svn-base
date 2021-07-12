package com.wolfking.jeesite.test.md;

import com.wolfking.jeesite.common.utils.GsonUtils;
import com.wolfking.jeesite.modules.md.entity.AreaTimeLiness;
import com.wolfking.jeesite.modules.md.entity.TimelinessLevel;
import com.wolfking.jeesite.modules.md.entity.UrgentLevel;
import com.wolfking.jeesite.modules.md.entity.viewModel.AreaUrgentModel;
import com.wolfking.jeesite.modules.md.service.AreaTimeLinessService;
import com.wolfking.jeesite.modules.md.service.TimelinessLevelService;
import com.wolfking.jeesite.modules.md.service.UrgentCustomerService;
import com.wolfking.jeesite.modules.md.service.UrgentLevelService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

/**
 * 时效配置测试
 * Created by Ryan on 2018/6/24.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@Slf4j
public class TimeLinessTest {

    @Autowired
    private TimelinessLevelService timelinessLevelService;

    @Autowired
    private AreaTimeLinessService areaTimeLinessService;


    @Test
    public void testFindAll(){
        List<TimelinessLevel> list = timelinessLevelService.findAllList();
        Assert.assertTrue("not retrun any",list != null && list.size()>0);
        list.forEach(t->{
            log.info("id:{} ,name:{} ,chargeIn:{} ,chargeOut:{}",t.getId(),t.getName(),t.getChargeIn(),t.getChargeOut());
        });
    }

    //@Test
    //public void getAreaTimeLinessByArea(){
    //    long areaId = 303;
    //    AreaTimeLiness entity = areaTimeLinessService.getFromCache(areaId);
    //    if(entity == null){
    //        log.info("return null");
    //    }else{
    //        log.info("entity:{}", GsonUtils.getInstance().toGson(entity));
    //    }
    //}

}
