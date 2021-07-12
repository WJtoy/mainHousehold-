package com.wolfking.jeesite.ms.b2bcenter.sd.service;

import cn.hutool.core.util.ObjectUtil;
import com.google.common.collect.Lists;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2b.common.B2BProcessFlag;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.entity.b2bcenter.pb.MQB2BOrderComplainProcessMessage;
import com.kkl.kklplus.entity.b2bcenter.pb.MQB2BOrderDismountReturnMessage;
import com.kkl.kklplus.entity.b2bcenter.pb.MQB2BOrderStatusUpdateMessage;
import com.kkl.kklplus.entity.b2bcenter.sd.*;
import com.kkl.kklplus.entity.canbo.sd.CanboOrderCompleted;
import com.kkl.kklplus.entity.validate.OrderValidate;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.service.SequenceIdService;
import com.wolfking.jeesite.common.utils.GsonUtils;
import com.wolfking.jeesite.modules.md.entity.Engineer;
import com.wolfking.jeesite.modules.sd.entity.*;
import com.wolfking.jeesite.modules.sd.service.OrderCacheReadService;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.ms.b2bcenter.md.utils.B2BMDUtils;
import com.wolfking.jeesite.ms.b2bcenter.mq.sender.B2BCenterOrderComplainProcessMQSender;
import com.wolfking.jeesite.ms.b2bcenter.mq.sender.B2BOrderStatusUpdateMQSender;
import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderStatusUpdateFailureLog;
import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderStatusUpdateReqEntity;
import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderVModel;
import com.wolfking.jeesite.ms.b2bcenter.sd.utils.B2BOrderUtils;
import com.wolfking.jeesite.ms.canbo.sd.service.CanboOrderService;
import com.wolfking.jeesite.ms.common.config.MicroServicesProperties;
import com.wolfking.jeesite.ms.inse.sd.service.InseOrderService;
import com.wolfking.jeesite.ms.jd.sd.service.JdOrderService;
import com.wolfking.jeesite.ms.jdue.sd.service.JDUEOrderService;
import com.wolfking.jeesite.ms.jdueplus.sd.service.JDUEPlusOrderService;
import com.wolfking.jeesite.ms.jinjing.service.JinJingOrderService;
import com.wolfking.jeesite.ms.jinran.sd.service.JinRanOrderService;
import com.wolfking.jeesite.ms.joyoung.sd.service.JoyoungOrderService;
import com.wolfking.jeesite.ms.konka.sd.service.KonkaOrderService;
import com.wolfking.jeesite.ms.lb.sb.service.LbOrderService;
import com.wolfking.jeesite.ms.mbo.service.MBOOrderService;
import com.wolfking.jeesite.ms.mqi.sd.service.MqiOrderService;
import com.wolfking.jeesite.ms.pdd.sd.service.PddOrderService;
import com.wolfking.jeesite.ms.philips.sd.service.PhilipsOrderService;
import com.wolfking.jeesite.ms.sf.sd.service.SFOrderService;
import com.wolfking.jeesite.ms.suning.sd.service.SuningOrderService;
import com.wolfking.jeesite.ms.supor.sd.service.SuporOrderService;
import com.wolfking.jeesite.ms.tmall.sd.service.TmallOrderService;
import com.wolfking.jeesite.ms.um.sd.service.UMOrderService;
import com.wolfking.jeesite.ms.usatonga.service.UsatonGaOrderService;
import com.wolfking.jeesite.ms.vatti.sd.service.VattiOrderService;
import com.wolfking.jeesite.ms.viomi.sd.service.VioMiOrderService;
import com.wolfking.jeesite.ms.weber.service.WeberOrderService;
import com.wolfking.jeesite.ms.xyyplus.sd.service.XYYPlusOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Slf4j
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Service
public class B2BCenterOrderService {

    @Autowired
    private B2BOrderStatusUpdateMQSender b2BOrderStatusUpdateMQSender;
    @Autowired
    private OrderCacheReadService orderCacheReadService;
    @Autowired
    private CanboOrderService canboOrderService;
    @Autowired
    private WeberOrderService weberOrderService;
    @Autowired
    private MBOOrderService mboOrderService;
    @Autowired
    private SuporOrderService suporOrderService;
    @Autowired
    private JinJingOrderService jinJingOrderService;
    @Autowired
    private UsatonGaOrderService usatonGaOrderService;
    @Autowired
    private MqiOrderService mqiOrderService;
    @Autowired
    private JinRanOrderService jinRanOrderService;
    @Autowired
    private TmallOrderService tmallOrderService;
    @Autowired
    private JdOrderService jdOrderService;
    @Autowired
    private InseOrderService inseOrderService;
    @Autowired
    private KonkaOrderService konkaOrderService;
    @Autowired
    private JoyoungOrderService joyoungOrderService;
    @Autowired
    private SuningOrderService suningOrderService;
    @Autowired
    private JDUEOrderService jdueOrderService;
    @Autowired
    private JDUEPlusOrderService jduePlusOrderService;
    @Autowired
    private XYYPlusOrderService xyyPlusOrderService;
    @Autowired
    private LbOrderService lbOrderService;
    @Autowired
    private UMOrderService umOrderService;
    @Autowired
    private PddOrderService pddOrderService;
    @Autowired
    private VioMiOrderService vioMiOrderService;
    @Autowired
    private SFOrderService sfOrderService;
    @Autowired
    private VattiOrderService vattiOrderService;
    @Autowired
    private PhilipsOrderService philipsOrderService;
//    @Autowired
//    private PhilipsNewOrderService philipsNewOrderService;
    @Autowired
    private MicroServicesProperties microServicesProperties;

    public static final User KKL_SYSTEM_USER = new User(0L, "快可立全国联保", "4006663653");
    public static final long USER_ID_KKL_AUTO_GRADE = 2L; //用户回复短信

    @Autowired
    private SequenceIdService sequenceIdService;

    @Autowired
    private B2BCenterOrderComplainProcessMQSender complainProcessMQSender;

    //-----------------------------------------------------------------------------------------------------------公用方法

    //region 公用方法

    /**
     * 是否需要发送工单状态消息给B2B微服务
     */
    private boolean isNeedSendOrderStatusMsgToB2B(Integer dataSourceId) {
        return B2BOrderUtils.canInvokeB2BMicroService(dataSourceId);
    }

    /**
     * 记录发送B2B工单状态变更消息过程中失败的情况
     */
    private void saveFailureLog(B2BOrderStatusUpdateFailureLog failureLog, String methodName) {
        try {
            String logJson = GsonUtils.toGsonString(failureLog);
            LogUtils.saveLog("B2BCenterOrderService.saveFailureLog", methodName, logJson, null, null);
        } catch (Exception e) {
            log.error("B2BCenterOrderService.saveFailureLog", e);
        }
    }

    //endregion 公用方法

    //----------------------------------------------------------------------------------------------创建工单状态变更消息实体

    //region 创建工单状态变更消息实体

    private void setB2BOrderStatusUpdateReqEntityProperties(B2BOrderStatusUpdateReqEntity.Builder entityBuilder, B2BOrderStatusEnum status, B2BOrderActionEnum action,
                                                            Integer dataSourceId, Long b2bOrderId, String b2bOrderNo, Long kklOrderId, String kklOrderNo,
                                                            User updater, Date updateDate) {
        if (entityBuilder != null) {
            entityBuilder
                    .setStatus(status)
                    .setActionType(action == null ? B2BOrderActionEnum.NONE : action)
                    .setDataSourceId(dataSourceId)
                    .setB2bOrderId(b2bOrderId == null ? 0L : b2bOrderId)
                    .setB2bOrderNo(b2bOrderNo)
                    .setOrderId(kklOrderId == null ? 0L : kklOrderId)
                    .setKklOrderNo(StringUtils.toString(kklOrderNo))
                    .setUpdaterId(updater == null || updater.getId() == null ? 0L : updater.getId())
                    .setUpdateDate(updateDate == null ? new Date() : updateDate);
        }

    }

    /**
     * B2B派单（APP接单、客户派单、网点派单）
     */
    public void planOrder(Order order, Engineer engineer, User updater, Date updateDate) {
        if (order != null && order.getId() != null) {
            if (order.getOrderCondition() != null && order.getOrderCondition().getCustomerId() > 0) {
                updateOrderStatus(B2BDataSourceEnum.UM, order.getOrderCondition().getCustomerId(), order.getId(), B2BOrderStatusEnum.PLANNED);
            } else {
                Order cachedOrder = orderCacheReadService.getOrderById(order.getId(), order.getQuarter(), OrderUtils.OrderDataLevel.CONDITION, true, false);
                if (cachedOrder != null) {
                    updateOrderStatus(B2BDataSourceEnum.UM, cachedOrder.getOrderCondition().getCustomerId(), cachedOrder.getId(), B2BOrderStatusEnum.PLANNED);
                }
            }
        }
        if (order != null && order.getDataSource() != null && engineer != null) {
            Long servicePointId = engineer.getServicePoint() != null && engineer.getServicePoint().getId() != null ? engineer.getServicePoint().getId() : 0;
            planB2BOrder(order.getDataSourceId(), order.getB2bOrderId(), order.getWorkCardId(), order.getId(), order.getOrderNo(),
                    servicePointId, engineer.getId(), engineer.getName(), engineer.getContactInfo(), updater, updateDate);
        }
    }

