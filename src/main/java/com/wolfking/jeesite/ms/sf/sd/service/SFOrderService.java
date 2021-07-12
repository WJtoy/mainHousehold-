package com.wolfking.jeesite.ms.sf.sd.service;

import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.entity.b2bcenter.pb.MQB2BOrderStatusUpdateMessage;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderActionEnum;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderCompletedItem;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderStatusEnum;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderTransferResult;
import com.kkl.kklplus.entity.sf.sd.SfOrderHandle;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.md.entity.Engineer;
import com.wolfking.jeesite.modules.md.entity.ProductCompletePicItem;
import com.wolfking.jeesite.modules.md.service.ServicePointService;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.entity.OrderItemComplete;
import com.wolfking.jeesite.modules.sd.entity.TwoTuple;
import com.wolfking.jeesite.modules.sd.service.OrderCacheReadService;
import com.wolfking.jeesite.modules.sd.service.OrderItemCompleteService;
import com.wolfking.jeesite.modules.sd.service.OrderLocationService;
import com.wolfking.jeesite.modules.sd.service.OrderMaterialService;
import com.wolfking.jeesite.modules.sd.utils.OrderPicUtils;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.ms.b2bcenter.mq.sender.B2BCenterOrderDismountReturnMQSender;
import com.wolfking.jeesite.ms.b2bcenter.sd.dao.B2BOrderDao;
import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderProcessEntity;
import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderStatusUpdateReqEntity;
import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderVModel;
import com.wolfking.jeesite.ms.b2bcenter.sd.service.B2BOrderManualBaseService;
import com.wolfking.jeesite.ms.praise.service.OrderPraiseService;
import com.wolfking.jeesite.ms.providermd.service.*;
import com.wolfking.jeesite.ms.sf.sd.feign.SFOrderFeign;
import com.wolfking.jeesite.ms.validate.service.MSOrderValidateService;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configurable
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Slf4j
public class SFOrderService extends B2BOrderManualBaseService {

    private static final User KKL_SF_B2B_USER = new User(0L, "快可立全国联保", "075729235666");

    @Autowired
    private SFOrderFeign sfOrderFeign;
    @Autowired
    private OrderCacheReadService orderCacheReadService;
    @Autowired
    private OrderItemCompleteService orderItemCompleteService;
    @Autowired
    private AreaService areaService;

    //region 工单转换

    /**
     * 取消工单转换
     */
    public MSResponse cancelOrderTransition(B2BOrderTransferResult b2BOrderTransferResult) {
        return sfOrderFeign.cancelOrderTransition(b2BOrderTransferResult);
    }

    /**
     * 批量检查工单是否可转换
     */
    public MSResponse checkB2BOrderProcessFlag(List<B2BOrderTransferResult> b2bOrderNos) {
        return sfOrderFeign.checkWorkcardProcessFlag(b2bOrderNos);
    }

    /**
     * 调用微服务的B2B工单转换进度更新接口
     */
    public MSResponse sendB2BOrderConversionProgressUpdateCommandToB2B(List<B2BOrderTransferResult> progressList) {
        return sfOrderFeign.updateTransferResult(Lists.newArrayList(progressList));
    }

    //endregion 工单转换

    /**
     * 批量更新工单的路由标记
     */
    public MSResponse updateOrderRoutingFlagAll() {
        return sfOrderFeign.updateSystemIdAll();
    }

    //region 工单处理

    /**
     * 派单
     */
    private MSResponse<Integer> orderPlan(MQB2BOrderStatusUpdateMessage.B2BOrderStatusUpdateMessage message) {
        SfOrderHandle params = new SfOrderHandle();
        params.setUniqueId(message.getMessageId());
        params.setB2bOrderId(message.getB2BOrderId());
        params.setInstallMaster(message.getEngineerName());
        params.setInstallContact(message.getEngineerMobile());
        params.setCreateById(message.getUpdaterId());
        params.setCreateDt(message.getUpdateDt());
        return sfOrderFeign.planned(params);
    }

    /**
     * 预约
     */
    private MSResponse<Integer> orderAppoint(MQB2BOrderStatusUpdateMessage.B2BOrderStatusUpdateMessage message) {
        SfOrderHandle params = new SfOrderHandle();
        params.setUniqueId(message.getMessageId());
        params.setB2bOrderId(message.getB2BOrderId());
        params.setAppTime(message.getEffectiveDt());
        params.setInstallMaster(message.getEngineerName());
        params.setInstallContact(message.getEngineerMobile());
        params.setContent(message.getRemarks());
        params.setCreateById(message.getUpdaterId());
        params.setCreateDt(message.getUpdateDt());
        return sfOrderFeign.appointment(params);
    }

