/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.wolfking.jeesite.modules.md.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kkl.kklplus.entity.b2bcenter.md.B2BProductMapping;
import com.kkl.kklplus.entity.common.NameValuePair;
import com.kkl.kklplus.entity.md.GlobalMappingSalesSubFlagEnum;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.config.redis.RedisTuple;
import com.wolfking.jeesite.common.mapper.JsonMapper;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.wolfking.jeesite.common.utils.SpringContextHolder;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.ServicePrice;
import com.wolfking.jeesite.modules.md.service.CustomerService;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.UserSubService;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providermd.service.MSCustomerService;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import com.wolfking.jeesite.ms.utils.MSUserUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.data.redis.connection.RedisZSetCommands;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 字典工具类
 *
 * @author ThinkGem
 * @version 2013-5-29
 */
@Slf4j
public class CustomerUtils {

    private static RedisUtils redisUtils = SpringContextHolder.getBean(RedisUtils.class);
    //	private static CustomerDao customerDao = SpringContextHolder.getBean(CustomerDao.class);
    private static CustomerService customerService = SpringContextHolder.getBean(CustomerService.class);

    private static int RETRY_TIMES = 5;

    private static MSCustomerService msCustomerService = SpringContextHolder.getBean(MSCustomerService.class);
    private static UserSubService userSubService = SpringContextHolder.getBean(UserSubService.class);

    /**
     * 所有的客户
     *
     * @return
     */
    public static List<Customer> getCustomerList() {
        // return getCustomerList(0);
        return getCustomerListFromMS();
    }

    /**
     * 所有的客户
     *
     * @return
     */
    public static List<Customer> getCustomerListFromMS() {
        return msCustomerService.findAllCustomerList();
    }

    /**
     * 所有的客户
     *
     * @return
     */
    public static List<Customer> getCustomerList(int retryTimes) {
        List<Customer> list = customerService.findAll();  // add on 2020-2-11
        return list!=null && !list.isEmpty() ?list.stream().sorted(Comparator.comparing(Customer::getName)).collect(Collectors.toList()):Lists.newArrayList();
    }



    /**
     * 返回所有客户
     * 以HashMap存储客户信息，key为客户的id
     *
     * @return
     */
    public static Map<Long, Customer> getAllCustomerMap() {
        List<Customer> list = customerService.findAll(); //add on 2020-2-13
        if(list == null || list.isEmpty()){
            return Maps.newHashMap();
        }
        return list.stream().collect(Collectors.toMap(Customer::getId, item -> item));
    }

    public static Map<Long, String> findAllCustomerMap(List<Long> customerIds) {
        // 调用微服务
        List<NameValuePair<Long, String>> list = customerService.findBatchListByIds(customerIds);
        if(list == null || list.isEmpty()){
            return Maps.newHashMap();
        }
        return list.stream().collect(Collectors.toMap(NameValuePair :: getName, NameValuePair :: getValue));
    }

    /**
     * 返回所有客户基本信息
     * 不包含finance
     * 以HashMap存储客户信息，key为客户的id
     */
    public static Map<Long, Customer> getAllCustomerBasicMap() {
        List<Customer> list = customerService.findAllSpecifiedColumnList();
        if(list == null || list.isEmpty()){
            return Maps.newHashMap();
        }
        return list.stream().collect(Collectors.toMap(Customer::getId, item -> item));
    }
    /**
     * 读取客户基本信息
     */
    public static Customer getCustomer(Long id) {
        Customer customer = null;
        if (id != null) {
            customer = customerService.getFromCache(id);
        }
        return customer;
    }

    /**
     * 登录用户所负责的客户
     *
     * @return
     */
    public static List<Customer> getMyCustomerList() {
        return getMyCustomerListFromMS();
    }

    /**
     * 登录用户所负责的客户
     *
     * @return
     */
    public static List<Customer> getMyCustomerListFromMS() {
        // 从微服务中获取
        //log.warn("从微服务中获取登录用户所负责的用户");
        User user = UserUtils.getUser();
        List<Customer> customerList = Lists.newArrayList();
        if(user.isCustomer()){
            Customer customer = UserUtils.getUser().getCustomerAccountProfile().getCustomer();
            customerList.add(customer);
        } else if(user.isSalesPerson()){  // 业务员
            customerList.addAll(msCustomerService.findListBySalesId(user.getId().intValue()));
            List<Customer> offlineCustomers = customerService.findOfflineCustomersForSD();
            customerList.addAll(offlineCustomers);
            if (customerList != null && !customerList.isEmpty()) {
                customerList = customerList.stream().sorted(Comparator.comparing(Customer::getName)).collect(Collectors.toList());
            }
        } else if (user.isMerchandiser()) { //跟单员
            customerList.addAll(msCustomerService.findListByMerchandiserId(user.getId()));
            List<Customer> offlineCustomers = customerService.findOfflineCustomersForSD();
            customerList.addAll(offlineCustomers);
            if (customerList != null && !customerList.isEmpty()) {
                customerList = customerList.stream().sorted(Comparator.comparing(Customer::getName)).collect(Collectors.toList());
            }
        } else if (user.isSalesManager()) {  //业务主管
            //1. 先获取客户列表列表
            List<Long> customerIdList = userSubService.findCustomerIdListByUserId(user.getId(), user.getUserType());
            //2.去掉重复的客户id
            List<Long> uniqueCustomerIdList = Optional.ofNullable(customerIdList).orElse(Collections.emptyList()).stream().distinct().collect(Collectors.toList());
            if (!uniqueCustomerIdList.isEmpty()) {
                customerList = customerService.findIdAndNameListByIds(uniqueCustomerIdList);
            }
        } else {
            customerList.addAll(msCustomerService.findAllCustomerList());
        }
        return customerList;
    }

