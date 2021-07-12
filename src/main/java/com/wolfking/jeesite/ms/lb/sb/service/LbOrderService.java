package com.wolfking.jeesite.ms.lb.sb.service;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2b.common.B2BBase;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.entity.b2bcenter.md.B2BModifyOperationEnum;
import com.kkl.kklplus.entity.b2bcenter.pb.MQB2BOrderStatusUpdateMessage;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderActionEnum;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderStatusEnum;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderTransferResult;
import com.kkl.kklplus.entity.canbo.sd.CanboOrderCompleted;
import com.kkl.kklplus.entity.common.material.B2BMaterial;
import com.kkl.kklplus.entity.lb.sd.LbOrderCancelApply;
import com.kkl.kklplus.entity.lb.sd.LbOrderCancelAudit;
import com.kkl.kklplus.entity.lb.sd.LbOrderCompleteApply;
import com.kkl.kklplus.entity.lb.sd.LbOrderStatus;
import com.kkl.kklplus.entity.xyyplus.sd.XYYOrderCancelAudit;
import com.kkl.kklplus.utils.NumberUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.GsonUtils;
import com.wolfking.jeesite.modules.md.entity.Engineer;
import com.wolfking.jeesite.modules.md.entity.ProductCompletePicItem;
import com.wolfking.jeesite.modules.md.service.ServicePointService;
import com.wolfking.jeesite.modules.sd.dao.OrderItemDao;
import com.wolfking.jeesite.modules.sd.entity.*;
import com.wolfking.jeesite.modules.sd.service.OrderCacheReadService;
import com.wolfking.jeesite.modules.sd.service.OrderItemCompleteService;
import com.wolfking.jeesite.modules.sd.utils.OrderItemUtils;
import com.wolfking.jeesite.modules.sd.utils.OrderPicUtils;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.VisibilityFlagEnum;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderModifyEntity;
import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderProcessEntity;
import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderStatusUpdateReqEntity;
import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderVModel;
import com.wolfking.jeesite.ms.b2bcenter.sd.service.B2BOrderManualBaseService;
import com.wolfking.jeesite.ms.lb.sb.feign.LbOrderFeign;
import com.wolfking.jeesite.ms.material.mq.entity.mapper.B2BMaterialMapper;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderActionEnum.CONVERTED_CANCEL;
import static com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderActionEnum.RETURN_REASSIGN;
import static com.wolfking.jeesite.modules.sys.entity.VisibilityFlagEnum.*;

@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Slf4j
public class LbOrderService extends B2BOrderManualBaseService {

    @Autowired
    private LbOrderFeign lbOrderFeign;

    @Resource
    private OrderItemDao orderItemDao;

    @Autowired
    private OrderItemCompleteService orderItemCompleteService;

    private static B2BMaterialMapper mapper = Mappers.getMapper(B2BMaterialMapper.class);

    //region 工单转换

    /**
     * 取消工单转换
     */
    public MSResponse cancelOrderTransition(B2BOrderTransferResult b2BOrderTransferResult) {
        return lbOrderFeign.cancelOrderTransition(b2BOrderTransferResult);
    }

    /**
     * 批量检查工单是否可转换
     */
    public MSResponse checkB2BOrderProcessFlag(List<B2BOrderTransferResult> b2bOrderNos) {
        return lbOrderFeign.checkWorkcardProcessFlag(b2bOrderNos);
    }

    /**
     * 调用微服务的B2B工单转换进度更新接口
     */
    public MSResponse sendB2BOrderConversionProgressUpdateCommandToB2B(List<B2BOrderTransferResult> progressList) {
        return lbOrderFeign.updateTransferResult(Lists.newArrayList(progressList));
    }

    //endregion 工单转换

    private void setB2BBaseProperties(B2BBase entity, long createById, long createDt) {
        if (entity != null) {
            entity.setCreateById(createById);
            entity.setUpdateById(createById);
            Date date = new Date(createDt);
            entity.setCreateDate(date);
            entity.setUpdateDate(date);
        }
    }

