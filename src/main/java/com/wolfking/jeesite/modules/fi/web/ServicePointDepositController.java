package com.wolfking.jeesite.modules.fi.web;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.md.MDDepositLevel;
import com.kkl.kklplus.utils.SequenceIdUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.service.SequenceIdService;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.Exceptions;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.fi.entity.CustomerCurrency;
import com.wolfking.jeesite.modules.fi.entity.EngineerCurrencyDeposit;
import com.wolfking.jeesite.modules.fi.entity.FIEnums;
import com.wolfking.jeesite.modules.fi.entity.viewModel.EngineerDepositVM;
import com.wolfking.jeesite.modules.fi.service.EngineerCurrencyDepositService;
import com.wolfking.jeesite.modules.md.entity.Engineer;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.md.entity.ServicePointFinance;
import com.wolfking.jeesite.modules.md.service.ServicePointService;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providermd.feign.MSDepositLevelFeign;
import com.wolfking.jeesite.ms.providermd.service.MSDepositLevelService;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.ServerSocket;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 网点质保金流水
 *
 * @author Ryan
 * @date 2021-02-22 14:30
 */
@Slf4j
@Controller
@RequestMapping(value = "${adminPath}/fi/servicepoint/deposit")
public class ServicePointDepositController extends BaseController {
    private static final int DEFAULT_PAGE_SIZE = 12;

    @Autowired
    private ServicePointService servicePointService;

    @Autowired
    private MSDepositLevelService msDepositLevelService;

    @Autowired
    private EngineerCurrencyDepositService engineerCurrencyDepositService;

    @Autowired
    private SequenceIdService sequenceIdService;

    /**
     * 质保金明细清单
     * @param paramMap  其余查询条件，用于特殊判读和处理，跟在地址？后传递
     * @param depositEntity 查询条件实体
     * @return
     */
    @RequiresPermissions(value = "fi:servicepoint:deposit:currency")
    @RequestMapping(value = "currencyList")
    public String currencyList(@RequestParam Map<String, Object> paramMap,@ModelAttribute("depositEntity") EngineerDepositVM depositEntity, HttpServletRequest request, HttpServletResponse response, Model model) {
        String view = "modules/fi/deposit/currencyList";
        if (depositEntity.getStartDate() == null) {
            depositEntity.setStartDate(DateUtils.setDays(new Date(), 1));
        }

        if (depositEntity.getEndDate() == null) {
            depositEntity.setEndDate(DateUtils.addDays(DateUtils.addMonths(depositEntity.getStartDate(), 1), -1));
        }
        //-> 23:59:59
        depositEntity.setEndDate(DateUtils.getDateEnd(depositEntity.getEndDate()));
        Page<EngineerCurrencyDeposit> page = new Page<>(request, response, DEFAULT_PAGE_SIZE);
        Long servicePointId = null;//查询条件
        User user = UserUtils.getUser();
        if (user.isEngineer()) {
            Engineer engineer = servicePointService.getEngineer(user.getEngineerId());
            servicePointId = engineer.getServicePoint().getId();
            if(servicePointId == null || servicePointId <=0){
                addMessage(model,"错误：读取您账号所属网点信息失败，请重试，或重新登录！");
                model.addAttribute("page", page);
                return view;
            }
            depositEntity.setServicePointId(servicePointId);
        }
        //数据分片
        List<String> quarters = QuarterUtils.getQuarters(depositEntity.getStartDate(), depositEntity.getEndDate());
        depositEntity.setQuarters(quarters);
        List<MDDepositLevel> allLevels = msDepositLevelService.findAllLevelList();
        model.addAttribute("levels",allLevels);
        page = engineerCurrencyDepositService.findCurrencyList(page,depositEntity);
        if (page.getCount() > 0) {
            FIEnums.DepositPaymentTypeENum paymentTypeENum;
            FIEnums.DepositActionTypeENum actionTypeENum;
            List<Long> sids = page.getList().stream().map(t->t.getServicePoint().getId()).distinct().collect(Collectors.toList());
            Map<Long, ServicePoint> servicePointMap = loadServicePointInfos(sids);
            ServicePoint servicePoint;
            for (EngineerCurrencyDeposit currency : page.getList()) {
                servicePoint = servicePointMap.get(currency.getServicePoint().getId());
                currency.setServicePoint(servicePoint);
                if (currency.getActionType() == FIEnums.DepositActionTypeENum.ORDER_DEDUCTION.getValue()) {
                    currency.setActionTypeName(FIEnums.DepositActionTypeENum.ORDER_DEDUCTION.getName());
                } else if (currency.getActionType() == FIEnums.DepositActionTypeENum.OFFLINE_RECHARGE.getValue()) {
                    paymentTypeENum = FIEnums.DepositPaymentTypeENum.fromValue(currency.getPaymentType());
                    if(paymentTypeENum != null){
                        currency.setActionTypeName(paymentTypeENum.getName());
                    }else{
                        currency.setActionTypeName(FIEnums.DepositActionTypeENum.OFFLINE_RECHARGE.getName());
                    }
                }else{
                    actionTypeENum = FIEnums.DepositActionTypeENum.fromValue(currency.getActionType());
                    if(actionTypeENum != null){
                        currency.setActionTypeName(actionTypeENum.getName());
                    }else{
                        currency.setActionTypeName(StringUtils.EMPTY);
                    }
                }
            }
        }
        model.addAttribute("page", page);
        model.addAllAttributes(paramMap);
        return view;
    }

