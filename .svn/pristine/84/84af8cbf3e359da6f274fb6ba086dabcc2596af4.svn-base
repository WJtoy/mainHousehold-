package com.wolfking.jeesite.ms.b2bcenter.md.web;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.entity.b2bcenter.md.B2BCustomerCategory;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.b2bcenter.md.service.B2BCustomerCategoryService;
import com.wolfking.jeesite.ms.common.config.MicroServicesProperties;
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
@RequestMapping(value = "${adminPath}/b2bcenter/md/customerCategory/")
public class B2BCustomerCategoryController extends BaseController {

    @Autowired
    private B2BCustomerCategoryService customerCategoryService;

    @Autowired
    private MicroServicesProperties msProperties;


    /**
     * 分页查询
     *
     * @param customerCategory
     * @return
     */
    @RequiresPermissions("md:b2bcustomercategory:view")
    @RequestMapping(value = {"getList", ""})
    public String getList(B2BCustomerCategory customerCategory, HttpServletRequest request, HttpServletResponse response, Model model){
        Page<B2BCustomerCategory> page = new Page<>(request, response);
        if (msProperties.getB2bcenter().getEnabled()) {
            page = customerCategoryService.getList(new Page<>(request, response), customerCategory);
        } else {
            addMessage(model, MSErrorCode.MICROSERVICE_DISABLED.msg);
        }
        model.addAttribute("page", page);
        model.addAttribute("customerCategory", customerCategory);
        return "modules/b2bcenter/md/b2bCustomerCategoryList";
    }


    /**
     * 跳转添加页面
     *
     * @param customerCategory
     * @return
     */
    @RequiresPermissions("md:b2bcustomercategory:view")
    @RequestMapping(value = "form")
    public String form(B2BCustomerCategory customerCategory, Model model) {
        if (!msProperties.getB2bcenter().getEnabled()) {
            addMessage(model, MSErrorCode.MICROSERVICE_DISABLED.msg);
        }
        model.addAttribute("customerCategory", customerCategory);
        return "modules/b2bcenter/md/b2bCustomerCategoryForm";
    }


    /**
     * 保存数据
     *
     * @param customerCategory
     * @return
     */
    @RequiresPermissions("md:b2bcustomercategory:edit")
    @RequestMapping("save")
    public String save(B2BCustomerCategory customerCategory, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        if (msProperties.getB2bcenter().getEnabled()) {
            if (!beanValidator(model, customerCategory)) {
                return form(customerCategory, model);
            }
            User user = UserUtils.getUser();
            if (user.getId() != null) {
                customerCategory.setCreateById(user.getId());
                customerCategory.setUpdateById(user.getId());
                try {
                    customerCategoryService.save(customerCategory);
                    addMessage(redirectAttributes, "保存成功");
                }catch (Exception e){
                    model.addAttribute("message", e.getMessage());
                    return form(customerCategory, model);
                }
            } else {
                addMessage(redirectAttributes, "当前用户不存在");
                return form(customerCategory, model);
            }
        } else {
            addMessage(redirectAttributes, MSErrorCode.MICROSERVICE_DISABLED.msg);
        }
        return "redirect:" + adminPath + "/b2bcenter/md/customerCategory/getList?repage";
    }


    /**
     * 删除
     *
     * @param customerCategory
     * @return
     */
    @RequiresPermissions("md:b2bcustomercategory:edit")
    @RequestMapping(value = "delete")
    public String delete(B2BCustomerCategory customerCategory,Model model,RedirectAttributes redirectAttributes){
        if (msProperties.getB2bcenter().getEnabled()) {
            User user = UserUtils.getUser();
            if(user!=null){
                customerCategory.setUpdateById(user.getId());
                try{
                    customerCategoryService.delete(customerCategory);
                    addMessage(redirectAttributes, "删除成功");
                }catch (Exception e){
                    model.addAttribute("message", e.getMessage());
                }
            }else{
                addMessage(redirectAttributes, "当前用户不存在");
            }
        } else {
            addMessage(redirectAttributes, MSErrorCode.MICROSERVICE_DISABLED.msg);
        }
        return "redirect:" + adminPath + "/b2bcenter/md/customerCategory/getList?repage";
    }

    /**
     * 根据数据源查找
     *
     * @param dataSource
     * @return
     */
    @RequestMapping("ajax/getListByDataSource")
    @ResponseBody
    public AjaxJsonEntity getListByDataSource(Integer dataSource,HttpServletResponse response){
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity jsonEntity = new AjaxJsonEntity(true);
        if(dataSource ==null && dataSource>0){
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage("数据源为空");
        }
        try {
            List<B2BCustomerCategory> list = customerCategoryService.getListByDataSource(dataSource);
            jsonEntity.setSuccess(true);
            jsonEntity.setData(list);
        }catch (Exception e){
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage(e.getMessage());
        }
        return  jsonEntity;
    }

}