    /**
     * 完成
     */
    private MSResponse<Integer> orderComplete(MQB2BOrderStatusUpdateMessage.B2BOrderStatusUpdateMessage message) {
        SfOrderHandle params = new SfOrderHandle();
        params.setUniqueId(message.getMessageId());
        params.setB2bOrderId(message.getB2BOrderId());
        List<String> picUrls = Lists.newArrayList();
        if (message.getCompletedItemCount() > 0) {
            List<String> subPicUrls;
            for (MQB2BOrderStatusUpdateMessage.CompletedItem innerItem: message.getCompletedItemList()) {
                subPicUrls = innerItem.getPicItemList().stream().map(MQB2BOrderStatusUpdateMessage.PicItem::getUrl).collect(Collectors.toList());
                if (!subPicUrls.isEmpty()) {
                    picUrls.addAll(subPicUrls);
                }
            }
            params.setPics(picUrls);
        }
        params.setCreateById(message.getUpdaterId());
        params.setCreateDt(message.getUpdateDt());
        return sfOrderFeign.finish(params);
    }

    /**
     * 取消
     */
    private MSResponse orderApplyForCancel(MQB2BOrderStatusUpdateMessage.B2BOrderStatusUpdateMessage message) {
        SfOrderHandle params = new SfOrderHandle();
        params.setUniqueId(message.getMessageId());
        params.setB2bOrderId(message.getB2BOrderId());
        params.setCity(message.getExtraField1());
        params.setContent(message.getRemarks());
        params.setCreateById(message.getUpdaterId());
        params.setCreateDt(message.getUpdateDt());
        return sfOrderFeign.cancel(params);
    }

    /**
     * 往微服务发送工单状态更新命令
     */
    public MSResponse sendOrderStatusUpdateCommandToB2B(MQB2BOrderStatusUpdateMessage.B2BOrderStatusUpdateMessage message) {
        MSResponse response = null;
        if (message.getStatus() == B2BOrderStatusEnum.PLANNED.value) {
            response = orderPlan(message);
        } else if (message.getStatus() == B2BOrderStatusEnum.APPOINTED.value) {
            response = orderAppoint(message);
        } else if (message.getStatus() == B2BOrderStatusEnum.COMPLETED.value) {
            response = orderComplete(message);
        } else if (message.getStatus() == B2BOrderStatusEnum.APPLIED_FOR_CANCEL.value) {
            response = orderApplyForCancel(message);
        }  else {
            response = new MSResponse<>(MSErrorCode.SUCCESS);
            String msgJson = new JsonFormat().printToString(message);
            LogUtils.saveLog("B2B工单状态变更消息的状态错误", "SFOrderService.sendOrderStatusUpdateCommandToB2B", msgJson, null, null);
        }
        return response;
    }

    /**
     * 创建派单请求对象
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
     * 创建预约请求对象
     */
    public TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> createAppointRequestEntity(Date appointmentDate, Long servicePointId, Long engineerId, User updater, String remarks) {
        TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> result = new TwoTuple<>(false, null);
        if (updater != null && appointmentDate != null) {
            Engineer engineer = servicePointService.getEngineerFromCache(servicePointId, engineerId);
            B2BOrderStatusUpdateReqEntity.Builder builder = new B2BOrderStatusUpdateReqEntity.Builder();
            builder.setUpdaterName(updater.getName())
                    .setEffectiveDate(appointmentDate)
                    .setRemarks(StringUtils.toString(remarks));
            if (engineer != null) {
                builder.setEngineerName(engineer.getName())
                        .setEngineerMobile(engineer.getContactInfo());
            }
            result.setAElement(true);
            result.setBElement(builder);
        }
        return result;
    }

    /**
     * 创建App完工请求对象
     */
    @Transactional()
    public TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> createCompleteRequestEntity(Long orderId, String quarter) {
        TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> result = new TwoTuple<>(false, null);
        if (orderId != null && orderId > 0) {
            List<B2BOrderCompletedItem> completedItems = getOrderCompletedItems(orderId, quarter);
            B2BOrderStatusUpdateReqEntity.Builder builder = new B2BOrderStatusUpdateReqEntity.Builder();
            builder.setOrderCompletedItems(completedItems);
            result.setAElement(true);
            result.setBElement(builder);
        }
        return result;
    }


