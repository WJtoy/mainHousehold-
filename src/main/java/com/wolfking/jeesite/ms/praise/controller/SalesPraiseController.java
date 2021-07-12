package com.wolfking.jeesite.ms.praise.controller;
import com.google.common.collect.Lists;
import com.kkl.kklplus.entity.md.GlobalMappingSalesSubFlagEnum;
import com.kkl.kklplus.entity.md.MDCustomerPraiseFee;
import com.kkl.kklplus.entity.praise.Praise;
import com.kkl.kklplus.entity.praise.PraisePageSearchModel;
import com.kkl.kklplus.entity.praise.PraiseStatusEnum;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.CurrencyUtil;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.service.CustomerService;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.praise.entity.PraiseLogModel;
import com.wolfking.jeesite.ms.praise.entity.ViewPraiseModel;
import com.wolfking.jeesite.ms.praise.service.OrderPraiseService;
import com.wolfking.jeesite.ms.praise.service.SalesPraiseService;
import com.wolfking.jeesite.ms.providermd.service.MSCustomerPraiseFeeService;
import lombok.extern.slf4j.Slf4j;
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
import java.util.List;

@Controller
@RequestMapping(value = "${adminPath}/sd/sales/praise/")
@Slf4j
public class SalesPraiseController extends BaseController {


    @Autowired
    private SalesPraiseService salesPraiseService;

    @Autowired
    private MSCustomerPraiseFeeService customerPraiseFeeService;

    @Autowired
    private OrderPraiseService orderPraiseService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private CustomerService customerService;


    /**
     * 业务查询待处理好评信息列表
     * @param praisePageSearchModel
     * @param request
     */
    @RequiresPermissions("sd:salespraise:view")
    @RequestMapping(value = "pendingReviewList")
    public String pendingReviewList(PraisePageSearchModel praisePageSearchModel, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<ViewPraiseModel> page = new Page(request, response);
        User user = UserUtils.getUser();
        if (user == null || user.getId() == null) {
            addMessage(model, "错误：登录超时，请退出后重新登录。");
            model.addAttribute("page", page);
            model.addAttribute("praisePageSearchModel", praisePageSearchModel);
            return "modules/sd/praise/salesPraiseList/pendingReviewList";
        }
        if (!user.isSystemUser()) {
            addMessage(model, "错误：无权限，此功能只开放给后台人员使用。");
            model.addAttribute("page", page);
            model.addAttribute("praisePageSearchModel", praisePageSearchModel);
            return "modules/sd/praise/salesPraiseList/pendingReviewList";
        }

        Date date;
        if (StringUtils.isBlank(praisePageSearchModel.getBeginDate())) {
            date = DateUtils.getDateEnd(new Date());
            praisePageSearchModel.setEndDate(DateUtils.formatDate(date,"yyyy-MM-dd"));
            praisePageSearchModel.setEndDt(date.getTime());
            date = DateUtils.getStartDayOfMonth(DateUtils.addMonth(new Date(), -1));
            praisePageSearchModel.setBeginDate(DateUtils.formatDate(date,"yyyy-MM-dd"));
            praisePageSearchModel.setBeginDt(date.getTime());
        } else {
            date = DateUtils.parseDate(praisePageSearchModel.getBeginDate());
            praisePageSearchModel.setBeginDt(date.getTime());
            date = DateUtils.parseDate(praisePageSearchModel.getEndDate());
            date = DateUtils.getDateEnd(date);
            praisePageSearchModel.setEndDt(date.getTime());
        }
        if (user.isSaleman()) {
            praisePageSearchModel.setSalesId(user.getId());//业务员
            List<Long> offlineCustomers = customerService.findIdListByOfflineOrderFlagFromCacheForSD();
            if(!org.springframework.util.CollectionUtils.isEmpty(offlineCustomers)){
                praisePageSearchModel.setOfflineCustomerList(offlineCustomers);
            }
            praisePageSearchModel.setSubFlag(user.getSubFlag()==null?0:user.getSubFlag());
            /*if(user.isSalesPerson()){ //业务
                praisePageSearchModel.setSubFlag(GlobalMappingSalesSubFlagEnum.SALES.getValue());
            }
            if(user.isMerchandiser()){ //跟单
                praisePageSearchModel.setSubFlag(GlobalMappingSalesSubFlagEnum.MERCHANDISER.getValue());
            }*/
        }
        page = salesPraiseService.pendingReviewList(page,praisePageSearchModel);
        model.addAttribute("page", page);
        model.addAttribute("praisePageSearchModel", praisePageSearchModel);
        return "modules/sd/praise/salesPraiseList/pendingReviewList";
    }



