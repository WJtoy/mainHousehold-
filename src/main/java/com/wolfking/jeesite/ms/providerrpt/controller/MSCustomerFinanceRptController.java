package com.wolfking.jeesite.ms.providerrpt.controller;

import com.kkl.kklplus.entity.md.GlobalMappingSalesSubFlagEnum;
import com.kkl.kklplus.entity.rpt.RPTCustomerFinanceEntity;
import com.kkl.kklplus.entity.rpt.exception.RPTBaseException;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.rpt.entity.RptSearchCondition;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providerrpt.service.MSCustomerFinanceRptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping(value = "${adminPath}/rpt/provider/customerFinance/")
public class MSCustomerFinanceRptController {
    @Autowired
    private MSCustomerFinanceRptService msCustomerFinanceRptService;

    @ModelAttribute("rptSearchCondition")
    public RptSearchCondition get(@ModelAttribute("rptSearchCondition") RptSearchCondition rptSearchCondition) {
        if (rptSearchCondition == null) {
            rptSearchCondition = new RptSearchCondition();
        }
        return rptSearchCondition;
    }
    /**
     * 客户账户余额报表
     */
  //  @RequiresPermissions("rpt:customerFinance:view")
    @RequestMapping(value = "customerFinanceReport")
    public String customerFinanceReport(RptSearchCondition rptSearchCondition, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<RPTCustomerFinanceEntity> page = new Page<>(request, response);
        User user = UserUtils.getUser();
        Long customerId = rptSearchCondition.getCustomerId();
        Long salesId = null;
        Long merchandiserId = null;
        Integer subFlag = null;
        if (rptSearchCondition.isSearching()) {
            if (user.isCustomer()) {
                customerId = UserUtils.getUser().getCustomerAccountProfile().getCustomer().getId();
            } else if (user.isSaleman()) {
                salesId = user.getId();
                if (user.getSubFlag() == GlobalMappingSalesSubFlagEnum.MANAGER.getValue()){
                    subFlag = GlobalMappingSalesSubFlagEnum.MANAGER.getValue();
                }
            }else if(user.isMerchandiser()){
                merchandiserId = user.getId();
            }
            Integer paymentType = (rptSearchCondition.getPaymentType() == null || rptSearchCondition.getPaymentType().equals("")) ?
                    null :
                    StringUtils.toInteger(rptSearchCondition.getPaymentType());
            page = msCustomerFinanceRptService.getCompletedOrderList(page, customerId, salesId, merchandiserId,paymentType, rptSearchCondition.getRemarks(),subFlag);

        }
        model.addAttribute("page", page);
        return "modules/providerrpt/customerFinanceReport";
    }

    @ResponseBody
    @RequestMapping(value = "checkExportTask", method = RequestMethod.POST)
    public AjaxJsonEntity checkExportTask(RptSearchCondition rptSearchCondition) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try {
            User user = UserUtils.getUser();
            Long customerId = rptSearchCondition.getCustomerId();
            Long salesId = null;
            Long merchandiserId = null;
            Integer subFlag = null;
            if (user.isCustomer()) {
                customerId = UserUtils.getUser().getCustomerAccountProfile().getCustomer().getId();
            } else if (user.isSaleman()) {
                salesId = user.getId();
                if (user.getSubFlag() == GlobalMappingSalesSubFlagEnum.MANAGER.getValue()){
                    subFlag = GlobalMappingSalesSubFlagEnum.MANAGER.getValue();
                }
            }else if(user.isMerchandiser()){
                merchandiserId = user.getId();
            }
            Integer paymentType = (rptSearchCondition.getPaymentType() == null || rptSearchCondition.getPaymentType().equals("")) ?
                    null :
                    StringUtils.toInteger(rptSearchCondition.getPaymentType());

            msCustomerFinanceRptService.checkRptExportTask(customerId, salesId, merchandiserId,paymentType, rptSearchCondition.getRemarks(), user,subFlag);

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
            Long customerId = rptSearchCondition.getCustomerId();
            Long salesId = null;
            Long merchandiserId = null;
            Integer subFlag = null;
            if (user.isCustomer()) {
                customerId = UserUtils.getUser().getCustomerAccountProfile().getCustomer().getId();
            } else if (user.isSaleman()) {
                salesId = user.getId();
                if (user.getSubFlag() == GlobalMappingSalesSubFlagEnum.MANAGER.getValue()){
                    subFlag = GlobalMappingSalesSubFlagEnum.MANAGER.getValue();
                }
            }else if(user.isMerchandiser()){
                merchandiserId = user.getId();
            }
            Integer paymentType = (rptSearchCondition.getPaymentType() == null || rptSearchCondition.getPaymentType().equals("")) ?
                    null :
                    StringUtils.toInteger(rptSearchCondition.getPaymentType());

            msCustomerFinanceRptService.createRptExportTask(customerId, salesId, merchandiserId,paymentType, rptSearchCondition.getRemarks(), user,subFlag);
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
