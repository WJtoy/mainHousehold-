package com.wolfking.jeesite.ms.providerrpt.controller;

import com.google.common.collect.Lists;
import com.kkl.kklplus.entity.rpt.RPTExploitDetailEntity;
import com.kkl.kklplus.entity.rpt.exception.RPTBaseException;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.ProductCategory;
import com.wolfking.jeesite.modules.md.utils.ProductUtils;
import com.wolfking.jeesite.modules.rpt.entity.RptSearchCondition;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.service.SystemService;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providerrpt.service.MSExploitDetailRptService;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Controller
@RequestMapping(value = "${adminPath}/rpt/provider/exploitDetail/")
public class MSExploitDetailRptController extends BaseController {

    private static final String MODEL_ATTR_PAGE = "page";
    private static final String MODEL_ATTR_RPTSEARCH = "rptSearchCondition";

    @Autowired
    private MSExploitDetailRptService msExploitDetailRptService;

    @Autowired
    private SystemService systemService;

    @Autowired
    private AreaService areaService;
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
            rptSearchCondition.setBeginDate(DateUtils.addMonth(now ,-3));
        }
        if (rptSearchCondition.getEndDate() == null) {
            rptSearchCondition.setEndDate(now);
        }

        return rptSearchCondition;
    }


    /**
     * 检查订单号，手机号输入
     * @param searchModel
     * @param model
     * @return
     */
    private Boolean checkOrderNoAndPhone(RptSearchCondition searchModel, Model model, Page<RPTExploitDetailEntity> page){
        if(searchModel == null){
            return true;
        }
        //检查电话
        int orderSerchType = searchModel.getOrderNoSearchType();
        if (StringUtils.isNotBlank(searchModel.getOrderNo())){
            if(orderSerchType != 1) {
                addMessage(model, "错误：请输入正确的订单号码");
                model.addAttribute(MODEL_ATTR_PAGE, page);
                model.addAttribute(MODEL_ATTR_RPTSEARCH, searchModel);
                return false;
            }else{
                //检查分片
                try {
                    Date goLiveDate = OrderUtils.getGoLiveDate();
                    String[] quarters = DateUtils.getQuarterRange(goLiveDate, new Date());
                    if(quarters.length == 2) {
                        int start = StringUtils.toInteger(quarters[0]);
                        int end = StringUtils.toInteger(quarters[1]);
                        if(start>0 && end > 0){
                            int quarter = StringUtils.toInteger(searchModel.getQuarter());
                            if(quarter < start || quarter > end){
                                addMessage(model, "错误：请输入正确的订单号码");
                                model.addAttribute(MODEL_ATTR_PAGE, page);
                                model.addAttribute(MODEL_ATTR_RPTSEARCH, searchModel);
                                return false;
                            }
                        }
                    }
                }catch (Exception e){
                    log.error("检查分片错误,orderNo:{}",searchModel.getOrderNo(),e);
                }
            }
        }
        if (StringUtils.isNotBlank(searchModel.getContactInfo())){
            if(searchModel.getIsPhone() != 1){
                addMessage(model, "错误：请输入正确的用户电话");
                model.addAttribute(MODEL_ATTR_PAGE, page);
                model.addAttribute(MODEL_ATTR_RPTSEARCH, searchModel);
                return false;
            }
        }
        return true;
    }
    /**
     * 开发明细报表
     *
     * @param rptSearchCondition
     * @param model
     * @return
     */
    @RequestMapping(value = "exploitDetailReport")
    public String exploitDetailReport(RptSearchCondition rptSearchCondition, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<RPTExploitDetailEntity> page = new Page<>(request, response);
        rptSearchCondition.setBeginDate(DateUtils.getStartOfDay(rptSearchCondition.getBeginDate()));
        rptSearchCondition.setEndDate(DateUtils.getEndOfDay(rptSearchCondition.getEndDate()));
        if(rptSearchCondition.getBeginPlanDate() != null){
            rptSearchCondition.setBeginPlanDate(DateUtils.getStartOfDay(rptSearchCondition.getBeginPlanDate()));
        }
        if(rptSearchCondition.getEndPlanDate() != null){
            rptSearchCondition.setEndPlanDate(DateUtils.getEndOfDay(rptSearchCondition.getEndPlanDate()));
        }
        Integer areaType = 0;
        if (rptSearchCondition.getAreaId() != null) {
            Area area = areaService.getFromCache(rptSearchCondition.getAreaId());
            areaType = area.getType();
        }
        List<ProductCategory> productCategoryList = ProductUtils.getProductCategoryRPTList();
        User user = UserUtils.getUser();
        List<Long> productCategoryIds = Lists.newArrayList();
        if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()) {
            productCategoryIds = systemService.getAuthorizedProductCategoryIds(user.getId());
            List<Long> finalProductCategoryIds = productCategoryIds;
            productCategoryList = productCategoryList.stream().filter(r -> finalProductCategoryIds.contains(r.getId())).collect(Collectors.toList());
        }
        if (rptSearchCondition.isSearching()) {
            Boolean isValid = checkOrderNoAndPhone(rptSearchCondition,model,page);
            if(!isValid){
                return "modules/providerrpt/exploitDetailReport";
            }
            if (rptSearchCondition.getProductCategory() != 0) {
                productCategoryIds = Lists.newArrayList();
                productCategoryIds.add(rptSearchCondition.getProductCategory().longValue());
            }
            if (productCategoryIds.isEmpty() &&
                    (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue())) {
            } else {
                page = msExploitDetailRptService.getExploitDetailList(page,rptSearchCondition.getOrderNo(),rptSearchCondition.getCustomerId(),rptSearchCondition.getAreaId(),areaType,
                        rptSearchCondition.getContactInfo(),rptSearchCondition.getBeginPlanDate(),rptSearchCondition.getEndPlanDate(),rptSearchCondition.getBeginDate(),
                        rptSearchCondition.getEndDate(),productCategoryIds,rptSearchCondition.getOrderNoSearchType(),rptSearchCondition.getIsPhone());
            }
        }

        model.addAttribute("page", page);
        model.addAttribute("productCategoryList", productCategoryList);
        return "modules/providerrpt/exploitDetailReport";
    }


    @ResponseBody
    @RequestMapping(value = "checkExportTask", method = RequestMethod.POST)
    public AjaxJsonEntity checkExportTask(RptSearchCondition rptSearchCondition,Model model) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try {
            User user = UserUtils.getUser();
            rptSearchCondition.setBeginDate(DateUtils.getStartOfDay(rptSearchCondition.getBeginDate()));
            rptSearchCondition.setEndDate(DateUtils.getEndOfDay(rptSearchCondition.getEndDate()));
            if(rptSearchCondition.getBeginPlanDate() != null){
                rptSearchCondition.setBeginPlanDate(DateUtils.getStartOfDay(rptSearchCondition.getBeginPlanDate()));
            }
            if(rptSearchCondition.getEndPlanDate() != null){
                rptSearchCondition.setEndPlanDate(DateUtils.getEndOfDay(rptSearchCondition.getEndPlanDate()));
            }
            Integer areaType = 0;
            if (rptSearchCondition.getAreaId() != null) {
                Area area = areaService.getFromCache(rptSearchCondition.getAreaId());
                areaType = area.getType();
            }
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
            Boolean isValid = checkOrderNoAndPhone(rptSearchCondition,model,null);
            if(!isValid){
                result.setSuccess(false);
                Object message = model.asMap().get("message");
                result.setMessage("创建报表导出任务失败，"+message);
                return result;
            }

            msExploitDetailRptService.checkRptExportTask(rptSearchCondition.getOrderNo(),rptSearchCondition.getCustomerId(),rptSearchCondition.getAreaId(),areaType,
                    rptSearchCondition.getContactInfo(),rptSearchCondition.getBeginPlanDate(),rptSearchCondition.getEndPlanDate(),rptSearchCondition.getBeginDate(),rptSearchCondition.getEndDate(),productCategoryIds, rptSearchCondition.getOrderNoSearchType(),rptSearchCondition.getIsPhone(),user);

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
    public AjaxJsonEntity export(RptSearchCondition rptSearchCondition,Model model) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try {
            User user = UserUtils.getUser();
            rptSearchCondition.setBeginDate(DateUtils.getStartOfDay(rptSearchCondition.getBeginDate()));
            rptSearchCondition.setEndDate(DateUtils.getEndOfDay(rptSearchCondition.getEndDate()));
            if(rptSearchCondition.getBeginPlanDate() != null){
                rptSearchCondition.setBeginPlanDate(DateUtils.getStartOfDay(rptSearchCondition.getBeginPlanDate()));
            }
            if(rptSearchCondition.getEndPlanDate() != null){
                rptSearchCondition.setEndPlanDate(DateUtils.getEndOfDay(rptSearchCondition.getEndPlanDate()));
            }
            Integer areaType = 0;
            if (rptSearchCondition.getAreaId() != null) {
                Area area = areaService.getFromCache(rptSearchCondition.getAreaId());
                areaType = area.getType();
            }
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
            Boolean isValid = checkOrderNoAndPhone(rptSearchCondition,model,null);
            if(!isValid){
                result.setSuccess(false);
                Object message = model.asMap().get("message");
                result.setMessage("创建报表导出任务失败，"+message);
                return result;
            }
            msExploitDetailRptService.createRptExportTask(rptSearchCondition.getOrderNo(),rptSearchCondition.getCustomerId(),rptSearchCondition.getAreaId(),areaType,
                    rptSearchCondition.getContactInfo(),rptSearchCondition.getBeginPlanDate(),rptSearchCondition.getEndPlanDate(),rptSearchCondition.getBeginDate(),rptSearchCondition.getEndDate(),productCategoryIds,rptSearchCondition.getOrderNoSearchType(),rptSearchCondition.getIsPhone(), user);
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
