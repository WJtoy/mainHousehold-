package com.wolfking.jeesite.ms.providerrpt.controller;


import com.google.common.collect.Lists;
import com.kkl.kklplus.entity.rpt.RPTCrushAreaEntity;
import com.kkl.kklplus.entity.rpt.RPTSpecialChargeAreaEntity;
import com.kkl.kklplus.entity.rpt.exception.RPTBaseException;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.md.entity.ProductCategory;
import com.wolfking.jeesite.modules.md.utils.ProductUtils;
import com.wolfking.jeesite.modules.rpt.entity.RptSearchCondition;
import com.wolfking.jeesite.modules.rpt.web.BaseRptController;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.SystemService;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providerrpt.service.MSCrushAreaRptService;
import com.wolfking.jeesite.ms.providerrpt.service.MSSpecialChargeAreaRptService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
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

@Controller
@RequestMapping(value = "${adminPath}/rpt/provider/crushArea/")
public class MSCrushAreaController extends BaseRptController {

    @Autowired
    private MSCrushAreaRptService msCrushAreaRptService;

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

    @RequiresPermissions("rpt:crushAreaQty:view")
    @RequestMapping(value = "crushAreaList")
    public String crushAreaListRpt(RptSearchCondition rptSearchCondition, Model model) {
        List<ProductCategory> productCategoryList = ProductUtils.getProductCategoryRPTList();
        User user = UserUtils.getUser();
        List<Long> productCategoryIds = Lists.newArrayList();
        if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()) {
            productCategoryIds =  systemService.getAuthorizedProductCategoryIds(user.getId());
            List<Long> finalProductCategoryIds = productCategoryIds;
            productCategoryList = productCategoryList.stream().filter(r->finalProductCategoryIds.contains(r.getId())).collect(Collectors.toList());
        }
        if (rptSearchCondition.isSearching()) {
            List<RPTCrushAreaEntity> result = Lists.newArrayList();
            if (rptSearchCondition.getProductCategory() != 0) {
                productCategoryIds = Lists.newArrayList();
                productCategoryIds.add(rptSearchCondition.getProductCategory().longValue());
            }
            if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()) {
                if(productCategoryIds.isEmpty()){
                    model.addAttribute("list", result);
                }else{
                    result = msCrushAreaRptService.getCrushAreaList(rptSearchCondition.getSelectedYear(),
                            rptSearchCondition.getSelectedMonth(),productCategoryIds);
                    model.addAttribute("list", result);
                }
            }else {
                result = msCrushAreaRptService.getCrushAreaList(rptSearchCondition.getSelectedYear(),
                        rptSearchCondition.getSelectedMonth(),productCategoryIds);
                model.addAttribute("list", result);
            }
        }

        int days = DateUtils.getDaysOfMonth(DateUtils.parseDate(rptSearchCondition.getSelectedYear() + "-" + rptSearchCondition.getSelectedMonth() + "-01"));
        model.addAttribute("productCategoryList",productCategoryList);
        rptSearchCondition.setDays(days);

        return "modules/providerrpt/msCrushAreaRpt";
    }

    @ResponseBody
    @RequiresPermissions("rpt:crushAreaQty:export")
    @RequestMapping(value = "checkExportTask", method = RequestMethod.POST)
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
            if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()){
                if (productCategoryIds.isEmpty()) {
                    result.setSuccess(false);
                    result.setMessage("创建报表导出任务失败，请重试");
                    return result;
                }
            }
            Date queryDate = DateUtils.getDate(rptSearchCondition.getSelectedYear(), rptSearchCondition.getSelectedMonth(), 1);
            String yearMonth = DateUtils.getYearMonth(queryDate);
            msCrushAreaRptService.checkRptExportTask(Integer.parseInt(yearMonth), productCategoryIds, user);

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
    @RequiresPermissions("rpt:crushAreaQty:export")
    @RequestMapping(value = "Export", method = RequestMethod.POST)
    public AjaxJsonEntity countyExport(RptSearchCondition rptSearchCondition) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try {
            User user = UserUtils.getUser();
            List<Long> productCategoryIds = Lists.newArrayList();
            Date queryDate = DateUtils.getDate(rptSearchCondition.getSelectedYear(), rptSearchCondition.getSelectedMonth(), 1);
            String yearMonth = DateUtils.getYearMonth(queryDate);
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
            msCrushAreaRptService.createRptExportTask(Integer.parseInt(yearMonth), productCategoryIds, user);
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

