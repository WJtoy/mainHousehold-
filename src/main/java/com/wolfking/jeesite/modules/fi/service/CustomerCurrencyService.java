package com.wolfking.jeesite.modules.fi.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.fi.recharge.CustomerOfflineRecharge;
import com.kkl.kklplus.entity.fi.recharge.CustomerOfflineRechargeSearch;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.service.LongIDCrudService;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.fi.dao.CustomerCurrencyDao;
import com.wolfking.jeesite.modules.fi.entity.CustomerCurrency;
import com.wolfking.jeesite.modules.md.dao.CustomerFinanceDao;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.CustomerFinance;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
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

/**
 * Created on 2017-05-02.
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Slf4j
public class CustomerCurrencyService extends LongIDCrudService<CustomerCurrencyDao, CustomerCurrency> {

    @Resource
    private CustomerFinanceDao customerFinanceDao;

    @Autowired
    private MSCustomerService msCustomerService;

    @Autowired
    private CustomerOfflineRechargeFeign customerOfflineRechargeFeign;

    @Autowired
    private CustomerBlockCurrencyService customerBlockCurrencyService;

    /**
     * 按流水单号和类型返回流水单据
     *
     * @param strCurrencyNo 流水单号
     * @param actionType    流水类型
     * @return
     */
    public CustomerCurrency getByCurrencyNo(String strCurrencyNo, Integer actionType) {
        return dao.getByCurrencyNo(strCurrencyNo, actionType);
    }

    /**
     * 按订单单号和类型返回流水单据
     *
     * @param orderNo     订单号
     * @param actionTypes 流水类型
     * @return
     */
    public List<CustomerCurrency> getByOrderNoAndActionTypes(String orderNo, Integer[] actionTypes) {
        return dao.getByOrderNoAndActionTypes(orderNo, actionTypes);
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
        List<CustomerCurrency> list = dao.findCurrencyList(customerCurrency);

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
        List<CustomerFinance> financeList = customerFinanceDao.getBalanceByIds(ids);

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
//        List<CustomerCurrency> list = dao.getCustomerBlockAmountList(customerId, salesId, beginDate, endDate, currencyType, currencyNo, page);
        List<CustomerCurrency> list = customerBlockCurrencyService.getCustomerBlockCurrencyList(customerId, salesId,currencyType, currencyNo, beginDate, endDate, page);
        List<Long> ids = list.stream().map(cc -> cc.getCustomer().getId()).distinct().collect(Collectors.toList());
        List<CustomerFinance> financeList = customerFinanceDao.getBlockAmountByIds(ids);

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
    @Transactional(readOnly = false)
    public void onSave(CustomerCurrency customerCurrency) {
        if (customerCurrency.getId() == null || customerCurrency.getId() < 0) {
            try {
                customerCurrency.setId(SeqUtils.NextIDValue(SeqUtils.TableName.CustomerCurrency));
                CustomerFinance customerFinance = customerFinanceDao.get(customerCurrency.getCustomer().getId());
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
                dao.insert(customerCurrency);

                customerFinance.setTransactionAmount(customerCurrency.getAmount());
                customerFinance.preUpdate();
                customerFinanceDao.updateBalance(customerFinance);

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

    // 在线充值保存临时单据
    @Transactional(readOnly = false)
    public void saveTempCurrency(CustomerCurrency customerCurrency) {
        if (super.get(customerCurrency.getId()) == null) {
            Long id = dao.checkRepeate(customerCurrency.getCurrencyNo(), new Integer[]{CustomerCurrency.ACTION_TYPE_TEMPRECHARGE, CustomerCurrency.ACTION_TYPE_TEMPRECHARGE2, CustomerCurrency.CURRENCY_TYPE_IN});
            if (id != null && id > 0) {
                throw new RuntimeException("重复提交");
            }
            try {
                CustomerFinance customerFinance = customerFinanceDao.get(customerCurrency.getCustomer().getId());
                //切换为微服务
                if (customerFinance.getPaymentType() != null && Integer.parseInt(customerFinance.getPaymentType().getValue()) > 0) {
                    String paymentTypeLabel = MSDictUtils.getDictLabel(customerFinance.getPaymentType().getValue(), "PaymentType", "");
                    customerFinance.getPaymentType().setLabel(paymentTypeLabel);
                }
                customerCurrency.setBeforeBalance(customerFinance.getBalance());
                customerCurrency.setBalance(customerCurrency.getBeforeBalance() + customerCurrency.getAmount());
                customerCurrency.setActionType(CustomerCurrency.ACTION_TYPE_TEMPRECHARGE);
                customerCurrency.setPaymentType(CustomerCurrency.PAYMENT_TYPE_TRANSFER_ACCOUNT);  //20 转帐
                String strQuarter = DateUtils.getYear() + DateUtils.getSeason();
                customerCurrency.setQuarter(strQuarter);
                customerCurrency.preInsert();
                dao.insert(customerCurrency);
            } catch (Exception ex) {
                throw new RuntimeException(ex.getMessage());
            }

        } else {
            throw new RuntimeException("传入参数为空.");
        }
    }

    @Transactional(readOnly = false)
    public void updateEntity(CustomerCurrency entity) {
        User user = new User(0l);
        Date date = new Date();
        CustomerFinance customerFinance = customerFinanceDao.get(entity.getCustomer().getId());
        //切换为微服务
        if (customerFinance.getPaymentType() != null && Integer.parseInt(customerFinance.getPaymentType().getValue()) > 0) {
            String paymentTypeLabel = MSDictUtils.getDictLabel(customerFinance.getPaymentType().getValue(), "PaymentType", "");
            customerFinance.getPaymentType().setLabel(paymentTypeLabel);
        }
        //更新旧的流水状态
        entity.setBeforeBalance(customerFinance.getBalance());
        entity.setBalance(entity.getBeforeBalance() + entity.getAmount());
        entity.setActionType(CustomerCurrency.ACTION_TYPE_TEMPRECHARGE2);
        entity.setUpdateBy(user);
        entity.setUpdateDate(date);
        dao.updateActionType(entity);

        Date createDate = entity.getCreateDate();//前一笔创建日期

        //充值成功新增一笔流水
        entity.setId(SeqUtils.NextIDValue(SeqUtils.TableName.CustomerCurrency));
        entity.setBeforeBalance(customerFinance.getBalance());
        entity.setBalance(entity.getBeforeBalance() + entity.getAmount());
        entity.setActionType(CustomerCurrency.ACTION_TYPE_CHARGEONLINE);
        entity.setPaymentType(CustomerCurrency.PAYMENT_TYPE_TRANSFER_ACCOUNT);  //20 转帐
        String strQuarter = DateUtils.getYear() + DateUtils.getSeason();
        entity.setQuarter(strQuarter);
        entity.setCreateBy(user);
        entity.setCreateDate(entity.getCreateDate());//订单产生日期
        entity.setUpdateDate(date);//实际付款日期
        entity.setUpdateBy(user);
        entity.setRemarks(entity.getCurrencyNo());
        entity.setCreateDate(createDate);
        entity.setCreateBy(user);
        dao.insert(entity);

        customerFinance.setTransactionAmount(entity.getAmount());
        customerFinance.setUpdateBy(user);
        customerFinance.setUpdateDate(date);
        customerFinanceDao.updateBalance(customerFinance);
    }

    public Page<CustomerOfflineRechargeModel> getCustomerOfflineRecharge(Page<CustomerOfflineRechargeModel> offlineRechargeModelPage ,CustomerOfflineRechargeSearch search){
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
