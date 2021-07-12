package com.wolfking.jeesite.ms.providerrpt.controller;


import com.google.common.collect.Lists;
import com.kkl.kklplus.entity.rpt.ServicePointChargeRptEntity;
import com.kkl.kklplus.entity.rpt.exception.RPTBaseException;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.md.entity.Engineer;
import com.wolfking.jeesite.modules.md.entity.ProductCategory;
import com.wolfking.jeesite.modules.md.service.ServicePointService;
import com.wolfking.jeesite.modules.md.utils.ProductUtils;
import com.wolfking.jeesite.modules.rpt.entity.RptSearchCondition;
import com.wolfking.jeesite.modules.rpt.web.BaseRptController;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.SystemService;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providerrpt.service.MSServciePointWritePptService;
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
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@Controller
@RequestMapping("${adminPath}/rpt/provider/detailedReconciliation/")
public class MSServciePointWritePptController extends BaseRptController {

    @Autowired
    private MSServciePointWritePptService msServciePointWritePptService;

    @Autowired
    private ServicePointService servicePointService;

    @Autowired
    private SystemService systemService;


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
     * 网点对账明细
     *
     * @param rptSearchCondition
     * @return
     */

    @RequestMapping("getNetworkReconciliation")
    public String getNetworkReconciliation(RptSearchCondition rptSearchCondition, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<ServicePointChargeRptEntity> page = new Page<>(request, response);
        User user = UserUtils.getUser();
        List<Long> productCategoryIds = Lists.newArrayList();
        if (user.isEngineer()) {
            Engineer engineer = servicePointService.getEngineer(user.getEngineerId());
            rptSearchCondition.setServicePointId(engineer.getServicePoint().getId());
            rptSearchCondition.setServicePointName(engineer.getName());
        }
        List<ProductCategory> productCategoryList = ProductUtils.getProductCategoryRPTList();
        if (rptSearchCondition.isSearching()) {
            if (rptSearchCondition.getProductCategory() != 0) {
                productCategoryIds = Lists.newArrayList();
                productCategoryIds.add(rptSearchCondition.getProductCategory().longValue());
            }

            page = msServciePointWritePptService.getNrPointWriteOff(page, productCategoryIds, rptSearchCondition.getServicePointId(),
                        rptSearchCondition.getSelectedYear(), rptSearchCondition.getSelectedMonth());

        }

        model.addAttribute("page",page);
        model.addAttribute("productCategoryList", productCategoryList);
        return "modules/providerrpt/detailedReconciliationRpt";

    }

    @ResponseBody
    @RequestMapping(value = "checkExportTask", method = RequestMethod.POST)
    public AjaxJsonEntity checkExportTask(RptSearchCondition rptSearchCondition) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        Date date = new Date();
        int year =DateUtils.getYear(date);
        int month = DateUtils.getMonth(date);
        try {

            User user = UserUtils.getUser();
            if (user.isEngineer()) {
                Engineer engineer = servicePointService.getEngineer(user.getEngineerId());
                rptSearchCondition.setServicePointId(engineer.getServicePoint().getId());
                rptSearchCondition.setServicePointName(engineer.getName());
            }
            List<Long> productCategoryIds = Lists.newArrayList();
            if(rptSearchCondition.getSelectedYear() == year &&rptSearchCondition.getSelectedMonth()>month){
                result.setSuccess(false);
                result.setMessage("导出失败：报表没有数据，无需导出");
                return result;
            }
            if (rptSearchCondition.getProductCategory() != 0) {
                productCategoryIds = Lists.newArrayList();
                productCategoryIds.add(rptSearchCondition.getProductCategory().longValue());
            }
            msServciePointWritePptService.checkRptExportTask(rptSearchCondition.getServicePointId(),
                    productCategoryIds,rptSearchCondition.getSelectedYear(), rptSearchCondition.getSelectedMonth(), user);
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
            if (user.isEngineer()) {
                Engineer engineer = servicePointService.getEngineer(user.getEngineerId());
                rptSearchCondition.setServicePointId(engineer.getServicePoint().getId());
                rptSearchCondition.setServicePointName(engineer.getName());
            }
            rptSearchCondition.setBeginDate(DateUtils.getStartOfDay(rptSearchCondition.getBeginDate()));
            rptSearchCondition.setEndDate(DateUtils.getEndOfDay(rptSearchCondition.getEndDate()));
            List<Long> productCategoryIds = Lists.newArrayList();

            if (rptSearchCondition.getProductCategory() != 0) {
                productCategoryIds = Lists.newArrayList();
                productCategoryIds.add(rptSearchCondition.getProductCategory().longValue());
            }
            msServciePointWritePptService.createRptExportTask(rptSearchCondition.getServicePointName(),rptSearchCondition.getServicePointId(),
                    productCategoryIds,rptSearchCondition.getSelectedYear(), rptSearchCondition.getSelectedMonth(), user);
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
