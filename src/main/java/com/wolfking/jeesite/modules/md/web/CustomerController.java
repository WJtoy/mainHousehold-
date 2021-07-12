/**
 * Copyright &copy; 2012-2013 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.wolfking.jeesite.modules.md.web;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.kkl.kklplus.entity.md.MDCustomer;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.config.Global;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.dao.MdAttachmentDao;
import com.wolfking.jeesite.modules.md.entity.*;
import com.wolfking.jeesite.modules.md.entity.viewModel.CustomerPrices;
import com.wolfking.jeesite.modules.md.service.CustomerService;
import com.wolfking.jeesite.modules.md.service.ProductPriceService;
import com.wolfking.jeesite.modules.md.service.ProductService;
import com.wolfking.jeesite.modules.md.service.ServiceTypeService;
import com.wolfking.jeesite.modules.md.utils.CustomerUtils;
import com.wolfking.jeesite.modules.sys.entity.*;
import com.wolfking.jeesite.modules.sys.service.DictService;
import com.wolfking.jeesite.modules.sys.service.SystemService;
import com.wolfking.jeesite.modules.sys.service.UserSubService;
import com.wolfking.jeesite.modules.sys.utils.SeqUtils;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providermd.service.MSAttachmentService;
import com.wolfking.jeesite.ms.providermd.service.MSCustomerService;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 客戶Controller
 *
 * @author ThinkGem
 * @version 2013-3-23
 */
@Slf4j
@Controller
@RequestMapping(value = "${adminPath}/md/customer")
public class CustomerController extends BaseController {

    @Autowired
    private CustomerService customerService;
    @Resource
    private MdAttachmentDao mdAttachmentDao;

    @Autowired
    private SystemService systemService;
    @Autowired
    private ServiceTypeService serviceTypeService;
    @Autowired
    private ProductService productService;
    @Autowired
    private ProductPriceService productPriceService;
    @Autowired
    private DictService dictService;
    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private MSCustomerService msCustomerService;

    @Autowired
    private MSAttachmentService msAttachmentService;

    @Autowired
    private UserSubService userSubService;

    //region 客户


    //@RequiresPermissions("md:customer:view")
    @RequestMapping(value = {"list", ""})
    public String listMS(MDCustomer mdCustomer, HttpServletRequest request, HttpServletResponse response, Model model) {
        if (!SecurityUtils.getSubject().isPermitted("md:customer:view")) {
            addMessage(model, "未开通浏览权限");
            model.addAttribute("page", new Page<MDCustomer>());
            model.addAttribute("mdCustomer", new MDCustomer());
            return "modules/md/customerListMS";
        }
        //  微服务调用获取客户信息
        if (mdCustomer == null) {
            mdCustomer = new MDCustomer();
        }
        User user = UserUtils.getUser();
		/*if (user.isSaleman()) {
			if (user.getSubFlag().intValue() == GlobalMappingSalesSubFlagEnum.SALES.getValue()) {
				mdCustomer.setSalesId(user.getId());
			} else if (user.getSubFlag().intValue() == GlobalMappingSalesSubFlagEnum.MERCHANDISER.getValue()) {
				mdCustomer.setMerchandiserId(user.getId());
			}
		}*/

        if (user.isSalesPerson()) {
            mdCustomer.setSalesId(user.getId());
        } else {
            if (user.isMerchandiser()) {
                mdCustomer.setMerchandiserId(user.getId());
            }
            if (StringUtils.isBlank(mdCustomer.getName()) && StringUtils.isBlank(mdCustomer.getPhone())) {
                addMessage(model, "请输入客户名称或负责人电话");
                model.addAttribute("page", new Page<MDCustomer>());
                model.addAttribute("mdCustomer", mdCustomer);
                return "modules/md/customerListMS";
            }
        }

        Page<MDCustomer> page = msCustomerService.findMDCustomerList(new Page<MDCustomer>(request, response), mdCustomer);
        model.addAttribute("page", page);
        model.addAttribute("mdCustomer", mdCustomer);

        return "modules/md/customerListMS";
    }

    @RequiresPermissions("md:customer:edit")
    @RequestMapping(value = "form")
    public String form(Customer customer, Model model) {
        User user = UserUtils.getUser();
        if (customer.getId() == null) {
			/*
			// mark on 2020-3-21
			if(user.isSaleman()) {
				customer.setSales(user);//自己
			}
			*/

            if (user.isSalesPerson()) {
                customer.setSales(user);//自己
            }

            CustomerFinance finance = new CustomerFinance();
            Dict paymentType = new Dict();
            //切换为微服务
            paymentType.setId(MSDictUtils.getDictByValue(String.valueOf(CustomerFinance.YF), "PaymentType").getId());
            paymentType.setValue(String.valueOf(CustomerFinance.YF));
            paymentType.setLabel("预付");
            paymentType.setType("PaymentType");
            finance.setPaymentType(paymentType);//预付
            customer.setFinance(finance);
            model.addAttribute("productIds", "");
        } else {
            customer = customerService.get(customer.getId());

            List<Long> attachmentIds = Lists.newArrayList();

            if (customer.getLogo() != null && customer.getLogo().getId() != null && customer.getLogo().getId() > 0) {
                attachmentIds.add(customer.getLogo().getId());
            }
            if (customer.getAttachment1() != null && customer.getAttachment1().getId() != null && customer.getAttachment1().getId() > 0) {
                attachmentIds.add(customer.getAttachment1().getId());
            }
            if (customer.getAttachment2() != null && customer.getAttachment2().getId() != null && customer.getAttachment2().getId() > 0) {
                attachmentIds.add(customer.getAttachment2().getId());
            }
            if (customer.getAttachment3() != null && customer.getAttachment3().getId() != null && customer.getAttachment3().getId() > 0) {
                attachmentIds.add(customer.getAttachment3().getId());
            }
            if (customer.getAttachment4() != null && customer.getAttachment4().getId() != null && customer.getAttachment4().getId() > 0) {
                attachmentIds.add(customer.getAttachment4().getId());
            }
            if (!attachmentIds.isEmpty()) {
                // 调用微服务返回附件列表
                List<MdAttachment> attachments = msAttachmentService.findListByAttachmentIds(attachmentIds);
                if (!CollectionUtils.isEmpty(attachments)) {
                    Map<Long, MdAttachment> map = attachments.stream().collect(Collectors.toMap(a -> a.getId(), Function.identity()));

                    if (Optional.ofNullable(map.get(customer.getLogo().getId())).isPresent() && map.get(customer.getLogo().getId()).getId().equals(customer.getLogo().getId())) {
                        customer.setLogo(map.get(customer.getLogo().getId()));
                    }
                    if (Optional.ofNullable(map.get(customer.getAttachment1().getId())).isPresent() && map.get(customer.getAttachment1().getId()).getId().equals(customer.getAttachment1().getId())) {
                        customer.setAttachment1(map.get(customer.getAttachment1().getId()));
                    }
                    if (Optional.ofNullable(map.get(customer.getAttachment2().getId())).isPresent() && map.get(customer.getAttachment2().getId()).getId().equals(customer.getAttachment2().getId())) {
                        customer.setAttachment2(map.get(customer.getAttachment2().getId()));
                    }
                    if (Optional.ofNullable(map.get(customer.getAttachment3().getId())).isPresent() && map.get(customer.getAttachment3().getId()).getId().equals(customer.getAttachment3().getId())) {
                        customer.setAttachment3(map.get(customer.getAttachment3().getId()));
                    }
                    if (Optional.ofNullable(map.get(customer.getAttachment4().getId())).isPresent() && map.get(customer.getAttachment4().getId()).getId().equals(customer.getAttachment4().getId())) {
                        customer.setAttachment4(map.get(customer.getAttachment4().getId()));
                    }

                }
            }


            List<String> nameIdList = Lists.newArrayList();
            List<CustomerProduct> productcustomerList = customerService.getListByCustomer(customer.getId());
            for (CustomerProduct p : productcustomerList) {
                nameIdList.add(Long.toString(p.getProduct().getId()));
            }
            model.addAttribute("productIds", StringUtils.join(nameIdList, ","));

            // add on 2020-8-14 begin
            if (customer.getCustomizePriceFlag() != null && customer.getCustomizePriceFlag().equals(1)) {
                customer.setUseDefaultPrice(0);
            }
            // add on 2020-8-14 end
        }
        //List<Product> productList=productDao.findAllList();
        //model.addAttribute("Products", productList);
        //model.addAttribute("ProductCategorys", productCategoryDao.findAllList());
        model.addAttribute("customer", customer);

        return "modules/md/customerForm";
    }

