package com.wolfking.jeesite.ms.jd.sd.service;

import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.entity.b2bcenter.pb.MQB2BOrderStatusUpdateMessage;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderActionEnum;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderStatusEnum;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderTransferResult;
import com.kkl.kklplus.entity.canbo.sd.CanboOrderCompleted;
import com.kkl.kklplus.entity.jd.sd.JDConstrant;
import com.kkl.kklplus.entity.jd.sd.JdOrderAppointed;
import com.kkl.kklplus.entity.jd.sd.JdOrderCancelled;
import com.kkl.kklplus.entity.jd.sd.JdOrderCompleted;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.utils.GsonUtils;
import com.wolfking.jeesite.modules.md.entity.Engineer;
import com.wolfking.jeesite.modules.md.service.ServicePointService;
import com.wolfking.jeesite.modules.sd.dao.OrderItemDao;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.entity.OrderItem;
import com.wolfking.jeesite.modules.sd.entity.TwoTuple;
import com.wolfking.jeesite.modules.sd.service.OrderCacheReadService;
import com.wolfking.jeesite.modules.sd.utils.OrderItemUtils;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.ms.b2bcenter.md.utils.B2BMDUtils;
import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderProcessEntity;
import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderStatusUpdateFailureLog;
import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderStatusUpdateReqEntity;
import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderVModel;
import com.wolfking.jeesite.ms.b2bcenter.sd.service.B2BOrderManualBaseService;
import com.wolfking.jeesite.ms.jd.sd.feign.JdOrderFeign;
import com.wolfking.jeesite.ms.utils.MSUserUtils;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

import static com.wolfking.jeesite.ms.b2bcenter.sd.service.B2BCenterOrderService.KKL_SYSTEM_USER;
import static com.wolfking.jeesite.ms.b2bcenter.sd.service.B2BCenterOrderService.USER_ID_KKL_AUTO_GRADE;

