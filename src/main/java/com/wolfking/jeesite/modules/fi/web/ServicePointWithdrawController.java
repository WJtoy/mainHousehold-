package com.wolfking.jeesite.modules.fi.web;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kkl.kklplus.entity.fi.servicepoint.ServicePointDeducted;
import com.kkl.kklplus.entity.md.MDServicePointViewModel;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.CurrencyUtil;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.fi.entity.ServicePointPayCondition;
import com.wolfking.jeesite.modules.fi.entity.ServicePointPayableMonthly;
import com.wolfking.jeesite.modules.fi.entity.ServicePointWithdraw;
import com.wolfking.jeesite.modules.fi.service.ServicePointDeductedService;
import com.wolfking.jeesite.modules.fi.service.ServicePointWithdrawService;
import com.wolfking.jeesite.modules.md.entity.Engineer;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.md.entity.ServicePointFinance;
import com.wolfking.jeesite.modules.md.service.ServicePointFinanceService;
import com.wolfking.jeesite.modules.md.service.ServicePointService;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providermd.service.MSServicePointService;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Jeff on 2017/6/15.
 */
@Slf4j
@Controller
@RequestMapping(value = "${adminPath}/fi/servicepointwithdraw")
public class ServicePointWithdrawController extends BaseController {

    public static final int EXECL_CELL_WIDTH_15 = 15;
    public static final int EXECL_CELL_WIDTH_8 = 8;

    @Autowired
    private ServicePointService servicePointService;
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private ServicePointWithdrawService servicePointWithdrawService;
    @Autowired
    private ServicePointDeductedService servicePointDeductedService;
    @Autowired
    private MSServicePointService msServicePointService;
    @Autowired
    private ServicePointFinanceService servicePointFinanceService;

    @RequestMapping(value = {"list", ""}, method = RequestMethod.GET)
    public String listGet(@RequestParam Map<String, Object> paramMap, HttpServletRequest request, HttpServletResponse response, Model model) {
        paramMap.put("payBeginDate", DateUtils.getDate("yyyy-MM-01"));
        paramMap.put("payEndDate", DateUtils.getDate());
        model.addAllAttributes(paramMap);
        return "modules/fi/servicePointWithdrawList";
    }

    @RequestMapping(value = {"list", ""}, method = RequestMethod.POST)
    public String listPost(@RequestParam Map<String, Object> paramMap, HttpServletRequest request, HttpServletResponse response, Model model) {
        ServicePointFinance servicePointFinance = new ServicePointFinance();
        ServicePoint servicePoint = new ServicePoint();
        ServicePointWithdraw servicePointWithdraw = new ServicePointWithdraw();
        if (paramMap.containsKey("payment") && paramMap.get("payment").toString().trim().length() > 0) {
            servicePointWithdraw.setPaymentType(Integer.parseInt(paramMap.get("payment").toString()));
        }
        if (paramMap.containsKey("bank") && paramMap.get("bank").toString().trim().length() > 0) {
            servicePointWithdraw.setBank(Integer.parseInt(paramMap.get("bank").toString()));
        }
        if (paramMap.containsKey("servicePointId") && paramMap.get("servicePointId").toString().trim().length() > 0) {
            servicePoint.setId(Long.parseLong(paramMap.get("servicePointId").toString()));
        }
        if (paramMap.containsKey("payBeginDate") && paramMap.get("payBeginDate").toString().trim().length() > 0) {
            servicePointWithdraw.setPayBeginDate(DateUtils.getDateStart(DateUtils.parseDate(paramMap.get("payBeginDate").toString())));
        }
        if (paramMap.containsKey("payEndDate") && paramMap.get("payEndDate").toString().trim().length() > 0) {
            servicePointWithdraw.setPayEndDate(DateUtils.getDateEnd(DateUtils.parseDate(paramMap.get("payEndDate").toString())));
        }
        if (paramMap.containsKey("withdrawStatus") && paramMap.get("withdrawStatus").toString().trim().length() > 0) {
            servicePointWithdraw.setStatus(Integer.parseInt(paramMap.get("withdrawStatus").toString()));
        }
        if (paramMap.containsKey("engineerInvoiceFlag") && paramMap.get("engineerInvoiceFlag").toString().trim().length() > 0) {
            servicePointFinance.setInvoiceFlag(Integer.parseInt(paramMap.get("engineerInvoiceFlag").toString()));
        } else {
            servicePointFinance.setInvoiceFlag(null);
        }
        if (paramMap.containsKey("engineerDiscountFlag") && paramMap.get("engineerDiscountFlag").toString().trim().length() > 0) {
            servicePointFinance.setDiscountFlag(Integer.parseInt(paramMap.get("engineerDiscountFlag").toString()));
        } else {
            servicePointFinance.setDiscountFlag(null);
        }
        //正在处理中与失败查询操作时间
        if (servicePointWithdraw.getStatus() != null && (servicePointWithdraw.getStatus() == 20 || servicePointWithdraw.getStatus() == 30)) {
            if (servicePointWithdraw.getPayBeginDate() != null) {
                servicePointWithdraw.setCreateBeginDate(servicePointWithdraw.getPayBeginDate());
                servicePointWithdraw.setPayBeginDate(null);
            }
            if (servicePointWithdraw.getPayEndDate() != null) {
                servicePointWithdraw.setCreateEndDate(servicePointWithdraw.getPayEndDate());
                servicePointWithdraw.setPayEndDate(null);
            }
        }
        servicePoint.setFinance(servicePointFinance);
        servicePointWithdraw.setServicePoint(servicePoint);
        Page<ServicePointWithdraw> page = servicePointWithdrawService.getInvoiceConfirmDetailPage(new Page<>(request, response), servicePointWithdraw);
        //切换为微服务
        if (page.getCount() > 0) {
            Map<String, Dict> paymentTypeMap = MSDictUtils.getDictMap("PaymentType");
            Map<String, Dict> bankTypeMap = MSDictUtils.getDictMap("banktype");
            Map<String, Dict> statusMap = MSDictUtils.getDictMap("ServicePointWithdrawStatus");
            Map<String, Dict> bankIssueTypeMap = MSDictUtils.getDictMap("BankIssueType");
            for (ServicePointWithdraw withdraw : page.getList()) {
                if (withdraw.getPaymentType() != null && withdraw.getPaymentType() > 0) {
                    Dict paymentDict = paymentTypeMap.get(withdraw.getPaymentType().toString());
                    if (paymentDict != null) {
                        withdraw.setPaymentTypeName(paymentTypeMap.get(withdraw.getPaymentType().toString()).getLabel());
                    }
                }
                if (withdraw.getBank() != null) {
                    Dict bankDict = bankTypeMap.get(withdraw.getBank().toString());
                    if (bankDict != null) {
                        withdraw.setBankName(bankDict.getLabel());
                    }
                }
                if (withdraw.getStatus() != null && withdraw.getStatus() > 0) {
                    Dict statusDict = statusMap.get(withdraw.getStatus().toString());
                    if (statusDict != null) {
                        withdraw.setStatusName(statusDict.getLabel());
                    }
                }
                if (withdraw.getServicePoint() != null && withdraw.getServicePoint().getFinance() != null &&
                        withdraw.getServicePoint().getFinance().getBankIssue() != null &&
                        StringUtils.toInteger(withdraw.getServicePoint().getFinance().getBankIssue().getValue()) > 0) {
                    Dict bankIssueTypeDict = bankIssueTypeMap.get(withdraw.getServicePoint().getFinance().getBankIssue().getValue());
                    withdraw.getServicePoint().getFinance().getBankIssue().setLabel(bankIssueTypeDict != null ? bankIssueTypeDict.getLabel() : "");
                }
            }
        }
        model.addAttribute("page", page);
        model.addAllAttributes(paramMap);
        return "modules/fi/servicePointWithdrawList";
    }