    private void planB2BOrder(Integer dataSourceId, Long b2bOrderId, String b2bOrderNo, Long orderId, String orderNo,
                              Long servicePointId, Long engineerId, String engineerName, String engineerMobile, User updater, Date updateDate) {
        if (isNeedSendOrderStatusMsgToB2B(dataSourceId) && StringUtils.isNotBlank(b2bOrderNo)) {
            String defaultEngineerName = microServicesProperties.getB2bcenter().getDefaultEngineerName();
            String defaultEngineerMobile = microServicesProperties.getB2bcenter().getDefaultEngineerPhone();
            TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> result;
            if (B2BDataSourceEnum.isTooneDataSourceId(dataSourceId)) {
                result = canboOrderService.createPlanRequestEntity(dataSourceId, servicePointId, engineerName, engineerMobile);
//                if (dataSourceId != B2BDataSourceEnum.USATON.id && dataSourceId != B2BDataSourceEnum.CANBO.id) {//阿斯丹顿和康宝传真实的电话
//                    if (StringUtils.isNotBlank(microServicesProperties.getCanbo().getDefaultPhoneNumber())) {
//                        result.getBElement().setEngineerMobile(microServicesProperties.getCanbo().getDefaultPhoneNumber());
//                    }
//                }
            } else if (dataSourceId == B2BDataSourceEnum.WEBER.id) {
                result = weberOrderService.createPlanRequestEntity(dataSourceId, servicePointId, engineerName, engineerMobile);
                if (StringUtils.isNotBlank(microServicesProperties.getWeber().getDefaultPhoneNumber())) {
                    result.getBElement().setEngineerMobile(microServicesProperties.getWeber().getDefaultPhoneNumber());
                }
            } else if (dataSourceId == B2BDataSourceEnum.MBO.id) {
                result = mboOrderService.createPlanRequestEntity(dataSourceId, servicePointId,  engineerName, engineerMobile);
                if (StringUtils.isNotBlank(microServicesProperties.getMbo().getDefaultPhoneNumber())) {
                    result.getBElement().setEngineerMobile(microServicesProperties.getMbo().getDefaultPhoneNumber());
                }
            } else if (dataSourceId == B2BDataSourceEnum.SUPOR.id) {
                result = suporOrderService.createPlanRequestEntity(defaultEngineerName, defaultEngineerMobile);
                if (StringUtils.isNotBlank(microServicesProperties.getSupor().getDefaultPhoneNumber())) {
                    result.getBElement().setEngineerMobile(microServicesProperties.getSupor().getDefaultPhoneNumber());
                }
            } else if (dataSourceId == B2BDataSourceEnum.JINJING.id) {
                result = jinJingOrderService.createPlanRequestEntity(dataSourceId, servicePointId, engineerName, engineerMobile);
                if (StringUtils.isNotBlank(microServicesProperties.getJinjing().getDefaultPhoneNumber())) {
                    result.getBElement().setEngineerMobile(microServicesProperties.getJinjing().getDefaultPhoneNumber());
                }
            } else if (dataSourceId == B2BDataSourceEnum.USATON_GA.id) {
                result = usatonGaOrderService.createPlanRequestEntity(dataSourceId, servicePointId,  engineerName, engineerMobile);
                if (StringUtils.isNotBlank(microServicesProperties.getUsatonGa().getDefaultPhoneNumber())) {
                    result.getBElement().setEngineerMobile(microServicesProperties.getUsatonGa().getDefaultPhoneNumber());
                }
            } else if (dataSourceId == B2BDataSourceEnum.MQI.id) {
                result = mqiOrderService.createPlanRequestEntity( engineerName, engineerMobile);
            } else if (dataSourceId == B2BDataSourceEnum.JINRAN.id) {
                result = jinRanOrderService.createPlanRequestEntity(defaultEngineerName, defaultEngineerMobile);
            } else if (dataSourceId == B2BDataSourceEnum.JD.id) {
                result = jdOrderService.createJdPlanRequestEntity(servicePointId, engineerName, engineerMobile);
            } else if (dataSourceId == B2BDataSourceEnum.INSE.id) {
                result = inseOrderService.createPlanRequestEntity(engineerId,  engineerName, engineerMobile);
                if (StringUtils.isNotBlank(microServicesProperties.getInse().getDefaultPhoneNumber())) {
                    result.getBElement().setEngineerMobile(microServicesProperties.getInse().getDefaultPhoneNumber());
                }
            } else if (dataSourceId == B2BDataSourceEnum.KONKA.id) {
                result = konkaOrderService.createPlanRequestEntity( engineerName, engineerMobile, "");
                if (StringUtils.isNotBlank(microServicesProperties.getKonka().getDefaultPhoneNumber())) {
                    result.getBElement().setEngineerMobile(microServicesProperties.getKonka().getDefaultPhoneNumber());
                }
            } else if (dataSourceId == B2BDataSourceEnum.JOYOUNG.id) {
                result = joyoungOrderService.createPlanRequestEntity(engineerName, engineerMobile);
                if (StringUtils.isNotBlank(microServicesProperties.getJoyoung().getDefaultPhoneNumber())) {
                    result.getBElement().setEngineerMobile(microServicesProperties.getJoyoung().getDefaultPhoneNumber());
                }
            } else if (dataSourceId == B2BDataSourceEnum.SUNING.id) {
                result = suningOrderService.createSuningPlanRequestEntity(engineerId,  engineerName, engineerMobile);
            } else if (dataSourceId == B2BDataSourceEnum.JDUE.id) {
                result = jdueOrderService.createPlanRequestEntity(engineerName, engineerMobile);
                if (StringUtils.isNotBlank(microServicesProperties.getJdue().getDefaultPhoneNumber())) {
                    result.getBElement().setEngineerMobile(microServicesProperties.getJdue().getDefaultPhoneNumber());
                }
            }
//            else if (dataSourceId == B2BDataSourceEnum.JDUEPLUS.id) {
//                result = jduePlusOrderService.createPlanRequestEntity(engineerName, engineerMobile);
//                if (StringUtils.isNotBlank(microServicesProperties.getJduePlus().getDefaultPhoneNumber())) {
//                    result.getBElement().setEngineerMobile(microServicesProperties.getJduePlus().getDefaultPhoneNumber());
//                }
//            }
            else if (dataSourceId == B2BDataSourceEnum.XYINGYAN.id) {
                result = xyyPlusOrderService.createPlanRequestEntity(engineerName, engineerMobile);
                if (StringUtils.isNotBlank(microServicesProperties.getXyyPlus().getDefaultPhoneNumber())) {
                    result.getBElement().setEngineerMobile(microServicesProperties.getXyyPlus().getDefaultPhoneNumber());
                }
            } else if (dataSourceId == B2BDataSourceEnum.LB.id) {
                result = lbOrderService.createPlanRequestEntity( engineerName, engineerMobile);
                if (StringUtils.isNotBlank(microServicesProperties.getLb().getDefaultPhoneNumber())) {
                    result.getBElement().setEngineerMobile(microServicesProperties.getLb().getDefaultPhoneNumber());
                }
            } else if (dataSourceId == B2BDataSourceEnum.TMALL.id) {
                result = tmallOrderService.createPlanRequestEntity(engineerName, engineerMobile);
            } else if (dataSourceId == B2BDataSourceEnum.PDD.id) {
                result = pddOrderService.createPlanRequestEntity(engineerId, engineerName, engineerMobile);
            } else if (dataSourceId == B2BDataSourceEnum.VIOMI.id) {
                result = vioMiOrderService.createPlanRequestEntity(engineerName, engineerMobile, updater);
            } else if (dataSourceId == B2BDataSourceEnum.SF.id) {
                result = sfOrderService.createPlanRequestEntity(engineerName, engineerMobile);
            } else if (dataSourceId == B2BDataSourceEnum.PHILIPS.id) {
                result = philipsOrderService.createPlanRequestEntity();
//                result = philipsNewOrderService.createPlanRequestEntity(engineerMobile);
            } else {
                result = new TwoTuple<>(true, null);
            }
            if (result.getAElement() && result.getBElement() != null) {
                setB2BOrderStatusUpdateReqEntityProperties(result.getBElement(), B2BOrderStatusEnum.PLANNED, B2BOrderActionEnum.PLAN,
                        dataSourceId, b2bOrderId, b2bOrderNo, orderId, orderNo, updater, updateDate);
                sendB2BOrderStatusUpdateMessage(result.getBElement().build());
            } else {
                if (!result.getAElement()) {
                    B2BOrderStatusUpdateFailureLog log = new B2BOrderStatusUpdateFailureLog(dataSourceId, b2bOrderNo, engineerId, engineerName, engineerMobile, updater, updateDate, B2BOrderStatusEnum.PLANNED);
                    saveFailureLog(log, "planB2BOrder");
                }
            }
        }
    }

    /**
     * 网点派单（APP派单、网点Web派单）
     */
    public void servicePointPlanOrder(Order order, Engineer engineer, User updater, Date updateDate) {
        if (order != null && order.getDataSource() != null && engineer != null) {
            Long servicePointId = engineer.getServicePoint() != null && engineer.getServicePoint().getId() != null ? engineer.getServicePoint().getId() : 0;
            servicePointPlanB2BOrder(order.getDataSourceId(), order.getB2bOrderId(), order.getWorkCardId(), order.getId(), order.getOrderNo(),
                    servicePointId, engineer.getId(), engineer.getName(), engineer.getContactInfo(), updater, updateDate);
        }
    }

    private void servicePointPlanB2BOrder(Integer dataSourceId, Long b2bOrderId, String b2bOrderNo, Long orderId, String orderNo,
                                          Long servicePointId, Long engineerId, String engineerName, String engineerMobile, User updater, Date updateDate) {
        if (isNeedSendOrderStatusMsgToB2B(dataSourceId) && StringUtils.isNotBlank(b2bOrderNo)) {
            TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> result;
            if (dataSourceId == B2BDataSourceEnum.JDUEPLUS.id) {
                result = jduePlusOrderService.createServicePointPlanRequestEntity(engineerName, engineerMobile);
                if (StringUtils.isNotBlank(microServicesProperties.getJduePlus().getDefaultPhoneNumber())) {
                    result.getBElement().setEngineerMobile(microServicesProperties.getJduePlus().getDefaultPhoneNumber());
                }
            } else {
                result = new TwoTuple<>(true, null);
            }
            if (result.getAElement() && result.getBElement() != null) {
                setB2BOrderStatusUpdateReqEntityProperties(result.getBElement(), B2BOrderStatusEnum.SERVICE_POINT_PLANNED, B2BOrderActionEnum.SERVICE_POINT_PLAN,
                        dataSourceId, b2bOrderId, b2bOrderNo, orderId, orderNo, updater, updateDate);
                sendB2BOrderStatusUpdateMessage(result.getBElement().build());
            } else {
                if (!result.getAElement()) {
                    B2BOrderStatusUpdateFailureLog log = new B2BOrderStatusUpdateFailureLog(dataSourceId, b2bOrderNo, engineerId, engineerName, engineerMobile, updater, updateDate, B2BOrderStatusEnum.SERVICE_POINT_PLANNED);
                    saveFailureLog(log, "servicePointPlanB2BOrder");
                }
            }
        }
    }

    /**
     * B2B预约
     */
    public void pendingOrder(Order order, Long servicePointId, Long engineerId, Integer pendingType, Date appointmentDate, User updater, Date updateDate, String remarks) {
        if (order != null && order.getId() != null && order.getOrderCondition() != null && order.getOrderCondition().getCustomerId() > 0) {
            updateOrderStatus(B2BDataSourceEnum.UM, order.getOrderCondition().getCustomerId(), order.getId(), B2BOrderStatusEnum.APPOINTED);
        }
        if (order != null && order.getDataSource() != null) {
            appointB2BOrder(order.getDataSourceId(), order.getB2bOrderId(), order.getWorkCardId(), order.getId(), order.getOrderNo(),
                    servicePointId, engineerId, pendingType, appointmentDate, StringUtils.toString(remarks), updater, updateDate);
        }
    }

    private void appointB2BOrder(Integer dataSourceId, Long b2bOrderId, String b2bOrderNo, Long orderId, String orderNo,
                                 Long servicePointId, Long engineerId, Integer pendingType, Date effectiveDate, String remarks, User updater, Date updateDate) {
        if (isNeedSendOrderStatusMsgToB2B(dataSourceId) && StringUtils.isNotBlank(b2bOrderNo)) {
            TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> result;
            if (B2BDataSourceEnum.isTooneDataSourceId(dataSourceId)) {
                result = canboOrderService.createAppointRequestEntity(effectiveDate, remarks, updater);
            } else if (dataSourceId == B2BDataSourceEnum.WEBER.id) {
                result = weberOrderService.createAppointRequestEntity(effectiveDate, remarks, updater);
            } else if (dataSourceId == B2BDataSourceEnum.MBO.id) {
                result = mboOrderService.createAppointRequestEntity(effectiveDate, remarks, updater);
            } else if (dataSourceId == B2BDataSourceEnum.SUPOR.id) {
                result = suporOrderService.createAppointRequestEntity(effectiveDate, pendingType);
            } else if (dataSourceId == B2BDataSourceEnum.JINJING.id) {
                result = jinJingOrderService.createAppointRequestEntity(effectiveDate, remarks, updater);
            } else if (dataSourceId == B2BDataSourceEnum.USATON_GA.id) {
                result = usatonGaOrderService.createAppointRequestEntity(effectiveDate, remarks, updater);
            } else if (dataSourceId == B2BDataSourceEnum.MQI.id) {
                result = mqiOrderService.createAppointRequestEntity(effectiveDate, remarks, updater);
            } else if (dataSourceId == B2BDataSourceEnum.JINRAN.id) {
                result = jinRanOrderService.createAppointRequestEntity(effectiveDate, remarks, updater);
            } else if (dataSourceId == B2BDataSourceEnum.TMALL.id) {
                result = tmallOrderService.createTmallAppointRequestEntity(effectiveDate, updater, servicePointId, engineerId);
            } else if (dataSourceId == B2BDataSourceEnum.JD.id) {
                result = jdOrderService.createJdPlanAndAppointRequestEntity(pendingType, effectiveDate, servicePointId, engineerId);
            } else if (dataSourceId == B2BDataSourceEnum.INSE.id) {
                result = inseOrderService.createAppointRequestEntity(effectiveDate);
            } else if (dataSourceId == B2BDataSourceEnum.KONKA.id) {
                result = konkaOrderService.createAppointRequestEntity(effectiveDate, updater, remarks);
            } else if (dataSourceId == B2BDataSourceEnum.JOYOUNG.id) {
                result = joyoungOrderService.createAppointRequestEntity(pendingType, effectiveDate, updater, remarks);
            } else if (dataSourceId == B2BDataSourceEnum.SUNING.id) {
                result = suningOrderService.createSuningAppointRequestEntity(effectiveDate, remarks);
            } else if (dataSourceId == B2BDataSourceEnum.JDUE.id) {
                result = jdueOrderService.createAppointRequestEntity(effectiveDate, remarks, updater);
            } else if (dataSourceId == B2BDataSourceEnum.JDUEPLUS.id) {
                result = jduePlusOrderService.createAppointRequestEntity(effectiveDate, remarks, updater);
            } else if (dataSourceId == B2BDataSourceEnum.XYINGYAN.id) {
                result = xyyPlusOrderService.createAppointRequestEntity(effectiveDate, servicePointId, engineerId);
            } else if (dataSourceId == B2BDataSourceEnum.LB.id) {
                result = lbOrderService.createAppointRequestEntity(effectiveDate, servicePointId, engineerId);
            } else if (dataSourceId == B2BDataSourceEnum.PDD.id) {
                result = pddOrderService.createAppointRequestEntity(effectiveDate, remarks);
            } else if (dataSourceId == B2BDataSourceEnum.VIOMI.id) {
                result = vioMiOrderService.createAppointRequestEntity(effectiveDate, updater, remarks);
            } else if (dataSourceId == B2BDataSourceEnum.SF.id) {
                result = sfOrderService.createAppointRequestEntity(effectiveDate, servicePointId, engineerId, updater, remarks);
            } else if (dataSourceId == B2BDataSourceEnum.PHILIPS.id) {
                result = philipsOrderService.createAppointRequestEntity(effectiveDate);
//                result = philipsNewOrderService.createAppointRequestEntity(effectiveDate);
            } else {
                result = new TwoTuple<>(true, null);
            }
            if (result.getAElement() && result.getBElement() != null) {
                setB2BOrderStatusUpdateReqEntityProperties(result.getBElement(), B2BOrderStatusEnum.APPOINTED, B2BOrderActionEnum.APPOINT,
                        dataSourceId, b2bOrderId, b2bOrderNo, orderId, orderNo, updater, updateDate);
                sendB2BOrderStatusUpdateMessage(result.getBElement().build());
            } else {
                if (!result.getAElement()) {
                    B2BOrderStatusUpdateFailureLog log = new B2BOrderStatusUpdateFailureLog(dataSourceId, b2bOrderNo, updater, servicePointId, engineerId, updateDate, effectiveDate, remarks, B2BOrderStatusEnum.APPOINTED);
                    saveFailureLog(log, "appointB2BOrder");
                }
            }
        }
    }