@Slf4j
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class JdOrderService extends B2BOrderManualBaseService {

    private static final User KKL_JD_B2B_USER = new User(0L, "快可立全国联保", "075729235666");

    @Autowired
    private JdOrderFeign jdOrderFeign;

    @Autowired
    private ServicePointService servicePointService;

    @Resource
    private OrderItemDao orderItemDao;

    //region 工单状态检查

    /**
     * 批量检查工单是否可转换
     */
    public MSResponse checkB2BOrderProcessFlag(List<B2BOrderTransferResult> b2bOrderNos) {
        return jdOrderFeign.checkWorkcardProcessFlag(b2bOrderNos);
    }

    //endregion 工单状态检查

    /**
     * 取消工单转换
     *
     * @param b2BOrderTransferResult
     * @return
     */
    public MSResponse cancelOrderTransition(B2BOrderTransferResult b2BOrderTransferResult) {
        return jdOrderFeign.cancelOrderTransition(b2BOrderTransferResult);
    }
    //region 更新转单进度

    /**
     * 调用天猫微服务的B2B工单转换进度更新接口
     */
    public MSResponse sendB2BOrderConversionProgressUpdateCommandToB2B(List<B2BOrderTransferResult> progressList) {
        return jdOrderFeign.updateTransferResult(Lists.newArrayList(progressList));
    }

    //endregion 更新转单进度

    /**
     * 批量更新工单的路由标记
     */
    public MSResponse updateOrderRoutingFlagAll() {
        return jdOrderFeign.updateSystemIdAll();
    }

    /**
     * 忽略工单转换
     */
    public MSResponse ignoreOrderTransition(B2BOrderTransferResult b2BOrderTransferResult) {
        return jdOrderFeign.ignoreCancel(b2BOrderTransferResult);
    }

    //region 工单状态变更

    /**
     * 派单
     */
    private MSResponse jdOrderPlan(Long b2bOrderId, String b2bOrderNo, String engineerName, String engineerMobile, long servicePointId, long createById, long createDt) {
        JdOrderAppointed appointed = new JdOrderAppointed();
        appointed.setB2bOrderId(b2bOrderId);
        appointed.setServiceNo(b2bOrderNo);
        appointed.setEngineerName(engineerName);
        appointed.setEngineerTel(engineerMobile);
        appointed.setCreateDt(createDt);
        appointed.setCreateById(createById);
        appointed.setServicePointId(servicePointId);
        return jdOrderFeign.planned(appointed);
    }

    /**
     * 预约/改预约/派单/改派
     */
    private MSResponse jdOrderPlanOrAppoint(Long b2bOrderId, String b2bOrderNo, Integer appointmentStatus, Long appointmentDate, String kklPendingType,
                                            String engineerName, String engineerMobile, long servicePointId, long createById, long createDt) {
        JdOrderAppointed appointed = new JdOrderAppointed();
        appointed.setB2bOrderId(b2bOrderId);
        appointed.setServiceNo(b2bOrderNo);
        appointed.setAppointmentStatus(appointmentStatus);
        appointed.setAppointmentTimeBegin(appointmentDate);
        appointed.setOtherReservationCode(kklPendingType);
        appointed.setEngineerName(engineerName);
        appointed.setEngineerTel(engineerMobile);
        appointed.setCreateDt(createDt);
        appointed.setCreateById(createById);
        appointed.setServicePointId(servicePointId);
        return jdOrderFeign.appointmentPush(appointed);
    }

    /**
     * 京东退单申请需要首先调用预约
     */
    private MSResponse jdOrderOnlyAppoint(Long b2bOrderId, String b2bOrderNo, Long appointmentDate, long createById, long createDt) {
        JdOrderAppointed appointed = new JdOrderAppointed();
        appointed.setB2bOrderId(b2bOrderId);
        appointed.setServiceNo(b2bOrderNo);
        appointed.setAppointmentTimeBegin(appointmentDate);
        appointed.setCreateDt(createDt);
        appointed.setCreateById(createById);
        return jdOrderFeign.appointJDOrder(appointed);
    }


//    private MSResponse jdOrderCompleted(String b2bOrderNo, String operator, String operatorTel, long createById, long createDt) {
//        JdOrderCompleted completed = new JdOrderCompleted();
//        completed.setServiceNo(b2bOrderNo);
//        completed.setOperatorName(operator);
//        completed.setOperatorTel(operatorTel);
//        completed.setInstallStatus(JDConstrant.INSTALL_STATUS_ONSITE_SERVICE);
//        completed.setCreateDt(createDt);
//        completed.setCreateById(createById);
//        return jdOrderFeign.installPush(completed);
//    }

    /**
     * 完成
     */
    private MSResponse jdOrderCompletedNew(Long b2bOrderId, String b2bOrderNo, String operator, String operatorTel, String pic1Ulr, String pic2Url, String pic3Url, double actualTotalSurcharge, long createById, long createDt) {
        JdOrderCompleted completed = new JdOrderCompleted();
        completed.setB2bOrderId(b2bOrderId);
        completed.setServiceNo(b2bOrderNo);
        completed.setOperatorName(operator);
        completed.setOperatorTel(operatorTel);
        completed.setInstallStatus(JDConstrant.INSTALL_STATUS_ONSITE_SERVICE);
        completed.setUrl1(pic1Ulr);
        completed.setUrl2(pic2Url);
        completed.setUrl3(pic3Url);
        completed.setActualCost(actualTotalSurcharge);
        completed.setCreateDt(createDt);
        completed.setCreateById(createById);
        return jdOrderFeign.completeOrder(completed);
    }

    /**
     * 取消单
     */
    private MSResponse jdOrderCancelled(Long b2bOrderId, String b2bOrderNo, Integer installStatus, String operator, String operatorTel, long createById, long createDt) {
        JdOrderCancelled cancelled = new JdOrderCancelled();
        cancelled.setB2bOrderId(b2bOrderId);
        cancelled.setServiceNo(b2bOrderNo);
        cancelled.setInstallStatus(installStatus);
        cancelled.setOperatorName(operator);
        cancelled.setOperatorTel(operatorTel);
        cancelled.setCreateDt(createDt);
        cancelled.setCreateById(createById);
        return jdOrderFeign.uninstallPush(cancelled);
    }

    /**
     * 往京东微服务发送工单状态更新命令
     */
    public MSResponse sendOrderStatusUpdateCommandToB2B(MQB2BOrderStatusUpdateMessage.B2BOrderStatusUpdateMessage message) {
        MSResponse response = null;
        if (message.getStatus() == B2BOrderStatusEnum.PLANNED.value) {
            response = jdOrderPlan(message.getB2BOrderId(), message.getB2BOrderNo(), message.getEngineerName(), message.getEngineerMobile(), message.getServicePointId(), message.getUpdaterId(), message.getUpdateDt());
        } else if (message.getStatus() == B2BOrderStatusEnum.APPOINTED.value) {
            response = jdOrderPlanOrAppoint(message.getB2BOrderId(), message.getB2BOrderNo(), message.getAppointmentStatus(), message.getEffectiveDt(), message.getKklPendingType(),
                    message.getEngineerName(), message.getEngineerMobile(), message.getServicePointId(), message.getUpdaterId(), message.getUpdateDt());
        } else if (message.getStatus() == B2BOrderStatusEnum.APPLIED_FOR_CANCEL.value) {
            response = jdOrderOnlyAppoint(message.getB2BOrderId(), message.getB2BOrderNo(), message.getEffectiveDt(), message.getUpdaterId(), message.getUpdateDt());
        } else if (message.getStatus() == B2BOrderStatusEnum.COMPLETED.value) {
            List<MQB2BOrderStatusUpdateMessage.CompletedItem> completedItems = message.getCompletedItemList();
            String pic1Url = "";
            String pic2Url = "";
            String pic3Url = "";
            if (completedItems != null && completedItems.size() > 0) {
                pic1Url = completedItems.get(0).getPic1();
                pic2Url = completedItems.get(0).getPic2();
                pic3Url = completedItems.get(0).getPic3();
            }
//            response = jdOrderCompleted(message.getB2BOrderNo(), message.getUpdaterName(), message.getUpdaterMobile(), message.getUpdaterId(), message.getUpdateDt());
            response = jdOrderCompletedNew(message.getB2BOrderId(), message.getB2BOrderNo(), message.getUpdaterName(), message.getUpdaterMobile(), pic1Url, pic2Url, pic3Url, message.getActualTotalSurcharge(), message.getUpdaterId(), message.getUpdateDt());
        } else if (message.getStatus() == B2BOrderStatusEnum.CANCELED.value) {
            response = jdOrderCancelled(message.getB2BOrderId(), message.getB2BOrderNo(), message.getInstallStatus(), message.getUpdaterName(), message.getUpdaterMobile(), message.getUpdaterId(), message.getUpdateDt());
        } else {
            response = new MSResponse(MSErrorCode.SUCCESS);
            String msgJson = new JsonFormat().printToString(message);
            LogUtils.saveLog("B2B工单状态变更消息的状态错误", "JdOrderService.sendOrderStatusUpdateCommandToB2B", msgJson, null, null);
        }
        return response;
    }


    /*
    // mark on 2020-7-11 begin
    // sys_log2微服务化
    @Resource
    private Log2Dao log2Dao;

    public void manualCancel() {
        List<String> jsonList = log2Dao.getParamsForCancelB2BOrderFailure();
        for (String json : jsonList) {
            directCancel(json);
        }
    }
    // mark on 2020-7-11 end
    */

    private void directCancel(String cancelJson) {
        B2BOrderStatusUpdateFailureLog entity = GsonUtils.getInstance().fromJson(cancelJson, B2BOrderStatusUpdateFailureLog.class);
        if (entity != null && entity.getDataSourceId() != null && entity.getDataSourceId() == B2BDataSourceEnum.JD.id) {
            if (entity.getKklCancelType() == 0) {
                entity.setKklCancelType(57);
            }
            if (StringUtils.isBlank(entity.getUpdaterName())) {
                entity.setUpdaterName(KKL_JD_B2B_USER.getName());
            }
            if (StringUtils.isBlank(entity.getUpdaterMobile())) {
                entity.setUpdaterMobile(KKL_JD_B2B_USER.getMobile());
            }
            Integer jdCancelType = B2BMDUtils.getJdCancelType(entity.getKklCancelType());
            MSResponse response = jdOrderCancelled(0L, entity.getB2bOrderNo(), jdCancelType, entity.getUpdaterName(), entity.getUpdaterMobile(), entity.getUpdaterId(), entity.getUpdateDate().getTime());
            if (MSResponse.isSuccessCode(response)) {
                if (response.getThirdPartyErrorCode() != null) {
                    log.error("b2bOrderNo:{}, errorMsg: {}", entity.getB2bOrderNo(), StringUtils.toString(response.getThirdPartyErrorCode().msg));
                } else {
                    log.error("成功 - b2bOrderNo:{}", entity.getB2bOrderNo());
                }
            } else {
                if (response.getThirdPartyErrorCode() != null) {
                    log.error("b2bOrderNo:{}, errorMsg: {}", entity.getB2bOrderNo(), StringUtils.toString(response.getThirdPartyErrorCode().msg));
                } else {
                    log.error("失败 - b2bOrderNo:{}", entity.getB2bOrderNo());
                }
            }

        }
    }


    //endregion 工单状态变更


    //-------------------------------------------------------------------------------------------------创建状态变更请求实体

    /**
     * 创建京东派单请求对象
     */
    public TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> createJdPlanRequestEntity(Long servicePointId, String engineerName, String engineerMobile) {
        TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> result = new TwoTuple<>(false, null);
        if (servicePointId != null && servicePointId > 0 && StringUtils.isNotBlank(engineerName) && StringUtils.isNotBlank(engineerMobile)) {
            B2BOrderStatusUpdateReqEntity.Builder builder = new B2BOrderStatusUpdateReqEntity.Builder();
            builder.setEngineerName(engineerName)
                    .setEngineerMobile(engineerMobile)
                    .setServicePointId(servicePointId);
            result.setAElement(true);
            result.setBElement(builder);
        }
        return result;
    }

    /**
     * 创建京东预约派单请求对象
     */
    public TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> createJdPlanAndAppointRequestEntity(Integer pendingType, Date appointmentDate, Long servicePointId, Long engineerId) {
        TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> result = new TwoTuple<>(false, null);
        if (appointmentDate != null && servicePointId != null && servicePointId > 0 && engineerId != null && engineerId > 0) {
            Engineer engineer = servicePointService.getEngineerFromCache(servicePointId, engineerId);
            if (engineer != null && StringUtils.isNotBlank(engineer.getName()) && StringUtils.isNotBlank(engineer.getContactInfo())) {
                B2BOrderStatusUpdateReqEntity.Builder builder = new B2BOrderStatusUpdateReqEntity.Builder();
                builder.setPendingType(pendingType == null ? "" : pendingType.toString())
                        .setEffectiveDate(appointmentDate)
                        .setEngineerName(engineer.getName())
                        .setEngineerMobile(engineer.getContactInfo())
                        .setServicePointId(servicePointId);
                result.setAElement(true);
                result.setBElement(builder);
            }
        }
        return result;
    }

    /**
     * 创建京东预约请求对象
     */
    public TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> createJdOnlyAppointRequestEntity(Date appointmentDate) {
        TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> result = new TwoTuple<>(false, null);
        if (appointmentDate != null) {
            B2BOrderStatusUpdateReqEntity.Builder builder = new B2BOrderStatusUpdateReqEntity.Builder();
            builder.setEffectiveDate(appointmentDate);
            result.setAElement(true);
            result.setBElement(builder);
        }
        return result;
    }

//    /**
//     * 创建京东完工请求对象
//     */
//    public TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> createJdCompleteRequestEntity(User updater) {
//        TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> result = new TwoTuple<>(false, null);
//        if (updater != null && updater.getId() != null && updater.getId() > 0) {
//            if (updater.getId() == USER_ID_KKL_AUTO_GRADE) {
//                updater = new User(KKL_SYSTEM_USER.getId(), KKL_SYSTEM_USER.getName(), KKL_SYSTEM_USER.getMobile());
//            } else {
//                updater = MSUserUtils.get(updater.getId());
//            }
//            if (updater == null) {
//                updater = new User(KKL_SYSTEM_USER.getId(), KKL_SYSTEM_USER.getName(), KKL_SYSTEM_USER.getMobile());
//            }
//            if (StringUtils.isNotBlank(updater.getName()) && (StringUtils.isNotBlank(updater.getMobile()) || StringUtils.isNotBlank(updater.getPhone()))) {
//                B2BOrderStatusUpdateReqEntity.Builder builder = new B2BOrderStatusUpdateReqEntity.Builder();
//                builder.setUpdaterName(updater.getName())
//                        .setUpdaterMobile(StringUtils.isNotBlank(updater.getMobile()) ? updater.getMobile() : updater.getPhone());
//                result.setAElement(true);
//                result.setBElement(builder);
//            }
//        }
//        return result;
//    }

    @Transactional()
    public TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> createJdCompleteRequestEntityNew(User updater, Long orderId, String quarter, List<OrderItem> orderItems) {
        TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> result = new TwoTuple<>(false, null);
        if (orderId != null && orderId > 0 && StringUtils.isNotBlank(quarter) && updater != null && updater.getId() != null && updater.getId() > 0) {
            if (updater.getId() == USER_ID_KKL_AUTO_GRADE) {
                updater = new User(KKL_SYSTEM_USER.getId(), KKL_SYSTEM_USER.getName(), KKL_SYSTEM_USER.getMobile());
            } else {
                updater = MSUserUtils.get(updater.getId());
            }
            if (updater == null) {
                updater = new User(KKL_SYSTEM_USER.getId(), KKL_SYSTEM_USER.getName(), KKL_SYSTEM_USER.getMobile());
            }
            if (StringUtils.isBlank(updater.getName())) {
                updater.setName(KKL_SYSTEM_USER.getName());
            }
            if (StringUtils.isBlank(updater.getMobile())) {
                updater.setMobile(KKL_SYSTEM_USER.getMobile());
            }
            if (orderItems == null || orderItems.isEmpty()) {
                Order order = orderItemDao.getOrderItems(quarter, orderId);
                if (order != null) {
                    //orderItems = OrderItemUtils.fromOrderItemsJson(order.getOrderItemJson());
                    orderItems = OrderItemUtils.pbToItems(order.getItemsPb());//2020-12-17 sd_order -> sd_order_head
                }
            }
            List<CanboOrderCompleted.CompletedItem> completedItems = getOrderCompletedItems(orderId, quarter, orderItems);
            double actualTotalSurcharge = getActualTotalSurcharge(orderId, quarter);
            if (StringUtils.isNotBlank(updater.getName()) && (StringUtils.isNotBlank(updater.getMobile()) || StringUtils.isNotBlank(updater.getPhone()))) {
                B2BOrderStatusUpdateReqEntity.Builder builder = new B2BOrderStatusUpdateReqEntity.Builder();
                builder.setUpdaterName(updater.getName())
                        .setCompletedItems(completedItems)
                        .setActualTotalSurcharge(actualTotalSurcharge)
                        .setUpdaterMobile(StringUtils.isNotBlank(updater.getMobile()) ? updater.getMobile() : updater.getPhone());
                result.setAElement(true);
                result.setBElement(builder);
            }
        }
        return result;
    }

    /**
     * 创建京东取消请求对象
     */
    public TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> createJdCancelRequestEntity(Integer kklCancelType, User updater) {
        TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> result = new TwoTuple<>(false, null);
        if (kklCancelType != null && updater != null && updater.getId() != null && updater.getId() > 0) {
            updater = MSUserUtils.get(updater.getId());
            if (updater == null) {
                updater = KKL_JD_B2B_USER;
            }
            Integer jdCancelType = B2BMDUtils.getJdCancelType(kklCancelType);
            if (jdCancelType != null) {
                String updaterName = StringUtils.isNotBlank(updater.getName()) ? updater.getName() : KKL_JD_B2B_USER.getName();
                String updaterMobile = StringUtils.isNotBlank(updater.getMobile()) ? updater.getMobile() : updater.getPhone();
                updaterMobile = StringUtils.isNotBlank(updaterMobile) ? updaterMobile : KKL_JD_B2B_USER.getMobile();

                B2BOrderStatusUpdateReqEntity.Builder builder = new B2BOrderStatusUpdateReqEntity.Builder();
                builder.setUpdaterName(updaterName)
                        .setUpdaterMobile(updaterMobile)
                        .setInstallStaus(jdCancelType);
                result.setAElement(true);
                result.setBElement(builder);
            }
        }
        return result;
    }


    @Autowired
    private OrderCacheReadService orderCacheReadService;

    /**
     * 取消工单
     */
    private boolean cancelKKLOrder(B2BOrderProcessEntity entity) {
        boolean result = false;
        Order order = orderCacheReadService.getOrderById(entity.getKklOrderId(), null, OrderUtils.OrderDataLevel.CONDITION, true, true);
        if (order != null && order.getOrderCondition() != null) {
            String remarks = StringUtils.toString(entity.getRemarks()) + "（" + B2BDataSourceEnum.JD.name + "平台通知取消B2B工单）";
            if (order.getOrderCondition().getStatusValue() <= Order.ORDER_STATUS_ACCEPTED) {
                orderService.cancelOrderNew(order.getId(), B2BOrderVModel.b2bUser, remarks, false);
                B2BOrderProcessEntity.saveFailureLog(entity, "京东主动取消工单[取消工单]", "JdOrderService.cancelKKLOrder", null);
            } else if (order.getOrderCondition().getStatusValue() == Order.ORDER_STATUS_RETURNING) {
                orderService.approveReturnOrderNew(order.getId(), order.getQuarter(), remarks, B2BOrderVModel.b2bUser);
                B2BOrderProcessEntity.saveFailureLog(entity, "京东主动取消工单[审核退单]", "JdOrderService.cancelKKLOrder", null);
            } else {
                orderService.b2bCancelOrder(order.getId(), order.getQuarter(), new Dict("51", "厂家(电商)通知取消"), remarks, B2BOrderVModel.b2bUser);
                B2BOrderProcessEntity.saveFailureLog(entity, "京东主动取消工单[退单申请与审核退单]", "JdOrderService.cancelKKLOrder", null);
            }
            result = true;
        } else {
            B2BOrderProcessEntity.saveFailureLog(entity, "读取工单失败", "JdOrderService.cancelKKLOrder", null);
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
            B2BOrderProcessEntity.saveFailureLog(processEntity, "修改快可立工单", "JdOrderService.processKKLOrder", e);
        }
        return new MSResponse(flag ? MSErrorCode.SUCCESS : MSErrorCode.FAILURE);
    }
}
