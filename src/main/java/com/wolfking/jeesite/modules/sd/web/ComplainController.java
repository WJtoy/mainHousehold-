package com.wolfking.jeesite.modules.sd.web;

import cn.hutool.core.exceptions.ExceptionUtil;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.entity.b2bcenter.pb.MQB2BOrderComplainMessage;
import com.kkl.kklplus.utils.ExceptionUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.service.SequenceIdService;
import com.wolfking.jeesite.common.service.UploadFileService;
import com.wolfking.jeesite.common.servlet.UploadFileModel;
import com.wolfking.jeesite.common.utils.BitUtils;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.GsonUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.Engineer;
import com.wolfking.jeesite.modules.md.entity.ProductCategory;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.md.service.CustomerService;
import com.wolfking.jeesite.modules.md.service.ProductCategoryService;
import com.wolfking.jeesite.modules.md.service.ServicePointService;
import com.wolfking.jeesite.modules.sd.entity.*;
import com.wolfking.jeesite.modules.sd.entity.viewModel.ComplainSearchModel;
import com.wolfking.jeesite.modules.sd.service.OrderComplainService;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.*;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.modules.sys.utils.SeqUtils;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.b2bcenter.mq.sender.B2BCenterOrderComplainMQSender;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 投诉Controller
 *
 */
@Controller
@RequestMapping(value = "${adminPath}/sd/complain")
@Slf4j
public class ComplainController extends BaseController
{
	private static final String MODEL_ATTR_PAGE = "page";
	private static final String MODEL_ATTR_ENTITY = "complain";

	@Autowired
	private OrderComplainService orderComplainService;

	@Autowired
	private OrderService orderService;

	@Autowired
	private CustomerService customerService;

	@Autowired
	private ServicePointService servicePointService;

	@Autowired
	private ProductCategoryService productCategoryService;

	@Autowired
	private AreaService areaService;

    @Autowired
	private B2BCenterOrderComplainMQSender complainMQSender;

    @Autowired
    private UploadFileService uploadFileService;


