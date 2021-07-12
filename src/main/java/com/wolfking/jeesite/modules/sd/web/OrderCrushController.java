package com.wolfking.jeesite.modules.sd.web;

import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.kkl.kklplus.entity.md.MDDepositLevel;
import com.kkl.kklplus.entity.md.MDServicePoint;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.Product;
import com.wolfking.jeesite.modules.md.entity.ProductCategory;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.md.entity.ServicePointFinance;
import com.wolfking.jeesite.modules.md.entity.viewModel.ServicePointModel;
import com.wolfking.jeesite.modules.md.service.ProductCategoryService;
import com.wolfking.jeesite.modules.md.service.ServicePointService;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.entity.OrderCondition;
import com.wolfking.jeesite.modules.sd.entity.OrderCrush;
import com.wolfking.jeesite.modules.sd.entity.OrderItem;
import com.wolfking.jeesite.modules.sd.entity.viewModel.HistoryPlanOrderModel;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderCrushSearchVM;
import com.wolfking.jeesite.modules.sd.service.OrderCrushService;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.KefuTypeEnum;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.service.SystemService;
import com.wolfking.jeesite.modules.sys.utils.SeqUtils;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providermd.service.*;
import com.wolfking.jeesite.ms.service.sys.MSDictService;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresUser;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 突击单控制层
 *
 */
@Controller
@RequestMapping(value = "${adminPath}/sd/order/crush/")
@Slf4j
public class OrderCrushController extends BaseController
{
	private static final String MODEL_ATTR_PAGE = "page";
	private static final String MODEL_ATTR_ENTITY = "orderCrush";
	private static final String MODEL_ATTR_SERVICETYPES = "orderServiceTypes";

	@Autowired
	private OrderCrushService crushService;

	@Autowired
	private SystemService systemService;

	@Autowired
	private ProductCategoryService productCategoryService;

	@Autowired
	private OrderService orderService;

	@Autowired
	private AreaService areaService;

	@Autowired
	private ServicePointService servicePointService;

	@Autowired
	private MSCustomerProductService msCustomerProductService;

	@Autowired
	private MSDictService msDictService;

	@Autowired
	private RedisUtils redisUtils;

	@Autowired
	private MSProductService msProductService;

	@Autowired
	private MSServicePointService msServicePointService;

	@Autowired
	private MSRegionPermissionService msRegionPermissionService;

	@Autowired
	private MSDepositLevelService msDepositLevelService;

	//region 列表

	/**
	 * 设置必须的查询条件
	 * @param user  当前帐号
	 * @param searchModel   查询条件
	 * @param initMonths    初始最小查询时间段(月)
	 * @param maxDays   最大查询范围(天)
	 * @param searchByCompleteDateRange by结案日期查询开关
	 * @param maxCompleteDays 结案最大查询范围(天)
	 */
	private OrderCrushSearchVM setSearchModel(Model model, User user, OrderCrushSearchVM searchModel, int initMonths, int maxDays, boolean searchByCompleteDateRange, int maxCompleteDays) {
		if (searchModel == null) {
			searchModel = new OrderCrushSearchVM();
		}
		Area area = searchModel.getArea();
		if(area == null){
			area = new Area(0L);
			searchModel.setArea(area);
		}
		if(area.getParent()==null || area.getParent().getId() == null){
			area.setParent(new Area(0L));
		}
		//客服主管
		boolean isServiceSupervisor = user.getRoleEnNames().contains("Customer service supervisor");
		Date now = new Date();

		//申请日期
		if (searchModel.getBeginDate() == null) {
			searchModel.setEndDate(DateUtils.getDateEnd(now));
			searchModel.setBeginDate(DateUtils.getStartDayOfMonth(DateUtils.addMonth(now, 0 - initMonths)));
		} else {
			searchModel.setEndDate(DateUtils.getDateEnd(searchModel.getEndDate()));
		}
		//检查最大时间范围
		if(maxDays > 0){
			Date maxDate = DateUtils.addDays(searchModel.getBeginDate(),maxDays-1);
			maxDate = DateUtils.getDateEnd(maxDate);
			if(DateUtils.pastDays(maxDate,searchModel.getEndDate())>0){
				searchModel.setEndDate(maxDate);
			}
		}
		//quarters
		Date startDate = DateUtils.addMonth(searchModel.getBeginDate(),-1);
		List<String> quarters = QuarterUtils.getQuarters(startDate,searchModel.getEndDate());
		if(!org.springframework.util.ObjectUtils.isEmpty(quarters)){
			searchModel.setQuarters(quarters);
		}
		//vip客服查询自己负责的单，by客户+区域+品类
		//1.by 客户，前端客户已按客服筛选了
		if(user.isKefu()){
			if(user.getSubFlag() == KefuTypeEnum.VIPKefu.getCode()){
				//vip客服
				searchModel.setSubQueryUserCustomer(1);//指派客户，关联sys_user_customer
				searchModel.setRushType(null);//忽略突击区域
				searchModel.setCreateBy(user);//*
			} else if(user.getSubFlag() == KefuTypeEnum.Kefu.getCode()){
				//普通客服，不能查询vip客户订单
				searchModel.setCustomerType(0);//不能查询vip客户订单
				searchModel.setRushType(0); //不能查看突击区域订单
			}else if(user.getSubFlag() == KefuTypeEnum.Rush.getCode()){
				//突击客服，只看自己负责的单
				searchModel.setCustomerType(null);//不受客户类型限制
				searchModel.setRushType(1);//查看突击区域订单
			} else {
				//超级客服
				//查询所有客户订单，包含Vip客户
				searchModel.setCustomerType(null); //也可查看Vip客户订单
				searchModel.setRushType(null);//可查看突击区域订单
			}
		}else{
			//其他类型帐号，不限制客户及突击区域订单
			searchModel.setCustomerType(null);
			searchModel.setRushType(null);
		}
		//2.by 区域
		String checkRegion = crushService.loadAndCheckUserRegions(searchModel,user);
		if(StringUtils.isNotBlank(checkRegion)){
			searchModel.setIsValid(false);
			return searchModel;
		}
		Boolean loadResult = loadProductCategories(model,searchModel,user);
		if(!loadResult){
			searchModel.setIsValid(false);
			return searchModel;
		}
		return searchModel;
	}

	/**
	 * 检查订单号，手机号输入
	 * @param searchModel
	 * @param model
	 * @return
	 */
	private Boolean checkOrderNoAndPhone(OrderCrushSearchVM searchModel,Model model,Page page){
		if(searchModel == null){
			return true;
		}
		int noSearchType = searchModel.getCrushNoSearchType();
		if (noSearchType != 1 && StringUtils.isNotBlank(searchModel.getCrushNo())){
			addMessage(model, "错误：请输入正确的突击单号");
			model.addAttribute(MODEL_ATTR_PAGE, page);
			model.addAttribute(MODEL_ATTR_ENTITY, searchModel);
			return false;
		}
		//检查订单号
		searchModel.setOrderNo(StringUtils.trimToEmpty(searchModel.getOrderNo()));
		if(StringUtils.isNotBlank(searchModel.getOrderNo())) {
			int orderSerchType = searchModel.getOrderNoSearchType();
			if (orderSerchType != 1) {
				addMessage(model, "错误：请输入正确的订单号");
				model.addAttribute(MODEL_ATTR_PAGE, page);
				model.addAttribute(MODEL_ATTR_ENTITY, searchModel);
				return false;
			}else{
				//quarter
				if(StringUtils.isBlank(searchModel.getQuarter())) {
					searchModel.setQuarter(QuarterUtils.getOrderQuarterFromNo(searchModel.getOrderNo()));
				}
				if(StringUtils.isNotBlank(searchModel.getQuarter())) {
					//检查分片
					try {
						Date goLiveDate = OrderUtils.getGoLiveDate();
						String[] quarters = DateUtils.getQuarterRange(goLiveDate, new Date());
						if (quarters.length == 2) {
							int start = StringUtils.toInteger(quarters[0]);
							int end = StringUtils.toInteger(quarters[1]);
							if (start > 0 && end > 0) {
								int quarter = StringUtils.toInteger(searchModel.getQuarter());
								if (quarter < start || quarter > end) {
									addMessage(model, "错误：请输入正确的订单号,日期超出范围");
									model.addAttribute(MODEL_ATTR_PAGE, page);
									model.addAttribute(MODEL_ATTR_ENTITY, searchModel);
									return false;
								}
							}
						}
					} catch (Exception e) {
						log.error("检查分片错误,orderNo:{}", searchModel.getOrderNo(), e);
					}
				}
			}
		}
		return true;
	}



