package com.wolfking.jeesite.test.ms.cc;

import com.google.common.collect.Maps;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.cc.Reminder;
import com.kkl.kklplus.entity.cc.ReminderStatus;
import com.kkl.kklplus.entity.cc.ReminderType;
import com.kkl.kklplus.entity.sys.SysUser;
import com.wolfking.jeesite.common.utils.GsonUtils;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.cc.entity.ReminderModel;
import com.wolfking.jeesite.ms.cc.entity.mapper.ReminderModelMapper;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.Map;

/**
 * 测试催单
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Slf4j
@SpringBootTest
public class ReminderTest {

    /**
     * 测试转换
     */
    @Test
    public void testMapper() {
        Reminder reminder = Reminder.builder()
                .dataSource(1)
                .quarter("20193")
                .reminderType(ReminderType.Manual.getCode())
                .reminderNo("CC201907120001")
                .orderId(1148798595793817600l)
                .orderNo("K2019071048760")
                .servicepointId(0l)
                .customerId(1482l)
                .userName("测试app异常")
                .userPhone("15850088888")
                .userAddress("广东省 深圳市 龙华区 龙华街道 测试地址")
                .provinceId(13l)
                .cityId(176l)
                .areaId(3403l)
                .subAreaId(19951l)
                .status(ReminderStatus.WaitReply.getCode())
                .reminderRemark("催单意见")
                .processBy("处理人")
                .processAt(1562899297845l)
                .processRemark("处理意见")
                .completeBy("完成人")
                .completeAt(1562899325846l)
                .completeRemark("完成意见")
                //.closeBy("关闭人")
                //.closeAt(1562899359171l)
                //.closeRemark("关闭意见")
                .processTimes(1)
                .reminderTimes(1)
                .build();
        reminder.setId(1234567890l);
        reminder.setCreateDt(1562896800000l);
        reminder.setCreateById(1l);
        //ReminderModel model = Mappers.getMapper(ReminderModelMapper.class).toViewModel(reminder);
        //log.warn(GsonUtils.getInstance().toGson(model));

        List<Reminder> reminders = Lists.newArrayList(reminder);
        /*
        List<ReminderModel> models = Mappers.getMapper(ReminderModelMapper.class).reminderToViewModels(reminders);
        log.warn(GsonUtils.getInstance().toGson(models));
        */
    }

}
