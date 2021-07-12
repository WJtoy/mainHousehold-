/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.wolfking.jeesite.modules.sys.utils;

import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.reflect.TypeToken;
import com.kkl.kklplus.entity.md.GlobalMappingSalesSubFlagEnum;
import com.wolfking.jeesite.common.config.Global;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.config.redis.RedisTuple;
import com.wolfking.jeesite.common.security.Digests;
import com.wolfking.jeesite.common.security.shiro.cache.RedisValueCache;
import com.wolfking.jeesite.common.service.BaseService;
import com.wolfking.jeesite.common.utils.GsonUtils;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.wolfking.jeesite.common.utils.SpringContextHolder;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.api.config.Constant;
import com.wolfking.jeesite.modules.api.util.JwtUtil;
import com.wolfking.jeesite.modules.md.dao.CustomerAccountProfileDao;
import com.wolfking.jeesite.modules.md.dao.CustomerDao;
import com.wolfking.jeesite.modules.md.dao.ServicePointDao;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.CustomerAccountProfile;
import com.wolfking.jeesite.modules.md.entity.CustomerShop;
import com.wolfking.jeesite.modules.md.entity.Engineer;
import com.wolfking.jeesite.modules.md.service.CustomerService;
import com.wolfking.jeesite.modules.md.service.ServicePointService;
import com.wolfking.jeesite.modules.sys.dao.*;
import com.wolfking.jeesite.modules.sys.entity.*;
import com.wolfking.jeesite.modules.sys.security.SystemAuthorizingRealm.Principal;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.service.SystemService;
import com.wolfking.jeesite.modules.sys.service.UserAttributesService;
import com.wolfking.jeesite.modules.sys.service.UserSubService;
import com.wolfking.jeesite.ms.b2bcenter.md.utils.B2BMDUtils;
import com.wolfking.jeesite.ms.providermd.service.MSCustomerAccountProfileService;
import com.wolfking.jeesite.ms.providermd.service.MSCustomerService;
import com.wolfking.jeesite.ms.providersys.service.MSSysAreaService;
import com.wolfking.jeesite.ms.providersys.service.MSSysOfficeService;
import com.wolfking.jeesite.ms.providersys.service.MSSysUserCustomerService;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import com.wolfking.jeesite.ms.utils.MSUserUtils;
import io.jsonwebtoken.Claims;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.lang3.RandomUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.UnavailableSecurityManagerException;
import org.apache.shiro.session.InvalidSessionException;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import sun.net.www.content.text.plain;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.wolfking.jeesite.modules.api.config.Constant.JWT_IM_ID;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;

/**
 * 用户工具类
 * @author ThinkGem
 * @version 2013-12-05
 */
public class UserUtils {

	private static UserDao userDao = SpringContextHolder.getBean(UserDao.class);
	private static RoleDao roleDao = SpringContextHolder.getBean(RoleDao.class);
	private static MenuDao menuDao = SpringContextHolder.getBean(MenuDao.class);
	private static MSCustomerService msCustomerService = SpringContextHolder.getBean(MSCustomerService.class);
	private static CustomerService customerService = SpringContextHolder.getBean(CustomerService.class);
	private static MSCustomerAccountProfileService msCustomerAccountProfileService = SpringContextHolder.getBean(MSCustomerAccountProfileService.class);
	private static ServicePointService servicePointService = SpringContextHolder.getBean(ServicePointService.class); // add on 2019-9-16
	private static AreaService areaService = SpringContextHolder.getBean(AreaService.class);   // add on 2020-7-31
	private static MSSysAreaService msSysAreaService = SpringContextHolder.getBean(MSSysAreaService.class);   //add on 2020-7-31
	private static MSSysUserCustomerService  msSysUserCustomerService = SpringContextHolder.getBean(MSSysUserCustomerService.class); //add on 2020-9-12
	private static MSSysOfficeService msSysOfficeService = SpringContextHolder.getBean(MSSysOfficeService.class);   //add on 2020-11-28
	private static SystemService sytemService = SpringContextHolder.getBean(SystemService.class);
	private static UserSubService userSubService = SpringContextHolder.getBean(UserSubService.class);
	private static UserAttributesService userAttributesService = SpringContextHolder.getBean(UserAttributesService.class);

	@Value("${cache.timeout}")
	private long timeout;

	private static RedisUtils redisUtils = SpringContextHolder.getBean(RedisUtils.class);

	/**
	 * 根据ID获取用户
	 * @param id
	 * @return 取不到返回null
	 */
	public static User get(Long id){
		return get(id,null,null);
	}

	/**
	 * 根据ID获取用户
	 * @param id
	 * @return 取不到返回null
	 */
	public static User get(Long id,String session){
		return get(id,session,null);
	}

	/**
	 * 根据ID获取用户
	 * @param id
	 * @param session session
	 * @param syncCache 同步缓存标记,判断缓存中key是否存在，不存在的重新读取并更新缓存
	 * @return 取不到返回null
	 */
	public static User get(Long id, String session, Boolean syncCache){
		User user = (User)redisUtils.get(String.format(RedisConstant.SYS_USER_ID,id),User.class);
		if (user ==  null){
			//user = userDao.get(id);   //mark on 2020-12-4
			user = sytemService.getUserFromDb(id); //add on 2020-12-4
			if (user == null){
				return null;
			}
			if(user.getDelFlag() == User.DEL_FLAG_DELETE){
				return null;
			}
			user.setLoginDate(new Date());
			long timeout = Long.valueOf(Global.getConfig("cache.timeout"))- RandomUtils.nextInt(0,60*30);
			loadUserInfo(user,session,timeout);
		}else if(syncCache != null && syncCache == true){
			long timeout = Long.valueOf(Global.getConfig("cache.timeout"))- RandomUtils.nextInt(0,60*30);
			loadUserInfo(user,session,timeout);
		}
		return user;
	}

	/**
	 * 根据ID获取用户帐号信息(不读取相关客户，安维信息)
	 * @param id
	 * @return 取不到返回null
	 */
	public static User getAcount(Long id){
		User user = (User)redisUtils.get(String.format(RedisConstant.SYS_USER_ID,id),User.class);
		if (user ==  null){
			user = userDao.getBaseInfo(id);
			if (user == null){
				return null;
			}
			if(user.getDelFlag() == User.DEL_FLAG_DELETE){
				return null;
			}
		}
		return user;
	}

