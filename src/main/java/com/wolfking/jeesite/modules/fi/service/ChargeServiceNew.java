package com.wolfking.jeesite.modules.fi.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.entity.fi.mq.MQCreateCustomerChargeMessage;
import com.kkl.kklplus.entity.fi.mq.MQCreateServicePointChargeMessage;
import com.wolfking.jeesite.common.config.WebProperties;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.utils.CurrencyUtil;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.wolfking.jeesite.modules.fi.entity.EngineerChargeMaster;
import com.wolfking.jeesite.modules.fi.entity.ServicePointPayableMonthly;
import com.wolfking.jeesite.modules.fi.entity.ServicePointPayableMonthlyDetail;
import com.wolfking.jeesite.modules.md.dao.CustomerFinanceDao;
import com.wolfking.jeesite.modules.md.dao.ServicePointDao;
import com.wolfking.jeesite.modules.md.entity.CustomerFinance;
import com.wolfking.jeesite.modules.md.entity.ServicePointFinance;
import com.wolfking.jeesite.modules.md.entity.ServiceType;
import com.wolfking.jeesite.modules.mq.dto.MQOrderCharge;
import com.wolfking.jeesite.modules.mq.sender.CreateCustomerChargeSender;
import com.wolfking.jeesite.modules.mq.sender.CreateServicePointChargeSender;
import com.wolfking.jeesite.modules.mq.sender.OrderFeeUpdateAfterChargeSender;
import com.wolfking.jeesite.modules.mq.service.ServicePointOrderBusinessService;
import com.wolfking.jeesite.modules.sd.entity.*;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.modules.sd.utils.OrderCacheUtils;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.modules.sys.utils.SeqUtils;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.b2bcenter.sd.service.B2BCenterAsyncTriggerB2BOperationService;
import com.wolfking.jeesite.ms.enums.OrderStatusType;
import com.wolfking.jeesite.ms.providermd.service.DepositLevelService;
import com.wolfking.jeesite.ms.providermd.service.MSServiceTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author: Jeff.Zhao
 * @date: 2018/6/13 11:11
 * @description:
 **/
