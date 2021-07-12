package com.wolfking.jeesite.ms.b2bcenter.md.web;

import com.google.common.collect.Lists;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.entity.b2bcenter.md.B2BSurchargeItemMapping;
import com.kkl.kklplus.entity.md.MDAuxiliaryMaterialItem;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.b2bcenter.md.entity.B2BSurchargeItemMappingVModel;
import com.wolfking.jeesite.ms.b2bcenter.md.service.B2BSurchargeItemMappingService;
import com.wolfking.jeesite.ms.common.config.MicroServicesProperties;
import com.wolfking.jeesite.ms.providermd.service.AuxiliaryMaterialItemService;
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
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "${adminPath}/b2bcenter/md/surchargeItemMapping/")
public class B2BSurchargeItemMappingController extends BaseController {

    @Autowired
    private B2BSurchargeItemMappingService surchargeItemMappingService;

    @Autowired
    private AuxiliaryMaterialItemService auxiliaryMaterialItemService;

    @Autowired
    private MicroServicesProperties msProperties;


    /**
     * 分页查询
     *
     * @param surchargeItemMapping
     * @return
     */
    @RequiresPermissions("md:b2bsurchargeitem:view")
    @RequestMapping(value = {"getList", ""})
    public String getList(B2BSurchargeItemMapping surchargeItemMapping, HttpServletRequest request, HttpServletResponse response, Model model){
        Page<B2BSurchargeItemMappingVModel> page = new Page<>(request, response);
        if (msProperties.getB2bcenter().getEnabled()) {
            page = surchargeItemMappingService.getList(new Page<>(request, response), surchargeItemMapping);
        } else {
            addMessage(model, MSErrorCode.MICROSERVICE_DISABLED.msg);
        }
        model.addAttribute("page", page);
        model.addAttribute("surchargeItemMapping", surchargeItemMapping);
        return "modules/b2bcenter/md/b2bSurchargeItemMappingList";
    }


    /**
     * 跳转添加页面
     *
     * @param surchargeItemMapping
     * @return
     */
    @RequiresPermissions("md:b2bsurchargeitem:view")
    @RequestMapping(value = "form")
    public String form(B2BSurchargeItemMapping surchargeItemMapping, Model model) {
        if (!msProperties.getB2bcenter().getEnabled()) {
            addMessage(model, MSErrorCode.MICROSERVICE_DISABLED.msg);
        }
        Long auxiliaryMaterialCategoryId = 0L;
        if(surchargeItemMapping.getId()!=null && surchargeItemMapping.getId()>0 &&
                surchargeItemMapping.getAuxiliaryMaterialItemId()!=null && surchargeItemMapping.getAuxiliaryMaterialItemId()>0){
            MDAuxiliaryMaterialItem auxiliaryMaterialItem =auxiliaryMaterialItemService.get(surchargeItemMapping.getAuxiliaryMaterialItemId());
            if(auxiliaryMaterialItem!=null){
                auxiliaryMaterialCategoryId = auxiliaryMaterialItem.getCategory().getId();
            }
        }
        model.addAttribute("auxiliaryMaterialCategoryId",auxiliaryMaterialCategoryId);
        model.addAttribute("surchargeItemMapping", surchargeItemMapping);
        return "modules/b2bcenter/md/b2bSurchargeItemMappingForm";
    }


    /**
     * 保存数据
     *
     * @param surchargeItemMapping
     * @return
     */
    @RequiresPermissions("md:b2bsurchargeitem:edit")
    @RequestMapping("save")
    public String save(B2BSurchargeItemMapping surchargeItemMapping, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        if (msProperties.getB2bcenter().getEnabled()) {
            if (!beanValidator(model, surchargeItemMapping)) {
                return form(surchargeItemMapping, model);
            }
            User user = UserUtils.getUser();
            if (user.getId() != null) {
                surchargeItemMapping.setCreateById(user.getId());
                surchargeItemMapping.setUpdateById(user.getId());
                try {
                    surchargeItemMappingService.save(surchargeItemMapping);
                    addMessage(redirectAttributes, "保存成功");
                }catch (Exception e){
                    model.addAttribute("message", e.getMessage());
                    return form(surchargeItemMapping, model);
                }
            } else {
                model.addAttribute("message", "当前用户不存在");
                return form(surchargeItemMapping, model);
            }
        } else {
            addMessage(redirectAttributes, MSErrorCode.MICROSERVICE_DISABLED.msg);
        }
        return "redirect:" + adminPath + "/b2bcenter/md/surchargeItemMapping/getList?repage";
    }


    /**
     * 删除
     *
     * @param surchargeItemMapping
     * @return
     */
    @RequiresPermissions("md:b2bsurchargeitem:edit")
    @RequestMapping(value = "delete")
    public String delete(B2BSurchargeItemMapping surchargeItemMapping,Model model,RedirectAttributes redirectAttributes){
        if (msProperties.getB2bcenter().getEnabled()) {
            User user = UserUtils.getUser();
            if(user!=null){
                surchargeItemMapping.setUpdateById(user.getId());
                try{
                    surchargeItemMappingService.delete(surchargeItemMapping);
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
        return "redirect:" + adminPath + "/b2bcenter/md/surchargeItemMapping/getList?repage";
    }

    /**
     * 根据辅材分类获取辅材项目
     * @param auxiliaryMaterialCategoryId
     * @return
     */
    @RequestMapping("ajax/getAuxiliaryItemListByAuxiliaryCategoryId")
    @ResponseBody
    public AjaxJsonEntity getAuxiliaryItemListByAuxiliaryCategoryId(Long auxiliaryMaterialCategoryId,HttpServletResponse response){
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity jsonEntity = new AjaxJsonEntity(true);
        if(auxiliaryMaterialCategoryId==null || auxiliaryMaterialCategoryId<=0){
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage("辅材分类为空");
            return jsonEntity;
        }
        try {
            List<MDAuxiliaryMaterialItem> list;
            List<MDAuxiliaryMaterialItem> auxiliaryMaterialItemList = auxiliaryMaterialItemService.findAllList();
            if(auxiliaryMaterialItemList!=null && !auxiliaryMaterialItemList.isEmpty()){
                list = auxiliaryMaterialItemList.stream().filter(t->t.getCategory().getId().equals(auxiliaryMaterialCategoryId))
                                                                               .collect(Collectors.toList());
                if(list!=null && !list.isEmpty()){
                    jsonEntity.setSuccess(true);
                    jsonEntity.setData(list);
                }else{
                    jsonEntity.setSuccess(true);
                    list = Lists.newArrayList();
                    jsonEntity.setData(list);
                }
            }else{
                jsonEntity.setSuccess(true);
                list = Lists.newArrayList();
                jsonEntity.setData(list);
            }
        }catch (Exception e){
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage(e.getMessage());
        }
        return jsonEntity;
    }

}
