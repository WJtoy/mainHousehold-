package com.wolfking.jeesite.modules.fi.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.entity.fi.mq.MQCreateEngineerCurrencyMessage;
import com.kkl.kklplus.entity.fi.mq.MQCreateUpdateServicePointDeductedMessage;
import com.kkl.kklplus.entity.fi.servicepoint.ServicePointDeducted;
import com.kkl.kklplus.entity.fi.servicepoint.ServicePointDeductedDetail;
import com.kkl.kklplus.entity.md.MDServicePointViewModel;
import com.kkl.kklplus.entity.push.AppMessageType;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.service.LongIDCrudService;
import com.wolfking.jeesite.common.utils.CurrencyUtil;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.PushMessageUtils;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.wolfking.jeesite.modules.fi.dao.ServicePointDeductedDetailDao;
import com.wolfking.jeesite.modules.fi.dao.ServicePointInvoiceMonthlyDao;
import com.wolfking.jeesite.modules.fi.dao.ServicePointPaidMonthlyDao;
import com.wolfking.jeesite.modules.fi.dao.ServicePointWithdrawDao;
import com.wolfking.jeesite.modules.fi.entity.*;
import com.wolfking.jeesite.modules.md.dao.ServicePointDao;
import com.wolfking.jeesite.modules.md.entity.Engineer;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.md.entity.ServicePointFinance;
import com.wolfking.jeesite.modules.md.service.ServicePointFinanceService;
import com.wolfking.jeesite.modules.md.service.ServicePointService;
import com.wolfking.jeesite.modules.mq.sender.CreateEngineerCurrencySender;
import com.wolfking.jeesite.modules.mq.sender.CreateUpdateServicePointDeductedSender;
import com.wolfking.jeesite.modules.rpt.dao.ServicePointBalanceMonthlyDao;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.modules.sys.utils.SeqUtils;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.entity.AppPushMessage;
import com.wolfking.jeesite.ms.enums.PaymentType;
import com.wolfking.jeesite.ms.providermd.service.MSServicePointService;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.wolfking.jeesite.modules.fi.entity.ServicePointWithdraw.*;