	/**
	 * 处理中列表
	 */
	@RequestMapping(value = "list")
	public String crushList(OrderCrushSearchVM orderCrush, HttpServletRequest request, HttpServletResponse response, Model model) {
		String viewForm = "modules/sd/crush/crushList";
		Page<OrderCrush> page = new Page<OrderCrush>(request,response);
		User user = UserUtils.getUser();
		orderCrush = setSearchModel(model, user,orderCrush,3,365,false,0);
		if(!orderCrush.getIsValid()){
			model.addAttribute(MODEL_ATTR_PAGE, page);
			model.addAttribute(MODEL_ATTR_ENTITY, orderCrush);
			return viewForm;
		}
		Boolean isValid = checkOrderNoAndPhone(orderCrush,model,page);
		if(!isValid){
			model.addAttribute(MODEL_ATTR_PAGE, page);
			model.addAttribute(MODEL_ATTR_ENTITY, orderCrush);
			return viewForm;
		}
		try {
			//查询
			//查询未完成的突击单
			orderCrush.setStatus(OrderCrush.STATUS_NEW);
			page = crushService.findPageList(new Page<OrderCrushSearchVM>(request, response), orderCrush);
		} catch (Exception e) {
			addMessage(model, "查询错误：" + e.getMessage());
		}
		model.addAttribute(MODEL_ATTR_PAGE, page);
		model.addAttribute(MODEL_ATTR_ENTITY, orderCrush);
		return viewForm;
	}

	/**
	 * 突击单处理之完成列表
	 */
	@RequestMapping(value = "closelist")
	public String crushCloselist(OrderCrushSearchVM orderCrush, HttpServletRequest request, HttpServletResponse response, Model model) {

		String viewForm = "modules/sd/crush/closeList";
		Page<OrderCrush> page = new Page<OrderCrush>(request,response);
		User user = UserUtils.getUser();
		orderCrush = setSearchModel(model, user,orderCrush,3,365,false,0);
		if(!orderCrush.getIsValid()){
			model.addAttribute(MODEL_ATTR_PAGE, page);
			model.addAttribute(MODEL_ATTR_ENTITY, orderCrush);
			return viewForm;
		}
		Boolean isValid = checkOrderNoAndPhone(orderCrush,model,page);
		if(!isValid){
			model.addAttribute(MODEL_ATTR_PAGE, page);
			model.addAttribute(MODEL_ATTR_ENTITY, orderCrush);
			return viewForm;
		}
		try {
			//查询
			orderCrush.setStatus(OrderCrush.STATUS_CLOSED);//完成
			page = crushService.findPageList(new Page<OrderCrushSearchVM>(request, response), orderCrush);
		} catch (Exception e) {
			addMessage(model, "查询错误：" + e.getMessage());
		}
		model.addAttribute(MODEL_ATTR_PAGE, page);
		model.addAttribute(MODEL_ATTR_ENTITY, orderCrush);
		return viewForm;
	}

	/**
	 * 突击单之历史派单列表
	 */
	@RequiresUser
	@RequestMapping(value = "historyPlanList")
	public String curshHistoryPlanList(OrderCrushSearchVM orderCrush,HttpServletRequest request, HttpServletResponse response, Model model) {
		String viewForm = "modules/sd/crush/historyPlanOrderList";
		Page<HistoryPlanOrderModel> page = new Page<>(request, response);
		page.setPageSize(20);
		Area area;
		if(orderCrush == null){
			orderCrush = new OrderCrushSearchVM();
			area = new Area(0L);
			area.setParent(new Area(0L));
			orderCrush.setArea(area);
			model.addAttribute("page", page);
			model.addAttribute("orderCrush", orderCrush);
			return viewForm;
		}
		area = orderCrush.getArea();
		if(area == null || area.getId() == null){
			area = new Area(0L);
			area.setParent(new Area(0L));
			orderCrush.setArea(area);
		}else if(area.getParent() == null || area.getParent().getId() == null){
			area.setParent(new Area(0L));
		}
		//date
		if (orderCrush.getBeginDate() == null) {
			orderCrush.setBeginDate(DateUtils.getStartDayOfMonth(DateUtils.addMonth(new Date(), -3)));
		}
		//完成日期
		if (orderCrush.getEndDate() == null) {
			orderCrush.setEndDate(DateUtils.getDateEnd(new Date()));
		} else {
			orderCrush.setEndDate(DateUtils.getDateEnd(orderCrush.getEndDate()));
		}
		page = crushService.getHistoryPlanListForCrush(page, orderCrush);
		model.addAttribute("page", page);
		model.addAttribute("orderCrush", orderCrush);
		return viewForm;
	}

	/**
	 * [Ajax]客服订单详情-突击单列表
	 * @param orderId	订单id
	 */
	@ResponseBody
	@RequestMapping(value = "ajax/list")
	public AjaxJsonEntity orderCrushList(@RequestParam String orderId, @RequestParam String quarter, HttpServletRequest request, HttpServletResponse response)
	{
		response.setContentType("application/json; charset=UTF-8");
		User user = UserUtils.getUser();
		if(user == null || user.getId()==null){
			AjaxJsonEntity jsonEntity =  AjaxJsonEntity.fail("登录超时，请重新登录。",null);
			jsonEntity.setLogin(false);
			return jsonEntity;
		}
		try
		{
			Long lorderId = Long.valueOf(orderId);
			if(lorderId == null || lorderId <=0){
				return AjaxJsonEntity.fail("订单参数错误",null);
			}
			List<OrderCrush> list = crushService.findCrushListOfOrder(lorderId,"",quarter);
			return AjaxJsonEntity.success("",list==null?Lists.newArrayList():list);
		} catch (Exception e)
		{
			return AjaxJsonEntity.fail(ExceptionUtils.getRootCauseMessage(e),null);
		}
	}

	/**
	 * 根据区县及街道获取网点列表
	 * @param area
	 * @param subArea
	 * @return
	 * @throws RuntimeException
	 */
	private List<ServicePoint> getCrushServicePointList(Area area,Area subArea, Long productCategoryId) throws RuntimeException {
		ServicePoint servicePoint = new ServicePoint();
		servicePoint.setArea(area);
		if(subArea != null && subArea.getId() != null && subArea.getId().intValue() > 3){
			// 只有大于3的区域id才有真正意义
			servicePoint.setSubArea(subArea);
		}else{
			servicePoint.setSubArea(null);
		}
		servicePoint.setProductCategoryId(productCategoryId == null ? 0 : productCategoryId);
		Page<ServicePoint> page = new Page<ServicePoint>(1,500);
		page = servicePointService.findServicePointListForPlanNew(page, servicePoint);
		List<ServicePoint> servicePoints = Lists.newArrayList();
		if (!org.springframework.util.ObjectUtils.isEmpty(page.getList())){
			servicePoints = page.getList();
		}
		return servicePoints;
	}

