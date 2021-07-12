package com.wolfking.jeesite.modules.mq.service.servicepoint;

import com.google.common.collect.Maps;
import com.googlecode.protobuf.format.JsonFormat;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.mq.dto.MQOrderServicePointMessage;
import com.wolfking.jeesite.modules.mq.entity.mapper.OrderServicePointMessageMapper;
import com.wolfking.jeesite.modules.sd.service.OrderServicePointService;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;

import java.util.Map;

/**
 * 工单异常处理（标记或取消异常)
 *  1.停滞
 *  2.app标记异常
 *  3.app完工
 *  4.短信
 *  5.客服异常处理
 *  6.异常单处理(取消异常)
 * @autor Ryan Lu
 * @date 2020/2/14
 */
@Slf4j
public class AbnormalyFlagExecutor extends ServicePointExecutor {

    private OrderServicePointMessageMapper messageMapper;

    public AbnormalyFlagExecutor(OrderServicePointService service) {
        this.service = service;
        this.messageMapper = Mappers.getMapper(OrderServicePointMessageMapper.class);
    }

    // 处理方法，更新数据
    public void process(MQOrderServicePointMessage.ServicePointMessage message){

        if(!checkBaseParameters(message)){
            return;
        }

        Map<String, Object> params = Maps.newHashMapWithExpectedSize(10);
        params.put("orderId", message.getOrderId());
        params.put("quarter", message.getQuarter());
        //app完工
        if(message.getOperationType().equals(MQOrderServicePointMessage.OperationType.OrderAppComplete)){
            params.put("appCompleteType",message.getAppCompleteType());
            //可能不标记异常
            if(message.getAbnormalyFlag() > 0){
                params.put("abnormalyFlag", message.getAbnormalyFlag());
            }
        }else{
            params.put("abnormalyFlag", message.getAbnormalyFlag());
        }
        if(message.getSubStatus()>0){
            params.put("subStatus",message.getSubStatus());
        }
        params.put("updateBy", message.getOperationBy());
        params.put("updateDate", DateUtils.longToDate(message.getOperationAt()));
        params.put("delFlag", 0);//只更新有效网点
        this.service.updateData(params);
    }
}