    // 去servicePoint  // add on 2019-10-9
    @RequestMapping(value = {"listNew"}, method = RequestMethod.GET)
    public String listGetNew(@RequestParam Map<String, Object> paramMap, HttpServletRequest request, HttpServletResponse response, Model model) {
        paramMap.put("payBeginDate", DateUtils.getDate("yyyy-MM-01"));
        paramMap.put("payEndDate", DateUtils.getDate());
        model.addAllAttributes(paramMap);
        return "modules/fi/servicePointWithdrawListNew";
    }

    // 去servicepoint // add on 2019-10-9
    @RequestMapping(value = {"listNew"}, method = RequestMethod.POST)
    public String listPostNew(@RequestParam Map<String, Object> paramMap, HttpServletRequest request, HttpServletResponse response, Model model) {
        ServicePointFinance servicePointFinance = new ServicePointFinance();
        ServicePoint servicePoint = new ServicePoint();
        ServicePointWithdraw servicePointWithdraw = new ServicePointWithdraw();
        if (paramMap.containsKey("payment") && paramMap.get("payment").toString().trim().length() > 0) {
            servicePointWithdraw.setPaymentType(Integer.parseInt(paramMap.get("payment").toString()));
        }
        if (paramMap.containsKey("bank") && paramMap.get("bank").toString().trim().length() > 0) {
            servicePointWithdraw.setBank(Integer.parseInt(paramMap.get("bank").toString()));
        }
        if (paramMap.containsKey("servicePointId") && paramMap.get("servicePointId").toString().trim().length() > 0) {
            servicePoint.setId(Long.parseLong(paramMap.get("servicePointId").toString()));
        }
        if (paramMap.containsKey("payBeginDate") && paramMap.get("payBeginDate").toString().trim().length() > 0) {
            servicePointWithdraw.setPayBeginDate(DateUtils.getDateStart(DateUtils.parseDate(paramMap.get("payBeginDate").toString())));
        }
        if (paramMap.containsKey("payEndDate") && paramMap.get("payEndDate").toString().trim().length() > 0) {
            servicePointWithdraw.setPayEndDate(DateUtils.getDateEnd(DateUtils.parseDate(paramMap.get("payEndDate").toString())));
        }
        if (paramMap.containsKey("withdrawStatus") && paramMap.get("withdrawStatus").toString().trim().length() > 0) {
            servicePointWithdraw.setStatus(Integer.parseInt(paramMap.get("withdrawStatus").toString()));
        }
        if (paramMap.containsKey("engineerInvoiceFlag") && paramMap.get("engineerInvoiceFlag").toString().trim().length() > 0) {
            servicePointFinance.setInvoiceFlag(Integer.parseInt(paramMap.get("engineerInvoiceFlag").toString()));
        } else {
            servicePointFinance.setInvoiceFlag(null);
        }
        if (paramMap.containsKey("engineerDiscountFlag") && paramMap.get("engineerDiscountFlag").toString().trim().length() > 0) {
            servicePointFinance.setDiscountFlag(Integer.parseInt(paramMap.get("engineerDiscountFlag").toString()));
        } else {
            servicePointFinance.setDiscountFlag(null);
        }
        //正在处理中与失败查询操作时间
        if (servicePointWithdraw.getStatus() != null && (servicePointWithdraw.getStatus() == 20 || servicePointWithdraw.getStatus() == 30)) {
            if (servicePointWithdraw.getPayBeginDate() != null) {
                servicePointWithdraw.setCreateBeginDate(servicePointWithdraw.getPayBeginDate());
                servicePointWithdraw.setPayBeginDate(null);
            }
            if (servicePointWithdraw.getPayEndDate() != null) {
                servicePointWithdraw.setCreateEndDate(servicePointWithdraw.getPayEndDate());
                servicePointWithdraw.setPayEndDate(null);
            }
        }
        servicePoint.setFinance(servicePointFinance);
        servicePointWithdraw.setServicePoint(servicePoint);
        Page<ServicePointWithdraw> page = servicePointWithdrawService.getInvoiceConfirmDetailPageNew(new Page<>(request, response), servicePointWithdraw);
        //切换为微服务
        if (page.getCount() > 0) {
            Map<String, Dict> paymentTypeMap = MSDictUtils.getDictMap("PaymentType");
            Map<String, Dict> bankTypeMap = MSDictUtils.getDictMap("banktype");
            Map<String, Dict> statusMap = MSDictUtils.getDictMap("ServicePointWithdrawStatus");
            Map<String, Dict> bankIssueTypeMap = MSDictUtils.getDictMap("BankIssueType");
            for (ServicePointWithdraw withdraw : page.getList()) {
                if (withdraw.getPaymentType() != null && withdraw.getPaymentType() > 0) {
                    Dict paymentDict = paymentTypeMap.get(withdraw.getPaymentType().toString());
                    if (paymentDict != null) {
                        withdraw.setPaymentTypeName(paymentTypeMap.get(withdraw.getPaymentType().toString()).getLabel());
                    }
                }
                if (withdraw.getBank() != null) {
                    Dict bankDict = bankTypeMap.get(withdraw.getBank().toString());
                    if (bankDict != null) {
                        withdraw.setBankName(bankDict.getLabel());
                    }
                }
                if (withdraw.getStatus() != null && withdraw.getStatus() > 0) {
                    Dict statusDict = statusMap.get(withdraw.getStatus().toString());
                    if (statusDict != null) {
                        withdraw.setStatusName(statusDict.getLabel());
                    }
                }
                if (withdraw.getServicePoint() != null && withdraw.getServicePoint().getFinance() != null &&
                        withdraw.getServicePoint().getFinance().getBankIssue() != null &&
                        StringUtils.toInteger(withdraw.getServicePoint().getFinance().getBankIssue().getValue()) > 0) {
                    Dict bankIssueTypeDict = bankIssueTypeMap.get(withdraw.getServicePoint().getFinance().getBankIssue().getValue());
                    withdraw.getServicePoint().getFinance().getBankIssue().setLabel(bankIssueTypeDict != null ? bankIssueTypeDict.getLabel() : "");
                }
            }
        }
        model.addAttribute("page", page);
        model.addAllAttributes(paramMap);
        return "modules/fi/servicePointWithdrawListNew";
    }

