package com.wolfking.jeesite.ms.service.push;

import com.kkl.kklplus.entity.push.*;
import com.wolfking.jeesite.common.config.Global;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.md.entity.Engineer;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.md.service.ServicePointService;
import com.wolfking.jeesite.modules.mq.sender.PushMessageSender;
import com.wolfking.jeesite.modules.sys.dao.APPNoticeDao;
import com.wolfking.jeesite.modules.sys.entity.APPNotice;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.entity.AppPushMessage;
import com.wolfking.jeesite.ms.providersys.service.MSAppNoticeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

/**
 * 推送切换为为服务
 */
@Slf4j
@Service
public class APPMessagePushService {

    @Autowired
    private PushMessageSender pushMessageSender;

//    @Resource
//    private APPNoticeDao appNoticeDao;

    @Autowired
    private MSAppNoticeService appNoticeService;  //add on 2020-7-10

    @Autowired
    private ServicePointService servicePointService;

//    @Autowired
//    private UserDao userDao;
//
//    @Autowired
//    private ServicePointService servicePointService;

    public void sendMessage(AppPushMessage message) {
        boolean pushEnabled = Boolean.valueOf(Global.getConfig("pushEnabled"));
        if (!pushEnabled) {
            return;
        }
        //APPNotice appNotice = appNoticeDao.getByUserId(message.getUserId());      //mark on 2020-7-10
        APPNotice appNotice = appNoticeService.getByUserId(message.getUserId());    //add on 2020-7-10
        if (appNotice != null) {
            try {
                MQAppPushMessage.PushMessage mqMessage = MQAppPushMessage.PushMessage.newBuilder()
                        .setPassThrough(message.getPassThroughType().value)
                        .setPlatform(appNotice.getPlatform())
                        .setRegId(appNotice.getChannelId())
                        .setMessageType(message.getMessageType().value)
                        .setSubject(message.getSubject())
                        .setUserId(message.getUserId())
                        .setTimestamp(message.getTimestamp())
                        .setTitle(message.getTitle())
                        .setDescription(message.getDescription())
                        .setContent(message.getContent())
                        .setCastMethod(CastMethodEnum.UNICAST.getValue())
                        .setCategory(AppMessageCategoryEnum.ORDER.getValue())
                        .build();

                pushMessageSender.send(mqMessage);
            }catch (Exception e){
                log.error("发送推送错误,user:{} ,msg:{}",message.getUserId(),message.getContent(),e);
            }
        }
    }

    /**
     * 向所有师傅APP广播消息
     *
     * @param title   主标题（Android消息弹窗上的标题，iOS不显示，小于50个字符）
     * @param content 副标题（Android与iOS消息弹窗上的消息内容，小于128个字符，不能为空）
     */
    public void broadcastToAllServicePoints(String title, String content, Date createDate) {
        boolean pushEnabled = Boolean.valueOf(Global.getConfig("pushEnabled"));
        if (!pushEnabled) {
            return;
        }
        try {
            MQAppPushMessage.PushMessage mqMessage = MQAppPushMessage.PushMessage.newBuilder()
                    .setCastMethod(CastMethodEnum.BROADCAST.getValue())
                    .setPlatform(Platform.ALL.value)
                    .setCategory(AppMessageCategoryEnum.SERVICE_POINT_NOTICE.getValue())
                    .setPassThrough(AppPushMessage.PassThroughType.NOTIFICATION.value)
                    .setMessageType(AppMessageType.SYSTEM.value)
                    .setTitle(title)
                    .setDescription(content)
                    .setTimestamp(createDate.getTime())
                    .setRegId("")
                    .setSubject("")
                    .setContent("")
                    .build();
            pushMessageSender.send(mqMessage);
        } catch (Exception e) {
            log.error("APPMessagePushService.broadcastAlltoServicePoints", e);
        }
    }

//    public void pushMessageToServicePointPrimaryAccount(Long servicePointId, String title, String content, Date createDate) {
//        boolean pushEnabled = Boolean.valueOf(Global.getConfig("pushEnabled"));
//        if (!pushEnabled) {
//            return;
//        }
//
//        ServicePoint servicePoint = servicePointService.getFromCache(servicePointId);
//        if (servicePoint.getPrimary() != null && servicePoint.getPrimary().getId() != null && servicePoint.getPrimary().getId() > 0) {
//            User user = userDao.getByEngineerId(servicePoint.getPrimary().getId());
//            if (user != null && user.getId() != null && user.getId() > 0) {
//                APPNotice appNotice = appNoticeDao.getByUserId(user.getId());
//                if (appNotice != null) {
//                    try {
//                        MQAppPushMessage.PushMessage mqMessage = MQAppPushMessage.PushMessage.newBuilder()
//                                .setCastMethod(CastMethodEnum.UNICAST.getValue())
//                                .setPassThrough(AppPushMessage.PassThroughType.NOTIFICATION.value)
//                                .setPlatform(appNotice.getPlatform())
//                                .setRegId(appNotice.getChannelId())
//                                .setMessageType(AppMessageType.SYSTEM.value)
//                                .setSubject("")
//                                .setTimestamp(createDate.getTime())
//                                .setTitle(title)
//                                .setContent("")
//                                .setDescription(content)
//                                .build();
//                        pushMessageSender.send(mqMessage);
//                    } catch (Exception e) {
//                        log.error("APPMessagePushService.pushMessageToServicePointPrimaryAccount", e);
//                    }
//                }
//            }
//        }
//    }


