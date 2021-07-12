package com.wolfking.jeesite.ms.viomi.sd.service;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.entity.b2bcenter.pb.MQB2BOrderComplainProcessMessage;
import com.kkl.kklplus.entity.b2bcenter.pb.MQB2BOrderDismountReturnMessage;
import com.kkl.kklplus.entity.b2bcenter.pb.MQB2BOrderProcessLogMessage;
import com.kkl.kklplus.entity.b2bcenter.pb.MQB2BOrderStatusUpdateMessage;
import com.kkl.kklplus.entity.b2bcenter.sd.*;
import com.kkl.kklplus.entity.common.NameValuePair;
import com.kkl.kklplus.entity.md.MDActionCode;
import com.kkl.kklplus.entity.md.MDErrorCode;
import com.kkl.kklplus.entity.md.MDErrorType;
import com.kkl.kklplus.entity.praise.Praise;
import com.kkl.kklplus.entity.praise.PraisePicItem;
import com.kkl.kklplus.entity.validate.OrderValidate;
import com.kkl.kklplus.entity.validate.ValidatePicItem;
import com.kkl.kklplus.entity.viomi.sd.*;
import com.kkl.kklplus.entity.xyyplus.sd.XYYOrderCancelAudit;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.utils.BitUtils;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.GsonUtils;
import com.wolfking.jeesite.modules.md.entity.*;
import com.wolfking.jeesite.modules.sd.entity.*;
import com.wolfking.jeesite.modules.sd.service.OrderCacheReadService;
import com.wolfking.jeesite.modules.sd.service.OrderItemCompleteService;
import com.wolfking.jeesite.modules.sd.service.OrderLocationService;
import com.wolfking.jeesite.modules.sd.service.OrderMaterialService;
import com.wolfking.jeesite.modules.sd.utils.OrderAdditionalInfoUtils;
import com.wolfking.jeesite.modules.sd.utils.OrderPicUtils;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.entity.VisibilityFlagEnum;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.ms.b2bcenter.md.utils.B2BMDUtils;
import com.wolfking.jeesite.ms.b2bcenter.mq.config.B2BCenterOrderDismountReturnConfig;
import com.wolfking.jeesite.ms.b2bcenter.mq.sender.B2BCenterOrderDismountReturnMQSender;
import com.wolfking.jeesite.ms.b2bcenter.sd.dao.B2BOrderDao;
import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderProcessEntity;
import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderProcessLogReqEntity;
import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderStatusUpdateReqEntity;
import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderVModel;
import com.wolfking.jeesite.ms.b2bcenter.sd.service.B2BOrderManualBaseService;
import com.wolfking.jeesite.ms.material.mq.entity.mapper.B2BMaterialMapper;
import com.wolfking.jeesite.ms.praise.service.OrderPraiseService;
import com.wolfking.jeesite.ms.providermd.service.*;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import com.wolfking.jeesite.ms.utils.MSUserUtils;
import com.wolfking.jeesite.ms.validate.service.MSOrderValidateService;
import com.wolfking.jeesite.ms.viomi.sd.feign.VioMiOrderFeign;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.wolfking.jeesite.modules.sys.entity.VisibilityFlagEnum.*;

@Configurable
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Slf4j
public class VioMiOrderService extends B2BOrderManualBaseService {

    private static final User KKL_VIOMI_B2B_USER = new User(0L, "快可立全国联保", "075729235666");
    private static final TwoTuple<Double, Double> DEFAULT_LOCATION = new TwoTuple<>(113.27661753, 22.75775595);
    private static final String VIOMI_VERIFY_CODE = "200618";
    private static B2BMaterialMapper mapper = Mappers.getMapper(B2BMaterialMapper.class);

    //id generator
    private static com.kkl.kklplus.utils.SequenceIdUtils sequenceIdUtils = new com.kkl.kklplus.utils.SequenceIdUtils(30,10);


    @Value("${userfiles.host}")
    private String imgShowUrl;

    @Resource
    private B2BOrderDao b2BOrderDao;

    @Autowired
    private VioMiOrderFeign vioMiOrderFeign;
    @Autowired
    private OrderCacheReadService orderCacheReadService;
    @Autowired
    private OrderLocationService orderLocationService;
    @Autowired
    private OrderItemCompleteService orderItemCompleteService;
    @Autowired
    private MSCustomerMaterialService msCustomerMaterialService;
    @Autowired
    private MSCustomerErrorTypeService msCustomerErrorTypeService;
    @Autowired
    private MSCustomerErrorCodeService msCustomerErrorCodeService;
    @Autowired
    private MSCustomerErrorActionService msCustomerErrorActionService;
    @Autowired
    private OrderPraiseService orderPraiseService;
    @Autowired
    private MSOrderValidateService msOrderValidateService;
    @Autowired
    private MSServicePointService msServicePointService;
    @Autowired
    private OrderMaterialService orderMaterialService;

    @Autowired
    private B2BCenterOrderDismountReturnMQSender orderDismountReturnMQSender;


    //region 工单转换

    /**
     * 取消工单转换
     */
    public MSResponse cancelOrderTransition(B2BOrderTransferResult b2BOrderTransferResult) {
        return vioMiOrderFeign.cancelOrderTransition(b2BOrderTransferResult);
    }

    /**
     * 批量检查工单是否可转换
     */
    public MSResponse checkB2BOrderProcessFlag(List<B2BOrderTransferResult> b2bOrderNos) {
        return vioMiOrderFeign.checkWorkcardProcessFlag(b2bOrderNos);
    }

    /**
     * 调用微服务的B2B工单转换进度更新接口
     */
    public MSResponse sendB2BOrderConversionProgressUpdateCommandToB2B(List<B2BOrderTransferResult> progressList) {
        return vioMiOrderFeign.updateTransferResult(Lists.newArrayList(progressList));
    }

    //endregion 工单转换

    //region 工单处理

    /**
     * 派单
     */
    private MSResponse<Integer> orderPlan(MQB2BOrderStatusUpdateMessage.B2BOrderStatusUpdateMessage message) {
        VioMiOrderHandle params = new VioMiOrderHandle();
        params.setUniqueId(message.getMessageId());
        params.setB2bOrderId(message.getB2BOrderId());
        params.setOrderNumber(message.getB2BOrderNo());
        params.setEngineerName(message.getEngineerName());
        params.setEngineerPhone(message.getEngineerMobile());
        params.setStatus(B2BOrderActionEnum.PLAN.value);
        params.setOperator(message.getUpdaterName());
        params.setCreateById(message.getUpdaterId());
        params.setCreateDt(message.getUpdateDt());
        return vioMiOrderFeign.planing(params);
    }

    /**
     * 预约
     */
    private MSResponse<Integer> orderAppoint(MQB2BOrderStatusUpdateMessage.B2BOrderStatusUpdateMessage message) {
        VioMiOrderHandle params = new VioMiOrderHandle();
        params.setUniqueId(message.getMessageId());
        params.setB2bOrderId(message.getB2BOrderId());
        params.setOrderNumber(message.getB2BOrderNo());
        params.setTimeOfAppointment(message.getEffectiveDt());
        params.setRemarks(message.getRemarks());
        params.setStatus(B2BOrderActionEnum.APPOINT.value);
        params.setOperator(message.getUpdaterName());
        params.setCreateById(message.getUpdaterId());
        params.setCreateDt(message.getUpdateDt());
        return vioMiOrderFeign.appointment(params);
    }

    /**
     * 打卡
     */
    private MSResponse<Integer> orderClockInHome(MQB2BOrderStatusUpdateMessage.B2BOrderStatusUpdateMessage message) {
        VioMiOrderHandle params = new VioMiOrderHandle();
        params.setUniqueId(message.getMessageId());
        params.setB2bOrderId(message.getB2BOrderId());
        params.setOrderNumber(message.getB2BOrderNo());
        params.setLocation(String.format("%.5f,%.5f", message.getLongitude(), message.getLatitude()));
        params.setOperator(message.getUpdaterName());
        params.setCreateById(message.getUpdaterId());
        params.setCreateDt(message.getUpdateDt());
        params.setStatus(B2BOrderActionEnum.SERVICE.value);
        return vioMiOrderFeign.clockInHome(params);
    }

