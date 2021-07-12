package com.wolfking.jeesite.ms.canbo.sd.service;

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
import com.kkl.kklplus.entity.canbo.sd.*;
import com.kkl.kklplus.entity.viomi.sd.VioMiOrderRemark;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.md.entity.ProductCompletePicItem;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.sd.dao.OrderItemDao;
import com.wolfking.jeesite.modules.sd.entity.*;
import com.wolfking.jeesite.modules.sd.service.OrderCacheReadService;
import com.wolfking.jeesite.modules.sd.service.OrderItemCompleteService;
import com.wolfking.jeesite.modules.sd.utils.OrderItemUtils;
import com.wolfking.jeesite.modules.sd.utils.OrderPicUtils;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderProcessEntity;
import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderProcessLogReqEntity;
import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderStatusUpdateReqEntity;
import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderVModel;
import com.wolfking.jeesite.ms.b2bcenter.sd.service.B2BOrderManualBaseService;
import com.wolfking.jeesite.ms.canbo.sd.feign.CanboOrderFeign;
import com.wolfking.jeesite.ms.providermd.service.MSServicePointService;
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

@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Slf4j
public class CanboOrderService extends B2BOrderManualBaseService {

    @Autowired
    private CanboOrderFeign canboOrderFeign;

    @Resource
    private OrderItemDao orderItemDao;

    @Autowired
    private OrderItemCompleteService orderItemCompleteService;


    @Autowired
    private OrderCacheReadService orderCacheReadService;
    @Autowired
    private MSServicePointService msServicePointService;

    /**
     * 取消工单转换
     *
     * @param b2BOrderTransferResult
     * @return
     */
    public MSResponse cancelOrderTransition(B2BOrderTransferResult b2BOrderTransferResult) {
        return canboOrderFeign.cancelOrderTransition(b2BOrderTransferResult);
    }


    //region 工单状态检查


    /**
     * 批量检查工单是否可转换
     */
    public MSResponse checkB2BOrderProcessFlag(List<B2BOrderTransferResult> b2bOrderNos) {
        return canboOrderFeign.checkWorkcardProcessFlag(b2bOrderNos);
    }

    //endregion 工单状态检查


    //region 更新工单转换状态

    /**
     * 调用同望微服务的B2B工单转换进度更新接口
     */
    public MSResponse sendB2BOrderConversionProgressUpdateCommandToB2B(List<B2BOrderTransferResult> progressList) {
        return canboOrderFeign.updateTransferResult(Lists.newArrayList(progressList));
    }

    //endregion 更新工单转换状态


    //region 康宝工单状态变更

    /**
     * 往康宝微服务发送工单状态更新命令
     */
    public MSResponse sendOrderStatusUpdateCommandToB2B(MQB2BOrderStatusUpdateMessage.B2BOrderStatusUpdateMessage message) {
        MSResponse response = null;
        if (message.getStatus() == B2BOrderStatusEnum.PLANNED.value) {
            response = canboOrderPlanned(message.getMessageId(), message.getDataSource(), message.getB2BOrderId(), message.getB2BOrderNo(),
                    message.getEngineerName(), message.getEngineerMobile(), message.getUpdaterId(), message.getUpdateDt());
        } else if (message.getStatus() == B2BOrderStatusEnum.APPOINTED.value) {
            response = canboOrderAppointed(message.getMessageId(), message.getDataSource(), message.getB2BOrderId(), message.getB2BOrderNo(),
                    message.getUpdaterName(), message.getEffectiveDt(), message.getRemarks(), message.getUpdaterId(), message.getUpdateDt());
        } else if (message.getStatus() == B2BOrderStatusEnum.COMPLETED.value) {
            List<MQB2BOrderStatusUpdateMessage.CompletedItem> messageCompletedItemList = message.getCompletedItemList();
            List<CanboOrderCompleted.CompletedItem> completedItemList = Lists.newArrayList();
            if (messageCompletedItemList != null && messageCompletedItemList.size() > 0) {
                CanboOrderCompleted.CompletedItem completedItem = null;
                for (MQB2BOrderStatusUpdateMessage.CompletedItem item : messageCompletedItemList) {
                    completedItem = new CanboOrderCompleted.CompletedItem();
                    completedItem.setItemCode(item.getItemCode());
                    completedItem.setPic1(item.getPic1());
                    completedItem.setPic2(item.getPic2());
                    completedItem.setPic3(item.getPic3());
                    completedItem.setPic4(item.getPic4());
                    completedItem.setBarcode(item.getBarcode());
                    completedItem.setOutBarcode(item.getOutBarcode());
                    completedItemList.add(completedItem);
                }
            }
            response = canboOrderCompleted(message.getMessageId(), message.getDataSource(), message.getB2BOrderId(),
                    message.getB2BOrderNo(), completedItemList, message.getUpdaterId(), message.getUpdateDt());
        } else if (message.getStatus() == B2BOrderStatusEnum.CANCELED.value) {
            response = canboOrderCancelled(message.getMessageId(), message.getDataSource(), message.getB2BOrderId(), message.getB2BOrderNo(),
                    message.getUpdaterName(), message.getEffectiveDt(), message.getRemarks(), message.getUpdaterId(), message.getUpdateDt());
        } else {
            response = new MSResponse(MSErrorCode.SUCCESS);
            String msgJson = new JsonFormat().printToString(message);
            LogUtils.saveLog("B2B工单状态变更消息的状态错误", "CanboOrderService.sendOrderStatusUpdateCommandToB2B", msgJson, null, null);
        }
        return response;
    }

