package com.wolfking.jeesite.ms.tmall.sd.service;

import com.google.common.collect.Maps;
import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2b.common.B2BTmallConstant;
import com.kkl.kklplus.entity.b2b.common.B2BWorkcardStatus;
import com.kkl.kklplus.entity.b2b.common.FourTuple;
import com.kkl.kklplus.entity.b2b.order.WorkcardStatusUpdate;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.entity.b2bcenter.md.B2BSurchargeCategoryMapping;
import com.kkl.kklplus.entity.b2bcenter.md.B2BSurchargeItemMapping;
import com.kkl.kklplus.entity.b2bcenter.pb.MQB2BOrderStatusUpdateMessage;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderActionEnum;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderCompletedItem;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderStatusEnum;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderTransferResult;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.md.entity.Engineer;
import com.wolfking.jeesite.modules.md.entity.Product;
import com.wolfking.jeesite.modules.md.entity.ProductCompletePicItem;
import com.wolfking.jeesite.modules.md.service.ServicePointService;
import com.wolfking.jeesite.modules.sd.dao.OrderItemDao;
import com.wolfking.jeesite.modules.sd.entity.*;
import com.wolfking.jeesite.modules.sd.service.OrderAuxiliaryMaterialService;
import com.wolfking.jeesite.modules.sd.service.OrderItemCompleteService;
import com.wolfking.jeesite.modules.sd.utils.OrderItemUtils;
import com.wolfking.jeesite.modules.sd.utils.OrderPicUtils;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.ms.b2bcenter.md.utils.B2BMDUtils;
import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderStatusUpdateReqEntity;
import com.wolfking.jeesite.ms.b2bcenter.sd.service.B2BOrderManualBaseService;
import com.wolfking.jeesite.ms.tmall.sd.feign.WorkcardFeign;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.mockito.internal.util.collections.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Slf4j
public class TmallOrderService extends B2BOrderManualBaseService {

    @Autowired
    private WorkcardFeign workcardFeign;

    @Autowired
    private OrderAuxiliaryMaterialService orderAuxiliaryMaterialService;

    @Autowired
    private OrderItemCompleteService orderItemCompleteService;

    @Autowired
    private ServicePointService servicePointService;

    //region 工单状态变更

    public MSResponse updateWorkcardProcessStatus(WorkcardStatusUpdate workcardStatusUpdate) {
        return workcardFeign.updateWorkcardProcessStatus(workcardStatusUpdate);
    }

    /**
     * 取消工单转换
     */
    public MSResponse cancelOrderTransition(B2BOrderTransferResult b2BOrderTransferResult) {
        return workcardFeign.cancelOrderTransition(b2BOrderTransferResult);
    }

    /**
     * 直接取消工单转换
     */
    public MSResponse directlyCancelOrderTransition(B2BOrderTransferResult b2BOrderTransferResult) {
        return workcardFeign.directCancel(b2BOrderTransferResult);
    }

    /**
     * 忽略工单转换
     */
    public MSResponse ignoreOrderTransition(B2BOrderTransferResult b2BOrderTransferResult) {
        return workcardFeign.ignoreCancel(b2BOrderTransferResult);
    }

    /**
     * 批量更新工单的异常标记
     */
    public MSResponse updateAbnormalOrderFlagAll() {
        return workcardFeign.updateAbnormalOrderFlagAll();
    }

    /**
     * 批量更新工单的路由标记
     */
    public MSResponse updateOrderRoutingFlagAll() {
        return workcardFeign.updateSystemIdAll();
    }

