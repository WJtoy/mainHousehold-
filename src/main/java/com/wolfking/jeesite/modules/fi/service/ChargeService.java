package com.wolfking.jeesite.modules.fi.service;

import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.service.LongIDCrudService;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.wolfking.jeesite.modules.fi.dao.*;
import com.wolfking.jeesite.modules.fi.entity.*;
import com.wolfking.jeesite.modules.md.dao.CustomerFinanceDao;
import com.wolfking.jeesite.modules.md.dao.ServicePointDao;
import com.wolfking.jeesite.modules.md.entity.*;
import com.wolfking.jeesite.modules.md.service.ServicePointFinanceService;
import com.wolfking.jeesite.modules.md.service.ServicePointService;
import com.wolfking.jeesite.modules.rpt.dao.ServicePointBalanceMonthlyDao;
import com.wolfking.jeesite.modules.sd.dao.OrderDao;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.entity.OrderDetail;
import com.wolfking.jeesite.modules.sd.entity.OrderServicePointFee;
import com.wolfking.jeesite.modules.sd.entity.OrderStatus;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.modules.sd.utils.OrderCacheUtils;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.modules.sys.utils.SeqUtils;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.enums.OrderStatusType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jeff on 2017/4/20.
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class ChargeService extends LongIDCrudService<CustomerChargeDao, CustomerCharge> {

    @Resource
    private CustomerChargeDao customerChargeDao;
    @Resource
    private CustomerChargeConditionDao customerChargeConditionDao;
    @Resource
    private CustomerFinanceDao customerFinanceDao;
    @Resource
    private CustomerCurrencyDao customerCurrencyDao;
    @Resource
    private EngineerChargeDao engineerChargeDao;
    @Resource
    private EngineerChargeConditionDao engineerChargeConditionDao;
    @Resource
    private EngineerChargeMasterDao engineerChargeMasterDao;
    @Resource
    private EngineerChargeMasterConditionDao engineerChargeMasterConditionDao;
    @Resource
    private ServicePointDao servicePointDao;
    @Resource
    private EngineerCurrencyDao engineerCurrencyDao;
    @Autowired
    private OrderService orderService;
    @Autowired
    private RedisUtils redisUtils;
    @Resource
    private OrderDao orderDao;
    @Resource
    private ServicePointPayableMonthlyDao servicePointPayableMonthlyDao;
    @Autowired
    private ServicePointService servicePointService;
    @Resource
    private ServicePointBalanceMonthlyDao servicePointBalanceMonthlyDao;

    @Autowired
    private ServicePointFinanceService servicePointFinanceService;

    @Transactional()
    public void createDeleted(Long orderId, Long createById) throws RuntimeException {
        // 生成对帐单数据
        boolean isAuto = false;
        User createBy;
        if (createById == null){
            createBy = UserUtils.getUser();
        } else {
            createBy = new User(createById);
            isAuto = true;
        }
        //锁
        String lockKey = String.format(RedisConstant.LOCK_CHARGE_KEY, orderId);
        //获得锁
        Boolean locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockKey, createBy.getId(), 60);//60秒
        try {
            Date createDate = new Date();
            CustomerCharge customerCharge;
            CustomerChargeCondition customerChargeCondition;
            EngineerCharge engineerCharge;
            EngineerChargeCondition engineerChargeCondition;
            EngineerChargeMaster engineerChargeMaster;
            EngineerChargeMasterCondition engineerChargeMasterCondition;
            Order order;
            OrderStatus orderStatus;
            CustomerCurrency customerCurrency;
            CustomerFinance customerFinance;
            EngineerCurrency engineerCurrency;
            ServicePointFinance servicePointFinance;
            ServicePointPayableMonthly servicePointPayableMonthly;
            Map<Long, EngineerChargeMaster> engineerChargeMasterMap = new HashMap<>();
            OrderServicePointFee servicePointFee;

            order = orderService.getOrderById(orderId, "", OrderUtils.OrderDataLevel.DETAIL, true, true);
            if (order == null) {
                throw new RuntimeException("对帐错误,找不到订单,ID:" + String.valueOf(orderId));
            }
            if(!locked){
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

            //生成客户对帐信息
            Long customerChargeId = SeqUtils.NextIDValue(SeqUtils.TableName.CustomerCharge);
            customerCharge = new CustomerCharge();
            customerCharge.setId(customerChargeId);
            customerCharge.setOrderId(order.getId());
            customerCharge.setOrderNo(order.getOrderNo());
            customerCharge.setCustomer(new Customer(order.getOrderCondition().getCustomer().getId()));
            customerCharge.setServiceCharge(order.getOrderFee().getServiceCharge());
            customerCharge.setExpressCharge(order.getOrderFee().getExpressCharge());
            customerCharge.setTravelCharge(order.getOrderFee().getTravelCharge());
            customerCharge.setMaterialCharge(order.getOrderFee().getMaterialCharge());
            customerCharge.setTimeLinessCharge(order.getOrderFee().getCustomerTimeLinessCharge());
            customerCharge.setUrgentCharge(order.getOrderFee().getCustomerUrgentCharge());
            customerCharge.setOtherCharge(order.getOrderFee().getOtherCharge());
            customerCharge.setServiceTimes(order.getOrderCondition().getServiceTimes());
            customerCharge.setPaymentType(Integer.parseInt(order.getOrderFee().getOrderPaymentType().getValue()));
            customerCharge.setChargeOrderType(CustomerCharge.CC_TYPE_ORIGINAL);
            customerCharge.setStatus(CustomerCharge.CC_STATUS_CLOSED);
            customerCharge.setCreateBy(createBy);
            customerCharge.setCreateDate(createDate);
            customerCharge.setQuarter(QuarterUtils.getSeasonQuarter(createDate));
            customerChargeDao.insert(customerCharge);

            //生成客户对帐查询信息
            customerChargeCondition = new CustomerChargeCondition();
            customerChargeCondition.setId(customerCharge.getId());
            customerChargeCondition.setOrderId(customerCharge.getOrderId());
            customerChargeCondition.setOrderNo(customerCharge.getOrderNo());
            customerChargeCondition.setCustomerId(customerCharge.getCustomer().getId());
            customerChargeCondition.setProductCategoryId(order.getOrderCondition().getProductCategoryId());
            customerChargeCondition.setProductIds(order.getOrderCondition().getProductIds());
            customerChargeCondition.setServiceTimes(customerCharge.getServiceTimes());
            customerChargeCondition.setPaymentType(customerCharge.getPaymentType());
            customerChargeCondition.setChargeOrderType(customerCharge.getChargeOrderType());
            customerChargeCondition.setCreateDate(customerCharge.getCreateDate());
            customerChargeCondition.setTotalQty(order.getOrderCondition().getTotalQty());
            customerChargeCondition.setOrderCreateDate(order.getOrderCondition().getCreateDate());
            customerChargeCondition.setOrderCloseDate(order.getOrderCondition().getCloseDate());
            customerChargeCondition.setServiceTypes(order.getOrderCondition().getServiceTypes());
            customerChargeCondition.setTimeLiness(order.getOrderFee().getCustomerTimeLiness());
            customerChargeCondition.setStatus(CustomerCharge.CC_STATUS_CLOSED);
            customerChargeCondition.setQuarter(customerCharge.getQuarter());
            customerChargeConditionDao.insert(customerChargeCondition);

            //生成客户资金异动流水
            customerFinance = customerFinanceDao.getAmounts(customerCharge.getCustomer().getId());
            customerCurrency = new CustomerCurrency();
            customerCurrency.setId(SeqUtils.NextIDValue(SeqUtils.TableName.CustomerCurrency));
            customerCurrency.setCustomer(customerCharge.getCustomer());
            customerCurrency.setCurrencyType(CustomerCurrency.CURRENCY_TYPE_OUT);
            customerCurrency.setCurrencyNo(customerCharge.getOrderNo());
            customerCurrency.setBeforeBalance(customerFinance.getBalance());
            customerCurrency.setBalance(customerCurrency.getBeforeBalance() - order.getOrderFee().getOrderCharge());
            customerCurrency.setAmount(0 - order.getOrderFee().getOrderCharge());
            customerCurrency.setPaymentType(CustomerCurrency.PAYMENT_TYPE_TRANSFER_ACCOUNT);
            customerCurrency.setActionType(CustomerCurrency.ACTION_TYPE_ORDERCHARGE);
            StringBuilder stringBuilder = new StringBuilder();
            if (customerCharge.getTimeLinessCharge() != 0) {
                stringBuilder.append(" (时效:").append(customerCharge.getTimeLinessCharge()).append(")");
            }
            if (customerCharge.getUrgentCharge() != 0) {
                stringBuilder.append(" (加急:").append(customerCharge.getUrgentCharge()).append(")");
            }
            customerCurrency.setRemarks("结帐扣款:".concat(order.getOrderFee().getOrderCharge().toString())
                    .concat(" .下单金额:")
                    .concat(order.getOrderFee().getExpectCharge().toString())
                    .concat(" ,冻结金额:")
                    .concat(order.getOrderFee().getBlockedCharge().toString())
                    .concat(" ,实际应收:")
                    .concat(order.getOrderFee().getOrderCharge().toString())
                    .concat(stringBuilder.toString())
                    .concat(" ,订单编号:")
                    .concat(customerCharge.getOrderNo()));
            customerCurrency.setCreateBy(createBy);
            customerCurrency.setCreateDate(createDate);
            customerCurrency.setQuarter(customerCharge.getQuarter());
            customerCurrencyDao.insert(customerCurrency);

            //更新客户余额
            double beforeBlockAmount = customerFinance.getBlockAmount();
            customerFinance.setUpdateBy(createBy);
            customerFinance.setUpdateDate(createDate);
            customerFinance.setBalance(0 - order.getOrderFee().getOrderCharge());
            customerFinanceDao.updateBalanceFromInvoice(customerFinance);
            customerFinance.setBlockAmount(0 - (order.getOrderFee().getExpectCharge() + order.getOrderFee().getBlockedCharge()));
            customerFinance.setTotalAmount(order.getOrderFee().getOrderCharge());
            customerFinanceDao.updateAmountFromInvoice(customerFinance);

            customerCurrency = new CustomerCurrency();
            customerCurrency.setId(SeqUtils.NextIDValue(SeqUtils.TableName.CustomerCurrency));
            customerCurrency.setCustomer(customerCharge.getCustomer());
            customerCurrency.setCurrencyType(CustomerCurrency.CURRENCY_TYPE_NONE);
            customerCurrency.setCurrencyNo(customerCharge.getOrderNo());
            customerCurrency.setBeforeBalance(beforeBlockAmount);//冻结金额
            customerCurrency.setBalance(customerCurrency.getBeforeBalance() - order.getOrderFee().getBlockedCharge() - order.getOrderFee().getExpectCharge());
            customerCurrency.setAmount(0 - (order.getOrderFee().getBlockedCharge() + order.getOrderFee().getExpectCharge()));
            customerCurrency.setCurrencyType(CustomerCurrency.CURRENCY_TYPE_OUT);
            customerCurrency.setPaymentType(CustomerCurrency.PAYMENT_TYPE_CASH);
            customerCurrency.setActionType(CustomerCurrency.ACTION_TYPE_BLOCK);
            customerCurrency.setRemarks(String.format("订单结账 %.2f元,相关单号为 %s", (order.getOrderFee().getBlockedCharge() + order.getOrderFee().getExpectCharge()), customerCharge.getOrderNo()));
            customerCurrency.setCreateBy(createBy);
            customerCurrency.setCreateDate(createDate);
            customerCurrency.setQuarter(customerCharge.getQuarter());
            customerCurrencyDao.insert(customerCurrency);

            //生成对帐明细
            for (OrderDetail orderDetail : order.getDetailList()) {
                //生成服务网点对帐信息
                engineerCharge = new EngineerCharge();
                Long engineerChargeId = SeqUtils.NextIDValue(SeqUtils.TableName.EngineerCharge);
                engineerCharge.setId(engineerChargeId);
                engineerCharge.setOrderId(customerCharge.getOrderId());
                engineerCharge.setOrderNo(customerCharge.getOrderNo());
                engineerCharge.setOrderDetailId(orderDetail.getId());
                engineerCharge.setServicePoint(new ServicePoint(orderDetail.getServicePoint().getId()));
                engineerCharge.setEngineer(new Engineer(orderDetail.getEngineer().getId()));
                engineerCharge.setProduct(orderDetail.getProduct());
                engineerCharge.setServiceType(orderDetail.getServiceType());
                engineerCharge.setQty(orderDetail.getQty());
                engineerCharge.setServiceCharge(orderDetail.getEngineerServiceCharge());
                engineerCharge.setExpressCharge(orderDetail.getEngineerExpressCharge());
                engineerCharge.setTravelCharge(orderDetail.getEngineerTravelCharge());
                engineerCharge.setMaterialCharge(orderDetail.getEngineerMaterialCharge());
                engineerCharge.setOtherCharge(orderDetail.getEngineerOtherCharge());
                engineerCharge.setServiceTimes(orderDetail.getServiceTimes());
                engineerCharge.setPaymentType(Integer.parseInt(orderDetail.getEngineerPaymentType().getValue()));
                engineerCharge.setChargeOrderType(EngineerCharge.EC_TYPE_ORIGINAL);
                engineerCharge.setStatus(EngineerCharge.EC_STATUS_CLOSED);
                engineerCharge.setCreateBy(createBy);
                engineerCharge.setCreateDate(createDate);
                engineerCharge.setQuarter(customerCharge.getQuarter());
                engineerChargeDao.insert(engineerCharge);

                //汇总网点数据
                if (engineerChargeMasterMap.containsKey(engineerCharge.getServicePoint().getId())){
                    EngineerChargeMaster chargeMaster = engineerChargeMasterMap.get(engineerCharge.getServicePoint().getId());
                    chargeMaster.setServiceCharge(chargeMaster.getServiceCharge() + engineerCharge.getServiceCharge());
                    chargeMaster.setExpressCharge(chargeMaster.getExpressCharge() + engineerCharge.getExpressCharge());
                    chargeMaster.setTravelCharge(chargeMaster.getTravelCharge() + engineerCharge.getTravelCharge());
                    chargeMaster.setMaterialCharge(chargeMaster.getMaterialCharge() + engineerCharge.getMaterialCharge());
                    chargeMaster.setOtherCharge(chargeMaster.getOtherCharge() + engineerCharge.getOtherCharge());
                } else {
                    EngineerChargeMaster chargeMaster = new EngineerChargeMaster();
                    chargeMaster.setServicePoint(engineerCharge.getServicePoint());
                    chargeMaster.setServiceCharge(engineerCharge.getServiceCharge());
                    chargeMaster.setExpressCharge(engineerCharge.getExpressCharge());
                    chargeMaster.setTravelCharge(engineerCharge.getTravelCharge());
                    chargeMaster.setMaterialCharge(engineerCharge.getMaterialCharge());
                    chargeMaster.setOtherCharge(engineerCharge.getOtherCharge());
                    chargeMaster.setPaymentType(engineerCharge.getPaymentType());
                    engineerChargeMasterMap.put(engineerCharge.getServicePoint().getId(), chargeMaster);
                }

                //生成服务网点对帐查询信息
                engineerChargeCondition = new EngineerChargeCondition();
                engineerChargeCondition.setId(engineerCharge.getId());
                engineerChargeCondition.setOrderId(customerCharge.getOrderId());
                engineerChargeCondition.setOrderNo(customerCharge.getOrderNo());
                engineerChargeCondition.setServicePointId(orderDetail.getServicePoint().getId());
                engineerChargeCondition.setEngineerId(orderDetail.getEngineer().getId());
                engineerChargeCondition.setProductCategoryId(orderDetail.getProduct().getCategory().getId());
                engineerChargeCondition.setProductId(orderDetail.getProduct().getId());
                engineerChargeCondition.setServiceTypeId(orderDetail.getServiceType().getId());
                engineerChargeCondition.setPaymentType(engineerCharge.getPaymentType());
                engineerChargeCondition.setChargeOrderType(EngineerCharge.EC_TYPE_ORIGINAL);
                engineerChargeCondition.setAutoChargeFlag(autoChargeFlag);
                engineerChargeCondition.setChargeDate(engineerCharge.getCreateDate());
                engineerChargeCondition.setStatus(EngineerCharge.EC_STATUS_CLOSED);
                engineerChargeCondition.setChargeBy(createBy.getId());
                engineerChargeCondition.setOrderCloseDate(order.getOrderCondition().getCloseDate());
                engineerChargeCondition.setQuarter(customerCharge.getQuarter());
                engineerChargeConditionDao.insert(engineerChargeCondition);

                //修改上门明细标记
                orderDetail.setEngineerInvoiceDate(createDate);
                orderDao.updateDetailInvoiceDate(orderDetail);
            }

            //获取网点费用汇总
            Map<Long, OrderServicePointFee> servicePointFeeMap = orderService.getOrderServicePointFeeMapsForCharge(order.getId(), order.getQuarter());
            for(Map.Entry entry : engineerChargeMasterMap.entrySet()) {
                //生成服务网点结帐信息
                Long engineerInvoiceId = SeqUtils.NextIDValue(SeqUtils.TableName.EngineerChargeMaster);
                //网点汇总数据
                EngineerChargeMaster masterTotalData = (EngineerChargeMaster) entry.getValue();
                //获取网点汇总信息
                if (servicePointFeeMap.containsKey(entry.getKey())) {
                    servicePointFee = servicePointFeeMap.get(entry.getKey());
                } else {
                    servicePointFee = null;
                }

                engineerChargeMaster = new EngineerChargeMaster();
                engineerChargeMaster.setId(engineerInvoiceId);
                engineerChargeMaster.setOrderId(customerCharge.getOrderId());
                engineerChargeMaster.setOrderNo(customerCharge.getOrderNo());
                engineerChargeMaster.setServicePoint(masterTotalData.getServicePoint());
                engineerChargeMaster.setServiceCharge(masterTotalData.getServiceCharge());
                engineerChargeMaster.setExpressCharge(masterTotalData.getExpressCharge());
                engineerChargeMaster.setTravelCharge(masterTotalData.getTravelCharge());
                engineerChargeMaster.setMaterialCharge(masterTotalData.getMaterialCharge());
                engineerChargeMaster.setOtherCharge(masterTotalData.getOtherCharge());
                //设置保险费用及单号,时效
                if (servicePointFee != null) {
                    engineerChargeMaster.setInsuranceCharge(servicePointFee.getInsuranceCharge());
                    engineerChargeMaster.setInsuranceNo(servicePointFee.getInsuranceNo());
                    engineerChargeMaster.setCustomerTimeLinessCharge(servicePointFee.getCustomerTimeLinessCharge());
                    engineerChargeMaster.setTimeLinessCharge(servicePointFee.getTimeLinessCharge());
                    engineerChargeMaster.setUrgentCharge(servicePointFee.getUrgentCharge());
                }
                engineerChargeMaster.setPaymentType(masterTotalData.getPaymentType());
                engineerChargeMaster.setInvoiceDate(createDate);
                engineerChargeMaster.setStatus(EngineerCharge.EC_STATUS_CLOSED);
                engineerChargeMaster.setCreateBy(createBy);
                engineerChargeMaster.setCreateDate(createDate);
                engineerChargeMaster.setQuarter(customerCharge.getQuarter());
                engineerChargeMasterDao.insert(engineerChargeMaster);

                engineerChargeMasterCondition = new EngineerChargeMasterCondition();
                engineerChargeMasterCondition.setId(engineerInvoiceId);
                engineerChargeMasterCondition.setOrderId(customerCharge.getOrderId());
                engineerChargeMasterCondition.setOrderNo(customerCharge.getOrderNo());
                engineerChargeMasterCondition.setServicePointId(masterTotalData.getServicePoint().getId());
                engineerChargeMasterCondition.setPaymentType(masterTotalData.getPaymentType());
                engineerChargeMasterCondition.setChargeOrderType(EngineerCharge.EC_TYPE_ORIGINAL);
                engineerChargeMasterCondition.setAutoChargeFlag(autoChargeFlag);
                engineerChargeMasterCondition.setChargeDate(createDate);
                engineerChargeMasterCondition.setStatus(EngineerCharge.EC_STATUS_CLOSED);
                engineerChargeMasterCondition.setChargeBy(createBy.getId());
                engineerChargeMasterCondition.setOrderCloseDate(order.getOrderCondition().getCloseDate());
                engineerChargeMasterCondition.setQuarter(customerCharge.getQuarter());
                //设置订单完成用时
                if (servicePointFee != null) {
                    engineerChargeMasterCondition.setCustomerTimeLiness(servicePointFee.getCustomerTimeLiness());
                    engineerChargeMasterCondition.setTimeLiness(servicePointFee.getTimeLiness());
                    engineerChargeMasterCondition.setInsuranceNo(servicePointFee.getInsuranceNo());
                }
                engineerChargeMasterConditionDao.insert(engineerChargeMasterCondition);

                //生成网点资金异动流水
                servicePointFinance = servicePointDao.getAmounts(engineerChargeMaster.getServicePoint().getId());
                engineerCurrency = new EngineerCurrency();
                engineerCurrency.setId(SeqUtils.NextIDValue(SeqUtils.TableName.EngineerCurrency));
                engineerCurrency.setServicePoint(engineerChargeMaster.getServicePoint());
                engineerCurrency.setCurrencyType(EngineerCurrency.CURRENCY_TYPE_IN);
                engineerCurrency.setCurrencyNo(engineerChargeMaster.getOrderNo());
                engineerCurrency.setBeforeBalance(servicePointFinance.getBalance());
                Double engineerTotalCharge = engineerChargeMaster.getServiceCharge() +
                        engineerChargeMaster.getExpressCharge() +
                        engineerChargeMaster.getTravelCharge() +
                        engineerChargeMaster.getMaterialCharge() +
                        engineerChargeMaster.getInsuranceCharge() +
                        engineerChargeMaster.getCustomerTimeLinessCharge() +
                        engineerChargeMaster.getTimeLinessCharge() +
                        engineerChargeMaster.getUrgentCharge() +
                        engineerChargeMaster.getOtherCharge();
                engineerCurrency.setBalance(engineerCurrency.getBeforeBalance() + engineerTotalCharge);
                engineerCurrency.setAmount(engineerTotalCharge);
                engineerCurrency.setPaymentType(EngineerCurrency.PAYMENT_TYPE_ORDER_INVOICE);
                engineerCurrency.setActionType(EngineerCurrency.ACTION_TYPE_CHARGE);
                engineerCurrency.setCreateBy(createBy);
                engineerCurrency.setCreateDate(createDate);

                StringBuilder sb = new StringBuilder("结帐转存:");
                sb.append(engineerTotalCharge);
                sb.append(".服务费:").append(engineerChargeMaster.getServiceCharge());
                if (engineerChargeMaster.getExpressCharge() != 0) {
                    sb.append(", 快递费:").append(engineerChargeMaster.getExpressCharge());
                }
                if (engineerChargeMaster.getTravelCharge() != 0) {
                    sb.append(", 远程费:").append(engineerChargeMaster.getTravelCharge());
                }
                if (engineerChargeMaster.getMaterialCharge() != 0) {
                    sb.append(", 配件费:").append(engineerChargeMaster.getMaterialCharge());
                }
                if (engineerChargeMaster.getInsuranceCharge() != 0) {
                    sb.append(" ,互助基金:").append(engineerChargeMaster.getInsuranceCharge())
                            .append(" (").append(engineerChargeMaster.getInsuranceNo()).append(")");
                }
                if (engineerChargeMaster.getCustomerTimeLinessCharge() != 0) {
                    sb.append(", 时效奖励(厂商):").append(engineerChargeMaster.getCustomerTimeLinessCharge());
                }
                if (engineerChargeMaster.getTimeLinessCharge() != 0) {
                    sb.append(", 时效奖励(快可立):").append(engineerChargeMaster.getTimeLinessCharge());
                }
                if (engineerChargeMaster.getUrgentCharge() != 0) {
                    sb.append(", 加急费:").append(engineerChargeMaster.getUrgentCharge());
                }
                if (engineerChargeMaster.getOtherCharge() != 0) {
                    sb.append(", 其他费:").append(engineerChargeMaster.getOtherCharge());
                }
                sb.append(". 订单编号:").append(engineerChargeMaster.getOrderNo());

                engineerCurrency.setRemarks(sb.toString());
                engineerCurrency.setQuarter(engineerChargeMaster.getQuarter());
                engineerCurrencyDao.insert(engineerCurrency);

                //更新网点余额
                servicePointFinance.setBalance(engineerTotalCharge);
                //累计即结款
                if (masterTotalData.getPaymentType() == 20) {
                    servicePointFinance.setDailyBalance(engineerTotalCharge);
                }
                servicePointDao.updateBalance(servicePointFinance);

                //更新缓存
                //ServicePoint cachedServicePoint = servicePointService.getFromCache(engineerChargeMaster.getServicePoint().getId());
                //if (cachedServicePoint != null) {
                    //cachedServicePoint.getFinance().setBalance(engineerCurrency.getBeforeBalance() + engineerTotalCharge);
                    //servicePointService.updateServicePointCache(cachedServicePoint); //mark on 2020-1-14  web端去servicePoint
                //}

                // add on 2020-5-4 begin
                // 更新网点财务缓存
                ServicePointFinance cachedServicePointFinance = servicePointFinanceService.getFromCache(engineerChargeMaster.getServicePoint().getId());
                if (cachedServicePointFinance != null) {
                    cachedServicePointFinance.setBalance(engineerCurrency.getBeforeBalance() + engineerTotalCharge);
                    servicePointFinanceService.updateCache(cachedServicePointFinance);
                }
                // add on 2020-5-4 end


                //累计网点应付款
                servicePointPayableMonthly = new ServicePointPayableMonthly();
                servicePointPayableMonthly.setServicePoint(engineerChargeMaster.getServicePoint());
                servicePointPayableMonthly.setPaymentType(masterTotalData.getPaymentType());
                servicePointPayableMonthly.setYear(DateUtils.getYear(createDate));
                servicePointPayableMonthly.setMonth(DateUtils.getMonth(createDate));
                servicePointPayableMonthly.setId((servicePointPayableMonthly.getServicePoint().getId() * 10000 + servicePointPayableMonthly.getYear()) * 100 + servicePointPayableMonthly.getPaymentType());
                servicePointPayableMonthly.setAmount(engineerTotalCharge);
                //更新应付款
                servicePointPayableMonthlyDao.incrAmountForCharge(servicePointPayableMonthly);

                //累计网点报表余额
                servicePointBalanceMonthlyDao.incrBalance(servicePointPayableMonthly);
            }

            //修改订单标记
            order.getOrderCondition().setId(order.getId());
            order.getOrderCondition().setChargeFlag(1);
            order.getOrderCondition().setAutoChargeFlag(autoChargeFlag);
            order.getOrderCondition().setStatus(OrderStatusType.toDict(OrderStatusType.CHARGED));
            order.getOrderCondition().setSubStatus(Order.ORDER_SUBSTATUS_CHARGED);
            orderDao.updateChargeFlag(order.getOrderCondition());

            orderStatus = new OrderStatus();
            orderStatus.setOrderId(order.getId());
            orderStatus.setChargeBy(createBy);
            orderStatus.setChargeDate(createDate);
            orderStatus.setCustomerInvoiceDate(createDate);
            orderStatus.setEngineerInvoiceDate(createDate);
            orderStatus.setQuarter(order.getQuarter());
            orderDao.updateStatusFlagsFromCharge(orderStatus);

            /*String key = String.format(RedisConstant.SD_ORDER, orderId);
            try {
                //订单缓存失效
                redisUtils.remove(RedisConstant.RedisDBType.REDIS_SD_DB, key);
            } catch (Exception e) {
                log.error("[ChargeService.create] {}", e.getLocalizedMessage());
            }*/
            //公共缓存
            OrderCacheUtils.delete(orderId);

        }catch (Exception e){
            LogUtils.saveLog("订单对帐." + (isAuto ? "队列" : "手工"), "FI:ChargeService.create", orderId.toString(), e, createBy);
            throw new RuntimeException(e.getMessage(),e);
        }finally {
            if(locked && lockKey !=null) {
                redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockKey);
            }
        }
    }
}
