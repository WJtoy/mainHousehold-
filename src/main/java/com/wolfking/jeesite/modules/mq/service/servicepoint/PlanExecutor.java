package com.wolfking.jeesite.modules.mq.service.servicepoint;

import com.googlecode.protobuf.format.JsonFormat;
import com.wolfking.jeesite.common.exception.OrderException;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.mq.dto.MQOrderServicePointMessage;
import com.wolfking.jeesite.modules.mq.entity.mapper.OrderServicePointMessageMapper;
import com.wolfking.jeesite.modules.sd.entity.OrderServicePoint;
import com.wolfking.jeesite.modules.sd.service.OrderServicePointService;
import com.wolfking.jeesite.modules.sys.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;

/**
 * 派单/抢单
 * 1.判断前网点是否上门，如未上门，逻辑删除
 * 2.更改前网点为非当前网点
 * 3.判读新网点是否逻辑删除
 *   3.1.是：恢复为有效，更新当前网点标记，更新status,sub_status
 *   3.2.否：新增记录
 * @autor Ryan Lu
 * @date 2019/3/23 00:02
 */
@Slf4j
public class PlanExecutor extends ServicePointExecutor {

    private OrderServicePointMessageMapper messageMapper;

    public PlanExecutor(OrderServicePointService service) {
        this.service = service;
        this.messageMapper = Mappers.getMapper(OrderServicePointMessageMapper.class);
    }

    // 处理方法，更新数据
    public void process(MQOrderServicePointMessage.ServicePointMessage message){

        if(!checkBaseParameters(message)){
            return;
        }
        if(message.getServicePointInfo() == null || message.getServicePointInfo().getServicePointId() <= 0
                || message.getServicePointInfo().getEngineerId() <= 0){
            String json = new JsonFormat().printToString(message);
            log.error("参数缺失，body:{}",json);
            return;
        }

        if(message.getServicePointInfo().getServicePointId() == message.getServicePointInfo().getPrevServicePointId()){
            //改派改当前网点，无效操作
            log.warn("{} 重复派单，重新派单给网点:{}",message.getOrderId(),message.getServicePointInfo().getServicePointId());
            return;
        }
        OrderServicePoint entity = this.messageMapper.mqToModel(message);
        if(entity == null){
            String json = new JsonFormat().printToString(message);
            log.error("消息转换为数据模型失败，body:{}",json);
            throw new OrderException("消息转换为数据模型失败");
        }
        OrderServicePoint prevPoint = null;
        /*prev service point
        if(message.getServicePointInfo().getPrevServicePointId()>0){
            prevPoint = this.service.findByOrderAndServicePoint(entity.getOrderId(),entity.getQuarter(),message.getServicePointInfo().getPrevServicePointId());
            if(prevPoint != null && prevPoint.getServiceFlag() == 0){
                prevPoint.setDelFlag(1);//无上门服务，逻辑删除
                prevPoint.setUpdateBy(entity.getCreateBy());
                prevPoint.setUpdateDate(entity.getCreateDate());
            }
        }*/
        //派单次序
        if(entity.getPlanOrder()==0) {
            int nextPlanOrder = this.service.getNextPlanOrder(entity.getOrderId(), entity.getQuarter());
            entity.setPlanOrder(nextPlanOrder);
        }
        OrderServicePoint point = this.service.findByOrderAndServicePoint(entity.getOrderId(),entity.getQuarter(),entity.getServicePointId());
        // 已派过单
        if(point != null && point.getIsCurrent() == 0){
            // set current
            point.setSubStatus(message.getSubStatus());
            point.getStatus().setValue(String.valueOf(message.getOrderInfo().getStatus()));
            point.setUpdateBy(entity.getCreateBy());
            point.setUpdateDate(entity.getCreateDate());
            if(message.getResetAppointmentDate()==1){ //跨网点派单，新网点要重新预约
                point.setAppointmentDate(null);
                point.setAppointmentAt(0);
            }else if(message.getAppointmentDate()>0){
                point.setAppointmentDate(DateUtils.longToDate(message.getAppointmentDate()));
                point.setAppointmentAt(message.getAppointmentDate());
            }
            if(message.getPendingType()>=0){
                point.setPendingType(message.getPendingType());
            }
            point.setReservationDate(entity.getReservationDate());
            point.setReservationAt(entity.getReservationAt());
            point.setComplainFlag(entity.getComplainFlag());
            point.setAbnormalyFlag(message.getAbnormalyFlag());
            point.setMasterFlag(message.getMasterFlag());
            point.setReminderFlag(message.getReminderFlag());
            point.setUrgentLevelId(message.getUrgentLevelId());
            //point.setAppCompleteType(message.getAppCompleteType());
            // unset current
            this.service.unSetCurrentServicePoint(
                    message.getOrderId(),
                    message.getQuarter(),
                    entity.getServicePointId(),
                    entity.getCreateBy().getId(),
                    entity.getCreateDate(),
                    message.getOrderInfo().getStatus(),
                    message.getSubStatus(),
                    entity.getComplainFlag(),
                    message.getAbnormalyFlag(),
                    message.getReminderFlag(),
                    message.getUrgentLevelId()
            );
            //current
            this.service.setCurrentServicePoint(point);
            return;
        }
        //1.非当前网点处理，一定在新增派单网点前执行
        this.service.unSetCurrentServicePoint(
                message.getOrderId(),
                message.getQuarter(),
                entity.getServicePointId(),
                entity.getCreateBy().getId(),
                entity.getCreateDate(),
                entity.getStatus().getIntValue(),
                entity.getSubStatus(),
                entity.getComplainFlag(),
                entity.getAbnormalyFlag(),
                entity.getReminderFlag(),
                entity.getUrgentLevelId()
        );
        //2.新派单
        this.service.assignNewServicePoint(entity);
        //if(prevPoint != null){
        //    this.service.unSetCurrentServicePoint(prevPoint);
        //}

    }
}
