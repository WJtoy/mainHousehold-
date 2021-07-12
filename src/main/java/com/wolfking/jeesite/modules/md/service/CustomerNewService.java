package com.wolfking.jeesite.modules.md.service;

import com.google.common.collect.Lists;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.common.NameValuePair;
import com.kkl.kklplus.entity.md.MDCustomer;
import com.kkl.kklplus.entity.md.MDCustomerAddress;
import com.kkl.kklplus.entity.md.MDCustomerEnum;
import com.kkl.kklplus.entity.md.MDCustomerVipLevel;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.fi.dao.CustomerCurrencyDao;
import com.wolfking.jeesite.modules.fi.entity.CustomerCurrency;
import com.wolfking.jeesite.modules.md.dao.CustomerFinanceDao;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.CustomerAccountProfile;
import com.wolfking.jeesite.modules.md.entity.CustomerFinance;
import com.wolfking.jeesite.modules.md.entity.MdAttachment;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.modules.sys.dao.UserDao;
import com.wolfking.jeesite.modules.sys.entity.*;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.service.SystemService;
import com.wolfking.jeesite.modules.sys.utils.AreaUtils;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.modules.sys.utils.SeqUtils;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.mapper.common.PageMapper;
import com.wolfking.jeesite.ms.providermd.feign.MSCustomerFeign;
import com.wolfking.jeesite.ms.providermd.service.*;
import com.wolfking.jeesite.ms.service.sys.MSUserService;
import com.wolfking.jeesite.ms.utils.MSUserUtils;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.MyBatisSystemException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class CustomerNewService {

    @Autowired
    private MSCustomerFeign msCustomerFeign;

    @Autowired
    private MSCustomerService msCustomerService;

    @Autowired
    private MSCustomerNewService msCustomerNewService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private MSUserService msUserService;

    @Autowired
    private SystemService systemService;

    @Autowired
    private MSAttachmentService msAttachmentService;

    @Autowired
    private MSCustomerAccountProfileService msCustomerAccountProfileService;

    @Resource
    private UserDao userDao;

    @Resource
    private CustomerFinanceDao customerFinanceDao;

    @Resource
    private CustomerCurrencyDao customerCurrencyDao;

    @Autowired
    private AreaService areaService;

    @Autowired
    private MSCustomerVipLevelService msCustomerVipLevelService;

    @Autowired
    private MSRegionPermissionNewService msRegionPermissionNewService;

    @Autowired
    private OrderService orderService;
    /**
     * 获取客户列表
     *
     * @param page
     * @param customer(code,name,salesid)
     * @return id,
     * code,
     * name,
     * master,
     * phone,
     * email,
     * technologyOwner,
     * technologyOwnerPhone,
     * defaultBrand,
     * effectFlag,
     * shortMessageFlag,
     * remarks
     */
    public Page<MDCustomer> findMDCustomerNewList(Page<MDCustomer> page, MDCustomer customer) {
        if (customer.getPage() == null) {
            PageMapper.INSTANCE.toMSPage(page);
        }
        Page<MDCustomer> customerPage = new Page<>();
        customerPage.setPageSize(page.getPageSize());
        customerPage.setPageNo(page.getPageNo());
        customer.setPage(new MSPage<>(customerPage.getPageNo(), customerPage.getPageSize()));
        MSResponse<MSPage<MDCustomer>> returnCustomer = msCustomerFeign.findCustomerList(customer);
        if (MSResponse.isSuccess(returnCustomer)) {
            MSPage<MDCustomer> data = returnCustomer.getData();
            Set<Long> merchandiserId = data.getList().stream().map(MDCustomer::getMerchandiserId).collect(Collectors.toSet());
            Set<Long> salesId = data.getList().stream().map(MDCustomer::getSalesId).collect(Collectors.toSet());
            salesId.addAll(merchandiserId);
            Map<Long, String> userNames = MSUserUtils.getNamesByUserIds(Lists.newArrayList(salesId));
            if (userNames != null) {
                for (MDCustomer mdCustomer : data.getList()) {
                    if (userNames.get(mdCustomer.getSalesId()) != null) {
                        mdCustomer.setSalesName(userNames.get(mdCustomer.getSalesId()));
                    }
                    if (userNames.get(mdCustomer.getMerchandiserId()) != null) {
                        mdCustomer.setMerchandiserName(userNames.get(mdCustomer.getMerchandiserId()));
                    }
                }
            }

            customerPage.setCount(data.getRowCount());
            customerPage.setList(data.getList());
            log.warn("findMDCustomerList返回的数据:{}", data.getList());
        } else {
            customerPage.setCount(0);
            customerPage.setList(new ArrayList<>());
            log.warn("findMDCustomerList返回无数据返回,参数customer:{}", customer);
        }
        return customerPage;
    }


    public Customer get(long id) {
        Customer customer = msCustomerService.getByIdToCustomerSpecifiedColumn(id);  //add on 2019-7-22 // 微服务调用
        if (customer != null && customer.getSales() != null && customer.getSales().getId() != null) {
            User sales = MSUserUtils.get(customer.getSales().getId());
            if (sales != null) {
                customer.getSales().setName(sales.getName());
                customer.getSales().setMobile(sales.getMobile());
                customer.getSales().setQq(sales.getQq());
            }
            try {
                Long userId = userDao.checkLoginCustomerId(customer.getMaster(), customer.getPhone(),customer.getId());
                if (userId != null) {
                    customer.setUserId(userId);
                }
            } catch (MyBatisSystemException e) {
                throw new RuntimeException("读取用户联系信息失败，请重试" + e.getMessage());
            }


            CustomerFinance customerFinance = customerService.getFinance(id);
            if (customerFinance != null) {
                customer.setFinance(customerFinance);
            }
            if (customer.getMerchandiser() != null && customer.getMerchandiser().getId() != null) {
                User merchandiser = MSUserUtils.get(customer.getMerchandiser().getId());
                if (merchandiser != null) {
                    customer.getMerchandiser().setName(merchandiser.getName());
                }
            }
            List<MDCustomerAddress> customerAddresses = msCustomerNewService.getCustomerAllAddress(id);


            for (MDCustomerAddress addresses : customerAddresses) {
                addresses.setAreaName(AreaUtils.getCountyFullName(addresses.getAreaId()));
            }


            Map<Integer, List<MDCustomerAddress>> customerAddressMap = customerAddresses.stream().collect(Collectors.groupingBy(MDCustomerAddress::getAddressType));

            List<MDCustomerAddress> customerAddressList = new ArrayList<>();
            int customerAddressType = MDCustomerEnum.CustomerAddressType.CUSTOMERADDR.getValue();
            int shipAddressType = MDCustomerEnum.CustomerAddressType.SHIPADDR.getValue();
            int returnAddressType = MDCustomerEnum.CustomerAddressType.RETURNADDR.getValue();

            List<MDCustomerAddress> customerAddress = customerAddressMap.get(customerAddressType);
            List<MDCustomerAddress> shipAddress = customerAddressMap.get(shipAddressType);
            List<MDCustomerAddress> returnAddress = customerAddressMap.get(returnAddressType);

            if (customerAddress == null) {
                customerAddress = Lists.newArrayList();
                customerAddress.add(new MDCustomerAddress(MDCustomerEnum.CustomerAddressType.CUSTOMERADDR.getValue(), MDCustomerEnum.CustomerAddressType.CUSTOMERADDR.getLabel()));
            }
            if (shipAddress == null) {
                shipAddress = Lists.newArrayList();
                shipAddress.add(new MDCustomerAddress(MDCustomerEnum.CustomerAddressType.SHIPADDR.getValue(), MDCustomerEnum.CustomerAddressType.SHIPADDR.getLabel()));
            }

            for (MDCustomerAddress address : customerAddress) {
                address.setAddressTypeName(MDCustomerEnum.CustomerAddressType.fromValue(customerAddressType).getLabel());
            }

            for (MDCustomerAddress address : shipAddress) {
                address.setAddressTypeName(MDCustomerEnum.CustomerAddressType.fromValue(shipAddressType).getLabel());
            }


            customerAddressList.addAll(customerAddress);
            customerAddressList.addAll(shipAddress);
            if (returnAddress != null) {
                for (MDCustomerAddress address : returnAddress) {
                    address.setAddressTypeName(MDCustomerEnum.CustomerAddressType.fromValue(returnAddressType).getLabel());
                }

                customerAddressList.addAll(returnAddress);
            }

            customer.setCustomerAddresses(customerAddressList);
        }
        return customer;
    }

    public void saveCustomerFinance(CustomerFinance customerFinance, Integer type) {
        if (customerFinance.getId() != null) {
            customerFinance.preUpdate();
            if (type == 1) {
                customerFinanceDao.updatePublic(customerFinance);
            } else if (type == 2) {
                customerFinanceDao.updatePrivate(customerFinance);
            }
        }
    }


    @Transactional(readOnly = false)
    public void save(Customer customer, Double oldCredit) throws Exception {
        boolean isNew = customer.getIsNewRecord();
        customer.setDelFlag(customer.DEL_FLAG_NORMAL);
        MdAttachment logo = null;
        MdAttachment attachment1 = null;
        MdAttachment attachment2 = null;
        MdAttachment attachment3 = null;
        MdAttachment attachment4 = null;

        if (customer.getLogo() == null) {
            customer.setLogo(new MdAttachment(0L));
        } else if (customer.getLogo().canAdd()) {

            logo = customer.getLogo();
            logo.preInsert();
            logo.setId(null);
        } else {
            customer.getLogo().setId(0L);
        }

        if (customer.getAttachment1() == null) {
            customer.setAttachment1(new MdAttachment(0L));
        } else if (customer.getAttachment1().canAdd()) {

            attachment1 = customer.getAttachment1();
            attachment1.setId(null);
            attachment1.preInsert();
        } else {
            customer.getAttachment1().setId(0L);
        }

        if (customer.getAttachment2() == null) {
            customer.setAttachment2(new MdAttachment(0L));
        } else if (customer.getAttachment2().canAdd()) {

            attachment2 = customer.getAttachment2();
            attachment2.setId(null);
            attachment2.preInsert();
        } else {
            customer.getAttachment2().setId(0L);
        }

        if (customer.getAttachment3() == null) {
            customer.setAttachment3(new MdAttachment(0L));
        } else if (customer.getAttachment3().canAdd()) {
            attachment3 = customer.getAttachment3();
            attachment3.setId(null);
            attachment3.preInsert();
        } else {
            customer.getAttachment3().setId(0L);
        }
        if(customer.getVipFlag() == 0){
            customer.setVip(0);
        }
        if (customer.getAttachment4() == null) {
            customer.setAttachment4(new MdAttachment(0L));
        } else if (customer.getAttachment4().canAdd()) {
            attachment4 = customer.getAttachment4();
            attachment4.setId(null);
            attachment4.preInsert();
        } else {
            customer.getAttachment4().setId(0L);
        }
        if(customer.getRemarks() != null){
            customer.setRemarks(customer.getRemarks().replace("\r",""));
        }
        Customer customer1 = null;
        if (!isNew) {
            customer1 = customerService.getFromCache(customer.getId());
        }
        //业务员
        User sales = systemService.getUser(customer.getSales().getId());
        if (sales == null) {
            throw new RuntimeException("读取业务员联系信息失败，请重试");
        }
        //paymentType
        CustomerFinance finance = customer.getFinance();
        customer.setPaymentType(finance.getPaymentType());

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

        // 调用customer微服务
        customer.preInsert();
        Area city;
        Area province;
        for (MDCustomerAddress address : customer.getCustomerAddresses()) {
            if (address.getAreaId() != null) {
                city = areaService.getFromCache(address.getAreaId());
                if (city != null) {
                    address.setCityId(city.getParentId());
                    province = areaService.getFromCache(city.getParentId());
                    if (province != null) {
                        address.setProvinceId(province.getParentId());
                    }
                }
            }
            if (address.getAddress() != null) {
                address.setAddress(address.getAddress().replace("&nbsp;", " "));
            }
            address.setCustomerId(Optional.ofNullable(customer.getId()).orElse(0L));
            address.setProvinceId(Optional.ofNullable(address.getProvinceId()).orElse(0L));
            address.setCityId(Optional.ofNullable(address.getCityId()).orElse(0L));
            address.setAreaId(Optional.ofNullable(address.getAreaId()).orElse(0L));
            address.setUserName(Optional.ofNullable(address.getUserName()).orElse(""));
            address.setContactInfo(Optional.ofNullable(address.getContactInfo()).orElse(""));
        }
        Long id = customer.getId();
        Long profileId = null;
        if (isNew) {
            MSResponse<NameValuePair<Long, Long>> msResponse = msCustomerNewService.insertCustomerUnion(customer);
            if (msResponse.getCode() != 0) {
                throw new RuntimeException("保存客户信息到微服务失败!失败原因: " + msResponse.getMsg());
            } else {
                id = msResponse.getData().getName();
                profileId = msResponse.getData().getValue();
            }
        } else {
            MSResponse<Integer> updateCustomerUnion = msCustomerNewService.updateCustomerUnion(customer);
            if (updateCustomerUnion.getCode() > 0) {
                throw new RuntimeException("修改客户失败，请重试");
            }
        }


        // add on 2019-7-2 end
        customer.setId(id);
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
            Role r = systemService.getRole(4L);
            roleList.add(r);
            customerAccount.setRoleList(roleList);

            CustomerAccountProfile profile = new CustomerAccountProfile();
            profile.setRemarks(customer.getName());
            profile.setCreateBy(customer.getCreateBy());
            profile.setCreateDate(customer.getCreateDate());
            profile.setCustomer(customer);
            profile.setOrderApproveFlag(0);//不需审核订单

            profile.setId(profileId);
            customerAccount.setCustomerAccountProfile(profile);
            customerAccount.preInsert();
            userDao.insert(customerAccount);
            userDao.insertUserRole(customerAccount);//角色
            MSUserUtils.addUserToRedis(customerAccount);//user微服务


        } else {

            customer.getFinance().setId(customerId);
            finance.preUpdate();
            customerFinanceDao.update(finance);
            //修改客户主帐号
            if (customer1.getPhone() != null && customer1.getPhone().length() > 1) {
                // todo:客户帐号串号重要监控处
                User user1 = null;
                // add on 2019-7-29 begin
                //User user2 = userDao.getByLoginNameAndTypeWithoutCustomerAccountProfile(customer1.getPhone(), new Integer[]{3});  // add on 202-12-3
                User user2 = systemService.getByLoginNameAndTypeWithoutCustomerAccountProfile(customer1.getPhone(), new Integer[]{3});  //add on 2020-12-3
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
                    Role r = systemService.getRoleById(4L);
                    roleList.add(r);
                    user1.setRoleList(roleList);
                    //先删除旧角色
                    userDao.deleteUserRole(user1);
                    userDao.insertUserRole(user1);

                }
            }
        }
        //修改客户
        if (!isNew) {
            try {
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

    }

    //检查客户是否存在
    public String existsCustomerByName(Long customerId, String name) {
        MSResponse<Long> response = msCustomerNewService.existByName(name);
        String date = "true";
        if (MSResponse.isSuccessCode(response)) {
            Long id = response.getData();
            if (id != null && !id.equals(customerId)) {
                date = "客户名称已存在";
            }
        } else {
            date = "客户名称检查不成功";
        }
        return date;
    }

    public List<MDCustomerVipLevel> customerVipLevelList(){
        return msCustomerVipLevelService.findAllIdAndNameList();
    }

    public MDCustomerVipLevel findCustomerLevel(){
        return msRegionPermissionNewService.findCustomerLevel();
    }

    //判断客户是否有工单  true:无   false:有
    public Boolean isCustomerOrOrder(Long customerId){
        Long id = orderService.getOrderIdByCustomerId(customerId);
        if(id == null){
            return true;
        }else{
            return false;
        }
    }
}
