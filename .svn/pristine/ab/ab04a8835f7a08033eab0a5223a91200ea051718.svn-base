package com.wolfking.jeesite.ms.suning.sd.service;

import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.md.B2BModifyOperationEnum;
import com.kkl.kklplus.entity.b2bcenter.pb.MQB2BOrderModifyMessage;
import com.kkl.kklplus.entity.b2bcenter.pb.MQB2BOrderStatusUpdateMessage;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderStatusEnum;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderTransferResult;
import com.kkl.kklplus.entity.canbo.sd.CanboOrderCompleted;
import com.kkl.kklplus.entity.suning.sd.B2BSuningOrderStatusEnum;
import com.kkl.kklplus.entity.suning.sd.SuningOrderModify;
import com.kkl.kklplus.entity.suning.sd.SuningOrderModifySrvtime;
import com.kkl.kklplus.entity.suning.sd.SuningOrderWorkStatus;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.md.service.ServicePointService;
import com.wolfking.jeesite.modules.sd.entity.OrderItemComplete;
import com.wolfking.jeesite.modules.sd.entity.TwoTuple;
import com.wolfking.jeesite.modules.sd.service.OrderItemCompleteService;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderModifyEntity;
import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderStatusUpdateReqEntity;
import com.wolfking.jeesite.ms.b2bcenter.sd.service.B2BOrderManualBaseService;
import com.wolfking.jeesite.ms.suning.sd.feign.SuningOrderFeign;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class SuningOrderService extends B2BOrderManualBaseService {

    private static final User KKL_SUNING_B2B_USER = new User(0L, "快可立全国联保", "075729235666");

    @Autowired
    private SuningOrderFeign suningOrderFeign;
    @Autowired
    private ServicePointService servicePointService;
    @Autowired
    private OrderItemCompleteService orderItemCompleteService;

    /**
     * 批量检查工单是否可转换
     */
    public MSResponse checkB2BOrderProcessFlag(List<B2BOrderTransferResult> b2bOrderNos) {
        return suningOrderFeign.checkWorkcardProcessFlag(b2bOrderNos);
    }


    /**
     * 取消工单转换
     */
    public MSResponse cancelOrderTransition(B2BOrderTransferResult b2BOrderTransferResult) {
        return suningOrderFeign.cancelOrderTransition(b2BOrderTransferResult);
    }

    /**
     * 批量更新工单的路由标记
     */
    public MSResponse updateOrderRoutingFlagAll() {
        return suningOrderFeign.updateSystemIdAll();
    }

    /**
     * 调用B2B工单转换进度更新接口
     */
    public MSResponse sendB2BOrderConversionProgressUpdateCommandToB2B(List<B2BOrderTransferResult> progressList) {
        return suningOrderFeign.updateOrderTransferResult(Lists.newArrayList(progressList));
    }

    /**
     * 修改B2B工单基本信息
     */
    public MSResponse modifyB2BOrder(MQB2BOrderModifyMessage.B2BOrderModifyMessage message) {
        SuningOrderModify orderModify = new SuningOrderModify();
        orderModify.setAsomOrderItemId(message.getB2BOrderNo());
        orderModify.setProvince(message.getUserProvince());
        orderModify.setCityDesc(message.getUserCity());
        orderModify.setTzoneDesc(message.getUserCounty());
        orderModify.setStreet(message.getUserStreet());
        orderModify.setCustomerName(message.getUserName());
        orderModify.setMobPhoneNum(message.getUserMobile());
        orderModify.setPhoneNum(message.getUserPhone());
        orderModify.setServiceMemo(message.getRemarks());
        return suningOrderFeign.orderModify(orderModify);
    }

    /**
     * 修改快可立工单
     */
    public MSResponse modifyKKLOrderBySuning(B2BOrderModifyEntity modifyEntity) {
        boolean flag = false;
        try {
            if (modifyEntity.getOperationType() == B2BModifyOperationEnum.CANCEL) {
                flag = cancelKKLOrder(modifyEntity);
            } else {
                flag = modifyKKLOrder(modifyEntity);
            }
        } catch (Exception e) {
            B2BOrderModifyEntity.saveFailureLog(modifyEntity, "修改快可立工单", "SuningOrderService.ModifyKKLOrderBySuning", e);
        }
        return new MSResponse(flag ? MSErrorCode.SUCCESS : MSErrorCode.FAILURE);
    }

    /**
     * 预约
     */
    private MSResponse suningOrderAppoint(Long b2bOrderId, String b2bOrderNo, Long appointmentDate,
                                          String remarks, long createById, long createDt) {
        SuningOrderModifySrvtime appointed = new SuningOrderModifySrvtime();
        appointed.setB2bOrderId(b2bOrderId);
        appointed.setAsomOrderItemId(b2bOrderNo);
        appointed.setPreServiceTime(appointmentDate);
        appointed.setReason(remarks);
        appointed.setCreateDt(createDt);
        appointed.setCreateById(createById);
        return suningOrderFeign.moditySrvtime(appointed);
    }

    /**
     * 派单
     */
    private MSResponse suningOrderPlan(Long b2bOrderId, String b2bOrderNo, String engineerId, String engineerName, String engineerMobile,
                                       long createById, long createDt) {
        SuningOrderWorkStatus planned = new SuningOrderWorkStatus();
        planned.setB2bOrderId(b2bOrderId);
        planned.setAsomOrderItemId(b2bOrderNo);
        planned.setServiceStatus(B2BSuningOrderStatusEnum.PLANNED.value);
        planned.setBp1Num(engineerId);
        planned.setBp1Name(engineerName);
        planned.setBp1Phone(engineerMobile);
        planned.setCreateDt(createDt);
        planned.setCreateById(createById);
        return suningOrderFeign.workStatus(planned);
    }


    /**
     * 完成
     */
    private MSResponse suningOrderComplete(Long b2bOrderId, String b2bOrderNo, Long completeDt, String intMachineNumber, String extMachineNumber,
                                           long createById, long createDt) {
        SuningOrderWorkStatus completed = new SuningOrderWorkStatus();
        completed.setB2bOrderId(b2bOrderId);
        completed.setAsomOrderItemId(b2bOrderNo);
        completed.setServiceStatus(B2BSuningOrderStatusEnum.COMPLETED.value);
        completed.setCompleteTime(completeDt);
        completed.setIntMachineNumber(intMachineNumber);
        completed.setExtMachineNumber(extMachineNumber);
        completed.setCreateDt(createDt);
        completed.setCreateById(createById);
        return suningOrderFeign.workStatus(completed);
    }

    /**
     * 取消
     */
    private MSResponse suningOrderCancel(Long b2bOrderId, String b2bOrderNo, String remarks,
                                         long createById, long createDt) {
        SuningOrderWorkStatus cancelled = new SuningOrderWorkStatus();
        cancelled.setB2bOrderId(b2bOrderId);
        cancelled.setAsomOrderItemId(b2bOrderNo);
        cancelled.setServiceStatus(B2BSuningOrderStatusEnum.CANCELED.value);
        cancelled.setReason(remarks);
        cancelled.setCreateDt(createDt);
        cancelled.setCreateById(createById);
        return suningOrderFeign.workStatus(cancelled);
    }

    /**
     * 往苏宁微服务发送工单状态更新命令
     */
    public MSResponse sendOrderStatusUpdateCommandToB2B(MQB2BOrderStatusUpdateMessage.B2BOrderStatusUpdateMessage message) {
        MSResponse response = null;
        if (message.getStatus() == B2BOrderStatusEnum.PLANNED.value) {
            response = suningOrderPlan(message.getB2BOrderId(), message.getB2BOrderNo(), message.getEngineerId(), message.getEngineerName(),
                    message.getEngineerMobile(), message.getUpdaterId(), message.getUpdateDt());
        } else if (message.getStatus() == B2BOrderStatusEnum.APPOINTED.value) {
            response = suningOrderAppoint(message.getB2BOrderId(), message.getB2BOrderNo(), message.getEffectiveDt(), message.getRemarks(), message.getUpdaterId(), message.getUpdateDt());
        } else if (message.getStatus() == B2BOrderStatusEnum.COMPLETED.value) {
            String barCode = "";
            String outBarCode = "";
            if (message.getCompletedItemList() != null && !message.getCompletedItemList().isEmpty()) {
                barCode = message.getCompletedItemList().get(0).getBarcode();
                outBarCode = message.getCompletedItemList().get(0).getOutBarcode();
            }
            response = suningOrderComplete(message.getB2BOrderId(), message.getB2BOrderNo(), message.getEffectiveDt(), barCode, outBarCode, message.getUpdaterId(), message.getUpdateDt());
        } else if (message.getStatus() == B2BOrderStatusEnum.CANCELED.value) {
            response = suningOrderCancel(message.getB2BOrderId(), message.getB2BOrderNo(), message.getRemarks(), message.getUpdaterId(), message.getUpdateDt());
        } else {
            response = new MSResponse(MSErrorCode.SUCCESS);
            String msgJson = new JsonFormat().printToString(message);
            LogUtils.saveLog("B2B工单状态变更消息的状态错误", "SuningOrderService.sendOrderStatusUpdateCommandToB2B", msgJson, null, null);
        }
        return response;
    }


    //-------------------------------------------------------------------------------------------------创建状态变更请求实体

    /**
     * 创建苏宁派单请求对象
     */
    public TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> createSuningPlanRequestEntity(Long engineerId, String engineerName, String engineerMobile) {
        TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> result = new TwoTuple<>(false, null);
        if (engineerId != null && StringUtils.isNotBlank(engineerName) && StringUtils.isNotBlank(engineerMobile)) {
            B2BOrderStatusUpdateReqEntity.Builder builder = new B2BOrderStatusUpdateReqEntity.Builder();
            builder.setEngineerName(engineerName)
                    .setEngineerMobile(engineerMobile)
                    .setEngineerId(engineerId.toString());
            result.setAElement(true);
            result.setBElement(builder);
        }
        return result;
    }

    /**
     * 创建苏宁预约请求对象
     */
    public TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> createSuningAppointRequestEntity(Date appointmentDate, String remarks) {
        TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> result = new TwoTuple<>(false, null);
        if (appointmentDate != null) {
            B2BOrderStatusUpdateReqEntity.Builder builder = new B2BOrderStatusUpdateReqEntity.Builder();
            builder.setEffectiveDate(appointmentDate)
                    .setRemarks(StringUtils.toString(remarks));
            result.setAElement(true);
            result.setBElement(builder);
        }
        return result;
    }

    /**
     * 创建苏宁完工请求对象
     */
    @Transactional()
    public TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> createCompleteRequestEntity(Long orderId, String quarter, Date completedDate) {
        TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> result = new TwoTuple<>(false, null);
        if (orderId != null && orderId > 0 && StringUtils.isNotBlank(quarter)) {
            B2BOrderStatusUpdateReqEntity.Builder builder = new B2BOrderStatusUpdateReqEntity.Builder();
            builder.setEffectiveDate(completedDate);
            List<OrderItemComplete> completeList = orderItemCompleteService.getByOrderId(orderId, quarter);
            if (completeList != null && !completeList.isEmpty()) {
                CanboOrderCompleted.CompletedItem completedItem = new CanboOrderCompleted.CompletedItem();
                completedItem.setBarcode(StringUtils.toString(completeList.get(0).getUnitBarcode()));
                completedItem.setOutBarcode(StringUtils.toString(completeList.get(0).getOutBarcode()));
                builder.setCompletedItems(Lists.newArrayList(completedItem));
            }
            result.setAElement(true);
            result.setBElement(builder);
        }
        return result;
    }

    /**
     * 创建苏宁取消请求对象
     */
    public TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> createSuningCancelRequestEntity(Integer kklCancelType) {
        TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> result = new TwoTuple<>(false, null);
        if (kklCancelType != null) {
            Dict cancelResponsibleDict = MSDictUtils.getDictByValue(kklCancelType.toString(), Dict.DICT_TYPE_CANCEL_RESPONSIBLE);
            if (cancelResponsibleDict != null && StringUtils.isNotBlank(cancelResponsibleDict.getLabel())) {
                B2BOrderStatusUpdateReqEntity.Builder builder = new B2BOrderStatusUpdateReqEntity.Builder();
                builder.setRemarks(cancelResponsibleDict.getLabel());
                result.setAElement(true);
                result.setBElement(builder);
            }
        }
        return result;
    }
}
