/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.wolfking.jeesite.ms.providermd.controller;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kkl.kklplus.entity.md.MDDepositLevel;
import com.kkl.kklplus.entity.md.MDEngineerCert;
import com.kkl.kklplus.entity.md.MDEngineerEnum;
import com.kkl.kklplus.entity.rpt.common.RPTSystemCodeEnum;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.GsonUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.Engineer;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.md.entity.ServicePointFinance;
import com.wolfking.jeesite.modules.md.entity.ServicePointStatus;
import com.wolfking.jeesite.modules.md.entity.viewModel.ServicePointPlanRemarkModel;
import com.wolfking.jeesite.modules.md.service.ProductCategoryService;
import com.wolfking.jeesite.modules.md.service.ServicePointFinanceService;
import com.wolfking.jeesite.modules.md.service.ServicePointService;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.service.SystemService;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providermd.service.MSDepositLevelService;
import com.wolfking.jeesite.ms.providermd.service.MSProductService;
import com.wolfking.jeesite.ms.providermd.service.MSServicePointService;
import com.wolfking.jeesite.ms.providermd.service.MSServicePointStationService;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
@RequestMapping(value = "${adminPath}/provider/md/servicePointNew")
public class ServicePointNewController extends BaseController {

    @Autowired
    private ServicePointService servicePointService;

    @Autowired
    private AreaService areaService;

    @Value("${SyncServicePoint2ES}")
    private boolean syncServicePoint2ES;

    @Value("${site.code}")
    private String siteCode;

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
    private MSServicePointStationService msServicePointStationService;

    @Autowired
    private MSDepositLevelService msDepositLevelService;

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
            return "modules/md/servicePointListNew";
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

        servicePoint.setAppFlag(-1);
        servicePoint.setFirstSearch(0);
        servicePoint.setDegree(0);
        servicePoint.setAutoCompleteOrder(-1);
        if (servicePoint.getFinance() == null) {
            servicePoint.setFinance(new ServicePointFinance());
        }
        servicePoint.getFinance().setInvoiceFlag(-1);
        servicePoint.getFinance().setDiscountFlag(-1);//all