    @RequiresPermissions("md:customer:edit")
    @RequestMapping(value = "save")
    // @Valid
    public String save(Customer customer, String oldCode, Double oldCredit, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {

        if (!StringUtils.isNotBlank(customer.getCode())) {
            customer.setCode(SeqUtils.NextSequenceNo("Customer"));//新的方法
        }
        if (customer.getId() != null && customer.getId() > 0) {
            if (!"true".equals(checkCustomerCode(oldCode, customer.getCode()))) {
                addMessage(model, "客户'" + customer.getCode() + "'已存在");
                return form(customer, model);
            }
        }
        if (customer.getSales() == null || customer.getSales().getId() <= 0) {
            addMessage(model, "请选择业务人员");
            return form(customer, model);
        }

        if (!beanValidator(model, customer)) {
            return form(customer, model);
        }

        try {
            customerService.save(customer, oldCredit);
            addMessage(redirectAttributes, "保存客戶'" + customer.getName() + "'成功");
        } catch (Exception e) {
            log.error("保存客戶{}失败", e);
            addMessage(redirectAttributes, "保存客戶'" + customer.getName()
                    + "'失败:" + e.getMessage());
        }
        return "redirect:" + Global.getAdminPath() + "/md/customer?repage";
    }

    //@RequiresPermissions("md:customer:view")
    @RequestMapping(value = "salesChangeForm")
    public String salesUpdateForm(Customer customer, Model model) {
        User user = UserUtils.getUser();
        if (customer.getId() != null) {
            customer = customerService.get(customer.getId());
        }
        model.addAttribute("customer", customer);

        return "modules/md/customerSalesForm";
    }

    @RequestMapping(value = "merchandiserChangeForm")
    public String merchandiserUpdateForm(Customer customer, Model model) {
        User user = UserUtils.getUser();
        if (customer.getId() != null) {
            customer = customerService.get(customer.getId());
        }
        model.addAttribute("customer", customer);

        return "modules/md/customerMerchandiserForm";
    }


    @ResponseBody
    @RequestMapping(value = {"saveSales"})
    public AjaxJsonEntity saveSales(@RequestParam Long id, @RequestParam Long salesId, HttpServletResponse response) {
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        User user = UserUtils.getUser();
        if (user == null || user.getId() == null) {
            result.setSuccess(false);
            result.setMessage("登录超时，请重新登录。");
            return result;
        }
        if (id == null) {
            result.setSuccess(false);
            result.setMessage("你传入的数据为空。");
            return result;
        }
        if (salesId == null) {
            result.setSuccess(false);
            result.setMessage("你传入的业务员数据为空。");
            return result;
        }
        try {
            log.warn("salesId:{}", salesId);
            Customer customer = new Customer(id);
            customer.setSales(new User(salesId));
            customer.setUpdateBy(user);
            customerService.saveSalesInfo(customer);

            User sales = systemService.getUser(salesId);
            result.setData(sales == null ? "" : sales.getName());
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage(e.getMessage().toString());
        }
        return result;
    }

    @ResponseBody
    @RequestMapping(value = {"saveMerchandiser"})
    public AjaxJsonEntity saveMerchandiser(@RequestParam Long id, @RequestParam Long merchandiserId, HttpServletResponse response) {
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        User user = UserUtils.getUser();
        if (user == null || user.getId() == null) {
            result.setSuccess(false);
            result.setMessage("登录超时，请重新登录。");
            return result;
        }
        if (id == null) {
            result.setSuccess(false);
            result.setMessage("你传入的数据为空。");
            return result;
        }
        if (merchandiserId == null) {
            result.setSuccess(false);
            result.setMessage("你传入的跟单员数据为空。");
            return result;
        }
        try {
            log.warn("merchandiserId:{}", merchandiserId);
            Customer customer = new Customer(id);
            customer.setMerchandiser(new User(merchandiserId));
            customer.setUpdateBy(user);
            customerService.saveMerchandiserInfo(customer);

            User merchandiser = systemService.getUser(merchandiserId);
            result.setData(merchandiser == null ? "" : merchandiser.getName());
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage(e.getMessage().toString());
        }
        return result;
    }

    public String checkCustomerCode(String oldLoginName, String code) {
        if (code != null && code.equals(oldLoginName)) {
            return "true";
        } else if (code != null && !customerService.existsCustomerByCode(code)) {
            return "true";
        }
        return "false";
    }

    @RequiresPermissions("md:customer:edit")
    @RequestMapping(value = "delete")
    public String delete(String id, RedirectAttributes redirectAttributes) {

        boolean isApprove = false;

        if (Global.isDemoMode()) {
            addMessage(redirectAttributes, "演示模式，不允许操作！");
            return "redirect:" + Global.getAdminPath() + "/md/customer?repage";
        }
        Customer customer = customerService.get(Long.parseLong(id));
        if (customer.getDelFlag() == User.APPROVE_FLAG_NO) {
            isApprove = true;
        }


        customerService.deleteById(customer.getId());
        //customerUtils.deleteCustomer(customer);
        addMessage(redirectAttributes, "删除客戶成功");

        if (isApprove) {
            return "redirect:" + Global.getAdminPath()
                    + "/md/customer/approvelist?repage";
        } else {
            return "redirect:" + Global.getAdminPath() + "/md/customer?repage";
        }

    }

    @RequiresPermissions("md:customer:edit")
    @RequestMapping(value =
            {"approvelist"})
    public String approveList(Customer customer, HttpServletRequest request,
                              HttpServletResponse response, Model model) {
        Page<Customer> page = customerService.findApprov(
                new Page<Customer>(request, response), customer);
        model.addAttribute("page", page);
        return "modules/md/customerApproveList";
    }

    @ResponseBody
    @RequiresPermissions("md:customer:edit")
    @RequestMapping(value =
            {"approve"})
    public AjaxJsonEntity approve(@RequestParam String ids,
                                  HttpServletResponse response) {
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        User user = UserUtils.getUser();
        if (user == null || user.getId() == null) {
            result.setSuccess(false);
            result.setMessage("登录超时，请重新登录。");
            return result;
        }
        try {
            customerService.approve(ids);
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage(e.getMessage().toString());
        }
        return result;
    }

    /**
     * 客户选择列表
     */
    @RequiresUser
    @RequestMapping(value = "select")
    public String select(Customer customer, HttpServletRequest request, HttpServletResponse response, Model model) {
        User user = UserUtils.getUser();
		/*
		// mark on 2020-3-21
		if (user.isSaleman()){
			customer.setSales(user);
		}
		*/

        if (user.isSalesPerson()) {
            customer.setSales(user);
        } else if (user.isMerchandiser()) {
            customer.setMerchandiser(user);
        }

        //Page<Customer> page = customerService.find(new Page<Customer>(request, response), customer); //mark on 2020-2-11 web端customer
        Page<Customer> page = msCustomerService.findCustomerList(new Page<Customer>(request, response), customer);  //add on 2020-2-11
        model.addAttribute("page", page);
        model.addAttribute("customer", customer);
        return "modules/md/cusotmerSelect";
    }

    /**
     * 获得产品分类json数据 包含id,pid,name三个节点内容
     *
     * @param kefu  客服id 参数取消
     * @param type    客户类型
     */
    @RequiresUser
    @ResponseBody
    @RequestMapping(value = "treeData")
    public List<Map<String, Object>> treeData(@RequestParam(required = false) Long kefu, @RequestParam(required = false) Long type, HttpServletResponse response) {
        User user = UserUtils.getUser();
        response.setContentType("application/json; charset=UTF-8");
        List<Map<String, Object>> mapList = Lists.newArrayList();
        List<Customer> list = Lists.newArrayList();
        if (user.isKefu()) {
            if (user.getSubFlag() == KefuTypeEnum.VIPKefu.getCode()) {
                //list = customerService.findVipListByKefu(user.getId());  // mark on 2020-3-17
                list = customerService.findVipListWithIdAndNameByKefu(user.getId()); // add on 2020-3-17
            } else if (user.getSubFlag() == KefuTypeEnum.Kefu.getCode()) {
                //未分配客户，读取非vip客户
                //list = customerService.findAllBaseList();   //mark on 2020-3-17
                //list = CustomerUtils.getCustomerListFromMS();  // add on 2019-7-23
                //list = list.stream().filter(t->t.getVipFlag()==0).collect(Collectors.toList()); //mark on 2020-3-17
                list = customerService.findNoVIPListWithIdAndName();   //add on 2020-3-17
            } else {
                list = CustomerUtils.getCustomerListFromMS();  // 2019-11-21
            }
			/* 客服都按区域筛选
			String key = String.format(RedisConstant.SHIRO_KEFU_CUSTOMER,user.getId());
			if(redisUtils.exists(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key )) {
                list = redisUtils.zRange(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB,key,0,-1,Customer.class);
			}else{
				UserUtils.loadUserInfo(user,null,null);//装载customer,区域等
				Map<String, Object> paramMap;
				paramMap = Maps.newHashMap();
				paramMap.put("userId", user.getId());
				list = customerService.findListByUserIdOrCustomerId(paramMap);
				if (list != null && list.size() > 0) {
					Set<RedisZSetCommands.Tuple> sets = Sets.newConcurrentHashSet();
					sets = list.stream()
							.map(t -> new RedisTuple(redisUtils.gsonRedisSerializer.serialize(t), t.getId().doubleValue()))
							.collect(Collectors.toSet());
					redisUtils.zAdd(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key, sets, 0);//不过期
					Set<Long> customerIds = list.stream()
							.map(t -> t.getId())
							.collect(Collectors.toSet());
					user.setCustomerIds(customerIds);
				}
			}
			//未分配客户，读取被分配的客户，做筛选
			if(list == null || list.size() == 0){
                list = CustomerUtils.getCustomerListFromMS();  // add on 2019-7-23
				List<Long> assignCustomers = customerService.getCustomerIdsAssignedService();
				if(assignCustomers!=null && assignCustomers.size()>0){
					list = list.stream().filter(t->!assignCustomers.contains(t.getId())).collect(Collectors.toList());
				}
			}
			*/
        } else if (user.isSalesPerson()) {                                              // add on 2019-7-27
            list = msCustomerService.findListBySalesId(user.getId().intValue());  // add on 2019-7-27
        } else if (user.isMerchandiser()) {
            list = msCustomerService.findListByMerchandiserId(user.getId());
        } else if (user.isSalesManager()) {  //业务主管
            //1. 先获取客户列表列表
            List<Long> customerIdList = userSubService.findCustomerIdListByUserId(user.getId(), user.getUserType());
            //2.去掉重复的客户id
            List<Long> uniqueCustomerIdList = Optional.ofNullable(customerIdList).orElse(Collections.emptyList()).stream().distinct().collect(Collectors.toList());
            if (!uniqueCustomerIdList.isEmpty()) {
                list = customerService.findIdAndNameListByIds(uniqueCustomerIdList);
            }
        } else {
            //list = CustomerUtils.getCustomerList(); //customerService.findAll();   // mark on 2019-7-23
            list = CustomerUtils.getCustomerListFromMS();  // add on 2019-7-23
        }
        //销售人员筛选

        // mark on 2019-7-27 begin
		/*
		if(user!=null && user.isSaleman()){
			list.stream().filter(t -> Objects.equals(t.getSales().getId(),user.getId()))
					.sorted(Comparator.comparing(Customer::getSort).thenComparing(Customer::getName))
					.forEach(t->{
						Map<String, Object> map= Maps.newHashMap();
						map.put("id", t.getId());
						map.put("pId", "");
						map.put("name", t.getName());
						mapList.add(map);
					});
			return mapList;
		}
		*/
        // mark on 2019-7-27 end

        list.stream()
                .sorted(Comparator.comparing(Customer::getSort).thenComparing(Customer::getName))
                .forEach(t -> {
                    Map<String, Object> map = Maps.newHashMap();
                    map.put("id", t.getId());
                    map.put("pId", "");
                    map.put("name", t.getName());
                    mapList.add(map);
                });
        return mapList;
		/*
		for (int i = 0,size = list.size(); i < size; i++)
		{
			Customer e = list.get(i);
			if(user!=null&&user.isSaleman())
			{
				if(e.getSales().equalsIgnoreCase(user.getName()))
				{
					Map<String, Object> map = Maps.newHashMap();
					map.put("id", e.getId());
					map.put("pId", "");
					map.put("name", e.getName());
					mapList.add(map);
				}
			}
			else
			{
				Map<String, Object> map = Maps.newHashMap();
				map.put("id", e.getId());
				map.put("pId", "");
				map.put("name", e.getName());
				mapList.add(map);
			}
		}
		return mapList;
		*/
    }

    //endregion 客户

    //region 客户帐号管理

    /**
     * 重置密码
     * @param id  账户id
     * @return
     */
    @ResponseBody
    @RequiresPermissions("md:customer:edit")
    @RequestMapping(value = "resetPassword")
    public AjaxJsonEntity resetPassword(String id, HttpServletResponse response) {
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        if (StringUtils.isBlank(id)) {
            result.setSuccess(false);
            result.setMessage("参数错误");
            return result;
        }
        Long lid = Long.valueOf(id);
        if (lid <= 0l) {
            result.setSuccess(false);
            result.setMessage("参数错误");
            return result;
        }

        User cuser = UserUtils.getUser();
        if (cuser == null || cuser.getId() == null) {
            result.setSuccess(false);
            result.setMessage("登录超时，请重新登录。");
            return result;
        }
        User user = customerService.getAccount(lid);
        if (user == null || user.getId() == null) {
            result.setSuccess(false);
            result.setMessage("客户帐号不存在或已停用");
            return result;
        }
        if (user.getMobile().trim().length() < 6) {
            result.setSuccess(false);
            result.setMessage("手机号长度不到6，");
            return result;
        }
        try {
            user.preUpdate();
            String pwd = new String("");
            pwd = StringUtils.right(user.getMobile().trim(), 6);
            //手机号后6位
            user.setPassword(SystemService.entryptPassword(pwd));
            customerService.resetPassword(user);
            //清除该帐号登录内容
            try {
                UserUtils.clearCache(user);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("更改密码时发生错误：" + e.getMessage());
        }
        return result;
    }

    @ResponseBody
//	@RequiresPermissions("md:customer:view")
    @RequestMapping(value = "account/checkLoginName")
    public String checkLoginName(String oldLoginName, String loginName, @RequestParam(required = false) String expectId) {
        User user = UserUtils.getUser();
        if (user == null || user.getId() == null) {
            return "false";
        }
        Long id = null;
        if (StringUtils.isNoneBlank(expectId)) {
            try {
                id = Long.valueOf(expectId);
            } catch (Exception e) {
            }
        }
        if (loginName != null && loginName.equals(oldLoginName)) {
            return "true";
        } else if (loginName != null
                && systemService.checkLoginName(id, loginName) == 0) {
            return "true";
        }
		/*if (loginName != null && loginName.equals(oldLoginName))
		{
			return "true";
		} else if (loginName != null
				&& systemService.getUserByLoginName(loginName) == null)
		{
			return "true";
		}*/
        return "客户账号人员登录名已存在";
//		return "false"; 不知什么原因，不显示messages中定义的提示
    }

    /**
     * 检查客户帐号手机号是否注册
     * @param id    帐号id
     * @param phone    手机号
     * @return
     */
    @ResponseBody
//	@RequiresPermissions("md:customer:view")
    @RequestMapping(value = "account/checkMasterPhone")
    public AjaxJsonEntity checkMasterPhone(String id, String phone) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        if (StringUtils.isBlank(phone)) {
            return result;
        }
        Long userId = systemService.checkLoginName(StringUtils.isBlank(id) ? null : Long.valueOf(id), phone);
        if (userId != null && userId > 0) {
            result.setMessage("手机号已注册");
            result.setSuccess(false);
        }
        return result;
    }


    @RequiresPermissions("md:customer:view")
    @RequestMapping(value = {"account/list", "account"})
    public String accountList(User user, HttpServletRequest request, HttpServletResponse response, Model model) {
        try {
            User cuser = UserUtils.getUser();
            if (cuser.isCustomer()) {
//				int orderApproveFlag = cuser.getCustomerAccountProfile().getOrderApproveFlag();
                CustomerAccountProfile profile = new CustomerAccountProfile();
                profile.setCustomer(cuser.getCustomerAccountProfile().getCustomer());
                profile.setOrderApproveFlag(cuser.getCustomerAccountProfile().getOrderApproveFlag());
                user.setCustomerAccountProfile(profile);
            } else if (cuser.isSalesPerson()) {
                user.setSalesId(cuser.getId());
            }
            user.setDelFlag(null);
            // Page<User> page = customerService.find(new Page<>(request,response), user);  // mark on 2019-9-7
            Page<User> page = customerService.findWithOutCustomerAccountProfile(new Page<>(request, response), user);
//			model.addAttribute("currentUserType", UserUtils.getUser().getUserType());
            model.addAttribute("page", page);
            model.addAttribute("user", user);
        } catch (Exception e) {
            log.error("customerController.accountList", e);
        }

        return "modules/md/customerAccountList";
    }

    @RequiresPermissions("md:customer:view")
    @RequestMapping(value = "account/form")
    public String accountForm(User user, Model model) {
        User currentUser = UserUtils.getUser();
        if (user.getId() != null && user.getId() != 0) {
            //edit
            user = customerService.getAccount(user.getId());
        } else {
            if (StringUtils.isBlank(user.getName())) {
                //new
                CustomerAccountProfile profile = new CustomerAccountProfile(0);
                if (currentUser.isCustomer()) {
                    profile.setCustomer(currentUser.getCustomerAccountProfile().getCustomer());
                }
                profile.setOrderApproveFlag(0);
                user.setUserType(User.USER_TYPE_SUBCUSTOMER);
                user.setCustomerAccountProfile(profile);
            }
        }

        model.addAttribute("currentuser", currentUser);
        model.addAttribute("user", user);
        return "modules/md/customerAccountForm";
    }

    @RequiresPermissions("md:customer:edit")
    @RequestMapping(value = "account/save")
    public String saveAccount(User user, String oldLoginName, String newPassword, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        // 如果新密码为空，则不更换密码
        if (StringUtils.isNotBlank(newPassword)) {
            user.setPassword(SystemService.entryptPassword(newPassword));
        }
        if (!beanValidator(model, user)) {
            return accountForm(user, model);
        }
        if (!"true".equals(checkLoginName(oldLoginName, user.getLoginName(), null))) {
            addMessage(model, "保存客户" + (user.getUserType() == User.USER_TYPE_CUSTOMER ? "" : (user.getUserType() == User.USER_TYPE_SEARCH_CUSTOMER ? "查询" : "子")) + "帐号'" + user.getLoginName() + "'失败，登录名已存在");
            return accountForm(user, model);
        }
        if (user.getCustomerAccountProfile().getOrderApproveFlag() < 0) {
            user.getCustomerAccountProfile().setOrderApproveFlag(0);
        }

		/* 一个客户只有一个主账号
		if (user.getUserType() == User.USER_TYPE_CUSTOMER && true == customerService.hasOtherPrimaryAccount(user.getCustomerAccountProfile().getCustomer().getId(),user.getId()))
		{
			addMessage(model, "该客户已存在主帐号，不能添加多个主账号");
			return accountForm(user, model);
		}*/
		/* company
		customerService.clear();
		CustomerAccountProfile profile = user.getCustomerAccountProfile();
		Customer customer = customerService.get(profile.getCustomer().getId());
		profile.setCustomer(customer);
		user.setCompany(customer.getCompany());
		*/
        // 角色数据有效性验证，过滤不在授权内的角色


        Role r;
        if (user.getUserType() == User.USER_TYPE_SEARCH_CUSTOMER) {
            r = systemService.getRoleByEnname("SearchCustomer");
        } else {
            long roleId = 4l;
            if (user.getUserType() == User.USER_TYPE_SUBCUSTOMER) {
                roleId = 5l;
            }
            r = systemService.getRoleById(Objects.equals(user.getUserType(), User.USER_TYPE_CUSTOMER) ? 4l : 5l);
        }
        List<Role> roleList = systemService.findUserRoles(user);
        long count = roleList.stream().filter(i -> i.getId().equals(r.getId())).count();
        if(count == 0){
            roleList.add(r);
        }

        user.setRoleList(roleList);
        boolean isNew = user.getIsNewRecord();
        //save
        customerService.save(user);
        addMessage(redirectAttributes, "保存客户".concat(user.getUserType().equals(User.USER_TYPE_CUSTOMER) ? "" : "子").concat("账号人员'").concat(user.getLoginName()).concat("'成功"));
//		return "redirect:" + Global.getAdminPath() + "/md/customer/account/list?repage";
        Customer customer = user.getCustomerAccountProfile().getCustomer();
        return String.format("redirect:%s/md/customer/account/list?repage&customerAccountProfile.customer.id=%s&userType=0", Global.getAdminPath(), customer.getId());
    }

    @RequiresPermissions("md:customer:edit")
    @RequestMapping(value = "account/delete")
    public String delete(User user, RedirectAttributes redirectAttributes) {
        if (UserUtils.getUser().getId().equals(user.getId())) {
            addMessage(redirectAttributes, "停用帐号失败, 不允许停用当前帐号");
        } else if (User.isAdmin(user.getId())) {
            addMessage(redirectAttributes, "停用帐号失败, 不允许停用超级管理员帐号");
        } else {
            systemService.deleteUser(user);
            addMessage(redirectAttributes, "停用帐号[ " + user.getLoginName() + " ]成功");
        }
        Customer customer = null;
        try {
            customer = user.getCustomerAccountProfile().getCustomer();
        } catch (Exception e) {
        }
        if (customer != null && customer.getId() != null) {
            return String.format("redirect:%s/md/customer/account/list?repage&customerAccountProfile.customer.id=%s&userType=0", Global.getAdminPath(), customer.getId());

        } else {
            return "redirect:" + adminPath + "/md/customer/account/list?repage";
        }
    }

    @RequiresPermissions("md:customer:edit")
    @RequestMapping(value = "account/enable")
    public String enableAccount(User user, RedirectAttributes redirectAttributes) {
        systemService.enableUser(user);
        addMessage(redirectAttributes, "启用[ " + user.getLoginName() + " ]成功");
        try {
            CustomerAccountProfile profile = customerService.getCustomerAccountProfileByUserId(user.getId());
            if (profile != null) {
                return String.format("redirect:%s/md/customer/account/list?repage&customerAccountProfile.customer.id=%s&userType=0", Global.getAdminPath(), profile.getCustomer().getId());
            } else {
                return "redirect:" + adminPath + "/md/customer/account/list?repage";
            }
        } catch (Exception e) {
            addMessage(redirectAttributes, "启用[ " + user.getLoginName() + " ]失败," + e.getMessage());
            return "redirect:" + adminPath + "/md/customer/account/list?repage";
        }
    }

    //endregion 客户帐号

    //region 客户帐号管理 for 业务

    @RequiresPermissions("md:customer:view")
    @RequestMapping(value = {"user/list", "users"})
    public String customerAccountList(User user, HttpServletRequest request, HttpServletResponse response, Model model) {
        try {
            User cuser = UserUtils.getUser();
            if (cuser.isCustomer()) {
//				int orderApproveFlag = cuser.getCustomerAccountProfile().getOrderApproveFlag();
                CustomerAccountProfile profile = new CustomerAccountProfile();
                profile.setCustomer(cuser.getCustomerAccountProfile().getCustomer());
                profile.setOrderApproveFlag(cuser.getCustomerAccountProfile().getOrderApproveFlag());
                user.setCustomerAccountProfile(profile);
            } else if (cuser.isSalesPerson()) {
                user.setSalesId(cuser.getId());
            }
            user.setDelFlag(null);
            //Page<User> page = customerService.find(new Page<>(request,response), user);
            Page<User> page = customerService.findWithOutCustomerAccountProfile(new Page<>(request, response), user);
//			model.addAttribute("currentUserType", UserUtils.getUser().getUserType());
            List<Dict> dicts = MSDictUtils.getDictList("CustomerOffice");
            Dict customerOffice = new Dict("41");
            if (dicts != null && dicts.size() > 0) {
                customerOffice = dicts.get(0);
            }
            Role role = new Role();
            role.setDelFlag(0);
            role.setOffice(new Office(Long.valueOf(customerOffice.getValue())));//只筛选出客户的
            role.setUseable(1);
            model.addAttribute("allRoles", systemService.findRole(role));
            model.addAttribute("page", page);
            model.addAttribute("user", user);
        } catch (Exception e) {
            log.error("customerController.customerAccountList", e);
        }

        return "modules/md/customerUserList";
    }

    @RequiresPermissions("md:customer:view")
    @RequestMapping(value = "user/form")
    public String userForm(User user, Model model) {
        User currentUser = UserUtils.getUser();
        if (user.getId() != null && user.getId() != 0) {
            //edit
            user = customerService.getAccount(user.getId());
            List<Role> roles = systemService.findUserRoles(user);
            user.setRoleList(roles);
        } else {
            if (StringUtils.isBlank(user.getName())) {
                //new
                CustomerAccountProfile profile = new CustomerAccountProfile(0);
                if (currentUser.isCustomer()) {
                    profile.setCustomer(currentUser.getCustomerAccountProfile().getCustomer());
                }
                profile.setOrderApproveFlag(0);
                user.setUserType(User.USER_TYPE_SUBCUSTOMER);
                user.setCustomerAccountProfile(profile);
            }
        }

        model.addAttribute("currentuser", currentUser);
        model.addAttribute("user", user);
//		List<Dict> dicts = dictService.getDictListByIdFromCache("CustomerOffice");
        List<Dict> dicts = MSDictUtils.getDictList("CustomerOffice");
        Dict customerOffice = new Dict("41");
        if (dicts != null && dicts.size() > 0) {
            customerOffice = dicts.get(0);
        }
        Role role = new Role();
        role.setDelFlag(0);
        role.setOffice(new Office(Long.valueOf(customerOffice.getValue())));//只筛选出客户的
        role.setUseable(1);
        model.addAttribute("allRoles", systemService.findRole(role));
        return "modules/md/customerUserForm";
    }

    @RequiresPermissions("md:customer:edit")
    @RequestMapping(value = "user/save")
    public String saveUser(User user, String oldLoginName, String newPassword, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        // 如果新密码为空，则不更换密码
        if (StringUtils.isNotBlank(newPassword)) {
            user.setPassword(SystemService.entryptPassword(newPassword));
        }
        if (!beanValidator(model, user)) {
            return accountForm(user, model);
        }
        if (!"true".equals(checkLoginName(oldLoginName, user.getLoginName(), null))) {
            addMessage(model, "保存客户" + (Objects.equals(user.getUserType(), User.USER_TYPE_CUSTOMER) ? "" : "子") + "账号'" + user.getLoginName() + "'失败，登录名已存在");
            return accountForm(user, model);
        }
        if (user.getCustomerAccountProfile().getOrderApproveFlag() < 0) {
            user.getCustomerAccountProfile().setOrderApproveFlag(0);
        }

        boolean isNew = user.getIsNewRecord();
        //save
        customerService.save(user);
        addMessage(redirectAttributes, "保存客户".concat(user.getUserType().equals(User.USER_TYPE_CUSTOMER) ? "" : "子").concat("账号人员'").concat(user.getLoginName()).concat("'成功"));
//		return "redirect:" + Global.getAdminPath() + "/md/customer/account/list?repage";
        Customer customer = user.getCustomerAccountProfile().getCustomer();
        return String.format("redirect:%s/md/customer/user/list?repage&customerAccountProfile.customer.id=%s&userType=0", Global.getAdminPath(), customer.getId());
    }

    @RequiresPermissions("md:customer:edit")
    @RequestMapping(value = "user/stop")
    public String stopUser(User user, RedirectAttributes redirectAttributes) {
        if (UserUtils.getUser().getId().equals(user.getId())) {
            addMessage(redirectAttributes, "停用失败, 不允许停用当前用户");
        } else if (User.isAdmin(user.getId())) {
            addMessage(redirectAttributes, "停用失败, 不允许停用超级管理员用户");
        } else {
            systemService.deleteUser(user);
            addMessage(redirectAttributes, "停用成功");
            try {
                CustomerAccountProfile profile = customerService.getCustomerAccountProfileByUserId(user.getId());
                if (profile != null) {
                    return String.format("redirect:%s/md/customer/user/list?repage&customerAccountProfile.customer.id=%s&userType=0", Global.getAdminPath(), profile.getCustomer().getId());
                } else {
                    return "redirect:" + adminPath + "/md/customer/user/list?repage";
                }

            } catch (Exception e) {
                return "redirect:" + adminPath + "/md/customer/user/list?repage";
            }
        }
        return "redirect:" + adminPath + "/md/customer/user/list?repage";
    }

    @RequiresPermissions("md:customer:edit")
    @RequestMapping(value = "user/enable")
    public String enableUser(User user, RedirectAttributes redirectAttributes) {

        systemService.enableUser(user);
        addMessage(redirectAttributes, "启用成功");
        try {
            CustomerAccountProfile profile = customerService.getCustomerAccountProfileByUserId(user.getId());
            if (profile != null) {
                return String.format("redirect:%s/md/customer/user/list?repage&customerAccountProfile.customer.id=%s&userType=0", Global.getAdminPath(), profile.getCustomer().getId());
            } else {
                return "redirect:" + adminPath + "/md/customer/user/list?repage";
            }

        } catch (Exception e) {
            return "redirect:" + adminPath + "/md/customer/user/list?repage";
        }
    }

    //endregion 客户帐号 for 业务

    //region 服务价格管理

    @RequiresPermissions("md:customerprice:view")
    @RequestMapping(value = {"price/list", "price"})
    public String priceList(CustomerPrice customerPrice, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<CustomerPrice> page = new Page<>(request, response);
        if (customerPrice == null) {
            customerPrice = new CustomerPrice();
        }
        if (customerPrice.getProduct() == null) {
            customerPrice.setProduct(new Product());
        }
        if (customerPrice.getCustomer() == null || customerPrice.getCustomer().getId() <= 0) {
            customerPrice.setCustomer(new Customer());
            List<ServiceType> serviceTypes = serviceTypeService.findAllListIdsAndNames();
            //end
            model.addAttribute("page", page);
            model.addAttribute("serviceTypes", ServiceType.ServcieTypeOrdering.sortedCopy(serviceTypes));
            addMessage(model, "请选择客户");
            return "modules/md/customerPriceList";
        }
        User user = UserUtils.getUser();
        boolean errorFlag = false;
        if (user.isCustomer()) {
            if (user.getCustomerAccountProfile() != null && user.getCustomerAccountProfile().getCustomer() != null) {
                //登录用户的客户，防篡改
                customerPrice.setCustomer(user.getCustomerAccountProfile().getCustomer());
            } else {
                addMessage(model, "错误：登录超时，请退出后重新登录。");
                errorFlag = true;
            }
        }
        if (user.isSalesPerson()) {   //业务员
            customerPrice.getCustomer().setSales(user);
        } else if (user.isMerchandiser()) { //跟单员
            customerPrice.getCustomer().setMerchandiser(user);
        }
        if (errorFlag) {
            model.addAttribute("page", page);
            model.addAttribute("customerPrice", customerPrice);
            return "modules/md/customerPriceList";
        }
        // add on 2019-7-26 begin
       /* if (customerPrice.getFirstSearch() == 1 && customerPrice.getCustomer().getId() == null ) {
            customerPrice.setFirstSearch(0);
            return "modules/md/customerPriceList";
        }*/
        // add on 2019-7-26 end
        page = customerService.findPage(page, customerPrice);
        model.addAttribute("page", page);
        // mark on 2019-10-11
        //List<ServiceType> serviceTypes = serviceTypeService.findAllList();
        //调用微服务 只返回id 和 名称 start 2019-10-11
        List<ServiceType> serviceTypes = serviceTypeService.findAllListIdsAndNames();
        //end
        model.addAttribute("serviceTypes", ServiceType.ServcieTypeOrdering.sortedCopy(serviceTypes));
        model.addAttribute("customerPrice", customerPrice);
        return "modules/md/customerPriceList";
    }

    /**
     * 编辑网点单个产品价格
     * @param customerPrice
     * @param model
     * @return
     */
    @RequiresPermissions("md:customerprice:view")
    @RequestMapping(value = "price/form")
    public String priceForm(CustomerPrice customerPrice, String qCustomerId, String qCustomerName,
                            String qProductCategoryId, String qProductCategoryName, String qProductId,
                            String qProductName, String qFirstSearch, String examine, Model model) {
        if (customerPrice != null && customerPrice.getId() != null && customerPrice.getCustomer() == null) {

//            customerPrice = customerService.getPrice(customerPrice.getId(), null);
            // 调用微服务优化 update on 2020-06-04
            customerPrice = customerService.getPriceNew(customerPrice.getId(), null);
            model.addAttribute("warrantyStatus", customerPrice.getServiceType().getWarrantyStatus().getValue());
        }
        if (customerPrice == null) {
            addMessage(model, "价格不存在");
            model.addAttribute("canAction", false);
        } else {
            //参考价格
            List<ProductPrice> allPrices = productPriceService.findGroupList(Lists.newArrayList(customerPrice.getProduct().getId()), Lists.newArrayList(customerPrice.getServiceType().getId()), null, null, customerPrice.getCustomer().getId());
            List<HashMap<String, Object>> productPriceList = Lists.newArrayList();
            //切换为微服务
            Map<String, Dict> priceTypeMap = MSDictUtils.getDictMap("PriceType");
            for (ProductPrice productPrice : allPrices) {
                HashMap<String, Object> productPriceMap = new HashMap<>();
                productPriceMap.put("priceType", productPrice.getPriceType().getValue());
                productPriceMap.put("priceTypeName", priceTypeMap.get(productPrice.getPriceType().getValue()).getLabel());
                productPriceMap.put("standPrice", productPrice.getCustomerStandardPrice());
                productPriceMap.put("discountPrice", productPrice.getCustomerDiscountPrice());
                productPriceList.add(productPriceMap);
            }
            model.addAttribute("productPriceList", productPriceList);
            model.addAttribute("canAction", true);
        }
        model.addAttribute("customerId", qCustomerId);
        model.addAttribute("customerName", qCustomerName);
        model.addAttribute("productCategoryId", qProductCategoryId);
        model.addAttribute("productCategoryName", qProductCategoryName);
        model.addAttribute("productId", qProductId);
        model.addAttribute("productName", qProductName);
        model.addAttribute("customerPrice", customerPrice);
        model.addAttribute("qFirstSearch", qFirstSearch);
        if (StringUtils.isNotBlank(examine)) {
            return "modules/md/customerPriceForm";
        }
        return "modules/md/customerPriceFormNew";
    }

    @RequiresPermissions("md:customerprice:view")
    @RequestMapping(value = "price/insertForm")
    public String insertForm(CustomerPrice customerPrice, String qCustomerId, String qCustomerName,
                            String qProductCategoryId, String qProductCategoryName, String qProductId,
                            String qProductName, String qFirstSearch, Model model) {
        if (customerPrice != null) {
            //参考价格
            List<ProductPrice> allPrices = productPriceService.findGroupList(Lists.newArrayList(customerPrice.getProduct().getId()), Lists.newArrayList(customerPrice.getServiceType().getId()), null, null, customerPrice.getCustomer().getId());
            List<HashMap<String, Object>> productPriceList = Lists.newArrayList();
            //切换为微服务
            Map<String, Dict> priceTypeMap = MSDictUtils.getDictMap("PriceType");
            for (ProductPrice productPrice : allPrices) {
                HashMap<String, Object> productPriceMap = new HashMap<>();
                productPriceMap.put("priceType", productPrice.getPriceType().getValue());
                productPriceMap.put("priceTypeName", priceTypeMap.get(productPrice.getPriceType().getValue()).getLabel());
                productPriceMap.put("standPrice", productPrice.getCustomerStandardPrice());
                productPriceMap.put("discountPrice", productPrice.getCustomerDiscountPrice());
                productPriceList.add(productPriceMap);
            }
            model.addAttribute("productPriceList", productPriceList);
            model.addAttribute("canAction", true);
        }
        model.addAttribute("customerId", qCustomerId);
        model.addAttribute("customerName", qCustomerName);
        model.addAttribute("productCategoryId", qProductCategoryId);
        model.addAttribute("productCategoryName", qProductCategoryName);
        model.addAttribute("productId", qProductId);
        model.addAttribute("productName", qProductName);
        model.addAttribute("customerPrice", customerPrice);
        model.addAttribute("qFirstSearch", qFirstSearch);
        model.addAttribute("warrantyStatus", customerPrice.getServiceType().getWarrantyStatus().getValue());
        return "modules/md/customerPriceFormNew";
    }

    /**
     * 保存修改后的价格
     */
    @RequiresPermissions("md:customerprice:edit")
    @RequestMapping(value = "price/save")
    public String savePrice(CustomerPrice customerPrice, String qCustomerId, String qCustomerName,
                            String qProductCategoryId, String qProductCategoryName, String qProductId,
                            String qProductName, String qFirstSearch, String examine, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) throws UnsupportedEncodingException {
        if (!beanValidator(model, customerPrice)) {
            return priceForm(customerPrice, qCustomerId, qCustomerName, qProductCategoryId, qProductCategoryName, qProductId, qProductName, qFirstSearch,examine, model);
        }

        ServiceType serviceType = serviceTypeService.get(customerPrice.getServiceType().getId());
        if (serviceType.getWarrantyStatus().getValue().equals(ServiceType.WARRANTY_STATUS_IW)) {
            if (customerPrice.getPrice() == 0 || customerPrice.getDiscountPrice() == 0) {
                addMessage(model, "保存客户服务价格失败，" + customerPrice.getServiceType().getName() + "(保内)价格不能为0.");
                return priceForm(customerPrice, qCustomerId, qCustomerName, qProductCategoryId, qProductCategoryName, qProductId, qProductName, qFirstSearch,examine, model);
            }
        }

        customerPrice.preUpdate();
        if (customerPrice.getDelFlag() == CustomerPrice.DEL_FLAG_NORMAL) {
            customerPrice.setDelFlag(CustomerPrice.DEL_FLAG_AUDIT);//待审核
        }
        customerService.savePrice(customerPrice, false);
        addMessage(redirectAttributes, "保存客户服务价格'" + customerPrice.getCustomer().getName() + "'成功");

		/*String s= String.format("redirect:%s/md/customer/price/list?repage=true&customer.id=%s&customer.name=%s&productCategory.id=%s&productCategory.name=%s&product.id=%s&product.name=%s",
				Global.getAdminPath(),
				qCustomerId,
				URLEncoder.encode(qCustomerName),
				qProductCategoryId,
				URLEncoder.encode(qProductCategoryName),
				qProductId,
				URLEncoder.encode(qProductName));
				*/

        return String.format("redirect:%s/md/customer/price/list?repage=true&customer.id=%s&customer.name=%s&productCategory.id=%s&productCategory.name=%s&product.id=%s&product.name=%s&firstSearch=%s",
                Global.getAdminPath(),
                qCustomerId,
                URLEncoder.encode(qCustomerName, "utf-8"),
                qProductCategoryId,
                URLEncoder.encode(qProductCategoryName, "utf-8"),
                qProductId,
                URLEncoder.encode(qProductName, "utf-8"),
                qFirstSearch);
    }

    @RequiresPermissions("md:customerprice:edit")
    @RequestMapping(value = "price/saveNew")
    @ResponseBody
    public AjaxJsonEntity savePriceNew(CustomerPrice customerPrice, String qCustomerId, String qCustomerName,
                            String qProductCategoryId, String qProductCategoryName, String qProductId,
                            String qProductName, String qFirstSearch, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) throws UnsupportedEncodingException {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);

        ServiceType serviceType = serviceTypeService.get(customerPrice.getServiceType().getId());
        if (serviceType.getWarrantyStatus().getValue().equals(ServiceType.WARRANTY_STATUS_IW)) {
            if (customerPrice.getPrice() == 0 || customerPrice.getDiscountPrice() == 0) {
                ajaxJsonEntity.setSuccess(false);
                ajaxJsonEntity.setMessage("保存客户服务价格失败，" + customerPrice.getServiceType().getName() + "(保内)价格不能为0.");
                return ajaxJsonEntity;
            }
        }
        if (customerPrice.getDelFlag() == CustomerPrice.DEL_FLAG_NORMAL) {
            customerPrice.setDelFlag(CustomerPrice.DEL_FLAG_AUDIT);//待审核
        }
        boolean isNew;
        try {
            if (customerPrice.getId() == null) {
                customerPrice.preInsert();
                isNew = true;
            } else {
                customerPrice.preUpdate();
                isNew = false;
            }
            customerService.savePrice(customerPrice, isNew);
            ajaxJsonEntity.setMessage("保存客户服务价格'" + customerPrice.getCustomer().getName() + "'成功");
        } catch (Exception e) {
            log.error("保存客户服务价格:" + customerPrice.getCustomer().getName() + " 价格失败:" + e.getMessage(), e);
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("保存客户服务价格'" + customerPrice.getCustomer().getName() + " 价格失败");
        }
        return ajaxJsonEntity;
    }

