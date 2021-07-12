package com.wolfking.jeesite.modules.fi.service;

import com.google.common.collect.Lists;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.service.LongIDCrudService;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.wolfking.jeesite.modules.fi.dao.CustomerChargeConditionDao;
import com.wolfking.jeesite.modules.fi.dao.CustomerChargeDao;
import com.wolfking.jeesite.modules.fi.dao.CustomerCurrencyDao;
import com.wolfking.jeesite.modules.fi.entity.CustomerCharge;
import com.wolfking.jeesite.modules.fi.entity.CustomerChargeCondition;
import com.wolfking.jeesite.modules.fi.entity.CustomerCurrency;
import com.wolfking.jeesite.modules.md.dao.CustomerFinanceDao;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.CustomerFinance;
import com.wolfking.jeesite.modules.sd.dao.OrderDao;
import com.wolfking.jeesite.modules.sd.dao.OrderHeadDao;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
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
public class CustomerChargeService extends LongIDCrudService<CustomerChargeDao, CustomerCharge> {
    @Autowired
    private RedisUtils redisUtils;
    @Resource
    private CustomerChargeDao customerChargeDao;
    @Resource
    private CustomerChargeConditionDao customerChargeConditionDao;
    @Resource
    private CustomerFinanceDao customerFinanceDao;
    @Resource
    private CustomerCurrencyDao customerCurrencyDao;
    @Resource
    private OrderDao orderDao;
    @Resource
    private OrderHeadDao orderHeadDao;

    /**
     * 分页查找客户对帐列表
     * @param page
     * @param entity
     * @return
     */
    public Page<CustomerCharge> find(Page<CustomerChargeCondition> page, CustomerChargeCondition entity) {
        entity.setPage(page);
        List<CustomerChargeCondition> list = customerChargeConditionDao.selectConditionInfo(entity);
        Page<CustomerCharge> rtnPage = new Page<>();
        rtnPage.setPageNo(page.getPageNo());
        rtnPage.setPageSize(page.getPageSize());
        rtnPage.setCount(page.getCount());
        rtnPage.setOrderBy(page.getOrderBy());
        if(list != null && list.size()>0){
            List<CustomerCharge> customerChargeList = Lists.newArrayListWithExpectedSize(list.size());
            CustomerCharge customerCharge;
            for(CustomerChargeCondition condition : list){
                customerCharge = dao.get(condition.getId());
                if(customerCharge != null) {
                    customerCharge.setCondition(condition);
                    Customer customer = (Customer) redisUtils.zRangeOneByScore(RedisConstant.RedisDBType.REDIS_MD_DB,RedisConstant.MD_CUSTOMER_ALL,customerCharge.getCustomer().getId(), customerCharge.getCustomer().getId(), Customer.class);
                    if(customer != null){
                        customerCharge.setCustomer(customer);
                    }
                    customerChargeList.add(customerCharge);
                }
            }
            rtnPage.setList(customerChargeList);

            //切换为微服务
            if (customerChargeList.size() > 0) {
                Map<String, Dict> chargeStatusMap = MSDictUtils.getDictMap("customer_charge_status");
                for (CustomerCharge item : customerChargeList) {
                    if (item.getStatus() != null && item.getStatus() > 0) {
                        Dict chargeStatusDict = chargeStatusMap.get(item.getStatus().toString());
                        item.setStatusName(chargeStatusDict != null ? chargeStatusDict.getLabel() : "");
                    }
                }
            }
        }
        return rtnPage;
    }

