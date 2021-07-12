/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.wolfking.jeesite.modules.sys.web;

import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.wolfking.jeesite.common.config.Global;
import com.wolfking.jeesite.common.config.redis.GsonRedisSerializer;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.utils.excel.ExportExcel;
import com.wolfking.jeesite.common.utils.excel.ImportExcel;
import com.wolfking.jeesite.common.validator.BeanValidators;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.ProductCategory;
import com.wolfking.jeesite.modules.md.service.CustomerService;
import com.wolfking.jeesite.modules.md.utils.CustomerUtils;
import com.wolfking.jeesite.modules.sys.entity.*;
import com.wolfking.jeesite.modules.sys.entity.viewModel.SalesModel;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.service.OfficeService;
import com.wolfking.jeesite.modules.sys.service.SystemService;
import com.wolfking.jeesite.modules.sys.service.UserRegionService;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 用户Controller
 * @author ThinkGem
 * @version 2013-8-29
 */
@Controller
@RequestMapping(value = "${adminPath}/sys/user")
@Slf4j
public class UserController extends BaseController {

	@Autowired
	private SystemService systemService;

	@Autowired
	private AreaService areaService;

    @Autowired
    private CustomerService customerService;

	@Autowired
	private OfficeService officeService;

	@Autowired
	GsonRedisSerializer gsonRedisSerializer;

	@Autowired
	private UserRegionService userRegionService;

	@ModelAttribute
	public User get(@RequestParam(required=false) Long id,HttpServletRequest request) {
		User user =  new User();
		if (id != null && !request.getMethod().equalsIgnoreCase("post")){
			user = systemService.getUser(id);
			//读取登录信息
			HashMap<String,Object> loginInfo = systemService.getLoginInfo(id);
			if(loginInfo!=null && loginInfo.size() == 2){
				user.setLoginIp(loginInfo.get("login_ip").toString());
				if(loginInfo.get("login_date") != null) {
					user.setLoginDate((Date) loginInfo.get("login_date"));
				}
			}
		}
		return user;
	}

	@RequiresPermissions("sys:user:view")
	@RequestMapping(value = {"index"})
	public String index(User user, Model model) {
		return "modules/sys/userIndex";
	}

	@RequiresPermissions("sys:user:view")
	@RequestMapping(value = {"list", ""})
	public String list(User user, HttpServletRequest request, HttpServletResponse response, Model model) {
		user.setUserType(User.USER_TYPE_SYSTEM);
		Page<User> page = systemService.findUser(new Page<User>(request, response), user);
        model.addAttribute("page", page);
        model.addAttribute("userType", user.getUserType());
//		return "modules/sys/userList";
		return "modules/sys/userListNew";
	}

	@RequiresPermissions("sys:user:view")
	@RequestMapping(value = {"salesList"})
	public String salesList(User user, HttpServletRequest request, HttpServletResponse response, Model model) {
		user.setUserType(User.USER_TYPE_SALES);
		Page<User> page = systemService.findUser(new Page<User>(request, response), user);
		model.addAttribute("page", page);
		model.addAttribute("userType", user.getUserType());
		return "modules/sys/userSalesList";
	}
	@ResponseBody
	@RequiresPermissions("sys:user:view")
	@RequestMapping(value = {"listData"})
	public Page<User> listData(User user, HttpServletRequest request, HttpServletResponse response, Model model) {
		Page<User> page = systemService.findUser(new Page<User>(request, response), user);
		return page;
	}

