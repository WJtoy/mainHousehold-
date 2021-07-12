/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.wolfking.jeesite.modules.sys.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.kkl.kklplus.entity.md.GlobalMappingSalesSubFlagEnum;
import com.kkl.kklplus.entity.md.GlobalMappingSyncTypeEnum;
import com.kkl.kklplus.entity.sys.SyncTypeEnum;
import com.kkl.kklplus.entity.sys.mq.MQSysUserCustomerMessage;
import com.kkl.kklplus.entity.sys.mq.MQSysUserSubMessage;
import com.wolfking.jeesite.common.config.Global;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.security.Digests;
import com.wolfking.jeesite.common.security.shiro.session.SessionDAO;
import com.wolfking.jeesite.common.service.BaseService;
import com.wolfking.jeesite.common.service.ServiceException;
import com.wolfking.jeesite.common.utils.Encodes;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.api.entity.md.RestResetPassword;
import com.wolfking.jeesite.modules.api.entity.md.RestUpdatePassword;
import com.wolfking.jeesite.modules.api.util.ErrorCode;
import com.wolfking.jeesite.modules.api.util.RestResult;
import com.wolfking.jeesite.modules.api.util.RestResultGenerator;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.mq.sender.SysUserCustomerSender;
import com.wolfking.jeesite.modules.mq.sender.SysUserSubSender;
import com.wolfking.jeesite.modules.sys.dao.MenuDao;
import com.wolfking.jeesite.modules.sys.dao.RoleDao;
import com.wolfking.jeesite.modules.sys.dao.UserDao;
import com.wolfking.jeesite.modules.sys.dao.UserSubDao;
import com.wolfking.jeesite.modules.sys.entity.*;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.globalmapping.service.ProductCategoryUserMappingService;
import com.wolfking.jeesite.ms.providermd.service.MSCustomerService;
import com.wolfking.jeesite.ms.providersys.feign.MSSysUserCustomerFeign;
import com.wolfking.jeesite.ms.providersys.service.MSSysOfficeService;
import com.wolfking.jeesite.ms.providersys.service.MSSysUserCustomerService;
import com.wolfking.jeesite.ms.service.sys.MSUserService;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import com.wolfking.jeesite.ms.utils.MSUserUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.wolfking.jeesite.common.config.redis.RedisConstant.RedisDBType.REDIS_TEMP_DB;
import static com.wolfking.jeesite.common.config.redis.RedisConstant.VERCODE_KEY;
import static com.wolfking.jeesite.common.utils.Collections3.distinctByKey;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.Comparator.comparingLong;
import static java.util.stream.Collectors.toCollection;

/**
 * 系统管理，安全相关实体的管理类,包括用户、角色、菜单.
 *
 * @author ThinkGem
 * @version 2013-12-05
 */
