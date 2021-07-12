package com.wolfking.jeesite.ms.providerrpt.controller;


import com.kkl.kklplus.entity.rpt.RPTCustomerNewOrderDailyRptEntity;
import com.kkl.kklplus.entity.rpt.exception.RPTBaseException;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.rpt.entity.RptSearchCondition;
import com.wolfking.jeesite.modules.rpt.web.BaseRptController;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providerrpt.service.CustomerNewOrderMonthRptService;
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
@RequestMapping(value = "${adminPath}/rpt/provider/customerNewOrderMonth/")
public class CustomerNewOrderMonthRptController extends BaseRptController {

    @Autowired
    private CustomerNewOrderMonthRptService customerNewOrderMonthRptService;

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

    @RequestMapping(value = "getMonthList")
    @RequiresPermissions("rpt:customerNewOrderDaily:view")
    public String customerNewOrderDailyRpt(RptSearchCondition rptSearchCondition, HttpServletRequest request, HttpServletResponse response, Model model) {
        User user = UserUtils.getUser();
        Long customerId = rptSearchCondition.getCustomerId();
        Long saleId = null;
        Integer subFlag = null;
        if (user.isCustomer()) {
            if (user.getCustomerAccountProfile() != null && user.getCustomerAccountProfile().getCustomer() != null) {
                //登录用户的客户，防篡改
                customerId = user.getCustomerAccountProfile().getCustomer().getId();
            }
        } else if (user.isSaleman()) {
            saleId = user.getId();
            subFlag = user.getSubFlag();
        }
        Page<RPTCustomerNewOrderDailyRptEntity> page = new Page<>(request, response);
        if (rptSearchCondition.isSearching()) {
            page = customerNewOrderMonthRptService.getCustomerNewOrderMonthRptList(page, customerId, saleId, subFlag,rptSearchCondition.getSelectedYear(),rptSearchCondition.getSelectedMonth());
        }
        model.addAttribute("page", page);
        return "modules/providerrpt/customerNewOrderMonthReport";
    }

    @ResponseBody
    @RequiresPermissions("rpt:customerNewOrderDaily:export")
    @RequestMapping(value = "checkExportTask", method = RequestMethod.POST)
    public AjaxJsonEntity checkExportTask(RptSearchCondition rptSearchCondition) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try {
            User user = UserUtils.getUser();
            Long customerId = rptSearchCondition.getCustomerId();
            Long saleId = null;
            Integer subFlag = null;
            if (user.isCustomer()) {
                if (user.getCustomerAccountProfile() != null && user.getCustomerAccountProfile().getCustomer() != null) {
                    customerId = user.getCustomerAccountProfile().getCustomer().getId();
                }
            } else if (user.isSaleman()) {
                saleId = user.getId();
                subFlag = user.getSubFlag();
            }
            if (customerId != null) {
                customerNewOrderMonthRptService.checkRptExportTask(customerId, saleId, rptSearchCondition.getSelectedYear(),rptSearchCondition.getSelectedMonth(), user,subFlag);
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
    @RequiresPermissions("rpt:customerNewOrderDaily:export")
    @RequestMapping(value = "export", method = RequestMethod.POST)
    public AjaxJsonEntity export(RptSearchCondition rptSearchCondition) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try {
            User user = UserUtils.getUser();
            Long customerId = rptSearchCondition.getCustomerId();
            Long saleId = null;
            Integer subFlag = null;
            if (user.isCustomer()) {
                if (user.getCustomerAccountProfile() != null && user.getCustomerAccountProfile().getCustomer() != null) {
                    customerId = user.getCustomerAccountProfile().getCustomer().getId();
                }
            } else if (user.isSaleman()) {
                saleId = user.getId();
                subFlag = user.getSubFlag();
            }
            if (customerId != null) {
                customerNewOrderMonthRptService.createRptExportTask(customerId, saleId, rptSearchCondition.getSelectedYear(),rptSearchCondition.getSelectedMonth(), user,subFlag);
                result.setMessage("报表导出任务创建成功，请前往'报表中心->报表下载'功能下载");
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

}