	/**
	 * 根据登录名获取用户
	 * 先取缓存，再取数据库
	 * @param loginName
	 * @return 取不到返回null
	 */
	public static User getByLoginName(String loginName){
		User user = null;
		Long id = (Long)redisUtils.get(String.format(RedisConstant.SYS_USER_LOGINNAME,loginName),Long.class);
		if(id!=null){
			user = (User)redisUtils.get(String.format(RedisConstant.SYS_USER_ID,id),User.class);
		}
		if (user == null){
			//user = userDao.getByLoginName(new User(null, loginName));  // mark on 2020-12-19

			// add on 2020-12-3 begin  office去微服务化
			User newUser = userDao.getByLoginNameNew(new User(null, loginName));
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
			user = newUser;

			if (user == null){
				return null;
			}
		}
		return user;
	}

	/**
	 * 根据登录名获取APP用户
	 * @param loginName
	 * @return
	 */
	public static User getAppUserByLoginName(String loginName){
		// add on 2020-12-3 begin  office去微服务化
		User newUser = userDao.getAppUserByLoginNameNew(new User(null, loginName));
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
		Map<Long, Office> officeMap;
		officeMap = ObjectUtils.isEmpty(officeList)?Maps.newHashMap():officeList.stream().collect(Collectors.toMap(r->r.getId(), r->r, (v2,v1)->v1));
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

		return newUser;
	}

	/**
	 * 装载用户信息
	 * 根据用户类型装载区域，客户等到缓存
	 * @param user 用户实例
	 * @param session session id,登录时传入值不为null
	 * @param timeout 超时时间
	 */
	public static void loadUserInfo(User user,String session,Long timeout){
		if(user == null || user.getId()==null){
			return;
		}
		Long mytimeout = timeout;
		if( mytimeout == null) {
			mytimeout =  Long.valueOf(Global.getConfig("cache.timeout")) - RandomUtils.nextInt(0,60*30);
		}
		//厂商帐号
		if (user.isCustomer() && user.getCustomerAccountProfile().getId()!= null && user.getCustomerAccountProfile().getId()>0 && (user.getCustomerAccountProfile().getCustomer() == null || user.getCustomerAccountProfile().getCustomer().getId() == null) ){
			CustomerAccountProfile customerAccountProfile = msCustomerAccountProfileService.getById(user.getCustomerAccountProfile().getId());   // add on 2019-7-29 调用微服务
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
			if (sales != null){
				customerAccountProfile.getCustomer().getSales().setName(sales.getName());
				customerAccountProfile.getCustomer().getSales().setMobile(sales.getMobile());
				customerAccountProfile.getCustomer().getSales().setQq(sales.getQq());
			}
			user.setCustomerAccountProfile(customerAccountProfile);

			// 获取客户的店铺列表
			UserAttributes userAttributes = userAttributesService.getUserAttributesList(user.getId(), UserAttributesEnum.CUSTOMERSHOP.getValue());
			if (userAttributes != null) {
				if (StringUtils.isNotBlank(userAttributes.getUserAttributes())) {
					Map<Integer,String> map = user.getAttributesMap();
					if(map == null){
						map = Maps.newHashMap();
					}
					map.put(UserAttributesEnum.CUSTOMERSHOP.getValue(), userAttributes.getUserAttributes());
					user.setAttributesMap(map);
				}
			}
		}
		//roles
		if(user.getRoleList()==null || user.getRoleList().size()==0) {
			List<Role> roles = roleDao.getUserRoles(user.getId());
			user.setRoleList(roles);
		}

		String customerKey = String.format(RedisConstant.SHIRO_KEFU_CUSTOMER,user.getId());
		// 客服
		if (user.isKefu()) {
			java.util.Map<String, Object> paramMap;
			//area
			String arerakey = String.format(RedisConstant.SHIRO_KEFU_AREA,user.getId());
			if(!redisUtils.exists(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB,arerakey)) {
				paramMap = Maps.newHashMap();
				paramMap.put("userId", user.getId());
				List<Area> areas = areaService.findListByUserIdOrAreaId(paramMap);  // add on 2020-7-30
				redisUtils.setEX(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, arerakey, areas, mytimeout);
			}

			//vip客服
			if(user.getSubFlag() == KefuTypeEnum.VIPKefu.getCode()){
				if(!redisUtils.exists(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB,customerKey)) {
					List<Customer> customers = customerService.findVipListByKefu(user.getId());
					Set<RedisZSetCommands.Tuple> sets;
					if (customers != null && customers.size() > 0) {
						sets = customers.stream()
								.map(t -> new RedisTuple(redisUtils.gsonRedisSerializer.serialize(t), t.getId().doubleValue()))
								.collect(Collectors.toSet());
						redisUtils.zAdd(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, customerKey, sets, 0);//不过期
						Set<Long> customerIds = customers.stream()
								.map(t -> t.getId())
								.collect(Collectors.toSet());
						user.setCustomerIds(customerIds);
					}
				}
			}
		}else if(user.isSaleman()){
			//customers
			if(!redisUtils.exists(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB,customerKey)) {
				List<Customer> customers = Lists.newArrayList();
				if(user.isMerchandiser()) {
					customers = msCustomerService.findListByMerchandiserId(user.getId());//跟单 2020-03-23 Ryan
				} else if (user.isSalesPerson()) {
					customers = msCustomerService.findListBySalesId(user.getId().intValue());  // add on 2019-7-20
				} else if (user.isSalesManager()) {  // 业务主管
					//1. 先获取客户列表列表
					List<Long> customerIdList = userSubService.findCustomerIdListByUserId(user.getId(), user.getUserType());
					//2.去掉重复的客户id
					List<Long> uniqueCustomerIdList = Optional.ofNullable(customerIdList).orElse(Collections.emptyList()).stream().distinct().collect(Collectors.toList());
					if (!uniqueCustomerIdList.isEmpty()) {
						customers = customerService.findIdAndNameListByIds(uniqueCustomerIdList);
					}
				}
				Set<RedisZSetCommands.Tuple> sets = Sets.newConcurrentHashSet();
				if (!CollectionUtils.isEmpty(customers)) {
					// add on 2019-7-20 begin
					customers.stream().forEach(r->{
						r.setSales(user);
					});
					// add on 2019-7-20 end
					sets = customers.stream()
							.map(t -> new RedisTuple(redisUtils.gsonRedisSerializer.serialize(t), t.getId().doubleValue()))
							.collect(Collectors.toSet());
					redisUtils.zAdd(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, customerKey, sets, 0);//不过期
					Set<Long> customerIds = customers.stream()
							.map(t -> t.getId())
							.collect(Collectors.toSet());
					user.setCustomerIds(customerIds);
				}
			}
		}else if(user.isEngineer()){
			// add on 2021-1-8 begin
			// 如果是网点信息员,加载信息员区域:
			if (user.getSubFlag().equals(2)) { //subFlag: 0 主账号，1 子账号，2 信息员
				java.util.Map<String, Object> paramMap;
				//area
				String arerakey = String.format(RedisConstant.SHIRO_KEFU_AREA, user.getId());
				if (!redisUtils.exists(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, arerakey)) {
					paramMap = Maps.newHashMap();
					paramMap.put("userId", user.getId());
					List<Area> areas = areaService.findListByUserIdOrAreaId(paramMap);
					redisUtils.setEX(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, arerakey, areas, mytimeout);
				}
			}
			// add on 2021-1-8 end

			// add on 2019-9-16 begin
			Engineer e = servicePointService.getEngineer(user.getEngineerId());
			// add on 2019-9-16 end

			if(e != null && e.getServicePoint() != null){
				user.setCompany(new Office(e.getServicePoint().getId(),e.getServicePoint().getName()));
			}
		}else if(user.getUserType() == User.USER_TYPE_INNER){ //内部帐号
			java.util.Map<String, Object> paramMap;
			//area
			String arerakey = String.format(RedisConstant.SHIRO_KEFU_AREA,user.getId());
			if(!redisUtils.exists(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB,arerakey)) {
				paramMap = Maps.newHashMap();
				paramMap.put("userId", user.getId());
				List<Area> areas = areaService.findListByUserIdOrAreaId(paramMap);  //add on 2020-7-30
				redisUtils.setEX(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, arerakey, areas, mytimeout);
			}
		}
		if(session != null) {
			//user session <-> id
			String key = String.format(RedisConstant.SHIRO_USER_SESSION, user.getId());
			redisUtils.set(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key, session, RedisValueCache.globExpire*RandomUtils.nextInt(60,120));
			//redisUtils.set(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key, session, 60 * RedisValueCache.globExpire);
			//user:name:*
			redisUtils.set(String.format(RedisConstant.SYS_USER_LOGINNAME,user.getLoginName()), user.getId(),mytimeout);//存储用户id
			redisUtils.set(String.format(RedisConstant.SYS_USER_ID,user.getId()),user,mytimeout);
		}
	}