	@RequiresPermissions("sys:user:view")
	@RequestMapping(value = "form")
	public String form(User user, Model model, Integer type) {
//		if (user.getCompany()==null || user.getCompany().getId()==null){
//			user.setCompany(UserUtils.getUser().getCompany());
//		}
//		if (user.getOffice()==null || user.getOffice().getId()==null){
//			user.setOffice(UserUtils.getUser().getOffice());
//		}

		java.util.Map<String,Object> paramMap = Maps.newHashMap();
        paramMap.put("userId",user.getId());

		if (user.getId()!=null && ((user.getAreaList() != null && user.getAreaList().size() == 0) || user.getAreaList() == null )) {
            //user.setAreaList(areaService.findListByUserIdOrAreaId(paramMap));
			List<UserRegion> userRegionList = userRegionService.getUserRegionsFromDB(user.getId());
			List<Long> userRegionIds = Lists.newArrayList();
			for(UserRegion item:userRegionList){
				if(item.getAreaType().equals(Area.TYPE_VALUE_COUNTRY)){
					userRegionIds.add(1L);
				}else if(item.getAreaType().equals(Area.TYPE_VALUE_PROVINCE)){
					userRegionIds.add(item.getProvinceId());
				}else if(item.getAreaType().equals(Area.TYPE_VALUE_CITY)){
					userRegionIds.add(item.getCityId());
				}else if(item.getAreaType().equals(Area.TYPE_VALUE_COUNTY)){
					userRegionIds.add(item.getAreaId());
				}
			}
			user.setAreaIds(userRegionIds);
        }

        if (user.getId()!=null && ((user.getCustomerList()!=null && user.getCustomerList().size() == 0) || user.getCustomerList() == null)){
            user.setCustomerList(customerService.findListByUserIdOrCustomerId(paramMap));
        }
        if (user.getId() != null) {
        	user.setProductCategoryIds(systemService.getProductCategoryIds(user.getId()));
		}
		List<Area> areaList = areaService.findAll(2);
		areaList.add(0,new Area(1l,"区域列表",1));
		model.addAttribute("areaList", areaList);
		if (user.getUserType().intValue() == User.USER_TYPE_SERVICE) {
			List<Long> customerIds = systemService.findCustomerIdList(paramMap);
			user.setCustomerIds(Sets.newHashSet(customerIds));
		}
		// add on 2020-07-03
		if (user.getId() != null) {
			if (type == 7) {
				// 根据当前业务员获取客户
				List<Customer> customers = Lists.newArrayList();
				Customer customer = new Customer();
				if (user.getSubFlag() == 1) {// 业务员
					customer.setSales(user);
					customers = customerService.findCustomersWithSales(customer);
				} else {
					customer.setMerchandiser(user);
					customers = customerService.findCustomersWithSales(customer);
				}
				model.addAttribute("customers", customers);
			}
			model.addAttribute("showChildren", true);
		}
		List<Role> allRoles;

		if(type.equals(User.USER_TYPE_SALES)){
			allRoles = systemService.findAllList().stream().filter(t->!t.getName().startsWith("网点")).collect(Collectors.toList());
		}else {
			allRoles = systemService.findAllList().stream().filter(t->!t.getName().startsWith("网点-")).filter(t-> !t.getName().contains("业务")).filter(t->!t.getName().contains("跟单")).collect(Collectors.toList());
		}

		model.addAttribute("user", user);
        //model.addAttribute("allCustomers", CustomerUtils.getCustomerList()); //当usertype=2(为客服时) 显示VIP客户
		model.addAttribute("allCustomers", new ArrayList<Customer>());
		model.addAttribute("allRoles", allRoles);
		model.addAttribute("userType", type);
//		return "modules/sys/userForm";
		return "modules/sys/userFormNew";
	}

