package com.wolfking.jeesite.modules.mq.service.servicepoint;

import com.google.common.collect.Maps;
import com.googlecode.protobuf.format.JsonFormat;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.mq.dto.MQOrderServicePointMessage;
import com.wolfking.jeesite.modules.mq.entity.mapper.OrderServicePointMessageMapper;
import com.wolfking.jeesite.modules.sd.service.OrderServicePointService;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;

import java.util.Map;

/**
 * 对账
 *  1.charge_date
 *  2.status
 *  3.sub_status
 * @autor Ryan Lu
 * @date 2019/3/23 14:23
 */
@Slf4j
public class ChargeExecutor extends ServicePointExecutor {

    public ChargeExecutor(OrderServicePointService service) {
        this.service = service;
    }

    // 处理方法，更新数据
    public void process(MQOrderServicePointMessage.ServicePointMessage message){

        if(!checkBaseParameters(message)){
            return;
        }

        if (message.getOrderInfo() == null || message.getOrderInfo().getStatus() <= 0) {
            String json = new JsonFormat().printToString(message);
            log.error("status错误，不能更新，body:{}", json);
            return;
        }

        Map<String, Object> params = Maps.newHashMapWithExpectedSize(10);
        params.put("orderId", message.getOrderId());
        params.put("quarter", message.getQuarter());
        params.put("chargeDate", DateUtils.longToDate(message.getChargeDate()));
        params.put("chargeAt", message.getChargeDate());
        params.put("status", message.getOrderInfo().getStatus());
        params.put("subStatus", message.getSubStatus());
        params.put("updateBy", message.getOperationBy());
        params.put("updateDate", DateUtils.longToDate(message.getOperationAt()));
        params.put("delFlag", 0);//只更新有效网点
        this.service.updateByOrder(params);

    }
}