	/**
	 * 清除当前用户缓存
	 */
	public static void clearCache(){
		User user = getUser();
		if(user == null) return;
		UserUtils.clearCache(getUser());
	}

	/**
	 * 清除指定用户缓存
	 * @param user
	 */
	public static void clearCache(User user){

		if(user == null || user.getId()==null){
			return;
		}
		Long userId = user.getId();
		try {
			List<String> list = Lists.newArrayList();
			String key = new String("");
			key = String.format(RedisConstant.SHIRO_USER_SESSION,userId);
			list.add(key);
			//session
			String session = (String)redisUtils.get(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB,key,String.class);
			if(StringUtils.isNoneBlank(session)){
				list.add(String.format("shiro-cache:activeSessionsCache:%s",session));
			}
			list.add(String.format("shiro-cache:com.wolfking.jeesite.modules.sys.security.SystemAuthorizingRealm.authorizationCache:%s",userId));
			list.add(String.format(RedisConstant.SHIRO_USER_MENU,userId));
			list.add(String.format(RedisConstant.USER_CACHE_LIST_BY_OFFICE_ID,userId));
			list.add(String.format(RedisConstant.SYS_USER_ID,userId));
			list.add(String.format(RedisConstant.SHIRO_USER_ROLE,userId));
			list.add(String.format(RedisConstant.SYS_USER_LOGINNAME,user.getLoginName()));
			list.add(String.format(RedisConstant.SYS_USER_LOGINNAME,user.getOldLoginName()));
			//客户
			list.add(String.format(RedisConstant.SHIRO_KEFU_CUSTOMER,userId));
			//区域
			list.add(String.format(RedisConstant.SHIRO_KEFU_AREA,userId));
			//if(user.isKefu()){
			//	//客服-区域
			//	list.add(String.format(RedisConstant.SHIRO_KEFU_AREA,userId));
			//}
			redisUtils.remove(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, list.toArray(new String[]{}));
		}catch (Exception e){
			try{
				redisUtils.remove(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB,String.format("shiro-cache:com.wolfking.jeesite.modules.sys.security.SystemAuthorizingRealm.authorizationCache:%s",userId));
			}catch (Exception e1){

			}
		}
	}

	/**
	 * 只清除用户基本信息缓存
	 * @param user
     */
	public static void clearUserInfo(User user){
		redisUtils.remove(String.format(RedisConstant.SYS_USER_ID,user.getId()),
				String.format(RedisConstant.SYS_USER_LOGINNAME,user.getLoginName())
				);
	}

	/**
	 * 获取当前用户
	 * @return 取不到返回 new User()
	 */
	public static User getUser(){
		Principal principal = getPrincipal();
		if (principal!=null){
			User user = get(Long.valueOf(principal.getId()),principal.getSessionid());
			if (user != null){
				//if(user.isCustomer() && user.getCustomerAccountProfile()==null){
				//	user.setCustomerAccountProfile(customerAccountProfileDao.getByUserId(user.getId()));
				//}
				return user;
			}
			return new User();
		}
		// 如果没有登录，则返回实例化空的User对象。
		return new User();
	}

	/**
	 * 获取当前用户,并同步缓存内容
	 * @return 取不到返回 new User()
	 */
	public static User reGetUser(){
		Principal principal = getPrincipal();
		if (principal!=null){
			User user = get(Long.valueOf(principal.getId()),principal.getSessionid(),true);
			if (user != null){
				return user;
			}
			return new User();
		}
		// 如果没有登录，则返回实例化空的User对象。
		return new User();
	}

	/**
	 * 业务员列表
	 * @return
	 */
	public static List<User> getSaleList(){
		User user = getUser();
		if(user != null && user.isSaleman() && user.getSubFlag().equals(GlobalMappingSalesSubFlagEnum.SALES.getValue())) {
			return Lists.newArrayList(user);
		}
		//return userDao.getSaleList(GlobalMappingSalesSubFlagEnum.SALES.getValue(),0);
		List<Integer> subFlagList = Lists.newArrayList();
		subFlagList.add(GlobalMappingSalesSubFlagEnum.SALES.getValue());
		subFlagList.add(GlobalMappingSalesSubFlagEnum.MANAGER.getValue());
		return userDao.getSaleListBySubFlagList(subFlagList, 0);
	}

