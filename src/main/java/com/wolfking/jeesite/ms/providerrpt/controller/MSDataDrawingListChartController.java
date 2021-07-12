package com.wolfking.jeesite.ms.providerrpt.controller;

import com.kkl.kklplus.entity.rpt.RPTCustomerComplainChartEntity;
import com.kkl.kklplus.entity.rpt.RPTCustomerReminderEntity;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.rpt.entity.RptSearchCondition;
import com.wolfking.jeesite.modules.rpt.web.BaseRptController;
import com.wolfking.jeesite.ms.providerrpt.service.MSDataDrawingListChartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(value = "${adminPath}/rpt/provider/dataDrawingList/")
public class MSDataDrawingListChartController extends BaseRptController {


    @Autowired
    private MSDataDrawingListChartService msDataDrawingListChartService;

    @RequestMapping(value = "dataDrawingListChart")
    public String getQueryDate(RptSearchCondition rptSearchCondition){
        Date date = new Date();
        if (rptSearchCondition.getEndDate() == null) {
            rptSearchCondition.setEndDate(DateUtils.getEndOfDay(date));
        } else {
            rptSearchCondition.setEndDate(rptSearchCondition.getEndDate());
        }
        rptSearchCondition.setEndPlanDate(date);

        return "modules/providerrpt/chart/dataDrawingListChart";
    }


    @ResponseBody
    @RequestMapping(value = "customerPlanChart")
    public AjaxJsonEntity customerPlanChart(Long endDate) {
        Map<String, Object> map = msDataDrawingListChartService.getOrderDataChartList(endDate);
        AjaxJsonEntity jsonEntity=new AjaxJsonEntity();
        jsonEntity.setSuccess(false);
        jsonEntity.setData(map);
        return jsonEntity;
    }

    @ResponseBody
    @RequestMapping(value = "orderQtyDailyChart")
    public AjaxJsonEntity OrderSituationChart(Long endDate) {
        Map<String, Object> map = msDataDrawingListChartService.getOrderQtyDailyChartData(endDate);
        AjaxJsonEntity jsonEntity=new AjaxJsonEntity();
        jsonEntity.setSuccess(false);
        jsonEntity.setData(map);
        return jsonEntity;
    }
    @ResponseBody
    @RequestMapping(value = "keFuCompletionTimeInstallChart")
    public AjaxJsonEntity keFuCompletionTimeInstallChart(Long endDate) {
        Map<String, Object> map = msDataDrawingListChartService.getKeFuCompleteTimeInstallChartList(endDate);
        AjaxJsonEntity jsonEntity=new AjaxJsonEntity();
        jsonEntity.setSuccess(false);
        jsonEntity.setData(map);
        return jsonEntity;
    }

    @ResponseBody
    @RequestMapping(value = "keFuCompletionTimeMaintainChart")
    public AjaxJsonEntity keFuCompletionTimeMaintainChart(Long endDate) {
        Map<String, Object> map = msDataDrawingListChartService.getKeFuCompletionTimeMaintainChartList(endDate);
        AjaxJsonEntity jsonEntity=new AjaxJsonEntity();
        jsonEntity.setSuccess(false);
        jsonEntity.setData(map);
        return jsonEntity;
    }

    @ResponseBody
    @RequestMapping(value = "customerComplaintChart")
    public AjaxJsonEntity customerComplaintChart(Long endDate) {
        RPTCustomerComplainChartEntity entity = msDataDrawingListChartService.getCustomerComplainChart(endDate);
        AjaxJsonEntity jsonEntity=new AjaxJsonEntity();
        jsonEntity.setSuccess(false);
        jsonEntity.setData(entity);
        return jsonEntity;
    }

    @ResponseBody
    @RequestMapping(value = "reminderChart")
    public AjaxJsonEntity reminderChart(Long endDate) {
        RPTCustomerReminderEntity entity = msDataDrawingListChartService.getCustomerReminderChart(endDate);
        AjaxJsonEntity jsonEntity=new AjaxJsonEntity();
        jsonEntity.setSuccess(false);
        jsonEntity.setData(entity);
        return jsonEntity;
    }