    //region 付款

    private String getNeedPaymentListCacheKey() {
        return String.format("SERVICEPOINT:NEED:PAY:PAYMENT:%d", UserUtils.getUser().getId());
    }

    private String getNeedPayConditionCacheKey() {
        return String.format("SERVICEPOINT:NEED:PAY:CONDITION:%d", UserUtils.getUser().getId());
    }

    private String getNeedPayBankCacheKey() {
        return String.format("SERVICEPOINT:NEED:PAY:BANK:%d", UserUtils.getUser().getId());
    }

    private String getNeedPayEngineerNameListCacheKey() {
        return String.format("SERVICEPOINT:NEED:PAY:ENGINEER:NAME:%d", UserUtils.getUser().getId());
    }

    private void initWithDrawPayCache() {
        if (redisUtils.exists(getNeedPaymentListCacheKey())) {
            redisUtils.remove(RedisConstant.RedisDBType.REDIS_TEMP_DB, getNeedPaymentListCacheKey());
        }
        if (redisUtils.exists(getNeedPayConditionCacheKey())) {
            redisUtils.remove(RedisConstant.RedisDBType.REDIS_TEMP_DB, getNeedPayConditionCacheKey());
        }

        //获取未付款列表
        //月结 -- 10
        //处理中的银行列表
        List<Integer> processMonthlyBankList = servicePointWithdrawService.getProcessBankList(10);
        List<ServicePointPayCondition> payableMonthlyList = servicePointService.getPayableMonthlyList(processMonthlyBankList);
        //即结 -- 20
        //处理中的银行列表
        List<Integer> processDailyBankList = servicePointWithdrawService.getProcessBankList(20);
        List<ServicePointPayCondition> payableDailyList = servicePointService.getPayableDailyList(processDailyBankList);
        List<Integer> needPaymentList = Lists.newArrayList();
        if (payableMonthlyList != null && payableMonthlyList.size() > 0) {
            needPaymentList.add(10);
        }
        if (payableDailyList != null && payableDailyList.size() > 0) {
            needPaymentList.add(20);
        }

        List<ServicePointPayCondition> needPayConditionList = Lists.newArrayList();
        needPayConditionList.addAll(payableMonthlyList);
        needPayConditionList.addAll(payableDailyList);

        redisUtils.set(RedisConstant.RedisDBType.REDIS_TEMP_DB, getNeedPaymentListCacheKey(), needPaymentList, 4 * 60 * 60);
        redisUtils.set(RedisConstant.RedisDBType.REDIS_TEMP_DB, getNeedPayConditionCacheKey(), needPayConditionList, 4 * 60 * 60);
    }

    private void initWithDrawPayCacheWithoutServicePoint() {
        if (redisUtils.exists(getNeedPaymentListCacheKey())) {
            redisUtils.remove(RedisConstant.RedisDBType.REDIS_TEMP_DB, getNeedPaymentListCacheKey());
        }
        if (redisUtils.exists(getNeedPayConditionCacheKey())) {
            redisUtils.remove(RedisConstant.RedisDBType.REDIS_TEMP_DB, getNeedPayConditionCacheKey());
        }

        //获取未付款列表
        //月结 -- 10
        //处理中的银行列表
        List<Integer> processMonthlyBankList = servicePointWithdrawService.getProcessBankList(10);
        List<ServicePointPayCondition> payableMonthlyList = servicePointService.getPayableMonthlyListWithoutServicePoint(processMonthlyBankList); //add on 2019-9-29
        //即结 -- 20
        //处理中的银行列表
        List<Integer> processDailyBankList = servicePointWithdrawService.getProcessBankList(20);
        List<ServicePointPayCondition> payableDailyList = servicePointService.getPayableDailyListWithoutServicePoint(processDailyBankList);  //add on 2019-9-29
        List<Integer> needPaymentList = Lists.newArrayList();
        if (payableMonthlyList != null && payableMonthlyList.size() > 0) {
            needPaymentList.add(10);
        }
        if (payableDailyList != null && payableDailyList.size() > 0) {
            needPaymentList.add(20);
        }

        List<ServicePointPayCondition> needPayConditionList = Lists.newArrayList();
        needPayConditionList.addAll(payableMonthlyList);
        needPayConditionList.addAll(payableDailyList);

        redisUtils.set(RedisConstant.RedisDBType.REDIS_TEMP_DB, getNeedPaymentListCacheKey(), needPaymentList, 4 * 60 * 60);
        redisUtils.set(RedisConstant.RedisDBType.REDIS_TEMP_DB, getNeedPayConditionCacheKey(), needPayConditionList, 4 * 60 * 60);
    }

    //切换为微服务
    private List<Dict> getNeedPaymentType() {
        List<Long> paymentValueList = redisUtils.getList(RedisConstant.RedisDBType.REDIS_TEMP_DB, getNeedPaymentListCacheKey(), Long[].class);
        List<Dict> returnList = Lists.newArrayList();
        if (paymentValueList != null && paymentValueList.size() > 0) {
            Map<String, Dict> paymentTypeMap = MSDictUtils.getDictMap("PaymentType");
            for (Long paymentValue : paymentValueList) {
                Dict paymentDict = paymentTypeMap.get(paymentValue.toString());
                if (paymentDict != null) {
                    returnList.add(paymentDict);
                } else {
                    returnList.add(new Dict(paymentValue.toString()));
                }
            }
        }
        return returnList;
    }

    private List<ServicePointPayCondition> getNeedPayCondition() {
        return redisUtils.getList(RedisConstant.RedisDBType.REDIS_TEMP_DB, getNeedPayConditionCacheKey(), ServicePointPayCondition[].class);
    }

    private void clearNeedPayBank() {

        if (redisUtils.exists(getNeedPayBankCacheKey())) {
            redisUtils.remove(RedisConstant.RedisDBType.REDIS_TEMP_DB, getNeedPayBankCacheKey());
        }
    }

    private void clearEngineerNameList() {
        if (redisUtils.exists(getNeedPayEngineerNameListCacheKey())) {
            redisUtils.remove(RedisConstant.RedisDBType.REDIS_TEMP_DB, getNeedPayEngineerNameListCacheKey());
        }
    }

    private List<HashMap> getNeedPayBank() {
        return redisUtils.getList(RedisConstant.RedisDBType.REDIS_TEMP_DB, getNeedPayBankCacheKey(), HashMap[].class);
    }

    private List<HashMap> getNeedPayEngineerName() {
        return redisUtils.getList(RedisConstant.RedisDBType.REDIS_TEMP_DB, getNeedPayEngineerNameListCacheKey(), HashMap[].class);
    }

