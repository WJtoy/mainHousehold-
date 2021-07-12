package com.wolfking.jeesite.modules.mq.service.servicepoint;

import com.google.common.collect.Maps;
import com.kkl.kklplus.entity.praise.PraiseStatusEnum;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.mq.dto.MQOrderServicePointMessage;
import com.wolfking.jeesite.modules.mq.entity.mapper.OrderServicePointMessageMapper;
import com.wolfking.jeesite.modules.sd.service.OrderServicePointService;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;

import java.util.Map;

/**
 * 好评单处理
 * @autor Ryan Lu
 * @date 2020/4/11
 */
@Slf4j
public class PraiseFormExecutor extends ServicePointExecutor {

    //private OrderServicePointMessageMapper messageMapper;

    public PraiseFormExecutor(OrderServicePointService service) {
        this.service = service;
        //this.messageMapper = Mappers.getMapper(OrderServicePointMessageMapper.class);
    }

    // 处理方法，更新数据
    public void process(MQOrderServicePointMessage.ServicePointMessage message){

        if(!checkBaseParameters(message)){
            return;
        }
        int status = message.getPraiseStatus();
        if(status > 0) {
            this.service.updatePriaseStatus(
                    message.getOrderId(),
                    message.getQuarter(),
                    message.getPraiseStatus(),
                    message.getServicePointInfo().getServicePointId(),
                    message.getOperationBy(),
                    message.getOperationAt()
                    );
        }
    }
}