    /**
     * B2B上门服务
     */
    public void serviceOrder(Order order, Long servicePointId, Long engineerId, User updater, Date updateDate) {
        if (order != null && order.getId() != null && order.getOrderCondition() != null && order.getOrderCondition().getCustomerId() > 0) {
            updateOrderStatus(B2BDataSourceEnum.UM, order.getOrderCondition().getCustomerId(), order.getId(), B2BOrderStatusEnum.SERVICED);
        }
        if (order != null && order.getDataSource() != null) {
            serviceB2BOrder(order.getDataSourceId(), order.getB2bOrderId(), order.getWorkCardId(), order.getId(), order.getOrderNo(), order.getQuarter(),
                    servicePointId, engineerId, updater, updateDate, "");
        }
    }

    private void serviceB2BOrder(Integer dataSourceId, Long b2bOrderId, String b2bOrderNo, Long orderId, String orderNo, String quarter,
                                 Long servicePointId, Long engineerId, User updater, Date updateDate, String remarks) {
        if (isNeedSendOrderStatusMsgToB2B(dataSourceId) && StringUtils.isNotBlank(b2bOrderNo)) {
            TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> result;
            if (dataSourceId == B2BDataSourceEnum.KONKA.id) {
                result = konkaOrderService.createServiceRequestEntity(updateDate, servicePointId, engineerId, remarks);
            } else if (dataSourceId == B2BDataSourceEnum.JOYOUNG.id) {
                result = joyoungOrderService.createServiceRequestEntity(updateDate, servicePointId, engineerId, remarks);
            } else if (dataSourceId == B2BDataSourceEnum.TMALL.id) {
                result = tmallOrderService.createServiceRequestEntity();
            } else if (dataSourceId == B2BDataSourceEnum.PDD.id) {
                result = pddOrderService.createServiceRequestEntity(updateDate, servicePointId, engineerId, remarks);
            } else if (dataSourceId == B2BDataSourceEnum.VIOMI.id) {
                result = vioMiOrderService.createServiceRequestEntity(orderId, quarter, updater);
            } else if (dataSourceId == B2BDataSourceEnum.MQI.id) {
                result = mqiOrderService.createServiceRequestEntity(updateDate, servicePointId, engineerId, remarks);
            }  else if (dataSourceId == B2BDataSourceEnum.JINRAN.id) {
                result = jinRanOrderService.createServiceRequestEntity(updateDate, servicePointId, engineerId, remarks);
            }
//            else if (dataSourceId == B2BDataSourceEnum.PHILIPS.id) {
//                result = philipsNewOrderService.createServiceRequestEntity();
//            }
            //TODO: 确认上门时，不调用樱雪的上门接口
//            else if (dataSourceId == B2BDataSourceEnum.INSE.id) {
//                result = inseOrderService.createServiceRequestEntity();
//            }
            else {
                result = new TwoTuple<>(true, null);
            }
            if (result.getAElement() && result.getBElement() != null) {
                setB2BOrderStatusUpdateReqEntityProperties(result.getBElement(), B2BOrderStatusEnum.SERVICED, B2BOrderActionEnum.SERVICE,
                        dataSourceId, b2bOrderId, b2bOrderNo, orderId, orderNo, updater, updateDate);
                sendB2BOrderStatusUpdateMessage(result.getBElement().build());
            } else {
                if (!result.getAElement()) {
                    B2BOrderStatusUpdateFailureLog log = new B2BOrderStatusUpdateFailureLog(dataSourceId, b2bOrderNo, servicePointId, engineerId, updater, updateDate, B2BOrderStatusEnum.SERVICED);
                    saveFailureLog(log, "serviceB2BOrder");
                }
            }
        }
    }

    @Transactional()
    public void appCompleteOrder(Order order, User updater, Date updateDate) {
//        if (order != null && order.getDataSource() != null) {
//            OrderCondition condition = order.getOrderCondition();
//            if (condition == null || condition.getCreateDate() == null || condition.getCustomer() == null || condition.getCustomer().getId() == null) {
//                order = orderCacheReadService.getOrderById(order.getId(), order.getQuarter(), OrderUtils.OrderDataLevel.CONDITION, true, false);
//            }
        if (order != null && order.getDataSource() != null) {
            long customerId = order.getOrderCondition() != null && order.getOrderCondition().getCustomer() != null ? order.getOrderCondition().getCustomerId() : 0;
            long servicePointId = order.getOrderCondition() != null && order.getOrderCondition().getServicePoint() != null && order.getOrderCondition().getServicePoint().getId() != null ? order.getOrderCondition().getServicePoint().getId() : 0;
            Date orderCreateDate = order.getOrderCondition() != null && order.getOrderCondition().getCreateDate() != null ? order.getOrderCondition().getCreateDate() : null;
            appCompleteB2BOrder(order.getDataSourceId(), order.getB2bOrderId(), order.getWorkCardId(),
                    order.getId(), order.getOrderNo(), order.getQuarter(), customerId, servicePointId, order.getItems(), orderCreateDate, updater, updateDate);
        }
//        }
    }

    private void appCompleteB2BOrder(Integer dataSourceId, Long b2bOrderId, String b2bOrderNo, Long orderId, String orderNo, String quarter,
                                     Long customerId, Long servicePointId, List<OrderItem> orderItems,
                                     Date orderCreateDate, User updater, Date updateDate) {
        if (isNeedSendOrderStatusMsgToB2B(dataSourceId) && StringUtils.isNotBlank(b2bOrderNo)) {
            TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> result;
            if (dataSourceId == B2BDataSourceEnum.VIOMI.id) {
                result = vioMiOrderService.createAppCompleteRequestEntity(orderId, quarter, customerId, servicePointId, orderItems, orderCreateDate, updater);
            } else {
                result = new TwoTuple<>(true, null);
            }
            if (result.getAElement() && result.getBElement() != null) {
                setB2BOrderStatusUpdateReqEntityProperties(result.getBElement(), B2BOrderStatusEnum.APP_COMPLETED, B2BOrderActionEnum.APP_COMPLETE,
                        dataSourceId, b2bOrderId, b2bOrderNo, orderId, orderNo, updater, updateDate);
                sendB2BOrderStatusUpdateMessage(result.getBElement().build());

            } else {
                if (!result.getAElement()) {
                    B2BOrderStatusUpdateFailureLog log = new B2BOrderStatusUpdateFailureLog(dataSourceId, orderId, updater, updateDate, B2BOrderStatusEnum.APP_COMPLETED);
                    saveFailureLog(log, "appCompleteB2BOrder");
                }
            }
        }
    }

    @Transactional()
    public void validateOrder(Order order, OrderValidate orderValidate, User updater, Date updateDate) {
        if (order != null && order.getDataSource() != null) {
            long servicePointId = order.getOrderCondition() != null && order.getOrderCondition().getServicePoint() != null && order.getOrderCondition().getServicePoint().getId() != null ? order.getOrderCondition().getServicePoint().getId() : 0;
            Date orderCreateDate = order.getOrderCondition() != null && order.getOrderCondition().getCreateDate() != null ? order.getOrderCondition().getCreateDate() : null;
            validateB2BOrder(order.getDataSourceId(), order.getB2bOrderId(), order.getWorkCardId(),
                    order.getId(), order.getOrderNo(), order.getQuarter(), servicePointId, orderValidate, orderCreateDate, updater, updateDate);
        }
    }

    private void validateB2BOrder(Integer dataSourceId, Long b2bOrderId, String b2bOrderNo, Long orderId, String orderNo, String quarter,
                                  Long servicePointId, OrderValidate orderValidate,
                                  Date orderCreateDate, User updater, Date updateDate) {
        if (isNeedSendOrderStatusMsgToB2B(dataSourceId) && StringUtils.isNotBlank(b2bOrderNo)) {
            TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> result;
            if (dataSourceId == B2BDataSourceEnum.VIOMI.id) {
                result = vioMiOrderService.createValidateRequestEntity(orderId, quarter, servicePointId, orderCreateDate, orderValidate, updater);
            } else {
                result = new TwoTuple<>(true, null);
            }
            if (result.getAElement() && result.getBElement() != null) {
                setB2BOrderStatusUpdateReqEntityProperties(result.getBElement(), B2BOrderStatusEnum.VALIDATE, B2BOrderActionEnum.VALIDATE,
                        dataSourceId, b2bOrderId, b2bOrderNo, orderId, orderNo, updater, updateDate);
                sendB2BOrderStatusUpdateMessage(result.getBElement().build());
            } else {
                if (!result.getAElement()) {
                    B2BOrderStatusUpdateFailureLog log = new B2BOrderStatusUpdateFailureLog(dataSourceId, orderId, updater, updateDate, B2BOrderStatusEnum.VALIDATE);
                    saveFailureLog(log, "validateB2BOrder");
                }
            }
        }
    }

    public void retryCompletedOrder(List<Long> orderIds) {
        for (Long id : orderIds) {
            Order order = orderCacheReadService.getOrderById(id, null, OrderUtils.OrderDataLevel.CONDITION, true, false);
            if (order.getOrderCondition().getStatus().getIntValue().equals(Order.ORDER_STATUS_COMPLETED) ||
                    order.getOrderCondition().getStatus().getIntValue().equals(Order.ORDER_STATUS_CHARGED)) {
                User updater = new User(1L, KKL_SYSTEM_USER.getName(), KKL_SYSTEM_USER.getMobile());
                completeOrder(order, order.getOrderCondition().getCloseDate(), updater, order.getOrderCondition().getCloseDate());
            }
        }
    }

    /**
     * B2B完成工单
     */
    @Transactional()
    public void completeOrder(Order order, Date completedDate, User updater, Date updateDate) {
        if (order != null && order.getId() != null) {
            OrderCondition condition = order.getOrderCondition();
            if (condition != null) {
                updateOrderStatus(B2BDataSourceEnum.UM, condition.getCustomerId(), order.getId(), B2BOrderStatusEnum.COMPLETED, completedDate);
            } else {
                Order cachedOrder = orderCacheReadService.getOrderById(order.getId(), order.getQuarter(), OrderUtils.OrderDataLevel.CONDITION, true, false);
                if (cachedOrder != null) {
                    condition = cachedOrder.getOrderCondition();
                    updateOrderStatus(B2BDataSourceEnum.UM, condition.getCustomerId(), cachedOrder.getId(), B2BOrderStatusEnum.COMPLETED, completedDate);
                }
            }
            if (order.getDataSource() != null) {
                Date appCompleteDate = condition != null ? condition.getAppCompleteDate() : null;
                Double orderCharge = order.getOrderFee() != null && order.getOrderFee().getOrderCharge() != null ? order.getOrderFee().getOrderCharge() : 0;
                long servicePointId = order.getOrderCondition() != null && order.getOrderCondition().getServicePoint() != null && order.getOrderCondition().getServicePoint().getId() != null ? order.getOrderCondition().getServicePoint().getId() : 0;
                completeB2BOrder(order.getDataSourceId(), order.getB2bOrderId(), order.getWorkCardId(), order.getId(), order.getOrderNo(), order.getQuarter(), order.getItems(), servicePointId, orderCharge,
                        completedDate, appCompleteDate, updater, updateDate, "");
            }
        }
    }