	/*@RequiresPermissions("sys:user:edit")
	@RequestMapping(value = "save")
	public String save(User user, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
		if(Global.isDemoMode()){
			addMessage(redirectAttributes, "演示模式，不允许操作！");
			return "redirect:" + adminPath + "/sys/user/list?repage";
		}
		String officeId = request.getParameter("office.id");
		if(StringUtils.isBlank(officeId)){
			user.setOffice(new Office(0l));
		}else {
			user.setOffice(officeService.get(Long.valueOf(officeId))); //add on 2017-4-13
		}
		// 如果新密码为空，则不更换密码
		if (StringUtils.isNotBlank(user.getNewPassword())) {
			user.setPassword(SystemService.entryptPassword(user.getNewPassword()));
		}
		if (!beanValidator(model, user)){
			return form(user, model);
		}
		if (!"true".equals(checkLoginName(user.getOldLoginName(), user.getLoginName()))){
			addMessage(model, "保存用户'" + user.getLoginName() + "'失败，登录名已存在");
			return form(user, model);
		}
		// 角色数据有效性验证，过滤不在授权内的角色
		List<Role> roleList = Lists.newArrayList();
		List<Long> roleIdList = user.getRoleIdList();
		for (Role r : systemService.findAllRole()){
			if (roleIdList.contains(r.getId())){
				roleList.add(r);
			}
		}
		user.setRoleList(roleList);
		StringBuilder json = new StringBuilder(user.getAreas());
		List<Area> areaList = Lists.newArrayList();
		if(json.length()>0){
			json = new StringBuilder(json.toString().replace("&quot;","\""));
			areaList = Arrays.asList((Area[])gsonRedisSerializer.fromJson(json.toString(),Area[].class));
		}
        user.setAreaList(areaList);
		//customerIds -> customer List
		if(user.getCustomerIds() != null && user.getCustomerIds().size()>0){
			List<Customer> customers = Lists.newArrayList();
			for (Long cid: user.getCustomerIds()) {
				customers.add(new Customer(cid));
			}
			user.setCustomerList(customers);
		}
		// regions
		Set<UserRegion> newRegions = Sets.newHashSet();
		Set<UserRegion> removeRegions = null;
		List<UserRegion> userRegionList = Lists.newArrayList();  // add on 2019-12-9
		if(StringUtils.isNoneBlank(user.getRegions())){
			json = new StringBuilder(user.getRegions());
			json = new StringBuilder(json.toString().replace("&quot;","\""));
			if(json.length()>0) {
				List<UserRegion> regions = Arrays.asList((UserRegion[]) gsonRedisSerializer.fromJson(json.toString(), UserRegion[].class));
				if(ObjectUtils.isEmpty(regions) && !ObjectUtils.isEmpty(areaList)){
					addMessage(model, "用户区域解析错误");
					return form(user,model);
				}
				userRegionList.addAll(regions); //add on 2019-12-9
				//修改
				if(user.getId() != null && user.getId() > 0){
					Map<String,Set<UserRegion>> regionsMap = compareUserRegions(regions,user.getId());
					newRegions = regionsMap.get("new");
					removeRegions = regionsMap.get("remove");
				}else{
					newRegions = regions.stream().collect(Collectors.toSet());
				}
			}
		} else {
			// add on 2019-12-11 begin
			// 添加原因： 保持redis缓存中的数据与DB的数据一致
			Optional.ofNullable(user.getId()).ifPresent(userRegionService::deleteByUserId);
			// add on 2019-12-11 end
		}

		try {
			systemService.saveUser(user,newRegions,removeRegions); // 保存用户信息
			userRegionService.writeUserRegionCache(user.getId(), userRegionList);  //add on 2019-12-9
		}
		catch(Exception ex)
		{
			addMessage(model, "保存用户失败,失败原因:" +ex.getMessage());
			return form(user,model);
		}
		// 清除用户缓存
		try {
			UserUtils.clearCache(user);
		}catch (Exception e){
			log.error("[UserController.save]clear cache,userId:{},loginName:{}",user.getId(),user.getLoginName(),e);
		}
		addMessage(redirectAttributes, "保存用户'" + user.getLoginName() + "'成功");
		return "redirect:" + adminPath + "/sys/user/list?repage";
	}*/

