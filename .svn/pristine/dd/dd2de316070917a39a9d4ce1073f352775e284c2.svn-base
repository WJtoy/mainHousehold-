package com.wolfking.jeesite.modules.finance.rpt.web;

import com.google.common.collect.Lists;
import com.kkl.kklplus.entity.rpt.RPTCustomerReceivableSummaryEntity;
import com.kkl.kklplus.entity.rpt.exception.RPTBaseException;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.finance.rpt.service.FiCustomerReceivableSummaryRptService;
import com.wolfking.jeesite.modules.md.entity.ProductCategory;
import com.wolfking.jeesite.modules.md.utils.ProductUtils;
import com.wolfking.jeesite.modules.rpt.entity.RptSearchCondition;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.SystemService;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providerrpt.service.MSCustomerReceivableSummaryRptService;
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

@Controller
@RequestMapping(value = "${adminPath}/finance/rpt/customerReceivableSummary/")
public class FiCustomerReceivableSummaryRptController {

    @Autowired
    private FiCustomerReceivableSummaryRptService fiCustomerReceivableSummaryRptService;

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

    @RequiresPermissions("rpt:finance:customerReceivableSummary:view")
    @RequestMapping(value = {"customerReceivableSummaryRptNewer"})
    public String customerReceivableSummaryRptNewer(RptSearchCondition rptSearchCondition,
                                                    HttpServletRequest request, HttpServletResponse response, Model model) {

        Page<RPTCustomerReceivableSummaryEntity> page = new Page<>(request, response);
        Integer paymentType = (rptSearchCondition.getPaymentType() == null || rptSearchCondition.getPaymentType().equals("")) ?
                null :
                StringUtils.toInteger(rptSearchCondition.getPaymentType());
        User user = UserUtils.getUser();
        List<Long> productCategoryIds = Lists.newArrayList();
        List<ProductCategory> productCategoryList = ProductUtils.getProductCategoryRPTList();
        if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()) {
            productCategoryIds = systemService.getAuthorizedProductCategoryIds(user.getId());
            List<Long> finalProductCategoryIds = productCategoryIds;
            productCategoryList = productCategoryList.stream().filter(r -> finalProductCategoryIds.contains(r.getId())).collect(Collectors.toList());
        }
        boolean result = true;
        if (rptSearchCondition.isSearching()) {
            if (rptSearchCondition.getProductCategory() != 0) {
                productCategoryIds = Lists.newArrayList();
                productCategoryIds.add(rptSearchCondition.getProductCategory().longValue());
            }
            if (productCategoryIds.isEmpty() &&
                    (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue())) {
            } else {
                page = fiCustomerReceivableSummaryRptService.getCustomerReceivableSummaryRpt(page,rptSearchCondition.getCustomerId(),
                        rptSearchCondition.getSelectedYear(),
                        rptSearchCondition.getSelectedMonth(),
                        paymentType, productCategoryIds);
            }
        }
        if (productCategoryIds.size() > 0) {
            result = false;
        }

        model.addAttribute("result", result);
        model.addAttribute("page", page);
        model.addAttribute("productCategoryList", productCategoryList);
        return "modules/finance/rpt/fiCustomerReceivableSummaryRpt";
    }


    @ResponseBody
    @RequiresPermissions("rpt:finance:customerReceivableSummary:export")
    @RequestMapping(value = "checkExportTask", method = RequestMethod.POST)
    public AjaxJsonEntity checkExportTask(RptSearchCondition rptSearchCondition) {
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
            if (rptSearchCondition.isSearching()) {
                if (rptSearchCondition.getProductCategory() != 0) {
                    productCategoryIds = Lists.newArrayList();
                    productCategoryIds.add(rptSearchCondition.getProductCategory().longValue());
                }
                if (productCategoryIds.isEmpty() &&
                        (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue())) {
                } else {
                    fiCustomerReceivableSummaryRptService.checkRptExportTask(rptSearchCondition.getCustomerId(), rptSearchCondition.getSelectedYear(),
                            rptSearchCondition.getSelectedMonth(), paymentType, productCategoryIds,user);
                }
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
    @RequiresPermissions("rpt:finance:customerReceivableSummary:export")
    @RequestMapping(value = "export", method = RequestMethod.POST)
    public AjaxJsonEntity export(RptSearchCondition rptSearchCondition) {
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
            if (rptSearchCondition.isSearching()) {
                if (rptSearchCondition.getProductCategory() != 0) {
                    productCategoryIds = Lists.newArrayList();
                    productCategoryIds.add(rptSearchCondition.getProductCategory().longValue());
                }
                if (productCategoryIds.isEmpty() &&
                        (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue())) {
                } else {
                    fiCustomerReceivableSummaryRptService.createRptExportTask(rptSearchCondition.getCustomerId(), rptSearchCondition.getSelectedYear(),
                            rptSearchCondition.getSelectedMonth(), paymentType, productCategoryIds,user);
                }
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
}
