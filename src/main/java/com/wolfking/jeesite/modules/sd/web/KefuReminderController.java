package com.wolfking.jeesite.modules.sd.web;

import com.google.common.collect.Lists;
import com.kkl.kklplus.entity.cc.*;
import com.kkl.kklplus.entity.cc.vm.ReminderPageSearchModel;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.config.Global;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.Exceptions;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.ProductCategory;
import com.wolfking.jeesite.modules.md.service.ProductCategoryService;
import com.wolfking.jeesite.modules.sd.entity.TwoTuple;
import com.wolfking.jeesite.modules.sd.service.OrderRegionService;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.*;
import com.wolfking.jeesite.modules.sys.service.SystemService;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.cc.entity.ReminderModel;
import com.wolfking.jeesite.ms.cc.entity.mapper.ReminderViewModelMapper;
import com.wolfking.jeesite.ms.cc.service.KefuReminderService;
import com.wolfking.jeesite.ms.cc.service.ReminderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 催单控制器
 *
 */
@Controller
@RequestMapping(value = "${adminPath}/sd/kefu/reminder")
@Slf4j
public class KefuReminderController extends BaseController
{
    private static final String MODEL_ATTR_PAGE = "page";
    private static final String MODEL_ATTR_ENTITY = "reminder";


	@Autowired
    private KefuReminderService kefuReminderService;


	@Autowired
	private ProductCategoryService productCategoryService;

	@Autowired
    private SystemService systemService;

    @Autowired
    private OrderRegionService orderRegionService;

    @Autowired
    private ReminderService reminderService;


	//region 列表及读取