    /**
     * 单个网点质保金流水清单
     * @param paramMap  其余查询条件，用于特殊判读和处理，跟在地址？后传递
     * @param depositEntity 查询条件实体
     * @return
     */
    @RequiresPermissions(value = "fi:servicepoint:deposit:balance")
    @RequestMapping(value = "myCurrencyList")
    public String myCurrencyList(EngineerDepositVM depositEntity, HttpServletRequest request, HttpServletResponse response, Model model) {
        String view = "modules/fi/deposit/myCurrencyList";
        if (depositEntity.getStartDate() == null) {
            depositEntity.setStartDate(DateUtils.setDays(new Date(), 1));
        }
        if (depositEntity.getEndDate() == null) {
            depositEntity.setEndDate(DateUtils.addDays(DateUtils.addMonths(depositEntity.getStartDate(), 1), -1));
        }
        //-> 23:59:59
        depositEntity.setEndDate(DateUtils.getDateEnd(depositEntity.getEndDate()));
        Page<EngineerCurrencyDeposit> page = new Page<>(request, response, DEFAULT_PAGE_SIZE);
        long servicePointId = Optional.ofNullable(depositEntity).map(t->t.getServicePointId()).orElse(0l);
        if(servicePointId <=0){
            addMessage(model,"无网点参数，无法查看。");
            model.addAttribute("page", page);
            model.addAttribute("depositEntity", depositEntity);
            return view;
        }
        ServicePoint servicePoint = loadSearvicePointInfo(servicePointId);
        model.addAttribute("servicePoint",servicePoint);
        //数据分片
        List<String> quarters = QuarterUtils.getQuarters(depositEntity.getStartDate(), depositEntity.getEndDate());
        depositEntity.setQuarters(quarters);
        page = engineerCurrencyDepositService.findCurrencyList(page,depositEntity);
        if (page.getCount() > 0) {
            FIEnums.DepositPaymentTypeENum paymentTypeENum;
            FIEnums.DepositActionTypeENum actionTypeENum;
            for (EngineerCurrencyDeposit currency : page.getList()) {
                if (currency.getActionType() == FIEnums.DepositActionTypeENum.ORDER_DEDUCTION.getValue()) {
                    currency.setActionTypeName(FIEnums.DepositActionTypeENum.ORDER_DEDUCTION.getName());
                } else if (currency.getActionType() == FIEnums.DepositActionTypeENum.OFFLINE_RECHARGE.getValue()) {
                    paymentTypeENum = FIEnums.DepositPaymentTypeENum.fromValue(currency.getPaymentType());
                    if(paymentTypeENum != null){
                        currency.setActionTypeName(paymentTypeENum.getName());
                    }else{
                        currency.setActionTypeName(FIEnums.DepositActionTypeENum.OFFLINE_RECHARGE.getName());
                    }
                }else{
                    actionTypeENum = FIEnums.DepositActionTypeENum.fromValue(currency.getActionType());
                    if(actionTypeENum != null){
                        currency.setActionTypeName(actionTypeENum.getName());
                    }else{
                        currency.setActionTypeName(StringUtils.EMPTY);
                    }
                }
            }
        }
        model.addAttribute("page", page);
        model.addAttribute("depositEntity", depositEntity);
        return view;
    }

    /**
     * 读取网点基本信息
     * 额外读取质保金等级，质保金余额
     * @param id
     * @return
     */
    private ServicePoint loadSearvicePointInfo(long id){
        if(id <=0){
            return null;
        }
        ServicePoint servicePoint = servicePointService.getFromCache(id);
        if(servicePoint == null) {
            return servicePoint;
        }
        //质保金等级
        MDDepositLevel depositLevel = servicePoint.getMdDepositLevel();
        long levelid= Optional.ofNullable(depositLevel).map(t->t.getId()).orElse(0l);
        if(levelid <= 0){
            depositLevel = new MDDepositLevel();
            depositLevel.setId(null);
            depositLevel.setName("");
            servicePoint.setMdDepositLevel(depositLevel);
        }else{
            depositLevel = msDepositLevelService.getById(levelid);
        }
        servicePoint.setMdDepositLevel(depositLevel);
        //质保金余额
        Map<Long, ServicePointFinance> depositMap = servicePointService.getDepositByIds(Lists.newArrayList(id));
        ServicePointFinance finance = depositMap.get(id);
        if(servicePoint.getFinance() != null){
            servicePoint.getFinance().setDeposit(finance.getDeposit());
            servicePoint.getFinance().setDepositRecharge(finance.getDepositRecharge());
        }else{
            servicePoint.setFinance(finance);
        }
        return servicePoint;
    }

