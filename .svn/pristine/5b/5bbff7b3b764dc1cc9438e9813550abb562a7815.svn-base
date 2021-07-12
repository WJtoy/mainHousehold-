package com.wolfking.jeesite.ms.b2bcenter.mq.receiver;

import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2b.common.B2BProcessFlag;
import com.kkl.kklplus.entity.b2b.mq.B2BMQConstant;
import com.kkl.kklplus.entity.b2b.mq.B2BMQQueueType;
import com.kkl.kklplus.entity.b2bcenter.pb.MQB2BOrderReminderProcessMessage;
import com.kkl.kklplus.utils.StringUtils;
import com.rabbitmq.client.Channel;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.ms.b2bcenter.mq.config.B2BCenterOrderReminderProcessConfig;
import com.wolfking.jeesite.ms.b2bcenter.mq.sender.B2BCenterOrderReminderProcessMQSender;
import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderVModel;
import com.wolfking.jeesite.ms.b2bcenter.sd.service.B2BOrderReminderService;
import com.wolfking.jeesite.ms.utils.B2BFailureLogUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 投诉单进度处理消费者
 */
@Slf4j
@Component
public class B2BCenterOrderReminderProcessMQReceiver implements ChannelAwareMessageListener {

    @Autowired
    private B2BCenterOrderReminderProcessMQSender sender;

    @Autowired
    private B2BOrderReminderService b2BOrderReminderService;



    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        User user = B2BOrderVModel.b2bUser;
        MQB2BOrderReminderProcessMessage.B2BOrderReminderProcessMessage msgObj = null;
        try {
            msgObj = MQB2BOrderReminderProcessMessage.B2BOrderReminderProcessMessage.parseFrom(message.getBody());
            if (msgObj == null) {
                processFail(B2BMQQueueType.B2BCENTER_NEW_B2BORDER_REMINDER_RETRY,null, B2BCenterOrderReminderProcessConfig.B2B_UID,B2BProcessFlag.PROCESS_FLAG_FAILURE,1,"消息体错误",null);
                return;
            }
            if (msgObj.getKklReminderId() <= 0) {
                processFail(B2BMQQueueType.B2BCENTER_NEW_B2BORDER_REMINDER_RETRY,null,B2BCenterOrderReminderProcessConfig.B2B_UID,B2BProcessFlag.PROCESS_FLAG_FAILURE,1,"参数错误:缺少kkl催单id",null);
                return;
            }
            int times = StringUtils.toInteger(message.getMessageProperties().getHeaders().get(B2BMQConstant.MESSAGE_PROPERTIES_HEADER_KEY_TIMES));
            MSResponse response = b2BOrderReminderService.reminderProcessCallback(msgObj);
            if (!MSResponse.isSuccessCode(response)) {
                processFail(B2BMQQueueType.B2BCENTER_NEW_B2BORDER_REMINDER_RETRY,msgObj,B2BCenterOrderReminderProcessConfig.B2B_UID,B2BProcessFlag.PROCESS_FLAG_FAILURE,1,"处理第三方系统催单进度失败",times);
                return;
            }
        } catch (Exception e) {
            if (msgObj != null) {
                String msgJson = new JsonFormat().printToString(msgObj);
                log.error("处理第三方系统催单进度重试消息失败, json:{}, errorMsg: {}", msgJson, e);
                LogUtils.saveLog("处理第三方系统催单进度重试消息失败", "B2BCenterOrderReminderProcessMQReceiver", msgJson, e, user);
            } else {
                log.error("处理第三方系统催单进度重试消息失败, errorMsg: {}", e);
                LogUtils.saveLog("处理第三方系统催单进度重试消息失败", "B2BCenterOrderReminderProcessMQReceiver", "", e, user);
            }
        } finally {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }
    }

    /**
     * 处理失败
     * 1.记录日志
     * 2.重送处理
     * @param queueType         队列类型
     * @param message           消息体
     * @param createById
     * @param processFlag       状态
     * @param processTime       错误次数
     * @param processComment    错误内容
     * @param retryTimes        重试次数,null不重送，超过重送次数也不重送
     */
    private void processFail(B2BMQQueueType queueType, MQB2BOrderReminderProcessMessage.B2BOrderReminderProcessMessage message, Long createById,
                             B2BProcessFlag processFlag, int processTime, String processComment,Integer retryTimes){
        String msgJson = StringUtils.EMPTY;
        try {
            if(message != null){
                msgJson = new JsonFormat().printToString(message);
            }
            B2BFailureLogUtils.saveFailureLog(queueType, msgJson, createById, processFlag, processTime, processComment);
        }catch (Exception e){
            LogUtils.saveLog("失败处理错误", "B2BCenterOrderReminderProcessMQReceiver", "", e, new User(createById));
        }
        //重试
        if(retryTimes != null && retryTimes < B2BCenterOrderReminderProcessConfig.RETRY_TIMES){
            retryTimes++;
            sender.sendRetry(message, retryTimes);
        }else{
            log.error(retryTimes==null?"[第三方发送催单跟踪进度]初次处理失败,msg:{}":"[第三方发送催单跟踪进度]重试3次失败,msg:{}",msgJson);
            LogUtils.saveLog(retryTimes==null?"[第三方发送催单跟踪进度]初次处理失败":"[第三方发送催单跟踪进度]重试3次失败", "B2BCenterOrderReminderProcessMQReceiver", msgJson, null, new User(createById));
        }
    }

}

