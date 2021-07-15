/**
 * Copyright &copy; 2012-2013 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.wolfking.jeesite.modules.md.web;

import com.google.common.collect.Lists;
import com.wolfking.jeesite.common.config.Global;
import com.wolfking.jeesite.common.exception.OrderException;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.*;
import com.wolfking.jeesite.modules.md.entity.viewModel.ServicePointModel;
import com.wolfking.jeesite.modules.md.entity.viewModel.ServicePrices;
import com.wolfking.jeesite.modules.md.service.*;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providermd.service.MSServicePointPriceService;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.wolfking.jeesite.modules.md.service.ServicePointService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 安维网点价格
 * 
 * @author Ryan Lu
 * @version 2017-5-2
 */
@Slf4j
@Controller
@RequestMapping(value = "${adminPath}/md/serviceprice")
public class ServicePriceController extends BaseController {

	@Autowired
	private ServicePointService pointService;

	@Autowired
	private ServiceTypeService serviceTypeService;

	@Autowired
	private ProductService productService;

	@Autowired
	private ProductPriceService productPriceService;

	@Autowired
	private ServicePointPriceService servicePointPriceService;

	@Autowired
	private ServicePointService servicePointService;

	@Autowired
	private MSServicePointPriceService msServicePointPriceService;

//	@ModelAttribute
//	public ServicePrice get(@RequestParam(required = false) Long id) {
//		if (id != null && id>0) {
//			ServicePrice servicePrice = pointService.getPrice(id);
//			//serviceType
//			ServiceType serviceType = serviceTypeService.getFromCache(servicePrice.getServiceType().getId());
//			if(serviceType != null){
//				servicePrice.setServiceType(serviceType);
//			}
//			return servicePrice;
//		} else {
//			ServicePrice servicePrice = new ServicePrice();
//			servicePrice.setProduct(new Product());
//			servicePrice.setServicePoint(new ServicePoint());
//			return servicePrice;
//		}
//	}

	/*@RequiresPermissions("md:serviceprice:view")
	@RequestMapping(value = { "list", "" })
	public String list(ServicePrice servicePrice, HttpServletRequest request, HttpServletResponse response, Model model) {
		Page<ServicePrice> page = new Page<>(request, response);
		String firstSearch = request.getParameter("firstSearch");
//		System.out.println(firstSearch);
		if (StringUtils.isNotBlank(firstSearch)) {
			// page = pointService.findPage(page, servicePrice);  // mark on 2020-3-13
			page = servicePointPriceService.findPage(page, servicePrice); //add on 2020-3-13
		}
		model.addAttribute("page", page);
		//mark on 2019-10-11
		//List<ServiceType> serviceTypes = serviceTypeService.findAllList();
		//调用微服务 只返回 id 和 名称 start 2019-10-11
		List<ServiceType> serviceTypes = serviceTypeService.findAllListIdsAndNames();
		//end
		model.addAttribute("serviceTypes", ServiceType.ServcieTypeOrdering.sortedCopy(serviceTypes));
		model.addAttribute("servicePrice",servicePrice);
		model.addAttribute("firstSearch","1");
		return "modules/md/servicePriceList";
	}*/

	@RequiresPermissions("md:serviceprice:view")
	@RequestMapping(value = "selectPrice")
	public String selectPrice(ServicePrice servicePrice, Long id, String servicePointNo, String servicePointName, String primaryName, String contactInfo, String customizePriceFlag, String useDefaultPrice,Integer degree,Integer serviceRemotePriceFlag,String remotePriceFlag,String remotePriceType
			, Long productCategoryId, Long productId, HttpServletRequest request, HttpServletResponse response, Model model) {
		model.addAttribute("servicePointNo", servicePointNo);
		model.addAttribute("servicePointName", servicePointName);
		model.addAttribute("primaryName", primaryName);
		model.addAttribute("contactInfo", contactInfo);
		model.addAttribute("customizePriceFlag", customizePriceFlag);// 是否标准价 0是 1否
		model.addAttribute("useDefaultPrice", useDefaultPrice);
		model.addAttribute("serviceRemotePriceFlag", serviceRemotePriceFlag);//价格类型  0服务价格 1远程价格
		model.addAttribute("remotePriceFlag", remotePriceFlag);
		model.addAttribute("remotePriceType", remotePriceType);
		model.addAttribute("servicePointId", id);
		model.addAttribute("degree", degree);
		model.addAttribute("productId", productId);
		Page<ServicePrice> page = new Page<>(request, response);
//		ServicePrice servicePrice = new ServicePrice();
		if (servicePrice.getProduct() == null) {
			if (productId == null || productId <= 0) {
				servicePrice.setProduct(new Product());
			} else {
				servicePrice.setProduct(new Product(productId));
			}
		}
		if (productCategoryId != null && productCategoryId >0) {
			servicePrice.setProductCategory(new ProductCategory(productCategoryId));
			model.addAttribute("productCategoryId", productCategoryId);
		}
		ServicePoint servicePoint = new ServicePoint(id);
		servicePoint.setCustomizePriceFlag(Integer.valueOf(customizePriceFlag));
		servicePoint.setRemotePriceFlag(Integer.parseInt(remotePriceFlag));
		servicePoint.setUseDefaultPrice(Integer.parseInt(useDefaultPrice));
		if(serviceRemotePriceFlag == 1){
			servicePoint.setRemotePriceType(Integer.parseInt(remotePriceType));
		}
		servicePrice.setServicePoint(servicePoint);

		page = servicePointPriceService.findPage(page, servicePrice,serviceRemotePriceFlag); //add on 2020-3-13
		model.addAttribute("page", page);
		List<ServiceType> serviceTypes = serviceTypeService.findAllListIdsAndNames();
		//end
		model.addAttribute("serviceTypes", ServiceType.ServcieTypeOrdering.sortedCopy(serviceTypes));
		model.addAttribute("servicePrice",servicePrice);
		model.addAttribute("firstSearch","1");
		return "modules/md/servicePriceListTwo";
	}

