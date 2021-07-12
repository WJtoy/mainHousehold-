package com.wolfking.jeesite.ms.providerrpt.controller;


import com.kkl.kklplus.entity.rpt.RPTCustomerRechargeSummaryEntity;
import com.kkl.kklplus.entity.rpt.RPTRechargeRecordEntity;
import com.kkl.kklplus.entity.rpt.exception.RPTBaseException;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.rpt.entity.RptSearchCondition;
import com.wolfking.jeesite.modules.rpt.web.BaseRptController;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providerrpt.service.MSDepositRechargeSummaryRptService;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("${adminPath}/rpt/provider/depositRecharge/")
public class MSDepositRechargeSummaryController extends BaseRptController {

    @Autowired
    private MSDepositRechargeSummaryRptService msDepositRechargeSummaryRptService;

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

        if (rptSearchCondition.getBeginDate() == null) {
            rptSearchCondition.setBeginDate(now);
        }
        if (rptSearchCondition.getEndDate() == null) {
            rptSearchCondition.setEndDate(now);
        }

        return rptSearchCondition;
    }


    /**
     * 质保金充值汇总统计
     */
    @RequiresPermissions("rpt:depositRecharge:view")
    @RequestMapping(value = "depositRechargeSummary")
    public String customerRechargeSummary(RptSearchCondition rptSearchCondition, Model model) {
        List<RPTCustomerRechargeSummaryEntity> list = new ArrayList<>();
        if (rptSearchCondition.isSearching()) {
            list =msDepositRechargeSummaryRptService.getDepositRechargeSummary(rptSearchCondition.getServicePointId(),rptSearchCondition.getSelectedYear(),rptSearchCondition.getSelectedMonth());
        }

        model.addAttribute("list", list);
        return "modules/providerrpt/depositRecharge";
    }


    /**
     * 质保金充值明细统计
     */
    @RequiresPermissions("rpt:depositRecharge:view")
    @RequestMapping(value = "depositRechargeDetails")
    public String rechargeRecordByPage(RptSearchCondition rptSearchCondition, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<RPTRechargeRecordEntity> page = new Page<>(request, response);
        rptSearchCondition.setBeginDate(DateUtils.getStartOfDay(rptSearchCondition.getBeginDate()));
        rptSearchCondition.setEndDate(DateUtils.getEndOfDay(rptSearchCondition.getEndDate()));
        if (rptSearchCondition.isSearching()) {
            page = msDepositRechargeSummaryRptService.getDepositRechargeDetails(page,rptSearchCondition.getActionType(),
                    rptSearchCondition.getBeginDate(), rptSearchCondition.getEndDate(), rptSearchCondition.getServicePointId());
        }
        model.addAttribute("page", page);
        return "modules/providerrpt/depositRechargedetails";
    }


    @ResponseBody
    @RequiresPermissions("rpt:depositRecharge:export")
    @RequestMapping(value = "checkExportTask", method = RequestMethod.POST)
    public AjaxJsonEntity checkExportTask(RptSearchCondition rptSearchCondition) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try {
            User user = UserUtils.getUser();
            msDepositRechargeSummaryRptService.checkRptExportTask(rptSearchCondition.getServicePointId(),rptSearchCondition.getSelectedYear(),rptSearchCondition.getSelectedMonth(),user);

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
    @RequiresPermissions("rpt:depositRecharge:export")
    @RequestMapping(value = "export", method = RequestMethod.POST)
    public AjaxJsonEntity export(RptSearchCondition rptSearchCondition) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try {
            User user = UserUtils.getUser();

            msDepositRechargeSummaryRptService.createRptExportTask(rptSearchCondition.getServicePointId(),rptSearchCondition.getSelectedYear(),rptSearchCondition.getSelectedMonth(),user);
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


    @ResponseBody
    @RequiresPermissions("rpt:depositRecharge:export")
    @RequestMapping(value = "checkExportDetalis", method = RequestMethod.POST)
    public AjaxJsonEntity checkExportDetalis(RptSearchCondition rptSearchCondition) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try {

            User user = UserUtils.getUser();
            rptSearchCondition.setBeginDate(DateUtils.getStartOfDay(rptSearchCondition.getBeginDate()));
            rptSearchCondition.setEndDate(DateUtils.getEndOfDay(rptSearchCondition.getEndDate()));

            msDepositRechargeSummaryRptService.checkExportTask(rptSearchCondition.getActionType(),
                    rptSearchCondition.getBeginDate(), rptSearchCondition.getEndDate(), rptSearchCondition.getServicePointId(),user);

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
    @RequiresPermissions("rpt:depositRecharge:export")
    @RequestMapping(value = "exportDetalis", method = RequestMethod.POST)
    public AjaxJsonEntity exportDetalis(RptSearchCondition rptSearchCondition) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try {

            User user = UserUtils.getUser();
            rptSearchCondition.setBeginDate(DateUtils.getStartOfDay(rptSearchCondition.getBeginDate()));
            rptSearchCondition.setEndDate(DateUtils.getEndOfDay(rptSearchCondition.getEndDate()));

            msDepositRechargeSummaryRptService.createExportTask(rptSearchCondition.getActionType(),
                    rptSearchCondition.getBeginDate(), rptSearchCondition.getEndDate(), rptSearchCondition.getServicePointId(),user);
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
