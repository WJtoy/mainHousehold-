package com.wolfking.jeesite.ms.providermd.controller;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.md.CustomerBrand;
import com.kkl.kklplus.entity.md.CustomerBrandCategory;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.service.CustomerService;
import com.wolfking.jeesite.modules.md.utils.CustomerUtils;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providermd.service.CustomerBrandCategoryService;
import com.wolfking.jeesite.ms.providermd.service.CustomerBrandService;
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

/**
 * 客户产品型号服务
 */
@Controller
@RequestMapping(value = "${adminPath}/provider/md/customerBrand")
public class CustomerBrandController extends BaseController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerBrandService customerBrandService;

    @Autowired
    private CustomerBrandCategoryService customerBrandCategoryService;

    /**
     * 分页查询
     *
     * @param customerBrand
     * @return
     */
    @RequiresPermissions("md:customerbrand:view")
    @RequestMapping(value = {"getList", ""})
    public String getList(CustomerBrand customerBrand, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<CustomerBrand> page = new Page<>(request, response);
        User user = UserUtils.getUser();
        Boolean erroFlag = false;
        if(user.isCustomer()){
            if (user.getCustomerAccountProfile() != null && user.getCustomerAccountProfile().getCustomer() != null) {
                //登录用户的客户，防篡改
                customerBrand.setCustomerId(user.getCustomerAccountProfile().getCustomer().getId());
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
            return "modules/providermd/customerBrandNewList";
        }
        if(customerBrand.getCustomerId() != null && customerBrand.getCustomerId() != 0){
            page = customerBrandService.getList(new Page<CustomerBrand>(request, response), customerBrand);
            List<CustomerBrand> list = page.getList();
            for(CustomerBrand entity:list){
                Customer customer = customerService.getFromCache(entity.getCustomerId());
                entity.setCustomerName(customer!=null&&StringUtils.isNotBlank(customer.getName())?customer.getName():"");
            }
        }

        model.addAttribute("page", page);
        model.addAttribute("customerBrand", customerBrand);
        return "modules/providermd/customerBrandNewList";
    }

    @RequiresPermissions("md:customerbrand:edit")
    @RequestMapping(value = "form")
    public String form(CustomerBrand customerBrand, Model model) {
        User user = UserUtils.getUser();
        if(customerBrand.getId()!=null && customerBrand.getId()>0){
            customerBrand = customerBrandService.getById(customerBrand.getId());
            customerBrand.setCustomerName(customerService.getFromCache(customerBrand.getCustomerId()).getName());
        }
        if(user.isCustomer()){
            if (user.getCustomerAccountProfile() != null && user.getCustomerAccountProfile().getCustomer() != null) {
                //登录用户的客户，防篡改
                customerBrand.setCustomerId(user.getCustomerAccountProfile().getCustomer().getId());
            } else {
                addMessage(model, "错误：登录超时，请退出后重新登录。");
                model.addAttribute("canAction", false);
                model.addAttribute("customerBrand", customerBrand);
                return "modules/providermd/customerBrandNewForm";
            }
        }
        model.addAttribute("canAction", true);
        model.addAttribute("customerBrand", customerBrand);
        return "modules/providermd/customerBrandNewForm";
    }


    /**
     * 保存数据(添加或修改)
     * @param customerBrand
     * @return
     */
    @RequiresPermissions("md:customerbrand:edit")
    @RequestMapping("save")
    public String save(CustomerBrand customerBrand, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        if (!beanValidator(model, customerBrand)) {
            return form(customerBrand, model);
        }
        User user = UserUtils.getUser();
        if (user.getId() != null) {
            customerBrand.setCreateById(user.getId());
            customerBrand.setUpdateById(user.getId());
            MSErrorCode mSResponse = customerBrandService.save(customerBrand);
            if (mSResponse.getCode() == 0) {
                addMessage(redirectAttributes, "保存成功");
            } else {
                addMessage(redirectAttributes, mSResponse.getMsg());
            }
        } else {
            addMessage(redirectAttributes, "当前用户不存在");
        }
        if(user.isSaleman()){
            return "redirect:" + adminPath + "/provider/md/customerBrand/getList?repage&customerId=" + customerBrand.getCustomerId();
        }
        return "redirect:" + adminPath + "/provider/md/customerBrand/getList?repage";
    }

    /**
     * 保存数据(添加或修改)
     * @param customerBrand
     * @return
     */
    @ResponseBody
    @RequiresPermissions("md:customerbrand:edit")
    @RequestMapping("ajaxSave")
    public AjaxJsonEntity ajaxSave(CustomerBrand customerBrand,Model model) {
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
                MSErrorCode mSResponse = customerBrandService.save(customerBrand);
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
    @RequiresPermissions("md:customerbrand:edit")
    @RequestMapping(value = "delete")
    public String delete(CustomerBrand entity, RedirectAttributes redirectAttributes) {
        List<CustomerBrandCategory> list = customerBrandCategoryService.findListByBrand(entity.getCustomerId(),entity.getId());
        if(list!=null && list.size()>0){
            addMessage(redirectAttributes, "该品牌已经绑定产品品牌,不允许删除");
            return "redirect:" + adminPath + "/provider/md/customerBrand/getList?repage";
        }
        MSResponse<Integer> msResponse = customerBrandService.delete(entity);
        if (msResponse.getCode() == 0) {
            addMessage(redirectAttributes, "删除成功");
        } else {
            addMessage(redirectAttributes, msResponse.getMsg());
        }
        return "redirect:" + adminPath + "/provider/md/customerBrand/getList?repage";
    }

}