	@RequiresPermissions("md:serviceprice:view")
	@RequestMapping(value = { "list", "" }, method = RequestMethod.GET)
	public String getList(ServicePoint servicePoint, ServicePointModel servicePointModel, HttpServletRequest request, HttpServletResponse response, Model model) {
		addMessage(model, "注：请选择网点名称，网点编号，网点电话中至少一项进行查询！");
		model.addAttribute("page", null);
		model.addAttribute("getRequest", true);
		return "modules/md/servicePriceList";
	}

	@RequiresPermissions("md:serviceprice:view")
	@RequestMapping(value = { "list", "" }, method = RequestMethod.POST)
	public String postList(ServicePoint servicePoint, ServicePointModel servicePointModel, HttpServletRequest request, HttpServletResponse response, Model model) {
		Page<ServicePoint> page = new Page<>(request, response);
		BeanUtils.copyProperties(servicePointModel, servicePoint);
		if (StringUtils.isBlank(servicePoint.getServicePointNo()) && StringUtils.isBlank(servicePoint.getName()) && StringUtils.isBlank(servicePoint.getContactInfo1())) {
			addMessage(model, "注：请选择网点名称，网点编号，网点电话中至少一项进行查询！");
			model.addAttribute("page", page);
			model.addAttribute("servicePoint", servicePointModel);
			return "modules/md/servicePriceList";
		}
		servicePoint.setInvoiceFlag(-1);
		if(servicePoint.getFinance() != null){
			servicePoint.getFinance().setInvoiceFlag(-1);
			servicePoint.getFinance().setDiscountFlag(-1);
		}
		servicePoint.setOrderBy("s.order_count desc,s.servicepoint_no");//sort
		servicePoint.setAutoPlanFlag(-1);    //自动派单
		servicePoint.setInsuranceFlag(-1);   //购买保险
		servicePoint.setTimeLinessFlag(-1);  //快可立补贴
		servicePoint.setUseDefaultPrice(-1); //结算标准
		page = servicePointService.findPricePage(page, servicePoint);
		model.addAttribute("page", page);
		model.addAttribute("servicePoint", servicePointModel);
		return "modules/md/servicePriceList";
	}

	@RequestMapping(value = "recover")
	public String recover(){
		return "modules/md/servicePriceRecover";
	}

	/**
	 * 编辑网点单个产品价格
	 * @param servicePrice
	 * @param model
	 * @return
	 */
	@RequiresPermissions("md:serviceprice:view")
	@RequestMapping(value = "form")
	public String form(ServicePrice servicePrice, String qServicePointId, String qServicePointName,
					   String qProductCategoryId, String qProductCategoryName, String qProductId,
					   String qProductName, Model model) {
		model.addAttribute("servicePrice", servicePrice);
		List<ProductPrice> allPrices = productPriceService.findGroupList(Lists.newArrayList(servicePrice.getProduct().getId()), Lists.newArrayList(servicePrice.getServiceType().getId()), null, servicePrice.getServicePoint().getId(), null);
		model.addAttribute("servicePointId", qServicePointId);
		model.addAttribute("servicePointName", qServicePointName);
		model.addAttribute("productCategoryId", qProductCategoryId);
		model.addAttribute("productCategoryName", qProductCategoryName);
		model.addAttribute("productId", qProductId);
		model.addAttribute("productName", qProductName);
		List<HashMap<String, Object>> productPriceList = Lists.newArrayList();
		//切换为微服务
		Map<String, Dict> priceTypeMap = MSDictUtils.getDictMap("PriceType");
		for (ProductPrice productPrice : allPrices){
			HashMap<String, Object> productPriceMap = new HashMap<>();
			productPriceMap.put("priceType", productPrice.getPriceType().getValue());
			productPriceMap.put("priceTypeName", priceTypeMap.get(productPrice.getPriceType().getValue()).getLabel());
			productPriceMap.put("standPrice", productPrice.getEngineerStandardPrice());
			productPriceMap.put("discountPrice", productPrice.getEngineerDiscountPrice());
			productPriceList.add(productPriceMap);
		}
		model.addAttribute("productPriceList", productPriceList);
		return "modules/md/servicePriceForm";
	}

	/**
	 * 修改网点是否使用标准价
	 * @param servicePoint
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequiresPermissions("md:serviceprice:edit")
	@RequestMapping(value = "updateCustomizePriceFlag",method = RequestMethod.POST)
	public AjaxJsonEntity updateCustomizePriceFlag(ServicePoint servicePoint,Integer serviceRemotePriceFlag, HttpServletResponse response){
		response.setContentType("application/json; charset=UTF-8");
		AjaxJsonEntity ajaxResponse = new AjaxJsonEntity(true);
		try {
			if(serviceRemotePriceFlag == 0){
				msServicePointPriceService.updateCustomizePriceFlag(servicePoint);
			}else {
				servicePoint.setRemotePriceFlag(0);
				msServicePointPriceService.updateRemotePriceFlag(servicePoint);
			}
			return ajaxResponse;
		} catch (Exception e){
			ajaxResponse.setSuccess(false);
			ajaxResponse.setMessage("修改网点价格类型失败：" + e.getMessage());
			return ajaxResponse;
		}
	}

	/**
	 * 保存修改后的价格

	@RequiresPermissions("md:serviceprice:edit")
	@RequestMapping(value = "save")
	public String save(ServicePrice servicePrice, String qServicePointId, String qServicePointName,
							   String qProductCategoryId, String qProductCategoryName, String qProductId,
							   String qProductName, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) throws UnsupportedEncodingException {
		if (!beanValidator(model, servicePrice)) {
			return form(servicePrice, qServicePointId, qServicePointName, qProductCategoryId, qProductCategoryName, qProductId, qProductName, model);
		}
		servicePrice.preUpdate();
		pointService.savePrice(servicePrice);
		addMessage(redirectAttributes, "保存服务网点价格'" + servicePrice.getServicePoint().getName() + "'成功");
		return String.format("redirect:%s/md/serviceprice?repage=true&servicePoint.id=%s&servicePoint.name=%s&productCategory.id=%s&productCategory.name=%s&product.id=%s&product.name=%s",
				Global.getAdminPath(),
				qServicePointId,
				URLEncoder.encode(qServicePointName),
				qProductCategoryId,
				URLEncoder.encode(qProductCategoryName),
				qProductId,
				URLEncoder.encode(qProductName));
	}
	 */