    /**
     * 批量读取网点信息
     * 额外读取质保金等级，质保金余额
     * @param ids
     * @return
     */
    private Map<Long,ServicePoint> loadServicePointInfos(List<Long> ids){
        if(CollectionUtils.isEmpty(ids)){
            return Maps.newHashMap();
        }
        Map<Long,ServicePoint> maps = Maps.newHashMapWithExpectedSize(ids.size());
        Map<Long, MDDepositLevel> allLevelMap = msDepositLevelService.getAllLevelMap();
        Map<Long, ServicePointFinance> depositMap = servicePointService.getDepositByIds(ids);
        for(Long id:ids) {
            ServicePoint servicePoint = servicePointService.getFromCache(id);
            if(servicePoint == null) {
                continue;
            }
            MDDepositLevel depositLevel = servicePoint.getMdDepositLevel();
            long levelid= Optional.ofNullable(depositLevel).map(t->t.getId()).orElse(0l);
            if(levelid <= 0){
                depositLevel = new MDDepositLevel();
                depositLevel.setId(null);
                depositLevel.setName("");
                servicePoint.setMdDepositLevel(depositLevel);
            }else{
                depositLevel = allLevelMap.get(levelid);
            }
            servicePoint.setMdDepositLevel(depositLevel);
            ServicePointFinance finance = depositMap.get(id);
            if(servicePoint.getFinance() != null){
                servicePoint.getFinance().setDeposit(finance.getDeposit());
                servicePoint.getFinance().setDepositRecharge(finance.getDepositRecharge());
            }else{
                servicePoint.setFinance(finance);
            }
            maps.put(id,servicePoint);

        }
        return maps;
    }

    /**
     * 质保金余额清单
     * @param depositEntity 查询条件实体
     * @return
     */
    @RequiresPermissions(value = "fi:servicepoint:deposit:balance")
    @RequestMapping(value = "balanceList")
    public String list(@ModelAttribute("depositEntity") EngineerDepositVM depositEntity, HttpServletRequest request, HttpServletResponse response, Model model) {
        String view = "modules/fi/deposit/balanceList";
        List<MDDepositLevel> levels = msDepositLevelService.findAllLevelList();
        if(levels == null){
            levels = Lists.newArrayList();
        }
        model.addAttribute("levels",levels);
        if (depositEntity.getStartDate() == null) {
            depositEntity.setStartDate(DateUtils.setDays(new Date(), 1));
        }

        if (depositEntity.getEndDate() == null) {
            depositEntity.setEndDate(DateUtils.addDays(DateUtils.addMonths(depositEntity.getStartDate(), 1), -1));
        }

        Page<ServicePoint> page = new Page<>(request, response, DEFAULT_PAGE_SIZE);;
        Long servicePointId = null;//查询条件
        User user = UserUtils.getUser();
        ServicePoint servicePoint = new ServicePoint();
        if (user.isEngineer()) {
            Engineer engineer = servicePointService.getEngineer(user.getEngineerId());
            servicePointId = engineer.getServicePoint().getId();
            if(servicePointId == null || servicePointId <=0){
                addMessage(model,"错误：读取您账号所属网点信息失败，请重试，或重新登录！");
                model.addAttribute("page", new Page<>());
                return view;
            }
            depositEntity.setServicePointId(servicePointId);
            servicePoint.setId(servicePointId);
        }else{
            servicePoint.setId(depositEntity.getServicePointId());
        }
        if(depositEntity.getDepositLevel()== -1) {
            servicePoint.setMdDepositLevel(null);
        }else{
            MDDepositLevel depositLevel = new MDDepositLevel();
            depositLevel.setId(depositEntity.getDepositLevel());
            servicePoint.setMdDepositLevel(depositLevel);
        }
        servicePoint.setServicePointNo(depositEntity.getServicePointNo());
        servicePoint.setContactInfo1(depositEntity.getContactInfo());
        //数据分片
        List<String> quarters = QuarterUtils.getQuarters(depositEntity.getStartDate(), depositEntity.getEndDate());
        depositEntity.setQuarters(quarters);

        page = servicePointService.findServicePointListForDeposit(page,servicePoint);
        model.addAttribute("page", page);
        return view;
    }


    //region 充值