    //region 取消工单

    public MSResponse applyOrderCancel(Long b2bOrderId, String b2bOrderNo, String remarks, Long createById, Long createDt) {
        LbOrderCancelApply apply = new LbOrderCancelApply();
        apply.setB2bOrderId(b2bOrderId);
        apply.setOrderNumber(b2bOrderNo);
        apply.setRemark(remarks);
        setB2BBaseProperties(apply, createById, createDt);
        return lbOrderFeign.orderCancelApply(apply);
    }

    private MSResponse updateApplyFlag(Long messageId) {
        return lbOrderFeign.processApplyFlag(messageId);
    }

    private LbOrderCancelAudit createApproveOrderCancelReqEntity(B2BOrderProcessEntity processEntity, Integer reviewStatus, String remarks) {
        LbOrderCancelAudit audit = new LbOrderCancelAudit();
        audit.setB2bOrderId(processEntity.getB2bOrderId());
        audit.setOrderNumber(processEntity.getB2bOrderNo());
        audit.setReviewStatus(reviewStatus);
        audit.setRemark(remarks);
        setB2BBaseProperties(audit, B2BOrderVModel.b2bUser.getId(), System.currentTimeMillis());
        return audit;
    }

    private MSResponse approveOrderCancel(LbOrderCancelAudit audit) {
        return lbOrderFeign.orderCancelAudit(audit);
    }

    private MSResponse updateAuditFlag(Long messageId) {
        return lbOrderFeign.processAuditFlag(messageId);
    }


    //endregion 取消工单

    //region 工单状态

    public MSResponse changeOrderStatus(Long b2bOrderId, String b2bOrderNo, String status, String updaterName, String updaterMobile, Long createById, Long createDt) {
        LbOrderStatus orderStatus = new LbOrderStatus();
        orderStatus.setB2bOrderId(b2bOrderId);
        orderStatus.setOrderNumber(b2bOrderNo);
        orderStatus.setStatus(status);
        orderStatus.setPrincipalName(updaterName);
        orderStatus.setPrincipalPhone(updaterMobile);
        setB2BBaseProperties(orderStatus, createById, createDt);
        return lbOrderFeign.orderStatus(orderStatus);
    }

    //endregion 工单状态


    //region 工单完成

    public MSResponse completeOrder(Long b2bOrderId, String b2bOrderNo, String picUrls, String remarks, double feePrice, Long createById, Long createDt) {
        LbOrderCompleteApply completeApply = new LbOrderCompleteApply();
        completeApply.setB2bOrderId(b2bOrderId);
        completeApply.setOrderNumber(b2bOrderNo);
        completeApply.setAnnex(picUrls);
        completeApply.setRemark(remarks);
        completeApply.setFeePrice(NumberUtils.formatDouble(feePrice, 2));
        setB2BBaseProperties(completeApply, createById, createDt);
        return lbOrderFeign.orderCompleteApply(completeApply);
    }

    //endregion 工单完成


    //region 工单状态变更