	//endregion

	//region 操作

	/**
	 * 读取用户产品类别权限数据
	 * @param model
	 * @param searchModel
	 * @param user
	 * @return
	 */
	private Boolean loadProductCategories(Model model, OrderCrushSearchVM searchModel, User user) {
		List<ProductCategory> list = productCategoryService.findAllList();
		if (list == null) {
			model.addAttribute("categories", Lists.newArrayList());
			return true;
		}else if(list.size() == 0){
			model.addAttribute("categories", list);
			return true;
		}
		Supplier<Stream<ProductCategory>> streamSupplier = () -> list.stream();
		List<ProductCategory> categories = streamSupplier.get().filter(t -> t.getDelFlag().equals(0)).collect(Collectors.toList());
		boolean isKefu = user.isKefu();
		boolean isInnerCount = user.isInnerAccount();
		if(isKefu || isInnerCount){
			List<Long> userProductCategoryList = systemService.getAuthorizedProductCategoryIds(user.getId());
			if(org.springframework.util.ObjectUtils.isEmpty(userProductCategoryList)){
				model.addAttribute("categories", list);
				addMessage(model, "您未开通产品类目权限，请联系管理员");
				return false;
			}
			//客服只能看到自己的品类
			if(!org.springframework.util.ObjectUtils.isEmpty(userProductCategoryList)){
				searchModel.setUserProductCategoryList(userProductCategoryList);
				if(!org.springframework.util.ObjectUtils.isEmpty(categories)){
					categories = streamSupplier.get().filter(t-> userProductCategoryList.contains(t.getId())).collect(Collectors.toList());
				}
				if(searchModel.getProductCategoryId()!=null && searchModel.getProductCategoryId() >0 && !userProductCategoryList.contains(searchModel.getProductCategoryId())){
					model.addAttribute("categories", categories);
					ProductCategory productCategory = streamSupplier.get().filter(t-> t.getId().equals(searchModel.getProductCategoryId())).findFirst().orElse(null);
					addMessage(model, productCategory==null?"您未开通选择的产品类目权限":"您未开通" + productCategory.getName() + "的产品类目权限");
					return false;
				}
			}
		}
		model.addAttribute("categories", categories);
		return true;
	}

	/**
	 * 新增/修改窗口
	 * @param orderId	订单id
	 */
	@RequiresPermissions(value="sd:orderCrush:edit")
	@RequestMapping(value = "form", method = RequestMethod.GET)
	public String form(@RequestParam(required = false) String id,@RequestParam String orderId, @RequestParam(required = false) String quarter, Model model) {
		User user = UserUtils.getUser();
		Long lid = null;
		OrderCrush orderCrush = new OrderCrush();
		String formView = "modules/sd/crush/crushForm";
		if(StringUtils.isBlank(id) && StringUtils.isBlank(orderId)) {
			return crushResult(orderCrush, model, "参数为空", formView);
		}
		Long lorderId = Long.valueOf(orderId);
		if(StringUtils.isBlank(id)){

			Order order = orderService.getOrderById(Long.valueOf(orderId),quarter, OrderUtils.OrderDataLevel.CONDITION,true);
			if (order == null || order.getOrderCondition() == null) {
				return crushResult(orderCrush, model, "系统繁忙，读取订单信息失败，请稍后重试", formView);
			}
			OrderCondition orderCondition = order.getOrderCondition();
			if (orderCondition.getRushOrderFlag() == 1){
				return crushResult(orderCrush, model, "订单已经有在突击中的突击单。", formView);
			}
			//servicepoint list
			List<ServicePoint> servicePoints;
			try {
				//突击单，不通过街道筛选
				servicePoints = getCrushServicePointList(orderCondition.getArea(), null, orderCondition.getProductCategoryId());
			}catch (Exception e){
				return crushResult(orderCrush, model, "系统繁忙，读取网点信息失败，请稍后重试。", formView);
			}
			//如果有暂存的突击单就返回暂存的突击单
			if (orderCondition.getRushOrderFlag() == 3) {
				OrderCrush entity = crushService.getTempSaveOrderCrush(lorderId, quarter);
				if (entity != null && entity.getId() != null) {
					entity.setUserName(orderCondition.getUserName());
					entity.setUserPhone(orderCondition.getServicePhone());
					entity.setUserAddress(orderCondition.getArea().getName() + orderCondition.getServiceAddress());
					entity.setCustomer(orderCondition.getCustomer());
					entity.setArea(orderCondition.getArea());
					entity.setServicePoints(servicePoints);
					return crushResult(entity, model, null, formView);
				}
			}

			orderCrush.setAction(0);//new
			orderCrush.setOrderId(lorderId);
			orderCrush.setQuarter(order.getQuarter());
			orderCrush.setOrderNo(order.getOrderNo());
			orderCrush.setUserName(order.getOrderCondition().getUserName());
			orderCrush.setUserPhone(order.getOrderCondition().getServicePhone());
			orderCrush.setUserAddress(order.getOrderCondition().getArea().getName()+order.getOrderCondition().getServiceAddress());
			orderCrush.setArea(order.getOrderCondition().getArea());
			orderCrush.setCustomer(order.getOrderCondition().getCustomer());
			orderCrush.setProductCategoryId(order.getOrderCondition().getProductCategoryId());//2019-10-14
			orderCrush.setCreateBy(user);
			orderCrush.setCreateDate(new Date());
			orderCrush.setServicePoints(servicePoints);
			orderCrush.setOrderServiceType(order.getOrderCondition().getOrderServiceType());//2020-09-19 订单类型
		}else{
			//edit
			try {
				lid = Long.valueOf(id);
			}catch (Exception e){
				lid = 0l;
			}
			if(lid<=0){
				return crushResult(orderCrush, model, "突击单参数类型错误。", formView);
			}
			orderCrush = crushService.getOrderCrush(lid,quarter,false);
			if(orderCrush ==null){
				return crushResult(orderCrush, model, "系统繁忙，读取突击单失败或不存在，请稍后重试。", formView);
			}
			else{
				if(orderCrush.getStatus()==1){
					return crushResult(orderCrush, model, "此突击单已完成，请稍后重试。", formView);
				}
				orderCrush.setAction(1);
			}
		}
		return crushResult(orderCrush, model, null, formView);
	}

	/**
	 * 突击错误
	 * @param orderCrush
	 * @param model
	 * @param errorMsg
	 * @param formView
	 * @return
	 */
	private String crushResult(OrderCrush orderCrush, Model model, String errorMsg, String formView) {
		if(StringUtils.isNotBlank(errorMsg)) {
			addMessage(model, errorMsg);
			model.addAttribute("canAction", false);
		}else{
			model.addAttribute("canAction", true);
		}
		model.addAttribute("orderCrush", orderCrush==null?new OrderCrush():orderCrush);
		return formView;
	}

