package com.wolfking.jeesite.modules.md.web;

import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.Brand;
import com.wolfking.jeesite.modules.md.entity.BrandCategory;
import com.wolfking.jeesite.modules.md.service.BrandCategoryService;
import com.wolfking.jeesite.modules.md.service.BrandService;
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
import java.util.List;

@Controller
@RequestMapping(value = "${adminPath}/md/brand")
public class BrandController extends BaseController
{
	@Autowired
	private BrandService brandService;

	@Autowired
	private BrandCategoryService brandCategoryService;

	@ModelAttribute
	public Brand get(@RequestParam(required = false) Long id)
	{
		if (id != null)
		{
			return brandService.get(id);
		} else
		{
			return new Brand();
		}
	}

	@RequiresPermissions("md:brand:view")
	@RequestMapping(value = { "list", "" })
	public String list(Brand brand, HttpServletRequest request, HttpServletResponse response, Model model)
	{
		Page<Brand> page = brandService.findPage(new Page<>(request, response), brand);
		model.addAttribute("page", page);
		return "modules/md/brandList";
	}

	@RequiresPermissions("md:brand:view")
	@RequestMapping(value = "form")
	public String form(Brand brand, Model model)
	{
		model.addAttribute("brand", brand);
		return "modules/md/brandForm";
	}

	@RequiresPermissions("md:brand:edit")
	@RequestMapping(value = "save")
	public String save(Brand brand, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes)
	{
		if (!beanValidator(model, brand))
		{
			return form(brand, model);
		}
		brandService.save(brand);
		addMessage(redirectAttributes, "保存品牌'" + brand.getName() + "'成功");
		return "redirect:" + adminPath + "/md/brand/list?repage";
	}

	@RequiresPermissions("md:brand:edit")
	@RequestMapping(value = "delete")
	public String delete(Long id, RedirectAttributes redirectAttributes)
	{
		BrandCategory brandCategory=new BrandCategory();
		brandCategory.setBrand(new Brand(id));
		List<BrandCategory> list = brandCategoryService.findList(brandCategory);
		if (list != null && list.size()>0) {
			addMessage(redirectAttributes, "删除品牌失败，该品牌已关联产品分类");
		} else {
			brandService.delete(new Brand(id));
			addMessage(redirectAttributes, "删除品牌成功");
		}
		return "redirect:" + adminPath + "/md/brand/list?repage";
	}

	@ResponseBody
	@RequestMapping(value = "checkBrandCode")
	public String checkProductCategoryCode(Long id, String code, HttpServletResponse response)
	{
		response.setContentType("application/json; charset=UTF-8");
		Brand productCategory = new Brand();
		productCategory.setId(id);
		productCategory.setCode(code);
		//false ：品牌编码已经存在,true:不存在
		return brandService.isExistBrandCode(productCategory) ? "品牌编码已经存在" : "true";
	}

	@ResponseBody
	@RequestMapping(value = "checkBrandName")
	public String checkProductCategoryName(Long id, String name, HttpServletResponse response)
	{
		response.setContentType("application/json; charset=UTF-8");
		Brand productCategory = new Brand();
		productCategory.setId(id);
		productCategory.setName(name);
		//false ：品牌名字已经存在,true:不存在
		return brandService.isExistBrandName(productCategory) ? "品牌名称已经存在" : "true";
	}
}