    private List<B2BOrderCompletedItem> getOrderCompletedItems(Long orderId, String quarter) {
        List<B2BOrderCompletedItem> result = Lists.newArrayList();
        if (orderId != null && orderId > 0) {
            Map<Long, B2BOrderCompletedItem> completedItemMap = Maps.newConcurrentMap();
            List<OrderItemComplete> completeList = orderItemCompleteService.getByOrderId(orderId, quarter);
            B2BOrderCompletedItem completedItem;
            if (completeList != null && !completeList.isEmpty()) {
                for (OrderItemComplete item : completeList) {
                    if (completedItemMap.containsKey(item.getProduct().getId())) {
                        completedItem = completedItemMap.get(item.getProduct().getId());
                    } else {
                        completedItem = new B2BOrderCompletedItem();
                        completedItemMap.put(item.getProduct().getId(), completedItem);
                    }

                    item.setItemList(OrderUtils.fromProductCompletePicItemsJson(item.getPicJson()));
                    B2BOrderCompletedItem.PicItem newPicItem;
                    for (ProductCompletePicItem innerItem : item.getItemList()) {
                        if (StringUtils.isNotEmpty(innerItem.getPictureCode()) && StringUtils.isNotEmpty(innerItem.getUrl())) {
                            newPicItem = new B2BOrderCompletedItem.PicItem();
                            newPicItem.setCode(innerItem.getPictureCode());
                            newPicItem.setUrl(OrderPicUtils.getOrderPicHostDir() + innerItem.getUrl());
                            completedItem.getPicItems().add(newPicItem);
                        }
                    }
                }
                result.addAll(completedItemMap.values());
            }
        }
        return result;
    }

    /**
     * 创建退单申请请求对象
     */
    public TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> createApplyForCancelRequestEntity(String areaFullName, Long areaId, String remarks) {
        TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> result = new TwoTuple<>(false, null);
        B2BOrderStatusUpdateReqEntity.Builder builder = new B2BOrderStatusUpdateReqEntity.Builder();
        Area area = areaService.getFromCache(areaId, Area.TYPE_VALUE_COUNTY);
        String cityFullName = null;
        String[] areaComponents;
        if (area != null && StringUtils.isNotBlank(area.getFullName())) {
            areaComponents = StrUtil.split(area.getFullName(), " ");
            if (areaComponents.length >= 3) {
                cityFullName = StrUtil.join("", areaComponents[0], areaComponents[1]);
            } else {
                cityFullName = StrUtil.replace(area.getFullName(), " ", "");
            }
        }
        if (StrUtil.isEmpty(cityFullName)) {
            cityFullName = areaFullName;
        }
        builder.setRemarks(StringUtils.toString(remarks))
                .setExtraField1(StringUtils.toString(cityFullName));
        result.setAElement(true);
        result.setBElement(builder);
        return result;
    }

    //endregion 工单处理


    //region 处理快可立工单

    /**
     * 取消工单
     */
    private boolean cancelKKLOrder(B2BOrderProcessEntity entity) {
        boolean result = false;
        Order order = orderCacheReadService.getOrderById(entity.getKklOrderId(), null, OrderUtils.OrderDataLevel.CONDITION, true, true);
        if (order != null && order.getOrderCondition() != null) {
            String remarks = StringUtils.toString(entity.getRemarks()) + "（" + B2BDataSourceEnum.SF.name + "平台通知取消B2B工单）";
            if (order.getOrderCondition().getStatusValue() <= Order.ORDER_STATUS_ACCEPTED) {
                orderService.cancelOrderNew(order.getId(), B2BOrderVModel.b2bUser, remarks, false);
                result = true;
            } else if (order.getOrderCondition().getStatusValue() == Order.ORDER_STATUS_RETURNING) {
                orderService.approveReturnOrderNew(order.getId(), order.getQuarter(), remarks, B2BOrderVModel.b2bUser);
                result = true;
            } else if (order.getOrderCondition().getStatusValue() < Order.ORDER_STATUS_RETURNING) {
//                orderService.returnOrderNew(order.getId(), new Dict("51", "厂家(电商)通知取消"), "", remarks, B2BOrderVModel.b2bUser);
                orderService.b2bCancelOrder(order.getId(), order.getQuarter(), new Dict("51", "厂家(电商)通知取消"), remarks, B2BOrderVModel.b2bUser);
                result = true;
            }
        } else {
            B2BOrderProcessEntity.saveFailureLog(entity, "读取工单失败", "SFOrderService.cancelKKLOrder", null);
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
            B2BOrderProcessEntity.saveFailureLog(processEntity, "SFOrderService", "processKKLOrder", e);
        }
        return new MSResponse(flag ? MSErrorCode.SUCCESS : MSErrorCode.FAILURE);
    }

    //endregion 处理快可立工单


}
