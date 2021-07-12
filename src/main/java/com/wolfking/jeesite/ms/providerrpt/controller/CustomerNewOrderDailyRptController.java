package com.wolfking.jeesite.ms.providerrpt.controller;


import com.kkl.kklplus.entity.rpt.RPTCustomerNewOrderDailyRptEntity;
import com.kkl.kklplus.entity.rpt.exception.RPTBaseException;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.modules.rpt.entity.RptSearchCondition;
import com.wolfking.jeesite.modules.rpt.web.BaseRptController;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providerrpt.service.CustomerNewOrderDailyRptService;
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
@RequestMapping(value = "${adminPath}/rpt/provider/customerNewOrderDaily/")
public class CustomerNewOrderDailyRptController extends BaseRptController {

    @Autowired
    private CustomerNewOrderDailyRptService customerNewOrderDailyRptService;

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

    @RequestMapping(value = "getList")
    @RequiresPermissions("rpt:customerNewOrderDaily:view")
    public String customerNewOrderDailyRpt(RptSearchCondition rptSearchCondition, HttpServletRequest request, HttpServletResponse response, Model model) {
        User user = UserUtils.getUser();
        Long customerId = rptSearchCondition.getCustomerId();
        Date endDate = rptSearchCondition.getEndDate();
        long query = endDate.getTime();
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
            page = customerNewOrderDailyRptService.getCustomerNewOrderDailyRptList(page, customerId, saleId, subFlag, query);
        }
        model.addAttribute("page", page);
        return "modules/providerrpt/customerNewOrderDailyReport";
    }

    @ResponseBody
    @RequiresPermissions("rpt:customerNewOrderDaily:export")
    @RequestMapping(value = "checkExportTask", method = RequestMethod.POST)
    public AjaxJsonEntity checkExportTask(RptSearchCondition rptSearchCondition) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try {
            User user = UserUtils.getUser();
            Long customerId = rptSearchCondition.getCustomerId();
            Date endDate = rptSearchCondition.getEndDate();
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
                customerNewOrderDailyRptService.checkRptExportTask(customerId, saleId, endDate, user,subFlag);
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
            Date endDate = rptSearchCondition.getEndDate();
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
                customerNewOrderDailyRptService.createRptExportTask(customerId, saleId, endDate, user,subFlag);
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

