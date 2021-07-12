package com.wolfking.jeesite.modules.sd.web;

import com.google.common.collect.Lists;
import com.kkl.kklplus.entity.praise.Praise;
import com.kkl.kklplus.entity.praise.PraiseStatusEnum;
import com.wolfking.jeesite.common.config.Global;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.exception.OrderException;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.Engineer;
import com.wolfking.jeesite.modules.md.entity.Product;
import com.wolfking.jeesite.modules.md.entity.ServiceType;
import com.wolfking.jeesite.modules.md.service.ProductService;
import com.wolfking.jeesite.modules.md.service.ServiceTypeService;
import com.wolfking.jeesite.modules.sd.entity.*;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderPendingSearchModel;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.modules.sd.service.OrderStatusFlagService;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.cc.entity.AbnormalFormModel;
import com.wolfking.jeesite.ms.cc.service.AbnormalFormService;
import com.wolfking.jeesite.ms.praise.service.OrderPraiseService;
import com.wolfking.jeesite.ms.service.sys.MSDictService;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 异常处理Controller
 *
 */
@Controller
@RequestMapping(value = "${adminPath}/sd/pending")
@Slf4j
public class PendingController extends BaseController
{

	@Autowired
	private OrderService orderService;

	@Autowired
	private ServiceTypeService serviceTypeService;
	@Autowired
	private ProductService productService;

	@Autowired
	private RedisUtils redisUtils;

	@Autowired
	private MSDictService msDictService;

	@Autowired
	private OrderStatusFlagService orderStatusFlagService;

	@Autowired
	private OrderPraiseService orderPraiseService;

	@Autowired
	private AbnormalFormService abnormalFormService;

	/**
	 * 修改窗口（与上门服务窗口基本相同）
	 * @param orderId	订单id
	 */
	@RequiresPermissions(value = { "sd:pending:edit" })
	@RequestMapping(value = "form", method = RequestMethod.GET)
	public String form(@RequestParam String orderId,@RequestParam(required = false) String quarter, Model model) {
		User user = UserUtils.getUser();
		Long lorderId = Long.valueOf(orderId);
		Order order = new Order();
		if (lorderId == null || lorderId <= 0) {
			addMessage(model, "导常处理失败：订单参数为空。");
			model.addAttribute("canAction", false);
			return "modules/sd/pending/pendingServiceForm";
		}
		String lockkey = String.format(RedisConstant.SD_ORDER_LOCK, orderId);//锁
		if (redisUtils.exists(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey)) {
			addMessage(model, "此订单正在处理中，请稍候重试，或刷新页面。");
			model.addAttribute("canAction", false);
			return "modules/sd/pending/pendingServiceForm";
		}

		order = orderService.getOrderById(lorderId, quarter, OrderUtils.OrderDataLevel.DETAIL, true);
		if (order == null || order.getOrderCondition() == null) {
			addMessage(model, "错误：系统繁忙，读取订单失败，请重试。");
			model.addAttribute("canAction", false);
			return "modules/sd/pending/pendingServiceForm";
		}

//		Integer chargeFlag = orderService.getChargeFlag(lorderId, quarter);
		Integer chargeFlag = order.getOrderCondition().getChargeFlag();
		if (chargeFlag != null && chargeFlag.intValue() == 1) {
			addMessage(model, "此订单已对账，不允许修改。");
			model.addAttribute("canAction", false);
		} else {
			//好评单
			OrderStatusFlag orderStatusFlag = orderStatusFlagService.getByOrderId(lorderId, order.getQuarter());
			if(orderStatusFlag!=null && orderStatusFlag.getPraiseStatus()== PraiseStatusEnum.APPROVE.code){
				model.addAttribute("canCancelPraise", true);
			}else{
				model.addAttribute("canCancelPraise", false);
			}
			model.addAttribute("canAction", true);
			model.addAttribute("order", order);
		}

		return "modules/sd/pending/pendingServiceForm";
	}