/**
 * Created by Jeff on 2017/6/16.
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class ServicePointWithdrawService extends LongIDCrudService<ServicePointWithdrawDao, ServicePointWithdraw> {
    @Resource
    private ServicePointDao servicePointDao;
    @Resource
    private ServicePointPaidMonthlyDao servicePointPaidMonthlyDao;
    @Resource
    private ServicePointInvoiceMonthlyDao servicePointInvoiceMonthlyDao;
    @Autowired
    private ServicePointService servicePointService;
    @Resource
    private ServicePointBalanceMonthlyDao servicePointBalanceMonthlyDao;
    @Autowired
    private CreateEngineerCurrencySender createEngineerCurrencySender;
    @Autowired
    private CreateUpdateServicePointDeductedSender createUpdateServicePointDeductedSender;
    @Autowired
    private MSServicePointService msServicePointService;
    @Resource
    private ServicePointDeductedDetailDao servicePointDeductedDetailDao;
    @Autowired
    private ServicePointFinanceService servicePointFinanceService;

    /**
     * 获取处理中的银行列表
     *
     * @param paymentType
     * @return
     */
    public List<Integer> getProcessBankList(Integer paymentType) {
        return dao.getProcessBankList(paymentType);
    }

    /**
     * 获取一条记录ID，用于判断记录是否存在
     *
     * @param servicePointWithdraw
     * @return
     */
    public Long getOneId(ServicePointWithdraw servicePointWithdraw) {
        return dao.getOneId(servicePointWithdraw);
    }

    /**
     * 获取付款确认列表
     *
     * @param servicePointWithdraw
     * @return
     */
    public List<ServicePointPayCondition> getInvoiceConfirmList(ServicePointWithdraw servicePointWithdraw) {
        return dao.getInvoiceConfirmList(servicePointWithdraw);
    }

    /**
     * 获取付款确认列表明细
     *
     * @param servicePointWithdraw
     * @return
     */
    public List<ServicePointWithdraw> getInvoiceConfirmDetailList(ServicePointWithdraw servicePointWithdraw) {
        //return dao.getInvoiceConfirmDetailList(servicePointWithdraw);
        return Lists.newArrayList();
    }

    /**
     * 获取付款确认列表明细(去servicePoint) //add on 2019-10-9
     * 财务管理->结账管理->付款确认
     * 财务管理->结账管理->付款修改
     *
     * @param servicePointWithdraw
     * @return
     */
    public List<ServicePointWithdraw> getInvoiceConfirmDetailListNew(ServicePointWithdraw servicePointWithdraw) {
        //List<ServicePointWithdraw> servicePointWithdrawList = dao.getInvoiceConfirmDetailList(servicePointWithdraw); //mark on 2020-2-16
        List<ServicePointWithdraw> servicePointWithdrawList = dao.getInvoiceConfirmDetailListWithoutServicePoint(servicePointWithdraw); //add on 2020-2-16
        if (servicePointWithdrawList == null || servicePointWithdrawList.isEmpty()) {
            return servicePointWithdrawList;
        }

        List<Long> servicePointIds = servicePointWithdrawList.stream().map(r -> r.getServicePoint().getId()).distinct().collect(Collectors.toList());
        // add on 2019-10-14 begin
        final List<Long> engineerIds = Lists.newArrayList();
        List<String> fields = Arrays.asList("id", "servicePointNo", "name", "contactInfo1", "contactInfo2", "primaryId", "invoiceFlag", "discountFlag");
        Map<Long, MDServicePointViewModel> servicePointViewModelMap = msServicePointService.findBatchByIdsByConditionToMap(servicePointIds, fields, null, ((engineerIdsFromMS) ->
                engineerIds.addAll(engineerIdsFromMS)
        ));

        //获取具体的安维人员信息
        Map<Long, String> engineerMap = Maps.newHashMap();
        if (engineerIds != null && !engineerIds.isEmpty()) {
            List<Engineer> engineerList = servicePointService.findAllEngineersName(engineerIds, Arrays.asList("id", "name"));
            if (engineerList != null && !engineerList.isEmpty()) {
                engineerMap = engineerList.stream().collect(Collectors.toMap(Engineer::getId, Engineer::getName));
            }
        }
        final Map<Long, String> finalEngineerMap = engineerMap;
        final Map<Long, MDServicePointViewModel> finalServicePointViewModelMap = servicePointViewModelMap;

        servicePointWithdrawList.stream().forEach(entity -> {
            MDServicePointViewModel servicePointVM = finalServicePointViewModelMap.get(entity.getServicePoint().getId());

            if (servicePointVM != null) {
                ServicePoint servicePointEntity = new ServicePoint();
                servicePointEntity.setId(servicePointVM.getId());
                servicePointEntity.setServicePointNo(servicePointVM.getServicePointNo());
                servicePointEntity.setName(servicePointVM.getName());
                servicePointEntity.setContactInfo1(servicePointVM.getContactInfo1());
                servicePointEntity.setContactInfo2(servicePointVM.getContactInfo2());

                if (servicePointVM.getPrimaryId() != null) {
                    String engineerName = finalEngineerMap.get(servicePointVM.getPrimaryId());
                    Engineer engineer = new Engineer();
                    engineer.setName(engineerName);
                    servicePointEntity.setPrimary(engineer);
                }

                ServicePointFinance finance = new ServicePointFinance();
                finance.setInvoiceFlag(servicePointVM.getInvoiceFlag());
                finance.setDiscountFlag(servicePointVM.getDiscountFlag());
                finance.setBankIssue(new Dict(Optional.ofNullable(servicePointVM.getBankIssue()).orElse(0), ""));
                servicePointEntity.setFinance(finance);

                entity.setServicePoint(servicePointEntity);
            }
        });
        // add on 2019-10-14 end

        return servicePointWithdrawList;
    }

    /**
     * 获取付款确认分页明细
     *
     * @param page
     * @param servicePointWithdraw
     * @return
     */
    public Page<ServicePointWithdraw> getInvoiceConfirmDetailPage(Page<ServicePointWithdraw> page, ServicePointWithdraw servicePointWithdraw) {
        servicePointWithdraw.setPage(page);
        //List<ServicePointWithdraw> servicePointWithdrawList = dao.getInvoiceConfirmDetailList(servicePointWithdraw); //mark on 2020-2-16
        List<ServicePointWithdraw> servicePointWithdrawList = Lists.newArrayList();  //add on 2020-2-16
        return page.setList(servicePointWithdrawList);
    }

    /**
     * 获取付款确认分页明细 (去ServicePoint) //add on 2019-10-9
     *
     * @param page
     * @param servicePointWithdraw
     * @return
     */
    public Page<ServicePointWithdraw> getInvoiceConfirmDetailPageNew(Page<ServicePointWithdraw> page, ServicePointWithdraw servicePointWithdraw) {
        servicePointWithdraw.setPage(page);
        //List<ServicePointWithdraw> servicePointWithdrawList = dao.getInvoiceConfirmDetailList(servicePointWithdraw);
        List<ServicePointWithdraw> servicePointWithdrawList = getInvoiceConfirmDetailListNew(servicePointWithdraw);
        return page.setList(servicePointWithdrawList);
    }

    /**
     * 财务主动付款给网点
     *
     * @param servicePointId
     * @param invoiceDate
     * @param debtsAmount
     * @param realCharge
     * @param paymentType
     * @param bank
     * @param branch
     * @param bankNo
     * @param bankOwner
     * @param remarks
     */
    @Transactional()
    public void servicePointPay(Long servicePointId, Date invoiceDate, Double balance, Double debtsAmount, Double realCharge,
                                Integer paymentType, Integer bank, String branch, String bankNo,
                                String bankOwner, String bankOwnerIdNo, String bankOwnerPhone,
                                String remarks, Integer qYear, Integer qMonth,
                                Double totalMinus, Double platformFee) {

        ServicePointWithdraw servicePointWithdraw = new ServicePointWithdraw();
        servicePointWithdraw.setServicePoint(new ServicePoint(servicePointId));
        servicePointWithdraw.setBank(bank);
        servicePointWithdraw.setPaymentType(paymentType);
        servicePointWithdraw.setStatus(20);
        Long processId = dao.getOneId(servicePointWithdraw);
        if (processId != null) {
            throw new RuntimeException("该网点已经有付款在处理,请先确认上次付款结果.");
        }

        User createBy = UserUtils.getUser();
        Date createDate = new Date();

        //生成提款ID与编号
        Long servicePointWithdrawId = SeqUtils.NextIDValue(SeqUtils.TableName.ServicePointWithdraw);
        String withdrawNo = SeqUtils.NextSequenceNo("ServicePointWithdrawNo");

        //生成网点抵扣消息
        BigDecimal minusAmount = BigDecimal.valueOf(0d);
        BigDecimal minusDetailAmount = BigDecimal.valueOf(0d);
        MQCreateUpdateServicePointDeductedMessage.CreateUpdateServicePointDeductedMessage.Builder createUpdateServicePointDeductedMessageBuilder = null;
        List<MQCreateEngineerCurrencyMessage.DeductionByProductCategory> deductionByProductCategories = null;
        List<ServicePointPayableMonthlyDetail> details;
        if (totalMinus != 0) {
            List<ServicePointPayableMonthly> payableMonthlyList = servicePointDao.getPayableMinusMonthlyListByServicePointId(servicePointId);
            createUpdateServicePointDeductedMessageBuilder = MQCreateUpdateServicePointDeductedMessage.CreateUpdateServicePointDeductedMessage.newBuilder();
            int payableYearMonth = qYear * 100 + qMonth;
            createUpdateServicePointDeductedMessageBuilder.setLastDeductionYearMonth(payableYearMonth);
            details = Lists.newArrayList();
            for (ServicePointPayableMonthly payableMonthly : payableMonthlyList) {
                int minusPayableYearMonth = payableMonthly.getYear() * 100 + payableMonthly.getMonth();
                //在最后抵扣之前与当前应付月之间出现的负值
                if (minusPayableYearMonth <= payableYearMonth &&
                        minusPayableYearMonth > payableMonthly.getLastDeductionYearMonth()) {
                    minusAmount = minusAmount.add(BigDecimal.valueOf(payableMonthly.getAmount()));
                    MQCreateUpdateServicePointDeductedMessage.CreateUpdateServicePointDeducted createUpdateServicePointDeducted = MQCreateUpdateServicePointDeductedMessage.CreateUpdateServicePointDeducted.newBuilder()
                            .setMessageId(SeqUtils.NextIDValue(SeqUtils.TableName.ServicePointDeducted))
                            .setActionType(ServicePointDeducted.ACTION_TYPE_CREATE)
                            .setServicePointId(servicePointId)
                            .setPaymentType(paymentType)
                            .setDeductionType(1)
                            .setYearMonth(minusPayableYearMonth)
                            .setDeductionYearMonth(payableYearMonth)
                            .setWithdrawId(servicePointWithdrawId)
                            .setWithdrawNo(withdrawNo)
                            .setAmount(payableMonthly.getAmount())
                            .setStatus(ServicePointWithdraw.SPW_STATUS_SUCCESS)
                            .setCreateById(createBy.getId())
                            .setCreateDate(createDate.getTime())
                            .build();
                    createUpdateServicePointDeductedMessageBuilder.addCreateUpdateServicePointDeducted(createUpdateServicePointDeducted);

                    ServicePointPayableMonthlyDetail detail = new ServicePointPayableMonthlyDetail();
                    detail.setTotalId((servicePointId * 10000 + payableMonthly.getYear()) * 100 + paymentType);
                    detail.setMonth(payableMonthly.getMonth());
                    details.add(detail);
                }
            }

            if (totalMinus.doubleValue() != minusAmount.doubleValue()) {
                throw new RuntimeException("待扣款金额发生变化,请刷新界面后重试.");
            }

            List<ServicePointPayableMonthlyDetail> payableMonthlyDetailList = servicePointDao.getPayableMinusMonthlyDetailList(details);
            deductionByProductCategories = Lists.newArrayList();

            for (ServicePointPayableMonthlyDetail detail : payableMonthlyDetailList) {
                int minusPayableYearMonth = detail.getYear() * 100 + detail.getMonth();
                //在最后抵扣之前与当前应付月之间出现的负值
                if (minusPayableYearMonth <= payableYearMonth &&
                        minusPayableYearMonth > detail.getLastDeductionYearMonth()) {
                    minusDetailAmount = minusDetailAmount.add(BigDecimal.valueOf(detail.getAmount()));
                    MQCreateUpdateServicePointDeductedMessage.CreateUpdateServicePointDeductedDetail createUpdateServicePointDeductedDetail = MQCreateUpdateServicePointDeductedMessage.CreateUpdateServicePointDeductedDetail.newBuilder()
                            .setMessageId(SeqUtils.NextIDValue(SeqUtils.TableName.ServicePointDeducted))
                            .setActionType(ServicePointDeducted.ACTION_TYPE_CREATE)
                            .setServicePointId(servicePointId)
                            .setProductCategoryId(detail.getProductCategoryId())
                            .setPaymentType(paymentType)
                            .setDeductionType(1)
                            .setYearMonth(minusPayableYearMonth)
                            .setDeductionYearMonth(payableYearMonth)
                            .setWithdrawId(servicePointWithdrawId)
                            .setWithdrawNo(withdrawNo)
                            .setAmount(detail.getAmount())
                            .setStatus(ServicePointWithdraw.SPW_STATUS_SUCCESS)
                            .setCreateById(createBy.getId())
                            .setCreateDate(createDate.getTime())
                            .build();
                    createUpdateServicePointDeductedMessageBuilder.addCreateUpdateServicePointDeductedDetail(createUpdateServicePointDeductedDetail);

                    MQCreateEngineerCurrencyMessage.DeductionByProductCategory deductionByProductCategory = MQCreateEngineerCurrencyMessage.DeductionByProductCategory.newBuilder()
                            .setProductCategoryId(detail.getProductCategoryId())
                            .setAmount(detail.getAmount())
                            .build();
                    deductionByProductCategories.add(deductionByProductCategory);
                }
            }

            if (totalMinus.doubleValue() != minusDetailAmount.doubleValue()) {
                throw new RuntimeException("待扣款金额发生变化,请刷新界面后重试.");
            }
        }

        ServicePointFinance servicePointFinance;

        //提款记录
        servicePointFinance = servicePointDao.getAmounts(servicePointId);
        int bankIssueValue = Integer.parseInt(servicePointFinance.getBankIssue().getValue());
        servicePointWithdraw = new ServicePointWithdraw();
        servicePointWithdraw.setId(servicePointWithdrawId);
        servicePointWithdraw.setWithdrawNo(withdrawNo);
        servicePointWithdraw.setServicePoint(new ServicePoint(servicePointId));
        servicePointWithdraw.setPaymentType(paymentType);
        servicePointWithdraw.setStatus(ServicePointWithdraw.SPW_STATUS_SUCCESS);
        servicePointWithdraw.setWithdrawType(ServicePointWithdraw.SPW_WITHDRAW_TYPE_PAY);
        servicePointWithdraw.setBank(bank);
        servicePointWithdraw.setBranch(branch);
        servicePointWithdraw.setBankNo(bankNo);
        servicePointWithdraw.setBankOwner(bankOwner);
        servicePointWithdraw.setBankOwnerIdNo(bankOwnerIdNo);
        servicePointWithdraw.setBankOwnerPhone(bankOwnerPhone);
        servicePointWithdraw.setBeforeBalance(servicePointFinance.getBalance());
        servicePointWithdraw.setBeforeDebts(servicePointFinance.getDebtsAmount());
        servicePointWithdraw.setApplyAmount(realCharge);
        servicePointWithdraw.setPayAmount(realCharge);
        servicePointWithdraw.setDebtsDeduction(debtsAmount);
        servicePointWithdraw.setSetDeductionAmount(totalMinus);
        servicePointWithdraw.setPlatformFee(platformFee);
        servicePointWithdraw.setPayBy(createBy);
        servicePointWithdraw.setPayDate(invoiceDate);
        //月结
        if (paymentType == 10) {
            servicePointWithdraw.setPayForYear(qYear);
            servicePointWithdraw.setPayForMonth(qMonth);
        }
        //即结
        if (paymentType == 20) {
            servicePointWithdraw.setPayForYear(DateUtils.getYear(invoiceDate));
            servicePointWithdraw.setPayForMonth(DateUtils.getMonth(invoiceDate));
        }
        servicePointWithdraw.setCreateBy(createBy);
        servicePointWithdraw.setCreateDate(createDate);
        servicePointWithdraw.setRemarks(remarks);
        servicePointWithdraw.setQuarter(QuarterUtils.getSeasonQuarter(createDate));
        dao.insert(servicePointWithdraw);
        //生成网点资金异动流水
        Long createEngineerCurrencyMessageId = SeqUtils.NextIDValue(SeqUtils.TableName.EngineerCurrency);
        MQCreateEngineerCurrencyMessage.CreateEngineerCurrencyMessage.Builder createEngineerCurrencyMessageBuilder = MQCreateEngineerCurrencyMessage.CreateEngineerCurrencyMessage.newBuilder()
                .setMessageId(createEngineerCurrencyMessageId)
                .setServicePointId(servicePointId)
                .setCurrencyType(EngineerCurrency.CURRENCY_TYPE_OUT)
                .setCurrencyNo(withdrawNo)
                .setBeforeBalance(servicePointFinance.getBalance())
                .setBalance(servicePointFinance.getBalance() - debtsAmount - realCharge + platformFee)
                .setAmount(debtsAmount + realCharge)
                .setPaymentType(EngineerCurrency.PAYMENT_TYPE_TRANSFER_ACCOUNT)
                .setActionType(EngineerCurrency.ACTION_TYPE_PAY)
                .setCreateById(createBy.getId())
                .setCreateDate(createDate.getTime())
                .setRemarks(platformFee == 0
                        ? String.format("本次付款:%.2f元, %s", realCharge, remarks)
                        : String.format("本次付款%.2f元(扣除平台服务费%.2f元,实际付款%.2f元), %s", realCharge - platformFee, 0 - platformFee, realCharge, remarks))
                .setQuarter(servicePointWithdraw.getQuarter());
        //变更网点余额
        servicePointFinance = new ServicePointFinance();
        servicePointFinance.setId(servicePointId);
        servicePointFinance.setTotalAmount(debtsAmount + realCharge);
        servicePointFinance.setBalance(0 - debtsAmount - realCharge + platformFee);
        servicePointFinance.setPlatformFee(platformFee);
        //更新最后付款时间
        servicePointFinance.setLastPayDate(invoiceDate);
        //更新最后付款金额
        servicePointFinance.setLastPayAmount(debtsAmount + realCharge);
        //即结,减少即结余额
        if (paymentType == 20) {
            servicePointFinance.setDailyBalance(0 - debtsAmount - realCharge + platformFee);
        }
        servicePointDao.payServicePoint(servicePointFinance);
        if (bankIssueValue != 0) {
            servicePointFinance.setBankIssue(new Dict(0, ""));
//            servicePointDao.updateBankIssue(servicePointFinance);
            servicePointService.updateBankIssue(servicePointFinance);  // add on 2019-9-17
        }
        // mark on 2020-5-4 begin
        ServicePoint cachedServicePoint = servicePointService.getFromCache(servicePointWithdraw.getServicePoint().getId());
        //更新缓存
//        if (cachedServicePoint != null) {
//            cachedServicePoint.getFinance().setBankIssue(new Dict("0", ""));//2018/08/05 Ryan 付款成功未同步付款失败原因
//            cachedServicePoint.getFinance().setLastPayDate(servicePointFinance.getLastPayDate());
//            cachedServicePoint.getFinance().setLastPayAmount(servicePointFinance.getLastPayAmount());
//            cachedServicePoint.getFinance().setBalance(createEngineerCurrencyMessageBuilder.getBalance());
            //servicePointService.updateServicePointCache(cachedServicePoint);  //mark on 2020-1-14  web端去servicePoint
//        }
        // mark on 2020-5-4 end
        // add on 2020-5-4 begin
        // 更新网点财务缓存
        ServicePointFinance cachedServicePointFinance = servicePointFinanceService.getFromCache(servicePointWithdraw.getServicePoint().getId());
        if (cachedServicePointFinance != null) {
            cachedServicePointFinance.setBankIssue(new Dict("0", ""));
            cachedServicePointFinance.setLastPayDate(servicePointFinance.getLastPayDate());
            cachedServicePointFinance.setLastPayAmount(servicePointFinance.getLastPayAmount());
            cachedServicePointFinance.setBalance(Double.parseDouble(String.format("%.2f",createEngineerCurrencyMessageBuilder.getBalance())));
        }
        servicePointFinanceService.updateCache(cachedServicePointFinance);
        // add on 2020-5-4 end
        //增加付款金额
        ServicePointPaidMonthly servicePointPaidMonthly = new ServicePointPaidMonthly();
        servicePointPaidMonthly.setServicePoint(servicePointWithdraw.getServicePoint());
        servicePointPaidMonthly.setYear(servicePointWithdraw.getPayForYear());
        servicePointPaidMonthly.setMonth(servicePointWithdraw.getPayForMonth());
        servicePointPaidMonthly.setAmount(debtsAmount + realCharge - platformFee);
        servicePointPaidMonthly.setPaymentType(paymentType);
        servicePointPaidMonthly.setId((servicePointPaidMonthly.getServicePoint().getId() * 10000 + servicePointPaidMonthly.getYear()) * 100 + servicePointPaidMonthly.getPaymentType());
        servicePointPaidMonthlyDao.incrAmount(servicePointPaidMonthly);
        //增加出帐金额
        ServicePointInvoiceMonthly servicePointInvoiceMonthly = new ServicePointInvoiceMonthly();
        servicePointInvoiceMonthly.setServicePoint(servicePointWithdraw.getServicePoint());
        servicePointInvoiceMonthly.setYear(DateUtils.getYear(invoiceDate));
        servicePointInvoiceMonthly.setMonth(DateUtils.getMonth(invoiceDate));
        servicePointInvoiceMonthly.setAmount(debtsAmount + realCharge - platformFee);
        servicePointInvoiceMonthly.setPaymentType(paymentType);
        servicePointInvoiceMonthly.setId((servicePointInvoiceMonthly.getServicePoint().getId() * 10000 + servicePointInvoiceMonthly.getYear()) * 100 + servicePointInvoiceMonthly.getPaymentType());
        servicePointInvoiceMonthlyDao.incrAmount(servicePointInvoiceMonthly);
        //扣除网点报表余额
        ServicePointPayableMonthly servicePointPayableMonthly = new ServicePointPayableMonthly();
        servicePointPayableMonthly.setServicePoint(servicePointWithdraw.getServicePoint());
        servicePointPayableMonthly.setPaymentType(paymentType);
        servicePointPayableMonthly.setYear(DateUtils.getYear(invoiceDate));
        servicePointPayableMonthly.setMonth(DateUtils.getMonth(invoiceDate));
        servicePointPayableMonthly.setId((servicePointPayableMonthly.getServicePoint().getId() * 10000 + servicePointPayableMonthly.getYear()) * 100 + servicePointPayableMonthly.getPaymentType());
        servicePointPayableMonthly.setAmount(0 - debtsAmount - realCharge + platformFee);
        servicePointBalanceMonthlyDao.incrBalance(servicePointPayableMonthly);
        //生成按品类金额异动消息
        MQCreateEngineerCurrencyMessage.IncreaseAmountByProductCategory increaseAmountByProductCategory = MQCreateEngineerCurrencyMessage.IncreaseAmountByProductCategory.newBuilder()
                .setPaymentType(paymentType)
                .setPayForYear(servicePointPaidMonthly.getYear())
                .setPayForMonth(servicePointPaidMonthly.getMonth())
                .setInvoiceYear(servicePointInvoiceMonthly.getYear())
                .setInvoiceMonth(servicePointInvoiceMonthly.getMonth())
                .setAmount(servicePointPaidMonthly.getAmount())
                .build();
        createEngineerCurrencyMessageBuilder.setIncreaseAmountByProductCategory(increaseAmountByProductCategory);
        //按品类拆分抵扣款
        if (totalMinus.doubleValue() != 0 && minusDetailAmount.doubleValue() != 0) {
            createEngineerCurrencyMessageBuilder.addAllDeductionByProductCategories(deductionByProductCategories);
        }
        //发送生成网点流水消息
        MQCreateEngineerCurrencyMessage.CreateEngineerCurrencyMessage createEngineerCurrencyMessage = createEngineerCurrencyMessageBuilder.build();
        try {
            createEngineerCurrencySender.send(createEngineerCurrencyMessage);
        } catch (Exception e) {
            LogUtils.saveLog("财务付款", "FI:ServicePointWithdraw.servicePointPay.currency", new JsonFormat().printToString(createEngineerCurrencyMessage), e, createBy);
        }
        //发送生成网点抵扣消息
        if (totalMinus != 0) {
            MQCreateUpdateServicePointDeductedMessage.CreateUpdateServicePointDeductedMessage createUpdateServicePointDeductedMessage = createUpdateServicePointDeductedMessageBuilder.build();
            try {
                createUpdateServicePointDeductedSender.send(createUpdateServicePointDeductedMessage);
            } catch (Exception e) {
                LogUtils.saveLog("财务付款", "FI:ServicePointWithdraw.servicePointPay.deducted", new JsonFormat().printToString(createUpdateServicePointDeductedMessage), e, createBy);
            }
        }
        //发送打款通知
        try {
            ServicePoint servicePointInfo = servicePointService.getFromCache(servicePointWithdraw.getServicePoint().getId());
            if (servicePointInfo.getPrimary() != null && servicePointInfo.getPrimary().getId() != null) {
                Engineer primaryEngineer = servicePointService.getEngineerFromCache(servicePointInfo.getId(), servicePointInfo.getPrimary().getId());
                if (primaryEngineer != null && primaryEngineer.getAccountId() != null) {
                    String content = platformFee == 0
                            ? String.format("您好,已向您%s的账号付款%.2f元,请注意查收(到账时间可能略有差异)",
                            (bankNo == null || bankNo.length() < 5 ? "" : "尾号为" + bankNo.substring(bankNo.length() - 4, bankNo.length())), realCharge)
                            : String.format("您好,已向您%s的账号付款%.2f元(扣除平台服务费%.2f元,实际付款%.2f元),请注意查收(到账时间可能略有差异)",
                            (bankNo == null || bankNo.length() < 5 ? "" : "尾号为" + bankNo.substring(bankNo.length() - 4, bankNo.length())), realCharge - platformFee, 0 - platformFee, realCharge);
                    PushMessageUtils.push(AppPushMessage.PassThroughType.NOTIFICATION, AppMessageType.PAY, "打款通知", content, primaryEngineer.getAccountId());
                }
            }
        } catch (Exception e) {

        }
    }

    /**
     * 批量付款
     *
     * @param ids
     */
    @Transactional()
    public void servicePointPaySelected(String ids, String qPayment, String qBank, String payBank) {
        String[] servicePointDatasArray = ids.split(",");
        ServicePointFinance servicePointFinance;
        User createBy = UserUtils.getUser();
        Date createDate = new Date();
        int payment = Integer.parseInt(qPayment);
        int bank = Integer.parseInt(qBank);

        ServicePointWithdraw spw = new ServicePointWithdraw();
        spw.setBank(bank);
        spw.setPaymentType(payment);
        spw.setStatus(20);
        Long processId = dao.getOneId(spw);
        if (processId != null) {
            throw new RuntimeException("该银行已经有付款在处理,请先确认上次付款结果.");
        }

        //生成提款编号
        String withdrawNo = SeqUtils.NextSequenceNo("ServicePointWithdrawNo");

        int index = 0;
        for (String servicePointDatas : servicePointDatasArray) {
            String[] servicePointData = servicePointDatas.split(";", -1);
            Long servicePointId = Long.parseLong(servicePointData[0]);
            double applyAmount = Double.parseDouble(servicePointData[1]);
            int paymentType = Integer.parseInt(servicePointData[2]);
            Integer qYear = Integer.parseInt(servicePointData[3]);
            Integer qMonth = Integer.parseInt(servicePointData[4]);
            double totalMinus = Double.parseDouble(servicePointData[5]);
            double totalDeductedAmount = Double.parseDouble(servicePointData[6]);
            double platformFee = Double.parseDouble(servicePointData[7]);
            String bankOwnerIdNo = servicePointData[8];
            String bankOwnerPhone = servicePointData[9];
            applyAmount = applyAmount + totalMinus + totalDeductedAmount + platformFee;

            //生成提款ID
            Long servicePointWithdrawId = SeqUtils.NextIDValue(SeqUtils.TableName.ServicePointWithdraw);
            servicePointWithdrawId = servicePointWithdrawId + index;

            //生成网点抵扣消息
            String minusInfo = "";
            BigDecimal minusAmount = BigDecimal.valueOf(0d);
            BigDecimal minusDetailAmount = BigDecimal.valueOf(0d);
            MQCreateUpdateServicePointDeductedMessage.CreateUpdateServicePointDeductedMessage.Builder createUpdateServicePointDeductedMessageBuilder = null;
            List<ServicePointPayableMonthlyDetail> details;
            if (totalMinus != 0) {
                List<ServicePointPayableMonthly> payableMonthlyList = servicePointDao.getPayableMinusMonthlyListByServicePointId(servicePointId);
                StringBuilder sb = new StringBuilder();
                createUpdateServicePointDeductedMessageBuilder = MQCreateUpdateServicePointDeductedMessage.CreateUpdateServicePointDeductedMessage.newBuilder();
                int payableYearMonth = qYear * 100 + qMonth;
                createUpdateServicePointDeductedMessageBuilder.setLastDeductionYearMonth(payableYearMonth);
                details = Lists.newArrayList();
                for (ServicePointPayableMonthly payableMonthly : payableMonthlyList) {
                    int minusPayableYearMonth = payableMonthly.getYear() * 100 + payableMonthly.getMonth();
                    if (minusPayableYearMonth <= payableYearMonth &&
                            minusPayableYearMonth > payableMonthly.getLastDeductionYearMonth()) {
                        sb.append(String.format("%d年%d月:%.2f元, ", payableMonthly.getYear(), payableMonthly.getMonth(), payableMonthly.getAmount()));
                        minusAmount = minusAmount.add(BigDecimal.valueOf(payableMonthly.getAmount()));
                        MQCreateUpdateServicePointDeductedMessage.CreateUpdateServicePointDeducted createUpdateServicePointDeducted = MQCreateUpdateServicePointDeductedMessage.CreateUpdateServicePointDeducted.newBuilder()
                                .setMessageId(SeqUtils.NextIDValue(SeqUtils.TableName.ServicePointDeducted))
                                .setActionType(ServicePointDeducted.ACTION_TYPE_CREATE)
                                .setServicePointId(servicePointId)
                                .setPaymentType(paymentType)
                                .setDeductionType(1)
                                .setYearMonth(minusPayableYearMonth)
                                .setDeductionYearMonth(payableYearMonth)
                                .setWithdrawId(servicePointWithdrawId)
                                .setWithdrawNo(withdrawNo)
                                .setAmount(payableMonthly.getAmount())
                                .setStatus(ServicePointWithdraw.SPW_STATUS_PROCESS)
                                .setCreateById(createBy.getId())
                                .setCreateDate(createDate.getTime())
                                .build();
                        createUpdateServicePointDeductedMessageBuilder.addCreateUpdateServicePointDeducted(createUpdateServicePointDeducted);

                        ServicePointPayableMonthlyDetail detail = new ServicePointPayableMonthlyDetail();
                        detail.setTotalId((servicePointId * 10000 + payableMonthly.getYear()) * 100 + paymentType);
                        detail.setMonth(payableMonthly.getMonth());
                        details.add(detail);
                    }
                    if (minusAmount.doubleValue() != 0d) {
                        minusInfo = sb.toString().substring(0, sb.length() - 2);
                    }
                }

                if (totalMinus != minusAmount.doubleValue()) {
                    throw new RuntimeException("待扣款金额发生变化,请刷新界面后重试.");
                }

                List<ServicePointPayableMonthlyDetail> payableMonthlyDetailList = servicePointDao.getPayableMinusMonthlyDetailList(details);

                for (ServicePointPayableMonthlyDetail detail : payableMonthlyDetailList) {
                    int minusPayableYearMonth = detail.getYear() * 100 + detail.getMonth();
                    //在最后抵扣之前与当前应付月之间出现的负值
                    if (minusPayableYearMonth <= payableYearMonth &&
                            minusPayableYearMonth > detail.getLastDeductionYearMonth()) {
                        minusDetailAmount = minusDetailAmount.add(BigDecimal.valueOf(detail.getAmount()));
                        MQCreateUpdateServicePointDeductedMessage.CreateUpdateServicePointDeductedDetail createUpdateServicePointDeductedDetail = MQCreateUpdateServicePointDeductedMessage.CreateUpdateServicePointDeductedDetail.newBuilder()
                                .setMessageId(SeqUtils.NextIDValue(SeqUtils.TableName.ServicePointDeducted))
                                .setActionType(ServicePointDeducted.ACTION_TYPE_CREATE)
                                .setServicePointId(servicePointId)
                                .setProductCategoryId(detail.getProductCategoryId())
                                .setPaymentType(paymentType)
                                .setDeductionType(1)
                                .setYearMonth(minusPayableYearMonth)
                                .setDeductionYearMonth(payableYearMonth)
                                .setWithdrawId(servicePointWithdrawId)
                                .setWithdrawNo(withdrawNo)
                                .setAmount(detail.getAmount())
                                .setStatus(ServicePointWithdraw.SPW_STATUS_PROCESS)
                                .setCreateById(createBy.getId())
                                .setCreateDate(createDate.getTime())
                                .build();
                        createUpdateServicePointDeductedMessageBuilder.addCreateUpdateServicePointDeductedDetail(createUpdateServicePointDeductedDetail);
                    }
                }

                if (totalMinus != minusDetailAmount.doubleValue()) {
                    throw new RuntimeException("待扣款金额发生变化,请刷新界面后重试.");
                }
            }

            //生成提款记录
            servicePointFinance = servicePointDao.getFinance(servicePointId);
            ServicePointWithdraw servicePointWithdraw = new ServicePointWithdraw();
            servicePointWithdraw.setId(servicePointWithdrawId);
            servicePointWithdraw.setWithdrawNo(withdrawNo);
            servicePointWithdraw.setServicePoint(new ServicePoint(servicePointId));
            servicePointWithdraw.setPaymentType(paymentType);
            servicePointWithdraw.setStatus(ServicePointWithdraw.SPW_STATUS_PROCESS);
            servicePointWithdraw.setWithdrawType(ServicePointWithdraw.SPW_WITHDRAW_TYPE_PAY);
            servicePointWithdraw.setBank(Integer.parseInt(servicePointFinance.getBank().getValue()));
            servicePointWithdraw.setBranch(servicePointFinance.getBranch());
            servicePointWithdraw.setBankNo(servicePointFinance.getBankNo());
            servicePointWithdraw.setBankOwner(servicePointFinance.getBankOwner());
            servicePointWithdraw.setBankOwnerIdNo(bankOwnerIdNo);
            servicePointWithdraw.setBankOwnerPhone(bankOwnerPhone);
            servicePointWithdraw.setBeforeBalance(servicePointFinance.getBalance());
            servicePointWithdraw.setBeforeDebts(servicePointFinance.getDebtsAmount());
            servicePointWithdraw.setApplyAmount(applyAmount);
            servicePointWithdraw.setPayAmount(0d);
            servicePointWithdraw.setDebtsDeduction(0d);
            servicePointWithdraw.setSetDeductionAmount(totalMinus);
            servicePointWithdraw.setPlatformFee(platformFee);
            servicePointWithdraw.setCreateBy(createBy);
            servicePointWithdraw.setCreateDate(createDate);
            //月结
            if (paymentType == 10) {
                servicePointWithdraw.setPayForYear(qYear);
                servicePointWithdraw.setPayForMonth(qMonth);
                servicePointWithdraw.setRemarks("快可立".
                        concat(qMonth.toString()).
                        concat("月月结").
                        concat("师傅付款").
                        concat(payBank).
                        concat(minusInfo.length() > 0 ? ",扣除待扣款:".concat(minusInfo) : ""));
            }
            //即结
            else if (paymentType == 20) {
                servicePointWithdraw.setPayForYear(DateUtils.getYear(createDate));
                servicePointWithdraw.setPayForMonth(DateUtils.getMonth(createDate));
                servicePointWithdraw.setRemarks("快可立".concat(String.valueOf(DateUtils.getMonth(createDate)).concat("月即结")).concat("师傅付款").concat(payBank));
            }
            servicePointWithdraw.setQuarter(QuarterUtils.getSeasonQuarter(createDate));
            dao.insert(servicePointWithdraw);

            //生成网点资金异动流水
            Long engineerCurrencyId = SeqUtils.NextIDValue(SeqUtils.TableName.EngineerCurrency);
            engineerCurrencyId = engineerCurrencyId + index;
            MQCreateEngineerCurrencyMessage.CreateEngineerCurrencyMessage createEngineerCurrencyMessage = MQCreateEngineerCurrencyMessage.CreateEngineerCurrencyMessage.newBuilder()
                    .setMessageId(engineerCurrencyId)
                    .setServicePointId(servicePointId)
                    .setCurrencyType(EngineerCurrency.CURRENCY_TYPE_NONE)
                    .setCurrencyNo(withdrawNo)
                    .setBeforeBalance(0)
                    .setBalance(0)
                    .setAmount(applyAmount)
                    .setPaymentType(EngineerCurrency.PAYMENT_TYPE_TRANSFER_ACCOUNT)
                    .setActionType(EngineerCurrency.ACTION_TYPE_PAY_APPLY)
                    .setCreateById(createBy.getId())
                    .setCreateDate(createDate.getTime())
                    .setRemarks(platformFee == 0
                            ? String.format("财务付款,银行处理中,本次付款:%.2f元", applyAmount)
                            : String.format("财务付款,银行处理中,本次付款%.2f元(扣除平台服务费%.2f元,实际付款%.2f元)", applyAmount - platformFee, 0 - platformFee, applyAmount))
                    .setQuarter(servicePointWithdraw.getQuarter())
                    .build();

            //发送生成网点流水消息
            try {
                createEngineerCurrencySender.send(createEngineerCurrencyMessage);
            } catch (Exception e) {
                LogUtils.saveLog("财务付款", "FI:ServicePointWithdraw.servicePointPaySelected.currency", new JsonFormat().printToString(createEngineerCurrencyMessage), e, createBy);
            }
            //发送生成网点抵扣消息
            if (totalMinus != 0) {
                MQCreateUpdateServicePointDeductedMessage.CreateUpdateServicePointDeductedMessage createUpdateServicePointDeductedMessage = createUpdateServicePointDeductedMessageBuilder.build();
                try {
                    createUpdateServicePointDeductedSender.send(createUpdateServicePointDeductedMessage);
                } catch (Exception e) {
                    LogUtils.saveLog("财务付款", "FI:ServicePointWithdraw.servicePointPaySelected.deducted", new JsonFormat().printToString(createUpdateServicePointDeductedMessage), e, createBy);
                }
            }

            index++;
        }
    }

    /**
     * 批量确认付款成功
     *
     * @param ids
     */
    @Transactional()
    public void servicePointConfirmSelectedSuccess(String ids) {
        String[] withdrawIds = ids.split(",", -1);
        User createBy = UserUtils.getUser();
        Date createDate = new Date();

        ServicePointWithdraw servicePointWithdraw;
        ServicePointFinance servicePointFinance;
        ServicePointPaidMonthly servicePointPaidMonthly;
        ServicePointInvoiceMonthly servicePointInvoiceMonthly;

        for (String idString : withdrawIds) {
            Long withdrawId = Long.parseLong(idString);
            servicePointWithdraw = dao.getForConfirm(withdrawId);
            if (servicePointWithdraw.getStatus() == 30 || servicePointWithdraw.getStatus() == 40) {
                throw new RuntimeException("该银行已经确认,请重新刷新页面.");
            }

            servicePointFinance = servicePointDao.getFinance(servicePointWithdraw.getServicePoint().getId());
            int bankIssueValue = Integer.parseInt(servicePointFinance.getBankIssue().getValue());

            MQCreateUpdateServicePointDeductedMessage.CreateUpdateServicePointDeductedMessage.Builder createUpdateServicePointDeductedMessageBuilder = null;
            List<MQCreateEngineerCurrencyMessage.DeductionByProductCategory> deductionByProductCategories = null;

            if (servicePointWithdraw.getDeductionAmount() != 0) {
                createUpdateServicePointDeductedMessageBuilder = MQCreateUpdateServicePointDeductedMessage.CreateUpdateServicePointDeductedMessage.newBuilder();
                createUpdateServicePointDeductedMessageBuilder.setLastDeductionYearMonth(servicePointWithdraw.getPayForYear() * 100 + servicePointWithdraw.getPayForMonth());
                MQCreateUpdateServicePointDeductedMessage.CreateUpdateServicePointDeducted createUpdateServicePointDeducted = MQCreateUpdateServicePointDeductedMessage.CreateUpdateServicePointDeducted.newBuilder()
                        .setServicePointId(servicePointWithdraw.getServicePoint().getId())
                        .setWithdrawId(withdrawId)
                        .setPaymentType(servicePointWithdraw.getPaymentType())
                        .setActionType(ServicePointDeducted.ACTION_TYPE_UPDATE)
                        .setStatus(ServicePointWithdraw.SPW_STATUS_SUCCESS)
                        .setUpdateById(createBy.getId())
                        .setUpdateDate(createDate.getTime())
                        .build();
                createUpdateServicePointDeductedMessageBuilder.addCreateUpdateServicePointDeducted(createUpdateServicePointDeducted);

                MQCreateUpdateServicePointDeductedMessage.CreateUpdateServicePointDeductedDetail createUpdateServicePointDeductedDetail = MQCreateUpdateServicePointDeductedMessage.CreateUpdateServicePointDeductedDetail.newBuilder()
                        .setServicePointId(servicePointWithdraw.getServicePoint().getId())
                        .setWithdrawId(withdrawId)
                        .setPaymentType(servicePointWithdraw.getPaymentType())
                        .setActionType(ServicePointDeducted.ACTION_TYPE_UPDATE)
                        .setStatus(ServicePointWithdraw.SPW_STATUS_SUCCESS)
                        .setUpdateById(createBy.getId())
                        .setUpdateDate(createDate.getTime())
                        .build();
                createUpdateServicePointDeductedMessageBuilder.addCreateUpdateServicePointDeductedDetail(createUpdateServicePointDeductedDetail);

                List<ServicePointDeductedDetail> deductedDetailAmountList = servicePointDeductedDetailDao.getDeductedDetailAmountList(withdrawId);
                deductionByProductCategories = Lists.newArrayListWithCapacity(deductedDetailAmountList.size());

                for (ServicePointDeductedDetail detail : deductedDetailAmountList) {
                    MQCreateEngineerCurrencyMessage.DeductionByProductCategory deductionByProductCategory = MQCreateEngineerCurrencyMessage.DeductionByProductCategory.newBuilder()
                            .setProductCategoryId(detail.getProductCategoryId())
                            .setAmount(detail.getAmount())
                            .build();
                    deductionByProductCategories.add(deductionByProductCategory);
                }
            }

            //生成网点资金异动流水
            Long engineerCurrencyId = SeqUtils.NextIDValue(SeqUtils.TableName.EngineerCurrency);
            MQCreateEngineerCurrencyMessage.CreateEngineerCurrencyMessage.Builder createEngineerCurrencyMessageBuilder = MQCreateEngineerCurrencyMessage.CreateEngineerCurrencyMessage.newBuilder()
                    .setMessageId(engineerCurrencyId)
                    .setServicePointId(servicePointWithdraw.getServicePoint().getId())
                    .setCurrencyType(EngineerCurrency.CURRENCY_TYPE_NONE)
                    .setCurrencyNo(servicePointWithdraw.getWithdrawNo())
                    .setBeforeBalance(servicePointFinance.getBalance())
                    .setBalance(servicePointFinance.getBalance() - servicePointWithdraw.getApplyAmount() + servicePointWithdraw.getPlatformFee())
                    .setAmount(servicePointWithdraw.getApplyAmount())
                    .setPaymentType(EngineerCurrency.PAYMENT_TYPE_TRANSFER_ACCOUNT)
                    .setActionType(EngineerCurrency.ACTION_TYPE_PAY)
                    .setCreateById(createBy.getId())
                    .setCreateDate(createDate.getTime())
                    .setRemarks(servicePointWithdraw.getPlatformFee() == 0
                            ? String.format("本次付款:%.2f元, %s", servicePointWithdraw.getApplyAmount(), servicePointWithdraw.getRemarks())
                            : String.format("本次付款%.2f元(扣除平台服务费%.2f元,实际付款%.2f元), %s", servicePointWithdraw.getApplyAmount() - servicePointWithdraw.getPlatformFee(), 0 - servicePointWithdraw.getPlatformFee(), servicePointWithdraw.getApplyAmount(), servicePointWithdraw.getRemarks()))
                    .setQuarter(servicePointWithdraw.getQuarter());
            //变更网点余额
            servicePointFinance = new ServicePointFinance();
            servicePointFinance.setId(servicePointWithdraw.getServicePoint().getId());
            servicePointFinance.setTotalAmount(servicePointWithdraw.getApplyAmount());
            servicePointFinance.setBalance(0 - servicePointWithdraw.getApplyAmount() + servicePointWithdraw.getPlatformFee());
            servicePointFinance.setPlatformFee(servicePointWithdraw.getPlatformFee());
            //更新最后付款时间
            servicePointFinance.setLastPayDate(servicePointWithdraw.getCreateDate());
            //更新最后付款金额
            servicePointFinance.setLastPayAmount(servicePointWithdraw.getApplyAmount());
            //即结,减少即结余额
            if (servicePointWithdraw.getPaymentType().intValue() == 20) {
                servicePointFinance.setDailyBalance(0 - servicePointWithdraw.getApplyAmount() + servicePointWithdraw.getPlatformFee());
            }
            servicePointDao.payServicePoint(servicePointFinance);
            if (bankIssueValue != 0) {
                servicePointFinance.setBankIssue(new Dict(0, ""));
//                servicePointDao.updateBankIssue(servicePointFinance);
                servicePointService.updateBankIssue(servicePointFinance);  // add on 2019-9-17
            }
            //更新缓存
            // mark on 2020-4-21 begin
            // 注释原因：无用代码
            /*
            ServicePoint cachedServicePoint = servicePointService.getFromCache(servicePointWithdraw.getServicePoint().getId());
            if (cachedServicePoint != null) {
                cachedServicePoint.getFinance().setBankIssue(new Dict("0", ""));//2018/08/05 Ryan 付款成功未同步付款失败原因
                cachedServicePoint.getFinance().setLastPayDate(servicePointFinance.getLastPayDate());
                cachedServicePoint.getFinance().setLastPayAmount(servicePointFinance.getLastPayAmount());
                cachedServicePoint.getFinance().setBalance(createEngineerCurrencyMessageBuilder.getBalance());
                //servicePointService.updateServicePointCache(cachedServicePoint);  //mark on 2020-1-14  web端去servicePoint
            }
            */
            // mark on 2020-4-21 end

            // add on 2020-5-4 begin
            // 更新网点财务缓存
            ServicePointFinance cachedServicePointFinance = servicePointFinanceService.getFromCache(servicePointWithdraw.getServicePoint().getId());
            if (cachedServicePointFinance != null) {
                cachedServicePointFinance.setBankIssue(new Dict("0", ""));//2018/08/05 Ryan 付款成功未同步付款失败原因
                cachedServicePointFinance.setLastPayDate(servicePointFinance.getLastPayDate());
                cachedServicePointFinance.setLastPayAmount(servicePointFinance.getLastPayAmount());
                cachedServicePointFinance.setBalance(Double.parseDouble(String.format("%.2f",createEngineerCurrencyMessageBuilder.getBalance())));
                servicePointFinanceService.updateCache(cachedServicePointFinance);
            }
            // add on 2020-5-4 end


            //增加付款金额
            servicePointPaidMonthly = new ServicePointPaidMonthly();
            servicePointPaidMonthly.setServicePoint(servicePointWithdraw.getServicePoint());
            servicePointPaidMonthly.setYear(servicePointWithdraw.getPayForYear());
            servicePointPaidMonthly.setMonth(servicePointWithdraw.getPayForMonth());
            servicePointPaidMonthly.setAmount(servicePointWithdraw.getApplyAmount() - servicePointWithdraw.getPlatformFee());
            servicePointPaidMonthly.setPaymentType(servicePointWithdraw.getPaymentType());
            servicePointPaidMonthly.setId((servicePointPaidMonthly.getServicePoint().getId() * 10000 + servicePointPaidMonthly.getYear()) * 100 + servicePointPaidMonthly.getPaymentType());
            servicePointPaidMonthlyDao.incrAmount(servicePointPaidMonthly);
            //增加出帐金额
            servicePointInvoiceMonthly = new ServicePointInvoiceMonthly();
            servicePointInvoiceMonthly.setServicePoint(servicePointWithdraw.getServicePoint());
            servicePointInvoiceMonthly.setYear(DateUtils.getYear(servicePointWithdraw.getCreateDate()));
            servicePointInvoiceMonthly.setMonth(DateUtils.getMonth(servicePointWithdraw.getCreateDate()));
            servicePointInvoiceMonthly.setAmount(servicePointWithdraw.getApplyAmount() - servicePointWithdraw.getPlatformFee());
            servicePointInvoiceMonthly.setPaymentType(servicePointWithdraw.getPaymentType());
            servicePointInvoiceMonthly.setId((servicePointInvoiceMonthly.getServicePoint().getId() * 10000 + servicePointInvoiceMonthly.getYear()) * 100 + servicePointInvoiceMonthly.getPaymentType());
            servicePointInvoiceMonthlyDao.incrAmount(servicePointInvoiceMonthly);

            //修改提现状态
            servicePointWithdraw.setPayAmount(servicePointWithdraw.getApplyAmount());
            servicePointWithdraw.setPayBy(createBy);
            servicePointWithdraw.setPayDate(createDate);
            servicePointWithdraw.setStatus(ServicePointWithdraw.SPW_STATUS_SUCCESS);
            servicePointWithdraw.setUpdateBy(createBy);
            servicePointWithdraw.setUpdateDate(createDate);
            dao.update(servicePointWithdraw);

            //扣除网点报表余额
            ServicePointPayableMonthly servicePointPayableMonthly = new ServicePointPayableMonthly();
            servicePointPayableMonthly.setServicePoint(servicePointWithdraw.getServicePoint());
            servicePointPayableMonthly.setPaymentType(servicePointWithdraw.getPaymentType());
            servicePointPayableMonthly.setYear(DateUtils.getYear(servicePointWithdraw.getCreateDate()));
            servicePointPayableMonthly.setMonth(DateUtils.getMonth(servicePointWithdraw.getCreateDate()));
            servicePointPayableMonthly.setId((servicePointPayableMonthly.getServicePoint().getId() * 10000 + servicePointPayableMonthly.getYear()) * 100 + servicePointPayableMonthly.getPaymentType());
            servicePointPayableMonthly.setAmount(0 - servicePointWithdraw.getApplyAmount() + servicePointWithdraw.getPlatformFee());
            servicePointBalanceMonthlyDao.incrBalance(servicePointPayableMonthly);

            //生成按品类金额异动消息
            MQCreateEngineerCurrencyMessage.IncreaseAmountByProductCategory increaseAmountByProductCategory = MQCreateEngineerCurrencyMessage.IncreaseAmountByProductCategory.newBuilder()
                    .setPaymentType(servicePointWithdraw.getPaymentType())
                    .setPayForYear(servicePointPaidMonthly.getYear())
                    .setPayForMonth(servicePointPaidMonthly.getMonth())
                    .setInvoiceYear(servicePointInvoiceMonthly.getYear())
                    .setInvoiceMonth(servicePointInvoiceMonthly.getMonth())
                    .setAmount(servicePointPaidMonthly.getAmount())
                    .build();
            createEngineerCurrencyMessageBuilder.setIncreaseAmountByProductCategory(increaseAmountByProductCategory);
            //按品类拆分抵扣款
            if (servicePointWithdraw.getDeductionAmount() != 0) {
                createEngineerCurrencyMessageBuilder.addAllDeductionByProductCategories(deductionByProductCategories);
            }

            //发送生成网点流水消息
            MQCreateEngineerCurrencyMessage.CreateEngineerCurrencyMessage createEngineerCurrencyMessage = createEngineerCurrencyMessageBuilder.build();
            try {
                createEngineerCurrencySender.send(createEngineerCurrencyMessage);
            } catch (Exception e) {
                LogUtils.saveLog("财务付款", "FI:ServicePointWithdraw.confirmSelectedSuccess.currency", new JsonFormat().printToString(createEngineerCurrencyMessage), e, createBy);
            }
            //发送更新网点抵扣消息
            if (servicePointWithdraw.getDeductionAmount() != 0) {
                MQCreateUpdateServicePointDeductedMessage.CreateUpdateServicePointDeductedMessage createUpdateServicePointDeductedMessage = createUpdateServicePointDeductedMessageBuilder.build();
                try {
                    createUpdateServicePointDeductedSender.send(createUpdateServicePointDeductedMessage);
                } catch (Exception e) {
                    LogUtils.saveLog("财务付款", "FI:ServicePointWithdraw.confirmSelectedSuccess.deducted", new JsonFormat().printToString(createUpdateServicePointDeductedMessage), e, createBy);
                }
            }
            //发送打款通知
            try {
                //ServicePoint servicePointInfo = servicePointService.getFromCache(servicePointWithdraw.getServicePoint().getId());  // mark on 2020-4-21  //注释原因：返回不必要的数据
                ServicePoint servicePointInfo = servicePointService.getSimpleFromCacheById(servicePointWithdraw.getServicePoint().getId());  // add on 2020-4-21
                if (servicePointInfo.getPrimary() != null && servicePointInfo.getPrimary().getId() != null) {
                    Engineer primaryEngineer = servicePointService.getEngineerFromCache(servicePointInfo.getId(), servicePointInfo.getPrimary().getId());
                    if (primaryEngineer != null && primaryEngineer.getAccountId() != null) {
                        String content = servicePointWithdraw.getPlatformFee() == 0
                                ? String.format("您好,已向您%s的账号付款%.2f元,请注意查收(到账时间可能略有差异)",
                                (servicePointWithdraw.getBankNo() == null || servicePointWithdraw.getBankNo().length() < 5 ? "" : "尾号为" + servicePointWithdraw.getBankNo().substring(servicePointWithdraw.getBankNo().length() - 4, servicePointWithdraw.getBankNo().length())),
                                servicePointWithdraw.getApplyAmount())
                                : String.format("您好,已向您%s的账号付款%.2f元(扣除平台服务费%.2f元,实际付款%.2f元),请注意查收(到账时间可能略有差异)",
                                (servicePointWithdraw.getBankNo() == null || servicePointWithdraw.getBankNo().length() < 5 ? "" : "尾号为" + servicePointWithdraw.getBankNo().substring(servicePointWithdraw.getBankNo().length() - 4, servicePointWithdraw.getBankNo().length())),
                                servicePointWithdraw.getApplyAmount() - servicePointWithdraw.getPlatformFee(), 0 - servicePointWithdraw.getPlatformFee(), servicePointWithdraw.getApplyAmount());
                        PushMessageUtils.push(AppPushMessage.PassThroughType.NOTIFICATION, AppMessageType.PAY, "打款通知", content, primaryEngineer.getAccountId());
                    }
                }
            } catch (Exception e) {

            }
        }
    }

    /**
     * 付款确认失败
     *
     * @param withdrawId
     * @param servicePointId
     */
    @Transactional()
    public void servicePointConfirmFail(Long withdrawId, Long servicePointId, String bankIssueValue) {
        User createBy = UserUtils.getUser();
        Date createDate = new Date();

        //变更提现状态
        ServicePointWithdraw spw = dao.getForConfirm(withdrawId);
        if (spw.getStatus() == 30 || spw.getStatus() == 40) {
            throw new RuntimeException("该银行已经确认,请重新刷新页面.");
        }
        ServicePointWithdraw servicePointWithdraw = new ServicePointWithdraw();
        servicePointWithdraw.setId(withdrawId);
        servicePointWithdraw.setStatus(ServicePointWithdraw.SPW_STATUS_FAIL);
        servicePointWithdraw.setUpdateBy(createBy);
        servicePointWithdraw.setUpdateDate(createDate);
        servicePointWithdraw.setQuarter(spw.getQuarter());
        dao.update(servicePointWithdraw);

        //设置异常原因
        ServicePointFinance servicePointFinance = new ServicePointFinance();
        servicePointFinance.setId(servicePointId);
        servicePointFinance.setBankIssue(new Dict(bankIssueValue));
//        servicePointDao.updateBankIssue(servicePointFinance);
        servicePointService.updateBankIssue(servicePointFinance);  // add on 2019-9-17
        servicePointDao.updateBankIssueFI(servicePointFinance);

        MQCreateUpdateServicePointDeductedMessage.CreateUpdateServicePointDeductedMessage.Builder createUpdateServicePointDeductedMessageBuilder = null;
        if (spw.getDeductionAmount() != 0) {
            createUpdateServicePointDeductedMessageBuilder = MQCreateUpdateServicePointDeductedMessage.CreateUpdateServicePointDeductedMessage.newBuilder();
            MQCreateUpdateServicePointDeductedMessage.CreateUpdateServicePointDeducted createUpdateServicePointDeducted = MQCreateUpdateServicePointDeductedMessage.CreateUpdateServicePointDeducted.newBuilder()
                    .setWithdrawId(withdrawId)
                    .setActionType(ServicePointDeducted.ACTION_TYPE_UPDATE)
                    .setStatus(ServicePointWithdraw.SPW_STATUS_FAIL)
                    .setUpdateById(createBy.getId())
                    .setUpdateDate(createDate.getTime())
                    .build();
            createUpdateServicePointDeductedMessageBuilder.addCreateUpdateServicePointDeducted(createUpdateServicePointDeducted);

            MQCreateUpdateServicePointDeductedMessage.CreateUpdateServicePointDeductedDetail createUpdateServicePointDeductedDetail = MQCreateUpdateServicePointDeductedMessage.CreateUpdateServicePointDeductedDetail.newBuilder()
                    .setWithdrawId(withdrawId)
                    .setActionType(ServicePointDeducted.ACTION_TYPE_UPDATE)
                    .setStatus(ServicePointWithdraw.SPW_STATUS_FAIL)
                    .setUpdateById(createBy.getId())
                    .setUpdateDate(createDate.getTime())
                    .build();
            createUpdateServicePointDeductedMessageBuilder.addCreateUpdateServicePointDeductedDetail(createUpdateServicePointDeductedDetail);
        }

        //生成网点资金异动流水
        long engineerCurrencyId = SeqUtils.NextIDValue(SeqUtils.TableName.EngineerCurrency);
        MQCreateEngineerCurrencyMessage.CreateEngineerCurrencyMessage createEngineerCurrencyMessage = MQCreateEngineerCurrencyMessage.CreateEngineerCurrencyMessage.newBuilder()
                .setMessageId(engineerCurrencyId)
                .setServicePointId(servicePointId)
                .setCurrencyType(EngineerCurrency.CURRENCY_TYPE_NONE)
                .setCurrencyNo(spw.getWithdrawNo())
                .setBeforeBalance(0)
                .setBalance(0)
                .setAmount(spw.getApplyAmount())
                .setPaymentType(EngineerCurrency.PAYMENT_TYPE_TRANSFER_ACCOUNT)
                .setActionType(EngineerCurrency.ACTION_TYPE_PAY_APPLY)
                .setCreateById(createBy.getId())
                .setCreateDate(createDate.getTime())
                .setRemarks("银行付款失败,付款金额：".concat(String.valueOf(spw.getApplyAmount())).concat(",").concat(MSDictUtils.getDictLabel(bankIssueValue, "BankIssueType", "")))
                .setQuarter(spw.getQuarter())
                .build();

        //更新缓存
        // mark on 2020-4-22 begin
        // 注释原因： 无用代码
        /*
        ServicePoint cachedServicePoint = servicePointService.getFromCache(spw.getServicePoint().getId());
        if (cachedServicePoint != null) {
            Dict bankIssue = MSDictUtils.getDictByValue(bankIssueValue, "BankIssueType");//切换为微服务
            if (bankIssue == null) {
                bankIssue = new Dict(bankIssueValue, "付款失败原因");
            }
            cachedServicePoint.getFinance().setBankIssue(bankIssue);
            //servicePointService.updateServicePointCache(cachedServicePoint); //mark on 2020-1-14  web端去servicePoint
        }
         */
        // mark on 2020-4-22 end
        // add on 2020-5-4 begin
        // 更新网点财务缓存
        ServicePointFinance cachedServicePointFinance = servicePointFinanceService.getFromCache(spw.getServicePoint().getId());
        if (cachedServicePointFinance != null) {
            Dict bankIssue = MSDictUtils.getDictByValue(bankIssueValue, "BankIssueType");//切换为微服务
            if (bankIssue == null) {
                bankIssue = new Dict(bankIssueValue, "付款失败原因");
            }
            cachedServicePointFinance.setBankIssue(bankIssue);
            servicePointFinanceService.updateCache(cachedServicePointFinance); //mark on 2020-1-14  web端去servicePoint
        }
        // add on 2020-5-4 end
        //发送更新网点抵扣消息
        if (spw.getDeductionAmount() != 0) {
            MQCreateUpdateServicePointDeductedMessage.CreateUpdateServicePointDeductedMessage createUpdateServicePointDeductedMessage = createUpdateServicePointDeductedMessageBuilder.build();
            try {
                createUpdateServicePointDeductedSender.send(createUpdateServicePointDeductedMessage);
            } catch (Exception e) {
                LogUtils.saveLog("财务付款", "FI:ServicePointWithdraw.confirmFail.deducted", new JsonFormat().printToString(createUpdateServicePointDeductedMessage), e, createBy);
            }
        }
        //发送生成网点流水消息
        try {
            createEngineerCurrencySender.send(createEngineerCurrencyMessage);
        } catch (Exception e) {
            LogUtils.saveLog("财务付款", "FI:ServicePointWithdraw.confirmFail.currency", new JsonFormat().printToString(createEngineerCurrencyMessage), e, createBy);
        }
    }

    /**
     * 网点付款修改
     *
     * @param withdrawId
     * @param bank
     * @param branch
     * @param bankNo
     * @param bankOwner
     * @param payDate
     */
    @Transactional()
    public void servicePointConfirmEdit(Long withdrawId, Integer bank, String branch, String bankNo, String bankOwner, Date payDate) {
        User createBy = UserUtils.getUser();
        Date createDate = new Date();

        //修改提现记录
        ServicePointWithdraw oldSpw = dao.get(withdrawId);
        ServicePointWithdraw servicePointWithdraw = new ServicePointWithdraw();
        servicePointWithdraw.setId(withdrawId);
        servicePointWithdraw.setBank(bank);
        servicePointWithdraw.setBranch(branch);
        servicePointWithdraw.setBankNo(bankNo);
        servicePointWithdraw.setBankOwner(bankOwner);
        servicePointWithdraw.setPayDate(payDate);
        servicePointWithdraw.setUpdateBy(createBy);
        servicePointWithdraw.setUpdateDate(createDate);
        servicePointWithdraw.setQuarter(oldSpw.getQuarter());
        dao.update(servicePointWithdraw);

        int oldYear = DateUtils.getYear(oldSpw.getPayDate());
        int oldMonth = DateUtils.getMonth(oldSpw.getPayDate());
        int year = DateUtils.getYear(payDate);
        int month = DateUtils.getMonth(payDate);

        if (oldYear != year || oldMonth != month) {

            //减少原出帐金额
            ServicePointInvoiceMonthly oldSpm = new ServicePointInvoiceMonthly();
            oldSpm.setServicePoint(oldSpw.getServicePoint());
            oldSpm.setPaymentType(oldSpw.getPaymentType());
            oldSpm.setAmount(0 - oldSpw.getPayAmount());
            oldSpm.setYear(oldYear);
            oldSpm.setMonth(oldMonth);
            oldSpm.setId((oldSpm.getServicePoint().getId() * 10000 + oldSpm.getYear()) * 100 + oldSpm.getPaymentType());
            servicePointInvoiceMonthlyDao.incrAmount(oldSpm);

            //累计网点报表余额
            ServicePointPayableMonthly oldPayable = new ServicePointPayableMonthly();
            oldPayable.setServicePoint(oldSpw.getServicePoint());
            oldPayable.setPaymentType(oldSpw.getPaymentType());
            oldPayable.setYear(oldYear);
            oldPayable.setMonth(oldMonth);
            oldPayable.setId((oldPayable.getServicePoint().getId() * 10000 + oldPayable.getYear()) * 100 + oldPayable.getPaymentType());
            oldPayable.setAmount(oldSpw.getPayAmount());
            servicePointBalanceMonthlyDao.incrBalance(oldPayable);

            //新增出帐金额
            ServicePointInvoiceMonthly spm = new ServicePointInvoiceMonthly();
            spm.setServicePoint(oldSpw.getServicePoint());
            spm.setPaymentType(oldSpw.getPaymentType());
            spm.setAmount(oldSpw.getPayAmount());
            spm.setYear(year);
            spm.setMonth(month);

            Long spmId = servicePointInvoiceMonthlyDao.getOneId(spm);
            if (spmId == null || spmId == 0) {
                spmId = SeqUtils.NextIDValue(SeqUtils.TableName.ServicePointPayableMonthly);
                spm.setId(spmId);
                servicePointInvoiceMonthlyDao.insert(spm);
            }
            spm.setId((spm.getServicePoint().getId() * 10000 + spm.getYear()) * 100 + spm.getPaymentType());
            servicePointInvoiceMonthlyDao.incrAmount(spm);

            //扣除网点报表余额
            ServicePointPayableMonthly newPayable = new ServicePointPayableMonthly();
            newPayable.setServicePoint(oldSpw.getServicePoint());
            newPayable.setPaymentType(oldSpw.getPaymentType());
            newPayable.setYear(year);
            newPayable.setMonth(month);
            newPayable.setId((newPayable.getServicePoint().getId() * 10000 + newPayable.getYear()) * 100 + newPayable.getPaymentType());
            newPayable.setAmount(0 - oldSpw.getPayAmount());
            servicePointBalanceMonthlyDao.incrBalance(newPayable);

            //生成网点资金异动流水
            long engineerCurrencyId = SeqUtils.NextIDValue(SeqUtils.TableName.EngineerCurrency);
            MQCreateEngineerCurrencyMessage.CreateEngineerCurrencyMessage createEngineerCurrencyMessage = MQCreateEngineerCurrencyMessage.CreateEngineerCurrencyMessage.newBuilder()
                    .setMessageId(engineerCurrencyId)
                    .setServicePointId(oldSpw.getServicePoint().getId())
                    .setCurrencyType(EngineerCurrency.CURRENCY_TYPE_NONE)
                    .setCurrencyNo(servicePointWithdraw.getWithdrawNo())
                    .setBeforeBalance(0)
                    .setBalance(0)
                    .setAmount(oldSpw.getApplyAmount())
                    .setPaymentType(EngineerCurrency.PAYMENT_TYPE_TRANSFER_ACCOUNT)
                    .setActionType(EngineerCurrency.ACTION_TYPE_PAY_APPLY)
                    .setCreateById(createBy.getId())
                    .setCreateDate(createDate.getTime())
                    .setRemarks("修改付款日期：(".concat(DateUtils.formatDate(oldSpw.getPayDate())).concat("->").concat(DateUtils.formatDate(payDate)))
                    .setQuarter(oldSpw.getQuarter())
                    .build();
            //发送生成网点流水消息
            try {
                createEngineerCurrencySender.send(createEngineerCurrencyMessage);
            } catch (Exception e) {
                LogUtils.saveLog("财务付款", "FI:ServicePointWithdraw.confirmEdit.currency", new JsonFormat().printToString(createEngineerCurrencyMessage), e, createBy);
            }
        }
    }

    /**
     * 提现申请
     *
     * @param applyType
     * @param servicePointId
     * @param applyAmount
     * @param applyDate
     */
    @Transactional()
    public void servicePointApply(String applyType, Long servicePointId, double applyAmount, Date applyDate) {
        int withdrawType = 0;
        switch (applyType) {
            case "KF":
                withdrawType = SPW_WITHDRAW_TYPE_KF;
                break;
            case "SP":
                withdrawType = SPW_WITHDRAW_TYPE_SP;
                break;
            case "APP":
                withdrawType = SPW_WITHDRAW_TYPE_APP;
                break;
        }
        if (withdrawType == 0) {
            throw new RuntimeException("无效的提款类别.");
        }

        Long processId = dao.getProcessOneId(servicePointId);
        if (processId != null) {
            throw new RuntimeException("您已经有提现操作处理中,完成后可再次提现.");
        }

        User createBy = UserUtils.getUser();
        ServicePointFinance servicePointFinance;

        //计算费用
        double platformFee = CurrencyUtil.round2(applyAmount * CurrencyUtil.platformFeeRate);
        applyAmount = applyAmount - platformFee;

        ServicePoint sp = servicePointService.getFromCache(servicePointId);

        //生成提款ID
        Long servicePointWithdrawId = SeqUtils.NextIDValue(SeqUtils.TableName.ServicePointWithdraw);
        //生成提款编号
        String withdrawNo = SeqUtils.NextSequenceNo("ServicePointWithdrawNo");

        //生成提款记录
        servicePointFinance = servicePointDao.getFinance(servicePointId);
        ServicePointWithdraw servicePointWithdraw = new ServicePointWithdraw();
        servicePointWithdraw.setId(servicePointWithdrawId);
        servicePointWithdraw.setWithdrawNo(withdrawNo);
        servicePointWithdraw.setServicePoint(new ServicePoint(servicePointId));
        servicePointWithdraw.setPaymentType(PaymentType.IMMEDIATELY.value);
        servicePointWithdraw.setStatus(ServicePointWithdraw.SPW_STATUS_NEW);
        servicePointWithdraw.setWithdrawType(withdrawType);
        servicePointWithdraw.setBank(Integer.parseInt(servicePointFinance.getBank().getValue()));
        servicePointWithdraw.setBranch(servicePointFinance.getBranch());
        servicePointWithdraw.setBankNo(servicePointFinance.getBankNo());
        servicePointWithdraw.setBankOwner(servicePointFinance.getBankOwner());
        servicePointWithdraw.setBeforeBalance(servicePointFinance.getBalance());
        servicePointWithdraw.setBeforeDebts(servicePointFinance.getDebtsAmount());
        servicePointWithdraw.setApplyAmount(applyAmount);
        servicePointWithdraw.setPayAmount(0d);
        servicePointWithdraw.setDebtsDeduction(0d);
        servicePointWithdraw.setSetDeductionAmount(0d);
        servicePointWithdraw.setPlatformFee(platformFee);
        servicePointWithdraw.setCreateBy(createBy);
        servicePointWithdraw.setCreateDate(applyDate);
        servicePointWithdraw.setPayForYear(DateUtils.getYear(applyDate));
        servicePointWithdraw.setPayForMonth(DateUtils.getMonth(applyDate));
        servicePointWithdraw.setRemarks("");
        servicePointWithdraw.setQuarter(QuarterUtils.getSeasonQuarter(applyDate));

        dao.insert(servicePointWithdraw);
    }
}
