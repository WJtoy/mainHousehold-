package com.wolfking.jeesite.ms.pdd.sd.service;

import com.google.common.collect.Lists;
import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.entity.b2bcenter.pb.MQB2BOrderStatusUpdateMessage;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderActionEnum;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderStatusEnum;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderTransferResult;
import com.kkl.kklplus.entity.canbo.sd.CanboOrderCompleted;
import com.kkl.kklplus.entity.pdd.sd.B2BPddOrderStatusEnum;
import com.kkl.kklplus.entity.pdd.sd.PddOrderUpdate;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.md.entity.Engineer;
import com.wolfking.jeesite.modules.md.entity.ProductCompletePicItem;
import com.wolfking.jeesite.modules.md.service.ServicePointService;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.entity.OrderItemComplete;
import com.wolfking.jeesite.modules.sd.entity.TwoTuple;
import com.wolfking.jeesite.modules.sd.service.OrderCacheReadService;
import com.wolfking.jeesite.modules.sd.service.OrderItemCompleteService;
import com.wolfking.jeesite.modules.sd.utils.OrderPicUtils;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderProcessEntity;
import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderStatusUpdateReqEntity;
import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderVModel;
import com.wolfking.jeesite.ms.b2bcenter.sd.service.B2BOrderManualBaseService;
import com.wolfking.jeesite.ms.pdd.sd.feign.PddOrderFeign;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;

