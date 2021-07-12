package com.wolfking.jeesite.ms.validate.service;

import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.praise.PraiseStatusEnum;
import com.kkl.kklplus.entity.validate.*;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.exception.OrderException;
import com.wolfking.jeesite.common.service.SequenceIdService;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.wolfking.jeesite.modules.sd.entity.*;
import com.wolfking.jeesite.modules.sd.service.OrderRegionService;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.utils.SeqUtils;
import com.wolfking.jeesite.ms.b2bcenter.sd.service.B2BCenterOrderService;
import com.wolfking.jeesite.ms.praise.entity.ViewPraiseModel;
import com.wolfking.jeesite.ms.providermd.service.MSServiceTypeService;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import com.wolfking.jeesite.ms.validate.feign.OrderValidateFeign;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.wolfking.jeesite.modules.sd.utils.OrderUtils.ORDER_LOCK_EXPIRED;

@Slf4j
@Service
public class MSOrderValidateService extends OrderRegionService {

    private static final String DICT_TYPE_CHECK_VALIDATE_RESULT = "CHECK_VALIDATE_RESULT";
    private static final String DICT_TYPE_PACK_VALIDATE_RESULT = "PACK_VALIDATE_RESULT";
    private static final String DICT_TYPE_VALIDATE_PIC_STANDARD = "VALIDATE_PIC_STANDARD";

    /**
     * 安装 - 鉴定服务类型Code
     */
    public static final String SERVICE_TYPE_CODE_INSTALLATION_VALIDATE = "VI";
    /**
     * 维修 - 鉴定服务类型Code
     */
    public static final String SERVICE_TYPE_CODE_MAINTENANCE_VALIDATE = "VR";

    @Autowired
    private OrderValidateFeign orderValidateFeign;
    @Autowired
    private AreaService areaService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private SequenceIdService sequenceIdService;
    @Autowired
    private B2BCenterOrderService b2BCenterOrderService;
    @Autowired
    private MSServiceTypeService msServiceTypeService;


    //region 鉴定结果、包装鉴定结果、鉴定图片

    /**
     * 获取鉴定结果选项
     */
    public List<Dict> getCheckValidateResultList() {
        return getValidateOptions(DICT_TYPE_CHECK_VALIDATE_RESULT);
    }

    public Map<String, Dict> getCheckValidateResultMap() {
        return getValidateOptions(DICT_TYPE_CHECK_VALIDATE_RESULT).stream().collect(Collectors.toMap(Dict::getValue, i -> i));
    }

    /**
     * 获取包装鉴定结果选项
     */
    public List<Dict> getPackValidateResultList() {
        return getValidateOptions(DICT_TYPE_PACK_VALIDATE_RESULT);
    }

    public Map<String, Dict> getPackValidateResultMap() {
        return getValidateOptions(DICT_TYPE_PACK_VALIDATE_RESULT).stream().collect(Collectors.toMap(Dict::getValue, i -> i));
    }

    /**
     * 获取鉴定图片标准
     */
    public List<ValidatePicItem> getValidatePicStandard() {
        List<ValidatePicItem> result = Lists.newArrayList();
        List<Dict> dictList = MSDictUtils.getDictList(DICT_TYPE_VALIDATE_PIC_STANDARD);
        if (!ObjectUtil.isEmpty(dictList)) {
            dictList = dictList.stream().sorted(Comparator.comparing(Dict::getSort)).collect(Collectors.toList());
            for (Dict dict : dictList) {
                ValidatePicItem item = new ValidatePicItem();
                item.setCode(dict.getValue());
                item.setName(dict.getLabel());
                item.setUrl("");
                result.add(item);
            }
        }
        return result;
    }

    public Map<String, ValidatePicItem> getValidatePicStandardMap() {
        return getValidatePicStandard().stream().collect(Collectors.toMap(ValidatePicItem::getCode, i -> i));
    }

