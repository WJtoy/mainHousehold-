package com.wolfking.jeesite.modules.sd.web;

import cn.hutool.core.util.StrUtil;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.entity.cc.*;
import com.kkl.kklplus.entity.cc.vm.ReminderPageSearchModel;
import com.kkl.kklplus.entity.cc.vm.RereminderCheckRespone;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.config.Global;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.Exceptions;
import com.wolfking.jeesite.common.utils.PushMessageUtils;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.ProductCategory;
import com.wolfking.jeesite.modules.md.service.CustomerService;
import com.wolfking.jeesite.modules.md.service.ProductCategoryService;
import com.wolfking.jeesite.modules.sd.entity.*;
import com.wolfking.jeesite.modules.sd.service.OrderRegionService;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.modules.sd.utils.OrderCacheUtils;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.*;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.service.SystemService;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.modules.sys.utils.SeqUtils;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.b2bcenter.md.service.B2BCustomerMappingService;
import com.wolfking.jeesite.ms.b2bcenter.md.utils.B2BMDUtils;
import com.wolfking.jeesite.ms.cc.entity.ReminderModel;
import com.wolfking.jeesite.ms.cc.entity.mapper.ReminderViewModelMapper;
import com.wolfking.jeesite.ms.cc.service.ReminderService;
import com.wolfking.jeesite.ms.tmall.md.service.B2bCustomerMapService;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * 催单控制器
 *
 */
@Controller
@RequestMapping(value = "${adminPath}/sd/reminder")
@Slf4j
public class ReminderController extends BaseController
{
    private static final String MODEL_ATTR_PAGE = "page";
    private static final String MODEL_ATTR_ENTITY = "reminder";

	@Autowired
	private OrderService orderService;

	@Autowired
    private ReminderService reminderService;

	@Autowired
	private AreaService areaService;

	@Autowired
	private ProductCategoryService productCategoryService;

	@Autowired
    private SystemService systemService;

    @Autowired
    private OrderRegionService orderRegionService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private B2bCustomerMapService b2bCustomerMapService;

	//region 列表及读取

	/**
	 * 待回复列表
	 */
    @RequiresPermissions(value ="sd:reminder:view")
    @RequestMapping(value = "list/process")
	public String waitReplyList(ReminderPageSearchModel searchModel, HttpServletRequest request, HttpServletResponse response, Model model) {
        String viewForm = "modules/sd/reminder/list/waitReplyList";
        User user = UserUtils.getUser();
        searchModel.setStatus(ReminderStatus.WaitReply.getCode());
        Page page = new Page(request, response);
        long areaId = searchModel.getAreaId();
        searchModel = setSearchModel(model, user,searchModel,3,365);
        if(!searchModel.getIsValid()){
            model.addAttribute(MODEL_ATTR_PAGE, page);
            model.addAttribute(MODEL_ATTR_ENTITY, searchModel);
            return viewForm;
        }
        try {
            page = reminderService.waitReplyList(page, searchModel);
        } catch (Exception e) {
            addMessage(model, "查询错误：" + Exceptions.getRootCauseMessage(e));
        }finally {
            searchModel.setAreaId(areaId);//还原
        }
        model.addAttribute(MODEL_ATTR_PAGE, page);
        model.addAttribute(MODEL_ATTR_ENTITY, searchModel);
        return viewForm;
	}

    /**
     * 查询已回复列表(已回复,待客户/跟单确认)
     */
    @RequiresPermissions(value ="sd:reminder:view")
    @RequestMapping(value = "/list/haveRepliedList")
    public String haveRepliedList(ReminderPageSearchModel searchModel, HttpServletRequest request, HttpServletResponse response, Model model) {
        String viewForm = "modules/sd/reminder/list/haveReplied";
        User user = UserUtils.getUser();
        searchModel.setStatus(ReminderStatus.Replied.getCode());
        Page page = new Page(request, response);
        long areaId = searchModel.getAreaId();
        searchModel = setSearchModel(model, user,searchModel,3,365);
        if(!searchModel.getIsValid()){
            model.addAttribute(MODEL_ATTR_PAGE, page);
            model.addAttribute(MODEL_ATTR_ENTITY, searchModel);
            return viewForm;
        }

        try {
            page = reminderService.haveRepliedList(page, searchModel);
        } catch (Exception e) {
            addMessage(model, "查询错误：" + Exceptions.getRootCauseMessage(e));
        }finally {
            searchModel.setAreaId(areaId);//还原
        }
        model.addAttribute(MODEL_ATTR_PAGE, page);
        model.addAttribute(MODEL_ATTR_ENTITY, searchModel);
        return viewForm;
    }

    /**
     * 查询已回复列表(已回复,待客户/跟单确认)
     */
    @RequiresPermissions(value ="sd:reminder:view")
    @RequestMapping(value = "/list/haveConfirmedList")
    public String haveConfirmedList(ReminderPageSearchModel searchModel, HttpServletRequest request, HttpServletResponse response, Model model) {
        String viewForm = "modules/sd/reminder/list/confirmed";
        User user = UserUtils.getUser();
        searchModel.setStatus(ReminderStatus.Replied.getCode());
        Page page = new Page(request, response);
        long areaId = searchModel.getAreaId();
        searchModel = setSearchModel(model, user,searchModel,3,365);
        if(!searchModel.getIsValid()){
            model.addAttribute(MODEL_ATTR_PAGE, page);
            model.addAttribute(MODEL_ATTR_ENTITY, searchModel);
            return viewForm;
        }
        try {
            page = reminderService.haveConfirmedList(page, searchModel);
        } catch (Exception e) {
            addMessage(model, "查询错误：" + Exceptions.getRootCauseMessage(e));
        }finally {
            searchModel.setAreaId(areaId);//还原
        }
        model.addAttribute(MODEL_ATTR_PAGE, page);
        model.addAttribute(MODEL_ATTR_ENTITY, searchModel);
        return viewForm;
    }

    /**
	 * 已完成列表
	 */
    @RequiresPermissions(value ="sd:reminder:view")
	@RequestMapping(value = "/list/complete")
	public String completeList(ReminderPageSearchModel searchModel, HttpServletRequest request, HttpServletResponse response, Model model) {
        String viewForm = "modules/sd/reminder/list/complete";
        User user = UserUtils.getUser();
        searchModel.setStatus(ReminderStatus.Completed.getCode());
        Page page = new Page(request, response);
        long areaId = searchModel.getAreaId();
        searchModel = setSearchModel(model, user,searchModel,3,365);
        if(!searchModel.getIsValid()){
            model.addAttribute(MODEL_ATTR_PAGE, page);
            model.addAttribute(MODEL_ATTR_ENTITY, searchModel);
            return viewForm;
        }
        try {
            page = reminderService.finishList(page, searchModel);
        } catch (Exception e) {
            addMessage(model, "查询错误：" + Exceptions.getRootCauseMessage(e));
        }finally {
            searchModel.setAreaId(areaId);//还原
        }
        model.addAttribute(MODEL_ATTR_PAGE, page);
        model.addAttribute(MODEL_ATTR_ENTITY, searchModel);
        return viewForm;
	}

