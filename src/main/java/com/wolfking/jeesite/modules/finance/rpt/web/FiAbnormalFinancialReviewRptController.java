package com.wolfking.jeesite.modules.finance.rpt.web;

import com.google.common.collect.Lists;
import com.kkl.kklplus.entity.rpt.RPTAbnormalFinancialAuditEntity;
import com.kkl.kklplus.entity.rpt.exception.RPTBaseException;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.finance.rpt.service.FiAbnormalFinancialReviewRptService;
import com.wolfking.jeesite.modules.rpt.entity.RptSearchCondition;
import com.wolfking.jeesite.modules.rpt.web.BaseRptController;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providerrpt.service.MSAbnormalFinancialReviewRptService;
import com.wolfking.jeesite.ms.utils.MSUserUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
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
@RequestMapping(value = "${adminPath}/finance/rpt/abnormalFinancialReview/")
public class FiAbnormalFinancialReviewRptController extends BaseRptController {

    @Autowired
    private FiAbnormalFinancialReviewRptService fiAbnormalFinancialReviewRptService;

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
     *   财务审单
     */
    @RequiresPermissions("rpt:finance:abnormalFinancialReview:view")
    @RequestMapping(value = "abnormalFinancialReviewRptData")
    public String AbnormalFinancialReviewRptData(RptSearchCondition rptSearchCondition, Model model){
        List<RPTAbnormalFinancialAuditEntity> list = Lists.newArrayList();

        List<User> reviewerList = new ArrayList<>();
        List<User> reviewer = MSUserUtils.getListByUserType(11);
        for(User item : reviewer){
            if(item.getSubFlag() ==1 ){
                reviewerList.add(item);
            }
        }

        if (rptSearchCondition.isSearching()) {
            list = fiAbnormalFinancialReviewRptService.getAbnormalPlanDailyList(rptSearchCondition.getSelectedYear(),rptSearchCondition.getSelectedMonth(),rptSearchCondition.getReviewerId());

        }

        int days = DateUtils.getDaysOfMonth(DateUtils.parseDate(rptSearchCondition.getSelectedYear() + "-" + rptSearchCondition.getSelectedMonth() + "-01"));

        rptSearchCondition.setDays(days);
        rptSearchCondition.setList(list);
        model.addAttribute("reviewerList", reviewerList);
        return "modules/finance/rpt/fiAbnormalFinancialReviewRpt";
    }


    @ResponseBody
    @RequiresPermissions("rpt:finance:abnormalFinancialReview:export")
    @RequestMapping(value = "checkExportTask", method = RequestMethod.POST)
    public AjaxJsonEntity checkExportTask(RptSearchCondition rptSearchCondition) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try {

            User user = UserUtils.getUser();
            int selectedYear = rptSearchCondition.getSelectedYear();
            int selectedMonth = rptSearchCondition.getSelectedMonth();

            fiAbnormalFinancialReviewRptService.checkRptExportTask(selectedYear,selectedMonth,rptSearchCondition.getReviewerId(),user);

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
    @RequiresPermissions("rpt:finance:abnormalFinancialReview:export")
    @RequestMapping(value = "export", method = RequestMethod.POST)
    public AjaxJsonEntity export(RptSearchCondition rptSearchCondition) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try {

            User user = UserUtils.getUser();
            int selectedYear = rptSearchCondition.getSelectedYear();
            int selectedMonth = rptSearchCondition.getSelectedMonth();

            fiAbnormalFinancialReviewRptService.createRptExportTask(selectedYear,selectedMonth,rptSearchCondition.getReviewerId(),user);
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
