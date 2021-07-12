package com.wolfking.jeesite.modules.mq.service.servicepoint;

import com.google.common.collect.Maps;
import com.googlecode.protobuf.format.JsonFormat;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.mq.dto.MQOrderServicePointMessage;
import com.wolfking.jeesite.modules.sd.service.OrderServicePointService;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 预约时间
 * @autor Ryan Lu
 * @date 2019/3/23 23:03
 */
@Slf4j
public class PendingExecutor extends ServicePointExecutor {

    public PendingExecutor(OrderServicePointService service) {
        this.service = service;
    }

    // 处理方法，更新数据
    public void process(MQOrderServicePointMessage.ServicePointMessage message) {

        if (!checkBaseParameters(message)) {
            return;
        }

        if (message.getReservationDate() <=0) {
            String json = new JsonFormat().printToString(message);
            log.error("无网点操作时间，不能更新，body:{}", json);
            return;
        }

        if (message.getServicePointInfo() == null || message.getServicePointInfo().getServicePointId() <= 0
                || message.getPendingType() <= 0) {
            String json = new JsonFormat().printToString(message);
            log.error("参数缺失，body:{}", json);
            return;
        }

        Map<String, Object> params = Maps.newHashMapWithExpectedSize(15);
        params.put("orderId", message.getOrderId());
        params.put("quarter", message.getQuarter());
        //params.put("servicePointId", message.getServicePointInfo().getServicePointId());
        params.put("subStatus",message.getSubStatus());
        params.put("pendingType",message.getPendingType());
        params.put("reservationDate", DateUtils.longToDate(message.getReservationDate()));
        params.put("reservationAt", message.getReservationDate());
        params.put("appointmentDate", DateUtils.longToDate(message.getAppointmentDate()));
        params.put("appointmentAt", message.getAppointmentDate());
        if(message.getAbnormalyFlag() == 1){
            params.put("abnormalyFlag", message.getAbnormalyFlag());
        }
        params.put("updateBy", message.getOperationBy());
        params.put("updateDate", DateUtils.longToDate(message.getOperationAt()));
        this.service.updateData(params);
    }
}
