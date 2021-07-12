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
 * 关联单据处理
 *  1.加急
 *  2.催单
 *  3.投诉
 * @autor Ryan Lu
 * @date 2020/2/14
 */
@Slf4j
public class RelatedFormExecutor extends ServicePointExecutor {

    private OrderServicePointMessageMapper messageMapper;

    public RelatedFormExecutor(OrderServicePointService service) {
        this.service = service;
        this.messageMapper = Mappers.getMapper(OrderServicePointMessageMapper.class);
    }

    // 处理方法，更新数据
    public void process(MQOrderServicePointMessage.ServicePointMessage message){

        if(!checkBaseParameters(message)){
            return;
        }

        Map<String, Object> params = Maps.newHashMapWithExpectedSize(10);
        //加急
        if(message.getUrgentLevelId()>0){
            params.put("urgentLevelId",message.getUrgentLevelId());
        }
        //催单
        if(message.getReminderFlag()>0){
            int reminderFlag = message.getReminderFlag();
            params.put("reminderFlag", message.getReminderFlag());
            int reminderSort = reminderFlag == 1?2:(reminderFlag>0?1:0);
            params.put("reminderSort",reminderSort);
        }
        //投诉
        if(message.getComplainFlag() == -1){//撤销投诉
            params.put("complainFlag", 0);
        }else if(message.getComplainFlag() > 0){
            params.put("complainFlag", message.getComplainFlag());
        }
        params.put("updateBy", message.getOperationBy());
        params.put("updateDate", DateUtils.longToDate(message.getOperationAt()));
        //where
        params.put("orderId", message.getOrderId());
        params.put("quarter", message.getQuarter());
        //params.put("delFlag", 0);//只更新有效网点
        this.service.updateByOrder(params);

    }
}