	@RequiresPermissions("sys:user:edit")
	@RequestMapping(value = "save")
	@ResponseBody
	public AjaxJsonEntity save(User user, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
		AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
		if(Global.isDemoMode()){
			ajaxJsonEntity.setSuccess(false);
			ajaxJsonEntity.setMessage("演示模式，不允许操作！");
		}
		String officeId = request.getParameter("office.id");
		if(StringUtils.isBlank(officeId)){
			user.setOffice(new Office(0l));
		}else {
			user.setOffice(officeService.get(Long.valueOf(officeId))); //add on 2017-4-13
		}
		// 如果新密码为空，则不更换密码
		if (StringUtils.isNotBlank(user.getNewPassword())) {
			user.setPassword(SystemService.entryptPassword(user.getNewPassword()));
		}
		if (!beanValidator(model, user)){
			ajaxJsonEntity.setSuccess(false);
		}
		if (!"true".equals(checkLoginName(user.getOldLoginName(), user.getLoginName()))){
			ajaxJsonEntity.setSuccess(false);
			ajaxJsonEntity.setMessage("保存用户'" + user.getLoginName() + "'失败，登录名已存在");
		}
		// 角色数据有效性验证，过滤不在授权内的角色
		List<Role> roleList = Lists.newArrayList();
		List<Long> roleIdList = user.getRoleIdList();
		for (Role r : systemService.findAllRole()){
			if (roleIdList.contains(r.getId())){
				roleList.add(r);
			}
		}
		user.setRoleList(roleList);
		StringBuilder json = new StringBuilder(user.getAreas());
		List<Area> areaList = Lists.newArrayList();
		if(json.length()>0){
			json = new StringBuilder(json.toString().replace("&quot;","\""));
			areaList = Arrays.asList((Area[])gsonRedisSerializer.fromJson(json.toString(),Area[].class));
		}
		user.setAreaList(areaList);
		//customerIds -> customer List
		if(user.getCustomerIds() != null && user.getCustomerIds().size()>0){
			List<Customer> customers = Lists.newArrayList();
			for (Long cid: user.getCustomerIds()) {
				customers.add(new Customer(cid));
			}
			user.setCustomerList(customers);
		}
		// regions
		Set<UserRegion> newRegions = Sets.newHashSet();
		Set<UserRegion> removeRegions = null;
		List<UserRegion> userRegionList = Lists.newArrayList();  // add on 2019-12-9
		if(StringUtils.isNoneBlank(user.getRegions())){
			json = new StringBuilder(user.getRegions());
			json = new StringBuilder(json.toString().replace("&quot;","\""));
			if(json.length()>0) {
				List<UserRegion> regions = Arrays.asList((UserRegion[]) gsonRedisSerializer.fromJson(json.toString(), UserRegion[].class));
				if(ObjectUtils.isEmpty(regions) && !ObjectUtils.isEmpty(areaList)){
					ajaxJsonEntity.setSuccess(false);
					ajaxJsonEntity.setMessage("用户区域解析错误");
				}
				userRegionList.addAll(regions); //add on 2019-12-9
				//修改
				if(user.getId() != null && user.getId() > 0){
					Map<String,Set<UserRegion>> regionsMap = compareUserRegions(regions,user.getId());
					newRegions = regionsMap.get("new");
					removeRegions = regionsMap.get("remove");
				}else{
					newRegions = regions.stream().collect(Collectors.toSet());
				}
			}
		} else {
			// add on 2019-12-11 begin
			// 添加原因： 保持redis缓存中的数据与DB的数据一致
			Optional.ofNullable(user.getId()).ifPresent(userRegionService::deleteByUserId);
			// add on 2019-12-11 end
		}

		try {
			systemService.saveUser(user,newRegions,removeRegions); // 保存用户信息
			userRegionService.writeUserRegionCache(user.getId(), userRegionList);  //add on 2019-12-9
			ajaxJsonEntity.setMessage("保存用户'" + user.getLoginName() + "'成功");
		} catch (Exception ex) {
			ajaxJsonEntity.setSuccess(false);
			ajaxJsonEntity.setMessage("保存用户失败,失败原因:" +ex.getMessage());
		}
		// 清除用户缓存
		try {
			UserUtils.clearCache(user);
		} catch (Exception e){
			log.error("[UserController.save]clear cache,userId:{},loginName:{}",user.getId(),user.getLoginName(),e);
		}
		return ajaxJsonEntity;
	}

    /*@RequiresPermissions("sys:user:view")
    @RequestMapping(value = "addCustomer")
	public String addCustomer(User user, Long salesId, Long merchandiserId, Model model){
        String view = "modules/sys/userEditCustomer";
        // 微服务获取所有客户列表
        List<Customer> customerList = customerService.findAllCustomerListFromDB();
        if (user != null && user.getId() != null) {
        	// 根据当前业务员获取客户
			Customer customer = new Customer();
			List<Customer> customers = Lists.newArrayList();
			if (user.getSubFlag() == 1) {// 业务员
				customer.setSales(user);
				customers = customerService.findCustomersWithSales(customer);
			} else {
				customer.setMerchandiser(user);
				customers = customerService.findCustomersWithSales(customer);
			}
			if (!customers.isEmpty()) {
//				List<Long> ids = customers.stream().map(entity -> entity.getId()).collect(Collectors.toList());
				model.addAttribute("customers", customers);
			}
		}
        model.addAttribute("customerList", customerList);
        return view;
    }*/

	@RequiresPermissions("sys:user:view")
	@RequestMapping(value = "addCustomer")
	public String addCustomerNew(User user){
//		return "modules/sys/userEditCustomerNew";
		// UPDATE ON 2020-08-03
		return "modules/sys/userEditCustomerNewTwo";
	}