    /**
     * 往天猫微服务发送工单状态更新命令
     *
     * @param message
     * @return
     */
    public MSResponse sendOrderStatusUpdateCommandToB2B(MQB2BOrderStatusUpdateMessage.B2BOrderStatusUpdateMessage message) {
        MSResponse response = null;
        WorkcardStatusUpdate workcardStatusUpdate = new WorkcardStatusUpdate();
        workcardStatusUpdate.setUniqueId(message.getMessageId());
        workcardStatusUpdate.setB2bOrderId(message.getB2BOrderId());
        workcardStatusUpdate.setWorkcardId(message.getB2BOrderNo());
        workcardStatusUpdate.setType(B2BTmallConstant.FIXED_VALUE_WORKCARDTYPE);
        workcardStatusUpdate.setUpdater(message.getUpdaterName());
        workcardStatusUpdate.setProcessUpdateDate(message.getUpdateDt());
        workcardStatusUpdate.setCreateById(message.getUpdaterId());
        workcardStatusUpdate.setUpdateById(message.getUpdaterId());
        workcardStatusUpdate.setActionType(message.getActionType());
        workcardStatusUpdate.setKklOrderId(message.getOrderId());

        workcardStatusUpdate.setKklOrderNo(message.getKklOrderNO());
        if (message.getStatus() == B2BOrderStatusEnum.APPOINTED.value) {
            workcardStatusUpdate.setStatus(B2BWorkcardStatus.WORKCARD_STATUS_PLANNED.value);
            workcardStatusUpdate.setServiceDate(message.getEffectiveDt());
            workcardStatusUpdate.setWorkerName(message.getEngineerName());
            workcardStatusUpdate.setWorkerMobile(message.getEngineerMobile());
            response = updateWorkcardProcessStatus(workcardStatusUpdate);
        } else if (message.getStatus() == B2BOrderStatusEnum.APPLIED_FOR_CANCEL.value) {//TODO: 天猫退单前需要使用预约接口来传递时间
            workcardStatusUpdate.setStatus(B2BWorkcardStatus.WORKCARD_STATUS_PLANNED.value);
            workcardStatusUpdate.setServiceDate(message.getEffectiveDt());
            workcardStatusUpdate.setWorkerName(message.getEngineerName());
            workcardStatusUpdate.setWorkerMobile(message.getEngineerMobile());
            response = updateWorkcardProcessStatus(workcardStatusUpdate);
        }
//        else if (message.getStatus() == B2BOrderStatusEnum.SERVICED.value) {
//            workcardStatusUpdate.setStatus(B2BWorkcardStatus.WORKCARD_STATUS_SERVICED.value);
//            response = updateWorkcardProcessStatus(workcardStatusUpdate);
//        }
        else if (message.getStatus() == B2BOrderStatusEnum.COMPLETED.value) {
            workcardStatusUpdate.setStatus(B2BWorkcardStatus.WORKCARD_STATUS_COMPLETED.value);
            workcardStatusUpdate.setCompleteDate(message.getEffectiveDt());

            List<MQB2BOrderStatusUpdateMessage.CompletedItem> list = message.getCompletedItemList();
            List<B2BOrderCompletedItem> completedItems = Lists.newArrayList();
            if (list != null && list.size() > 0) {
                B2BOrderCompletedItem completedItem;
                B2BOrderCompletedItem.B2BSurchargeItem surchargeItem;
                for (MQB2BOrderStatusUpdateMessage.CompletedItem item : list) {
                    completedItem = new B2BOrderCompletedItem();
                    completedItem.setB2bProductCode(item.getItemCode());
                    completedItem.setPic1(item.getPic1());
                    completedItem.setPic2(item.getPic2());
                    completedItem.setPic3(item.getPic3());
                    completedItem.setPic4(item.getPic4());
                    completedItem.setUnitBarcode(item.getBarcode());
                    completedItem.setOutBarcode(item.getOutBarcode());
//                    for (MQB2BOrderStatusUpdateMessage.B2BSurchargeItem innerItem : item.getSurchargeItemsList()) {
//                        surchargeItem = new B2BOrderCompletedItem.B2BSurchargeItem();
//                        surchargeItem.setCategoryId(innerItem.getCategoryId());
//                        surchargeItem.setCategoryName(innerItem.getCategoryName());
//                        surchargeItem.setItemId(innerItem.getItemId());
//                        surchargeItem.setItemName(innerItem.getItemName());
//                        surchargeItem.setItemQty(innerItem.getItemQty());
//                        surchargeItem.setUnitPrice(innerItem.getUnitPrice());
//                        surchargeItem.setTotalPrice(innerItem.getTotalPrice());
//                        completedItem.getSurchargeItems().add(surchargeItem);
//                    }
                    completedItems.add(completedItem);
                }
            }
            workcardStatusUpdate.setCompletedItems(completedItems);

            response = updateWorkcardProcessStatus(workcardStatusUpdate);
        } else if (message.getStatus() == B2BOrderStatusEnum.CANCELED.value) {
            workcardStatusUpdate.setRemarks(message.getRemarks());
            if (message.getActionType() == B2BOrderActionEnum.CONVERTED_CANCEL.value) {
                // 按顺序调用派单失败、上门服务失败接口
                workcardStatusUpdate.setStatus(B2BWorkcardStatus.WORKCARD_STATUS_PLAN_FAILURE.value);
                response = updateWorkcardProcessStatus(workcardStatusUpdate);
                if (MSResponse.isSuccessCode(response)) {
                    workcardStatusUpdate.setStatus(B2BWorkcardStatus.WORKCARD_STATUS_SERVICE_FAILURE.value);
                    response = updateWorkcardProcessStatus(workcardStatusUpdate);
                }
            } else {
                workcardStatusUpdate.setStatus(B2BWorkcardStatus.WORKCARD_STATUS_SERVICE_FAILURE.value);
                response = updateWorkcardProcessStatus(workcardStatusUpdate);
            }
        }  else if (message.getStatus() == B2BOrderStatusEnum.PLANNED.value) {
            workcardStatusUpdate.setStatus(B2BWorkcardStatus.WORKCARD_STATUS_ASSIGN_WORKER.value);
            workcardStatusUpdate.setWorkerName(message.getEngineerName());
            workcardStatusUpdate.setWorkerMobile(message.getEngineerMobile());
            response = updateWorkcardProcessStatus(workcardStatusUpdate);
        } else if (message.getStatus() == B2BOrderStatusEnum.SERVICED.value) {
            workcardStatusUpdate.setStatus(B2BWorkcardStatus.WORKCARD_STATUS_SIGN_IN.value);
            response = updateWorkcardProcessStatus(workcardStatusUpdate);
        } else {
            response = new MSResponse(MSErrorCode.SUCCESS);
            String msgJson = new JsonFormat().printToString(message);
            LogUtils.saveLog("B2B工单状态变更消息的状态错误", "TmallOrderService.sendOrderStatusUpdateCommandToB2B", msgJson, null, null);
        }
        return response;
    }

