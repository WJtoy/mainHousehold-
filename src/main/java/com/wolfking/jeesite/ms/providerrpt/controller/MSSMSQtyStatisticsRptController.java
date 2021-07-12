package com.wolfking.jeesite.ms.providerrpt.controller;

import com.kkl.kklplus.entity.rpt.RPTSMSQtyStatisticsEntity;
import com.kkl.kklplus.entity.rpt.exception.RPTBaseException;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.rpt.entity.RptSearchCondition;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providerrpt.service.MSSMSQtyStatisticsRptService;
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
import java.util.Map;

@Controller
@RequestMapping(value = "${adminPath}/rpt/provider/smsQtyStatistics/")
public class MSSMSQtyStatisticsRptController {
    @Autowired
    private MSSMSQtyStatisticsRptService mssmsQtyStatisticsRptService;

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

    @SuppressWarnings("deprecation")
    @RequestMapping(value = "smsQtyStatisticsReport")
    public String smsQtyStatisticsReport(RptSearchCondition rptSearchCondition, Model model) {
        List<RPTSMSQtyStatisticsEntity> list = new ArrayList<>();
        if (rptSearchCondition.isSearching()) {
            list = mssmsQtyStatisticsRptService.getSMSQtyStatisticsList(rptSearchCondition.getSelectedYear(), rptSearchCondition.getSelectedMonth());
        }
        rptSearchCondition.setList(list);
        return "modules/providerrpt/smsQtyStatisticsReport";
    }

    @RequestMapping(value = "smsQtyStatisticsChart")
    public String smsQtyStatisticsChart(RptSearchCondition rptSearchCondition, Model model) {
        Map<String, Object> map = mssmsQtyStatisticsRptService.getSMSQtyStatisticsChartList(rptSearchCondition.getSelectedYear(), rptSearchCondition.getSelectedMonth());
        //设置圆形图表
        model.addAttribute("mapList", map.get("mapList"));
        //设置柱状图表
        model.addAttribute("sendDateList", map.get("sendDateList"));
        model.addAttribute("plannedList", map.get("plannedList"));
        model.addAttribute("acceptedAppList", map.get("acceptedAppList"));
        model.addAttribute("pendingList", map.get("pendingList"));
        model.addAttribute("pendingApps", map.get("pendingApps"));
        model.addAttribute("verificationCodes", map.get("verificationCodes"));
        model.addAttribute("orderDetailPages", map.get("orderDetailPages"));
        model.addAttribute("callBacks", map.get("callBacks"));
        model.addAttribute("cancelleds", map.get("cancelleds"));
        //设置线型报表
        model.addAttribute("plannedRates", map.get("plannedRates"));
        model.addAttribute("acceptedAppRates", map.get("acceptedAppRates"));
        model.addAttribute("pendingRates", map.get("pendingRates"));
        model.addAttribute("pendingAppRates", map.get("pendingAppRates"));
        model.addAttribute("verificationCodeRates", map.get("verificationCodeRates"));
        model.addAttribute("orderDetailPageRates", map.get("orderDetailPageRates"));
        model.addAttribute("callBackRates", map.get("callBackRates"));
        model.addAttribute("cancelledRates", map.get("cancelledRates"));
        return "modules/providerrpt/smsQtyStatisticsChart";
    }

    @ResponseBody
    @RequestMapping(value = "checkExportTask", method = RequestMethod.POST)
    public AjaxJsonEntity checkExportTask(RptSearchCondition rptSearchCondition) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try {
            User user = UserUtils.getUser();
            mssmsQtyStatisticsRptService.checkRptExportTask(rptSearchCondition.getSelectedYear(),rptSearchCondition.getSelectedMonth(),user);

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
            mssmsQtyStatisticsRptService.createRptExportTask(rptSearchCondition.getSelectedYear(),rptSearchCondition.getSelectedMonth(),user);
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