    private void completeB2BOrder(Integer dataSourceId, Long b2bOrderId, String b2bOrderNo, Long orderId, String orderNo, String quarter, List<OrderItem> orderItems, Long servicePointId, double orderCharge,
                                  Date effectiveDate, Date appCompleteDate, User updater, Date updateDate, String remarks) {
        if (isNeedSendOrderStatusMsgToB2B(dataSourceId) && StringUtils.isNotBlank(b2bOrderNo)) {
            TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> result;
            if (B2BDataSourceEnum.isTooneDataSourceId(dataSourceId)) {
                result = canboOrderService.createCompleteRequestEntity(orderId, quarter, orderItems);
            } else if (dataSourceId == B2BDataSourceEnum.WEBER.id) {
                result = weberOrderService.createCompleteRequestEntity(orderId, quarter, orderItems);
            } else if (dataSourceId == B2BDataSourceEnum.MBO.id) {
                result = mboOrderService.createCompleteRequestEntity(orderId, quarter, orderItems);
            } else if (dataSourceId == B2BDataSourceEnum.JINJING.id) {
                result = jinJingOrderService.createCompleteRequestEntity(orderId, quarter, orderItems);
            } else if (dataSourceId == B2BDataSourceEnum.USATON_GA.id) {
                result = usatonGaOrderService.createCompleteRequestEntity(orderId, quarter, orderItems);
            } else if (dataSourceId == B2BDataSourceEnum.MQI.id) {
                result = mqiOrderService.createCompleteRequestEntity(orderId, quarter, orderItems);
            } else if (dataSourceId == B2BDataSourceEnum.JINRAN.id) {
                result = jinRanOrderService.createCompleteRequestEntity(orderId, quarter, orderItems);
            }  else if (dataSourceId == B2BDataSourceEnum.TMALL.id) {
//                result = tmallOrderService.createTmallCompleteRequestEntity(effectiveDate, updater);
                result = tmallOrderService.createTmallCompleteRequestEntityNew(orderId, quarter, orderItems, effectiveDate, updater);
            } else if (dataSourceId == B2BDataSourceEnum.JD.id) {
                result = jdOrderService.createJdCompleteRequestEntityNew(updater, orderId, quarter, orderItems);
            } else if (dataSourceId == B2BDataSourceEnum.INSE.id) {
                result = inseOrderService.createInseCompleteRequestEntity(orderId, quarter, orderItems);
            } else if (dataSourceId == B2BDataSourceEnum.KONKA.id) {
                result = konkaOrderService.createCompleteRequestEntity(orderId, quarter, orderItems, effectiveDate, remarks);
            } else if (dataSourceId == B2BDataSourceEnum.JOYOUNG.id) {
                result = joyoungOrderService.createCompleteRequestEntity(orderId, quarter, orderItems, servicePointId, remarks);
            } else if (dataSourceId == B2BDataSourceEnum.SUNING.id) {
                result = suningOrderService.createCompleteRequestEntity(orderId, quarter, effectiveDate);
            } else if (dataSourceId == B2BDataSourceEnum.JDUE.id) {
                result = jdueOrderService.createCompleteRequestEntity(orderId, quarter, orderItems);
            } else if (dataSourceId == B2BDataSourceEnum.JDUEPLUS.id) {
                result = jduePlusOrderService.createCompleteRequestEntity(orderId, quarter, orderItems);
            } else if (dataSourceId == B2BDataSourceEnum.XYINGYAN.id) {
                result = xyyPlusOrderService.createCompleteRequestEntity(orderId, quarter, orderItems, orderCharge);
            } else if (dataSourceId == B2BDataSourceEnum.LB.id) {
                result = lbOrderService.createCompleteRequestEntity(orderId, quarter, orderItems, orderCharge);
            } else if (dataSourceId == B2BDataSourceEnum.PDD.id) {
                result = pddOrderService.createCompleteRequestEntity(orderId, quarter, effectiveDate);
            } else if (dataSourceId == B2BDataSourceEnum.VIOMI.id) {
                result = vioMiOrderService.createCompleteRequestEntity(updater);
            } else if (dataSourceId == B2BDataSourceEnum.SF.id) {
                result = sfOrderService.createCompleteRequestEntity(orderId, quarter);
            } else if (dataSourceId == B2BDataSourceEnum.PHILIPS.id) {
                result = philipsOrderService.createCompleteRequestEntity(orderId, quarter);
//                result = philipsNewOrderService.createCompleteRequestEntity(orderId, quarter, orderItems, effectiveDate);
            } else {
                result = new TwoTuple<>(true, null);
            }
            if (result.getAElement() && result.getBElement() != null) {
                setB2BOrderStatusUpdateReqEntityProperties(result.getBElement(), B2BOrderStatusEnum.COMPLETED, B2BOrderActionEnum.COMPLETE,
                        dataSourceId, b2bOrderId, b2bOrderNo, orderId, orderNo, updater, updateDate);
                result.getBElement().setAppCompleteDt(appCompleteDate == null ? 0L : appCompleteDate.getTime());
                sendB2BOrderStatusUpdateMessage(result.getBElement().build());

            } else {
                if (!result.getAElement()) {
                    B2BOrderStatusUpdateFailureLog log = new B2BOrderStatusUpdateFailureLog(orderId, quarter, orderItems, dataSourceId, b2bOrderNo, updater, updateDate, effectiveDate, remarks, B2BOrderStatusEnum.COMPLETED);
                    saveFailureLog(log, "completeB2BOrder");
                }
            }
        }
    }

    /**
     * 退单申请（工单退单申请时调用）
     */
    public void applyReturnOrder(Order order, Integer kklCancelType, Date applyDate, String remarks, String verifyCode, User updater, Date updateDate) {
        if (order != null && order.getDataSource() != null) {
            Long servicePointId = order.getOrderCondition() != null && order.getOrderCondition().getServicePoint() != null && order.getOrderCondition().getServicePoint().getId() != null ? order.getOrderCondition().getServicePoint().getId() : 0;
            Long engineerId = order.getOrderCondition() != null && order.getOrderCondition().getEngineer() != null && order.getOrderCondition().getEngineer().getId() != null ? order.getOrderCondition().getEngineer().getId() : 0;
            Long areaId = order.getOrderCondition() != null && order.getOrderCondition().getArea() != null && order.getOrderCondition().getArea().getId() != null ? order.getOrderCondition().getArea().getId() : 0;
            String areaFullName = order.getOrderCondition() != null && order.getOrderCondition().getArea() != null && order.getOrderCondition().getArea().getFullName() != null ? order.getOrderCondition().getArea().getFullName() : "";
            applyForCancelB2BOrder(order.getDataSourceId(), order.getB2bOrderId(), order.getWorkCardId(), order.getId(), order.getOrderNo(),
                    kklCancelType, verifyCode, areaId, areaFullName,
                    applyDate, remarks, updater, updateDate, servicePointId, engineerId);
        }
    }

    private void applyForCancelB2BOrder(Integer dataSourceId, Long b2bOrderId, String b2bOrderNo, Long orderId, String orderNo,
                                        Integer kklCancelType, String verifyCode, Long areaId, String areaFullName,
                                        Date effectiveDate, String remarks, User updater, Date updateDate, Long servicePointId, Long engineerId) {
        if (isNeedSendOrderStatusMsgToB2B(dataSourceId) && StringUtils.isNotBlank(b2bOrderNo)) {
            TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> result;
            if (dataSourceId == B2BDataSourceEnum.TMALL.id) {
                result = tmallOrderService.createTmallAppointRequestEntity(effectiveDate, updater, servicePointId, engineerId);
            } else if (dataSourceId == B2BDataSourceEnum.JD.id) {
                result = jdOrderService.createJdOnlyAppointRequestEntity(effectiveDate);
            } else if (dataSourceId == B2BDataSourceEnum.XYINGYAN.id) {
                result = xyyPlusOrderService.createReturnOrderApplyRequestEntity(remarks);
            } else if (dataSourceId == B2BDataSourceEnum.LB.id) {
                result = lbOrderService.createReturnOrderApplyRequestEntity(remarks);
            } else if (dataSourceId == B2BDataSourceEnum.INSE.id) {
                result = inseOrderService.createReturnOrderApplyRequestEntity(remarks);
            } else if (dataSourceId == B2BDataSourceEnum.VIOMI.id) {
                result = vioMiOrderService.createApplyForCancelRequestEntity(kklCancelType, updater, verifyCode, remarks);
            } else if (dataSourceId == B2BDataSourceEnum.SF.id) {
                result = sfOrderService.createApplyForCancelRequestEntity(areaFullName, areaId, remarks);
            } else {
                result = new TwoTuple<>(true, null);
            }
            if (result.getAElement() && result.getBElement() != null) {
                setB2BOrderStatusUpdateReqEntityProperties(result.getBElement(), B2BOrderStatusEnum.APPLIED_FOR_CANCEL, B2BOrderActionEnum.CONVERTED_CANCEL,
                        dataSourceId, b2bOrderId, b2bOrderNo, orderId, orderNo, updater, updateDate);
                sendB2BOrderStatusUpdateMessage(result.getBElement().build());
            } else {
                if (!result.getAElement()) {
                    B2BOrderStatusUpdateFailureLog log = new B2BOrderStatusUpdateFailureLog(dataSourceId, b2bOrderNo, updater, null, null, updateDate, effectiveDate, remarks, B2BOrderStatusEnum.APPOINTED);
                    saveFailureLog(log, "applyForCancelB2BOrder");
                }
            }
        }
    }

    public void retryReturnOrder(List<Long> orderIds) {
        for (Long id : orderIds) {
            Order order = orderCacheReadService.getOrderById(id, null, OrderUtils.OrderDataLevel.STATUS, true, false);
            if (order.getOrderCondition().getStatus().getIntValue().equals(Order.ORDER_STATUS_RETURNED)) {
                User updater = new User(KKL_SYSTEM_USER.getId(), KKL_SYSTEM_USER.getName(), KKL_SYSTEM_USER.getMobile());
                approveReturnOrder(order, order.getOrderStatus().getCancelResponsible().getIntValue(), order.getOrderCondition().getCloseDate(), "", updater, order.getOrderCondition().getCloseDate());
            }
        }
    }

    /**
     * 退单审核
     */
    public void approveReturnOrder(Order order, Integer kklCancelType, Date approveDate, String remarks, User updater, Date updateDate) {
        if (order != null && order.getId() != null && order.getOrderCondition() != null && order.getOrderCondition().getCustomerId() > 0) {
            updateOrderStatus(B2BDataSourceEnum.UM, order.getOrderCondition().getCustomerId(), order.getId(), B2BOrderStatusEnum.CANCELED, approveDate);
        }
        if (order != null && order.getDataSource() != null) {
            cancelB2BOrder(B2BOrderActionEnum.RETURN, order.getDataSourceId(), order.getB2bOrderId(), order.getWorkCardId(), order.getId(), order.getOrderNo(),
                    kklCancelType, approveDate, remarks, updater, updateDate);
        }
    }

//    public void returnB2BOrder(Integer dataSourceId, String b2bOrderNo, Long orderId, String orderNo, Integer kklCancelType, User updater, Date updateDate, Date effectiveDate, String remarks) {
//        cancelB2BOrder(B2BOrderActionEnum.RETURN, dataSourceId, b2bOrderNo, orderId, orderNo, kklCancelType, updater, updateDate, effectiveDate, remarks);
//    }

    public void retryCancelOrder(List<Long> orderIds) {
        for (Long id : orderIds) {
            Order order = orderCacheReadService.getOrderById(id, null, OrderUtils.OrderDataLevel.STATUS, true, false);
            if (order.getOrderCondition().getStatus().getIntValue().equals(Order.ORDER_STATUS_CANCELED)) {
                User updater = new User(KKL_SYSTEM_USER.getId(), KKL_SYSTEM_USER.getName(), KKL_SYSTEM_USER.getMobile());
                cancelOrder(order, order.getOrderStatus().getCancelResponsible().getIntValue(), order.getOrderCondition().getCloseDate(), "", updater, order.getOrderCondition().getCloseDate());
            }
        }
    }

    /**
     * 取消工单
     */
    public void cancelOrder(Order order, Integer kklCancelType, Date approveDate, String remarks, User updater, Date updateDate) {
        if (order != null && order.getId() != null && order.getOrderCondition() != null && order.getOrderCondition().getCustomerId() > 0) {
            updateOrderStatus(B2BDataSourceEnum.UM, order.getOrderCondition().getCustomerId(), order.getId(), B2BOrderStatusEnum.CANCELED, approveDate);
        }
        if (order != null && order.getDataSource() != null) {
            cancelB2BOrder(B2BOrderActionEnum.CONVERTED_CANCEL, order.getDataSourceId(), order.getB2bOrderId(), order.getWorkCardId(), order.getId(), order.getOrderNo(),
                    kklCancelType, approveDate, remarks, updater, updateDate);
        }
    }
//    public void cancelConvertedB2BOrder(Integer dataSourceId, String b2bOrderNo, Long orderId, String orderNo, Integer kklCancelType, User updater, Date updateDate, Date effectiveDate, String remarks) {
//        cancelB2BOrder(B2BOrderActionEnum.CONVERTED_CANCEL, dataSourceId, b2bOrderNo, orderId, orderNo, kklCancelType, updater, updateDate, effectiveDate, remarks);
//    }