    //endregion 工单状态变更

    //region 工单状态检查

    /**
     * 批量检查工单是否可转换
     *
     * @param b2bOrders
     * @return
     */
    public MSResponse checkB2BOrderProcessFlag(List<B2BOrderTransferResult> b2bOrders) {
        return workcardFeign.checkWorkcardProcessFlag(b2bOrders);
    }

    //endregion 工单状态检查


    //region 更新工单转换状态

    /**
     * 调用天猫微服务的B2B工单转换进度更新接口
     */
    public MSResponse sendB2BOrderConversionProgressUpdateCommandToB2B(List<B2BOrderTransferResult> progressList) {
        return workcardFeign.updateOrderTransferResult(Lists.newArrayList(progressList));
    }

    //endregion 更新工单转换状态

    //-------------------------------------------------------------------------------------------------创建状态变更请求实体

    /**
     * 创建天猫派单请求对象
     */
    public TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> createPlanRequestEntity(String engineerName, String engineerMobile) {
        TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> result = new TwoTuple<>(false, null);
        B2BOrderStatusUpdateReqEntity.Builder builder = new B2BOrderStatusUpdateReqEntity.Builder();
        builder.setEngineerName(StringUtils.trimToEmpty(engineerName))
                .setEngineerMobile(StringUtils.trimToEmpty(engineerMobile));
        result.setAElement(true);
        result.setBElement(builder);
        return result;
    }