    //@PostConstruct
    //public void init() throws ServletException {
    //    ComplainController.sequenceIdUtils = new SequenceIdUtils(ThreadLocalRandom.current().nextInt(32), ThreadLocalRandom.current().nextInt(32));
    //}
	@Autowired
	private SequenceIdService sequenceIdService;

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
	private ComplainSearchModel setSearchModel(Model model,User user, ComplainSearchModel searchModel, int initMonths,int maxDays,boolean searchByCompleteDateRange,int maxCompleteDays) {
		if (searchModel == null) {
			searchModel = new ComplainSearchModel();
		}
		Area area = searchModel.getArea();
		if(area == null){
			area = new Area(0L);
			searchModel.setArea(area);
		}
		if(area.getParent()==null || area.getParent().getId() == null){
			area.setParent(new Area(0L));
		}
		String checkRegion = orderComplainService.loadAndCheckUserRegions(searchModel,user);
		if(StringUtils.isNotBlank(checkRegion)){
			searchModel.setIsValid(false);
			return searchModel;
		}
		boolean isServiceSupervisor = user.getRoleEnNames().contains("Customer service supervisor");//客服主管
		if (searchModel.getStatus() == null || StringUtils.isBlank(searchModel.getStatus().getValue())) {
			searchModel.setStatus(null);
		}
		Date now = new Date();
		searchModel.setCurrentTime(now);
		//投诉日期
		if (searchModel.getBeginDate() == null) {
			searchModel.setEndDate(DateUtils.getDateEnd(new Date()));
			searchModel.setBeginDate(DateUtils.getStartDayOfMonth(DateUtils.addMonth(new Date(), 0 - initMonths)));
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
		//结案日期
		if(searchByCompleteDateRange){
			if (searchModel.getCompleteEndDate() != null) {
				searchModel.setCompleteEndDate(DateUtils.getDateEnd(searchModel.getCompleteEndDate()));
			}
			//检查最大时间范围
			if(maxCompleteDays > 0 && searchModel.getCompleteBeginDate() != null){
				Date maxDate = DateUtils.addDays(searchModel.getCompleteBeginDate(),maxCompleteDays-1);
				maxDate = DateUtils.getDateEnd(maxDate);
				if(searchModel.getCompleteEndDate() == null){
					searchModel.setCompleteEndDate(DateUtils.getDateEnd(now));
				}
				if(DateUtils.pastDays(maxDate,searchModel.getCompleteEndDate())>0){
					searchModel.setCompleteEndDate(maxDate);
				}
			}
		}
		//int subQueryUserArea = 1;
		//vip客服查询自己负责的单，by客户+区域+品类
		//1.by 客户，前端客户已按客服筛选了
		if(user.isKefu()) {
			KefuTypeEnum kefuTypeEnum = KefuTypeEnum.fromCode(user.getSubFlag());
			if(kefuTypeEnum!=null){
				searchModel.setCustomerType(kefuTypeEnum.getCustomerType());
				searchModel.setKefuType(kefuTypeEnum.getKefuType());
			}else{
				addMessage(model, "错误:读取客服类型错误");
				searchModel.setIsValid(false);
				return searchModel;
			}
		}else if(user.isSaleman()){
			searchModel.setSalesId(user.getId());
			searchModel.setSubUserType(user.getSubFlag());//子账号类型
			List<Long> offlineCustomers = customerService.findIdListByOfflineOrderFlagFromCacheForSD();
			if(!org.springframework.util.CollectionUtils.isEmpty(offlineCustomers)){
				searchModel.setOfflineCustomerList(offlineCustomers);
			}
		} else{
			//其他类型帐号，不限制客户及突击区域订单
			searchModel.setCustomerType(null);
			searchModel.setKefuType(null);
		}
		int userType = user.getUserType();
		if (isServiceSupervisor) {
			searchModel.setCreateBy(user);//*
		} else if (user.isKefu()) {
			searchModel.setCreateBy(user);//*,只有客服才按帐号筛选
		} else if (user.isInnerAccount()) { //内部帐号
			searchModel.setCreateBy(user);//*
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
	private Boolean checkOrderNoAndPhone(ComplainSearchModel searchModel,Model model,Page page){
		if(searchModel == null){
			return true;
		}
		int noSearchType = searchModel.getComplainNoSearchType();
        if (noSearchType != 1 && StringUtils.isNotBlank(searchModel.getComplainNo())){
            addMessage(model, "错误：请输入正确的投诉单号");
            model.addAttribute(MODEL_ATTR_PAGE, page);
            model.addAttribute(MODEL_ATTR_ENTITY, searchModel);
            return false;
        }
		//检查订单号
		if(StringUtils.isNotBlank(searchModel.getOrderNo())) {
			int orderSerchType = searchModel.getOrderNoSearchType();
			if (orderSerchType != 1) {
				addMessage(model, "错误：请输入正确的订单号");
				model.addAttribute(MODEL_ATTR_PAGE, page);
				model.addAttribute(MODEL_ATTR_ENTITY, searchModel);
				return false;
			}else{
				//检查分片
				try {
					Date goLiveDate = OrderUtils.getGoLiveDate();
					String[] quarters = DateUtils.getQuarterRange(goLiveDate, new Date());
					if(quarters.length == 2) {
						int start = StringUtils.toInteger(quarters[0]);
						int end = StringUtils.toInteger(quarters[1]);
						if(start>0 && end > 0){
							int quarter = StringUtils.toInteger(searchModel.getQuarter());
							if(quarter < start || quarter > end){
								addMessage(model, "错误：请输入正确的订单号,日期超出范围");
								model.addAttribute(MODEL_ATTR_PAGE, page);
								model.addAttribute(MODEL_ATTR_ENTITY, searchModel);
								return false;
							}
						}
					}
				}catch (Exception e){
					log.error("检查分片错误,orderNo:{}",searchModel.getOrderNo(),e);
				}
			}
		}
		if (StringUtils.isNotBlank(searchModel.getUserPhone())){
			if(searchModel.getIsPhone() != 1){
				addMessage(model, "错误：请输入正确的电话");
				model.addAttribute(MODEL_ATTR_PAGE, page);
				model.addAttribute(MODEL_ATTR_ENTITY, searchModel);
				return false;
			}
		}
		return true;
	}


	/**
	 * 处理中列表
	 */
	@RequiresPermissions(value ="sd:complain:judge")
	@RequestMapping(value = "/dealinglist")
	public String dealingList(ComplainSearchModel searchModel, HttpServletRequest request, HttpServletResponse response, Model model) {
		String viewForm = "modules/sd/complain/dealingList";
		Page<OrderComplain> page = new Page<OrderComplain>();
		User user = UserUtils.getUser();
		//状态：所有
		//加载处理中的投诉单
		searchModel.setStatus(new Dict(OrderComplain.STATUS_PROCESSING.toString()));
		searchModel.setSearchType("dealing");
		searchModel = setSearchModel(model, user,searchModel,3,365,false,0);
		if(!searchModel.getIsValid()){
			model.addAttribute(MODEL_ATTR_PAGE, page);
			model.addAttribute(MODEL_ATTR_ENTITY, searchModel);
			return viewForm;
		}
		Boolean isValid = checkOrderNoAndPhone(searchModel,model,page);
		if(!isValid){
			model.addAttribute(MODEL_ATTR_PAGE, page);
			model.addAttribute(MODEL_ATTR_ENTITY, searchModel);
			return viewForm;
		}
		//
		try {
			//查询
			page = orderComplainService.findComplainList(new Page<ComplainSearchModel>(request, response), searchModel);

		} catch (Exception e) {
			addMessage(model, "查询错误：" + e.getMessage());
		}
		model.addAttribute(MODEL_ATTR_PAGE, page);
		model.addAttribute(MODEL_ATTR_ENTITY, searchModel);
		return viewForm;
	}

	/**
	 * 待跟进列表
	 * @param searchModel
	 * @param request
	 * @param response
	 * @param model
	 * @return
	 */
	@RequiresPermissions(value ="sd:complain:judge")
	@RequestMapping(value = "/appointlist")
	public String appointList(ComplainSearchModel searchModel, HttpServletRequest request, HttpServletResponse response, Model model) {
		String viewForm = "modules/sd/complain/appointList";
		Page<OrderComplain> page = new Page<OrderComplain>();
		User user = UserUtils.getUser();
		//状态：所有
		//加载处理中的投诉单
		searchModel.setStatus(new Dict(OrderComplain.STATUS_PROCESSING.toString()));
		searchModel.setSearchType("appoint");
		searchModel = setSearchModel(model, user,searchModel,3,365,false,0);
		if(!searchModel.getIsValid()){
			model.addAttribute(MODEL_ATTR_PAGE, page);
			model.addAttribute(MODEL_ATTR_ENTITY, searchModel);
			return viewForm;
		}
		Boolean isValid = checkOrderNoAndPhone(searchModel,model,page);
		if(!isValid){
			model.addAttribute(MODEL_ATTR_PAGE, page);
			model.addAttribute(MODEL_ATTR_ENTITY, searchModel);
			return viewForm;
		}
		try {
			//查询
			page = orderComplainService.findComplainList(new Page<ComplainSearchModel>(request, response), searchModel);

		} catch (Exception e) {
			addMessage(model, "查询错误：" + e.getMessage());
		}
		model.addAttribute(MODEL_ATTR_PAGE, page);
		model.addAttribute(MODEL_ATTR_ENTITY, searchModel);
		return viewForm;
	}


	//endregion 列表

	/**
	 * 设定停滞原因 form
	 *
	 * @param orderComlpainId
	 * @param
	 */
	@RequiresPermissions(value ="sd:complain:judge")
	@RequestMapping(value = "/appointForm", method = RequestMethod.GET)
	public String appointFrom(String orderComlpainId,String quarter, HttpServletRequest request, Model model)
	{
		String viewForm = "modules/sd/complain/appointForm";
		Long lorderId = Long.valueOf(orderComlpainId);
		OrderComplain orderComplain=new OrderComplain();
		if (lorderId == null || lorderId<=0) {
			addMessage(model, "错误：投单参数无效");
			model.addAttribute("canSave", false);
			model.addAttribute(MODEL_ATTR_ENTITY, orderComplain);
			return viewForm;
		}else{
			orderComplain.setId(lorderId);
			// 时间取整点时间
			Date date = DateUtils.addDays(new Date(), 1);
			String time = DateUtils.formatDate(date,"yyyy-MM-dd 08:00:00");
			Date appointmentDate = null;
			try
			{
				appointmentDate = DateUtils.parse(time,"yyyy-MM-dd HH:00:00");
			} catch (java.text.ParseException e)
			{
				log.error("[ComplainController.appoint] invalid datetime:{}",time, e);
			}
			// 时间取整点时间
			orderComplain.setAppointDate(appointmentDate);
			orderComplain.setQuarter(quarter);
			model.addAttribute("canSave", true);
			model.addAttribute(MODEL_ATTR_ENTITY, orderComplain);
			return viewForm;
		}

	}

	/**
	 * 保存待跟进
	 * @param orderComplain
	 * @param response
	 * @return
	 */
	@RequiresPermissions(value ="sd:complain:judge")
	@ResponseBody
	@RequestMapping(value = "appointSave", method = RequestMethod.POST)
	public AjaxJsonEntity appointSave(OrderComplain orderComplain, HttpServletResponse response)
	{
		AjaxJsonEntity result = new AjaxJsonEntity(true);
		try
		{
			orderComplainService.saveOrderComplainAppoint(orderComplain.getId(),orderComplain.getQuarter(),orderComplain.getAppointDate());
		} catch (Exception e)
		{
			result.setSuccess(false);
			result.setMessage(e.getMessage());
		}
		return result;
	}


	//region 申请

	/**
	 * 具体工单的投诉单列表
	 */
	@RequiresPermissions(value ={"sd:complain:create","sd:complain:judge","sd:complain:complete"},logical = Logical.OR)
	@RequestMapping(value = "/orderlist")
	public String list(Long orderId,String orderNo,String quarter, HttpServletRequest request, HttpServletResponse response, Model model)
	{
		Page<OrderComplain> page = new Page<OrderComplain>();
		if(orderId==null || orderId<0) {
			addMessage(model, "导常处理失败：订单参数为空。");
		}else {
			try {
				List<OrderComplain> list = orderComplainService.getComplainListByOrder(orderId,null,quarter,true);
				page.setList(list);
			} catch (Exception e) {
				addMessage(model, "错误：" + e.getMessage());
			}
		}
		model.addAttribute(MODEL_ATTR_PAGE,page);
		model.addAttribute("orderNo",StringUtils.isNotBlank(orderNo)?orderNo:"");
		model.addAttribute("orderId",orderId.toString());
		return "modules/sd/complain/orderList";
	}

	/**
	 * 新增/修改窗口
	 * @param orderId	订单id
	 */
	@RequiresPermissions(value="sd:complain:create")
	@RequestMapping(value = "/form", method = RequestMethod.GET)
	public String form(@RequestParam(required = false) String id,@RequestParam String orderId, @RequestParam(required = false) String quarter, Model model) {
		User user = UserUtils.getUser();
		Long lid = null;
		OrderComplain complain = new OrderComplain();
		complain.setProductCategoryId(0L);
		String formView = "modules/sd/complain/complainForm";
		if(StringUtils.isBlank(id) && StringUtils.isBlank(orderId)){
			addMessage(model, "参数为空。");
			model.addAttribute("canAction", false);
			model.addAttribute(MODEL_ATTR_ENTITY,complain);
            model.addAttribute("hasOpenForm",false);//已有处理中投诉单标记
			return formView;
		}

		Long lorderId = Long.valueOf(orderId);
		if(StringUtils.isBlank(id)){

			Order order = orderService.getOrderById(lorderId,quarter, OrderUtils.OrderDataLevel.CONDITION,true);
			if (order == null || order.getOrderCondition() == null) {
				addMessage(model, "订单读取失败，或不存在。请重试");
				model.addAttribute("canAction", false);
				model.addAttribute(MODEL_ATTR_ENTITY,complain);
                model.addAttribute("hasOpenForm",false);
				return formView;
			}else{
                //check complain
                List<OrderComplain> complains = orderComplainService.getComplainListByOrder(lorderId,order.getOrderNo(),order.getQuarter(),true);
                if(complains != null && complains.size()>0){
                    OrderComplain openComplain = complains.stream().filter(t->Integer.valueOf(t.getStatus().getValue())<=1 || t.getStatus().getValue().equals("3")).findFirst().orElse(null);
                    if(openComplain != null){
                        model.addAttribute("canAction", false);
                        model.addAttribute(MODEL_ATTR_ENTITY,complain);
                        model.addAttribute("list",complains);
                        model.addAttribute("hasOpenForm",true);
                        return formView;
                    }
                }
				lid = SeqUtils.NextID();
				complain.setId(lid);
				complain.setAction(0);//new
				complain.setOrderId(lorderId);
				complain.setQuarter(order.getQuarter());
				complain.setOrderNo(order.getOrderNo());
				complain.setUserName(order.getOrderCondition().getUserName());
				complain.setUserPhone(order.getOrderCondition().getServicePhone());
				complain.setUserAddress(order.getOrderCondition().getArea().getName()+order.getOrderCondition().getServiceAddress());
				complain.setArea(order.getOrderCondition().getArea());
				complain.setCustomer(order.getOrderCondition().getCustomer());
				complain.setComplainBy(user.getName());
				complain.setKefu(order.getOrderCondition().getKefu());
				complain.setProductCategoryId(order.getOrderCondition().getProductCategoryId()==null?0L:order.getOrderCondition().getProductCategoryId());//2019-10-14
				//List<ServicePoint> servicePoints = orderService.getSetProductIdIncludeMe()
				//complain.setServicePoint(order.getOrderCondition().getServicePoint());
				complain.setComplainDate(new Date());
				model.addAttribute("canAction", true);
				model.addAttribute(MODEL_ATTR_ENTITY,complain);
                model.addAttribute("hasOpenForm",false);
				return formView;
			}
		}else{
			//edit
			try {
				lid = Long.valueOf(id);
			}catch (Exception e){
				lid = 0l;
			}
			if(lid<=0){
				addMessage(model, "投诉单参数类型错误。");
				model.addAttribute("canAction", false);
				model.addAttribute(MODEL_ATTR_ENTITY,complain);
				model.addAttribute("hasOpenForm",false);
				return formView;
			}
			complain = orderComplainService.getComplain(lid,quarter);
			if(complain ==null){
				addMessage(model, "读取投诉单失败或不存在，请重试。");
				model.addAttribute("canAction", false);
                model.addAttribute("hasOpenForm",false);
				complain=new OrderComplain();
			}
			else{
				if(!complain.getStatus().getValue().equalsIgnoreCase(OrderComplain.STATUS_APPLIED.toString())){
					addMessage(model, "投诉单已在处理中，不能修改申请内容。");
					model.addAttribute("canAction", false);
					model.addAttribute(MODEL_ATTR_ENTITY,complain);
					model.addAttribute("hasOpenForm",false);
					return formView;
				}
				complain.setAction(1);//修改
				List<OrderComplainAttachment> attachments = orderComplainService.getComplainAttachements(lid,quarter,0);
				if(attachments != null && attachments.size()>0){
					OrderComplainAttachment attachment;
					for(int i=0,size=attachments.size();i<size;i++){
						attachment = attachments.get(i);
						attachment.setStrId(attachment.getId().toString());
					}
				}
				if(attachments != null){
					complain.setApplyAttaches(attachments);
				}
				//投诉对象complain_object
				if(complain.getComplainObject()>0){
					complain.setComplainObjectsIds(BitUtils.getPositions(complain.getComplainObject(),String.class));
				}
				//投诉项目complain_item
				if(complain.getComplainItem()>0){
					complain.setComplainItemsIds(BitUtils.getPositions(complain.getComplainItem(),String.class));
				}
				Customer customer = customerService.getFromCache(complain.getCustomer().getId());
				complain.setCustomer(customer);
				model.addAttribute("hasOpenForm",false);
				model.addAttribute("canAction", true);
			}
			model.addAttribute(MODEL_ATTR_ENTITY,complain);
			model.addAttribute("hasOpenForm",false);
			return formView;
		}
	}

	/**
	 * 提交投诉申请
	 */
	@RequiresPermissions("sd:complain:create")
	@ResponseBody
	@RequestMapping(value = "/save")
	public AjaxJsonEntity save(OrderComplain complain, HttpServletRequest request, HttpServletResponse response, Model model) {
		response.setContentType("application/json; charset=UTF-8");
		AjaxJsonEntity jsonEntity = new AjaxJsonEntity(true);
		if(complain == null){
			jsonEntity.setSuccess(false);
			jsonEntity.setMessage("提交表单为空");
			return jsonEntity;
		}
		User user = UserUtils.getUser();
		if(user==null || user.getId()==null){
			jsonEntity.setSuccess(false);
			jsonEntity.setMessage("登录超时，请重新登录。");
			jsonEntity.setLogin(false);
			return jsonEntity;
		}
		//System.out.println(GsonUtils.getInstance().toGson(complain));
		if (!beanValidator(model, complain))
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
		Order order = orderService.getOrderById(complain.getOrderId(), complain.getQuarter(), OrderUtils.OrderDataLevel.DETAIL, true);
		OrderCondition orderCondition = order.getOrderCondition();
		if (order == null || orderCondition == null) {
			jsonEntity.setSuccess(false);
			jsonEntity.setMessage("读取订单错误，请重试!");
			return jsonEntity;
		}
		try{
			if(complain.getAction()==0){
				//new
				Area area = areaService.getFromCache(complain.getArea().getId());
				if(area != null) {
					List<String> ids = Splitter.onPattern(",")
							.omitEmptyStrings()
							.trimResults()
							.splitToList(area.getParentIds());
					if (ids.size() >= 2) {
						complain.setCity(new Area(Long.valueOf(ids.get(ids.size() - 1))));
						complain.setProvince(new Area(Long.valueOf(ids.get(ids.size() - 2))));
					}else{
						jsonEntity.setSuccess(false);
						jsonEntity.setMessage("读取区域所属省/市错误");
						return jsonEntity;
					}
				}else{
					jsonEntity.setSuccess(false);
					jsonEntity.setMessage("无区县信息");
					return jsonEntity;
				}
				complain.setStatus(new Dict(OrderComplain.STATUS_APPLIED.toString(),"待处理"));
				String no = SeqUtils.NextSequenceNo("ComplainNo");
				if(StringUtils.isBlank(no)){
					no =  SeqUtils.NextSequenceNo("ComplainNo");
				}
				complain.setComplainNo(no);
			}
			UserUtils.substrUserName(user,20);
			complain.setCreateBy(user);
			complain.setCreateDate(new Date());
			complain.setUpdateBy(user);
			complain.setUpdateDate(new Date());
			complain.setOrderStatus(orderCondition.getStatus());
			complain.setCanRush(orderCondition.getCanRush());
			complain.setCreateType(OrderComplain.CREATE_TYPE_MANUANL);
			complain.setKefuType(orderCondition.getKefuType());
			orderComplainService.saveComplainApply(complain,order.getOrderStatus());
		}catch (Exception e){
			if(complain.getAction()==0 && StringUtils.isNotBlank(complain.getComplainNo())){
				try{
					SeqUtils.reputSequenceNo("ComplainNo",complain.getCreateDate(),complain.getComplainNo());
				}catch (Exception e1){}
			}
			jsonEntity.setSuccess(false);
			jsonEntity.setMessage("保存投诉单失败:" + e.getMessage());
			log.error("[ComplainController.save] orderNo:{} ,userId:{}",complain.getOrderNo(),user.getId(),e);
			LogUtils.saveLog("投诉单-申请","ComplainController.save",complain.getOrderNo(),e,user);
		}
		return jsonEntity;
	}

	/**
	 * 附件
	 */
	@RequiresPermissions(value = { "sd:complain:create", "sd:complain:judge", "sd:complain:complete" }, logical = Logical.OR)
	@ResponseBody
	@RequestMapping(value = "/attachments")
	public AjaxJsonEntity attachments(String id,String quarter,Integer attachmentType, HttpServletRequest request, HttpServletResponse response, Model model, RedirectAttributes redirectAttributes)
	{
		response.setContentType("application/json; charset=UTF-8");
		AjaxJsonEntity result = new AjaxJsonEntity(true);
		if(StringUtils.isBlank(id)){
			result.setSuccess(false);
			result.setMessage("参数错误。");
			return result;
		}
		Long lid = null;
		try{
			lid = Long.valueOf(id);
		}catch (Exception e){
			lid = 0l;
		}
		if(lid<=0){
			result.setSuccess(false);
			result.setMessage("参数类型错误。");
			return result;
		}
		User user = UserUtils.getUser();
		if(user==null || user.getId()==null){
			result.setSuccess(false);
			result.setMessage("登录超时，请重新登录。");
			return result;
		}
		List<OrderComplainAttachment> attachments = orderComplainService.getComplainAttachements(lid,quarter,attachmentType);
		if(attachments==null){
			attachments = Lists.newArrayList();
		}

		try
		{
			List<UploadFileModel> files = Lists.newArrayList();
			OrderComplainAttachment attachment;
			UploadFileModel file;
			for(int i=0,size=attachments.size();i<size;i++){
				attachment = attachments.get(i);
				file = new UploadFileModel();
				file.setId(attachment.getId().toString());
				file.setFileName(attachment.getFilePath());
				file.setOrigalName(attachment.getFileName());
				//file.setImage(true);
				files.add(file);
			}
			result.setData(files);
		} catch (Exception e)
		{
			result.setSuccess(false);
			result.setMessage("读取附件失败，请关闭后重新打开窜口");
			log.error("[ComplainController.attachments] id:{} ,userId:{}",id,user.getId(),e);
			//LogUtils.saveLog("读取附件失败","ComplainController.attachments",id,e,user);
		}
		return result;
	}

	//endregion 申请

	//region 判定

	/**
	 * 待判定投诉单列表
	 * 待处理
	 */
	@RequiresPermissions(value ="sd:complain:judge")
	@RequestMapping(value = "/judgelist")
	public String judgeList(ComplainSearchModel searchModel, HttpServletRequest request, HttpServletResponse response, Model model) {
		String viewForm = "modules/sd/complain/judgeList";
	    Page<OrderComplain> page = new Page<OrderComplain>();
		User user = UserUtils.getUser();
        searchModel = setSearchModel(model, user,searchModel,3,365,false,0);
		if(!searchModel.getIsValid()){
			model.addAttribute(MODEL_ATTR_PAGE, page);
			model.addAttribute(MODEL_ATTR_ENTITY, searchModel);
			return viewForm;
		}
        Boolean isValid = checkOrderNoAndPhone(searchModel,model,page);
        if(!isValid){
			model.addAttribute(MODEL_ATTR_PAGE, page);
			model.addAttribute(MODEL_ATTR_ENTITY, searchModel);
            return viewForm;
        }
        //状态：待处理
        //修改为加载待处理的投诉单
        Dict statusSearcher=new Dict(OrderComplain.STATUS_APPLIED,"待处理");
        searchModel.setStatus(statusSearcher);
		try {
			//查询
			page = orderComplainService.findComplainList(new Page<ComplainSearchModel>(request, response), searchModel);
		} catch (Exception e) {
			addMessage(model, "查询错误：" + e.getMessage());
		}
		model.addAttribute(MODEL_ATTR_PAGE, page);
		model.addAttribute(MODEL_ATTR_ENTITY, searchModel);
		return viewForm;
	}

	/**
	 * 判定窗口
	 * @param id		投诉单id
	 * @param quarter 	分片
	 */
	@RequiresPermissions(value="sd:complain:judge")
	@RequestMapping(value = "/judge", method = RequestMethod.GET)
	public String judgeForm(@RequestParam String id, @RequestParam(required = false) String quarter, Model model) {
		User user = UserUtils.getUser();
		OrderComplain complain = new OrderComplain();
		String formView = "modules/sd/complain/judgeForm";
		if(StringUtils.isBlank(id)){
			addMessage(model, "参数为空。");
			model.addAttribute("canAction", false);
			model.addAttribute(MODEL_ATTR_ENTITY,complain);
			return formView;
		}

		Long lid = null;
		try {
			lid = Long.valueOf(id);
		}catch (Exception e){
			lid = 0l;
		}
		if (lid == null || lid <= 0){
			addMessage(model, "投诉单参数类型错误。");
			model.addAttribute("canAction", false);
			model.addAttribute(MODEL_ATTR_ENTITY,complain);
			return formView;
		}
		try {
			complain = orderComplainService.getComplain(lid, quarter);
		}catch (Exception e){
			addMessage(model, "投诉单读取错误。");
			model.addAttribute("canAction", false);
			model.addAttribute(MODEL_ATTR_ENTITY,complain);
			return formView;
		}
		if(complain ==null){
			addMessage(model, "读取投诉单失败或不存在，请重试。");
			model.addAttribute("canAction", false);
			complain=new OrderComplain();
			return formView;
		}
		int statusValue = Integer.valueOf(complain.getStatus().getValue());
		if(statusValue == OrderComplain.STATUS_CLOSED){
			addMessage(model, "投诉单已结案，不能再判定。");
			model.addAttribute("canAction", false);
			model.addAttribute(MODEL_ATTR_ENTITY,complain);
			return formView;
		}
		if(complain.getAttachmentQty()>0) {
			List<OrderComplainAttachment> attachments = orderComplainService.getComplainAttachements(lid, quarter, 0);
			if (attachments != null && attachments.size() > 0) {
				complain.setApplyAttaches(attachments);
			}
		}
		//加载判定附件
		List<OrderComplainAttachment> judgeattachments = orderComplainService.getComplainAttachements(lid, quarter, OrderComplainAttachment.ATTACHMENTTYPE_JUDEG);
		if (judgeattachments != null && judgeattachments.size() > 0) {
			complain.setJudgeAttaches(judgeattachments);
		}

		if(statusValue == OrderComplain.STATUS_APPLIED){
		    complain.setJudgeBy(user);
		    complain.setJudgeDate(new Date());
        }
		//status
		//Dict dict = MSDictUtils.getDictByValue(complain.getStatus().getValue(),"complain_status");//切换为微服务
		Dict dict = getStatus(complain.getStatus().getValue());
		if (dict != null) {
			complain.setStatus(dict);
		}

		//投诉对象complain_object
		if(complain.getComplainObject()>0){
			complain.setComplainObjectsIds(BitUtils.getPositions(complain.getComplainObject(),String.class));
			if(complain.getComplainObjectsIds() != null && complain.getComplainObjectsIds().size() > 0) {
				complain.setComplainObjects(MSDictUtils.getDictInclueList("complain_object",Joiner.on(",").join(complain.getComplainObjectsIds())));
			}
		}
		//投诉项目complain_item
		if(complain.getComplainItem()>0){
			complain.setComplainItemsIds(BitUtils.getPositions(complain.getComplainItem(),String.class));
			if(complain.getComplainItemsIds() != null && complain.getComplainItemsIds().size() > 0) {
				complain.setComplainItems(MSDictUtils.getDictInclueList("complain_item",Joiner.on(",").join(complain.getComplainItemsIds())));
			}
		}
		Customer customer = customerService.getFromCache(complain.getCustomer().getId());
		complain.setCustomer(customer);

		//判定
		//对象
		if(complain.getJudgeObject()>0){
			complain.setJudgeObjectsIds(BitUtils.getPositions(complain.getJudgeObject(),String.class));
		}
		//test
        //complain.setJudgeObjectsIds(Lists.newArrayList("3"));
		//项目
		if(complain.getJudgeItem()>0){
			complain.setJudgeItemsIds(BitUtils.getPositions(complain.getJudgeItem(),String.class));
		}
        if(complain.getJudgeDate() == null){
		    complain.setJudgeBy(user);
		    complain.setJudgeDate(new Date());
        }
        //网点，根据上门服务取得
//		List<ServicePoint> servicePoints = orderService.getOrderServicePoints(complain.getOrderId(),complain.getQuarter());
		List<ServicePoint> servicePoints = orderService.getPlannedServicePoints(complain.getOrderId(),complain.getQuarter());
		//name = no-name
		servicePoints.stream().forEach(t->t.setName(t.getServicePointNo()+"-"+t.getName()));
		complain.setServicePoints(servicePoints);

		model.addAttribute("canAction", true);
		model.addAttribute(MODEL_ATTR_ENTITY,complain);
		return formView;
	}

	/**
	 * 开始处理，状态变更为：处理中
	 */
	@RequiresPermissions(value =  "sd:complain:judge")
	@ResponseBody
	@RequestMapping(value = "/accept")
	public AjaxJsonEntity accept(String id,String quarter,HttpServletRequest request, HttpServletResponse response)
	{
		response.setContentType("application/json; charset=UTF-8");
		AjaxJsonEntity result = new AjaxJsonEntity(true);
		if(StringUtils.isBlank(id)){
			result.setSuccess(false);
			result.setMessage("参数错误。");
			return result;
		}
		Long lid = null;
		try{
			lid = Long.valueOf(id);
		}catch (Exception e){
			lid = 0l;
		}
		if(lid<=0){
			result.setSuccess(false);
			result.setMessage("参数类型错误。");
			return result;
		}
		User user = UserUtils.getUser();
		if(user==null || user.getId()==null){
			result.setSuccess(false);
			result.setMessage("登录超时，请重新登录。");
			return result;
		}
		OrderComplain complain = null;
		try
		{
			complain = orderComplainService.getComplain(lid,quarter);
			if(complain == null){
				result.setSuccess(false);
				result.setMessage("读取错误，请检查投诉单是否存在，或请重试。");
				return result;
			}
			Dict dict = complain.getStatus();
			int statusValue = Integer.valueOf(dict.getValue());
			if(statusValue !=OrderComplain.STATUS_APPLIED){
				result.setSuccess(false);
				dict = getStatus(complain.getStatus().getValue());
				/*
				Dict dict = MSDictUtils.getDictByValue(complain.getStatus().getValue(),"complain_status");//切换为微服务
				if(dict==null){
					dict = complain.getStatus();
					if(statusValue==1){
						dict.setLabel("处理中");
					}else if(statusValue==2){
						dict.setLabel("已结案");
					}
				}*/
				result.setMessage("投诉单目前状态:" + dict.getValue() +",不能开始处理");
				return result;
			}
			UserUtils.substrUserName(user,30);
			orderComplainService.acceptComplain(lid,complain.getQuarter(),user,new Date(),complain.getOrderId());
			result.setData(OrderComplain.STATUS_PROCESSING.toString());

		} catch (Exception e)
		{
			result.setSuccess(false);
			result.setMessage("处理失败，请关闭后重新打开窜口");
			log.error("[ComplainController.accept] id:{} ,userId:{}",id,user.getId(),e);
			//LogUtils.saveLog("投诉单-开始处理","ComplainController.accept",lid.toString(),e,user);
		}
		return result;
	}

    /**
     * 提交判定
     */
    @RequiresPermissions(value =  "sd:complain:judge")
    @ResponseBody
    @RequestMapping(value = "/savejudge")
    public AjaxJsonEntity saveJudge(OrderComplain complain,HttpServletRequest request, HttpServletResponse response)
    {
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        if(complain == null){
            result.setSuccess(false);
            result.setMessage("传入参数为空。");
            return result;
        }

        User user = UserUtils.getUser();
        if(user==null || user.getId()==null){
            result.setSuccess(false);
            result.setMessage("登录超时，请重新登录。");
            return result;
        }
        OrderComplain orgComplain = null;
        try
        {
            orgComplain = orderComplainService.getComplain(complain.getId(),complain.getQuarter());
            if(orgComplain == null){
                result.setSuccess(false);
                result.setMessage("读取单据错误，或投诉单不存在，请重试。");
                return result;
            }
            Dict dict = orgComplain.getStatus();
            int statusValue = Integer.valueOf(dict.getValue());
            if(statusValue != OrderComplain.STATUS_PROCESSING){
                result.setSuccess(false);
                dict = getStatus(orgComplain.getStatus().getValue());
				/*
                //Dict dict = MSDictUtils.getDictByValue(complain.getStatus().getValue(),"complain_status");//切换为微服务
                if(dict==null){
                    //dict = complain.getStatus();
                    dict = orgComplain.getStatus();
                    if(statusValue==1){
                        dict.setLabel("处理中");
                    }else if(statusValue==2){
                        dict.setLabel("已结案");
                    }
                }*/
                result.setMessage("投诉单目前状态:" + dict.getLabel() +",不能保存判定");
                return result;
            }
			Order order = orderService.getOrderById(orgComplain.getOrderId(),orgComplain.getQuarter(), OrderUtils.OrderDataLevel.HEAD,true);
			if(order == null){
				result.setSuccess(false);
				result.setMessage("读取订单信息错误。");
				return result;
			}
            StringBuffer message = new StringBuffer();
            if(StringUtils.isBlank(complain.getJudgeRemark())){
                message.append(",判责意见");
            }
            if(complain.getJudgeObjectsIds().size()==0){
                message.append(",责任对象");
            }
			if(complain.getJudgeObjectsIds().contains("1") && (complain.getServicePoint() == null || complain.getServicePoint().getId() == null || complain.getServicePoint().getId() == 0)){
				message.append(",责任网点");
			}
            if(complain.getJudgeObjectsIds().size()==0){
                message.append(",责任项目");
            }
            if(message.length()>0) {
                result.setSuccess(false);
                result.setMessage("请输入或选择"+message.toString().substring(1));
                return result;
            }

            /*complain.setOrderId(orgComplain.getOrderId());//*
			UserUtils.substrUserName(user,10);
            complain.setJudgeBy(user);
            complain.setJudgeDate(new Date());*/
			complain.setDataSource(order.getDataSourceId());
			complain.setOrderId(orgComplain.getOrderId());//*
			complain.setComplainNo(orgComplain.getComplainNo());
			complain.setB2bComplainNo(orgComplain.getB2bComplainNo());
			complain.setCreateType(orgComplain.getCreateType());
			UserUtils.substrUserName(user,10);
			complain.setJudgeBy(user);
			complain.setJudgeDate(new Date());
			orderComplainService.judgeComplain(complain);

            result.setData(OrderComplain.STATUS_PROCESSING.toString());

        } catch (Exception e)
        {
            result.setSuccess(false);
            result.setMessage("处理失败，请关闭后重新打开窜口");
			log.error("[ComplainController.saveJudge] id:{} ,userId:{}",complain.getId(),user.getId(),e);
			//LogUtils.saveLog("投诉单-判定","ComplainController.saveJudge",complain.getId().toString(),e,user);
        }
        return result;
    }

    @RequiresPermissions(value =  "sd:complain:judge")
    @ResponseBody
    @PostMapping(value = "/deleteJudgeAttach")
    public AjaxJsonEntity deleteJudgeAttach(@RequestParam("id") String id,@RequestParam("quarter") String quarter,HttpServletRequest request, HttpServletResponse response)
    {
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        if(StringUtils.isBlank(id) || StringUtils.isBlank(quarter)){
            result.setSuccess(false);
            result.setMessage("传入参数为空。");
            return result;
        }

        Long aid = StringUtils.toLong(id);
        if(aid<=0){
            result.setSuccess(false);
            result.setMessage("传入ID参数错误。");
            return result;
        }

        User user = UserUtils.getUser();
        if(user==null || user.getId()==null){
            result.setSuccess(false);
            result.setMessage("登录超时，请重新登录。");
            return result;
        }
        OrderComplainAttachment attachment = null;
        try
        {
            attachment = orderComplainService.getComplainAttachement(aid,quarter);
            if (attachment == null){
                result.setSuccess(false);
                result.setMessage("附件不存在或已删除");
                return result;
            }
			orderComplainService.deleteComplainAttachment(attachment.getComplainId(),aid,quarter,user,new Date());
        } catch (Exception e){
            result.setSuccess(false);
            result.setMessage("删除附件失败");
            log.error("id:{} ,userId:{}",id,user.getId(),e);
        }
        return result;
    }

    //endregion 判定

	//region 结案

	/**
	 * 待结案投诉单列表
	 * 待完成的投诉单
	 */
	@RequiresPermissions(value ="sd:complain:complete")
	@RequestMapping(value = "/completelist")
	public String completeList(ComplainSearchModel searchModel, HttpServletRequest request, HttpServletResponse response, Model model) {
		String viewForm = "modules/sd/complain/completeList";
	    Page<OrderComplain> page = new Page<OrderComplain>();
		User user = UserUtils.getUser();
        searchModel = setSearchModel(model, user,searchModel,3,365,false,0);
		if(!searchModel.getIsValid()){
			model.addAttribute(MODEL_ATTR_PAGE, page);
			model.addAttribute(MODEL_ATTR_ENTITY, searchModel);
			return viewForm;
		}
        Boolean isValid = checkOrderNoAndPhone(searchModel,model,page);
        if(!isValid){
			model.addAttribute(MODEL_ATTR_PAGE, page);
			model.addAttribute(MODEL_ATTR_ENTITY, searchModel);
            return viewForm;
        }
		//状态：所有
		//加载处理中的投诉单
		searchModel.setStatus(new Dict(OrderComplain.STATUS_PROCESSING.toString()));
		//待完成加载有判定人的投诉单
		searchModel.setJudgeBy(new User(-1l));
		//
		try {
			//查询
			Page<ComplainSearchModel> searchPage= new Page<ComplainSearchModel>(request, response);
			page = orderComplainService.findComplainList(searchPage, searchModel);
		} catch (Exception e) {
			addMessage(model, "查询错误：" + e.getMessage());
		}
		model.addAttribute(MODEL_ATTR_PAGE, page);
		model.addAttribute(MODEL_ATTR_ENTITY, searchModel);
		return viewForm;
	}

	/**
	 * 已结案投诉单列表
	 */
	@RequiresPermissions(value ="sd:complain:judge")
	@RequestMapping(value = "/alreadyCompleteList")
	public String alreadyCompleteList(ComplainSearchModel searchModel, HttpServletRequest request, HttpServletResponse response, Model model) {
		String viewForm = "modules/sd/complain/alreadyCompleteList";
	    Page<OrderComplain> page = new Page<OrderComplain>();
		User user = UserUtils.getUser();
        searchModel = setSearchModel(model, user,searchModel,3,365,true,365);
		if(!searchModel.getIsValid()){
			model.addAttribute(MODEL_ATTR_PAGE, page);
			model.addAttribute(MODEL_ATTR_ENTITY, searchModel);
			return viewForm;
		}
        Boolean isValid = checkOrderNoAndPhone(searchModel,model,page);
        if(!isValid){
			model.addAttribute(MODEL_ATTR_PAGE, page);
			model.addAttribute(MODEL_ATTR_ENTITY, searchModel);
            return viewForm;
        }
        //状态：所有
        //加载处理中的投诉单
        searchModel.setStatus(new Dict(OrderComplain.STATUS_CLOSED.toString()));
		try {
			//查询
			//已完成的设置 为了按完成时间排序
			searchModel.setSearchType("alreadyComplete");
			page = orderComplainService.findComplainList(new Page<ComplainSearchModel>(request, response), searchModel);

		} catch (Exception e) {
			addMessage(model, "查询错误：" + e.getMessage());
		}
		model.addAttribute(MODEL_ATTR_PAGE, page);
		model.addAttribute(MODEL_ATTR_ENTITY, searchModel);
		return viewForm;
	}


	/**
	 * 结案窗口
	 * @param id		投诉单id
	 * @param quarter 	分片
	 */
	@RequiresPermissions(value="sd:complain:complete")
	@RequestMapping(value = "/complete", method = RequestMethod.GET)
	public String completeForm(@RequestParam String id, @RequestParam(required = false) String quarter, Model model) {
		User user = UserUtils.getUser();
		OrderComplain complain = new OrderComplain();
		String formView = "modules/sd/complain/completeForm";
		if(StringUtils.isBlank(id)){
			addMessage(model, "参数为空。");
			model.addAttribute("canAction", false);
			model.addAttribute(MODEL_ATTR_ENTITY,complain);
			return formView;
		}

		Long lid = null;
		try {
			lid = Long.valueOf(id);
		}catch (Exception e){
			lid = 0l;
		}
		if (lid == null || lid <= 0){
			addMessage(model, "投诉单参数类型错误。");
			model.addAttribute("canAction", false);
			model.addAttribute(MODEL_ATTR_ENTITY,complain);
			return formView;
		}
		try {
			complain = orderComplainService.getComplain(lid, quarter);
		}catch (Exception e){
			addMessage(model, "投诉单读取错误。");
			model.addAttribute("canAction", false);
			model.addAttribute(MODEL_ATTR_ENTITY,complain);
			return formView;
		}
		if(complain ==null){
			addMessage(model, "读取投诉单失败或不存在，请重试。");
			model.addAttribute("canAction", false);
			complain=new OrderComplain();
			return formView;
		}


		int statusValue = Integer.valueOf(complain.getStatus().getValue());
		if(statusValue == OrderComplain.STATUS_CLOSED){
			addMessage(model, "投诉单已结案，不能操作。");
			model.addAttribute("canAction", false);
			model.addAttribute(MODEL_ATTR_ENTITY,complain);
			return formView;
		}
		if(statusValue==OrderComplain.STATUS_PROCESSING && complain.getJudgeDate() == null){
			addMessage(model, "投诉单还未判定，不能结案。");
			model.addAttribute("canAction", false);
			model.addAttribute(MODEL_ATTR_ENTITY,complain);
			return formView;
		}
		//servicePoint
		ServicePoint servicePoint = complain.getServicePoint();
		if(servicePoint != null && servicePoint.getId()>0){
			ServicePoint msServicePoint = servicePointService.getFromCache(complain.getServicePoint().getId());
			if(msServicePoint == null){
				addMessage(model, "读取网点失败，请重试。");
				model.addAttribute("canAction", false);
				model.addAttribute(MODEL_ATTR_ENTITY,complain);
				return formView;
			}else{
				servicePoint = msServicePoint;
				complain.setServicePoint(msServicePoint);
			}
		}
		if(complain.getAttachmentQty()>0) {
			List<OrderComplainAttachment> attachments = orderComplainService.getComplainAttachements(lid, quarter, 0);
			if (attachments != null && attachments.size() > 0) {
				complain.setApplyAttaches(attachments);
			}
		}

		//加载判定附件
		List<OrderComplainAttachment> judgeattachments = orderComplainService.getComplainAttachements(lid, quarter, OrderComplainAttachment.ATTACHMENTTYPE_JUDEG);
		if (judgeattachments != null && judgeattachments.size() > 0) {
			complain.setJudgeAttaches(judgeattachments);
		}

		//投诉对象complain_object
		if(complain.getComplainObject()>0){
			complain.setComplainObjectsIds(BitUtils.getPositions(complain.getComplainObject(),String.class));
			if(complain.getComplainObjectsIds() != null && complain.getComplainObjectsIds().size() > 0) {
				complain.setComplainObjects(MSDictUtils.getDictInclueList("complain_object",Joiner.on(",").join(complain.getComplainObjectsIds())));
			}
		}
		//投诉项目complain_item
		if(complain.getComplainItem()>0){
			complain.setComplainItemsIds(BitUtils.getPositions(complain.getComplainItem(),String.class));
			if(complain.getComplainItemsIds() != null && complain.getComplainItemsIds().size() > 0) {
				complain.setComplainItems(MSDictUtils.getDictInclueList("complain_item",Joiner.on(",").join(complain.getComplainItemsIds())));
			}
		}

		Customer customer = customerService.getFromCache(complain.getCustomer().getId());
		complain.setCustomer(customer);

		//判定
		//对象
		if(complain.getJudgeObject()>0){
			complain.setJudgeObjectsIds(BitUtils.getPositions(complain.getJudgeObject(),String.class));
			if(complain.getJudgeObjectsIds() != null && complain.getJudgeObjectsIds().size() > 0) {
				complain.setJudgeObjects(MSDictUtils.getDictInclueList("judge_object",Joiner.on(",").join(complain.getJudgeObjectsIds())));
			}
		}
		//test
		//complain.setJudgeObjectsIds(Lists.newArrayList("3"));
		//项目
		if(complain.getJudgeItem()>0){
			complain.setJudgeItemsIds(BitUtils.getPositions(complain.getJudgeItem(),String.class));
		}
		//status
		//Dict dict = MSDictUtils.getDictByValue(complain.getStatus().getValue(),"complain_status");//切换为微服务
		Dict dict = getStatus(complain.getStatus().getValue());
		if (dict != null) {
			complain.setStatus(dict);
		}
		//servicepoints
		//List<ServicePoint> servicePoints = orderService.getOrderServicePoints(complain.getOrderId(),complain.getQuarter());
		//complain.setServicePoints(servicePoints);
		model.addAttribute("canAction", true);
		model.addAttribute(MODEL_ATTR_ENTITY,complain);
		return formView;
	}

	/**
	 * 提交结案
	 */
	@RequiresPermissions(value =  "sd:complain:complete")
	@ResponseBody
	@RequestMapping(value = "/savecomplete")
	public AjaxJsonEntity saveComplete(OrderComplain complain,HttpServletRequest request, HttpServletResponse response)
	{
		response.setContentType("application/json; charset=UTF-8");
		AjaxJsonEntity result = new AjaxJsonEntity(true);
		if(complain == null){
			result.setSuccess(false);
			result.setMessage("传入参数为空。");
			return result;
		}

		User user = UserUtils.getUser();
		if(user==null || user.getId()==null){
			result.setSuccess(false);
			result.setMessage("登录超时，请重新登录。");
			return result;
		}
		OrderComplain orgComplain = null;
		try
		{
			orgComplain = orderComplainService.getComplain(complain.getId(),complain.getQuarter());
			if(orgComplain == null){
				result.setSuccess(false);
				result.setMessage("读取单据错误，或投诉单不存在，请重试。");
				return result;
			}
			int statusValue = Integer.valueOf(orgComplain.getStatus().getValue());
			if(statusValue !=OrderComplain.STATUS_PROCESSING && statusValue!=OrderComplain.STATUS_APPEAL){
				result.setSuccess(false);
				Dict dict = getStatus(orgComplain.getStatus().getValue());
				result.setMessage("投诉单目前状态:" + dict.getLabel() +",不能结案");
				return result;
			}
			if(statusValue ==OrderComplain.STATUS_PROCESSING && orgComplain.getJudgeDate() == null){
				result.setSuccess(false);
				result.setMessage("投诉单还未判责,不能结案");
				return result;
			}

			StringBuffer message = new StringBuffer();
			if(StringUtils.isBlank(complain.getCompleteRemark())){
				message.append(",处理意见");
			}
			if(complain.getCompleteResultIds().size()==0){
				message.append(",处理方案");
			}
			if(message.length()>0) {
				result.setSuccess(false);
				result.setMessage("请输入或选择"+message.toString().substring(1));
				return result;
			}
			if(complain.getAmerceResultIds().contains("1") && (orgComplain.getServicePoint().getId() == null || orgComplain.getServicePoint().getId() == 0) ){
				result.setSuccess(false);
				result.setMessage("判责时未判定网点责任，不能对网点处罚。");
				return result;
			}
			//2020-10-14 get datasource from order
			Order order = orderService.getOrderById(orgComplain.getOrderId(),orgComplain.getQuarter(), OrderUtils.OrderDataLevel.HEAD,true);
			if(order == null){
				result.setSuccess(false);
				result.setMessage("读取订单信息错误。");
				return result;
			}
			complain.setDataSource(order.getDataSourceId());
			UserUtils.substrUserName(user,10);
			complain.setCompleteBy(user);
			complain.setCompleteDate(new Date());
			Integer minStatus = null;
			if(B2BDataSourceEnum.isB2BDataSource(order.getDataSourceId())) {
				//多投诉单处理
				List<LongTwoTuple> complainStatus = orderComplainService.getStatusByOrder(orgComplain.getOrderId(),order.getQuarter());
				if(!CollectionUtils.isEmpty(complainStatus) && complainStatus.size() > 1) {
					//筛选状态：0-待处理，1-处理中，3-申诉
					List<Long> statusRange = Lists.newArrayList(0L, 1L, 3L);
					minStatus = complainStatus.stream().filter(t -> statusRange.contains(t.getBElement()) && !t.getAElement().equals(complain.getId())).map(t -> t.getBElement().intValue()).min(Integer::compareTo).orElse(null);
				}
			}
			orderComplainService.completeComplain(complain,orgComplain,minStatus);
			result.setData(OrderComplain.STATUS_PROCESSING.toString());
		} catch (Exception e)
		{
			result.setSuccess(false);
			result.setMessage("处理失败，请关闭后重新打开窗口。<br>" + ExceptionUtil.getRootCauseMessage(e));
			log.error("[ComplainController.saveComplete] id:{} ,userId:{}",complain.getId(),user.getId(),e);
			LogUtils.saveLog("投诉单-完成","ComplainController.saveComplete",complain.getId().toString(),e,user);
		}
		return result;
	}

	//endregion 结案

	//region 申诉

	/**
	 * 申诉列表
	 */
	@RequiresPermissions(value ="sd:complain:complete")
	@RequestMapping(value = "/appeallist")
	public String appealList(ComplainSearchModel searchModel, HttpServletRequest request, HttpServletResponse response, Model model) {
		String viewForm = "modules/sd/complain/appealList";
	    Page<OrderComplain> page = new Page<OrderComplain>();
		User user = UserUtils.getUser();
        searchModel = setSearchModel(model, user,searchModel,3,365,true,365);
		if(!searchModel.getIsValid()){
			model.addAttribute(MODEL_ATTR_PAGE, page);
			model.addAttribute(MODEL_ATTR_ENTITY, searchModel);
			return viewForm;
		}
        Boolean isValid = checkOrderNoAndPhone(searchModel,model,page);
        if(!isValid){
			model.addAttribute(MODEL_ATTR_PAGE, page);
			model.addAttribute(MODEL_ATTR_ENTITY, searchModel);
            return viewForm;
        }
		//状态：所有
		//加载处理中的投诉单
		searchModel.setStatus(new Dict(OrderComplain.STATUS_APPEAL.toString()));
		try {
			//查询
			//设置主要为了排序
			searchModel.setSearchType("appeal");
			page = orderComplainService.findComplainList(new Page<ComplainSearchModel>(request, response), searchModel);

		} catch (Exception e) {
			addMessage(model, "查询错误：" + e.getMessage());
		}
		model.addAttribute(MODEL_ATTR_PAGE, page);
		model.addAttribute(MODEL_ATTR_ENTITY, searchModel);
		return "modules/sd/complain/appealList";
	}

	/**
	 * 申诉处理窗口
	 * @param id		投诉单id
	 * @param quarter 	分片
	 */
	@RequiresPermissions(value="sd:complain:complete")
	@RequestMapping(value = "/appealDealForm", method = RequestMethod.GET)
	public String appealDealForm(@RequestParam String id, @RequestParam(required = false) String quarter, Model model) {
		User user = UserUtils.getUser();
		OrderComplain complain = new OrderComplain();
		String formView = "modules/sd/complain/appealDealForm";
		if(StringUtils.isBlank(id)){
			addMessage(model, "参数为空。");
			model.addAttribute("canAction", false);
			model.addAttribute(MODEL_ATTR_ENTITY,complain);
			return formView;
		}

		Long lid = null;
		try {
			lid = Long.valueOf(id);
		}catch (Exception e){
			lid = 0l;
		}
		if (lid == null || lid <= 0){
			addMessage(model, "投诉单参数类型错误。");
			model.addAttribute("canAction", false);
			model.addAttribute(MODEL_ATTR_ENTITY,complain);
			return formView;
		}
		try {
			complain = orderComplainService.getComplain(lid, quarter);
		}catch (Exception e){
			addMessage(model, "投诉单读取错误。");
			model.addAttribute("canAction", false);
			model.addAttribute(MODEL_ATTR_ENTITY,complain);
			return formView;
		}
		if(complain ==null){
			addMessage(model, "读取投诉单失败或不存在，请重试。");
			model.addAttribute("canAction", false);
			complain=new OrderComplain();
			return formView;
		}
		int statusValue = Integer.valueOf(complain.getStatus().getValue());
		if(statusValue == OrderComplain.STATUS_CLOSED){
			addMessage(model, "投诉单已结案，不能操作。");
			model.addAttribute("canAction", false);
			model.addAttribute(MODEL_ATTR_ENTITY,complain);
			return formView;
		}
		if(statusValue==OrderComplain.STATUS_PROCESSING && complain.getJudgeDate() == null){
			addMessage(model, "投诉单还未判定，不能结案。");
			model.addAttribute("canAction", false);
			model.addAttribute(MODEL_ATTR_ENTITY,complain);
			return formView;
		}
		//servicePoint
		ServicePoint servicePoint = complain.getServicePoint();
		if(servicePoint != null && servicePoint.getId()>0){
			ServicePoint msServicePoint = servicePointService.getFromCache(complain.getServicePoint().getId());
			if(msServicePoint == null){
				addMessage(model, "读取网点失败，请重试。");
				model.addAttribute("canAction", false);
				model.addAttribute(MODEL_ATTR_ENTITY,complain);
				return formView;
			}else{
				servicePoint = msServicePoint;
				complain.setServicePoint(msServicePoint);
			}
		}
		//status
		//Dict dict = MSDictUtils.getDictByValue(complain.getStatus().getValue(),"complain_status");//切换为微服务
		Dict dict = getStatus(String.valueOf(complain.getStatus().getValue()));
		if (dict != null) {
			complain.setStatus(dict);
		}
		if(complain.getAttachmentQty()>0) {
			List<OrderComplainAttachment> attachments = orderComplainService.getComplainAttachements(lid, quarter, 0);
			if (attachments != null && attachments.size() > 0) {
				complain.setApplyAttaches(attachments);
			}
		}

		//加载判定附件
		List<OrderComplainAttachment> judgeattachments = orderComplainService.getComplainAttachements(lid, quarter, OrderComplainAttachment.ATTACHMENTTYPE_JUDEG);
		if (judgeattachments != null && judgeattachments.size() > 0) {
			complain.setJudgeAttaches(judgeattachments);
		}

		//投诉对象complain_object
		if(complain.getComplainObject()>0){
			complain.setComplainObjectsIds(BitUtils.getPositions(complain.getComplainObject(),String.class));
			if(complain.getComplainObjectsIds() != null && complain.getComplainObjectsIds().size() > 0) {
				complain.setComplainObjects(MSDictUtils.getDictInclueList("complain_object",Joiner.on(",").join(complain.getComplainObjectsIds())));
			}
		}
		//投诉项目complain_item
		if(complain.getComplainItem()>0){
			complain.setComplainItemsIds(BitUtils.getPositions(complain.getComplainItem(),String.class));
			if(complain.getComplainItemsIds() != null && complain.getComplainItemsIds().size() > 0) {
				complain.setComplainItems(MSDictUtils.getDictInclueList("complain_item",Joiner.on(",").join(complain.getComplainItemsIds())));
			}
		}
		Customer customer = customerService.getFromCache(complain.getCustomer().getId());
		complain.setCustomer(customer);

		//判定
		//对象
		if(complain.getJudgeObject()>0){
			complain.setJudgeObjectsIds(BitUtils.getPositions(complain.getJudgeObject(),String.class));
			if(complain.getJudgeObjectsIds() != null && complain.getJudgeObjectsIds().size() > 0) {
				complain.setJudgeObjects(MSDictUtils.getDictInclueList("judge_object",Joiner.on(",").join(complain.getJudgeObjectsIds())));
			}
		}
		//test
		//complain.setJudgeObjectsIds(Lists.newArrayList("3"));
		//项目
		if(complain.getJudgeItem()>0){
			complain.setJudgeItemsIds(BitUtils.getPositions(complain.getJudgeItem(),String.class));
		}
		//servicepoints
		//List<ServicePoint> servicePoints = orderService.getOrderServicePoints(complain.getOrderId(),complain.getQuarter());
		//complain.setServicePoints(servicePoints);

		//complete_result
		if(complain.getCompleteResult()>0){
			complain.setCompleteResultIds(BitUtils.getPositions(complain.getCompleteResult(),String.class));
			if(complain.getCompleteResultIds() != null && complain.getCompleteResultIds().size() > 0) {
				complain.setCompleteResults(MSDictUtils.getDictInclueList("complete_result",Joiner.on(",").join(complain.getCompleteResultIds())));
			}
		}

		model.addAttribute("canAction", true);
		model.addAttribute(MODEL_ATTR_ENTITY,complain);
		return formView;
	}

	/**
	 * 申诉申请窗口
	 * @param id	投诉单id
	 */
	@RequiresPermissions(value="sd:complain:create")
	@RequestMapping(value = "/appealForm", method = RequestMethod.GET)
	public String appealForm(@RequestParam(required = false) String id,
							 @RequestParam(required = false) String complainNo,
							 @RequestParam(required = false) String quarter, Model model) {
		User user = UserUtils.getUser();
		OrderComplainLog complainlog = new OrderComplainLog();
		String formView = "modules/sd/complain/appealForm";

		Long lid = null;
		try{
			lid = Long.valueOf(id);
			complainlog.setComplainId(lid);
			complainlog.setQuarter(quarter);
			model.addAttribute("canAction", true);
			model.addAttribute("complainlog", complainlog);
			model.addAttribute("complainNo", complainNo);
		}catch (Exception e){
			model.addAttribute("canAction", false);
			addMessage(model,"错误：参数错误");
		}

		return formView;
	}

	/**
	 * 提交申诉
	 */
	@RequiresPermissions(value="sd:complain:create")
	@ResponseBody
	@RequestMapping(value = "/ajax/appealSave")
	public AjaxJsonEntity appealSave(OrderComplainLog complainlog, HttpServletRequest request, HttpServletResponse response, Model model) {
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
			UserUtils.substrUserName(user,30);
			complainlog.setCreateBy(user);
			complainlog.setCreateDate(new Date());
			Dict status =new Dict(OrderComplain.STATUS_APPEAL,"申诉");
			complainlog.setStatus(status);
			orderComplainService.saveAppeal(complainlog);

		} catch (Exception e)
		{
			jsonEntity.setSuccess(false);
			jsonEntity.setMessage(e.getMessage());
		}
		return jsonEntity;
	}

	//endregion 申诉

	//region 所有

	/**
	 * 所有列表
	 */
	@RequestMapping(value = "/alllist")
	public String allList(ComplainSearchModel searchModel, HttpServletRequest request, HttpServletResponse response, Model model) {
		String viewForm = "modules/sd/complain/allList";
		Page<OrderComplain> page = new Page<OrderComplain>();
		User user = UserUtils.getUser();
		searchModel = setSearchModel(model, user,searchModel,3,365,true,365);
		if(!searchModel.getIsValid()){
			model.addAttribute(MODEL_ATTR_PAGE, page);
			model.addAttribute(MODEL_ATTR_ENTITY, searchModel);
			return viewForm;
		}
		Boolean isValid = checkOrderNoAndPhone(searchModel,model,page);
		if(!isValid){
			model.addAttribute(MODEL_ATTR_PAGE, page);
			model.addAttribute(MODEL_ATTR_ENTITY, searchModel);
			return viewForm;
		}
        //状态
        //加载处理中的投诉单
        //searchModel.setStatus(null);//all
		try {
			//根据查询类型进行不同的排序
			searchModel.setSearchType("all");
			page = orderComplainService.findComplainList(new Page<ComplainSearchModel>(request, response), searchModel);

		} catch (Exception e) {
			addMessage(model, "查询错误：" + e.getMessage());
		}
		model.addAttribute(MODEL_ATTR_PAGE, page);
		model.addAttribute(MODEL_ATTR_ENTITY, searchModel);
		return viewForm;
	}

	//endregion

	//region 公共
	/**
	 * 浏览窗口
	 * @param id		投诉单id
	 * @param quarter 	分片
	 */
	@RequiresPermissions(value={"sd:complain:complete","sd:complain:judge","sd:complain:create","sd:complain:view"},logical = Logical.OR)
	@RequestMapping(value = "/view", method = RequestMethod.GET)
	public String viewForm(@RequestParam String id, @RequestParam(required = false) String quarter, Model model) {
		User user = UserUtils.getUser();
		OrderComplain complain = new OrderComplain();
		String formView = "modules/sd/complain/viewForm";
		if(StringUtils.isBlank(id)){
			addMessage(model, "参数为空。");
			model.addAttribute("canAction", false);
			model.addAttribute(MODEL_ATTR_ENTITY,complain);
			return formView;
		}

		Long lid = null;
		try {
			lid = Long.valueOf(id);
		}catch (Exception e){
			lid = 0l;
		}
		if (lid == null || lid <= 0){
			addMessage(model, "投诉单参数类型错误。");
			model.addAttribute("canAction", false);
			model.addAttribute(MODEL_ATTR_ENTITY,complain);
			return formView;
		}
		try {
			complain = orderComplainService.getComplain(lid, quarter);
		}catch (Exception e){
			addMessage(model, "投诉单读取错误。");
			model.addAttribute("canAction", false);
			model.addAttribute(MODEL_ATTR_ENTITY,complain);
			return formView;
		}
		if(complain ==null){
			addMessage(model, "读取投诉单失败或不存在，请重试。");
			model.addAttribute("canAction", false);
			complain=new OrderComplain();
			return formView;
		}

		if(complain.getAttachmentQty()>0) {
			List<OrderComplainAttachment> attachments = orderComplainService.getComplainAttachements(lid, quarter, 0);
			if (attachments != null && attachments.size() > 0) {
				complain.setApplyAttaches(attachments);
			}
		}

		//加载判定附件
		List<OrderComplainAttachment> judgeattachments = orderComplainService.getComplainAttachements(lid, quarter, OrderComplainAttachment.ATTACHMENTTYPE_JUDEG);
		if (judgeattachments != null && judgeattachments.size() > 0) {
			complain.setJudgeAttaches(judgeattachments);
		}
		//status
		//Dict dict = MSDictUtils.getDictByValue(complain.getStatus().getValue(),"complain_status");//切换为微服务
		Dict dict = getStatus(String.valueOf(complain.getStatus().getValue()));
		if (dict != null) {
			complain.setStatus(dict);
		}
		//投诉对象 complain_object
		if(complain.getComplainObject()>0){
			complain.setComplainObjectsIds(BitUtils.getPositions(complain.getComplainObject(),String.class));
			if(complain.getComplainObjectsIds() != null && complain.getComplainObjectsIds().size() > 0) {
				complain.setComplainObjects(MSDictUtils.getDictInclueList("complain_object",Joiner.on(",").join(complain.getComplainObjectsIds())));
			}
		}
		//投诉项目 complain_item
		if(complain.getComplainItem()>0){
			complain.setComplainItemsIds(BitUtils.getPositions(complain.getComplainItem(),String.class));
			if(complain.getComplainItemsIds() != null && complain.getComplainItemsIds().size() > 0) {
				complain.setComplainItems(MSDictUtils.getDictInclueList("complain_item",Joiner.on(",").join(complain.getComplainItemsIds())));
			}
		}
		Customer customer = customerService.getFromCache(complain.getCustomer().getId());
		complain.setCustomer(customer);

		//判定
		//对象
		if(complain.getJudgeObject()>0){
			complain.setJudgeObjectsIds(BitUtils.getPositions(complain.getJudgeObject(),String.class));
            if(complain.getJudgeObjectsIds() != null && complain.getJudgeObjectsIds().size() > 0) {
                complain.setJudgeObjects(MSDictUtils.getDictInclueList("judge_object",Joiner.on(",").join(complain.getJudgeObjectsIds())));
            }
		}
		//test
		//complain.setJudgeObjectsIds(Lists.newArrayList("3"));
		//项目
		if(complain.getJudgeItem()>0){
			complain.setJudgeItemsIds(BitUtils.getPositions(complain.getJudgeItem(),String.class));
		}
		//判定
		//servicepoint
		ServicePoint servicePoint = complain.getServicePoint();
		if(servicePoint!= null && servicePoint.getId() != null && servicePoint.getId()>0){
			servicePoint = servicePointService.getFromCache(servicePoint.getId());
			if(servicePoint != null){
				complain.setServicePoint(servicePoint);
				complain.getServicePoints().add(servicePoint);
			}
		}
		//complete_result
		if(complain.getCompleteResult()>0){
			complain.setCompleteResultIds(BitUtils.getPositions(complain.getCompleteResult(),String.class));
            if(complain.getCompleteResultIds() != null && complain.getCompleteResultIds().size() > 0) {
                complain.setCompleteResults(MSDictUtils.getDictInclueList("complete_result",Joiner.on(",").join(complain.getCompleteResultIds())));
            }
		}
		model.addAttribute("canAction", true);
		model.addAttribute(MODEL_ATTR_ENTITY,complain);
		return formView;
	}

	/**
	 * [Ajax]订单日志-投诉列表
	 * @param complainId	投诉单id
	 * @quarter 分片
	 */
	@ResponseBody
	@RequestMapping(value = "/ajax/complainLogList")
	public AjaxJsonEntity complainLogList(@RequestParam String complainId, @RequestParam String quarter, HttpServletResponse response)
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
			Long lorderId = Long.valueOf(complainId);
			if(lorderId == null || lorderId <=0){
				jsonEntity.setSuccess(false);
				jsonEntity.setMessage("投诉单参数错误");
				return jsonEntity;
			}

			List<OrderComplainLog> list = orderComplainService.getComplainLogListByCompliaId(lorderId,quarter);
			if(list ==null){
				jsonEntity.setData(Lists.newArrayList());
			}else {
				//若当前用户是客户，则只显示客户可见的投诉单日志
				if (user.isCustomer()) {
					list = list.stream().filter(i-> i.getVisibilityFlag() == 0 || VisibilityFlagEnum.has(i.getVisibilityFlag(), VisibilityFlagEnum.CUSTOMER)).collect(Collectors.toList());
				}

				List<Dict> status = MSDictUtils.getDictList("complain_status");//切换为微服务
				OrderComplainLog complainlog;
				List<Dict> dictList;
				final StringBuffer buffer = new StringBuffer();
				Dict dict;
				for(int i=0,size=list.size();i<size;i++){
					complainlog = list.get(i);
					//status
					buffer.setLength(0);
					buffer.append(complainlog.getStatus().getValue());
					dict = status.stream().filter(t->t.getValue().equalsIgnoreCase(buffer.toString())).findFirst().orElse(null);
					if(dict != null){
						complainlog.setStatus(dict);
					}
				}
				jsonEntity.setData(list);
			}
		} catch (Exception e)
		{
			jsonEntity.setSuccess(false);
			jsonEntity.setMessage(e.getMessage());
		}
		return jsonEntity;
	}

