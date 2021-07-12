package com.wolfking.jeesite.ms.b2bcenter.sd.service;

import com.google.common.collect.Sets;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.entity.b2bcenter.pb.MQB2BOrderProcessLogMessage;
import com.wolfking.jeesite.common.utils.GsonUtils;
import com.wolfking.jeesite.modules.sd.entity.OrderProcessLog;
import com.wolfking.jeesite.modules.sd.entity.TwoTuple;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.entity.VisibilityFlagEnum;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.ms.b2bcenter.mq.sender.B2BPushOrderProcessLogToMSMQSender;
import com.wolfking.jeesite.ms.b2bcenter.sd.dao.B2BOrderDao;
import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderProcessLogReqEntity;
import com.wolfking.jeesite.ms.b2bcenter.sd.utils.B2BOrderUtils;
import com.wolfking.jeesite.ms.canbo.sd.service.CanboOrderService;
import com.wolfking.jeesite.ms.common.config.MicroServicesProperties;
import com.wolfking.jeesite.ms.inse.sd.service.InseOrderService;
import com.wolfking.jeesite.ms.jdueplus.sd.service.JDUEPlusOrderService;
import com.wolfking.jeesite.ms.jinjing.service.JinJingOrderService;
import com.wolfking.jeesite.ms.joyoung.sd.service.JoyoungOrderService;
import com.wolfking.jeesite.ms.mbo.service.MBOOrderService;
import com.wolfking.jeesite.ms.um.sd.service.UMOrderService;
import com.wolfking.jeesite.ms.usatonga.service.UsatonGaOrderService;
import com.wolfking.jeesite.ms.viomi.sd.service.VioMiOrderService;
import com.wolfking.jeesite.ms.weber.service.WeberOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Slf4j
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Service
public class B2BCenterOrderProcessLogService {

    @Resource
    B2BOrderDao b2BOrderDao;

    @Autowired
    private B2BPushOrderProcessLogToMSMQSender b2BPushOrderProcessLogToMSMQSender;

    @Autowired
    private JoyoungOrderService joyoungOrderService;

    public static final User KKL_B2B_SYSTEM_USER = new User(3L, "B2B系统用户", "4006663653");
    @Autowired
    private UMOrderService umOrderService;
    @Autowired
    private InseOrderService inseOrderService;
    @Autowired
    private VioMiOrderService vioMiOrderService;
    @Autowired
    private CanboOrderService canboOrderService;
    @Autowired
    private WeberOrderService weberOrderService;
    @Autowired
    private MBOOrderService mboOrderService;
    @Autowired
    private JinJingOrderService jinJingOrderService;
    @Autowired
    private UsatonGaOrderService usatonGaOrderService;
    @Autowired
    private JDUEPlusOrderService jduePlusOrderService;

    @Autowired
    private MicroServicesProperties microServicesProperties;


    //-----------------------------------------------------------------------------------------------------------公用方法
//    private boolean isNeedSendOrderLogToUM(Integer dataSourceId, Long customerId) {
//        B2BDataSourceEnum dataSource = B2BDataSourceEnum.valueOf(dataSourceId);
//        List<Long> customerIds = microServicesProperties.getUm().getCustomerIds();
//        if (microServicesProperties.getUm().getEnabled()
//                && microServicesProperties.getUm().getLogEnabled()
//                && dataSource != B2BDataSourceEnum.KKL
//                && customerIds != null && !customerIds.isEmpty()
//                && customerId != null && customerId > 0) {
//            return customerIds.contains(customerId);
//        }
//        return false;
//    }

    private boolean isNeedSendOrderLogToUM(Long customerId) {
        List<Long> customerIds = microServicesProperties.getUm().getCustomerIds();
        if (microServicesProperties.getUm().getEnabled()
                && microServicesProperties.getUm().getLogEnabled()
                && customerIds != null && !customerIds.isEmpty()
                && customerId != null && customerId > 0) {
            return customerIds.contains(customerId);
        }
        return false;
    }

    //region 公共方法

