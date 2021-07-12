package com.wolfking.jeesite.ms.providerrpt.controller;


import com.google.common.collect.Lists;
import com.kkl.kklplus.entity.rpt.RPTAreaCompletedDailyEntity;
import com.kkl.kklplus.entity.rpt.exception.RPTBaseException;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.md.entity.ProductCategory;
import com.wolfking.jeesite.modules.md.utils.ProductUtils;
import com.wolfking.jeesite.modules.rpt.entity.RptSearchCondition;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.service.SystemService;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providerrpt.service.MSComplainRatioDailyRptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping(value = "${adminPath}/rpt/provider/complainCompleteRatio/")
public class MSComplainRatioDailyRptController {

    @Autowired
    private MSComplainRatioDailyRptService  msComplainRatioDailyRptService;

    @Autowired
    private AreaService areaService;

    @Autowired
    private SystemService systemService;

    @ModelAttribute("rptSearchCondition")
    public RptSearchCondition get(@ModelAttribute("rptSearchCondition") RptSearchCondition rptSearchCondition) {
        if (rptSearchCondition == null) {
            rptSearchCondition = new RptSearchCondition();
        }
        Date now = new Date();
        if (rptSearchCondition.getSelectedYear() == null) {
            rptSearchCondition.setSelectedYear(DateUtils.getYear(now));
        }
        if (rptSearchCondition.getSelectedMonth() == null) {
            rptSearchCondition.setSelectedMonth(DateUtils.getMonth(now));
        }

        return rptSearchCondition;
    }

