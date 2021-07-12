package com.wolfking.jeesite.ms.providerrpt.controller;


import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.kkl.kklplus.entity.rpt.RPTAreaCompletedDailyEntity;
import com.kkl.kklplus.entity.rpt.RPTDevelopAverageOrderFeeEntity;
import com.kkl.kklplus.entity.rpt.RPTGradedOrderEntity;
import com.kkl.kklplus.entity.rpt.RPTKefuCompletedDailyEntity;
import com.kkl.kklplus.entity.rpt.exception.RPTBaseException;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
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
import com.wolfking.jeesite.ms.providerrpt.service.MSGradedOrderRptService;
import com.wolfking.jeesite.ms.providerrpt.utils.MSRptUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "${adminPath}/rpt/provider/gradedOrder/")
public class MSGradedOrderRptController extends BaseRptController {

    @Autowired
    private MSGradedOrderRptService msGradedOrderRptService;

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

        if (rptSearchCondition.getBeginDate() == null) {
            rptSearchCondition.setBeginDate( DateUtils.addDays(now,-1));
        }
        if (rptSearchCondition.getEndDate() == null) {
            rptSearchCondition.setEndDate( DateUtils.addDays(now,-1));
        }
        return rptSearchCondition;
    }

    /**
     * 省每日完成报表
     *
     * @param rptSearchCondition
     * @return
     */
    @RequestMapping(value = "provinceCompleteOrderRpt")
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
                list = msGradedOrderRptService.getProvinceCompletedRptData(type, rptSearchCondition.getAreaId(), rptSearchCondition.getSelectedYear(), rptSearchCondition.getSelectedMonth(),
                        rptSearchCondition.getCustomerId(), productCategoryIds);
            }
        }
        int days = DateUtils.getDaysOfMonth(DateUtils.parseDate(rptSearchCondition.getSelectedYear() + "-" + rptSearchCondition.getSelectedMonth() + "-01"));
        rptSearchCondition.setDays(days);
        model.addAttribute("list", list);
        model.addAttribute("productCategoryList",productCategoryList);
        return "modules/providerrpt/msOrderProvinceCompleteDayRpt";
    }

    /**
     * 市每日完成报表
     *
     * @param rptSearchCondition
     * @return
     */
    @RequestMapping(value = "cityCompleteOrderRpt")
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
                list = msGradedOrderRptService.getCityCompletedRptData(type, rptSearchCondition.getAreaId(), rptSearchCondition.getSelectedYear(), rptSearchCondition.getSelectedMonth(),
                        rptSearchCondition.getCustomerId(), productCategoryIds); }
        }

        int days = DateUtils.getDaysOfMonth(DateUtils.parseDate(rptSearchCondition.getSelectedYear() + "-" + rptSearchCondition.getSelectedMonth() + "-01"));

        rptSearchCondition.setDays(days);
        model.addAttribute("list", list);
        model.addAttribute("productCategoryList",productCategoryList);
        return "modules/providerrpt/msOrderCityCompleteDayRpt";
    }

    /**
     * 区每日完成报表
     *
     * @param rptSearchCondition
     * @return
     */
    @RequestMapping(value = "areaCompleteOrderRpt")
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
                list = msGradedOrderRptService.getAreaCompletedRptData(type, rptSearchCondition.getAreaId(), rptSearchCondition.getSelectedYear(), rptSearchCondition.getSelectedMonth(),
                        rptSearchCondition.getCustomerId(), productCategoryIds);  }
        }
        int days = DateUtils.getDaysOfMonth(DateUtils.parseDate(rptSearchCondition.getSelectedYear() + "-" + rptSearchCondition.getSelectedMonth() + "-01"));

        rptSearchCondition.setDays(days);
        model.addAttribute("list", list);
        model.addAttribute("productCategoryList",productCategoryList);
        return "modules/providerrpt/msOrderAreaCompleteDayRpt";
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

    /**
     * 工单网点费用报表  .
     * @param rptSearchCondition
     * @param request
     * @param response
     * @param model
     * @return
     */
    @RequestMapping(value = "orderServicePointFeeRpt")
    public String orderServicePointFeeRpt(RptSearchCondition rptSearchCondition, HttpServletRequest request, HttpServletResponse response, Model model) {
        Date beginDate = rptSearchCondition.getBeginDate();
        if (StringUtils.isBlank(rptSearchCondition.getRemarks())) {
            rptSearchCondition.setBeginDate(DateUtils.addDays(beginDate, -31));
            rptSearchCondition.setEndDate(beginDate);
        } else {
            List<String> dates = Splitter.onPattern("~") //[~|-]
                    .omitEmptyStrings()
                    .trimResults()
                    .splitToList(rptSearchCondition.getRemarks());
            if (dates.isEmpty()) {
                rptSearchCondition.setBeginDate(DateUtils.addDays(beginDate, -31));
                rptSearchCondition.setBeginDate(beginDate);
            } else {
                rptSearchCondition.setBeginDate(DateUtils.parseDate(dates.get(0)));
                if (dates.size() > 1) {
                    rptSearchCondition.setEndDate(DateUtils.parseDate(dates.get(1)));
                } else {
                    rptSearchCondition.setEndDate(beginDate);
                }
            }
        }
        rptSearchCondition.setBeginDate(DateUtils.getStartOfDay(rptSearchCondition.getBeginDate()));
        rptSearchCondition.setEndDate(DateUtils.getEndOfDay(rptSearchCondition.getEndDate()));

        User user = UserUtils.getUser();
        Page<RPTGradedOrderEntity> list = new Page<>(request, response);
        List<Long> productCategoryIds = Lists.newArrayList();
        List<ProductCategory> productCategoryList = ProductUtils.getProductCategoryRPTList();
        if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()) {
            productCategoryIds = systemService.getAuthorizedProductCategoryIds(user.getId());
            List<Long> finalProductCategoryIds = productCategoryIds;
            productCategoryList = productCategoryList.stream().filter(r -> finalProductCategoryIds.contains(r.getId())).collect(Collectors.toList());
        }
        if (rptSearchCondition.isSearching()) {
            if (rptSearchCondition.getProductCategory() != 0) {
                productCategoryIds = Lists.newArrayList();
                productCategoryIds.add(rptSearchCondition.getProductCategory().longValue());
            }
            if (productCategoryIds.isEmpty() &&
                    (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue())) {
            } else {
                 list = msGradedOrderRptService.getOrderServicePointFeeRptData(list,rptSearchCondition.getBeginDate(), rptSearchCondition.getEndDate(), rptSearchCondition.getServicePointId(), productCategoryIds);
            }
        }

        model.addAttribute("productCategoryList",productCategoryList);
        model.addAttribute("page",list);

        return "modules/providerrpt/msOrderServicePointFeeRpt";
    }



    @RequestMapping(value = "kefuOrderCompletedDailyRpt")
    public String kefuOrderCompleteDayRpt(RptSearchCondition rptSearchCondition, Model model) {
        List<RPTKefuCompletedDailyEntity> list = Lists.newArrayList();
        User user = UserUtils.getUser();
        List<Long> productCategoryIds = Lists.newArrayList();
        List<ProductCategory> productCategoryList = ProductUtils.getProductCategoryRPTList();
        if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()) {
            productCategoryIds = systemService.getAuthorizedProductCategoryIds(user.getId());
            List<Long> finalProductCategoryIds = productCategoryIds;
            productCategoryList = productCategoryList.stream().filter(r -> finalProductCategoryIds.contains(r.getId())).collect(Collectors.toList());
        }
        if (rptSearchCondition.isSearching()) {
            if (rptSearchCondition.getProductCategory() != 0) {
                productCategoryIds = Lists.newArrayList();
                productCategoryIds.add(rptSearchCondition.getProductCategory().longValue());
            }
            if (productCategoryIds.isEmpty() &&
                    (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue())) {
            } else {
                list = msGradedOrderRptService.getKefuCompletedDailyRptData(rptSearchCondition.getSubFlag(),rptSearchCondition.getSelectedYear(),
                        rptSearchCondition.getSelectedMonth(),productCategoryIds,
                        rptSearchCondition.getKefuId());
            }
        }
        int days = DateUtils.getDaysOfMonth(DateUtils.parseDate(rptSearchCondition.getSelectedYear() + "-" + rptSearchCondition.getSelectedMonth() + "-01"));

        rptSearchCondition.setList(list);
        rptSearchCondition.setDays(days);
        model.addAttribute("productCategoryList", productCategoryList);
        model.addAttribute("keFuTypeEnumList", MSRptUtils.getAllKeFuTypeList());

        return "modules/providerrpt/msKefuOrderCompleteDayRpt";
    }

    @RequestMapping(value = "keFuOrderCompleteChart")
    public String keFuOrderCompleteMonthChart(RptSearchCondition rptSearchCondition, Model model) {
        User user = UserUtils.getUser();
        List<Long> productCategoryIds = Lists.newArrayList();
        List<ProductCategory> productCategoryList = ProductUtils.getProductCategoryRPTList();
        if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()) {
            productCategoryIds = systemService.getAuthorizedProductCategoryIds(user.getId());
            List<Long> finalProductCategoryIds = productCategoryIds;
            productCategoryList = productCategoryList.stream().filter(r -> finalProductCategoryIds.contains(r.getId())).collect(Collectors.toList());
        }
        Map<String, Object> map = new HashMap<>();
        if (rptSearchCondition.getProductCategory() != 0) {
            productCategoryIds = Lists.newArrayList();
            productCategoryIds.add(rptSearchCondition.getProductCategory().longValue());
        }
        if (productCategoryIds.isEmpty() &&
                (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue())) {
        } else {
            map = msGradedOrderRptService.turnToKeFuOrderCompleteDayChartInformation(rptSearchCondition.getSubFlag(),rptSearchCondition.getSelectedYear(),rptSearchCondition.getSelectedMonth(),productCategoryIds,rptSearchCondition.getKefuId());
        }
        model.addAttribute("list", map.get("list"));
        model.addAttribute("daySums", map.get("daySums"));
        model.addAttribute("createDates", map.get("createDates"));
        model.addAttribute("productCategoryList", productCategoryList);
        model.addAttribute("keFuTypeEnumList", MSRptUtils.getAllKeFuTypeList());
        return "modules/providerrpt/msKeFuOrderCompleteChart";
    }

    @RequestMapping(value = "developAverageOrderFeeRpt")
    public String developAverageOrderFee(RptSearchCondition rptSearchCondition, Model model) {
        User user = UserUtils.getUser();
        List<Long> productCategoryIds = Lists.newArrayList();
        List<ProductCategory> productCategoryList = ProductUtils.getProductCategoryRPTList();
        if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()) {
            productCategoryIds = systemService.getAuthorizedProductCategoryIds(user.getId());
            List<Long> finalProductCategoryIds = productCategoryIds;
            productCategoryList = productCategoryList.stream().filter(r -> finalProductCategoryIds.contains(r.getId())).collect(Collectors.toList());
        }
        List<RPTDevelopAverageOrderFeeEntity> list = Lists.newArrayList();
        Date beginDate = DateUtils.getDateStart(rptSearchCondition.getEndDate());
        Date endDate = DateUtils.getDateEnd(rptSearchCondition.getEndDate());
        if (rptSearchCondition.isSearching()) {
            if (rptSearchCondition.getProductCategory() != 0) {
                productCategoryIds = Lists.newArrayList();
                productCategoryIds.add(rptSearchCondition.getProductCategory().longValue());
            }
            if (productCategoryIds.isEmpty() &&
                    (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue())) {
            } else {
                list = msGradedOrderRptService.getDevelopAverageFee(beginDate, endDate, productCategoryIds);
            }
        }
        model.addAttribute("productCategoryList",productCategoryList);
        model.addAttribute("list",list);
        return "modules/providerrpt/msDevelopAverageOrderFeeRpt";
    }

    @ResponseBody
    @RequestMapping(value = "kefuCompletedCheckExportTask", method = RequestMethod.POST)
    public AjaxJsonEntity kefuOrderCheckExportTask(RptSearchCondition rptSearchCondition) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try {
            User user = UserUtils.getUser();
            List<Long> productCategoryIds = Lists.newArrayList();
            List<ProductCategory> productCategoryList = ProductUtils.getProductCategoryRPTList();
            if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()) {
                productCategoryIds = systemService.getAuthorizedProductCategoryIds(user.getId());
                List<Long> finalProductCategoryIds = productCategoryIds;
                productCategoryList = productCategoryList.stream().filter(r -> finalProductCategoryIds.contains(r.getId())).collect(Collectors.toList());
            }
            Map<String, Object> map = new HashMap<>();
            if (rptSearchCondition.getProductCategory() != 0) {
                productCategoryIds = Lists.newArrayList();
                productCategoryIds.add(rptSearchCondition.getProductCategory().longValue());
            }
            if (productCategoryIds.isEmpty() &&
                    (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue())) {
            } else {
                msGradedOrderRptService.checkKefuCompletedRptExportTask(rptSearchCondition.getSubFlag(),rptSearchCondition.getSelectedYear(),  rptSearchCondition.getSelectedMonth(),rptSearchCondition.getKefuId(),productCategoryIds, user);
            }
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
    @RequestMapping(value = "areaCompletedCheckExportTask", method = RequestMethod.POST)
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
            msGradedOrderRptService.checkAreaCompletedRptExportTask(rptSearchCondition.getAreaId(),type,rptSearchCondition.getSelectedYear(),rptSearchCondition.getSelectedMonth(),rptSearchCondition.getCustomerId(),  productCategoryIds, user);

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
    @RequestMapping(value = "developAverageCheckExportTask", method = RequestMethod.POST)
    public AjaxJsonEntity developAverageCheckExportTask(RptSearchCondition rptSearchCondition) {
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

            if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()){
                if (productCategoryIds.isEmpty()) {
                    result.setSuccess(false);
                    result.setMessage("创建报表导出任务失败，请重试");
                    return result;
                }
            }
            Date beginDate = DateUtils.getStartOfDay(rptSearchCondition.getEndDate());
            Date endDate = DateUtils.getEndOfDay(rptSearchCondition.getEndDate());
            msGradedOrderRptService.checkDevelopAverageRptExportTask(beginDate,endDate, productCategoryIds, user);
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
    @RequestMapping(value = "orderServicePointFeeCheckExportTask", method = RequestMethod.POST)
    public AjaxJsonEntity checkExportTask(RptSearchCondition rptSearchCondition) {
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
            List<String> dates = Splitter.onPattern("~") //[~|-]
                    .omitEmptyStrings()
                    .trimResults()
                    .splitToList(rptSearchCondition.getRemarks());

            rptSearchCondition.setBeginDate(DateUtils.parseDate(dates.get(0)));
            if (dates.size() > 1) {
                rptSearchCondition.setEndDate(DateUtils.parseDate(dates.get(1)));
            }else {
                rptSearchCondition.setEndDate(rptSearchCondition.getBeginDate());
            }
            if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()){
                if (productCategoryIds.isEmpty()) {
                    result.setSuccess(false);
                    result.setMessage("创建报表导出任务失败，请重试");
                    return result;
                }
            }

            msGradedOrderRptService.checkOrderServicePointFeeRptExportTask(rptSearchCondition.getBeginDate(),rptSearchCondition.getEndDate(),rptSearchCondition.getServicePointId(),  productCategoryIds, user);

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
    @RequestMapping(value = "kefuCompletedExport", method = RequestMethod.POST)
    public AjaxJsonEntity kefuCompletedExport(RptSearchCondition rptSearchCondition) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try {
            User user = UserUtils.getUser();
            List<Long> productCategoryIds = Lists.newArrayList();
            List<ProductCategory> productCategoryList = ProductUtils.getProductCategoryRPTList();
            if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()) {
                productCategoryIds = systemService.getAuthorizedProductCategoryIds(user.getId());
                List<Long> finalProductCategoryIds = productCategoryIds;
                productCategoryList = productCategoryList.stream().filter(r -> finalProductCategoryIds.contains(r.getId())).collect(Collectors.toList());
            }
            Map<String, Object> map = new HashMap<>();
            if (rptSearchCondition.getProductCategory() != 0) {
                productCategoryIds = Lists.newArrayList();
                productCategoryIds.add(rptSearchCondition.getProductCategory().longValue());
            }
            if (productCategoryIds.isEmpty() &&
                    (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue())) {
            } else {
                msGradedOrderRptService.createKefuCompletedRptExportTask(rptSearchCondition.getSubFlag(),rptSearchCondition.getKefuId(), rptSearchCondition.getSelectedYear(),rptSearchCondition.getSelectedMonth(), productCategoryIds,user);
            }
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
    @RequestMapping(value = "provinceCompletedExport", method = RequestMethod.POST)
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
            msGradedOrderRptService.createAreaCompletedRptExportTask(rptSearchCondition.getAreaId(),type,rptSearchCondition.getSelectedYear(),rptSearchCondition.getSelectedMonth(),rptSearchCondition.getCustomerId(), productCategoryIds, 1,user);
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
    @RequestMapping(value = "cityCompletedExport", method = RequestMethod.POST)
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
            msGradedOrderRptService.createAreaCompletedRptExportTask(rptSearchCondition.getAreaId(),type,rptSearchCondition.getSelectedYear(),rptSearchCondition.getSelectedMonth(),rptSearchCondition.getCustomerId(), productCategoryIds, 2,user);
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
    @RequestMapping(value = "areaCompletedExport", method = RequestMethod.POST)
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
            msGradedOrderRptService.createAreaCompletedRptExportTask(rptSearchCondition.getAreaId(),type,rptSearchCondition.getSelectedYear(),rptSearchCondition.getSelectedMonth(),rptSearchCondition.getCustomerId(), productCategoryIds, 3,user);
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
    @RequestMapping(value = "orderServicePointFeeExport", method = RequestMethod.POST)
    public AjaxJsonEntity orderServicePointFeeExport(RptSearchCondition rptSearchCondition) {
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
            if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()){
                if (productCategoryIds.isEmpty()) {
                    result.setSuccess(false);
                    result.setMessage("创建报表导出任务失败，请重试");
                    return result;
                }
            }
            List<String> dates = Splitter.onPattern("~") //[~|-]
                    .omitEmptyStrings()
                    .trimResults()
                    .splitToList(rptSearchCondition.getRemarks());

            rptSearchCondition.setBeginDate(DateUtils.parseDate(dates.get(0)));
            if (dates.size() > 1) {
                rptSearchCondition.setEndDate(DateUtils.parseDate(dates.get(1)));
            }
            msGradedOrderRptService.createOrderServicePointFeeRptExportTask(rptSearchCondition.getBeginDate(), rptSearchCondition.getEndDate(),rptSearchCondition.getServicePointId(), productCategoryIds, user);
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
    @RequestMapping(value = "developAverageFeeExport", method = RequestMethod.POST)
    public AjaxJsonEntity developAverageFeeExport(RptSearchCondition rptSearchCondition) {
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
            if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()){
                if (productCategoryIds.isEmpty()) {
                    result.setSuccess(false);
                    result.setMessage("创建报表导出任务失败，请重试");
                    return result;
                }
            }
            Date beginDate = DateUtils.getDateStart(rptSearchCondition.getEndDate());
            Date endDate = DateUtils.getDateEnd(rptSearchCondition.getEndDate());
            msGradedOrderRptService.createDevelopAverageRptExportTask(beginDate, endDate, productCategoryIds, user);
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