    /**
     * B2B取消工单
     */
    private void cancelB2BOrder(B2BOrderActionEnum actionType, Integer dataSourceId, Long b2bOrderId, String b2bOrderNo, Long orderId, String orderNo,
                                Integer kklCancelType, Date effectiveDate, String remarks, User updater, Date updateDate) {
        if (isNeedSendOrderStatusMsgToB2B(dataSourceId) && StringUtils.isNotBlank(b2bOrderNo)) {
            TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> result;
            if (B2BDataSourceEnum.isTooneDataSourceId(dataSourceId)) {
                result = canboOrderService.createTooneCancelRequestEntity(effectiveDate, updater, remarks);
            } else if (dataSourceId == B2BDataSourceEnum.WEBER.id) {
                result = weberOrderService.createCancelRequestEntity(effectiveDate, updater, remarks);
            } else if (dataSourceId == B2BDataSourceEnum.MBO.id) {
                result = mboOrderService.createCancelRequestEntity(effectiveDate, updater, remarks);
            } else if (dataSourceId == B2BDataSourceEnum.SUPOR.id) {
                result = suporOrderService.createCancelRequestEntity(kklCancelType);
            } else if (dataSourceId == B2BDataSourceEnum.JINJING.id) {
                result = jinJingOrderService.createCancelRequestEntity(effectiveDate, updater, remarks);
            } else if (dataSourceId == B2BDataSourceEnum.USATON_GA.id) {
                result = usatonGaOrderService.createCancelRequestEntity(effectiveDate, updater, remarks);
            } else if (dataSourceId == B2BDataSourceEnum.MQI.id) {
                result = mqiOrderService.createCancelRequestEntity(effectiveDate, updater, remarks);
            } else if (dataSourceId == B2BDataSourceEnum.JINRAN.id) {
                result = jinRanOrderService.createCancelRequestEntity(effectiveDate, updater, remarks);
            }  else if (dataSourceId == B2BDataSourceEnum.TMALL.id) {
                result = tmallOrderService.createTmallCancelRequestEntity(updater, remarks);
            } else if (dataSourceId == B2BDataSourceEnum.JD.id) {
                result = jdOrderService.createJdCancelRequestEntity(kklCancelType, updater);
            } else if (dataSourceId == B2BDataSourceEnum.INSE.id) {
                result = inseOrderService.createInseCancelRequestEntity(kklCancelType, remarks);
            } else if (dataSourceId == B2BDataSourceEnum.KONKA.id) {
                result = konkaOrderService.createCancelRequestEntity(kklCancelType, effectiveDate, updater);
            } else if (dataSourceId == B2BDataSourceEnum.JOYOUNG.id) {
                result = joyoungOrderService.createCancelRequestEntity(kklCancelType, remarks, effectiveDate, updater);
            } else if (dataSourceId == B2BDataSourceEnum.SUNING.id) {
                result = suningOrderService.createSuningCancelRequestEntity(kklCancelType);
            } else if (dataSourceId == B2BDataSourceEnum.JDUE.id) {
                result = jdueOrderService.createCancelRequestEntity(effectiveDate, updater, remarks);
            } else if (dataSourceId == B2BDataSourceEnum.JDUEPLUS.id) {
                result = jduePlusOrderService.createCancelRequestEntity(effectiveDate, updater, remarks);
            } else if (dataSourceId == B2BDataSourceEnum.PDD.id) {
                result = pddOrderService.createCancelRequestEntity(kklCancelType);
            } else if (dataSourceId == B2BDataSourceEnum.VIOMI.id) {
                result = vioMiOrderService.createCancelRequestEntity(kklCancelType, updater, remarks);
            } else if (dataSourceId == B2BDataSourceEnum.PHILIPS.id) {
                result = philipsOrderService.createCancelRequestEntity(kklCancelType, remarks);
//                result = philipsNewOrderService.createCancelRequestEntity(kklCancelType, remarks);
            } else {
                result = new TwoTuple<>(true, null);
            }
            if (result.getAElement() && result.getBElement() != null) {
                setB2BOrderStatusUpdateReqEntityProperties(result.getBElement(), B2BOrderStatusEnum.CANCELED, actionType,
                        dataSourceId, b2bOrderId, b2bOrderNo, orderId, orderNo, updater, updateDate);
                sendB2BOrderStatusUpdateMessage(result.getBElement().build());
            } else {
                if (!result.getAElement()) {
                    B2BOrderStatusUpdateFailureLog log = new B2BOrderStatusUpdateFailureLog(dataSourceId, b2bOrderNo, kklCancelType, updater, updateDate, effectiveDate, remarks, B2BOrderStatusEnum.CANCELED);
                    saveFailureLog(log, "cancelB2BOrder");
                }
            }
        }
    }

    public void retryChargeOrder(List<OrderCondition> conditions) {
        for (OrderCondition condition : conditions) {
            Date now = new Date();
            if (condition.getStatus().getIntValue().equals(Order.ORDER_STATUS_CHARGED)) {
                User updater = new User(KKL_SYSTEM_USER.getId(), KKL_SYSTEM_USER.getName(), KKL_SYSTEM_USER.getMobile());
                chargeB2BOrder(condition.getCustomerId(), condition.getOrderId(), condition.getQuarter(), updater.getId(), now.getTime());
            }
        }
    }

    /**
     * B2B工单对账操作
     */
    void chargeB2BOrder(Long customerId, Long kklOrderId, String kklQuarter, Long updaterId, Long updateAt) {
        TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> result;
        int dataSourceId = 0;
        if (B2BOrderUtils.canSendOrderDataToMS(customerId)) {
            dataSourceId = B2BDataSourceEnum.UM.id;
            result = umOrderService.createChargeRequestEntity(kklOrderId, kklQuarter, updateAt);
        } else {
            result = new TwoTuple<>(true, null);
        }
        if (result.getAElement() && result.getBElement() != null) {
            result.getBElement()
                    .setStatus(B2BOrderStatusEnum.CHARGED)
                    .setActionType(B2BOrderActionEnum.CHARGE)
                    .setDataSourceId(dataSourceId)
                    .setOrderId(kklOrderId)
                    .setChargeAt(updateAt)
                    .setUpdaterId(updaterId)
                    .setUpdateDate(new Date(updateAt));
            sendB2BOrderStatusUpdateMessage(result.getBElement().build());
        } else {
            if (!result.getAElement()) {
                B2BOrderStatusUpdateFailureLog log = new B2BOrderStatusUpdateFailureLog(dataSourceId, kklOrderId, new User(updaterId), new Date(updateAt), B2BOrderStatusEnum.CHARGED);
                saveFailureLog(log, "chargeB2BOrder");
            }
        }
    }

    /**
     * 换货-确认收货
     */
    public MSResponse confirmReceived(Order order, String remarks, User updater, Date updateDate) {
        MSResponse response = new MSResponse(MSErrorCode.SUCCESS);
        if (order == null || order.getDataSource() == null) {
            return response;
        }
        //end
        if (isNeedSendOrderStatusMsgToB2B(order.getDataSourceId()) && StringUtils.isNotBlank(order.getWorkCardId())) {
            if (order.getDataSourceId() == B2BDataSourceEnum.VIOMI.id) {
                return vioMiOrderService.confirmReceived(order.getDataSourceId(), order.getB2bOrderId(), order.getWorkCardId(), updater, updateDate, remarks);
            }
        }
        return response;
    }

    /**
     * 退换货-拆装
     *
     * @param b2bOrderId sd_order.b2b_order_id
     * @param b2bOrderNo sd_order.workcard_id
     */
    public MSResponse orderDismounting(Integer dataSource, Integer orderType, Long b2bOrderId, String b2bOrderNo, OrderReturnComplete item, User updater, Date updateDate) {
        MSResponse response = new MSResponse(MSErrorCode.SUCCESS);
        if (dataSource == null || dataSource <= 1 || item == null) {
            return response;
        }
        if (isNeedSendOrderStatusMsgToB2B(dataSource) && !com.wolfking.jeesite.common.utils.StringUtils.longIsNullOrLessSpecialValue(b2bOrderId, 0)) {
            if (dataSource == B2BDataSourceEnum.VIOMI.id) {
                return vioMiOrderService.orderDismounting(dataSource, orderType, b2bOrderId, b2bOrderNo, updater, updateDate, item);
            }
        }
        return response;
    }

    /**
     * 退换货-寄回
     */
    public MSResponse backLogistics(Integer dataSource, Integer orderType, Long b2bOrderId, String b2bOrderNo, OrderReturnComplete item, User updater, Date updateDate) {
        MSResponse response = new MSResponse(MSErrorCode.SUCCESS);
        if (dataSource == null || dataSource <= 1 || item == null) {
            return response;
        }
        if (isNeedSendOrderStatusMsgToB2B(dataSource) && !com.wolfking.jeesite.common.utils.StringUtils.longIsNullOrLessSpecialValue(b2bOrderId, 0)) {
            if (dataSource == B2BDataSourceEnum.VIOMI.id) {
                return vioMiOrderService.backLogistics(dataSource, orderType, b2bOrderId, b2bOrderNo, updater, updateDate, item);
            }
        }
        return response;
    }

    //endregion 创建工单状态变更消息实体

    //------------------------------------------------------------------------------------------往队列发送消息、处理队列消息

    //region 向队列发送消息、处理消息

