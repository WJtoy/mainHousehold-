package com.wolfking.jeesite.modules.sd.service;

import com.wolfking.jeesite.common.service.LongIDBaseService;
import com.wolfking.jeesite.modules.mq.sender.ShortMessageSender;
import com.wolfking.jeesite.modules.sd.dao.TestDao;
import com.wolfking.jeesite.modules.sd.entity.OrderCondition;
import com.wolfking.jeesite.modules.sys.entity.Notice;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.NoticeService;
import com.wolfking.jeesite.modules.td.entity.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.Date;
import java.util.List;

/**
 * Created by Jeff on 2017/7/24.
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class TestService extends LongIDBaseService {

    @Autowired
    private TestDao testDao;

//    @Autowired
//    private MessageDao messageDao;

    @Autowired
    private ShortMessageSender shortMessageSender;

//    @Autowired
//    private NoticeService noticeService;

    @Transactional(readOnly = true)
    public List<OrderCondition> findOrderConditionList(int pageIndex, int pageSize) {
        return testDao.findOrderConditionList(pageIndex, pageSize);
    }

    public List<OrderCondition> findOrderConditionListReadOnly(int pageIndex, int pageSize) {
        return testDao.findOrderConditionList(pageIndex, pageSize);
    }

    @Transactional(readOnly = false)
    public void testSetRollbackOnly() {
        try {
            Message message = new Message();
            message.setContent("测试setRollbackOnly");
            message.setMobile("13760468206");
            message.setStatus(30);
            message.setType("pt");
            message.setTriggerBy(1l);
            message.setTriggerDate(new Date());
            message.setSendTime(new Date());
            message.setRetryTimes(1);
            message.setCreateBy(new User(1l, "", "系统管理员"));
            message.setCreateDate(new Date());
//            messageDao.insert(message);
            int zero = 1 / 0;   //打开这个异常
        } catch (Exception e) {
            e.printStackTrace();
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            /* 未成功commit数据库
            Message message = new Message();
            message.setContent("测试setRollbackOnly,catch部分");
            message.setMobile("13760468206");
            message.setStatus(30);
            message.setType("pt");
            message.setTriggerBy(1l);
            message.setTriggerDate(new Date());
            message.setSendTime(new Date());
            message.setRetryTimes(1);
            message.setCreateBy(new User(1l,"","系统管理员"));
            message.setCreateDate(new Date());
            messageDao.insert(message);
            */
            /* 未成功commit数据库
            Log log = new Log();
            log.setRemoteAddr("loclahost");
            log.setUserAgent("Test");
            log.setRequestUri("TestService.testSetRollbackOnly");
            log.setMethod("test");
            log.setTitle("测试");
            log.setType(1);
            log.setCreateBy(new User(1l,"","系统管理员"));
            log.setCreateDate(new Date());
            logDao.insert(log);
            */
            /* 日志 成功
            LogUtils.saveLog("测试","TestService.testSetRollbackOnly","Test",null,null,2);
            */
            /* mq 成功
            MQShortMessage.ShortMessage message = MQShortMessage.ShortMessage.newBuilder()
                    .setMobile("13760468206")
                    .setSendTime(new Date().getTime())
                    .setContent("石师傅,现有: 赵辉13760468206 河东区 大桥道萦东花园8号楼4门602九阳燃气灶1台 需要安装，请2小时内联系用户确认安维环境并预约上门时间，务必48小时内上门，严禁对产品作任何评价，带齐相应的工具和配件，现场有问题请联系客服林小姐0757-26169180/4006663653")
                    .setId(0l)
                    .setExtNo("086")
                    .setTriggerBy(1l)
                    .setTriggerDate(new Date().getTime())
                    .setType("pt")
                    .build();
            shortMessageSender.send(message);
            */
            /* 其他service 失败 */
            Notice notice = new Notice();
            notice.setUserId(1l);
            notice.setTitle("问题反馈");
            notice.setReferNo("K20181122000001");
            notice.setContext("[问题反馈]工单号：K20181122000001 回复问题反馈");
            notice.setLink("");
            notice.setNoticeType(Notice.NOTICE_TYPE_FEEDBACK);
            notice.setCreateBy(new User(1l, "系统管理员"));
            notice.setCreateDate(new Date());
            notice.setReferId(11111l);
            //noticeService.insert(notice);

            /* 异步 成功
            try {
                CompletableFuture.supplyAsync(() -> {
                        Notice notice = new Notice();
                        notice.setUserId(1l);
                        notice.setTitle("问题反馈");
                        notice.setReferNo("K20181122000001");
                        notice.setContext("[问题反馈]工单号：K20181122000001 回复问题反馈");
                        notice.setLink("");
                        notice.setNoticeType(Notice.NOTICE_TYPE_FEEDBACK);
                        notice.setCreateBy(new User(1l,"系统管理员"));
                        notice.setCreateDate(new Date());
                        notice.setReferId(11111l);
                        noticeService.insert(notice);
                        return true;
                    })
                    .exceptionally(t -> {
                        System.out.println("Unexpected error:" + t);
                        return null;
                    }).get();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            } catch (ExecutionException e1) {
                e1.printStackTrace();
            }*/

            System.out.println("test");
        }
    }
}
