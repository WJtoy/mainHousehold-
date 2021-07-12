package com.wolfking.jeesite.ms.praise.controller;
import com.kkl.kklplus.entity.md.MDCustomerPraiseFee;
import com.kkl.kklplus.entity.praise.PraisePageSearchModel;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.praise.entity.ViewPraiseModel;
import com.wolfking.jeesite.ms.praise.service.CustomerPraiseService;
import com.wolfking.jeesite.ms.praise.service.OrderPraiseService;
import com.wolfking.jeesite.ms.providermd.service.MSCustomerPraiseFeeService;
import com.wolfking.jeesite.ms.providermd.service.MSCustomerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "${adminPath}/customer/praise/")
@Slf4j
public class CustomerPraiseController extends BaseController {


    @Autowired
    private CustomerPraiseService customerPraiseService;

    @Autowired
    private  MSCustomerService msCustomerService;

    @Autowired
    private MSCustomerPraiseFeeService msCustomerPraiseFeeService;


    private PraisePageSearchModel setSerachModel(PraisePageSearchModel praisePageSearchModel,User user,Model model){
        //检查客户帐号信息
        Integer reminderFlag = 1; //客户是否可以催单
        if (user.isCustomer()) {
            if (user.getCustomerAccountProfile() != null && user.getCustomerAccountProfile().getCustomer() != null) {
                //登录用户的客户，防篡改
                praisePageSearchModel.setCustomerId(user.getCustomerAccountProfile().getCustomer().getId());
                //登录用户的客户，防篡改
                if (praisePageSearchModel.getCustomerId()!=null && praisePageSearchModel.getCustomerId()>0) {
                    Customer customerFromMS = msCustomerService.get(praisePageSearchModel.getCustomerId());
                    if (customerFromMS != null) {
                        reminderFlag = customerFromMS.getReminderFlag();
                    }
                }
            }
            //客户账号负责的店铺
            List<String> shopIds = UserUtils.getShopIdsOfCustomerAccount(user);
            if(!CollectionUtils.isEmpty(shopIds)){
                praisePageSearchModel.setShopIds(shopIds);
            }
        }
        Date date;
        if (StringUtils.isBlank(praisePageSearchModel.getBeginDate())) {
            date = DateUtils.getDateEnd(new Date());
            praisePageSearchModel.setEndDate(DateUtils.formatDate(date,"yyyy-MM-dd"));
            praisePageSearchModel.setEndDt(date.getTime());
            date = DateUtils.getStartDayOfMonth(DateUtils.addMonth(new Date(), -1));
            praisePageSearchModel.setBeginDate(DateUtils.formatDate(date,"yyyy-MM-dd"));
            praisePageSearchModel.setBeginDt(date.getTime());
        } else {
            date = DateUtils.parseDate(praisePageSearchModel.getBeginDate());
            praisePageSearchModel.setBeginDt(date.getTime());
            date = DateUtils.parseDate(praisePageSearchModel.getEndDate());
            date = DateUtils.getDateEnd(date);
            praisePageSearchModel.setEndDt(date.getTime());
        }
        model.addAttribute("reminderFlag",reminderFlag);
        return praisePageSearchModel;
    }

    /**
     * 客服查询待处理好评信息列表
     * @param praisePageSearchModel
     * @param request
     */
    @RequiresPermissions("sd:order:view")
    @RequestMapping(value = "praiseList")
    public String praiseList(PraisePageSearchModel praisePageSearchModel, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<ViewPraiseModel> page = new Page(request, response);
        User user = UserUtils.getUser();
        if (user == null || user.getId() == null) {
            addMessage(model, "错误：登录超时，请退出后重新登录。");
            model.addAttribute("page", page);
            model.addAttribute("praisePageSearchModel", praisePageSearchModel);
            return "modules/sd/customerNew/praiseList";
        }
        praisePageSearchModel = setSerachModel(praisePageSearchModel,user,model);
        if(praisePageSearchModel.getCustomerId()!=null && praisePageSearchModel.getCustomerId()>0){
            MDCustomerPraiseFee customerPraiseFee = msCustomerPraiseFeeService.getByCustomerIdFromCacheForCP(praisePageSearchModel.getCustomerId());
            if(customerPraiseFee==null){
                //long beginDt = DateUtils.getDateStart(DateUtils.addDays(new Date(),-30)).getTime();
                long beginDt = DateUtils.getStartDayOfMonth(new Date()).getTime();
                Integer praiseCount = customerPraiseService.praiseCount(beginDt);
                model.addAttribute("praiseCount",praiseCount);
                model.addAttribute("praisePageSearchModel", praisePageSearchModel);
                return "modules/sd/customerNew/praiseGuide";
            }
        }
        page = customerPraiseService.waitProcessList(page,praisePageSearchModel);
        model.addAttribute("page", page);
        model.addAttribute("praisePageSearchModel", praisePageSearchModel);
        return "modules/sd/customerNew/praiseList";
    }

    /**
     * 客服订单详情查看查看好评单
     * @param orderId
     * @param quarter
     */
    @RequestMapping(value = "ajax/getPraiseForCustomer")
    @ResponseBody
    public AjaxJsonEntity getPraiseForCustomer(Long orderId,String quarter,Long servicePointId){
          AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
          try {
              ViewPraiseModel praiseModel = customerPraiseService.getPraiseForCustomer(orderId,quarter,servicePointId);
              ajaxJsonEntity.setData(praiseModel);
          }catch (Exception e){
              ajaxJsonEntity.setSuccess(false);
              ajaxJsonEntity.setMessage(e.getMessage());
          }
          return ajaxJsonEntity;
    }

}
