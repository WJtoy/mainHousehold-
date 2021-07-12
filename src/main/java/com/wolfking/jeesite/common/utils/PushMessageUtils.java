/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.wolfking.jeesite.common.utils;

import com.kkl.kklplus.entity.push.AppMessageType;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.entity.AppPushMessage;
import com.wolfking.jeesite.ms.service.push.APPMessagePushService;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

/**
 * APP消息推送工具类
 *
 * @author Ryan
 * @version 2018-01-15
 */
@Slf4j
public class PushMessageUtils {

    //	private static PushMessageSender sender = SpringContextHolder.getBean(PushMessageSender.class);
    private static APPMessagePushService appMessagePushService = SpringContextHolder.getBean(APPMessagePushService.class);

//    public enum MessageType {
//        //0-接单消息
//        AcceptOrder,
//        //1-派单消息
//        PlanOrder,
//        //2-配件审核
//        MaterialPass,
//        //3-配件驳回
//        MaterialReject,
//        //4-配件已发货
//        MaterialDeliver,
//        //5-打款通知
//        Pay
//    }

//	/**
//	 * 发送APP推送消息到消息队列
//	 * @param pushMessageType	推送类型 0-透传，1-通知
//	 * @param messageType 消息类型 0-接单消息，1-派单消息 2-配件审核 3-配件驳回 4-配件已发货 5-打款通知
//	 * @param subject	主题
//	 * @param content	内容
//	 * @param userId	用户帐号id
//	 * @return	是否成功
//	 */
//	public static boolean push(MQPushMessage.PushMessageType pushMessageType,MessageType messageType,String subject,String content,Long userId
//	){
//		MQPushMessage.PushMessage.Builder pushBuilder = MQPushMessage.PushMessage.newBuilder();
//		pushBuilder.setPushMessageType(pushMessageType)
//				.setMessageType(messageType.ordinal())
//				.setSubject(subject)
//				.setContent("")
//				.setTimestamp(System.currentTimeMillis())
//				.setUserId(userId)
//				.setDescription(content);
//
//		AppPushMessage message = new AppPushMessage();
//		message.setUserId(userId);
//		message.setNoticeType(messageType.ordinal()+1);
//		message.setDescription(content);
//		message.setTitle("快可立");
//		appMessagePushService.pushMessage(message);
//		try{
////			sender.sendNew(pushBuilder.build());
//			return true;
//		}catch (Exception e){
//			log.error("发送APP推送错误",e);
//			return false;
//		}
//	}

    /**
     * 将推送切换为微服务
     * <p>
     * 发送APP推送消息到消息队列
     *
     * @param passThroughType 推送类型 0-透传，1-通知
     * @param messageType     消息类型 0-接单消息，1-派单消息 2-配件审核 3-配件驳回 4-配件已发货 5-打款通知
     * @param subject         主题
     * @param content         内容
     * @param userId          用户帐号id
     * @return 是否成功
     */
    public static boolean push(AppPushMessage.PassThroughType passThroughType, AppMessageType messageType, String subject, String content, Long userId) {

        AppPushMessage message = new AppPushMessage();
        message.setPassThroughType(passThroughType);
        message.setMessageType(messageType);
        message.setSubject(subject);
        message.setContent("");
        message.setTimestamp(System.currentTimeMillis());
        message.setUserId(userId);
        message.setDescription(content);
        message.setTitle("快可立");

        try {
            appMessagePushService.sendMessage(message);
            return true;
        } catch (Exception e) {
            log.error("发送APP推送错误", e);
            return false;
        }
    }

    /**
     * 催单通知
     * //todo: 小米推送api升级后处理催单语音提示
     */
    public static boolean pushReminderMessage(ServicePoint servicePoint, User engineer, String orderNo) {
        if (servicePoint != null && servicePoint.getId() != null && servicePoint.getId() > 0
                && engineer != null && engineer.getId() != null && engineer.getId() > 0) {
            try {
                String content = "您有新的催单(" + orderNo + ")，请及时处理！ ";
                appMessagePushService.pushMessageToEngineer(AppMessageType.REMINDER,servicePoint.getId(), engineer.getId(), AppMessageType.REMINDER.title, content);
                return true;
            } catch (Exception e) {
                log.error("发送催单通知错误", e);
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * 好评单通知
     * //todo: 小米推送api升级后处理催单语音提示
     */
    public static boolean pushPraiseMessage(Long servicePointId, Long engineerId, String title, String content) {
        if (servicePointId != null && servicePointId > 0
                && engineerId != null && engineerId > 0) {
            try {
                appMessagePushService.pushMessageToEngineer(AppMessageType.SYSTEM,servicePointId, engineerId, title, content);
                return true;
            } catch (Exception e) {
                log.error("发送催单通知错误", e);
                return false;
            }
        } else {
            return false;
        }
    }
}