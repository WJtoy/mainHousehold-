package com.wolfking.jeesite.ms.providerrpt.controller;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.kkl.kklplus.entity.rpt.RPTCompletedOrderDetailsEntity;
import com.kkl.kklplus.entity.rpt.exception.RPTBaseException;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.md.entity.ProductCategory;
import com.wolfking.jeesite.modules.md.utils.ProductUtils;
import com.wolfking.jeesite.modules.rpt.entity.RptSearchCondition;
import com.wolfking.jeesite.modules.rpt.web.BaseRptController;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.SystemService;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providerrpt.service.MSCompletedOrderNewRptService;
import com.wolfking.jeesite.ms.providerrpt.service.MSCompletedOrderRptService;
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
@RequestMapping(value = "${adminPath}/rpt/provider/completedOrderNew/")
public class MSCompletedOrderNewRptController extends BaseRptController {

    @Autowired
    private MSCompletedOrderNewRptService msCompletedOrderNewRptService;

    @Autowired
    private SystemService systemService;


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
        if (rptSearchCondition.getBeginDate() == null) {
            rptSearchCondition.setBeginDate(DateUtils.addDays(now ,-1));
        }
        if (rptSearchCondition.getEndDate() == null) {
            rptSearchCondition.setEndDate(now);
        }

        return rptSearchCondition;
    }

    /**
     * 订单完工明细报表(财务专用)
     *
     * @param rptSearchCondition
     * @param model
     * @return
     */
    @RequiresPermissions("rpt:completedOrderNewReport:view")
    @RequestMapping(value = "completedOrderNewReport")
    public String completedOrderReport(RptSearchCondition rptSearchCondition, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<RPTCompletedOrderDetailsEntity> page = new Page<>(request, response);
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
        Integer paymentType = (rptSearchCondition.getPaymentType() == null || rptSearchCondition.getPaymentType().equals("")) ?
                null :
                StringUtils.toInteger(rptSearchCondition.getPaymentType());
        List<ProductCategory> productCategoryList = ProductUtils.getProductCategoryRPTList();
        User user = UserUtils.getUser();
        List<Long> productCategoryIds = Lists.newArrayList();
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
                page = msCompletedOrderNewRptService.getCompletedOrderList(page, rptSearchCondition.getCustomerId(), paymentType,productCategoryIds, rptSearchCondition.getBeginDate(), rptSearchCondition.getEndDate());
            }
        }

        model.addAttribute("page", page);
        model.addAttribute("productCategoryList", productCategoryList);
        return "modules/providerrpt/completedOrderNewReport";
    }


    @ResponseBody
    @RequiresPermissions("rpt:completedOrderNewReport:export")
    @RequestMapping(value = "checkExportTask", method = RequestMethod.POST)
    public AjaxJsonEntity checkExportTask(RptSearchCondition rptSearchCondition) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try {

            User user = UserUtils.getUser();
            Integer paymentType = (rptSearchCondition.getPaymentType() == null || rptSearchCondition.getPaymentType().equals("")) ?
                    null :
                    StringUtils.toInteger(rptSearchCondition.getPaymentType());
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
            }
            rptSearchCondition.setBeginDate(DateUtils.getStartOfDay(rptSearchCondition.getBeginDate()));
            rptSearchCondition.setEndDate(DateUtils.getEndOfDay(rptSearchCondition.getEndDate()));
            if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()){
                if (productCategoryIds.isEmpty()) {
                    result.setSuccess(false);
                    result.setMessage("创建报表导出任务失败，请重试");
                    return result;
                }
            }
            msCompletedOrderNewRptService.checkRptExportTask(rptSearchCondition.getCustomerId(), paymentType,productCategoryIds,rptSearchCondition.getBeginDate(), rptSearchCondition.getEndDate(), user);
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
    @RequiresPermissions("rpt:completedOrderNewReport:export")
    @RequestMapping(value = "export", method = RequestMethod.POST)
    public AjaxJsonEntity export(RptSearchCondition rptSearchCondition) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try {
            User user = UserUtils.getUser();
            Integer paymentType = (rptSearchCondition.getPaymentType() == null || rptSearchCondition.getPaymentType().equals("")) ?
                    null :
                    StringUtils.toInteger(rptSearchCondition.getPaymentType());
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
            }
            rptSearchCondition.setBeginDate(DateUtils.getStartOfDay(rptSearchCondition.getBeginDate()));
            rptSearchCondition.setEndDate(DateUtils.getEndOfDay(rptSearchCondition.getEndDate()));
            if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()){
                if (productCategoryIds.isEmpty()) {
                    result.setSuccess(false);
                    result.setMessage("创建报表导出任务失败，请重试");
                    return result;
                }
            }
            msCompletedOrderNewRptService.createRptExportTask(rptSearchCondition.getCustomerId(), paymentType,productCategoryIds,rptSearchCondition.getBeginDate(), rptSearchCondition.getEndDate(), user);
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
