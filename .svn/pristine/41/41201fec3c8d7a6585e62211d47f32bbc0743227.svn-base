package com.wolfking.jeesite.modules.sd.web;

import com.google.common.collect.Lists;
import com.kkl.kklplus.entity.cc.AbnormalForm;
import com.kkl.kklplus.entity.cc.AbnormalFormEnum;
import com.kkl.kklplus.entity.cc.vm.AbnormalFormSearchModel;
import com.kkl.kklplus.entity.common.NameValuePair;
import com.kkl.kklplus.entity.md.MDCustomerPraiseFee;
import com.kkl.kklplus.entity.md.MDCustomerPraiseFeePraiseStandardItem;
import com.kkl.kklplus.entity.praise.Praise;
import com.kkl.kklplus.entity.praise.PraiseStatusEnum;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.cc.entity.AbnormalFormModel;
import com.wolfking.jeesite.ms.cc.service.AbnormalFormService;
import com.wolfking.jeesite.ms.praise.entity.PraiseLogModel;
import com.wolfking.jeesite.ms.praise.entity.ViewPraiseModel;
import com.wolfking.jeesite.ms.praise.service.OrderPraiseService;
import com.wolfking.jeesite.ms.providermd.service.MSCustomerPraiseFeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 异常单控制器
 */
@Controller
@RequestMapping(value = "${adminPath}/sd/abnormalForm")
@Slf4j
public class AbnormalFormController extends BaseController {

    @Autowired
    private AbnormalFormService abnormalFormService;

    @Autowired
    private MSCustomerPraiseFeeService customerPraiseFeeService;

    @Autowired
    private OrderPraiseService orderPraiseService;

    /**
     * 待处理列表
     * @param abnormalFormSearchModel
     * @return
     */
/*    @RequiresPermissions(value ="sd:abnormal:view")
    @RequestMapping("waitProcessList")
    public String waitProcessList(AbnormalFormSearchModel abnormalFormSearchModel, HttpServletRequest request, HttpServletResponse response, Model model){
        Page<AbnormalFormModel> page = new Page(request, response);
        User user = UserUtils.getUser();
        String checkRegion = abnormalFormService.loadAreaForKefu(abnormalFormSearchModel,user);
        if(StringUtils.isNotBlank(checkRegion)){
            addMessage(model, checkRegion);
            model.addAttribute("page", page);
            model.addAttribute("abnormalForm", abnormalFormSearchModel);
            model.addAttribute("formTypeList",abnormalFormService.findFormTypeList());
            model.addAttribute("formSubType",abnormalFormService.findFormSubType());
            return "modules/sd/abnormalForm/waitProcessList";
        }
        page = abnormalFormService.waitProcessList(page,abnormalFormSearchModel);
        model.addAttribute("page", page);
        model.addAttribute("abnormalForm", abnormalFormSearchModel);
        model.addAttribute("formTypeList",abnormalFormService.findFormTypeList());
        model.addAttribute("formSubType",abnormalFormService.findFormSubType());
        return "modules/sd/abnormalForm/waitProcessList";
    }*/

    /**
     * 已处理列表
     * @param abnormalFormSearchModel
     * @return
     */
    @RequiresPermissions(value ="sd:abnormal:view")
    @RequestMapping("processedList")
    public String processedList(AbnormalFormSearchModel abnormalFormSearchModel, HttpServletRequest request, HttpServletResponse response, Model model){
        Page<AbnormalFormModel> page = new Page(request, response);
        User user = UserUtils.getUser();
        String checkRegion = abnormalFormService.loadAreaForKefu(abnormalFormSearchModel,user);
        if(StringUtils.isNotBlank(checkRegion)){
            addMessage(model, checkRegion);
            model.addAttribute("page", page);
            model.addAttribute("abnormalForm", abnormalFormSearchModel);
            model.addAttribute("formTypeList",abnormalFormService.findFormTypeList());
            model.addAttribute("formSubType",abnormalFormService.findFormSubType());
            return "modules/sd/abnormalForm/processedList";
        }
        page = abnormalFormService.processedList(page,abnormalFormSearchModel);
        model.addAttribute("page", page);
        model.addAttribute("abnormalForm", abnormalFormSearchModel);
        model.addAttribute("formTypeList",abnormalFormService.findFormTypeList());
        model.addAttribute("formSubType",abnormalFormService.findFormSubType());
        return "modules/sd/abnormalForm/processedList";
    }


