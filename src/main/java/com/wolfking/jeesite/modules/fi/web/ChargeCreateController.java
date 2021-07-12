package com.wolfking.jeesite.modules.fi.web;

import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.fi.service.ChargeServiceNew;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.Engineer;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.md.service.ServicePointService;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderChargeViewModel;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderSearchModel;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.Map;

/**
 * 生成对帐Controller
 * 
 * @author Jeff Zhao
 * @version 2014-10-08
 */
@Controller
@RequestMapping(value = "${adminPath}/fi/chargecreate")
public class ChargeCreateController extends BaseController {
	@Autowired
	private ChargeServiceNew chargeServiceNew;
	@Autowired
	private OrderService orderService;
	@Autowired
	private ServicePointService servicePointService;

	/**deleted by jeff 2020/4/10
	@RequestMapping(value = {"form", ""}, method = RequestMethod.GET)
	public String formGet(@RequestParam Map<String, Object> paramMap, HttpServletRequest request, HttpServletResponse response, Model model) {
		paramMap.put("beginDate", DateUtils.getDate("yyyy-MM-01"));
		paramMap.put("endDate", DateUtils.getDate());
        model.addAttribute("page", null);
        model.addAllAttributes(paramMap);
		return "modules/fi/chargeCreateForm";
	}
	
	@RequestMapping(value = {"form", ""}, method = RequestMethod.POST)
	public String formPost(@RequestParam Map<String, Object> paramMap, HttpServletRequest request, HttpServletResponse response, Model model) {
		OrderSearchModel orderSearchModel = new OrderSearchModel();
		// 客户
		if (paramMap.containsKey("customerId") && paramMap.get("customerId").toString().length() > 0){
			orderSearchModel.setCustomer(new Customer(Long.parseLong(paramMap.get("customerId").toString())));
		}
		// 产品类别：
		if (paramMap.containsKey("productCategoryId") && paramMap.get("productCategoryId").toString().length() > 0){
			orderSearchModel.setProductCategoryId(Long.parseLong(paramMap.get("productCategoryId").toString()));
		}
		// 产品：
		if (paramMap.containsKey("productId") && paramMap.get("productId").toString().length() > 0){
			orderSearchModel.setProductIds(paramMap.get("productId").toString());
		}
		int month = -1;
		// 日期，今天 到往前一月
		Date beginDate = DateUtils.parseDate(paramMap.get("beginDate"));
		if (beginDate == null){
			beginDate = DateUtils.addMonth(new Date(), month);
			paramMap.put("beginDate",
					DateUtils.formatDate(beginDate, "yyyy-MM-dd"));
		}
		beginDate = DateUtils.getDateStart(beginDate);
		Date endDate = DateUtils.parseDate(paramMap.get("endDate"));
		if (endDate == null){
			endDate = new Date();
			paramMap.put("endDate", DateUtils.formatDate(endDate, "yyyy-MM-dd"));
		}
		endDate = DateUtils.getDateEnd(endDate);
		// 完成日期：
		if (beginDate != null && endDate != null){
			orderSearchModel.setBeginDate(beginDate);
			orderSearchModel.setEndDate(endDate);
		}
		// 订单编号：
		if (paramMap.containsKey("orderNo") && paramMap.get("orderNo").toString().trim().length() > 0){
			orderSearchModel.setOrderNo(paramMap.get("orderNo").toString());
		}
		// 数　　量：
		if (paramMap.containsKey("totalQty") && paramMap.get("totalQty").toString().trim().length() > 0){
			orderSearchModel.setTotalQty(Integer.parseInt(paramMap.get("totalQty").toString()));
		}
		// 累计上门：
		if (paramMap.containsKey("serviceTimes") && paramMap.get("serviceTimes").toString().trim().length() > 0){
			orderSearchModel.setServiceTimes(Integer.parseInt(paramMap.get("serviceTimes").toString()));
		}
		//服务网点
		if (paramMap.containsKey("servicePointId") && paramMap.get("servicePointId").toString().length() > 0){
			orderSearchModel.setServicePoint(new ServicePoint(Long.parseLong(paramMap.get("servicePointId").toString())));
		}
		// 远程，配件，其他
		if (paramMap.containsKey("travelChargeFlag")
				&& paramMap.get("travelChargeFlag").toString().toUpperCase().equals("ON")){
			orderSearchModel.setTravelChargeFlag(1);
		}
		if (paramMap.containsKey("partsFlag")
				&& paramMap.get("partsFlag").toString().toUpperCase().equals("ON")){
			orderSearchModel.setPartsFlag(1);
		}
		if (paramMap.containsKey("otherChargeFlag")
				&& paramMap.get("otherChargeFlag").toString().toUpperCase().equals("ON")){
			orderSearchModel.setOtherChargeFlag(1);
		}
		//应收
		if (paramMap.containsKey("totalInStart") && paramMap.get("totalInStart").toString().trim().length() > 0){
			orderSearchModel.setTotalInStart(Double.parseDouble(paramMap.get("totalInStart").toString()));
		}
		if (paramMap.containsKey("totalInEnd") && paramMap.get("totalInEnd").toString().trim().length() > 0){
			orderSearchModel.setTotalInEnd(Double.parseDouble(paramMap.get("totalInEnd").toString()));
		}
		//应付
		if (paramMap.containsKey("totalOutStart") && paramMap.get("totalOutStart").toString().trim().length() > 0){
			orderSearchModel.setTotalOutStart(Double.parseDouble(paramMap.get("totalOutStart").toString()));
		}
		if (paramMap.containsKey("totalOutEnd") && paramMap.get("totalOutEnd").toString().trim().length() > 0){
			orderSearchModel.setTotalOutEnd(Double.parseDouble(paramMap.get("totalOutEnd").toString()));
		}
		orderSearchModel.setOrderDataLevel(OrderUtils.OrderDataLevel.DETAIL);
		Page<Order> page;
		if (StringUtils.isNotBlank(orderSearchModel.getOrderNo()) && orderSearchModel.getOrderNoSearchType() != 1){
			addMessage(model, "错误：请输入正确的订单号码");
			page = new Page<>(request, response);
		}
		else {
			page = orderService.findOrderForCharge(new Page<>(request, response), orderSearchModel);
		}
        model.addAttribute("page", page);
        model.addAllAttributes(paramMap);
		return "modules/fi/chargeCreateForm";
	}
	 **/

