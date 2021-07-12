package com.wolfking.jeesite.modules.finance.rpt.web;

import com.kkl.kklplus.entity.rpt.RPTServicePointInvoiceEntity;
import com.kkl.kklplus.entity.rpt.exception.RPTBaseException;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.finance.rpt.service.FiServicePointInvoiceRptService;
import com.wolfking.jeesite.modules.rpt.entity.RptSearchCondition;
import com.wolfking.jeesite.modules.rpt.web.BaseRptController;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providerrpt.service.MSServicePointInvoiceRptService;
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
@RequestMapping(value = "${adminPath}/finance/rpt/servicePointInvoice")
public class FiServicePointInvoiceRptController extends BaseRptController {

    @Autowired
    private FiServicePointInvoiceRptService fiServicePointInvoiceRptService;

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

    @RequiresPermissions("rpt:finance:servicePointInvoiceRpt:view")
    @RequestMapping(value = {"servicePointInvoiceRpt"})
    public String servicePointInvoiceRptNew(RptSearchCondition rptSearchCondition,
                                            HttpServletRequest request, HttpServletResponse response, Model model) {

        Page<RPTServicePointInvoiceEntity> page = new Page<>(request, response);
        //结账单创建时间
        Date now = new Date();
        if (rptSearchCondition.getBeginInvoiceDate() == null) {
            rptSearchCondition.setBeginInvoiceDate(DateUtils.getStartDayOfMonth(now));
        }
        if (rptSearchCondition.getEndInvoiceDate() == null) {
            rptSearchCondition.setEndInvoiceDate(now);
        }
        rptSearchCondition.setBeginDate(DateUtils.getStartOfDay(rptSearchCondition.getBeginDate()));
        rptSearchCondition.setEndDate(DateUtils.getEndOfDay(rptSearchCondition.getEndDate()));
        rptSearchCondition.setBeginInvoiceDate(DateUtils.getStartOfDay(rptSearchCondition.getBeginInvoiceDate()));
        rptSearchCondition.setEndInvoiceDate(DateUtils.getEndOfDay(rptSearchCondition.getEndInvoiceDate()));

        Integer paymentType = (rptSearchCondition.getPaymentType() == null || rptSearchCondition.getPaymentType().equals("")) ?
                null :
                StringUtils.toInteger(rptSearchCondition.getPaymentType());

        Map<String, Dict> statusMap = MSDictUtils.getDictMap("ServicePointWithdrawStatus");
        List<Dict> statusList = new ArrayList<>();
        statusMap.remove("10");
        statusMap.remove("20");
        for(Dict item: statusMap.values()){
            statusList.add(item);
        }


        if (rptSearchCondition.isSearching()) {
            page = fiServicePointInvoiceRptService.getServicePointInvoiceRptDateNew(page,rptSearchCondition.getServicePointId(),
                    rptSearchCondition.getWithdrawNo(), paymentType, rptSearchCondition.getBank(),
                    rptSearchCondition.getBeginDate(), rptSearchCondition.getEndDate(),
                    rptSearchCondition.getBeginInvoiceDate(), rptSearchCondition.getEndInvoiceDate(),rptSearchCondition.getStatus());

        }

        model.addAttribute("page", page);
        model.addAttribute("statusList", statusList);
        return "modules/finance/rpt/fiServicePointInvoiceRpt";
    }

    @ResponseBody
    @RequiresPermissions("rpt:finance:servicePointInvoiceRpt:export")
    @RequestMapping(value = "checkExportTask", method = RequestMethod.POST)
    public AjaxJsonEntity checkExportTask(RptSearchCondition rptSearchCondition) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try {

            User user = UserUtils.getUser();
            Date now = new Date();
            if (rptSearchCondition.getBeginInvoiceDate() == null) {
                rptSearchCondition.setBeginInvoiceDate(DateUtils.getStartDayOfMonth(now));
            }
            if (rptSearchCondition.getEndInvoiceDate() == null) {
                rptSearchCondition.setEndInvoiceDate(now);
            }
            rptSearchCondition.setBeginDate(DateUtils.getStartOfDay(rptSearchCondition.getBeginDate()));
            rptSearchCondition.setEndDate(DateUtils.getEndOfDay(rptSearchCondition.getEndDate()));
            rptSearchCondition.setBeginInvoiceDate(DateUtils.getStartOfDay(rptSearchCondition.getBeginInvoiceDate()));
            rptSearchCondition.setEndInvoiceDate(DateUtils.getEndOfDay(rptSearchCondition.getEndInvoiceDate()));

            Integer paymentType = (rptSearchCondition.getPaymentType() == null || rptSearchCondition.getPaymentType().equals("")) ?
                    null :
                    StringUtils.toInteger(rptSearchCondition.getPaymentType());

            fiServicePointInvoiceRptService.checkRptExportTask(rptSearchCondition.getServicePointId(),
                    rptSearchCondition.getWithdrawNo(), paymentType, rptSearchCondition.getBank(),
                    rptSearchCondition.getBeginDate(), rptSearchCondition.getEndDate(),
                    rptSearchCondition.getBeginInvoiceDate(), rptSearchCondition.getEndInvoiceDate(),rptSearchCondition.getStatus(),user);

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
    @RequiresPermissions("rpt:finance:servicePointInvoiceRpt:export")
    @RequestMapping(value = "export", method = RequestMethod.POST)
    public AjaxJsonEntity export(RptSearchCondition rptSearchCondition) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try {

            User user = UserUtils.getUser();
            Date now = new Date();
            if (rptSearchCondition.getBeginInvoiceDate() == null) {
                rptSearchCondition.setBeginInvoiceDate(DateUtils.getStartDayOfMonth(now));
            }
            if (rptSearchCondition.getEndInvoiceDate() == null) {
                rptSearchCondition.setEndInvoiceDate(now);
            }
            rptSearchCondition.setBeginDate(DateUtils.getStartOfDay(rptSearchCondition.getBeginDate()));
            rptSearchCondition.setEndDate(DateUtils.getEndOfDay(rptSearchCondition.getEndDate()));
            rptSearchCondition.setBeginInvoiceDate(DateUtils.getStartOfDay(rptSearchCondition.getBeginInvoiceDate()));
            rptSearchCondition.setEndInvoiceDate(DateUtils.getEndOfDay(rptSearchCondition.getEndInvoiceDate()));

            Integer paymentType = (rptSearchCondition.getPaymentType() == null || rptSearchCondition.getPaymentType().equals("")) ?
                    null :
                    StringUtils.toInteger(rptSearchCondition.getPaymentType());

            fiServicePointInvoiceRptService.createRptExportTask(rptSearchCondition.getServicePointId(),
                    rptSearchCondition.getWithdrawNo(), paymentType, rptSearchCondition.getBank(),
                    rptSearchCondition.getBeginDate(), rptSearchCondition.getEndDate(),
                    rptSearchCondition.getBeginInvoiceDate(), rptSearchCondition.getEndInvoiceDate(),rptSearchCondition.getStatus(),user);
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
