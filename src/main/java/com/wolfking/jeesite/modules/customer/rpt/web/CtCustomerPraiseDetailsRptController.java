package com.wolfking.jeesite.modules.customer.rpt.web;

import com.kkl.kklplus.entity.rpt.RPTKeFuPraiseDetailsEntity;
import com.kkl.kklplus.entity.rpt.exception.RPTBaseException;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.customer.rpt.service.CtCustomerPraiseDetailsRptService;
import com.wolfking.jeesite.modules.rpt.entity.RptSearchCondition;
import com.wolfking.jeesite.modules.rpt.web.BaseRptController;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providerrpt.service.MSCustomerPraiseDetailsRptService;
import com.wolfking.jeesite.ms.providerrpt.utils.MSRptUtils;
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

@Controller
@RequestMapping(value = "${adminPath}/customer/rpt/customerPraiseDetails/")
public class CtCustomerPraiseDetailsRptController extends BaseRptController {

    @Autowired
    private CtCustomerPraiseDetailsRptService ctCustomerPraiseDetailsRptService;

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
     * 客戶好评明细
     *
     * @param rptSearchCondition
     * @param model
     * @return
     */
    @RequiresPermissions("rpt:customer:customerPraiseDetails:view")
    @RequestMapping(value = "customerPraiseDetailsRptData")
    public String keFuPraiseDetailsRptData(RptSearchCondition rptSearchCondition, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<RPTKeFuPraiseDetailsEntity> page = new Page<>(request, response);
        rptSearchCondition.setBeginDate(DateUtils.getStartOfDay(rptSearchCondition.getBeginDate()));
        rptSearchCondition.setEndDate(DateUtils.getEndOfDay(rptSearchCondition.getEndDate()));
        User user = UserUtils.getUser();
        Long customerId;
        if (rptSearchCondition.isSearching()) {
            if(user.isCustomer()){
                customerId = UserUtils.getUser().getCustomerAccountProfile().getCustomer().getId();
                rptSearchCondition.setCustomerId(customerId);
            }else{
                return "modules/customer/rpt/ctCustomerOrderPlanDailyReport";
            }
            page = ctCustomerPraiseDetailsRptService.getPraiseOrderList(page,rptSearchCondition.getStatus(),rptSearchCondition.getCustomerId(),rptSearchCondition.getSalesId(), rptSearchCondition.getBeginDate(), rptSearchCondition.getEndDate());

        }

        model.addAttribute("page", page);
        model.addAttribute("praiseStatusEnumList", MSRptUtils.getAllPraiseTypeList());
        return "modules/customer/rpt/ctCustomerPraiseDetailsRpt";
    }

    @ResponseBody
    @RequiresPermissions("rpt:customer:customerPraiseDetails:export")
    @RequestMapping(value = "checkExportTask", method = RequestMethod.POST)
    public AjaxJsonEntity checkExportTask(RptSearchCondition rptSearchCondition) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try {
            User user = UserUtils.getUser();
            rptSearchCondition.setBeginDate(DateUtils.getStartOfDay(rptSearchCondition.getBeginDate()));
            rptSearchCondition.setEndDate(DateUtils.getEndOfDay(rptSearchCondition.getEndDate()));
            Long customerId;
            if(user.isCustomer()){
                customerId = UserUtils.getUser().getCustomerAccountProfile().getCustomer().getId();
                rptSearchCondition.setCustomerId(customerId);
            }else{
                result.setSuccess(false);
                result.setMessage("创建报表导出任务失败：权限不足");
                return result;
            }

            ctCustomerPraiseDetailsRptService.checkRptExportTask(rptSearchCondition.getStatus(),rptSearchCondition.getCustomerId(), rptSearchCondition.getSalesId(), rptSearchCondition.getBeginDate(), rptSearchCondition.getEndDate(),user);

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
    @RequiresPermissions("rpt:customer:customerPraiseDetails:export")
    @RequestMapping(value = "export", method = RequestMethod.POST)
    public AjaxJsonEntity export(RptSearchCondition rptSearchCondition) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try {

            User user = UserUtils.getUser();
            rptSearchCondition.setBeginDate(DateUtils.getStartOfDay(rptSearchCondition.getBeginDate()));
            rptSearchCondition.setEndDate(DateUtils.getEndOfDay(rptSearchCondition.getEndDate()));
            Long customerId;
            if(user.isCustomer()){
                customerId = UserUtils.getUser().getCustomerAccountProfile().getCustomer().getId();
                rptSearchCondition.setCustomerId(customerId);
            }else{
                result.setSuccess(false);
                result.setMessage("创建报表导出任务失败：权限不足");
                return result;
            }

            ctCustomerPraiseDetailsRptService.createRptExportTask(rptSearchCondition.getStatus(),rptSearchCondition.getCustomerId(),rptSearchCondition.getSalesId(), rptSearchCondition.getBeginDate(), rptSearchCondition.getEndDate(),user);
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
