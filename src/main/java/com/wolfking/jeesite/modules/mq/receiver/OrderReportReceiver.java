package com.wolfking.jeesite.modules.mq.receiver;

import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.entity.rpt.common.RPTOrderProcessTypeEnum;
import com.rabbitmq.client.Channel;
import com.wolfking.jeesite.modules.mq.dto.MQOrderReport;
import com.wolfking.jeesite.modules.mq.entity.RPTOrderProcessModel;
import com.wolfking.jeesite.modules.mq.service.RPTOrderProcessService;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 订单报表统计消息消费者
 * Created by Ryan on 2017/7/27.
 */
@Component
@Slf4j
public class OrderReportReceiver implements ChannelAwareMessageListener {

    @Autowired
    private OrderService orderService;

    @Autowired
    private RPTOrderProcessService orderProcessService;

    @Override
    public void onMessage(org.springframework.amqp.core.Message message, Channel channel) throws Exception {

        //成功
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

        MQOrderReport.OrderReport myMessage = MQOrderReport.OrderReport.parseFrom(message.getBody());

        if(myMessage == null){
            //消息内容为空,丢弃
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }
        //成功
        try{
            // 业务业绩报表,客评后触发
            if(myMessage.getOrderType() == Order.ORDER_STATUS_COMPLETED) {
                Order order = orderService.getOrderById(myMessage.getOrderId(), "", OrderUtils.OrderDataLevel.CONDITION, false);
                if (order != null) {
                    //region 报表微服务消息队列
                    try {
                        RPTOrderProcessModel rptOrderProcessModel = new RPTOrderProcessModel();
                        rptOrderProcessModel.setOrderId(order.getId());
                        rptOrderProcessModel.setProcessType(RPTOrderProcessTypeEnum.GRADE.getValue());
                        rptOrderProcessModel.setQuarter(order.getQuarter());
                        orderProcessService.sendRPTOrderProcess(rptOrderProcessModel);
                    }catch (Exception e){
                        log.error("客评发送报表消息[sendRPTOrderProcess]失败,msg:{}",new JsonFormat().printToString(myMessage),e);
                    }
                    //endregion
                } else {
                    LogUtils.saveLog("报表消息处理异常", "MQOrderReportService", new JsonFormat().printToString(myMessage), null, null);
                }
            }

        }catch (Exception e){
            try {
                LogUtils.saveLog("报表消息处理异常", "MQOrderReportService", new JsonFormat().printToString(myMessage), e, null);
            } catch (Exception ex){
                log.error("报表消息处理异常[MQOrderReportService] msg:{}",new JsonFormat().printToString(myMessage),ex);
                //LogUtils.saveLog("报表消息处理异常", "MQOrderReportService", model.getOrderType().toString(), e, null);
            }
            //log.error("MQ:保存订单统计报表记录失败,",e);
        }

    }
}