	/**
	 * 完成返回

	@RequiresPermissions("sd:pending:edit")
	@RequestMapping(value = "save")
	public String save(@RequestParam String id, @RequestParam String orderNo,HttpServletRequest request, Model model, RedirectAttributes redirectAttributes)
	{
		Long lid = Long.valueOf(id);
		if(lid == null || lid <= 0){
			addMessage(redirectAttributes, "订单参数错误");
			return form(id,"",model);
		}
		try
		{
			User user = UserUtils.getUser();
			orderService.saveOrderPending(lid,user);

			addMessage(redirectAttributes, "订单'" + orderNo + "'异常处理完成");
			return "redirect:" + Global.getAdminPath() + "/sd/pending/pendingServiceForm";
		} catch (Exception e)
		{
			addMessage(redirectAttributes,"完成时错误：" + e.getMessage());
			return "modules/sd/pending/list";
		}

	} */

	/**
	 * 添加上门服务明细窗口
	 * @param orderId	订单id
	 */
	@RequiresPermissions(value = { "sd:pending:edit" })
	@RequestMapping(value = "addservice")
	public String addservice(@RequestParam(required = false) Long orderId,@RequestParam(required = false) String quarter, Model model) {
		String lockkey = String.format(RedisConstant.SD_ORDER_LOCK, orderId);
		Long customerId = 0L;
		int dataSource = 0;
		model.addAttribute("customerId",customerId);
		model.addAttribute("dataSource",dataSource);
		//锁
		String vierForm = "modules/sd/pending/pendingServiceItemForm";
		if (redisUtils.exists(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey)) {
			addMessage(model, "错误：此订单正在处理中，请稍候重试，或刷新页面。");
			model.addAttribute("canAction", false);
			return vierForm;
		}
		User user = UserUtils.getUser();
		OrderDetail detail = new OrderDetail();
		Order order = orderService.getOrderById(orderId, quarter, OrderUtils.OrderDataLevel.DETAIL, true);
		if (order == null || order.getOrderCondition() == null) {
			addMessage(model, "错误：系统繁忙，读取订单失败,或订单不存在。请重试！");
			model.addAttribute("canAction", false);
			return vierForm;
		}

		Integer chargeFlag = order.getOrderCondition().getChargeFlag();
		if (chargeFlag != null && chargeFlag.intValue() == 1) {
			addMessage(model, "错误：此订单已对账，不允许修改。");
			model.addAttribute("canAction", false);
			return vierForm;
		}
		detail.setOrderId(orderId);
		if(CollectionUtils.isNotEmpty(order.getItems())){
			detail.setProduct(order.getItems().get(0).getProduct());
		}
		OrderCondition condition = order.getOrderCondition();
		customerId = Optional.ofNullable(condition.getCustomer()).map(t->t.getId()).orElse(0L);
		dataSource = order.getDataSourceId();
		//2020-09-24 接入云米，增加经纬度检查
		AjaxJsonEntity locationCheckResult = orderService.checkAddressLocation(dataSource,condition.getOrderId(),condition.getQuarter());
		if(!locationCheckResult.getSuccess()){
			addMessage(model, "错误：因" + locationCheckResult.getMessage() + "，不能上门服务");
			model.addAttribute("canAction","false");
			return vierForm;
		}
		detail.setServicePoint(condition.getServicePoint());
		Engineer engineer = new Engineer(condition.getEngineer().getId());
		engineer.setName(condition.getEngineer().getName());
		detail.setEngineer(engineer);

		Integer times = condition.getServiceTimes();
		times = times + 1;
		detail.setServiceTimes(times);
		detail.setOrderServiceTimes(times);
		// 显示派单时的备注
		detail.setQty(1);
		detail.setAddType(1);//异常处理
        //订单类型 2019-12-02
        detail.setServiceCategory(new Dict(condition.getOrderServiceType(),""));
        //服务类型列表(来自数据字典)
        List<Dict> serviceCategories = MSDictUtils.getDictList(Dict.DICT_TYPE_ORDER_SERVICE_TYPE);
        if(CollectionUtils.isNotEmpty(serviceCategories)){
            serviceCategories = serviceCategories.stream().filter(t->t.getIntValue()>0).collect(Collectors.toList());
        }
		//2020-11-22 远程费+其他费用总费用受控品类
		//合计费用超过设定金额，不允许派单
		//费用不超过设定金额，应收为0
		Dict limitRemoteDict = MSDictUtils.getDictByValue(condition.getProductCategoryId().toString(), OrderUtils.LIMIT_REMOTECHARGE_CATEGORY_DICT);
		if(limitRemoteDict != null){
			model.addAttribute("limitRemoteCharge",limitRemoteDict.getSort());
		}
        model.addAttribute("serviceCategories",serviceCategories);
		model.addAttribute("item", detail);
		model.addAttribute("order", order);
		model.addAttribute("canAction", true);
		model.addAttribute("customerId",customerId);
		model.addAttribute("dataSource",dataSource);
		return vierForm;

	}