    private MSResponse<Integer> processComplete(MQB2BOrderStatusUpdateMessage.B2BOrderStatusUpdateMessage message) {
        VioMiOrderHandle params = new VioMiOrderHandle();
        params.setUniqueId(message.getMessageId());
        params.setB2bOrderId(message.getB2BOrderId());
        params.setOrderNumber(message.getB2BOrderNo());

        if (message.getCompletedItemCount() > 0) {
            MQB2BOrderStatusUpdateMessage.CompletedItem firstItem = message.getCompletedItem(0);
            params.setMiSn(firstItem.getBarcode());
            params.setBuyDate(firstItem.getBuyDt());
            List<String> picUrls = firstItem.getPicItemList().stream().map(MQB2BOrderStatusUpdateMessage.PicItem::getUrl).collect(Collectors.toList());
            params.setAttachment(picUrls);
            //维修故障
            if (firstItem.getErrorItemCount() > 0) {
                MQB2BOrderStatusUpdateMessage.ErrorItem errorItem = firstItem.getErrorItem(0);
                List<String> faults = Lists.newArrayList(errorItem.getErrorType(), errorItem.getErrorCode(), errorItem.getErrorAnalysis());
                String faultType = faults.stream().filter(StringUtils::isNotBlank).collect(Collectors.joining("/"));
                params.setFaultType(faultType);
                params.setServiceMeasures(errorItem.getErrorAction());
            }
            //配件
            if (firstItem.getMaterialCount() > 0) {
                List<VioMiParts> parts = Lists.newArrayList();
                VioMiParts part;
                for (MQB2BOrderStatusUpdateMessage.Material item : firstItem.getMaterialList()) {
                    part = new VioMiParts();
                    part.setYunmiCode(item.getMaterialCode());
                    part.setCount(item.getQty());
                    parts.add(part);
                }
                params.setParts(parts);
            }
        }
        if (message.getPraiseItem() != null && message.getPraiseItem().getPicUrlCount() > 0) {
            params.setPraiseScreenshot(message.getPraiseItem().getPicUrlList());
            params.setPraiseSuccess("是");
        } else {
            params.setPraiseSuccess("否");
        }

        params.setOperator(message.getUpdaterName());
        params.setCreateById(message.getUpdaterId());
        params.setCreateDt(message.getUpdateDt());
        params.setStatus(B2BOrderActionEnum.APP_COMPLETE.value);
        return vioMiOrderFeign.processComplete(params);
    }

    private MSResponse<Integer> orderValidate(MQB2BOrderStatusUpdateMessage.B2BOrderStatusUpdateMessage message) {
        VioMiOrderHandle params = new VioMiOrderHandle();
        params.setUniqueId(message.getMessageId());
        params.setB2bOrderId(message.getB2BOrderId());
        params.setOrderNumber(message.getB2BOrderNo());

        if (message.getValidateItem() != null) {
            MQB2BOrderStatusUpdateMessage.ValidateItem validateItem = message.getValidateItem();
            params.setMiSn(validateItem.getProductSn());
            params.setBuyDate(validateItem.getBuyDt());
            params.setAttachment(validateItem.getPicUrlList());
            if (message.getValidateItem().getErrorItem() != null) {
                MQB2BOrderStatusUpdateMessage.ErrorItem errorItem = message.getValidateItem().getErrorItem();
                List<String> faults = Lists.newArrayList(errorItem.getErrorType(), errorItem.getErrorCode(), errorItem.getErrorAnalysis());
                String faultType = faults.stream().filter(StringUtils::isNotBlank).collect(Collectors.joining("/"));
                params.setFaultType(faultType);
                params.setServiceMeasures(errorItem.getErrorAction());
            }
            params.setIsFault(validateItem.getIsFault() == 1 ? "是" : "否");
            params.setWorkerErrorDesc(validateItem.getErrorDescription());
            String checkValidateResults = StringUtils.join(validateItem.getCheckValidateResultValuesList(), ",");
            params.setCheckValidateResult(checkValidateResults);
            params.setCheckValidateDetail(validateItem.getCheckValidateDetail());
            String packValidateResults = StringUtils.join(validateItem.getPackValidateResultValuesList(), ",");
            params.setPackValidate(packValidateResults);
            params.setPackValidateDetail(validateItem.getPackValidateDetail());
            String networkInfo = String.format("网点名称:%s, 网点地址:%s, 网点联系方式:%s", validateItem.getReceiver(), validateItem.getReceiveAddress(), validateItem.getReceivePhone());
            params.setNetworkInfo(networkInfo);
        }
        params.setOperator(message.getUpdaterName());
        params.setCreateById(message.getUpdaterId());
        params.setCreateDt(message.getUpdateDt());
        params.setStatus(B2BOrderActionEnum.VALIDATE.value);
        return vioMiOrderFeign.orderNeedValidate(params);
    }

    private MSResponse<Integer> applyFinished(MQB2BOrderStatusUpdateMessage.B2BOrderStatusUpdateMessage message) {
        VioMiOrderHandle params = new VioMiOrderHandle();
        params.setUniqueId(message.getMessageId());
        params.setB2bOrderId(message.getB2BOrderId());
        params.setOrderNumber(message.getB2BOrderNo());
        params.setVerifyCode(VIOMI_VERIFY_CODE);
        params.setOperator(message.getUpdaterName());
        params.setCreateById(message.getUpdaterId());
        params.setCreateDt(message.getUpdateDt());
        params.setStatus(B2BOrderActionEnum.COMPLETE.value);
        return vioMiOrderFeign.applyFinished(params);
    }

    private MSResponse<Integer> orderReturnVisit(MQB2BOrderStatusUpdateMessage.B2BOrderStatusUpdateMessage message) {
        VioMiOrderHandle params = new VioMiOrderHandle();
        params.setUniqueId(message.getMessageId() + 1);
        params.setB2bOrderId(message.getB2BOrderId());
        params.setOrderNumber(message.getB2BOrderNo());
        params.setOperator(message.getUpdaterName());
        params.setCreateById(message.getUpdaterId());
        params.setCreateDt(message.getUpdateDt());
        params.setStatus(B2BOrderActionEnum.RETURN_VISIT.value);
        return vioMiOrderFeign.orderReturnVisit(params);
    }

    /**
     * 取消
     */
    private MSResponse orderCancel(MQB2BOrderStatusUpdateMessage.B2BOrderStatusUpdateMessage message) {
        VioMiOrderCancel params = new VioMiOrderCancel();
        params.setUniqueId(message.getMessageId());
        params.setB2bOrderId(message.getB2BOrderId());
        params.setOrderNumber(message.getB2BOrderNo());
        params.setReason(message.getB2BReason());
        params.setRemarks(message.getRemarks());
        params.setOperator(message.getUpdaterName());
        params.setCreateById(message.getUpdaterId());
        params.setCreateDt(message.getUpdateDt());
        return vioMiOrderFeign.cancel(params);
    }

