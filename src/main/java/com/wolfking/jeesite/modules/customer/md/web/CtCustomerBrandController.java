package com.wolfking.jeesite.modules.customer.md.web;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.md.CustomerBrand;
import com.kkl.kklplus.entity.md.CustomerBrandCategory;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.customer.md.service.CtCustomerBrandCategoryService;
import com.wolfking.jeesite.modules.customer.md.service.CtCustomerBrandService;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.utils.CustomerUtils;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providermd.service.MSCustomerService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
@RequestMapping(value = "${adminPath}/customer/md/customerBrand")
public class CtCustomerBrandController extends BaseController {

    @Autowired
    private CtCustomerBrandService ctCustomerBrandService;

    @Autowired
    private MSCustomerService msCustomerService;

    @Autowired
    private CtCustomerBrandCategoryService ctCustomerBrandCategoryService;

    /**
     * 分页查询
     *
     * @param customerBrand
     * @return
     */
    @RequiresPermissions("customer:md:customerbrand:view")
    @RequestMapping(value = {"getList", ""})
    public String getList(CustomerBrand customerBrand, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<CustomerBrand> page = new Page<>(request, response);
        User user = UserUtils.getUser();
        Boolean erroFlag = false;
        if(user.isCustomer()){
            if (user.getCustomerAccountProfile() != null && user.getCustomerAccountProfile().getCustomer() != null) {
                //登录用户的客户，防篡改
                customerBrand.setCustomerId(user.getCustomerAccountProfile().getCustomer().getId());
                customerBrand.setCustomerName(user.getCustomerAccountProfile().getCustomer().getName());
            } else {
                addMessage(model, "错误：登录超时，请退出后重新登录。");
                erroFlag = true;
            }
        }else if(user.isSaleman() && (customerBrand.getCustomerId()==null || customerBrand.getCustomerId()<=0)){
            List<Customer> customers = CustomerUtils.getMyCustomerList();
            if(customers !=null && customers.size()>0){
                customerBrand.setCustomerId(customers.get(0).getId());
            }else{
                customerBrand.setCustomerId(0L);
            }
        }
        if(erroFlag){
            model.addAttribute("page", page);
            model.addAttribute("customerBrand", customerBrand);
            return "modules/customer/md/ctCustomerBrandList";
        }
        if(customerBrand.getCustomerId() != null && customerBrand.getCustomerId() != 0) {
            page = ctCustomerBrandService.getList(new Page<CustomerBrand>(request, response), customerBrand);
            List<CustomerBrand> list = page.getList();
            for (CustomerBrand entity : list) {
                Customer customer = msCustomerService.getFromCache(entity.getCustomerId());
                entity.setCustomerName(customer != null && StringUtils.isNotBlank(customer.getName()) ? customer.getName() : "");
            }
        }

        model.addAttribute("page", page);
        model.addAttribute("customerBrand", customerBrand);
        return "modules/customer/md/ctCustomerBrandList";
    }

    @RequiresPermissions("customer:md:customerbrand:edit")
    @RequestMapping(value = "form")
    public String form(CustomerBrand customerBrand, Model model) {
        User user = UserUtils.getUser();
        if(customerBrand.getId()!=null && customerBrand.getId()>0){
            customerBrand = ctCustomerBrandService.getById(customerBrand.getId());
            customerBrand.setCustomerName(msCustomerService.getFromCache(customerBrand.getCustomerId()).getName());
        }
        if(user.isCustomer()){
            if (user.getCustomerAccountProfile() != null && user.getCustomerAccountProfile().getCustomer() != null) {
                //登录用户的客户，防篡改
                customerBrand.setCustomerId(user.getCustomerAccountProfile().getCustomer().getId());
            } else {
                addMessage(model, "错误：登录超时，请退出后重新登录。");
                model.addAttribute("canAction", false);
                model.addAttribute("customerBrand", customerBrand);
                return "modules/customer/md/ctCustomerBrandForm";
            }
        }
        model.addAttribute("canAction", true);
        model.addAttribute("customerBrand", customerBrand);
        return "modules/customer/md/ctCustomerBrandForm";
    }

    /**
     * 保存数据(添加或修改)
     * @param customerBrand
     * @return
     */
    @ResponseBody
    @RequiresPermissions("customer:md:customerbrand:edit")
    @RequestMapping("ajaxSave")
    public AjaxJsonEntity ajaxSave(CustomerBrand customerBrand, Model model) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        if (!beanValidator(model, customerBrand)) {
            ajaxJsonEntity.setSuccess(false);
            return ajaxJsonEntity;
        }
        try {
            User user = UserUtils.getUser();
            if (user.getId() != null) {
                customerBrand.setCreateById(user.getId());
                customerBrand.setUpdateById(user.getId());
                MSErrorCode mSResponse = ctCustomerBrandService.save(customerBrand);
                if (mSResponse.getCode() == 0) {
                    ajaxJsonEntity.setMessage("保存成功");
                } else {
                    ajaxJsonEntity.setSuccess(false);
                    ajaxJsonEntity.setMessage(mSResponse.getMsg());
                }
            } else {
                ajaxJsonEntity.setSuccess(false);
                ajaxJsonEntity.setMessage("当前用户不存在");
            }
            return ajaxJsonEntity;
        }catch (Exception ex){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage(ex.getMessage());
            return ajaxJsonEntity;
        }

    }

    /**
     * 删除
     * @param entity
     * @return
     */
    @RequiresPermissions("customer:md:customerbrand:edit")
    @RequestMapping(value = "delete")
    public String delete(CustomerBrand entity, RedirectAttributes redirectAttributes) {
        List<CustomerBrandCategory> list = ctCustomerBrandCategoryService.findListByBrand(entity.getCustomerId(),entity.getId());
        if(list!=null && list.size()>0){
            addMessage(redirectAttributes, "该品牌已经绑定产品品牌,不允许删除");
            return "redirect:" + adminPath + "/customer/md/customerBrand/getList?repage";
        }
        MSResponse<Integer> msResponse = ctCustomerBrandService.delete(entity);
        if (msResponse.getCode() == 0) {
            addMessage(redirectAttributes, "删除成功");
        } else {
            addMessage(redirectAttributes, msResponse.getMsg());
        }
        return "redirect:" + adminPath + "/customer/md/customerBrand/getList?repage";
    }
}
