package com.wolfking.jeesite.modules.fi.service;

import com.google.common.collect.Lists;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.service.LongIDCrudService;
import com.wolfking.jeesite.common.utils.CurrencyUtil;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.fi.dao.*;
import com.wolfking.jeesite.modules.fi.entity.*;
import com.wolfking.jeesite.modules.md.dao.ServicePointDao;
import com.wolfking.jeesite.modules.md.entity.Product;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.md.entity.ServicePointFinance;
import com.wolfking.jeesite.modules.md.entity.ServiceType;
import com.wolfking.jeesite.modules.md.service.ProductService;
import com.wolfking.jeesite.modules.md.service.ServicePointFinanceService;
import com.wolfking.jeesite.modules.md.service.ServicePointService;
import com.wolfking.jeesite.modules.md.service.ServiceTypeService;
import com.wolfking.jeesite.modules.rpt.dao.ServicePointBalanceMonthlyDao;
import com.wolfking.jeesite.modules.rpt.dao.ServicePointBalanceMonthlyDetailDao;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.SeqUtils;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by Jeff on 2017/4/20.
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class EngineerChargeService extends LongIDCrudService<EngineerChargeDao, EngineerCharge> {
    @Resource
    private EngineerChargeDao engineerChargeDao;
    @Resource
    private EngineerChargeConditionDao engineerChargeConditionDao;
    @Resource
    private ServicePointDao servicePointDao;
    @Resource
    private EngineerCurrencyDao engineerCurrencyDao;
    @Resource
    private ServicePointPayableMonthlyDao servicePointPayableMonthlyDao;
    @Resource
    private ServicePointPayableMonthlyDetailDao servicePointPayableMonthlyDetailDao;
    @Resource
    private ServicePointBalanceMonthlyDao servicePointBalanceMonthlyDao;
    @Resource
    private ServicePointBalanceMonthlyDetailDao servicePointBalanceMonthlyDetailDao;

    @Autowired
    private ServiceTypeService serviceTypeService;
    @Autowired
    private ServicePointService servicePointService;
    @Autowired
    private ProductService productService;
    @Autowired
    private ServicePointFinanceService servicePointFinanceService;

    /**
     * 分页查找客户对帐列表
     * @param page
     * @param entity
     * @return
     */
    public Page<EngineerCharge> find(Page<EngineerChargeCondition> page, EngineerChargeCondition entity) {
        entity.setPage(page);
        List<EngineerChargeCondition> list = engineerChargeConditionDao.selectConditionInfo(entity);
        Page<EngineerCharge> rtnPage = new Page<>();
        rtnPage.setPageNo(page.getPageNo());
        rtnPage.setPageSize(page.getPageSize());
        rtnPage.setCount(page.getCount());
        rtnPage.setOrderBy(page.getOrderBy());
        if(list != null && list.size()>0){
            List<EngineerCharge> engineerChargeList = Lists.newArrayListWithExpectedSize(list.size());
            EngineerCharge engineerCharge;
            //List<ServiceType> serviceTypes = serviceTypeService.findAllList(); mark on 2019-10-10
            // 调用微服务,数据只返回服务类型id和名称 start 2019-10-10
            Map<Long,String> map = serviceTypeService.findAllIdsAndNames();
            String serviceTypeName="";
            // end
            ServiceType serviceType;
            for(EngineerChargeCondition condition : list){
                engineerCharge = dao.get(condition.getId());
                if(engineerCharge != null) {
                    //ServicePoint servicePoint = (ServicePoint) redisUtils.zRangeOneByScore(RedisConstant.RedisDBType.REDIS_MD_DB,RedisConstant.MD_SERVICEPOINT_ALL,engineerCharge.getServicePoint().getId(),engineerCharge.getServicePoint().getId(),ServicePoint.class);  //mark on 2020-1-17
                    ServicePoint servicePoint = servicePointService.getFromCache(engineerCharge.getServicePoint().getId());   //add on 2020-1-17
                    if(servicePoint != null){
                        engineerCharge.setServicePoint(servicePoint);
                    }
                    //Product product = (Product) redisUtils.hGet(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_PRODUCT, engineerCharge.getProduct().getId().toString(), Product.class); //mark on 2020-2-13
                    Product product = productService.getProductByIdFromCache(engineerCharge.getProduct().getId());
                    if (product != null){
                        engineerCharge.setProduct(product);
                    }
                    //Service Type
                    // mark on 2019-10-10
                    /*if(serviceTypes !=null && serviceTypes.size()>0) {
                        Long serviceTypeId = engineerCharge.getServiceType().getId();
                        serviceType = serviceTypes.stream().filter(t -> Objects.equals(serviceTypeId, t.getId())).findFirst().orElse(null);
                        if (serviceType != null) {
                            engineerCharge.setServiceType(serviceType);
                        }
                    }*/
                    // add on 2019-10-10
                    if(map!=null && map.size()>0){
                        Long serviceTypeId = engineerCharge.getServiceType().getId();
                        serviceTypeName = map.get(serviceTypeId);
                        if(StringUtils.isNotBlank(serviceTypeName)){
                            serviceType = new ServiceType();
                            serviceType.setId(serviceTypeId);
                            serviceType.setName(serviceTypeName);
                            engineerCharge.setServiceType(serviceType);
                        }
                    }
                    //end
                    engineerChargeList.add(engineerCharge);
                }
            }
            rtnPage.setList(engineerChargeList);
            //切换为微服务
            if (engineerChargeList.size() > 0) {
                Map<String, Dict> engineerChargeStatusMap = MSDictUtils.getDictMap("engineer_charge_status");
                for (EngineerCharge item: engineerChargeList){
                    if (item.getStatus() != null && item.getStatus() > 0) {
                        Dict statusDict = engineerChargeStatusMap.get(item.getStatus().toString());
                        item.setStatusName(statusDict!=null?statusDict.getLabel():"");
                    }
                }
            }
        }
        return rtnPage;
    }

    /**
     * 网点退补
     * @param id 原对帐单id
     * @param sc 服务费
     * @param ec 快递费
     * @param tc 远程费
     * @param mc 配件费
     * @param oc 其他费
     * @param remarks 说明
     * @throws RuntimeException
     */
    @Transactional()
    public void writeOffSave(String id,String sc,String ec, String tc, String mc, String oc, String remarks) throws RuntimeException{
        // 生成退补单数据
        User createBy = UserUtils.getUser();
        Date createDate = new Date();
        Double serviceCharge = Double.valueOf(sc);
        Double expressCharge = Double.valueOf(ec);
        Double travelCharge = Double.valueOf(tc);
        Double materialCharge = Double.valueOf(mc);
        Double otherCharge = Double.valueOf(oc);

        //获取原始订单结算信息
        EngineerCharge originEngineerCharge = engineerChargeDao.get(Long.parseLong(id));

        //获取网点信息
        ServicePoint servicePoint = servicePointService.getFromCache(originEngineerCharge.getServicePoint().getId());

        //记录扣点和平台费点
        double taxFeeRate = originEngineerCharge.getTaxFeeRate();
        double infoFeeRate = originEngineerCharge.getInfoFeeRate();
        //生成服务网点退补信息
        EngineerCharge engineerCharge = new EngineerCharge();
        Long engineerChargeId = SeqUtils.NextIDValue(SeqUtils.TableName.EngineerCharge);
        engineerCharge.setId(engineerChargeId);
        engineerCharge.setOrderId(originEngineerCharge.getOrderId());
        engineerCharge.setOrderNo(originEngineerCharge.getOrderNo());
        engineerCharge.setOrderDetailId(originEngineerCharge.getOrderDetailId());
        engineerCharge.setServicePoint(originEngineerCharge.getServicePoint());
        engineerCharge.setEngineer(originEngineerCharge.getEngineer());
        engineerCharge.setProduct(originEngineerCharge.getProduct());
        engineerCharge.setServiceType(originEngineerCharge.getServiceType());
        engineerCharge.setQty(0);
        engineerCharge.setServiceCharge(serviceCharge - CurrencyUtil.round2(serviceCharge*taxFeeRate) - CurrencyUtil.round2(serviceCharge*infoFeeRate));
        engineerCharge.setExpressCharge(expressCharge - CurrencyUtil.round2(expressCharge*taxFeeRate) - CurrencyUtil.round2(expressCharge*infoFeeRate));
        engineerCharge.setTravelCharge(travelCharge - CurrencyUtil.round2(travelCharge*taxFeeRate) - CurrencyUtil.round2(travelCharge*infoFeeRate));
        engineerCharge.setMaterialCharge(materialCharge - CurrencyUtil.round2(materialCharge*taxFeeRate) - CurrencyUtil.round2(materialCharge*infoFeeRate));
        engineerCharge.setOtherCharge(otherCharge - CurrencyUtil.round2(otherCharge*taxFeeRate) - CurrencyUtil.round2(otherCharge*infoFeeRate));
        engineerCharge.setServiceTimes(0);
        engineerCharge.setPaymentType(Integer.parseInt(servicePoint.getFinance().getPaymentType().getValue()));
        engineerCharge.setChargeOrderType(EngineerCharge.EC_TYPE_WRITE_OFF);
        engineerCharge.setTaxFeeRate(taxFeeRate);
        engineerCharge.setInfoFeeRate(infoFeeRate);
        engineerCharge.setStatus(EngineerCharge.EC_STATUS_CLOSED);
        engineerCharge.setRemarks(remarks);
        engineerCharge.setCreateBy(createBy);
        engineerCharge.setCreateDate(createDate);
        engineerCharge.setQuarter(QuarterUtils.getSeasonQuarter(createDate));
        engineerCharge.setCustomerId(originEngineerCharge.getCustomerId());
        engineerChargeDao.insert(engineerCharge);

        //生成服务网点对帐查询信息
        EngineerChargeCondition originEngineerChargeCondition = engineerChargeConditionDao.get(Long.parseLong(id));
        EngineerChargeCondition engineerChargeCondition = new EngineerChargeCondition();
        engineerChargeCondition.setId(engineerCharge.getId());
        engineerChargeCondition.setOrderId(engineerCharge.getOrderId());
        engineerChargeCondition.setOrderNo(engineerCharge.getOrderNo());
        engineerChargeCondition.setServicePointId(originEngineerChargeCondition.getServicePointId());
        engineerChargeCondition.setEngineerId(originEngineerChargeCondition.getEngineerId());
        engineerChargeCondition.setProductCategoryId(originEngineerChargeCondition.getProductCategoryId());
        engineerChargeCondition.setProductId(originEngineerChargeCondition.getProductId());
        engineerChargeCondition.setServiceTypeId(originEngineerChargeCondition.getServiceTypeId());
        engineerChargeCondition.setPaymentType(originEngineerChargeCondition.getPaymentType());
        engineerChargeCondition.setChargeOrderType(EngineerCharge.EC_TYPE_WRITE_OFF);
        engineerChargeCondition.setStatus(EngineerCharge.EC_STATUS_CLOSED);
        engineerChargeCondition.setAutoChargeFlag(0);
        engineerChargeCondition.setChargeDate(engineerCharge.getCreateDate());
        engineerChargeCondition.setChargeBy(createBy.getId());
        engineerChargeCondition.setOrderCloseDate(originEngineerChargeCondition.getOrderCloseDate());
        engineerChargeCondition.setQuarter(engineerCharge.getQuarter());
        engineerChargeCondition.setCustomerId(originEngineerChargeCondition.getCustomerId());
        engineerChargeConditionDao.insert(engineerChargeCondition);

        //生成网点资金异动流水
        Double engineerTotalCharge = serviceCharge +
                expressCharge +
                travelCharge +
                materialCharge +
                otherCharge;
        //记录扣点，平台费前费用 - 用于生成备注信息
        double amount = engineerTotalCharge;
        //从数据库读取余额与扣点开关与扣点点-用于计算扣点，平台费点
        ServicePointFinance servicePointFinance = servicePointDao.getBalanceById(engineerCharge.getServicePoint().getId());
        //计算扣点，平台费
        double taxFee = 0;
        double infoFee = 0;
        //扣点
        if (taxFeeRate > 0) {
            taxFee = 0 - CurrencyUtil.round2(engineerTotalCharge*taxFeeRate);
        }
        //信息费
        if (infoFeeRate > 0) {
            infoFee = 0 - CurrencyUtil.round2(engineerTotalCharge*infoFeeRate);
        }
        //重新汇总余额 - 汇总服务/快递/远程/配件保险/其他费 + 扣点/信息费
        engineerTotalCharge = engineerTotalCharge + infoFee + taxFee;

        //生成备注信息
        StringBuilder remarksBuilder = new StringBuilder();
        if (infoFee != 0) {
            remarksBuilder.append(String.format(",平台费:%.2f元", infoFee));
        }
        if (taxFee != 0) {
            remarksBuilder.append(String.format(",扣点:%.2f元", taxFee));
        }
        if (infoFee != 0 || taxFee != 0) {
            remarksBuilder.insert(0, String.format("(金额:%.2f元", amount));
            remarksBuilder.append(")");
        }
        remarksBuilder.insert(0, remarks);

        EngineerCurrency engineerCurrency = new EngineerCurrency();
        engineerCurrency.setId(SeqUtils.NextIDValue(SeqUtils.TableName.EngineerCurrency));
        engineerCurrency.setServicePoint(engineerCharge.getServicePoint());
        engineerCurrency.setCurrencyType(engineerTotalCharge == 0 ? EngineerCurrency.CURRENCY_TYPE_NONE : (engineerTotalCharge > 0 ? EngineerCurrency.CURRENCY_TYPE_IN : EngineerCurrency.CURRENCY_TYPE_OUT));
        engineerCurrency.setCurrencyNo(engineerCharge.getOrderNo());
        engineerCurrency.setBeforeBalance(servicePointFinance.getBalance());
        engineerCurrency.setBalance(engineerCurrency.getBeforeBalance() + engineerTotalCharge);
        engineerCurrency.setAmount(engineerTotalCharge);
        engineerCurrency.setPaymentType(EngineerCurrency.PAYMENT_TYPE_WRITE_OFF);
        engineerCurrency.setActionType(engineerTotalCharge == 0 ? EngineerCurrency.ACTION_TYPE_NONE : (engineerTotalCharge > 0 ? EngineerCurrency.ACTION_TYPE_REFUND : EngineerCurrency.ACTION_TYPE_REPLENISH));
        engineerCurrency.setCreateBy(createBy);
        engineerCurrency.setCreateDate(createDate);
        engineerCurrency.setRemarks(remarksBuilder.toString());
        engineerCurrency.setQuarter(engineerCharge.getQuarter());
        engineerCurrencyDao.insert(engineerCurrency);

        //更新网点余额
        servicePointFinance.setBalance(engineerCurrency.getAmount());
        //累计即结款
        if (engineerCharge.getPaymentType() == 20) {
            servicePointFinance.setDailyBalance(engineerCurrency.getAmount());
        }
        //累计信息费
        servicePointFinance.setInfoFee(infoFee);
        //累计扣点
        servicePointFinance.setTaxFee(taxFee);
        servicePointDao.updateBalance(servicePointFinance);

        //更新缓存
        //ServicePoint cachedServicePoint = servicePointService.getFromCache(engineerCharge.getServicePoint().getId());
        //if (cachedServicePoint != null) {
        //    cachedServicePoint.getFinance().setBalance(engineerCurrency.getBeforeBalance() + engineerTotalCharge);
            //servicePointService.updateServicePointCache(cachedServicePoint);  //mark on 2020-1-14  web端去servicePoint
        //}
        // add on 2020-5-4 begin
        // 更新网点财务缓存
        ServicePointFinance cachedServicePointFinance = servicePointFinanceService.getFromCache(engineerCharge.getServicePoint().getId());
        if (cachedServicePointFinance != null) {
            cachedServicePointFinance.setBalance(engineerCurrency.getBeforeBalance() + engineerTotalCharge);
            cachedServicePointFinance.setInfoFee(infoFee);
            cachedServicePointFinance.setTaxFee(taxFee);
            servicePointFinanceService.updateCache(cachedServicePointFinance);
        }
        // add on 2020-5-4 end

        //累计网点应付款
        ServicePointPayableMonthly servicePointPayableMonthly = new ServicePointPayableMonthly();
        servicePointPayableMonthly.setServicePoint(originEngineerCharge.getServicePoint());
        servicePointPayableMonthly.setPaymentType(engineerCharge.getPaymentType());
        servicePointPayableMonthly.setYear(DateUtils.getYear(createDate));
        servicePointPayableMonthly.setMonth(DateUtils.getMonth(createDate));
        servicePointPayableMonthly.setId((servicePointPayableMonthly.getServicePoint().getId() * 10000 + servicePointPayableMonthly.getYear()) * 100 + servicePointPayableMonthly.getPaymentType());
        servicePointPayableMonthly.setAmount(engineerTotalCharge);
        //更新应付款
        servicePointPayableMonthlyDao.incrAmountForCharge(servicePointPayableMonthly);

        //累计网点应付款按品类
        ServicePointPayableMonthlyDetail payableMonthlyDetail = new ServicePointPayableMonthlyDetail();
        payableMonthlyDetail.setTotalId(servicePointPayableMonthly.getId());
        payableMonthlyDetail.setServicePoint(originEngineerCharge.getServicePoint());
        payableMonthlyDetail.setPaymentType(engineerCharge.getPaymentType());
        payableMonthlyDetail.setYear(DateUtils.getYear(createDate));
        payableMonthlyDetail.setMonth(DateUtils.getMonth(createDate));
        payableMonthlyDetail.setProductCategoryId(originEngineerChargeCondition.getProductCategoryId());
        payableMonthlyDetail.setId((payableMonthlyDetail.getServicePoint().getId() * 10000 + payableMonthlyDetail.getYear()) * 100 + payableMonthlyDetail.getPaymentType() + payableMonthlyDetail.getProductCategoryId());
        payableMonthlyDetail.setAmount(engineerTotalCharge);
        //更新应付款
        servicePointPayableMonthlyDetailDao.incrAmountForCharge(payableMonthlyDetail);

        //累计网点报表余额
        servicePointBalanceMonthlyDao.incrBalance(servicePointPayableMonthly);

        //累计网点报表余额按品类
        servicePointBalanceMonthlyDetailDao.incrBalance(payableMonthlyDetail);
    }

}
