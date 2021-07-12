package com.wolfking.jeesite.ms.um.sd.service;

import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.md.B2BCustomerMapping;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.entity.b2bcenter.pb.MQB2BOrderProcessLogMessage;
import com.kkl.kklplus.entity.b2bcenter.pb.MQB2BOrderStatusUpdateMessage;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderActionEnum;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderServiceItem;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderStatusEnum;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderTransferResult;
import com.kkl.kklplus.entity.um.mq.message.MQB2BUmOrderMessage;
import com.kkl.kklplus.entity.um.sd.UmOrderAuditCharged;
import com.kkl.kklplus.entity.um.sd.UmOrderProcessLog;
import com.kkl.kklplus.entity.um.sd.UmOrderServiceItem;
import com.kkl.kklplus.entity.um.sd.UmOrderStatusUpdate;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.md.entity.ServiceType;
import com.wolfking.jeesite.modules.md.service.ServiceTypeService;
import com.wolfking.jeesite.modules.sd.entity.*;
import com.wolfking.jeesite.modules.sd.service.OrderCacheReadService;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.ms.b2bcenter.md.utils.B2BMDUtils;
import com.wolfking.jeesite.ms.b2bcenter.sd.dao.B2BManualRetryDao;
import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderProcessEntity;
import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderProcessLogReqEntity;
import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderStatusUpdateReqEntity;
import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderVModel;
import com.wolfking.jeesite.ms.common.config.MicroServicesProperties;
import com.wolfking.jeesite.ms.um.mq.sender.UMPushOrderWebToMSMQSender;
import com.wolfking.jeesite.ms.um.sd.feign.UmOrderFeign;
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
public class UMOrderService {

    @Autowired
    private AreaService areaService;

    @Autowired
    private ServiceTypeService serviceTypeService;

    @Autowired
    private UMPushOrderWebToMSMQSender umPushOrderWebToMSMQSender;

    @Autowired
    private MicroServicesProperties microServicesProperties;

    @Autowired
    private UmOrderFeign umOrderFeign;

    @Resource
    private B2BManualRetryDao b2BManualRetryDao;


    /**
     * 只发送优盟的数据
     */
    private boolean isNeedSendOrderDataToMS(Integer dataSourceId, Long customerId) {
        List<Long> customerIds = microServicesProperties.getUm().getCustomerIds();
        if (microServicesProperties.getUm().getEnabled()
                && microServicesProperties.getUm().getOrderInfoEnabled()
//                && dataSourceId != null && dataSourceId != B2BDataSourceEnum.KKL.id
                && customerIds != null && !customerIds.isEmpty()
                && customerId != null && customerId > 0) {
            return customerIds.contains(customerId);
        }
        return false;
    }


