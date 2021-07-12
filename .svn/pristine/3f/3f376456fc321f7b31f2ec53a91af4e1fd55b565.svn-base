package com.wolfking.jeesite.ms.providerrpt.controller;


import com.google.common.collect.Lists;
import com.kkl.kklplus.entity.rpt.RPTServicePointPaySummaryEntity;
import com.kkl.kklplus.entity.rpt.exception.RPTBaseException;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.md.entity.ProductCategory;
import com.wolfking.jeesite.modules.md.utils.ProductUtils;
import com.wolfking.jeesite.modules.rpt.entity.RptSearchCondition;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.service.SystemService;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providerrpt.service.MSServicePointChargeRptService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
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
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping(value = "${adminPath}/rpt/provider/servicePointCharge/")
public class MSServicePointChargeRptController {

    @Autowired
    private MSServicePointChargeRptService msServicePointChargeRptService;

    @Autowired
    private SystemService systemService;

    @Autowired
    private AreaService areaService;

    /**
     * 获取报表的查询条件
     *
     * @param rptSearchCondition
     * @return
     */
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
     * 网点应付款汇总报表
     *
     * @param rptSearchCondition
     * @param model
     * @return
     */
    @RequestMapping(value = "servicePointPaySummaryReport")
    public String servicePointPaySummaryReport(RptSearchCondition rptSearchCondition, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<RPTServicePointPaySummaryEntity> page = new Page<>(request, response);
        Integer paymentType = (rptSearchCondition.getPaymentType() == null || rptSearchCondition.getPaymentType().equals("")) ?
                null :
                StringUtils.toInteger(rptSearchCondition.getPaymentType());
        List<Long> productCategoryIds = Lists.newArrayList();
        User user = UserUtils.getUser();
        List<ProductCategory> productCategoryList = ProductUtils.getProductCategoryRPTList();
        if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()) {
            productCategoryIds = systemService.getAuthorizedProductCategoryIds(user.getId());
            List<Long> finalProductCategoryIds = productCategoryIds;
            productCategoryList = productCategoryList.stream().filter(r -> finalProductCategoryIds.contains(r.getId())).collect(Collectors.toList());
        }
        if (rptSearchCondition.isSearching()) {
            productCategoryIds.add(rptSearchCondition.getProductCategory().longValue());
            if (rptSearchCondition.getProductCategory() != 0) {
                productCategoryIds = Lists.newArrayList();
                productCategoryIds.add(rptSearchCondition.getProductCategory().longValue());
            }
            if (productCategoryIds.isEmpty() &&
                    (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue())) {
            } else {
                page = msServicePointChargeRptService.getServicePointPaySummaryRptList(page, rptSearchCondition.getSelectedYear(), rptSearchCondition.getSelectedMonth(), paymentType,
                        rptSearchCondition.getServicePointId(), productCategoryIds);
            }
        }
        model.addAttribute("productCategoryList", productCategoryList);
        model.addAttribute("page", page);