    /**
     * 登录用户所负责的VIP客户
     *
     * @return
     */
    public static List<Customer> getMyVipCustomerList() {
        return getMyVipCustomerListFromMS();
    }
    /**
     * 登录用户所负责的VIP客户
     *
     * @return
     */
    public static List<Customer> getMyVipCustomerListFromMS() {
        // 从微服务中获取
        //log.warn("从微服务中获取登录用户所负责的用户");
        User user = UserUtils.getUser();
        List<Customer> customerList = Lists.newArrayList();
        if(user.isCustomer()){
            Customer customer = UserUtils.getUser().getCustomerAccountProfile().getCustomer();
            customerList.add(customer);
        } else if(user.isSalesPerson()){  // 业务员
            customerList.addAll(msCustomerService.findVipListBySalesId(user.getId().intValue()));
            if (customerList != null && !customerList.isEmpty()) {
                customerList = customerList.stream().sorted(Comparator.comparing(Customer::getName)).collect(Collectors.toList());
            }
        } else if (user.isMerchandiser()) { //跟单员
            customerList.addAll(msCustomerService.findVipListByMerchandiserId(user.getId()));
            if (customerList != null && !customerList.isEmpty()) {
                customerList = customerList.stream().sorted(Comparator.comparing(Customer::getName)).collect(Collectors.toList());
            }
        } else if (user.isSalesManager()) {
            //1. 先获取客户列表列表
            List<Long> customerIdList = userSubService.findCustomerIdListByUserId(user.getId(), user.getUserType());
            //2.去掉重复的客户id
            List<Long> uniqueCustomerIdList = Optional.ofNullable(customerIdList).orElse(Collections.emptyList()).stream().distinct().collect(Collectors.toList());
            if (!uniqueCustomerIdList.isEmpty()) {
                customerList = msCustomerService.findVipListByCustomerIdsFromCacheForRPT(uniqueCustomerIdList);
            }
        }
        else {
            customerList.addAll(msCustomerService.findAllVipCustomerList());
        }
        return customerList;
    }

    /**
     * 登录用户所负责的非VIP客户
     *
     * @return
     */
    public static List<Customer> getMyNotVipCustomerList() {
        return getMyNotVipCustomerListFromMS();
    }

    /**
     * 登录用户所负责的普通客户
     *
     * @return
     */
    public static List<Customer> getMyNotVipCustomerListFromMS() {
        // 从微服务中获取
        //log.warn("从微服务中获取登录用户所负责的用户");
        User user = UserUtils.getUser();
        List<Customer> customerList = Lists.newArrayList();
        if(user.isCustomer()){
            Customer customer = UserUtils.getUser().getCustomerAccountProfile().getCustomer();
            customerList.add(customer);
        } else if(user.isSalesPerson()){  // 业务员
            customerList.addAll(msCustomerService.findNotVipListBySalesId(user.getId().intValue()));
            if (customerList != null && !customerList.isEmpty()) {
                customerList = customerList.stream().sorted(Comparator.comparing(Customer::getName)).collect(Collectors.toList());
            }
        } else if (user.isMerchandiser()) { //跟单员
            customerList.addAll(msCustomerService.findNotVipListByMerchandiserId(user.getId()));
            if (customerList != null && !customerList.isEmpty()) {
                customerList = customerList.stream().sorted(Comparator.comparing(Customer::getName)).collect(Collectors.toList());
            }
        } else if (user.isSalesManager()) {
            //1. 先获取客户列表列表
            List<Long> customerIdList = userSubService.findCustomerIdListByUserId(user.getId(), user.getUserType());
            //2.去掉重复的客户id
            List<Long> uniqueCustomerIdList = Optional.ofNullable(customerIdList).orElse(Collections.emptyList()).stream().distinct().collect(Collectors.toList());
            if (!uniqueCustomerIdList.isEmpty()) {
                customerList = msCustomerService.findNotVipListByCustomerIdsFromCacheForRPT(uniqueCustomerIdList);
            }
        } else {
            customerList.addAll(msCustomerService.findAllNotVipCustomerList());
        }
        return customerList;
    }

    /**
     * 获取所有的VIP客户列表 //add on 2019-12-9
     * @return
     */
    public static List<Customer> getVipCustomerListFromMS() {
        return msCustomerService.findListByVipCustomer();
    }


    /**
     * 获得最大id的客户
     * @return
     */
    public static Customer getMaxCustomer(){
        List<Customer> customers = getCustomerListFromMS();
        if(customers != null && !customers.isEmpty()) {
            return customers.stream().sorted(Comparator.comparing(Customer::getId).reversed()).findFirst().orElse(null);
        }
        return null;
    }

    /**
     * 获取业务员负责的客户
     */
    public static List<Customer> getCustomerListBySales(User user) {
        List<Customer> customerList = Lists.newArrayList();
        if(user.isSalesPerson()){  // 业务员
            customerList.addAll(msCustomerService.findListBySalesId(user.getId().intValue()));
        } else if (user.isMerchandiser()) { //跟单员
            customerList.addAll(msCustomerService.findListByMerchandiserId(user.getId()));
        }  else if (user.isSalesManager()) {  //业务主管
            //1. 先获取客户列表列表
            List<Long> customerIdList = userSubService.findCustomerIdListByUserId(user.getId(), user.getUserType());
            //2.去掉重复的客户id
            List<Long> uniqueCustomerIdList = Optional.ofNullable(customerIdList).orElse(Collections.emptyList()).stream().distinct().collect(Collectors.toList());
            if (!uniqueCustomerIdList.isEmpty()) {
                customerList = customerService.findIdAndNameListByIds(uniqueCustomerIdList);
            }
        }
        return customerList;
    }
}
