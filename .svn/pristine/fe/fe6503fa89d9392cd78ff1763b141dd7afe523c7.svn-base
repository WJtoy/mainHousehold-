package com.wolfking.jeesite.modules.fi.web;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kkl.kklplus.entity.md.MDServicePointViewModel;
import com.wolfking.jeesite.common.config.Global;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.Encodes;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.wolfking.jeesite.common.utils.excel.ExportExcel;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.fi.entity.ServicePointPayCondition;
import com.wolfking.jeesite.modules.fi.entity.ServicePointWithdraw;
import com.wolfking.jeesite.modules.fi.service.ServicePointWithdrawService;
import com.wolfking.jeesite.modules.md.entity.Engineer;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.md.entity.ServicePointFinance;
import com.wolfking.jeesite.modules.md.service.ServicePointService;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providermd.service.MSServicePointService;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Jeff on 2017/6/15.
 */
@Slf4j
@Controller
@RequestMapping(value = "${adminPath}/fi/servicepointinvoice/confirm")
public class ServicePointInvoiceConfirmController extends BaseController {

    public static final int EXECL_CELL_WIDTH_15 = 15;
    public static final int EXECL_CELL_WIDTH_8 = 8;

    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private ServicePointWithdrawService servicePointWithdrawService;
    @Autowired
    private ServicePointService servicePointService;
    @Autowired
    private MSServicePointService msServicePointService;

    private String getNeedConfirmPaymentListCacheKey() {
        return String.format("SERVICEPOINT:NEED:CONFIRM:PAYMENT:%d", UserUtils.getUser().getId());
    }

    private String getNeedConfirmConditionCacheKey() {
        return String.format("SERVICEPOINT:NEED:CONFIRM:CONDITION:%d", UserUtils.getUser().getId());
    }

    private String getNeedConfirmBankCacheKey() {
        return String.format("SERVICEPOINT:NEED:CONFIRM:BANK:%d", UserUtils.getUser().getId());
    }

    private String getNeedConfirmEngineerNameListCacheKey() {
        return String.format("SERVICEPOINT:NEED:CONFIRM:ENGINEER:NAME:%d", UserUtils.getUser().getId());
    }

    private void initWithDrawInvoiceCache() {
        if (redisUtils.exists(getNeedConfirmPaymentListCacheKey())) {
            redisUtils.remove(RedisConstant.RedisDBType.REDIS_TEMP_DB, getNeedConfirmPaymentListCacheKey());
        }
        if (redisUtils.exists(getNeedConfirmConditionCacheKey())) {
            redisUtils.remove(RedisConstant.RedisDBType.REDIS_TEMP_DB, getNeedConfirmConditionCacheKey());
        }

        //获取确认列表
        //月结 -- 10
        //处理中的银行列表
        ServicePointWithdraw servicePointWithdraw = new ServicePointWithdraw();
        servicePointWithdraw.setPaymentType(10);
        List<ServicePointPayCondition> confirmMonthlyList = servicePointWithdrawService.getInvoiceConfirmList(servicePointWithdraw);
        //即结 -- 20
        //处理中的银行列表
        servicePointWithdraw.setPaymentType(20);
        List<ServicePointPayCondition> confirmDailyList = servicePointWithdrawService.getInvoiceConfirmList(servicePointWithdraw);
        List<Integer> needConfirmPaymentList = Lists.newArrayList();
        if (confirmMonthlyList != null && confirmMonthlyList.size() > 0) {
            needConfirmPaymentList.add(10);
        }
        if (confirmDailyList != null && confirmDailyList.size() > 0) {
            needConfirmPaymentList.add(20);
        }

        List<ServicePointPayCondition> needConfirmConditionList = Lists.newArrayList();
        needConfirmConditionList.addAll(confirmMonthlyList);
        needConfirmConditionList.addAll(confirmDailyList);

        redisUtils.set(RedisConstant.RedisDBType.REDIS_TEMP_DB, getNeedConfirmPaymentListCacheKey(), needConfirmPaymentList, 4 * 60 * 60);
        redisUtils.set(RedisConstant.RedisDBType.REDIS_TEMP_DB, getNeedConfirmConditionCacheKey(), needConfirmConditionList, 4 * 60 * 60);
    }