    /**
     * 创建天猫上门请求对象
     */
    public TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> createServiceRequestEntity() {
        TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> result = new TwoTuple<>(false, null);
        B2BOrderStatusUpdateReqEntity.Builder builder = new B2BOrderStatusUpdateReqEntity.Builder();
        result.setAElement(true);
        result.setBElement(builder);
        return result;
    }

    /**
     * 创建天猫预约请求对象
     */
    public TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> createTmallAppointRequestEntity(Date appointmentDate, User updater, Long servicePointId, Long engineerId) {
        TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> result = new TwoTuple<>(false, null);
        if (appointmentDate != null && updater != null && StringUtils.isNotBlank(updater.getName())) {
            B2BOrderStatusUpdateReqEntity.Builder builder = new B2BOrderStatusUpdateReqEntity.Builder();
            builder.setEffectiveDate(appointmentDate)
                    .setUpdaterName(updater.getName());
            Engineer engineer = servicePointService.getEngineerFromCache(servicePointId, engineerId);
            if (engineer != null) {
                builder.setEngineerName(StringUtils.trimToEmpty(engineer.getName()));
                builder.setEngineerMobile(StringUtils.trimToEmpty(engineer.getContactInfo()));
            }
            result.setAElement(true);
            result.setBElement(builder);
        }
        return result;
    }

    /**
     * 创建天猫完工请求对象
     */
    public TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> createTmallCompleteRequestEntity(Date completeDate, User updater) {
        TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> result = new TwoTuple<>(false, null);
        if (completeDate != null && updater != null && StringUtils.isNotBlank(updater.getName())) {
            B2BOrderStatusUpdateReqEntity.Builder builder = new B2BOrderStatusUpdateReqEntity.Builder();
            builder.setEffectiveDate(completeDate)
                    .setUpdaterName(updater.getName());
            result.setAElement(true);
            result.setBElement(builder);
        }
        return result;
    }

    @Resource
    OrderItemDao orderItemDao;

    @Transactional()
    public TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> createTmallCompleteRequestEntityNew(Long orderId, String quarter, List<OrderItem> orderItems, Date completeDate, User updater) {
        TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> result = new TwoTuple<>(false, null);
        if (orderId != null && orderId > 0 && StringUtils.isNotBlank(quarter) && completeDate != null && updater != null && StringUtils.isNotBlank(updater.getName())) {
            if (orderItems == null || orderItems.isEmpty()) {
                Order order = orderItemDao.getOrderItems(quarter, orderId);
                if (order != null) {
                    //orderItems = OrderItemUtils.fromOrderItemsJson(order.getOrderItemJson());
                    orderItems = OrderItemUtils.pbToItems(order.getItemsPb());//2020-12-17 sd_order -> sd_order_head
                }
            }
            List<B2BOrderCompletedItem> completedItems = getB2BOrderCompletedItems(B2BDataSourceEnum.TMALL.id, orderId, quarter, orderItems);
            B2BOrderStatusUpdateReqEntity.Builder builder = new B2BOrderStatusUpdateReqEntity.Builder();
            builder.setEffectiveDate(completeDate)
                    .setUpdaterName(updater.getName())
                    .setOrderCompletedItems(completedItems);
            result.setAElement(true);
            result.setBElement(builder);
        }
        return result;
    }

