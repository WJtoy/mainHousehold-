package com.wolfking.jeesite.modules.mq.receiver;

import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2b.mq.B2BMQConstant;
import com.rabbitmq.client.Channel;
import com.wolfking.jeesite.common.utils.GsonUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.mq.dto.MQOrderImportMessage;
import com.wolfking.jeesite.modules.mq.entity.mapper.OrderImportMessageMapper;
import com.wolfking.jeesite.modules.mq.sender.OrderImportMessageSender;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.entity.TempOrder;
import com.wolfking.jeesite.modules.sd.service.OrderImportService;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.b2bcenter.mq.config.B2BCenterServiceMonitorRetryMQConfig;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;

/**
 * 订单客评重试消息消费者
 */
@Slf4j
@Component
public class OrderImportRetryMessageReceiver implements ChannelAwareMessageListener {

    @Autowired
    private RabbitProperties rabbitProperties;

    @Autowired
    private OrderImportService orderImportService;

    @Autowired
    private OrderImportMessageSender orderImportMessageSender;

    @Override
    public void onMessage(Message message, Channel channel) throws IOException {
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        MQOrderImportMessage.OrderImportMessage msg = null;
        long tmpId = 0;
        try {
            msg = MQOrderImportMessage.OrderImportMessage.parseFrom(message.getBody());
            if (msg != null) {
                tmpId = msg.getTmpId()>0?msg.getTmpId():msg.getId();
                MSResponse<Order> msResponse = orderImportService.transferOrder(msg);
                //自动转到失败
                if(!MSResponse.isSuccessCode(msResponse)){
                    if(msResponse.getCode() == OrderImportService.ERRORCODE_DUPLICATE_ENTRY){
                        String json = new JsonFormat().printToString(msg);
                        log.warn("导入单ID重复,json:{}",json);
                        try{
                            orderImportService.retryError(tmpId,msResponse.getMsg(),new User(msg.getCreateById()),new Date(),1);
                        }catch (Exception e){
                            log.error("更新数据库失败,json:{}",json,e);
                        }
                    }else {
                        TempOrder order = null;
                        try {
                            order = Mappers.getMapper(OrderImportMessageMapper.class).mqToModel(msg);
                            order.setErrorMsg(msResponse.getMsg());
                            //if (order.getRetryTimes() == 0) {
                            //    orderImportService.insertTempOrder(order);
                            //} else {
                            //    orderImportService.retryError(tmpId, msResponse.getMsg(), order.getCreateBy(), new Date(),null);
                            //}
                            if (order.getRetryTimes() == 0) {
                                if(msg.getTmpId()>0){
                                    orderImportService.retryError(msg.getTmpId(), msResponse.getMsg(), order.getCreateBy(), new Date(),null);
                                }else {
                                    orderImportService.insertTempOrder(order);
                                }
                            } else {
                                orderImportService.retryError(tmpId, msResponse.getMsg(), order.getCreateBy(), new Date(),null);
                            }
                        } catch (Exception e1) {
                            String json = new String();
                            if (order == null) {
                                json = new JsonFormat().printToString(msg);
                            } else {
                                json = GsonUtils.getInstance().toGson(order);
                            }
                            log.error("保存失败记录错误,errMsg:{} ,json:{}", msResponse.getMsg(), json, e1);
                        }
                    }
                }
            }else{
                log.error("导入订单重试消息接受失败:消息体解析错误");
            }
        }
        catch (Exception e){
            if(msg != null){
                if(StringUtils.contains(e.getLocalizedMessage(),"Duplicate")) {//重复
                    log.error("导入订单消息接受失败，ID重复,message:{}", new JsonFormat().printToString(msg), e);
                    try {
                        orderImportService.retryError(tmpId, "订单已专函，重复提交", new User(msg.getCreateById(), msg.getCreateByName(), ""), new Date(), 1);
                    }catch (Exception oe){
                        log.error("重复提交，更新数据库错误");
                    }
                }else {
                    int times = StringUtils.toInteger(message.getMessageProperties().getHeaders().get(B2BMQConstant.MESSAGE_PROPERTIES_HEADER_KEY_TIMES));
                    if (times < B2BCenterServiceMonitorRetryMQConfig.RETRY_TIMES) {
                        times++;
                        orderImportMessageSender.sendRetry(msg, getDelaySeconds(times), times);
                    } else {
                        TempOrder order = null;
                        try {
                            order = Mappers.getMapper(OrderImportMessageMapper.class).mqToModel(msg);
                            orderImportService.insertTempOrder(order);
                        } catch (Exception e1) {
                            String json = new String();
                            if (order == null) {
                                json = new JsonFormat().printToString(msg);
                            } else {
                                json = GsonUtils.getInstance().toGson(order);
                            }
                            log.error("订单导入重试消息失败，保存失败记录错误:{}", json, e1);
                        }
                    }
                }
            }else{
                log.error("订单导入重试消息消费失败", e);
            }
        }
    }

    private int getDelaySeconds(int times) {
        return (int) (rabbitProperties.getTemplate().getRetry().getInitialInterval() * rabbitProperties.getTemplate().getRetry().getMultiplier() * times);
    }


}
