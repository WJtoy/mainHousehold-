package com.wolfking.jeesite.ms.providerrpt.controller;

import com.google.common.collect.Lists;
import com.kkl.kklplus.entity.md.GlobalMappingSalesSubFlagEnum;
import com.kkl.kklplus.entity.rpt.RPTComplainStatisticsDailyEntity;
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
import com.wolfking.jeesite.ms.providerrpt.service.MSComplainStatisticsDailyRptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "${adminPath}/rpt/provider/complainStatistics/")
public class MSComplaintsStatisticsDailyRptController extends BaseRptController {

    @Autowired
    private MSComplainStatisticsDailyRptService msComplainStatisticsDailyRptService;

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

    @RequestMapping(value = "complainStatisticsDailyRpt")
    public String complainStatisticsDailyRpt(RptSearchCondition rptSearchCondition, Model model) {
        List<RPTComplainStatisticsDailyEntity> entityList = new ArrayList<>();
        User user = UserUtils.getUser();
        List<Long> productCategoryIds = Lists.newArrayList();
        Date date = DateUtils.addDays(new Date(), -1);
        List<ProductCategory> productCategoryList = ProductUtils.getProductCategoryRPTList();
        if (rptSearchCondition.getEndDate() == null) {
            rptSearchCondition.setEndDate(DateUtils.getEndOfDay(date));
        } else {
            rptSearchCondition.setEndDate(rptSearchCondition.getEndDate());
        }
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
                Long customerId = rptSearchCondition.getCustomerId();
                Long salesId = null;
                Integer subFlag = null;
                if (user.isCustomer()) {
                    customerId = UserUtils.getUser().getCustomerAccountProfile().getCustomer().getId();
                } else if (user.isSaleman()) {
                    salesId = user.getId();
                    if (user.getSubFlag()==GlobalMappingSalesSubFlagEnum.MANAGER.getValue() ){
                        subFlag = GlobalMappingSalesSubFlagEnum.MANAGER.getValue() ;
                    }
                }
                if (rptSearchCondition.getProductCategory() != 0) {
                    productCategoryIds = Lists.newArrayList();
                    productCategoryIds.add(rptSearchCondition.getProductCategory().longValue());
                }
                if (productCategoryIds.isEmpty() &&
                        (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue())) {
                } else {
                    entityList = msComplainStatisticsDailyRptService.getComplainStatisticsDaily(rptSearchCondition.getEndDate(), type, rptSearchCondition.getAreaId(), customerId, salesId, rptSearchCondition.getKefuId(), rptSearchCondition.getServicePointId(),productCategoryIds,subFlag);
                }

        }