    @RequestMapping(value = "chargeForm")
    public String chargeForm(@ModelAttribute("depositCurrency") EngineerCurrencyDeposit depositCurrency, Model model, HttpServletRequest request)
    {
        String view = "modules/fi/deposit/chargeForm";
        model.addAttribute("paymentTypes", FIEnums.DepositPaymentTypeENum.values());
        depositCurrency.setBalance(0.00);
        depositCurrency.setBeforeBalance(0.00);
        depositCurrency.setActionTypeName(StringUtils.EMPTY);
        depositCurrency.setCreateDate(new Date());

        long servicePointId = Optional.ofNullable(depositCurrency.getServicePoint()).map(t->t.getId()).orElse(0l);
        if(servicePointId >0){
            ServicePoint servicePoint = servicePointService.getFromCache(servicePointId);
            if(servicePoint == null){
                addMessage(model,"错误：读取网点信息失败。请重试!");
                model.addAttribute("depositCurrency", depositCurrency);
                return view;
            }
            Long depositLevelId = Optional.ofNullable(servicePoint.getMdDepositLevel()).map(t->t.getId()).orElse(null);
            if(depositLevelId == null){
                addMessage(model,"错误：读取网点质保信息失败。请重试!");
                model.addAttribute("depositCurrency", depositCurrency);
                return view;
            }
            if(depositLevelId <=0){
                addMessage(model, MessageFormat.format("错误：网点【{0}】当前未设定质保金等级，不能充值!" ,servicePoint.getName()));
                model.addAttribute("depositCurrency", depositCurrency);
                return view;
            }
            //读取质保金等级
            MDDepositLevel depositLevel = msDepositLevelService.getById(depositLevelId);
            if(depositLevel == null){
                addMessage(model, MessageFormat.format("错误：读取网点【{0}】当前质保金等级失败!" ,servicePoint.getName()));
                model.addAttribute("depositCurrency", depositCurrency);
                return view;
            }
            servicePoint.setMdDepositLevel(depositLevel);
            //应缴金额
            //depositCurrency.setBalance(depositLevel.getMaxAmount());
            depositCurrency.setBalance(servicePoint.getDeposit());
            //等级
            depositCurrency.setActionTypeName(depositLevel.getName());
            //已缴金额
            Map<Long, ServicePointFinance> financeMap = servicePointService.getDepositByIds(Lists.newArrayList(servicePointId));
            ServicePointFinance servicePointFinance = financeMap.get(servicePointId);
            if(servicePointFinance == null){
                addMessage(model, MessageFormat.format("错误：读取网点【{0}】质保金已缴金额失败，请重试!" ,servicePoint.getName()));
                model.addAttribute("depositCurrency", depositCurrency);
                return view;
            }
            depositCurrency.setBeforeBalance(servicePointFinance.getDeposit());
            depositCurrency.setServicePoint(servicePoint);
        }

        //model.addAttribute("depositdepositCurrency", depositCurrency);
        return view;
    }

    @ResponseBody
    @PostMapping(value = "chargeSubmit")
    public AjaxJsonEntity chargeSubmit(EngineerCurrencyDeposit depositCurrency, HttpServletRequest request,HttpServletResponse response){
        try{
            //check input
            if(depositCurrency == null){
                return AjaxJsonEntity.fail("提交的表单无数据",null);
            }
            User user = UserUtils.getUser();
            long servicePointId = Optional.ofNullable(depositCurrency.getServicePoint()).map(t -> t.getId()).orElse(0l);
            if(servicePointId == 0){
                return AjaxJsonEntity.fail("请选择充值的网点",null);
            }else if(depositCurrency.getPaymentType() == null || depositCurrency.getPaymentType() <= 0){
                return AjaxJsonEntity.fail("请选择支付类型",null);
            }else if(depositCurrency.getAmount() == 0){
                return AjaxJsonEntity.fail("请输入充值金额",null);
            }else if(depositCurrency.getCreateDate() == null){
                return AjaxJsonEntity.fail("请输入充值时间",null);
            }
            depositCurrency.setCurrencyNo(StringUtils.trimToEmpty(depositCurrency.getCurrencyNo()));
            depositCurrency.setCreateBy(user);
            depositCurrency.setUpdateDate(new Date());
            String quarter = QuarterUtils.getSeasonQuarter(depositCurrency.getCreateDate());
            depositCurrency.setQuarter(quarter);
            depositCurrency.setId(sequenceIdService.nextId());
            engineerCurrencyDepositService.recharge(depositCurrency);
            return AjaxJsonEntity.success("充值成功!",null);
        }
        catch(Exception e){
            log.error("充值错误,form:{}",depositCurrency,e);
            return AjaxJsonEntity.fail(Exceptions.getRootCauseMessage(e),null);
        }
    }


    //endregion 充值

}
