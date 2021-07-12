/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.wolfking.jeesite.modules.md.web;

import com.google.common.collect.Lists;
import com.kkl.kklplus.entity.md.MDCustomer;
import com.kkl.kklplus.entity.md.MDDepositLevel;
import com.kkl.kklplus.entity.md.MDServicePoint;
import com.kkl.kklplus.entity.md.dto.MDServicePointDto;
import com.kkl.kklplus.entity.md.dto.MDServicePointTimeLinessSummaryDto;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.config.Global;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.IntegerRange;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.GsonUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.*;
import com.wolfking.jeesite.modules.md.entity.viewModel.ServicePointModel;
import com.wolfking.jeesite.modules.md.entity.viewModel.ServicePointPlanRemarkModel;
import com.wolfking.jeesite.modules.md.service.ProductCategoryService;
import com.wolfking.jeesite.modules.md.service.ServicePointFinanceService;
import com.wolfking.jeesite.modules.md.service.ServicePointService;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.service.SystemService;
import com.wolfking.jeesite.modules.sys.utils.AreaUtils;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providermd.service.MSDepositLevelService;
import com.wolfking.jeesite.ms.providermd.service.MSProductService;
import com.wolfking.jeesite.ms.providermd.service.MSServicePointService;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresUser;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 安维网点Controller
 *
 * @author Ryan Lu
 * @version 2017-04-26
 */
@Slf4j
@Controller
@RequestMapping(value = "${adminPath}/md/servicepoint")
public class ServicePointController extends BaseController {

    @Autowired
    private ServicePointService servicePointService;

    @Autowired
    private AreaService areaService;

    @Value("${SyncServicePoint2ES}")
    private boolean syncServicePoint2ES;

    @Autowired
    private ServicePointFinanceService servicePointFinanceService;

    @Autowired
    private MSServicePointService msServicePointService;

    @Autowired
    private MSProductService msProductService;

    @Autowired
    private ProductCategoryService productCategoryService;

    @Autowired
    private SystemService systemService;

    @Autowired
    private MSDepositLevelService msDepositLevelService;

    /**
     * 安维网点选择列表
     */
    @RequiresUser
    @RequestMapping(value = "select")
    public String select(ServicePointModel servicePointModel, HttpServletRequest request, HttpServletResponse response, Model model) {
        if (servicePointModel.getArea() != null && servicePointModel.getArea().getId() != null && StringUtils.isBlank(servicePointModel.getArea().getName())) {
            Area area = areaService.getFromCache(servicePointModel.getArea().getId());
            servicePointModel.getArea().setName(area.getFullName());
        }
        ServicePoint servicePoint = new ServicePoint();
        BeanUtils.copyProperties(servicePointModel, servicePoint);
        servicePoint.setInvoiceFlag(-1);
        if (servicePointModel.getSearchType() == 0) {
            servicePoint.setName(servicePointModel.getName());
            servicePoint.setServicePointNo("");
        } else {
            servicePoint.setServicePointNo(servicePointModel.getName());
            servicePoint.setName("");
        }
        if(servicePoint.getFinance() != null){
            servicePoint.getFinance().setInvoiceFlag(-1);
            servicePoint.getFinance().setDiscountFlag(-1);
        }
		/* 只查询level 1 ~ 5的
		if(servicePointModel.getLevel()==null || StringUtils.isBlank(servicePointModel.getLevel().getValue())
				|| servicePointModel.getLevel().getValue() == "0"){
			IntegerRange levelRange = new IntegerRange(1,5);
			servicePoint.setLevelRange(levelRange);
		}*/
        Page<ServicePoint> page = new Page<>(request, response);
        page.setPageSize(8);
        servicePoint.setOrderBy("s.order_count desc,s.servicepoint_no");//sort
        // add on 2019-6-12 begin
        servicePoint.setAutoPlanFlag(-1);    //自动派单
        servicePoint.setInsuranceFlag(-1);   //购买保险
        servicePoint.setTimeLinessFlag(-1);  //快可立补贴
        servicePoint.setCustomerTimeLinessFlag(-1);  //客户时效
        servicePoint.setUseDefaultPrice(-1); //结算标准
        // add on 2019-6-12 end
        page = servicePointService.findPage(page, servicePoint);
        model.addAttribute("page", page);
        model.addAttribute("servicePoint", servicePointModel);
        String hidePhone = request.getParameter("hidePhone");
        model.addAttribute("hidePhone", StringUtils.trimToEmpty(hidePhone));

        String dialogType = request.getParameter("dialogType");
        if (dialogType != null && dialogType.equalsIgnoreCase("layer")) {
            return "modules/md/servicePointSelectLayer";
        } else {
            return "modules/md/servicePointSelect";
        }
    }

//    /**
//     * 安维网点选择列表
//     */
//    @RequiresUser
//    //@RequestMapping(value = "selectForPlan_v1")
//    public String selectForPlan(ServicePointModel servicePointModel, HttpServletRequest request, HttpServletResponse response, Model model) {
//        //TODO: 此方法已没有地方调用,后面会废弃  //add on 2019-12-30
//        if (servicePointModel.getArea() != null && servicePointModel.getArea().getId() != null && StringUtils.isBlank(servicePointModel.getArea().getName())) {
//            Area area = areaService.getFromCache(servicePointModel.getArea().getId());
//            servicePointModel.getArea().setName(area.getFullName());
//        }
//        // add on 2019-6-20 begin
//        if (servicePointModel.getSubArea() != null && servicePointModel.getSubArea().getId() != null && StringUtils.isBlank(servicePointModel.getSubArea().getName())) {
//            if (servicePointModel.getSubArea().getId().intValue() > 3) {
//                // 只有大于3的区域id才有真正意义
//                Area area = areaService.getFromCache(servicePointModel.getSubArea().getId());
//                servicePointModel.getSubArea().setName(area.getFullName());
//            } else {
//                servicePointModel.setSubArea(null);
//            }
//        }
//        // add on 2019-6-20 end
//        ServicePoint servicePoint = new ServicePoint();
//        BeanUtils.copyProperties(servicePointModel, servicePoint);
//        if (servicePointModel.getSearchType() == 0) {
//            servicePoint.setName(servicePointModel.getName());
//            servicePoint.setServicePointNo("");
//        } else {
//            servicePoint.setServicePointNo(servicePointModel.getName());
//            servicePoint.setName("");
//        }
//        // 只查询level 1 ~ 5的
////        if (servicePointModel.getLevel() == null || StringUtils.isBlank(servicePointModel.getLevel().getValue())
////                || servicePointModel.getLevel().getValue().equalsIgnoreCase("0")) {
////            IntegerRange levelRange = new IntegerRange(1, 5);
////            servicePoint.setLevelRange(levelRange);
////        }
//        Page<ServicePoint> page = new Page<>(request, response);
//        page.setPageSize(8);
//        servicePoint.setOrderBy("s.level desc,s.order_count desc,s.servicepoint_no");//sort
//        IntegerRange levelRange = new IntegerRange(1, 5);
//        servicePoint.setLevelRange(levelRange);//不显示停用的网点
//        servicePoint.setStatus(ServicePointStatus.createDict(ServicePointStatus.NORMAL));
//        page = servicePointService.findServicePointListForPlan(page, servicePoint);
//        model.addAttribute("page", page);
//        model.addAttribute("servicePoint", servicePointModel);
//        return "modules/md/servicePointSelectForPlan";
//    }