	/**
	 * 保存修改后的价格
	 */
	@ResponseBody
	@RequiresPermissions("md:serviceprice:edit")
	@RequestMapping(value = "save",method = RequestMethod.POST)
	public AjaxJsonEntity save(ServicePrice servicePrice, String qServicePointId, String qServicePointName,
							   String qProductCategoryId, String qProductCategoryName, String qProductId,
							   String qProductName, HttpServletResponse response) {
		response.setContentType("application/json; charset=UTF-8");
		AjaxJsonEntity ajaxResponse = new AjaxJsonEntity(false);
		String validMsg = beanValidator(servicePrice);
		if(StringUtils.isNotBlank(validMsg)){
			ajaxResponse.setMessage(validMsg);
			return ajaxResponse;
		}
		try {
			servicePrice.preUpdate();
			pointService.savePrice(servicePrice);
			ajaxResponse.setSuccess(true);
			ajaxResponse.setData(String.format("%s/md/serviceprice?repage=true&firstSearch=1&servicePoint.id=%s&servicePoint.name=%s&productCategory.id=%s&productCategory.name=%s&product.id=%s&product.name=%s&time=%s",
					Global.getAdminPath(),
					qServicePointId,
					URLEncoder.encode(qServicePointName),
					qProductCategoryId,
					URLEncoder.encode(qProductCategoryName),
					qProductId,
					URLEncoder.encode(qProductName),
					System.currentTimeMillis()
					)
			);
			return ajaxResponse;
		} catch (OrderException oe){
			ajaxResponse.setMessage(oe.getMessage());
			return ajaxResponse;
		} catch (Exception e){
			ajaxResponse.setMessage("保存服务网点价格失败：" + e.getMessage());
			return ajaxResponse;
		}
	}
	/**
	 * 停用
	 */
	@RequiresPermissions("md:serviceprice:edit")
	@RequestMapping(value = "delete")
	public String delete(Long id, String qServicePointId, String qServicePointName,
						 String qProductCategoryId, String qProductCategoryName, String qProductId,
						 String qProductName, RedirectAttributes redirectAttributes) throws UnsupportedEncodingException {
		pointService.stopPrice(id);
		addMessage(redirectAttributes, "停用服务网点价格成功");
		return String.format("redirect:%s/md/serviceprice?repage=true&servicePoint.id=%s&servicePoint.name=%s&productCategory.id=%s&productCategory.name=%s&product.id=%s&product.name=%s",
				Global.getAdminPath(),
				qServicePointId,
				URLEncoder.encode(qServicePointName),
				qProductCategoryId,
				URLEncoder.encode(qProductCategoryName),
				qProductId,
				URLEncoder.encode(qProductName));
	}

	/**
	 * 启用
	 */
	@RequiresPermissions("md:serviceprice:edit")
	@RequestMapping(value = "active")
	public String active(Long id, String qServicePointId, String qServicePointName,
						 String qProductCategoryId, String qProductCategoryName, String qProductId,
						 String qProductName, RedirectAttributes redirectAttributes) throws UnsupportedEncodingException {
		pointService.activePrice(id);
		addMessage(redirectAttributes, "启用服务网点价格成功");
		return String.format("redirect:%s/md/serviceprice?repage=true&servicePoint.id=%s&servicePoint.name=%s&productCategory.id=%s&productCategory.name=%s&product.id=%s&product.name=%s",
						Global.getAdminPath(),
						qServicePointId,
						URLEncoder.encode(qServicePointName),
						qProductCategoryId,
						URLEncoder.encode(qProductCategoryName),
						qProductId,
						URLEncoder.encode(qProductName));
	}


