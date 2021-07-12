package com.wolfking.jeesite.modules.md.web;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.InsurancePrice;
import com.wolfking.jeesite.modules.md.service.InsurancePriceService;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providermd.service.MSProductCategoryNewService;
import com.wolfking.jeesite.ms.providermd.service.MSProductCategoryService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping(value = "${adminPath}/md/insurancePrice")
public class InsurancePriceController extends BaseController {
    @Autowired
    private InsurancePriceService insurancePriceService;

//	@Autowired
//	private MSProductCategoryService msProductCategoryService;  //mark on 2020-4-1

    @Autowired
    private MSProductCategoryNewService msProductCategoryNewService;

    @ModelAttribute
    public InsurancePrice get(@RequestParam(required = false) Long id) {
        if (id != null) {
            return insurancePriceService.get(id);
        } else {
            return new InsurancePrice();
        }
    }

    @RequiresPermissions("md:insuranceprice:view")
    @RequestMapping(value = {"list", ""})
    public String list(InsurancePrice insurancePrice, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<InsurancePrice> page = new Page<>(request, response);
        page = insurancePriceService.findPage(new Page<>(request, response), insurancePrice);
        model.addAttribute("page", page);
        //model.addAttribute("productCategories", msProductCategoryService.findAllList());  // add on 2019-8-14  //mark on 2020-4-1
        model.addAttribute("productCategories", msProductCategoryNewService.findAllListForMDWithEntity());  // add on 2020-4-1
        return "modules/md/insurancePriceNewList";
    }

    @RequiresPermissions("md:insuranceprice:view")
    @RequestMapping(value = "form")
    public String form(InsurancePrice insurancePrice, Model model) {
        //model.addAttribute("productcategory", productCategory);
        model.addAttribute("insurancePrice", insurancePrice);
        return "modules/md/insurancePriceNewForm";
    }

    @RequiresPermissions("md:insuranceprice:edit")
    @RequestMapping(value = "save")
    public String save(InsurancePrice insurancePrice, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        if (!beanValidator(model, insurancePrice)) {
            return form(insurancePrice, model);
        }
        if (insurancePrice.getCategory() != null && insurancePrice.getCategory().getId() != null) {
            insurancePriceService.save(insurancePrice);
            addMessage(redirectAttributes, "保存互助基金配置成功");
        } else {
            addMessage(model, "保存互助基金配置失败，产品分类有误。请重试。");
            return form(insurancePrice, model);
        }

        return "redirect:" + adminPath + "/md/insurancePrice?repage";
    }

    /**
     * 保存数据(添加或修改)
     * @param insurancePrice
     * @return
     */
    @ResponseBody
    @RequiresPermissions("md:insuranceprice:edit")
    @RequestMapping("ajaxSave")
    public AjaxJsonEntity ajaxSave(InsurancePrice insurancePrice, Model model) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        if (!beanValidator(model, insurancePrice)) {
            ajaxJsonEntity.setSuccess(false);
            return ajaxJsonEntity;
        }
        try {
            if (insurancePrice.getCategory() != null && insurancePrice.getCategory().getId() != null) {
                insurancePriceService.save(insurancePrice);
                ajaxJsonEntity.setMessage("保存互助基金配置成功");
            } else {
                ajaxJsonEntity.setSuccess(false);
                ajaxJsonEntity.setMessage("保存互助基金配置失败，产品分类有误。请重试。");
            }
            return ajaxJsonEntity;
        } catch (Exception ex) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage(ex.getMessage());
            return ajaxJsonEntity;
        }

    }

    @RequiresPermissions("md:insuranceprice:edit")
    @RequestMapping(value = "delete")
    public String delete(Long id, RedirectAttributes redirectAttributes) {
        insurancePriceService.delete(new InsurancePrice(id));
        addMessage(redirectAttributes, "删除互助基金配置成功");
        return "redirect:" + adminPath + "/md/insurancePrice?repage";
    }

	/**
	 * 验证名称是否有效
	 *
	 * @return
	 */

	@ResponseBody
	@RequestMapping(value = "checkProductCategory")
	public AjaxJsonEntity checkProductCategory(Long loginId, Long  productCategoryId) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(false);
		Long id = insurancePriceService.getIdByCategoryId(productCategoryId);
		if (id == null) {
		    ajaxJsonEntity.setData(id);
		    ajaxJsonEntity.setSuccess(true);
		} else if (loginId != null && loginId.equals(id)) {
            ajaxJsonEntity.setData(id);
            ajaxJsonEntity.setSuccess(true);
		}
        return ajaxJsonEntity;
	}
}
