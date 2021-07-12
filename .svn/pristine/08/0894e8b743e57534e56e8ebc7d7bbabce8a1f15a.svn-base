package com.wolfking.jeesite.modules.mq.service.servicepoint;

import com.google.common.collect.Maps;
import com.googlecode.protobuf.format.JsonFormat;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.mq.dto.MQOrderServicePointMessage;
import com.wolfking.jeesite.modules.mq.entity.mapper.OrderServicePointMessageMapper;
import com.wolfking.jeesite.modules.sd.entity.OrderServicePoint;
import com.wolfking.jeesite.modules.sd.service.OrderServicePointService;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;

import java.util.Date;
import java.util.Map;

/**
 * 确认上门服务 与添加上门服务逻辑类似
 * 1.判断网点是否已上门，如未上门则作以下变更
 *  1.1.service_flag = 1
 *  1.2.status
 *  1.3.sub_status
 *  1.4.reservation_at
 * @autor Ryan Lu
 * @date 2019/3/23 11:41
 */
@Slf4j
public class ConfirmOnSiteServiceExecutor extends ServicePointExecutor {

    private OrderServicePointMessageMapper messageMapper;

    public ConfirmOnSiteServiceExecutor(OrderServicePointService service) {
        this.service = service;
        this.messageMapper = Mappers.getMapper(OrderServicePointMessageMapper.class);
    }

    // 处理方法，更新数据
    public void process(MQOrderServicePointMessage.ServicePointMessage message){

        if(!checkBaseParameters(message)){
            return;
        }
        /*
        if(message.getServicePointInfo() == null || message.getServicePointInfo().getServicePointId() <= 0){
            String json = new JsonFormat().printToString(message);
            log.error("参数缺失，body:{}",json);
            return;
        }*/
        /*
        OrderServicePoint point = this.service.findByOrderAndServicePoint(message.getOrderId(),message.getQuarter(),message.getServicePointInfo().getServicePointId());
        if(point == null){
            log.error("无网点订单信息,orderId:{},servicePointId:{}",message.getOrderId(),message.getServicePointInfo().getServicePointId());
            return;
        }
        Date updateDate = DateUtils.longToDate(message.getOperationAt());
        if(point.getServiceFlag() == 0) {
         */
        Date updateDate = DateUtils.longToDate(message.getOperationAt());
        Map<String, Object> params = Maps.newHashMapWithExpectedSize(12);
        params.put("orderId", message.getOrderId());
        params.put("quarter", message.getQuarter());
        params.put("servicePointId", message.getServicePointInfo().getServicePointId());
        if(message.getOrderInfo() != null && message.getOrderInfo().getStatus()>0){
            params.put("status",message.getOrderInfo().getStatus());
        }
        if(message.getSubStatus()>0) {
            params.put("subStatus", message.getSubStatus());
        }
        params.put("serviceFlag", 1);
        params.put("pendingType",message.getPendingType());
        params.put("delFlag", 0);//有效
        params.put("reservationDate", updateDate);
        params.put("reservationAt", message.getOperationAt());
        params.put("updateBy", message.getOperationBy());
        params.put("updateDate", updateDate);
        this.service.updateData(params);

        //非当前网点
        params.clear();
        params.put("orderId", message.getOrderId());
        params.put("quarter", message.getQuarter());
        params.put("exceptServicePointId", message.getServicePointInfo().getServicePointId());
        params.put("pendingType",message.getPendingType());
        if(message.getSubStatus()>0){
            params.put("subStatus",message.getSubStatus());
        }
        if(message.getOrderInfo()!=null && message.getOrderInfo().getStatus()>0){
            params.put("status",message.getOrderInfo().getStatus());
        }
        params.put("updateBy", message.getOperationBy());
        params.put("updateDate", updateDate);
        this.service.updateNotActiveServiePoint(params);

        try {
            this.service.updateServiceFlagOfOrderPlan(message.getOrderId(), message.getQuarter(),
                    message.getServicePointInfo().getServicePointId(), message.getServicePointInfo().getEngineerId(),
                    1,
                    message.getOperationBy(), updateDate
            );
        } catch (Exception e) {
            log.error("确认上门更新安维派单serviceFlag错误 orderId:{} ,servicePoint:{} ,engineer:{} ,update:{}",
                    message.getOrderId(),
                    message.getServicePointInfo().getServicePointId(),
                    message.getServicePointInfo().getEngineerId(),
                    message.getOperationAt()
                    , e);
        }
    }
}