    /**
     * 客户退补
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
    public void writeOffSave(String id,String sc,String ec, String tc, String mc, String tlc, String uc, String pf, String oc, String remarks) throws RuntimeException{
        // 生成退补单数据
        User createBy = UserUtils.getUser();
        Date createDate = new Date();
        Double serviceCharge = Double.valueOf(sc);
        Double expressCharge = Double.valueOf(ec);
        Double travelCharge = Double.valueOf(tc);
        Double materialCharge = Double.valueOf(mc);
        Double timeLinessChrage = Double.valueOf(tlc);
        Double urgentCharge = Double.valueOf(uc);
        Double praiseFee = Double.valueOf(pf);
        Double otherCharge = Double.valueOf(oc);

        //生成客户退补数据信息
        CustomerCharge originCustomerCharge = customerChargeDao.get(Long.parseLong(id));
        Long customerChargeId = SeqUtils.NextIDValue(SeqUtils.TableName.CustomerCharge);
        CustomerCharge customerCharge = new CustomerCharge();
        customerCharge.setId(customerChargeId);
        customerCharge.setOrderId(originCustomerCharge.getOrderId());
        customerCharge.setOrderNo(originCustomerCharge.getOrderNo());
        customerCharge.setCustomer(originCustomerCharge.getCustomer());
        customerCharge.setServiceCharge(serviceCharge);
        customerCharge.setExpressCharge(expressCharge);
        customerCharge.setTravelCharge(travelCharge);
        customerCharge.setMaterialCharge(materialCharge);
        customerCharge.setTimeLinessCharge(timeLinessChrage);
        customerCharge.setUrgentCharge(urgentCharge);
        customerCharge.setPraiseFee(praiseFee);
        customerCharge.setOtherCharge(otherCharge);
        customerCharge.setCurrency(originCustomerCharge.getCurrency());
        customerCharge.setServiceTimes(0);
        customerCharge.setPaymentType(originCustomerCharge.getPaymentType());
        customerCharge.setChargeOrderType(CustomerCharge.CC_TYPE_WRITE_OFF);
        customerCharge.setStatus(CustomerCharge.CC_STATUS_CLOSED);
        customerCharge.setRemarks(remarks);
        customerCharge.setCreateBy(createBy);
        customerCharge.setCreateDate(createDate);
        customerCharge.setQuarter(QuarterUtils.getSeasonQuarter(createDate));
        customerChargeDao.insert(customerCharge);

        //生成客户对帐查询信息
        CustomerChargeCondition originCustomerChargeCondition = customerChargeConditionDao.get(Long.parseLong(id));
        CustomerChargeCondition customerChargeCondition = new CustomerChargeCondition();
        customerChargeCondition.setId(customerCharge.getId());
        customerChargeCondition.setOrderId(customerCharge.getOrderId());
        customerChargeCondition.setOrderNo(customerCharge.getOrderNo());
        customerChargeCondition.setCustomerId(customerCharge.getCustomer().getId());
        customerChargeCondition.setProductCategoryId(originCustomerChargeCondition != null ? originCustomerChargeCondition.getProductCategoryId() : 0);
        customerChargeCondition.setProductIds(originCustomerChargeCondition.getProductIds());
        customerChargeCondition.setServiceTimes(originCustomerChargeCondition.getServiceTimes());
        customerChargeCondition.setPaymentType(customerCharge.getPaymentType());
        customerChargeCondition.setStatus(CustomerCharge.CC_STATUS_CLOSED);
        customerChargeCondition.setChargeOrderType(customerCharge.getChargeOrderType());
        customerChargeCondition.setCreateDate(customerCharge.getCreateDate());
        customerChargeCondition.setTotalQty(originCustomerChargeCondition.getTotalQty());
        customerChargeCondition.setOrderCreateDate(originCustomerChargeCondition.getOrderCreateDate());
        customerChargeCondition.setOrderCloseDate(originCustomerChargeCondition.getOrderCloseDate());
        customerChargeCondition.setServiceTypes(originCustomerChargeCondition.getServiceTypes());
        customerChargeCondition.setQuarter(customerCharge.getQuarter());
        customerChargeConditionDao.insert(customerChargeCondition);

        //修改订单退补标记0-无,1-有客户退补
        String quarter = QuarterUtils.getOrderQuarterFromNo(customerCharge.getOrderNo());
        if(StringUtils.isBlank(quarter)){
            quarter = null;
        }
        orderHeadDao.updateWriteOffFlag(1,originCustomerCharge.getOrderId(),quarter);//2020-12-03 sd_order -> sd_order_head

        //生成客户资金异动流水
        Double totalOrderFee = serviceCharge + expressCharge + travelCharge + materialCharge + timeLinessChrage + urgentCharge + praiseFee + otherCharge;
        CustomerFinance customerFinance = customerFinanceDao.getAmounts(customerCharge.getCustomer().getId());
        CustomerCurrency customerCurrency = new CustomerCurrency();
        customerCurrency.setId(SeqUtils.NextIDValue(SeqUtils.TableName.CustomerCurrency));
        customerCurrency.setCustomer(customerCharge.getCustomer());
        customerCurrency.setCurrencyType(totalOrderFee == 0 ? CustomerCurrency.CURRENCY_TYPE_NONE : (totalOrderFee > 0 ? CustomerCurrency.CURRENCY_TYPE_OUT : CustomerCurrency.CURRENCY_TYPE_IN));
        customerCurrency.setCurrencyNo(customerCharge.getOrderNo());
        customerCurrency.setBeforeBalance(customerFinance.getBalance());
        customerCurrency.setBalance(customerCurrency.getBeforeBalance() - totalOrderFee);
        customerCurrency.setAmount(0-totalOrderFee);
        customerCurrency.setPaymentType(CustomerCurrency.PAYMENT_TYPE_TRANSFER_ACCOUNT);
        customerCurrency.setActionType(totalOrderFee == 0 ? CustomerCurrency.ACTION_TYPE_NONE : (totalOrderFee > 0 ? CustomerCurrency.ACTION_TYPE_REPLENISH : CustomerCurrency.ACTION_TYPE_REFUND));
        customerCurrency.setRemarks(remarks);
        customerCurrency.setCreateBy(createBy);
        customerCurrency.setCreateDate(createDate);
        customerCurrency.setQuarter(customerCharge.getQuarter());
        customerCurrencyDao.insert(customerCurrency);

        //更新客户余额
        customerFinance.setUpdateBy(createBy);
        customerFinance.setUpdateDate(createDate);
        customerFinance.setBalance(0-totalOrderFee);
        customerFinanceDao.updateBalanceFromInvoice(customerFinance);
        customerFinance.setBlockAmount(0d);
        customerFinance.setTotalAmount(totalOrderFee);
        customerFinanceDao.updateAmountFromInvoice(customerFinance);
    }
}
