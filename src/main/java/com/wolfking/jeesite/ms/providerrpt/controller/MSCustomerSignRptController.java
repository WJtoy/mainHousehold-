package com.wolfking.jeesite.ms.providerrpt.controller;

import com.kkl.kklplus.entity.b2bcenter.md.B2BSign;
import com.kkl.kklplus.entity.rpt.RPTServicePointInvoiceEntity;
import com.kkl.kklplus.entity.rpt.exception.RPTBaseException;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.rpt.entity.RptSearchCondition;
import com.wolfking.jeesite.modules.rpt.web.BaseRptController;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providerrpt.service.MSCustomerSignRptService;
import com.wolfking.jeesite.ms.providerrpt.service.MSServicePointInvoiceRptService;
import com.wolfking.jeesite.ms.providerrpt.utils.MSRptUtils;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
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
import java.util.Map;

@Controller
@RequestMapping(value = "${adminPath}/rpt/provider/customerSign")
public class MSCustomerSignRptController extends BaseRptController {

    @Autowired
    private MSCustomerSignRptService msCustomerSignRptService;

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
    @RequiresPermissions("rpt:customerSignRpt:view")
    @RequestMapping(value = {"customerSignRpt"})
    public String customerSignRpt(RptSearchCondition rptSearchCondition,
                                            HttpServletRequest request, HttpServletResponse response, Model model) {

        Page<B2BSign> page = new Page<>(request, response);
        //结账单创建时间
        Date now = new Date();
        if (rptSearchCondition.getBeginDate() == null) {
            rptSearchCondition.setBeginDate(DateUtils.getStartDayOfMonth(now));
        }
        if (rptSearchCondition.getEndDate() == null) {
            rptSearchCondition.setEndDate(now);
        }
        rptSearchCondition.setBeginDate(DateUtils.getStartOfDay(rptSearchCondition.getBeginDate()));
        rptSearchCondition.setEndDate(DateUtils.getEndOfDay(rptSearchCondition.getEndDate()));

        if (rptSearchCondition.isSearching()) {
            page = msCustomerSignRptService.getCustomerSignList(page,rptSearchCondition.getMallId(), rptSearchCondition.getMallName(), rptSearchCondition.getMobile(),
                    rptSearchCondition.getStatus(),rptSearchCondition.getBeginDate(), rptSearchCondition.getEndDate());

        }

        model.addAttribute("page", page);
        model.addAttribute("customerSignEnumList", MSRptUtils.getCustomerSignReportList());
        return "modules/providerrpt/customerSignListRpt";
    }

    @ResponseBody
    @RequiresPermissions("rpt:customerSignRpt:export")
    @RequestMapping(value = "checkExportTask", method = RequestMethod.POST)
    public AjaxJsonEntity checkExportTask(RptSearchCondition rptSearchCondition) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try {

            User user = UserUtils.getUser();
            Date now = new Date();
            if (rptSearchCondition.getBeginDate() == null) {
                rptSearchCondition.setBeginDate(DateUtils.getStartDayOfMonth(now));
            }
            if (rptSearchCondition.getEndDate() == null) {
                rptSearchCondition.setEndDate(now);
            }
            rptSearchCondition.setBeginDate(DateUtils.getStartOfDay(rptSearchCondition.getBeginDate()));
            rptSearchCondition.setEndDate(DateUtils.getEndOfDay(rptSearchCondition.getEndDate()));

            msCustomerSignRptService.checkRptExportTask(rptSearchCondition.getMallId(), rptSearchCondition.getMallName(), rptSearchCondition.getMobile(),
                    rptSearchCondition.getStatus(),rptSearchCondition.getBeginDate(), rptSearchCondition.getEndDate(),user);

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
    @RequiresPermissions("rpt:customerSignRpt:export")
    @RequestMapping(value = "export", method = RequestMethod.POST)
    public AjaxJsonEntity export(RptSearchCondition rptSearchCondition) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try {

            User user = UserUtils.getUser();
            Date now = new Date();
            if (rptSearchCondition.getBeginDate() == null) {
                rptSearchCondition.setBeginDate(DateUtils.getStartDayOfMonth(now));
            }
            if (rptSearchCondition.getEndDate() == null) {
                rptSearchCondition.setEndDate(now);
            }
            rptSearchCondition.setBeginDate(DateUtils.getStartOfDay(rptSearchCondition.getBeginDate()));
            rptSearchCondition.setEndDate(DateUtils.getEndOfDay(rptSearchCondition.getEndDate()));


            msCustomerSignRptService.createRptExportTask(rptSearchCondition.getMallId(), rptSearchCondition.getMallName(), rptSearchCondition.getMobile(),
                    rptSearchCondition.getStatus(),rptSearchCondition.getBeginDate(), rptSearchCondition.getEndDate(),user);
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
