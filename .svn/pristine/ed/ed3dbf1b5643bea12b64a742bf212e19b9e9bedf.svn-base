package com.wolfking.jeesite.modules.mq.service.servicepoint;

import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.mq.dto.MQOrderServicePointMessage;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.service.OrderServicePointService;
import lombok.extern.slf4j.Slf4j;

/**
 * 网点订单表数据更新执行者(基类)
 * @autor Ryan Lu
 * @date 2019/3/22 6:02 PM
 */
@Slf4j
public abstract class ServicePointExecutor {

    protected OrderServicePointService service;

    // 处理方法，更新数据
    public abstract void process(MQOrderServicePointMessage.ServicePointMessage message);

    protected boolean checkBaseParameters(MQOrderServicePointMessage.ServicePointMessage message){
        if(message == null){
            log.error("消息体null");
            return false;
        }
        String json = new JsonFormat().printToString(message);
        if(message.getOrderId()<=0){
            log.error("无订单id,body:{}",json);
            return false;
        }
        if(StringUtils.isBlank(message.getQuarter())){
            log.error("无数据分片,body:{}",json);
            return false;
        }
        /*退单审核 或取消单 有可能没有网点
        if(message.getOrderInfo() != null &&
                (message.getOrderInfo().getStatus() == Order.ORDER_STATUS_RETURNED || message.getOrderInfo().getStatus() == Order.ORDER_STATUS_CANCELED )
                && ( message.getServicePointInfo() == null || message.getServicePointInfo().getServicePointId() <= 0)
        ){
            return false;
        }*/
        return true;
    }
}
