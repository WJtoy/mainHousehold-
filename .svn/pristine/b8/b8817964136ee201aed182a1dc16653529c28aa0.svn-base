package com.wolfking.jeesite.ms.konka.sd.service;

import com.google.common.collect.Maps;
import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.pb.MQB2BOrderStatusUpdateMessage;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderStatusEnum;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderTransferResult;
import com.kkl.kklplus.entity.canbo.sd.CanboOrderCompleted;
import com.kkl.kklplus.entity.konka.sd.*;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.md.entity.Engineer;
import com.wolfking.jeesite.modules.md.entity.ProductCompletePicItem;
import com.wolfking.jeesite.modules.md.service.ServicePointService;
import com.wolfking.jeesite.modules.sd.dao.OrderItemDao;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.entity.OrderItem;
import com.wolfking.jeesite.modules.sd.entity.OrderItemComplete;
import com.wolfking.jeesite.modules.sd.entity.TwoTuple;
import com.wolfking.jeesite.modules.sd.service.OrderItemCompleteService;
import com.wolfking.jeesite.modules.sd.utils.OrderItemUtils;
import com.wolfking.jeesite.modules.sd.utils.OrderPicUtils;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderStatusUpdateReqEntity;
import com.wolfking.jeesite.ms.b2bcenter.sd.service.B2BOrderManualBaseService;
import com.wolfking.jeesite.ms.konka.sd.feign.KonkaOrderFeign;
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
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class KonkaOrderService extends B2BOrderManualBaseService {

    @Resource
    private OrderItemDao orderItemDao;

    @Autowired
    private KonkaOrderFeign konkaOrderFeign;

    @Autowired
    OrderItemCompleteService orderItemCompleteService;

    @Autowired
    private ServicePointService servicePointService;

    /**
     * 取消工单转换
     */
    public MSResponse cancelOrderTransition(B2BOrderTransferResult b2BOrderTransferResult) {
        return konkaOrderFeign.cancelOrderTransition(b2BOrderTransferResult);
    }

    //region 工单状态检查

    /**
     * 批量检查工单是否可转换
     */
    public MSResponse checkB2BOrderProcessFlag(List<B2BOrderTransferResult> b2bOrderNos) {
        return konkaOrderFeign.checkWorkcardProcessFlag(b2bOrderNos);
    }

    //endregion 工单状态检查

    //region 更新转单进度

    /**
     * 调用天猫微服务的B2B工单转换进度更新接口
     */
    public MSResponse sendB2BOrderConversionProgressUpdateCommandToB2B(List<B2BOrderTransferResult> progressList) {
        return konkaOrderFeign.updateTransferResult(Lists.newArrayList(progressList));
    }

    //endregion 更新转单进度

    //region 工单状态变更

    /**
     * 派单
     */
    private MSResponse konkaOrderPlan(Long b2bOrderId, String b2bOrderNo, String engineerName, String engineerMobile, String remarks,
                                      long createById, long createDt) {
        KonkaOrderPlanned planned = new KonkaOrderPlanned();
        planned.setB2bOrderId(b2bOrderId);
        planned.setOrderId(b2bOrderNo);
        planned.setEngineerName(engineerName);
        planned.setEngineerMobile(engineerMobile);
        planned.setRemark(remarks);
        planned.setCreateDt(createDt);
        planned.setCreateById(createById);
        planned.setUpdateDt(createDt);
        planned.setUpdateById(createById);
        return konkaOrderFeign.orderPlanned(planned);
    }

    /**
     * 预约
     */
    private MSResponse konkaOrderAppoint(Long b2bOrderId, String b2bOrderNo, String appointedMan, Long appointmentDt, String remarks, long createById, long createDt) {
        KonKaOrderAppointed appointed = new KonKaOrderAppointed();
        appointed.setB2bOrderId(b2bOrderId);
        appointed.setOrderId(b2bOrderNo);
        appointed.setAppointedMan(appointedMan);
        appointed.setAppointedDateTime(appointmentDt);
        appointed.setRemark(remarks);
        appointed.setCreateDt(createDt);
        appointed.setCreateById(createById);
        appointed.setUpdateDt(createDt);
        appointed.setUpdateById(createById);
        return konkaOrderFeign.orderAppointed(appointed);
    }

    /**
     * 上门
     */
    private MSResponse konkaOrderVisit(Long b2bOrderId, String b2bOrderNo, String visitedMan, Long visitedDt, String remarks, long createById, long createDt) {
        KonkaOrderVisited visited = new KonkaOrderVisited();
        visited.setB2bOrderId(b2bOrderId);
        visited.setOrderId(b2bOrderNo);
        visited.setVisitMan(visitedMan);
        visited.setVisitDateTime(visitedDt);
        visited.setRemark(remarks);
        visited.setCreateDt(createDt);
        visited.setCreateById(createById);
        visited.setUpdateDt(createDt);
        visited.setUpdateById(createById);
        return konkaOrderFeign.orderVisited(visited);
    }

    /**
     * 完成
     */
    private MSResponse konkaOrderCompleted(Long b2bOrderId, String b2bOrderNo, List<String> picList, Long completedDt, String remarks, long createById, long createDt) {
        KonkaOrderCompleted completed = new KonkaOrderCompleted();
        completed.setB2bOrderId(b2bOrderId);
        completed.setOrderId(b2bOrderNo);
        completed.setPicList(picList);
        completed.setCompletedDateTime(completedDt);
        completed.setRemark(remarks);
        completed.setCreateDt(createDt);
        completed.setCreateById(createById);
        completed.setUpdateDt(createDt);
        completed.setUpdateById(createById);
        return konkaOrderFeign.orderCompleted(completed);
    }

    /**
     * 取消
     */
    private MSResponse konkaOrderCancelled(Long b2bOrderId, String b2bOrderNo, String cancelledMan, Long cancelledDt, String remark, long createById, long createDt) {
        KonkaOrderCancelled cancelled = new KonkaOrderCancelled();
        cancelled.setB2bOrderId(b2bOrderId);
        cancelled.setOrderId(b2bOrderNo);
        cancelled.setCancelledMan(cancelledMan);
        cancelled.setCancelledDateTime(cancelledDt);
        cancelled.setRemark(remark);
        cancelled.setCreateDt(createDt);
        cancelled.setCreateById(createById);
        cancelled.setUpdateDt(createDt);
        cancelled.setUpdateById(createById);
        return konkaOrderFeign.orderCancelled(cancelled);
    }

    /**
     * 往康佳微服务发送工单状态更新命令
     */
    public MSResponse sendOrderStatusUpdateCommandToB2B(MQB2BOrderStatusUpdateMessage.B2BOrderStatusUpdateMessage message) {
        MSResponse response;
        if (message.getStatus() == B2BOrderStatusEnum.PLANNED.value) {
            response = konkaOrderPlan(message.getB2BOrderId(), message.getB2BOrderNo(), message.getEngineerName(), message.getEngineerMobile(), message.getRemarks(), message.getUpdaterId(), message.getUpdateDt());
        } else if (message.getStatus() == B2BOrderStatusEnum.APPOINTED.value) {
            response = konkaOrderAppoint(message.getB2BOrderId(), message.getB2BOrderNo(), message.getUpdaterName(), message.getEffectiveDt(), message.getRemarks(), message.getUpdaterId(), message.getUpdateDt());
        } else if (message.getStatus() == B2BOrderStatusEnum.SERVICED.value) {
            response = konkaOrderVisit(message.getB2BOrderId(), message.getB2BOrderNo(), message.getEngineerName(), message.getEffectiveDt(), message.getRemarks(), message.getUpdaterId(), message.getUpdateDt());
        } else if (message.getStatus() == B2BOrderStatusEnum.COMPLETED.value) {
            List<MQB2BOrderStatusUpdateMessage.CompletedItem> itemList = message.getCompletedItemList();
            List<String> picList = Lists.newArrayList();
            List<String> tempList;
            if (itemList != null && itemList.size() > 0) {
                tempList = itemList.stream().filter(i -> StringUtils.isNotBlank(i.getPic1())).map(MQB2BOrderStatusUpdateMessage.CompletedItem::getPic1).collect(Collectors.toList());
                picList.addAll(tempList);
                tempList = itemList.stream().filter(i -> StringUtils.isNotBlank(i.getPic2())).map(MQB2BOrderStatusUpdateMessage.CompletedItem::getPic2).collect(Collectors.toList());
                picList.addAll(tempList);
                tempList = itemList.stream().filter(i -> StringUtils.isNotBlank(i.getPic3())).map(MQB2BOrderStatusUpdateMessage.CompletedItem::getPic3).collect(Collectors.toList());
                picList.addAll(tempList);
                tempList = itemList.stream().filter(i -> StringUtils.isNotBlank(i.getPic4())).map(MQB2BOrderStatusUpdateMessage.CompletedItem::getPic4).collect(Collectors.toList());
                picList.addAll(tempList);
            }
            response = konkaOrderCompleted(message.getB2BOrderId(), message.getB2BOrderNo(), picList, message.getEffectiveDt(), message.getRemarks(), message.getUpdaterId(), message.getUpdateDt());
        } else if (message.getStatus() == B2BOrderStatusEnum.CANCELED.value) {
            response = konkaOrderCancelled(message.getB2BOrderId(), message.getB2BOrderNo(), message.getUpdaterName(), message.getEffectiveDt(), message.getRemarks(), message.getUpdaterId(), message.getUpdateDt());
        } else {
            response = new MSResponse(MSErrorCode.SUCCESS);
            String msgJson = new JsonFormat().printToString(message);
            LogUtils.saveLog("B2B工单状态变更消息的状态错误", "KonkaOrderService.sendOrderStatusUpdateCommandToB2B", msgJson, null, null);
        }
        return response;
    }


    //endregion 工单状态变更

    //region 其他

    private List<CanboOrderCompleted.CompletedItem> getKonkaOrderCompletedItems(Long orderId, String quarter, List<OrderItem> orderItems) {
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
                            if (picItems.size() > 1) {
                                completedItem.setPic2(OrderPicUtils.getOrderPicHostDir() + picItems.get(1).getUrl());
                            }
                            if (picItems.size() > 2) {
                                completedItem.setPic3(OrderPicUtils.getOrderPicHostDir() + picItems.get(2).getUrl());
                            }
                            if (picItems.size() > 3) {
                                completedItem.setPic4(OrderPicUtils.getOrderPicHostDir() + picItems.get(3).getUrl());
                            }
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
     * 创建康佳派单请求对象
     */
    public TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> createPlanRequestEntity(String engineerName, String engineerMobile, String remarks) {
        TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> result = new TwoTuple<>(false, null);
        if (StringUtils.isNotBlank(engineerName) && StringUtils.isNotBlank(engineerMobile)) {
            B2BOrderStatusUpdateReqEntity.Builder builder = new B2BOrderStatusUpdateReqEntity.Builder();
            builder.setEngineerName(engineerName)
                    .setEngineerMobile(engineerMobile)
                    .setRemarks(StringUtils.toString(remarks));
            result.setAElement(true);
            result.setBElement(builder);
        }
        return result;
    }

    /**
     * 创建康佳预约请求对象
     */
    public TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> createAppointRequestEntity(Date appointmentDate, User updater, String remarks) {
        TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> result = new TwoTuple<>(false, null);
        if (updater != null && StringUtils.isNotBlank(updater.getName()) && appointmentDate != null) {
            B2BOrderStatusUpdateReqEntity.Builder builder = new B2BOrderStatusUpdateReqEntity.Builder();
            builder.setUpdaterName(updater.getName())
                    .setEffectiveDate(appointmentDate)
                    .setRemarks(StringUtils.toString(remarks));
            result.setAElement(true);
            result.setBElement(builder);
        }
        return result;
    }

    /**
     * 创建康佳上门请求对象
     */
    public TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> createServiceRequestEntity(Date visitedDate, Long servicePointId, Long engineerId, String remarks) {
        TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> result = new TwoTuple<>(false, null);
        if (visitedDate != null && servicePointId != null && servicePointId > 0 && engineerId != null && engineerId > 0) {
            Engineer engineer = servicePointService.getEngineerFromCache(servicePointId, engineerId);
            if (engineer != null && StringUtils.isNotBlank(engineer.getName()) && StringUtils.isNotBlank(engineer.getContactInfo())) {
                B2BOrderStatusUpdateReqEntity.Builder builder = new B2BOrderStatusUpdateReqEntity.Builder();
                builder.setEngineerName(engineer.getName())
                        .setEffectiveDate(visitedDate)
                        .setRemarks(StringUtils.toString(remarks));
                result.setAElement(true);
                result.setBElement(builder);
            }
        }
        return result;
    }

    /**
     * 创建康佳完工请求对象
     */
    @Transactional()
    public TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> createCompleteRequestEntity(Long orderId, String quarter, List<OrderItem> orderItems, Date effectiveDate, String remarks) {
        TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> result = new TwoTuple<>(false, null);
        if (orderId != null && orderId > 0 && StringUtils.isNotBlank(quarter)) {
            if (orderItems == null || orderItems.isEmpty()) {
                Order order = orderItemDao.getOrderItems(quarter, orderId);
                if (order != null) {
                    //orderItems = OrderItemUtils.fromOrderItemsJson(order.getOrderItemJson());
                    orderItems = OrderItemUtils.pbToItems(order.getItemsPb());//2020-12-17 sd_order -> sd_order_head
                }
            }
            List<CanboOrderCompleted.CompletedItem> completedItems = getKonkaOrderCompletedItems(orderId, quarter, orderItems);
            B2BOrderStatusUpdateReqEntity.Builder builder = new B2BOrderStatusUpdateReqEntity.Builder();
            builder.setCompletedItems(completedItems)
                    .setEffectiveDate(effectiveDate)
                    .setRemarks(StringUtils.toString(remarks));
            result.setAElement(true);
            result.setBElement(builder);
        }
        return result;
    }

    /**
     * 创建康佳取消请求对象
     */
    public TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> createCancelRequestEntity(Integer kklCancelType, Date cancelDate, User updater) {
        TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> result = new TwoTuple<>(false, null);
        String cancelResponsible = null;
        if (kklCancelType != null) {
            cancelResponsible = MSDictUtils.getDictLabel(kklCancelType.toString(), Dict.DICT_TYPE_CANCEL_RESPONSIBLE, "");
        }
        if (StringUtils.isNotBlank(cancelResponsible) && cancelDate != null) {
            B2BOrderStatusUpdateReqEntity.Builder builder = new B2BOrderStatusUpdateReqEntity.Builder();
            builder.setEffectiveDate(cancelDate)
                    .setUpdaterName(updater != null && StringUtils.isNotBlank(updater.getName()) ? updater.getName() : "")
                    .setRemarks(cancelResponsible);
            result.setAElement(true);
            result.setBElement(builder);
        }
        return result;
    }

}
