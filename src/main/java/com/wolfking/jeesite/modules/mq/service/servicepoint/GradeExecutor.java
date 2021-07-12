package com.wolfking.jeesite.modules.mq.service.servicepoint;

import com.google.common.collect.Maps;
import com.googlecode.protobuf.format.JsonFormat;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.mq.dto.MQOrderServicePointMessage;
import com.wolfking.jeesite.modules.mq.entity.mapper.OrderServicePointMessageMapper;
import com.wolfking.jeesite.modules.sd.entity.OrderServicePoint;
import com.wolfking.jeesite.modules.sd.service.OrderServicePointService;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;

import java.util.Map;

/**
 * 客评
 *  1.close_at
 *  2.status
 *  3.sub_status
 * @autor Ryan Lu
 * @date 2019/3/23 14:08
 */
@Slf4j
public class GradeExecutor extends ServicePointExecutor {

    private OrderServicePointMessageMapper messageMapper;

    public GradeExecutor(OrderServicePointService service) {
        this.service = service;
        this.messageMapper = Mappers.getMapper(OrderServicePointMessageMapper.class);
    }

    // 处理方法，更新数据
    public void process(MQOrderServicePointMessage.ServicePointMessage message){

        if(!checkBaseParameters(message)){
            return;
        }

        if ((message.getOrderInfo() == null || message.getOrderInfo().getStatus() <= 0) && message.getSubStatus() <= 0) {
            String json = new JsonFormat().printToString(message);
            log.error("status或subStatus值错误，不能更新，body:{}", json);
            return;
        }

        Map<String, Object> params = Maps.newHashMapWithExpectedSize(10);
        params.put("orderId", message.getOrderId());
        params.put("quarter", message.getQuarter());
        params.put("closeDate", DateUtils.longToDate(message.getCloseDate()));
        params.put("closeAt", message.getCloseDate());
        params.put("status", message.getOrderInfo().getStatus());
        if(message.getPendingType()>=0) {
            params.put("pendingType", message.getPendingType());
        }
        params.put("subStatus", message.getSubStatus());
        params.put("updateBy", message.getOperationBy());
        params.put("updateDate", DateUtils.longToDate(message.getOperationAt()));
        params.put("delFlag", 0);//只更新有效网点
        this.service.updateByOrder(params);

    }
}
