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
 * 删除上门服务，且网点无其他上门记录才处理
 *  service_flag = 0
 * @autor Ryan Lu
 * @date 2019/3/23 11:41
 *
 * @author Ryan Lu
 * @date 2019/9/5 09:20
 * 消息增加updateServicePoint和updateEngineer
 * updateEngineer =1 时，更新sd_order_plan的service_flag=0
 */
@Slf4j
public class DelOnSiteServiceExecutor extends ServicePointExecutor {

    private OrderServicePointMessageMapper messageMapper;

    public DelOnSiteServiceExecutor(OrderServicePointService service) {
        this.service = service;
        this.messageMapper = Mappers.getMapper(OrderServicePointMessageMapper.class);
    }

    // 处理方法，更新数据
    public void process(MQOrderServicePointMessage.ServicePointMessage message){

        if(!checkBaseParameters(message)){
            return;
        }

        if(message.getServicePointInfo() == null || message.getServicePointInfo().getServicePointId() <= 0){
            String json = new JsonFormat().printToString(message);
            log.error("参数缺失，body:{}",json);
            return;
        }

        Date updateDate = DateUtils.longToDate(message.getOperationAt());
        if(message.getUpdateServicePoint() == 1) {
            Map<String, Object> params = Maps.newHashMapWithExpectedSize(10);
            params.put("orderId", message.getOrderId());
            params.put("quarter", message.getQuarter());
            params.put("servicePointId", message.getServicePointInfo().getServicePointId());
            params.put("serviceFlag", 0);
            //非当前网点，标记为时效
            //prevServicePointId:此时传递的是当前网点id
            if (message.getServicePointInfo().getServicePointId() != message.getServicePointInfo().getPrevServicePointId()) {
                params.put("delFlag", 1);
            }
            params.put("updateBy", message.getOperationBy());
            params.put("updateDate", updateDate);
            this.service.updateData(params);
        }
        //安维派单记录表
        if(message.getUpdateEngineer() == 1) {
            try {
                this.service.updateServiceFlagOfOrderPlan(message.getOrderId(), message.getQuarter(),
                        message.getServicePointInfo().getServicePointId(), message.getServicePointInfo().getEngineerId(),
                        0,
                        message.getOperationBy(), updateDate
                );
            } catch (Exception e) {
                log.error("删除上门服务更新安维派单serviceFlag错误，id:{} ,orderId:{} ,servicePoint:{} ,engineer:{} ,update:{}",
                        message.getId(),
                        message.getOrderId(),
                        message.getServicePointInfo().getServicePointId(),
                        message.getServicePointInfo().getEngineerId(),
                        message.getOperationAt()
                        , e);
            }
        }
    }
}