    @RequestMapping(value = {"pay"}, method = RequestMethod.GET)
    public String payFormGet(@RequestParam Map<String, Object> paramMap, HttpServletRequest request, HttpServletResponse response, Model model) {
        initWithDrawPayCache();
        Date payableMonthDate = DateUtils.addMonth(new Date(), -1);
        paramMap.put("payableYear", DateUtils.getYear(payableMonthDate));
        paramMap.put("payableMonth", DateUtils.getMonth(payableMonthDate));
        paramMap.put("engineerStatus", null);
        paramMap.put("engineerInvoiceFlag", null);
        model.addAttribute("page", null);
        model.addAttribute("bankList", null);
        model.addAttribute("paymentTypeList", getNeedPaymentType());
        model.addAllAttributes(paramMap);
        return "modules/fi/servicePointPayForm";
    }

    @RequestMapping(value = {"payNew"}, method = RequestMethod.GET)
    public String payFormGetNew(@RequestParam Map<String, Object> paramMap, HttpServletRequest request, HttpServletResponse response, Model model) {
        initWithDrawPayCacheWithoutServicePoint();
        Date payableMonthDate = DateUtils.addMonth(new Date(), -1);
        paramMap.put("payableYear", DateUtils.getYear(payableMonthDate));
        paramMap.put("payableMonth", DateUtils.getMonth(payableMonthDate));
        paramMap.put("engineerStatus", null);
        paramMap.put("engineerInvoiceFlag", null);
        model.addAttribute("page", null);
        model.addAttribute("bankList", null);
        model.addAttribute("paymentTypeList", getNeedPaymentType());
        model.addAllAttributes(paramMap);
        return "modules/fi/servicePointPayFormNew";
    }

    @RequestMapping(value = {"pay"}, method = RequestMethod.POST)
    public String payFormPost(@RequestParam Map<String, Object> paramMap, HttpServletRequest request, HttpServletResponse response, Model model) {
        model.addAttribute("paymentTypeList", getNeedPaymentType());
        model.addAttribute("bankList", getNeedPayBank());
        model.addAttribute("engineerList", getNeedPayEngineerName());

        ServicePointFinance servicePointFinance = new ServicePointFinance();
        if (paramMap.containsKey("payment") && paramMap.get("payment").toString().trim().length() > 0) {
            servicePointFinance.setPaymentType(new Dict(paramMap.get("payment").toString()));
        }
        if (paramMap.containsKey("bank") && paramMap.get("bank").toString().trim().length() > 0) {
            servicePointFinance.setBank(new Dict(paramMap.get("bank").toString()));
        }
        if (paramMap.containsKey("engineer") && paramMap.get("engineer").toString().trim().length() > 0) {
            servicePointFinance.setId(Long.parseLong(paramMap.get("engineer").toString()));
        }
        if (paramMap.containsKey("engineerStatus") && paramMap.get("engineerStatus").toString().trim().length() > 0) {
            servicePointFinance.setBankIssue(new Dict(paramMap.get("engineerStatus").toString()));
        }
        if (paramMap.containsKey("engineerInvoiceFlag") && paramMap.get("engineerInvoiceFlag").toString().trim().length() > 0) {
            servicePointFinance.setInvoiceFlag(Integer.parseInt(paramMap.get("engineerInvoiceFlag").toString()));
        } else {
            servicePointFinance.setInvoiceFlag(null);
        }
        if (paramMap.containsKey("engineerDiscountFlag") && paramMap.get("engineerDiscountFlag").toString().trim().length() > 0) {
            servicePointFinance.setDiscountFlag(Integer.parseInt(paramMap.get("engineerDiscountFlag").toString()));
        } else {
            servicePointFinance.setDiscountFlag(null);
        }
        Long areaId = null;
        if (paramMap.containsKey("areaId") && paramMap.get("areaId").toString().trim().length() > 0) {
            areaId = Long.parseLong(paramMap.get("areaId").toString());
        }
        List<ServicePoint> list = null;
        List<ServicePointPayableMonthly> minusPayableList = null;
        List<ServicePointDeducted> deductedList = null;
        int payableYear = 0;
        int payableMonth = 0;
        double totalMinus = 0d;
        double totalDeductedAmount = 0d;
        double totalPlatformFee = 0d;
        //月结，设置年，月
        if (servicePointFinance.getPaymentType().getValue().equals("10")) {
            if (paramMap.containsKey("payableYear") && paramMap.get("payableYear").toString().trim().length() > 0) {
                payableYear = Integer.parseInt(paramMap.get("payableYear").toString());
                servicePointFinance.setPayableYear(payableYear);
            }
            if (paramMap.containsKey("payableMonth") && paramMap.get("payableMonth").toString().trim().length() > 0) {
                payableMonth = Integer.parseInt(paramMap.get("payableMonth").toString());
                servicePointFinance.setPayableMonth(payableMonth);
            }
            //处理中的银行列表
            List<Integer> processMonthlyBankList = servicePointWithdrawService.getProcessBankList(10);
            list = servicePointService.getPayableMonthlyDetailList(servicePointFinance, processMonthlyBankList, areaId);
            minusPayableList = servicePointService.getPayableMinusMonthlyList();
            deductedList = servicePointDeductedService.getDeductedAmountList(payableYear * 100 + payableMonth, null);
        } else if (servicePointFinance.getPaymentType().getValue().equals("20")) {
            //处理中的银行列表
            List<Integer> processDailyBankList = servicePointWithdrawService.getProcessBankList(20);
            list = servicePointService.getPayableDailyDetailList(servicePointFinance, processDailyBankList, areaId);
        }
        //切换为微服务
        if (list != null && list.size() > 0) {
            Map<String, Dict> paymentTypeMap = MSDictUtils.getDictMap("PaymentType");
            Map<String, Dict> bankTypeMap = MSDictUtils.getDictMap("banktype");
            for (int index = list.size() - 1; index >= 0; index--) {
                ServicePoint servicePoint = list.get(index);
                double minus = 0;
                double deductedAmount = 0;
                double platformFee = 0;
                if (servicePoint.getFinance() != null) {
                    if (servicePoint.getFinance().getPaymentType() != null) {
                        Dict paymentDict = paymentTypeMap.get(servicePoint.getFinance().getPaymentType().getValue());
                        if (paymentDict != null) {
                            servicePoint.getFinance().setPaymentType(paymentDict);
                        }
                    }
                    if (servicePoint.getFinance().getBank() != null) {
                        Dict bankDict = bankTypeMap.get(servicePoint.getFinance().getBank().getValue());
                        if (bankDict != null) {
                            servicePoint.getFinance().setBank(bankDict);
                        }
                    }
                }
                if (minusPayableList != null && minusPayableList.size() > 0){
                    for (int i = minusPayableList.size() -1; i >=0; i--){
                        ServicePointPayableMonthly payableMonthly = minusPayableList.get(i);
                        int payableYearMonth = payableYear * 100 + payableMonth;
                        int minusPayableYearMonth = payableMonthly.getYear() * 100 + payableMonthly.getMonth();
                        if (servicePoint.getId().equals(payableMonthly.getServicePoint().getId()) &&
                            minusPayableYearMonth <= payableYearMonth &&
                            minusPayableYearMonth > payableMonthly.getLastDeductionYearMonth()) {
                            servicePoint.getFinance().setMinusAmount(servicePoint.getFinance().getMinusAmount() + payableMonthly.getAmount());
                            minus += payableMonthly.getAmount();
                            minusPayableList.remove(payableMonthly);
                        }
                    }
                }
                if (deductedList != null && deductedList.size() > 0){
                    for (int i = deductedList.size() -1; i >=0; i--){
                        ServicePointDeducted servicePointDeducted = deductedList.get(i);
                        if (servicePoint.getId().equals(servicePointDeducted.getServicepointId())){
                            servicePoint.getFinance().setDeductedAmount(servicePoint.getFinance().getDeductedAmount() + servicePointDeducted.getAmount());
                            deductedAmount += servicePointDeducted.getAmount();
                            deductedList.remove(servicePointDeducted);
                        }
                    }
                }
                if (servicePoint.getFinance().getPayableAmount() +
                        servicePoint.getFinance().getMinusAmount() +
                        servicePoint.getFinance().getDeductedAmount() <= 0) {
                    list.remove(servicePoint);
                } else {
                    totalMinus += minus;
                    totalDeductedAmount += deductedAmount;
                    platformFee = (servicePoint.getFinance().getPayableAmount() +
                            servicePoint.getFinance().getMinusAmount() +
                            servicePoint.getFinance().getDeductedAmount()) * CurrencyUtil.platformFeeRate;
                    platformFee = 0 - CurrencyUtil.round2(platformFee);
                    servicePoint.getFinance().setPlatformFee(platformFee);
                    totalPlatformFee += platformFee;
                }
            }
        }
        model.addAttribute("list", list);
        model.addAttribute("totalMinus", totalMinus);
        model.addAttribute("totalDeductedAmount", totalDeductedAmount);
        model.addAttribute("totalPlatformFee", totalPlatformFee);
        model.addAllAttributes(paramMap);
        return "modules/fi/servicePointPayForm";
    }

