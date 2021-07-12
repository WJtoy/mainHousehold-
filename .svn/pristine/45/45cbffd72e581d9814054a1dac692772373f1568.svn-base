package com.wolfking.jeesite.modules.customer.rpt.web;

import com.google.common.collect.Lists;
import com.kkl.kklplus.entity.rpt.RPTCustomerOrderPlanDailyEntity;
import com.kkl.kklplus.entity.rpt.exception.RPTBaseException;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.customer.rpt.service.CtCustomerOrderPlanDailyRptService;
import com.wolfking.jeesite.modules.md.entity.ProductCategory;
import com.wolfking.jeesite.modules.md.utils.ProductUtils;
import com.wolfking.jeesite.modules.rpt.entity.RptSearchCondition;
import com.wolfking.jeesite.modules.rpt.web.BaseRptController;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.SystemService;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providerrpt.service.MSCustomerOrderPlanDailyRptService;
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
@RequestMapping(value = "${adminPath}/customer/rpt/customerOrderPlan/")
public class CtCustomerOrderPlanDailyRptController extends BaseRptController {
    @Autowired
    private CtCustomerOrderPlanDailyRptService ctCustomerOrderPlanDailyRptService;

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
    @RequiresPermissions("rpt:customer:customerOrderPlanDaily:view")
    @RequestMapping(value = "customerOrderPlanDailyReport")
    public String customerOrderPlanDailyReport(RptSearchCondition rptSearchCondition, Model model) {

        List<RPTCustomerOrderPlanDailyEntity> list = Lists.newArrayList();
        User user = UserUtils.getUser();
        List<Long> productCategoryIds = Lists.newArrayList();
        List<ProductCategory> productCategoryList = ProductUtils.getProductCategoryRPTList();
        Long salesId = rptSearchCondition.getSalesId();
        Long customerId ;
        if (rptSearchCondition.isSearching()) {
            if (user.isCustomer()) {
                customerId = UserUtils.getUser().getCustomerAccountProfile().getCustomer().getId();
            }else{
                return "modules/customer/rpt/ctCustomerOrderPlanDailyReport";
            }
            if (rptSearchCondition.getProductCategory() != 0) {
                productCategoryIds = Lists.newArrayList();
                productCategoryIds.add(rptSearchCondition.getProductCategory().longValue());
            }

                list = ctCustomerOrderPlanDailyRptService.getCustomerOrderPlanDailyList(rptSearchCondition.getSelectedYear(),
                        rptSearchCondition.getSelectedMonth(),salesId,customerId,productCategoryIds);
        }

        int days = DateUtils.getDaysOfMonth(DateUtils.parseDate(rptSearchCondition.getSelectedYear() + "-" + rptSearchCondition.getSelectedMonth() + "-01"));

        rptSearchCondition.setDays(days);
        rptSearchCondition.setList(list);

        model.addAttribute("productCategoryList", productCategoryList);
        return "modules/customer/rpt/ctCustomerOrderPlanDailyReport";
    }

    @SuppressWarnings("deprecation")
    @RequiresPermissions("rpt:customer:customerOrderPlanDaily:view")
    @RequestMapping(value = "customerOrderPlanDailyChart")
    public String customerOrderPlanDailyChart(RptSearchCondition rptSearchCondition, Model model) {
        User user = UserUtils.getUser();
        Long customerId;
        Long salesId = rptSearchCondition.getSalesId();
        List<Long> productCategoryIds = Lists.newArrayList();
        List<ProductCategory> productCategoryList = ProductUtils.getProductCategoryRPTList();

        Map<String, Object> map = new HashMap<>();

        if (user.isCustomer()) {
            customerId = UserUtils.getUser().getCustomerAccountProfile().getCustomer().getId();
        }else{
            return "modules/customer/rpt/ctCustomerOrderPlanDailyChart";
        }
        if (rptSearchCondition.getProductCategory() != 0) {
            productCategoryIds = Lists.newArrayList();
            productCategoryIds.add(rptSearchCondition.getProductCategory().longValue());
        }

        map = ctCustomerOrderPlanDailyRptService.getCustomerOrderPlanDailyChartList(rptSearchCondition.getSelectedYear(),
                    rptSearchCondition.getSelectedMonth(),salesId,customerId,productCategoryIds);

        model.addAttribute("list", map.get("list"));
        model.addAttribute("daySums", map.get("daySums"));
        model.addAttribute("createDates", map.get("createDates"));
        model.addAttribute("productCategoryList", productCategoryList);
        return "modules/customer/rpt/ctCustomerOrderPlanDailyChart";
    }
    @ResponseBody
    @RequiresPermissions("rpt:customer:customerOrderPlanDaily:export")
    @RequestMapping(value = "checkExportTask", method = RequestMethod.POST)
    public AjaxJsonEntity checkExportTask(RptSearchCondition rptSearchCondition) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try {

            User user = UserUtils.getUser();
            int selectedYear = rptSearchCondition.getSelectedYear();
            int selectedMonth = rptSearchCondition.getSelectedMonth();
            List<Long> productCategoryIds = Lists.newArrayList();
            Long customerId ;
            Long salesId = rptSearchCondition.getSalesId();
            if (user.isCustomer()) {
                customerId = UserUtils.getUser().getCustomerAccountProfile().getCustomer().getId();
            } else{
                result.setSuccess(false);
                result.setMessage("创建报表导出任务失败：权限不足");
                return result;
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
            ctCustomerOrderPlanDailyRptService.checkRptExportTask(selectedYear,selectedMonth,salesId,customerId,productCategoryIds,user);

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
    @RequiresPermissions("rpt:customer:customerOrderPlanDaily:export")
    @RequestMapping(value = "export", method = RequestMethod.POST)
    public AjaxJsonEntity export(RptSearchCondition rptSearchCondition) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try {

            User user = UserUtils.getUser();

            int selectedYear = rptSearchCondition.getSelectedYear();
            int selectedMonth = rptSearchCondition.getSelectedMonth();
            List<Long> productCategoryIds = Lists.newArrayList();
            Long customerId;
            Long salesId = rptSearchCondition.getSalesId();
            if (user.isCustomer()) {
                customerId = UserUtils.getUser().getCustomerAccountProfile().getCustomer().getId();
            } else{
                result.setSuccess(false);
                result.setMessage("创建报表导出任务失败：权限不足");
                return result;
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
            ctCustomerOrderPlanDailyRptService.createRptExportTask(selectedYear,selectedMonth,salesId,customerId,productCategoryIds,user);
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
