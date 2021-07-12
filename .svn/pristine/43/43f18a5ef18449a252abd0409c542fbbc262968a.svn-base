package com.wolfking.jeesite.modules.servicepoint.receipt.web;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.kkl.kklplus.entity.common.NameValuePair;
import com.kkl.kklplus.entity.md.MDCustomerPraiseFee;
import com.kkl.kklplus.entity.md.MDCustomerPraiseFeePraiseStandardItem;
import com.kkl.kklplus.entity.praise.*;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.Engineer;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.md.service.ServicePointService;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.servicepoint.receipt.service.ServicePointPraiseService;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.modules.utils.PraiseUtils;
import com.wolfking.jeesite.ms.praise.entity.PraiseLogModel;
import com.wolfking.jeesite.ms.praise.entity.ViewPraiseModel;
import com.wolfking.jeesite.ms.praise.service.OrderPraiseService;
import com.wolfking.jeesite.ms.providermd.service.MSCustomerPraiseFeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "${adminPath}/servicePoint/receipt/praise/")
@Slf4j
public class ServicePointPraiseController extends BaseController {


    @Autowired
    private ServicePointPraiseService servicePointPraiseService;

    @Autowired
    private ServicePointService servicePointService;

    @Autowired
    private MSCustomerPraiseFeeService customerPraiseFeeService;

    @Autowired
    private OrderPraiseService orderPraiseService;

    @Autowired
    private OrderService orderService;