    /**
     * 已审核好评单列表 get请求 (不点击出查询按钮不加载数据)
     * */
    @RequiresPermissions("sd:salespraise:view")
    @RequestMapping(value = {"approvedList"}, method = RequestMethod.GET)
    public String approvedListGet(PraisePageSearchModel praisePageSearchModel, Model model) {
        Date date;
        date = DateUtils.getDateEnd(new Date());
        praisePageSearchModel.setEndDate(DateUtils.formatDate(date,"yyyy-MM-dd"));
        praisePageSearchModel.setEndDt(date.getTime());
        date = DateUtils.getStartDayOfMonth(DateUtils.addMonth(new Date(), -1));
        praisePageSearchModel.setBeginDate(DateUtils.formatDate(date,"yyyy-MM-dd"));
        praisePageSearchModel.setBeginDt(date.getTime());
        model.addAttribute("page", null);
        model.addAttribute("praisePageSearchModel", praisePageSearchModel);
        return "modules/sd/praise/salesPraiseList/approvedList";
    }

    /**
     * 已审核好评单列表 post请求
     * */
    @RequiresPermissions("sd:salespraise:view")
    @RequestMapping(value = {"approvedList"},method = RequestMethod.POST)
    public String approvedList(PraisePageSearchModel praisePageSearchModel, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<ViewPraiseModel> page = new Page(request, response);
        User user = UserUtils.getUser();
        if (user == null || user.getId() == null) {
            addMessage(model, "错误：登录超时，请退出后重新登录。");
            model.addAttribute("page", page);
            model.addAttribute("praisePageSearchModel", praisePageSearchModel);
            return "modules/sd/praise/salesPraiseList/approvedList";
        }
        if (!user.isSystemUser()) {
            addMessage(model, "错误：无权限，此功能只开放给后台人员使用。");
            model.addAttribute("page", page);
            model.addAttribute("praisePageSearchModel", praisePageSearchModel);
            return "modules/sd/praise/salesPraiseList/approvedList";
        }
        Date date;
        if (StringUtils.isBlank(praisePageSearchModel.getBeginDate())) {
            date = DateUtils.getDateEnd(new Date());
            praisePageSearchModel.setEndDate(DateUtils.formatDate(date,"yyyy-MM-dd"));
            praisePageSearchModel.setEndDt(date.getTime());
            date = DateUtils.getStartDayOfMonth(DateUtils.addMonth(new Date(), -1));
            praisePageSearchModel.setBeginDate(DateUtils.formatDate(date,"yyyy-MM-dd"));
            praisePageSearchModel.setBeginDt(date.getTime());
        } else {
            date = DateUtils.parseDate(praisePageSearchModel.getBeginDate());
            praisePageSearchModel.setBeginDt(date.getTime());
            date = DateUtils.parseDate(praisePageSearchModel.getEndDate());
            date = DateUtils.getDateEnd(date);
            praisePageSearchModel.setEndDt(date.getTime());
        }
        if (user.isSaleman()) {
            praisePageSearchModel.setSalesId(user.getId());//业务员
            List<Long> offlineCustomers = customerService.findIdListByOfflineOrderFlagFromCacheForSD();
            if(!org.springframework.util.CollectionUtils.isEmpty(offlineCustomers)){
                praisePageSearchModel.setOfflineCustomerList(offlineCustomers);
            }
            praisePageSearchModel.setSubFlag(user.getSubFlag()==null?0:user.getSubFlag());
            /*
            if(user.isSalesPerson()){ //业务
                praisePageSearchModel.setSubFlag(GlobalMappingSalesSubFlagEnum.SALES.getValue());
            }
            if(user.isMerchandiser()){ //跟单
                praisePageSearchModel.setSubFlag(GlobalMappingSalesSubFlagEnum.MERCHANDISER.getValue());
            }*/
        }
        page = salesPraiseService.approvedList(page,praisePageSearchModel);
        model.addAttribute("page", page);
        model.addAttribute("praisePageSearchModel", praisePageSearchModel);
        return "modules/sd/praise/salesPraiseList/approvedList";
    }