    /**
     * 按区县/街道/品类 分页查询可派单列表
     * 只查询level 1 ~ 5的,且status=10
     */
    @RequiresUser
    @RequestMapping(value = "selectForPlan")
    public String selectForPlanNew(ServicePointModel servicePointModel, HttpServletRequest request, HttpServletResponse response, Model model) {
        String viewForm = "modules/md/servicePointSelectForPlan";
        Page<ServicePoint> page = new Page<>(request, response);
        page.setPageSize(8);
        if(servicePointModel.getArea() == null || servicePointModel.getArea().getId() == null || servicePointModel.getArea().getId() <= 0){
            return selectServicePointResult("区域参数无值",servicePointModel, model, viewForm, page);
        }
        //if (servicePointModel.getArea() != null && servicePointModel.getArea().getId() != null && StringUtils.isBlank(servicePointModel.getArea().getName())) {
        Area area = areaService.getFromCache(servicePointModel.getArea().getId(),Area.TYPE_VALUE_COUNTY);
        if(area == null){
            return selectServicePointResult("读取区域无返回内容，请重试",servicePointModel, model, viewForm, page);
        }
        servicePointModel.setArea(area);
        Area city = areaService.getFromCache(area.getParentId(),Area.TYPE_VALUE_CITY);
        if(city == null){
            return selectServicePointResult("读取市无返回内容，请重试",servicePointModel, model, viewForm, page);
        }
        servicePointModel.setCity(city);
        //}
        //街道
        if (servicePointModel.getSubArea() != null && servicePointModel.getSubArea().getId() != null && servicePointModel.getSubArea().getId().intValue() > 3) {
            // 只有大于3的区域id才有真正意义
            area = areaService.getTownFromCache(servicePointModel.getArea().getId(),servicePointModel.getSubArea().getId());
            if(area == null){
                return selectServicePointResult("读取街道无返回内容，请重试",servicePointModel, model, viewForm, page);
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
        /*if (servicePointModel.getSearchType() == 0) {
            servicePoint.setName(servicePointModel.getName());
            servicePoint.setServicePointNo("");
        } else {
            servicePoint.setServicePointNo(servicePointModel.getName());
            servicePoint.setName("");
        }*/
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
        return selectServicePointResult(org.apache.commons.lang3.StringUtils.EMPTY,servicePointModel, model, "modules/md/servicePointSelectForPlan", page);
    }

    /**
     * 派单失败
     * @param servicePointModel
     * @param model
     * @param viewForm
     * @param page
     * @return
     */
    private String selectServicePointResult(String msg, ServicePointModel servicePointModel, Model model, String viewForm, Page<ServicePoint> page) {
        if(StringUtils.isNotBlank(msg)) {
            addMessage(model, msg);
        }
        model.addAttribute("page", page);
        model.addAttribute("servicePoint", servicePointModel);
        return viewForm;
    }

    @RequestMapping(value = "selectForStation")
    public String selectForStation(ServicePoint servicePoint, HttpServletRequest request, HttpServletResponse response, Model model){
        if(StringUtils.isBlank(servicePoint.getServicePointNo()) && StringUtils.isBlank(servicePoint.getName()) && StringUtils.isBlank(servicePoint.getContactInfo1())) {
            addMessage(model, "请至少输入编号，名称，电话中的一项");
            model.addAttribute("page", new Page<>());
            model.addAttribute("servicePoint", servicePoint);
            return "modules/md/servicePointSelectForStation";
        }
        servicePoint.setOrderBy("s.auto_plan_flag desc");
        servicePoint.setServicePointNo(servicePoint.getServicePointNo().trim());//去除空格
        servicePoint.setName(servicePoint.getName().trim());
        if (servicePoint.getFirstSearch() == 1) {
            servicePoint.setAutoPlanFlag(-1);  //是否自动派单,为-1时查询条件被忽略
            servicePoint.setFirstSearch(0);
        }
        if (!ObjectUtils.isEmpty(servicePoint.getFinance())) {
            servicePoint.getFinance().setDiscountFlag(-1);  // 是否折扣,为-1时查询条件被忽略
            servicePoint.getFinance().setInvoiceFlag(-1);
        }

        servicePoint.setInsuranceFlag(-1);   //购买保险
        servicePoint.setTimeLinessFlag(-1);  //快可立补贴
        servicePoint.setUseDefaultPrice(-1); //结算标准
        servicePoint.setCustomerTimeLinessFlag(-1);  //客户时效
        servicePoint.setAutoPlanFlag(-1);  // 自动派单  add on 2020-8-21

        Page<ServicePoint> page = servicePointService.findServicePointListForStation(new Page<>(request, response), servicePoint);
        model.addAttribute("page", page);
        model.addAttribute("servicePoint", servicePoint);
        return "modules/md/servicePointSelectForStation";
    }

    /**
     * 安维网点列表
     */
    //@RequiresPermissions("md:servicepoint:view")
    @RequestMapping(value = {"list", ""})
    public String list(ServicePoint servicePoint, HttpServletRequest request, HttpServletResponse response, Model model) {
        if (!SecurityUtils.getSubject().isPermitted("md:servicepoint:view")) {
            addMessage(model, "未开通浏览权限");
            model.addAttribute("page", new Page<>());
            model.addAttribute("servicePoint", servicePoint);
            return "modules/md/servicePointList";
        }
        servicePoint.setOrderBy("");
        //去除空格
        servicePoint.setServicePointNo(StringUtils.trimToEmpty(servicePoint.getServicePointNo()));
        servicePoint.setName(StringUtils.trimToEmpty(servicePoint.getName()));
        servicePoint.setContactInfo1(StringUtils.trimToEmpty(servicePoint.getContactInfo1()));
        servicePoint.setDeveloper(StringUtils.trimToEmpty(servicePoint.getDeveloper()));
        if (servicePoint.getFinance() != null && StringUtils.isNoneBlank(servicePoint.getFinance().getBankNo())) {
            servicePoint.getFinance().setBankNo(StringUtils.trimToEmpty(servicePoint.getFinance().getBankNo()));
        }
        if (servicePoint.getFirstSearch() == 1) {
            Engineer primary = new Engineer();
            primary.setAppFlag(-1);
            servicePoint.setPrimary(primary);
            servicePoint.setFirstSearch(0);
            if (servicePoint.getFinance() != null) {
                servicePoint.setAutoCompleteOrder(-1);

                servicePoint.getFinance().setInvoiceFlag(-1);
                servicePoint.getFinance().setDiscountFlag(-1);//all
            }
        }
        if(StringUtils.isBlank(servicePoint.getServicePointNo()) && StringUtils.isBlank(servicePoint.getName()) && StringUtils.isBlank(servicePoint.getContactInfo1())
                && StringUtils.isBlank(servicePoint.getFinance().getBankNo()) && (servicePoint.getFinance().getBankIssue() == null || StringUtils.isBlank(servicePoint.getFinance().getBankIssue().getValue()))) {
            addMessage(model, "请至少输入编号，名称，电话，账号及支付失&nbsp败原因中的一项");
            model.addAttribute("page", new Page<>());
            model.addAttribute("servicePoint", servicePoint);
            return "modules/md/servicePointList";
        }
        //if(request.getMethod().equalsIgnoreCase("get") && servicePoint.getFinance() != null){
        //    servicePoint.getFinance().setDiscountFlag(-1);//all
        //}
        // add on 2019-6-12 begin
        servicePoint.setAutoPlanFlag(-1);    //自动派单
        servicePoint.setInsuranceFlag(-1);   //购买保险
        servicePoint.setTimeLinessFlag(-1);  //快可立补贴
        servicePoint.setUseDefaultPrice(-1); //结算标准
        servicePoint.setCustomerTimeLinessFlag(-1);  //客户时效
        // add on 2019-6-12 end
        Page<ServicePoint> page = servicePointService.findPage(new Page<>(request, response), servicePoint);
        model.addAttribute("page", page);
        model.addAttribute("servicePoint", servicePoint);
        return "modules/md/servicePointList";
    }

    @RequiresPermissions("md:servicepoint:view")
    @RequestMapping(value = {"psList"})
    public String permissionSettingList(ServicePoint servicePoint, HttpServletRequest request, HttpServletResponse response, Model model) {
        // 权限设定页面;用于查询网点信息

        servicePoint.setOrderBy("");
        //去除空格
        servicePoint.setServicePointNo(servicePoint.getServicePointNo().trim());
        servicePoint.setName(servicePoint.getName().trim());
        servicePoint.setContactInfo1(servicePoint.getContactInfo1().trim());
        servicePoint.setDeveloper(servicePoint.getDeveloper().trim());
        if (servicePoint.getFinance() != null && StringUtils.isNoneBlank(servicePoint.getFinance().getBankNo())) {
            servicePoint.getFinance().setBankNo(servicePoint.getFinance().getBankNo().trim());
        }
        if (servicePoint.getFirstSearch() == 1) {
            Engineer primary = new Engineer();
            primary.setAppFlag(-1);
            servicePoint.setPrimary(primary);
            servicePoint.setFirstSearch(0);
            if (servicePoint.getFinance() != null) {
                servicePoint.setAutoCompleteOrder(-1);

                servicePoint.getFinance().setInvoiceFlag(-1);
                servicePoint.getFinance().setDiscountFlag(-1);  //all
            }
        }
        if(StringUtils.isBlank(servicePoint.getServicePointNo()) && StringUtils.isBlank(servicePoint.getName()) && StringUtils.isBlank(servicePoint.getContactInfo1())
                && StringUtils.isBlank(servicePoint.getFinance().getBankNo()) && (servicePoint.getFinance().getBankIssue() == null || StringUtils.isBlank(servicePoint.getFinance().getBankIssue().getValue()))) {
            addMessage(model, "请至少输入编号，名称，电话，账号及支付失&nbsp败原因中的一项");
            model.addAttribute("page", new Page<>());
            model.addAttribute("servicePoint", servicePoint);
            return "modules/md/servicePointPermissionSettingList";
        }

        servicePoint.setInsuranceFlag(-1);   //购买保险
        servicePoint.setTimeLinessFlag(-1);  //快可立补贴
        servicePoint.setAutoPlanFlag(-1);    //自动派单
        servicePoint.setUseDefaultPrice(-1); //结算标准
        servicePoint.setCustomerTimeLinessFlag(-1);  //客户时效

        Page<ServicePoint> page = servicePointService.findPage(new Page<>(request, response), servicePoint);
        model.addAttribute("page", page);
        model.addAttribute("servicePoint", servicePoint);
        return "modules/md/servicePointPermissionSettingList";

    }

    /**
     * 安维网点停用列表
     */
    //@RequiresPermissions("md:servicepoint:view")
    @RequestMapping(value = "disableList")
    public String disableList(ServicePoint servicePoint, HttpServletRequest request, HttpServletResponse response, Model model) {

        if (!SecurityUtils.getSubject().isPermitted("md:servicepoint:view")) {
            addMessage(model, "未开通浏览权限");
            model.addAttribute("page", new Page<MDCustomer>());
            model.addAttribute("servicePoint", servicePoint);
            return "modules/md/servicePointDisableList";
        }
        servicePoint.setOrderBy("");
        //去除空格
        servicePoint.setServicePointNo(StringUtils.trimToEmpty(servicePoint.getServicePointNo()));
        servicePoint.setName(StringUtils.trimToEmpty(servicePoint.getName()));
        servicePoint.setContactInfo1(StringUtils.trimToEmpty(servicePoint.getContactInfo1()));
        servicePoint.setDeveloper(StringUtils.trimToEmpty(servicePoint.getDeveloper()));
        if (servicePoint.getFinance() != null && StringUtils.isNoneBlank(servicePoint.getFinance().getBankNo())) {
            servicePoint.getFinance().setBankNo(servicePoint.getFinance().getBankNo().trim());
        }
        if (servicePoint.getFirstSearch() == 1) {
            Engineer primary = new Engineer();
            primary.setAppFlag(-1);
            servicePoint.setPrimary(primary);
            servicePoint.setFirstSearch(0);
            if (servicePoint.getFinance() != null) {
                servicePoint.setAutoCompleteOrder(-1);

                servicePoint.getFinance().setInvoiceFlag(-1);
                servicePoint.getFinance().setDiscountFlag(-1);//all
            }
            servicePoint.setInsuranceFlag(-1);   //购买保险
            servicePoint.setTimeLinessFlag(-1);  //快可立补贴
            servicePoint.setCustomerTimeLinessFlag(-1);  //客户时效
            servicePoint.setAutoPlanFlag(-1);    //自动派单
            servicePoint.setUseDefaultPrice(-1); //结算标准
        } else {
            if (servicePoint.getFinance() != null) {
                if (servicePoint.getFinance().getInvoiceFlag() != 1) {
                    servicePoint.getFinance().setInvoiceFlag(-1);
                }
                servicePoint.getFinance().setDiscountFlag(-1);
            }
        }
        if(StringUtils.isBlank(servicePoint.getServicePointNo()) && StringUtils.isBlank(servicePoint.getName()) && StringUtils.isBlank(servicePoint.getContactInfo1())
                && StringUtils.isBlank(servicePoint.getFinance().getBankNo()) && (servicePoint.getFinance().getBankIssue() == null || StringUtils.isBlank(servicePoint.getFinance().getBankIssue().getValue()))) {
            addMessage(model, "请选择网点名称，网点编号，网点电话，账号，支付异常中至少一项进行查询");
            model.addAttribute("page", new Page<>());
            model.addAttribute("servicePoint", servicePoint);
            return "modules/md/servicePointDisableList";
        }
        //查找停用列表
//        servicePoint.setLevel(new Dict("8", "停用"));
        //TODO: md_servicepoint-status -> 增加网点状态筛选(status)
        servicePoint.setStatus(ServicePointStatus.createDict(ServicePointStatus.PAUSED));
        // add on 2019-6-12 begin
        servicePoint.setAutoPlanFlag(-1);    //自动派单
        servicePoint.setInsuranceFlag(-1);   //购买保险
        servicePoint.setTimeLinessFlag(-1);  //快可立补贴
        servicePoint.setCustomerTimeLinessFlag(-1);  //客户时效
        servicePoint.setUseDefaultPrice(-1); //结算标准
        // add on 2019-6-12 end

        Page<ServicePoint> page = servicePointService.findPage(new Page<>(request, response), servicePoint);
        model.addAttribute("page", page);
        model.addAttribute("servicePoint", servicePoint);
        return "modules/md/servicePointDisableList";
    }

    /**
     * 待审核安维网点列表(del_flag = 2)
     */
    @RequiresPermissions("md:servicepoint:edit")
    @RequestMapping(value = "approvelist")//,method = RequestMethod.GET
    public String approveList(ServicePoint servicePoint, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<ServicePoint> page = servicePointService.findApprovePage(new Page<ServicePoint>(request, response), servicePoint);
        model.addAttribute("page", page);
        model.addAttribute("servicePoint", servicePoint);
        return "modules/md/servicePointApproveList";
    }

    /**
     * 审核安维网点 (ajax)
     *
     * @param ids
     * @param response
     * @return
     */
    @ResponseBody
    @RequiresPermissions("md:servicepoint:edit")
    @RequestMapping(value = "approve", method = RequestMethod.POST)
    public AjaxJsonEntity approve(@RequestParam String ids, HttpServletResponse response) {
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        User user = UserUtils.getUser();
        if (user == null || user.getId() == null) {
            result.setSuccess(false);
            result.setMessage("登录超时，请重新登录。");
            return result;
        }
        if (StringUtils.isBlank(ids)) {
            result.setSuccess(false);
            result.setMessage("请选择待审核的网点");
            return result;
        }
        List<String> lstids;
        List<Long> lids;
        try {
            //字符转字符List
            lstids = Arrays.asList(ids.split(","));
            //List<String> -> List<Long>
            lids = lstids.stream().map(t -> Long.valueOf(t)).collect(Collectors.toList());
            servicePointService.approve(lids, user.getId());
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage(e.getMessage().toString());
        }
        lstids = null;
        lids = null;

        return result;
    }

    /**
     * 安维网点审核列表(网点以前付过款)
     *
     * @param paramMap
     * @param request
     * @param response
     * @param model
     * @return
     */
    @RequestMapping(value = {"approveinvoiced"})
    public String approveinvoiced(@RequestParam Map<String, Object> paramMap, HttpServletRequest request,
                                  HttpServletResponse response, Model model) {
        //Page<User> page = engineerService.findApprovInvoicedUser(new Page<User>(request,response),paramMap);
        Page<User> page = new Page<User>();
        model.addAttribute("page", page);
        model.addAllAttributes(paramMap);
        return "modules/md/engineerApproveInvoicedList";
    }

    /**
     * 修改网点
     */
    @RequiresPermissions("md:servicepoint:edit")
    @RequestMapping(value = "form")
    public String form(ServicePoint servicePoint, Model model) {
        User user = UserUtils.getUser();
        if (servicePoint.getId() != null && servicePoint.getId() != 0) {
            servicePoint = servicePointService.get(servicePoint.getId());
            //primary
//			Engineer primary = servicePointService.getEngineer(servicePoint.getPrimary().getId());
//			servicePoint.setPrimary(primary);
            //area
            List<Integer> areas = servicePointService.getAreaIds(servicePoint.getId());
            servicePoint.setAreaIds(areas.stream().map(t -> t.toString()).collect(Collectors.joining(",")));
            //product
            List<Integer> products = servicePointService.getProductIds(servicePoint.getId());
            servicePoint.setProductIds(products.stream().map(t -> t.toString()).collect(Collectors.joining(",")));
        } else {
            servicePoint.setDeveloper(user.getName());
            servicePoint.setResetPrice(1);//可重置价格
            servicePoint.setSignFlag(0);//签约，否
            String str = MSDictUtils.getDictSingleValue("AppAcceptFlag", "0");//切换为微服务
            int appAcceptFlag = Integer.parseInt(str);

            //level
            String label = MSDictUtils.getDictLabel("1", "ServicePointLevel", "一星");//切换为微服务
            servicePoint.setLevel(new Dict("1", label));
            if (servicePoint.getPrimary() == null) {
                Engineer primary = new Engineer();
                primary.setLevel(new Dict("1", label));
                primary.setAppFlag(appAcceptFlag);//默认手机是否可以接单
                servicePoint.setPrimary(primary);
            } else {
                servicePoint.getPrimary().setLevel(new Dict("1", label));
                servicePoint.getPrimary().setAppFlag(appAcceptFlag);//默认手机是否可以接单
            }
            servicePoint.setStatus(ServicePointStatus.createDict(ServicePointStatus.NORMAL));

            servicePoint.setInsuranceFlag(ServicePoint.INSURANCE_FLAG_ENABLED);
            /*
            // mark on 2019-10-5
            servicePoint.setTimeLinessFlag(ServicePoint.TIME_LINESS_FLAG_ENABLED);
            */
        }
        model.addAttribute("servicePoint", servicePoint);
        List<Area> areaList = areaService.findAll(2);
        areaList.add(0, new Area(1l, "区域列表", 1));
        model.addAttribute("areaList", areaList);
        return "modules/md/servicePointForm";
    }


    /**
     * 修改网点
     */
    @RequiresPermissions("md:servicepoint:view")
    @RequestMapping(value = "newForm")
    public String newForm(ServicePoint servicePoint, Model model) {
        User user = UserUtils.getUser();
        if (servicePoint.getId() != null && servicePoint.getId() != 0) {
            /*
            // mark on 2020-2-25 begin
            // 针对厨电用户反应更新数据不能及时刷新问题，特注释如下代码，改变调用顺序，被注释的代码并没有逻辑问题。// 2020-2-25
            servicePoint = servicePointService.get(servicePoint.getId());
            //primary
//			Engineer primary = servicePointService.getEngineer(servicePoint.getPrimary().getId());
//			servicePoint.setPrimary(primary);
            //area
            List<Integer> areas = servicePointService.getAreaIds(servicePoint.getId());
            servicePoint.setAreaIds(areas.stream().map(t -> t.toString()).collect(Collectors.joining(",")));
            //product
            List<Integer> products = servicePointService.getProductIds(servicePoint.getId());
            servicePoint.setProductIds(products.stream().map(t -> t.toString()).collect(Collectors.joining(",")));
            // mark on 2020-2-25 end
             */

            //add on 2020-2-25 begin
            // 改变代码的调用顺序
            //area
            List<Integer> areas = servicePointService.getAreaIds(servicePoint.getId());
            //product
            List<Integer> products = servicePointService.getProductIds(servicePoint.getId());
            servicePoint = servicePointService.getWithExtendPropertyFromMaster(servicePoint.getId());
            if (servicePoint != null) {
                servicePoint.setAreaIds(areas.stream().map(t -> t.toString()).collect(Collectors.joining(",")));
                servicePoint.setProductIds(products.stream().map(t -> t.toString()).collect(Collectors.joining(",")));
            }
            // add on 2020-2-25 end
            List<Long> categories = servicePointService.findCategoryListByServicePiontId(servicePoint.getId());
            String categoryIds = "";
            if (categories != null && !categories.isEmpty()) {
                categoryIds = categories.stream().map(Object::toString).collect(Collectors.joining(","));
            }
            model.addAttribute("productCategories", categoryIds);
        } else {
            servicePoint.setDeveloper(user.getName());
            servicePoint.setResetPrice(1);//可重置价格
            servicePoint.setSignFlag(0);//签约，否
            String str = MSDictUtils.getDictSingleValue("AppAcceptFlag", "0");//切换为微服务
            int appAcceptFlag = Integer.parseInt(str);

            //level
            String label = MSDictUtils.getDictLabel("1", "ServicePointLevel", "一星");//切换为微服务
            servicePoint.setLevel(new Dict("1", label));
            if (servicePoint.getPrimary() == null) {
                Engineer primary = new Engineer();
                primary.setLevel(new Dict("1", label));
                primary.setAppFlag(appAcceptFlag);//默认手机是否可以接单
                servicePoint.setPrimary(primary);
            } else {
                servicePoint.getPrimary().setLevel(new Dict("1", label));
                servicePoint.getPrimary().setAppFlag(appAcceptFlag);//默认手机是否可以接单
            }
            servicePoint.setStatus(ServicePointStatus.createDict(ServicePointStatus.NORMAL));

            servicePoint.setInsuranceFlag(ServicePoint.INSURANCE_FLAG_ENABLED);
            /*
            // mark on 2019-10-5
            servicePoint.setTimeLinessFlag(ServicePoint.TIME_LINESS_FLAG_ENABLED);
            */
        }
        model.addAttribute("servicePoint", servicePoint);
        List<Area> areaList = areaService.findAll(2);
        areaList.add(0, new Area(1l, "区域列表", 1));
        model.addAttribute("areaList", areaList);
        return "modules/md/servicePointFormNewer";
    }


    /**
     * 权限设定中的修改网点
     */
    @RequiresPermissions("md:servicepoint:view")
    @RequestMapping(value = "psForm")
    public String permissionSettingForm(ServicePoint servicePoint, Model model) {
        User user = UserUtils.getUser();
        if (servicePoint.getId() != null && servicePoint.getId() != 0) {
            servicePoint = servicePointService.get(servicePoint.getId());
            //primary
//			Engineer primary = servicePointService.getEngineer(servicePoint.getPrimary().getId());
//			servicePoint.setPrimary(primary);
            //area
        }
        model.addAttribute("servicePoint", servicePoint);
        return "modules/md/servicePointPermissionSettingForm";
    }


    /**
     * 保存网点
     * 不需要审核
     */
    @RequiresPermissions("md:servicepoint:edit")
    @RequestMapping(value = "save")
    public String save(ServicePoint servicePoint, Model model, RedirectAttributes redirectAttributes) {
        User user = UserUtils.getUser();
        if (user == null || user.getId() == null) {
            addMessage(redirectAttributes, "错误：登录超时");
            return form(servicePoint, model);
        }
        if (servicePoint.getArea() != null && StringUtils.isNoneBlank(servicePoint.getArea().getFullName())) {
            servicePoint.setAddress(servicePoint.getArea().getFullName() + " " + servicePoint.getSubAddress());
        }
        if (!beanValidator(model, servicePoint)) {
            return form(servicePoint, model);
        }
        if(StringUtils.isBlank(servicePoint.getDeveloper())){
            addMessage(model,"请输入开发人员!");
            return form(servicePoint,model);
        }
        //servicePoint.setDelFlag(ServicePoint.DEL_FLAG_AUDIT);//待审核
        //try {
        servicePoint.setCreateBy(user);
        servicePoint.setCreateDate(new Date());
        //servicePointService.save(servicePoint);              // mark on 2020-5-20
        servicePointService.insertServicePoint(servicePoint);  // add on 2020-5-20
        addMessage(redirectAttributes, "保存网点[" + servicePoint.getName() + "]成功");
        return "redirect:" + adminPath + "/md/servicepoint/?repage";
        //}catch (Exception e){
        //	addMessage(model,e.getMessage());
        //	return form(servicePoint, model);
        //}

    }

    /**
     * 保存网点并通过审核
     */
    @RequiresPermissions("md:servicepoint:edit")
    @RequestMapping(value = "saveAndApprove")
    public String saveAndApprove(ServicePoint servicePoint, Model model, RedirectAttributes redirectAttributes) {
        User user = UserUtils.getUser();
        if (user == null || user.getId() == null) {
            addMessage(redirectAttributes, "错误：登录超时");
            return form(servicePoint, model);
        }
        if (!beanValidator(model, servicePoint)) {
            return form(servicePoint, model);
        }
        servicePoint.setCreateBy(user);
        servicePoint.setCreateDate(new Date());
        servicePoint.setDelFlag(ServicePoint.DEL_FLAG_NORMAL);//正常
        servicePointService.save(servicePoint);
        addMessage(redirectAttributes, "保存并审核网点'" + servicePoint.getName() + "'成功");
        return "redirect:" + adminPath + "/md/servicepoint/?repage";
    }

    /**
     * 保存网点重要信息
     */
    @RequiresPermissions("md:servicepoint:edit")
    @RequestMapping(value = "psSave")
    public String permisionSettingSave(ServicePoint servicePoint, Model model, RedirectAttributes redirectAttributes) {
        User user = UserUtils.getUser();
        if (user == null || user.getId() == null) {
            addMessage(redirectAttributes, "错误：登录超时");
            return permissionSettingForm(servicePoint, model);
        }
        if (!beanValidator(model, servicePoint)) {
            return permissionSettingForm(servicePoint, model);
        }
        servicePoint.setCreateBy(user);
        servicePoint.setCreateDate(new Date());
        servicePoint.setDelFlag(ServicePoint.DEL_FLAG_NORMAL);//正常
        servicePointService.permissionSettingSave(servicePoint);
        addMessage(redirectAttributes, "保存网点 '" + servicePoint.getName() + "' 设定成功");
        return "redirect:" + adminPath + "/md/servicepoint/psList?repage";
    }

    /**
     * 保存网点基本信息
     * 不需要审核
     */
    @RequiresPermissions("md:servicepoint:edit")
    @RequestMapping(value = "saveBaseInfo")
    public String saveBaseInfo(ServicePoint servicePoint, Model model, RedirectAttributes redirectAttributes) {
        User user = UserUtils.getUser();
        if (user == null || user.getId() == null) {
            addMessage(model, "错误：登录超时");
            return form(servicePoint, model);
        }
        if (servicePoint.getArea() != null && StringUtils.isNoneBlank(servicePoint.getArea().getFullName())) {
            servicePoint.setAddress(servicePoint.getArea().getFullName() + " " + servicePoint.getSubAddress());
        }
        if (!beanValidator(model, servicePoint)) {
            return form(servicePoint, model);
        }
        if(StringUtils.isBlank(servicePoint.getDeveloper())){
            addMessage(model,"请输入开发人员!");
            return newForm(servicePoint,model);
        }

        servicePoint.setCreateBy(user);
        servicePoint.setCreateDate(new Date());
        servicePointService.saveBaseInfo(servicePoint);
        addMessage(model, "保存网点[" + servicePoint.getName() + "]基本信息成功");

        return newForm(servicePoint, model);

    }

    /**
     * 保存网点并通过审核
     */
    @RequiresPermissions("md:servicepoint:edit")
    @RequestMapping(value = "saveBaseInfoAndApprove")
    public String saveBaseInfoAndApprove(ServicePoint servicePoint, Model model, RedirectAttributes redirectAttributes) {
        User user = UserUtils.getUser();
        if (user == null || user.getId() == null) {
            addMessage(redirectAttributes, "错误：登录超时");
            return newForm(servicePoint, model);
        }
        if (!beanValidator(model, servicePoint)) {
            return newForm(servicePoint, model);
        }
        servicePoint.setCreateBy(user);
        servicePoint.setCreateDate(new Date());
        servicePoint.setDelFlag(ServicePoint.DEL_FLAG_NORMAL);//正常
        servicePointService.saveBaseInfo(servicePoint);
        addMessage(model, "保存并审核网点'" + servicePoint.getName() + "'成功");
        //return "redirect:" + adminPath + "/md/servicepoint/?repage";

        return newForm(servicePoint, model);
    }

    /**
     * 保存网点基本信息
     * 不需要审核
     */
    @RequiresPermissions("md:servicepoint:edit")
    @RequestMapping(value = "saveProducts")
    public String saveProducts(ServicePoint servicePoint, Model model, RedirectAttributes redirectAttributes) {
        User user = UserUtils.getUser();
        if (user == null || user.getId() == null) {
            addMessage(model, "错误：登录超时");
            return newForm(servicePoint, model);
        }

        servicePoint.setCreateBy(user);
        servicePoint.setCreateDate(new Date());
        servicePointService.saveProducts(servicePoint);
        addMessage(model, "保存网点 [" + servicePoint.getName() + "] 产品成功");

        return newForm(servicePoint, model);
    }

    /**
     * 保存网点基本信息
     * 不需要审核
     */
    @RequiresPermissions("md:servicepoint:edit")
    @RequestMapping(value = "saveAreas")
    public String saveAreas(ServicePoint servicePoint, Model model, RedirectAttributes redirectAttributes) {
        User user = UserUtils.getUser();
        if (user == null || user.getId() == null) {
            addMessage(model, "错误：登录超时");
            return newForm(servicePoint, model);
        }

        servicePoint.setCreateBy(user);
        servicePoint.setCreateDate(new Date());
        servicePointService.saveAreas(servicePoint);
        addMessage(model, "保存网点 [" + servicePoint.getName() + "] 区域成功");

        return newForm(servicePoint, model);
    }


    /**
     * 删除
     */
    @RequiresPermissions("md:servicepoint:edit")
    @RequestMapping(value = "delete")
    public String delete(Long id, String type, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        User user = UserUtils.getUser();
        if (user == null || user.getId() == null) {
            addMessage(redirectAttributes, "错误：登录超时");
        } else {
            ServicePoint servicePoint = new ServicePoint(id);
            servicePoint.setCreateBy(user);//写网点操作日志需要user
            servicePointService.delete(servicePoint);
            addMessage(redirectAttributes, "删除网点成功");
        }
        if (StringUtils.equals(type, "listDelete")) {
            return "redirect:" + Global.getAdminPath()
                    + "/md/servicepoint/list?repage";
        } else if (StringUtils.equals(type, "approvelistDelete")) {
            return "redirect:" + Global.getAdminPath()
                    + "/md/servicepoint/approvelist?repage";
        } else {
            return "redirect:" + Global.getAdminPath() + "/md/servicepoint/list?repage";
        }
    }

    /**
     * 删除
     */
    @ResponseBody
    @RequiresPermissions("md:servicepoint:edit")
    @RequestMapping(value = "ajax/delete")
    public AjaxJsonEntity ajaxDelete(Long id, String type, HttpServletRequest request, HttpServletResponse response) {
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        User user = UserUtils.getUser();
        if (user == null || user.getId() == null) {
            result.setSuccess(false);
            result.setMessage("登录超时，请重新登录。");
            return result;
        }
        try {
            ServicePoint servicePoint = new ServicePoint(id);
            servicePoint.setCreateBy(user);   //写网点操作日志需要user
            servicePointService.delete(servicePoint);
            result.setMessage("网点已删除");
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage(e.getMessage());
        }
        return result;
    }

    /**
     * 检查网点编号是否重复
     */
    @ResponseBody
    @RequestMapping(value = "checkNo")
    public String checkNo(String id, String servicePointNo) {
        User user = UserUtils.getUser();
        if (user == null || user.getId() == null) {
            return "false";
        }
        if (StringUtils.isBlank(servicePointNo)) {
            return "true";
        }
        try {
            Long exceptId = 0l;
            if (StringUtils.isNotBlank(id)) {
                exceptId = Long.valueOf(id.trim());
            }
            String result = servicePointService.checkServicePointNo(exceptId, servicePointNo.trim());
            return result.equalsIgnoreCase("true") ? result : "安维网点编号已存在";
//			return servicePointService.checkServicePointNo(exceptId,servicePointNo.trim());
        } catch (Exception ex) {
            log.error("check service point no error,", ex);
            return "false";
        }
    }

    /**
     * 检查网点手机联系方式是否注册
     */
    @ResponseBody
    @RequestMapping(value = "checkContact")
    public String checkContact(Long id, String contactInfo1) {
        User user = UserUtils.getUser();
        if (user == null || user.getId() == null) {
            return "false";
        }
        if (StringUtils.isBlank(contactInfo1)) {
            return "true";
        }
        try {
            String result = servicePointService.checkServicePointContact(id, contactInfo1.trim());
            return result.equalsIgnoreCase("true") ? result : "手机号已被网点注册";
        } catch (Exception ex) {
            log.error("check service point mobile error,", ex);
            return "false";
        }
    }

    /**
     * 检查安维手机号是否已注册
     */
    @ResponseBody
    @RequestMapping(value = "checkEngineerMobile")
    public String checkEngineerMobile(Long id, HttpServletRequest request) {
        User user = UserUtils.getUser();
        if (user == null || user.getId() == null) {
            return "false";
        }
        String mobile = request.getParameter("contactInfo");
        if (StringUtils.isBlank(mobile)) {
            mobile = request.getParameter("primary.contactInfo");
        }
        //String engineerMobile = request.getParameter("primary.contactInfo");
        if (StringUtils.isBlank(mobile)) {
            return "true";
        }
        try {
            String result = servicePointService.checkEngineerMobile(id, mobile.trim());
            return result.equalsIgnoreCase("true") ? result : "手机号已被师傅注册";
        } catch (Exception ex) {
            log.error("check engineer mobile error,", ex);
            return "false";
        }
    }

    /**
     * 检查网点银行卡号是否已注册
     */
    @ResponseBody
    @RequestMapping(value = "checkBankNo")
    public String checkBankNo(Long id, HttpServletRequest request) {
        User user = UserUtils.getUser();
        if (user == null || user.getId() == null) {
            return "false";
        }
        String bankNo = request.getParameter("finance.bankNo");
        if (StringUtils.isBlank(bankNo)) {
            return "true";
        }
        try {
            String result = servicePointService.checkBankNo(id, bankNo.trim());
            return result.equalsIgnoreCase("true") ? result : "银行卡号已被使用";
        } catch (Exception ex) {
            log.error("check bank no error,", ex);
            return "false";
        }
    }

    /**
     * 派单更新安维的派单备注 (ajax)
     *
     * @param servicePointId
     * @param response
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/ajax/savePlanRemark", method = RequestMethod.POST)
    public AjaxJsonEntity savePlanRemark(@RequestParam String servicePointId, @RequestParam String planRemark, HttpServletResponse response) {
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        User user = UserUtils.getUser();
        if (user == null || user.getId() == null) {
            result.setSuccess(false);
            result.setMessage("登录超时，请重新登录。");
            return result;
        }
        if (StringUtils.isBlank(servicePointId)) {
            result.setSuccess(false);
            result.setMessage("网店信息错误!");
            return result;
        }
        if (StringUtils.isBlank(planRemark)) {
            result.setSuccess(false);
            result.setMessage("备注为空!");
            return result;
        }
        try {
            servicePointService.savePlanRemark(Long.valueOf(servicePointId), planRemark);
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage(e.getMessage().toString());
        }
        return result;
    }

    /**
     * 查看派单备注列表
     */
    @RequiresPermissions("md:servicepoint:view")
    @RequestMapping(value = "viewPlanRemarkList")
    public String viewPlanRemarkList(@RequestParam String servicePointId, @RequestParam String servicePointNo, @RequestParam String servicePointName, HttpServletRequest request, HttpServletResponse response, Model model) {
        List<ServicePointPlanRemarkModel> list = new ArrayList<>();
        try {
            list = servicePointService.getPlanRemarks(Long.valueOf(servicePointId));
        } catch (Exception e) {
            addMessage(model, "加载网店历史备注列表失败,请重试");
        }
        model.addAttribute("servicePointNo", servicePointNo);
        model.addAttribute("servicePointName", servicePointName);
        model.addAttribute("planRemarks", list);
        return "modules/sd/service/viewPlanRemarkList";
    }

    /**
     * 单独更新网点的备注 (ajax)
     *
     * @param servicePointId
     * @param response
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/ajax/updateRemark", method = RequestMethod.POST)
    public AjaxJsonEntity updateRemark(@RequestParam String servicePointId, @RequestParam String remarks, HttpServletResponse response) {
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        User user = UserUtils.getUser();
        if (user == null || user.getId() == null) {
            result.setSuccess(false);
            result.setMessage("登录超时，请重新登录。");
            return result;
        }
        if (StringUtils.isBlank(servicePointId)) {
            result.setSuccess(false);
            result.setMessage("网店信息错误!");
            return result;
        }
        if (StringUtils.isBlank(remarks)) {
            result.setSuccess(false);
            result.setMessage("备注为空!");
            return result;
        }
        try {
            servicePointService.updateRemark(Long.valueOf(servicePointId), remarks);
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage(e.getMessage().toString());
        }
        return result;
    }

    /**
     * 查看派单备注列表
     */
    @RequiresPermissions("md:servicepoint:view")
    @RequestMapping(value = "viewRemarkList")
    public String viewRemarkList(@RequestParam String servicePointId, @RequestParam String servicePointNo, @RequestParam String servicePointName, HttpServletRequest request, HttpServletResponse response, Model model) {
        List<ServicePointPlanRemarkModel> list = new ArrayList<>();
        try {
            list = servicePointService.getRemarksList(Long.valueOf(servicePointId));
        } catch (Exception e) {
            addMessage(model, "加载网店历史备注列表失败,请重试");
        }
        model.addAttribute("servicePointNo", servicePointNo);
        model.addAttribute("servicePointName", servicePointName);
        model.addAttribute("planRemarks", list);
        return "modules/sd/service/viewPlanRemarkList";
    }


    /**
     * Ajax更新是否自动派单
     * @param servicePointId  网点id
     * @param autoPlanFlag  是否自动派单,1-自动派单,0-人工派单
     * @param response
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/ajax/updateAutoPlanFlag", method = RequestMethod.POST)
    public AjaxJsonEntity updateAutoPlanFlag(@RequestParam String servicePointId, @RequestParam String autoPlanFlag, HttpServletResponse response) {
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        User user = UserUtils.getUser();
        if (user == null || user.getId() == null) {
            result.setSuccess(false);
            result.setMessage("登录超时，请重新登录。");
            return result;
        }
        if (StringUtils.isBlank(servicePointId)) {
            result.setSuccess(false);
            result.setMessage("网点信息错误!");
            return result;
        }
        if (StringUtils.isBlank(autoPlanFlag)) {
            result.setSuccess(false);
            result.setMessage("自动派单标志不能为空!");
            return result;
        }
        Integer iAutoPlanFlag = Integer.parseInt(autoPlanFlag);
        try {
            servicePointService.updateAutoPlanFlag(Long.valueOf(servicePointId), iAutoPlanFlag);
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage(e.getMessage().toString());
        }
        return result;
    }

    /**
     * Ajax更新网点等级
     * @param servicePointId  网点id
     * @param level  是否自动派单,1-自动派单,0-人工派单
     * @param response
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/ajax/updateLevel", method = RequestMethod.POST)
    public AjaxJsonEntity updateLevel(@RequestParam String servicePointId, @RequestParam String level, HttpServletResponse response) {
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        User user = UserUtils.getUser();
        if (user == null || user.getId() == null) {
            result.setSuccess(false);
            result.setMessage("登录超时，请重新登录。");
            return result;
        }
        if (StringUtils.isBlank(servicePointId)) {
            result.setSuccess(false);
            result.setMessage("网点信息错误!");
            return result;
        }
        if (StringUtils.isBlank(level)) {
            result.setSuccess(false);
            result.setMessage("网点等级不能为空!");
            return result;
        }
        Integer iLevel = Integer.parseInt(level);
        try {
            servicePointService.updateLevel(Long.valueOf(servicePointId), iLevel);
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage(e.getMessage().toString());
        }
        return result;
    }

    @RequiresPermissions("md:servicepoint:edit")
    @RequestMapping(value = "syncDataToEs")
    public String  syncDataToEs(String id,HttpServletRequest request,RedirectAttributes redirectAttributes) {
        User user = UserUtils.getUser();
        if (user == null || user.getId() == null) {
            addMessage(redirectAttributes, "错误：登录超时");
        } else {
            if (StringUtils.isBlank(id)) {
                addMessage(redirectAttributes, "网点信息错误");
            }
//            System.out.println(String.format("是否同步ElasticSearch:%s",syncServicePoint2ES));
            if (syncServicePoint2ES) {
                ServicePoint servicePoint = servicePointService.get(Long.valueOf(id));
                  //servicePointService.pushServicePointAndStationsToEs(servicePoint);    //mark on 2020-11-25 //不再往ES发送旧的ES消息

                msServicePointService.pushServicePointAndStationToES(Long.valueOf(id));  // add on 2020-8-21
            }
            addMessage(redirectAttributes, "同步网点成功");
        }
        return "redirect:" + Global.getAdminPath() + "/md/servicepoint/list?repage";
    }

    //@RequiresPermissions("md:servicepoint:edit")
    @ResponseBody
    @RequestMapping(value = "ajax/syncDataToEs")
    public AjaxJsonEntity  ajaxSyncDataToEs(String id,HttpServletRequest request,HttpServletResponse response) {
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        User user = UserUtils.getUser();
        if (user == null || user.getId() == null) {
            result.setSuccess(false);
            result.setMessage("登录超时，请重新登录。");
            return result;
        }
        if (StringUtils.isBlank(id)) {
            result.setSuccess(false);
            result.setMessage("网点信息错误。");
            return result;
        }
        try {
            if (syncServicePoint2ES) {
                ServicePoint servicePoint = servicePointService.get(Long.valueOf(id));
                //servicePointService.pushServicePointAndStationsToEs(servicePoint);     //mark on 2020-11-25 //不需要再同步旧的自动派单消息发送

                msServicePointService.pushServicePointAndStationToES(Long.valueOf(id));  // add on 2020-8-21
                result.setMessage("同步网点信息成功!");
            }
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage(e.getMessage());
        }
        return result;
    }

    @RequestMapping(value="addressRoute")
    public String addressRoute(String fromAddr, String toAddr, Model model) {
        try {
            String fromAddress =java.net.URLDecoder.decode(fromAddr,"UTF-8");  //进行解码，会抛异常，直接捕获即可。
            String[] areaArray = AreaUtils.getLocation(fromAddress);
            Double  centerLongtitude =0.0,
                    centerLatitude = 0.0;

            if (!ObjectUtils.isEmpty(areaArray) && areaArray.length == 2) {
                centerLongtitude = Double.valueOf(areaArray[0]);
                centerLatitude = Double.valueOf(areaArray[1]);
            }

            model.addAttribute("centerLng",centerLongtitude);
            model.addAttribute("centerLat",centerLatitude);

            String toAddress =java.net.URLDecoder.decode(toAddr,"UTF-8");  //进行解码，会抛异常，直接捕获即可。
            areaArray = AreaUtils.getLocation(toAddress);
            Double  toLongtitude =0.0,
                    toLatitude = 0.0;

            if (!ObjectUtils.isEmpty(areaArray) && areaArray.length == 2) {
                toLongtitude = Double.valueOf(areaArray[0]);
                toLatitude = Double.valueOf(areaArray[1]);
            }

            model.addAttribute("toLng", toLongtitude);
            model.addAttribute("toLat", toLatitude);
            model.addAttribute("fromAddress", fromAddress);
            model.addAttribute("toAddress", toAddress);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "modules/md/servicePointAddressRoute";
    }




    /**
     * 分页查询
     * @param servicePointDto
     * @return
     */
    @RequiresPermissions("md:servicepointtimeliness:view")
    @RequestMapping("findServicePointTimelinessList")
    public String findServicePointTimelinessList(MDServicePointDto servicePointDto, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<MDServicePointDto> servicePointDtoPage = new Page<>(request, response);
        if(servicePointDto.getAreaId()!=null){
            Area area = areaService.getFromCache(servicePointDto.getAreaId());
            if(area.getId()!=null && area.getType().equals(Area.TYPE_VALUE_CITY)){
                List<Area> areaList = areaService.findListByParent(Area.TYPE_VALUE_COUNTY,area.getId());
                if(areaList!=null && areaList.size()>0){
                    List<Long> areaIdList = areaList.stream().map(Area::getId).collect(Collectors.toList());
                    servicePointDto.setAreaIds(areaIdList);
                }
            }else if(area.getId()!=null && area.getType().equals(Area.TYPE_VALUE_COUNTY)){
                List<Long> areaIdList = Lists.newArrayList(servicePointDto.getAreaId());
                servicePointDto.setAreaIds(areaIdList);
            }
        }
        Page<MDServicePointDto> page= msServicePointService.findServicePointTimeliness(servicePointDtoPage,servicePointDto);
        model.addAttribute("servicePointDto",servicePointDto);
        model.addAttribute("page", page);
        return "modules/providermd/servicePointTimelinessList";
    }

    @RequiresPermissions("md:servicepointtimeliness:view")
    @RequestMapping("formServiceTimeliness")
    public String formServiceTimeliness(Long id,Model model){
        MDServicePointDto servicePointDto = msServicePointService.getServicePointTimeliness(id);
        model.addAttribute("canSave",true);
        if(servicePointDto==null){
            model.addAttribute("canSave",false);
            model.addAttribute("servicePointDto",new MDServicePointDto());
            model.addAttribute("message","获取网点信息失败");
        }else{
            model.addAttribute("servicePointDto",servicePointDto);
        }
        return "modules/providermd/servicePointTimelinessForm";
    }

    @RequiresPermissions("md:servicepointtimeliness:edit")
    @RequestMapping("updateTimeliness")
    @ResponseBody
    public AjaxJsonEntity updateTimeliness(MDServicePointDto servicePointDto){
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        User user = UserUtils.getUser();
        if(user ==null || user.getId()==null || user.getId()<=0){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("当前用户不存在,请重新登录");
            return ajaxJsonEntity;
        }
        try {
            servicePointDto.setUpdateById(user.getId());
            servicePointDto.setUpdateDate(new Date());
            msServicePointService.updateTimeliness(servicePointDto);
        }catch (Exception e){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage(e.getMessage());
        }
        return ajaxJsonEntity;
    }


    /**
     * 按省获取网点时效信息
     * @param servicePointTimeLinessSummaryDto
     * @return
     */
    @RequiresPermissions("md:servicepointtimeliness:view")
    @RequestMapping("servicePointAreaTimelinessList")
    public String servicePointAreaTimelinessList(MDServicePointTimeLinessSummaryDto servicePointTimeLinessSummaryDto,Model model,HttpServletRequest request){
        model.addAttribute("servicePointTimeLinessSummaryDto",servicePointTimeLinessSummaryDto);
       if(servicePointTimeLinessSummaryDto==null || servicePointTimeLinessSummaryDto.getAreaId()==null || servicePointTimeLinessSummaryDto.getAreaId()<=0){
         return "modules/providermd/servicePointAreaTimelinessList";
       }
        List<MDServicePointTimeLinessSummaryDto> list = msServicePointService.servicePointAreaTimelinessList(servicePointTimeLinessSummaryDto);
        model.addAttribute("list",list);
        return "modules/providermd/servicePointAreaTimelinessList";
    }



    /**
     * 关闭或者开启kkl时效
     * @param servicePointDto
     * @return
     */
    @RequiresPermissions("md:servicepointtimeliness:edit")
    @RequestMapping("saveTimeliness")
    @ResponseBody
    public AjaxJsonEntity saveTimeliness(MDServicePointDto servicePointDto){
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        if(servicePointDto.getAreaId()==null || servicePointDto.getAreaId()<=0){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("获取城市Id错误");
        }
        User user = UserUtils.getUser();
        if(user==null || user.getId()==null || user.getId()<=0){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("超时,请重新登录");
        }
        List<Area> countyList = areaService.findListByParent(Area.TYPE_VALUE_COUNTY,servicePointDto.getAreaId());
        List<Long> countIdList = countyList.stream().map(Area::getId).collect(Collectors.toList());
        servicePointDto.setAreaIds(countIdList);
        servicePointDto.setUpdateById(user.getId());
        servicePointDto.setUpdateDate(new Date());
        try {
            msServicePointService.updateTimelinessByArea(servicePointDto);
        }catch (Exception e){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage(e.getMessage());
        }
        return ajaxJsonEntity;
    }

    /**
     * 关闭或者开启网点客户时效
     * @param servicePointDto
     * @return
     */
    @RequiresPermissions("md:servicepointtimeliness:edit")
    @RequestMapping("saveCustomerTimeliness")
    @ResponseBody
    public AjaxJsonEntity saveCustomerTimeliness(MDServicePointDto servicePointDto){
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        if(servicePointDto.getAreaId()==null || servicePointDto.getAreaId()<=0){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("获取城市Id错误");
        }
        User user = UserUtils.getUser();
        if(user==null || user.getId()==null || user.getId()<=0){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("超时,请重新登录");
        }
        List<Area> countyList = areaService.findListByParent(Area.TYPE_VALUE_COUNTY,servicePointDto.getAreaId());
        List<Long> countIdList = countyList.stream().map(Area::getId).collect(Collectors.toList());
        servicePointDto.setAreaIds(countIdList);
        servicePointDto.setUpdateById(user.getId());
        servicePointDto.setUpdateDate(new Date());
        try {
            msServicePointService.updateCustomerTimelinessByArea(servicePointDto);
        }catch (Exception e){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage(e.getMessage());
        }
        return ajaxJsonEntity;
    }

    /**
     * 根据市获取网点编号和网点名称
     * @param servicePointDto
     * @return
     */
    @RequiresPermissions("md:servicepoint:view")
    @RequestMapping("findListByAreaIds")
    public String findListByAreaIds(MDServicePointDto servicePointDto,Model model){
        model.addAttribute("servicePointDto",servicePointDto);
        if(servicePointDto.getAreaId()==null || servicePointDto.getAreaId()==0){
            return "modules/md/servicePointNoList";
        }
        List<Area> countyList = areaService.findListByParent(Area.TYPE_VALUE_COUNTY,servicePointDto.getAreaId());
        List<Long> countyIds = countyList.stream().map(Area::getId).collect(Collectors.toList());
        List<MDServicePointDto> list = msServicePointService.findIdAndPointNoByAreaIds(countyIds);
        model.addAttribute("list",list);
        return "modules/md/servicePointNoList";
    }

    /**
     * 客服派单添加网点
     * @param servicePoint
     * @return
     */
    @RequestMapping("addServicePointForPlanForm")
    public String addServicePointForPlanForm(ServicePoint servicePoint,Model model,HttpServletRequest request){
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
        return "modules/md/servicePointForPlanFormAdd";
    }

    /**
     * 突击客服保存网点
     * @param servicePoint
     * @return
     */
    @RequestMapping("saveServicePointForPlan")
    @ResponseBody
    public AjaxJsonEntity saveServicePointForPlan(ServicePoint servicePoint,Model model){
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
     * 按区县/街道/品类 分页查询停用网点列表
     * 只查询level 1 ~ 5的,且status=20,30
     */
    @RequiresUser
    @RequestMapping(value = "unableSelectForPlan")
    public String disAbleSelectForPlan(ServicePointModel servicePointModel, HttpServletRequest request, HttpServletResponse response, Model model) {
        String viewForm = "modules/md/unableServicePointSelectForPlan";
        Page<ServicePoint> page = new Page<>(request, response);
        page.setPageSize(8);
        if(servicePointModel.getArea() == null || servicePointModel.getArea().getId() == null || servicePointModel.getArea().getId() <= 0){
            return selectServicePointResult("区域参数无值",servicePointModel, model, viewForm, page);
        }
        //if (servicePointModel.getArea() != null && servicePointModel.getArea().getId() != null && StringUtils.isBlank(servicePointModel.getArea().getName())) {
        Area area = areaService.getFromCache(servicePointModel.getArea().getId(),Area.TYPE_VALUE_COUNTY);
        if(area == null){
            return selectServicePointResult("读取区域无返回内容，请重试",servicePointModel, model, viewForm, page);
        }
        servicePointModel.setArea(area);
        Area city = areaService.getFromCache(area.getParentId(),Area.TYPE_VALUE_CITY);
        if(city == null){
            return selectServicePointResult("读取市无返回内容，请重试",servicePointModel, model, viewForm, page);
        }
        servicePointModel.setCity(city);
        //}
        //街道
        if (servicePointModel.getSubArea() != null && servicePointModel.getSubArea().getId() != null && servicePointModel.getSubArea().getId().intValue() > 3) {
            // 只有大于3的区域id才有真正意义
            area = areaService.getTownFromCache(servicePointModel.getArea().getId(),servicePointModel.getSubArea().getId());
            if(area == null){
                return selectServicePointResult("读取街道无返回内容，请重试",servicePointModel, model, viewForm, page);
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
        return selectServicePointResult(org.apache.commons.lang3.StringUtils.EMPTY,servicePointModel, model, viewForm, page);
    }


    /**
     * 突击客服恢复网点
     * @param servicePoint
     * @return
     */
    @RequestMapping("updateStatusForPlan")
    @ResponseBody
    public AjaxJsonEntity updateStatusForPlan(MDServicePoint servicePoint){
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
     * 服务网点选择列表
     */
    @RequiresUser
    @RequestMapping(value = "mdservicePointSelector")
    public String servicePointSelector(ServicePointModel servicePointModel, HttpServletRequest request, HttpServletResponse response, Model model) {
        ServicePoint servicePoint = new ServicePoint();
        BeanUtils.copyProperties(servicePointModel, servicePoint);
        servicePoint.setInvoiceFlag(-1);

        servicePoint.setName(servicePointModel.getName());
        servicePoint.setServicePointNo(servicePointModel.getServicePointNo());

        if (servicePoint.getFinance() != null) {
            servicePoint.getFinance().setInvoiceFlag(-1);
            servicePoint.getFinance().setDiscountFlag(-1);
        }
        if (StringUtils.isBlank(servicePoint.getName()) && StringUtils.isBlank(servicePoint.getServicePointNo()) && StringUtils.isBlank(servicePoint.getContactInfo1())) {
            addMessage(model, "请选择网点名称，网点编号，网点电话中至少一项进行查询");
            model.addAttribute("servicePoint", servicePointModel);
            return "modules/md/mdServicePointSelector";
        }

        Page<ServicePoint> page = new Page<>(request, response);
        page.setPageSize(8);
        servicePoint.setOrderBy("s.order_count desc,s.servicepoint_no");//sort
        servicePoint.setAutoPlanFlag(-1);    //自动派单
        servicePoint.setInsuranceFlag(-1);   //购买保险
        servicePoint.setTimeLinessFlag(-1);  //快可立补贴
        servicePoint.setCustomerTimeLinessFlag(-1);  //客户时效
        servicePoint.setUseDefaultPrice(-1); //结算标准
        page = servicePointService.findPage(page, servicePoint);
        model.addAttribute("page", page);
        model.addAttribute("servicePoint", servicePointModel);
        return "modules/md/mdServicePointSelector";
    }

    @RequiresPermissions("md:servicepoint:view")
    @RequestMapping("findUserListByContactInfo")
    public String findUserListByContactInfo(User user, String expectType, Model model){
        if (!StringUtils.isNotBlank(user.getMobile())) {
            return "modules/md/servicePointUserList";
        }
        List<User> users = systemService.findByMobile(user.getMobile().trim(),expectType,null);
        model.addAttribute("user", user);
        model.addAttribute("userList", users);
        return "modules/md/servicePointUserList";
    }

    @RequiresPermissions("md:servicepoint:edit")
    @ResponseBody
    @RequestMapping("reloadServicePointCacheById/{id}")
    public String reloadServicePointCacheById(@PathVariable Long id){
        if (id <= 0 ) {
            return "fail.";
        }
        int i =msServicePointService.reloadServicePointCacheById(id);
        return "success.count="+i;
    }

    @RequiresPermissions("md:servicepoint:edit")
    @ResponseBody
    @RequestMapping("getFromCache/{id}")
    public ServicePoint getFromCache(@PathVariable Long id){
        if (id <= 0 ) {
            return null;
        }
        return msServicePointService.getCacheById(id);
    }

    @RequiresPermissions("md:servicepoint:edit")
    @ResponseBody
    @RequestMapping(value="reloadFinanceToRedis")
    public String reloadFinanceToRedis() {
        Integer rowCount = servicePointFinanceService.reloadAllToCache();
        return String.format("roload servicePointFinance rowCount：%s", rowCount);
    }

    @RequiresPermissions("md:servicepoint:edit")
    @ResponseBody
    @RequestMapping("finance/getFromCache/{id}")
    public ServicePointFinance getFinanceFromCache(@PathVariable Long id){
        if (id <= 0 ) {
            return null;
        }
        return servicePointFinanceService.getFromCache(id);
    }

    //region 质保金

    /**
     * 查询并分页显示网点
     * 只查询有效网点，且有质保等级设定，不包含返现网点
     */
    @RequiresUser
    @RequestMapping(value = "selectForDeposit")
    public String selectForDeposit(ServicePointModel servicePointModel, HttpServletRequest request, HttpServletResponse response, Model model) {
        String viewForm = "modules/md/servicePointSelectForDeposit";
        Page<ServicePoint> page = new Page<>(request, response);
        page.setPageSize(12);
        ServicePoint servicePoint = new ServicePoint();
        BeanUtils.copyProperties(servicePointModel, servicePoint);
        page = servicePointService.findServicePointListForDeposit(page, servicePoint);
        return selectServicePointResult(StringUtils.EMPTY,servicePointModel, model, viewForm, page);
    }

    //endregion 质保金
}
