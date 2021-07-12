package com.wolfking.jeesite.ms.providerrpt.controller;

import com.google.common.collect.Lists;
import com.kkl.kklplus.entity.md.GlobalMappingSalesSubFlagEnum;
import com.kkl.kklplus.entity.rpt.RPTCustomerOrderPlanDailyEntity;
import com.kkl.kklplus.entity.rpt.exception.RPTBaseException;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.md.entity.ProductCategory;
import com.wolfking.jeesite.modules.md.utils.ProductUtils;
import com.wolfking.jeesite.modules.rpt.entity.RptSearchCondition;
import com.wolfking.jeesite.modules.rpt.web.BaseRptController;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.SystemService;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providerrpt.service.MSCustomerOrderPlanDailyRptService;
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
@RequestMapping(value = "${adminPath}/rpt/provider/customerOrderPlan/")
public class MSCustomerOrderPlanDailyRptController extends BaseRptController {
    @Autowired
    private MSCustomerOrderPlanDailyRptService msCustomerOrderPlanDailyRptService;

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
     *   客户每日下单明细
     */
    @RequestMapping(value = "customerOrderPlanDailyReport")
    public String customerOrderPlanDailyReport(RptSearchCondition rptSearchCondition, Model model) {

        Integer paymentType = (rptSearchCondition.getPaymentType() == null || rptSearchCondition.getPaymentType().equals("")) ?
                null :
                StringUtils.toInteger(rptSearchCondition.getPaymentType());
        List<RPTCustomerOrderPlanDailyEntity> list = Lists.newArrayList();
        User user = UserUtils.getUser();
        List<Long> productCategoryIds = Lists.newArrayList();
        List<ProductCategory> productCategoryList = ProductUtils.getProductCategoryRPTList();
        if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()) {
            productCategoryIds = systemService.getAuthorizedProductCategoryIds(user.getId());
            List<Long> finalProductCategoryIds = productCategoryIds;
            productCategoryList = productCategoryList.stream().filter(r -> finalProductCategoryIds.contains(r.getId())).collect(Collectors.toList());
        }
        Integer subFlag = null;
        if (rptSearchCondition.isSearching()) {
            //如果是业务员，则只显示业务员负责的客户
            Long customerId = rptSearchCondition.getCustomerId();
            Long salesId = rptSearchCondition.getSalesId();

            if (user.isCustomer()) {
                customerId = UserUtils.getUser().getCustomerAccountProfile().getCustomer().getId();
            } else if (user.isSaleman()) {
                salesId = user.getId();
                if (user.getSubFlag()== GlobalMappingSalesSubFlagEnum.MANAGER.getValue()){
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
                list = msCustomerOrderPlanDailyRptService.getCustomerOrderPlanDailyList(rptSearchCondition.getSelectedYear(),
                        rptSearchCondition.getSelectedMonth(), salesId,customerId,productCategoryIds,subFlag);
            }
        }

        int days = DateUtils.getDaysOfMonth(DateUtils.parseDate(rptSearchCondition.getSelectedYear() + "-" + rptSearchCondition.getSelectedMonth() + "-01"));

        rptSearchCondition.setDays(days);
        rptSearchCondition.setList(list);

        model.addAttribute("productCategoryList", productCategoryList);
        return "modules/providerrpt/customerOrderPlanDailyReport";
    }