    /**
     * 同望派单
     */
    private MSResponse canboOrderPlanned(Long messageId, Integer dataSource, Long b2bOrderId, String b2bOrderNo, String engineerName, String engineerMobile, long createById, long createDt) {
        CanboOrderPlanned planned = new CanboOrderPlanned();
        planned.setUniqueId(messageId);
        planned.setDataSource(dataSource);
        planned.setB2bOrderId(b2bOrderId);
        planned.setOrderNo(b2bOrderNo);
        planned.setEngineerName(engineerName);
        planned.setEngineerMobile(engineerMobile);
        planned.setCreateById(createById);
        planned.setUpdateById(createById);
        Date date = new Date(createDt);
        planned.setCreateDate(date);
        planned.setUpdateDate(date);
        return canboOrderFeign.orderPlanned(planned);
    }

    /**
     * 同望预约
     */
    private MSResponse canboOrderAppointed(Long messageId, Integer dataSource, Long b2bOrderId, String b2bOrderNo, String operator, Long appointmentDt, String remark, long createById, long createDt) {
        CanboOrderAppointed appointed = new CanboOrderAppointed();
        appointed.setUniqueId(messageId);
        appointed.setDataSource(dataSource);
        appointed.setB2bOrderId(b2bOrderId);
        appointed.setOrderNo(b2bOrderNo);
        appointed.setBookMan(operator);
        appointed.setBookDt(appointmentDt);
        appointed.setBookRemark(remark);
        appointed.setCreateById(createById);
        appointed.setUpdateById(createById);
        Date date = new Date(createDt);
        appointed.setCreateDate(date);
        appointed.setUpdateDate(date);
        return canboOrderFeign.orderAppointed(appointed);
    }

    /**
     * 同望完成
     */
    private MSResponse canboOrderCompleted(Long messageId, Integer dataSource, Long b2bOrderId, String b2bOrderNo, List<CanboOrderCompleted.CompletedItem> completedItems, long createById, long createDt) {
        CanboOrderCompleted completed = new CanboOrderCompleted();
        completed.setUniqueId(messageId);
        completed.setDataSource(dataSource);
        completed.setB2bOrderId(b2bOrderId);
        completed.setOrderNo(b2bOrderNo);
        completed.setItems(completedItems);
        completed.setCreateById(createById);
        completed.setUpdateById(createById);
        Date date = new Date(createDt);
        completed.setCreateDate(date);
        completed.setUpdateDate(date);
        return canboOrderFeign.orderCompleted(completed);
    }

    /**
     * 同望取消
     */
    private MSResponse canboOrderCancelled(Long messageId, Integer dataSource, Long b2bOrderId, String b2bOrderNo, String operator, Long cancelDt, String remark, long createById, long createDt) {
        CanboOrderCancelled cancelled = new CanboOrderCancelled();
        cancelled.setUniqueId(messageId);
        cancelled.setDataSource(dataSource);
        cancelled.setB2bOrderId(b2bOrderId);
        cancelled.setOrderNo(b2bOrderNo);
        cancelled.setCancelMan(operator);
        cancelled.setCancelDt(cancelDt);
        cancelled.setCancelRemark(remark);
        cancelled.setCreateById(createById);
        cancelled.setUpdateById(createById);
        Date date = new Date(createDt);
        cancelled.setCreateDate(date);
        cancelled.setUpdateDate(date);
        return canboOrderFeign.orderCancelled(cancelled);
    }

