package com.wolfking.jeesite.modules.servicepoint.receipt.web;

import com.google.common.collect.Lists;
import com.kkl.kklplus.entity.cc.AbnormalFormEnum;
import com.kkl.kklplus.entity.cc.vm.AbnormalFormSearchModel;
import com.kkl.kklplus.entity.md.MDCustomerPraiseFee;
import com.kkl.kklplus.entity.md.MDCustomerPraiseFeePraiseStandardItem;
import com.kkl.kklplus.entity.praise.PraiseStatusEnum;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.Engineer;
import com.wolfking.jeesite.modules.md.service.ServicePointService;
import com.wolfking.jeesite.modules.servicepoint.receipt.service.ServicePointAbnormalService;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.cc.entity.AbnormalFormModel;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

/**
 * 异常单控制器
 */
@Controller
@RequestMapping(value = "${adminPath}/servicePoint/receipt/abnormalForm")
@Slf4j
public class ServicePointAbnormalController extends BaseController {

    @Autowired
    private ServicePointAbnormalService servicePointAbnormalService;

    @Autowired
    private ServicePointService servicePointService;

    @Autowired
    private MSCustomerPraiseFeeService customerPraiseFeeService;

    @Autowired
    private OrderPraiseService orderPraiseService;




    private AbnormalFormSearchModel setSerachModel(AbnormalFormSearchModel abnormalFormSearchModel,User currentUser){
        if (currentUser.isEngineer()) {
            Engineer engineer = null;
            if (currentUser.getCompanyId() > 0) {
                engineer = servicePointService.getEngineerFromCache(currentUser.getCompanyId(), currentUser.getEngineerId());
            }
            if (engineer == null) {
                engineer = servicePointService.getEngineer(currentUser.getEngineerId());
            }
            Long servicePointId = (engineer.getServicePoint() == null ? null : engineer.getServicePoint().getId());
            abnormalFormSearchModel.setServicepointId(servicePointId);
      /*      if (engineer.getMasterFlag() == 1) {
                praisePageSearchModel.setEngineerId(null);
                if(servicePointId != null) {
                    List<Engineer> engineers = servicePointService.getEngineerListOfServicePoint(servicePointId);
                    searchModel.setEngineerList(engineers);
                }
            } else {
                praisePageSearchModel.set
            }*/
        }
        Date date;
        if (StringUtils.isBlank(abnormalFormSearchModel.getBeginDate())) {
            date = DateUtils.getDateEnd(new Date());
            abnormalFormSearchModel.setEndDate(DateUtils.formatDate(date,"yyyy-MM-dd"));
            abnormalFormSearchModel.setEndDt(date.getTime());
            date = DateUtils.getStartDayOfMonth(DateUtils.addMonth(new Date(), -1));
            abnormalFormSearchModel.setBeginDate(DateUtils.formatDate(date,"yyyy-MM-dd"));
            abnormalFormSearchModel.setBeginDt(date.getTime());
        } else {
            date = DateUtils.parseDate(abnormalFormSearchModel.getBeginDate());
            abnormalFormSearchModel.setBeginDt(date.getTime());
            date = DateUtils.parseDate(abnormalFormSearchModel.getEndDate());
            date = DateUtils.getDateEnd(date);
            abnormalFormSearchModel.setEndDt(date.getTime());
        }
        return abnormalFormSearchModel;
    }

    /**
     * 好评单异常列表
     * @param abnormalFormSearchModel
     * @return
     */
    @RequiresPermissions(value ="sd:servicepointabnormal:view")
    @RequestMapping("praiseAbnormalList")
    public String praiseAbnormalList(AbnormalFormSearchModel abnormalFormSearchModel, HttpServletRequest request, HttpServletResponse response, Model model){
        Page<AbnormalFormModel> page = new Page(request, response);
        User user = UserUtils.getUser();
        if (user == null || user.getId() == null) {
            addMessage(model, "错误：登录超时，请退出后重新登录。");
            model.addAttribute("page", page);
            model.addAttribute("abnormalFormSearchModel", abnormalFormSearchModel);
            return "modules/sd/abnormalForm/servicePoint/praiseAbnormalList";
        }
        abnormalFormSearchModel = setSerachModel(abnormalFormSearchModel,user);
        abnormalFormSearchModel.setFormType(AbnormalFormEnum.FormType.PRAISE_ABNORMAL.getCode());
        page = servicePointAbnormalService.praiseAbnormalList(page,abnormalFormSearchModel);
        model.addAttribute("page", page);
        model.addAttribute("abnormalForm", abnormalFormSearchModel);
        return "modules/servicePoint/receipt/abnormal/praiseAbnormalList";
    }

    /**
     * 网点好评驳回处理页面
     * @param
     * @return
     */
    @RequestMapping("praiseHandleForm")
    public String praiseHandleForm(Long orderId,String quarter,Long servicePointId,Model model){
        model.addAttribute("canAction",true);
        ViewPraiseModel praiseModel = servicePointAbnormalService.praiseInfoForServicePoint(orderId,quarter,servicePointId);
        if(praiseModel==null){
            addMessage(model,"读取好评费失败,请重新尝试!");
            model.addAttribute("canAction",false);
            return "modules/servicePoint/receipt/abnormal/praiseHandleForm";
        }
        /*if(praiseModel.getStatus() != PraiseStatusEnum.REJECT.code){
            addMessage(model,"错误:好评单不是驳回!");
            model.addAttribute("canAction",false);
            return "modules/servicePoint/receipt/abnormal/praiseHandleForm";
        }*/
        MDCustomerPraiseFee customerPraiseFee = customerPraiseFeeService.getByCustomerIdFromCacheNewForCP(praiseModel.getCustomerId());
        List<MDCustomerPraiseFeePraiseStandardItem> praiseList = Lists.newArrayList();
        if(customerPraiseFee==null){
            addMessage(model,"错误:获取客户好评配置错误！");
            model.addAttribute("canAction",false);
            return "modules/servicePoint/receipt/abnormal/praiseHandleForm";

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
        return "modules/servicePoint/receipt/abnormal/praiseHandleForm";
    }

}