    private MSResponse applyForCancel(MQB2BOrderStatusUpdateMessage.B2BOrderStatusUpdateMessage message) {
        VioMiOrderCancel params = new VioMiOrderCancel();
        params.setUniqueId(message.getMessageId());
        params.setB2bOrderId(message.getB2BOrderId());
        params.setOrderNumber(message.getB2BOrderNo());
        params.setReason(message.getB2BReason());
        params.setCode(message.getVerifyCode());
        params.setRemarks(message.getRemarks());
        params.setOperator(message.getUpdaterName());
        params.setCreateById(message.getUpdaterId());
        params.setCreateDt(message.getUpdateDt());
        return vioMiOrderFeign.cancel(params);
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
        } else if (message.getStatus() == B2BOrderStatusEnum.SERVICED.value) {
            response = orderClockInHome(message);
        } else if (message.getStatus() == B2BOrderStatusEnum.APP_COMPLETED.value) {
            response = processComplete(message);
        } else if (message.getStatus() == B2BOrderStatusEnum.VALIDATE.value) {
            response = orderValidate(message);
        } else if (message.getStatus() == B2BOrderStatusEnum.COMPLETED.value) {
            response = applyFinished(message);
            response = orderReturnVisit(message);
        } else if (message.getStatus() == B2BOrderStatusEnum.CANCELED.value) {
            response = orderCancel(message);
        }  else if (message.getStatus() == B2BOrderStatusEnum.APPLIED_FOR_CANCEL.value) {
            applyForCancel(message);//该操作不重试
            response = new MSResponse<>(MSErrorCode.SUCCESS);
        } else {
            response = new MSResponse<>(MSErrorCode.SUCCESS);
            String msgJson = new JsonFormat().printToString(message);
            LogUtils.saveLog("B2B工单状态变更消息的状态错误", "VioMiOrderService.sendOrderStatusUpdateCommandToB2B", msgJson, null, null);
        }
        return response;
    }

    /**
     * 创建派单请求对象
     */
    public TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> createPlanRequestEntity(String engineerName, String engineerMobile, User updater) {
        TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> result = new TwoTuple<>(false, null);
        if (StringUtils.isNotBlank(engineerName) && StringUtils.isNotBlank(engineerMobile)) {
            B2BOrderStatusUpdateReqEntity.Builder builder = new B2BOrderStatusUpdateReqEntity.Builder();
            builder.setEngineerName(engineerName)
                    .setEngineerMobile(engineerMobile)
                    .setUpdaterName(updater != null && StringUtils.isNotBlank(updater.getName()) ? updater.getName() : "");
            result.setAElement(true);
            result.setBElement(builder);
        }
        return result;
    }

    /**
     * 创建预约请求对象
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
     * 创建上门请求对象
     */
    public TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> createServiceRequestEntity(Long kklOrderId, String kklQuarter, User updater) {
        TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> result = new TwoTuple<>(false, null);
        if (kklOrderId != null) {
            OrderLocation location = orderLocationService.getByOrderId(kklOrderId, kklQuarter);
            if (location == null) {
                location = new OrderLocation();
                location.setLongitude(DEFAULT_LOCATION.getAElement());
                location.setLatitude(DEFAULT_LOCATION.getBElement());
            }
            B2BOrderStatusUpdateReqEntity.Builder builder = new B2BOrderStatusUpdateReqEntity.Builder();
            builder.setLongitude(location.getLongitude())
                    .setLatitude(location.getLatitude())
                    .setUpdaterName(updater != null && StringUtils.isNotBlank(updater.getName()) ? updater.getName() : "");
            result.setAElement(true);
            result.setBElement(builder);
        }
        return result;
    }

    /**
     * 创建App完工请求对象
     */
    @Transactional()
    public TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> createAppCompleteRequestEntity(Long orderId, String quarter, Long customerId, Long servicePointId, List<OrderItem> orderItems, Date orderCreateDate, User updater) {
        TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> result = new TwoTuple<>(false, null);
        if (orderId != null && orderId > 0) {
            long buyDt = 0;
            //2020-12-17 sd_order -> sd_order_head
            OrderAdditionalInfo orderAdditionalInfo = null;
            Order order = b2BOrderDao.getOrderAdditionalInfo(orderId, quarter);
            if(order != null && order.getAdditionalInfoPb() != null && order.getAdditionalInfoPb().length > 0){
                orderAdditionalInfo = OrderAdditionalInfoUtils.pbBypesToAdditionalInfo(order.getAdditionalInfoPb());
            }
            if (orderAdditionalInfo != null && orderAdditionalInfo.getBuyDate() != null && orderAdditionalInfo.getBuyDate() > 0) {
                buyDt = orderAdditionalInfo.getBuyDate();
            } else {
                buyDt = orderCreateDate != null ? orderCreateDate.getTime() : 0;
            }
            List<B2BOrderCompletedItem> completedItems = getOrderCompletedItems(customerId, orderId, quarter, orderItems, buyDt);
            B2BOrderPraiseItem praiseItem = getOrderPraiseItem(orderId, quarter, servicePointId);
            B2BOrderStatusUpdateReqEntity.Builder builder = new B2BOrderStatusUpdateReqEntity.Builder();
            builder.setOrderCompletedItems(completedItems)
                    .setOrderPraiseItem(praiseItem)
                    .setUpdaterName(updater != null && StringUtils.isNotBlank(updater.getName()) ? updater.getName() : "");
            result.setAElement(true);
            result.setBElement(builder);
        }
        return result;
    }


    private List<B2BOrderCompletedItem> getOrderCompletedItems(Long customerId, Long orderId, String quarter, List<OrderItem> orderItems, long buyDt) {
        List<B2BOrderCompletedItem> result = Lists.newArrayList();
        if (orderId != null && orderId > 0) {
            Set<Long> productIdSet = Sets.newHashSet();

            Map<Long, B2BOrderCompletedItem> completedItemMap = Maps.newConcurrentMap();
            List<OrderItemComplete> completeList = orderItemCompleteService.getByOrderId(orderId, quarter);
            B2BOrderCompletedItem completedItem;
            if (completeList != null && !completeList.isEmpty()) {
                for (OrderItemComplete item : completeList) {
                    productIdSet.add(item.getProduct().getId());
                    completedItem = new B2BOrderCompletedItem();
                    completedItem.setProductId(item.getProduct().getId());
                    completedItem.setUnitBarcode(StringUtils.toString(item.getUnitBarcode()));

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
                    //多个相同的产品仅保存第一个
                    if (!completedItemMap.containsKey(item.getProduct().getId())) {
                        completedItemMap.put(item.getProduct().getId(), completedItem);
                    }
                    result.add(completedItem);
                }
            }

            //读取维修故障信息
            Map<Long, List<B2BOrderCompletedItem.ErrorItem>> errorMap = Maps.newConcurrentMap();
            List<OrderDetail> details = b2BOrderDao.getOrderErrors(orderId, quarter);
            if (details != null && !details.isEmpty()) {
                List<NameValuePair<Long, Long>> errorTypeIds = Lists.newArrayList();
                List<NameValuePair<Long, Long>> errorCodeIds = Lists.newArrayList();
                List<NameValuePair<Long, Long>> errorActionIds = Lists.newArrayList();
                for (OrderDetail detail : details) {
                    productIdSet.add(detail.getProductId());
                    errorTypeIds.add(new NameValuePair<>(detail.getProduct().getId(), detail.getErrorType().getId()));
                    errorCodeIds.add(new NameValuePair<>(detail.getProduct().getId(), detail.getErrorCode().getId()));
                    errorActionIds.add(new NameValuePair<>(detail.getProduct().getId(), detail.getActionCode().getId()));
                }
                List<MDErrorType> errorTypes = msCustomerErrorTypeService.findListByCustomerIdAndProductIdsAndIds(customerId, errorTypeIds);
                List<MDErrorCode> errorCodes = msCustomerErrorCodeService.findListByCustomerIdAndProductIdsAndIds(customerId, errorCodeIds);
                List<MDActionCode> actionCodes = msCustomerErrorActionService.findListByCustomerIdAndProductIdsAndIds(customerId, errorActionIds);
                Map<String, MDErrorType> errorTypeMap = Maps.newConcurrentMap();
                for (MDErrorType type : errorTypes) {
                    errorTypeMap.put(String.format("%d:%d", type.getProductId(), type.getId()), type);
                }
                Map<String, MDErrorCode> errorCodeMap = Maps.newConcurrentMap();
                for (MDErrorCode code : errorCodes) {
                    errorCodeMap.put(String.format("%d:%d", code.getProductId(), code.getId()), code);
                }
                Map<String, MDActionCode> errorActionMap = Maps.newConcurrentMap();
                for (MDActionCode action : actionCodes) {
                    errorActionMap.put(String.format("%d:%d", action.getProductId(), action.getId()), action);
                }
                Long productId;
                MDErrorType errorType;
                MDErrorCode errorCode;
                MDActionCode actionCode;
                B2BOrderCompletedItem.ErrorItem errorItem;
                for (OrderDetail detail : details) {
                    errorItem = new B2BOrderCompletedItem.ErrorItem();
                    productId = detail.getProductId();
                    errorType = errorTypeMap.get(String.format("%d:%d", productId, detail.getErrorType().getId()));
                    if (errorType != null) {
                        errorItem.setErrorTypeId(errorType.getId());
                        errorItem.setErrorType(errorType.getName());
                    }
                    errorCode = errorCodeMap.get(String.format("%d:%d", productId, detail.getErrorCode().getId()));
                    if (errorCode != null) {
                        errorItem.setErrorCodeId(errorCode.getId());
                        errorItem.setErrorCode(errorCode.getName());
                    }
                    actionCode = errorActionMap.get(String.format("%d:%d", productId, detail.getActionCode().getId()));
                    if (actionCode != null) {
                        errorItem.setErrorAnalysisId(actionCode.getId());
                        errorItem.setErrorAnalysis(actionCode.getAnalysis());
                        errorItem.setErrorActionId(actionCode.getId());
                        errorItem.setErrorAction(actionCode.getName());
                    }
                    if (errorMap.containsKey(productId)) {
                        errorMap.get(productId).add(errorItem);
                    } else {
                        errorMap.put(productId, Lists.newArrayList(errorItem));
                    }
                }
            }

            //读取配件信息
            Map<Long, List<B2BOrderCompletedItem.Material>> materialMap = Maps.newConcurrentMap();
            List<MaterialItem> materials = b2BOrderDao.getOrderMaterials(orderId, quarter);
            if (materials != null && !materials.isEmpty()) {
                List<NameValuePair<Long, String>> productIdAndCustomerModels = orderMaterialService.getOrderProductIdAndCustomerModels(orderItems);
                List<CustomerMaterial> params = Lists.newArrayList();
                CustomerMaterial param;
                String customerModel;
                CustomerProductModel productModel;
                for (MaterialItem item : materials) {
                    productIdSet.add(item.getProduct().getId());
                    param = new CustomerMaterial();
                    param.setCustomer(new Customer(customerId));
                    param.setProduct(item.getProduct());
                    param.setMaterial(new Material(item.getMaterial().getId()));
                    customerModel = productIdAndCustomerModels.stream().filter(i -> i.getName().equals(item.getProduct().getId())).findFirst().map(NameValuePair::getValue).orElse("");
                    productModel = new CustomerProductModel();
                    productModel.setCustomerModelId(customerModel);
                    param.setCustomerProductModel(productModel);
                    params.add(param);
                }
                List<CustomerMaterial> customerMaterials = msCustomerMaterialService.findListByCustomerMaterial(params);
                Map<String, String> customerPartCodeMap = Maps.newConcurrentMap();
                if (customerMaterials != null && !customerMaterials.isEmpty()) {
                    for (CustomerMaterial customerMaterial : customerMaterials) {
                        customerPartCodeMap.put(String.format("%d:%d", customerMaterial.getProduct().getId(), customerMaterial.getMaterial().getId()), customerMaterial.getCustomerPartCode());
                    }
                }
                String key;
                Long productId;
                Long materialId;
                String materialCode;
                B2BOrderCompletedItem.Material material;
                for (MaterialItem item : materials) {
                    productId = item.getProduct().getId();
                    materialId = item.getMaterial().getId();
                    key = String.format("%d:%d", productId, materialId);
                    materialCode = customerPartCodeMap.get(key);
                    material = new B2BOrderCompletedItem.Material();
                    material.setMaterialId(materialId);
                    material.setMaterialCode(StringUtils.toString(materialCode));
                    material.setQty(item.getQty());
                    if (materialMap.containsKey(productId)) {
                        materialMap.get(productId).add(material);
                    } else {
                        materialMap.put(productId, Lists.newArrayList(material));
                    }
                }
            }
            //设置维修故障与配件
            for (Long pId : productIdSet) {
                B2BOrderCompletedItem newCompletedItem = completedItemMap.get(pId);
                if (newCompletedItem == null) {
                    newCompletedItem = new B2BOrderCompletedItem();
                    newCompletedItem.setProductId(pId);
                    result.add(newCompletedItem);
                }
                List<B2BOrderCompletedItem.ErrorItem> b2bErrorItems = errorMap.get(pId);
                if (b2bErrorItems != null) {
                    newCompletedItem.setErrorItems(b2bErrorItems);
                }
                List<B2BOrderCompletedItem.Material> b2bMaterials = materialMap.get(pId);
                if (b2bMaterials != null) {
                    newCompletedItem.setMaterials(b2bMaterials);
                }
                newCompletedItem.setBuyDt(buyDt);
            }
        }
        return result;
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

    /**
     * 创建工单鉴定请求对象
     */
    @Transactional()
    public TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> createValidateRequestEntity(Long orderId, String quarter, Long servicePointId, Date orderCreateDate, OrderValidate orderValidate, User updater) {
        TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> result = new TwoTuple<>(false, null);
        if (orderId != null && orderId > 0) {
            long buyDt = 0;
            //2020-12-17 sd_order -> sd_order_head
            OrderAdditionalInfo orderAdditionalInfo = null;
            Order order = b2BOrderDao.getOrderAdditionalInfo(orderId, quarter);
            if(order != null && order.getAdditionalInfoPb() != null && order.getAdditionalInfoPb().length > 0){
                orderAdditionalInfo = OrderAdditionalInfoUtils.pbBypesToAdditionalInfo(order.getAdditionalInfoPb());
            }
            if (orderAdditionalInfo != null && orderAdditionalInfo.getBuyDate() != null && orderAdditionalInfo.getBuyDate() > 0) {
                buyDt = orderAdditionalInfo.getBuyDate();
            } else {
                buyDt = orderCreateDate != null ? orderCreateDate.getTime() : 0;
            }
            B2BOrderValidateItem validateItem = getOrderValidateItem(orderValidate);
            if (validateItem != null) {
                validateItem.setBuyDt(buyDt);
                ServicePoint servicePoint = msServicePointService.getSimpleCacheById(servicePointId);
                if (servicePoint != null && StringUtils.isNotBlank(servicePoint.getName())) {
                    validateItem.setReceiver(servicePoint.getName());
                }
                B2BOrderStatusUpdateReqEntity.Builder builder = new B2BOrderStatusUpdateReqEntity.Builder();
                builder.setOrderValidateItem(validateItem)
                        .setUpdaterName(updater != null && StringUtils.isNotBlank(updater.getName()) ? updater.getName() : "");
                result.setAElement(true);
                result.setBElement(builder);
            }
        }
        return result;
    }

    private B2BOrderValidateItem getOrderValidateItem(OrderValidate validate) {
        B2BOrderValidateItem result = null;
        if (validate != null) {
//            OrderValidate validate = msOrderValidateService.getUncompletedOrderValidate(orderId, quarter);
            result = new B2BOrderValidateItem();
            result.setProductId(validate.getProductId());
            result.setProductSn(validate.getProductSn());
            result.setIsFault(validate.getIsFault());
            result.setErrorDescription(validate.getErrorDescription());
            result.setCheckValidateDetail(validate.getCheckValidateDetail());
            result.setPackValidateDetail(validate.getPackValidateDetail());
            result.setReceiver(validate.getReceiver());
            result.setReceivePhone(validate.getReceivePhone());
            result.setReceiveAddress(validate.getReceiveAddress());

            B2BOrderValidateItem.ErrorItem errorItem = new B2BOrderValidateItem.ErrorItem();
            if (validate.getErrorTypeId() != null && validate.getErrorTypeId() > 0) {
                MDErrorType errorType = msCustomerErrorTypeService.getByProductIdAndCustomerIdFromCache(validate.getCustomerId(), validate.getProductId(), validate.getErrorTypeId());
                if (errorType != null) {
                    errorItem.setErrorTypeId(errorType.getId());
                    errorItem.setErrorType(errorType.getName());
                }
            }
            if (validate.getErrorCodeId() != null && validate.getErrorCodeId() > 0) {
                MDErrorCode errorCode = msCustomerErrorCodeService.getByProductIdAndCustomerIdFromCache(validate.getCustomerId(), validate.getProductId(), validate.getErrorCodeId());
                if (errorCode != null) {
                    errorItem.setErrorCodeId(errorCode.getId());
                    errorItem.setErrorCode(errorCode.getName());
                }
            }
            if (validate.getActionCodeId() != null && validate.getActionCodeId() > 0) {
                MDActionCode actionCode = msCustomerErrorActionService.getByProductIdAndCustomerIdFromCache(validate.getCustomerId(), validate.getProductId(), validate.getActionCodeId());
                if (actionCode != null) {
                    errorItem.setErrorAnalysisId(actionCode.getId());
                    errorItem.setErrorAnalysis(actionCode.getAnalysis());
                    errorItem.setErrorActionId(actionCode.getId());
                    errorItem.setErrorAction(actionCode.getName());
                }
            }
            result.setErrorItem(errorItem);

            Dict dict;
            List<String> checkValidateResultValues = BitUtils.getPositions(validate.getCheckValidateResult(), String.class);
            if (ObjectUtil.isNotEmpty(checkValidateResultValues)) {
                Map<String, Dict> checkValidateResultMap = msOrderValidateService.getCheckValidateResultMap();
                for (String value : checkValidateResultValues) {
                    dict = checkValidateResultMap.get(value);
                    if (dict != null) {
                        result.getCheckValidateResultValues().add(dict.getLabel());
                    }
                }
            }
            List<String> packValidateResultValues = BitUtils.getPositions(validate.getPackValidateResult(), String.class);
            if (ObjectUtil.isNotEmpty(packValidateResultValues)) {
                Map<String, Dict> packValidateResultMap = msOrderValidateService.getPackValidateResultMap();
                for (String value : packValidateResultValues) {
                    dict = packValidateResultMap.get(value);
                    if (dict != null) {
                        result.getPackValidateResultValues().add(dict.getLabel());
                    }
                }
            }
            if (ObjectUtil.isNotEmpty(validate.getPicItems())) {
                for (ValidatePicItem item : validate.getPicItems()) {
                    result.getPicUrls().add(OrderPicUtils.getPraisePicUrl(item.getUrl()));
                }
            }
        }
        return result;
    }


    /**
     * 创建完成请求对象
     */
    public TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> createCompleteRequestEntity(User updater) {
        TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> result = new TwoTuple<>(false, null);
        B2BOrderStatusUpdateReqEntity.Builder builder = new B2BOrderStatusUpdateReqEntity.Builder();
        builder.setUpdaterName(updater != null && StringUtils.isNotBlank(updater.getName()) ? updater.getName() : "");
        result.setAElement(true);
        result.setBElement(builder);
        return result;
    }


    /**
     * 创建取消请求对象
     */
    public TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> createCancelRequestEntity(Integer kklCancelType, User updater, String remarks) {
        TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> result = new TwoTuple<>(false, null);
        if (kklCancelType != null && updater != null && updater.getId() != null && updater.getId() > 0) {
            updater = MSUserUtils.get(updater.getId());
            if (updater == null) {
                updater = KKL_VIOMI_B2B_USER;
            }
            String cancelReason = B2BMDUtils.getVioMiCancelReason(kklCancelType);
            if (StringUtils.isNotBlank(cancelReason)) {
                String updaterName = StringUtils.isNotBlank(updater.getName()) ? updater.getName() : KKL_VIOMI_B2B_USER.getName();
                B2BOrderStatusUpdateReqEntity.Builder builder = new B2BOrderStatusUpdateReqEntity.Builder();
                builder.setUpdaterName(updaterName)
                        .setB2bReason(cancelReason)
                        .setRemarks(StringUtils.toString(remarks));
                result.setAElement(true);
                result.setBElement(builder);
            }
        }
        return result;
    }

    public TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> createApplyForCancelRequestEntity(Integer kklCancelType, User updater, String verifyCode, String remarks) {
        TwoTuple<Boolean, B2BOrderStatusUpdateReqEntity.Builder> result = new TwoTuple<>(false, null);
        if (kklCancelType != null && updater != null && updater.getId() != null && updater.getId() > 0) {
            updater = MSUserUtils.get(updater.getId());
            if (updater == null) {
                updater = KKL_VIOMI_B2B_USER;
            }
            String cancelReason = B2BMDUtils.getVioMiCancelReason(kklCancelType);
            if (StringUtils.isNotBlank(cancelReason)) {
                String updaterName = StringUtils.isNotBlank(updater.getName()) ? updater.getName() : KKL_VIOMI_B2B_USER.getName();
                B2BOrderStatusUpdateReqEntity.Builder builder = new B2BOrderStatusUpdateReqEntity.Builder();
                builder.setUpdaterName(updaterName)
                        .setB2bReason(cancelReason)
                        .setVerifyCode(verifyCode)
                        .setRemarks(StringUtils.toString(remarks));
                result.setAElement(true);
                result.setBElement(builder);
            }
        }
        return result;
    }


    /**
     * 确认收货(换货流程)
     */
    public MSResponse<Integer> confirmReceived(int dataSource,Long b2bOrderId, String b2bOrderNo, User updater, Date updateDate, String remarks) {
        MSResponse response = new MSResponse(MSErrorCode.SUCCESS);
        MQB2BOrderDismountReturnMessage.B2BOrderDismountReturnMessage message = null;
        try {
            message = MQB2BOrderDismountReturnMessage.B2BOrderDismountReturnMessage.newBuilder()
                    .setId(sequenceIdUtils.nextId())
                    .setDataSource(dataSource)
                    .setB2BOrderId(b2bOrderId)
                    .setB2BOrderNo(b2bOrderNo)
                    .setStatus(B2BOrderActionEnum.CONFIRM_RECEIVED.value)
                    .setOperator(updater.getName())
                    .setCreateById(updater.getId())
                    .setCreateDt(updateDate.getTime())
                    .build();
            orderDismountReturnMQSender.sendDelay(message, 0, 0);
        }catch (Exception e){
            StringBuilder json = new StringBuilder(1000);
            if(message != null){
                json.append(new JsonFormat().printToString(message));
            }else{
                json.append("dataSource: ").append(dataSource)
                        .append(", b2bOrderId: ").append(b2bOrderId)
                        .append(", updater: ").append(updater.getName())
                        .append(", updateDate: ").append(DateUtils.formatDateTime(updateDate))
                        .append(", remarks: ").append(remarks);
            }
            log.error("发送退换货-确认收货消息错误,data:{}",json.toString(),e);
            response = new MSResponse(MSErrorCode.newInstance(MSErrorCode.FAILURE, ExceptionUtil.getMessage(e)));
        }
        return response;

        //VioMiOrderHandle params = new VioMiOrderHandle();
        //params.setB2bOrderId(b2bOrderId);
        //params.setOrderNumber(b2bOrderNo);
        //params.setStatus(B2BOrderActionEnum.CONFIRM_RECEIVED.value);
        //params.setOperator(updater.getName());
        //params.setCreateById(updater.getId());
        //params.setCreateDt(updateDate.getTime());
        //return vioMiOrderFeign.orderConfirm(params);
    }

    /**
     * 退换货-拆装
     */
    public MSResponse<Integer> orderDismounting(int dataSource,Integer orderType, Long b2bOrderId, String b2bOrderNo, User updater, Date updateDate, OrderReturnComplete item) {
        MSResponse<Integer> checkResult = checkDiscountingItem(orderType, item);
        if (!MSResponse.isSuccessCode(checkResult)) {
            return checkResult;
        }

        MSResponse response = new MSResponse(MSErrorCode.SUCCESS);
        MQB2BOrderDismountReturnMessage.B2BOrderDismountReturnMessage message = null;
        try {
            MQB2BOrderDismountReturnMessage.B2BOrderDismountReturnMessage.Builder builder = MQB2BOrderDismountReturnMessage.B2BOrderDismountReturnMessage.newBuilder()
                    .setId(sequenceIdUtils.nextId())
                    .setDataSource(dataSource)
                    .setB2BOrderId(b2bOrderId)
                    //.setB2BOrderNo(b2bOrderNo)
                    .setStatus(B2BOrderActionEnum.DISMOUNTING.value)
                    .setOperator(updater.getName())
                    .setCreateById(updater.getId())
                    .setCreateDt(updateDate.getTime());
                //item
                if (orderType == OrderUtils.OrderTypeEnum.EXCHANGE.getId()) {//换货
                    builder.setOldSn(item.getOldSN());
                    builder.setMiSn(item.getNewSN());
                } else {
                    builder.setMiSn(item.getOldSN());
                }
            //photos
            OrderReturnComplete.JsonItem jsonItem = item.getJsonItem();
            if (jsonItem != null) {
                builder.addAllAttachment(getPhotoFullUrl(jsonItem.getPhotos()));
            }
            message = builder.build();
            orderDismountReturnMQSender.sendDelay(message, 0, 0);
        }catch (Exception e){
            StringBuilder json = new StringBuilder(1000);
            if(message != null){
                json.append(new JsonFormat().printToString(message));
            }else{
                json.append("dataSource: ").append(dataSource)
                        .append(", b2bOrderId: ").append(b2bOrderId)
                        .append(", updater: ").append(updater.getName())
                        .append(", updateDate: ").append(DateUtils.formatDateTime(updateDate));
                try {
                    json.append(", item: ").append(GsonUtils.getInstance().toGson(item));
                }catch (Exception e1){
                    json.append(", item: 序列化失败");
                }
            }
            log.error("发送退换货-拆装产品信息消息错误,data:{}",json.toString(),e);
            response = new MSResponse(MSErrorCode.newInstance(MSErrorCode.FAILURE, ExceptionUtil.getMessage(e)));
        }
        return response;
        /*
        VioMiOrderHandle params = new VioMiOrderHandle();
        params.setB2bOrderId(b2bOrderId);
        //params.setOrderNumber(b2bOrderNo);
        params.setStatus(B2BOrderActionEnum.DISMOUNTING.value);
        params.setOperator(updater.getName());
        params.setCreateById(updater.getId());
        params.setCreateDt(updateDate.getTime());
        //item
        if (orderType == OrderUtils.OrderTypeEnum.EXCHANGE.getId()) {//换货
            params.setOldSn(item.getOldSN());
            params.setMiSn(item.getNewSN());
        } else {
            params.setMiSn(item.getOldSN());
        }
        //photos
        OrderReturnComplete.JsonItem jsonItem = item.getJsonItem();
        if (jsonItem != null) {
            params.setAttachment(getPhotoFullUrl(jsonItem.getPhotos()));
            //List<OrderReturnComplete.PicSubItem> photos = jsonItem.getPhotos();
            //if(!CollectionUtils.isEmpty(photos)) {
            //    List<String> urls = photos.stream().map(t -> t.getUrl()).collect(Collectors.toList());
            //    params.setAttachment(urls);
            //}
        }
        return vioMiOrderFeign.orderDismounting(params);
        */
    }

    /**
     * 图片路径处理，加域名和前缀
     *
     * @param photos
     */
    private List<String> getPhotoFullUrl(List<OrderReturnComplete.PicSubItem> photos) {
        if (CollectionUtils.isEmpty(photos)) {
            return Lists.newArrayList();
        }
        List<String> urls = Lists.newArrayList();
        StringBuilder url = new StringBuilder();
        for (OrderReturnComplete.PicSubItem photo : photos) {
            if (StringUtils.isBlank(photo.getUrl())) {
                continue;
            }
            url.setLength(0);
            url.append(imgShowUrl).append("/").append(photo.getUrl());
            urls.add(url.toString());
        }
        return urls;
    }

    /**
     * 检验拆装内容
     *
     * @param orderType 订单类型 3-退货  4-换货
     * @param item      检验项目
     * @return
     */
    private MSResponse<Integer> checkDiscountingItem(Integer orderType, OrderReturnComplete item) {
        if (item == null || item.getItemType() != OrderReturnComplete.ItemTypeEnum.DISMOUNT.getId()) {
            return new MSResponse<Integer>(MSErrorCode.newInstance(MSErrorCode.FAILURE, "参数错误"));
        }
        if (item.getJsonItem() == null || CollectionUtils.isEmpty(item.getJsonItem().getPhotos())) {
            return new MSResponse<Integer>(MSErrorCode.newInstance(MSErrorCode.FAILURE, "无上传图片信息"));
        }
        if (StringUtils.isBlank(item.getOldSN())) {
            return new MSResponse<Integer>(MSErrorCode.newInstance(MSErrorCode.FAILURE, "无故障货品SN码"));
        }
        if (orderType == OrderUtils.OrderTypeEnum.EXCHANGE.getId()) {
            if (StringUtils.isBlank(item.getNewSN())) {
                return new MSResponse<Integer>(MSErrorCode.newInstance(MSErrorCode.FAILURE, "无新到货品SN码"));
            }
        }
        //图片
        List<OrderReturnComplete.PicSubItem> photos = item.getJsonItem().getPhotos();
        if (CollectionUtils.isEmpty(photos)) {
            return new MSResponse<Integer>(MSErrorCode.newInstance(MSErrorCode.FAILURE, "无上传图片信息"));
        }
        if (orderType == OrderUtils.OrderTypeEnum.BACK.getId() && photos.size() < 3) {
            return new MSResponse<Integer>(MSErrorCode.newInstance(MSErrorCode.FAILURE, "退货需上传3张图片"));
        }
        if (orderType == OrderUtils.OrderTypeEnum.EXCHANGE.getId() && photos.size() < 5) {
            return new MSResponse<Integer>(MSErrorCode.newInstance(MSErrorCode.FAILURE, "换货需上传5张图片"));
        }
        return new MSResponse<Integer>(1);
    }

    /**
     * 退换货-寄回
     */
    public MSResponse<Integer> backLogistics(int dataSource,Integer orderType, Long b2bOrderId, String b2bOrderNo, User updater, Date updateDate, OrderReturnComplete item) {
        MSResponse<Integer> checkResult = checkLogisticsItem(item);
        if (!MSResponse.isSuccessCode(checkResult)) {
            return checkResult;
        }

        MSResponse response = new MSResponse(MSErrorCode.SUCCESS);
        MQB2BOrderDismountReturnMessage.B2BOrderDismountReturnMessage message = null;
        try {
            MQB2BOrderDismountReturnMessage.B2BOrderDismountReturnMessage.Builder builder = MQB2BOrderDismountReturnMessage.B2BOrderDismountReturnMessage.newBuilder()
                    .setId(sequenceIdUtils.nextId())
                    .setDataSource(dataSource)
                    .setB2BOrderId(b2bOrderId)
                    //.setB2BOrderNo(b2bOrderNo)
                    .setStatus(B2BOrderActionEnum.BACK_PRODUCT.value)
                    .setOperator(updater.getName())
                    .setCreateById(updater.getId())
                    .setCreateDt(updateDate.getTime());
            //item
            OrderReturnComplete.JsonItem logistics = item.getJsonItem();
            builder.setBackLogisticsCompany(logistics.getCompany());
            builder.setBackLogisticsNumber(logistics.getNumber());
            builder.addAllBackProductPhone(getPhotoFullUrl(logistics.getPhotos()));
            message = builder.build();
            orderDismountReturnMQSender.sendDelay(message, 60, 0);
        }catch (Exception e){
            StringBuilder json = new StringBuilder(1000);
            if(message != null){
                json.append(new JsonFormat().printToString(message));
            }else{
                json.append("dataSource: ").append(dataSource)
                        .append(", b2bOrderId: ").append(b2bOrderId)
                        .append(", updater: ").append(updater.getName())
                        .append(", updateDate: ").append(DateUtils.formatDateTime(updateDate));
                try {
                    json.append(", item: ").append(GsonUtils.getInstance().toGson(item));
                }catch (Exception e1){
                    json.append(", item: 序列化失败");
                }
            }
            log.error("发送退换货-寄回快递信息消息错误,data:{}",json.toString(),e);
            response = new MSResponse(MSErrorCode.newInstance(MSErrorCode.FAILURE, ExceptionUtil.getMessage(e)));
        }
        return response;
        /*
        //check 项目
        MSResponse<Integer> checkResult = checkLogisticsItem(item);
        if (!MSResponse.isSuccessCode(checkResult)) {
            return checkResult;
        }
        VioMiOrderHandle params = new VioMiOrderHandle();
        params.setB2bOrderId(b2bOrderId);
        //params.setOrderNumber(b2bOrderNo);
        params.setStatus(B2BOrderActionEnum.BACK_PRODUCT.value);
        params.setOperator(updater.getName());
        params.setCreateById(updater.getId());
        params.setCreateDt(updateDate.getTime());
        //item
        OrderReturnComplete.JsonItem logistics = item.getJsonItem();
        params.setBackProductLogisticsCompany(logistics.getCompany());
        params.setBackProductLogisticsNumber(logistics.getNumber());
        params.setBackProductPhone(getPhotoFullUrl(logistics.getPhotos()));
        return vioMiOrderFeign.orderServicePointSend(params);
         */
    }

    /**
     * 检验拆装内容
     *
     * @param item 检验项目
     * @return
     */
    private MSResponse<Integer> checkLogisticsItem(OrderReturnComplete item) {
        if (item == null || item.getItemType() != OrderReturnComplete.ItemTypeEnum.LOGISTICS.getId()) {
            return new MSResponse<Integer>(MSErrorCode.newInstance(MSErrorCode.FAILURE, "参数错误"));
        }
        if (item.getJsonItem() == null) {
            return new MSResponse<Integer>(MSErrorCode.newInstance(MSErrorCode.FAILURE, "无物流信息"));
        }

        OrderReturnComplete.JsonItem logistics = item.getJsonItem();
        if (StringUtils.isBlank(logistics.getCompany())) {
            return new MSResponse<Integer>(MSErrorCode.newInstance(MSErrorCode.FAILURE, "未上传物流公司"));
        }
        if (StringUtils.isBlank(logistics.getNumber())) {
            return new MSResponse<Integer>(MSErrorCode.newInstance(MSErrorCode.FAILURE, "未上传物流单号"));
        }
        if (CollectionUtils.isEmpty(logistics.getPhotos())) {
            return new MSResponse<Integer>(MSErrorCode.newInstance(MSErrorCode.FAILURE, "未上传寄回产品照片"));
        }
        return new MSResponse<Integer>(1);
    }

    /**
     * 处理退换货入口
     * @param message
     * @return
     */
    public MSResponse processDismountAndReturnMessage(MQB2BOrderDismountReturnMessage.B2BOrderDismountReturnMessage message){
        MSResponse response = new MSResponse(MSErrorCode.SUCCESS);
        if(message == null){
            return response;
        }
        if(message.getStatus() == B2BOrderActionEnum.CONFIRM_RECEIVED.value){
            return confirmReceived(message);
        }else if(message.getStatus() == B2BOrderActionEnum.DISMOUNTING.value){
            return dismounting(message);
        }else if(message.getStatus() == B2BOrderActionEnum.BACK_PRODUCT.value){
            return backLogistics(message);
        }else{
            return response;
        }
    }


    /**
     * 退换货- 确认收货
     * @param message
     * @return
     */
    private MSResponse confirmReceived(MQB2BOrderDismountReturnMessage.B2BOrderDismountReturnMessage message){
        MSResponse response = new MSResponse(MSErrorCode.SUCCESS);
        try{
            VioMiOrderHandle params = new VioMiOrderHandle();
            params.setUniqueId(message.getId());
            params.setB2bOrderId(message.getB2BOrderId());
            params.setOrderNumber(message.getB2BOrderNo());
            params.setStatus(message.getStatus());
            params.setOperator(message.getOperator());
            params.setCreateById(message.getCreateById());
            params.setCreateDt(message.getCreateDt());
            return vioMiOrderFeign.orderConfirm(params);
        }catch (Exception e) {
            response = new MSResponse(MSErrorCode.newInstance(MSErrorCode.FAILURE, ExceptionUtil.getMessage(e)));
            String msgJson = new JsonFormat().printToString(message);
            LogUtils.saveLog("【云米】确认收货错误", "VioMiOrderService.confirmReceived", msgJson, null, null);
        }
        return response;
    }

    /**
     * 退换货- 拆装
     * @param message
     * @return
     */
    private MSResponse dismounting(MQB2BOrderDismountReturnMessage.B2BOrderDismountReturnMessage message){
        MSResponse response;
        try{
            VioMiOrderHandle params = new VioMiOrderHandle();
            params.setUniqueId(message.getId());
            params.setB2bOrderId(message.getB2BOrderId());
            //params.setOrderNumber(message.getB2BOrderNo());
            params.setStatus(message.getStatus());
            params.setOperator(message.getOperator());
            params.setCreateById(message.getCreateById());
            params.setCreateDt(message.getCreateDt());
            //item
            params.setOldSn(message.getOldSn());
            params.setMiSn(message.getMiSn());
            params.setAttachment(message.getAttachmentList());
            return vioMiOrderFeign.orderDismounting(params);
        }catch (Exception e) {
            response = new MSResponse(MSErrorCode.newInstance(MSErrorCode.FAILURE, ExceptionUtil.getMessage(e)));
            String msgJson = new JsonFormat().printToString(message);
            LogUtils.saveLog("【云米】确认收货错误", "VioMiOrderService.dismounting", msgJson, null, null);
        }
        return response;
    }

    /**
     * 退换货-寄回
     */
    private MSResponse<Integer> backLogistics(MQB2BOrderDismountReturnMessage.B2BOrderDismountReturnMessage message){
        MSResponse response;
        try{
            VioMiOrderHandle params = new VioMiOrderHandle();
            params.setUniqueId(message.getId());
            params.setB2bOrderId(message.getB2BOrderId());
            //params.setOrderNumber(message.getB2BOrderNo());
            params.setStatus(message.getStatus());
            params.setOperator(message.getOperator());
            params.setCreateById(message.getCreateById());
            params.setCreateDt(message.getCreateDt());
            //item
            params.setBackProductLogisticsCompany(message.getBackLogisticsCompany());
            params.setBackProductLogisticsNumber(message.getBackLogisticsNumber());
            params.setBackProductPhone(message.getBackProductPhoneList());
            return vioMiOrderFeign.orderServicePointSend(params);
        }catch (Exception e) {
            response = new MSResponse(MSErrorCode.newInstance(MSErrorCode.FAILURE, ExceptionUtil.getMessage(e)));
            String msgJson = new JsonFormat().printToString(message);
            LogUtils.saveLog("【云米】寄回", "VioMiOrderService.backLogistics", msgJson, null, null);
        }
        return response;
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
            String remarks = StringUtils.toString(entity.getRemarks()) + "（" + B2BDataSourceEnum.VIOMI.name + "平台通知取消B2B工单）";
            if (order.getOrderCondition().getStatusValue() <= Order.ORDER_STATUS_ACCEPTED) {
                orderService.cancelOrderNew(order.getId(), B2BOrderVModel.b2bUser, remarks, false);
                result = true;
            } else if (order.getOrderCondition().getStatusValue() == Order.ORDER_STATUS_RETURNING) {
                orderService.approveReturnOrderNew(order.getId(), order.getQuarter(), remarks, B2BOrderVModel.b2bUser);
                result = true;
            } else if (order.getOrderCondition().getStatusValue() < Order.ORDER_STATUS_RETURNING) {
                orderService.returnOrderNew(order.getId(), new Dict("51", "厂家(电商)通知取消"), "", remarks, B2BOrderVModel.b2bUser);
//                orderService.b2bCancelOrder(order.getId(), order.getQuarter(), new Dict("51", "厂家(电商)通知取消"), remarks, B2BOrderVModel.b2bUser);
                result = true;
            }
        } else {
            B2BOrderProcessEntity.saveFailureLog(entity, "读取工单失败", "VioMiOrderService.cancelKKLOrder", null);
        }
        return result;
    }

    /**
     * 写工单日志
     */
    private boolean writeKKLOrderLog(B2BOrderProcessEntity entity) {
        boolean flag = false;
        Order order = orderCacheReadService.getOrderById(entity.getKklOrderId(), null, OrderUtils.OrderDataLevel.CONDITION, true, true);
        if (order != null && order.getOrderCondition() != null) {
            OrderProcessLog processLog = new OrderProcessLog();
            processLog.setQuarter(order.getQuarter());
            processLog.setOrderId(order.getId());
            processLog.setAction("B2B客户写工单日志");
            processLog.setActionComment(StringUtils.left(String.format("【%s日志】%s", B2BDataSourceEnum.VIOMI.name, entity.getRemarks()), 250));
            processLog.setStatus(MSDictUtils.getDictLabel(order.getOrderCondition().getStatus().getValue(), "order_status", "订单已审核"));
            processLog.setStatusValue(order.getOrderCondition().getStatusValue());
            processLog.setStatusFlag(OrderProcessLog.OPL_SF_NOT_CHANGE_STATUS);
            processLog.setCloseFlag(0);
            processLog.setCreateBy(B2BOrderVModel.b2bUser);
            processLog.setCreateDate(new Date());
            processLog.setCustomerId(order.getOrderCondition() != null ? order.getOrderCondition().getCustomerId() : 0);
            processLog.setDataSourceId(order.getDataSourceId());
            processLog.setVisibilityFlag(VisibilityFlagEnum.or(Sets.newHashSet(KEFU, CUSTOMER, SERVICE_POINT)));
            orderService.saveProcessLogToDB(processLog);
            flag = true;
        } else {
            B2BOrderProcessEntity.saveFailureLog(entity, "读取工单失败", "VioMiOrderService.writeKKLOrderLog", null);
        }
        return flag;
    }

    /**
     * 鉴定单
     */
    private boolean validateOrder(B2BOrderProcessEntity entity) {
        boolean result = false;
        Order order = orderCacheReadService.getOrderById(entity.getKklOrderId(), null, OrderUtils.OrderDataLevel.CONDITION, true, true);
        if (order != null && order.getOrderCondition() != null) {
            Date date = new Date();
            if (entity.getStatus() == 0) {
                msOrderValidateService.approveOrderValidate(order, entity.getRemarks(), B2BOrderVModel.b2bUser, date);
                result = true;
            } else {
                msOrderValidateService.rejectOrderValidate(order, entity.getRemarks(), B2BOrderVModel.b2bUser, date);
                result = true;
            }
        } else {
            B2BOrderProcessEntity.saveFailureLog(entity, "读取工单失败", "VioMiOrderService.cancelKKLOrder", null);
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
            String remarks = StringUtils.toString(entity.getRemarks()) + "（" + B2BDataSourceEnum.VIOMI.name + "平台审核B2B工单的退单申请）";
            orderService.approveReturnOrderNew(order.getId(), order.getQuarter(), remarks, B2BOrderVModel.b2bUser);
            result = true;
        } else {
            B2BOrderProcessEntity.saveFailureLog(entity, "读取工单失败", "VioMiOrderService.approveReturnKKLOrder", null);
        }
        return result;
    }

    /**
     * 退单申请驳回
     */
    private boolean rejectReturnKKLOrder(B2BOrderProcessEntity entity) {
        boolean result = false;
        Order order = orderCacheReadService.getOrderById(entity.getKklOrderId(), null, OrderUtils.OrderDataLevel.CONDITION, true, true);
        if (order != null && order.getOrderCondition() != null) {
            String remarks = StringUtils.toString(entity.getRemarks()) + "（" + B2BDataSourceEnum.VIOMI.name + "平台驳回B2B工单的退单申请）";
            orderService.rejectReturnOrderNew(order.getId(), order.getQuarter(), remarks, B2BOrderVModel.b2bUser);
            result = true;
        } else {
            B2BOrderProcessEntity.saveFailureLog(entity, "读取工单失败", "VioMiOrderService.rejectReturnKKLOrder", null);
        }
        return result;
    }


    public MSResponse processKKLOrder(B2BOrderProcessEntity processEntity) {
        boolean flag = false;
        try {
            if (processEntity.getActionType() == B2BOrderActionEnum.RETURN_APPLY) {
                updateProcessFlag(processEntity.getMessageId());
                flag = cancelKKLOrder(processEntity);
            } else if (processEntity.getActionType() == B2BOrderActionEnum.RETURN_APPROVE) {//樱雪审核退单审核
                updateProcessFlag(processEntity.getMessageId());
                if (processEntity.getStatus() == XYYOrderCancelAudit.REVIEW_STATUS_SUCCESS.intValue()) {
                    flag = approveReturnKKLOrder(processEntity);
                } else if (processEntity.getStatus() == XYYOrderCancelAudit.REVIEW_STATUS_FAILURE.intValue()) {
                    flag = rejectReturnKKLOrder(processEntity);
                }
            } else if (processEntity.getActionType() == B2BOrderActionEnum.LOG) {
                updateProcessFlag(processEntity.getMessageId());
                flag = writeKKLOrderLog(processEntity);
            } else if (processEntity.getActionType() == B2BOrderActionEnum.VALIDATE) {
                flag = validateOrder(processEntity);
            }
        } catch (Exception e) {
            B2BOrderProcessEntity.saveFailureLog(processEntity, "VioMiOrderService", "processKKLOrder", e);
        }
        return new MSResponse(flag ? MSErrorCode.SUCCESS : MSErrorCode.FAILURE);
    }


    private void updateProcessFlag(Long messageId) {
        vioMiOrderFeign.updateProcessFlag(messageId);
    }

    //endregion 处理快可立工单

    //region 配件单
