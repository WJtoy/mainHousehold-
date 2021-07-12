package com.wolfking.jeesite.ms.providerrpt.controller;

import com.kkl.kklplus.entity.rpt.RPTCancelledOrderEntity;
import com.kkl.kklplus.entity.rpt.RPTCompletedOrderEntity;
import com.kkl.kklplus.entity.rpt.RPTCustomerChargeSummaryMonthlyEntity;
import com.kkl.kklplus.entity.rpt.RPTCustomerWriteOffEntity;
import com.kkl.kklplus.entity.rpt.exception.RPTBaseException;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.rpt.entity.RptSearchCondition;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providerrpt.service.MSCustomerChargeRptService;
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
@RequestMapping(value = "${adminPath}/rpt/provider/customerCharge/")
public class MSCustomerChargeRptController {
    @Autowired
    private MSCustomerChargeRptService MSCustomerChargeRptService;

    @Autowired
    private MSCustomerChargeRptService customerChargeRptService;


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

        if (rptSearchCondition.getBeginCancelApplyDate() == null) {
            rptSearchCondition.setBeginCancelApplyDate(now);
        }
        if (rptSearchCondition.getEndCancelApplyDate() == null) {
            rptSearchCondition.setEndCancelApplyDate(now);
        }
        if (rptSearchCondition.getSelectedYear() == null) {
            rptSearchCondition.setSelectedYear(DateUtils.getYear(now));
        }
        if (rptSearchCondition.getSelectedMonth() == null) {
            rptSearchCondition.setSelectedMonth(DateUtils.getMonth(now));
        }

        return rptSearchCondition;
    }
    /**
     * 客户对账单 - 工单数量或消费金额
     */
    @RequestMapping(value = "customerChargeSummaryRpt")
    public String customerChargeSUMRpt(RptSearchCondition rptSearchCondition, Model model) {

        if (rptSearchCondition.isSearching() && rptSearchCondition.getCustomerId() != null) {
            RPTCustomerChargeSummaryMonthlyEntity customerChargeSummary = customerChargeRptService.getCustomerChargeSummary(rptSearchCondition.getCustomerId(), rptSearchCondition.getSelectedYear(),
                    rptSearchCondition.getSelectedMonth());
            model.addAttribute("summary", customerChargeSummary);
        }
        return "modules/providerrpt/customerChargeSummaryRpt";
    }


    /**
     * 客户对账单  完工单
     */
    @RequestMapping(value = "customerChargeCompleteReport")
    public String customerChargeCompleteReport(RptSearchCondition rptSearchCondition, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<RPTCompletedOrderEntity> page = new Page<>(request, response);
        if (rptSearchCondition.isSearching() && rptSearchCondition.getCustomerId() != null) {
            page = MSCustomerChargeRptService.getCompletedOrderList(page, rptSearchCondition.getCustomerId(),
                    rptSearchCondition.getSelectedYear(), rptSearchCondition.getSelectedMonth());
        }
        model.addAttribute("page", page);
        return "modules/providerrpt/customerChargeCompleteReport";
    }

    /**
     * 客户对账单  退单取消单
     */
    @RequestMapping(value = "returnedOrderCancelledRpt")
    public String cancelledOrReturnedOrderRpt(RptSearchCondition rptSearchCondition, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<RPTCancelledOrderEntity> page = new Page<>(request, response);
        if (rptSearchCondition.isSearching() && rptSearchCondition.getCustomerId() != null) {
            page = MSCustomerChargeRptService.getCancelledOrReturnedOrderList(page, rptSearchCondition.getCustomerId(),
                    rptSearchCondition.getSelectedYear(), rptSearchCondition.getSelectedMonth());
        }
        model.addAttribute("page", page);
        return "modules/providerrpt/customerChargeReturnedOrCancelledRpt";
    }



    /**
     * 客户对账单  退补单
     */
    @RequestMapping(value = "customerChargeWriteOffRpt")
    public String customerChargeWriteOffRpt(RptSearchCondition rptSearchCondition, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<RPTCustomerWriteOffEntity> page = new Page<>(request, response);
        if (rptSearchCondition.isSearching() && rptSearchCondition.getCustomerId() != null) {
            page = MSCustomerChargeRptService.getCustomerWriteOffList(page, rptSearchCondition.getCustomerId(),
                    rptSearchCondition.getSelectedYear(), rptSearchCondition.getSelectedMonth());
        }
        model.addAttribute("page", page);
        return "modules/providerrpt/customerChargeWriteOffRpt";
    }



    @ResponseBody
    @RequestMapping(value = "export", method = RequestMethod.POST)
    public AjaxJsonEntity export(RptSearchCondition rptSearchCondition) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try {
            User user = UserUtils.getUser();
            Long customerId = rptSearchCondition.getCustomerId();
            int selectedYear = rptSearchCondition.getSelectedYear();
            int selectedMonth = rptSearchCondition.getSelectedMonth();
            if(customerId != null){
                MSCustomerChargeRptService.createRptExportTask(customerId,selectedYear,selectedMonth,user);
                result.setMessage("报表导出任务创建成功，请前往'报表中心->报表下载'功能下载");
            }else {
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