	/**
	 * 通过业务员或跟单员查询客户列表
	 * @param customer
	 * @param model
	 * @return
	 */
	/*@RequiresPermissions("sys:user:view")
	@RequestMapping(value = "responsibleCustomer")
	@ResponseBody
    public AjaxJsonEntity responsibleCustomer(Customer customer, Model model){
		AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
		List<Long> ids = Lists.newArrayList();
		try {
			List<Customer> customers = customerService.findCustomersWithSales(customer);
			ids = customers.stream().map(c -> c.getId()).collect(Collectors.toList());
			ajaxJsonEntity.setData(ids);
		} catch (Exception e) {
			ajaxJsonEntity.setSuccess(false);
			ajaxJsonEntity.setMessage(e.getMessage());
		}
		model.addAttribute("customer", customer);
		return ajaxJsonEntity;
	}*/
	@RequiresPermissions("sys:user:view")
	@RequestMapping(value = "responsibleCustomer")
	@ResponseBody
	public AjaxJsonEntity responsibleCustomer(Customer customer, Model model){
		AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
		List<Long> ids = Lists.newArrayList();
		try {
			List<Customer> customers = customerService.findCustomersWithSales(customer);
//			ids = customers.stream().map(c -> c.getId()).collect(Collectors.toList());
//			ajaxJsonEntity.setData(ids);

			List<User> saleList = systemService.getAllSales();
			Map<Long, String> salesMap = saleList.stream().collect(Collectors.toMap(User::getId, User::getName));
			List<SalesModel> list = Lists.newArrayList();
			for (Customer entity : customers) {
				SalesModel salesModel = new SalesModel();
				salesModel.setCustomerId(entity.getId());
				salesModel.setCustomerName(entity.getName());
				salesModel.setSalesName(salesMap.get(entity.getSales().getId()));
				salesModel.setMerchandiserName(salesMap.get(entity.getMerchandiser().getId()));
				list.add(salesModel);
			}
			ajaxJsonEntity.setData(list);
		} catch (Exception e) {
			ajaxJsonEntity.setSuccess(false);
			ajaxJsonEntity.setMessage(e.getMessage());
		}
		model.addAttribute("customer", customer);
		return ajaxJsonEntity;
	}

	@RequiresPermissions("sys:user:edit")
	@RequestMapping(value = "batchEditCustomer")
	@ResponseBody
	public AjaxJsonEntity batchEditCustomer(User user) {
		AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
		if(Global.isDemoMode()){
			ajaxJsonEntity.setSuccess(false);
			ajaxJsonEntity.setMessage("演示模式，不允许操作！");
		}
		try {
			systemService.batchSave(user);
			ajaxJsonEntity.setMessage("更新成功");
		} catch (Exception ex) {
			ajaxJsonEntity.setSuccess(false);
			ajaxJsonEntity.setMessage("更新客户失败,失败原因:" +ex.getMessage());
		}
		return ajaxJsonEntity;
	}

	/**
	 * 比对新提交区域与原有区域
	 * 返回比对后结果
	 * @param regions
	 * @param userId
	 * @return Map<key,List<UserRegion>>
	 *     new: 新增集合
	 *     remove: 删除集合
	 */
	private Map<String,Set<UserRegion>> compareUserRegions(List<UserRegion> regions,Long userId){
		Set<UserRegion> regionSet = Sets.newHashSetWithExpectedSize(regions.size());
		regions.stream().forEach(t-> {
			t.setUserId(userId);
			regionSet.add(t);
		});
		//List<UserRegion> orgRegions = userRegionService.getUserRegions(userId); // mark on 2019-12-11 //原因：在修改region后直接写缓存导致缓存中的数据与DB数据不一致
		List<UserRegion> orgRegions = userRegionService.getUserRegionsFromDB(userId);  // add on 2019-12-11
		Map<String,Set<UserRegion>> map = Maps.newHashMapWithExpectedSize(2);
		if(ObjectUtils.isEmpty(orgRegions)){
			orgRegions = null;
			map.put("new",regionSet);
			map.put("remove",Sets.newHashSet());
			return map;
		}
		Set<UserRegion> orgSet = orgRegions.stream().collect(Collectors.toSet());
		orgRegions = null;
		Set<UserRegion> newSet = Sets.difference(regionSet,orgSet);
		Set<UserRegion> removeSet = Sets.difference(orgSet,regionSet);
		map.put("new",newSet);
		map.put("remove",removeSet);
		return map;
	}

	/*@RequiresPermissions("sys:user:edit")
	@RequestMapping(value = "delete")
	public String delete(User user, RedirectAttributes redirectAttributes) {
		if(Global.isDemoMode()){
			addMessage(redirectAttributes, "演示模式，不允许操作！");
			return "redirect:" + adminPath + "/sys/user/list?repage";
		}
		if (UserUtils.getUser().getId().equals(user.getId())){
			addMessage(redirectAttributes, "删除用户失败, 不允许删除当前用户");
		}else if (User.isAdmin(user.getId())){
			addMessage(redirectAttributes, "删除用户失败, 不允许删除超级管理员用户");
		}else{
			systemService.deleteUser(user);
			//清除用户区域缓存
            userRegionService.removeUserRegionCash(user.getId());
			addMessage(redirectAttributes, "删除用户成功");
		}
		return "redirect:" + adminPath + "/sys/user/list?repage";
	}*/