    /**
     * 省每日完成报表
     *
     * @param rptSearchCondition
     * @return
     */
    @RequestMapping(value = "provinceComplainCompleteOrderRpt")
    public String provinceOrderCompleteDayRpt(RptSearchCondition rptSearchCondition, Model model) {
        List<ProductCategory> productCategoryList = ProductUtils.getProductCategoryRPTList();
        List<Long> productCategoryIds = Lists.newArrayList();
        List<RPTAreaCompletedDailyEntity> list = Lists.newArrayList();
        User user = UserUtils.getUser();
        if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()) {
            productCategoryIds = systemService.getAuthorizedProductCategoryIds(user.getId());
            List<Long> finalProductCategoryIds = productCategoryIds;
            productCategoryList = productCategoryList.stream().filter(r -> finalProductCategoryIds.contains(r.getId())).collect(Collectors.toList());
        }
        if (rptSearchCondition.isSearching()) {
            Integer type = 0;
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
                list = msComplainRatioDailyRptService.getProvinceComplainCompletedRptData(type, rptSearchCondition.getAreaId(), rptSearchCondition.getSelectedYear(), rptSearchCondition.getSelectedMonth(),
                        rptSearchCondition.getCustomerId(), productCategoryIds);
            }
        }
        int days = DateUtils.getDaysOfMonth(DateUtils.parseDate(rptSearchCondition.getSelectedYear() + "-" + rptSearchCondition.getSelectedMonth() + "-01"));
        rptSearchCondition.setDays(days);
        model.addAttribute("list", list);
        model.addAttribute("productCategoryList",productCategoryList);
        return "modules/providerrpt/provinceComplainCompletedRatioReport";
    }

    /**
     * 市每日完成报表
     *
     * @param rptSearchCondition
     * @return
     */
    @RequestMapping(value = "cityComplainCompleteOrderRpt")
    public String cityOrderCompleteDayRpt(RptSearchCondition rptSearchCondition,Long provinceId, Integer selectedYear, Integer selectedMonth, String areaName, Model model) {
        List<ProductCategory> productCategoryList = ProductUtils.getProductCategoryRPTList();
        WriteQueryConditions(rptSearchCondition, provinceId, selectedYear, selectedMonth, areaName);
        List<Long> productCategoryIds = Lists.newArrayList();
        List<RPTAreaCompletedDailyEntity> list = Lists.newArrayList();
        User user = UserUtils.getUser();
        if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()) {
            productCategoryIds = systemService.getAuthorizedProductCategoryIds(user.getId());
            List<Long> finalProductCategoryIds = productCategoryIds;
            productCategoryList = productCategoryList.stream().filter(r -> finalProductCategoryIds.contains(r.getId())).collect(Collectors.toList());
        }
        if (rptSearchCondition.isSearching()) {
            Integer type = 0;
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
                list = msComplainRatioDailyRptService.getCityComplainCompletedRptData(type, rptSearchCondition.getAreaId(), rptSearchCondition.getSelectedYear(), rptSearchCondition.getSelectedMonth(),
                        rptSearchCondition.getCustomerId(), productCategoryIds); }
        }

        int days = DateUtils.getDaysOfMonth(DateUtils.parseDate(rptSearchCondition.getSelectedYear() + "-" + rptSearchCondition.getSelectedMonth() + "-01"));

        rptSearchCondition.setDays(days);
        model.addAttribute("list", list);
        model.addAttribute("productCategoryList",productCategoryList);
        return "modules/providerrpt/cityComplainCompletedRatioReport";
    }

    /**
     * 区每日完成报表
     *
     * @param rptSearchCondition
     * @return
     */
    @RequestMapping(value = "areaComplainCompleteOrderRpt")
    public String areaOrderCompleteDayRpt(RptSearchCondition rptSearchCondition,Long cityId, Integer selectedYear, Integer selectedMonth, String areaName, Model model) {
        List<ProductCategory> productCategoryList = ProductUtils.getProductCategoryRPTList();
        WriteQueryConditions(rptSearchCondition, cityId, selectedYear, selectedMonth, areaName);
        List<Long> productCategoryIds = Lists.newArrayList();
        List<RPTAreaCompletedDailyEntity> list = Lists.newArrayList();
        User user = UserUtils.getUser();
        if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()) {
            productCategoryIds = systemService.getAuthorizedProductCategoryIds(user.getId());
            List<Long> finalProductCategoryIds = productCategoryIds;
            productCategoryList = productCategoryList.stream().filter(r -> finalProductCategoryIds.contains(r.getId())).collect(Collectors.toList());
        }
        if (rptSearchCondition.isSearching()) {
            Integer type = 0;
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
                list = msComplainRatioDailyRptService.getAreaComplainCompletedRptData(type, rptSearchCondition.getAreaId(), rptSearchCondition.getSelectedYear(), rptSearchCondition.getSelectedMonth(),
                        rptSearchCondition.getCustomerId(), productCategoryIds);  }
        }
        int days = DateUtils.getDaysOfMonth(DateUtils.parseDate(rptSearchCondition.getSelectedYear() + "-" + rptSearchCondition.getSelectedMonth() + "-01"));

        rptSearchCondition.setDays(days);
        model.addAttribute("list", list);
        model.addAttribute("productCategoryList",productCategoryList);
        return "modules/providerrpt/areaComplainCompletedRatioReport";
    }
    private void WriteQueryConditions(RptSearchCondition rptSearchCondition, Long cityId, Integer selectedYear, Integer selectedMonth, String areaName) {
        if (cityId != null) {
            rptSearchCondition.setIsSearching(1);
            if (rptSearchCondition.getAreaId() == null) {
                rptSearchCondition.setAreaId(cityId);
            }
            if (rptSearchCondition.getAreaName() == null) {
                rptSearchCondition.setAreaName(areaName);
            }
            if (rptSearchCondition.getSelectedYear() == null) {
                rptSearchCondition.setSelectedYear(selectedYear);
                rptSearchCondition.setSelectedMonth(selectedMonth);
            }
        }
    }

    @ResponseBody
    @RequestMapping(value = "areaComplainCompletedCheckExportTask", method = RequestMethod.POST)
    public AjaxJsonEntity areaCompletedCheckExportTask(RptSearchCondition rptSearchCondition) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try {
            User user = UserUtils.getUser();
            List<Long> productCategoryIds = Lists.newArrayList();
            if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()) {
                productCategoryIds =  systemService.getAuthorizedProductCategoryIds(user.getId());
            }
            if (rptSearchCondition.getProductCategory() != 0) {
                productCategoryIds = Lists.newArrayList();
                productCategoryIds.add(rptSearchCondition.getProductCategory().longValue());
            }
            Integer type = 0;
            if (rptSearchCondition.getAreaId() != null) {
                Area area = areaService.getFromCache(rptSearchCondition.getAreaId());
                type = area.getType();
            }
            if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()){
                if (productCategoryIds.isEmpty()) {
                    result.setSuccess(false);
                    result.setMessage("创建报表导出任务失败，请重试");
                    return result;
                }
            }
            msComplainRatioDailyRptService.checkAreaCompletedRptExportTask(rptSearchCondition.getAreaId(),type,rptSearchCondition.getSelectedYear(),rptSearchCondition.getSelectedMonth(),rptSearchCondition.getCustomerId(),  productCategoryIds, user);

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
    @RequestMapping(value = "provinceComplainCompletedExport", method = RequestMethod.POST)
    public AjaxJsonEntity provinceCompletedExport(RptSearchCondition rptSearchCondition) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try {
            User user = UserUtils.getUser();
            List<Long> productCategoryIds = Lists.newArrayList();
            Integer type = 0;
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
            if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()){
                if (productCategoryIds.isEmpty()) {
                    result.setSuccess(false);
                    result.setMessage("创建报表导出任务失败，请重试");
                    return result;
                }
            }
            msComplainRatioDailyRptService.createAreaCompletedRptExportTask(rptSearchCondition.getAreaId(),type,rptSearchCondition.getSelectedYear(),rptSearchCondition.getSelectedMonth(),rptSearchCondition.getCustomerId(), productCategoryIds, 1,user);
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

    @ResponseBody
    @RequestMapping(value = "cityComplainCompletedExport", method = RequestMethod.POST)
    public AjaxJsonEntity cityCompletedExport(RptSearchCondition rptSearchCondition) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try {
            User user = UserUtils.getUser();
            List<Long> productCategoryIds = Lists.newArrayList();
            Integer type = 0;
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
            if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()){
                if (productCategoryIds.isEmpty()) {
                    result.setSuccess(false);
                    result.setMessage("创建报表导出任务失败，请重试");
                    return result;
                }
            }
            msComplainRatioDailyRptService.createAreaCompletedRptExportTask(rptSearchCondition.getAreaId(),type,rptSearchCondition.getSelectedYear(),rptSearchCondition.getSelectedMonth(),rptSearchCondition.getCustomerId(), productCategoryIds, 2,user);
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

    @ResponseBody
    @RequestMapping(value = "areaComplainCompletedExport", method = RequestMethod.POST)
    public AjaxJsonEntity areaCompletedExport(RptSearchCondition rptSearchCondition) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try {
            User user = UserUtils.getUser();
            List<Long> productCategoryIds = Lists.newArrayList();
            Integer type = 0;
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
            if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()){
                if (productCategoryIds.isEmpty()) {
                    result.setSuccess(false);
                    result.setMessage("创建报表导出任务失败，请重试");
                    return result;
                }
            }
            msComplainRatioDailyRptService.createAreaCompletedRptExportTask(rptSearchCondition.getAreaId(),type,rptSearchCondition.getSelectedYear(),rptSearchCondition.getSelectedMonth(),rptSearchCondition.getCustomerId(), productCategoryIds, 3,user);
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