    /**
     * 创建天猫取消请求对象
     */
    public TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> createTmallCancelRequestEntity(User updater, String remarks) {
        TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> result = new TwoTuple<>(false, null);
        if (updater != null && StringUtils.isNotBlank(updater.getName())) {
            B2BOrderStatusUpdateReqEntity.Builder builder = new B2BOrderStatusUpdateReqEntity.Builder();
            builder.setUpdaterName(updater.getName());
            builder.setRemarks(StringUtils.trimToEmpty(remarks));
            result.setAElement(true);
            result.setBElement(builder);
        }
        return result;
    }


    /**
     * @return FourTuple:产品ID、B2B产品编码、产品数量、套组中包含的具体产品ID集合
     */
    private List<FourTuple<Long, String, Integer, Set<Long>>> getB2BProductCodeMappings(List<OrderItem> orderItems) {
        List<FourTuple<Long, String, Integer, Set<Long>>> result = Lists.newArrayList();
        if (orderItems != null && !orderItems.isEmpty()) {
            List<Long> tempIds = orderItems.stream().map(OrderItem::getProductId).distinct().collect(Collectors.toList());
            Map<Long, Product> productMap = productService.getProductMap(tempIds);
            Product product;
            Long productId;
            Set<Long> pIdSet;
            FourTuple<Long, String, Integer, Set<Long>> tuple;
            for (OrderItem item : orderItems) {
                product = productMap.get(item.getProductId());
                if (product != null && product.getId() != null) {
                    pIdSet = Sets.newSet();
                    tuple = new FourTuple<>();
                    tuple.setAElement(item.getProductId());
                    tuple.setBElement(StringUtils.toString(item.getB2bProductCode()));
                    tuple.setCElement(item.getQty());
                    tuple.setDElement(pIdSet);

                    if (product.getSetFlag() == 1) {
                        String[] setIds = product.getProductIds().split(",");
                        for (String id : setIds) {
                            productId = StringUtils.toLong(id);
                            if (productId > 0) {
                                pIdSet.add(productId);
                            }
                        }
                    }
                    result.add(tuple);
                }
            }
        }
        return result;
    }