	/**
	 * 编辑网点某产品下各服务价格
	 * @param servicePrices
	 */
	@RequiresPermissions("md:serviceprice:edit")
	@RequestMapping(value = "product")
	public String productForm(ServicePrices servicePrices, String qServicePointId, String qServicePointName,
							  String qProductCategoryId, String qProductCategoryName, String qProductId,
							  String qProductName, Model model) {
		Boolean canAction = true;
		model.addAttribute("servicePointId", qServicePointId);
		model.addAttribute("servicePointName", qServicePointName);
		model.addAttribute("productCategoryId", qProductCategoryId);
		model.addAttribute("productCategoryName", qProductCategoryName);
		model.addAttribute("productId", qProductId);
		model.addAttribute("productName", qProductName);

		if(servicePrices.getServicePoint()==null || servicePrices.getServicePoint().getId()==null ||
				servicePrices.getProduct()==null || servicePrices.getProduct().getId() == null){
			addMessage(model,"参数：网点或产品错误.");
			model.addAttribute("canAction",false);
			return "modules/md/servicePriceProductForm";
		}

		Long sid = servicePrices.getServicePoint().getId();
		//ServicePoint servicePoint = pointService.getFromCache(sid);
		ServicePoint servicePoint = pointService.getSimple(sid);
		if(servicePoint == null){
			addMessage(model,"读取网点信息失败，请关闭窗口，重新打开。");
			model.addAttribute("canAction",false);
			return "modules/md/servicePriceProductForm";
		}
		servicePrices.setServicePoint(servicePoint);

		Long pid = servicePrices.getProduct().getId();
		Product product = productService.getProductByIdFromCache(pid);
		if(product == null){
			addMessage(model,"读取产品信息失败，请关闭窗口，重新打开。");
			model.addAttribute("canAction",false);
			return "modules/md/servicePriceProductForm";
		}
		servicePrices.setProduct(product);
		//mark on 2019-10-11
		//List<ServiceType> serviceTypes = serviceTypeService.findAllList();
		//调用微服务 只返回id和名称 start 2019-10-11
		List<ServiceType> serviceTypes = serviceTypeService.findAllListIdsAndNames();
		// end
		if(serviceTypes == null || serviceTypes.size()==0){
			addMessage(model,"读取服务类型信息失败，请关闭窗口，重新打开。");
			model.addAttribute("canAction",false);
			return "modules/md/servicePriceProductForm";
		}
        //默认价格
        List<ProductPrice> allPrices = productPriceService.findGroupList(Lists.newArrayList(pid), null, null, servicePoint.getId(), null);
		List<Long> allServiceTypeIdList = allPrices.stream().map(t->t.getServiceType().getId()).distinct().collect(Collectors.toList());
		if(allPrices == null || allPrices.size()==0){
            addMessage(model,"产品参考价格为空，请先维护产品参考价格。");
            model.addAttribute("canAction",false);
            return "modules/md/servicePriceProductForm";
        }

		List<ServicePrice> prices = null;
		try{
			/*
			// mark on 2020-3-4 begin
			prices = pointService.getPrices(sid, product.getId(), null);//所有价格，包含停用
			List<ServicePrice> servicePriceList = pointService.getPricesNew(sid, product.getId(), null); // add on 2019-12-20
			pointService.getPricesCompare("ServicePriceController.productForm.", sid, prices, servicePriceList); 			// add on 2019-12-20
			// mark on 2020-3-4 end
			*/
			//prices = pointService.getPricesNew(sid, product.getId(), null); // add on 2019-12-20  //mark on 2020-3-13
			prices = servicePointPriceService.getPricesNew(sid, product.getId(), null,null);  // add on 2020-3-13
		}catch (Exception e){
			log.error("[getPrices]读取网点价格错误:{}",sid,e);
			addMessage(model,"读取网点已维护价格错误，请重试。");
			model.addAttribute("canAction",false);
			return "modules/md/servicePriceProductForm";
		}
		if (prices == null){
			prices = Lists.newArrayList();
		}
		final List<ServicePrice> hasprices = prices.stream().filter(t->Objects.equals(t.getProduct().getId(), pid))
				.collect(Collectors.toList());
		List<ServicePrice> list = Lists.newArrayList();
		allServiceTypeIdList.forEach(t->{
			List<ProductPrice> pp = allPrices.stream().filter(p->Objects.equals(p.getServiceType().getId(),t)).collect(Collectors.toList());
            ServiceType st = serviceTypes.stream().filter(m->Objects.equals(m.getId(),t)).findFirst().orElse(null);
            if(st !=null) {
                ServicePrice price = hasprices.stream().filter(s -> Objects.equals(s.getServiceType().getId(), t)).findFirst().orElse(null);
                if (price == null) {
                    price = new ServicePrice();
//                    price.setPrice(productPrice.getEngineerStandardPrice());
//                    price.setDiscountPrice(productPrice.getEngineerDiscountPrice());
                }
                int useDefaultPrice = servicePoint.getUseDefaultPrice();  // add on 2020-3-5

				price.setServiceType(st);
//				price.setReferPrice(productPrice.getEngineerStandardPrice());
//				price.setReferDiscountPrice(productPrice.getEngineerDiscountPrice());
				List<HashMap<String, Object>> productPriceList = Lists.newArrayList();
				//切换为微服务
				Map<String, Dict> priceTypeMap = MSDictUtils.getDictMap("PriceType");
				for (ProductPrice productPrice : pp){
					HashMap<String, Object> productPriceMap = new HashMap<>();
					productPriceMap.put("priceType", productPrice.getPriceType().getValue());
					productPriceMap.put("priceTypeName", priceTypeMap.get(productPrice.getPriceType().getValue()).getLabel());
					productPriceMap.put("standPrice", productPrice.getEngineerStandardPrice());
					productPriceMap.put("discountPrice", productPrice.getEngineerDiscountPrice());
					productPriceList.add(productPriceMap);
					// add on 2020-3-5 begin
					if (useDefaultPrice == productPrice.getPriceType().getIntValue()) {
						if (price.getId() == null && price.getServiceType().getId().longValue() == productPrice.getServiceType().getId().longValue()) {
							price.setPrice(productPrice.getEngineerStandardPrice());
							price.setDiscountPrice(productPrice.getEngineerDiscountPrice());
						}
					}
					// add on 2020-3-5 end
				}
				price.setProductPriceList(productPriceList);

				list.add(price);
            }
        });

        servicePrices.getPrices().addAll(list);

		model.addAttribute("canAction",true);
		model.addAttribute("servicePrices", servicePrices);
		return "modules/md/servicePriceProductForm";
	}

