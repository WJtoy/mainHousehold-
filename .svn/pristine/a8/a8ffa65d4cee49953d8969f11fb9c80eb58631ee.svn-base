package com.wolfking.jeesite.ms.providerrpt.controller;

import com.kkl.kklplus.entity.praise.PraiseStatusEnum;
import com.kkl.kklplus.entity.rpt.RPTKeFuPraiseDetailsEntity;
import com.kkl.kklplus.entity.rpt.exception.RPTBaseException;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.md.entity.Engineer;
import com.wolfking.jeesite.modules.md.service.ServicePointService;
import com.wolfking.jeesite.modules.rpt.entity.RptSearchCondition;
import com.wolfking.jeesite.modules.rpt.web.BaseRptController;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providerrpt.service.MSServicePointPraiseDetailsRptService;
import com.wolfking.jeesite.ms.providerrpt.utils.MSRptUtils;
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
import java.util.Arrays;
import java.util.Date;

@Controller
@RequestMapping(value = "${adminPath}/rpt/provider/servicePointPraiseDetails/")
public class MSServicePointPraiseDetailsRptController extends BaseRptController {

    @Autowired
    private MSServicePointPraiseDetailsRptService msServicePointPraiseDetailsRptService;



    @Autowired
    private ServicePointService servicePointService;
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
        if (rptSearchCondition.getBeginDate() == null) {
            rptSearchCondition.setBeginDate(DateUtils.addDays(now ,-1));
        }
        if (rptSearchCondition.getEndDate() == null) {
            rptSearchCondition.setEndDate(now);
        }

        return rptSearchCondition;
    }


    /**
     * 网点好评明细
     *
     * @param rptSearchCondition
     * @param model
     * @return
     */
    @RequestMapping(value = "servicePointPraiseDetailsRptData")
    public String keFuPraiseDetailsRptData(RptSearchCondition rptSearchCondition, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<RPTKeFuPraiseDetailsEntity> page = new Page<>(request, response);
        User user = UserUtils.getUser();
        if (user.isEngineer()) {
            Engineer engineer = servicePointService.getEngineer(user.getEngineerId());
            rptSearchCondition.setServicePointId(engineer.getServicePoint().getId());
            rptSearchCondition.setServicePointName(engineer.getName());
        }
        rptSearchCondition.setBeginDate(DateUtils.getStartOfDay(rptSearchCondition.getBeginDate()));
        rptSearchCondition.setEndDate(DateUtils.getEndOfDay(rptSearchCondition.getEndDate()));
        if (rptSearchCondition.isSearching()) {

            page = msServicePointPraiseDetailsRptService.getPraiseOrderList(page,rptSearchCondition.getStatus(),rptSearchCondition.getServicePointId(),rptSearchCondition.getBeginDate(), rptSearchCondition.getEndDate());

        }

        model.addAttribute("page", page);
        model.addAttribute("praiseStatusEnumList", MSRptUtils.getAllPraiseTypeList());
        return "modules/providerrpt/servicePointPraiseDetailsRpt";
    }

    @ResponseBody
    @RequestMapping(value = "checkExportTask", method = RequestMethod.POST)
    public AjaxJsonEntity checkExportTask(RptSearchCondition rptSearchCondition) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try {
            User user = UserUtils.getUser();
            rptSearchCondition.setBeginDate(DateUtils.getStartOfDay(rptSearchCondition.getBeginDate()));
            rptSearchCondition.setEndDate(DateUtils.getEndOfDay(rptSearchCondition.getEndDate()));

            if (user.isEngineer()) {
                Engineer engineer = servicePointService.getEngineer(user.getEngineerId());
                rptSearchCondition.setServicePointId(engineer.getServicePoint().getId());
                rptSearchCondition.setServicePointName(engineer.getName());
            }

            msServicePointPraiseDetailsRptService.checkRptExportTask(rptSearchCondition.getStatus(),rptSearchCondition.getServicePointId(),rptSearchCondition.getBeginDate(), rptSearchCondition.getEndDate(),user);

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
            rptSearchCondition.setBeginDate(DateUtils.getStartOfDay(rptSearchCondition.getBeginDate()));
            rptSearchCondition.setEndDate(DateUtils.getEndOfDay(rptSearchCondition.getEndDate()));
            if (user.isEngineer()) {
                Engineer engineer = servicePointService.getEngineer(user.getEngineerId());
                rptSearchCondition.setServicePointId(engineer.getServicePoint().getId());
                rptSearchCondition.setServicePointName(engineer.getName());
            }


            msServicePointPraiseDetailsRptService.createRptExportTask(rptSearchCondition.getStatus(),rptSearchCondition.getServicePointId(),rptSearchCondition.getBeginDate(), rptSearchCondition.getEndDate(),user);
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
