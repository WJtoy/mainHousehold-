package com.wolfking.jeesite.ms.b2bcenter.md.web;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.entity.b2bcenter.md.B2BCustomerCategory;
import com.kkl.kklplus.entity.b2bcenter.md.B2BServiceFeeCategory;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.b2bcenter.md.service.B2BCustomerCategoryService;
import com.wolfking.jeesite.ms.b2bcenter.md.service.B2BServiceFeeCategoryService;
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
@RequestMapping(value = "${adminPath}/b2bcenter/md/serviceFeeCategory/")
public class B2BServiceFeeCategoryController extends BaseController {

    @Autowired
    private B2BServiceFeeCategoryService serviceFeeCategoryService;

    @Autowired
    private MicroServicesProperties msProperties;


    /**
     * 分页查询
     *
     * @param serviceFeeCategory
     * @return
     */
    @RequiresPermissions("md:b2bservicefeecategory:view")
    @RequestMapping(value = {"getList", ""})
    public String getList(B2BServiceFeeCategory serviceFeeCategory, HttpServletRequest request, HttpServletResponse response, Model model){
        Page<B2BServiceFeeCategory> page = new Page<>(request, response);
        if (msProperties.getB2bcenter().getEnabled()) {
            page = serviceFeeCategoryService.getList(new Page<>(request, response), serviceFeeCategory);
        } else {
            addMessage(model, MSErrorCode.MICROSERVICE_DISABLED.msg);
        }
        model.addAttribute("page", page);
        model.addAttribute("serviceFeeCategory", serviceFeeCategory);
        return "modules/b2bcenter/md/b2bServiceFeeCategoryList";
    }


    /**
     * 跳转添加页面
     *
     * @param serviceFeeCategory
     * @return
     */
    @RequiresPermissions("md:b2bservicefeecategory:view")
    @RequestMapping(value = "form")
    public String form(B2BServiceFeeCategory serviceFeeCategory, Model model) {
        if (!msProperties.getB2bcenter().getEnabled()) {
            addMessage(model, MSErrorCode.MICROSERVICE_DISABLED.msg);
        }
        if(serviceFeeCategory.getId()!=null && serviceFeeCategory.getId()>0){
            serviceFeeCategory = serviceFeeCategoryService.get(serviceFeeCategory.getId());
        }
        model.addAttribute("serviceFeeCategory", serviceFeeCategory);
        return "modules/b2bcenter/md/b2bServiceFeeCategoryForm";
    }


    /**
     * 保存数据
     *
     * @param serviceFeeCategory
     * @return
     */
    @RequiresPermissions("md:b2bservicefeecategory:edit")
    @RequestMapping("save")
    public String save(B2BServiceFeeCategory serviceFeeCategory, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        if (msProperties.getB2bcenter().getEnabled()) {
            if (!beanValidator(model, serviceFeeCategory)) {
                return form(serviceFeeCategory, model);
            }
            User user = UserUtils.getUser();
            if (user.getId() != null) {
                serviceFeeCategory.setCreateById(user.getId());
                serviceFeeCategory.setUpdateById(user.getId());
                try {
                    serviceFeeCategoryService.save(serviceFeeCategory);
                    addMessage(redirectAttributes, "保存成功");
                }catch (Exception e){
                    model.addAttribute("message", e.getMessage());
                    if(serviceFeeCategory.getId()==null || serviceFeeCategory.getId()<=0){
                        serviceFeeCategory = new B2BServiceFeeCategory();
                    }
                    model.addAttribute("serviceFeeCategory", serviceFeeCategory);
                    return "modules/b2bcenter/md/b2bServiceFeeCategoryForm";
                }
            } else {
                addMessage(redirectAttributes, "当前用户不存在");
                return form(serviceFeeCategory, model);
            }
        } else {
            addMessage(redirectAttributes, MSErrorCode.MICROSERVICE_DISABLED.msg);
        }
        return "redirect:" + adminPath + "/b2bcenter/md/serviceFeeCategory/getList?repage";
    }


    /**
     * 删除
     *
     * @param serviceFeeCategory
     * @return
     */
    @RequiresPermissions("md:b2bservicefeecategory:edit")
    @RequestMapping(value = "delete")
    public String delete(B2BServiceFeeCategory serviceFeeCategory,Model model,RedirectAttributes redirectAttributes){
        if (msProperties.getB2bcenter().getEnabled()) {
            User user = UserUtils.getUser();
            if(user!=null){
                serviceFeeCategory.setUpdateById(user.getId());
                try{
                    serviceFeeCategoryService.delete(serviceFeeCategory);
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
        return "redirect:" + adminPath + "/b2bcenter/md/serviceFeeCategory/getList?repage";
    }

    /**
     * 根据数据源获取服务费项目分类
     * @param dataSource
     * @return
     */
    @RequestMapping("ajax/getServiceFeeCategory")
    @ResponseBody
    public AjaxJsonEntity getServiceFeeCategory(Integer dataSource,HttpServletResponse response){
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity jsonEntity = new AjaxJsonEntity(true);
        if(dataSource==null || dataSource<=0){
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage("数据源不存在");
            return jsonEntity;
        }
        try {
            List<B2BServiceFeeCategory> list = serviceFeeCategoryService.getListByDataSource(dataSource);
            jsonEntity.setSuccess(true);
            jsonEntity.setData(list);
        }catch (Exception e){
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage(e.getMessage());
        }
        return jsonEntity;
    }

}
