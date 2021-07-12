package com.wolfking.jeesite.modules.fi.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kkl.kklplus.entity.md.MDServicePointViewModel;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.service.LongIDCrudService;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.wolfking.jeesite.modules.fi.dao.EngineerChargeDao;
import com.wolfking.jeesite.modules.fi.dao.EngineerCurrencyDao;
import com.wolfking.jeesite.modules.fi.dao.ServicePointFinanceDao;
import com.wolfking.jeesite.modules.fi.entity.EngineerCharge;
import com.wolfking.jeesite.modules.fi.entity.EngineerCurrency;
import com.wolfking.jeesite.modules.md.dao.ServicePointDao;
import com.wolfking.jeesite.modules.md.entity.Engineer;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.md.entity.ServicePointFinance;
import com.wolfking.jeesite.modules.md.service.ServicePointService;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.ms.providermd.service.MSServicePointService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Jeff on 2017/4/20.
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Slf4j
public class ServicePointCurrencyService extends LongIDCrudService<EngineerCurrencyDao, EngineerCurrency> {
    @Resource
    private EngineerCurrencyDao engineerCurrencyDao;
    @Resource
    private ServicePointDao servicePointDao;
    @Resource
    private EngineerChargeDao engineerChargeDao;

    @Autowired
    private MSServicePointService msServicePointService;

    @Autowired
    private ServicePointService servicePointService;

    @Resource
    private ServicePointFinanceDao servicePointFinanceDao;

    /**
     *
     * @param servicePointId
     * @param actionType
     * @param beginDate
     * @param endDate
     * @param currencyNo
     * @param page
     * @return
     */
    public Page<EngineerCurrency> getServicePointCurrencyList(Long servicePointId, Integer actionType, Date beginDate, Date endDate, String currencyNo,
                                                              Page<EngineerCurrency> page) {
        //List<EngineerCurrency> list =  engineerCurrencyDao.getServicePointCurrencyList(servicePointId, actionType, beginDate, endDate, currencyNo, page);  // mark on 2019-9-24
        List<EngineerCurrency> list =  getServicePointCurrencyListWithoutServicePointAndEngineer(servicePointId, actionType, beginDate, endDate, currencyNo, page); // add on 2019-9-24
        List<Long> ids = list.stream().map(ec -> ec.getServicePoint().getId()).distinct().collect(Collectors.toList());
        List<ServicePointFinance> financeList = servicePointDao.getBalanceByIds(ids);
        list.forEach(ec -> {
            ServicePointFinance finance = financeList.stream().filter(f -> f.getId().equals(ec.getServicePoint().getId())).findFirst().orElse(null);
            if (finance != null){
                ec.getServicePoint().getFinance().setBalance(finance.getBalance());
            }
        });
        page.setList(list);
        return page;
    }

    /**
     *
     * @param servicePointId
     * @param actionType
     * @param beginDate
     * @param endDate
     * @param currencyNo
     * @param page
     * @return
     */
    public Page<EngineerCurrency> getServicePointCurrencyListVerSecond(Long servicePointId, Integer actionType, Date beginDate, Date endDate, String currencyNo,
                                                              Page<EngineerCurrency> page) {
        // add on 2021-3-25
        List<EngineerCurrency> list =  getServicePointCurrencyListWithoutServicePointAndEngineer(servicePointId, actionType, beginDate, endDate, currencyNo, page);
        List<Long> ids = list.stream().map(ec -> ec.getServicePoint().getId()).distinct().collect(Collectors.toList());
        List<ServicePointFinance> financeList = servicePointFinanceDao.getBalanceByIds(ids);
        Map<Long,ServicePointFinance> financeMap = CollectionUtils.isEmpty(financeList)?Maps.newHashMap():financeList.stream().collect(Collectors.toMap(ServicePointFinance::getId,r->r));
        list.forEach(ec -> {
            ServicePointFinance finance = financeMap.get(ec.getServicePoint().getId());
            if (finance != null){
                ec.getServicePoint().getFinance().setBalance(finance.getBalance());
            }
        });
        page.setList(list);
        return page;
    }