	@RequiresPermissions("md:serviceprice:edit")
	@RequestMapping(value = "productNew")
	public String productFormNew(ServicePrices servicePrices, String qServicePointId, String qServicePointName,
							  String qProductCategoryId, String qProductCategoryName, String qProductId,
							  String qProductName,Integer serviceRemotePriceFlag, Model model) {
		Boolean canAction = true;
		model.addAttribute("servicePointId", qServicePointId);
		model.addAttribute("servicePointName", qServicePointName);
		model.addAttribute("productCategoryId", qProductCategoryId);
		model.addAttribute("productCategoryName", qProductCategoryName);
		model.addAttribute("productId", qProductId);
		model.addAttribute("productName", qProductName);
		model.addAttribute("serviceRemotePriceFlag", serviceRemotePriceFlag);//价格类型  0服务价格 1远程价格
		String view = "modules/md/servicePriceProductFormNew";

		if(servicePrices.getServicePoint()==null || servicePrices.getServicePoint().getId()==null ||
				servicePrices.getProduct()==null || servicePrices.getProduct().getId() == null){
			addMessage(model,"参数：网点或产品错误.");
			model.addAttribute("canAction",false);
			return view;
		}

		Long sid = servicePrices.getServicePoint().getId();
		//ServicePoint servicePoint = pointService.getFromCache(sid);
		ServicePoint servicePoint = pointService.getSimple(sid);
		if(servicePoint == null){
			addMessage(model,"读取网点信息失败，请关闭窗口，重新打开。");
			model.addAttribute("canAction",false);
			return view;
		}

		servicePrices.setServicePoint(servicePoint);

		Long pid = servicePrices.getProduct().getId();
		Product product = productService.getProductByIdFromCache(pid);
		if(product == null){
			addMessage(model,"读取产品信息失败，请关闭窗口，重新打开。");
			model.addAttribute("canAction",false);
			return view;
		}
		servicePrices.setProduct(product);
		List<ServiceType> serviceTypes = serviceTypeService.findAllListIdsAndNames();
		// end
		if(serviceTypes == null || serviceTypes.size()==0){
			addMessage(model,"读取服务类型信息失败，请关闭窗口，重新打开。");
			model.addAttribute("canAction",false);
			return view;
		}
		//默认价格
		List<ProductPrice> allPrices = productPriceService.findGroupList(Lists.newArrayList(pid), null, null, servicePoint.getId(), null);
		List<Long> allServiceTypeIdList = allPrices.stream().map(t->t.getServiceType().getId()).distinct().collect(Collectors.toList());
		if(allPrices == null || allPrices.size()==0){
			addMessage(model,"产品参考价格为空，请先维护产品参考价格。");
			model.addAttribute("canAction",false);
			return view;
		}

		List<ServicePrice> prices = null;
		try{
			if(serviceRemotePriceFlag == 0){
				prices = servicePointPriceService.getPricesNew(sid, product.getId(), null,servicePoint.getUseDefaultPrice());  // add on 2020-3-13
			}else {
				prices = servicePointPriceService.getPricesNew(sid, product.getId(), null,servicePoint.getRemotePriceType());  // add on 2020-3-13
			}


		}catch (Exception e){
			log.error("[getPrices]读取网点价格错误:{}",sid,e);
			addMessage(model,"读取网点已维护价格错误，请重试。");
			model.addAttribute("canAction",false);
			return view;
		}
		if (prices == null){
			prices = Lists.newArrayList();
		}
		final List<ServicePrice> hasprices = prices.stream().filter(t->Objects.equals(t.getProduct().getId(), pid))
				.collect(Collectors.toList());
		List<ServicePrice> list = Lists.newArrayList();
		int priceType;
		if(serviceRemotePriceFlag == 0){
			priceType = servicePoint.getUseDefaultPrice();
		}else {
			priceType = servicePoint.getRemotePriceType();  // add on 2020-3-5
		}
		allServiceTypeIdList.forEach(t->{
			List<ProductPrice> pp = allPrices.stream().filter(p->Objects.equals(p.getServiceType().getId(),t)).collect(Collectors.toList());
			ServiceType st = serviceTypes.stream().filter(m->Objects.equals(m.getId(),t)).findFirst().orElse(null);
			if(st !=null) {
				ServicePrice price = hasprices.stream().filter(s -> Objects.equals(s.getServiceType().getId(), t)).findFirst().orElse(null);
				if (price == null) {
					price = new ServicePrice();
				}
				price.setServiceType(st);
				List<HashMap<String, Object>> productPriceList = Lists.newArrayList();
				//切换为微服务
				Map<String, Dict> priceTypeMap = MSDictUtils.getDictMap("PriceType");
				for (ProductPrice productPrice : pp){
					HashMap<String, Object> productPriceMap = new HashMap<>();
					productPriceMap.put("priceType", productPrice.getPriceType().getValue());
					productPriceMap.put("priceTypeName", priceTypeMap.get(productPrice.getPriceType().getValue()).getLabel());
					productPriceMap.put("standPrice", productPrice.getEngineerStandardPrice());
					productPriceMap.put("discountPrice", productPrice.getEngineerDiscountPrice());
					productPriceList.add(productPriceMap);
					Collections.sort(productPriceList, new Comparator<Map<String, Object>>(){

						public int compare(Map<String, Object> o1, Map<String, Object> o2) {
							String name1 =(String)o1.get("priceType");
							String name2= (String)o2.get("priceType");
							return name1.compareTo(name2);
						}

					});
					if (priceType == productPrice.getPriceType().getIntValue()) {
						if (price.getId() == null && price.getServiceType().getId().longValue() == productPrice.getServiceType().getId().longValue()) {
							price.setPrice(productPrice.getEngineerStandardPrice());
							price.setDiscountPrice(productPrice.getEngineerDiscountPrice());
						}
					}
				}
				price.setProductPriceList(productPriceList);

				list.add(price);
			}
		});

		servicePrices.getPrices().addAll(list);

		model.addAttribute("canAction",true);
		model.addAttribute("servicePrices", servicePrices);
		return view;
	}