@Slf4j
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class ChargeServiceNew {

    @Autowired
    private OrderService orderService;
    @Autowired
    private ChargeProcessService chargeProcessService;
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private CreateCustomerChargeSender createCustomerChargeSender;
    @Autowired
    private CreateServicePointChargeSender createServicePointChargeSender;
    @Resource
    private CustomerFinanceDao customerFinanceDao;
    @Resource
    private ServicePointDao servicePointDao;
    @Autowired
    private ServicePointOrderBusinessService servicePointOrderBusinessService;
    @Autowired
    private B2BCenterAsyncTriggerB2BOperationService b2BCenterAsyncTriggerB2BOperationService;
    @Autowired
    private MSServiceTypeService msServiceTypeService;
    @Autowired
    private OrderFeeUpdateAfterChargeSender orderFeeUpdateAfterChargeSender;
    @Autowired
    private CustomerBlockCurrencyService customerBlockCurrencyService;
    @Autowired
    private DepositLevelService depositLevelService;
    @Autowired
    private WebProperties webProperties;

    public void createCharge(Long orderId, Long createById) throws RuntimeException{
        //生成对帐单数据
        boolean isAuto = false;
        User createBy;
        if (createById == null) {
            createBy = UserUtils.getUser();
        } else {
            createBy = new User(createById);
            isAuto = true;
        }

        Date createDate = new Date();
        String createQuarter = QuarterUtils.getSeasonQuarter(createDate);

        //锁
        String lockKey = String.format(RedisConstant.LOCK_CHARGE_KEY, orderId);
        //获得锁60秒
        Boolean locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockKey, createBy.getId(), 60);

        try {
            //收集对帐所需数据
            HashMap<String, Object> resultMap = preCreateCharge(orderId, locked, createById, createBy, createDate, createQuarter);
            //保存对帐信息到数据库
            chargeProcessService.saveCreateCharge(resultMap);
            //对帐后续操作，发队列，删订单缓存
            afterCreateCharge(orderId, resultMap, createBy, isAuto);
        }
        catch (Exception e) {
            LogUtils.saveLog("订单对帐." + (isAuto ? "队列" : "手工"), "FI:ChargeService.create", orderId.toString(), e, createBy);
            throw new RuntimeException(e.getMessage());
        } finally {
            if (locked && lockKey != null) {
                redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockKey);
            }
        }
    }

    private HashMap<String, Object> preCreateCharge(Long orderId, boolean locked, Long createById, User createBy, Date createDate, String createQuarter) throws RuntimeException {
        HashMap<String, Object> returnMap = new HashMap<>();

        Order order = orderService.getOrderById(orderId, "", OrderUtils.OrderDataLevel.DETAIL, true, true);
        if (order == null) {
            throw new RuntimeException("对帐错误,找不到订单,ID:" + String.valueOf(orderId));
        }
        //判断是否有品类信息
        if (order.getOrderCondition().getProductCategoryId() == null || order.getOrderCondition().getProductCategoryId() == 0) {
            throw new RuntimeException(String.format("订单:%s 无品类信息"));
        }
        if (!locked) {
            throw new RuntimeException("订单:" + order.getOrderNo() + "正在对帐中，请稍候重试，或刷新页面。");
        }
        Boolean checkFee = orderService.checkOrderFeeAndServiceAmount(order, false);
        if (checkFee == false) {
            throw new RuntimeException("订单:" + order.getOrderNo() + " 金额与实际上门服务金额不一致");
        }
        //判断是否标记了异常单
        if (order.getOrderCondition().getPendingFlag() <= 1) {
            throw new RuntimeException("订单:" + order.getOrderNo() + " 已经标记异常");
        }
        //判断是否可以生成对帐单
        if (order.getOrderCondition().getChargeFlag() != 0) {
            throw new RuntimeException("订单:" + order.getOrderNo() + " 已经生成对帐单");
        }

        //计算自动对帐标记
        int autoChargeFlag = 0;//手工对帐
        //自动对帐
        if (createById != null) {
            //自动对帐标记 0：否，1：自动客评，对帐，2：自动对帐
            /* commented at 2019/01/22
            autoChargeFlag = order.getOrderCondition().getAutoGradeFlag() != null &&
                    (order.getOrderCondition().getAutoGradeFlag() == 1 || order.getOrderCondition().getAutoGradeFlag() == 3 ) ? 1 : 2;
            */
            Integer gradeFlag = order.getOrderCondition().getGradeFlag();
            autoChargeFlag = gradeFlag != null && (
                    gradeFlag == OrderUtils.OrderGradeType.MESSAGE_GRADE.getValue()
                            ||  gradeFlag == OrderUtils.OrderGradeType.VOICE_GRADE.getValue()
                            ||  gradeFlag == OrderUtils.OrderGradeType.APP_GRADE.getValue()
            ) ? 1 : 2;
        }


        Map<Long, EngineerChargeMaster> engineerChargeMasterMap = new HashMap<>();

        //修改订单对帐标记-只生成数据不做修改
        order.getOrderCondition().setId(order.getId());
        order.getOrderCondition().setChargeFlag(1);
        order.getOrderCondition().setAutoChargeFlag(autoChargeFlag);
        order.getOrderCondition().setStatus(OrderStatusType.toDict(OrderStatusType.CHARGED));
        order.getOrderCondition().setSubStatus(Order.ORDER_SUBSTATUS_CHARGED);
        returnMap.put("orderCondition", order.getOrderCondition());

        //修改订单状态-只生成数据不做修改
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setOrderId(order.getId());
        orderStatus.setChargeBy(createBy);
        orderStatus.setChargeDate(createDate);
        orderStatus.setQuarter(order.getQuarter());
        returnMap.put("orderStatus", orderStatus);

        //更新客户余额-只生成数据不做修改
        CustomerFinance customerFinance = customerFinanceDao.getAmounts(order.getOrderCondition().getCustomer().getId());
        double beforeBalance = customerFinance.getBalance();
        double beforeBlockBalance = customerFinance.getBlockAmount();
        customerFinance.setUpdateBy(createBy);
        customerFinance.setUpdateDate(createDate);
        customerFinance.setBalance(0 - order.getOrderFee().getOrderCharge());
        customerFinance.setBlockAmount(0 - (order.getOrderFee().getExpectCharge() + order.getOrderFee().getBlockedCharge()));
        customerFinance.setTotalAmount(order.getOrderFee().getOrderCharge());
        returnMap.put("customerFinance", customerFinance);

        final long createCustomerChargeMessageId = SeqUtils.NextID();
        final long customerChargeId = SeqUtils.NextIDValue(SeqUtils.TableName.CustomerCharge);
        final long customerInvoiceCurrencyId = SeqUtils.NextIDValue(SeqUtils.TableName.CustomerCurrency);
        final long customerBlockCurrencyId = SeqUtils.NextIDValue(SeqUtils.TableName.ServicePointPayableMonthly);
        //生成客户对帐相关数据
        MQCreateCustomerChargeMessage.CreateCustomerChargeMessage createCustomerChargeMessage = MQCreateCustomerChargeMessage.CreateCustomerChargeMessage.newBuilder()
                .setMessageId(createCustomerChargeMessageId)
                .setCustomerChargeId(customerChargeId)
                .setCustomerInvoiceCurrencyId(customerInvoiceCurrencyId)
                .setCustomerBlockCurrencyId(customerBlockCurrencyId)
                .setOrderId(order.getId())
                .setOrderNo(order.getOrderNo())
                .setCustomerId(order.getOrderCondition().getCustomer().getId())
                .setServiceCharge(order.getOrderFee().getServiceCharge())
                .setExpressCharge(order.getOrderFee().getExpressCharge())
                .setTravelCharge(order.getOrderFee().getTravelCharge())
                .setMaterialCharge(order.getOrderFee().getMaterialCharge())
                .setTimeLinessCharge(order.getOrderFee().getCustomerTimeLinessCharge())
                .setUrgentCharge(order.getOrderFee().getCustomerUrgentCharge())
                .setPraiseFee(order.getOrderFee().getPraiseFee())
                .setOtherCharge(order.getOrderFee().getOtherCharge())
                .setServiceTimes(order.getOrderCondition().getServiceTimes())
                .setPaymentType(Integer.parseInt(order.getOrderFee().getOrderPaymentType().getValue()))
                .setCreateById(createBy.getId())
                .setCreateDate(createDate.getTime())
                .setQuarter(createQuarter)
                .setProductCategoryId(order.getOrderCondition().getProductCategoryId())
                .setProductIds(order.getOrderCondition().getProductIds())
                .setTotalQty(order.getOrderCondition().getTotalQty())
                .setOrderCreateDate(order.getOrderCondition().getCreateDate().getTime())
                .setOrderCloseDate(order.getOrderCondition().getCloseDate().getTime())
                .setServiceTypes(order.getOrderCondition().getServiceTypes())
                .setTimeLiness(order.getOrderFee().getCustomerTimeLiness())
                .setBeforeBalance(beforeBalance)
                .setBalance(beforeBalance - order.getOrderFee().getOrderCharge())
                .setAmount(0 - order.getOrderFee().getOrderCharge())
                .setBeforeBlockBalance(beforeBlockBalance)
                .setBlockBalance(beforeBlockBalance - order.getOrderFee().getBlockedCharge() - order.getOrderFee().getExpectCharge())
                .setBlockAmount(0 - (order.getOrderFee().getBlockedCharge() + order.getOrderFee().getExpectCharge()))
                .setOrderCharge(order.getOrderFee().getOrderCharge())
                .setExpectCharge(order.getOrderFee().getExpectCharge())
                .setBlockedCharge(order.getOrderFee().getBlockedCharge())
                .build();

        //生成网点对帐相关数据
        final long createServicePointChargeMessageId = SeqUtils.NextID();
        MQCreateServicePointChargeMessage.CreateServicePointChargeMessage.Builder createServicePointChargeMessageBuilder = MQCreateServicePointChargeMessage.CreateServicePointChargeMessage.newBuilder();
        createServicePointChargeMessageBuilder.setMessageId(createServicePointChargeMessageId);
        createServicePointChargeMessageBuilder.setOrderId(order.getId());
        createServicePointChargeMessageBuilder.setOrderNo(order.getOrderNo());
        createServicePointChargeMessageBuilder.setQuarter(createQuarter);
        createServicePointChargeMessageBuilder.setCreateById(createBy.getId());
        createServicePointChargeMessageBuilder.setCreateDate(createDate.getTime());
        createServicePointChargeMessageBuilder.setAutoChargeFlag(autoChargeFlag);
        createServicePointChargeMessageBuilder.setOrderCloseDate(order.getOrderCondition().getCloseDate().getTime());
        createServicePointChargeMessageBuilder.setIsUpdateMonthly(false);
        createServicePointChargeMessageBuilder.setCustomerId(order.getOrderCondition().getCustomer().getId());
        createServicePointChargeMessageBuilder.setProductCategoryId(order.getOrderCondition().getProductCategoryId());

        //生成更新订单费用消息头
        MQOrderCharge.OrderFeeUpdateAfterCharge.Builder orderFeeUpdateAfterChargeMessageBuilder = MQOrderCharge.OrderFeeUpdateAfterCharge.newBuilder();
        orderFeeUpdateAfterChargeMessageBuilder.setOrderId(order.getId());
        orderFeeUpdateAfterChargeMessageBuilder.setQuarter(order.getQuarter());
        orderFeeUpdateAfterChargeMessageBuilder.setTriggerBy(createBy.getId());
        orderFeeUpdateAfterChargeMessageBuilder.setTriggerDate(createDate.getTime());

        //根据上门服务明细汇总数据
        //1.根据上门服务明细汇总按网点汇总数据(charge master),服务/快递/远程/配件/其他费
        //2.每一条服务明细生成对应的网点对帐数据,服务/快递/远程/配件/其他费

        // 从数据库读取 余额 与 扣点开关 与 扣点点数 与 总质保金额
        // 1. 用于计算扣点，平台费点
        // 2. 用于计算质保金有没有扣满
        List<Long> servicePointIds = order.getDetailList().stream().map(orderDetail -> orderDetail.getServicePoint().getId()).distinct().collect(Collectors.toList());
        Map<Long, ServicePointFinance> servicePointFinanceMap = Maps.newHashMap();
        if ( (!ObjectUtils.isEmpty(servicePointIds))) {
            servicePointFinanceMap = servicePointDao.getBalanceAndDiscountAndDeposit(servicePointIds);
        }
        //从微服务获取服务类型对应的扣点开关与信息费开关-用于计算扣点，平台费点
        List<Long> serviceTypeIds = order.getDetailList().stream().map(orderDetail -> orderDetail.getServiceType().getId()).distinct().collect(Collectors.toList());
        Map<Long, ServiceType> serviceTypeMap = msServiceTypeService.findTaxAndInfoFlagMapByIdsForFI(serviceTypeIds);
        //TODO 添加微服务调用异常处理办法
        if (servicePointFinanceMap.size() > 0 && serviceTypeMap.size() == 0) {
            throw new RuntimeException("服务类型读取失败，请重试。");
        }
        //扣点点数，平台费点
        double taxFeeRate;
        double infoFeeRate;
        double currentDeposit;
        //从微服务获取网点对应的质保金等级-用于计算此次结算扣多少质保金
        Map<Long, Map<String, Object>> depositLevelMap = new HashMap<>();
        //TODO 正式上线稳定后去掉此开关与此处代码
        if (webProperties.getServicePoint().getDepositEnabled()) {
            depositLevelMap = depositLevelService.getDepositLevelByServicePointIdsForFI(servicePointIds);
        } else {
            for (Long id : servicePointIds) {
                depositLevelMap.put(id, null);
            }
        }
        if (servicePointFinanceMap.size() != depositLevelMap.size()) {
            throw new RuntimeException("质保等级读取失败，请重试。");
        }

        int index = 1;
        for (OrderDetail orderDetail :  order.getDetailList()) {
            taxFeeRate = 0;
            infoFeeRate = 0;
            currentDeposit = 0;
            Long engineerChargeId = SeqUtils.NextIDValue(SeqUtils.TableName.EngineerCharge);
            engineerChargeId = engineerChargeId + index;

            //计算扣点，平台费点
            ServicePointFinance finance = servicePointFinanceMap.get(orderDetail.getServicePoint().getId());
            ServiceType type = serviceTypeMap.get(orderDetail.getServiceType().getId());
            if (type.getTaxFeeFlag() == 1 && finance.getDiscountFlag() == 1) {
                taxFeeRate = finance.getDiscount();
            }
            if (type.getInfoFeeFlag() == 1) {
                infoFeeRate = CurrencyUtil.infoFeeRate;
            }

            //汇总网点数据
            if (engineerChargeMasterMap.containsKey(orderDetail.getServicePoint().getId())) {
                EngineerChargeMaster chargeMaster = engineerChargeMasterMap.get(orderDetail.getServicePoint().getId());
                chargeMaster.setServiceCharge(chargeMaster.getServiceCharge() + orderDetail.getEngineerServiceCharge());
                chargeMaster.setExpressCharge(chargeMaster.getExpressCharge() + orderDetail.getEngineerExpressCharge());
                chargeMaster.setTravelCharge(chargeMaster.getTravelCharge() + orderDetail.getEngineerTravelCharge());
                chargeMaster.setMaterialCharge(chargeMaster.getMaterialCharge() + orderDetail.getEngineerMaterialCharge());
                chargeMaster.setOtherCharge(chargeMaster.getOtherCharge() + orderDetail.getEngineerOtherCharge());
            } else {
                //计算质保金扣减金额
                //1. 服务类型的开关为开
                //2. 网点有设定质保金等级
                //3. 当前未超质保金上限
                Map<String, Object> depositInfoMap = depositLevelMap.get(orderDetail.getServicePoint().getId());
                if (depositInfoMap != null) {
                    double depositFromOrderFlag = Double.parseDouble(depositInfoMap.get("depositFromOrderFlag").toString());
                    //服务类型质保金开关为开,网点从工单扣除质保金的开关为开
                    if (type.getDepositFlag() == 1 && depositFromOrderFlag == 1) {
                        double maxAmount = Double.parseDouble(depositInfoMap.get("maxAmount").toString());
                        double deductPerOrder = Double.parseDouble(depositInfoMap.get("deductPerOrder").toString());
                        //获取上限以及每单扣除金额，计算此单扣除金额
                        if (maxAmount > 0 && deductPerOrder > 0 &&
                                finance.getDeposit() < maxAmount) {
                            currentDeposit = maxAmount - finance.getDeposit() > deductPerOrder ?
                                    deductPerOrder : maxAmount - finance.getDeposit();
                        }
                    }
                }

                EngineerChargeMaster chargeMaster = new EngineerChargeMaster();
                chargeMaster.setServicePoint(orderDetail.getServicePoint());
                chargeMaster.setServiceCharge(orderDetail.getEngineerServiceCharge());
                chargeMaster.setExpressCharge(orderDetail.getEngineerExpressCharge());
                chargeMaster.setTravelCharge(orderDetail.getEngineerTravelCharge());
                chargeMaster.setMaterialCharge(orderDetail.getEngineerMaterialCharge());
                chargeMaster.setOtherCharge(orderDetail.getEngineerOtherCharge());
                chargeMaster.setPaymentType(Integer.parseInt(orderDetail.getEngineerPaymentType().getValue()));
                chargeMaster.setTaxFeeRate(taxFeeRate);
                chargeMaster.setInfoFeeRate(infoFeeRate);
                chargeMaster.setDeposit(currentDeposit);
                engineerChargeMasterMap.put(orderDetail.getServicePoint().getId(), chargeMaster);
            }

            MQCreateServicePointChargeMessage.CreateEngineerCharge createEngineerCharge = MQCreateServicePointChargeMessage.CreateEngineerCharge.newBuilder()
                    .setEngineerChargeId(engineerChargeId)
                    .setOrderDetailId(orderDetail.getId())
                    .setServicePointId(orderDetail.getServicePoint().getId())
                    .setEngineerId(orderDetail.getEngineer().getId())
                    .setProductId(orderDetail.getProductId())
                    .setServiceTypeId(orderDetail.getServiceType().getId())
                    .setQty(orderDetail.getQty())
                    .setServiceCharge(orderDetail.getEngineerServiceCharge())
                    .setExpressCharge(orderDetail.getEngineerExpressCharge())
                    .setTravelCharge(orderDetail.getEngineerTravelCharge())
                    .setMaterialCharge(orderDetail.getEngineerMaterialCharge())
                    .setOtherCharge(orderDetail.getEngineerOtherCharge())
                    .setServiceTimes(orderDetail.getServiceTimes())
                    .setPaymentType(Integer.parseInt(orderDetail.getEngineerPaymentType().getValue()))
                    .setTaxFeeRate(taxFeeRate)
                    .setInfoFeeRate(infoFeeRate)
                    .setProductCategoryId(orderDetail.getProduct().getCategory().getId())
                    .build();

            createServicePointChargeMessageBuilder.addCreateEngineerCharge(createEngineerCharge);

            index++;
        }

        //获取网点费用汇总,快可立时效/客户时效/保险/加急费/好评费
        Map<Long, OrderServicePointFee> servicePointFeeMap = orderService.getOrderServicePointFeeMapsForCharge(order.getId(), order.getQuarter());
        OrderServicePointFee servicePointFee;
        List<ServicePointFinance> servicePointFinanceList = Lists.newArrayListWithCapacity(engineerChargeMasterMap.size());
        List<ServicePointPayableMonthly> payableMonthlyList = Lists.newArrayListWithCapacity(engineerChargeMasterMap.size());
        List<ServicePointPayableMonthlyDetail> payableMonthlyDetailList = Lists.newArrayListWithCapacity(engineerChargeMasterMap.size());
        index = 1;
        for (Map.Entry entry : engineerChargeMasterMap.entrySet()) {
            //网点汇总数据
            EngineerChargeMaster masterTotalData = (EngineerChargeMaster) entry.getValue();
            //获取网点汇总信息
            if (servicePointFeeMap.containsKey(entry.getKey())) {
                servicePointFee = servicePointFeeMap.get(entry.getKey());
            } else {
                servicePointFee = null;
            }

            //保险费
            double insuranceCharge = servicePointFee != null ? servicePointFee.getInsuranceCharge() : 0;
            //客户时效费
            double customerTimeLinessCharge = servicePointFee != null ? servicePointFee.getCustomerTimeLinessCharge() : 0;
            //快可立时效费
            double timeLinessCharge = servicePointFee != null ? servicePointFee.getTimeLinessCharge() : 0;
            //加急费
            double urgentCharge = servicePointFee != null ? servicePointFee.getUrgentCharge() : 0;
            //好评费
            double praiseFee = servicePointFee != null ? servicePointFee.getPraiseFee() : 0;

            //生成网点资金异动流水
            //计算费用汇总 服务/快递/远程/配件保险/客户时效/快可立时效/加急/好评/其他费
            ServicePointFinance servicePointFinance = servicePointFinanceMap.get(masterTotalData.getServicePoint().getId());
            double servicePointBeforeBalance = servicePointFinance.getBalance();
            double servicePointBeforeDeposit = servicePointFinance.getDeposit();
            Double engineerTotalCharge = masterTotalData.getServiceCharge() +
                    masterTotalData.getExpressCharge() +
                    masterTotalData.getTravelCharge() +
                    masterTotalData.getMaterialCharge() +
                    // 逻辑调整2021/3/2,计算平台费，扣点后，再扣互助基金 insuranceCharge +
                    customerTimeLinessCharge +
                    timeLinessCharge +
                    urgentCharge +
                    praiseFee +
                    masterTotalData.getOtherCharge();

            //信息费
            double infoFee = 0 - CurrencyUtil.round2(engineerTotalCharge * masterTotalData.getInfoFeeRate());
            //扣点
            double taxFee = 0 - CurrencyUtil.round2(engineerTotalCharge * masterTotalData.getTaxFeeRate());
            //质保金
            double deposit = 0 - masterTotalData.getDeposit();
            //重新汇总余额 - 汇总服务/快递/远程/配件/客户时效/快可立时效/加急/好评/其他费 + 扣点/信息费 + 质保金（正值）+ 互助基金（负值）
            engineerTotalCharge = engineerTotalCharge + infoFee + taxFee + deposit + insuranceCharge;
            //更新网点余额
            servicePointFinance.setBalance(engineerTotalCharge);
            //累计即结款
            if (masterTotalData.getPaymentType() == 20) {
                servicePointFinance.setDailyBalance(engineerTotalCharge);
            }
            //累计信息费
            servicePointFinance.setInfoFee(infoFee);
            //累计扣点
            servicePointFinance.setTaxFee(taxFee);
            //累计总质保金
            servicePointFinance.setDeposit(0-deposit);
            //累计工单扣除的质保金
            servicePointFinanceList.add(servicePointFinance);

            //累计网点应付款
            ServicePointPayableMonthly servicePointPayableMonthly = new ServicePointPayableMonthly();
            servicePointPayableMonthly.setServicePoint(masterTotalData.getServicePoint());
            servicePointPayableMonthly.setPaymentType(masterTotalData.getPaymentType());
            servicePointPayableMonthly.setYear(DateUtils.getYear(createDate));
            servicePointPayableMonthly.setMonth(DateUtils.getMonth(createDate));
            servicePointPayableMonthly.setId((servicePointPayableMonthly.getServicePoint().getId() * 10000 + servicePointPayableMonthly.getYear()) * 100 + servicePointPayableMonthly.getPaymentType());
            servicePointPayableMonthly.setAmount(engineerTotalCharge);
            payableMonthlyList.add(servicePointPayableMonthly);

            //累计网点应付款-按品类区分
            ServicePointPayableMonthlyDetail payableDetail = new ServicePointPayableMonthlyDetail();
            payableDetail.setTotalId(servicePointPayableMonthly.getId());
            payableDetail.setServicePoint(masterTotalData.getServicePoint());
            payableDetail.setPaymentType(masterTotalData.getPaymentType());
            payableDetail.setYear(DateUtils.getYear(createDate));
            payableDetail.setMonth(DateUtils.getMonth(createDate));
            payableDetail.setProductCategoryId(order.getOrderCondition().getProductCategoryId());
            payableDetail.setId((payableDetail.getServicePoint().getId() * 10000 + payableDetail.getYear()) * 100 + payableDetail.getPaymentType() + payableDetail.getProductCategoryId());
            payableDetail.setAmount(engineerTotalCharge);
            payableMonthlyDetailList.add(payableDetail);

            //生成服务网点对帐信息
            Long engineerChargeMasterId = SeqUtils.NextIDValue(SeqUtils.TableName.EngineerChargeMaster);
            engineerChargeMasterId = engineerChargeMasterId + index;
            MQCreateServicePointChargeMessage.CreateEngineerChargeMaster createEngineerChargeMaster = MQCreateServicePointChargeMessage.CreateEngineerChargeMaster.newBuilder()
                    .setEngineerChargeMasterId(engineerChargeMasterId)
                    .setServicePointId(masterTotalData.getServicePoint().getId())
                    .setServiceCharge(masterTotalData.getServiceCharge())
                    .setExpressCharge(masterTotalData.getExpressCharge())
                    .setTravelCharge(masterTotalData.getTravelCharge())
                    .setMaterialCharge(masterTotalData.getMaterialCharge())
                    .setInsuranceCharge(insuranceCharge)
                    .setCustomerTimeLinessCharge(customerTimeLinessCharge)
                    .setTimeLinessCharge(timeLinessCharge)
                    .setUrgentCharge(urgentCharge)
                    .setPraiseFee(praiseFee)
                    .setInfoFee(infoFee)
                    .setTaxFee(taxFee)
                    .setDeposit(deposit)
                    .setOtherCharge(masterTotalData.getOtherCharge())
                    .setInsuranceNo(servicePointFee != null ? servicePointFee.getInsuranceNo() : "")
                    .setPaymentType(masterTotalData.getPaymentType())
                    .setTaxFeeRate(masterTotalData.getTaxFeeRate())
                    .setInfoFeeRate(masterTotalData.getInfoFeeRate())
                    .setCustomerTimeLiness(servicePointFee != null ? servicePointFee.getCustomerTimeLinessCharge() : 0)
                    .setTimeLiness(servicePointFee != null ? servicePointFee.getTimeLinessCharge() : 0)
                    .setBeforeBalance(servicePointBeforeBalance)
                    .setBalance(servicePointBeforeBalance + engineerTotalCharge)
                    .setAmount(engineerTotalCharge)
                    .setBeforeDeposit(servicePointBeforeDeposit)
                    .setDepositAmount(0-deposit)
                    .build();

            createServicePointChargeMessageBuilder.addCreateEngineerChargeMaster(createEngineerChargeMaster);

            //生成更新工单费用消息体
            if (infoFee != 0 || taxFee != 0) {
                MQOrderCharge.FeeUpdateItem feeUpdateItem = MQOrderCharge.FeeUpdateItem.newBuilder()
                        .setServicePointId(masterTotalData.getServicePoint().getId())
                        .setTaxFee(taxFee)
                        .setInfoFee(infoFee)
                        .setDeposit(deposit)
                        .build();

                orderFeeUpdateAfterChargeMessageBuilder.addItems(feeUpdateItem);
            }

            index++;
        }
        returnMap.put("servicePointFinanceList", servicePointFinanceList);
        returnMap.put("payableMonthlyList", payableMonthlyList);
        returnMap.put("payableMonthlyDetailList", payableMonthlyDetailList);
        //生成网点对帐消息
        MQCreateServicePointChargeMessage.CreateServicePointChargeMessage createServicePointChargeMessage = createServicePointChargeMessageBuilder.build();
        returnMap.put("createCustomerChargeMessage", createCustomerChargeMessage);
        returnMap.put("createServicePointChargeMessage", createServicePointChargeMessage);
        //生成更新工单费用消息
        MQOrderCharge.OrderFeeUpdateAfterCharge orderFeeUpdateAfterChargeMessage = orderFeeUpdateAfterChargeMessageBuilder.build();
        returnMap.put("orderFeeUpdateAfterChargeMessage", orderFeeUpdateAfterChargeMessage);

        return returnMap;
    }

    private void afterCreateCharge(Long orderId, HashMap<String, Object> hashMap, User createBy, boolean isAuto){
        //发送生成客户对帐消息
        MQCreateCustomerChargeMessage.CreateCustomerChargeMessage createCustomerChargeMessage =
                (MQCreateCustomerChargeMessage.CreateCustomerChargeMessage) hashMap.get("createCustomerChargeMessage");
        try {
            createCustomerChargeSender.send(createCustomerChargeMessage);
        } catch (Exception e) {
            LogUtils.saveLog("订单对帐." + (isAuto ? "队列" : "手工"), "FI:ChargeService.create.customerCharge", new JsonFormat().printToString(createCustomerChargeMessage), e, createBy);
        }
        //发送生成网点对帐消息
        MQCreateServicePointChargeMessage.CreateServicePointChargeMessage createServicePointChargeMessage =
                (MQCreateServicePointChargeMessage.CreateServicePointChargeMessage) hashMap.get("createServicePointChargeMessage");
        try {
            createServicePointChargeSender.send(createServicePointChargeMessage);
        } catch (Exception e) {
            LogUtils.saveLog("订单对帐." + (isAuto ? "队列" : "手工"), "FI:ChargeService.create.servicePointCharge", new JsonFormat().printToString(createServicePointChargeMessage), e, createBy);
        }
        //发送更新工单费用消息
        MQOrderCharge.OrderFeeUpdateAfterCharge orderFeeUpdateAfterChargeMessage =
                (MQOrderCharge.OrderFeeUpdateAfterCharge) hashMap.get("orderFeeUpdateAfterChargeMessage");
        if (orderFeeUpdateAfterChargeMessage.getItemsCount() > 0) {
            try {
                orderFeeUpdateAfterChargeSender.sendDelay(orderFeeUpdateAfterChargeMessage,5000,1);
            } catch (Exception e) {
                LogUtils.saveLog("订单对帐:" + (isAuto ? "队列" : "手工"), "FI:ChargeService.create.updateOrderFee", new JsonFormat().printToString(orderFeeUpdateAfterChargeMessage), e, createBy);
            }
        }

        //region 网点订单数据更新 2019-03-25
        OrderCondition orderCondition = (OrderCondition) hashMap.get("orderCondition");
        if (orderCondition != null) {
            servicePointOrderBusinessService.orderCharge(orderId, orderCondition.getQuarter(),Order.ORDER_STATUS_CHARGED, Order.ORDER_SUBSTATUS_CHARGED, createBy.getId(),createServicePointChargeMessage.getCreateDate());
        }
        //endregion

        //触发B2B的对账操作
        b2BCenterAsyncTriggerB2BOperationService.triggerOrderChargeOperation(createCustomerChargeMessage.getCustomerId(), orderId, orderCondition != null ? orderCondition.getQuarter() : "", createBy.getId(), createCustomerChargeMessage.getCreateDate());

        //TODO: 切分冻结流水
        customerBlockCurrencyService.saveOrderChargedBlockCurrency(createCustomerChargeMessage);

        //删除订单缓存
        OrderCacheUtils.delete(orderId);
    }
}