    /**
     * 去掉ServicePoint关联及相关的Engineer //add on 2019-9-24
     * 基础资料->网点管理->账户明细
     * @param servicePointId
     * @param actionType
     * @param beginDate
     * @param endDate
     * @param currencyNo
     * @param page
     * @return
     */
    public List<EngineerCurrency> getServicePointCurrencyListWithoutServicePointAndEngineer(Long servicePointId, Integer actionType, Date beginDate, Date endDate, String currencyNo,
                                                                                            Page<EngineerCurrency> page) {
        List<EngineerCurrency> list = engineerCurrencyDao.getServicePointCurrencyListWithoutServicePointAndEngineer(servicePointId, actionType, beginDate, endDate, currencyNo, page);
        // 获取servicePoint的id
        List<Long> servicePointIds = list.stream().map(ec -> ec.getServicePoint().getId()).distinct().collect(Collectors.toList());

        // 到微服务中根据id找网点
        // add on 2019-10-14 begin
        List<Long> engineerIds = Lists.newArrayList();
        String[] fieldArray = new String[]{"id","servicePointNo","name","paymentType","primaryId"};
        Map<Long, MDServicePointViewModel> servicePointViewModelMap = msServicePointService.findBatchByIdsByConditionToMap(servicePointIds, Arrays.asList(fieldArray), null, ((engineerIdsFromMS)->
                engineerIds.addAll(engineerIdsFromMS)
        ));
        // add on 2019-10-14 end

        // 获取安维人员的数据
        List<Engineer> engineerList = engineerIds!= null && engineerIds.size()>0? servicePointService.findAllEngineersName(engineerIds,Arrays.asList("id","name")):Lists.newArrayList();
        Map<Long, String> engineerMap = Maps.newHashMap();
        if (engineerList != null && !engineerList.isEmpty()) {
            engineerMap = engineerList.stream().collect(Collectors.toMap(Engineer::getId, Engineer::getName));
        }

        // add on 2019-10-14 begin
        final Map<Long, String> finalEngineerMap = engineerMap;
        list.stream().forEach(engineerCurrency -> {
            MDServicePointViewModel servicePointVM = servicePointViewModelMap.get(engineerCurrency.getServicePoint().getId());
            if (servicePointVM != null) {
                engineerCurrency.getServicePoint().setServicePointNo(servicePointVM.getServicePointNo());
                engineerCurrency.getServicePoint().setName(servicePointVM.getName());

                // 网点财务信息
                ServicePointFinance servicePointFinance = new ServicePointFinance();
                servicePointFinance.setBalance(0);
                servicePointFinance.setPaymentType(new Dict(Optional.ofNullable(servicePointVM.getPaymentType()).orElse(0),""));
                engineerCurrency.getServicePoint().setFinance(servicePointFinance);

                // 查找安维人员信息
                Engineer engineer = new Engineer();
                if (servicePointVM.getPrimaryId() != null) {
                    String engineerName = finalEngineerMap.get(servicePointVM.getPrimaryId());
                    engineer.setName(engineerName);
                }
                engineerCurrency.getServicePoint().setPrimary(engineer);
            }
        });
        // add on 2019-10-14 end
        return list;
    }

    public Page<EngineerCurrency> getServicePointCurrencyListForApi(Long servicePointId, Integer actionType, Date beginDate, Date endDate, String currencyNo,
                                                              Page<EngineerCurrency> page) {
        // add on 2019-11-16 begin
        Date[] dates = OrderUtils.getQuarterDates(beginDate, endDate, 0, 0);
        List<String> quarters = QuarterUtils.getQuarters(dates[0], dates[1]);
        // add on 2019-11-16 end
        List<EngineerCurrency> list =  engineerCurrencyDao.getServicePointCurrencyListForApi(servicePointId, actionType, beginDate, endDate, currencyNo, quarters, page);
        page.setList(list);
        return page;
    }

    /**
     * 网点帐户明细按月汇总
     * @param servicePointId
     * @param actionType
     * @param beginDate
     * @param endDate
     * @return
     */
    public List<Map<String,Object>> getServicePointCurrencySummryByMonthApi(Long servicePointId, Integer actionType, Date beginDate, Date endDate) {
         return engineerCurrencyDao.getServicePointCurrencySummryByMonthApi(servicePointId, actionType, beginDate, endDate);
    }

    public List<EngineerCurrency> getModifyList(){
        return engineerCurrencyDao.getModifyList();
    }

//    @Transactional()
//    public void updateCurrency(HashMap<String, Object> map){
//        engineerCurrencyDao.updateMSF(map);
//        engineerCurrencyDao.updateFSPM(map);
//        engineerCurrencyDao.updateFSIM(map);
//        engineerCurrencyDao.updateFSBM(map);
//        engineerCurrencyDao.updateCurrency(map);
//        engineerCurrencyDao.deleteById(map);
//    }

