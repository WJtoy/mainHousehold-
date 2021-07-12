package com.wolfking.jeesite.ms.providerrpt.controller;

import com.kkl.kklplus.entity.rpt.RPTAreaOrderPlanDailyEntity;
import com.kkl.kklplus.entity.rpt.RPTEveryDayCompleteEntity;
import com.kkl.kklplus.entity.rpt.exception.RPTBaseException;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.md.entity.ProductCategory;
import com.wolfking.jeesite.modules.md.utils.ProductUtils;
import com.wolfking.jeesite.modules.rpt.entity.RptSearchCondition;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.service.SystemService;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;

import com.wolfking.jeesite.ms.providerrpt.service.MSEveryDayCompleteService;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Auther wj
 * @Date 2021/5/27 9:35
 */
@Controller
@RequestMapping("${adminPath}/rpt/provider/everyDayComplete/")
public class MSEveryDayCompleteController {

    @Autowired
    private MSEveryDayCompleteService msEveryDayCompleteService;

    @Autowired
    private SystemService systemService;

    @Autowired
    private AreaService areaService;

    @ModelAttribute("rptSearchCondition")
    public RptSearchCondition get(@ModelAttribute("rptSearchCondition") RptSearchCondition rptSearchCondition) {
        if (rptSearchCondition == null) {
            rptSearchCondition = new RptSearchCondition();
        }
        Date now = new Date();
        if (rptSearchCondition.getEndDate() == null) {
            rptSearchCondition.setEndDate(now);
        }
        if (rptSearchCondition.getBeginDate() == null){
            rptSearchCondition.setBeginDate(now);
        }
        return rptSearchCondition;

    }

    /**
     *
     */
    @RequestMapping(value = "areaOrderCompleteRateReport")
    public String areaOrderCompleteRateReport(RptSearchCondition rptSearchCondition, Model model) {
        Map<String, List<RPTEveryDayCompleteEntity>> entityMap = new HashMap<>();

        if (rptSearchCondition.isSearching()) {
            Date endDate = DateUtils.parseDate(rptSearchCondition.getDateString());
            rptSearchCondition.setEndDate(endDate);
            Integer type = 0;
            if (rptSearchCondition.getAreaId() != null) {
                Area area = areaService.getFromCache(rptSearchCondition.getAreaId());
                type = area.getType();
            }


          entityMap = msEveryDayCompleteService.getAreaOrderRateDailyList(endDate,type, rptSearchCondition.getAreaId(), rptSearchCondition.getCustomerId());
        }
        if(!entityMap.isEmpty()){
            model.addAttribute(RPTEveryDayCompleteEntity.MAP_KEY_PROVINCELIST, entityMap.get(RPTEveryDayCompleteEntity .MAP_KEY_PROVINCELIST));
            model.addAttribute(RPTEveryDayCompleteEntity .MAP_KEY_CITYLIST, entityMap.get(RPTEveryDayCompleteEntity .MAP_KEY_CITYLIST));
        }
        model.addAttribute("dateStr",rptSearchCondition.getDateString());
        return "modules/providerrpt/everyDayCompleteRateReport";
    }

    @ResponseBody
    @RequestMapping(value = "checkExportTask", method = RequestMethod.POST)
    public AjaxJsonEntity checkExportTask(RptSearchCondition rptSearchCondition) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try {
            Integer areaType = 0;
            if (rptSearchCondition.getAreaId() != null) {
                Area area = areaService.getFromCache(rptSearchCondition.getAreaId());
                areaType = area.getType();
            }
            User user = UserUtils.getUser();
            Date queryDate = DateUtils.parseDate(rptSearchCondition.getDateString());
            msEveryDayCompleteService.checkRptExportTask(queryDate,areaType,rptSearchCondition.getAreaId(),rptSearchCondition.getCustomerId(),user);
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
            Integer areaType = 0;
            if (rptSearchCondition.getAreaId() != null) {
                Area area = areaService.getFromCache(rptSearchCondition.getAreaId());
                areaType = area.getType();
            }
            User user = UserUtils.getUser();
            Date queryDate = DateUtils.parseDate(rptSearchCondition.getDateString());
            msEveryDayCompleteService.createRptExportTask(queryDate,areaType,rptSearchCondition.getAreaId(),rptSearchCondition.getCustomerId(),user);
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
