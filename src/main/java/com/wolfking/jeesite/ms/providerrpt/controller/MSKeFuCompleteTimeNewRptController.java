package com.wolfking.jeesite.ms.providerrpt.controller;

import com.google.common.collect.Lists;
import com.kkl.kklplus.entity.rpt.RPTKeFuCompleteTimeEntity;
import com.kkl.kklplus.entity.rpt.exception.RPTBaseException;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.md.entity.ProductCategory;
import com.wolfking.jeesite.modules.md.utils.ProductUtils;
import com.wolfking.jeesite.modules.rpt.entity.RptSearchCondition;
import com.wolfking.jeesite.modules.rpt.web.BaseRptController;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.service.SystemService;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providerrpt.service.MSKeFuCompleteTimeNewRptService;
import com.wolfking.jeesite.ms.providerrpt.service.MSKeFuCompleteTimeRptService;
import com.wolfking.jeesite.ms.providerrpt.utils.MSRptUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "${adminPath}/rpt/provider/keFuCompleteTimeNew/")
public class MSKeFuCompleteTimeNewRptController extends BaseRptController {

    @Autowired
    private MSKeFuCompleteTimeNewRptService msKeFuCompleteTimeNewRptService;

    @Autowired
    private SystemService systemService;

    @Autowired
    private AreaService areaService;


    @ModelAttribute("rptSearchCondition")
    public RptSearchCondition get(@ModelAttribute("rptSearchCondition") RptSearchCondition rptSearchCondition) {
        if (rptSearchCondition == null) {
            rptSearchCondition = new RptSearchCondition();
        }
        return rptSearchCondition;
    }