    @Transactional()
    public void insertNotExistCurrency(){
        List<EngineerCharge> chargeNotExistCurrencyList = engineerChargeDao.getNotExistCurrencyList();
        List<Long> distinctServiceIdList = chargeNotExistCurrencyList.stream().map(t->t.getServicePoint().getId()).distinct().collect(Collectors.toList());
        List<EngineerCharge> chargeList = Lists.newArrayList();

        double totalAmount = 0;
        boolean canUpdate = false;

        int totalInsert = 0;
        int canUpdateCount = 0;
        int canNotUpdateCount = 0;

        for (int ii = distinctServiceIdList.size() - 1; ii >= 0; ii--){
            Long servicePointId = distinctServiceIdList.get(ii);
            for (int index = chargeNotExistCurrencyList.size() - 1; index >= 0; index--){
                EngineerCharge ec = chargeNotExistCurrencyList.get(index);
                if (ec.getServicePoint().getId().equals(servicePointId)){
                    totalAmount = totalAmount + ec.getServiceCharge() + ec.getTravelCharge() + ec.getExpressCharge() + ec.getMaterialCharge() + ec.getOtherCharge();
                    chargeList.add(0, ec);
                    chargeNotExistCurrencyList.remove(ec);
                }else{
                    break;
                }
            }
            //region 1条
            if (chargeList.size() == 1){
                EngineerCharge ec = chargeList.get(0);
                EngineerCurrency previousCurrency = engineerCurrencyDao.getOnePrevious(ec.getId(), ec.getServicePoint().getId());
                if (previousCurrency == null){
                    EngineerCurrency nextCurrency = engineerCurrencyDao.getOneNext(ec.getId(), ec.getServicePoint().getId());
                    if (nextCurrency == null){
                        if (ec.getChargeOrderType() == 0) {
                            EngineerCurrency newCurrency = new EngineerCurrency();
                            newCurrency.setId(ec.getId() + 1);
                            newCurrency.setServicePoint(ec.getServicePoint());
                            newCurrency.setCurrencyType(EngineerCurrency.CURRENCY_TYPE_IN);
                            newCurrency.setCurrencyNo(ec.getOrderNo());
                            newCurrency.setBeforeBalance(0d);
                            Double engineerTotalCharge = ec.getServiceCharge() +
                                    ec.getExpressCharge() +
                                    ec.getTravelCharge() +
                                    ec.getMaterialCharge() +
                                    ec.getOtherCharge();
                            newCurrency.setBalance(engineerTotalCharge);
                            newCurrency.setAmount(engineerTotalCharge);
                            newCurrency.setPaymentType(EngineerCurrency.PAYMENT_TYPE_ORDER_INVOICE);
                            newCurrency.setActionType(EngineerCurrency.ACTION_TYPE_CHARGE);
                            newCurrency.setCreateBy(ec.getCreateBy());
                            newCurrency.setCreateDate(ec.getCreateDate());
                            newCurrency.setRemarks("结帐转存:".concat(engineerTotalCharge.toString())
                                    .concat(" .服务费:")
                                    .concat(ec.getServiceCharge().toString())
                                    .concat(" ,快递费:")
                                    .concat(ec.getExpressCharge().toString())
                                    .concat(" ,远程费:")
                                    .concat(ec.getTravelCharge().toString())
                                    .concat(" ,配件费:")
                                    .concat(ec.getMaterialCharge().toString())
                                    .concat(" ,其他费:")
                                    .concat(ec.getOtherCharge().toString())
                                    .concat(" ,订单编号:")
                                    .concat(ec.getOrderNo()));
                            newCurrency.setQuarter(ec.getQuarter());
//                                engineerCurrencyDao.insert(newCurrency);
                            totalInsert++;
                        }else{
                            Double engineerTotalCharge = ec.getServiceCharge() +
                                    ec.getExpressCharge() +
                                    ec.getTravelCharge() +
                                    ec.getMaterialCharge() +
                                    ec.getOtherCharge();
                            EngineerCurrency newCurrency = new EngineerCurrency();
                            newCurrency.setId(ec.getId() + 1);
                            newCurrency.setServicePoint(ec.getServicePoint());
                            newCurrency.setCurrencyType(engineerTotalCharge == 0 ? EngineerCurrency.CURRENCY_TYPE_NONE : (engineerTotalCharge > 0 ? EngineerCurrency.CURRENCY_TYPE_IN : EngineerCurrency.CURRENCY_TYPE_OUT));
                            newCurrency.setCurrencyNo(ec.getOrderNo());
                            newCurrency.setBeforeBalance(0d);
                            newCurrency.setBalance(engineerTotalCharge);
                            newCurrency.setAmount(engineerTotalCharge);
                            newCurrency.setPaymentType(EngineerCurrency.PAYMENT_TYPE_WRITE_OFF);
                            newCurrency.setActionType(engineerTotalCharge == 0 ? EngineerCurrency.ACTION_TYPE_NONE : (engineerTotalCharge > 0 ? EngineerCurrency.ACTION_TYPE_REFUND : EngineerCurrency.ACTION_TYPE_REPLENISH));
                            newCurrency.setCreateBy(ec.getCreateBy());
                            newCurrency.setCreateDate(ec.getCreateDate());
                            newCurrency.setRemarks("退补款:".concat(engineerTotalCharge.toString())
                                    .concat(" ,订单编号:")
                                    .concat(ec.getOrderNo()));
                            newCurrency.setQuarter(ec.getQuarter());
//                                engineerCurrencyDao.insert(newCurrency);
                            totalInsert++;
                        }
                    }else{
                        if (ec.getChargeOrderType() == 0) {
                            EngineerCurrency newCurrency = new EngineerCurrency();
                            newCurrency.setId(ec.getId() + 1);
                            newCurrency.setServicePoint(ec.getServicePoint());
                            newCurrency.setCurrencyType(EngineerCurrency.CURRENCY_TYPE_IN);
                            newCurrency.setCurrencyNo(ec.getOrderNo());
                            Double engineerTotalCharge = ec.getServiceCharge() +
                                    ec.getExpressCharge() +
                                    ec.getTravelCharge() +
                                    ec.getMaterialCharge() +
                                    ec.getOtherCharge();
                            newCurrency.setBeforeBalance(nextCurrency.getBeforeBalance() - engineerTotalCharge);
                            newCurrency.setBalance(nextCurrency.getBeforeBalance());
                            newCurrency.setAmount(engineerTotalCharge);
                            newCurrency.setPaymentType(EngineerCurrency.PAYMENT_TYPE_ORDER_INVOICE);
                            newCurrency.setActionType(EngineerCurrency.ACTION_TYPE_CHARGE);
                            newCurrency.setCreateBy(ec.getCreateBy());
                            newCurrency.setCreateDate(ec.getCreateDate());
                            newCurrency.setRemarks("结帐转存:".concat(engineerTotalCharge.toString())
                                    .concat(" .服务费:")
                                    .concat(ec.getServiceCharge().toString())
                                    .concat(" ,快递费:")
                                    .concat(ec.getExpressCharge().toString())
                                    .concat(" ,远程费:")
                                    .concat(ec.getTravelCharge().toString())
                                    .concat(" ,配件费:")
                                    .concat(ec.getMaterialCharge().toString())
                                    .concat(" ,其他费:")
                                    .concat(ec.getOtherCharge().toString())
                                    .concat(" ,订单编号:")
                                    .concat(ec.getOrderNo()));
                            newCurrency.setQuarter(ec.getQuarter());
//                                engineerCurrencyDao.insert(newCurrency);
                            totalInsert++;
                        }else{
                            Double engineerTotalCharge = ec.getServiceCharge() +
                                    ec.getExpressCharge() +
                                    ec.getTravelCharge() +
                                    ec.getMaterialCharge() +
                                    ec.getOtherCharge();
                            EngineerCurrency newCurrency = new EngineerCurrency();
                            newCurrency.setId(ec.getId() + 1);
                            newCurrency.setServicePoint(ec.getServicePoint());
                            newCurrency.setCurrencyType(engineerTotalCharge == 0 ? EngineerCurrency.CURRENCY_TYPE_NONE : (engineerTotalCharge > 0 ? EngineerCurrency.CURRENCY_TYPE_IN : EngineerCurrency.CURRENCY_TYPE_OUT));
                            newCurrency.setCurrencyNo(ec.getOrderNo());
                            newCurrency.setBeforeBalance(nextCurrency.getBeforeBalance() + engineerTotalCharge);
                            newCurrency.setBalance(nextCurrency.getBeforeBalance());
                            newCurrency.setAmount(engineerTotalCharge);
                            newCurrency.setPaymentType(EngineerCurrency.PAYMENT_TYPE_WRITE_OFF);
                            newCurrency.setActionType(engineerTotalCharge == 0 ? EngineerCurrency.ACTION_TYPE_NONE : (engineerTotalCharge > 0 ? EngineerCurrency.ACTION_TYPE_REFUND : EngineerCurrency.ACTION_TYPE_REPLENISH));
                            newCurrency.setCreateBy(ec.getCreateBy());
                            newCurrency.setCreateDate(ec.getCreateDate());
                            newCurrency.setRemarks("退补款:".concat(engineerTotalCharge.toString())
                                    .concat(" ,订单编号:")
                                    .concat(ec.getOrderNo()));
                            newCurrency.setQuarter(ec.getQuarter());
//                                engineerCurrencyDao.insert(newCurrency);
                            totalInsert++;
                        }
                    }
                }else{
                    if (ec.getChargeOrderType() == 0) {
                        EngineerCurrency newCurrency = new EngineerCurrency();
                        newCurrency.setId(ec.getId() + 1);
                        newCurrency.setServicePoint(ec.getServicePoint());
                        newCurrency.setCurrencyType(EngineerCurrency.CURRENCY_TYPE_IN);
                        newCurrency.setCurrencyNo(ec.getOrderNo());
                        Double engineerTotalCharge = ec.getServiceCharge() +
                                ec.getExpressCharge() +
                                ec.getTravelCharge() +
                                ec.getMaterialCharge() +
                                ec.getOtherCharge();
                        newCurrency.setBeforeBalance(previousCurrency.getBalance());
                        newCurrency.setBalance(previousCurrency.getBalance()+engineerTotalCharge);
                        newCurrency.setAmount(engineerTotalCharge);
                        newCurrency.setPaymentType(EngineerCurrency.PAYMENT_TYPE_ORDER_INVOICE);
                        newCurrency.setActionType(EngineerCurrency.ACTION_TYPE_CHARGE);
                        newCurrency.setCreateBy(ec.getCreateBy());
                        newCurrency.setCreateDate(ec.getCreateDate());
                        newCurrency.setRemarks("结帐转存:".concat(engineerTotalCharge.toString())
                                .concat(" .服务费:")
                                .concat(ec.getServiceCharge().toString())
                                .concat(" ,快递费:")
                                .concat(ec.getExpressCharge().toString())
                                .concat(" ,远程费:")
                                .concat(ec.getTravelCharge().toString())
                                .concat(" ,配件费:")
                                .concat(ec.getMaterialCharge().toString())
                                .concat(" ,其他费:")
                                .concat(ec.getOtherCharge().toString())
                                .concat(" ,订单编号:")
                                .concat(ec.getOrderNo()));
                        newCurrency.setQuarter(ec.getQuarter());
//                                engineerCurrencyDao.insert(newCurrency);
                        totalInsert++;
                    }else{
                        Double engineerTotalCharge = ec.getServiceCharge() +
                                ec.getExpressCharge() +
                                ec.getTravelCharge() +
                                ec.getMaterialCharge() +
                                ec.getOtherCharge();
                        EngineerCurrency newCurrency = new EngineerCurrency();
                        newCurrency.setId(ec.getId() + 1);
                        newCurrency.setServicePoint(ec.getServicePoint());
                        newCurrency.setCurrencyType(engineerTotalCharge == 0 ? EngineerCurrency.CURRENCY_TYPE_NONE : (engineerTotalCharge > 0 ? EngineerCurrency.CURRENCY_TYPE_IN : EngineerCurrency.CURRENCY_TYPE_OUT));
                        newCurrency.setCurrencyNo(ec.getOrderNo());
                        newCurrency.setBeforeBalance(previousCurrency.getBalance());
                        newCurrency.setBalance(previousCurrency.getBalance() + engineerTotalCharge);
                        newCurrency.setAmount(engineerTotalCharge);
                        newCurrency.setPaymentType(EngineerCurrency.PAYMENT_TYPE_WRITE_OFF);
                        newCurrency.setActionType(engineerTotalCharge == 0 ? EngineerCurrency.ACTION_TYPE_NONE : (engineerTotalCharge > 0 ? EngineerCurrency.ACTION_TYPE_REFUND : EngineerCurrency.ACTION_TYPE_REPLENISH));
                        newCurrency.setCreateBy(ec.getCreateBy());
                        newCurrency.setCreateDate(ec.getCreateDate());
                        newCurrency.setRemarks("退补款:".concat(engineerTotalCharge.toString())
                                .concat(" ,订单编号:")
                                .concat(ec.getOrderNo()));
                        newCurrency.setQuarter(ec.getQuarter());
//                                engineerCurrencyDao.insert(newCurrency);
                        totalInsert++;
                    }
                }

                //TODO =1 直接以前一或后一为基准计算beforebalance与balance
                //TODO >1
                //TODO 最后currency beforebalance - 前一currencybalance = totalAmount;补全数据
                //TODO 查不到currency 直接补数据
                //TODO 其他情况先不做处理
            }

            //endregion 1条

            //region 多条
            else {
                EngineerCharge firstCharge = chargeList.get(0);
                EngineerCharge lastCharge = chargeList.get(chargeList.size() - 1);

                double previousAmount = 0;
                double nextAmount = 0;
                EngineerCurrency previousCurrency = engineerCurrencyDao.getOnePrevious(firstCharge.getId(), firstCharge.getServicePoint().getId());
                if (previousCurrency != null){
                    previousAmount = previousCurrency.getBalance();
                }
                EngineerCurrency nextCurrency = engineerCurrencyDao.getOneNext(lastCharge.getId(), lastCharge.getServicePoint().getId());
                if (nextCurrency != null){
                    nextAmount = nextCurrency.getBeforeBalance();
                }
                if ((nextAmount == 0 && previousAmount == 0) || (nextAmount-previousAmount == totalAmount)){
                    canUpdate = true;
                    canUpdateCount++;
                    log.info("========= can update =========={}",firstCharge.getServicePoint().getId());
                }else{
                    String s = "";
                    canNotUpdateCount++;
                    log.info("========= can not update =========={}",firstCharge.getServicePoint().getId());
                }

                if (canUpdate){
                    for (EngineerCharge ec : chargeList){
                        if (ec.getChargeOrderType() == 0) {
                            EngineerCurrency newCurrency = new EngineerCurrency();
                            newCurrency.setId(ec.getId() + 1);
                            newCurrency.setServicePoint(ec.getServicePoint());
                            newCurrency.setCurrencyType(EngineerCurrency.CURRENCY_TYPE_IN);
                            newCurrency.setCurrencyNo(ec.getOrderNo());
                            Double engineerTotalCharge = ec.getServiceCharge() +
                                    ec.getExpressCharge() +
                                    ec.getTravelCharge() +
                                    ec.getMaterialCharge() +
                                    ec.getOtherCharge();
                            newCurrency.setBeforeBalance(previousAmount);
                            newCurrency.setBalance(previousAmount+engineerTotalCharge);
                            newCurrency.setAmount(engineerTotalCharge);
                            newCurrency.setPaymentType(EngineerCurrency.PAYMENT_TYPE_ORDER_INVOICE);
                            newCurrency.setActionType(EngineerCurrency.ACTION_TYPE_CHARGE);
                            newCurrency.setCreateBy(ec.getCreateBy());
                            newCurrency.setCreateDate(ec.getCreateDate());
                            newCurrency.setRemarks("结帐转存:".concat(engineerTotalCharge.toString())
                                    .concat(" .服务费:")
                                    .concat(ec.getServiceCharge().toString())
                                    .concat(" ,快递费:")
                                    .concat(ec.getExpressCharge().toString())
                                    .concat(" ,远程费:")
                                    .concat(ec.getTravelCharge().toString())
                                    .concat(" ,配件费:")
                                    .concat(ec.getMaterialCharge().toString())
                                    .concat(" ,其他费:")
                                    .concat(ec.getOtherCharge().toString())
                                    .concat(" ,订单编号:")
                                    .concat(ec.getOrderNo()));
                            newCurrency.setQuarter(ec.getQuarter());
//                            engineerCurrencyDao.insert(newCurrency);
                            previousAmount = previousAmount + engineerTotalCharge;
                            totalInsert++;
                        }else{
                            Double engineerTotalCharge = ec.getServiceCharge() +
                                    ec.getExpressCharge() +
                                    ec.getTravelCharge() +
                                    ec.getMaterialCharge() +
                                    ec.getOtherCharge();
                            EngineerCurrency newCurrency = new EngineerCurrency();
                            newCurrency.setId(ec.getId() + 1);
                            newCurrency.setServicePoint(ec.getServicePoint());
                            newCurrency.setCurrencyType(engineerTotalCharge == 0 ? EngineerCurrency.CURRENCY_TYPE_NONE : (engineerTotalCharge > 0 ? EngineerCurrency.CURRENCY_TYPE_IN : EngineerCurrency.CURRENCY_TYPE_OUT));
                            newCurrency.setCurrencyNo(ec.getOrderNo());
                            newCurrency.setBeforeBalance(previousAmount);
                            newCurrency.setBalance(previousAmount + engineerTotalCharge);
                            newCurrency.setAmount(engineerTotalCharge);
                            newCurrency.setPaymentType(EngineerCurrency.PAYMENT_TYPE_WRITE_OFF);
                            newCurrency.setActionType(engineerTotalCharge == 0 ? EngineerCurrency.ACTION_TYPE_NONE : (engineerTotalCharge > 0 ? EngineerCurrency.ACTION_TYPE_REFUND : EngineerCurrency.ACTION_TYPE_REPLENISH));
                            newCurrency.setCreateBy(ec.getCreateBy());
                            newCurrency.setCreateDate(ec.getCreateDate());
                            newCurrency.setRemarks("退补款:".concat(engineerTotalCharge.toString())
                                    .concat(" ,订单编号:")
                                    .concat(ec.getOrderNo()));
                            newCurrency.setQuarter(ec.getQuarter());
//                            engineerCurrencyDao.insert(newCurrency);
                            previousAmount = previousAmount + engineerTotalCharge;
                            totalInsert++;
                        }
                    }
                }else{
                    for (EngineerCharge ec : chargeList){
                        previousCurrency = engineerCurrencyDao.getOnePrevious(ec.getId(), ec.getServicePoint().getId());
                        if (previousCurrency == null){
                            nextCurrency = engineerCurrencyDao.getOneNext(ec.getId(), ec.getServicePoint().getId());
                            if (nextCurrency == null){
                                if (ec.getChargeOrderType() == 0) {
                                    EngineerCurrency newCurrency = new EngineerCurrency();
                                    newCurrency.setId(ec.getId() + 1);
                                    newCurrency.setServicePoint(ec.getServicePoint());
                                    newCurrency.setCurrencyType(EngineerCurrency.CURRENCY_TYPE_IN);
                                    newCurrency.setCurrencyNo(ec.getOrderNo());
                                    newCurrency.setBeforeBalance(0d);
                                    Double engineerTotalCharge = ec.getServiceCharge() +
                                            ec.getExpressCharge() +
                                            ec.getTravelCharge() +
                                            ec.getMaterialCharge() +
                                            ec.getOtherCharge();
                                    newCurrency.setBalance(engineerTotalCharge);
                                    newCurrency.setAmount(engineerTotalCharge);
                                    newCurrency.setPaymentType(EngineerCurrency.PAYMENT_TYPE_ORDER_INVOICE);
                                    newCurrency.setActionType(EngineerCurrency.ACTION_TYPE_CHARGE);
                                    newCurrency.setCreateBy(ec.getCreateBy());
                                    newCurrency.setCreateDate(ec.getCreateDate());
                                    newCurrency.setRemarks("结帐转存:".concat(engineerTotalCharge.toString())
                                            .concat(" .服务费:")
                                            .concat(ec.getServiceCharge().toString())
                                            .concat(" ,快递费:")
                                            .concat(ec.getExpressCharge().toString())
                                            .concat(" ,远程费:")
                                            .concat(ec.getTravelCharge().toString())
                                            .concat(" ,配件费:")
                                            .concat(ec.getMaterialCharge().toString())
                                            .concat(" ,其他费:")
                                            .concat(ec.getOtherCharge().toString())
                                            .concat(" ,订单编号:")
                                            .concat(ec.getOrderNo()));
                                    newCurrency.setQuarter(ec.getQuarter());
                                engineerCurrencyDao.insert(newCurrency);
                                    totalInsert++;
                                }else{
                                    Double engineerTotalCharge = ec.getServiceCharge() +
                                            ec.getExpressCharge() +
                                            ec.getTravelCharge() +
                                            ec.getMaterialCharge() +
                                            ec.getOtherCharge();
                                    EngineerCurrency newCurrency = new EngineerCurrency();
                                    newCurrency.setId(ec.getId() + 1);
                                    newCurrency.setServicePoint(ec.getServicePoint());
                                    newCurrency.setCurrencyType(engineerTotalCharge == 0 ? EngineerCurrency.CURRENCY_TYPE_NONE : (engineerTotalCharge > 0 ? EngineerCurrency.CURRENCY_TYPE_IN : EngineerCurrency.CURRENCY_TYPE_OUT));
                                    newCurrency.setCurrencyNo(ec.getOrderNo());
                                    newCurrency.setBeforeBalance(0d);
                                    newCurrency.setBalance(engineerTotalCharge);
                                    newCurrency.setAmount(engineerTotalCharge);
                                    newCurrency.setPaymentType(EngineerCurrency.PAYMENT_TYPE_WRITE_OFF);
                                    newCurrency.setActionType(engineerTotalCharge == 0 ? EngineerCurrency.ACTION_TYPE_NONE : (engineerTotalCharge > 0 ? EngineerCurrency.ACTION_TYPE_REFUND : EngineerCurrency.ACTION_TYPE_REPLENISH));
                                    newCurrency.setCreateBy(ec.getCreateBy());
                                    newCurrency.setCreateDate(ec.getCreateDate());
                                    newCurrency.setRemarks("退补款:".concat(engineerTotalCharge.toString())
                                            .concat(" ,订单编号:")
                                            .concat(ec.getOrderNo()));
                                    newCurrency.setQuarter(ec.getQuarter());
                                engineerCurrencyDao.insert(newCurrency);
                                    totalInsert++;
                                }
                            }else{
                                if (ec.getChargeOrderType() == 0) {
                                    EngineerCurrency newCurrency = new EngineerCurrency();
                                    newCurrency.setId(ec.getId() + 1);
                                    newCurrency.setServicePoint(ec.getServicePoint());
                                    newCurrency.setCurrencyType(EngineerCurrency.CURRENCY_TYPE_IN);
                                    newCurrency.setCurrencyNo(ec.getOrderNo());
                                    Double engineerTotalCharge = ec.getServiceCharge() +
                                            ec.getExpressCharge() +
                                            ec.getTravelCharge() +
                                            ec.getMaterialCharge() +
                                            ec.getOtherCharge();
                                    newCurrency.setBeforeBalance(nextCurrency.getBeforeBalance() - engineerTotalCharge);
                                    newCurrency.setBalance(nextCurrency.getBeforeBalance());
                                    newCurrency.setAmount(engineerTotalCharge);
                                    newCurrency.setPaymentType(EngineerCurrency.PAYMENT_TYPE_ORDER_INVOICE);
                                    newCurrency.setActionType(EngineerCurrency.ACTION_TYPE_CHARGE);
                                    newCurrency.setCreateBy(ec.getCreateBy());
                                    newCurrency.setCreateDate(ec.getCreateDate());
                                    newCurrency.setRemarks("结帐转存:".concat(engineerTotalCharge.toString())
                                            .concat(" .服务费:")
                                            .concat(ec.getServiceCharge().toString())
                                            .concat(" ,快递费:")
                                            .concat(ec.getExpressCharge().toString())
                                            .concat(" ,远程费:")
                                            .concat(ec.getTravelCharge().toString())
                                            .concat(" ,配件费:")
                                            .concat(ec.getMaterialCharge().toString())
                                            .concat(" ,其他费:")
                                            .concat(ec.getOtherCharge().toString())
                                            .concat(" ,订单编号:")
                                            .concat(ec.getOrderNo()));
                                    newCurrency.setQuarter(ec.getQuarter());
                                engineerCurrencyDao.insert(newCurrency);
                                    totalInsert++;
                                }else{
                                    Double engineerTotalCharge = ec.getServiceCharge() +
                                            ec.getExpressCharge() +
                                            ec.getTravelCharge() +
                                            ec.getMaterialCharge() +
                                            ec.getOtherCharge();
                                    EngineerCurrency newCurrency = new EngineerCurrency();
                                    newCurrency.setId(ec.getId() + 1);
                                    newCurrency.setServicePoint(ec.getServicePoint());
                                    newCurrency.setCurrencyType(engineerTotalCharge == 0 ? EngineerCurrency.CURRENCY_TYPE_NONE : (engineerTotalCharge > 0 ? EngineerCurrency.CURRENCY_TYPE_IN : EngineerCurrency.CURRENCY_TYPE_OUT));
                                    newCurrency.setCurrencyNo(ec.getOrderNo());
                                    newCurrency.setBeforeBalance(nextCurrency.getBeforeBalance() + engineerTotalCharge);
                                    newCurrency.setBalance(nextCurrency.getBeforeBalance());
                                    newCurrency.setAmount(engineerTotalCharge);
                                    newCurrency.setPaymentType(EngineerCurrency.PAYMENT_TYPE_WRITE_OFF);
                                    newCurrency.setActionType(engineerTotalCharge == 0 ? EngineerCurrency.ACTION_TYPE_NONE : (engineerTotalCharge > 0 ? EngineerCurrency.ACTION_TYPE_REFUND : EngineerCurrency.ACTION_TYPE_REPLENISH));
                                    newCurrency.setCreateBy(ec.getCreateBy());
                                    newCurrency.setCreateDate(ec.getCreateDate());
                                    newCurrency.setRemarks("退补款:".concat(engineerTotalCharge.toString())
                                            .concat(" ,订单编号:")
                                            .concat(ec.getOrderNo()));
                                    newCurrency.setQuarter(ec.getQuarter());
                                engineerCurrencyDao.insert(newCurrency);
                                    totalInsert++;
                                }
                            }
                        }else{
                            if (ec.getChargeOrderType() == 0) {
                                EngineerCurrency newCurrency = new EngineerCurrency();
                                newCurrency.setId(ec.getId() + 1);
                                newCurrency.setServicePoint(ec.getServicePoint());
                                newCurrency.setCurrencyType(EngineerCurrency.CURRENCY_TYPE_IN);
                                newCurrency.setCurrencyNo(ec.getOrderNo());
                                Double engineerTotalCharge = ec.getServiceCharge() +
                                        ec.getExpressCharge() +
                                        ec.getTravelCharge() +
                                        ec.getMaterialCharge() +
                                        ec.getOtherCharge();
                                newCurrency.setBeforeBalance(previousCurrency.getBalance());
                                newCurrency.setBalance(previousCurrency.getBalance()+engineerTotalCharge);
                                newCurrency.setAmount(engineerTotalCharge);
                                newCurrency.setPaymentType(EngineerCurrency.PAYMENT_TYPE_ORDER_INVOICE);
                                newCurrency.setActionType(EngineerCurrency.ACTION_TYPE_CHARGE);
                                newCurrency.setCreateBy(ec.getCreateBy());
                                newCurrency.setCreateDate(ec.getCreateDate());
                                newCurrency.setRemarks("结帐转存:".concat(engineerTotalCharge.toString())
                                        .concat(" .服务费:")
                                        .concat(ec.getServiceCharge().toString())
                                        .concat(" ,快递费:")
                                        .concat(ec.getExpressCharge().toString())
                                        .concat(" ,远程费:")
                                        .concat(ec.getTravelCharge().toString())
                                        .concat(" ,配件费:")
                                        .concat(ec.getMaterialCharge().toString())
                                        .concat(" ,其他费:")
                                        .concat(ec.getOtherCharge().toString())
                                        .concat(" ,订单编号:")
                                        .concat(ec.getOrderNo()));
                                newCurrency.setQuarter(ec.getQuarter());
                                engineerCurrencyDao.insert(newCurrency);
                                totalInsert++;
                            }else{
                                Double engineerTotalCharge = ec.getServiceCharge() +
                                        ec.getExpressCharge() +
                                        ec.getTravelCharge() +
                                        ec.getMaterialCharge() +
                                        ec.getOtherCharge();
                                EngineerCurrency newCurrency = new EngineerCurrency();
                                newCurrency.setId(ec.getId() + 1);
                                newCurrency.setServicePoint(ec.getServicePoint());
                                newCurrency.setCurrencyType(engineerTotalCharge == 0 ? EngineerCurrency.CURRENCY_TYPE_NONE : (engineerTotalCharge > 0 ? EngineerCurrency.CURRENCY_TYPE_IN : EngineerCurrency.CURRENCY_TYPE_OUT));
                                newCurrency.setCurrencyNo(ec.getOrderNo());
                                newCurrency.setBeforeBalance(previousCurrency.getBalance());
                                newCurrency.setBalance(previousCurrency.getBalance() + engineerTotalCharge);
                                newCurrency.setAmount(engineerTotalCharge);
                                newCurrency.setPaymentType(EngineerCurrency.PAYMENT_TYPE_WRITE_OFF);
                                newCurrency.setActionType(engineerTotalCharge == 0 ? EngineerCurrency.ACTION_TYPE_NONE : (engineerTotalCharge > 0 ? EngineerCurrency.ACTION_TYPE_REFUND : EngineerCurrency.ACTION_TYPE_REPLENISH));
                                newCurrency.setCreateBy(ec.getCreateBy());
                                newCurrency.setCreateDate(ec.getCreateDate());
                                newCurrency.setRemarks("退补款:".concat(engineerTotalCharge.toString())
                                        .concat(" ,订单编号:")
                                        .concat(ec.getOrderNo()));
                                newCurrency.setQuarter(ec.getQuarter());
                                engineerCurrencyDao.insert(newCurrency);
                                totalInsert++;
                            }
                        }
                    }
                }
            }
            //endregion 多条
            canUpdate = false;
            totalAmount = 0;
            chargeList.clear();
        }
        log.info("==================={}",totalInsert);
        log.info("=======can update count============{}",canUpdateCount);
        log.info("==========can not update count========={}",canNotUpdateCount);
    }
}
