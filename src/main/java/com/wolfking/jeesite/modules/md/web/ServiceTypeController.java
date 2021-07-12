/**
 * Copyright &copy; 2012-2013 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.wolfking.jeesite.modules.md.web;

import com.google.common.collect.Lists;
import com.wolfking.jeesite.common.config.Global;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.ServiceType;
import com.wolfking.jeesite.modules.md.service.ServiceTypeService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 服务类型Controller
 * 
 * @author
 * @version
 */
@Controller
@RequestMapping(value = "${adminPath}/md/servicetype")
public class ServiceTypeController extends BaseController {

	@Autowired
	private ServiceTypeService serviceTypeService;

	@ModelAttribute
	public ServiceType get(@RequestParam(required = false) Long id) {
		if (id != null) {
			return serviceTypeService.get(id);
		} else {
			return new ServiceType();
		}
	}

	@RequiresPermissions("md:servicetype:view")
	@RequestMapping(value = { "list", "" })
	public String list(ServiceType serviceType, HttpServletRequest request, HttpServletResponse response, Model model) {
		Page<ServiceType> page = serviceTypeService.find(new Page<ServiceType>(request, response), serviceType);
		model.addAttribute("page", page);
		return "modules/md/serviceTypeList";
	}

	@RequiresPermissions("md:servicetype:view")
	@RequestMapping(value = "form")
	public String form(ServiceType serviceType, Model model) {
		model.addAttribute("serviceType", serviceType);
		return "modules/md/serviceTypeForm";
	}

	@RequiresPermissions("md:servicetype:edit")
	@RequestMapping(value = "save")
	// @Valid
	public String save(ServiceType serviceType, String oldCode, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
		if (Global.isDemoMode()) {
			addMessage(redirectAttributes, "演示模式，不允许操作！");
			return "redirect:" + Global.getAdminPath() + "/md/servicetype?repage&name=" + serviceType.getName();
		}

		if (serviceType.getId() != null) {
			if (!"true".equals(checkServiceCode(oldCode, serviceType.getCode()))) {
				addMessage(model, "服务类型'" + serviceType.getCode() + "'已存在");
				return form(serviceType, model);
			}
		}

		serviceType.setUnit(10);  //10--RMB

		if (!beanValidator(model, serviceType)) {
			return form(serviceType, model);
		}
		serviceTypeService.save(serviceType);
		addMessage(redirectAttributes, "保存服务类型'" + serviceType.getName() + "'成功");
		return "redirect:" + Global.getAdminPath() + "/md/servicetype?repage";
	}

	@RequiresPermissions("md:servicetype:edit")
	@RequestMapping(value = "ajax/save")
	@ResponseBody
	public AjaxJsonEntity save(ServiceType serviceType, String oldCode,Model model) {

		AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);

		if (Global.isDemoMode()) {
			ajaxJsonEntity.setMessage("演示模式，不允许操作！");
			ajaxJsonEntity.setSuccess(false);
		}

		if (serviceType.getId() != null) {
			if (!"true".equals(checkServiceCode(oldCode, serviceType.getCode()))) {
				ajaxJsonEntity.setSuccess(false);
				ajaxJsonEntity.setMessage("服务类型'" + serviceType.getCode() + "'已存在");

			}
		}
		serviceType.setUnit(10);  //10--RMB