	@RequiresPermissions("sys:user:edit")
	@RequestMapping(value = "delete")
	@ResponseBody
	public AjaxJsonEntity delete(User user, RedirectAttributes redirectAttributes) {
		AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
		if(Global.isDemoMode()){
			ajaxJsonEntity.setSuccess(false);
			ajaxJsonEntity.setMessage("演示模式，不允许操作！");
		}
		if (UserUtils.getUser().getId().equals(user.getId())){
			ajaxJsonEntity.setSuccess(false);
			ajaxJsonEntity.setMessage("删除用户失败, 不允许删除当前用户");
		}else if (User.isAdmin(user.getId())){
			ajaxJsonEntity.setSuccess(false);
			ajaxJsonEntity.setMessage("删除用户失败, 不允许删除超级管理员用户");
		}else{
			systemService.deleteUser(user);
			//清除用户区域缓存
			userRegionService.removeUserRegionCash(user.getId());
			ajaxJsonEntity.setMessage("删除用户成功");
		}
		return ajaxJsonEntity;
	}

	@RequiresPermissions("sys:user:edit")
	@RequestMapping(value = "userEnableDisable")
	@ResponseBody
	public AjaxJsonEntity userEnableDisable(Long userId,Integer statusFlag){
		AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
		if(Global.isDemoMode()){
			ajaxJsonEntity.setSuccess(false);
			ajaxJsonEntity.setMessage("演示模式，不允许操作！");
		}
		if(statusFlag == 1){
			if (UserUtils.getUser().getId().equals(userId)){
				ajaxJsonEntity.setSuccess(false);
				ajaxJsonEntity.setMessage("停用用户失败, 不允许停用当前用户");
			}else if (User.isAdmin(userId)){
				ajaxJsonEntity.setSuccess(false);
				ajaxJsonEntity.setMessage("停用用户失败, 不允许停用超级管理员用户");
			}else {
				systemService.userEnableDisable(userId,statusFlag);
				ajaxJsonEntity.setMessage("登录帐号已停用");
			}
		}else{
			systemService.userEnableDisable(userId,statusFlag);
			ajaxJsonEntity.setMessage("登录帐号已启用");
		}
		return ajaxJsonEntity;
	}
	/**
	 * 导出用户数据
	 * @param user
	 * @param request
	 * @param response
	 * @param redirectAttributes
	 * @return
	 */
	@RequiresPermissions("sys:user:view")
    @RequestMapping(value = "export", method=RequestMethod.POST)
    public String exportFile(User user, HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes) {
		try {
            String fileName = "用户数据"+ DateUtils.getDate("yyyyMMddHHmmss")+".xlsx";
            Page<User> requestPage = new Page<User>(request, response);
            String pageNo = request.getParameter("pageNo");
            if(pageNo.equalsIgnoreCase("all")){
                requestPage = new Page<User>(request, response,-1);
            }
            Page<User> page = systemService.findUser(requestPage, user);
    		new ExportExcel("用户数据", User.class).setDataList(page.getList()).write(response, fileName).dispose();
    		//return null;
		} catch (Exception e) {
			addMessage(redirectAttributes, "导出用户失败！失败信息："+e.getMessage());
		}
		return "redirect:" + adminPath + "/sys/user/list?repage";
    }

