package com.wolfking.jeesite.ms.providerrpt.controller;

import com.google.common.collect.Lists;
import com.kkl.kklplus.entity.rpt.RPTServicePointBaseInfoEntity;
import com.kkl.kklplus.entity.rpt.exception.RPTBaseException;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.rpt.entity.RptSearchCondition;
import com.wolfking.jeesite.modules.rpt.web.BaseRptController;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.service.SystemService;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providerrpt.service.MSServicePointBaseRptService;
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
@RequestMapping(value = "${adminPath}/rpt/provider/servicePointBaseInfo/")
public class MSServicePointBaseRptController extends BaseRptController {


    @Autowired
    private AreaService areaService;

    @Autowired
    private MSServicePointBaseRptService msServicePointBaseRptService;

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

        Date now = new Date(); //默认使用当天作为查询条件
        if (rptSearchCondition.getSelectedYear() == null) {
            rptSearchCondition.setSelectedYear(DateUtils.getYear(now));
        }
        if (rptSearchCondition.getSelectedMonth() == null) {
            rptSearchCondition.setSelectedMonth(DateUtils.getMonth(now));
        }

        return rptSearchCondition;
    }

    /**
     * 网点基础资料
     *
     * @param rptSearchCondition
     * @return
     */
    @RequestMapping(value = "servicePointBaseInfoRpt")
    public String servicePointBaseInfoRpt(RptSearchCondition rptSearchCondition, HttpServletRequest request, HttpServletResponse response, Model model) {

        Page<RPTServicePointBaseInfoEntity> page = new Page<>(request, response);
        if (rptSearchCondition.isSearching()) {
            Integer type = 0;
            if (rptSearchCondition.getAreaId() != null) {
                Area area = areaService.getFromCache(rptSearchCondition.getAreaId());
                type = area.getType();
            }

            page = msServicePointBaseRptService.getServicePointBaseRpt(page,rptSearchCondition.getServicePointId(),type, rptSearchCondition.getAreaId());
        }


        model.addAttribute("page", page);
        return "modules/providerrpt/servicePointBaseInfoRptReport";

    }

    @ResponseBody
    @RequestMapping(value = "checkExportTask", method = RequestMethod.POST)
    public AjaxJsonEntity checkExportTask(RptSearchCondition rptSearchCondition) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try {

            User user = UserUtils.getUser();

            if (rptSearchCondition.isSearching()) {
                Integer type = 0;
                if (rptSearchCondition.getAreaId() != null) {
                    Area area = areaService.getFromCache(rptSearchCondition.getAreaId());
                    type = area.getType();
                }

                msServicePointBaseRptService.checkRptExportTask(rptSearchCondition.getServicePointId(),
                        type,rptSearchCondition.getAreaId(),user);
            }
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

            if (rptSearchCondition.isSearching()) {
                Integer type = 0;
                if (rptSearchCondition.getAreaId() != null) {
                    Area area = areaService.getFromCache(rptSearchCondition.getAreaId());
                    type = area.getType();
                }
                msServicePointBaseRptService.createRptExportTask(rptSearchCondition.getServicePointId(), type, rptSearchCondition.getAreaId(), user);
                result.setMessage("报表导出任务创建成功，请前往'报表中心->报表下载'功能下载");

            }

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
