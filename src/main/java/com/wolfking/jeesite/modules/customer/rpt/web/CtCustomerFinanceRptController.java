package com.wolfking.jeesite.modules.customer.rpt.web;

import com.kkl.kklplus.entity.rpt.RPTCustomerFinanceEntity;
import com.kkl.kklplus.entity.rpt.exception.RPTBaseException;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.modules.customer.rpt.service.CtCustomerFinanceRptService;
import com.wolfking.jeesite.modules.rpt.entity.RptSearchCondition;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providerrpt.service.MSCustomerFinanceRptService;
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

@Controller
@RequestMapping(value = "${adminPath}/customer/rpt/customerFinance/")
public class CtCustomerFinanceRptController {
    @Autowired
    private CtCustomerFinanceRptService ctCustomerFinanceRptService;

    @ModelAttribute("rptSearchCondition")
    public RptSearchCondition get(@ModelAttribute("rptSearchCondition") RptSearchCondition rptSearchCondition) {
        if (rptSearchCondition == null) {
            rptSearchCondition = new RptSearchCondition();
        }
        return rptSearchCondition;
    }
    /**
     * 客户账户余额报表
     */
  //  @RequiresPermissions("rpt:customerFinance:view")
    @RequiresPermissions("rpt:customer:customerFinance:view")
    @RequestMapping(value = "customerFinanceReport")
    public String customerFinanceReport(RptSearchCondition rptSearchCondition, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<RPTCustomerFinanceEntity> page = new Page<>(request, response);
        User user = UserUtils.getUser();
        Long customerId;
        if (rptSearchCondition.isSearching()) {
            if(user.isCustomer()){
                customerId = user.getCustomerAccountProfile().getCustomer().getId();
            }else{
                return "modules/customer/rpt/ctCustomerFinanceReport";
            }
            Integer paymentType = (rptSearchCondition.getPaymentType() == null || rptSearchCondition.getPaymentType().equals("")) ?
                    null :
                    StringUtils.toInteger(rptSearchCondition.getPaymentType());
            page = ctCustomerFinanceRptService.getCompletedOrderList(page, customerId,paymentType, rptSearchCondition.getRemarks());

        }
        model.addAttribute("page", page);
        return "modules/customer/rpt/ctCustomerFinanceReport";
    }

    @ResponseBody
    @RequiresPermissions("rpt:customer:customerFinance:export")
    @RequestMapping(value = "checkExportTask", method = RequestMethod.POST)
    public AjaxJsonEntity checkExportTask(RptSearchCondition rptSearchCondition) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try {
            User user = UserUtils.getUser();
            Long customerId;
            if(user.isCustomer()){
                customerId = user.getCustomerAccountProfile().getCustomer().getId();
            }else{
                result.setSuccess(false);
                result.setMessage("创建报表导出任务失败：权限不足");
                return result;
            }
            Integer paymentType = (rptSearchCondition.getPaymentType() == null || rptSearchCondition.getPaymentType().equals("")) ?
                    null :
                    StringUtils.toInteger(rptSearchCondition.getPaymentType());

            ctCustomerFinanceRptService.checkRptExportTask(customerId,paymentType, rptSearchCondition.getRemarks(), user);

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
    @RequiresPermissions("rpt:customer:customerFinance:export")
    @RequestMapping(value = "export", method = RequestMethod.POST)
    public AjaxJsonEntity export(RptSearchCondition rptSearchCondition) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try {
            User user = UserUtils.getUser();
            Long customerId;
            if (user.isCustomer()) {
                customerId = UserUtils.getUser().getCustomerAccountProfile().getCustomer().getId();
            }else{
                result.setSuccess(false);
                result.setMessage("创建报表导出任务失败：权限不足");
                return result;
            }
            Integer paymentType = (rptSearchCondition.getPaymentType() == null || rptSearchCondition.getPaymentType().equals("")) ?
                    null :
                    StringUtils.toInteger(rptSearchCondition.getPaymentType());

            ctCustomerFinanceRptService.createRptExportTask(customerId,paymentType, rptSearchCondition.getRemarks(), user);
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