    /**
     * 向微服务推送工单日志
     */
    @Transactional()
    public void pushOrderProcessLogToMS(OrderProcessLog orderProcessLog) {
        if (orderProcessLog != null && orderProcessLog.getId() != null && orderProcessLog.getOrderId() != null
                && VisibilityFlagEnum.has(orderProcessLog.getVisibilityFlag(), Sets.newHashSet(VisibilityFlagEnum.CUSTOMER))) {
            Integer dataSourceId = orderProcessLog.getDataSourceId();
            if (dataSourceId == null || dataSourceId == 0) {
                //有部分写工单日志的方法不能直接获取数据源ID，故需要查询一次数据库
                dataSourceId = b2BOrderDao.getDataSourceIdByOrderId(orderProcessLog.getOrderId(), orderProcessLog.getQuarter());
            }
            Long customerId = orderProcessLog.getCustomerId();
            if (customerId == null || customerId == 0) {
                //有部分写工单日志的方法不能直接获取客户ID，故需要查询一次数据库
                customerId = b2BOrderDao.getCustomerIdByOrderId(orderProcessLog.getOrderId(), orderProcessLog.getQuarter());
            }
            TwoTuple<Boolean, B2BOrderProcessLogReqEntity.Builder> result;
            if (B2BDataSourceEnum.isB2BDataSource(dataSourceId) && B2BOrderUtils.canSendOrderProcessLog(dataSourceId)) {
                if (dataSourceId == B2BDataSourceEnum.UM.id) {
                    result = umOrderService.createOrderProcessLogReqEntity(orderProcessLog);
                } else if (dataSourceId == B2BDataSourceEnum.JOYOUNG.id) {
                    result = joyoungOrderService.createOrderProcessLogReqEntity(orderProcessLog);
                } else if (dataSourceId == B2BDataSourceEnum.INSE.id) {
                    result = inseOrderService.createOrderProcessLogReqEntity(orderProcessLog);
                } else if (dataSourceId == B2BDataSourceEnum.VIOMI.id) {
                    result = vioMiOrderService.createOrderProcessLogReqEntity(orderProcessLog);
                } else if (B2BDataSourceEnum.isTooneDataSourceId(dataSourceId)) {
                    result = canboOrderService.createOrderProcessLogReqEntity(orderProcessLog, dataSourceId);
                } else if (dataSourceId == B2BDataSourceEnum.WEBER.id) {
                    result = weberOrderService.createOrderProcessLogReqEntity(orderProcessLog, dataSourceId);
                } else if (dataSourceId == B2BDataSourceEnum.MBO.id) {
                    result = mboOrderService.createOrderProcessLogReqEntity(orderProcessLog, dataSourceId);
                }  else if (dataSourceId == B2BDataSourceEnum.JINJING.id) {
                    result = jinJingOrderService.createOrderProcessLogReqEntity(orderProcessLog, dataSourceId);
                }  else if (dataSourceId == B2BDataSourceEnum.USATON_GA.id) {
                    result = usatonGaOrderService.createOrderProcessLogReqEntity(orderProcessLog, dataSourceId);
                } else if (dataSourceId == B2BDataSourceEnum.JDUEPLUS.id) {
                    result = jduePlusOrderService.createOrderProcessLogReqEntity(orderProcessLog, dataSourceId);
                } else {
                    result = new TwoTuple<>(true, null);
                }
            } else if (isNeedSendOrderLogToUM(customerId)) {
                result = umOrderService.createOrderProcessLogReqEntity(orderProcessLog);
            } else {
                result = new TwoTuple<>(true, null);
            }
//            if (B2BDataSourceEnum.isB2BDataSource(dataSourceId) && dataSourceId == B2BDataSourceEnum.JOYOUNG.id && B2BOrderUtils.canSendOrderProcessLog(dataSourceId)) {
//                result = joyoungOrderService.createOrderProcessLogReqEntity(orderProcessLog);
//            } else if (B2BDataSourceEnum.isB2BDataSource(dataSourceId) && dataSourceId == B2BDataSourceEnum.INSE.id && B2BOrderUtils.canSendOrderProcessLog(dataSourceId)) {
//                result = inseOrderService.createOrderProcessLogReqEntity(orderProcessLog);
//            } else if (B2BDataSourceEnum.isB2BDataSource(dataSourceId) && dataSourceId == B2BDataSourceEnum.VIOMI.id && B2BOrderUtils.canSendOrderProcessLog(dataSourceId)) {
//                result = vioMiOrderService.createOrderProcessLogReqEntity(orderProcessLog);
//            } else if (isNeedSendOrderLogToUM(customerId)) {
//                result = umOrderService.createOrderProcessLogReqEntity(orderProcessLog);
//            } else {
//                result = new TwoTuple<>(true, null);
//            }
            if (result.getAElement() && result.getBElement() != null) {
                result.getBElement()
                        .setId(orderProcessLog.getId())
                        .setOrderId(orderProcessLog.getOrderId())
                        .setCustomerId(customerId == null ? 0 : customerId)
                        .setCreateById(KKL_B2B_SYSTEM_USER.getId())
                        .setCreateDt((new Date()).getTime());
                sendOrderProcessLogMessage(result.getBElement().build());
            } else {
                if (!result.getAElement()) {
                    try {
                        String logJson = GsonUtils.toGsonString(orderProcessLog);
                        LogUtils.saveLog("B2BCenterOrderProcessLogService", "pushOrderProcessLogToMS", logJson, null, null);
                    } catch (Exception e) {
                        log.error("B2BCenterOrderProcessLogService.pushOrderProcessLogToMS", e);
                    }
                }
            }
        }
    }

