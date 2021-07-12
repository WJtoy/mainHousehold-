package com.wolfking.jeesite.modules.fi.web;

import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.fi.entity.CustomerCharge;
import com.wolfking.jeesite.modules.fi.entity.CustomerChargeCondition;
import com.wolfking.jeesite.modules.fi.service.CustomerChargeService;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
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
 * 客户对帐异常处理Controller
 * 
 * @author Jeff Zhao
 * @version 2017-04-20
 */
@Controller
@RequestMapping(value = "${adminPath}/fi/customerchargewriteoff")
public class CustomerChargeWriteOffController extends BaseController {

	@Autowired
	private CustomerChargeService customerChargeService;
	
	@RequestMapping(value = { "list" }, method = RequestMethod.GET)
	public String listGet(@RequestParam Map<String, Object> paramMap, HttpServletRequest request, HttpServletResponse response, Model model) {
		paramMap.put("beginDate", DateUtils.getDate("yyyy-MM-01"));
		paramMap.put("endDate", DateUtils.getDate());
        model.addAttribute("page", null);
        model.addAllAttributes(paramMap);
		return "modules/fi/customerChargeList";
	}
	@RequestMapping(value = { "list" }, method = RequestMethod.POST)
	public String listPost(@RequestParam Map<String, Object> paramMap, HttpServletRequest request, HttpServletResponse response, Model model) {

		CustomerChargeCondition customerChargeCondition = new CustomerChargeCondition();
		// 客户
		if (paramMap.containsKey("customerId") && paramMap.get("customerId").toString().length() > 0){
			customerChargeCondition.setCustomerId(Long.parseLong(paramMap.get("customerId").toString()));
		}
		// 产品类别：
		if (paramMap.containsKey("productCategoryId") && paramMap.get("productCategoryId").toString().length() > 0){
			customerChargeCondition.setProductCategoryId(Long.parseLong(paramMap.get("productCategoryId").toString()));
		}
		// 产品：
		if (paramMap.containsKey("productId") && paramMap.get("productId").toString().length() > 0){
			customerChargeCondition.setProductIds(paramMap.get("productId").toString());
		}
		//状态
		if (paramMap.containsKey("status") && paramMap.get("status").toString().length() > 0){
			customerChargeCondition.setStatus(Integer.parseInt(paramMap.get("status").toString()));
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
		// 对帐日期：
		if (beginDate != null && endDate != null){
			customerChargeCondition.setCreateBeginDate(beginDate);
			customerChargeCondition.setCreateEndDate(endDate);
		}
		// 订单编号：
		if (paramMap.containsKey("orderNo") && paramMap.get("orderNo").toString().trim().length() > 0){
			customerChargeCondition.setOrderNo(paramMap.get("orderNo").toString());
		}
		// 数　　量：
		if (paramMap.containsKey("totalQty") && paramMap.get("totalQty").toString().trim().length() > 0){
			customerChargeCondition.setTotalQty(Integer.parseInt(paramMap.get("totalQty").toString()));
		}
		//下单日期
		if (paramMap.containsKey("createBeginDate") && paramMap.get("createBeginDate").toString().trim().length() > 0){
			customerChargeCondition.setOrderCreateBeginDate(DateUtils.parseDate(paramMap.get("createBeginDate")));
		}
		if (paramMap.containsKey("createEndDate") && paramMap.get("createEndDate").toString().trim().length() > 0){
			customerChargeCondition.setOrderCreateEndDate(DateUtils.parseDate(paramMap.get("createEndDate")));
		}
		//完成日期
		if (paramMap.containsKey("closeBeginDate") && paramMap.get("closeBeginDate").toString().trim().length() > 0){
			customerChargeCondition.setOrderCloseBeginDate(DateUtils.parseDate(paramMap.get("closeBeginDate")));
		}
		if (paramMap.containsKey("closeEndDate") && paramMap.get("closeEndDate").toString().trim().length() > 0){
			customerChargeCondition.setOrderCloseEndDate(DateUtils.parseDate(paramMap.get("closeEndDate")));
		}
		// 累计上门：
		if (paramMap.containsKey("serviceTimes") && paramMap.get("serviceTimes").toString().trim().length() > 0){
			customerChargeCondition.setServiceTimes(Integer.parseInt(paramMap.get("serviceTimes").toString()));
		}
		Page<CustomerCharge> page = customerChargeService.find(new Page<>(request, response), customerChargeCondition);
		model.addAttribute("page", page);
        model.addAllAttributes(paramMap);
		return "modules/fi/customerChargeList";
	}
	
	/**
	 * 异常 form
	 * @param request
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "writeoffform",method= RequestMethod.GET)
	public String writeOffForm(Long id, HttpServletRequest request, Model model) {
		CustomerCharge customerCharge = customerChargeService.get(id);
		customerCharge.setRemarks("");
		model.addAttribute("customerCharge", customerCharge);
		return "modules/fi/customerChargeWriteOffForm";
	}
	
	@ResponseBody
	@RequestMapping(value = "writeoffsave")
	public AjaxJsonEntity writeOffSave(@RequestParam String id,
									   @RequestParam String serviceCharge, @RequestParam String expressCharge, @RequestParam String travelCharge, @RequestParam String materialCharge,
									   @RequestParam String timeLinessCharge, @RequestParam String urgentCharge, @RequestParam String praiseFee, @RequestParam String otherCharge,
									   String remarks, HttpServletResponse response) {
		response.setContentType("application/json; charset=UTF-8");
		AjaxJsonEntity jsonEntity = new AjaxJsonEntity(true);
		try
		{
			customerChargeService.writeOffSave(id, serviceCharge, expressCharge, travelCharge, materialCharge,
					timeLinessCharge, urgentCharge, praiseFee, otherCharge, remarks);
		}
		catch(Exception e)
		{
			try {
				LogUtils.saveLog("客户退补", "FI:CustomerChargeWriteOff", id, e, null);
			}catch (Exception loge){}
			jsonEntity.setSuccess(false);
			jsonEntity.setMessage(e.getMessage().toString());
		}
		return jsonEntity;
	}
}