	@RequiresPermissions("md:serviceprice:edit")
	@RequestMapping(value = "remotePriceProductForm")
	public String remotePriceProductForm(ServicePrices servicePrices, String qServicePointId, String qServicePointName,
								 String qProductCategoryId, String qProductCategoryName, String qProductId,
								 String qProductName,Integer serviceRemotePriceFlag, Model model) {
		Boolean canAction = true;
		model.addAttribute("servicePointId", qServicePointId);
		model.addAttribute("servicePointName", qServicePointName);
		model.addAttribute("productCategoryId", qProductCategoryId);
		model.addAttribute("productCategoryName", qProductCategoryName);
		model.addAttribute("productId", qProductId);
		model.addAttribute("productName", qProductName);
		model.addAttribute("serviceRemotePriceFlag", serviceRemotePriceFlag);//价格类型  0服务价格 1远程价格
		String view = "modules/md/remotePriceProductForm";

		if(servicePrices.getServicePoint()==null || servicePrices.getServicePoint().getId()==null ||
				servicePrices.getProduct()==null || servicePrices.getProduct().getId() == null){
			addMessage(model,"参数：网点或产品错误.");
			model.addAttribute("canAction",false);
			return view;
		}

		Long sid = servicePrices.getServicePoint().getId();
		ServicePoint servicePoint = pointService.getSimple(sid);
		if(servicePoint == null){
			addMessage(model,"读取网点信息失败，请关闭窗口，重新打开。");
			model.addAttribute("canAction",false);
			return view;
		}

		servicePrices.setServicePoint(servicePoint);

		Long pid = servicePrices.getProduct().getId();
		Product product = productService.getProductByIdFromCache(pid);
		if(product == null){
			addMessage(model,"读取产品信息失败，请关闭窗口，重新打开。");
			model.addAttribute("canAction",false);
			return view;
		}
		servicePrices.setProduct(product);
		List<ServiceType> serviceTypes = serviceTypeService.findAllListIdsAndNames();
		List<Long> allServiceTypeIdList = serviceTypes.stream().map(ServiceType::getId).distinct().collect(Collectors.toList());
		// end
		if(serviceTypes.size() == 0){
			addMessage(model,"读取服务类型信息失败，请关闭窗口，重新打开。");
			model.addAttribute("canAction",false);
			return view;
		}
			//默认价格
		List<ProductPrice> allPrices = productPriceService.findGroupList(Lists.newArrayList(pid), null, servicePoint.getUseDefaultPrice(), servicePoint.getId(), null);
		if(allPrices == null || allPrices.size()==0){
			addMessage(model,"产品参考价格为空，请先维护产品参考价格。");
			model.addAttribute("canAction",false);
			return view;
		}
		List<ServicePrice> prices;
		List<ServicePrice> servicePriceList;
		try{
			prices = servicePointPriceService.getPricesNew(sid, product.getId(), null,servicePoint.getRemotePriceType());  // add on 2020-3-13
			servicePriceList = servicePointPriceService.getPricesNew(sid, product.getId(), null,servicePoint.getUseDefaultPrice());  // add on 2020-3-13
		}catch (Exception e){
			log.error("[getPrices]读取网点价格错误:{}",sid,e);
			addMessage(model,"读取网点已维护价格错误，请重试。");
			model.addAttribute("canAction",false);
			return view;
		}

		if (prices == null){
			prices = Lists.newArrayList();
		}
		final List<ServicePrice> hasprices = prices.stream().filter(t->Objects.equals(t.getProduct().getId(), pid))
				.collect(Collectors.toList());

		final List<ServicePrice> hasSerivcePrices = servicePriceList.stream().filter(t->Objects.equals(t.getProduct().getId(), pid))
				.collect(Collectors.toList());
		List<ServicePrice> list = Lists.newArrayList();

		allServiceTypeIdList.forEach(t->{
			List<ProductPrice> pp = allPrices.stream().filter(p->Objects.equals(p.getServiceType().getId(),t)).collect(Collectors.toList());
			ServiceType st = serviceTypes.stream().filter(m->Objects.equals(m.getId(),t)).findFirst().orElse(null);
			if(st !=null) {
				ServicePrice price = hasprices.stream().filter(s -> Objects.equals(s.getServiceType().getId(), t)).findFirst().orElse(null);
				ServicePrice serPrice = hasSerivcePrices.stream().filter(s -> Objects.equals(s.getServiceType().getId(), t)).findFirst().orElse(null);
				if (price == null) {
					price = new ServicePrice();
				}
				if (serPrice == null) {
					for(ProductPrice productPrice :pp){
						price.setReferPrice(productPrice.getEngineerStandardPrice());
						price.setReferDiscountPrice(productPrice.getEngineerDiscountPrice());
					}
				}else {
					price.setReferPrice(serPrice.getPrice());
					price.setReferDiscountPrice(serPrice.getDiscountPrice());
				}
				price.setServiceType(st);

				list.add(price);
			}
		});

		servicePrices.getPrices().addAll(list);

		model.addAttribute("canAction",true);
		model.addAttribute("servicePrices", servicePrices);
		return view;
	}
	/**
	 * 保存某网点下某产品的所有安维价格
	 * 新增加：判断价格是否和默认价格一致，不一致要审核
	 * 修改：都要审核
	 */
	/*@RequiresPermissions("md:serviceprice:edit")
	@RequestMapping(value = "saveProductPrices")
	public String saveProductPrices(ServicePrices entity, String qServicePointId, String qServicePointName,
									String qProductCategoryId, String qProductCategoryName, String qProductId,
									String qProductName, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {

		if (!beanValidator(model, entity)) {
			return productForm(entity, qServicePointId, qServicePointName, qProductCategoryId, qProductCategoryName, qProductId, qProductName, model);
		}

		List<ServicePrice> prices = entity.getPrices().stream().filter(t->t.getServiceType()!=null).collect(Collectors.toList());
		if(prices==null || prices.size()==0){
			addMessage(model, "错误：请设定服务价格");
			return productForm(entity, qServicePointId, qServicePointName, qProductCategoryId, qProductCategoryName, qProductId, qProductName, model);
		}
		final User user = UserUtils.getUser();
		entity.setCreateBy(user);
		entity.setCreateDate(new Date());
		entity.setPrices(prices);
		try {
			//pointService.saveProductPrices(entity);  //mark on 2020-3-11
			servicePointPriceService.saveProductPrices(entity);	// add on 2020-3-11
			addMessage(model, "保存服务网点：" + entity.getServicePoint().getName() + " 价格成功");
			return String.format("redirect:%s/md/serviceprice?repage=true&servicePoint.id=%s&servicePoint.name=%s&productCategory.id=%s&productCategory.name=%s&product.id=%s&product.name=%s",
					Global.getAdminPath(),
					qServicePointId,
					URLEncoder.encode(qServicePointName),
					qProductCategoryId,
					URLEncoder.encode(qProductCategoryName),
					qProductId,
					URLEncoder.encode(qProductName));
		} catch (Exception e) {
			addMessage(model, "保存服务网点：" + entity.getServicePoint().getName() + " 价格失败:" + e.getMessage());
			entity.setPrices(Lists.newArrayList());  // add on 2020-3-11
			return productForm(entity, qServicePointId, qServicePointName, qProductCategoryId, qProductCategoryName, qProductId, qProductName, model);
		}

	}*/



