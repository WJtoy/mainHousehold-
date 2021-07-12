package com.wolfking.jeesite.ms.joyoung.sd.service;

import com.google.common.collect.Maps;
import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.entity.b2bcenter.pb.MQB2BOrderComplainProcessMessage;
import com.kkl.kklplus.entity.b2bcenter.pb.MQB2BOrderProcessLogMessage;
import com.kkl.kklplus.entity.b2bcenter.pb.MQB2BOrderReminderProcessMessage;
import com.kkl.kklplus.entity.b2bcenter.pb.MQB2BOrderStatusUpdateMessage;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderComplainProcess;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderPraiseItem;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderStatusEnum;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderTransferResult;
import com.kkl.kklplus.entity.canbo.sd.CanboOrderCompleted;
import com.kkl.kklplus.entity.common.material.B2BMaterial;
import com.kkl.kklplus.entity.common.material.B2BMaterialArrival;
import com.kkl.kklplus.entity.common.material.B2BMaterialClose;
import com.kkl.kklplus.entity.joyoung.sd.*;
import com.kkl.kklplus.entity.praise.Praise;
import com.kkl.kklplus.entity.praise.PraisePicItem;
import com.kkl.kklplus.entity.viomi.sd.VioMiOrderSnCode;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.md.entity.Engineer;
import com.wolfking.jeesite.modules.md.entity.ProductCompletePicItem;
import com.wolfking.jeesite.modules.md.service.ServicePointService;
import com.wolfking.jeesite.modules.sd.dao.OrderItemDao;
import com.wolfking.jeesite.modules.sd.entity.*;
import com.wolfking.jeesite.modules.sd.service.OrderItemCompleteService;
import com.wolfking.jeesite.modules.sd.utils.OrderItemUtils;
import com.wolfking.jeesite.modules.sd.utils.OrderPicUtils;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderProcessLogReqEntity;
import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderStatusUpdateReqEntity;
import com.wolfking.jeesite.ms.b2bcenter.sd.service.B2BOrderManualBaseService;
import com.wolfking.jeesite.ms.joyoung.sd.feign.JoyoungOrderFeign;
import com.wolfking.jeesite.ms.material.mq.entity.mapper.B2BMaterialMapper;
import com.wolfking.jeesite.ms.praise.service.OrderPraiseService;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class JoyoungOrderService extends B2BOrderManualBaseService {

    @Resource
    private OrderItemDao orderItemDao;

    @Autowired
    private JoyoungOrderFeign joyoungOrderFeign;

    private static B2BMaterialMapper mapper = Mappers.getMapper(B2BMaterialMapper.class);

    @Autowired
    OrderItemCompleteService orderItemCompleteService;

    @Autowired
    private ServicePointService servicePointService;
    @Autowired
    private OrderPraiseService orderPraiseService;

    /**
     * 取消工单转换
     */
    public MSResponse cancelOrderTransition(B2BOrderTransferResult b2BOrderTransferResult) {
        return joyoungOrderFeign.cancelOrderTransition(b2BOrderTransferResult);
    }

    //region 工单状态检查

    /**
     * 批量检查工单是否可转换
     */
    public MSResponse checkB2BOrderProcessFlag(List<B2BOrderTransferResult> b2bOrderNos) {
        return joyoungOrderFeign.checkWorkcardProcessFlag(b2bOrderNos);
    }

    //endregion 工单状态检查

    //region 更新转单进度

    /**
     * 调用天猫微服务的B2B工单转换进度更新接口
     */
    public MSResponse sendB2BOrderConversionProgressUpdateCommandToB2B(List<B2BOrderTransferResult> progressList) {
        return joyoungOrderFeign.updateTransferResult(Lists.newArrayList(progressList));
    }

    //endregion 更新转单进度

    //region 工单状态变更

    /**
     * 派单
     */
    private MSResponse joyoungOrderPlan(Long messageId, Long b2bOrderId, String b2bOrderNo, String engineerName, String engineerMobile,
                                        long createById, long createDt) {
        JoyoungOrderPlanned planned = new JoyoungOrderPlanned();
        planned.setUniqueId(messageId);
        planned.setB2bOrderId(b2bOrderId);
        planned.setOrderNo(b2bOrderNo);
        planned.setEngineerName(engineerName);
        planned.setEngineerMobile(engineerMobile);
        planned.setCreateDt(createDt);
        planned.setCreateById(createById);
        planned.setUpdateDt(createDt);
        planned.setUpdateById(createById);
        return joyoungOrderFeign.orderPlanned(planned);
    }

    /**
     * 预约
     */
    private MSResponse joyoungOrderAppoint(Long messageId, Long b2bOrderId, String b2bOrderNo, String appointedMan, Long appointmentDt, String remarks, Integer pendingType,
                                           long createById, long createDt) {
        JoyoungOrderAppointed appointed = new JoyoungOrderAppointed();
        appointed.setUniqueId(messageId);
        appointed.setB2bOrderId(b2bOrderId);
        appointed.setOrderNo(b2bOrderNo);
        appointed.setBookMan(appointedMan);
        appointed.setBookDate(appointmentDt);
        appointed.setBookRemark(remarks);
        appointed.setReasonType(pendingType);
        appointed.setCreateDt(createDt);
        appointed.setCreateById(createById);
        appointed.setUpdateDt(createDt);
        appointed.setUpdateById(createById);
        return joyoungOrderFeign.orderAppointed(appointed);
    }

    /**
     * 上门
     */
    private MSResponse joyoungOrderVisit(Long messageId, Long b2bOrderId, String b2bOrderNo, String visitedMan, Long visitedDt, String remarks,
                                         long createById, long createDt) {
        JoyoungOrderVisited visited = new JoyoungOrderVisited();
        visited.setUniqueId(messageId);
        visited.setB2bOrderId(b2bOrderId);
        visited.setOrderNo(b2bOrderNo);
        visited.setVisitMan(visitedMan);
        visited.setVisitDate(visitedDt);
        visited.setRemark(remarks);
        visited.setCreateDt(createDt);
        visited.setCreateById(createById);
        visited.setUpdateDt(createDt);
        visited.setUpdateById(createById);
        return joyoungOrderFeign.orderVisited(visited);
    }

    /**
     * 完成
     */
    private MSResponse joyoungOrderCompleted(Long messageId, Long b2bOrderId, String b2bOrderNo, List<JoyoungOrderCompleted.ProductDetail> completedItems, String remarks, long appCompleteDt,
                                             long createById, long createDt, MQB2BOrderStatusUpdateMessage.PraiseItem praiseItem) {
        JoyoungOrderCompleted completed = new JoyoungOrderCompleted();
        completed.setUniqueId(messageId);
        completed.setB2bOrderId(b2bOrderId);
        completed.setOrderNo(b2bOrderNo);
        completed.setFinishNote(remarks);
        completed.setItems(completedItems);
        completed.setAppCompleteDate(appCompleteDt);
        completed.setCreateDt(createDt);
        completed.setCreateById(createById);
        completed.setUpdateDt(createDt);
        completed.setUpdateById(createById);
        if (praiseItem != null && !praiseItem.getPicUrlList().isEmpty()) {
            completed.setIsPraise(1);
            completed.setPraisePhoto1(praiseItem.getPicUrl(0));
            if (praiseItem.getPicUrlCount() > 1) {
                completed.setPraisePhoto2(praiseItem.getPicUrl(1));
            }
        }
        return joyoungOrderFeign.orderCompleted(completed);
    }

    /**
     * 取消
     */
    private MSResponse joyoungOrderCancelled(Long messageId, Long b2bOrderId, String b2bOrderNo, String cancelledMan, Long cancelledDt, String remark,
                                             long createById, long createDt) {
        JoyoungOrderCancelled cancelled = new JoyoungOrderCancelled();
        cancelled.setUniqueId(messageId);
        cancelled.setB2bOrderId(b2bOrderId);
        cancelled.setOrderNo(b2bOrderNo);
        cancelled.setCancelMan(cancelledMan);
        cancelled.setCancelDate(cancelledDt);
        cancelled.setCancelRemark(remark);
        cancelled.setCreateDt(createDt);
        cancelled.setCreateById(createById);
        cancelled.setUpdateDt(createDt);
        cancelled.setUpdateById(createById);
        return joyoungOrderFeign.orderCancelled(cancelled);
    }

    /**
     * 往康佳微服务发送工单状态更新命令
     */
    public MSResponse sendOrderStatusUpdateCommandToB2B(MQB2BOrderStatusUpdateMessage.B2BOrderStatusUpdateMessage message) {
        MSResponse response;
        if (message.getStatus() == B2BOrderStatusEnum.PLANNED.value) {
            response = joyoungOrderPlan(message.getMessageId(), message.getB2BOrderId(), message.getB2BOrderNo(), message.getEngineerName(), message.getEngineerMobile(), message.getUpdaterId(), message.getUpdateDt());
        } else if (message.getStatus() == B2BOrderStatusEnum.APPOINTED.value) {
            response = joyoungOrderAppoint(message.getMessageId(), message.getB2BOrderId(), message.getB2BOrderNo(), message.getUpdaterName(), message.getEffectiveDt(), message.getRemarks(),
                    StringUtils.toInteger(message.getKklPendingType()), message.getUpdaterId(), message.getUpdateDt());
        } else if (message.getStatus() == B2BOrderStatusEnum.SERVICED.value) {
            response = joyoungOrderVisit(message.getMessageId(), message.getB2BOrderId(), message.getB2BOrderNo(), message.getEngineerName(), message.getEffectiveDt(), message.getRemarks(), message.getUpdaterId(), message.getUpdateDt());
        } else if (message.getStatus() == B2BOrderStatusEnum.COMPLETED.value) {

            List<MQB2BOrderStatusUpdateMessage.CompletedItem> itemList = message.getCompletedItemList();
            List<JoyoungOrderCompleted.ProductDetail> completedItemList = Lists.newArrayList();
            if (itemList != null && itemList.size() > 0) {
                JoyoungOrderCompleted.ProductDetail completedItem;
                for (MQB2BOrderStatusUpdateMessage.CompletedItem item : itemList) {
                    completedItem = new JoyoungOrderCompleted.ProductDetail();
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
            response = joyoungOrderCompleted(message.getMessageId(), message.getB2BOrderId(), message.getB2BOrderNo(), completedItemList, message.getRemarks(), message.getAppCompleteDt(), message.getUpdaterId(), message.getUpdateDt(), message.getPraiseItem());
        } else if (message.getStatus() == B2BOrderStatusEnum.CANCELED.value) {
            response = joyoungOrderCancelled(message.getMessageId(), message.getB2BOrderId(), message.getB2BOrderNo(), message.getUpdaterName(), message.getEffectiveDt(), message.getRemarks(), message.getUpdaterId(), message.getUpdateDt());
        } else {
            response = new MSResponse(MSErrorCode.SUCCESS);
            String msgJson = new JsonFormat().printToString(message);
            LogUtils.saveLog("B2B工单状态变更消息的状态错误", "JoyoungOrderService.sendOrderStatusUpdateCommandToB2B", msgJson, null, null);
        }
        return response;
    }


    //endregion 工单状态变更

    //region 其他

    private List<CanboOrderCompleted.CompletedItem> getJoyoungOrderCompletedItems(Long orderId, String quarter, List<OrderItem> orderItems) {
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

    private B2BOrderPraiseItem getOrderPraiseItem(Long orderId, String quarter, Long servicePointId) {
        B2BOrderPraiseItem praiseItem = null;
        Praise praise = orderPraiseService.getByOrderId(quarter, orderId, servicePointId);
        if (praise != null && praise.getPicItems() != null && !praise.getPicItems().isEmpty()) {
            praiseItem = new B2BOrderPraiseItem();
            for (PraisePicItem item : praise.getPicItems()) {
                if (StringUtils.isNotBlank(item.getUrl())) {
                    praiseItem.getPicUrls().add(OrderPicUtils.getPraisePicUrl(item.getUrl()));
                }
            }
        }
        return praiseItem;
    }

    //endregion 其他

    //-------------------------------------------------------------------------------------------------创建状态变更请求实体

    //region 创建工单状态变更情况实体

    /**
     * 创建九阳派单请求对象
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

    /**
     * 创建九阳预约请求对象
     */
    public TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> createAppointRequestEntity(Integer pendingType, Date appointmentDate, User updater, String remarks) {
        TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> result = new TwoTuple<>(false, null);
        if (updater != null && StringUtils.isNotBlank(updater.getName()) && appointmentDate != null) {
            B2BOrderStatusUpdateReqEntity.Builder builder = new B2BOrderStatusUpdateReqEntity.Builder();
            builder.setUpdaterName(updater.getName())
                    .setEffectiveDate(appointmentDate)
                    .setRemarks(StringUtils.toString(remarks))
                    .setPendingType(pendingType == null ? "" : pendingType.toString());
            result.setAElement(true);
            result.setBElement(builder);
        }
        return result;
    }

    /**
     * 创建九阳上门请求对象
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
     * 创建九阳完工请求对象
     */
    @Transactional()
    public TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> createCompleteRequestEntity(Long orderId, String quarter, List<OrderItem> orderItems, Long servicePointId, String remarks) {
        TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> result = new TwoTuple<>(false, null);
        if (orderId != null && orderId > 0 && StringUtils.isNotBlank(quarter)) {
            if (orderItems == null || orderItems.isEmpty()) {
                Order order = orderItemDao.getOrderItems(quarter, orderId);
                if (order != null) {
                    //orderItems = OrderItemUtils.fromOrderItemsJson(order.getOrderItemJson());
                    orderItems = OrderItemUtils.pbToItems(order.getItemsPb());//2020-12-17 sd_order -> sd_order_head
                }
            }
            List<CanboOrderCompleted.CompletedItem> completedItems = getJoyoungOrderCompletedItems(orderId, quarter, orderItems);
            B2BOrderPraiseItem praiseItem = getOrderPraiseItem(orderId, quarter, servicePointId);
            B2BOrderStatusUpdateReqEntity.Builder builder = new B2BOrderStatusUpdateReqEntity.Builder();
            builder.setCompletedItems(completedItems)
                    .setOrderPraiseItem(praiseItem)
                    .setRemarks(StringUtils.toString(remarks));
            result.setAElement(true);
            result.setBElement(builder);
        }
        return result;
    }

    /**
     * 创建九阳取消请求对象
     */
    public TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> createCancelRequestEntity(Integer kklCancelType, String remarks, Date cancelDate, User updater) {
        TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> result = new TwoTuple<>(false, null);
        StringBuilder cancelResponsible = new StringBuilder();
        if (kklCancelType != null) {
            cancelResponsible.append(MSDictUtils.getDictLabel(kklCancelType.toString(), Dict.DICT_TYPE_CANCEL_RESPONSIBLE, ""));
        }
        if (StringUtils.isNotBlank(remarks)) {
            if (StringUtils.isNotBlank(cancelResponsible.toString())) {
                cancelResponsible.append(":");
            }
            cancelResponsible.append(remarks);
        }
        if (StringUtils.isNotBlank(cancelResponsible.toString()) && cancelDate != null) {
            B2BOrderStatusUpdateReqEntity.Builder builder = new B2BOrderStatusUpdateReqEntity.Builder();
            builder.setEffectiveDate(cancelDate)
                    .setUpdaterName(updater != null && StringUtils.isNotBlank(updater.getName()) ? updater.getName() : "")
                    .setRemarks(cancelResponsible.toString());
            result.setAElement(true);
            result.setBElement(builder);
        }
        return result;
    }

    //endregion 创建工单状态变更情况实体

    //region 创建工单日志消息实体

    /**
     * 创建九阳工单日志消息实体
     */
    public TwoTuple<Boolean, B2BOrderProcessLogReqEntity.Builder> createOrderProcessLogReqEntity(OrderProcessLog log) {
        TwoTuple<Boolean, B2BOrderProcessLogReqEntity.Builder> result = new TwoTuple<>(false, null);
        if (log.getCreateBy() != null && StringUtils.isNotBlank(log.getCreateBy().getName())
                && log.getCreateDate() != null && StringUtils.isNotBlank(log.getActionComment())) {
            B2BOrderProcessLogReqEntity.Builder builder = new B2BOrderProcessLogReqEntity.Builder();
            builder.setOperatorName(log.getCreateBy().getName())
                    .setLogDt(log.getCreateDate().getTime())
                    .setLogContext(log.getActionComment())
                    .setDataSourceId(B2BDataSourceEnum.JOYOUNG.id);
            result.setAElement(true);
            result.setBElement(builder);
        }
        return result;
    }


    /**
     * 往九阳微服务推送工单日志
     */
    public MSResponse pushOrderProcessLogToMS(MQB2BOrderProcessLogMessage.B2BOrderProcessLogMessage message) {
        JoyoungOrderProcessLog orderProcessLog = new JoyoungOrderProcessLog();
        orderProcessLog.setOrderId(message.getOrderId());
        orderProcessLog.setOperatorName(message.getOperatorName());
        orderProcessLog.setLogDate(message.getLogDt());
        orderProcessLog.setLogType(message.getLogType());
        orderProcessLog.setLogContent(message.getLogContext());
        orderProcessLog.setCreateById(message.getCreateById());
        orderProcessLog.setCreateDt(message.getCreateDt());
        MSResponse response = joyoungOrderFeign.saveOrderProcesslog(orderProcessLog);
        return response;
    }

    //endregion 创建工单日志消息实体

    //region 配件

    /**
     * 申请配件单
     */
    public MSResponse newMaterialForm(MaterialMaster materialMaster) {
        try {
            B2BMaterial materialForm = mapper.toB2BMaterialForm(materialMaster);
            if (materialForm == null) {
                return new MSResponse<>(MSErrorCode.newInstance(MSErrorCode.FAILURE, "配件单转九阳配件单错误"));
            }
            return joyoungOrderFeign.newMaterialForm(materialForm);
        } catch (Exception e) {
            log.error("orderId:{} ", materialMaster.getOrderId(), e);
            return new MSResponse<>(MSErrorCode.newInstance(MSErrorCode.FAILURE, "微服务接口执行失败"));
        }
    }

    /**
     * 关闭配件单
     * 包含正常关闭，异常签收，取消(订单退单/取消)
     */
    public MSResponse materialClose(Long formId, String formNo, B2BMaterialClose.CloseType closeType, String remark, Long user) {
        if (formId == null || formId <= 0 || StringUtils.isBlank(formNo)) {
            return new MSResponse<>(MSErrorCode.newInstance(MSErrorCode.FAILURE, "参数不合法"));
        }
        B2BMaterialClose joyoung = B2BMaterialClose.builder()
                .kklMasterId(formId)
                .kklMasterNo(formNo)
                .closeType(closeType.getCode())
                .remark(remark)
                .build();
        joyoung.setCreateById(user);
        joyoung.setCreateDt(System.currentTimeMillis());
        joyoung.setUpdateById(user);
        joyoung.setUpdateDt(System.currentTimeMillis());
        return joyoungOrderFeign.materialClose(joyoung);
    }

    /**
     * by订单关闭
     * 包含正常关闭，异常签收，取消(订单退单/取消)
     */
    public MSResponse materialCloseByOrder(Long orderId, B2BMaterialClose.CloseType closeType, String remark, Long user) {
        if (orderId == null || orderId <= 0) {
            return new MSResponse<>(MSErrorCode.newInstance(MSErrorCode.FAILURE, "参数不合法"));
        }
        B2BMaterialClose joyoung = B2BMaterialClose.builder()
                .kklOrderId(orderId)
                .closeType(closeType.getCode())
                .remark(remark)
                .build();
        joyoung.setCreateById(user);
        joyoung.setCreateDt(System.currentTimeMillis());
        joyoung.setUpdateById(user);
        joyoung.setUpdateDt(System.currentTimeMillis());
        return joyoungOrderFeign.materialCloseByOrder(joyoung);
    }

    /**
     * 到货
     */
    public MSResponse materialArrival(Long formId, String formNo, Long arriveAt, String remark, Long user) {
        B2BMaterialArrival joyoung = B2BMaterialArrival.builder()
                .kklMasterId(formId)
                .kklMasterNo(formNo)
                .arrivalDate(arriveAt)
                .remark(remark)
                .build();
        return joyoungOrderFeign.materialArrival(joyoung);
    }

    /**
     * 处理完"审核"消息回调通知微服务
     */
    public MSResponse notifyApplyFlag(Long formId) {
        return joyoungOrderFeign.updateApplyFlag(formId);
    }

    /**
     * 处理完"已发货"消息回调通知微服务
     */
    public MSResponse notifyDeliverFlag(Long formId) {
        return joyoungOrderFeign.updateDeliverFlag(formId);
    }

    //endregion 配件

    //region 咨询单
    /**
     * 更新九阳投诉单的kkl投诉单Id
     */
    public MSResponse updateFlag(Long b2bConsultingId,Long kklConsultingId){
          MSResponse msResponse = joyoungOrderFeign.updateFlag(b2bConsultingId,kklConsultingId);
          return msResponse;
    }


    /**
     * 更新处理日志(投诉)
     */
     public MSResponse complainProcess(MQB2BOrderComplainProcessMessage.B2BOrderComplainProcessMessage message){
         JoyoungConsultingOrderProcess consultingOrderProcess = new JoyoungConsultingOrderProcess();
         consultingOrderProcess.setUniqueId(message.getKklComplainId());
         consultingOrderProcess.setConsultingNo(message.getB2BComplainNo());
         consultingOrderProcess.setType(message.getOperationType());
         consultingOrderProcess.setProcessContent(message.getContent());
         consultingOrderProcess.setCreateDt(message.getCreateAt());
         consultingOrderProcess.setCreateById(message.getOperatorId());
         return process(consultingOrderProcess);
     }


    /**
     * 九阳回复催单调用完成接口
     */
     public MSResponse reminderProcess(MQB2BOrderReminderProcessMessage.B2BOrderReminderProcessMessage message){
         JoyoungConsultingOrderProcess consultingOrderProcess = new JoyoungConsultingOrderProcess();
         consultingOrderProcess.setUniqueId(message.getKklReminderId());
         consultingOrderProcess.setType(message.getOperationType());
         consultingOrderProcess.setProcessContent(message.getContent());
         consultingOrderProcess.setCreateDt(message.getCreateDate());
         consultingOrderProcess.setConsultingNo(message.getB2BReminderNo());
         consultingOrderProcess.setCreateById(message.getOperatorId());
         return process(consultingOrderProcess);
     }

    /**
     * 更新处理日志(催单/投诉)
     */
    public MSResponse process(JoyoungConsultingOrderProcess consultingOrderProcess){
           return joyoungOrderFeign.process(consultingOrderProcess);
    }

    // endregion 咨询单

    //region 验证产品SN
    public MSResponse checkProductSN(String productSn) {
        return joyoungOrderFeign.getProductData(productSn);
    }
    //endregion 验证产品SN
}