    /**
     * 往微服务发送工单状态更新命令
     */
    public MSResponse sendOrderStatusUpdateCommandToB2B(MQB2BOrderStatusUpdateMessage.B2BOrderStatusUpdateMessage message) {
        MSResponse response;
        if (message.getStatus() == B2BOrderStatusEnum.PLANNED.value) {
            response = changeOrderStatus(message.getB2BOrderId(), message.getB2BOrderNo(), "派单", message.getEngineerName(), message.getEngineerMobile(), message.getUpdaterId(), message.getUpdateDt());
        } else if (message.getStatus() == B2BOrderStatusEnum.APPOINTED.value) {
            response = changeOrderStatus(message.getB2BOrderId(), message.getB2BOrderNo(), "预约", message.getEngineerName(), message.getEngineerMobile(), message.getUpdaterId(), message.getUpdateDt());
        } else if (message.getStatus() == B2BOrderStatusEnum.APPLIED_FOR_CANCEL.value) {
            response = applyOrderCancel(message.getB2BOrderId(), message.getB2BOrderNo(), message.getRemarks(), message.getUpdaterId(), message.getUpdateDt());
        } else if (message.getStatus() == B2BOrderStatusEnum.COMPLETED.value) {
            List<MQB2BOrderStatusUpdateMessage.CompletedItem> messageCompletedItemList = message.getCompletedItemList();
            String picUrlsString = "";
            if (messageCompletedItemList != null && messageCompletedItemList.size() > 0) {
                MQB2BOrderStatusUpdateMessage.CompletedItem item = messageCompletedItemList.get(0);
                List<String> picUrls = Lists.newArrayList();
                picUrls.add(item.getPic1());
                picUrls.add(item.getPic2());
                picUrls.add(item.getPic3());
                picUrls.add(item.getPic4());
                picUrls = picUrls.stream().filter(StringUtils::isNotBlank).collect(Collectors.toList());
                picUrlsString = StringUtils.join(picUrls, ",");
            }
            response = completeOrder(message.getB2BOrderId(), message.getB2BOrderNo(), picUrlsString, message.getRemarks(), message.getOrderCharge(), message.getUpdaterId(), message.getUpdateDt());
        } else {
            response = new MSResponse(MSErrorCode.SUCCESS);
            String msgJson = new JsonFormat().printToString(message);
            LogUtils.saveLog("B2B工单状态变更消息的状态错误", "XYYPlusOrderService.sendOrderStatusUpdateCommandToB2B", msgJson, null, null);
        }
        return response;
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
     * 派单请求对象
     */
    public TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> createPlanRequestEntity(String engineerName, String engineerMobile) {
        TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> result = new TwoTuple<>(false, null);
        if (StringUtils.isNotBlank(engineerName) && StringUtils.isNotBlank(engineerMobile)) {
            B2BOrderStatusUpdateReqEntity.Builder builder = new B2BOrderStatusUpdateReqEntity.Builder();
            builder.setEngineerName(engineerName)
                    .setEngineerMobile(engineerMobile);
            result.setAElement(true);
            result.setBElement(builder);
        }
        return result;
    }

    @Autowired
    ServicePointService servicePointService;

    /**
     * 预约请求对象
     */
    public TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> createAppointRequestEntity(Date appointmentDate, Long servicePointId, Long engineerId) {
        TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> result = new TwoTuple<>(false, null);
        if (appointmentDate != null && servicePointId != null && servicePointId > 0 && engineerId != null && engineerId > 0) {
            Engineer engineer = servicePointService.getEngineerFromCache(servicePointId, engineerId);
            if (engineer != null && StringUtils.isNotBlank(engineer.getName()) && StringUtils.isNotBlank(engineer.getContactInfo())) {
                B2BOrderStatusUpdateReqEntity.Builder builder = new B2BOrderStatusUpdateReqEntity.Builder();
                builder.setEffectiveDate(appointmentDate)
                        .setEngineerName(engineer.getName())
                        .setEngineerMobile(engineer.getContactInfo());
                result.setAElement(true);
                result.setBElement(builder);
            }
        }
        return result;
    }

    /**
     * 创建退单申请请求对象
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
     * 创建同望完工请求对象
     */
    @Transactional()
    public TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> createCompleteRequestEntity(Long orderId, String quarter, List<OrderItem> orderItems, Double orderCharge) {
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
            builder.setCompletedItems(completedItems)
                    .setOrderCharge(orderCharge);
            result.setAElement(true);
            result.setBElement(builder);
        }
        return result;
    }


    //region 编辑、处理快可立工单

    @Autowired
    private OrderCacheReadService orderCacheReadService;