	/**
	 * 保存申诉日志
	 * @param
	 */
	@RequiresPermissions(value="sd:complain:judge")
	@ResponseBody
	@RequestMapping(value = "/ajax/saveComplainlog")
	public AjaxJsonEntity saveComplainlog(HttpServletRequest request, HttpServletResponse response, Model model) {
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
			OrderComplainLog complainlog=new OrderComplainLog();
			complainlog.setContent(request.getParameter("complainLogContent").toString());
			complainlog.setComplainId(Long.valueOf(request.getParameter("complainId")));
			complainlog.setQuarter(request.getParameter("quarter"));
			int visibilityFlag = StringUtils.toInteger(request.getParameter("visibilityFlag"));
			int visibilityValue = (visibilityFlag == 0 ? VisibilityFlagEnum.subtract(OrderComplainLog.VISIBILITY_FLAG_ALL, VisibilityFlagEnum.CUSTOMER) : OrderComplainLog.VISIBILITY_FLAG_ALL);
			complainlog.setVisibilityFlag(visibilityValue);
			UserUtils.substrUserName(user,30);
			complainlog.setCreateBy(user);
			complainlog.setCreateDate(new Date());
			Dict status =new Dict(OrderComplain.STATUS_PROCESSING,"处理中");
			complainlog.setStatus(status);
			orderComplainService.addOrderComplainLog(complainlog);
		} catch (Exception e)
		{
			jsonEntity.setSuccess(false);
			jsonEntity.setMessage(e.getMessage());
		}
		return jsonEntity;
	}

	/**
	 * 查看订单明细(客服)
	 * @param id	订单id
	 * @return
	 */
	@RequestMapping(value = { "/orderDetailInfo" })
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
				addMessage(model,"错误：读取订单失败，请重试!");
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
		model.addAttribute("refreshParent",StringUtils.isBlank(refreshParent)?"true":refreshParent);//调用方法决定是否在关闭详情页后刷新iframe
		String changed = request.getParameter("changed");
		model.addAttribute("changed",StringUtils.isBlank(changed)?"false":changed);
		return "modules/sd/complain/complainOrderDefailInfoForm";
	}

	/**
	 * 读取用户产品类别权限数据
	 * @param model
	 * @param searchModel
	 * @param user
	 * @return
	 */
	private Boolean loadProductCategories(Model model, ComplainSearchModel searchModel, User user) {
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
		/*
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
		}*/
		model.addAttribute("categories", categories);
		return true;
	}

    @PostMapping("/upload")
    public void fileUpload(HttpServletRequest request, HttpServletResponse response) throws IOException{
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        Long complainId = StringUtils.toLong(request.getParameter("complainId"));
        String quarter = request.getParameter("quarter");
        if(complainId<=0 || StringUtils.isBlank(quarter)){
            log.error("参数错误 - complainId:{},quarter:{}",complainId,quarter);
            response.getWriter().print("{id:'',fileName:'参数错误',status:'false',origalName:'','isImage':false}");
            return;
        }
        Map<Long,String> map = uploadFileService.getRootFolder("");
        Long id = sequenceIdService.nextId();
        String json;
        try {
            json = uploadFileService.uploadSingle(id, map.get(0L), map.get(1L), request, response);
        }catch (Exception e){
            log.error("上传文件失败",e);
            response.getWriter().print("{id:'',fileName:'读取上传文件失败',status:'false',origalName:'','isImage':false}");
            return;
        }
        UploadFileModel entity = null;
        entity = GsonUtils.getInstance().fromJson(json, UploadFileModel.class);
        if(entity == null){
            log.error("解析json失败:{}",json);
            response.getWriter().print("{id:'',fileName:'解析上传结果失败',status:'false',origalName:'','isImage':false}");
            return;
        }

        if(entity.getStatus().equalsIgnoreCase("success")){
            User user = UserUtils.getUser();
            OrderComplainAttachment attach = new OrderComplainAttachment();
            attach.setId(id);
            attach.setComplainId(complainId);
            attach.setQuarter(quarter);
            attach.setCreateBy(user);
            attach.setCreateDate(new Date());
            attach.setAttachmentType(OrderComplainAttachment.ATTACHMENTTYPE_JUDEG);
            attach.setFileName(StringUtils.substringAfterLast(entity.getFileName(),"/"));
            attach.setFilePath(entity.getFileName());
            try {
				orderComplainService.insertComplainAttachement(attach);
                response.getWriter().print(json);
            }catch (Exception e){
                log.error("文件上传成功，保存数据库失败:{}",json,e);
                response.getWriter().print("{id:'',fileName:'件上传成功，保存数据库失败',status:'false',origalName:'','isImage':false}");
            }
        }
    }

	//endregion 公共

	/**
	 * [Ajax]订单日志-投诉列表
	 * @param orderId	订单id
	 */
	@ResponseBody
	@RequestMapping(value = "/ajax/list")
	public AjaxJsonEntity orderComplainList(@RequestParam String orderId, @RequestParam String quarter, HttpServletResponse response)
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
			List<OrderComplain> list = orderComplainService.getComplainListByOrder(lorderId,"",quarter,true);
			if(list ==null){
				jsonEntity.setData(Lists.newArrayList());
			}else {
				Order order = orderService.getOrderById(lorderId, quarter,OrderUtils.OrderDataLevel.DETAIL,true);
				if(order!=null){
					for(OrderComplain orderComplain:list){
						orderComplain.setDataSource(order.getDataSourceId());
					}
				}
				jsonEntity.setData(list);
			}
		} catch (Exception e)
		{
			jsonEntity.setSuccess(false);
			jsonEntity.setMessage(e.getMessage());
		}
		return jsonEntity;
	}
	/**
	 * [Ajax]撤销投诉单
	 * @param complainId	投诉单id
	 * @param quarter 分片
	 */
	@ResponseBody
	@RequestMapping(value = "/ajax/cancleComplain")
	public AjaxJsonEntity cancleComplain(@RequestParam String complainId, @RequestParam String quarter, HttpServletResponse response)
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
			Long LcomplainId = Long.valueOf(complainId);
			if(LcomplainId == null || LcomplainId <=0){
				jsonEntity.setSuccess(false);
				jsonEntity.setMessage("投诉单参数错误");
				return jsonEntity;
			}

			orderComplainService.cancleComplain(LcomplainId,quarter,user);

		} catch (Exception e)
		{
			jsonEntity.setSuccess(false);
			jsonEntity.setMessage(e.getMessage());
		}
		return jsonEntity;
	}

	private Dict getStatus(String statusValue){
		Dict dict = null;
		try{
			dict = MSDictUtils.getDictByValue(statusValue,"complain_status");//切换为微服务
		}catch (Exception e){
			log.error("读取数据字典错误,type:{},value:{}","complain_status",statusValue,e);
		}
		if(dict==null) {
			dict = new Dict(statusValue);
			int intValue = 0;
			try {
				intValue = Integer.parseInt(statusValue);
				switch (intValue) {
					case 0:
						dict.setLabel("待处理");
						break;
					case 1:
						dict.setLabel("处理中");
						break;
					case 2:
						dict.setLabel("已关闭");
						break;
					case 3:
						dict.setLabel("已申诉");
						break;
					case 4:
						dict.setLabel("已撤销");
						break;
					default:
						dict.setLabel("状态错误");
						break;
				}
			} catch (Exception e) {
				dict.setValue("0");
				dict.setLabel("状态错误");
			}
		}
		return dict;
	}

	//region 新判定窗口

	@RequiresPermissions(value="sd:complain:judge")
	@RequestMapping(value = "/judgeNew", method = RequestMethod.GET)
	public String judgeFormNew(@RequestParam String id, @RequestParam(required = false) String quarter, Model model) {
		User user = UserUtils.getUser();
		OrderComplain complain = new OrderComplain();
		String formView = "modules/sd/complain/judgeFormNew";
		if(StringUtils.isBlank(id)){
			addMessage(model, "参数为空。");
			model.addAttribute("canAction", false);
			model.addAttribute(MODEL_ATTR_ENTITY,complain);
			return formView;
		}

		Long lid = null;
		try {
			lid = Long.valueOf(id);
		}catch (Exception e){
			lid = 0l;
		}
		if (lid == null || lid <= 0){
			addMessage(model, "投诉单参数类型错误。");
			model.addAttribute("canAction", false);
			model.addAttribute(MODEL_ATTR_ENTITY,complain);
			return formView;
		}
		try {
			complain = orderComplainService.getComplain(lid, quarter);
		}catch (Exception e){
			addMessage(model, "投诉单读取错误。");
			model.addAttribute("canAction", false);
			model.addAttribute(MODEL_ATTR_ENTITY,complain);
			return formView;
		}
		if(complain ==null){
			addMessage(model, "读取投诉单失败或不存在，请重试。");
			model.addAttribute("canAction", false);
			complain=new OrderComplain();
			return formView;
		}
		int statusValue = Integer.valueOf(complain.getStatus().getValue());
		if(statusValue == OrderComplain.STATUS_CLOSED){
			addMessage(model, "投诉单已结案，不能再判定。");
			model.addAttribute("canAction", false);
			model.addAttribute(MODEL_ATTR_ENTITY,complain);
			return formView;
		}
		if(complain.getAttachmentQty()>0) {
			List<OrderComplainAttachment> attachments = orderComplainService.getComplainAttachements(lid, quarter, 0);
			if (attachments != null && attachments.size() > 0) {
				complain.setApplyAttaches(attachments);
			}
		}
		//加载判定附件
		List<OrderComplainAttachment> judgeattachments = orderComplainService.getComplainAttachements(lid, quarter, OrderComplainAttachment.ATTACHMENTTYPE_JUDEG);
		if (judgeattachments != null && judgeattachments.size() > 0) {
			complain.setJudgeAttaches(judgeattachments);
		}

		if(statusValue == OrderComplain.STATUS_APPLIED){
			complain.setJudgeBy(user);
			complain.setJudgeDate(new Date());
		}
		//status
		//Dict dict = MSDictUtils.getDictByValue(complain.getStatus().getValue(),"complain_status");//切换为微服务
		Dict dict = getStatus(complain.getStatus().getValue());
		if (dict != null) {
			complain.setStatus(dict);
		}

		//投诉对象complain_object
		if(complain.getComplainObject()>0){
			complain.setComplainObjectsIds(BitUtils.getPositions(complain.getComplainObject(),String.class));
			if(complain.getComplainObjectsIds() != null && complain.getComplainObjectsIds().size() > 0) {
				complain.setComplainObjects(MSDictUtils.getDictInclueList("complain_object",Joiner.on(",").join(complain.getComplainObjectsIds())));
			}
		}
		//投诉项目complain_item
		if(complain.getComplainItem()>0){
			complain.setComplainItemsIds(BitUtils.getPositions(complain.getComplainItem(),String.class));
			if(complain.getComplainItemsIds() != null && complain.getComplainItemsIds().size() > 0) {
				complain.setComplainItems(MSDictUtils.getDictInclueList("complain_item",Joiner.on(",").join(complain.getComplainItemsIds())));
			}
		}
		Customer customer = customerService.getFromCache(complain.getCustomer().getId());
		complain.setCustomer(customer);

		//判定
		//对象
		if(complain.getJudgeObject()>0){
			complain.setJudgeObjectsIds(BitUtils.getPositions(complain.getJudgeObject(),String.class));
		}
		//test
		//complain.setJudgeObjectsIds(Lists.newArrayList("3"));
		//项目
		if(complain.getJudgeItem()>0){
			complain.setJudgeItemsIds(BitUtils.getPositions(complain.getJudgeItem(),String.class));
		}
		if(complain.getJudgeDate() == null){
			complain.setJudgeBy(user);
			complain.setJudgeDate(new Date());
		}
		//网点，根据上门服务取得
//		List<ServicePoint> servicePoints = orderService.getOrderServicePoints(complain.getOrderId(),complain.getQuarter());
		List<ServicePoint> servicePoints = orderService.getPlannedServicePoints(complain.getOrderId(),complain.getQuarter());
		//name = no-name
		servicePoints.stream().forEach(t->t.setName(t.getServicePointNo()+"-"+t.getName()));
		complain.setServicePoints(servicePoints);

		ServicePoint servicePoint = complain.getServicePoint();
		if(servicePoint != null && servicePoint.getId()>0){
			ServicePoint msServicePoint = servicePointService.getFromCache(complain.getServicePoint().getId());
			if(msServicePoint == null){
				addMessage(model, "读取网点失败，请重试。");
				model.addAttribute("canAction", false);
				model.addAttribute(MODEL_ATTR_ENTITY,complain);
				return formView;
			}else{
				servicePoint = msServicePoint;
				complain.setServicePoint(msServicePoint);
			}
		}

		model.addAttribute("canAction", true);
		model.addAttribute(MODEL_ATTR_ENTITY,complain);
		return formView;
	}

	/**
	 * 提交判定
	 */
	@RequiresPermissions(value =  "sd:complain:judge")
	@ResponseBody
	@RequestMapping(value = "/savejudgeNew")
	public AjaxJsonEntity saveJudgeNew(OrderComplain complain,HttpServletRequest request, HttpServletResponse response)
	{
		response.setContentType("application/json; charset=UTF-8");
		AjaxJsonEntity result = new AjaxJsonEntity(true);
		if(complain == null){
			result.setSuccess(false);
			result.setMessage("传入参数为空。");
			return result;
		}

		User user = UserUtils.getUser();
		if(user==null || user.getId()==null){
			result.setSuccess(false);
			result.setMessage("登录超时，请重新登录。");
			return result;
		}
		OrderComplain orgComplain = null;
		try
		{
			orgComplain = orderComplainService.getComplain(complain.getId(),complain.getQuarter());
			if(orgComplain == null){
				result.setSuccess(false);
				result.setMessage("读取单据错误，或投诉单不存在，请重试。");
				return result;
			}
			Dict dict = orgComplain.getStatus();
			int statusValue = Integer.valueOf(dict.getValue());
			if(statusValue != OrderComplain.STATUS_PROCESSING){
				result.setSuccess(false);
				dict = getStatus(orgComplain.getStatus().getValue());
				/*
                //Dict dict = MSDictUtils.getDictByValue(complain.getStatus().getValue(),"complain_status");//切换为微服务
                if(dict==null){
                    //dict = complain.getStatus();
                    dict = orgComplain.getStatus();
                    if(statusValue==1){
                        dict.setLabel("处理中");
                    }else if(statusValue==2){
                        dict.setLabel("已结案");
                    }
                }*/
				result.setMessage("投诉单目前状态:" + dict.getLabel() +",不能保存判定");
				return result;
			}
			Order order = orderService.getOrderById(orgComplain.getOrderId(),orgComplain.getQuarter(), OrderUtils.OrderDataLevel.HEAD,true);
			if(order == null){
				result.setSuccess(false);
				result.setMessage("读取订单信息错误。");
				return result;
			}
			StringBuffer message = new StringBuffer();
			if(StringUtils.isBlank(complain.getJudgeRemark())){
				message.append(",判责意见");
			}
			if(complain.getJudgeObjectsIds().size()==0){
				message.append(",责任对象");
			}
			if(complain.getJudgeObjectsIds().contains("1") && (complain.getServicePoint() == null || complain.getServicePoint().getId() == null || complain.getServicePoint().getId() == 0)){
				message.append(",责任网点");
			}
			if(complain.getJudgeObjectsIds().size()==0){
				message.append(",责任项目");
			}
			if(message.length()>0) {
				result.setSuccess(false);
				result.setMessage("请输入或选择"+message.toString().substring(1));
				return result;
			}
			complain.setDataSource(order.getDataSourceId());
			complain.setOrderId(orgComplain.getOrderId());//*
			complain.setComplainNo(orgComplain.getComplainNo());
			complain.setB2bComplainNo(orgComplain.getB2bComplainNo());
			UserUtils.substrUserName(user,10);
			complain.setJudgeBy(user);
			complain.setJudgeDate(new Date());
			complain.setCreateType(orgComplain.getCreateType());
			orderComplainService.judgeComplain(complain);
			Map<String, Object> objMap = Maps.newHashMap();
			if (complain.getJudgeObjectsIds().contains("1")
					&& (complain.getServicePoint() != null && complain.getServicePoint().getId() != null && complain.getServicePoint().getId() > 0)) {
				ServicePoint msServicePoint = servicePointService.getFromCache(complain.getServicePoint().getId());
				objMap.put("servicePoint", msServicePoint);
			}
			objMap.put("status", OrderComplain.STATUS_PROCESSING.toString());
//			result.setData(OrderComplain.STATUS_PROCESSING.toString());
			result.setData(objMap);

		} catch (Exception e)
		{
			result.setSuccess(false);
			result.setMessage("处理失败，请关闭后重新打开窜口");
			log.error("[ComplainController.saveJudge] id:{} ,userId:{}",complain.getId(),user.getId(),e);
			//LogUtils.saveLog("投诉单-判定","ComplainController.saveJudge",complain.getId().toString(),e,user);
		}
		return result;
	}

	@RequiresPermissions(value =  "sd:complain:complete")
	@ResponseBody
	@RequestMapping(value = "/savecompleteNew")
	public AjaxJsonEntity saveCompleteNew(OrderComplain complain,HttpServletRequest request, HttpServletResponse response)
	{
		response.setContentType("application/json; charset=UTF-8");
		AjaxJsonEntity result = new AjaxJsonEntity(true);
		if(complain == null){
			result.setSuccess(false);
			result.setMessage("传入参数为空。");
			return result;
		}

		User user = UserUtils.getUser();
		if(user==null || user.getId()==null){
			result.setSuccess(false);
			result.setMessage("登录超时，请重新登录。");
			return result;
		}
		OrderComplain orgComplain = null;
		try
		{
			orgComplain = orderComplainService.getComplain(complain.getId(),complain.getQuarter());
			if(orgComplain == null){
				result.setSuccess(false);
				result.setMessage("读取单据错误，或投诉单不存在，请重试。");
				return result;
			}
			int statusValue = Integer.valueOf(orgComplain.getStatus().getValue());
			if(statusValue !=OrderComplain.STATUS_PROCESSING && statusValue!=OrderComplain.STATUS_APPEAL){
				result.setSuccess(false);
				Dict dict = getStatus(orgComplain.getStatus().getValue());
				result.setMessage("投诉单目前状态:" + dict.getLabel() +",不能结案");
				return result;
			}
			if(statusValue ==OrderComplain.STATUS_PROCESSING && orgComplain.getJudgeDate() == null){
				result.setSuccess(false);
				result.setMessage("投诉单还未判责,不能结案");
				return result;
			}

			StringBuffer message = new StringBuffer();
			if(StringUtils.isBlank(complain.getCompleteRemark())){
				message.append(",处理意见");
			}
			if(complain.getCompleteResultIds().size()==0){
				message.append(",处理方案");
			}
			if(message.length()>0) {
				result.setSuccess(false);
				result.setMessage("请输入或选择"+message.toString().substring(1));
				return result;
			}
			if(complain.getAmerceResultIds().contains("1") && (orgComplain.getServicePoint().getId() == null || orgComplain.getServicePoint().getId() == 0) ){
				result.setSuccess(false);
				result.setMessage("判责时未判定网点责任，不能对网点处罚。");
				return result;
			}
			//2020-10-14 get datasource from order
			Order order = orderService.getOrderById(orgComplain.getOrderId(),orgComplain.getQuarter(), OrderUtils.OrderDataLevel.HEAD,true);
			if(order == null){
				result.setSuccess(false);
				result.setMessage("读取订单信息错误。");
				return result;
			}
			complain.setDataSource(order.getDataSourceId());
			UserUtils.substrUserName(user,10);
			complain.setCompleteBy(user);
			complain.setCompleteDate(new Date());
			complain.setOrderId(orgComplain.getOrderId());
			Integer minStatus = null;
			if(B2BDataSourceEnum.isB2BDataSource(order.getDataSourceId())) {
				//多投诉单处理
				List<LongTwoTuple> complainStatus = orderComplainService.getStatusByOrder(orgComplain.getOrderId(),order.getQuarter());
				if(!CollectionUtils.isEmpty(complainStatus) && complainStatus.size() > 1) {
					//筛选状态：0-待处理，1-处理中，3-申诉
					List<Long> statusRange = Lists.newArrayList(0L, 1L, 3L);
					minStatus = complainStatus.stream().filter(t -> statusRange.contains(t.getBElement()) && !t.getAElement().equals(complain.getId())).map(t -> t.getBElement().intValue()).min(Integer::compareTo).orElse(null);
				}
			}
			orderComplainService.completeComplain(complain,orgComplain,minStatus);
			result.setData(OrderComplain.STATUS_PROCESSING.toString());

		} catch (Exception e)
		{
			result.setSuccess(false);
			result.setMessage("处理失败，请关闭后重新打开窜口");
			log.error("[ComplainController.saveComplete] id:{} ,userId:{}",complain.getId(),user.getId(),e);
			LogUtils.saveLog("投诉单-完成","ComplainController.saveComplete",complain.getId().toString(),e,user);
		}
		return result;
	}

	//endregion 新判定窗口

	//region Test
	@ResponseBody
	@RequestMapping(value = "mq/send/complain")
	public AjaxJsonEntity sendComplainForm(@RequestBody String json, HttpServletRequest request, HttpServletResponse response)  {
		response.setContentType("application/json; charset=UTF-8");
		AjaxJsonEntity result = new AjaxJsonEntity(true);
		if(StringUtils.isBlank(json)){
			result.setSuccess(false);
			result.setMessage("data empty");
		}else{
			MQB2BOrderComplainMessage.B2BOrderComplainMessage.Builder builder = MQB2BOrderComplainMessage.B2BOrderComplainMessage.newBuilder();
			try {
				new JsonFormat().merge(new ByteArrayInputStream(json.getBytes()), builder);
				MQB2BOrderComplainMessage.B2BOrderComplainMessage message = builder.build();
				complainMQSender.sendRetry(message,0);
			} catch (IOException e) {
				log.error("消息格式错误",e);
				result.setSuccess(false);
				result.setMessage("消息格式错误");
			}
		}
		result.setData("OK");
		return result;
	}

	//endregion Test
}