    /**
     * 所有好评单列表 get请求 (不点击出查询按钮不加载数据)
     * */
    @RequiresPermissions("sd:salespraise:view")
    @RequestMapping(value = {"findAllList"}, method = RequestMethod.GET)
    public String findAllListGet(PraisePageSearchModel praisePageSearchModel, Model model) {
        Date date;
        date = DateUtils.getDateEnd(new Date());
        praisePageSearchModel.setEndDate(DateUtils.formatDate(date,"yyyy-MM-dd"));
        praisePageSearchModel.setEndDt(date.getTime());
        date = DateUtils.getStartDayOfMonth(DateUtils.addMonth(new Date(), -1));
        praisePageSearchModel.setBeginDate(DateUtils.formatDate(date,"yyyy-MM-dd"));
        praisePageSearchModel.setBeginDt(date.getTime());
        model.addAttribute("page", null);
        model.addAttribute("praisePageSearchModel", praisePageSearchModel);
        return "modules/sd/praise/salesPraiseList/allList";
    }

    /**
     * 业务查询所有好评信息列表
     * @param praisePageSearchModel
     * @param request
     */
    @RequiresPermissions("sd:salespraise:view")
    @RequestMapping(value = {"findAllList"},method = RequestMethod.POST)
    public String findAllList(PraisePageSearchModel praisePageSearchModel, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<ViewPraiseModel> page = new Page(request, response);
        User user = UserUtils.getUser();
        if (user == null || user.getId() == null) {
            addMessage(model, "错误：登录超时，请退出后重新登录。");
            model.addAttribute("page", page);
            model.addAttribute("praisePageSearchModel", praisePageSearchModel);
            return "modules/sd/praise/salesPraiseList/allList";
        }
        if (!user.isSystemUser()) {
            addMessage(model, "错误：无权限，此功能只开放给后台人员使用。");
            model.addAttribute("page", page);
            model.addAttribute("praisePageSearchModel", praisePageSearchModel);
            return "modules/sd/praise/salesPraiseList/allList";
        }
        Date date;
        if (StringUtils.isBlank(praisePageSearchModel.getBeginDate())) {
            date = DateUtils.getDateEnd(new Date());
            praisePageSearchModel.setEndDate(DateUtils.formatDate(date,"yyyy-MM-dd"));
            praisePageSearchModel.setEndDt(date.getTime());
            date = DateUtils.getStartDayOfMonth(DateUtils.addMonth(new Date(), -1));
            praisePageSearchModel.setBeginDate(DateUtils.formatDate(date,"yyyy-MM-dd"));
            praisePageSearchModel.setBeginDt(date.getTime());
        } else {
            date = DateUtils.parseDate(praisePageSearchModel.getBeginDate());
            praisePageSearchModel.setBeginDt(date.getTime());
            date = DateUtils.parseDate(praisePageSearchModel.getEndDate());
            date = DateUtils.getDateEnd(date);
            praisePageSearchModel.setEndDt(date.getTime());
        }
        if (user.isSaleman()) {
            praisePageSearchModel.setSalesId(user.getId());//业务员
            List<Long> offlineCustomers = customerService.findIdListByOfflineOrderFlagFromCacheForSD();
            if(!org.springframework.util.CollectionUtils.isEmpty(offlineCustomers)){
                praisePageSearchModel.setOfflineCustomerList(offlineCustomers);
            }
            praisePageSearchModel.setSubFlag(user.getSubFlag()==null?0:user.getSubFlag());
            /*
            if(user.isSalesPerson()){ //业务
                praisePageSearchModel.setSubFlag(GlobalMappingSalesSubFlagEnum.SALES.getValue());
            }else if(user.isMerchandiser()){ //跟单
                praisePageSearchModel.setSubFlag(GlobalMappingSalesSubFlagEnum.MERCHANDISER.getValue());
            }*/
        }
        page = salesPraiseService.findPraiseList(page,praisePageSearchModel);
        model.addAttribute("page", page);
        model.addAttribute("praisePageSearchModel", praisePageSearchModel);
        return "modules/sd/praise/salesPraiseList/allList";
    }