    /**
     * 修改快可立工单
     */
    public MSResponse modifyKKLOrderByXYYPlus(B2BOrderModifyEntity modifyEntity) {
        boolean flag = false;
        try {
            if (modifyEntity.getOperationType() == B2BModifyOperationEnum.XYY_MODIFY_ORDER) {
                flag = modifyKKLOrder(modifyEntity);
            } else if (modifyEntity.getOperationType() == B2BModifyOperationEnum.XYY_WRITE_EXPRESS) {
                flag = writeExpressInfoToKKLOrder(modifyEntity);
            } else {
                flag = true;
            }
        } catch (Exception e) {
            B2BOrderModifyEntity.saveFailureLog(modifyEntity, "修改快可立工单", "SuningOrderService.ModifyKKLOrderBySuning", e);
        }
        return new MSResponse(flag ? MSErrorCode.SUCCESS : MSErrorCode.FAILURE);
    }

    private boolean writeExpressInfoToKKLOrder(B2BOrderModifyEntity entity) {
        boolean flag = false;
        Order order = orderCacheReadService.getOrderById(entity.getKklOrderId(), null, OrderUtils.OrderDataLevel.CONDITION, true, true);
        if (order != null) {
            List<String> expressInfo = Lists.newArrayList();
            expressInfo.add("快递公司：" + StringUtils.toString(entity.getExpressCompany()));
            expressInfo.add("快递单号：" + StringUtils.toString(entity.getExpressNo()));
            if (entity.getDeliveryTime() > 0) {
                expressInfo.add("发货时间：" + DateUtils.formatDateString(entity.getDeliveryTime()));
            }
            //仅记入工单日志
            String originalUserInfoStr = StringUtils.join(expressInfo, "、");
            OrderProcessLog processLog = new OrderProcessLog();
            processLog.setQuarter(order.getQuarter());
            processLog.setOrderId(order.getId());
            processLog.setAction("B2B客户修改工单快递信息");
            String actionComment = StringUtils.left(String.format("修改订单:%s,修改人:%s, 修改工单快递信息：%s",
                    order.getOrderNo(), B2BOrderVModel.b2bUser.getName(), originalUserInfoStr), 250);
            processLog.setActionComment(actionComment);
            String status = MSDictUtils.getDictLabel(order.getOrderCondition().getStatus().getValue(), "order_status", "订单已审核");
            processLog.setStatus(status);
            processLog.setStatusValue(Integer.parseInt((order.getOrderCondition().getStatus().getValue())));
            processLog.setStatusFlag(OrderProcessLog.OPL_SF_NOT_CHANGE_STATUS);
            processLog.setCloseFlag(0);
            processLog.setCreateBy(B2BOrderVModel.b2bUser);
            processLog.setCreateDate(new Date());
            processLog.setCustomerId(order.getOrderCondition() != null ? order.getOrderCondition().getCustomerId() : 0);
            processLog.setDataSourceId(order.getDataSourceId());
            int visibilityValue = VisibilityFlagEnum.or(Sets.newHashSet(KEFU, CUSTOMER, SERVICE_POINT));
            processLog.setVisibilityFlag(visibilityValue);
            orderService.saveOrderProcessLogWithNoCalcVisibility(processLog);
            flag = true;

        } else {
            B2BOrderModifyEntity.saveFailureLog(entity, "读取工单失败", "XYYPlusOrderService.writeExpressInfoToKKLOrder", null);
        }
        return flag;
    }

    /**
     * 取消工单
     */
    private boolean cancelKKLOrder(B2BOrderProcessEntity entity) {
        boolean result = false;
        Order order = orderCacheReadService.getOrderById(entity.getKklOrderId(), null, OrderUtils.OrderDataLevel.CONDITION, true, true);
        if (order != null && order.getOrderCondition() != null) {
            String remarks = StringUtils.toString(entity.getRemarks()) + "（" + B2BDataSourceEnum.XYINGYAN.name + "平台通知取消B2B工单）";
            if (order.getOrderCondition().getStatusValue() <= Order.ORDER_STATUS_ACCEPTED) {
                orderService.cancelOrderNew(order.getId(), B2BOrderVModel.b2bUser, remarks, false);
                result = true;
            }
        } else {
            B2BOrderProcessEntity.saveFailureLog(entity, "读取工单失败", "B2BOrderManualBaseService.cancelKKLOrder", null);
        }
        return result;
    }

