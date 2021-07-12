package com.wolfking.jeesite.modules.mq.service.servicepoint;

import com.google.common.collect.Maps;
import com.googlecode.protobuf.format.JsonFormat;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.mq.dto.MQOrderServicePointMessage;
import com.wolfking.jeesite.modules.sd.service.OrderServicePointService;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.Map;

/**
 * 只变更订单状态及subStatus情况
 * 1.退单申请
 * 2.退单确认
 * 3.工单取消
 * 4.APP完成
 * 5.回访失败
 * @autor Ryan Lu
 * @date 2019/3/23 12:08
 */
@Slf4j
public class OrderStatusUpdateExecutor extends ServicePointExecutor {

    public OrderStatusUpdateExecutor(OrderServicePointService service) {
        this.service = service;
    }

    // 处理方法，更新数据
    public void process(MQOrderServicePointMessage.ServicePointMessage message) {

        if (!checkBaseParameters(message)) {
            return;
        }

        Map<String, Object> params = Maps.newHashMapWithExpectedSize(15);
        params.put("orderId", message.getOrderId());
        params.put("quarter", message.getQuarter());
        if(message.getServicePointInfo() != null && message.getServicePointInfo().getServicePointId()>0) {
            params.put("servicePointId", message.getServicePointInfo().getServicePointId());
        }
        int status = 0;
        if(message.getOrderInfo() != null && message.getOrderInfo().getStatus()>=0) {
            status = message.getOrderInfo().getStatus();
            params.put("status", status);
        }
        int subStatus = 0;
        if(message.getSubStatus() >= 0){
            subStatus = message.getSubStatus();
            params.put("subStatus", subStatus);
        }
        if(message.getResetAppointmentDate() == 1){//重置
            params.put("resetAppointmentDate", message.getResetAppointmentDate());
        }else if(message.getAppointmentDate()>0){
            params.put("appointmentDate", DateUtils.longToDate(message.getAppointmentDate()));
            params.put("appointmentAt",message.getAppointmentDate());
        }
        if(message.getReservationDate()>0){
            params.put("reservationDate", DateUtils.longToDate(message.getReservationDate()));
            params.put("reservationAt", message.getReservationDate());
        }
        if(message.getPendingType() >=0){
            params.put("pendingType", message.getPendingType());
        }
        params.put("updateBy", message.getOperationBy());
        Date updateDate = DateUtils.longToDate(message.getOperationAt());
        params.put("updateDate", updateDate);
        this.service.updateData(params);

        //非当前网点
        if (message.getServicePointInfo() != null && message.getServicePointInfo().getServicePointId() > 0
                && (status > 0 || subStatus > 0)) {
            params.clear();
            params.put("orderId", message.getOrderId());
            params.put("quarter", message.getQuarter());
            params.put("exceptServicePointId", message.getServicePointInfo().getServicePointId());
            if(message.getPendingType() >=0){
                params.put("pendingType", message.getPendingType());
            }
            if (subStatus > 0) {
                params.put("subStatus", subStatus);
            }
            if (status > 0) {
                params.put("status", status);
            }
            params.put("updateBy", message.getOperationBy());
            params.put("updateDate", updateDate);
            this.service.updateNotActiveServiePoint(params);
        }
    }
}