    /**
     * 业务好评单详情
     * @param id
     * @param quarter
     * @param model
     */
    @RequestMapping(value = "praiseInfoForSales")
    public String praiseInfoForSales(Long id,String quarter,Model model){
        model.addAttribute("canAction",true);
        model.addAttribute("canSave",false);
        ViewPraiseModel praiseModel = salesPraiseService.getPraiseInfoForSale(id,quarter);
        if(praiseModel==null){
            addMessage(model,"读取好评费失败,请重新尝试!");
            model.addAttribute("canAction",false);
            return "modules/sd/praise/salesPraiseList/praiseInfoForSalesForm";
        }
        MDCustomerPraiseFee customerPraiseFee = customerPraiseFeeService.getByCustomerIdFromCacheNewForCP(praiseModel.getCustomerId());
        if(customerPraiseFee==null){
            addMessage(model,"错误,读取客户好评配置失败,请重新尝试!");
            model.addAttribute("canAction",false);
            return "modules/sd/praise/salesPraiseList/praiseInfoForSalesForm";
        }
        customerPraiseFee.setPraiseRequirement
                (org.apache.commons.lang3.StringUtils.replace(customerPraiseFee.getPraiseRequirement(),"\n","<br>"));
        if(praiseModel.getStatus()== PraiseStatusEnum.PENDING_REVIEW.code){
            model.addAttribute("canSave",true);
        }
        List<PraiseLogModel> praiseLogModelList = orderPraiseService.finPraiseLogList(quarter,id);
        if(praiseLogModelList==null){
            praiseLogModelList = Lists.newArrayList();
        }
        model.addAttribute("praise",praiseModel);
        model.addAttribute("customerPraiseFee",customerPraiseFee);
        model.addAttribute("praiseLogModelList",praiseLogModelList);
        return "modules/sd/praise/salesPraiseList/praiseInfoForSalesForm";
    }

    /**
     * 业务通过好评单审核
     * @param praise
     */
    @RequiresPermissions("sd:salespraise:edit")
    @RequestMapping("approve")
    @ResponseBody
    public AjaxJsonEntity approve(Praise praise,HttpServletRequest request){
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        User user = UserUtils.getUser();
        if(user==null){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("当前用户不存在,请重新登陆");
            return ajaxJsonEntity;
        }
        try {
            MDCustomerPraiseFee customerPraiseFee = customerPraiseFeeService.getByCustomerIdFromCacheNewForCP(praise.getCustomerId());
            if(customerPraiseFee==null){
                ajaxJsonEntity.setSuccess(false);
                ajaxJsonEntity.setMessage("获取客户好评设定失败");
                return ajaxJsonEntity;
            }

            //计算费用
            double servicePointFee = 0;
            if(customerPraiseFee.getDiscount()<=0){
                servicePointFee = praise.getCustomerPraiseFee();
            }else{
                servicePointFee = praise.getCustomerPraiseFee()-CurrencyUtil.round2(praise.getCustomerPraiseFee()*customerPraiseFee.getDiscount());
            }
            praise.setServicepointPraiseFee(servicePointFee);
            salesPraiseService.approve(praise,user);
        }catch (Exception e){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage(e.getMessage());
        }
        return ajaxJsonEntity;
    }


    /**
     * 业务驳回好评单审核
     * @param praise
     */
    @RequiresPermissions("sd:salespraise:edit")
    @RequestMapping("reject")
    @ResponseBody
    public AjaxJsonEntity reject(Praise praise){
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        Order order = orderService.getOrderById(praise.getOrderId(), praise.getQuarter(), OrderUtils.OrderDataLevel.CONDITION,true);
        if(order==null){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("工单信息丢失,请重试");
            return ajaxJsonEntity;
        }
        User user = UserUtils.getUser();
        if(user==null){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("当前用户不存在,请重新登陆");
            return ajaxJsonEntity;
        }
        try {
            salesPraiseService.reject(praise,order,user);
        }catch (Exception e){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage(e.getMessage());
        }
        return ajaxJsonEntity;
    }


    /**
     * 业务取消好评单审核
     * @param praise
     */
    @RequiresPermissions("sd:salespraise:edit")
    @RequestMapping("cancelled")
    @ResponseBody
    public AjaxJsonEntity cancelled(Praise praise){
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        User user = UserUtils.getUser();
        if(user==null){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("当前用户不存在,请重新登陆");
            return ajaxJsonEntity;
        }
        try {
            salesPraiseService.cancelled(praise,user);
        }catch (Exception e){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage(e.getMessage());
        }
        return ajaxJsonEntity;
    }

}
