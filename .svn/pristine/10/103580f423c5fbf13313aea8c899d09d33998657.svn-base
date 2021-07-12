package com.wolfking.jeesite.modules.mq.sender;

import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.entity.b2b.mq.B2BMQConstant;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.mq.conf.OrderImportMessageConfig;
import com.wolfking.jeesite.modules.mq.conf.OrderImportRetryMessageConfig;
import com.wolfking.jeesite.modules.mq.dto.MQOrderImportMessage;
import com.wolfking.jeesite.modules.mq.entity.mapper.OrderImportMessageMapper;
import com.wolfking.jeesite.modules.sd.entity.TempOrder;
import com.wolfking.jeesite.modules.sd.service.OrderImportService;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 导入订单消息队列生产者
 */
@Slf4j
@Component
public class OrderImportMessageSender implements RabbitTemplate.ConfirmCallback {

    @Autowired
    private OrderImportService orderImportService;

    private RabbitTemplate rabbitTemplate;

    private RetryTemplate retryTemplate;

    @Autowired
    public OrderImportMessageSender(RabbitTemplate manualRabbitTemplate, RetryTemplate kklRabbitRetryTemplate) {
        this.rabbitTemplate = manualRabbitTemplate;
        this.rabbitTemplate.setConfirmCallback(this);
        this.retryTemplate = kklRabbitRetryTemplate;
    }

    /**
     * 正常发送消息
     *
     * @param message 消息体
     */
    public void send(MQOrderImportMessage.OrderImportMessage message) {
        try {
            retryTemplate.execute((RetryCallback<Object, Exception>) context -> {
                context.setAttribute(B2BMQConstant.RETRY_CONTEXT_ATTRIBUTE_KEY_MESSAGE, message);
                rabbitTemplate.convertAndSend(
                        OrderImportMessageConfig.MQ_ORDER_IMPORT,
                        OrderImportMessageConfig.MQ_ORDER_IMPORT,
                        message.toByteArray(),
                        new CorrelationData());
                return null;
            }, context -> {
                Object msgObj = context.getAttribute(B2BMQConstant.RETRY_CONTEXT_ATTRIBUTE_KEY_MESSAGE);
                MQOrderImportMessage.OrderImportMessage msg = MQOrderImportMessage.OrderImportMessage.parseFrom((byte[]) msgObj);
                Throwable throwable = context.getLastThrowable();
                log.error("normal send error", throwable);

                String msgJson = null;
                try {
                    msgJson = new JsonFormat().printToString(msg);
                    TempOrder order = null;
                    OrderImportMessageMapper mapper = Mappers.getMapper(OrderImportMessageMapper.class);
                    try {
                        order = mapper.mqToModel(message);
                    } catch (Exception se) {
                        log.error("message:{}", msgJson, se);
                    }
                    if (order == null) {
                        order = mapper.manualMqToModel(message);
                    }
                    if (order == null) {
                        log.error("导入订单转换错误");
                    }
                    if (order.getId() == null || order.getId() <= 0) {
                        orderImportService.insertTempOrder(order);
                    } else {
                        orderImportService.retryError(order.getId(), "发送导入单消息失败", order.getCreateBy(), new Date(),null);
                    }
                    if (order.getId() == null || order.getId() <= 0) {
                        orderImportService.insertTempOrder(order);
                    } else {
                        orderImportService.retryError(order.getId(), "发送导入单消息失败", order.getCreateBy(), new Date(),null);
                    }
                }catch (Exception e1){
                    if(StringUtils.isBlank(msgJson)) {
                        log.error("保存sd_temporder表错误,message:{}",message,e1);
                    }else{
                        log.error("保存sd_temporder表错误,json:{}",msgJson,e1);
                    }
                }

                return null;
            });
        } catch (Exception e) {
            log.error("normal send error",e);
            String msgJson = null;
            try {
                msgJson = new JsonFormat().printToString(message);
                TempOrder order = null;
                OrderImportMessageMapper mapper = Mappers.getMapper(OrderImportMessageMapper.class);
                try {
                    order = mapper.mqToModel(message);
                } catch (Exception se) {
                    log.error("message:{}", msgJson, se);
                }
                if (order == null) {
                    order = mapper.manualMqToModel(message);
                }
                if (order == null) {
                    log.error("导入订单转换错误");
                }
                if (order.getId() == null || order.getId() <= 0) {
                    orderImportService.insertTempOrder(order);
                } else {
                    orderImportService.retryError(order.getId(), "发送导入单消息失败", order.getCreateBy(), new Date(),null);
                }
                if (order.getId() == null || order.getId() <= 0) {
                    orderImportService.insertTempOrder(order);
                } else {
                    orderImportService.retryError(order.getId(), "发送导入单消息失败", order.getCreateBy(), new Date(),null);
                }
            }catch (Exception e1){
                if(StringUtils.isBlank(msgJson)) {
                    log.error("保存sd_temporder表错误,message:{}",message,e1);
                }else{
                    log.error("保存sd_temporder表错误,json:{}",msgJson,e1);
                }
            }
        }
    }

