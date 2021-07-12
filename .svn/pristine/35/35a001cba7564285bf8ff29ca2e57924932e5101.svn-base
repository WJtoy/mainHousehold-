package com.wolfking.jeesite.modules.mq.service.servicepoint;

import com.google.common.collect.Maps;
import com.googlecode.protobuf.format.JsonFormat;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.mq.dto.MQOrderServicePointMessage;
import com.wolfking.jeesite.modules.sd.service.OrderServicePointService;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.Map;

/**
 * 网点改派师傅
 * @autor Ryan Lu
 * @date 2019/3/23 11:25
 */
@Slf4j
public class ChangEngineerExecutor extends ServicePointExecutor {

    public ChangEngineerExecutor(OrderServicePointService service) {
        this.service = service;
    }

    // 处理方法，更新数据
    public void process(MQOrderServicePointMessage.ServicePointMessage message){

        if(!checkBaseParameters(message)){
            return;
        }

        if(message.getServicePointInfo() == null || message.getServicePointInfo().getEngineerId() <=0
                || message.getServicePointInfo().getServicePointId() <= 0 || message.getOperationBy() <= 0
                || message.getOperationAt() <= 0) {
            String json = new JsonFormat().printToString(message);
            log.error("参数缺失，body:{}",json);
            //无更新内容
            return;
        }
        Map<String,Object> params = Maps.newHashMapWithExpectedSize(10);
        params.put("orderId",message.getOrderId());
        params.put("quarter",message.getQuarter());
        params.put("servicePointId",message.getServicePointInfo().getServicePointId());
        params.put("engineerId",message.getServicePointInfo().getEngineerId());
        params.put("masterFlag",message.getMasterFlag());
        params.put("updateBy",message.getOperationBy());
        params.put("updateDate",DateUtils.longToDate(message.getOperationAt()));
        this.service.updateData(params);
    }
}