	/**
	 * 导入用户数据
	 * @param file
	 * @param redirectAttributes
	 * @return
	 */
	@RequiresPermissions("sys:user:edit")
    @RequestMapping(value = "import", method=RequestMethod.POST)
    public String importFile(MultipartFile file, RedirectAttributes redirectAttributes) {
		if(Global.isDemoMode()){
			addMessage(redirectAttributes, "演示模式，不允许操作！");
			return "redirect:" + adminPath + "/sys/user/list?repage";
		}
		try {
			int successNum = 0;
			int failureNum = 0;
			StringBuilder failureMsg = new StringBuilder();
			ImportExcel ei = new ImportExcel(file, 1, 0);
			List<User> list = ei.getDataList(User.class);
			for (User user : list){
				try{
					if ("true".equals(checkLoginName("", user.getLoginName()))){
						user.setPassword(SystemService.entryptPassword("123456"));
						BeanValidators.validateWithException(validator, user);
						systemService.saveUser(user,null,null);
						successNum++;
					}else{
						failureMsg.append("<br/>登录名 "+user.getLoginName()+" 已存在; ");
						failureNum++;
					}
				}catch(ConstraintViolationException ex){
					failureMsg.append("<br/>登录名 "+user.getLoginName()+" 导入失败：");
					List<String> messageList = BeanValidators.extractPropertyAndMessageAsList(ex, ": ");
					for (String message : messageList){
						failureMsg.append(message+"; ");
						failureNum++;
					}
				}catch (Exception ex) {
					failureMsg.append("<br/>登录名 "+user.getLoginName()+" 导入失败："+ex.getMessage());
				}
			}
			if (failureNum>0){
				failureMsg.insert(0, "，失败 "+failureNum+" 条用户，导入信息如下：");
			}
			addMessage(redirectAttributes, "已成功导入 "+successNum+" 条用户"+failureMsg);
		} catch (Exception e) {
			addMessage(redirectAttributes, "导入用户失败！失败信息："+e.getMessage());
		}
		return "redirect:" + adminPath + "/sys/user/list?repage";
    }
	
	/**
	 * 下载导入用户数据模板
	 * @param response
	 * @param redirectAttributes
	 * @return
	 */
	@RequiresPermissions("sys:user:view")
    @RequestMapping(value = "import/template")
    public String importFileTemplate(HttpServletResponse response, RedirectAttributes redirectAttributes) {
		try {
            String fileName = "用户数据导入模板.xlsx";
    		List<User> list = Lists.newArrayList(); list.add(UserUtils.getUser());
    		new ExportExcel("用户数据", User.class, 2).setDataList(list).write(response, fileName).dispose();
    		return null;
		} catch (Exception e) {
			addMessage(redirectAttributes, "导入模板下载失败！失败信息："+e.getMessage());
		}
		return "redirect:" + adminPath + "/sys/user/list?repage";
    }

	/**
	 * 验证登录名是否有效
	 * @param oldLoginName
	 * @param loginName
	 * @return
	 */
	@ResponseBody
	@RequiresPermissions("sys:user:edit")
	@RequestMapping(value = "checkLoginName")
	public String checkLoginName(String oldLoginName, String loginName) {
        User user = UserUtils.getUser();
        if(user==null || user.getId()==null){
            return "false";
        }
		if (loginName !=null && loginName.equals(oldLoginName)) {
			return "true";
		} else if (loginName !=null && systemService.getUserByLoginName(loginName) == null) {
			return "true";
		}
		return "登录名已存在。";
	}

	/**
	 * 检查手机号是否已注册
	 */
	@ResponseBody
	@RequestMapping(value = "checkMobile")
	public String checkMobile(String mobile,String expectId,String expectType,String returnType,HttpServletRequest request,HttpServletResponse response) {
		response.setContentType("text/html; charset=UTF-8");
		String rtnType = StringUtils.isBlank(returnType)?"boolean":"message";//返回类型，是布尔型，还是信息
		if(StringUtils.isBlank(mobile)){
			return rtnType.equalsIgnoreCase("boolean")?"true":"true";
		}
		try{
			Long eid = null;
			if(StringUtils.isNoneBlank(expectId)){
				eid = Long.valueOf(expectId);
			}
			List<User> users = systemService.findByMobile(mobile.trim(),expectType,eid);
			if(users==null || users.size()==0){
				return rtnType.equalsIgnoreCase("boolean")?"true":"true";
			}
			return rtnType.equalsIgnoreCase("boolean")?"false":"该手机已注册";
		} catch (Exception ex){
			log.error("[UserController.checkMobile] mobile:{}",mobile,ex);
			return rtnType.equalsIgnoreCase("boolean")?"false":"检查错误";
		}
	}


	/**
	 * 用户信息显示及保存
	 * @param user
	 * @param model
	 * @return
	 */
	@RequiresPermissions("user")
	@RequestMapping(value = "info")
	public String info(User user, HttpServletResponse response, Model model) {
		User currentUser = UserUtils.getUser();
		if (StringUtils.isNotBlank(user.getName())){
			if(Global.isDemoMode()){
				model.addAttribute("message", "演示模式，不允许操作！");
				return "modules/sys/userInfo";
			}
			currentUser.setEmail(user.getEmail());
			currentUser.setPhone(user.getPhone());
			currentUser.setMobile(user.getMobile());
			currentUser.setRemarks(user.getRemarks());
			currentUser.setPhoto(user.getPhoto());
			currentUser.setQq(user.getQq());
			systemService.updateMyInfo(currentUser);
//			systemService.updateUserInfo(currentUser);
			model.addAttribute("message", "保存用户信息成功");
		}
		model.addAttribute("user", currentUser);
		model.addAttribute("Global", new Global());
		return "modules/sys/userInfo";
	}

