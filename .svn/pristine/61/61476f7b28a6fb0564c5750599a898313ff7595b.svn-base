package com.wolfking.jeesite.modules.fi.web;

import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.fi.entity.EngineerCurrency;
import com.wolfking.jeesite.modules.fi.service.ServicePointCurrencyService;
import com.wolfking.jeesite.modules.md.entity.Engineer;
import com.wolfking.jeesite.modules.md.service.ServicePointService;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providermd.service.MSEngineerService;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.Map;

/**
 * 网点账号流水Controller
 * @author
 * @version
 */
@Controller
@RequestMapping(value = "${adminPath}/fi/servicepointcurrency")
public class ServicePointCurrencyController extends BaseController {

    @Autowired
    private ServicePointCurrencyService servicePointCurrencyService;
    @Autowired
    private ServicePointService servicePointService;

    @Autowired
    private MSEngineerService msEngineerService;


    @ModelAttribute("engineerCurrency")
    public EngineerCurrency get(@ModelAttribute("engineerCurrency") EngineerCurrency engineerCurrency) {
        if (engineerCurrency == null) {
            engineerCurrency = new EngineerCurrency();
        }
        Date now = new Date();
        if (engineerCurrency.getCreateDate() == null) {
            engineerCurrency.setCreateDate(DateUtils.getStartDayOfMonth(now));
        }
        if (engineerCurrency.getUpdateDate() == null) {
            engineerCurrency.setUpdateDate(DateUtils.getLastDayOfMonth(now));
        }
        return engineerCurrency;
    }

    @RequiresPermissions("fi:servicepointcurrency:view")
    @RequestMapping(value ={ "list", "" })
    public String list(EngineerCurrency engineerCurrency,
                       @RequestParam(name = "beginDate", required = false) Date beginDate,
                       @RequestParam(name = "endDate", required = false) Date endDate,
                       HttpServletRequest request, HttpServletResponse response,Model model) {

        engineerCurrency.setCreateDate(DateUtils.getStartOfDay(engineerCurrency.getCreateDate()));
        engineerCurrency.setUpdateDate(DateUtils.getEndOfDay(engineerCurrency.getUpdateDate()));
        Long servicePointId = null;
        User user = UserUtils.getUser();
        if (user.isEngineer()) {
            Engineer engineer = servicePointService.getEngineer(user.getEngineerId());
            servicePointId = engineer.getServicePoint().getId();
        }
        else {
            servicePointId = engineerCurrency.getServicePoint()==null?null:engineerCurrency.getServicePoint().getId();
        }
        Page<EngineerCurrency> page = new Page<>(request,response);
        page = servicePointCurrencyService.getServicePointCurrencyList(servicePointId,
                engineerCurrency.getActionType(), engineerCurrency.getCreateDate(), engineerCurrency.getUpdateDate(), engineerCurrency.getCurrencyNo(), page);
        //切换为微服务
        if (page.getCount() > 0){
            Map<String,Dict> paymentTypeMap = MSDictUtils.getDictMap("PaymentType");
            Map<String,Dict> actionTypeMap = MSDictUtils.getDictMap("ServicePointActionType");
            if (paymentTypeMap.size() > 0 && actionTypeMap.size() > 0) {
                for (EngineerCurrency currency : page.getList()) {
                    currency.getServicePoint().getFinance().setPaymentType(paymentTypeMap.get(currency.getServicePoint().getFinance().getPaymentType().getValue()));
                    if (currency.getActionType() != null && currency.getActionType().intValue() > 0) {
                        currency.setActionTypeName(actionTypeMap.get(currency.getActionType().toString()).getLabel());
                    }
                }
            }
        }
        model.addAttribute("page", page);
        model.addAttribute("beginDate", beginDate);
        model.addAttribute("endDate", endDate);
        return "modules/fi/servicePointCurrencyList";
    }

    @RequiresPermissions("fi:servicepointcurrency:view")
    @RequestMapping(value ={ "listVerSecond" })
    public String listVerSecond(EngineerCurrency engineerCurrency,
                       @RequestParam(name = "beginDate", required = false) Date beginDate,
                       @RequestParam(name = "endDate", required = false) Date endDate,
                       HttpServletRequest request, HttpServletResponse response,Model model) {

        engineerCurrency.setCreateDate(DateUtils.getStartOfDay(engineerCurrency.getCreateDate()));
        engineerCurrency.setUpdateDate(DateUtils.getEndOfDay(engineerCurrency.getUpdateDate()));
        Long servicePointId = null;
        User user = UserUtils.getUser();
        if (user.isEngineer()) {
            Engineer engineer = msEngineerService.getByIdFromCache(user.getEngineerId());
            servicePointId = engineer.getServicePoint().getId();
        }
        else {
            servicePointId = engineerCurrency.getServicePoint()==null?null:engineerCurrency.getServicePoint().getId();
        }
        Page<EngineerCurrency> page = new Page<>(request,response);
        page = servicePointCurrencyService.getServicePointCurrencyListVerSecond(servicePointId,
                engineerCurrency.getActionType(), engineerCurrency.getCreateDate(), engineerCurrency.getUpdateDate(), engineerCurrency.getCurrencyNo(), page);
        //切换为微服务
        if (page.getCount() > 0){
            Map<String,Dict> paymentTypeMap = MSDictUtils.getDictMap("PaymentType");
            Map<String,Dict> actionTypeMap = MSDictUtils.getDictMap("ServicePointActionType");
            if (paymentTypeMap.size() > 0 && actionTypeMap.size() > 0) {
                for (EngineerCurrency currency : page.getList()) {
                    currency.getServicePoint().getFinance().setPaymentType(paymentTypeMap.get(currency.getServicePoint().getFinance().getPaymentType().getValue()));
                    if (currency.getActionType() != null && currency.getActionType().intValue() > 0) {
                        currency.setActionTypeName(actionTypeMap.get(currency.getActionType().toString()).getLabel());
                    }
                }
            }
        }
        model.addAttribute("page", page);
        model.addAttribute("beginDate", beginDate);
        model.addAttribute("endDate", endDate);
        return "modules/fi/servicePointCurrencyList";
    }
}
