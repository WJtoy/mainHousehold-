package com.wolfking.jeesite.ms.b2bcenter.md.web;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.entity.b2bcenter.md.B2BSurchargeCategoryMapping;;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.b2bcenter.md.entity.B2BSurchargeCategoryMappingVModel;
import com.wolfking.jeesite.ms.b2bcenter.md.service.B2BSurchargeCategoryMappingService;
import com.wolfking.jeesite.ms.common.config.MicroServicesProperties;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@Controller
@RequestMapping(value = "${adminPath}/b2bcenter/md/surchargeCategoryMapping/")
public class B2BSurchargeCategoryMappingController extends BaseController {

    @Autowired
    private B2BSurchargeCategoryMappingService surchargeCategoryMappingService;

    @Autowired
    private MicroServicesProperties msProperties;


    /**
     * 分页查询
     *
     * @param surchargeCategoryMapping
     * @return
     */
    @RequiresPermissions("md:b2bsurchargecategory:view")
    @RequestMapping(value = {"getList", ""})
    public String getList(B2BSurchargeCategoryMapping surchargeCategoryMapping, HttpServletRequest request, HttpServletResponse response, Model model){
        Page<B2BSurchargeCategoryMappingVModel> page = new Page<>(request, response);
        if (msProperties.getB2bcenter().getEnabled()) {
            page = surchargeCategoryMappingService.getList(new Page<>(request, response), surchargeCategoryMapping);
        } else {
            addMessage(model, MSErrorCode.MICROSERVICE_DISABLED.msg);
        }
        model.addAttribute("page", page);
        model.addAttribute("surchargeCategoryMapping", surchargeCategoryMapping);
        return "modules/b2bcenter/md/b2bSurchargeCategoryMappingList";
    }


    /**
     * 跳转添加页面
     *
     * @param surchargeCategoryMapping
     * @return
     */
    @RequiresPermissions("md:b2bsurchargecategory:view")
    @RequestMapping(value = "form")
    public String form(B2BSurchargeCategoryMapping surchargeCategoryMapping, Model model) {
        if (!msProperties.getB2bcenter().getEnabled()) {
            addMessage(model, MSErrorCode.MICROSERVICE_DISABLED.msg);
        }
        model.addAttribute("surchargeCategoryMapping", surchargeCategoryMapping);
        return "modules/b2bcenter/md/b2bSurchargeCategoryMappingForm";
    }


    /**
     * 保存数据
     *
     * @param surchargeCategoryMapping
     * @return
     */
    @RequiresPermissions("md:b2bsurchargecategory:edit")
    @RequestMapping("save")
    public String save(B2BSurchargeCategoryMapping surchargeCategoryMapping, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        if (msProperties.getB2bcenter().getEnabled()) {
            if (!beanValidator(model, surchargeCategoryMapping)) {
                return form(surchargeCategoryMapping, model);
            }
            User user = UserUtils.getUser();
            if (user.getId() != null) {
                surchargeCategoryMapping.setCreateById(user.getId());
                surchargeCategoryMapping.setUpdateById(user.getId());
                try {
                    surchargeCategoryMappingService.save(surchargeCategoryMapping);
                    addMessage(redirectAttributes, "保存成功");
                }catch (Exception e){
                    model.addAttribute("message", e.getMessage());
                    return form(surchargeCategoryMapping, model);
                }
            } else {
                addMessage(redirectAttributes, "当前用户不存在");
                return form(surchargeCategoryMapping, model);
            }
        } else {
            addMessage(redirectAttributes, MSErrorCode.MICROSERVICE_DISABLED.msg);
        }
        return "redirect:" + adminPath + "/b2bcenter/md/surchargeCategoryMapping/getList?repage";
    }


    /**
     * 删除
     *
     * @param surchargeCategoryMapping
     * @return
     */
    @RequiresPermissions("md:b2bsurchargecategory:edit")
    @RequestMapping(value = "delete")
    public String delete(B2BSurchargeCategoryMapping surchargeCategoryMapping,Model model,RedirectAttributes redirectAttributes){
        if (msProperties.getB2bcenter().getEnabled()) {
            User user = UserUtils.getUser();
            if(user!=null){
                surchargeCategoryMapping.setUpdateById(user.getId());
                try{
                    surchargeCategoryMappingService.delete(surchargeCategoryMapping);
                    addMessage(redirectAttributes, "删除成功");
                }catch (Exception e){
                    addMessage(redirectAttributes, e.getMessage());
                }
            }else{
                addMessage(redirectAttributes, "当前用户不存在");
            }
        } else {
            addMessage(redirectAttributes, MSErrorCode.MICROSERVICE_DISABLED.msg);
        }
        return "redirect:" + adminPath + "/b2bcenter/md/surchargeCategoryMapping/getList?repage";
    }

}