    /**
     * app异常列表
     * @param abnormalFormSearchModel
     * @return
     */
    @RequiresPermissions(value ="sd:abnormal:view")
    @RequestMapping("appAbnormalList")
    public String appAbnormalList(AbnormalFormSearchModel abnormalFormSearchModel, HttpServletRequest request, HttpServletResponse response, Model model){
        Page<AbnormalFormModel> page = new Page(request, response);
        User user = UserUtils.getUser();
        String checkRegion = abnormalFormService.loadAreaForKefu(abnormalFormSearchModel,user);
        if(StringUtils.isNotBlank(checkRegion)){
            addMessage(model, checkRegion);
            model.addAttribute("page", page);
            model.addAttribute("abnormalForm", abnormalFormSearchModel);
            //model.addAttribute("formTypeList",abnormalFormService.findFormTypeList());
            model.addAttribute("formSubType",abnormalFormService.findSubTypeByFormType(AbnormalFormEnum.FormType.APP_ABNORMALY.getCode()));
            return "modules/sd/abnormalForm/appAbnormalList";
        }
        page = abnormalFormService.appAbnormalList(page,abnormalFormSearchModel);
        model.addAttribute("page", page);
        model.addAttribute("abnormalForm", abnormalFormSearchModel);
        //model.addAttribute("formTypeList",abnormalFormService.findFormTypeList());
        model.addAttribute("formSubType",abnormalFormService.findSubTypeByFormType(AbnormalFormEnum.FormType.APP_ABNORMALY.getCode()));
        return "modules/sd/abnormalForm/appAbnormalList";
    }

    /**
     * 审单异常列表
     * @param abnormalFormSearchModel
     * @return
     */
    @RequiresPermissions(value ="sd:abnormal:view")
    @RequestMapping("waitProcessList")
    public String reviewAbnormalList(AbnormalFormSearchModel abnormalFormSearchModel, HttpServletRequest request, HttpServletResponse response, Model model){
        Page<AbnormalFormModel> page = new Page(request, response);
        User user = UserUtils.getUser();
        String checkRegion = abnormalFormService.loadAreaForKefu(abnormalFormSearchModel,user);
        if(StringUtils.isNotBlank(checkRegion)){
            addMessage(model, checkRegion);
            model.addAttribute("page", page);
            model.addAttribute("abnormalForm", abnormalFormSearchModel);
            //model.addAttribute("formTypeList",abnormalFormService.findFormTypeList());
            model.addAttribute("formSubType",abnormalFormService.findSubTypeByFormType(AbnormalFormEnum.FormType.REVIEW_ABNORMALY.getCode()));
            return "modules/sd/abnormalForm/reviewAbnormalList";
        }
        page = abnormalFormService.reviewAbnormalList(page,abnormalFormSearchModel);
        model.addAttribute("page", page);
        model.addAttribute("abnormalForm", abnormalFormSearchModel);
        //model.addAttribute("formTypeList",abnormalFormService.findFormTypeList());
        model.addAttribute("formSubType",abnormalFormService.findSubTypeByFormType(AbnormalFormEnum.FormType.REVIEW_ABNORMALY.getCode()));
        return "modules/sd/abnormalForm/reviewAbnormalList";
    }


