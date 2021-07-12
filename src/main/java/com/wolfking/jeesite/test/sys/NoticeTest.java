package com.wolfking.jeesite.test.sys;

import com.wolfking.jeesite.modules.sys.entity.Notice;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.NoticeService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

/**
 * Created by ryan
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class NoticeTest {

//    @Autowired
//    private NoticeService noticeService;



    @Test
    public void testInser(){
        Notice notice = new Notice();
        notice.setUserId(1l);
        notice.setTitle("");
        notice.setContext("[问题反馈]工单号：K20170915000002回复问题反馈");
        notice.setLink("");
        notice.setNoticeType(Notice.NOTICE_TYPE_FEEDBACK);
        notice.setCreateBy(new User(1l,"系统管理员"));
        notice.setCreateDate(new Date());
        notice.setReferId(11111l);
        //noticeService.insert(notice);
        System.out.println("new id:" + notice.getId().toString());
    }

    @Test
    public void testFind(){
        //List<Notice> notices = noticeService.getListByUserID(1l,0);
//        for (Notice notice:notices) {
//            System.out.println("id:" + notice.getId().toString());
//        }

    }

    @Test
    public void testgetNotReadLastId(){

        //Long lid = noticeService.getNotReadLastId(913554653000634367l,1852l,1);
//        Assert.assertNotNull(lid);
//        System.out.println("id:" + lid);

    }


}