	@RequestMapping(value = {"new"}, method = RequestMethod.GET)
	public String formGetNew(@RequestParam Map<String, Object> paramMap, @RequestParam String type, HttpServletRequest request, HttpServletResponse response, Model model) {
		paramMap.put("beginDate", DateUtils.getDate("yyyy-MM-01"));
		paramMap.put("endDate", DateUtils.getDate());
		model.addAttribute("page", null);
		model.addAttribute("paymentType", type);
		model.addAllAttributes(paramMap);
		return "modules/fi/chargeCreateFormNew";
	}

	@RequestMapping(value = {"new"}, method = RequestMethod.POST)
	public String formPostNew(@RequestParam Map<String, Object> paramMap, @RequestParam String type, HttpServletRequest request, HttpServletResponse response, Model model) {
		OrderSearchModel orderSearchModel = new OrderSearchModel();
		ServicePoint servicePoint = new ServicePoint();
		servicePoint.setPaymentType(new Dict(type));
		orderSearchModel.setServicePoint(servicePoint);
		// 客户
		if (paramMap.containsKey("customerId") && paramMap.get("customerId").toString().length() > 0){
			orderSearchModel.setCustomer(new Customer(Long.parseLong(paramMap.get("customerId").toString())));
		}
		// 产品类别：
		if (paramMap.containsKey("productCategoryId") && paramMap.get("productCategoryId").toString().length() > 0){
			orderSearchModel.setProductCategoryId(Long.parseLong(paramMap.get("productCategoryId").toString()));
		}
		// 产品：
		if (paramMap.containsKey("productId") && paramMap.get("productId").toString().length() > 0){
			orderSearchModel.setProductIds(paramMap.get("productId").toString());
		}
		int month = -1;
		// 日期，今天 到往前一月
		Date beginDate = DateUtils.parseDate(paramMap.get("beginDate"));
		if (beginDate == null){
			beginDate = DateUtils.addMonth(new Date(), month);
			paramMap.put("beginDate",
					DateUtils.formatDate(beginDate, "yyyy-MM-dd"));
		}
		beginDate = DateUtils.getDateStart(beginDate);
		Date endDate = DateUtils.parseDate(paramMap.get("endDate"));
		if (endDate == null){
			endDate = new Date();
			paramMap.put("endDate", DateUtils.formatDate(endDate, "yyyy-MM-dd"));
		}
		endDate = DateUtils.getDateEnd(endDate);
		// 完成日期：
		if (beginDate != null && endDate != null){
			orderSearchModel.setBeginDate(beginDate);
			orderSearchModel.setEndDate(endDate);
		}
		// 订单编号：
		if (paramMap.containsKey("orderNo") && paramMap.get("orderNo").toString().trim().length() > 0){
			orderSearchModel.setOrderNo(paramMap.get("orderNo").toString());
		}
		// 累计上门：
		if (paramMap.containsKey("serviceTimes") && paramMap.get("serviceTimes").toString().trim().length() > 0){
			orderSearchModel.setServiceTimes(Integer.parseInt(paramMap.get("serviceTimes").toString()));
		}
		//服务网点
		if (paramMap.containsKey("servicePointId") && paramMap.get("servicePointId").toString().length() > 0){
			orderSearchModel.getServicePoint().setId(Long.parseLong(paramMap.get("servicePointId").toString()));
		}
		// 远程，配件，其他
		if (paramMap.containsKey("travelChargeFlag")
				&& paramMap.get("travelChargeFlag").toString().toUpperCase().equals("ON")){
			orderSearchModel.setTravelChargeFlag(1);
		}
		if (paramMap.containsKey("partsFlag")
				&& paramMap.get("partsFlag").toString().toUpperCase().equals("ON")){
			orderSearchModel.setPartsFlag(1);
		}
		if (paramMap.containsKey("otherChargeFlag")
				&& paramMap.get("otherChargeFlag").toString().toUpperCase().equals("ON")){
			orderSearchModel.setOtherChargeFlag(1);
		}
		orderSearchModel.setOrderDataLevel(OrderUtils.OrderDataLevel.DETAIL);
		Page<OrderChargeViewModel> page;
		if (StringUtils.isNotBlank(orderSearchModel.getOrderNo()) && orderSearchModel.getOrderNoSearchType() != 1){
			addMessage(model, "错误：请输入正确的订单号码");
			page = new Page<>(request, response);
		}
		else {
			page = orderService.findOrderForChargeNew(new Page<>(request, response), orderSearchModel);
		}
		model.addAttribute("page", page);
		model.addAttribute("paymentType", type);
		model.addAllAttributes(paramMap);
		return "modules/fi/chargeCreateFormNew";
	}
	