    /**
     * app完工异常
     * @param abnormalFormSearchModel
     * @return
     */
    @RequiresPermissions(value ="sd:abnormal:view")
    @RequestMapping("appCompleteAbnormalList")
    public String appCompleteAbnormalList(AbnormalFormSearchModel abnormalFormSearchModel, HttpServletRequest request, HttpServletResponse response, Model model){
        Page<AbnormalFormModel> page = new Page(request, response);
        User user = UserUtils.getUser();
        String checkRegion = abnormalFormService.loadAreaForKefu(abnormalFormSearchModel,user);
        if(StringUtils.isNotBlank(checkRegion)){
            addMessage(model, checkRegion);
            model.addAttribute("page", page);
            model.addAttribute("abnormalForm", abnormalFormSearchModel);
            //model.addAttribute("formTypeList",abnormalFormService.findFormTypeList());
            //model.addAttribute("formSubType",abnormalFormService.findFormSubType());
            return "modules/sd/abnormalForm/appCompleteAbnormalList";
        }
        page = abnormalFormService.appCompleteAbnormalList(page,abnormalFormSearchModel);
        model.addAttribute("page", page);
        model.addAttribute("abnormalForm", abnormalFormSearchModel);
        //model.addAttribute("formTypeList",abnormalFormService.findFormTypeList());
        //model.addAttribute("formSubType",abnormalFormService.findFormSubType());
        return "modules/sd/abnormalForm/appCompleteAbnormalList";
    }


    /**
     * 短信异常
     * @param abnormalFormSearchModel
     * @return
     */
    @RequiresPermissions(value ="sd:abnormal:view")
    @RequestMapping("smsAbnormalList")
    public String smsAbnormalList(AbnormalFormSearchModel abnormalFormSearchModel, HttpServletRequest request, HttpServletResponse response, Model model){
        Page<AbnormalFormModel> page = new Page(request, response);
        User user = UserUtils.getUser();
        String checkRegion = abnormalFormService.loadAreaForKefu(abnormalFormSearchModel,user);
        if(StringUtils.isNotBlank(checkRegion)){
            addMessage(model, checkRegion);
            model.addAttribute("page", page);
            model.addAttribute("abnormalForm", abnormalFormSearchModel);
            //model.addAttribute("formTypeList",abnormalFormService.findFormTypeList());
            //model.addAttribute("formSubType",abnormalFormService.findFormSubType());
            return "modules/sd/abnormalForm/smsAbnormalList";
        }
        page = abnormalFormService.smsAbnormalList(page,abnormalFormSearchModel);
        model.addAttribute("page", page);
        model.addAttribute("abnormalForm", abnormalFormSearchModel);
        //model.addAttribute("formTypeList",abnormalFormService.findFormTypeList());
        //model.addAttribute("formSubType",abnormalFormService.findFormSubType());
        return "modules/sd/abnormalForm/smsAbnormalList";
    }

    /**
     * 旧app异常列表
     * @param abnormalFormSearchModel
     * @return
     */
    @RequiresPermissions(value ="sd:abnormal:view")
    @RequestMapping("oldAppAbnormalList")
    public String oldAppAbnormalList(AbnormalFormSearchModel abnormalFormSearchModel, HttpServletRequest request, HttpServletResponse response, Model model){
        Page<AbnormalFormModel> page = new Page(request, response);
        User user = UserUtils.getUser();
        String checkRegion = abnormalFormService.loadAreaForKefu(abnormalFormSearchModel,user);
        if(StringUtils.isNotBlank(checkRegion)){
            addMessage(model, checkRegion);
            model.addAttribute("page", page);
            model.addAttribute("abnormalForm", abnormalFormSearchModel);
            //model.addAttribute("formTypeList",abnormalFormService.findFormTypeList());
            //model.addAttribute("formSubType",abnormalFormService.findFormSubType());
            return "modules/sd/abnormalForm/oldAppAbnormalList";
        }
        page = abnormalFormService.oldAppAbnormalList(page,abnormalFormSearchModel);
        model.addAttribute("page", page);
        model.addAttribute("abnormalForm", abnormalFormSearchModel);
        //model.addAttribute("formTypeList",abnormalFormService.findFormTypeList());
        //model.addAttribute("formSubType",abnormalFormService.findFormSubType());
        return "modules/sd/abnormalForm/oldAppAbnormalList";
    }