    private List<Dict> getValidateOptions(String optionType) {
        List<Dict> result = Lists.newArrayList();
        if (StrUtil.isNotEmpty(optionType)) {
            List<Dict> dictList = MSDictUtils.getDictList(optionType);
            if (!ObjectUtil.isEmpty(dictList)) {
                result = dictList.stream().sorted(Comparator.comparing(Dict::getSort)).collect(Collectors.toList());
            }
        }
        return result;
    }

    //endregion

    @Transactional(readOnly = false)
    public void createOrderValidate(Long orderId, String quarter, OrderValidate validate, ValidateCreatorTypeEnum creatorType, User user, Date createDate) {
        Order order = orderService.getOrderById(orderId, quarter, OrderUtils.OrderDataLevel.CONDITION, true);
        if (order == null || order.getOrderCondition() == null) {
            throw new RuntimeException("订单不存在，或读取订单失败");
        }
        if (order.isSuspended() == 1) {
            throw new RuntimeException("订单被挂起，无法操作，请联系客服");
        }
        OrderCondition condition = order.getOrderCondition();
        String lockKey = String.format(RedisConstant.SD_ORDER_LOCK, order.getId());
        Boolean locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockKey, 1, ORDER_LOCK_EXPIRED);
        if (!locked) {
            throw new OrderException("此订单正在处理中，请稍候重试。");
        }
        String validateNo = SeqUtils.NextSequenceNo("validateNo", 0, 3);
        if (StringUtils.isBlank(validateNo)) {
            throw new RuntimeException("生成鉴定单号失败");
        }
        try {
            long validateId = sequenceIdService.nextId();
            validate.setId(validateId);
            validate.setValidateNo(validateNo);
            validate.setStatus(ValidateStatusEnum.NEW.code);
            validate.setOrderId(order.getId());
            validate.setOrderNo(order.getOrderNo());
            validate.setQuarter(order.getQuarter());
            validate.setProductCategoryId(condition.getProductCategoryId());
            validate.setDataSource(order.getDataSourceId());
            validate.setWorkcardId(order.getWorkCardId());
            validate.setParentBizOrderId(order.getParentBizOrderId());
            validate.setAreaId(condition.getArea().getId());
            validate.setSubAreaId(condition.getSubArea().getId());
            Area area = areaService.getFromCache(condition.getArea().getId());
            if (area != null) {
                List<String> ids = Splitter.onPattern(",").omitEmptyStrings().trimResults().splitToList(area.getParentIds());
                if (ids.size() >= 2) {
                    validate.setCityId(Long.valueOf(ids.get(ids.size() - 1)));
                    validate.setProvinceId(Long.valueOf(ids.get(ids.size() - 2)));
                }
            }
            validate.setCustomerId(condition.getCustomer().getId());
            validate.setKefuId(condition.getKefu() != null && condition.getKefu().getId() != null ? condition.getKefu().getId() : 0);
            validate.setServicepointId(condition.getServicePoint().getId());
            validate.setEngineerId(condition.getEngineer().getId());
            validate.setUserName(condition.getUserName());
            validate.setUserPhone(condition.getServicePhone());
            validate.setUserAddress(condition.getArea().getName() + condition.getServiceAddress());
            validate.setRemarks("");
            validate.setCreatorType(creatorType.code);
            validate.setCreateById(user.getId());
            validate.setCreateDt(createDate.getTime());

            OrderValidateLog validateLog = new OrderValidateLog();
            validateLog.setId(sequenceIdService.nextId());
            validateLog.setStatus(ValidateStatusEnum.NEW.code);
            validateLog.setActionType(PraiseStatusEnum.NEW.code);
            validateLog.setCreatorType(creatorType.code);
            validateLog.setVisibilityFlag(ViewPraiseModel.VISIBILITY_FLAG_ALL);
            validateLog.setContent("[新建]鉴定单");
            validate.setValidateLog(validateLog);
            MSResponse<OrderValidate> msResponse = orderValidateFeign.saveValidate(validate);
            if (!MSResponse.isSuccess(msResponse)) {
                throw new RuntimeException("创建鉴定单失败:" + msResponse.getMsg());
            }
            //将工单挂起
            orderService.suspendOrder(order.getId(), order.getQuarter(), OrderSuspendTypeEnum.VALIDATE, OrderSuspendFlagEnum.SUSPENDED);
            //写工单日志
            OrderProcessLog processLog = new OrderProcessLog();
            processLog.setOrderId(validate.getOrderId());
            processLog.setQuarter(validate.getQuarter());
            processLog.setAction("创建鉴定单");
            processLog.setActionComment(creatorType.label + "创建鉴定单[" + validateNo + "]成功，等待B2B客户确认");
            processLog.setStatus(condition.getStatus().getLabel());
            processLog.setStatusValue(condition.getStatusValue());
            processLog.setStatusFlag(OrderProcessLog.OPL_SF_NOT_CHANGE_STATUS);
            processLog.setCloseFlag(0);
            processLog.setCreateBy(user);
            processLog.setCreateDate(createDate);
            processLog.setCustomerId(condition.getCustomerId());
            processLog.setDataSourceId(order.getDataSourceId());
            processLog.setVisibilityFlag(ViewPraiseModel.VISIBILITY_FLAG_ALL);
            orderService.saveOrderProcessLogWithNoCalcVisibility(processLog);
            //B2B
            b2BCenterOrderService.validateOrder(order, msResponse.getData(), user, createDate);
        } finally {
            if (lockKey != null) {
                redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockKey);
            }
        }
    }

    /**
     * 审核通过
     */
    @Transactional(readOnly = false)
    public void approveOrderValidate(Order order, String remarks, User user, Date updateDate) {
        OrderCondition condition = order.getOrderCondition();
        OrderValidate orderValidate = getLastOrderValidate(order.getId(), order.getQuarter());
        if (OrderSuspendTypeEnum.VALIDATE.getValue() == condition.getSuspendType()
                && OrderSuspendFlagEnum.SUSPENDED.getValue() == condition.getSuspendFlag()
                && orderValidate != null && orderValidate.getStatus() == ValidateStatusEnum.NEW.code) {
            String lockKey = String.format(RedisConstant.SD_ORDER_LOCK, order.getId());
            Boolean locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockKey, 1, ORDER_LOCK_EXPIRED);
            if (!locked) {
                throw new OrderException("此订单正在处理中，请稍候重试。");
            }
            try {
                OrderValidate validate = new OrderValidate();
                validate.setId(orderValidate.getId());
                validate.setStatus(ValidateStatusEnum.APPROVED.code);
                validate.setOrderId(order.getId());
                validate.setQuarter(order.getQuarter());
                validate.setRemarks(StringUtils.toString(remarks));
                validate.setUpdateById(user.getId());
                validate.setUpdateDt(updateDate.getTime());

                OrderValidateLog validateLog = new OrderValidateLog();
                validateLog.setId(sequenceIdService.nextId());
                validateLog.setStatus(ValidateStatusEnum.APPROVED.code);
                validateLog.setActionType(ValidateStatusEnum.APPROVED.code);
                validateLog.setCreatorType(ValidateCreatorTypeEnum.B2B_CUSTOMER.code);
                validateLog.setVisibilityFlag(ViewPraiseModel.VISIBILITY_FLAG_ALL);
                validateLog.setContent(StringUtils.toString(remarks));
                validate.setValidateLog(validateLog);
                MSResponse<Integer> msResponse = orderValidateFeign.approve(validate);
                if (!MSResponse.isSuccessCode(msResponse)) {
                    throw new RuntimeException("处理鉴定单失败:" + msResponse.getMsg());
                }
                //将工单挂起
//                orderService.suspendOrder(order.getId(), order.getQuarter(), OrderSuspendTypeEnum.VALIDATE, OrderSuspendFlagEnum.SUSPENDED);
                //写工单日志
                OrderProcessLog processLog = new OrderProcessLog();
                processLog.setOrderId(validate.getOrderId());
                processLog.setQuarter(validate.getQuarter());
                processLog.setAction("审核鉴定单");
                StrBuilder strBuilder = new StrBuilder();
                strBuilder.append("鉴定单[")
                        .append(orderValidate.getValidateNo())
                        .append("]已被")
                        .append(ValidateCreatorTypeEnum.B2B_CUSTOMER.label)
                        .append("审核通过");
                if (StringUtils.isNotEmpty(remarks)) {
                    strBuilder.append("；备注：").append(remarks);
                }
                processLog.setActionComment(strBuilder.toString());
                processLog.setStatus(condition.getStatus().getLabel());
                processLog.setStatusValue(condition.getStatusValue());
                processLog.setStatusFlag(OrderProcessLog.OPL_SF_NOT_CHANGE_STATUS);
                processLog.setCloseFlag(0);
                processLog.setCreateBy(user);
                processLog.setCreateDate(updateDate);
                processLog.setCustomerId(condition.getCustomerId());
                processLog.setDataSourceId(order.getDataSourceId());
                processLog.setVisibilityFlag(ViewPraiseModel.VISIBILITY_FLAG_ALL);
                orderService.saveOrderProcessLogWithNoCalcVisibility(processLog);
            } finally {
                if (lockKey != null) {
                    redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockKey);
                }
            }
        }
    }

    /**
     * 驳回
     */
    @Transactional(readOnly = false)
    public void rejectOrderValidate(Order order, String remarks, User user, Date updateDate) {
        OrderCondition condition = order.getOrderCondition();
        OrderValidate orderValidate = getLastOrderValidate(order.getId(), order.getQuarter());
        if (OrderSuspendTypeEnum.VALIDATE.getValue() == condition.getSuspendType()
                && OrderSuspendFlagEnum.SUSPENDED.getValue() == condition.getSuspendFlag()
                && orderValidate != null && orderValidate.getStatus() == ValidateStatusEnum.NEW.code) {
            String lockKey = String.format(RedisConstant.SD_ORDER_LOCK, order.getId());
            Boolean locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockKey, 1, ORDER_LOCK_EXPIRED);
            if (!locked) {
                throw new OrderException("此订单正在处理中，请稍候重试。");
            }
            try {
                OrderValidate validate = new OrderValidate();
                validate.setId(orderValidate.getId());
                validate.setStatus(ValidateStatusEnum.REJECTED.code);
                validate.setOrderId(order.getId());
                validate.setQuarter(order.getQuarter());
                validate.setRemarks(StringUtils.toString(remarks));
                validate.setUpdateById(user.getId());
                validate.setUpdateDt(updateDate.getTime());

                OrderValidateLog validateLog = new OrderValidateLog();
                validateLog.setId(sequenceIdService.nextId());
                validateLog.setStatus(ValidateStatusEnum.REJECTED.code);
                validateLog.setActionType(ValidateStatusEnum.REJECTED.code);
                validateLog.setCreatorType(ValidateCreatorTypeEnum.B2B_CUSTOMER.code);
                validateLog.setVisibilityFlag(ViewPraiseModel.VISIBILITY_FLAG_ALL);
                validateLog.setContent(StringUtils.toString(remarks));
                validate.setValidateLog(validateLog);
                MSResponse<Integer> msResponse = orderValidateFeign.reject(validate);
                if (!MSResponse.isSuccessCode(msResponse)) {
                    throw new RuntimeException("驳回鉴定单失败:" + msResponse.getMsg());
                }
                //将工单挂起
                orderService.suspendOrder(order.getId(), order.getQuarter(), OrderSuspendTypeEnum.NONE, OrderSuspendFlagEnum.NORMAL);
                //写工单日志
                OrderProcessLog processLog = new OrderProcessLog();
                processLog.setOrderId(validate.getOrderId());
                processLog.setQuarter(validate.getQuarter());
                processLog.setAction("驳回鉴定单");
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("鉴定单[")
                        .append(orderValidate.getValidateNo())
                        .append("]已被")
                        .append(ValidateCreatorTypeEnum.B2B_CUSTOMER.label)
                        .append("驳回");
                if (StringUtils.isNotEmpty(remarks)) {
                    stringBuilder.append("；驳回原因：")
                            .append(remarks);
                }
                processLog.setActionComment(stringBuilder.toString());
                processLog.setStatus(condition.getStatus().getLabel());
                processLog.setStatusValue(condition.getStatusValue());
                processLog.setStatusFlag(OrderProcessLog.OPL_SF_NOT_CHANGE_STATUS);
                processLog.setCloseFlag(0);
                processLog.setCreateBy(user);
                processLog.setCreateDate(updateDate);
                processLog.setCustomerId(condition.getCustomerId());
                processLog.setDataSourceId(order.getDataSourceId());
                processLog.setVisibilityFlag(ViewPraiseModel.VISIBILITY_FLAG_ALL);
                orderService.saveOrderProcessLogWithNoCalcVisibility(processLog);
            } finally {
                if (lockKey != null) {
                    redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockKey);
                }
            }
        }
    }

    /**
     * 获取工单当前未处理的鉴定单
     */
    public OrderValidate getLastOrderValidate(Long orderId, String quarter) {
        OrderValidate validate = null;
        if (orderId != null && StringUtils.isNotBlank(quarter)) {
            MSResponse<List<OrderValidate>> msResponse = orderValidateFeign.findListByOrderId(orderId, quarter);
            if (!MSResponse.isSuccessCode(msResponse)) {
                throw new RuntimeException("读取鉴定单失败");
            }
            if (ObjectUtil.isNotEmpty(msResponse.getData())) {
                validate = msResponse.getData().stream().sorted(Comparator.comparing(OrderValidate::getId).reversed()).findFirst().orElse(null);
            }
        }
        return validate;
    }

    /**
     * 检查因鉴定而挂起的工单是否有鉴定服务项目
     */
    public boolean hasValidateServiceType(Integer orderServiceType, List<OrderDetail> orderDetails) {
        boolean result = false;
        if (ObjectUtil.isNotEmpty(orderDetails)) {
            Map<Long, String> serviceTypeCodeMap = msServiceTypeService.findIdsAndCodes();
            Set<Long> serviceTypeIdSet = orderDetails.stream().map(i -> i.getServiceType().getId()).collect(Collectors.toSet());
            String serviceTypeCode;
            for (Long id : serviceTypeIdSet) {
                serviceTypeCode = serviceTypeCodeMap.get(id);
                //1:安装单 2:维修单 (数据字典:order_service_type)
                if (orderServiceType == OrderUtils.OrderTypeEnum.INSTALL.getId() && SERVICE_TYPE_CODE_INSTALLATION_VALIDATE.equals(serviceTypeCode)) {
                    result = true;
                    break;
                }
                if (orderServiceType == OrderUtils.OrderTypeEnum.REPAIRE.getId() && SERVICE_TYPE_CODE_MAINTENANCE_VALIDATE.equals(serviceTypeCode)) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * 是否有鉴定单等待审核
     * @param order
     * @return
     */
    public boolean isWaitingApproveOrderValidate(Order order) {
        boolean result = false;
        if (order.isSuspendedForValidate() ==  1) {
            OrderValidate validate = getLastOrderValidate(order.getId(), order.getQuarter());
            if (validate != null && validate.getStatus() != null && validate.getStatus() == ValidateStatusEnum.NEW.code) {
                result = true;
            }
        }
        return result;
    }

}
