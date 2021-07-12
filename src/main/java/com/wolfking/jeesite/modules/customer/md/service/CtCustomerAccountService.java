package com.wolfking.jeesite.modules.customer.md.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.service.ServiceException;
import com.wolfking.jeesite.common.utils.GsonUtils;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.CustomerAccountProfile;
import com.wolfking.jeesite.modules.md.entity.CustomerShop;
import com.wolfking.jeesite.modules.md.entity.viewModel.CustomerShopModel;
import com.wolfking.jeesite.modules.sys.dao.UserDao;
import com.wolfking.jeesite.modules.sys.entity.*;
import com.wolfking.jeesite.modules.sys.service.SystemService;
import com.wolfking.jeesite.modules.sys.service.UserAttributesService;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providermd.service.MSCustomerAccountProfileService;
import com.wolfking.jeesite.ms.providermd.service.MSCustomerService;
import com.wolfking.jeesite.ms.service.sys.MSUserService;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import com.wolfking.jeesite.ms.utils.MSUserUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CtCustomerAccountService {
    @Autowired
    private MSCustomerService msCustomerService;

    @Autowired
    private MSCustomerAccountProfileService msCustomerAccountProfileService;

    @Autowired
    private MSUserService msUserService;

    @Resource
    private UserDao userDao;

    @Autowired
    private SystemService systemService;

    @Autowired
    private UserAttributesService userAttributesService;

    @Autowired
    private RedisUtils redisUtils;

    public Page<User> findCustomerAccountProfile(Page<User> page, User user) {
        user.setPage(page);
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
                customerMap = customerList.stream().collect(Collectors.toMap(Customer::getId, Function.identity()));
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


        List<User> userList = userDao.findListWithOutCustomerAccountProfile(user);
        // 如果是业务员,要对数据进行筛选
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
        }

        page.setList(userList);
        return page;
    }

    public User getAccount(Long id) {
        //User user = userDao.get(id);  //mark on 2020-12-4
        User user = systemService.getUserFromDb(id);  //add on 2020-12-4
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
    public void save(User user,String customerShops) {
        boolean isNew = user.getIsNewRecord();
        if (isNew) {
            CustomerAccountProfile profile = user.getCustomerAccountProfile();
            profile.preInsert();

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

            if(user.getId() != null && user.getId() > 0){
                if(!customerShops.equals("") && customerShops.length() > 0){
                    UserAttributes userAttributes = new UserAttributes();
                    userAttributes.setUserAttributes(customerShops);
                    userAttributes.setUserId(user.getId());
                    userAttributes.setType(UserAttributesEnum.CUSTOMERSHOP.getValue());
                    userAttributesService.saveUserAttributes(userAttributes);
                }
            }

            // 更新用户与角色关联
            if (user.getRoleList() != null && user.getRoleList().size() > 0) {
                userDao.insertUserRole(user);
            } else {
                throw new ServiceException(user.getLoginName() + "没有设置角色！");
            }
        } else {
            user.getCustomerAccountProfile().preUpdate();

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
            if(user.getCustomerAccountProfile() != null && user.getCustomerAccountProfile().getCustomer() != null && user.getCustomerAccountProfile().getCustomer().getId() != null){
                if(user.getCompany() != null){
                    user.getCompany().setId(user.getCustomerAccountProfile().getCustomer().getId());
                }else {
                    user.setCompany(new Office(user.getCustomerAccountProfile().getCustomer().getId()));
                }
            }
            userDao.updateNew(user);
            msUserService.refreshUserCacheByUserId(user.getId());//user微服务
//            // 删除原角色
//            userDao.deleteUserRole(user);

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

    public CustomerAccountProfile getCustomerAccountProfileByUserId(Long userId) {
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
    }

    /**
     * 重置密码
     * 手机号后6位
     *
     * @param account
     */
    @Transactional(readOnly = false)
    public void resetPassword(User account) {
        userDao.resetPassword(account);
    }


    public List<CustomerShop> getCustomerShopList(Long userId){

        List<CustomerShop> customerShopList = Lists.newArrayList();
        UserAttributes userAttributes = new UserAttributes();
        if(userId != null){
            userAttributes = userAttributesService.getUserAttributesList(userId, UserAttributesEnum.CUSTOMERSHOP.getValue());
        }
        if(userAttributes != null && userAttributes.getUserAttributes() != null){
            Gson g = new Gson();
            customerShopList = g.fromJson(userAttributes.getUserAttributes(), new TypeToken<List<CustomerShop>>() {}.getType());
        }
        return customerShopList;
    }

    public void saveCustomerShop(CustomerShopModel customerShopModel){

        List<CustomerShop> customerShopList;
        if(customerShopModel != null && customerShopModel.getCustomerShops() != null){
            userAttributesService.deleteUserAttributes(customerShopModel.getUserId(),UserAttributesEnum.CUSTOMERSHOP.getValue());
            customerShopList = customerShopModel.getCustomerShops();
            UserAttributes userAttributes = new UserAttributes();
            userAttributes.setUserAttributes(GsonUtils.toGsonString(customerShopList));
            userAttributes.setUserId(customerShopModel.getUserId());
            userAttributes.setType(UserAttributesEnum.CUSTOMERSHOP.getValue());
            userAttributesService.saveUserAttributes(userAttributes);

            redisUtils.remove(String.format(RedisConstant.SYS_USER_ID,customerShopModel.getUserId()));//保存后清除缓存
        }else {
            if(customerShopModel != null){
                userAttributesService.deleteUserAttributes(customerShopModel.getUserId(),UserAttributesEnum.CUSTOMERSHOP.getValue());
                redisUtils.remove(String.format(RedisConstant.SYS_USER_ID,customerShopModel.getUserId()));//保存后清除缓存
            }
        }
    }

    public String getCustomerShopNames(CustomerShopModel customerShopModel){
        List<String> CustomerShopNames = Lists.newArrayList();
        if(customerShopModel != null && customerShopModel.getCustomerShops() != null){
            for(CustomerShop customerShop:customerShopModel.getCustomerShops()){
                CustomerShopNames.add(customerShop.getName());
            }
        }
        return CustomerShopNames.stream().map(String::valueOf).collect(Collectors.joining("，"));
    }
}