    /**
     * 发送重试消息
     *
     * @param message 消息体
     * @param delay 延迟时间，单位：毫秒
     * @param times   第几次发送
     */
    public void sendRetry(MQOrderImportMessage.OrderImportMessage message,int delay, int times) {
        try {
            retryTemplate.execute((RetryCallback<Object, Exception>) context -> {
                context.setAttribute(B2BMQConstant.RETRY_CONTEXT_ATTRIBUTE_KEY_MESSAGE, message);
                rabbitTemplate.convertAndSend(
                        OrderImportRetryMessageConfig.MQ_ORDER_IMPORT_RETRY,
                        OrderImportRetryMessageConfig.MQ_ORDER_IMPORT_RETRY,
                        message.toByteArray(), msg -> {
                            msg.getMessageProperties().setDelay(delay);
                            msg.getMessageProperties().setHeader(B2BMQConstant.MESSAGE_PROPERTIES_HEADER_KEY_TIMES, times);
                            msg.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                            return msg;
                        }, new CorrelationData());
                return null;
            }, context -> {
                Object msgObj = context.getAttribute(B2BMQConstant.RETRY_CONTEXT_ATTRIBUTE_KEY_MESSAGE);
                MQOrderImportMessage.OrderImportMessage msg = MQOrderImportMessage.OrderImportMessage.parseFrom((byte[]) msgObj);
                Throwable throwable = context.getLastThrowable();
                log.error("normal send error", throwable);

                String msgJson = null;
                try {
                    msgJson = new JsonFormat().printToString(msg);
                    TempOrder order = null;
                    OrderImportMessageMapper mapper = Mappers.getMapper(OrderImportMessageMapper.class);
                    try {
                        order = mapper.mqToModel(message);
                    } catch (Exception se) {
                        log.error("message:{}", msgJson, se);
                    }
                    if (order == null) {
                        order = mapper.manualMqToModel(message);
                    }
                    if (order == null) {
                        log.error("导入订单转换错误");
                    }
                    if (order.getId() == null || order.getId() <= 0) {
                        orderImportService.insertTempOrder(order);
                    } else {
                        orderImportService.retryError(order.getId(), "发送导入单消息失败", order.getCreateBy(), new Date(),null);
                    }
                    if (order.getId() == null || order.getId() <= 0) {
                        orderImportService.insertTempOrder(order);
                    } else {
                        orderImportService.retryError(order.getId(), "发送导入单消息失败", order.getCreateBy(), new Date(),null);
                    }
                }catch (Exception e1){
                    if(StringUtils.isBlank(msgJson)) {
                        log.error("保存sd_temporder表错误,message:{}",message,e1);
                    }else{
                        log.error("保存sd_temporder表错误,json:{}",msgJson,e1);
                    }
                }

                return null;
            });
        } catch (Exception e) {
            log.error("normal send error",e);
            String msgJson = null;
            try {
                msgJson = new JsonFormat().printToString(message);
                TempOrder order = null;
                OrderImportMessageMapper mapper = Mappers.getMapper(OrderImportMessageMapper.class);
                try {
                    order = mapper.mqToModel(message);
                } catch (Exception se) {
                    log.error("message:{}", msgJson, se);
                }
                if (order == null) {
                    order = mapper.manualMqToModel(message);
                }
                if (order == null) {
                    log.error("导入订单转换错误");
                }
                if (order.getId() == null || order.getId() <= 0) {
                    orderImportService.insertTempOrder(order);
                } else {
                    orderImportService.retryError(order.getId(), "发送导入单消息失败", order.getCreateBy(), new Date(),null);
                }
                if (order.getId() == null || order.getId() <= 0) {
                    orderImportService.insertTempOrder(order);
                } else {
                    orderImportService.retryError(order.getId(), "发送导入单消息失败", order.getCreateBy(), new Date(),null);
                }
            }catch (Exception e1){
                if(StringUtils.isBlank(msgJson)) {
                    log.error("保存sd_temporder表错误,message:{}",message,e1);
                }else{
                    log.error("保存sd_temporder表错误,json:{}",msgJson,e1);
                }
            }
        }
    }

    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {

    }
}
