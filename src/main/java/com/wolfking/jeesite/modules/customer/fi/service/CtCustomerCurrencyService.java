package com.wolfking.jeesite.modules.customer.fi.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.fi.recharge.CustomerOfflineRecharge;
import com.kkl.kklplus.entity.fi.recharge.CustomerOfflineRechargeSearch;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.customer.fi.dao.CtCustomerCurrencyDao;
import com.wolfking.jeesite.modules.customer.fi.dao.CtCustomerFinanceDao;
import com.wolfking.jeesite.modules.fi.entity.CustomerCurrency;
import com.wolfking.jeesite.modules.fi.service.CustomerBlockCurrencyService;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.CustomerFinance;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.utils.SeqUtils;
import com.wolfking.jeesite.ms.providermd.service.MSCustomerService;
import com.wolfking.jeesite.ms.recharge.entity.CustomerOfflineRechargeModel;
import com.wolfking.jeesite.ms.recharge.entity.mapper.CustomerOffineRechargeModelMapper;
import com.wolfking.jeesite.ms.recharge.feign.CustomerOfflineRechargeFeign;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class CtCustomerCurrencyService {

    @Resource
    private CtCustomerCurrencyDao ctCustomerCurrencyDao;

    @Resource
    private CtCustomerFinanceDao ctCustomerFinanceDao;

    @Autowired
    private MSCustomerService msCustomerService;

    @Autowired
    private CustomerOfflineRechargeFeign customerOfflineRechargeFeign;

    @Autowired
    private CustomerBlockCurrencyService customerBlockCurrencyService;

    public CustomerCurrency get(long id) {
        return ctCustomerCurrencyDao.get(id);
    }

    /**
     * 在线充值列表
     *
     * @param page
     * @param customerCurrency
     * @return
     */
    public Page<CustomerCurrency> find(Page<CustomerCurrency> page, CustomerCurrency customerCurrency) {
        //CustomerCurrency customerCurrency=new CustomerCurrency();
        customerCurrency.setPage(page);

        //借用 create_date 和 update_date传递开始时间和结束时间参数
        customerCurrency.setCreateDate(DateUtils.getDateStart(customerCurrency.getCreateDate()));
        customerCurrency.setUpdateDate(DateUtils.getDateEnd(customerCurrency.getUpdateDate()));
        // 执行分页查询
        List<CustomerCurrency> list = ctCustomerCurrencyDao.findCurrencyList(customerCurrency);

        // 切换微服务 begin 2019-6-27
        Map<Long,Customer> customerMap = Maps.newHashMap();
        if (list != null && !list.isEmpty()) {
            list.stream().forEach(r -> {
                Customer innerCustomer = null;
                if (r.getCustomer() != null && r.getCustomer().getId() != null) {
                    innerCustomer = customerMap.get(r.getCustomer().getId());
                    if (innerCustomer == null) {
                        log.warn("customerMap中没有找到id：{}", r.getCustomer().getId());
                        innerCustomer = msCustomerService.get(r.getCustomer().getId());
                    }
                }

                if (innerCustomer != null && innerCustomer.getId() != null) {
                    Customer customerEntity = new Customer();
                    customerEntity.setId(innerCustomer.getId());
                    customerEntity.setName(innerCustomer.getName());

                    CustomerFinance customerFinance = new CustomerFinance();
                    customerFinance.setBalance(0D);
                    customerFinance.setPaymentType(innerCustomer.getPaymentType());

                    r.setCustomer(customerEntity);
                    r.setCustomerFinance(customerFinance);
                    customerMap.put(innerCustomer.getId(),innerCustomer);
                }
            });
        }
        customerMap.clear();
        // 切换微服务 end 2019-6-27

        // List<Long> ids = list.stream().map(cc -> cc.getCustomer().getId()).distinct().collect(Collectors.toList()); // mark on 2019-6-29
        List<Long> ids = list.stream().map(cc ->{ return cc.getCustomer()==null?0:cc.getCustomer().getId();}).distinct().collect(Collectors.toList());  // add on 2019-6-29
        List<CustomerFinance> financeList = ctCustomerFinanceDao.getBalanceByIds(ids);

        // 切换为微服务
        Map<String, Dict> actionTypeMap = MSDictUtils.getDictMap("CustomerActionType");
        for (CustomerCurrency item : list) {
            if (item.getActionType() != null && item.getActionType() > 0) {
                Dict actionTypeDict = actionTypeMap.get(item.getActionType().toString());
                item.setActionTypeName(actionTypeDict!=null?actionTypeDict.getLabel():"");
            }
            else {
                item.setActionTypeName("");
            }
            //CustomerFinance finance = financeList.stream().filter(f -> f.getId().equals(item.getCustomer().getId())).findFirst().orElse(null);  // mark on 2019-6-29
            CustomerFinance finance = financeList.stream().filter(f -> f.getId().equals(item.getCustomer()==null?0:item.getCustomer().getId())).findFirst().orElse(null); //add on 2019-6-29
            if (finance != null){
                if (item.getCustomerFinance() != null) { // add on 2019-7-11
                    item.getCustomerFinance().setBalance(finance.getBalance());
                }
            }
        }
        page.setList(list);
        return page;
    }

    /**
     * 客户的冻结金额流水列表
     *
     * @param customerId
     * @param salesId
     * @param currencyType
     * @param currencyNo
     * @param beginDate
     * @param endDate
     * @param page
     * @return
     */
    public Page<CustomerCurrency> getCustomerBlockAmountList(Long customerId, Long salesId, Integer currencyType, String currencyNo,
                                                             Date beginDate, Date endDate, Page<CustomerCurrency> page) {
        //TODO: 切分冻结流水
//        List<CustomerCurrency> list = ctCustomerCurrencyDao.getCustomerBlockAmountList(customerId, salesId, beginDate, endDate, currencyType, currencyNo, page);
        List<CustomerCurrency> list = customerBlockCurrencyService.getCustomerBlockCurrencyList(customerId, salesId,currencyType, currencyNo, beginDate, endDate, page);
        List<Long> ids = list.stream().map(cc -> cc.getCustomer().getId()).distinct().collect(Collectors.toList());
        List<CustomerFinance> financeList = ctCustomerFinanceDao.getBlockAmountByIds(ids);

        //Customer customer = customerId == null ? null: msCustomerService.get(customerId); // 切换微服务 on 2019-6-29
        //Map<Long, Customer> allCustomerMap = CustomerUtils.getAllCustomerMap();  // mark on 2019-6-29

        Map<Long, Customer> customerMap = Maps.newHashMap();
        for (CustomerCurrency item : list) {
            /*
            // mark on 2019-6-29
            // 注释原因: 只选择一个客户
            if (item.getCustomer() != null && item.getCustomer().getId() != null) {
                Customer customer = allCustomerMap.get(item.getCustomer().getId());
                if (customer != null) {
                    item.setCustomer(customer);
                }
            }
            */
            Customer customer = null;
            if (item.getCustomer() != null && item.getCustomer().getId() != null) {
                customer = customerMap.get(item.getCustomer().getId());
                if (customer == null) {
                    log.warn("customerMap没有获取到：{}", item.getCustomer().getId());
                    customer = msCustomerService.get(item.getCustomer().getId());
                }
            }
            if (customer != null) {  // 从微服务中获取的customer
                item.setCustomer(customer);
                customerMap.put(customer.getId(),customer);
            }

            //切换为微服务
            Map<String, Dict> currencyTypeMap = MSDictUtils.getDictMap("BlockCurrencyType");
            if (item.getCurrencyType() != null && item.getCurrencyType() > 0) {
                Dict currencyTypeDict = currencyTypeMap.get(item.getCurrencyType().toString());
                item.setCurrencyTypeName(currencyTypeDict != null ? currencyTypeDict.getLabel() : "");
            } else {
                item.setCurrencyTypeName("");
            }

            CustomerFinance finance = financeList.stream().filter(f -> f.getId().equals(item.getCustomer().getId())).findFirst().orElse(null);
            if (finance != null){
                item.getCustomerFinance().setBlockAmount(finance.getBlockAmount());
            }
        }
        customerMap.clear();
        page.setList(list);
        return page;
    }

    /**
     * 客户充值 界面保存充值信息
     *
     * @param customerCurrency
     * @return
     */
    @Transactional
    public void onSave(CustomerCurrency customerCurrency) {
        if (customerCurrency.getId() == null || customerCurrency.getId() < 0) {
            try {
                customerCurrency.setId(SeqUtils.NextIDValue(SeqUtils.TableName.CustomerCurrency));
                CustomerFinance customerFinance = ctCustomerFinanceDao.get(customerCurrency.getCustomer().getId());
                //切换为微服务
                if (customerFinance.getPaymentType() != null && Integer.parseInt(customerFinance.getPaymentType().getValue()) > 0) {
                    String paymentTypeLabel = MSDictUtils.getDictLabel(customerFinance.getPaymentType().getValue(), "PaymentType", "");
                    customerFinance.getPaymentType().setLabel(paymentTypeLabel);
                }
                customerCurrency.setBeforeBalance(customerFinance.getBalance());
                customerCurrency.setBalance(customerCurrency.getBeforeBalance() + customerCurrency.getAmount());
                customerCurrency.setActionType(CustomerCurrency.ACTION_TYPE_RECHARGE);
                customerCurrency.setCurrencyNo(SeqUtils.NextSequenceNo("CustomerCurrencyNo"));
                String strQuarter = DateUtils.getYear() + DateUtils.getSeason();
                customerCurrency.setQuarter(strQuarter);
                customerCurrency.preInsert();
                ctCustomerCurrencyDao.insert(customerCurrency);

                customerFinance.setTransactionAmount(customerCurrency.getAmount());
                customerFinance.preUpdate();
                ctCustomerFinanceDao.updateBalance(customerFinance);

                //cache begin
//                Customer customer = customerDao.get(customerFinance.getId());
//                double score= Double.parseDouble(customer.getId().toString());
//                redisUtils.zRemRangeByScore("all:customer",score,score);
//                redisUtils.zAdd("all:customer", customer, customer.getId(), 0);
                //cache end
            } catch (Exception ex) {
                throw new RuntimeException(ex.getMessage());
            }
        } else {
            throw new RuntimeException("传入参数为空.");
        }
    }

    public Page<CustomerOfflineRechargeModel> getCustomerOfflineRecharge(Page<CustomerOfflineRechargeModel> offlineRechargeModelPage , CustomerOfflineRechargeSearch search){
        search.setPage(new MSPage<>(offlineRechargeModelPage.getPageNo(),offlineRechargeModelPage.getPageSize()));
        MSResponse<MSPage<CustomerOfflineRecharge>> pageMSResponse = customerOfflineRechargeFeign.findListForCustomer(search);
        if(MSResponse.isSuccess(pageMSResponse)){
            MSPage<CustomerOfflineRecharge> data = pageMSResponse.getData();
            offlineRechargeModelPage.setCount(data.getRowCount());
            List<CustomerOfflineRechargeModel> list = Mappers.getMapper(CustomerOffineRechargeModelMapper.class).toViewModels(data.getList());
            offlineRechargeModelPage.setList(list);
        }else {
            offlineRechargeModelPage.setCount(0);
            offlineRechargeModelPage.setList(Lists.newArrayList());
        }
        return offlineRechargeModelPage;
    }
}