/**
 * 拼多多订单服务
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class PddOrderService extends B2BOrderManualBaseService {

    @Autowired
    private PddOrderFeign pddOrderFeign;

    @Autowired
    private OrderItemCompleteService orderItemCompleteService;

    @Autowired
    private ServicePointService servicePointService;

    @Autowired
    private OrderCacheReadService orderCacheReadService;

    /**
     * 批量检查工单是否可转换
     */
    public MSResponse checkB2BOrderProcessFlag(List<B2BOrderTransferResult> b2bOrderNos) {
        return pddOrderFeign.checkWorkcardProcessFlag(b2bOrderNos);
    }

    /**
     * 取消工单转换/拒单
     */
    public MSResponse cancelOrderTransition(B2BOrderTransferResult b2BOrderTransferResult) {
        return pddOrderFeign.cancelOrderTransition(b2BOrderTransferResult);
    }

    /**
     * 批量更新工单的安装标识
     */
    public MSResponse updateInstallFlag() {
        return pddOrderFeign.updateInstallFlag();
    }

    /**
     * 批量更新工单的路由标记
     */
    public MSResponse updateOrderRoutingFlagAll() {
        return pddOrderFeign.updateSystemIdAll();
    }

    /**
     * 批量更新工单的路由标记

    public MSResponse updateOrderRoutingFlagAll() {
        return pddOrderFeign.updateSystemIdAll();
    }
     */

    /**
     * 调用B2B工单转换进度更新接口
     */
    public MSResponse sendB2BOrderConversionProgressUpdateCommandToB2B(List<B2BOrderTransferResult> progressList) {
        return pddOrderFeign.updateOrderTransferResult(Lists.newArrayList(progressList));
    }


    /**
     * 预约
     */
    private MSResponse appoint(Long b2bOrderId, String b2bOrderNo, Long appointmentDate,
                                          String remarks, long createById, long createDt) {
        PddOrderUpdate appointUpdate = new PddOrderUpdate();
        appointUpdate.setB2bOrderId(b2bOrderId);
        appointUpdate.setUpdateById(createById);
        appointUpdate.setUpdateDt(createDt);
        appointUpdate.setBizStatus(B2BPddOrderStatusEnum.APPOINT.value)
                .setServOrderSn(b2bOrderNo)
                .setAppointServTime(appointmentDate)
                .setExecuteTime(createDt)
                .setBizDetailDesc(StringUtils.trimToEmpty(remarks));
        return pddOrderFeign.workStatus(appointUpdate);
    }

    /**
     * 派单
     */
    private MSResponse orderPlan(Long b2bOrderId, String b2bOrderNo, String engineerId, String engineerName, String engineerMobile,
                                       long createById, long createDt) {
        PddOrderUpdate appointUpdate = new PddOrderUpdate();
        appointUpdate.setB2bOrderId(b2bOrderId);
        appointUpdate.setStaffName(engineerName);
        appointUpdate.setStaffPhone(engineerMobile);
        appointUpdate.setUpdateById(createById);
        appointUpdate.setUpdateDt(createDt);
        appointUpdate.setBizStatus(B2BPddOrderStatusEnum.PLANNED.value)
                .setServOrderSn(b2bOrderNo)
                .setExecuteTime(createDt)
                .setBizDetailDesc(StringUtils.EMPTY);
        return pddOrderFeign.workStatus(appointUpdate);
    }

    /**
     * 上门服务 -> Pdd:服务签到
     */
    private MSResponse orderService(Long b2bOrderId, String b2bOrderNo, Long serviceDt,long createById, long createDt) {
        PddOrderUpdate appointUpdate = new PddOrderUpdate();
        appointUpdate.setB2bOrderId(b2bOrderId);
        appointUpdate.setUpdateById(createById);
        appointUpdate.setUpdateDt(createDt);
        appointUpdate.setBizStatus(B2BPddOrderStatusEnum.SERVICE.value)
                .setServOrderSn(b2bOrderNo)
                .setExecuteTime(serviceDt)
                .setBizDetailDesc(StringUtils.EMPTY);
        return pddOrderFeign.workStatus(appointUpdate);
    }

    /**
     * 完成
     */
    private MSResponse orderComplete(Long b2bOrderId, String b2bOrderNo, Long completeDt, List<PddOrderUpdate.AttachUrl> attachs,
                                           long createById, long createDt) {
        PddOrderUpdate appointUpdate = new PddOrderUpdate();
        appointUpdate.setB2bOrderId(b2bOrderId);
        appointUpdate.setUpdateById(createById);
        appointUpdate.setUpdateDt(createDt);
        appointUpdate.setBizStatus(B2BPddOrderStatusEnum.COMPLETED.value)
                .setServOrderSn(b2bOrderNo)
                .setExecuteTime(completeDt)
                .setBizDetailDesc(StringUtils.EMPTY)
                .setAttachUrls(attachs==null?Lists.newArrayList():attachs);
        return pddOrderFeign.workStatus(appointUpdate);
    }

    /**
     * 取消
     */
    private MSResponse orderCancel(Long b2bOrderId, String b2bOrderNo, String remarks,
                                         long createById, long createDt) {
        PddOrderUpdate appointUpdate = new PddOrderUpdate();
        appointUpdate.setB2bOrderId(b2bOrderId);
        appointUpdate.setUpdateById(createById);
        appointUpdate.setUpdateDt(createDt);
        appointUpdate.setBizStatus(B2BPddOrderStatusEnum.CANCELED.value)
                .setServOrderSn(b2bOrderNo)
                .setExecuteTime(createDt)
                .setBizDetailDesc(remarks);
        return pddOrderFeign.workStatus(appointUpdate);
    }

    /**
     * 往微服务发送工单状态更新命令
     */
    public MSResponse sendOrderStatusUpdateCommandToB2B(MQB2BOrderStatusUpdateMessage.B2BOrderStatusUpdateMessage message) {
        MSResponse response = null;
        if (message.getStatus() == B2BOrderStatusEnum.PLANNED.value) {
            response = orderPlan(message.getB2BOrderId(), message.getB2BOrderNo(), message.getEngineerId(), message.getEngineerName(),
                    message.getEngineerMobile(), message.getUpdaterId(), message.getUpdateDt());
        } else if (message.getStatus() == B2BOrderStatusEnum.APPOINTED.value) {
            response = appoint(message.getB2BOrderId(), message.getB2BOrderNo(), message.getEffectiveDt(), message.getRemarks(), message.getUpdaterId(), message.getUpdateDt());
        } else if (message.getStatus() == B2BOrderStatusEnum.SERVICED.value){
            response = orderService(message.getB2BOrderId(), message.getB2BOrderNo(), message.getEffectiveDt(),message.getUpdaterId(), message.getUpdateDt());
        } else if (message.getStatus() == B2BOrderStatusEnum.COMPLETED.value) {
            //pics
            List<PddOrderUpdate.AttachUrl> attachUrls = null;
            if(!CollectionUtils.isEmpty(message.getCompletedItemList())){
                attachUrls = Lists.newArrayListWithCapacity(message.getCompletedItemOrBuilderList().size());
                for (MQB2BOrderStatusUpdateMessage.CompletedItem item : message.getCompletedItemList()) {
                    attachUrls.add(new PddOrderUpdate.AttachUrl().setName(item.getBarcode()).setUrl(item.getPic1()));
                }
            }
            response = orderComplete(message.getB2BOrderId(), message.getB2BOrderNo(), message.getEffectiveDt(), attachUrls, message.getUpdaterId(), message.getUpdateDt());
        } else if (message.getStatus() == B2BOrderStatusEnum.CANCELED.value) {
            response = orderCancel(message.getB2BOrderId(), message.getB2BOrderNo(), message.getRemarks(), message.getUpdaterId(), message.getUpdateDt());
        } else {
            response = new MSResponse(MSErrorCode.SUCCESS);
            String msgJson = new JsonFormat().printToString(message);
            LogUtils.saveLog("B2B工单状态变更消息的状态错误", "PddOrderService.sendOrderStatusUpdateCommandToB2B", msgJson, null, null);
        }
        return response;
    }

    /**
     * 处理B2B发来的处理指令
     * 1.取消工单
     * @param processEntity
     * @return
     */
    public MSResponse processKKLOrder(B2BOrderProcessEntity processEntity) {
        boolean flag = false;
        try {
            if (processEntity.getActionType() == B2BOrderActionEnum.CONVERTED_CANCEL) {
                flag = cancelKKLOrder(processEntity);
            }
        } catch (Exception e) {
            B2BOrderProcessEntity.saveFailureLog(processEntity, "修改快可立工单", "PddOrderService.processKKLOrder", e);
        }
        return new MSResponse(flag ? MSErrorCode.SUCCESS : MSErrorCode.FAILURE);
    }

    /**
     * 收到拼多多指令后，直接取消系统工单
     * 1.未派单，取消
     * 2.已退单申请，审核退单
     * 3.其他状态，b2b取消订单
     */
    private boolean cancelKKLOrder(B2BOrderProcessEntity entity) {
        boolean result = false;
        Order order = orderCacheReadService.getOrderById(entity.getKklOrderId(), null, OrderUtils.OrderDataLevel.CONDITION, true, true);
        if (order != null && order.getOrderCondition() != null) {
            String remarks = StringUtils.toString(entity.getRemarks()) + "（" + B2BDataSourceEnum.PDD.name + "平台通知取消B2B工单）";
            if (order.getOrderCondition().getStatusValue() <= Order.ORDER_STATUS_ACCEPTED) {
                orderService.cancelOrderNew(order.getId(), B2BOrderVModel.b2bUser, remarks, false);
                result = true;
            } else if (order.getOrderCondition().getStatusValue() == Order.ORDER_STATUS_RETURNING) { //当前是退单审核状态，则直接退单审核
                orderService.approveReturnOrderNew(order.getId(), order.getQuarter(), remarks, B2BOrderVModel.b2bUser);
                result = true;
            } else {
                orderService.returnOrderNew(order.getId(), new Dict("51", "厂家(电商)通知取消"), "", remarks, B2BOrderVModel.b2bUser);
//                orderService.b2bCancelOrder(order.getId(), order.getQuarter(), new Dict("51", "厂家(电商)通知取消"), remarks, B2BOrderVModel.b2bUser);
                result = true;
            }
            updateProcessFlag(entity.getMessageId());
        } else {
            B2BOrderProcessEntity.saveFailureLog(entity, "读取工单失败", "PddOrderService.cancelKKLOrder", null);
        }
        return result;
    }

    /**
     * Pdd方取消订单，工单系统处理完成后，回调通知B2B
     * @param messageId
     * @return
     */
    public MSResponse updateProcessFlag(Long messageId) {
        return pddOrderFeign.updateProcessFlag(messageId);
    }

    //region Generator

    /**
     * 创建派单请求对象
     */
    public TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> createPlanRequestEntity(Long engineerId, String engineerName, String engineerMobile) {
        TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> result = new TwoTuple<>(false, null);
        if (engineerId != null && StringUtils.isNotBlank(engineerName) && StringUtils.isNotBlank(engineerMobile)) {
            B2BOrderStatusUpdateReqEntity.Builder builder = new B2BOrderStatusUpdateReqEntity.Builder();
            builder.setEngineerName(engineerName)
                    .setEngineerMobile(engineerMobile)
                    .setEngineerId(engineerId.toString());
            result.setAElement(true);
            result.setBElement(builder);
        }
        return result;
    }

    /**
     * 创建预约请求对象
     */
    public TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> createAppointRequestEntity(Date appointmentDate, String remarks) {
        TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> result = new TwoTuple<>(false, null);
        if (appointmentDate != null) {
            B2BOrderStatusUpdateReqEntity.Builder builder = new B2BOrderStatusUpdateReqEntity.Builder();
            builder.setEffectiveDate(appointmentDate)
                    .setRemarks(StringUtils.trimToEmpty(remarks));
            result.setAElement(true);
            result.setBElement(builder);
        }
        return result;
    }

    /**
     * 创建上门请求对象
     */
    public TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> createServiceRequestEntity(Date visitedDate, Long servicePointId, Long engineerId, String remarks) {
        TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> result = new TwoTuple<>(false, null);
        if (visitedDate != null && servicePointId != null && servicePointId > 0 && engineerId != null && engineerId > 0) {
            Engineer engineer = servicePointService.getEngineerFromCache(servicePointId, engineerId);
            if (engineer != null && StringUtils.isNotBlank(engineer.getName()) && StringUtils.isNotBlank(engineer.getContactInfo())) {
                B2BOrderStatusUpdateReqEntity.Builder builder = new B2BOrderStatusUpdateReqEntity.Builder();
                builder.setEngineerName(engineer.getName())
                        .setEffectiveDate(visitedDate)
                        .setRemarks(StringUtils.trimToEmpty(remarks));
                result.setAElement(true);
                result.setBElement(builder);
            }
        }
        return result;
    }

    /**
     * 创建完工请求对象
     * 需要图片名称及地址
     */
    @Transactional()
    public TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> createCompleteRequestEntity(Long orderId, String quarter, Date completedDate) {
        TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> result = new TwoTuple<>(false, null);
        if (orderId != null && orderId > 0 && StringUtils.isNotBlank(quarter)) {
            B2BOrderStatusUpdateReqEntity.Builder builder = new B2BOrderStatusUpdateReqEntity.Builder();
            builder.setEffectiveDate(completedDate);
            List<OrderItemComplete> completeList = orderItemCompleteService.getByOrderId(orderId, quarter);
            if (!CollectionUtils.isEmpty(completeList)) {
                List<CanboOrderCompleted.CompletedItem> items = Lists.newArrayListWithCapacity(20);
                for (OrderItemComplete item : completeList) {
                    item.setItemList(OrderUtils.fromProductCompletePicItemsJson(item.getPicJson()));
                    //pics
                    if(!CollectionUtils.isEmpty(item.getItemList())){
                        for (ProductCompletePicItem picItem : item.getItemList()) {
                            if(StringUtils.isNotBlank(picItem.getUrl())){
                                CanboOrderCompleted.CompletedItem completedItem = new CanboOrderCompleted.CompletedItem();
                                completedItem.setBarcode(StringUtils.trimToEmpty(picItem.getTitle()));
                                completedItem.setPic1(OrderPicUtils.getOrderPicHostDir() + picItem.getUrl());
                                items.add(completedItem);
                            }
                        }
                    }
                }
                builder.setCompletedItems(items);
            }
            result.setAElement(true);
            result.setBElement(builder);
        }
        return result;
    }

    /**
     * 创建取消请求对象
     */
    public TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> createCancelRequestEntity(Integer kklCancelType) {
        TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> result = new TwoTuple<>(false, null);
        if (kklCancelType != null) {
            Dict cancelResponsibleDict = MSDictUtils.getDictByValue(kklCancelType.toString(), Dict.DICT_TYPE_CANCEL_RESPONSIBLE);
            if (cancelResponsibleDict != null && StringUtils.isNotBlank(cancelResponsibleDict.getLabel())) {
                B2BOrderStatusUpdateReqEntity.Builder builder = new B2BOrderStatusUpdateReqEntity.Builder();
                builder.setRemarks(cancelResponsibleDict.getLabel());
                result.setAElement(true);
                result.setBElement(builder);
            }
        }
        return result;
    }

    //endregion Generator

}