	/**
	 * 所有列表
	 */
    @RequiresPermissions(value ="sd:reminder:view")
	@RequestMapping(value = "/list/all")
	public String allList(ReminderPageSearchModel searchModel, HttpServletRequest request, HttpServletResponse response, Model model) {
        String viewForm = "modules/sd/reminder/list/all";
        User user = UserUtils.getUser();
        Page page = new Page(request, response);
        long areaId = searchModel.getAreaId();
        searchModel = setSearchModel(model, user,searchModel,3,365);
        if(!searchModel.getIsValid()){
            model.addAttribute(MODEL_ATTR_PAGE, page);
            model.addAttribute(MODEL_ATTR_ENTITY, searchModel);
            return viewForm;
        }
        try {
            page = reminderService.getPage(page, searchModel);
        } catch (Exception e) {
            addMessage(model, "查询错误：" + Exceptions.getRootCauseMessage(e));
        }finally {
            searchModel.setAreaId(areaId);//还原
        }
        model.addAttribute(MODEL_ATTR_PAGE, page);
        model.addAttribute(MODEL_ATTR_ENTITY, searchModel);
        return viewForm;
	}


    /**
     * 工单的所有催单列表

    @RequestMapping(value = "/orderlist")
    public String orderList(Long orderId,String orderNo,String quarter, HttpServletRequest request, HttpServletResponse response, Model model)
    {
        Page<OrderComplain> page = new Page<OrderComplain>();
        if(orderId==null || orderId<0) {
            addMessage(model, "处理失败：订单参数为空。");
        }else {
            try {
                List<OrderComplain> list = orderComplainService.getComplainListByOrder(orderId,null,quarter,true);
                page.setList(list);
            } catch (Exception e) {
                addMessage(model, "错误：" + Exceptions.getRootCauseMessage(e));
            }
        }
        model.addAttribute("page",page);
        model.addAttribute("orderNo",StringUtils.isNotBlank(orderNo)?orderNo:"");
        model.addAttribute("orderId",orderId.toString());
        return "modules/sd/reminder/orderList";
    }*/