    public List<B2BOrderCompletedItem> getB2BOrderCompletedItems(Integer dataSourceId, Long orderId, String quarter, List<OrderItem> orderItems) {
        List<B2BOrderCompletedItem> result = Lists.newArrayList();
        if (orderId != null && orderId > 0 && StringUtils.isNotBlank(quarter) && orderItems != null && !orderItems.isEmpty()) {
            List<OrderItem> itemList = orderItems.stream().filter(i -> i.getProduct() != null && i.getProduct().getId() != null && i.getQty() != null).collect(Collectors.toList());
            if (!itemList.isEmpty()) {
                List<FourTuple<Long, String, Integer, Set<Long>>> b2BProductCodeMappings = getB2BProductCodeMappings(orderItems);
                List<OrderItemComplete> completedPicItems = orderItemCompleteService.getByOrderId(orderId, quarter);
                Map<Long, List<OrderItemComplete>> completedPicMap = Maps.newHashMap();
                Long productId;
                for (OrderItemComplete item : completedPicItems) {
                    item.setItemList(OrderUtils.fromProductCompletePicItemsJson(item.getPicJson()));
                    productId = item.getProduct().getId();
                    if (completedPicMap.containsKey(productId)) {
                        completedPicMap.get(productId).add(item);
                    } else {
                        completedPicMap.put(productId, Lists.newArrayList(item));
                    }
                }

//                List<AuxiliaryMaterial> auxiliaryMaterials = orderAuxiliaryMaterialService.getOrderAuxiliaryMaterialList(orderId, quarter);
//                Map<Long, B2BSurchargeCategoryMapping> surchargeCategoryMappingMap = B2BMDUtils.getB2BSurchargeCategoryMap(dataSourceId);
//                Map<Long, B2BSurchargeItemMapping> surchargeItemMappingMap = B2BMDUtils.getB2BSurchargeItemMap(dataSourceId);
//                Map<Long, List<B2BOrderCompletedItem.B2BSurchargeItem>> auxiliaryMaterialMap = Maps.newHashMap();
//                for (AuxiliaryMaterial item : auxiliaryMaterials) {
//                    productId = item.getProduct().getId();
//                    B2BOrderCompletedItem.B2BSurchargeItem surchargeItem = new B2BOrderCompletedItem.B2BSurchargeItem();
//                    surchargeItem.setCategoryId(item.getCategory().getId());
//                    B2BSurchargeCategoryMapping categoryMapping = surchargeCategoryMappingMap.get(item.getCategory().getId());
//                    if (categoryMapping != null) {
//                        surchargeItem.setCategoryName(categoryMapping.getB2bCategoryName());
//                    }
//                    surchargeItem.setItemId(item.getMaterial().getId());
//                    surchargeItem.setItemQty(item.getQty());
//                    surchargeItem.setUnitPrice(item.getMaterial().getPrice());
//                    surchargeItem.setTotalPrice(item.getSubtotal());
//                    B2BSurchargeItemMapping itemMapping = surchargeItemMappingMap.get(item.getMaterial().getId());
//                    if (itemMapping != null) {
//                        surchargeItem.setItemName(itemMapping.getB2bItemName());
//                    }
//                    if (auxiliaryMaterialMap.containsKey(productId)) {
//                        auxiliaryMaterialMap.get(productId).add(surchargeItem);
//                    } else {
//                        auxiliaryMaterialMap.put(productId, Lists.newArrayList(surchargeItem));
//                    }
//                }

                OrderItemComplete itemComplete;
                ProductCompletePicItem picItem;
                B2BOrderCompletedItem completedItem;
                for (FourTuple<Long, String, Integer, Set<Long>> item : b2BProductCodeMappings) {
                    List<OrderItemComplete> picItems = null;
                    if (completedPicMap.containsKey(item.getAElement())) {
                        picItems = completedPicMap.get(item.getAElement());
                    } else {
                        if (!item.getDElement().isEmpty()) {
                            for (Long pId : item.getDElement()) {
                                if (completedPicMap.containsKey(pId)) {
                                    picItems = completedPicMap.get(pId);
                                    break;
                                }
                            }
                        }
                    }

//                    List<B2BOrderCompletedItem.B2BSurchargeItem> surchargeItems = auxiliaryMaterialMap.get(item.getAElement());

                    int qty = item.getCElement();
                    if (picItems != null && picItems.size() > 0) {
                        qty = picItems.size();
                    }
                    qty = qty > 1 ? 1 : qty;

                    for (int i = 0; i < qty; i++) {
                        completedItem = new B2BOrderCompletedItem();
                        completedItem.setB2bProductCode(item.getBElement());

                        //设置完工图片、产品条码
                        if (picItems != null && picItems.size() > i) {
                            itemComplete = picItems.get(i);
                            Map<String, ProductCompletePicItem> picItemMap = itemComplete.getItemList().stream()
                                    .filter(p -> StringUtils.isNotBlank(p.getPictureCode()) && StringUtils.isNotBlank(p.getUrl()))
                                    .collect(Collectors.toMap(ProductCompletePicItem::getPictureCode, p -> p));

                            completedItem.setUnitBarcode(StringUtils.toString(itemComplete.getUnitBarcode()));
                            completedItem.setOutBarcode(StringUtils.toString(itemComplete.getOutBarcode()));
                            picItem = picItemMap.get("pic4");//条码图片
                            if (picItem != null && StringUtils.isNotBlank(picItem.getUrl())) {
                                completedItem.setPic4(OrderPicUtils.getOrderPicHostDir() + picItem.getUrl());
                            }
                            picItem = picItemMap.get("pic1");//现场图片
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
                        }

//                        if (i == 0 && surchargeItems != null && !surchargeItems.isEmpty()) {
//                            completedItem.setSurchargeItems(surchargeItems);
//                        }

                        result.add(completedItem);
                    }
                }


            }
        }
        return result;
    }

}