	@RequiresPermissions("md:serviceprice:edit")
	@RequestMapping(value = "saveProductPrices")
	@ResponseBody
	public AjaxJsonEntity saveProductPrices(ServicePrices entity, String qServicePointId, String qServicePointName,
											String qProductCategoryId, String qProductCategoryName, String qProductId,
											String qProductName,Integer serviceRemotePriceFlag, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {

		AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
		if (!beanValidator(model, entity)) {
			try {
				productForm(entity, qServicePointId, qServicePointName, qProductCategoryId, qProductCategoryName, qProductId, qProductName, model);
			} catch (Exception e) {
				ajaxJsonEntity.setSuccess(false);
				ajaxJsonEntity.setMessage(e.getMessage());
			}
		}

		List<ServicePrice> prices = entity.getPrices().stream().filter(t->t.getServiceType()!=null).collect(Collectors.toList());
		if(prices==null || prices.size()==0){
			ajaxJsonEntity.setSuccess(false);
			if(serviceRemotePriceFlag == 0){
				ajaxJsonEntity.setMessage("错误：请设定服务价格");
			}
			productForm(entity, qServicePointId, qServicePointName, qProductCategoryId, qProductCategoryName, qProductId, qProductName, model);
		}
		final User user = UserUtils.getUser();
		entity.setCreateBy(user);
		entity.setCreateDate(new Date());
		entity.setPrices(prices);
		try {
			//pointService.saveProductPrices(entity);  //mark on 2020-3-11
			servicePointPriceService.saveProductPrices(entity,serviceRemotePriceFlag);	// add on 2020-3-11
			model.addAttribute("servicePoint", entity.getServicePoint());
			ajaxJsonEntity.setMessage("保存服务网点：" + entity.getServicePoint().getName() + " 价格成功");
		} catch (Exception e) {
			log.error("保存服务网点：" + entity.getServicePoint().getName() + " 价格失败:" + e.getMessage(), e);
			ajaxJsonEntity.setSuccess(false);
			ajaxJsonEntity.setMessage("保存服务网点：" + entity.getServicePoint().getName() + " 价格失败:" + e.getMessage());
		}
		return ajaxJsonEntity;
	}


	@RequiresPermissions("md:serviceprice:edit")
	@RequestMapping(value = "saveRemotePrices")
	@ResponseBody
	public AjaxJsonEntity saveRemotePrices(ServicePrices entity, Model model) {

		AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);


		List<ServicePrice> prices = entity.getPrices().stream().filter(t->t.getServiceType()!=null).collect(Collectors.toList());

		final User user = UserUtils.getUser();
		entity.setCreateBy(user);
		entity.setCreateDate(new Date());
		entity.setPrices(prices);
		try {
			servicePointPriceService.saveRemotePrices(entity);	// add on 2020-3-11
			model.addAttribute("servicePoint", entity.getServicePoint());
			ajaxJsonEntity.setMessage("保存服务网点：" + entity.getServicePoint().getName() + " 价格成功");
		} catch (Exception e) {
			log.error("保存服务网点：" + entity.getServicePoint().getName() + " 价格失败:" + e.getMessage(), e);
			ajaxJsonEntity.setSuccess(false);
			ajaxJsonEntity.setMessage("保存服务网点：" + entity.getServicePoint().getName() + " 价格失败:" + e.getMessage());
		}
		return ajaxJsonEntity;
	}


