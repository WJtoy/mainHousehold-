package com.wolfking.jeesite.ms.providermd.controller;

import com.kkl.kklplus.entity.md.MDAuxiliaryMaterialCategory;
import com.kkl.kklplus.entity.md.MDAuxiliaryMaterialItem;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.Material;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providermd.service.AuxiliaryMaterialCategoryService;
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
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "${adminPath}/provider/md/auxiliaryMaterialCategory/")
public class AuxiliaryMaterialCategoryController extends BaseController {

    @Autowired
    private AuxiliaryMaterialCategoryService auxiliaryMaterialCategoryService;

    /**
     * 分页查询
     *
     * @param auxiliaryMaterialCategory
     * @return
     */
    @RequiresPermissions("md:auxiliarymaterialcategory:view")
    @RequestMapping(value = {"getList", ""})
    public String getList(MDAuxiliaryMaterialCategory auxiliaryMaterialCategory, HttpServletRequest request, HttpServletResponse response, Model model){
        Page<MDAuxiliaryMaterialCategory> page = new Page<>(request, response);
        page = auxiliaryMaterialCategoryService.getList(new Page<>(request, response), auxiliaryMaterialCategory);
        model.addAttribute("page", page);
        model.addAttribute("auxiliaryMaterialCategory", auxiliaryMaterialCategory);
        return "modules/providermd/auxiliaryMaterialCategoryList";
    }


    /**
     * 跳转添加页面
     *
     * @param auxiliaryMaterialCategory
     * @return
     */
    @RequiresPermissions("md:auxiliarymaterialcategory:view")
    @RequestMapping(value = "form")
    public String form(MDAuxiliaryMaterialCategory auxiliaryMaterialCategory, Model model) {

        if(auxiliaryMaterialCategory.getId()!=null && auxiliaryMaterialCategory.getId()>0){
            auxiliaryMaterialCategory = auxiliaryMaterialCategoryService.get(auxiliaryMaterialCategory.getId());
        }
        model.addAttribute("auxiliaryMaterialCategory", auxiliaryMaterialCategory);
        return "modules/providermd/auxiliaryMaterialCategoryForm";
    }


    /**
     * 保存数据
     *
     * @param auxiliaryMaterialCategory
     * @return
     */
    @RequiresPermissions("md:auxiliarymaterialcategory:edit")
    @RequestMapping("save")
    public String save(MDAuxiliaryMaterialCategory auxiliaryMaterialCategory, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        if (!beanValidator(model, auxiliaryMaterialCategory)) {
            return form(auxiliaryMaterialCategory, model);
        }
        User user = UserUtils.getUser();
        if (user.getId() != null) {
            auxiliaryMaterialCategory.setCreateById(user.getId());
            auxiliaryMaterialCategory.setUpdateById(user.getId());
            try {
                auxiliaryMaterialCategoryService.save(auxiliaryMaterialCategory);
                addMessage(redirectAttributes, "保存成功");
            }catch (Exception e){
                model.addAttribute("message", e.getMessage());
                if(auxiliaryMaterialCategory.getId()==null || auxiliaryMaterialCategory.getId()<=0){
                    auxiliaryMaterialCategory = new MDAuxiliaryMaterialCategory();
                }
                model.addAttribute("auxiliaryMaterialCategory", auxiliaryMaterialCategory);
                return "modules/providermd/auxiliaryMaterialCategoryForm";
            }
        } else {
            addMessage(redirectAttributes, "当前用户不存在");
            return form(auxiliaryMaterialCategory, model);
        }
        return "redirect:" + adminPath + "/provider/md/auxiliaryMaterialCategory/getList?repage";
    }


    /**
     * 删除
     *
     * @param auxiliaryMaterialCategory
     * @return
     */
    @RequiresPermissions("md:auxiliarymaterialcategory:edit")
    @RequestMapping(value = "delete")
    public String delete(MDAuxiliaryMaterialCategory auxiliaryMaterialCategory,Model model,RedirectAttributes redirectAttributes){
        User user = UserUtils.getUser();
        if(user!=null){
            auxiliaryMaterialCategory.setUpdateById(user.getId());
            try{
                auxiliaryMaterialCategoryService.delete(auxiliaryMaterialCategory);
                addMessage(redirectAttributes, "删除成功");
            }catch (Exception e){
                addMessage(redirectAttributes,e.getMessage());
            }
        }else{
            addMessage(redirectAttributes, "当前用户不存在");
        }
        return "redirect:" + adminPath + "/provider/md/auxiliaryMaterialCategory/getList?repage";
    }

    /**
     * 获取所有辅件类别
     * @param response
     * @return
     */
    @RequestMapping("ajax/findAllList")
    @ResponseBody
    public AjaxJsonEntity getServiceFeeCategory(HttpServletResponse response){
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity jsonEntity = new AjaxJsonEntity(true);
        try {
            List<MDAuxiliaryMaterialCategory> list = auxiliaryMaterialCategoryService.findAllList();
            jsonEntity.setSuccess(true);
            jsonEntity.setData(list);
        }catch (Exception e){
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage(e.getMessage());
        }
        return jsonEntity;
    }

}
