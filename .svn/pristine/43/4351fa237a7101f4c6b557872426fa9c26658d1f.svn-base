package com.wolfking.jeesite.ms.providerrpt.controller;


import com.google.common.collect.Lists;
import com.kkl.kklplus.entity.rpt.RPTTravelChargeRankEntity;
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
import com.wolfking.jeesite.ms.providerrpt.service.MSTravelChargeRankRptService;
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
@RequestMapping(value = "${adminPath}/rpt/provider/travelChargeRank/")
public class MSTravelChargeRankRptController extends BaseRptController {

    @Autowired
    private MSTravelChargeRankRptService msTravelChargeRankRptService;

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

    @RequestMapping(value = "getList")
    public String travelChargeRank(RptSearchCondition rptSearchCondition, HttpServletRequest request, HttpServletResponse response, Model model) {
        List<ProductCategory> productCategoryList = ProductUtils.getProductCategoryRPTList();
        Page<RPTTravelChargeRankEntity> page = new Page<>(request, response);
        page.setPageSize(100);
        Integer paymentType = (rptSearchCondition.getPaymentType() == null || rptSearchCondition.getPaymentType().equals("")) ?
                null : StringUtils.toInteger(rptSearchCondition.getPaymentType());
        User user = UserUtils.getUser();
        List<Long> productCategoryIds = Lists.newArrayList();
        model.addAttribute("userFlag",1);
        if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()) {
            productCategoryIds =  systemService.getAuthorizedProductCategoryIds(user.getId());
            List<Long> finalProductCategoryIds = productCategoryIds;
            productCategoryList = productCategoryList.stream().filter(r->finalProductCategoryIds.contains(r.getId())).collect(Collectors.toList());
            model.addAttribute("userFlag",2);
        }
        if (rptSearchCondition.isSearching()) {
            if (rptSearchCondition.getProductCategory() != 0) {
                productCategoryIds = Lists.newArrayList();
                productCategoryIds.add(rptSearchCondition.getProductCategory().longValue());
            }
            if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()) {
                if (productCategoryIds.isEmpty()) {
                    model.addAttribute("page", page);
                }else {
                    page = msTravelChargeRankRptService.getTravelChargeRankRptList(page,rptSearchCondition.getSelectedYear(), rptSearchCondition.getSelectedMonth(),
                            paymentType, rptSearchCondition.getAreaId(), rptSearchCondition.getServicePointNo(), rptSearchCondition.getServicePointName(), rptSearchCondition.getContactInfo(),
                            rptSearchCondition.getAppFlag(), rptSearchCondition.getFinishQty(), productCategoryIds);
                    model.addAttribute("page", page);
                }
            }else {
                page = msTravelChargeRankRptService.getTravelChargeRankRptList(page,rptSearchCondition.getSelectedYear(), rptSearchCondition.getSelectedMonth(),
                        paymentType, rptSearchCondition.getAreaId(), rptSearchCondition.getServicePointNo(), rptSearchCondition.getServicePointName(), rptSearchCondition.getContactInfo(),
                        rptSearchCondition.getAppFlag(), rptSearchCondition.getFinishQty(), productCategoryIds);
                model.addAttribute("page", page);

            }
        }

        model.addAttribute("productCategoryList",productCategoryList);
        return "modules/providerrpt/msTravelChargeRankRpt";
    }

    @ResponseBody
    @RequestMapping(value = "checkExportTask", method = RequestMethod.POST)
    public AjaxJsonEntity checkExportTask(RptSearchCondition rptSearchCondition) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try {
            Integer paymentType = (rptSearchCondition.getPaymentType() == null || rptSearchCondition.getPaymentType().equals("")) ?
                    null : StringUtils.toInteger(rptSearchCondition.getPaymentType());
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
            msTravelChargeRankRptService.checkRptExportTask(rptSearchCondition.getSelectedYear(),rptSearchCondition.getSelectedMonth(),paymentType
            ,rptSearchCondition.getAreaId(),rptSearchCondition.getServicePointNo(),rptSearchCondition.getServicePointName(),rptSearchCondition.getContactInfo(),
                    rptSearchCondition.getAppFlag(),rptSearchCondition.getFinishQty(),productCategoryIds,user
            );
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
            Integer paymentType = (rptSearchCondition.getPaymentType() == null || rptSearchCondition.getPaymentType().equals("")) ?
                    null : StringUtils.toInteger(rptSearchCondition.getPaymentType());
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
            msTravelChargeRankRptService.createRptExportTask(rptSearchCondition.getSelectedYear(),rptSearchCondition.getSelectedMonth(),paymentType
                    ,rptSearchCondition.getAreaId(),rptSearchCondition.getServicePointNo(),rptSearchCondition.getServicePointName(),rptSearchCondition.getContactInfo(),
                    rptSearchCondition.getAppFlag(),rptSearchCondition.getFinishQty(),productCategoryIds,user
            );
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