        if (StringUtils.isBlank(servicePoint.getServicePointNo()) && StringUtils.isBlank(servicePoint.getName()) && StringUtils.isBlank(servicePoint.getContactInfo1())
                && StringUtils.isBlank(servicePoint.getFinance().getBankNo()) && (servicePoint.getFinance().getBankIssue() == null || StringUtils.isBlank(servicePoint.getFinance().getBankIssue().getValue()))) {
            addMessage(model, "请选择网点名称，网点编号，网点电话，账号，支付异常中至少一项进行查询");
            model.addAttribute("page", new Page<>());
            model.addAttribute("servicePoint", servicePoint);
            return "modules/md/servicePointListNew";
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
        return "modules/md/servicePointListNew";
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

        servicePoint.setAppFlag(-1);
        servicePoint.setFirstSearch(0);
        servicePoint.setSignFlag(-1);
        servicePoint.setAutoCompleteOrder(-1);
        if (servicePoint.getFinance() == null) {
            servicePoint.setFinance(new ServicePointFinance());
        }
        servicePoint.getFinance().setInvoiceFlag(-1);
        servicePoint.getFinance().setDiscountFlag(-1);//all


        if (StringUtils.isBlank(servicePoint.getServicePointNo()) && StringUtils.isBlank(servicePoint.getName()) && StringUtils.isBlank(servicePoint.getContactInfo1())
                && StringUtils.isBlank(servicePoint.getFinance().getBankNo()) && (servicePoint.getFinance().getBankIssue() == null || StringUtils.isBlank(servicePoint.getFinance().getBankIssue().getValue()))) {
            addMessage(model, "请选择网点名称，网点编号，网点电话，账号，支付异常中至少一项进行查询");
            model.addAttribute("page", new Page<>());
            model.addAttribute("servicePoint", servicePoint);
            return "modules/md/servicePointPermissionSettingListNew";
        }

        servicePoint.setInsuranceFlag(-1);   //购买保险
        servicePoint.setTimeLinessFlag(-1);  //快可立补贴
        servicePoint.setAutoPlanFlag(-1);    //自动派单
        servicePoint.setUseDefaultPrice(-1); //结算标准
        servicePoint.setCustomerTimeLinessFlag(-1);  //客户时效

        Page<ServicePoint> page = servicePointService.findPage(new Page<>(request, response), servicePoint);
        model.addAttribute("page", page);
        model.addAttribute("servicePoint", servicePoint);
        return "modules/md/servicePointPermissionSettingListNew";

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
            //area
            List<Integer> areas = servicePointService.getAreaIds(servicePoint.getId());
            servicePoint.setAreaIds(areas.stream().map(t -> t.toString()).collect(Collectors.joining(",")));
            //product
            List<Integer> products = servicePointService.getProductIds(servicePoint.getId());
            servicePoint.setProductIds(products.stream().map(t -> t.toString()).collect(Collectors.joining(",")));

            ServicePointFinance finance = servicePoint.getFinance();
            if (servicePoint.getDiscountFlag() != null &&  servicePoint.getDiscountFlag() == 1 && finance != null ) {
                double discount = finance.getDiscount();
                if (discount >0) {
                    discount = (int)(discount*100);
                    finance.setDiscount(discount);
                }
            }

        } else {
            servicePoint.setDeveloper(user.getName());
            servicePoint.setResetPrice(1);//可重置价格
            servicePoint.setSignFlag(0);//签约，否
            servicePoint.setCapacity(1);
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
            servicePoint.setRemotePriceFlag(0);
            servicePoint.setRemotePriceEnabledFlag(0);

            //空调打开快可立时效
            if (org.apache.commons.lang.StringUtils.endsWithIgnoreCase(siteCode, RPTSystemCodeEnum.AC.code)) {
                servicePoint.setTimeLinessFlag(1);
            }
        }

        if (servicePoint.getAppFlag() == null) {
            servicePoint.setAppFlag(0);
        }
        if(servicePoint.getRemotePriceEnabledFlag() == null){
            servicePoint.setRemotePriceEnabledFlag(0);
        }
        if(servicePoint.getRemotePriceType() == null){
            servicePoint.setRemotePriceType(0);
        }
        if(servicePoint.getRemotePriceFlag() == null){
            servicePoint.setRemotePriceFlag(0);
        }

        ArrayList<MDEngineerEnum.EngineerCertPicOrder> list = new ArrayList<>(Arrays.asList(MDEngineerEnum.EngineerCertPicOrder.values()));
        List<MDEngineerCert> mdEngineerCerts = Lists.newArrayList();
        for (MDEngineerEnum.EngineerCertPicOrder item : list) {
            MDEngineerCert mdEngineerCert = new MDEngineerCert();
            mdEngineerCert.setNo(item.getValue());
            mdEngineerCert.setPicUrl(String.valueOf(item.getLabel()));
            mdEngineerCerts.add(mdEngineerCert);
        }

