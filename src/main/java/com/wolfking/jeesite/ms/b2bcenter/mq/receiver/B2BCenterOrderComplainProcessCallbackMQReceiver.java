package com.wolfking.jeesite.ms.b2bcenter.mq.receiver;

import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2b.common.B2BProcessFlag;
import com.kkl.kklplus.entity.b2b.mq.B2BMQConstant;
import com.kkl.kklplus.entity.b2b.mq.B2BMQQueueType;
import com.kkl.kklplus.entity.b2bcenter.pb.MQB2BOrderComplainProcessMessage;
import com.kkl.kklplus.utils.StringUtils;
import com.rabbitmq.client.Channel;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.ms.b2bcenter.mq.config.B2BCenterOrderComplainConfig;
import com.wolfking.jeesite.ms.b2bcenter.mq.config.B2BCenterOrderComplainProcessCallbackConfig;
import com.wolfking.jeesite.ms.b2bcenter.mq.sender.B2BCenterOrderComplainProcessCallbackMQSender;
import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderVModel;
import com.wolfking.jeesite.ms.b2bcenter.sd.service.B2BOrderComplainService;
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
public class B2BCenterOrderComplainProcessCallbackMQReceiver implements ChannelAwareMessageListener {

    @Autowired
    private B2BCenterOrderComplainProcessCallbackMQSender sender;

    @Autowired
    private B2BOrderComplainService b2BOrderComplainService;



    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        User user = B2BOrderVModel.b2bUser;
        MQB2BOrderComplainProcessMessage.B2BOrderComplainProcessMessage msgObj = null;
        try {
            msgObj = MQB2BOrderComplainProcessMessage.B2BOrderComplainProcessMessage.parseFrom(message.getBody());
            if (msgObj == null) {
                processFail(B2BMQQueueType.B2BCENTER_ORDER_COMPLAIN_PROCESS_RETRY,null, B2BCenterOrderComplainProcessCallbackConfig.B2B_UID,B2BProcessFlag.PROCESS_FLAG_FAILURE,1,"消息体错误",null);
                return;
            }
            if (msgObj.getKklComplainId() <= 0) {
                processFail(B2BMQQueueType.B2BCENTER_ORDER_COMPLAIN_PROCESS_RETRY,null,B2BCenterOrderComplainProcessCallbackConfig.B2B_UID,B2BProcessFlag.PROCESS_FLAG_FAILURE,1,"参数错误:快可立投诉单Id内容错误",null);
                return;
            }
            int times = StringUtils.toInteger(message.getMessageProperties().getHeaders().get(B2BMQConstant.MESSAGE_PROPERTIES_HEADER_KEY_TIMES));
            MSResponse response = b2BOrderComplainService.processCallback(msgObj);
            if (!MSResponse.isSuccessCode(response)) {
                processFail(B2BMQQueueType.B2BCENTER_ORDER_COMPLAIN_PROCESS_RETRY,msgObj,B2BCenterOrderComplainConfig.B2B_UID,B2BProcessFlag.PROCESS_FLAG_FAILURE,1,"投诉单处理进度失败",times);
                return;
            }
        } catch (Exception e) {
            if (msgObj != null) {
                String msgJson = new JsonFormat().printToString(msgObj);
                log.error("重试消息失败, json:{}, errorMsg: {}", msgJson, e);
                LogUtils.saveLog("重试消息失败", "B2BCenterOrderComplainProcessCallbackMQReceiver", msgJson, e, user);
            } else {
                log.error("重试消息失败, errorMsg: {}", e);
                LogUtils.saveLog("重试消息失败", "B2BCenterOrderComplainProcessCallbackMQReceiver", "", e, user);
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
    private void processFail(B2BMQQueueType queueType, MQB2BOrderComplainProcessMessage.B2BOrderComplainProcessMessage message, Long createById,
                             B2BProcessFlag processFlag, int processTime, String processComment,Integer retryTimes){
        String msgJson = StringUtils.EMPTY;
        try {
            if(message != null){
                msgJson = new JsonFormat().printToString(message);
            }
            B2BFailureLogUtils.saveFailureLog(queueType, msgJson, createById, processFlag, processTime, processComment);
        }catch (Exception e){
            LogUtils.saveLog("失败处理错误", "B2BCenterOrderComplainProcessCallbackMQReceiver", "", e, new User(createById));
        }
        //重试
        if(retryTimes != null && retryTimes < B2BCenterOrderComplainConfig.RETRY_TIMES){
            retryTimes++;
            sender.sendRetry(message, retryTimes);
        }else{
            log.error(retryTimes==null?"[B2B投诉回调-发送跟进信息]初次处理失败,msg:{}":"[B2B投诉回调进度-发送跟进信息]重试3次失败,msg:{}",msgJson);
            LogUtils.saveLog(retryTimes==null?"B2B投诉回调-初次处理失败":"B2B投诉回调-重试3次失败", "B2BCenterOrderComplainProcessCallbackMQReceiver", msgJson, null, new User(createById));
        }
    }

}