    /**
     * 发送B2B工单的状态变更消息
     */
    private void sendB2BOrderStatusUpdateMessage(B2BOrderStatusUpdateReqEntity reqEntity) {
        MQB2BOrderStatusUpdateMessage.B2BOrderStatusUpdateMessage.Builder builder = MQB2BOrderStatusUpdateMessage.B2BOrderStatusUpdateMessage.newBuilder();
        builder.setDataSource(reqEntity.getDataSourceId())
                .setOrderId(reqEntity.getOrderId())
                .setKklOrderNO(reqEntity.getKklOrderNo())
                .setB2BOrderId(reqEntity.getB2bOrderId())
                .setB2BOrderNo(reqEntity.getB2bOrderNo())
                .setStatus(reqEntity.getStatus().value)
                .setUpdaterId(reqEntity.getUpdaterId())
                .setUpdaterMobile(reqEntity.getUpdaterMobile())
                .setUpdaterName(reqEntity.getUpdaterName())
                .setEngineerId(StringUtils.toString(reqEntity.getEngineerId()))
                .setEngineerName(StringUtils.toString(reqEntity.getEngineerName()))
                .setEngineerMobile(StringUtils.toString(reqEntity.getEngineerMobile()))
                .setUpdateDt(reqEntity.getUpdateDate().getTime())
                .setEffectiveDt(reqEntity.getEffectiveDate() != null ? reqEntity.getEffectiveDate().getTime() : 0)
                .setRemarks(reqEntity.getRemarks())
                .setAppointmentStatus(reqEntity.getAppointmentStatus())
                .setInstallStatus(reqEntity.getInstallStaus())
                .setServicePointId(reqEntity.getServicePointId() == null ? 0 : reqEntity.getServicePointId())
                .setMessageId(sequenceIdService.nextId())
                .setActionType(reqEntity.getActionType().value)
                .setOrderCharge(reqEntity.getOrderCharge() == null ? 0.0 : reqEntity.getOrderCharge())
                .setKklPendingType(StringUtils.toString(reqEntity.getPendingType()))
                .setActualTotalSurcharge(reqEntity.getActualTotalSurcharge() == null ? 0.0 : reqEntity.getActualTotalSurcharge())
                .setCustomerTotalCharge(reqEntity.getCustomerTotalCharge() == null ? 0.0 : reqEntity.getCustomerTotalCharge())
                .setChargeAt(reqEntity.getChargeAt() == null ? 0 : reqEntity.getChargeAt())
                .setAppCompleteDt(reqEntity.getAppCompleteDt() == null ? 0 : reqEntity.getAppCompleteDt())
                .setB2BReason(StringUtils.toString(reqEntity.getB2bReason()))
                .setLongitude(reqEntity.getLongitude() == null ? 0.0 : reqEntity.getLongitude())
                .setLatitude(reqEntity.getLatitude() == null ? 0.0 : reqEntity.getLatitude())
                .setVerifyCode(StringUtils.toString(reqEntity.getVerifyCode()))
                .setExtraField1(StringUtils.toString(reqEntity.getExtraField1()));
        if (reqEntity.getOrderCompletedItems() != null && !reqEntity.getOrderCompletedItems().isEmpty()) {
            MQB2BOrderStatusUpdateMessage.CompletedItem.Builder completedItemBuilder;
            MQB2BOrderStatusUpdateMessage.B2BSurchargeItem surchargeItem;
            MQB2BOrderStatusUpdateMessage.ErrorItem errorItem;
            MQB2BOrderStatusUpdateMessage.Material material;
            MQB2BOrderStatusUpdateMessage.PicItem picItem;
            for (B2BOrderCompletedItem item : reqEntity.getOrderCompletedItems()) {
                completedItemBuilder = MQB2BOrderStatusUpdateMessage.CompletedItem.newBuilder()
                        .setItemCode(StringUtils.toString(item.getB2bProductCode()))
                        .setPic1(StringUtils.toString(item.getPic1()))
                        .setPic2(StringUtils.toString(item.getPic2()))
                        .setPic3(StringUtils.toString(item.getPic3()))
                        .setPic4(StringUtils.toString(item.getPic4()))
                        .setBarcode(StringUtils.toString(item.getUnitBarcode()))
                        .setOutBarcode(StringUtils.toString(item.getOutBarcode()))
                        .setBuyDt(item.getBuyDt());
                for (B2BOrderCompletedItem.B2BSurchargeItem innerItem : item.getSurchargeItems()) {
                    surchargeItem = MQB2BOrderStatusUpdateMessage.B2BSurchargeItem.newBuilder()
                            .setCategoryId(innerItem.getCategoryId())
                            .setCategoryName(innerItem.getCategoryName())
                            .setItemId(innerItem.getItemId())
                            .setItemName(innerItem.getItemName())
                            .setItemQty(innerItem.getItemQty())
                            .setUnitPrice(innerItem.getUnitPrice())
                            .setTotalPrice(innerItem.getTotalPrice())
                            .build();
                    completedItemBuilder.addSurchargeItems(surchargeItem);
                }
                for (B2BOrderCompletedItem.ErrorItem innerItem : item.getErrorItems()) {
                    errorItem = MQB2BOrderStatusUpdateMessage.ErrorItem.newBuilder()
                            .setErrorTypeId(innerItem.getErrorTypeId())
                            .setErrorType(innerItem.getErrorType())
                            .setErrorCodeId(innerItem.getErrorCodeId())
                            .setErrorCode(innerItem.getErrorCode())
                            .setErrorAnalysisId(innerItem.getErrorAnalysisId())
                            .setErrorAnalysis(innerItem.getErrorAnalysis())
                            .setErrorActionId(innerItem.getErrorActionId())
                            .setErrorAction(innerItem.getErrorAction())
                            .build();
                    completedItemBuilder.addErrorItem(errorItem);
                }
                for (B2BOrderCompletedItem.Material innerItem : item.getMaterials()) {
                    material = MQB2BOrderStatusUpdateMessage.Material.newBuilder()
                            .setMaterialId(innerItem.getMaterialId())
                            .setMaterialCode(innerItem.getMaterialCode())
                            .setQty(innerItem.getQty())
                            .build();
                    completedItemBuilder.addMaterial(material);
                }
                for (B2BOrderCompletedItem.PicItem innerItem : item.getPicItems()) {
                    picItem = MQB2BOrderStatusUpdateMessage.PicItem.newBuilder()
                            .setCode(innerItem.getCode())
                            .setUrl(innerItem.getUrl())
                            .build();
                    completedItemBuilder.addPicItem(picItem);
                }
                builder.addCompletedItem(completedItemBuilder.build());
            }
        } else {
            if (reqEntity.getCompletedItems() != null && !reqEntity.getCompletedItems().isEmpty()) {
                MQB2BOrderStatusUpdateMessage.CompletedItem completedItem;
                for (CanboOrderCompleted.CompletedItem item : reqEntity.getCompletedItems()) {
                    completedItem = MQB2BOrderStatusUpdateMessage.CompletedItem.newBuilder()
                            .setItemCode(StringUtils.toString(item.getItemCode()))
                            .setPic1(StringUtils.toString(item.getPic1()))
                            .setPic2(StringUtils.toString(item.getPic2()))
                            .setPic3(StringUtils.toString(item.getPic3()))
                            .setPic4(StringUtils.toString(item.getPic4()))
                            .setBarcode(StringUtils.toString(item.getBarcode()))
                            .setOutBarcode(StringUtils.toString(item.getOutBarcode()))
                            .build();
                    builder.addCompletedItem(completedItem);
                }
            }
        }
        if (reqEntity.getServiceItems() != null && !reqEntity.getServiceItems().isEmpty()) {
            MQB2BOrderStatusUpdateMessage.ServiceItem serviceItem;
            for (B2BOrderServiceItem item : reqEntity.getServiceItems()) {
                serviceItem = MQB2BOrderStatusUpdateMessage.ServiceItem.newBuilder()
                        .setServiceItemId(item.getServiceItemId())
                        .setServiceAt(item.getServiceAt())
                        .setProductId(item.getProductId())
                        .setServiceTypeId(item.getServiceTypeId())
                        .setQty(item.getQty())
                        .setCharge(item.getCharge())
                        .build();
                builder.addServiceItem(serviceItem);
            }
        }
        if (reqEntity.getOrderPraiseItem() != null && !reqEntity.getOrderPraiseItem().getPicUrls().isEmpty()) {
            MQB2BOrderStatusUpdateMessage.PraiseItem praiseItem = MQB2BOrderStatusUpdateMessage.PraiseItem.newBuilder()
                    .addAllPicUrl(reqEntity.getOrderPraiseItem().getPicUrls())
                    .build();
            builder.setPraiseItem(praiseItem);
        }
        if (reqEntity.getOrderValidateItem() != null) {
            B2BOrderValidateItem params = reqEntity.getOrderValidateItem();
            B2BOrderValidateItem.ErrorItem innerItem = params.getErrorItem();
            MQB2BOrderStatusUpdateMessage.ErrorItem errorItem = MQB2BOrderStatusUpdateMessage.ErrorItem.newBuilder()
                    .setErrorTypeId(innerItem.getErrorTypeId())
                    .setErrorType(innerItem.getErrorType())
                    .setErrorCodeId(innerItem.getErrorCodeId())
                    .setErrorCode(innerItem.getErrorCode())
                    .setErrorAnalysisId(innerItem.getErrorAnalysisId())
                    .setErrorAnalysis(innerItem.getErrorAnalysis())
                    .setErrorActionId(innerItem.getErrorActionId())
                    .setErrorAction(innerItem.getErrorAction())
                    .build();
            MQB2BOrderStatusUpdateMessage.ValidateItem validateItem = MQB2BOrderStatusUpdateMessage.ValidateItem.newBuilder()
                    .setProductId(params.getProductId())
                    .setProductSn(params.getProductSn())
                    .setBuyDt(params.getBuyDt())
                    .setIsFault(params.getIsFault())
                    .setErrorDescription(params.getErrorDescription())
                    .setCheckValidateDetail(params.getCheckValidateDetail())
                    .setPackValidateDetail(params.getPackValidateDetail())
                    .setReceiver(params.getReceiver())
                    .setReceivePhone(params.getReceivePhone())
                    .setReceiveAddress(params.getReceiveAddress())
                    .setErrorItem(errorItem)
                    .addAllCheckValidateResultValues(params.getCheckValidateResultValues())
                    .addAllPackValidateResultValues(params.getPackValidateResultValues())
                    .addAllPicUrl(params.getPicUrls())
                    .build();
            builder.setValidateItem(validateItem);
        }
        b2BOrderStatusUpdateMQSender.send(builder.build());
    }


    /**
     * 处理B2B工单的状态变更消息
     */
    public MSResponse processB2BOrderStatusUpdateMessage(MQB2BOrderStatusUpdateMessage.B2BOrderStatusUpdateMessage message) {
        MSResponse response = new MSResponse<>(MSErrorCode.SUCCESS);
        if (B2BDataSourceEnum.isB2BDataSource(message.getDataSource())) {
            if (message.getDataSource() == B2BDataSourceEnum.TMALL.id) {
                response = tmallOrderService.sendOrderStatusUpdateCommandToB2B(message);
            } else if (message.getDataSource() == B2BDataSourceEnum.CANBO.id || message.getDataSource() == B2BDataSourceEnum.USATON.id) {
                response = canboOrderService.sendOrderStatusUpdateCommandToB2B(message);
            } else if (message.getDataSource() == B2BDataSourceEnum.WEBER.id) {
                response = weberOrderService.sendOrderStatusUpdateCommandToB2B(message);
            } else if (message.getDataSource() == B2BDataSourceEnum.MBO.id) {
                response = mboOrderService.sendOrderStatusUpdateCommandToB2B(message);
            } else if (message.getDataSource() == B2BDataSourceEnum.SUPOR.id) {
                response = suporOrderService.sendOrderStatusUpdateCommandToB2B(message);
            } else if (message.getDataSource() == B2BDataSourceEnum.JINJING.id) {
                response = jinJingOrderService.sendOrderStatusUpdateCommandToB2B(message);
            } else if (message.getDataSource() == B2BDataSourceEnum.USATON_GA.id) {
                response = usatonGaOrderService.sendOrderStatusUpdateCommandToB2B(message);
            } else if (message.getDataSource() == B2BDataSourceEnum.MQI.id) {
                response = mqiOrderService.sendOrderStatusUpdateCommandToB2B(message);
            } else if (message.getDataSource() == B2BDataSourceEnum.JINRAN.id) {
                response = jinRanOrderService.sendOrderStatusUpdateCommandToB2B(message);
            }  else if (message.getDataSource() == B2BDataSourceEnum.JD.id) {
                response = jdOrderService.sendOrderStatusUpdateCommandToB2B(message);
            } else if (message.getDataSource() == B2BDataSourceEnum.INSE.id) {
                response = inseOrderService.sendOrderStatusUpdateCommandToB2B(message);
            } else if (message.getDataSource() == B2BDataSourceEnum.KONKA.id) {
                response = konkaOrderService.sendOrderStatusUpdateCommandToB2B(message);
            } else if (message.getDataSource() == B2BDataSourceEnum.JOYOUNG.id) {
                response = joyoungOrderService.sendOrderStatusUpdateCommandToB2B(message);
            } else if (message.getDataSource() == B2BDataSourceEnum.SUNING.id) {
                response = suningOrderService.sendOrderStatusUpdateCommandToB2B(message);
            } else if (message.getDataSource() == B2BDataSourceEnum.JDUE.id) {
                response = jdueOrderService.sendOrderStatusUpdateCommandToB2B(message);
            } else if (message.getDataSource() == B2BDataSourceEnum.JDUEPLUS.id) {
                response = jduePlusOrderService.sendOrderStatusUpdateCommandToB2B(message);
            } else if (message.getDataSource() == B2BDataSourceEnum.XYINGYAN.id) {
                response = xyyPlusOrderService.sendOrderStatusUpdateCommandToB2B(message);
            } else if (message.getDataSource() == B2BDataSourceEnum.LB.id) {
                response = lbOrderService.sendOrderStatusUpdateCommandToB2B(message);
            } else if (message.getDataSource() == B2BDataSourceEnum.UM.id) {
                response = umOrderService.sendOrderStatusUpdateCommandToB2B(message);
            } else if (message.getDataSource() == B2BDataSourceEnum.PDD.id) {
                response = pddOrderService.sendOrderStatusUpdateCommandToB2B(message);
            } else if (message.getDataSource() == B2BDataSourceEnum.VIOMI.id) {
                response = vioMiOrderService.sendOrderStatusUpdateCommandToB2B(message);
            } else if (message.getDataSource() == B2BDataSourceEnum.SF.id) {
                response = sfOrderService.sendOrderStatusUpdateCommandToB2B(message);
            } else if (message.getDataSource() == B2BDataSourceEnum.PHILIPS.id) {
                response = philipsOrderService.sendOrderStatusUpdateCommandToB2B(message);
//                response = philipsNewOrderService.sendOrderStatusUpdateCommandToB2B(message);
            }
        }
        return response;
    }

    //endregion 向队列发送消息、处理消息

    //--------------------------------------------------------------------------------------------------------更新转单进度

    //region 更新B2B转单进度