    //切换为微服务
    private List<Dict> getNeedConfirmPaymentType() {
        List<Long> paymentValueList = redisUtils.getList(RedisConstant.RedisDBType.REDIS_TEMP_DB, getNeedConfirmPaymentListCacheKey(), Long[].class);
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

    private List<ServicePointPayCondition> getNeedConfirmCondition() {
        return redisUtils.getList(RedisConstant.RedisDBType.REDIS_TEMP_DB, getNeedConfirmConditionCacheKey(), ServicePointPayCondition[].class);
    }

    private void clearNeedConfirmBank() {

        if (redisUtils.exists(getNeedConfirmBankCacheKey())) {
            redisUtils.remove(RedisConstant.RedisDBType.REDIS_TEMP_DB, getNeedConfirmBankCacheKey());
        }
    }

    private void clearNeedConfirmEngineerNameList() {
        if (redisUtils.exists(getNeedConfirmEngineerNameListCacheKey())) {
            redisUtils.remove(RedisConstant.RedisDBType.REDIS_TEMP_DB, getNeedConfirmEngineerNameListCacheKey());
        }
    }

    private List<HashMap> getNeedConfirmBank() {
        return redisUtils.getList(RedisConstant.RedisDBType.REDIS_TEMP_DB, getNeedConfirmBankCacheKey(), HashMap[].class);
    }

    private List<HashMap> getNeedConfirmEngineerName() {
        return redisUtils.getList(RedisConstant.RedisDBType.REDIS_TEMP_DB, getNeedConfirmEngineerNameListCacheKey(), HashMap[].class);
    }

    @RequestMapping(value = {""}, method = RequestMethod.GET)
    public String payFormGet(@RequestParam Map<String, Object> paramMap, HttpServletRequest request, HttpServletResponse response, Model model) {
        initWithDrawInvoiceCache();
        Date payableMonthDate = DateUtils.addMonth(new Date(), -1);
        paramMap.put("engineerStatus", null);
        paramMap.put("engineerInvoiceFlag", null);
        paramMap.put("withdrawStatus", 20);
        model.addAttribute("page", null);
        model.addAttribute("bankList", null);
        model.addAttribute("paymentTypeList", getNeedConfirmPaymentType());
        model.addAllAttributes(paramMap);
        return "modules/fi/servicePointInvoiceConfirmForm";
    }

    @RequestMapping(value = {""}, method = RequestMethod.POST)
    public String payFormPost(@RequestParam Map<String, Object> paramMap, HttpServletRequest request, HttpServletResponse response, Model model) {
        model.addAttribute("paymentTypeList", getNeedConfirmPaymentType());
        model.addAttribute("bankList", getNeedConfirmBank());
        model.addAttribute("engineerList", getNeedConfirmEngineerName());

        ServicePointFinance servicePointFinance = new ServicePointFinance();
        ServicePoint servicePoint = new ServicePoint();
        ServicePointWithdraw servicePointWithdraw = new ServicePointWithdraw();
        if (paramMap.containsKey("payment") && paramMap.get("payment").toString().trim().length() > 0) {
            servicePointWithdraw.setPaymentType(Integer.parseInt(paramMap.get("payment").toString()));
        }
        if (paramMap.containsKey("bank") && paramMap.get("bank").toString().trim().length() > 0) {
            servicePointWithdraw.setBank(Integer.parseInt(paramMap.get("bank").toString()));
        }
        if (paramMap.containsKey("engineer") && paramMap.get("engineer").toString().trim().length() > 0) {
            servicePoint.setId(Long.parseLong(paramMap.get("engineer").toString()));
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
        if (paramMap.containsKey("createBeginDate") && paramMap.get("createBeginDate").toString().trim().length() > 0) {
            servicePointWithdraw.setCreateBeginDate(DateUtils.getDateStart(DateUtils.parseDate(paramMap.get("createBeginDate").toString())));
        }
        if (paramMap.containsKey("createEndDate") && paramMap.get("createEndDate").toString().trim().length() > 0) {
            servicePointWithdraw.setCreateEndDate(DateUtils.getDateEnd(DateUtils.parseDate(paramMap.get("createEndDate").toString())));
        }
        if (paramMap.containsKey("withdrawNo") && paramMap.get("withdrawNo").toString().trim().length() > 0) {
            servicePointWithdraw.setWithdrawNo(paramMap.get("withdrawNo").toString());
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
        servicePoint.setFinance(servicePointFinance);
        servicePointWithdraw.setServicePoint(servicePoint);
//        Page<ServicePointWithdraw> page = servicePointWithdrawService.getInvoiceConfirmDetailPage(new Page<>(request, response), servicePointWithdraw);
//        model.addAttribute("page", page);
        List<ServicePointWithdraw> list = servicePointWithdrawService.getInvoiceConfirmDetailList(servicePointWithdraw);
        //切换为微服务

        if (list != null && list.size() > 0){
            Map<String,Dict> paymentTypeMap = MSDictUtils.getDictMap("PaymentType");
            Map<String,Dict> bankTypeMap = MSDictUtils.getDictMap("banktype");
            Map<String,Dict> statusMap = MSDictUtils.getDictMap("ServicePointWithdrawStatus");
            for (ServicePointWithdraw withdraw : list){
                if (withdraw.getPaymentType() != null && withdraw.getPaymentType() > 0){
                    Dict paymentDict = paymentTypeMap.get(withdraw.getPaymentType().toString());
                    if (paymentDict != null) {
                        withdraw.setPaymentTypeName(paymentDict.getLabel());
                    }
                }
                if (withdraw.getBank() != null && withdraw.getBank() > 0){
                    Dict bankDict = bankTypeMap.get(withdraw.getBank().toString());
                    if (bankDict != null){
                        withdraw.setBankName(bankDict.getLabel());
                    }
                }
                if (withdraw.getStatus() != null && withdraw.getStatus() > 0){
                    Dict statusDict = statusMap.get(withdraw.getStatus().toString());
                    if (statusDict != null){
                        withdraw.setStatusName(statusDict.getLabel());
                    }
                }
            }
        }
        model.addAttribute("list", list);
        model.addAllAttributes(paramMap);
        return "modules/fi/servicePointInvoiceConfirmForm";
    }

    @RequestMapping(value = {"/new"}, method = RequestMethod.GET)
    public String payFormGetNew(@RequestParam Map<String, Object> paramMap, HttpServletRequest request, HttpServletResponse response, Model model) {
        initWithDrawInvoiceCache();
        Date payableMonthDate = DateUtils.addMonth(new Date(), -1);
        paramMap.put("engineerStatus", null);
        paramMap.put("engineerInvoiceFlag", null);
        paramMap.put("withdrawStatus", 20);
        model.addAttribute("page", null);
        model.addAttribute("bankList", null);
        model.addAttribute("paymentTypeList", getNeedConfirmPaymentType());
        model.addAllAttributes(paramMap);
        return "modules/fi/servicePointInvoiceConfirmFormNew";
    }

    @RequestMapping(value = {"/new"}, method = RequestMethod.POST)
    public String payFormPostNew(@RequestParam Map<String, Object> paramMap, HttpServletRequest request, HttpServletResponse response, Model model) {
        model.addAttribute("paymentTypeList", getNeedConfirmPaymentType());
        model.addAttribute("bankList", getNeedConfirmBank());
        model.addAttribute("engineerList", getNeedConfirmEngineerName());

        ServicePointFinance servicePointFinance = new ServicePointFinance();
        ServicePoint servicePoint = new ServicePoint();
        ServicePointWithdraw servicePointWithdraw = new ServicePointWithdraw();
        if (paramMap.containsKey("payment") && paramMap.get("payment").toString().trim().length() > 0) {
            servicePointWithdraw.setPaymentType(Integer.parseInt(paramMap.get("payment").toString()));
        }
        if (paramMap.containsKey("bank") && paramMap.get("bank").toString().trim().length() > 0) {
            servicePointWithdraw.setBank(Integer.parseInt(paramMap.get("bank").toString()));
        }
        if (paramMap.containsKey("engineer") && paramMap.get("engineer").toString().trim().length() > 0) {
            servicePoint.setId(Long.parseLong(paramMap.get("engineer").toString()));
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
        if (paramMap.containsKey("createBeginDate") && paramMap.get("createBeginDate").toString().trim().length() > 0) {
            servicePointWithdraw.setCreateBeginDate(DateUtils.getDateStart(DateUtils.parseDate(paramMap.get("createBeginDate").toString())));
        }
        if (paramMap.containsKey("createEndDate") && paramMap.get("createEndDate").toString().trim().length() > 0) {
            servicePointWithdraw.setCreateEndDate(DateUtils.getDateEnd(DateUtils.parseDate(paramMap.get("createEndDate").toString())));
        }
        if (paramMap.containsKey("withdrawNo") && paramMap.get("withdrawNo").toString().trim().length() > 0) {
            servicePointWithdraw.setWithdrawNo(paramMap.get("withdrawNo").toString());
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
        servicePoint.setFinance(servicePointFinance);
        servicePointWithdraw.setServicePoint(servicePoint);
//        Page<ServicePointWithdraw> page = servicePointWithdrawService.getInvoiceConfirmDetailPage(new Page<>(request, response), servicePointWithdraw);
//        model.addAttribute("page", page);
        List<ServicePointWithdraw> list = servicePointWithdrawService.getInvoiceConfirmDetailListNew(servicePointWithdraw);
        //切换为微服务

        if (list != null && list.size() > 0){
            Map<String,Dict> paymentTypeMap = MSDictUtils.getDictMap("PaymentType");
            Map<String,Dict> bankTypeMap = MSDictUtils.getDictMap("banktype");
            Map<String,Dict> statusMap = MSDictUtils.getDictMap("ServicePointWithdrawStatus");
            for (ServicePointWithdraw withdraw : list){
                if (withdraw.getPaymentType() != null && withdraw.getPaymentType() > 0){
                    Dict paymentDict = paymentTypeMap.get(withdraw.getPaymentType().toString());
                    if (paymentDict != null) {
                        withdraw.setPaymentTypeName(paymentDict.getLabel());
                    }
                }
                if (withdraw.getBank() != null && withdraw.getBank() > 0){
                    Dict bankDict = bankTypeMap.get(withdraw.getBank().toString());
                    if (bankDict != null){
                        withdraw.setBankName(bankDict.getLabel());
                    }
                }
                if (withdraw.getStatus() != null && withdraw.getStatus() > 0){
                    Dict statusDict = statusMap.get(withdraw.getStatus().toString());
                    if (statusDict != null){
                        withdraw.setStatusName(statusDict.getLabel());
                    }
                }
            }
        }
        model.addAttribute("list", list);
        model.addAllAttributes(paramMap);
        return "modules/fi/servicePointInvoiceConfirmFormNew";
    }

    @SuppressWarnings("unchecked")
    @ResponseBody
    @RequestMapping(value = {"getbanklist"})
    public AjaxJsonEntity getBankListByPaymentType(@RequestParam String paymentType, HttpServletRequest request, HttpServletResponse response) {
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity jsonEntity = new AjaxJsonEntity(true);
        try {
            clearNeedConfirmBank();
            clearNeedConfirmEngineerNameList();
            List<Map<String, String>> bankList = new ArrayList<>();
            List<ServicePointPayCondition> needConfirmCondition = getNeedConfirmCondition();

            //切换为微服务
            Map<String, Dict> bankTypeMap = MSDictUtils.getDictMap("banktype");
            for (ServicePointPayCondition item : needConfirmCondition) {
                if (item.getBank() != null && item.getBank() > 0) {
                    Dict bankDict = bankTypeMap.get(item.getBank().toString());
                    if (bankDict != null && bankDict.getLabel() != null) {
                        item.setBankName(bankDict.getLabel());
                    }
                    else {
                        item.setBankName("");
                    }
                }
            }

            for (ServicePointPayCondition servicePointPayCondition : needConfirmCondition) {
                if (servicePointPayCondition.getPaymentType().toString().equals(paymentType)) {
                    Map<String, String> bankMap = new HashMap<>();
                    bankMap.put("text", servicePointPayCondition.getBankName());
                    bankMap.put("value", servicePointPayCondition.getBank().toString());
                    if (!bankList.contains(bankMap)) {
                        bankList.add(bankMap);
                    }
                }
            }
            redisUtils.set(RedisConstant.RedisDBType.REDIS_TEMP_DB, getNeedConfirmBankCacheKey(), bankList, 4 * 60 * 60);
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
    public AjaxJsonEntity getEngineerListByBank(@RequestParam String bank, String paymentType, HttpServletRequest request, HttpServletResponse response) {
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity jsonEntity = new AjaxJsonEntity(true);
        try {
            clearNeedConfirmEngineerNameList();
            List<Map<String, String>> engineerNameList = new ArrayList<>();
            List<ServicePointPayCondition> needConfirmCondition = getNeedConfirmCondition();
            List<Long> servicePointIds = Lists.newArrayList();
            for (ServicePointPayCondition servicePointPayCondition : needConfirmCondition) {
                if (servicePointPayCondition.getBank().toString().equals(bank) &&
                        servicePointPayCondition.getPaymentType().toString().equals(paymentType)) {
                    //Map<String, String> engineerMap = new HashMap<>();  //mark on 2020-2-19
                    // mark on 2020-1-17 begin  web端去md_servicepoint
                    //ServicePoint servicePoint = (ServicePoint) redisUtils.zRangeOneByScore(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_SERVICEPOINT_ALL,
                    //        servicePointPayCondition.getServicePointId(), servicePointPayCondition.getServicePointId(), ServicePoint.class);
                    // mark on 2020-1-17 end
                    //ServicePoint servicePoint = servicePointService.getFromCache(servicePointPayCondition.getServicePointId());   //add on 2020-1-19
                    /*  //mark on 2020-2-19 begin
                    ServicePoint servicePoint = servicePointService.getSimpleFromCache(servicePointPayCondition.getServicePointId());   //add on 2020-2-19
                    if (servicePoint.getPrimary() != null && servicePoint.getPrimary().getName() != null) {
                        engineerMap.put("text", servicePoint.getServicePointNo() + "," + servicePoint.getPrimary().getName());
                    } else {
                        engineerMap.put("text", servicePoint.getServicePointNo() + "," + servicePoint.getName());
                    }
                    engineerMap.put("value", servicePointPayCondition.getServicePointId().toString());
                    engineerNameList.add(engineerMap);
                    //mark on 2020-2-19 end
                    */
                    servicePointIds.add(servicePointPayCondition.getServicePointId());
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

            redisUtils.set(RedisConstant.RedisDBType.REDIS_TEMP_DB, getNeedConfirmEngineerNameListCacheKey(), engineerNameList, 4 * 60 * 60);
            jsonEntity.setData(engineerNameList);
        } catch (Exception e) {
            log.error(e.getStackTrace().toString().concat("\n").concat(e.toString()));
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage(e.getMessage());
        }
        return jsonEntity;
    }

    @RequestMapping(value = {"fail"}, method = RequestMethod.GET)
    public String issueGet(String withdrawId, String servicePointId, HttpServletRequest request, HttpServletResponse response, Model model) {
        model.addAttribute("withdrawId", withdrawId);
        model.addAttribute("servicePointId", servicePointId);
        return "modules/fi/servicePointInvoiceConfirmFail";
    }

    @ResponseBody
    @RequestMapping(value = {"fail"}, method = RequestMethod.POST)
    public AjaxJsonEntity issuePost(String withdrawId, String servicePointId, String bankIssueValue, HttpServletRequest request, HttpServletResponse response, Model model) {
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity jsonEntity = new AjaxJsonEntity(true);
        try {
            servicePointWithdrawService.servicePointConfirmFail(Long.parseLong(withdrawId), Long.parseLong(servicePointId), bankIssueValue);
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage(e.getMessage());
        }
        return jsonEntity;
    }

    @RequestMapping(value = {"edit"}, method = RequestMethod.GET)
    public String editGet(String withdrawId, HttpServletRequest request, HttpServletResponse response, Model model) {
        Long wId = Long.parseLong(withdrawId);
        ServicePointWithdraw servicePointWithdraw = servicePointWithdrawService.get(wId);
        model.addAttribute("servicePointWithdraw", servicePointWithdraw);
        return "modules/fi/servicePointInvoiceConfirmEdit";
    }

    @ResponseBody
    @RequestMapping(value = {"edit"}, method = RequestMethod.POST)
    public AjaxJsonEntity editPost(String withdrawId, String bank, String branch, String bankNo, String bankOwner,
                                   String payDate, HttpServletRequest request, HttpServletResponse response, Model model) {
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity jsonEntity = new AjaxJsonEntity(true);
        try {
            servicePointWithdrawService.servicePointConfirmEdit(Long.parseLong(withdrawId), Integer.parseInt(bank), branch, bankNo, bankOwner, DateUtils.parseDate(payDate));
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage(e.getMessage());
        }
        return jsonEntity;
    }

    @ResponseBody
    @RequestMapping(value = {"selected"})
    public AjaxJsonEntity confirmSelected(@RequestParam String ids, HttpServletResponse response) {
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try {
            servicePointWithdrawService.servicePointConfirmSelectedSuccess(ids);
        } catch (Exception e) {
            try {
                LogUtils.saveLog("付款确认", "FI:ServicePointInvoiceConfirm", ids, e, null);
            } catch (Exception loge) {
            }
            result.setSuccess(false);
            result.setMessage(e.getMessage());
        }
        return result;
    }

    @RequestMapping(value = "export", method = RequestMethod.POST)
    public String payExportFile(@RequestParam Map<String, Object> paramMap, HttpServletRequest request,
                                HttpServletResponse response, RedirectAttributes redirectAttributes) {
        try {
            String xName = "网点付款明细";
            SXSSFWorkbook xBook;
            Sheet xSheet;
            Map<String, CellStyle> xStyle;

            ExportExcel exportExcel = new ExportExcel();
            xBook = new SXSSFWorkbook(500);
            xSheet = xBook.createSheet(xName);
            xSheet.setDefaultColumnWidth(EXECL_CELL_WIDTH_15);
            xStyle = exportExcel.createStyles(xBook);

            // 加入标题
            int rowNum = 0;

            Row headRow;
            Row dataRow;
            Cell cell;
            // 加入表头
            headRow = xSheet.createRow(rowNum++);
            headRow.setHeightInPoints(16);

            String[] TableTitle = new String[]
                    {"收款账户列", "收款户名列", "转账金额列", "备注列", "收款银行列"};

            for (int i = 0; i < TableTitle.length; i++) {
//                xSheet.addMergedRegion(new CellRangeAddress(1, 1, i, i));
                cell = headRow.createCell(i);
                cell.setCellStyle(xStyle.get("header"));
                cell.setCellValue(TableTitle[i]);
            }
            xSheet.createFreezePane(0, rowNum); // 冻结单元格(x, y)

            ServicePointFinance servicePointFinance = new ServicePointFinance();
            ServicePoint servicePoint = new ServicePoint();
            ServicePointWithdraw servicePointWithdraw = new ServicePointWithdraw();
            if (paramMap.containsKey("payment") && paramMap.get("payment").toString().trim().length() > 0) {
                servicePointWithdraw.setPaymentType(Integer.parseInt(paramMap.get("payment").toString()));
            }
            if (paramMap.containsKey("bank") && paramMap.get("bank").toString().trim().length() > 0) {
                servicePointWithdraw.setBank(Integer.parseInt(paramMap.get("bank").toString()));
            }
            if (paramMap.containsKey("engineer") && paramMap.get("engineer").toString().trim().length() > 0) {
                servicePoint.setId(Long.parseLong(paramMap.get("engineer").toString()));
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
            if (paramMap.containsKey("createBeginDate") && paramMap.get("createBeginDate").toString().trim().length() > 0) {
                servicePointWithdraw.setCreateBeginDate(DateUtils.getDateStart(DateUtils.parseDate(paramMap.get("createBeginDate").toString())));
            }
            if (paramMap.containsKey("createEndDate") && paramMap.get("createEndDate").toString().trim().length() > 0) {
                servicePointWithdraw.setCreateEndDate(DateUtils.getDateEnd(DateUtils.parseDate(paramMap.get("createEndDate").toString())));
            }
            if (paramMap.containsKey("withdrawNo") && paramMap.get("withdrawNo").toString().trim().length() > 0) {
                servicePointWithdraw.setWithdrawNo(paramMap.get("withdrawNo").toString());
            }
            if (paramMap.containsKey("payBeginDate") && paramMap.get("payBeginDate").toString().trim().length() > 0) {
                servicePointWithdraw.setPayBeginDate(DateUtils.getDateStart(DateUtils.parseDate(paramMap.get("payBeginDate").toString())));
            }
            if (paramMap.containsKey("payEndDate") && paramMap.get("payEndDate").toString().trim().length() > 0) {
                servicePointWithdraw.setPayEndDate(DateUtils.getDateEnd(DateUtils.parseDate(paramMap.get("payEndDate").toString())));
            }
            servicePointWithdraw.setStatus(20);
            servicePoint.setFinance(servicePointFinance);
            servicePointWithdraw.setServicePoint(servicePoint);
            List<ServicePointWithdraw> list = servicePointWithdrawService.getInvoiceConfirmDetailList(servicePointWithdraw);

            //切换为微服务
            Map<String, Dict> bankTypeMap = MSDictUtils.getDictMap("banktype");
            for (ServicePointWithdraw item : list) {
                if (item.getBank() != null && item.getBank() > 0) {
                    Dict bankDict = bankTypeMap.get(item.getBank().toString());
                    if (bankDict != null && bankDict.getLabel() != null) {
                        item.setBankName(bankDict.getLabel());
                    }
                    else {
                        item.setBankName("");
                    }
                }
            }

            // 写入数据
            if (list != null && list.size() > 0) {
                int cellNum = 0;

                for (ServicePointWithdraw spw : list) {
                    dataRow = xSheet.createRow(rowNum++);
                    dataRow.setHeightInPoints(12);
                    cellNum = 0;

                    cell = dataRow.createCell(cellNum++);
                    cell.setCellStyle(xStyle.get("data"));
                    cell.setCellValue(spw.getBankNo() == null ? ""
                            : spw.getBankNo());

                    cell = dataRow.createCell(cellNum++);
                    cell.setCellStyle(xStyle.get("data"));
                    cell.setCellValue(spw.getBankOwner() == null ? ""
                            : spw.getBankOwner());

                    cell = dataRow.createCell(cellNum++);
                    cell.setCellStyle(xStyle.get("data"));
                    cell.setCellValue(
                            Double.valueOf(spw.getApplyAmount())
                    );

                    cell = dataRow.createCell(cellNum++);
                    cell.setCellStyle(xStyle.get("data"));

                    cell.setCellValue(spw.getRemarks());

                    cell = dataRow.createCell(cellNum++);
                    cell.setCellStyle(xStyle.get("data"));
//                    cell.setCellValue(DictUtils.getDictDescription(spw.getBank().toString(), "banktype", ""));
                    cell.setCellValue(spw.getBankName());//切换为微服务
                }

            }
            response.reset();
            response.setContentType("application/octet-stream; charset=utf-8");
            response.setHeader("Content-Disposition", "attachment; filename="
                    + Encodes.urlEncode(xName + ".xlsx"));
            xBook.write(response.getOutputStream());
            xBook.dispose();
            return null;
        } catch (Exception e) {
            addMessage(redirectAttributes, "导出Excel失败！失败信息：" + e.getMessage());
            return "redirect:" + Global.getAdminPath() + "/fi/servicepointinvoice/confirm";
        }
    }


    @RequestMapping(value = "exportNew", method = RequestMethod.POST)
    public String payExportFileNew(@RequestParam Map<String, Object> paramMap, HttpServletRequest request,
                                HttpServletResponse response, RedirectAttributes redirectAttributes) {
        try {
            String xName = "网点付款明细";
            SXSSFWorkbook xBook;
            Sheet xSheet;
            Map<String, CellStyle> xStyle;

            ExportExcel exportExcel = new ExportExcel();
            xBook = new SXSSFWorkbook(500);
            xSheet = xBook.createSheet(xName);
            xSheet.setDefaultColumnWidth(EXECL_CELL_WIDTH_15);
            xStyle = exportExcel.createStyles(xBook);

            // 加入标题
            int rowNum = 0;

            Row headRow;
            Row dataRow;
            Cell cell;
            // 加入表头
            headRow = xSheet.createRow(rowNum++);
            headRow.setHeightInPoints(16);

            String[] TableTitle = new String[]
                    {"收款账户列", "收款户名列", "转账金额列", "备注列", "收款银行列"};

            for (int i = 0; i < TableTitle.length; i++) {
//                xSheet.addMergedRegion(new CellRangeAddress(1, 1, i, i));
                cell = headRow.createCell(i);
                cell.setCellStyle(xStyle.get("header"));
                cell.setCellValue(TableTitle[i]);
            }
            xSheet.createFreezePane(0, rowNum); // 冻结单元格(x, y)

            ServicePointFinance servicePointFinance = new ServicePointFinance();
            ServicePoint servicePoint = new ServicePoint();
            ServicePointWithdraw servicePointWithdraw = new ServicePointWithdraw();
            if (paramMap.containsKey("payment") && paramMap.get("payment").toString().trim().length() > 0) {
                servicePointWithdraw.setPaymentType(Integer.parseInt(paramMap.get("payment").toString()));
            }
            if (paramMap.containsKey("bank") && paramMap.get("bank").toString().trim().length() > 0) {
                servicePointWithdraw.setBank(Integer.parseInt(paramMap.get("bank").toString()));
            }
            if (paramMap.containsKey("engineer") && paramMap.get("engineer").toString().trim().length() > 0) {
                servicePoint.setId(Long.parseLong(paramMap.get("engineer").toString()));
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
            if (paramMap.containsKey("createBeginDate") && paramMap.get("createBeginDate").toString().trim().length() > 0) {
                servicePointWithdraw.setCreateBeginDate(DateUtils.getDateStart(DateUtils.parseDate(paramMap.get("createBeginDate").toString())));
            }
            if (paramMap.containsKey("createEndDate") && paramMap.get("createEndDate").toString().trim().length() > 0) {
                servicePointWithdraw.setCreateEndDate(DateUtils.getDateEnd(DateUtils.parseDate(paramMap.get("createEndDate").toString())));
            }
            if (paramMap.containsKey("withdrawNo") && paramMap.get("withdrawNo").toString().trim().length() > 0) {
                servicePointWithdraw.setWithdrawNo(paramMap.get("withdrawNo").toString());
            }
            if (paramMap.containsKey("payBeginDate") && paramMap.get("payBeginDate").toString().trim().length() > 0) {
                servicePointWithdraw.setPayBeginDate(DateUtils.getDateStart(DateUtils.parseDate(paramMap.get("payBeginDate").toString())));
            }
            if (paramMap.containsKey("payEndDate") && paramMap.get("payEndDate").toString().trim().length() > 0) {
                servicePointWithdraw.setPayEndDate(DateUtils.getDateEnd(DateUtils.parseDate(paramMap.get("payEndDate").toString())));
            }
            servicePointWithdraw.setStatus(20);
            servicePoint.setFinance(servicePointFinance);
            servicePointWithdraw.setServicePoint(servicePoint);
            List<ServicePointWithdraw> list = servicePointWithdrawService.getInvoiceConfirmDetailListNew(servicePointWithdraw);

            //切换为微服务
            Map<String, Dict> bankTypeMap = MSDictUtils.getDictMap("banktype");
            for (ServicePointWithdraw item : list) {
                if (item.getBank() != null && item.getBank() > 0) {
                    Dict bankDict = bankTypeMap.get(item.getBank().toString());
                    if (bankDict != null && bankDict.getLabel() != null) {
                        item.setBankName(bankDict.getLabel());
                    }
                    else {
                        item.setBankName("");
                    }
                }
            }

            // 写入数据
            if (list != null && list.size() > 0) {
                int cellNum = 0;

                for (ServicePointWithdraw spw : list) {
                    dataRow = xSheet.createRow(rowNum++);
                    dataRow.setHeightInPoints(12);
                    cellNum = 0;

                    cell = dataRow.createCell(cellNum++);
                    cell.setCellStyle(xStyle.get("data"));
                    cell.setCellValue(spw.getBankNo() == null ? ""
                            : spw.getBankNo());

                    cell = dataRow.createCell(cellNum++);
                    cell.setCellStyle(xStyle.get("data"));
                    cell.setCellValue(spw.getBankOwner() == null ? ""
                            : spw.getBankOwner());

                    cell = dataRow.createCell(cellNum++);
                    cell.setCellStyle(xStyle.get("data"));
                    cell.setCellValue(
                            Double.valueOf(spw.getApplyAmount())
                    );

                    cell = dataRow.createCell(cellNum++);
                    cell.setCellStyle(xStyle.get("data"));

                    cell.setCellValue(spw.getRemarks());

                    cell = dataRow.createCell(cellNum++);
                    cell.setCellStyle(xStyle.get("data"));
//                    cell.setCellValue(DictUtils.getDictDescription(spw.getBank().toString(), "banktype", ""));
                    cell.setCellValue(spw.getBankName());//切换为微服务
                }

            }
            response.reset();
            response.setContentType("application/octet-stream; charset=utf-8");
            response.setHeader("Content-Disposition", "attachment; filename="
                    + Encodes.urlEncode(xName + ".xlsx"));
            xBook.write(response.getOutputStream());
            xBook.dispose();
            return null;
        } catch (Exception e) {
            addMessage(redirectAttributes, "导出Excel失败！失败信息：" + e.getMessage());
            return "redirect:" + Global.getAdminPath() + "/fi/servicepointinvoice/confirm/new";
        }
    }

    @RequestMapping(value = "exportNewGD", method = RequestMethod.POST)
    public String payExportFileNewGD(@RequestParam Map<String, Object> paramMap, HttpServletRequest request,
                                   HttpServletResponse response, RedirectAttributes redirectAttributes) {
        try {
            String xName = "网点付款明细";
            SXSSFWorkbook xBook;
            Sheet xSheet;
            Map<String, CellStyle> xStyle;

            ExportExcel exportExcel = new ExportExcel();
            xBook = new SXSSFWorkbook(500);
            xSheet = xBook.createSheet(xName);
            xSheet.setDefaultColumnWidth(EXECL_CELL_WIDTH_15);
            xStyle = exportExcel.createStyles(xBook);

            // 加入标题
            int rowNum = 0;

            Row headRow;
            Row dataRow;
            Cell cell;
            // 加入表头
            headRow = xSheet.createRow(rowNum++);
            headRow.setHeightInPoints(16);

            String[] TableTitle = new String[]
                    {"姓名", "证件类型", "证件号", "手机号", "银行卡号", "付款金额"};

            for (int i = 0; i < TableTitle.length; i++) {
                cell = headRow.createCell(i);
                cell.setCellStyle(xStyle.get("header"));
                cell.setCellValue(TableTitle[i]);
            }
            xSheet.createFreezePane(0, rowNum); // 冻结单元格(x, y)

            ServicePointFinance servicePointFinance = new ServicePointFinance();
            ServicePoint servicePoint = new ServicePoint();
            ServicePointWithdraw servicePointWithdraw = new ServicePointWithdraw();
            if (paramMap.containsKey("payment") && paramMap.get("payment").toString().trim().length() > 0) {
                servicePointWithdraw.setPaymentType(Integer.parseInt(paramMap.get("payment").toString()));
            }
            if (paramMap.containsKey("bank") && paramMap.get("bank").toString().trim().length() > 0) {
                servicePointWithdraw.setBank(Integer.parseInt(paramMap.get("bank").toString()));
            }
            if (paramMap.containsKey("engineer") && paramMap.get("engineer").toString().trim().length() > 0) {
                servicePoint.setId(Long.parseLong(paramMap.get("engineer").toString()));
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
            if (paramMap.containsKey("createBeginDate") && paramMap.get("createBeginDate").toString().trim().length() > 0) {
                servicePointWithdraw.setCreateBeginDate(DateUtils.getDateStart(DateUtils.parseDate(paramMap.get("createBeginDate").toString())));
            }
            if (paramMap.containsKey("createEndDate") && paramMap.get("createEndDate").toString().trim().length() > 0) {
                servicePointWithdraw.setCreateEndDate(DateUtils.getDateEnd(DateUtils.parseDate(paramMap.get("createEndDate").toString())));
            }
            if (paramMap.containsKey("withdrawNo") && paramMap.get("withdrawNo").toString().trim().length() > 0) {
                servicePointWithdraw.setWithdrawNo(paramMap.get("withdrawNo").toString());
            }
            if (paramMap.containsKey("payBeginDate") && paramMap.get("payBeginDate").toString().trim().length() > 0) {
                servicePointWithdraw.setPayBeginDate(DateUtils.getDateStart(DateUtils.parseDate(paramMap.get("payBeginDate").toString())));
            }
            if (paramMap.containsKey("payEndDate") && paramMap.get("payEndDate").toString().trim().length() > 0) {
                servicePointWithdraw.setPayEndDate(DateUtils.getDateEnd(DateUtils.parseDate(paramMap.get("payEndDate").toString())));
            }
            servicePointWithdraw.setStatus(20);
            servicePoint.setFinance(servicePointFinance);
            servicePointWithdraw.setServicePoint(servicePoint);
            List<ServicePointWithdraw> list = servicePointWithdrawService.getInvoiceConfirmDetailListNew(servicePointWithdraw);

            //切换为微服务
            Map<String, Dict> bankTypeMap = MSDictUtils.getDictMap("banktype");
            for (ServicePointWithdraw item : list) {
                if (item.getBank() != null && item.getBank() > 0) {
                    Dict bankDict = bankTypeMap.get(item.getBank().toString());
                    if (bankDict != null && bankDict.getLabel() != null) {
                        item.setBankName(bankDict.getLabel());
                    }
                    else {
                        item.setBankName("");
                    }
                }
            }

            // 写入数据
            if (list != null && list.size() > 0) {
                int cellNum = 0;

                for (ServicePointWithdraw spw : list) {
                    dataRow = xSheet.createRow(rowNum++);
                    dataRow.setHeightInPoints(12);
                    cellNum = 0;

                    cell = dataRow.createCell(cellNum++);
                    cell.setCellStyle(xStyle.get("data"));
                    cell.setCellValue(spw.getBankOwner() == null ? ""
                            : spw.getBankOwner());

                    cell = dataRow.createCell(cellNum++);
                    cell.setCellStyle(xStyle.get("data"));
                    cell.setCellValue("身份证");

                    cell = dataRow.createCell(cellNum++);
                    cell.setCellStyle(xStyle.get("data"));
                    cell.setCellValue(spw.getBankOwnerIdNo() == null ? ""
                            : spw.getBankOwnerIdNo());

                    cell = dataRow.createCell(cellNum++);
                    cell.setCellStyle(xStyle.get("data"));
                    cell.setCellValue(spw.getBankOwnerPhone() == null ? ""
                            : spw.getBankOwnerPhone());

                    cell = dataRow.createCell(cellNum++);
                    cell.setCellStyle(xStyle.get("data"));
                    cell.setCellValue(spw.getBankNo() == null ? ""
                            : spw.getBankNo());

                    cell = dataRow.createCell(cellNum++);
                    cell.setCellStyle(xStyle.get("data"));
                    cell.setCellValue(
                            Double.valueOf(spw.getApplyAmount())
                    );
                }

            }
            response.reset();
            response.setContentType("application/octet-stream; charset=utf-8");
            response.setHeader("Content-Disposition", "attachment; filename="
                    + Encodes.urlEncode(xName + ".xlsx"));
            xBook.write(response.getOutputStream());
            xBook.dispose();
            return null;
        } catch (Exception e) {
            addMessage(redirectAttributes, "导出Excel失败！失败信息：" + e.getMessage());
            return "redirect:" + Global.getAdminPath() + "/fi/servicepointinvoice/confirm/new";
        }
    }
}
