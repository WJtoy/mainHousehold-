package com.wolfking.jeesite.modules.finance.rpt.web;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kkl.kklplus.entity.rpt.RPTCustomerRevenueEntity;
import com.kkl.kklplus.entity.rpt.exception.RPTBaseException;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.finance.rpt.service.FiCustomerRevenueRptService;
import com.wolfking.jeesite.modules.md.entity.ProductCategory;
import com.wolfking.jeesite.modules.md.utils.ProductUtils;
import com.wolfking.jeesite.modules.rpt.entity.RptSearchCondition;
import com.wolfking.jeesite.modules.rpt.web.BaseRptController;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.SystemService;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providerrpt.service.MSCustomerRevenueRptService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "${adminPath}/finance/rpt/customerRevenue/")
public class FiCustomerRevenueRptController extends BaseRptController {

    @Autowired
    private FiCustomerRevenueRptService fiCustomerRevenueRptService;

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

    @SuppressWarnings("deprecation")
    @RequiresPermissions("rpt:finance:customerRevenueReport:view")
    @RequestMapping(value = "customerRevenueReport")
    public String customerRevenueReport(RptSearchCondition rptSearchCondition, Model model) {
        List<RPTCustomerRevenueEntity> list = new ArrayList<>();
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
                list = fiCustomerRevenueRptService.getCustomerRevenueRptList(rptSearchCondition.getCustomerId(), rptSearchCondition.getSelectedYear(), rptSearchCondition.getSelectedMonth(),productCategoryIds);
            }
        }
        rptSearchCondition.setList(list);
        if(rptSearchCondition.getStatus() == null){
            rptSearchCondition.setStatus(0);
        }
        model.addAttribute("productCategoryList", productCategoryList);
        model.addAttribute("status",rptSearchCondition.getStatus());
        return "modules/finance/rpt/fiCustomerRevenueReport";
    }

    /**
     * 客户营收排名
     *
     * @param rptSearchCondition
     * @param model
     * @return
     */
    @RequestMapping(value = "customerRevenueChart")
    public String customerRevenueChart(RptSearchCondition rptSearchCondition, Model model) {
        User user = UserUtils.getUser();
        List<Long> productCategoryIds = Lists.newArrayList();
        Map<String, Object> map = Maps.newHashMap();
        List<ProductCategory> productCategoryList = ProductUtils.getProductCategoryRPTList();
        if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()) {
            productCategoryIds = systemService.getAuthorizedProductCategoryIds(user.getId());
            List<Long> finalProductCategoryIds = productCategoryIds;
            productCategoryList = productCategoryList.stream().filter(r -> finalProductCategoryIds.contains(r.getId())).collect(Collectors.toList());
        }
        if (rptSearchCondition.getProductCategory() != 0) {
            productCategoryIds = Lists.newArrayList();
            productCategoryIds.add(rptSearchCondition.getProductCategory().longValue());
        }

        if (productCategoryIds.isEmpty() &&
                (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue())) {
        } else {
            map = fiCustomerRevenueRptService.getSMSQtyStatisticsChartList(rptSearchCondition.getCustomerId(), rptSearchCondition.getSelectedYear(), rptSearchCondition.getSelectedMonth(), productCategoryIds);
        }

        model.addAttribute("totalReceivables", map.get("totalReceivables"));
        model.addAttribute("finishOrders", map.get("finishOrders"));
        model.addAttribute("orderGrossProfits", map.get("orderGrossProfits"));
        model.addAttribute("everySingleGrossProfits", map.get("everySingleGrossProfits"));
        model.addAttribute("productCategoryList",productCategoryList);
        return "modules/finance/rpt/fiCustomerRevenueChart";
    }

    @ResponseBody
    @RequiresPermissions("rpt:finance:customerRevenueReport:export")
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
                productCategoryIds = org.assertj.core.util.Lists.newArrayList();
                productCategoryIds.add(rptSearchCondition.getProductCategory().longValue());
            }
            if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()){
                if (productCategoryIds.isEmpty()) {
                    result.setSuccess(false);
                    result.setMessage("创建报表导出任务失败，请重试");
                    return result;
                }
            }
            fiCustomerRevenueRptService.checkRptExportTask(rptSearchCondition.getCustomerId(),rptSearchCondition.getSelectedYear(),rptSearchCondition.getSelectedMonth(),productCategoryIds,user);

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
    @RequiresPermissions("rpt:finance:customerRevenueReport:export")
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
                productCategoryIds = org.assertj.core.util.Lists.newArrayList();
                productCategoryIds.add(rptSearchCondition.getProductCategory().longValue());
            }
            if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()){
                if (productCategoryIds.isEmpty()) {
                    result.setSuccess(false);
                    result.setMessage("创建报表导出任务失败，请重试");
                    return result;
                }
            }
            fiCustomerRevenueRptService.createRptExportTask(rptSearchCondition.getCustomerId(),rptSearchCondition.getSelectedYear(),rptSearchCondition.getSelectedMonth(),productCategoryIds,user);
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