    /**
     * 好评单异常列表
     * @param abnormalFormSearchModel
     * @return
     */
    @RequiresPermissions(value ="sd:abnormal:view")
    @RequestMapping("praiseAbnormalList")
    public String praiseAbnormalList(AbnormalFormSearchModel abnormalFormSearchModel, HttpServletRequest request, HttpServletResponse response, Model model){
        Page<AbnormalFormModel> page = new Page(request, response);
        User user = UserUtils.getUser();
        String checkRegion = abnormalFormService.loadAreaForKefu(abnormalFormSearchModel,user);
        if(StringUtils.isNotBlank(checkRegion)){
            addMessage(model, checkRegion);
            model.addAttribute("page", page);
            model.addAttribute("abnormalForm", abnormalFormSearchModel);
            return "modules/sd/abnormalForm/praiseAbnormalList";
        }
        abnormalFormSearchModel.setFormType(AbnormalFormEnum.FormType.PRAISE_ABNORMAL.getCode());
        page = abnormalFormService.praiseAbnormalList(page,abnormalFormSearchModel);
        model.addAttribute("page", page);
        model.addAttribute("abnormalForm", abnormalFormSearchModel);
        return "modules/sd/abnormalForm/praiseAbnormalList";
    }

    /**
     * 关闭异常单
     * @param
     * @return
     */
    @RequiresPermissions(value ="sd:abnormal:close")
    @RequestMapping("closeAbnormalForKefu")
    @ResponseBody
    public AjaxJsonEntity closeAbnormalForKefu(AbnormalForm abnormalForm){
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        if(abnormalForm.getId() ==null || abnormalForm.getId()<=0){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("缺少异常单id,请检查");
            return ajaxJsonEntity;
        }
        if(abnormalForm.getOrderId()==null || abnormalForm.getOrderId()<=0){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("缺少工单id,请检查");
            return ajaxJsonEntity;
        }
        if(StringUtils.isBlank(abnormalForm.getQuarter())){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("缺少分片,请检查");
            return ajaxJsonEntity;
        }
        if(abnormalForm.getCreateDt() ==null || abnormalForm.getCreateDt()<=0){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("缺少反馈时间,请检查");
            return ajaxJsonEntity;
        }
        if(abnormalForm.getFormType()==null || abnormalForm.getFormType()<=0){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("缺少异常类型,请检查");
            return ajaxJsonEntity;
        }
        User user = UserUtils.getUser();
        if(user == null || user.getId() == null || user.getId()<=0){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("当前用户不存在,请重新登录");
            return ajaxJsonEntity;
        }
        abnormalForm.setCloseBy(user.getId());
        abnormalForm.setCloseAt(System.currentTimeMillis());
        try{
            abnormalFormService.closeAbnormalForm(abnormalForm,user);
        }catch (Exception e){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage(e.getMessage());
        }
        return ajaxJsonEntity;
    }