	/**
	 * 跟单员列表  //add on 2019-11-15
	 * @return
	 */
	public static List<User> getMerchandiserList() {
		User user = getUser();
		if(user != null && user.isSaleman() && user.getSubFlag().equals(GlobalMappingSalesSubFlagEnum.MERCHANDISER.getValue())){
			return Lists.newArrayList(user);
		}
		return userDao.getSaleList(GlobalMappingSalesSubFlagEnum.MERCHANDISER.getValue(),0);
	}

	/**
	 * 按部门获得所有帐号id
	 * @param officeId	部门
	 * @param userType  用户类型
	 * @return
	 */
	public static List<String> getOfficeAccountIds(Long officeId,Integer userType){
		String key = String.format(RedisConstant.SYS_OFFICE_USER,officeId);
		List<User> users;
		if(redisUtils.exists(RedisConstant.RedisDBType.REDIS_SYS_DB,key)){
			Map<String, byte[]> maps = redisUtils.hGetAll(RedisConstant.RedisDBType.REDIS_SYS_DB,key);
			return maps.keySet().stream().filter(t-> !t.equalsIgnoreCase("leader"))
					.collect(Collectors.toList());
		}else{
			users = userDao.findOfficeAccountList(officeId,userType);
			Map<String, Object> maps = users.stream().collect(Collectors.toMap(
					e->e.getId().toString(),
					e->e.getId().toString()
			));
			String roleEnName = new String("");
			switch (userType){
				case 2:
					roleEnName = "kefuleader";
					break;
				case 7:
					roleEnName = "salesleader";
					break;
				default:
					break;
			}
			users = userDao.findOfficeLeaderList(officeId,userType,null,roleEnName);
			if(users != null && users.size()>0) {
				maps.put("leader",users.stream().map(t->t.getId().toString()).collect(Collectors.joining(",")));
			}
			redisUtils.hmSetAll(RedisConstant.RedisDBType.REDIS_SYS_DB,key,maps,0l);
			return users.stream().map(t->t.getId().toString()).collect(Collectors.toList());
		}
	}

	/**
	 * 按客服获得所有主管帐号id
	 * @param userId  用户类型
	 * @return
	 */
	public static List<String> getOfficeLeaderIds(Long userId){
		User user = get(userId,null);
		if(user == null){
			return Lists.newArrayList();
		}
		return getOfficeLeaderIds(user.getOfficeId(),user.getUserType());
	}

	/**
	 * 按部门获得所有主管帐号id
	 * @param officeId	部门
	 * @param userType  用户类型
	 * @return
	 */
	public static List<String> getOfficeLeaderIds(Long officeId,Integer userType){
		String key = String.format(RedisConstant.SYS_OFFICE_USER,officeId);
		List<User> users;
		if(redisUtils.exists(RedisConstant.RedisDBType.REDIS_SYS_DB,key)){
			String leader = redisUtils.hGet(RedisConstant.RedisDBType.REDIS_SYS_DB,key,"leader",String.class);
			if(StringUtils.isBlank(leader)){
				return Lists.newArrayList();
			}else{
				return Arrays.stream(leader.split(",")).collect(Collectors.toList());
			}
		}else{
			users = userDao.findOfficeAccountList(officeId,userType);
			Map<String, Object> maps = users.stream().collect(Collectors.toMap(
					e->e.getId().toString(),
					e->e.getId().toString()
			));
			String roleEnName = new String("");
			switch (userType){
				case 2:
					roleEnName = "kefuleader";
					break;
				case 7:
					roleEnName = "salesleader";
					break;
				default:
					break;
			}
			users = userDao.findOfficeLeaderList(officeId,userType,null,roleEnName);
			if(users != null && users.size()>0) {
				maps.put("leader",users.stream().map(t->t.getId().toString()).collect(Collectors.joining(",")));
			}
			redisUtils.hmSetAll(RedisConstant.RedisDBType.REDIS_SYS_DB,key,maps,0l);
			return users.stream().map(t->t.getId().toString()).collect(Collectors.toList());
		}
	}

	/**
	 * 待接单订单 by订单找客服
	 */
	//该方式不使用 去掉sys_user_area
	public static List<Long> getKefuIdListByOrder(Long customerId,Long areaId){
		List<Long> userIds = Lists.newArrayList();
		List<User> users = userDao.getKefuListByCustomer(customerId);
		if(users !=null && users.size()>0){
			userIds = users.stream().map(t->t.getId()).collect(Collectors.toList());
		}
		/*users = userDao.getKefuListByArea(areaId);
		if(users !=null && users.size()>0){
			return users.stream().map(t->t.getId()).collect(Collectors.toList());
		}*/

		// add on 2020-9-12 begin
		List<Long> userIdsFromMS = msSysUserCustomerService.findUserIdListByCustomerId(customerId);
		List<Long> validUserId = Lists.newArrayList();
		if (!ObjectUtils.isEmpty(userIdsFromMS)) {
			if (userIdsFromMS.size() < 100) {
				Optional.ofNullable(userDao.checkDelFlagOfUser(userIdsFromMS)).ifPresent(validUserId::addAll);
			} else {
				Lists.partition(userIdsFromMS, 100).forEach(partList->{
					Optional.ofNullable(userDao.checkDelFlagOfUser(partList)).ifPresent(validUserId::addAll);
				});
			}
		}
		// 是否还要添加方法，来验证返回的用户id是否已删除
		msSysUserCustomerService.compareCollectionData(userIds, validUserId, customerId+"", "UserUtils.getKefuIdListByOrder");
		// add on 2020-9-12 end

		return userIds;
		//return Lists.newArrayList();
	}