        List<MDDepositLevel> depositLevelList = msDepositLevelService.findAllLevelList();
        model.addAttribute("depositLevelList", depositLevelList);
        model.addAttribute("servicePoint", servicePoint);
        model.addAttribute("mdEngineerCerts", mdEngineerCerts);
        List<Area> areaList = areaService.findAll(2);
        areaList.add(0, new Area(1l, "区域列表", 1));
        model.addAttribute("areaList", areaList);
        return "modules/md/servicePointFormNewTwo";
    }


    /**
     * 修改网点
     */
    @RequiresPermissions("md:servicepoint:view")
    @RequestMapping(value = "newForm")
    public String newForm(ServicePoint servicePoint, Model model) {
        User user = UserUtils.getUser();
        if (servicePoint.getId() != null && servicePoint.getId() != 0) {
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

                if(!servicePoint.getPrimary().getAttachment().equals("")){
                    Gson gson = new GsonBuilder().create();
                    List<MDEngineerCert> list = gson.fromJson(servicePoint.getPrimary().getAttachment(),new TypeToken<ArrayList<MDEngineerCert>>(){}.getType());
                    servicePoint.getPrimary().setEngineerCerts(list);
                }
            }
            // add on 2020-2-25 end
            List<Long> categories = servicePointService.findCategoryListByServicePiontId(servicePoint.getId());
            String categoryIds = "";
            if (categories != null && !categories.isEmpty()) {
                categoryIds = categories.stream().map(Object::toString).collect(Collectors.joining(","));
            }
            model.addAttribute("productCategories", categoryIds);

            List<Map<String, Object>> stationList = msServicePointStationService.findSpecListByServicePointIdToMapList(servicePoint.getId());
            model.addAttribute("stationList", stationList);

            ServicePointFinance finance = servicePoint.getFinance();
            if (servicePoint.getDiscountFlag() != null &&  servicePoint.getDiscountFlag() == 1 && finance != null ) {
                double discount = finance.getDiscount();
                if (discount >0) {
                    discount = (int)(discount * 100);
                    finance.setDiscount(discount);
                }
            }
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
            servicePoint.setRemotePriceFlag(0);
            servicePoint.setRemotePriceEnabledFlag(0);

        }


        ArrayList<MDEngineerEnum.EngineerCertPicOrder> list = new ArrayList<>(Arrays.asList(MDEngineerEnum.EngineerCertPicOrder.values()));
        List<MDEngineerCert> mdEngineerCerts = Lists.newArrayList();
        for (MDEngineerEnum.EngineerCertPicOrder item : list) {
            MDEngineerCert mdEngineerCert = new MDEngineerCert();
            mdEngineerCert.setNo(item.getValue());
            mdEngineerCert.setPicUrl(String.valueOf(item.getLabel()));
            mdEngineerCerts.add(mdEngineerCert);
        }

        if (servicePoint.getAppFlag() == null) {
            servicePoint.setAppFlag(0);
        }

        if(servicePoint.getRemotePriceEnabledFlag() == null){
            servicePoint.setRemotePriceEnabledFlag(0);
        }
        if(servicePoint.getRemotePriceType() == null){
            servicePoint.setRemotePriceType(0);
        }
        if(servicePoint.getRemotePriceFlag() == null){
            servicePoint.setRemotePriceFlag(0);
        }

        List<MDDepositLevel> depositLevelList = msDepositLevelService.findAllLevelList();
        model.addAttribute("depositLevelList", depositLevelList);
        model.addAttribute("servicePoint", servicePoint);
        model.addAttribute("mdEngineerCerts", mdEngineerCerts);
        List<Area> areaList = areaService.findAll(2);
        areaList.add(0, new Area(1l, "区域列表", 1));
        model.addAttribute("areaList", areaList);