	@RequiresPermissions(value = "sd:order:service")
	@ResponseBody
	@RequestMapping(value = "saveservice")
	public AjaxJsonEntity saveService(OrderDetail detail, Model model, HttpServletResponse response)
	{
		User user = UserUtils.getUser();

		response.setContentType("application/json; charset=UTF-8");
		AjaxJsonEntity jsonEntity = new AjaxJsonEntity();
		jsonEntity.setSuccess(true);

		if (!beanValidator(model, detail))
		{
			jsonEntity.setSuccess(false);
			if (model.containsAttribute("message"))
			{
				jsonEntity.setMessage((String) model.asMap().get("message"));
			} else
			{
				jsonEntity.setMessage("输入错误，请检查。");
			}
			return jsonEntity;
		}
        jsonEntity = orderService.checkServiceSubmitInfo(detail);
        if(!jsonEntity.getSuccess()){
            return jsonEntity;
        }
		try
		{
			Date date = new Date();
			detail.setCreateBy(user);
			detail.setCreateDate(date);
			orderService.addDetail(detail, false);
		} catch (Exception e)
		{
			jsonEntity.setSuccess(false);
			jsonEntity.setMessage(e.getMessage());
		}

		return jsonEntity;
	}

	/**
	 * 删除订单实际服务项目
	 *
	 * @param id	上门明细id
	 * @param orderId 订单id

	@RequiresPermissions("sd:pending:edit")
	@RequestMapping(value = "delservice")
	public String delservice(@RequestParam String id, @RequestParam String orderId,@RequestParam(required = false) String quarter, RedirectAttributes redirectAttributes)
	{
		Long lid = Long.valueOf(id);
		if (lid == null || lid<=0)
		{
			addMessage(redirectAttributes, "服务项目编号错误，无法删除");
			return "redirect:" + Global.getAdminPath() + "/sd/pending/form?orderId=" + orderId;
		}
		String lockkey = String.format(RedisConstant.SD_ORDER_LOCK,orderId);;//锁
		if(redisUtils.exists(RedisConstant.RedisDBType.REDIS_LOCK_DB,lockkey)){
			return "redirect:" + Global.getAdminPath() + "/sd/pending/form?orderId=" + orderId;
		}

		try {
			User user = UserUtils.getUser();
			OrderDetail detail = new OrderDetail();
			detail.setId(lid);
			detail.setQuarter(quarter);
			detail.setOrderId(Long.valueOf(orderId));
			detail.setAddType(1);//异常操作
			detail.setCreateBy(user);
			detail.setCreateDate(new Date());
			orderService.deleteDetail(detail);
			addMessage(redirectAttributes, "删除服务项目成功");
		}catch (Exception e){
			addMessage(redirectAttributes, "删除服务项目错误：" + e.getMessage());
		}

		return "redirect:" + Global.getAdminPath() + "/sd/pending/form?orderId=" + orderId;
	}
	 */

