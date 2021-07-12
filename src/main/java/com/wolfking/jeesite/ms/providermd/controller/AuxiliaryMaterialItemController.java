package com.wolfking.jeesite.ms.providermd.controller;

import com.kkl.kklplus.entity.md.MDAuxiliaryMaterialCategory;
import com.kkl.kklplus.entity.md.MDAuxiliaryMaterialItem;
import com.wolfking.jeesite.common.persistence.Page;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.Product;
import com.wolfking.jeesite.modules.md.service.ProductService;
import com.wolfking.jeesite.modules.md.utils.ProductUtils;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providermd.service.AuxiliaryMaterialCategoryService;
import com.wolfking.jeesite.ms.providermd.service.AuxiliaryMaterialItemService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "${adminPath}/provider/md/auxiliaryMaterialItem/")
public class AuxiliaryMaterialItemController extends BaseController {

    @Autowired
    private AuxiliaryMaterialItemService auxiliaryMaterialItemService;

    @Autowired
    private AuxiliaryMaterialCategoryService auxiliaryMaterialCategoryService;

    @Autowired
    private ProductService productService;


    /**
     * 分页查询
     *
     * @param auxiliaryMaterialItem
     * @return
     */
    @RequiresPermissions("md:auxiliarymaterialitem:view")
    @RequestMapping(value = {"getList", ""})
    public String getList(MDAuxiliaryMaterialItem auxiliaryMaterialItem, HttpServletRequest request, HttpServletResponse response, Model model){
        Page<MDAuxiliaryMaterialItem> page = new Page<>(request, response);
        page = auxiliaryMaterialItemService.getList(new Page<>(request, response), auxiliaryMaterialItem);
        List<MDAuxiliaryMaterialCategory> list = auxiliaryMaterialCategoryService.findAllList();
        if(page.getList()!=null && page.getList().size()>0){
            Map<Long,MDAuxiliaryMaterialCategory> map = list.stream().collect(Collectors.toMap(MDAuxiliaryMaterialCategory::getId, auxiliaryMaterialCategory -> auxiliaryMaterialCategory));
            Map<Long,Product> productMap = ProductUtils.getAllProductMap();
            for(MDAuxiliaryMaterialItem entity:page.getList()){
                entity.setCategory(map.get(entity.getCategory().getId()));
                Product product = productMap.get(entity.getProductId());
                if(product!=null){
                    entity.setProductName(product.getName());
                }
            }
        }
        model.addAttribute("page", page);
        model.addAttribute("auxiliaryMaterialItem", auxiliaryMaterialItem);
        model.addAttribute("auxiliaryMaterialCategoryList", list);
        return "modules/providermd/auxiliaryMaterialItemList";
    }


    /**
     * 跳转添加页面
     *
     * @param auxiliaryMaterialItem
     * @return
     */
    @RequiresPermissions("md:auxiliarymaterialitem:view")
    @RequestMapping(value = "form")
    public String form(MDAuxiliaryMaterialItem auxiliaryMaterialItem, Model model) {
        if(auxiliaryMaterialItem.getId()!=null && auxiliaryMaterialItem.getId()>0){
            auxiliaryMaterialItem = auxiliaryMaterialItemService.get(auxiliaryMaterialItem.getId());
            Product product = productService.getProductByIdFromCache(auxiliaryMaterialItem.getProductId());
            if(product!=null){
                auxiliaryMaterialItem.setProductName(product.getName());
            }
        }
        model.addAttribute("auxiliaryMaterialItem", auxiliaryMaterialItem);
        return "modules/providermd/auxiliaryMaterialItemForm";
    }


    /**
     * 保存数据
     *
     * @param auxiliaryMaterialItem
     * @return
     */
    @RequiresPermissions("md:auxiliarymaterialitem:edit")
    @RequestMapping("save")
    public String save(MDAuxiliaryMaterialItem auxiliaryMaterialItem, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        if (!beanValidator(model, auxiliaryMaterialItem)) {
            return form(auxiliaryMaterialItem, model);
        }
        User user = UserUtils.getUser();
        if (user.getId() != null) {
            auxiliaryMaterialItem.setCreateById(user.getId());
            auxiliaryMaterialItem.setUpdateById(user.getId());
            try {
                auxiliaryMaterialItemService.save(auxiliaryMaterialItem);
                addMessage(redirectAttributes, "保存成功");
            }catch (Exception e){
                model.addAttribute("message", e.getMessage());
                model.addAttribute("auxiliaryMaterialItem", auxiliaryMaterialItem);
                return form(auxiliaryMaterialItem, model);
            }
        } else {
            addMessage(redirectAttributes, "当前用户不存在");
            return form(auxiliaryMaterialItem, model);
        }
        return "redirect:" + adminPath + "/provider/md/auxiliaryMaterialItem/getList?repage";
    }


    /**
     * 删除
     *
     * @param auxiliaryMaterialItem
     * @return
     */
    @RequiresPermissions("md:auxiliarymaterialitem:edit")
    @RequestMapping(value = "delete")
    public String delete(MDAuxiliaryMaterialItem auxiliaryMaterialItem,Model model,RedirectAttributes redirectAttributes){
        User user = UserUtils.getUser();
        if(user!=null){
            auxiliaryMaterialItem.setUpdateById(user.getId());
            try{
                auxiliaryMaterialItemService.delete(auxiliaryMaterialItem);
                addMessage(redirectAttributes, "删除成功");
            }catch (Exception e){
                addMessage(redirectAttributes, e.getMessage());
            }
        }else{
            addMessage(redirectAttributes, "当前用户不存在");
        }
        return "redirect:" + adminPath + "/provider/md/auxiliaryMaterialItem/getList?repage";
    }

}
