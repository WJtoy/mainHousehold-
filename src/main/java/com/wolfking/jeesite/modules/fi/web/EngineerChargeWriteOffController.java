package com.wolfking.jeesite.modules.fi.web;

import com.google.common.collect.Lists;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.fi.entity.EngineerCharge;
import com.wolfking.jeesite.modules.fi.entity.EngineerChargeCondition;
import com.wolfking.jeesite.modules.fi.entity.EngineerChargeMaster;
import com.wolfking.jeesite.modules.fi.service.EngineerChargeMasterService;
import com.wolfking.jeesite.modules.fi.service.EngineerChargeService;
import com.wolfking.jeesite.modules.md.dao.ServicePointDao;
import com.wolfking.jeesite.modules.md.entity.ServicePointFinance;
import com.wolfking.jeesite.modules.md.entity.ServiceType;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.ms.providermd.service.MSServiceTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 客户对帐异常处理Controller
 * 
 * @author Jeff Zhao
 * @version 2014-10-27
 */
@Controller
@RequestMapping(value = "${adminPath}/fi/engineerchargewriteoff")
public class EngineerChargeWriteOffController extends BaseController {

	@Autowired
	private EngineerChargeService engineerChargeService;
	@Autowired
	private EngineerChargeMasterService engineerChargeMasterService;
	@Autowired
	private MSServiceTypeService msServiceTypeService;
	@Resource
	private ServicePointDao servicePointDao;
	
	@RequestMapping(value = { "list" }, method = RequestMethod.GET)
	public String newListGet(@RequestParam Map<String, Object> paramMap, HttpServletRequest request, HttpServletResponse response, Model model) {
		paramMap.put("beginDate", DateUtils.getDate("yyyy-MM-01"));
		paramMap.put("endDate", DateUtils.getDate());
        model.addAttribute("page", null);
        model.addAllAttributes(paramMap);
		return "modules/fi/engineerChargeList";
	}
	@RequestMapping(value = { "list" }, method = RequestMethod.POST)
	public String newListPost(@RequestParam Map<String, Object> paramMap, HttpServletRequest request, HttpServletResponse response, Model model) {
		EngineerChargeCondition engineerChargeCondition = new EngineerChargeCondition();
		// 网点
		if (paramMap.containsKey("servicePointId") && paramMap.get("servicePointId").toString().length() > 0){
			engineerChargeCondition.setServicePointId(Long.parseLong(paramMap.get("servicePointId").toString()));
		}
		// 产品类别：
		if (paramMap.containsKey("productCategoryId") && paramMap.get("productCategoryId").toString().length() > 0){
			engineerChargeCondition.setProductCategoryId(Long.parseLong(paramMap.get("productCategoryId").toString()));
		}
		// 产品：
		if (paramMap.containsKey("productId") && paramMap.get("productId").toString().length() > 0){
			engineerChargeCondition.setProductId(Long.parseLong(paramMap.get("productId").toString()));
		}
		//状态
		if (paramMap.containsKey("status") && paramMap.get("status").toString().length() > 0){
			engineerChargeCondition.setStatus(Integer.parseInt(paramMap.get("status").toString()));
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
			engineerChargeCondition.setOrderCloseBeginDate(beginDate);
			engineerChargeCondition.setOrderCloseEndDate(endDate);
		}
		// 订单编号：
		if (paramMap.containsKey("orderNo") && paramMap.get("orderNo").toString().trim().length() > 0){
			engineerChargeCondition.setOrderNo(paramMap.get("orderNo").toString());
		}
		Page<EngineerCharge> page = engineerChargeService.find(new Page<EngineerChargeCondition>(request, response), engineerChargeCondition);
		model.addAttribute("page", page);
        model.addAllAttributes(paramMap);
		return "modules/fi/engineerChargeList";
	}
	
	/**
	 * 异常 form
	 * @param request
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "writeoffform",method= RequestMethod.GET)
	public String writeOffForm(Long id, HttpServletRequest request, Model model) {
		EngineerCharge engineerCharge = engineerChargeService.get(id);
		engineerCharge.setRemarks("");
		//读取工单级费用
		EngineerChargeMaster engineerChargeMaster = engineerChargeMasterService.findOrderLevelFee(engineerCharge.getOrderId(), engineerCharge.getServicePoint().getId());
		//从数据库读取扣点开关与扣点点数-用于计算扣点，平台费点
		ServicePointFinance servicePointFinance = servicePointDao.getDiscountFlagById(engineerCharge.getServicePoint().getId());
		//从微服务获取服务类型对应的扣点开关与信息费开关-用于计算扣点，平台费点
		List<ServiceType> serviceTypeList = msServiceTypeService.findTaxAndInfoFlagListByIdsForFI(Lists.newArrayList(engineerCharge.getServiceType().getId()));
		if (serviceTypeList.size() == 0) {
			throw new RuntimeException("服务类型读取失败，请重试。");
		}
		ServiceType serviceType = serviceTypeList.get(0);
		model.addAttribute("engineerCharge", engineerCharge);
		//用于显示工单级费用
		model.addAttribute("engineerChargeMaster", engineerChargeMaster);
		//读取当前扣点点数
		model.addAttribute("servicePointFinance", servicePointFinance);
		//读取上门服务项扣点/平台费开关
		model.addAttribute("serviceType", serviceType);
		model.addAttribute("id", id);
		return "modules/fi/engineerChargeWriteOffForm";
	}
	
	@ResponseBody
	@RequestMapping(value = "writeoffsave")
	public AjaxJsonEntity writeOffSave(String id, String sc, String ec, String tc, String mc, String oc, String remarks, HttpServletResponse response) {
		response.setContentType("application/json; charset=UTF-8");
		AjaxJsonEntity jsonEntity = new AjaxJsonEntity(true);
		try
		{
			engineerChargeService.writeOffSave(id, sc, ec, tc, mc, oc, remarks);
		}
		catch(Exception e)
		{
			try {
				LogUtils.saveLog("网点退补", "FI:EngineerChargeWriteOff", id, e, null);
			}catch (Exception loge){}
			jsonEntity.setSuccess(false);
			jsonEntity.setMessage(e.getMessage().toString());
		}
		return jsonEntity;
	}
}
