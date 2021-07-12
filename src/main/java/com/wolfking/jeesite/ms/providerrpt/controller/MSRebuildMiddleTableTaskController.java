package com.wolfking.jeesite.ms.providerrpt.controller;


import com.google.common.collect.Lists;
import com.kkl.kklplus.entity.rpt.RPTRebuildMiddleTableTaskEntity;
import com.kkl.kklplus.entity.rpt.common.RPTMiddleTableEnum;
import com.kkl.kklplus.entity.rpt.common.RPTRebuildOperationTypeEnum;
import com.kkl.kklplus.entity.rpt.exception.RPTBaseException;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.rpt.entity.RptSearchCondition;
import com.wolfking.jeesite.modules.rpt.web.BaseRptController;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providerrpt.service.MSRebuildMiddleTableTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping(value = "${adminPath}/rpt/provider/rebuildMiddleTableTask/")
public class MSRebuildMiddleTableTaskController extends BaseRptController {

    @Autowired
    private MSRebuildMiddleTableTaskService msRebuildMiddleTableTaskService;

//    @ModelAttribute("rptSearchCondition")
//    public RptSearchCondition get(@ModelAttribute("rptSearchCondition") RptSearchCondition rptSearchCondition) {
//        if (rptSearchCondition == null) {
//            rptSearchCondition = new RptSearchCondition();
//        }
//        Date now = new Date();
//        if (rptSearchCondition.getBeginDate() == null) {
//            rptSearchCondition.setBeginDate(DateUtils.addDays(now, -1));
//        }
//        if (rptSearchCondition.getEndDate() == null) {
//            rptSearchCondition.setEndDate(now);
//        }
//        return rptSearchCondition;
//    }

    @RequestMapping(value = "reportMiddleTableList")
    public String reportMiddleTableList(HttpServletRequest request, HttpServletResponse response, Model model) {
        User user = UserUtils.getUser();

        List<RPTMiddleTableEnum> middleTables = Lists.newArrayList();
        if (user.isAdmin()) {
            middleTables.addAll(RPTMiddleTableEnum.getAllMiddleTable());
        }
        model.addAttribute("list", middleTables);
        return "modules/providerrpt/reportMiddleTableList";
    }

    @RequestMapping(value = "rebuildForm", method = RequestMethod.GET)
    public String rebuildForm(Integer midTableId, Integer operationType, HttpServletRequest request, Model model) {
        RPTMiddleTableEnum tableEnum = RPTMiddleTableEnum.valueOf(midTableId);
        RPTRebuildOperationTypeEnum operationTypeEnum = RPTRebuildOperationTypeEnum.valueOf(operationType);

        RptSearchCondition searchCondition = new RptSearchCondition();
        Date now = new Date();
        searchCondition.setBeginDate(DateUtils.addDays(now, -1));
        searchCondition.setEndDate(DateUtils.addDays(now, -1));
        Date lastMonth = DateUtils.addMonth(now, -1);
        searchCondition.setSelectedYear(DateUtils.getYear(lastMonth));
        searchCondition.setSelectedMonth(DateUtils.getMonth(lastMonth));
        if (tableEnum != null && operationTypeEnum != null) {
            searchCondition.setMiddleTableId(tableEnum.getValue());
            searchCondition.setMiddleTableType(tableEnum.getType());
            searchCondition.setRebuildOperationType(operationTypeEnum.getValue());
        }
        model.addAttribute("searchCondition", searchCondition);
        return "modules/providerrpt/rebuildMidTableForm";
    }

    @ResponseBody
    @RequestMapping(value = "rebuild", method = RequestMethod.POST)
    public AjaxJsonEntity rebuild(RptSearchCondition params) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try {
            Date now = new Date();
//            int hourIndex = DateUtils.getHourOfDay(now);
//            if (hourIndex > 8 && hourIndex < 13) {
//                result.setSuccess(false);
//                result.setMessage("只允许在20:00 ~ 8:00执行重建报表中间表任务");
//            }
//            else {
                User user = UserUtils.getUser();
                if (user.isAdmin()) {
                    msRebuildMiddleTableTaskService.createRebuildMiddleTableTask(params);
                } else {
                    result.setSuccess(false);
                    result.setMessage("只允许系统管理重建报表中间表");
                }
//            }
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage(e.getMessage());
        }
        return result;
    }

    @RequestMapping(value = "logList")
    public String customerNewOrderDailyRpt(Integer middleTableId,
                                           HttpServletRequest request, HttpServletResponse response, Model model) {
        User user = UserUtils.getUser();
        Page<RPTRebuildMiddleTableTaskEntity> page = new Page<>(request, response);
        if (user.isAdmin() && RPTMiddleTableEnum.isMiddleTableId(middleTableId)) {
            try {
                page = msRebuildMiddleTableTaskService.getTaskList(page, middleTableId);
            } catch (RPTBaseException e) {
                addMessage(model, e.getMessage());
            }
        }
        model.addAttribute("middleTableId", middleTableId);
        model.addAttribute("page", page);
        return "modules/providerrpt/rebuildMiddleTableLogList";
    }

}

