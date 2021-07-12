package com.wolfking.jeesite.modules.mq.receiver.voice;

import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.entity.voiceservice.CallbackType;
import com.kkl.kklplus.entity.voiceservice.VoiceStatus;
import com.kkl.kklplus.entity.voiceservice.mq.MQVoiceSeviceMessage;
import com.rabbitmq.client.Channel;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.md.entity.*;
import com.wolfking.jeesite.modules.md.service.CustomerService;
import com.wolfking.jeesite.modules.md.service.GradeService;
import com.wolfking.jeesite.modules.md.service.ServiceTypeService;
import com.wolfking.jeesite.modules.sd.entity.*;
import com.wolfking.jeesite.modules.sd.service.*;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.httpclient.NameValuePair;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.*;

/**
 * 语音回访结果更新消息消费端
 * @author Ryan
 * @date 2019/01/02 10:29
 *
 * @author Ryan
 * @date 2019-06-24
 * 增加全局语音回访自动客评配置
 *
 */
@Slf4j
@Configuration
@Component
public class CallbackReceiver implements ChannelAwareMessageListener {

    @Autowired
    private OrderService orderService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private GradeService gradeService;

    @Autowired
    private OrderVoiceTaskService orderVoiceTaskService;

    @Autowired
    private ServiceTypeService serviceTypeService;
    
    @Autowired
    private OrderGradeService orderGradeService;

    @Autowired
    private OrderMaterialService orderMaterialService;

    @Autowired
    private OrderMaterialReturnService orderMaterialReturnService;

    @Value("${site.code}")
    private String siteCode;

    @Value("${voiceService.autoGrade}")
    private boolean voiceAutoGrade;

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        //先确认
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

