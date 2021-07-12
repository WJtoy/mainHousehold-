package com.wolfking.jeesite.modules.sd.web;


import com.google.common.base.Splitter;
import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.reflect.TypeToken;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.entity.md.CustomerProductModel;
import com.kkl.kklplus.entity.praise.PraiseStatusEnum;
import com.kkl.kklplus.entity.push.AppMessageType;
import com.kkl.kklplus.entity.sys.SysDict;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.config.Global;
import com.wolfking.jeesite.common.config.redis.GsonRedisSerializer;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.exception.OrderException;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.LongIDDataEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.*;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.api.util.ErrorCode;
import com.wolfking.jeesite.modules.api.util.RestResult;
import com.wolfking.jeesite.modules.fi.entity.CustomerCurrency;
import com.wolfking.jeesite.modules.fi.entity.viewModel.CustomerCurrencyModel;
import com.wolfking.jeesite.modules.fi.service.CustomerCurrencyService;
import com.wolfking.jeesite.modules.md.entity.*;
import com.wolfking.jeesite.modules.md.service.*;
import com.wolfking.jeesite.modules.md.utils.ServicePointUtils;
import com.wolfking.jeesite.modules.sd.config.CreateOrderConfig;
import com.wolfking.jeesite.modules.sd.dao.OrderItemCompleteDao;
import com.wolfking.jeesite.modules.sd.entity.*;
import com.wolfking.jeesite.modules.sd.entity.viewModel.*;
import com.wolfking.jeesite.modules.sd.service.*;
import com.wolfking.jeesite.modules.sd.utils.CreateOrderModelAdapter;
import com.wolfking.jeesite.modules.sd.utils.OrderCacheUtils;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.service.SystemService;
import com.wolfking.jeesite.modules.sys.utils.*;
import com.wolfking.jeesite.ms.b2bcenter.sd.service.B2BCenterOrderService;
import com.wolfking.jeesite.ms.entity.AppPushMessage;
import com.wolfking.jeesite.ms.providermd.service.MSRegionPermissionService;
import com.wolfking.jeesite.ms.providermd.service.ProductModelService;
import com.wolfking.jeesite.ms.service.sys.MSDictService;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import com.wolfking.jeesite.ms.validate.service.MSOrderValidateService;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresUser;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//import com.wolfking.jeesite.common.web.FormToken;

/**
 * 订单Controller
 *
 * @author Ryan
 */
@Controller
@RequestMapping(value = "${adminPath}/sd/order/")
@Slf4j
public class OrderController extends BaseController
{

	@Autowired
	private OrderService orderService;

	@Autowired
	private ProductService productService;

	@Autowired
	private FeedbackService feedbackService;

	@Autowired
	private CustomerService customerService;

	@Autowired
	private OrderAttachmentService attachmentService;

	@Autowired
	private ServiceTypeService serviceTypeService;

	@Autowired
	private AreaService areaService;

	@Autowired
	private RedisUtils redisUtils;

	@Autowired
	private ServicePointService servicePointService;

	@Resource(name = "gsonRedisSerializer")
	public GsonRedisSerializer gsonRedisSerializer;

	@Autowired
	private CustomerCurrencyService customerCurrencyService;

    @Autowired
	private UrgentLevelService urgentLevelService;
	@Autowired
    private MapperFacade mapper;

    @Autowired
	private MSDictService msDictService;

	@Autowired
	private OrderMQService orderMQService;

    @Autowired
	private ProductModelService msMDProductModelService;//微服务

	@Autowired
	private OrderEditFormService orderEditFormService;

	@Autowired
	private OrderVoiceTaskService orderVoiceTaskService;

	@Autowired
	private OrderMaterialService orderMaterialService;

	@Autowired
	private OrderAuxiliaryMaterialService orderAuxiliaryMaterialService;

	@Autowired
	private CustomerOrderService customerOrderService;

	@Autowired
    private SystemService systemService;

	@Autowired
	private OrderStatusFlagService orderStatusFlagService;

	@Autowired
	private OrderServicePointFeeService orderServicePointFeeService;

	//下单配置参数
	@Autowired
	private CreateOrderConfig createOrderConfig;

	//不发短信的数据源设定
	@Value("${shortmessage.ignore-data-sources}")
	private String smIgnoreDataSources;

	@Autowired
	private MSRegionPermissionService msRegionPermissionService;

	@Autowired
	private OrderItemCompleteService orderItemCompleteService;

	@Autowired
	private ProductCompletePicService productCompletePicService;

	@Autowired
	private CustomerProductCompletePicService customerPicService;

	@Autowired
	private B2BCenterOrderService b2BCenterOrderService;

	@Autowired
	private OrderReturnCompleteService  returnCompleteService;

	@Autowired
	private MSOrderValidateService msOrderValidateService;

	//region [客户功能]