    @SuppressWarnings("deprecation")
    @RequestMapping(value = "customerOrderPlanDailyChart")
    public String customerOrderPlanDailyChart(RptSearchCondition rptSearchCondition, Model model) {
        Integer paymentType = (rptSearchCondition.getPaymentType() == null || rptSearchCondition.getPaymentType().equals("")) ?
                null :
                StringUtils.toInteger(rptSearchCondition.getPaymentType());
        User user = UserUtils.getUser();
        Long customerId = rptSearchCondition.getCustomerId();
        Long salesId = rptSearchCondition.getSalesId();
        Integer subFlag = null;
        List<Long> productCategoryIds = Lists.newArrayList();
        List<ProductCategory> productCategoryList = ProductUtils.getProductCategoryRPTList();
        if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()) {
            productCategoryIds = systemService.getAuthorizedProductCategoryIds(user.getId());
            List<Long> finalProductCategoryIds = productCategoryIds;
            productCategoryList = productCategoryList.stream().filter(r -> finalProductCategoryIds.contains(r.getId())).collect(Collectors.toList());
        }
        Map<String, Object> map = new HashMap<>();
        //如果是业务员，则只显示业务员负责的客户
        if (user.isCustomer()) {
            customerId = UserUtils.getUser().getCustomerAccountProfile().getCustomer().getId();
        }else if (user.isSaleman()) {
            salesId = user.getId();
            if (user.getSubFlag() == GlobalMappingSalesSubFlagEnum.MANAGER.getValue()){
                subFlag =GlobalMappingSalesSubFlagEnum.MANAGER.getValue();
            }
        }
        if (rptSearchCondition.getProductCategory() != 0) {
            productCategoryIds = Lists.newArrayList();
            productCategoryIds.add(rptSearchCondition.getProductCategory().longValue());
        }
        if (productCategoryIds.isEmpty() &&
                (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue())) {
        } else {
            map = msCustomerOrderPlanDailyRptService.getCustomerOrderPlanDailyChartList(rptSearchCondition.getSelectedYear(),
                    rptSearchCondition.getSelectedMonth(),salesId,customerId,productCategoryIds,subFlag);
        }
        model.addAttribute("list", map.get("list"));
        model.addAttribute("daySums", map.get("daySums"));
        model.addAttribute("createDates", map.get("createDates"));
        model.addAttribute("productCategoryList", productCategoryList);
        return "modules/providerrpt/customerOrderPlanDailyChart";
    }
    @ResponseBody
    @RequestMapping(value = "checkExportTask", method = RequestMethod.POST)
    public AjaxJsonEntity checkExportTask(RptSearchCondition rptSearchCondition) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try {

            User user = UserUtils.getUser();
            int selectedYear = rptSearchCondition.getSelectedYear();
            int selectedMonth = rptSearchCondition.getSelectedMonth();
            List<Long> productCategoryIds = Lists.newArrayList();
            Long customerId = rptSearchCondition.getCustomerId();
            Long salesId = rptSearchCondition.getSalesId();
            Integer subFlag = null;
            if (user.isCustomer()) {
                customerId = UserUtils.getUser().getCustomerAccountProfile().getCustomer().getId();
            } else if (user.isSaleman()) {
                salesId = user.getId();
                if (user.getSubFlag() == GlobalMappingSalesSubFlagEnum.MANAGER.getValue()){
                    subFlag =  GlobalMappingSalesSubFlagEnum.MANAGER.getValue();
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
            msCustomerOrderPlanDailyRptService.checkRptExportTask(selectedYear,selectedMonth,salesId,customerId,productCategoryIds,user,subFlag);

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

            int selectedYear = rptSearchCondition.getSelectedYear();
            int selectedMonth = rptSearchCondition.getSelectedMonth();
            List<Long> productCategoryIds = Lists.newArrayList();
            Long customerId = rptSearchCondition.getCustomerId();
            Long salesId = rptSearchCondition.getSalesId();
            Integer subFlag = null;
            if (user.isCustomer()) {
                customerId = UserUtils.getUser().getCustomerAccountProfile().getCustomer().getId();
            } else if (user.isSaleman()) {
                salesId = user.getId();
                if (user.getSubFlag() == GlobalMappingSalesSubFlagEnum.MANAGER.getValue()){
                    subFlag =  GlobalMappingSalesSubFlagEnum.MANAGER.getValue();
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
            msCustomerOrderPlanDailyRptService.createRptExportTask(selectedYear,selectedMonth,salesId,customerId,productCategoryIds,user,subFlag);
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
