package com.wolfking.jeesite.ms.providerrpt.controller;

import com.kkl.kklplus.entity.rpt.RPTAreaOrderPlanDailyEntity;
import com.kkl.kklplus.entity.rpt.exception.RPTBaseException;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.md.entity.ProductCategory;
import com.wolfking.jeesite.modules.md.utils.ProductUtils;
import com.wolfking.jeesite.modules.rpt.entity.RptSearchCondition;
import com.wolfking.jeesite.modules.rpt.web.BaseRptController;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.service.SystemService;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providerrpt.service.MSAreaOrderPlanDailyRptService;
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

@Controller
@RequestMapping(value = "${adminPath}/rpt/provider/areaOrderPlan/")
public class MSAreaOrderPlanDailyRptController extends BaseRptController {
    @Autowired
    private MSAreaOrderPlanDailyRptService msAreaOrderPlanDailyRptService;

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
        if (rptSearchCondition.getSelectedYear() == null) {
            rptSearchCondition.setSelectedYear(DateUtils.getYear(now));
        }
        if (rptSearchCondition.getSelectedMonth() == null) {
            rptSearchCondition.setSelectedMonth(DateUtils.getMonth(now));
        }
        return rptSearchCondition;

    }

    /**
     *   省市区每日下单明细
     */
    @RequestMapping(value = "areaOrderPlanDailyReport")
    public String areaOrderPlanDailyReport(RptSearchCondition rptSearchCondition, Model model) {
        Map<String, List<RPTAreaOrderPlanDailyEntity>> entityMap = new HashMap<>();
        User user = UserUtils.getUser();
        List<Long> productCategoryIds = Lists.newArrayList();
        List<ProductCategory> productCategoryList = ProductUtils.getProductCategoryRPTList();
        if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()) {
            productCategoryIds = systemService.getAuthorizedProductCategoryIds(user.getId());
            List<Long> finalProductCategoryIds = productCategoryIds;
            productCategoryList = productCategoryList.stream().filter(r -> finalProductCategoryIds.contains(r.getId())).collect(Collectors.toList());
        }
        if (rptSearchCondition.isSearching()) {
            Integer type = 0;
            if (rptSearchCondition.getAreaId() != null) {
                Area area = areaService.getFromCache(rptSearchCondition.getAreaId());
                type = area.getType();
            }
            if (rptSearchCondition.getProductCategory() != 0) {
                productCategoryIds = Lists.newArrayList();
                productCategoryIds.add(rptSearchCondition.getProductCategory().longValue());
            }
            if (productCategoryIds.isEmpty() &&
                    (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue())) {
            } else {
                entityMap = msAreaOrderPlanDailyRptService.getAreaOrderPlanDailyList(rptSearchCondition.getSelectedYear(), rptSearchCondition.getSelectedMonth(),
                        type, rptSearchCondition.getAreaId(), rptSearchCondition.getCustomerId(), productCategoryIds,rptSearchCondition.getDataSource());

            }

        }
        int days = DateUtils.getDaysOfMonth(DateUtils.parseDate(rptSearchCondition.getSelectedYear() + "-" + rptSearchCondition.getSelectedMonth() + "-01"));

        rptSearchCondition.setDays(days);
        if(!entityMap.isEmpty()){
            model.addAttribute(RPTAreaOrderPlanDailyEntity.MAP_KEY_PROVINCELIST, entityMap.get(RPTAreaOrderPlanDailyEntity.MAP_KEY_PROVINCELIST));
            model.addAttribute(RPTAreaOrderPlanDailyEntity.MAP_KEY_CITYLIST, entityMap.get(RPTAreaOrderPlanDailyEntity.MAP_KEY_CITYLIST));
            model.addAttribute(RPTAreaOrderPlanDailyEntity.MAP_KEY_AREALIST, entityMap.get(RPTAreaOrderPlanDailyEntity.MAP_KEY_AREALIST));
            model.addAttribute(RPTAreaOrderPlanDailyEntity.MAP_KEY_SUMUP, entityMap.get(RPTAreaOrderPlanDailyEntity.MAP_KEY_SUMUP).get(0));
        }

        model.addAttribute("productCategoryList", productCategoryList);
        return "modules/providerrpt/areaOrderPlanDailyReport";
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
                int selectedYear = rptSearchCondition.getSelectedYear();
                int selectedMonth = rptSearchCondition.getSelectedMonth();
                List<Long> productCategoryIds = Lists.newArrayList();
                if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()) {
                    productCategoryIds =  systemService.getAuthorizedProductCategoryIds(user.getId());
                }
                if (rptSearchCondition.getProductCategory() != 0) {
                    productCategoryIds = Lists.newArrayList();
                    productCategoryIds.add(rptSearchCondition.getProductCategory().longValue());
                }
                if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()){
                    if (productCategoryIds.isEmpty()) {
                        result.setSuccess(false);
                        result.setMessage("创建报表导出任务失败，请重试");
                        return result;
                    }
                }
                msAreaOrderPlanDailyRptService.checkRptExportTask(selectedYear,selectedMonth,areaType,rptSearchCondition.getAreaId(),rptSearchCondition.getCustomerId(),productCategoryIds,rptSearchCondition.getDataSource(),user);

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

                int selectedYear = rptSearchCondition.getSelectedYear();
                int selectedMonth = rptSearchCondition.getSelectedMonth();
                List<Long> productCategoryIds = Lists.newArrayList();
                if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()) {
                    productCategoryIds =  systemService.getAuthorizedProductCategoryIds(user.getId());
                }
                if (rptSearchCondition.getProductCategory() != 0) {
                    productCategoryIds = Lists.newArrayList();
                    productCategoryIds.add(rptSearchCondition.getProductCategory().longValue());
                }
                if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()){
                    if (productCategoryIds.isEmpty()) {
                        result.setSuccess(false);
                        result.setMessage("创建报表导出任务失败，请重试");
                        return result;
                    }
                }

                msAreaOrderPlanDailyRptService.createRptExportTask(selectedYear,selectedMonth,areaType,rptSearchCondition.getAreaId(),rptSearchCondition.getCustomerId(),productCategoryIds,rptSearchCondition.getDataSource(),user);
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
