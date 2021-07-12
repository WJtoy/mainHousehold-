package com.wolfking.jeesite.modules.mq.service.servicepoint;

import com.google.common.collect.Maps;
import com.googlecode.protobuf.format.JsonFormat;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.mq.dto.MQOrderServicePointMessage;
import com.wolfking.jeesite.modules.sd.entity.OrderServicePoint;
import com.wolfking.jeesite.modules.sd.service.OrderServicePointService;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 回退到接单状态
 * 订单无上门情况，才可回退
 * @autor Ryan Lu
 * @date 2019/3/23 22:48
 */
@Slf4j
public class GoBackToAcceptExecutor extends ServicePointExecutor {

    public GoBackToAcceptExecutor(OrderServicePointService service) {
        this.service = service;
    }

    // 处理方法，更新数据
    public void process(MQOrderServicePointMessage.ServicePointMessage message) {

        if (!checkBaseParameters(message)) {
            return;
        }
        /*
        Map<String, Object> params = Maps.newHashMap();
        params.put("orderId", message.getOrderId());
        params.put("quarter", message.getQuarter());
        params.put("isCurrent", 0); //当前网点标记
        params.put("delFlag", 1);
        params.put("serviceFlag", 0);//上门标记
        //params.put("planDate", null);//派单日期null
        if(message.getResetAppointmentDate() == 1) {
            params.put("resetAppointmentDate",1);
        }
        if(message.getPendingType()>=0) {
            params.put("pendingType", message.getPendingType());
        }
        params.put("updateBy", message.getOperationBy());
        params.put("updateDate", DateUtils.longToDate(message.getOperationAt()));
        this.service.updateData(params);
        */
        OrderServicePoint entity = new OrderServicePoint(message.getOrderId(),null,message.getQuarter());
        this.service.goBackToAccept(entity);
    }

}
