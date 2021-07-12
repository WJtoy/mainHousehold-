package com.wolfking.jeesite.ms.providerrpt.controller;

import com.google.common.collect.Lists;
import com.kkl.kklplus.entity.md.GlobalMappingSalesSubFlagEnum;
import com.kkl.kklplus.entity.rpt.RPTCancelledOrderEntity;
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
import com.wolfking.jeesite.ms.providerrpt.service.MSCancelledOrderNewRptService;
import com.wolfking.jeesite.ms.providerrpt.service.MSCancelledOrderRptService;
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
@RequestMapping(value = "${adminPath}/rpt/provider/cancelledOrderNew/")
public class MSCancelledOrderNewRptController extends BaseRptController {

    @Autowired
    private MSCancelledOrderNewRptService msCancelledOrderNewRptService;

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
            rptSearchCondition.setBeginDate(now);
        }
        if (rptSearchCondition.getEndDate() == null) {
            rptSearchCondition.setEndDate(now);
        }

        return rptSearchCondition;
    }

    /**
     * 订单退单明细报表(新)
     *
     * @param rptSearchCondition
     * @param model
     * @return
     */
    @RequiresPermissions("rpt:cancelledOrderNew:view")
    @RequestMapping(value = "cancelledOrderNewReport")
    public String cancelledOrderReport(RptSearchCondition rptSearchCondition, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<RPTCancelledOrderEntity> page = new Page<>(request, response);
        if (rptSearchCondition.getBeginCancelApplyDate() == null) {
            rptSearchCondition.setEndCancelApplyDate(DateUtils.getStartOfDay(DateUtils.addDays(new Date(),-1)));
            rptSearchCondition.setBeginCancelApplyDate(DateUtils.getEndOfDay(DateUtils.addDays(new Date(),-1)));
        } else {
            rptSearchCondition.setEndCancelApplyDate(DateUtils.getDateEnd(rptSearchCondition.getEndCancelApplyDate()));
        }

        Integer paymentType = (rptSearchCondition.getPaymentType() == null || rptSearchCondition.getPaymentType().equals("")) ?
                null :
                StringUtils.toInteger(rptSearchCondition.getPaymentType());
        Integer cancelResponsibleId = (rptSearchCondition.getCancelResponsible() == null || rptSearchCondition.getCancelResponsible().equals("")) ?
                null :
                StringUtils.toInteger(rptSearchCondition.getCancelResponsible());
        User user = UserUtils.getUser();
        List<Long> productCategoryIds = Lists.newArrayList();
        List<ProductCategory> productCategoryList = ProductUtils.getProductCategoryRPTList();
        if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()) {
            productCategoryIds = systemService.getAuthorizedProductCategoryIds(user.getId());
            List<Long> finalProductCategoryIds = productCategoryIds;
            productCategoryList = productCategoryList.stream().filter(r -> finalProductCategoryIds.contains(r.getId())).collect(Collectors.toList());
        }
        if (rptSearchCondition.isSearching()) {

            Long salesId = rptSearchCondition.getSalesId();
            Integer subFlag=null;
            if (user.isSaleman()) {
                salesId = user.getId();
                if (user.getSubFlag()==GlobalMappingSalesSubFlagEnum.MANAGER.getValue()){
                    subFlag=GlobalMappingSalesSubFlagEnum.MANAGER.getValue();
                }
            }

            if (rptSearchCondition.getProductCategory() != 0) {
                productCategoryIds = Lists.newArrayList();
                productCategoryIds.add(rptSearchCondition.getProductCategory().longValue());
            }

            if (productCategoryIds.isEmpty() &&
                    (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue())) {

            } else {
                page = msCancelledOrderNewRptService.getCancelledOrderList(page, rptSearchCondition.getBeginCancelApplyDate(), rptSearchCondition.getEndCancelApplyDate(),
                        rptSearchCondition.getCustomerId(), paymentType, cancelResponsibleId, rptSearchCondition.getBeginPlanDate(), rptSearchCondition.getEndPlanDate(),productCategoryIds,salesId,subFlag);
            }
        }

        model.addAttribute("page", page);
        model.addAttribute("productCategoryList", productCategoryList);
        return "modules/providerrpt/cancelledOrderNewReport";
    }


    @ResponseBody
    @RequiresPermissions("rpt:cancelledOrderNew:export")
    @RequestMapping(value = "checkExportTask", method = RequestMethod.POST)
    public AjaxJsonEntity checkExportTask(RptSearchCondition rptSearchCondition) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try {
            User user = UserUtils.getUser();
            if (rptSearchCondition.getBeginCancelApplyDate() == null) {
                rptSearchCondition.setEndCancelApplyDate(DateUtils.getStartOfDay(DateUtils.addDays(new Date(),-1)));
                rptSearchCondition.setBeginCancelApplyDate(DateUtils.getEndOfDay(DateUtils.addDays(new Date(),-1)));
            } else {
                rptSearchCondition.setEndCancelApplyDate(DateUtils.getDateEnd(rptSearchCondition.getEndCancelApplyDate()));
            }
            Integer paymentType = (rptSearchCondition.getPaymentType() == null || rptSearchCondition.getPaymentType().equals("")) ?
                    null :
                    StringUtils.toInteger(rptSearchCondition.getPaymentType());
            Integer cancelResponsibleId = (rptSearchCondition.getCancelResponsible() == null || rptSearchCondition.getCancelResponsible().equals("")) ?
                    null :
                    StringUtils.toInteger(rptSearchCondition.getCancelResponsible());
            List<Long> productCategoryIds = Lists.newArrayList();
            if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()) {
                productCategoryIds =  systemService.getAuthorizedProductCategoryIds(user.getId());
            }
            if (rptSearchCondition.getProductCategory() != 0) {
                productCategoryIds = Lists.newArrayList();
                productCategoryIds.add(rptSearchCondition.getProductCategory().longValue());
            }
            Long salesId = rptSearchCondition.getSalesId();
            Integer subFlag=null;
            if (user.isSaleman()) {
                salesId = user.getId();
                if (user.getSubFlag() ==GlobalMappingSalesSubFlagEnum.MANAGER.getValue()) {
                    subFlag = GlobalMappingSalesSubFlagEnum.MANAGER.getValue();
                }
            }
            if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()){
                if (productCategoryIds.isEmpty()) {
                    result.setSuccess(false);
                    result.setMessage("创建报表导出任务失败，请重试");
                    return result;
                }
            }
            msCancelledOrderNewRptService.checkRptExportTask(rptSearchCondition.getBeginCancelApplyDate(), rptSearchCondition.getEndCancelApplyDate(),
                    rptSearchCondition.getCustomerId(), paymentType, cancelResponsibleId, rptSearchCondition.getBeginPlanDate(), rptSearchCondition.getEndPlanDate(),productCategoryIds, salesId,user,subFlag);
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
    @RequiresPermissions("rpt:cancelledOrderNew:export")
    @RequestMapping(value = "export", method = RequestMethod.POST)
    public AjaxJsonEntity export(RptSearchCondition rptSearchCondition) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try {
            User user = UserUtils.getUser();
            if (rptSearchCondition.getBeginCancelApplyDate() == null) {
                rptSearchCondition.setEndCancelApplyDate(DateUtils.getStartOfDay(DateUtils.addDays(new Date(),-1)));
                rptSearchCondition.setBeginCancelApplyDate(DateUtils.getEndOfDay(DateUtils.addDays(new Date(),-1)));
            } else {
                rptSearchCondition.setEndCancelApplyDate(DateUtils.getDateEnd(rptSearchCondition.getEndCancelApplyDate()));
            }
            Integer paymentType = (rptSearchCondition.getPaymentType() == null || rptSearchCondition.getPaymentType().equals("")) ?
                    null :
                    StringUtils.toInteger(rptSearchCondition.getPaymentType());
            Integer cancelResponsibleId = (rptSearchCondition.getCancelResponsible() == null || rptSearchCondition.getCancelResponsible().equals("")) ?
                    null :
                    StringUtils.toInteger(rptSearchCondition.getCancelResponsible());

            List<Long> productCategoryIds = Lists.newArrayList();
            if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()) {
                productCategoryIds =  systemService.getAuthorizedProductCategoryIds(user.getId());
            }
            if (rptSearchCondition.getProductCategory() != 0) {
                productCategoryIds = Lists.newArrayList();
                productCategoryIds.add(rptSearchCondition.getProductCategory().longValue());
            }
            Long salesId = rptSearchCondition.getSalesId();
            Integer subFlag = null;
            if (user.isSaleman()) {
                salesId = user.getId();
                if (user.getSubFlag() == GlobalMappingSalesSubFlagEnum.MANAGER.getValue()){
                    subFlag =GlobalMappingSalesSubFlagEnum.MANAGER.getValue();
                }
            }
            if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()){
                if (productCategoryIds.isEmpty()) {
                    result.setSuccess(false);
                    result.setMessage("创建报表导出任务失败，请重试");
                    return result;
                }
            }
            msCancelledOrderNewRptService.createRptExportTask(rptSearchCondition.getBeginCancelApplyDate(), rptSearchCondition.getEndCancelApplyDate(),
                    rptSearchCondition.getCustomerId(), paymentType, cancelResponsibleId, rptSearchCondition.getBeginPlanDate(), rptSearchCondition.getEndPlanDate(),productCategoryIds, salesId, user,subFlag);
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
