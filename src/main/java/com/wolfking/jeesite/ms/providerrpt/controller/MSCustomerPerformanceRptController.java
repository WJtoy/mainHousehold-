package com.wolfking.jeesite.ms.providerrpt.controller;

import com.google.common.collect.Lists;
import com.kkl.kklplus.entity.md.GlobalMappingSalesSubFlagEnum;
import com.kkl.kklplus.entity.rpt.RPTSalesPerfomanceEntity;
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
import com.wolfking.jeesite.ms.providerrpt.service.MSCustomerPerformanceRptService;
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
@RequestMapping(value = "${adminPath}/rpt/provider/customerPerformance/")
public class MSCustomerPerformanceRptController extends BaseRptController {

    @Autowired
    private MSCustomerPerformanceRptService msCustomerPerformanceRptService;

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

    @RequestMapping(value = "salesPerformanceReport")
    public String salesPerformanceReport(RptSearchCondition rptSearchCondition, Model model) {
        List<RPTSalesPerfomanceEntity> list = Lists.newArrayList();
        User user = UserUtils.getUser();
        Long salesId = 0L;
        Integer subFlag= null;
        List<Long> productCategoryIds = Lists.newArrayList();
        List<ProductCategory> productCategoryList = ProductUtils.getProductCategoryRPTList();
        if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()) {
            productCategoryIds = systemService.getAuthorizedProductCategoryIds(user.getId());
            List<Long> finalProductCategoryIds = productCategoryIds;
            productCategoryList = productCategoryList.stream().filter(r -> finalProductCategoryIds.contains(r.getId())).collect(Collectors.toList());
        }
        if (rptSearchCondition.isSearching()) {
            if (user.isSaleman()) {
                salesId = user.getId();
                if (user.getSubFlag() == GlobalMappingSalesSubFlagEnum.MANAGER.getValue()){
                    subFlag = GlobalMappingSalesSubFlagEnum.MANAGER.getValue();
                }
            }
            if (rptSearchCondition.getProductCategory() != 0) {
                productCategoryIds = Lists.newArrayList();
                productCategoryIds.add(rptSearchCondition.getProductCategory().longValue());
            }
            if (productCategoryIds.isEmpty() &&
                    (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue())) {
            } else {
                list = msCustomerPerformanceRptService.getSalesPerformanceMonthPlanDailyList(rptSearchCondition.getSelectedYear(), rptSearchCondition.getSelectedMonth(), productCategoryIds,salesId,subFlag);
            }

        }
        model.addAttribute("list", list);
        model.addAttribute("productCategoryList", productCategoryList);
        return "modules/providerrpt/customerPerformanceRpt";
    }

    /**
     * 业务员业绩明细报表
     *
     * @param rptSearchCondition
     * @param model
     * @return
     */
//    @SuppressWarnings("deprecation")
    @RequestMapping(value = "salesManAchievementDetail")
    public String salesManAchievementDetail(RptSearchCondition rptSearchCondition,Long salesId,Integer selectedYear,Integer selectedMonth,Integer productCategoryId,Model model) {
        List<RPTSalesPerfomanceEntity> list = Lists.newArrayList();
        User user = UserUtils.getUser();
        List<Long> productCategoryIds = Lists.newArrayList();
        List<ProductCategory> productCategoryList = ProductUtils.getProductCategoryRPTList();


        if (salesId != null && salesId != 0) {
            rptSearchCondition.setIsSearching(1);
            if (rptSearchCondition.getSelectedYear() == null) {
                rptSearchCondition.setSelectedYear(selectedYear);
            }
            if(rptSearchCondition.getSelectedMonth() == null){
                rptSearchCondition.setSelectedMonth(selectedMonth);
            }
            rptSearchCondition.setSalesId(salesId);
            rptSearchCondition.setProductCategory(productCategoryId);
        }else {
            salesId = user.getId();
            rptSearchCondition.setSalesId(salesId);
        }
        Integer subFlag = null;
        if (rptSearchCondition.isSearching()) {
            if (user.isSaleman() && (salesId == null || salesId == 0)) {
                salesId = user.getId();
                rptSearchCondition.setSalesId(salesId);
                if (user.getSubFlag() == GlobalMappingSalesSubFlagEnum.MANAGER.getValue()){
                    subFlag = GlobalMappingSalesSubFlagEnum.MANAGER.getValue();
                }
            }
            if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()) {
                productCategoryIds = systemService.getAuthorizedProductCategoryIds(user.getId());
                List<Long> finalProductCategoryIds = productCategoryIds;
                productCategoryList = productCategoryList.stream().filter(r -> finalProductCategoryIds.contains(r.getId())).collect(Collectors.toList());
            }

            if (rptSearchCondition.getProductCategory() != null && rptSearchCondition.getProductCategory() != 0 ) {
                productCategoryIds = Lists.newArrayList();
                productCategoryIds.add(rptSearchCondition.getProductCategory().longValue());
            }

            if (productCategoryIds.isEmpty() &&
                    (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue())) {
            } else {
                list = msCustomerPerformanceRptService.getSalesManAchievementRptDataNew(rptSearchCondition.getSelectedYear(),
                        rptSearchCondition.getSelectedMonth(), rptSearchCondition.getSalesId(),productCategoryIds,subFlag);
            }
        }

        model.addAttribute("list", list);
        model.addAttribute("productCategoryList", productCategoryList);
        return "modules/providerrpt/salesCustomerPerformanceRpt";
    }


    @ResponseBody
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
            msCustomerPerformanceRptService.checkRptExportTask(rptSearchCondition.getSelectedYear(),rptSearchCondition.getSelectedMonth(),productCategoryIds,user);

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
            msCustomerPerformanceRptService.createRptExportTask(rptSearchCondition.getSelectedYear(),rptSearchCondition.getSelectedMonth(),productCategoryIds,user);
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
    @RequestMapping(value = "customerCheckExportTask", method = RequestMethod.POST)
    public AjaxJsonEntity customerCheckExportTask(RptSearchCondition rptSearchCondition) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try {

            User user = UserUtils.getUser();
            List<Long> productCategoryIds = Lists.newArrayList();
            Long salesId = rptSearchCondition.getSalesId();
            Integer subFlag = null;
            if (user.isSaleman()) {
                salesId = user.getId();
                if (user.getSubFlag() == GlobalMappingSalesSubFlagEnum.MANAGER.getValue()){
                    subFlag = GlobalMappingSalesSubFlagEnum.MANAGER.getValue();
                }
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
            msCustomerPerformanceRptService.customerCheckRptExportTask(rptSearchCondition.getSelectedYear(),rptSearchCondition.getSelectedMonth(),salesId,productCategoryIds,user,subFlag);

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
    @RequestMapping(value = "customerExport", method = RequestMethod.POST)
    public AjaxJsonEntity customerExport(RptSearchCondition rptSearchCondition) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try {

            User user = UserUtils.getUser();
            Long salesId = rptSearchCondition.getSalesId();
            Integer subFlag= null;
            if (user.isSaleman()) {
                salesId = user.getId();
                if (user.getSubFlag() == GlobalMappingSalesSubFlagEnum.MANAGER.getValue()){
                    subFlag= GlobalMappingSalesSubFlagEnum.MANAGER.getValue();
                }
            }
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
            msCustomerPerformanceRptService.customerCreateRptExportTask(rptSearchCondition.getSelectedYear(),rptSearchCondition.getSelectedMonth(),salesId,productCategoryIds,user,subFlag);
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