    /**
     * 停用
     */
    /*@RequiresPermissions("md:customerprice:edit")
    @RequestMapping(value = "price/delete")
    public String deletePrice(Long id, String qCustomerId, String qCustomerName,
                              String qProductCategoryId, String qProductCategoryName, String qProductId,
                              String qProductName, String qFirstSearch, RedirectAttributes redirectAttributes) throws UnsupportedEncodingException {
        CustomerPrice customerPrice;
        try {
            customerPrice = customerService.getPrice(id, null);
            if (customerPrice == null) {
                addMessage(redirectAttributes, "读取价格失败");
                customerPrice = new CustomerPrice();
                customerPrice.setCustomer(new Customer(0l, ""));
            } else {
                customerService.deletePrice(id);
                addMessage(redirectAttributes, "停用客户价格成功");
            }
        } catch (Exception e) {
            addMessage(redirectAttributes, e.getMessage());
        }

        return String.format("redirect:%s/md/customer/price/list?repage=true&customer.id=%s&customer.name=%s&productCategory.id=%s&productCategory.name=%s&product.id=%s&product.name=%s&firstSearch=%s",
                Global.getAdminPath(),
                qCustomerId,
                URLEncoder.encode(qCustomerName, "utf-8"),
                qProductCategoryId,
                URLEncoder.encode(qProductCategoryName, "utf-8"),
                qProductId,
                URLEncoder.encode(qProductName, "utf-8"),
                qFirstSearch);

    }*/

