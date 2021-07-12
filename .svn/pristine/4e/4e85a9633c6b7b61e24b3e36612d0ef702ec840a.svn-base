package com.wolfking.jeesite.modules.md.web;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.md.B2BCustomerMapping;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.entity.common.NameValuePair;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.kkl.kklplus.utils.SequenceIdUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.utils.CustomerUtils;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.b2bcenter.md.service.B2BCustomerMappingService;
import com.wolfking.jeesite.ms.common.config.MicroServicesProperties;
import com.wolfking.jeesite.ms.providermd.service.MSCustomerShopService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.text.html.Option;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;


@Controller
@RequestMapping(value = "${adminPath}/md/customerShop/")
public class CustomerShopController extends BaseController {

    @Autowired
    private B2BCustomerMappingService b2BCustomerMappingService;

    @Autowired
    private MicroServicesProperties msProperties;

    @Autowired
    private MSCustomerShopService customerShopService;


    /**
     * 分页查询
     *
     * @param b2BCustomerMapping
     * @return
     */
    @RequiresPermissions("md:customershop:view")
    @RequestMapping(value = {"getList", ""})
    public String getList(B2BCustomerMapping b2BCustomerMapping, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<B2BCustomerMapping> page = new Page<>(request, response);
        if (msProperties.getB2bcenter().getEnabled()) {
            User user = UserUtils.getUser();
            Boolean errorFlag = false;
            if(user.isCustomer()){
                if (user.getCustomerAccountProfile() != null && user.getCustomerAccountProfile().getCustomer() != null) {
                    //登录用户的客户，防篡改
                    b2BCustomerMapping.setCustomerId(user.getCustomerAccountProfile().getCustomer().getId());
                } else {
                    addMessage(model, "错误：登录超时，请退出后重新登录。");
                    errorFlag = true;
                }
            }else if(user.isSaleman()){
                if(b2BCustomerMapping.getCustomerId()==null || b2BCustomerMapping.getCustomerId()<=0){
                    errorFlag = true;
                }
            }
            if(errorFlag){
                model.addAttribute("page", page);
                model.addAttribute("b2BCustomerMapping", b2BCustomerMapping);
                return "modules/md/customerShopList";
            }
            b2BCustomerMapping.setDataSource(B2BDataSourceEnum.KKL.id);
//            page = b2BCustomerMappingService.getList(page, b2BCustomerMapping);
            page = customerShopService.findList(page, b2BCustomerMapping);
            List<B2BCustomerMapping> customerMappingList = page.getList();
//            Map<Long,Customer> map = CustomerUtils.getAllCustomerMap();
//            Customer customer;
//            for (B2BCustomerMapping entity : customerMappingList) {
//                customer = map.get(entity.getCustomerId());
//                if (customer != null) {
//                    entity.setCustomerName(customer.getName());
//                }
//            }

            // 调整批量分页优化获取
            List<Long> customerIds = page.getList().stream().distinct().map(p -> p.getCustomerId()).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(customerIds)) {
                Map<Long, String> map = CustomerUtils.findAllCustomerMap(customerIds);
                for (B2BCustomerMapping entity : customerMappingList) {
                    entity.setCustomerName(Optional.ofNullable(map.get(entity.getCustomerId())).orElse(""));
                }
            }
        } else {
            addMessage(model, MSErrorCode.MICROSERVICE_DISABLED.msg);
        }
        model.addAttribute("page", page);
        model.addAttribute("b2BCustomerMapping", b2BCustomerMapping);
        return "modules/md/customerShopList";
    }

    @RequiresPermissions("md:customershop:view")
    @RequestMapping(value = "form")
    public String form(B2BCustomerMapping b2BCustomerMapping, Model model) {
        if (msProperties.getB2bcenter().getEnabled()) {
            if (b2BCustomerMapping.getId() != null && b2BCustomerMapping.getId() > 0) {
                MSResponse<B2BCustomerMapping> msResponse = b2BCustomerMappingService.getById(b2BCustomerMapping.getId());
                if(MSResponse.isSuccess(msResponse)){
                    b2BCustomerMapping = msResponse.getData();
                }
            }
            User user = UserUtils.getUser();
            if(user.isCustomer()){
                if (user.getCustomerAccountProfile() != null && user.getCustomerAccountProfile().getCustomer() != null) {
                    //登录用户的客户，防篡改
                    Customer customer = user.getCustomerAccountProfile().getCustomer();
                    b2BCustomerMapping.setCustomerId(customer.getId());
                    b2BCustomerMapping.setCustomerName(customer.getName());
                } else {
                    addMessage(model, "错误：登录超时，请退出后重新登录。");
                    model.addAttribute("b2BCustomerMapping", b2BCustomerMapping);
                    return "modules/md/customerShopForm";
                }
            }
        } else {
            addMessage(model, MSErrorCode.MICROSERVICE_DISABLED.msg);
        }
        model.addAttribute("b2BCustomerMapping", b2BCustomerMapping);
        return "modules/md/customerShopForm";
    }


    /**
     * 添加或者修改数据
     *
     * @param b2BCustomerMapping
     * @return
     */
    @RequiresPermissions("md:customershop:edit")
    @RequestMapping("save")
    @ResponseBody
    public AjaxJsonEntity save(B2BCustomerMapping b2BCustomerMapping, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity();
        if (msProperties.getB2bcenter().getEnabled()) {
            if (!beanValidator(model, b2BCustomerMapping)) {
//                return form(b2BCustomerMapping, model);
                ajaxJsonEntity.setSuccess(false);
                return ajaxJsonEntity;
            }
            User user = UserUtils.getUser();
            if (user.getId() != null) {
                b2BCustomerMapping.setCreateById(user.getId());
                b2BCustomerMapping.setUpdateById(user.getId());
                if(b2BCustomerMapping.getId()==null || b2BCustomerMapping.getId()<=0){
                    String shopId = String.valueOf(new SequenceIdUtils(ThreadLocalRandom.current().nextInt(32), ThreadLocalRandom.current().nextInt(32)).nextId());
                    b2BCustomerMapping.setShopId(shopId);
                }
                b2BCustomerMapping.setShopName(StringUtils.trim(b2BCustomerMapping.getShopName()));
                MSErrorCode mSResponse = b2BCustomerMappingService.save(b2BCustomerMapping);
                if (mSResponse.getCode() == 0) {
//                    addMessage(redirectAttributes, "保存成功");
                    ajaxJsonEntity.setSuccess(true);
                    ajaxJsonEntity.setMessage("保存成功");
                } else {
                    /*model.addAttribute("message",mSResponse.getMsg());
                    return form(b2BCustomerMapping,model);*/
                    ajaxJsonEntity.setSuccess(false);
                    ajaxJsonEntity.setMessage(mSResponse.getMsg());
                    return ajaxJsonEntity;
                }
            } else {
                /*model.addAttribute("message","当前用户不存在");
                return form(b2BCustomerMapping,model);*/
                ajaxJsonEntity.setSuccess(false);
                ajaxJsonEntity.setMessage("当前用户不存在");
            }
        } else {
//            addMessage(redirectAttributes, MSErrorCode.MICROSERVICE_DISABLED.msg);
            ajaxJsonEntity.setMessage(MSErrorCode.MICROSERVICE_DISABLED.msg);
        }
        /*User user = UserUtils.getUser();
        if(user.isSaleman()){
            return "redirect:" + adminPath + "/md/customerShop/getList?repage&customerId=" + b2BCustomerMapping.getCustomerId();
        }else{
            return "redirect:" + adminPath + "/md/customerShop/getList?repage";
        }*/
        return ajaxJsonEntity;
    }

    /**
     * 删除数据
     *
     * @param entity
     * @return
     */
    /*@RequiresPermissions("md:customershop:edit")
    @RequestMapping(value = "delete")
    public String delete(B2BCustomerMapping entity, RedirectAttributes redirectAttributes) {
        if (msProperties.getB2bcenter().getEnabled()) {
            MSResponse<Integer> msResponse = b2BCustomerMappingService.delete(entity);
            if (msResponse.getCode() == 0) {
                addMessage(redirectAttributes, "删除成功");
            } else {
                addMessage(redirectAttributes, msResponse.getMsg());
            }
        } else {
            addMessage(redirectAttributes, MSErrorCode.MICROSERVICE_DISABLED.msg);
        }
        User user = UserUtils.getUser();
        if(user.isSaleman()){
            if(entity.getCustomerId()!=null && entity.getCustomerId()>0){
                return "redirect:" + adminPath + "/md/customerShop/getList?repage&customerId=" + entity.getCustomerId();
            }
        }
        return "redirect:" + adminPath + "/md/customerShop/getList?repage";
    }*/

    @RequiresPermissions("md:customershop:edit")
    @RequestMapping(value = "delete")
    @ResponseBody
    public AjaxJsonEntity delete(B2BCustomerMapping entity, RedirectAttributes redirectAttributes) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity();
        if (msProperties.getB2bcenter().getEnabled()) {
            MSResponse<Integer> msResponse = b2BCustomerMappingService.delete(entity);
            if (msResponse.getCode() == 0) {
                ajaxJsonEntity.setSuccess(true);
                ajaxJsonEntity.setMessage("删除成功");
            } else {
                ajaxJsonEntity.setMessage(msResponse.getMsg());
            }
        } else {
            ajaxJsonEntity.setMessage(MSErrorCode.MICROSERVICE_DISABLED.msg);
        }
        return ajaxJsonEntity;
    }
}