	@ResponseBody
	@RequestMapping(value = {"save"})
	public AjaxJsonEntity save(@RequestParam String ids, HttpServletResponse response) {
		response.setContentType("application/json; charset=UTF-8");
		AjaxJsonEntity result = new AjaxJsonEntity(true);
		try{
			String[] orderIds = ids.split(",");
			for (String orderIdString : orderIds) {
				Long orderId = Long.parseLong(orderIdString);
				chargeServiceNew.createCharge(orderId, null);
			}
		}
		catch(Exception e){
			result.setSuccess(false);
			result.setMessage(e.getMessage().toString());
		}
		return result;
	}
	
	/**
	 * 异常 form
	 * @param request
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "pending",method=RequestMethod.GET)
	public String pending(String processType, HttpServletRequest request, Model model) {
		model.addAttribute("processType", processType);
		model.addAttribute("order", new Order());
		return "modules/fi/chargePendingForm";
	}
	
	/**
	 * ajax提交异常信息
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "pending",method=RequestMethod.POST)
	public AjaxJsonEntity pending(@RequestParam String ids, @RequestParam String remarks, @RequestParam Integer auditType, HttpServletResponse response) {
		response.setContentType("application/json; charset=UTF-8");
		AjaxJsonEntity result = new AjaxJsonEntity(true);
		if(auditType == null){
			result.setSuccess(false);
			result.setMessage("异常类型不能为空");
			return result;
		}
		try
		{
			if(remarks==null){
				remarks = "";
			}
			orderService.setPending(ids, remarks,auditType);
		}
		catch(Exception e)
		{
			result.setSuccess(false);
			result.setMessage(e.getMessage().toString());
		}
		return result;
	}


	/**
	 * 为订单做标记
	 * @param id	订单id
	 * @return
	 */
	@RequestMapping(value = { "markOrder" })
	public String markOrder(String id,String quarter, HttpServletRequest request, Model model)
	{
		Boolean errorFlag = false;
		Order order = new Order();
		Long lid = Long.valueOf(id);
		if (lid == null || lid <= 0)
		{
			errorFlag = true;
			addMessage(model, "订单参数错误");
		} else
		{
			order = orderService.getOrderById(lid, quarter,OrderUtils.OrderDataLevel.DETAIL,true);
			ServicePoint servicePoint = order.getOrderCondition().getServicePoint();
			if(servicePoint != null && servicePoint.getId() != null & servicePoint.getId()>0) {
				Engineer engineer = servicePointService.getEngineerFromCache(servicePoint.getId(), order.getOrderCondition().getEngineer().getId());
				if(engineer != null){
					User engineerUser = new User(engineer.getId());
					engineerUser.setName(engineer.getName());
					engineerUser.setMobile(engineer.getContactInfo());
					engineerUser.setSubFlag(engineer.getMasterFlag()==1?0:1);
					order.getOrderCondition().setEngineer(engineerUser);
				}
			}
		}
		model.addAttribute("order", order);
		model.addAttribute("errorFlag",errorFlag);
		return "modules/fi/markOrderForm";
	}
}
