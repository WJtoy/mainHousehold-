package com.wolfking.jeesite.ms.providerrpt.controller;


import cn.hutool.core.net.URLEncoder;
import com.kkl.kklplus.entity.rpt.RPTExportTaskEntity;
import com.kkl.kklplus.entity.rpt.exception.RPTBaseException;
import com.wolfking.jeesite.common.config.Global;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.md.entity.Engineer;
import com.wolfking.jeesite.modules.md.service.ServicePointService;
import com.wolfking.jeesite.modules.rpt.entity.RptSearchCondition;
import com.wolfking.jeesite.modules.rpt.web.BaseRptController;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providerrpt.service.ReportExportTaskService;
import com.wolfking.jeesite.ms.providerrpt.utils.MSRptUtils;
import org.apache.ibatis.annotations.Param;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.Charset;
import java.util.Date;

@Controller
@RequestMapping(value = "${adminPath}/rpt/provider/exportTask/")
public class ReportExportTaskController extends BaseRptController {

    @Autowired
    private ReportExportTaskService reportExportTaskService;

    @ModelAttribute("rptSearchCondition")
    public RptSearchCondition get(@ModelAttribute("rptSearchCondition") RptSearchCondition rptSearchCondition) {
        if (rptSearchCondition == null) {
            rptSearchCondition = new RptSearchCondition();
        }
        Date now = new Date(); //默认使用当天作为查询条件
        if (rptSearchCondition.getBeginDate() == null) {
            rptSearchCondition.setBeginDate(DateUtils.addDays(now, -7));
        }
        if (rptSearchCondition.getEndDate() == null) {
            rptSearchCondition.setEndDate(now);
        }
        return rptSearchCondition;
    }

    @RequestMapping(value = "list")
    public String customerNewOrderDailyRpt(RptSearchCondition rptSearchCondition,
                                           HttpServletRequest request, HttpServletResponse response, Model model) {
        User user = UserUtils.getUser();
        Page<RPTExportTaskEntity> page = new Page<>(request, response);
        if (user != null && user.getId() != null) {
            User taskCreateBy = null;
            if (!user.isAdmin()) {
                taskCreateBy = user;
            }
            rptSearchCondition.setBeginDate(DateUtils.getStartOfDay(rptSearchCondition.getBeginDate()));
            rptSearchCondition.setEndDate(DateUtils.getEndOfDay(rptSearchCondition.getEndDate()));

            try {
                page = reportExportTaskService.getRptExportTaskList(page,
                        rptSearchCondition.getReportId(), rptSearchCondition.getReportType(), taskCreateBy,
                        rptSearchCondition.getBeginDate(), rptSearchCondition.getEndDate());
            } catch (RPTBaseException e) {
                addMessage(model, e.getMessage());
            }
        }
        model.addAttribute("allReportList", MSRptUtils.getAllReportList());
        model.addAttribute("allReportTypeList", MSRptUtils.getAllReportTypeList());
        model.addAttribute("page", page);
        return "modules/providerrpt/reportExportTaskList";
    }

    @RequestMapping(value = "downloadReportExcel")
    public String customerNewOrderDailyRpt(@Param("taskId") Long taskId,
                                           HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes) {
        User user = UserUtils.getUser();
        String downloadUrl = "";
        if (user != null && user.getId() != null) {
            downloadUrl = reportExportTaskService.getReportExcelDownloadUrl(taskId, user);
        }
        if (StringUtils.isBlank(downloadUrl)) {
            addMessage(redirectAttributes,"该文档不是您创建的，您无权下载");
            return  "redirect:" + Global.getAdminPath() + "/rpt/provider/exportTask/list";
        }
//        downloadUrl = URLEncoder.DEFAULT.encode("http://localhost:8080/static/doc/快可立全国联保批量下单数据模板.xls", Charset.defaultCharset());;
        return "redirect:" + URLEncoder.DEFAULT.encode(downloadUrl, Charset.defaultCharset());
    }

}