	/**
	 * 新增或编辑网点下所有价格   弃用
	 * @return

	@RequiresPermissions("md:serviceprice:view")
	@RequestMapping(value = "forms")
	public String forms(ServicePrices servicePrices, Model model) {
		Long id =null;
		List<ServicePrice> prices = Lists.newArrayList();
		List<Product> products = pointService.getProducts(servicePrices.getServicePoint().getId());
		List<ProductPrice> allprices = productPriceService.findAllList();
		if(servicePrices !=null && servicePrices.getServicePoint() !=null
				&& servicePrices.getServicePoint().getId() != null && servicePrices.getServicePoint().getId() >0 ) {
			id = servicePrices.getServicePoint().getId();
		}
		if(id!=null && id>0) {
			//已维护的价格
			prices = pointService.getPrices(id,null, null);
			Map<Product, List<ServicePrice>> map = prices.stream()
					.collect(
							Collectors.groupingBy(ServicePrice::getProduct)
					);

			List<ServiceType> serviceTypes = serviceTypeService.findAllList();
			//一个产品下所有的服务价格
			List<ServicePrice> defaultPrices = Lists.newArrayList();
			for (ServiceType serviceType : serviceTypes) {
				ServicePrice price = new ServicePrice();
				price.setServiceType(serviceType);
				price.setPrice(0.0);
				price.setDiscountPrice(0.0);
				price.setDelFlag(1);//不需设定
				defaultPrices.add(price);
			}
			//所有服务类型id
			Set<Long> allTypeSet = serviceTypes.stream().map(m->m.getId()).collect(Collectors.toSet());
			//已有价格的服务类型id
			final Set<Long> prodServiceTypes = Sets.newHashSet();

			if(map==null || map.size()==0){////客户下产品都未设置价格

				for(Product p:products){
					List<ServicePrice> list = new ArrayList<ServicePrice>();
					prodServiceTypes.clear();
					allprices.stream().filter(t->Objects.equals(t.getProduct().getId(),p.getId()))
							.forEach(item->{
								prodServiceTypes.add(item.getServiceType().getId());
								ServicePrice price = new ServicePrice();
								price.setServiceType(item.getServiceType());
								price.setPrice(item.getEngineerStandardPrice());
								price.setDiscountPrice(item.getEngineerDiscountPrice());
								list.add(price);
							});
					//该产品未定义服务价格的
					Set<Long> noset= Sets.difference(allTypeSet, prodServiceTypes);
					if(noset!= null && noset.size()>0){
						noset.stream().forEach(t->{
							ServiceType st = serviceTypes.stream().filter(m->Objects.equals(m.getId(),t)).findFirst().orElse(null);
							if(st != null){
								ServicePrice price = new ServicePrice();
								price.setServiceType(st);
								price.setPrice(0.0);
								price.setDiscountPrice(0.0);
								price.setDelFlag(1);
								list.add(price);
							}
						});
					}
					map.put(p, list);
				}
			}else { ////有部分产品设置了价格
				//已有价格的产品
				for (Map.Entry<Product, List<ServicePrice>> e : map.entrySet()) {
					Set<Long> pServiceTypes = e.getValue().stream().map(t->t.getServiceType().getId()).collect(Collectors.toSet());
					Set<Long> noset= Sets.difference(allTypeSet, pServiceTypes);
					noset.stream().forEach(t->{
						ServiceType st = serviceTypes.stream().filter(m->Objects.equals(m.getId(),t)).findFirst().orElse(null);
						if(st != null){
							ServicePrice price = new ServicePrice();
							price.setServiceType(st);
							price.setPrice(0.0);
							price.setDiscountPrice(0.0);
							price.setDelFlag(1);
							e.getValue().add(price);
						}
					});
				}
				//未设定价格的产品
				List<Product> newProducts = new ArrayList<Product>(products);
				newProducts.removeAll(map.keySet());
				if(newProducts !=null && newProducts.size()>0){
					for(Product p:newProducts){
						List<ServicePrice> list = new ArrayList<ServicePrice>();
						allTypeSet.stream().forEach(m-> {
							ProductPrice prodPrice = allprices.stream().filter(t -> Objects.equals(t.getProduct().getId(), p.getId())
									&& Objects.equals(t.getServiceType().getId(), m)).findFirst().orElse(null);
							ServicePrice price = new ServicePrice();
							if (prodPrice != null) {
								price.setServiceType(prodPrice.getServiceType());
								price.setPrice(prodPrice.getEngineerStandardPrice());
								price.setDiscountPrice(prodPrice.getEngineerDiscountPrice());
							} else {
								price.setServiceType(new ServiceType(m));
								price.setPrice(0.0);
								price.setDiscountPrice(0.0);
								price.setDelFlag(1);
							}
							list.add(price);
						});
						map.put(p, list);
					}
				}
			}
			model.addAttribute("serviceTypes", ServiceType.ServcieTypeOrdering.sortedCopy(serviceTypes));
			model.addAttribute("priceMap", map);
			model.addAttribute("products", products);
		}
		model.addAttribute("servicePrices", servicePrices);
		return "modules/md/servicePriceForms";
	}


	@RequiresPermissions("md:serviceprice:edit")
	@RequestMapping(value = "savePrices")
	public String savePrices(ServicePrices entity, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {

		if (!beanValidator(model, entity)) {
			return forms(entity, model);
		}

		List<ServicePrice> prices = entity.getPrices();
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
			pointService.insertPrices(entity);
			addMessage(model, "保存服务网点：" + entity.getServicePoint().getName() + " 价格成功");
			return "redirect:" + Global.getAdminPath() + "/md/serviceprice?repage=true&servicePoint.id="+entity.getServicePoint().getId();
		} catch (Exception e) {
			addMessage(model, "保存服务网点：" + entity.getServicePoint().getName() + "' 价格失败:" + e.getMessage());
			return forms(entity,model);
		}

	}
	 */

}