        MQVoiceSeviceMessage.CallbackNotice callbackNotice = MQVoiceSeviceMessage.CallbackNotice.parseFrom(message.getBody());
        if(callbackNotice == null) {
            return;
        }
        // 消息处理
        onAction(callbackNotice);
    }

    /**
     * 处理接收的消息
     *
     * @author Ryan
     * @date 2019-06-24
     * 增加全局语音回访自动客评配置
     *
     */
    private void onAction(MQVoiceSeviceMessage.CallbackNotice callbackNotice){

        String json = new JsonFormat().printToString(callbackNotice);
        //是否是本子系统负责的消息
        if(!callbackNotice.getSite().equalsIgnoreCase(siteCode)){
            log.warn("此系统为:{},接收到其他系统消息:{},msg:{}",siteCode,callbackNotice.getSite(),json);

            String msg = MessageFormat.format("此系统为:{0},接收到非本系统消息:{1},msg:{2}",siteCode,callbackNotice.getSite(),json);
            LogUtils.saveLog("语音回访:site错误","Order.CallbackReceiver",msg,null,null);
            return;
        }

        User user = new User(2l, "语音回访", "");
        Date date = new Date();

        //region 更新语音回访结果
        OrderVoiceTask task = null;
        VoiceStatus voiceStatus = null;
        try {
            //检查sd_voice_task表
            task = orderVoiceTaskService.getBaseInfoByOrderId(callbackNotice.getOrderQuarter(), callbackNotice.getOrderId());
            if (task == null) {
                String msg = MessageFormat.format("语音回访:无语音回访记录。msg:{0}", json);
                log.warn(msg);
                LogUtils.saveLog("语音回访:无语音回访记录", "Order.CallbackReceiver", msg, null, null);
                return;
            }

            //重复通知
            if (callbackNotice.getConnectedAt() == task.getConnectedAt()) {
                log.info("语音回访:重复回调,msg:{}",json);
                return;
            }

            // 语音回访判断：根据status
            voiceStatus = VoiceStatus.fromCode(callbackNotice.getStatus());
            if(voiceStatus == null){
                voiceStatus = VoiceStatus.F;
            }
            //更新回访记录
            task.setConnectedAt(callbackNotice.getConnectedAt());
            task.setDisconnectedAt(callbackNotice.getDisconnectedAt());
            task.setEndReason(callbackNotice.getEndReason());
            task.setTalkTimes(callbackNotice.getTalkTimes());
            task.setScore(StringUtils.toInteger(callbackNotice.getScore()));
            task.setLabelling(callbackNotice.getLabelling());
            task.setStatus(callbackNotice.getStatus());
            if (callbackNotice.getStatus().equalsIgnoreCase(VoiceStatus.A.code) || callbackNotice.getStatus().equalsIgnoreCase(VoiceStatus.B.code)) {
                task.setTaskResult(OrderVoiceTask.TaskResult.OK.code);
            } else {
                task.setTaskResult(OrderVoiceTask.TaskResult.FAIL.code);
            }
            task.setUpdateDate(date.getTime());
            orderVoiceTaskService.update(task);
        } catch (Exception e) {
            log.error("语音回访:更新回访结果失败,msg:{}", e,json);
        }

        //endregion

        //region 业务处理部分

        try {
            //客评
            Order order = orderService.getOrderById(callbackNotice.getOrderId(), callbackNotice.getOrderQuarter(), OrderUtils.OrderDataLevel.DETAIL, true,true,false,true);
            //检查是否可自动客评
            NameValuePair checkResult = orderGradeService.canAutoGrade(order);
            int checkResultValue = StringUtils.toInteger(checkResult.getValue());

            //日志
            OrderProcessLog processLog = new OrderProcessLog();
            Dict status = order.getOrderCondition().getStatus();
            processLog.setStatus(status.getLabel());
            processLog.setStatusValue(Integer.parseInt(status.getValue()));
            processLog.setQuarter(order.getQuarter());
            processLog.setAction("语音回访评价");
            processLog.setOrderId(callbackNotice.getOrderId());
            processLog.setActionComment(
                    MessageFormat.format("语音回访【{0}-{1}】",
                            voiceStatus.name,
                            callbackNotice.getLabelling().replace(";;",";").replace(";", ",")
                    )
            );
            processLog.setStatusFlag(OrderProcessLog.OPL_SF_TRACKING);//跟踪进度
            processLog.setCloseFlag(0);
            // 客评满意
            if (voiceStatus == VoiceStatus.A || voiceStatus == VoiceStatus.B) {
                processLog.setRemarks(processLog.getActionComment());
            }
            processLog.setCreateBy(user);
            processLog.setCreateDate(date);
            processLog.setCustomerId(order.getOrderCondition().getCustomerId());
            processLog.setDataSourceId(order.getDataSourceId());
            orderService.saveOrderProcessLogNew(processLog);

            //2-已客评，已对账
            if (checkResultValue == 2) {
                log.warn("语音回访:不能自动客评-{},msg:{}", checkResult.getName(), json);
                return;
            }

            //转到回访失败列表标记
            Dict orderStatus = order.getOrderCondition().getStatus();
            //语音回访不成功
            if(checkResult.getValue().equals("0") && voiceStatus != VoiceStatus.A && voiceStatus != VoiceStatus.B){
                checkResultValue = 4;//*
                checkResult.setValue("4");
                checkResult.setName(MessageFormat.format("语音回访分类为:{0} ,不能自动客评",voiceStatus.code));
            }

            int orderStatusValue = StringUtils.toInteger(orderStatus.getValue());
            // 回访失败
            // 检查不通过 + 工单状态 + 待回访状态
            if (checkResultValue > 0
                    && orderStatusValue >= Order.ORDER_STATUS_APPROVED
                    && orderStatusValue <= Order.ORDER_STATUS_APP_COMPLETED
                    && order.getOrderCondition().getSubStatus() == Order.ORDER_SUBSTATUS_APPCOMPLETED ) {
                orderGradeService.followUp(CallbackType.VOICE,order.getOrderCondition(),checkResult,user,date);
                return;
            }
            //log.warn("checkResultValue:" + checkResultValue);
            //自动客评
            if (voiceAutoGrade && checkResultValue == 0
                    && (orderStatusValue == Order.ORDER_STATUS_APP_COMPLETED || orderStatusValue == Order.ORDER_STATUS_APP_COMPLETED)
                    && order.getOrderCondition().getSubStatus() == Order.ORDER_SUBSTATUS_APPCOMPLETED ) {
                orderGradeService.autoGradeForCallback(order,user,json, OrderUtils.OrderGradeType.VOICE_GRADE);
            }else{
                log.warn("语音回访:不退回访失败列表，也不自动客评,flag:{} msg:{}",voiceAutoGrade,json);
            }

        } catch (Exception e){
            log.error("语音回访:回调处理失败,msg:{}",json,e);
            try {
                LogUtils.saveLog("语音回访:回调处理失败","Order.CallbackReceiver",json,e,user);
            }catch (Exception ex){}
        }

        //endregion
    }

}