        rptSearchCondition.setEndPlanDate(date);
        rptSearchCondition.setList(entityList);
        model.addAttribute("productCategoryList", productCategoryList);
        return "modules/providerrpt/complainStatisticsDailyRpt";
    }

    @RequestMapping(value = "complainStatisticsDailyChart")
    public String complainStatisticsDailyChart(RptSearchCondition rptSearchCondition, Model model) {
        Map<String, Object> map = new HashMap<>();
        User user = UserUtils.getUser();
        List<Long> productCategoryIds = Lists.newArrayList();
        Date date = DateUtils.addDays(new Date(), -1);
        List<ProductCategory> productCategoryList = ProductUtils.getProductCategoryRPTList();
        if (rptSearchCondition.getEndDate() == null) {
            rptSearchCondition.setEndDate(DateUtils.getEndOfDay(date));
        } else {
            rptSearchCondition.setEndDate(rptSearchCondition.getEndDate());
        }
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
            Long customerId = rptSearchCondition.getCustomerId();
            Long salesId = null;
            Integer subFlag = null;
            if (user.isCustomer()) {
                customerId = UserUtils.getUser().getCustomerAccountProfile().getCustomer().getId();
            } else if (user.isSaleman()) {
                salesId = user.getId();
                if (user.getSubFlag() == GlobalMappingSalesSubFlagEnum.MANAGER.getValue() ){
                    subFlag = GlobalMappingSalesSubFlagEnum.MANAGER.getValue() ;
                }
            }
            if (rptSearchCondition.getProductCategory() != 0) {
                productCategoryIds = Lists.newArrayList();
                productCategoryIds.add(rptSearchCondition.getProductCategory().longValue());
            }
            if (productCategoryIds.isEmpty() &&
                    (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue())) {
            } else {
                map = msComplainStatisticsDailyRptService.turnToChartInformationNew(rptSearchCondition.getEndDate(), type, rptSearchCondition.getAreaId(), customerId, salesId, rptSearchCondition.getKefuId(), rptSearchCondition.getServicePointId(),productCategoryIds,subFlag);
            }

        }

        rptSearchCondition.setEndPlanDate(date);
        model.addAttribute("createDates", map.get("createDates"));
        model.addAttribute("orderCreateDates", map.get("orderCreateDates"));
        model.addAttribute("strDayComplainSum", map.get("strDayComplainSum"));
        model.addAttribute("strDayComplainSumRate", map.get("strDayComplainSumRate"));
        model.addAttribute("rate", map.get("rate"));
        model.addAttribute("productCategoryList", productCategoryList);
        return "modules/providerrpt/complainStatisticsDailyChart";
    }

    @ResponseBody
    @RequestMapping(value = "checkExportTask", method = RequestMethod.POST)
    public AjaxJsonEntity checkExportTask(RptSearchCondition rptSearchCondition) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try {

            User user = UserUtils.getUser();
            List<Long> productCategoryIds = Lists.newArrayList();
            Date date = DateUtils.addDays(new Date(), -1);
            if (rptSearchCondition.getEndDate() == null) {
                rptSearchCondition.setEndDate(DateUtils.getEndOfDay(date));
            } else {
                rptSearchCondition.setEndDate(rptSearchCondition.getEndDate());
            }
            if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()) {
                productCategoryIds = systemService.getAuthorizedProductCategoryIds(user.getId());
            }

                Integer type = 0;
                if (rptSearchCondition.getAreaId() != null) {
                    Area area = areaService.getFromCache(rptSearchCondition.getAreaId());
                    type = area.getType();
                }
                Long customerId = rptSearchCondition.getCustomerId();
                Long salesId = null;
                Integer subFlag = null;
                if (user.isCustomer()) {
                    customerId = UserUtils.getUser().getCustomerAccountProfile().getCustomer().getId();
                } else if (user.isSaleman()) {
                    salesId = user.getId();
                    if (user.getSubFlag()==GlobalMappingSalesSubFlagEnum.MANAGER.getValue() ){
                        subFlag =GlobalMappingSalesSubFlagEnum.MANAGER.getValue() ;
                    }
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
            msComplainStatisticsDailyRptService.checkRptExportTask(rptSearchCondition.getEndDate(), type, rptSearchCondition.getAreaId(),
                    customerId, salesId, rptSearchCondition.getKefuId(), rptSearchCondition.getServicePointId(),productCategoryIds,user,subFlag);

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
    @RequestMapping(value = "export", method = RequestMethod.POST)
    public AjaxJsonEntity export(RptSearchCondition rptSearchCondition) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try {

            User user = UserUtils.getUser();
            List<Long> productCategoryIds = Lists.newArrayList();
            Date date = DateUtils.addDays(new Date(), -1);
            if (rptSearchCondition.getEndDate() == null) {
                rptSearchCondition.setEndDate(DateUtils.getEndOfDay(date));
            } else {
                rptSearchCondition.setEndDate(rptSearchCondition.getEndDate());
            }
            if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()) {
                productCategoryIds = systemService.getAuthorizedProductCategoryIds(user.getId());
            }

            Integer type = 0;
            if (rptSearchCondition.getAreaId() != null) {
                Area area = areaService.getFromCache(rptSearchCondition.getAreaId());
                type = area.getType();
            }
            Long customerId = rptSearchCondition.getCustomerId();
            Long salesId = null;
            Integer subFlag = null;
            if (user.isCustomer()) {
                customerId = UserUtils.getUser().getCustomerAccountProfile().getCustomer().getId();
            } else if (user.isSaleman()) {
                salesId = user.getId();
                if (user.getSubFlag() ==GlobalMappingSalesSubFlagEnum.MANAGER.getValue() ){
                    subFlag =GlobalMappingSalesSubFlagEnum.MANAGER.getValue();
                }
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
            msComplainStatisticsDailyRptService.createRptExportTask(rptSearchCondition.getEndDate(), type, rptSearchCondition.getAreaId(),
                    customerId, salesId, rptSearchCondition.getKefuId(), rptSearchCondition.getServicePointId(),productCategoryIds,user,subFlag);
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