@Service
@Slf4j
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class SystemService extends BaseService {

    public static final String HASH_ALGORITHM = "SHA-1";
    public static final int HASH_INTERATIONS = 1024;
    public static final int SALT_SIZE = 8;

    @Autowired
    private UserDao userDao;
    @Autowired
    private RoleDao roleDao;
    @Autowired
    private MenuDao menuDao;
    @Autowired
    private SessionDAO sessionDao;

    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private MSUserService msUserService;
    @Autowired
    private ProductCategoryUserMappingService productCategoryUserMappingService;

    @Autowired
    private UserRegionService userRegionService;

    @Autowired
    private MSCustomerService msCustomerService;

    @Autowired
    private MSSysUserCustomerService msSysUserCustomerService;

    @Autowired
    private SysUserCustomerSender sysUserCustomerSender;

    @Autowired
    private MSSysOfficeService msSysOfficeService;

    @Autowired
    private SysUserSubSender sysUserSubSender;

    @Resource
    private UserSubDao userSubDao;
    private final String USER_CACHE_ID_ = "user:id:";
    private final String USER_CACHE_LOGIN_NAME_ = "user:name:";

    public SessionDAO getSessionDao() {
        return sessionDao;
    }


    //region User Service

    /**
     * 获取用户
     * 优先缓存
     *
     * @param id
     * @return
     */
    public User getUser(Long id) {
        return UserUtils.get(id);
    }

    /**
     * 从数据库获取用户
     *
     * @param id
     * @return
     */
    public User getUserFromDb(Long id) {
        //return userDao.get(id);
        //User user = userDao.get(id);   // mark on 2020-12-19

        // add on 2020-12-3 begin  office去微服务化
        User newUser = userDao.getNew(id);
        List<Long> ids = Lists.newArrayList();
        Optional.ofNullable(newUser).map(r->r.getCompany()).map(Office::getId).ifPresent(ids::add);
        Optional.ofNullable(newUser).map(r->r.getOffice()).map(Office::getId).ifPresent(ids::add);

        List<Office> officeList = Lists.newArrayList();
        if (ids.size() >1) {
            officeList = msSysOfficeService.findSpecColumnListByIds(ids);
        } else if (ids.size() == 1){
            Office office = msSysOfficeService.getSpecColumnById(ids.get(0));
            Optional.ofNullable(office).ifPresent(officeList::add);
        }
        Map<Long, Office> officeMap = ObjectUtils.isEmpty(officeList)?Maps.newHashMap():officeList.stream().collect(Collectors.toMap(r->r.getId(), r->r, (v2,v1)->v1));
        if (newUser != null && newUser.getCompany() != null && newUser.getCompany().getId() != null) {
            Office company = officeMap.get(newUser.getCompany().getId());
            if (company != null) {
                newUser.setCompany(company);  //获取 name,parent_id,parent_ids
            }
        }
        if (newUser != null && newUser.getOffice() != null && newUser.getOffice().getId() != null) {
            Office office = officeMap.get(newUser.getOffice().getId());
            if (office != null) {
                newUser.setOffice(office);  //获取 name,parent_id,parent_ids
            }
        }

//        List<Office> localOfficeList = Lists.newArrayList();
//        Optional.ofNullable(user).map(r->r.getCompany()).ifPresent(localOfficeList::add);
//        Optional.ofNullable(user).map(r->r.getOffice()).ifPresent(localOfficeList::add);
//
//        msSysOfficeService.compareListOffice(String.format("id=%s",id),localOfficeList, officeList, "SystemService.getUserFromDb");
        // add on 2020-12-3 end

        //return user;
        return newUser;
    }

    /**
     * 读取账户登录信息，包含ip(key:login_ip)和日期(key:login_date)
     * @param id
     * @return
     */
    public HashMap<String, Object> getLoginInfo(Long id){
        return userDao.getLoginInfo(id);
    }


    /**
     * 按安维id获得帐号信息
     *
     * @param engineerId
     * @return
     */
    public User getUserByEngineerId(Long engineerId) {
        return userDao.getByEngineerId(engineerId);
    }

    /**
     * 根据登录名获取用户
     * 先去缓存，再取数据库
     *
     * @param loginName
     * @return
     */
    public User getUserByLoginName(String loginName) {
        return UserUtils.getByLoginName(loginName);
    }

    /**
     * 根据登录名获取APP用户
     *
     * @param loginName
     * @return
     */
    public User getAppUserByLoginName(String loginName) {
        return UserUtils.getAppUserByLoginName(loginName);
    }

    /**
     * 根据登录名从数据库获取用户
     *
     * @param loginName
     * @return
     */
    /*
    // 没地方调用，注释代码   2020-12-3
    public User getUserByLoginNameFromDb(String loginName) {
        return userDao.getByLoginName(new User(null, loginName));
    }
    */

    /**
     * 检查用户帐号是否注册
     *
     * @param id        除此用户id外（修改时检查用）
     * @param loginName 登录帐号（安维，厂商使用手机号）
     * @return
     */
    public Long checkLoginName(Long id, String loginName) {
        Long userId = userDao.checkLoginName(id, loginName);
        if (userId == null) {
            return 0l;
        }
        return userId;
    }

    public User getByLoginNameAndTypeWithoutCustomerAccountProfile(String loginName,  Integer[] userTypes) {
        //User user = userDao.getByLoginNameAndTypeWithoutCustomerAccountProfile(loginName, userTypes);  //mark on 2020-12-19

        // add on 2020-12-3 begin  office去微服务化
        User newUser = userDao.getByLoginNameAndTypeWithoutCustomerAccountProfileNew(loginName, userTypes);
        List<Long> ids = Lists.newArrayList();
        Optional.ofNullable(newUser).map(r->r.getCompany()).map(Office::getId).ifPresent(ids::add);
        Optional.ofNullable(newUser).map(r->r.getOffice()).map(Office::getId).ifPresent(ids::add);

        List<Office> officeList = Lists.newArrayList();
        if (ids.size() >1) {
            officeList = msSysOfficeService.findSpecColumnListByIds(ids);
        } else if (ids.size() == 1){
            Office office = msSysOfficeService.getSpecColumnById(ids.get(0));
            Optional.ofNullable(office).ifPresent(officeList::add);
        }
        Map<Long, Office> officeMap = ObjectUtils.isEmpty(officeList)?Maps.newHashMap():officeList.stream().collect(Collectors.toMap(r->r.getId(), r->r, (v2,v1)->v1));
        if (newUser != null && newUser.getCompany() != null && newUser.getCompany().getId() != null) {
            Office company = officeMap.get(newUser.getCompany().getId());
            if (company != null) {
                newUser.setCompany(company);  //获取 name,parent_id,parent_ids
            }
        }
        if (newUser != null && newUser.getOffice() != null && newUser.getOffice().getId() != null) {
            Office office = officeMap.get(newUser.getOffice().getId());
            if (office != null) {
                newUser.setOffice(office);  //获取 name,parent_id,parent_ids
            }
        }

        // mark on 2020-12-19 begin
//        List<Office> localOfficeList = Lists.newArrayList();
//        Optional.ofNullable(user).map(r->r.getCompany()).ifPresent(localOfficeList::add);
//        Optional.ofNullable(user).map(r->r.getOffice()).ifPresent(localOfficeList::add);
//
//        msSysOfficeService.compareListOffice(String.format("loginname=%s,userTypes=%s",loginName, userTypes),localOfficeList, officeList, "SystemService.getByLoginNameAndTypeWithoutCustomerAccountProfile");
        // mark on 2020-12-19 end
        // add on 2020-12-3 end

        //return user; //mark on 2020-12-19
        return newUser;
    }

    /**
     * 读取业务员列表
     * 返回：id,name,qq,mobile
     */
    public List<User> getSaleList(Integer delFlag) {
        return userDao.getSaleList(GlobalMappingSalesSubFlagEnum.SALES.getValue(),delFlag);
    }

    /**
     * 获取所有业务员不限子类型
     * @return
     */
    public List<User> getAllSales() {
        return userDao.getAllSales();
    }

    public Page<User> findUser(Page<User> page, User user) {
        // 生成数据权限过滤条件（dsf为dataScopeFilter的简写，在xml中使用 ${sqlMap.dsf}调用权限SQL）
        //user.getSqlMap().put("dsf", dataScopeFilter(user.getCurrentUser(), "o", "a"));
        // 设置分页参数
        user.setPage(page);
        // 执行分页查询
        // page.setList(userDao.findList(user));  // mark on 2017-4-13


        // add on 2017-4-13 begin
        // mark on 2020-12-19 begin
//        List<Office> localOfficeList = Lists.newArrayList();
//        List<User> userList = userDao.findList(user);
//        for (User entity : userList) {
//            entity.setRoleList(roleDao.findList(new Role(entity)));
//            Optional.ofNullable(entity).map(r->r.getCompany()).ifPresent(localOfficeList::add);
//            Optional.ofNullable(entity).map(r->r.getOffice()).ifPresent(localOfficeList::add);
//        }
        // mark on 2020-12-19 end

        // add on 2020-12-3 begin  office微服务化
        List<Long> companyIds = Lists.newArrayList();
        List<Long> officeIds = Lists.newArrayList();
        if (user != null && user.getCompany() != null && user.getCompany().getId() >0) {
            companyIds = msSysOfficeService.findIdListById(user.getCompany().getId());
            user.setCompanyIds(companyIds);
        }
        if (user != null && user.getOffice() != null && user.getOffice().getId() >0) {
            officeIds = msSysOfficeService.findIdListById(user.getOffice().getId());
            user.setOfficeIds(officeIds);
        }
        List<User> newUserList = userDao.findListNew(user);
        for (User entity : newUserList) {
            entity.setRoleList(roleDao.findListNew(new Role(entity)));
        }

        Set<Long> ids = Sets.newHashSet();
        ids.addAll(companyIds);
        ids.addAll(officeIds);
        for (User entity : newUserList) {
            Optional.ofNullable(entity).map(r->r.getCompany()).map(r->r.getId()).ifPresent(ids::add);
            Optional.ofNullable(entity).map(r->r.getOffice()).map(r->r.getId()).ifPresent(ids::add);
        }

        List<Office> officeList = Lists.newArrayList();
        if (ids.size() >1) {
            officeList = msSysOfficeService.findSpecColumnListByIds(ids.stream().collect(Collectors.toList()));
        } else if (ids.size() == 1){
            Office office = msSysOfficeService.getSpecColumnById(Lists.newArrayList(ids).get(0));
            Optional.ofNullable(office).ifPresent(officeList::add);
        }
        Map<Long, Office> officeMap;
        officeMap = ObjectUtils.isEmpty(officeList)?Maps.newHashMap():officeList.stream().collect(Collectors.toMap(r->r.getId(), r->r, (v2,v1)->v1));

        if (!ObjectUtils.isEmpty(newUserList)) {
            for (User newUser : newUserList) {
                if (newUser != null && newUser.getOffice() != null && newUser.getOffice().getId() != null) {
                    Office office = officeMap.get(newUser.getOffice().getId());
                    if (office != null) {
                        newUser.setOffice(office);  //获取 name,parent_id,parent_ids
                    }
                }
            }
            // 排序字段: c.code, o.code, a.name
        }

//        msSysOfficeService.compareListOffice("",localOfficeList, officeList, "SystemService.findUser");  //mark on 2020-12-19
        // add on 2020-12-3 end

//        page.setList(userList);  //mark on 2020-12-19
        page.setList(newUserList); //add on 2020-12-19
        // add on 2017-4-13 end
        return page;
    }

    /**
     * 无分页查询人员列表
     *
     * @param user
     * @return
     */
    public List<User> findUser(User user) {
        // 生成数据权限过滤条件（dsf为dataScopeFilter的简写，在xml中使用 ${sqlMap.dsf}调用权限SQL）
        //user.getSqlMap().put("dsf", dataScopeFilter(user.getCurrentUser(), "o", "a"));
        //mark on 2020-12-19 begin
//        List<User> list = userDao.findList(user);

        // add on 2020-12-3 begin  office微服务化
//        List<Office> localOfficeList = Lists.newArrayList();
//        for (User entity : list) {
//            Optional.ofNullable(entity).map(r->r.getCompany()).ifPresent(localOfficeList::add);
//            Optional.ofNullable(entity).map(r->r.getOffice()).ifPresent(localOfficeList::add);
//        }
        //mark on 2020-12-19 end

        List<Long> companyIds = Lists.newArrayList();
        List<Long> officeIds = Lists.newArrayList();
        if (user != null && user.getCompany() != null && user.getCompany().getId() >0) {
            companyIds = msSysOfficeService.findIdListById(user.getCompany().getId());
            user.setCompanyIds(companyIds);
        }
        if (user != null && user.getOffice() != null && user.getOffice().getId() >0) {
            officeIds = msSysOfficeService.findIdListById(user.getOffice().getId());
            user.setOfficeIds(officeIds);
        }
        List<User> newUserList = userDao.findListNew(user);

        Set<Long> ids = Sets.newHashSet();
        ids.addAll(companyIds);
        ids.addAll(officeIds);
        for (User entity : newUserList) {
            Optional.ofNullable(entity).map(r->r.getCompany()).map(r->r.getId()).ifPresent(ids::add);
            Optional.ofNullable(entity).map(r->r.getOffice()).map(r->r.getId()).ifPresent(ids::add);
        }

        List<Office> officeList = Lists.newArrayList();
        if (ids.size() >1) {
            officeList = msSysOfficeService.findSpecColumnListByIds(ids.stream().collect(Collectors.toList()));
        } else if (ids.size() == 1){
            Office office = msSysOfficeService.getSpecColumnById(Lists.newArrayList(ids).get(0));
            Optional.ofNullable(office).ifPresent(officeList::add);
        }
        Map<Long, Office> officeMap = ObjectUtils.isEmpty(officeList)?Maps.newHashMap():officeList.stream().collect(Collectors.toMap(r->r.getId(), r->r, (v2,v1)->v1));

        if (!ObjectUtils.isEmpty(newUserList)) {
            for (User newUser : newUserList) {
                if (newUser != null && newUser.getOffice() != null && newUser.getOffice().getId() != null) {
                    Office office = officeMap.get(newUser.getOffice().getId());
                    if (office != null) {
                        newUser.setOffice(office);  //获取 name,parent_id,parent_ids
                    }
                }
            }
        }
        // mark on 2020-12-19 begin
//        msSysOfficeService.compareListOffice("",localOfficeList, officeList, "SystemService.findUser");
        // add on 2020-12-3 end
//        return list;
        // mark on 2020-12-19 end
        return newUserList;
    }

    /**
     * 通过部门ID获取用户列表，仅返回用户id和name（树查询用户时用）
     *
     * @param officeId
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<User> findUserByOfficeId(Long officeId) {
        String key = String.format(RedisConstant.USER_CACHE_LIST_BY_OFFICE_ID, officeId);
        List<User> list = redisUtils.lRange(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key, 0, -1, User.class);
        if (list == null) {
            User user = new User();
            user.setOffice(new Office(officeId));
            list = userDao.findUserByOfficeId(user);
            redisUtils.lPushAll(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key, list);
        }
        return list;
    }

    /**
     * 更新用户所属角色 // add on 2017-4-27
     *
     * @param user
     */
    @Transactional(readOnly = false)
    public void updateUserRole(User user) {
        // Add on 2017-4-27 begin
        // 更新用户与角色关联
        //long timeout = Long.valueOf(Global.getConfig("cache.timeout")) - new Random().nextInt(100);
        Long userId = userDao.getUserRoleByUserId(user.getId());
        if (userId != null) {
            userDao.deleteUserRole(user);
        }
        if (user.getRoleList() != null && user.getRoleList().size() > 0) {
            userDao.insertUserRole(user);
        } else {
            throw new ServiceException(user.getLoginName() + "没有设置角色！");
        }

        redisUtils.remove(USER_CACHE_ID_ + user.getId());
        redisUtils.remove(USER_CACHE_LOGIN_NAME_ + user.getLoginName());
        // Add on 2017-4-27 end
    }

    @Transactional(readOnly = false)
    public void batchSave(User user){
        if (!StringUtils.isBlank(user.getSalesCustomerIds())) {
            String[] str = user.getSalesCustomerIds().split(",");
            List<Long> customerIds = Arrays.stream(str)
                    .map(s -> Long.parseLong(s.trim())).collect(Collectors.toList());
            msCustomerService.updateAuthorizedCustomers(user.getId(), user.getSubFlag(), customerIds);
        }
    }

    @Transactional(readOnly = false)
    public void saveUser(User user,Set<UserRegion> newRegions,Set<UserRegion> removeRegions) {
        boolean isNew = false;
        if (user.getId() == null || user.getId() <= 0) {
            isNew = true;
            user.preInsert();
            userDao.insert(user);
            MSUserUtils.addUserToRedis(user);//user微服务
        } else {
            // 清除原用户机构用户缓存
            //User oldUser = userDao.get(user.getId());
            // 更新用户数据
            user.preUpdate();
            // todo:比对修改内容
            //String json = GsonUtils.difference(oldUser,user, Sets.newHashSet("subFlag","password"));
            //System.out.println("update diff:" + json);
            userDao.update(user);
            msUserService.refreshUserCacheByUserId(user.getId());//user微服务
            // 更新用户与角色关联
            userDao.deleteUserRole(user);
        }
        //region
        userRegionService.saveUserRegions(user.getId(),newRegions,removeRegions);
        //再次移除用户-区域缓存
        userRegionService.removeUserRegionCash(user.getId());

        if (user.getRoleList() != null && user.getRoleList().size() > 0) {
            userDao.insertUserRole(user);
        } else {
            throw new ServiceException(user.getLoginName() + "没有设置角色！");
        }
        //清除旧数据
        //userDao.deleteUserArea(user); //去掉sys_user_area mark on 2020-9-7
        userDao.deleteUserCustomer(user);
        msSysUserCustomerService.deleteByUserId(user.getId());   //sysUserCustomer微服务化
        try {
            MQSysUserCustomerMessage.SysUserCustomerMessage message = MQSysUserCustomerMessage.SysUserCustomerMessage.newBuilder()
                    .setUserId(user.getId())
                    .setSyncType(SyncTypeEnum.DELETE.getValue())
                    .build();
            sysUserCustomerSender.send(message);
        }catch (Exception e){
            log.error("发送消息队列删除表数据库失败.SystemService.saveUser:{}",user.getId(),e.getMessage());
        }
        //new
        if (user.getUserType().equals(User.USER_TYPE_SERVICE)) {
            if ((user.getCustomerList() == null && user.getCustomerList().size() == 0)
                    && (user.getAreaList() == null || user.getAreaList().size() == 0)) {
                throw new ServiceException(user.getLoginName() + "所负责的客服或区域必须设定其中一项！");
            }
            if (user.getAreaList() != null && user.getAreaList().size() > 0) {
                List<Area> lists = user.getAreaList();
                //500一批提交一个语句
                List<List<Area>> parts = Lists.partition(lists, 500);
                parts.stream().forEach(list -> {
                    user.setAreaList(list);
                    //userDao.insertUserArea(user); //去掉sys_user_area
                });
            }
            if (user.getCustomerList() != null && user.getCustomerList().size() > 0) {
                //去重，前端传入有重复
                List<Customer> customers = user.getCustomerList();
                customers = customers.stream().filter(distinctByKey(t -> t.getId())).collect(Collectors.toList());
                user.setCustomerList(customers);
                userDao.insertUserCustomer(user);

                msSysUserCustomerService.batchInsert(user);  // add on 2020-9-12
            }
        }else if (user.getUserType().equals(User.USER_TYPE_INNER)) {
            if (user.getAreaList() != null && user.getAreaList().size() > 0) {
                List<Area> lists = user.getAreaList();
                //500一批提交一个语句
                List<List<Area>> parts = Lists.partition(lists, 500);
                parts.stream().forEach(list -> {
                    user.setAreaList(list);
                    //userDao.insertUserArea(user); // 去掉sys_user_area
                });
            }
        }
        if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()
                || user.getUserType() == User.USER_TYPE_GROUP.intValue()) {
            productCategoryUserMappingService.saveProductCategoryUserMapping(isNew? GlobalMappingSyncTypeEnum.ADD: GlobalMappingSyncTypeEnum.UPDATE,
                    user.getId(), user.getProductCategoryIds());
            //remove from redis hashmap
            if(!isNew) {
                try {
                    redisUtils.hdel(RedisConstant.RedisDBType.REDIS_SYS_DB, RedisConstant.SYS_USER_PRODUCT_CATEGORY, user.getId().toString());
                } catch (Exception e) {
                    log.error("hdel user product category list error:{}", user.getId(), e);
                }
            }
        }
        /*if (user.getUserType() == User.USER_TYPE_SALES.intValue()) {
            if (!StringUtils.isBlank(user.getSalesCustomerIds())) {
                String[] str = user.getSalesCustomerIds().split(",");
                List<Long> customerIds = Arrays.stream(str)
                        .map(s -> Long.parseLong(s.trim())).collect(Collectors.toList());
                msCustomerService.updateAuthorizedCustomers(user.getId(), user.getSubFlag(), customerIds);
            }
        }*/
        // 清除用户缓存
        UserUtils.clearCache(user);
    }

    @Transactional(readOnly = false)
    public void updateUserInfo(User user) {
        user.preUpdate();
        userDao.updateUserInfo(user);
        msUserService.refreshUserCacheByUserId(user.getId());//user微服务
        // 清除用户缓存
        UserUtils.clearCache(user);
    }

    /**
     * 启用停用用户
     * @param userId
     * @param statusFlag
     */
    public void userEnableDisable(Long userId,Integer statusFlag){
        userDao.userEnableDisable(userId,statusFlag);
        msUserService.refreshUserCacheByUserId(userId);//user微服务
    }
    /**
     * 用户修改个人信息
     */
    @Transactional(readOnly = false)
    public void updateMyInfo(User user) {
        user.preUpdate();
        userDao.updateUserInfo(user);
        msUserService.refreshUserCacheByUserId(user.getId());//user微服务
        // 修改缓存
        UserUtils.clearUserInfo(user);
    }

    @Transactional(readOnly = false)
    public void deleteUser(User user) {
        userDao.delete(user);
        // 同步到Activiti
        // 清除用户缓存
        List<Long> productCategoryIds = getAuthorizedProductCategoryIds(user.getId());
        if (productCategoryIds != null && !productCategoryIds.isEmpty()) {
            productCategoryUserMappingService.deleteProductCategoryUserMapping(user.getId());
        }
        UserUtils.clearCache(user);
        Long userId = 0L;
        List<Long> subUserIds = Lists.newArrayList();
        if (user.getUserType().equals(User.USER_TYPE_SALES)){
            try {
                if(user.getSubFlag() == 1){
                    subUserIds.add(user.getId());
                    userSubDao.deleteUserSub(user.getId());//删除之前主管的关联
                }else if(user.getSubFlag() == 3){
                    userId = user.getId();
                    userSubDao.deleteUserUnderling(user.getId());//删除之前主管与下属关联
                }
                MQSysUserSubMessage.SysUserSubMessage message = MQSysUserSubMessage.SysUserSubMessage.newBuilder()
                        .setUserId(userId)
                        .setSyncType(SyncTypeEnum.DELETE.getValue())
                        .setType(User.USER_TYPE_SALES)
                        .addAllSubUserIds(subUserIds)
                        .build();
                sysUserSubSender.send(message);
            }catch (Exception e){
                log.error("发送消息队列删除表数据库失败.SystemService.deleteUser:{}",user.getId(),e.getMessage());
            }
        }

    }

    /**
     * 启用帐号
     *
     * @param user
     */
    @Transactional(readOnly = false)
    public void enableUser(User user) {
        userDao.enableUser(user.getId());
        msUserService.refreshUserCacheByUserId(user.getId());//user微服务
    }

    @Transactional(readOnly = false)
    public void updatePasswordById(Long id, String loginName, String newPassword) {
        User user = new User(id);
        user.setPassword(entryptPassword(newPassword));
        updatePasswordById(user);
        // 清除用户缓存
        user.setLoginName(loginName);
        UserUtils.clearCache(user);
    }


    @Transactional(readOnly = false)
    public void updateUserLoginInfo(User user) {
        // 保存上次登录信息
        user.setOldLoginIp(user.getLoginIp());
        user.setOldLoginDate(user.getLoginDate());
        // 更新本次登录信息
        user.setLoginIp(user.getLoginIp());
        user.setLoginDate(new Date());
        userDao.updateLoginInfo(user);
    }

    /**
     * 生成安全的密码，生成随机的16位salt并经过1024次 sha-1 hash
     */
    public static String entryptPassword(String plainPassword) {
        String plain = Encodes.unescapeHtml(plainPassword);
        byte[] salt = Digests.generateSalt(SALT_SIZE);
        byte[] hashPassword = Digests.sha1(plain.getBytes(), salt, HASH_INTERATIONS);
        return Encodes.encodeHex(salt) + Encodes.encodeHex(hashPassword);
    }

    /**
     * 验证密码
     *
     * @param plainPassword 明文密码
     * @param password      密文密码
     * @return 验证成功返回true
     */
    public static boolean validatePassword(String plainPassword, String password) {
        String plain = Encodes.unescapeHtml(plainPassword);
        byte[] salt = Encodes.decodeHex(password.substring(0, 16));
        byte[] hashPassword = Digests.sha1(plain.getBytes(), salt, HASH_INTERATIONS);
        return password.equals(Encodes.encodeHex(salt) + Encodes.encodeHex(hashPassword));
    }

    /**
     * 获得活动会话
     *
     * @return
     */
    public Collection<Session> getActiveSessions() {
        return sessionDao.getActiveSessions(false);
    }


    /**
     * 按组织id获得管理的人员帐号
     *
     * @param officeId
     * @param userType
     * @return
     */
    public List<User> findOfficeAccountList(Long officeId, Integer userType) {
        return userDao.findOfficeAccountList(officeId, userType);
    }

    /**
     * 按组织id和类型，角色名获得人员帐号
     *
     * @param officeId   组织id
     * @param userType   用户类型
     * @param roleEnName 角色名称
     * @return
     */
    public List<User> findOfficeLeaderList(Long officeId, Integer userType, String roleEnName) {
        return userDao.findOfficeLeaderList(officeId, userType, null, roleEnName);
    }

    /**
     * 按手机号返回帐号列表
     *
     * @param mobile
     */
    public List<User> findByMobile(String mobile, String expectType, Long expectId) {
        if (StringUtils.isBlank(mobile)) {
            return Lists.newArrayList();
        }
        List<Map<String, Object>> maps = userDao.getByMobile(mobile.trim(), expectType, expectId);
        if (maps == null || maps.size() == 0) {
            return Lists.newArrayList();
        } else {
            List<User> users = Lists.newArrayList();
            Long id;
            String name = new String("");
            String m = new String("");
            for (int i = 0, len = maps.size(); i < len; i++) {
                id = (Long) maps.get(i).get("id");
                name = (String) maps.get(i).get("name");
                m = (String) maps.get(i).get("mobile");
                users.add(new User(id, name, m));
            }
            return users;
        }
    }

    //endregion User Service

    //region Role Service

    /**
     * 角色基本信息及菜单
     *
     * @param id
     * @return
     */
    public Role getRole(Long id) {
        //Role role = roleDao.get(id); //mark on 2020-12-19

        // add on 2020-12-2 begin   office微服务化
        Role newRole = roleDao.getNew(id);
        Optional.ofNullable(newRole).ifPresent(x->{
            Office office = msSysOfficeService.getSpecColumnById(Optional.ofNullable(newRole).map(p->p.getOffice()).map(p->p.getId()).orElse(0L));  //仅获取code，name
            x.setOffice(office);
        });
        //msSysOfficeService.compareSingleOffice(String.format("id=%s", id), Optional.ofNullable(role).map(x->x.getOffice()).orElse(null), Optional.ofNullable(newRole).map(x->x.getOffice()).orElse(null), "SystemService.getRole"); //mark on 2020-12-19
        // add on 2020-12-2 end

        //return role;
        return newRole;
    }

    /**
     * 角色基本信息
     *
     * @param id
     * @return
     */
    public Role getRoleById(Long id) {
        //return roleDao.getById(id);
        //Role role = roleDao.getById(id);  //mark on 2020-12-19

        // add on 2020-12-2 begin   office微服务化
        Role newRole = roleDao.getByIdNew(id);
        Optional.ofNullable(newRole).ifPresent(x->{
            Office office = msSysOfficeService.getSpecColumnById(Optional.ofNullable(newRole).map(p->p.getOffice()).map(p->p.getId()).orElse(0L));  //仅获取code，name
            x.setOffice(office);
        });
        //msSysOfficeService.compareSingleOffice(String.format("id=%s", id), Optional.ofNullable(role).map(x->x.getOffice()).orElse(null), Optional.ofNullable(newRole).map(x->x.getOffice()).orElse(null), "SystemService.getRoleById");  //mark on 2020-12-19
        // add on 2020-12-2 end

        //return role;
        return newRole;
    }

    public Role getRoleByName(String name) {
        Role r = new Role();
        r.setName(name);
        //return roleDao.getByName(r);
        //Role role = roleDao.getByName(r);  //mark on 2020-12-19

        // add on 2020-12-2 begin   office微服务化
        Role newRole = roleDao.getByNameNew(r);
        Optional.ofNullable(newRole).ifPresent(x->{
            Office office = msSysOfficeService.getSpecColumnById(Optional.ofNullable(newRole).map(p->p.getOffice()).map(p->p.getId()).orElse(0L));  //仅获取code，name
            x.setOffice(office);
        });
        //msSysOfficeService.compareSingleOffice(String.format("name=%s", name), Optional.ofNullable(role).map(x->x.getOffice()).orElse(null), Optional.ofNullable(newRole).map(x->x.getOffice()).orElse(null), "SystemService.getRoleByName");  //mark on 2020-12-19
        // add on 2020-12-2 end

        //return role;
        return newRole;
    }

    public Role getRoleByEnname(String enname) {
        Role r = new Role();
        r.setEnname(enname);
        //return roleDao.getByEnname(r);

        //Role role = roleDao.getByEnname(r);  //mark on 2020-12-19

        // add on 2020-12-2 begin   office微服务化
        Role newRole = roleDao.getByEnnameNew(r);
        Optional.ofNullable(newRole).ifPresent(x->{
            Office office = msSysOfficeService.getSpecColumnById(Optional.ofNullable(newRole).map(p->p.getOffice()).map(p->p.getId()).orElse(0L));  //仅获取code，name
            x.setOffice(office);
        });
        //msSysOfficeService.compareSingleOffice(String.format("enname=%s", enname), Optional.ofNullable(role).map(x->x.getOffice()).orElse(null), Optional.ofNullable(newRole).map(x->x.getOffice()).orElse(null), "SystemService.getRoleByEnname");
        // add on 2020-12-2 end

        //return role;
        return newRole;
    }

    public List<Role> findRole(Role role) {
        //return roleDao.findList(role);

        //List<Role> roleList = roleDao.findList(role); //mark on 2020-12-19

        // add on 2020-12-2 begin   office微服务化

        // mark on 2020-12-19 begin
//        List<Office> separateOfficeList = Lists.newArrayList();
//        if (!ObjectUtils.isEmpty(roleList)) {
//            //Map<Long, Office> officeMap = roleList.stream().filter(r->r.getOffice() != null).map(r->r.getOffice()).collect(Collectors.toMap(r->r.getId(), r->r, (v1, v2)->v2));
//            //Optional.ofNullable(officeMap).ifPresent(r->separateOfficeList.addAll(r.values()));
//            separateOfficeList = roleList.stream().filter(r->r.getOffice() != null).map(r->r.getOffice()).collect(collectingAndThen(toCollection(()->new TreeSet<>(Comparator.comparingLong(Office::getId))), ArrayList::new));
//        }
        // mark on 2020-12-19 end
        List<Role> newRoleList = roleDao.findListNew(role);
        if (!ObjectUtils.isEmpty(newRoleList)) {
            Set<Long> officeIds = newRoleList.stream().map(r -> r.getOffice().getId()).collect(Collectors.toSet());
            List<Office> officeList = msSysOfficeService.findSpecColumnListByIds(Lists.newArrayList(officeIds));  //仅获取code，name

            Map<Long, Office> officeMap = Maps.newHashMap();
            if (!ObjectUtils.isEmpty(officeList)) {
                officeMap = officeList.stream().collect(Collectors.toMap(r -> r.getId(), Function.identity()));
            }

            for(int i=0; i< newRoleList.size(); i++) {
                Role tempRole = newRoleList.get(i);
                Office office = officeMap.get(tempRole.getOffice().getId());
                if (office != null) {
                    tempRole.setOffice(office);
                }
            }
            try {
                newRoleList = newRoleList.stream().sorted(Comparator.comparing((Role x) -> x.getOffice().getCode()).thenComparing(Role::getName)).collect(Collectors.toList());
            } catch(Exception ex) {
            }

            //msSysOfficeService.compareListOffice(role.toString(), separateOfficeList , officeList, "SystemService.findRole");  //mark on 2020-12-19
        }
        // add on 2020-12-2 end


        //return roleList;  //mark on 2020-12-19
        return newRoleList;
    }

    /**
     * 获得用户的角色列表
     *
     * @param user
     * @return
     */
    public List<Role> findUserRoles(User user) {
        //return roleDao.findList(new Role(user));
        Role initRole = new Role(user);


        // add on 2020-12-2 begin   office微服务化
        // mark on 2020-12-19 begin
//        List<Role> roleList = roleDao.findList(initRole);
//        List<Office> separateOfficeList = Lists.newArrayList();
//        if (!ObjectUtils.isEmpty(roleList)) {
//            separateOfficeList = roleList.stream().filter(r->r.getOffice() != null).map(r->r.getOffice()).collect(collectingAndThen(toCollection(()->new TreeSet<>(Comparator.comparingLong(Office::getId))), ArrayList::new));
//        }
        // mark on 2020-12-19 end
        List<Role> newRoleList = roleDao.findListNew(initRole);
        if (!ObjectUtils.isEmpty(newRoleList)) {
            Set<Long> officeIds = newRoleList.stream().map(r -> r.getOffice().getId()).collect(Collectors.toSet());
            List<Office> officeList = msSysOfficeService.findSpecColumnListByIds(Lists.newArrayList(officeIds));  //仅获取code，name

            Map<Long, Office> officeMap = Maps.newHashMap();
            if (!ObjectUtils.isEmpty(officeList)) {
                officeMap = officeList.stream().collect(Collectors.toMap(r -> r.getId(), Function.identity()));
            }

            for(int i=0; i< newRoleList.size(); i++) {
                Role tempRole = newRoleList.get(i);
                Office office = officeMap.get(tempRole.getOffice().getId());
                if (office != null) {
                    tempRole.setOffice(office);
                }
            }

            try {
                newRoleList = newRoleList.stream().sorted(Comparator.comparing((Role x) -> x.getOffice().getCode()).thenComparing(Role::getName)).collect(Collectors.toList());
            } catch (Exception ex) {
            }

            //msSysOfficeService.compareListOffice(initRole.toString(), separateOfficeList , officeList, "SystemService.findUserRoles");  //mark on 2020-12-19
        }
        // add on 2020-12-2 end

        //return roleList;
        return newRoleList;
    }

    /**
     * 当前用户的角色列表
     *
     * @return
     */
    public List<Role> findAllRole() {
        //return UserUtils.getRoleList();
        return UserUtils.getAllRoleList();
    }

    /**
     * 所有角色
     *
     * @return
     */
    public List<Role> findAllList() {
        // mark on 2020-12-19 begin
//        List<Role> roleList = roleDao.findAllList();
//
//        // add on 2020-12-2 begin   office微服务化
//        List<Office> separateOfficeList = Lists.newArrayList();
//        if (!ObjectUtils.isEmpty(roleList)) {
//            separateOfficeList = roleList.stream().filter(r->r.getOffice() != null).map(r->r.getOffice()).collect(collectingAndThen(toCollection(()->new TreeSet<>(Comparator.comparingLong(Office::getId))), ArrayList::new));
//        }
        // mark on 2020-12-19 end
        List<Role> newRoleList = roleDao.findAllListNew();
        if (!ObjectUtils.isEmpty(newRoleList)) {
            Set<Long> officeIds = newRoleList.stream().map(r -> r.getOffice().getId()).collect(Collectors.toSet());
            List<Office> officeList = msSysOfficeService.findSpecColumnListByIds(Lists.newArrayList(officeIds));  //仅获取code，name

            Map<Long, Office> officeMap = Maps.newHashMap();
            if (!ObjectUtils.isEmpty(officeList)) {
                officeMap = officeList.stream().collect(Collectors.toMap(r -> r.getId(), Function.identity()));
            }

            for(int i=0; i< newRoleList.size(); i++) {
                Role tempRole = newRoleList.get(i);
                Office office = officeMap.get(tempRole.getOffice().getId());
                if (office != null) {
                    tempRole.setOffice(office);
                }
            }

            try {
                newRoleList = newRoleList.stream().sorted(Comparator.comparing((Role x) -> x.getOffice().getCode()).thenComparing(Role::getName)).collect(Collectors.toList());
            } catch (Exception ex) {
            }

            //msSysOfficeService.compareListOffice("", separateOfficeList , officeList, "SystemService.findAllList");  //mark on 2020-12-19
        }
        // add on 2020-12-2 end

        //切换为微服务
        Map<String, Dict> dataScopeMap = MSDictUtils.getDictMap("sys_data_scope");
        //for (Role item : roleList) {   //mark on 2020-12-19
        for (Role item : newRoleList) {  //add on 2020-12-19
            if (item.getDataScope() != null && item.getDataScope() > 0) {
                Dict dataScopeDict = dataScopeMap.get(item.getDataScope().toString());
                item.setDataScopeName(dataScopeDict != null ? dataScopeDict.getLabel() : "");
            }
        }
        return newRoleList;
    }

    @Transactional(readOnly = false)
    public void saveRole(Role role) {
//		if (StringUtils.isBlank(role.getId())) {
        if (role.getId() == null || role.getId() <= 0) {
            role.preInsert();
            roleDao.insert(role);
            // 同步到Activiti
        } else {
            role.preUpdate();
            roleDao.update(role);
        }
        // 更新角色与菜单关联
        roleDao.deleteRoleMenu(role);
        if (role.getMenuList().size() > 0) {
            roleDao.insertRoleMenu(role);
        }
        // 更新角色与部门关联
        roleDao.deleteRoleOffice(role);
        if (role.getOfficeList().size() > 0) {
            roleDao.insertRoleOffice(role);
        }
        // 同步到Activiti
//		UserUtils.removeCache(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB,RedisConstant);
        redisUtils.remove(RedisConstant.RedisDBType.REDIS_SYS_DB, RedisConstant.SYS_MENU_ALL_LIST);
        redisUtils.remove(RedisConstant.RedisDBType.REDIS_SYS_DB,RedisConstant.SYS_ROLE_ALL_LIST);
//		// 清除权限缓存
//		systemRealm.clearAllCachedAuthorizationInfo();
    }

    /**
     * 查询分页数据
     *
     * @param page 分页对象
     * @param role
     * @return
     */
    public Page<Long> getUserIdListOfRole(Page<Role> page, Role role) {
        role.setPage(page);
        Page<Long> rtnPage = new Page<>();
        rtnPage.setList(roleDao.getUserIdList(role));
        rtnPage.setPageNo(page.getPageNo());
        rtnPage.setPageSize(page.getPageSize());
        rtnPage.setCount(page.getCount());
        rtnPage.setOrderBy(page.getOrderBy());
        return rtnPage;
    }

    @Transactional(readOnly = false)
    public void deleteRole(Role role) {
        roleDao.delete(role);
        // 同步到Activiti
        redisUtils.remove(RedisConstant.RedisDBType.REDIS_SYS_DB, RedisConstant.SYS_MENU_ALL_LIST);
        redisUtils.remove(RedisConstant.RedisDBType.REDIS_SYS_DB,RedisConstant.SYS_ROLE_ALL_LIST);
    }

    @Transactional(readOnly = false)
    public Boolean outUserInRole(Role role, User user) {
        List<Role> roles = user.getRoleList();
        for (Role e : roles) {
            if (e.getId().equals(role.getId())) {
                roles.remove(e);
                updateUserRole(user);  //add on 2017-4-27
                return true;
            }
        }
        return false;
    }

    @Transactional(readOnly = false)
    public User assignUserToRole(Role role, User user) {
        if (user == null) {
            return null;
        }
        List<Long> roleIds = user.getRoleIdList();
        if (roleIds.contains(role.getId())) {
            return null;
        }
        user.getRoleList().add(role);
        updateUserRole(user);  //add on 2017-4-27

        return user;
    }

    //endregion Role Service

    //region Menu Service

    public Menu getMenu(Long id) {
        return menuDao.get(id);
    }

    public List<Menu> findAllMenu() {
        return UserUtils.getAllMenuList();
    }

    @Transactional(readOnly = false)
    public void saveMenu(Menu menu) {

        // 获取父节点实体
        menu.setParent(this.getMenu(menu.getParent().getId()));

        // 获取修改前的parentIds，用于更新子节点的parentIds
        String oldParentIds = menu.getParentIds();

        // 设置新的父节点串
        menu.setParentIds(menu.getParent().getParentIds() + menu.getParent().getId() + ",");

        // 保存或更新实体
        if (menu.getId() == null || menu.getId() <= 0) {
            menu.preInsert();
            menuDao.insert(menu);
        } else {
            menu.preUpdate();
            menuDao.update(menu);
        }

        // 更新子节点 parentIds
        Menu m = new Menu();
        m.setParentIds("%," + menu.getId() + ",%");
        List<Menu> list = menuDao.findByParentIdsLike(m);
        for (Menu e : list) {
            e.setParentIds(e.getParentIds().replace(oldParentIds, menu.getParentIds()));
            menuDao.updateParentIds(e);
        }
        // 清除用户菜单缓存
        UserUtils.removeCache(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, String.format(RedisConstant.SHIRO_USER_MENU, "") + "*");
        UserUtils.removeCache(RedisConstant.RedisDBType.REDIS_SYS_DB, RedisConstant.SYS_MENU_ALL_LIST);
        // 清除日志相关缓存
        UserUtils.removeCache(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, LogUtils.CACHE_MENU_NAME_PATH_MAP);
    }

    @Transactional(readOnly = false)
    public void updateMenuSort(List<Menu> menus) {
        if (menus == null || menus.size() == 0) return;
        int size = menus.size();
        Menu menu;
        for (int i = 0; i < size; i++) {
            menu = menus.get(i);
            menuDao.updateSort(menu);
        }
        // 清除用户菜单缓存
        UserUtils.removeCache(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, RedisConstant.SHIRO_USER_MENU + "*");
        UserUtils.removeCache(RedisConstant.RedisDBType.REDIS_SYS_DB, RedisConstant.SYS_MENU_ALL_LIST);
        // 清除日志相关缓存
        UserUtils.removeCache(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, LogUtils.CACHE_MENU_NAME_PATH_MAP);
    }

    @Transactional(readOnly = false)
    public void deleteMenu(Menu menu) {
        menuDao.delete(menu);
        // 清除用户菜单缓存
        UserUtils.removeCache(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, RedisConstant.SHIRO_USER_MENU + "*");
        UserUtils.removeCache(RedisConstant.RedisDBType.REDIS_SYS_DB, RedisConstant.SYS_MENU_ALL_LIST);
        // 清除日志相关缓存
        UserUtils.removeCache(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, LogUtils.CACHE_MENU_NAME_PATH_MAP);
    }

    //endregion Menu Service

    /**
     * 获取Key加载信息
     */
    public static boolean printKeyLoadMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("\r\n======================================================================\r\n");
        sb.append("\r\n    欢迎使用 " + Global.getConfig("productName") + "\r\n");
        sb.append("\r\n======================================================================\r\n");
        System.out.println(sb.toString());
        return true;
    }

    //region api functions

    /**
     * 登出
     *
     * @param userId
     * @return
     */
    public RestResult<Object> logout(Long userId) {
        String key = String.format(RedisConstant.APP_SESSION, userId);
        if (redisUtils.exists(RedisConstant.RedisDBType.REDIS_NEW_APP_DB, key)) {
            redisUtils.remove(RedisConstant.RedisDBType.REDIS_NEW_APP_DB, key);
        }
        return RestResultGenerator.success();
    }

    /**
     * 重置密码
     *
     * @param resetPassword
     * @return
     */
    public RestResult<Object> resetPassword(RestResetPassword resetPassword) {
        Long id = userDao.getIdByMobile(resetPassword.getPhone());
        if (id == null) {
            return RestResultGenerator.custom(ErrorCode.MEMBER_PHONE_NOT_EXIST.code, ErrorCode.MEMBER_PHONE_NOT_EXIST.message);
        }
        String verifyCodeCacheKey = String.format(VERCODE_KEY, 1, resetPassword.getPhone());
        if (redisUtils.exists(REDIS_TEMP_DB, verifyCodeCacheKey)) {
            String verifyCode = (String) redisUtils.get(REDIS_TEMP_DB, verifyCodeCacheKey, String.class);
            if (!verifyCode.equals(resetPassword.getCode())) {
                return RestResultGenerator.custom(ErrorCode.MEMBER_VERIFYCODE_FAIL.code, ErrorCode.MEMBER_VERIFYCODE_FAIL.message);
            }
        } else {
            return RestResultGenerator.custom(ErrorCode.MEMBER_VERIFYCODE_TIMEOUT.code, ErrorCode.MEMBER_VERIFYCODE_TIMEOUT.message);
        }
        User user = new User(id);
        user.setPassword(entryptPassword(resetPassword.getNewPwd()));
        updatePasswordById(user);
        redisUtils.remove(REDIS_TEMP_DB, verifyCodeCacheKey);
        return RestResultGenerator.success();
    }

    /**
     * 修改密码
     *
     * @param userId
     * @param restUpdatePassword
     * @return
     */
    public RestResult<Object> updatePassword(Long userId, RestUpdatePassword restUpdatePassword) {
        String oldPwd = userDao.getPasswordById(userId);
        if (oldPwd == null || oldPwd.length() == 0) {
            return RestResultGenerator.custom(ErrorCode.MEMBER_ENGINEER_NO_EXSIT.code, ErrorCode.MEMBER_ENGINEER_NO_EXSIT.message);
        }
        if (!SystemService.validatePassword(restUpdatePassword.getOldPwd(), oldPwd)) {
            return RestResultGenerator.custom(ErrorCode.MEMBER_ACCOUNT_NOT_EXSIT_OR_PASS_WRONG.code, ErrorCode.MEMBER_ACCOUNT_NOT_EXSIT_OR_PASS_WRONG.message);
        }
        User user = new User(userId);
        user.setPassword(entryptPassword(restUpdatePassword.getNewPwd()));
        updatePasswordById(user);
        return RestResultGenerator.success();
    }
    //endregion api functions

    /**
     * 修改密码的辅助方法
     * 修改密码也同时更新updateDate —— 2018-3-26 10:02
     *
     * @param user
     * @return
     */
    public int updatePasswordById(User user) {
        user.setUpdateDate(new Date());
        return userDao.updatePasswordById(user);
    }

    /**
     * 根据userid或customerId获取customerId列表  // add 2019-7-23
     * @param paramMap
     * @return
     * CustomerId
     */
    public List<Long> findCustomerIdList(Map<String, Object> paramMap){
        //return userDao.findCustomerIdList(paramMap);
        List<Long> customerIds = userDao.findCustomerIdList(paramMap);
        // add on 2020-9-12 begin
        List<Long> customerIdsFromMS = msSysUserCustomerService.findCustomerIdList(paramMap);
        msSysUserCustomerService.compareCollectionData(customerIds, customerIdsFromMS, paramMap.toString(), "systemService.findCustomerIdList");
        // add on 2020-9-12 end
        return customerIds;
    }

    /**
     * 根据客服id获取vip客户id列表
     */
    public List<Long> findVipCustomerIdListByKefu(Long kefuId){
        if(kefuId == null || kefuId <= 0L){
            return Lists.newArrayList();
        }
        //return userDao.findVipCustomerIdListByKefu(kefuId);
        List<Long> customerIds = userDao.findVipCustomerIdListByKefu(kefuId);

        //  add on 2020-9-12 begin
        Map<String,Object> paramMap = Maps.newHashMap();
        paramMap.put("userId",kefuId);
        List<Long> customerIdsFromMS = msSysUserCustomerService.findCustomerIdList(paramMap);
        msSysUserCustomerService.compareCollectionData(customerIds, customerIdsFromMS, kefuId+"", "SystemService.findVipCustomerIdListByKefu");
        //  add on 2020-9-12 end
        return customerIds;
    }

    /**
     * 获取用户被授权的产品类目的id列表
     */
    public List<Long> getAuthorizedProductCategoryIds(Long userId) {
        if (userId == null || userId <= 0) {
            return Lists.newArrayListWithCapacity(0);
        }
        //from cache
        if(redisUtils.hexist(RedisConstant.RedisDBType.REDIS_SYS_DB,RedisConstant.SYS_USER_PRODUCT_CATEGORY,userId.toString())){
            return redisUtils.hGetList(RedisConstant.RedisDBType.REDIS_SYS_DB, RedisConstant.SYS_USER_PRODUCT_CATEGORY, userId.toString(), Long[].class);
        }
        List<Long> result = userDao.getAuthorizedProductCategoryIds(userId);
        if(result == null){
            return Lists.newArrayListWithCapacity(0);
        }
        else if(result.isEmpty()){
            return result;
        }
        //write cache
        try {
            redisUtils.hmSet(RedisConstant.RedisDBType.REDIS_SYS_DB, RedisConstant.SYS_USER_PRODUCT_CATEGORY, userId.toString(), result, -1);
        }catch (Exception e){
            log.error("redis hmSet user product category list error:{}",userId,e);
        }
        return result;
    }

    /**
     * 查询所有为主账号的安维人员列表信息  // add on 2019-11-8
     * @return
     */
    public List<User> findEngineerAccountList(List<Long> engineerIds,Integer subFlag) {
        return userDao.findEngineerAccountsList(engineerIds, subFlag);
    }

    /**
     * read customerProductCategories with to DB
     * then write cache
     * @param userId
     * @return
     */
    public List<Long> getProductCategoryIds(Long userId) {
        if (userId == null || userId <= 0) {
            return Lists.newArrayListWithCapacity(0);
        }
        List<Long> result = userDao.getAuthorizedProductCategoryIds(userId);
        if (result == null) {
            return Lists.newArrayListWithCapacity(0);
        } else if (result.isEmpty()) {
            return result;
        }
        //write cache
        try {
            redisUtils.hmSet(RedisConstant.RedisDBType.REDIS_SYS_DB, RedisConstant.SYS_USER_PRODUCT_CATEGORY, userId.toString(), result, -1);
        } catch (Exception e){
            log.error("redis hmSet user product category list error:{}",userId,e);
        }
        return result;
    }
}