    public void pushMessageToEngineer(Long servicePointId, Long engineerId, String title, String content, Date createDate) {
        boolean pushEnabled = Boolean.valueOf(Global.getConfig("pushEnabled"));
        if (!pushEnabled) {
            return;
        }

        Engineer engineer = servicePointService.getEngineerFromCache(servicePointId, engineerId);
        if (engineer != null && engineer.getAccountId() != null && engineer.getAccountId() > 0) {
            //APPNotice appNotice = appNoticeDao.getByUserId(engineer.getAccountId());      //mark on 2020-7-10
            APPNotice appNotice = appNoticeService.getByUserId(engineer.getAccountId());    //add on 2020-7-10
            if (appNotice != null) {
                try {
                    MQAppPushMessage.PushMessage mqMessage = MQAppPushMessage.PushMessage.newBuilder()
                            .setCastMethod(CastMethodEnum.UNICAST.getValue())
                            .setPassThrough(AppPushMessage.PassThroughType.NOTIFICATION.value)
                            .setPlatform(appNotice.getPlatform())
                            .setRegId(appNotice.getChannelId())
                            .setMessageType(AppMessageType.SYSTEM.value)
                            .setSubject("")
                            .setTimestamp(createDate.getTime())
                            .setTitle(title)
                            .setContent("")
                            .setDescription(content)
                            .build();
                    pushMessageSender.send(mqMessage);
                } catch (Exception e) {
                    log.error("APPMessagePushService.pushMessageToEngineer", e);
                }
            }
        }
    }

    public void pushMessageToEngineer(AppMessageType appMessageType,Long servicePointId, Long engineerId, String title, String content) {
        if(appMessageType == null || StringUtils.isBlank(content)){
            return;
        }
        if(servicePointId == null || servicePointId <= 0 || engineerId == null || engineerId <= 0){
            return;
        }
        boolean pushEnabled = Boolean.valueOf(Global.getConfig("pushEnabled"));
        if (!pushEnabled) {
            return;
        }

        Engineer engineer = servicePointService.getEngineerFromCache(servicePointId, engineerId);
        if (engineer != null && engineer.getAccountId() != null && engineer.getAccountId() > 0) {
            //APPNotice appNotice = appNoticeDao.getByUserId(engineer.getAccountId());      //mark on 2020-7-10
            APPNotice appNotice = appNoticeService.getByUserId(engineer.getAccountId());    //add on 2020-7-10
            if (appNotice != null) {
                try {
                    MQAppPushMessage.PushMessage mqMessage = MQAppPushMessage.PushMessage.newBuilder()
                            .setCastMethod(CastMethodEnum.UNICAST.getValue())
                            .setPassThrough(AppPushMessage.PassThroughType.NOTIFICATION.value)
                            .setPlatform(appNotice.getPlatform())
                            .setRegId(appNotice.getChannelId())
                            .setMessageType(appMessageType.value)
                            .setSubject("")
                            .setTimestamp(System.currentTimeMillis())
                            .setTitle(StringUtils.isBlank(title)?appMessageType.title:title)
                            .setContent("")
                            .setDescription(content)
                            .build();
                    pushMessageSender.send(mqMessage);
                } catch (Exception e) {
                    log.error("APPMessagePushService.pushMessageToEngineer", e);
                }
            }
        }
    }
}
