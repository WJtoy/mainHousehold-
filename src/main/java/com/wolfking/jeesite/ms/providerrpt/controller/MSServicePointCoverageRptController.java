package com.wolfking.jeesite.ms.providerrpt.controller;

import com.kkl.kklplus.entity.rpt.RPTServicePointCoverageEntity;
import com.kkl.kklplus.entity.rpt.exception.RPTBaseException;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.modules.rpt.web.BaseRptController;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providerrpt.service.MSServicePointCoverageRptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(value = "${adminPath}/rpt/provider/servicePointCoverage/")
public class MSServicePointCoverageRptController extends BaseRptController {

    @Autowired
    private MSServicePointCoverageRptService msServicePointCoverageRptService;

    /**
     * 网点覆盖报表
     * @param model
     * @return
     */
    @SuppressWarnings("deprecation")
    @RequestMapping(value = "servicePointCoverageReport")
    public String servicePointCoverageReport(@RequestParam Map<String, Object> paramMap, Model model) {
        List<RPTServicePointCoverageEntity> list = msServicePointCoverageRptService.getServicePointCoverAreasRptData();
        model.addAttribute("list", list);
        model.addAllAttributes(paramMap);
        return "modules/providerrpt/servicePointCoverageReport";
    }

    /**
     * 网点未报表
     * @param model
     * @return
     */
    @SuppressWarnings("deprecation")
    @RequestMapping(value = "servicePointNoCoverageReport")
    public String servicePointNoCoverageReport(@RequestParam Map<String, Object> paramMap, Model model) {
        List<RPTServicePointCoverageEntity> list = msServicePointCoverageRptService.getServicePointNoCoverAreasRptData();
        model.addAttribute("list", list);
        model.addAllAttributes(paramMap);
        return "modules/providerrpt/servicePointNoCoverageReport";
    }

    @ResponseBody
    @RequestMapping(value = "export", method = RequestMethod.POST)
    public AjaxJsonEntity export() {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try {
            User user = UserUtils.getUser();
            msServicePointCoverageRptService.createRptExportTask(user);
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
    @RequestMapping(value = "servicePointNoCoverageExport", method = RequestMethod.POST)
    public AjaxJsonEntity servicePointNoCoverageExport() {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try {
            User user = UserUtils.getUser();
            msServicePointCoverageRptService.createServicePointNoCoverAreasRptExportTask(user);
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