	@RequiresPermissions("sd:pending:edit")
	@RequestMapping(value = "tracking", method = RequestMethod.GET)
	public String tracking(String orderId,@RequestParam(required = false) String quarter, HttpServletRequest request, Model model)
	{
		Long lorderId = Long.valueOf(orderId);
		if(lorderId==null || lorderId<=0){
			model.addAttribute("canAction", false);
			return "modules/sd/tracking/orderTrackingForm";
		}

		Order order = orderService.getOrderById(lorderId, quarter,OrderUtils.OrderDataLevel.DETAIL,true);
		if (order == null || order.getOrderCondition() == null) {
			addMessage(model, "错误：系统繁忙，读取订单失败，请重试。");
			model.addAttribute("canAction", false);
			return "modules/sd/pending/orderTrackingForm";
		}
		// 检查是否可以最终
		if (!order.canTracking() && order.getOrderCondition().getPendingFlag() != 2) {
			addMessage(model, String.format("订单：%s 不能进度跟踪，当前订单状态:%s",order.getOrderNo(),order.getOrderCondition().getStatus().getLabel()));
			model.addAttribute("canAction", false);
			return "modules/sd/pending/orderTrackingForm";
		}
//		Integer chargeFlag = orderService.getChargeFlag(lorderId, quarter);
		Integer chargeFlag = order.getOrderCondition().getChargeFlag();
		if (chargeFlag != null && chargeFlag.intValue() == 1) {
			addMessage(model, "此订单已对账，不允许修改。");
			model.addAttribute("canAction", false);
			return "modules/sd/tracking/orderTrackingForm";
		}

		order.setTrackingDate(new Date());
		order.setRemarks("");
		//读取跟踪进度,statusFlag 1:订单状态变更 4：进度跟踪
		List<OrderProcessLog> list = orderService.getOrderLogsByFlags(lorderId, order.getQuarter() ,Arrays.asList(new Integer[] {1,4}),null);
		order.setLogList(list);
		model.addAttribute("order", order);

//		List<Dict> types = DictUtils.getDictList("TrackingType");
		//切换为微服务
		List<Dict> types = msDictService.findListByType("TrackingType");
		model.addAttribute("tracks", types);
		model.addAttribute("canAction", true);
		return "modules/sd/pending/orderTrackingForm";
	}



	//region ajax

	/**
	 * 完成返回(ajax)
	 */
	@RequiresPermissions("sd:pending:edit")
	@ResponseBody
	@RequestMapping(value = "ajax/save")
	public AjaxJsonEntity ajaxSave(@RequestParam String id, @RequestParam String quarter,@RequestParam String orderNo,HttpServletRequest request,HttpServletResponse response)
	{
		response.setContentType("application/json; charset=UTF-8");
		AjaxJsonEntity result = new AjaxJsonEntity(true);
		Long lid = Long.valueOf(id);
		if(lid == null || lid <= 0){
			result.setSuccess(false);
			result.setMessage("订单参数错误");
			return result;
		}
		try
		{
			User user = UserUtils.getUser();
			orderService.saveOrderPending(lid,quarter,user);
			result.setMessage("订单'" + orderNo + "'异常处理完成");
		} catch (Exception e)
		{
			result.setSuccess(false);
			result.setMessage(e.getMessage());
		}
		return result;
	}