	/**
	 * 待回复列表
	 */
    @RequiresPermissions(value ="sd:reminder:view")
    @RequestMapping(value = "list/process")
	public String waitReplyList(ReminderPageSearchModel searchModel, HttpServletRequest request, HttpServletResponse response, Model model) {
        String viewForm = "modules/sd/reminder/kefu/list/waitReplyList";
        User user = UserUtils.getUser();
        searchModel.setStatus(ReminderStatus.WaitReply.getCode());
        Page<ReminderModel> page = new Page(request, response);
        long areaId = searchModel.getAreaId();
        searchModel = setSearchModel(model, user,searchModel,3,365);
        if(!searchModel.getIsValid()){
            model.addAttribute(MODEL_ATTR_PAGE, page);
            model.addAttribute(MODEL_ATTR_ENTITY, searchModel);
            return viewForm;
        }
        try {
            page = kefuReminderService.waitReplyList(page, searchModel);
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
     * 查询已回复列表
     */
    @RequiresPermissions(value ="sd:reminder:view")
    @RequestMapping(value = "/list/haveRepliedList")
    public String haveRepliedList(ReminderPageSearchModel searchModel, HttpServletRequest request, HttpServletResponse response, Model model) {
        String viewForm = "modules/sd/reminder/kefu/list/haveReplied";
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
            page = kefuReminderService.haveRepliedList(page, searchModel);
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
        String viewForm = "modules/sd/reminder/kefu/list/all";
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
            if(searchModel.getStatus()==5){//按未完成条件查询
                Date date = new Date();
                TwoTuple<Long,Long> twoTuple = DateUtils.getTwoTupleDate(9,18);
                long dataTimeAt=0L;
                long timeoutAt = 0L;
                if(twoTuple.getAElement()<= date.getTime() && date.getTime()<= twoTuple.getBElement()){//在工作时间内
                    //searchModel.setBeginDt(DateUtils.addDays(new Date(), -3).getTime());
                    dataTimeAt = DateUtils.addDays(new Date(), -2).getTime();
                }else if(date.getTime()<twoTuple.getAElement()){ //如果当前时间小于9九点
                    //searchModel.setBeginDt(DateUtils.addDays(new Date(twoTuple.getAElement()), -3).getTime());
                    dataTimeAt = DateUtils.addDays(new Date(twoTuple.getAElement()), -2).getTime();
                }else{//如果是18点
                    //searchModel.setBeginDt(DateUtils.addDays(new Date(twoTuple.getAElement()), -2).getTime());
                    dataTimeAt = DateUtils.addDays(new Date(twoTuple.getAElement()), -1).getTime();
                }
                timeoutAt = dataTimeAt-(6*60*60*1000);
                Calendar c = Calendar.getInstance();
                c.setTime(new Date(dataTimeAt));
                c.set(Calendar.HOUR_OF_DAY, 9);
                c.set(Calendar.MINUTE, 0);
                c.set(Calendar.SECOND, 0);
                if(c.getTime().getTime()>timeoutAt){
                    timeoutAt = c.getTime().getTime()-(15*60*60*1000)-(c.getTime().getTime()-timeoutAt);
                }
                searchModel.setBeginDt(timeoutAt);
            }
            page = kefuReminderService.getPage(page, searchModel);
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
     * 超过24小时未完成(工单未完成)催单
     */
    @RequiresPermissions(value ="sd:reminder:view")
    @RequestMapping(value = "list/moreThan24HoursUnfinishedList")
    public String moreThan24HoursUnfinishedList(ReminderPageSearchModel searchModel, HttpServletRequest request, HttpServletResponse response, Model model) {
        String viewForm = "modules/sd/reminder/kefu/list/moreThan24HoursUnfinishedList";
        User user = UserUtils.getUser();
        Page<ReminderModel> page = new Page(request, response);
        long areaId = searchModel.getAreaId();
        searchModel = setSearchModel(model, user,searchModel,3,365);
        if(!searchModel.getIsValid()){
            model.addAttribute(MODEL_ATTR_PAGE, page);
            model.addAttribute(MODEL_ATTR_ENTITY, searchModel);
            return viewForm;
        }
        try {
            Date date = new Date();
            TwoTuple<Long,Long> twoTuple = DateUtils.getTwoTupleDate(9,18);
            long dataTimeAt=0L;
            long timeoutAt = 0L;
            if(twoTuple.getAElement()<= date.getTime() && date.getTime()<= twoTuple.getBElement()){//在工作时间内
                //searchModel.setBeginDt(DateUtils.addDays(new Date(), -3).getTime());
                dataTimeAt = DateUtils.addDays(new Date(), -2).getTime();
            }else if(date.getTime()<twoTuple.getAElement()){ //如果当前时间小于9九点
                //searchModel.setBeginDt(DateUtils.addDays(new Date(twoTuple.getAElement()), -3).getTime());
                dataTimeAt = DateUtils.addDays(new Date(twoTuple.getAElement()), -2).getTime();
            }else{//如果是18点
                //searchModel.setBeginDt(DateUtils.addDays(new Date(twoTuple.getAElement()), -2).getTime());
                dataTimeAt = DateUtils.addDays(new Date(twoTuple.getAElement()), -1).getTime();
            }
            timeoutAt = dataTimeAt-(6*60*60*1000);
            Calendar c = Calendar.getInstance();
            c.setTime(new Date(dataTimeAt));
            c.set(Calendar.HOUR_OF_DAY, 9);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            if(c.getTime().getTime()>timeoutAt){
                timeoutAt = c.getTime().getTime()-(15*60*60*1000)-(c.getTime().getTime()-timeoutAt);
            }
            searchModel.setEndDt(timeoutAt);
            //小于48小时
            if(twoTuple.getAElement()<= date.getTime() && date.getTime()<= twoTuple.getBElement()){//在工作时间内
                dataTimeAt = DateUtils.addDays(new Date(), -5).getTime();
            }else if(date.getTime()<twoTuple.getAElement()){ //如果当前时间小于9九点
                dataTimeAt = DateUtils.addDays(new Date(twoTuple.getAElement()), -5).getTime();
            }else{//如果是18点
                dataTimeAt = DateUtils.addDays(new Date(twoTuple.getAElement()), -4).getTime();
            }
            timeoutAt = dataTimeAt-(3*60*60*1000);
            Calendar endDt = Calendar.getInstance();
            endDt.setTime(new Date(dataTimeAt));
            endDt.set(Calendar.HOUR_OF_DAY, 9);
            endDt.set(Calendar.MINUTE, 0);
            endDt.set(Calendar.SECOND, 0);
            if(endDt.getTime().getTime()>timeoutAt){
                timeoutAt = endDt.getTime().getTime()-(15*60*60*1000)-(endDt.getTime().getTime()-timeoutAt);
            }
            searchModel.setBeginDt(timeoutAt);
            page = kefuReminderService.moreThan24HoursUnfinishedList(page, searchModel);
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
     * 超过48小时未完成(工单未完成)催单
     */
    @RequiresPermissions(value ="sd:reminder:view")
    @RequestMapping(value = "list/moreThan48HoursUnfinishedList")
    public String moreThan48HoursUnfinishedList(ReminderPageSearchModel searchModel, HttpServletRequest request, HttpServletResponse response, Model model) {
        String viewForm = "modules/sd/reminder/kefu/list/moreThan48HoursUnfinishedList";
        User user = UserUtils.getUser();
        Page<ReminderModel> page = new Page(request, response);
        long areaId = searchModel.getAreaId();
        searchModel = setSearchModel(model, user,searchModel,3,365);
        if(!searchModel.getIsValid()){
            model.addAttribute(MODEL_ATTR_PAGE, page);
            model.addAttribute(MODEL_ATTR_ENTITY, searchModel);
            return viewForm;
        }
        try {
            Date date = new Date();
            TwoTuple<Long,Long> twoTuple = DateUtils.getTwoTupleDate(9,18);
            long dataTimeAt=0L;
            long timeoutAt = 0L;
            if(twoTuple.getAElement()<= date.getTime() && date.getTime()<= twoTuple.getBElement()){//在工作时间内
                dataTimeAt = DateUtils.addDays(new Date(), -5).getTime();
            }else if(date.getTime()<twoTuple.getAElement()){ //如果当前时间小于9九点
                dataTimeAt = DateUtils.addDays(new Date(twoTuple.getAElement()), -5).getTime();
            }else{//如果是18点
                dataTimeAt = DateUtils.addDays(new Date(twoTuple.getAElement()), -4).getTime();
            }
            timeoutAt = dataTimeAt-(3*60*60*1000);
            Calendar c = Calendar.getInstance();
            c.setTime(new Date(dataTimeAt));
            c.set(Calendar.HOUR_OF_DAY, 9);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            if(c.getTime().getTime()>timeoutAt){
                timeoutAt = c.getTime().getTime()-(15*60*60*1000)-(c.getTime().getTime()-timeoutAt);
            }
            searchModel.setBeginDt(timeoutAt);
            page = kefuReminderService.moreThan48HoursUnfinishedList(page, searchModel);
        } catch (Exception e) {
            addMessage(model, "查询错误：" + Exceptions.getRootCauseMessage(e));
        }finally {
            searchModel.setAreaId(areaId);//还原
        }
        model.addAttribute(MODEL_ATTR_PAGE, page);
        model.addAttribute(MODEL_ATTR_ENTITY, searchModel);
        return viewForm;
    }

    //endregion

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
        }else{
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

    private String reminderCheckFail(Model model, String formView, ReminderModel reminder, String s) {
        addMessage(model, s);
        model.addAttribute("canAction", false);
        model.addAttribute("reminder", reminder);
        return formView;
    }

	//endregion 公共

    //region 客服催单Form
    /**
     * 处理窗口
     * @param id	催单id
     * @param quarter 	分片
     */
    @RequestMapping(value = "/processForm", method = RequestMethod.GET)
    public String processForm(@RequestParam String id, @RequestParam(required = false) String quarter, Model model, Long itemId) {
        String formView = "modules/sd/reminder/kefu/processForm";
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
        Reminder reminderModel = reminderService.getReminderById(lid,quarter,1);
        if(reminderModel == null){
            return reminderCheckFail(model, formView, reminder, "读取催单失败，或催单单据不存在。");
        }
        ReminderItem currentItem =null;
        List<ReminderItem> itemList = reminderModel.getItems();
        if(itemId!=null && itemId>0){
            if(itemList!=null && itemList.size()>0){
                currentItem = itemList.stream().filter(t->t.getId()==itemId && t.getStatus()==ReminderStatus.WaitReply.getCode()).findFirst().orElse(null);
            }
        }else{
            currentItem = itemList.stream().filter(t->t.getStatus()==ReminderStatus.WaitReply.getCode()).findFirst().orElse(null);
        }
        if(currentItem==null){
            ReminderStatus reminderStatus = ReminderStatus.fromCode(reminderModel.getStatus());
            return reminderCheckFail(model, formView, reminder, "当前催单状态:" + (reminderStatus==null?"":reminderStatus.getMsg()) + ",不能进行处理操作,请重新刷新页面");
        }

        reminderModel.setItemId(currentItem.getId());
        reminder = Mappers.getMapper(ReminderViewModelMapper.class).toViewModel(reminderModel);
        if(reminder == null){
            return reminderCheckFail(model, formView, reminder, "单据转换失败，请重试");
        }
        /*if(reminderModel.getItemId()>0 && !CollectionUtils.isEmpty(reminderModel.getItems())){
            ReminderItem item = reminderModel.getItems().stream().filter(t->t.getId() == reminderModel.getItemId()).findFirst().orElse(null);
            if(item != null){
                reminder.setCreateRemark(item.getCreateRemark());
                reminder.setCreateName(item.getCreateName());
                reminder.setCreateDate(DateFormatUtils.format(item.getCreateAt(), "yyyy-MM-dd HH:mm:ss"));
                reminder.setReminderReason(item.getReminderReason());
            }
        }*/
        reminder.setCreateRemark(currentItem.getCreateRemark());
        reminder.setCreateName(currentItem.getCreateName());
        reminder.setCreateDate(DateFormatUtils.format(currentItem.getCreateAt(), "yyyy-MM-dd HH:mm:ss"));
        reminder.setReminderReason(currentItem.getReminderReason());
        reminder.setProcessRemark("已与【" + reminder.getUserPhone() + "】联系，沟通方案为【】，预计完成时间【】");
        model.addAttribute("canAction", true);
        model.addAttribute("reminder", reminder);
        return formView;
    }

    //endregion

}
