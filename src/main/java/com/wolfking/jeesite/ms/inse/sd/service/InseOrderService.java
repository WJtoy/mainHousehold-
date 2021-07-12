package com.wolfking.jeesite.ms.inse.sd.service;

import com.google.common.collect.Maps;
import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.entity.b2bcenter.pb.MQB2BOrderProcessLogMessage;
import com.kkl.kklplus.entity.b2bcenter.pb.MQB2BOrderStatusUpdateMessage;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderActionEnum;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderStatusEnum;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderTransferResult;
import com.kkl.kklplus.entity.canbo.sd.CanboOrderCompleted;
import com.kkl.kklplus.entity.inse.sd.*;
import com.kkl.kklplus.entity.xyyplus.sd.XYYOrderCancelAudit;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.md.entity.ProductCompletePicItem;
import com.wolfking.jeesite.modules.sd.dao.OrderItemDao;
import com.wolfking.jeesite.modules.sd.entity.*;
import com.wolfking.jeesite.modules.sd.service.OrderCacheReadService;
import com.wolfking.jeesite.modules.sd.service.OrderItemCompleteService;
import com.wolfking.jeesite.modules.sd.utils.OrderItemUtils;
import com.wolfking.jeesite.modules.sd.utils.OrderPicUtils;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderProcessEntity;
import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderProcessLogReqEntity;
import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderStatusUpdateReqEntity;
import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderVModel;
import com.wolfking.jeesite.ms.b2bcenter.sd.service.B2BOrderManualBaseService;
import com.wolfking.jeesite.ms.inse.sd.feign.InseOrderFeign;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class InseOrderService extends B2BOrderManualBaseService {

    @Resource
    private OrderItemDao orderItemDao;

    @Autowired
    private InseOrderFeign inseOrderFeign;

    @Autowired
    OrderItemCompleteService orderItemCompleteService;

    /**
     * 取消工单转换
     */
    public MSResponse cancelOrderTransition(B2BOrderTransferResult b2BOrderTransferResult) {
        return inseOrderFeign.cancelOrderTransition(b2BOrderTransferResult);
    }

    //region 工单状态检查

    /**
     * 批量检查工单是否可转换
     */
    public MSResponse checkB2BOrderProcessFlag(List<B2BOrderTransferResult> b2bOrderNos) {
        return inseOrderFeign.checkWorkcardProcessFlag(b2bOrderNos);
    }

    //endregion 工单状态检查

    //region 更新转单进度

    /**
     * 调用天猫微服务的B2B工单转换进度更新接口
     */
    public MSResponse sendB2BOrderConversionProgressUpdateCommandToB2B(List<B2BOrderTransferResult> progressList) {
        return inseOrderFeign.updateTransferResult(Lists.newArrayList(progressList));
    }

    //endregion 更新转单进度

    //region 创建工单日志消息实体

    /**
     * 创建优盟工单日志消息实体
     */
    public TwoTuple<Boolean, B2BOrderProcessLogReqEntity.Builder> createOrderProcessLogReqEntity(OrderProcessLog log) {
        TwoTuple<Boolean, B2BOrderProcessLogReqEntity.Builder> result = new TwoTuple<>(false, null);
        if (log.getCreateBy() != null && StringUtils.isNotBlank(log.getCreateBy().getName())
                && log.getCreateDate() != null && StringUtils.isNotBlank(log.getActionComment())) {
            B2BOrderProcessLogReqEntity.Builder builder = new B2BOrderProcessLogReqEntity.Builder();
            builder.setOperatorName(log.getCreateBy().getName())
                    .setLogDt(log.getCreateDate().getTime())
                    .setLogContext(log.getActionComment())
                    .setDataSourceId(B2BDataSourceEnum.INSE.id);
            result.setAElement(true);
            result.setBElement(builder);
        }
        return result;
    }


    /**
     * 往优盟微服务推送工单日志
     */
    public MSResponse pushOrderProcessLogToMS(MQB2BOrderProcessLogMessage.B2BOrderProcessLogMessage message) {
        InseOrderRemark orderProcessLog = new InseOrderRemark();
        orderProcessLog.setOrderId(message.getOrderId());
        orderProcessLog.setRemark(message.getLogContext());
        orderProcessLog.setCreateById(message.getCreateById());
        orderProcessLog.setCreateDt(message.getCreateDt());
        MSResponse response = inseOrderFeign.saveLog(orderProcessLog);
        return response;
    }

    //endregion 创建工单日志消息实体


    //region 工单状态变更

    /**
     * 派单
     */
    private MSResponse inseOrderPlan(Long b2bOrderId, String b2bOrderNo, String engineerId, String engineerName, String engineerMobile,
                                     long createById, long createDt) {
        InseOrderPlanned planned = new InseOrderPlanned();
        planned.setB2bOrderId(b2bOrderId);
        planned.setOrderNo(b2bOrderNo);
        planned.setEngineerId(engineerId);
        planned.setEngineerName(engineerName);
        planned.setEngineerMobile(engineerMobile);
        planned.setCreateDt(createDt);
        planned.setCreateById(createById);
        planned.setUpdateDt(createDt);
        planned.setUpdateById(createById);
        return inseOrderFeign.orderPlanned(planned);
    }

    /**
     * 预约
     */
    private MSResponse inseOrderAppoint(Long b2bOrderId, String b2bOrderNo, Long appointmentDt, long createById, long createDt) {
        InseOrderAppointed appointed = new InseOrderAppointed();
        appointed.setB2bOrderId(b2bOrderId);
        appointed.setOrderNo(b2bOrderNo);
        appointed.setBookDt(appointmentDt);
        appointed.setCreateDt(createDt);
        appointed.setCreateById(createById);
        appointed.setUpdateDt(createDt);
        appointed.setUpdateById(createById);
        return inseOrderFeign.orderAppointed(appointed);
    }

    /**
     * 上门
     */
    private MSResponse inseOrderVisit(Long b2bOrderId, String b2bOrderNo, long createById, long createDt) {
        InseOrderVisited visited = new InseOrderVisited();
        visited.setB2bOrderId(b2bOrderId);
        visited.setOrderNo(b2bOrderNo);
        visited.setCreateDt(createDt);
        visited.setCreateById(createById);
        visited.setUpdateDt(createDt);
        visited.setUpdateById(createById);
        return inseOrderFeign.orderVisited(visited);
    }

    /**
     * 完成
     */
    private MSResponse inseOrderCompleted(Long b2bOrderId, String b2bOrderNo, List<InseOrderCompleted.ProductDetail> items, long createById, long createDt) {
        InseOrderCompleted completed = new InseOrderCompleted();
        completed.setB2bOrderId(b2bOrderId);
        completed.setOrderNo(b2bOrderNo);
        completed.setItems(items);
        completed.setCreateDt(createDt);
        completed.setCreateById(createById);
        completed.setUpdateDt(createDt);
        completed.setUpdateById(createById);
        return inseOrderFeign.orderCompleted(completed);
    }

    /**
     * 退单申请
     */
    private MSResponse inseApplyCancelOrder(Long b2bOrderId, String b2bOrderNo, String cancelRemark, long createById, long createDt) {
        InseOrderCancelApply cancelApply = new InseOrderCancelApply();
        cancelApply.setB2bOrderId(b2bOrderId);
        cancelApply.setOrderNo(b2bOrderNo);
        cancelApply.setCancelRemark(cancelRemark);
        cancelApply.setCreateDt(createDt);
        cancelApply.setCreateById(createById);
        cancelApply.setUpdateDt(createDt);
        cancelApply.setUpdateById(createById);
        return inseOrderFeign.apply(cancelApply);
    }

    /**
     * 取消
     */
    private MSResponse inseOrderCancelled(Long b2bOrderId, String b2bOrderNo, String cancelRemark, long createById, long createDt) {
        InseOrderCancelled cancelled = new InseOrderCancelled();
        cancelled.setB2bOrderId(b2bOrderId);
        cancelled.setOrderNo(b2bOrderNo);
        cancelled.setCancelRemark(cancelRemark);
        cancelled.setCreateDt(createDt);
        cancelled.setCreateById(createById);
        cancelled.setUpdateDt(createDt);
        cancelled.setUpdateById(createById);
        return inseOrderFeign.orderCancelled(cancelled);
    }

    /**
     * 往樱雪微服务发送工单状态更新命令
     */
    public MSResponse sendOrderStatusUpdateCommandToB2B(MQB2BOrderStatusUpdateMessage.B2BOrderStatusUpdateMessage message) {
        MSResponse response;
        if (message.getStatus() == B2BOrderStatusEnum.PLANNED.value) {
            response = inseOrderPlan(message.getB2BOrderId(), message.getB2BOrderNo(), message.getEngineerId(), message.getEngineerName(), message.getEngineerMobile(), message.getUpdaterId(), message.getUpdateDt());
        } else if (message.getStatus() == B2BOrderStatusEnum.APPOINTED.value) {
            response = inseOrderAppoint(message.getB2BOrderId(), message.getB2BOrderNo(), message.getEffectiveDt(), message.getUpdaterId(), message.getUpdateDt());
        } else if (message.getStatus() == B2BOrderStatusEnum.SERVICED.value) {
            response = inseOrderVisit(message.getB2BOrderId(), message.getB2BOrderNo(), message.getUpdaterId(), message.getUpdateDt());
        } else if (message.getStatus() == B2BOrderStatusEnum.COMPLETED.value || message.getStatus() == B2BOrderStatusEnum.APP_COMPLETED.value) {
            List<MQB2BOrderStatusUpdateMessage.CompletedItem> itemList = message.getCompletedItemList();
            List<InseOrderCompleted.ProductDetail> completedItemList = Lists.newArrayList();
            if (itemList != null && itemList.size() > 0) {
                InseOrderCompleted.ProductDetail completedItem;
                for (MQB2BOrderStatusUpdateMessage.CompletedItem item : itemList) {
                    completedItem = new InseOrderCompleted.ProductDetail();
                    completedItem.setProductCode(item.getItemCode());
                    completedItem.setPic(item.getPic1());
                    completedItemList.add(completedItem);
                }
            }
            response = inseOrderCompleted(message.getB2BOrderId(), message.getB2BOrderNo(), completedItemList, message.getUpdaterId(), message.getUpdateDt());
        } else if (message.getStatus() == B2BOrderStatusEnum.APPLIED_FOR_CANCEL.value) {
            response = inseApplyCancelOrder(message.getB2BOrderId(), message.getB2BOrderNo(), message.getRemarks(), message.getUpdaterId(), message.getUpdateDt());
        } else if (message.getStatus() == B2BOrderStatusEnum.CANCELED.value) {
            response = inseOrderCancelled(message.getB2BOrderId(), message.getB2BOrderNo(), message.getRemarks(), message.getUpdaterId(), message.getUpdateDt());
            //new MSResponse(MSErrorCode.SUCCESS);
        } else {
            response = new MSResponse(MSErrorCode.SUCCESS);
            String msgJson = new JsonFormat().printToString(message);
            LogUtils.saveLog("B2B工单状态变更消息的状态错误", "InseOrderService.sendOrderStatusUpdateCommandToB2B", msgJson, null, null);
        }
        return response;
    }


    //endregion 工单状态变更

    //region 其他

    private List<CanboOrderCompleted.CompletedItem> getInseOrderCompletedItems(Long orderId, String quarter, List<OrderItem> orderItems) {
        List<CanboOrderCompleted.CompletedItem> list = Lists.newArrayList();
        if (orderId != null && orderId > 0 && StringUtils.isNotBlank(quarter) && orderItems != null && !orderItems.isEmpty()) {
            Map<Long, List<String>> b2bProductCodeMap = getProductIdToB2BProductCodeMapping(orderItems);
//            for (OrderItem item : orderItems) {
//                if (StringUtils.isNotBlank(item.getB2bProductCode())) {
//                    if (b2bProductCodeMap.containsKey(item.getProductId())) {
//                        b2bProductCodeMap.get(item.getProductId()).add(item.getB2bProductCode());
//                    } else {
//                        b2bProductCodeMap.put(item.getProductId(), Lists.newArrayList(item.getB2bProductCode()));
//                    }
//                }
//            }
            List<OrderItemComplete> completeList = orderItemCompleteService.getByOrderId(orderId, quarter);
            Map<Long, List<OrderItemComplete>> itemCompleteMap = Maps.newHashMap();
            for (OrderItemComplete item : completeList) {
                if (itemCompleteMap.containsKey(item.getProduct().getId())) {
                    itemCompleteMap.get(item.getProduct().getId()).add(item);
                } else {
                    itemCompleteMap.put(item.getProduct().getId(), Lists.newArrayList(item));
                }
            }

            CanboOrderCompleted.CompletedItem completedItem;
            for (Map.Entry<Long, List<String>> item : b2bProductCodeMap.entrySet()) {
                List<OrderItemComplete> itemCompletes = itemCompleteMap.get(item.getKey());
                for (int i = 0; i < item.getValue().size(); i++) {
                    completedItem = new CanboOrderCompleted.CompletedItem();
                    completedItem.setItemCode(item.getValue().get(i));

                    if (itemCompletes != null && itemCompletes.size() > i) {
                        OrderItemComplete itemComplete = itemCompletes.get(i);
                        List<ProductCompletePicItem> picItems = OrderUtils.fromProductCompletePicItemsJson(itemComplete.getPicJson());
                        if (picItems != null && !picItems.isEmpty()) {
                            completedItem.setPic1(OrderPicUtils.getOrderPicHostDir() + picItems.get(0).getUrl());
                        }
                    }

                    list.add(completedItem);
                }
            }
        }
        return list;
    }

    //endregion 其他


    //-------------------------------------------------------------------------------------------------创建状态变更请求实体

    /**
     * 创建樱雪派单请求对象
     */
    public TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> createPlanRequestEntity(Long engineerId, String engineerName, String engineerMobile) {
        TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> result = new TwoTuple<>(false, null);
        if (engineerId != null && StringUtils.isNotBlank(engineerName) && StringUtils.isNotBlank(engineerMobile)) {
            B2BOrderStatusUpdateReqEntity.Builder builder = new B2BOrderStatusUpdateReqEntity.Builder();
            builder.setEngineerId(engineerId.toString())
                    .setEngineerName(engineerName)
                    .setEngineerMobile(engineerMobile);
            result.setAElement(true);
            result.setBElement(builder);
        }
        return result;
    }

    /**
     * 创建樱雪预约请求对象
     */
    public TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> createAppointRequestEntity(Date appointmentDate) {
        TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> result = new TwoTuple<>(false, null);
        if (appointmentDate != null) {
            B2BOrderStatusUpdateReqEntity.Builder builder = new B2BOrderStatusUpdateReqEntity.Builder();
            builder.setEffectiveDate(appointmentDate);
            result.setAElement(true);
            result.setBElement(builder);
        }
        return result;
    }

    /**
     * 创建樱雪上门请求对象
     */
    public TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> createServiceRequestEntity() {
        TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> result = new TwoTuple<>(false, null);
        B2BOrderStatusUpdateReqEntity.Builder builder = new B2BOrderStatusUpdateReqEntity.Builder();
        result.setAElement(true);
        result.setBElement(builder);
        return result;
    }

    /**
     * 创建樱雪完工请求对象
     */
    @Transactional()
    public TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> createInseCompleteRequestEntity(Long orderId, String quarter, List<OrderItem> orderItems) {
        TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> result = new TwoTuple<>(false, null);
        if (orderId != null && orderId > 0 && StringUtils.isNotBlank(quarter)) {
            if (orderItems == null || orderItems.isEmpty()) {
                Order order = orderItemDao.getOrderItems(quarter, orderId);
                if (order != null) {
                    //orderItems = OrderItemUtils.fromOrderItemsJson(order.getOrderItemJson());
                    orderItems = OrderItemUtils.pbToItems(order.getItemsPb());//2020-12-17 sd_order -> sd_order_head
                }
            }
            List<CanboOrderCompleted.CompletedItem> completedItems = getInseOrderCompletedItems(orderId, quarter, orderItems);
            B2BOrderStatusUpdateReqEntity.Builder builder = new B2BOrderStatusUpdateReqEntity.Builder();
            builder.setCompletedItems(completedItems);
            result.setAElement(true);
            result.setBElement(builder);
        }
        return result;
    }

    /**
     * 创建樱雪取消请求对象
     */
    public TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> createReturnOrderApplyRequestEntity(String remarks) {
        TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> result = new TwoTuple<>(false, null);
        B2BOrderStatusUpdateReqEntity.Builder builder = new B2BOrderStatusUpdateReqEntity.Builder();
        builder.setRemarks(StringUtils.toString(remarks));
        result.setAElement(true);
        result.setBElement(builder);
        return result;
    }

    /**
     * 创建樱雪取消请求对象
     */
    public TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> createInseCancelRequestEntity(Integer kklCancelType, String remarks) {
        TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> result = new TwoTuple<>(false, null);
        StringBuilder stringBuilder = new StringBuilder();
        String cancelResponsible = null;
        if (kklCancelType != null) {
            cancelResponsible = MSDictUtils.getDictLabel(kklCancelType.toString(), Dict.DICT_TYPE_CANCEL_RESPONSIBLE, "");
            stringBuilder.append(cancelResponsible);
        }
        if (StringUtils.isNotBlank(remarks)) {
            if (StringUtils.isNotBlank(cancelResponsible)) {
                stringBuilder.append(":");
            }
            stringBuilder.append(remarks);
        }
        String cancelRemarks = stringBuilder.toString();
        if (StringUtils.isNotBlank(cancelRemarks)) {
            B2BOrderStatusUpdateReqEntity.Builder builder = new B2BOrderStatusUpdateReqEntity.Builder();
            builder.setRemarks(cancelRemarks);
            result.setAElement(true);
            result.setBElement(builder);
        }
        return result;
    }

    //region 处理快可立工单

    @Autowired
    private OrderCacheReadService orderCacheReadService;


    /**
     * 取消工单
     */
    private boolean cancelKKLOrder(B2BOrderProcessEntity entity) {
        boolean result = false;
        Order order = orderCacheReadService.getOrderById(entity.getKklOrderId(), null, OrderUtils.OrderDataLevel.CONDITION, true, true);
        if (order != null && order.getOrderCondition() != null) {
            String remarks = StringUtils.toString(entity.getRemarks()) + "（" + B2BDataSourceEnum.INSE.name + "平台通知取消B2B工单）";
            if (order.getOrderCondition().getStatusValue() <= Order.ORDER_STATUS_ACCEPTED) {
                orderService.cancelOrderNew(order.getId(), B2BOrderVModel.b2bUser, remarks, false);
                result = true;
            } else if (order.getOrderCondition().getStatusValue() == Order.ORDER_STATUS_RETURNING) { //当前是退单审核状态，则直接退单审核
                orderService.approveReturnOrderNew(order.getId(), order.getQuarter(), remarks, B2BOrderVModel.b2bUser);
                result = true;
            } else {
                orderService.b2bCancelOrder(order.getId(), order.getQuarter(), new Dict("51", "厂家(电商)通知取消"), remarks, B2BOrderVModel.b2bUser);
                result = true;
            }
        } else {
            B2BOrderProcessEntity.saveFailureLog(entity, "读取工单失败", "InseOrderService.cancelKKLOrder", null);
        }
        return result;
    }

    /**
     * 退单审核
     */
    private boolean approveReturnKKLOrder(B2BOrderProcessEntity entity) {
        boolean result = false;
        Order order = orderCacheReadService.getOrderById(entity.getKklOrderId(), null, OrderUtils.OrderDataLevel.CONDITION, true, true);
        if (order != null && order.getOrderCondition() != null) {
            String remarks = StringUtils.toString(entity.getRemarks()) + "（" + B2BDataSourceEnum.INSE.name + "平台审核B2B工单的退单申请）";
            orderService.approveReturnOrderNew(order.getId(), order.getQuarter(), remarks, B2BOrderVModel.b2bUser);
            result = true;
        } else {
            B2BOrderProcessEntity.saveFailureLog(entity, "读取工单失败", "InseOrderService.approveReturnKKLOrder", null);
        }
        return result;
    }

    /**
     * 退单申请驳回
     */
    private boolean rejectReturnKKLOrder(B2BOrderProcessEntity entity) {
        boolean result = false;
        Order order = orderCacheReadService.getOrderById(entity.getKklOrderId(), null, OrderUtils.OrderDataLevel.CONDITION, true, true);
        if (order != null && order.getOrderCondition() != null) {
            String remarks = StringUtils.toString(entity.getRemarks()) + "（" + B2BDataSourceEnum.INSE.name + "平台驳回B2B工单的退单申请）";
            orderService.rejectReturnOrderNew(order.getId(), order.getQuarter(), remarks, B2BOrderVModel.b2bUser);
            result = true;
        } else {
            B2BOrderProcessEntity.saveFailureLog(entity, "读取工单失败", "InseOrderService.rejectReturnKKLOrder", null);
        }
        return result;
    }


    public MSResponse processKKLOrder(B2BOrderProcessEntity processEntity) {
        boolean flag = false;
        try {
            if (processEntity.getActionType() == B2BOrderActionEnum.CONVERTED_CANCEL) {//樱雪主动申请取消工单
                updateProcessFlag(processEntity.getMessageId());
                flag = cancelKKLOrder(processEntity);
            } else if (processEntity.getActionType() == B2BOrderActionEnum.RETURN_APPROVE) {//樱雪审核退单审核
                updateProcessFlag(processEntity.getMessageId());
                if (processEntity.getStatus() == XYYOrderCancelAudit.REVIEW_STATUS_SUCCESS.intValue()) {
                    flag = approveReturnKKLOrder(processEntity);
                } else if (processEntity.getStatus() == XYYOrderCancelAudit.REVIEW_STATUS_FAILURE.intValue()) {
                    flag = rejectReturnKKLOrder(processEntity);
                }
            }
        } catch (Exception e) {
            B2BOrderProcessEntity.saveFailureLog(processEntity, "修改快可立工单", "InseOrderService.processKKLOrder", e);
        }
        return new MSResponse(flag ? MSErrorCode.SUCCESS : MSErrorCode.FAILURE);
    }


    public MSResponse updateProcessFlag(Long messageId) {
        return inseOrderFeign.updateProcessFlag(messageId);
    }
    //endregion 编辑、处理快可立工单

}