	/**
	 * 提交申请
	 */
	@RequiresPermissions(value="sd:orderCrush:edit")
	@ResponseBody
	@RequestMapping(value = "save")
	public AjaxJsonEntity save(OrderCrush orderCrush, HttpServletRequest request, HttpServletResponse response, Model model) {
		response.setContentType("application/json; charset=UTF-8");
		if(orderCrush == null){
			return AjaxJsonEntity.fail("提交表单为空",null);
		}
		User user = UserUtils.getUser();
		if(user==null || user.getId()==null){
			AjaxJsonEntity jsonEntity =  AjaxJsonEntity.fail("登录超时，请重新登录。",null);
			jsonEntity.setLogin(false);
			return jsonEntity;
		}
		if (!beanValidator(model, orderCrush)){
			return AjaxJsonEntity.fail(model.containsAttribute("message")?(String) model.asMap().get("message"):"输入错误，请检查。",null);
		}

		try{

			if ((orderCrush.getId() == null || orderCrush.getId() == 0) && crushService.hasOpenOrderCrush(orderCrush.getOrderId(),orderCrush.getQuarter())){
				return AjaxJsonEntity.fail("保存突击单失败:订单已经有在突击中的突击单了",null);
			}
			boolean isNew = true;
			if(orderCrush.getId() != null && orderCrush.getId() >0 ) {
				isNew = false;
			}
			//province & city
			if(isNew) {
				Map<Integer, Area> parents = areaService.getAllParentsWithDistrict(orderCrush.getArea().getId());
				if (org.springframework.util.ObjectUtils.isEmpty(parents) || parents.size() < 3) {
					return AjaxJsonEntity.fail("保存突击单失败:读取省市区域错误，请重试",null);
				}
				orderCrush.setCity(parents.get(Area.TYPE_VALUE_CITY));
				orderCrush.setProvince(parents.get(Area.TYPE_VALUE_PROVINCE));
			}else{
				orderCrush.setIsNewRecord(true);//暂存后提交
			}
			orderCrush.setStatus(OrderCrush.STATUS_NEW);
			if(orderCrush.getOrderServiceType() == null){
				orderCrush.setOrderServiceType(0);
			}
			if(isNew){
				//new
				orderCrush.setCreateBy(user);
				orderCrush.setCreateDate(new Date());
				String no = SeqUtils.NextSequenceNo("CrushNo");
				if(StringUtils.isBlank(no)){
					no =  SeqUtils.NextSequenceNo("CrushNo");
				}
				if (StringUtils.isBlank(no)){
					return AjaxJsonEntity.fail("突击单号产生失败，请重试",null);
				}
				orderCrush.setCrushNo(no);
			}else{
				orderCrush.setUpdateBy(user);
				orderCrush.setUpdateDate(new Date());
			}
			if (orderCrush.getId() == null || orderCrush.getId() <= 0) {
				Integer itemNo = crushService.getMaxTimes(orderCrush.getOrderId(),orderCrush.getQuarter());
				if(itemNo == null){
					itemNo = 1;
				}else{
					itemNo = itemNo + 1;
				}
				orderCrush.setItemNo(itemNo);
			}
			crushService.saveOrderCrush(orderCrush);
		}catch (Exception e){
			if(orderCrush.getAction()==0 && StringUtils.isNotBlank(orderCrush.getCrushNo())){
				try{
					SeqUtils.reputSequenceNo("CrushNo",orderCrush.getCreateDate(),orderCrush.getCrushNo());
				}catch (Exception e1){}
			}
			return AjaxJsonEntity.fail("保存突击单失败:" + ExceptionUtils.getRootCauseMessage(e),null);
		}
		return AjaxJsonEntity.success("",null);
	}

	/**
	 * 创建的时候暂存
	 */
	@RequiresPermissions(value="sd:orderCrush:edit")
	@ResponseBody
	@RequestMapping(value = "tempCreateSave")
	public AjaxJsonEntity tempCreateSave(OrderCrush orderCrush, HttpServletRequest request, HttpServletResponse response, Model model) {
		response.setContentType("application/json; charset=UTF-8");
		if(orderCrush == null){
			return AjaxJsonEntity.fail("提交表单为空",null);
		}
		User user = UserUtils.getUser();
		if(user==null || user.getId()==null){
			AjaxJsonEntity jsonEntity =  AjaxJsonEntity.fail("登录超时，请重新登录。",null);
			jsonEntity.setLogin(false);
			return jsonEntity;
		}
		if (!beanValidator(model, orderCrush))
		{
			return AjaxJsonEntity.fail(model.containsAttribute("message")?(String) model.asMap().get("message"):"输入错误，请检查。",null);
		}

		try{

			if ((orderCrush.getId() == null || orderCrush.getId() == 0) && crushService.hasOpenOrderCrush(orderCrush.getOrderId(),orderCrush.getQuarter())){
				return AjaxJsonEntity.fail("暂存突击单失败:订单已经有在突击中的突击单了",null);
			}
			boolean isNew = true;
			if(orderCrush.getId() != null && orderCrush.getId() >0 ) {
				isNew = false;
			}
			//province & city
			if(isNew) {
				Map<Integer, Area> parents = areaService.getAllParentsWithDistrict(orderCrush.getArea().getId());
				if (org.springframework.util.ObjectUtils.isEmpty(parents) || parents.size() < 3) {
					return AjaxJsonEntity.fail("暂存突击单失败:读取省市区域错误",null);
				}
				orderCrush.setCity(parents.get(Area.TYPE_VALUE_CITY));
				orderCrush.setProvince(parents.get(Area.TYPE_VALUE_PROVINCE));
			}else{
				orderCrush.setIsNewRecord(true);//暂存
			}
			orderCrush.setStatus(OrderCrush.STATUS_TEMPCREATE);
			if(isNew){
				//new
				orderCrush.setCreateBy(user);
				orderCrush.setCreateDate(new Date());
				String no = SeqUtils.NextSequenceNo("CrushNo");
				if(StringUtils.isBlank(no)){
					no =  SeqUtils.NextSequenceNo("CrushNo");
				}
				if (StringUtils.isBlank(no)){
					return AjaxJsonEntity.fail("突击单号产生失败，请重试!",null);
				}
				orderCrush.setCrushNo(no);
				Integer itemNo = crushService.getMaxTimes(orderCrush.getOrderId(),orderCrush.getQuarter());
				if(itemNo == null){
					itemNo = 1;
				}else{
					itemNo = itemNo + 1;
				}
				orderCrush.setItemNo(itemNo);
			}else{
				orderCrush.setUpdateBy(user);
				orderCrush.setUpdateDate(new Date());
			}

			crushService.tempSaveOrderCrush(orderCrush);
		}catch (Exception e){
			if(orderCrush.getAction()==0 && StringUtils.isNotBlank(orderCrush.getCrushNo())){
				try{
					SeqUtils.reputSequenceNo("CrushNo",orderCrush.getCreateDate(),orderCrush.getCrushNo());
				}catch (Exception e1){}
			}
			return AjaxJsonEntity.fail("保存突击单失败:" + ExceptionUtils.getRootCauseMessage(e),null);
		}
		return AjaxJsonEntity.success("",null);
	}

