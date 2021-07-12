package com.wolfking.jeesite.modules.customer.rpt.web;

import com.kkl.kklplus.entity.rpt.RPTCustomerFrozenDailyEntity;
import com.kkl.kklplus.entity.rpt.exception.RPTBaseException;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.modules.customer.rpt.service.CtCustomerFrozenDailyRptService;
import com.wolfking.jeesite.modules.rpt.entity.RptSearchCondition;
import com.wolfking.jeesite.modules.rpt.web.BaseRptController;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providerrpt.service.MSCustomerFrozenDailyRptService;
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
@RequestMapping(value = "${adminPath}/customer/rpt/customerFrozenDailyRpt/")
public class CtCustomerFrozenDailyRptController extends BaseRptController {

    @Autowired
    private CtCustomerFrozenDailyRptService ctCustomerFrozenDailyRptService;

    @ModelAttribute("rptSearchCondition")
    public RptSearchCondition get(@ModelAttribute("rptSearchCondition") RptSearchCondition rptSearchCondition) {
        if (rptSearchCondition == null) {
            rptSearchCondition = new RptSearchCondition();
        }
        Date now = new Date();
        if (rptSearchCondition.getEndDate() == null) {
            rptSearchCondition.setEndDate(now);
        }
        if (rptSearchCondition.getBeginDate() == null) {
            rptSearchCondition.setBeginDate(now);
        }
        return rptSearchCondition;
    }


    @RequiresPermissions("rpt:customer:customerNewOrderDaily:view")
    @RequestMapping(value = "customerFrozenDailyRptData")
    public String customerFrozenDailyRptData(RptSearchCondition rptSearchCondition, HttpServletRequest request, HttpServletResponse response, Model model) {
        User user = UserUtils.getUser();
        Long customerId = 0L;
        Date endDate = rptSearchCondition.getEndDate();
        long query = endDate.getTime();
        Long saleId = null;
        if (user.isCustomer()) {
            if (user.getCustomerAccountProfile() != null && user.getCustomerAccountProfile().getCustomer() != null) {
                //登录用户的客户，防篡改
                customerId = user.getCustomerAccountProfile().getCustomer().getId();
            }
        } else {
            return "modules/customer/rpt/ctCustomerFrozenDailyReport";
        }
        Page<RPTCustomerFrozenDailyEntity> page = new Page<>(request, response);
        if (rptSearchCondition.isSearching()) {
            page = ctCustomerFrozenDailyRptService.getCustomerFrozenDailyRptList(page, customerId, saleId, query);
        }
        model.addAttribute("page", page);
        return "modules/customer/rpt/ctCustomerFrozenDailyReport";
    }

    @ResponseBody
    @RequiresPermissions("rpt:customer:customerNewOrderDaily:export")
    @RequestMapping(value = "checkExportTask", method = RequestMethod.POST)
    public AjaxJsonEntity checkExportTask(RptSearchCondition rptSearchCondition) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try {
            User user = UserUtils.getUser();
            Long customerId = 0L;
            Date endDate = rptSearchCondition.getEndDate();
            Long saleId = null;
            if (user.isCustomer()) {
                if (user.getCustomerAccountProfile() != null && user.getCustomerAccountProfile().getCustomer() != null) {
                    customerId = user.getCustomerAccountProfile().getCustomer().getId();
                }
            } else {
                result.setSuccess(false);
                result.setMessage("创建报表导出任务失败：权限不走");
                return result;
            }
            if (customerId != 0L) {
                ctCustomerFrozenDailyRptService.checkRptExportTask(customerId, saleId, endDate, user);
            } else {
                result.setSuccess(false);
                result.setMessage("创建报表导出任务失败：请选择客户");
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
    @RequiresPermissions("rpt:customer:customerNewOrderDaily:export")
    @RequestMapping(value = "export", method = RequestMethod.POST)
    public AjaxJsonEntity export(RptSearchCondition rptSearchCondition) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try {
            User user = UserUtils.getUser();
            Long customerId =0L;
            Date endDate = rptSearchCondition.getEndDate();
            Long saleId = null;
            if (user.isCustomer()) {
                if (user.getCustomerAccountProfile() != null && user.getCustomerAccountProfile().getCustomer() != null) {
                    customerId = user.getCustomerAccountProfile().getCustomer().getId();
                }
            } else {
                result.setSuccess(false);
                result.setMessage("创建报表导出任务失败：权限不足");
                return result;
            }
            if (customerId != 0L) {
                ctCustomerFrozenDailyRptService.createRptExportTask(customerId, saleId, endDate, user);
                result.setMessage("报表导出任务创建成功，请前往'报表中心->报表下载'功能下载");
            } else {
                result.setSuccess(false);
                result.setMessage("创建报表导出任务失败：权限不足");
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
}
