package com.wolfking.jeesite.ms.providerrpt.controller;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.kkl.kklplus.entity.rpt.RPTCompletedOrderDetailsEntity;
import com.kkl.kklplus.entity.rpt.RPTKeFuAverageOrderFeeEntity;
import com.kkl.kklplus.entity.rpt.exception.RPTBaseException;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.ProductCategory;
import com.wolfking.jeesite.modules.md.utils.CustomerUtils;
import com.wolfking.jeesite.modules.md.utils.ProductUtils;
import com.wolfking.jeesite.modules.rpt.entity.RptSearchCondition;
import com.wolfking.jeesite.modules.rpt.web.BaseRptController;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.KefuTypeEnum;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.service.SystemService;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providerrpt.service.MSCompletedOrderNewRptService;
import com.wolfking.jeesite.ms.providerrpt.service.MSKeFuAverageOrderFeeRptService;
import com.wolfking.jeesite.ms.utils.MSUserUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "${adminPath}/rpt/provider/keFuAverageOrderFee/")
public class MSKeFuAverageOrderFeeRptController extends BaseRptController {

    @Autowired
    private MSKeFuAverageOrderFeeRptService msKeFuAverageOrderFeeRptService;

    @Autowired
    private SystemService systemService;

    @Autowired
    private AreaService areaService;