	/**
	 * 完成窗口
	 * @param id		突击单id
	 * @param quarter 	分片
	 */
	@RequiresPermissions(value="sd:orderCrush:edit")
	@RequestMapping(value = "close", method = RequestMethod.GET)
	public String closeForm(@RequestParam String id, @RequestParam(required = false) String quarter, Model model) {
		User user = UserUtils.getUser();
		OrderCrush orderCrush = new OrderCrush();
		String formView = "modules/sd/crush/closeForm";
		if(StringUtils.isBlank(id)){
			return crushResult(orderCrush,model,"参数为空。",formView);
		}
		Long lid = null;
		try {
			lid = Long.valueOf(id);
		}catch (Exception e){
			lid = 0l;
		}
		if (lid == null || lid <= 0){
			return crushResult(orderCrush,model,"突击单参数类型错误。",formView);
		}
		try {
			orderCrush = crushService.getOrderCrush(lid,quarter,true);
			if(orderCrush.getStatus()==1)
			{
				return crushResult(orderCrush,model,"该突击单已经完成，请确认。",formView);
			}
		}catch (Exception e){
			log.error("突击单读取错误-id:{} ,quarter:{}",id,quarter,e);
			return crushResult(orderCrush,model,"突击单读取错误,请重试。",formView);
		}
		if(orderCrush ==null){
			return crushResult(null,model,"读取突击单失败或不存在，请重试。",formView);
		}
		//servicepoint list
		int degree=0;
		List<ServicePoint> servicePoints;
		try {
			Long productCategoryId = orderService.getOrderProductCategoryId(orderCrush.getQuarter(), orderCrush.getOrderId());
			List<Dict> dictList = MSDictUtils.getDictList("degreeType");
			if(dictList!=null && dictList.size()>0){
				degree = dictList.get(0).getIntValue();
			}
			//servicePoints = getCrushServicePointList(orderCrush.getArea(), null, productCategoryId);
			servicePoints = findCrushServicePointByDegree(orderCrush.getArea(),productCategoryId,degree);
		}catch (Exception e){
			return crushResult(orderCrush, model, "系统繁忙，读取网点信息失败，请稍后重试。", formView);
		}
		orderCrush.setServicePoints(servicePoints);
		//load area
		try {
			Map<Integer, Area> areaMap = areaService.getAllParentsWithDistrict(orderCrush.getArea().getId());
			if(areaMap.containsKey(Area.TYPE_VALUE_PROVINCE)){
				orderCrush.setProvince(areaMap.get(Area.TYPE_VALUE_PROVINCE));
			}
			if(areaMap.containsKey(Area.TYPE_VALUE_CITY)){
				orderCrush.setCity(areaMap.get(Area.TYPE_VALUE_CITY));
			}
			if(areaMap.containsKey(Area.TYPE_VALUE_COUNTY)){
				orderCrush.setArea(areaMap.get(Area.TYPE_VALUE_COUNTY));
			}
			//地址处理
			StringBuilder address = new StringBuilder(orderCrush.getUserAddress());
			StringBuilder sb = new StringBuilder(address.length());
			sb.append(address.toString()
					.replaceAll(orderCrush.getProvince().getName(),"")
					.replaceAll(orderCrush.getCity().getName(),"")
					.replaceAll(orderCrush.getArea().getName(),"")
			);
			if(orderCrush.getSubArea().getId()>0){
				address.setLength(0);
				address.append(sb.toString().replaceAll(orderCrush.getSubArea().getName(),""));
				orderCrush.setUserAddress(address.toString().trim());
			}else{
				orderCrush.setUserAddress(sb.toString().trim());
			}
			sb = null;
			address = null;
			model.addAttribute("degreeType",degree);

		}catch (Exception e){
			log.error("读取突击单所在区域错误,area id:{}",(orderCrush.getArea()==null || orderCrush.getArea().getId() == null)?"null":orderCrush.getArea().getId(),e);
		}
		return crushResult(orderCrush,model,null,formView);
	}

	/**
	 * 提交完成突击单
	 */
	@RequiresPermissions(value="sd:orderCrush:edit")
	@ResponseBody
	@RequestMapping(value = "saveClose")
	public AjaxJsonEntity saveClose(OrderCrush orderCrush, HttpServletRequest request, HttpServletResponse response, Model model) {
		response.setContentType("application/json; charset=UTF-8");
		if(orderCrush == null){
			return AjaxJsonEntity.fail("提交表单为空",null);
		}
		User user = UserUtils.getUser();
		if(user==null || user.getId()==null){
			AjaxJsonEntity jsonEntity =  AjaxJsonEntity.fail("登录超时，请重新登录。",null);
			jsonEntity.setLogin(false);
			return jsonEntity;
		}
		String json = new String("");
		try{

			OrderCrush dbOrderCrush = crushService.getOrderCrushNoDetailById(orderCrush.getId(),orderCrush.getQuarter());
			if(dbOrderCrush.getStatus()==1)
			{
				return AjaxJsonEntity.fail("该突击单已经完成，请刷新重试",null);
			}
			orderCrush.setStatus(OrderCrush.STATUS_CLOSED);
			orderCrush.setCloseDate(new Date());
			orderCrush.setCloseBy(user);
			//计算时效
			double timeliness = DateUtils.differTime(dbOrderCrush.getCreateDate().getTime(),orderCrush.getCloseDate().getTime());
			orderCrush.setTimeLiness(timeliness);
			crushService.closeOrderCursh(orderCrush);
		}catch (Exception e){
			return AjaxJsonEntity.fail("保存突击单失败:" + ExceptionUtils.getRootCauseMessage(e),null);
		}
		return AjaxJsonEntity.success("",null);
	}

	/**
	 * 暂存突击单内容(处理中)
	 */
	@RequiresPermissions(value="sd:orderCrush:edit")
	@ResponseBody
	@RequestMapping(value = "tempSave")
	public AjaxJsonEntity tempSave(OrderCrush orderCrush, HttpServletRequest request, HttpServletResponse response, Model model) {
		response.setContentType("application/json; charset=UTF-8");

		if(orderCrush == null){
			return AjaxJsonEntity.fail("提交表单为空",null);
		}
		User user = UserUtils.getUser();
		if(user==null || user.getId()==null){
			AjaxJsonEntity jsonEntity =  AjaxJsonEntity.fail("登录超时，请重新登录。",null);
			jsonEntity.setLogin(false);
			return jsonEntity;
		}
		String json = new String();
		try{
			OrderCrush dbOrderCrush = crushService.getOrderCrush(orderCrush.getId(),orderCrush.getQuarter(),false);
			if(dbOrderCrush.getStatus()==1)
			{
				return AjaxJsonEntity.fail("该突击单已经完成，请刷新重试",null);
			}

			OrderCrush entity=new OrderCrush();
			entity.setId(orderCrush.getId());
			entity.setQuarter(orderCrush.getQuarter());
			//servicepoint list
			List<ServicePoint> servicePoints;
			try {
				Long productCategoryId = orderService.getOrderProductCategoryId(orderCrush.getQuarter(), orderCrush.getOrderId());
				servicePoints = getCrushServicePointList(dbOrderCrush.getArea(), null, productCategoryId);
			}catch (Exception e){
				return AjaxJsonEntity.fail("系统繁忙，读取网点信息失败，请稍后重试。",null);
			}
			entity.setServicePoints(servicePoints);
			entity.setCloseRemark(orderCrush.getCloseRemark());
			crushService.updateOrderCrush(entity);
		}catch (Exception e){
			return AjaxJsonEntity.fail("暂存突击单失败:" + ExceptionUtils.getRootCauseMessage(e),null);
		}
		return AjaxJsonEntity.success("",null);
	}

