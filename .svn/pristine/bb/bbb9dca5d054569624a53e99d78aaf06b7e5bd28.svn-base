package com.wolfking.jeesite.modules.mq.receiver;

import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.common.response.MSResponse;
import com.rabbitmq.client.Channel;
import com.wolfking.jeesite.common.utils.GsonUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.mq.dto.MQOrderImportMessage;
import com.wolfking.jeesite.modules.mq.entity.mapper.OrderImportMessageMapper;
import com.wolfking.jeesite.modules.mq.sender.OrderImportMessageSender;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.entity.TempOrder;
import com.wolfking.jeesite.modules.sd.service.OrderImportService;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.modules.sys.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.stereotype.Component;
import org.springframework.transaction.UnexpectedRollbackException;

import java.io.IOException;
import java.util.Date;

/**
 * 导入订单消息消费者
 * 1.订单检查(包含基本信息、区域)
 * 2.自动保存订单，会分发其他报表消息
 *
 * 处理失败记录在sd_temporder
 */
@Slf4j
@Component
public class OrderImportMessageReceiver implements ChannelAwareMessageListener {

    @Autowired
    private RabbitProperties rabbitProperties;

    @Autowired
    private OrderService orderService;

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
                        log.error("导入单ID重复,json:{}",json);
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
                log.error("导入订单消息接受失败:消息体解析错误");
            }
        }
        catch (UnexpectedRollbackException ur){
            //事务处理失败，delay & retry
            if(StringUtils.contains(ur.getLocalizedMessage(),"Duplicate")) {//重复
                log.error("导入订单消息接受失败，ID重复,message:{}", new JsonFormat().printToString(msg), ur);
                try {
                    orderImportService.retryError(tmpId, "订单已专函，重复提交", new User(msg.getCreateById(), msg.getCreateByName(), ""), new Date(), 1);
                }catch (Exception oe){
                    log.error("重复提交，更新数据库错误");
                }
            }else{
                orderImportMessageSender.sendRetry(msg, getDelaySeconds(),1);
            }
        }
        catch (Exception e){
            if(msg != null){
                orderImportMessageSender.sendRetry(msg, getDelaySeconds(),1);
            }else{
                log.error("导入订单消息接受失败,message:", new JsonFormat().printToString(msg), e);
            }
        }
    }

    private int getDelaySeconds() {
        return (int) (rabbitProperties.getTemplate().getRetry().getInitialInterval() * rabbitProperties.getTemplate().getRetry().getMultiplier());
    }

}
