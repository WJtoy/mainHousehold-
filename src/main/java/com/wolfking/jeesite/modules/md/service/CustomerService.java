package com.wolfking.jeesite.modules.md.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.entity.common.NameValuePair;
import com.kkl.kklplus.entity.md.MDCustomer;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.persistence.LongIDDataEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.service.LongIDCrudService;
import com.wolfking.jeesite.common.service.ServiceException;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.wolfking.jeesite.modules.fi.dao.CustomerCurrencyDao;
import com.wolfking.jeesite.modules.fi.entity.CustomerCurrency;
import com.wolfking.jeesite.modules.md.dao.CustomerDao;
import com.wolfking.jeesite.modules.md.dao.CustomerFinanceDao;
import com.wolfking.jeesite.modules.md.dao.CustomerProductDao;
import com.wolfking.jeesite.modules.md.dao.MdAttachmentDao;
import com.wolfking.jeesite.modules.md.entity.*;
import com.wolfking.jeesite.modules.md.entity.viewModel.CustomerPrices;
import com.wolfking.jeesite.modules.sys.dao.UserDao;
import com.wolfking.jeesite.modules.sys.entity.*;
import com.wolfking.jeesite.modules.sys.service.SystemService;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.modules.sys.utils.SeqUtils;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.modules.td.entity.Message2;
import com.wolfking.jeesite.ms.providermd.service.*;
import com.wolfking.jeesite.ms.service.sys.MSUserService;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import com.wolfking.jeesite.ms.utils.MSUserUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created on 2017-04-12.
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class CustomerService extends LongIDCrudService<CustomerDao, Customer> {

    private static int RETRY_TIMES = 3;

    //@Resource
    //private CustomerDao customerDao;  //mark on 2020-2-11
    @Resource
    private CustomerFinanceDao customerFinanceDao;
    @Resource
    private MdAttachmentDao mdAttachmentDao;
    @Resource
    private CustomerProductDao customerProductDao;
    /*
    @Resource
    private CustomerAccountProfileDao profileDao;
    */
    @Resource
    private UserDao userDao;

    @Autowired
    private ServiceTypeService typeService;
    @Autowired
    private ProductPriceService productPriceService;
    @Autowired
    private SystemService systemService;

    @Autowired
    private RedisUtils redisUtils;

    @Resource
    private CustomerCurrencyDao customerCurrencyDao;

//    @Resource(name = "customerAdapter")
//    private CustomerAdapter customerAdapter;

    @Autowired
    private MSUserService msUserService;

    @Autowired
    private MSCustomerService msCustomerService;

//    @Autowired
//    protected MapperFacade mapperFacade;

    @Autowired
    private MSCustomerAccountProfileService msCustomerAccountProfileService;

//    @Autowired
//    private CustomerChangeSalesSender customerChangeSalesSender;

    @Autowired
    private MSProductService msProductService;

    @Autowired
    private MSCustomerProductService msCustomerProductService;

    @Autowired
    private MSCustomerPriceService msCustomerPriceService;

    @Autowired
    private MSAttachmentService msAttachmentService;


    //region 客户

    //region 财务
    /**
     * 获得客户帐号当前余额
     */
    public Double getBalanceAmount(Long id) {
        return customerFinanceDao.getBalanceAmount(id);
    }

    public CustomerFinance getFinance(long id) {
        //切换为微服务
        CustomerFinance finance = customerFinanceDao.get(id);
        if (finance != null && finance.getPaymentType() != null && Integer.parseInt(finance.getPaymentType().getValue()) > 0) {
            String paymentTypeLabel = MSDictUtils.getDictLabel(finance.getPaymentType().getValue(), "PaymentType", "");
            finance.getPaymentType().setLabel(paymentTypeLabel);
        }
        return finance;
    }

    /**
     * For 下单
     * 从主库读取，只返回：余额，冻结金额，信用额度标记，信用额度,支付方式等
     *
     * @param id
     * @return
     */
    public CustomerFinance getFinanceForAddOrder(long id) {
        //切换为微服务
        CustomerFinance finance = customerFinanceDao.getForAddOrder(id);
        if (finance != null && finance.getPaymentType() != null && Integer.parseInt(finance.getPaymentType().getValue()) > 0) {
            String paymentTypeLabel = MSDictUtils.getDictLabel(finance.getPaymentType().getValue(), "PaymentType", "");
            finance.getPaymentType().setLabel(paymentTypeLabel);
        }
        return finance;
    }

    /**
     * 更新余额，做加法运算
     * @param finance
     */
    @Transactional
    public void increaseFinanceAmount(CustomerFinance finance){
        customerFinanceDao.updateBalanceForRecharge(finance);
    }

    //endregion 财务

    /**
     * 按用户id或者客户id查找客户列表
     *
     * @param paramMap
     * @return
     */
    public java.util.List<Customer> findListByUserIdOrCustomerId(java.util.Map<String, Object> paramMap) {
//        List<Customer> customerList = dao.findListByUserIdOrCustomerId(paramMap);
//        if (customerList != null && customerList.size() > 0) {
//            List<Long> salesList = customerList.stream().map(customer -> customer.getSales().getId()).collect(Collectors.toList());
//            Map<Long, User> userMap = MSUserUtils.getMapByUserIds(salesList);
//            for (Customer customer : customerList) {
//                User sales = userMap.get(customer.getId());
//                if (sales != null) {
//                    customer.getSales().setName(sales.getName());
//                    customer.getSales().setMobile(sales.getMobile());
//                    customer.getSales().setQq(sales.getQq());
//                }
//            }
//        }
//        return customerList;

        List<Customer> customerList = Lists.newArrayList();
        List<Long> customerIdList = systemService.findCustomerIdList(paramMap);
        if (customerIdList != null && !customerIdList.isEmpty()) {
            //String strIds = customerIdList.stream().map(Object::toString).collect(Collectors.joining(","));  //mark on 2020-3-17
            //customerList = msCustomerService.findBatchByIds(strIds);    //mark on 2020-3-17

            customerList = msCustomerService.findListByBatchIds(customerIdList);  //add on 2020-3-17

            if (customerList != null && customerList.size() > 0) {
                List<Long> salesList = customerList.stream().map(customer -> customer.getSales().getId()).collect(Collectors.toList());

                if (salesList != null && salesList.size()>1) {
                    salesList = salesList.stream().distinct().collect(Collectors.toList());  // 去重复
                }
                Map<Long, User> userMap = MSUserUtils.getMapByUserIds(salesList);
                for (Customer customer : customerList) {
                    User sales = userMap.get(customer.getSales().getId());
                    if (sales != null) {
                        customer.getSales().setName(sales.getName());
                        customer.getSales().setMobile(sales.getMobile());
                        customer.getSales().setQq(sales.getQq());
                    }
                }
            }
        }
        return customerList;
    }

    /**
     * 根据客服id获取vip客户列表
     */
    public List<Customer> findVipListByKefu(Long kefuId) {
        List<Customer> customerList = null;
        List<Long> customerIdList = systemService.findVipCustomerIdListByKefu(kefuId);
        if (!CollectionUtils.isEmpty(customerIdList)) {
            //String strIds = customerIdList.stream().map(Object::toString).collect(Collectors.joining(","));  //mark on 2020-3-17
            //customerList = msCustomerService.findBatchByIds(strIds);   //mark on 2020-3-17
            customerList = msCustomerService.findListByBatchIds(customerIdList);  //add on 2020-3-17
        }
        return customerList==null?Lists.newArrayList():customerList;
    }

    public List<Customer> findVipListWithIdAndNameByKefu(Long kefuId) {
        //
        // add on 2020-3-17
        //
        List<Customer> customerList = null;
        List<Long> customerIdList = systemService.findVipCustomerIdListByKefu(kefuId);
        if (!CollectionUtils.isEmpty(customerIdList)) {
            customerList = msCustomerService.findIdAndNameListByIds(customerIdList);
        }
        return customerList==null?Lists.newArrayList():customerList;
    }

    /**
     * 根据客户id列表获取客户id，name列表  2021-6-12
     * @param customerIds
     * @return
     */
    public List<Customer> findIdAndNameListByIds(List<Long> customerIds) {
        List<Customer> customerList = Lists.newArrayList();
        if (customerIds != null && !customerIds.isEmpty()) {
            if (customerIds.size() < 50) {
                customerList = msCustomerService.findIdAndNameListByIds(customerIds);
            } else {
                List<Customer> tempCustomers = Lists.newArrayList();
                Lists.partition(customerIds, 50).forEach(partCustomerIds -> {
                    List<Customer> partCustomers = msCustomerService.findIdAndNameListByIds(partCustomerIds);
                    tempCustomers.addAll(partCustomers);
                });
                customerList.addAll(tempCustomers);
            }
        }
        return customerList;
    }

    /**
     * 获得业务负责的客户列表  todo:2018/08/28 comment by ryan

     public List<Customer> findCustomerOfSales(Long userId) {
     String key = String.format(RedisConstant.SHIRO_KEFU_CUSTOMER, userId);
     //from cache
     if (redisUtils.exists(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key)) {
     return redisUtils.getList(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key, Customer[].class);
     }
     //filter from customer all list of cache
     //List<Customer> customers = findAll();
     //customers = customers.stream().filter(t -> Objects.equals(t.getSales().getId(),userId)).collect(Collectors.toList());
     List<Customer> customers = dao.getCustomerListOfSales(userId);
     //sync to cache
     if (customers != null && customers.size() > 0) {
     long timeout = Long.valueOf(Global.getConfig("cache.timeout")) - new Random().nextInt(100);
     redisUtils.setEX(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key, customers, timeout);
     }
     return customers;
     }
     */

    /**
     * 获得客服负责的客户列表  todo:2018/08/28 comment by ryan
     * 无负责的客户，不写入redis
     * 由 key/value -> zset

     public List<Customer> findCustomerOfKefu(Long kefuId) {
     String key = String.format(RedisConstant.SHIRO_KEFU_CUSTOMER, kefuId);
     //from cache
     if (redisUtils.exists(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key)) {
     //return redisUtils.getList(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key, Customer[].class);
     return redisUtils.zRange(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key, 0, -1, Customer.class);
     }
     //from db
     List<Customer> customers = Lists.newArrayList();
     Map<String, Object> paramMap = Maps.newHashMap();
     paramMap.put("userId", kefuId);
     customers = customerDao.findListByUserIdOrCustomerId(paramMap);
     if (customers == null || customers.size() == 0) {
     return null;
     }
     //sync to cache
     long timeout = Long.valueOf(Global.getConfig("cache.timeout")) - new Random().nextInt(100);
     Set<RedisZSetCommands.Tuple> sets = customers.stream()
     .map(t -> new RedisTuple(redisUtils.gsonRedisSerializer.serialize(t), t.getId().doubleValue()))
     .collect(Collectors.toSet());

     redisUtils.zAdd(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key, sets, timeout);
     //redisUtils.setEX(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB,key, customers, timeout);

     return customers;
     }
     */

    /**
     * 检查客服是否负责指定客户
     */
    public Boolean checkAssignedCustomer(Long kefuId, double customerId) {
        String key = String.format(RedisConstant.SHIRO_KEFU_CUSTOMER, kefuId);
        if (redisUtils.exists(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key)) {
            Customer customer = (Customer) redisUtils.zRangeOneByScore(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key, customerId, customerId, Customer.class);
            return customer != null;
        }
        return false;
    }

    /**
     * 检查客服是否负责指定客户 todo:2018/08/28 comment by ryan
     * <p>
     * public Boolean checkAssignedCustomer(Long kefuId) {
     * String key = String.format(RedisConstant.SHIRO_KEFU_CUSTOMER, kefuId);
     * if (redisUtils.exists(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key)) {
     * return redisUtils.zCard(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key) > 0;
     * }
     * return false;
     * }
     */

    //切换为微服务
    public List<Customer> findAll() {
        // mark on 2019-7-23 begin
        /*
        List<Customer> allCustomerList = dao.findAllList(new Customer());
        if (allCustomerList != null && allCustomerList.size() > 0) {
            Set<Long> salesList = allCustomerList.stream()
                    .filter(i -> i.getSales() != null && i.getSales().getId() != null)
                    .map(customer -> customer.getSales().getId()).collect(Collectors.toSet());
            Map<Long, User> userMap = MSUserUtils.getMapByUserIds(Lists.newArrayList(salesList));
            Map<String, Dict> paymentTypeMap = MSDictUtils.getDictMap("PaymentType");
            for (Customer customer : allCustomerList) {
                if (customer.getFinance().getPaymentType() != null && Integer.parseInt(customer.getFinance().getPaymentType().getValue()) > 0) {
                    customer.getFinance().setPaymentType(paymentTypeMap.get(customer.getFinance().getPaymentType().getValue()));
                    customer.setPaymentType(paymentTypeMap.get(customer.getPaymentType().getValue()));
                }
                if (customer.getSales() != null && customer.getSales().getId() != null) {
                    User sales = userMap.get(customer.getSales().getId());
                    if (sales != null) {
                        customer.setSales(sales);
                    }
                }
            }
        }
        return allCustomerList;
        */
        // mark on 2019-7-23 end

        // add on 2019-7-23 begin
        List<Customer> allCustomerList =  msCustomerService.findAll();
        List<Customer> filterCustomerList =  Lists.newArrayList();
        if (allCustomerList != null && allCustomerList.size() > 0) {
            allCustomerList.stream().forEach(customer -> {
                CustomerFinance customerFinance = getFinance(customer.getId());
                if (customerFinance != null) {
                    customer.setFinance(customerFinance);
                    filterCustomerList.add(customer);
                }
            });

            Set<Long> salesList = filterCustomerList.stream()
                    .filter(i -> i.getSales() != null && i.getSales().getId() != null)
                    .map(customer -> customer.getSales().getId()).collect(Collectors.toSet());
            Map<Long, User> userMap = MSUserUtils.getMapByUserIds(Lists.newArrayList(salesList));
            Map<String, Dict> paymentTypeMap = MSDictUtils.getDictMap("PaymentType");
            for (Customer customer : filterCustomerList) {
                if (customer.getFinance() != null && customer.getFinance().getPaymentType() != null && Integer.parseInt(customer.getFinance().getPaymentType().getValue()) > 0) {
                    customer.getFinance().setPaymentType(paymentTypeMap.get(customer.getFinance().getPaymentType().getValue()));
                    customer.setPaymentType(paymentTypeMap.get(customer.getPaymentType().getValue()));
                }
                if (customer.getSales() != null && customer.getSales().getId() != null) {
                    User sales = userMap.get(customer.getSales().getId());
                    if (sales != null) {
                        customer.setSales(sales);
                    }
                }
            }
        }
        // add on 2019-7-23 end
        return filterCustomerList!=null && !filterCustomerList.isEmpty() ? filterCustomerList.stream().sorted(Comparator.comparing(Customer::getCode)).collect(Collectors.toList()):Lists.newArrayList();
    }

    // 调用客户微服务获取id/name
    public List<NameValuePair<Long, String>> findBatchListByIds(List<Long> customerIds) {
        List<NameValuePair<Long, String>> list = msCustomerService.findBatchListByIds(customerIds);
        return list == null ? Lists.newArrayList() : list;
    }

    /**
     * 获得所有客户基本信息
     * 不读取md_customer_finance
     */
    public List<Customer> findAllBaseList() {
        List<Customer> allCustomerList =  msCustomerService.findAll();
        return allCustomerList==null?Lists.newArrayList():allCustomerList;
    }

    public List<Customer> findNoVIPListWithIdAndName() {
        return msCustomerService.findNoVIPList();
    }

    /**
     * 获得所有客户基本信息
     * 不读取md_customer_finance
     */
    public List<Customer> findAllSpecifiedColumnList() {
        List<Customer> allCustomerList =  msCustomerService.findAllSpecifiedColumn();
        return allCustomerList==null?Lists.newArrayList():allCustomerList;
    }


    public Page<Customer> find(Page<Customer> page, Customer customer) {
        customer.setPage(page);
        // 执行分页查询
        List<Customer> temp = null; //customerDao.rptFindCustomerList(customer);  //mark on 2020-2-11
        if (temp != null && temp.size() > 0) {
            List<Long> salesList = temp.stream().map(c -> c.getSales().getId()).collect(Collectors.toList());
            Map<Long, User> userMap = MSUserUtils.getMapByUserIds(salesList);
            for (Customer c : temp) {
                User sales = userMap.get(c.getId());
                if (sales != null) {
                    c.getSales().setName(sales.getName());
                    c.getSales().setMobile(sales.getMobile());
                    c.getSales().setQq(sales.getQq());
                }
            }
        }
        page.setList(temp);
        return page;
    }

    public Page<Customer> findApprov(Page<Customer> page, Customer customer) {
        customer.setPage(page);
        List<Customer> customers = null; //customerDao.findApproveList(customer);  //mark on 2020-2-11 web端去customer
        if (customers != null && customers.size() > 0) {
            List<Long> salesList = customers.stream().map(c -> c.getSales().getId()).collect(Collectors.toList());
            Map<Long, User> userMap = MSUserUtils.getMapByUserIds(salesList);
            for (Customer c : customers) {
                User sales = userMap.get(c.getId());
                if (sales != null) {
                    c.getSales().setName(sales.getName());
                    c.getSales().setMobile(sales.getMobile());
                    c.getSales().setQq(sales.getQq());
                }
            }
        }
        // 执行分页查询
        page.setList(customers);
        return page;
    }

    @Transactional(readOnly = false)
    public void approve(String userId) throws Exception {
        try {
            for (String uid : userId.split(",")) {

                Customer customer = null; //customerDao.getApprove(Long.parseLong(uid));  //mark on 2020-2-11 web端去customer
                User sales = MSUserUtils.get(customer.getSales().getId());
                if (sales != null) {
                    customer.getSales().setName(sales.getName());
                    customer.getSales().setMobile(sales.getMobile());
                    customer.getSales().setQq(sales.getQq());
                }

                customer.setDelFlag(User.DEL_FLAG_NORMAL);

                List<CustomerAccountProfile> list = customer.getCustomerAccountProfile();
                //customerDao.update(customer); //mark on 2020-2-11
                //cache
                double score = Double.parseDouble(customer.getId().toString());
                //按权重(客户id)删除
//                redisUtils.zRemRangeByScore(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_CUSTOMER_ALL, score, score);  //,
//                redisUtils.zAdd(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_CUSTOMER_ALL, customer, customer.getId(), 0);

                String mobile = customer.getPhone();
                String content = "温馨提示:"
                        .concat("您注册快可立全国联保信息已经审核通过，现在就去体验一番吧。。。");
                Date date = DateUtils.addDays(
                        new Date(System.currentTimeMillis()), 3);

                Message2 message = new Message2(mobile,
                        content, Message2.EXTNO_DEFAULT);
            }

        } catch (Exception e) {
            throw new Exception("客户审核失败:" + e.getMessage());
        }
    }

    public Customer get(long id) {
        //Customer customer = customerDao.get(id);   // mark on 2019-7-22
        Customer customer = msCustomerService.getByIdToCustomerSpecifiedColumn(id);  //add on 2019-7-22 // 微服务调用
        if (customer != null && customer.getSales() != null && customer.getSales().getId() != null) {
            User sales = MSUserUtils.get(customer.getSales().getId());
            if (sales != null) {
                customer.getSales().setName(sales.getName());
                customer.getSales().setMobile(sales.getMobile());
                customer.getSales().setQq(sales.getQq());
            }
            CustomerFinance customerFinance = getFinance(id);
            if (customerFinance != null) {
                customer.setFinance(customerFinance);
            }
            if (customer.getMerchandiser()!=null && customer.getMerchandiser().getId()!=null) {
                User merchandiser = MSUserUtils.get(customer.getMerchandiser().getId());
                if (merchandiser != null) {
                    customer.getMerchandiser().setName(merchandiser.getName());
                }
            }
        }
        return customer;
    }

    public boolean existsCustomerByCode(String name) {
        //return customerDao.existsCustomerByCode(name) != null;  // mark on 2019-7-23
        return msCustomerService.getCustomerIdByCode(name);   // add on 2019-7-23   // customer微服务调用
    }

    @Transactional(readOnly = false)
    public void save(Customer customer, Double oldCredit) throws Exception {
        boolean isNew = customer.getIsNewRecord();
        //List<Product> productList = Lists.newArrayList(); //mark on 2019-9-29
        List<Long> productIds = Lists.newArrayList();
        customer.setDelFlag(customer.DEL_FLAG_NORMAL);
        MdAttachment logo = null;
        MdAttachment attachment1 = null;
        MdAttachment attachment2 = null;
        MdAttachment attachment3 = null;
        MdAttachment attachment4 = null;

        if (customer.getLogo() == null) {
            customer.setLogo(new MdAttachment(0l));
        } else if (customer.getLogo().canAdd()) {
            logo = new MdAttachment();
            // 原先的
//            mdAttachmentDao.insert(customer.getLogo());
            logo = customer.getLogo();
            logo.preInsert();
            logo.setId(null);
        } else {
            customer.getLogo().setId(0l);
        }

        if (customer.getAttachment1() == null) {
            customer.setAttachment1(new MdAttachment(0l));
        } else if (customer.getAttachment1().canAdd()) {
            attachment1 = new MdAttachment();
//            mdAttachmentDao.insert(customer.getAttachment1());
            attachment1 = customer.getAttachment1();
            attachment1.setId(null);
            attachment1.preInsert();
        } else {
            customer.getAttachment1().setId(0l);
        }

        if (customer.getAttachment2() == null) {
            customer.setAttachment2(new MdAttachment(0l));
        } else if (customer.getAttachment2().canAdd()) {
            attachment2 = new MdAttachment();
//            mdAttachmentDao.insert(customer.getAttachment2());
            attachment2 = customer.getAttachment2();
            attachment2.setId(null);
            attachment2.preInsert();
        } else {
            customer.getAttachment2().setId(0l);
        }

        if (customer.getAttachment3() == null) {
            customer.setAttachment3(new MdAttachment(0l));
        } else if (customer.getAttachment3().canAdd()) {
            attachment3 = new MdAttachment();
//            mdAttachmentDao.insert(customer.getAttachment3());
            attachment3 = customer.getAttachment3();
            attachment3.setId(null);
            attachment3.preInsert();
        } else {
            customer.getAttachment3().setId(0l);
        }

        if (customer.getAttachment4() == null) {
            customer.setAttachment4(new MdAttachment(0l));
        } else if (customer.getAttachment4().canAdd()) {
            attachment4 = new MdAttachment();
//            mdAttachmentDao.insert(customer.getAttachment4());
            attachment4 = customer.getAttachment4();
            attachment4.setId(null);
            attachment4.preInsert();
        } else {
            customer.getAttachment4().setId(0l);
        }

        Customer customer1 = null;
        if (!isNew) {
            customer1 = getFromCache(customer.getId());
        }
        //业务员
        User sales = systemService.getUser(customer.getSales().getId());
        if (sales == null) {
            throw new RuntimeException("读取业务员联系信息失败，请重试");
        }
        //paymentType
        CustomerFinance finance = customer.getFinance();
        customer.setPaymentType(finance.getPaymentType());
        //super.save(customer);  //mark on 2020-2-11
        // add on 2019-7-2 begin

        if(customer.getVipFlag() == 0){
            customer.setVip(0);
        }
        // 插入附件图片
        try {
            // 调用attachment微服务
            if (logo != null) {
                msAttachmentService.insert(logo);
                customer.setLogo(new MdAttachment(logo.getId()));
            }
            if (attachment1 != null) {
                msAttachmentService.insert(attachment1);
                customer.setAttachment1(new MdAttachment(attachment1.getId()));
            }
            if (attachment2 != null) {
                msAttachmentService.insert(attachment2);
                customer.setAttachment2(new MdAttachment(attachment2.getId()));
            }
            if (attachment3 != null) {
                msAttachmentService.insert(attachment3);
                customer.setAttachment3(new MdAttachment(attachment3.getId()));
            }
            if (attachment4 != null) {
                msAttachmentService.insert(attachment4);
                customer.setAttachment4(new MdAttachment(attachment4.getId()));
            }

        } catch (Exception e) {
            throw new RuntimeException("保存附件信息失败:" + e.getMessage());
        }

        // add on 2020-8-14 begin
        if (isNew) {
            if (customer.getUseDefaultPrice() == 0) {
                customer.setCustomizePriceFlag(1);  // 自定义价格
                customer.setUseDefaultPrice(10);
            } else if (customer.getUseDefaultPrice() >= 10) {
                customer.setUseDefaultPrice(10);
                customer.setCustomizePriceFlag(0);
            }
        } else {
            customer.setCustomizePriceFlag(null);
            customer.setUseDefaultPrice(10);
        }
        // add on 2020-8-14 end

        // 调用customer微服务
        MSErrorCode msErrorCode = msCustomerService.save(customer, isNew);
        if (msErrorCode.getCode() != 0) {
            throw new RuntimeException("保存客户信息到微服务失败!失败原因: " + msErrorCode.getMsg());
        }
        // add on 2019-7-2 end

        customer.setSales(sales);//2018/01/25
        Long customerId = customer.getId();

        if (isNew) {
            //fi
            customer.setIsFrontShow(0);
            customer.getFinance().setId(customerId);
            finance.preInsert();
            customerFinanceDao.insert(finance);

            //客户主帐号
            User customerAccount = new User();
            customerAccount.setCompany(new Office(customerId));//客户id
            customerAccount.setName(customer.getMaster());
            customerAccount.setLoginName(customer.getPhone());
            customerAccount.setPhone(customer.getPhone());
            customerAccount.setMobile(customer.getPhone());
            customerAccount.setPassword(SystemService.entryptPassword(StringUtils.right(customer.getPhone().trim(), 6)));//手机号后6位
            customerAccount.setCreateBy(customer.getCreateBy());
            customerAccount.setCreateDate(customer.getCreateDate());
            customerAccount.setUserType(User.USER_TYPE_CUSTOMER);//主帐号
            customerAccount.setSubFlag(0);
            customerAccount.setCompany(new Office(customer.getId()));
            customerAccount.setRemarks(customer.getName());
            // 角色:客户主帐号
            List<Role> roleList = Lists.newArrayList();
            //vip厂商主帐号使用角色7，普通厂商主帐号使用角色4
            Role r = systemService.getRole(4l);
            roleList.add(r);
            customerAccount.setRoleList(roleList);

            CustomerAccountProfile profile = new CustomerAccountProfile();
            profile.setRemarks(customer.getName());
            profile.setCreateBy(customer.getCreateBy());
            profile.setCreateDate(customer.getCreateDate());
            profile.setCustomer(customer);
            profile.setOrderApproveFlag(0);//不需审核订单
            //profileDao.insert(profile);   //mark on 2020-1-11
            // add on 2019-7-27 begin
            // 调用customer微服务
            MSErrorCode msErrorCodeForCustomerAccountProfile = msCustomerAccountProfileService.save(profile, isNew);
            if (msErrorCodeForCustomerAccountProfile.getCode() != 0) {
                throw new RuntimeException("保存客户账号信息到微服务失败!失败原因: " + msErrorCodeForCustomerAccountProfile.getMsg());
            }
            // add on 2019-7-27 end
            customerAccount.setCustomerAccountProfile(profile);
            customerAccount.preInsert();
            userDao.insert(customerAccount);
            userDao.insertUserRole(customerAccount);//角色
            MSUserUtils.addUserToRedis(customerAccount);//user微服务

            // add on 2019-8-7 begin
            //从字典中获取节点信息

            //改为md微服务调用 add no 2019-12-24
            /*String strQuarterCount = MSDictUtils.getDictSingleValue("quarterCount","0");
            Integer iQuarterCount = StringUtils.toInteger(strQuarterCount);
            if (iQuarterCount != null) {
                List<String> quarterList = getQuarters(iQuarterCount);
                if (quarterList != null && !quarterList.isEmpty()) {
                    quarterList.stream().forEach(s -> {
                        // 生成业务员mq
                        MQCustomerSalesMessage.CustomerSalesMessage.Builder customerSalesMessage = MQCustomerSalesMessage.CustomerSalesMessage.newBuilder();
                        customerSalesMessage.setSyncType(MQCustomerSalesMessage.SyncType.ADD);
                        customerSalesMessage.setCustomerId(customer.getId());
                        customerSalesMessage.setSalesId(customer.getSales().getId());
                        customerSalesMessage.setSubFlag(GlobalMappingSalesSubFlagEnum.SALES.getValue());
                        customerSalesMessage.setQuarter(s);

                        customerChangeSalesSender.send(customerSalesMessage.build());

                        // 暂时不上此功能2019-11-19
                        // 生成跟单员mq
                        MQCustomerSalesMessage.CustomerSalesMessage.Builder customerSalesMessageForMerchandiser = MQCustomerSalesMessage.CustomerSalesMessage.newBuilder();
                        customerSalesMessageForMerchandiser.setSyncType(MQCustomerSalesMessage.SyncType.ADD);
                        customerSalesMessageForMerchandiser.setCustomerId(customer.getId());
                        customerSalesMessageForMerchandiser.setSalesId(customer.getMerchandiser().getId());
                        customerSalesMessageForMerchandiser.setSubFlag(GlobalMappingSalesSubFlagEnum.MERCHANDISER.getValue());
                        customerSalesMessageForMerchandiser.setQuarter(s);

                        customerChangeSalesSender.send(customerSalesMessageForMerchandiser.build());
                    });
                }
            }*/
            // add on 2019-8-7 end

        } else {
            //productList = customerProductDao.getProductIdsById(customerId);//原产品列表 mark on 2019-9-27
            // 调用微服务 2019-9-27
            productIds = msCustomerProductService.getProductIdsById(customerId);

            //customerProductDao.deleteByCustomer(customerId); add on  2020-1-8
            //调用微服务 2019-9-27 add on 2020-1-8
            /*MSErrorCode customerProductErrorCode = msCustomerProductService.deleteByCustomer(customerId);
            if(customerProductErrorCode.getCode()>0){
                throw new RuntimeException("删除客户产品失败.失败原因:"+customerProductErrorCode.getMsg());
            }*/
            //fi
            customer.getFinance().setId(customerId);
            finance.preUpdate();
            customerFinanceDao.update(finance);
            //修改客户主帐号
            if (customer1.getPhone() != null && customer1.getPhone().length() > 1) {
                // todo:客户帐号串号重要监控处
                //User user1 = userDao.getByLoginNameAndType(customer1.getPhone(), customerId, new Integer[]{3});  // mark on 2019-7-30
                User user1 = null;
                // add on 2019-7-29 begin
                //User user2 = userDao.getByLoginNameAndTypeWithoutCustomerAccountProfile(customer1.getPhone(), new Integer[]{3});      // mark on 2020-12-3
                User user2 = systemService.getByLoginNameAndTypeWithoutCustomerAccountProfile(customer1.getPhone(), new Integer[]{3});  // add on 2020-12-3
                if (customerId != null) {
                    CustomerAccountProfile customerAccountProfileForMS = new CustomerAccountProfile();
                    customerAccountProfileForMS.setCustomer(new Customer(customerId));
                    List<CustomerAccountProfile> customerAccountProfileList = msCustomerAccountProfileService.findByCustomerIdAndOrderApproveFlag(customerAccountProfileForMS);
                    if (customerAccountProfileList != null && !customerAccountProfileList.isEmpty() && user2 != null) {
                        long lCount = customerAccountProfileList.stream().filter(customerAccountProfile -> customerAccountProfile.getId().longValue() == user2.getCustomerAccountProfile().getId().longValue()).count();
                        if (lCount > 0) {
                            user1 = user2;
                        } else {
                            user1 = null;
                        }
                    } else {
                        user1 = null;
                    }
                }
                // add on 2019-7-29 end

                if (user1 != null
                        && (!user1.getMobile().equalsIgnoreCase(customer.getPhone())
                        || !user1.getName().equalsIgnoreCase(customer.getMaster())
                )) {
                    user1.setPhone(customer.getPhone());
                    user1.setMobile(customer.getPhone());
                    //user1.setName(customer.getMaster());//comment by ryan 2018/04/26 修改时不同步帐号名称
                    user1.setUpdateDate(customer.getUpdateDate());
                    user1.setUpdateBy(customer.getUpdateBy());
                    if (user1.getName().equalsIgnoreCase(customer.getSales().getName())) {
                        LogUtils.saveLog(
                                "帐号修改串号，与业务员姓名相同",
                                "CustomerService.save(customer)",
                                String.format("before - primary name:%s update - name:%s", customer1.getMaster(), user1.getName()), null,
                                null,
                                Log.TYPE_EXCEPTION
                        );
                    } else {
                        //检查与当前帐号
                        User currUser = UserUtils.getUser();
                        if (currUser != null && user1.getName().equalsIgnoreCase(currUser.getName())) {
                            LogUtils.saveLog(
                                    "帐号修改串号,与当前帐号姓名相同",
                                    "CustomerService.save(customer)",
                                    String.format("before - primary name:%s update - name:%s", customer1.getMaster(), user1.getName()), null,
                                    null,
                                    Log.TYPE_EXCEPTION
                            );
                        }
                    }
                    userDao.updateUserInfo(user1);
                    msUserService.refreshUserCacheByUserId(user1.getId());//user微服务
                    // 角色:客户主帐号
                    List<Role> roleList = Lists.newArrayList();
                    //vip厂商主帐号使用角色7，普通厂商主帐号使用角色4
                    Role r = systemService.getRoleById(4l);
                    roleList.add(r);
                    user1.setRoleList(roleList);
                    //先删除旧角色
                    userDao.deleteUserRole(user1);
                    userDao.insertUserRole(user1);

                }
            }
        }

        //product,缓存 list
       /* List<Long> products = Lists.newArrayList();
        if (StringUtils.isNoneBlank(customer.getProductIds())) {
            products = Arrays.stream(customer.getProductIds().split(","))
                    .map(t -> Long.valueOf(t))
                    .collect(Collectors.toList());
            customerProductDao.assignProducts(customerId, products);
            //调用微服务 2019-9-27
            MSErrorCode batchInsertMSError = msCustomerProductService.batchInsert(customerId,products);
            if(batchInsertMSError.getCode()>0){
                throw new RuntimeException("保存客户产品失败.失败原因:"+batchInsertMSError.getMsg());
            }
        }*/

        List<Long> products = Lists.newArrayList();
        if (StringUtils.isNoneBlank(customer.getProductIds())) {
            products = Arrays.stream(customer.getProductIds().split(","))
                    .map(t -> Long.valueOf(t))
                    .collect(Collectors.toList());
            //customerProductDao.assignProducts(customerId, products);
            CustomerProduct customerProduct = new CustomerProduct();
            customerProduct.setCustomer(customer);
            customerProduct.setCreateBy(customer.getCreateBy());
            customerProduct.setUpdateBy(customer.getUpdateBy());
            customerProduct.preInsert();
            if(!isNew){
                List<Long> relationProductIds = Lists.newArrayList();
                relationProductIds.addAll(products);
                relationProductIds.removeAll(productIds);
                if(relationProductIds!=null && relationProductIds.size()>0){
                    //需要添加的客户产品
                    //customerProductDao.batchInsert(customerProduct,relationProductIds);
                    MSErrorCode newBatchInsertErrorCode = msCustomerProductService.newBatchInsert(customerProduct,relationProductIds);
                    if(newBatchInsertErrorCode.getCode()>0){
                        throw new RuntimeException("保存客户产品失败.失败原因:" + newBatchInsertErrorCode.getMsg());
                    }
                }
                relationProductIds.clear();
                relationProductIds.addAll(productIds);
                relationProductIds.removeAll(products);
                if(relationProductIds !=null && relationProductIds.size()>0){
                    //需要删除的客户产品
                    for(Long item:relationProductIds){
                        customerProduct.setProduct(new Product(item));
                        //customerProductDao.deleteCustomerProduct(customerProduct);
                    }
                    MSErrorCode batchDeleteErrorCode = msCustomerProductService.batchDelete(customerProduct,relationProductIds);
                    if(batchDeleteErrorCode.getCode()>0){
                        throw new RuntimeException("删除客户产品失败.失败原因:" + batchDeleteErrorCode.getMsg());
                    }
                }
            }else{
                //customerProductDao.batchInsert(customerProduct,products);
                MSErrorCode newBatchInsertErrorCode = msCustomerProductService.newBatchInsert(customerProduct,products);
                if(newBatchInsertErrorCode.getCode()>0){
                    throw new RuntimeException("保存客户产品失败.失败原因:" + newBatchInsertErrorCode.getMsg());
                }
            }
        }


        //修改客户
        if (!isNew) {
            //删除产品价格  调用微服务前代码
            /*if (productList != null && productList.size() > 0) {
                Set<Long> productListId = productList.stream().map(t -> t.getId()).collect(Collectors.toSet());
                //使用参考价格
                if (customer.getUseDefaultPrice() > 0) {
                    //清除原产品价格
                    dao.deletePricesByCustomerAndProducts(customerId, Lists.newArrayList(productListId));
                } else {
                    //删除不在支持的产品价格
                    Set<Long> noset = Sets.difference(productListId, Sets.newHashSet(products));
                    if (noset != null && noset.size() > 0) {
                        dao.deletePricesByCustomerAndProducts(customerId, Lists.newArrayList(noset));
                    }
                }
            }*/
            //调用微服务 2019-9-27
            if (productIds != null && productIds.size() > 0) {
                Set<Long> productListId = productIds.stream().collect(Collectors.toSet());
                //使用参考价格
                if (customer.getUseDefaultPrice() > 0) {
                    //清除原产品价格
                    //dao.deletePricesByCustomerAndProducts(customerId, Lists.newArrayList(productListId));  //mark on 2020-2-11
                    // 调用微服务 add on 2019-10-28
                     /*
                    // mark on 2020-8-14 begin
                    MSErrorCode msErrorCustomerPriceCode = msCustomerPriceService.deletePricesByCustomerAndProducts(customerId,Lists.newArrayList(productListId));
                    if(msErrorCustomerPriceCode.getCode()>0){
                        throw new RuntimeException("删除产品价格失败.失败原因:"+msErrorCustomerPriceCode.getMsg());
                    }
                    // mark on 2020-8-14 end
                    */
                    // end
                } else {
                    //删除不在支持的产品价格
                    Set<Long> noset = Sets.difference(productListId, Sets.newHashSet(products));
                    if (noset != null && noset.size() > 0) {
                        //dao.deletePricesByCustomerAndProducts(customerId, Lists.newArrayList(noset));  //mark on 2020-2-11
                        // 调用微服务 add on 2019-10-28
                        MSErrorCode msErrorCustomerPriceCode = msCustomerPriceService.deletePricesByCustomerAndProducts(customerId,Lists.newArrayList(noset));
                        if(msErrorCustomerPriceCode.getCode()>0){
                            throw new RuntimeException("删除产品价格失败.失败原因:"+msErrorCustomerPriceCode.getMsg());
                        }
                        // end
                    }
                }
            }
            try {

//                double score= Double.parseDouble(customer.getId().toString());
//                redisUtils.zRemRangeByScore(RedisConstant.RedisDBType.REDIS_MD_DB,RedisConstant.MD_CUSTOMER_ALL,score,score);
//                redisUtils.zAdd(RedisConstant.RedisDBType.REDIS_MD_DB,RedisConstant.MD_CUSTOMER_ALL, customer, customer.getId(), 0);

                //检查信用额度
                if (!oldCredit.equals(customer.getFinance().getCredit())) {
                    CustomerCurrency customerCurrency = new CustomerCurrency();
                    customerCurrency.setId(SeqUtils.NextIDValue(SeqUtils.TableName.CustomerCurrency));
                    customerCurrency.setCustomer(customer);
                    customerCurrency.setCurrencyType(CustomerCurrency.CURRENCY_TYPE_NONE);
                    customerCurrency.setCurrencyNo(customer.getCode());
                    customerCurrency.setBeforeBalance(oldCredit);
                    customerCurrency.setBalance(customer.getFinance().getCredit());
                    customerCurrency.setAmount(customer.getFinance().getCredit() - oldCredit);
                    customerCurrency.setPaymentType(CustomerCurrency.PAYMENT_TYPE_TRANSFER_ACCOUNT);
                    customerCurrency.setActionType(CustomerCurrency.ACTION_TYPE_CREDIT);
                    customerCurrency.setRemarks("保存客户时信用额度调整");
                    customerCurrency.preInsert();
                    customerCurrency.setQuarter(DateUtils.getYear() + String.valueOf(DateUtils.getSeason(new Date())));
                    customerCurrencyDao.insert(customerCurrency);
                }
            } catch (Exception e) {
                throw new RuntimeException("保存客户信息失败:" + e.getMessage());
            }
        }

        //if (customer.getUseDefaultPrice() > 0 ) {  //mark on 2020-8-14
        if (customer.getUseDefaultPrice() > 0 && isNew && customer.getCustomizePriceFlag().equals(0)) {  //add on 2020-8-15
            //套用参考价格
            List<ProductPrice> allPrices = productPriceService.findGroupList(products, null, customer.getUseDefaultPrice(), null, null);
           /* Map<Product, List<ProductPrice>> productPriceMap = allPrices.stream().collect(Collectors.groupingBy(ProductPrice::getProduct));
            if(productPriceMap !=null && productPriceMap.size()>0){
                int i = 0;
                boolean isError=true;
                StringBuffer sb = new StringBuffer("");
                for(Map.Entry<Product, List<ProductPrice>> entry : productPriceMap.entrySet()){
                   if(entry.getValue()!=null && entry.getValue().size()>0){
                       List<CustomerPrice> list = Lists.newArrayList();
                       entry.getValue().forEach(p -> {
                           CustomerPrice price = new CustomerPrice();
                           price.setServiceType(p.getServiceType());
                           price.setProduct(p.getProduct());
                           price.setPrice(p.getCustomerStandardPrice());
                           price.setDiscountPrice(p.getCustomerDiscountPrice());
                           price.setCustomer(customer);
                           price.setCreateBy(customer.getCreateBy());
                           price.setCreateDate(customer.getCreateDate());
                           if (!isNew) {
                               //修改时需要审核
                               price.setDelFlag(LongIDDataEntity.DEL_FLAG_AUDIT);
                           }
                           price.preInsert();
                           dao.insertPrice(price);
                           list.add(price);
                       });
                       //调用微服务 add on 2019-10-31
                       if(list!=null && list.size()>0){
                           MSErrorCode msErrorCodeBatchInsert = msCustomerPriceService.batchInsert(list); //根据产品分组保存客户产品价格
                           if(msErrorCodeBatchInsert.getCode()>0){
                               i=i+1;
                               isError = false;
                               Map<Long,String> productMap = MDUtils.getAllProductNames();
                               if(i==1){
                                   String productName = productMap.get(list.get(0).getProduct().getId());
                                   sb.append("产品:" + productName);
                               }else{
                                   String productName = productMap.get(list.get(0).getProduct().getId());
                                   sb.append("," + productName);
                               }
                           }
                       }
                       // end
                   }
                }
                if(!isError){
                    throw new RuntimeException(sb.toString()+"保存默认价格失败,请到服务价格添加服务价格");
                }
            }*/
            List<CustomerPrice> list = Lists.newArrayList();
            allPrices.forEach(p -> {
                CustomerPrice price = new CustomerPrice();
                price.setServiceType(p.getServiceType());
                price.setProduct(p.getProduct());
                price.setPrice(p.getCustomerStandardPrice());
                price.setDiscountPrice(p.getCustomerDiscountPrice());
                price.setCustomer(customer);
                price.setCreateBy(customer.getCreateBy());
                price.setCreateDate(customer.getCreateDate());
                if (!isNew) {
                    //修改时需要审核
                    price.setDelFlag(LongIDDataEntity.DEL_FLAG_AUDIT);
                }
                price.preInsert();
                //dao.insertPrice(price);  //mark on 2020-2-11
                list.add(price);
            });
            //调用微服务
            if(list !=null && list.size()>0){
                MSErrorCode msErrorCodeBatchInsert = msCustomerPriceService.batchInsert(list);
                if(msErrorCodeBatchInsert.getCode()>0){
                    throw new RuntimeException("微服务保存默认价格失败.失败原因" + msErrorCodeBatchInsert.getMsg()+",请联系管理员");
                }
            }
        }
        //更新redis
        //updateCustomerCache(customer);  // mark on 2020-2-11
    }

    @Transactional(readOnly = false)
    public void saveSalesInfo(Customer customer) {
        Customer cacheCustomer = getFromCache(customer.getId());
        cacheCustomer.setSales(customer.getSales());
        cacheCustomer.setUpdateBy(customer.getUpdateBy());
        //super.save(cacheCustomer);  // mark on 2020-2-11 web端去md_customer

        // 调用customer微服务
        MSErrorCode msErrorCode = msCustomerService.updateSales(customer);
        if (msErrorCode.getCode() != 0) {
            throw new RuntimeException("保存客户信息到微服务失败!失败原因: " + msErrorCode.getMsg());
        }

        //从字典中获取节点信息
        // 改为在微服务发消息队列 add on 2019-12-24
        /*String strQuarterCount = MSDictUtils.getDictSingleValue("quarterCount","0");
        Integer iQuarterCount = StringUtils.toInteger(strQuarterCount);
        if (iQuarterCount != null) {
            List<String> quarterList = getQuarters(iQuarterCount);
            if (quarterList != null && !quarterList.isEmpty()) {
                quarterList.stream().forEach(r -> {
                    // 生成mq
                    MQCustomerSalesMessage.CustomerSalesMessage.Builder customerSalesMessage = MQCustomerSalesMessage.CustomerSalesMessage.newBuilder();
                    customerSalesMessage.setSyncType(MQCustomerSalesMessage.SyncType.UPDATE);
                    customerSalesMessage.setCustomerId(customer.getId());
                    customerSalesMessage.setSalesId(customer.getSales().getId());
                    customerSalesMessage.setSubFlag(GlobalMappingSalesSubFlagEnum.SALES.getValue());
                    customerSalesMessage.setQuarter(r);

                    customerChangeSalesSender.send(customerSalesMessage.build()); // 5*1000 为5秒,3为3次
                });
            }
        }*/
        //更新redis
        //updateCustomerCache(cacheCustomer);  //mark on 2020-2-11
    }

    @Transactional(readOnly = false)
    public void saveMerchandiserInfo(Customer customer) {
        //  修改跟单员信息
        Customer cacheCustomer = getFromCache(customer.getId());
        cacheCustomer.setMerchandiser(customer.getMerchandiser());
        cacheCustomer.setUpdateBy(customer.getUpdateBy());
        //super.save(cacheCustomer);  //mark on 2020-2-11 web端去除md_customer

        // 调用customer微服务
        msCustomerService.updateMerchandiser(customer);
        //从字典中获取节点信息
        // 改为 在微服务调用 add on 2019-12-24
        /*String strQuarterCount = MSDictUtils.getDictSingleValue("quarterCount","0");
        Integer iQuarterCount = StringUtils.toInteger(strQuarterCount);
        if (iQuarterCount != null) {
            List<String> quarterList = getQuarters(iQuarterCount);
            if (quarterList != null && !quarterList.isEmpty()) {
                quarterList.stream().forEach(r -> {
                    // 生成mq
                    MQCustomerSalesMessage.CustomerSalesMessage.Builder customerSalesMessage = MQCustomerSalesMessage.CustomerSalesMessage.newBuilder();
                    customerSalesMessage.setSyncType(MQCustomerSalesMessage.SyncType.UPDATE);
                    customerSalesMessage.setCustomerId(customer.getId());
                    customerSalesMessage.setSalesId(customer.getMerchandiser().getId());
                    customerSalesMessage.setSubFlag(GlobalMappingSalesSubFlagEnum.MERCHANDISER.getValue()); //1-为业务员，2-为跟单员
                    customerSalesMessage.setQuarter(r);
                    customerChangeSalesSender.send(customerSalesMessage.build()); // 5*1000 为5秒,3为3次
                });
            }
        }*/
        //更新redis
        //updateCustomerCache(cacheCustomer);  //mark on 2020-2-11
    }

    @Transactional(readOnly = false)
    public void deleteById(long id) {
        // add on 2019-7-2 begin
        Customer customer = new Customer(id);
        MSErrorCode msErrorCode = msCustomerService.delete(customer);
        if ( msErrorCode.getCode() >0 ) {
            throw new RuntimeException("调用微服务删除客户信息失败!");
        }
        // add on 2019-7-2 end

        //customerDao.deleteById(id);   // mark on 2020-2-11 web端去customer
        customerFinanceDao.deleteById(id);
        /*
        //mark on 2020-2-11 web端customer begin
        try {
            redisUtils.zRemRangeByScore(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_CUSTOMER_ALL, id, id);
//            redisUtils.zRemRangeByScore("all:customer",id,id);
        } catch (Exception ex) {
            log.error("remove cache error:customer - {}", id, ex);
        }
        //mark on 2020-2-11 web端customer end
         */
    }

    public List<CustomerProduct> getListByCustomer(long customerId) {
        CustomerProduct cp = new CustomerProduct();
        Customer c = new Customer();
        c.setId(customerId);
        cp.setCustomer(c);
        //mark on 2019-9-28
        //return customerProductDao.getByCustomer(cp);
        //调用微服务 2019-9-28
        return msCustomerProductService.getByCustomer(customerId);
        //return Lists.newArrayList();
    }

    public CustomerAccountProfile getCustomerAccountProfile(Long id) {
//        CustomerAccountProfile customerAccountProfile = profileDao.get(id);  //mark on 2019-7-27
        CustomerAccountProfile customerAccountProfile = msCustomerAccountProfileService.getById(id); // add on 2019-7-27 改成微服务调用
        // add on 2019-6-29 begin
        // 调用微服务获取customer信息
        if (customerAccountProfile != null
            && customerAccountProfile.getCustomer() != null
            && customerAccountProfile.getCustomer().getId() != null) {
            Customer customer = msCustomerService.get(customerAccountProfile.getCustomer().getId());
            if (customer != null) {
                customerAccountProfile.setCustomer(customer);
            }
        }
        // add on 2019-6-29 end
        User sales = MSUserUtils.get(customerAccountProfile.getCustomer().getSales().getId());
        if (sales != null) {
            customerAccountProfile.getCustomer().getSales().setName(sales.getName());
            customerAccountProfile.getCustomer().getSales().setMobile(sales.getMobile());
            customerAccountProfile.getCustomer().getSales().setQq(sales.getQq());
        }
        return customerAccountProfile;
    }

    public CustomerAccountProfile getCustomerAccountProfileByUserId(Long userId) {
        //return profileDao.getByUserId(userId);  // mark on 2019-6-29
        // add on 2019-6-29 begin
        // 改成调用微服务
//        CustomerAccountProfile customerAccountProfile = profileDao.getByUserId(userId);
//        if (customerAccountProfile != null
//            && customerAccountProfile.getCustomer() != null
//            && customerAccountProfile.getCustomer().getId() != null) {
//            Customer customer = msCustomerService.get(customerAccountProfile.getCustomer().getId());
//            if (customer != null) {
//                customerAccountProfile.getCustomer().setName(customer.getName());
//                customerAccountProfile.getCustomer().setSales(customer.getSales());
//            }
//        }
        //return customerAccountProfile;
        // add on 2019-6-29 end

        // add on 2019-7-27 begin
        // 改成调用微服务
        CustomerAccountProfile customerAccountProfileForMS = null;
        User user = UserUtils.getAcount(userId);
        if (user != null
                && user.getCustomerAccountProfile() != null
                && user.getCustomerAccountProfile().getId() != null) {
            customerAccountProfileForMS = msCustomerAccountProfileService.getById(user.getCustomerAccountProfile().getId());
            if (customerAccountProfileForMS != null
                    && customerAccountProfileForMS.getCustomer() != null
                    && customerAccountProfileForMS.getCustomer().getId() != null) {
                Customer customer = msCustomerService.get(customerAccountProfileForMS.getCustomer().getId());
                if (customer != null) {
                    customerAccountProfileForMS.getCustomer().setName(customer.getName());
                    customerAccountProfileForMS.getCustomer().setSales(customer.getSales());
                    User userForMS = msUserService.get(customer.getSales().getId());
                    if (userForMS != null) {
                        customerAccountProfileForMS.getCustomer().getSales().setName(userForMS.getName());
                        customerAccountProfileForMS.getCustomer().getSales().setMobile(userForMS.getMobile());
                        customerAccountProfileForMS.getCustomer().getSales().setQq(userForMS.getQq());
                    }
                }
            }
        }
        return customerAccountProfileForMS;
        // add on 2019-7-27 end
    }

    /**
     * 读取所有线下单客户ID
     */
    public List<Long> findIdListByOfflineOrderFlagFromCacheForSD(){
        return msCustomerService.findIdListByOfflineOrderFlagFromCacheForSD();
    }

    public List<Customer> findOfflineCustomersForSD(){
        List<Long> ids = msCustomerService.findIdListByOfflineOrderFlagFromCacheForSD();
        if(CollectionUtils.isEmpty(ids)){
            return Lists.newArrayList();
        }
        List<Customer> cusotmers = msCustomerService.findIdAndNameListByIds(ids);
        return cusotmers;
    }

    //region 缓存

    /**
     * 从缓存读取客户信息
     * 只包含基本信息
     *
     * @param id
     * @return
     */
    public Customer getFromCache(long id) {
        // add on 2020-2-11
        Customer customerFromCache = msCustomerService.getFromCache(id);
        if (customerFromCache != null && customerFromCache.getSales() != null && customerFromCache.getSales().getId() != null) {
            User sales = MSUserUtils.get(customerFromCache.getSales().getId());
            if (sales != null) {
                customerFromCache.getSales().setName(sales.getName());
                customerFromCache.getSales().setMobile(sales.getMobile());
                customerFromCache.getSales().setQq(sales.getQq());
            }
            CustomerFinance customerFinance = getFinance(id);
            if (customerFinance != null) {
                customerFromCache.setFinance(customerFinance);
            }
        }
        // 读取跟单员信息
        if(customerFromCache!=null && customerFromCache.getMerchandiser()!=null && customerFromCache.getMerchandiser().getId()!=null && customerFromCache.getMerchandiser().getId()>0){
            User merchandiser = MSUserUtils.get(customerFromCache.getMerchandiser().getId());
            customerFromCache.getMerchandiser().setName(merchandiser.getName());
            customerFromCache.getMerchandiser().setMobile(merchandiser.getMobile());
            customerFromCache.getMerchandiser().setQq(merchandiser.getQq());
        }
        // 为了防止调用的地方报错。
        if (customerFromCache == null) {
            customerFromCache = new Customer(id);
        }

        return  customerFromCache;
        /*
        // mark on 2020-2-11 begin
        log.warn("1:{}",GsonUtils.toGsonString(customerFromCache));

        Customer customer = null;
        if (redisUtils.exists(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_CUSTOMER_ALL)) {
            customer = (Customer) redisUtils.zRangeOneByScore(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_CUSTOMER_ALL, id, id, Customer.class);
            if (customer == null) {
                customer = get(id);
//                if (customer != null) {  // mark on 2019-7-22
                if (customer != null && customer.getId() != null) { // add on 2019-7-22
                    redisUtils.zSetEX(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_CUSTOMER_ALL, customer, id, 0l);
                }
            }
            log.warn("2:{}",GsonUtils.toGsonString(customerFromCache));
            return customer;
        }
        //未装载客户列表
        loadAllCustomer();
        customer = (Customer) redisUtils.zRangeOneByScore(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_CUSTOMER_ALL, id, id, Customer.class);
        return customer;
        // mark on 2020-2-11 end
        */
    }

    public Customer getCustomerByIdFromCache(Long id) {
        Customer customerFromCache = msCustomerService.getCustomerByIdFromCache(id);
        // 为了防止调用的地方报错。
        if (customerFromCache == null) {
            customerFromCache = new Customer(id);
        }
        return  customerFromCache;
    }


    /**
     * 从缓存读取客户信息
     * 按需读取，requiredTags = null，只读取客户基本信息
     */
    public Customer getFromCacheAsRequired(long id,Integer requiredTags) {
        // add on 2020-2-11
        Customer customer = msCustomerService.getFromCache(id);
        if(customer == null){
            customer = new Customer(id);
            return customer;
        }
        if(requiredTags == null || requiredTags<=0){
            return customer;
        }
        if(CustomerRequiredTagEnum.FINANCE.hasTag(requiredTags)){
            CustomerFinance customerFinance = getFinance(id);
            if (customerFinance != null) {
                customer.setFinance(customerFinance);
            }
        }
        if(CustomerRequiredTagEnum.SALE.hasTag(requiredTags)){
            Long saleId = Optional.ofNullable(customer.getSales()).map(t->t.getId()).orElse(0L);
            if (saleId > 0 ) {
                User sales = MSUserUtils.get(saleId);
                if (sales != null) {
                    customer.getSales().setName(sales.getName());
                    customer.getSales().setMobile(sales.getMobile());
                    customer.getSales().setQq(sales.getQq());
                }
            }
        }
        return customer;
    }

    /*
    //mark on 2020-2-11 begin
    public Map<Long, Customer> getCustomerMap(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Maps.newHashMap();
        }
        if (!redisUtils.exists(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_CUSTOMER_ALL)) {
            loadAllCustomer();
        }
        List<Customer> list = redisUtils.getObjFromZSetByIds(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_CUSTOMER_ALL, ids, Customer.class);
        Map<Long, Customer> map = list.stream().collect(Collectors.toMap(LongIDBaseEntity::getId, i -> i));
        return map;
    }
    //mark on 2020-2-11 end
     */

    /**
     * 更新客户缓存,同时清除客户-价格，客户-产品
     * 存在：替换
     * 不存在：添加
     */
    /*
    // mark on 2020-2-11 begin
    public boolean updateCustomerCache(Customer customer) {
        if (customer == null || customer.getId() == null || customer.getId() <= 0) {
            return false;
        }
        if (redisUtils.exists(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_CUSTOMER_ALL)) {
            final Long cid = customer.getId();
            try {
                return (Boolean) redisUtils.redisTemplate.execute(new RedisCallback<Object>() {
                    @Override
                    public Object doInRedis(RedisConnection connection)
                            throws DataAccessException {
                        Boolean result = true;
                        try {
                            connection.select(RedisConstant.RedisDBType.REDIS_MD_DB.ordinal());
                            GsonRedisSerializer gsonRedisSerializer = redisUtils.gsonRedisSerializer;
                            byte[] bkey = RedisConstant.MD_CUSTOMER_ALL.getBytes(StandardCharsets.UTF_8);
                            //customer:all
                            connection.zRemRangeByScore(bkey, cid, cid);
                            //connection.zRemRange(bkey, cid, cid);
                            connection.zAdd(bkey, customer.getId(), gsonRedisSerializer.serialize(customer));
                            //prices 去掉缓存价格微服务处理 add on 2019-12-6
                            //connection.del(String.format(RedisConstant.MD_CUSTOMER_PRICE, cid).getBytes(StandardCharsets.UTF_8));
                            //products
                             //去掉客户产品缓存
                            //connection.del(String.format(RedisConstant.MD_PRODUCT_CUSTOMER, cid).getBytes(StandardCharsets.UTF_8));
                        } catch (Exception e) {
                            log.error("CustomerService.updateCustomerCache", e);
                            return false;
                        }
                        return false;
                    }
                });

            } catch (Exception e) {
                try {
                    return (Boolean) redisUtils.redisTemplate.execute(new RedisCallback<Object>() {
                        @Override
                        public Object doInRedis(RedisConnection connection)
                                throws DataAccessException {
                            Boolean result = true;
                            try {
                                connection.select(RedisConstant.RedisDBType.REDIS_MD_DB.ordinal());
                                GsonRedisSerializer gsonRedisSerializer = redisUtils.gsonRedisSerializer;
                                byte[] bkey = RedisConstant.MD_CUSTOMER_ALL.getBytes(StandardCharsets.UTF_8);
                                //customer:all
//                                connection.zRemRange(bkey, cid, cid);
                                connection.zRemRangeByScore(bkey, cid, cid);
                                connection.zAdd(bkey, customer.getId(), gsonRedisSerializer.serialize(customer));
                                //prices //去掉缓存价格微服务处理 add on 2019-12-6
                                //connection.del(String.format(RedisConstant.MD_CUSTOMER_PRICE, cid).getBytes(StandardCharsets.UTF_8));
                                //products
                                // 去掉客户产品缓存 add on 2019-1-9
                                //connection.del(String.format(RedisConstant.MD_PRODUCT_CUSTOMER, cid).getBytes(StandardCharsets.UTF_8));
                            } catch (Exception e) {
                                log.error("CustomerService.updateCustomerCache", e);
                                return false;
                            }
                            return true;
                        }
                    });
                } catch (Exception e1) {
                    log.error("CustomerService.updateCustomerCache", e1);
                    return false;
                }
            }
        } else {
            return loadAllCustomer();//从数据库重新装载
        }
    }
    // mark on 2020-2-11 end
     */

    /**
     * 从数据库装载所有通过审核的客户
     *
     * @return
     */
    /*
    // mark on 2020-2-11 begin
    public boolean loadAllCustomer() {
        try {
            //切换为微服务 List<Customer> customers = findAllList(new Customer());
            List<Customer> customers = findAll();
            redisUtils.redisTemplate.executePipelined(new RedisCallback<Object>() {    // enable Redis Pipeline
                @Override
                public Object doInRedis(RedisConnection connection) throws DataAccessException {
                    connection.select(RedisConstant.RedisDBType.REDIS_MD_DB.ordinal());
                    connection.del(RedisConstant.MD_CUSTOMER_ALL.getBytes(StandardCharsets.UTF_8));
                    Customer customer;
                    for (int i = 0, length = customers.size(); i < length; i++) {
                        customer = customers.get(i);
                        connection.zAdd(RedisConstant.MD_CUSTOMER_ALL.getBytes(StandardCharsets.UTF_8), customer.getId(), customerAdapter.toJson(customer).getBytes());
                    }
                    return null;
                }
            });
            return true;
        } catch (Exception e) {
            log.error("[CustomerService.loadAllCustomer]", e);
            return false;
        }
    }
    // mark on 2020-2-11 end
     */

    //endregion 缓存

    //endregion 客户

    //region 帐号管理

    public User getAccount(Long id) {
        // return profileDao.getUser(id);  // mark on 2019-6-29
        // add on 2019-6-29 begin
        // customer改为微服务调用
        /*
        User user = profileDao.getUser(id);
        if (user != null
            && user.getCustomerAccountProfile() != null
            && user.getCustomerAccountProfile().getCustomer() != null
            && user.getCustomerAccountProfile().getCustomer().getId() != null) {
            Customer customer = msCustomerService.get(user.getCustomerAccountProfile().getCustomer().getId());
            if (customer != null) {
                user.getCustomerAccountProfile().setCustomer(customer);
            }
        }
        return user;
        */
        // add on 2019-6-29 end
        //User user = userDao.get(id);   //mark on 2020-12-4
        User user = systemService.getUserFromDb(id);   //add on 2020-12-4
        if (user != null
                && user.getCustomerAccountProfile() != null
                && user.getCustomerAccountProfile().getId() != null) {
            CustomerAccountProfile customerAccountProfile = msCustomerAccountProfileService.getById(user.getCustomerAccountProfile().getId());
            if (customerAccountProfile != null
                && customerAccountProfile.getCustomer() != null
                && customerAccountProfile.getCustomer().getId() != null ) {
                Customer customer = msCustomerService.get(customerAccountProfile.getCustomer().getId());
                if (customer != null) {
                    customerAccountProfile.setCustomer(customer);
                }
                user.setCustomerAccountProfile(customerAccountProfile);
            }
        }
        return user;
    }

    @Transactional(readOnly = false)
    public void save(User user) {
        boolean isNew = user.getIsNewRecord();
        if (isNew) {
            CustomerAccountProfile profile = user.getCustomerAccountProfile();
            profile.preInsert();
            //profileDao.insert(profile);  //mark on 2020-1-11

            // add on 2019-7-27 begin
            // 为了获取profile的id
            MSErrorCode msErrorCode =  msCustomerAccountProfileService.save(profile, isNew);
            if (msErrorCode.getCode() >0) {
                throw new ServiceException("调用微服务保存客户账号信息失败!失败原因:" + msErrorCode.getMsg());
            }
            // add on 2019-7-27 end

            user.preInsert();
            user.setCompany(new Office(profile.getCustomer().getId()));//company
            userDao.insert(user);
            MSUserUtils.addUserToRedis(user);//user微服务
        } else {
            user.getCustomerAccountProfile().preUpdate();
            //profileDao.update(user.getCustomerAccountProfile());   //mark on 2020-1-11

            // add on 2019-7-27 begin
            MSErrorCode msErrorCode =  msCustomerAccountProfileService.save(user.getCustomerAccountProfile(), isNew);
            if (msErrorCode.getCode() >0) {
                throw new ServiceException("调用微服务保存客户账号信息失败!失败原因:" + msErrorCode.getMsg());
            }
            // add on 2019-7-27 end
            user.preUpdate();
            //帐号串号问题监控
            User currUser = UserUtils.getUser();
            if (currUser != null && currUser.getId().equals(user.getId())) {
                LogUtils.saveLog(
                        "帐号修改串了",
                        "CustomerService.save(User)",
                        String.format("login user - id:%s,name:%s update user - id:%s,name:%s", currUser.getId(), currUser.getName(), user.getId(), user.getName()), null,
                        null,
                        Log.TYPE_EXCEPTION
                );
            }
            userDao.updateNew(user);
            msUserService.refreshUserCacheByUserId(user.getId());//user微服务
            // 删除原角色
            userDao.deleteUserRole(user);
        }
        // 更新用户与角色关联
        if (user.getRoleList() != null && user.getRoleList().size() > 0) {
            userDao.insertUserRole(user);
        } else {
            throw new ServiceException(user.getLoginName() + "没有设置角色！");
        }
        //cache
        if (!isNew) {
            try {
                UserUtils.clearCache(user);
            } catch (Exception ex) {
                log.error("clear cache error : customer account - {}", user.getId(), ex);
            }
        }
    }

    @Deprecated   //此方法已停用  //mark on 2020-1-11
    public Page<User> find(Page<User> page, User user) {
        user.setPage(page);
        // 执行分页查询
        List<User> userList = null;   //profileDao.findList(user);
        // add on 2019-6-29 begin
        // 调用客户微服务
        Customer customer = null;
        if (user.getCustomerAccountProfile() != null
                && user.getCustomerAccountProfile().getCustomer() != null
                && user.getCustomerAccountProfile().getCustomer().getId() != null) {
            customer = msCustomerService.get(user.getCustomerAccountProfile().getCustomer().getId());
        }
        // add on 2019-6-29 end
        //切换为微服务
        Map<String, Dict> userTypeMap = MSDictUtils.getDictMap("sys_user_type");
        for (User item : userList) {
            if (item.getUserType() != null && item.getUserType() > 0) {
                Dict userTypeDict = userTypeMap.get(item.getUserType().toString());
                item.setUserTypeName(userTypeDict != null ? userTypeDict.getLabel() : "");
            }
            // add on 2019-6-26 begin
            if (customer != null) {
                item.getCustomerAccountProfile().setCustomer(customer);
            }
            // add on 2019-6-26 end
        }

        page.setList(userList);
        return page;
    }

    public Page<User> findWithOutCustomerAccountProfile(Page<User> page,User user) {
        user.setPage(page);
        // 此方法是find(Page<User> page, User user)的微服务版   // add on 2019-7-29
        // add on 2019-9-7 begin
        Long customerId = Optional.ofNullable(user.getCustomerAccountProfile()).map(CustomerAccountProfile::getCustomer).map(Customer::getId).orElse(null);
        User loginUser = UserUtils.getUser();
        Set<Long> customerIds = Sets.newHashSet();
        List<Customer> customerList = Lists.newArrayList();
        Map<Long, Customer> customerMap = Maps.newHashMap();

        if(loginUser.isCustomer()){  //客户
            Customer customer = loginUser.getCustomerAccountProfile().getCustomer();
            user.setCompany(new Office(customer.getId()));  // add on 2019-10-8
            customer = msCustomerService.get(customerId);
            if (customer != null) {
                customerMap.put(customer.getId(), customer);
            }
        } else if(loginUser.isSalesPerson()){  // 业务员
            customerList.addAll(msCustomerService.findListBySalesId(loginUser.getId().intValue()));
            List<Long> ids = Lists.newArrayList();
            if (customerList != null && !customerList.isEmpty()) {
                ids = customerList.stream().map(Customer::getId).distinct().collect(Collectors.toList());
                customerMap = customerList.stream().collect(Collectors.toMap(Customer::getId,Function.identity()));
            }
            customerIds.addAll(ids);

            if (customerId != null) {
                user.setCompany(new Office(customerId));
            }
        } else if (loginUser.isMerchandiser()) { //跟单员 // add on 2020-3-21 begin
            customerList.addAll(msCustomerService.findListByMerchandiserId(loginUser.getId()));
            List<Long> ids = Lists.newArrayList();
            if (customerList != null && !customerList.isEmpty()) {
                ids = customerList.stream().map(Customer::getId).distinct().collect(Collectors.toList());
                customerMap = customerList.stream().collect(Collectors.toMap(Customer::getId,Function.identity()));
            }
            customerIds.addAll(ids);

            if (customerId != null) {
                user.setCompany(new Office(customerId));
            }
            // add on 2020-3-21 end
        } else { // 管理员
           if (customerId != null) {
               user.setCompany(new Office(customerId));
               Customer customer = msCustomerService.get(customerId);
               if (customer != null) {
                   customerMap.put(customer.getId(), customer);
               }
           } else {
               customerList = msCustomerService.findAllCustomerList();
               if (customerList != null && !customerList.isEmpty()) {
                   customerMap = customerList.stream().collect(Collectors.toMap(Customer::getId,Function.identity()));
               }
           }
        }

        //List<User> userList = profileDao.findListWithOutCustomerAccountProfile(user);
        List<User> userList = userDao.findListWithOutCustomerAccountProfile(user);
        List<Long> userIds = userList.stream().map(User::getId).collect(Collectors.toList());
        List<Role> roles = Lists.newArrayList();
        if(!userIds.isEmpty()){
            roles = userDao.findListUserRoleName(userIds);
        }
        Map<Long,Role> roleMap = roles.stream().collect(Collectors.toMap(role -> role.getUser().getId(),Function.identity()));
        // 如果是业务员,要对数据进行筛选
        //if(loginUser.isSaleman()) { //mark on 2020-3-21
        if (loginUser.isSalesPerson() || loginUser.isMerchandiser()) { //add on 2020-3-21
            userList = userList.stream().filter(r->customerIds.contains(r.getCompany().getId())).collect(Collectors.toList());
        }
        // add on 2019-9-7 end

        //切换为微服务
        Map<String, Dict> userTypeMap = MSDictUtils.getDictMap("sys_user_type");

        if (customerList != null && !customerList.isEmpty()) {
            customerMap = customerList.stream().collect(Collectors.toMap(Customer::getId,Function.identity()));
        }
        for (User item : userList) {
            if (item.getUserType() != null && item.getUserType() > 0) {
                Dict userTypeDict = userTypeMap.get(item.getUserType().toString());
                item.setUserTypeName(userTypeDict != null ? userTypeDict.getLabel() : "");
            }
            // add on 2019-9-7 begin
            if (item != null && item.getCompany() != null && item.getCompany().getId() != null) {
                item.getCustomerAccountProfile().setCustomer(customerMap.get(item.getCompany().getId()));
                CustomerAccountProfile customerAccountProfile = msCustomerAccountProfileService.getById(item.getCustomerAccountProfile().getId());
                if (customerAccountProfile != null) {
                    item.getCustomerAccountProfile().setOrderApproveFlag(customerAccountProfile.getOrderApproveFlag());
                }
            }
            // add on 2019-9-7 end
            if(roleMap.get(item.getId()) != null){
                item.setRole(roleMap.get(item.getId()));
            }

        }

        page.setList(userList);
        return page;
    }

//    /**
//     * 返回客户所有帐号列表
//     *
//     * @return
//     */
//    public List<User> getAllAccountList() {
//        return userDao.findAllCustomerAccountList();
//    }

    /**
     * 返回客户所有帐号列表
     *
     * @param customerId
     * @return
     */
    public List<String> getAccountList(Long customerId) {
        String key = String.format(RedisConstant.SYS_CUSTOMER_USER, customerId);
        List<String> values = Lists.newArrayList();
        if (redisUtils.exists(RedisConstant.RedisDBType.REDIS_SYS_DB, key)) {
            //String id = new String("");
            @SuppressWarnings("unchecked")
            Map<String, byte[]> maps = redisUtils.hGetAll(RedisConstant.RedisDBType.REDIS_SYS_DB, key);
            if (maps != null && maps.size() > 0) {
                return maps.keySet().stream().filter(t -> !t.equalsIgnoreCase("leader")).collect(Collectors.toList());
                /*
                for (Map.Entry<String, byte[]> entry : maps.entrySet()) {
                    if(!entry.getKey().equalsIgnoreCase("leader")) {
                        id = (String) redisUtils.gsonRedisSerializer.deserialize(entry.getValue(), String.class);
                        values.add(id);
                    }
                }*/
            }
        } else {
            //from db
            //1.get
            List<User> users = userDao.findCustomerAccountIdList(customerId);
            Map<String, Object> fields = Maps.newHashMap();
            String masterids = users.stream()
                    .filter(t -> t.getUserType() == 3)
                    .map(t -> t.getId().toString())
                    .collect(Collectors.joining(","));
            if (StringUtils.isNoneBlank(masterids)) {
                fields.put("leader", masterids);//主帐号
            }
            users.stream().forEach(t -> {
                fields.put(t.getId().toString(), t.getId().toString());
                values.add(t.getId().toString());
            });
            //2.write to redis
            redisUtils.hmSetAll(RedisConstant.RedisDBType.REDIS_SYS_DB, key, fields, 0l);
        }
        return values;
    }

    /**
     * 获得客户主帐号id列表
     *
     * @param customerId
     * @return
     */
    public List<String> getAccountMasters(Long customerId) {
        String key = String.format(RedisConstant.SYS_CUSTOMER_USER, customerId);
        List<String> values = Lists.newArrayList();
        String masterids = new String("");
        if (redisUtils.exists(RedisConstant.RedisDBType.REDIS_SYS_DB, key)) {
            masterids = redisUtils.hGet(RedisConstant.RedisDBType.REDIS_SYS_DB, key, "leader", String.class);
            return StringUtils.isNoneBlank(masterids) ? Arrays.asList(masterids.split(",")) : Lists.newArrayList();
        } else {
            //from db
            //1.get leader,by user type
            List<User> users = userDao.findCustomerAccountIdList(customerId);
            Map<String, Object> fields = Maps.newHashMap();
            masterids = users.stream()
                    .filter(t -> t.getUserType() == 3)
                    .map(t -> t.getId().toString())
                    .collect(Collectors.joining(","));
            if (StringUtils.isNoneBlank(masterids)) {
                fields.put("leader", masterids);//主帐号
            }
            users.stream().forEach(t -> {
                fields.put(t.getId().toString(), t.getId().toString());
                values.add(t.getId().toString());
            });
            //2.write to redis
            redisUtils.hmSetAll(RedisConstant.RedisDBType.REDIS_SYS_DB, key, fields, 0l);
            return StringUtils.isNoneBlank(masterids) ? Arrays.asList(masterids.split(",")) : Lists.newArrayList();
        }
    }

    /**
     * 判断客户下时候还有其他主帐号   todo:2018/08/28 comment by ryan
     *
     * @param customerId 客户id
     * @param expectId   排除user id
     * @return public boolean hasOtherPrimaryAccount(Long customerId, Long expectId) {
    return profileDao.hasOtherPrimaryAccount(customerId, expectId) > 0;
    }
     */

    /**
     * 重置密码
     * 手机号后6位
     *
     * @param account
     */
    @Transactional(readOnly = false)
    public void resetPassword(User account) {
        //profileDao.resetPassword(account); //mark on 2020-1-11
        userDao.resetPassword(account);
    }

    //endregion 帐号管理

    //region 价格

//    /**
//     * 获得价格
//     * @param id
//     * @return
//     */
//    public CustomerPrice getPrice(Long id){
//        return dao.getPrice(id,0);
//    }

    /**
     * 获得价格
     *
     * @param id
     * @param delFlag 0:生效的价格 1：停用的价格 2：待审核价格 null:所有价格
     * @return
     */
    /*public CustomerPrice getPrice(Long id, Integer delFlag) {
        //return dao.getPrice(id, delFlag);  // mark on 2019-7-22
        // add on 2019-7-22 begin
        //mark on 2019-11-4
        *//*CustomerPrice customerPrice = dao.getPrice(id,delFlag);
        List<CustomerPrice> customerPriceList = Lists.newArrayList(customerPrice);
        customerPriceList = handlerCustomerPrice(customerPriceList,true);
        return customerPriceList.stream().findFirst().orElse(null);*//*
        // add on 2019-7-22 end
        return msCustomerPriceService.getPrice(id,delFlag);
    }*/

    /**
     * 获取单个客户价格new
     * @param id
     * @param delFlag 0:生效的价格 1：停用的价格 2：待审核价格 null:所有价格
     * @return
     */
    public CustomerPrice getPriceNew(Long id, Integer delFlag) {
        return msCustomerPriceService.getPriceNew(id,delFlag);
    }

    /**
     * 从数据库读取某客户的所有价格清单(后台)
     *
     * @param id
     * @param delFlag 0:启用的价格 1:停用的价格 2:待审核价格
     * @return
     */
    public List<CustomerPrice> getPrices(Long id, Integer delFlag) {
        //return dao.getPrices(id, delFlag);   //mark on 2019-7-20
        // add on 2019-7-20  begin
        // 改成微服务获取customer
        //List<CustomerPrice> customerPriceList = dao.getPrices(id, delFlag); //改为调用微服务 findPrices mark on // 2019-10-28
        //调用微服务 add on 2019-10-28
        //List<CustomerPrice> customerPriceList = msCustomerPriceService.findPrices(id,delFlag);
        // 调用微服务 update on 2020-06-04
        List<CustomerPrice> customerPriceList = msCustomerPriceService.findPricesNew(id,delFlag);
        // end
        //customerPriceList = handlerCustomerPrice(customerPriceList,false);
        // add on 2019-7-20  end
        //return customerPriceList != null && !customerPriceList.isEmpty()?customerPriceList.stream().sorted(Comparator.comparing(r->r.getProduct().getName())).collect(Collectors.toList()):null;
        // update on 2020-06-04
        return customerPriceList != null && !customerPriceList.isEmpty() ? customerPriceList : null;
    }

    /**
     * 按主键id停用单个价格
     *
     * @param id
     */
    @Transactional(readOnly = false)
    public void deletePrice(Long id) {
        List<Long> ids = Lists.newArrayList(id);
        User user = UserUtils.getUser();
        //CustomerPrice price = dao.getPrice(id, 0);  // mark on 2019-7-22
        //CustomerPrice price = getPrice(id, 0);        // add on 2019-7-22
        CustomerPrice price = getPriceNew(id, 0);   // update on 2020-06-04
        if (price == null) {
            throw new RuntimeException("停用价格不存在");
        }
        HashMap<String, Object> maps = new HashMap<String, Object>();
        maps.put("id", id);
        maps.put("delFlag", 1);
        maps.put("updateBy", user);
        maps.put("updateDate", new Date());
        //dao.updatePriceByMap(maps);  //mark on 2020-2-11
        //调用微服务 add on 2019-11-4
        //MSErrorCode msErrorCode = msCustomerPriceService.updatePriceByMap(maps);
        // 调用微服务 update on 2020-06-04
        MSErrorCode msErrorCode = msCustomerPriceService.updatePriceByMapNew(maps);

        if(msErrorCode.getCode()>0){
            throw new RuntimeException("停用价格失败.失败原因:" + msErrorCode.getMsg());
        }
        // end
        //cache 去掉缓存 add on 2019-12-6
        //Long cid = price.getCustomer().getId();
        //resetCustomerPricesCache(cid);
    }


    /**
     * 保存单个价格
     *
     * @param price
     */
    @Transactional(readOnly = false)
    public void savePrice(CustomerPrice price, boolean isNew) {
        //dao.updatePrice(price); //mark on 2020-2-11
        // 调用微服务 add no 2019-10-28
        MSErrorCode errorCode = msCustomerPriceService.updatePrice(price, isNew);
        if(errorCode.getCode()>0){
            throw new RuntimeException("保存客户价格失败.失败原因:" + errorCode.getMsg());
        }
        // end
        //cache
        //待审核价格缓存才更新 add on 2019-12-6
        //Long cid = price.getCustomer().getId();
        //resetCustomerPricesCache(cid);
    }

    /**
     * 分页查询
     * 先从数据库返回客户id,再根据id从缓存中读取，缓存不存在则再从数据库读取并更新至缓存
     * 保存在map属性中
     */
    public Page<CustomerPrice> findPage(Page<CustomerPrice> page, CustomerPrice entity) {

        HashMap<String, List<HashMap<String, Object>>> customerPriceListMap = new HashMap<>();
        List<HashMap<String, Object>> customerProductPriceList = Lists.newArrayList();
        HashMap<String, Object> customerProductPriceMap;
        List<HashMap<String, Object>> customerPriceMapList;
        HashMap<String, Object> customerPriceMap;
        List<CustomerPrice> customerPriceList;
        entity.setPage(page);
        //List<CustomerProduct> customerProductsAll = customerProductDao.getCustomerProductsByIds(entity);  // mark on 2019-7-24
        //List<CustomerProduct> customerProductsAll = getCustomerProductsByIds(entity);    // add on 2019-7-24  //mark on 2019-10-30 调用微服务
        // 调用微服务 add on 2019-10-30
        Page<CustomerProduct> customerProductPage = msCustomerProductService.findCustomerProductList(entity);
        List<CustomerProduct> customerProductsAll = customerProductPage.getList();
        page.setCount(customerProductPage.getCount());
        //end
        List<Long> customerIds = customerProductsAll.stream()
                .map(t -> t.getCustomer().getId())
                .distinct()
                .collect(Collectors.toList());
        //List<CustomerPrice> prices = dao.getPricesByCustomers(customerIds, entity.getProduct().getId(), null); // 改为调用微服务 findPricesByCustomers方法 mark on 2019-10-28
        //调用微服务 add on 2019-10-28
        //List<CustomerPrice> prices = msCustomerPriceService.findPricesByCustomers(customerIds,entity.getProduct().getId(),null);
        // 调用微服务优化返回数据 update on 2020-06-04
        List<CustomerPrice> prices = msCustomerPriceService.findPricesByCustomersNew(customerIds,entity.getProduct().getId(),null);

        // end
        //产品参考价格
        List<Long> productIds = customerProductsAll.stream()
                .map(t -> t.getProduct().getId())
                .distinct()
                .collect(Collectors.toList());
        List<ProductPrice> productPrices = productPriceService.findGroupList(productIds, null, null, null, entity.getCustomer().getId());
         //mark on 2019-10-11
        //List<ServiceType> serviceTypes = typeService.findAllList();

        //调用微服务获取服务类型,只返回Id和服务名称 start 2019-10-11
        //List<ServiceType> serviceTypes = typeService.findAllListIdsAndNames();
        // update on 2020-06-18 返回id,name,warrantyStatus
        List<ServiceType> serviceTypes = typeService.findListIdAndNameAndWarrantyStatus();
        //end
        serviceTypes = ServiceType.ServcieTypeOrdering.sortedCopy(serviceTypes);//服务类型

        Customer customer = null;
        Product product;
        ProductPrice productPrice;
        CustomerPrice price;

        for (Long customerId : customerIds) {
            final Long ci = customerId;
            customer = customerProductsAll.stream()
                    .filter(cpp -> Objects.equals(cpp.getCustomer().getId(), ci))
                    .findFirst().orElse(null).getCustomer();

            customerProductPriceMap = new HashMap<>();
            customerPriceMapList = Lists.newArrayList();

            customerProductPriceMap.put("customerId", customer.getId());
            customerProductPriceMap.put("customerCode", customer.getCode());
            customerProductPriceMap.put("customerName", customer.getName());

            List<CustomerProduct> customerProducts = customerProductsAll.stream()
                    .filter(t -> Objects.equals(t.getCustomer().getId(), ci))
                    .collect(Collectors.toList());

            for (CustomerProduct customerProduct : customerProducts) {
                product = customerProduct.getProduct();
                customerPriceMap = new HashMap<>();
                customerPriceList = Lists.newArrayList();
                final Long productId = product.getId();

                customerPriceMap.put("productId", product.getId());
                customerPriceMap.put("productName", product.getName());

                for (ServiceType serviceType : serviceTypes) {
                    final Long serviceTypeId = serviceType.getId();
                    //已有价格
                    price = prices.stream()
                            .filter(t -> Objects.equals(t.getProduct().getId(), productId)
                                    && Objects.equals(t.getServiceType().getId(), serviceTypeId)
                                    && Objects.equals(t.getCustomer().getId(), ci))
                            .findFirst().orElse(null);
                    if (price != null) { //维护
                        price.setFlag(0);
                        price.setServiceType(serviceType);
                        customerPriceList.add(price);
                        continue;
                    }
                    //参考价格
                    productPrice = productPrices.stream().filter(t -> Objects.equals(t.getProduct().getId(), productId)
                            && Objects.equals(t.getServiceType().getId(), serviceTypeId))
                            .findFirst().orElse(null);
                    if (productPrice != null) {
                        price = new CustomerPrice();
                        price.setServiceType(serviceType);
                        price.setReferPrice(productPrice.getCustomerStandardPrice());
                        price.setReferDiscountPrice(productPrice.getCustomerDiscountPrice());
                        price.setFlag(1);//有参考价格
                    } else {
                        price = new CustomerPrice();
                        price.setServiceType(serviceType);
                        price.setFlag(2);//无参考价格
                    }
                    customerPriceList.add(price);
                }
                customerPriceMap.put("customerPriceList", customerPriceList);
                customerPriceMapList.add(customerPriceMap);
            }

            customerProductPriceMap.put("customerPriceMapList", customerPriceMapList);
            customerProductPriceList.add(customerProductPriceMap);
        }
        customerPriceListMap.put("list", customerProductPriceList);
        page.setMap(customerPriceListMap);
        return page;
    }


    /**
     * 分页获得待审核价格(后台)
     *
     * @param page
     * @param entity
     * @return
     */
    public Page<CustomerPrice> findApprovePricePage(Page<CustomerPrice> page, CustomerPrice entity) {
        entity.setPage(page);
        //page.setList(dao.findApprovePriceList(entity));  //add on 2019-7-22
        // add on 2019-7-22 begin
        //List<CustomerPrice> customerPriceList = dao.findApprovePriceList(entity); //mark on 2019-11-4
        //customerPriceList = handlerCustomerPrice(customerPriceList,true); //mark on 2019-11-4
        //调用微服务 add on 2019-11-4
        Page<CustomerPrice> customerPricePage = msCustomerPriceService.findApprovePriceList(page,entity);
        List<CustomerPrice> customerPriceList = customerPricePage.getList();
        // end
        Function<CustomerPrice,String> customerCodeSorted = customerPrice -> customerPrice.getCustomer().getCode();
        Function<CustomerPrice,String> productNameSorted = customerPrice -> customerPrice.getProduct().getName();
        Function<CustomerPrice,String> serviceTypeSorted = customerPrice -> customerPrice.getServiceType().getCode();
        //log.warn("排序前：{}", GsonUtils.toGsonString(customerPriceList));
        List<CustomerPrice> sortedCustomerPriceList = customerPriceList != null && !customerPriceList.isEmpty()?customerPriceList.stream().sorted(Comparator.comparing(customerCodeSorted).thenComparing(productNameSorted).thenComparing(serviceTypeSorted)).collect(Collectors.toList()):null;
        //log.warn("排序后：{}", GsonUtils.toGsonString(sortedCustomerPriceList));
        page.setList(sortedCustomerPriceList);
        page.setCount(customerPricePage.getCount());
        // add on 2019-7-22 end
        return page;
    }

    /**
     * 审核价格（后台）
     *
     * @param ids
     * @param updateBy
     */
    @Transactional(readOnly = false)
    public void approvePrices(List<Long> ids, Long updateBy) {
        //dao.approvePrices(ids, updateBy);  //mark on 2020-2-11
        // 调用微服务 add on 2019-11-4
        MSErrorCode msErrorCode = msCustomerPriceService.approvePrices(ids,updateBy,new Date().getTime());
        if(msErrorCode.getCode()>0){
            throw new RuntimeException("审核价格失败.失败原因:" + msErrorCode.getMsg());
        }
        // end
        //cache  去掉缓存 add on 2018-12-6
        /*try {
            List<CustomerPrice> prices = dao.getPricesByPriceIds(ids); //mark on 2019-11-5
            prices = handlerCustomerPrice(prices,true); // 调用微服务处理客户信息 // 2019-7-22 //mark on 2019-11-5
            List<CustomerPrice> pricesFromMS = msCustomerPriceService.findPricesByPriceIds(ids);
            if (prices == null || prices.size() == 0) {
                return;
            }
            // 判断从web端和微服务数据库获取客户审核价格加载到缓存是否一致 add on 2019-12-2
            String strPrices = "";
            String strPricesFromMS = "";
            prices.stream().sorted(Comparator.comparing(CustomerPrice::getId)).collect(Collectors.toList());
            strPrices = GsonUtils.toGsonString(prices);
            if(pricesFromMS !=null && pricesFromMS.size()>0){
                pricesFromMS.stream().sorted(Comparator.comparing(CustomerPrice::getId)).collect(Collectors.toList());
                strPricesFromMS = GsonUtils.toGsonString(pricesFromMS);
            }
            if(strPrices.hashCode()!= strPricesFromMS.hashCode()){
                try {
                    log.error("客户价格审核加载到缓存读取数据web端与微服务端不一致:微服务端:" + strPricesFromMS + ",web端" + strPrices);
                }catch (Exception e){}
            }
            // end
            //addCustomerPricesCache(prices);
        } catch (Exception e) {
            log.error("[CustomerService.approvePrices] update cache", e);
        }*/
    }

    /**
     * 停用价格
     *
     * @param ids
     * @param updateBy
     */
    @Transactional(readOnly = false)
    public void stopPrices(List<Long> ids, Long updateBy) {
        //dao.stopPrices(ids, updateBy);  //mark on 2020-2-11
        //缓存，淘汰原则
        try {
            //List<CustomerPrice> prices = dao.getPricesByPriceIds(ids); // mark on 2019-11-5
            //prices = handlerCustomerPrice(prices,true);    // 调用微服务处理客户信息 // 2019-7-22  //mark on 2019-11-5
            // 调用微服务 add on 2019-11-5
            List<CustomerPrice> prices = msCustomerPriceService.findPricesByPriceIds(ids);
            //end
            if (prices == null || prices.size() == 0) {
                return;
            }

            List<Long> customerIds = prices.stream()
                    .collect(Collectors.groupingBy(CustomerPrice::getCustomer, Collectors.counting()))
                    .keySet().stream().map(t -> t.getId()).collect(Collectors.toList());
            // add on 2019-12-6
            //delCustomerPricesCache(customerIds);

        } catch (Exception e) {
            log.error("[ServicePointService.stopPrices] update cache", e);
        }
    }

    /**
     * 启用价格
     * 与参考价格比对，
     * 相同  ：不审核
     * 不相同：要审核
     *
     * @param p    价格
     * @param user
     */
    @Transactional(readOnly = false)
    public void startPrice(CustomerPrice p, User user) {
        if (p == null) {
            throw new RuntimeException("停用的价格不存在");
        }

        //标准价格
//        ProductPrice productPrice = productPriceService.getByProductIDAndServiceTypeId(p.getProduct().getId(),p.getServiceType().getId());
//        if(productPrice  ==null){
//            throw new RuntimeException("读取参考价格失败，请维护相应的参考价格");
//        }
//        if(Objects.equals(p.getPrice(),productPrice.getCustomerStandardPrice())
//                && Objects.equals(p.getDiscountPrice(),productPrice.getCustomerDiscountPrice())){
        //相同,不审核
        p.setDelFlag(0);
//        }else{
//            p.setDelFlag(2);//待审核
//        }

        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("id", p.getId());
        map.put("delFlag", p.getDelFlag());
        map.put("updateBy", user);
        map.put("updateDate", new Date());
        //dao.updatePriceByMap(map);   //mark on 2020-2-11
        //调用微服务 add on 2019-11-4
        //MSErrorCode msErrorCode = msCustomerPriceService.updatePriceByMap(map);
        // 调用微服务 update on 2020-06-04
        MSErrorCode msErrorCode = msCustomerPriceService.updatePriceByMapNew(map);
        if(msErrorCode.getCode()>0){
            throw new RuntimeException("启用价格失败.失败原因" + msErrorCode.getMsg());
        }
        //end
        //不需要审核,更新价格缓存
//        if(p.getDelFlag() == 0) {
        //CustomerPrice price = dao.getPrice(p.getId(), 0);  // mark on 2019-7-22
        // 去掉价格缓存 add on 2019-12-6
       /* CustomerPrice price = getPrice(p.getId(), 0);        //add on 2019-7-22
        if (price != null) {
            addCustomerPricesCache(Lists.newArrayList(p));
        }*/
        //resetCustomerPrices(p.getCustomer().getId());
//        }
    }

    /**
     * 保存客户某产品的所有安维价格（后台）
     * 修改价格：要审核
     * 新增价格：与参考价格对比，不同：要审核 相同：不审核
     *
     * @param customerPrices
     */
    @Transactional(readOnly = false)
    public void saveProductPrices(CustomerPrices customerPrices) {
        Customer customer = customerPrices.getCustomer();
        Product product = customerPrices.getProduct();
        User user = customerPrices.getCreateBy();
        Date date = customerPrices.getCreateDate();
        ProductPrice productPrice;
        List<ProductPrice> allPrices = productPriceService.findGroupList(Lists.newArrayList(product.getId()), null, null, null, customer.getId());
        List<CustomerPrice> list = Lists.newArrayList();
        for (CustomerPrice p : customerPrices.getPrices()) {
            // add on 2019-7-22 begin
            if (p == null || p.getServiceType() == null) {
                continue;
            }
            // add on 2019-7-22 end
            if (p.getServiceType().getWarrantyStatus().getValue().equalsIgnoreCase("IW")
                    && (p.getPrice() <= 0 || p.getDiscountPrice() <= 0)) {
                //保内，无论新增、修改价格不大于0的都不更新数据库
                continue;
            }
            if (p.getId() != null) {//待审核
                p.setProduct(product);
                p.setDelFlag(2);//待审核
                p.setUpdateBy(user);
                p.setUpdateDate(date);
                p.setCustomer(customer);
                p.setIsNewRecord(false);
                //dao.updatePrice(p);  //mark on 2020-2-11 web端去customer
                list.add(p); //add on 2019-10-30
            } else {
                //new
                productPrice = allPrices.stream()
                        .filter(t -> Objects.equals(t.getServiceType().getId(), p.getServiceType().getId()))
                        .findFirst()
                        .orElse(null);
                if (productPrice != null) {
                    p.setProduct(product);
                    p.setCustomer(customer);
                    p.setCreateBy(user);
                    p.setCreateDate(date);
                    if (!(Objects.equals(productPrice.getCustomerStandardPrice(), p.getPrice())
                            && Objects.equals(productPrice.getCustomerDiscountPrice(), p.getDiscountPrice()))) {
                        //待审核
                        p.setDelFlag(2);
                    }
//                    if (!Objects.equals(productPrice.getCustomerStandardPrice(), p.getPrice())
//                            || !Objects.equals(productPrice.getCustomerDiscountPrice(), p.getDiscountPrice())) {
//                        //待审核
//                        p.setDelFlag(2);
//                    }
                    p.setIsNewRecord(true);
                    //dao.insertPrice(p);  //add on 2020-2-11
                    list.add(p); //add on 2019-10-30
                }
            }
        }
        //调用微服务 add  on 2019-10-30
        //MSErrorCode  msErrorCode = msCustomerPriceService.insertOrUpdateBatch(list);
        // 调用微服务 update on 2020-06-04
        MSErrorCode  msErrorCode = msCustomerPriceService.insertOrUpdateBatchNew(list);
        if(msErrorCode.getCode()>0){
            throw new RuntimeException("保存客户价格失败.失败原因:" + msErrorCode.getMsg());
        }
        //end
        //cache，重载 去掉缓存 add on 2019-12-6
        // resetCustomerPricesCache(customer.getId());
    }

    //region 缓存

    /**
     * 读取某客户的价格清单
     * 先从缓存读取，缓存不存在从数据库读取，并更新缓存
     *
     * @param id 客户id
     * @return
     */
    public List<CustomerPrice> getPricesFromCache(Long id) {
        //调用微服务 add on 2019-11-6
        //List<CustomerPrice> list = msCustomerPriceService.getPricesFromCache(id);

        List<CustomerPrice> listNew = msCustomerPriceService.getPricesFromCacheNew(id);
        if (listNew != null && !listNew.isEmpty()) {
            List<Long> productIds = listNew.stream().map(r->r.getProduct().getId()).distinct().collect(Collectors.toList());
            List<Product> productList = Lists.newArrayList();
            if (productIds != null && !productIds.isEmpty()) {
                productList =  msProductService.findProductByIdListFromCache(productIds);
            }

            List<Long> serviceTypeIds = listNew.stream().map(r->r.getServiceType().getId()).distinct().collect(Collectors.toList());
            List<ServiceType> serviceTypeList = Lists.newArrayList();
            if (serviceTypeIds != null && !serviceTypeIds.isEmpty()) {
                serviceTypeList =  typeService.findAllListIdsAndNamesAndCodes();
            }

            Map<Long,Product> productMap = !ObjectUtils.isEmpty(productList)?productList.stream().collect(Collectors.toMap(r->r.getId(),r->r)):Maps.newHashMap();
            Map<Long,ServiceType> serviceTypeMap = !ObjectUtils.isEmpty(serviceTypeList)?serviceTypeList.stream().collect(Collectors.toMap(r->r.getId(),r->r)):Maps.newHashMap();
            listNew.stream().forEach(customerPrice -> {
                Product product = productMap.get(customerPrice.getProduct().getId());
                if (product != null) {
                    customerPrice.setProduct(product);
                }
                ServiceType serviceType = serviceTypeMap.get(customerPrice.getServiceType().getId());
                if (serviceType != null) {
                    customerPrice.setServiceType(serviceType);
                }
            });
        }
        //log.warn("old:{}", list);
        //log.warn("new:{}", listNew);


       /* if(listMD!=null && listMD.size()>0){
            return listMD;
        }*/
        //end
        // add on 比较微服务与web从缓存中读取的数据是否一致
       /* String strListFromMS = "";
        if(listMD !=null && listMD.size()>0){
            //Function<CustomerPrice, Integer> productSort = customerPrice->customerPrice.getProduct().getSort();
            listMD = listMD.stream().sorted(Comparator.comparing(CustomerPrice::getId)).collect(Collectors.toList());
            strListFromMS = GsonUtils.toGsonString(listMD);
        }
        List<CustomerPrice> list = getPricesFromCache(id,0);
        String strList = "";
        if(list !=null && list.size()>0){
            //Function<CustomerPrice, Integer> productSort = customerPrice->customerPrice.getProduct().getSort();
            list = list.stream().sorted(Comparator.comparing(CustomerPrice::getId)).collect(Collectors.toList());
            strList = GsonUtils.toGsonString(list);
        }
        if(strListFromMS.hashCode()!=strList.hashCode()){
            try {
                Customer customer = getFromCache(id);
                String customerName = "";
                if(customer !=null){
                    customerName = customer.getName();
                }
                log.error("客户:" + customerName + "从缓存取价格:微服务取的客户价格与web取得客户价格不一致,微服务客户价格:" + strListFromMS+ "web端客户价格:" + strList);
            }catch (Exception e){}
        }*/
        // end
        return listNew;
    }

    /*
    //mark on 2020-2-12 begin
    private List<CustomerPrice> getPricesFromCache(Long id,int retryTimes) {
        if(retryTimes >= RETRY_TIMES){
            return Lists.newArrayList();
        }
        //String threadName = Thread.currentThread().getName();
        String key = String.format(RedisConstant.MD_CUSTOMER_PRICE, id);
        List<CustomerPrice> prices = redisUtils.lRange(RedisConstant.RedisDBType.REDIS_MD_DB, key, 0, -1, CustomerPrice.class);
        if (prices == null || prices.size() == 0) {
            String lockKey = "Lock:"+key;
            Boolean locked = redisUtils.getLock(lockKey,"1",30);
            if(locked) {
                //System.out.println(MessageFormat.format("{0} {1}-get from db", DateUtils.getDate("yyyy-MM-dd HH:mm:ss.SSS"),threadName));

                prices = dao.getPricesForCache(id);
                prices = handlerCustomerPrice(prices); // 调用微服务获取customer  // add 2019-7-20
                // add on 2019-8-19 begin
                // 先按product.sort排序，再serviceType.sort排序
                Function<CustomerPrice, Integer> productSort = customerPrice->customerPrice.getProduct().getSort();
                Function<CustomerPrice, Integer> serviceTypeSort = customerPrice -> customerPrice.getServiceType().getSort();
                prices = prices != null && !prices.isEmpty()?prices.stream().sorted(Comparator.comparing(productSort).thenComparing(serviceTypeSort)).collect(Collectors.toList()) : null;
                // add on 2019-8-19 end

                prices = getPricesForCacheMethod(id);
                if (prices != null && prices.size() > 0) {
                    redisUtils.lPushAll(RedisConstant.RedisDBType.REDIS_MD_DB, key, prices);
                    redisUtils.releaseLock(lockKey,"1");
                }else{
                    return Lists.newArrayList();
                }
            }else{
                retryTimes++;
                //System.out.println(MessageFormat.format("{0} {1}-retry:{2}", DateUtils.getDate("yyyy-MM-dd HH:mm:ss.SSS"),threadName,retryTimes));
                try {
                    TimeUnit.MILLISECONDS.sleep(2000 + RandomUtils.nextInt(0,1000));
                } catch (InterruptedException e) {}
                return getPricesFromCache(id,retryTimes);
            }
        }else{
            //System.out.println(MessageFormat.format("{0} {1}-get from cache", DateUtils.getDate("yyyy-MM-dd HH:mm:ss.SSS"),threadName));
        }
        return prices;
    }
    // add on 2020-2-12 end
    */

    /**
     * @param customerId
     * @return 返回结果集的key为“productId:serviceTypeId”
     */
    public Map<String, CustomerPrice> getCustomerPriceMap(Long customerId) {
        Map<String, CustomerPrice> priceMap = Maps.newHashMap();
        if (customerId != null) {
            String key = null;
            List<CustomerPrice> priceList = getPricesFromCache(customerId);
            if (priceList != null && priceList.size() > 0) {
                for (CustomerPrice price : priceList) {
                    key = String.format("%d:%d", price.getProduct().getId(), price.getServiceType().getId());
                    priceMap.put(key, price);
                }
            }
        }
        return priceMap;
    }

    /**
     * 重载客户价格到缓存
     *
     * @param customerId
     * @return
     */
    /*
    // mark on 2020-2-12 begin
    public Boolean resetCustomerPricesCache(Long customerId) {
        String key = String.format(RedisConstant.MD_CUSTOMER_PRICE, customerId);
        String lockKey = "Lock:"+key;
        Boolean locked = redisUtils.getLock(lockKey,"1",10);
        if(!locked) {
            return false;
        }
        try {
            redisUtils.remove(RedisConstant.RedisDBType.REDIS_MD_DB, key);

            //List<CustomerPrice> prices = dao.getPricesForCache(customerId);
            //prices = handlerCustomerPrice(prices);   // 调用微服务处理customer  //add on 2019-7-20

            List<CustomerPrice> prices = getPricesForCacheMethod(customerId);

            if (prices != null && prices.size() > 0) {
                redisUtils.lPushAll(RedisConstant.RedisDBType.REDIS_MD_DB, key, prices);
            }
        } catch (Exception e) {
            log.error("[CustomerService.deletePrice]remove customer price cid: {}", customerId.toString(), e);
            return false;
        }finally {
            redisUtils.releaseLock(lockKey,"1");
        }
        return true;
    }
    // mark on 2020-2-12 end
     */

    /**
     * 批量移除缓存中的客户下的所有服务价格
     *
     * @param customerIds 客户id列表
     * @return
     */
    /*
    // mark on 2020-2-12 begin
    public Boolean delCustomerPricesCache(List<Long> customerIds) {
        if (customerIds == null || customerIds.size() == 0) {
            return true;
        }

        return (Boolean) redisUtils.redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection)
                    throws DataAccessException {
                Boolean result = true;
                try {
                    connection.select(RedisConstant.RedisDBType.REDIS_MD_DB.ordinal());
                    GsonRedisSerializer gsonRedisSerializer = redisUtils.gsonRedisSerializer;
                    for (int i = 0, size = customerIds.size(); i < size; i++) {
                        Long id = customerIds.get(i);
                        byte[] bkey = String.format(RedisConstant.MD_CUSTOMER_PRICE, id).getBytes(StandardCharsets.UTF_8);
                        connection.del(bkey);
                    }
                    return result;
                } catch (Exception e) {
                    log.error("CustomerService.removeCustomerPrices", e);
                }
                return false;
            }
        });
    }
    // mark on 2020-2-12 end
    */

    /**
     * 批量添加客户服务价格到缓存
     *
     * @param prices 客户服务价格列表
     * @return
     */
    /*
    // add on 2020-2-12 begin
    public Boolean addCustomerPricesCache(List<CustomerPrice> prices) {
        if (prices == null || prices.size() == 0) {
            return true;
        }

        return (Boolean) redisUtils.redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection)
                    throws DataAccessException {
                Boolean result = true;
                try {
                    connection.select(RedisConstant.RedisDBType.REDIS_MD_DB.ordinal());
                    GsonRedisSerializer gsonRedisSerializer = redisUtils.gsonRedisSerializer;
                    for (int i = 0, size = prices.size(); i < size; i++) {
                        CustomerPrice price = prices.get(i);
                        byte[] bkey = String.format(RedisConstant.MD_CUSTOMER_PRICE, price.getCustomer().getId()).getBytes(StandardCharsets.UTF_8);
                        if (!connection.exists(bkey)) {
                            continue;
                        }
                        connection.lPush(bkey, gsonRedisSerializer.serialize(price));
                    }
                    return result;
                } catch (Exception e) {
                    log.error("CustomerService.addCustomerPricesCache", e);
                }
                return false;
            }
        });
    }
    // mark on 2020-2-12 end
    */
    //endregion 缓存


    //endregion 价格


    /**
     * 查询客户信息报表的数据
     *
     * @param customerId
     * @param paymentType
     * @return

    public List<CustomerBaseInfoRptEntity> getCustomerBaseInfoRptData(Long customerId, Integer paymentType) {

        List<CustomerBaseInfoRptEntity> list = null;  //customerDao.getCustomerBaseInfoList(customerId, paymentType); //mark on 2020-2-11 web端去cusotmer
        List<Product> productsList = null; //customerDao.getCustomerProducts(customerId, paymentType);  //mark on 2020-2-11 web端去cusotmer

        Map<Long, String> productsMap = Maps.newHashMap();
        for (Product item : productsList) {
            if (item.getCustomerId() != null && item.getName() != null) {
                productsMap.put(item.getCustomerId(), item.getName());
            }
        }

        Map<Long, Customer> allCustomerMap = CustomerUtils.getAllCustomerMap();
        for (CustomerBaseInfoRptEntity item : list) {
            Customer customer = allCustomerMap.get(item.getId());
            if (customer != null) {
                item.setPaymentType(customer.getFinance().getPaymentType());
                item.setSales(customer.getSales());
            }
            String products = productsMap.get(item.getId());
            if (products != null) {
                item.setProducts(products);
            }
        }

        return list;
    }*/

//    public List<CustomerBaseInfoRptEntity> getCustomerBaseInfoRptDataNew(Long customerId, Integer paymentType) {
//        List<Customer> customerList = msCustomerService.findAllByIdAndPaymentType(customerId, paymentType);  // 调用微服务
//        List<CustomerBaseInfoRptEntity> list = Lists.newArrayList();
//        //List<Product> productsList = Lists.newArrayList();
//        if (customerList != null && !customerList.isEmpty()) {
//            customerList.stream().forEach(customer -> {
//                CustomerBaseInfoRptEntity entity = new CustomerBaseInfoRptEntity();
//                entity.setId(customer.getId());
//                entity.setCode(customer.getCode());
//                entity.setName(customer.getName());
//                entity.setFullName(customer.getFullName());
//                entity.setContractDate(customer.getContractDate());
//                entity.setSales(customer.getSales());
//                entity.setPaymentType(customer.getPaymentType());
//                entity.setAddress(customer.getAddress());
//                entity.setPhone(customer.getPhone());
//                entity.setFax(customer.getFax());
//                entity.setMaster(customer.getMaster());
//                entity.setMobile(customer.getMobile());
//                entity.setEmail(customer.getEmail());
//                entity.setProjectOwner(customer.getProjectOwner());
//                entity.setProjectOwnerPhone(customer.getProjectOwnerPhone());
//                entity.setProjectOwnerQq(customer.getProjectOwnerQq());
//                entity.setServiceOwner(customer.getServiceOwner());
//                entity.setServiceOwnerPhone(customer.getServiceOwnerPhone());
//                entity.setServiceOwnerQq(customer.getServiceOwnerQq());
//                entity.setTechnologyOwner(customer.getTechnologyOwner());
//                entity.setTechnologyOwnerPhone(customer.getTechnologyOwnerPhone());
//                entity.setTechnologyOwnerQq(customer.getTechnologyOwnerQq());
//                entity.setFinanceOwner(customer.getFinanceOwner());
//                entity.setFinanceOwnerPhone(customer.getFinanceOwnerPhone());
//                entity.setFinanceOwnerQq(customer.getFinanceOwnerQq());
//
//                CustomerFinance customerFinance = getFinance(customer.getId());
//                if (customerFinance != null) {
//                    entity.setDeposit(customerFinance.getDeposit());
//                    entity.setTaxpayerCode(customerFinance.getTaxpayerCode());
//                    entity.setPublicName(customerFinance.getPublicName());
//                    entity.setPublicBank(customerFinance.getPublicBank());
//                    entity.setPublicBranch(customerFinance.getPublicBranch());
//                    entity.setPublicAccount(customerFinance.getPublicAccount());
//                    entity.setPrivateName(customerFinance.getPrivateName());
//                    entity.setPrivateBank(customerFinance.getPrivateBank());
//                    entity.setPrivateBranch(customerFinance.getPrivateBranch());
//                    entity.setPrivateAccount(customerFinance.getPrivateAccount());
//                }
//                list.add(entity);
//              /*  List<Product> products = customerDao.getCustomerProductsWithOutCustomer(customer.getId(), paymentType);
//                if (products != null && !products.isEmpty()) {
//                    productsList.addAll(products);
//                }*/
//            });
//        }
//
//        //List<Product> productsList = customerDao.getCustomerProductsWithOutCustomer(customerId, paymentType);
//        // add on 2019-8-17 begin
//        // product 微服务
//        /*if (productsList != null && !productsList.isEmpty()) {
//            productsList.stream().forEach(product -> {
//                Page<Product> productPage = new Page<>();
//                productPage.setPageSize(500);
//                Page<Product> page = msProductService.findList(productPage, product);
//                if (page != null && page.getList() != null && !page.getList().isEmpty()) {
//                    String strName = page.getList().stream().map(Product::getName).collect(Collectors.joining(","));
//                    //log.warn("product:{},name:{}", product.getName(), strName);
//                    product.setName(strName);
//                }
//            });
//        }*/
//        // add on 2019-8-17 end
//
//      /*  List<Product> filterProductList = Lists.newArrayList();
//        if (paymentType != null && paymentType >0) {
//            List<Long> customerIdList = msCustomerService.findCustomerIdByPaymentType(paymentType);  // 调用微服务
//            List<Long> productsCustomerIds = productsList == null && !productsList.isEmpty()? Lists.newArrayList() : productsList.stream().map(Product::getCustomerId).collect(Collectors.toList());
//            //  获取交集
//            if (productsCustomerIds != null && !productsCustomerIds.isEmpty()) {
//                productsCustomerIds.retainAll(customerIdList);
//            }
//            if (productsCustomerIds != null && !productsCustomerIds.isEmpty()) {
//                List<Product> products = productsList.stream().filter(product -> productsCustomerIds.contains(product.getCustomerId())).collect(Collectors.toList());
//                if (products != null && !products.isEmpty()) {
//                    filterProductList.addAll(products);
//                }
//            }
//        } else {
//            filterProductList.addAll(productsList);
//        }*/
//        List<MDProductDto> productDtoList = msCustomerProductService.getCustomerProducts(customerId,paymentType);
//        Map<Long, String> productsMap = Maps.newHashMap();
//        for (MDProductDto item : productDtoList) {
//            if (item.getCustomerId() != null && item.getName() != null) {
//                productsMap.put(item.getCustomerId(), item.getName());
//            }
//        }
//
//        //Map<Long, String> productsMap = filterProductList!=null && !filterProductList.isEmpty()?filterProductList.stream().filter(r->r.getCustomerId() != null && r.getName() != null).collect(Collectors.toMap(Product::getCustomerId,Product::getName)):null;
//
//        Map<Long, Customer> allCustomerMap = CustomerUtils.getAllCustomerMap();
//        for (CustomerBaseInfoRptEntity item : list) {
//            Customer customer = allCustomerMap.get(item.getId());
//            if (customer != null) {
//                item.setSales(customer.getSales());
//            }
//            String products = productsMap.get(item.getId());
//            if (products != null) {
//                item.setProducts(products);
//            }
//        }
//        return list;
//    }


    /**
     * 处理价格列表中的customer
     * @param prices
     * @return
     */
    /*
    // 此方法已无处调用，废弃
    // mark on 2020-2-12 begin
    private List<CustomerPrice> handlerCustomerPrice(List<CustomerPrice> prices,boolean isLoadServiceType) {

        List<CustomerPrice> finalPricesList = Lists.newArrayList();
        if (prices != null && !prices.isEmpty()) {
            String strIds = prices.stream().map(r->r.getCustomer().getId()+"").distinct().collect(Collectors.joining(","));
            List<Customer> customerList = msCustomerService.findBatchByIds(strIds);  // 调用微服务返回customer列表

            String strProductIds = prices.stream().map(r->r.getProduct().getId()+"").distinct().collect(Collectors.joining(","));
            List<Product> productList = Lists.newArrayList();                        // 调用Product微服务 // add on 2019-8-17
            Page<Product> productPage = new Page<>();
            productPage.setPageSize(5000);
            Product productEntity = new Product();
            productEntity.setProductIds(strProductIds);
            Page<Product> page = msProductService.findList(productPage,productEntity);
            if (page != null && page.getList() != null && !page.getList().isEmpty()) {
                productList = page.getList();
            }

            Map<Long, Customer> customerMap = null;
            if (customerList!= null && !customerList.isEmpty()) {
                customerMap = customerList.stream().collect(Collectors.toMap(Customer::getId, customer -> customer));
            }

            Map<Long, Product> productMap = null;
            if (productList != null && !productList.isEmpty()) {
                productMap = productList.stream().collect(Collectors.toMap(Product::getId, product->product));
            }

            //调用微服务获取服务类型 对象只返回 id,name,code,sort, warrantyStatus add on 2019-10-14
            Map<Long,ServiceType> serviceTypeMap = null;
            if(isLoadServiceType){
                List<ServiceType> serviceTypeList = typeService.findAllListFields();
                if(serviceTypeList!=null && !serviceTypeList.isEmpty()){
                    serviceTypeMap = serviceTypeList.stream().collect(Collectors.toMap(ServiceType::getId, Function.identity(), (oldServiceType, serviceType) -> serviceType));
                }
            }
            //end

            // end
            final Map<Long, Customer> finalCustomerMap = customerMap;
            final Map<Long, Product> finalProductMap = productMap;
            final Map<Long,ServiceType> finalServiceTypeMap = serviceTypeMap; //add on 2019-10-14
            if(isLoadServiceType){
                prices.stream().forEach(customerPrice -> {
                    //Customer customer = customerList!= null && !customerList.isEmpty()?customerList.stream().filter(r->r.getId().longValue() == customerPrice.getCustomer().getId().longValue()).findFirst().orElse(null):null;  //性能欠佳
                    Customer customer = finalCustomerMap==null? null:finalCustomerMap.get(customerPrice.getCustomer().getId());  // 从map中获取
                    Product product = finalProductMap==null? null:finalProductMap.get(customerPrice.getProduct().getId());
                    ServiceType serviceType = finalServiceTypeMap == null?null:finalServiceTypeMap.get(customerPrice.getServiceType().getId()); //add on 2019-10-14
                    // mark on 2019-10-14
                    if (product != null && customer != null && serviceType!=null) {
                        customerPrice.getCustomer().setCode(customer.getCode());
                        customerPrice.getCustomer().setName(customer.getName());
                        customerPrice.getProduct().setName(product.getName());
                        customerPrice.getProduct().setSort(product.getSort());
                        customerPrice.setServiceType(serviceType);
                        finalPricesList.add(customerPrice);
                    }
                });
            }else {
                prices.stream().forEach(customerPrice -> {
                    //Customer customer = customerList!= null && !customerList.isEmpty()?customerList.stream().filter(r->r.getId().longValue() == customerPrice.getCustomer().getId().longValue()).findFirst().orElse(null):null;  //性能欠佳
                    Customer customer = finalCustomerMap==null? null:finalCustomerMap.get(customerPrice.getCustomer().getId());  // 从map中获取
                    Product product = finalProductMap==null? null:finalProductMap.get(customerPrice.getProduct().getId());
                    if (product != null && customer != null) {
                        customerPrice.getCustomer().setCode(customer.getCode());
                        customerPrice.getCustomer().setName(customer.getName());
                        customerPrice.getProduct().setName(product.getName());
                        customerPrice.getProduct().setSort(product.getSort());
                        finalPricesList.add(customerPrice);
                    }
                });
            }
        }
        return finalPricesList;
    }
    // mark on 2020-2-12 end
     */

    /**
     * 将dao.getPricesForCache方法进行整理
     * @param customerId
     * @return
     */
    /*
    //mark on 2020-2-12 begin
    // 此方法没有地方调用  代码废弃
    private List<CustomerPrice> getPricesForCacheMethod(Long customerId) {
        return null;

        List<CustomerPrice> prices = null; //dao.getPricesForCache(customerId); // mark on 2019-11-5 改为调用 findPricesForCache方法
        prices = handlerCustomerPrice(prices,true);      // 调用微服务获取customer  // add 2019-7-20  //mark on 2019-11-5 改为调用 findPricesForCache方法
        // 调用微服务 add on 2019-11-5
        List<CustomerPrice> pricesFromMS = msCustomerPriceService.findPricesForCache(customerId);
        // end
        //log.warn("排序前的数据:{}", prices);

        // add on 2019-12-2 判读微服务与web取得数据是否一致,后面会删除
        String strPrices = "";
        String strPricesFromMS = "";
        if(prices!=null && prices.size()>0){
            prices = prices.stream().sorted(Comparator.comparing(CustomerPrice::getId)).collect(Collectors.toList());
            strPrices = GsonUtils.toGsonString(prices);
        }
        if(pricesFromMS !=null && pricesFromMS.size()>0){
            pricesFromMS = pricesFromMS.stream().sorted(Comparator.comparing(CustomerPrice::getId)).collect(Collectors.toList());
            strPricesFromMS = GsonUtils.toGsonString(pricesFromMS);
        }
        if(strPrices.hashCode() != strPricesFromMS.hashCode()){
            try {
                Customer customer = getFromCache(customerId);
                String customerName = "";
                if(customer !=null){
                    customerName = customer.getName();
                }
                 log.error( "客户:" +customerName +"从数据库读取客户价格加载到缓存:微服务取的客户价格与web取得客户价格不一致,微服务端客户价格:" + strPricesFromMS+",web端客户价格:" + strPrices);
            }catch (Exception e){}
        }
        // end
        // add on 2019-8-19 begin
        // 先按product.sort排序，再serviceType.sort排序
        Function<CustomerPrice, Integer> productSort = customerPrice->customerPrice.getProduct().getSort();
        Function<CustomerPrice, Integer> serviceTypeSort = customerPrice -> customerPrice.getServiceType().getSort();
        prices = prices != null && !prices.isEmpty()?prices.stream().sorted(Comparator.comparing(productSort).thenComparing(serviceTypeSort)).collect(Collectors.toList()) : null;
        //log.warn("排序后的数据:{}", prices);
        // add on 2019-8-19 end

        return prices;
    }
    */

    /*
    // 此段代码没有地方调用，废弃 //2020-2-12
    private List<CustomerProduct> getCustomerProductsByIds(CustomerPrice customerPrice) {
        List<CustomerProduct> customerProducts = Lists.newArrayList();
        List<Customer> customerList = Lists.newArrayList();
        if (customerPrice.getCustomer() != null && customerPrice.getCustomer().getId() != null) {
            Customer customer = msCustomerService.getByIdToCustomer(customerPrice.getCustomer().getId());
            if (customer != null) {
                customerList.add(customer);
            }
        } else if (customerPrice.getCustomer() != null
                && customerPrice.getCustomer().getSales() != null
                && customerPrice.getCustomer().getSales().getId() != null
                && customerPrice.getCustomer().getSales().getId() >0 ) {
            List<Customer> customerListFromMS = msCustomerService.findListBySalesId(customerPrice.getCustomer().getSales().getId().intValue());
            if (customerListFromMS != null && !customerListFromMS.isEmpty()) {
                customerList.addAll(customerListFromMS);
            }
        } else {
            List<Customer> customerListFromMS = msCustomerService.findAllSpecifiedColumn();
            if (customerListFromMS != null && !customerListFromMS.isEmpty()) {
                customerList.addAll(customerListFromMS);
            }
        }

        if (customerList != null && !customerList.isEmpty()) {
            customerList.stream().forEach(customer -> {
                CustomerPrice customerPriceEntity = new CustomerPrice();
                BeanUtils.copyProperties(customerPrice,customerPriceEntity);
                customerPriceEntity.setCustomer(customer);
                //List<CustomerProduct> customerProductsAll = customerProductDao.getCustomerProductsByIdsWithOutCustomer(customerPriceEntity);  // mark on 2019-8-19

                // add on 2019-8-19 begin
                // 获取当前产品列表
                List<Product> productList = Lists.newArrayList();
                if (customerPriceEntity.getProduct() != null && customerPriceEntity.getProduct().getId() != null) {
                    // 选择一个产品
                    Product product = msProductService.getById(customerPriceEntity.getProduct().getId());
                    productList.add(product);
                } else if (customerPriceEntity.getProductCategory() != null &&
                        customerPriceEntity.getProductCategory().getId() != null) {
                    // 按产品类别获取
                    List<Product> products = msProductService.findListByProductCategoryId(customerPriceEntity.getProductCategory().getId());  // 调用微服务获取
                    if (products != null && !products.isEmpty()) {
                        productList.addAll(products);

                        List<Long> productIds = productList.stream().map(r->r.getId()).collect(Collectors.toList());  // 获取产品id列表
                        customerPriceEntity.setProductIds(productIds);
                    }
                } else {
                    // 获取所有产品
                    Page<Product> productPage = new Page<>();
                    productPage.setPageSize(1000);
                    Page<Product> page = msProductService.findList(productPage, new Product());
                    if (page != null && page.getList() != null && !page.getList().isEmpty()) {
                        productList.addAll(page.getList());
                    }
                }
                 // mark on 2019-9-27
                //List<CustomerProduct> customerProductsAll = customerProductDao.getCustomerProductsByIdsWithoutCustomerAndProduct(customerPriceEntity);

                 //调用微服务 2019-9-27 start
                CustomerProduct msCustomerProduct = new CustomerProduct();
                if(customerPriceEntity.getCustomer()!=null){
                    msCustomerProduct.setCustomer(customerPriceEntity.getCustomer());
                }
                if(customerPriceEntity.getProduct()!=null){
                    msCustomerProduct.setProduct(customerPriceEntity.getProduct());
                }

                msCustomerProduct.setPage(new Page<>(customerPriceEntity.getPage().getPageNo(),customerPriceEntity.getPage().getPageSize()));
                Page<CustomerProduct> pageCustomer = msCustomerProductService.findCustomerProductsByIdsWithoutCustomerAndProduct(msCustomerProduct,customerPriceEntity.getProductIds());
                List<CustomerProduct> customerProductsAll = pageCustomer.getList();
                customerPriceEntity.getPage().setCount(pageCustomer.getCount());
                 // end 2019 - 9 -27
                // add on 2019-8-19 end
                if (customerProductsAll != null && !customerProductsAll.isEmpty()) {
                    // add on 2019-8-19 begin
                    List<Long> productIds = customerProductsAll.stream().map(r->r.getProduct().getId()).collect(Collectors.toList());  // 获取产品id列表
                    // 取交集
                    Map<Long,Product> productMap = productList.stream().filter(r->productIds.contains(r.getId())).collect(Collectors.toMap(Product::getId, Function.identity()));
                    // add on 2019-8-19 end
                    customerProductsAll.stream().forEach(customerProduct -> {
                        customerProduct.getCustomer().setCode(customer.getCode());
                        customerProduct.getCustomer().setName(customer.getName());

                        Product product = productMap.get(customerProduct.getProduct().getId());
                        if (product != null) {
                            customerProduct.getProduct().setName(product.getName());
                            customerProduct.getProduct().setSort(product.getSort());
                            customerProduct.getProduct().setCategory(product.getCategory());

                            customerProducts.add(customerProduct);
                        }
                    });
                    //customerProducts.addAll(customerProductsAll);
                }
            });
        }

        // 排序，先按客户名称，然后按产品排序好排序
        if (customerProducts != null && !customerProducts.isEmpty()) {
            Function<CustomerProduct, String> customerNameSorted = customerProduct -> customerProduct.getCustomer().getName();
            Function<CustomerProduct, Integer> productSortSorted = customerProduct -> customerProduct.getProduct().getSort();
            return customerProducts.stream().sorted(Comparator.comparing(customerNameSorted).thenComparing(productSortSorted)).collect(Collectors.toList());
        }

        return customerProducts;
    }
    */


    // region 数据分片
    /**
     * 获取分片名字列表
     * @return
     */
    /*
    // 以下代码自从发送业务员消息取消后，可以废弃 //mark on 2020-2-12  begin
    public List<String> getQuarters(int quarterCount) {
        if (quarterCount <=0) {
            return null;
        }
        Date startDate = OrderUtils.getGoLiveDate();
        int iYear = DateUtils.getYear(startDate);
        int iSeason = DateUtils.getSeason(startDate);
        Date dtSeasonStartDate = getSeasonStartDate(iYear,iSeason);
        Date endDate = DateUtils.addMonth(dtSeasonStartDate, 3*quarterCount);
        endDate = DateUtils.addDays(endDate,-1);
        List<String> quarterList = getQuarters(startDate, endDate);
        log.warn("开始日期:{},结束日期:{},分片:{}", DateUtils.formatDate(dtSeasonStartDate,"yyyy-MM-dd"), DateUtils.formatDate(endDate,"yyyy-MM-dd") ,quarterList);
        return quarterList;
    }

    @SuppressWarnings("all")
    public List<String> getQuarters(Date startDate, Date endDate){
        List<String> quarters = Lists.newArrayList();
        if(startDate == null){
            return quarters;
        }

        startDate = DateUtils.parseDate(DateUtils.formatDate(startDate,"yyyy-MM-dd"));
        endDate = DateUtils.parseDate(DateUtils.formatDate(endDate,"yyyy-MM-dd"));
        int startMonth,endMonth;
        while(true){
            startMonth = startDate.getMonth();
            endMonth = endDate.getMonth();
            if(startDate.getTime()>endDate.getTime()
                    && startMonth != endMonth
                    && (startMonth/3 + 1) != (endMonth /3 + 1)
                    ){
                break;
            }
            quarters.add(QuarterUtils.getSeasonQuarter(startDate));
            startDate = DateUtils.addMonth(startDate,3);
        }
        return quarters;
    }

    private Date getSeasonStartDate(int year, int season) {
        String combineDate = "";
        switch(season) {
            case 1:
                combineDate =  year + "-01-01";
                break;
            case 2:
                combineDate = year + "-04-01";
                break;
            case 3:
                combineDate = year + "-07-01";
                break;
            case 4:
                combineDate = year + "-10-01";
                break;
        }
        return DateUtils.parseDate(combineDate);
    }
    //mark on 2020-2-12  begin
    */
    // endregion  数据分片

    public List<Customer> findAllCustomerListFromDB(){
        List<Customer> customers = msCustomerService.findAllCustomerListFromDB();
        if (customers.isEmpty()) {
            return Lists.newArrayList();
        }
        return customers;
    }

    public List<Customer> findCustomersWithSales(Customer customer) {
        MDCustomer mdCustomer = new MDCustomer();
        if (customer.getSales().getId() != null) {
            mdCustomer.setSalesId(customer.getSales().getId());
        }
        if (customer.getMerchandiser().getId() != null) {
            mdCustomer.setMerchandiserId(customer.getMerchandiser().getId());
        }
        return msCustomerService.findCustomersWithSales(mdCustomer);
    }

}