    private PraisePageSearchModel setSerachModel(PraisePageSearchModel praisePageSearchModel,User currentUser){
        if (currentUser.isEngineer()) {
            Engineer engineer = null;
            if (currentUser.getCompanyId() > 0) {
                engineer = servicePointService.getEngineerFromCache(currentUser.getCompanyId(), currentUser.getEngineerId());
            }
            if (engineer == null) {
                engineer = servicePointService.getEngineer(currentUser.getEngineerId());
            }
            Long servicePointId = (engineer.getServicePoint() == null ? null : engineer.getServicePoint().getId());
            praisePageSearchModel.setServicepointId(servicePointId);
      /*      if (engineer.getMasterFlag() == 1) {
                praisePageSearchModel.setEngineerId(null);
                if(servicePointId != null) {
                    List<Engineer> engineers = servicePointService.getEngineerListOfServicePoint(servicePointId);
                    searchModel.setEngineerList(engineers);
                }
            } else {
                praisePageSearchModel.set
            }*/
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
        return praisePageSearchModel;
    }

    /**
     * 业务查询待处理好评信息列表
     * @param praisePageSearchModel
     * @param request
     */
    @RequiresPermissions("sd:servicepointpraise:view")
    @RequestMapping(value = "pendingReviewList")
    public String pendingReviewList(PraisePageSearchModel praisePageSearchModel, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<ViewPraiseModel> page = new Page(request, response);
        User user = UserUtils.getUser();
        if (user == null || user.getId() == null) {
            addMessage(model, "错误：登录超时，请退出后重新登录。");
            model.addAttribute("page", page);
            model.addAttribute("praisePageSearchModel", praisePageSearchModel);
            return "modules/servicePoint/receipt/praise/pendingReviewList";
        }
        praisePageSearchModel = setSerachModel(praisePageSearchModel,user);
        page = servicePointPraiseService.pendingReviewList(page,praisePageSearchModel);
        model.addAttribute("page", page);
        model.addAttribute("praisePageSearchModel", praisePageSearchModel);
        return "modules/servicePoint/receipt/praise/pendingReviewList";
    }


    /**
     * 网点查询已审核好评信息列表
     * @param praisePageSearchModel
     * @param model
     */
    @RequiresPermissions("sd:servicepointpraise:view")
    @RequestMapping(value = {"approvedList"},method = RequestMethod.GET)
    public String approvedListGet(PraisePageSearchModel praisePageSearchModel, Model model){
        Date date;
        date = DateUtils.getDateEnd(new Date());
        praisePageSearchModel.setEndDate(DateUtils.formatDate(date,"yyyy-MM-dd"));
        praisePageSearchModel.setEndDt(date.getTime());
        date = DateUtils.getStartDayOfMonth(DateUtils.addMonth(new Date(), -1));
        praisePageSearchModel.setBeginDate(DateUtils.formatDate(date,"yyyy-MM-dd"));
        praisePageSearchModel.setBeginDt(date.getTime());
        model.addAttribute("page", null);
        model.addAttribute("praisePageSearchModel", praisePageSearchModel);
        return "modules/servicePoint/receipt/praise/approvedList";
    }

    /**
     * 网点查询已审核好评信息列表
     * @param praisePageSearchModel
     * @param request
     */
    @RequiresPermissions("sd:servicepointpraise:view")
    @RequestMapping(value = {"approvedList"},method = RequestMethod.POST)
    public String approvedList(PraisePageSearchModel praisePageSearchModel, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<ViewPraiseModel> page = new Page(request, response);
        User user = UserUtils.getUser();
        if (user == null || user.getId() == null) {
            addMessage(model, "错误：登录超时，请退出后重新登录。");
            model.addAttribute("page", page);
            model.addAttribute("praisePageSearchModel", praisePageSearchModel);
            return "modules/servicePoint/receipt/praise/approvedList";
        }
        praisePageSearchModel = setSerachModel(praisePageSearchModel,user);
        page = servicePointPraiseService.approvedList(page,praisePageSearchModel);
        model.addAttribute("page", page);
        model.addAttribute("praisePageSearchModel", praisePageSearchModel);
        return "modules/servicePoint/receipt/praise/approvedList";
    }

    /**
     * 网点查询已驳回好评信息列表
     * @param praisePageSearchModel
     * @param request
     */
    @RequiresPermissions("sd:servicepointpraise:view")
    @RequestMapping(value = "rejectList")
    public String rejectList(PraisePageSearchModel praisePageSearchModel, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<ViewPraiseModel> page = new Page(request, response);
        User user = UserUtils.getUser();
        if (user == null || user.getId() == null) {
            addMessage(model, "错误：登录超时，请退出后重新登录。");
            model.addAttribute("page", page);
            model.addAttribute("praisePageSearchModel", praisePageSearchModel);
            return "modules/servicePoint/receipt/praise/rejectList";
        }
        praisePageSearchModel = setSerachModel(praisePageSearchModel,user);
        page = servicePointPraiseService.rejectList(page,praisePageSearchModel);
        model.addAttribute("page", page);
        model.addAttribute("praisePageSearchModel", praisePageSearchModel);
        return "modules/servicePoint/receipt/praise/rejectList";
    }


    /**
     * 网点查询所有好评信息列表
     * @param praisePageSearchModel
     */
    @RequestMapping(value = {"findAllList"},method = RequestMethod.GET)
    public String findAllListGet(PraisePageSearchModel praisePageSearchModel, Model model){
        Date date;
        date = DateUtils.getDateEnd(new Date());
        praisePageSearchModel.setEndDate(DateUtils.formatDate(date,"yyyy-MM-dd"));
        praisePageSearchModel.setEndDt(date.getTime());
        date = DateUtils.getStartDayOfMonth(DateUtils.addMonth(new Date(), -1));
        praisePageSearchModel.setBeginDate(DateUtils.formatDate(date,"yyyy-MM-dd"));
        praisePageSearchModel.setBeginDt(date.getTime());
        model.addAttribute("page", null);
        model.addAttribute("praisePageSearchModel", praisePageSearchModel);
        return "modules/servicePoint/receipt/praise/allList";
    }

    /**
     * 网点查询所有好评信息列表
     * @param praisePageSearchModel
     * @param request
     */
    @RequiresPermissions("sd:servicepointpraise:view")
    @RequestMapping(value = {"findAllList"},method = RequestMethod.POST)
    public String findAllList(PraisePageSearchModel praisePageSearchModel, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<ViewPraiseModel> page = new Page(request, response);
        User user = UserUtils.getUser();
        if (user == null || user.getId() == null) {
            addMessage(model, "错误：登录超时，请退出后重新登录。");
            model.addAttribute("page", page);
            model.addAttribute("praisePageSearchModel", praisePageSearchModel);
            return "modules/servicePoint/receipt/praise/allLis";
        }
        praisePageSearchModel = setSerachModel(praisePageSearchModel,user);
        page = servicePointPraiseService.findPraiseList(page,praisePageSearchModel);
        model.addAttribute("page", page);
        model.addAttribute("praisePageSearchModel", praisePageSearchModel);
        return "modules/servicePoint/receipt/praise/allList";
    }

    /**
     * 网点好评单详情
     * @param id
     * @param quarter
     * @param model
     */
     @RequestMapping(value = "praiseInfoForServicePoint")
    public String praiseInfoForServicePoint(Long id,String quarter,Model model){
         model.addAttribute("canAction",true);
         model.addAttribute("canSave",false);
         ViewPraiseModel praiseModel = servicePointPraiseService.getPraiseInfoForServicePoint(id,quarter);
         if(praiseModel==null){
             addMessage(model,"读取好评费失败,请重新尝试!");
             model.addAttribute("canAction",false);
             return "modules/sd/praise/servicePointPraiseList/praiseInfoForServicePointForm";
         }
         MDCustomerPraiseFee customerPraiseFee = customerPraiseFeeService.getByCustomerIdFromCacheNewForCP(praiseModel.getCustomerId());
         List<MDCustomerPraiseFeePraiseStandardItem> praiseList = Lists.newArrayList();
         if(customerPraiseFee==null){
             addMessage(model,"错误:获取客户好评配置错误！");
             model.addAttribute("canAction",false);
             return "modules/sd/praise/servicePointPraiseList/praiseInfoForServicePointForm";
         }else{
             praiseList = customerPraiseFee.getPraiseStandardItems();
         }
         customerPraiseFee.setPraiseRequirement
                 (org.apache.commons.lang3.StringUtils.replace(customerPraiseFee.getPraiseRequirement(),"\n","<br>"));
         if(praiseModel.getStatus()== PraiseStatusEnum.REJECT.code || praiseModel.getStatus() == PraiseStatusEnum.NEW.code){
             model.addAttribute("canSave",true);
         }
         List<PraiseLogModel> praiseLogModelList = orderPraiseService.finPraiseLogList(quarter,id);
         if(praiseLogModelList==null){
             praiseLogModelList = Lists.newArrayList();
         }
         model.addAttribute("praise",praiseModel);
         model.addAttribute("customerPraiseFee",customerPraiseFee);
         model.addAttribute("praiseLogModelList",praiseLogModelList);
         model.addAttribute("praiseList",praiseList);
         return "modules/servicePoint/receipt/praise/praiseInfoForServicePointForm";
     }

    /**
     * 网点修改好评单
     * @param praise
     */
    @RequiresPermissions("sd:servicepointpraise:edit")
    @RequestMapping("updatePraise")
    @ResponseBody
    public AjaxJsonEntity updatePraise(Praise praise){
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        User user = UserUtils.getUser();
        if(praise.getStatus()!= PraiseStatusEnum.NEW.code && praise.getStatus()!=PraiseStatusEnum.REJECT.code){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("好评单状态态不是创建或者驳回状态");
            return ajaxJsonEntity;
        }
        if(user ==null || user.getId()==null){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("当前用户不存在,请重新登录");
            return ajaxJsonEntity;
        }
        try {
            MDCustomerPraiseFee customerPraiseFee = customerPraiseFeeService.getByCustomerIdFromCacheNewForCP(praise.getCustomerId());
            if(customerPraiseFee==null){
                ajaxJsonEntity.setSuccess(false);
                ajaxJsonEntity.setMessage("读取好评配置信息失败,请重试");
                return ajaxJsonEntity;
            }
            if(customerPraiseFee.getPraiseFeeFlag()==0){
                praise.setApplyCustomerPraiseFee(0.0);
                praise.setApplyServicepointPraiseFee(0.0);
            }else{
                NameValuePair<Double,Double> praiseFee = OrderPraiseService.getPraiseFee(praise,customerPraiseFee);
                if(!praiseFee.getName().equals(praise.getApplyCustomerPraiseFee())){
                    ajaxJsonEntity.setSuccess(false);
                    ajaxJsonEntity.setMessage("申请厂商好评费不准确!请确认");
                    return ajaxJsonEntity;
                }
                if(!praiseFee.getValue().equals(praise.getApplyServicepointPraiseFee())){
                    ajaxJsonEntity.setSuccess(false);
                    ajaxJsonEntity.setMessage("申请网点好评费不准确!请确认");
                    return ajaxJsonEntity;
                }
            }
            servicePointPraiseService.updatePraise(praise,user);
        }catch (Exception e){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage(e.getMessage());
        }
        return ajaxJsonEntity;
    }

    /**
     * 网点取消好评单审核
     * @param praise
     */
    @RequiresPermissions("sd:servicepointpraise:edit")
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
            servicePointPraiseService.cancelled(praise,user);
        }catch (Exception e){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage(e.getMessage());
        }
        return ajaxJsonEntity;
    }

    /**
     * 网点跳转到好评单申请页面
     * @param orderId
     * @param quarter
     */
    @RequestMapping("praiseForm")
    public String praiseForm(Long orderId, Long customerId, String quarter, Model model) {
        model.addAttribute("canAction", true);
        model.addAttribute("canSave", false);

        Order order = orderService.getOrderById(orderId, quarter, OrderUtils.OrderDataLevel.DETAIL, true);
        if (order == null || order.getOrderCondition() == null) {
            Praise praise = new Praise();
            praise.setOrderId(orderId);
            praise.setQuarter(quarter);
            addMessage(model, "错误:读取订单失败,请重试！");
            model.addAttribute("canAction", false);
            model.addAttribute("praise", praise);
            return "modules/servicePoint/receipt/praise/applyPraiseForm";
        }
        MDCustomerPraiseFee customerPraiseFee = customerPraiseFeeService.getByCustomerIdFromCacheNewForCP(customerId);
        List<MDCustomerPraiseFeePraiseStandardItem> praiseList = Lists.newArrayList();
        if (customerPraiseFee == null) {
            Praise praise = new Praise();
            praise.setOrderId(orderId);
            praise.setQuarter(quarter);
            addMessage(model, "错误:获取客户好评配置错误！");
            model.addAttribute("canAction", false);
            model.addAttribute("praise", praise);
            return "modules/servicePoint/receipt/praise/applyPraiseForm";
            //model.addAttribute("configFlag",false);
        }else{
            praiseList = customerPraiseFee.getPraiseStandardItems();
        }
        customerPraiseFee.setPraiseRequirement
                (org.apache.commons.lang3.StringUtils.replace(customerPraiseFee.getPraiseRequirement(),"\n","<br>"));
        double servicePointFee = 0.0;
        if(customerPraiseFee.getPraiseFeeFlag()==1){
            NameValuePair<Double,Double> nameValuePair = PraiseUtils.calculatePraiseCost(customerPraiseFee.getPraiseFee(),customerPraiseFee.getMaxPraiseFee(),customerPraiseFee.getDiscount(),null);
            servicePointFee = nameValuePair.getValue();
        }
        ServicePoint servicePoint = order.getOrderCondition().getServicePoint();
        Praise praise = orderPraiseService.getByOrderId(quarter, orderId, servicePoint.getId());
        if (praise != null) {
            if (praise.getStatus() == PraiseStatusEnum.REJECT.code || praise.getStatus() == PraiseStatusEnum.NEW.code) {
                model.addAttribute("canSave", true);
            }
            praise.setOrderId(orderId);
            praise.setQuarter(quarter);
        } else {
            //添加
            praise = new Praise();
            praise.setOrderId(orderId);
            praise.setQuarter(quarter);
            model.addAttribute("canSave",true);
            String strProductName="";
            List<String> productNames = order.getItems().stream().map(t->t.getProduct().getName()).collect(Collectors.toList());
            strProductName = StringUtils.join(productNames,",");
            praise.setApplyServicepointPraiseFee(servicePointFee);
            praise.setApplyCustomerPraiseFee(customerPraiseFee.getPraiseFee());
            praise.setProductNames(strProductName);
            praise.setOrderNo(order.getOrderNo());
        }
        praise.setServicepointId(servicePoint.getId());
        String servicePointName = servicePoint.getName();
        if (order.getOrderCondition().getStatusValue() >= Order.ORDER_STATUS_CHARGED) {
            addMessage(model, "工单已完成");
            model.addAttribute("canSave", false);
        }
        model.addAttribute("praise", praise);
        model.addAttribute("customerPraiseFee", customerPraiseFee);
        model.addAttribute("servicePointName", servicePointName);
        model.addAttribute("praiseList",praiseList);
        return "modules/servicePoint/receipt/praise/applyPraiseForm";
    }


    /**
     * 申请好评费
     * */
    @RequestMapping("applyPraise")
    @ResponseBody
    public AjaxJsonEntity applyPraise(Praise praise, HttpServletRequest request) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        Order order = orderService.getOrderById(praise.getOrderId(), praise.getQuarter(), OrderUtils.OrderDataLevel.DETAIL, true);
        if (order == null || order.getOrderCondition() == null) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("读取订单失败,请重试！");
            return ajaxJsonEntity;
        }
        User user = UserUtils.getUser();
        if (user == null || user.getId() == null || user.getId() <= 0) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("读取当前用户失败,请重新登录");
            return ajaxJsonEntity;
        }
        try {
            MDCustomerPraiseFee customerPraiseFee = customerPraiseFeeService.getByCustomerIdFromCacheNewForCP(order.getOrderCondition().getCustomerId());
            if(customerPraiseFee==null){
                ajaxJsonEntity.setSuccess(false);
                ajaxJsonEntity.setMessage("读取好评配置信息失败,请重试");
                return ajaxJsonEntity;
            }
            if(customerPraiseFee.getPraiseFeeFlag()==0){ //如果客户好评配置停用,则好无好评费用
                praise.setApplyCustomerPraiseFee(0.0);
                praise.setApplyServicepointPraiseFee(0.0);
            }else {
                NameValuePair<Double,Double> praiseFee = OrderPraiseService.getPraiseFee(praise,customerPraiseFee);
                if(!praiseFee.getName().equals(praise.getApplyCustomerPraiseFee())){
                    ajaxJsonEntity.setSuccess(false);
                    ajaxJsonEntity.setMessage("申请厂商好评费不准确!请确认");
                    return ajaxJsonEntity;
                }
                if(!praiseFee.getValue().equals(praise.getApplyServicepointPraiseFee())){
                    ajaxJsonEntity.setSuccess(false);
                    ajaxJsonEntity.setMessage("申请网点好评费不准确!请确认");
                    return ajaxJsonEntity;
                }
            }
            if (praise.getId() != null && praise.getId() > 0) {
                servicePointPraiseService.updatePraise(praise, user, PraiseCreatorTypeEnum.SERVICE_POINT.code);
            } else {
                servicePointPraiseService.saveApplyPraise(praise, order, user,PraiseCreatorTypeEnum.SERVICE_POINT.code);
            }
        } catch (Exception e) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage(e.getMessage());
        }
        return ajaxJsonEntity;
    }
}
