package com.wolfking.jeesite.modules.sd.web;

import com.google.common.collect.Lists;
import com.kkl.kklplus.entity.cc.Reminder;
import com.kkl.kklplus.entity.cc.ReminderItem;
import com.kkl.kklplus.entity.cc.ReminderStatus;
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
import com.wolfking.jeesite.modules.sys.entity.KefuTypeEnum;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.SystemService;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.cc.entity.ReminderModel;
import com.wolfking.jeesite.ms.cc.entity.mapper.ReminderViewModelMapper;
import com.wolfking.jeesite.ms.cc.service.CustomerReminderService;
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
@RequestMapping(value = "${adminPath}/sd/customer/reminder")
@Slf4j
public class CustomerReminderController extends BaseController
{
    private static final String MODEL_ATTR_PAGE = "page";
    private static final String MODEL_ATTR_ENTITY = "reminder";

	@Autowired
    private CustomerReminderService customerReminderService;

    @Autowired
    private ReminderService reminderService;


	//region 列表及读取

	/**
	 * 待回复列表
	 */
    @RequiresPermissions(value ="sd:customerReminder:view")
    @RequestMapping(value = "list/process")
	public String waitReplyList(ReminderPageSearchModel searchModel, HttpServletRequest request, HttpServletResponse response, Model model) {
        String viewForm = "modules/sd/reminder/customer/list/waitReplyList";
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
            page = customerReminderService.waitReplyList(page, searchModel);
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
    @RequiresPermissions(value ="sd:customerReminder:view")
    @RequestMapping(value = "/list/haveRepliedList")
    public String haveRepliedList(ReminderPageSearchModel searchModel, HttpServletRequest request, HttpServletResponse response, Model model) {
        String viewForm = "modules/sd/reminder/customer/list/haveReplied";
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
            page = customerReminderService.haveRepliedList(page, searchModel);
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
    @RequiresPermissions(value ="sd:customerReminder:view")
	@RequestMapping(value = "/list/all")
	public String allList(ReminderPageSearchModel searchModel, HttpServletRequest request, HttpServletResponse response, Model model) {
        String viewForm = "modules/sd/reminder/customer/list/all";
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
            page = customerReminderService.getPage(page, searchModel);
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
        Boolean isValid = checkOrderNoAndPhone(searchModel,model);
        if(!isValid){
            searchModel.setIsValid(false);
            return searchModel;
        }
        if (user.isCustomer()) {
            if (user.getCustomerAccountProfile() != null && user.getCustomerAccountProfile().getCustomer() != null) {
                //登录用户的客户，防篡改
                searchModel.setCustomerId(user.getCustomerAccountProfile().getCustomer().getId());
            } else {
                searchModel.setIsValid(false);
                addMessage(model, "错误：登录超时，请退出后重新登录。");
                model.addAttribute("canAction", false);
                model.addAttribute("searchModel", searchModel);
                return searchModel;
            }
            //客户账号负责的店铺
            List<String> shopIds = UserUtils.getShopIdsOfCustomerAccount(user);
            if(!CollectionUtils.isEmpty(shopIds)){
                searchModel.setShopIds(shopIds);
            }
        }
        initAreaSearchBySelect(searchModel);
        return searchModel;
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

    private int checkIsPhone(ReminderPageSearchModel searchModel){
        if (StringUtils.isNotBlank(searchModel.getUserPhone())){
            if("".equalsIgnoreCase(StringUtils.isPhoneWithRelaxed(searchModel.getUserPhone()))){
                searchModel.setIsPhone(1);
            }
        }
        return searchModel.getIsPhone();
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
    @RequestMapping(value = "/viewForm", method = RequestMethod.GET)
    public String processForm(@RequestParam String id, @RequestParam(required = false) String quarter, Model model) {
        String formView = "modules/sd/reminder/customer/viewForm";
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

        reminder = Mappers.getMapper(ReminderViewModelMapper.class).toViewModel(reminderModel);
        if(reminder == null){
            return reminderCheckFail(model, formView, reminder, "单据转换失败，请重试");
        }
        model.addAttribute("canAction", true);
        model.addAttribute("reminder", reminder);
        return formView;
    }

    //endregion

}