    @RequestMapping(value = {"payNew"}, method = RequestMethod.POST)
    public String payFormPostNew(@RequestParam Map<String, Object> paramMap, HttpServletRequest request, HttpServletResponse response, Model model) {
        model.addAttribute("paymentTypeList", getNeedPaymentType());
        model.addAttribute("bankList", getNeedPayBank());
        model.addAttribute("engineerList", getNeedPayEngineerName());

        ServicePointFinance servicePointFinance = new ServicePointFinance();
        if (paramMap.containsKey("payment") && paramMap.get("payment").toString().trim().length() > 0) {
            servicePointFinance.setPaymentType(new Dict(paramMap.get("payment").toString()));
        }
        if (paramMap.containsKey("bank") && paramMap.get("bank").toString().trim().length() > 0) {
            servicePointFinance.setBank(new Dict(paramMap.get("bank").toString()));
        }
        if (paramMap.containsKey("engineer") && paramMap.get("engineer").toString().trim().length() > 0) {
            servicePointFinance.setId(Long.parseLong(paramMap.get("engineer").toString()));
        }
        if (paramMap.containsKey("engineerStatus") && paramMap.get("engineerStatus").toString().trim().length() > 0) {
            servicePointFinance.setBankIssue(new Dict(paramMap.get("engineerStatus").toString()));
        }
        if (paramMap.containsKey("engineerInvoiceFlag") && paramMap.get("engineerInvoiceFlag").toString().trim().length() > 0) {
            servicePointFinance.setInvoiceFlag(Integer.parseInt(paramMap.get("engineerInvoiceFlag").toString()));
        } else {
            servicePointFinance.setInvoiceFlag(null);
        }
        if (paramMap.containsKey("engineerDiscountFlag") && paramMap.get("engineerDiscountFlag").toString().trim().length() > 0) {
            servicePointFinance.setDiscountFlag(Integer.parseInt(paramMap.get("engineerDiscountFlag").toString()));
        } else {
            servicePointFinance.setDiscountFlag(null);
        }
        Long areaId = null;
        if (paramMap.containsKey("areaId") && paramMap.get("areaId").toString().trim().length() > 0) {
            areaId = Long.parseLong(paramMap.get("areaId").toString());
        }
        List<ServicePoint> list = null;
        List<ServicePointPayableMonthly> minusPayableList = null;
        List<ServicePointDeducted> deductedList = null;
        int payableYear = 0;
        int payableMonth = 0;
        double totalMinus = 0d;
        double totalDeductedAmount = 0d;
        double totalPlatformFee = 0d;
        //月结，设置年，月
        if (servicePointFinance.getPaymentType().getValue().equals("10")) {
            if (paramMap.containsKey("payableYear") && paramMap.get("payableYear").toString().trim().length() > 0) {
                payableYear = Integer.parseInt(paramMap.get("payableYear").toString());
                servicePointFinance.setPayableYear(payableYear);
            }
            if (paramMap.containsKey("payableMonth") && paramMap.get("payableMonth").toString().trim().length() > 0) {
                payableMonth = Integer.parseInt(paramMap.get("payableMonth").toString());
                servicePointFinance.setPayableMonth(payableMonth);
            }
            //处理中的银行列表
            List<Integer> processMonthlyBankList = servicePointWithdrawService.getProcessBankList(10);
            list = servicePointService.getPayableMonthlyDetailListWithoutServicePoint(servicePointFinance, processMonthlyBankList, areaId); //add on 2019-9-29
            minusPayableList = servicePointService.getPayableMinusMonthlyList();
            deductedList = servicePointDeductedService.getDeductedAmountList(payableYear * 100 + payableMonth, null);
        } else if (servicePointFinance.getPaymentType().getValue().equals("20")) {
            //处理中的银行列表
            List<Integer> processDailyBankList = servicePointWithdrawService.getProcessBankList(20);
            list = servicePointService.getPayableDailyDetailListWithoutServicePoint(servicePointFinance, processDailyBankList, areaId);  //add on 2019-9-29
        }
        //切换为微服务
        if (list != null && list.size() > 0) {
            Map<String, Dict> paymentTypeMap = MSDictUtils.getDictMap("PaymentType");
            Map<String, Dict> bankTypeMap = MSDictUtils.getDictMap("banktype");
            for (int index = list.size() - 1; index >= 0; index--) {
                ServicePoint servicePoint = list.get(index);
                double minus = 0;
                double deductedAmount = 0;
                double platformFee = 0;
                if (servicePoint.getFinance() != null) {
                    if (servicePoint.getFinance().getPaymentType() != null) {
                        Dict paymentDict = paymentTypeMap.get(servicePoint.getFinance().getPaymentType().getValue());
                        if (paymentDict != null) {
                            servicePoint.getFinance().setPaymentType(paymentDict);
                        }
                    }
                    if (servicePoint.getFinance().getBank() != null) {
                        Dict bankDict = bankTypeMap.get(servicePoint.getFinance().getBank().getValue());
                        if (bankDict != null) {
                            servicePoint.getFinance().setBank(bankDict);
                        }
                    }
                }
                if (minusPayableList != null && minusPayableList.size() > 0){
                    for (int i = minusPayableList.size() -1; i >=0; i--){
                        ServicePointPayableMonthly payableMonthly = minusPayableList.get(i);
                        int payableYearMonth = payableYear * 100 + payableMonth;
                        int minusPayableYearMonth = payableMonthly.getYear() * 100 + payableMonthly.getMonth();
                        if (servicePoint.getId().equals(payableMonthly.getServicePoint().getId()) &&
                                minusPayableYearMonth <= payableYearMonth &&
                                minusPayableYearMonth > payableMonthly.getLastDeductionYearMonth()) {
                            servicePoint.getFinance().setMinusAmount(servicePoint.getFinance().getMinusAmount() + payableMonthly.getAmount());
                            minus += payableMonthly.getAmount();
                            minusPayableList.remove(payableMonthly);
                        }
                    }
                }
                if (deductedList != null && deductedList.size() > 0){
                    for (int i = deductedList.size() -1; i >=0; i--){
                        ServicePointDeducted servicePointDeducted = deductedList.get(i);
                        if (servicePoint.getId().equals(servicePointDeducted.getServicepointId())){
                            servicePoint.getFinance().setDeductedAmount(servicePoint.getFinance().getDeductedAmount() + servicePointDeducted.getAmount());
                            deductedAmount += servicePointDeducted.getAmount();
                            deductedList.remove(servicePointDeducted);
                        }
                    }
                }
                if (servicePoint.getFinance().getPayableAmount() +
                        servicePoint.getFinance().getMinusAmount() +
                        servicePoint.getFinance().getDeductedAmount() <= 0) {
                    list.remove(servicePoint);
                } else {
                    totalMinus += minus;
                    totalDeductedAmount += deductedAmount;
                    platformFee = (servicePoint.getFinance().getPayableAmount() +
                            servicePoint.getFinance().getMinusAmount() +
                            servicePoint.getFinance().getDeductedAmount()) * CurrencyUtil.platformFeeRate;
                    platformFee = 0 - CurrencyUtil.round2(platformFee);
                    servicePoint.getFinance().setPlatformFee(platformFee);
                    totalPlatformFee += platformFee;
                }
            }
        }
        model.addAttribute("list", list);
        model.addAttribute("totalMinus", totalMinus);
        model.addAttribute("totalDeductedAmount", totalDeductedAmount);
        model.addAttribute("totalPlatformFee", totalPlatformFee);
        model.addAllAttributes(paramMap);
        return "modules/fi/servicePointPayFormNew";
    }