	/**
	 * 返回客户产品-服务信息(ajax)
	 * @param id 客户id
	 * @return
	 * {products:
	 * [
	 * {id:id,name:name,model:model,brand:brand,
	 * 	services:[
	 * 		{id:id,code:code,name:name},...
	 * 	]},
	 * 	models:["model1","model2",...] //2018/12/21
	 * 	...
	 * ],
	 * expresses:[
	 * 	{label:label,value:value},...
	 * ],
	 * customer:{
	 *   balance:1000.00,credit:200.00,blockAmount:500.00,brands:[]
	 * }}
	 *
	 * @date 2018/12/21
	 * @author Ryan
	 * 下单增加客户产品已维护型号判断处理，已维护型号，只能选择其中一个或空白；未维护型号，可输入型号
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "form_products",method = RequestMethod.GET)
	public HashMap formProducts(@RequestParam(required = true) Long id,@RequestParam(value = "action",required = false,defaultValue = "new") String action, HttpServletRequest request, HttpServletResponse response){
		HashMap rtnmaps = new HashMap();
		User user = UserUtils.getUser();
		if(user==null || user.getId()==null){
			rtnmaps.put("success",false);
			rtnmaps.put("message","登录超时，请重新登录。");
			return rtnmaps;
		}
		//清除临时订单缓存
		try {
			//String cachekey = getUserTmpOrderCacheKey(request, response);
			String cachekey = OrderUtils.getUserTmpOrderCacheKey(request, response,action);
			if(StringUtils.isNotBlank(cachekey)) {
				CreateOrderModel order = (CreateOrderModel)redisUtils.get(RedisConstant.RedisDBType.REDIS_TEMP_DB,cachekey,CreateOrderModel.class);
				if(order.getCustomer() != null && order.getCustomer().getId() != null && !order.getCustomer().getId().equals(id)) {
					redisUtils.remove(RedisConstant.RedisDBType.REDIS_TEMP_DB, cachekey);
				}else{
					if(order.getItems() != null && order.getItems().size()>0) {
						rtnmaps.put("items", order.getItems());
					}
				}
			}
		}catch(Exception e){}

		List<CustomerProductVM> list = Lists.newArrayList();
		rtnmaps.put("products",list);
		rtnmaps.put("expresses",Lists.newArrayList());
		if(id==null || id<=0l){
			return rtnmaps;
		}
		List<CustomerPrice> prices = customerService.getPricesFromCache(id);
		if(prices==null || prices.size()==0){
			return rtnmaps;
		}
		Customer c = customerService.getFromCache(id);
		if(c ==null){
			rtnmaps.put("success",false);
			rtnmaps.put("message","读取厂商信息失败。");
			return rtnmaps;
		}
		//2018/04/08 管控业务员和代下单如需管控下单权限，取消注释
		//if(c.getEffectFlag() != 1){
         //   rtnmaps.put("success",false);
         //   rtnmaps.put("message","您的账户已冻结，暂不能下单，请联系管理员。");
         //   return rtnmaps;
        //}
        //2018/04/08 END
		List<String> brands = Lists.newArrayList();
		if(StringUtils.isNoneBlank(c.getDefaultBrand())) {
			Arrays.stream(c.getDefaultBrand().replace("，", ",").split(","))
					.forEach(t->{
						if(StringUtils.isNoneBlank(t)){
							brands.add(t.trim());
						}
					});
		}

		List<ServiceType> allServiceTypes = serviceTypeService.findAllList();
		Map<Product,List<CustomerPrice>> maps = prices.stream().collect(Collectors.groupingBy(CustomerPrice::getProduct));
		maps.forEach((k,v)->{
			CustomerProductVM m = new CustomerProductVM();
			m.setId(k.getId());
			m.setName(k.getName());
			m.setBrand(k.getBrand());
			m.setModel(k.getModel());
			//排序
			Product p = productService.getProductByIdFromCache(k.getId());
			if(p!=null){
				m.setSort(p.getSort());
			}else {
				m.setSort(k.getSort());
			}
			//types
			final List<ServiceType> serviceTypes = Lists.newArrayList();
			v.forEach(item ->{
				ServiceType st = allServiceTypes.stream().filter(t->Objects.equals(t.getId(),item.getServiceType().getId())).findFirst().orElse(null);
				if(st!=null){
					serviceTypes.add(st);
				}else {
					serviceTypes.add(item.getServiceType());
				}
			});
			//service types 排序
			m.setServices(serviceTypes.stream().sorted(Comparator.comparing(ServiceType::getSort)).collect(Collectors.toList()));
			list.add(m);
		});
		//按产品sort排序
		List<CustomerProductVM> products = list.stream().sorted(Comparator.comparing(CustomerProductVM::getSort)).collect(Collectors.toList());
		rtnmaps.put("products",products);
		//get model of first product
		if(products != null && !products.isEmpty()){
			CustomerProductVM firstProduct = products.get(0);
			List<CustomerProductModel> models = msMDProductModelService.getModelListFromCache(id,firstProduct.getId());
			firstProduct.setModels(models);
		}
		//Expresses
		List<Dict> expresses = MSDictUtils.getDictList("express_type");//切换为微服务
		rtnmaps.put("expresses",expresses);
		HashMap<String, Object> customer = new HashMap<String, Object>(5);
		if(action == null || !action.equalsIgnoreCase("edit")) {
			//customer finance
			CustomerFinance fi = customerService.getFinance(id);
			customer.put("balance", fi == null ? 0.00 : fi.getBalance());
			if (fi.getCreditFlag() == 1) {//支持信用额度
				customer.put("credit", fi == null ? 0.00 : fi.getCredit());
			} else {
				customer.put("credit", 0.00);
			}
			customer.put("blockAmount", fi.getBlockAmount());//已冻结金额
		}
		/*else{
			customer.put("credit", 0.00);
			customer.put("blockAmount", 0.00);//已冻结金额
		}*/
		customer.put("brands",brands);//厂商的默认品牌
		customer.put("urgentFlag",c.getUrgentFlag());
		rtnmaps.put("customer",customer);
		rtnmaps.put("success",true);
		return rtnmaps;
	}

	/**
	 * @date 2018/12/21
	 * @author Ryan
	 * 下单时，动态获取客户某产品的型号列表
	 *
	 * @param customerId	客户编号
	 * @param productId		产品编号
	 * @return AjaxJsonEntity
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "form_product_models",method = RequestMethod.GET)
	public AjaxJsonEntity formProductModels(@RequestParam(required=true) Long customerId,@RequestParam(required=true) Long productId){
		AjaxJsonEntity ajaxEntity = new AjaxJsonEntity(true);
		User user = UserUtils.getUser();
		if(user==null || user.getId()==null){
			ajaxEntity.setSuccess(false);
			ajaxEntity.setMessage("登录超时，请重新登录。");
			return ajaxEntity;
		}
		if(customerId == null || customerId ==0){
			ajaxEntity.setSuccess(false);
			ajaxEntity.setMessage("客户编号未传入。");
			return ajaxEntity;
		}
		if(productId == null || productId ==0){
			ajaxEntity.setSuccess(false);
			ajaxEntity.setMessage("产品编号未传入。");
			return ajaxEntity;
		}
		List<CustomerProductModel> models = msMDProductModelService.getModelListFromCache(customerId,productId);
		ajaxEntity.setData(models);
		return ajaxEntity;
	}

	/**
	 * 保存导入单前检查
	 * 1.检查此单是否已经转成订单
	 * 2.检查是否重复下单
	 * @param id	导入单id
	 * @param phone1
	 * @param phone2
	 * @param customerId
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "checkOrderBeforeTransfer")
	public AjaxJsonEntity checkRepeatForSaveImportOrder(
					@RequestParam String id,
					@RequestParam String phone1,
					@RequestParam String phone2,
					@RequestParam Long customerId,
					HttpServletResponse response){

		response.setContentType("application/json; charset=UTF-8");
		AjaxJsonEntity jsonEntity = new AjaxJsonEntity();
		User user = UserUtils.getUser();
		if(user==null || user.getId()==null){
			jsonEntity.setSuccess(false);
			jsonEntity.setMessage("登录超时，请重新登录。");
			return jsonEntity;
		}
		jsonEntity.setMessage("OK");
		if (StringUtils.isBlank(phone1) && StringUtils.isBlank(phone2) || customerId == null || customerId<0)
		{
			jsonEntity.setSuccess(false);
			jsonEntity.setMessage("传入参数值为空。");
			return jsonEntity;
		}
		try
		{
			/* 检查是否转单 comment by ryan at 2018/09/06 提交前不需要检查，转单前已有检查
			TempOrder o = orderService.getTempOrder(Long.valueOf(id));
			if(o==null ||  o.getCustomer() == null || o.getCustomer().getId()==null) {
				jsonEntity.setSuccess(false);
				jsonEntity.setMessage("订单信息不完整。");
				return jsonEntity;
			}
			if(o.getSuccessFlag() == 1 || o.getDelFlag() == TempOrder.DEL_FLAG_DELETE){
				jsonEntity.setSuccess(false);
				jsonEntity.setMessage(o.getSuccessFlag() == 1?"订单已转成正式订单":"订单已被取消");
				return jsonEntity;
			}
			*/
			// 检查制定天数内是否有相同用户的订单
			//String orderNo = orderService.checkRepeateOrder(phone1, phone2,customerId,30);
			String orderNo = orderService.getRepeateOrderNo(customerId,phone1);
			jsonEntity.setSuccess(true);
			jsonEntity.setData(StringUtils.isBlank(orderNo)?"":orderNo);
		} catch (Exception e)
		{
			jsonEntity.setSuccess(false);
			jsonEntity.setMessage(e.getMessage().toString());
		}
		return jsonEntity;
	}

	/**
	 * 检查当天是否有相同手机号用户已开单
	 *
	 * @param phone1 客户手机号
	 * @param phone2 电话二
	 * @param customerId 客户id
	 * @return
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "checkrepeatorder")
	public AjaxJsonEntity checkrepeatorder(@RequestParam String phone1,
										   @RequestParam(required = false) String phone2,
										   @RequestParam Long customerId,
										   HttpServletResponse response)
	{
		response.setContentType("application/json; charset=UTF-8");
		AjaxJsonEntity jsonEntity = new AjaxJsonEntity();
		User user = UserUtils.getUser();
		if(user==null || user.getId()==null){
			jsonEntity.setSuccess(false);
			jsonEntity.setMessage("登录超时，请重新登录。");
			jsonEntity.setLogin(false);
			return jsonEntity;
		}
		jsonEntity.setMessage("OK");// 默认
		//if (StringUtils.isBlank(phone1) && StringUtils.isBlank(phone2) || customerId == null || customerId<0)
		if (StringUtils.isBlank(phone1) || customerId == null || customerId < 0)
		{
			jsonEntity.setSuccess(false);
			jsonEntity.setMessage("传入参数值为空。");
			return jsonEntity;
		}

		try
		{
			// 检查制定天数内是否有相同用户的订单
			//String orderNo = orderService.checkRepeateOrder(phone1, phone2,customerId,30);
			String orderNo = orderService.getRepeateOrderNo(customerId,phone1);
			jsonEntity.setSuccess(true);
			jsonEntity.setData(StringUtils.isBlank(orderNo)?"":orderNo);
		} catch (Exception e)
		{
			jsonEntity.setSuccess(false);
			jsonEntity.setMessage(e.getMessage().toString());
		}
		return jsonEntity;
	}

	/**
	 * 待客户主帐号审核新建订单列表
	 *
	 */
	@RequiresPermissions("sd:order:approvenew")
	@RequestMapping(value = "customer/approvelist")
	public String approveNewOrderList(OrderSearchModel order, HttpServletRequest request, HttpServletResponse response, Model model)
	{
		Page<Order> page = new Page<Order>();
		User user = UserUtils.getUser();
		Boolean canSearch = true;
		//提交查询
//		if(request.getMethod().equalsIgnoreCase("post")) {
			Boolean erroFlag = false;
			//检查客户帐号信息
			if (user.isCustomer()) {
				if(user.getCustomerAccountProfile()!=null && user.getCustomerAccountProfile().getCustomer() != null){
					order.setCustomer(user.getCustomerAccountProfile().getCustomer());
				}else{
					addMessage(model,"错误：登录超时，请退出后重新登录。");
					erroFlag = true;
					canSearch = false;
				}
				//客户账号负责的店铺 2021/06/23
				List<String> shopIds = UserUtils.getShopIdsOfCustomerAccount(user);
				if(!org.springframework.util.CollectionUtils.isEmpty(shopIds)){
					order.setShopIds(shopIds);
				}
			}else if(user.isSaleman()){
				order.setSalesId(user.getId());
				order.setSubUserType(user.getSubFlag());//子账号类型
				List<Long> offlineCustomers = customerService.findIdListByOfflineOrderFlagFromCacheForSD();
				if(!org.springframework.util.CollectionUtils.isEmpty(offlineCustomers)){
					order.setOfflineCustomerList(offlineCustomers);
				}
			}
			if(erroFlag){
				model.addAttribute("page", page);
				model.addAttribute("order", order);
				return "modules/sd/customer/approveList";
			}
			order.setOrderDataLevel(OrderUtils.OrderDataLevel.FEE);
			order.setStatus(new Dict(String.valueOf(Order.ORDER_STATUS_NEW)));//新建单

			Date now = new Date();
			if(order.getBeginDate() == null){
				order.setBeginDate(DateUtils.addMonth(now, -1));
			}
			if (order.getEndDate() == null) {
				order.setEndDate(now);
			}
			order.setBeginDate(DateUtils.getStartOfDay(order.getBeginDate()));
			order.setEndDate(DateUtils.getEndOfDay(order.getEndDate()));

			try {
				page = customerOrderService.getWaitingApproveOrderList(new Page<>(request, response), order);
			} catch (Exception e) {
				addMessage(model, "错误："+e.getMessage());
			}
		model.addAttribute("page", page);
		model.addAttribute("order",order);
		model.addAttribute("canSearch",canSearch);
		return "modules/sd/customer/approveList";
	}

	/**
	 * 客户主帐号审核新订单
	 */
	@RequiresPermissions("sd:order:approvenew")
	@ResponseBody
	@RequestMapping(value = "approve")
	public AjaxJsonEntity approve(@RequestBody OrderApproveModel[] ids, HttpServletResponse response)
	{
		response.setContentType("application/json; charset=UTF-8");
		AjaxJsonEntity result = new AjaxJsonEntity(true);
		User user = UserUtils.getUser();
		//只有客户主帐号才能审核订单
		if(user.getUserType() == User.USER_TYPE_SUBCUSTOMER){
			result.setSuccess(false);
			result.setMessage("审核订单失败:客户子账号没有订单审核权限。");
			return result;
		}
		// 判断角色（登录时根据用户类型决定）
		if (ids==null || ids.length==0)
		{
			result.setSuccess(false);
			result.setMessage("审核订单失败:未参数传递参数，请选择要审核的订单。");
			return result;
		}

		StringBuilder msgs = new StringBuilder();
		Long version;
		String key = new String("");
		for (OrderApproveModel condition : ids)
		{
			key = String.format(RedisConstant.SD_ORDER,condition.getOrderId());
			version = redisUtils.hGet(RedisConstant.RedisDBType.REDIS_SD_DB,key,"version",Long.class);
			if(!Objects.equals(version.toString(),condition.getVersion())){
				msgs.append(String.format("订单:%s 信息已被其它帐号更新",condition.getOrderNo()));
				continue;
			}
			try {
				orderService.customerApproveOrder(Long.valueOf(condition.getOrderId()),condition.getOrderNo(),condition.getQuarter(), user);
			}catch (Exception e){
				msgs.append(String.format("%s :审核发生异常:%s",condition.getOrderNo(),e.getMessage()));
				log.error("订单审核发生异常:{},",condition.getOrderNo(),e);
			}
		}
		if (msgs.length() > 0) {
			result.setSuccess(false);
			result.setMessage(msgs.toString());
		}

		return result;
	}

	/**
	 * 客户取消订单 form
	 *
	 * @param id		订单id
	 */
	@RequiresPermissions("sd:order:cancel")
	@RequestMapping(value = "cancel", method = RequestMethod.GET)
	public String cancel(@RequestParam(required = true) String id,String quarter,@RequestParam(required = true) String orderNo, HttpServletRequest request, Model model)
	{
		String viewForm = "modules/sd/customer/orderCancelForm";
		Long lid = Long.valueOf(id);
		OrderApproveModel condition = new OrderApproveModel();
		condition.setOrderId(id);
		model.addAttribute("order", condition);
		boolean canAction = true;
		if (lid !=null && lid>0) {
			User user = UserUtils.getUser();
			if(user==null || user.getId() == null || user.getId()<=0){
				addMessage(model, "登录超时,请重新登录");
				model.addAttribute("canAction",false);
				return viewForm;
			}
		    if(StringUtils.isBlank(quarter)) {
				quarter = orderService.getOrderQuarterFromCache(lid);
			}
		    Order order = orderService.getOrderById(lid,quarter, OrderUtils.OrderDataLevel.CONDITION,true);
		    if(order == null || order.getOrderCondition() == null){
				addMessage(model, "订单取消操作失败：读取订单失败");
				model.addAttribute("canAction", false);
				return viewForm;
			}
			//Dict status = orderService.getConditionStatusById(lid,quarter, true);
			Dict status = order.getOrderCondition().getStatus();
			if (status == null) {
				addMessage(model, "订单取消操作失败：获取订单状态错误");
				model.addAttribute("canAction", false);
				return viewForm;
			} else {
				boolean canCanceled = true;
				if(user.isSaleman()){
					canCanceled = order.canSaleCanceled();
				}else{
					canCanceled = order.canCanceled();
				}
				// 检查是否可以取消
				//if (!OrderUtils.canCanceled(Integer.valueOf(status.getValue()))) {
				if(!canCanceled){
					addMessage(model, String.format("订单：%s 不允许取消，当前订单状态:%s", orderNo, status.getLabel()));
					model.addAttribute("canAction", false);
					return viewForm;
				}
				//检查配件单
				if(order.getOrderCondition().getPartsFlag() == 1){
					MSResponse msResponse = orderMaterialService.canGradeOfMaterialForm(order.getDataSourceId(),order.getId(),order.getQuarter());
					if(!MSResponse.isSuccessCode(msResponse)){
						addMessage(model, "错误：" + msResponse.getMsg());
						model.addAttribute("canAction", false);
						return viewForm;
					}
				}
			}
		}else{
			addMessage(model, "订单取消操作失败：订单参数读取失败");
			canAction = false;
		}
		model.addAttribute("canAction", canAction);
		return viewForm;
	}

	/**
	 * 客户ajax提交取消订单
	 *
	 * @param order
	 * @param response
	 * @return
	 */
	@RequiresPermissions("sd:order:cancel")
	@ResponseBody
	@RequestMapping(value = "cancel", method = RequestMethod.POST)
	public AjaxJsonEntity cancel(OrderApproveModel order, HttpServletResponse response)
	{
		response.setContentType("application/json; charset=UTF-8");
		AjaxJsonEntity result = new AjaxJsonEntity(true);
		try
		{
			User user = UserUtils.getUser();
			orderService.cancelOrder(Long.valueOf(order.getOrderId()),user,order.getRemarks());
		} catch (OrderException oe){
            result.setSuccess(false);
            result.setMessage(oe.getMessage());
        } catch (Exception e) {
			result.setSuccess(false);
			result.setMessage(e.getMessage());
			log.error("[OrderController.cancel] orderId:{}",order.getOrderId(),e);
		}
		return result;
	}


	/**
	 * 客户加急设定 form
	 *
	 * @param id		订单id
	 */
	@RequiresPermissions("sd:order:edit")
	@RequestMapping(value = "urgent", method = RequestMethod.GET)
	public String urgent(@RequestParam String id,@RequestParam String quarter,@RequestParam String orderNo, HttpServletRequest request, Model model)
	{
		String viewForm = "modules/sd/customer/urgentForm";
		Long lid = Long.valueOf(id);
		UrgentModel order = new UrgentModel();
		boolean canAction = false;
		if (lid !=null && lid>0) {
			order.setOrderId(lid);
			order.setQuarter(quarter);
			order.setOrderNo(orderNo);
			model.addAttribute("order", order);
			Order o = orderService.getOrderById(lid,quarter, OrderUtils.OrderDataLevel.STATUS,true);
			if(o == null || o.getOrderCondition() == null || o.getOrderStatus() == null){
				addMessage(model, "读取订单信息错误，请重试！");
				model.addAttribute("canAction", canAction);
				return viewForm;
			}
			//检查状态
			int statusValue = o.getOrderCondition().getStatus().getIntValue();
			//TODO: APP完工[55]
//			if(statusValue > Order.ORDER_STATUS_SERVICED){
			if(statusValue > Order.ORDER_STATUS_APP_COMPLETED){
				addMessage(model, String.format("不能设定加急，当前订单状态:%s", o.getOrderCondition().getStatus().getLabel()));
				model.addAttribute("canAction", canAction);
				return viewForm;
			}
			/*是否可以加急，如更改过一次加急，不能再加急  -> 更改为不限定次数 2018-07-03
			//判断依据，orderStatus.urgentDate != orderCondition.createDate
			if(o.getOrderStatus().getUrgentDate() != null && o.getOrderStatus().getUrgentDate().getTime() != o.getOrderCondition().getCreateDate().getTime()){
				addMessage(model, "订单已修改过加急，不允许再次设定加急！");
				model.addAttribute("canAction", canAction);
				return viewForm;
			} */
			//加急等级
			UrgentLevel oUrgentLevel = o.getOrderCondition().getUrgentLevel();
			List<UrgentLevel> urgentLevels = urgentLevelService.findAllList();
			if(urgentLevels == null || urgentLevels.size() == 0){
				addMessage(model, "无加急等级设定数据！");
				model.addAttribute("canAction", canAction);
				return viewForm;
			}
			order.setAreaId(o.getOrderCondition().getArea().getId());
			order.setCustomerId(o.getOrderCondition().getCustomer().getId());
			if(oUrgentLevel.getId().longValue() == 0){
				order.setUrgentLevels(urgentLevels);
			}else{
				UrgentLevel urgentLevel = urgentLevels.stream().filter(t->t.getId().longValue() == oUrgentLevel.getId().longValue()).findFirst().orElse(null);
				if(urgentLevel == null){
					addMessage(model, "当前加急等级不存在！");
					model.addAttribute("canAction", canAction);
					return viewForm;
				}
				urgentLevels = urgentLevels.stream().filter(t->t.getSort()<urgentLevel.getSort()).collect(Collectors.toList());
				/*已是最高等级,0~24
				if(urgentLevels.size() == 0){
					addMessage(model, "当前加急等级已是最高等级，无法再设定！");
					model.addAttribute("canAction", canAction);
					return viewForm;
				}*/
				//urgentLevels.add(0,new UrgentLevel(0l,"不加急"));
                if(urgentLevels.size() == 0){
                    addMessage(model, "当前加急：" + urgentLevel.getRemarks() +",无其他可选加急项目");
                    model.addAttribute("canAction", canAction);
                    return viewForm;
                }
				order.setUrgentLevels(urgentLevels);
			}
			canAction = true;
		}else{
			addMessage(model, "订单加急操作失败：参数错误");
			canAction = false;
		}
		model.addAttribute("canAction", canAction);
		return viewForm;
	}


	/**
	 * 客户ajax提交取消订单
	 *
	 * @param urgentModel
	 * @param response
	 * @return
	 */
	@RequiresPermissions("sd:order:edit")
	@ResponseBody
	@RequestMapping(value = "setUrgent", method = RequestMethod.POST)
	public AjaxJsonEntity setUrgent(UrgentModel urgentModel,HttpServletRequest request, HttpServletResponse response)
	{
		response.setContentType("application/json; charset=UTF-8");
		AjaxJsonEntity result = new AjaxJsonEntity(true);
		try
		{
			User user = UserUtils.getUser();
			urgentModel.setCreateBy(user);
			String chargeValues = CookieUtils.getCookie(request,"data");
			if(StringUtils.isNotBlank(chargeValues)){
				CookieUtils.setCookie(response,"data","",0);//清除cookie
				//检查前台回传和cookie中是否一致
				Map<String,String> charges = Splitter.onPattern("[,;]{1,}")
						.withKeyValueSeparator(':')
						.split(chargeValues);
				if(!charges.containsKey("chargeIn") || !charges.containsKey("chargeOut")){
					result.setSuccess(false);
					result.setMessage("页面提交数据疑似被串改");
					return result;
				}
				Double chargeIn = Double.valueOf(charges.get("chargeIn"));
				Double chargeOut = Double.valueOf(charges.get("chargeOut"));
				if(urgentModel.getChargeIn().doubleValue() != chargeIn.doubleValue()
						|| urgentModel.getChargeOut().doubleValue() != chargeOut.doubleValue()){
					result.setSuccess(false);
					result.setMessage("页面提交数据疑似被串改");
					return result;
				}
			}
			orderService.setUrgent(urgentModel);
		} catch (OrderException oe){
            result.setSuccess(false);
            result.setMessage(oe.getMessage());
        } catch (Exception e) {
			result.setSuccess(false);
			result.setMessage(e.getMessage());
			log.error("[OrderController.setUrgent]orderId:{}",urgentModel.getOrderId(),e);
		}
		return result;
	}

	//region 列表

	/**
	 * [Ajax]厂商退补单列表
	 */
	@ResponseBody
	@RequestMapping(value = "customerReturnAndAdditionalList")
	public AjaxJsonEntity customerReturnAndAdditionalList(@RequestParam String orderNo,@RequestParam String quarter,HttpServletResponse response)
	{
		User user = UserUtils.getUser();
		response.setContentType("application/json; charset=UTF-8");
		AjaxJsonEntity jsonEntity = new AjaxJsonEntity(true);
		try
		{

			if(StringUtils.isBlank(orderNo)){
				jsonEntity.setSuccess(false);
				jsonEntity.setMessage("订单号为空");
				return jsonEntity;
			}
			//if (!user.isCustomer()) {
			//	jsonEntity.setSuccess(false);
			//	jsonEntity.setMessage("您没有权限查看厂商退补");
			//	return jsonEntity;
			//}
			List<CustomerCurrency> list = customerCurrencyService.getByOrderNoAndActionTypes(orderNo,new Integer[]{30,40});
			if(list ==null){
				jsonEntity.setData(Lists.newArrayList());
			}else {
                final Map<String,Dict> dicts = MSDictUtils.getDictMap("ServicePointActionType");//切换为微服务
                List<CustomerCurrencyModel> rtnList = Lists.newArrayList();
                CustomerCurrencyModel model;
                CustomerCurrency customerCurrency;
                Dict dict;
                Dict actionType;
                for(int i=0,size=list.size();i<size;i++){
                    customerCurrency = list.get(i);
                    model = mapper.map(customerCurrency,CustomerCurrencyModel.class);
                    actionType = new Dict(customerCurrency.getActionType().toString());
                    if(dicts != null){
                        dict = dicts.get(customerCurrency.getActionType().toString());
                        if(dict !=null){
                            actionType.setLabel(dict.getDescription());
                        }else{
                            actionType.setLabel("读取失败，请刷新");
                        }
                    }
                    model.setActionType(actionType);
                    rtnList.add(model);
                }
				jsonEntity.setData(rtnList);
			}
		} catch (Exception e)
		{
			jsonEntity.setSuccess(false);
			jsonEntity.setMessage(e.getMessage());
		}
		return jsonEntity;
	}

    //endregion 列表

	//endregion [客户功能]

	//region [客服功能]

    //region 接单

	/**
	 * 接单 form (暂不使用)
	 *
	 * @param orderId
	 */
	@RequiresPermissions("sd:order:accept")
	@RequestMapping(value = "accept", method = RequestMethod.GET)
	public String accept(String orderId, HttpServletRequest request, Model model)
	{
		String viewForm = "modules/sd/service/acceptForm";
		Order order = new Order();
		Long lorderId = Long.valueOf(orderId);
		if (lorderId!=null && lorderId>0)
		{
			order = orderService.getOrderById(lorderId, "",OrderUtils.OrderDataLevel.CONDITION,true);
			if(order == null || order.getOrderCondition() == null){
				addMessage(model, "错误：读取订单失败，请重试");
				model.addAttribute("order", new Order());
				return viewForm;
			}
			// 检查是否可以取消
			if (!order.canAccept())
			{
				addMessage(
						model,
						String.format("操作失败，订单：%s 无法接单，当前订单状态:%s",
								order.getOrderNo(),
								order.getOrderCondition().getStatus().getLabel()
						)
				);
				model.addAttribute("order", order);
				return viewForm;
			}
		}else{
			addMessage(model,"参数错误");
		}
		order.setRemarks("");
		model.addAttribute("order", order);
		return viewForm;
	}

	/**
	 * ajax提交接单信息
	 *
	 * @param order
	 */
	@RequiresPermissions("sd:order:accept")
	@ResponseBody
	@RequestMapping(value = "accept", method = RequestMethod.POST)
	public AjaxJsonEntity accept(Order order, HttpServletResponse response)
	{
		AjaxJsonEntity result = new AjaxJsonEntity(true);
		try
		{
			if (order.getId() == null){
				result.setSuccess(false);
				result.setMessage("订单参数错误");
				return result;
			}
//			Order o = orderService.getOrderById(order.getId(), OrderUtils.OrderDataLevel.CONDITION,true);
//			if(!o.canAccept()){
//				result.setSuccess(false);
//				result.setMessage("该订单不能接单，请刷新页面查看订单是否已取消或其他人员已接单。");
//				return result;
//			}
			orderService.acceptOrder(order.getId(),order.getQuarter(),UserUtils.getUser(),order.getRemarks());
		} catch (OrderException oe){
            result.setSuccess(false);
            result.setMessage(oe.getMessage());
        } catch (Exception e)
		{
			result.setSuccess(false);
			result.setMessage(e.getMessage());
			log.error("[OrderController.accept] orderId:{}",order.getId(),e);
		}
		return result;
	}

	/**
	 * 安维人员的接单操作
	 *
	 * @param orderId
	 * @param response
	 * @return
	 */

	@RequiresPermissions("sd:order:engineeraccept")
	@ResponseBody
	@RequestMapping(value = "engineerAcceptOrder", method = RequestMethod.POST)
	public AjaxJsonEntity engineerAcceptOrder(String orderId,String quarter, HttpServletResponse response)
	{
		AjaxJsonEntity result = new AjaxJsonEntity(true);
		try
		{
			Long lorderId = Long.valueOf(orderId);
			if(lorderId==null || lorderId<=0){
				result.setSuccess(false);
				result.setMessage("参数错误");
			}else {
				orderService.engineerAcceptOrder(lorderId,quarter);
			}
		} catch (OrderException oe){
            result.setSuccess(false);
            result.setMessage(oe.getMessage());
        } catch (Exception e) {
			result.setSuccess(false);
			result.setMessage(e.getMessage());
			log.error("[OrderController.engineerAcceptOrder] orderId:{}",orderId,e);
		}
		return result;
	}

    //endregion 接单

	//region 退单

	/**
	 * 退单申请 form
	 *
	 * @param orderId 订单id
	 */
	@RequiresPermissions("sd:order:return")
	@RequestMapping(value = "return", method = RequestMethod.GET)
	public String returnorder(String orderId,String quarter, HttpServletRequest request, Model model) {
//		String viewForm = "modules/sd/service/returnOrderRequiredVerifyCodeForm";
		String viewForm = "modules/sd/service/returnForm";
		OrderCondition condition = new OrderCondition();
		Long lorderId = Long.valueOf(orderId);
		if (lorderId == null || lorderId<=0) {
			addMessage(model, "订单参数错误");
			model.addAttribute("order", condition);
			model.addAttribute("canReturn",false);
			return viewForm;
		}
		Order order = orderService.getOrderById(lorderId,quarter, OrderUtils.OrderDataLevel.CONDITION,true);
		if(order == null || order.getOrderCondition() == null){
			addMessage(model, "读取订单失败");
			model.addAttribute("order", condition);
			model.addAttribute("canReturn",false);
			return viewForm;
		}
		//云米B2B维修单退单时需要验证码，故跳转到新的页面
		if (B2BDataSourceEnum.isB2BDataSource(order.getDataSourceId()) && B2BDataSourceEnum.VIOMI.id == order.getDataSourceId()
				&& OrderUtils.checkOrderServiceType(order, OrderUtils.OrderTypeEnum.REPAIRE)) {
			viewForm = "modules/sd/service/returnOrderRequiredVerifyCodeForm";
		}
		condition = order.getOrderCondition();
        //检查配件单
        if(condition.getPartsFlag() == 1) {
            MSResponse msResponse = orderMaterialService.canGradeOfMaterialForm(order.getDataSourceId(),lorderId, quarter);
            if (!MSResponse.isSuccessCode(msResponse)) {
                addMessage(model, "错误：" + msResponse.getMsg());
                model.addAttribute("order", condition);
                model.addAttribute("canReturn", false);
                //需手动关闭
                if (msResponse.getCode() == 1) {
                    model.addAttribute("manuCloseMaterialForm", 1);
                }
                return viewForm;
            }
        }
		Dict status = condition.getStatus();
		/*
		String key = String.format(RedisConstant.SD_ORDER, orderId);
		Dict status = redisUtils.hGet(RedisConstant.RedisDBType.REDIS_SD_DB, key, "status", Dict.class);
		*/
		Integer statusValue = Integer.parseInt(status.getValue());
		if (statusValue > Order.ORDER_STATUS_RETURNING || statusValue < Order.ORDER_STATUS_ACCEPTED) {
			addMessage(model, "该订单不能退单，请刷新列表，确认订单状态。");
			model.addAttribute("canReturn",false);
		}else{
			model.addAttribute("canReturn",true);
		}
		condition.setRemarks("");
		model.addAttribute("order", condition);
		return viewForm;
	}

	/**
	 * ajax提交退单申请
	 *
	 * @param order
	 * @param response
	 * @return
	 */
	@RequiresPermissions("sd:order:return")
	@ResponseBody
	@RequestMapping(value = "return", method = RequestMethod.POST)
	public AjaxJsonEntity returnorder(OrderCondition order, HttpServletResponse response)
	{
		AjaxJsonEntity result = new AjaxJsonEntity(true);
		try
		{
			orderService.returnOrder(order.getOrderId(),order.getPendingType(),order.getRemarks());
		} catch (OrderException oe){
            result.setSuccess(false);
            result.setMessage(oe.getMessage());
        } catch (Exception e){
			result.setSuccess(false);
			result.setMessage(e.getMessage());
			if(order != null && order.getOrderId() != null) {
				log.error("[OrderController.returnorder] orderId:{}",order.getOrderId(),e);
			}else{
				log.error("[OrderController.returnorder]", e);
			}
		}
		return result;
	}

	/**
	 * 发送退单验证码
	 */
	@RequiresPermissions("sd:order:return")
	@ResponseBody
	@RequestMapping(value = "sendReturnVerifyCode", method = RequestMethod.POST)
	public AjaxJsonEntity getReturnVerifyCode(OrderCondition order, HttpServletResponse response) {
		AjaxJsonEntity result = new AjaxJsonEntity(true);
		try {
			User user = UserUtils.getUser();
			orderService.sendReturnVerifyCode(order.getOrderId(), order.getQuarter() ,order.getPendingType(),order.getRemarks(), user);
		} catch (OrderException oe){
			result.setSuccess(false);
			result.setMessage(oe.getMessage());
		} catch (Exception e){
			result.setSuccess(false);
			result.setMessage(e.getMessage());
			if(order != null && order.getOrderId() != null) {
				log.error("[OrderController.sendReturnVerifyCode] orderId:{}",order.getOrderId(),e);
			}else{
				log.error("[OrderController.sendReturnVerifyCode]", e);
			}
		}
		return result;
	}

	/**
	 * ajax提交退单申请
	 */
	@RequiresPermissions("sd:order:return")
	@ResponseBody
	@RequestMapping(value = "returnOrderWithVerifyCode", method = RequestMethod.POST)
	public AjaxJsonEntity returnOrderWithVerifyCode(OrderCondition order, HttpServletResponse response)
	{
		AjaxJsonEntity result = new AjaxJsonEntity(true);
		User user = UserUtils.getUser();
		if (order.getPendingType() == null || StringUtils.isEmpty(order.getPendingType().getValue()) ){
			result.setSuccess(false);
			result.setMessage("错误：退单类型不能为空");
			return result;
		}
		if (StringUtils.isBlank(order.getVerifyCode())){
			result.setSuccess(false);
			result.setMessage("错误：验证码不能为空");
			return result;
		}
		try {
			orderService.returnOrderNew(order.getOrderId(),order.getPendingType(), order.getVerifyCode(), order.getRemarks(), user);
		} catch (OrderException oe){
			result.setSuccess(false);
			result.setMessage(oe.getMessage());
		} catch (Exception e){
			result.setSuccess(false);
			result.setMessage(e.getMessage());
			if(order != null && order.getOrderId() != null) {
				log.error("[OrderController.returnOrderWithVerifyCode] orderId:{}",order.getOrderId(),e);
			}else{
				log.error("[OrderController.returnOrderWithVerifyCode]", e);
			}
		}
		return result;
	}

	/**
	 * 审核退单 KefuOrderController.orderReturnApproveList 
	 * @param action 审核操作 approve-通过审核 reject-驳回申请
	 * @param json 	 退单订单列表,[{orderId:1,quarter:"20194"}]
	 */
	@RequiresPermissions("sd:order:approvereturn")
	@ResponseBody
	@RequestMapping(value = "approvereturn")
	public AjaxJsonEntity approvereturn(@RequestParam String json, @RequestParam String action, HttpServletResponse response)
	{
		response.setContentType("application/json; charset=UTF-8");
		AjaxJsonEntity result = new AjaxJsonEntity(true);
		User user = UserUtils.getUser();
		if (StringUtils.isBlank(json) || StringUtils.isBlank(action)){
			result.setSuccess(false);
			result.setMessage("提交数据失败:未参数传递参数，请选择要操作的订单。");
			return result;
		}
		try
		{
			json = StringEscapeUtils.unescapeHtml4(json);
			List<OrderReturnViewModel> orders = GsonUtils.getInstance().getGson().fromJson(json,new TypeToken<List<OrderReturnViewModel>>() {}.getType());
			StringBuilder msgs = new StringBuilder();
			OrderReturnViewModel order;
			if (action.equalsIgnoreCase("approve")) {
				//MSResponse<String> msResponse;
				//Long orderId;
				for (int i=0,size=orders.size();i<size;i++){
					try {
						order = orders.get(i);
						orderService.approveReturnOrderNew(order.getOrderId(),order.getQuarter(), "",user);
					}catch (Exception e) {
						msgs.append(e.getMessage().replace("java.lang.RuntimeException:","") + "</br>");
					}
				}
			} else {
				for (int i=0,size=orders.size();i<size;i++){
					order = orders.get(i);
					try {
						orderService.rejectReturnOrderNew(order.getOrderId(),order.getQuarter(), "",user);
					}catch (Exception e) {
						msgs.append(e.getMessage().replace("java.lang.RuntimeException:","") + "</br>");
					}
				}
			}
			if (msgs.length() > 0)
			{
				result.setSuccess(false);
				result.setMessage(msgs.toString());
			}
		} catch (OrderException oe){
            result.setSuccess(false);
            result.setMessage(oe.getMessage());
        } catch (Exception e){
			result.setSuccess(false);
			result.setMessage((action.equalsIgnoreCase("approve") ? "同意退单申请"
					: "驳回退单申请").concat("时发生异常:").concat(e.getMessage()));
			log.error("[OrderController..approvereturn] - orders:{}", json, e);
		}
		return result;
	}

	/**
	 * 审核退单
	 */
	@RequiresPermissions("sd:order:approvereturn")
	@ResponseBody
	@RequestMapping(value = "approveReturnB2bOrder")
	public AjaxJsonEntity approveReturnB2bOrder(@RequestParam String json, @RequestParam String action, HttpServletResponse response) {
		response.setContentType("application/json; charset=UTF-8");
		AjaxJsonEntity result = new AjaxJsonEntity(true);
		User user = UserUtils.getUser();
		if (StringUtils.isBlank(json) || StringUtils.isBlank(action)){
			result.setSuccess(false);
			result.setMessage("提交数据失败:未参数传递参数，请选择要操作的订单。");
			return result;
		}
		try
		{
			json = StringEscapeUtils.unescapeHtml4(json);
			List<OrderReturnViewModel> orders = GsonUtils.getInstance().getGson().fromJson(json,new TypeToken<List<OrderReturnViewModel>>() {}.getType());
			StringBuilder msgs = new StringBuilder();
			OrderReturnViewModel order;
			if (action.equalsIgnoreCase("approve")) {
				//MSResponse<String> msResponse;
				//Long orderId;
				for (int i=0,size=orders.size();i<size;i++){
					try {
						order = orders.get(i);
						orderService.kklApproveReturnB2bOrder(order.getOrderId(),order.getQuarter(), "",user);
					}catch (Exception e) {
						msgs.append(e.getMessage().replace("java.lang.RuntimeException:","") + "</br>");
					}
				}
			} else {
				for (int i=0,size=orders.size();i<size;i++){
					order = orders.get(i);
					try {
						orderService.kklRejectReturnB2bOrder(order.getOrderId(),order.getQuarter(), "",user);
					}catch (Exception e) {
						msgs.append(e.getMessage().replace("java.lang.RuntimeException:","") + "</br>");
					}
				}
			}
			if (msgs.length() > 0)
			{
				result.setSuccess(false);
				result.setMessage(msgs.toString());
			}
		} catch (OrderException oe){
			result.setSuccess(false);
			result.setMessage(oe.getMessage());
		} catch (Exception e){
			result.setSuccess(false);
			result.setMessage((action.equalsIgnoreCase("approve") ? "同意退单申请"
					: "驳回退单申请").concat("时发生异常:").concat(e.getMessage()));
			log.error("[OrderController..approvereturn] - orders:{}", json, e);
		}
		return result;
	}


	//endregion 退单

    //region 订单详情页

	/**
	 * 查看订单明细(客服)
	 * @param id	订单id
	 * @return
	 */
	@RequestMapping(value = { "kefu/orderDetailInfo" })
	public String kefuOrderDetailInfo(@RequestParam String id,@RequestParam String quarter,String refreshParent, HttpServletRequest request,HttpServletResponse response, Model model)
	{
		Boolean errorFlag = false;
		Order order = new Order();
		Long lid = Long.valueOf(id);
		if (lid == null || lid <= 0)
		{
			errorFlag = true;
			addMessage(model, "订单代码传递错误");
		} else
		{
			order = orderService.getOrderById(lid, quarter,OrderUtils.OrderDataLevel.DETAIL,true);
			if(order == null || order.getOrderCondition() == null){
				errorFlag = true;
				addMessage(model, "错误：系统繁忙，读取订单失败，请重试。");
			}else {
				ServicePoint servicePoint = order.getOrderCondition().getServicePoint();
				if (servicePoint != null && servicePoint.getId() != null & servicePoint.getId() > 0) {
					Engineer engineer = servicePointService.getEngineerFromCache(servicePoint.getId(), order.getOrderCondition().getEngineer().getId());
					if (engineer != null) {
						User engineerUser = new User(engineer.getId());
						engineerUser.setName(engineer.getName());
						engineerUser.setMobile(engineer.getContactInfo());
						engineerUser.setSubFlag(engineer.getMasterFlag() == 1 ? 0 : 1);
						order.getOrderCondition().setEngineer(engineerUser);
					}
				}
			}
		}
		//2020-09-10 屏蔽客户(应收)自动同步加的远程费及其他费用
		User user = UserUtils.getUser();
		if(user.isKefu()) {
			OrderUtils.customerSyncChargeActionShield(order);
		}
		model.addAttribute("order", order);
		model.addAttribute("errorFlag",errorFlag);
		if(!errorFlag) {
			model.addAttribute("fourServicePhone", MSDictUtils.getDictSingleValue("400ServicePhone", "400-666-3653"));
		}else{
			model.addAttribute("fourServicePhone", "400-666-3653");
		}
		model.addAttribute("refreshParent",StringUtils.isBlank(refreshParent)?"true":refreshParent);//调用方法决定是否在关闭详情页后刷新iframe
		String changed = request.getParameter("changed");
//        if(StringUtils.isNoneBlank(changed)) {
//            CookieUtils.setCookie(response, "orderDetail.changed", changed);
//        }
		model.addAttribute("changed",StringUtils.isBlank(changed)?"false":changed);
		return "modules/sd/service/orderDefailInfoKefuForm";
	}

	/**
	 * 查看订单明细(For负责派单的客服)
	 * @param id	订单id
	 * @return
	 */
	@RequestMapping(value = { "plan/orderDetailInfo" })
	public String planOrderDetailInfo(@RequestParam String id,@RequestParam String quarter,String refreshParent, HttpServletRequest request,HttpServletResponse response, Model model)
	{
		Boolean errorFlag = false;
		Order order = new Order();
		Long lid = Long.valueOf(id);
		if (lid == null || lid <= 0)
		{
			errorFlag = true;
			addMessage(model, "订单代码传递错误");
		} else
		{
			order = orderService.getOrderById(lid, quarter,OrderUtils.OrderDataLevel.DETAIL,true);
			if(order == null || order.getOrderCondition() == null){
				errorFlag = true;
				addMessage(model, "错误：系统繁忙，读取订单失败，请重试。");
			}else {
				ServicePoint servicePoint = order.getOrderCondition().getServicePoint();
				if (servicePoint != null && servicePoint.getId() != null & servicePoint.getId() > 0) {
					Engineer engineer = servicePointService.getEngineerFromCache(servicePoint.getId(), order.getOrderCondition().getEngineer().getId());
					if (engineer != null) {
						User engineerUser = new User(engineer.getId());
						engineerUser.setName(engineer.getName());
						engineerUser.setMobile(engineer.getContactInfo());
						engineerUser.setSubFlag(engineer.getMasterFlag() == 1 ? 0 : 1);
						order.getOrderCondition().setEngineer(engineerUser);
					}
				}
			}
		}
		model.addAttribute("order", order);
		model.addAttribute("errorFlag",errorFlag);
		if(!errorFlag) {
			model.addAttribute("fourServicePhone", MSDictUtils.getDictSingleValue("400ServicePhone", "400-666-3653"));
		}else{
			model.addAttribute("fourServicePhone", "400-666-3653");
		}
		model.addAttribute("refreshParent",StringUtils.isBlank(refreshParent)?"true":refreshParent);//调用方法决定是否在关闭详情页后刷新iframe
		String changed = request.getParameter("changed");
//        if(StringUtils.isNoneBlank(changed)) {
//            CookieUtils.setCookie(response, "orderDetail.changed", changed);
//        }
		model.addAttribute("changed",StringUtils.isBlank(changed)?"false":changed);
		return "modules/sd/service/orderDefailInfoPlanForm";
	}

	/**
	 * 查看订单明细 for 客户，业务
	 * @param id	订单id
	 * @return
	 */
	@RequestMapping(value = { "orderDetailInfo" })
	public String orderDetailInfo(String id,String quarter, HttpServletRequest request, Model model)
	{
		Boolean errorFlag = false;
		Order order = new Order();
		Long lid = Long.valueOf(id);
		boolean hasAuxiliaryMaterils = false;
		if (lid == null || lid <= 0)
		{
			errorFlag = true;
			addMessage(model, "订单参数错误");
		} else
		{
			order = orderService.getOrderById(lid, quarter,OrderUtils.OrderDataLevel.DETAIL,true);
			if(order == null || order.getOrderCondition() == null){
				errorFlag = true;
				addMessage(model, "错误：系统繁忙，读取订单失败，请重试。");
			}else {
				ServicePoint servicePoint = order.getOrderCondition().getServicePoint();
				if (servicePoint != null && servicePoint.getId() != null & servicePoint.getId() > 0) {
					Engineer engineer = servicePointService.getEngineerFromCache(servicePoint.getId(), order.getOrderCondition().getEngineer().getId());
					if (engineer != null) {
						User engineerUser = new User(engineer.getId());
						engineerUser.setName(engineer.getName());
						engineerUser.setMobile(engineer.getContactInfo());
						engineerUser.setSubFlag(engineer.getMasterFlag() == 1 ? 0 : 1);
						order.getOrderCondition().setEngineer(engineerUser);
					}
				}
				hasAuxiliaryMaterils = orderAuxiliaryMaterialService.hasAuxiliaryMaterials(order.getId(), order.getQuarter());
			}
		}
		model.addAttribute("order", order);
		model.addAttribute("hasAuxiliaryMaterils", hasAuxiliaryMaterils ? 1 : 0);
		model.addAttribute("errorFlag",errorFlag);
		if(!errorFlag) {
			model.addAttribute("fourServicePhone", MSDictUtils.getDictSingleValue("400ServicePhone", "400-666-3653"));
		}else{
			model.addAttribute("fourServicePhone", "400-666-3653");
		}
		 //好评单是否可见
		int praiseFlag = 0;
		OrderStatusFlag orderStatusFlag = orderStatusFlagService.getByOrderId(lid,quarter);
		if(orderStatusFlag !=null && orderStatusFlag.getPraiseStatus()== PraiseStatusEnum.APPROVE.code){
			praiseFlag = 1;
		}
		model.addAttribute("praiseFlag",praiseFlag);
		return "modules/sd/service/orderDefailInfoForm";
	}

    //endregion 订单详情页

    //region 操作

	/**
	 * 派单 form
	 *
	 * @param orderId
	 */
	@RequiresPermissions(value = { "sd:order:plan", "sd:order:engineerplan" }, logical = Logical.OR)
	@RequestMapping(value = "plan", method = RequestMethod.GET)
	//@FormToken(save = true)
	public String plan(String orderId,String quarter, @RequestParam(name = "crushPlanFlag", required = false, defaultValue = "0") Integer crushPlanFlag, HttpServletRequest request, Model model) {
		Order order = new Order();
		Long lorderId = Long.valueOf(orderId);
		if (lorderId == null || lorderId <= 0) {
			addMessage(model, "错误：订单号丢失");
			model.addAttribute("canSave", false);
			model.addAttribute("order", order);
			return "modules/sd/service/orderPlanForm";
		}
		String lockkey = String.format(RedisConstant.SD_ORDER_LOCK,orderId);
		if(redisUtils.exists(RedisConstant.RedisDBType.REDIS_LOCK_DB,lockkey)){
			addMessage(model,"错误:此订单正在处理中，请稍候重试，或刷新页面。");
			model.addAttribute("canSave", false);
			model.addAttribute("order", order);
			return "modules/sd/service/orderPlanForm";
		}

		order = orderService.getOrderById(lorderId, quarter,OrderUtils.OrderDataLevel.FEE, true);
		if(order == null || order.getOrderCondition() == null){
			addMessage(model, "错误：系统繁忙，读取订单失败，请重试。");
			model.addAttribute("canSave", false);
			model.addAttribute("order", order);
			return "modules/sd/service/orderPendingTypeForm";
		}
		// 检查是否可以取消
		if (!order.canPlanOrder()) {
			addMessage(model, String.format("错误:订单：%s 无法派单，当前订单状态:",order.getOrderNo(),order.getOrderCondition().getStatus().getLabel()));
			model.addAttribute("canSave", false);
			model.addAttribute("order", order);
			return "modules/sd/service/orderPlanForm";
		}

        //厂商远程费标识 add on 2020-3-24
        OrderCondition orderCondition = order.getOrderCondition();
		Integer customerRemoteFee = 1;
		// 检查是否为受控品类：自动同步应收远程费和其他费用
		Dict dict = MSDictUtils.getDictByValue(orderCondition.getProductCategoryId().toString(), OrderUtils.SYNC_CUSTOMER_CHARGE_DICT);
		if (dict != null && dict.getValue().equals(orderCondition.getProductCategoryId().toString())) {
			customerRemoteFee = 0;
		} else if(orderCondition.getCustomer()!=null && orderCondition.getCustomer().getId()>0){
		    Customer customer = customerService.getFromCache(orderCondition.getCustomer().getId());
		    if(customer!=null){
                customerRemoteFee = customer.getRemoteFeeFlag();
            }
        }

		Integer areaRemoteFee = msRegionPermissionService.getRemoteFeeStatusFromCacheForSD(orderCondition.getProductCategoryId(),orderCondition.getArea().getId(),orderCondition.getSubArea().getId());
		List<Dict> travelCharges = msDictService.findListByType("TravelChargeConfig");
		int freeDistance = 20;
		double price = 1.0;
		if(travelCharges != null && travelCharges.size() > 0){
			for(int i=0,size=travelCharges.size();i<size;i++){
				dict = travelCharges.get(i);
				if(dict.getValue().equalsIgnoreCase("FreeDistance")){
					freeDistance = Integer.valueOf(dict.getLabel());
				}else if(dict.getValue().equalsIgnoreCase("Price")) {
					price = Double.valueOf(dict.getLabel());
				}
			}
		}
		//远程费+其他费用总费用受控品类
		//合计费用超过设定金额，不允许派单
		//费用不超过设定金额，应收为0
		Dict limitRemoteDict = MSDictUtils.getDictByValue(orderCondition.getProductCategoryId().toString(), OrderUtils.LIMIT_REMOTECHARGE_CATEGORY_DICT);
		if(limitRemoteDict != null){
			model.addAttribute("limitRemoteDict",limitRemoteDict);
		}
		order.setRemarks("");
		order.setCrushPlanFlag(crushPlanFlag);//是否是突击单派单
		model.addAttribute("canSave", true);
		model.addAttribute("freeDistance", freeDistance);//远程费起收点
        model.addAttribute("customerRemoteFee",customerRemoteFee);
        model.addAttribute("areaRemoteFee",areaRemoteFee);
		model.addAttribute("price", price);//远程费单价
		model.addAttribute("order", order);
		return "modules/sd/service/orderPlanForm";
	}

	/**
	 * ajax提交派单信息
	 *
	 * @param order
	 */
	@RequiresPermissions("sd:order:plan")
	@ResponseBody
	@RequestMapping(value = "plan", method = RequestMethod.POST)
	//@FormToken(remove = true)
	public AjaxJsonEntity plan(Order order, HttpServletResponse response)
	{
		AjaxJsonEntity result = new AjaxJsonEntity(true);
		Date date = new Date();
		User user = UserUtils.getUser();
		if (order == null || order.getId() == null)
		{
			result.setSuccess(false);
			result.setMessage("派单时发生错误：订单号丢失");
			return result;
		}
		OrderCondition condition = order.getOrderCondition();
		if (condition==null || condition.getServicePoint()==null || condition.getServicePoint().getId() == null)
		{
			result.setSuccess(false);
			result.setMessage("未指派安维网点");
			return result;
		}
		OrderFee fee = order.getOrderFee();
		if (fee == null || fee.getEngineerPaymentType()==null || StringUtils.isBlank(fee.getEngineerPaymentType().getValue()))
		{
			result.setSuccess(false);
			result.setMessage("未设定安维网点结算方式");
			return result;
		}

		ServicePoint servicePoint = servicePointService.getFromCache(condition.getServicePoint().getId());
		if (servicePoint.getPrimary() == null || servicePoint.getPrimary().getId() == null) {
			result.setSuccess(false);
			result.setMessage(String.format("网点：%s 未设定主帐号", servicePoint.getName()));
			return result;
		}
		Dict engineerPaymentType = servicePoint.getFinance().getPaymentType();
		if (engineerPaymentType == null || StringUtils.isBlank(engineerPaymentType.getValue()) || engineerPaymentType.getIntValue() <= 0) {
			result.setSuccess(false);
			result.setMessage(String.format("网点：%s 付款方式未设定", servicePoint.getName()));
			return result;
		}
		Engineer engineer = servicePointService.getEngineerFromCache(condition.getServicePoint().getId(), servicePoint.getPrimary().getId());
		if (engineer == null) {
			result.setSuccess(false);
			result.setMessage(String.format("读取网点：%s 主帐号信息错误,师傅主账号被停用或者无主账号", servicePoint.getName()));
			return result;
		}
		PlanActionEntity planActionEntity = new PlanActionEntity();
		try {
			Order o = orderService.getOrderById(order.getId(), order.getQuarter(), OrderUtils.OrderDataLevel.DETAIL, true);
			if (null == o || null == o.getOrderCondition()) {
				result.setSuccess(false);
				result.setMessage("读取订单失败，请重试。");
				return result;
			}
			if (!o.canPlanOrder()) {
				result.setSuccess(false);
				result.setMessage("该订单不能派单，请刷新页面查看订单是否已取消。");
				return result;
			}
			//2021/6/2 网点容量判断
			ServicePoint oldServicePoint = o.getOrderCondition().getServicePoint();
			if(oldServicePoint==null || !oldServicePoint.getId().equals(servicePoint.getId())){
				if(servicePoint.getUnfinishedOrderCount()>=servicePoint.getCapacity()){
					result.setSuccess(false);
					result.setMessage("该网点的未完工数量已经大于或等于网点的工单容量，请选择其他网点派单");
					return result;
				}
			}
			//2021/05/18 偏远区域判断是否维护网点服务价格
			RestResult<Object> remoteCheckResult = orderService.checkServicePointRemoteAreaAndPrice(servicePoint.getId(), o.getOrderCondition(), o.getItems());
			if(remoteCheckResult.getCode() != ErrorCode.NO_ERROR.code){
				return AjaxJsonEntity.fail(remoteCheckResult.getMsg(),null);
			}
			//远程费+其他费用总费用受控品类
			//合计费用超过设定金额，不允许派单
			//费用不超过设定金额，应收为0
			Dict limitRemoteDict = MSDictUtils.getDictByValue(o.getOrderCondition().getProductCategoryId().toString(), OrderUtils.LIMIT_REMOTECHARGE_CATEGORY_DICT);
			if(limitRemoteDict != null){
				Double totalLimitCharge = fee.getPlanOtherCharge() + fee.getPlanTravelCharge();
				Double limitSetCharge = Double.valueOf(limitRemoteDict.getSort());
				if(totalLimitCharge > limitSetCharge){
					result.setSuccess(false);
					result.setMessage(MessageFormat.format("远程费用和其他费用合计不能超过{0,number,#.##}元，无法派单!请确认是否操作退单!", limitSetCharge));
					return result;
				}
				//应收清零
				fee.setCustomerPlanTravelCharge(0.00);
				fee.setCustomerPlanOtherCharge(0.00);
			}
			condition.setServicePoint(servicePoint);
			servicePoint.setPrimary(engineer);
			fee.setEngineerPaymentType(engineerPaymentType);//防止篡改，重取
			order.setOrderNo(o.getOrderNo());
			order.setItems(o.getItems());
			//保险单
			long productCategtoryId = Optional.ofNullable(o.getOrderCondition()).map(t->t.getProductCategoryId()).orElse(0L);
			MSResponse<Object> msResponse = generateInsuranceRecord(order,productCategtoryId,user,date);
			if(!MSResponse.isSuccessCode(msResponse)){
				result.setSuccess(false);
				result.setMessage(msResponse.getMsg());
				return result;
			}
			planActionEntity.setOrderInsurance(Optional.ofNullable(msResponse.getData()).map(t->(OrderInsurance)t).orElse(null));
			//派单记录
			msResponse = generateOrderPlanRecord(order,user,date);
			if(!MSResponse.isSuccessCode(msResponse)){
				result.setSuccess(false);
				result.setMessage(msResponse.getMsg());
				return result;
			}
			planActionEntity.setOrderPlan(Optional.ofNullable(msResponse.getData()).map(t->(OrderPlan)t).orElse(null));

			msResponse = generateServicePointFeeRecord(order,planActionEntity.getOrderInsurance(),user,date);
			if(!MSResponse.isSuccessCode(msResponse)){
				result.setSuccess(false);
				result.setMessage(msResponse.getMsg());
				return result;
			}
			planActionEntity.setServicePointFee(Optional.ofNullable(msResponse.getData()).map(t->(OrderServicePointFee)t).orElse(null));

			OrderCondition oc = o.getOrderCondition();
			condition.setKefu(oc.getKefu());
			condition.setUserName(oc.getUserName());
			condition.setPhone1(oc.getPhone1());
			condition.setServicePhone(oc.getServicePhone());
			condition.setServiceAddress(oc.getServiceAddress());
			condition.setArea(oc.getArea());
			condition.setSubArea(oc.getSubArea());
			condition.setCustomer(oc.getCustomer());
			StringBuffer engineerMsg = new StringBuffer(200);
			StringBuffer userMsg = new StringBuffer(200);
			generatePlanMessage(
					smIgnoreDataSources,
					order,
					user,
					date,
					planActionEntity
			);

			//组装
			planActionEntity.setOrder(order);
			planActionEntity.setO(o);
			planActionEntity.setUser(user);
			planActionEntity.setDate(date);

			orderService.planOrder(planActionEntity);
		} catch (OrderException oe){
            result.setSuccess(false);
            result.setMessage(oe.getMessage());
        } catch (Exception e){
			result.setSuccess(false);
			result.setMessage(e.getMessage());
			log.error("[OrderController.plan] orderId:{}",order.getId(),e);

		}
		return result;
	}

	//region 装配方法
	/**
	 * 生成保险记录
	 *
	 * @autho Ryan
	 * @date 2021/04/20
	 * @desc 订单单一品类，品类信息读取orderConditon，无需变量产品项;并记录日志
	 *
	 */
	private MSResponse generateInsuranceRecord(Order order,long productCategtoryId,User user,Date date){
		OrderCondition condition = order.getOrderCondition();
		if(productCategtoryId ==0){
			log.error("订单中无品类信息,order no:{}",condition.getOrderNo());
			return new MSResponse<>(new MSErrorCode(MSErrorCode.FAILURE.getCode(),"订单中无品类信息"));
		}
		ServicePoint servicePoint = condition.getServicePoint();
		OrderInsurance orderInsurance = orderService.getOrderInsuranceByServicePoint(order.getId(),order.getQuarter(),servicePoint.getId());
		if(orderInsurance != null){
			log.error("订单[{}]已有网点[{}]的保险单",order.getOrderNo(),servicePoint.getId());
			return new MSResponse<>(MSErrorCode.SUCCESS,null);
		}
		Integer insuranceFlag = Optional.ofNullable(servicePoint.getInsuranceFlag()).orElse(0);
		if(insuranceFlag == 0){
			log.error("订单[{}] 网点[{}]的保险开关未开启",order.getOrderNo(),servicePoint.getId());
			return new MSResponse<>(MSErrorCode.SUCCESS,null);
		}
		boolean insuranced = ServicePointUtils.servicePointInsuranceEnabled(servicePoint);
		if(!insuranced){
			log.error("系统级配置：web.insuranceEnabled 保险开关 未开启");
			return new MSResponse<>(MSErrorCode.SUCCESS,null);
		}
		//List<Long> categorids = order.getItems().stream().filter(t -> t.getDelFlag() == 0).map(t -> t.getProduct().getCategory().getId()).distinct().collect(Collectors.toList());
		Double insuranceAmount = orderService.getOrderInsuranceAmount(Lists.newArrayList(productCategtoryId));
		if (insuranceAmount == null) {
			log.error("订单[{}] 网点[{}] 品类[{}]的未设定保险费",order.getOrderNo(),servicePoint.getId(),productCategtoryId);
			return new MSResponse<>(MSErrorCode.SUCCESS,null);
		}

		//保险费大于0，才生成保单
		if (insuranceAmount <= 0) {
			log.error("订单[{}] 网点[{}] 品类[{}]的保险费为:0",order.getOrderNo(),servicePoint.getId(),productCategtoryId);
			return new MSResponse<>(MSErrorCode.SUCCESS,null);
		}
		//保险单号
		String insuranceNo = SeqUtils.NextSequenceNo("orderInsuranceNo");
		if (StringUtils.isBlank(insuranceNo)) {
			insuranceNo = SeqUtils.NextSequenceNo("orderInsuranceNo");
			if (StringUtils.isBlank(insuranceNo)) {
				return new MSResponse<>(new MSErrorCode(MSErrorCode.FAILURE.getCode(),"生成工单保险单号错误"));
			}
		}
		//保险单
		Engineer engineer = condition.getServicePoint().getPrimary();
		orderInsurance = new OrderInsurance();
		orderInsurance.setAmount(insuranceAmount);
		orderInsurance.setInsuranceNo(insuranceNo);
		orderInsurance.setOrderId(order.getId());
		orderInsurance.setOrderNo(order.getOrderNo());
		orderInsurance.setQuarter(order.getQuarter());
		orderInsurance.setServicePointId(condition.getServicePoint().getId());
		orderInsurance.setAssured(engineer.getName());//主帐号
		orderInsurance.setPhone(engineer.getContactInfo());
		orderInsurance.setAddress(engineer.getAddress());
		orderInsurance.setInsureDate(date);
		orderInsurance.setInsuranceDuration(12);//投保期限12个月
		orderInsurance.setCreateBy(user);
		orderInsurance.setCreateDate(date);
		orderInsurance.setDelFlag(LongIDDataEntity.DEL_FLAG_DELETE);
		return new MSResponse<>(orderInsurance);
	}

	/**
	 * 派单记录
	 * 无派单记录，产生新记录
	 * 有：更新派单次序
	 */
	private MSResponse<Object> generateOrderPlanRecord(Order order,User user,Date date){
		ServicePoint servicePoint = order.getOrderCondition().getServicePoint();
		Engineer engineer = servicePoint.getPrimary();
		Integer nextPlanTimes = orderService.getOrderPlanMaxTimes(order.getId(), order.getQuarter());
		if (nextPlanTimes == null) {
			nextPlanTimes = 1;
		} else {
			//同网点派单不加1
			nextPlanTimes++;//+1
		}
		OrderPlan orderPlan = orderService.getOrderPlan(order.getId(), order.getQuarter(), servicePoint.getId(), engineer.getId());
		if (orderPlan != null && orderPlan.getId() > 0) {
			orderPlan.setPlanTimes(nextPlanTimes);
			orderPlan.setEstimatedOtherCost(order.getOrderFee().getPlanOtherCharge());//其它费用
			orderPlan.setEstimatedDistance(order.getOrderFee().getPlanDistance());//距离
			orderPlan.setEstimatedTravelCost(order.getOrderFee().getPlanTravelCharge());//远程费
			Double amount = orderService.calcServicePointCost(servicePoint, order.getItems());//服务费
			orderPlan.setEstimatedServiceCost(amount);
			orderPlan.setUpdateBy(user);
			orderPlan.setUpdateDate(date);
			return new MSResponse<>(orderPlan);
		}
		//无派单记录
		try {
			orderPlan = new OrderPlan();
			orderPlan.setQuarter(order.getQuarter());
			orderPlan.setOrderId(order.getId());
			orderPlan.setServicePoint(servicePoint);
			orderPlan.setEngineer(engineer);
			orderPlan.setIsMaster(1);//*
			orderPlan.setPlanTimes(nextPlanTimes);//*
			orderPlan.setCreateBy(user);
			orderPlan.setCreateDate(date);
			orderPlan.setUpdateBy(new User(0l));
			orderPlan.setEstimatedOtherCost(order.getOrderFee().getPlanOtherCharge());//其它费用
			orderPlan.setEstimatedDistance(order.getOrderFee().getPlanDistance());//距离
			orderPlan.setEstimatedTravelCost(order.getOrderFee().getPlanTravelCharge());//远程费
			Double amount = orderService.calcServicePointCost(servicePoint, order.getItems());//服务费
			orderPlan.setEstimatedServiceCost(amount);
			return new MSResponse<>(orderPlan);
		}catch (Exception e){
			StringBuffer msg = new StringBuffer(200);
			log.error("生成派单记录错误",e);
			msg.append("生成派单记录错误:").append(Exceptions.getRootCauseMessage(e));
			MSResponse msResponse = new MSResponse<>(new MSErrorCode(MSErrorCode.FAILURE.getCode(),StringUtils.left(msg.toString(),150)));
			msg.setLength(0);
			msg = null;
			return msResponse;
		}
	}

	/**
	 * 生成网点订单记录
	 */
	private MSResponse<Object> generateServicePointFeeRecord(Order order,OrderInsurance insurance, User user,Date date){
		ServicePoint servicePoint = order.getOrderCondition().getServicePoint();
		OrderServicePointFee servicePointFee = orderServicePointFeeService.getByPrimaryKeys(order.getId(), order.getQuarter(),servicePoint.getId());
		if(servicePointFee != null){
			return new MSResponse<>(MSErrorCode.SUCCESS);
		}
		servicePointFee = new OrderServicePointFee();
		servicePointFee.setServicePoint(servicePoint);
		servicePointFee.setOrderId(order.getId());
		servicePointFee.setQuarter(order.getQuarter());
		if(insurance != null){
			//保险费
			servicePointFee.setInsuranceCharge(0.00-insurance.getAmount());
			servicePointFee.setOrderCharge(0-insurance.getAmount());
			servicePointFee.setInsuranceNo(insurance.getInsuranceNo());
		}
		return new MSResponse<>(servicePointFee);
	}

	/**
	 * 生成派单是短信信息，包含用户和网点两种短信内容
	 * @param smIgnoreDataSources	忽略的数据源
	 * @param order	订单信息
	 * @param user	操作人
	 * @param date	操作日志
	 * @param planActionEntity 派单实例
	 */
	private void generatePlanMessage(String smIgnoreDataSources, Order order, User user, Date date,PlanActionEntity planActionEntity){
		User engieerAccount = null;
		OrderCondition orderCondition = order.getOrderCondition();
		ServicePoint servicePoint = orderCondition.getServicePoint();
		Engineer engineer = orderCondition.getServicePoint().getPrimary();
		//region 发送用户短信
		//未在配置中：shortmessage.ignore-data-sources  //2018-12-05
		// 派单发给用户的短信调整：师傅使用app和不使用app的短信格式统一
		List<String> ignoreDataSources = StringUtils.isBlank(smIgnoreDataSources) ? Lists.newArrayList() : Splitter.on(",").trimResults().splitToList(smIgnoreDataSources);
		if (!ignoreDataSources.contains(order.getDataSource().getValue()) && order.getSendUserMessageFlag() != null && order.getSendUserMessageFlag() == 1) {
			// 派单后给用户发送短信
			// 检查客户短信发送开关，1:才发送
			Customer customer = null;
			try {
				customer = customerService.getFromCache(order.getOrderCondition().getCustomer().getId());
			} catch (Exception e) {
				LogUtils.saveLog("客服派单:检查客户短信开关异常", "geneartePlanMessage", order.getId().toString(), e, user);
			}
			//发送短信 1.未取到客户信息 2.取到，且短信发送标记为：1 2018/04/12
			String mobile = StringUtils.EMPTY;
			if (customer == null || (customer != null && customer.getShortMessageFlag() == 1)) {
				try {
					//用户电话号码
					mobile = StringUtils.isBlank(orderCondition.getServicePhone()) ? orderCondition.getPhone1() : orderCondition.getServicePhone();
					if (StringUtils.isBlank(mobile)) {
						mobile = orderCondition.getPhone2();
					}
					//有电话号码，才发送短信
					if (StringUtils.isNotBlank(mobile) && servicePoint.getPlanContactFlag() == 0) {
						// 网点联系人 为网点负责人(0)时此处发送短信;师傅(1)在网点派单或App派单时再发短信 2020-11-19
						StringBuffer userMsg = new StringBuffer(250);
						userMsg.append("您的");
						OrderItem item;
						for (int i = 0, size = order.getItems().size(); i < size; i++) {
							item = order.getItems().get(i);
							userMsg
									.append(item.getBrand())
									.append(com.wolfking.jeesite.common.utils.StringUtils.getStandardProductName(item.getProduct().getName()))
									.append(item.getQty())
									.append(item.getProduct().getSetFlag() == 0 ? "台" : "套")
									.append(item.getServiceType().getName())
									.append((i == (size - 1)) ? "" : " ");
						}
						userMsg.append("，");
						userMsg.append(engineer.getName().substring(0, 1));
						userMsg.append("师傅").append(engineer.getContactInfo()).append("已接单,");
						if (orderCondition.getKefu() != null) {
							userMsg
									.append("客服")
									.append(orderCondition.getKefu().getName().substring(0, 1)).append("小姐")
									.append(orderCondition.getKefu().getPhone())
									.append("/");
						}
						userMsg.append(MSDictUtils.getDictSingleValue("400ServicePhone", "4006663653"));
						planActionEntity.setUserMsg(userMsg.toString());
						planActionEntity.setUserPhone(mobile);
					}
				} catch (Exception e) {
					log.error(MessageFormat.format("[geneartePlanMessage]sms- mobile:{0},triggerBy:{1},triggerDate:{2}",mobile,user.getId(),date.getTime()),e);
				}
			}
		}
		//endregion

		//region 安维人员短息
		if (order.getSendEngineerMessageFlag() != null && order.getSendEngineerMessageFlag() == 1) {
			StringBuffer engineerMsg = new StringBuffer(250);
			try {
				boolean isSend = true;
				if (engieerAccount == null) {
					engieerAccount = systemService.getUserByEngineerId(engineer.getId());
				}
				if (engieerAccount != null && engieerAccount.getAppLoged() == 1) {
					// 人工派单给有APP的师傅提醒：张师傅，有新单派给您，请及时打开APP进行查看处理
					engineerMsg.append(engieerAccount.getName().substring(0, 1))
							.append("师傅，有新单派给您，请及时打开APP进行查看处理");
				} else {
					// 石师傅,现有: 赵辉13920663603 河东区 大桥道萦东花园8号楼4门602 九阳 燃气灶 1台
					// 需要安装，请2小时内联系用户确认安维环境并预约上门时间，务必48小时内上门，
					// 严禁对产品作任何评价，带齐相应的工具和配件，
					// 现场有问题请联系客服林小姐0757-26169180/400-666-3653
					// new 2019-05-21
					//赵辉13920663603 河东区 大桥道萦东花园8号楼4门602九阳燃气灶1台 安装，
					// 请2小时内联系用户确认环境并预约，48小时内上门，严禁对产品作任何评价，
					// 有问题联系客服林小姐0757-26169180/4006663653
					// 2019-07-18 地址去掉省市
					Area area = orderCondition.getArea();
					if(area == null){//未获得区县
						isSend = false;
					}else{
						Area cacheArea = areaService.getFromCache(area.getId(),4);
						if(cacheArea != null){
							area.setName(cacheArea.getName());
						}
					}
					Area subArea = orderCondition.getSubArea();
					if (subArea == null) {
						isSend = false;
					}else{
						if(subArea.getId() > 3) {
							Area cacheSubArea = areaService.getTownFromCache(area.getId(), subArea.getId());
							if (cacheSubArea != null) {
								subArea.setName(cacheSubArea.getName());
							}
						}else{
							subArea.setName("");
						}
					}
					if(isSend) {
						engineerMsg
								.append(orderCondition.getUserName())
								.append(orderCondition.getServicePhone())
								.append(" ")
								.append(area.getName())
								.append(StringUtils.isBlank(subArea.getName()) ? "" : " " + subArea.getName())
								.append(" ")
								.append(orderCondition.getServiceAddress());
						for (OrderItem item : order.getItems()) {
							engineerMsg.append(" ").append(item.getBrand())
									.append(com.wolfking.jeesite.common.utils.StringUtils.getStandardProductName(item.getProduct().getName()))
									.append(item.getQty()).append("台 ")
									.append(item.getServiceType().getName());
						}
						engineerMsg.append(",请2小时内联系用户确认环境并预约，48小时内上门，严禁对产品作任何评价，有问题请联系客服");
						if (orderCondition.getKefu() != null) {
							engineerMsg
									.append(StringUtils.left(orderCondition.getKefu().getName(),1)).append("小姐")
									.append(StringUtils.isBlank(orderCondition.getKefu().getPhone()) ? orderCondition.getKefu().getMobile() : orderCondition.getKefu().getPhone())
									.append("/");
						}
						engineerMsg.append(MSDictUtils.getDictSingleValue("400ServicePhone", "400-666-3653"));
					}
				}
				// 使用新的短信发送方法 2019/02/28
				if(!isSend) {
					engineerMsg.setLength(0);
				}else{
					planActionEntity.setEngineerMsg(engineerMsg.toString());
					planActionEntity.setEngineerPhone(engieerAccount.getMobile());
				}
			} catch (Exception e) {
				log.error(MessageFormat.format("[OrderService.planOrder]engineer sms- mobile:{0},content:{1},triggerBy:{2},triggerDate:{3}",
						engieerAccount.getMobile(),
						engineerMsg.toString(),
						user.getId(),
						date.getTime()
				),e);
			}
		}
		//endregion

		//region APP通知
		if (engieerAccount != null) {// && engieerAccount.getAppLoged() == 1
			// 张三师傅，在您附近有一张上门安装百得油烟机的工单，请尽快登陆APP接单~
			try {
				//将推送切换为微服务
				AppPushMessage appPushMessage = new AppPushMessage();
				appPushMessage.setPassThroughType(AppPushMessage.PassThroughType.NOTIFICATION);
				appPushMessage.setMessageType(AppMessageType.PLANORDER);
				appPushMessage.setSubject("");
				appPushMessage.setContent("");
				appPushMessage.setTimestamp(System.currentTimeMillis());
				appPushMessage.setUserId(engieerAccount.getId());
				appPushMessage.setDescription(engieerAccount.getName().substring(0, 1).concat("师傅,有新单派给您，请及时打开APP进行查看处理"));
				planActionEntity.setAppPushMessage(appPushMessage);
			} catch (Exception e) {
				log.error("[OrderService.planOrder]app notice - uid:".concat(engieerAccount.getId().toString())
								.concat(",msg:").concat(engineer.getName().substring(0, 1).concat("师傅,有新单派给您，请及时打开APP进行查看处理"))
						, e);
			}
		}
		//endregion
	}
	//endregion 装配方法

	/**
	 * 修改实际上门联系信息
	 * 2020-09-15: 增加省市区县街道的修改
	 * @param orderId
	 * @param request
	 * @param model
	 * @return
	 */
	@RequiresPermissions(value = { "sd:order:plan", "sd:order:engineerplan" }, logical = Logical.OR)
	@RequestMapping(value = "updateUserServiceInfo", method = RequestMethod.GET)
	public String updateUserServiceInfo(String orderId,String quarter, HttpServletRequest request, Model model)
	{
		Order order = new Order();
		Long lorderId = Long.valueOf(orderId);
		if (lorderId == null || lorderId <= 0) {
			addMessage(model, "修改联系信息失败：订单号丢失");
			model.addAttribute("canSave", false);
			model.addAttribute("order", order);
			return "modules/sd/service/updateServiceInfoForm";
		}

		order = orderService.getOrderById(lorderId, quarter,OrderUtils.OrderDataLevel.FEE, true);
		if(order == null || order.getOrderCondition() == null) {
			addMessage(model, "错误：系统繁忙，读取订单失败，请重试。");
			model.addAttribute("canSave", false);
			model.addAttribute("order", new Order());
		}else{
			order.setRemarks("");
			model.addAttribute("canSave", true);
			OrderCondition orderCondition = order.getOrderCondition();
			orderCondition.setAddress(MessageFormat.format("{0} {1}",orderCondition.getArea().getName(),orderCondition.getAddress()));
			model.addAttribute("order", order);
		}

		return "modules/sd/service/updateServiceInfoForm";
	}

	/**
	 * ajax提交 修改实际上门联系信息
	 * 2020-09-15: 增加省市区县街道的修改
	 * @param order
	 * @param response
	 * @return
	 */
	@RequiresPermissions("sd:order:plan")
	@ResponseBody
	@RequestMapping(value = "updateUserServiceInfo", method = RequestMethod.POST)
	public AjaxJsonEntity saveUpdateUserServiceInfo(Order order, HttpServletResponse response)
	{
		AjaxJsonEntity result = new AjaxJsonEntity(true);
		if (order == null || order.getId() == null)
		{
			result.setSuccess(false);
			result.setMessage("修改实际上门联系信息时发生错误：订单号丢失");
			return result;
		}
		OrderCondition orderCondition = order.getOrderCondition();
		// 如果区域为空的情况
		if (orderCondition.getArea() == null || orderCondition.getArea().getId() == null  || orderCondition.getArea().getId()<=0)
		{
			result.setSuccess(false);
			result.setMessage("请选择用户地址所在的省市区。");
			return result;
		}
		//检查区域type
		Area area  = areaService.getFromCache(orderCondition.getArea().getId());
		if(area == null){
			result.setSuccess(false);
			result.setMessage("找不到指定的区域,请重新选择。");
			return result;
		}
		if(area.getType() != 4){
			result.setSuccess(false);
			result.setMessage("区域请选择至区/县。");
			return result;
		}
		if(orderCondition.getSubArea() == null || orderCondition.getSubArea().getId() == null) {
			orderCondition.setSubArea(new Area(0L,""));
		}
		try
		{
			//根据areaId获取省和市
			Map<Integer,Area> areas = areaService.getAllParentsWithDistrict(orderCondition.getArea().getId());
			Area province = areas.getOrDefault(Area.TYPE_VALUE_PROVINCE,new Area(0L));
			Area city = areas.getOrDefault(Area.TYPE_VALUE_CITY,new Area(0L));
			orderCondition.setProvinceId(province.getId());
			orderCondition.setCityId(city.getId());
			orderCondition.setServiceAddress(StringUtils.filterAddress(orderCondition.getServiceAddress()));
			order.setDescription(StringUtils.left(StringUtils.filterAddress(order.getDescription()),250));
			orderService.updateServiceInfo(order);
		} catch (OrderException oe){
            result.setSuccess(false);
            result.setMessage(oe.getMessage());
        } catch (Exception e){
			result.setSuccess(false);
			result.setMessage("修改实际上门联系信息时发生异常:" + e.getMessage());
			log.error("[OrderController.saveUpdateUserServiceInfo] orderId:{}",order.getId(),e);
		}
		return result;
	}

	/**
	 * 修改到货日期
	 */
	@RequiresPermissions("sd:order:plan")
	@RequestMapping(value = "arrivaldate", method = RequestMethod.GET)
	public String arrivalDate(String orderId,String quarter, HttpServletRequest request, Model model)
	{
		String viewForm = "modules/sd/service/arrivalDateForm";
		Order order = new Order();
		Long lorderId = Long.valueOf(orderId);
		if (lorderId == null || lorderId <= 0) {
			addMessage(model, "修改到货日期失败：订单号丢失");
			model.addAttribute("canSave", false);
			model.addAttribute("order", order);
			return viewForm;
		}

		order = orderService.getOrderById(lorderId, quarter,OrderUtils.OrderDataLevel.FEE, true);
		if (order == null || order.getOrderCondition() == null) {
			addMessage(model, "错误：系统繁忙，读取订单失败，请重试");
			model.addAttribute("canSave", false);
			model.addAttribute("order", new Order());
			return viewForm;
		}
		order.setCreateDate(new Date());
		model.addAttribute("canSave", true);
		model.addAttribute("order", order);
		return viewForm;
	}

	/**
	 * ajax提交 修改到货日期
	 */
	@RequiresPermissions("sd:order:plan")
	@ResponseBody
	@RequestMapping(value = "arrivaldate", method = RequestMethod.POST)
	public AjaxJsonEntity arrivalDate(Order order, HttpServletResponse response)
	{
		AjaxJsonEntity result = new AjaxJsonEntity(true);
		User user = UserUtils.getUser();
		if(user == null || user.getId() == null){
			result.setSuccess(false);
			result.setMessage("登录超时，请重新登录。");
			return result;
		}
		if (order == null || order.getId() == null)
		{
			result.setSuccess(false);
			result.setMessage("修改到货日期时发生错误：订单号丢失");
			return result;
		}

		try
		{
			order.setCurrentUser(user);
			orderService.updateArrivalDate(order);
		} catch (OrderException oe){
            result.setSuccess(false);
            result.setMessage(oe.getMessage());
        } catch (Exception e){
			result.setSuccess(false);
			result.setMessage("修改到货日期时发生异常:" + e.getMessage());
			log.error("[OrderController.arrivalDate] orderId:{}",order.getId(),e);
		}
		return result;
	}

	/**
	 * 订单退回
	 * 退回到：派单区(已接单)
	 */
	@RequiresPermissions("sd:order:accept")
	@ResponseBody
	@RequestMapping(value = "orderBackToAccept", method = RequestMethod.POST)
	public AjaxJsonEntity orderBackToAccept(String orderId,String quarter, HttpServletResponse response)
	{
		Long lid = Long.valueOf(orderId);
		AjaxJsonEntity result = new AjaxJsonEntity(true);
		if(lid == null || lid <= 0){
			result.setSuccess(false);
			result.setMessage("订单参数错误");
			return result;
		}
		try
		{
			orderService.orderBackToAccept(lid,quarter);

		} catch (OrderException oe){
            result.setSuccess(false);
            result.setMessage(oe.getMessage());
        } catch (Exception e){
			result.setSuccess(false);
			result.setMessage(e.getMessage());
			log.error("[OrderController.orderBackToAccept] orderId:{}",orderId,e);
		}
		return result;
	}

    //endregion 操作

	// region 停滞原因

	/**
	 * 设定订单停滞原因 form
	 *
	 * @param orderId   订单id
	 * @param from  	调用方 0:订单列表 1:订单明细页
	 */
	@RequiresPermissions(value = { "sd:order:service", "sd:order:engineeraccept" }, logical = Logical.OR)
	@RequestMapping(value = "pending", method = RequestMethod.GET)
	public String pending(String orderId,String quarter,Long from, HttpServletRequest request, Model model)
	{
		Order order = new Order();
		OrderCondition condition = new OrderCondition();
		Long lorderId = Long.valueOf(orderId);
		if (lorderId == null || lorderId<=0) {
			addMessage(model, "错误：订单参数无效");
			model.addAttribute("canSave", false);
			model.addAttribute("order", condition);
			return "modules/sd/service/orderPendingTypeForm";
		}else{
			order = orderService.getOrderById(lorderId,quarter, OrderUtils.OrderDataLevel.CONDITION,true);
			if(order == null || order.getOrderCondition() == null){
				addMessage(model, "错误：系统繁忙，读取订单失败，请重试。");
				model.addAttribute("canSave", false);
				model.addAttribute("order", condition);
				return "modules/sd/service/orderPendingTypeForm";
			}
			if (!order.canPendingType())
			{
				addMessage(model, String.format("错误：此订单不允许设置停滞原因，当前订单状态:%s",order.getOrderCondition().getStatus().getLabel()));
				model.addAttribute("canSave", false);
				model.addAttribute("order", condition);
				return "modules/sd/service/orderPendingTypeForm";
			}
		}
		String lockkey = String.format(RedisConstant.SD_ORDER_LOCK,orderId);
		if(redisUtils.exists(RedisConstant.RedisDBType.REDIS_LOCK_DB,lockkey)){
			addMessage(model,"错误：此订单正在处理中，请稍候重试，或刷新页面。");
			model.addAttribute("canSave", false);
			model.addAttribute("order", condition);
			return "modules/sd/service/orderPendingTypeForm";
		}

		condition = order.getOrderCondition();
		condition.setOrderId(order.getId());
		condition.setRemarks("");
//		condition.setPendingType(new Dict("1","等通知"));
		//按排序取第一个
		List<Dict> pendingTypes = MSDictUtils.getDictExceptList("PendingType", "3");
		if(pendingTypes != null && pendingTypes.size()>0){
			condition.setPendingType(pendingTypes.get(0));
		}
		condition.setFeedbackId(from);//调用方

		// 时间取整点时间
		Date date = DateUtils.addDays(new Date(), 1);
		String time = DateUtils.formatDate(date,"yyyy-MM-dd 08:00:00");
		Date appointmentDate = null;
		try{
			appointmentDate = DateUtils.parse(time,"yyyy-MM-dd HH:00:00");
		} catch (java.text.ParseException e){
			log.error("[OrderController.pending] invalid datetime:{}",time,e);
		}
		// 时间取整点时间
		order.getOrderCondition().setAppointmentDate(appointmentDate);
		model.addAttribute("order", condition);
		return "modules/sd/service/orderPendingTypeForm";
	}

	/**
	 * ajax提交停滞原因
	 *
	 * @param order
	 */
	@RequiresPermissions(value = { "sd:order:service", "sd:order:engineeraccept" }, logical = Logical.OR)
	@ResponseBody
	@RequestMapping(value = "pending", method = RequestMethod.POST)
	public AjaxJsonEntity pending(OrderCondition order, HttpServletResponse response)
	{
		AjaxJsonEntity result = new AjaxJsonEntity(true);
		try
		{
			User user = UserUtils.getUser();
			order.setCreateBy(user);
			orderService.pendingOrder(order);
		} catch (OrderException oe){
            result.setSuccess(false);
            result.setMessage(oe.getMessage());
        } catch (Exception e){
			result.setSuccess(false);
			result.setMessage(e.getMessage());
			if(order != null && order.getOrderId() != null) {
				log.error("[OrderController.pending] orderId:{}",order.getOrderId(), e);
			}else{
				log.error("[OrderController.pending]", e);
			}
		}
		return result;
	}

	// endregion 停滞原因

    //region 预约日期

    /**
     * 设定预约日期 form
     *
     * @param orderId   订单id
     * @param from  	调用方 0:订单列表 1:订单明细页
     */
    @RequiresPermissions(value = { "sd:order:service", "sd:order:engineeraccept" }, logical = Logical.OR)
    @RequestMapping(value = "appoint", method = RequestMethod.GET)
    public String appoint(String orderId,String quarter,Long from, HttpServletRequest request, Model model)
    {
    	String viewForm = "modules/sd/service/orderAppointForm";
        Order order = new Order();
        OrderCondition condition = new OrderCondition();
        Long lorderId = Long.valueOf(orderId);
        if (lorderId == null || lorderId<=0) {
            addMessage(model, "错误：订单参数无效");
            model.addAttribute("canSave", false);
            model.addAttribute("order", condition);
            return viewForm;
        }else{
            order = orderService.getOrderById(lorderId,quarter, OrderUtils.OrderDataLevel.CONDITION,true);
			if (order == null || order.getOrderCondition() == null) {
				addMessage(model, "错误：系统繁忙，读取订单失败，请重试");
				model.addAttribute("canService","false");
				return viewForm;
			}
            if (!order.canAppoint())
            {
                addMessage(model, String.format("错误：此订单不允许预约上门时间，当前订单状态:%s",order.getOrderCondition().getStatus().getLabel()));
                model.addAttribute("canSave", false);
                model.addAttribute("order", condition);
                return viewForm;
            }
        }
        String lockkey = String.format(RedisConstant.SD_ORDER_LOCK,orderId);
        if(redisUtils.exists(RedisConstant.RedisDBType.REDIS_LOCK_DB,lockkey)){
            addMessage(model,"错误：此订单正在处理中，请稍候重试，或刷新页面。");
            model.addAttribute("canSave", false);
            model.addAttribute("order", condition);
            return viewForm;
        }

        condition = order.getOrderCondition();
        condition.setOrderId(order.getId());
        condition.setRemarks("");
        condition.setFeedbackId(from);//调用方

        // 时间取整点时间
        Date date = DateUtils.addDays(new Date(), 1);
        String time = DateUtils.formatDate(date,"yyyy-MM-dd 08:00:00");
        Date appointmentDate = null;
        try
        {
            appointmentDate = DateUtils.parse(time,"yyyy-MM-dd HH:00:00");
        } catch (java.text.ParseException e)
        {
			log.error("[OrderController.appoint] invalid datetime:{}",time, e);
        }
        // 时间取整点时间
        order.getOrderCondition().setAppointmentDate(appointmentDate);
        model.addAttribute("order", condition);
        return viewForm;
    }

    /**
     * ajax提交预约日期
     *
     * @param order
     */
    @RequiresPermissions(value = { "sd:order:service", "sd:order:engineeraccept" }, logical = Logical.OR)
    @ResponseBody
    @RequestMapping(value = "appoint", method = RequestMethod.POST)
    public AjaxJsonEntity appoint(OrderCondition order, HttpServletResponse response)
    {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try
        {
            User user = UserUtils.getUser();
            order.setCreateBy(user);
            order.setPendingType(new Dict("3","预约时间"));
            orderService.pendingOrder(order);
        } catch (OrderException oe){
            result.setSuccess(false);
            result.setMessage(oe.getMessage());
        } catch (Exception e){
            result.setSuccess(false);
            result.setMessage(e.getMessage());
			if(order != null && order.getOrderId() != null) {
				log.error("[OrderController.appoint] orderId:{}",order.getOrderId(), e);
			}else{
				log.error("[OrderController.appoint]", e);
			}
        }
        return result;
    }

    //endregion 预约日期

    //region 待跟进

    /**
     * 设定客服下次跟进日期 form
     *
     * @param orderId   订单id
     * @param from  	调用方 0:订单列表 1:订单明细页
     */
    @RequiresPermissions(value = { "sd:order:service", "sd:order:engineeraccept" }, logical = Logical.OR)
    @RequestMapping(value = "nextFollowUpTime", method = RequestMethod.GET)
    public String nextFollowUpTime(String orderId,String quarter,Long from, HttpServletRequest request, Model model)
    {
        String viewForm = "modules/sd/service/orderNextFollowUpTimeForm";
        Order order = new Order();
        OrderCondition condition = new OrderCondition();
//        Long lorderId = Long.valueOf(orderId); //日志中出现过java.lang.NumberFormatException
        Long lorderId = StringUtils.toLong(orderId);
        if (lorderId == null || lorderId<=0) {
            addMessage(model, "错误：订单参数无效");
            model.addAttribute("canSave", false);
            model.addAttribute("order", condition);
            return viewForm;
        }else{
            order = orderService.getOrderById(lorderId,quarter, OrderUtils.OrderDataLevel.CONDITION,true);
            if (order == null || order.getOrderCondition() == null) {
                addMessage(model, "错误：系统繁忙，读取订单失败，请重试");
                model.addAttribute("canService","false");
                return viewForm;
            }
            if (!order.canPendingType())
            {
                addMessage(model, String.format("错误：此订单不允许待跟进，当前订单状态:%s",order.getOrderCondition().getStatus().getLabel()));
                model.addAttribute("canSave", false);
                model.addAttribute("order", condition);
                return viewForm;
            }
        }
        String lockkey = String.format(RedisConstant.SD_ORDER_LOCK,orderId);
        if(redisUtils.exists(RedisConstant.RedisDBType.REDIS_LOCK_DB,lockkey)){
            addMessage(model,"错误：此订单正在处理中，请稍候重试，或刷新页面。");
            model.addAttribute("canSave", false);
            model.addAttribute("order", condition);
            return viewForm;
        }

        condition = order.getOrderCondition();
        condition.setOrderId(order.getId());
        condition.setRemarks("");
        condition.setFeedbackId(from);//调用方

        // 时间取整点时间
        Date date = DateUtils.addDays(new Date(), 1);
        String time = DateUtils.formatDate(date,"yyyy-MM-dd 08:00:00");
        Date appointmentDate = null;
        try
        {
            appointmentDate = DateUtils.parse(time,"yyyy-MM-dd HH:00:00");
        } catch (java.text.ParseException e){
            log.error("[OrderController.nextFollowUpTime] invalid datetime:{}",time, e);
        }
        // 时间取整点时间
        order.getOrderCondition().setAppointmentDate(appointmentDate);
        model.addAttribute("order", condition);
        return viewForm;
    }

    /**
     * ajax提交客服下次跟进日期
     *
     * @param order
     */
    @RequiresPermissions(value = { "sd:order:service", "sd:order:engineeraccept" }, logical = Logical.OR)
    @ResponseBody
    @RequestMapping(value = "nextFollowUpTime", method = RequestMethod.POST)
    public AjaxJsonEntity nextFollowUpTime(OrderCondition order, HttpServletResponse response)
    {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try
        {
            User user = UserUtils.getUser();
            order.setCreateBy(user);
            order.setPendingType(new Dict("7","待跟进"));
            orderService.pendingOrder(order);
        } catch (OrderException oe){
            result.setSuccess(false);
            result.setMessage(oe.getMessage());
        } catch (Exception e){
            result.setSuccess(false);
            result.setMessage(e.getMessage());
            if(order != null && order.getOrderId() != null) {
                log.error("[OrderController.nextFollowUpTime] orderId:{}",order.getOrderId(), e);
            }else{
                log.error("OrderController.nextFollowUpTime]", e);
            }
        }
        return result;
    }

    //endregion 待跟进

	//region 上门服务

	/**
	 * 上门服务窗口
	 */
	@RequiresPermissions(value = { "sd:order:service" })
	@RequestMapping(value = "service", method = RequestMethod.GET)
	public String service(@RequestParam String orderId, Model model)
	{
		User user = UserUtils.getUser();
		Order order = new Order();
		Long lorderId = Long.valueOf(orderId);
		if (lorderId==null || lorderId<=0)
		{
			addMessage(model, "上门服务失败：订单参数为空。");
		} else
		{
			order = orderService.getOrderById(lorderId,"", OrderUtils.OrderDataLevel.DETAIL,true);
			if(order == null || order.getOrderCondition() == null){
				addMessage(model, "错误：系统繁忙，读取订单失败，请重试。");
				order = new Order();
			}
			if (!order.canService())
			{
				addMessage(model, "该订单不能上门服务。请刷新订单列查看订单处理状态。");
				order = new Order();
			}
		}

		model.addAttribute("order", order);
		return "modules/sd/service/orderServiceForm";
	}

	/**
	 * 添加服务明细窗口
	 * @param orderId 订单id
	 */
	@RequiresPermissions(value = { "sd:order:service" })
	@RequestMapping(value = "addservice")
	public String addservice(@RequestParam String orderId,@RequestParam(required = false) Integer addType, Model model) {
		User user = UserUtils.getUser();
		Long lorderId = Long.valueOf(orderId);
		Long customerId = 0L;
		int dataSource = 0;
		model.addAttribute("customerId",customerId);
		model.addAttribute("dataSource",dataSource);
		if (lorderId == null || lorderId <= 0) {
			addMessage(model, "参数错误");
			return "modules/sd/service/orderServiceItemForm";
		}
		String lockkey = String.format(RedisConstant.SD_ORDER_LOCK,orderId);
		if(redisUtils.exists(RedisConstant.RedisDBType.REDIS_LOCK_DB,lockkey)){
			addMessage(model, "错误：此订单正在处理中，请稍候重试，或刷新页面。");
			model.addAttribute("canService","false");
			return "modules/sd/service/orderServiceItemForm";
		}

		Order order = orderService.getOrderById(lorderId,"", OrderUtils.OrderDataLevel.DETAIL, true);
		if (order == null || order.getOrderCondition() == null) {
			addMessage(model, "错误：系统繁忙，读取订单失败，请重试");
			model.addAttribute("canService","false");
			return "modules/sd/service/orderServiceItemForm";
		}
		if (!order.canService()) {
			addMessage(model, "错误：此订单不能添加上门服务具体服务项目，请刷新订单列查看订单处理状态。");
			model.addAttribute("canService","false");
			return "modules/sd/service/orderServiceItemForm";
		}
		//预约检测
		if (order.getOrderCondition().getAppointmentDate() == null) {
			addMessage(model, "错误：没有设置预约时间，不允许直接确认上门。");
			model.addAttribute("canService","false");
			return "modules/sd/service/orderServiceItemForm";
		}
		Date date = new Date();
		if (order.getOrderCondition().getAppointmentDate().getTime() > DateUtils.getEndOfDay(date).getTime()) {
			addMessage(model, "错误：预约时间与当前不一致，请重新预约！");
			model.addAttribute("canService","false");
			return "modules/sd/service/orderServiceItemForm";
		}
		//2020-10-21 从主库读取派单时预设的费用和单号
		OrderFee feeMaster = orderService.getPresetFeeWhenPlanFromMasterDB(order.getId(),order.getQuarter());
		if(feeMaster != null){
			OrderFee orderFee = order.getOrderFee();
			if(orderFee != null){
				orderFee.setPlanTravelCharge(feeMaster.getPlanTravelCharge());
				orderFee.setPlanTravelNo(feeMaster.getPlanTravelNo());
				orderFee.setPlanDistance(feeMaster.getPlanDistance());
				orderFee.setCustomerPlanTravelCharge(feeMaster.getCustomerPlanTravelCharge());
				orderFee.setPlanOtherCharge(feeMaster.getPlanOtherCharge());
				orderFee.setCustomerPlanOtherCharge(feeMaster.getCustomerPlanOtherCharge());
			}
		}
        //厂商设定远程费
		OrderCondition condition = order.getOrderCondition();
		Integer customerRemoteFee = 1;
		customerId = Optional.ofNullable(condition.getCustomer()).map(t->t.getId()).orElse(0L);
		dataSource = order.getDataSourceId();
		AjaxJsonEntity locationCheckResult = orderService.checkAddressLocation(dataSource,condition.getOrderId(),condition.getQuarter());
		if(!locationCheckResult.getSuccess()){
			addMessage(model, "错误：因" + locationCheckResult.getMessage() + "，不能上门服务");
			model.addAttribute("canService","false");
			return "modules/sd/service/orderServiceItemForm";
		}

		// 检查是否为受控品类：自动同步应收远程费和其他费用
		Dict dict = MSDictUtils.getDictByValue(condition.getProductCategoryId().toString(), OrderUtils.SYNC_CUSTOMER_CHARGE_DICT);
		if (dict != null && dict.getValue().equals(condition.getProductCategoryId().toString())) {
			customerRemoteFee = 0;
		} else if (customerId > 0) {
			Customer customer = customerService.getFromCache(customerId);
			if (customer != null) {
				customerRemoteFee = customer.getRemoteFeeFlag();
			}
		}
        //区域远程费
		Integer areaRemoteFee = msRegionPermissionService.getRemoteFeeStatusFromCacheForSD(condition.getProductCategoryId(), condition.getArea().getId(), condition.getSubArea().getId());
		OrderDetail detail = new OrderDetail();
		detail.setQuarter(order.getQuarter());
		detail.setOrderId(lorderId);
        if (addType != null) {
            detail.setAddType(addType);
        }
        //订单类型 2019-12-02
        detail.setServiceCategory(new Dict(condition.getOrderServiceType(),""));
		OrderStatus orderStatus = order.getOrderStatus();
		detail.setServicePoint(condition.getServicePoint());
		Engineer engineer = new Engineer(condition.getEngineer().getId());
		engineer.setName(condition.getEngineer().getName());
		detail.setEngineer(engineer);

		Integer times = condition.getServiceTimes();
		times = times + 1;
		detail.setServiceTimes(times);
		detail.setOrderServiceTimes(times);
		detail.setRemarks(orderStatus.getPlanComment());// 2015-01-16
		// 显示派单时的备注
		OrderItem orderItem = order.getItems().get(0);
		detail.setProduct(orderItem.getProduct());
		detail.setBrand(orderItem.getBrand());
		detail.setProductSpec(orderItem.getProductSpec());
		detail.setQty(orderItem.getQty());
		detail.setServiceType(orderItem.getServiceType());
		detail.setTravelNo(order.getOrderFee().getPlanTravelNo());// 派单时预设的远程单号
		//service Type
		ServiceType serviceType = serviceTypeService.getFromCache(orderItem.getServiceType().getId());
		if(serviceType!=null){
			detail.setServiceType(serviceType);
			if(serviceType.getOrderServiceType()>0){
				Dict serviceCategory = new Dict(serviceType.getOrderServiceType(),"");
				detail.setServiceCategory(serviceCategory);
			}
		}
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
			double limitRemoteCharge = 0.0;
			if(limitRemoteDict.getSort()>0){
				limitRemoteCharge = Double.valueOf(limitRemoteDict.getSort());
			}
			model.addAttribute("limitRemoteCharge",limitRemoteCharge);
		}
        model.addAttribute("serviceCategories",serviceCategories);
		model.addAttribute("canService","true");
		model.addAttribute("item", detail);
		model.addAttribute("order", order);
        model.addAttribute("customerRemoteFee",customerRemoteFee);
        model.addAttribute("areaRemoteFee",areaRemoteFee);
        model.addAttribute("customerId",customerId);
		model.addAttribute("dataSource",dataSource);
		return "modules/sd/service/orderServiceItemForm";
	}


	/**
	 * 删除订单实际服务项目
	 *
	 * @param id 服务项目ID
	 */
	@RequiresPermissions("sd:order:service")
	@RequestMapping(value = "delservice")
	public String delservice(@RequestParam(required = false) String id, @RequestParam(required = false) String orderId, RedirectAttributes redirectAttributes)
	{
		Long lid = Long.valueOf(id);
		if (lid == null || lid <=0)
		{
			addMessage(redirectAttributes, "服务项目编号错误，无法删除");
		} else
		{
			String lockkey = String.format(RedisConstant.SD_ORDER_LOCK,orderId);
			if(redisUtils.exists(RedisConstant.RedisDBType.REDIS_LOCK_DB,lockkey)){
				addMessage(redirectAttributes, "错误：此订单正在处理中，请稍候重试，或刷新页面。");
			}else {
				try {
					Long lorderId = Long.valueOf(orderId);
					if (lorderId == null || lorderId <= 0) {
						addMessage(redirectAttributes, "订单id错误，无法删除");
					} else {
						User user = UserUtils.getUser();
						Date date = new Date();
						OrderDetail detail = new OrderDetail();
						detail.setOrderId(lorderId);
						detail.setId(lid);
						detail.setCreateBy(user);
						detail.setCreateDate(date);
						orderService.deleteDetail(detail);
						addMessage(redirectAttributes, "删除服务项目成功");
					}
				} catch (Exception e) {
					addMessage(redirectAttributes, "错误：" + e.getMessage());
				}
			}
		}
		return "redirect:" + Global.getAdminPath() + "/sd/order/service?orderId=" + orderId;
	}

	/**
	 * 删除订单实际服务项目
	 *
	 * @param id 服务项目ID
	 */
	@RequiresPermissions("sd:order:service")
	@ResponseBody
	@RequestMapping(value = "ajaxdelservice")
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
			return jsonEntity;
		}

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

		return jsonEntity;
	}

	/**
	 * ajax 保存服务明细
	 *
	 * @date 2019/12/03
	 * @author Ryan Lu
	 * 1.非安装服务类型，新增：服务类型，故障分类，故障现象，故障处理(id和name),其他故障维修说明
 	 */
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
			orderService.addDetail(detail);
		} catch (Exception e)
		{
		    //LogUtils.saveLog("上门服务","saveService",GsonUtils.getInstance().toGson(detail),e,user);
			jsonEntity.setSuccess(false);
			jsonEntity.setMessage(Exceptions.getRootCauseMessage(e));
			jsonEntity.setData(ExceptionUtils.getRootCauseStackTrace(e));
		}

		return jsonEntity;
	}

	/**
	 * 确认上门(ajax)
	 *
	 * @param orderId
	 * @param confirmType 确认类型: 0-客服 1-网点
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "confirmDoorAuto", method = RequestMethod.POST)
	public AjaxJsonEntity confirmDoorAuto(String orderId,String quarter, Integer confirmType, HttpServletResponse response)
	{
		User user = UserUtils.getUser();
		AjaxJsonEntity result = new AjaxJsonEntity(true);
		try
		{
			if(confirmType == null){
				confirmType = 0;
			}
			Long lorderId = Long.valueOf(orderId);
			orderService.confirmDoorAuto(lorderId,quarter,user,confirmType);
		} catch (OrderException oe){
            result.setSuccess(false);
            result.setMessage(oe.getMessage());
        } catch (Exception e){
			result.setSuccess(false);
			//result.setMessage(e.getMessage());
			result.setMessage(ExceptionUtils.getRootCauseMessage(e));
			result.setData(ExceptionUtils.getRootCauseStackTrace(e));
			log.error("[OrderController.confirmDoorAuto] orderId:{}",orderId, e);
		}
		return result;
	}

	/**
	 * 取消APP异常(ajax)
	 *
	 * @param orderId
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "orderDealAPPException", method = RequestMethod.POST)
	public AjaxJsonEntity orderDealAPPException(String orderId,String quarter, HttpServletResponse response)
	{
		AjaxJsonEntity result = new AjaxJsonEntity(true);
		try
		{
			Long lorderId = Long.valueOf(orderId);
			orderService.dealAPPException(lorderId,quarter);
		} catch (OrderException oe){
            result.setSuccess(false);
            result.setMessage(oe.getMessage());
        } catch (Exception e){
			result.setSuccess(false);
			result.setMessage("订单异常处理发生异常:" + e.getMessage());
			log.error("[OrderController.orderDealAPPException] orderId:{}",orderId, e);
		}
		return result;
	}


	//endregion 上门服务

	//endregion [客服功能]

	//region [公共部分]

	/**
	 * 检查账号是否有权限
	 * @param permissionTag 权限标签
	 * @param errorMsg	错误提示内容
	 * @return
	 */
	private AjaxJsonEntity hasPermission(String permissionTag,String errorMsg){
		if (!SecurityUtils.getSubject().isPermitted(permissionTag)) {
			return AjaxJsonEntity.fail(errorMsg, null);
		}else{
			return new AjaxJsonEntity(true);
		}
	}

	/**
	 * 上传订单附件
	 * @param filePath
	 * @param orginalName
	 * @param orderId
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "saveAttach")
	public AjaxJsonEntity saveAttach(@RequestParam String filePath, @RequestParam String orginalName,@RequestParam String orderId, HttpServletResponse response)
	{
		User user = UserUtils.getUser();
		response.setContentType("application/json; charset=UTF-8");
		AjaxJsonEntity jsonEntity = new AjaxJsonEntity(true);
		try
		{
			Long lorderId = Long.valueOf(orderId);
			if(lorderId==null || lorderId<=0){
				jsonEntity.setSuccess(false);
				jsonEntity.setMessage("参数错误");
				return jsonEntity;
			}
			Order order = orderService.getOrderById(lorderId, "",OrderUtils.OrderDataLevel.CONDITION,true);
			if(order==null || order.getOrderCondition() == null){
				jsonEntity.setSuccess(false);
				jsonEntity.setMessage("系统繁忙，读取订单失败，请重试");
				return jsonEntity;
			}
			Long  customerId  = order.getOrderCondition().getCustomer().getId();
			Customer customer = customerService.getFromCache(customerId);
//			Customer customer = (Customer)redisUtils.zRangeOneByScore(RedisConstant.RedisDBType.REDIS_MD_DB,RedisConstant.MD_CUSTOMER_ALL,customerId,customerId,Customer.class);
			int max = customer.getMaxUploadNumber();
			if (max > 0 && order.getAttachments().size() >= max)
			{
				jsonEntity.setSuccess(false);
				jsonEntity.setMessage("该客户最多只能上传" + max + "张图片");
				return jsonEntity;
			}

			OrderAttachment attachment = new OrderAttachment();
			attachment.setQuarter(order.getQuarter());//数据库分片
			attachment.setFilePath(filePath);
			attachment.setOrderId(lorderId);
			attachment.setRemarks(orginalName);
			attachment.setCreateBy(user);
			attachment.setCreateDate(new Date());
			attachmentService.save(attachment);
		} catch (OrderException oe){
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage(oe.getMessage());
        } catch (Exception e){
			jsonEntity.setSuccess(false);
			jsonEntity.setMessage(e.getMessage());
		}
		// 更新订单信息
		return jsonEntity;
	}

	/**
	 * 删除上传的附件/
	 * @param attachmentid
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "deleteAttach")
	public AjaxJsonEntity deleteAttach(@RequestParam String attachmentid,@RequestParam String orderId, HttpServletResponse response)
	{
		User user = UserUtils.getUser();
		response.setContentType("application/json; charset=UTF-8");
		AjaxJsonEntity result = new AjaxJsonEntity(true);
		try
		{
			Long lattachmentid = Long.valueOf(attachmentid);
			Long lorderId = Long.valueOf(orderId);
			if(lattachmentid==null || lattachmentid<=0 || lorderId==null || lorderId<=0){
				result.setSuccess(false);
				result.setMessage("参数错误");
			}
			else
			{
				String quarter = orderService.getOrderQuarterFromCache(lorderId);
				OrderAttachment attachment = new OrderAttachment();
				attachment.setId(lattachmentid);
				attachment.setQuarter(quarter);//*
				attachment.setOrderId(lorderId);
				attachment.setCreateBy(user);
				attachment.setCreateDate(new Date());
				attachmentService.delete(attachment);
			}

			return result;
		} catch (OrderException oe){
            result.setSuccess(false);
            result.setMessage(oe.getMessage());
        } catch (Exception e){
			result.setSuccess(false);
			result.setMessage("删除附件错误:" + e.getMessage());
			log.error("[OrderController.deleteAttach] orderId:{} , attachmentId:{}",orderId,attachmentid, e);
		}
		return result;
	}


	/**
	 * 订单附件的查看
	 *
	 * @param orderId 订单id
	 */
	@RequestMapping(value = { "viewDetailAttachment" })
	public String viewDetailAttachment(@RequestParam String orderId,@RequestParam String quarter, HttpServletRequest request, Model model)
	{
		Order order = new Order();
		Long lorderId = Long.valueOf(orderId);
		if (lorderId!=null || lorderId > 0)
		{
			order = orderService.getOrderById(lorderId,quarter, OrderUtils.OrderDataLevel.DETAIL,true);
			if(order == null || order.getOrderCondition() == null) {
				addMessage(model, "错误：系统繁忙，读取订单失败，请重试。");
				model.addAttribute("order", new Order());
				return "modules/sd/viewOrderAttachmentForm";
			}
			if(order.getOrderCondition().getFinishPhotoQty() ==0)
			{
				addMessage(model, "此单上门服务没有添加附件");
				model.addAttribute("order", order);
				return "modules/sd/viewOrderAttachmentForm";
			}

			List<OrderAttachment> attachments = order.getAttachments();
			if(attachments == null||attachments.size()==0){
				attachments = attachmentService.getAttachesByOrderId(lorderId,order.getQuarter());
				if(attachments==null || attachments.size()==0){
					attachments = Lists.newArrayList();
				}else{
					//write to cache
					/*String key = String.format(RedisConstant.SD_ORDER, orderId);
					if(redisUtils.exists(RedisConstant.RedisDBType.REDIS_SD_DB,key)) {
						try {
							redisUtils.hmSet(RedisConstant.RedisDBType.REDIS_SD_DB, key, "attachments", attachments, 0l);
						} catch (Exception e) {
							log.error("[OrderController.viewDetailAttachement] write cache key:{}",key, e);
						}
					}*/
					//调用公共缓存
					OrderCacheParam.Builder builder = new OrderCacheParam.Builder();
					builder.setOpType(OrderCacheOpType.UPDATE)
							.setOrderId(lorderId)
							.setAttachments(attachments);
					OrderCacheUtils.update(builder.build());
				}
			}
			order.setAttachments(attachments);
		}
		model.addAttribute("order", order);
		return "modules/sd/viewOrderAttachmentForm";
	}

	/**
     * [Ajax]订单日志-跟踪进度
     * 按日期顺序
     * @param orderId	订单id
     */
    @ResponseBody
    @RequestMapping(value = "trackingLog")
    public AjaxJsonEntity trackingLog(@RequestParam String orderId,@RequestParam String quarter ,String isCustomer,HttpServletResponse response)
    {
        User user = UserUtils.getUser();
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity jsonEntity = new AjaxJsonEntity(true);
        try
        {
            Long lorderId = Long.valueOf(orderId);
            if(lorderId == null || lorderId<=0){
                jsonEntity.setSuccess(false);
                jsonEntity.setMessage("参数错误");
                return jsonEntity;
            }
            Boolean bIsCustomer = false;
            //读取跟踪进度,statusFlag 0:未更改订单状态 1:订单状态变更 4：进度跟踪
            List<Integer> statusFlags = Lists.newArrayList();
            statusFlags.add(4);
            statusFlags.add(1);
            if(StringUtils.isNoneBlank(isCustomer) && isCustomer.equalsIgnoreCase("true")){
                bIsCustomer = true;
            }else{
                statusFlags.add(0);
            }
            List<OrderProcessLog> trackings = orderService.getOrderLogsByFlags(lorderId,quarter,statusFlags,null);
            if(CollectionUtils.isEmpty(trackings)){
                jsonEntity.setData(Lists.newArrayList());
            }else {
                if(bIsCustomer){
					List<OrderProcessLog> news = Lists.newArrayList();
					int customer_visable_flag = 2;
					trackings.stream().filter(t-> (t.getVisibilityFlag().intValue()&customer_visable_flag) == customer_visable_flag)
							.forEach(t->{
								t.setRemarks(t.getActionComment());
								news.add(t);
							});
					List<OrderProcessLog> logs = news.stream()
							.sorted(Comparator.comparing(OrderProcessLog::getId).reversed()).collect(Collectors.toList());
					jsonEntity.setData(logs);
                    /*正常日志（订单状态变更的）,将actionComment -> remarks
                    List<OrderProcessLog> news = Lists.newArrayList();
                    trackings.stream().filter(t->t.getStatusFlag() == 1)
                            .forEach(t->{
                                t.setRemarks(t.getActionComment());
                                news.add(t);
                            });
                    //跟踪进度只显示remarks不为空的记录
                    trackings.stream().filter(t-> t.getStatusFlag()==4 && StringUtils.isNoneBlank(t.getRemarks()))
                            .forEach(t->news.add(t));
                    trackings = news.stream().sorted(Comparator.comparing(OrderProcessLog::getId)).collect(Collectors.toList());
                    */
                }else {
					int customer_visable_flag = 2;
					//trackings = trackings.stream().filter(t->t.getVisibilityFlag().intValue()!=5 && t.getStatusValue().intValue()!=80).collect(Collectors.toList());
					trackings.stream().filter(t-> (t.getVisibilityFlag().intValue()&customer_visable_flag) == customer_visable_flag)
							.forEach(t->{
								t.setRemarks(t.getActionComment());
							});
					List<OrderProcessLog> logs = trackings.stream().sorted(Comparator.comparing(OrderProcessLog::getId))
                            .collect(Collectors.toList());
					jsonEntity.setData(logs);
                }
            }
        } catch (Exception e)
        {
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage(e.getMessage());
        }
        return jsonEntity;
    }

	/**
	 * [Ajax]订单详情-跟踪进度
	 * 包含：要增加内容和记录
	 * 显示顺序按日期倒序
	 * @param orderId	订单id
	 */
	@ResponseBody
	@RequestMapping(value = "orderDetailTracking")
	public AjaxJsonEntity orderDetailTracking(@RequestParam String orderId,String quarter,@RequestParam(required = false) Integer status, HttpServletResponse response)
	{
		User user = UserUtils.getUser();
		response.setContentType("application/json; charset=UTF-8");
		AjaxJsonEntity jsonEntity = new AjaxJsonEntity(true);
		try
		{
			Long lorderId = Long.valueOf(orderId);
			if(status == null) {
				Order order = orderService.getOrderById(lorderId, quarter, OrderUtils.OrderDataLevel.CONDITION, true);
				if(order == null || order.getOrderCondition() == null){
					jsonEntity.setSuccess(false);
					jsonEntity.setMessage("系统繁忙，读取订单失败，请重试");
					return jsonEntity;
				}
				// 检查是否可以保存
				if (!order.canTracking() && order.getOrderCondition().getPendingFlag() != 2) {
					jsonEntity.setSuccess(false);
					jsonEntity.setMessage(String.format("该订单不能进度跟踪，当前订单状态:%s", order.getOrderCondition().getStatus().getLabel()));
					return jsonEntity;
				}
			}else if(status.intValue()<Order.ORDER_STATUS_APPROVED.intValue()){
				jsonEntity.setSuccess(false);
				jsonEntity.setMessage("该订单为待审核订单，请先联系相关人员审核通过后再操作。");
				return jsonEntity;
			}
			OrderTrackingModel trackingModel = new OrderTrackingModel();
			trackingModel.setOrderId(lorderId);
			trackingModel.setTrackingDate(new Date());
			trackingModel.setRemarks("");
			//读取跟踪进度,statusFlag 0:未更改订单状态 1:订单状态变更 4：进度跟踪
			List<OrderProcessLog> list = orderService.getOrderLogsByFlags(lorderId,quarter,Arrays.asList(new Integer[] {0,1,4}),null);
			int customer_visable_flag = 2;
			//过滤客户看见
			List<OrderProcessLog> orderProcessLogs = list.stream().filter(t->t.getVisibilityFlag().intValue()!=customer_visable_flag).collect(Collectors.toList());
			Supplier<Stream<OrderProcessLog>> streamSupplier = () -> orderProcessLogs.stream();
			//客户能看
			streamSupplier.get().filter(t-> (t.getVisibilityFlag().intValue()&customer_visable_flag) == customer_visable_flag)
					.forEach(t->{
						t.setRemarks(t.getActionComment());
					});
			//按时间正序
			List<OrderProcessLog> logs = streamSupplier.get()
					.sorted(Comparator.comparing(OrderProcessLog::getId).reversed()).collect(Collectors.toList());
			//list = list.stream().sorted(Comparator.comparing(OrderProcessLog::getId).reversed()).collect(Collectors.toList());
			trackingModel.setLogs(logs);
			//跟踪项目
//			List<Dict> types = DictUtils.getDictList("TrackingType");
			//切换为微服务
			List<Dict> types = msDictService.findListByType("TrackingType");
			trackingModel.setTracks(types);
			jsonEntity.setData(trackingModel);
		} catch (Exception e)
		{
			jsonEntity.setSuccess(false);
			jsonEntity.setMessage(e.getMessage());
		}
		return jsonEntity;
	}

	/**
	 * [Ajax]订单日志-异常处理
	 * 对账时，财务标记异常
	 * @param orderId	订单id
	 */
	@ResponseBody
	@RequestMapping(value = "exceptLog")
	public AjaxJsonEntity exceptLog(@RequestParam String orderId, @RequestParam String quarter, HttpServletResponse response)
	{
        response.setContentType("application/json; charset=UTF-8");
        User user = UserUtils.getUser();
		AjaxJsonEntity jsonEntity = new AjaxJsonEntity(true);
		if(user == null || user.getId()==null){
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage("登录已超时");
            return jsonEntity;
        }
		try
		{
			Long lorderId = Long.valueOf(orderId);
			if(lorderId == null || lorderId <=0){
				jsonEntity.setSuccess(false);
				jsonEntity.setMessage("订单参数错误");
				return jsonEntity;
			}
			//读取异常处理,statusFlag 2:生成对账单时标记异常 6：订单异常处理
			List<OrderProcessLog> excepts = orderService.getOrderLogsByFlags(lorderId,quarter,Arrays.asList(new Integer[] {2,6}),null);
			if(excepts ==null){
				jsonEntity.setData(Lists.newArrayList());
			}else {
				jsonEntity.setData(excepts);
			}
		} catch (Exception e)
		{
			jsonEntity.setSuccess(false);
			jsonEntity.setMessage(e.getMessage());
		}
		return jsonEntity;
	}

	/**
	 * [Ajax]订单日志-问题反馈
	 * @param id	问题反馈id
	 */
	@ResponseBody
	@RequestMapping(value = "feedbackLog")
	public AjaxJsonEntity feedbackLog(@RequestParam String id,@RequestParam String quarter, HttpServletResponse response)
	{
		User user = UserUtils.getUser();
		response.setContentType("application/json; charset=UTF-8");
		AjaxJsonEntity jsonEntity = new AjaxJsonEntity(true);
		try
		{
			Long lorderId = Long.valueOf(id);
			if(lorderId == null || lorderId <=0){
				jsonEntity.setSuccess(false);
				jsonEntity.setMessage("订单参数错误");
				return jsonEntity;
			}
			List<FeedbackItem> items = feedbackService.getFeedbackItems(lorderId,quarter);
			if(items ==null){
				jsonEntity.setData(Lists.newArrayList());
			}else {
				jsonEntity.setData(items);
			}
		} catch (Exception e)
		{
			jsonEntity.setSuccess(false);
			jsonEntity.setMessage(e.getMessage());
		}
		return jsonEntity;
	}

	/**
	 * 订单产品选择，从订单明细中读取，如果有套组，将分别将套组的组成产品显示在列表中
	 *
	 * @param orderId
	 * @param request
	 * @param response
	 * @param model
	 * @return
	 */
	@RequiresUser
	@RequestMapping(value = "selectproduct")
	public String selectProduct(@RequestParam String orderId,@RequestParam String quarter, HttpServletRequest request, HttpServletResponse response, Model model)
	{
		List<Product> list = Lists.newArrayList();
		Page<Product> page = new Page<Product>();
		Long lorderId = Long.valueOf(orderId);
		if (lorderId != null && lorderId>0)
		{
			Order order = orderService.getOrderById(lorderId,quarter, OrderUtils.OrderDataLevel.CONDITION,true);
			if(order == null){
				addMessage(model,"错误：系统繁忙，读取订单失败，请重试。");
				model.addAttribute("page", page);
				return "modules/sd/productSelect";
			}
			Map<String, String> maps = Maps.newHashMap();
			Map<Long,ServiceType> serviceTypeMap = serviceTypeService.getAllServiceTypeMap();
			ServiceType serviceType;
			String key = new String("");
			for (OrderItem item : order.getItems())
			{
				serviceType = serviceTypeMap.get(item.getServiceType().getId());
				if(serviceType == null){
					addMessage(model,"错误：数据处理失败，请重试。");
					model.addAttribute("page", page);
					return "modules/sd/productSelect";
				}
				key = String.format("%s_%s",item.getProduct().getId(),serviceType.getId());
				if (!maps.containsKey(key))
				{
					maps.put(key, "");
					Product cp = new Product();
					BeanUtils.copyProperties(item.getProduct(),cp);
					cp.setBrand(item.getBrand());
					cp.setModel(item.getProductSpec());
					cp.setServiceType(serviceType);
					list.add(cp);
					// 套组
					if (cp.getSetFlag() == 1)
					{
						List<Product> slist = productService.getProductListOfSet(cp.getId());
						for (Product sp : slist)
						{
							if (!maps.containsKey(String.format("%s_%s",sp.getId(),serviceType.getId())))
							{
								maps.put(String.format("%s_%s",sp.getId(),serviceType.getId()),"");
								Product scp = new Product();
								BeanUtils.copyProperties(sp,scp);
								scp.setServiceType(serviceType);
								scp.setBrand(item.getBrand());
								scp.setModel(item.getProductSpec());
								list.add(scp);
							}
						}
					}
				}
			}
		}

		page.setPageSize(list.size());
		page.setPageNo(1);
		page.setCount(list.size());
		page.setList(list);
		model.addAttribute("page", page);
		return "modules/sd/productSelect";
	}

	/**
	 * ajax获取客户价格的备注信息
	 */
	@ResponseBody
	@RequestMapping(value = "getCustomerPrice", method = RequestMethod.POST)
	public AjaxJsonEntity getCustomerPrice(@RequestParam Long customerId,@RequestParam Long productId,@RequestParam Long serviceTypeId, HttpServletResponse response)
	{
		response.setContentType("application/json; charset=UTF-8");
		AjaxJsonEntity result = new AjaxJsonEntity(true);
		try
		{
//			List<CustomerPrice> prices = redisUtils.zRange(RedisConstant.RedisDBType.REDIS_MD_DB,String.format(RedisConstant.MD_CUSTOMER_PRICE,customerId),0,-1,CustomerPrice.class);
			List<CustomerPrice> prices = customerService.getPricesFromCache(customerId);
			if(prices == null || prices.size()==0){
				result.setMessage("无");
			}else{
				CustomerPrice price = prices.stream()
						.filter(t->Objects.equals(t.getProduct().getId(),productId)
								&& Objects.equals(t.getServiceType().getId(),serviceTypeId)
						).findFirst().orElse(null);
				if(price==null || StringUtils.isBlank(price.getRemarks())){
					result.setMessage("无");
				}else{
					result.setMessage(price.getRemarks());
				}
			}

		} catch (OrderException oe){
            result.setSuccess(false);
            result.setMessage(oe.getMessage());
        } catch (Exception e){
			result.setSuccess(false);
			result.setMessage("读取产品说明错误:" + e.getMessage());
			log.error("[OrderController.getCustomerPrice] customerId:{}",customerId, e);
		}
		return result;
	}

    /**
     * 计算加急费
     * @param customerId 客户id
     * @param areaId    区域id (要转换成省的id)
     * @param urgentLevelId 加急等级
     */
	@ResponseBody
	@RequestMapping(value = "getCustomerUrgentCharge", method = RequestMethod.POST)
	public AjaxJsonEntity getCustomerUrgentCharge(@RequestParam String customerId,@RequestParam String areaId,@RequestParam String urgentLevelId, HttpServletResponse response)
	{
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity result = new AjaxJsonEntity(true);
		HashMap<String,Object> maps = Maps.newHashMap();
		maps.put("chargeIn",0.00d);
		maps.put("chargeOut",0.00d);
		if(StringUtils.isBlank(customerId) || StringUtils.isBlank(areaId) || StringUtils.isBlank(urgentLevelId)){
			result.setSuccess(false);
			result.setData(maps);
			result.setMessage("读取加急费错误，参数无值!");
			return result;
		}
	    try{
            Long cid = StringUtils.toLong(customerId);
            Long aid = StringUtils.toLong(areaId);
            Long uid = StringUtils.toLong(urgentLevelId);
            maps = orderService.getCustomerUrgentCharge(cid,aid,uid,"false");
            if(maps == null || maps.size() == 0){
				result.setSuccess(false);
				result.setMessage("该客户未设置加急费用标准");
			}
		} catch (Exception e)
		{
			result.setSuccess(false);
			result.setMessage(e.getMessage());
		}finally {
			result.setData(maps);
		}
		return result;
	}

	//endregion [公共部分]

	//region 跟踪进度

	/**
	 * 跟踪订单进度 for kefu form
	 */
	@RequiresPermissions("sd:order:tracking")
	@RequestMapping(value = "tracking", method = RequestMethod.GET)
	public String tracking(@RequestParam String orderId, @RequestParam String quarter,HttpServletRequest request, Model model)
	{
		Long lorderId = Long.valueOf(orderId);
		if(lorderId==null || lorderId<=0){
			model.addAttribute("canAction", false);
			return "modules/sd/tracking/orderTrackingForm";
		}

		Order order = orderService.getOrderById(lorderId, quarter,OrderUtils.OrderDataLevel.DETAIL,true);
		if(order == null || order.getOrderCondition() == null){
			addMessage(model, "错误：系统繁忙，读取订单失败，请重试。");
			model.addAttribute("canAction", false);
			return "modules/sd/tracking/orderTrackingForm";
		}
		// 检查是否可以最终
		if (!order.canTracking() && order.getOrderCondition().getPendingFlag() != 2) {
			addMessage(model, String.format("订单：%s 不能进度跟踪，当前订单状态:%s",order.getOrderNo(),order.getOrderCondition().getStatus().getLabel()));
			model.addAttribute("canAction", false);
			return "modules/sd/tracking/orderTrackingForm";
		}

		order.setTrackingDate(new Date());
		order.setRemarks("");
		//读取跟踪进度,statusFlag 1:订单状态变更 4：进度跟踪
		List<OrderProcessLog> list = orderService.getOrderLogsByFlags(lorderId,order.getQuarter(),Arrays.asList(new Integer[] {1,4}),null);
		order.setLogList(list);
		model.addAttribute("order", order);

//		List<Dict> types = DictUtils.getDictList("TrackingType");
		//切换为微服务
		List<Dict> types = msDictService.findListByType("TrackingType");
		model.addAttribute("tracks", types);
		model.addAttribute("canAction", true);
		return "modules/sd/tracking/orderTrackingForm";
	}

	/**
	 * 客服提交跟踪进度(ajax)
	 *
	 * @param order
	 */
	//@RequiresPermissions("sd:order:tracking") 使用hasPermission判断
	@ResponseBody
	@RequestMapping(value = "tracking", method = RequestMethod.POST)
	public AjaxJsonEntity tracking(Order order, HttpServletResponse response)
	{
		AjaxJsonEntity result = hasPermission("sd:order:tracking", "无跟踪进度权限!");
		//AjaxJsonEntity result = new AjaxJsonEntity(true);
		if(!result.getSuccess()){
			return result;
		}
		try
		{
			User user = UserUtils.getUser();
			order.setCreateBy(user);
			order.setCreateDate(new Date());
			OrderProcessLog log = orderService.saveTracking(order, user.isEngineer());
			result.setData(log);
		} catch (Exception e)
		{
			result.setSuccess(false);
			result.setMessage("进度跟踪时发生异常:" + e.getMessage());
		}
		return result;
	}


	/**
	 * 跟踪订单进度 form 安维人员添加的跟踪进度
	 *
	 * @param orderId
	 * @param request
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "trackingEnginner", method = RequestMethod.GET)
	public String trackingEnginner(String orderId,String quarter, HttpServletRequest request, Model model)
	{
		Long lorderId = Long.valueOf(orderId);
		if(lorderId==null || lorderId<=0){
			model.addAttribute("canAction", false);
			return "modules/sd/tracking/orderTrackingEngineerForm";
		}

		Order order = orderService.getOrderById(lorderId,quarter, OrderUtils.OrderDataLevel.DETAIL,true);
		if(order == null || order.getOrderCondition() == null){
			addMessage(model, "错误：系统繁忙，读取订单失败，请重试。");
			model.addAttribute("canAction", false);
			return "modules/sd/tracking/orderTrackingEngineerForm";
		}
		// 检查是否可以追踪
		if (!order.canTracking() && order.getOrderCondition().getPendingFlag() != 2) {
			addMessage(model, String.format("订单：%s 不能进度跟踪，当前订单状态:%s",order.getOrderNo(),order.getOrderCondition().getStatus().getLabel()));
			model.addAttribute("canAction", false);
			return "modules/sd/tracking/orderTrackingEngineerForm";
		}

		order.setTrackingDate(new Date());
		order.setRemarks("");
		User user = UserUtils.getUser();
		//读取跟踪进度 statusFlag:4 进度跟踪 closeFlag=2:安维提交
        List<OrderProcessLog> list = orderService.getAppOrderLogs(lorderId,order.getQuarter().trim());
		if(user.isEngineer()) {
            Engineer e = servicePointService.getEngineer(user.getEngineerId());
            //派单记录
            List<OrderPlan> plans = orderService.getOrderPlanList(lorderId, order.getQuarter(), null);
            list = OrderUtils.filterServicePointOrderProcessLog(list, plans, e.getServicePoint().getId());
        }
		order.setLogList(list);
		model.addAttribute("order", order);

		//切换为微服务
		List<Dict> types = msDictService.findListByType("TrackingType");
		model.addAttribute("tracks", types);
		model.addAttribute("canAction", true);
		return "modules/sd/tracking/orderTrackingEngineerForm";
	}


	/**
	 * 安维提交跟踪进度(ajax)
	 */
	@ResponseBody
	@RequestMapping(value = "trackingEngineer", method = RequestMethod.POST)
	public AjaxJsonEntity trackingEnginner(Order order, HttpServletResponse response)
	{
		AjaxJsonEntity result = new AjaxJsonEntity(true);
		try
		{
			OrderProcessLog log = orderService.saveTracking(order, true);
			result.setData(log);
		} catch (Exception e)
		{
			result.setSuccess(false);
			result.setMessage("进度跟踪时发生异常:" + e.getMessage());
		}
		return result;
	}

	//endregion 跟踪进度

	//region 客评

	/**
	 * 客评弹窗
	 * 检查订单是否可以客评，并装载客评项
	 */
	@RequiresPermissions("sd:order:grade")
	@RequestMapping(value = { "grade" })
	public String grade(@RequestParam String orderId,@RequestParam(required = false) String quarter, HttpServletRequest request, HttpServletResponse response, Model model) {
		Long lorderId = Long.valueOf(orderId);
		String lockkey = String.format(RedisConstant.SD_ORDER_LOCK, orderId);
		if (redisUtils.exists(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey)) {
			addMessage(model, "错误：此订单正在处理中，请稍候重试，或刷新页面。");
			return gradeFormResult(false, false, null, model);
		}
		Order order = orderService.getOrderById(lorderId, quarter, OrderUtils.OrderDataLevel.DETAIL, true,false,false,true);
		if (order == null || order.getOrderCondition() == null) {
			addMessage(model, "错误：系统繁忙，读取订单失败，请重试。");
			return gradeFormResult(false, false, null, model);
		}
		if (!order.canGrade()) {
			addMessage(model, "错误：请确认及检查订单状态及是否有异常未处理，或是有已客评。");
			return gradeFormResult(false, false, null, model);
		}
		/* 检查是否有未审核的配件
		Integer qty = orderMaterialService.getNoApprovedMaterialMasterQty(lorderId,order.getQuarter());
		if (qty != null && qty > 0) {
			addMessage(model, "错误：还有配件申请未通过审核或未返件!");
			model.addAttribute("canAction", false);
			return "modules/sd/service/orderGradeForm";
		}*/
		OrderGradeModel gradeModel = null;
		// 根据订单配件状态检查是否可以客评 2019/06/13 22:52 at home
		MSResponse msResponse = orderMaterialService.canGradeOfMaterialForm(order.getDataSourceId(), lorderId, order.getQuarter());
		if (!MSResponse.isSuccessCode(msResponse)) {
			addMessage(model, "错误：" + msResponse.getMsg());
			//需手动关闭
			if (msResponse.getCode() == 1) {
				gradeModel = new OrderGradeModel();
				gradeModel.setOrderId(lorderId);
				gradeModel.setOrderNo(order.getOrderNo());
				gradeModel.setQuarter(order.getQuarter());
				//model.addAttribute("orderGrade", gradeModel);
				model.addAttribute("manuCloseMaterialForm", 1);
			}
			return gradeFormResult(false, false, gradeModel, model);
		}
		//费用异常
		Boolean chargeError = orderService.checkOrderFeeAndServiceAmountBeforeGrade(order, true);
		if (!chargeError) {
			addMessage(model, "错误：此单费用异常");
			return gradeFormResult(false, false, gradeModel, model);
		}
		String key = String.format(RedisConstant.SD_ORDER, lorderId);
		//检查附件数量
		OrderCondition orderCondition = order.getOrderCondition();
		Long cid = order.getOrderCondition().getCustomer().getId();
		//退换货流程
		if (orderCondition.getOrderServiceType() == OrderUtils.OrderTypeEnum.EXCHANGE.getId()
				|| orderCondition.getOrderServiceType() == OrderUtils.OrderTypeEnum.BACK.getId()) {
			//2020-10-20 退换货检查完工项目是否上传及同步厂商系统
			Boolean returnCompleteCheckResult = returnCompleteService.isCompleted(order.getId(), order.getQuarter());
			if (!returnCompleteCheckResult) {
				addMessage(model, "错误：退/换货完工信息不完整,或还未同步到厂商系统。");
				return gradeFormResult(false, false, gradeModel, model);
			}
		} else {
			//非退换货流程，包含：安装，维修，及其他
			//检查未上传的产品
			List<OrderItemComplete> itemCompleteList = orderItemCompleteService.findItemCompleteByOrderId(order.getId(), order.getQuarter());
			//按产品分组
			Map<Long, List<OrderItemComplete>> itemCompleteMap = itemCompleteList.stream().collect(Collectors.groupingBy(t -> t.getProduct().getId()));
			for (OrderDetail item : order.getDetailList()) {
				Product entity = productService.getProductByIdFromCache(item.getProductId());
				if (entity.getSetFlag() == 1) {
					//套组
					String[] productIds = entity.getProductIds().split(",");
					for (int i = 0; i < productIds.length; i++) {
						List<OrderItemComplete> itemCompletes = itemCompleteMap.get(Long.valueOf(productIds[i]));
						if (itemCompletes == null || itemCompletes.size() <= 0) {
							ProductCompletePic completePic = customerPicService.getFromCache(Long.valueOf(productIds[i]), cid);
							if (completePic == null) {
								//如果客户没有配置,再从产品配置获取
								completePic = productCompletePicService.getFromCache(Long.valueOf(productIds[i]));
							}
							if (completePic != null) {
								completePic.parseItemsFromJson();
								if (completePic.getItems() != null && completePic.getItems().size() > 0) {
									List<ProductCompletePicItem> completePicItemList = completePic.getItems().stream().filter(t -> t.getMustFlag() == 1).collect(Collectors.toList());
									if (completePicItemList != null && completePicItemList.size() > 0) {
										Product product = productService.getProductByIdFromCache(Long.valueOf(productIds[i]));
										addMessage(model, "错误：产品[" + product.getName() + "] 未上传完工图片");
										return gradeFormResult(false, false, gradeModel, model);
									}
								}
							}
						}
					}
				} else {
					List<OrderItemComplete> itemCompletes = itemCompleteMap.get(item.getProductId());
					if (itemCompletes == null || itemCompletes.size() <= 0) {
						ProductCompletePic completePic = customerPicService.getFromCache(item.getProductId(), cid);
						if (completePic == null) {
							//如果客户没有配置,再从产品配置获取
							completePic = productCompletePicService.getFromCache(item.getProductId());
						}
						if (completePic != null) {
							completePic.parseItemsFromJson();
							if (completePic.getItems() != null && completePic.getItems().size() > 0) {
								List<ProductCompletePicItem> completePicItemList = completePic.getItems().stream().filter(t -> t.getMustFlag() == 1).collect(Collectors.toList());
								if (completePicItemList != null && completePicItemList.size() > 0) {
									Product product = productService.getProductByIdFromCache(item.getProductId());
									addMessage(model, "错误：产品[" + product.getName() + "] 未上传完工图片");
									return gradeFormResult(false, false, gradeModel, model);
								}
							}
						}
					}
				}
			}
			Customer customer = customerService.getFromCache(cid);
			int uploadPicCount = orderItemCompleteService.getUploadCountByOrderId(order.getId(), order.getQuarter());
			int min = customer.getMinUploadNumber();
			int max = customer.getMaxUploadNumber();
			if (min > 0 && uploadPicCount < min) {
				addMessage(model, "错误：此订单的客户[ " + customer.getName() + " ]已设置必须上传"
						+ min + "~" + max + "张图片,请在上门服务界面去添加附件图片");
				return gradeFormResult(false, false, gradeModel, model);
			}
		}
		gradeModel = orderService.getOrderGrade(order.getOrderCondition());
		if (gradeModel.getGradeList().size() == 0) {
			addMessage(model, "错误：读取客评项目失败，请确认是否维护了客评资料。");
			return gradeFormResult(false, false, gradeModel, model);
		}

		if (order.getOrderCondition().getGradeFlag() > 0) {
			addMessage(model, "错误：此工单已客评。");
			return gradeFormResult(false, false, gradeModel, model);
		}

		if (order.getOrderCondition().getChargeFlag() != null && order.getOrderCondition().getChargeFlag() == 1) {
			addMessage(model, "错误：此工单已生成对账单，请联系管理员");
			return gradeFormResult(false, false, gradeModel, model);
		}
		if (order.getOrderCondition().getAppAbnormalyFlag() == 1) {
			//判断是否有异常未处理
			addMessage(model, "错误：APP异常还未处理，不能客评。请刷新订单页面");
			return gradeFormResult(false, false, gradeModel, model);
		}
		// 2020-09-25 云米：增加[完工]的判断
		int dataSource = order.getDataSourceId();
		if (dataSource == B2BDataSourceEnum.VIOMI.getId()) {
			if (StringUtils.isEmpty(orderCondition.getAppCompleteType())) {
				addMessage(model, "错误：此订单需先[完工]，然后方可[客评]");
				return gradeFormResult(false, false, gradeModel, model);
			}
		} else {
			//其他数据源,按品类检查App完工开关
			int appCompleteFlag = Optional.ofNullable(orderCondition.getProductCategory()).map(p->p.getAppCompleteFlag()).orElse(0);
			int status = orderCondition.getStatusValue();
			if(appCompleteFlag == 1 && status != Order.ORDER_STATUS_APP_COMPLETED ){
				addMessage(model, "错误：此订单需先[完工]，然后方可[客评]");
				return gradeFormResult(false, false, gradeModel, model);
			}
		}

		//检查是否上传条码
		Boolean checkSNResult = orderService.checkOrderProductBarCode(lorderId, order.getQuarter(), cid, order.getDetailList());
		if (!checkSNResult) {
			addMessage(model, "错误：该厂商要求上传产品序列号，请检查是否已上传！");
			return gradeFormResult(false, false, gradeModel, model);
		}

		//智能语音回访
		Integer voiceResult = null;
		int orderStatusValue = order.getOrderCondition().getStatusValue();
		if (StringUtils.isNotBlank(order.getOrderCondition().getAppCompleteType())
				&& orderStatusValue >= Order.ORDER_STATUS_SERVICED
				&& order.getOrderCondition().getGradeFlag() != OrderUtils.OrderGradeType.APP_GRADE.value) {
			voiceResult = orderVoiceTaskService.getVoiceTaskResult(order.getQuarter(), order.getId());
			if (voiceResult != null) {
				model.addAttribute("voiceResult", voiceResult);
			}
		}

		gradeModel.setQuarter(order.getQuarter());//*
		//设置每个评价项目的默认值
		List<OrderGrade> grades = gradeModel.getGradeList();
		// 时效
		Date timeLinessStartDate = orderService.getServicePointTimeLinessStartDate(order, lorderId, order.getQuarter(), order.getOrderCondition().getServicePoint().getId());
		if (timeLinessStartDate == null) {
			addMessage(model, "错误：读取网点派单日期失败，请重试。");
			return gradeFormResult(false, false, gradeModel, model);
		}

		long productCategoryId = Optional.ofNullable(order.getOrderCondition()).map(t -> t.getProductCategoryId()).orElse(0L);
		Dict timeLinessInfo = null;
		try {
			timeLinessInfo = orderService.getServicePointTimeLinessInfo(Order.TimeLinessType.ALL, order.getOrderCondition(), timeLinessStartDate, productCategoryId, null);
		} catch (Exception e) {
			addMessage(model, "错误：" + e.getMessage());
			return gradeFormResult(false, false, gradeModel, model);
		}

		OrderGrade grade;
		for (int i = 0, size = grades.size(); i < size; i++) {
			grade = grades.get(i);
			//按分数倒序，分数高的排前面，且为默认选项
			GradeItem gradeItem = grade.getItems().stream().sorted(Comparator.comparing(GradeItem::getPoint).reversed())
					.findFirst().orElse(null);
			if (gradeItem != null) {
				grade.setGradeItemId(gradeItem.getId());
			}
			//时效费
			if (StringUtils.isNotBlank(grade.getDictType()) && TimeLinessPrice.TIME_LINESS_LEVEL.equalsIgnoreCase(grade.getDictType()) && timeLinessInfo != null) {
				//用时
				grade.setRemarks(grade.getRemarks() + String.format("(<font color='red'>实际用时：%s 小时</font>)", timeLinessInfo.getType()));
				int level = timeLinessInfo.getIntValue();
				if (level > 0) {
					gradeItem = grade.getItems().stream().filter(t -> t.getDictValue().equalsIgnoreCase(String.valueOf(level)))
							.findFirst().orElse(null);
					if (gradeItem != null) {
						grade.setGradeItemId(gradeItem.getId());
					}
				}
			}
		}
		return gradeFormResult(true, true, gradeModel, model);
	}

	private String gradeFormResult(Boolean canAction,Boolean canSave,OrderGradeModel gradeModel,Model model){
		model.addAttribute("orderGrade", gradeModel);
		model.addAttribute("canSave", canSave);
		model.addAttribute("canAction", canAction);
		return "modules/sd/service/orderGradeForm";
	}


	@ResponseBody
	@RequestMapping(value = { "savegrade" })
	public AjaxJsonEntity savegrade(OrderGradeModel gradeModel,HttpServletRequest request, HttpServletResponse response)
	{
		response.setContentType("application/json; charset=UTF-8");
		AjaxJsonEntity jsonEntity = new AjaxJsonEntity(true);
		if (gradeModel==null || gradeModel.getOrderId()==null)
		{
			jsonEntity.setSuccess(false);
			jsonEntity.setMessage("错误：传入参数没有值。");
			return jsonEntity;
		}
		try {
		    gradeModel.setAutoGradeFlag(OrderUtils.OrderGradeType.MANUAL_GRADE.getValue());
			User user = UserUtils.getUser();
			OrderGrade timelinessGradeItem = gradeModel.getGradeList().stream().filter(t->t.getDictType().equalsIgnoreCase(TimeLinessPrice.TIME_LINESS_LEVEL)).findFirst().orElse(null);
			Integer level = null;
			if(timelinessGradeItem != null){
				level = StringUtils.toInteger(timelinessGradeItem.getDictValue());
				if(level == 0){
					level = null;
				}
			}
			Order order = orderService.getOrderById(gradeModel.getOrderId(), StringUtils.isBlank(gradeModel.getQuarter()) ? "" : gradeModel.getQuarter(), OrderUtils.OrderDataLevel.DETAIL, true, true,false,true);
			gradeModel.setOrder(order);
			OrderStatusFlag orderStatusFlag = orderStatusFlagService.getByOrderId(order.getId(),order.getQuarter());
			orderService.saveGrade(gradeModel,orderStatusFlag,user,null,level);
		} catch (OrderException oe){
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage(oe.getMessage());
        } catch (Exception e){
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage(e.getMessage());
            log.error("[OrderController.savegrade] orderId:{}",gradeModel.getOrderId(),e);
		}
		return jsonEntity;
	}

	//endregion 客评

	//region 客服完工(云米)

	@RequiresPermissions("sd:order:grade")
	@RequestMapping(value = "completeForViomi")
	public String completeForViomi(@RequestParam String orderId,@RequestParam(required = false) String quarter, HttpServletRequest request, HttpServletResponse response, Model model) {
		KefuCompleteModel completeModel = new KefuCompleteModel();
		model.addAttribute("completeModel", completeModel);
		Long lorderId = Long.valueOf(orderId);
		String lockkey = String.format(RedisConstant.SD_ORDER_LOCK,orderId);
		String viewForm = "modules/sd/service/completeFormForViomi";
		if(redisUtils.exists(RedisConstant.RedisDBType.REDIS_LOCK_DB,lockkey)){
			addMessage(model, "错误：此工单正在处理中，请稍候重试，或刷新页面。");
			model.addAttribute("canAction", false);
			return viewForm;
		}
		Order order = orderService.getOrderById(lorderId, quarter,OrderUtils.OrderDataLevel.DETAIL, true);
		if (order == null || order.getOrderCondition() == null) {
			addMessage(model, "错误：系统繁忙，读取工单失败，请重试。");
			model.addAttribute("canAction", false);
			return viewForm;
		}
		int status = order.getOrderCondition().getStatusValue();
		if(status != Order.ORDER_STATUS_SERVICED){
			addMessage(model, "错误：工单不是 [已上门] 状态，不能完工。");
			model.addAttribute("canAction", false);
			return viewForm;
		}
		Long cid = order.getOrderCondition().getCustomer().getId();
		boolean canAction = true;
		if(order.getOrderCondition().getGradeFlag() > 0){
			canAction = false;
			addMessage(model, "错误：此工单已客评。");
		}else if(order.getOrderCondition().getChargeFlag() != null && order.getOrderCondition().getChargeFlag() == 1) {
			canAction = false;
			addMessage(model, "错误：此工单已生成对账单，请联系管理员");
		}else if(StringUtils.isNotBlank(order.getOrderCondition().getAppCompleteType())){
			canAction = false;
			addMessage(model, "错误：此工单已完工操作，请刷新页面确认");
		}
		int dataSource = order.getDataSourceId();
		if(dataSource != B2BDataSourceEnum.VIOMI.getId()){
			canAction = false;
			addMessage(model, "错误：此工单不需要[完工]操作");
		} else {
			//检查是否上传条码
			Boolean checkSNResult = orderService.checkOrderProductBarCode(lorderId, order.getQuarter(), cid, order.getDetailList());
			if (!checkSNResult) {
				canAction = false;
				addMessage(model, "错误：该厂商要求上传产品序列号，请检查是否已上传！");
			}
		}
		completeModel.setOrderId(lorderId);
		completeModel.setOrderNo(order.getOrderCondition().getOrderNo());
		completeModel.setQuarter(order.getQuarter());
		Long buyDate = order.getOrderAdditionalInfo() != null && order.getOrderAdditionalInfo().getBuyDate() != null ? order.getOrderAdditionalInfo().getBuyDate() : 0L;
		if (buyDate > 0) {
			completeModel.setBuyDate(new Date(buyDate));
		}
		model.addAttribute("completeModel", completeModel);
		model.addAttribute("canAction", canAction);
		return viewForm;
	}

	@ResponseBody
	@RequestMapping(value = "saveCompleteForViomi")
	public AjaxJsonEntity saveCompleteForViomi(KefuCompleteModel kefuCompleteModel,HttpServletRequest request, HttpServletResponse response)
	{
		response.setContentType("application/json; charset=UTF-8");
		AjaxJsonEntity jsonEntity = new AjaxJsonEntity(true);
		if (kefuCompleteModel == null || kefuCompleteModel.getOrderId() == null)
		{
			jsonEntity.setSuccess(false);
			jsonEntity.setMessage("错误：传入参数没有值。");
			return jsonEntity;
		}
		try {
			User user = UserUtils.getUser();
			Order order = orderService.getOrderById(kefuCompleteModel.getOrderId(), kefuCompleteModel.getQuarter(), OrderUtils.OrderDataLevel.DETAIL, true, true);
			if(order == null || order.getOrderCondition() == null){
				jsonEntity.setSuccess(false);
				jsonEntity.setMessage("错误：订单不存在或读取错误。");
				return jsonEntity;
			}
			int dataSource = order.getDataSourceId();
			if(dataSource != B2BDataSourceEnum.VIOMI.getId()){
				jsonEntity.setSuccess(false);
				jsonEntity.setMessage("错误：该订单不需要[完工]操作。");
				return jsonEntity;
			}
			if (kefuCompleteModel.getBuyDate() == null) {
				jsonEntity.setSuccess(false);
				jsonEntity.setMessage("错误：没有设置购买时间的工单不允许进行[完工]操作");
				return jsonEntity;
			}
			if (order.getOrderCondition().getCreateDate() == null || kefuCompleteModel.getBuyDate().getTime() > order.getOrderCondition().getCreateDate().getTime()) {
				jsonEntity.setSuccess(false);
				jsonEntity.setMessage("错误：购买时间不允许晚于下单时间");
				return jsonEntity;
			}
			if (msOrderValidateService.isWaitingApproveOrderValidate(order)) {
				jsonEntity.setSuccess(false);
				jsonEntity.setMessage("错误：鉴定单待审核的工单不允许进行[完工]操作。");
				return jsonEntity;
			}
			boolean checkRepairErrorFlag = b2BCenterOrderService.checkRepairError(order.getDataSourceId(), order.getOrderCondition().getOrderServiceType(), order.getDetailList());
			if (!checkRepairErrorFlag) {
				B2BDataSourceEnum dataSourceEnum = B2BDataSourceEnum.valueOf(order.getDataSourceId());
				jsonEntity.setSuccess(false);
				jsonEntity.setMessage("错误：" + dataSourceEnum.name + "维修单的上门项目没有设置维修故障，不能进行完工操作。");
				return jsonEntity;
			}

			int status = order.getOrderCondition().getStatusValue();
			if (status == Order.ORDER_STATUS_COMPLETED.intValue() || status == Order.ORDER_STATUS_CHARGED.intValue()) {
				jsonEntity.setSuccess(false);
				jsonEntity.setMessage("错误：订单已客评。");
				return jsonEntity;
			} else if (status > Order.ORDER_STATUS_CHARGED.intValue()) {
				jsonEntity.setSuccess(false);
				jsonEntity.setMessage("错误：订单已取消或已退单。");
				return jsonEntity;
			} else if (status != Order.ORDER_STATUS_SERVICED.intValue()){
				jsonEntity.setSuccess(false);
				jsonEntity.setMessage("错误：订单状态不是[已上门]，不能完工操作。");
				return jsonEntity;
			}
			//APP完工[55]
			else if(StringUtils.isNotBlank(order.getOrderCondition().getAppCompleteType()) || status == Order.ORDER_STATUS_APP_COMPLETED){
				jsonEntity.setSuccess(false);
				jsonEntity.setMessage("错误：订单已完工操作。");
				return jsonEntity;
			}
			kefuCompleteModel.setUser(user);
			kefuCompleteModel.setOrder(order);
			Dict completeType = MSDictUtils.getDictByValue("compeled_kefu","completed_type");
			if(completeType == null){
				completeType = new Dict("compeled_kefu","客服完工");
			}
			kefuCompleteModel.setCompleteType(completeType);
			kefuCompleteModel.setDataSourceId(order.getDataSourceId());
			orderService.kefuComplete(kefuCompleteModel);
		} catch (OrderException oe){
			jsonEntity.setSuccess(false);
			jsonEntity.setMessage(oe.getMessage());
		} catch (Exception e){
			jsonEntity.setSuccess(false);
			jsonEntity.setMessage(e.getMessage());
			log.error("[OrderController.saveCompleteForViomi] orderId:{}",kefuCompleteModel.getOrderId(),e);
		}
		return jsonEntity;
	}

	//endregion 客服完工(云米)

	//region 客服完工

	@RequiresPermissions("sd:order:grade")
	@RequestMapping(value = "completeForKefu")
	public String completeForKefu(@RequestParam String orderId,@RequestParam(required = false) String quarter, HttpServletRequest request, HttpServletResponse response, Model model) {
		String viewForm = "modules/sd/service/completeFormForKefu";
		KefuCompleteModel completeModel = new KefuCompleteModel();
		model.addAttribute("completeModel", completeModel);
		Long lorderId = Long.valueOf(orderId);
		String lockkey = String.format(RedisConstant.SD_ORDER_LOCK,orderId);
		if(redisUtils.exists(RedisConstant.RedisDBType.REDIS_LOCK_DB,lockkey)){
			addMessage(model, "错误：此工单正在处理中，请稍候重试，或刷新页面。");
			model.addAttribute("canAction", false);
			return viewForm;
		}
		Order order = orderService.getOrderById(lorderId, quarter,OrderUtils.OrderDataLevel.DETAIL, true);
		if (order == null || order.getOrderCondition() == null) {
			addMessage(model, "错误：系统繁忙，读取工单失败，请重试。");
			model.addAttribute("canAction", false);
			return viewForm;
		}
		int status = order.getOrderCondition().getStatusValue();
		if(status == Order.ORDER_STATUS_APP_COMPLETED){
			addMessage(model, "错误：工单已[完成服务]，不能再次完成服务。");
			model.addAttribute("canAction", false);
			return viewForm;
		}
		else if(status != Order.ORDER_STATUS_SERVICED){
			addMessage(model, "错误：工单不是 [已上门] 状态，不能完成服务。");
			model.addAttribute("canAction", false);
			return viewForm;
		}
		Long cid = order.getOrderCondition().getCustomer().getId();
		boolean canAction = true;
		if(order.getOrderCondition().getGradeFlag() > 0){
			canAction = false;
			addMessage(model, "错误：此工单已客评。");
		}else if(order.getOrderCondition().getChargeFlag() != null && order.getOrderCondition().getChargeFlag() == 1) {
			canAction = false;
			addMessage(model, "错误：此工单已生成对账单，请联系管理员");
		}else if(StringUtils.isNotBlank(order.getOrderCondition().getAppCompleteType())){
			canAction = false;
			addMessage(model, "错误：此工单已完成服务，请刷新页面确认");
		}
		int dataSource = order.getDataSourceId();
		//检查是否上传条码
		Boolean checkSNResult = orderService.checkOrderProductBarCode(lorderId, order.getQuarter(), cid, order.getDetailList());
		if (!checkSNResult) {
			canAction = false;
			addMessage(model, "错误：该厂商要求上传产品序列号，请检查是否已上传！");
		}
		completeModel.setOrderId(lorderId);
		completeModel.setOrderNo(order.getOrderCondition().getOrderNo());
		completeModel.setQuarter(order.getQuarter());
		Long buyDate = order.getOrderAdditionalInfo() != null && order.getOrderAdditionalInfo().getBuyDate() != null ? order.getOrderAdditionalInfo().getBuyDate() : 0L;
		model.addAttribute("completeModel", completeModel);
		model.addAttribute("canAction", canAction);
		return viewForm;
	}

	@ResponseBody
	@RequestMapping(value = "saveCompleteForKefu")
	public AjaxJsonEntity saveCompleteForKefu(KefuCompleteModel kefuCompleteModel,HttpServletRequest request, HttpServletResponse response)
	{
		response.setContentType("application/json; charset=UTF-8");
		AjaxJsonEntity jsonEntity = new AjaxJsonEntity(true);
		if (kefuCompleteModel == null || kefuCompleteModel.getOrderId() == null)
		{
			jsonEntity.setSuccess(false);
			jsonEntity.setMessage("错误：传入参数没有值。");
			return jsonEntity;
		}
		Dict completeType = kefuCompleteModel.getCompleteType();
		if(completeType == null || StringUtils.isBlank(completeType.getValue()) || StringUtils.isBlank(completeType.getLabel())){
			jsonEntity.setSuccess(false);
			jsonEntity.setMessage("请选择完工类型。");
			return jsonEntity;
		}
		try {
			User user = UserUtils.getUser();
			Order order = orderService.getOrderById(kefuCompleteModel.getOrderId(), kefuCompleteModel.getQuarter(), OrderUtils.OrderDataLevel.DETAIL, true, true);
			if(order == null || order.getOrderCondition() == null){
				jsonEntity.setSuccess(false);
				jsonEntity.setMessage("错误：订单不存在或读取错误。");
				return jsonEntity;
			}
			int dataSource = order.getDataSourceId();
			//与云米完工不同，不需要购买时间
			if (msOrderValidateService.isWaitingApproveOrderValidate(order)) {
				jsonEntity.setSuccess(false);
				jsonEntity.setMessage("错误：鉴定单待审核的工单不允许进行[完工]操作。");
				return jsonEntity;
			}
			boolean checkRepairErrorFlag = b2BCenterOrderService.checkRepairError(order.getDataSourceId(), order.getOrderCondition().getOrderServiceType(), order.getDetailList());
			if (!checkRepairErrorFlag) {
				B2BDataSourceEnum dataSourceEnum = B2BDataSourceEnum.valueOf(order.getDataSourceId());
				jsonEntity.setSuccess(false);
				jsonEntity.setMessage("错误：" + dataSourceEnum.name + "维修单的上门项目没有设置维修故障，不能进行完工操作。");
				return jsonEntity;
			}

			int status = order.getOrderCondition().getStatusValue();
			if (status == Order.ORDER_STATUS_COMPLETED.intValue() || status == Order.ORDER_STATUS_CHARGED.intValue()) {
				jsonEntity.setSuccess(false);
				jsonEntity.setMessage("错误：订单已客评。");
				return jsonEntity;
			} else if (status > Order.ORDER_STATUS_CHARGED.intValue()) {
				jsonEntity.setSuccess(false);
				jsonEntity.setMessage("错误：订单已取消或已退单。");
				return jsonEntity;
			} else if (status != Order.ORDER_STATUS_SERVICED.intValue()){
				jsonEntity.setSuccess(false);
				jsonEntity.setMessage("错误：订单状态不是[已上门]，不能完工操作。");
				return jsonEntity;
			}
			//APP完工[55]
			else if(StringUtils.isNotBlank(order.getOrderCondition().getAppCompleteType()) || status == Order.ORDER_STATUS_APP_COMPLETED){
				jsonEntity.setSuccess(false);
				jsonEntity.setMessage("错误：订单已完工操作。");
				return jsonEntity;
			}
			kefuCompleteModel.setUser(user);
			kefuCompleteModel.setOrder(order);
			kefuCompleteModel.setDataSourceId(order.getDataSourceId());
			orderService.kefuComplete(kefuCompleteModel);
		} catch (OrderException oe){
			jsonEntity.setSuccess(false);
			jsonEntity.setMessage(oe.getMessage());
		} catch (Exception e){
			jsonEntity.setSuccess(false);
			jsonEntity.setMessage(e.getMessage());
			log.error("[OrderController.saveCompleteForKefu] orderId:{}",kefuCompleteModel.getOrderId(),e);
		}
		return jsonEntity;
	}

	//endregion 客服完工

	//region 网点

	/**
	 * 派单 form (网点)
	 *
	 * @param orderId
	 */
	@RequiresPermissions(value = { "sd:order:engineeraccept" }, logical = Logical.OR)
	@RequestMapping(value = "servicepointplan", method = RequestMethod.GET)
	public String servicePointPlan(String orderId,String quarter, HttpServletRequest request, Model model) {
		Order order = new Order();
		Long lorderId = Long.valueOf(orderId);
		if (lorderId == null || lorderId <= 0) {
			addMessage(model, "派单时发生错误：订单号丢失");
			model.addAttribute("canSave", false);
			model.addAttribute("order", order);
			return "modules/sd/engineer/orderServicePointPlanForm";
		}

		order = orderService.getOrderById(lorderId,quarter, OrderUtils.OrderDataLevel.FEE, true);
		if(order == null || order.getOrderCondition() == null){
			addMessage(model, "错误：系统繁忙，读取订单失败，请重试。");
			model.addAttribute("canSave", false);
			model.addAttribute("order", new Order());
			return "modules/sd/engineer/orderServicePointPlanForm";
		}
		// 检查是否可以取消
		if (!order.canPlanOrder()) {
			addMessage(model, String.format("操作失败：订单：%s 无法派单，当前订单状态:",order.getOrderNo(),order.getOrderCondition().getStatus().getLabel()));
			model.addAttribute("canSave", false);
			model.addAttribute("order", order);
			return "modules/sd/engineer/orderServicePointPlanForm";
		}

		order.setRemarks("");
		model.addAttribute("canSave", true);
		model.addAttribute("order", order);
		return "modules/sd/engineer/orderServicePointPlanForm";
	}

	/**
	 * ajax提交网点派单信息
	 *
	 * @param order
	 */
	@RequiresPermissions("sd:order:engineeraccept")
	@ResponseBody
	@RequestMapping(value = "servicepointplan", method = RequestMethod.POST)
	public AjaxJsonEntity servicePointPlan(Order order,HttpServletRequest request, HttpServletResponse response)
	{
		AjaxJsonEntity result = new AjaxJsonEntity(true);
		if (order == null || order.getId() == null)
		{
			result.setSuccess(false);
			result.setMessage("派单时发生错误：订单号丢失");
			return result;
		}
		OrderCondition condition = order.getOrderCondition();
		if (condition==null || condition.getEngineer()==null || condition.getEngineer().getId() == null)
		{
			result.setSuccess(false);
			result.setMessage("未指派安维人员");
			return result;
		}

		try
		{
			User user = UserUtils.getUser();
			order.setCreateBy(user);
			orderService.servicePointPlanOrder(order);
			result.setSuccess(true);
			result.setMessage("派单成功");
		} catch (OrderException oe){
            result.setSuccess(false);
            result.setMessage(oe.getMessage());
        } catch (Exception e){
            result.setSuccess(false);
            result.setMessage(e.getMessage());
            log.error("[OrderController.servicePointPlan] orderId:{}",order.getId(),e);
		}
		return result;
	}

	//endregion 网点

	//region 下单

	//region item
	/**
	 * 保存新增的订单项目到缓存中,并返回现有订单所有项目 (ajax)
	 */
	@RequiresPermissions(value =
			{ "sd:order:add", "sd:order:edit" }, logical = Logical.OR)
	@ResponseBody
	@RequestMapping(value = "saveitem")
	public AjaxJsonEntity saveItem(OrderItemModel item, Model model, HttpServletRequest request, HttpServletResponse response)
	{
		response.setContentType("application/json; charset=UTF-8");
		User user = UserUtils.getUser();
		if(user==null || user.getId()==null){
			return AjaxJsonEntity.fail("登录超时，请重新登录。",null);
		}
		if (!beanValidator(model, item)) {
			if (model.containsAttribute("message")) {
				return AjaxJsonEntity.fail((String) model.asMap().get("message"), null);
			}
			return AjaxJsonEntity.fail("输入错误，请检查。", null);
		}

		if(StringUtils.isEmpty(item.getAction())){
			item.setAction("new");
		}
		// cookie
		String cachekey = OrderUtils.getUserTmpOrderCacheKey(request,response,item.getAction());
		CreateOrderModel order = (CreateOrderModel)redisUtils.get(RedisConstant.RedisDBType.REDIS_TEMP_DB,cachekey,CreateOrderModel.class);
		if (order == null) {
			order = new CreateOrderModel();
		}else{
			redisUtils.expire(RedisConstant.RedisDBType.REDIS_TEMP_DB,cachekey,OrderUtils.TMP_ORDER_EXPIRED);//延长半小时
		}
		//检查类目 2019-09-24
		if(!org.springframework.util.ObjectUtils.isEmpty(order.getItems())){
			if(order.getCategory() != null  && order.getCategory().getId() != null && !order.getCategory().getId().equals(item.getProduct().getCategory().getId())){
				return AjaxJsonEntity.fail("订单已有品类:" + order.getCategory().getName()+" 的产品，不能再添加其他品类产品",null);
			}
		}else{
			order.setCategory(item.getProduct().getCategory());
		}
		order.setCreateBy(user);
		Customer customer = order.getCustomer();
		if (customer == null || customer.getId() == null || customer.getId()<=0)
		{
			if (item.getCustomerId() != null)
			{
				customer = customerService.getFromCache(item.getCustomerId());
				order.setCustomer(customer);
			}
		}else{
			//订单无项次，已页面传递的客户为准
			if(!Objects.equals(order.getCustomer().getId(),item.getCustomerId()) &&
					(order.getItems()==null || order.getItems().size()==0)){
				customer = customerService.getFromCache(item.getCustomerId());
				order.setCustomer(customer);
			}
		}
		//检查产品-服务-价格 是否存在
		List<CustomerPrice> prices = customerService.getPricesFromCache(customer.getId());//启用的价格
		Optional<CustomerPrice> price = prices.stream().filter(t->
				Objects.equals(t.getProduct().getId(),item.getProduct().getId()) && Objects.equals(t.getServiceType().getId(),item.getServiceType().getId())).findFirst();

		if(!price.isPresent())
		{
			return AjaxJsonEntity.fail(String.format("产品:%s 未定义项目:%s 的服务价格。",item.getProduct().getName(),item.getServiceType().getName()), null);
		}

		CustomerPrice p = price.get();
		List<OrderItemModel> list = order.getItems();
		// add new add
		item.setTmpId(IdGen.uuid());
		item.setCreateDate(new Date());
		item.setItemNo(10);
		item.setFlag("lastadd");
		item.setStandPrice(p.getPrice());
		item.setDiscountPrice(p.getDiscountPrice());
		item.setDelFlag(OrderItem.DEL_FLAG_NORMAL);
		//item.product.category
		if(item.getProduct().getCategory()==null || item.getProduct().getCategory().getId()==null){
			Product prod = productService.getProductByIdFromCache(item.getProductId());
			item.setProduct(prod);
		}

		if (StringUtils.isBlank(item.getBrand())) {
			item.setBrand("");
		} else {
			item.setBrand(StringUtils.cleanHtmlTagAndSpecChars(item.getBrand()));
		}

		if (StringUtils.isBlank(item.getExpressNo())) {
			item.setExpressNo("");
		} else {
			item.setExpressNo(StringUtils.cleanHtmlTagAndSpecChars(item.getExpressNo()));
		}

		if (StringUtils.isBlank(item.getProductSpec())) {
			item.setProductSpec("");
		} else {
			item.setProductSpec(StringUtils.cleanHtmlTagAndSpecChars(item.getProductSpec()));
		}

		if (StringUtils.isBlank(item.getRemarks())) {
			item.setRemarks("");
		} else {
			item.setRemarks(StringUtils.cleanHtmlTagAndSpecChars(item.getRemarks()));
		}
		item.setBlockedCharge(p.getBlockedPrice() * item.getQty());// import
		list.add(item);

		OrderUtils.rechargeOrder(list);

		Double totalCharge = 0.00;
		Double blockedCharge = 0.00;
		Integer qty = 0;
		String isInstall = "0";
		for (OrderItemModel i : list)
		{
			if (i.getDelFlag()==OrderItemModel.DEL_FLAG_NORMAL && !i.getFlag().equalsIgnoreCase("del"))
			{
				totalCharge = totalCharge + i.getCharge();
				blockedCharge = blockedCharge + i.getBlockedCharge();
				qty = qty + i.getQty();
				if(i.getServiceType().getId().longValue() == 1){
					isInstall = "1";
				}
			}
		}

		//检查余额客户帐号财务
		//CustomerFinance finance = customerService.getFinance(customer.getId());
		//if(finance.getBalance() + (finance.getCreditFlag() == 1 ? finance.getCredit() : 0) - finance.getBlockAmount() - totalCharge - blockedCharge <0)
		//修改订单:balance(已扣减 帐号冻结费用) + credit + 修改前服务费(不包含加急费) + 修改前冻结费  - 修改后费用(已汇总了加急费) - 修改后冻结费用
		if(item.getAction().toLowerCase().startsWith("edit")){
			//+ order.getCustomerUrgentCharge()+ 加急费
			double diff = item.getBalance() + item.getCredit() + order.getExpectCharge() + order.getBlockedCharge()   - totalCharge - blockedCharge;
			if(diff < 0) {
				return AjaxJsonEntity.fail("账户余额不足...", null);
			}
		}else if(item.getBalance() + item.getCredit() - totalCharge - blockedCharge < 0){
			return AjaxJsonEntity.fail("账户余额不足...", null);
		}

		order.setTotalQty(qty);
		//切换为微服务
		//Dict paymentType = MSDictUtils.getDictByValue(String.valueOf(finance.getPaymentType()),"PaymentType");
		if(order.getOrderPaymentType() == null || order.getOrderPaymentType().getValue() == null || order.getOrderPaymentType().getValue() == "" || order.getOrderPaymentType().getValue() == "0") {
			order.setOrderPaymentType(customer.getFinance().getPaymentType());
		}
		//order.setCustomerBlockBalance(finance.getBlockAmount());//冻结金额
		//order.setCustomerBalance(finance.getBalance());// 客户余额
		//order.setCustomerCredit(finance.getCreditFlag() == 1 ? finance.getCredit() : 0.0);// 信用额度
		order.setUrgentLevel(item.getUrgentLevel());
		order.setCustomerUrgentCharge(item.getCustomerUrgentCharge());
		order.setEngineerUrgentCharge(item.getEngineerUrgentCharge());
		redisUtils.setEX(RedisConstant.RedisDBType.REDIS_TEMP_DB,cachekey,order,OrderUtils.TMP_ORDER_EXPIRED);//缓存半个小时
		////不更改缓存中数据，界面刷新用：更新应收及冻结
		order.setExpectCharge(totalCharge);//订单应收费用
		order.setBlockedCharge(blockedCharge);
		return AjaxJsonEntity.success("",order);
	}

	// ajax调用删除订单项目（更新缓存中标记,提交时才删除）,并返回所有列表（包括已删除）
	@ResponseBody
	@RequiresPermissions(value = { "sd:order:add", "sd:order:edit" }, logical = Logical.OR)
	@RequestMapping(value = "delnewitem")
	public AjaxJsonEntity delNewItem(@RequestParam String ids, double balance,double credit,@RequestParam(value = "action",required = false,defaultValue = "new") String action,HttpServletResponse response, HttpServletRequest request)
	{
		response.setContentType("application/json; charset=UTF-8");
		if (StringUtils.isBlank(ids)) {
			return AjaxJsonEntity.fail("传入参数错误", null);
		}
		try {

			//String cachekey = getUserTmpOrderCacheKey(request, response);
			String cachekey = OrderUtils.getUserTmpOrderCacheKey(request, response,action);
			CreateOrderModel order = (CreateOrderModel) redisUtils.get(RedisConstant.RedisDBType.REDIS_TEMP_DB, cachekey, CreateOrderModel.class);
			if (order == null) {
				return AjaxJsonEntity.fail("订单未在半小时内及时保存，已过期。", null);
			}
			String[] idlist = ids.split(",");
			List<OrderItemModel> list = order.getItems();
			OrderItemModel itm = null;
			for (Iterator<OrderItemModel> it = list.iterator(); it
					.hasNext(); ) {
				itm = it.next();
				for (String id : idlist) {
					if (itm.getId() != null && id.equalsIgnoreCase(itm.getId().toString())) {
						itm.setDelFlag(OrderItemModel.DEL_FLAG_DELETE);// 修改已有数据时更改删除标志
						continue;
					} else if (id.equalsIgnoreCase(itm.getTmpId())) {
						it.remove();// 新增订单时，直接删除
					}
				}
			}
			OrderUtils.rechargeOrder(list);
			Double totalCharge = 0.00;
			Double blockedCharge = 0.00;
			Integer qty = 0;
			for (OrderItemModel i : list) {
				if (i.getDelFlag() == OrderItemModel.DEL_FLAG_NORMAL && !i.getFlag().equalsIgnoreCase("del")) {
					totalCharge = totalCharge + i.getCharge();
					blockedCharge = blockedCharge + i.getBlockedCharge();
					qty = qty + i.getQty();
				}
			}

			order.setTotalQty(qty);
			if(qty ==0){
				order.setCategory(null);
			}
			// 重新读取客户余额
			//CustomerFinance finance = customerService.getFinance(order.getCustomer().getId());
			//切换为微服务
			//Dict paymentType = MSDictUtils.getDictByValue(String.valueOf(finance.getPaymentType()),"PaymentType");
			//order.setOrderPaymentType(paymentType);
			//order.setCustomerBalance(finance.getBalance());// 客户余额
			//order.setCustomerCredit(finance.getCreditFlag() == 1 ? finance.getCredit() : 0);// 信用额度
			redisUtils.setEX(RedisConstant.RedisDBType.REDIS_TEMP_DB, cachekey, order, OrderUtils.TMP_ORDER_EXPIRED);//缓存半个小时
			//不更改缓存中数据，界面刷新用：更新应收及冻结
			order.setExpectCharge(totalCharge);
			order.setBlockedCharge(blockedCharge);
			return AjaxJsonEntity.success("",order);

		} catch (Exception e)
		{
			return AjaxJsonEntity.fail("删除订单详细发生错误:" + ExceptionUtils.getMessage(e), null);
		}
	}
	//endregion item

	//region 旧版本下单

	/**
	 * 下单
	 * 客户及客服均可下单
	 * @date 2019-09-24
	 * @author Ryan Lu
	 * 1.订单保存省/市两级id(通过area来读取)
	 * 2.限制订单只能添加相同类目的订单项
	 *
	 * @param order
	 * @param request
	 * @param response
	 * @param model
	 * @return
	 */
	@RequiresPermissions(value ="sd:order:add")
	@RequestMapping(value = "form",method = RequestMethod.GET)
	public String form(CreateOrderModel order, HttpServletRequest request, HttpServletResponse response, Model model)
	{
		String viewForm = "modules/sd/orderForm";
		User user = UserUtils.getUser();
		Boolean canCreateOrder = false;//初始值
		Customer customer = null;
		List<UrgentLevel> urgentLevels = null;
		model.addAttribute("createOrderConfig",createOrderConfig);
		boolean hasPermissionShowReceive = false;
		if (SecurityUtils.getSubject().isPermitted("sd:order:showreceive")) {
			hasPermissionShowReceive = true;
		}
		model.addAttribute("showReceive",hasPermissionShowReceive);
		Boolean isCustomer = user.isCustomer();
		if(isCustomer) {
			//1.检查客户账号信息
			CustomerAccountProfile profile = user.getCustomerAccountProfile();
			if (profile == null || profile.getCustomer()==null) {
				addMessage(model, "错误：您的账户基本信息不完整，请完善基本信息。");
				model.addAttribute("order", order);
				canCreateOrder = false;
				model.addAttribute("canCreateOrder", canCreateOrder);
				return viewForm;
			}
			customer = customerService.getFromCache(profile.getCustomer().getId());
			if(customer == null ){
				addMessage(model, "错误：读取账户信息失败，请重试。");
				model.addAttribute("order", order);
				canCreateOrder = false;
				model.addAttribute("canCreateOrder", canCreateOrder);
				return viewForm;
			}else if(customer.getEffectFlag()==0){
				addMessage(model, "错误：您的账户已冻结，暂时无法下单，请联系管理员。");
				model.addAttribute("order", order);
				canCreateOrder = false;
				model.addAttribute("canCreateOrder", canCreateOrder);
				return viewForm;
			}
			user.setCustomerAccountProfile(profile);
		}
		//2.从缓存中装载上次未提交的订单
		//String key = getUserTmpOrderCacheKey(request,response);
		String key = OrderUtils.getUserTmpOrderCacheKey(request, response,"new");
		if(redisUtils.exists(RedisConstant.RedisDBType.REDIS_TEMP_DB,key)) {
			order = (CreateOrderModel) redisUtils.get(RedisConstant.RedisDBType.REDIS_TEMP_DB, key, CreateOrderModel.class);
			if (order == null) {
				order = new CreateOrderModel();
				order.setCreateBy(user);//must
			} else {
				if (customer == null) {
					customer = order.getCustomer();
				}
			}
		}
		if(order.getCategory() == null){
			if(!org.springframework.util.ObjectUtils.isEmpty(order.getItems())){
				order.setCategory(order.getItems().get(0).getProduct().getCategory());
			}else{
				order.setCategory(new ProductCategory(0L));
			}
		}
		//加急
		String urgentFlag = MSDictUtils.getDictSingleValue("OrderUrgentFlag","0");
		if(urgentFlag.equalsIgnoreCase("1")) {
			order.setUrgentFlag(1);
			urgentLevels = urgentLevelService.findAllList();
			model.addAttribute("urgentLevels", urgentLevels);
		}else{
			order.setUrgentFlag(0);
		}
		if(order.getUrgentLevel() == null){
			order.setUrgentLevel(new UrgentLevel(0l));//默认不加急
		}
		if(isCustomer) {
			if(customer != null){
				order.setCustomer(customer);
			}else {
				customer = user.getCustomerAccountProfile().getCustomer();
				order.setCustomer(user.getCustomerAccountProfile().getCustomer());
			}
		}

		if(order.getCustomer()==null) {
			//addMessage(model, "请选择客户。");
			order.setCustomerBalance(0.00);
			order.setCustomerCredit(0.00);
			model.addAttribute("order", order);
			model.addAttribute("canCreateOrder", true);
			return viewForm;
		}

		//3.检查客户账户是否可下单
		//余额+信用额度-预付金额-冻结金额>0
		//重取客户财务信息
		CustomerFinance finance = customerService.getFinance(order.getCustomer().getId());
		order.setCustomerBalance(finance.getBalance());
		order.setCustomerCredit(finance.getCreditFlag() == 1 ? finance.getCredit() : 0);
		order.setCustomerBlockBalance(finance.getBlockAmount());
		if (order.getCustomerBalance() + order.getCustomerCredit() - order.getCustomerBlockBalance() > 0) {
			canCreateOrder = true;
			if (order.getOrderPaymentType() == null || StringUtils.isBlank(order.getOrderPaymentType().getLabel())) {
				order.setOrderPaymentType(finance.getPaymentType());
			}
		}else{
			addMessage(model,"账户余额不足，请尽快充值。");
		}

		// 用于当不同的用户在同一台电脑上下单，串单的问题
		order.setCreateBy(user);
		order.setCreateDate(new Date());
		try {
			redisUtils.setEX(RedisConstant.RedisDBType.REDIS_TEMP_DB, key, order, OrderUtils.TMP_ORDER_EXPIRED);//延长半小时
		}catch (Exception e){
			log.error("[OrderController.form]redis setEx ,key:{}",key,e);
		}
		model.addAttribute("order", order);
		model.addAttribute("canCreateOrder", canCreateOrder);
		return viewForm;
	}

	/**
	 * @date 2018/09/03
	 * @version 2.1
	 * 保存下单
	 * 1.去掉sd_orderitem读写
	 * 2.sd_order_fee改为消息队列处理
	 * 3.md_customer_finance读取栏位精简，
	 *   按需读取(lock_flag,credit,credit_flag,block_amount,balance)
	 * 4.冻结流水使用消息队列处理
	 *
	 * @date 2019/09/25
	 * @author Ryan Lu
	 * 1.增加省市id保存
	 * 2.增加品类检查
	 */
	@RequiresPermissions("sd:order:add")
	@RequestMapping(value = "add",method = RequestMethod.POST)
	public String addOrder(CreateOrderModel order, HttpServletRequest request,
						   HttpServletResponse response, Model model,
						   RedirectAttributes redirectAttributes)
	{
		User user = UserUtils.getUser();
		if (user == null || user.getId()==null) {
			try {
				SecurityUtils.getSubject().logout();
			} catch (Exception e) {
				throw new RuntimeException("您的账号登录超时，请重新登录");
			}
		}
		Customer customer = customerService.getFromCache(order.getCustomer().getId());
		if(customer == null){
			addMessage(redirectAttributes,"检查客户结算方式错误。");
			return "redirect:" + Global.getAdminPath() + "/sd/order/form";
		}
		//customer缓存已保存付款方式
		CustomerFinance cacheFinance = customer.getFinance();
		if(cacheFinance == null || cacheFinance.getPaymentType() == null || StringUtils.isBlank(cacheFinance.getPaymentType().getValue())){
			addMessage(redirectAttributes,"未设置结算方式，请联系系统管理员。");
			return form(order, request, response, model);
		}

		//保存到临时缓存
		order.setCreateDate(new Date());
		order.setCreateBy(user);
		if(StringUtils.isBlank(order.getAction())){
			order.setAction("new");
		}
		String cachekey = OrderUtils.getUserTmpOrderCacheKey(request, response,order.getAction());
		/* 检查品类 2019-09-25 */
		CreateOrderModel cacheOrder = (CreateOrderModel)redisUtils.get(RedisConstant.RedisDBType.REDIS_TEMP_DB,cachekey,CreateOrderModel.class);
		if(cacheOrder != null){
			order.setItems(cacheOrder.getItems());
		}else{
			Product p;
			Long categoryId = null;
			for(OrderItemModel item:order.getItems()){
				p = productService.getProductByIdFromCache(item.getProduct().getId());
				/* 检查品类 2019-09-25 */
				if(categoryId == null){
					categoryId = p.getCategory().getId();
				}else{
					if(!categoryId.equals(p.getCategory().getId())){
						addMessage(model, "订单中产品属不同品类，无法保存。");
						return form(order, request, response, model);
					}
				}
				item.setProduct(p);
			}
		}
		redisUtils.setEX(RedisConstant.RedisDBType.REDIS_TEMP_DB,cachekey,order,OrderUtils.TMP_ORDER_EXPIRED);
		// check
		if (!beanValidator(model, order)) {
			return form(order, request, response, model);
		}

		// 如果区域为空的情况
		if (order.getArea() == null || order.getArea().getId() == null || order.getArea().getId() <= 0)
		{
			addMessage(model, "找不到指定的区域,请重新选择。");
			return form(order, request, response, model);
		}
		//检查区域type
		Area area  = areaService.getFromCache(order.getArea().getId());
		if(area == null){
			addMessage(model, "找不到指定的区域,请重新选择。");
			return form(order, request, response, model);
		}
		if(area.getType() != 4){
			addMessage(model, "区域请选择至区/县,请重新选择。");
			return form(order, request, response, model);
		}
		//加急
		String urgentFlag = MSDictUtils.getDictSingleValue("OrderUrgentFlag","0");
		UrgentLevel inUrgentLevel = order.getUrgentLevel();
		if(inUrgentLevel != null && inUrgentLevel.getId().longValue() > 0 && !urgentFlag.equalsIgnoreCase("1")){
			addMessage(model, "系统未开通加急，但传入有加急等级。");
			return form(order, request, response, model);
		}

		//金额二次检查
		CustomerFinance finance = customerService.getFinanceForAddOrder(order.getCustomer().getId());
		if(finance.getBalance() + (finance.getCreditFlag() == 1 ? finance.getCredit() : 0) -finance.getBlockAmount() - order.getExpectCharge() - order.getBlockedCharge()<0){
			addMessage(model, "账户余额不足，请尽快充值。");
			return form(order, request, response, model);
		}
		order.setOrderPaymentType(finance.getPaymentType());
		customer.setFinance(finance);
		Product product = order.getItems().get(0).getProduct();
		Long categoryId = (product.getCategory()==null || product.getCategory().getId() == null)?0L:product.getCategory().getId();
		Map<Integer,Area> areas = areaService.getAllParentsWithDistrict(order.getArea().getId());
		Area province = areas.getOrDefault(Area.TYPE_VALUE_PROVINCE,new Area(0L));
		Area city = areas.getOrDefault(Area.TYPE_VALUE_CITY,new Area(0L));
		int canRush = 0;
		int kefuType = 0;
		//vip客户，不检查突击区域 ， 街道id小于等于3也不检查突击区域 2020-06-20 Ryan
		//获取客服类型
		long subAreaId = Optional.ofNullable(order.getSubArea()).map(t->t.getId()).orElse(0l);
		/*if(customer.getVipFlag()==1){
			kefuType = OrderCondition.VIP_KEFU_TYPE;
		}else{ //有街道
			canRush = orderService.isCanRush(categoryId,city.getId(),order.getArea().getId(),subAreaId);
			kefuType = orderService.getKefuType(categoryId,city.getId(),order.getArea().getId(),subAreaId);
		}*/
		OrderKefuTypeRuleEnum orderKefuTypeRuleEnum = orderService.getKefuType(categoryId,city.getId(),order.getArea().getId(),subAreaId,customer.getVipFlag(),customer.getVip());
		kefuType = orderKefuTypeRuleEnum.getCode();
		if(kefuType==OrderCondition.RUSH_KEFU_TYPE){
			canRush = 1;
		}
		//随机客服
		User kefu = orderService.getRandomKefu(order.getCustomer().getId(),order.getArea().getId(),categoryId,kefuType,city.getId(),province.getId());
		if (kefu == null) {
			//无客服
			String tip = orderService.noFindKefuTip(user,customer,categoryId,kefuType,order.getArea().getId(),city.getId(),province.getId());
			model.addAttribute("noKefuFlag",1);
			model.addAttribute("tip",tip);
			return form(order, request, response, model);
		}
		String orderNo = orderService.getNewOrderNo();
		if(StringUtils.isBlank(orderNo)){
			addMessage(model, "生成订单号失败，请重试");
			return form(order, request, response, model);
		}

		customer.setFinance(finance);
		order.setCustomer(customer);//*

		//重新计算及检查订单项及金额
		int itemCount = 0;
		int totalQty =0;//下单的产品数量
		OrderItemModel item = null;
		int itemNo = 0;
		Product p;
		List<OrderItemModel> items = order.getItems();
		OrderUtils.rechargeOrder(items);//重新计价
		Double totalCharge = 0.00;
		Double blockedCharge = 0.00;

		Set<String> pids = Sets.newHashSet();//产品
		Set<String> sids = Sets.newHashSet();//服务项目
		Integer hasSet = 0;
		List<Dict> expressCompanys = MSDictUtils.getDictList("express_type");//切换为微服务
		Dict expressCompany;
		int orderServiceType = 0;
		// 根据服务类型中工单类型判断
		Map<Long,ServiceType> serviceTypeMap = serviceTypeService.getAllServiceTypeMap();
		StringBuilder content = new StringBuilder();
		content.append("师傅，在您附近有一张  ");
		categoryId = null;
		ServiceType serviceType;
		for (int j=0,size=items.size();j<size;j++) {
			item = items.get(j);
			if (item == null || !item.getDelFlag().equals(OrderCondition.DEL_FLAG_NORMAL) || item.getFlag().equalsIgnoreCase("del")) {
				continue;
			}
			if (item.getProduct() == null || item.getProduct().getId() == null) {
				addMessage(model, "选择产品。");
				return form(order, request, response, model);
			} else if (item.getServiceType() == null || item.getServiceType().getId() == null) {
				addMessage(model, "选择服务类型。");
				return form(order, request, response, model);
			}
			item.setItemNo(itemNo += 10);
			itemCount++;
			p = item.getProduct();
			if(categoryId == null){
				categoryId = p.getCategory().getId();
			}
			item.setProduct(p);
			if (p.getSetFlag() == 1) {
				hasSet = 1;
			}
			pids.add(String.format(",%s,", p.getId()));
			sids.add(String.format(",%s,", item.getServiceType().getId()));
			//工单类型按服务项目设定为准
			serviceType = serviceTypeMap.get(item.getServiceType().getId());
			if(serviceType == null){
				addMessage(model, "确认服务项目的工单类型错误，请重试");
				return form(order, request, response, model);
			}
			//除维修(2)外，值最大的优先
			if(orderServiceType == 0){
				orderServiceType = serviceType.getOrderServiceType();
			}else if (serviceType.getOrderServiceType() == 2){
				orderServiceType = serviceType.getOrderServiceType();
			}else if(orderServiceType < serviceType.getOrderServiceType()){
				orderServiceType = serviceType.getOrderServiceType();
			}
		 	/*
            if (installTypes.contains(item.getServiceType().getId())) {
                orderServiceType = 1;//安装
            } else {
                orderServiceType = 2;//维修
            }*/
			final Dict company = item.getExpressCompany();
			if (expressCompanys != null && expressCompanys.size() > 0) {
				expressCompany = expressCompanys.stream().filter(t -> t.getValue().equalsIgnoreCase(company.getValue())).findFirst().orElse(item.getExpressCompany());
				item.setExpressCompany(expressCompany);
			}

			totalQty = totalQty + item.getQty();
			totalCharge = totalCharge + item.getCharge();
			blockedCharge = blockedCharge + item.getBlockedCharge();
			content.append(item.getServiceType().getName())
					.append(item.getBrand())
					.append(item.getProduct().getName());
		}
		content.append("的工单，请尽快登陆APP接单~");

		if (itemCount == 0)
		{
			addMessage(model, "订单无服务项目，请添加。");
			return form(order, request, response, model);
		}

		if(orderServiceType==0){
			orderServiceType = 2;//维修
		}
		order.setExpectCharge(totalCharge);//*
		order.setBlockedCharge(blockedCharge);//*
		order.setTotalQty(totalQty);//*
		order.setCreateDate(new Date());//*
		order.setCreateBy(user);//*
		//转换后的订单实例
		Order o;
		try
		{
			//加急
			UrgentLevel urgentLevel = order.getUrgentLevel();
			if(urgentLevel != null && urgentLevel.getId().longValue()>0){
				HashMap<String,Object> urgentChargeMap = orderService.getCustomerUrgentCharge(order.getCustomer().getId(),order.getArea().getId(),urgentLevel.getId(),"false");
				if(urgentChargeMap == null || urgentChargeMap.size() == 0){
					addMessage(model, "确认加急费失败，请重试");
					return form(order, request, response, model);
				}
				double chargeIn = (double)urgentChargeMap.get("chargeIn");
				order.setCustomerUrgentCharge(chargeIn);//下单时写入，在客评时，判断是否符合加急，不符合更改为0
				order.setEngineerUrgentCharge((double)urgentChargeMap.get("chargeOut"));
			}else{
				order.setUrgentLevel(new UrgentLevel(0l));//默认不加急
				order.setCustomerUrgentCharge(0.0);
				order.setEngineerUrgentCharge(0.0);
			}

			String quarter = QuarterUtils.getSeasonQuarter(order.getCreateDate());//分片
			order.setQuarter(quarter);
			Long oid = SeqUtils.NextIDValue(SeqUtils.TableName.Order);//production
			order.setId(oid);

			order.setOrderNo(orderNo);
			order.setCustomerOwner(user.getName());//下单负责人 2018/08/03
			//转换
			o = OrderUtils.toOrder(order);
			o.getOrderCondition().setProvinceId(province.getId());
			o.getOrderCondition().setCityId(city.getId());
			o.getOrderCondition().setCanRush(canRush);
			o.getOrderCondition().setKefuType(kefuType);
			/* 针对手动选择区域，重新获得经纬度座标 2019-04-15 */
			if(order.getLongitude() == 0 || order.getLatitude() == 0){
				String address = MessageFormat.format("{0} {1}",area.getFullName(),o.getOrderCondition().getAddress());
				String[] rstArrays = AreaUtils.getLocation(address);
				if(rstArrays != null && rstArrays.length == 2 ){
					o.getOrderLocation().setLongitude(StringUtils.toDouble(rstArrays[0]));
					o.getOrderLocation().setLatitude(StringUtils.toDouble(rstArrays[1]));
				}
			}
			/* 再次检查重单 2018/04/19 */
			if(StringUtils.isBlank(o.getRepeateNo())) {
				//使用新的重单检查方法，从缓存中检查
				String repeateOrderNo = orderService.getRepeateOrderNo(order.getCustomer().getId(),order.getPhone1());
				if (StringUtils.isNotBlank(repeateOrderNo)) {
					o.setRepeateNo(repeateOrderNo);
				}
			}
			Dict status;
			if(user.isSystemUser()){
				//不需审核
				status = MSDictUtils.getDictByValue(String.valueOf(Order.ORDER_STATUS_APPROVED), "order_status");//切换为微服务
			}else if(user.isCustomer() && user.getCustomerAccountProfile().getOrderApproveFlag()==0) {
				//不需审核
				status = MSDictUtils.getDictByValue(String.valueOf(Order.ORDER_STATUS_APPROVED), "order_status");//切换为微服务
			}else {
				status = MSDictUtils.getDictByValue(String.valueOf(Order.ORDER_STATUS_NEW), "order_status");//切换为微服务
			}
			o.getOrderCondition().setTotalQty(totalQty);
			o.getOrderCondition().setStatus(status);
			o.getOrderCondition().setKefu(kefu);
			o.getOrderCondition().setProductCategoryId(categoryId);
			o.getOrderCondition().setHasSet(hasSet);
			o.getOrderCondition().setProductIds(pids.stream().collect(Collectors.joining(",")).replace(",,,",",,"));
			o.getOrderCondition().setServiceTypes(sids.stream().collect(Collectors.joining(",")).replace(",,,",",,"));
			o.getOrderCondition().setOrderServiceType(orderServiceType);//12-06

			Dict orderType = MSDictUtils.getDictByValue(String.valueOf(Order.ORDER_ORDERTYPE_DSXD),"order_type");//切换为微服务
			if(orderType == null){
				orderType = new Dict(Order.ORDER_ORDERTYPE_DSXD,"电商下单");
			}
			o.setOrderType(orderType);// 电商下单

			//如果地址中出现null 字符串，则会引起 API 返回JSON数据到APP 导致格式不匹配,所以加字符串判断
			if(o.getOrderCondition().getAddress()!=null)
			{
				String address=o.getOrderCondition().getAddress().replace("null", "");
				o.getOrderCondition().setAddress(address);
			}
			String dailyLogKey = String.format(RedisConstant.SD_CREATE_ORDER_LOG,DateUtils.getDate());
			boolean saveSuccess = true;
			//订单保存
			try {//成功，执行后面操作
				orderService.createOrder_v2_1(o,null);
			}catch (Exception e){
				saveSuccess = false;
				try {
					log.error("下单保存数据库失败,json:{}",gsonRedisSerializer.toJson(o),e);
				}catch (Exception e1){
					log.error("下单保存数据库失败,userId:{} ,userName:{} ,mobile:{}",user.getId(),order.getUserName(),order.getServicePhone(),e);
				}
				try {
					//订单号返还redis列表
					if(StringUtils.isNoneBlank(orderNo)){
						SeqUtils.reputOrderNo(o.getCreateDate(),orderNo);
					}
				}catch (Exception e1){
					log.error("返还订单号失败,orderNo:{}",orderNo,e1);
				}
				addMessage(model,"下单失败,请重试。" + ExceptionUtils.getRootCauseMessage(e));
				return form(order, request, response, model);
			}finally {
				//下单log，供每日核对，防止漏单
				//先加到redis,不成功记录到sys_log
				try{
					double score = Double.valueOf(orderNo.substring(1));
					if(saveSuccess) {
						redisUtils.zAdd(RedisConstant.RedisDBType.REDIS_TEMP_DB, dailyLogKey, orderNo, score, OrderUtils.REDIS_CREATE_LOG_EXPIRED);
					}else{
						redisUtils.zAdd(RedisConstant.RedisDBType.REDIS_TEMP_DB, dailyLogKey, CreateOrderModelAdapter.getInstance().toJson(order), score, OrderUtils.REDIS_CREATE_LOG_EXPIRED);
					}
				}catch (Exception e){
					log.error("[OrderController.addOrder] push orderNo:{} to dailyLogKey:{}",orderNo,dailyLogKey,e);
				}
			}

			if (true == saveSuccess) {
				o.setAppMessage(content.toString());
				orderMQService.sendCreateOrderMessage(o,"addOrder");

				// 更新临时订单缓存
				try {
					order = new CreateOrderModel();
					order.setCustomer(o.getOrderCondition().getCustomer());
					redisUtils.setEX(RedisConstant.RedisDBType.REDIS_TEMP_DB, cachekey, order, OrderUtils.TMP_ORDER_EXPIRED);
				} catch (Exception e) {
				}
			}
			addMessage(model, "新建订单:".concat(orderNo).concat(" 成功"));
			return form(order, request, response, model);
		} catch (Exception e)
		{
			String json = new String();
			try{
				json = gsonRedisSerializer.toJson(order);
			}catch(Exception e1) {}
			log.error("下单失败,order:{}",json,e);
			addMessage(model, "下单失败,请重试。");
			return form(order, request, response, model);
		}
	}

	//region 修改订单

	/**
	 * 修改订单
	 */
	@RequiresPermissions(value = { "sd:order:edit", "sd:order:add" }, logical = Logical.OR)
	@RequestMapping(value = "edit")
	public String editForm(CreateOrderModel order, HttpServletRequest request, HttpServletResponse response, Model model)
	{
		String viewForm = "modules/sd/orderEditForm";
		boolean hasPermissionShowReceive = false;
		if (SecurityUtils.getSubject().isPermitted("sd:order:showreceive")) {
			hasPermissionShowReceive = true;
		}
		model.addAttribute("showReceive",hasPermissionShowReceive);
		String cid = request.getParameter("id");
		Long id = Long.parseLong(cid);
		if(id == null || id<=0){
			addMessage(model,"错误：订单参数为空");
			model.addAttribute("canCreateOrder", false);
			return viewForm;
		}

		Order o;
		User user = UserUtils.getUser();
		String cachekey;

		o = orderService.getOrderById(id,order.getQuarter(),OrderUtils.OrderDataLevel.FEE,true);
		if(o == null || o.getOrderCondition() == null){
			addMessage(model, "错误：系统繁忙，读取订单失败，请重试");
			model.addAttribute("order", order);
			model.addAttribute("canCreateOrder", "false");
			return viewForm;
		}
		order = OrderUtils.toCreateOrderModel(o);
		//审核修改跳转到审核页面用
		if(request.getParameter("actiontype")!=null)
		{
			String actionType = request.getParameter("actiontype");
			order.setActionType(actionType);
		}
		//根据品类区分修改方式(现有方式还是灯饰下单方式)
		OrderCondition condition = o.getOrderCondition();
		Long productCategoryId = condition.getProductCategoryId();
		if(productCategoryId != null && CollectionUtils.isNotEmpty(createOrderConfig.getSecondType().getCategories()) && createOrderConfig.getSecondType().getCategories().contains(productCategoryId)){
			//灯饰下单
			//order.setCreateBy(user);
			order.setOrder(o);
			return secondEditForm(order,request,response,model);
		}
		int statusValue = condition.getStatusValue();
		if (user.isCustomer() && !OrderUtils.canEdit(statusValue))
		{
			//客户，已接单订单不允许修改
			addMessage(model, "订单:".concat(order.getOrderNo()).concat(" 不允许修改订单，请刷新，并确认订单具体状态及明细。"));
			model.addAttribute("order", order);
			model.addAttribute("canCreateOrder", "false");
			return viewForm;
		}else if(user.isSystemUser()
				&& (condition.getPartsFlag() == 1 || condition.getServiceTimes()>0 || statusValue >= Order.ORDER_STATUS_RETURNING)
		){
			//后台帐号，上门服务前(含上门服务)有配件和上门记录的订单不允许修改
			addMessage(model, "订单:".concat(order.getOrderNo()).concat(" 不允许修改订单，请刷新，并确认订单是否已有上门服务记录或配件申请。"));
			model.addAttribute("order", order);
			model.addAttribute("canCreateOrder", "false");
			return viewForm;
		}

		//检查是否有未完成突击单
		//有，不允许修改
		if(Sets.newHashSet(Integer.valueOf(1),Integer.valueOf(3)).contains(condition.getRushOrderFlag())){
			addMessage(model, "订单突击正在处理中，不允许修改订单信息！");
			model.addAttribute("order", order);
			model.addAttribute("canCreateOrder", "false");
			return viewForm;
		}
		Long customerId = order.getCustomer().getId();
		//models、brand、b2bProductCodes 2018/1/7
		List<Long> productIds = order.getItems().stream().map(OrderItem::getProductId).collect(Collectors.toList());
		Map<Long, ThreeTuple<List<Brand>, List<CustomerProductModel>, List<String>>> productPropertiesMap = orderEditFormService.getProductPropertyEntris(customerId,StringUtils.toInteger(order.getDataSource().getValue()), productIds);
		if (!productPropertiesMap.isEmpty()) {
			ThreeTuple<List<Brand>, List<CustomerProductModel>, List<String>> productProperties;
			for (OrderItemModel item : order.getItems()) {
				productProperties = productPropertiesMap.get(item.getProductId());
				if (productProperties != null) {
					item.setBrands(productProperties.getAElement());
					item.setModels(productProperties.getBElement());
					item.setB2bProductCodes(productProperties.getCElement());
				}
			}
		}
		if(!org.springframework.util.ObjectUtils.isEmpty(order.getItems())){
			order.setCategory(order.getItems().get(0).getProduct().getCategory());
		}
		//客户
		CustomerFinance finance = customerService.getFinance(customerId);
		order.setCustomerBalance(finance.getBalance());// 客户余额
		order.setCustomerBlockBalance(finance.getBlockAmount());//冻结金额
		order.setCustomerCredit(finance.getCreditFlag() == 1 ? finance.getCredit() : 0);// 客户信用额度
		order.setCreateBy(user);//更改为当前用户
		//判断4级地址是否属于3级地址下的街道
		if(condition.getSubArea()!=null && condition.getSubArea().getId()>3){
			Area subArea = areaService.getFromCache(condition.getSubArea().getId());
			if(subArea!=null){
               if(!subArea.getParent().getId().equals(condition.getArea().getId())){
				   condition.getSubArea().setId(1L);
			   }
			}
		}
		//cachekey = getUserEditOrderCacheKey(request, response);
		cachekey = OrderUtils.getUserTmpOrderCacheKey(request, response,"edit");
		try {
			redisUtils.setEX(RedisConstant.RedisDBType.REDIS_TEMP_DB, cachekey, order, OrderUtils.TMP_ORDER_EXPIRED);
		}catch (Exception e){
			try{
				redisUtils.setEX(RedisConstant.RedisDBType.REDIS_TEMP_DB, cachekey, order, OrderUtils.TMP_ORDER_EXPIRED);
			}catch (Exception e1){}
		}
		model.addAttribute("order", order);
		//加急，下单选择了加急或者加急开关开启
        /*
		String urgentFlag = MSDictUtils.getDictSingleValue("OrderUrgentFlag","0");
		if(urgentFlag.equalsIgnoreCase("1") || (order.getUrgentLevel() != null && order.getUrgentLevel().getId() != null && order.getUrgentLevel().getId().longValue()>0 )){
		    order.setUrgentFlag(1);
			List<UrgentLevel> urgentLevels = urgentLevelService.findAllList();
			model.addAttribute("urgentLevels",urgentLevels);
		} else {
            order.setUrgentFlag(0);
        }*/
		model.addAttribute("canCreateOrder", "true");
		return viewForm;
	}

	/**
	 * 保存修改后的订单
	 *
	 * @param order
	 * @param request
	 * @param model
	 * @param redirectAttributes
	 * @return
	 */
	@RequiresPermissions("sd:order:edit")
	@RequestMapping(value = "save",method = RequestMethod.POST)
	public String saveOrder(CreateOrderModel order, HttpServletRequest request,
							HttpServletResponse response, Model model,
							RedirectAttributes redirectAttributes)
	{
		User user = UserUtils.getUser();
		if (user == null || user.getId()==null) {
			try {
				SecurityUtils.getSubject().logout();
			} catch (Exception e) {
				addMessage(model, "您的账号登录超时，请重新登录。");
				return editForm(order, request, response, model);
			}
		}

		//检查订单是否被修改
		if(!orderService.orderVersionValid(order.getId(),order.getVersion())){
			addMessage(model, "订单已被其它帐号操作，请刷新待审核列表。");
			return editForm(order, request, response, model);
		}

		//保存到缓存
		String cachekey = getUserEditOrderCacheKey(request, response);
		try {
			redisUtils.setEX(RedisConstant.RedisDBType.REDIS_TEMP_DB, cachekey, order, OrderUtils.TMP_ORDER_EXPIRED);
		} catch (Exception e){
			log.error("[OrderController.saveOrder] key:{}",cachekey,e);
		}

		// check
		if (!beanValidator(model, order)) {
			return editForm(order,request, response, model);
		}

		// 如果区域为空的情况
		if (order.getArea() == null || order.getArea().getId() == null || order.getArea().getId() == 0) {
			addMessage(model, "找不到指定的区域,请重新选择。");
			return editForm(order, request, response, model);
		}
		Order orgOrder;
		try {
			orgOrder = orderService.getOrderById(order.getId(), order.getQuarter(), OrderUtils.OrderDataLevel.CONDITION, true);
		}catch (Exception e){
			addMessage(model, "读取修改前订单服务项目错误，请重试");
			return editForm(order, request, response, model);
		}
		if(orgOrder == null){
			addMessage(model, "错误：系统繁忙，读取订单失败，请重试。");
			return editForm(order, request, response, model);
		}
		OrderCondition orgOrderCondition = orgOrder.getOrderCondition();
		if(orgOrderCondition == null || orgOrderCondition.getStatus() == null || orgOrderCondition.getStatus().getIntValue() == 0){
			addMessage(model, "确认订单状态错误，请重试。");
			return editForm(order, request, response, model);
		}
		if(orgOrderCondition.getStatus().getIntValue() > Order.ORDER_STATUS_PLANNED){
			addMessage(model, "错误：订单已开始上门服务，不允许修改。");
			return editForm(order, request, response, model);
		}

		// 订单项次处理
		List<OrderItemModel> list = order.getItems();
		List<CustomerPrice> prices = customerService.getPricesFromCache(order.getCustomer().getId());
		Optional<CustomerPrice> price ;
		Product p;
		Set<String> pids = Sets.newHashSet();//产品
		Set<String> sids = Sets.newHashSet();//服务项目
		Integer hasSet = 0;
		int orderServiceType = 0;
		// 根据服务类型中工单类型判断
		Map<Long,ServiceType> serviceTypeMap = serviceTypeService.getAllServiceTypeMap();
		/*调用微服务获取服务类型,只返回id和code start 2019-10-11
		Map<Long,String> map = serviceTypeService.findIdsAndCodes();
		Long serviceId = map.entrySet().stream().filter(t->t.getValue().equalsIgnoreCase("II")).map(t->t.getKey()).findFirst().orElse(null);
		ServiceType installService;
		if(serviceId!=null && serviceId>0){
			installService = new ServiceType(serviceId);
		}else{
			installService = new ServiceType(0l,"II","安装");
		}
		// end
		 */
		Long categoryId = null;//品类
		ServiceType serviceType;
		// 移除产品为空的项目,并读取最新价格
		for (Iterator<OrderItemModel> it = list.iterator(); it.hasNext();)
		{
			OrderItem item = it.next();
			if(item == null || item.getProduct() == null || item.getServiceType() == null){
				it.remove();
				continue;
			}
			//价格
			price = prices.stream().filter(t->
					Objects.equals(t.getProduct().getId(),item.getProduct().getId()) && Objects.equals(t.getServiceType().getId(),item.getServiceType().getId())).findFirst();
			if(!price.isPresent()){
				addMessage(model, String.format("产品:%s 未定义服务项目:%s 的服务价格",item.getProduct().getName(),item.getServiceType().getName()));
				return editForm(order, request, response, model);
			}
			item.setStandPrice(price.get().getPrice());
			item.setDiscountPrice(price.get().getDiscountPrice());
			p = productService.getProductByIdFromCache(item.getProduct().getId());
			/* 检查品类 2019-09-25 */
			if(categoryId == null){
				categoryId = p.getCategory().getId();
			}else{
				if(!categoryId.equals(p.getCategory().getId())){
					addMessage(model, "订单中产品属不同品类，无法保存。");
					return form(order, request, response, model);
				}
			}
			item.setProduct(p);
			pids.add(String.format(",%s,",p.getId()));
			sids.add(String.format(",%s,",item.getServiceType().getId()));
			//工单类型按服务项目设定为准
			serviceType = serviceTypeMap.get(item.getServiceType().getId());
			if(serviceType == null){
				addMessage(model, "确认服务项目的工单类型错误，请重试");
				return form(order, request, response, model);
			}
			//除维修(2)外，值最大的优先
			if(orderServiceType == 0){
				orderServiceType = serviceType.getOrderServiceType();
			}else if (serviceType.getOrderServiceType() == 2){
				orderServiceType = serviceType.getOrderServiceType();
			}else if(orderServiceType < serviceType.getOrderServiceType()){
				orderServiceType = serviceType.getOrderServiceType();
			}
			/*
			if(item.getServiceType().getId().longValue() != installService.getId().longValue()){
				orderServiceType = 2;
			}else if(0 == orderServiceType ){
				orderServiceType = 1;
			}*/
		}
		if(orderServiceType==0){
			orderServiceType = 2;
		}

		//重新计算价格
		OrderUtils.rechargeOrder(list);
		Double totalCharge = 0.00;
		Double blockedCharge = 0.00;
		Integer qty = 0;
		for (OrderItemModel item : list) {
			totalCharge = totalCharge + item.getCharge();
			blockedCharge = blockedCharge + item.getBlockedCharge();
			qty = qty + item.getQty();
		}

		if (qty == 0)
		{
			addMessage(model, "订单无明细项目，请添加。");
			return editForm(order, request, response, model);
		}
		order.setExpectCharge(totalCharge);
		order.setBlockedCharge(blockedCharge);

		//原订单费用
		OrderFee fee = orderService.getOrderFeeById(order.getId(),order.getQuarter(),false);
		//加急，不变更
		order.setUrgentLevel(orgOrderCondition.getUrgentLevel());
		order.setCustomerUrgentCharge(fee.getCustomerUrgentCharge());
		order.setEngineerUrgentCharge(fee.getEngineerUrgentCharge());
		//客户账户余额检查
		CustomerFinance cfee = customerService.getFinanceForAddOrder(order.getCustomer().getId());
		Double credit = cfee.getCreditFlag()==0?0d:cfee.getCredit();
		//客户账户余额 + 信用额度 + 此单费用(=此单原金额 + 此单原冻结金额) - 现客户总冻结金额 - 修改后单费用(=当前单金额 + 加急费 + 当前单冻结金额)
		if(cfee.getBalance() + credit + fee.getExpectCharge()+fee.getBlockedCharge() - cfee.getBlockAmount() - totalCharge - order.getCustomerUrgentCharge() - blockedCharge <0  ) {
			addMessage(model, "账户余额不足...");
			return editForm(order, request, response, model);
		}

		StringBuffer orgItemString = new StringBuffer(200);
		OrderItem orgItem;
		List<OrderItem> orgItems = orgOrder.getItems();
		for(int i=0,size=orgItems.size();i<size;i++){
			orgItem = orgItems.get(i);
			orgItemString.append(String.valueOf(i+1))
					.append(".")
					.append("产品：").append(orgItem.getProduct().getName()).append(" ")
					.append("品牌:").append(orgItem.getBrand()).append(" ")
					.append("服务:").append(orgItem.getServiceType().getName()).append(" ")
					.append("数量:").append(orgItem.getQty())
					.append(" ");
		}
		Order o = OrderUtils.toOrder(order);
		try
		{
			//如果地址中出现null 字符串，则会引起 API 返回JSON数据到APP 导致格式不匹配,所以加字符串判断
			if(o.getOrderCondition().getAddress()!=null)
			{
				String address=o.getOrderCondition().getAddress().replace("null", "");
				o.getOrderCondition().setAddress(address);
			}
			o.preUpdate();
			o.getOrderCondition().setStatus(orgOrderCondition.getStatus());//edited by zhoucy, 工单修改后其状态保持不变
			o.getOrderCondition().setTotalQty(qty);//下单时的产品数量
			o.getOrderCondition().setProductCategoryId(categoryId);//品类 2019-09-25
			o.getOrderCondition().setHasSet(hasSet);
			o.getOrderCondition().setProductIds(pids.stream().collect(Collectors.joining(",")).replace(",,,",",,"));
			o.getOrderCondition().setServiceTypes(sids.stream().collect(Collectors.joining(",")).replace(",,,",",,"));
			o.getOrderCondition().setOrderServiceType(orderServiceType);//12-06
			//quarter
			o.setQuarter(order.getQuarter());
			/* 省/市id 2019-09-25 */
			Map<Integer,Area> areas = areaService.getAllParentsWithDistrict(order.getArea().getId());
			Area province = areas.getOrDefault(Area.TYPE_VALUE_PROVINCE,new Area(0L));
			Area city = areas.getOrDefault(Area.TYPE_VALUE_CITY,new Area(0L));
			o.getOrderCondition().setProvinceId(province.getId());
			o.getOrderCondition().setCityId(city.getId());
			//检查客服类型 2020-12-8
			Customer customer = customerService.getFromCache(order.getCustomer().getId());
			Area subArea = o.getOrderCondition().getSubArea();
			int kefuType = 0;
			int canRush = 0;
			/*if(customer!=null && customer.getVipFlag()==1){
				kefuType = OrderCondition.VIP_KEFU_TYPE;
			}else{
				kefuType = orderService.getKefuType(categoryId,city.getId(),order.getArea().getId(),subArea.getId());
			}*/
			OrderKefuTypeRuleEnum orderKefuTypeRuleEnum = orderService.getKefuType(categoryId,city.getId(),order.getArea().getId(),subArea.getId(),customer.getVipFlag(),customer.getVip());
			kefuType = orderKefuTypeRuleEnum.getCode();
			o.getOrderCondition().setKefuType(kefuType);
			if(kefuType==OrderKefuTypeRuleEnum.ORDER_RUSH.getCode()){
				canRush = 1;
			}
			o.getOrderCondition().setCanRush(canRush);
			User kefu = orderService.getRandomKefu(customer.getId(),order.getArea().getId(),categoryId,kefuType,city.getId(),province.getId());
			if(kefu==null){
				//addMessage(model, "此区域暂未分配"+failReason+"，暂时无法下单。请联系管理员：18772732342，QQ:572202493");
                String tip = orderService.noFindKefuTip(user,customer,categoryId,kefuType,order.getArea().getId(),city.getId(),province.getId());
                model.addAttribute("noKefuFlag",1);
                model.addAttribute("tip",tip);
				return editForm(order, request, response, model);
			}
			orderService.updateOrder(o,fee,orgItemString.toString());//修改
			// 清空临时订单缓存
			try {
				redisUtils.remove(RedisConstant.RedisDBType.REDIS_SD_DB, cachekey);
			}catch (Exception e){
				log.error("[OrderController.saveOrder] remove redis,key:{}",cachekey,e);
			}
			try {
				CookieUtils.setCookie(response, OrderUtils.ORDER_EDIT_COOKIE_KEY, "", 0);// cookie失效
			}catch (Exception e){
				log.error("[OrderController.saveOrder] setCookie,key:{}",OrderUtils.ORDER_EDIT_COOKIE_KEY,e);
				try{
					CookieUtils.setCookie(response, OrderUtils.ORDER_EDIT_COOKIE_KEY, "", 0);// cookie失效
				}catch (Exception e1){
				}
			}

			addMessage(model, "修改订单:".concat(o.getOrderNo()).concat(" 成功"));
			if(order.getActionType().equalsIgnoreCase("approve")) {
				return "redirect:" + Global.getAdminPath() + "/sd/order/customer/approvelist?repage=true";//订单审核
			}
			if(user.isCustomer() || order.getActionType().startsWith("cust_")){//
				return "redirect:" + Global.getAdminPath() + "/sd/order/customerNew/" + order.getActionType().replace("cust_","") + "list?repage=true";//我的订单
			}
			if (user.isSaleman() || order.getActionType().startsWith("sales_")) {
				return "redirect:" + Global.getAdminPath() + "/sd/order/salesNew/" + order.getActionType().replace("sales_","") + "list?repage=true";//我的订单
				//return "redirect:" + Global.getAdminPath() + "/sd/order/salesNew/list?repage=true";//我的订单
			}
			return "redirect:" + Global.getAdminPath() + "/sd/order/customerNew/list?repage=true";//我的订单
			//return editForm(order, request, response, model);
		} catch (OrderException oe) {
			addMessage(model, oe.getMessage());
			return editForm(order, request, response, model);
		} catch (Exception e) {
			log.error("[OrderController.saveOrder] orderNo:{}",order.getOrderNo(),e);
			addMessage(model, e.getMessage());
			return editForm(order, request, response, model);
		}
	}

	//endregion 修改订单

	//endregion 旧版本下单

	//region 公共方法
	/**
	 * 获取临时订单的缓存key,使用cookie保存，不存在则产生新的，并返回
	 * @param request
	 * @param response
	 * @return

	private String getUserTmpOrderCacheKey(HttpServletRequest request,
										   HttpServletResponse response)
	{
		String orderkey = CookieUtils.getCookie(request, OrderUtils.ORDER_CREATE_COOKIE_KEY);
		if (StringUtils.isBlank(orderkey))
		{
			orderkey = "tmp:order:" + IdGen.randomBase62(10);
			CookieUtils.setCookie(response, OrderUtils.ORDER_CREATE_COOKIE_KEY,
					orderkey, 60 * 60 * 24 * 7);
		}
		return orderkey;
	}*/

	/**
	 * 获取临时订单的缓存key,使用cookie保存，不存在则产生新的，并返回
	 * @param request
	 * @param response
	 * @return
	 */
	private String getUserEditOrderCacheKey(HttpServletRequest request,
											HttpServletResponse response)
	{
		String orderkey = CookieUtils.getCookie(request, OrderUtils.ORDER_EDIT_COOKIE_KEY);
		if (StringUtils.isBlank(orderkey))
		{
			orderkey = "edit:order:" + IdGen.randomBase62(10);
			CookieUtils.setCookie(response, OrderUtils.ORDER_EDIT_COOKIE_KEY,
					orderkey, 60 * 60 * 24 * 7);
		}
		return orderkey;
	}

	private String returnViewForm(Map<String,Object> attributes,Model model,String viewForm,String msg){
		if(org.apache.commons.lang3.StringUtils.isNotBlank(msg)){
			addMessage(model, msg);
		}
		if(attributes != null && !attributes.isEmpty()){
			model.addAllAttributes(attributes);
		}
		return viewForm;
	}

	//endregion 公共方法

	//region 新版本下单

	/**
	 * 新版本下单
	 * @date 2019-09-24
	 * @author Ryan Lu
	 * 1.可选择分类，二级分类，规格
	 * 2.可上传产品
	 */
	@RequiresPermissions(value ="sd:order:add")
	@RequestMapping(value = "second/createForm",method = RequestMethod.GET)
	public String secondCreateForm(CreateOrderModel order, HttpServletRequest request,
					   HttpServletResponse response, Model model)
	{
		String viewForm = "modules/sd/secondOrder/createOrderForm";
		User user = UserUtils.getUser();
		Boolean canCreateOrder = false;//初始值
		Customer customer = null;
		List<UrgentLevel> urgentLevels = null;
		model.addAttribute("createOrderConfig",createOrderConfig);
		boolean hasPermissionShowReceive = false;
		if (SecurityUtils.getSubject().isPermitted("sd:order:showreceive")) {
			hasPermissionShowReceive = true;
		}
		model.addAttribute("showReceive",hasPermissionShowReceive);
		Boolean isCustomer = user.isCustomer();
		if(isCustomer) {
			//1.检查客户账号信息
			CustomerAccountProfile profile = user.getCustomerAccountProfile();
			if (profile == null || profile.getCustomer()==null) {
				addMessage(model, "错误：您的账户基本信息不完整，请完善基本信息。");
				model.addAttribute("order", order);
				canCreateOrder = false;
				model.addAttribute("canCreateOrder", canCreateOrder);
				return viewForm;
			}
			customer = customerService.getFromCache(profile.getCustomer().getId());
			if(customer == null ){
				addMessage(model, "错误：读取账户信息失败，请重试。");
				model.addAttribute("order", order);
				canCreateOrder = false;
				model.addAttribute("canCreateOrder", canCreateOrder);
				return viewForm;
			}else if(customer.getEffectFlag()==0){
				addMessage(model, "错误：您的账户已冻结，暂时无法下单，请联系管理员。");
				model.addAttribute("order", order);
				canCreateOrder = false;
				model.addAttribute("canCreateOrder", canCreateOrder);
				return viewForm;
			}
			user.setCustomerAccountProfile(profile);
		}
		//2.从缓存中装载上次未提交的订单
		//String key = getUserTmpOrderCacheKey(request,response);
		String key = OrderUtils.getUserTmpOrderCacheKey(request,response,"newv2");
		if(redisUtils.exists(RedisConstant.RedisDBType.REDIS_TEMP_DB,key)) {
			order = (CreateOrderModel) redisUtils.get(RedisConstant.RedisDBType.REDIS_TEMP_DB, key, CreateOrderModel.class);
			if (order == null) {
				order = new CreateOrderModel();
				order.setCreateBy(user);//must
			} else {
				if (customer == null) {
					customer = order.getCustomer();
				}
			}
		}
		if(order.getCategory() == null){
			if(!org.springframework.util.ObjectUtils.isEmpty(order.getItems())){
				order.setCategory(order.getItems().get(0).getProduct().getCategory());
			}else{
				order.setCategory(new ProductCategory(0L));
			}
		}
		//加急
		String urgentFlag = MSDictUtils.getDictSingleValue("OrderUrgentFlag","0");
		if(urgentFlag.equalsIgnoreCase("1")) {
			order.setUrgentFlag(1);
			urgentLevels = urgentLevelService.findAllList();
			model.addAttribute("urgentLevels", urgentLevels);
		}else{
			order.setUrgentFlag(0);
		}
		if(order.getUrgentLevel() == null){
			order.setUrgentLevel(new UrgentLevel(0l));//默认不加急
		}
		if(isCustomer) {
			if(customer != null){
				order.setCustomer(customer);
			}else {
				customer = user.getCustomerAccountProfile().getCustomer();
				order.setCustomer(user.getCustomerAccountProfile().getCustomer());
			}
		}

		if(order.getCustomer()==null) {
			//addMessage(model, "请选择客户。");
			order.setCustomerBalance(0.00);
			order.setCustomerCredit(0.00);
			model.addAttribute("order", order);
			model.addAttribute("canCreateOrder", true);
			return viewForm;
		}

		//3.检查客户账户是否可下单
		//余额+信用额度-预付金额-冻结金额>0
		//重取客户财务信息
		CustomerFinance finance = customerService.getFinance(order.getCustomer().getId());
		order.setCustomerBalance(finance.getBalance());
		order.setCustomerCredit(finance.getCreditFlag() == 1 ? finance.getCredit() : 0);
		order.setCustomerBlockBalance(finance.getBlockAmount());
		if (order.getCustomerBalance() + order.getCustomerCredit() - order.getCustomerBlockBalance() > 0) {
			canCreateOrder = true;
			if (order.getOrderPaymentType() == null || StringUtils.isBlank(order.getOrderPaymentType().getLabel())) {
//				Dict orderPaymentType = DictUtils.getDictByValue(String.valueOf(finance.getPaymentType()),"PaymentType");
//				order.setOrderPaymentType(orderPaymentType);
				order.setOrderPaymentType(finance.getPaymentType());
			}
		}else{
			addMessage(model,"账户余额不足，请尽快充值。");
		}

		// 用于当不同的用户在同一台电脑上下单，串单的问题
		order.setCreateBy(user);
		order.setCreateDate(new Date());
		try {
			redisUtils.setEX(RedisConstant.RedisDBType.REDIS_TEMP_DB, key, order, OrderUtils.TMP_ORDER_EXPIRED);//延长半小时
		}catch (Exception e){
			log.error("[OrderController.form]redis setEx ,key:{}",key,e);
		}

		model.addAttribute("order", order);
		model.addAttribute("maxSelectGalleryQty",createOrderConfig.getSecondType().getMaxSelectQty());
		model.addAttribute("canCreateOrder", canCreateOrder);
		return viewForm;
	}

	/**
	 * @date 2020/03/20
	 * @version 1.0
	 */
	@RequiresPermissions("sd:order:add")
	@RequestMapping(value = "second/add",method = RequestMethod.POST)
	public String addSecondOrder(CreateOrderModel order, HttpServletRequest request,
						   HttpServletResponse response, Model model,
						   RedirectAttributes redirectAttributes)
	{
		User user = UserUtils.getUser();
		if (user == null || user.getId()==null) {
			try {
				SecurityUtils.getSubject().logout();
			} catch (Exception e) {
				throw new RuntimeException("您的账号登录超时，请重新登录");
			}
		}
		//Long start = System.currentTimeMillis();
		Customer customer = customerService.getFromCache(order.getCustomer().getId());
		if(customer == null){
			addMessage(redirectAttributes,"检查客户结算方式错误。");
			return "redirect:" + Global.getAdminPath() + "/sd/order/second/createForm";
		}
		//customer缓存已保存付款方式
		CustomerFinance cacheFinance = customer.getFinance();
		if(cacheFinance == null || cacheFinance.getPaymentType() == null || StringUtils.isBlank(cacheFinance.getPaymentType().getValue())){
			addMessage(redirectAttributes,"未设置结算方式，请联系系统管理员。");
			return secondCreateForm(order, request, response, model);
		}

		//保存到临时缓存
		order.setCreateDate(new Date());
		order.setCreateBy(user);
		if(StringUtils.isBlank(order.getAction())){
			order.setAction("new");
		}
		//String cachekey = getUserTmpOrderCacheKey(request, response);
		String cachekey = OrderUtils.getUserTmpOrderCacheKey(request, response,order.getAction());
		/* 检查品类 2019-09-25 */
		CreateOrderModel cacheOrder = (CreateOrderModel)redisUtils.get(RedisConstant.RedisDBType.REDIS_TEMP_DB,cachekey,CreateOrderModel.class);
		if(cacheOrder != null){
			order.setItems(cacheOrder.getItems());
		}else{
			Product p;
			Long categoryId = null;
			for(OrderItemModel item:order.getItems()){
				p = productService.getProductByIdFromCache(item.getProduct().getId());
				/* 检查品类 2019-09-25 */
				if(categoryId == null){
					categoryId = p.getCategory().getId();
				}else{
					if(!categoryId.equals(p.getCategory().getId())){
						addMessage(model, "订单中产品属不同品类，无法保存。");
						return secondCreateForm(order, request, response, model);
					}
				}
				item.setProduct(p);
			}
		}
		redisUtils.setEX(RedisConstant.RedisDBType.REDIS_TEMP_DB,cachekey,order,OrderUtils.TMP_ORDER_EXPIRED);
		// check
		if (!beanValidator(model, order)) {
			return secondCreateForm(order, request, response, model);
		}

		// 如果区域为空的情况
		if (order.getArea() == null || order.getArea().getId() == null  || order.getArea().getId()<=0)
		{
			addMessage(model, "找不到指定的区域,请重新选择。");
			return secondCreateForm(order, request, response, model);
		}
		//检查区域type
		Area area  = areaService.getFromCache(order.getArea().getId());
		if(area == null){
			addMessage(model, "找不到指定的区域,请重新选择。");
			return secondCreateForm(order, request, response, model);
		}
		if(area.getType() != 4){
			addMessage(model, "区域请选择至区/县,请重新选择。");
			return secondCreateForm(order, request, response, model);
		}
		//加急
		String urgentFlag = MSDictUtils.getDictSingleValue("OrderUrgentFlag","0");
		UrgentLevel inUrgentLevel = order.getUrgentLevel();
		if(inUrgentLevel != null && inUrgentLevel.getId().longValue() > 0 && !urgentFlag.equalsIgnoreCase("1")){
			addMessage(model, "系统未开通加急，但传入有加急等级。");
			return secondCreateForm(order, request, response, model);
		}

		//金额二次检查
		CustomerFinance finance = customerService.getFinanceForAddOrder(order.getCustomer().getId());
		if(finance.getBalance() + (finance.getCreditFlag() == 1 ? finance.getCredit() : 0) -finance.getBlockAmount() - order.getExpectCharge() - order.getBlockedCharge()<0){
			addMessage(model, "账户余额不足，请尽快充值。");
			return secondCreateForm(order, request, response, model);
		}
		order.setOrderPaymentType(finance.getPaymentType());
		customer.setFinance(finance);
		Product product = order.getItems().get(0).getProduct();
		Long categoryId = (product.getCategory()==null || product.getCategory().getId() == null)?0L:product.getCategory().getId();
		int canRush = 0;
		int kefuType = 0;
		Map<Integer,Area> areas = areaService.getAllParentsWithDistrict(order.getArea().getId());
		Area province = areas.getOrDefault(Area.TYPE_VALUE_PROVINCE,new Area(0L));
		Area city = areas.getOrDefault(Area.TYPE_VALUE_CITY,new Area(0L));
		//vip客户，不检查突击区域 ， 街道id小于等于3也不检查突击区域 2020-06-20 Ryan
		long subAreaId = Optional.ofNullable(order.getSubArea()).map(t->t.getId()).orElse(0l);
		/*if(customer.getVipFlag() == 1){
			kefuType = OrderCondition.VIP_KEFU_TYPE;
		}else{ //有街道
			canRush = orderService.isCanRush(categoryId,city.getId(),order.getArea().getId(),subAreaId);
			kefuType = orderService.getKefuType(categoryId,city.getId(),order.getArea().getId(),subAreaId);
		}*/
		OrderKefuTypeRuleEnum orderKefuTypeRuleEnum  = orderService.getKefuType(categoryId,city.getId(),order.getArea().getId(),subAreaId,customer.getVipFlag(),customer.getVip());
		kefuType = orderKefuTypeRuleEnum.getCode();
		if(kefuType==OrderCondition.RUSH_KEFU_TYPE){
			canRush = 1;
		}
		//随机客服
		User kefu = orderService.getRandomKefu(order.getCustomer().getId(),order.getArea().getId(),categoryId,kefuType,city.getId(),province.getId());
		if (kefu == null) {
			//无客服
			//addMessage(model, "此区域暂未分配"+failReason+"，暂时无法下单。请联系管理员：18772732342，QQ:572202493");
            String tip = orderService.noFindKefuTip(user,customer,categoryId,kefuType,order.getArea().getId(),city.getId(),province.getId());
            model.addAttribute("noKefuFlag",1);
            model.addAttribute("tip",tip);
			return secondCreateForm(order, request, response, model);
		}
		String orderNo = orderService.getNewOrderNo();
		if(StringUtils.isBlank(orderNo)){
			addMessage(model, "生成订单号失败，请重试");
			return secondCreateForm(order, request, response, model);
		}

		customer.setFinance(finance);
		order.setCustomer(customer);//*

		//重新计算及检查订单项及金额
		int itemCount = 0;
		int totalQty =0;//下单的产品数量
		OrderItemModel item = null;
		int itemNo = 0;
		Product p;
		List<OrderItemModel> items = order.getItems();
		OrderUtils.rechargeOrder(items);//重新计价
		Double totalCharge = 0.00;
		Double blockedCharge = 0.00;

		Set<String> pids = Sets.newHashSet();//产品
		Set<String> sids = Sets.newHashSet();//服务项目
		Integer hasSet = 0;
		List<Dict> expressCompanys = MSDictUtils.getDictList("express_type");//切换为微服务
		Dict expressCompany;
		int orderServiceType = 0;
		// 根据服务类型中工单类型判断
		Map<Long,ServiceType> serviceTypeMap = serviceTypeService.getAllServiceTypeMap();
		StringBuilder content = new StringBuilder();
		content.append("师傅，在您附近有一张  ");
		categoryId = null;
		ServiceType serviceType;
		for (int j=0,size=items.size();j<size;j++) {
			item = items.get(j);
			if (item == null || !item.getDelFlag().equals(OrderCondition.DEL_FLAG_NORMAL) || item.getFlag().equalsIgnoreCase("del")) {
				continue;
			}
			if (item.getProduct() == null || item.getProduct().getId() == null) {
				addMessage(model, "选择产品。");
				return secondCreateForm(order, request, response, model);
			} else if (item.getServiceType() == null || item.getServiceType().getId() == null) {
				addMessage(model, "选择服务类型。");
				return secondCreateForm(order, request, response, model);
			}
			item.setItemNo(itemNo += 10);
			itemCount++;
			p = item.getProduct();
			if(categoryId == null){
				categoryId = p.getCategory().getId();
			}
			item.setProduct(p);
			if (p.getSetFlag() == 1) {
				hasSet = 1;
			}
			pids.add(String.format(",%s,", p.getId()));
			sids.add(String.format(",%s,", item.getServiceType().getId()));
			//工单类型按服务项目设定为准
			serviceType = serviceTypeMap.get(item.getServiceType().getId());
			if(serviceType == null){
				addMessage(model, "确认服务项目的工单类型错误，请重试");
				return secondCreateForm(order, request, response, model);
			}
			//除维修(2)外，值最大的优先
			if(orderServiceType == 0){
				orderServiceType = serviceType.getOrderServiceType();
			}else if (serviceType.getOrderServiceType() == 2){
				orderServiceType = serviceType.getOrderServiceType();
			}else if(orderServiceType < serviceType.getOrderServiceType()){
				orderServiceType = serviceType.getOrderServiceType();
			}
			final Dict company = item.getExpressCompany();
			if (expressCompanys != null && expressCompanys.size() > 0) {
				expressCompany = expressCompanys.stream().filter(t -> t.getValue().equalsIgnoreCase(company.getValue())).findFirst().orElse(item.getExpressCompany());
				item.setExpressCompany(expressCompany);
			}

			totalQty = totalQty + item.getQty();
			totalCharge = totalCharge + item.getCharge();
			blockedCharge = blockedCharge + item.getBlockedCharge();
			content.append(item.getServiceType().getName())
					.append(item.getBrand())
					.append(item.getProduct().getName());
		}
		content.append("的工单，请尽快登陆APP接单~");

		if (itemCount == 0)
		{
			addMessage(model, "订单无服务项目，请添加。");
			return secondCreateForm(order, request, response, model);
		}

		if(orderServiceType==0){
			orderServiceType = 2;//维修
		}
		order.setExpectCharge(totalCharge);//*
		order.setBlockedCharge(blockedCharge);//*
		order.setTotalQty(totalQty);//*
		order.setCreateDate(new Date());//*
		order.setCreateBy(user);//*
		//转换后的订单实例
		Order o;
		try
		{
			//加急
			UrgentLevel urgentLevel = order.getUrgentLevel();
			if(urgentLevel != null && urgentLevel.getId().longValue()>0){
				HashMap<String,Object> urgentChargeMap = orderService.getCustomerUrgentCharge(order.getCustomer().getId(),order.getArea().getId(),urgentLevel.getId(),"false");
				if(urgentChargeMap == null || urgentChargeMap.size() == 0){
					addMessage(model, "确认加急费失败，请重试");
					return secondCreateForm(order, request, response, model);
				}
				double chargeIn = (double)urgentChargeMap.get("chargeIn");
				order.setCustomerUrgentCharge(chargeIn);//下单时写入，在客评时，判断是否符合加急，不符合更改为0
				order.setEngineerUrgentCharge((double)urgentChargeMap.get("chargeOut"));
			}else{
				order.setUrgentLevel(new UrgentLevel(0l));//默认不加急
				order.setCustomerUrgentCharge(0.0);
				order.setEngineerUrgentCharge(0.0);
			}

			String quarter = QuarterUtils.getSeasonQuarter(order.getCreateDate());//分片
			order.setQuarter(quarter);
			Long oid = SeqUtils.NextIDValue(SeqUtils.TableName.Order);//production
			order.setId(oid);

			order.setOrderNo(orderNo);
			order.setCustomerOwner(user.getName());//下单负责人 2018/08/03
			//转换
			o = OrderUtils.toOrder(order);
			o.getOrderCondition().setProvinceId(province.getId());
			o.getOrderCondition().setCityId(city.getId());
			o.getOrderCondition().setCanRush(canRush);
			o.getOrderCondition().setKefuType(kefuType);
			/* 针对手动选择区域，重新获得经纬度座标 2019-04-15 */
			if(order.getLongitude() == 0 || order.getLatitude() == 0){
				String address = MessageFormat.format("{0} {1}",area.getFullName(),o.getOrderCondition().getAddress());
				String[] rstArrays = AreaUtils.getLocation(address);
				if(rstArrays != null && rstArrays.length == 2 ){
					o.getOrderLocation().setLongitude(StringUtils.toDouble(rstArrays[0]));
					o.getOrderLocation().setLatitude(StringUtils.toDouble(rstArrays[1]));
				}
			}
			/* 再次检查重单 2018/04/19 */
			if(StringUtils.isBlank(o.getRepeateNo())) {
				//使用新的重单检查方法，从缓存中检查
				String repeateOrderNo = orderService.getRepeateOrderNo(order.getCustomer().getId(),order.getPhone1());
				if (StringUtils.isNotBlank(repeateOrderNo)) {
					o.setRepeateNo(repeateOrderNo);
				}
			}
			Dict status;
			if(user.isSystemUser()){
				//不需审核
				status = MSDictUtils.getDictByValue(String.valueOf(Order.ORDER_STATUS_APPROVED), "order_status");//切换为微服务
			}else if(user.isCustomer() && user.getCustomerAccountProfile().getOrderApproveFlag()==0) {
				//不需审核
				status = MSDictUtils.getDictByValue(String.valueOf(Order.ORDER_STATUS_APPROVED), "order_status");//切换为微服务
			}else {
				status = MSDictUtils.getDictByValue(String.valueOf(Order.ORDER_STATUS_NEW), "order_status");//切换为微服务
			}
			o.getOrderCondition().setTotalQty(totalQty);
			o.getOrderCondition().setStatus(status);
			o.getOrderCondition().setKefu(kefu);
			o.getOrderCondition().setProductCategoryId(categoryId);
			o.getOrderCondition().setHasSet(hasSet);
			o.getOrderCondition().setProductIds(pids.stream().collect(Collectors.joining(",")).replace(",,,",",,"));
			o.getOrderCondition().setServiceTypes(sids.stream().collect(Collectors.joining(",")).replace(",,,",",,"));
			o.getOrderCondition().setOrderServiceType(orderServiceType);//12-06

			Dict orderType = MSDictUtils.getDictByValue(String.valueOf(Order.ORDER_ORDERTYPE_DSXD),"order_type");//切换为微服务
			if(orderType == null){
				orderType = new Dict(Order.ORDER_ORDERTYPE_DSXD,"电商下单");
			}
			o.setOrderType(orderType);// 电商下单

			//如果地址中出现null 字符串，则会引起 API 返回JSON数据到APP 导致格式不匹配,所以加字符串判断
			if(o.getOrderCondition().getAddress()!=null)
			{
				String address=o.getOrderCondition().getAddress().replace("null", "");
				o.getOrderCondition().setAddress(address);
			}
			String dailyLogKey = String.format(RedisConstant.SD_CREATE_ORDER_LOG,DateUtils.getDate());
			boolean saveSuccess = true;
			//订单保存
			try {//成功，执行后面操作
				orderService.createOrder_v2_1(o,null);
			}catch (Exception e){
				saveSuccess = false;
				try {
					log.error("下单保存数据库失败,json:{}",gsonRedisSerializer.toJson(o),e);
				}catch (Exception e1){
					log.error("下单保存数据库失败,userId:{} ,userName:{} ,mobile:{}",user.getId(),order.getUserName(),order.getServicePhone(),e);
				}
				try {
					//订单号返还redis列表
					if(StringUtils.isNoneBlank(orderNo)){
						SeqUtils.reputOrderNo(o.getCreateDate(),orderNo);
					}
				}catch (Exception e1){
					log.error("返还订单号失败,orderNo:{}",orderNo,e1);
				}
				addMessage(model,"下单失败,请重试。");
				return secondCreateForm(order, request, response, model);
			}finally {
				//下单log，供每日核对，防止漏单
				//先加到redis,不成功记录到sys_log
				try{
					double score = Double.valueOf(orderNo.substring(1));
					if(saveSuccess) {
						redisUtils.zAdd(RedisConstant.RedisDBType.REDIS_TEMP_DB, dailyLogKey, orderNo, score, OrderUtils.REDIS_CREATE_LOG_EXPIRED);
					}else{
						redisUtils.zAdd(RedisConstant.RedisDBType.REDIS_TEMP_DB, dailyLogKey, CreateOrderModelAdapter.getInstance().toJson(order), score, OrderUtils.REDIS_CREATE_LOG_EXPIRED);
					}
				}catch (Exception e){
					log.error("[OrderController.addOrder] push orderNo:{} to dailyLogKey:{}",orderNo,dailyLogKey,e);
				}
			}

			if (true == saveSuccess) {
				o.setAppMessage(content.toString());
				orderMQService.sendCreateOrderMessage(o,"addOrder");

				// 更新临时订单缓存
				try {
					order = new CreateOrderModel();
					order.setCustomer(o.getOrderCondition().getCustomer());
					redisUtils.setEX(RedisConstant.RedisDBType.REDIS_TEMP_DB, cachekey, order, OrderUtils.TMP_ORDER_EXPIRED);
				} catch (Exception e) {
				}
			}
			addMessage(model, "新建订单:".concat(orderNo).concat(" 成功"));
			return secondCreateForm(order, request, response, model);
		} catch (Exception e){
			String json = new String();
			try{
				json = gsonRedisSerializer.toJson(order);
			}catch(Exception e1) {}
			log.error("下单失败,order:{}",json,e);
			addMessage(model, "下单失败,请重试。" + ExceptionUtils.getRootCauseMessage(e));
			return secondCreateForm(order, request, response, model);
		}
	}

	//region 修改订单

	/**
	 * 修改订单窗口
	 */
	@RequiresPermissions(value = { "sd:order:edit", "sd:order:add" }, logical = Logical.OR)
	@RequestMapping(value = "/second/edit")
	public String secondEditForm(CreateOrderModel order, HttpServletRequest request, HttpServletResponse response, Model model)
	{
		String viewForm = "modules/sd/secondOrder/orderEditForm";
		boolean hasPermissionShowReceive = false;
		if (SecurityUtils.getSubject().isPermitted("sd:order:showreceive")) {
			hasPermissionShowReceive = true;
		}
		model.addAttribute("showReceive",hasPermissionShowReceive);
		Map<String, Object> attributes = Maps.newHashMapWithExpectedSize(10);
		attributes.put("createOrderConfig",createOrderConfig);
		if(order.getId() == null || order.getId().longValue() <=0){
			attributes.put("order",order);
			attributes.put("canCreateOrder", "false");
			return returnViewForm(attributes,model,viewForm,"错误：订单参数为空");
		}
		User user = UserUtils.getUser();
		String cachekey;
		Order o = order.getOrder();
		if(o == null){
			String cid = request.getParameter("id");
			Long id = Long.parseLong(cid);
			if(id == null || id <= 0){
				attributes.put("canCreateOrder", "false");
				return returnViewForm(attributes,model,viewForm,"错误：订单参数为空!");
			}
			o =orderService.getOrderById(id,order.getQuarter(),OrderUtils.OrderDataLevel.FEE,true);
			if(o == null || o.getOrderCondition() == null){
				attributes.put("order",order);
				attributes.put("canCreateOrder", "false");
				return returnViewForm(attributes,model,viewForm,"错误：系统繁忙，读取订单失败，请重试!");
			}
		}
		OrderCondition condition = o.getOrderCondition();
		int statusValue = condition.getStatusValue();
		if (user.isCustomer() && !OrderUtils.canEdit(statusValue))
		{
			//客户，已接单订单不允许修改
			attributes.put("order",order);
			attributes.put("canCreateOrder", "false");
			return returnViewForm(attributes,model,viewForm,"订单:".concat(order.getOrderNo()).concat(" 不允许修改订单，请刷新，并确认订单具体状态及明细。"));
		}else if(user.isSystemUser()
				&& (condition.getPartsFlag() == 1 || condition.getServiceTimes()>0 || statusValue >= Order.ORDER_STATUS_RETURNING)
		){
			//后台帐号，上门服务前(含上门服务)有配件和上门记录的订单不允许修改
			attributes.put("order",order);
			attributes.put("canCreateOrder", "false");
			return returnViewForm(attributes,model,viewForm,"订单:".concat(order.getOrderNo()).concat(" 不允许修改订单，请刷新，并确认订单是否已有上门服务记录或配件申请。"));
		}

		//检查是否有未完成突击单
		//有，不允许修改
		if(Sets.newHashSet(Integer.valueOf(1),Integer.valueOf(3)).contains(condition.getRushOrderFlag())){
			attributes.put("order",order);
			attributes.put("canCreateOrder", "false");
			return returnViewForm(attributes,model,viewForm,"订单突击正在处理中，不允许修改订单信息！");
		}
		Long customerId = order.getCustomer().getId();
		if(!org.springframework.util.ObjectUtils.isEmpty(order.getItems())){
			order.setCategory(order.getItems().get(0).getProduct().getCategory());
		}
		//加急
		String urgentFlag = MSDictUtils.getDictSingleValue("OrderUrgentFlag","0");
		if(urgentFlag.equalsIgnoreCase("1")) {
			order.setUrgentFlag(1);
			List<UrgentLevel> urgentLevels = urgentLevelService.findAllList();
			model.addAttribute("urgentLevels", urgentLevels);
		}else{
			order.setUrgentFlag(0);
		}
		if(order.getUrgentLevel() == null){
			order.setUrgentLevel(new UrgentLevel(0l));//默认不加急
		}
		//客户
		CustomerFinance finance = customerService.getFinance(customerId);
		order.setCustomerBalance(finance.getBalance());// 客户余额
		order.setCustomerBlockBalance(finance.getBlockAmount());//冻结金额
		order.setCustomerCredit(finance.getCreditFlag() == 1 ? finance.getCredit() : 0);// 客户信用额度
		order.setCreateBy(user);//更改为当前用户
		//item.tmpId
		long lid = System.currentTimeMillis();
		int idx =0;
		for(OrderItemModel item:order.getItems()){
			item.setTmpId(String.valueOf(lid+idx));
			idx++;
		}
		//判断4级地址是否属于3级地址下的街道
		if(condition.getSubArea()!=null && condition.getSubArea().getId()>3){
			Area subArea = areaService.getFromCache(condition.getSubArea().getId());
			if(subArea!=null){
				if(!subArea.getParent().getId().equals(condition.getArea().getId())){
					condition.getSubArea().setId(1L);
				}
			}
		}
		cachekey = OrderUtils.getUserTmpOrderCacheKey(request, response,"editv2");
		try {
			redisUtils.setEX(RedisConstant.RedisDBType.REDIS_TEMP_DB, cachekey, order, OrderUtils.TMP_ORDER_EXPIRED);
		}catch (Exception e){
			try{
				redisUtils.setEX(RedisConstant.RedisDBType.REDIS_TEMP_DB, cachekey, order, OrderUtils.TMP_ORDER_EXPIRED);
			}catch (Exception e1){}
		}
		attributes.put("order",order);
		attributes.put("canCreateOrder", "true");
		return returnViewForm(attributes,model,viewForm,null);
	}

	/**
	 * 保存修改后的订单
	 */
	@RequiresPermissions("sd:order:edit")
	@RequestMapping(value = "/second/save",method = RequestMethod.POST)
	public String saveSecondOrder(CreateOrderModel order, HttpServletRequest request,
							HttpServletResponse response, Model model,
							RedirectAttributes redirectAttributes)
	{
		User user = UserUtils.getUser();
		if (user == null || user.getId()==null) {
			try {
				SecurityUtils.getSubject().logout();
			} catch (Exception e) {
				addMessage(model, "您的账号登录超时，请重新登录。");
				return secondEditForm(order, request, response, model);
			}
		}

		//检查订单是否被修改
		if(!orderService.orderVersionValid(order.getId(),order.getVersion())){
			addMessage(model, "订单已被其它帐号操作，请刷新待审核列表。");
			return secondEditForm(order, request, response, model);
		}

		//保存到缓存
		//String cachekey = getUserEditOrderCacheKey(request, response);
		String cachekey = OrderUtils.getUserTmpOrderCacheKey(request, response,"editv2");
		try {
			redisUtils.setEX(RedisConstant.RedisDBType.REDIS_TEMP_DB, cachekey, order, OrderUtils.TMP_ORDER_EXPIRED);
		} catch (Exception e){
			log.error("[OrderController.saveOrder] key:{}",cachekey,e);
		}

		// check
		if (!beanValidator(model, order)) {
			return secondEditForm(order,request, response, model);
		}

		// 如果区域为空的情况
		if (order.getArea() == null || order.getArea().getId() == null || order.getArea().getId() == 0) {
			addMessage(model, "找不到指定的区域,请重新选择。");
			return secondEditForm(order, request, response, model);
		}
		Order orgOrder;
		try {
			orgOrder = orderService.getOrderById(order.getId(), order.getQuarter(), OrderUtils.OrderDataLevel.CONDITION, true);
		}catch (Exception e){
			addMessage(model, "读取修改前订单服务项目错误，请重试");
			return secondEditForm(order, request, response, model);
		}
		if(orgOrder == null){
			addMessage(model, "错误：系统繁忙，读取订单失败，请重试。");
			return secondEditForm(order, request, response, model);
		}
		OrderCondition orgOrderCondition = orgOrder.getOrderCondition();
		if(orgOrderCondition == null || orgOrderCondition.getStatus() == null || orgOrderCondition.getStatus().getIntValue() == 0){
			addMessage(model, "确认订单状态错误，请重试。");
			return secondEditForm(order, request, response, model);
		}
		if(orgOrderCondition.getStatus().getIntValue() > Order.ORDER_STATUS_PLANNED){
			addMessage(model, "错误：订单已开始上门服务，不允许修改。");
			return secondEditForm(order, request, response, model);
		}

		// 订单项次处理
		List<OrderItemModel> list = order.getItems();
		List<CustomerPrice> prices = customerService.getPricesFromCache(order.getCustomer().getId());
		Optional<CustomerPrice> price ;
		Product p;
		Set<String> pids = Sets.newHashSet();//产品
		Set<String> sids = Sets.newHashSet();//服务项目
		Integer hasSet = 0;
		int orderServiceType = 0;
		// 根据服务类型中工单类型判断
		Map<Long,ServiceType> serviceTypeMap = serviceTypeService.getAllServiceTypeMap();
		Long categoryId = null;//品类
		ServiceType serviceType;
		// 移除产品为空的项目,并读取最新价格
		for (Iterator<OrderItemModel> it = list.iterator(); it.hasNext();)
		{
			OrderItem item = it.next();
			if(item == null || item.getProduct() == null || item.getServiceType() == null){
				it.remove();
				continue;
			}
			//价格
			price = prices.stream().filter(t->
					Objects.equals(t.getProduct().getId(),item.getProduct().getId()) && Objects.equals(t.getServiceType().getId(),item.getServiceType().getId())).findFirst();
			if(!price.isPresent()){
				addMessage(model, String.format("产品:%s 未定义服务项目:%s 的服务价格",item.getProduct().getName(),item.getServiceType().getName()));
				return secondEditForm(order, request, response, model);
			}
			item.setStandPrice(price.get().getPrice());
			item.setDiscountPrice(price.get().getDiscountPrice());
			p = productService.getProductByIdFromCache(item.getProduct().getId());
			/* 检查品类 2019-09-25 */
			if(categoryId == null){
				categoryId = p.getCategory().getId();
			}else{
				if(!categoryId.equals(p.getCategory().getId())){
					addMessage(model, "订单中产品属不同品类，无法保存。");
					return form(order, request, response, model);
				}
			}
			item.setProduct(p);
			pids.add(String.format(",%s,",p.getId()));
			sids.add(String.format(",%s,",item.getServiceType().getId()));
			//工单类型按服务项目设定为准
			serviceType = serviceTypeMap.get(item.getServiceType().getId());
			if(serviceType == null){
				addMessage(model, "确认服务项目的工单类型错误，请重试");
				return secondEditForm(order, request, response, model);
			}
			//除维修(2)外，值最大的优先
			if(orderServiceType == 0){
				orderServiceType = serviceType.getOrderServiceType();
			}else if (serviceType.getOrderServiceType() == 2){
				orderServiceType = serviceType.getOrderServiceType();
			}else if(orderServiceType < serviceType.getOrderServiceType()){
				orderServiceType = serviceType.getOrderServiceType();
			}
		}
		if(orderServiceType==0){
			orderServiceType = 2;
		}

		//重新计算价格
		OrderUtils.rechargeOrder(list);
		Double totalCharge = 0.00;
		Double blockedCharge = 0.00;
		Integer qty = 0;
		for (OrderItemModel item : list) {
			totalCharge = totalCharge + item.getCharge();
			blockedCharge = blockedCharge + item.getBlockedCharge();
			qty = qty + item.getQty();
		}

		if (qty == 0)
		{
			addMessage(model, "订单无明细项目，请添加。");
			return secondEditForm(order, request, response, model);
		}
		order.setExpectCharge(totalCharge);
		order.setBlockedCharge(blockedCharge);

		//原订单费用
		OrderFee fee = orderService.getOrderFeeById(order.getId(),order.getQuarter(),false);
		//加急，不变更
		order.setUrgentLevel(orgOrderCondition.getUrgentLevel());
		order.setCustomerUrgentCharge(fee.getCustomerUrgentCharge());
		order.setEngineerUrgentCharge(fee.getEngineerUrgentCharge());
		//客户账户余额检查
		CustomerFinance cfee = customerService.getFinanceForAddOrder(order.getCustomer().getId());
		Double credit = cfee.getCreditFlag()==0?0d:cfee.getCredit();
		//客户账户余额 + 信用额度 + 此单费用(=此单原金额 + 此单原冻结金额) - 现客户总冻结金额 - 修改后单费用(=当前单金额 + 加急费 + 当前单冻结金额)
		if(cfee.getBalance() + credit + fee.getExpectCharge() + fee.getBlockedCharge() - cfee.getBlockAmount() - totalCharge - order.getCustomerUrgentCharge() - blockedCharge <0  ) {
			addMessage(model, "账户余额不足...");
			return secondEditForm(order, request, response, model);
		}

		StringBuffer orgItemString = new StringBuffer(200);
		OrderItem orgItem;
		List<OrderItem> orgItems = orgOrder.getItems();
		for(int i=0,size=orgItems.size();i<size;i++){
			orgItem = orgItems.get(i);
			orgItemString.append(String.valueOf(i+1))
					.append(".")
					.append("产品：").append(orgItem.getProduct().getName()).append(" ")
					.append("品牌:").append(orgItem.getBrand()).append(" ")
					.append("服务:").append(orgItem.getServiceType().getName()).append(" ")
					.append("数量:").append(orgItem.getQty())
					.append(" ");
		}
		Order o = OrderUtils.toOrder(order);
		try
		{
			//如果地址中出现null 字符串，则会引起 API 返回JSON数据到APP 导致格式不匹配,所以加字符串判断
			if(o.getOrderCondition().getAddress()!=null)
			{
				String address=o.getOrderCondition().getAddress().replace("null", "");
				o.getOrderCondition().setAddress(address);
			}
			o.preUpdate();
			o.getOrderCondition().setStatus(orgOrderCondition.getStatus());//edited by zhoucy, 工单修改后其状态保持不变
			o.getOrderCondition().setTotalQty(qty);//下单时的产品数量
			o.getOrderCondition().setProductCategoryId(categoryId);//品类 2019-09-25
			o.getOrderCondition().setHasSet(hasSet);
			o.getOrderCondition().setProductIds(pids.stream().collect(Collectors.joining(",")).replace(",,,",",,"));
			o.getOrderCondition().setServiceTypes(sids.stream().collect(Collectors.joining(",")).replace(",,,",",,"));
			o.getOrderCondition().setOrderServiceType(orderServiceType);//12-06
			//quarter
			o.setQuarter(order.getQuarter());
			o.setVerificationCode("sencond:order");//灯饰下单，用于判断更改店铺
			/* 省/市id 2019-09-25 */
			Map<Integer,Area> areas = areaService.getAllParentsWithDistrict(order.getArea().getId());
			Area province = areas.getOrDefault(Area.TYPE_VALUE_PROVINCE,new Area(0L));
			Area city = areas.getOrDefault(Area.TYPE_VALUE_CITY,new Area(0L));
			o.getOrderCondition().setProvinceId(province.getId());
			o.getOrderCondition().setCityId(city.getId());
            //检查客服类型 2020-12-8
			Customer customer = customerService.getFromCache(order.getCustomer().getId());
			Area subArea = o.getOrderCondition().getSubArea();
			int kefuType = 0;
			int canRush = 0;
			/*if(customer!=null && customer.getVipFlag()==1){
				kefuType = OrderCondition.VIP_KEFU_TYPE;
			}else if(customer!=null && customer.getVipFlag()==0){
				kefuType = orderService.getKefuType(categoryId,city.getId(),order.getArea().getId(),subArea.getId());
			}*/
			OrderKefuTypeRuleEnum orderKefuTypeRuleEnum = orderService.getKefuType(categoryId,city.getId(),order.getArea().getId(),subArea.getId(),customer.getVipFlag(),customer.getVip());
			kefuType = orderKefuTypeRuleEnum.getCode();
			o.getOrderCondition().setKefuType(kefuType);
			if(kefuType==OrderKefuTypeRuleEnum.ORDER_RUSH.getCode()){
				canRush = 1;
			}
			o.getOrderCondition().setCanRush(canRush);
			User kefu = orderService.getRandomKefu(customer.getId(),order.getArea().getId(),categoryId,kefuType,city.getId(),province.getId());
			if(kefu==null){
				//addMessage(model, "此区域暂未分配"+failReason+"，暂时无法下单。请联系管理员：18772732342，QQ:572202493");
				String tip = orderService.noFindKefuTip(user,customer,categoryId,kefuType,order.getArea().getId(),city.getId(),province.getId());
				model.addAttribute("noKefuFlag",1);
				model.addAttribute("tip",tip);
				return secondEditForm(order, request, response, model);
			}
			orderService.updateOrder(o,fee,orgItemString.toString());//修改
			// 清空临时订单缓存
			try {
				redisUtils.remove(RedisConstant.RedisDBType.REDIS_SD_DB, cachekey);
			}catch (Exception e){
				log.error("[OrderController.saveOrder] remove redis,key:{}",cachekey,e);
			}
			try {
				CookieUtils.setCookie(response, OrderUtils.ORDER_EDIT_COOKIE_KEY, "", 0);// cookie失效
			}catch (Exception e){
				log.error("[OrderController.saveOrder] setCookie,key:{}",OrderUtils.ORDER_EDIT_COOKIE_KEY,e);
				try{
					CookieUtils.setCookie(response, OrderUtils.ORDER_EDIT_COOKIE_KEY, "", 0);// cookie失效
				}catch (Exception e1){
				}
			}

			addMessage(model, "修改订单:".concat(o.getOrderNo()).concat(" 成功"));
			if(order.getActionType().equalsIgnoreCase("approve")) {
				return "redirect:" + Global.getAdminPath() + "/sd/order/customer/approvelist?repage=true";//订单审核
			}
			if(user.isCustomer() || order.getActionType().startsWith("cust_")){//
				return "redirect:" + Global.getAdminPath() + "/sd/order/customerNew/" + order.getActionType().replace("cust_","") + "list?repage=true";//我的订单
			}
			if (user.isSaleman() || order.getActionType().startsWith("sales_")) {
				return "redirect:" + Global.getAdminPath() + "/sd/order/salesNew/" + order.getActionType().replace("sales_","") + "list?repage=true";//我的订单
			}
			return "redirect:" + Global.getAdminPath() + "/sd/order/customerNew/list?repage=true";//我的订单
		} catch (OrderException oe) {
			addMessage(model, oe.getMessage());
			return secondEditForm(order, request, response, model);
		} catch (Exception e) {
			log.error("orderNo:{}",order.getOrderNo(),e);
			addMessage(model, e.getMessage());
			return secondEditForm(order, request, response, model);
		}
	}

	//endregion 修改订单

	//endregion 新版本下单

	//endregion 下单


	//region 客服跟踪进度(分页)
	/**
	 * [Ajax]订单详情-跟踪进度(分页获取)
	 * 包含：要增加内容和记录
	 * 显示顺序按日期倒序
	 * @param orderId	订单id
	 */
	@ResponseBody
	@RequestMapping(value = "orderDetailTrackingNew")
	public AjaxJsonEntity orderDetailTrackingNew(@RequestParam String orderId,String quarter,@RequestParam(required = false) Integer status, HttpServletResponse response,Integer pageNo)
	{
		User user = UserUtils.getUser();
		response.setContentType("application/json; charset=UTF-8");
		AjaxJsonEntity jsonEntity = new AjaxJsonEntity(true);
		try
		{
			Long lorderId = Long.valueOf(orderId);
			if(status == null) {
				Order order = orderService.getOrderById(lorderId, quarter, OrderUtils.OrderDataLevel.CONDITION, true);
				if(order == null || order.getOrderCondition() == null){
					jsonEntity.setSuccess(false);
					jsonEntity.setMessage("系统繁忙，读取订单失败，请重试");
					return jsonEntity;
				}
				// 检查是否可以保存
				if (!order.canTracking() && order.getOrderCondition().getPendingFlag() != 2) {
					jsonEntity.setSuccess(false);
					jsonEntity.setMessage(String.format("该订单不能进度跟踪，当前订单状态:%s", order.getOrderCondition().getStatus().getLabel()));
					return jsonEntity;
				}
			}else if(status.intValue()<Order.ORDER_STATUS_APPROVED.intValue()){
				jsonEntity.setSuccess(false);
				jsonEntity.setMessage("该订单为待审核订单，请先联系相关人员审核通过后再操作。");
				return jsonEntity;
			}
			int pageSize = 10;
			OrderTrackingModel trackingModel = new OrderTrackingModel();
			trackingModel.setOrderId(lorderId);
			trackingModel.setTrackingDate(new Date());
			trackingModel.setRemarks("");
			trackingModel.setQuarter(quarter);
			//读取跟踪进度,statusFlag 0:未更改订单状态 1:订单状态变更 4：进度跟踪
			OrderTrackingSearchModel orderTrackingSearchModel = new OrderTrackingSearchModel();
			orderTrackingSearchModel.setOrderId(lorderId);
			orderTrackingSearchModel.setQuarter(quarter);
			orderTrackingSearchModel.setStatusFlags(Arrays.asList(new Integer[] {0,1,4}));
			orderTrackingSearchModel.setCloseFlag(null);
			//List<OrderProcessLog> list = orderService.getOrderLogsByFlagsNew(lorderId,quarter,Arrays.asList(new Integer[] {0,1,4}),null,pageNo,pageSize);
			if(pageNo==0){
				pageNo = 1;
			}
			Page<OrderTrackingSearchModel> page = new Page<>(pageNo,pageSize);
			Page<OrderProcessLog> returnPage = orderService.getOrderLogsByFlagsNew(page,orderTrackingSearchModel);
			List<OrderProcessLog> list = returnPage.getList();
			if(pageNo!=page.getLast()){ //不是最后一页
				trackingModel.setNextPageFlag(true);
				trackingModel.setNextPageNo(pageNo+1);
			}else{
				trackingModel.setNextPageFlag(false);
			}
			int customer_visable_flag = 2;
			//过滤客户看见
			//List<OrderProcessLog> orderProcessLogs = list.stream().filter(t->t.getVisibilityFlag().intValue()!=customer_visable_flag).collect(Collectors.toList());
			Supplier<Stream<OrderProcessLog>> streamSupplier = () -> list.stream();
			//客户能看
			streamSupplier.get().filter(t-> (t.getVisibilityFlag().intValue()&customer_visable_flag) == customer_visable_flag)
					.forEach(t->{
						t.setRemarks(t.getActionComment());
					});
			//按时间正序
			List<OrderProcessLog> logs = streamSupplier.get()
					.sorted(Comparator.comparing(OrderProcessLog::getId).reversed()).collect(Collectors.toList());
			//list = list.stream().sorted(Comparator.comparing(OrderProcessLog::getId).reversed()).collect(Collectors.toList());
			trackingModel.setLogs(logs);
			trackingModel.setPageCount(page.getCount());
			//trackingModel.setPageCount(page.getCount());
			//跟踪项目
//			List<Dict> types = DictUtils.getDictList("TrackingType");
			//切换为微服务
			List<Dict> types = msDictService.findListByType("TrackingType");
			trackingModel.setTracks(types);
			jsonEntity.setData(trackingModel);
		} catch (Exception e)
		{
			jsonEntity.setSuccess(false);
			jsonEntity.setMessage(e.getMessage());
		}
		return jsonEntity;
	}

    /**
     * [Ajax]订单详情-修改工单判断区域属性是否改变
     * @param order	订单id
     */
    @ResponseBody
    @RequestMapping(value = "checkOrderKefuType")
    public AjaxJsonEntity checkOrderKefuType(Order order, HttpServletResponse response){
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        if (order == null || order.getId() == null)
        {
            result.setSuccess(false);
            result.setMessage("修改实际上门联系信息时发生错误：订单号丢失");
            return result;

        }
        OrderCondition orderCondition = order.getOrderCondition();
        // 如果区域为空的情况
        if (orderCondition.getArea() == null || orderCondition.getArea().getId() == null  || orderCondition.getArea().getId()<=0)
        {
            result.setSuccess(false);
            result.setMessage("请选择用户地址所在的省市区。");
            return result;
        }
        //检查区域type
        Area area  = areaService.getFromCache(orderCondition.getArea().getId());
        if(area == null){
            result.setSuccess(false);
            result.setMessage("找不到指定的区域,请重新选择。");
            return result;
        }
        if(area.getType() != 4){
            result.setSuccess(false);
            result.setMessage("区域请选择至区/县。");
            return result;
        }
        if(orderCondition.getSubArea() == null || orderCondition.getSubArea().getId() == null) {
            orderCondition.setSubArea(new Area(0L,""));
        }
		Order o = orderService.getOrderById(order.getId(), order.getQuarter(), OrderUtils.OrderDataLevel.CONDITION, true);
		if (o == null || o.getOrderCondition() == null) {
			throw new OrderException("读取订单失败，请重试。");
		}
        Customer customer = o.getOrderCondition().getCustomer();
        if(customer==null){
			result.setSuccess(false);
			result.setMessage("获取客户失败,请检查");
			return result;
		}
        try
        {
            //根据areaId获取省和市
            Map<Integer,Area> areas = areaService.getAllParentsWithDistrict(orderCondition.getArea().getId());
            Area city = areas.getOrDefault(Area.TYPE_VALUE_CITY,new Area(0L));
            OrderKefuTypeRuleEnum orderKefuTypeRuleEnum = orderService.getKefuType(o.getOrderCondition().getProductCategoryId(),city.getId(),orderCondition.getArea().getId(),orderCondition.getSubArea().getId(),customer.getVipFlag(),customer.getVip());
            if(!orderKefuTypeRuleEnum.getCode().equals(o.getOrderCondition().getKefuType())){
            	String oldKefuType = orderService.getKefuTypeName(o.getOrderCondition().getKefuType(),customer.getVipFlag());
            	String newKefuType = orderService.getKefuTypeName(orderKefuTypeRuleEnum.getCode(),customer.getVipFlag());
				result.setSuccess(true);
				result.setMessage("修改后工单负责客服由<font style=\"color:#2FA2DE\">"+oldKefuType+"</font>转为<font style=\"color:#2FA2DE\">"+newKefuType+"</font>，是否确认修改");
				return result;
			}
        } catch (OrderException oe){
            result.setSuccess(false);
            result.setMessage(oe.getMessage());
        } catch (Exception e){
            result.setSuccess(false);
            result.setMessage("修改实际上门联系信息时发生异常:" + e.getMessage());
            log.error("[OrderController.saveUpdateUserServiceInfo] orderId:{}",order.getId(),e);
        }
        return result;
    }


	//endregion

}