    @ResponseBody
    @RequestMapping(value = "praiseOrderChart")
    public AjaxJsonEntity praiseOrderChart(Long endDate) {
        AjaxJsonEntity jsonEntity=new AjaxJsonEntity();
        jsonEntity.setSuccess(false);
        //jsonEntity.setData(entity);
        return jsonEntity;
    }

    @ResponseBody
    @RequestMapping(value = "crushOrderChart")
    public AjaxJsonEntity crushOrderChart(Long endDate) {
        Map<String, Object> map = msDataDrawingListChartService.getOrderCrushQtyChart(endDate);
        AjaxJsonEntity jsonEntity=new AjaxJsonEntity();
        jsonEntity.setSuccess(false);
        jsonEntity.setData(map);
        return jsonEntity;
    }

    @ResponseBody
    @RequestMapping(value = "incurExpenseChart")
    public AjaxJsonEntity incurExpenseChart(Long endDate) {
        List<Double> list = msDataDrawingListChartService.getIncurExpenseChart(endDate);
        AjaxJsonEntity jsonEntity=new AjaxJsonEntity();
        jsonEntity.setSuccess(false);
        jsonEntity.setData(list);
        return jsonEntity;
    }
    @ResponseBody
    @RequestMapping(value = "servicePointQtyChart")
    public AjaxJsonEntity servicePointQtyChart(Long endDate) {
        Map<String, Object> map = msDataDrawingListChartService.getServicePointQtyChart(endDate);
    AjaxJsonEntity jsonEntity=new AjaxJsonEntity();
        jsonEntity.setSuccess(false);
        jsonEntity.setData(map);
        return jsonEntity;
}


    @ResponseBody
    @RequestMapping(value = "servicePointStreetChart")
    public AjaxJsonEntity servicePointStreetChart(Long endDate) {
        Map<String, Object> map = msDataDrawingListChartService.getServicePointStreetQtyChart(endDate);
        AjaxJsonEntity jsonEntity=new AjaxJsonEntity();
        jsonEntity.setSuccess(false);
        jsonEntity.setData(map);
        return jsonEntity;
    }


    @RequestMapping(value = "getOrderPlanDailyChart")
    public String getOrderPlanDailyChart(RptSearchCondition rptSearchCondition,Date endDate, Model model){
        if(endDate == null){
            Date date = new Date();
            if (rptSearchCondition.getEndDate() == null) {
                rptSearchCondition.setEndDate(DateUtils.getEndOfDay(date));
            } else {
                rptSearchCondition.setEndDate(rptSearchCondition.getEndDate());
            }
            rptSearchCondition.setEndPlanDate(date);
        }else {
            rptSearchCondition.setEndDate(endDate);
        }
        Map<String, Object> map = msDataDrawingListChartService.getOrderPlanDailyChart(rptSearchCondition.getEndDate().getTime());

        model.addAttribute("customerPlanQtyList", map.get("customerPlanQtyList"));
        model.addAttribute("createDate", map.get("createDate"));
        model.addAttribute("orderPlanQty", map.get("orderPlanQty"));
        model.addAttribute("productPlan1Qty", map.get("productPlan1Qty"));
        model.addAttribute("productPlan2Qty", map.get("productPlan2Qty"));
        model.addAttribute("productPlan3Qty", map.get("productPlan3Qty"));
        model.addAttribute("customerProductCategory1", map.get("customerProductCategory1"));
        model.addAttribute("customerProductCategory2", map.get("customerProductCategory2"));
        model.addAttribute("customerProductCategory3", map.get("customerProductCategory3"));
        model.addAttribute("productCategoryName", map.get("productCategoryName"));
        model.addAttribute("productCategory1Name", map.get("productCategory1Name"));
        model.addAttribute("productCategory2Name", map.get("productCategory2Name"));
        model.addAttribute("productCategory3Name", map.get("productCategory3Name"));
        return "modules/providerrpt/chart/customerPlanDetailsChart";
    }
}
