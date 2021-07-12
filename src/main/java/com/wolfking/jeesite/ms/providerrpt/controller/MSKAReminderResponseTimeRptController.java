package com.wolfking.jeesite.ms.providerrpt.controller;

import com.kkl.kklplus.entity.rpt.RPTReminderResponseTimeEntity;
import com.kkl.kklplus.entity.rpt.exception.RPTBaseException;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.rpt.entity.RptSearchCondition;
import com.wolfking.jeesite.modules.rpt.web.BaseRptController;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providerrpt.service.MSKAReminderResponseTimeRptService;
import com.wolfking.jeesite.ms.providerrpt.service.MSReminderResponseTimeRptService;
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
@RequestMapping(value = "${adminPath}/rpt/provider/kaReminderResponseTime/")
public class MSKAReminderResponseTimeRptController extends BaseRptController {
    @Autowired
    private MSKAReminderResponseTimeRptService mskaReminderResponseTimeRptService;

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

        return rptSearchCondition;
    }

    /**
     * 催单时效统计
     */
    @RequestMapping(value = "kaReminderResponseTimeReport")
    public String reminderResponseTimeReport(RptSearchCondition rptSearchCondition, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<RPTReminderResponseTimeEntity> page = new Page<>(request, response);
        if (rptSearchCondition.getBeginDate() == null) {
            rptSearchCondition.setEndDate(DateUtils.getDateEnd(new Date()));
            rptSearchCondition.setBeginDate(DateUtils.getStartDayOfMonth(DateUtils.addMonth(new Date(), -1)));
        } else {
            rptSearchCondition.setEndDate(DateUtils.getDateEnd(rptSearchCondition.getEndDate()));
        }

        if (rptSearchCondition.isSearching()) {

            page = mskaReminderResponseTimeRptService.getReminderResponseTimeList(page, rptSearchCondition.getOrderNo(), rptSearchCondition.getAreaId(), rptSearchCondition.getReminderNo(),
                    rptSearchCondition.getReminderTimes(), rptSearchCondition.getBeginDate(), rptSearchCondition.getEndDate(),rptSearchCondition.getCustomerId());
        }
        model.addAttribute("page", page);
        return "modules/providerrpt/kaReminderResponseTimeReport";
    }


    @ResponseBody
    @RequestMapping(value = "checkExportTask", method = RequestMethod.POST)
    public AjaxJsonEntity checkExportTask(RptSearchCondition rptSearchCondition) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try {
            User user = UserUtils.getUser();
            String orderNo = rptSearchCondition.getOrderNo();
            Long areaId = rptSearchCondition.getAreaId();
            String reminderNo = rptSearchCondition.getReminderNo();
            Integer reminderTimes = rptSearchCondition.getReminderTimes();
            Date beginDate = rptSearchCondition.getBeginDate();
            Date endDate = DateUtils.getDateEnd(rptSearchCondition.getEndDate());

            mskaReminderResponseTimeRptService.checkRptExportTask(orderNo, areaId, reminderNo, reminderTimes, beginDate, endDate,rptSearchCondition.getCustomerId(), user);

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
            String orderNo = rptSearchCondition.getOrderNo();
            Long areaId = rptSearchCondition.getAreaId();
            String reminderNo = rptSearchCondition.getReminderNo();
            Integer reminderTimes = rptSearchCondition.getReminderTimes();
            Date beginDate = rptSearchCondition.getBeginDate();
            Date endDate = DateUtils.getDateEnd(rptSearchCondition.getEndDate());


            mskaReminderResponseTimeRptService.createRptExportTask(orderNo, areaId, reminderNo, reminderTimes, beginDate, endDate,rptSearchCondition.getCustomerId(), user);
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