	/**
	 * 突击单浏览窗口
	 * @param id		突击单id
	 * @param quarter 	分片
	 */
	@RequestMapping(value = "view", method = RequestMethod.GET)
	public String viewForm(@RequestParam String id, @RequestParam(required = false) String quarter, Model model) {
		User user = UserUtils.getUser();
		OrderCrush orderCrush = new OrderCrush();
		String formView = "modules/sd/crush/viewForm";
		if(StringUtils.isBlank(id)){
			return crushResult(orderCrush,model,"参数为空。",formView);
		}
		Long lid = null;
		try {
			lid = Long.valueOf(id);
		}catch (Exception e){
			lid = 0l;
		}
		if (lid == null || lid <= 0){
			return crushResult(orderCrush,model,"突击单参数类型错误。",formView);
		}
		try {
			orderCrush = crushService.getOrderCrush(lid,quarter,false);
		}catch (Exception e){
			return crushResult(orderCrush,model,"突击单读取错误。",formView);
		}
		if(orderCrush ==null){
			return crushResult(orderCrush,model,"读取突击单失败或不存在，请重试。",formView);
		}
		if (orderCrush.getOrderId()>0) {
			Order order = orderService.getOrderById(orderCrush.getOrderId(), quarter, OrderUtils.OrderDataLevel.CONDITION, true);
			if (order == null || order.getOrderCondition() == null) {
				return crushResult(orderCrush,model,"系统繁忙：读取订单读取失败，稍后请重试。",formView);
			} else {
				orderCrush.setUserName(order.getOrderCondition().getUserName());
				orderCrush.setUserPhone(order.getOrderCondition().getServicePhone());
				orderCrush.setUserAddress(order.getOrderCondition().getArea().getName() + order.getOrderCondition().getServiceAddress());
				orderCrush.setCustomer(order.getOrderCondition().getCustomer());
			}
		}
		Map<Integer, Area> areaMap = areaService.getAllParentsWithDistrict(orderCrush.getArea().getId());
		if(areaMap.containsKey(Area.TYPE_VALUE_PROVINCE)){
			orderCrush.setProvince(areaMap.get(Area.TYPE_VALUE_PROVINCE));
		}
		if(areaMap.containsKey(Area.TYPE_VALUE_CITY)){
			orderCrush.setCity(areaMap.get(Area.TYPE_VALUE_CITY));
		}
		if(areaMap.containsKey(Area.TYPE_VALUE_COUNTY)){
			orderCrush.setArea(areaMap.get(Area.TYPE_VALUE_COUNTY));
		}
		//servicepoint list
		int degree = 0;
		List<ServicePoint> servicePoints;
		try {
			Long productCategoryId = orderService.getOrderProductCategoryId(orderCrush.getQuarter(), orderCrush.getOrderId());
			//servicePoints = getCrushServicePointList(orderCrush.getArea(), null, productCategoryId);
			List<Dict> dictList = MSDictUtils.getDictList("degreeType");
			if(dictList!=null && dictList.size()>0){
				degree = dictList.get(0).getIntValue();
			}
			//servicePoints = getCrushServicePointList(orderCrush.getArea(), null, productCategoryId);
			servicePoints = findCrushServicePointByDegree(orderCrush.getArea(),productCategoryId,degree);
		}catch (Exception e){
			return crushResult(orderCrush, model, "系统繁忙，读取网点信息失败，请稍后重试。", formView);
		}
		orderCrush.setServicePoints(servicePoints);
		model.addAttribute("degreeType",degree);
		return crushResult(orderCrush,model,"",formView);
	}
	//endregion