//        return "modules/md/servicePointFormNewer";
        return "modules/md/servicePointFormNewTwo";
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
        }
        if(servicePoint.getRemotePriceEnabledFlag() == null){
            servicePoint.setRemotePriceEnabledFlag(0);
        }
        if(servicePoint.getRemotePriceType() == null){
            servicePoint.setRemotePriceType(0);
        }
        if(servicePoint.getRemotePriceFlag() == null){
            servicePoint.setRemotePriceFlag(0);
        }
        ServicePointFinance finance = servicePoint.getFinance();
        if (servicePoint.getDiscountFlag() != null &&  servicePoint.getDiscountFlag() == 1 && finance != null ) {
            double discount = finance.getDiscount();
            if (discount >0) {
                discount = (int)(discount * 100);
                finance.setDiscount(discount);
            }
        }
        model.addAttribute("servicePoint", servicePoint);
        return "modules/md/servicePointPermissionSettingFormNew";
    }


    /**
     * 保存网点
     * 不需要审核
     */
    @RequiresPermissions("md:servicepoint:edit")
    @RequestMapping(value = "save")
    @ResponseBody
    public AjaxJsonEntity save(ServicePoint servicePoint, Model model, RedirectAttributes redirectAttributes) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        User user = UserUtils.getUser();
        if (user == null || user.getId() == null) {
            addMessage(redirectAttributes, "错误：登录超时");
            ajaxJsonEntity.setMessage("错误：登录超时");
            ajaxJsonEntity.setSuccess(false);
            return ajaxJsonEntity;
        }
        if (servicePoint.getArea() != null && StringUtils.isNoneBlank(servicePoint.getArea().getFullName())) {
            servicePoint.setAddress(servicePoint.getArea().getFullName() + " " + servicePoint.getSubAddress());
        }
        if (StringUtils.isBlank(servicePoint.getDeveloper())) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("请输入开发人员!");
            return ajaxJsonEntity;
        }
        if(servicePoint.getPrimary() != null && !servicePoint.getPrimary().getAttachment().equals("")){
            List<MDEngineerCert> engineerCerts = new ArrayList();
            MDEngineerCert entity;
           String[] arr  =  servicePoint.getPrimary().getAttachment().split(":");
            for(int i = 0;i<arr.length;i++){
                if(arr[i] != null && !arr[i].equals("")){
                    String[] starr = arr[i].split(",");
                    entity = new MDEngineerCert();
                    entity.setNo(Integer.valueOf(starr[0]));
                    entity.setPicUrl(starr[1]);
                    engineerCerts.add(entity);
                }
            }
            servicePoint.getPrimary().setEngineerCerts(engineerCerts);
        }
        try {
            servicePoint.setCreateBy(user);
            servicePoint.setCreateDate(new Date());
            servicePointService.insertServicePoint(servicePoint);  // add on 2020-5-20
            ajaxJsonEntity.setMessage("网点信息已保存");
        } catch (Exception e) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("网点信息已保存失败，原因：" + e.getMessage());
        }
        return ajaxJsonEntity;
    }

    /**
     * 保存网点重要信息
     */
    @RequiresPermissions("md:servicepoint:edit")
    @RequestMapping(value = "psSave")
    @ResponseBody
    public AjaxJsonEntity permisionSettingSave(ServicePoint servicePoint, Model model, RedirectAttributes redirectAttributes) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        User user = UserUtils.getUser();
        if (user == null || user.getId() == null) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("错误：登录超时");
            return ajaxJsonEntity;
        }
        try {
            servicePoint.setCreateBy(user);
            servicePoint.setCreateDate(new Date());
            servicePoint.setDelFlag(ServicePoint.DEL_FLAG_NORMAL);//正常
            servicePointService.permissionSettingSave(servicePoint);
            ajaxJsonEntity.setMessage("网点信息已保存");
        } catch (Exception e) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("网点信息已保存失败，原因：" + e.getMessage());
        }
        return ajaxJsonEntity;
    }

    /**
     * 保存网点基本信息
     * 不需要审核
     */
    @RequiresPermissions("md:servicepoint:edit")
    @RequestMapping(value = "saveBaseInfo")
    @ResponseBody
    public AjaxJsonEntity saveBaseInfo(ServicePoint servicePoint, Model model, RedirectAttributes redirectAttributes) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        User user = UserUtils.getUser();
        if (user == null || user.getId() == null) {
            ajaxJsonEntity.setMessage("错误：登录超时");
            ajaxJsonEntity.setSuccess(false);
            return ajaxJsonEntity;
        }
        if (servicePoint.getArea() != null && StringUtils.isNoneBlank(servicePoint.getArea().getFullName())) {
            servicePoint.setAddress(servicePoint.getArea().getFullName() + " " + servicePoint.getSubAddress());
        }
        if (StringUtils.isBlank(servicePoint.getDeveloper())) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("请输入开发人员!");
            return ajaxJsonEntity;
        }

        if(servicePoint.getPrimary() != null && !servicePoint.getPrimary().getAttachment().equals("")){
            List<MDEngineerCert> engineerCerts = new ArrayList();
            MDEngineerCert entity;
            String[] arr  =  servicePoint.getPrimary().getAttachment().split(":");
            for(int i = 0;i<arr.length;i++){
                if(arr[i] != null && !arr[i].equals("")){
                    String[] starr = arr[i].split(",");
                    entity = new MDEngineerCert();
                    entity.setNo(Integer.valueOf(starr[0]));
                    entity.setPicUrl(starr[1]);
                    engineerCerts.add(entity);
                }
            }
            servicePoint.getPrimary().setEngineerCerts(engineerCerts);
        }

        try {
            servicePoint.setCreateBy(user);
            servicePoint.setCreateDate(new Date());
            servicePointService.saveBaseInfo(servicePoint);
            ajaxJsonEntity.setMessage("网点信息已保存");
        } catch (Exception e) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("网点信息已保存失败，原因：" + e.getMessage());
        }
        return ajaxJsonEntity;

    }

    /**
     * 保存网点产品
     *
     * @param servicePoint
     * @param model
     * @param redirectAttributes
     * @return
     */
    @RequiresPermissions("md:servicepoint:edit")
    @RequestMapping(value = "saveProductsNew")
    @ResponseBody
    public AjaxJsonEntity saveProductsNew(ServicePoint servicePoint, Model model, RedirectAttributes redirectAttributes) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        User user = UserUtils.getUser();
        if (user == null || user.getId() == null) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("错误：登录超时");
            return ajaxJsonEntity;
        }

        try {
            servicePoint.setCreateBy(user);
            servicePoint.setCreateDate(new Date());
            servicePointService.saveProducts(servicePoint);
            ajaxJsonEntity.setMessage("保存网点 [" + servicePoint.getName() + "] 产品成功");
        } catch (Exception e) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("错误，原因：" + e.getMessage());
        }
        return ajaxJsonEntity;
    }

    /**
     * 保存网点区域
     */
    @RequiresPermissions("md:servicepoint:edit")
    @RequestMapping(value = "saveAreasNew")
    @ResponseBody
    public AjaxJsonEntity saveAreasNew(ServicePoint servicePoint, Model model, RedirectAttributes redirectAttributes) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        User user = UserUtils.getUser();
        if (user == null || user.getId() == null) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("错误：登录超时");
            return ajaxJsonEntity;
        }

        servicePoint.setCreateBy(user);
        servicePoint.setCreateDate(new Date());
        try {
            servicePointService.saveAreas(servicePoint);
            ajaxJsonEntity.setMessage("保存网点 [" + servicePoint.getName() + "] 区域成功");
        } catch (Exception e) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("错误，原因：" + e.getMessage());
        }
        return ajaxJsonEntity;
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
        return "modules/sd/service/viewPlanRemarkListNew";
    }

    /**
     * 获取保质金信息
     */
    @RequestMapping(value = "getDepositLevel")
    @ResponseBody
    public AjaxJsonEntity getDepositLevel(Long mdDepositLevelId) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        User user = UserUtils.getUser();
        if (user == null || user.getId() == null) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("错误：登录超时");
            return ajaxJsonEntity;
        }
        try {
            MDDepositLevel depositLevel = msDepositLevelService.getById(mdDepositLevelId);
            ajaxJsonEntity.setData(depositLevel);
        } catch (Exception e) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("错误，原因：" + e.getMessage());
        }
        return ajaxJsonEntity;
    }

    /**
     * 修改互助基金
     */
    @RequestMapping(value = "updateInsuranceFlag")
    @ResponseBody
    public AjaxJsonEntity updateInsuranceFlag(Long id, Integer insuranceFlag) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        User user = UserUtils.getUser();
        if (user == null || user.getId() == null) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("错误：登录超时");
            return ajaxJsonEntity;
        }
        try {
            Integer num = servicePointService.updateInsuranceFlag(id, insuranceFlag);
            if (num == 0) {
                ajaxJsonEntity.setSuccess(false);
                ajaxJsonEntity.setMessage("修改失败");
            }
        } catch (Exception e) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("错误，原因：" + e.getMessage());
        }
        return ajaxJsonEntity;
    }
}
