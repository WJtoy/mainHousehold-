package com.wolfking.jeesite.modules.mq.service;

import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.utils.SequenceIdUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.service.SequenceIdService;
import com.wolfking.jeesite.modules.mq.dto.MQOrderServicePointMessage;
import com.wolfking.jeesite.modules.mq.sender.OrderServicePointMessageSender;
import com.wolfking.jeesite.modules.mq.service.servicepoint.ServicePointExecutor;
import com.wolfking.jeesite.modules.mq.service.servicepoint.ServicePointExecutorFactory;
import com.wolfking.jeesite.modules.sd.service.OrderServicePointService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 网点订单业务实现层
 */
@Service
@Slf4j
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class ServicePointOrderBusinessService {

    @Autowired
    private OrderServicePointMessageSender sender;
    @Autowired
    private OrderServicePointService service;
    /*
    private int workerId = ThreadLocalRandom.current().nextInt(32);
    private int datacenterId = ThreadLocalRandom.current().nextInt(32);
    private SequenceIdUtils sequenceIdUtils = new SequenceIdUtils(workerId,datacenterId);
    */
    @Autowired
    private SequenceIdService sequenceIdService;
    //region 消费方法

    /**
     * 处理收到的消息
     * @param message
     */
    public void processMessage(MQOrderServicePointMessage.ServicePointMessage message){
        if(message == null){
            log.error("message is null");
            return;
        }
        ServicePointExecutor executor = ServicePointExecutorFactory.getExecutor(message.getOperationType(),service);
        if(executor == null){
            String json = new JsonFormat().printToString(message);
            log.error("OperationType valid,body:{}",json);
            return;
        }
        //try{
            executor.process(message);
        //}catch (Exception e){
        //    String json = new JsonFormat().printToString(message);
        //    log.error("OperationType valid,body:{}",json);
        //    return;
        //}
    }

    //endregion

    //region 生产方法

    /**
     * 派单/抢单
     * @param builder
     */
    public void planOrder(MQOrderServicePointMessage.ServicePointMessage.Builder builder){
        MQOrderServicePointMessage.ServicePointMessage message = builder
                .setId(sequenceIdService.nextId())
                .setOperationType(MQOrderServicePointMessage.OperationType.Create)
                .build();
        sender.sendRetry(message,0,0);
    }

    /**
     * 修改用户信息
     */
    public void updateUserInfo(long orderId,String quarter,String phone,String address,long updateBy,long updateAt){
        /* comment at 2020-08-08
        MQOrderServicePointMessage.ServicePointMessage message = MQOrderServicePointMessage.ServicePointMessage.newBuilder()
                .setOperationType(MQOrderServicePointMessage.OperationType.UpdateServiceInfo)
                .setOrderId(orderId)
                .setQuarter(quarter)
                .setOperationAt(updateAt)
                .setOperationBy(updateBy)
                .setUserInfo(MQOrderServicePointMessage.UserInfo.newBuilder()
                        .setPhone(phone)
                        .setAddress(address)
                        .build())
                .build();
        sender.sendRetry(message,0,0);
         */
    }

    /**
     * 网点派单/App
     */
    public void changeEngineer(long orderId,String quarter,long servicePointId,long engineerId,int masterFlag, long updateBy,long updateAt){
        MQOrderServicePointMessage.ServicePointMessage message = MQOrderServicePointMessage.ServicePointMessage.newBuilder()
                .setOperationType(MQOrderServicePointMessage.OperationType.ChangeEngineer)
                .setOrderId(orderId)
                .setQuarter(quarter)
                .setOperationAt(updateAt)
                .setOperationBy(updateBy)
                .setMasterFlag(masterFlag)
                .setServicePointInfo(MQOrderServicePointMessage.ServicePointInfo.newBuilder()
                        .setServicePointId(servicePointId)
                        .setEngineerId(engineerId)
                        .build())
                .build();
        sender.sendRetry(message,0,0);
    }

    /**
     * 预约时间
     * @param orderId
     * @param quarter
     * @param servicePointId  网点id，用于判断：已派单的才更新
     * @param subStatus
     * @param pendingType   停滞原因
     * @param appointmentDate
     * @param reservationDate
     * @param appAbnormalyFlag  工单异常标记
     * @param updateBy
     * @param updateAt
     */
    public void pending(long orderId,String quarter,long servicePointId,int subStatus,int pendingType,long appointmentDate,long reservationDate,int appAbnormalyFlag,long updateBy,long updateAt){
        /* comment at 2020-08-08
        //未派单，不需更新
        if(servicePointId <= 0){
            return;
        }
        MQOrderServicePointMessage.ServicePointMessage message = MQOrderServicePointMessage.ServicePointMessage.newBuilder()
                .setOperationType(MQOrderServicePointMessage.OperationType.Pending)
                .setOrderId(orderId)
                .setQuarter(quarter)
                .setOperationAt(updateAt)
                .setOperationBy(updateBy)
                .setPendingType(pendingType)
                .setAppointmentDate(appointmentDate)
                .setReservationDate(reservationDate)
                .setSubStatus(subStatus)
                .setAbnormalyFlag(appAbnormalyFlag)
                .setServicePointInfo(MQOrderServicePointMessage.ServicePointInfo.newBuilder()
                        .setServicePointId(servicePointId)
                        .build())
                .build();
        sender.sendRetry(message,0,0);
         */
    }

    /**
     * 上门服务
     * 2019/09/03 RyanLu
     * 新增对sd_order_plan.service_flag处理
     */
    public void onSiteService(long orderId,String quarter,long servicePointId,Long engineerId,Integer status,Integer subStatus,long updateBy,long updateAt){
        MQOrderServicePointMessage.ServicePointMessage.Builder builder = MQOrderServicePointMessage.ServicePointMessage.newBuilder()
                .setOperationType(MQOrderServicePointMessage.OperationType.OnSiteService)
                .setOrderId(orderId)
                .setQuarter(quarter)
                .setOperationAt(updateAt)
                .setOperationBy(updateBy)
                .setReservationDate(0)
                .setPendingType(0)
                .setServicePointInfo(MQOrderServicePointMessage.ServicePointInfo.newBuilder()
                        .setServicePointId(servicePointId)
                        .setEngineerId(engineerId==null?0L:engineerId) //删除的上门服务记录中的安维人员id 2019/09/03
                        .build());
        if(status != null){
            builder.setOrderInfo(MQOrderServicePointMessage.OrderInfo.newBuilder()
                    .setStatus(status).build());
        }
        if(subStatus != null){
            builder.setSubStatus(subStatus);
        }
        MQOrderServicePointMessage.ServicePointMessage message = builder.build();
        sender.sendRetry(message,0,0);
    }

    /**
     * 确认上门
     * 2019/09/03 RyanLu
     * 新增对sd_order_plan.service_flag处理
     */
    public void confirmOnSiteService(long orderId,String quarter,Long servicePointId,Long engineerId,Integer status,int subStatus,long updateBy,long updateAt){
        MQOrderServicePointMessage.ServicePointMessage.Builder builder = MQOrderServicePointMessage.ServicePointMessage.newBuilder()
                .setOperationType(MQOrderServicePointMessage.OperationType.ConfirmOnSiteService)
                .setOrderId(orderId)
                .setQuarter(quarter)
                .setOperationAt(updateAt)
                .setOperationBy(updateBy)
                .setServicePointInfo(MQOrderServicePointMessage.ServicePointInfo.newBuilder()
                        .setServicePointId(servicePointId)
                        .setEngineerId(engineerId==null?0L:engineerId) //删除的上门服务记录中的安维人员id 2019/09/03
                        .build())
                .setReservationDate(updateAt)
                .setPendingType(0)
                .setSubStatus(subStatus);
        if(status != null){
            builder.setOrderInfo(MQOrderServicePointMessage.OrderInfo.newBuilder()
                    .setStatus(status)
                    .build());
        }
        MQOrderServicePointMessage.ServicePointMessage message = builder.build();
        sender.sendRetry(message,0,0);
    }


    /**
     * 删除上门服务
     * 删除后
     *      如改网点再无其他上门服务项，则变更serviceFlag=0
     *      如删除的服务不是当前负责的网点，记录标记为时效
     * 2019/09/04 RyanLu
     * 新增对sd_order_plan.service_flag处理，
     * 根据updateEngineer判断，1:更新为0
     */
    public void delOnSiteService(long id,int updateServicePoint,int updateEngineer,long orderId,String quarter,long servicePointId,Long currentPointId,Long engineerId,long updateBy,long updateAt){
        MQOrderServicePointMessage.ServicePointMessage message = MQOrderServicePointMessage.ServicePointMessage.newBuilder()
                .setOperationType(MQOrderServicePointMessage.OperationType.DelOnSiteService)
                .setId(id)
                .setUpdateServicePoint(updateServicePoint)
                .setUpdateEngineer(updateEngineer)
                .setOrderId(orderId)
                .setQuarter(quarter)
                .setOperationAt(updateAt)
                .setOperationBy(updateBy)
                .setServicePointInfo(MQOrderServicePointMessage.ServicePointInfo.newBuilder()
                        .setServicePointId(servicePointId)
                        .setPrevServicePointId(currentPointId)//订单当前处理网点id
                        .setEngineerId(engineerId==null?0L:engineerId) //删除的上门服务记录中的安维人员id 2019/09/03
                        .build())
                .build();
        sender.sendRetry(message,0,0);
    }

    /**
     * 回退到已接单
     */
    public void goBackToAccept(long orderId,String quarter,int status,int subStatus,long updateBy,long updateAt){
        MQOrderServicePointMessage.ServicePointMessage message = MQOrderServicePointMessage.ServicePointMessage.newBuilder()
                .setOperationType(MQOrderServicePointMessage.OperationType.GoBackToAccept)
                .setOrderId(orderId)
                .setQuarter(quarter)
                .setSubStatus(subStatus)
                .setOrderInfo(MQOrderServicePointMessage.OrderInfo.newBuilder()
                        .setStatus(status)
                        .build())
                .setPendingType(0)
                .setResetAppointmentDate(1)
                .setOperationAt(updateAt)
                .setOperationBy(updateBy)
                .build();
        sender.sendRetry(message,0,0);
    }

    /**
     * 客评
     */
    public void orderGrade(long orderId,String quarter,int status,int subStatus,long updateBy,long updateAt){
        /* comment at 2020-08-08
        MQOrderServicePointMessage.ServicePointMessage message = MQOrderServicePointMessage.ServicePointMessage.newBuilder()
                .setOperationType(MQOrderServicePointMessage.OperationType.Grade)
                .setOrderId(orderId)
                .setQuarter(quarter)
                .setOperationAt(updateAt)
                .setOperationBy(updateBy)
                .setSubStatus(subStatus)
                .setCloseDate(updateAt)
                .setPendingType(0)
                .setOrderInfo(MQOrderServicePointMessage.OrderInfo.newBuilder()
                        .setStatus(status)
                        .build())
                .build();
        sender.sendRetry(message,0,0);
        */
    }

    /**
     * 对账
     */
    public void orderCharge(long orderId,String quarter,int status, int subStatus, long updateBy,long updateAt){
        /*
        MQOrderServicePointMessage.ServicePointMessage message = MQOrderServicePointMessage.ServicePointMessage.newBuilder()
                .setOperationType(MQOrderServicePointMessage.OperationType.Charge)
                .setOrderId(orderId)
                .setQuarter(quarter)
                .setOperationAt(updateAt)
                .setOperationBy(updateBy)
                .setSubStatus(subStatus)
                .setChargeDate(updateAt)
                .setOrderInfo(MQOrderServicePointMessage.OrderInfo.newBuilder()
                        .setStatus(status)//order.ORDER_STATUS_CHARGED
                        .build())
                .build();
        sender.sendRetry(message,0,0);
        */
    }


    /**
     * 订单状态变更或子状态变更
     * 1.退单申请
     * 2.退单确认
     * 3.工单取消
     * 4.APP完成
     * 5.回访失败
     */
    public void orderStatusUpdate(int operationTypeValue,Long orderId,String quarter,Long servicePointId,
                                  Integer status,Integer subStatus, int pendintType,
                                  boolean resetAppointmentDate,Long reservationDate,Long updateBy,Long updateAt){
        /* comment at 2020-08-08
        //pendingType: -1 ，不变更
        //resetAppointmentDate：true,重置为null,此时reservationDate 无效
        //subStatus:null or <0 ，不变更
        //status:null or <0 ,不变更
        //无网点信息，不需更新[x]
        //退单申请，退单，取消都可无网点信息 2020-03-28
        if(orderId == null || orderId <=0 || StringUtils.isBlank(quarter)){
            return;
        }
        MQOrderServicePointMessage.OperationType operationType = MQOrderServicePointMessage.OperationType.forNumber(operationTypeValue);
        if(operationType == null){
            log.error("更新类型错误:",operationTypeValue);
            return;
        }
        MQOrderServicePointMessage.ServicePointMessage.Builder builder = MQOrderServicePointMessage.ServicePointMessage.newBuilder()
                .setOperationType(operationType)
                .setOrderId(orderId)
                .setQuarter(quarter)
                .setOperationAt(updateAt)
                .setOperationBy(updateBy)
                .setPendingType(pendintType)
                .setResetAppointmentDate(resetAppointmentDate==true?1:0);
        if(reservationDate != null && reservationDate.longValue()>0){
            builder.setReservationDate(reservationDate);
        }
        if(servicePointId != null){
            builder.setServicePointInfo(MQOrderServicePointMessage.ServicePointInfo.newBuilder()
                    .setServicePointId(servicePointId)
                    .build()
            );
        }
        builder.setSubStatus(subStatus==null?-1:subStatus);
        builder.setOrderInfo(MQOrderServicePointMessage.OrderInfo.newBuilder()
                .setStatus(status==null?-1:status)
                .build());

        MQOrderServicePointMessage.ServicePointMessage message = builder.build();
        sender.sendRetry(message,0,0);
         */
    }

    /**
     * app完工
     * @param orderId
     * @param quarter
     * @param subStatus 子状态
     * @param appCompleteType 完工类型(字符)
     * @param abnormalyFlag 工单标记异常标志
     * @param updateBy
     * @param updateAt
     */
    public void appComplete(long orderId,String quarter,Integer subStatus,String appCompleteType,int abnormalyFlag,long updateBy,long updateAt){
        /* comment at 2020-08-08
        MQOrderServicePointMessage.ServicePointMessage message = MQOrderServicePointMessage.ServicePointMessage.newBuilder()
                .setOperationType(MQOrderServicePointMessage.OperationType.OrderAppComplete)
                .setOrderId(orderId)
                .setQuarter(quarter)
                .setSubStatus(subStatus)
                .setAppCompleteType(appCompleteType) //app完工类型
                .setAbnormalyFlag(abnormalyFlag) //异常标记
                .setOperationAt(updateAt)
                .setOperationBy(updateBy)
                .build();
        sender.sendRetry(message,0,0);
         */
    }

    /**
     * 工单标记/取消异常
     * @param orderId
     * @param quarter
     * @param updateBy
     * @param updateAt
     */
    public void abnormalyFlag(long orderId,String quarter,Long servicePointId,int abnormalyFlag,long updateBy,long updateAt){
        /* comment at 2020-08-08
        MQOrderServicePointMessage.ServicePointMessage message = MQOrderServicePointMessage.ServicePointMessage.newBuilder()
                .setOperationType(MQOrderServicePointMessage.OperationType.AbnormalyFlag)
                .setOrderId(orderId)
                .setQuarter(quarter)
                .setServicePointInfo(MQOrderServicePointMessage.ServicePointInfo.newBuilder()
                        .setServicePointId(servicePointId==null? 0 : servicePointId)
                        .build())
                .setAbnormalyFlag(abnormalyFlag) //异常标记
                .setOperationAt(updateAt)
                .setOperationBy(updateBy)
                .build();
        sender.sendRetry(message,0,0);
        */
    }

    /**
     * 工单关联单据处理
     * 包含加急，催单，投诉单等等
     * @param orderId
     * @param quarter
     * @param reminderStatus 催单状态
     * @param complainFlag 投诉
     * @param urgentLevelId 加急
     * @param updateBy
     * @param updateAt
     */
    public void relatedForm(long orderId,String quarter,
                            Integer reminderStatus,Integer complainFlag,Integer urgentLevelId,
                            long updateBy,long updateAt){
        /* comment at 2020-08-08
        MQOrderServicePointMessage.ServicePointMessage message = MQOrderServicePointMessage.ServicePointMessage.newBuilder()
                .setOperationType(MQOrderServicePointMessage.OperationType.RelatedForm)
                .setOrderId(orderId)
                .setQuarter(quarter)
                .setReminderFlag(Objects.isNull(reminderStatus)?0:reminderStatus.intValue())
                .setComplainFlag(Objects.isNull(complainFlag)?0:complainFlag.intValue())
                .setUrgentLevelId(Objects.isNull(urgentLevelId)?0:urgentLevelId.intValue())
                .setOperationAt(updateAt)
                .setOperationBy(updateBy)
                .build();
        sender.sendRetry(message,0,0);
        */
    }

    /**
     * 更新好评单状态
     * @param orderId
     * @param quarter
     * @param servicePointId    网点
     * @param praiseStatus      好评单状态
     * @param updateBy
     * @param updateAt
     */
    public void syncPraiseStatus(long orderId,String quarter,Long servicePointId,Integer praiseStatus,
                                    long updateBy,long updateAt){
        /* comment at 2020-08-08
        MQOrderServicePointMessage.ServicePointMessage message = MQOrderServicePointMessage.ServicePointMessage.newBuilder()
                .setOperationType(MQOrderServicePointMessage.OperationType.PraiseForm)
                .setOrderId(orderId)
                .setQuarter(quarter)
                .setServicePointInfo(MQOrderServicePointMessage.ServicePointInfo.newBuilder()
                        .setServicePointId(servicePointId==null? 0 : servicePointId)
                        .build())
                .setPraiseStatus(praiseStatus)
                .setOperationAt(updateAt)
                .setOperationBy(updateBy)
                .build();
        sender.sendRetry(message,0,0);
        */
    }

    //endregion 生产方法

}