	/**
	 * 派单 form
	 *
	 * @param orderId
	 */
	@RequiresPermissions(value = { "sd:order:plan", "sd:order:engineerplan" }, logical = Logical.OR)
	@RequestMapping(value = "rushPlan", method = RequestMethod.GET)
	//@FormToken(save = true)
	public String rushPlan(String orderId,String quarter, @RequestParam(name = "crushPlanFlag", required = false, defaultValue = "0") Integer crushPlanFlag, HttpServletRequest request, Model model) {
		Order order = new Order();
		Long lorderId = Long.valueOf(orderId);
		if (lorderId == null || lorderId <= 0) {
			addMessage(model, "错误：订单号丢失");
			model.addAttribute("canSave", false);
			model.addAttribute("order", order);
			return "modules/sd/crush/rushOrderPlanForm";
		}
		String lockkey = String.format(RedisConstant.SD_ORDER_LOCK,orderId);
		if(redisUtils.exists(RedisConstant.RedisDBType.REDIS_LOCK_DB,lockkey)){
			addMessage(model,"错误:此订单正在处理中，请稍候重试，或刷新页面。");
			model.addAttribute("canSave", false);
			model.addAttribute("order", order);
			return "modules/sd/crush/rushOrderPlanForm";
		}

		order = orderService.getOrderById(lorderId, quarter,OrderUtils.OrderDataLevel.FEE, true);
		if(order == null || order.getOrderCondition() == null){
			addMessage(model, "错误：系统繁忙，读取订单失败，请重试。");
			model.addAttribute("canSave", false);
			model.addAttribute("order", order);
			return "modules/sd/crush/rushOrderPlanForm";
		}
		// 检查是否可以取消
		if (!order.canPlanOrder()) {
			addMessage(model, String.format("错误:订单：%s 无法派单，当前订单状态:",order.getOrderNo(),order.getOrderCondition().getStatus().getLabel()));
			model.addAttribute("canSave", false);
			model.addAttribute("order", order);
			return "modules/sd/crush/rushOrderPlanForm";
		}
		//厂商远程费标识 add on 2020-3-24
		OrderCondition orderCondition = order.getOrderCondition();
		//List<Long> productIds = order.getItems().stream().map(OrderItem::getProductId).collect(Collectors.toList());
		//Integer customerRemoteFee = msCustomerProductService.getRemoteFeeFlag(orderCondition.getCustomer().getId(),productIds);
		Integer customerRemoteFee = 1;
		// 检查是否为受控品类：自动同步应收远程费和其他费用
		Dict dict = MSDictUtils.getDictByValue(orderCondition.getProductCategoryId().toString(), OrderUtils.SYNC_CUSTOMER_CHARGE_DICT);
		if (dict != null && dict.getValue().equals(orderCondition.getProductCategoryId().toString())) {
			customerRemoteFee = 0;
		} else if(orderCondition.getCustomer()!=null && orderCondition.getCustomer().getId()>0){
			customerRemoteFee = orderCondition.getCustomer().getRemoteFeeFlag();
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
		return "modules/sd/crush/rushOrderPlanForm";
	}


	/**
	 * 突击处理 按区县/街道/品类 分页查询可派单列表
	 * 只查询level 1 ~ 5的,且status=10
	 */
	@RequiresUser
	@RequestMapping(value = "rushSelectForPlan")
	public String rushSelectForPlan(ServicePointModel servicePointModel, HttpServletRequest request, HttpServletResponse response, Model model) {
		String viewForm = "modules/sd/crush/rushServicePointSelectForPlan";
		Page<ServicePoint> page = new Page<>(request, response);
		page.setPageSize(8);
		if(servicePointModel.getArea() == null || servicePointModel.getArea().getId() == null || servicePointModel.getArea().getId() <= 0){
			addMessage(model, "区域参数无值");
			model.addAttribute("page", page);
			model.addAttribute("servicePoint", servicePointModel);
			return viewForm;
		}
		Area area = areaService.getFromCache(servicePointModel.getArea().getId(),Area.TYPE_VALUE_COUNTY);
		if(area == null){
			addMessage(model, "读取区域无返回内容，请重试");
			model.addAttribute("page", page);
			model.addAttribute("servicePoint", servicePointModel);
			return viewForm;
		}
		servicePointModel.setArea(area);
		Area city = areaService.getFromCache(area.getParentId(),Area.TYPE_VALUE_CITY);
		if(city == null){
			addMessage(model, "读取市无返回内容，请重试");
			model.addAttribute("page", page);
			model.addAttribute("servicePoint", servicePointModel);
			return viewForm;
		}
		servicePointModel.setCity(city);
		//}
		//街道
		if (servicePointModel.getSubArea() != null && servicePointModel.getSubArea().getId() != null && servicePointModel.getSubArea().getId().intValue() > 3) {
			// 只有大于3的区域id才有真正意义
			area = areaService.getTownFromCache(servicePointModel.getArea().getId(),servicePointModel.getSubArea().getId());
			if(area == null){
				addMessage(model, "读取街道无返回内容，请重试");
				model.addAttribute("page", page);
				model.addAttribute("servicePoint", servicePointModel);
				return viewForm;
			}
			servicePointModel.setSubArea(area);
		}else {
			servicePointModel.setSubArea(null);
		}
		if(servicePointModel.getDegree()==null){
			//获取排序最前的一个网点分级
			List<Dict> dictList = MSDictUtils.getDictList("degreeType");
			if(!ObjectUtils.isEmpty(dictList)){
				servicePointModel.setDegree(dictList.get(0).getIntValue());
			}
		}
		ServicePoint servicePoint = new ServicePoint();
		BeanUtils.copyProperties(servicePointModel, servicePoint);
		try {
			String addr = java.net.URLDecoder.decode(servicePointModel.getAddress(), "UTF-8");  //进行解码，会抛异常，直接捕获即可。
			servicePointModel.setAddress(addr);
		} catch (Exception ex){
		}
		page = servicePointService.findServicePointListForPlanNew(page, servicePoint);
		List<Long> ids = page.getList().stream().map(ServicePoint::getId).collect(Collectors.toList());
		Map<Long,ServicePointFinance> financeMap = servicePointService.getDepositByIds(ids);
		Map<Long,MDDepositLevel> depositLevelMap = msDepositLevelService.getAllLevelMap();
		ServicePointFinance servicePointFinance =null;
		MDDepositLevel depositLevel = null;
		for(ServicePoint entity:page.getList()){
			servicePointFinance = financeMap.get(entity.getId());
			if(servicePointFinance!=null){
				ServicePointFinance finance = entity.getFinance();
				if(finance!=null){
					finance.setDeposit(servicePointFinance.getDeposit());
					finance.setDepositRecharge(servicePointFinance.getDepositRecharge());
				}else{
					entity.setFinance(servicePointFinance);
				}
			}
			depositLevel = depositLevelMap.get(entity.getMdDepositLevel().getId());
			if(depositLevel!=null){
				entity.setMdDepositLevel(depositLevel);
			}
		}
		//return selectServicePointResult(org.apache.commons.lang3.StringUtils.EMPTY,servicePointModel, model, "modules/md/servicePointSelectForPlan", page);
		model.addAttribute("page", page);
		model.addAttribute("servicePoint", servicePointModel);
		return viewForm;
	}


	/**
	 * 突击处理 按区县/街道/品类 分页查询停用网点列表
	 * 只查询level 1 ~ 5的,且status=20,30
	 */
	@RequiresUser
	@RequestMapping(value = "rushUnableSelectForPlan")
	public String rushUnableSelectForPlan(ServicePointModel servicePointModel, HttpServletRequest request, HttpServletResponse response, Model model) {
		String viewForm = "modules/sd/crush/rushUnableServicePointSelectForPlan";
		Page<ServicePoint> page = new Page<>(request, response);
		page.setPageSize(8);
		if(servicePointModel.getArea() == null || servicePointModel.getArea().getId() == null || servicePointModel.getArea().getId() <= 0){
			addMessage(model, "区域参数无值");
			model.addAttribute("page", page);
			model.addAttribute("servicePoint", servicePointModel);
			return viewForm;
		}
		//if (servicePointModel.getArea() != null && servicePointModel.getArea().getId() != null && StringUtils.isBlank(servicePointModel.getArea().getName())) {
		Area area = areaService.getFromCache(servicePointModel.getArea().getId(),Area.TYPE_VALUE_COUNTY);
		if(area == null){
			addMessage(model, "读取区域无返回内容，请重试");
			model.addAttribute("page", page);
			model.addAttribute("servicePoint", servicePointModel);
			return viewForm;
		}
		servicePointModel.setArea(area);
		Area city = areaService.getFromCache(area.getParentId(),Area.TYPE_VALUE_CITY);
		if(city == null){
			addMessage(model, "读取市无返回内容，请重试");
			model.addAttribute("page", page);
			model.addAttribute("servicePoint", servicePointModel);
			return viewForm;
		}
		servicePointModel.setCity(city);
		//}
		//街道
		if (servicePointModel.getSubArea() != null && servicePointModel.getSubArea().getId() != null && servicePointModel.getSubArea().getId().intValue() > 3) {
			// 只有大于3的区域id才有真正意义
			area = areaService.getTownFromCache(servicePointModel.getArea().getId(),servicePointModel.getSubArea().getId());
			if(area == null){
				addMessage(model, "读取街道无返回内容，请重试");
				model.addAttribute("page", page);
				model.addAttribute("servicePoint", servicePointModel);
				return viewForm;
			}
			servicePointModel.setSubArea(area);
		}else {
			servicePointModel.setSubArea(null);
		}
		ServicePoint servicePoint = new ServicePoint();
		BeanUtils.copyProperties(servicePointModel, servicePoint);
		try {
			String addr = java.net.URLDecoder.decode(servicePointModel.getAddress(), "UTF-8");  //进行解码，会抛异常，直接捕获即可。
			servicePointModel.setAddress(addr);
		} catch (Exception ex){
		}
		page = servicePointService.findUnbleSelectForPlan(page, servicePoint);
		List<Long> ids = page.getList().stream().map(ServicePoint::getId).collect(Collectors.toList());
		Map<Long,ServicePointFinance> financeMap = servicePointService.getDepositByIds(ids);
		Map<Long,MDDepositLevel> depositLevelMap = msDepositLevelService.getAllLevelMap();
		ServicePointFinance servicePointFinance =null;
		MDDepositLevel depositLevel = null;
		for(ServicePoint entity:page.getList()){
			servicePointFinance = financeMap.get(entity.getId());
			if(servicePointFinance!=null){
				ServicePointFinance finance = entity.getFinance();
				if(finance!=null){
					finance.setDeposit(servicePointFinance.getDeposit());
					finance.setDepositRecharge(servicePointFinance.getDepositRecharge());
				}else{
					entity.setFinance(servicePointFinance);
				}
			}
			depositLevel = depositLevelMap.get(entity.getMdDepositLevel().getId());
			if(depositLevel!=null){
				entity.setMdDepositLevel(depositLevel);
			}
		}
		model.addAttribute("page", page);
		model.addAttribute("servicePoint", servicePointModel);
		return viewForm;
	}


	/**
	 * 客服派单添加网点
	 * @param servicePoint
	 * @return
	 */
	@RequestMapping("rushAddServicePointForPlanForm")
	public String rushAddServicePointForPlanForm(ServicePoint servicePoint,Model model,HttpServletRequest request){
		Area county = areaService.getFromCache(servicePoint.getArea().getId());
		servicePoint.setArea(county);
		List<Area> subAreaList = areaService.findListByParent(Area.TYPE_VALUE_TOWN,servicePoint.getArea().getId());
		String categoryName = "";
		ProductCategory productCategory =productCategoryService.getFromCache(servicePoint.getProductCategoryId());
		if(productCategory!=null){
			categoryName = productCategory.getName();
		}
		List<Product> productList = msProductService.findSingleListByProductCategoryId(servicePoint.getProductCategoryId());
		servicePoint.setAreas(subAreaList);
		String layerIndex = request.getParameter("layerIndex");
		String parentLayerIndex = request.getParameter("parentLayerIndex");
		ServicePointModel servicePointModel = new ServicePointModel();
		servicePointModel.setLayerIndex(layerIndex);
		servicePointModel.setParentLayerIndex(parentLayerIndex);
		servicePointModel.setArea(servicePoint.getArea());
		servicePointModel.setSubArea(servicePoint.getSubArea());
		servicePointModel.setAddress(servicePoint.getAddress());
		servicePointModel.setProductCategoryId(servicePoint.getProductCategoryId());
		model.addAttribute("servicePoint",servicePoint);
		model.addAttribute("categoryName",categoryName);
		model.addAttribute("productList",productList);
		model.addAttribute("servicePointModel",servicePointModel);
		return "modules/sd/crush/rushServicePointForPlanFormAdd";
	}


	/**
	 * 开发网点保存网点
	 * @param servicePoint
	 * @return
	 */
	@RequestMapping("rushSaveServicePointForPlan")
	@ResponseBody
	public AjaxJsonEntity rushSaveServicePointForPlan(ServicePoint servicePoint,Model model){
		//servicePointService.insertServicePointForPlan(servicePoint);
		AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
		try {
			if (servicePoint.getArea() != null && StringUtils.isNoneBlank(servicePoint.getArea().getFullName())) {
				servicePoint.setAddress(servicePoint.getArea().getFullName() + " " + servicePoint.getSubAddress());
			}
			servicePointService.insertServicePointForPlan(servicePoint);
			ajaxJsonEntity.setSuccess(true);
		}catch (Exception e){
			log.error(e.getMessage());
			ajaxJsonEntity.setSuccess(false);
			ajaxJsonEntity.setMessage(e.getMessage());
		}
		return ajaxJsonEntity;
	}

	/**
	 * 突击单处理恢复网点
	 * @param servicePoint
	 * @return
	 */
	@RequestMapping("rushRecoverForPlan")
	@ResponseBody
	public AjaxJsonEntity rushRecoverForPlan(MDServicePoint servicePoint){
		AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
		if(servicePoint.getId()==null || servicePoint.getId()<=0){
			ajaxJsonEntity.setSuccess(false);
			ajaxJsonEntity.setMessage("网点id丢失,请检查");
		}
		User user = UserUtils.getUser();
		if(user==null || user.getId()==null || user.getId()<=0){
			ajaxJsonEntity.setSuccess(false);
			ajaxJsonEntity.setMessage("当前用户不存在,请重新登录");
		}
		servicePoint.setUpdateById(user.getId());
		servicePoint.setUpdateDate(new Date());
		servicePoint.setStatus(10);
		try {
			msServicePointService.updateStatusForPlan(servicePoint);
			ajaxJsonEntity.setSuccess(true);
		}catch (Exception e){
			ajaxJsonEntity.setSuccess(false);
			ajaxJsonEntity.setMessage(e.getMessage());
		}
		return ajaxJsonEntity;
	}

	/**
	 * 跳转修改地址页面
	 * @param orderId
	 * @param quarter
	 * @return
	 */
	@RequestMapping("updateAddressForm")
	public String updateAddressForm(Long orderId,String quarter,Long id,Model model){
	   String viewForm = "modules/sd/crush/updateAddressForm";
	   model.addAttribute("canSave",true);
	   Order order = new Order();
       if(orderId==null || orderId<=0){
		model.addAttribute("canSave",false);
       	addMessage(model,"错误：工单号丢失,请重试");
       	model.addAttribute("order", order);
		return viewForm;
	   }
	    order = orderService.getOrderById(orderId, quarter,OrderUtils.OrderDataLevel.FEE, true);
		if(order == null || order.getOrderCondition() == null){
			addMessage(model, "错误：系统繁忙，读取订单失败，请重试。");
			model.addAttribute("canSave", false);
			model.addAttribute("order", order);
			return viewForm;
		}
		Area area = areaService.getThreeLevelAreaById(order.getOrderCondition().getArea().getId());
		order.getOrderCondition().setAreaName(area.getParent().getParent().getName()+" "+area.getParent().getName()+" " +area.getName());
		if(order.getOrderCondition().getSubArea().getId()>3){
			order.getOrderCondition().setSubArea(areaService.getFromCache(order.getOrderCondition().getSubArea().getId()));
		}
		List<Area> subAreaList = areaService.findListByParent(Area.TYPE_VALUE_TOWN,area.getId());
		model.addAttribute("subAreaList",subAreaList);
		model.addAttribute("crushId",id);
		model.addAttribute("orderCondition", order.getOrderCondition());
        return viewForm;
	}

	/**
	 * 保持修改地址
	 * @param orderCondition
	 * @return
	 */
	@RequestMapping("updateAddress")
	@ResponseBody
	public AjaxJsonEntity updateAddress(OrderCondition orderCondition,Long crushId){
		AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
		if(orderCondition.getOrderId()==null || orderCondition.getOrderId()<=0){
			ajaxJsonEntity.setSuccess(false);
			ajaxJsonEntity.setMessage("工单号缺失,请检查");
			return ajaxJsonEntity;
		}
		User user = UserUtils.getUser();
		if(user==null || user.getId()==null || user.getId()<=0){
			ajaxJsonEntity.setSuccess(false);
			ajaxJsonEntity.setMessage("用户超时,请重新登录");
			return ajaxJsonEntity;
		}
		if(orderCondition.getSubArea()==null ||  orderCondition.getSubArea().getId()==null || orderCondition.getSubArea().getId()<=0){
			ajaxJsonEntity.setSuccess(false);
			ajaxJsonEntity.setMessage("请选择街道");
			return ajaxJsonEntity;
		}
		//String strCrushId = request.getParameter("crushId");
		/*if(StringUtils.isNotBlank(crushId)){
			ajaxJsonEntity.setSuccess(false);
			ajaxJsonEntity.setMessage("突击单Id丢失,请检查");
			return ajaxJsonEntity;
		}*/
		Area area = areaService.getFromCache(orderCondition.getSubArea().getId());
		orderCondition.setAreaName(orderCondition.getAreaName()+" "+ area.getName());
		try {
			orderCondition.setServiceAddress(StringUtils.filterAddress(orderCondition.getServiceAddress()));
			crushService.updateAddress(orderCondition,crushId);
		}catch (Exception e){
			ajaxJsonEntity.setSuccess(false);
			ajaxJsonEntity.setMessage(e.getMessage());
		}
        return ajaxJsonEntity;
	}

	/**
	 * 获取网点
	 * @param orderId
	 * @param areaId
	 * @param quarter
	 * @param degree
	 * @return
	 */
	@RequestMapping("findCrushServicePointList")
	@ResponseBody
	public AjaxJsonEntity findCrushServicePointList(Long orderId,Long areaId,String quarter,Integer degree){
		AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
		if(orderId==null || orderId<=0){
			ajaxJsonEntity.setSuccess(false);
			ajaxJsonEntity.setMessage("工单ID丢失,请检查");
			return ajaxJsonEntity;
		}
		if(areaId==null || areaId<=0){
			ajaxJsonEntity.setSuccess(false);
			ajaxJsonEntity.setMessage("区县ID丢失,请检查");
			return ajaxJsonEntity;
		}
		Long productCategoryId = orderService.getOrderProductCategoryId(quarter, orderId);
		if(degree==0 || degree<=0){
			List<Dict> dictList = MSDictUtils.getDictList("degreeType");
			if(dictList!=null && dictList.size()>0){
				degree = dictList.get(0).getIntValue();
			}
		}
		try {
			List<ServicePoint> servicePoints = findCrushServicePointByDegree(new Area(areaId),productCategoryId,degree);
			ajaxJsonEntity.setSuccess(true);
			ajaxJsonEntity.setData(servicePoints);
		}catch (Exception e){
			ajaxJsonEntity.setSuccess(false);
			ajaxJsonEntity.setMessage(e.getMessage());
		}
        return ajaxJsonEntity;
	}


	/**
	 * 根据区id，品类,网点分类获取网点
	 * @param area
	 * @param productCategoryId
	 * @param degree
	 * @return
	 */
	private List<ServicePoint> findCrushServicePointByDegree(Area area,long productCategoryId,int degree){
		ServicePoint servicePoint = new ServicePoint();
		servicePoint.setArea(area);
		servicePoint.setProductCategoryId(productCategoryId);
		servicePoint.setDegree(degree);
		Page<ServicePoint> page = new Page<ServicePoint>(1,500);
		page = servicePointService.findServicePointListForPlanNew(page, servicePoint);
		List<ServicePoint> servicePoints = Lists.newArrayList();
		if (!org.springframework.util.ObjectUtils.isEmpty(page.getList())){
			servicePoints = page.getList();
		}
		return servicePoints;
	}

}