    /**
     * 新客服完工时效报表
     *
     * @param rptSearchCondition
     * @param model
     * @return
     */
    @SuppressWarnings("deprecation")
    @RequiresPermissions("rpt:keFuCompleteTimeNewReport:view")
    @RequestMapping(value = "keFuCompleteTimeNewReport")
    public String KeFuCompleteTimeReport(RptSearchCondition rptSearchCondition, Model model) {
        Date date = DateUtils.addDays(new Date(), -1);
        if (rptSearchCondition.getEndDate() == null) {
            rptSearchCondition.setEndDate(DateUtils.getEndOfDay(date));
        } else {
            rptSearchCondition.setEndDate(rptSearchCondition.getEndDate());
        }
        User user = UserUtils.getUser();
        List<Long> productCategoryIds = Lists.newArrayList();
        List<ProductCategory> productCategoryList = ProductUtils.getProductCategoryRPTList();
        if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()) {
            productCategoryIds = systemService.getAuthorizedProductCategoryIds(user.getId());
            List<Long> finalProductCategoryIds = productCategoryIds;
            productCategoryList = productCategoryList.stream().filter(r -> finalProductCategoryIds.contains(r.getId())).collect(Collectors.toList());
        }
        List<RPTKeFuCompleteTimeEntity> list = Lists.newArrayList();
        if (rptSearchCondition.isSearching()) {
            Integer type = 0;
            Long customerId = rptSearchCondition.getCustomerId();
            Long salesId = null;
            if (user.isCustomer()) {
                customerId = UserUtils.getUser().getCustomerAccountProfile().getCustomer().getId();
            } else if (user.isSaleman()) {
                salesId = user.getId();
            }
            rptSearchCondition.setCustomerId(customerId);
            rptSearchCondition.setSalesId(salesId);
            if (rptSearchCondition.getAreaId() != null) {
                Area area = areaService.getFromCache(rptSearchCondition.getAreaId());
                type = area.getType();
            }
            if (rptSearchCondition.getProductCategory() != 0) {
                productCategoryIds = Lists.newArrayList();
                productCategoryIds.add(rptSearchCondition.getProductCategory().longValue());
            }
            if (productCategoryIds.isEmpty() &&
                    (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue())) {
            } else {
                list = msKeFuCompleteTimeNewRptService.getKeFuCompleteTimeList(rptSearchCondition.getSubFlag(),rptSearchCondition.getEndDate(), type, rptSearchCondition.getAreaId(), rptSearchCondition.getSalesId(), rptSearchCondition.getCustomerId(), rptSearchCondition.getKefuId(), rptSearchCondition.getServicePointId(), rptSearchCondition.getOrderServiceType(), productCategoryIds);
            }
        }
        rptSearchCondition.setEndPlanDate(date);
        rptSearchCondition.setList(list);
        model.addAttribute("productCategoryList", productCategoryList);
        model.addAttribute("keFuTypeEnumList", MSRptUtils.getAllKeFuTypeList());
        return "modules/providerrpt/keFuCompleteTimeNewReport";
    }


    @RequiresPermissions("rpt:keFuCompleteTimeNewReport:view")
    @RequestMapping(value = "keFuCompleteTimeNewChart")
    public String KeFuCompleteTimeChart(RptSearchCondition rptSearchCondition, Model model) {
        Date date = DateUtils.addDays(new Date(), -1);
        if (rptSearchCondition.getEndDate() == null) {
            rptSearchCondition.setEndDate(DateUtils.getEndOfDay(date));
        } else {
            rptSearchCondition.setEndDate(rptSearchCondition.getEndDate());
        }
        User user = UserUtils.getUser();
        List<Long> productCategoryIds = Lists.newArrayList();
        List<ProductCategory> productCategoryList = ProductUtils.getProductCategoryRPTList();
        if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()) {
            productCategoryIds = systemService.getAuthorizedProductCategoryIds(user.getId());
            List<Long> finalProductCategoryIds = productCategoryIds;
            productCategoryList = productCategoryList.stream().filter(r -> finalProductCategoryIds.contains(r.getId())).collect(Collectors.toList());
        }
        Map<String, Object> map = new HashMap<>();
        rptSearchCondition.setEndPlanDate(date);
        Integer type = 0;
        Long customerId = rptSearchCondition.getCustomerId();
        Long salesId = null;
        if (user.isCustomer()) {
            customerId = UserUtils.getUser().getCustomerAccountProfile().getCustomer().getId();
        } else if (user.isSaleman()) {
            salesId = user.getId();
        }
        rptSearchCondition.setCustomerId(customerId);
        rptSearchCondition.setSalesId(salesId);
        if (rptSearchCondition.getAreaId() != null) {
            Area area = areaService.getFromCache(rptSearchCondition.getAreaId());
            type = area.getType();
        }
        if (rptSearchCondition.getProductCategory() != 0) {
            productCategoryIds = Lists.newArrayList();
            productCategoryIds.add(rptSearchCondition.getProductCategory().longValue());
        }
        if (productCategoryIds.isEmpty() &&
                (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue())) {
        } else {
            map = msKeFuCompleteTimeNewRptService.getKeFuCompleteTimeChartList(rptSearchCondition.getSubFlag(),rptSearchCondition.getEndDate(), type, rptSearchCondition.getAreaId(), rptSearchCondition.getSalesId(), rptSearchCondition.getCustomerId(), rptSearchCondition.getKefuId(), rptSearchCondition.getServicePointId(), rptSearchCondition.getOrderServiceType(), productCategoryIds);
        }

        if (!map.isEmpty()) {
            //设置圆形图表
            model.addAttribute("mapList", map.get("mapList"));
            model.addAttribute("completeMapList", map.get("completeMapList"));
            //设置柱状图表
            model.addAttribute("strComplete24hours", map.get("strComplete24hours"));
            model.addAttribute("strComplete48hours", map.get("strComplete48hours"));
            model.addAttribute("strComplete72hours", map.get("strComplete72hours"));
            model.addAttribute("strOverComplete72hours", map.get("strOverComplete72hours"));
            model.addAttribute("strUnfulfilledOrders", map.get("strUnfulfilledOrders"));
            model.addAttribute("strTheTotalOrders", map.get("strTheTotalOrders"));
            model.addAttribute("createDates", map.get("createDates"));
            //设置线型报表
            model.addAttribute("strComplete24hourRates", map.get("strComplete24hourRates"));
            model.addAttribute("strComplete48hourRates", map.get("strComplete48hourRates"));
            model.addAttribute("strComplete72hourRates", map.get("strComplete72hourRates"));
            model.addAttribute("strOverComplete72hourRates", map.get("strOverComplete72hourRates"));
            model.addAttribute("strUnfulfilledOrderRates", map.get("strUnfulfilledOrderRates"));
        }

        model.addAttribute("productCategoryList", productCategoryList);
        model.addAttribute("keFuTypeEnumList", MSRptUtils.getAllKeFuTypeList());
        return "modules/providerrpt/keFuCompleteTimeNewChart";
    }

    @ResponseBody
    @RequiresPermissions("rpt:keFuCompleteTimeNewReport:export")
    @RequestMapping(value = "checkExportTask", method = RequestMethod.POST)
    public AjaxJsonEntity checkExportTask(RptSearchCondition rptSearchCondition) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try {

            Date date = DateUtils.addDays(new Date(), -1);
            if (rptSearchCondition.getEndDate() == null) {
                rptSearchCondition.setEndDate(DateUtils.getEndOfDay(date));
            } else {
                rptSearchCondition.setEndDate(rptSearchCondition.getEndDate());
            }
            User user = UserUtils.getUser();
            List<Long> productCategoryIds = Lists.newArrayList();
            Integer type = 0;
            Long customerId = rptSearchCondition.getCustomerId();
            Long salesId = null;
            if (user.isCustomer()) {
                customerId = UserUtils.getUser().getCustomerAccountProfile().getCustomer().getId();
            } else if (user.isSaleman()) {
                salesId = user.getId();
            }
            rptSearchCondition.setCustomerId(customerId);
            rptSearchCondition.setSalesId(salesId);
            if (rptSearchCondition.getAreaId() != null) {
                Area area = areaService.getFromCache(rptSearchCondition.getAreaId());
                type = area.getType();
            }
            if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()) {
                productCategoryIds =  systemService.getAuthorizedProductCategoryIds(user.getId());
            }
            if (rptSearchCondition.getProductCategory() != 0) {
                productCategoryIds = Lists.newArrayList();
                productCategoryIds.add(rptSearchCondition.getProductCategory().longValue());
            }
            if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()) {
                if (productCategoryIds.isEmpty()) {
                    result.setSuccess(false);
                    result.setMessage("创建报表导出任务失败，请重试");
                    return result;
                }
            }
            msKeFuCompleteTimeNewRptService.checkRptExportTask(rptSearchCondition.getSubFlag(),rptSearchCondition.getEndDate(), type, rptSearchCondition.getAreaId(), rptSearchCondition.getSalesId(), rptSearchCondition.getCustomerId(), rptSearchCondition.getKefuId(), rptSearchCondition.getServicePointId(), rptSearchCondition.getOrderServiceType(), productCategoryIds, user);

        } catch (RPTBaseException e) {
            result.setSuccess(false);
            result.setMessage(e.getMessage());
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("创建报表导出任务失败，请重试");
        }
        return result;
    }

    @ResponseBody
    @RequiresPermissions("rpt:keFuCompleteTimeNewReport:export")
    @RequestMapping(value = "export", method = RequestMethod.POST)
    public AjaxJsonEntity export(RptSearchCondition rptSearchCondition) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try {

            Date date = DateUtils.addDays(new Date(), -1);
            if (rptSearchCondition.getEndDate() == null) {
                rptSearchCondition.setEndDate(DateUtils.getEndOfDay(date));
            } else {
                rptSearchCondition.setEndDate(rptSearchCondition.getEndDate());
            }
            User user = UserUtils.getUser();
            List<Long> productCategoryIds = Lists.newArrayList();
            Integer type = 0;
            Long customerId = rptSearchCondition.getCustomerId();
            Long salesId = null;
            if (user.isCustomer()) {
                customerId = UserUtils.getUser().getCustomerAccountProfile().getCustomer().getId();
            } else if (user.isSaleman()) {
                salesId = user.getId();
            }
            rptSearchCondition.setCustomerId(customerId);
            rptSearchCondition.setSalesId(salesId);
            if (rptSearchCondition.getAreaId() != null) {
                Area area = areaService.getFromCache(rptSearchCondition.getAreaId());
                type = area.getType();
            }

            if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()) {
                productCategoryIds =  systemService.getAuthorizedProductCategoryIds(user.getId());
            }

            if (rptSearchCondition.getProductCategory() != 0) {
                productCategoryIds = Lists.newArrayList();
                productCategoryIds.add(rptSearchCondition.getProductCategory().longValue());
            }
            if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()) {
                if (productCategoryIds.isEmpty()) {
                    result.setSuccess(false);
                    result.setMessage("创建报表导出任务失败，请重试");
                    return result;
                }
            }
            msKeFuCompleteTimeNewRptService.createRptExportTask(rptSearchCondition.getSubFlag(),rptSearchCondition.getEndDate(), type, rptSearchCondition.getAreaId(), rptSearchCondition.getSalesId(), rptSearchCondition.getCustomerId(), rptSearchCondition.getKefuId(), rptSearchCondition.getServicePointId(), rptSearchCondition.getOrderServiceType(), productCategoryIds, user);
            result.setMessage("报表导出任务创建成功，请前往'报表中心->报表下载'功能下载");

        } catch (RPTBaseException e) {
            result.setSuccess(false);
            result.setMessage(e.getMessage());
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("创建报表导出任务失败，请重试");
        }
        return result;
    }
}