        return "modules/providerrpt/servicePointPaySummaryReport";
    }


    @ResponseBody
    @RequestMapping(value = "checkPaySummaryExportTask", method = RequestMethod.POST)
    public AjaxJsonEntity checkPaySummaryExportTask(RptSearchCondition rptSearchCondition, Model model) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try {
            Integer paymentType = (rptSearchCondition.getPaymentType() == null || rptSearchCondition.getPaymentType().equals("")) ?
                    null :
                    StringUtils.toInteger(rptSearchCondition.getPaymentType());
            User user = UserUtils.getUser();
            List<Long> productCategoryIds = Lists.newArrayList();
            if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()) {
                productCategoryIds = systemService.getAuthorizedProductCategoryIds(user.getId());
            }
            productCategoryIds.add(rptSearchCondition.getProductCategory().longValue());
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

            msServicePointChargeRptService.checkPaySummaryRptExportTask(rptSearchCondition.getSelectedYear(), rptSearchCondition.getSelectedMonth(), paymentType,
                    rptSearchCondition.getServicePointId(), productCategoryIds, user);

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
    @RequestMapping(value = "paySummaryExport", method = RequestMethod.POST)
    public AjaxJsonEntity paySummaryExport(RptSearchCondition rptSearchCondition, Model model) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try {
            User user = UserUtils.getUser();
            Integer paymentType = (rptSearchCondition.getPaymentType() == null || rptSearchCondition.getPaymentType().equals("")) ?
                    null :
                    StringUtils.toInteger(rptSearchCondition.getPaymentType());
            List<Long> productCategoryIds = com.google.common.collect.Lists.newArrayList();
            if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()) {
                productCategoryIds = systemService.getAuthorizedProductCategoryIds(user.getId());
            }
            productCategoryIds.add(rptSearchCondition.getProductCategory().longValue());

            if (rptSearchCondition.getProductCategory() != 0) {
                productCategoryIds = com.google.common.collect.Lists.newArrayList();
                productCategoryIds.add(rptSearchCondition.getProductCategory().longValue());
            }
            if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()) {
                if (productCategoryIds.isEmpty()) {
                    result.setSuccess(false);
                    result.setMessage("创建报表导出任务失败，请重试");
                    return result;
                }
            }
            msServicePointChargeRptService.createPaySummaryRptExportTask(rptSearchCondition.getSelectedYear(), rptSearchCondition.getSelectedMonth(), paymentType,
                    rptSearchCondition.getServicePointId(), productCategoryIds, user);
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


    /**
     * 网点成本排名报表
     *
     * @param rptSearchCondition
     * @param model
     * @return
     */
    @RequestMapping(value = "servicePointCostPerReport")
    public String servicePointCostPerReport(RptSearchCondition rptSearchCondition, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<RPTServicePointPaySummaryEntity> page = new Page<>(request, response);
        Integer paymentType = (rptSearchCondition.getPaymentType() == null || rptSearchCondition.getPaymentType().equals("")) ?
                null :
                StringUtils.toInteger(rptSearchCondition.getPaymentType());


        List<Long> productCategoryIds = Lists.newArrayList();
        User user = UserUtils.getUser();
        List<ProductCategory> productCategoryList = ProductUtils.getProductCategoryRPTList();
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

            productCategoryIds.add(rptSearchCondition.getProductCategory().longValue());
            if (rptSearchCondition.getProductCategory() != 0) {
                productCategoryIds = Lists.newArrayList();
                productCategoryIds.add(rptSearchCondition.getProductCategory().longValue());
            }
            if (productCategoryIds.isEmpty() &&
                    (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue())) {
            } else {
                page = msServicePointChargeRptService.getServicePointCostPerRptList(page, rptSearchCondition.getSelectedYear(), rptSearchCondition.getSelectedMonth(), type, rptSearchCondition.getAreaId(), paymentType,
                        rptSearchCondition.getServicePointId(), rptSearchCondition.getAppFlag(), rptSearchCondition.getFinishQty(), productCategoryIds);
            }
        }
        model.addAttribute("page", page);
        model.addAttribute("productCategoryList", productCategoryList);
        return "modules/providerrpt/servicePointCostPerReport";
    }


    @ResponseBody
    @RequiresPermissions("rpt:order:servicePointCostPerOrderRptExport")
    @RequestMapping(value = "checkCostPerExportTask", method = RequestMethod.POST)
    public AjaxJsonEntity checkCostPerExportTask(RptSearchCondition rptSearchCondition, Model model) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try {
            Integer paymentType = (rptSearchCondition.getPaymentType() == null || rptSearchCondition.getPaymentType().equals("")) ?
                    null :
                    StringUtils.toInteger(rptSearchCondition.getPaymentType());
            User user = UserUtils.getUser();
            Integer type = 0;
            if (rptSearchCondition.getAreaId() != null) {
                Area area = areaService.getFromCache(rptSearchCondition.getAreaId());
                type = area.getType();
            }
            List<Long> productCategoryIds = com.google.common.collect.Lists.newArrayList();
            if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()) {
                productCategoryIds = systemService.getAuthorizedProductCategoryIds(user.getId());
            }
            productCategoryIds.add(rptSearchCondition.getProductCategory().longValue());

            if (rptSearchCondition.getProductCategory() != 0) {
                productCategoryIds = com.google.common.collect.Lists.newArrayList();
                productCategoryIds.add(rptSearchCondition.getProductCategory().longValue());
            }
            if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()) {
                if (productCategoryIds.isEmpty()) {
                    result.setSuccess(false);
                    result.setMessage("创建报表导出任务失败，请重试");
                    return result;
                }
            }
            msServicePointChargeRptService.checkCostPerRptExportTask(rptSearchCondition.getSelectedYear(), rptSearchCondition.getSelectedMonth(), type, rptSearchCondition.getAreaId(), paymentType,
                    rptSearchCondition.getServicePointId(), rptSearchCondition.getAppFlag(), rptSearchCondition.getFinishQty(), productCategoryIds, user);

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
    @RequiresPermissions("rpt:order:servicePointCostPerOrderRptExport")
    @RequestMapping(value = "costPerExport", method = RequestMethod.POST)
    public AjaxJsonEntity costPerExport(RptSearchCondition rptSearchCondition, Model model) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try {
            User user = UserUtils.getUser();
            Integer paymentType = (rptSearchCondition.getPaymentType() == null || rptSearchCondition.getPaymentType().equals("")) ?
                    null :
                    StringUtils.toInteger(rptSearchCondition.getPaymentType());
            Integer type = 0;
            if (rptSearchCondition.getAreaId() != null) {
                Area area = areaService.getFromCache(rptSearchCondition.getAreaId());
                type = area.getType();
            }
            List<Long> productCategoryIds = com.google.common.collect.Lists.newArrayList();
            if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()) {
                productCategoryIds = systemService.getAuthorizedProductCategoryIds(user.getId());
            }
            productCategoryIds.add(rptSearchCondition.getProductCategory().longValue());

            if (rptSearchCondition.getProductCategory() != 0) {
                productCategoryIds = com.google.common.collect.Lists.newArrayList();
                productCategoryIds.add(rptSearchCondition.getProductCategory().longValue());
            }
            if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()) {
                if (productCategoryIds.isEmpty()) {
                    result.setSuccess(false);
                    result.setMessage("创建报表导出任务失败，请重试");
                    return result;
                }
            }
            msServicePointChargeRptService.createCostPerRptExportTask(rptSearchCondition.getSelectedYear(), rptSearchCondition.getSelectedMonth(), type, rptSearchCondition.getAreaId(), paymentType,
                    rptSearchCondition.getServicePointId(), rptSearchCondition.getAppFlag(), rptSearchCondition.getFinishQty(), productCategoryIds, user);
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