    @SuppressWarnings("unchecked")
    @ResponseBody
    @RequestMapping(value = {"getbanklist"})
    public AjaxJsonEntity getBankListByPaymenttype(@RequestParam String paymenttype, HttpServletRequest request, HttpServletResponse response) {
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity jsonEntity = new AjaxJsonEntity(true);
        try {
            clearNeedPayBank();
            clearEngineerNameList();
            List<Map<String, String>> bankList = new ArrayList<>();
            List<ServicePointPayCondition> needPayCondition = getNeedPayCondition();

            //切换为微服务
            Map<String, Dict> bankTypeMap = MSDictUtils.getDictMap("banktype");
            for (ServicePointPayCondition item : needPayCondition) {
                if (item.getBank() != null && item.getBank() > 0) {
                    Dict bankDict = bankTypeMap.get(item.getBank().toString());
                    if (bankDict != null && bankDict.getLabel() != null) {
                        item.setBankName(bankDict.getLabel());
                    } else {
                        item.setBankName("");
                    }
                }
            }

            for (ServicePointPayCondition servicePointPayCondition : needPayCondition) {
                if (servicePointPayCondition.getPaymentType().toString().equals(paymenttype)) {
                    Map<String, String> bankMap = new HashMap<>();
//                    bankMap.put("text", DictUtils.getDictLabel(servicePointPayCondition.getBank().toString(), "banktype", ""));
                    bankMap.put("text", servicePointPayCondition.getBankName() != null ? servicePointPayCondition.getBankName() : "");
                    bankMap.put("value", servicePointPayCondition.getBank().toString());
                    if (!bankList.contains(bankMap)) {
                        bankList.add(bankMap);
                    }
                }
            }
            redisUtils.set(RedisConstant.RedisDBType.REDIS_TEMP_DB, getNeedPayBankCacheKey(), bankList, 4 * 60 * 60);
            jsonEntity.setData(bankList);
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage(e.getMessage());
        }
        return jsonEntity;
    }