	/**
	 * 获取所有菜单
	 * @return
	 */
	public static List<Role> getAllRoleList(){
		List<Role> roleList = redisUtils.getList(RedisConstant.RedisDBType.REDIS_SYS_DB,RedisConstant.SYS_ROLE_ALL_LIST,Role[].class);
		if (CollectionUtils.isEmpty(roleList)){
			roleList = Lists.newArrayList();
            List<Office> separateOfficeList = Lists.newArrayList();
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

				roleList.addAll(newRoleList);
            }

			redisUtils.set(RedisConstant.RedisDBType.REDIS_SYS_DB,RedisConstant.SYS_ROLE_ALL_LIST,roleList, 1800l);//15分钟
		}
		return roleList;
	}

	/**
	 * 获取当前用户角色列表   commented at 2021-06-01
	 * @return

	public static List<Role> getRoleList(){
		User user = getUser();
		if(user == null || user.getId()==null || user.getId()<=0){
			return Lists.newArrayList();
		}
		String key = String.format(RedisConstant.SHIRO_USER_ROLE,user.getId());
		List<Role> roleList = redisUtils.getList(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB,key,Role[].class);
		if (roleList == null){
			Role role = new Role();
			//role.getSqlMap().put("dsf", BaseService.dataScopeFilter(user.getCurrentUser(), "o", "u"));
            roleList = roleDao.findListNew(role);
            if (!ObjectUtils.isEmpty(roleList)) {
                Set<Long> officeIds = roleList.stream().map(r -> r.getOffice().getId()).collect(Collectors.toSet());
                List<Office> officeList = msSysOfficeService.findSpecColumnListByIds(Lists.newArrayList(officeIds));  //仅获取code，name

                Map<Long, Office> officeMap = Maps.newHashMap();
                if (!ObjectUtils.isEmpty(officeList)) {
                    officeMap = officeList.stream().collect(Collectors.toMap(r -> r.getId(), Function.identity()));
                }

                for(int i=0; i< roleList.size(); i++) {
                    Role tempRole = roleList.get(i);
                    Office office = officeMap.get(tempRole.getOffice().getId());
                    if (office != null) {
                        tempRole.setOffice(office);
                    }
                }
                try {
					roleList = roleList.stream().sorted(Comparator.comparing((Role x) -> x.getOffice().getCode()).thenComparing(Role::getName)).collect(Collectors.toList());
				} catch (Exception ex) {
				}
            }
            // add on 2020-12-2 end

			redisUtils.setEX(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB,key, roleList, 0l);
		}
		return roleList;
	}
	 */

	/**
	 * 根据用户id查询角色列表  2021-5-29
	 * @param userId
	 * @return
	 */
	public static List<Role> findRoleList(Long userId){
		if(userId == null || userId <= 0){
			return Lists.newArrayList();
		}
		String key = String.format(RedisConstant.SHIRO_USER_ROLE, userId);
		List<Role> roleList = redisUtils.getList(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB,key,Role[].class);
		if (CollectionUtils.isEmpty(roleList)){
			//roleList = roleDao.getUserRoles(userId);
			Role role = new Role();
			role.setUser(new User(userId));
			roleList = roleDao.findListNew(role);
			if (!ObjectUtils.isEmpty(roleList)) {
				Set<Long> officeIds = roleList.stream().map(r -> r.getOffice().getId()).collect(Collectors.toSet());
				List<Office> officeList = msSysOfficeService.findSpecColumnListByIds(Lists.newArrayList(officeIds));  //仅获取code，name

				Map<Long, Office> officeMap = Maps.newHashMap();
				if (!ObjectUtils.isEmpty(officeList)) {
					officeMap = officeList.stream().collect(Collectors.toMap(r -> r.getId(), Function.identity()));
				}

				for(int i=0; i< roleList.size(); i++) {
					Role tempRole = roleList.get(i);
					Office office = officeMap.get(tempRole.getOffice().getId());
					if (office != null) {
						tempRole.setOffice(office);
					}
				}
				try {
					roleList = roleList.stream().sorted(Comparator.comparing((Role x) -> x.getOffice().getCode()).thenComparing(Role::getName)).collect(Collectors.toList());
					redisUtils.setEX(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB,key, roleList, 1800l);
				} catch (Exception ex) {
				}
			}
		}
		return roleList;
	}

	/**
	 * 移除缓存
	 * @param key
	 */
	public static void removeCache(RedisConstant.RedisDBType dbType,String key){
		if(key.contains("*") || key.contains("?")){
			redisUtils.removePattern(dbType,key);
		}else {
			redisUtils.remove(dbType,key);
		}
	}

	/**
	 * 获取当前用户授权菜单
	 * @return
	 */
	public static List<Menu> getMenuList(){
		User user = getUser();
		if(user == null || user.getId()==null || user.getId()<=0){
			return Lists.newArrayList();
		}
		String key = String.format(RedisConstant.SHIRO_USER_MENU,user.getId());
		List<Menu> menuList = redisUtils.getList(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB,key,Menu[].class);
		if (menuList == null){
			if (user.isAdmin()){
				menuList = getAllMenuList();
			}else{
				Menu m = new Menu();
				m.setUserId(user.getId());
				menuList = menuDao.findByUserId(m);
			}
			long ttl = redisUtils.ttl(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB,String.format(RedisConstant.SHIRO_USER_SESSION,user.getId()));
			if(ttl<=0){
				ttl = 60 * RedisValueCache.globExpire;
			}
			redisUtils.setEX(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB,key,menuList, ttl);
		}
		return menuList;
	}

	/**
	 * 获取当前用户授权第一层菜单(parent id =0)
	 * @return
	 */
	public static List<Menu> getTopMenuList(){
		User user = getUser();
		if(user == null || user.getId()==null || user.getId()<=0){
			return Lists.newArrayList();
		}
		String key = String.format(RedisConstant.SHIRO_USER_MENU,user.getId());
		List<Menu> menuList = redisUtils.getList(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB,key,Menu[].class);
		if (menuList == null){
			if (user.isAdmin()){
				menuList = getAllMenuList();
			}else{
				Menu m = new Menu();
				m.setUserId(user.getId());
				menuList = menuDao.findByUserId(m);
			}
			long ttl = redisUtils.ttl(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB,String.format(RedisConstant.SHIRO_USER_SESSION,user.getId()));
			if(ttl<=0){
				ttl = 60 * RedisValueCache.globExpire;
			}
			redisUtils.setEX(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB,key,menuList, ttl);
		}
		return menuList.stream().filter(t->t.getParentId().longValue()==1l).collect(Collectors.toList());
	}

	/**
	 * 获取所有菜单
	 * @return
	 */
	public static List<Menu> getAllMenuList(){
		List<Menu> menuList = redisUtils.getList(RedisConstant.RedisDBType.REDIS_SYS_DB,RedisConstant.SYS_MENU_ALL_LIST,Menu[].class);
		if (menuList == null){
			menuList = menuDao.findAllList(new Menu());
			redisUtils.set(RedisConstant.RedisDBType.REDIS_SYS_DB,RedisConstant.SYS_MENU_ALL_LIST,menuList, 0l);
		}
		return menuList;
	}

	/**
	 * 重载所有区域至缓存
	 */
	public static Boolean loadAreas(){
		//
		//  此方法只有 AreaController.clearcache()调用，现在已注释此方法  2020-7-31
		//
		//List<Area> areaList = areaDao.findAllList(new Area());  //mark on 2020-7-31
		List<Area> areaList = Lists.newArrayList();  // add on 2020-7-31
		if(areaList == null || areaList.size()==0) return false;
		int size=0;
		Integer mkey=0;
		try {
			Map<Integer,List<Area>> maps = areaList.stream().filter(t->t.getType()>1).collect(Collectors.groupingBy(Area::getType));
			redisUtils.redisTemplate.executePipelined(new RedisCallback<Object>() {    // enable Redis Pipeline
				@Override
				public Object doInRedis(RedisConnection connection) throws DataAccessException {
					connection.select(RedisConstant.RedisDBType.REDIS_SYS_DB.ordinal());
					StringBuffer key = new StringBuffer();
					maps.forEach((k,v)->{
						key.setLength(0);
						key.append(String.format(RedisConstant.SYS_AREA_TYPE,k.intValue()));
						connection.del(key.toString().getBytes(StandardCharsets.UTF_8));//del
						v.forEach(area->{
							connection.zAdd(key.toString().getBytes(StandardCharsets.UTF_8),area.getId(),redisUtils.gsonRedisSerializer.serialize(area));
						});
					});
					//del area:type:all
					connection.del(String.format(RedisConstant.SYS_AREA_TYPE,"all").getBytes(StandardCharsets.UTF_8));
					return null;
				}
			});
			/*
			Map<Integer, List<Area>> maps = areaList.stream().collect(Collectors.groupingBy(Area::getType));
			String typeKey = new String("");
			for (final Integer key : maps.keySet()) {
				mkey = key;
				areaList = maps.get(mkey);
				//remove area:type:?
				typeKey = String.format(RedisConstant.SYS_AREA_TYPE,mkey);
				//String typeKey = "area:type:" + mkey;
				redisUtils.remove(RedisConstant.RedisDBType.REDIS_SYS_DB,typeKey);
				size = areaList.size();
				Area area;
				for (int i = 0; i < size; i++) {
					area = areaList.get(i);
					redisUtils.zAdd(RedisConstant.RedisDBType.REDIS_SYS_DB,typeKey, area, area.getId(), 0);
				}
			}*/
			return true;
		}
		catch (Exception ex){
			redisUtils.remove(RedisConstant.RedisDBType.REDIS_SYS_DB,String.format(RedisConstant.SYS_AREA_TYPE,"*"));
			return false;
		}
	}


	/**
	 * 获取所有的区域
	 * @return
	 */
	public static List<Area> getAreaList(){

		// mark on 2020-11-13 begin
		/*
		List<Area> areaList;
		String key = new String(String.format(RedisConstant.SYS_AREA_TYPE,"all"));
		if(!redisUtils.exists(RedisConstant.RedisDBType.REDIS_SYS_DB,key)) {
			boolean hastype = redisUtils.exists(RedisConstant.RedisDBType.REDIS_SYS_DB,String.format(RedisConstant.SYS_AREA_TYPE,2));
			if(hastype){
				hastype = redisUtils.exists(RedisConstant.RedisDBType.REDIS_SYS_DB,String.format(RedisConstant.SYS_AREA_TYPE,3));
			}
			if(hastype){
				hastype = redisUtils.exists(RedisConstant.RedisDBType.REDIS_SYS_DB,String.format(RedisConstant.SYS_AREA_TYPE,4));
			}
			if(!hastype){
				return loadAllAreasToCache();
			}
			Long result = redisUtils.zUnionStore(RedisConstant.RedisDBType.REDIS_SYS_DB,String.format(RedisConstant.SYS_AREA_TYPE,"all"), String.format(RedisConstant.SYS_AREA_TYPE,"2"), String.format(RedisConstant.SYS_AREA_TYPE,"3"), String.format(RedisConstant.SYS_AREA_TYPE,"4"));
		}

		//get from redis
		//long ltime = System.currentTimeMillis();
		Set<byte[]> sets = redisUtils.zRange(RedisConstant.RedisDBType.REDIS_SYS_DB,key,0,-1);
		//System.out.println("读取redis耗时"+(System.currentTimeMillis()-ltime));
		final List<Area> result = Lists.newArrayList();
		sets.stream()
				.forEach(t->result.add((Area)redisUtils.gsonRedisSerializer.deserialize(t,Area.class)));
		return result.stream().sorted(Comparator.comparingInt(Area::getSort)).collect(Collectors.toList());
		*/
		// mark on 2020-11-13 end

		return loadAllAreasToCache();   // add on 2020-7-31
	}

	/**
	 * 装载所有区域到缓存
	 * key:   area:type:2,area:type:3,area:type:4,area:type:all
	 */
	public static List<Area> loadAllAreasToCache(){

		/*
		List<Area> areaList = areaDao.findAllList(new Area());  //mark on 2020-7-30

		// mark on 2020-7-31 begin
		if (areaList == null || areaList.size() == 0) {
			return areaList;
		}

		//检查锁
		String lockKey = new String("LOCK:LOAD:ALL:AREA");
		if(redisUtils.exists(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockKey)){
			//已有其它功能在处理
			return areaList;
		}
		//加锁
		Boolean accepted = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockKey, 1, 60);//60秒
		if (!accepted) {//加锁失败，再重复2次
			try {
				java.lang.Thread.sleep(Global.ONE_SECOND);
				accepted = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockKey, 1, 60);//60秒
			} catch (InterruptedException e) {
				try{
					java.lang.Thread.sleep(Global.ONE_SECOND);
					accepted = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockKey, 1, 60);//60秒
				}catch (Exception e1){
					e1.printStackTrace();
					return Lists.newArrayList();
				}
			}
		}

		if (true == accepted) {
			// list -> map<type,list>
			//type = 1,国别
			areaList = areaList.stream().filter(t->t.getType()>1).collect(Collectors.toList());;
			Map<Integer,List<Area>> maps = areaList.stream().collect(Collectors.groupingBy(Area::getType));
			StringBuffer areaKey = new StringBuffer();
			maps.forEach((k,v)->{
				areaKey.setLength(0);
				areaKey.append(String.format(RedisConstant.SYS_AREA_TYPE,k));
				redisUtils.remove(RedisConstant.RedisDBType.REDIS_SYS_DB,areaKey.toString());//del
				Set<RedisZSetCommands.Tuple> sets = v.stream()
						.map(t -> new RedisTuple(redisUtils.gsonRedisSerializer.serialize(t), t.getId().doubleValue()))
						.collect(Collectors.toSet());
				redisUtils.zAdd(RedisConstant.RedisDBType.REDIS_SYS_DB,areaKey.toString(),sets,0l);
			});

			//merge
			Long result = redisUtils.zUnionStore(RedisConstant.RedisDBType.REDIS_SYS_DB,String.format(RedisConstant.SYS_AREA_TYPE,"all"), String.format(RedisConstant.SYS_AREA_TYPE,"2"), String.format(RedisConstant.SYS_AREA_TYPE,"3"), String.format(RedisConstant.SYS_AREA_TYPE,"4"));
			try{
				redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB,lockKey);
			}catch (Exception e){
				try{
					redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB,lockKey);
				}catch (Exception e1){
					e1.printStackTrace();
				}
			}
			//删除锁
			redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockKey);
		}
		*/
		List<Area> areaListFromMS = msSysAreaService.findListExcludeTownFormCache();  // add on 2020-7-31
		//AreaService.compareListArea("", areaList, areaListFromMS, "UserUtils.loadAllAreasToCache");
		//return areaList;
		// mark on 2020-7-31 end
		return areaListFromMS;
	}

	/**
	 * 获取当前用户有权限访问的部门
	 * 改成直接从数据库读取 by ryan
	 * @return
	 */
	public static List<Office> getOfficeList() {
		List<Office> officeList;
		User user = getUser();
		List<Office> officeListFromMS;
		if (user.isAdmin()) {
			//officeList = officeDao.findAllList(new Office());  //mark on 2020-12-19
			officeList = msSysOfficeService.findAllList();
		} else {
			Office office = new Office();
			//office.getSqlMap().put("dsf", BaseService.dataScopeFilter(user, "a", ""));
			//officeList = officeDao.findList(office);  //mark on 2020-12-19
			Long id = Optional.ofNullable(user.getOffice()).map(Office::getId).orElse(0L);
			officeList = msSysOfficeService.findList(id);
		}
		//msSysOfficeService.compareListOffice("", officeList, officeListFromMS, "UserUtils.getOfficeList");  //add on 2020-11-28  //mark on 2020-12-20

		// add on 2020-8-4 begin
		if (!officeList.isEmpty()) {
			List<Long> areaIds = officeList.stream().filter(r->r.getArea()!= null).map(r->r.getArea().getId()).distinct().collect(Collectors.toList());
			List<Area> areaList = areaIds!= null && !areaIds.isEmpty()?msSysAreaService.findSpecListByIds(areaIds):Lists.newArrayList();
			Map<Long,Area> areaMap = areaList != null && !areaList.isEmpty()?areaList.stream().collect(Collectors.toMap(r->r.getId(),r->r)):Maps.newHashMap();
			officeList.stream().forEach(r->{
				Area area = areaMap.get(r.getArea().getId());
				if (area != null) {
					r.getArea().setName(area.getName());
					r.getArea().setParentIds(area.getParentIds());
				}
			});
		}
		// add on 2020-8-4 end
		return officeList;
		/*
		List<Office> officeList = (List<Office>)getCache(CACHE_OFFICE_LIST);
		if (officeList == null){
			User user = getUser();
			if (user.isAdmin()){
				officeList = officeDao.findAllList(new Office());
			}else{
				Office office = new Office();
				office.getSqlMap().put("dsf", BaseService.dataScopeFilter(user, "a", ""));
				officeList = officeDao.findList(office);
			}
			putCache(CACHE_OFFICE_LIST, officeList);
		}
		return officeList;
		*/
	}

	/**
	 * 获取所有的部门
	 * 改成直接从数据库读取 by ryan
	 * @return
	 */
	public static List<Office> getOfficeAllList(){
		//return officeDao.findAllList(new Office());  // mark on 2020-8-4
		// add on 2020-8-4 begin
		//List<Office> officeList = officeDao.findAllList(new Office());  //mark on 2020-12-19
		// add on 2020-11-28 begin
		List<Office> officeList = msSysOfficeService.findAllList();
		//msSysOfficeService.compareListOffice("", officeList, officeListFromMS, "UserUtils.getOfficeAllList");  // mark on 2020-12-19
		// add on 2020-11-28 end
		if (!officeList.isEmpty()) {
			List<Long> areaIds = officeList.stream().filter(r->r.getArea()!= null).map(r->r.getArea().getId()).collect(Collectors.toList());
			List<Area> areaList = areaIds!= null && !areaIds.isEmpty()?msSysAreaService.findSpecListByIds(areaIds):Lists.newArrayList();
			Map<Long,Area> areaMap = areaList.stream().collect(Collectors.toMap(r->r.getId(),r->r));
			officeList.stream().forEach(r->{
				Area area = areaMap.get(r.getArea().getId());
				if (area != null) {
					r.getArea().setName(area.getName());
					r.getArea().setParentIds(area.getParentIds());
				}
			});
		}
		return officeList;
		// add on 2020-8-4 end
		/*
		List<Office> officeList = redisUtils.getList(CACHE_OFFICE_ALL_LIST,Office[].class);
		if (officeList == null || officeList.size()==0){
			officeList = officeDao.findAllList(new Office());
			redisUtils.set(CACHE_OFFICE_ALL_LIST,officeList, 0l);
		}
		return officeList;
		*/
	}

	/**
	 * 获取授权主要对象
	 */
	public static Subject getSubject(){
		return SecurityUtils.getSubject();
	}

	/**
	 * 获取当前登录者对象
	 */
	public static Principal getPrincipal(){
		try{
			Subject subject = SecurityUtils.getSubject();
			Principal principal = (Principal)subject.getPrincipal();
			if (principal != null){
				return principal;
			}
		}catch (UnavailableSecurityManagerException e) {

		}catch (InvalidSessionException e){

		} catch (Exception ex) {
		}
		return null;
	}

	public static Session getSession(){
		try{
			Subject subject = SecurityUtils.getSubject();
			Session session = subject.getSession(false);
			if (session == null){
				session = subject.getSession();
			}
			if (session != null){
				return session;
			}
//			subject.logout();
		}catch (InvalidSessionException e){

		}
		return null;
	}

	public static String getSessionId(){
		Session session = getSession();
		if(session == null){
			return "";
		}else{
			if(session.getId() == null){
				return "";
			}else {
				return session.getId().toString();
			}
		}
	}

	/**
	 * 是否是验证码登录
	 *
	 * @param username 用户名
	 * @param isFail   计数加1
	 * @param clean    计数清零
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static boolean isValidateCodeLogin(String username, boolean isFail, boolean clean) {
		Long loginFailNum = 0l;
		if (isFail) {
			loginFailNum = redisUtils.hIncr(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB,"loginFailMap",username);
		}
		if (clean) {
			redisUtils.hdel(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB,"loginFailMap",username);
		}
		return loginFailNum >= 3;
        /*
        Map<String, Integer> loginFailMap = (Map<String, Integer>) CacheUtils.get("loginFailMap");
        if (loginFailMap == null) {
            loginFailMap = Maps.newHashMap();
            CacheUtils.put("loginFailMap", loginFailMap);
        }
        Integer loginFailNum = loginFailMap.get(username);
        if (loginFailNum == null) {
            loginFailNum = 0;
        }
        if (isFail) {
            loginFailNum++;
            loginFailMap.put(username, loginFailNum);
        }
        if (clean) {
            loginFailMap.remove(username);
        }
        return loginFailNum >= 3;
        */
	}

	public static boolean isKefuLeader(User user){
		if(user == null) return false;
		List<Long> roleIds = user.getRoleIdList();
		if(roleIds!=null && roleIds.size()>0){
			return roleIds.stream().filter(t->Objects.equals(t,Long.valueOf(14))).count()>1?true:false;
		}
		List<Role> roles = roleDao.getUserRoles(user.getId());
		if(roles==null || roles.size()==0){
			return false;
		}
		return roles.stream().filter(t->Objects.equals(t.getId(),Long.valueOf(14))).count()>1?true:false;
	}

	/**
	 * 查找指定的用户列表：每个User对象只有id、name属性有值
	 */
	public static List<User> getListByUserType(Integer userType) {
		if (userType != null && userType >= User.USER_TYPE_SYSTEM && userType <= User.USER_TYPE_SEARCH_CUSTOMER) {
			return userDao.getListByUserType(userType);
		}
		else {
			return Lists.newArrayList();
		}
	}

	/**
	 * 查询客服列表：每个User对象只有id、name属性有值
	 */
	public static List<User> getKefuList() {
//		return getListByUserType(User.USER_TYPE_SERVICE);
		return MSUserUtils.getListByUserType(User.USER_TYPE_SERVICE);//user微服务
	}

	// ============== User Cache ==============
	/*
	public static Object getCache(String key) {
		return getCache(key, null);
	}

	public static Object getCache(String key, Object defaultValue) {
//		Object obj = getCacheMap().get(key);
		Object obj = getSession().getAttribute(key);
		return obj==null?defaultValue:obj;
	}

	public static void putCache(String key, Object value) {
//		getCacheMap().put(key, value);
		getSession().setAttribute(key, value);
	}

	public static void removeCache(String key) {
//		getCacheMap().remove(key);
		getSession().removeAttribute(key);
	}
	*/

	/**
	 * 截取用户姓名
	 * @param user
	 * @param length
	 */
	public static void substrUserName(User user,int length){
		if(user != null){
			user.setName(StringUtils.left(user.getName(),length));
		}
	}

	public static String getIMToken(Long userId,String session,Integer userType){
		try {
			StringBuilder sb = new StringBuilder();
			String salt = "im";
			sb.append("im").append("|").append(userId.toString()).append("|").append(session).append("|").append(userType.toString()).append("|").append("kkl");
			return JwtUtil.createJWT(JWT_IM_ID, sb.toString(), Constant.JWT_TTL);
		}catch (Exception e){
			return "";
		}
	}

	public static String[] parseIMUserInfo(String token){
		try {
			final Claims claims = JwtUtil.parseJWT(token);
			if (claims.isEmpty() || !claims.containsKey("jti") || !claims.containsKey("sub") || !claims.containsKey("exp")) {
				throw new RuntimeException("非法请求：认证错误");
			}
			//jwt id
			if(!claims.getId().equalsIgnoreCase(JWT_IM_ID)){
				throw new RuntimeException("非法请求：认证错误");
			}
			//iat(Issue At):签发时间
			//exp:过期时间
			if(claims.getExpiration().before(new Date())){
				throw new RuntimeException("非法请求：认证错误");
			}
			if(StringUtils.isBlank(claims.getSubject())){
				throw new RuntimeException("非法请求：认证错误");
			}
			String str = claims.getSubject();
			String[] params = str.split("|");
			if(params.length<5){
				throw new RuntimeException("非法请求：认证错误");
			}
			return params;
		}catch (Exception e){
			e.printStackTrace();
			return null;
		}
	}

	//region customer

	/**
	 * 读取厂商帐号店铺清单
	 * @param user  帐号
	 * @return  如帐号未设置店铺，按客户返回所有店铺清单
	 */
	public static List<Dict> getShops(long customerId,User user){
		if(user == null || customerId <= 0){
			return Lists.newArrayList();
		}
		//非客户帐号，读取所有店铺
		if(!user.isCustomer()){
			List<Dict> shops = B2BMDUtils.getCustomerKKLShopListNew(customerId);
			return shops;
		}
		// 客户帐号处理
		Map<Integer, String> map = Optional.ofNullable(user.getAttributesMap()).orElse(Maps.newHashMap());
		String json = map.get(UserAttributesEnum.CUSTOMERSHOP.getValue());
		if(StrUtil.isBlank(json)){
			// 未配置店铺
			List<Dict> shops = B2BMDUtils.getCustomerKKLShopListNew(customerId);
			return shops;
		}
		// 已配置店铺
		List<CustomerShop> shopList = GsonUtils.getInstance().getGson().fromJson(json, new TypeToken<List<CustomerShop>>(){}.getType());
		if(CollectionUtils.isEmpty(shopList)){
			List<Dict> shops = B2BMDUtils.getCustomerKKLShopListNew(customerId);
			return shops;
		}
		List<Dict> shops = shopList.stream()
				.map(s -> {
					Dict dict = new Dict(s.getId(),s.getName());
					dict.setSort(s.getDataSource());
					return dict;
				}).collect(Collectors.toList());
		return shops;
	}

	/**
	 * 获取客户账号负责的店铺id清单
	 * @param user
	 * @return
	 */
	public static List<String> getShopIdsOfCustomerAccount(User user){
		if(user == null){
			return Lists.newArrayList();
		}
		if(!user.isCustomer()){
			return Lists.newArrayList();
		}
		Map<Integer, String> map = Optional.ofNullable(user.getAttributesMap()).orElse(Maps.newHashMap());
		String json = map.get(Integer.valueOf(UserAttributesEnum.CUSTOMERSHOP.getValue()));
		if(StrUtil.isBlank(json)){
			return Lists.newArrayList();
		}
		List<CustomerShop> shops = GsonUtils.getInstance().getGson().fromJson(json, new TypeToken<List<CustomerShop>>(){}.getType());
		if(CollectionUtils.isEmpty(shops)){
			return Lists.newArrayList();
		}
		List<String> shopIds = shops.stream().map(s -> s.getId()).distinct().collect(Collectors.toList());
		return shopIds;
	}

	//endregion customer
}