    /**
     * 调用B2B微服务更新B2B工单转换进度 - 单个工单
     */
    public void updateB2BOrderConversionProgressNew(B2BDataSourceEnum dataSource, String b2bOrderNo, B2BProcessFlag processFlag,
                                                    Long orderId, String orderNo, String b2bQuarter, String processComment, User user, Long b2bOrderId) {
        if (dataSource != null && StringUtils.isNotBlank(b2bOrderNo) && processFlag != null) {
            B2BOrderTransferResult progress = new B2BOrderTransferResult();
            progress.setId(b2bOrderId);
            progress.setB2bOrderId(b2bOrderId);
            progress.setDataSource(dataSource.id);
            progress.setOrderId(orderId);
            progress.setKklOrderNo(orderNo);
            progress.setB2bOrderNo(b2bOrderNo);
            progress.setB2bQuarter(b2bQuarter);
            progress.setProcessFlag(processFlag.value);
            progress.setProcessComment(StringUtils.left(processComment, 250));
            progress.setUpdater(user == null || StringUtils.isBlank(user.getName()) ? KKL_SYSTEM_USER.getName() : user.getName());
            progress.setUpdateDt((new Date()).getTime());
            sendB2BOrderConversionProgressUpdateCommandToB2B(dataSource, Lists.newArrayList(progress));
        }
    }

    /**
     * 调用B2B微服务更新B2B工单转换进度 - 多个工单
     */
    public MSResponse updateB2BOrderConversionProgress(B2BDataSourceEnum dataSource, List<B2BOrderTransferResult> progressList, User user) {
        MSResponse response = new MSResponse(MSErrorCode.SUCCESS);
        if (dataSource != null && progressList != null && !progressList.isEmpty()) {
            Long nowTimestamp = (new Date()).getTime();
            String userName = (user == null || StringUtils.isBlank(user.getName()) ? KKL_SYSTEM_USER.getName() : user.getName());
            for (B2BOrderTransferResult item : progressList) {
                item.setDataSource(dataSource.id);
                item.setProcessComment(StringUtils.left(item.getProcessComment(), 250));
                item.setUpdater(userName);
                item.setUpdateDt(nowTimestamp);
            }
            response = sendB2BOrderConversionProgressUpdateCommandToB2B(dataSource, progressList);
        }
        return response;
    }

    private MSResponse sendB2BOrderConversionProgressUpdateCommandToB2B(B2BDataSourceEnum dataSource, List<B2BOrderTransferResult> progressList) {
        MSResponse response = new MSResponse(MSErrorCode.SUCCESS);
        if (dataSource == B2BDataSourceEnum.TMALL) {
            response = tmallOrderService.sendB2BOrderConversionProgressUpdateCommandToB2B(progressList);
        } else if (B2BDataSourceEnum.isTooneDataSource(dataSource)) {
            response = canboOrderService.sendB2BOrderConversionProgressUpdateCommandToB2B(progressList);
        } else if (dataSource == B2BDataSourceEnum.WEBER) {
            response = weberOrderService.sendB2BOrderConversionProgressUpdateCommandToB2B(progressList);
        } else if (dataSource == B2BDataSourceEnum.MBO) {
            response = mboOrderService.sendB2BOrderConversionProgressUpdateCommandToB2B(progressList);
        } else if (dataSource == B2BDataSourceEnum.SUPOR) {
            response = suporOrderService.sendB2BOrderConversionProgressUpdateCommandToB2B(progressList);
        } else if (dataSource == B2BDataSourceEnum.JINJING) {
            response = jinJingOrderService.sendB2BOrderConversionProgressUpdateCommandToB2B(progressList);
        } else if (dataSource == B2BDataSourceEnum.USATON_GA) {
            response = usatonGaOrderService.sendB2BOrderConversionProgressUpdateCommandToB2B(progressList);
        } else if (dataSource == B2BDataSourceEnum.MQI) {
            response = mqiOrderService.sendB2BOrderConversionProgressUpdateCommandToB2B(progressList);
        } else if (dataSource == B2BDataSourceEnum.JINRAN) {
            response = jinRanOrderService.sendB2BOrderConversionProgressUpdateCommandToB2B(progressList);
        }  else if (dataSource == B2BDataSourceEnum.JD) {
            response = jdOrderService.sendB2BOrderConversionProgressUpdateCommandToB2B(progressList);
        } else if (dataSource == B2BDataSourceEnum.INSE) {
            response = inseOrderService.sendB2BOrderConversionProgressUpdateCommandToB2B(progressList);
        } else if (dataSource == B2BDataSourceEnum.KONKA) {
            response = konkaOrderService.sendB2BOrderConversionProgressUpdateCommandToB2B(progressList);
        } else if (dataSource == B2BDataSourceEnum.JOYOUNG) {
            response = joyoungOrderService.sendB2BOrderConversionProgressUpdateCommandToB2B(progressList);
        } else if (dataSource == B2BDataSourceEnum.SUNING) {
            response = suningOrderService.sendB2BOrderConversionProgressUpdateCommandToB2B(progressList);
        } else if (dataSource == B2BDataSourceEnum.JDUE) {
            response = jdueOrderService.sendB2BOrderConversionProgressUpdateCommandToB2B(progressList);
        } else if (dataSource == B2BDataSourceEnum.JDUEPLUS) {
            response = jduePlusOrderService.sendB2BOrderConversionProgressUpdateCommandToB2B(progressList);
        } else if (dataSource == B2BDataSourceEnum.XYINGYAN) {
            response = xyyPlusOrderService.sendB2BOrderConversionProgressUpdateCommandToB2B(progressList);
        } else if (dataSource == B2BDataSourceEnum.LB) {
            response = lbOrderService.sendB2BOrderConversionProgressUpdateCommandToB2B(progressList);
        } else if (dataSource == B2BDataSourceEnum.UM) {
            response = umOrderService.sendB2BOrderConversionProgressUpdateCommandToB2B(progressList);
        } else if (dataSource == B2BDataSourceEnum.PDD) {
            response = pddOrderService.sendB2BOrderConversionProgressUpdateCommandToB2B(progressList);
        } else if (dataSource == B2BDataSourceEnum.VIOMI) {
            response = vioMiOrderService.sendB2BOrderConversionProgressUpdateCommandToB2B(progressList);
        } else if (dataSource == B2BDataSourceEnum.SF) {
            response = sfOrderService.sendB2BOrderConversionProgressUpdateCommandToB2B(progressList);
        } else if (dataSource == B2BDataSourceEnum.VATTI) {
            response = vattiOrderService.sendB2BOrderConversionProgressUpdateCommandToB2B(progressList);
        } else if (dataSource == B2BDataSourceEnum.PHILIPS) {
            response = philipsOrderService.sendB2BOrderConversionProgressUpdateCommandToB2B(progressList);
//            response = philipsNewOrderService.sendB2BOrderConversionProgressUpdateCommandToB2B(progressList);
        }
        if (!MSResponse.isSuccessCode(response)) {
            String logJson = GsonUtils.toGsonString(progressList);
            LogUtils.saveLog("B2BCenterOrderService.sendB2BOrderConversionProgressUpdateCommandToB2B", "", logJson, null, null);
        }
        return response;
    }

    //endregion 更新B2B转单进度

    //-------------------------------------------------------------------------------------------------检查是否可以进行转单

    //region 检查是否可以进行转单

    public MSResponse checkB2BOrderProcessFlag(B2BDataSourceEnum dataSource, Long b2bOrderId, String b2bOrderNo) {
        B2BOrderTransferResult transferResult = new B2BOrderTransferResult(dataSource.id, b2bOrderNo);
        transferResult.setId(b2bOrderId);
        transferResult.setB2bOrderId(b2bOrderId);
        return checkB2BOrderProcessFlag(Lists.newArrayList(transferResult), dataSource);
    }

    /**
     * 检查工单是否允许进行转单操作
     */
    public MSResponse checkB2BOrderProcessFlag(List<B2BOrderTransferResult> b2bOrderNos, B2BDataSourceEnum dataSource) {
        MSResponse response = new MSResponse(MSErrorCode.SUCCESS);
        if (dataSource == B2BDataSourceEnum.TMALL) {
            response = tmallOrderService.checkB2BOrderProcessFlag(b2bOrderNos);
        } else if (B2BDataSourceEnum.isTooneDataSource(dataSource)) {
            response = canboOrderService.checkB2BOrderProcessFlag(b2bOrderNos);
        } else if (dataSource == B2BDataSourceEnum.WEBER) {
            response = weberOrderService.checkB2BOrderProcessFlag(b2bOrderNos);
        } else if (dataSource == B2BDataSourceEnum.MBO) {
            response = mboOrderService.checkB2BOrderProcessFlag(b2bOrderNos);
        } else if (dataSource == B2BDataSourceEnum.SUPOR) {
            response = suporOrderService.checkB2BOrderProcessFlag(b2bOrderNos);
        } else if (dataSource == B2BDataSourceEnum.JINJING) {
            response = jinJingOrderService.checkB2BOrderProcessFlag(b2bOrderNos);
        } else if (dataSource == B2BDataSourceEnum.USATON_GA) {
            response = usatonGaOrderService.checkB2BOrderProcessFlag(b2bOrderNos);
        } else if (dataSource == B2BDataSourceEnum.MQI) {
            response = mqiOrderService.checkB2BOrderProcessFlag(b2bOrderNos);
        } else if (dataSource == B2BDataSourceEnum.JINRAN) {
            response = jinRanOrderService.checkB2BOrderProcessFlag(b2bOrderNos);
        }  else if (dataSource == B2BDataSourceEnum.JD) {
            response = jdOrderService.checkB2BOrderProcessFlag(b2bOrderNos);
        } else if (dataSource == B2BDataSourceEnum.INSE) {
            response = inseOrderService.checkB2BOrderProcessFlag(b2bOrderNos);
        } else if (dataSource == B2BDataSourceEnum.KONKA) {
            response = konkaOrderService.checkB2BOrderProcessFlag(b2bOrderNos);
        } else if (dataSource == B2BDataSourceEnum.JOYOUNG) {
            response = joyoungOrderService.checkB2BOrderProcessFlag(b2bOrderNos);
        } else if (dataSource == B2BDataSourceEnum.SUNING) {
            response = suningOrderService.checkB2BOrderProcessFlag(b2bOrderNos);
        } else if (dataSource == B2BDataSourceEnum.JDUE) {
            response = jdueOrderService.checkB2BOrderProcessFlag(b2bOrderNos);
        } else if (dataSource == B2BDataSourceEnum.JDUEPLUS) {
            response = jduePlusOrderService.checkB2BOrderProcessFlag(b2bOrderNos);
        } else if (dataSource == B2BDataSourceEnum.XYINGYAN) {
            response = xyyPlusOrderService.checkB2BOrderProcessFlag(b2bOrderNos);
        } else if (dataSource == B2BDataSourceEnum.LB) {
            response = lbOrderService.checkB2BOrderProcessFlag(b2bOrderNos);
        } else if (dataSource == B2BDataSourceEnum.UM) {
            response = umOrderService.checkB2BOrderProcessFlag(b2bOrderNos);
        } else if (dataSource == B2BDataSourceEnum.PDD) {
            response = pddOrderService.checkB2BOrderProcessFlag(b2bOrderNos);
        } else if (dataSource == B2BDataSourceEnum.VIOMI) {
            response = vioMiOrderService.checkB2BOrderProcessFlag(b2bOrderNos);
        } else if (dataSource == B2BDataSourceEnum.SF) {
            response = sfOrderService.checkB2BOrderProcessFlag(b2bOrderNos);
        } else if (dataSource == B2BDataSourceEnum.VATTI) {
            response = vattiOrderService.checkB2BOrderProcessFlag(b2bOrderNos);
        } else if (dataSource == B2BDataSourceEnum.PHILIPS) {
            response = philipsOrderService.checkB2BOrderProcessFlag(b2bOrderNos);
//            response = philipsNewOrderService.checkB2BOrderProcessFlag(b2bOrderNos);
        }
        return response;
    }

    //endregion 检查是否可以进行转单

    //-----------------------------------------------------------------------------------------------------------取消转单

    //region 取消转单

