package com.wolfking.jeesite.ms.providerrpt.controller;


import com.kkl.kklplus.entity.rpt.RPTCustomerRechargeSummaryEntity;
import com.kkl.kklplus.entity.rpt.exception.RPTBaseException;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.rpt.entity.RptSearchCondition;
import com.wolfking.jeesite.modules.rpt.web.BaseRptController;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providerrpt.service.MSCustomerRechargeSummaryRptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("${adminPath}/rpt/provider/customerRecharge/")
public class MSCustomerRechargeSummaryController extends BaseRptController {

    @Autowired
    private MSCustomerRechargeSummaryRptService msCustomerRechargeSummaryRptService;

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
        if (rptSearchCondition.getSelectedYear() == null) {
            rptSearchCondition.setSelectedYear(DateUtils.getYear(now));
        }
        if (rptSearchCondition.getSelectedMonth() == null) {
            rptSearchCondition.setSelectedMonth(DateUtils.getMonth(now));
        }
        return rptSearchCondition;
    }


    /**
     * 客戶充值汇总统计
     */
    @RequestMapping(value = "customerRechargeSummary")
    public String customerRechargeSummary(RptSearchCondition rptSearchCondition, Model model) {
        List<RPTCustomerRechargeSummaryEntity> list = new ArrayList<>();
        if (rptSearchCondition.isSearching()) {
            list =msCustomerRechargeSummaryRptService.getCustomerRechargeSummary(rptSearchCondition.getCustomerId(),rptSearchCondition.getSelectedYear(),rptSearchCondition.getSelectedMonth());
        }

        model.addAttribute("list", list);
        return "modules/providerrpt/customerRechargeSummary";
    }


    @ResponseBody
    @RequestMapping(value = "checkExportTask", method = RequestMethod.POST)
    public AjaxJsonEntity checkExportTask(RptSearchCondition rptSearchCondition) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try {
            User user = UserUtils.getUser();


            msCustomerRechargeSummaryRptService.checkRptExportTask(rptSearchCondition.getCustomerId(),rptSearchCondition.getSelectedYear(),rptSearchCondition.getSelectedMonth(),user);

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

            msCustomerRechargeSummaryRptService.createRptExportTask(rptSearchCondition.getCustomerId(),rptSearchCondition.getSelectedYear(),rptSearchCondition.getSelectedMonth(),user);
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