    private boolean cancelKKLOrder(B2BOrderProcessEntity entity, B2BOrderActionEnum action) {
        boolean result = false;
        Order order = orderCacheReadService.getOrderById(entity.getKklOrderId(), null, OrderUtils.OrderDataLevel.CONDITION, true, true);
        if (order != null && order.getOrderCondition() != null) {
            String remarks = StringUtils.toString(entity.getRemarks()) + "（" + B2BDataSourceEnum.XYINGYAN.name + "平台主动申请退单）";
            if (order.getOrderCondition().getStatusValue() < Order.ORDER_STATUS_COMPLETED) {
                int cancelResponseValue = (action == CONVERTED_CANCEL ? OrderStatus.CANCEL_RESPONSIBLE_CUSTOMER_APPLY_RETURN : OrderStatus.CANCEL_RESPONSIBLE_B2B_REASSIGN);
//                Dict cancelResponseDict = (action == CONVERTED_CANCEL ? new Dict("51", "厂家(电商)通知取消") : new Dict("501", "厂家(电商)改派退单"));
                Dict cancelResponseDict = MSDictUtils.getDictByValue(cancelResponseValue + "", Dict.DICT_TYPE_CANCEL_RESPONSIBLE);
//                orderService.returnOrderNew(order.getId(), cancelResponseDict, remarks, B2BOrderVModel.b2bUser);
                orderService.b2bCancelOrder(order.getId(), order.getQuarter(), cancelResponseDict, remarks, B2BOrderVModel.b2bUser);
                result = true;
            }
        } else {
            B2BOrderProcessEntity.saveFailureLog(entity, "读取工单失败", "XYYPlusOrderService.cancelKKLOrder", null);
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
            String remarks = StringUtils.toString(entity.getRemarks()) + "（" + B2BDataSourceEnum.XYINGYAN.name + "平台审核B2B工单的退单申请）";
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
            B2BOrderProcessEntity.saveFailureLog(entity, "读取工单失败", "XYYPlusOrderService.approveReturnKKLOrder", null);
        }
//        if (order != null && order.getOrderCondition() != null) {
//            String remarks = StringUtils.toString(entity.getRemarks()) + "（" + B2BDataSourceEnum.XYINGYAN.name + "平台审核B2B工单的退单申请）";
//            orderService.approveReturnOrderNew(order.getId(), order.getQuarter(), remarks, B2BOrderVModel.b2bUser);
//            result = true;
//        } else {
//            B2BOrderProcessEntity.saveFailureLog(entity, "读取工单失败", "XYYPlusOrderService.approveReturnKKLOrder", null);
//        }
        return result;
    }


    /**
     * 退单申请驳回
     */
    private boolean rejectReturnKKLOrder(B2BOrderProcessEntity entity) {
        boolean result = false;
        Order order = orderCacheReadService.getOrderById(entity.getKklOrderId(), null, OrderUtils.OrderDataLevel.CONDITION, true, true);
        if (order != null && order.getOrderCondition() != null) {
            String remarks = StringUtils.toString(entity.getRemarks()) + "（" + B2BDataSourceEnum.XYINGYAN.name + "平台驳回B2B工单的退单申请）";
            orderService.rejectReturnOrderNew(order.getId(), order.getQuarter(), remarks, B2BOrderVModel.b2bUser);
            result = true;
        } else {
            B2BOrderProcessEntity.saveFailureLog(entity, "读取工单失败", "XYYPlusOrderService.rejectReturnKKLOrder", null);
        }
        return result;
    }