    @Value("${site.code}")
    private String siteCode;


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
        if (rptSearchCondition.getSelectedYear() == null) {
            rptSearchCondition.setSelectedYear(DateUtils.getYear(now));
        }
        if (rptSearchCondition.getSelectedMonth() == null) {
            rptSearchCondition.setSelectedMonth(DateUtils.getMonth(now));
        }
        return rptSearchCondition;

    }
    /**
     * 客服均单费用（非KA）
     *
     * @param rptSearchCondition
     * @param model
     * @return
     */
    @RequiresPermissions("rpt:keFuAverageOrderFee:view")
    @RequestMapping(value = "keFuAverageOrderFeeReport")
    public String keFuAverageOrderFeeReport(RptSearchCondition rptSearchCondition, Model model) {
        List<RPTKeFuAverageOrderFeeEntity> list = Lists.newArrayList();
        List<ProductCategory> productCategoryList = ProductUtils.getProductCategoryRPTList();
        User user = UserUtils.getUser();
        List<Long> productCategoryIds = Lists.newArrayList();
        Long kefuId = 0L;
        if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()) {
            productCategoryIds = systemService.getAuthorizedProductCategoryIds(user.getId());
            List<Long> finalProductCategoryIds = productCategoryIds;
            productCategoryList = productCategoryList.stream().filter(r -> finalProductCategoryIds.contains(r.getId())).collect(Collectors.toList());
        }
        if (rptSearchCondition.isSearching()) {
            Integer type = 0;
            if(user.isKefu()){
                Map<Long, User> userMap = MSUserUtils.getMapByUserType(2);
                kefuId = user.getId();
                User kefu = userMap.get(kefuId);
                if(kefu != null){
                    if(kefu.getSubFlag() == 1){
                        return "modules/providerrpt/keFuAverageOrderFeeRpt";
                    }
                }
            }
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
                list = msKeFuAverageOrderFeeRptService.getKeFuAverageOrderFeeList(productCategoryIds,kefuId, rptSearchCondition.getCustomerId(),type,rptSearchCondition.getAreaId(),rptSearchCondition.getSelectedYear(),rptSearchCondition.getSelectedMonth());
            }
        }

        model.addAttribute("list", list);
        model.addAttribute("siteCode", siteCode);
        model.addAttribute("productCategoryList", productCategoryList);
        return "modules/providerrpt/keFuAverageOrderFeeRpt";
    }


    /**
     * 厨电客服均单费用（非KA）
     *
     * @param rptSearchCondition
     * @param model
     * @return
     */
    @RequiresPermissions("rpt:keFuAverageOrderFee:view")
    @RequestMapping(value = "CWKeFuAverageOrderFeeReport")
    public String CWKeFuAverageOrderFeeReport(RptSearchCondition rptSearchCondition, Model model) {
        List<RPTKeFuAverageOrderFeeEntity> list = Lists.newArrayList();
        List<ProductCategory> productCategoryList = ProductUtils.getProductCategoryRPTList();
        User user = UserUtils.getUser();
        List<Long> productCategoryIds = Lists.newArrayList();
        productCategoryIds.add(5L);
        productCategoryIds.add(11L);
        productCategoryIds.add(12L);
        productCategoryIds.add(1L);
        List<Long> finalProductCategoryIds = productCategoryIds;
        productCategoryList = productCategoryList.stream().filter(x -> finalProductCategoryIds.contains(x.getId())).collect(Collectors.toList());

        if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()) {
            List <Long> keFuProductCategoryIds =  systemService.getAuthorizedProductCategoryIds(user.getId());
            productCategoryIds = productCategoryIds.stream().filter(r -> keFuProductCategoryIds.contains(r.longValue())).collect(Collectors.toList());
            List<Long> finalProductCategoryIds1 = productCategoryIds;
            productCategoryList = productCategoryList.stream().filter(r -> finalProductCategoryIds1.contains(r.getId())).collect(Collectors.toList());
        }

        Long kefuId = 0L;
        if (rptSearchCondition.isSearching()) {
            Integer type = 0;
            if(user.isKefu()){
                Map<Long, User> userMap = MSUserUtils.getMapByUserType(2);
                kefuId = user.getId();
                User kefu = userMap.get(kefuId);
                if(kefu != null){
                    if(kefu.getSubFlag() == 1){
                        return "modules/providerrpt/keFuAverageOrderFeeRpt";
                    }
                }
            }
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
                list = msKeFuAverageOrderFeeRptService.getKeFuAverageOrderFeeList(productCategoryIds, kefuId, rptSearchCondition.getCustomerId(), type, rptSearchCondition.getAreaId(), rptSearchCondition.getSelectedYear(), rptSearchCondition.getSelectedMonth());
            }
        }

        model.addAttribute("list", list);
        model.addAttribute("productCategoryList", productCategoryList);
        return "modules/providerrpt/cwKeFuAverageOrderFeeRpt";
    }



    /**
     * 客服均单费用（KA）
     *
     * @param rptSearchCondition
     * @param model
     * @return
     */
    @RequiresPermissions("rpt:keFuAverageOrderFee:view")
    @RequestMapping(value = "vipKeFuAverageOrderFeeReport")
    public String vipKeFuAverageOrderFeeReport(RptSearchCondition rptSearchCondition, Model model) {
        List<RPTKeFuAverageOrderFeeEntity> list = Lists.newArrayList();
        List<ProductCategory> productCategoryList = ProductUtils.getProductCategoryRPTList();
        User user = UserUtils.getUser();
        List<Long> productCategoryIds = Lists.newArrayList();
        Long kefuId = 0L;
        if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()) {
            productCategoryIds = systemService.getAuthorizedProductCategoryIds(user.getId());
            List<Long> finalProductCategoryIds = productCategoryIds;
            productCategoryList = productCategoryList.stream().filter(r -> finalProductCategoryIds.contains(r.getId())).collect(Collectors.toList());
        }
        if (rptSearchCondition.isSearching()) {
            Integer type = 0;
            if(user.isKefu()){
                kefuId = user.getId();
                Map<Long, User> userMap = MSUserUtils.getMapByUserType(2);
                User kefu = userMap.get(kefuId);
                if(kefu != null){
                    if(kefu.getSubFlag() != 1){
                        return "modules/providerrpt/vipKeFuAverageOrderFeeRpt";
                    }
                }

            }
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
                list = msKeFuAverageOrderFeeRptService.getVipKeFuAverageOrderFeeList(productCategoryIds,kefuId, rptSearchCondition.getCustomerId(),type,rptSearchCondition.getAreaId(),rptSearchCondition.getSelectedYear(),rptSearchCondition.getSelectedMonth());
            }
        }

        model.addAttribute("list", list);
        model.addAttribute("siteCode", siteCode);
        model.addAttribute("productCategoryList", productCategoryList);
        return "modules/providerrpt/vipKeFuAverageOrderFeeRpt";
    }


    @ResponseBody
    @RequiresPermissions("rpt:keFuAverageOrderFee:export")
    @RequestMapping(value = "checkExportTask", method = RequestMethod.POST)
    public AjaxJsonEntity checkExportTask(RptSearchCondition rptSearchCondition) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try {

            User user = UserUtils.getUser();
            List<Long> productCategoryIds = Lists.newArrayList();
            Long kefuId = 0L;
            if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()) {
                productCategoryIds =  systemService.getAuthorizedProductCategoryIds(user.getId());
            }
            if(user.isKefu()){
                if(user.isKefu()){
                    Map<Long, User> userMap = MSUserUtils.getMapByUserType(2);
                    kefuId = user.getId();
                    User kefu = userMap.get(kefuId);
                    if(kefu != null){
                        if(kefu.getSubFlag() == 1){
                            result.setSuccess(false);
                            return result;
                        }
                    }
                }
            }
            Integer type = 0;
            if (rptSearchCondition.getAreaId() != null) {
                Area area = areaService.getFromCache(rptSearchCondition.getAreaId());
                type = area.getType();
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
            msKeFuAverageOrderFeeRptService.checkRptExportTask(productCategoryIds,kefuId, rptSearchCondition.getCustomerId(),type,rptSearchCondition.getAreaId(),rptSearchCondition.getSelectedYear(),rptSearchCondition.getSelectedMonth(),user);
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
    @RequiresPermissions("rpt:keFuAverageOrderFee:export")
    @RequestMapping(value = "export", method = RequestMethod.POST)
    public AjaxJsonEntity export(RptSearchCondition rptSearchCondition) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try {
            User user = UserUtils.getUser();
            Long kefuId = 0L;
            List<Long> productCategoryIds = Lists.newArrayList();
            if(user.isKefu()){
                kefuId = user.getId();
            }
            if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()) {
                productCategoryIds =  systemService.getAuthorizedProductCategoryIds(user.getId());
            }
            if (rptSearchCondition.getProductCategory() != 0) {
                productCategoryIds = Lists.newArrayList();
                productCategoryIds.add(rptSearchCondition.getProductCategory().longValue());
            }
            Integer type = 0;
            if (rptSearchCondition.getAreaId() != null) {
                Area area = areaService.getFromCache(rptSearchCondition.getAreaId());
                type = area.getType();
            }
            if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()){
                if (productCategoryIds.isEmpty()) {
                    result.setSuccess(false);
                    result.setMessage("创建报表导出任务失败，请重试");
                    return result;
                }
            }
            msKeFuAverageOrderFeeRptService.createRptExportTask(productCategoryIds,kefuId, rptSearchCondition.getCustomerId(),type,rptSearchCondition.getAreaId(),rptSearchCondition.getSelectedYear(),rptSearchCondition.getSelectedMonth() ,user);
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
    @RequiresPermissions("rpt:keFuAverageOrderFee:export")
    @RequestMapping(value = "checkCWExportTask", method = RequestMethod.POST)
    public AjaxJsonEntity checkCWExportTask(RptSearchCondition rptSearchCondition) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try {

            User user = UserUtils.getUser();
            List<Long> productCategoryIds = Lists.newArrayList();
            productCategoryIds.add(5L);
            productCategoryIds.add(11L);
            productCategoryIds.add(12L);
            productCategoryIds.add(1L);

            Long kefuId = 0L;
            if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()) {
                List <Long> keFuProductCategoryIds =  systemService.getAuthorizedProductCategoryIds(user.getId());
                productCategoryIds = productCategoryIds.stream().filter(r -> keFuProductCategoryIds.contains(r.longValue())).collect(Collectors.toList());
            }

            if(user.isKefu()){
                if(user.isKefu()){
                    Map<Long, User> userMap = MSUserUtils.getMapByUserType(2);
                    kefuId = user.getId();
                    User kefu = userMap.get(kefuId);
                    if(kefu != null){
                        if(kefu.getSubFlag() == 1){
                            result.setSuccess(false);
                            return result;
                        }
                    }
                }
            }
            Integer type = 0;
            if (rptSearchCondition.getAreaId() != null) {
                Area area = areaService.getFromCache(rptSearchCondition.getAreaId());
                type = area.getType();
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

            msKeFuAverageOrderFeeRptService.checkRptExportTask(productCategoryIds,kefuId, rptSearchCondition.getCustomerId(),type,rptSearchCondition.getAreaId(),rptSearchCondition.getSelectedYear(),rptSearchCondition.getSelectedMonth(),user);
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
    @RequiresPermissions("rpt:keFuAverageOrderFee:export")
    @RequestMapping(value = "cwExport", method = RequestMethod.POST)
    public AjaxJsonEntity cwExport(RptSearchCondition rptSearchCondition) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try {
            User user = UserUtils.getUser();
            Long kefuId = 0L;
            List<Long> productCategoryIds = Lists.newArrayList();
            productCategoryIds.add(5L);
            productCategoryIds.add(11L);
            productCategoryIds.add(12L);
            productCategoryIds.add(1L);
            if(user.isKefu()){
                kefuId = user.getId();
            }
            if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()) {
                List <Long> keFuProductCategoryIds =  systemService.getAuthorizedProductCategoryIds(user.getId());
                productCategoryIds = productCategoryIds.stream().filter(r -> keFuProductCategoryIds.contains(r.longValue())).collect(Collectors.toList());

            }
            if (rptSearchCondition.getProductCategory() != 0) {
                productCategoryIds = Lists.newArrayList();
                productCategoryIds.add(rptSearchCondition.getProductCategory().longValue());
            }
            Integer type = 0;
            if (rptSearchCondition.getAreaId() != null) {
                Area area = areaService.getFromCache(rptSearchCondition.getAreaId());
                type = area.getType();
            }
            if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()){
                if (productCategoryIds.isEmpty()) {
                    result.setSuccess(false);
                    result.setMessage("创建报表导出任务失败，请重试");
                    return result;
                }
            }

            msKeFuAverageOrderFeeRptService.createRptExportTask(productCategoryIds,kefuId, rptSearchCondition.getCustomerId(),type,rptSearchCondition.getAreaId(),rptSearchCondition.getSelectedYear(),rptSearchCondition.getSelectedMonth() ,user);
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
    @RequiresPermissions("rpt:keFuAverageOrderFee:export")
    @RequestMapping(value = "vipCheckExportTask", method = RequestMethod.POST)
    public AjaxJsonEntity vipCheckExportTask(RptSearchCondition rptSearchCondition) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try {

            User user = UserUtils.getUser();
            List<Long> productCategoryIds = Lists.newArrayList();
            Long kefuId = 0L;
            if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()) {
                productCategoryIds =  systemService.getAuthorizedProductCategoryIds(user.getId());
            }
            if(user.isKefu()){
                if(user.isKefu()){
                    kefuId = user.getId();
                    Map<Long, User> userMap = MSUserUtils.getMapByUserType(2);
                    User kefu = userMap.get(kefuId);
                    if(kefu != null){
                        if(kefu.getSubFlag() != 1){
                            result.setSuccess(false);
                            return result;
                        }
                    }

                }
            }
            Integer type = 0;
            if (rptSearchCondition.getAreaId() != null) {
                Area area = areaService.getFromCache(rptSearchCondition.getAreaId());
                type = area.getType();
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
            msKeFuAverageOrderFeeRptService.vipCheckRptExportTask(productCategoryIds,kefuId, rptSearchCondition.getCustomerId(),type,rptSearchCondition.getAreaId(),rptSearchCondition.getSelectedYear(),rptSearchCondition.getSelectedMonth(),user);
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
    @RequiresPermissions("rpt:keFuAverageOrderFee:export")
    @RequestMapping(value = "vipExport", method = RequestMethod.POST)
    public AjaxJsonEntity vipExport(RptSearchCondition rptSearchCondition) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try {
            User user = UserUtils.getUser();
            Long kefuId = 0L;
            List<Long> productCategoryIds = Lists.newArrayList();
            if(user.isKefu()){
                kefuId = user.getId();
            }
            if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()) {
                productCategoryIds =  systemService.getAuthorizedProductCategoryIds(user.getId());
            }
            if (rptSearchCondition.getProductCategory() != 0) {
                productCategoryIds = Lists.newArrayList();
                productCategoryIds.add(rptSearchCondition.getProductCategory().longValue());
            }
            Integer type = 0;
            if (rptSearchCondition.getAreaId() != null) {
                Area area = areaService.getFromCache(rptSearchCondition.getAreaId());
                type = area.getType();
            }
            if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()){
                if (productCategoryIds.isEmpty()) {
                    result.setSuccess(false);
                    result.setMessage("创建报表导出任务失败，请重试");
                    return result;
                }
            }
            msKeFuAverageOrderFeeRptService.vipCreateRptExportTask(productCategoryIds,kefuId, rptSearchCondition.getCustomerId(),type,rptSearchCondition.getAreaId(),rptSearchCondition.getSelectedYear(),rptSearchCondition.getSelectedMonth() ,user);
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