    /**
     * 处理B2B发来的处理指令
     * 1.取消工单
     *
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
            B2BOrderProcessEntity.saveFailureLog(processEntity, "修改快可立工单", "CanboOrderService.processKKLOrder", e);
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
            String remarks = StringUtils.toString(entity.getRemarks()) + "（" + order.getDataSource().getLabel() + "平台通知取消B2B工单）";
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
            B2BOrderProcessEntity.saveFailureLog(entity, "读取工单失败", "CanboOrderService.cancelKKLOrder", null);
        }
        return result;
    }

    //endregion region 工单状态变更


    //region 其他


    private List<CanboOrderCompleted.CompletedItem> getCanboOrderCompletedItems(Long orderId, String quarter, List<OrderItem> orderItems) {
        List<CanboOrderCompleted.CompletedItem> list = null;
        if (orderId != null && orderId > 0 && StringUtils.isNotBlank(quarter) && orderItems != null && !orderItems.isEmpty()) {
            List<OrderItemComplete> completeList = orderItemCompleteService.getByOrderId(orderId, quarter);
            CanboOrderCompleted.CompletedItem completedItem = null;
            ProductCompletePicItem picItem = null;
            if (completeList != null && !completeList.isEmpty()) {
                list = com.google.common.collect.Lists.newArrayList();
                Map<Long, List<String>> b2bProductCodeMap = getProductIdToB2BProductCodeMapping(orderItems);
                ;
//                for (OrderItem orderItem : orderItems) {
//                    if (StringUtils.isNotBlank(orderItem.getB2bProductCode())) {
//                        if (b2bProductCodeMap.containsKey(orderItem.getProductId())) {
//                            b2bProductCodeMap.get(orderItem.getProductId()).add(orderItem.getB2bProductCode());
//                        } else {
//                            b2bProductCodeMap.put(orderItem.getProductId(), com.google.common.collect.Lists.newArrayList(orderItem.getB2bProductCode()));
//                        }
//                    }
//                }
                List<String> b2bProductCodeList;
                for (OrderItemComplete item : completeList) {
                    item.setItemList(OrderUtils.fromProductCompletePicItemsJson(item.getPicJson()));
                    Map<String, ProductCompletePicItem> picItemMap = Maps.newHashMap();
                    for (ProductCompletePicItem innerItem : item.getItemList()) {
                        picItemMap.put(innerItem.getPictureCode(), innerItem);
                    }
                    completedItem = new CanboOrderCompleted.CompletedItem();

                    //获取B2B产品编码
                    b2bProductCodeList = b2bProductCodeMap.get(item.getProduct().getId());
                    if (b2bProductCodeList != null && !b2bProductCodeList.isEmpty()) {
                        completedItem.setItemCode(b2bProductCodeList.get(0));
                        if (b2bProductCodeList.size() > 1) {
                            b2bProductCodeList.remove(0);
                        }
                    } else {
                        completedItem.setItemCode("");
                    }

                    completedItem.setBarcode(StringUtils.toString(item.getUnitBarcode()));
                    completedItem.setOutBarcode(StringUtils.toString(item.getOutBarcode()));
                    //条码图片
                    picItem = picItemMap.get("pic4");
                    if (picItem != null && StringUtils.isNotBlank(picItem.getUrl())) {
                        completedItem.setPic4(OrderPicUtils.getOrderPicHostDir() + picItem.getUrl());
                    }
                    //现场图片
                    picItem = picItemMap.get("pic1");
                    if (picItem != null && StringUtils.isNotBlank(picItem.getUrl())) {
                        completedItem.setPic1(OrderPicUtils.getOrderPicHostDir() + picItem.getUrl());
                    }
                    picItem = picItemMap.get("pic2");
                    if (picItem != null && StringUtils.isNotBlank(picItem.getUrl())) {
                        completedItem.setPic2(OrderPicUtils.getOrderPicHostDir() + picItem.getUrl());
                    }
                    picItem = picItemMap.get("pic3");
                    if (picItem != null && StringUtils.isNotBlank(picItem.getUrl())) {
                        completedItem.setPic3(OrderPicUtils.getOrderPicHostDir() + picItem.getUrl());
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
     * 创建同望派单请求对象
     */
    public TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> createPlanRequestEntity(Integer dataSourceId, Long servicePointId, String engineerName, String engineerMobile) {
        TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> result = new TwoTuple<>(false, null);
        B2BOrderStatusUpdateReqEntity.Builder builder;
        if (dataSourceId == B2BDataSourceEnum.USATON.id) {
            if (servicePointId != null && servicePointId > 0 && StringUtils.isNotBlank(engineerMobile)) {
                ServicePoint servicePoint = msServicePointService.getSimpleCacheById(servicePointId);
                String servicePointName = servicePoint != null && StringUtils.isNotBlank(servicePoint.getName()) ? servicePoint.getName() : StringUtils.toString(engineerName);
                if (StringUtils.isNotBlank(servicePointName) && StringUtils.isNotBlank(engineerMobile)) {
                    builder = new B2BOrderStatusUpdateReqEntity.Builder();
                    builder.setEngineerName(servicePointName)
                            .setEngineerMobile(engineerMobile);
                    result.setAElement(true);
                    result.setBElement(builder);
                }
            }
        } else {
            if (StringUtils.isNotBlank(engineerName) && StringUtils.isNotBlank(engineerMobile)) {
                builder = new B2BOrderStatusUpdateReqEntity.Builder();
                builder.setEngineerName(engineerName)
                        .setEngineerMobile(engineerMobile);
                result.setAElement(true);
                result.setBElement(builder);
            }
        }
        return result;
    }

