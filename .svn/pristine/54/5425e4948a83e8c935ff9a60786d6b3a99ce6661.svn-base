package com.wolfking.jeesite.modules.mq.receiver.sms;

import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.entity.voiceservice.CallbackType;
import com.kkl.kklplus.entity.voiceservice.mq.MQSmsCallbackMessage;
import com.rabbitmq.client.Channel;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.md.service.CustomerService;
import com.wolfking.jeesite.modules.md.service.GradeService;
import com.wolfking.jeesite.modules.md.service.ServiceTypeService;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.entity.OrderProcessLog;
import com.wolfking.jeesite.modules.sd.service.OrderGradeService;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.modules.sd.service.OrderVoiceTaskService;
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
import java.util.Date;

/**
 * 短信回访消费者
 * @author Ryan
 * @date 2019/02/26
 */
@Slf4j
@Configuration
@Component
public class SmsCallbackReceiver implements ChannelAwareMessageListener {

    @Autowired
    private SmsBusinessService businessService;

    @Value("${site.code}")
    private String siteCode;

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        //先确认
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

        MQSmsCallbackMessage.SmsCallbackEntity callbackEntity = MQSmsCallbackMessage.SmsCallbackEntity.parseFrom(message.getBody());
        if(callbackEntity == null) {
            return;
        }
        // 消息处理
        businessService.autoGradeAction(callbackEntity);
    }



}