//
//    /**
//     * 申请配件单
//     */
//    public MSResponse newMaterial(MaterialMaster materialMaster) {
//        try {
//            B2BMaterial materialForm = mapper.toB2BMaterialForm(materialMaster);
//            if (materialForm == null) {
//                return new MSResponse<>(MSErrorCode.newInstance(MSErrorCode.FAILURE, "配件单转新迎燕配件单错误"));
//            }
//            materialForm.setApplyType(materialMaster.getApplyType().getIntValue());
//            materialForm.setB2bOrderId(materialMaster.getB2bOrderId());
//            return xyyPlusOrderFeign.newMaterial(materialForm);
//        } catch (Exception e) {
//            log.error("orderId:{} ", materialMaster.getOrderId(), e);
//            return new MSResponse<>(MSErrorCode.newInstance(MSErrorCode.FAILURE, "微服务接口执行失败"));
//        }
//    }

    /**
     * 处理完"审核"消息回调通知微服务
     */
//    public MSResponse notifyApplyFlag(Long formId) {
//        return xyyPlusOrderFeign.updateAuditFlag(formId);
//    }

    /**
     * 处理完"已发货"消息回调通知微服务
     */
//    public MSResponse notifyDeliverFlag(Long formId) {
//        return xyyPlusOrderFeign.updateDeliverFlag(formId);
//    }


    //endregion   配件单


    //region  日志

    /**
     * 往云米微服务推送工单日志
     */
    public MSResponse pushOrderProcessLogToMS(MQB2BOrderProcessLogMessage.B2BOrderProcessLogMessage message) {
        VioMiOrderRemark log = new VioMiOrderRemark();
        log.setUniqueId(message.getId());
        log.setKklOrderId(message.getOrderId());
        log.setOperator(message.getOperatorName());
        log.setRemarks(message.getLogContext());
        log.setCreateById(message.getCreateById());
        log.setCreateDt(message.getCreateDt());
        return vioMiOrderFeign.saveLog(log);
    }

    /**
     * 创建云米日志消息实体
     */
    public TwoTuple<Boolean, B2BOrderProcessLogReqEntity.Builder> createOrderProcessLogReqEntity(OrderProcessLog log) {
        TwoTuple<Boolean, B2BOrderProcessLogReqEntity.Builder> result = new TwoTuple<>(false, null);
        if (log.getCreateBy() != null && StringUtils.isNotBlank(log.getCreateBy().getName())
                && StringUtils.isNotBlank(log.getActionComment())) {
            B2BOrderProcessLogReqEntity.Builder builder = new B2BOrderProcessLogReqEntity.Builder();
            builder.setOperatorName(log.getCreateBy().getName())
                    .setLogContext(log.getActionComment())
                    .setDataSourceId(B2BDataSourceEnum.VIOMI.id);
            result.setAElement(true);
            result.setBElement(builder);
        }
        return result;
    }
    //endregion  日志

    //region 验证产品SN

    public MSResponse checkProductSN(String b2bOrderNo, String productSn, User operator) {
        VioMiOrderSnCode params = new VioMiOrderSnCode();
        params.setOrderNumber(b2bOrderNo);
        params.setSnCode(productSn);
        params.setCreateById(operator.getId());
        return vioMiOrderFeign.getGradeSn(params);
    }

    //endregion 验证产品SN

    //region 投诉

    public MSResponse complainProcess(MQB2BOrderComplainProcessMessage.B2BOrderComplainProcessMessage message){
        MSResponse response = new MSResponse(MSErrorCode.SUCCESS);
        if(message.getOperationType() == B2BOrderEnum.ComplainOperationTypeEnum.CLOSE.value){
            response = completeComplainForm(message);
        }
        return response;
    }

    /**
     * 完成关闭投诉单
     */
    private MSResponse completeComplainForm(MQB2BOrderComplainProcessMessage.B2BOrderComplainProcessMessage message) {
        //Long complainId, String complainNo, String b2bComplainNo, User operator, String content, List<String> attachments
        B2BOrderComplainProcess process = new B2BOrderComplainProcess()
                .setComplainId(message.getKklComplainId())
                .setComplainNo(message.getKklComplainNo())
                .setB2bComplainNo(message.getB2BComplainNo())
                .setOperationType(message.getOperationType())//20关闭
                .setContent(message.getContent())
                .setCreateAt(message.getCreateAt())
                .setCreateId(message.getOperatorId())
                .setCreateName(message.getOperator())
                .setAttachments(message.getAttachmentList());
        return vioMiOrderFeign.complainCompleted(process);
    }

    //endregion 投诉

    //region 取消验证码

    public MSResponse sendCancelVerifyCode(String b2bOrderNo, String phone, String reason, User operator) {
        VioMiOrderSendSms params = new VioMiOrderSendSms();
        params.setOrderNumber(b2bOrderNo);
        params.setPhone(phone);
        params.setReason(reason);
        params.setCreateById(operator.getId());
        return vioMiOrderFeign.cancelValidateCode(params);
    }

    //endregion 取消验证码
}