    /**
     * 发送工单数据给优盟微服务
     */
    public void sendOrderDataToMS(Order order) {
        if (order != null && order.getOrderCondition() != null
                && order.getOrderCondition().getCustomer() != null
                && isNeedSendOrderDataToMS(order.getDataSourceId(), order.getOrderCondition().getCustomer().getId())) {
            MQB2BUmOrderMessage.B2BUmOrderMessage message = null;
            try {
                OrderCondition condition = order.getOrderCondition();
                OrderFee fee = order.getOrderFee();
                List<OrderItem> orderItems = order.getItems();
                long cityId = 0;
                long provinceId = 0;
                Area area = areaService.getFromCache(condition.getArea().getId(), Area.TYPE_VALUE_COUNTY);
                if (area != null && StringUtils.isNotBlank(area.getParentIds())) {
                    String[] parentIds = area.getParentIds().split(",");
                    if (parentIds.length == 4) {
                        provinceId = StringUtils.toLong(parentIds[2]);
                        cityId = StringUtils.toLong(parentIds[3]);
                    }
                }
                String shopId = order.getB2bShop() != null && StringUtils.isNotBlank(order.getB2bShop().getShopId()) ? order.getB2bShop().getShopId() : "";
                String shopName = "";
                if (StringUtils.isNotBlank(shopId)) {
                    TwoTuple<Map<String, B2BCustomerMapping>, Map<String, B2BCustomerMapping>> allCustomerMappingMaps = B2BMDUtils.getAllCustomerMappingMaps();
                    shopName = B2BMDUtils.getShopName(order.getDataSource().getIntValue(), shopId, allCustomerMappingMaps);
                }
                Map<Long, ServiceType> serviceTypeMap = serviceTypeService.getAllServiceTypeMap();
                MQB2BUmOrderMessage.B2BUmOrderMessage.Builder builder = MQB2BUmOrderMessage.B2BUmOrderMessage.newBuilder();
                builder.setOrderId(order.getId())
                        .setOrderNo(order.getOrderNo())
                        .setB2BOrderNo(StringUtils.toString(order.getWorkCardId()))
                        .setParentBizOrderId(StringUtils.toString(order.getParentBizOrderId()))
                        .setShopId(shopId)
                        .setShopName(shopName)
                        .setQuarter(order.getQuarter())
                        .setB2BDataSource(order.getDataSource().getIntValue())
                        .setDescription(order.getDescription())
                        .setUserName(condition.getUserName())
                        .setMobile(condition.getServicePhone())
                        .setAreaId(condition.getArea().getId())
                        .setCityId(cityId)
                        .setProvinceId(provinceId)
                        .setAddress(condition.getServiceAddress())
                        .setFeedbackId(condition.getFeedbackId())
                        .setBlockedCharge(fee.getBlockedCharge() + fee.getExpectCharge());
                MQB2BUmOrderMessage.UmOrderItem umOrderItem;
                ServiceType serviceType;
                for (OrderItem item : orderItems) {
                    serviceType = serviceTypeMap.get(item.getServiceType().getId());
                    umOrderItem = MQB2BUmOrderMessage.UmOrderItem.newBuilder()
                            .setDelFlag(item.getDelFlag())
                            .setStandPrice(item.getStandPrice())
                            .setDiscountPrice(item.getDiscountPrice())
                            .setCharge(item.getCharge())
                            .setServiceTypeId(item.getServiceType().getId())
                            .setProductId(item.getProductId())
                            .setQty(item.getQty())
                            .setWarranty(serviceType.getWarrantyStatus().getValue())
                            .setBrand(StringUtils.toString(item.getBrand()))
                            .build();
                    builder.addOrderItems(umOrderItem);
                }
                message = builder.build();
                umPushOrderWebToMSMQSender.send(message);
            } catch (Exception e) {
                if (message != null) {
                    String json = new JsonFormat().printToString(message);
                    log.error("生成客户流水数据失败，json:{}", json, e);
                    LogUtils.saveLog("UMOrderService.sendOrderDataToMS", "成功生成消息体，发送失败", json, e, null);
                } else {
                    LogUtils.saveLog("UMOrderService.sendOrderDataToMS", "生成消息体失败", "orderId:" + order.getId(), e, null);
                }
            }
        }
    }


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
                    .setDataSourceId(B2BDataSourceEnum.UM.id);
            result.setAElement(true);
            result.setBElement(builder);
        }
        return result;
    }


    /**
     * 往优盟微服务推送工单日志
     */
    public MSResponse pushOrderProcessLogToMS(MQB2BOrderProcessLogMessage.B2BOrderProcessLogMessage message) {
        UmOrderProcessLog orderProcessLog = new UmOrderProcessLog();
        orderProcessLog.setOrderId(message.getOrderId());
        orderProcessLog.setProcesslogUser(message.getOperatorName());
        orderProcessLog.setProcesslogTime(message.getLogDt());
        orderProcessLog.setProcesslog(message.getLogContext());
        orderProcessLog.setNum(message.getId());
        orderProcessLog.setCreateBy(message.getCreateById());
        orderProcessLog.setCreateDate(message.getCreateDt());
        MSResponse response = umOrderFeign.saveProcesslog(orderProcessLog);
        return response;
    }

    //endregion 创建工单日志消息实体


    public TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> createChargeRequestEntity(Long kklOrderId, String kklQuarter, Long chargeAt) {
        TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> result = new TwoTuple<>(false, null);
        try {
            Order order = orderCacheReadService.getOrderById(kklOrderId, kklQuarter, OrderUtils.OrderDataLevel.DETAIL, true, true);
            if (order != null) {
                double customerTotalCharge = 0.0;
                List<B2BOrderServiceItem> serviceItems = Lists.newArrayList();
                if (order.getDetailList() != null && order.getDetailList().size() > 0) {

                    B2BOrderServiceItem serviceItem;
                    for (OrderDetail detail : order.getDetailList()) {
                        serviceItem = new B2BOrderServiceItem();
                        serviceItem.setServiceItemId(detail.getId() != null ? detail.getId() : 0);
                        serviceItem.setServiceAt(detail.getCreateDate() != null ? detail.getCreateDate().getTime() : 0);
                        serviceItem.setProductId(detail.getProductId() != null ? detail.getProductId() : 0);
                        serviceItem.setServiceTypeId(detail.getServiceType() != null && detail.getServiceType().getId() != null ? detail.getServiceType().getId() : 0);
                        serviceItem.setQty(detail.getQty());
                        serviceItem.setCharge(detail.getCustomerCharge() != null ? detail.getCustomerCharge() : 0.0);
                        customerTotalCharge = customerTotalCharge + serviceItem.getCharge();
                        serviceItems.add(serviceItem);
                    }
                }
                B2BOrderStatusUpdateReqEntity.Builder builder = new B2BOrderStatusUpdateReqEntity.Builder();
                builder.setServiceItems(serviceItems)
                        .setCustomerTotalCharge(customerTotalCharge)
                        .setChargeAt(chargeAt);
                result.setAElement(true);
                result.setBElement(builder);
            }
        } catch (Exception e) {
            LogUtils.saveLog("创建B2B操作请求实体", "UMOrderService.createChargeRequestEntity", "kklOrderId: " + kklOrderId + ";" + "创建优盟对账请求实体失败.", e, null);
        }
        return result;
    }


    public MSResponse sendOrderStatusUpdateCommandToB2B(MQB2BOrderStatusUpdateMessage.B2BOrderStatusUpdateMessage message) {
        MSResponse response = null;
        if (message.getStatus() == B2BOrderStatusEnum.CHARGED.value) {
            List<MQB2BOrderStatusUpdateMessage.ServiceItem> serviceItems = message.getServiceItemList();
            List<UmOrderServiceItem> umOrderServiceItems = Lists.newArrayList();
            if (serviceItems != null && serviceItems.size() > 0) {
                UmOrderServiceItem umOrderServiceItem;
                for (MQB2BOrderStatusUpdateMessage.ServiceItem item : serviceItems) {
                    umOrderServiceItem = new UmOrderServiceItem();
                    umOrderServiceItem.setServiceDate(item.getServiceAt());
                    umOrderServiceItem.setProductId(item.getProductId());
                    umOrderServiceItem.setServiceTypeId(item.getServiceTypeId());
                    umOrderServiceItem.setQty(item.getQty());
                    umOrderServiceItem.setCharge(item.getCharge());
                    umOrderServiceItems.add(umOrderServiceItem);
                }
            }
            response = umOrderCharged(message.getOrderId(), umOrderServiceItems, message.getCustomerTotalCharge(), message.getChargeAt());
        } else {
            response = new MSResponse(MSErrorCode.SUCCESS);
            String msgJson = new JsonFormat().printToString(message);
            LogUtils.saveLog("B2B工单状态变更消息的状态错误", "UmOrderService.sendOrderStatusUpdateCommandToB2B", msgJson, null, null);
        }
        return response;
    }


    private MSResponse umOrderCharged(Long kklOrderId, List<UmOrderServiceItem> serviceItems, Double totalCharge, Long chargeAt) {
        UmOrderAuditCharged charged = new UmOrderAuditCharged();
        charged.setKklOrderId(kklOrderId);
        charged.setServiceItems(serviceItems);
        charged.setTotalCharge(totalCharge);
        charged.setChargeDate(chargeAt);
        return umOrderFeign.auditCharged(charged);
    }

    //region 工单状态检查

    /**
     * 批量检查工单是否可转换
     */
    public MSResponse checkB2BOrderProcessFlag(List<B2BOrderTransferResult> b2bOrderNos) {
        return umOrderFeign.checkWorkcardProcessFlag(b2bOrderNos);
    }

    //endregion 工单状态检查

    /**
     * 取消工单转换
     */
    public MSResponse cancelOrderTransition(B2BOrderTransferResult b2BOrderTransferResult) {
        return umOrderFeign.cancelOrderTransition(b2BOrderTransferResult);
    }
    //region 更新转单进度

    /**
     * 调用微服务的B2B工单转换进度更新接口
     */
    public MSResponse sendB2BOrderConversionProgressUpdateCommandToB2B(List<B2BOrderTransferResult> progressList) {
        return umOrderFeign.updateTransferResult(Lists.newArrayList(progressList));
    }

    //endregion 更新转单进度


    /**
     * 更新优盟微服务数据库中的工单状态
     */
    public MSResponse updateOrderStatus(Long kklOrderId, B2BOrderStatusEnum statusEnum, Long closeDt) {
        int status = UmOrderStatusUpdate.STATUS_NEW;
        switch (statusEnum) {
            case NEW:
                status = UmOrderStatusUpdate.STATUS_NEW;
                break;
            case PLANNED:
                status = UmOrderStatusUpdate.STATUS_PLANNED;
                break;
            case APPOINTED:
                status = UmOrderStatusUpdate.STATUS_APPOINTED;
                break;
            case SERVICED:
                status = UmOrderStatusUpdate.STATUS_SERVICED;
                break;
            case COMPLETED:
                status = UmOrderStatusUpdate.STATUS_COMPLETED;
                break;
            case CANCELED:
                status = UmOrderStatusUpdate.STATUS_CANCELLED;
                break;
        }
        UmOrderStatusUpdate params = new UmOrderStatusUpdate();
        params.setKklOrderId(kklOrderId);
        params.setOrderStatus(status);
        params.setCloseDate(closeDt);
        return umOrderFeign.statusUpdate(params);
    }

    /**
     * UM的客户ID：1482、2751
     */
    public void retrySendOrderData(Date beginDate, Date endDate, String quarter) {
        List<OrderCondition> conditions = b2BManualRetryDao.listOrders(quarter, Lists.newArrayList(1482L, 2751L), beginDate, endDate);

        for (OrderCondition c : conditions) {
            if (c.getStatusValue() == Order.ORDER_STATUS_CHARGED) {
                Order order = orderCacheReadService.getOrderById(c.getOrderId(), c.getQuarter(), OrderUtils.OrderDataLevel.DETAIL, true, true);
                if (order != null) {
                    double customerTotalCharge = 0.0;
                    List<UmOrderServiceItem> umOrderServiceItems = Lists.newArrayList();
                    if (order.getDetailList() != null && order.getDetailList().size() > 0) {
                        UmOrderServiceItem umOrderServiceItem;
                        for (OrderDetail detail : order.getDetailList()) {
                            umOrderServiceItem = new UmOrderServiceItem();
                            umOrderServiceItem.setServiceDate(detail.getCreateDate() != null ? detail.getCreateDate().getTime() : 0);
                            umOrderServiceItem.setProductId(detail.getProductId() != null ? detail.getProductId() : 0);
                            umOrderServiceItem.setServiceTypeId(detail.getServiceType() != null && detail.getServiceType().getId() != null ? detail.getServiceType().getId() : 0);
                            umOrderServiceItem.setQty(detail.getQty());
                            umOrderServiceItem.setCharge(detail.getCustomerCharge() != null ? detail.getCustomerCharge() : 0.0);
                            customerTotalCharge = customerTotalCharge + umOrderServiceItem.getCharge();
                            umOrderServiceItems.add(umOrderServiceItem);
                        }
                    }
                    long chargeDt = order.getOrderStatus() != null && order.getOrderStatus().getChargeDate() != null ? order.getOrderStatus().getChargeDate().getTime() : 0;
                    umOrderCharged(c.getOrderId(), umOrderServiceItems, customerTotalCharge, chargeDt);
                }
            } else {
                int status = UmOrderStatusUpdate.STATUS_NEW;
                switch (c.getStatusValue()) {
                    case 40:
                        status = UmOrderStatusUpdate.STATUS_PLANNED;
                        break;
                    case 50:
                        status = UmOrderStatusUpdate.STATUS_SERVICED;
                        break;
                    case 80:
                        status = UmOrderStatusUpdate.STATUS_COMPLETED;
                        break;
                    case 90:
                    case 100:
                        status = UmOrderStatusUpdate.STATUS_CANCELLED;
                        break;
                }
                UmOrderStatusUpdate params = new UmOrderStatusUpdate();
                params.setKklOrderId(c.getOrderId());
                params.setOrderStatus(status);
                long closeDt = c.getCloseDate() != null ? c.getCloseDate().getTime() : 0;
                params.setCloseDate(closeDt);
                umOrderFeign.statusUpdate(params);
            }
        }
    }


    /**
     * UM的客户ID：1482、2751
     */
    public void retrySendOrderData(List<Long> orderIds) {
        for (Long orderId : orderIds) {
            Order order = orderCacheReadService.getOrderById(orderId, null, OrderUtils.OrderDataLevel.DETAIL, true, true);
            if (order != null && order.getOrderCondition() != null) {
                if (order.getOrderCondition().getStatusValue() == Order.ORDER_STATUS_CHARGED) {
                    double customerTotalCharge = 0.0;
                    List<UmOrderServiceItem> umOrderServiceItems = Lists.newArrayList();
                    if (order.getDetailList() != null && order.getDetailList().size() > 0) {
                        UmOrderServiceItem umOrderServiceItem;
                        for (OrderDetail detail : order.getDetailList()) {
                            umOrderServiceItem = new UmOrderServiceItem();
                            umOrderServiceItem.setServiceDate(detail.getCreateDate() != null ? detail.getCreateDate().getTime() : 0);
                            umOrderServiceItem.setProductId(detail.getProductId() != null ? detail.getProductId() : 0);
                            umOrderServiceItem.setServiceTypeId(detail.getServiceType() != null && detail.getServiceType().getId() != null ? detail.getServiceType().getId() : 0);
                            umOrderServiceItem.setQty(detail.getQty());
                            umOrderServiceItem.setCharge(detail.getCustomerCharge() != null ? detail.getCustomerCharge() : 0.0);
                            customerTotalCharge = customerTotalCharge + umOrderServiceItem.getCharge();
                            umOrderServiceItems.add(umOrderServiceItem);
                        }
                    }
                    long chargeDt = order.getOrderStatus() != null && order.getOrderStatus().getChargeDate() != null ? order.getOrderStatus().getChargeDate().getTime() : 0;
                    umOrderCharged(orderId, umOrderServiceItems, customerTotalCharge, chargeDt);
                } else {
                    int status = UmOrderStatusUpdate.STATUS_NEW;
                    switch (order.getOrderCondition().getStatusValue()) {
                        case 40:
                            status = UmOrderStatusUpdate.STATUS_PLANNED;
                            break;
                        case 50:
                            status = UmOrderStatusUpdate.STATUS_SERVICED;
                            break;
                        case 80:
                            status = UmOrderStatusUpdate.STATUS_COMPLETED;
                            break;
                        case 90:
                        case 100:
                            status = UmOrderStatusUpdate.STATUS_CANCELLED;
                            break;
                    }
                    UmOrderStatusUpdate params = new UmOrderStatusUpdate();
                    params.setKklOrderId(orderId);
                    params.setOrderStatus(status);
                    long closeDt = order.getOrderCondition().getCloseDate() != null ? order.getOrderCondition().getCloseDate().getTime() : 0;
                    params.setCloseDate(closeDt);
                    umOrderFeign.statusUpdate(params);
                }
            }
            else {
                log.error("你好吗：orderId:[{}]", orderId);
            }
        }
    }


    @Autowired
    private OrderCacheReadService orderCacheReadService;

    @Autowired
    private OrderService orderService;

    /**
     * 取消工单
     */
    private boolean cancelKKLOrder(B2BOrderProcessEntity entity) {
        boolean result = false;
        Order order = orderCacheReadService.getOrderById(entity.getKklOrderId(), null, OrderUtils.OrderDataLevel.CONDITION, true, true);
        if (order != null && order.getOrderCondition() != null) {
            String remarks = StringUtils.toString(entity.getRemarks()) + "（" + B2BDataSourceEnum.UM.name + "平台通知取消B2B工单）";
            if (order.getOrderCondition().getStatusValue() <= Order.ORDER_STATUS_ACCEPTED) {
                orderService.cancelOrderNew(order.getId(), B2BOrderVModel.b2bUser, remarks, true);
                B2BOrderProcessEntity.saveFailureLog(entity, "优盟主动取消工单", "UMOrderService.cancelKKLOrder", null);
            } else if (order.getOrderCondition().getStatusValue() < Order.ORDER_STATUS_RETURNING) {
                orderService.returnOrderNew(order.getId(), new Dict("51", "厂家(电商)通知取消"), "", remarks, B2BOrderVModel.b2bUser);
                B2BOrderProcessEntity.saveFailureLog(entity, "优盟主动申请退单", "UMOrderService.cancelKKLOrder", null);
            } else {
                B2BOrderProcessEntity.saveFailureLog(entity, "优盟主动申请退单，但工单状态不允许取消或申请退单", "UMOrderService.cancelKKLOrder", null);
            }
            result = true;
        } else {
            B2BOrderProcessEntity.saveFailureLog(entity, "读取工单失败", "UMOrderService.cancelKKLOrder", null);
        }
        return result;
    }

    public MSResponse processKKLOrder(B2BOrderProcessEntity processEntity) {
        boolean flag = false;
        try {
            if (processEntity.getActionType() == B2BOrderActionEnum.CONVERTED_CANCEL) {
                flag = cancelKKLOrder(processEntity);
            }
        } catch (Exception e) {
            B2BOrderProcessEntity.saveFailureLog(processEntity, "修改快可立工单", "UMOrderService.processKKLOrder", e);
        }
        return new MSResponse(flag ? MSErrorCode.SUCCESS : MSErrorCode.FAILURE);
    }

}