    /**
     * 好评驳回处理详情页面
     * @param
     * @return
     */
    @RequiresPermissions(value ="sd:abnormal:close")
    @RequestMapping("praiseInfoForKefu")
    public String praiseInfoForKefu(Long orderId,String quarter,Long servicePointId,Model model){
        model.addAttribute("canAction",true);
        ViewPraiseModel praiseModel = abnormalFormService.praiseInfoForKefu(orderId,quarter,servicePointId);
        if(praiseModel==null){
            addMessage(model,"读取好评费失败,请重新尝试!");
            model.addAttribute("canAction",false);
            return "modules/sd/abnormalForm/praiseInfoForKefuForm";
        }
        /*if(praiseModel.getStatus() != PraiseStatusEnum.REJECT.code){
            addMessage(model,"错误:好评单不是驳回!");
            model.addAttribute("canAction",false);
            return "modules/sd/abnormalForm/praiseInfoForKefuForm";
        }*/
        MDCustomerPraiseFee customerPraiseFee = customerPraiseFeeService.getByCustomerIdFromCacheNewForCP(praiseModel.getCustomerId());
        List<MDCustomerPraiseFeePraiseStandardItem> praiseList = Lists.newArrayList();
        if(customerPraiseFee==null){
            addMessage(model,"错误:获取客户好评配置错误！");
            model.addAttribute("canAction",false);
            return "modules/sd/abnormalForm/praiseInfoForKefuForm";

        }else{
            praiseList = customerPraiseFee.getPraiseStandardItems();
        }
        customerPraiseFee.setPraiseRequirement
                (org.apache.commons.lang3.StringUtils.replace(customerPraiseFee.getPraiseRequirement(),"\n","<br>"));
        List<PraiseLogModel> praiseLogModelList = orderPraiseService.finPraiseLogList(quarter,praiseModel.getId());
        if(praiseLogModelList==null){
            praiseLogModelList = Lists.newArrayList();
        }
        model.addAttribute("praise",praiseModel);
        model.addAttribute("customerPraiseFee",customerPraiseFee);
        model.addAttribute("praiseLogModelList",praiseLogModelList);
        model.addAttribute("praiseList",praiseList);
        return "modules/sd/abnormalForm/praiseInfoForKefuForm";
    }


    @RequiresPermissions(value ="sd:abnormal:close")
    @RequestMapping("updatePraiseForKefu")
    @ResponseBody
    public AjaxJsonEntity updatePraiseForKefu(Praise praise){
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        User user = UserUtils.getUser();
        if(praise.getStatus()!=PraiseStatusEnum.REJECT.code){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("好评单状态不是驳回状态");
            return ajaxJsonEntity;
        }
        if(user ==null || user.getId()==null){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("当前用户不存在,请重新登录");
            return ajaxJsonEntity;
        }
        try {
            MDCustomerPraiseFee customerPraiseFee = customerPraiseFeeService.getByCustomerIdFromCacheNewForCP(praise.getCustomerId());
            if(customerPraiseFee==null){
                ajaxJsonEntity.setSuccess(false);
                ajaxJsonEntity.setMessage("读取好评配置信息失败,请重试");
                return ajaxJsonEntity;
            }
            if(customerPraiseFee.getPraiseFeeFlag()==0){
                praise.setApplyCustomerPraiseFee(0.0);
                praise.setApplyServicepointPraiseFee(0.0);
            }else{
                NameValuePair<Double,Double> praiseFee = OrderPraiseService.getPraiseFee(praise,customerPraiseFee);
                if(!praiseFee.getName().equals(praise.getApplyCustomerPraiseFee())){
                    ajaxJsonEntity.setSuccess(false);
                    ajaxJsonEntity.setMessage("申请厂商好评费不准确!请确认");
                    return ajaxJsonEntity;
                }
                if(!praiseFee.getValue().equals(praise.getApplyServicepointPraiseFee())){
                    ajaxJsonEntity.setSuccess(false);
                    ajaxJsonEntity.setMessage("申请网点好评费不准确!请确认");
                    return ajaxJsonEntity;
                }
            }
            abnormalFormService.updatePraiseForKefu(praise,user);
        }catch (Exception e){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage(e.getMessage());
        }
        return ajaxJsonEntity;
    }

    /**
     * 网点取消好评单审核
     * @param praise
     */
    @RequiresPermissions(value ="sd:abnormal:close")
    @RequestMapping("cancelled")
    @ResponseBody
    public AjaxJsonEntity cancelledForKefu(Praise praise){
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        User user = UserUtils.getUser();
        if(user==null){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("当前用户不存在,请重新登陆");
            return ajaxJsonEntity;
        }
        try {
            abnormalFormService.cancelled(praise,user);
        }catch (Exception e){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage(e.getMessage());
        }
        return ajaxJsonEntity;
    }





}