    @SuppressWarnings("unchecked")
    @ResponseBody
    @RequestMapping(value = {"getengineerlist"})
    public AjaxJsonEntity getEngineerListByBank(@RequestParam String bank, String paymenttype, HttpServletRequest request, HttpServletResponse response) {
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity jsonEntity = new AjaxJsonEntity(true);
        ServicePointPayCondition sPP = null;
        try {
            clearEngineerNameList();
            List<Map<String, String>> engineerNameList = new ArrayList<>();
            List<ServicePointPayCondition> needPayCondition = getNeedPayCondition();
            List<Long> servicePointIds = Lists.newArrayList();  //add on 2020-2-19
            for (ServicePointPayCondition servicePointPayCondition : needPayCondition) {
                sPP = servicePointPayCondition;
                if (servicePointPayCondition.getBank().toString().equals(bank) &&
                        servicePointPayCondition.getPaymentType().toString().equals(paymenttype)) {
                    //Map<String, String> engineerMap = new HashMap<>();
                    // mark on 2020-1-17 begin web端去md_servicepoint
                    //ServicePoint servicePoint = (ServicePoint) redisUtils.zRangeOneByScore(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_SERVICEPOINT_ALL,
                    //        servicePointPayCondition.getServicePointId(), servicePointPayCondition.getServicePointId(), ServicePoint.class);
                    // mark on 2020-1-17 end
                    //ServicePoint servicePoint = servicePointService.getFromCache(servicePointPayCondition.getServicePointId());
                    /*
                    // mark on 2020-2-19 begin
                    ServicePoint servicePoint = servicePointService.getSimpleFromCache(servicePointPayCondition.getServicePointId());
                    if (servicePoint != null) { // add on 2019-9-30
                        if (servicePoint.getPrimary() != null && servicePoint.getPrimary().getName() != null) {
                            engineerMap.put("text", servicePoint.getServicePointNo() + "," + servicePoint.getPrimary().getName());
                        } else {
                            engineerMap.put("text", servicePoint.getServicePointNo() + "," + servicePoint.getName());
                        }
                        engineerMap.put("value", servicePointPayCondition.getServicePointId().toString());
                        engineerNameList.add(engineerMap);
                    }
                    // mark on 2020-2-19 end
                    */
                    servicePointIds.add(servicePointPayCondition.getServicePointId());  //add on 2020-2-19
                }
            }

            // add on 2020-2-19 begin
            String[] fieldsArray = new String[]{"id","servicePointNo","name","primaryId"};
            List<MDServicePointViewModel> servicePointViewModelList = msServicePointService.findBatchByIdsByCondition(servicePointIds, Arrays.asList(fieldsArray), null);
            if (servicePointViewModelList != null && !servicePointViewModelList.isEmpty()) {
                List<Long> engineerIds = servicePointViewModelList.stream().map(t -> t.getPrimaryId()).distinct().collect(Collectors.toList());
                Map<Long, String> engineerNameMap = Maps.newHashMap();
                if (engineerIds != null && !engineerIds.isEmpty()) {
                    List<Engineer> engineerList = servicePointService.findAllEngineersName(engineerIds, Arrays.asList("id", "name"));
                    if (engineerList != null && !engineerList.isEmpty()) {
                        engineerNameMap = engineerList.stream().collect(Collectors.toMap(Engineer::getId, Engineer::getName));
                    }
                }

                Map<Long, String> finalEngineerNameMap = engineerNameMap;
                servicePointViewModelList.stream().forEach(servicePoint->{
                    Map<String, String> engineerMap = new HashMap<>();
                    if (servicePoint.getPrimaryId() != null) {
                        if (finalEngineerNameMap.get(servicePoint.getPrimaryId()) != null) {
                            engineerMap.put("text", servicePoint.getServicePointNo() + "," + finalEngineerNameMap.get(servicePoint.getPrimaryId()));
                        } else {
                            engineerMap.put("text", servicePoint.getServicePointNo() + "," + servicePoint.getName());
                        }
                    } else {
                        engineerMap.put("text", servicePoint.getServicePointNo() + "," + servicePoint.getName());
                    }
                    engineerMap.put("value", servicePoint.getId().toString());
                    engineerNameList.add(engineerMap);
                });

            }
            // add on 2020-2-19 end
            redisUtils.set(RedisConstant.RedisDBType.REDIS_TEMP_DB, getNeedPayEngineerNameListCacheKey(), engineerNameList, 4 * 60 * 60);
            jsonEntity.setData(engineerNameList);
        } catch (Exception e) {
            LogUtils.saveLog("财务付款", "FI:WithDraw:getEngineerListByBank", sPP == null ? "" : sPP.toString(), e, new User(1L));
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage(e.getMessage());
        }
        return jsonEntity;
    }

