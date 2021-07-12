package com.wolfking.jeesite.modules.fi.web;

import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.fi.entity.CustomerCharge;
import com.wolfking.jeesite.modules.fi.entity.CustomerChargeCondition;
import com.wolfking.jeesite.modules.fi.service.CustomerChargeService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
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
 * 客户结帐Controller
 * 
 * @author Jeff Zhao
 * @version 2014-10-15
 */
@Controller
@RequestMapping(value = "${adminPath}/fi/customerinvoice")
public class CustomerInvoiceController extends BaseController
{
	@Autowired
	private CustomerChargeService customerChargeService;
//	@Autowired
//	private CustomerInvoiceService customerInvoiceService;
//	@Autowired
//	private CustomerCurrencyService customerCurrencyService;

	@RequiresPermissions("fi:customerinvoice:edit")
	@RequestMapping(value = { "form", "" }, method = RequestMethod.GET)
	public String formGet(@RequestParam Map<String, Object> paramMap,
                             HttpServletRequest request, HttpServletResponse response,
                             Model model) {
		paramMap.put("beginDate", DateUtils.getDate("yyyy-MM-01"));
		paramMap.put("endDate", DateUtils.getDate());
		model.addAttribute("page", null);
		model.addAllAttributes(paramMap);
		return "modules/fi/customerInvoiceForm";
	}

	@RequiresPermissions("fi:customerinvoice:edit")
	@RequestMapping(value = { "form", "" }, method = RequestMethod.POST)
	public String formPost(@RequestParam Map<String, Object> paramMap,
                              HttpServletRequest request, HttpServletResponse response,
                              Model model) {
		CustomerChargeCondition customerChargeCondition = new CustomerChargeCondition();
		// 客户
		if (paramMap.containsKey("customerId") && paramMap.get("customerId").toString().length() > 0){
			customerChargeCondition.setCustomerId(Long.parseLong(paramMap.get("customerId").toString()));
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
		}//下单日期
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
		Page<CustomerCharge> page = customerChargeService.find(new Page<>(request, response), customerChargeCondition);
		model.addAttribute("page", page);
		model.addAllAttributes(paramMap);
		return "modules/fi/customerInvoiceForm";
	}

	/**
	 * 结帐确认 form
	 *
	 * @param request
	 * @param model
	 * @return
	 */
	@RequiresPermissions("fi:customerinvoice:edit")
	@RequestMapping(value = "save", method = RequestMethod.GET)
	public String saveConfirm(String customerId, String totalCharge,
                              HttpServletRequest request, Model model) {
		model.addAttribute("customerId", customerId);
		model.addAttribute("totalCharge", totalCharge);
		model.addAttribute("currentDate",
				DateUtils.formatDate(new Date(), "yyyy-MM-dd"));
		model.addAttribute("customerCharge", new CustomerCharge());
		return "modules/fi/customerInvoiceConfirmForm";
	}

	@ResponseBody
	@RequiresPermissions("fi:customerinvoice:edit")
	@RequestMapping(value = { "save" })
	public AjaxJsonEntity save(@RequestParam String ids,
							   @RequestParam Date invoiceDate, @RequestParam String remarks,
							   HttpServletResponse response) {
		response.setContentType("application/json; charset=UTF-8");
		AjaxJsonEntity jsonEntity = new AjaxJsonEntity(true);
		try
		{
			// 保存
//			customerInvoiceService.add(ids.split(","), invoiceDate, remarks);
		} catch (Exception e)
		{
			jsonEntity.setSuccess(false);
			jsonEntity.setMessage(e.getMessage().toString());
		}
		return jsonEntity;
	}

//	/**
//	 * 结帐确认 form
//	 *
//	 * @param request
//	 * @param model
//	 * @return
//	 */
//	@RequiresPermissions("fi:customerinvoice:invoiceall")
//	@RequestMapping(value = "invoiceall", method = RequestMethod.GET)
//	public String invoiceAllConfirm(String customerId, String customerChargeNo,
//                                    String beginDate, String endDate, String orderNo,
//                                    String createBeginDate, String createEndDate,
//                                    String closeBeginDate, String closeEndDate,
//                                    HttpServletRequest request, Model model)
//	{
//		model.addAttribute("customerId", customerId);
//		model.addAttribute("customerChargeNo", customerChargeNo);
//		model.addAttribute("beginDate", beginDate);
//		model.addAttribute("endDate", endDate);
//		model.addAttribute("orderNo", orderNo);
//		model.addAttribute("createBeginDate", createBeginDate);
//		model.addAttribute("createEndDate", createEndDate);
//		model.addAttribute("closeBeginDate", closeBeginDate);
//		model.addAttribute("closeEndDate", closeEndDate);
//		model.addAttribute("currentDate",
//				DateUtils.formatDate(new Date(), "yyyy-MM-dd"));
//		model.addAttribute("order", new Order());
//		return "modules/sd/customerInvoiceConfirmAllForm";
//	}
//
//	@ResponseBody
//	@RequiresPermissions("fi:customerinvoice:invoiceall")
//	@RequestMapping(value =
//	{ "invoiceall" })
//	public AjaxJsonEntity invoiceAll(String customerId,
//                                     String customerChargeNo, String beginDate, String endDate,
//                                     String orderNo, String createBeginDate, String createEndDate,
//                                     String closeBeginDate, String closeEndDate,
//                                     @RequestParam Date invoiceDate, @RequestParam String remarks,
//                                     HttpServletResponse response)
//	{
//		response.setContentType("application/json; charset=UTF-8");
//		AjaxJsonEntity jsonEntity = new AjaxJsonEntity(true);
//		try
//		{
//
//			Map<String, Object> paraMap = new HashMap<String, Object>();
//			paraMap.put("customerId", customerId);
//			paraMap.put("customerChargeNo", customerChargeNo);
//			paraMap.put("beginDate", beginDate);
//			paraMap.put("endDate", endDate);
//			paraMap.put("orderNo", orderNo);
//			paraMap.put("createBeginDate", createBeginDate);
//			paraMap.put("createEndDate", createEndDate);
//			paraMap.put("closeBeginDate", closeBeginDate);
//			paraMap.put("closeEndDate", closeEndDate);
//			paraMap.put("status", CustomerChargeMaster.CC_STATUS_CONFIRMED);
//			List<CustomerChargeMaster> needInvoiceList = customerChargeService
//					.find(paraMap);
//
//			// 保存
//			customerInvoiceService.invoiceAll(needInvoiceList, invoiceDate,
//					remarks);
//		} catch (Exception e)
//		{
//			jsonEntity.setSuccess(false);
//			jsonEntity.setMessage(e.getMessage().toString());
//		}
//		return jsonEntity;
//	}

}
