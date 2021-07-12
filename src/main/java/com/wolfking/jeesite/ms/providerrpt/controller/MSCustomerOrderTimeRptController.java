package com.wolfking.jeesite.ms.providerrpt.controller;

import com.google.common.collect.Lists;
import com.kkl.kklplus.entity.md.GlobalMappingSalesSubFlagEnum;
import com.kkl.kklplus.entity.rpt.RPTCustomerOrderTimeEntity;
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
import com.wolfking.jeesite.ms.providerrpt.service.MSCustomerOrderTimeRptService;
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
@RequestMapping(value = "${adminPath}/rpt/provider/customerOrderTime/")
public class MSCustomerOrderTimeRptController extends BaseRptController {

    @Autowired
    private MSCustomerOrderTimeRptService msCustomerOrderTimeRptService;

    @Autowired
    private SystemService systemService;

    @Autowired
    private AreaService areaService;


    @ModelAttribute("rptSearchCondition")
    public RptSearchCondition get(@ModelAttribute("rptSearchCondition") RptSearchCondition rptSearchCondition) {
        if (rptSearchCondition == null) {
            rptSearchCondition = new RptSearchCondition();
        }
        return rptSearchCondition;
    }

    /**
     * 客户工单时效报表
     *
     * @param rptSearchCondition
     * @param model
     * @return
     */
    @SuppressWarnings("deprecation")
    @RequestMapping(value = "customerOrderTimeReport")
    public String CustomerOrderTimeReport(RptSearchCondition rptSearchCondition, Model model) {
        Date date = DateUtils.addDays(new Date(), -1);
        if (rptSearchCondition.getEndDate() == null) {
            rptSearchCondition.setEndDate(DateUtils.getEndOfDay(date));
        } else {
            rptSearchCondition.setEndDate(rptSearchCondition.getEndDate());
        }
        User user = UserUtils.getUser();
        List<Long> productCategoryIds = Lists.newArrayList();
        List<ProductCategory> productCategoryList = ProductUtils.getProductCategoryRPTList();
        if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()) {
            productCategoryIds = systemService.getAuthorizedProductCategoryIds(user.getId());
            List<Long> finalProductCategoryIds = productCategoryIds;
            productCategoryList = productCategoryList.stream().filter(r -> finalProductCategoryIds.contains(r.getId())).collect(Collectors.toList());
        }
        List<RPTCustomerOrderTimeEntity> list = Lists.newArrayList();
        if (rptSearchCondition.isSearching()) {
            Integer type = 0;
            Long customerId = rptSearchCondition.getCustomerId();
            Long salesId = null;
            Integer subFlag = null;
            if (user.isCustomer()) {
                customerId = UserUtils.getUser().getCustomerAccountProfile().getCustomer().getId();
            } else if (user.isSaleman()) {
                salesId = user.getId();
                if (user.getSubFlag()== GlobalMappingSalesSubFlagEnum.MANAGER.getValue()){
                    subFlag = GlobalMappingSalesSubFlagEnum.MANAGER.getValue();
                }
            }
            rptSearchCondition.setCustomerId(customerId);
            rptSearchCondition.setSalesId(salesId);
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
                list = msCustomerOrderTimeRptService.getCustomerOrderTimeList(rptSearchCondition.getEndDate(), type, rptSearchCondition.getAreaId(), rptSearchCondition.getSalesId(), rptSearchCondition.getCustomerId(), rptSearchCondition.getKefuId(), rptSearchCondition.getServicePointId(), rptSearchCondition.getOrderServiceType(), productCategoryIds,subFlag);
            }
        }
        rptSearchCondition.setEndPlanDate(date);
        rptSearchCondition.setList(list);
        model.addAttribute("productCategoryList", productCategoryList);
        return "modules/providerrpt/customerOrderTimeReport";
    }


    @RequestMapping(value = "customerOrderTimeChart")
    public String CustomerOrderTimeChart(RptSearchCondition rptSearchCondition, Model model) {
        Date date = DateUtils.addDays(new Date(), -1);
        if (rptSearchCondition.getEndDate() == null) {
            rptSearchCondition.setEndDate(DateUtils.getEndOfDay(date));
        } else {
            rptSearchCondition.setEndDate(rptSearchCondition.getEndDate());
        }
        User user = UserUtils.getUser();
        List<Long> productCategoryIds = Lists.newArrayList();
        List<ProductCategory> productCategoryList = ProductUtils.getProductCategoryRPTList();
        if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()) {
            productCategoryIds = systemService.getAuthorizedProductCategoryIds(user.getId());
            List<Long> finalProductCategoryIds = productCategoryIds;
            productCategoryList = productCategoryList.stream().filter(r -> finalProductCategoryIds.contains(r.getId())).collect(Collectors.toList());
        }
        Map<String, Object> map = new HashMap<>();

        Integer type = 0;
        Long customerId = rptSearchCondition.getCustomerId();
        Long salesId = null;
        Integer subFlag = null;
        if (user.isCustomer()) {
            customerId = UserUtils.getUser().getCustomerAccountProfile().getCustomer().getId();
        } else if (user.isSaleman()) {
            salesId = user.getId();
            if (user.getSubFlag() == GlobalMappingSalesSubFlagEnum.MANAGER.getValue()){
                subFlag = GlobalMappingSalesSubFlagEnum.MANAGER.getValue();
            }
        }
        rptSearchCondition.setCustomerId(customerId);
        rptSearchCondition.setSalesId(salesId);
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
            map = msCustomerOrderTimeRptService.getCustomerOrderTimeChartList(rptSearchCondition.getEndDate(), type, rptSearchCondition.getAreaId(), rptSearchCondition.getSalesId(), rptSearchCondition.getCustomerId(), rptSearchCondition.getKefuId(), rptSearchCondition.getServicePointId(), rptSearchCondition.getOrderServiceType(), productCategoryIds,subFlag);
        }

        if (!map.isEmpty()) {
            model.addAttribute("mapPlanList", map.get("mapPlanList"));
            model.addAttribute("mapCloseList", map.get("mapCloseList"));
            model.addAttribute("dateList", map.get("dateList"));
            model.addAttribute("dateAllList", map.get("dateAllList"));
            //设置柱状图表
            model.addAttribute("less12List", map.get("less12List"));
            model.addAttribute("less24List", map.get("less24List"));
            model.addAttribute("less48List", map.get("less48List"));
            model.addAttribute("less72List", map.get("less72List"));
            model.addAttribute("more72List", map.get("more72List"));

            //设置线型报表
            model.addAttribute("less12RateList", map.get("less12RateList"));
            model.addAttribute("less24RateList", map.get("less24RateList"));
            model.addAttribute("less48RateList", map.get("less48RateList"));
            model.addAttribute("less72RateList", map.get("less72RateList"));
            model.addAttribute("more72RateList", map.get("more72RateList"));
        }
        model.addAttribute("productCategoryList", productCategoryList);
        return "modules/providerrpt/customerOrderTimeChart";
    }

    @ResponseBody
    @RequestMapping(value = "checkExportTask", method = RequestMethod.POST)
    public AjaxJsonEntity checkExportTask(RptSearchCondition rptSearchCondition) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try {

            Date date = DateUtils.addDays(new Date(), -1);
            if (rptSearchCondition.getEndDate() == null) {
                rptSearchCondition.setEndDate(DateUtils.getEndOfDay(date));
            } else {
                rptSearchCondition.setEndDate(rptSearchCondition.getEndDate());
            }
            User user = UserUtils.getUser();
            List<Long> productCategoryIds = Lists.newArrayList();
            Integer type = 0;
            Long customerId = rptSearchCondition.getCustomerId();
            Long salesId = null;
            Integer subFlag= null;
            if (user.isCustomer()) {
                customerId = UserUtils.getUser().getCustomerAccountProfile().getCustomer().getId();
            } else if (user.isSaleman()) {
                salesId = user.getId();
                if (user.getSubFlag() == GlobalMappingSalesSubFlagEnum.MANAGER.getValue()){
                    subFlag= GlobalMappingSalesSubFlagEnum.MANAGER.getValue();
                }
            }
            rptSearchCondition.setCustomerId(customerId);
            rptSearchCondition.setSalesId(salesId);
            if (rptSearchCondition.getAreaId() != null) {
                Area area = areaService.getFromCache(rptSearchCondition.getAreaId());
                type = area.getType();
            }

            if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()) {
                productCategoryIds =  systemService.getAuthorizedProductCategoryIds(user.getId());
            }
            if (rptSearchCondition.getProductCategory() != 0) {
                productCategoryIds = Lists.newArrayList();
                productCategoryIds.add(rptSearchCondition.getProductCategory().longValue());
            }
            if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()) {
                if (productCategoryIds.isEmpty()) {
                    result.setSuccess(false);
                    result.setMessage("创建报表导出任务失败，请重试");
                    return result;
                }
            }
            msCustomerOrderTimeRptService.checkRptExportTask(rptSearchCondition.getEndDate(), type, rptSearchCondition.getAreaId(), rptSearchCondition.getSalesId(), rptSearchCondition.getCustomerId(), rptSearchCondition.getKefuId(), rptSearchCondition.getServicePointId(), rptSearchCondition.getOrderServiceType(), productCategoryIds, user,subFlag);

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

            Date date = DateUtils.addDays(new Date(), -1);
            if (rptSearchCondition.getEndDate() == null) {
                rptSearchCondition.setEndDate(DateUtils.getEndOfDay(date));
            } else {
                rptSearchCondition.setEndDate(rptSearchCondition.getEndDate());
            }
            User user = UserUtils.getUser();
            List<Long> productCategoryIds = Lists.newArrayList();
            Integer type = 0;
            Long customerId = rptSearchCondition.getCustomerId();
            Long salesId = null;
            Integer subFlag = null;
            if (user.isCustomer()) {
                customerId = UserUtils.getUser().getCustomerAccountProfile().getCustomer().getId();
            } else if (user.isSaleman()) {
                salesId = user.getId();
                if (user.getSubFlag() == GlobalMappingSalesSubFlagEnum.MANAGER.getValue()){
                    subFlag = GlobalMappingSalesSubFlagEnum.MANAGER.getValue();
                }
            }
            rptSearchCondition.setCustomerId(customerId);
            rptSearchCondition.setSalesId(salesId);
            if (rptSearchCondition.getAreaId() != null) {
                Area area = areaService.getFromCache(rptSearchCondition.getAreaId());
                type = area.getType();
            }
            if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()) {
                productCategoryIds =  systemService.getAuthorizedProductCategoryIds(user.getId());
            }
            if (rptSearchCondition.getProductCategory() != 0) {
                productCategoryIds = Lists.newArrayList();
                productCategoryIds.add(rptSearchCondition.getProductCategory().longValue());
            }
            if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()) {
                if (productCategoryIds.isEmpty()) {
                    result.setSuccess(false);
                    result.setMessage("创建报表导出任务失败，请重试");
                    return result;
                }
            }
            msCustomerOrderTimeRptService.createRptExportTask(rptSearchCondition.getEndDate(), type, rptSearchCondition.getAreaId(), rptSearchCondition.getSalesId(), rptSearchCondition.getCustomerId(), rptSearchCondition.getKefuId(), rptSearchCondition.getServicePointId(), rptSearchCondition.getOrderServiceType(), productCategoryIds, user,subFlag);
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