	/**
	 * 返回用户信息
	 * @return
	 */
	@RequiresPermissions("user")
	@ResponseBody
	@RequestMapping(value = "infoData")
	public User infoData() {
		return UserUtils.getUser();
	}
	
	/**
	 * 修改个人用户密码
	 * @param oldPassword
	 * @param newPassword
	 * @param model
	 * @return
	 */
	@RequiresPermissions("user")
	@RequestMapping(value = "modifyPwd")
	public String modifyPwd(String oldPassword, String newPassword, Model model) {
		User user = UserUtils.getUser();
		if (StringUtils.isNotBlank(oldPassword) && StringUtils.isNotBlank(newPassword)){
			if(Global.isDemoMode()){
				model.addAttribute("message", "演示模式，不允许操作！");
				return "modules/sys/userModifyPwd";
			}
			if (SystemService.validatePassword(oldPassword, user.getPassword())){
				systemService.updatePasswordById(user.getId(), user.getLoginName(), newPassword);
				model.addAttribute("message", "修改密码成功!");
			}else{
				model.addAttribute("message", "修改密码失败，旧密码错误");
			}
		}
		model.addAttribute("user", user);
		return "modules/sys/userModifyPwd";
	}

	/**
	 * ajax修改个人用户密码 (预留)
	 *
	 * @param oldPassword
	 * @param newPassword
	 * @param model
	 * @return
	 */
	@ResponseBody
	@RequiresPermissions("user")
	@RequestMapping(value = "ajaxModifyPwd",method = RequestMethod.POST)
	public AjaxJsonEntity ajaxModifyPwd(String oldPassword, String newPassword, Model model) {
		AjaxJsonEntity result = new AjaxJsonEntity(true);
		User user = UserUtils.getUser();
		if (StringUtils.isNotBlank(oldPassword) && StringUtils.isNotBlank(newPassword)){
			if (SystemService.validatePassword(oldPassword, user.getPassword())){
				systemService.updatePasswordById(user.getId(), user.getLoginName(), newPassword);
				result.setSuccess(true);
				result.setMessage("修改密码成功!<br/>为保护您的系统帐号安全，3秒后自动跳转到登录页面，请重新登录。");
			}else{
				result.setSuccess(false);
				result.setMessage("修改密码失败，旧密码错误");
			}
		}else{
			if(StringUtils.isBlank(oldPassword) ) {
				result.setSuccess(false);
				result.setMessage("请输入原密码。");
			}else{
				result.setSuccess(false);
				result.setMessage("请输入新密码。");
			}
		}
		return result;
	}

	@RequiresPermissions("user")
	@ResponseBody
	@RequestMapping(value = "treeData")
	public List<Map<String, Object>> treeData(@RequestParam(required=false) Long officeId, HttpServletResponse response) {
		List<Map<String, Object>> mapList = Lists.newArrayList();
		List<User> list = systemService.findUserByOfficeId(officeId);
		for (int i=0; i<list.size(); i++){
			User e = list.get(i);
			Map<String, Object> map = Maps.newHashMap();
			map.put("id", "u_"+e.getId());
			map.put("pId", officeId);
			map.put("name", StringUtils.replace(e.getName(), " ", ""));
			mapList.add(map);
		}
		return mapList;
	}
    
//	@InitBinder
//	public void initBinder(WebDataBinder b) {
//		b.registerCustomEditor(List.class, "roleList", new PropertyEditorSupport(){
//			@Autowired
//			private SystemService systemService;
//			@Override
//			public void setAsText(String text) throws IllegalArgumentException {
//				String[] ids = StringUtils.split(text, ",");
//				List<Role> roles = new ArrayList<Role>();
//				for (String id : ids) {
//					Role role = systemService.getRole(Long.valueOf(id));
//					roles.add(role);
//				}
//				setValue(roles);
//			}
//			@Override
//			public String getAsText() {
//				return Collections3.extractToString((List) getValue(), "id", ",");
//			}
//		});
//	}
}
