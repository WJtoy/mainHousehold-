package com.wolfking.jeesite.ms.providerrpt.controller;

import com.kkl.kklplus.entity.md.GlobalMappingSalesSubFlagEnum;
import com.kkl.kklplus.entity.rpt.RPTKeFuPraiseDetailsEntity;
import com.kkl.kklplus.entity.rpt.exception.RPTBaseException;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.rpt.entity.RptSearchCondition;
import com.wolfking.jeesite.modules.rpt.web.BaseRptController;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providerrpt.service.MSCustomerPraiseDetailsRptService;
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
import java.util.Date;

@Controller
@RequestMapping(value = "${adminPath}/rpt/provider/customerPraiseDetails/")
public class MSCustomerPraiseDetailsRptController extends BaseRptController {

    @Autowired
    private MSCustomerPraiseDetailsRptService msCustomerPraiseDetailsRptService;

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
     * 客戶好评明细
     *
     * @param rptSearchCondition
     * @param model
     * @return
     */
    @RequestMapping(value = "customerPraiseDetailsRptData")
    public String keFuPraiseDetailsRptData(RptSearchCondition rptSearchCondition, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<RPTKeFuPraiseDetailsEntity> page = new Page<>(request, response);
        rptSearchCondition.setBeginDate(DateUtils.getStartOfDay(rptSearchCondition.getBeginDate()));
        rptSearchCondition.setEndDate(DateUtils.getEndOfDay(rptSearchCondition.getEndDate()));
        User user = UserUtils.getUser();
        Long salesId;
        Long customerId;
        Integer subFlag =null;
        if (rptSearchCondition.isSearching()) {
            if (user.isSaleman()) {
                salesId = user.getId();
                rptSearchCondition.setSalesId(salesId);
                if (user.getSubFlag()== GlobalMappingSalesSubFlagEnum.MANAGER.getValue()){
                    subFlag = GlobalMappingSalesSubFlagEnum.MANAGER.getValue();
                }
            }else if(user.isCustomer()){
                customerId = UserUtils.getUser().getCustomerAccountProfile().getCustomer().getId();
                rptSearchCondition.setCustomerId(customerId);
            }
            page = msCustomerPraiseDetailsRptService.getPraiseOrderList(page,rptSearchCondition.getStatus(),rptSearchCondition.getCustomerId(),rptSearchCondition.getSalesId(), rptSearchCondition.getBeginDate(), rptSearchCondition.getEndDate(),subFlag);

        }

        model.addAttribute("page", page);
        model.addAttribute("praiseStatusEnumList", MSRptUtils.getAllPraiseTypeList());
        return "modules/providerrpt/customerPraiseDetailsRpt";
    }

    @ResponseBody
    @RequestMapping(value = "checkExportTask", method = RequestMethod.POST)
    public AjaxJsonEntity checkExportTask(RptSearchCondition rptSearchCondition) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try {
            User user = UserUtils.getUser();
            rptSearchCondition.setBeginDate(DateUtils.getStartOfDay(rptSearchCondition.getBeginDate()));
            rptSearchCondition.setEndDate(DateUtils.getEndOfDay(rptSearchCondition.getEndDate()));
            Long salesId;
            Long customerId;
            Integer subFlag =null;
            if (user.isSaleman()) {
                salesId = user.getId();
                rptSearchCondition.setSalesId(salesId);
                if (user.getSubFlag()== GlobalMappingSalesSubFlagEnum.MANAGER.getValue()){
                    subFlag = GlobalMappingSalesSubFlagEnum.MANAGER.getValue();
                }
            }else if(user.isCustomer()){
                customerId = UserUtils.getUser().getCustomerAccountProfile().getCustomer().getId();
                rptSearchCondition.setCustomerId(customerId);
            }

            msCustomerPraiseDetailsRptService.checkRptExportTask(rptSearchCondition.getStatus(),rptSearchCondition.getCustomerId(), rptSearchCondition.getSalesId(), rptSearchCondition.getBeginDate(), rptSearchCondition.getEndDate(),user,subFlag);

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
            Long salesId;
            Long customerId;
            Integer subFlag =null;
            if (user.isSaleman()) {
                salesId = user.getId();
                rptSearchCondition.setSalesId(salesId);
                if (user.getSubFlag()== GlobalMappingSalesSubFlagEnum.MANAGER.getValue()){
                    subFlag = GlobalMappingSalesSubFlagEnum.MANAGER.getValue();
                }
            }else if(user.isCustomer()){
                customerId = UserUtils.getUser().getCustomerAccountProfile().getCustomer().getId();
                rptSearchCondition.setCustomerId(customerId);
            }

            msCustomerPraiseDetailsRptService.createRptExportTask(rptSearchCondition.getStatus(),rptSearchCondition.getCustomerId(),rptSearchCondition.getSalesId(), rptSearchCondition.getBeginDate(), rptSearchCondition.getEndDate(),user,subFlag);
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