    /**
     * 结帐确认 form
     *
     * @return
     */
    @RequestMapping(value = "paysave", method = RequestMethod.GET)
    public String paySaveGet(String servicePointId, String servicePointNo, String servicePointName, String phone1, String phone2,
                             String paymentType, String bank, String branch, String bankNo,
                             String bankOwner, String bankOwnerIdNo, String bankOwnerPhone, String bankIssue,
                             String totalAmount, String debtsAmount, String debtsDesc, String invoiceFlag, String discountFlag,
                             String qYear, String qMonth, HttpServletRequest request, HttpServletResponse response, Model model) {
        model.addAttribute("servicePointId", servicePointId);
        String servicePointNoString = "", servicePointNameString = "", bankOwnerString = "", bankOwnerIdNoString = "", bankOwnerPhoneString = "", debtsDescString = "", branchString = "";
        try {
            servicePointNoString = URLDecoder.decode(servicePointNo, "UTF-8");
            servicePointNameString = URLDecoder.decode(servicePointName, "UTF-8");
            bankOwnerString = URLDecoder.decode(bankOwner, "UTF-8");
            bankOwnerIdNoString = URLDecoder.decode(bankOwnerIdNo, "UTF-8");
            bankOwnerPhoneString = URLDecoder.decode(bankOwnerPhone, "UTF-8");
            debtsDescString = URLDecoder.decode(debtsDesc, "UTF-8");
            branchString = URLDecoder.decode(branch, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error("结帐确认-参数转换失败 {}", e.getLocalizedMessage());
        }
        model.addAttribute("servicePointId", servicePointId);
        model.addAttribute("servicePointNo", servicePointNoString);
        model.addAttribute("servicePointName", servicePointNameString);
        model.addAttribute("phone1", phone1);
        model.addAttribute("phone2", phone2);
        model.addAttribute("paymentType", paymentType);
        model.addAttribute("bank", bank);
        model.addAttribute("branch", branchString);
        model.addAttribute("bankNo", bankNo);
        model.addAttribute("bankOwner", bankOwnerString);
        model.addAttribute("bankOwnerIdNo", bankOwnerIdNoString);
        model.addAttribute("bankOwnerPhone", bankOwnerPhoneString);
        model.addAttribute("currentDate", DateUtils.formatDate(new Date(), "yyyy-MM-dd"));
        model.addAttribute("hBankIssue", bankIssue);
        model.addAttribute("invoiceFlag", invoiceFlag);
        model.addAttribute("discountFlag", discountFlag);
        model.addAttribute("qYear", qYear);
        model.addAttribute("qMonth", qMonth);
        model.addAttribute("debtsDesc", debtsDesc);
        model.addAttribute("debtsAmount", debtsAmount);
        model.addAttribute("totalAmount", totalAmount);

        String minusInfo = "";
        double totalMinus = 0;
        double totalDeductedAmount = 0;
        //月结
        if (paymentType.equals("10")) {
            //计算待扣款
            List<ServicePointPayableMonthly> payableMonthlyList = servicePointService.getPayableMinusMonthlyListByServicePointId(Long.parseLong(servicePointId));
            int payableYearMonth = Integer.parseInt(qYear) * 100 + Integer.parseInt(qMonth);
            if (payableMonthlyList != null && payableMonthlyList.size() > 0) {
                StringBuilder sb = new StringBuilder();

                for (ServicePointPayableMonthly payableMonthly : payableMonthlyList) {
                    int minusPayableYearMonth = payableMonthly.getYear() * 100 + payableMonthly.getMonth();
                    if (minusPayableYearMonth <= payableYearMonth &&
                        minusPayableYearMonth > payableMonthly.getLastDeductionYearMonth()) {
                        sb.append(String.format("%d年%d月:%.2f元, ", payableMonthly.getYear(), payableMonthly.getMonth(), payableMonthly.getAmount()));
                        totalMinus += payableMonthly.getAmount();
                    }
                }
                if (totalMinus != 0) {
                    minusInfo = sb.toString().substring(0, sb.length() - 2);
                }
            }
            List<ServicePointDeducted> deductedList = servicePointDeductedService.getDeductedAmountList(payableYearMonth, Long.parseLong(servicePointId));
            if (deductedList != null && deductedList.size() > 0){
                for (ServicePointDeducted deducted : deductedList){
                    totalDeductedAmount += deducted.getAmount();
                }
            }
            //切换为微服务
            model.addAttribute("remarks", "快可立".
                            concat(qMonth.concat("月").
                            concat(MSDictUtils.getDictLabel(paymentType, "PaymentType", "")).
                            concat("师傅付款招行")).
                            concat(minusInfo.length()>0?",扣除待扣款:".concat(minusInfo):""));
        }
        //即结
        else if (paymentType.equals("20")) {
            //切换为微服务
            model.addAttribute("remarks", "快可立".
                            concat(DateUtils.getDate("M").
                            concat("月").
                            concat(MSDictUtils.getDictLabel(paymentType, "PaymentType", "")).
                            concat("师傅付款招行")));
        }

        model.addAttribute("totalMinus", totalMinus);
        model.addAttribute("totalDeductedAmount", totalDeductedAmount);
        model.addAttribute("minusInfo", minusInfo);
        model.addAttribute("platformFeeRate", CurrencyUtil.platformFeeRate);

        ServicePointWithdraw servicePointWithdraw = new ServicePointWithdraw();
        servicePointWithdraw.setServicePoint(new ServicePoint(Long.parseLong(servicePointId)));
        servicePointWithdraw.setBank(Integer.parseInt(bank));
        servicePointWithdraw.setPaymentType(Integer.parseInt(paymentType));
        servicePointWithdraw.setStatus(20);
        Long processId = servicePointWithdrawService.getOneId(servicePointWithdraw);

        model.addAttribute("canPay", processId == null ? 1 : 0);

        return "modules/fi/servicePointPayConfirmForm";
    }

    @ResponseBody
    @RequestMapping(value = {"paysave"}, method = RequestMethod.POST)
    public AjaxJsonEntity paySavePost(@RequestParam String servicePointId, @RequestParam Date invoiceDate, @RequestParam String balance, @RequestParam String debtsAmount, @RequestParam String realAmount,
                                      @RequestParam String paymentType, @RequestParam String bank, @RequestParam String branch, @RequestParam String bankNo,
                                      @RequestParam String bankOwner, @RequestParam String bankOwnerIdNo, @RequestParam String bankOwnerPhone,
                                      @RequestParam String remarks, @RequestParam String qYear, @RequestParam String qMonth,
                                      @RequestParam String totalMinus, @RequestParam String platformFee,
                                      HttpServletRequest request, HttpServletResponse response, Model model) {
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity jsonEntity = new AjaxJsonEntity(true);
        try {
            // 保存
            servicePointWithdrawService.servicePointPay(Long.parseLong(servicePointId), invoiceDate, Double.parseDouble(balance), Double.parseDouble(debtsAmount),
                    Double.parseDouble(realAmount), Integer.parseInt(paymentType), Integer.parseInt(bank), branch, bankNo, bankOwner, bankOwnerIdNo, bankOwnerPhone,
                    remarks, Integer.parseInt(qYear), Integer.parseInt(qMonth), Double.parseDouble(totalMinus), 0 - Double.parseDouble(platformFee));
        } catch (Exception e) {
            try {
                LogUtils.saveLog("网点付款单个", "FI:ServicePointWithdrawPaySave",
                        String.format("servicePointId:%s,realAmount:%s,paymentType:%s,bank:%s,bankNo:%s,remarks:%s,qYear:%s,qMonth:%s",
                                servicePointId, realAmount, paymentType, bank, bankNo, remarks, qYear, qMonth), e, null);
            } catch (Exception loge) {
            }
            log.error(e.getLocalizedMessage());
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage(e.getMessage());
        }
        return jsonEntity;
    }

    //region 付款

    @RequestMapping(value = {"issueget"}, method = RequestMethod.GET)
    public String issueGet(String servicePointId, HttpServletRequest request, HttpServletResponse response, Model model) {
        model.addAttribute("servicePointId", servicePointId);
        return "modules/fi/servicePointPayIssueForm";
    }

    @ResponseBody
    @RequestMapping(value = {"issuesave"}, method = RequestMethod.POST)
    public AjaxJsonEntity issuePost(String servicePointId, String bankIssueValue, HttpServletRequest request, HttpServletResponse response, Model model) {
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity jsonEntity = new AjaxJsonEntity(true);
        try {
            ServicePointFinance servicePointFinance = new ServicePointFinance();
            servicePointFinance.setId(Long.parseLong(servicePointId));
            servicePointFinance.setBankIssue(new Dict(bankIssueValue));
            servicePointService.updateBankIssue(servicePointFinance);
            servicePointService.updateBankIssueFI(servicePointFinance);

            //更新缓存
            // mark on 2020-5-4 begin
//            ServicePoint cachedServicePoint = servicePointService.getFromCache(servicePointFinance.getId());
//            Dict bankIssue = MSDictUtils.getDictByValue(bankIssueValue, "BankIssueType");//切换为微服务
//            if (bankIssue == null) {
//                bankIssue = new Dict(cachedServicePoint.getFinance().getBankIssue().getValue(), "读取错误");
//            }
//            cachedServicePoint.getFinance().setBankIssue(bankIssue);//ryan at 2018/02/05
            // mark on 2020-5-4 end
            //cachedServicePoint.getFinance().setBankIssue(new Dict(bankIssueValue));
            //servicePointService.updateServicePointCache(cachedServicePoint);  //mark on 2020-1-14  web端去servicePoint

            // add on 20202-5-4 begin
            // 更新网点财务缓存
            ServicePointFinance cachedServicePointFinance = servicePointFinanceService.getFromCache(servicePointFinance.getId());
            Dict bankIssue = MSDictUtils.getDictByValue(bankIssueValue, "BankIssueType");//切换为微服务
            if (bankIssue == null) {
                bankIssue = new Dict(cachedServicePointFinance.getBankIssue().getValue(), "读取错误");
            }
            cachedServicePointFinance.setBankIssue(bankIssue);
            servicePointFinanceService.updateCache(cachedServicePointFinance);
            // add on 2020-5-4 end
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage(e.getMessage());
        }
        return jsonEntity;
    }

    @ResponseBody
    @RequestMapping(value = {"payselected"})
    public AjaxJsonEntity save(@RequestParam String datas, @RequestParam String qPayment, @RequestParam String qBank, @RequestParam String payBank,
                               HttpServletResponse response) {
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try {
            servicePointWithdrawService.servicePointPaySelected(datas, qPayment, qBank, payBank);
        } catch (Exception e) {
            try {
                LogUtils.saveLog("网点付款批量", "FI:ServicePointWithdrawPaySelected",
                        String.format("datas:%s,qPayment:%s,qBank:%s", datas, qPayment, qBank), e, null);
            } catch (Exception loge) {
            }
            result.setSuccess(false);
            result.setMessage(e.getMessage());
        }
        return result;
    }
}