		if (!beanValidator(model, serviceType)) {
			ajaxJsonEntity.setSuccess(false);
		}
		serviceTypeService.save(serviceType);
		ajaxJsonEntity.setMessage("保存服务类型'" + serviceType.getName() + "'成功");
		return ajaxJsonEntity;
	}
	@RequiresPermissions("md:servicetype:edit")
	@RequestMapping(value = "delete")
	public String delete(ServiceType serviceType, RedirectAttributes redirectAttributes) {
		if (Global.isDemoMode()) {
			addMessage(redirectAttributes, "演示模式，不允许操作！");
			return "redirect:" + Global.getAdminPath() + "/md/servicetype?repage";
		}
		serviceTypeService.delete(serviceType);
		addMessage(redirectAttributes, "删除服务类型成功");
		return "redirect:" + Global.getAdminPath() + "/md/servicetype?repage";
	}

	@ResponseBody
	@RequiresPermissions("md:servicetype:edit")
	@RequestMapping(value = "checkLoginName")
	public String checkServiceCode(String oldLoginName, String code) {
		if (code != null && code.equals(oldLoginName)) {
			return "true";
		} else if (code != null && !serviceTypeService.existServiceName(code)) {
			return "true";
		}
		return "服务类型名已存在";
	}

	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "treeData")
	public List<Map<String, Object>> treeData(@RequestParam(required = false) String filter, HttpServletRequest request, HttpServletResponse response) {
		response.setContentType("application/json; charset=UTF-8");
		List<Map<String, Object>> mapList = Lists.newArrayList();
		//按产品读取产品所涵盖的服务
		/*if(StringUtils.isNotBlank(filter)){
			String[] paras = filter.split(",");
			Page<CustomerPrice> page = new Page<CustomerPrice>();
			page.setPageNo(1);
			page.setPageSize(1000);
			CustomerPrice price = new CustomerPrice();
			price.setProduct(new Product(paras[0]));
			if(paras.length>1){
				price.setCustomer(new Customer(paras[1]));
			}
			String userType = UserUtils.getUser().getUserType();
			if(userType.equalsIgnoreCase(User.USER_TYPE_CUSTOMER) || (userType.equalsIgnoreCase(User.USER_TYPE_SUBCUSTOMER)) )
			{
				page = customerPriceService.find(page, price,true);
			}
			else{
				page = customerPriceService.find(page, price);
			}
//			if(StringUtils.isNotBlank(customerId)){
//				price.setCustomer(new Customer(customerId));
//			}
			
			List<CustomerPrice> list = page.getList();
			for (int i = 0; i < list.size(); i++) {
				CustomerPrice e = list.get(i);
				Map<String, Object> map = Maps.newHashMap();
				map.put("id", e.getServiceType().getId());
				map.put("pId", "");
				map.put("name", e.getServiceType().getName());
				map.put("standPrice", e.getPrice());
				map.put("discountPrice", e.getDiscountPrice());
				map.put("blockedPrice", e.getBlockedPrice());
				mapList.add(map);
			}
		}
		else{
			List<ServiceType> list = serviceTypeService.findAllList();
			for (int i = 0; i < list.size(); i++) {
				ServiceType e = list.get(i);
				Map<String, Object> map = Maps.newHashMap();
				map.put("id", e.getId());
				map.put("pId", "");
				map.put("name", e.getName());
				map.put("standPrice", 0);
				map.put("discountPrice", 0);
				map.put("blockedPrice", 0);
				map.put("engineerPrice", 0);
				map.put("engineerDiscountPrice", 0);
				map.put("sort", 0);
				mapList.add(map);
			}
		}*/
		return mapList;
	}

	/**
	 * 获得某工单类型下的服务项目列表
	 * @param orderType 工单类型
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "ofOrderType")
	public AjaxJsonEntity getListofOrderType(@RequestParam Integer orderType,@RequestParam String orderTypeName, HttpServletRequest request, HttpServletResponse response) {
		response.setContentType("application/json; charset=UTF-8");
		if(orderType == null || orderType <=0){
			return AjaxJsonEntity.fail("读取服务项目失败：参数错误",null);
		}
		List<ServiceType> list = serviceTypeService.findListOfOrderType(orderType);
		if(CollectionUtils.isEmpty(list)){
			return AjaxJsonEntity.fail("服务类型:" + orderTypeName + "未维护服务项目",null);
		}
		return AjaxJsonEntity.success("",list);
	}


}