    /**
     * [Ajax]订单详情页-催单列表(tab)
     * @param orderId	订单id
     * @param quarter 	分片
     * @param detailType 详情页类型 1:客服 如有待回复催单，显示催单回复界面 2:客户，只显示催单列表
     * @return Map<String,Object>
     *     isWaitReply:1 待回复 ，显示待回复催单内容；0：已回复，显示催单内容，不可回复
     *     value: isWait=1，待回复/待确认的催单实例；0:最新的催单实例
     */
    @ResponseBody
    @RequestMapping(value = "/ajax/list")
    public AjaxJsonEntity ajaxOrderReminderList(@RequestParam String orderId, @RequestParam String quarter, @RequestParam Integer detailType,HttpServletResponse response)
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
            Map<String,Object> data = Maps.newHashMapWithExpectedSize(5);
            data.put("reminderCheckFlag",1);
            data.put("reminderCheckMsg","");
            data.put("needConfirm",1);
            Reminder reminder = reminderService.getLastReminderByOrderId(lorderId,quarter);
            if(reminder == null){
                jsonEntity.setSuccess(false);
                jsonEntity.setMessage("读取催单信息失败，请重试");
                return jsonEntity;
            }
            if(reminder.getItemId()>0 && !CollectionUtils.isEmpty(reminder.getItems())){
                ReminderItem item = reminder.getItems().stream().filter(t->t.getId() == reminder.getItemId()).findFirst().orElse(null);
                if(item != null){
                    reminder.setReminderRemark(item.getCreateRemark());
                    reminder.setReminderReason(item.getReminderReason());
                }
            }
            /*if(reminder.getStatus() == ReminderStatus.Replied.getCode()){*/
                //检查是否可以再次催单
                RereminderCheckRespone checkRespone = reminderService.reReminderCheck(reminder.getOrderId(),reminder.getQuarter(),System.currentTimeMillis());
                if(checkRespone.getCode() != 1){
                    data.put("reminderCheckMsg",checkRespone.getMsg());
                    data.put("reminderCheckFlag",0);
                }
            //}
            ReminderModel model = Mappers.getMapper(ReminderViewModelMapper.class).toViewModel(reminder);
            if((user.isAdmin() || user.isCustomer() || user.isSaleman() || user.isMerchandiser()) && detailType == 2){
             /*   data.put("isWait",reminder.getStatus() == 2?1:0); //待确认
                if(reminder.getStatus() == 2 && (user.isMerchandiser() || user.isSaleman())){
                    data.put("needConfirm",1); //确认按钮权限
                }
                if(reminder.getStatus()==2){
                    ReminderItem item = reminder.getItems().stream().filter(t->t.getStatus()==2).findFirst().orElse(null);
                    if(item != null){
                        //reminder.setReminderRemark(item.getCreateRemark());
                        model.setProcessRemark(item.getProcessRemark());
                        model.setReminderReason(item.getReminderReason());
                        model.setItemId(item.getId());
                    }
                }*/
                ReminderItem item = reminder.getItems().stream().filter(t->t.getStatus()==2).findFirst().orElse(null);
                if(item!=null){
                    model.setProcessRemark(item.getProcessRemark());
                    model.setReminderReason(item.getReminderReason());
                    model.setItemId(item.getId());
                    data.put("isWait",1);
                    if(item.getStatus() == 2 && (user.isMerchandiser() || user.isSaleman())){
                        data.put("needConfirm",1); //确认按钮权限
                    }
                }else{
                    data.put("isWait",0);
                }

            }else if((user.isKefu() || user.isInnerAccount() || user.isAdmin() || user.isSystemUser()) && detailType == 1){
            /*    if(reminder.getStatus() == 1){
                    if(reminder.getItemId()>0 && !CollectionUtils.isEmpty(reminder.getItems())){
                        ReminderItem item = reminder.getItems().stream().filter(t->t.getStatus()==1).findFirst().orElse(null);
                        if(item != null){
                            //reminder.setReminderRemark(item.getCreateRemark());
                            model.setCreateRemark(item.getCreateRemark());
                            model.setReminderReason(item.getReminderReason());
                            model.setItemId(item.getId());
                        }
                    }
                    data.put("isWait",1); //待回复
                }else{
                    data.put("isWait",0); //待回复
                }*/
                ReminderItem item = reminder.getItems().stream().filter(t->t.getStatus()==1).findFirst().orElse(null);
                if(item != null){
                    model.setCreateRemark(item.getCreateRemark());
                    model.setReminderReason(item.getReminderReason());
                    model.setItemId(item.getId());
                    data.put("isWait",1);
                }else{
                    data.put("isWait",0);
                }
                //data.put("isWait",reminder.getStatus() == 1?1:0); //待回复
                //model.setProcessRemark("您好，已回电客户【】，针对客户问题已与通方案为【】，用户认可，此问题预计【】处理完成，谢谢");
                //已与【联系电话】联系，沟通方案为【沟通方案】，预计完成时间【 完成时间】
                model.setProcessRemark("已与【" + reminder.getUserPhone() + "】联系，沟通方案为【】，预计完成时间【】");
            }else{
                data.put("isWait",0);
            }
            model.setProcessRemark(StringUtils.toString(model.getProcessRemark()));
            //获取催单类型
            List<Dict> dicts = MSDictUtils.getDictList("reminder_reason");
            data.put("value",model);
            data.put("reminderReasons",dicts);
            jsonEntity.setData(data);
            return jsonEntity;


        } catch (Exception e)
        {
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage(Exceptions.getRootCauseMessage(e));
        }
        return jsonEntity;
    }

	//endregion 列表

	//region 处理

	/**
	 * 新建催单(窗口)
	 * @param orderId	订单id
	 * @param orderNo	订单号
	 * @param quarter 	分片
	 */
	@RequiresPermissions(value="sd:reminder:create")
	@RequestMapping(value = "/form", method = RequestMethod.GET)
	public String newForm(@RequestParam String orderId, @RequestParam(required = false) String quarter,@RequestParam(required = false) String orderNo, Model model) {
        String formView = "modules/sd/reminder/newForm";
        User user = UserUtils.getUser();

        Long lid = null;
        ReminderModel reminder = new ReminderModel();
        if (StringUtils.isBlank(orderId)) {
            return reminderCheckFail(model, formView, reminder, "参数为空。");
        }

        Long lorderId = Long.valueOf(orderId);
        //判断订单
        Order order = orderService.getOrderById(lorderId,quarter, OrderUtils.OrderDataLevel.STATUS,true);
        if(order == null || order.getOrderCondition() == null){
            return reminderCheckFail(model, formView, reminder, "订单读取失败，或订单不存在。请重试");
        }
        //判断状态
        OrderCondition orderCondition = order.getOrderCondition();
        int status = orderCondition.getStatusValue();
        //APP完工[55]
        if(status == 0 || status > 55 ){
            addMessage(model, "当前订单状态:" + orderCondition.getStatus().getLabel() + ",不能催单");
            model.addAttribute("canAction", false);
            model.addAttribute("reminder", reminder);
            return formView;
        }
        //是否已经有催单
        Integer reminderFlag = order.getOrderStatus().getReminderStatus();
        if(reminderFlag == null){
            reminderFlag = 0;
        }
        if(reminderFlag > ReminderStatus.All.getCode()  && reminderFlag < ReminderStatus.Completed.getCode()){
            ReminderStatus reminderStatus = ReminderStatus.fromCode(reminderFlag);
            addMessage(model, "当前订单已催单，状态为:" + reminderStatus !=null?reminderStatus.getMsg():"" + ",不能催单");
            model.addAttribute("canAction", false);
            model.addAttribute("reminder", reminder);
            return formView;
        }
        reminder.setAction(0);
        reminder.setOrderId(order.getId());
        reminder.setQuarter(order.getQuarter());
        reminder.setOrderNo(order.getOrderNo());
        reminder.setDataSourceName(order.getDataSource().getLabel());
        reminder.setUserName(orderCondition.getUserName());
        reminder.setUserPhone(orderCondition.getServicePhone());
        reminder.setUserAddress(orderCondition.getArea().getName() + order.getOrderCondition().getServiceAddress());
        reminder.setCustomer(orderCondition.getCustomer());
        reminder.setServicePoint(orderCondition.getServicePoint());
        reminder.setCreateName(user.getName());
        //reminder.setCreateAt(System.currentTimeMillis());
        reminder.setCreateDate(DateUtils.getDate("yyyy-MM-dd HH:mm:ss"));
        model.addAttribute("canAction", true);
        model.addAttribute("reminder", reminder);
        model.addAttribute("hasOpenForm", false);
        return formView;
    }

	/**
	 * 提交新建催单
	 */
	@RequiresPermissions("sd:reminder:create")
	@ResponseBody
	@RequestMapping(value = "/save")
	public AjaxJsonEntity save(ReminderModel reminder, HttpServletRequest request, HttpServletResponse response, Model model) {
		response.setContentType("application/json; charset=UTF-8");
		if(reminder == null){
			return AjaxJsonEntity.fail("提交内容为空，请确认",null);
		}
		User user = UserUtils.getUser();
		if(user==null || user.getId()==null){
			return AjaxJsonEntity.fail("登录超时，请重新登录。",null);
		}
		MSErrorCode msErrorCode = checkReminderForm(reminder);
		if(msErrorCode.getCode() != MSErrorCode.SUCCESS.getCode()){
			return AjaxJsonEntity.fail(msErrorCode.getMsg(),null);
		}

		try{
			Order order = orderService.getOrderById(reminder.getOrderId(),reminder.getQuarter(), OrderUtils.OrderDataLevel.STATUS,true);
			if(order == null || order.getOrderCondition() == null){
				return AjaxJsonEntity.fail("确认订单信息失败",null);
			}
			OrderCondition orderCondition = order.getOrderCondition();
            String reminderNo = SeqUtils.NextSequenceNo("ReminderNo",0,3);
            if(StringUtils.isBlank(reminderNo)){
                return AjaxJsonEntity.fail("生成单据编号失败，请重新提交",null);
            }
            //long servicePointId = orderCondition.getServicePoint() == null || orderCondition.getServicePoint().getId()==null || orderCondition.getServicePoint().getId() <=0?0:orderCondition.getServicePoint().getId();
            long servicePointId = Optional.ofNullable(orderCondition.getServicePoint()).map(t->t.getId()).orElse(0L);
            long kefuId = orderCondition.getKefu() == null || orderCondition.getKefu().getId() == null || orderCondition.getKefu().getId() <= 0 ? 0 : orderCondition.getKefu().getId();
            long engineerId = orderCondition.getEngineer() == null || orderCondition.getEngineer().getId() == null || orderCondition.getEngineer().getId() <= 0 ? 0 : orderCondition.getEngineer().getId();
            String shopId = Optional.ofNullable(order.getB2bShop()).map(t->t.getShopId()).orElse(StrUtil.EMPTY);
            Reminder.ReminderBuilder builder = Reminder.builder()
                    .reminderNo(reminderNo)
                    .reminderType(ReminderType.Manual.getCode())
                    .dataSource(order.getDataSourceId())
                    .orderId(reminder.getOrderId())
                    .quarter(reminder.getQuarter())
                    .orderCreateAt(orderCondition.getCreateDate().getTime())
                    .customerId(orderCondition.getCustomer().getId())
                    .shopId(shopId==null?StrUtil.EMPTY:shopId)
                    .servicepointId(servicePointId)
                    .productCategoryId(orderCondition.getProductCategoryId()==null?0L:orderCondition.getProductCategoryId()) //2019-10-08
                    .userName(orderCondition.getUserName())
                    .userPhone(orderCondition.getServicePhone())
                    .userAddress(orderCondition.getArea().getName() + order.getOrderCondition().getServiceAddress())
                    .orderNo(order.getOrderNo())
                    .cityId(0l)
                    .provinceId(0l)
                    .areaId(orderCondition.getArea().getId())
                    .subAreaId(orderCondition.getSubArea().getId())
                    .kefuId(kefuId)
                    .kefuType(orderCondition.getKefuType())
                    .engineerId(engineerId)
					.status(ReminderStatus.WaitReply.getCode())
					.reminderRemark(reminder.getCreateRemark())
                    .canRush(orderCondition.getCanRush())
                    .reminderReason(reminder.getReminderReason());
			Area area = areaService.getFromCache(orderCondition.getArea().getId());
			if(area != null) {
                List<String> ids = Splitter.onPattern(",")
                        .omitEmptyStrings()
                        .trimResults()
                        .splitToList(area.getParentIds());
                if (ids.size() >= 2) {
                    builder.cityId(Long.valueOf(ids.get(ids.size() - 1)))
                            .provinceId(Long.valueOf(ids.get(ids.size() - 2)));
                }
            }
			Reminder reminderForm = builder.build();
            reminderForm.setCreateById(user.getId());
            reminderForm.setOperatorType(reminderService.getReminderCreatorType(user).getCode());
            reminderForm.setCreateBy(user.getName());//user name
            reminder.setReminderType(ReminderType.Manual.getCode());
            reminderForm.setCreateDt(System.currentTimeMillis());
			reminderService.newReminder(reminderForm);
			OrderStatus orderStatus = order.getOrderStatus();
			if(orderStatus == null){
                //淘汰订单orderStatus缓存
                OrderCacheParam.Builder cacheBuilder = new OrderCacheParam.Builder();
                cacheBuilder.setOpType(OrderCacheOpType.UPDATE)
                        .setOrderId(reminder.getOrderId())
                        .setDeleteField(OrderCacheField.ORDER_STATUS);
                OrderCacheUtils.update(cacheBuilder.build());
            }else {
			    orderStatus.setReminderStatus(ReminderStatus.WaitReply.getCode());
                //更新orderStatus缓存
                OrderCacheParam.Builder cacheBuilder = new OrderCacheParam.Builder();
                cacheBuilder.setOpType(OrderCacheOpType.UPDATE)
                        .setOrderId(reminder.getOrderId())
                        .setOrderStatus(orderStatus);
                OrderCacheUtils.update(cacheBuilder.build());
            }
            PushMessageUtils.pushReminderMessage(orderCondition.getServicePoint(), orderCondition.getEngineer(), order.getOrderNo());
			return AjaxJsonEntity.success("保存成功",null);
		}catch (Exception e){
			log.error("[ReminderController.save] orderNo:{} ,userId:{}",reminder.getOrderNo(),user.getId(),e);
			LogUtils.saveLog("创建催单","ReminderController.save",reminder.getOrderNo(),e,user);
			return AjaxJsonEntity.fail("保存失败:" + ExceptionUtils.getRootCauseMessage(e),null);
		}
	}

    /**
     * 处理窗口
     * @param id	催单id
     * @param quarter 	分片
     */
    @RequiresPermissions(value="sd:reminder:process")
    @RequestMapping(value = "/processForm", method = RequestMethod.GET)
    public String processForm(@RequestParam String id, @RequestParam(required = false) String quarter,Model model,Long itemId) {
        String formView = "modules/sd/reminder/processForm";
        User user = UserUtils.getUser();
        Long lid = null;
        ReminderModel reminder = new ReminderModel();
        try{
            lid = Long.parseLong(id);
        }catch (Exception e){
            return reminderCheckFail(model, formView, reminder, "传入催单id类型不符");
        }
        if(StringUtils.isBlank(quarter)){
            return reminderCheckFail(model, formView, reminder, "传入参数：数据分片 无内容");
        }
        if(itemId==null || itemId<=0){
            return reminderCheckFail(model, formView, reminder, "传入参数:缺少催单项次Id,请检查");
        }
        Reminder reminderModel = reminderService.getReminderById(lid,quarter,1);
        if(reminderModel == null){
            return reminderCheckFail(model, formView, reminder, "读取催单失败，或催单单据不存在。");
        }
        ReminderItem currentItem =null;
        List<ReminderItem> itemList = reminderModel.getItems();
        if(itemList!=null && itemList.size()>0){
            currentItem = itemList.stream().filter(t->t.getId()==itemId).findFirst().orElse(null);
        }
        if(currentItem==null){
            return reminderCheckFail(model, formView, reminder, "读取当前催单项失败,或催单项不存在");
        }
        //判断状态
        /*if(reminderModel.getStatus() != ReminderStatus.WaitReply.getCode()){
            ReminderStatus reminderStatus = ReminderStatus.fromCode(reminderModel.getStatus());
            return reminderCheckFail(model, formView, reminder, "当前催单状态:" + (reminderStatus==null?"":reminderStatus.getMsg()) + ",不能进行处理操作。");
        }*/
        if(currentItem.getStatus() != ReminderStatus.WaitReply.getCode()){
            ReminderStatus reminderStatus = ReminderStatus.fromCode(reminderModel.getStatus());
            return reminderCheckFail(model, formView, reminder, "当前催单状态:" + (reminderStatus==null?"":reminderStatus.getMsg()) + ",不能进行处理操作。");
        }
        reminderModel.setItemId(currentItem.getId());
        reminder = Mappers.getMapper(ReminderViewModelMapper.class).toViewModel(reminderModel);
        if(reminder == null){
            return reminderCheckFail(model, formView, reminder, "单据转换失败，请重试");
        }
        if(reminderModel.getItemId()>0 && !CollectionUtils.isEmpty(reminderModel.getItems())){
            ReminderItem item = reminderModel.getItems().stream().filter(t->t.getId() == reminderModel.getItemId()).findFirst().orElse(null);
            if(item != null){
                reminder.setCreateRemark(item.getCreateRemark());
                reminder.setCreateName(item.getCreateName());
                reminder.setCreateDate(DateFormatUtils.format(item.getCreateAt(), "yyyy-MM-dd HH:mm:ss"));
                reminder.setReminderReason(item.getReminderReason());
            }
        }
        //reminder.setProcessRemark("您好，已回电客户【】，针对客户问题已与通方案为【】，用户认可，此问题预计【】处理完成，谢谢");
        //已与【联系电话】联系，沟通方案为【沟通方案】，预计完成时间【 完成时间】
        reminder.setProcessRemark("已与【" + reminder.getUserPhone() + "】联系，沟通方案为【】，预计完成时间【】");
        model.addAttribute("canAction", true);
        model.addAttribute("reminder", reminder);
        return formView;
    }

    /**
     * 保存回复内容
     */
    //@RequiresPermissions("sd:reminder:process")
    @ResponseBody
    @RequestMapping(value = "/saveReply")
    public AjaxJsonEntity saveReply(ReminderModel reminder, HttpServletRequest request, HttpServletResponse response, Model model) {
        response.setContentType("application/json; charset=UTF-8");
        if (!SecurityUtils.getSubject().isPermitted("sd:reminder:process")) {
            return AjaxJsonEntity.fail("无[回复]权限，请联系管理员",null);
        }
        if(reminder == null){
            return AjaxJsonEntity.fail("提交内容为空，请确认",null);
        }
        User user = UserUtils.getUser();
        if(user==null || user.getId()==null){
            return AjaxJsonEntity.fail("登录超时，请重新登录。",null);
        }
        if(reminder.getId() <=0){
            return AjaxJsonEntity.fail("提交内容无催单id",null);
        }
        if(StringUtils.isBlank(reminder.getQuarter())){
            return AjaxJsonEntity.fail("提交内容无分片数据",null);
        }
        if(StringUtils.isBlank(reminder.getProcessRemark())){
            return AjaxJsonEntity.fail("请输入处理意见",null);
        }
        user.setName(StringUtils.left(user.getName(),30));
        try{
            Long servicePointId = null;
            Order order = orderService.getOrderById(reminder.getOrderId(),reminder.getQuarter(), OrderUtils.OrderDataLevel.CONDITION,true);
            servicePointId = Optional.ofNullable(order).map(t->t.getOrderCondition()).map(t->t.getServicePoint()).map(t->t.getId()).orElse(0L);
            reminder.setProcessRemark(StringUtils.left(reminder.getProcessRemark(),250));
            reminderService.replyReminder(reminder.getId(),reminder.getOrderId(),reminder.getQuarter(),reminder.getProcessRemark(),user,servicePointId,reminder.getItemId());
            //淘汰订单orderStatus缓存
            OrderCacheParam.Builder cacheBuilder = new OrderCacheParam.Builder();
            cacheBuilder.setOpType(OrderCacheOpType.UPDATE)
                    .setOrderId(reminder.getOrderId())
                    .setDeleteField(OrderCacheField.ORDER_STATUS);
            OrderCacheUtils.update(cacheBuilder.build());
            return AjaxJsonEntity.success("保存成功",null);
        }catch (Exception e){
            log.error("[ReminderController.saveProcess] reminderNo:{} ,userId:{}",reminder.getReminderNo(),user.getId(),e);
            LogUtils.saveLog("回复催单","ReminderController.saveProcess",StringUtils.trimToEmpty(reminder.getReminderNo()),e,user);
            return AjaxJsonEntity.fail("保存失败:" + ExceptionUtils.getRootCauseMessage(e),null);
        }
    }

    /**
     * 确认窗口 (跟单/客户)
     * 可再次催单或确认
     * @param id	催单id
     * @param quarter 	分片
     */
    @RequiresPermissions(value="sd:reminder:complete")
    @RequestMapping(value = "/confirmForm", method = RequestMethod.GET)
    public String confirmForm(@RequestParam String id, @RequestParam(required = false) String orderId, @RequestParam(required = false) String quarter,Long itemId,Model model) {
        String formView = "modules/sd/reminder/confirmForm";
        model.addAttribute("reminderCheckFlag", 0);
        User user = UserUtils.getUser();
        Long lid = null;
        Long lorderId = null;
        ReminderModel reminder = new ReminderModel();
        if(StringUtils.isBlank(id) && StringUtils.isBlank(orderId)){
            return reminderCheckFail(model, formView, reminder, "传入催单id无内容");
        }
        if(StringUtils.isBlank(quarter)){
            return reminderCheckFail(model, formView, reminder, "传入参数：数据分片 无内容");
        }
        if(StringUtils.isNotBlank(id)) {
            try {
                lid = Long.parseLong(id);
            } catch (Exception e) {
                return reminderCheckFail(model, formView, reminder, "传入催单id类型不符");
            }
        }else{
            try {
                lorderId = Long.parseLong(orderId);
            } catch (Exception e) {
                return reminderCheckFail(model, formView, reminder, "传入订单id类型不符");
            }
        }
        if(itemId==null || itemId<=0){
            return reminderCheckFail(model, formView, reminder, "参数错误：无催单项ID");
        }
        Reminder reminderModel = null;
        if(lid != null) {
            reminderModel = reminderService.getReminderById(lid, quarter,1);
        }else{
            //by order
            /*
            List<Reminder>  reminders = reminderService.getListByOrderId(lorderId,quarter,1,"createDt desc");
            if(ObjectUtils.isEmpty(reminders)){
                return reminderCheckFail(model, formView, reminder, "读取催单单据失败");
            }
            reminderModel = reminders.get(0);
            */
            reminderModel = reminderService.getLastReminderByOrderId(lorderId,quarter);
        }
        if(reminderModel == null){
            return reminderCheckFail(model, formView, reminder, "读取催单失败，或催单单据不存在。");
        }
        ReminderItem currentItem =null;
        List<ReminderItem> itemList = reminderModel.getItems();
        if(itemList!=null && itemList.size()>0){
            currentItem = itemList.stream().filter(t->t.getId()==itemId).findFirst().orElse(null);
        }
        if(currentItem==null){
            return reminderCheckFail(model, formView, reminder, "读取当前催单项失败,或催单项不存在");
        }
        //判断状态
       /* if(reminderModel.getStatus() != ReminderStatus.Replied.getCode()){
            ReminderStatus reminderStatus = ReminderStatus.fromCode(reminderModel.getStatus());
            return reminderCheckFail(model, formView, reminder, "当前催单状态:" + (reminderStatus==null?"":reminderStatus.getMsg()) + ",不能进行完成操作。");
        }*/
        if(currentItem.getStatus() != ReminderStatus.Replied.getCode()){
            ReminderStatus reminderStatus = ReminderStatus.fromCode(reminderModel.getStatus());
            return reminderCheckFail(model, formView, reminder, "当前催单状态:" + (reminderStatus==null?"":reminderStatus.getMsg()) + ",不能进行完成操作。");
        }
        //检查是否可以再次催单
        RereminderCheckRespone checkRespone = reminderService.reReminderCheck(reminderModel.getOrderId(),reminderModel.getQuarter(),System.currentTimeMillis());
        if(checkRespone.getCode() != 1){
            model.addAttribute("reminderCheckMsg", checkRespone.getMsg());
            model.addAttribute("reminderCheckFlag", 1);
        }
        reminder = Mappers.getMapper(ReminderViewModelMapper.class).toViewModel(reminderModel);
        if(reminder == null){
            return reminderCheckFail(model, formView, reminder, "单据转换失败，请重试");
        }
        reminder.setItemId(currentItem.getId());
        reminder.setProcessRemark("");
        model.addAttribute("canAction", true);
        model.addAttribute("reminder", reminder);
        return formView;
    }

    /**
     * 保存确认意见
     */
    @RequiresPermissions("sd:reminder:complete")
    @ResponseBody
    @RequestMapping(value = "/saveConfirm")
    public AjaxJsonEntity saveConfirm(ReminderModel reminder, HttpServletRequest request, HttpServletResponse response, Model model) {
        response.setContentType("application/json; charset=UTF-8");
        if(reminder == null){
            return AjaxJsonEntity.fail("提交内容为空，请确认",null);
        }
        Integer actionCode = reminder.getAction();
        if(actionCode == null || actionCode <=0 || actionCode >2){
            return AjaxJsonEntity.fail("提交操作错误，请确认",null);
        }
        User user = UserUtils.getUser();
        if(user==null || user.getId()==null){
            return AjaxJsonEntity.fail("登录超时，请重新登录。",null);
        }
        if(reminder.getId() <=0){
            return AjaxJsonEntity.fail("提交内容无催单id",null);
        }
        if(StringUtils.isBlank(reminder.getQuarter())){
            return AjaxJsonEntity.fail("提交内容无分片数据",null);
        }
        if(StringUtils.isBlank(reminder.getProcessRemark())){
            return AjaxJsonEntity.fail("请输入催单意见",null);
        }
        user.setName(StringUtils.left(user.getName(),30));
        Order order = null;
        order = orderService.getOrderById(reminder.getOrderId(),reminder.getQuarter(), OrderUtils.OrderDataLevel.CONDITION,true);
        if(order == null || order.getOrderCondition() == null){
            return AjaxJsonEntity.fail("确认订单信息失败",null);
        }
        try{
            reminder.setProcessRemark(StringUtils.left(reminder.getProcessRemark(),250));
            OrderCondition condition = order.getOrderCondition();
            Long servicePointId = Optional.ofNullable(condition.getServicePoint()).map(t->t.getId()).orElse(0L);
            if(actionCode == 1) {//确认
                reminderService.confirmReminder(reminder.getId(), reminder.getOrderId(), reminder.getQuarter(), reminder.getProcessRemark(), user,servicePointId,reminder.getItemId());
            }else {//再次催单
                reminderService.rejectReminder(reminder.getId(), reminder.getOrderId(), reminder.getQuarter(), reminder.getProcessRemark(), user,reminder.getStatus(),servicePointId,reminder.getReminderReason());
                if(order != null && order.getOrderCondition() != null) {
                    OrderCondition orderCondition = order.getOrderCondition();
                    PushMessageUtils.pushReminderMessage(orderCondition.getServicePoint(), orderCondition.getEngineer(), order.getOrderNo());
                }
            }
            //淘汰订单orderStatus缓存
            OrderCacheParam.Builder cacheBuilder = new OrderCacheParam.Builder();
            cacheBuilder.setOpType(OrderCacheOpType.UPDATE)
                    .setOrderId(reminder.getOrderId())
                    .setDeleteField(OrderCacheField.ORDER_STATUS);
            OrderCacheUtils.update(cacheBuilder.build());
            return AjaxJsonEntity.success("保存成功",null);
        }catch (Exception e){
            log.error("reminderNo:{} ,userId:{}",reminder.getReminderNo(),user.getId(),e);
            LogUtils.saveLog("确认催单","ReminderController.saveConfirm",reminder.getReminderNo(),e,user);
            return AjaxJsonEntity.fail("保存失败:" + Exceptions.getRootCauseMessage(e),null);
        }
    }


    /**
     * 再次催单
     * @param id	    催单id
     * @param orderId	订单id
     * @param quarter 	分片
     */
    @RequiresPermissions(value="sd:reminder:complete")
    @RequestMapping(value = "/reReminder", method = RequestMethod.GET)
    public String reReminder(@RequestParam String id, @RequestParam(required = false) String orderId, @RequestParam(required = false) String quarter,Model model) {
        String formView = "modules/sd/reminder/reReminderForm";
        User user = UserUtils.getUser();
        Long lid = null;
        Long lorderId = null;
        ReminderModel reminder = new ReminderModel();
        if(StringUtils.isBlank(id) && StringUtils.isBlank(orderId)){
            return reminderCheckFail(model, formView, reminder, "传入催单id无内容");
        }
        if(StringUtils.isBlank(quarter)){
            return reminderCheckFail(model, formView, reminder, "传入参数：数据分片 无内容");
        }
        if(StringUtils.isNotBlank(id)) {
            try {
                lid = Long.parseLong(id);
            } catch (Exception e) {
                return reminderCheckFail(model, formView, reminder, "传入催单id类型不符");
            }
        }else{
            try {
                lorderId = Long.parseLong(orderId);
            } catch (Exception e) {
                return reminderCheckFail(model, formView, reminder, "传入订单id类型不符");
            }
        }
        Reminder reminderModel = null;
        if(lid != null) {
            reminderModel = reminderService.getReminderById(lid, quarter,1);
        }else{
            //by order
            /*
            List<Reminder>  reminders = reminderService.getListByOrderId(lorderId,quarter,1,"createDt desc");
            if(ObjectUtils.isEmpty(reminders)){
                return reminderCheckFail(model, formView, reminder, "读取催单单据失败");
            }
            reminderModel = reminders.get(0);
            */
            reminderModel = reminderService.getLastReminderByOrderId(lorderId,quarter);
        }
        if(reminderModel == null){
            return reminderCheckFail(model, formView, reminder, "读取催单失败，或催单单据不存在。");
        }
        //判断状态
        //未回复，或已完成，不能再次催单
        if(reminderModel.getStatus() < ReminderStatus.Replied.getCode() || reminderModel.getStatus() >= ReminderStatus.Completed.getCode()){
            ReminderStatus reminderStatus = ReminderStatus.fromCode(reminderModel.getStatus());
            return reminderCheckFail(model, formView, reminder, "当前催单状态:" + (reminderStatus==null?"":reminderStatus.getMsg()) + ",不能再次催单。");
        }
        //检查是否可以再次催单
        RereminderCheckRespone checkRespone = reminderService.reReminderCheck(reminderModel.getOrderId(),reminderModel.getQuarter(),System.currentTimeMillis());
        if(checkRespone.getCode() != 1){
            model.addAttribute("reminderCheckMsg", checkRespone.getMsg());
            model.addAttribute("reminderCheckFlag", 1);
        }
        reminder = Mappers.getMapper(ReminderViewModelMapper.class).toViewModel(reminderModel);
        if(reminder == null){
            return reminderCheckFail(model, formView, reminder, "单据转换失败，请重试");
        }
        if(StringUtils.isBlank(reminder.getCompleteDate())){
            reminder.setProcessRemark("");
            reminder.setProcessDate(DateUtils.getDate("yyyy-MM-dd HH:mm:ss"));
            reminder.setProcessName(user.getName());
        }
        model.addAttribute("canAction", true);
        model.addAttribute("reminder", reminder);
        return formView;
    }

    //endregion 处理

    //region 日志

    /**
     * [Ajax]日志列表
     * @param reminderId    催单id
     * @quarter 分片
     */
    @ResponseBody
    @RequestMapping(value = "/logs/list")
    public AjaxJsonEntity getLogList(@RequestParam String reminderId, @RequestParam String quarter, HttpServletResponse response)
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
            Long lid = Long.valueOf(reminderId);
            if(lid == null || lid <=0){
                jsonEntity.setSuccess(false);
                jsonEntity.setMessage("催单参数错误");
                return jsonEntity;
            }

            List<ReminderLog> list = reminderService.getLogs(quarter,lid);
            if(list ==null){
                jsonEntity.setData(Lists.newArrayList());
            }else {
                //客户可见标志处理
                list.stream()
                        .filter(i-> VisibilityFlagEnum.has(i.getVisibilityFlag(), VisibilityFlagEnum.CUSTOMER))
                        .forEach(t->t.setVisibilityFlag(0));
                //若当前用户是客户，则只显示客户可见的投诉单日志
                //if (user.isCustomer()) {
                //    list = list.stream().filter(i-> i.getVisibilityFlag() == 0 || VisibilityFlagEnum.has(i.getVisibilityFlag(), VisibilityFlagEnum.CUSTOMER)).collect(Collectors.toList());
                //}
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
     * 保存日志
     */
    @RequiresPermissions(value={"sd:reminder:process","sd:reminder:complete"},logical = Logical.OR)
    @ResponseBody
    @RequestMapping(value = "/log/saveLog")
    public AjaxJsonEntity savelog(HttpServletRequest request, HttpServletResponse response, Model model) {
        response.setContentType("application/json; charset=UTF-8");
        User user = UserUtils.getUser();
        AjaxJsonEntity jsonEntity = new AjaxJsonEntity(true);
        if(user == null || user.getId()==null){
            return AjaxJsonEntity.fail("登录已超时,请刷新页面并重新登录后继续处理！",null);
        }
        String content = request.getParameter("logContent").toString();
        if(StringUtils.isBlank(content)){
            return AjaxJsonEntity.fail("无日志内容",null);
        }
        Long id = Long.valueOf(request.getParameter("reminderId"));
        if(id == null || id<=0){
            return AjaxJsonEntity.fail("催单id类型不符",null);
        }
        String quarter = request.getParameter("quarter");
        if(StringUtils.isBlank(quarter)){
            return AjaxJsonEntity.fail("数据分片无内容",null);
        }
        try
        {
            int status = Integer.parseInt(request.getParameter("status"));
            int visibilityFlag = StringUtils.toInteger(request.getParameter("visibilityFlag"));
            int visibilityValue = (visibilityFlag == 0 ? VisibilityFlagEnum.subtract(ReminderModel.VISIBILITY_FLAG_ALL, VisibilityFlagEnum.CUSTOMER) : ReminderModel.VISIBILITY_FLAG_ALL);
            ReminderLog log = ReminderLog.builder()
                    .reminderId(id)
                    .content(StringUtils.left(content,250))
                    .createName(StringUtils.left(user.getName(),30))
                    .visibilityFlag(visibilityValue)
                    .quarter(quarter)
                    .status(status)
                    .build();
            log.setCreateDt(System.currentTimeMillis());
            reminderService.newLog(log);
        } catch (Exception e)
        {
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage(e.getLocalizedMessage());
        }
        return jsonEntity;
    }

    //endregion 日志

	//region 公共

    /**
     * 设置必须的查询条件
     * @param user  当前帐号
     * @param searchModel   查询条件
     * @param initMonths    初始最小查询时间段(月)
     * @param maxDays   最大查询范围(天)
     *
     */
    private ReminderPageSearchModel setSearchModel(Model model, User user, ReminderPageSearchModel searchModel, int initMonths, int maxDays) {
        if (searchModel == null) {
            searchModel = new ReminderPageSearchModel();
        }
        //boolean isServiceSupervisor = user.getRoleEnNames().contains("Customer service supervisor");//客服主管
        //投诉日期
        Date date;
        if (StringUtils.isBlank(searchModel.getBeginDate())) {
            date = DateUtils.getDateEnd(new Date());
            searchModel.setEndDate(DateUtils.formatDate(date,"yyyy-MM-dd"));
            searchModel.setEndDt(date.getTime());
            date = DateUtils.getStartDayOfMonth(DateUtils.addMonth(new Date(), 0 - initMonths));
            searchModel.setBeginDate(DateUtils.formatDate(date,"yyyy-MM-dd"));
            searchModel.setBeginDt(date.getTime());
        } else {
            date = DateUtils.parseDate(searchModel.getBeginDate());
            searchModel.setBeginDt(date.getTime());
            date = DateUtils.parseDate(searchModel.getEndDate());
            date = DateUtils.getDateEnd(date);
            searchModel.setEndDt(date.getTime());
        }
        //检查最大时间范围
        if(maxDays > 0){
            date = DateUtils.parseDate(searchModel.getBeginDate());
            Date maxDate = DateUtils.addDays(date,maxDays-1);
            maxDate = DateUtils.getDateEnd(maxDate);
            date = DateUtils.parseDate(searchModel.getEndDate());
            if(DateUtils.pastDays(maxDate,date)>0){
                searchModel.setEndDate(DateUtils.formatDate(maxDate,"yyyy-MM-dd"));
            }
        }
        String checkRegion = orderRegionService.loadAndCheckReminderUserRegions(searchModel,user);
        if(StringUtils.isNotBlank(checkRegion)){
            searchModel.setIsValid(false);
            return searchModel;
        }
        Boolean loadResult = loadProductCategories(model,searchModel,user);
        if(!loadResult){
            searchModel.setIsValid(false);
            return searchModel;
        }
        Boolean isValid = checkOrderNoAndPhone(searchModel,model);
        if(!isValid){
            searchModel.setIsValid(false);
            return searchModel;
        }
        //vip客服查询自己负责的单，by客户+区域+品类
        //1.by 客户，前端客户已按客服筛选了
        if(user.isKefu()){
            KefuTypeEnum kefuTypeEnum = KefuTypeEnum.fromCode(user.getSubFlag());
            if(kefuTypeEnum!=null){
                searchModel.setCustomerType(kefuTypeEnum.getCustomerType());
                searchModel.setKefuType(kefuTypeEnum.getKefuType());
            }else{
                addMessage(model, "错误:读取客服类型错误");
                searchModel.setIsValid(false);
                return searchModel;
            }
           /* if(user.getSubFlag() == KefuTypeEnum.VIPKefu.getCode()){
                //vip客服
                searchModel.setCustomerType(1);//指派客户，关联sys_user_customer
                searchModel.setRushType(null);//忽略突击区域
            } else if(user.getSubFlag() == KefuTypeEnum.Kefu.getCode()){
                //普通客服
                searchModel.setCustomerType(0);//不能查询vip客户订单
                searchModel.setRushType(0);//不能查看突击区域订单
            }else if (user.getSubFlag() == KefuTypeEnum.Rush.getCode()){
                //突击客服，只看自己负责的单
                searchModel.setCustomerType(null);//不能查询vip客户订单
                searchModel.setRushType(1);//查看突击区域订单
            } else if(user.getSubFlag() == KefuTypeEnum.AutomaticKefu.getCode()){
                searchModel.setCustomerType(null);//查看所有客户工单
                searchModel.setRushType(0);//查看非突击区域订单
            }else if(user.getSubFlag() == KefuTypeEnum.COMMON_KEFU.getCode()){
                searchModel.setCustomerType(null);//查看所有客户工单
                searchModel.setRushType(0);//查看非突击区域订单
            }else {//超级客服，查询所有客户订单
                //超级客服，查询所有客户订单，包含Vip客户
                searchModel.setCustomerType(null); //可查看Vip客户订单
                searchModel.setRushType(null);//可查看突击区域订单
            }*/
        } else if(user.isSaleman()){
            searchModel.setCustomerType(null);
            searchModel.setRushType(null);
            searchModel.setSalesId(user.getId());
            searchModel.setSubFlag(user.getSubFlag()==null?0:user.getSubFlag());
            List<Long> offlineCustomers = customerService.findIdListByOfflineOrderFlagFromCacheForSD();
            if(!org.springframework.util.CollectionUtils.isEmpty(offlineCustomers)){
                searchModel.setOfflineCustomerList(offlineCustomers);
            }
        } else {
            //其他类型帐号，不限制客户及突击区域订单
            searchModel.setCustomerType(null);
            searchModel.setRushType(null);
        }
        int userType = user.getUserType();
        boolean isServiceSupervisor = user.getRoleEnNames().contains("Customer service supervisor");//客服主管
        if (isServiceSupervisor) {
            searchModel.setCreateBy(user);//*
        } else if (user.isKefu()) {
            searchModel.setCreateBy(user);//*,只有客服才按帐号筛选
        } else if (user.isInnerAccount()) { //内部帐号
            searchModel.setCreateBy(user);//*
        }
        //区域
        initAreaSearchBySelect(searchModel);
        return searchModel;
    }

    // 根据选择设定查询区域id
    private void initAreaSearchBySelect(ReminderPageSearchModel searchModel){
        if(searchModel.getAreaId() == 0){
            searchModel.setAreaLevel(-1);
            return;
        }
        long areaId = searchModel.getAreaId();
        int areaLevel = searchModel.getAreaLevel();
        searchModel.setAreaId(0l);//归0
        switch (areaLevel){
            case 0:
                searchModel.setProvinceId(areaId);
                break;
            case 1:
                searchModel.setCityId(areaId);
                break;
            case 2:
                searchModel.setAreaId(areaId);
                break;
            case 3:
                searchModel.setAreaId(searchModel.getArea().getParent().getId());
                searchModel.setSubAreaId(areaId);
                break;
            default:
                break;
        }
    }

    /**
     * 检查订单号，手机号输入
     * @param searchModel
     * @param model
     * @return
     */
    private Boolean checkOrderNoAndPhone(ReminderPageSearchModel searchModel,Model model){
        if(searchModel == null){
            return true;
        }
        int noSearchType = searchModel.getReminderNoSearchType();
        if (noSearchType != 1 && StringUtils.isNotBlank(searchModel.getReminderNo())){
            addMessage(model, "错误：请输入正确的催单号码");
            return false;
        }
        //检查订单号
        if(StringUtils.isNotBlank(searchModel.getOrderNo())) {
            int orderSerchType = getOrderNoSearchType(searchModel);
            if (orderSerchType != 1) {
                addMessage(model, "错误：请输入正确的订单号");
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
            if(1 != checkIsPhone(searchModel)){
                addMessage(model, "错误：请输入正确的电话");
                return false;
            }
        }
        return true;
    }

    /**
     * 检查订单号码
     * @param searchModel
     * @return
     *  1: 正确
     *  2: 单头正确，长度不够
     *  3: 单头错误
     */
    private int getOrderNoSearchType(ReminderPageSearchModel searchModel){
        int orderNoSearchType = 0;
        if (StringUtils.isNotBlank(searchModel.getOrderNo())){
            searchModel.setOrderNo(StringUtils.trimToEmpty(searchModel.getOrderNo()));
            String orderNoPrefix = Global.getConfig("OrderPrefix");
            if (!searchModel.getOrderNo().startsWith(orderNoPrefix)){
                orderNoSearchType = 3;
            }else if (searchModel.getOrderNo().startsWith(orderNoPrefix)) {
                if (searchModel.getOrderNo().length() == 14) {
                    orderNoSearchType = 1;
                    String quarter = QuarterUtils.getOrderQuarterFromNo(searchModel.getOrderNo());
                    if (StringUtils.isNotBlank(quarter)) {
                        searchModel.setQuarter(quarter);
                    }
                } else {
                    orderNoSearchType = 2;
                }
            }
        }
        searchModel.setOrderNoSearchType(orderNoSearchType);
        return orderNoSearchType;
    }

    private int checkIsPhone(ReminderPageSearchModel searchModel){
        if (StringUtils.isNotBlank(searchModel.getUserPhone())){
            if("".equalsIgnoreCase(StringUtils.isPhoneWithRelaxed(searchModel.getUserPhone()))){
                searchModel.setIsPhone(1);
            }
        }
        return searchModel.getIsPhone();
    }

    private Boolean loadProductCategories(Model model,ReminderPageSearchModel searchModel,User user) {
        List<ProductCategory> categories = productCategoryService.findAllList();
        if (categories == null) {
            categories = Lists.newArrayList();
        } else {
            if(categories.size()>0) {
                categories = categories.stream().filter(t -> t.getDelFlag().equals(0)).collect(Collectors.toList());
            }
        }
        boolean isKefu = user.isKefu();
        if(isKefu){
            List<Long> userProductCategoryList = systemService.getAuthorizedProductCategoryIds(user.getId());
            if(ObjectUtils.isEmpty(userProductCategoryList)){
                addMessage(model, "您未开通产品类目权限，请联系管理员");
                return false;
            }
            //客服只能看到自己的品类
            if(!ObjectUtils.isEmpty(userProductCategoryList)){
                searchModel.setUserProductCategoryList(userProductCategoryList);
                if(!ObjectUtils.isEmpty(categories)){
                    categories = categories.stream().filter(t-> userProductCategoryList.contains(t.getId())).collect(Collectors.toList());
                }
            }
        }
        model.addAttribute("categories", categories);
        return true;
    }

    /**
	 * 查看催单表单
	 * @param id		催单id
	 * @param quarter 	分片
	 */
	@RequestMapping(value = "/view", method = RequestMethod.GET)
	public String viewForm(@RequestParam String id,@RequestParam String orderId, @RequestParam(required = false) String quarter, Model model) {
		User user = UserUtils.getUser();
		ReminderModel reminderModel = new ReminderModel();
		String formView = "modules/sd/reminder/viewForm";
		if(StringUtils.isBlank(id) && StringUtils.isBlank(orderId)){
            return reminderCheckFail(model, formView, reminderModel, "参数为空。");
        }

		Long lid = null;
		Long lorderId = null;
		if(StringUtils.isNotBlank(id)) {
            try {
                lid = Long.valueOf(id);
            } catch (Exception e) {
                lid = 0l;
            }
        }else{
            try {
                lorderId = Long.valueOf(orderId);
            } catch (Exception e) {
                lorderId = 0l;
            }
        }
        if ((lorderId==null || lorderId <= 0) && (lid == null || lid <= 0)) {
            return reminderCheckFail(model, formView, reminderModel, "催单参数类型错误。");
        }
        Reminder reminder = null;
        try {
            if(lid != null) {
                //按催单id读取
                reminder = reminderService.getReminderById(lid, quarter,1);
            }else{
                reminder = reminderService.getLastReminderByOrderId(lorderId,quarter);
            }
            if(reminder != null && reminder.getServicepointId() == 0){
                //get service point
                Order order = orderService.getOrderById(reminder.getOrderId(),reminder.getQuarter(), OrderUtils.OrderDataLevel.CONDITION,true);
                Long servicePointId = Optional.ofNullable(order).map(o->o.getOrderCondition()).map(o->o.getServicePoint()).map(s->s.getId()).orElse(null);
                if(servicePointId != null && servicePointId>0){
                    reminder.setServicepointId(servicePointId);
                }
            }
		}catch (Exception e){
            return reminderCheckFail(model, formView, reminderModel, "读取催单错误。");
        }
		if(reminder ==null){
            return reminderCheckFail(model, formView, reminderModel, "读取催单失败,或催单不存在，请重试。");
        }

        reminderModel = Mappers.getMapper(ReminderViewModelMapper.class).toViewModel(reminder);
		if(reminderModel == null){
            return reminderCheckFail(model, formView, new ReminderModel(), "转换催单失败,请重试。");
        }
        /*日志
        List<ReminderLog> logs = reminderService.getLogs(quarter,reminder.getId());
		if(logs == null){
		    reminderModel.setLogs(logs);
        }*/
		model.addAttribute("canAction", true);
		model.addAttribute("reminder",reminderModel);
		return formView;
	}

    private String reminderCheckFail(Model model, String formView, ReminderModel reminder, String s) {
        addMessage(model, s);
        model.addAttribute("canAction", false);
        model.addAttribute("reminder", reminder);
        return formView;
    }

    /**
     *	检查表单输入
     */
    private MSErrorCode checkReminderForm(ReminderModel model){
        if(model == null){
            return MSErrorCode.newInstance(MSErrorCode.FAILURE,"内容为空");
        }
        if(model.getOrderId()<=0){
            return MSErrorCode.newInstance(MSErrorCode.FAILURE,"无订单id");
        }
        if(StringUtils.isBlank(model.getQuarter())){
            return MSErrorCode.newInstance(MSErrorCode.FAILURE,"无数据分片");
        }
        if(StringUtils.isBlank(model.getCreateRemark())){
            return MSErrorCode.newInstance(MSErrorCode.FAILURE,"请输入催单意见");
        }
        return MSErrorCode.SUCCESS;
    }

	//endregion 公共

}
