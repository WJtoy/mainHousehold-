package com.wolfking.jeesite.modules.customer.rpt.web;

import com.kkl.kklplus.entity.rpt.RPTUncompletedOrderEntity;
import com.kkl.kklplus.entity.rpt.exception.RPTBaseException;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.customer.rpt.service.CtUncompletedOrderRptService;
import com.wolfking.jeesite.modules.rpt.entity.RptSearchCondition;
import com.wolfking.jeesite.modules.rpt.web.BaseRptController;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providerrpt.service.MSUncompletedOrderRptService;
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
@RequestMapping(value = "${adminPath}/customer/rpt/uncompletedOrder/")
public class CtUncompletedOrderRptController extends BaseRptController {

    @Autowired
    private CtUncompletedOrderRptService ctUncompletedOrderRptService;

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
        if (rptSearchCondition.getEndDate() == null) {
            rptSearchCondition.setEndDate(DateUtils.addDays(now,-1));
        }
        return rptSearchCondition;
    }

    /**
     *   未完工单明细
     */
    @RequiresPermissions("rpt:customer:uncompletedOrderReport:view")
    @RequestMapping(value = "uncompletedOrderReport")
    public String unCompleteOrderReport(RptSearchCondition rptSearchCondition, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<RPTUncompletedOrderEntity> page = new Page<>(request, response);
        User user = UserUtils.getUser();
        Long customerId;
        if (rptSearchCondition.isSearching()) {
            if(user.isCustomer()){
                customerId = UserUtils.getUser().getCustomerAccountProfile().getCustomer().getId();
                rptSearchCondition.setCustomerId(customerId);
            }else {
                return "modules/customer/rpt/ctUncompletedOrderReport";
            }
            page = ctUncompletedOrderRptService.getUnCompletedOrderList(page, rptSearchCondition.getCustomerId(),
                    rptSearchCondition.getEndDate());
        }
        model.addAttribute("page", page);
        return "modules/customer/rpt/ctUncompletedOrderReport";
    }



    @ResponseBody
    @RequiresPermissions("rpt:customer:uncompletedOrderReport:export")
    @RequestMapping(value = "checkExportTask", method = RequestMethod.POST)
    public AjaxJsonEntity checkExportTask(RptSearchCondition rptSearchCondition) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try {
            User user = UserUtils.getUser();
            Long customerId = 0L;
            Date endDate = rptSearchCondition.getEndDate();
            if (user.isCustomer()) {
                if (user.getCustomerAccountProfile() != null && user.getCustomerAccountProfile().getCustomer() != null) {
                    customerId = user.getCustomerAccountProfile().getCustomer().getId();
                }
            }else{
                result.setSuccess(false);
                result.setMessage("创建报表导出任务失败：权限不足");
                return result;
            }
                ctUncompletedOrderRptService.checkRptExportTask(customerId,endDate, user);

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
    @RequiresPermissions("rpt:customer:uncompletedOrderReport:export")
    @RequestMapping(value = "export", method = RequestMethod.POST)
    public AjaxJsonEntity export(RptSearchCondition rptSearchCondition) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try {
            User user = UserUtils.getUser();
            Long customerId = 0L;
            Date endDate = rptSearchCondition.getEndDate();
            if (user.isCustomer()) {
                if (user.getCustomerAccountProfile() != null && user.getCustomerAccountProfile().getCustomer() != null) {
                    customerId = user.getCustomerAccountProfile().getCustomer().getId();
                }
            }
            if (customerId != 0L) {
                ctUncompletedOrderRptService.createRptExportTask(customerId, endDate, user);
                result.setMessage("报表导出任务创建成功，请前往'报表中心->报表下载'功能下载");
            } else {
                result.setSuccess(false);
                result.setMessage("创建报表导出任务失败：用户没有客户权限");
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