    public MSResponse cancelOrderTransition(B2BOrderTransferResult b2BOrderTransferResult, B2BDataSourceEnum dataSource) {
        MSResponse response = new MSResponse(MSErrorCode.SUCCESS);
        if (dataSource == B2BDataSourceEnum.TMALL) {
            response = tmallOrderService.cancelOrderTransition(b2BOrderTransferResult);
        } else if (B2BDataSourceEnum.isTooneDataSource(dataSource)) {
            response = canboOrderService.cancelOrderTransition(b2BOrderTransferResult);
        } else if (dataSource == B2BDataSourceEnum.WEBER) {
            response = weberOrderService.cancelOrderTransition(b2BOrderTransferResult);
        } else if (dataSource == B2BDataSourceEnum.MBO) {
            response = mboOrderService.cancelOrderTransition(b2BOrderTransferResult);
        } else if (dataSource == B2BDataSourceEnum.SUPOR) {
            response = suporOrderService.cancelOrderTransition(b2BOrderTransferResult);
        } else if (dataSource == B2BDataSourceEnum.JINJING) {
            response = jinJingOrderService.cancelOrderTransition(b2BOrderTransferResult);
        } else if (dataSource == B2BDataSourceEnum.USATON_GA) {
            response = usatonGaOrderService.cancelOrderTransition(b2BOrderTransferResult);
        } else if (dataSource == B2BDataSourceEnum.MQI) {
            response = mqiOrderService.cancelOrderTransition(b2BOrderTransferResult);
        } else if (dataSource == B2BDataSourceEnum.JINRAN) {
            response = jinRanOrderService.cancelOrderTransition(b2BOrderTransferResult);
        }  else if (dataSource == B2BDataSourceEnum.JD) {
            response = jdOrderService.cancelOrderTransition(b2BOrderTransferResult);
        } else if (dataSource == B2BDataSourceEnum.INSE) {
            response = inseOrderService.cancelOrderTransition(b2BOrderTransferResult);
        } else if (dataSource == B2BDataSourceEnum.KONKA) {
            response = konkaOrderService.cancelOrderTransition(b2BOrderTransferResult);
        } else if (dataSource == B2BDataSourceEnum.JOYOUNG) {
            response = joyoungOrderService.cancelOrderTransition(b2BOrderTransferResult);
        } else if (dataSource == B2BDataSourceEnum.SUNING) {
            response = suningOrderService.cancelOrderTransition(b2BOrderTransferResult);
        } else if (dataSource == B2BDataSourceEnum.JDUE) {
            response = jdueOrderService.cancelOrderTransition(b2BOrderTransferResult);
        } else if (dataSource == B2BDataSourceEnum.JDUEPLUS) {
            response = jduePlusOrderService.cancelOrderTransition(b2BOrderTransferResult);
        } else if (dataSource == B2BDataSourceEnum.XYINGYAN) {
            response = xyyPlusOrderService.cancelOrderTransition(b2BOrderTransferResult);
        } else if (dataSource == B2BDataSourceEnum.LB) {
            response = lbOrderService.cancelOrderTransition(b2BOrderTransferResult);
        } else if (dataSource == B2BDataSourceEnum.UM) {
            response = umOrderService.cancelOrderTransition(b2BOrderTransferResult);
        } else if (dataSource == B2BDataSourceEnum.PDD) {
            response = pddOrderService.cancelOrderTransition(b2BOrderTransferResult);
        } else if (dataSource == B2BDataSourceEnum.VIOMI) {
            response = vioMiOrderService.cancelOrderTransition(b2BOrderTransferResult);
        } else if (dataSource == B2BDataSourceEnum.SF) {
            response = sfOrderService.cancelOrderTransition(b2BOrderTransferResult);
        } else if (dataSource == B2BDataSourceEnum.PHILIPS) {
            response = philipsOrderService.cancelOrderTransition(b2BOrderTransferResult);
//            response = philipsNewOrderService.cancelOrderTransition(b2BOrderTransferResult);
        }
        return response;
    }

    public MSResponse directlyCancelOrderTransition(B2BOrderTransferResult b2BOrderTransferResult, B2BDataSourceEnum dataSource) {
        MSResponse response = new MSResponse(MSErrorCode.SUCCESS);
        if (dataSource == B2BDataSourceEnum.TMALL) {
            response = tmallOrderService.directlyCancelOrderTransition(b2BOrderTransferResult);
        }
        return response;
    }

    /**
     * 忽略（直接关掉工单）
     */
    public MSResponse ignoreOrderTransition(B2BOrderTransferResult b2BOrderTransferResult, B2BDataSourceEnum dataSource) {
        MSResponse response = new MSResponse(MSErrorCode.SUCCESS);
        if (dataSource == B2BDataSourceEnum.TMALL) {
            response = tmallOrderService.ignoreOrderTransition(b2BOrderTransferResult);
        } else if (dataSource == B2BDataSourceEnum.JD) {
            response = jdOrderService.ignoreOrderTransition(b2BOrderTransferResult);
        }
        return response;
    }

    //endregion 取消转单

    //-----------------------------------------------------------------------------------------------------------更新B2B工单的异常标记

    //region 更新B2B工单的异常标记

    public MSResponse updateOrderAbnormalFlagBatch(B2BDataSourceEnum dataSource) {
        MSResponse response = new MSResponse(MSErrorCode.SUCCESS);
        if (dataSource == B2BDataSourceEnum.TMALL) {
            response = tmallOrderService.updateAbnormalOrderFlagAll();
        } else if (dataSource == B2BDataSourceEnum.PDD) {
            response = pddOrderService.updateInstallFlag();
        }
        return response;
    }

    //endregion 更新B2B工单的异常标记

    //region 更新B2B工单的路由标记

    public MSResponse updateOrderRoutingFlagBatch(B2BDataSourceEnum dataSource) {
        MSResponse response = new MSResponse(MSErrorCode.SUCCESS);
        if (dataSource == B2BDataSourceEnum.TMALL) {
            response = tmallOrderService.updateOrderRoutingFlagAll();
        } else if (dataSource == B2BDataSourceEnum.JD) {
            response = jdOrderService.updateOrderRoutingFlagAll();
        } else if (dataSource == B2BDataSourceEnum.JDUE) {
            response = jdueOrderService.updateOrderRoutingFlagAll();
        } else if (dataSource == B2BDataSourceEnum.JDUEPLUS) {
            response = jduePlusOrderService.updateOrderRoutingFlagAll();
        } else if (dataSource == B2BDataSourceEnum.SUNING) {
            response = suningOrderService.updateOrderRoutingFlagAll();
        } else if (dataSource == B2BDataSourceEnum.PDD) {
            response = pddOrderService.updateOrderRoutingFlagAll();
        } else if (dataSource == B2BDataSourceEnum.SF) {
            response = sfOrderService.updateOrderRoutingFlagAll();
        }
        return response;
    }

    //endregion 更新B2B工单的异常标记


    //-----------------------------------------------------------------------------------------------------------更新B2B微服务DB的工单状态
    //region 更新B2B微服务DB的工单状态

    private MSResponse updateOrderStatus(B2BDataSourceEnum dataSource, Long customerId, Long kklOrderId, B2BOrderStatusEnum status) {
        return updateOrderStatus(dataSource, customerId, kklOrderId, status, null);
    }

    private MSResponse updateOrderStatus(B2BDataSourceEnum dataSource, Long customerId, Long kklOrderId, B2BOrderStatusEnum status, Date closeDate) {
        MSResponse response = new MSResponse(MSErrorCode.SUCCESS);
        if (B2BMDUtils.isOrderStatusUpdateEnabled(dataSource, customerId) && kklOrderId != null && kklOrderId > 0 && status != null) {
            response = umOrderService.updateOrderStatus(kklOrderId, status, closeDate == null ? 0 : closeDate.getTime());
        }
        return response;
    }

    //endregion 更新B2B微服务DB的工单状态

    //region 检查产品条码

    public MSResponse checkProductSN(Integer dataSourceId, String b2bOrderNo, String productSn, User operator) {
        MSResponse response = new MSResponse(MSErrorCode.SUCCESS);
        if (B2BDataSourceEnum.isB2BDataSource(dataSourceId) && StringUtils.isNotBlank(b2bOrderNo) && StringUtils.isNotBlank(productSn)) {
            operator = (operator == null || operator.getId() == null ? B2BOrderVModel.b2bUser : operator);
            if (dataSourceId == B2BDataSourceEnum.VIOMI.id) {
                response = vioMiOrderService.checkProductSN(b2bOrderNo, productSn, operator);
            } else if (dataSourceId == B2BDataSourceEnum.JOYOUNG.id) {
                response = joyoungOrderService.checkProductSN(productSn);
            } else if (dataSourceId == B2BDataSourceEnum.MQI.id) {
                response = mqiOrderService.checkProductSN(b2bOrderNo, productSn);
            }
//            else if (dataSourceId == B2BDataSourceEnum.JINRAN.id) {
//                response = jinRanOrderService.checkProductSN(b2bOrderNo, productSn);
//            }
        }
        return response;
    }

    //endregion 检查产品条码

    //region 投诉

    ///**
    // * 完成关闭投诉单
    // */
    //public MSResponse completeComplainForm(Integer dataSourceId, Long complainId, String complainNo, String b2bComplainNo, User operator, String content, List<String> attachments) {
    //    MSResponse response = new MSResponse(MSErrorCode.SUCCESS);
    //    if (B2BDataSourceEnum.isB2BDataSource(dataSourceId) && complainId != null && complainId > 0
    //            && StringUtils.isNotBlank(complainNo) && StringUtils.isNotBlank(b2bComplainNo) && StringUtils.isNotBlank(content)) {
    //        operator = (operator == null || operator.getId() == null ? B2BOrderVModel.b2bUser : operator);
    //        if (dataSourceId == B2BDataSourceEnum.VIOMI.id) {
    //            response = vioMiOrderService.completeComplainForm(complainId, complainNo, b2bComplainNo, operator, content, null);
    //        }
    //    }
    //    return response;
    //}

    /**
     * 投诉单处理进度生产者
     */
    public MSResponse complainProcessProductor(MQB2BOrderComplainProcessMessage.B2BOrderComplainProcessMessage message) {
        MSResponse response = new MSResponse(MSErrorCode.SUCCESS);
        int dataSourceId = message.getDataSource();
        if (dataSourceId == B2BDataSourceEnum.VIOMI.id || dataSourceId == B2BDataSourceEnum.JOYOUNG.id) {
            complainProcessMQSender.sendRetry(message, 1);
        }
        return response;
    }

    /**
     * 处理投诉单进度
     */
    public MSResponse complainProcess(MQB2BOrderComplainProcessMessage.B2BOrderComplainProcessMessage message) {
        MSResponse response = new MSResponse(MSErrorCode.SUCCESS);
        int dataSourceId = message.getDataSource();
        if (B2BDataSourceEnum.isB2BDataSource(dataSourceId) && message.getKklComplainId() > 0
                && StringUtils.isNotBlank(message.getKklComplainNo()) && StringUtils.isNotBlank(message.getB2BComplainNo())
                && StringUtils.isNotBlank(message.getContent())) {
            if (dataSourceId == B2BDataSourceEnum.VIOMI.id) {
                response = vioMiOrderService.complainProcess(message);
            } else if (dataSourceId == B2BDataSourceEnum.JOYOUNG.id && StringUtils.isNotBlank(message.getB2BComplainNo())) {
                response = joyoungOrderService.complainProcess(message);
            }
        }
        return response;
    }

    //endregion 投诉

    //region 检查维修故障

    /**
     * 检查维修单的上门服务是否包含维修故障
     */
    public boolean checkRepairError(Integer dataSourceId, Integer orderServiceType, List<OrderDetail> orderDetails) {
        if (dataSourceId != B2BDataSourceEnum.VIOMI.id) {
            return true;
        }
        boolean result = false;
        if (ObjectUtil.isNotEmpty(orderDetails)) {
            result = true;
            for (OrderDetail detail : orderDetails) {
                if (orderServiceType == OrderUtils.OrderTypeEnum.REPAIRE.getId()
                        && (detail.getErrorType() == null || detail.getErrorType().getId() == null || detail.getErrorType().getId() == 0
                        || detail.getActionCode() == null || detail.getActionCode().getId() == null || detail.getActionCode().getId() == 0)) {
                    result = false;
                    break;
                }
            }
        }
        return result;
    }

    //endregion 检查维修故障

    //region 退换货

    public MSResponse processDismountAndReturnMessage(MQB2BOrderDismountReturnMessage.B2BOrderDismountReturnMessage message) {
        MSResponse response = new MSResponse<>(MSErrorCode.SUCCESS);
        if (B2BDataSourceEnum.isB2BDataSource(message.getDataSource())) {
            if (message.getDataSource() == B2BDataSourceEnum.VIOMI.id) {
                response = vioMiOrderService.processDismountAndReturnMessage(message);
            }
        }
        return response;
    }

    //endregion 退换货

    //region 获取退单验证码

    public MSResponse sendCancelVerifyCode(Integer dataSourceId, String b2bOrderNo, String phoneNumber, String remarks, User operator) {
        MSResponse response = new MSResponse(MSErrorCode.SUCCESS);
        if (B2BDataSourceEnum.isB2BDataSource(dataSourceId)) {
            response = vioMiOrderService.sendCancelVerifyCode(b2bOrderNo, phoneNumber, remarks, operator);
        }
        return response;
    }

    //endregion 获取退单验证码
}