    public MSResponse processKKLOrder(B2BOrderProcessEntity processEntity) {
        boolean flag = false;
        try {
            if (processEntity.getActionType() == CONVERTED_CANCEL || processEntity.getActionType() == RETURN_REASSIGN) {//新迎燕主动申请取消工单
                updateApplyFlag(processEntity.getMessageId());
                flag = cancelKKLOrder(processEntity, processEntity.getActionType());
            } else if (processEntity.getActionType() == B2BOrderActionEnum.RETURN_APPROVE) {//新迎燕审核退单审核
                updateAuditFlag(processEntity.getMessageId());
                if (processEntity.getStatus() == XYYOrderCancelAudit.REVIEW_STATUS_SUCCESS.intValue()) {
                    flag = approveReturnKKLOrder(processEntity);
                } else if (processEntity.getStatus() == XYYOrderCancelAudit.REVIEW_STATUS_FAILURE.intValue()) {
                    flag = rejectReturnKKLOrder(processEntity);
                }
            }
        } catch (Exception e) {
            B2BOrderProcessEntity.saveFailureLog(processEntity, "修改快可立工单", "SuningOrderService.ModifyKKLOrderBySuning", e);
        }
        if (processEntity.getActionType() == CONVERTED_CANCEL || processEntity.getActionType() == RETURN_REASSIGN) {
            int reviewStatus = flag ? XYYOrderCancelAudit.REVIEW_STATUS_SUCCESS : XYYOrderCancelAudit.REVIEW_STATUS_FAILURE;
            String remarks = flag ? "" : "工单不可取消";
            int cancelResponseValue = (processEntity.getActionType() == CONVERTED_CANCEL ? OrderStatus.CANCEL_RESPONSIBLE_CUSTOMER_APPLY_RETURN : OrderStatus.CANCEL_RESPONSIBLE_B2B_REASSIGN);
            LbOrderCancelAudit audit = createApproveOrderCancelReqEntity(processEntity, reviewStatus, remarks);
            audit.setCancelType(cancelResponseValue);
            MSResponse response = approveOrderCancel(audit);
            if (!MSResponse.isSuccessCode(response)) {
                String logJson = GsonUtils.toGsonString(audit);
                LogUtils.saveLog("审核B2B新迎燕的工单取消申请", "XYYPlusOrderService.processKKLOrder", logJson, null, null);
            }
        }

        return new MSResponse(flag ? MSErrorCode.SUCCESS : MSErrorCode.FAILURE);
    }

    //endregion 编辑、处理快可立工单

    //region 配件单

    /**
     * 申请配件单
     */
    public MSResponse newMaterial(MaterialMaster materialMaster) {
        try {
            B2BMaterial materialForm = mapper.toB2BMaterialForm(materialMaster);
            if (materialForm == null) {
                return new MSResponse<>(MSErrorCode.newInstance(MSErrorCode.FAILURE, "配件单转新迎燕配件单错误"));
            }
            materialForm.setApplyType(materialMaster.getApplyType().getIntValue());
            materialForm.setB2bOrderId(materialMaster.getB2bOrderId());
            return lbOrderFeign.newMaterial(materialForm);
        } catch (Exception e) {
            log.error("orderId:{} ", materialMaster.getOrderId(), e);
            return new MSResponse<>(MSErrorCode.newInstance(MSErrorCode.FAILURE, "微服务接口执行失败"));
        }
    }

    /**
     * 处理完"审核"消息回调通知微服务
     */
    public MSResponse notifyApplyFlag(Long formId) {
        return lbOrderFeign.updateAuditFlag(formId);
    }

    /**
     * 处理完"已发货"消息回调通知微服务
     */
    public MSResponse notifyDeliverFlag(Long formId) {
        return lbOrderFeign.updateDeliverFlag(formId);
    }


    //endregion   配件单
}
