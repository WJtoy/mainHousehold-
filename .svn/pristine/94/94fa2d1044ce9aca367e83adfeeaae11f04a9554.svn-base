package com.wolfking.jeesite.ms.b2bcenter.md.web;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.entity.b2bcenter.md.B2BServiceFeeCategory;
import com.kkl.kklplus.entity.b2bcenter.md.B2BServiceFeeItem;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.Product;
import com.wolfking.jeesite.modules.md.service.ProductService;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.b2bcenter.md.service.B2BServiceFeeCategoryService;
import com.wolfking.jeesite.ms.b2bcenter.md.service.B2BServiceFeeItemService;
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
@RequestMapping(value = "${adminPath}/b2bcenter/md/serviceFeeItem/")
public class B2BServiceFeeItemController extends BaseController {

    @Autowired
    private B2BServiceFeeItemService serviceFeeItemService;

    @Autowired
    private MicroServicesProperties msProperties;

    @Autowired
    private B2BServiceFeeCategoryService serviceFeeCategoryService;

    @Autowired
    private ProductService productService;


    /**
     * 分页查询
     *
     * @param serviceFeeItem
     * @return
     */
    @RequiresPermissions("md:b2bservicefeeitem:view")
    @RequestMapping(value = {"getList", ""})
    public String getList(B2BServiceFeeItem serviceFeeItem, HttpServletRequest request, HttpServletResponse response, Model model){
        Page<B2BServiceFeeItem> page = new Page<>(request, response);
        if (msProperties.getB2bcenter().getEnabled()) {
            page = serviceFeeItemService.getList(new Page<>(request, response), serviceFeeItem);
            for(B2BServiceFeeItem entity:page.getList()){
                B2BServiceFeeCategory serviceFeeCategory =serviceFeeCategoryService.get(entity.getCategory().getId());
                if(serviceFeeCategory !=null){
                    entity.getCategory().setCategoryName(serviceFeeCategory.getCategoryName());
                }
                Product product = productService.getProductByIdFromCache(entity.getProductId());
                if(product!=null){
                    entity.setProductName(product.getName());
                }
            }
        } else {
            addMessage(model, MSErrorCode.MICROSERVICE_DISABLED.msg);
        }
        model.addAttribute("page", page);
        model.addAttribute("serviceFeeItem", serviceFeeItem);
        return "modules/b2bcenter/md/b2bServiceFeeItemList";
    }


    /**
     * 跳转添加页面
     *
     * @param serviceFeeItem
     * @return
     */
    @RequiresPermissions("md:b2bservicefeeitem:view")
    @RequestMapping(value = "form")
    public String form(B2BServiceFeeItem serviceFeeItem, Model model) {
        if (!msProperties.getB2bcenter().getEnabled()) {
            addMessage(model, MSErrorCode.MICROSERVICE_DISABLED.msg);
        }
        if(serviceFeeItem.getId()!=null && serviceFeeItem.getId()>0){
            serviceFeeItem = serviceFeeItemService.get(serviceFeeItem.getId());
            B2BServiceFeeCategory serviceFeeCategory =serviceFeeCategoryService.get(serviceFeeItem.getCategory().getId());
            if(serviceFeeCategory !=null){
                serviceFeeItem.getCategory().setCategoryName(serviceFeeCategory.getCategoryName());
            }
            Product product = productService.getProductByIdFromCache(serviceFeeItem.getProductId());
            if(product!=null){
                serviceFeeItem.setProductName(product.getName());
            }
        }
        model.addAttribute("serviceFeeItem", serviceFeeItem);
        return "modules/b2bcenter/md/b2bServiceFeeItemForm";
    }


    /**
     * 保存数据
     *
     * @param serviceFeeItem
     * @return
     */
    @RequiresPermissions("md:b2bservicefeeitem:edit")
    @RequestMapping("save")
    public String save(B2BServiceFeeItem serviceFeeItem, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        if (msProperties.getB2bcenter().getEnabled()) {
            if (!beanValidator(model, serviceFeeItem)) {
                return form(serviceFeeItem, model);
            }
            User user = UserUtils.getUser();
            if (user.getId() != null) {
                serviceFeeItem.setCreateById(user.getId());
                serviceFeeItem.setUpdateById(user.getId());
                try {
                    serviceFeeItemService.save(serviceFeeItem);
                    addMessage(redirectAttributes, "保存成功");
                }catch (Exception e){
                    model.addAttribute("message", e.getMessage());
                    if(serviceFeeItem.getId()==null || serviceFeeItem.getId()<=0){
                        serviceFeeItem = new B2BServiceFeeItem();
                    }
                    model.addAttribute("serviceFeeItem", serviceFeeItem);
                    return "modules/b2bcenter/md/b2bServiceFeeItemForm";
                }
            } else {
                addMessage(redirectAttributes, "当前用户不存在");
                return form(serviceFeeItem, model);
            }
        } else {
            addMessage(redirectAttributes, MSErrorCode.MICROSERVICE_DISABLED.msg);
        }
        return "redirect:" + adminPath + "/b2bcenter/md/serviceFeeItem/getList?repage";
    }


    /**
     * 删除
     *
     * @param serviceFeeItem
     * @return
     */
    @RequiresPermissions("md:b2bservicefeeitem:edit")
    @RequestMapping(value = "delete")
    public String delete(B2BServiceFeeItem serviceFeeItem,Model model,RedirectAttributes redirectAttributes){
        if (msProperties.getB2bcenter().getEnabled()) {
            User user = UserUtils.getUser();
            if(user!=null){
                serviceFeeItem.setUpdateById(user.getId());
                try{
                    serviceFeeItemService.delete(serviceFeeItem);
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
        return "redirect:" + adminPath + "/b2bcenter/md/serviceFeeItem/getList?repage";
    }

}
