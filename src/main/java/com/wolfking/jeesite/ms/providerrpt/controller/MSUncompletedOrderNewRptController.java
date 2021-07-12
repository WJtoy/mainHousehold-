package com.wolfking.jeesite.ms.providerrpt.controller;

import com.kkl.kklplus.entity.rpt.RPTUncompletedOrderEntity;
import com.kkl.kklplus.entity.rpt.RPTUncompletedQtyEntity;
import com.kkl.kklplus.entity.rpt.exception.RPTBaseException;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.wolfking.jeesite.modules.rpt.entity.RptSearchCondition;
import com.wolfking.jeesite.modules.rpt.web.BaseRptController;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providerrpt.service.MSUncompletedOrderNewRptService;
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
import java.util.List;

@Controller
@RequestMapping(value = "${adminPath}/rpt/provider/uncompletedOrderNew/")
public class MSUncompletedOrderNewRptController extends BaseRptController {

    @Autowired
    private MSUncompletedOrderNewRptService msUncompletedOrderNewRptService;

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
    @RequestMapping(value = "uncompletedOrderNewReport")
    @RequiresPermissions("rpt:uncompletedOrderNewReport:view")
    public String uncompletedOrderNewReport(RptSearchCondition rptSearchCondition, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<RPTUncompletedQtyEntity> page = new Page<>(request, response);
        if (rptSearchCondition.isSearching()) {
            page = msUncompletedOrderNewRptService.getUnCompletedOrderNewList(page,
                    rptSearchCondition.getEndDate(), rptSearchCondition.getQuarter());
        }
        List<String> quarters = QuarterUtils.getQuarters(OrderUtils.getGoLiveDate(), DateUtils.getEndOfDay(new Date()));
        model.addAttribute("quarters", quarters);
        model.addAttribute("page", page);
        return "modules/providerrpt/uncompletedOrderNewReport";
    }



    @ResponseBody
    @RequiresPermissions("rpt:uncompletedOrderNewReport:export")
    @RequestMapping(value = "checkExportTask", method = RequestMethod.POST)
    public AjaxJsonEntity checkExportTask(RptSearchCondition rptSearchCondition) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try {
            User user = UserUtils.getUser();
            Date endDate = rptSearchCondition.getEndDate();
            msUncompletedOrderNewRptService.checkRptExportTask(endDate, rptSearchCondition.getQuarter(), user);

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
    @RequiresPermissions("rpt:uncompletedOrderNewReport:export")
    @RequestMapping(value = "export", method = RequestMethod.POST)
    public AjaxJsonEntity export(RptSearchCondition rptSearchCondition) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try {
            User user = UserUtils.getUser();
            Date endDate = rptSearchCondition.getEndDate();
            msUncompletedOrderNewRptService.createRptExportTask(endDate, rptSearchCondition.getQuarter(), user);
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