    /**
     * 创建同望预约请求对象
     */
    public TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> createAppointRequestEntity(Date appointmentDate, String remarks, User updater) {
        TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> result = new TwoTuple<>(false, null);
        if (appointmentDate != null && updater != null && StringUtils.isNotBlank(updater.getName())) {
            B2BOrderStatusUpdateReqEntity.Builder builder = new B2BOrderStatusUpdateReqEntity.Builder();
            builder.setEffectiveDate(appointmentDate)
                    .setUpdaterName(updater.getName())
                    .setRemarks(StringUtils.toString(remarks));
            result.setAElement(true);
            result.setBElement(builder);
        }
        return result;
    }

    /**
     * 创建同望完工请求对象
     */
    @Transactional()
    public TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> createCompleteRequestEntity(Long orderId, String quarter, List<OrderItem> orderItems) {
        TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> result = new TwoTuple<>(false, null);
        if (orderId != null && orderId > 0 && StringUtils.isNotBlank(quarter)) {
            if (orderItems == null || orderItems.isEmpty()) {
                Order order = orderItemDao.getOrderItems(quarter, orderId);
                if (order != null) {
                    //orderItems = OrderItemUtils.fromOrderItemsJson(order.getOrderItemJson());
                    orderItems = OrderItemUtils.pbToItems(order.getItemsPb());//2020-12-17 sd_order -> sd_order_head
                }
            }
            List<CanboOrderCompleted.CompletedItem> completedItems = getCanboOrderCompletedItems(orderId, quarter, orderItems);
            B2BOrderStatusUpdateReqEntity.Builder builder = new B2BOrderStatusUpdateReqEntity.Builder();
            builder.setCompletedItems(completedItems);
            result.setAElement(true);
            result.setBElement(builder);
        }
        return result;
    }

    /**
     * 创建同望取消请求对象
     */
    public TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> createTooneCancelRequestEntity(Date cancelDate, User updater, String remarks) {
        TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> result = new TwoTuple<>(false, null);
        if (cancelDate != null && updater != null && StringUtils.isNotBlank(updater.getName())) {
            B2BOrderStatusUpdateReqEntity.Builder builder = new B2BOrderStatusUpdateReqEntity.Builder();
            builder.setEffectiveDate(cancelDate)
                    .setUpdaterName(updater.getName())
                    .setRemarks(StringUtils.toString(remarks));
            result.setAElement(true);
            result.setBElement(builder);
        }
        return result;
    }

    //region  日志

    /**
     * 往云米微服务推送工单日志
     */
    public MSResponse pushOrderProcessLogToMS(MQB2BOrderProcessLogMessage.B2BOrderProcessLogMessage message) {
        CanboOrderLog log = new CanboOrderLog();
        log.setUniqueId(message.getId());
        log.setOrderId(message.getOrderId());
        log.setLogAction(message.getLogTitle());
        log.setLogContent(message.getLogContext());
        log.setCreateById(message.getCreateById());
        log.setDataSource(message.getDataSourceId());
        return canboOrderFeign.saveLog(log);
    }

    /**
     * 创建云米日志消息实体
     */
    public TwoTuple<Boolean, B2BOrderProcessLogReqEntity.Builder> createOrderProcessLogReqEntity(OrderProcessLog log, Integer dataSourceId) {
        TwoTuple<Boolean, B2BOrderProcessLogReqEntity.Builder> result = new TwoTuple<>(false, null);
        if (log.getCreateBy() != null &&log.getCreateBy().getId() != null
                && StringUtils.isNotBlank(log.getAction()) && StringUtils.isNotBlank(log.getActionComment()) ) {
            B2BOrderProcessLogReqEntity.Builder builder = new B2BOrderProcessLogReqEntity.Builder();
            builder.setOperatorName(log.getCreateBy().getName())
                    .setCreateById(log.getCreateBy().getId())
                    .setLogTitle(log.getAction())
                    .setLogContext(log.getActionComment())
                    .setDataSourceId(dataSourceId);
            result.setAElement(true);
            result.setBElement(builder);
        }
        return result;
    }
    //endregion  日志

}