    //endregion 公共方法

    //------------------------------------------------------------------------------------------往队列发送消息、处理队列消息

    //region 向队列发送消息、处理消息

    /**
     * 发送工单日志消息
     */
    private void sendOrderProcessLogMessage(B2BOrderProcessLogReqEntity reqEntity) {
        MQB2BOrderProcessLogMessage.B2BOrderProcessLogMessage.Builder builder = MQB2BOrderProcessLogMessage.B2BOrderProcessLogMessage.newBuilder();
        builder.setId(reqEntity.getId())
                .setOrderId(reqEntity.getOrderId())
                .setDataSourceId(reqEntity.getDataSourceId())
                .setCustomerId(reqEntity.getCustomerId())
                .setOperatorName(reqEntity.getOperatorName())
                .setLogDt(reqEntity.getLogDt())
                .setLogType(reqEntity.getLogType())
                .setLogTitle(reqEntity.getLogTitle())
                .setLogContext(reqEntity.getLogContext())
                .setLogRemarks(reqEntity.getLogRemarks())
                .setCreateById(reqEntity.getCreateById())
                .setCreateDt(reqEntity.getCreateDt());
        b2BPushOrderProcessLogToMSMQSender.send(builder.build());
    }


    /**
     * 处理B2B工单的状态变更消息
     */
    public MSResponse processOrderProcessLogMessage(MQB2BOrderProcessLogMessage.B2BOrderProcessLogMessage message) {
        MSResponse response = new MSResponse<>(MSErrorCode.SUCCESS);
        if (message.getDataSourceId() == B2BDataSourceEnum.UM.id) {
            response = umOrderService.pushOrderProcessLogToMS(message);
        } else if (message.getDataSourceId() == B2BDataSourceEnum.JOYOUNG.id) {
            response = joyoungOrderService.pushOrderProcessLogToMS(message);
        } else if (message.getDataSourceId() == B2BDataSourceEnum.INSE.id) {
            response = inseOrderService.pushOrderProcessLogToMS(message);
        } else if (message.getDataSourceId() == B2BDataSourceEnum.VIOMI.id) {
            response = vioMiOrderService.pushOrderProcessLogToMS(message);
        } else if (B2BDataSourceEnum.isTooneDataSourceId(message.getDataSourceId())) {
            response = canboOrderService.pushOrderProcessLogToMS(message);
        } else if (message.getDataSourceId() == B2BDataSourceEnum.WEBER.id) {
            response = weberOrderService.pushOrderProcessLogToMS(message);
        } else if (message.getDataSourceId() == B2BDataSourceEnum.MBO.id) {
            response = mboOrderService.pushOrderProcessLogToMS(message);
        } else if (message.getDataSourceId() == B2BDataSourceEnum.JINJING.id) {
            response = jinJingOrderService.pushOrderProcessLogToMS(message);
        } else if (message.getDataSourceId() == B2BDataSourceEnum.USATON_GA.id) {
            response = usatonGaOrderService.pushOrderProcessLogToMS(message);
        } else if (message.getDataSourceId() == B2BDataSourceEnum.JDUEPLUS.id) {
            response = jduePlusOrderService.pushOrderProcessLogToMS(message);
        }
        return response;
    }

    //endregion 向队列发送消息、处理消息

}