	/**
	 * 删除订单实际服务项目
	 *
	 * @param id 服务项目ID
	 */
	@RequiresPermissions("sd:order:service")
	@ResponseBody
	@RequestMapping(value = "ajax/delservice")
	public AjaxJsonEntity ajaxdelservice(@RequestParam(required = false) String id, @RequestParam(required = false) String orderId, HttpServletResponse response)
	{
		User user = UserUtils.getUser();
		response.setContentType("application/json; charset=UTF-8");
		AjaxJsonEntity jsonEntity = new AjaxJsonEntity();
		jsonEntity.setSuccess(true);
		Long lid = Long.valueOf(id);
		if (lid == null || lid <=0)
		{
			jsonEntity.setSuccess(false);
			jsonEntity.setMessage("服务项目编号错误，无法删除");
		} else
		{
			try {
				Long lorderId = Long.valueOf(orderId);
				if(lorderId==null || lorderId<=0){
					jsonEntity.setSuccess(false);
					jsonEntity.setMessage("订单id错误，无法删除");
				}else {
					Date date = new Date();
					OrderDetail detail = new OrderDetail();
					detail.setOrderId(lorderId);
					detail.setId(lid);
					detail.setAddType(1);//异常操作
					detail.setCreateBy(user);
					detail.setCreateDate(date);
					orderService.deleteDetail(detail);
					jsonEntity.setMessage("删除服务项目成功");
				}
			}catch (Exception e)
			{
				jsonEntity.setSuccess(false);
				jsonEntity.setMessage(e.getMessage());
			}
		}
		return jsonEntity;
	}


	/**
	 * 取消好评
	 * @param id 服务项目ID
	 */
	@RequestMapping("cancelled")
	@ResponseBody
	public AjaxJsonEntity cancelledForKefu(@RequestParam String id, @RequestParam String quarter,@RequestParam String orderNo,HttpServletResponse response){
		response.setContentType("application/json; charset=UTF-8");
		AjaxJsonEntity result = new AjaxJsonEntity(true);
		Long lid = Long.valueOf(id);
		if(lid == null || lid <= 0){
			result.setSuccess(false);
			result.setMessage("订单参数错误");
			return result;
		}
		Order order = orderService.getOrderById(lid, quarter, OrderUtils.OrderDataLevel.DETAIL, true);
		if (order == null || order.getOrderCondition() == null) {
			result.setSuccess(false);
			result.setMessage("读取订单信息错误。");
			return result;
		}
		OrderCondition condition = order.getOrderCondition();
		long servicePointId = Optional.ofNullable(condition.getServicePoint()).map(t->t.getId()).orElse(0L);
		if(servicePointId == 0){
			result.setSuccess(false);
			result.setMessage("读取订单网点信息错误。");
			return result;
		}
		long cnt = 0;
		List<OrderDetail> details = order.getDetailList();
		if(!org.springframework.util.CollectionUtils.isEmpty(details)){
			cnt = details.stream().filter(t -> t.getDelFlag() == 0 && t.getServicePoint().getId().longValue() == servicePointId).count();
		}
		if(cnt>0){
			result.setSuccess(false);
			result.setMessage("该工单有上门服务项,不允许取消好评");
			return result;
		}
		Praise praise = orderPraiseService.getByOrderId(quarter,lid,servicePointId);
		if(praise==null || praise.getId()==null || praise.getId()<=0){
			result.setSuccess(false);
			result.setMessage("好评单不存在请检查");
			return result;
		}
		User user = UserUtils.getUser();
		if(user==null){
			result.setSuccess(false);
			result.setMessage("当前用户不存在,请重新登陆");
			return result;
		}
		try
		{   Praise cancelPraise = new Praise();
			cancelPraise.setId(praise.getId());
			cancelPraise.setStatus(praise.getStatus());
			cancelPraise.setServicepointId(praise.getServicepointId());
			cancelPraise.setEngineerId(praise.getEngineerId());
			cancelPraise.setOrderNo(order.getOrderNo());
			cancelPraise.setRemarks("处理审单异常取消好评单");
			cancelPraise.setQuarter(quarter);
			abnormalFormService.cancelled(cancelPraise,user);
		} catch (Exception e)
		{
			result.setSuccess(false);
			result.setMessage(e.getMessage());
		}
		return result;
	}

	//endregion ajax

}