    @RequiresPermissions("md:customerprice:edit")
    @RequestMapping(value = "price/deleteNew")
    @ResponseBody
    public AjaxJsonEntity deletePriceNew(Long id, String qCustomerId, String qCustomerName,
                              String qProductCategoryId, String qProductCategoryName, String qProductId,
                              String qProductName, String qFirstSearch, RedirectAttributes redirectAttributes) throws UnsupportedEncodingException {
        CustomerPrice customerPrice;
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        try {
            customerPrice = customerService.getPriceNew(id, null);
            if (customerPrice == null) {
                ajaxJsonEntity.setSuccess(false);
                ajaxJsonEntity.setMessage("读取价格失败");
                customerPrice = new CustomerPrice();
                customerPrice.setCustomer(new Customer(0l, ""));
            } else {
                customerService.deletePrice(id);
                ajaxJsonEntity.setMessage("停用客户价格成功");
            }
        } catch (Exception e) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage(e.getMessage());
        }
        return ajaxJsonEntity;
    }

    /**
     * 启用
     */
    /*@RequiresPermissions("md:customerprice:edit")
    @RequestMapping(value = "price/active")
    public String activePrice(Long id, String qCustomerId, String qCustomerName,
                              String qProductCategoryId, String qProductCategoryName, String qProductId,
                              String qProductName, String qFirstSearch, RedirectAttributes redirectAttributes) throws UnsupportedEncodingException {
//		CustomerPrice customerPrice = customerService.getPrice(id,1);
        User user = UserUtils.getUser();
        CustomerPrice p;
        try {
            p = customerService.getPrice(id, 1);
            if (p == null) {
                throw new RuntimeException("停用的价格记录不存在");
            }
            customerService.startPrice(p, user);
//		customerService.deletePrice(id);
            addMessage(redirectAttributes, "启用客户价格成功");
        } catch (Exception e) {
            addMessage(redirectAttributes, e.getMessage());
        }

        return String.format("redirect:%s/md/customer/price/list?repage=true&customer.id=%s&customer.name=%s&productCategory.id=%s&productCategory.name=%s&product.id=%s&product.name=%s&firstSearch=%s",
                Global.getAdminPath(),
                qCustomerId,
                URLEncoder.encode(qCustomerName, "utf-8"),
                qProductCategoryId,
                URLEncoder.encode(qProductCategoryName, "utf-8"),
                qProductId,
                URLEncoder.encode(qProductName, "utf-8"),
                qFirstSearch);
    }*/

    @RequiresPermissions("md:customerprice:edit")
    @RequestMapping(value = "price/activeNew")
    @ResponseBody
    public AjaxJsonEntity activePriceNew(Long id, String qCustomerId, String qCustomerName,
                              String qProductCategoryId, String qProductCategoryName, String qProductId,
                              String qProductName, String qFirstSearch, RedirectAttributes redirectAttributes) throws UnsupportedEncodingException {
        User user = UserUtils.getUser();
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        CustomerPrice p;
        try {
            p = customerService.getPriceNew(id, 1);
            if (p == null) {
                ajaxJsonEntity.setSuccess(false);
                ajaxJsonEntity.setMessage("停用的价格记录不存在");
            }
            customerService.startPrice(p, user);
            ajaxJsonEntity.setMessage("启用客户价格成功");
        } catch (Exception e) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage(e.getMessage());
            addMessage(redirectAttributes, e.getMessage());
        }
        return ajaxJsonEntity;
    }

    /**
     * 新增或编辑网点下所有价格
     * @return
     */
    @RequiresPermissions("md:customerprice:view")
    @RequestMapping(value = "price/forms")
    public String forms(CustomerPrices customerPrices, Model model) {
        Long id = null;
        List<CustomerPrice> prices = Lists.newArrayList();
        List<ProductPrice> allprices = productPriceService.findAllList();
        List<Product> products = productService.getCustomerProductList(customerPrices.getCustomer().getId());
        if (customerPrices != null && customerPrices.getCustomer() != null
                && customerPrices.getCustomer().getId() != null && customerPrices.getCustomer().getId() > 0) {
            id = customerPrices.getCustomer().getId();
        }
        if (id != null && id > 0 && customerPrices.isReload() == true) {
            //已维护的价格
            prices = customerService.getPrices(id, null);
//			Map<Product,Map<ServiceType,CustomerPrice>> map = prices.stream()
//					.collect(Collectors.groupingBy(CustomerPrice::getProduct,Collectors.groupingBy(CustomerPrice::getServiceType)
//					));

            Map<Product, List<CustomerPrice>> map = prices.stream()
                    .collect(
                            Collectors.groupingBy(CustomerPrice::getProduct)
                    );

            List<ServiceType> serviceTypes = serviceTypeService.findAllList();
            //一个产品下所有的服务价格
            List<CustomerPrice> defaultPrices = Lists.newArrayList();
            for (ServiceType serviceType : serviceTypes) {
                CustomerPrice price = new CustomerPrice();
                price.setServiceType(serviceType);
                price.setPrice(0.0);
                price.setDiscountPrice(0.0);
                price.setBlockedPrice(0.0);
                price.setDelFlag(1);
                defaultPrices.add(price);
            }
            //所有服务类型id
            Set<Long> allTypeSet = serviceTypes.stream().map(m -> m.getId()).collect(Collectors.toSet());
            //已有价格的服务类型id
            final Set<Long> prodServiceTypes = Sets.newHashSet();

            if (map == null || map.size() == 0) {//客户下产品都未设置价格
                for (Product p : products) {
                    List<CustomerPrice> list = new ArrayList<CustomerPrice>();
                    prodServiceTypes.clear();
                    allprices.stream().filter(t -> Objects.equals(t.getProduct().getId(), p.getId()))
                            .forEach(item -> {
                                prodServiceTypes.add(item.getServiceType().getId());
                                CustomerPrice price = new CustomerPrice();
                                price.setServiceType(item.getServiceType());
                                price.setPrice(item.getCustomerStandardPrice());
                                price.setDiscountPrice(item.getCustomerDiscountPrice());
                                price.setBlockedPrice(0.0);
                                list.add(price);
                            });
                    //该产品未定义服务价格的
                    Set<Long> noset = Sets.difference(allTypeSet, prodServiceTypes);
                    if (noset != null && noset.size() > 0) {
                        noset.stream().forEach(t -> {
                            ServiceType st = serviceTypes.stream().filter(m -> Objects.equals(m.getId(), t)).findFirst().orElse(null);
                            if (st != null) {
//								ProductPrice productPrice = allprices.stream().filter(m->Objects.equals(m.getProduct().getId(),p.getId())
//										&&Objects.equals(m.getServiceType().getId(),t))
//										.findFirst().orElse(null);
                                CustomerPrice price = new CustomerPrice();
                                price.setServiceType(st);
                                price.setPrice(0.0);
                                price.setDiscountPrice(0.0);
                                price.setBlockedPrice(0.0);
                                price.setDelFlag(1);
                                list.add(price);
                            }
                        });
                    }
//					Collections.copy(list, defaultPrices);
//					map.put(p,CustomerPrice.CustomerPriceOrdering.sortedCopy(list));
                    map.put(p, list);

                }
            } else { //有部分产品设置了价格
                for (Map.Entry<Product, List<CustomerPrice>> e : map.entrySet()) {
                    Set<Long> pServiceTypes = e.getValue().stream().map(t -> t.getServiceType().getId()).collect(Collectors.toSet());
                    Set<Long> noset = Sets.difference(allTypeSet, pServiceTypes);
                    noset.stream().forEach(t -> {
                        ServiceType st = serviceTypes.stream().filter(m -> Objects.equals(m.getId(), t)).findFirst().orElse(null);
                        if (st != null) {
                            CustomerPrice price = new CustomerPrice();
                            price.setServiceType(st);
                            price.setPrice(0.0);
                            price.setDiscountPrice(0.0);
                            price.setBlockedPrice(0.0);
                            price.setDelFlag(1);
                            e.getValue().add(price);
                        }
                    });
					/*
					List<CustomerPrice> list = new ArrayList<CustomerPrice>(defaultPrices);
					list.removeAll(e.getValue());
					e.getValue().addAll(list);//并集
					list = new ArrayList<CustomerPrice>(e.getValue());
					e.setValue(CustomerPrice.CustomerPriceOrdering.sortedCopy(list));//按servicetype.id排序
					*/
                }
                //未设定价格的产品
                List<Product> newProducts = new ArrayList<Product>(products);
                newProducts.removeAll(map.keySet());
                if (newProducts != null && newProducts.size() > 0) {
                    for (Product p : newProducts) {
                        List<CustomerPrice> list = new ArrayList<CustomerPrice>();
//						map.put(p,CustomerPrice.CustomerPriceOrdering.sortedCopy(list));
                        allTypeSet.stream().forEach(m -> {
                            ProductPrice prodPrice = allprices.stream().filter(t -> Objects.equals(t.getProduct().getId(), p.getId())
                                    && Objects.equals(t.getServiceType().getId(), m)).findFirst().orElse(null);
                            CustomerPrice price = new CustomerPrice();
                            if (prodPrice != null) {
                                price.setServiceType(prodPrice.getServiceType());
                                price.setPrice(prodPrice.getCustomerStandardPrice());
                                price.setDiscountPrice(prodPrice.getCustomerDiscountPrice());
                                price.setBlockedPrice(0.0);
                            } else {
                                price.setServiceType(new ServiceType(m));
                                price.setPrice(0.0);
                                price.setDiscountPrice(0.0);
                                price.setBlockedPrice(0.0);
                                price.setDelFlag(1);
                            }
                            list.add(price);
                        });

                        if (list == null || list.size() == 0) {
                            map.put(p, CustomerPrice.CustomerPriceOrdering.sortedCopy(defaultPrices));
                        } else {
                            map.put(p, list);
                        }
                    }
                }
            }
            model.addAttribute("serviceTypes", ServiceType.ServcieTypeOrdering.sortedCopy(serviceTypes));
            model.addAttribute("priceMap", map);
            model.addAttribute("products", products);
        }
        model.addAttribute("customerPrices", customerPrices);
        return "modules/md/customerPriceForms";
    }

	/*
	@RequiresPermissions("md:customerprice:edit")
	@RequestMapping(value = "price/savePrices")
	public String savePrices(CustomerPrices entity, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {

		if (!beanValidator(model, entity)) {
			return forms(entity, model);
		}

		List<CustomerPrice> prices = entity.getPrices();
		if(prices==null || prices.size()==0){
			addMessage(model, "错误：请设定服务价格");
			return forms(entity,model);
		}
		final User user = UserUtils.getUser();
		final Date date = new Date();
		prices = prices.stream().filter(t->t.getPrice()>0)
				.collect(Collectors.toList());

		prices.forEach( t -> {
			t.setCreateBy(user);
			t.setCreateDate(date);
		});
		entity.setPrices(prices);
		try {
			customerService.insertPrices(entity);
			addMessage(model, "保存客户：" + entity.getCustomer().getName() + " 价格成功");
			return "redirect:" + Global.getAdminPath() + "/md/customer/price/list?repage=true&customer.id="+entity.getCustomer().getId();
		} catch (Exception e) {
			entity.setReload(false);
			addMessage(model, "保存客户：" + entity.getCustomer().getName() + "' 价格失败:" + e.getMessage());
			return forms(entity,model);
		}
	}*/

    /**
     * 编辑客户某产品下各服务价格
     * @param customerPrices
     */
    @RequiresPermissions("md:customerprice:edit")
    @RequestMapping(value = "price/productform")
    public String productForm(CustomerPrices customerPrices, String qCustomerId, String qCustomerName,
                              String qProductCategoryId, String qProductCategoryName, String qProductId,
                              String qProductName, String qFirstSearch, Model model) {
        Boolean canAction = true;
        model.addAttribute("customerId", qCustomerId);
        model.addAttribute("customerName", qCustomerName);
        model.addAttribute("productCategoryId", qProductCategoryId);
        model.addAttribute("productCategoryName", qProductCategoryName);
        model.addAttribute("productId", qProductId);
        model.addAttribute("productName", qProductName);
        model.addAttribute("qFirstSearch", qFirstSearch);

        if (customerPrices.getCustomer() == null || customerPrices.getCustomer().getId() == null ||
                customerPrices.getProduct() == null || customerPrices.getProduct().getId() == null) {
            addMessage(model, "参数：客户或产品错误.");
            model.addAttribute("canAction", false);
            return "modules/md/customerPriceProductForm";
        }

        Long cid = customerPrices.getCustomer().getId();
        Customer customer = customerService.getFromCache(cid);
        if (customer == null) {
            addMessage(model, "读取客户信息失败，请返回并重新打开。");
            model.addAttribute("canAction", false);
            return "modules/md/customerPriceProductForm";
        }
        customerPrices.setCustomer(customer);

        Long pid = customerPrices.getProduct().getId();// 获取客户价格下的产品
        Product product = productService.getProductByIdFromCache(pid);
        if (product == null) {
            addMessage(model, "读取产品信息失败，请返回并重新打开。");
            model.addAttribute("canAction", false);
            return "modules/md/customerPriceProductForm";
        }
        customerPrices.setProduct(product);
        //mark on 2019-10-12
        //List<ServiceType> serviceTypes = serviceTypeService.findAllList();
        //调用微服务获取服务类型,返回id,名称,warrantyStatus,code start 2019-10-12
        List<ServiceType> serviceTypes = serviceTypeService.findAllListIdsAndNamesAndCodes();
        //end
        if (serviceTypes == null || serviceTypes.size() == 0) {
            addMessage(model, "读取服务类型信息失败，请返回并重新打开。");
            model.addAttribute("canAction", false);
            return "modules/md/customerPriceProductForm";
        }
        //默认价格 标准价
        List<ProductPrice> allPrices = productPriceService.findGroupList(Lists.newArrayList(pid), null, null, null, customer.getId());
        // 获取此客户下的服务项目ids
        List<Long> allServiceTypeIdList = allPrices.stream().map(t -> t.getServiceType().getId()).distinct().collect(Collectors.toList());
        if (allPrices == null || allPrices.size() == 0) {
            addMessage(model, "产品参考价格为空，请先维护产品参考价格。");
            model.addAttribute("canAction", false);
            return "modules/md/customerPriceProductForm";
        }
//		List<CustomerPrice> prices = customerService.getPricesFromCache(cid);//缓存中没有待审核价格
        // 所有价格
        List<CustomerPrice> prices = customerService.getPrices(cid, null);//包含待审核价格,停用
        // 获取包含当前产品的价格
        final List<CustomerPrice> hasprices = prices != null && !prices.isEmpty() ? prices.stream().filter(t -> Objects.equals(t.getProduct().getId(), pid))
                .collect(Collectors.toList()) : null;
        List<CustomerPrice> list = Lists.newArrayList();
        // 遍历服务项目
        allServiceTypeIdList.forEach(t -> {
            // 筛选出匹配服务项目的价格
            List<ProductPrice> pp = allPrices.stream().filter(p -> Objects.equals(p.getServiceType().getId(), t)).collect(Collectors.toList());
            // 筛选第一个匹配的服务项目
            ServiceType st = serviceTypes.stream().filter(m -> Objects.equals(m.getId(), t)).findFirst().orElse(null);
            if (st != null) {
                // 筛选第一个匹配的客户价格
                CustomerPrice price = hasprices != null && !hasprices.isEmpty() ? hasprices.stream().filter(s -> Objects.equals(s.getServiceType().getId(), t)).findFirst().orElse(null) : null;
                // 匹配到了服务项目下的客户价格不为空
                if (price == null) {
                    price = new CustomerPrice();
                }
                price.setServiceType(st);

                List<HashMap<String, Object>> productPriceList = Lists.newArrayList();
                //切换为微服务
                Map<String, Dict> priceTypeMap = MSDictUtils.getDictMap("PriceType");
                for (ProductPrice productPrice : pp) {
                    HashMap<String, Object> productPriceMap = new HashMap<>();
                    productPriceMap.put("priceType", productPrice.getPriceType().getValue());
                    productPriceMap.put("priceTypeName", priceTypeMap.get(productPrice.getPriceType().getValue()).getLabel());
                    productPriceMap.put("standPrice", productPrice.getCustomerStandardPrice());
                    productPriceMap.put("discountPrice", productPrice.getCustomerDiscountPrice());
                    productPriceList.add(productPriceMap);
                }
                price.setProductPriceList(productPriceList);

                list.add(price);
            }
        });

//		customerPrices.getPrices().addAll(list);
        customerPrices.setPrices(list);

        model.addAttribute("canAction", true);
        model.addAttribute("customerPrices", customerPrices);
//        return "modules/md/customerPriceProductForm";
        return "modules/md/customerPriceProductFormNew";
    }

    /**
     * 保存客户下某产品的所有服务价格
     * 新增加：判断价格是否和默认价格一致，不一致要审核
     * 修改：都要审核
     */
    @RequiresPermissions("md:customerprice:edit")
    @RequestMapping(value = "price/saveProductPrices")
    public String saveProductPrices(CustomerPrices entity, String qCustomerId, String qCustomerName,
                                    String qProductCategoryId, String qProductCategoryName, String qProductId,
                                    String qProductName, String qFirstSearch, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {

        if (!beanValidator(model, entity)) {
            return productForm(entity, qCustomerId, qCustomerName, qProductCategoryId, qProductCategoryName, qProductId, qProductName, qFirstSearch, model);
        }

        List<CustomerPrice> prices = entity.getPrices().stream().filter(t -> t.getServiceType() != null).collect(Collectors.toList());
        if (prices == null || prices.size() == 0) {
            addMessage(model, "错误：请设定服务价格");
            return productForm(entity, qCustomerId, qCustomerName, qProductCategoryId, qProductCategoryName, qProductId, qProductName, qFirstSearch, model);
        }
		/*
		for (CustomerPrice item : prices) {
			if (item.getServiceType().getWarrantyStatus() != null && item.getServiceType().getWarrantyStatus().getValue() != null &&
					item.getServiceType().getWarrantyStatus().getValue().equals(ServiceType.WARRANTY_STATUS_IW) && (
					item.getPrice() == 0 || item.getDiscountPrice() == 0)) {
				addMessage(model, "保存客户价格失败，" + item.getServiceType().getName() + "(保内)的价格不能为0.");
				return productForm(entity, qCustomerId, qCustomerName, qProductCategoryId, qProductCategoryName, qProductId, qProductName, model);
			}
		}*/

        final User user = UserUtils.getUser();
        entity.setCreateBy(user);
        entity.setCreateDate(new Date());
        try {
            customerService.saveProductPrices(entity);
            addMessage(model, "保存客户：" + entity.getCustomer().getName() + " 价格成功");
            return String.format("redirect:%s/md/customer/price/list?repage=true&customer.id=%s&customer.name=%s&productCategory.id=%s&productCategory.name=%s&product.id=%s&product.name=%s&firstSearch=%s",
                    Global.getAdminPath(),
                    qCustomerId,
                    URLEncoder.encode(qCustomerName, "utf-8"),
                    qProductCategoryId,
                    URLEncoder.encode(qProductCategoryName, "utf-8"),
                    qProductId,
                    URLEncoder.encode(qProductName, "utf-8"),
                    qFirstSearch);
        } catch (Exception e) {
            addMessage(model, "保存客户：" + entity.getCustomer().getName() + "' 价格失败:" + e.getMessage());
            return productForm(entity, qCustomerId, qCustomerName, qProductCategoryId, qProductCategoryName, qProductId, qProductName, qFirstSearch, model);
        }

    }

    @RequiresPermissions("md:customerprice:edit")
    @RequestMapping(value = "price/saveProductPricesNew")
    @ResponseBody
    public AjaxJsonEntity saveProductPricesNew(CustomerPrices entity, String qCustomerId, String qCustomerName,
                                    String qProductCategoryId, String qProductCategoryName, String qProductId,
                                    String qProductName, String qFirstSearch, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        if (!beanValidator(model, entity)) {
            try {
                productForm(entity, qCustomerId, qCustomerName, qProductCategoryId, qProductCategoryName, qProductId, qProductName, qFirstSearch, model);
            } catch (Exception e) {
                ajaxJsonEntity.setSuccess(false);
                ajaxJsonEntity.setMessage(e.getMessage());
            }
        }

        List<CustomerPrice> prices = entity.getPrices().stream().filter(t -> t.getServiceType() != null).collect(Collectors.toList());
        if (prices == null || prices.size() == 0) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("错误：请设定服务价格");
            productForm(entity, qCustomerId, qCustomerName, qProductCategoryId, qProductCategoryName, qProductId, qProductName, qFirstSearch, model);
        }

        final User user = UserUtils.getUser();
        entity.setCreateBy(user);
        entity.setCreateDate(new Date());
        try {
            customerService.saveProductPrices(entity);
            ajaxJsonEntity.setMessage("保存客户：" + entity.getCustomer().getName() + " 价格成功");
        } catch (Exception e) {
            log.error("保存客户：" + entity.getCustomer().getName() + " 价格失败:" + e.getMessage(), e);
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("保存客户：" + entity.getCustomer().getName() + "' 价格失败:" + e.getMessage());
        }
        return ajaxJsonEntity;
    }

    /**
     * 待审核价格列表
     * @param customerPrice
     * @param request
     * @param response
     * @param model
     * @return
     */
    @RequiresPermissions("md:customerprice:view")
    @RequestMapping(value = "price/approvelist")
    public String priceApprovelist(CustomerPrice customerPrice, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<CustomerPrice> page = new Page<CustomerPrice>(request, response);
        String repage = request.getParameter("repage");
        if (request.getMethod().equalsIgnoreCase("post") || (repage != null && repage.equalsIgnoreCase("true"))) {
            page = customerService.findApprovePricePage(page, customerPrice);
        }
        model.addAttribute("page", page);
        //List<ServiceType> serviceTypes = serviceTypeService.findAllList();
        //model.addAttribute("serviceTypes", ServiceType.ServcieTypeOrdering.sortedCopy(serviceTypes));
        model.addAttribute("customerPrice", customerPrice);
        return "modules/md/customerPriceApproveList";
    }

    /**
     * 审核服务价格
     *
     * @param ids  价格id
     * @param response
     * @return
     */
    @RequiresPermissions("md:customerprice:approve")
    @ResponseBody
    @RequestMapping(value = "price/approve")
    public AjaxJsonEntity approvePrice(@RequestParam String ids, HttpServletResponse response) {
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        User user = UserUtils.getUser();
        if (user == null || user.getId() == null) {
            result.setSuccess(false);
            result.setMessage("登录超时，请重新登录。");
            return result;
        }
        if (StringUtils.isBlank(ids)) {
            result.setSuccess(false);
            result.setMessage("审核服务价格失败:未传递参数，请选择要审核的选项。");
            return result;
        }
        List<String> lstids;
        List<Long> lids;
        try {
            //字符转字符List
            lstids = Arrays.asList(ids.split(","));
            //List<String> -> List<Long>
            lids = lstids.stream().map(t -> Long.valueOf(t)).collect(Collectors.toList());
            customerService.approvePrices(lids, user.getId());
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("审核服务价格时发生异常:" + e.getMessage());
        }

        return result;
    }

    /**
     * 停用服务价格(ajax)
     *
     * @param ids  价格id
     * @param response
     * @return

     @RequiresPermissions("md:customerprice:approve")
     @ResponseBody
     @RequestMapping(value = "price/stop")
     public AjaxJsonEntity stopPrice(@RequestParam String ids,
     HttpServletResponse response)
     {
     response.setContentType("application/json; charset=UTF-8");
     AjaxJsonEntity result = new AjaxJsonEntity(true);
     if (StringUtils.isBlank(ids))
     {
     result.setSuccess(false);
     result.setMessage("停用服务价格失败:未传递参数，请选择要停用的选项。");
     return result;
     }
     List<String> lstids;
     List<Long> lids;
     try
     {
     User user = UserUtils.getUser();
     //字符转字符List
     lstids = Arrays.asList(ids.split(","));
     //List<String> -> List<Long>
     lids = lstids.stream().map(t->Long.valueOf(t)).collect(Collectors.toList());
     customerService.stopPrices(lids,user.getId());
     } catch (Exception e)
     {
     result.setSuccess(false);
     result.setMessage("停用服务价格时发生异常:" + e.getMessage());
     }

     return result;
     }
     */
    /**
     * 启用服务价格
     *
     * @param id  价格id
     * @param response
     * @return

     @RequiresPermissions("md:customerprice:approve")
     @ResponseBody
     @RequestMapping(value = "price/start")
     public AjaxJsonEntity startPrice(@RequestParam Long id, HttpServletResponse response)
     {
     response.setContentType("application/json; charset=UTF-8");
     AjaxJsonEntity result = new AjaxJsonEntity(true);
     if (id==null || id<=0)
     {
     result.setSuccess(false);
     result.setMessage("启用服务价格失败:参数无效。");
     return result;
     }

     try
     {
     User user = UserUtils.getUser();
     customerService.startPrice(id,user);
     } catch (Exception e)
     {
     result.setSuccess(false);
     result.setMessage("停用服务价格时发生异常:" + e.getMessage());
     }

     return result;
     }
     */

    //endregion 服务价格管理


}
